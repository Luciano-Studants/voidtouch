package com.voidtouch.launcher

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.AppRepository
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidTouchTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    /** Muda a cada volta à tela, para recarregar a lista (app instalado ou removido). */
    private var refreshKey by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bars = SystemBarStyle.dark(Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)

        setContent {
            VoidTouchTheme {
                // Um launcher não deve fechar com o botão voltar.
                BackHandler {}
                SkeletonHome(refreshKey)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshKey++
    }
}

/** Etapa 2: esqueleto. Lista de apps em texto puro; tocar abre o app. */
@Composable
private fun SkeletonHome(refreshKey: Int) {
    val context = LocalContext.current
    val apps by produceState(emptyList<AppEntry>(), refreshKey) {
        value = withContext(Dispatchers.Default) { AppRepository.loadApps(context) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidColors.Void)
            .safeDrawingPadding(),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(apps, key = { it.packageName + "/" + it.activityName }) { app ->
                Text(
                    text = app.label,
                    color = VoidColors.Text,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
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
