package com.rit.twitdownloader

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.rit.twitdownloader.App.Companion.context
import com.rit.twitdownloader.download.DownloaderV2
import com.rit.twitdownloader.util.FileUtil
import com.rit.twitdownloader.util.NotificationUtil
import com.rit.twitdownloader.util.ToastUtil
import com.yausername.youtubedl_android.YoutubeDL
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class NotificationActionReceiver : BroadcastReceiver(), KoinComponent {
    val downloader = get<DownloaderV2>()

    companion object {
        private const val TAG = "CancelReceiver"
        private const val PACKAGE_NAME_PREFIX = "com.junkfood.seal."

        const val ACTION_CANCEL_TASK = 0
        const val ACTION_ERROR_REPORT = 1

        const val ACTION_KEY = PACKAGE_NAME_PREFIX + "action"
        const val TASK_ID_KEY = PACKAGE_NAME_PREFIX + "taskId"

        const val NOTIFICATION_ID_KEY = PACKAGE_NAME_PREFIX + "notificationId"
        const val ERROR_REPORT_KEY = PACKAGE_NAME_PREFIX + "error_report"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val notificationId = intent.getIntExtra(NOTIFICATION_ID_KEY, 0)
        val action = intent.getStringExtra(ACTION_KEY)
        val taskId = intent.getStringExtra(TASK_ID_KEY)
        Log.d(TAG, "onReceive: action=$action, taskId=$taskId, notificationId=$notificationId")
        
        when (action) {
            ACTION_CANCEL_TASK.toString() -> {
                val taskId = intent.getStringExtra(TASK_ID_KEY)
                cancelTask(taskId, notificationId)
            }

            ACTION_ERROR_REPORT.toString() -> {
                val errorReport = intent.getStringExtra(ERROR_REPORT_KEY)
                if (!errorReport.isNullOrEmpty()) copyErrorReport(errorReport, notificationId)
            }
            
            "DISMISS" -> {
                // Dismiss notification
                NotificationUtil.cancelNotification(NotificationUtil.SERVICE_NOTIFICATION_ID)
            }
            
            "OPEN_FILE" -> {
                // Open the specific video file using FileUtil.openFile()
                try {
                    val filePath = intent.getStringExtra("FILE_PATH")
                    Log.d(TAG, "Opening file: $filePath")
                    
                    if (!filePath.isNullOrEmpty()) {
                        // Use the same FileUtil.openFile() method as the app UI
                        FileUtil.openFile(path = filePath) { 
                            Log.e(TAG, "File not available: $filePath")
                            ToastUtil.makeToastSuspend("File not available")
                        }
                    } else {
                        // Fallback to opening the downloads folder
                        val downloadsFolder = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                        val xdownFolder = java.io.File(downloadsFolder, "Seal")
                        
                        Log.d(TAG, "No file path provided, opening folder: ${xdownFolder.absolutePath}")
                        
                        if (xdownFolder.exists()) {
                            val folderUri = FileProvider.getUriForFile(
                                context!!,
                                "${context.packageName}.provider",
                                xdownFolder
                            )
                            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(folderUri, "resource/folder")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(openIntent)
                        } else {
                            // Fallback to opening the app
                            val appIntent = Intent(context, MainActivity::class.java)
                            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context?.startActivity(appIntent)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error opening file: ${e.message}", e)
                    // Fallback to opening the app
                    try {
                        val appIntent = Intent(context, MainActivity::class.java)
                        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context?.startActivity(appIntent)
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error opening app: ${ex.message}", ex)
                    }
                }
            }
            
            "PAUSE" -> {
                // Pause the current download
                val taskId = intent.getStringExtra(TASK_ID_KEY)
                if (!taskId.isNullOrEmpty()) {
                    cancelTask(taskId, notificationId)
                }
            }
        }
    }

    private fun cancelTask(taskId: String?, notificationId: Int) {
        if (taskId.isNullOrEmpty()) return
        NotificationUtil.cancelNotification(notificationId)
        val res = downloader.cancel(taskId)
        if (res) {
            Log.d(TAG, "Task (id:$taskId) was killed.")
        } else {
            // todo: reserved for custom commands
            YoutubeDL.destroyProcessById(taskId)
            Downloader.onProcessCanceled(taskId)
        }
    }

    private fun copyErrorReport(error: String, notificationId: Int) {
        App.clipboard.setPrimaryClip(ClipData.newPlainText(null, error))
        context.let { ToastUtil.makeToastSuspend(it.getString(R.string.error_copied)) }
        NotificationUtil.cancelNotification(notificationId)
    }
}

