package gdr.gdrapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

public class GDRApp extends AppCompatActivity {
    BluetoothAdapter meuBluetooth = BluetoothAdapter.getDefaultAdapter();
    ImageButton BTstatus;
    ImageButton bateria;
    ImageButton config;
    ImageButton manual;
    TextView texto;
    BluetoothDevice mDevice=null;
    BluetoothSocket mSocket=null;
    BluetoothServerSocket server;
    int i;
    UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    static final int VISIBILITY_REQUEST = 1;
    static final int CONEXAO_REQUEST = 2;
    ArrayList<BluetoothDevice> encontrados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdrapp);
        configBotoes();
    }



    //função é chamada sempre que o dispositivo detecta outro disponivel
    private final BroadcastReceiver receptor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (encontrados.contains(device)) {
                    return;
                }
                encontrados.add(device);
            }else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {

                listaDispositivos();
            }
        }
    };


    //sempre que o app fica em segundo plano e vc abre ele novamente
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

    //configura os botoes e atribui ações a cada um deles
    public void configBotoes(){
        BTstatus = findViewById(R.id.OnOff);
        bateria = findViewById(R.id.bateria);
        config = findViewById(R.id.configuracao);
        manual = findViewById(R.id.manual);
        texto = findViewById(R.id.Pbateria);
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

    //faz o jogo de icones e descrição entre o bluetooth ligado e desligado
    public void setBT(){
        if (!meuBluetooth.isEnabled()){
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            this.startActivityForResult(discoverableIntent, VISIBILITY_REQUEST);
        } else {
            meuBluetooth.disable();
            BTstatus.setContentDescription("ligar bluetooth");
            BTstatus.setBackgroundResource(R.drawable.btoff);
        }
    }

    //chamado quando vc confirma o pedido de visibilidade
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VISIBILITY_REQUEST && resultCode == RESULT_FIRST_USER) {
            meuBluetooth.enable();
            IntentFilter bluetoothFilter = new IntentFilter();
            bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
            bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receptor, bluetoothFilter);
            encontrados = new ArrayList<>();
            BTstatus.setContentDescription("desligar bluetooth");
            BTstatus.setBackgroundResource(R.drawable.bton);
            meuBluetooth.startDiscovery();
        }
    }

    //pega a lista de dispositivos encontrados e mostra no AlertDialog
    public void listaDispositivos(){
        CharSequence[] x = new CharSequence[encontrados.size()];
        for(int i = 0; i<encontrados.size();i++){
            x[i] = encontrados.get(i).getName()+"\n"+encontrados.get(i).getAddress();
        }
        final CharSequence[] disp = x;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dispositivos encontrados");
        builder.setCancelable(false);
        builder.setItems(disp, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, final int selecionado) {
                try {
                    conecta(selecionado); //quando a pessoa escolhe ele pega o indice do selecionado na lista de encontrados
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setPositiveButton("Procurar novamente", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                meuBluetooth.startDiscovery();
            }
        });
        builder.create().show();
    }

    //Chamado quando vc escolhe o dispositivo que quer se conectar
    public void conecta(int i) throws IOException {
        BluetoothDevice GDR = encontrados.get(i);
        mSocket = GDR.createInsecureRfcommSocketToServiceRecord(mUUID);
        try{
            mSocket.connect();
        } catch (IOException e){
            try{
                Method method = GDR.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                mSocket = (BluetoothSocket)method.invoke(GDR, Integer.valueOf(1));
                mSocket.connect();
            } catch (Exception ex) {
                throw new IOException(e);
            }
        }
        //mSocket.connect();


    }

    @Override
    public void onPause() {
        try{
            if(receptor != null) {
                this.unregisterReceiver(receptor);
            }
        } catch (Exception e){
        }
        super.onPause();
    }

}