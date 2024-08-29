# Cordova Background Media Plugin

This Cordova plugin enables background audio and video playback with support for HLS and DASH streaming. It provides a unified JavaScript API for both Android and iOS platforms, allowing you to control media playback and display now playing information in the system's notification area or control center.

## Features

- Background audio and video playback
- Support for HLS and DASH streaming
- Media controls in system notification (Android) and control center (iOS)
- Custom metadata display (title, artist, artwork)
- Playback time control

## Installation

```bash
cordova plugin add cordova-plugin-background-media
```

## Usage

```javascript
var backgroundMedia = cordova.plugins.backgroundMedia;

// Play media starting at 30 seconds
backgroundMedia.play(
    'https://example.com/media.m3u8',
    'Song Title',
    'Artist Name',
    'https://example.com/artwork.jpg',
    false, // isVideo
    30, // startTime in seconds
    function(message) {
        console.log('Success: ' + message);
    },
    function(error) {
        console.error('Error: ' + error);
    }
);

// Pause playback
backgroundMedia.pause(
    function(message) {
        console.log('Success: ' + message);
    },
    function(error) {
        console.error('Error: ' + error);
    }
);

// Stop playback
backgroundMedia.stop(
    function(message) {
        console.log('Success: ' + message);
    },
    function(error) {
        console.error('Error: ' + error);
    }
);

// Set volume (0.0 to 1.0)
backgroundMedia.setVolume(
    0.5,
    function(message) {
        console.log('Success: ' + message);
    },
    function(error) {
        console.error('Error: ' + error);
    }
);

// Set playback time to 1 minute
backgroundMedia.setPlaybackTime(
    60,
    function(message) {
        console.log('Success: ' + message);
    },
    function(error) {
        console.error('Error: ' + error);
    }
);

// Destroy the plugin and release resources
backgroundMedia.destroy(
    function(message) {
        console.log('Success: ' + message);
    },
    function(error) {
        console.error('Error: ' + error);
    }
);
```

## API Reference

### play(url, title, artist, artwork, isVideo, startTime, successCallback, errorCallback)

Starts playback of the specified media.

- `url` (String): The URL of the media to play (supports HLS and DASH)
- `title` (String): The title of the media
- `artist` (String): The artist name
- `artwork` (String): URL of the artwork image
- `isVideo` (Boolean): Set to true for video playback, false for audio
- `startTime` (Number): The time in seconds to start playback from
- `successCallback` (Function): Called on successful playback start
- `errorCallback` (Function): Called if an error occurs

### pause(successCallback, errorCallback)

Pauses the current playback.

- `successCallback` (Function): Called when playback is successfully paused
- `errorCallback` (Function): Called if an error occurs

### stop(successCallback, errorCallback)

Stops the current playback and releases resources.

- `successCallback` (Function): Called when playback is successfully stopped
- `errorCallback` (Function): Called if an error occurs

### setVolume(volume, successCallback, errorCallback)

Sets the playback volume.

- `volume` (Number): Volume level from 0.0 (mute) to 1.0 (max)
- `successCallback` (Function): Called when volume is successfully set
- `errorCallback` (Function): Called if an error occurs

### setPlaybackTime(time, successCallback, errorCallback)

Sets the current playback time.

- `time` (Number): The time in seconds to set the playback to
- `successCallback` (Function): Called when playback time is successfully set
- `errorCallback` (Function): Called if an error occurs

### destroy(successCallback, errorCallback)

Destroys the plugin instance and releases all resources.

- `successCallback` (Function): Called when the plugin is successfully destroyed
- `errorCallback` (Function): Called if an error occurs

## Platform Specific Notes

### Android

- Uses ExoPlayer for media playback
- Displays a notification with media controls and metadata

### iOS

- Uses AVPlayer for media playback
- Updates the Now Playing info in the Control Center and lock screen

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