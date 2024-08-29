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
    destroy: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BackgroundMedia", "destroy", []);
    },
    onEvent: function(callback) {
        exec(callback, null, "BackgroundMedia", "registerEventCallback", []);
    }
};

module.exports = BackgroundMedia;