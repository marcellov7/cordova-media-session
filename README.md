# Cordova Media Session Plugin

This Cordova plugin provides a unified interface for the Media Session API on Android platforms. It offers a consistent API for managing media metadata and playback state, as well as handling media control actions.

## Installation

```bash
cordova plugin add cordova-plugin-media-session
```

## Usage

The plugin provides a global `MediaSession` object that offers a consistent API.

### Setting Metadata

```javascript
MediaSession.setMetadata({
    title: "Song Title",
    artist: "Artist Name",
    album: "Album Name"
}).then(() => {
    console.log("Metadata set successfully");
}).catch((error) => {
    console.error("Error setting metadata:", error);
});
```

### Setting Playback State

```javascript
MediaSession.setPlaybackState({
    state: "playing", // or "paused"
    position: 0,
    speed: 1.0
}).then(() => {
    console.log("Playback state set successfully");
}).catch((error) => {
    console.error("Error setting playback state:", error);
});
```

### Handling Actions

```javascript
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
```

## Platform Support

- Android 5.0+ (API level 21+)

## Developer Notes

- The plugin provides a unified API that works on Android.
- On Android, the plugin uses native Media Session APIs under the hood.
- The plugin creates a notification with media controls when metadata and playback state are set.
- Make sure your app has the necessary permissions to show notifications.

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
    }).then(() => {
        return MediaSession.setPlaybackState({
            state: "playing",
            position: 0,
            speed: 1.0
        });
    }).then(() => {
        console.log("Media session set up successfully");
    }).catch((error) => {
        console.error("Error setting up media session:", error);
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
```

## License

This project is licensed under the MIT License.