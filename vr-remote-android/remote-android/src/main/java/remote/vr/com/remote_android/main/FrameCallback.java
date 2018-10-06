package remote.vr.com.remote_android.main;

import android.graphics.Bitmap;
import android.util.Log;

import org.webrtc.VideoRenderer;

import java.nio.ByteBuffer;

public class FrameCallback {

    private static final String TAG = "VR-REMOTE";


    public static FrameCallback sFrameCallback;

    private FrameCallback() {

    }

    public static FrameCallback instance() {
        if(null == sFrameCallback) {
            sFrameCallback = new FrameCallback();
        }
        return sFrameCallback;
    }


    private int mWidth;
    private int mHeight;
    private byte[] mAlpha8ByteArray = null;
    private boolean mIsNewFrame = true;

    private Bitmap mBitmap;


    public synchronized Bitmap getAlpha8Bitmap() {
        if(!mIsNewFrame) {
            return mBitmap;
        }

        if(mAlpha8ByteArray == null) {
            return null;
        }
        if(mBitmap == null || mBitmap.getHeight() != mHeight || mBitmap.getWidth() != mWidth) {
            if(mBitmap != null) {
                mBitmap.recycle();
            }
            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ALPHA_8);
        }

        mBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(mAlpha8ByteArray));

        return mBitmap;
    }


    public synchronized void onFrame(VideoRenderer.I420Frame frame) {
        Log.v(TAG, "on Frame Received");


        if(frame.yuvPlanes == null) {
            Log.w(TAG, "no yuv planes detected");
            return;
        }

        ByteBuffer yBuffer = frame.yuvPlanes[0];


        if(yBuffer == null || yBuffer.limit() <= 0) {
            return;
        }

        yBuffer.rewind();

        if(mAlpha8ByteArray == null || mAlpha8ByteArray.length != yBuffer.remaining()) {
            mAlpha8ByteArray = new byte[yBuffer.remaining()];
        }

        mWidth = frame.width;
        mHeight = frame.height;
        yBuffer.get(mAlpha8ByteArray);
        mIsNewFrame = true;

        yBuffer.rewind();
    }


    public boolean isNewFrame() {
        return mIsNewFrame;
    }


    public void setIsNewFrame(boolean isNewFrame) {
        mIsNewFrame = isNewFrame;
    }
}
