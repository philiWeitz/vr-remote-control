package remote.vr.com.remote_android.main;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.UUID;

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


    public void start() {
        if (mPeerConnectionClient != null) {
            mPeerConnectionClient.startVideoSource();
        }
    }

    public void stop() {
        if (mPeerConnectionClient != null) {
            mPeerConnectionClient.stopVideoSource();
        }
    }

    public void destroy() {
        if(mPeerConnectionClient != null) {
            disconnect();
        }
    }


    public void onCreate(Activity activity, String roomId) {
        this.mActivity = activity;

        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) activity
                .findViewById(android.R.id.content)).getChildAt(0);

        TextView textView = new TextView(mActivity);
        viewGroup.addView(textView);

        textView.getLayoutParams().width = 300;
        textView.getLayoutParams().height = 100;
        textView.setTextColor(Color.GREEN);

        mPeerConnectionClient = new PeerConnectionClient();
        mSignalingEvents = new SignalingEvents(mPeerConnectionClient, mActivity);

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
        mAppRtcClient = new WebSocketRTCClient(mSignalingEvents);
        mPeerConnectionEvents = new PeerConnectionEvents(mAppRtcClient);

        mPeerConnectionClient.createPeerConnectionFactory(
                mActivity.getApplicationContext(), getPeerParameter(), mPeerConnectionEvents);

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
                "OPUS",
                true,
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
