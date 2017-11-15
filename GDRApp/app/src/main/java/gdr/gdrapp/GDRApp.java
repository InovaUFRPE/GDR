package gdr.gdrapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;
import java.util.UUID;

public class GDRApp extends AppCompatActivity {
    BluetoothAdapter meuBluetooth = BluetoothAdapter.getDefaultAdapter();
    ImageButton BTstatus;
    ImageButton bateria;
    ImageButton config;
    ImageButton manual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdrapp);
        configBotoes();

    }




    @Override
    public void onResume(){
        super.onResume();
        if (meuBluetooth.isEnabled()){
            BTstatus.setContentDescription("desconectar");
            BTstatus.setBackgroundResource(R.drawable.bton);
        } else {
            BTstatus.setContentDescription("conectar");
            BTstatus.setBackgroundResource(R.drawable.btoff);
        }
    }


    public void configBotoes(){
        BTstatus = findViewById(R.id.OnOff);
        bateria = findViewById(R.id.bateria);
        config = findViewById(R.id.configuracao);
        manual = findViewById(R.id.manual);

        BTstatus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setBT();
            }
        });
        manual.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(GDRApp.this, Manual.class));
                finish();
            }
        });
        config.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(GDRApp.this, Manual.class));
                finish();
            }
        });

    }

    public void setBT(){
        if (!meuBluetooth.isEnabled()){
            meuBluetooth.enable();
            BTstatus.setContentDescription("desconectar");
            BTstatus.setBackgroundResource(R.drawable.bton);
        } else {
            meuBluetooth.disable();
            BTstatus.setContentDescription("conectar");
            BTstatus.setBackgroundResource(R.drawable.btoff);
        }
    }


}