package kit.pef.vyuka.downapp;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Lukáš Čížek.
 */

// Stahovani pomoci dvou metod.
public class MainActivity extends Activity {
    Button btnDM, btnTR;
    String link = "http://nd04.jxs.cz/426/756/e151d720b2_75706548_o2.jpg";

    // odhadnuti nazvu koncoveho souboru na zaklade nazvu linku pravdepodobne jen zkopirovani nazvu
    String nazevSouboru = URLUtil.guessFileName(link, null,
            MimeTypeMap.getFileExtensionFromUrl(link));
    private static final String TAG = "MyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDM = (Button) this.findViewById(R.id.btnDM);
        btnTR = (Button) this.findViewById(R.id.btnTR);


        btnDM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadmanager();

            }
        });

        btnTR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MyTask().execute();
            }
        });


    }



    //DownloadManager, ktery se podle mne hodi spise pro stahovani souboru jako takovych
    private void downloadmanager() {
        if(!isOnline()) {
            Toast.makeText(MainActivity.this, "" + "Neni pripojeni - ukonceni!",Toast.LENGTH_SHORT).show();
            return;
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
        request.setTitle("Stahovani souboru!");
        request.setDescription("Soubor se stahuje....");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nazevSouboru);

        DownloadManager manager = (DownloadManager) MainActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    // kontrola zda je telefon pripojeny
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }



    //  HttpURLConnection, ktere je idealni pro stahovani streamu typu xml/json pro dalsi
    //  zpracovani, to neznamena, ze nejde vyuzit na stahovani souboru.
    public class MyTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean aBoolean = false;
            if(!isOnline()) {
                runOnUiThread(new Runnable() {

                    public void run() {
                        Toast.makeText(getApplicationContext(), "Neni pripojeni - ukonceni!", Toast.LENGTH_SHORT).show();
                    }
                });

                return aBoolean;
            }


            try {
                URL myUrl = new URL(link);


                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.connect();
                Log.v(TAG, "Delani souboru");
                File root = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "Moje_obrazky");

                if(!root.exists()) {
                    root.mkdirs();
                }

                Log.v(TAG, "soubor udelan");
                File soubor = new File(root, nazevSouboru);
                soubor.createNewFile();

                InputStream inputStream = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(soubor);
                Log.v(TAG, "otevreni pripojeni");

                byte[] buffer = new byte[1024];
                int byteCount;

                while((byteCount = inputStream.read(buffer)) > 0 ) {
                    output.write(buffer, 0, byteCount);
                }

                output.close();

                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.parse(link));
                MainActivity.this.sendBroadcast(intent);

                aBoolean = true;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return aBoolean;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(aBoolean)
                Toast.makeText(MainActivity.this, "Staženo!",Toast.LENGTH_LONG).show();
        }
    }

}


