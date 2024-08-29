package com.example;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MediaSessionPlugin extends CordovaPlugin {
    private static final String TAG = "MediaSessionPlugin";

    private MediaSessionService service = null;
    private boolean startServiceOnlyDuringPlayback = true;
    private final Map<String, CallbackContext> actionHandlers = new HashMap<>();

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaSessionService.LocalBinder binder = (MediaSessionService.LocalBinder) iBinder;
            service = binder.getService();
            Intent intent = new Intent(cordova.getActivity(), cordova.getActivity().getClass());
            service.connectAndInitialize(MediaSessionPlugin.this, intent);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "Disconnected from MediaSessionService");
        }
    };

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

    private void setMetadata(JSONObject options, CallbackContext callbackContext) {
        try {
            String title = options.optString("title", "");
            String artist = options.optString("artist", "");
            String album = options.optString("album", "");
            JSONArray artworkArray = options.optJSONArray("artwork");
            Bitmap artwork = null;
            if (artworkArray != null && artworkArray.length() > 0) {
                JSONObject artworkObj = artworkArray.getJSONObject(0);
                String src = artworkObj.optString("src");
                if (src != null) {
                    artwork = urlToBitmap(src);
                }
            }

            if (service != null) {
                service.setTitle(title);
                service.setArtist(artist);
                service.setAlbum(album);
                service.setArtwork(artwork);
                service.update();
            }

            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error("Error setting metadata: " + e.getMessage());
        }
    }

    private void setPlaybackState(JSONObject options, CallbackContext callbackContext) {
        try {
            String playbackState = options.getString("playbackState");
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

            if (service != null) {
                service.setPlaybackState(state);
                service.update();
            }

            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error("Error setting playback state: " + e.getMessage());
        }
    }

    private void setActionHandler(JSONObject options, CallbackContext callbackContext) {
        try {
            String action = options.getString("action");
            actionHandlers.put(action, callbackContext);
            
            if (service != null) {
                service.updatePossibleActions();
            }
        } catch (Exception e) {
            callbackContext.error("Error setting action handler: " + e.getMessage());
        }
    }

    private void setPositionState(JSONObject options, CallbackContext callbackContext) {
        try {
            double duration = options.optDouble("duration", 0);
            double position = options.optDouble("position", 0);
            float playbackRate = (float) options.optDouble("playbackRate", 1.0);

            if (service != null) {
                service.setDuration(Math.round(duration * 1000));
                service.setPosition(Math.round(position * 1000));
                service.setPlaybackSpeed(playbackRate);
                service.update();
            }

            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error("Error setting position state: " + e.getMessage());
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
        } else {
            Log.e(TAG, "Unsupported URL format for artwork");
            return null;
        }
    }

    public void notifyActionHandler(String action, JSONObject data) {
        CallbackContext callbackContext = actionHandlers.get(action);
        if (callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, data);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
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