package com.example;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.util.ArrayList;
import java.util.List;

public class BackgroundMediaPlugin extends CordovaPlugin {

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private CallbackContext eventCallback;
    private static android.app.Activity cordovaActivity;
    private BackgroundMediaService mediaService;
    private boolean serviceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundMediaService.LocalBinder binder = (BackgroundMediaService.LocalBinder) service;
            mediaService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaBrowser != null && mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
        }
        if (serviceBound) {
            cordova.getActivity().unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        cordovaActivity = cordova.getActivity();
        Intent intent = new Intent(cordova.getActivity(), BackgroundMediaService.class);
        cordova.getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static android.app.Activity getCordovaActivity() {
        return cordovaActivity;
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
            case "setPlaylist":
                setPlaylist(args, callbackContext);
                return true;
            case "addToPlaylist":
                addToPlaylist(args, callbackContext);
                return true;
            case "removeFromPlaylist":
                removeFromPlaylist(args, callbackContext);
                return true;
            case "playById":
                playById(args, callbackContext);
                return true;
            case "playNext":
                playNext(callbackContext);
                return true;
            case "playPrevious":
                playPrevious(callbackContext);
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

                            @Override
                            public void onExtrasChanged(Bundle extras) {
                                if (extras != null && extras.containsKey("trackId")) {
                                    String trackId = extras.getString("trackId");
                                    if (eventCallback != null) {
                                        PluginResult result = new PluginResult(PluginResult.Status.OK, "onTrackChanged:" + trackId);
                                        result.setKeepCallback(true);
                                        eventCallback.sendPluginResult(result);
                                    }
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

    private void setPlaylist(JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (!serviceBound) {
            callbackContext.error("Service not bound");
            return;
        }

        JSONArray playlistArray = args.getJSONArray(0);
        List<MediaItem> playlist = new ArrayList<>();
        for (int i = 0; i < playlistArray.length(); i++) {
            JSONObject track = playlistArray.getJSONObject(i);
            String id = track.getString("id");
            String url = track.getString("url");
            String title = track.getString("title");
            String artist = track.getString("artist");
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(Uri.parse(url))
                    .setMediaId(id)
                    .setMediaMetadata(new MediaMetadata.Builder()
                            .setTitle(title)
                            .setArtist(artist)
                            .build())
                    .build();
            playlist.add(mediaItem);
        }
        
        mediaService.setPlaylist(playlist);
        callbackContext.success("Playlist set");
    }

    private void addToPlaylist(JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (!serviceBound) {
            callbackContext.error("Service not bound");
            return;
        }

        JSONObject track = args.getJSONObject(0);
        String id = track.getString("id");
        String url = track.getString("url");
        String title = track.getString("title");
        String artist = track.getString("artist");
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(Uri.parse(url))
                .setMediaId(id)
                .setMediaMetadata(new MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(artist)
                        .build())
                .build();
        
        mediaService.addToPlaylist(mediaItem);
        callbackContext.success("Track added to playlist");
    }

    private void removeFromPlaylist(JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (!serviceBound) {
            callbackContext.error("Service not bound");
            return;
        }

        String id = args.getString(0);
        mediaService.removeFromPlaylist(id);
        callbackContext.success("Track removed from playlist");
    }

    private void playById(JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (!serviceBound) {
            callbackContext.error("Service not bound");
            return;
        }

        String id = args.getString(0);
        mediaService.playById(id);
        callbackContext.success("Playing track by ID");
    }

    private void playNext(CallbackContext callbackContext) {
        if (!serviceBound) {
            callbackContext.error("Service not bound");
            return;
        }

        mediaService.playNext();
        callbackContext.success("Playing next track");
    }

    private void playPrevious(CallbackContext callbackContext) {
        if (!serviceBound) {
            callbackContext.error("Service not bound");
            return;
        }

        mediaService.playPrevious();
        callbackContext.success("Playing previous track");
    }

    private void destroy(CallbackContext callbackContext) {
        if (mediaBrowser != null && mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
        }
        if (mediaController != null) {
            mediaController.unregisterCallback(mediaControllerCallback);
        }
        if (serviceBound) {
            cordova.getActivity().unbindService(serviceConnection);
            serviceBound = false;
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