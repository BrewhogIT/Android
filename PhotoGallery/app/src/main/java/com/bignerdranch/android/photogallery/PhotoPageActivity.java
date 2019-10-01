package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.webkit.WebView;

public class PhotoPageActivity extends SingleFragmentActivity {
    private PhotoPageFragment fragment;

    public static Intent newIntent(Context context, Uri photoPageUri){
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        fragment = PhotoPageFragment.newInstance(getIntent().getData());
        return fragment;
    }

    @Override
    public void onBackPressed() {
        if (fragment.mWebView.canGoBack()){
            fragment.mWebView.goBack();
        }else {
            super.onBackPressed();
        }
    }
}
