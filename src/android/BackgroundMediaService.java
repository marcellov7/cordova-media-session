package com.marcellov7;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.List;

public class BackgroundMediaService extends MediaBrowserServiceCompat {

    private static final String CHANNEL_ID = "media_playback_channel";
    private static final int NOTIFICATION_ID = 1;

    private MediaSessionCompat mediaSession;
    private ExoPlayer player;
    private PlayerNotificationManager playerNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        player = new ExoPlayer.Builder(this).build();
        mediaSession = new MediaSessionCompat(this, "BackgroundMediaService");
        MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(player);

        setSessionToken(mediaSession.getSessionToken());

        playerNotificationManager = new PlayerNotificationManager.Builder(this, NOTIFICATION_ID, CHANNEL_ID)
                .setMediaDescriptionAdapter(new MediaDescriptionAdapter())
                .setNotificationListener(new NotificationListener())
                .build();

        playerNotificationManager.setPlayer(player);
        playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());
    }

    @Override
    public void onDestroy() {
        mediaSession.release();
        player.release();
        playerNotificationManager.setPlayer(null);
        super.onDestroy();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    private class MediaDescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {
        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return player.getCurrentMediaItem().playbackProperties.tag.toString();
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            return null;
        }

        @Nullable
        @Override
        public CharSequence getCurrentSubText(Player player) {
            return null;
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            return null;
        }

        @Nullable
        @Override
        public android.app.PendingIntent createCurrentContentIntent(Player player) {
            Intent intent = new Intent(BackgroundMediaService.this, cordova.getActivity().getClass());
            return android.app.PendingIntent.getActivity(BackgroundMediaService.this, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private class NotificationListener implements PlayerNotificationManager.NotificationListener {
        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            stopForeground(true);
            stopSelf();
        }

        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            startForeground(notificationId, notification);
        }
    }
}