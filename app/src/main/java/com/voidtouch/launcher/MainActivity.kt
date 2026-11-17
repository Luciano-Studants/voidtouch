package com.voidtouch.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
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

    private var backdrop: Backdrop = Backdrop.Void

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
        backdrop = Backdrop.fromKey(prefs.getString(KEY_BACKDROP, null))
        applyBackdrop()

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
                // Verdadeiro enquanto se marca os apps do foco na lista completa.
                var markingFocusApps by remember { mutableStateOf(false) }

                // Apertar Home com o launcher aberto sempre volta para a home.
                LaunchedEffect(homeSignal) {
                    showAllApps = false
                    showHealth = false
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

                Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    if (showHealth) {
                        HealthScreen(
                            apps = apps,
                            nowMillis = nowMillis,
                            onClose = { showHealth = false },
                        )
                    } else if (showAllApps) {
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
                            onClose = { showAllApps = false },
                        )
                    } else {
                        HomeContent(
                            nowMillis = nowMillis,
                            stats = stats,
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
                            onClockLongPress = ::cycleBackdrop,
                        )
                    }
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
        unregisterReceiver(timeReceiver)
    }

    override fun onResume() {
        super.onResume()
        refreshKey++
    }

    private fun refreshClock() {
        nowMillis = System.currentTimeMillis()
        stats = DeviceStatsReader.read(applicationContext)
    }

    private fun togglePin(app: AppEntry) {
        val current = pinnedKeys
        pinnedKeys = when {
            app.key in current -> current - app.key
            current.size >= FavoritesStore.MAX -> {
                Toast.makeText(this, "Máximo de ${FavoritesStore.MAX} favoritos", Toast.LENGTH_SHORT).show()
                current
            }
            else -> current + app.key
        }
        favoritesStore.save(pinnedKeys)
    }

    /** Liga ou desliga o foco. Sem nenhum app marcado não há o que esconder, então avisa em vez de ligar. */
    private fun toggleFocus() {
        if (focusSince != 0L) {
            focusSince = 0L
        } else if (focusApps.isEmpty()) {
            Toast.makeText(
                this,
                "Marque os apps do foco: lista completa > apps do foco",
                Toast.LENGTH_LONG,
            ).show()
            return
        } else {
            focusSince = System.currentTimeMillis()
        }
        focusStore.saveSince(focusSince)
    }

    private fun toggleFocusApp(app: AppEntry) {
        focusApps = if (app.key in focusApps) focusApps - app.key else focusApps + app.key
        focusStore.saveApps(focusApps)
    }

    private fun cycleBackdrop() {
        backdrop = backdrop.next()
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_BACKDROP, backdrop.key).apply()
        applyBackdrop()
    }

    private fun applyBackdrop() {
        window.setBackgroundDrawable(ColorDrawable(backdrop.argb))
    }

    private companion object {
        const val PREFS = "voidtouch"
        const val KEY_BACKDROP = "backdrop"
    }
}
