package com.example.antonio.ecoleaide;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.onesignal.OneSignal;

public class MainActivity extends AppCompatActivity implements ForceUpdateChecker.OnUpdateNeededListener{

    WebView myWebView;
    AlertDialog.Builder builder;
    AlertDialog.Builder exit;
    AlertDialog dialog;
    AlertDialog exitDialog;
    ProgressBar progressbar;
    private AdView mAdView;
    String fail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App a = new App();
        a.onCreate();

        ForceUpdateChecker.with(this).onUpdateNeeded(MainActivity.this).check();

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        MobileAds.initialize(this, "ca-app-pub-6690759795572320~3448358495");

        progressbar=findViewById(R.id.progressbar);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        builder = new AlertDialog.Builder(this,android.R.style.ThemeOverlay_Material_Dialog_Alert);
        builder.setTitle("Network Error");
        builder.setMessage("Please Check Your Internet Connection!!");

        builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                myWebView.loadUrl(fail);
            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        exit = new AlertDialog.Builder(this,android.R.style.ThemeOverlay_Material_Dialog_Alert);
        exit.setTitle("Exit");
        exit.setMessage("Do you want to leave the app?");

        exit.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });

        exit.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });


        exitDialog = exit.create();
        exitDialog.setCanceledOnTouchOutside(false);


        myWebView = findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                dialog.show();
                myWebView.loadUrl("about:blank");
                fail = failingUrl;
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                    if(url.equals("https://sset.ecoleaide.com/home.htm")||url.equals("https://sset.ecoleaide.com/dashboard.htm#login"))
                        view.clearHistory();
                }
            @Override
            public void onPageFinished(WebView view, String url){
                if(url.equals("https://sset.ecoleaide.com/home.htm")||url.equals("https://sset.ecoleaide.com/dashboard.htm#login"))
                    view.clearHistory();
            }
        });
        myWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view,int progress){
                progressbar.setProgress(progress);
                if(progress < 100)
                    progressbar.setVisibility(View.VISIBLE);
                if(progress == 100)
                    progressbar.setVisibility(View.GONE);
            }
        });
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl("https://sset.ecoleaide.com/");
    }

    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("New version available")
                .setMessage("Please, update app to new version to continue reposting.")
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton("No, thanks",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void onBackPressed(){
        if(dialog.isShowing())
            myWebView.reload();
        else if(myWebView.canGoBack())
            myWebView.goBack();
        else
            exitDialog.show();
    }
}
