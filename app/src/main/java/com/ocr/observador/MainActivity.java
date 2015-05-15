package com.ocr.observador;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ocr.observador.custom.navigationDrawer.NavDrawerItem;
import com.ocr.observador.custom.navigationDrawer.NavDrawerListAdapter;
import com.ocr.observador.fragments.MapFragment;
import com.ocr.observador.utilities.AndroidBus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    public static Bus bus;

    public static FragmentManager fragmentManagerGlobal;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        bus = new AndroidBus();
        bus.register(this);


        /**toolBar **/
        setUpToolBar();

        setUpDrawer();

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            reactToDrawerClick(0);
        }

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
                fragment = new MapFragment();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                    .setMessage("Esta seguro que quiere salir de la aplicación?")
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
