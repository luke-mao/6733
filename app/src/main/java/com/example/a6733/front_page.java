package com.example.a6733;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;
import android.os.Bundle;

public class front_page
    extends AppCompatActivity
    implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.front_page);

        findViewById(R.id.client).setOnClickListener(this);
        findViewById(R.id.server).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        if (v.getId() == R.id.client){
            Intent intent = new Intent();
            intent.setClass(this, client.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.server){
            Intent intent = new Intent();
            intent.setClass(this, server.class);
            startActivity(intent);
        }
    }
}
