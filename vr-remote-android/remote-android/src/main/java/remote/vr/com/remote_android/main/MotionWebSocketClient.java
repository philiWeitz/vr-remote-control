package remote.vr.com.remote_android.main;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MotionWebSocketClient extends WebSocketListener {

    private static final String TAG = "VR-REMOTE";

    private static final int WEBSOCKET_RECONNECT_TIME_MS = 3000;
    private static final String DEFAULT_WEBSOCKET_URL = "ws://192.168.43.135:8080";

    private static final int NORMAL_CLOSURE_STATUS = 1000;

    private static MotionWebSocketClient sMotionWebSocketClient;


    public static MotionWebSocketClient instance() {
        if(null == sMotionWebSocketClient) {
            sMotionWebSocketClient = new MotionWebSocketClient();
        }
        return sMotionWebSocketClient;
    }


    private WebSocket ws;
    private Activity mActivity;
    private OkHttpClient mClient;
    private String mWebSocketUrl = DEFAULT_WEBSOCKET_URL;



    public MotionWebSocketClient() {
        mClient = new OkHttpClient();
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void start() {
        Request request = new Request.Builder().url(mWebSocketUrl).build();
        ws = mClient.newWebSocket(request, this);
    }

    public void stop() {
        if(ws != null) {
            ws.close(NORMAL_CLOSURE_STATUS, "Shutdown");
        }
    }

    public void setWebSocketUrl(String url) {
        this.mWebSocketUrl = url;
        Log.d(TAG, "Websocket url changed to: " + url);
    }

    public void sendMessage(String message) {
        if(null != ws) {
            ws.send(message);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.d(TAG, "Websocket opened");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d(TAG, "Websocket received: " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Log.d(TAG, "Websocket received: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "Websocket closing: " + code + ", " + reason);
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.w(TAG, "Motion Websocket connection error: " + t.getMessage());

        ws.cancel();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.w(TAG, "Reconnecting to motion server");
                        start();
                    }
                }, WEBSOCKET_RECONNECT_TIME_MS);
            }
        });
    }
}
