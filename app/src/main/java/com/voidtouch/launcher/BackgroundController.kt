package com.voidtouch.launcher

import android.content.Context
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.voidtouch.launcher.data.ColorSets
import com.voidtouch.launcher.data.OwnWallpaper
import com.voidtouch.launcher.data.Parallax
import com.voidtouch.launcher.data.Tone
import com.voidtouch.launcher.data.WallpaperLoader
import com.voidtouch.launcher.data.guarded
import com.voidtouch.launcher.ui.theme.Backdrop
import com.voidtouch.launcher.ui.theme.VoidColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Tudo do fundo da home num só lugar: imagem (própria ou do sistema), paralaxe pelo acelerômetro e o tom
 * das cores. A MainActivity só diz o estado atual (fundo, paralaxe, conjunto de cores, tela visível) e
 * chama [refresh] quando algo muda. O sensor só fica ligado com a tela visível e a paralaxe valendo.
 */
class BackgroundController(
    private val context: Context,
    private val backdrop: () -> Backdrop,
    private val parallax: () -> Boolean,
    private val colorSet: () -> String,
    private val screenActive: () -> Boolean,
    /** Aplica a cor de fundo da janela (a MainActivity cuida do Window). */
    private val applyWindow: () -> Unit,
) {
    var tiltX by mutableStateOf(0f)
        private set
    var tiltY by mutableStateOf(0f)
        private set
    var bitmap by mutableStateOf<Bitmap?>(null)
        private set
    var parallaxAvailable by mutableStateOf(true)
        private set
    var ownImage by mutableStateOf(OwnWallpaper.exists(context))
        private set

    private var toneCache: Pair<Int, Tone?>? = null
    private var generation = 0
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(e: SensorEvent) {
            // Em retrato: x positivo = inclinado para a esquerda; y positivo = topo para cima.
            tiltX = Parallax.smooth(tiltX, Parallax.target(e.values[0]))
            tiltY = Parallax.smooth(tiltY, Parallax.target(e.values[1] - 9.81f * 0.6f))
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /** A tela ficou visível. */
    fun onStart() {
        startSensor()
        refreshTone()
    }

    /** A tela saiu de vista: desliga o sensor e solta a imagem da memória. */
    fun onStop() {
        unregisterSensor()
        generation++ // cancela uma leitura ainda em andamento
        bitmap = null
    }

    fun onDestroy() {
        scope.cancel()
    }

    /** Fundo, paralaxe ou conjunto de cores mudou: refaz sensor, imagem e tom. */
    fun refresh() {
        onStop()
        if (screenActive()) startSensor()
        refreshTone()
    }

    /** Copia a imagem escolhida para o app e passa a usá-la. [onDone] roda na thread principal. */
    fun importOwn(uri: Uri, onDone: (Boolean) -> Unit) {
        val m = context.resources.displayMetrics
        scope.launch {
            val ok = guarded("imagem própria", false) { OwnWallpaper.import(context, uri, m.widthPixels, m.heightPixels) }
            withContext(Dispatchers.Main) {
                if (ok) {
                    ownImage = true
                    toneCache = null
                }
                onDone(ok)
            }
        }
    }

    fun removeOwn() {
        OwnWallpaper.remove(context)
        ownImage = false
        toneCache = null
        bitmap = null
        refresh()
    }

    private fun startSensor() {
        if (backdrop() != Backdrop.Wallpaper || !parallax()) return
        runCatching {
            val sm = context.getSystemService(SensorManager::class.java)
            val sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun unregisterSensor() {
        runCatching { context.getSystemService(SensorManager::class.java).unregisterListener(listener) }
        tiltX = 0f
        tiltY = 0f
    }

    private fun applyPalette(tone: Tone?) {
        VoidColors.apply(ColorSets.palette(ColorSets.fromKey(colorSet()), tone, backdrop() == Backdrop.Wallpaper))
        applyWindow()
    }

    /**
     * Imagem para o fundo/paralaxe e tom das cores. A leitura roda em segundo plano (já reduzida ao tamanho
     * da tela) e o tom fica em cache até a imagem mudar.
     */
    private fun refreshTone() {
        val onWallpaper = backdrop() == Backdrop.Wallpaper
        val own = OwnWallpaper.exists(context)
        val needBitmap = onWallpaper && screenActive() && (own || parallax())
        val needTone = onWallpaper && ColorSets.fromKey(colorSet()).auto
        val id = if (own) -OwnWallpaper.stamp(context) - 2 else WallpaperLoader.id(context)
        val cached = toneCache?.takeIf { it.first == id }
        applyPalette(cached?.second)
        if (!needBitmap && (!needTone || cached != null)) return
        val gen = ++generation
        val m = context.resources.displayMetrics
        scope.launch {
            // Imagem própria: lida do arquivo do app. Papel de parede do sistema: o tom vem das cores do Android
            // (sem permissão) e a imagem só é lida se a paralaxe precisar dela.
            val loaded = when {
                !needBitmap -> null
                own -> guarded("imagem própria", null) { OwnWallpaper.load(context, m.widthPixels, m.heightPixels) }
                else -> guarded("papel de parede", null) { WallpaperLoader.load(context, m.widthPixels, m.heightPixels) }
            }
            val tone = if (own) loaded?.tone ?: cached?.second
            else guarded("cores do papel de parede", null) { WallpaperLoader.colorsTone(context) } ?: loaded?.tone
            withContext(Dispatchers.Main) {
                if (gen != generation) return@withContext
                toneCache = id to tone
                if (needBitmap) {
                    bitmap = loaded?.bitmap
                    parallaxAvailable = loaded != null
                    if (loaded == null) unregisterSensor()
                }
                applyPalette(tone)
            }
        }
    }
}
