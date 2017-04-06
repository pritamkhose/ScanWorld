package com.pritam.scanworld;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/*
http://www.codepool.biz/making-an-android-ocr-application-with-tesseract.html

https://github.com/tesseract-ocr/tessdata
https://github.com/tesseract-ocr/tesseract/blob/master/doc/tesseract.1.asc#languages
https://github.com/tesseract-ocr/tesseract/wiki

https://github.com/tesseract-ocr/tesseract/wiki/3rdParty
 */

public class OCRActivity3 extends ActionBarActivity implements OnClickListener {

    private View promptsView;
    private OCRTess mOCRTess;
    private TextView mResult;
    private ProgressDialog mProgressDialog;
    private ImageView mImage;
    private Button mButtonGallery, mButtonCamera;
    private String mCurrentPhotoPath;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static int GALLERY_KITKAT_INTENT_CALLED = 3;
    int imgW =1200, imgH =720;

    String urlstr = "", eid = "",ACCid="", Leadid="";
    Boolean  saveaddr = false;

    public String ocrTxt="";
    HashMap<String, String> hm;
    // Progress Dialog
    private ProgressDialog pDialog;
    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;

    Boolean imgRotate = false;
    final int PIC_CROP = 3;
    Bitmap bitmapsave;
    Uri picUri;

    List<String> cstatusarr=null;
    String cstatus="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);



        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "scanworld"+ File.separator+"tessdata"+ File.separator+"eng.traineddata");
        if (file.exists() == false) {
            try {
                if(!Utility.isConnected(this)) {
                    Toast.makeText(this, R.string.chkNet, Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
                else {
                    // File url to download
                    String file_url = "https://github.com/tesseract-ocr/tessdata/raw/master/eng.traineddata";
                    // starting new Async Task
                    new DownloadFileFromURL().execute(file_url);
                }
            } catch (Exception e0) {
                onBackPressed();
            }
        }
        ((ScrollView) findViewById(R.id.scrollview)).setVisibility(View.GONE);


        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
        String[] dataarr = getResources().getStringArray(R.array.lang);
        HashMap<String, String> hm = new HashMap<String, String>();
        for(int i=0; i<dataarr.length; i++) {
            hm.put("txt", dataarr[i].split("~")[1]);
            hm.put("cur", dataarr[i].split("~")[0]);
            aList.add(hm);
        }

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

        if(al!=null && al.size()>0){
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item , al);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner spinner = (Spinner) findViewById(R.id.lang);
            spinner.setAdapter(adapter);
            cstatusarr = al;

            if(cstatus!=null && cstatus.length()>0)
                if(cstatusarr.indexOf(cstatus)>=0)
                    ((Spinner) findViewById(R.id.lang)).setSelection(cstatusarr.indexOf(cstatus));
            ((Spinner) findViewById(R.id.lang)).setVisibility(View.VISIBLE);
        } else {
            ((Spinner) findViewById(R.id.lang)).setVisibility(View.GONE);
        }

        ((Button) findViewById(R.id.saveBtn)).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OCRActivity3.this, DownloadLib.class);
                startActivity(intent);
            }
        });


        {
            mResult = (TextView) findViewById(R.id.tv_result);
            mImage = (ImageView) findViewById(R.id.image);
            mButtonGallery = (Button) findViewById(R.id.bt_gallery);
            mButtonGallery.setOnClickListener(this);
            mButtonCamera = (Button) findViewById(R.id.bt_camera);
            mButtonCamera.setOnClickListener(this);
          //  mOCRTess = new OCRTess(((Spinner) findViewById(R.id.lang)).getSelectedItem().toString());
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

    /**
     * Showing Dialog
     * */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading Library file...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
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

        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Uri uri = (Uri) intent
                    .getParcelableExtra(Intent.EXTRA_STREAM);
            uriOCR(uri);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detailmenu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Respond to the toolbar action bar's back button
            case android.R.id.home:
                if (getParentActivityIntent() == null) {
                    //Log.i(TAG, "You have forgotten to specify the parentActivityName in the AndroidManifest!");
                    onBackPressed();
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            case R.id.action_save:
                // search action
                Toast.makeText(getApplicationContext(),"Nothing to save ", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_exit:
            {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
                finish();
                System.exit(0);
            }
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        mOCRTess.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        if (requestCode == REQUEST_TAKE_PHOTO  && resultCode == Activity.RESULT_OK && data != null) {
            // setPic();
            if (mProgressDialog == null)
                mProgressDialog = ProgressDialog.show(this, "Scanning Card", "Processing...", true);
            else
                mProgressDialog.show();

            File file = new File(mCurrentPhotoPath);
            Bitmap bitmap = Utility.decodeSampledBitmapFromFile(file.getAbsolutePath(),imgW, imgH);
            Utility.SaveImage(bitmap,file.getName());

            bitmapsave = bitmap;
            picUri = Uri.fromFile(file); // convert path to Uri
            //performCrop(picUri);
             imgRotate = true;
           // doOCR(bitmap);
            doOCR(bitmapsave);
        }
        else if (requestCode == REQUEST_TAKE_PHOTO ) {
            // setPic();
            try {
                if (mProgressDialog == null)
                    mProgressDialog = ProgressDialog.show(this, "Scanning Card", "Processing...", true);
                else
                    mProgressDialog.show();

                File file = new File(mCurrentPhotoPath);
                picUri = Uri.fromFile(file); // convert path to Uri
                Bitmap bitmap = Utility.decodeSampledBitmapFromFile(file.getAbsolutePath(), imgW, imgH);
                Utility.SaveImage(bitmap, file.getName());

                bitmapsave = bitmap;
                picUri = Uri.fromFile(file); // convert path to Uri
                //performCrop(picUri);
                imgRotate = true;
                // doOCR(bitmap);
                doOCR(bitmapsave);

            } catch (Exception e) {
                Toast.makeText(this, "problem with camera image", Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
            }
        }
        else if (requestCode == REQUEST_PICK_PHOTO  && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                if (mProgressDialog == null)
                    mProgressDialog = ProgressDialog.show(this, "Scanning Card", "Processing...", true);
                else
                    mProgressDialog.show();

                uriOCR(uri);
            }
        }
        else if (requestCode == GALLERY_KITKAT_INTENT_CALLED && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (uri != null) {
                if (mProgressDialog == null)
                    mProgressDialog = ProgressDialog.show(this, "Scanning Card", "Processing...", true);
                else
                    mProgressDialog.show();
                uriOCR(uri);
            }
        }

        //user is returning from cropping the image
        else if(requestCode == PIC_CROP){
            //get the returned data
           /* Bundle extras = data.getExtras();
            //get the cropped bitmap
            Bitmap bitmap = (Bitmap) extras.get("data");*/
            //display the returned cropped image
            doOCR(bitmapsave);
        }
        else {
            // mProgressDialog.show();
            Toast.makeText(this, "Problem with capture Image", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        switch (id) {
            case R.id.bt_gallery:
                pickPhoto();
                break;
            case R.id.bt_camera:
                takePhoto();
                break;
        }
    }

    public void camPhoto(View view) {
        takePhoto();
    }

    public void gallaryPhoto(View view) {
        pickPhoto();
    }

    private void pickPhoto() {
       /* Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_PHOTO);*/
        File photoFile = null;
        try {
            photoFile = createImageFile();
            Uri  uriImageFile =Uri.fromFile(photoFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (Build.VERSION.SDK_INT != 19){
            // Create the Intent for Image Gallery.
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Start new activity with the LOAD_IMAGE_RESULTS to handle back the results when image is picked from the Image Gallery.
            startActivityForResult(i, REQUEST_PICK_PHOTO);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/jpeg");
            intent.putExtra("crop", "true");
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
        }

    }

    private void takePhoto() {
        Uri uriImageFile = null;
        final int CAMERA_PIC_REQUEST = 1;
        File photoFile = null;
        try {
            photoFile = createImageFile();
            uriImageFile =Uri.fromFile(photoFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (!hasImageCaptureBug()) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImageFile);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, CAMERA_PIC_REQUEST);
    }

    /**
     * http://developer.android.com/training/camera/photobasics.html
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String storageDir = Environment.getExternalStorageDirectory() + "/scanworld";
        File dir = new File(storageDir);
        if (!dir.exists())
            dir.mkdir();

        File image = new File(storageDir + "/" + imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uriOCR(Uri uri) {

        if (uri != null) {
            InputStream is = null;
            try {
                is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                //image decode
                SaveImage(bitmap);
               // performCrop(picUri);
               // imgRotate = true;
                doOCR(bitmapsave);

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        else
            mProgressDialog.dismiss();
    }


    private void doOCR(final Bitmap bitmap) {

        Bitmap bitmap0 = null;
        if(imgRotate) {
            try {
                bitmap0 = toRotate(bitmap);
                imgRotate = false;
            } catch (Exception e) {
                bitmap0 = bitmap;
            }
        } else bitmap0 = bitmap;
        mImage.setImageBitmap(bitmap0);
        Bitmap bitmap1 = toGrayscale(bitmap0);
        final Bitmap bitmap2 = RemoveNoise(bitmap1);

        mOCRTess = new OCRTess(((Spinner) findViewById(R.id.lang)).getSelectedItem().toString());

        //mImage.setImageBitmap(bitmap2);
        new Thread(new Runnable() {
            public void run() {

                final String result = mOCRTess.getOCRResult(bitmap2);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (result != null && !result.equals("")) {
                            ocrTxt=result;
                            //mResult.setText(result);
                            //mImage.setImageBitmap(bitmap2);
                        }else {
                            Toast.makeText(OCRActivity3.this, "Unable to get data, please scan again", Toast.LENGTH_SHORT).show();
                        }
                        mProgressDialog.dismiss();
                        ((ScrollView) findViewById(R.id.scrollview)).setVisibility(View.VISIBLE);

                        mResult.setText(ocrTxt);
                        System.out.print("text result - "+ocrTxt+"\n----------------\n");

                    }

                });

            };
        }).start();
    }


    private void SaveImage(Bitmap finalBitmap) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_" + ".jpg";
        String storageDir = Environment.getExternalStorageDirectory() + "/scanworld";
        File dir = new File(storageDir);
        if (!dir.exists())
            dir.mkdir();

        // Save a file: path for use with ACTION_VIEW intents
        File file = new File (storageDir, imageFileName);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        picUri = Uri.fromFile(file); // convert path to Uri
        bitmapsave = Utility.decodeSampledBitmapFromFile(file.getAbsolutePath(), imgW, imgH);

    }

    //SetGrayscale
    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);//RGB_565
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    //SetGrayscale
    public Bitmap toRotate(Bitmap bmpOriginal)
    {
        System.out.println("Image dimesion \n"+bmpOriginal.getWidth()+"\t"+  bmpOriginal.getHeight());

        Matrix matrix = new Matrix();
        // matrix.postRotate((float) 90,  bmpOriginal.getWidth(),  bmpOriginal.getHeight());
        matrix.postRotate(90);
        Bitmap rotated = Bitmap.createBitmap(bmpOriginal, 0, 0, bmpOriginal.getWidth(), bmpOriginal.getHeight(), matrix, true);


        int value = getApplicationContext().getResources().getConfiguration().orientation;
        if (value == Configuration.ORIENTATION_PORTRAIT) {
            //orientation = "Portrait";
        }

        if (value == Configuration.ORIENTATION_LANDSCAPE) {
            //orientation = "Landscape";
        }

        // if(bmpOriginal.getWidth() > bmpOriginal.getHeight())
        return rotated;
        // else
        //    return bmpOriginal;


    }

    //RemoveNoise
    public Bitmap RemoveNoise(Bitmap bmap) {
        for (int x = 0; x < bmap.getWidth(); x++) {
            for (int y = 0; y < bmap.getHeight(); y++) {
                int pixel = bmap.getPixel(x, y);
                int R = Color.red(pixel);
                int G = Color.green(pixel);
                int B = Color.blue(pixel);
                if (R < 162 && G < 162 && B < 162)
                    bmap.setPixel(x, y, Color.BLACK);
            }
        }
        for (int  x = 0; x < bmap.getWidth(); x++) {
            for (int y = 0; y < bmap.getHeight(); y++) {
                int pixel = bmap.getPixel(x, y);
                int R = Color.red(pixel);
                int G = Color.green(pixel);
                int B = Color.blue(pixel);
                if (R > 162 && G > 162 && B > 162)
                    bmap.setPixel(x, y, Color.WHITE);
            }
        }
        return bmap;

    }


    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //super.onBackPressed();
            //Exit When Back and Set no History
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
            intent.putExtra("EXIT", true);
            startActivity(intent);
            finish();
            System.exit(0);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click back button again for exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
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
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "scanworld"+ File.separator+"tessdata"+File.separator+"eng.traineddata");

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

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
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

        }
    }


    public boolean hasImageCaptureBug() {

        // list of known devices that have the bug
        ArrayList<String> devices = new ArrayList<String>();
        devices.add("android-devphone1/dream_devphone/dream");
        devices.add("generic/sdk/generic");
        devices.add("vodafone/vfpioneer/sapphire");
        devices.add("tmobile/kila/dream");
        devices.add("verizon/voles/sholes");
        devices.add("google_ion/google_ion/sapphire");

        return devices.contains(Build.BRAND + "/" + Build.PRODUCT + "/"
                + Build.DEVICE);

    }
}


