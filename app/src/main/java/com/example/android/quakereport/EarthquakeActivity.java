/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.StateSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.novoda.merlin.Merlin;
import com.novoda.merlin.MerlinBuilder;
import com.novoda.merlin.registerable.connection.Connectable;
import com.novoda.merlin.registerable.disconnection.Disconnectable;

import com.example.android.quakereport.maps_activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class EarthquakeActivity extends AppCompatActivity
        implements LoaderCallbacks<List<Earthquake>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static ArrayList<Earthquake> FinalQuakes;
    private static final String LOG_TAG = EarthquakeActivity.class.getName();
    private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";
    private static final int EARTHQUAKE_LOADER_ID = 1;
    private EarthquakeAdapter mAdapter;
    ArrayList<Earthquake> OfflineEarthquakes;// = new ArrayList<Earthquake>();
    private TextView mEmptyStateTextView;
    SQLiteDatabase sqLiteDatabaseWrite;
    private boolean NetworkFlag;
    private Merlin merlin;
    private TextView StatusView;
    boolean startflag;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton floatingActionButton;
    private TextView emptyView;
    private FloatingActionButton ShowMaps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);
        ListView earthquakeListView = (ListView) findViewById(R.id.list);
        startflag = true;
        emptyView = (TextView) findViewById(R.id.empty_view);
        emptyView.setVisibility(View.INVISIBLE);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.FAB);
        ShowMaps = (FloatingActionButton)findViewById(R.id.ShowMaps);
        ShowMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),Map_display.class);
                startActivity(intent);
            }
        });
        //floatingActionButton.setImageResource(R.drawable.loc);

        Dbhelper db = new Dbhelper(this);
        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
        sqLiteDatabase.execSQL(Dbhelper.Create);
        OfflineEarthquakes = GetOfflineData();
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());
        merlin = new Merlin.Builder().withConnectableCallbacks().withDisconnectableCallbacks().build(this);
        GetConnectivity();
        earthquakeListView.setAdapter(mAdapter);
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            NetworkFlag = true;
        }else{
            startflag = false;
            StatusView = (TextView)findViewById(R.id.textView);
            /*StatusView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StatusView.setText("Offline Mode :(");
                }
            });*/
            swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeContainer);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Toast.makeText(getApplicationContext(),"Offline!",Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
            StatusView.setVisibility(View.VISIBLE);
            StatusView.setTextColor(Color.WHITE);
            StatusView.setText("Offline Mode");
            StatusView.setBackgroundColor(getResources().getColor(R.color.OfflineColor));
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // And register to be notified of preference changes
        // So we know when the user has adjusted the query settings
        prefs.registerOnSharedPreferenceChangeListener(this);

        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Earthquake currentEarthquake = mAdapter.getItem(position);

                    Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                    startActivity(websiteIntent);

            }
        });
        if (NetworkFlag) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();


            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        } else {
            Toast.makeText(this,"Offline Mode",Toast.LENGTH_LONG).show();
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            mAdapter.addAll(OfflineEarthquakes);

        }
        if(NetworkFlag){
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),maps_activity.class);
                    startActivity(intent);
                }
            });
        }else{
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(),"Cant Access Map In Offline Mode.",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        merlin.bind();
    }

    @Override
    protected void onPause() {
        //merlin.unbind();
        super.onPause();
        merlin.bind();
    }

    private ArrayList<Earthquake> GetOfflineData() {
        ArrayList<Earthquake> EarthquakeList = new ArrayList<Earthquake>();
        Dbhelper dbhelper = new Dbhelper(this);
        Cursor cursor = dbhelper.RetrieveData();
        if (cursor.getCount()==0){
            Log.e("TaG","Error While Fetching");
        }else {
            while(cursor.moveToNext()) {
                double mag = cursor.getDouble(0);
                String loc = cursor.getString(1);
                String uRL = cursor.getString(2);
                long time = cursor.getLong(3);
                EarthquakeList.add(new Earthquake(mag,loc,time,uRL));
                Log.i("TaG","Retrieved Data " + mag + " " + loc + " " +uRL +" "+time );
            }
        }
        return EarthquakeList;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.settings_min_magnitude_key)) || key.equals(getString(R.string.settings_order_by_key))){

                mAdapter.clear();

            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            getLoaderManager().restartLoader(EARTHQUAKE_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("limit", "200");
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("orderby", orderBy);
        if(maps_activity.latitude== -361 && maps_activity.longitude== -361){

        }else {
            uriBuilder.appendQueryParameter("latitude", maps_activity.latitude + "");
            uriBuilder.appendQueryParameter("longitude", maps_activity.longitude + "");
            uriBuilder.appendQueryParameter("maxradiuskm", "1000");
        }
        return new EarthquakeLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {
        View loadingIndicator = findViewById(R.id.loading_indicator);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setRefreshing(false);
        loadingIndicator.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(),"Done Loading!",Toast.LENGTH_SHORT).show();

        mAdapter.clear();

        if (earthquakes != null && !earthquakes.isEmpty()) {
            StoreDB(earthquakes);
            FinalQuakes = (ArrayList<Earthquake>)earthquakes;
            emptyView.setVisibility(View.INVISIBLE);
            /*for (int i = 0 ; i < earthquakes.size();i++){
                Log.i("TaG",earthquakes.get(i).toString());
            }*/
            mAdapter.addAll(earthquakes);
        }else{
            FinalQuakes = (ArrayList<Earthquake>)earthquakes;
            emptyView.setText("It Seems No Earthquakes appeared here.");
            emptyView.setVisibility(View.VISIBLE);
        }
    }
    public void GetConnectivity(){
        merlin.registerConnectable(new Connectable() {
            @Override
            public void onConnect() {
                NetworkFlag = true;
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(),maps_activity.class);
                        startActivity(intent);
                    }
                });
                Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_SHORT).show();
                StatusView = (TextView)findViewById(R.id.textView);
                StatusView.setBackgroundColor(getResources().getColor(R.color.BackOnline));
                if(startflag){
                    startflag = false;
                    StatusView.setText("Connected");
                    swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeContainer);
                    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            getLoaderManager().restartLoader(EARTHQUAKE_LOADER_ID, null,EarthquakeActivity.this);
                            //swipeRefreshLayout.setRefreshing(false);
                            /*new Handler().postDelayed(new Runnable() {
                                @Override public void run() {
                                    // Stop animation (This will be after 3 seconds)
                                    swipeRefreshLayout.setRefreshing(false);
                                    Toast.makeText(getApplicationContext(),"Refreshed..!",Toast.LENGTH_SHORT).show();
                                }
                            }, 4000);*/

                        }
                    });
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Do some stuff
                                    StatusView.setVisibility(View.GONE);
                                }
                            });
                        }
                    };
                    thread.start();
                }else {
                    StatusView.setVisibility(View.VISIBLE);
                    StatusView.setText("Back Online(Pull To Refresh)");
                    StatusView.setTextColor(Color.WHITE);
                    StatusView.setBackgroundColor(getResources().getColor(R.color.BackOnline));
                    /*StatusView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            StatusView.setVisibility(View.GONE);
                            View loadingIndicator = findViewById(R.id.loading_indicator);
                            loadingIndicator.setVisibility(View.VISIBLE);

                            getLoaderManager().restartLoader(EARTHQUAKE_LOADER_ID, null,EarthquakeActivity.this);
                        }
                    });*/
                    swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeContainer);
                    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            StatusView.setVisibility(View.GONE);

                            getLoaderManager().restartLoader(EARTHQUAKE_LOADER_ID, null,EarthquakeActivity.this);
                            /*new Handler().postDelayed(new Runnable() {
                                @Override public void run() {
                                    // Stop animation (This will be after 3 seconds)
                                    swipeRefreshLayout.setRefreshing(false);
                                    Toast.makeText(getApplicationContext(), "Refreshed..!", Toast.LENGTH_LONG).show();
                                }
                            }, 4000);*/
                        }
                    });

                }


                //setViews(con);
            }
        });

        merlin.registerDisconnectable(new Disconnectable() {
            @Override
            public void onDisconnect() {
                NetworkFlag = false;
                startflag = false;
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(),"Cant Access Map In Offline Mode.",Toast.LENGTH_SHORT).show();
                    }
                });
                //setViews(con);
                Toast.makeText(getApplicationContext(),"Disconnected",Toast.LENGTH_SHORT).show();
                StatusView = (TextView)findViewById(R.id.textView);
                /*StatusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StatusView.setText("Offline Mode :(");
                    }
                });*/
                swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeContainer);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Toast.makeText(getApplicationContext(),"Offline!",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        StatusView.setVisibility(View.VISIBLE);
                        StatusView.setTextColor(Color.WHITE);
                        StatusView.setText("Offline Mode");
                        StatusView.setBackgroundColor(getResources().getColor(R.color.OfflineColor));
                    }
                });

            }
        });
    }
    private int Min(int a,int b){
        return a<b?a:b;
    }
    /*public ArrayList<Earthquake> getQuakes(){
        return FinalQuakes;
    }*/
    private void StoreDB(List<Earthquake> earthquakes)throws SQLiteException {
        Dbhelper dbhelper = new Dbhelper(this);
        sqLiteDatabaseWrite = dbhelper.getWritableDatabase();
        sqLiteDatabaseWrite.execSQL("DROP TABLE IF EXISTS "+Dbhelper.TB_NAME);
        sqLiteDatabaseWrite.execSQL(Dbhelper.Create);
        //earthquakes = (ArrayList<Earthquake>)earthquakes;
        for (int i = 0 ; i < Min(10,earthquakes.size());i++){
            ContentValues contentValues = new ContentValues();
            contentValues.put(Dbhelper.MAG,earthquakes.get(i).getMagnitude());
            contentValues.put(Dbhelper.LOC,earthquakes.get(i).getLocation());
            contentValues.put(Dbhelper.uRL,earthquakes.get(i).getUrl());
            contentValues.put(Dbhelper.tIME,earthquakes.get(i).getTimeInMilliseconds());
            long res = sqLiteDatabaseWrite.insert(Dbhelper.TB_NAME,null,contentValues);
            if (res == -1){
                Log.e("TaG","Error In Inserting ContentValues at " + i);
            }else{
                Log.i("TaG","Successfully Inserted " + i + "th element at " + res);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(NetworkFlag) {
            if (id == R.id.action_settings) {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }else{
            Toast.makeText(this,"Cant Access In Offline Mode",Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
