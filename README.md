# Cordova Media Session Plugin

This Cordova plugin provides an interface for Media Sessions on Android, iOS, and Web. It allows you to control media playback, set metadata, and handle media control actions.

## Installation

```bash
cordova plugin add cordova-plugin-media-session
```

## Usage

The plugin is accessible through the `cordova.plugins.MediaSession` object.

### Setting Metadata

```javascript
cordova.plugins.MediaSession.setMetadata({
    title: 'Song Name',
    artist: 'Artist Name',
    album: 'Album Name',
    artwork: [
        { src: 'https://example.com/artwork.png', sizes: '512x512', type: 'image/png' }
    ]
}).then(() => {
    console.log('Metadata set successfully');
}).catch((error) => {
    console.error('Error setting metadata', error);
});
```

### Setting Playback State

```javascript
cordova.plugins.MediaSession.setPlaybackState({
    playbackState: 'playing' // or 'paused'
}).then(() => {
    console.log('Playback state set successfully');
}).catch((error) => {
    console.error('Error setting playback state', error);
});
```

### Handling Actions

```javascript
cordova.plugins.MediaSession.setActionHandler({
    action: 'play'
}, () => {
    console.log('Play action triggered');
    // Start playback here
});

cordova.plugins.MediaSession.setActionHandler({
    action: 'pause'
}, () => {
    console.log('Pause action triggered');
    // Pause playback here
});

// Other available actions: 'previoustrack', 'nexttrack', 'seekbackward', 'seekforward', 'seekto', 'stop'
```

### Setting Position State

```javascript
cordova.plugins.MediaSession.setPositionState({
    duration: 300, // total duration in seconds
    position: 60,  // current position in seconds
    playbackRate: 1.0 // playback speed
}).then(() => {
    console.log('Position state set successfully');
}).catch((error) => {
    console.error('Error setting position state', error);
});
```

## Behavior on Different Platforms

- **Android**: Uses a native implementation to provide media controls in the lock screen and notifications.
- **iOS and Web**: Uses the browser's native Media Sessions API, if supported.

## Notes

- On iOS and Web, functionality depends on the browser's support for the Media Sessions API.
- For the best experience on Android, make sure to handle all relevant actions (`play`, `pause`, `previoustrack`, `nexttrack`, etc.).
- Artwork on Android supports remote URLs and base64 data.

## Complete Usage Example

```javascript
document.addEventListener('deviceready', function() {
    // Set initial metadata
    cordova.plugins.MediaSession.setMetadata({
        title: 'My Awesome Song',
        artist: 'The Great Artist',
        album: 'Best Album Ever',
        artwork: [
            { src: 'https://example.com/album-art.jpg', sizes: '512x512', type: 'image/jpeg' }
        ]
    });

    // Set initial playback state
    cordova.plugins.MediaSession.setPlaybackState({ playbackState: 'paused' });

    // Handle play action
    cordova.plugins.MediaSession.setActionHandler({ action: 'play' }, function() {
        // Start playback
        myAudioElement.play();
        cordova.plugins.MediaSession.setPlaybackState({ playbackState: 'playing' });
    });

    // Handle pause action
    cordova.plugins.MediaSession.setActionHandler({ action: 'pause' }, function() {
        // Pause playback
        myAudioElement.pause();
        cordova.plugins.MediaSession.setPlaybackState({ playbackState: 'paused' });
    });

    // Update position state periodically
    setInterval(function() {
        cordova.plugins.MediaSession.setPositionState({
            duration: myAudioElement.duration,
            position: myAudioElement.currentTime,
            playbackRate: myAudioElement.playbackRate
        });
    }, 1000);
}, false);