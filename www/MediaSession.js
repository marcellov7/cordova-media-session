var exec = require('cordova/exec');

var MediaSession = {
    setMetadata: function(options) {
        if (cordova.platformId === 'android') {
            return new Promise(function(resolve, reject) {
                exec(resolve, reject, 'MediaSession', 'setMetadata', [options]);
            });
        } else {
            if ('mediaSession' in navigator) {
                navigator.mediaSession.metadata = new MediaMetadata(options);
                return Promise.resolve();
            } else {
                return Promise.reject('Media Session API not available');
            }
        }
    },
    
    setPlaybackState: function(options) {
        if (cordova.platformId === 'android') {
            return new Promise(function(resolve, reject) {
                exec(resolve, reject, 'MediaSession', 'setPlaybackState', [options]);
            });
        } else {
            if ('mediaSession' in navigator) {
                navigator.mediaSession.playbackState = options.playbackState;
                return Promise.resolve();
            } else {
                return Promise.reject('Media Session API not available');
            }
        }
    },
    
    setActionHandler: function(options, handler) {
        if (cordova.platformId === 'android') {
            return new Promise(function(resolve, reject) {
                exec(resolve, reject, 'MediaSession', 'setActionHandler', [options, handler]);
            });
        } else {
            if ('mediaSession' in navigator) {
                navigator.mediaSession.setActionHandler(options.action, handler);
                return Promise.resolve();
            } else {
                return Promise.reject('Media Session API not available');
            }
        }
    },
    
    setPositionState: function(options) {
        if (cordova.platformId === 'android') {
            return new Promise(function(resolve, reject) {
                exec(resolve, reject, 'MediaSession', 'setPositionState', [options]);
            });
        } else {
            if ('mediaSession' in navigator) {
                navigator.mediaSession.setPositionState(options);
                return Promise.resolve();
            } else {
                return Promise.reject('Media Session API not available');
            }
        }
    }
};

module.exports = MediaSession;