package com.photosync.app;

import android.content.Intent;
import android.net.Uri;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.HashMap;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.qiniu.auth.JSONObjectRet;
import com.qiniu.io.IO;
import com.qiniu.io.PutExtra;



@EActivity(R.layout.activity_main)
public class MainActivity extends Activity  {

    public static final int PICK_PICTURE_RESUMABLE = 0;

    // @gist upload_arg
    public static String bucketName = "<bucketName>";
    public static String domain = bucketName + ".qiniudn.com";
    // upToken 这里需要自行获取. SDK 将不实现获取过程. 当token过期后才再获取一遍
    public String uptoken = "<token>";
    // @endgist


    @ViewById
    TextView hint;

    @ViewById
    Button btnUpload;

    @ViewById
    Button  btnResumableUpload;


    /**
     * 初始化控件
     */
    private void initWidget() {
/*        btnUpload.setOnClickListener(this);
        btnResumableUpload.setOnClickListener(this);*/
    }

    // @gist upload
    boolean uploading = false;
    /**
     * 普通上传文件
     * @param uri
     */
    private void doUpload(Uri uri) {
        if (uploading) {
            hint.setText("上传中，请稍后");
            return;
        }
        uploading = true;
        String key = IO.UNDEFINED_KEY; // 自动生成key
        PutExtra extra = new PutExtra();
        extra.params = new HashMap<String, String>();
        extra.params.put("x:a", "测试中文信息");
        hint.setText("上传中");
        IO.putFile(this, uptoken, key, uri, extra, new JSONObjectRet() {
            @Override
            public void onProcess(long current, long total) {
                hint.setText(current + "/" + total);
            }

            @Override
            public void onSuccess(JSONObject resp) {
                uploading = false;
                String hash = resp.optString("hash", "");
                String value = resp.optString("x:a", "");
                String redirect = "http://" + domain + "/" + hash;
                hint.setText("上传成功! " + hash);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirect));
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception ex) {
                uploading = false;
                hint.setText("错误: " + ex.getMessage());
            }
        });
    }
    // @endgist

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.err.println("app create");
        initWidget();
        System.err.println("event bind");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Click(R.id.btnUpload)
    void btnUploadClicked() {
        System.err.println("click trigger!");
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_PICTURE_RESUMABLE);
        return;
    }

    @Click(R.id.btnResumableUpload)
    void btnResumableUploadClicked(View v){
        startActivity(new Intent(this, MyResumableActivity_.class));
    }

/*    @Override
    public void onClick(View view) {
        System.err.println("click trigger!");
        if (view.equals(btnUpload)) {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, PICK_PICTURE_RESUMABLE);
            return;
        }
        if (view.equals(btnResumableUpload)) {
            //startActivity(new Intent(this, MyResumableActivity.class));
            return;
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (requestCode == PICK_PICTURE_RESUMABLE) {
            doUpload(data.getData());
            return;
        }
    }

}
