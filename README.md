# Cordova Background Media Plugin

This Cordova plugin enables background audio and video playback with support for HLS and DASH streaming. It provides a unified JavaScript API for both Android and iOS platforms, allowing you to control media playback and display now playing information in the system's notification area or control center.

## Features

- Background audio and video playback
- Support for HLS and DASH streaming
- Media controls in system notification (Android) and control center (iOS)
- Custom metadata display (title, artist, artwork)
- Playback time control
- Volume control
- Playlist management with unique IDs
- Next/Previous track controls
- Playback events (start, end, skip forward, skip backward)

## Installation

```bash
cordova plugin add cordova-plugin-background-media
```

## Usage

```javascript
var backgroundMedia = cordova.plugins.backgroundMedia;

// Play a single track
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

// Set a playlist
var playlist = [
    {id: "1", url: "https://example.com/song1.mp3", title: "Song 1", artist: "Artist 1"},
    {id: "2", url: "https://example.com/song2.mp3", title: "Song 2", artist: "Artist 2"},
    // ...
];

backgroundMedia.setPlaylist(playlist, 
    function(message) {
        console.log('Success: ' + message);
    },
    function(error) {
        console.error('Error: ' + error);
    }
);

// Add a track to the playlist
backgroundMedia.addToPlaylist(
    {id: "3", url: "https://example.com/song3.mp3", title: "Song 3", artist: "Artist 3"},
    function(message) {
        console.log('Success: ' + message);
    },
    function(error) {
        console.error('Error: ' + error);
    }
);

// Remove a track from the playlist
backgroundMedia.removeFromPlaylist(
    "2", // track ID
    function(message) {
        console.log('Success: ' + message);
    },
    function(error) {
        console.error('Error: ' + error);
    }
);

// Play a specific track by ID
backgroundMedia.playById(
    "3", // track ID
    function(message) {
        console.log('Success: ' + message);
    },
    function(error) {
        console.error('Error: ' + error);
    }
);

// Listen for events
backgroundMedia.onEvent(function(event) {
    switch(event) {
        case "onPlaybackStart":
            console.log("Playback started");
            break;
        case "onPlaybackEnd":
            console.log("Playback ended");
            break;
        case "onSkipForward":
            console.log("Skipped to next track");
            break;
        case "onSkipBackward":
            console.log("Skipped to previous track");
            break;
    }
});

// Other methods
backgroundMedia.pause(successCallback, errorCallback);
backgroundMedia.stop(successCallback, errorCallback);
backgroundMedia.setVolume(0.5, successCallback, errorCallback);
backgroundMedia.setPlaybackTime(60, successCallback, errorCallback);
backgroundMedia.playNext(successCallback, errorCallback);
backgroundMedia.playPrevious(successCallback, errorCallback);
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

### setPlaylist(playlist, successCallback, errorCallback)

Sets the current playlist.

### addToPlaylist(track, successCallback, errorCallback)

Adds a track to the current playlist.

### removeFromPlaylist(id, successCallback, errorCallback)

Removes a track from the current playlist by ID.

### playById(id, successCallback, errorCallback)

Plays a specific track from the playlist by ID.

### playNext(successCallback, errorCallback)

Skips to the next track in the playlist.

### playPrevious(successCallback, errorCallback)

Skips to the previous track in the playlist.

### onEvent(callback)

Registers a callback to receive playback events.

### destroy(successCallback, errorCallback)

Destroys the plugin instance and releases all resources.

## Events

- `onPlaybackStart`: Fired when playback starts
- `onPlaybackEnd`: Fired when playback ends
- `onSkipForward`: Fired when the user skips to the next track
- `onSkipBackward`: Fired when the user skips to the previous track

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