# Cordova Background Media Plugin

This Cordova plugin enables background audio and video playback with support for HLS and DASH streaming. It provides a unified JavaScript API for both Android and iOS platforms, allowing you to control media playback and display now playing information in the system's notification area or control center.

## Features

- Background audio and video playback
- Support for HLS and DASH streaming
- Media controls in system notification (Android) and control center (iOS)
- Custom metadata display (title, artist, artwork)
- Playback time control
- Next/Previous track controls
- Playback events (start, end, skip forward, skip backward)

## Installation

```bash
cordova plugin add cordova-plugin-background-media
```

## Usage

```javascript
var backgroundMedia = cordova.plugins.backgroundMedia;

backgroundMedia.play(
    'https://example.com/stream.m3u8',
    'Song Title',
    'Artist Name',
    'https://example.com/artwork.jpg',
    false, // isVideo
    0, // startTime
    function(message) {
        console.log('Success: ' + message);
    },
    function(error) {
        console.error('Error: ' + error);
    }
);

// Listen for events
backgroundMedia.onEvent(function(event) {
    if (event.startsWith("onSkipForward")) {
        console.log("User pressed skip forward");
        // Start playback of next media
    } else if (event.startsWith("onSkipBackward")) {
        console.log("User pressed skip backward");
        // Start playback of previous media
    } else if (event.startsWith("onPlaybackEnd")) {
        console.log("Playback ended");
        // Start playback of next media
    } else if (event.startsWith("onPlaybackStart")) {
        console.log("Playback started");
    }
});

// Other methods
backgroundMedia.pause(successCallback, errorCallback);
backgroundMedia.stop(successCallback, errorCallback);
backgroundMedia.setVolume(0.5, successCallback, errorCallback);
backgroundMedia.setPlaybackTime(60, successCallback, errorCallback);
backgroundMedia.destroy(successCallback, errorCallback);
```

## API Reference

### play(url, title, artist, artwork, isVideo, startTime, successCallback, errorCallback)

Starts playback of the specified media.

### pause(successCallback, errorCallback)

Pauses the current playback.

### stop(successCallback, errorCallback)

Stops the current playback and releases resources.

### setVolume(volume, successCallback, errorCallback)

Sets the playback volume (0.0 to 1.0).

### setPlaybackTime(time, successCallback, errorCallback)

Sets the current playback time in seconds.

### onEvent(callback)

Registers a callback to receive playback events.

### destroy(successCallback, errorCallback)

Destroys the plugin instance and releases all resources.

## Events

- `onPlaybackStart`: Fired when playback starts
- `onPlaybackEnd`: Fired when playback ends
- `onSkipForward`: Fired when the user presses the next track button
- `onSkipBackward`: Fired when the user presses the previous track button


## Requirements

- Cordova 9.0.0 or higher
- iOS 11.0 or higher
- Android 5.0 (API 21) or higher

## License

This project is licensed under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

If you're having any problem, please raise an issue on GitHub and we'll be happy to help.