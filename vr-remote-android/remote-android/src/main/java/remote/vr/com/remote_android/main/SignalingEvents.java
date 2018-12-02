package remote.vr.com.remote_android.main;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
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

    private boolean mActivateCamera = false;

    private Handler mFpsHandler = new Handler();
    private double mFramesPer10SecondsCounter = 0;

    private VideoCapturer mCapturer = null;
    private boolean mResolutionSet = false;


    public SignalingEvents(PeerConnectionClient peerConnectionClient, Activity activity, boolean activateCamera) {
        this.mActivity = activity;
        this.mActivateCamera = activateCamera;
        this.mPeerConnectionClient = peerConnectionClient;
    }

    @Override
    public void onConnectedToRoom(AppRTCClient.SignalingParameters params) {
        if(mActivateCamera) {
            mCapturer = createVideoCapturer();
        }

        mPeerConnectionClient.createPeerConnection(
                mVideoSink, mVideoRendererCallbacks, mCapturer, params);

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

        // reset connection after other party has left the call
        CallView.instance().reset();
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

    private VideoCapturer createVideoCapturer() {
        if (null != mActivity) {
            Camera2Enumerator enumerator = new Camera2Enumerator(mActivity);
            final String[] deviceNames = enumerator.getDeviceNames();

            Logging.d(TAG, "Getting front facing camera");
            for (String deviceName : deviceNames) {
                if (enumerator.isFrontFacing(deviceName)) {
                    Logging.d(TAG, "Creating front facing camera capturer.");
                    VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, mCapturerEventHandler);

                    if (videoCapturer != null) {
                        Logging.e(TAG, "Unable to create video capturer");
                    }
                    return videoCapturer;
                }
            }
            Logging.e(TAG, "Unable to find front facing camera");
            return null;
        }
        Logging.e(TAG, "Unable to create video capturer - activity not set");
        return null;
    }


    private CameraVideoCapturer.CameraEventsHandler mCapturerEventHandler = new CameraVideoCapturer.CameraEventsHandler() {
        @Override
        public void onCameraError(String s) {
            Log.e(TAG, "Video capturer error: " + s);
        }

        @Override
        public void onCameraDisconnected() { }

        @Override
        public void onCameraFreezed(String s) { }

        @Override
        public void onCameraOpening(String s) {
            if (!mResolutionSet) {
                mResolutionSet = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCapturer.changeCaptureFormat(800, 600, 30);
                    }
                }, 2000);
            }
        }

        @Override
        public void onFirstFrameAvailable() {

        }

        @Override
        public void onCameraClosed() { }
    };
}
