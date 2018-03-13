package com.fieldbook.tracker.fieldbookservice;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.app.ActivityManager.RunningServiceInfo;

public class MainActivity extends AppCompatActivity {

    private ListView mEventListView;
    private ListView mMessageListView;
    private static final String TAG = "FieldBookService";
    private Intent serviceIntent;
    private ArrayAdapter<String> mAdapter;
    private DataList mDataList;

    private IntentFilter mIntentFilter;
    private ActivityReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataList = DataList.getInstance();
        serviceIntent = new Intent(this, DataTransferService.class);
        mEventListView = (ListView) findViewById(R.id.eventListView);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.fieldbook.tracker.fieldbookservice.NEWDATA");

        mReceiver = new ActivityReceiver();
        registerReceiver(mReceiver, mIntentFilter);

        mEventListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);

                Log.i(TAG, selectedItem + " click event");

                listViewAction(selectedItem);
            }
        });

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDataList.getList());
        mMessageListView.setAdapter(mAdapter);
    }

    private void listViewAction(String selectedItem) {

        switch (selectedItem) {
            case "Start Service":
                if (isServiceRunning(DataTransferService.class) == true) {
                    Log.i(TAG, "Service is running now");
                } else {
                    startService(serviceIntent);
                }
                break;
            case "Stop Service":
                if (isServiceRunning(DataTransferService.class) == false) {
                    Log.i(TAG, "Service already stop");
                } else {
                    stopService(serviceIntent);
                }
                break;
            /*case "Check Bluetooth Connection Status":

                break;
            case "Re-connect Bluetooth":

                break;*/
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private class ActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String data = intent.getExtras().getString("message");

            mAdapter.notifyDataSetChanged();
        }
    }

}
