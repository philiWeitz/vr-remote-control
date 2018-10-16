package remote.vr.com.vr_remote_android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import remote.vr.com.remote_android.PluginClass;
import remote.vr.com.remote_android.main.CallView;
import remote.vr.com.remote_android.main.FrameCallback;

public class MainActivity extends Activity {

    private Handler handler = new Handler();
    private ImageView mImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        PluginClass.mainActivity = this;
        PluginClass.setupCallView("123456789abcd");
        PluginClass.startCallView();
        PluginClass.connectToMotionWebSocket();

        handler.postDelayed(repeatedTask, 2000);
    }

    @Override
    protected void onStop() {
        CallView.instance().stop();
        CallView.instance().destroy();

        super.onStop();
    }

    ColorMatrixColorFilter mColorFilter = new ColorMatrixColorFilter(new float[]{
            0, 0, 0, 1, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 255
    });

    private Runnable repeatedTask = new Runnable() {
        @Override
        public void run() {
            //PluginClass.sendMessageToMotionWebSocket("Ping Pong From Android");

            //Bitmap bitmap = FrameCallback.instance().getAlpha8Bitmap();
            Bitmap bitmap = FrameCallback.instance().getArgbBitmap();

            if (null != bitmap) {
                mImageView.setImageBitmap(bitmap);
                //mImageView.setColorFilter(mColorFilter);
            }

            handler.postDelayed(repeatedTask, 50);
        }
    };
}
