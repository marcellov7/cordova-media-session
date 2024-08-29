package com.example;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BackgroundMediaService extends MediaBrowserServiceCompat {

    private static final String CHANNEL_ID = "media_playback_channel";
    private static final int NOTIFICATION_ID = 1;

    private MediaSessionCompat mediaSession;
    private ExoPlayer player;
    private PlayerNotificationManager playerNotificationManager;
    private MediaSessionConnector mediaSessionConnector;
    private Map<String, MediaItem> playlist = new HashMap<>();
    private List<String> playlistOrder = new ArrayList<>();
    private int currentIndex = 0;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        BackgroundMediaService getService() {
            return BackgroundMediaService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        player = new ExoPlayer.Builder(this).build();
        mediaSession = new MediaSessionCompat(this, "BackgroundMediaService");
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(player);

        setSessionToken(mediaSession.getSessionToken());

        playerNotificationManager = new PlayerNotificationManager.Builder(this, NOTIFICATION_ID, CHANNEL_ID)
                .setMediaDescriptionAdapter(new MediaDescriptionAdapter())
                .setNotificationListener(new NotificationListener())
                .build();

        playerNotificationManager.setPlayer(player);
        playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());

        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                if (mediaItem != null) {
                    String trackId = mediaItem.mediaId;
                    notifyTrackChanged(trackId);
                }
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    // Automatically play the next item
                    if (player.hasNextMediaItem()) {
                        player.seekToNext();
                    } else {
                        // End of playlist
                        player.seekTo(0, 0);
                        player.pause();
                    }
                }
            }
        });

        mediaSessionConnector.setQueueNavigator(new MediaSessionConnector.QueueNavigator() {
            @Override
            public long getSupportedQueueNavigatorActions(@NonNull Player player) {
                return PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
            }

            @Override
            public void onSkipToNext(@NonNull Player player) {
                playNext();
            }

            @Override
            public void onSkipToPrevious(@NonNull Player player) {
                playPrevious();
            }

            // Implement other required methods...
        });
    }

    public void setPlaylist(List<MediaItem> newPlaylist) {
        playlist.clear();
        playlistOrder.clear();
        for (MediaItem item : newPlaylist) {
            String id = item.mediaId;
            playlist.put(id, item);
            playlistOrder.add(id);
        }
        updatePlayerPlaylist();
    }

    public void addToPlaylist(MediaItem item) {
        String id = item.mediaId;
        playlist.put(id, item);
        playlistOrder.add(id);
        updatePlayerPlaylist();
    }

    public void removeFromPlaylist(String id) {
        playlist.remove(id);
        playlistOrder.remove(id);
        updatePlayerPlaylist();
    }

    private void updatePlayerPlaylist() {
        List<MediaItem> items = new ArrayList<>();
        for (String id : playlistOrder) {
            items.add(playlist.get(id));
        }
        player.setMediaItems(items);
        player.prepare();
    }

    public void playById(String id) {
        int index = playlistOrder.indexOf(id);
        if (index != -1) {
            player.seekTo(index, 0);
            player.play();
        }
    }

    public void playNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNext();
            player.play();
        }
    }

    public void playPrevious() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPrevious();
            player.play();
        }
    }

    private void notifyTrackChanged(String trackId) {
        if (mediaSession != null) {
            Bundle extras = new Bundle();
            extras.putString("trackId", trackId);
            mediaSession.setExtras(extras);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        mediaSession.release();
        mediaSessionConnector.setPlayer(null);
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
            MediaItem currentItem = player.getCurrentMediaItem();
            return currentItem != null ? currentItem.mediaMetadata.title : "";
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(Player player) {
            MediaItem currentItem = player.getCurrentMediaItem();
            return currentItem != null ? currentItem.mediaMetadata.artist : "";
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
            Intent intent = new Intent(BackgroundMediaService.this, BackgroundMediaPlugin.getCordovaActivity());
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