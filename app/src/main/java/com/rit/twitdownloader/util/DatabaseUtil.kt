package com.rit.twitdownloader.util

import androidx.room.Room
import com.rit.twitdownloader.App.Companion.applicationScope
import com.rit.twitdownloader.App.Companion.context
import com.rit.twitdownloader.database.AppDatabase
import com.rit.twitdownloader.database.backup.Backup
import com.rit.twitdownloader.database.backup.BackupUtil.BackupType
import com.rit.twitdownloader.database.backup.BackupUtil.decodeToBackup
import com.rit.twitdownloader.database.objects.CommandTemplate
import com.rit.twitdownloader.database.objects.CookieProfile
import com.rit.twitdownloader.database.objects.DownloadedVideoInfo
import com.rit.twitdownloader.database.objects.OptionShortcut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object DatabaseUtil {
    private const val DATABASE_NAME = "twitdownloader_database" // Use a unique name for current package
    
    @Volatile
    private var INSTANCE: AppDatabase? = null
    
    private val db: AppDatabase
        get() = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase().also { INSTANCE = it }
        }
    
    private val dao = db.videoInfoDao()
    
    private fun buildDatabase(): AppDatabase {
        android.util.Log.d("DatabaseUtil", "Building database with context: ${context.packageName}")
        android.util.Log.d("DatabaseUtil", "Using database name: $DATABASE_NAME")
        
        // Check for existing database files from old package names
        checkAndMigrateOldDatabases()
        
        val database = Room.databaseBuilder(
            context.applicationContext, // Use application context to prevent memory leaks
            AppDatabase::class.java,
            DATABASE_NAME
        ).build()
        
        android.util.Log.d("DatabaseUtil", "Database built successfully: $DATABASE_NAME")
        return database
    }
    
    private fun checkAndMigrateOldDatabases() {
        try {
            val databaseDir = context.applicationContext.getDatabasePath(DATABASE_NAME).parentFile
            if (databaseDir?.exists() == true) {
                val databaseFiles = databaseDir.listFiles { file ->
                    file.extension == "db" || file.extension == "db-shm" || file.extension == "db-wal"
                }
                
                android.util.Log.d("DatabaseUtil", "Found ${databaseFiles?.size ?: 0} database files:")
                databaseFiles?.forEach { file ->
                    android.util.Log.d("DatabaseUtil", "Database file: ${file.name}, size: ${file.length()} bytes")
                }
                
                // Delete old database files from previous package names
                deleteOldDatabaseFiles(databaseFiles)
            }
        } catch (e: Exception) {
            android.util.Log.e("DatabaseUtil", "Error checking database files", e)
        }
    }
    
    private fun deleteOldDatabaseFiles(databaseFiles: Array<File>?) {
        if (databaseFiles == null) return
        
        val currentPackageName = context.packageName
        android.util.Log.d("DatabaseUtil", "Current package name: $currentPackageName")
        
        databaseFiles.forEach { file ->
            val fileName = file.name
            // Check if this is an old database file from a different package
            val isOldDatabase = when {
                fileName.contains("com.junkfood.seal") -> true
                fileName.contains("com.rit.twidown") -> true
                fileName.contains("com.rit.twitdown") -> true
                fileName.contains("com.junkfood.tvd") -> true
                fileName.contains("com.junkfood.TwiDown") -> true
                else -> false
            }
            
            if (isOldDatabase) {
                try {
                    android.util.Log.d("DatabaseUtil", "Deleting old database file: $fileName")
                    file.delete()
                    android.util.Log.d("DatabaseUtil", "Successfully deleted old database: $fileName")
                } catch (e: Exception) {
                    android.util.Log.e("DatabaseUtil", "Failed to delete old database: $fileName", e)
                }
            } else {
                android.util.Log.d("DatabaseUtil", "Keeping current database file: $fileName")
            }
        }
    }
    
    init {
        android.util.Log.d("DatabaseUtil", "DatabaseUtil initialized")
        // Force database initialization to ensure it's ready
        applicationScope.launch(Dispatchers.IO) {
            try {
                val isWorking = testDatabase()
                android.util.Log.d("DatabaseUtil", "Database initialization test result: $isWorking")
            } catch (e: Exception) {
                android.util.Log.e("DatabaseUtil", "Database initialization failed", e)
            }
        }
    }

    fun insertInfo(vararg infoList: DownloadedVideoInfo) {
        applicationScope.launch(Dispatchers.IO) {
            try {
                infoList.forEach { info ->
                    android.util.Log.d("DatabaseUtil", "Inserting download info: ${info.videoTitle} at ${info.videoPath}")
                    dao.insertInfoDistinctByPath(info)
                    android.util.Log.d("DatabaseUtil", "Successfully inserted: ${info.videoTitle}")
                }
            } catch (e: Exception) {
                android.util.Log.e("DatabaseUtil", "Failed to insert download info", e)
            }
        }
    }

    init {
        applicationScope.launch {
            getTemplateFlow().collect {
                if (it.isEmpty()) PreferenceUtil.initializeTemplateSample()
            }
        }
    }

    fun getDownloadHistoryFlow() = dao.getDownloadHistoryFlow()

    private suspend fun getDownloadHistory() = dao.getDownloadHistory()
    
    // Debug method to get current download count
    suspend fun getDownloadCount(): Int {
        return try {
            val count = dao.getDownloadHistory().size
            android.util.Log.d("DatabaseUtil", "Current download count: $count")
            count
        } catch (e: Exception) {
            android.util.Log.e("DatabaseUtil", "Failed to get download count", e)
            0
        }
    }
    
    // Force refresh database connection
    fun forceRefreshDatabase() {
        android.util.Log.d("DatabaseUtil", "Forcing database refresh")
        INSTANCE = null // Clear the singleton instance
        // The next access will create a new instance
    }
    
    // Clean up all old database files from app data directory
    fun cleanupOldDatabases() {
        try {
            val appDataDir = context.applicationContext.filesDir.parentFile
            if (appDataDir?.exists() == true) {
                val databaseDir = File(appDataDir, "databases")
                if (databaseDir.exists()) {
                    val databaseFiles = databaseDir.listFiles { file ->
                        file.extension == "db" || file.extension == "db-shm" || file.extension == "db-wal"
                    }
                    
                    android.util.Log.d("DatabaseUtil", "Found ${databaseFiles?.size ?: 0} database files in app data directory")
                    
                    databaseFiles?.forEach { file ->
                        val fileName = file.name
                        val isOldDatabase = when {
                            fileName.contains("com.junkfood.seal") -> true
                            fileName.contains("com.rit.twidown") -> true
                            fileName.contains("com.rit.twitdown") -> true
                            fileName.contains("com.junkfood.tvd") -> true
                            fileName.contains("com.junkfood.TwiDown") -> true
                            else -> false
                        }
                        
                        if (isOldDatabase) {
                            try {
                                android.util.Log.d("DatabaseUtil", "Deleting old database file from app data: $fileName")
                                file.delete()
                                android.util.Log.d("DatabaseUtil", "Successfully deleted old database from app data: $fileName")
                            } catch (e: Exception) {
                                android.util.Log.e("DatabaseUtil", "Failed to delete old database from app data: $fileName", e)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DatabaseUtil", "Error cleaning up old databases", e)
        }
    }
    
    // Get all downloads with detailed logging
    suspend fun getAllDownloadsWithLogging(): List<DownloadedVideoInfo> {
        return try {
            val downloads = dao.getDownloadHistory()
            android.util.Log.d("DatabaseUtil", "Retrieved ${downloads.size} downloads from database:")
            downloads.forEachIndexed { index, download ->
                android.util.Log.d("DatabaseUtil", "Download $index: ${download.videoTitle} - ${download.videoUrl} - ${download.videoPath}")
            }
            downloads
        } catch (e: Exception) {
            android.util.Log.e("DatabaseUtil", "Failed to get downloads", e)
            emptyList()
        }
    }
    
    // Test function to verify database is working
    suspend fun testDatabase(): Boolean {
        return try {
            val count = dao.getDownloadHistory().size
            android.util.Log.d("DatabaseUtil", "Database test successful - found $count downloads")
            
            // Test insert and retrieve
            val testInfo = DownloadedVideoInfo(
                id = 0,
                videoTitle = "Test Video",
                videoAuthor = "Test Author", 
                videoUrl = "https://test.com",
                thumbnailUrl = "https://test.com/thumb.jpg",
                videoPath = "/test/path.mp4",
                extractor = "test"
            )
            
            dao.insert(testInfo)
            val retrieved = dao.getInfoByPath("/test/path.mp4")
            if (retrieved != null) {
                android.util.Log.d("DatabaseUtil", "Database insert/retrieve test successful")
                dao.deleteInfoByPath("/test/path.mp4") // Clean up test data
                true
            } else {
                android.util.Log.e("DatabaseUtil", "Database insert/retrieve test failed")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("DatabaseUtil", "Database test failed", e)
            false
        }
    }

    fun getTemplateFlow() = dao.getTemplateFlow()

    fun getCookiesFlow() = dao.getCookieProfileFlow()

    fun getShortcuts() = dao.getOptionShortcuts()

    suspend fun deleteShortcut(shortcut: OptionShortcut) = dao.deleteShortcut(shortcut)

    suspend fun insertShortcut(shortcut: OptionShortcut) = dao.insertShortcut(shortcut)

    suspend fun getCookieById(id: Int) = dao.getCookieById(id)

    suspend fun deleteCookieProfile(profile: CookieProfile) = dao.deleteCookieProfile(profile)

    suspend fun insertCookieProfile(profile: CookieProfile) = dao.insertCookieProfile(profile)

    suspend fun updateCookieProfile(profile: CookieProfile) = dao.updateCookieProfile(profile)

    suspend fun getTemplateList() = dao.getTemplateList()

    suspend fun getShortcutList() = dao.getShortcutList()

    suspend fun deleteInfoList(infoList: List<DownloadedVideoInfo>, deleteFile: Boolean = false) {
        dao.deleteInfoList(infoList)
        infoList.forEach { info -> if (deleteFile) FileUtil.deleteFile(info.videoPath) }
    }

    suspend fun getInfoById(id: Int): DownloadedVideoInfo = dao.getInfoById(id)

    suspend fun getInfoByUrl(url: String): DownloadedVideoInfo? = dao.getInfoByUrl(url)

    suspend fun deleteInfoById(id: Int) = dao.deleteInfoById(id)

    suspend fun insertTemplate(commandTemplate: CommandTemplate) =
        dao.insertTemplate(commandTemplate)

    suspend fun updateTemplate(commandTemplate: CommandTemplate) {
        dao.updateTemplate(commandTemplate)
    }

    suspend fun importBackup(backup: Backup, types: Set<BackupType>): Int {
        var cnt = 0
        backup.run {
            if (types.contains(BackupType.DownloadHistory)) {
                val itemList = getDownloadHistory()

                if (!downloadHistory.isNullOrEmpty()) {
                    dao.insertAll(
                        downloadHistory
                            .filterNot { itemList.contains(it) }
                            .map { it.copy(id = 0) }
                            .also { cnt += it.size }
                    )
                }
            }
            if (types.contains(BackupType.CommandTemplate)) {
                if (templates != null) {
                    val templateList = getTemplateList()
                    dao.importTemplates(
                        templateList
                            .filterNot { templateList.contains(it) }
                            .map { it.copy(id = 0) }
                            .also { cnt += it.size }
                    )
                }
            }
            if (types.contains(BackupType.CommandShortcut)) {
                val shortcutList = getShortcutList()
                if (shortcuts != null) {
                    dao.insertAllShortcuts(
                        shortcuts
                            .filterNot { shortcutList.contains(it) }
                            .map { it.copy(id = 0) }
                            .also { cnt += it.size }
                    )
                }
            }
        }
        return cnt
    }

    suspend fun importTemplatesFromJson(json: String): Int {
        json
            .decodeToBackup()
            .onSuccess { backup ->
                return importBackup(
                    backup = backup,
                    types = setOf(BackupType.CommandTemplate, BackupType.CommandShortcut),
                )
            }
            .onFailure { it.printStackTrace() }
        return 0
    }

    suspend fun deleteTemplateById(id: Int) = dao.deleteTemplateById(id)

    suspend fun deleteTemplates(templates: List<CommandTemplate>) = dao.deleteTemplates(templates)

    private const val TAG = "DatabaseUtil"
}

