package com.achilles.jumpx;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class JumpService extends Service {

    public static final int MSG_SCREENSHOT_COMPLETE = 0;
    private Handler mHandler;

    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;

    private MyLinearLayout mLinearLayout;
    private Button mButton;

    private Point mFirstPoint;
    private Point mSecondPoint;

    private boolean mBeginGamp = false;

    private OpenCVManager javaUtil = new OpenCVManager(this);
    private IMatchLittleBoy mIMatchLittleBoy;

    private boolean mMatchBoyCompleted = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //设置悬浮窗参数并显示
        mParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);

        mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.x = 0;
        mParams.y = 0;

        mParams.width = JumpUtils.SMALL_SIZE_WIDTH;
        mParams.height = JumpUtils.SMALL_SIZE_HIGH;

        mLinearLayout = (MyLinearLayout) LayoutInflater.from(getApplication()).inflate(R.layout.layout, null);
        mButton = mLinearLayout.findViewById(R.id.btn);
        mWindowManager.addView(mLinearLayout, mParams);


        mLinearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //在成功匹配到小人位置前，忽略触屏反应
                if (!mMatchBoyCompleted) return false;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!mBeginGamp) return false;
                    Log.d("Achilles", "x:" + motionEvent.getX() + " y:" + motionEvent.getY());

                    mSecondPoint = new Point((int) motionEvent.getX(), (int) motionEvent.getY());
                    tryJump(mFirstPoint, mSecondPoint);
                }
                return false;
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBeginGamp = !mBeginGamp;
                mButton.setText(mBeginGamp ? "点击关闭" : "点击开始");
                resizeLayout(mBeginGamp);
                if (!mBeginGamp) {
                    mFirstPoint = null;
                    mSecondPoint = null;
                    mHandler.removeMessages(MSG_SCREENSHOT_COMPLETE);
                }
                if (mBeginGamp) {
                    try2Screenshot();
                }
            }
        });

        //初始化openCV
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, javaUtil);
        } else {
            javaUtil.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_SCREENSHOT_COMPLETE: {
                        try2MatchLittleBoy();
                        break;
                    }
                }
            }
        };

        mIMatchLittleBoy = new IMatchLittleBoy() {
            @Override
            public void postion(Point point1, Point point2) {
                mFirstPoint = getFirstPointFormRect(point1,point2);
                mLinearLayout.setPointsAndShowLittleBoyRect(point1, point2);
            }
        };
    }

    //获取起跳坐标
    private Point getFirstPointFormRect(Point point1, Point point2) {
        int pointX = (int) ((point1.x + point2.x) / 2f);
        return new Point(pointX, point2.y);
    }

    //起跳
    private void tryJump(Point firstPoint, Point secondPoint) {
        //两点之间的距离
        double distance = Math.sqrt(Math.pow(firstPoint.x - secondPoint.x, 2) + Math.pow(firstPoint.y - secondPoint.y, 2));
        //根据两点距离判断起跳系数
        float ratio = distance > 600 ? JumpUtils.JUMP_SPEED_SLOW : distance < 300 ? JumpUtils.JUMP_SPEED_FAST : JumpUtils.JUMP_SPEED;
        //生成按下屏幕的时间
        final double holdTime = distance * ratio;

        //执行swipe命令时，需要将悬浮框覆盖屏幕的范围缩小，否则swipe命令会作用在悬浮框上，就起不了作用
        resizeLayout(false);
        //清除匹配小人的方块
        mLinearLayout.clearDraw();

        //执行swipe命令
        new Thread(new Runnable() {
            @Override
            public void run() {
                String command[] = new String[]{"sh", "-c",
                        "input touchscreen swipe 1000 1000 1200 1200 " + (int) holdTime};
                ShellUtils.CommandResult commandResult = ShellUtils.execCommand(command, true, true);
                Log.d("Achilles:", commandResult.errorMsg);
            }
        }).start();

        //重新将悬浮框覆盖整个屏幕
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resizeLayout(true);
            }
        }, 800);

        //等跳跃动画结束后，进行下一次截图
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try2Screenshot();
            }
        },3000);

        mMatchBoyCompleted = false;
    }

    private void resizeLayout(boolean isOpen) {
        Log.d("Achilles: ", "isOpen:" + isOpen);
        mWindowManager.removeView(mLinearLayout);
        mParams.width = isOpen ? JumpUtils.LARGE_SIZE_WIDTH : JumpUtils.SMALL_SIZE_WIDTH;
        mParams.height = isOpen ? JumpUtils.LARGE_SIZE_HIGH : JumpUtils.SMALL_SIZE_HIGH;
        mWindowManager.addView(mLinearLayout, mParams);
    }

    //root下的截屏
    private void try2Screenshot() {
        File file = new File(JumpUtils.SCREENSHOT_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String command[] = new String[]{"sh", "-p",
                        "/system/bin/screencap " + JumpUtils.SCREENSHOT_FILE_NAME};
                ShellUtils.CommandResult commandResult = ShellUtils.execCommand(command, true, true);
                Log.d("Achilles:", commandResult.errorMsg);
            }
        }).start();
        //延时800ms，确保截图完成后，进行图片匹配
        mHandler.sendEmptyMessageDelayed(MSG_SCREENSHOT_COMPLETE, 800);
    }

    private void try2MatchLittleBoy() {
        Mat source = new Mat();   //Mat相当于Android的Bitmap
        Mat template = new Mat();

        //由于笔者开了root与文件读写权限，若在Android M或更高级的系统上，可能需要按照官方的文件读写实现，否则返回的bitmapSource可能为null
        Bitmap bitmapSource = BitmapFactory.decodeFile(JumpUtils.SCREENSHOT_FILE_NAME);
        Bitmap bitmapTemplate = BitmapFactory.decodeFile(JumpUtils.LITTLE_BOY_FILE_NAME);

        Utils.bitmapToMat(bitmapSource, source);
        Utils.bitmapToMat(bitmapTemplate, template);

        //创建于原图相同的大小，储存匹配度
        Mat result = Mat.zeros(source.rows() - template.rows() + 1, source.cols() - template.cols() + 1, CvType.CV_32FC1);
        //调用模板匹配方法
        Imgproc.matchTemplate(source, template, result, Imgproc.TM_SQDIFF_NORMED);
        //规格化
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1);
        //获得最可能点，MinMaxLocResult是其数据格式，包括了最大、最小点的位置x、y
        Core.MinMaxLocResult mlr = Core.minMaxLoc(result);
        org.opencv.core.Point matchLoc = mlr.minLoc;

        //通知成功匹配的坐标
        notifyDrawLittleBoyRect(matchLoc, template);
    }

    private void notifyDrawLittleBoyRect(org.opencv.core.Point matchLoc, Mat template) {
        mMatchBoyCompleted = true;
        //在截屏中匹配的坐标是全局的，需要减去标题栏高度
        int matacLocY = (int)(matchLoc.y - JumpUtils.getStatusBarHeight(mLinearLayout.getContext()));
        mIMatchLittleBoy.postion(new Point((int) matchLoc.x, matacLocY), new Point((int) (matchLoc.x + template.width()), (int) (matacLocY + template.height())));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

