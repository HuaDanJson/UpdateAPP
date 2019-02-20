package cool.android.updateapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;

import java.io.File;
import java.util.List;

import cool.android.updateapp.download.DownloadUtil;
import cool.android.updateapp.download.VideoDownloadListener;

public class MainActivity extends AppCompatActivity {

    private Button mUpdateButton;
    private String mDpwnLoadURL = "http://bmob-cdn-23838.b0.upaiyun.com/2019/02/20/2790a878402f43ed80c9c6413b4d930e.apk";
    private DownloadUtil mDownloadUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUpdateButton = findViewById(R.id.btn_update);
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionUtils.permission(PermissionConstants.STORAGE)
                        .rationale(new PermissionUtils.OnRationaleListener() {
                            @Override
                            public void rationale(final ShouldRequest shouldRequest) {
                                shouldRequest.again(true);
                            }
                        })
                        .callback(new PermissionUtils.FullCallback() {
                            @Override
                            public void onGranted(List<String> permissionsGranted) {
                                downloadAPK();
                            }

                            @Override
                            public void onDenied(List<String> permissionsDeniedForever,
                                                 List<String> permissionsDenied) {
                                if (!permissionsDeniedForever.isEmpty()) {
                                    PermissionUtils.launchAppDetailsSettings();
                                }
                            }
                        }).request();
            }
        });
    }

    private void downloadAPK() {
        final String videoUrl = mDpwnLoadURL;
        if (TextUtils.isEmpty(videoUrl)) {
//            onDownloadFail(storyModel, new NullPointerException("No video url to download"));
            return;
        }
        if (mDownloadUtil == null) {
            mDownloadUtil = new DownloadUtil();
        }
        mDownloadUtil.downloadFile(videoUrl, new VideoDownloadListener() {
            @Override
            public void onStart() {
                LogUtils.d("downloadFile  onStart");
            }

            @Override
            public void onProgress(final int currentLength) {
                LogUtils.d("downloadFile  onProgress  currentLength ： " + currentLength);
            }

            @Override
            public void onFinish(String localPath) {
                LogUtils.d("downloadFile  onFinish localPath ：" + localPath);
                final String mVideoPath = localPath;
                installApk(MainActivity.this, mVideoPath);
            }

            @Override
            public void onFailure(final String erroInfo) {
                LogUtils.d("downloadFile  onFailure erroInfo ：" + erroInfo);
            }
        });
    }

    /**
     * 安装apk
     *
     * @param context
     * @param apkPath
     */
    public static void installApk(Context context, String apkPath) {
        LogUtils.d("downloadFile installApk  apkPath ：" + apkPath);
        try {
            /**
             * provider
             * 处理android 7.0 及以上系统安装异常问题
             */
            File file = new File(apkPath);
            Intent install = new Intent();
            install.setAction(Intent.ACTION_VIEW);
            install.addCategory(Intent.CATEGORY_DEFAULT);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(context, ResourceUtil.getString(R.string.file_authorities), file);//在AndroidManifest中的android:authorities值
                Log.d("======", "apkUri=" + apkUri);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                LogUtils.d("downloadFile installApk  apkUri ：" + apkUri);
            } else {
                install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                LogUtils.d("downloadFile installApk  2222");
            }
            context.startActivity(install);
            LogUtils.d("downloadFile installApk  context.startActivity(install);");
        } catch (Exception e) {
            Log.d("======", e.getMessage());
            Toast.makeText(context, "文件解析失败", Toast.LENGTH_SHORT).show();
            LogUtils.d("downloadFile installApk  文件解析失败");
        }
    }

}
