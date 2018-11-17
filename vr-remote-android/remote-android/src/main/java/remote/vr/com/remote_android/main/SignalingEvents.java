package remote.vr.com.remote_android.main;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.VideoFrame;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSink;

import remote.vr.com.remote_android.webrtc.AppRTCClient;
import remote.vr.com.remote_android.webrtc.PeerConnectionClient;


class SignalingEvents implements AppRTCClient.SignalingEvents {

    private static final String TAG = "VR-REMOTE";

    private static final int FPS_DELAY_TIME = 10 * 1000;

    private Activity mActivity;
    private PeerConnectionClient mPeerConnectionClient;

    private Handler mFpsHandler = new Handler();
    private double mFramesPer10SecondsCounter = 0;


    public SignalingEvents(PeerConnectionClient peerConnectionClient, Activity activity) {
        this.mActivity = activity;
        this.mPeerConnectionClient = peerConnectionClient;
    }

    @Override
    public void onConnectedToRoom(AppRTCClient.SignalingParameters params) {
        mPeerConnectionClient.createPeerConnection(
                mVideoSink, mVideoRendererCallbacks, null, params);

        if (params.initiator) {
            mPeerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                mPeerConnectionClient.setRemoteDescription(params.offerSdp);
                mPeerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    mPeerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }

        mFpsHandler.postDelayed(LogFps, FPS_DELAY_TIME);
    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {
        mPeerConnectionClient.setRemoteDescription(sdp);
        if (sdp.type != SessionDescription.Type.ANSWER) {
            Log.d(TAG, "Signalling channel create an answer");
            mPeerConnectionClient.createAnswer();
        }
    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {
        mPeerConnectionClient.addRemoteIceCandidate(candidate);
    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {
        mPeerConnectionClient.removeRemoteIceCandidates(candidates);
    }

    @Override
    public void onChannelClose() {
        mFpsHandler.removeCallbacks(LogFps);
        Log.d(TAG, "Signalling channel closed");
    }

    @Override
    public void onChannelError(String description) {
        Log.e(TAG, "Signalling channel error: " + description);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, "Error: " + description, Toast.LENGTH_LONG).show();
            }
        });
    }

    private VideoRenderer.Callbacks mVideoRendererCallbacks = new VideoRenderer.Callbacks() {
        @Override
        public void renderFrame(VideoRenderer.I420Frame i420Frame) {
            ++mFramesPer10SecondsCounter;

            FrameCallback.instance().onFrame(i420Frame);
            VideoRenderer.renderFrameDone(i420Frame);
        }
    };

    private VideoSink mVideoSink = new VideoSink() {
        @Override
        public void onFrame(VideoFrame videoFrame) {
            videoFrame.release();
        }
    };

    private Runnable LogFps = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "FPS: " + (int) (mFramesPer10SecondsCounter / 10.0));
            mFramesPer10SecondsCounter = 0;
            mFpsHandler.postDelayed(this, FPS_DELAY_TIME);
        }
    };
}
