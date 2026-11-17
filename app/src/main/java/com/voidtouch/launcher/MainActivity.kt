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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.AppRepository
import com.voidtouch.launcher.ui.ClockHeader
import com.voidtouch.launcher.ui.theme.Backdrop
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidTouchTheme
import com.voidtouch.launcher.ui.theme.VoidType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    /** Muda a cada volta à tela, para recarregar a lista (app instalado ou removido). */
    private var refreshKey by mutableIntStateOf(0)

    /** Horário atual, atualizado pelo sistema a cada minuto enquanto a tela está visível. */
    private var nowMillis by mutableLongStateOf(System.currentTimeMillis())

    private var backdrop: Backdrop = Backdrop.Void

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            nowMillis = System.currentTimeMillis()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bars = SystemBarStyle.dark(Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)

        backdrop = Backdrop.fromKey(prefs().getString(KEY_BACKDROP, null))
        applyBackdrop()

        setContent {
            VoidTouchTheme {
                // Um launcher não deve fechar com o botão voltar.
                BackHandler {}
                HomeScreen(
                    refreshKey = refreshKey,
                    nowMillis = nowMillis,
                    onClockLongPress = ::cycleBackdrop,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        nowMillis = System.currentTimeMillis()
        // O relógio só "acorda" enquanto a tela está visível; em segundo plano não gasta nada.
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

    private fun cycleBackdrop() {
        backdrop = backdrop.next()
        prefs().edit().putString(KEY_BACKDROP, backdrop.key).apply()
        applyBackdrop()
    }

    private fun applyBackdrop() {
        window.setBackgroundDrawable(ColorDrawable(backdrop.argb))
    }

    private fun prefs() = getSharedPreferences(PREFS, MODE_PRIVATE)

    private companion object {
        const val PREFS = "voidtouch"
        const val KEY_BACKDROP = "backdrop"
    }
}

/** Relógio no topo e, logo abaixo, a lista de apps em texto. Tocar num app abre ele. */
@Composable
private fun HomeScreen(
    refreshKey: Int,
    nowMillis: Long,
    onClockLongPress: () -> Unit,
) {
    val context = LocalContext.current
    val apps by produceState(emptyList<AppEntry>(), refreshKey) {
        value = withContext(Dispatchers.Default) { AppRepository.loadApps(context) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "header", contentType = "header") {
                ClockHeader(nowMillis = nowMillis, onLongPress = onClockLongPress)
            }
            items(
                items = apps,
                key = { it.packageName + "/" + it.activityName },
                contentType = { "app" },
            ) { app ->
                Text(
                    text = app.label,
                    style = VoidType.App,
                    color = VoidColors.Text,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { AppRepository.launch(context, app) }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                )
            }
        }
    }
}
