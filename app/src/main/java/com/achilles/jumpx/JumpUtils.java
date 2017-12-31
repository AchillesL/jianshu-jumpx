package com.achilles.jumpx;

import android.content.Context;

/**
 * Created by Achilles on 2017/12/31.
 */

public class JumpUtils {

    public static final int SMALL_SIZE_WIDTH = 280;
    public static final int SMALL_SIZE_HIGH = 150;

    public static final int LARGE_SIZE_WIDTH = 1080;
    public static final int LARGE_SIZE_HIGH = 1920;

    public static final String SCREENSHOT_DIR = "/storage/emulated/0/JumpX/" ;
    public static final String SCREENSHOT_FILE_NAME = SCREENSHOT_DIR + "screenshot.png";
    public static final String LITTLE_BOY_FILE_NAME = SCREENSHOT_DIR + "little_boy.png";

    public static final float JUMP_SPEED = 1.46f;
    public static final float JUMP_SPEED_SLOW = 1.38f;
    public static final float JUMP_SPEED_FAST = 1.51f;

    //获取标题栏高度
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
