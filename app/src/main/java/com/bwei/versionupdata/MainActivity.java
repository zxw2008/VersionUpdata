package com.bwei.versionupdata;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private String URL = "https://www.zhaoapi.cn/version/getVersion?type=1";
    private Handler handler = new Handler();
    private int c;
    private String versionCode;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        initWeb();
    }


    public void downFile(String url) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("正在下载");
        progressDialog.setMessage("请稍后...");
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();
        progressDialog.setCancelable(false);
        DownloadUtil.get().download(url, Environment.getExternalStorageDirectory().getAbsolutePath(), "mapp.apk", new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                //下载完成进行相关逻辑操作

            }

            @Override
            public void onDownloading(int progress) {
                progressDialog.setProgress(progress);
            }

            @Override
            public void onDownloadFailed(Exception e) {
                //下载异常进行相关提示操作
            }
        });

}


    private void initWeb() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(URL)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "无网络", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){
                    final String result = response.body().string();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            final SupperClass supperClass = gson.fromJson(result, SupperClass.class);
                           String code = supperClass.getData().getVersionCode();
                            c = Integer.parseInt(code);

                            int vc = Integer.parseInt(versionCode);
                            Log.i("AAA","vc"+vc+"-c"+c);
                            if (c>vc){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("发现新版本");
                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {
                                        String apkUrl = supperClass.getData().getApkUrl();
                                        downFile(apkUrl);
                                    }
                                });
                                //    设置一个NegativeButton
                                builder.setNegativeButton("取消", null);
                                builder.show();
                            }
                        }
                    });

                }

            }
        });
        versionCode = APKVersionCodeUtils.getVersionCode(this) + "";


    }
}
