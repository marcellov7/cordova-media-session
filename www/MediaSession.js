var exec = require('cordova/exec');

var MediaSession = {
    setMetadata: function(options) {
        return new Promise(function(resolve, reject) {
            if (cordova.platformId === 'android') {
                exec(resolve, reject, 'MediaSession', 'setMetadata', [options]);
            } else if ('mediaSession' in navigator) {
                navigator.mediaSession.metadata = new MediaMetadata(options);
                resolve();
            } else {
                reject('Media Session API not available');
            }
        });
    },
    
    setPlaybackState: function(options) {
        return new Promise(function(resolve, reject) {
            if (cordova.platformId === 'android') {
                exec(resolve, reject, 'MediaSession', 'setPlaybackState', [options]);
            } else if ('mediaSession' in navigator) {
                navigator.mediaSession.playbackState = options.playbackState;
                resolve();
            } else {
                reject('Media Session API not available');
            }
        });
    },
    
    setActionHandler: function(options, handler) {
        return new Promise(function(resolve, reject) {
            if (cordova.platformId === 'android') {
                exec(function(result) {
                    // Registra il gestore localmente per iOS
                    if ('mediaSession' in navigator) {
                        navigator.mediaSession.setActionHandler(options.action, handler);
                    }
                    resolve(result);
                }, reject, 'MediaSession', 'setActionHandler', [options]);
            } else if ('mediaSession' in navigator) {
                navigator.mediaSession.setActionHandler(options.action, handler);
                resolve();
            } else {
                reject('Media Session API not available');
            }
        });
    },
    
    setPositionState: function(options) {
        return new Promise(function(resolve, reject) {
            if (cordova.platformId === 'android') {
                exec(resolve, reject, 'MediaSession', 'setPositionState', [options]);
            } else if ('mediaSession' in navigator) {
                navigator.mediaSession.setPositionState(options);
                resolve();
            } else {
                reject('Media Session API not available');
            }
        });
    }
};

module.exports = MediaSession;