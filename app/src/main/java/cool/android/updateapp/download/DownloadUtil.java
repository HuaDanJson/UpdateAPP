package cool.android.updateapp.download;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadUtil {
    private static final String TAG = "DownloadUtil";
    private static final String PATH_CHALLENGE_VIDEO = Environment.getExternalStorageDirectory() + "/DownloadFile";
    //视频下载相关
    protected ApiInterface mApi;
    private Call<ResponseBody> mCall;
    private File mFile;
    private Thread mThread;
    private String mVideoPath; //下载到本地的视频路径

    public DownloadUtil() {
        if (mApi == null) {
            mApi = ApiHelper.getInstance().buildRetrofit("https://sapi.daishumovie.com/")
                    .createService(ApiInterface.class);
        }
    }

    public void downloadFile(String url, final VideoDownloadListener videoDownloadListener) {
        //通过Url得到保存到本地的文件名
        String name = url;
        if (FileUtils.createOrExistsDir(PATH_CHALLENGE_VIDEO)) {
            int i = name.lastIndexOf('/');//一定是找最后一个'/'出现的位置
            if (i != -1) {
                name = name.substring(i);
                mVideoPath = PATH_CHALLENGE_VIDEO + name;
            }
        }
        if (TextUtils.isEmpty(mVideoPath)) {
            Log.e(TAG, "downloadVideo: 存储路径为空了");
            return;
        }
        //建立一个文件
        mFile = new File(mVideoPath);
        if (!FileUtils.isFileExists(mFile) && FileUtils.createOrExistsFile(mFile)) {
            if (mApi == null) {
                Log.e(TAG, "downloadVideo: 下载接口为空了");
                return;
            }
            mCall = mApi.downloadFile(url);
            mCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                    //下载文件放在子线程
                    mThread = new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            //保存到本地
                            writeFile2Disk(response, mFile, videoDownloadListener);
                        }
                    };
                    mThread.start();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    videoDownloadListener.onFailure("网络错误！");
                }
            });
        } else {
            videoDownloadListener.onFinish(mVideoPath);
        }
    }

    private void writeFile2Disk(Response<ResponseBody> response, File file, VideoDownloadListener videoDownloadListener) {
        videoDownloadListener.onStart();
        long currentLength = 0;
        OutputStream os = null;

        if (response.body() == null) {
            videoDownloadListener.onFailure("资源错误！");
            return;
        }
        InputStream is = response.body().byteStream();
        long totalLength = response.body().contentLength();

        try {
            os = new FileOutputStream(file);
            int len;
            byte[] buff = new byte[1024];
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
                currentLength += len;
                Log.e(TAG, "当前进度: " + currentLength);
                videoDownloadListener.onProgress((int) (100 * currentLength / totalLength));
                if ((int) (100 * currentLength / totalLength) == 100) {
                    videoDownloadListener.onFinish(mVideoPath);
                }
            }
        } catch (FileNotFoundException e) {
            videoDownloadListener.onFailure("未找到文件！");
            e.printStackTrace();
        } catch (IOException e) {
            videoDownloadListener.onFailure("IO错误！");
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
