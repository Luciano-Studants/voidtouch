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
import com.voidtouch.launcher.ui.AppList
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

    private var backdrop: Backdrop = Backdrop.Void

    private lateinit var favoritesStore: FavoritesStore

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

                // Apertar Home com o launcher aberto sempre volta para a home.
                LaunchedEffect(homeSignal) { showAllApps = false }
                // Ao sair da lista completa, a busca é limpa.
                LaunchedEffect(showAllApps) {
                    if (!showAllApps) {
                        query = ""
                        focusSearch = false
                    }
                }
                // Um launcher não fecha com o botão voltar; aqui ele só sai da lista completa.
                BackHandler { if (showAllApps) showAllApps = false }

                val favorites = remember(apps, pinnedKeys) {
                    val byKey = apps.associateBy { it.key }
                    pinnedKeys.mapNotNull { byKey[it] }
                }

                Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    if (showAllApps) {
                        AppList(
                            apps = apps,
                            pinnedKeys = pinnedKeys,
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
