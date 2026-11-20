package com.voidtouch.launcher.notify

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat

/** Uma notificação resumida para a home. */
data class NoteItem(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val intent: PendingIntent?,
    /** Resposta direta, quando a notificação aceita (mensagens). */
    val replyIntent: PendingIntent? = null,
    val replyInput: android.app.RemoteInput? = null,
    val postedAt: Long = 0L,
)

/** Uma atividade ao vivo (chamada, cronômetro, download...) para a ilha da home. */
data class LiveItem(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val kind: com.voidtouch.launcher.data.LiveKind,
    val percent: Int?,
    val chrono: Boolean,
    val whenMs: Long,
    val countDown: Boolean,
    val intent: PendingIntent?,
)

/** A música tocando agora. */
data class MediaInfo(
    val title: String,
    val artist: String,
    val playing: Boolean,
    val durationMs: Long,
    val art: android.graphics.Bitmap?,
    val controller: MediaController,
)

/**
 * Ponto único com o que o serviço de notificações enxerga. A home só lê estes estados;
 * o serviço só trabalha quando o usuário liberou o acesso, e o sistema o encerra se ele for revogado.
 */
object NotificationHub {
    var items by mutableStateOf<List<NoteItem>>(emptyList())
    var media by mutableStateOf<MediaInfo?>(null)
    var live by mutableStateOf<List<LiveItem>>(emptyList())
    internal var service: NotificationListenerService? = null

    fun hasAccess(context: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    /** Envia a resposta pela própria notificação do app. Devolve falso se não der. */
    fun reply(context: Context, item: NoteItem, text: String): Boolean {
        val input = item.replyInput ?: return false
        val pending = item.replyIntent ?: return false
        return runCatching {
            val intent = android.content.Intent()
            val bundle = android.os.Bundle().apply { putCharSequence(input.resultKey, text) }
            android.app.RemoteInput.addResultsToIntent(arrayOf(input), intent, bundle)
            pending.send(context, 0, intent)
            true
        }.getOrDefault(false)
    }

    fun dismiss(key: String) {
        runCatching { service?.cancelNotification(key) }
    }

    fun togglePlay() {
        val m = media ?: return
        runCatching {
            if (m.playing) m.controller.transportControls.pause() else m.controller.transportControls.play()
        }
    }

    fun next() {
        runCatching { media?.controller?.transportControls?.skipToNext() }
    }

    fun seekTo(positionMs: Long) {
        runCatching { media?.controller?.transportControls?.seekTo(positionMs) }
    }

    fun previous() {
        runCatching { media?.controller?.transportControls?.skipToPrevious() }
    }
}

/** Lê as notificações ativas e as sessões de mídia e entrega ao [NotificationHub]. */
class VoidNotificationService : NotificationListenerService() {

    private val labels = HashMap<String, String>()
    private val callbacks = HashMap<MediaController, MediaController.Callback>()

    private val sessionListener =
        MediaSessionManager.OnActiveSessionsChangedListener { list -> updateSessions(list.orEmpty()) }

    override fun onListenerConnected() {
        NotificationHub.service = this
        refresh()
        try {
            val manager = getSystemService(MediaSessionManager::class.java)
            val component = ComponentName(this, VoidNotificationService::class.java)
            manager.addOnActiveSessionsChangedListener(sessionListener, component)
            updateSessions(manager.getActiveSessions(component))
        } catch (_: SecurityException) {
        }
    }

    override fun onListenerDisconnected() {
        release()
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) = refresh()

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = refresh()

    private fun release() {
        runCatching { getSystemService(MediaSessionManager::class.java).removeOnActiveSessionsChangedListener(sessionListener) }
        callbacks.forEach { (c, cb) -> runCatching { c.unregisterCallback(cb) } }
        callbacks.clear()
        NotificationHub.service = null
        NotificationHub.items = emptyList()
        NotificationHub.live = emptyList()
        NotificationHub.media = null
    }

    private fun refresh() {
        val active = try {
            activeNotifications
        } catch (_: Exception) {
            null
        } ?: return
        NotificationHub.items = active
            .filter { !it.isOngoing && it.packageName != packageName && (it.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY) == 0 }
            .sortedByDescending { it.postTime }
            .mapNotNull { sbn ->
                val extras = sbn.notification.extras
                val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
                    ?: extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
                    ?: return@mapNotNull null
                val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString().orEmpty()
                val replyAction = sbn.notification.actions?.firstOrNull { a -> a.remoteInputs?.any { it.allowFreeFormInput } == true }
                val replyInput = replyAction?.remoteInputs?.firstOrNull { it.allowFreeFormInput }
                NoteItem(
                    sbn.key, sbn.packageName, labelOf(sbn.packageName), title, text, sbn.notification.contentIntent,
                    replyIntent = replyAction?.actionIntent.takeIf { replyInput != null },
                    replyInput = replyInput,
                    postedAt = sbn.postTime,
                )
            }
            .take(20)
        NotificationHub.live = liveFrom(active)
    }

    /** Notificações em andamento que merecem a ilha: chamada, navegação, cronômetro e progresso. */
    private fun liveFrom(active: Array<StatusBarNotification>): List<LiveItem> {
        val found = active.mapNotNull { sbn ->
            if (sbn.packageName == packageName) return@mapNotNull null
            val n = sbn.notification
            if ((n.flags and android.app.Notification.FLAG_GROUP_SUMMARY) != 0) return@mapNotNull null
            val extras = n.extras
            val max = extras.getInt(android.app.Notification.EXTRA_PROGRESS_MAX, 0)
            val progress = extras.getInt(android.app.Notification.EXTRA_PROGRESS, 0)
            val chrono = extras.getBoolean(android.app.Notification.EXTRA_SHOW_CHRONOMETER, false)
            val kind = com.voidtouch.launcher.data.Live.classify(
                category = n.category,
                ongoing = sbn.isOngoing,
                hasProgress = max > 0,
                showChrono = chrono,
            ) ?: return@mapNotNull null
            val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
                ?: extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()
                ?: labelOf(sbn.packageName)
            LiveItem(
                key = sbn.key,
                packageName = sbn.packageName,
                appLabel = labelOf(sbn.packageName),
                title = title,
                kind = kind,
                percent = com.voidtouch.launcher.data.Live.percent(progress, max),
                chrono = chrono,
                whenMs = n.`when`,
                countDown = extras.getBoolean("android.chronometerCountDown", false),
                intent = n.contentIntent,
            )
        }
        return com.voidtouch.launcher.data.Live.top(found) { it.kind }
    }

    private fun labelOf(pkg: String): String = labels.getOrPut(pkg) {
        try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (_: Exception) {
            pkg
        }
    }

    private fun updateSessions(list: List<MediaController>) {
        val gone = callbacks.keys.filter { c -> list.none { it.sessionToken == c.sessionToken } }
        gone.forEach { c -> callbacks.remove(c)?.let { runCatching { c.unregisterCallback(it) } } }
        list.forEach { c ->
            if (callbacks.keys.none { it.sessionToken == c.sessionToken }) {
                val cb = object : MediaController.Callback() {
                    override fun onMetadataChanged(metadata: MediaMetadata?) = pickMedia()
                    override fun onPlaybackStateChanged(state: PlaybackState?) = pickMedia()
                    override fun onSessionDestroyed() = pickMedia()
                }
                c.registerCallback(cb)
                callbacks[c] = cb
            }
        }
        pickMedia()
    }

    private fun pickMedia() {
        val all = callbacks.keys.filter { it.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) != null }
        val chosen = all.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: all.firstOrNull()
        NotificationHub.media = chosen?.let {
            val meta = it.metadata
            MediaInfo(
                title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty(),
                artist = (meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                    ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)).orEmpty(),
                playing = it.playbackState?.state == PlaybackState.STATE_PLAYING,
                durationMs = meta?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
                art = meta?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                    ?: meta?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    ?: meta?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON),
                controller = it,
            )
        }
    }
}
