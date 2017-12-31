package com.achilles.jumpx;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class OpenCVManager extends BaseLoaderCallback {
    private boolean isInit = false;
    private Context context;
    public OpenCVManager(Context context){
        super(context);
        this.context = context;
    }
    
    @Override
    public void onManagerConnected(int status) {
           switch (status) {
             case LoaderCallbackInterface.SUCCESS:
                 Toast.makeText(context,"opencv准备就绪",Toast.LENGTH_SHORT).show();
                 isInit = true;
              break;
             default:
                 isInit = false;
                 super.onManagerConnected(status);
              break;
         }
    }

}