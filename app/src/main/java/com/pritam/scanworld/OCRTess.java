package com.pritam.scanworld;


import java.io.File;

import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

 /* Created by SalesBabu on 01-09-2016.*/


public class OCRTess {
    private TessBaseAPI mTess;

    public OCRTess( String language) {
        // TODO Auto-generated constructor stub
        mTess = new TessBaseAPI();
        String datapath = Environment.getExternalStorageDirectory() + "/scanworld/";
        System.out.print("datapath -" + datapath);
       // String language = "eng";
        File dir = new File(datapath + "tessdata/");
        if (!dir.exists())
            dir.mkdirs();
        mTess.init(datapath, language);
    }

    public String getOCRResult(Bitmap bitmap) {

        mTess.setImage(bitmap);
        String result = mTess.getUTF8Text();

        return result;
    }

    public void onDestroy() {
        if (mTess != null)
            mTess.end();
    }


}
