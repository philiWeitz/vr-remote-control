package remote.vr.com.remote_android.serial;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class BleController {

    private static final String TAG = "VR-REMOTE_BLE";
    private static BleController instance;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String BLE_RW_SERIVCE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";


    public static BleController instance() {
        if (null == instance) {
            instance = new BleController();
        }
        return instance;
    }


    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBleScanner;
    private Activity mActivity;
    private BluetoothDevice mDevice;
    private BluetoothGatt mBleGatt;
    private BluetoothGattCharacteristic mCharacteristic;

    private BleController() {

    }


    public void sendData(String data) {
        if(null != mBleGatt && null != mCharacteristic) {
            mCharacteristic.setValue(data);
            mBleGatt.writeCharacteristic(mCharacteristic);
        }
    }


    public void disconnect() {
        BleController.instance().sendData("HEAD,90,90$");

        if (mBleGatt != null) {
            mBleGatt.disconnect();
        }
        if (mBleScanner != null) {
            mBleScanner.stopScan(mScanCallback);
        }
        mBleGatt = null;
        mDevice = null;
        mBleScanner = null;
        mCharacteristic = null;
    }

    public void openConnection(Activity activity) {
        if (mCharacteristic != null) {
            return;
        }

        mActivity = activity;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (mBluetoothAdapter != null) {
            mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mBleScanner.startScan(mScanCallback);

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity,"Start scanning...", Toast.LENGTH_SHORT).show();
                    writeDebugMovementToBle();
                }
            });
        }
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (mDevice == null && null != result.getDevice() && "HMSoft".equals(result.getDevice().getName())) {
                mDevice = result.getDevice();
                mBleScanner.stopScan(mScanCallback);
                mBleGatt = mDevice.connectGatt(mActivity, false, mGattCallback);
            }
        }
    };


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBleGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "BLE Disconnected");

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity,
                                "BLE Disconnected", Toast.LENGTH_LONG).show();

                        BleController.instance().openConnection(mActivity);
                    }
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for(BluetoothGattService service: gatt.getServices()) {
                    if(service.getUuid().toString().equals(BLE_RW_SERIVCE_UUID)) {
                        mCharacteristic = service.getCharacteristics().get(0);

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity,"Connected to BLE", Toast.LENGTH_SHORT).show();
                                writeDebugMovementToBle();
                            }
                        });
                    }
                }
            } else {
                Log.d(TAG, "Failed to discover BLE services");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }
    };

    private void writeDebugMovementToBle() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BleController.instance().sendData("HEAD,40,40$");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BleController.instance().sendData("HEAD,120,120$");
                    }
                }, 1000);
            }
        }, 2000);
    }
}
