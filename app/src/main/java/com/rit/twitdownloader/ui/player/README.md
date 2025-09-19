# Internal Video Player Module

This module provides an internal video player for downloaded videos in the Twitter Video Downloader app.

## Features

- **Internal Video Playback**: Downloaded videos open in the app's built-in player instead of external apps
- **ExoPlayer Integration**: Uses Media3 ExoPlayer for robust video playback
- **Error Handling**: Graceful fallback to external apps if internal player fails
- **Fullscreen Support**: Immersive fullscreen video experience
- **File Support**: Supports both local files and content URIs

## Behavior

- **Downloaded Videos Only**: Internal player only opens for downloaded video files (mp4, mkv, webm, avi, mov, flv, wmv, 3gp)
- **External Fallback**: Non-video files and failed video loads fall back to external apps
- **URI Handling**: Supports both `file://` and `content://` URIs with proper permission handling

## Architecture

- `VideoPlayerActivity`: Main activity that handles video playback
- `VideoPlayerScreen`: Compose UI for the video player
- `VideoPlayerViewModel`: Manages ExoPlayer state and lifecycle
- `FileUtil.openFile()`: Routes video files to internal player automatically

## Error Handling

- Invalid URIs show user-friendly error messages
- Missing files trigger fallback to external apps
- Security exceptions are logged and handled gracefully
- All errors include proper logging with `PLAYER_ERROR` tag

## Testing

Test on Android APIs 23, 29, 33+ with various video formats and file locations.





