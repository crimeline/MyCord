package com.android.diagnosislibrary.utils;

import android.content.Context;

import com.android.diagnosislibrary.utils.Logger.Logger;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    private static final String TAG = "FileUtils";
    private static boolean suPermissions = false;

    public static void checkSuPermissions(Context ctx) {
        File fileNO = null;
        File fileOk = null;
        try {
            fileNO = new File(ctx.getFilesDir(), ".su.no");
            fileOk = new File(ctx.getFilesDir(), ".su.ok");
            if (fileOk.exists()) {
                suPermissions = true;
                return;
            } else if (fileNO.exists()) {
                suPermissions = false;
                return ;
            } else {
                Logger.i(TAG, "no check permissions ");
                Process process = Runtime.getRuntime().exec("su");
                process.destroy();
                fileOk.createNewFile();
            }
            suPermissions = true;
        } catch (Exception e) {
            try {
                fileNO.createNewFile();
                suPermissions = false;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Logger.e(TAG, "checkSuPermissions error: " + e.toString());
        }
        return;
    }

    public static boolean isSuPermissions(){
        return suPermissions;
    }
}
