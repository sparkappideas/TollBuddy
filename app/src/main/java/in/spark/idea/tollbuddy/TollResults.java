package in.spark.idea.tollbuddy;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.IOException;
import java.util.ArrayList;

public class TollResults extends AppCompatActivity {
    String vehTypes;
    DatabaseHelper myDbHelper;
    TableLayout tl;
    TableRow tr;
    TextView tollName,singleJ,returnJ;
    InterstitialAd mInterstitialAd;
    RecyclerView tollrecyclerView;
    private ArrayList<DataModel> data;
    static String[] mplazaName;
    static String[] mtollPrice;
    static String[] mplazaid;
    private RecyclerView.Adapter adapter;
    static View.OnClickListener myOnClickListener;
    private RecyclerView.LayoutManager layoutManager;
    String mFrom,mTo,mJourneyType;
    double totPrize;

    String[] mvehTypes = {"Car/Jeep/Van","LCV","Bus-Truck","Upto 3 Axle Vehicle","4 to 6 Axle","HCM-EME","7 or more Axle"};
    String[] mjourneyTypes = {"Single","Two Way","Monthly","Local Vehicle"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toll_results);

        getSupportActionBar().setHomeButtonEnabled(true);

        mFrom = getIntent().getExtras().getString("from");
        mTo = getIntent().getExtras().getString("to");
        mJourneyType = getIntent().getExtras().getString("journeytype");

        getSupportActionBar().setTitle(mFrom + " To " + mTo);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4283164631469341~3129888273");
        AdView mBottomAdView = (AdView) findViewById(R.id.resultsbottomBanner);

        AdRequest adBottomRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mBottomAdView.loadAd(adBottomRequest);

        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        mInterstitialAd.setAdUnitId(getString(R.string.interestial_results_ad_unit_id));

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


        myOnClickListener = new MyOnClickListener(this);

        tollrecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        tollrecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        tollrecyclerView.setLayoutManager(layoutManager);

        myDbHelper = new DatabaseHelper(this);

        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            Toast.makeText(this,"Something went wrong!",Toast.LENGTH_SHORT).show();
            finish();
        }

        try {
            myDbHelper.openDataBase();
        }catch(SQLException sqle){
            Toast.makeText(this,"Something went wrong!",Toast.LENGTH_SHORT).show();
            finish();
        }

        String geo = getIntent().getExtras().getString("geo").replace("$",",");
        vehTypes = getIntent().getExtras().getString("vehtypes");

        String[] loc = geo.split(",");

        TextView lblrslt=(TextView)findViewById(R.id.lblrslt);

        String latitude="",longidude="";

        for(int i=0;i<loc.length;i++) {
            if(loc[i].length()>2) {
                if (i % 2 == 0) {
                    latitude = latitude + loc[i] + ",";
                } else {
                    longidude = longidude + loc[i] + ",";
                }
            }
        }

        latitude = latitude.substring(0,latitude.length()-1);
        longidude = longidude.substring(0,longidude.length()-1);

        lblrslt.setText("latitudes:" + latitude + "\n" + "longidude:" + longidude);

        String qry="";
        if(mJourneyType.equalsIgnoreCase(mjourneyTypes[0])){
            qry = "select pl_id as plazaid, pl_name as place,v_single as price from plaza A," +
                    " veh_types B where A.pl_id = B.v_pl_id and v_type = '" + vehTypes  + "' and pl_lat in (" + latitude + ") and pl_lang in (" + longidude + ")";
        }else if(mJourneyType.equalsIgnoreCase(mjourneyTypes[1])){
            qry = "select pl_id as plazaid, pl_name as place,v_return as price from plaza A," +
                    " veh_types B where A.pl_id = B.v_pl_id and v_type = '" + vehTypes  + "' and pl_lat in (" + latitude + ") and pl_lang in (" + longidude + ")";
        }else if(mJourneyType.equalsIgnoreCase(mjourneyTypes[2])){
            qry = "select pl_id as plazaid, pl_name as place,v_monthly as price from plaza A," +
                    " veh_types B where A.pl_id = B.v_pl_id and v_type = '" + vehTypes  + "' and pl_lat in (" + latitude + ") and pl_lang in (" + longidude + ")";
        }else if(mJourneyType.equalsIgnoreCase(mjourneyTypes[3])){
            qry = "select pl_id as plazaid, pl_name as place,v_inlocal as price from plaza A," +
                    " veh_types B where A.pl_id = B.v_pl_id and v_type = '" + vehTypes  + "' and pl_lat in (" + latitude + ") and pl_lang in (" + longidude + ")";
        }

        Cursor Res = myDbHelper.getTollResults(qry);
        String plazaName="",singleJ="",plazaid="";

        totPrize=0;

        if (Res.moveToFirst()){
            do{
                plazaid = plazaid + Res.getString(Res.getColumnIndex("plazaid")) + ",";
                plazaName = plazaName + Res.getString(Res.getColumnIndex("place")) + ",";
                singleJ = singleJ + Res.getString(Res.getColumnIndex("price")) + ",";
                totPrize = totPrize + Double.parseDouble(Res.getString(Res.getColumnIndex("price")));
            }while(Res.moveToNext());
        }
        Res.close();

        plazaid = plazaid.substring(0,plazaid.length()-1);
        plazaName = plazaName.substring(0,plazaName.length()-1);
        singleJ = singleJ.substring(0,singleJ.length()-1);

        mplazaName = plazaName.split(",");
        mtollPrice = singleJ.split(",");
        mplazaid = plazaid.split(",");

        data = new ArrayList<DataModel>();
        for (int i = 0; i < mplazaName.length; i++) {
            data.add(new DataModel(
                    mplazaName[i],
                    mtollPrice[i],
                    Integer.parseInt(mplazaid[i]),
                    0
            ));
        }

        adapter = new CustomAdapter(this,data,mJourneyType);
        tollrecyclerView.setAdapter(adapter);


//        tl = (TableLayout) findViewById(R.id.maintable);

//        addHeaders();
//        addData(plazaName.split(","),singleJ.split(","));

        lblrslt.setText("Total Cost\n" + this.getString(R.string.Rs) + " " + String.format("%.2f", totPrize));
    }

/*
    */
/** This function add the headers to the table **//*

    public void addHeaders(){
        */
/** Create a TableRow dynamically **//*

        tr = new TableRow(this);
        TableLayout.LayoutParams lp= new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(5,5,5,5);
        tr.setLayoutParams(lp);

        */
/** Creating a TextView to add to the row **//*

        TextView companyTV = new TextView(this);
        companyTV.setText("PLAZA NAME");
        companyTV.setBackgroundColor(Color.parseColor("#87cefa"));
        companyTV.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        companyTV.setPadding(5, 5, 5, 5);
        tr.addView(companyTV);  // Adding textView to tablerow.

        */
/** Creating another textview **//*

        TextView valueTV = new TextView(this);
        valueTV.setText("PRICE");
        valueTV.setBackgroundColor(Color.parseColor("#87cefa"));
        valueTV.setPadding(5, 5, 5, 5);
        valueTV.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tr.addView(valueTV); // Adding textView to tablerow.

        // Add the TableRow to the TableLayout
        tl.addView(tr, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        // we are adding two textviews for the divider because we have two columns
        tr = new TableRow(this);
        tr.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        // Add the TableRow to the TableLayout
        tl.addView(tr, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }

    */
/** This function add the data to the table **//*

    public void addData(String[] plaza,String[] single){

        for (int i = 0; i < plaza.length; i++)
        {
            */
/** Create a TableRow dynamically **//*

            tr = new TableRow(this);
            tr.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            */
/** Creating a TextView to add to the row **//*

            tollName = new TextView(this);
            tollName.setText("" + plaza[i]);
            tollName.setBackgroundColor(Color.parseColor("#87cefa"));
            tollName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            tollName.setPadding(5, 5, 5, 5);
            tr.addView(tollName);  // Adding textView to tablerow.

            */
/** Creating another textview **//*

            singleJ = new TextView(this);
            singleJ.setText("" + String.format("%.2f", Double.parseDouble(single[i])));
            singleJ.setBackgroundColor(Color.parseColor("#87cefa"));
            singleJ.setPadding(5, 5, 5, 5);
            singleJ.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            tr.addView(singleJ); // Adding textView to tablerow.

            // Add the TableRow to the TableLayout
            tl.addView(tr, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }
*/

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

    private class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            int selectedItemPosition = tollrecyclerView.getChildPosition(v);
            RecyclerView.ViewHolder viewHolder
                    = tollrecyclerView.findViewHolderForPosition(selectedItemPosition);
            TextView textViewName
                    = (TextView) viewHolder.itemView.findViewById(R.id.lblplazaname);
            String selectedName = (String) textViewName.getText();
            int selectedItemId = -1;
            for (int i = 0; i < mplazaName.length; i++) {
                if (selectedName.equalsIgnoreCase(mplazaName[i])) {
                    selectedItemId = Integer.parseInt(mplazaid[i]);
                }
            }

            Intent iy= new Intent(TollResults.this,PlazaDetails.class);
            iy.putExtra("link","http://tis.nhai.gov.in/TollInformation?TollPlazaID=" + selectedItemId);
            iy.putExtra("plname",selectedName);
            startActivity(iy);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }
    //and this to handle actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Total Toll Cost from " + mFrom + " To " + mTo + " is " + this.getString(R.string.Rs) + " " + String.format("%.2f", totPrize) + "\nCheck more routes at: https://play.google.com/store/apps/details?id=in.spark.idea.tollbuddy");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

