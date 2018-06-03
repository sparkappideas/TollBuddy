package in.spark.idea.tollbuddy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TollPrice.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TollPrice#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TollPrice extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    PlaceAutocompleteFragment fromPlace,toPlace;
    MaterialBetterSpinner spnVehTypes,spnJourneyTypes;
    String fromPlaceAddr="",toPlaceAddr="",mfrom,mTo;
    View v;
    ProgressBar pgLoading;

    String[] mvehTypes = {"Car/Jeep/Van","LCV","Bus-Truck","Upto 3 Axle Vehicle","4 to 6 Axle","HCM-EME","7 or more Axle"};
    String[] mjourneyTypes = {"Single","Two Way","Monthly","Local Vehicle"};

    private OnFragmentInteractionListener mListener;

    public TollPrice() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TollPrice.
     */
    // TODO: Rename and change types and number of parameters
    public static TollPrice newInstance(String param1, String param2) {
        TollPrice fragment = new TollPrice();
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

        v = inflater.inflate(R.layout.fragment_toll_price, container, false);

        MobileAds.initialize(getActivity(), "ca-app-pub-4283164631469341~3129888273");
        AdView mBottomAdView = (AdView) v.findViewById(R.id.tollbottomBanner);

        AdRequest adBottomRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mBottomAdView.loadAd(adBottomRequest);


        fromPlace = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.place_from);

        toPlace = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.place_to);
         

        spnVehTypes = (MaterialBetterSpinner )v.findViewById(R.id.spn_veh_types);
        spnJourneyTypes = (MaterialBetterSpinner)v.findViewById(R.id.spn_journey_type);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line,mvehTypes);
        spnVehTypes.setAdapter(arrayAdapter);

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line,mjourneyTypes);
        spnJourneyTypes.setAdapter(arrayAdapter2);

        ((EditText)fromPlace.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Search From City");
//        ((EditText)fromPlace.getView().findViewById(R.id.place_autocomplete_search_input)).setHintTextColor(getResources().getColor(R.color.toolTipColor));
        ((EditText)toPlace.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Search To City");
//        ((EditText)toPlace.getView().findViewById(R.id.place_autocomplete_search_input)).setHintTextColor(getResources().getColor(R.color.toolTipColor));


        /*
        * The following code example shows setting an AutocompleteFilter on a PlaceAutocompleteFragment to
        * set a filter returning only results with a precise address.
        */

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .setCountry("IN")
                .build();


        fromPlace.setFilter(typeFilter);
        toPlace.setFilter(typeFilter);

        fromPlace.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getAddress());//get place details here
                mfrom = place.getName().toString();
                fromPlaceAddr = place.getAddress().toString();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        toPlace.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());//get place details here
                mTo = place.getName().toString();
                toPlaceAddr = place.getAddress().toString();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        Button btnSubmit = (Button)v.findViewById(R.id.btnsubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fromPlaceAddr.isEmpty() || toPlaceAddr.isEmpty()) {
                    Toast.makeText(getActivity(),"Please select From and To City!",Toast.LENGTH_LONG).show();
                    return;
                }

                if(spnVehTypes.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(),"Please select Vehicle Type!",Toast.LENGTH_LONG).show();
                    return;
                }

                if(spnJourneyTypes.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(),"Please select Journey Type!",Toast.LENGTH_LONG).show();
                    return;
                }

                String url1 = "http://nhtis.org/nhai/uploadhandler.ashx?up=3&Source=" + fromPlaceAddr + "&Destination=" + toPlaceAddr + "&waypoints=";
                new getResp().execute(url1);
            }
        });

        pgLoading = (ProgressBar)v.findViewById(R.id.pgloading);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    class getResp extends AsyncTask<String,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgLoading.setVisibility(View.VISIBLE);
            pgLoading.setIndeterminate(true);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = null;
            try {
                URL url = new URL(strings[0].replace(" ","%20"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(80000);
                // read the response
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = convertStreamToString(in);
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException: " + e.getMessage());
            } catch (ProtocolException e) {
                Log.e(TAG, "ProtocolException: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.length()>0) {
                Intent act = new Intent(getActivity(), TollResults.class);
                act.putExtra("geo", s);
                act.putExtra("vehtypes", spnVehTypes.getText().toString());
                act.putExtra("journeytype", spnJourneyTypes.getText().toString());
                act.putExtra("from", mfrom);
                act.putExtra("to", mTo);
                startActivity(act);
            }else{
                Toast.makeText(getActivity(),"Something went wrong!",Toast.LENGTH_LONG).show();
            }
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            pgLoading.setVisibility(View.GONE);
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PlaceAutocompleteFragment f = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_from);
        if (f != null)
            getActivity().getFragmentManager().beginTransaction().remove(f).commit();


        PlaceAutocompleteFragment f1 = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_to);
        if (f1 != null)
            getActivity().getFragmentManager().beginTransaction().remove(f1).commit();

    }
}
