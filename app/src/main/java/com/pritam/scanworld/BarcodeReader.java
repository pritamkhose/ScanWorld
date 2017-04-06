package com.pritam.scanworld;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class BarcodeReader extends Activity implements View.OnClickListener{
    private CameraPreview mPreview;
    private CameraManager mCameraManager;
    private HoverView mHoverView;

	public static Activity activity = null;
	public static boolean flashon = false;
	Button btnFlash,btnSwitch,btn_zoomin,btn_zoomout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		activity = this;

		Display display = getWindowManager().getDefaultDisplay();
		mHoverView = (HoverView)findViewById(R.id.hover_view);
		mHoverView.update(display.getWidth(), display.getHeight());
		
		mCameraManager = new CameraManager(this);
        mPreview = new CameraPreview(this, mCameraManager.getCamera());
        mPreview.setArea(mHoverView.getHoverLeft(), mHoverView.getHoverTop(), mHoverView.getHoverAreaWidth(), display.getWidth());
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        getActionBar().hide();

		btnFlash =(Button)findViewById(R.id.btnFlash);
		btnSwitch =(Button)findViewById(R.id.btnSwitch);
		btn_zoomin =(Button)findViewById(R.id.btn_zoomin);
		btn_zoomout =(Button)findViewById(R.id.btn_zoomout);
		btnSwitch.setOnClickListener(this);
		btnFlash.setOnClickListener(this);
		btn_zoomin.setOnClickListener(this);
		btn_zoomout.setOnClickListener(this);

	}
	
	@Override
    protected void onPause() {
        super.onPause();
        mPreview.onPause();
        mCameraManager.onPause();
    }

	public void onClick(View v) {
		//Camera mCamera;
		//Parameters params = BarcodeReader.mCamera.getParameters();
		// TODO Auto-generated method stub
		switch (v.getId()) {

			case R.id.btnFlash:
				Toast.makeText(this, "btnFlash", Toast.LENGTH_LONG).show();
				/*try {
					if(BarcodeReader.flashon) {
						params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH); //FLASH_MODE_ON
						Toast.makeText(this, "Flash on", Toast.LENGTH_SHORT).show();
					}
					else {
						params.setFlashMode(Parameters.FLASH_MODE_OFF);
						Toast.makeText(this, "Flash off", Toast.LENGTH_SHORT).show();
					}
					mCamera.setParameters(params);
				} catch (RuntimeException e) {
					// Toast.makeText(getContext(), "Your camera doesn't appear to support torch mode.", Toast.LENGTH_LONG).show();
					//e.printStackTrace();
				}*/
				if(flashon) {
					flashon = false;
					//CameraPreview.params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					//Toast.makeText(getApplicationContext(), "Flash on", Toast.LENGTH_LONG).show();
				}else {
					flashon = true;
					//CameraPreview.params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					//Toast.makeText(getApplicationContext(), "Flash off", Toast.LENGTH_LONG).show();
				}
				//mCamera.setParameters(params);
				break;

			case R.id.btnSwitch:
				Toast.makeText(this, "btnSwitch", Toast.LENGTH_LONG).show();
				break;

			case R.id.btn_zoomin:
				Toast.makeText(this, "Z+", Toast.LENGTH_LONG).show();
				break;

			case R.id.btn_zoomout:
				Toast.makeText(this, "Z-", Toast.LENGTH_LONG).show();
				break;

			default:
				break;
		}
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		{
			super.onResume();
			mCameraManager.onResume();
			mPreview.setCamera(mCameraManager.getCamera());
		}
	}

	/*public void flashLight(View view){
		if(flashon) {
			flashon = false;
			//Toast.makeText(getApplicationContext(), "Flash on", Toast.LENGTH_LONG).show();
		}else {
			flashon = true;
			//Toast.makeText(getApplicationContext(), "Flash off", Toast.LENGTH_LONG).show();
		}
	}
	public void switchCamera(View view){
		//Toast.makeText(getApplicationContext(), "switch Camera", Toast.LENGTH_LONG).show();

	}*/

	@Override
	public void onBackPressed(){
		this.finish();
		super.onBackPressed();

	}

	public static void onBackPressedmy(){
		//onResume();
		//Toast.makeText(getApplicationContext(), "Barcode capture: "+CameraPreview.BarResult, Toast.LENGTH_LONG).show();
		System.out.print(CameraPreview.BarResult);

	}

}
