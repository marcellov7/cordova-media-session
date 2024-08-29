package com.marcellov7.cordova.mediasession;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Base64;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MediaSessionPlugin extends CordovaPlugin {
    private static final String TAG = "MediaSessionPlugin";

    private boolean startServiceOnlyDuringPlayback = true;

    private String title = "";
    private String artist = "";
    private String album = "";
    private Bitmap artwork = null;
    private String playbackState = "none";
    private double duration = 0.0;
    private double position = 0.0;
    private double playbackRate = 1.0;
    private final Map<String, CallbackContext> actionHandlers = new HashMap<>();

    private MediaSessionService service = null;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaSessionService.LocalBinder binder = (MediaSessionService.LocalBinder) iBinder;
            service = binder.getService();
            Intent intent = new Intent(cordova.getActivity(), cordova.getActivity().getClass());
            service.connectAndInitialize(MediaSessionPlugin.this, intent);
            updateServiceMetadata();
            updateServicePlaybackState();
            updateServicePositionState();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "Disconnected from MediaSessionService");
        }
    };

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();

        if (!startServiceOnlyDuringPlayback) {
            startMediaService();
        }
    }

    public void startMediaService() {
        Intent intent = new Intent(cordova.getActivity(), MediaSessionService.class);
        ContextCompat.startForegroundService(cordova.getContext(), intent);
        cordova.getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void updateServiceMetadata() {
        if (service != null) {
            service.setTitle(title);
            service.setArtist(artist);
            service.setAlbum(album);
            service.setArtwork(artwork);
            service.update();
        }
    }

    private Bitmap urlToBitmap(String url) throws IOException {
        final boolean blobUrl = url.startsWith("blob:");
        if (blobUrl) {
            Log.i(TAG, "Converting Blob URLs to Bitmap for media artwork is not yet supported");
        }

        final boolean httpUrl = url.startsWith("http");
        if (httpUrl) {
            HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        }

        int base64Index = url.indexOf(";base64,");
        if (base64Index != -1) {
            String base64Data = url.substring(base64Index + 8);
            byte[] decoded = Base64.decode(base64Data, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        }

        return null;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("setMetadata")) {
            this.setMetadata(args.getJSONObject(0), callbackContext);
            return true;
        } else if (action.equals("setPlaybackState")) {
            this.setPlaybackState(args.getJSONObject(0), callbackContext);
            return true;
        } else if (action.equals("setActionHandler")) {
            this.setActionHandler(args.getJSONObject(0), callbackContext);
            return true;
        } else if (action.equals("setPositionState")) {
            this.setPositionState(args.getJSONObject(0), callbackContext);
            return true;
        }
        return false;
    }

    private void setMetadata(JSONObject options, CallbackContext callbackContext) throws JSONException, IOException {
        title = options.optString("title", title);
        artist = options.optString("artist", artist);
        album = options.optString("album", album);

        final JSONArray artworkArray = options.optJSONArray("artwork");
        if (artworkArray != null) {
            final List<JSONObject> artworkList = new ArrayList<>();
            for (int i = 0; i < artworkArray.length(); i++) {
                artworkList.add(artworkArray.getJSONObject(i));
            }
            for (JSONObject artwork : artworkList) {
                String src = artwork.getString("src");
                if (src != null) {
                    this.artwork = urlToBitmap(src);
                }
            }
        }

        if (service != null) { 
            updateServiceMetadata();
        }
        callbackContext.success();
    }

    private void updateServicePlaybackState() {
        if (service != null) {
            int state;
            if (playbackState.equals("playing")) {
                state = PlaybackStateCompat.STATE_PLAYING;
            } else if (playbackState.equals("paused")) {
                state = PlaybackStateCompat.STATE_PAUSED;
            } else {
                state = PlaybackStateCompat.STATE_NONE;
            }
            service.setPlaybackState(state);
            service.update();
        }
    }

    private void setPlaybackState(JSONObject options, CallbackContext callbackContext) throws JSONException {
        playbackState = options.getString("playbackState");

        final boolean playback = playbackState.equals("playing") || playbackState.equals("paused");
        if (startServiceOnlyDuringPlayback && service == null && playback) {
            startMediaService();
        } else if (startServiceOnlyDuringPlayback && service != null && !playback) {
            cordova.getActivity().unbindService(serviceConnection);
            service = null;
        } else if (service != null) {
            updateServicePlaybackState();
        }
        callbackContext.success();
    }

    private void updateServicePositionState() {
        if (service != null) {
            service.setDuration(Math.round(duration * 1000));
            service.setPosition(Math.round(position * 1000));
            float playbackSpeed = playbackRate == 0.0 ? (float) 1.0 : (float) playbackRate;
            service.setPlaybackSpeed(playbackSpeed);
            service.update();
        }
    }

    private void setPositionState(JSONObject options, CallbackContext callbackContext) throws JSONException {
        duration = options.optDouble("duration", duration);
        position = options.optDouble("position", position);
        playbackRate = options.optDouble("playbackRate", playbackRate);

        if (service != null) { 
            updateServicePositionState();
        }
        callbackContext.success();
    }

    private void setActionHandler(JSONObject options, CallbackContext callbackContext) throws JSONException {
        String action = options.getString("action");
        actionHandlers.put(action, callbackContext);

        if (service != null) { 
            service.updatePossibleActions();
        }
    }

    public boolean hasActionHandler(String action) {
        return actionHandlers.containsKey(action);
    }

    public void actionCallback(String action) {
        actionCallback(action, new JSONObject());
    }

    public void actionCallback(String action, JSONObject data) {
        CallbackContext callbackContext = actionHandlers.get(action);
        if (callbackContext != null) {
            try {
                data.put("action", action);
                PluginResult result = new PluginResult(PluginResult.Status.OK, data);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            } catch (JSONException e) {
                Log.e(TAG, "Error creating JSON data for action callback", e);
            }
        } else {
            Log.d(TAG, "No handler for action " + action);
        }
    }

    @Override
    public void onDestroy() {
        if (service != null) {
            cordova.getActivity().unbindService(serviceConnection);
            service = null;
        }
        super.onDestroy();
    }
}