var exec = require('cordova/exec');

var MediaSession = {
    isNative: function() {
        return cordova.platformId === 'android';
    },

    setMetadata: function(options) {
        if (this.isNative()) {
            return new Promise(function(resolve, reject) {
                exec(resolve, reject, 'MediaSession', 'setMetadata', [options]);
            });
        } else {
            if ('mediaSession' in navigator) {
                navigator.mediaSession.metadata = new MediaMetadata({
                    title: options.title,
                    artist: options.artist,
                    album: options.album,
                    artwork: options.artwork
                });
            }
            return Promise.resolve();
        }
    },
    
    setPlaybackState: function(options) {
        if (this.isNative()) {
            return new Promise(function(resolve, reject) {
                exec(resolve, reject, 'MediaSession', 'setPlaybackState', [options]);
            });
        } else {
            if ('mediaSession' in navigator) {
                navigator.mediaSession.playbackState = options.playbackState;
            }
            return Promise.resolve();
        }
    },
    
    setActionHandler: function(options, handler) {
        if (this.isNative()) {
            var success = function(result) {
                if (typeof handler === 'function') {
                    handler(result);
                }
            };
            return new Promise(function(resolve, reject) {
                exec(success, reject, 'MediaSession', 'setActionHandler', [options]);
                resolve();
            });
        } else {
            if ('mediaSession' in navigator) {
                navigator.mediaSession.setActionHandler(options.action, handler);
            }
            return Promise.resolve();
        }
    },
    
    setPositionState: function(options) {
        if (this.isNative()) {
            return new Promise(function(resolve, reject) {
                exec(resolve, reject, 'MediaSession', 'setPositionState', [options]);
            });
        } else {
            if ('mediaSession' in navigator) {
                navigator.mediaSession.setPositionState({
                    duration: options.duration,
                    playbackRate: options.playbackRate,
                    position: options.position
                });
            }
            return Promise.resolve();
        }
    }
};

module.exports = MediaSession;