package com.voidtouch.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.AppRepository
import com.voidtouch.launcher.data.DeviceStats
import com.voidtouch.launcher.data.DeviceStatsReader
import com.voidtouch.launcher.data.FavoritesStore
import com.voidtouch.launcher.data.FocusStore
import com.voidtouch.launcher.ui.AppList
import com.voidtouch.launcher.ui.HealthScreen
import com.voidtouch.launcher.ui.HomeContent
import com.voidtouch.launcher.data.MemoryMode
import com.voidtouch.launcher.data.MemoryReader
import com.voidtouch.launcher.data.MemoryShown
import androidx.activity.result.contract.ActivityResultContracts
import com.voidtouch.launcher.ui.Banner
import com.voidtouch.launcher.ui.BannerHost
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.voidtouch.launcher.ui.OptimizeScreen
import com.voidtouch.launcher.ui.ChangelogScreen
import com.voidtouch.launcher.data.Changelog
import com.voidtouch.launcher.data.ReleaseNote
import com.voidtouch.launcher.notify.NoteItem
import com.voidtouch.launcher.notify.NotificationHub
import android.provider.Settings
import android.net.Uri
import com.voidtouch.launcher.ui.SettingsScreen
import com.voidtouch.launcher.data.ClockStyle
import com.voidtouch.launcher.data.SettingsStore
import com.voidtouch.launcher.data.SwipeSide
import com.voidtouch.launcher.ui.theme.Backdrop
import com.voidtouch.launcher.ui.theme.VoidTouchTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    /** Muda a cada volta à tela, para recarregar a lista (app instalado ou removido). */
    private var refreshKey by mutableIntStateOf(0)

    /** Aumenta quando o botão Home é apertado com o launcher já aberto (volta para a home). */
    private var homeSignal by mutableIntStateOf(0)

    /** Horário atual, atualizado pelo sistema a cada minuto enquanto a tela está visível. */
    private var nowMillis by mutableLongStateOf(System.currentTimeMillis())

    private var stats by mutableStateOf<DeviceStats?>(null)

    private var pinnedKeys by mutableStateOf<List<String>>(emptyList())

    /** Apps marcados como distração; somem da home, da lista e da busca com o foco ligado. */
    private var focusApps by mutableStateOf<Set<String>>(emptySet())

    /** Horário em que o modo foco foi ligado; zero significa desligado. */
    private var focusSince by mutableLongStateOf(0L)

    private var backdrop by mutableStateOf(Backdrop.Void)

    private var clockStyle by mutableStateOf(ClockStyle.H)

    private var swipeLeftKey by mutableStateOf<String?>(null)

    private var swipeRightKey by mutableStateOf<String?>(null)

    private lateinit var settingsStore: SettingsStore

    /** Verdadeiro quando o acesso às notificações está liberado; relido a cada volta à tela. */
    private var notifAccess by mutableStateOf(false)

    /** Verdadeiro só com a tela visível; o relógio do player para quando o app sai de cena. */
    private var screenActive by mutableStateOf(false)

    private var memoryMode by mutableStateOf(MemoryMode.Off)
    private var hiddenPhotos by mutableStateOf<Set<Long>>(emptySet())
    private var photoAccess by mutableStateOf(false)

    private val photoPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        photoAccess = MemoryReader.hasAccess(this)
        if (photoAccess) {
            say("memórias ligadas: ${memoryMode.label}")
        } else {
            memoryMode = MemoryMode.Off
            settingsStore.saveMemoryMode(memoryMode)
            say("sem acesso às fotos: memórias desligadas")
        }
    }

    private fun cycleMemoryMode() {
        val next = memoryMode.next()
        memoryMode = next
        settingsStore.saveMemoryMode(next)
        if (next == MemoryMode.Off) {
            say("memórias desligadas")
        } else if (!MemoryReader.hasAccess(this)) {
            say("o Android vai pedir acesso às fotos")
            photoPermission.launch(MemoryReader.permissionsToRequest())
        } else {
            say("memórias: ${next.label}")
        }
    }

    /** Aviso na tela (no lugar do Toast); some sozinho. */
    private var banner by mutableStateOf<Banner?>(null)
    private var bannerSeq = 0

    private fun say(text: String) {
        banner = Banner(++bannerSeq, text)
    }

    private val versionName: String
        get() = packageManager.getPackageInfo(packageName, 0).versionName ?: ""

    private lateinit var favoritesStore: FavoritesStore

    private lateinit var focusStore: FocusStore

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshClock()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bars = SystemBarStyle.dark(Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        favoritesStore = FavoritesStore(prefs)
        pinnedKeys = favoritesStore.load()
        focusStore = FocusStore(prefs)
        focusApps = focusStore.loadApps()
        focusSince = focusStore.loadSince()
        settingsStore = SettingsStore(prefs)
        backdrop = settingsStore.loadBackdrop()
        clockStyle = settingsStore.loadClockStyle()
        swipeLeftKey = settingsStore.loadSwipeApp(SwipeSide.Left)
        swipeRightKey = settingsStore.loadSwipeApp(SwipeSide.Right)
        memoryMode = settingsStore.loadMemoryMode()
        hiddenPhotos = settingsStore.loadHiddenPhotos()
        photoAccess = MemoryReader.hasAccess(this)
        applyBackdrop()

        // Depois de uma atualização (ou na primeira instalação), mostra as novidades uma vez.
        val seen = prefs.getString(KEY_SEEN_VERSION, null)
        val initialNotes: List<ReleaseNote>? =
            if (seen != versionName) Changelog.forVersion(versionName)?.let { listOf(it) } else null
        prefs.edit().putString(KEY_SEEN_VERSION, versionName).apply()

        setContent {
            VoidTouchTheme {
                val apps by produceState(emptyList<AppEntry>(), refreshKey) {
                    value = withContext(Dispatchers.Default) { AppRepository.loadApps(applicationContext) }
                }
                var showAllApps by remember { mutableStateOf(false) }
                // Verdadeiro quando a lista foi aberta pela pílula de busca (já abre o teclado).
                var focusSearch by remember { mutableStateOf(false) }
                var query by remember { mutableStateOf("") }
                var showHealth by remember { mutableStateOf(false) }
                var showSettings by remember { mutableStateOf(false) }
                var showOptimize by remember { mutableStateOf(false) }
                // Novidades: após atualizar só a versão nova; em Ajustes, o histórico todo.
                var changelog by remember { mutableStateOf(initialNotes) }
                var changelogTitle by remember { mutableStateOf("novidades da v$versionName") }
                // Verdadeiro enquanto se marca os apps do foco na lista completa.
                var markingFocusApps by remember { mutableStateOf(false) }

                // Apertar Home com o launcher aberto sempre volta para a home.
                LaunchedEffect(homeSignal) {
                    showAllApps = false
                    showHealth = false
                    showSettings = false
                    changelog = null
                    showOptimize = false
                }
                // Ao sair da lista completa, a busca é limpa e a marcação termina.
                LaunchedEffect(showAllApps) {
                    if (!showAllApps) {
                        query = ""
                        focusSearch = false
                        markingFocusApps = false
                    }
                }
                // Um launcher não fecha com o botão voltar; aqui ele só sai das telas internas.
                BackHandler {
                    when {
                        changelog != null -> changelog = null
                        showOptimize -> showOptimize = false
                        showSettings -> showSettings = false
                        showHealth -> showHealth = false
                        showAllApps && markingFocusApps -> markingFocusApps = false
                        showAllApps -> showAllApps = false
                    }
                }

                val focusOn = focusSince != 0L
                val favorites = remember(apps, pinnedKeys, focusApps, focusOn) {
                    val byKey = apps.associateBy { it.key }
                    pinnedKeys.mapNotNull { byKey[it] }
                        .filter { !focusOn || it.key !in focusApps }
                }

                // Com o foco ligado, notificações dos apps marcados ficam escondidas.
                val notifications = NotificationHub.items.filter { n ->
                    !focusOn || focusApps.none { it.substringBefore('/') == n.packageName }
                }

                // A foto "neste dia" é escolhida uma vez por bloco de 3 horas, fora da thread principal.
                val memory by produceState<MemoryShown?>(null, memoryMode, photoAccess, hiddenPhotos, nowMillis / 10_800_000L) {
                    value = withContext(Dispatchers.IO) {
                        MemoryReader.pick(applicationContext, nowMillis, memoryMode, hiddenPhotos)?.let { m ->
                            MemoryReader.thumbnail(applicationContext, m)?.let { MemoryShown(m, it) }
                        }
                    }
                }

                val screen = when {
                    changelog != null -> "changelog"
                    showOptimize -> "optimize"
                    showSettings -> "settings"
                    showHealth -> "health"
                    showAllApps -> "drawer"
                    else -> "home"
                }
                // Guarda as novidades para o texto não sumir durante a animação de saída.
                var lastNotes by remember { mutableStateOf(emptyList<ReleaseNote>()) }
                changelog?.let { lastNotes = it }

                LaunchedEffect(banner) {
                    if (banner != null) {
                        delay(2400)
                        banner = null
                    }
                }

                Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                  AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        if (targetState == "home") {
                            // Voltar para a home: a tela de cima some e a home aparece por baixo.
                            fadeIn(tween(200)) togetherWith fadeOut(tween(140))
                        } else {
                            // Ir para outra tela: entra subindo um pouco, com fade.
                            (fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 14 }) togetherWith
                                fadeOut(tween(120))
                        }
                    },
                    label = "screen",
                  ) { current ->
                    if (current == "changelog") {
                        ChangelogScreen(title = changelogTitle, notes = changelog ?: lastNotes, onClose = { changelog = null })
                    } else if (current == "optimize") {
                        OptimizeScreen(apps = apps, refreshKey = refreshKey, onMessage = ::say, onClose = { showOptimize = false })
                    } else if (current == "settings") {
                        SettingsScreen(
                            apps = apps,
                            backdrop = backdrop,
                            clockStyle = clockStyle,
                            swipeLeftKey = swipeLeftKey,
                            swipeRightKey = swipeRightKey,
                            versionName = versionName,
                            notificationsEnabled = notifAccess,
                            onOpenNotificationAccess = ::openNotificationAccess,
                            onOpenAppInfo = ::openAppInfo,
                            memoryMode = memoryMode,
                            hiddenPhotos = hiddenPhotos.size,
                            onMemoryMode = ::cycleMemoryMode,
                            onRestorePhotos = {
                                hiddenPhotos = emptySet()
                                settingsStore.saveHiddenPhotos(hiddenPhotos)
                                say("fotos ocultas restauradas")
                            },
                            onOpenOptimize = { showSettings = false; showOptimize = true },
                            onOpenChangelog = {
                                changelogTitle = "novidades"
                                changelog = Changelog.all
                            },
                            onBackdrop = {
                                backdrop = it
                                settingsStore.saveBackdrop(it)
                                applyBackdrop()
                            },
                            onClockStyle = {
                                clockStyle = it
                                settingsStore.saveClockStyle(it)
                            },
                            onSwipeApp = { side, key ->
                                if (side == SwipeSide.Left) swipeLeftKey = key else swipeRightKey = key
                                settingsStore.saveSwipeApp(side, key)
                            },
                            onClose = { showSettings = false },
                        )
                    } else if (current == "health") {
                        HealthScreen(
                            apps = apps,
                            nowMillis = nowMillis,
                            refreshKey = refreshKey,
                            onClose = { showHealth = false },
                        )
                    } else if (current == "drawer") {
                        AppList(
                            apps = apps,
                            pinnedKeys = pinnedKeys,
                            focusApps = focusApps,
                            focusModeOn = focusOn,
                            markingFocusApps = markingFocusApps,
                            onMarkingChange = { markingFocusApps = it },
                            onToggleFocusApp = { toggleFocusApp(it) },
                            query = query,
                            onQueryChange = { query = it },
                            focusSearch = focusSearch,
                            onLaunch = { AppRepository.launch(this@MainActivity, it) },
                            onTogglePin = { togglePin(it) },
                            onOpenSettings = { showSettings = true },
                            onClose = { showAllApps = false },
                        )
                    } else {
                        HomeContent(
                            nowMillis = nowMillis,
                            stats = stats,
                            clockStyle = clockStyle,
                            notifications = notifications,
                            notificationsEnabled = notifAccess,
                            onNotificationClick = { openNotification(it) },
                            onNotificationDismiss = { NotificationHub.dismiss(it.key) },
                            onEnableNotifications = ::openNotificationAccess,
                            active = screenActive,
                            memory = memory,
                            onMemoryClick = {
                                memory?.let {
                                    runCatching {
                                        startActivity(
                                            Intent(Intent.ACTION_VIEW).setDataAndType(it.memory.uri, "image/*")
                                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                                        )
                                    }
                                }
                            },
                            onMemoryHide = {
                                memory?.let {
                                    hiddenPhotos = hiddenPhotos + it.memory.id
                                    settingsStore.saveHiddenPhotos(hiddenPhotos)
                                    say("foto oculta: não aparece mais")
                                }
                            },
                            focusSinceMillis = focusSince,
                            favorites = favorites,
                            onLaunch = { AppRepository.launch(this@MainActivity, it) },
                            onUnpin = { togglePin(it) },
                            onOpenDrawer = {
                                focusSearch = false
                                showAllApps = true
                            },
                            onOpenSearch = {
                                focusSearch = true
                                showAllApps = true
                            },
                            onOpenHealth = { showHealth = true },
                            onToggleFocus = ::toggleFocus,
                            onOpenSettings = { showSettings = true },
                            onSwipeLeft = { launchByKey(apps, swipeLeftKey, focusOn) },
                            onSwipeRight = { launchByKey(apps, swipeRightKey, focusOn) },
                        )
                    }
                  }
                    BannerHost(
                        banner = banner,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp),
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) homeSignal++
    }

    override fun onStart() {
        super.onStart()
        screenActive = true
        refreshClock()
        // Relógio, bateria e RAM só atualizam enquanto a tela está visível; em segundo plano não gastam nada.
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(this, timeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        screenActive = false
        unregisterReceiver(timeReceiver)
    }

    override fun onResume() {
        super.onResume()
        notifAccess = NotificationHub.hasAccess(this)
        photoAccess = MemoryReader.hasAccess(this)
        refreshKey++
    }

    private fun refreshClock() {
        nowMillis = System.currentTimeMillis()
        stats = DeviceStatsReader.read(applicationContext)
    }

    private fun openNotificationAccess() {
        runCatching { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
    }

    private fun openAppInfo() {
        runCatching {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        }
    }

    private fun openNotification(item: NoteItem) {
        runCatching { item.intent?.send() }
    }

    private fun togglePin(app: AppEntry) {
        val current = pinnedKeys
        pinnedKeys = when {
            app.key in current -> {
                say("${app.label} saiu da home")
                current - app.key
            }
            current.size >= FavoritesStore.MAX -> {
                say("máximo de ${FavoritesStore.MAX} favoritos: solte um para fixar outro")
                current
            }
            else -> {
                say("${app.label} fixado na home · ${current.size + 1}/${FavoritesStore.MAX}")
                current + app.key
            }
        }
        favoritesStore.save(pinnedKeys)
    }

    /** Liga ou desliga o foco. Sem nenhum app marcado não há o que esconder, então avisa em vez de ligar. */
    private fun toggleFocus() {
        if (focusSince != 0L) {
            focusSince = 0L
            say("foco desligado")
        } else if (focusApps.isEmpty()) {
            say("marque os apps do foco: lista completa > apps do foco")
            return
        } else {
            focusSince = System.currentTimeMillis()
            say("foco ligado · ${focusApps.size} app(s) escondido(s)")
        }
        focusStore.saveSince(focusSince)
    }

    private fun toggleFocusApp(app: AppEntry) {
        focusApps = if (app.key in focusApps) focusApps - app.key else focusApps + app.key
        focusStore.saveApps(focusApps)
    }

    private fun launchByKey(apps: List<AppEntry>, key: String?, focusOn: Boolean) {
        if (key == null) {
            say("defina o app desse deslize em ajustes")
            return
        }
        val app = apps.firstOrNull { it.key == key } ?: return
        if (focusOn && app.key in focusApps) {
            say("${app.label} está escondido pelo modo foco")
            return
        }
        say("abrindo ${app.label}…")
        AppRepository.launch(this, app)
    }

    private fun applyBackdrop() {
        window.setBackgroundDrawable(ColorDrawable(backdrop.argb))
    }

    private companion object {
        const val PREFS = "voidtouch"
        const val KEY_SEEN_VERSION = "seen_version"
    }
}
