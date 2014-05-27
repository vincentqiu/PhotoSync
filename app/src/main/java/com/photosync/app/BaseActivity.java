package com.photosync.app;

import android.app.Activity;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by admin on 2014/5/27.
 */
public class BaseActivity extends Activity {

    protected ImageLoader imageLoader = ImageLoader.getInstance();
}
