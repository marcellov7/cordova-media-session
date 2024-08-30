package com.example.cordovamediasession;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.app.NotificationCompat;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MediaSession extends CordovaPlugin {

    private static final String CHANNEL_ID = "MediaSessionChannel";
    private static final int NOTIFICATION_ID = 1;

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

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
        
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
            }

            @Override
            public void onPause() {
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
            }

            @Override
            public void onSkipToNext() {
                // Handle next track
            }

            @Override
            public void onSkipToPrevious() {
                // Handle previous track
            }
        });
        
        mediaSession.setActive(true);
        
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Media playback", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        
        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        switch (action) {
            case "setMetadata":
                this.setMetadata(args.getJSONObject(0), callbackContext);
                return true;
            case "setPlaybackState":
                this.setPlaybackState(args.getJSONObject(0), callbackContext);
                return true;
            case "setActionHandler":
                this.setActionHandler(args.getString(0), callbackContext);
                return true;
            case "clearActionHandler":
                this.clearActionHandler(args.getString(0), callbackContext);
                return true;
        }
        return false;
    }

    private void setMetadata(JSONObject metadata, CallbackContext callbackContext) throws JSONException {
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
        MediaMetadataCompat mediaMetadata = metadataBuilder.build();
        mediaSession.setMetadata(mediaMetadata);
        updateNotification(mediaMetadata);
        callbackContext.success();
    }

    private void setPlaybackState(JSONObject state, CallbackContext callbackContext) throws JSONException {
        String stateString = state.getString("state");
        int playbackState = stateString.equals("playing") ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState(playbackState);
        callbackContext.success();
    }

    private void setActionHandler(String action, CallbackContext callbackContext) {
        // Action handlers are managed by MediaSession callback
        callbackContext.success();
    }

    private void clearActionHandler(String action, CallbackContext callbackContext) {
        // Action handlers are managed by MediaSession callback
        callbackContext.success();
    }

    private void updatePlaybackState(int state) {
        PlaybackStateCompat playbackState = stateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f).build();
        mediaSession.setPlaybackState(playbackState);
        updateNotification(null);
    }

    private void updateNotification(MediaMetadataCompat metadata) {
        if (metadata == null) {
            metadata = mediaSession.getController().getMetadata();
        }
        
        if (metadata == null) {
            return;  // Can't update notification without metadata
        }

        notificationBuilder
                .setContentTitle(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .setContentText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                .setSubText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
                .setSmallIcon(cordova.getActivity().getApplicationInfo().icon)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        PlaybackStateCompat playbackState = mediaSession.getController().getPlaybackState();
        if (playbackState != null) {
            if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                notificationBuilder.addAction(android.R.drawable.ic_media_pause, "Pause", createActionIntent("pause"));
            } else {
                notificationBuilder.addAction(android.R.drawable.ic_media_play, "Play", createActionIntent("play"));
            }
        }

        notificationBuilder.addAction(android.R.drawable.ic_media_previous, "Previous", createActionIntent("previous"));
        notificationBuilder.addAction(android.R.drawable.ic_media_next, "Next", createActionIntent("next"));

        Notification notification = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private PendingIntent createActionIntent(String action) {
        Intent intent = new Intent(cordova.getActivity(), MediaButtonReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(cordova.getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static MediaSessionCompat getMediaSession() {
        return mediaSession;
    }
}