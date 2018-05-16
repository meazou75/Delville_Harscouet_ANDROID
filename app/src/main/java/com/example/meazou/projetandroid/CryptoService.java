package com.example.meazou.projetandroid;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.ContentValues.TAG;

public class CryptoService extends IntentService {

    private static final String ACTION_CRYPTO = "com.example.meazou.projetandroid.action.CRYPTO";

    public CryptoService() {
        super("CryptoService");
    }

    public static void startActionCrypto(Context context) {
        Intent intent = new Intent(context, CryptoService.class);
        intent.setAction(ACTION_CRYPTO);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CRYPTO.equals(action)) {
                handleActionCrypto();
            }
        }
    }

    private void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf,0,len);
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleActionCrypto() {
        Log.d(TAG, "Thread service name:" + Thread.currentThread().getName());
        URL url = null;
        try {
            url = new URL("https://api.coinmarketcap.com/v2/ticker/?convert=EUR&limit=20");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                copyInputStreamToFile(conn.getInputStream(),
                        new File(getCacheDir(), "crypto.json"));
                Log.d(TAG, "CRYPTO JSON downloaded");
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(MainActivity.CRYPTO_UPDATE));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
