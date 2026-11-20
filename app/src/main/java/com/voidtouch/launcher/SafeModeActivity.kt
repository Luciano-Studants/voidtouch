package com.voidtouch.launcher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.CrashRules
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType

/**
 * Tela mínima que aparece quando o VoidTouch travou várias vezes seguidas. Não usa nada do app normal
 * (nem ajustes, nem sensores, nem papel de parede): só uma lista de apps e os caminhos para sair do problema.
 */
class SafeModeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val apps = runCatching { loadApps() }.getOrDefault(emptyList())
        setContent {
            var message by remember { mutableStateOf<String?>(null) }
            SafeModeScreen(
                error = CrashGuard.lastError(this),
                apps = apps,
                message = message,
                onOpenApp = { (_, pkg) ->
                    packageManager.getLaunchIntentForPackage(pkg)?.let { runCatching { startActivity(it) } }
                },
                onChangeHome = { openHomeSettings() },
                onCopy = {
                    val text = "VoidTouch ${versionLabel()}\n${CrashGuard.lastError(this).orEmpty()}"
                    getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("erro", text))
                    message = "erro copiado"
                },
                onRetry = {
                    // Desliga os recursos mais arriscados antes de tentar de novo, para não repetir o mesmo caminho.
                    val prefs = getSharedPreferences("voidtouch", Context.MODE_PRIVATE).edit()
                    CrashRules.RISKY_BOOLEANS.forEach { (k, v) -> prefs.putBoolean(k, v) }
                    prefs.putString("color_set", CrashRules.SAFE_COLOR_SET).putString("backdrop", CrashRules.SAFE_BACKDROP).commit()
                    CrashGuard.leaveSafeMode(this)
                    startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    finish()
                },
            )
        }
    }

    private fun versionLabel(): String = runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
    }.getOrDefault("")

    private fun loadApps(): List<Pair<String, String>> {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(query, 0)
            .map { it.loadLabel(packageManager).toString() to it.activityInfo.packageName }
            .filter { it.second != packageName }
            .sortedBy { it.first.lowercase() }
    }

    /** Abre onde se troca a tela inicial padrão; tenta caminhos mais genéricos se o aparelho não tiver o primeiro. */
    private fun openHomeSettings() {
        val options = listOf(
            Intent(Settings.ACTION_HOME_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS),
        )
        for (i in options) if (runCatching { startActivity(i) }.isSuccess) return
    }
}

@Composable
private fun SafeModeScreen(
    error: String?,
    apps: List<Pair<String, String>>,
    message: String?,
    onOpenApp: (Pair<String, String>) -> Unit,
    onChangeHome: () -> Unit,
    onCopy: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidColors.Void)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Text("modo seguro", style = VoidType.App, color = VoidColors.Text, modifier = Modifier.padding(top = 28.dp))
        Text(
            "o VoidTouch fechou várias vezes seguidas, então a home normal foi pausada. Seus apps continuam abaixo e nada foi apagado.",
            style = VoidType.Small,
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
        )
        Action("tentar a home de novo (desliga recursos novos)", onRetry)
        Action("trocar a tela inicial padrão do Android", onChangeHome)
        Action("copiar o erro", onCopy)
        if (message != null) Text(message, style = VoidType.Caption, color = VoidColors.TextDim)
        if (!error.isNullOrBlank()) {
            Text(error, style = VoidType.Caption, color = VoidColors.TextDim, modifier = Modifier.padding(vertical = 12.dp))
        }
        Text("apps", style = VoidType.Caption, color = VoidColors.TextDim, modifier = Modifier.padding(top = 12.dp))
        apps.forEach { app ->
            Text(
                app.first,
                style = VoidType.App,
                color = VoidColors.Text,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().clickable { onOpenApp(app) }.padding(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun Action(label: String, onClick: () -> Unit) {
    Text(
        label,
        style = VoidType.App,
        color = VoidColors.Text,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
    )
}
