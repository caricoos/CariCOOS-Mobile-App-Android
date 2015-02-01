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
    private static int SPLASH_TIME_OUT = 3000;
    public String DATA;
    public boolean dataReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
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
            fetch fetchObj = new fetch(/*URL*/"http://136.145.59.33/app/elmer/csv/JsonBuoyData.json");
            try {
                DATA = fetchObj.postDataException();
            }catch (IOException e) {
                fetchObj = new fetch(/*URL*/"http://136.145.249.39/app/elmer/csv/JsonBuoyData.json");
            }

            DATA = fetchObj.postData();

            if(DATA.isEmpty()) {
                dataReady = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //Log.i("coos", ""+result);

            if (!dataReady /*Verify if data is ready*/) {
                Toast.makeText(getApplicationContext(),
                        "There was a network error.", Toast.LENGTH_LONG).show();
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
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date date = new Date();
                createFile("date.txt", dateFormat.format(date));
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