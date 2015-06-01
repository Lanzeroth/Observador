package com.ocr.observador.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ocr.observador.MainActivity;
import com.ocr.observador.R;
import com.ocr.observador.events.DrawMarkersEvent;
import com.ocr.observador.events.GetMarkersEvent;
import com.ocr.observador.events.StartCameraIntentEvent;
import com.ocr.observador.events.UploadImageEvent;
import com.ocr.observador.model.ModelMarker;
import com.ocr.observador.utilities.AndroidBus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {
    private static View view;

    public static Bus mapBus;

    private AlertDialog markerDialog = null;


    CheckBox checkBox1_1;
    CheckBox checkBox1_2;
    CheckBox checkBox1_3;
    CheckBox checkBox2_1;
    CheckBox checkBox2_2;
    CheckBox checkBox2_3;

    ImageButton buttonPicture1;
    ImageButton buttonPicture2;
    ImageButton buttonVideo1;
    ImageButton buttonVideo2;

    String mPicture1String = "";
    String mPicture2String = "";

    String mVideo1String = "";
    String mVideo2String = "";


    List<ModelMarker> markerList;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
            ButterKnife.inject(this, view);

        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        mapBus = new AndroidBus();
        mapBus.register(this);

        setUpMapIfNeeded();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();

//        MainActivity.bus.post(getResources().getString(R.string.titlesMapFragment));
    }

    @Subscribe
    public void handleLocation(LatLng latLng) {
        CameraUpdate cameraUpdateFactory = CameraUpdateFactory.newLatLng(latLng);
        mMap.moveCamera(cameraUpdateFactory);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
    }

    @Subscribe
    public void drawMarkers(DrawMarkersEvent event) {
        markerList = getMarkers();
        MarkerOptions options = new MarkerOptions();
        for (ModelMarker marker : markerList) {
            options.position(new LatLng(marker.latitude, marker.longitude));
            options.title(marker.title);
            mMap.addMarker(options);
        }
    }


    /**
     * DB query
     *
     * @return db Markers
     */
    public List<ModelMarker> getMarkers() {
        return new Select().from(ModelMarker.class).execute();
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment. (THIS TOOK 2 HOURS)
            mMap = ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                inflateMarkerDialog(marker);

            }
        });
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                MainActivity.bus.post(new GetMarkersEvent(GetMarkersEvent.Type.STARTED, 1));
            }
        });
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

    /**
     * Inflates the dialog, this is the most important part of the app
     *
     * @param marker
     */
    private void inflateMarkerDialog(Marker marker) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.marker_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                markerDialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.setTitle("Observaciones de la " + marker.getTitle());
        markerDialog = builder.show();

        Button button1 = ButterKnife.findById(view, R.id.button_1);
        Button button2 = ButterKnife.findById(view, R.id.button_2);

        final LinearLayout container1 = ButterKnife.findById(view, R.id.container_1);
        final LinearLayout container2 = ButterKnife.findById(view, R.id.container_2);

        checkBox1_1 = ButterKnife.findById(view, R.id.checkBox_1_1);
        checkBox1_2 = ButterKnife.findById(view, R.id.checkBox_1_2);
        checkBox1_3 = ButterKnife.findById(view, R.id.checkBox_1_3);
        checkBox2_1 = ButterKnife.findById(view, R.id.checkBox_2_1);
        checkBox2_2 = ButterKnife.findById(view, R.id.checkBox_2_2);
        checkBox2_3 = ButterKnife.findById(view, R.id.checkBox_2_3);

        buttonPicture1 = ButterKnife.findById(view, R.id.button_picture_1);
        buttonPicture2 = ButterKnife.findById(view, R.id.button_picture_2);

        buttonVideo1 = ButterKnife.findById(view, R.id.button_video_1);
        buttonVideo2 = ButterKnife.findById(view, R.id.button_video_2);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilityToggle(container1);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilityToggle(container2);
            }
        });

        buttonPicture1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture(1);
            }
        });

        buttonVideo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeVideo(1);
            }
        });

        buttonPicture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture(2);
            }
        });

        buttonVideo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeVideo(2);
            }
        });


    }

    /**
     * Will try to take a VIDEO with the built in camera, upload it to GCS and then come back to
     * the response below with the image name to be put into the json
     *
     * @param category the "category" / checklist / division that was clicked
     */
    private void takeVideo(int category) {
        MainActivity.bus.post(new StartCameraIntentEvent(StartCameraIntentEvent.Type.STARTED, false, 1, category));
    }

    /**
     * Will try to take an IMAGE with the built in camera, upload it to GCS and then come back to
     * the response below with the image name to be put into the json
     *
     * @param category the "category" / checklist / division that was clicked
     */
    private void takePicture(int category) {
        MainActivity.bus.post(new StartCameraIntentEvent(StartCameraIntentEvent.Type.STARTED, true, 1, category));
    }

    /**
     * Last step of IMAGE upload
     *
     * @param event .
     */
    @Subscribe
    public void uploadImageToGCSResponse(UploadImageEvent event) {
        if (event.getResultCode() == 1) {
            Toast.makeText(getActivity(), "Imagen subida con exito...", Toast.LENGTH_SHORT).show();
            switch (event.getCategory()) {
                case 1:
                    mPicture1String = event.getImageName();
                    break;
                case 2:
                    mPicture2String = event.getImageName();
                    break;
            }
        }
    }

    /**
     * Last step of VIDEO upload
     *
     * @param event .
     */
    @Subscribe
    public void uploadVideoToGCSResponse(UploadImageEvent event) {
        if (event.getResultCode() == 1) {
            Toast.makeText(getActivity(), "Video subido con exito...", Toast.LENGTH_SHORT).show();
            switch (event.getCategory()) {
                case 1:
                    mVideo1String = event.getImageName();
                    break;
                case 2:
                    mVideo2String = event.getImageName();
                    break;
            }
        }
    }

    private void visibilityToggle(View v) {
        if (v.getVisibility() == View.GONE) {
            v.setVisibility(View.VISIBLE);
        } else if (v.getVisibility() == View.VISIBLE) {
            v.setVisibility(View.GONE);
        }
    }

    /**
     * creates a json with all the info at the moment.
     */
    private void createJSON() {
        JSONObject category1 = new JSONObject();
        try {
            category1.put("category", "initial observations");
            category1.put("checklist_item_1", checkBox1_1.isSelected());
            category1.put("checklist_item_2", checkBox1_2.isSelected());
            category1.put("checklist_item_3", checkBox1_3.isSelected());
            category1.put("picture", mPicture1String);
            category1.put("video", mVideo1String);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject category2 = new JSONObject();
        try {
            category2.put("category", "category 2");
            category2.put("checklist_item_1", checkBox2_1.isSelected());
            category2.put("checklist_item_2", checkBox2_2.isSelected());
            category2.put("checklist_item_3", checkBox2_3.isSelected());
            category2.put("picture", mPicture2String);
            category2.put("video", mVideo2String);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(category1);
        jsonArray.put(category2);

        JSONObject observations = new JSONObject();
        try {
            observations.put("Observations", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
