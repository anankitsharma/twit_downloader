package com.rit.twitdownloader.util

import android.content.Context
import android.content.SharedPreferences

class RatingPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "rating_preferences"
        private const val KEY_DOWNLOADS = "successful_downloads"
        private const val KEY_ATTEMPTS = "rating_prompt_attempts"
        private const val KEY_STATUS = "user_rating_status"
        private const val KEY_LAST_PROMPT_DOWNLOADS = "last_rating_prompt_downloads"
    }
    
    // Download count
    fun getSuccessfulDownloads(): Int {
        return prefs.getInt(KEY_DOWNLOADS, 0)
    }
    
    fun setSuccessfulDownloads(count: Int) {
        prefs.edit().putInt(KEY_DOWNLOADS, count).apply()
    }
    
    fun incrementDownloads() {
        val current = getSuccessfulDownloads()
        setSuccessfulDownloads(current + 1)
    }
    
    // Rating prompt attempts
    fun getRatingPromptAttempts(): Int {
        return prefs.getInt(KEY_ATTEMPTS, 0)
    }
    
    fun setRatingPromptAttempts(attempts: Int) {
        prefs.edit().putInt(KEY_ATTEMPTS, attempts).apply()
    }
    
    fun incrementAttempts() {
        val current = getRatingPromptAttempts()
        setRatingPromptAttempts(current + 1)
    }
    
    // User rating status
    fun getRatingStatus(): String {
        return prefs.getString(KEY_STATUS, RatingStatus.NEVER_ASKED.name) ?: RatingStatus.NEVER_ASKED.name
    }
    
    fun setRatingStatus(status: RatingStatus) {
        prefs.edit().putString(KEY_STATUS, status.name).apply()
    }
    
    // Last prompt downloads
    fun getLastPromptDownloads(): Int {
        return prefs.getInt(KEY_LAST_PROMPT_DOWNLOADS, 0)
    }
    
    fun setLastPromptDownloads(downloads: Int) {
        prefs.edit().putInt(KEY_LAST_PROMPT_DOWNLOADS, downloads).apply()
    }
    
    // Last prompt launches
    fun getLastPromptLaunches(): Int {
        return prefs.getInt("last_rating_prompt_launches", 0)
    }
    
    fun setLastPromptLaunches(launches: Int) {
        prefs.edit().putInt("last_rating_prompt_launches", launches).apply()
    }
    
    // Generic int setter (for launch count)
    fun setInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
    
    fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }
    
    // Clear all data (for testing)
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    // Debug methods
    fun getDebugInfo(): String {
        return """
            Downloads: ${getSuccessfulDownloads()}
            Attempts: ${getRatingPromptAttempts()}
            Status: ${getRatingStatus()}
            Last Prompt Downloads: ${getLastPromptDownloads()}
        """.trimIndent()
    }
}

enum class RatingStatus {
    NEVER_ASKED,
    DISMISSED,
    RATED,
    NEVER_ASK_AGAIN
}
