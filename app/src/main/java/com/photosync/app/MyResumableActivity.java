package com.photosync.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qiniu.auth.JSONObjectRet;
import com.qiniu.io.IO;
import com.qiniu.resumableio.PutExtra;
import com.qiniu.resumableio.ResumableIO;

import org.androidannotations.annotations.Background;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.w3c.dom.Text;


@EActivity(R.layout.resumable)
public class MyResumableActivity extends ActionBarActivity {

    int taskId = -1;
    Uri uploadUri;
    PutExtra mExtra;
    public String uptoken = "";

    @ViewById
    Button  start;

    @ViewById
    Button  stop;

    @ViewById
    TextView    hint;

    @ViewById
    ProgressBar pb;

    @Click(R.id.start)
    void startClicked() {
         selectFile();
    }

    @Click(R.id.stop)
    void stopClicked() {
        if (uploadUri == null) {
            Toast.makeText(this, "还没开始任务", 20).show();
            return;
        }
        if (taskId >= 0) {
            ResumableIO.stop(taskId);
            stop.setText("开始");
            hint.setText("暂停");
            taskId = -1;
            return;
        }
        stop.setText("暂停");
        hint.setText("连接中");
        doResumableUpload(uploadUri, mExtra);
        return;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resumable);
        pb.setMax(100);
    }

    @Override
    protected void onResume(){
        super.onResume();
        getToken();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_resumable, menu);
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


    @Background
    void getToken(){
        String url="http://api.sacabook.com/qiniuphotosync/getToken.php";
        String token = "";
        HttpGet get=new HttpGet(url);
        HttpClient client=new DefaultHttpClient();
        try {
            HttpResponse response=client.execute(get);
            token =  EntityUtils.toString(response.getEntity());

        } catch (Exception e) {
            // TODO: handle exception
            System.err.println(e.toString());
        }

        uptoken = token;
    }

    public void doResumableUpload(final Uri uri, PutExtra extra) {
        hint.setText("连接中");
        String key = IO.UNDEFINED_KEY; // 自动生成key
        extra.params = new HashMap<String, String>();
        extra.params.put("x:a", "bb");
        uploadUri = uri;
        taskId = ResumableIO.putFile(this, uptoken, key, uri, extra, new JSONObjectRet() {
            @Override
            public void onSuccess(JSONObject obj) {
                hint.setText("上传成功: " + obj.optString("key", ""));
            }

            @Override
            public void onProcess(long current, long total) {
                float percent = (float) (current*10000/total) / 100;
                hint.setText("上传中: " + percent + "%");
                pb.setProgress((int) percent);
            }

            @Override
            public void onPause(Object tag) {
                uploadUri = uri;
                mExtra = (PutExtra) tag;
            }

            @Override
            public void onFailure(Exception ex) {
                hint.setText(ex.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        PutExtra e = new PutExtra();
        e.notify = new PutExtra.INotify() {
            @Override
            public void onSuccessUpload(PutExtra ex) {
                if (ex.isFinishAll()) return;
                JSONObject json;
                try {
                    json = ex.toJSON();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    return;
                }
                // store to disk
                // restore PutExtra by new PutExtra(JSONObject);
            }
        };
        doResumableUpload(data.getData(), e);
    }

    public void selectFile() {
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        //startActivityForResult(i, 0);
        //intent.setType("*/*");
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            //startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"), 1);
            startActivityForResult(i, 0);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

}
