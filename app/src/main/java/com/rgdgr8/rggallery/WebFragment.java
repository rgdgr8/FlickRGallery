package com.rgdgr8.rggallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

public class WebFragment extends Fragment {
    public static final String INTENT_URL = "web_url";
    private static final String TAG = "WebFragment";
    public static boolean onWeb = false;

    public WebView getWebView() {
        return mWebView;
    }

    private WebView mWebView;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View v = inflater.inflate(R.layout.fragment_web, container, false);

        Uri uri = getArguments().getParcelable(INTENT_URL);
        String scheme = uri.getScheme();

        if (scheme.equals("http") || scheme.equals("https")) {
            String url = uri.toString();

            mWebView = v.findViewById(R.id.web_view);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.getSettings().setUseWideViewPort(true);
            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.setWebViewClient(new WebViewClient());
            mWebView.loadUrl(url);
        }else {
            Intent i = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(i);
        }

        onWeb = true;
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
        onWeb = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
