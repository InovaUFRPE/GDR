package gdr.gdrapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by user on 22/01/2018.
 */

public class ConnectThread extends Thread{
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    InputStream inputStream = null;
    private BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    int avilableBytes=0;
    UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    boolean GDR;



    public ConnectThread(String address, Context context) throws IOException {
        preferences = context.getSharedPreferences("config", MODE_PRIVATE);
        editor = preferences.edit();
        InputStream temp = null;
        mmDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(mUUID);
        App.CONTEXT = context;


        try{
            mmSocket.connect();
            if (mmDevice.getName().equals("HC-05") && mmDevice.getAddress().equals("20:17:03:06:04:29")){
                Toast.makeText(App.CONTEXT,"Você está conectado ao GDR",Toast.LENGTH_LONG).show();
                temp = mmSocket.getInputStream();
                editor.putBoolean("GDR",true);
                GDR = true;
                App.GDR = true;
            }
        } catch (IOException e){
            try{
                Method method = mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                mmSocket = (BluetoothSocket)method.invoke(mmDevice, Integer.valueOf(1));
                mmSocket.connect();
                if (mmDevice.getName().equals("HC-05") && mmDevice.getAddress().equals("20:17:03:06:04:29")){
                    Toast.makeText(App.CONTEXT,"Você está conectado ao GDR",Toast.LENGTH_LONG).show();
                    temp = mmSocket.getInputStream();
                    editor.putBoolean("GDR",true);
                    GDR = true;
                    App.GDR = true;
                }
            } catch (Exception ex) {
                throw new IOException(e);
            }
        }
        inputStream = temp;
    }

    public void run() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        try{
            int bytes;
            while (true){
                try{
                    avilableBytes=inputStream.available();
                    final byte[] buffer=new byte[avilableBytes];
                    if (avilableBytes>5){
                        bytes=inputStream.read(buffer);
                        final String readMessage=new String(buffer);
                        if (bytes>=3){
                            App.ACT.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (readMessage.toLowerCase().contains("buraco".toLowerCase())){
                                        Toast.makeText(App.ACT, "buraco detectado", Toast.LENGTH_SHORT).show();
                                    }
                                    if (readMessage.toLowerCase().contains("obstaculo".toLowerCase())){
                                        Toast.makeText(App.ACT, "obstaculo detectado", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else {
                            SystemClock.sleep(100);
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void alertaBateria(){
        Intent rIntent = new Intent(App.CONTEXT,GDRApp.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(App.CONTEXT, 0, rIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification not = new Notification.Builder(App.CONTEXT)
                .setContentTitle("GDRApp")
                .setContentText("Bateria fraca, recarregue o GDR")
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{0, 1000})
                .setSmallIcon(R.mipmap.desconectar)
                .setContentIntent(resultPendingIntent)
                .build();
        not.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager nm = (NotificationManager) App.CONTEXT.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(001,not);
    }


    public void result(String tensao){

        ImageView icon = (ImageView) App.ACT.findViewById(R.id.bateria);
        if(tensao.equals("3.0")){
            //Toast.makeText(App.CONTEXT, "baixa", Toast.LENGTH_SHORT).show();
            icon.setImageResource(R.drawable.vazia);
            icon.setContentDescription("bateria vazia, recarregue");
            alertaBateria();
        } else if (tensao.equals("7.0")){
            //Toast.makeText(App.CONTEXT, "media", Toast.LENGTH_SHORT).show();
            icon.setImageResource(R.drawable.media);
            icon.setContentDescription("bateria na metade");
        } else if (tensao.equals("9.0")){
            //Toast.makeText(App.CONTEXT, "alta", Toast.LENGTH_SHORT).show();
            icon.setImageResource(R.drawable.cheia);
            icon.setContentDescription("bateria cheia");
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
