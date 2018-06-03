package in.spark.idea.tollbuddy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class PlazaDetails extends AppCompatActivity {

    String mPlazaName;
    InterstitialAd mInterstitialAd;
    String url = "";
    private WebView mainWebContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plaza_details);

        getSupportActionBar().setHomeButtonEnabled(true);

        mPlazaName = getIntent().getExtras().getString("plname");

        getSupportActionBar().setTitle(mPlazaName);


        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4283164631469341~3129888273");
        AdView mBottomAdView = (AdView) findViewById(R.id.plazabottomBanner);

        AdRequest adBottomRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mBottomAdView.loadAd(adBottomRequest);

        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        mInterstitialAd.setAdUnitId(getString(R.string.plaza_interestial_ad_unit_id));

        requestNewInterstitial();

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isNetworkAvailable()){
                    showInterstitial();
                }
            }
        }, 5000);


        url = getIntent().getExtras().getString("link");

        mainWebContent = (WebView) findViewById(R.id.mainweb);
        mainWebContent.getSettings().setLoadWithOverviewMode(true);
        mainWebContent.getSettings().setUseWideViewPort(true);
        mainWebContent.getSettings().setJavaScriptEnabled(true);
        mainWebContent.getSettings().setBuiltInZoomControls(true);
        mainWebContent.getSettings().setCacheMode(0);
        mainWebContent.getSettings().setSupportZoom(true);
        mainWebContent.clearCache(true);
        mainWebContent.loadUrl(url);

        mainWebContent.setWebViewClient(new MyWebViewClient());
        mainWebContent.setWebChromeClient(new MyWebChromeClient());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void requestNewInterstitial() {
        // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
    }

    public void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

}
