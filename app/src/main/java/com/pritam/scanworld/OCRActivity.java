package com.pritam.scanworld;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.tools.RequestPermissionsTool;
import com.tools.RequestPermissionsToolImpl;

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

public class OCRActivity extends ActionBarActivity implements View.OnClickListener,ActivityCompat.OnRequestPermissionsResultCallback  {

    private static final String TAG = OCRActivity.class.getSimpleName();
    static final int PHOTO_REQUEST_CODE = 10;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static int GALLERY_KITKAT_INTENT_CALLED = 3;
    final int PIC_CROP = 30;
    int imgW = 1200, imgH = 720;
    Uri picUri;

    private TessBaseAPI tessBaseApi;
    TextView textView;
    Uri outputFileUri;
    private static final String lang = "eng";
    String result = "empty";
    private RequestPermissionsTool requestTool; //for API >=23 only

    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/scanworld/";
    private static final String TESSDATA = "tessdata";


    private TextView mResult;
    private ProgressDialog mProgressDialog;
    private ImageView mImage;
    private Button mButtonGallery, mButtonCamera;
    private String mCurrentPhotoPath;
    public String ocrTxt = "";
    String urlstr = "";

    // Progress Dialog
    private ProgressDialog pDialog;
    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;

    List<String> cstatusarr=null;
    String cstatus="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "scanworld" + File.separator + "tessdata" + File.separator + "eng.traineddata");
        if (file.exists() == false) {
           try {
                if (!Utility.isConnected(this)) {
                    Toast.makeText(this, R.string.chkNet, Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {
                    // File url to download starting new Async Task
                    new OCRActivity.DownloadFileFromURL().execute("https://github.com/tesseract-ocr/tessdata/raw/master/eng.traineddata");
                }
            } catch (Exception e0) {
                onBackPressed();
            }
        }


        /*Button captureImg = (Button) findViewById(R.id.action_btn);
        if (captureImg != null) {
            captureImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startCameraActivity();
                }
            });
        }*/
        textView = (TextView) findViewById(R.id.tv_result);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        }

        ((ScrollView) findViewById(R.id.scrollview)).setVisibility(View.GONE);

        PopulatSpinner();

        mResult = (TextView) findViewById(R.id.tv_result);
        mImage = (ImageView) findViewById(R.id.image);
        mButtonGallery = (Button) findViewById(R.id.bt_gallery);
        mButtonGallery.setOnClickListener(this);
        mButtonCamera = (Button) findViewById(R.id.bt_camera);
        mButtonCamera.setOnClickListener(this);

        ((Button) findViewById(R.id.saveBtn)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OCRActivity.this, DownloadLib.class);
                startActivity(intent);
            }
        });

    }

    public void Barcoderead(View view) {
        Intent intent = new Intent(this, BarcodeReader.class);
        startActivity(intent);
    }


    @Override
    public void onBackPressed(){
        this.finish();
        super.onBackPressed();
    }

    private void PopulatSpinner()  {
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

        ((Button) findViewById(R.id.saveBtn)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OCRActivity.this, DownloadLib.class);
                startActivity(intent);
            }
        });

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
     * to get high resolution image from camera
     */
    private void startCameraActivity() {
        try {
            String IMGS_PATH = Environment.getExternalStorageDirectory().toString() + "/scanworld";
            prepareDirectory(IMGS_PATH);

            String img_path = IMGS_PATH + "/ocr.jpg";

            outputFileUri = Uri.fromFile(new File(img_path));

            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, PHOTO_REQUEST_CODE);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Prepare directory on external storage
     *
     * @param path
     * @throws Exception
     */
    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }
    }


    private void prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //copyTessDataFiles(TESSDATA);
    }

    /**
     * Copy tessdata files (located on assets/tessdata) to destination directory
     *
     * @param path - name of directory with .traineddata files
     */
    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }


    /**
     * don't run this code in main thread - it stops UI thread. Create AsyncTask instead.
     * http://developer.android.com/intl/ru/reference/android/os/AsyncTask.html
     *
     * @param imgUri
     */
    private void startOCR(Uri imgUri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            Bitmap bitmap = BitmapFactory.decodeFile(imgUri.getPath(), options);

            result = extractText(bitmap);
            textView.setText(result);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    private String extractText(Bitmap bitmap) {
        try {
            tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (tessBaseApi == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }

        tessBaseApi.init(DATA_PATH, lang);

        Log.d(TAG, "Training file loaded");
        tessBaseApi.setImage(bitmap);
        String extractedText = "empty result";
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, "Error in recognizing text.");
        }
        tessBaseApi.end();
        return extractedText;
    }


    private void requestPermissions() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestTool = new RequestPermissionsToolImpl();
        requestTool.requestPermissions(this, permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean grantedAllPermissions = true;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                grantedAllPermissions = false;
            }
        }

        if (grantResults.length != permissions.length || (!grantedAllPermissions)) {

            requestTool.onPermissionDenied();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    ////////////////////////////////////////////////////////



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

        PopulatSpinner();

        ((TextView) findViewById(R.id.displaybarcode)).setText(CameraPreview.BarResult);
       /* Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Uri uri = (Uri) intent
                    .getParcelableExtra(Intent.EXTRA_STREAM);
            uriOCR(uri);
        }*/
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
                //Toast.makeText(getApplicationContext(),"Customer Info - Save menu called... ", Toast.LENGTH_LONG).show();
                //doSave(null);
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
    }



    // ////////////////////////////////////////////////////

/*
    void Onclickitem() {

        ((ScrollView) findViewById(R.id.scrollview)).setVisibility(View.GONE);

        mResult = (TextView) findViewById(R.id.tv_result);
        mImage = (ImageView) findViewById(R.id.image);
        mButtonGallery = (Button) findViewById(R.id.bt_gallery);
        mButtonGallery.setOnClickListener(this);
        mButtonCamera = (Button) findViewById(R.id.bt_camera);
        mButtonCamera.setOnClickListener(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simplerow, SpinData);

        spName = (Spinner) findViewById(R.id.spName);
        spName.setAdapter(adapter);
        spName.setSelection(1);
        spDesg = (Spinner) findViewById(R.id.spDesg);
        spDesg.setAdapter(adapter);
        spDesg.setSelection(2);
        spCName = (Spinner) findViewById(R.id.spCName);
        spCName.setAdapter(adapter);
        spCName.setSelection(3);
        spAddr = (Spinner) findViewById(R.id.spAddr);
        spAddr.setAdapter(adapter);
        spAddr.setSelection(4);
        spcity = (Spinner) findViewById(R.id.spcity);
        spcity.setAdapter(adapter);
        spcity.setSelection(5);
        spState = (Spinner) findViewById(R.id.spState);
        spState.setAdapter(adapter);
        spState.setSelection(6);
        spCnty = (Spinner) findViewById(R.id.spCnty);
        spCnty.setAdapter(adapter);
        spCnty.setSelection(7);
        spPin = (Spinner) findViewById(R.id.spPin);
        spPin.setAdapter(adapter);
        spPin.setSelection(8);
        spPhone = (Spinner) findViewById(R.id.spPhone);
        spPhone.setAdapter(adapter);
        spPhone.setSelection(9);
        spEmail = (Spinner) findViewById(R.id.spEmail);
        spEmail.setAdapter(adapter);
        spEmail.setSelection(10);
        spWeb = (Spinner) findViewById(R.id.spWeb);
        spWeb.setAdapter(adapter);
        spWeb.setSelection(11);

    }
*/
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
            case R.id.image:
                showImage1();
                break;
        }
    }

    public void showImage (View view) {
        showImage1();
    }

    public void showImage1() {
        String filePath = Environment.getExternalStorageDirectory()  + "/scanworld/temporary.jpg";
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "scanworld" + File.separator  + "temporary.jpg");
        if (file.exists() == true) {
            Intent intent = new Intent(this, ImgActivity.class);
            intent.putExtra("imgpath", filePath);
            startActivity(intent);
        }   else
            Toast.makeText(this, "Preview not Available", Toast.LENGTH_SHORT).show();
    }

    public void camPhoto(View view) {
        Toast.makeText(this, "Capture photo Which Landscape mode or Horizontally", Toast.LENGTH_SHORT).show();
        takePhoto();
    }

    public void gallaryPhoto(View view) {
        Toast.makeText(this, "Select photo Which Landscape mode or Horizontally", Toast.LENGTH_SHORT).show();
        pickPhoto();
    }

    private void pickPhoto() {
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //making photo
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            doOCR();
        }
        else if (requestCode == REQUEST_TAKE_PHOTO  && resultCode == Activity.RESULT_OK && data != null) {
            // setPic();
            if (mProgressDialog == null)
                mProgressDialog = ProgressDialog.show(this, "Scanning Card", "Processing...", true);
            else
                mProgressDialog.show();

            File file = new File(mCurrentPhotoPath);
            Bitmap bitmap = Utility.decodeSampledBitmapFromFile(file.getAbsolutePath(),imgW, imgH);
            Utility.SaveImage(bitmap,file.getName());
            Uri picUri = Uri.fromFile(file); // convert path to Uri
            performCrop(picUri);
        }
        else if (requestCode == REQUEST_TAKE_PHOTO ) {
            try {
                if (mProgressDialog == null)
                    mProgressDialog = ProgressDialog.show(this, "Scanning Card", "Processing...", true);
                else
                    mProgressDialog.show();

                File file = new File(mCurrentPhotoPath);
                Bitmap bitmap = Utility.decodeSampledBitmapFromFile(file.getAbsolutePath(),imgW, imgH);
                Utility.SaveImage(bitmap,file.getName());
                Uri picUri = Uri.fromFile(file); // convert path to Uri
                performCrop(picUri);

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
            /*//get the returned data
            Bundle extras = data.getExtras();
            //get the cropped bitmap
            Bitmap bitmap = (Bitmap) extras.get("data");
            //display the returned cropped image
            doOCR(bitmap);*/
            //File f = new File(Environment.getExternalStorageDirectory(),"/scanworld/temporary.jpg");
            String filePath = Environment.getExternalStorageDirectory()  + "/scanworld/temporary.jpg";
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            doOCR(bitmap);
        }


        else {
            Toast.makeText(this, "ERROR: Image was not obtained.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uriOCR(Uri uri) {
        if (uri != null) {
            InputStream is = null;
            try {
                is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                //image decode
                SaveImage(bitmap);
                performCrop(picUri);

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                mProgressDialog.dismiss();
                Toast.makeText(this, "FileNotFoundException", Toast.LENGTH_SHORT).show();
                System.out.print("FileNotFoundException");
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        mProgressDialog.dismiss();
                        Toast.makeText(this, "IOException", Toast.LENGTH_SHORT).show();
                        System.out.print("IOException");
                    }
                }
            }
        }
        else
            mProgressDialog.dismiss();
    }

    private void doOCR() {
        prepareTesseract();
        startOCR(outputFileUri);
    }


    private void doOCR(final Bitmap bitmap) {
        Bitmap bitmap2= null;
        try{
            Bitmap bitmap0 = bitmap;
            Bitmap bitmap1 = toGrayscale(bitmap0);
            bitmap2 = RemoveNoise(bitmap1);
            //mImage.setImageBitmap(bitmap2);
        } catch (Exception e ) {
            e.printStackTrace();
            System.out.print("Error to Grayscale ");
            Toast.makeText(this, "Error to Grayscale ", Toast.LENGTH_SHORT).show();
        }

        final Bitmap bitmap20= bitmap2;

        try {

            new Thread(new Runnable() {
                public void run() {

                    result = extractText(bitmap20);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if (result != null && !result.equals("")) {
                                ocrTxt = result;
                                textView.setText(result);
                                mImage.setImageBitmap(bitmap20);
                                ((ScrollView) findViewById(R.id.scrollview)).setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(OCRActivity.this, "Unable to get data, please scan again", Toast.LENGTH_SHORT).show();
                            }
                            mProgressDialog.dismiss();
                        }

                    });

                }

                ;
            }).start();


        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            System.out.print("Error with Recognize text");
            Toast.makeText(this, "Error with Recognize text", Toast.LENGTH_SHORT).show();
        }



        /*try {
            new Thread(new Runnable() {
                public void run() {

                   // final String result = mOCRTess.getOCRResult(bitmap20);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            if (result != null && !result.equals("")) {
                                ocrTxt = result;
                                mImage.setImageBitmap(bitmap20);
                            } else {
                                Toast.makeText(OCRActivity.this, "Unable to get data, please scan again", Toast.LENGTH_SHORT).show();
                            }
                            mProgressDialog.dismiss();
                            setData(ocrTxt);
                        }

                    });

                }

                ;
            }).start();

        } catch (Exception e ) {
            e.printStackTrace();
            System.out.print("Error with Recognize text");
            Toast.makeText(this, "Error with Recognize text", Toast.LENGTH_SHORT).show();
        }*/
    }

//////////////////////////////////////////////////////////////

   /*
    void contactaddSave() {
        urlstr = "";

        if (eid != null && eid.length() > 0){
            urlstr = urlstr + "e=" + ApplConstants.ACCOUNT + "&se=2&a=" + ApplConstants.ACT_EDIT_SAVE + "&i=" + eid + "&ac=" + ((SBCrmApplication) this.getApplication()).getAccDB() + "&ui=" + ((SBCrmApplication) this.getApplication()).getUID()
                    + "&al=" + DataHM.get("Address").toString()
                    + "&c=" + DataHM.get("City").toString()
                    + "&s=" + DataHM.get("State").toString()
                    + "&cy=" + DataHM.get("Country").toString()
                    + "&p=" + DataHM.get("Pin").toString();

            //urlstr = URLEncoder.encode(urlstr, "utf-8");
            urlstr = ((SBCrmApplication) this.getApplication()).getApplURL() + urlstr;
            urlstr = (urlstr.replace(' ', '+')).replaceAll("\n","%0A");
            System.out.println(".........contactaddSave doSave::::url=" + urlstr);
            GetXMLTask1 task = new GetXMLTask1();
            task.execute(new String[]{urlstr});
        }   else
            Toast.makeText(this, "Problem with get id during save address", Toast.LENGTH_SHORT).show();
    }

    private class GetXMLTask1 extends HttpAsyncTask {
        @Override
        protected void onPostExecute(String output) {
            //Toast.makeText(getApplicationContext(),"GetXMLTask..." + output, Toast.LENGTH_LONG).show();
            //System.out.println("----------"+output);
            if (output.equals("Timeout\n")) {
                Toast.makeText(getApplicationContext(), R.string.chktimeout, Toast.LENGTH_LONG).show();
            } else {
                output = output.replaceAll("\b", " ");
                String[] result = output.trim().split("\\^");
                if (result.length > 3) {
                    callModulePage(result);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.serErr, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void callModulePage(String[] data) {

        if(data.length>3) {
            if (data[3].equals("CHECKACCLEAD")) {
                if (data[4].length() > 0 && data[4] != null) {
                    String dataarr[] = data[4].split("~");
                    dataarr[1] = (dataarr[1].replaceAll("ACNT", ""));
                    if (dataarr[0].toString().equals("ACC") && !((dataarr[1].toString().replaceAll("ACNT", "")).equals("0"))) {
                        ACCid = dataarr[1].toString();
                    } else if (dataarr[2].toString().equals("LEAD") && !(dataarr[2].toString().equals("0"))) {
                        Leadid = dataarr[3].toString();
                    } else {
                        eid = "";
                    }
                    showOCRPopup();
                }

            }

            if (data.length >= 4 && data[2].equals(ApplConstants.ACT_EDIT_SAVE + "") || data[2].equals(ApplConstants.ACT_CREATE_SAVE + "")) {
                Toast.makeText(getApplicationContext(), "Data saved successfully", Toast.LENGTH_LONG).show();
                if(saveaddr) {
                    saveaddr= false;
                    String [] dataarr = data[4].split("~");
                    eid = dataarr[0].toString();
                    contactaddSave();
                                    }
            }
        }

    }

    */
//////////////////////////////////////////////////////////////

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







    private Bitmap SaveImage(Bitmap finalBitmap) {

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

        picUri = Uri.fromFile(file); // convert path to Uri*/
        return finalBitmap;

    }


    private void performCrop(Uri picUri){
        try {
           /* //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 0.8);
            cropIntent.putExtra("aspectY", 0.5);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            // cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);*/

            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            //cropIntent.putExtra("aspectX", 16);
            //cropIntent.putExtra("aspectY", 9);
            //indicate output X and Y
            //cropIntent.putExtra("outputX", 800);
            //cropIntent.putExtra("outputY", 800);

            File f = new File(Environment.getExternalStorageDirectory(),"/scanworld/temporary.jpg");
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Log.e("io", ex.getMessage());
            }

            Uri uri = Uri.fromFile(f);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, PIC_CROP);

        } //respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            //display an error message
            Toast.makeText(this, "Your device doesn't support the crop action!", Toast.LENGTH_SHORT).show();
        }
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


    /**  Background Async Task to download file * */

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
                //  Toast.makeText(OCRActivity.this, "Error in download file", Toast.LENGTH_SHORT).show();

                File dataDir  = new File(Environment.getExternalStorageDirectory(), "scanworld"+File.separator+"tessdata"+File.separator+"eng.traineddata");
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

            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "scanworld" + File.separator + "tessdata" + File.separator + "eng.traineddata");
            if (file.exists() == false) {
                Toast.makeText(OCRActivity.this, "Downloading Failed due to lack of Internet, Retry again", Toast.LENGTH_SHORT).show();
               // onBackPressed();
            }
        }
    }

}


