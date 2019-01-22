package remote.vr.com.remote_android.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.UUID;

import remote.vr.com.remote_android.serial.BleController;
import remote.vr.com.remote_android.util.AsyncHttpURLConnection;
import remote.vr.com.remote_android.webrtc.AppRTCClient;
import remote.vr.com.remote_android.webrtc.PeerConnectionClient;
import remote.vr.com.remote_android.webrtc.WebSocketRTCClient;


/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
public class CallView {
    private static final String TAG = "VR-REMOTE";
    private static final String APP_RTC_URL = "https://appr.tc";

    private static CallView sCallView;


    public static CallView instance() {
        if(null == sCallView) {
            sCallView = new CallView();
        }
        return sCallView;
    }


    private Activity mActivity;
    private AppRTCClient mAppRtcClient;
    private SignalingEvents mSignalingEvents = null;
    private PeerConnectionClient mPeerConnectionClient = null;
    private PeerConnectionEvents mPeerConnectionEvents = null;

    private TextView mResolutionTextView = null;

    private String mRoomId = "";
    private boolean mActivateCamera = false;


    public void start() {
        if (mPeerConnectionClient != null) {
            mPeerConnectionClient.startVideoSource();
        }
    }

    public void stop() {
        leaveLastRoom(mActivity);
        //SerialController.instance().closeDriver();
        BleController.instance().disconnect();

        if (mPeerConnectionClient != null) {
            mPeerConnectionClient.stopVideoSource();
            disconnect();
        }
    }

    public void destroy() {
        if(mPeerConnectionClient != null) {
            disconnect();
        }
    }

    public void reset() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stop();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onCreate(mActivity, mRoomId, mActivateCamera);
                        start();
                    }
                }, 3000);
            }
        });
    }

    public void sendMessage(String msg) {
        if (mPeerConnectionClient != null) {
            mPeerConnectionClient.sendDataByChannel(msg);
        }
    }


    public void setResolution(String resolution) {
        if(null != mResolutionTextView) {
            this.mResolutionTextView.setText(resolution);
        }
    }


    public void leaveLastRoom(Context ctx) {
        String lastRoomId = SharedPreferencesUtil.getPreference(
                ctx, SharedPreferencesUtil.KEY_ROOM_ID);
        String lastClientId = SharedPreferencesUtil.getPreference(
                ctx, SharedPreferencesUtil.KEY_CLIENT_ID);

        String postURL = "https://appr.tc/leave/" + lastRoomId + "/" + lastClientId;

        if(lastRoomId != null && lastClientId != null) {
            AsyncHttpURLConnection httpConnection =
                    new AsyncHttpURLConnection("POST", postURL, "", new AsyncHttpURLConnection.AsyncHttpEvents() {
                        @Override
                        public void onHttpError(String e) {
                            Log.e(TAG, "Error while leaving last room: " + e);
                        }

                        @Override
                        public void onHttpComplete(String response) {
                            Log.d(TAG, "Left previous room");
                        }
                    });
            httpConnection.send();
        }
    }


    public void onCreate(Activity activity, String roomId, boolean activateCamera) {
        this.mActivity = activity;
        this.mRoomId = roomId;
        this.mActivateCamera = activateCamera;

        // if it is camera client => open USB connection
        if(activateCamera) {
            // SerialController.instance().openDriver(activity);
            BleController.instance().openConnection(activity);
        }

        // leave the previous room before starting a new connection
        leaveLastRoom(activity);

        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) activity
                .findViewById(android.R.id.content)).getChildAt(0);

        TextView textView = new TextView(mActivity);
        mResolutionTextView = new TextView(mActivity);

        viewGroup.addView(textView);
        viewGroup.addView(mResolutionTextView);

        textView.getLayoutParams().width = 300;
        textView.getLayoutParams().height = 100;
        textView.setTextColor(Color.GREEN);

        mResolutionTextView.getLayoutParams().width = 300;
        mResolutionTextView.getLayoutParams().height = 100;
        mResolutionTextView.setTextColor(Color.GREEN);

        if(mResolutionTextView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) mResolutionTextView.getLayoutParams()).leftMargin = 400;
        } else if(mResolutionTextView.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            ((RelativeLayout.LayoutParams) mResolutionTextView.getLayoutParams()).leftMargin = 400;
        }

        mPeerConnectionClient = new PeerConnectionClient();
        mSignalingEvents = new SignalingEvents(mPeerConnectionClient, mActivity, activateCamera);

        String webRtcRoomId = roomId;

        // take the room id or generate a random room string
        if(webRtcRoomId == null || webRtcRoomId.isEmpty()) {
            webRtcRoomId = UUID.randomUUID().toString().toLowerCase().substring(0, 7);
        }

        textView.setText(webRtcRoomId);
        Log.d(TAG, "Room ID: " + webRtcRoomId);

        connectToRoom(webRtcRoomId);
    }


    private void connectToRoom(String roomId) {
        // create the RTC client
        mAppRtcClient = new WebSocketRTCClient(mSignalingEvents, mActivity);
        mPeerConnectionEvents = new PeerConnectionEvents(mAppRtcClient);

        mPeerConnectionClient.createPeerConnectionFactory(
                mActivity.getApplicationContext(),
                getPeerParameter(), mPeerConnectionEvents);

        Uri roomUri = Uri.parse(APP_RTC_URL);

        AppRTCClient.RoomConnectionParameters roomConnectionParameters =
                new AppRTCClient.RoomConnectionParameters(
                        roomUri.toString(),
                        roomId,
                        false,
                        null);

        mAppRtcClient.connectToRoom(roomConnectionParameters);
    }


    private void disconnect() {
        if (mAppRtcClient != null) {
            mAppRtcClient.disconnectFromRoom();
            mAppRtcClient = null;
        }
        if (mPeerConnectionClient != null) {
            mPeerConnectionClient.close();
            mPeerConnectionClient = null;
        }
    }


    private PeerConnectionClient.PeerConnectionParameters getPeerParameter() {
        PeerConnectionClient.DataChannelParameters dataChannelParameters =
            new PeerConnectionClient.DataChannelParameters(
                true,
                -1,
                -1,
                "",
                false,
                -1
        );

        return new PeerConnectionClient.PeerConnectionParameters(true,
                false,
                false,
                0,
                0,
                0,
                1700,
                PeerConnectionClient.VIDEO_CODEC_VP8,
                false,
                false,
                32,
                "ISAC",
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                dataChannelParameters);
    }
}
