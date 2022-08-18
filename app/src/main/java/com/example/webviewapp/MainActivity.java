package com.example.webviewapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView mywebView;
    private ValueCallback<Uri[]> afterLollipop;
    private ValueCallback<Uri> mUploadMessage;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mywebView = findViewById(R.id.webview);
        WebSettings webSettings= mywebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        if (savedInstanceState == null) {
            if (isConnected()) {
                mywebView.loadUrl("https://mobile.blindsjunction.co.uk/");
            } else {
                mywebView.loadUrl("file:///android_asset/index.html");
            }
        }
        // Line of Code for opening links in app
        mywebView.setWebViewClient(new WebViewClient(){
            @SuppressLint("JavascriptInterface")
            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
                super.onReceivedError(webView, errorCode, description, failingUrl);
                try {
                    webView.stopLoading();
                } catch (Exception e) {
                    Log.e("Connectivity Exception", e.getMessage());
                }

                if (webView.canGoBack()) {
                    webView.goBack();
                }
                webView.loadUrl("file:///android_asset/index.html");
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Check your internet connection and try again.");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Try Again", (dialog, which) -> {
                    finish();
                    startActivity(getIntent());
                });
                alertDialog.show();
                super.onReceivedError(webView, errorCode, description, failingUrl);
            }
        });

        mywebView.setWebChromeClient(new WebChromeClient() {

            // For Android > 4.1 - undocumented method
            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 101);

            }

            // For Android > 5.0
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                afterLollipop = filePathCallback;
                startActivityForResult(fileChooserParams.createIntent(), 101);
                return true;

            }

        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {

                Uri result = intent == null ? null
                        : intent.getData();
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(result);
                } else if (afterLollipop != null) {

                    afterLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                    afterLollipop = null;
                }
                mUploadMessage = null;
            }
        }

    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mywebView.saveState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        mywebView.restoreState(savedInstanceState);
    }
    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
            mywebView.loadUrl("file:///android_asset/index.html");
        }
        return connected;
    }
    @Override
    public void onBackPressed() {
        if (!isConnected()) {
            mywebView.loadUrl("file:///android_asset/index.html");
        } else {
            if(mywebView.canGoBack())
            {
                mywebView.goBack();
            }
            else
            {
                super.onBackPressed();
            }
        }
    }

}