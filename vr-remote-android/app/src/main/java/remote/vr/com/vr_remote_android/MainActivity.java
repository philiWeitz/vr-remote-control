package remote.vr.com.vr_remote_android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import remote.vr.com.remote_android.PluginClass;
import remote.vr.com.remote_android.main.CallView;
import remote.vr.com.remote_android.main.FrameCallback;
import remote.vr.com.remote_android.main.SharedPreferencesUtil;
import remote.vr.com.remote_android.serial.BleController;

public class MainActivity extends Activity {

    private static final boolean IS_CAMERA_CLIENT = true;
    private static final String STATIC_ROOM_ID = "123456789b1ww";

    private Handler handler = new Handler();
    private ImageView mImageView;
    private ImageView mImageViewSmile;

    private String roomId = "";
    private EditText mRoomEditText;
    private View mRoomEditLayout;
    private boolean mWebRtcRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageViewSmile = (ImageView) findViewById(R.id.imageViewSmile);
        mRoomEditText = (EditText) findViewById(R.id.main_activity_room_input);
        mRoomEditLayout = findViewById(R.id.main_activity_room_input_layout);

        String lastRoomId = SharedPreferencesUtil.getPreference(
                this, SharedPreferencesUtil.KEY_ROOM_ID);
        mRoomEditText.setText(lastRoomId);

        mRoomEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    InputMethodManager imm = (InputMethodManager) MainActivity.this
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mRoomEditText.getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mWebRtcRunning) {
            joinRoom();
        }

        // open connection to the Arduino
        if (IS_CAMERA_CLIENT) {
            // SerialController.instance().openDriver(this);
            BleController.instance().openConnection(this);
        }
    }

    public void onJoinRoomButtonClick(View v) {
        // get room id from input field
        roomId = mRoomEditText.getText().toString();

        if(roomId.isEmpty()) {
            roomId = STATIC_ROOM_ID;
        }
        joinRoom();
    }

    public void onDemoButtonClick(View v) {
        BleController.instance().sendData("DEMO$");
    }

    private void joinRoom() {
        PluginClass.mainActivity = this;
        PluginClass.setupCallView(roomId, IS_CAMERA_CLIENT);
        PluginClass.startCallView();
        PluginClass.connectToMotionWebSocket();

        mRoomEditLayout.setVisibility(View.INVISIBLE);
        mWebRtcRunning = true;

        if (!IS_CAMERA_CLIENT) {
            handler.postDelayed(repeatedTask, 2000);
        } else {
            mImageViewSmile.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        if (mWebRtcRunning) {
            CallView.instance().stop();
            CallView.instance().destroy();
        }

        BleController.instance().disconnect();
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
            //Bitmap bitmap = FrameCallback.instance().getAlpha8Bitmap();
            Bitmap bitmap = FrameCallback.instance().getArgbBitmap();

            if (null != bitmap) {
                mImageView.setImageBitmap(bitmap);
                //mImageView.setColorFilter(mColorFilter);
            }

            handler.postDelayed(repeatedTask, 50);
        }
    };

    public void onSmileClick(View v) {
        mImageViewSmile.setRotation(mImageViewSmile.getRotation() * -1);

        WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        layoutParams.screenBrightness = 0.90f;
    }
}
