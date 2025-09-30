package com.rit.twitdownloader.util

import android.content.Context

class RatingManager(private val context: Context) {
    
    private val ratingPreferences = RatingPreferences(context)
    private val engagementTracker = UserEngagementTracker(context)
    
    companion object {
        private val LAUNCH_THRESHOLDS = listOf(3, 10, 15)
    }
    
    /**
     * Check if we should show the rating prompt
     * Returns true if all conditions are met
     */
    fun shouldShowRatingPrompt(): Boolean {
        val status = ratingPreferences.getRatingStatus()
        val attempts = ratingPreferences.getRatingPromptAttempts()
        
        println("RatingManager: Checking prompt conditions...")
        println("RatingManager: Status: $status, Attempts: $attempts")
        
        // Never ask if already rated or opted out
        if (status == RatingStatus.RATED.name || status == RatingStatus.NEVER_ASK_AGAIN.name) {
            println("RatingManager: User already rated or opted out. Not showing prompt.")
            return false
        }
        
        // Check if we've reached max attempts
        if (attempts >= 3) {
            println("RatingManager: Max attempts reached. Setting to never ask again.")
            ratingPreferences.setRatingStatus(RatingStatus.NEVER_ASK_AGAIN)
            return false
        }
        
        val launches = engagementTracker.getLaunchCount()
        val lastPromptLaunches = ratingPreferences.getLastPromptLaunches()
        
        // Check if enough progress made since last prompt
        val newLaunches = launches - lastPromptLaunches
        
        println("RatingManager: Launches: $launches, Last prompt: $lastPromptLaunches, New: $newLaunches")
        
        // For first prompt (attempts = 0), don't require progress
        // For subsequent prompts, require at least 2 new launches
        if (attempts > 0 && newLaunches < 2) {
            println("RatingManager: Not enough progress since last prompt. Need at least 2 new launches.")
            return false
        }
        
        // Check threshold
        val thresholdMet = checkLaunchesThreshold(launches, newLaunches, attempts)
        
        if (thresholdMet) {
            println("RatingManager: Threshold met! Should show prompt.")
        } else {
            println("RatingManager: Threshold not met. Not showing prompt.")
        }
        
        return thresholdMet
    }
    
    /**
     * Check if launches threshold is met
     */
    private fun checkLaunchesThreshold(current: Int, new: Int, attempts: Int): Boolean {
        if (attempts >= LAUNCH_THRESHOLDS.size) {
            println("RatingManager: All thresholds exhausted.")
            return false
        }
        
        val requiredLaunches = LAUNCH_THRESHOLDS[attempts]
        
        // For first prompt (attempts = 0), only check if current >= required
        // For subsequent prompts, also check if new >= 2
        val thresholdMet = if (attempts == 0) {
            current >= requiredLaunches
        } else {
            current >= requiredLaunches && new >= 2
        }
        
        println("RatingManager: Required launches: $requiredLaunches, Current: $current, New: $new, Met: $thresholdMet")
        
        return thresholdMet
    }
    
    /**
     * Called when rating prompt is shown
     * Updates attempt count and last prompt launches
     */
    fun onRatingPromptShown() {
        val attempts = ratingPreferences.getRatingPromptAttempts()
        val launches = engagementTracker.getLaunchCount()
        
        ratingPreferences.incrementAttempts()
        ratingPreferences.setLastPromptLaunches(launches)
        
        println("RatingManager: Prompt shown. Attempts: ${attempts + 1}, Launches: $launches")
        
        // If this was the 3rd attempt, mark as never ask again
        if (attempts + 1 >= 3) {
            ratingPreferences.setRatingStatus(RatingStatus.NEVER_ASK_AGAIN)
            println("RatingManager: Max attempts reached. Marking as never ask again.")
        }
    }
    
    /**
     * Called when user clicks "Rate Now"
     * Sets status to RATED and never asks again
     */
    fun onUserRated() {
        ratingPreferences.setRatingStatus(RatingStatus.RATED)
        println("RatingManager: User rated. Never asking again.")
    }
    
    /**
     * Called when user clicks "Later"
     * Increments attempts and waits for next threshold
     */
    fun onUserDismissed() {
        ratingPreferences.setRatingStatus(RatingStatus.DISMISSED)
        println("RatingManager: User dismissed. Will ask again at next threshold.")
    }
    
    /**
     * Called when user clicks "No Thanks"
     * Sets status to NEVER_ASK_AGAIN
     */
    fun onUserOptedOut() {
        ratingPreferences.setRatingStatus(RatingStatus.NEVER_ASK_AGAIN)
        println("RatingManager: User opted out. Never asking again.")
    }
    
    /**
     * Track a successful download
     * This should be called whenever a download completes
     */
    fun trackSuccessfulDownload() {
        engagementTracker.trackSuccessfulDownload()
    }
    
    /**
     * Track app launch
     * This should be called in MainActivity.onCreate()
     */
    fun trackAppLaunch() {
        engagementTracker.trackAppLaunch()
    }
    
    /**
     * Get current rating status
     */
    fun getRatingStatus(): String {
        return ratingPreferences.getRatingStatus()
    }
    
    /**
     * Get debug information
     */
    fun getDebugInfo(): String {
        return engagementTracker.getDebugInfo()
    }
    
    /**
     * Reset all rating data (for testing)
     */
    fun resetAll() {
        ratingPreferences.clearAll()
        println("RatingManager: All data reset for testing.")
    }
}
