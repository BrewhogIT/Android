package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.net.URISyntaxException;

import javax.xml.validation.Schema;

public class PhotoPageFragment extends VisibleFragment {
    private static final String ARG_URI ="photo_page_url";
    private final String TAG = "PhotoPageFragment";

    public WebView mWebView;
    private ProgressBar mProgressBar;
    private Uri mUri;

    public static PhotoPageFragment newInstance (Uri uri){
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI,uri);
        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri = getArguments().getParcelable(ARG_URI);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_page,container,false);

        mProgressBar = v.findViewById(R.id.progress_bar);
        mProgressBar.setMax(100);

        mWebView = v.findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView webView, int newProgress){
                if (newProgress == 100){
                    mProgressBar.setVisibility(View.GONE);
                }else{
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }

            public void onReceivedTitle(WebView webView, String title){
                AppCompatActivity activity =(AppCompatActivity) getActivity();
                activity.getSupportActionBar().setSubtitle(title);
            }
        });
        mWebView.setWebViewClient(new WebViewClient());
//        mWebView.setWebViewClient(new WebViewClient(){
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                Uri uri = request.getUrl();
//                if(uri.getScheme().equals("http")|| uri.getScheme().equals("https")) {
//                    return false;
//                }else{
//                    try {
//                        Intent intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
//                                .setAction(Intent.ACTION_VIEW);
//                        Uri fallbackUri = intent.getParcelableExtra("browser_fallback_url");
//                        Intent newIntent = new Intent (Intent.ACTION_VIEW,fallbackUri);
//                        startActivity(intent);
//
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    }
//                    return true;
//                }
//            }
//        });
        mWebView.loadUrl(mUri.toString());

        return v;
    }
}
