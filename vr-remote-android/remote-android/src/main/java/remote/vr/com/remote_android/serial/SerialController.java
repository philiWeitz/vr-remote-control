package remote.vr.com.remote_android.serial;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.Map;

public class SerialController {

    private static final String ACTION_USB_PERMISSION = "1";
    private static final String TAG = "VR_REMOTE-SERIAL";
    private static SerialController instance;

    public static SerialController instance() {
        if (null == instance) {
            instance = new SerialController();
        }
        return instance;
    }


    private UsbDeviceConnection mConnection = null;
    private UsbManager mUsbManager = null;
    private UsbDevice mDevice = null;
    private UsbSerialDevice mSerialPort = null;
    private Context mCtx = null;


    private SerialController() {

    }

    public void openDriver(Context ctx) {
        if(null == mSerialPort) {
            mCtx = ctx;
            mUsbManager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);

            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            ctx.registerReceiver(mBroadcastReceiver, filter);

            Map<String, UsbDevice> usbDevices = mUsbManager.getDeviceList();
            if (!usbDevices.isEmpty()) {
                for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                    mDevice = entry.getValue();

                    PendingIntent pi = PendingIntent.getBroadcast(ctx, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);
                    mUsbManager.requestPermission(mDevice, pi);
                }
            }
        }
    }

    public void closeDriver() {
        if(null != mSerialPort) {
            mSerialPort.close();
            mConnection.close();

            mSerialPort = null;
            mConnection = null;
            mDevice = null;
        }
    }

    public void sendData(String data) {
        if(null != mSerialPort) {
            mSerialPort.write(data.getBytes());
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCtx.unregisterReceiver(mBroadcastReceiver);

            if (intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)) {
                mConnection = mUsbManager.openDevice(mDevice);
                mSerialPort = UsbSerialDevice.createUsbSerialDevice(mDevice, mConnection);
                if (mSerialPort != null) {
                    if (mSerialPort.open()) { //Set Serial Connection Parameters.
                        mSerialPort.setBaudRate(9600);
                        mSerialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                        mSerialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                        mSerialPort.setParity(UsbSerialInterface.PARITY_NONE);
                        mSerialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    }
                }
            }
        };
    };
}
