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
import com.voidtouch.launcher.data.Design
import com.voidtouch.launcher.data.Hit
import com.voidtouch.launcher.data.RotinaTokens
import com.voidtouch.launcher.data.HighVis
import com.voidtouch.launcher.data.DailyOpens
import com.voidtouch.launcher.ui.PauseScreen
import com.voidtouch.launcher.data.LaterItem
import com.voidtouch.launcher.data.LaunchCounts
import com.voidtouch.launcher.data.ReadLater
import com.voidtouch.launcher.data.LiveKind
import com.voidtouch.launcher.data.Dock
import com.voidtouch.launcher.data.NightMode
import com.voidtouch.launcher.data.OpenAnim
import com.voidtouch.launcher.data.Screen
import com.voidtouch.launcher.data.guarded
import kotlinx.coroutines.launch
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.cancel
import com.voidtouch.launcher.data.ColorSets
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.FavoritesOrderScreen
import com.voidtouch.launcher.ui.OrderRow
import com.voidtouch.launcher.data.Parallax
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
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
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var openAnim by mutableStateOf(true)
    private var design by mutableStateOf(Design.Void)
    private var highVis by mutableStateOf(false)
    private var lightAuto by mutableStateOf(false)
    private var lightState = com.voidtouch.launcher.data.LightAuto.State()
    private val lightListener = object : android.hardware.SensorEventListener {
        override fun onSensorChanged(e: android.hardware.SensorEvent) {
            val lux = e.values.firstOrNull() ?: return
            lightState = com.voidtouch.launcher.data.LightAuto.next(lightState, lux)
            if (lightState.on != highVis) applyHighVis(lightState.on, persist = false)
        }
        override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
    }
    private val lightSensor: android.hardware.Sensor? by lazy {
        runCatching { getSystemService(android.hardware.SensorManager::class.java).getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT) }.getOrNull()
    }
    private var pauseOn by mutableStateOf(false)
    private var pausePending by mutableStateOf<AppEntry?>(null)
    private var dailyOpens = DailyOpens.empty("")
    private var parallax by mutableStateOf(true)
    private var colorSet by mutableStateOf(ColorSets.DEFAULT)
    // Criado só no primeiro uso (depois do onCreate), quando o contexto do app já está pronto.
    private val background by lazy {
        BackgroundController(
        context = this,
        backdrop = { backdrop },
        parallax = { parallax },
        colorSet = { colorSet },
        screenActive = { screenActive },
        applyWindow = ::applyBackdrop,
        )
    }
    private var openHaptic by mutableStateOf(true)
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

    /** Imagem própria de fundo: copiada em tamanho de tela para o armazenamento privado do app. */
    private val ownImagePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            background.importOwn(uri) { ok ->
                if (ok) {
                    backdrop = Backdrop.Wallpaper
                    settingsStore.saveBackdrop(backdrop)
                    background.refresh()
                    say("imagem de fundo definida")
                } else {
                    say("não foi possível usar essa imagem")
                }
            }
        }
    }

    private val backupRestore = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = runCatching { contentResolver.openInputStream(uri)?.use { input ->
                // Lê no máximo um byte além do limite: arquivo enorme vira "grande demais" sem encher a memória.
                val buf = ByteArray(Backup.MAX_BYTES + 1)
                var n = 0
                while (n < buf.size) {
                    val r = input.read(buf, n, buf.size - n)
                    if (r < 0) break
                    n += r
                }
                String(buf, 0, n, Charsets.UTF_8)
            } }.getOrNull()
            val problem = if (text == null) "não foi possível ler o arquivo" else Backup.import(getSharedPreferences(PREFS, MODE_PRIVATE), text)
            if (problem == null) {
                // Recria a tela para reler tudo (favoritos, pastas, gestos...) já restaurado.
                recreate()
            } else {
                say("$problem; nada foi alterado")
            }
        }
    }

    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val reloadApps = Runnable { appsKey++ }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.data?.schemeSpecificPart?.let { IconLoader.invalidate(it) }
            // Instalar ou atualizar vários apps de uma vez gera uma rajada de avisos: junta tudo numa só releitura.
            mainHandler.removeCallbacks(reloadApps)
            mainHandler.postDelayed(reloadApps, 800)
        }
    }

    private val system by lazy { SystemActions(this, ::say) }

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
        // Travou várias vezes seguidas: abre a tela de emergência em vez da home normal.
        if (CrashGuard.inSafeMode(this)) {
            startActivity(Intent(this, SafeModeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))
            finish()
            return
        }
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
        openAnim = settingsStore.loadOpenAnim()
        design = Design.fromKey(settingsStore.loadDesign())
        VoidColors.design = design
        highVis = settingsStore.loadHighVis()
        pauseOn = settingsStore.loadPauseOn()
        lightAuto = settingsStore.loadLightAuto()
        dailyOpens = DailyOpens.decode(settingsStore.loadDailyOpens(), java.time.LocalDate.now().toString())
        VoidColors.highVis = highVis
        parallax = settingsStore.loadParallax()
        colorSet = settingsStore.loadColorSet()
        openHaptic = settingsStore.loadOpenHaptic()
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
                    value = withContext(Dispatchers.Default) { guarded("lista de apps", emptyList()) { AppRepository.loadApps(applicationContext) } }
                }
                // Com a lista pronta, prepara os ícones em segundo plano (baixa prioridade) para a grade rolar lisa.
                LaunchedEffect(apps) {
                    if (apps.isNotEmpty()) {
                        if (!Perf.stats.has(Perf.READY)) Perf.stats.record(Perf.READY, SystemClock.elapsedRealtime() - createdAt)
                        delay(600)
                        withContext(Dispatchers.Default) {
                            apps.forEach {
                                // Se a lista mudou no meio, para aqui em vez de terminar um trabalho que já não vale.
                                coroutineContext.ensureActive()
                                guarded("ícone", Unit) { IconLoader.load(applicationContext, it) }
                            }
                        }
                    }
                }
                // Pasta que a lista já abre aberta (quando se toca numa pasta fixada na home).
                var startFolder by remember { mutableStateOf<String?>(null) }
                // Verdadeiro quando a lista foi aberta pela pílula de busca (já abre o teclado).
                var focusSearch by remember { mutableStateOf(false) }
                var query by remember { mutableStateOf("") }
                // Qual tela interna está aberta (só uma por vez); o relógio de cabeceira e as novidades têm estado próprio.
                val nav = remember { Navigator(if (firstUse) Screen.Tips else Screen.Home) }
                var showDock by remember { mutableStateOf(false) }
                var dockDismissed by remember { mutableStateOf(false) }
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

                // Ficou de pé por 30 segundos: a versão atual é considerada saudável e a contagem de travamentos zera.
                LaunchedEffect(Unit) {
                    delay(30_000)
                    CrashGuard.markHealthy(this@MainActivity)
                }

                // Apertar Home com o launcher aberto sempre volta para a home.
                LaunchedEffect(homeSignal) {
                    nav.home()
                    changelog = null
                }
                // Mede quanto a lista de apps leva para aparecer (até o primeiro quadro desenhado).
                LaunchedEffect(nav.isOpen(Screen.Drawer)) {
                    if (nav.isOpen(Screen.Drawer)) {
                        val t0 = SystemClock.elapsedRealtime()
                        withFrameNanos { }
                        Perf.stats.record(Perf.DRAWER, SystemClock.elapsedRealtime() - t0)
                    }
                }
                // Ao sair da lista completa, a busca é limpa e a marcação termina.
                LaunchedEffect(nav.isOpen(Screen.Drawer)) {
                    if (!nav.isOpen(Screen.Drawer)) {
                        startFolder = null
                        query = ""
                        focusSearch = false
                        markingFocusApps = false
                    }
                }
                // Um launcher não fecha com o botão voltar; aqui ele só sai das telas internas.
                BackHandler {
                    when {
                        pausePending != null -> pausePending = null
                        showDock -> { showDock = false; dockDismissed = true }
                        changelog != null -> changelog = null
                        nav.isOpen(Screen.Drawer) && markingFocusApps -> markingFocusApps = false
                        nav.current != Screen.Home -> nav.home()
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
                        action == QuickAction.Search -> { focusSearch = true; nav.open(Screen.Drawer) }
                        action == QuickAction.Drawer -> { focusSearch = false; nav.open(Screen.Drawer) }
                        action == QuickAction.Focus -> toggleFocus()
                        action == QuickAction.HighVis -> toggleHighVis()
                        action == QuickAction.Capture -> nav.open(Screen.Capture)
                        action == QuickAction.Usage -> nav.open(Screen.Usage)
                        action == QuickAction.Optimize -> nav.open(Screen.Optimize)
                        action == QuickAction.Health -> nav.open(Screen.Health)
                        action == QuickAction.Settings -> nav.open(Screen.Settings)
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
                    value = withContext(Dispatchers.IO) { guarded("agenda", emptyList()) { Agenda.read(applicationContext, nowMillis) } }
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
                        guarded("memórias", null) {
                            MemoryReader.pick(applicationContext, nowMillis, memoryMode, hiddenPhotos)?.let { m ->
                                MemoryReader.thumbnail(applicationContext, m)?.let { MemoryShown(m, it) }
                            }
                        }
                    }
                }

                val screen = when {
                    nav.isOpen(Screen.Tips) -> Screen.Tips.key
                    changelog != null -> "changelog"
                    else -> nav.current.key
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

                Box(Modifier.fillMaxSize()) {
                val wp = background.bitmap
                // A imagem de fundo cobre a tela inteira (inclusive atrás das barras do sistema).
                if (backdrop == Backdrop.Wallpaper && wp != null && !highVis) {
                    val maxPx = with(androidx.compose.ui.platform.LocalDensity.current) { Parallax.MAX_DP.dp.toPx() }
                    androidx.compose.foundation.Image(
                        bitmap = wp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (parallax) {
                                    scaleX = Parallax.OVERSCAN; scaleY = Parallax.OVERSCAN
                                    translationX = -background.tiltX * maxPx; translationY = background.tiltY * maxPx
                                }
                            },
                    )
                    Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color(VoidColors.Veil)))
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            // Só observa os toques (sem consumir) para saber há quanto tempo a home está parada.
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    lastTouchMs = System.currentTimeMillis()
                                    event.changes.firstOrNull()?.position?.let { lastTouchX = it.x; lastTouchY = it.y }
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
                        TipsScreen(onClose = { nav.close(Screen.Tips) })
                    } else if (current == "changelog") {
                        ChangelogScreen(title = changelogTitle, notes = changelog ?: lastNotes, onClose = { changelog = null })
                    } else if (current == "usage") {
                        UsageScreen(refreshKey = refreshKey, onClose = { nav.close(Screen.Usage) })
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
                            onClose = { nav.close(Screen.Capture) },
                        )
                    } else if (current == "order") {
                        FavoritesOrderScreen(
                            rows = pinnedKeys.mapNotNull { key ->
                                val fid = com.voidtouch.launcher.data.FavoriteKeys.folderId(key)
                                val label = if (fid != null) folders.firstOrNull { it.id == fid }?.name
                                else apps.firstOrNull { it.key == key }?.label
                                label?.let { OrderRow(key, it) }
                            },
                            onMove = { key, delta ->
                                pinnedKeys = HomeRules.move(pinnedKeys, key, delta)
                                favoritesStore.save(pinnedKeys)
                            },
                            onClose = { nav.close(Screen.Order) },
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
                            onClose = { nav.close(Screen.Later) },
                        )
                    } else if (current == "permissions") {
                        PermissionsScreen(
                            refreshKey = refreshKey,
                            notificationAccess = notifAccess,
                            onOpenNotificationAccess = ::openNotificationAccess,
                            onRequestPhotos = { runCatching { photoPermission.launch(MemoryReader.permissionsToRequest()) } },
                            contactsAccess = contactsAccess,
                            onRequestContacts = { runCatching { contactsPermission.launch(android.Manifest.permission.READ_CONTACTS) } },
                            onRequestCalendar = { runCatching { calendarPermission.launch(android.Manifest.permission.READ_CALENDAR) } },
                            onOpenAppInfo = ::openAppInfo,
                            onClose = { nav.close(Screen.Permissions) },
                        )
                    } else if (current == "optimize") {
                        OptimizeScreen(apps = apps, refreshKey = refreshKey, onMessage = ::say, onClose = { nav.close(Screen.Optimize) })
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
                            onOpenOptimize = { nav.close(Screen.Settings); nav.open(Screen.Optimize) },
                            onOpenPermissions = { nav.close(Screen.Settings); nav.open(Screen.Permissions) },
                            onOpenUsage = { nav.close(Screen.Settings); nav.open(Screen.Usage) },
                            onOpenHomeSettings = ::openHomeSettings,
                            onOpenFavoritesOrder = { nav.close(Screen.Settings); nav.open(Screen.Order) },
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
                            colorSet = colorSet,
                            onColorSet = {
                                colorSet = it
                                settingsStore.saveColorSet(it)
                                background.refresh()
                            },
                            design = design,
                            onDesign = ::changeDesign,
                            highVis = highVis,
                            onHighVis = ::toggleHighVis,
                            lightAuto = lightAuto,
                            lightAutoAvailable = lightSensor != null,
                            onLightAuto = {
                                lightAuto = !lightAuto
                                settingsStore.saveLightAuto(lightAuto)
                                if (lightAuto) startLight() else stopLight()
                                say(if (lightAuto) "alta visibilidade automática ligada" else "alta visibilidade automática desligada")
                            },
                            pauseOn = pauseOn,
                            onPauseToggle = {
                                pauseOn = !pauseOn
                                settingsStore.savePauseOn(pauseOn)
                                say(if (pauseOn) "pausa antes de abrir ligada" else "pausa antes de abrir desligada")
                            },
                            parallax = parallax,
                            parallaxAvailable = background.parallaxAvailable,
                            ownImage = background.ownImage,
                            onPickImage = { runCatching { ownImagePicker.launch(arrayOf("image/*")) }.onFailure { say("este aparelho não tem um seletor de arquivos") } },
                            onRemoveImage = {
                                background.removeOwn()
                                say("imagem de fundo removida")
                            },
                            onParallax = {
                                parallax = !parallax
                                settingsStore.saveParallax(parallax)
                                background.refresh()
                            },
                            openAnim = openAnim,
                            onOpenAnim = {
                                openAnim = !openAnim
                                settingsStore.saveOpenAnim(openAnim)
                            },
                            openHaptic = openHaptic,
                            onOpenHaptic = {
                                openHaptic = !openHaptic
                                settingsStore.saveOpenHaptic(openHaptic)
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
                            onOpenTips = { nav.close(Screen.Settings); nav.open(Screen.Tips) },
                            onBackupSave = { runCatching { backupSave.launch("voidtouch-backup.json") }.onFailure { say("este aparelho não tem um seletor de arquivos") } },
                            onBackupRestore = { runCatching { backupRestore.launch(arrayOf("application/json", "text/plain", "*/*")) }.onFailure { say("este aparelho não tem um seletor de arquivos") } },
                            openTimeMs = openTimeMs,
                            onOpenChangelog = {
                                changelogTitle = "novidades"
                                changelog = Changelog.all
                            },
                            onBackdrop = {
                                backdrop = it
                                settingsStore.saveBackdrop(it)
                                applyBackdrop()
                                background.refresh()
                            },
                            onClockStyle = {
                                clockStyle = it
                                settingsStore.saveClockStyle(it)
                            },
                            onSwipeApp = { side, key ->
                                gestures = gestures + (side to key)
                                settingsStore.saveSwipeApp(side, key)
                            },
                            onClose = { nav.close(Screen.Settings) },
                        )
                    } else if (current == "health") {
                        HealthScreen(
                            apps = apps,
                            nowMillis = nowMillis,
                            refreshKey = refreshKey,
                            onClose = { nav.close(Screen.Health) },
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
                                        QuickAction.HighVis -> toggleHighVis()
                                        QuickAction.Capture -> { nav.close(Screen.Drawer); nav.open(Screen.Capture) }
                                        QuickAction.Usage -> { nav.close(Screen.Drawer); nav.open(Screen.Usage) }
                                        QuickAction.Optimize -> { nav.close(Screen.Drawer); nav.open(Screen.Optimize) }
                                        QuickAction.Health -> { nav.close(Screen.Drawer); nav.open(Screen.Health) }
                                        QuickAction.Settings -> { nav.close(Screen.Drawer); nav.open(Screen.Settings) }
                                    }
                                }
                            },
                            onOpenSettings = { nav.open(Screen.Settings) },
                            onClose = { nav.close(Screen.Drawer) },
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
                            onOpenCapture = { nav.open(Screen.Capture) },
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
                            onOpenLater = { nav.open(Screen.Later) },
                            onEnableNotifications = ::openNotificationAccess,
                            active = screenActive,
                            agenda = agenda,
                            agendaEnabled = calendarAccess,
                            onEnableAgenda = { runCatching { calendarPermission.launch(android.Manifest.permission.READ_CALENDAR) } },
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
                                    say("foto oculta: não aparece mais aqui")
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
                                nav.open(Screen.Drawer)
                            },
                            onOpenDrawer = {
                                focusSearch = false
                                nav.open(Screen.Drawer)
                            },
                            onOpenSearch = {
                                focusSearch = true
                                nav.open(Screen.Drawer)
                            },
                            onOpenHealth = { nav.open(Screen.Health) },
                            onToggleFocus = ::toggleFocus,
                            onOpenSettings = { nav.open(Screen.Settings) },
                            onSwipeLeft = { runGesture(SwipeSide.Left) },
                            onSwipeRight = { runGesture(SwipeSide.Right) },
                            onSwipeDown = { runGesture(SwipeSide.Down) },
                            onDoubleTap = { runGesture(SwipeSide.DoubleTap) },
                        )
                    }
                  }
                    pausePending?.let { p ->
                        PauseScreen(
                            appLabel = p.label,
                            phrase = DailyOpens.phrase(DailyOpens.count(dailyOpens, java.time.LocalDate.now().toString(), p.key)),
                            onOpen = { pausePending = null; launchApp(p, skipPause = true) },
                            onBack = { pausePending = null },
                        )
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
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(packageReceiver) }
        mainHandler.removeCallbacks(reloadApps)
        background.onDestroy()
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
        runCatching { ContextCompat.registerReceiver(this, timeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED) }
        system.startTorchWatch()
        background.onStart()
        if (lightAuto) startLight()
    }

    override fun onStop() {
        super.onStop()
        screenActive = false
        runCatching { unregisterReceiver(timeReceiver) }
        background.onStop()
        system.stopTorchWatch()
        stopLight()
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

    private fun openNotificationAccess() = system.openNotificationAccess()

    private fun openAppInfo() = system.openAppInfo()

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

    private fun openPackageInfo(pkg: String) = system.openPackageInfo(pkg)

    private fun uninstall(app: AppEntry) = system.uninstall(app)

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
    private fun launchApp(app: AppEntry, skipPause: Boolean = false) {
        if (pauseOn && !skipPause && app.key in focusApps) {
            pausePending = app
            return
        }
        val today = java.time.LocalDate.now().toString()
        dailyOpens = DailyOpens.increment(dailyOpens, today, app.key)
        settingsStore.saveDailyOpens(DailyOpens.encode(dailyOpens))
        opens = LaunchCounts.increment(opens, app.key)
        if (predictOn) {
            val hour = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).hour
            predictHistory = Predict.record(predictHistory, hour, app.key)
            settingsStore.savePredictHistory(Predict.encode(predictHistory))
        }
        settingsStore.saveLaunchCounts(LaunchCounts.encode(opens))
        val decor = window.decorView
        if (openHaptic) decor.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)
        val origin = if (openAnim && com.voidtouch.launcher.data.Motion.enabled(this)) {
            OpenAnim.rect(
                lastTouchX, lastTouchY, (56 * resources.displayMetrics.density).toInt(),
                decor.width, decor.height, System.currentTimeMillis() - lastTouchMs,
            )
        } else {
            null
        }
        AppRepository.launch(this, app, origin)
    }

    /** Executa um resultado da busca que não é app. */
    private fun openHit(hit: Hit, runAction: (QuickAction) -> Unit) = system.openHit(hit, runAction)

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

    private fun toggleTorch() = system.toggleTorch()

    /** Abre onde se troca a tela inicial padrão. */
    private fun openHomeSettings() = system.openHomeSettings()

    private fun expandNotificationPanel() = system.expandNotificationPanel()

    private fun applyBackdrop() {
        val argb = when {
            highVis -> HighVis.BACKGROUND
            backdrop == Backdrop.Wallpaper -> VoidColors.Veil
            VoidColors.isRotina -> RotinaTokens.BACKGROUND
            else -> backdrop.argb
        }
        window.setBackgroundDrawable(ColorDrawable(argb))
        // Alta visibilidade: brilho no máximo só nesta janela (a home); os outros apps não mudam.
        val attrs = window.attributes
        attrs.screenBrightness = if (highVis) 1f else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = attrs
    }

    private fun changeDesign(next: Design) {
        design = next
        VoidColors.design = next
        settingsStore.saveDesign(next.key)
        applyBackdrop()
    }

    private fun toggleHighVis() {
        applyHighVis(!highVis, persist = true)
        lightState = com.voidtouch.launcher.data.LightAuto.State(highVis)
        say(if (highVis) "alta visibilidade ligada" else "alta visibilidade desligada")
    }

    /** Liga/desliga o visual; a troca feita pelo sensor não é gravada, para não prender o modo. */
    private fun applyHighVis(on: Boolean, persist: Boolean) {
        highVis = on
        VoidColors.highVis = on
        if (persist) settingsStore.saveHighVis(on)
        applyBackdrop()
    }

    private fun startLight() {
        val s = lightSensor ?: return
        lightState = com.voidtouch.launcher.data.LightAuto.State(highVis)
        runCatching { getSystemService(android.hardware.SensorManager::class.java).registerListener(lightListener, s, 1_000_000) }
    }

    private fun stopLight() {
        runCatching { getSystemService(android.hardware.SensorManager::class.java).unregisterListener(lightListener) }
    }

    private companion object {
        const val PREFS = "voidtouch"
        const val KEY_SEEN_VERSION = "seen_version"
        const val KEY_TIPS_SEEN = "tips_seen"
        const val KEY_OPEN_MS = "last_open_ms"
    }
}
