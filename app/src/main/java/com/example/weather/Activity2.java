package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
    //ez az Activity jeleníti meg részletesebben az adatokat pár textview segítségével
public class Activity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        Intent intent=getIntent();
        TextView name=findViewById(R.id.name2);
        TextView country=findViewById(R.id.country);
        TextView weather=findViewById(R.id.weath2);
        TextView hum=findViewById(R.id.hum2);
        TextView temp=findViewById(R.id.temp2);

        name.setText(intent.getStringExtra("name"));
        country.setText(intent.getStringExtra("country"));
        weather.setText(intent.getStringExtra("weather"));
        hum.setText(intent.getStringExtra("humidity"));
        temp.setText(intent.getStringExtra("temp"));
    }
}
