package com.pritam.scanworld;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback  { //,View.OnClickListener
    private SurfaceHolder mHolder;
    public Camera mCamera;
    private static final String TAG = "camera";
    private int mWidth, mHeight;
    private Context mContext;
    private MultiFormatReader mMultiFormatReader;
   // private AlertDialog mDialog;
    private int mLeft, mTop, mAreaWidth, mAreaHeight;
    public static String BarResult;
    public Parameters params;
   // Button btnFlash,btnSwitch;

    private boolean mAutoFocusOK = false;
    private boolean mAutoFocusInProgress = false;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         params = mCamera.getParameters();

        //System.out.print("in = "+params.toString());

        int orientation=this.getResources().getConfiguration().orientation;
        if (orientation== Configuration.ORIENTATION_LANDSCAPE){
            mWidth = 640;
            mHeight = 480;
        }
        else
        {
            mWidth = 144;
            mHeight = 176;
        }
        
        params.setPreviewSize(mWidth, mHeight);
        mCamera.setParameters(params);
        
        mMultiFormatReader = new MultiFormatReader();

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if ((mCamera != null) && mAutoFocusOK && (!mAutoFocusInProgress)) {
            try {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        mAutoFocusInProgress = false;
                    }
                });
                mAutoFocusInProgress = true;
            } catch (Exception e) {
                mAutoFocusOK = false;
                return true;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }


/*
    public void onClick(View v) {

            // TODO Auto-generated method stub
            switch (v.getId()) {

                case R.id.btnFlash:
                    try {
                        if(BarcodeReader.flashon) {
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH); //FLASH_MODE_ON
                            Toast.makeText(getContext(), "Flash on", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            params.setFlashMode(Parameters.FLASH_MODE_OFF);
                            Toast.makeText(getContext(), "Flash off", Toast.LENGTH_SHORT).show();
                        }
                        mCamera.setParameters(params);
                    } catch (RuntimeException e) {
                        // Toast.makeText(getContext(), "Your camera doesn't appear to support torch mode.", Toast.LENGTH_LONG).show();
                        //e.printStackTrace();
                    }
                    break;

                case R.id.btnSwitch:
                    Toast.makeText(getContext(), "Your camera doesn't appear to support torch mode.", Toast.LENGTH_LONG).show();
                    break;

                default:
                    break;
            }
    }
*/
    @Override
	public void surfaceDestroyed(SurfaceHolder holder) {
       // mAutoFocusOK = false;     error

    }


    @Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        FlashOnOFF();



        mAutoFocusOK = true;

        if (mHolder.getSurface() == null){
          return;
        }
        try {
            mCamera.setPreviewCallback(mPreviewCallback);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private void CameraSwitch() {

    }

    private void FlashOnOFF() {
        try {
            if(BarcodeReader.flashon) {
                params.setFlashMode(Parameters.FLASH_MODE_TORCH); //FLASH_MODE_ON
            }
            else {
                params.setFlashMode(Parameters.FLASH_MODE_OFF);
            }
            mCamera.setParameters(params);
        } catch (RuntimeException e) {
            Toast.makeText(getContext(), "Your camera doesn't appear to support torch mode.", Toast.LENGTH_LONG).show();
            //e.printStackTrace();
        }
    }

    public void setCamera(Camera camera) {
    	mCamera = camera;
    }

    public void onPause() {
    	if (mCamera != null) {
    		mCamera.setPreviewCallback(null);
    		mCamera.stopPreview();
    	}
    }

    private PreviewCallback mPreviewCallback = new PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub

        	LuminanceSource source = new PlanarYUVLuminanceSource(data, mWidth, mHeight, mLeft, mTop, mAreaWidth, mAreaHeight, false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer( source));
            Result result;
          
            try {
				result = mMultiFormatReader.decode(bitmap, null);
				if (result != null) {

                    mCamera.stopPreview();

                    BarResult =  result.getText();
                    try {
                        BarcodeReader.activity.finish();

                    } catch (Exception ignored) {
                        Toast.makeText(getContext(), "error in close window "+BarResult, Toast.LENGTH_LONG).show();
                    }

                }
            } catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    };
    
    public void setArea(int left, int top, int areaWidth, int width) {
    	double ratio = width / mWidth;
    	mLeft = (int) (left / (ratio + 1));
    	mTop = (int) (top / (ratio + 1));
    	mAreaHeight = mAreaWidth = mWidth - mLeft * 2;
    }
    
}
