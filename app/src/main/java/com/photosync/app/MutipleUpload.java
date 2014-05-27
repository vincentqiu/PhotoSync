package com.photosync.app;

import android.support.v7.app.ActionBarActivity;

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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.util.ArrayList;


@EActivity(R.layout.ac_image_grid)
public class MutipleUpload extends BaseActivity {


    private ArrayList<String> imageUrls;
    private DisplayImageOptions options;
    private ImageAdapter imageAdapter;

    @ViewById
    GridView    gridview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_image_grid);

        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        Cursor imagecursor = managedQuery(
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
                .showStubImage(R.drawable.stub_image)
                .showImageForEmptyUri(R.drawable.image_for_empty_url)
                .cacheInMemory()
                .cacheOnDisc()
                .build();

        imageAdapter = new ImageAdapter(this, imageUrls);
        gridview.setAdapter(imageAdapter);

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
