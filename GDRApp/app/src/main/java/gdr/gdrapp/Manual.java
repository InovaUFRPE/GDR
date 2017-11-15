package gdr.gdrapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Manual extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, GDRApp.class));
        finish();
    }
}
