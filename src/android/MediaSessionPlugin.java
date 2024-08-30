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
        if (url.startsWith("http") || url.startsWith("https")) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            connection.disconnect();
            return bitmap;
        } else if (url.startsWith("data:image")) {
            String base64Data = url.substring(url.indexOf(",") + 1);
            byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        return null;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("setMetadata")) {
            try {
                this.setMetadata(args.getJSONObject(0), callbackContext);
            } catch (IOException e) {
                callbackContext.error("Error setting metadata: " + e.getMessage());
            }
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
        if (artworkArray != null && artworkArray.length() > 0) {
            JSONObject artworkObj = artworkArray.getJSONObject(0);
            String src = artworkObj.optString("src");
            if (src != null) {
                artwork = urlToBitmap(src);
            }
        }

        if (service != null) { 
            updateServiceMetadata();
        }
        callbackContext.success();
    }

    private void setPlaybackState(JSONObject options, CallbackContext callbackContext) throws JSONException {
        playbackState = options.getString("playbackState");

        int state;
        switch (playbackState) {
            case "playing":
                state = PlaybackStateCompat.STATE_PLAYING;
                break;
            case "paused":
                state = PlaybackStateCompat.STATE_PAUSED;
                break;
            default:
                state = PlaybackStateCompat.STATE_NONE;
        }

        final boolean playback = playbackState.equals("playing") || playbackState.equals("paused");
        if (startServiceOnlyDuringPlayback && service == null && playback) {
            startMediaService();
        } else if (startServiceOnlyDuringPlayback && service != null && !playback) {
            cordova.getActivity().unbindService(serviceConnection);
            service = null;
        } else if (service != null) {
            service.setPlaybackState(state);
            service.update();
        }
        callbackContext.success();
    }

    private void setPositionState(JSONObject options, CallbackContext callbackContext) throws JSONException {
        duration = options.optDouble("duration", duration);
        position = options.optDouble("position", position);
        playbackRate = options.optDouble("playbackRate", playbackRate);

        if (service != null) { 
            service.setDuration(Math.round(duration * 1000));
            service.setPosition(Math.round(position * 1000));
            service.setPlaybackSpeed((float) playbackRate);
            service.update();
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