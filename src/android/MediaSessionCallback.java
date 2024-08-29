package com.example;

import android.support.v4.media.session.MediaSessionCompat;
import org.json.JSONObject;

public class MediaSessionCallback extends MediaSessionCompat.Callback {
    private static final String TAG = "MediaSessionCallback";

    private final MediaSessionPlugin plugin;

    MediaSessionCallback(MediaSessionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlay() {
        plugin.notifyActionHandler("play", null);
    }

    @Override
    public void onPause() {
        plugin.notifyActionHandler("pause", null);
    }

    @Override
    public void onSeekTo(long pos) {
        JSONObject data = new JSONObject();
        try {
            data.put("seekTime", (double) pos / 1000.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        plugin.notifyActionHandler("seekto", data);
    }

    @Override
    public void onRewind() {
        plugin.notifyActionHandler("seekbackward", null);
    }

    @Override
    public void onFastForward() {
        plugin.notifyActionHandler("seekforward", null);
    }

    @Override
    public void onSkipToPrevious() {
        plugin.notifyActionHandler("previoustrack", null);
    }

    @Override
    public void onSkipToNext() {
        plugin.notifyActionHandler("nexttrack", null);
    }

    @Override
    public void onStop() {
        plugin.notifyActionHandler("stop", null);
    }
}