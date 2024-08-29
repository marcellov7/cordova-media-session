package com.marcellov7;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.net.Uri;
import android.os.Bundle;

public class BackgroundMediaPlugin extends CordovaPlugin {

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private CallbackContext eventCallback;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaBrowser != null && mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (mediaBrowser == null) {
            initMediaBrowser();
        }

        switch (action) {
            case "play":
                play(args, callbackContext);
                return true;
            case "pause":
                pause(callbackContext);
                return true;
            case "stop":
                stop(callbackContext);
                return true;
            case "setVolume":
                setVolume(args.getDouble(0), callbackContext);
                return true;
            case "setPlaybackTime":
                setPlaybackTime(args.getLong(0), callbackContext);
                return true;
            case "registerEventCallback":
                this.eventCallback = callbackContext;
                return true;
            case "destroy":
                destroy(callbackContext);
                return true;
        }
        return false;
    }

    private void initMediaBrowser() {
        mediaBrowser = new MediaBrowserCompat(
            cordova.getActivity(),
            new ComponentName(cordova.getActivity(), BackgroundMediaService.class),
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        mediaController = new MediaControllerCompat(cordova.getActivity(), mediaBrowser.getSessionToken());
                        mediaController.registerCallback(new MediaControllerCompat.Callback() {
                            @Override
                            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                                if (eventCallback != null) {
                                    String eventName = "";
                                    switch (state.getState()) {
                                        case PlaybackStateCompat.STATE_PLAYING:
                                            eventName = "onPlaybackStart";
                                            break;
                                        case PlaybackStateCompat.STATE_PAUSED:
                                        case PlaybackStateCompat.STATE_STOPPED:
                                            eventName = "onPlaybackEnd";
                                            break;
                                    }
                                    PluginResult result = new PluginResult(PluginResult.Status.OK, eventName);
                                    result.setKeepCallback(true);
                                    eventCallback.sendPluginResult(result);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },
            null
        );
        mediaBrowser.connect();
    }

    private void play(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String url = args.getString(0);
        String title = args.getString(1);
        String artist = args.getString(2);
        String artwork = args.getString(3);
        boolean isVideo = args.getBoolean(4);
        long startTime = args.getLong(5);

        if (mediaController != null) {
            Bundle extras = new Bundle();
            extras.putString("title", title);
            extras.putString("artist", artist);
            extras.putString("artwork", artwork);
            extras.putBoolean("isVideo", isVideo);
            extras.putLong("startTime", startTime);

            mediaController.getTransportControls().playFromUri(Uri.parse(url), extras);
            callbackContext.success("Playback started");
        } else {
            callbackContext.error("Media controller not initialized");
        }
    }

    private void pause(CallbackContext callbackContext) {
        if (mediaController != null) {
            mediaController.getTransportControls().pause();
            callbackContext.success("Playback paused");
        } else {
            callbackContext.error("Media controller not initialized");
        }
    }

    private void stop(CallbackContext callbackContext) {
        if (mediaController != null) {
            mediaController.getTransportControls().stop();
            callbackContext.success("Playback stopped");
        } else {
            callbackContext.error("Media controller not initialized");
        }
    }

    private void setVolume(double volume, CallbackContext callbackContext) {
        if (mediaController != null) {
            Bundle extras = new Bundle();
            extras.putFloat("volume", (float)volume);
            mediaController.getTransportControls().sendCustomAction("setVolume", extras);
            callbackContext.success("Volume set");
        } else {
            callbackContext.error("Media controller not initialized");
        }
    }

    private void setPlaybackTime(long time, CallbackContext callbackContext) {
        if (mediaController != null) {
            mediaController.getTransportControls().seekTo(time);
            callbackContext.success("Playback time set");
        } else {
            callbackContext.error("Media controller not initialized");
        }
    }

    private void destroy(CallbackContext callbackContext) {
        if (mediaBrowser != null && mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
        }
        if (mediaController != null) {
            mediaController.unregisterCallback(mediaControllerCallback);
        }
        callbackContext.success("Plugin destroyed");
    }

    private MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            // Handle playback state changes
        }
    };
}