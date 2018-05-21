package com.example.meazou.projetandroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
@Author : DELVILLE Francois
*/

public class CryptoDescription extends AppCompatActivity {

    TextView cryptoDescriptionName;
    TextView cryptoDescriptionSymbol;
    TextView cryptoDescriptionRank;
    TextView cryptoDescriptionWeekChanged;
    TextView cryptoDescriptionDailyChanged;
    TextView cryptoDescriptionHourChanged;
    TextView cryptoDescriptionPrice;

    ProgressBar cryptoProgressBar;

    Button cryptoBackButton;
    Button cryptoBuyButton;

    LinearLayout mainLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(CryptoDescription.this);
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
        setContentView(R.layout.activity_crypto_description);

        cryptoProgressBar = findViewById(R.id.progressBar2);

        cryptoDescriptionName = findViewById(R.id.rv_crypto_description_name);
        cryptoDescriptionSymbol = findViewById(R.id.rv_crypto_description_symbol);
        cryptoDescriptionRank = findViewById(R.id.rv_crypto_description_rank);
        cryptoDescriptionWeekChanged = findViewById(R.id.rv_crypto_description_weekChanged);
        cryptoDescriptionDailyChanged = findViewById(R.id.rv_crypto_description_dailyChanged);
        cryptoDescriptionHourChanged = findViewById(R.id.rv_crypto_description_hourChanged);
        cryptoDescriptionPrice = findViewById(R.id.rv_crypto_description_price);
        mainLayout = findViewById(R.id.rv_crypto_description_layout);
        cryptoBackButton = findViewById(R.id.buttonBack);
        cryptoBuyButton = findViewById(R.id.buttonBuy);

        cryptoBackButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(CryptoDescription.this);
                Notification notification = builder.setSmallIcon(android.R.drawable.btn_dialog)
                        .setContentTitle(getString(R.string.notifTitle))
                        .setContentText(getString(R.string.notifContent))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build();

                manager.notify(0, notification);
                CryptoDescription.this.onBackPressed();
            }
        });

        cryptoBuyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.coinbase.com/"));
                startActivity(browserIntent);
            }
        });

        mainLayout.setVisibility(View.GONE);

        Integer cryptoId = getIntent().getIntExtra("id", 0);

        new getCryptoDescriptionTask().execute("https://api.coinmarketcap.com/v2/ticker/" + cryptoId + "/?convert=EUR");

    }


    public void Refresh(JSONObject main) {
        try {
            this.cryptoDescriptionName.setText(String.valueOf(main.getString("name")));
            this.cryptoDescriptionRank.setText(String.valueOf(main.getInt("rank")));
            this.cryptoDescriptionSymbol.setText(String.valueOf(main.getString("symbol")));

            JSONObject quotes = main.getJSONObject("quotes").getJSONObject("EUR");

            double hourRatio = quotes.getDouble("percent_change_1h");
            double dayRatio = quotes.getDouble("percent_change_24h");
            double weekRatio = quotes.getDouble("percent_change_7d");

            this.cryptoDescriptionPrice.setText(String.format("%s €", String.valueOf(quotes.getDouble("price"))));

            this.cryptoDescriptionDailyChanged.setText(String.format("%s%%", String.valueOf(dayRatio)));
            this.cryptoDescriptionHourChanged.setText(String.format("%s%%", String.valueOf(hourRatio)));
            this.cryptoDescriptionWeekChanged.setText(String.format("%s%%", String.valueOf(weekRatio)));

            if (hourRatio < 0) {
                this.cryptoDescriptionHourChanged.setTextColor(Color.RED);
            } else {
                this.cryptoDescriptionHourChanged.setTextColor(Color.GREEN);
            }
            if (dayRatio < 0) {
                this.cryptoDescriptionDailyChanged.setTextColor(Color.RED);
            } else {
                this.cryptoDescriptionDailyChanged.setTextColor(Color.GREEN);
            }
            if (weekRatio < 0) {
                this.cryptoDescriptionWeekChanged.setTextColor(Color.RED);
            } else {
                this.cryptoDescriptionWeekChanged.setTextColor(Color.GREEN);
            }

            mainLayout.setVisibility(View.VISIBLE);
            cryptoProgressBar.setVisibility(View.GONE);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class getCryptoDescriptionTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject main;
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    builder.append(inputString);
                }

                JSONObject topLevel = new JSONObject(builder.toString());
                main = topLevel.getJSONObject("data");

                urlConnection.disconnect();
            } catch (IOException | JSONException e) {
                main = new JSONObject();
                e.printStackTrace();
            }
            return main;
        }

        @Override
        protected void onPostExecute(JSONObject main) {
            Refresh(main);
        }
    }
}
