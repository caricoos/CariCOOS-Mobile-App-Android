package caricoos_app_mobile.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class splashActivity extends Activity {

    public ProgressBar pb;
    private static int SPLASH_TIME_OUT = 1000;
    public String DATA;
    public String DATA_FORECAST;
    public String DATA_FORECAST_ofs;
    public String DATA_WAVEWATCH;
    public boolean dataReady = true;
    public boolean dataReady_forecast = true;
    public boolean dataReady_forecast_ofs = true;
    public boolean dataReady_wavewatch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        try {
            Bundle bundle = getIntent().getExtras();
            SPLASH_TIME_OUT = Integer.parseInt(bundle.getString("delay"));
        } catch(Exception e) { e.printStackTrace(); }

        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new fetchData().execute();
            }
        }, SPLASH_TIME_OUT);
    }

    private class fetchData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            fetch fetchObj =
                    new fetch( /*URL*/ "http://136.145.249.39/app/elmer/csv/JsonBuoyData.json");

            DATA = fetchObj.postData();

            if(DATA.isEmpty()) {
                dataReady = false;
            }

            fetch fetchObj_forecast =
                    new fetch( /*URL*/ "http://caricoos.org/Swan.json");

            DATA_FORECAST = fetchObj_forecast.postData();

            if(DATA_FORECAST.isEmpty()) {
                dataReady_forecast = false;
            }

            fetch fetchObj_forecast_ofs =
                    new fetch( /*URL*/ "http://caricoos.org/Swan1.json");

            DATA_FORECAST_ofs = fetchObj_forecast_ofs.postData();

            if(DATA_FORECAST_ofs.isEmpty()) {
                dataReady_forecast_ofs = false;
            }

            fetch fetchObj_wavewatch =
                    new fetch( /*URL*/ "http://caricoos.org/Swan2.json");

            DATA_WAVEWATCH = fetchObj_wavewatch.postData();

            if(DATA_WAVEWATCH.isEmpty()) {
                dataReady_wavewatch = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!dataReady /*Verify if data is ready*/) {
                Toast.makeText(getApplicationContext(),
                        "There was na network error.", Toast.LENGTH_LONG).show();
            } else {
                File data_file = getFileStreamPath("data.json");
                File date_file = getFileStreamPath("date.txt");
                if(data_file.exists()){
                    data_file.delete();
                }
                if(date_file.exists()) {
                    date_file.delete();
                }

                createFile("data.json", DATA);
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss" /**/);
                Date date = new Date();
                createFile("date.txt", dateFormat.format(date));
            }

            if (!dataReady_forecast /*Verify if data is ready*/) {
                Toast.makeText(getApplicationContext(),
                        "There was na network error.", Toast.LENGTH_LONG).show();
            } else {
                File data_file = getFileStreamPath("data_forecast.json");
                if(data_file.exists()){
                    data_file.delete();
                }
                createFile("data_forecast.json", DATA_FORECAST);
            }

            if (!dataReady_forecast_ofs /*Verify if data is ready*/) {
                Toast.makeText(getApplicationContext(),
                        "There was na network error.", Toast.LENGTH_LONG).show();
            } else {
                File data_file = getFileStreamPath("data_forecast_ofs.json");
                if(data_file.exists()){
                    data_file.delete();
                }
                createFile("data_forecast_ofs.json", DATA_FORECAST_ofs);
            }

            if (!dataReady_wavewatch /*Verify if data is ready*/) {
                Toast.makeText(getApplicationContext(),
                        "There was na network error.", Toast.LENGTH_LONG).show();
            } else {
                File data_file = getFileStreamPath("data_wavewatch.json");
                if(data_file.exists()){
                    data_file.delete();
                }
                createFile("data_wavewatch.json", DATA_WAVEWATCH);
            }

            Intent i = new Intent(splashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        public void createFile(String filename, String data) {
            FileOutputStream fos;
            try {
                fos = openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write(data.getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}