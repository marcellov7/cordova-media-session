var exec = require('cordova/exec');

var BackgroundMedia = {
    play: function(url, title, artist, artwork, isVideo, startTime, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "play", [url, title, artist, artwork, isVideo, startTime]);
    },
    pause: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "pause", []);
    },
    stop: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "stop", []);
    },
    setVolume: function(volume, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "setVolume", [volume]);
    },
    setPlaybackTime: function(time, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "setPlaybackTime", [time]);
    },
    setPlaylist: function(playlist, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "setPlaylist", [playlist]);
    },
    addToPlaylist: function(track, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "addToPlaylist", [track]);
    },
    removeFromPlaylist: function(id, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "removeFromPlaylist", [id]);
    },
    playById: function(id, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "playById", [id]);
    },
    playNext: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "playNext", []);
    },
    playPrevious: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "playPrevious", []);
    },
    onEvent: function(callback) {
        exec(function(event) {
            if (typeof event === 'string' && event.startsWith("onTrackChanged:")) {
                var trackId = event.split(":")[1];
                callback({type: "onTrackChanged", trackId: trackId});
            } else {
                callback({type: event});
            }
        }, null, "BackgroundMedia", "registerEventCallback", []);
    },
    destroy: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "destroy", []);
    }
};

module.exports = BackgroundMedia;