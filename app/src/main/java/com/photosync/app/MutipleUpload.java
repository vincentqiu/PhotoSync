package com.photosync.app;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;


import com.nostra13.universalimageloader.core.DisplayImageOptions;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.qiniu.auth.JSONObjectRet;
import com.qiniu.io.IO;
import com.qiniu.io.PutExtra;

import java.util.ArrayList;
import java.util.HashMap;


@EActivity(R.layout.ac_image_grid)
public class MutipleUpload extends BaseActivity {


    // @gist upload_arg
    public static String bucketName = "photosync";
    public static String domain = bucketName + ".qiniudn.com";
    // upToken 这里需要自行获取. SDK 将不实现获取过程. 当token过期后才再获取一遍
    public String uptoken = "";

    // @endgist

    private ArrayList<String> imageUrls;
    private DisplayImageOptions options;
    private ImageAdapter imageAdapter;

    @ViewById
    GridView    gridview;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_image_grid);

        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        Cursor imagecursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy + " DESC");

        this.imageUrls = new ArrayList<String>();

        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            imageUrls.add(imagecursor.getString(dataColumnIndex));

            System.out.println("=====> Array path => "+imageUrls.get(i));
        }

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.stub_image)
                .showImageForEmptyUri(R.drawable.image_for_empty_url)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();



    }

    @Override
    protected void onResume() {
        super.onResume();
        imageAdapter = new ImageAdapter(this, imageUrls);
        gridview.setAdapter(imageAdapter);
        getToken();
    }

    // @gist upload
    boolean uploading = false;
    /**
     * 普通上传文件
     * @param uri
     */
    @Background
    void doUpload(Uri uri) {

//        if (uploading) {
//            System.out.println("上传中，请稍后");
//            return;
//        }

        uploading = true;
        String key = IO.UNDEFINED_KEY; // 自动生成key
        PutExtra extra = new PutExtra();
        extra.params = new HashMap<String, String>();
        extra.params.put("x:a", "测试中文信息");
        System.out.println("上传中");
        IO.putFile(this, uptoken, key, uri, extra, new JSONObjectRet() {
            @Override
            public void onProcess(long current, long total) {
                System.out.println(current + "/" + total);
            }

            @Override
            public void onSuccess(JSONObject resp) {
                uploading = false;
                String hash = resp.optString("hash", "");
                String value = resp.optString("x:a", "");
                String redirect = "http://" + domain + "/" + hash;
                System.out.println("上传成功! " + hash);
                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirect));
                //startActivity(intent);
            }

            @Override
            public void onFailure(Exception ex) {
                uploading = false;
                System.out.println("错误: " + ex.getMessage());
            }
        });
    }

    @Click
    void btnChoosePhotosClicked(){

        System.out.println("photos clicked");

        ArrayList<String> checkList;
        checkList = imageAdapter.getCheckedItems();
        for(int i=0; i<checkList.size();i++){
            System.out.println(checkList.get(i));
            Uri uri = Uri.fromFile(new File(checkList.get(i)));
            doUpload(uri);
        }

    }

    public class ImageAdapter extends BaseAdapter {

        ArrayList<String> mList;
        LayoutInflater mInflater;
        Context mContext;
        SparseBooleanArray mSparseBooleanArray;

        public ImageAdapter(Context context, ArrayList<String> imageList) {
            // TODO Auto-generated constructor stub
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mSparseBooleanArray = new SparseBooleanArray();
            mList = new ArrayList<String>();
            this.mList = imageList;

        }

        public ArrayList<String> getCheckedItems() {
            ArrayList<String> mTempArry = new ArrayList<String>();

            for(int i=0;i<mList.size();i++) {
                if(mSparseBooleanArray.get(i)) {
                    mTempArry.add(mList.get(i));
                }
            }

            return mTempArry;
        }

        @Override
        public int getCount() {
            return imageUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.row_multiphoto_item, null);
            }

            CheckBox mCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);

            imageLoader.displayImage("file://"+imageUrls.get(position), imageView, options, new SimpleImageLoadingListener() {
//                @Override
//                public void onLoadingComplete(Bitmap loadedImage) {
//                    Animation anim = AnimationUtils.loadAnimation(MutipleUpload.this, R.anim.fade_in);
//                    imageView.setAnimation(anim);
//                    anim.start();
//                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    Animation anim = AnimationUtils.loadAnimation(MutipleUpload.this, R.anim.fade_in);
                    imageView.setAnimation(anim);
                    anim.start();
                }
            });

            mCheckBox.setTag(position);
            mCheckBox.setChecked(mSparseBooleanArray.get(position));
            mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);

            return convertView;
        }

        OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
            }
        };
    }
}
