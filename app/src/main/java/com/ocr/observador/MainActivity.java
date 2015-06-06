package com.ocr.observador;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.ocr.observador.custom.navigationDrawer.NavDrawerItem;
import com.ocr.observador.custom.navigationDrawer.NavDrawerListAdapter;
import com.ocr.observador.events.DrawMarkersEvent;
import com.ocr.observador.events.GetCasillasSafeKeyEvent;
import com.ocr.observador.events.GetCategoriesEvent;
import com.ocr.observador.events.MarkerClickedEvent;
import com.ocr.observador.events.RegisterUserEvent;
import com.ocr.observador.events.StartCameraIntentEvent;
import com.ocr.observador.events.StartRegisteringUserEvent;
import com.ocr.observador.events.UploadImageEvent;
import com.ocr.observador.fragments.ListFragment;
import com.ocr.observador.fragments.MapFragment;
import com.ocr.observador.fragments.ObservationsFragment;
import com.ocr.observador.jobs.GetCasillasSafeKeyJob;
import com.ocr.observador.jobs.GetCategoriesJob;
import com.ocr.observador.jobs.RegisterUserJob;
import com.ocr.observador.jobs.UploadImageToGCSJob;
import com.ocr.observador.jobs.UploadVideoToGCSJob;
import com.ocr.observador.model.ModelMarker;
import com.ocr.observador.model.RegisteredUser;
import com.ocr.observador.services.MagicalLocationService;
import com.ocr.observador.utilities.AndroidBus;
import com.orhanobut.logger.Logger;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.path.android.jobqueue.JobManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private final String TAG = MainActivity.class.getSimpleName();

    public static Bus bus;

    public static FragmentManager fragmentManagerGlobal;

    JobManager jobManager;

    Uri mImageUri;

    private static final String BUCKET_NAME = "observador-media";

    private int mCategoryId;

    private static final int IMAGE_REQUEST = 100;
    private static final int VIDEO_REQUEST = 200;


    // Navigation Drawer
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;

    @InjectView(R.id.list_slidermenu)
    ListView mDrawerList;
    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.linearLayoutDrawer)
    RelativeLayout mDrawerRelativeLayout;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;


    private AlertDialog markerDialog = null;

    private List<ModelMarker> mMarkerList;

    /**
     * register user
     */
    private String accountType;
    private long age;
    private String mEmail;
    private String name;
    private String installationId;

    public static boolean finishedUserRegistration = false;

    public static String nationalIdSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        bus = new AndroidBus();
        bus.register(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isLocationServiceRunning = prefs.getBoolean(FirstApplication.PREF_IS_LOCATION_SERVICE_RUNNING, false);

        Logger.d("Check service running " + isLocationServiceRunning);

//        if (!isLocationServiceRunning) {
        Intent mServiceIntent = new Intent(this, MagicalLocationService.class);
        startService(mServiceIntent);
//        }


        jobManager = FirstApplication.getInstance().getJobManager();

        /** toolBar **/
        setUpToolBar();

        setUpDrawer();

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            reactToDrawerClick(0);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


    }


    /**
     * sets up the top bar
     */
    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Finishes to draw the drawer
     */
    private void setUpDrawer() {
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);
        navDrawerItems = new ArrayList<NavDrawerItem>();

        // Map
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // List
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // Recycle the typed array
        navMenuIcons.recycle();
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                mDrawerList.setItemChecked(1, true);
                mDrawerList.setSelection(1);
                getSupportActionBar().setTitle(R.string.app_name);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();
    }


    /**
     * fill user info into the global variables
     */
    private void fillUserInfoThenRegister() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        installationId = ParseInstallation.getCurrentInstallation().getInstallationId();
        Logger.d("InstallationId ::" + installationId);

        accountType = "G+";
        age = 30L;
        mEmail = parseUser.getEmail();
        name = parseUser.getUsername();
        registerUserBackend();
    }

    /**
     * BackendCall
     * Registers the user on the backend
     */
    private void registerUserBackend() {
        jobManager.addJobInBackground(new RegisterUserJob(accountType, age, mEmail, name, installationId));
    }

    @Subscribe
    public void registerUserBackendResponse(RegisterUserEvent event) {
        if (event.getResultCode() == 1) {
            registerUser();
            getMarkersFromBackend(new GetCasillasSafeKeyEvent(GetCasillasSafeKeyEvent.Type.STARTED, 1));
        } else if (event.getResultCode() == 99) {
            Logger.i("BACKEND, Bad-registerUserBackend");
        }
    }

    /**
     * DatabaseSave
     * checks where is the user registering from and saves is to the database
     */
    private void registerUser() {
        // we need to check if the user is already in the database
        RegisteredUser previouslyRegisteredUser = checkForExistingUser(mEmail);
        if (previouslyRegisteredUser == null) {
            RegisteredUser registeredUser = new RegisteredUser(
                    accountType,
                    age,
                    mEmail,
                    name,
                    installationId
            );
            registeredUser.save();

        }
    }

    /**
     * DatabaseQuery
     * We check in our database if the user is already registered
     *
     * @param emailAsUsername use the mEmail as username
     * @return a registered user if exists
     */
    private RegisteredUser checkForExistingUser(String emailAsUsername) {
        return new Select()
                .from(RegisteredUser.class)
                .where("Email = ?", emailAsUsername)
                .executeSingle();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Logger.i("Location services connected.");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logger.i("Location services suspended. Please reconnect.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Logger.i("Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    /**
     * gets the location and then asks Map fragment to update it
     *
     * @param location
     */
    private void handleNewLocation(Location location) {
        Logger.d(location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MapFragment.mapBus.post(latLng);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * DB query
     *
     * @return db Markers
     */
    public ModelMarker queryMarker(String nationalId) {
        return new Select().
                from(ModelMarker.class).
                where("national_id = ?", nationalId).
                executeSingle();
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            reactToDrawerClick(position);
        }
    }

    /**
     * Displaying fragment view for selected nav drawer list item or sending an action to the fragment
     */
    private void reactToDrawerClick(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        boolean isFragmentTransition = false;
        switch (position) {
            case 0:
                fragment = new MapFragment();
                break;
            case 1:
                fragment = new ListFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));
                fragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, fragment)
                    .commit();
            Log.d(TAG, "fragment added " + fragment.getTag());

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            // update selected item and title, then close the drawer
            mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
        } else if (!isFragmentTransition) {
            Log.i(TAG, "Action");

        } else {
            // error in creating fragment
            Log.e(TAG, "Error in creating fragment");
        }
    }

    @Subscribe
    public void registerUserToBackend(StartRegisteringUserEvent event) {
        if (event.getResultCode() == 1) {
            fillUserInfoThenRegister();
        }
    }

    /**
     * get the markers, save them into the db and then query it
     */
    @Subscribe
    public void getMarkersFromBackend(GetCasillasSafeKeyEvent event) {
        if (event.getResultCode() == 1 && event.getType() == GetCasillasSafeKeyEvent.Type.STARTED) {

            jobManager.addJobInBackground(new GetCasillasSafeKeyJob(mEmail));
        }
        if (event.getResultCode() == 1 && event.getType() == GetCasillasSafeKeyEvent.Type.COMPLETED) {
            MapFragment.mapBus.post(new DrawMarkersEvent(DrawMarkersEvent.Type.STARTED, 1));
        }
    }


    /**
     * Every fragment opened from the drawer must call this method to set the
     * correct toolbar title
     *
     * @param string the title that we want to show on the toolbar
     */
    @Subscribe
    public void changeTitle(String string) {
        getSupportActionBar().setTitle(string);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Subscribe
    public void startCameraIntent(StartCameraIntentEvent event) {
        if (event.getResultCode() == 1) {
            if (event.isImage()) {
                mCategoryId = event.getCategoryNumber();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mImageUri = Uri.fromFile(getTempFile(this, true));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(intent, IMAGE_REQUEST);
            } else {
                mCategoryId = event.getCategoryNumber();
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(intent, VIDEO_REQUEST);
            }
        }
    }

    private File getTempFile(Context context, boolean isImage) {
        //it will return /sdcard/image.tmp
        final File path = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
        if (!path.exists()) {
            path.mkdir();
        }
        if (isImage) {
            return new File(path, "image.tmp");
        } else {
            return new File(path, "video.mp4");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_REQUEST) {
                final File file = getTempFile(this, true);
                try {
                    Bitmap captureBmp = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                    // do whatever you want with the bitmap (Resize, Rename, Add To Gallery, etc)
                    file.delete();
                    uploadImageToGCS(captureBmp);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == VIDEO_REQUEST) {
                try {
                    AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(data.getData(), "r");
                    FileInputStream fis = videoAsset.createInputStream();
                    File tmpFile = new File(Environment.getExternalStorageDirectory(), "VideoFile.3gp");
                    FileOutputStream fos = new FileOutputStream(tmpFile);

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = fis.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                    fis.close();
                    fos.close();
                    uploadVideoToGCS(tmpFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    private void uploadVideoToGCS(File file) {
        Toast.makeText(this, "Subiendo Video...", Toast.LENGTH_SHORT).show();
        try {
            byte[] video = readFile(file);
            jobManager.addJobInBackground(new UploadVideoToGCSJob(video, this, BUCKET_NAME, mCategoryId));

        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.i("Initiate Video upload");
    }

    /**
     * we need to transform the video file to byte array again :(
     *
     * @param file video file
     * @return byte array
     * @throws IOException
     */
    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    /**
     * The response is on map fragment
     *
     * @param image
     */
    public void uploadImageToGCS(Bitmap image) {
        Toast.makeText(this, "Subiendo Imagen...", Toast.LENGTH_SHORT).show();
        Logger.i("Initiating Image upload");
        jobManager.addJobInBackground(new UploadImageToGCSJob(image, this, BUCKET_NAME, mCategoryId));
    }


    /**
     * Last step of IMAGE upload
     *
     * @param event .
     */
    @Subscribe
    public void uploadImageToGCSResponse(UploadImageEvent event) {
        if (event.getResultCode() == 1) {
            Toast.makeText(this, "Imagen subida con exito...", Toast.LENGTH_SHORT).show();
            switch (event.getCategory()) {
                case 1:
//                    mPicture1String = event.getImageName();
                    break;
                case 2:
//                    mPicture2String = event.getImageName();
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
            Toast.makeText(this, "Video subido con exito...", Toast.LENGTH_SHORT).show();
            switch (event.getCategory()) {
                case 1:
//                    mVideo1String = event.getImageName();
                    break;
                case 2:
//                    mVideo2String = event.getImageName();
                    break;
            }
        }
    }


    /**
     * we gotta get the categories from backend
     *
     * @param event
     */
    @Subscribe
    public void reactToMarkerClickedEvent(MarkerClickedEvent event) {
        if (event.getResultCode() == 1) {
            ModelMarker markerClicked  = queryMarker(event.getMarkerId());
            jobManager.addJob(new GetCategoriesJob(markerClicked.url_key, markerClicked.national_id));
            //call job to get categories
        }
    }

    @Subscribe
    public void getCategoriesResponse(GetCategoriesEvent event) {
        if (event.getResultCode() == 1) {
            nationalIdSelected = event.getNationalId();
            Fragment fragment = new ObservationsFragment();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));
                fragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, fragment)
                    .commit();
            Log.d(TAG, "fragment added " + fragment.getTag());
        }
    }


    /**
     * We want to exit the app on many back pressed
     */
    @Override
    public void onBackPressed() {
        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        if (fragments > 1) {
            super.onBackPressed();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Esta seguro que quiere salir de la aplicacion?")
                    .setCancelable(false)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            // this will call for a finish on the top login activity
                            //LoginActivityBack.loginBus.post(true);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }
}
