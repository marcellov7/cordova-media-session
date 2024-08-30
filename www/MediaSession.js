// MediaSession.js

var exec = require('cordova/exec');

var MediaSession = {
    metadata: null,
    playbackState: null,

    setMetadata: function(metadata) {
        this.metadata = metadata;
        if (cordova.platformId === 'android') {
            exec(null, null, "MediaSession", "setMetadata", [metadata]);
        } else if (cordova.platformId === 'ios') {
            if ('mediaSession' in navigator) {
                navigator.mediaSession.metadata = new MediaMetadata(metadata);
            }
        }
    },

    setPlaybackState: function(state) {
        this.playbackState = state;
        if (cordova.platformId === 'android') {
            exec(null, null, "MediaSession", "setPlaybackState", [state]);
        } else if (cordova.platformId === 'ios') {
            if ('mediaSession' in navigator) {
                navigator.mediaSession.playbackState = state.state;
            }
        }
    },

    setActionHandler: function(action, handler) {
        if (cordova.platformId === 'android') {
            if (handler) {
                exec(handler, null, "MediaSession", "setActionHandler", [action]);
            } else {
                exec(null, null, "MediaSession", "clearActionHandler", [action]);
            }
        } else if (cordova.platformId === 'ios') {
            if ('mediaSession' in navigator) {
                navigator.mediaSession.setActionHandler(action, handler);
            }
        }
    }
};

module.exports = MediaSession;