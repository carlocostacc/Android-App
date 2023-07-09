package com.example.bluetoothtesting;

import static androidx.constraintlayout.widget.StateSet.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.Binder;
import android.util.Log;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private BLEService bleService;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView); // replace with your text view's id

        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bleService = ((BLEService.LocalBinder) service).getService();
            if (!bleService.initialize()) {
                Log.e("MainActivity", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            bleService.connect("55:19:13:45:c6:86"); // replace with your device's address
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bleService = null;
        }
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BLEService.OutputAction.equals(intent.getAction())) {
                final String receivedData = intent.getStringExtra(BLEService.SERIALOUPUT);
                textView.setText(receivedData);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.OutputAction);
        return intentFilter;
    }
}
