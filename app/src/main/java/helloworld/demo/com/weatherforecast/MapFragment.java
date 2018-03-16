package helloworld.demo.com.weatherforecast;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapFragment";
    private static final LatLngBounds bounds = new
            LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private AutoCompleteTextView mSearchText;
    private PlaceAutocompleteAdapter mplaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private BroadcastReceiver broadcastReceiver;
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {


        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
           final Place place = places.get(0);
            try {
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());

                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                LatLng location = place.getLatLng();
               // passingLocationData(new LatLng(place.latitude,place.longitude));
             //   passingLocationData(place.getLatLng());
                //newInstance(location);
//
//                WeatherFragment fragment = new WeatherFragment();
//                Bundle arguments = new Bundle();
//                arguments.putString("key", String.valueOf(location));
//                fragment.setArguments(arguments);
//                MyBroadcastReceiver broadcastReceiver = new MyBroadcastReceiver() {
//                    @Override
//                    public void onReceive(Context context, Intent intent) {
//
//                    }
//                }

//               Intent intent = new Intent(String.valueOf(location));
//                intent.setAction("my.custom.action.tag");
//               intent.addCategory(Intent.CATEGORY_DEFAULT);
//                sendBroadcast(intent);

//                Intent intent =new Intent();
//                intent.setAction("location");
//                intent.putExtra("key",location);
//                sendBroadcast(intent);

                Log.d(TAG, "onResult: latlng: " + place.getLatLng());

                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                Log.d(TAG, "onResult: place: " + mPlace.toString());
            } catch (NullPointerException e) {
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage());
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), 10.2f, mPlace.getName());

           // passingLocationData(new LatLng(latitude,longitude));

            places.release();

        }


    };

//    private void passingLocationData(LatLng location)
//    {
//        Intent intent = new Intent("custom-event-name");
//        intent.putExtra("key",location);
//        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
//
//    }


//    public interface OnDataSetListener
//    {
//        public void setLocation(LatLng );
//    }
//    private void passingLocationData(LatLng latLng) {
//        WeatherFragment fragment = new WeatherFragment();
//                Bundle bundle = new Bundle();
//                Double x = latLng;
//
//                             fragment.setArguments(bundle);
//    }


    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
           // hideSoftKeyboard();

            final AutocompletePrediction item = mplaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    // View v1;
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_1, container, false);

       // mMap = ((SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mSearchText = (AutoCompleteTextView)v.findViewById(R.id.input_search);

        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            init();

            return v;

        }


        return v;
    }
        private void init() {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(getContext())
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(getActivity(), this)
                    .build();
            mSearchText.setOnItemClickListener(mAutocompleteClickListener);
            mplaceAutocompleteAdapter = new PlaceAutocompleteAdapter(getContext(), mGoogleApiClient, bounds, null);
            mSearchText.setAdapter(mplaceAutocompleteAdapter);
            mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                            || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                        geoLocate();
                    }
                    return false;
                }
            });
        }

        private void geoLocate() {
            String searchString = mSearchText.getText().toString();

            Geocoder geocoder = new Geocoder(getContext());
            List<Address> list = new ArrayList<>();
            try {
                list = geocoder.getFromLocationName(searchString, 1);
            } catch (IOException e) {
                Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
            }
            if (list.size() > 0) {
                Address address = list.get(0);
                Log.d(TAG, "geoLocate: found a location: " + address.toString());


            }
        }

        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

        }

//        private void hideSoftKeyboard() {
//            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        }

        private void moveCamera(LatLng latLng, float zoom, String title) {
            Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

            if (!title.equals("My Location")) {
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(title);
                mMap.addMarker(options);
                passingLocationData(latLng.latitude,latLng.longitude);
            }

            //hideSoftKeyboard();
        }

    public void passingLocationData(double latitude, double longitude) {

        Intent intent = new Intent("custom-event-name");
        intent.putExtra("key1",String.valueOf(latitude));
        intent.putExtra("key2",String.valueOf(longitude));
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
//        WeatherFragment fragment = new WeatherFragment();
//    Bundle arguments = new Bundle();
////    String x = String.valueOf(latLng.latitude);
////    String y = String.valueOf(latLng.longitude);
//    arguments.putDouble("key1",latitude);
//    arguments.putDouble("key2",longitude);
//    fragment.setArguments(arguments);
    }

//    public void passingLocationData(LatLng latLng)
//     {
//    WeatherFragment fragment = new WeatherFragment();
//    Bundle bundle = new Bundle();
//    String x = String.valueOf(latLng.latitude);
//    String y = String.valueOf(latLng.longitude);
//                bundle.putString("key1",x);
//                bundle.putString("key2",y);
//                fragment.setArguments(bundle);
//    }
//public static WeatherFragment newInstance(LatLng location)
//{
//    WeatherFragment fragment = new WeatherFragment();
//    Bundle arguments = new Bundle();
//  //  String x = String.valueOf(latLng.latitude);
// //   String y = String.valueOf(latLng.longitude);
//    arguments.putDouble("key1",location.latitude);
//    arguments.putDouble("key2",location.longitude);
//    fragment.setArguments(arguments);
//    return fragment;
//}


}
