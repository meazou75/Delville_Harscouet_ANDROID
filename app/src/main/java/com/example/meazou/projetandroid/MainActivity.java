package com.example.meazou.projetandroid;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

/*
@Author : DELVILLE Francois
*/

public class MainActivity extends AppCompatActivity {

    private ProgressBar loadingBar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.about)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Nothing
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter(CRYPTO_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(new CryptoUpdate(), intentFilter);

        setContentView(R.layout.activity_main);

        CryptoService.startActionCrypto(MainActivity.this);

        loadingBar = findViewById(R.id.progressBar1);

        RecyclerView mRecyclerView = findViewById(R.id.rv_crypto);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        CryptoAdapter mAdapter = new CryptoAdapter(getCryptoFromFile());
        mRecyclerView.setAdapter(mAdapter);

    }

    public static final String CRYPTO_UPDATE = "com.example.meazou.projetandroid.CRYPTO_UPDATE";

    public class CryptoUpdate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(),R.string.fetchSuccess, Toast.LENGTH_LONG).show();
            loadingBar.setVisibility(View.INVISIBLE);
        }
    }

    public class CryptoAdapter extends RecyclerView.Adapter<CryptoAdapter.CryptoHolder> {

        private ArrayList<JSONObject> myArray;

        CryptoAdapter(ArrayList<JSONObject> myArray) {
            this.myArray = myArray;
        }

        @Override
        public CryptoHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_crypto_element, parent, false);

            CryptoHolder ph = new CryptoHolder(v);
            return ph;
        }

        @Override
        public void onBindViewHolder(CryptoHolder holder, final int position) {
            try {
                holder.name.setText(myArray.get(position).getString("name"));
                holder.symbol.setText(myArray.get(position).getString("symbol"));
                holder.button.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {
                        try {
                            Intent myIntent = new Intent(MainActivity.this, CryptoDescription.class);
                            myIntent.putExtra("id", myArray.get(position).getInt("id"));
                            startActivity(myIntent);
                            Toast.makeText(getApplicationContext(),myArray.get(position).getString("name") , Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return myArray.size();
        }

        class CryptoHolder extends RecyclerView.ViewHolder {

            TextView name;
            TextView symbol;
            Button button;

            CryptoHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.rv_crypto_element_name);
                symbol = itemView.findViewById(R.id.rv_crypto_element_symbol);
                button = itemView.findViewById(R.id.rv_crypto_element_button);
            }
        }
    }

    public ArrayList<JSONObject> getCryptoFromFile() {
        try {
            InputStream is = new FileInputStream(getCacheDir() + "/" + "crypto.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            JSONObject obj = new JSONObject(new String(buffer, "UTF-8"));

            obj = obj.getJSONObject("data");

            Iterator<String> keys = obj.keys();

            ArrayList<JSONObject> arrayOfObject = new ArrayList<>();

            while (keys.hasNext()) {
                arrayOfObject.add(obj.getJSONObject(keys.next()));
            }

            return arrayOfObject;


        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
