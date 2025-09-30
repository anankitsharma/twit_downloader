package com.rit.twitdownloader.util

import android.content.Context

class UserEngagementTracker(private val context: Context) {
    
    private val ratingPreferences = RatingPreferences(context)
    
    /**
     * Track a successful download
     * This should be called whenever a download completes successfully
     */
    fun trackSuccessfulDownload() {
        ratingPreferences.incrementDownloads()
        println("RatingTracker: Download tracked. Total downloads: ${ratingPreferences.getSuccessfulDownloads()}")
    }
    
    /**
     * Get current download count
     */
    fun getDownloadCount(): Int {
        return ratingPreferences.getSuccessfulDownloads()
    }
    
    /**
     * Get current app launch count
     * This is tracked separately in MainActivity
     */
    fun getLaunchCount(): Int {
        return ratingPreferences.getInt("app_launch_count", 0)
    }
    
    /**
     * Track app launch
     * This should be called in MainActivity.onCreate()
     */
    fun trackAppLaunch() {
        val current = getLaunchCount()
        ratingPreferences.setInt("app_launch_count", current + 1)
        println("RatingTracker: App launch tracked. Total launches: ${getLaunchCount()}")
    }
    
    /**
     * Reset all tracking data (for testing)
     */
    fun resetAll() {
        ratingPreferences.clearAll()
        println("RatingTracker: All data reset")
    }
    
    /**
     * Get debug information
     */
    fun getDebugInfo(): String {
        return ratingPreferences.getDebugInfo()
    }
}
