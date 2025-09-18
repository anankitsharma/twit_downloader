---
description: Add internal video player for downloaded videos only
---

# Rule: Internal Video Player Feature

These instructions govern how to implement an **internal video player** for downloaded videos in the project. Cursor AI **must follow** all steps to avoid errors.

1. **File modifications / creations**  
   - Modify `app/build.gradle.kts` to add `androidx.media3` / ExoPlayer dependencies.  
   - Modify `app/src/main/AndroidManifest.xml`: add `VideoPlayerActivity` and `FileProvider` provider.  
   - Create `VideoPlayerActivity.kt` in `ui/player/` directory.  
   - Create `VideoPlayerScreen.kt` in `ui/player/`.  
   - Modify `FileUtil.kt` to route opening downloaded videos to internal player and fallback.  
   - Modify `VideoListPage.kt` so click on a downloaded video calls `FileUtil.openFile(...)`.  
   - If missing, create `res/xml/file_paths.xml` for `FileProvider` configuration.

2. **Behavior rules**  
   - Internal player must open *only* when the user taps on a *downloaded video*. For non-downloaded video links, external apps must be used.  
   - Support both `File` + `content://` URIs. If `File` used, convert via `FileProvider`.  
   - Pass URI as `String` extra `EXTRA_VIDEO_URI` to `VideoPlayerActivity`. Include `FLAG_GRANT_READ_URI_PERMISSION` if needed.  

3. **Player lifecycle and UI**  
   - Use ExoPlayer (Media3) for playback. Prepare, auto-play, then release in `onDestroy()`, pause in `onStop()`.  
   - UI must use `PlayerView` via Compose’s `AndroidView`. Include play/pause/seek controllers, back button overlay, controller hide timeout.  

4. **Error handling & fallbacks**  
   - If `Uri` is invalid, file missing, or `SecurityException`, show toast or Snackbar with user-friendly message.  
   - On failure to prepare / playback errors, fallback option: **Open with external app**.  
   - Log all errors with tag (e.g. `PLAYER_ERROR`), include `uri` and stack trace.  

5. **Testing requirements**  
   - Manual test: tap downloaded video → internal player launches and plays.  
   - Manual test: tap non-downloaded video link → external chooser.  
   - Test on Android APIs 23, 29, 33+.  
   - Edge cases: missing file, revoked permission, malformed URI.  

6. **Documentation & commit**  
   - Add README or module documentation paragraph describing internal player behavior and known limitations.  
   - Use commit message: `feat(player): internal video player for downloaded videos only, with fallback/error handling`.

