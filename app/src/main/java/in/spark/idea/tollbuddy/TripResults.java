package in.spark.idea.tollbuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class TripResults extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_results);

        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle("About Us");

        MobileAds.initialize(this, "ca-app-pub-4283164631469341~3129888273");
        AdView mBottomAdView = (AdView) findViewById(R.id.tripbottomBanner);

        AdRequest adBottomRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mBottomAdView.loadAd(adBottomRequest);

//        Toast.makeText(this,"Something went wrong!",Toast.LENGTH_LONG).show();

        Button btnmoreapps


    }
}
