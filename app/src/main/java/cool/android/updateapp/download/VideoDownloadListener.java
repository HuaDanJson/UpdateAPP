package cool.android.updateapp.download;

public interface VideoDownloadListener {

    void onStart();

    void onProgress(int currentLength);

    void onFinish(String localPath);

    void onFailure(String erroInfo);
}
