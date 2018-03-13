package com.fieldbook.tracker.fieldbookservice;

/**
 * Created by jessica on 3/9/18.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DataTransferService extends Service {

    private BluetoothServer mBluetoothServer;
    private ActivityReceiver mActivityReceiver;
    private IntentFilter mIntentFilter;
    private Intent mIntentSender;
    private DataList mDataList;

    @Override
    public void onCreate() {

        mBluetoothServer = new BluetoothServer();
        mActivityReceiver = new ActivityReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.fieldbook.tracker.utilities.DATA_CHANGE");
        registerReceiver(mActivityReceiver, mIntentFilter);
        mBluetoothServer.init();
        mIntentSender = new Intent();
        mIntentSender.setAction("com.fieldbook.tracker.fieldbookservice.NEWDATA");
        mDataList = DataList.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mActivityReceiver);
        mBluetoothServer.cancelConnection();
        super.onDestroy();
    }

    private class ActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("Receiver", "Broadcast received: " + action);

            String data = intent.getExtras().getString("data");
            Log.i("Receiver", data);
            mBluetoothServer.write(data.getBytes());
            mDataList.addData(data);
            mIntentSender.putExtra("message", data);
            sendBroadcast(mIntentSender);
        }
    }
}
