package remote.vr.com.remote_android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import remote.vr.com.remote_android.main.CallView;
import remote.vr.com.remote_android.main.FrameCallback;
import remote.vr.com.remote_android.main.MotionWebSocketClient;


public class PluginClass {

    private static final String TAG = "VR-REMOTE";

    public static Activity mainActivity = null;

    final static int[] textureHandle = new int[1];


    public static class TextureResult {
        public int width;
        public int height;
        public int texturePtr;
    }


    public static String getTextFromPlugin(int number) {
        return "Android " + number + " - " + (mainActivity != null);
    }

    public static void setupCallView() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Setup Call View...");
                CallView.instance().onCreate(mainActivity);
            }
        });
    }

    public static void startCallView() {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Start Call View...");
                CallView.instance().start();
            }
        });
    }

    public static void connectToMotionWebSocket() {
        MotionWebSocketClient.instance().setActivity(mainActivity);
        MotionWebSocketClient.instance().start();
    }

    public static void sendMessageToMotionWebSocket(String message) {
        MotionWebSocketClient.instance().sendMessage(message);
    }


    public static void setWebSocketUrl(String url) {
        MotionWebSocketClient.instance().setWebSocketUrl(url);
    }

    // get a test texture pointer
    public static int getTexturePointer() {
        Log.d(TAG, "Generate external texture");

        // generate test bitmap
        Bitmap bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.GREEN);

        return bitmapToTexture(bmp);
    }

    // get a test texture pointer modified by a render script
    public static TextureResult getTextureResult() {
        Log.d(TAG, "Generate texture result");

        Bitmap bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.RED);

        //bmp = RenderScriptUtil.yuvToRgba(bmp, mainActivity);
        int texturePointer = bitmapToTexture(bmp);

        TextureResult result = new TextureResult();
        result.texturePtr = texturePointer;
        result.width = bmp.getWidth();
        result.height = bmp.getHeight();

        return result;
    }

    // get a Alpha8 texture pointer
    public static TextureResult getAlpha8TextureResult() {
        Log.v(TAG, "Generate Alpha 8 texture result");

        TextureResult result = new TextureResult();
        Bitmap bmp = FrameCallback.instance().getAlpha8Bitmap();

        if(bmp == null) {
            result.texturePtr = 0;
            result.width = 0;
            result.height = 0;

        } else {
            int texturePointer = bitmapToTexture(bmp);
            result.texturePtr = texturePointer;
            result.width = bmp.getWidth();
            result.height = bmp.getHeight();
        }

        FrameCallback.instance().setIsNewFrame(false);

        return result;
    }

    // write bitmap to texture
    private static int bitmapToTexture(Bitmap bmp) {
        if(!FrameCallback.instance().isNewFrame()) {
            return textureHandle[0];
        }

        if (textureHandle[0] == 0) {
            GLES20.glGenTextures(1, textureHandle, 0);
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0, bmp, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        Log.d(TAG, "Texture id: " + textureHandle[0]);
        return textureHandle[0];
    }
}