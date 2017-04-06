package com.pritam.scanworld;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DownloadLib extends AppCompatActivity {

    private ProgressDialog pDialog;
    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;
    String cur ="", txt ="";
    Boolean fileDownload = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_lib);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListViewUpdate();
    }

    private void ListViewUpdate() {
        File dataDir1 = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dataDir1 = new File(Environment.getExternalStorageDirectory(), "scanworld" + File.separator + "tessdata");
        }
        List<File> files1 = getListFiles(dataDir1);
        List<String> al = new ArrayList<String>();
        for(int i =0; i < files1.size();i++){
            String[]  a = files1.get(i).toString().split("/");
            al.add(a[a.length-1].split("\\.")[0].toString().toLowerCase());
        }

        final List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();

        String[] dataarr = getResources().getStringArray(R.array.lang);
        for(int i=0; i<dataarr.length; i++)
        {
            HashMap<String, String> hm = new HashMap<String,String>();
            hm.put(  "txt",dataarr[i].split("~")[1]);
            hm.put(  "cur",dataarr[i].split("~")[0]);

            String color = "0";
            if(al.contains(dataarr[i].split("~")[0]))
                color = "1";
            hm.put(  "color",color);

            aList.add(hm);
        }


        // Keys used in Hashmap
        String[] from = { "txt","cur" };

        // Ids of views in listview_layout
        int[] to = { R.id.txt,R.id.cur};

        // Instantiating an adapter to store each items
        // R.layout.listview_layout defines the layout of each item
        // SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.list_item, from, to);
        SimpleAdapterBGColor adapter = new SimpleAdapterBGColor(getBaseContext(), aList, R.layout.list_item, from, to);

        // Getting a reference to listview of main.xml layout file
        ListView listView = ( ListView ) findViewById(R.id.list);
        listView.setFastScrollEnabled(true);
        // Setting the adapter to the listView
        listView.setAdapter(adapter);

        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {

                txt = aList.get(position).get("txt");
                cur =  aList.get(position).get("cur");
                // File url to download

                if(fileDownload) {
                    fileDownload = false;
                    String file_url = "https://github.com/tesseract-ocr/tessdata/raw/master/" + cur + ".traineddata";
                    // starting new Async Task
                    new DownloadFileFromURL().execute(file_url);
                } else
                    Toast.makeText(DownloadLib.this, "Wait until completion of downloading file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Showing Dialog
     * */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading "+txt+" Library ...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(false);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }




    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }


    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /* Before starting background thread  Show Progress Bar Dialog */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showDialog(progress_bar_type);
        }

        /* Downloading file in background thread */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {

                File dataDir = null;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    dataDir = new File(Environment.getExternalStorageDirectory(), "scanworld"+File.separator+"tessdata");
                    if(!dataDir.isDirectory()) {
                        dataDir.mkdirs();
                    }
                }

                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "scanworld"+ File.separator+"tessdata"+File.separator+cur+".traineddata");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress(""+(int)((total*100)/lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                fileDownload = true;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                File dataDir  = new File(Environment.getExternalStorageDirectory(), "scanworld"+ File.separator+"tessdata"+File.separator+cur+".traineddata");
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    if (!dataDir.isDirectory()) {
                        dataDir.delete();
                    }
                }
            }

            return null;
        }


        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /*** After completing background task  Dismiss the progress dialog ***/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "scanworld"+ File.separator+"tessdata"+File.separator+cur+".traineddata");
            if (file.exists() == false) {
                Toast.makeText(DownloadLib.this, "Downloading Failed due to lack of Internet, Retry again", Toast.LENGTH_LONG).show();
                //onBackPressed();
            }

            ListViewUpdate();
        }
    }



        private List<File> getListFiles(File parentDir) {
            ArrayList<File> inFiles = new ArrayList<File>();
            File[] files = parentDir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    inFiles.addAll(getListFiles(file));
                } else {
                    if(file.getName().endsWith(".traineddata")){
                        inFiles.add(file);
                    }
                }
            }
            return inFiles;
        }
    }
