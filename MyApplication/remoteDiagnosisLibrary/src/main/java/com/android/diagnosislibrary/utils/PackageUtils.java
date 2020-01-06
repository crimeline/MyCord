package com.android.diagnosislibrary.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.diagnosislibrary.utils.Logger.Logger;

import java.lang.reflect.Method;

public class PackageUtils {
    private static final String TAG = "PackageUtil";


    private static PackageInfo getPackageInfo(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(getName(context), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 获取应用包名
     */
    public static String getName(Context ctx) {
        if(ctx == null){
            return null;
        }
        return ctx.getPackageName();
    }

    /**
     * 获取应用版本号
     */
    private static String getVersion(Context ctx) {
        return getPackageInfo(ctx).versionName;
    }

    public static String getChannel(Context ctx) {
        if (ctx == null) {
            return null;
        }
        return getAppMetaData(ctx, "UMENG_CHANNEL");
    }

    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }

        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }

    /**
     * 根据应用包名获取应用版本号
     */
    public static int getAppVersion(Context ctx, String packageName) {
        if (ctx == null) {
            return -1;
        }
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 根据应用包名获取应用名称
     */
    public static String getAppName(Context ctx, String packageName) {
        if (ctx == null) {
            return null;
        }
        try {
            PackageManager pm = ctx.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(info).toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static int getVersionCode(Context ctx) {
        if (ctx == null) {
            return -1;
        }
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(ctx.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            return -1;
        }

    }

    public static String getVersionName(Context ctx) {
        if (ctx == null) {
            return null;
        }
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(ctx.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            return "";
        }

    }

    public static Bitmap getAppIcon(Context ctx, String packageName) {
        if (ctx == null) {
            return null;
        }
        Bitmap icon = null;

        try {
            PackageManager pm = ctx.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);


            Drawable drawable = info.loadIcon(pm);
            icon = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(icon);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            //
        }

        return icon;
    }

    public static boolean getPackageStatus(Context ctx, String packageName) {
        if (ctx == null) {
            return false;
        }
        boolean status;

        try {
            ctx.getPackageManager().getPackageInfo(packageName, 0);
            status = true;
        } catch (Exception e) {
            status = false;
        }

        return status;
    }

    public static boolean isSystemApp(Context ctx, String packageName) {
        if (ctx == null) {
            return false;
        }
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    || packageInfo.applicationInfo.uid < 9999) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static boolean scanApp(Context ctx, String packageName) {
        if (ctx == null) {
            return false;
        }
        boolean result = false;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        PackageManager pm = ctx.getPackageManager();
        try {
            Class clazz = pm.getClass();
            Method m2;
            m2 = clazz.getDeclaredMethod("scanApk", String.class);

            result = (Boolean) m2.invoke(pm, packageName);

        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }

        Logger.d(TAG, " - scan app result : " + result);
        return result;
    }

    public static boolean isAppInstalled(Context ctx, String packageName) {
        if (ctx == null) {
            return false;
        }
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            if (packageInfo != null) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return false;
    }
}
