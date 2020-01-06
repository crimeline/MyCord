package com.android.diagnosislibrary.utils;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.android.diagnosislibrary.utils.Logger.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {

    private final static String TAG = "CommonUtil";

    public static String intIP2str(int ip) {
        String ipAddr = "";

        ipAddr = (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 24) & 0xFF);
        return ipAddr;
    }

    /**
     * 万能获取当前网络的IP地址
     *
     * @return
     */
    public static String getIpAddress(Context ctx) {
        String ipaddress = "";

        try {
            WifiManager wifi = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifi.getConnectionInfo();

            ipaddress = CommonUtil.intIP2str(wifiInfo.getIpAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(ipaddress) || "0.0.0.0".equals(ipaddress)) {
            Enumeration<NetworkInterface> netInterfaces = null;
            try {
                netInterfaces = NetworkInterface.getNetworkInterfaces();
                while (netInterfaces.hasMoreElements()) {
                    NetworkInterface intf = netInterfaces.nextElement();
                    if (intf.getName().toLowerCase().equals("eth0")
                            || intf.getName().toLowerCase().equals("wlan0")
                            || intf.getName().toLowerCase().equals("br0v1")
                            || intf.getName().toLowerCase().equals("br0")) {
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                ipaddress = inetAddress.getHostAddress().toString();
                            }
                        }
                    } else {
                        continue;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ipaddress;
    }

    /**
     * 获取设备dpi比例
     *
     * @param context
     * @return
     */
    public static float getDeviceDensity(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float density = dm.density;
        Logger.d(TAG, " getDeviceDensity density : " + density);

        return density;
    }

    /**
     * 根据应用包名获取应用图标
     */
    public static Bitmap getAppIcon(Context ctx, String packageName) {
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

    /**
     * 根据应用包名获取应用版本号
     */
    public static int getAppVersion(Context ctx, String packageName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }

    public static String getLocalMacAddressFromIp() {
        String mac_s = "";
        try {
            byte[] mac;
            NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(getLocalIpAddress()));
            mac = ne.getHardwareAddress();
            mac_s = byte2hex(mac);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mac_s;
    }

    private static String getLocalIpAddress() {
        try {
            List<NetworkInterface> niList = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : niList) {
                List<InetAddress> iaList = Collections.list(ni.getInetAddresses());
                for (InetAddress address : iaList) {
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                        return address.getHostAddress().toString();
                    }
                }
            }

        } catch (SocketException ex) {
            Logger.e(TAG, ex.getMessage());
        }
        return null;
    }

    private static String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer(b.length);
        String stmp = "";
        int len = b.length;
        for (int n = 0; n < len; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1)
                hs = hs.append("0").append(stmp);
            else {
                hs = hs.append(stmp);
            }
        }
        return String.valueOf(hs);
    }

    /**
     * 获取系统属性
     */
    public static String getProperties(String key, String defaultvalue) {
        String value = "";
        Class<?> SystemPropertiesClass;
        Method method;

        try {
            SystemPropertiesClass = Class.forName("android.os.SystemProperties");
            Class<?> getType[] = new Class[2];
            getType[0] = String.class;
            getType[1] = String.class;
            method = SystemPropertiesClass.getMethod("get", getType);

            Object arglist[] = new Object[2];
            arglist[0] = key;
            arglist[1] = defaultvalue;

            Object receiver = new Object();

            Object returnvalue = method.invoke(receiver, arglist);
            value = (String) returnvalue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value.trim();
    }

    /**
     * 根据应用包名获取应用版本名称
     */
    public static String getAppVersionName(Context ctx, String packageName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 获取当前运行应用包名
     *
     * @param context
     * @return
     */
    public static final int AID_APP = 10000;
    public static final int AID_USER = 100000;

    public static String getForegroundApp(Context ctx) {
        String foregroundProcess = "";
       /* File[] files = new File("/proc").listFiles();
        int lowestOomScore = Integer.MAX_VALUE;

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            int pid;
            try {
                pid = Integer.parseInt(file.getName());
            } catch (NumberFormatException e) {
                continue;
            }
            try {
                String cgroup = read(String.format("/proc/%d/cgroup", pid));
                String[] lines = cgroup.split("\n");
                String cpuSubsystem;
                String cpuaccctSubsystem;

                if (lines.length == 2) {//有的手机里cgroup包含2行或者3行，我们取cpu和cpuacct两行数据
                    cpuSubsystem = lines[0];
                    cpuaccctSubsystem = lines[1];
                }else if(lines.length==3){
                    cpuSubsystem = lines[0];
                    cpuaccctSubsystem = lines[2];
                }else {
                    continue;
                }
                if (!cpuaccctSubsystem.endsWith(Integer.toString(pid))) {
                    // not an application process
                    continue;
                }
                if (cpuSubsystem.endsWith("bg_non_interactive")) {
                    // background policy
                    continue;
                }
                String cmdline = read(String.format("/proc/%d/cmdline", pid));
                if (cmdline.contains("com.android.systemui")) {
                    continue;
                }
                int uid = Integer.parseInt(
                        cpuaccctSubsystem.split(":")[2].split("/")[1].replace("uid_", ""));
                if (uid >= 1000 && uid <= 1038) {
                    // system process
                    continue;
                }
                int appId = uid - AID_APP;
                int userId = 0;

                while (appId > AID_USER) {
                    appId -= AID_USER;
                    userId++;
                }
                if (appId < 0) {
                    continue;
                }

                File oomScoreAdj = new File(String.format("/proc/%d/oom_score_adj", pid));
                if (oomScoreAdj.canRead()) {
                    int oomAdj = Integer.parseInt(read(oomScoreAdj.getAbsolutePath()));
                    if (oomAdj != 0) {
                        continue;
                    }
                }
                int oomscore = Integer.parseInt(read(String.format("/proc/%d/oom_score", pid)));
                if (oomscore < lowestOomScore) {
                    lowestOomScore = oomscore;
                    foregroundProcess = cmdline;
                }
            } catch (IOException e) {
                //
            }
        }

        try{
            if(TextUtils.isEmpty(foregroundProcess)){
                Logger.d(TAG, " get from process is null, try get from RunningTaskInfo ");
                ActivityManager am = (ActivityManager) Application.getContext().getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> rti = am.getRunningTasks(1);
                foregroundProcess = rti.get(0).topActivity.getPackageName();
            }
        }catch (Exception e){
            Logger.d(TAG, " get from RunningTaskInfo error .");
        }*/

        try {
            foregroundProcess = getPackageName(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Logger.d(TAG, " foreground package : " + foregroundProcess);
        return foregroundProcess;
    }

    private static final long END_TIME = System.currentTimeMillis();
    private static final long TIME_INTERVAL = 7 * 24 * 60 * 60 * 1000L;
    private static final long START_TIME = END_TIME - TIME_INTERVAL;

    /**
     * 通过UsageStatsManager获取List<usagestats>集合
     */
    public static List<UsageStats> getUsageStatsList(Context context, long startTime, long endTime) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager manager = (UsageStatsManager) context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
            //UsageStatsManager.INTERVAL_WEEKLY，UsageStatsManager的参数定义了5个，具体查阅源码
            List<UsageStats> usageStatses = manager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);
            if (usageStatses == null || usageStatses.size() == 0) {// 没有权限，获取不到数据
                return null;
            }
            return usageStatses;
        }
        return null;
    }

    /**
     * 获取记录前台应用的UsageStats对象
     */
    private static UsageStats getForegroundUsageStats(Context context, long startTime, long endTime) {
        UsageStats usageStatsResult = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<UsageStats> usageStatses = getUsageStatsList(context, startTime, endTime);
            if (usageStatses == null || usageStatses.isEmpty()) return null;
            for (UsageStats usageStats : usageStatses) {
                if (usageStatsResult == null || usageStatsResult.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                    usageStatsResult = usageStats;
                }
            }
        }
        return usageStatsResult;
    }

    public static String getPackageName(Context ctx) {
        String currentClassName = "";
        try {
            if (true/*Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP*/) {
                ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
                currentClassName = manager.getRunningTasks(1).get(0).topActivity.getPackageName();
            } /*else {
                UsageStats initStat = getForegroundUsageStats(Application.getAppContext(), START_TIME, END_TIME);
                if (initStat != null) {
                    currentClassName = initStat.getPackageName();
                }
            }*/
        } catch (Exception e) {
            Logger.d(TAG, "getPackageName err: " + e.getMessage());
        }
        Logger.d(TAG, " getPackageName package : " + currentClassName);
        return currentClassName;
    }

    private static String read(String path) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        output.append(reader.readLine());
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            output.append('\n').append(line);
        }
        reader.close();
        return output.toString().trim();//不调用trim()，包名后面会带有乱码
    }

    /**
     * 判断应用是否为系统应用
     */
    public static boolean isSystemApp(Context ctx, String packageName) {
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

    /**
     * 获取application中指定的meta-data
     */
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
     * 根据路径获取图片资源
     *
     * @param path
     * @return
     */
    public static Drawable getDrawableByFilePath(String path, String fileName) {
        InputStream in = null;
        Bitmap bitmap = null;
        Drawable drawable = null;
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(fileName)) {
            return null;
        }

        try {
            File file = new File(path, fileName);
            in = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            Logger.d(TAG, " get file error, file name : " + fileName);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (bitmap != null) {
            drawable = new BitmapDrawable(bitmap);
        }

        return drawable;
    }

    /**
     * 生成二维码
     * @param url 二维码内容
     * @param qrCodeSize 二维码大小
     * @param pandding 白色边框大小
     * @param bgFlag 是否填充白色底
     * return
     */
//    public static Bitmap loadQRCode(String url, int qrCodeSize, int pandding, boolean bgFlag){
//        Bitmap bitmap = null;
//        try{
//            if(url.contains("?")){
//                url = url + "&qd=" + CommonUtil.getAppMetaData("UMENG_CHANNEL") + "&vc=" + SPUtil.getString(SPConstants.KEY_MES_LINKCODE, "");
//            }else{
//                url = url + "?qd=" + CommonUtil.getAppMetaData("UMENG_CHANNEL") + "&vc=" + SPUtil.getString(SPConstants.KEY_MES_LINKCODE, "");
//            }
//            Logger.d(TAG, "- qr code url : "+url);
//
//            int width = qrCodeSize;
//            int height = qrCodeSize;
//
//            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
//            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//            hints.put(EncodeHintType.MARGIN, pandding);  //白边框大小
//            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
//
//            //图像数据转换，使用了矩阵转换
//            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height, hints);
//            int[] pixels = new int[width * height];
//
//            //两个for循环是图片横列扫描的结果
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++) {
//                    if (bitMatrix.get(x, y)) {
//                        pixels[y * width + x] = 0xff000000;
//                    }else {
//                        if(bgFlag){
//                            pixels[y * width + x] = 0xffffffff;
//                        }
//                    }
//                }
//            }
//
//            //生成二维码图片的格式，使用ARGB_8888
//            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        return bitmap;
//    }

    /**
     * 生成中间带logo的二维码
     * @param url 二维码内容
     * @param qrCodeSize 二维码大小
     * @param logoSize 中间logo大小
     * @param pandding 白色边框大小
     * @param bgFlag 是否填充白色底
     */
//    public static Bitmap loadQRCodeWithLogo(String url, int qrCodeSize, Bitmap logo, int logoSize, int pandding, boolean bgFlag){
//        Bitmap bitmap = null;
//        try{
//            int qrLogoSize = logoSize;
//
//            Matrix m = new Matrix();
//            float sx = (float) 2 * qrLogoSize / logo.getWidth();
//            float sy = (float) 2 * qrLogoSize / logo.getHeight();
//            m.setScale(sx, sy);//设置缩放信息
//
//            //将logo图片按martix设置的信息缩放
//            logo = Bitmap.createBitmap(logo, 0, 0, logo.getWidth(), logo.getHeight(), m, false);
////            MultiFormatWriter writer = new MultiFormatWriter();
//            Hashtable hst = new Hashtable();
//            hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");//设置字符编码
//            hst.put(EncodeHintType.MARGIN, pandding);  //白边框大小
//            hst.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
//
//            int width = qrCodeSize;//矩阵高度
//            int height = qrCodeSize;//矩阵宽度
//            BitMatrix matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height, hst);//生成二维码矩阵信息
//
//            int halfW = width / 2;
//            int halfH = height / 2;
//            int[] pixels = new int[width * height];//定义数组长度为矩阵高度*矩阵宽度，用于记录矩阵中像素信息
//            for (int y = 0; y < height; y++) {//从行开始迭代矩阵
//                for (int x = 0; x < width; x++) {//迭代列
//                    if (x > halfW - qrLogoSize && x < halfW + qrLogoSize
//                            && y > halfH - qrLogoSize
//                            && y < halfH + qrLogoSize) {//该位置用于存放图片信息
//                        //记录图片每个像素信息
//                        pixels[y * width + x] = logo.getPixel(x - halfW
//                                + qrLogoSize, y - halfH + qrLogoSize); } else {
//                        if (matrix.get(x, y)) {//如果有黑块点，记录信息
//                            pixels[y * width + x] = 0xff000000;//记录黑块信息
//                        }else {
//                            if(bgFlag){
//                                pixels[y * width + x] = 0xffffffff;
//                            }
//                        }
//                    }
//                }
//            }
//
//            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        return bitmap;
//    }

    /**
     * 获取应用进程名
     */
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager == null) {
            return null;
        }

        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * 根据进程名获取进程ID
     *
     * @param processName
     * @return
     */
    public static int getProcessIDByName(Context ctx, String processName) {
        int pid = 0;
        ActivityManager mActivityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (!TextUtils.isEmpty(processName) && processName.equals(appProcess.processName)) {
                return appProcess.pid;
            }
        }

        return pid;
    }

    /**
     * 输入流转字符串
     */
    public static String inputStream2String(InputStream is, String charset) {
        String result = "";
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            int i = -1;
            while ((i = is.read()) != -1) {
                baos.write(i);
            }
            result = baos.toString(charset);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != baos) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    baos = null;
                }
            }
        }

        return result;
    }

    /**
     * 读取/proc/self/net/dev文件,获取接收到的网络数据大小
     *
     * @return long 接收到的网络数据大小
     */
    /**
     * 系统流量文件
     */
    private static final String DEV_FILE_PATH = "/proc/self/net/dev";
    /**
     * 以太网信息所在行
     */
    private static final String ETHLINE = "eth";
    /**
     * Wifi信息所在行
     */
    private static final String WIFILINE = "wlan0";
    /**
     * 数字的正则表达式
     */
    private static final String DIGITAL_REGEX = "^[0-9]*$";

    public static long getNetDataFromDev() {
        long receiveData = 0;
        FileReader fileReader = null;
        BufferedReader in = null;

        try {
            fileReader = new FileReader(DEV_FILE_PATH);
            in = new BufferedReader(fileReader);

            String line;
            while ((line = in.readLine()) != null) {
                /** 以":"号进行分割字符串 */
                String[] segs = line.trim().split(":");
                /** segs数组的长度 */
                int segsLen = 2;
                if (line.contains(ETHLINE) || line.contains(WIFILINE)) {
                    if (segs != null && segs.length == segsLen) {
                        String[] netdata = segs[segsLen - 1].trim().split(" ");
                        if (netdata != null && netdata.length > 0) {
                            String data = netdata[0];
                            /** 验证是否是数字 */
                            if (regexMatcher(DIGITAL_REGEX, data)) {
                                receiveData += Long.parseLong(data);
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Logger.d(TAG, "Could not read " + DEV_FILE_PATH);
        } catch (IOException e) {
            Logger.d(TAG, "IOException --->" + e.toString());
        } finally {
            try {
                in.close();
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return receiveData;
    }

    /**
     * 根据所给的正则表达式进行匹配
     *
     * @param regex 正则表达式
     * @param value 验证的值
     * @return boolean
     */
    public static boolean regexMatcher(String regex, String value) {
        if (regex == null || value == null) {
            Logger.d(TAG, "Matcher regex or value is null");
            return false;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);

        return matcher.matches();
    }

    /**
     * 判断是否是小米mini盒子
     *
     * @return
     */
    public static boolean isXiaomiMini() {
        String model = new Build().MODEL;
        if (!TextUtils.isEmpty(model) && model.contains("MiBOX_mini")) {
            return true;
        }
        return false;
    }

//    /**
//     * 解压ZIP包
//     * @param zipFile zip包文件
//     * @param destPath 解压到的目标目录
//     */
//    public static void zipDecompressing(final File zipFile, final String destPath, final String soVersion){
//        new Thread(){
//            @Override
//            public void run() {
//                File oldFfmpegFile = new File("/data/data/"+Application.getAppContext().getPackageName()+"/libijkffmpeg.so");
//                File oldSdlFile = new File("/data/data/"+Application.getAppContext().getPackageName()+"/libijksdl.so");
//                File oldPlayerFile = new File("/data/data/"+Application.getAppContext().getPackageName()+"/libijkplayer.so");
//                File oldMd5File = new File("/data/data/"+Application.getAppContext().getPackageName()+"/md5.json");
//                if(oldFfmpegFile.exists()){
//                    oldFfmpegFile.delete();
//                }
//                if(oldSdlFile.exists()){
//                    oldSdlFile.delete();
//                }
//                if(oldPlayerFile.exists()){
//                    oldPlayerFile.delete();
//                }
//                if(oldMd5File.exists()){
//                    oldMd5File.delete();
//                }
//
//                ZipInputStream Zin = null;
//                BufferedInputStream Bin = null;
//
//                try {
//                    Zin = new ZipInputStream(new FileInputStream(zipFile.getAbsolutePath()));//输入源zip路径
//                    Bin = new BufferedInputStream(Zin);
//                    ZipEntry entry;
//
//                    try {
//                        while((entry = Zin.getNextEntry()) != null && !entry.isDirectory()){
//                            File Fout = new File(destPath,entry.getName());
//                            if(!Fout.exists()){
//                                (new File(Fout.getParent())).mkdirs();
//                            }
//
//                            FileOutputStream out = null;
//                            BufferedOutputStream Bout = null;
//                            try {
//                                out = new FileOutputStream(Fout);
//                                Bout = new BufferedOutputStream(out);
//                                int b;
//                                while((b = Bin.read()) != -1){
//                                    Bout.write(b);
//                                }
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }finally {
//                                if(Bout != null){
//                                    Bout.close();
//                                }
//                                if(out != null){
//                                    out.close();
//                                }
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }finally {
//                    if(Bin != null){
//                        try {
//                            Bin.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    if(Zin != null){
//                        try {
//                            Zin.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                Logger.d(TAG, " zip decompress success, delete zip file ");
//                zipFile.delete(); //解压完成后删除zip包
//
//                File newFfmpegFile = new File("/data/data/"+Application.getAppContext().getPackageName()+"/libijkffmpeg.so");
//                File newSdlFile = new File("/data/data/"+Application.getAppContext().getPackageName()+"/libijksdl.so");
//                File newPlayerFile = new File("/data/data/"+Application.getAppContext().getPackageName()+"/libijkplayer.so");
//                File md5File = new File("/data/data/"+Application.getAppContext().getPackageName()+"/md5.json");
//                String md5Str = null;
//                if(md5File.exists()){
//                    md5Str = getFromFile(md5File);
//                }
//                boolean isSaveVersion = true;
//
//                if(!TextUtils.isEmpty(md5Str)){
//                    try {
//                        PlayerFileMD5Json md5Json = JsonUtil.getInstance().fromJson(md5Str, PlayerFileMD5Json.class);
//                        if(md5Json != null){
//                            String ffmpegMd5 = md5Json.getIjkffmpeg();
//                            String sdlMd5 = md5Json.getIjksdl();
//                            String playerMd5 = md5Json.getIjkplayer();
//
//                            if(!TextUtils.isEmpty(ffmpegMd5) && !ffmpegMd5.equalsIgnoreCase(getFileMD5(newFfmpegFile))){
//                                Logger.d(TAG, " libijkffmpeg check md5 failed delete .");
//                                newFfmpegFile.delete();
//                                isSaveVersion = false;
//                            }
//
//                            if (!TextUtils.isEmpty(sdlMd5) && !sdlMd5.equalsIgnoreCase(getFileMD5(newSdlFile))) { //MD5检验失败，删除文件
//                                Logger.d(TAG, " libijksdl check md5 failed delete .");
//                                newSdlFile.delete();
//                                isSaveVersion = false;
//                            }
//
//                            if (!TextUtils.isEmpty(playerMd5) && !playerMd5.equalsIgnoreCase(getFileMD5(newPlayerFile))) { //MD5检验失败，删除文件
//                                Logger.d(TAG, " libijkplayer check md5 failed delete .");
//                                newPlayerFile.delete();
//                                isSaveVersion = false;
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//
//                if(isSaveVersion){
//                    SPUtil.putString(SPConstants.KEY_PLAYERSO_VERSION, soVersion);
//                }
//            }
//        }.start();
//    }

    /**
     * 读取本地文件 <功能详细描述>
     *
     * @param fileName
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     */
    public static String getFromFile(File fileName) {
        if (fileName == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream is = new FileInputStream(fileName);
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从文件中获取
     *
     * @return
     */
    public static String getFromFile(String filePath, String charSet) {
        StringBuilder str = new StringBuilder();
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tmp = "";
            while ((tmp = reader.readLine()) != null) {
                str.append(tmp + "\n");
            }
        } catch (IOException e) {
            Logger.e(TAG, e.toString());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Logger.e(TAG, e.toString());
            }
        }
        return str.toString();
    }

    public static String getException(Throwable t) {
        if (t != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("msg : ");
            sb.append(t.getMessage() + "\n");
            sb.append("Caused by : ");

            StackTraceElement[] stackTrace = t.getStackTrace();
            if (stackTrace != null) {
                for (int i = 0; i < stackTrace.length; i++) {
                    sb.append("\tat ");
                    sb.append(stackTrace[i].toString());
                    sb.append("\n");
                }
            }

            return sb.toString();
        }

        return "";
    }

    /**
     * 将隐式intent转换为显示intent
     */
    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() < 1) {
            Logger.d(TAG, "-- resolveInfo is null or size is 0, return null");
            return null;
        }

        Logger.d(TAG, "-- resolveInfo : " + resolveInfo);
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    /**
     * 从工程Asset目录下拷贝文件到指定目录
     *
     * @param context
     * @param filename        Asset目录下要拷贝的文件名字
     * @param destinationPath 要拷贝到的目录
     */
    public static void copyFileFormAsset(Context context, String filename, String destinationPath) {
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open(filename);
            String newFileName = destinationPath + "/" + filename;
            out = new FileOutputStream(newFileName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    /**
//     * base64 解密
//     * @param str
//     * @return
//     */
//    public static String getFromBase64(String str) {
//        byte[] b = null;
//        String result = null;
//
//        if (str != null) {
//            BASE64Decoder decoder = new BASE64Decoder();
//            try {
//                b = decoder.decodeBuffer(str);
//                result = new String(b, "utf-8");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return result;
//    }

    public static void startService(Context ctx, Bundle bundle, Class clzz) {
        Intent intent = new Intent(ctx, clzz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        ctx.startService(intent);
    }

    public static boolean isMainProcess(Context ctx) {
        String curProcessName = getCurProcessName(ctx);
        if (ctx.getPackageName().equals(curProcessName)) {
            return true;
        }
        return false;
    }

    public static boolean isXiaoMiOrMagic() {
        String model = new Build().MODEL;
        if (!TextUtils.isEmpty(model)) {
            if (model.contains("MiBOX") || model.contains("Magic")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从asset中获得数据 <功能详细描述>
     *
     * @param fileName
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     */
    public static String getFromAsset(Context ctx, String fileName) {
        if (fileName == null) {
            return null;
        }
        String result = "";
        InputStream in = null;
        try {
            in = ctx.getResources().getAssets().open(fileName); // 从Assets中的文件获取输入流
            int length = in.available(); // 获取文件的字节数
            byte[] buffer = new byte[length]; // 创建byte数组
            in.read(buffer); // 将文件中的数据读取到byte数组中
            result = new String(buffer, "UTF-8"); // 将byte数组转换成指定格式的字符串
        } catch (IOException e) {
            e.printStackTrace(); // 捕获异常并打印
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

//    public static boolean isSupportInput() {
//        if("seecloud".equals(PackageUtil.getAppMetaData("UMENG_CHANNEL"))){
//            return true;
//        }
//        boolean isSystemApp = PackageUtil.isSystemApp(Application.getAppContext().getPackageName());
//        boolean accessInput = InputEventUtils.checkPermission();
//
//        return accessInput || isSystemApp || CommonUtil.isXiaoMiOrMagic();
//    }

    /**
     * 获取文件的md5值
     *
     * @param file
     * @return
     */
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        byte[] md5sum = digest.digest();
        return toHex(md5sum);
    }

    private static String toHex(byte[] buffer) {
        StringBuffer sb = new StringBuffer(buffer.length * 2);
        for (int i = 0; i < buffer.length; i++) {
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
        }
        return sb.toString();
    }

    /**
     * 保存字符串到文件
     *
     * @param path
     * @param fileName
     * @param strValue
     */
    public static void saveToFile(String path, String fileName, String strValue) {
        if (path == null || strValue == null) {
            return;
        }

        File fileDir = new File(path);
        try {
            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File fileJson = new File(path + "/" + fileName);
        if (fileJson.exists()) {
            fileJson.delete();
        }

        if (!fileJson.exists()) {
            try {
                fileJson.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(fileJson, "rw");
            raf.seek(0);
            raf.write(strValue.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将bitmap文件保存到本地文件中
     *
     * @param bitmap:源文件
     * @param dirPath：文件路径
     * @param picName：文件名
     * @return void [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     */
    public static void saveBitmap(Bitmap bitmap, String dirPath, String picName) {
        if (bitmap == null) {
            return;
        }

        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(dirPath, picName);
        if (file.exists()) {
            file.delete();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 对字符串进行MD5加密转换
     *
     * @param originString
     * @return
     */
    public static String encodeByMD5(String originString) {
        if (!TextUtils.isEmpty(originString)) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                //使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
                byte[] results = md.digest(originString.getBytes());
                //将得到的字节数组变成字符串返回
                String resultString = byteArrayToHexString(results);
                String pass = resultString.toUpperCase();
                return pass;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private final static String[] hexDigits = {"0", "1", "2", "3", "4",
            "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * 连接指定wifi
     *
     * @param ssid       wifi名称
     * @param password   wifi密码
     * @param capability wifi加密方式，无密码时可传null
     */
    public static void connectWifi(Context ctx, String ssid, String password, String capability) {
        Logger.d(TAG, "connectWifi ssid : " + ssid + "; password : " + password + "; capability : " + capability);

        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        //指定对应的SSID
        config.SSID = "\"" + ssid + "\"";

        //如果之前有类似的配置,则先清除
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (int i = 0; i < configs.size(); i++) {
                if (config.SSID.equals("\"" + ssid + "\"")) {
                    wifiManager.removeNetwork(configs.get(i).networkId);
                    break;
                }
            }
        }

        if (TextUtils.isEmpty(password)) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); //不需要密码
        } else {
            if (TextUtils.isEmpty(capability) && capability.contains("WPA")) { //以WPA或WPA2加密
                config.preSharedKey = "\"" + password + "\"";
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.status = WifiConfiguration.Status.ENABLED;
            } else if (TextUtils.isEmpty(capability) && capability.contains("WEP")) { //以WEP加密
                config.hiddenSSID = true;
                config.wepKeys[0] = "\"" + password + "\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
            } else { //默认加密方式为WPA或WPA2
                config.preSharedKey = "\"" + password + "\"";
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.status = WifiConfiguration.Status.ENABLED;
            }
        }

        int networkId = wifiManager.addNetwork(config);
        boolean enable = wifiManager.enableNetwork(networkId, true);
        boolean reconnect = wifiManager.reconnect();
        Logger.d(TAG, "connectWifi networkId : " + networkId + "; enable : " + enable + "; reconnect : " + reconnect);
    }

    /**
     * 检测网络是否可用
     *
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * 日期转化为字符串
     */
    public static String dealTime(Object date) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern,
                Locale.getDefault());
        return sdf.format(date);
    }
}
