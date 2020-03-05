package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {



    class City { //City osztály,amely egybegyűjti egy város adatait

        String name;
        String temp;
        String humidity;
        String country;
        String weather;
    }

    private List<City> cities=new ArrayList<City>() ; //ebben vannak a városok

    RecyclerView recyclerView;
    MyRecyclerViewAdapter adapter;
    Context context;
    EditText editText;
    private Handler async_handler=new Handler();
    private ProgressBar progressBar_async;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar_async=findViewById(R.id.progressBar);
        String[] list_of_strings= new String[]{ //ezek a városok szerepelnek az appban
                "London","Berlin","Las Vegas","Miami","Tokyo","Budapest","Wien","Madrid","Moskva",
                "Rome", "Beijing","Seoul","Sydney","Melbourne","Johannesburg","Buenos Aires",
                "Rio de Janeiro","Dallas","Ottawa","Liverpool", "Manchester","Bangkok","Mumbai",
                "Lagos","Helsinki","Portland","Chicago","New Orleans","Boston","Munich"};
        editText=findViewById(R.id.input); //kereső
        TextView tv_internet_info=findViewById(R.id.internet_info); //internet hiányát jelző szövegdoboz
        recyclerView = findViewById(R.id.recview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, cities);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        //ha van internetkapcsolat akkor elindítja az adatokat lekérdező Task-ot
        if (isNetworkConnected()) {
            new JsonTask().execute(list_of_strings);
            tv_internet_info.setVisibility(View.GONE);
        }
        else { //ha nincs internet, akkor pedig kiírja, hogy nincs
            tv_internet_info.setText(R.string.nointernet);
        }

        EditText searchField=findViewById(R.id.input);
        searchField.addTextChangedListener(new TextWatcher() { //keresés megvalósítása
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString()); //megszűri a cities listát a beírt értéknek megfelelően
            }
        });

    }

    void filter(String text){
        List<City> temp = new ArrayList();
        for(City d: cities){
            //ha a bemeneti szövege tartalmazza valamelyik listabeli elemet, akkor azt hozzáadja
            //egy ideiglenes listához, amit átad a recyclerview adapterének
            if(d.name.contains(text) || d.name.toLowerCase().contains(text)){
                temp.add(d);
            }
        }
        //update recyclerview
        adapter.updateList(temp);
    }



    @Override
    public void onItemClick(View view, int position) {
        //ha rákattintunk valamelyik listaelemre, akkor elindít egy új activityt, ahol
        //részletesebben írja le a város időjárását
        Intent intent=new Intent(this,Activity2.class);
        intent.putExtra("name",adapter.getItem(position).name);
        intent.putExtra("country",adapter.getItem(position).country);
        intent.putExtra("humidity",adapter.getItem(position).humidity);
        intent.putExtra("temp",adapter.getItem(position).temp);
        intent.putExtra("weather",adapter.getItem(position).weather);
        startActivity(intent);

    }
    //az aktuális időjárás adatokat lekérdező task
    private class JsonTask extends AsyncTask<String[], Integer, Void>  {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(String[]... params) {
            for (int i=0;i<params[0].length;i++) {
                //hozzáadja a listához a lekért város adatait
                cities.add(getdatas(params[0][i]));
                final int finalI = i;
                //frissíti a progressbar-t a lekért városok számának függvényében
                async_handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar_async.setProgress((int) (finalI *4));
                    }
                });
            }

            return null;
        }
        //lekéri az adatokat az API-ról
        private City getdatas(String city) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url=new URL("https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid=1d6944e190740f950c0dd1825a643b68");

                Log.i("url",url.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);
                }
                JSONObject json = new JSONObject(buffer.toString());

                City act_city = new City();
                act_city.temp = json.getJSONObject("main").getString("temp");
                act_city.temp=String.valueOf((int)(Double.parseDouble(act_city.temp)-272.15));
                act_city.temp+=" °C";
                act_city.name = json.get("name").toString();
                act_city.humidity=json.getJSONObject("main").getString("humidity");
                act_city.country=json.getJSONObject("sys").getString("country");
                act_city.weather= json.getJSONArray("weather").getJSONObject(0).getString("description");


                return act_city;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

           // Log.i("result",result.name);
            //ha lekérte az összes adatot, akkor frissíti az recyclerviewt
            adapter.notifyDataSetChanged();



        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

}
