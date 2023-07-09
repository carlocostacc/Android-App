package com.example.bluetoothtesting;



import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import java.util.UUID;

public class BLEService extends Service {
    private final IBinder binder = new LocalBinder();
    public static final String SerialPortUUID = "0000dfb1-0000-1000-8000-00805f9b34fb";
    //private int SerialPort=9600;
    //private String SerialPortBuffer = "AT+CURRUART="+SerialPort+"\r\n";
    public final static String SERIALOUPUT = "SerialOutputHere";
    public final static String OutputAction = "Output";
    public ArrayList <Double> temp = new ArrayList< Double > ();
    public double averageValue = 0;

    BluetoothGattCharacteristic SerialPortCharacteristic;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @SuppressLint("MissingPermission")
    public boolean onUnbind(Intent intent) {
        bluetoothGatt.close();
        bluetoothGatt = null;
        return super.onUnbind(intent);
    }

    class LocalBinder extends Binder {
        BLEService getService() {
            return BLEService.this;
        }
    }

    public static final String TAG = "BluetoothLeService";
    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bluetoothAdapter;


    public boolean initialize() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e("ERROR", "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        Log.w("ERROR", "BluetoothAdapter Initialized");
        return true;
    }

    @SuppressLint("MissingPermission")
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address.");
            return false;
        }
        // connect to the GATT server on the device
    }

    @SuppressLint("MissingPermission")
    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Log.w(TAG, "Successfully Connected to Gatt Server");
                if (bluetoothGatt.discoverServices()) {
                    Log.i(TAG, "Starting Service Discovery");
                    for (BluetoothGattService gattService : bluetoothGatt.getServices()) {
                        String uuid = gattService.getUuid().toString();
                        Log.i(TAG, "displayGattServices + uuid=" + uuid);

                        for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                            if (uuid.equals(SerialPortUUID)) {
                                SerialPortCharacteristic = gattCharacteristic;
                                Log.i(TAG, "SerialPortCharacteristic  " + SerialPortCharacteristic.getUuid().toString());
                            }
                        }
                    }

                } else {
                    Log.i(TAG, "Service Discovery Failed");

                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "No GATT Server or wrong device");
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "GATT Success");
            }
            Log.w(TAG, "onServicesDiscovered received: " + status);
            SerialPortCharacteristic = bluetoothGatt.getService(UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb")).getCharacteristic(UUID.fromString(SerialPortUUID)); //For some reason if it doesn't appear in Services
            Log.i(TAG, "Characteristic Connected" + String.valueOf(SerialPortCharacteristic.getUuid()));
            bluetoothGatt.setCharacteristicNotification(SerialPortCharacteristic, true);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            final byte[] SerialRaw = characteristic.getValue();

            if ((SerialRaw!=null)){
                String SerialOutput = new String(characteristic.getValue());


                Intent intent = new Intent(OutputAction);
                intent.putExtra(SERIALOUPUT, SerialOutput);
                sendBroadcast(intent);
                Log.i(TAG, SerialOutput);
            }

        }

        ;


    };
}
