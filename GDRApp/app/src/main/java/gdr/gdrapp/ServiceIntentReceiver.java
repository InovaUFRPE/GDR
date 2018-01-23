package gdr.gdrapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by user on 06/12/2017.
 */

public class ServiceIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        boolean bt = preferences.getBoolean("alertaBT",true);
        boolean bat = preferences.getBoolean("alertaBateria",true);
        boolean gdr = preferences.getBoolean("GDR",true);

        final String action = intent.getAction();
        if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            //if (gdr && bt){
                Intent rIntent = new Intent(context,GDRApp.class);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, rIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification not = new Notification.Builder(context)
                        .setContentTitle("GDRApp")
                        .setContentText("Conexão com o GDR perdida")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[]{0, 1000})
                        .setSmallIcon(R.drawable.bton)
                        .setContentIntent(resultPendingIntent)
                        .build();

                not.flags = Notification.FLAG_AUTO_CANCEL;

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(001,not);


                editor.putBoolean("GDR",false);
            //}
        }
    }

}
