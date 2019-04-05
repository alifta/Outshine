package ca.mun.outshine.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import ca.mun.outshine.sqlite.DbHelper;

public class WifiService extends IntentService {

    private static final String TAG = "WifiService";
    private boolean mLogging = true; //TODO change to false later

    // wifiManager
    private WifiManager wifiManager;

    // database
    private DbHelper db; // TODO fix DB leak -> close DB after using it

    public WifiService() {
        // name the worker thread for debugging.
        super("Wifi-Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // optional logging
        if (mLogging) {
            Log.d(TAG, "Service is running");
        }

        // access with DB
        db = new DbHelper(this);

        // initialize wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // turn on the wifi, if disabled
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        // register wifi broadcast receiver and scan for the first time
        // registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        List<ScanResult> wifiScanList = wifiManager.getScanResults();

        if (wifiScanList != null && wifiScanList.size() > 0) {
            Log.d(TAG, "#WiFi-Networks: " + String.valueOf(wifiScanList.size()));
            for (ScanResult scanResult : wifiScanList) {
                Log.d(TAG, scanResult.SSID);
                if (!db.existBssid(scanResult.BSSID)) {
                    Log.d(TAG, "DB: " + String.valueOf(db.existBssid(scanResult.BSSID)));
                    db.addAp(scanResult.SSID,scanResult.BSSID);
                }
            }
        }

        // send new data for broadcasting
        // intent.setAction(Constants.ACTION_WIFI);
        // put extras into the intent
        // intent.putExtra(Constants.KEY_WIFI, result);
        // fire the broadcast with intent packaged
        // sendBroadcast(intent);

        // on the BOOT version:
        // release the wake lock provided by the WakefulBroadcastReceiver.
        // WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

}
