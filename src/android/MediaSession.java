package com.example.cordovamediasession;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public class MediaSession extends CordovaPlugin {

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;

    @Override
    protected void pluginInitialize() {
        Context context = cordova.getActivity().getApplicationContext();
        mediaSession = new MediaSessionCompat(context, "CordovaMediaSession");
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setActive(true);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("setMetadata")) {
            this.setMetadata(args.getJSONObject(0));
            return true;
        } else if (action.equals("setPlaybackState")) {
            this.setPlaybackState(args.getJSONObject(0));
            return true;
        } else if (action.equals("setActionHandler")) {
            this.setActionHandler(args.getString(0), callbackContext);
            return true;
        } else if (action.equals("clearActionHandler")) {
            this.clearActionHandler(args.getString(0));
            return true;
        }
        return false;
    }

    private void setMetadata(JSONObject metadata) throws JSONException {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        if (metadata.has("title")) {
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadata.getString("title"));
        }
        if (metadata.has("artist")) {
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadata.getString("artist"));
        }
        if (metadata.has("album")) {
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadata.getString("album"));
        }
        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void setPlaybackState(JSONObject state) throws JSONException {
        int playbackState = state.getInt("state");
        long position = state.getLong("position");
        float speed = (float) state.getDouble("speed");

        stateBuilder.setState(playbackState, position, speed);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void setActionHandler(String action, CallbackContext callbackContext) {
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                if (action.equals("play")) {
                    callbackContext.success();
                }
            }

            @Override
            public void onPause() {
                if (action.equals("pause")) {
                    callbackContext.success();
                }
            }

            @Override
            public void onSkipToNext() {
                if (action.equals("nexttrack")) {
                    callbackContext.success();
                }
            }

            @Override
            public void onSkipToPrevious() {
                if (action.equals("previoustrack")) {
                    callbackContext.success();
                }
            }
        });
    }

    private void clearActionHandler(String action) {
        mediaSession.setCallback(null);
    }
}
