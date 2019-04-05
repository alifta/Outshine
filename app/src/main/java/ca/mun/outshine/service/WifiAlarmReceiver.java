package ca.mun.outshine.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class WifiAlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "WifiAlarm";
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.codepath.example.servicesdemo.alarm";

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("TAG", "WifiAlarmReceiver triggered");

        Intent i = new Intent(context, WifiService.class);
        i.putExtra("foo", "alarm!!");

//        ResultReceiver receiver = intent.getParcelableExtra("receiver");
//        i.putExtra("receiver", receiver);

        context.startService(i);

        Toast.makeText(context, "Welcome --------1", Toast.LENGTH_LONG).show();

    }
}
