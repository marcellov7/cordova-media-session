# Cordova Media Session Plugin

This Cordova plugin provides a unified interface for the Media Session API on Android and iOS platforms. It offers a consistent API across both operating systems as an independent entity.

## Installation

```bash
cordova plugin add cordova-media-session
```

## Usage

The plugin provides a global `MediaSession` object that offers a consistent API across platforms.

### Setting Metadata

```javascript
MediaSession.setMetadata({
    title: "Song Title",
    artist: "Artist Name",
    album: "Album Name"
});
```

### Setting Playback State

```javascript
MediaSession.setPlaybackState({
    state: "playing", // or "paused"
    position: 0,
    speed: 1.0
});
```

### Handling Actions

```javascript
MediaSession.setActionHandler("play", function() {
    // Logic for play
});

MediaSession.setActionHandler("pause", function() {
    // Logic for pause
});

MediaSession.setActionHandler("nexttrack", function() {
    // Logic for next track
});

MediaSession.setActionHandler("previoustrack", function() {
    // Logic for previous track
});
```

## Platform Support

- Android 5.0+ (API level 21+)
- iOS 10.0+

## Developer Notes

- The plugin provides a unified API that works consistently across both Android and iOS.
- On Android, the plugin uses native Media Session APIs under the hood.
- On iOS, the plugin leverages the Web APIs for Media Session available in the browser.
- Developers don't need to worry about platform-specific implementations; the plugin handles these differences internally.

## Example

Here's a complete example of how to use the plugin:

```javascript
document.addEventListener('deviceready', onDeviceReady, false);

function onDeviceReady() {
    // Set up media session
    MediaSession.setMetadata({
        title: "Amazing Song",
        artist: "Awesome Artist",
        album: "Fantastic Album"
    });

    MediaSession.setPlaybackState({
        state: "playing",
        position: 0,
        speed: 1.0
    });

    // Set up action handlers
    MediaSession.setActionHandler("play", function() {
        console.log("Play clicked");
        // Your play logic here
    });

    MediaSession.setActionHandler("pause", function() {
        console.log("Pause clicked");
        // Your pause logic here
    });

    MediaSession.setActionHandler("nexttrack", function() {
        console.log("Next track clicked");
        // Your next track logic here
    });

    MediaSession.setActionHandler("previoustrack", function() {
        console.log("Previous track clicked");
        // Your previous track logic here
    });
}
