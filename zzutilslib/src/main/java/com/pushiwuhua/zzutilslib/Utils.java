package com.pushiwuhua.zzutilslib;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.text.TextUtils.isEmpty;

/**
 * Utils 常用工具类
 * @deprecated  wzz 这个类里的方法会逐步迁移到扩展方法内，也会逐步用Kotlin改写
 * wzz created at 2016/2/18 13:45
 */
public class Utils {

    public final static String userinfo = "userinfo_pref";

    /**
     * 检测是否有网络连接，如果无网络连接，弹出“网络未连接提示”
     *
     * @return 是否网络连接
     */
    public static boolean isNetworkAvailableWithTip(Context context) {
        boolean flag = checkNetworkState(context);
        if (!flag) {
            Toast.makeText(context, "网络未连接", Toast.LENGTH_SHORT).show();
        }
        return flag;
    }

    /**
     * 检测是否有网络连接,并返回结果，无任何界面提示
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        return checkNetworkState(context);
    }

    /**
     * 判断wifi是否连接
     */
    public static boolean isWifiAvailable(Context context) {
        boolean isWifiConnected = false;
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);//得到网络连接信息
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            isWifiConnected = networkInfo.isConnected();
        }
        return isWifiConnected;
    }

    /**
     * 判断网络是否连接
     */
    public static boolean checkNetworkState(Context context) {
        boolean isConnect = false;
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);//得到网络连接信息
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null) {
            isConnect = networkInfo.isAvailable();//去进行判断网络是否连接
        }
        return isConnect;
    }

    /**
     * 网络未连接时，弹出提示框提示用户调用设置方法
     */
    private static void showSetNetworkDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setIcon(R.drawable.logo);
        builder.setTitle("网络提示信息");
        builder.setMessage("网络不可用，如果继续，请先设置网络！");
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                /**
                 * 判断手机系统的版本！如果API大于10 就是3.0+
                 * 因为3.0以上的版本的设置和3.0以下的设置不一样，调用的方法不同
                 */
                if (Build.VERSION.SDK_INT > 10) {
                    intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                } else {
                    intent = new Intent();
                    ComponentName component = new ComponentName(
                            "com.android.settings",
                            "com.android.settings.WirelessSettings");
                    intent.setComponent(component);
                    intent.setAction("android.intent.action.VIEW");
                }
                context.startActivity(intent);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create();
        builder.show();
    }


    /**
     * 隐藏软键盘
     *
     * @param context 上下文对象
     */
    public static void hideKeyBoard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 隐藏软键盘
     *
     * @param context 上下文对象
     */
    public static void forceHideKeyBoard(Context context, View v) {
        InputMethodManager imm = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    /**
     * 强制弹出软键盘，传递一个EditText控件进去
     *
     * @param targetView
     */
    public static void forceShowKeyBoard(View targetView) {
        InputMethodManager imm = (InputMethodManager) targetView.getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(targetView, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 从网络上获取内容文本
     *
     * @param path 文本路径
     * @return
     * @throws Exception
     */
    public static List<String> readTextCotant(String path) throws Exception {
        List<String> list = new ArrayList<String>();
        String string = null;
        BufferedReader buffer = null;
        URL url;
        HttpURLConnection hconn = null;

        url = new URL(path);
        hconn = (HttpURLConnection) url.openConnection();
        hconn.setConnectTimeout(5 * 1000);
        hconn.setReadTimeout(5 * 1000);
        buffer = new BufferedReader(new InputStreamReader(
                hconn.getInputStream(), "GBK"));
        while ((string = buffer.readLine()) != null) {
            int i = string.indexOf("\r\n");
            if (i != -1) {
                list.add(string.substring(0, i));
            } else {
                list.add(string);
            }

        }
        buffer.close();
        return list;
    }

    //读取文本文件中的内容
    public static String readTxtFile(String strFilePath) {
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }

    /**
     * 从assets目录中读取文本内容
     *
     * @param context
     * @param fileName 资源文件名
     * @return
     */
    public static String readContentFromAssets(Context context, String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 创建一条图片地址uri,用于保存拍照后的照片
     *
     * @param context
     * @return 图片的uri
     */
    public static Uri createImagePathUri(Context context) {
        Uri imageFilePath = null;
        String status = Environment.getExternalStorageState();
        SimpleDateFormat timeFormatter = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.CHINA);
        long time = System.currentTimeMillis();
        String imageName = timeFormatter.format(new Date(time));
        Log.i("", "生成的照片输出路径111：" + imageName);
        // ContentValues是我们希望这条记录被创建时包含的数据信息
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, time);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
            imageFilePath = context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            imageFilePath = context.getContentResolver().insert(
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        }
        Log.i("", "生成的照片输出路径：" + imageFilePath);
        return imageFilePath;
    }

    /**
     * 对字符串进行md5加密
     *
     * @param string
     * @return
     */
    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));

        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException("Huh, MD5 should be supported?", e);

        } catch (UnsupportedEncodingException e) {

            throw new RuntimeException("Huh, UTF-8 should be supported?", e);

        }

        StringBuilder hex = new StringBuilder(hash.length * 2);

        for (byte b : hash) {

            if ((b & 0xFF) < 0x10) hex.append("0");

            hex.append(Integer.toHexString(b & 0xFF));

        }
        return hex.toString();

    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param context
     * @param dpValue 要转换的dp值
     */
    public static int dip2px(Context context, float dpValue) {
        if (context != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        } else {
            return 0;
        }
    }

    /**
     * 将 sp 转换为 px， 保证尺寸大小不变
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int sp2px(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue * fontScale + 0.5f);
    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();
        return bp;
    }

    /**
     * 获取当前屏幕截图，包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;
    }

    /**
     * 检查某APK是否存在
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean checkApkExist(Context context, String packageName) {
        if (isEmpty(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 获取manifest meta值  用处之一: 百度推送获取api key
     *
     * @param context
     * @param metaKey
     * @return wzz
     */
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(AiLog.TAG_WZZ, "getMetaValue error " + e.getMessage());
        }
        return apiKey;
    }

    /**
     * 判断应用是否已经启动
     *
     * @param context     一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName) {
        ActivityManager activityManager =
                (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processInfos.size(); i++) {
            if (processInfos.get(i).processName.equals(packageName)) {
                Log.i("NotificationLaunch",
                        String.format("the %s is running, isAppAlive return true", packageName));
                return true;
            }
        }
        Log.i("NotificationLaunch",
                String.format("the %s is not running, isAppAlive return false", packageName));
        return false;
    }

    /**
     * 检测是否有emoji表情
     *
     * @param source
     * @return
     */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (!isEmojiCharacter(codePoint)) {
                //如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否是Emoji
     *
     * @param codePoint 比较的单个字符
     * @return
     */
    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) ||
                (codePoint == 0xA) || (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000));
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
//        if (drawable instanceof BitmapDrawable) {
//            BitmapDrawable db = (BitmapDrawable) drawable;
//            if(db.getBitmap() != null){
//                db.getBitmap().recycle();
//            }
//        }
        return bitmap;
    }

    public static Drawable zoomDrawableDp(Context context, Drawable drawable, int wdp, int hdp) {
        int w = dip2px(context, wdp);
        int h = dip2px(context, hdp);
        return zoomDrawable(context, drawable, w, h);
    }

    public static Drawable zoomDrawable(Context context, Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap oldbmp = drawableToBitmap(drawable); // drawable 转换成 bitmap
        Matrix matrix = new Matrix();   // 创建操作图片用的 Matrix 对象
        float scaleWidth = ((float) w / width);   // 计算缩放比例
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);         // 设置缩放比例
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);       // 建立新的 bitmap ，其内容是对原 bitmap 的缩放后的图
        oldbmp.recycle();
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), newbmp);
        return bd;  // 把 bitmap 转换成 drawable 并返回
    }

    /**
     * 抓取屏幕截图存入文件路径
     * <p>
     * wzz created at 2016/3/8 16:27
     */
    public static void captureScreenToFile(String fileName) {
//        String cmd = "screencap -p " + fileName;
        String cmd = "screencap -p /storage/emulated/legacy/ttt2.png";
//        String cmd = "screencap -v";
        String result = android_command(cmd);
//            AiLog.i(AiLog.TAG_WZZ, "Utils captureScreenToFile ok:" + fileName);

    }

    /**
     * 执行shell命令
     * <p>
     * wzz created at 2016/3/8 16:24
     */
    public static void execCommand(String command) throws IOException {
//        AiLog.i(AiLog.TAG_WZZ, "Utils execCommand:" + command);
        // start the ls command running
        //String[] args =  new String[]{"sh", "-c", command};
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);        //这句话就是shell与高级语言间的调用
        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line).append("-");
            }

//            AiLog.i(AiLog.TAG_WZZ, "Utils execCommand result:" + stringBuffer.toString());

        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }

    public static String android_command(String cmd) {
        String[] arr = cmd.split(" ");
        try {
            return run(cmd, "/system/bin");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 执行一个shell命令，并返回字符串值
     *
     * @param cmd           命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}）
     * @param workdirectory 命令执行路径（例如："system/bin/"）
     * @return 执行结果组成的字符串
     * @throws IOException
     */
    public static synchronized String run(String cmd, String workdirectory)
            throws IOException {
        StringBuffer result = new StringBuffer();
        try {
            // 创建操作系统进程（也可以由Runtime.exec()启动）
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(cmd);
            DataOutputStream os = null;
            DataInputStream is = null;
//            os = new DataOutputStream(proc.getOutputStream());
            is = new DataInputStream(proc.getInputStream());
//            os.writeBytes(cmd + "\n");
//            os.writeBytes("exit\n");
//            os.flush();

            byte[] re = new byte[1024];
            while (is.read(re) != -1) {
                result = result.append(new String(re));
            }
            // 关闭输入流
            is.close();

            //			ProcessBuilder builder = new ProcessBuilder(cmd);
            //			InputStream in = null;
            //			// 设置一个路径（绝对路径了就不一定需要）
            //			if (workdirectory != null) {
            //				// 设置工作目录（同上）
            //				builder.directory(new File(workdirectory));
            //				// 合并标准错误和标准输出
            //				builder.redirectErrorStream(true);
            //				// 启动一个新进程
            //				Process process = builder.start();
            //				// 读取进程标准输出流
            //				in = process.getInputStream();
            //				byte[] re = new byte[1024];
            //				while (in.read(re) != -1) {
            //					result = result.append(new String(re));
            //				}
            //			}
            //			// 关闭输入流
            //			if (in != null) {
            //				in.close();
            //			}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }

    /**
     * 给定图片维持宽高比缩放后，截取正中间的正方形部分
     *
     * @param bitmap     原图
     * @param edgeLength 希望得到的正方形部分的边长
     * @return 缩放截取正中部分后的位图。
     */
    public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength) {
        if (null == bitmap || edgeLength <= 0) {
            return null;
        }
        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();

        if (widthOrg > edgeLength && heightOrg > edgeLength) {
            //压缩到一个最小长度是edgeLength的bitmap
            int longerEdge = edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg);
            int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;

            try {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
            } catch (Exception e) {
                return null;
            }

            //从图中截取正中间的正方形部分。
            int xTopLeft = (scaledWidth - edgeLength) / 2;
            int yTopLeft = (scaledHeight - edgeLength) / 2;

            try {
                result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
                scaledBitmap.recycle();
            } catch (Exception e) {
                return null;
            }
        }
        return result;
    }

    public static void copyInputStreamToFile(InputStream inputStream, String savaPath) {
//        AiLog.i(AiLog.TAG_WZZ, "Utils copyInputStreamToFile savePath:" + savaPath);
        if (inputStream == null || isEmpty(savaPath)) {
            return;
        }
        InputStream stream = inputStream;
        FileOutputStream output = null;
        try {
            File file = new File(savaPath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            output = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            output.close();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存数据到文件
     * <p>
     * wzz created at 2016/5/11 15:23
     */
    public static void saveByteArrToFile(byte[] data, String path) {
        if (data == null) {
            return;
        }
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            //建立输出字节流
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();//关闭输出流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 存储图片到路径
     * <p>
     * wzz created at 2016/5/11 16:21
     */
    public static void saveBitmap(Bitmap bm, String savePath) {
        if (isEmpty(savePath) || bm == null) {
            return;
        }
        try {
            File file = new File(savePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream out = new FileOutputStream(savePath);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            System.out.println("file " + savePath + "output done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveBitmapQuality(Bitmap bm, int quality, String savePath) {
        if (isEmpty(savePath) || bm == null) {
            return;
        }
        try {
            File file = new File(savePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream out = new FileOutputStream(savePath);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, out);
            System.out.println("file " + savePath + "output done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 获取存储卡上原图
     *
     * @param path
     * @return
     */
    public static Bitmap getLocalBitmap(String path) {
        Bitmap bitmap = null;
        try {
            File e = new File(path);
            if (e.exists()) {
                bitmap = BitmapFactory.decodeFile(path);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return bitmap;
    }

    /**
     * 对Bitmap进行缩放
     * <p>
     * wzz created at 2016/5/28 14:50
     */
    public static Bitmap getBitmapZoom(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = (float) newWidth / width;
        float scaleHeight = (float) newHeight / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newBm;
    }

    public static Bitmap getBitmapZoomDp(Context context, Bitmap bm, int wdp, int hdp) {
        int newWidth = dip2px(context, wdp);
        int newHeight = dip2px(context, hdp);
        return getBitmapZoom(bm, newWidth, newHeight);
    }

    /**
     * 将数据存储为文件
     *
     * @param data
     * @param savePath
     * @param saveName
     */
    public static void saveFile(byte[] data, String savePath, String saveName) {
        String fname = savePath + saveName;
        try {
            File file = new File(fname);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(fname);
            out.write(data);
            out.flush();
            out.close();
            System.out.println("file " + fname + "output done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将base64编码格式的图片存储
     * wzz created at 2018/6/4 15:14
     */
    public static void savebase64ToFile(String base64Str, String saveDir, String saveName, String imageType) {
        if (isEmpty(saveDir) || isEmpty(saveName) || isEmpty(base64Str) || isEmpty(imageType)) {
            return;
        }
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(base64Str, Base64.DEFAULT);
            saveByteArrToFile(bitmapArray, saveDir + "/" + saveName + "." + imageType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对Bitmap进行存储
     * <p>
     * wzz created at 2016/5/28 14:53
     */
    public static void saveBitmapToFile(Bitmap bm, String saveDir, String saveName, String imageType) {
        try {
            String fname = saveDir + "/" + saveName + "." + imageType;
            File file = new File(fname);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 把压缩后的数据存放到baos中
            if (imageType.trim().toLowerCase().equalsIgnoreCase("png"))
                bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            else if ((imageType.trim().toLowerCase().equalsIgnoreCase("jpeg")) ||
                    (imageType.trim().toLowerCase().equalsIgnoreCase("jpg"))) {
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
            System.out.println("file " + fname + "output done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {
        }
        return verCode;
    }

    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {
        }
        return verName;
    }

    public static String getAppName(Context context) {
        String verName = context
                .getResources()
                .getText(
                        context.getResources().getIdentifier("app_name",
                                "string", context.getPackageName())).toString();
        return verName;
    }

    public static String getStringFromAssets(Context c, String fileName) {
        String Result = "";
        try {
            InputStreamReader inputReader = new InputStreamReader(c.getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";

            while ((line = bufReader.readLine()) != null)
                Result = Result + line;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result;
    }

    public static String getPreferenceString(Context c, String key) {
        SharedPreferences preferences = c.getSharedPreferences(userinfo,
                0);
        if (preferences != null) {
            return preferences.getString(key, "");
        } else {
            return "";
        }
    }

    public static void savePreferenceString(Context c, String key, String value) {
        SharedPreferences preferences = c.getSharedPreferences(userinfo,
                0);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    public static void copy(Context c, String ASSETS_NAME, String savePath, String saveName) {
        String filename = savePath + "/" + saveName;
        File dir = new File(savePath);

        if (!dir.exists())
            dir.mkdir();
        try {
//            if (!new File(filename).exists()) {
            InputStream is = c.getResources().getAssets().open(ASSETS_NAME);
            FileOutputStream fos = new FileOutputStream(filename);
            byte[] buffer = new byte[7168];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.close();
            is.close();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(Context c, String pathSrc, String savePath, String saveName) {
        String filename = savePath + "/" + saveName;
        File dir = new File(savePath);
        if (!dir.exists())
            dir.mkdir();
        try {
            if (!new File(filename).exists()) {
                InputStream is = new FileInputStream(pathSrc);
                FileOutputStream fos = new FileOutputStream(filename);
                byte[] buffer = new byte[7168];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap bytesToBimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return null;
    }

    /**
     * 将图片的exif信息从A拷贝写入到B
     *
     * @param fromPath
     * @param toPath
     */
    public static void copyExifInfo(String fromPath, String toPath) {
        //写入照片信息到文件
        ExifInterface exifFrom = null;// 获取图片Exif
        ExifInterface exifTo = null;// 获取图片Exif
        try {
            exifFrom = new ExifInterface(fromPath);
            exifTo = new ExifInterface(toPath);
            String tTag = exifFrom.getAttribute(ExifInterface.TAG_DATETIME);
            String aTag = exifFrom.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
            String latTag = exifFrom.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String lngTag = exifFrom.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
//            AiLog.i(AiLog.TAG_WZZ, "Utils copyExifInfo:" + tTag + "@" + aTag + "@" + latTag + "@" + lngTag);
            if (!isEmpty(tTag)) {
                exifTo.setAttribute(ExifInterface.TAG_DATETIME, tTag);
            }
            if (!isEmpty(aTag)) {
                exifTo.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, aTag);
            }
            if (!isEmpty(latTag)) {
                exifTo.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latTag);
            }
            if (!isEmpty(lngTag)) {
                exifTo.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lngTag);
            }
            exifTo.saveAttributes();
//            AiLog.i(AiLog.TAG_WZZ, "Utils copyExifInfo 写入exif信息成功:" + exifFrom.getAttribute(ExifInterface.TAG_DATETIME));
        } catch (Exception e) {
            e.printStackTrace();
//            AiLog.i(AiLog.TAG_WZZ, "Utils copyExifInfo 写入exif信息失败:" + e.getMessage());
        }
    }

    public static void setViewBg(View view, Drawable drawable) {
        if (view == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    /**
     * @param number 传入的粉丝数等、超过1W就以万为单位如   120000 = 12万
     * @return
     */
    public static String formatNumber(int number) {
        String text = "";
        if (number <= 9999)
            text = number + "";
        else {
            BigDecimal d1 = new BigDecimal(number);
            BigDecimal d2 = new BigDecimal(10000);
            BigDecimal d3 = d1.divide(d2);
            text = d3.toString() + "万";
        }
        return text;
    }


    /**
     * @param loginName 传入11位电话号码
     * @return 135*****666格式字符串
     */
    public static String formatPhoneNumber(String loginName) {
        if (!isEmpty(loginName) && loginName.length() == 11) {
            String showStr = loginName.substring(0, 3) + "*****" + loginName.subSequence(8, 11);
            return showStr;
        } else
            return "";
    }

    public static String formatMeter(double meter) {
        String result = "";// 最终返回结果
        String flag = String.valueOf(meter);// 转成字符串
        boolean isNegativeNumber = false; // 是否为负数
        if (flag.contains("-"))
            isNegativeNumber = true; // 包含负号则为负数

        double meter1 = Math.abs(meter); //取绝对值
        if (meter1 >= 0 && meter1 < 1000) {
            if (isNegativeNumber)
                result = "-" + String.valueOf((int) meter1) + " m";
            else
                result = String.valueOf((int) meter1) + " m";
        } else if (meter1 >= 1000) {
            if (isNegativeNumber)
                result = "-" + keepTwoBitNumber(meter1) + " km";
            else
                result = keepTwoBitNumber(meter1) + " km";
        }
        return result;
    }

    /**
     * 保留一位小数
     *
     * @param number 一个double数据
     * @return 按四舍五入保留的一位小数
     */
    public static String keepTwoBitNumber(double number) {
        String result = "";
        if (number >= 1000) {
            BigDecimal number1 = new BigDecimal(number);
            BigDecimal number2 = new BigDecimal(1000);
            BigDecimal re = number1.divide(number2);
            if (!re.toString().contains(".")) {
                result = re.toString();
            } else {
                double d = re.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                String kmString = String.valueOf(d);
                String[] array2 = kmString.split("\\.");
                if (array2[1].indexOf(array2[1].length()) == 0) {
                    result = String.valueOf((int) d);
                } else {
                    result = String.valueOf(d);
                }
            }
        }
        return result;
    }

    /**
     * 格式化速度，保留1位小数
     *
     * @param speed
     * @return
     */
    public static String formatSpeed(double speed) {
        String result = "";
        BigDecimal d1 = new BigDecimal(speed);
        BigDecimal d2 = d1.setScale(1, BigDecimal.ROUND_HALF_UP);
        String d2String = d2.toString();
        if (d2String.substring(d2String.length() - 1, d2String.length()).equals("0")) {
            result = d2String.substring(0, d2String.length() - 2);
        } else {
            result = d2String;
        }
        return result + " km/h";
    }

    public static boolean checkPermission(Activity activity, String permissonStr) {
        int resCode = activity.getPackageManager().checkPermission(permissonStr, activity.getPackageName());
        boolean fla2g = ActivityCompat.shouldShowRequestPermissionRationale(activity, permissonStr);
        boolean result = PackageManager.PERMISSION_GRANTED == resCode;
//        AiLog.i(AiLog.TAG_WZZ, "Utils checkPermission:" + result + "," + permissonStr);
//        AiLog.i(AiLog.TAG_WZZ, "Utils checkPermission:" + fla2g);
//        AiLog.i(AiLog.TAG_DLX, "ContactUtil checkXmlPermission: " + result);
        if (!result) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean flag = ActivityCompat.shouldShowRequestPermissionRationale(activity, permissonStr);
//                AiLog.d(AiLog.TAG_DLX, "ContactUtil askPermission() flag=" + flag);
                if (!flag) {
//                    // 此处可以弹窗或用其他方式向用户解释需要该权限的原因
//                    AiMessage.showToast("未获得通讯录读取权限");
                }
                // 无需解释，直接请求权限
                else {
                    ActivityCompat.requestPermissions(activity, new String[]{permissonStr},
                            0);
                }
            }
        }
        return result;
    }

    public static void cleanDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return;
        }

        if (!directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            forceDelete(file);
        }
    }


    public static void forceDelete(File file) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            file.delete();
        }
    }

    public static void deleteDirectory(File directory) {
        if (!directory.exists()) {
            return;
        }
        cleanDirectory(directory);
        if (!directory.delete()) {
        }
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobiles) {
        String telRegex = "[1][1-9]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为1-9中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        return !isEmpty(mobiles) && mobiles.matches(telRegex);
    }

    /**
     * 验证北斗卡号
     */
    public static boolean isBeidouNO(String bdcode) {
        String telRegex = "\\d{1,8}";//数字, 最少匹配1次,最多匹配8次
        return !isEmpty(bdcode) && bdcode.matches(telRegex) && Integer.valueOf(bdcode) <= Integer.parseInt("FFFFFF", 16);
    }

    /**
     * 判断app在前台还是后台,wzz封装已测, 此函数速度很快,毫秒数在个位数
     */
    public static boolean checkAppBackground(Context context) {
        if (context != null) {
            ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses != null && appProcesses.size() > 0) {
                for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                    if (appProcess.processName.equals(context.getPackageName())) {
                        return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
                    }
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 判断是否在系统忽略电量优化的白名单内
     */
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            boolean isBatteryOpt = pm.isIgnoringBatteryOptimizations(context.getPackageName());
//            AiLog.i(AiLog.TAG_WZZ, "Utils isIgnoringBatteryOptimizations 白名单权限:" + isBatteryOpt);
            return isBatteryOpt;
        } else {
//            AiLog.i(AiLog.TAG_WZZ, "Utils isIgnoringBatteryOptimizations 白名单权限有效");
            return true;
        }
    }

    /**
     * 获取图片旋转角度
     */
    public static int getImageRotate(String imagePath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
            int oritention = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            if (oritention == ExifInterface.ORIENTATION_NORMAL) {
                return 0;
            } else if (oritention == ExifInterface.ORIENTATION_ROTATE_90) {
                return 90;
            } else if (oritention == ExifInterface.ORIENTATION_ROTATE_180) {
                return 180;
            } else if (oritention == ExifInterface.ORIENTATION_ROTATE_270) {
                return 270;
            } else {
                return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param rotate 旋转角度，可正可负
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float rotate) {
        if (origin == null) {
            return null;
        }

        if (rotate == 0) {
            return origin;
        }

        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(rotate);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 通知系统更新扫描新的图片
     *
     * @param newImagePath 新的图片地址
     */
    public static void updateGallery(Context context, String newImagePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(newImagePath));
        intent.setData(uri);
        context.sendBroadcast(intent);//这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
    }

    /**
     * 将纬度转为6位小数,带北纬南纬标识的字符串
     *
     * @param lat 纬度
     * @return
     */
    public static String covertLat(double lat) {
        double latScale = new BigDecimal(lat).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        StringBuilder sb = new StringBuilder();
        sb.append(latScale);
        if (lat > 0.0) {
            sb.append("N");
        } else {
            sb.append("S");
        }
        return sb.toString();
    }

    /**
     * 将经度转为6位小数,带东经西经标识的字符串
     *
     * @param lng 经度
     * @return
     */
    public static String covertLng(double lng) {
        double lngScale = new BigDecimal(lng).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        StringBuilder sb = new StringBuilder();
        sb.append(lngScale);
        if (lng > 0.0) {
            sb.append("E");
        } else {
            sb.append("W");
        }
        return sb.toString();
    }

    /**
     * @param num   原始数据
     * @param point 保留小数多少位
     * @return
     */
    public static double roundNum(double num, int point) {
        BigDecimal decimal = new BigDecimal(num);
        return decimal.setScale(point, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * deviceID的组成为：渠道标志+识别符来源标志+hash后的终端识别符
     * <p>
     * 渠道标志为：
     * 1，andriod（a）
     * <p>
     * 识别符来源标志：
     * 1， wifi mac地址（wifi）；
     * 2， IMEI（imei）；
     * 3， 序列号（sn）；
     * 4， id：随机码。若前面的都取不到时，则随机生成一个随机码，需要缓存。
     *
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        StringBuilder deviceId = new StringBuilder();
        // 渠道标志
        deviceId.append("ut");
        try {
//            //wifi mac地址
//            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//            WifiInfo info = wifi.getConnectionInfo();
//            String wifiMac = info.getMacAddress();
//            if (!isEmpty(wifiMac)) {
//                deviceId.append("wifi");
//                deviceId.append(wifiMac);
//                return deviceId.toString();
//            }

            //IMEI（imei）
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if (!isEmpty(imei)) {
                deviceId.append("imei");
                deviceId.append(imei);
                return deviceId.toString();
            }

            //序列号（sn）
            String sn = tm.getSimSerialNumber();
            if (!isEmpty(sn)) {
                deviceId.append("sn");
                deviceId.append(sn);
                return deviceId.toString();
            }

            //如果上面都没有， 则生成一个id：随机码
            String uuid = getUUID(context);
            if (!isEmpty(uuid)) {
                deviceId.append("id");
                deviceId.append(uuid);
                return deviceId.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            deviceId.append("id").append(getUUID(context));
        }
        return deviceId.toString();
    }

    /**
     * 得到全局唯一UUID
     */
    public static String getUUID(Context context) {
        String uuid = getPreferenceString(context, "uuid");
        if (isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            savePreferenceString(context, "uuid", uuid);
        }
        return uuid;
    }
}

