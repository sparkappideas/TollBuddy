package in.spark.idea.tollbuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FuelPrice.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FuelPrice#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FuelPrice extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // temporary string to show the parsed response
    private String jsonResponse;
    private String urlJsonArry = "http://fuelpriceindia.herokuapp.com/cities";
    private String urlfuel = "http://fuelpriceindia.herokuapp.com/price?city=";

    List<String> allNames;
    MaterialBetterSpinner spnCities;
    Button btnfuel;

    TextView lblPetrolPrice,lblDieselPrice;

    Double petrolPrice=0.0,DieselPrice=0.0;
    ProgressBar pgLoading;

    private OnFragmentInteractionListener mListener;

    public FuelPrice() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FuelPrice.
     */
    // TODO: Rename and change types and number of parameters
    public static FuelPrice newInstance(String param1, String param2) {
        FuelPrice fragment = new FuelPrice();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fuel_price, container, false);

        makeJsonObjectRequest();

        MobileAds.initialize(getActivity(), "ca-app-pub-4283164631469341~3129888273");
        AdView mBottomAdView = (AdView) v.findViewById(R.id.fuelbottomBanner);

        AdRequest adBottomRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mBottomAdView.loadAd(adBottomRequest);

        spnCities = (MaterialBetterSpinner )v.findViewById(R.id.spn_cities);
        spnCities.setEnabled(false);

        btnfuel = (Button)v.findViewById(R.id.btngetfuel);
        lblDieselPrice = (TextView)v.findViewById(R.id.lbldieselprice);
        lblPetrolPrice = (TextView)v.findViewById(R.id.lblpetrolprice);

        btnfuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(spnCities.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(),"Please select city!",Toast.LENGTH_LONG).show();
                    return;
                }
                new FuelTask().execute(spnCities.getText().toString());
            }
        });

        pgLoading = (ProgressBar)v.findViewById(R.id.fuelloading);

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    class FuelTask extends AsyncTask<String,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DieselPrice = 0.0;
            petrolPrice = 0.0;
            pgLoading.setVisibility(View.VISIBLE);
            pgLoading.setIndeterminate(true);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        }

        @Override
        protected Void doInBackground(String... voids) {
            getPetrolPrice(voids[0]);
            getDieselPrice(voids[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    lblDieselPrice.setText("Today Diesel Price: " + getString(R.string.Rs) + " " + String.format("%.2f", DieselPrice));
                    lblPetrolPrice.setText("Today Petrol Price: " + getString(R.string.Rs) + " " + String.format("%.2f", petrolPrice));
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    pgLoading.setVisibility(View.GONE);
                }
            }, 5000);


        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Method to make json object request where json response starts wtih {
     * */
    private void makeJsonObjectRequest() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlJsonArry, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("JSONRESP", response.toString());
                try {
                    allNames = new ArrayList<String>();
                    JSONArray cast = response.getJSONArray("cities");
                    for (int i=0; i<cast.length(); i++) {
                        String name = cast.getString(i);
                        allNames.add(name);
                    }
                    if(!allNames.isEmpty()) {
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, allNames);
                        spnCities.setAdapter(arrayAdapter);
                        spnCities.setEnabled(true);
                    }
                    Log.d("JSONRESP", "" + allNames.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("JSONERROR", "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    /**
     * Method to make json object request where json response starts wtih {
     * */
    private void getPetrolPrice(String city) {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlfuel + city + "&fuel_type=petrol", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("JSONRESP", response.toString());
                try {
                    petrolPrice = response.getDouble("petrol");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("JSONERROR", "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }


    /**
     * Method to make json object request where json response starts wtih {
     * */
    private void getDieselPrice(String city) {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlfuel + city + "&fuel_type=diesel", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("JSONRESP", response.toString());
                try {
                    DieselPrice = response.getDouble("diesel");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("JSONERROR", "Error: " + error.getMessage());
            }
        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

}
