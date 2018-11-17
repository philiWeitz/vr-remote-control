package remote.vr.com.remote_android.main;

import android.graphics.Bitmap;
import android.util.Log;

import org.webrtc.VideoRenderer;

import java.nio.ByteBuffer;

import remote.vr.com.remote_android.util.PerformanceLogger;

public class FrameCallback {

    private static final String TAG = "VR-REMOTE";
    private static final boolean RENDER_ARGB = true;

    public static FrameCallback sFrameCallback;

    private static PerformanceLogger perfLogger = new PerformanceLogger(
        1000, "Performance YUV Frame get Bitmap");


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
    private YuvFrame mYuvFrame;


    public synchronized Bitmap getAlpha8Bitmap() {
        if(!mIsNewFrame) {
            return mBitmap;
        }

        if(mAlpha8ByteArray == null || RENDER_ARGB) {
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


    public synchronized Bitmap getArgbBitmap() {
        if(!mIsNewFrame) {
            return mBitmap;
        }

        if(mYuvFrame == null || !RENDER_ARGB) {
            return null;
        }

        if(mBitmap != null) {
            mBitmap.recycle();
        }

        perfLogger.start();
        mBitmap = mYuvFrame.getBitmap();
        perfLogger.stop();

        return mBitmap;
    }


    public synchronized void onFrame(VideoRenderer.I420Frame frame) {
        Log.v(TAG, "on Frame Received");

        if(frame.yuvPlanes == null) {
            Log.w(TAG, "no yuv planes detected");
            return;
        }

        if(RENDER_ARGB) {
            mYuvFrame = new YuvFrame(frame);

        } else {
            ByteBuffer yBuffer = frame.yuvPlanes[0];


            if (yBuffer == null || yBuffer.limit() <= 0) {
                return;
            }

            yBuffer.rewind();

            if (mAlpha8ByteArray == null || mAlpha8ByteArray.length != yBuffer.remaining()) {
                mAlpha8ByteArray = new byte[yBuffer.remaining()];
            }

            mWidth = frame.width;
            mHeight = frame.height;
            yBuffer.get(mAlpha8ByteArray);

            yBuffer.rewind();
        }
        mIsNewFrame = true;
    }


    public boolean isNewFrame() {
        return mIsNewFrame;
    }


    public void setIsNewFrame(boolean isNewFrame) {
        mIsNewFrame = isNewFrame;
    }
}
