package remote.vr.com.remote_android.main;

import android.util.Log;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;

import remote.vr.com.remote_android.webrtc.AppRTCClient;
import remote.vr.com.remote_android.webrtc.PeerConnectionClient;

class PeerConnectionEvents implements PeerConnectionClient.PeerConnectionEvents {

    private static final String TAG = "VR-REMOTE";

    private AppRTCClient mAppRtcClient;


    public PeerConnectionEvents(AppRTCClient appRtcClient) {
        mAppRtcClient = appRtcClient;
    }

    @Override
    public void onLocalDescription(SessionDescription sdp) {
        if (sdp.type == SessionDescription.Type.OFFER) {
            Log.d(TAG, "Sending an offer...");
            mAppRtcClient.sendOfferSdp(sdp);
        } else {
            Log.d(TAG, "Answering an offer...");
            mAppRtcClient.sendAnswerSdp(sdp);
        }
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        mAppRtcClient.sendLocalIceCandidate(candidate);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        mAppRtcClient.sendLocalIceCandidateRemovals(candidates);
    }

    @Override
    public void onIceConnected() {

    }

    @Override
    public void onIceDisconnected() {

    }

    @Override
    public void onPeerConnectionClosed() {
        Log.d(TAG, "Peer connection closed");
    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {

    }

    @Override
    public void onPeerConnectionError(String description) {

    }
}
