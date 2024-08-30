var exec = require('cordova/exec');

var MediaSession = {
    setMetadata: function(metadata) {
        return new Promise((resolve, reject) => {
            if (cordova.platformId === 'android') {
                exec(resolve, reject, "MediaSession", "setMetadata", [metadata]);
            } else if (cordova.platformId === 'ios') {
                if ('mediaSession' in navigator) {
                    navigator.mediaSession.metadata = new MediaMetadata({
                        title: metadata.title,
                        artist: metadata.artist,
                        album: metadata.album
                    });
                    resolve();
                } else {
                    reject('MediaSession API not supported');
                }
            } else {
                reject('Platform not supported');
            }
        });
    },

    setPlaybackState: function(state) {
        return new Promise((resolve, reject) => {
            if (cordova.platformId === 'android') {
                exec(resolve, reject, "MediaSession", "setPlaybackState", [state]);
            } else if (cordova.platformId === 'ios') {
                if ('mediaSession' in navigator) {
                    navigator.mediaSession.playbackState = state.state;
                    resolve();
                } else {
                    reject('MediaSession API not supported');
                }
            } else {
                reject('Platform not supported');
            }
        });
    },

    setActionHandler: function(action, handler) {
        return new Promise((resolve, reject) => {
            if (cordova.platformId === 'android') {
                if (handler) {
                    exec(handler, null, "MediaSession", "setActionHandler", [action]);
                } else {
                    exec(null, null, "MediaSession", "clearActionHandler", [action]);
                }
                resolve();
            } else if (cordova.platformId === 'ios') {
                if ('mediaSession' in navigator) {
                    navigator.mediaSession.setActionHandler(action, handler);
                    resolve();
                } else {
                    reject('MediaSession API not supported');
                }
            } else {
                reject('Platform not supported');
            }
        });
    }
};

module.exports = MediaSession;