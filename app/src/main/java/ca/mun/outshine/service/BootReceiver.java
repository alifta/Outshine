package ca.mun.outshine.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

// WakefulBroadcastReceiver ensures the device does not go back to sleep
// during the startup of the service and needs two following permissions
// <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
// <uses-permission android:name="android.permission.WAKE_LOCK" />
public class BootReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            // Launch the specified service when this message is received
            Intent startServiceIntent = new Intent(context, WifiService.class);
            startWakefulService(context, startServiceIntent);
        }
    }

}

// enabling the receiver
//ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
//    PackageManager pm = context.getPackageManager();
//
//pm.setComponentEnabledSetting(receiver,
//        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//        PackageManager.DONT_KILL_APP);

// disabling the receiver
//ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
//    PackageManager pm = context.getPackageManager();
//
//pm.setComponentEnabledSetting(receiver,
//        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//        PackageManager.DONT_KILL_APP);