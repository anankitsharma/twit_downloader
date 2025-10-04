package com.rit.twitdownloader.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.content.FileProvider
import com.rit.twitdownloader.App.Companion.context
import com.rit.twitdownloader.MainActivity
import com.rit.twitdownloader.NotificationActionReceiver
import com.rit.twitdownloader.NotificationActionReceiver.Companion.ACTION_CANCEL_TASK
import com.rit.twitdownloader.NotificationActionReceiver.Companion.ACTION_ERROR_REPORT
import com.rit.twitdownloader.NotificationActionReceiver.Companion.ACTION_KEY
import com.rit.twitdownloader.NotificationActionReceiver.Companion.ERROR_REPORT_KEY
import com.rit.twitdownloader.NotificationActionReceiver.Companion.NOTIFICATION_ID_KEY
import com.rit.twitdownloader.NotificationActionReceiver.Companion.TASK_ID_KEY
import com.rit.twitdownloader.R
import com.rit.twitdownloader.util.NOTIFICATION
import com.rit.twitdownloader.util.PreferenceUtil.getBoolean


@SuppressLint("StaticFieldLeak")
object NotificationUtil {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private const val PROGRESS_MAX = 100
    private const val PROGRESS_INITIAL = 0
    private const val CHANNEL_ID = "xdown_download_progress"
    private const val CHANNEL_COMPLETE_ID = "xdown_download_complete"
    private const val CHANNEL_ERROR_ID = "xdown_download_error"
    private const val SERVICE_CHANNEL_ID = "xdown_download_service"
    private const val NOTIFICATION_GROUP_ID = "xdown.download.notification"
    private const val DEFAULT_NOTIFICATION_ID = 100
    const val SERVICE_NOTIFICATION_ID = 123
    private lateinit var serviceNotification: Notification


    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val channelGroup =
            NotificationChannelGroup(NOTIFICATION_GROUP_ID, "XDown Downloads")
        
        // Progress channel - Low importance, no sound
        val progressChannel = NotificationChannel(
            CHANNEL_ID, 
            "Download Progress", 
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows download progress and status"
            group = NOTIFICATION_GROUP_ID
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
        }
        
        // Completion channel - Default importance, with sound
        val completeChannel = NotificationChannel(
            CHANNEL_COMPLETE_ID, 
            "Download Complete", 
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifies when downloads are completed"
            group = NOTIFICATION_GROUP_ID
            setShowBadge(true)
            enableLights(true)
            enableVibration(true)
        }
        
        // Error channel - High importance, with sound
        val errorChannel = NotificationChannel(
            CHANNEL_ERROR_ID, 
            "Download Error", 
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifies when downloads fail"
            group = NOTIFICATION_GROUP_ID
            setShowBadge(true)
            enableLights(true)
            enableVibration(true)
        }
        
        // Service channel - Low importance, no sound
        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID, 
            "Download Service", 
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background download service"
            group = NOTIFICATION_GROUP_ID
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
        }
        
        notificationManager.createNotificationChannelGroup(channelGroup)
        notificationManager.createNotificationChannel(progressChannel)
        notificationManager.createNotificationChannel(completeChannel)
        notificationManager.createNotificationChannel(errorChannel)
        notificationManager.createNotificationChannel(serviceChannel)
    }

    // DEPRECATED: Use updateServiceNotificationForFetching instead
    @Deprecated("Use updateServiceNotificationForFetching instead")
    fun notifyFetching(
        title: String,
        @Suppress("UNUSED_PARAMETER") notificationId: Int = DEFAULT_NOTIFICATION_ID,
        taskId: String? = null,
    ) {
        updateServiceNotificationForFetching(title, taskId)
    }

    // DEPRECATED: Use updateServiceNotificationForProgress instead
    @Deprecated("Use updateServiceNotificationForProgress instead")
    fun notifyProgress(
        title: String,
        @Suppress("UNUSED_PARAMETER") notificationId: Int = DEFAULT_NOTIFICATION_ID,
        progress: Int = PROGRESS_INITIAL,
        taskId: String? = null,
        @Suppress("UNUSED_PARAMETER") text: String? = null,
        speed: String? = null,
        fileSize: String? = null,
    ) {
        updateServiceNotificationForProgress(title, progress, speed, fileSize, taskId)
    }
    

    // DEPRECATED: Use updateServiceNotificationForComplete instead
    @Deprecated("Use updateServiceNotificationForComplete instead")
    fun finishNotification(
        notificationId: Int = DEFAULT_NOTIFICATION_ID,
        title: String? = null,
        text: String? = null,
        intent: PendingIntent? = null,
        fileSize: String? = null,
        downloadTime: String? = null,
    ) {
        updateServiceNotificationForComplete(title ?: "Download Complete")
    }
    
    private fun createShareIntent(fileName: String): PendingIntent {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this video I downloaded with XDown: $fileName")
            putExtra(Intent.EXTRA_SUBJECT, "XDown Download")
        }
        return PendingIntent.getActivity(
            context,
            0,
            Intent.createChooser(shareIntent, "Share with XDown"),
            PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createOpenIntent(): PendingIntent {
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            type = "resource/folder"
        }
        return PendingIntent.getActivity(
            context,
            1,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun finishNotificationForCustomCommands(
        notificationId: Int = DEFAULT_NOTIFICATION_ID,
        title: String? = null,
        text: String? = null,
    ) {
        //        notificationManager.cancel(notificationId)
        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentText(text)
                .setProgress(0, 0, false)
                .setAutoCancel(true)
                .setOngoing(false)
                .setStyle(null)
        title?.let { builder.setContentTitle(title) }

        notificationManager.notify(notificationId, builder.build())
    }

    fun makeServiceNotification(
        intent: PendingIntent, 
        text: String? = null,
        title: String? = null,
        progress: Int = -1,
        isIndeterminate: Boolean = false,
        speed: String? = null,
        fileSize: String? = null,
        taskId: String? = null
    ): Notification {
        // This is now a minimal service notification - user notifications are handled separately
        val notificationBuilder = NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xdown_notification)
            .setContentTitle("XDown Download Service")
            .setContentText("Downloading in background...")
            .setOngoing(true)
            .setContentIntent(intent)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .setColor(0xFF1DA1F2.toInt()) // Twitter Blue
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        serviceNotification = notificationBuilder.build()
        return serviceNotification
    }

    fun updateServiceNotificationForPlaylist(index: Int, itemCount: Int) {
        serviceNotification =
            NotificationCompat.Builder(context, serviceNotification)
                .setContentTitle(context.getString(R.string.service_title) + " ($index/$itemCount)")
                .build()
        notificationManager.notify(SERVICE_NOTIFICATION_ID, serviceNotification)
    }

    fun updateServiceNotificationForFetching(title: String, taskId: String? = null) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        
        // Create cancel action
        val cancelIntent = Intent(context.applicationContext, NotificationActionReceiver::class.java)
            .putExtra(TASK_ID_KEY, taskId ?: "")
            .putExtra(ACTION_KEY, ACTION_CANCEL_TASK.toString())
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            (taskId ?: "").hashCode(),
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xdown_notification)
            .setContentTitle("XDown • ${getCurrentTime()}")
            .setContentText(title)
            .setOngoing(true)
            .setContentIntent(intent)
            .setColor(0xFF1DA1F2.toInt()) // Twitter Blue
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(0, 0, true) // Indeterminate progress
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Fetching video info..."))
            .addAction(
                R.drawable.outline_cancel_24,
                "Cancel",
                cancelPendingIntent
            )
        
        notificationManager.notify(SERVICE_NOTIFICATION_ID, builder.build())
    }

    fun updateServiceNotificationForProgress(
        title: String, 
        progress: Int, 
        speed: String? = null, 
        fileSize: String? = null,
        taskId: String? = null
    ) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        
        // Create pause action
        val pauseIntent = Intent(context.applicationContext, NotificationActionReceiver::class.java)
            .putExtra(TASK_ID_KEY, taskId ?: "")
            .putExtra(ACTION_KEY, "PAUSE")
        val pausePendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            (taskId ?: "").hashCode() + 1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create cancel action
        val cancelIntent = Intent(context.applicationContext, NotificationActionReceiver::class.java)
            .putExtra(TASK_ID_KEY, taskId ?: "")
            .putExtra(ACTION_KEY, ACTION_CANCEL_TASK.toString())
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            (taskId ?: "").hashCode() + 2,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build progress text with speed and file size
        val progressText = buildString {
            if (speed != null && fileSize != null) {
                append("$speed • $fileSize")
            } else if (speed != null) {
                append(speed)
            } else if (fileSize != null) {
                append(fileSize)
            }
        }
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xdown_notification)
            .setContentTitle("XDown • ${getCurrentTime()}")
            .setContentText(title)
            .setOngoing(true)
            .setContentIntent(intent)
            .setColor(0xFF1DA1F2.toInt()) // Twitter Blue
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(100, progress, false)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(progressText))
            .addAction(
                R.drawable.ic_pause,
                "Pause",
                pausePendingIntent
            )
            .addAction(
                R.drawable.outline_cancel_24,
                "Cancel",
                cancelPendingIntent
            )
        
        notificationManager.notify(SERVICE_NOTIFICATION_ID, builder.build())
    }

    fun updateServiceNotificationForComplete(title: String, filePath: String? = null) {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        
        // Create open action
        val openIntent = Intent(context.applicationContext, NotificationActionReceiver::class.java)
            .putExtra(ACTION_KEY, "OPEN_FILE")
            .putExtra("FILE_PATH", filePath ?: "")
        val openPendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create share action - try to share the actual file if possible
        val shareIntent = try {
            Intent(Intent.ACTION_SEND).apply {
                // Try to find the actual file first
                val downloadsFolder = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val xdownFolder = java.io.File(downloadsFolder, "Seal")
                val videoFile = xdownFolder.listFiles()?.find { 
                    it.name.contains(title.substringBeforeLast("."), ignoreCase = true) && 
                    (it.extension.equals("mp4", ignoreCase = true) || it.extension.equals("mkv", ignoreCase = true))
                }
                
                if (videoFile != null && videoFile.exists()) {
                    // Share the actual video file using FileProvider
                    type = "video/*"
                    val fileUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        videoFile
                    )
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_TEXT, "Check out this video I downloaded with XDown: $title")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    // Fallback to text sharing
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Check out this video I downloaded with XDown: $title")
                }
                putExtra(Intent.EXTRA_SUBJECT, "XDown Download - $title")
            }
        } catch (e: Exception) {
            // Fallback to simple text sharing if file operations fail
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check out this video I downloaded with XDown: $title")
                putExtra(Intent.EXTRA_SUBJECT, "XDown Download - $title")
            }
        }
        val sharePendingIntent = PendingIntent.getActivity(
            context,
            1,
            Intent.createChooser(shareIntent, "Share with XDown"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_COMPLETE_ID)
            .setSmallIcon(R.drawable.ic_xdown_notification)
            .setContentTitle("XDown • Now")
            .setContentText(title)
            .setOngoing(false)
            .setContentIntent(intent)
            .setColor(0xFF1DA1F2.toInt()) // Twitter Blue
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Download completed successfully"))
            .addAction(
                R.drawable.ic_play_arrow,
                "Play",
                openPendingIntent
            )
            .addAction(
                R.drawable.ic_share,
                "Share",
                sharePendingIntent
            )
            .setAutoCancel(true)
        
        notificationManager.notify(SERVICE_NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun notifyError(
        title: String,
        textId: Int = R.string.download_error_msg,
        notificationId: Int,
        report: String,
        taskId: String? = null,
    ) {
        if (!NOTIFICATION.getBoolean()) return

        // Create copy error action
        val copyErrorIntent = Intent()
            .setClass(context, NotificationActionReceiver::class.java)
            .putExtra(NOTIFICATION_ID_KEY, notificationId)
            .putExtra(ERROR_REPORT_KEY, report)
            .putExtra(ACTION_KEY, ACTION_ERROR_REPORT)

        val copyErrorPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            copyErrorIntent,
            PendingIntent.FLAG_ONE_SHOT or
                PendingIntent.FLAG_IMMUTABLE or
                PendingIntent.FLAG_UPDATE_CURRENT,
        )
        
        // Create retry action if taskId is provided
        val retryIntent = taskId?.let {
            Intent(context.applicationContext, NotificationActionReceiver::class.java)
                .putExtra(TASK_ID_KEY, taskId)
                .putExtra(NOTIFICATION_ID_KEY, notificationId)
                .putExtra(ACTION_KEY, ACTION_CANCEL_TASK) // We'll use this for retry for now
                .run {
                    PendingIntent.getBroadcast(
                        context.applicationContext,
                        notificationId + 1000, // Different request code
                        this,
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
                    )
                }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ERROR_ID)
            .setSmallIcon(R.drawable.ic_download_error)
            .setContentTitle("${context.getString(R.string.notification_xdown)} • $title")
            .setContentText(context.getString(textId))
            .setOngoing(false)
            .setAutoCancel(true)
            .setColor(0xFFF44336.toInt()) // Red for error
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(textId)))
            .addAction(
                R.drawable.outline_content_copy_24,
                context.getString(R.string.notification_copy_error),
                copyErrorPendingIntent,
            )
        
        retryIntent?.let {
            builder.addAction(R.drawable.ic_refresh, context.getString(R.string.notification_retry), it)
        }
        
        notificationManager.cancel(notificationId)
        notificationManager.notify(notificationId, builder.build())
    }

    fun makeNotificationForCustomCommand(
        notificationId: Int,
        taskId: String,
        progress: Int,
        text: String? = null,
        templateName: String,
        taskUrl: String,
    ) {
        if (!NOTIFICATION.getBoolean()) return

        val intent =
            Intent(context.applicationContext, NotificationActionReceiver::class.java)
                .putExtra(TASK_ID_KEY, taskId)
                .putExtra(NOTIFICATION_ID_KEY, notificationId)
                .putExtra(ACTION_KEY, ACTION_CANCEL_TASK)

        val pendingIntent =
            PendingIntent.getBroadcast(
                context.applicationContext,
                notificationId,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
            )

        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xdown_notification)
            .setContentTitle(
                "${context.getString(R.string.notification_xdown)} • [$templateName] " +
                    context.getString(R.string.execute_command_notification)
            )
            .setContentText(text)
            .setOngoing(true)
            .setProgress(PROGRESS_MAX, progress, progress == -1)
            .setColor(0xFF1DA1F2.toInt()) // Twitter Blue
            .addAction(
                R.drawable.outline_cancel_24,
                context.getString(R.string.cancel),
                pendingIntent,
            )
            .run { notificationManager.notify(notificationId, build()) }
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT <= 24) true
        else notificationManager.areNotificationsEnabled()
    }
    
    private fun getCurrentTime(): String {
        val now = System.currentTimeMillis()
        val timeAgo = now - (now % 60000) // Round to nearest minute
        val minutesAgo = (now - timeAgo) / 60000
        
        return when {
            minutesAgo < 1 -> "Now"
            minutesAgo == 1L -> "1 minute ago"
            else -> "$minutesAgo minutes ago"
        }
    }
}

