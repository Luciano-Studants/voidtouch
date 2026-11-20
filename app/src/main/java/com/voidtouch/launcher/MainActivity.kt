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
import android.view.WindowManager
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
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
import com.voidtouch.launcher.data.Agenda
import com.voidtouch.launcher.data.AgendaItem
import com.voidtouch.launcher.data.Backup
import com.voidtouch.launcher.data.FocusAction
import com.voidtouch.launcher.data.HomeRules
import com.voidtouch.launcher.data.FocusSchedule
import com.voidtouch.launcher.data.FocusSession
import com.voidtouch.launcher.data.ContactsReader
import com.voidtouch.launcher.data.Hit
import com.voidtouch.launcher.data.LaterItem
import com.voidtouch.launcher.data.LaunchCounts
import com.voidtouch.launcher.data.ReadLater
import com.voidtouch.launcher.data.LiveKind
import com.voidtouch.launcher.data.Dock
import com.voidtouch.launcher.data.NightMode
import com.voidtouch.launcher.data.Note
import com.voidtouch.launcher.data.Notes
import com.voidtouch.launcher.data.Predict
import com.voidtouch.launcher.data.Scene
import com.voidtouch.launcher.data.Scenes
import java.time.Instant
import java.time.ZoneId
import com.voidtouch.launcher.data.Shortcuts
import com.voidtouch.launcher.data.Favorite
import com.voidtouch.launcher.ui.TipsScreen
import android.os.SystemClock
import com.voidtouch.launcher.data.FavoriteKeys
import com.voidtouch.launcher.data.Folder
import com.voidtouch.launcher.data.GestureTarget
import com.voidtouch.launcher.data.IconLoader
import com.voidtouch.launcher.data.QuickAction
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.voidtouch.launcher.data.FolderStore
import com.voidtouch.launcher.data.Folders
import com.voidtouch.launcher.data.MemoryMode
import com.voidtouch.launcher.data.MemoryReader
import com.voidtouch.launcher.data.Perf
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
import com.voidtouch.launcher.ui.CaptureScreen
import com.voidtouch.launcher.ui.DockScreen
import com.voidtouch.launcher.ui.PermissionsScreen
import com.voidtouch.launcher.ui.UsageScreen
import com.voidtouch.launcher.ui.ReadLaterScreen
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
    private var focusUntil by mutableLongStateOf(0L)
    private var focusSummary by mutableStateOf<String?>(null)

    private var backdrop by mutableStateOf(Backdrop.Void)

    private var clockStyle by mutableStateOf(ClockStyle.H)

    private var gestures by mutableStateOf<Map<SwipeSide, String?>>(emptyMap())

    /** Muda quando um app é instalado, removido ou atualizado: só então a lista de apps é relida. */
    private var appsKey by mutableIntStateOf(0)

    private var torchOn = false

    private var calendarAccess by mutableStateOf(false)

    private val calendarPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        calendarAccess = granted
        say(if (granted) "agenda ligada" else "sem acesso ao calendário")
    }

    private fun openCalendar(nowMillis: Long) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.calendar/time/$nowMillis")))
        }.onFailure { say("não foi possível abrir o calendário") }
    }

    private var focusAutoOn by mutableStateOf(false)
    private var focusAutoStart by mutableIntStateOf(FocusSchedule.DEFAULT_START)
    private var focusAutoEnd by mutableIntStateOf(FocusSchedule.DEFAULT_END)

    private var opens by mutableStateOf<Map<String, Int>>(emptyMap())
    private var later by mutableStateOf<List<LaterItem>>(emptyList())
    private var notes by mutableStateOf<List<Note>>(emptyList())
    private var dockOn by mutableStateOf(true)
    private var dockAwake by mutableStateOf(true)
    private var lastTouchMs = System.currentTimeMillis()
    private var predictOn by mutableStateOf(true)
    private var predictHistory by mutableStateOf<Map<Int, Map<String, Int>>>(emptyMap())
    private var contactsAccess by mutableStateOf(false)

    private val contactsPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        contactsAccess = granted
        say(if (granted) "contatos ligados na busca" else "sem acesso aos contatos")
    }

    private var scenes by mutableStateOf<List<Scene>>(emptyList())
    private var sceneActive by mutableStateOf<String?>(null)
    private var sceneAuto by mutableStateOf(false)

    private var nightEnabled by mutableStateOf(false)
    private var nightStart by mutableIntStateOf(NightMode.DEFAULT_START)
    private var nightEnd by mutableIntStateOf(NightMode.DEFAULT_END)

    private val createdAt = SystemClock.elapsedRealtime()
    private var openTimeMs by mutableLongStateOf(0L)

    private val backupSave = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            val ok = runCatching {
                contentResolver.openOutputStream(uri)?.use {
                    it.write(Backup.export(getSharedPreferences(PREFS, MODE_PRIVATE)).toByteArray())
                } != null
            }.getOrDefault(false)
            say(if (ok) "backup salvo" else "não foi possível salvar o backup")
        }
    }

    private val backupRestore = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = runCatching { contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } }.getOrNull()
            val problem = if (text == null) "não foi possível ler o arquivo" else Backup.import(getSharedPreferences(PREFS, MODE_PRIVATE), text)
            if (problem == null) {
                // Recria a tela para reler tudo (favoritos, pastas, gestos...) já restaurado.
                recreate()
            } else {
                say("$problem; nada foi alterado")
            }
        }
    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            appsKey++
        }
    }

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            torchOn = enabled
        }
    }

    private lateinit var settingsStore: SettingsStore

    private lateinit var folderStore: FolderStore

    private var folders by mutableStateOf<List<Folder>>(emptyList())

    /** Verdadeiro quando o acesso às notificações está liberado; relido a cada volta à tela. */
    private var notifAccess by mutableStateOf(false)

    /** Verdadeiro só com a tela visível; o relógio do player para quando o app sai de cena. */
    private var screenActive by mutableStateOf(false)

    private var memoryMode by mutableStateOf(MemoryMode.Off)
    private var hiddenPhotos by mutableStateOf<Set<Long>>(emptySet())
    private var photoAccess by mutableStateOf(false)

    private val photoPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        photoAccess = MemoryReader.hasAccess(this)
        calendarAccess = Agenda.hasAccess(this)
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
        focusUntil = focusStore.loadUntil()
        settingsStore = SettingsStore(prefs)
        folderStore = FolderStore(prefs)
        folders = folderStore.load()
        backdrop = settingsStore.loadBackdrop()
        clockStyle = settingsStore.loadClockStyle()
        gestures = SwipeSide.entries.associateWith { settingsStore.loadSwipeApp(it) }
        memoryMode = settingsStore.loadMemoryMode()
        nightEnabled = settingsStore.loadNightEnabled()
        scenes = settingsStore.loadScenes()
        opens = LaunchCounts.decode(settingsStore.loadLaunchCounts())
        later = settingsStore.loadReadLater()
        predictOn = settingsStore.loadPredictOn()
        dockOn = settingsStore.loadDockOn()
        dockAwake = settingsStore.loadDockAwake()
        notes = settingsStore.loadNotes()
        predictHistory = Predict.decode(settingsStore.loadPredictHistory())
        contactsAccess = ContactsReader.hasAccess(this)
        sceneActive = settingsStore.loadSceneActive()?.takeIf { id -> scenes.any { it.id == id } }
        sceneAuto = settingsStore.loadSceneAuto()
        focusAutoOn = settingsStore.loadFocusAutoOn()
        focusAutoStart = settingsStore.loadFocusAutoStart()
        focusAutoEnd = settingsStore.loadFocusAutoEnd()
        nightStart = settingsStore.loadNightStart()
        nightEnd = settingsStore.loadNightEnd()
        hiddenPhotos = settingsStore.loadHiddenPhotos()
        photoAccess = MemoryReader.hasAccess(this)
        applyBackdrop()

        // Só relê a lista de apps quando algo é instalado, removido ou atualizado.
        val pkgFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(this, packageReceiver, pkgFilter, ContextCompat.RECEIVER_NOT_EXPORTED)

        // Depois de uma atualização (ou na primeira instalação), mostra as novidades uma vez.
        val seen = prefs.getString(KEY_SEEN_VERSION, null)
        val initialNotes: List<ReleaseNote>? =
            if (seen != versionName) Changelog.forVersion(versionName)?.let { listOf(it) } else null
        prefs.edit().putString(KEY_SEEN_VERSION, versionName).apply()
        val firstUse = !prefs.getBoolean(KEY_TIPS_SEEN, false)
        if (firstUse) prefs.edit().putBoolean(KEY_TIPS_SEEN, true).apply()
        openTimeMs = prefs.getLong(KEY_OPEN_MS, 0L)

        setContent {
            VoidTouchTheme {
                val apps by produceState(emptyList<AppEntry>(), appsKey) {
                    value = withContext(Dispatchers.Default) { AppRepository.loadApps(applicationContext) }
                }
                // Com a lista pronta, prepara os ícones em segundo plano (baixa prioridade) para a grade rolar lisa.
                LaunchedEffect(apps) {
                    if (apps.isNotEmpty()) {
                        if (!Perf.stats.has(Perf.READY)) Perf.stats.record(Perf.READY, SystemClock.elapsedRealtime() - createdAt)
                        delay(600)
                        withContext(Dispatchers.Default) {
                            apps.forEach { IconLoader.load(applicationContext, it) }
                        }
                    }
                }
                var showAllApps by remember { mutableStateOf(false) }
                // Pasta que a lista já abre aberta (quando se toca numa pasta fixada na home).
                var startFolder by remember { mutableStateOf<String?>(null) }
                // Verdadeiro quando a lista foi aberta pela pílula de busca (já abre o teclado).
                var focusSearch by remember { mutableStateOf(false) }
                var query by remember { mutableStateOf("") }
                var showHealth by remember { mutableStateOf(false) }
                var showSettings by remember { mutableStateOf(false) }
                var showOptimize by remember { mutableStateOf(false) }
                var showPermissions by remember { mutableStateOf(false) }
                var showLater by remember { mutableStateOf(false) }
                var showCapture by remember { mutableStateOf(false) }
                var showUsage by remember { mutableStateOf(false) }
                var showDock by remember { mutableStateOf(false) }
                var dockDismissed by remember { mutableStateOf(false) }
                var showTips by remember { mutableStateOf(firstUse) }
                // Novidades: após atualizar só a versão nova; em Ajustes, o histórico todo.
                var changelog by remember { mutableStateOf(initialNotes) }
                var changelogTitle by remember { mutableStateOf("novidades da v$versionName") }
                // Verdadeiro enquanto se marca os apps do foco na lista completa.
                var markingFocusApps by remember { mutableStateOf(false) }

                // Mede quanto a primeira tela levou para aparecer, para saber se a abertura está rápida.
                LaunchedEffect(Unit) {
                    val ms = SystemClock.elapsedRealtime() - createdAt
                    openTimeMs = ms
                    Perf.stats.record(Perf.OPEN, ms)
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().putLong(KEY_OPEN_MS, ms).apply()
                }

                // Apertar Home com o launcher aberto sempre volta para a home.
                LaunchedEffect(homeSignal) {
                    showAllApps = false
                    showHealth = false
                    showSettings = false
                    changelog = null
                    showOptimize = false
                    showPermissions = false
                    showLater = false
                    showCapture = false
                    showUsage = false
                    showTips = false
                }
                // Mede quanto a lista de apps leva para aparecer (até o primeiro quadro desenhado).
                LaunchedEffect(showAllApps) {
                    if (showAllApps) {
                        val t0 = SystemClock.elapsedRealtime()
                        withFrameNanos { }
                        Perf.stats.record(Perf.DRAWER, SystemClock.elapsedRealtime() - t0)
                    }
                }
                // Ao sair da lista completa, a busca é limpa e a marcação termina.
                LaunchedEffect(showAllApps) {
                    if (!showAllApps) {
                        startFolder = null
                        query = ""
                        focusSearch = false
                        markingFocusApps = false
                    }
                }
                // Um launcher não fecha com o botão voltar; aqui ele só sai das telas internas.
                BackHandler {
                    when {
                        showDock -> { showDock = false; dockDismissed = true }
                        changelog != null -> changelog = null
                        showTips -> showTips = false
                        showUsage -> showUsage = false
                        showCapture -> showCapture = false
                        showLater -> showLater = false
                        showPermissions -> showPermissions = false
                        showOptimize -> showOptimize = false
                        showSettings -> showSettings = false
                        showHealth -> showHealth = false
                        showAllApps && markingFocusApps -> markingFocusApps = false
                        showAllApps -> showAllApps = false
                    }
                }

                val focusOn = focusSince != 0L

                // Executa o destino de um gesto: abre um app ou faz uma ação rápida.
                val runGesture: (SwipeSide) -> Unit = { side ->
                    val target = gestures[side]
                    val action = GestureTarget.action(target)
                    when {
                        target == null -> say("defina esse gesto em ajustes")
                        action == QuickAction.Torch -> toggleTorch()
                        action == QuickAction.NotificationPanel -> expandNotificationPanel()
                        action == QuickAction.Search -> { focusSearch = true; showAllApps = true }
                        action == QuickAction.Drawer -> { focusSearch = false; showAllApps = true }
                        action == QuickAction.Focus -> toggleFocus()
                        action == QuickAction.Capture -> showCapture = true
                        action == QuickAction.Usage -> showUsage = true
                        action == QuickAction.Optimize -> showOptimize = true
                        action == QuickAction.Health -> showHealth = true
                        action == QuickAction.Settings -> showSettings = true
                        else -> launchByKey(apps, target, focusOn)
                    }
                }
                val favorites = remember(apps, pinnedKeys, folders, focusApps, focusOn) {
                    HomeRules.favorites(apps, pinnedKeys, folders, focusApps, focusOn)
                }

                // Com o foco ligado, notificações dos apps marcados ficam escondidas.
                val notifications = NotificationHub.items.filter { n ->
                    !HomeRules.notificationHidden(n.packageName, focusApps, focusOn)
                }

                // A foto "neste dia" é escolhida uma vez por bloco de 3 horas, fora da thread principal.
                // Modo noite: dentro da janela de horário a home fica escura e calma.
                val hour = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()).hour
                // Sessão de foco com hora para acabar: espera até o fim e encerra. Reinicia a cada volta ao
                // app (refreshKey), então uma sessão que venceu com a tela apagada acaba assim que você volta.
                LaunchedEffect(focusUntil, focusSince, refreshKey) {
                    if (focusUntil > 0L && focusSince != 0L) {
                        delay(FocusSession.remainingMs(focusUntil, System.currentTimeMillis()))
                        finishFocus()
                        say("sessão de foco terminou")
                    }
                }
                // Linha "agora": apps prováveis neste horário, sem repetir os favoritos nem os escondidos pelo foco.
                val suggestions = remember(apps, predictHistory, predictOn, hour, pinnedKeys, focusApps, focusOn) {
                    if (!predictOn) {
                        emptyList()
                    } else {
                        val byKey = apps.associateBy { it.key }
                        val skip = pinnedKeys.toSet() + (if (focusOn) focusApps else emptySet())
                        Predict.suggest(predictHistory, hour, skip).mapNotNull { byKey[it] }
                    }
                }
                val quietScene = scenes.firstOrNull { it.id == sceneActive }?.quiet == true
                val night = (nightEnabled && NightMode.isNight(hour, nightStart, nightEnd)) || quietScene

                // Próximos compromissos, relidos a cada 5 minutos com a tela visível.
                val agenda by produceState(emptyList<AgendaItem>(), calendarAccess, nowMillis / 300_000L) {
                    value = withContext(Dispatchers.IO) { Agenda.read(applicationContext, nowMillis) }
                }

                // Foco automático: só age quando a janela de horário vira (entrou ou saiu).
                LaunchedEffect(nowMillis / 60_000L, focusAutoOn, focusAutoStart, focusAutoEnd) {
                    if (focusAutoOn) applyFocusSchedule(hour)
                }

                // Cenas automáticas: só agem quando o horário escolhe uma cena diferente da última vez.
                LaunchedEffect(nowMillis / 60_000L, sceneAuto, scenes.size) {
                    if (sceneAuto) applySceneSchedule(hour)
                }

                val memory by produceState<MemoryShown?>(null, memoryMode, photoAccess, hiddenPhotos, nowMillis / 10_800_000L) {
                    value = withContext(Dispatchers.IO) {
                        MemoryReader.pick(applicationContext, nowMillis, memoryMode, hiddenPhotos)?.let { m ->
                            MemoryReader.thumbnail(applicationContext, m)?.let { MemoryShown(m, it) }
                        }
                    }
                }

                val screen = when {
                    showTips -> "tips"
                    changelog != null -> "changelog"
                    showUsage -> "usage"
                    showCapture -> "capture"
                    showLater -> "later"
                    showPermissions -> "permissions"
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

                // Modo carregando: com o celular carregando e a home parada, mostra o relógio de cabeceira.
                val charging = stats?.charging == true
                val onHomeNow = screen == "home"
                LaunchedEffect(charging, onHomeNow, dockOn, dockDismissed) {
                    if (!charging) dockDismissed = false
                    showDock = false
                    if (dockOn && charging && onHomeNow && !dockDismissed) {
                        while (!Dock.shouldShow(true, true, true, false, System.currentTimeMillis() - lastTouchMs)) delay(1000)
                        showDock = true
                    }
                }
                DisposableEffect(showDock, dockAwake) {
                    if (showDock && dockAwake) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            // Só observa os toques (sem consumir) para saber há quanto tempo a home está parada.
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent(PointerEventPass.Initial)
                                    lastTouchMs = System.currentTimeMillis()
                                }
                            }
                        }
                        .safeDrawingPadding(),
                ) {
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
                    if (current == "tips") {
                        TipsScreen(onClose = { showTips = false })
                    } else if (current == "changelog") {
                        ChangelogScreen(title = changelogTitle, notes = changelog ?: lastNotes, onClose = { changelog = null })
                    } else if (current == "usage") {
                        UsageScreen(refreshKey = refreshKey, onClose = { showUsage = false })
                    } else if (current == "capture") {
                        CaptureScreen(
                            notes = notes,
                            onAdd = { text ->
                                saveNotes(Notes.add(notes, "n" + System.currentTimeMillis(), text, System.currentTimeMillis()))
                                say("anotado")
                            },
                            onToggleDone = { saveNotes(Notes.toggleDone(notes, it.id)) },
                            onSetFocus = { saveNotes(Notes.setFocus(notes, it.id)); say("tarefa do foco") },
                            onRemove = { saveNotes(Notes.remove(notes, it.id)) },
                            onClearDone = { saveNotes(Notes.clearDone(notes)) },
                            onClose = { showCapture = false },
                        )
                    } else if (current == "later") {
                        ReadLaterScreen(
                            items = later,
                            onOpenApp = { item ->
                                packageManager.getLaunchIntentForPackage(item.pkg)?.let { runCatching { startActivity(it) } }
                            },
                            onRemove = { item ->
                                later = ReadLater.remove(later, item.id)
                                settingsStore.saveReadLater(later)
                            },
                            onClose = { showLater = false },
                        )
                    } else if (current == "permissions") {
                        PermissionsScreen(
                            refreshKey = refreshKey,
                            notificationAccess = notifAccess,
                            onOpenNotificationAccess = ::openNotificationAccess,
                            onRequestPhotos = { photoPermission.launch(MemoryReader.permissionsToRequest()) },
                            contactsAccess = contactsAccess,
                            onRequestContacts = { contactsPermission.launch(android.Manifest.permission.READ_CONTACTS) },
                            onRequestCalendar = { calendarPermission.launch(android.Manifest.permission.READ_CALENDAR) },
                            onOpenAppInfo = ::openAppInfo,
                            onClose = { showPermissions = false },
                        )
                    } else if (current == "optimize") {
                        OptimizeScreen(apps = apps, refreshKey = refreshKey, onMessage = ::say, onClose = { showOptimize = false })
                    } else if (current == "settings") {
                        SettingsScreen(
                            apps = apps,
                            backdrop = backdrop,
                            clockStyle = clockStyle,
                            gestures = gestures,
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
                            onOpenPermissions = { showSettings = false; showPermissions = true },
                            onOpenUsage = { showSettings = false; showUsage = true },
                            focusAutoOn = focusAutoOn,
                            focusAutoStart = focusAutoStart,
                            focusAutoEnd = focusAutoEnd,
                            onFocusAutoToggle = {
                                focusAutoOn = !focusAutoOn
                                settingsStore.saveFocusAutoOn(focusAutoOn)
                                // Recém-ligado: nulo faz valer o horário atual na hora.
                                settingsStore.saveFocusAutoLast(null)
                                say(if (focusAutoOn) "foco automático: ${focusAutoStart}h às ${focusAutoEnd}h" else "foco automático desligado")
                            },
                            onFocusAutoStart = {
                                focusAutoStart = (focusAutoStart + 1) % 24
                                settingsStore.saveFocusAutoHours(focusAutoStart, focusAutoEnd)
                                settingsStore.saveFocusAutoLast(null)
                            },
                            onFocusAutoEnd = {
                                focusAutoEnd = (focusAutoEnd + 1) % 24
                                settingsStore.saveFocusAutoHours(focusAutoStart, focusAutoEnd)
                                settingsStore.saveFocusAutoLast(null)
                            },
                            dockOn = dockOn,
                            onDockToggle = {
                                dockOn = !dockOn
                                settingsStore.saveDockOn(dockOn)
                                say(if (dockOn) "modo carregando ligado" else "modo carregando desligado")
                            },
                            dockAwake = dockAwake,
                            onDockAwake = {
                                dockAwake = !dockAwake
                                settingsStore.saveDockAwake(dockAwake)
                            },
                            predictOn = predictOn,
                            onPredictToggle = {
                                predictOn = !predictOn
                                settingsStore.savePredictOn(predictOn)
                                say(if (predictOn) "linha agora ligada" else "linha agora desligada")
                            },
                            onPredictClear = {
                                predictHistory = emptyMap()
                                settingsStore.savePredictHistory("")
                                say("histórico apagado")
                            },
                            scenes = scenes,
                            sceneActive = sceneActive,
                            sceneAuto = sceneAuto,
                            onSceneAuto = {
                                sceneAuto = !sceneAuto
                                settingsStore.saveSceneAuto(sceneAuto)
                                settingsStore.saveSceneLastPick(null)
                                say(if (sceneAuto) "cenas automáticas ligadas" else "cenas automáticas desligadas")
                            },
                            onSceneUse = { id -> activateScene(id) },
                            onSceneEdit = { edited -> saveScenes(scenes.map { if (it.id == edited.id) edited else it }) },
                            onSceneAdd = {
                                val created = Scenes.create(scenes, "s" + System.currentTimeMillis())
                                if (created == null) say("máximo de ${Scenes.MAX} cenas") else saveScenes(scenes + created)
                            },
                            onSceneDelete = { id ->
                                if (sceneActive == id) activateScene(null)
                                saveScenes(scenes.filter { it.id != id })
                            },
                            nightEnabled = nightEnabled,
                            nightStart = nightStart,
                            nightEnd = nightEnd,
                            onNightToggle = {
                                nightEnabled = !nightEnabled
                                settingsStore.saveNightEnabled(nightEnabled)
                                say(if (nightEnabled) "modo noite ligado: ${nightStart}h às ${nightEnd}h" else "modo noite desligado")
                            },
                            onNightStart = {
                                nightStart = (nightStart + 1) % 24
                                settingsStore.saveNightHours(nightStart, nightEnd)
                            },
                            onNightEnd = {
                                nightEnd = (nightEnd + 1) % 24
                                settingsStore.saveNightHours(nightStart, nightEnd)
                            },
                            onOpenTips = { showSettings = false; showTips = true },
                            onBackupSave = { backupSave.launch("voidtouch-backup.json") },
                            onBackupRestore = { backupRestore.launch(arrayOf("application/json", "text/plain", "*/*")) },
                            openTimeMs = openTimeMs,
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
                                gestures = gestures + (side to key)
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
                            onLaunch = { launchApp(it) },
                            onTogglePin = { togglePin(it) },
                            folders = folders,
                            onFolderCreate = { app, name ->
                                updateFolders(Folders.create(folders, name, app.key))
                                say("pasta criada com ${app.label}")
                            },
                            onFolderAdd = { id, app ->
                                updateFolders(Folders.addApp(folders, id, app.key))
                                say("${app.label} entrou na pasta")
                            },
                            onFolderRemoveApp = { app ->
                                updateFolders(Folders.removeApp(folders, app.key))
                                say("${app.label} saiu da pasta")
                            },
                            onFolderRename = { id, name ->
                                updateFolders(Folders.rename(folders, id, name))
                                say("pasta renomeada")
                            },
                            onTogglePinFolder = { togglePinFolder(it) },
                            startFolderId = startFolder,
                            onFolderDelete = { id ->
                                updateFolders(Folders.delete(folders, id))
                                say("pasta excluída: os apps voltaram à lista")
                            },
                            onAppInfo = { openPackageInfo(it.packageName) },
                            onUninstall = { uninstall(it) },
                            onStartShortcut = { sc ->
                                if (!Shortcuts.start(this@MainActivity, sc)) say("não foi possível abrir o atalho")
                            },
                            opens = opens,
                            onHit = { hit ->
                                openHit(hit) { action ->
                                    when (action) {
                                        QuickAction.Torch -> toggleTorch()
                                        QuickAction.NotificationPanel -> expandNotificationPanel()
                                        QuickAction.Search -> focusSearch = true
                                        QuickAction.Drawer -> Unit
                                        QuickAction.Focus -> toggleFocus()
                                        QuickAction.Capture -> { showAllApps = false; showCapture = true }
                                        QuickAction.Usage -> { showAllApps = false; showUsage = true }
                                        QuickAction.Optimize -> { showAllApps = false; showOptimize = true }
                                        QuickAction.Health -> { showAllApps = false; showHealth = true }
                                        QuickAction.Settings -> { showAllApps = false; showSettings = true }
                                    }
                                }
                            },
                            onOpenSettings = { showSettings = true },
                            onClose = { showAllApps = false },
                        )
                    } else {
                        HomeContent(
                            nowMillis = nowMillis,
                            stats = stats,
                            clockStyle = clockStyle,
                            notifications = notifications,
                            suggestions = suggestions,
                            focusNote = Notes.focusNote(notes),
                            onNoteDone = { saveNotes(Notes.toggleDone(notes, it.id)); say("feito: ${it.text}") },
                            onOpenCapture = { showCapture = true },
                            live = NotificationHub.live.filter { it.kind == LiveKind.Call || !HomeRules.notificationHidden(it.packageName, focusApps, focusOn) },
                            onLiveOpen = { runCatching { it.intent?.send() } },
                            notificationsEnabled = notifAccess,
                            onNotificationClick = { openNotification(it) },
                            onNotificationDismiss = { NotificationHub.dismiss(it.key) },
                            onNotificationReply = { item, text ->
                                say(if (NotificationHub.reply(this@MainActivity, item, text)) "resposta enviada" else "não foi possível responder")
                            },
                            onNotificationLater = { item ->
                                later = ReadLater.add(
                                    later,
                                    LaterItem(item.key + System.currentTimeMillis(), item.packageName, item.appLabel, item.title, item.text, System.currentTimeMillis()),
                                )
                                settingsStore.saveReadLater(later)
                                NotificationHub.dismiss(item.key)
                                say("guardado em ler depois")
                            },
                            laterCount = later.size,
                            onOpenLater = { showLater = true },
                            onEnableNotifications = ::openNotificationAccess,
                            active = screenActive,
                            agenda = agenda,
                            agendaEnabled = calendarAccess,
                            onEnableAgenda = { calendarPermission.launch(android.Manifest.permission.READ_CALENDAR) },
                            onAgendaClick = { openCalendar(nowMillis) },
                            memory = if (night) null else memory,
                            night = night,
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
                            focusUntilMillis = focusUntil,
                            onStartFocus = { startFocus(it) },
                            focusSummary = focusSummary,
                            onDismissFocusSummary = { focusSummary = null },
                            favorites = favorites,
                            onLaunch = { launchApp(it) },
                            onUnpin = { fav ->
                                when (fav) {
                                    is Favorite.App -> togglePin(fav.app)
                                    is Favorite.FolderItem -> togglePinFolder(fav.folder)
                                }
                            },
                            onOpenFolder = { f ->
                                focusSearch = false
                                startFolder = f.id
                                showAllApps = true
                            },
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
                            onSwipeLeft = { runGesture(SwipeSide.Left) },
                            onSwipeRight = { runGesture(SwipeSide.Right) },
                            onSwipeDown = { runGesture(SwipeSide.Down) },
                            onDoubleTap = { runGesture(SwipeSide.DoubleTap) },
                        )
                    }
                  }
                    if (showDock) {
                        DockScreen(
                            nowMillis = nowMillis,
                            clockStyle = clockStyle,
                            stats = stats,
                            media = NotificationHub.media,
                            onExit = { showDock = false; dockDismissed = true },
                        )
                    }
                    BannerHost(
                        banner = banner,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp),
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(packageReceiver) }
        super.onDestroy()
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
        runCatching { getSystemService(CameraManager::class.java).registerTorchCallback(torchCallback, null) }
    }

    override fun onStop() {
        super.onStop()
        screenActive = false
        unregisterReceiver(timeReceiver)
        runCatching { getSystemService(CameraManager::class.java).unregisterTorchCallback(torchCallback) }
    }

    override fun onResume() {
        super.onResume()
        notifAccess = NotificationHub.hasAccess(this)
        photoAccess = MemoryReader.hasAccess(this)
        calendarAccess = Agenda.hasAccess(this)
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

    private fun updateFolders(list: List<Folder>) {
        folders = list
        folderStore.save(list)
        // Pasta que deixou de existir sai dos favoritos.
        val cleaned = FavoriteKeys.withoutMissingFolders(pinnedKeys, list)
        if (cleaned != pinnedKeys) {
            pinnedKeys = cleaned
            favoritesStore.save(cleaned)
        }
    }

    private fun openPackageInfo(pkg: String) {
        runCatching {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", pkg, null)))
        }
    }

    private fun uninstall(app: AppEntry) {
        runCatching {
            startActivity(Intent(Intent.ACTION_DELETE, Uri.fromParts("package", app.packageName, null)))
        }.onFailure { say("não foi possível desinstalar ${app.label}") }
    }

    private fun openNotification(item: NoteItem) {
        runCatching { item.intent?.send() }
    }

    private fun togglePin(app: AppEntry) = togglePinKey(app.key, app.label)

    private fun togglePinFolder(folder: Folder) = togglePinKey(FavoriteKeys.folder(folder.id), "pasta ${folder.name}")

    /** Fixa ou solta um app ou pasta nos favoritos da home (limite de 10 no total). */
    private fun togglePinKey(key: String, label: String) {
        val (next, result) = HomeRules.togglePin(pinnedKeys, key, FavoritesStore.MAX)
        when (result) {
            HomeRules.PinResult.Removed -> say("$label saiu da home")
            HomeRules.PinResult.Full -> say("máximo de ${FavoritesStore.MAX} favoritos: solte um para fixar outro")
            HomeRules.PinResult.Added -> say("$label fixado na home · ${next.size}/${FavoritesStore.MAX}")
        }
        pinnedKeys = next
        favoritesStore.save(pinnedKeys)
    }

    /** Liga ou desliga o foco. Sem nenhum app marcado não há o que esconder, então avisa em vez de ligar. */
    private fun toggleFocus() {
        when (HomeRules.toggleFocus(focusSince, focusApps)) {
            HomeRules.FocusToggle.TurnedOff -> {
                finishFocus()
                say("foco desligado")
            }
            HomeRules.FocusToggle.NeedsApps -> {
                say("marque os apps do foco: lista completa > apps do foco")
                return
            }
            HomeRules.FocusToggle.TurnedOn -> {
                focusSince = System.currentTimeMillis()
                focusUntil = 0L
                focusSummary = null
                say("foco ligado · ${focusApps.size} app(s) escondido(s)")
            }
        }
        focusStore.saveSince(focusSince)
        focusStore.saveUntil(focusUntil)
    }

    /** Liga uma sessão de foco; [minutes] zero é sem fim. */
    private fun startFocus(minutes: Int) {
        if (focusApps.isEmpty()) {
            say("marque os apps do foco: lista completa > apps do foco")
            return
        }
        val now = System.currentTimeMillis()
        focusSince = now
        focusUntil = if (minutes > 0) now + minutes * 60_000L else 0L
        focusSummary = null
        focusStore.saveSince(focusSince)
        focusStore.saveUntil(focusUntil)
        say(if (minutes > 0) "foco por $minutes min · ${focusApps.size} app(s) escondido(s)" else "foco ligado · ${focusApps.size} app(s) escondido(s)")
    }

    /**
     * Encerra o foco (à mão, pelo horário, por cena ou ao acabar a sessão) e prepara o resumo:
     * quanto durou e quantas notificações dos apps escondidos estão esperando na bandeja.
     */
    private fun finishFocus() {
        if (focusSince == 0L) return
        val since = focusSince
        val waiting = NotificationHub.items.count { n ->
            n.postedAt >= since && focusApps.any { it.substringBefore('/') == n.packageName }
        }
        focusSummary = FocusSession.summary(System.currentTimeMillis() - since, waiting)
        focusSince = 0L
        focusUntil = 0L
        focusStore.saveSince(0L)
        focusStore.saveUntil(0L)
    }

    /** Liga ou desliga o foco nas viradas da janela programada; sem apps marcados, só avisa. */
    private fun applyFocusSchedule(hour: Int) {
        val inWindow = NightMode.isNight(hour, focusAutoStart, focusAutoEnd)
        val last = settingsStore.loadFocusAutoLast()
        when (FocusSchedule.decide(inWindow, last)) {
            FocusAction.TurnOn -> {
                if (focusApps.isEmpty()) {
                    say("foco automático: marque os apps do foco na lista")
                } else if (focusSince == 0L) {
                    focusSince = System.currentTimeMillis()
                    focusStore.saveSince(focusSince)
                    say("foco ligado automaticamente")
                }
            }
            FocusAction.TurnOff -> if (focusSince != 0L) {
                finishFocus()
                say("foco desligado automaticamente")
            }
            FocusAction.None -> Unit
        }
        if (last != inWindow) settingsStore.saveFocusAutoLast(inWindow)
    }

    /** Abre o app e conta a abertura, para a busca mostrar primeiro o que você mais usa. */
    private fun launchApp(app: AppEntry) {
        opens = LaunchCounts.increment(opens, app.key)
        if (predictOn) {
            val hour = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).hour
            predictHistory = Predict.record(predictHistory, hour, app.key)
            settingsStore.savePredictHistory(Predict.encode(predictHistory))
        }
        settingsStore.saveLaunchCounts(LaunchCounts.encode(opens))
        AppRepository.launch(this, app)
    }

    /** Executa um resultado da busca que não é app. */
    private fun openHit(hit: Hit, runAction: (QuickAction) -> Unit) {
        when (hit) {
            is Hit.Answer -> {
                val text = hit.title.substringAfter("= ", hit.title)
                (getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager)
                    .setPrimaryClip(android.content.ClipData.newPlainText("resultado", text))
                say("copiado: $text")
            }
            is Hit.Action -> runAction(hit.action)
            is Hit.Setting -> runCatching { startActivity(Intent(hit.intentAction).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                .onFailure { say("não foi possível abrir ${hit.title}") }
            is Hit.Contact -> runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(hit.uri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                .onFailure { say("não foi possível abrir o contato") }
        }
    }

    private fun saveNotes(list: List<Note>) {
        notes = list
        settingsStore.saveNotes(list)
    }

    private fun saveScenes(list: List<Scene>) {
        scenes = list
        settingsStore.saveScenes(list)
    }

    /** Troca de cena: guarda os favoritos da que sai, mostra os da que entra e ajusta o foco. */
    private fun activateScene(id: String?) {
        if (id == sceneActive) return
        val leaving = scenes.firstOrNull { it.id == sceneActive }
        val entering = scenes.firstOrNull { it.id == id }
        val result = Scenes.switch(scenes, settingsStore.loadSceneDefaultPins(), sceneActive, id, pinnedKeys)
        saveScenes(result.scenes)
        settingsStore.saveSceneDefaultPins(result.defaultPins)
        if (result.pins != pinnedKeys) {
            pinnedKeys = result.pins
            favoritesStore.save(pinnedKeys)
        }
        sceneActive = id
        settingsStore.saveSceneActive(id)
        if (leaving?.focus == true && focusSince != 0L) finishFocus()
        if (entering?.focus == true) {
            if (focusApps.isEmpty()) {
                say("cena ${entering.name}: marque os apps do foco para ele ligar")
            } else if (focusSince == 0L) {
                focusSince = System.currentTimeMillis()
                focusStore.saveSince(focusSince)
            }
        }
        say(if (entering != null) "cena: ${entering.name}" else "sem cena")
    }

    /** O horário escolhe a cena; só troca quando a escolha muda, para não desfazer uma troca manual. */
    private fun applySceneSchedule(hour: Int) {
        val picked = Scenes.pick(scenes, hour)?.id
        if (picked == settingsStore.loadSceneLastPick()) return
        settingsStore.saveSceneLastPick(picked)
        activateScene(picked)
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
        launchApp(app)
    }

    /** Liga ou desliga a lanterna (não precisa de permissão). */
    private fun toggleTorch() {
        runCatching {
            val manager = getSystemService(CameraManager::class.java)
            val id = manager.cameraIdList.firstOrNull { cam ->
                val c = manager.getCameraCharacteristics(cam)
                c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: return say("este aparelho não tem lanterna")
            manager.setTorchMode(id, !torchOn)
            say(if (torchOn) "lanterna desligada" else "lanterna ligada")
        }.onFailure { say("não foi possível usar a lanterna") }
    }

    /** Abre o painel de notificações do sistema (método interno do Android; pode não existir em todo aparelho). */
    private fun expandNotificationPanel() {
        runCatching {
            val service = getSystemService("statusbar")
            Class.forName("android.app.StatusBarManager").getMethod("expandNotificationsPanel").invoke(service)
        }.onFailure { say("este aparelho não permite abrir o painel por aqui") }
    }

    private fun applyBackdrop() {
        window.setBackgroundDrawable(ColorDrawable(backdrop.argb))
    }

    private companion object {
        const val PREFS = "voidtouch"
        const val KEY_SEEN_VERSION = "seen_version"
        const val KEY_TIPS_SEEN = "tips_seen"
        const val KEY_OPEN_MS = "last_open_ms"
    }
}
