package com.exuberant.egws.fragments;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.exuberant.egws.R;
import com.exuberant.egws.activities.MainActivity;
import com.exuberant.egws.bottomsheets.OurHomeBottomSheet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReportsReference;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FloatingActionButton reportButton;
    private MaterialButton ourHomeButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initialize(view);
        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getFragmentSwitchInterface().switchToReport();
            }
        });
        ourHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OurHomeBottomSheet ourHomeBottomSheet = new OurHomeBottomSheet();
                ourHomeBottomSheet.show(getActivity().getSupportFragmentManager(), "Home");
            }
        });
        return view;
    }

    public void initialize(View view) {

        reportButton = view.findViewById(R.id.fab_report);
        ourHomeButton = view.findViewById(R.id.btn_our_home);

        mDatabase = FirebaseDatabase.getInstance();
        mReportsReference = mDatabase.getReference().child("buildings");

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.setMyLocationEnabled(true);
                    try {
                        // Customise the styling of the base map using a JSON object defined
                        // in a raw resource file.
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                        getContext(), R.raw.style));
                        if (!success) {
                            Log.e(TAG, "Style parsing failed.");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.e(TAG, "Can't find style. Error: ", e);
                    }
                }
            });
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

}
