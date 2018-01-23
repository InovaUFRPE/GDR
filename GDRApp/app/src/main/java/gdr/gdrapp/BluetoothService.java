package gdr.gdrapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by user on 06/12/2017.
 */

public class BluetoothService extends android.app.Service {
    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("tag", "onStartCommand");
        String address = intent.getStringExtra("device");
        try {
            ConnectThread ct = new ConnectThread(address,context);
            ct.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        return;
    }
}
