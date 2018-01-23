package gdr.gdrapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class GDRApp extends AppCompatActivity {
    BluetoothAdapter meuBluetooth = BluetoothAdapter.getDefaultAdapter();
    ImageButton BTstatus;
    ImageView bateria;
    ImageButton config;
    ImageButton manual;
    TextView texto;
    ProgressDialog loading;
    static final int VISIBILITY_REQUEST = 1;
    ArrayList<BluetoothDevice> encontrados;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdrapp);
        App.CONTEXT = this.getApplicationContext();
        App.ACT = this;
        configBotoes();
        setGDR(false);
    }




    public void setGDR(boolean GDR){
        SharedPreferences.Editor editor = getSharedPreferences("config",MODE_PRIVATE).edit();
        editor.putBoolean("GDR",GDR);
        editor.commit();
    }

    // Essa função é chamada sempre que:
    // - o dispositivo detecta outro disponivel
    // - quando acaba a busca
    // - quando a conexão com o GDR acaba
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
                loading.dismiss();
                listaDispositivos();
            }else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                if (state == BluetoothAdapter.STATE_DISCONNECTED) {

                }
            }
        }
    };


    //sempre que o App fica em segundo plano e vc abre ele novamente
    @Override
    public void onResume(){
        super.onResume();
        if (meuBluetooth.isEnabled()){
            BTstatus.setContentDescription("Clique para desligar bluetooth, clique e segure para pesquisar dispositivos");
            BTstatus.setBackgroundResource(R.drawable.bton);
        } else {
            BTstatus.setContentDescription("Clique para ligar bluetooth e achar o GDR");
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
        BTstatus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(meuBluetooth.enable()){
                    procurar();
                    return true;
                }
                return false;
            }
        });
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
                final AlertDialog.Builder dialog1 = new AlertDialog.Builder(GDRApp.this);
                dialog1.setCustomTitle(getLayoutInflater().inflate(R.layout.btn_share,null));
                dialog1.create().show();
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
            BTstatus.setContentDescription("Clique para ligar bluetooth e achar o GDR");
            BTstatus.setBackgroundResource(R.drawable.btoff);
        }
    }

    //chamado quando vc confirma o pedido de visibilidade
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VISIBILITY_REQUEST && resultCode == RESULT_FIRST_USER) {
            meuBluetooth.enable();
            BTstatus.setContentDescription("Clique para desligar bluetooth, clique e segure para pesquisar dispositivos");
            BTstatus.setBackgroundResource(R.drawable.bton);
            procurar();
        }
    }

    public void procurar(){
        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receptor, bluetoothFilter);
        encontrados = new ArrayList<>();
        loading = ProgressDialog.show(this,"","Procurando dispositivos...",false,false);
        meuBluetooth.startDiscovery();
    }



    //pega a lista de dispositivos encontrados e mostra no AlertDialog
    public void listaDispositivos(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (encontrados.size()== 0){
            builder.setMessage("Nenhum dispositivo encontrado");
        }else {
            CharSequence[] x = new CharSequence[encontrados.size()];
            for(int i = 0; i<encontrados.size();i++){
                x[i] = encontrados.get(i).getName()+"\n";
            }
            final CharSequence[] disp = x;
            builder.setTitle("Dispositivos encontrados");
            builder.setItems(disp, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, final int selecionado) {
                    try {
                        conecta(selecionado); //quando a pessoa escolhe ele pega o indice do selecionado na lista de encontrados
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
        builder.setPositiveButton("Procurar novamente", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                procurar();
            }
        });
        builder.create().show();
    }

    //20:17:03:06:04:29
    //Inicia a conexão em segundo plano
    public void conecta(int i) throws IOException {
        BluetoothDevice GDR = encontrados.get(i);
        Intent intent = new Intent(getBaseContext(),BluetoothService.class);
        intent.putExtra("device", GDR.getAddress());
        startService(intent);

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