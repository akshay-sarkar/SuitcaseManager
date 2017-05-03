package edu.uta.cse5320.suitcasemanager;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.uta.cse5320.dao.BagAdapter;
import edu.uta.cse5320.dao.BagData;
import edu.uta.cse5320.dao.BagHelper;
import edu.uta.cse5320.util.ApplicationConstant;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static edu.uta.cse5320.util.ApplicationConstant.root_prop;
import static edu.uta.cse5320.util.ApplicationConstant.root_trip_prop;
import static edu.uta.cse5320.util.ApplicationConstant.trip_bag_prop;
import static edu.uta.cse5320.util.ApplicationConstant.trip_val;

public class BagListActivity extends AppCompatActivity {

    FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog progressDialog;
    private Context ctx;
    public static DatabaseReference myDbRef, imageURLRef;
    private StorageReference mStorageRef;
    private ListView listBagTrip;
    BagAdapter myAdapter;
    public static HashMap<String, String> hmap, hmapval ;
    File photoFile;
    private static int CAMERA_REQUEST_CODE = 200;
    private TextView bagHeading;

    final int PERMISSION_REQUEST_LOCATION = 101;
    LocationManager locationManager;
    LocationListener gpsListener, networklistener;

    //List<String> bagArray ;
    String TAG = "Suitcase Manager::BagScreen";
    int i = 1;

    private BagHelper bagHelperDB;
    ArrayList<BagData> bagDataList;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private GoogleApiClient mGoogleApiClient;
    private InputMethodManager imm;

    // for font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bag_list);
        //shared prefernece for tip flag
        final SharedPreferences pref = getApplicationContext().getSharedPreferences(ApplicationConstant.MySharedPrefName, MODE_PRIVATE);
        final SharedPreferences.Editor editor = getSharedPreferences(ApplicationConstant.MySharedPrefName, MODE_PRIVATE).edit();


        // Left Menu / Navigational Layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_bag_list);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.menu_open, R.string.menu_close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final NavigationView nv = (NavigationView)findViewById(R.id.nv2);
        bagHeading = (TextView) findViewById(R.id.textViewBagHeading);


        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if(menuItem.getTitle().equals(ApplicationConstant.logout)) {
                    mAuth.signOut();
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                }else if(menuItem.getTitle().equals(ApplicationConstant.Airline_Information)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    Intent intent = new Intent(ctx, AirlineActivity.class);
                    startActivity(intent);
                }else if(menuItem.getTitle().equals(ApplicationConstant.Home)) {
                    finish();
                }else if(menuItem.getTitle().equals(ApplicationConstant.Tip_On)) {
                    menuItem.setVisible(false);
                    nv.getMenu().findItem(R.id.nav5).setVisible(true);
                    editor.putBoolean(ApplicationConstant.tipflag, false);
                    editor.apply();

                    //ApplicationConstant.tipflag = false;
                }else if(menuItem.getTitle().equals(ApplicationConstant.Tip_Off)) {
                    menuItem.setVisible(false);
                    nv.getMenu().findItem(R.id.nav4).setVisible(true);
                    editor.putBoolean(ApplicationConstant.tipflag, true);
                    editor.apply();
                }else{
                    System.out.println("--- Reache Here -- "+ menuItem.getItemId());
                }
                return true;
            }
        });

        checkInternet();
        checkLocationPermisssion();

        //Progress for operations
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retrieving Your Data..");
        progressDialog.show();

        /* Getting Data from the data */
        Intent intent = getIntent();
        trip_val = intent.getStringExtra(TripListActivity.EXTRA_MESSAGE);

        bagHelperDB = new BagHelper(this);
        bagDataList = new ArrayList<>();
        Cursor data = bagHelperDB.getListContents();
        int numRows = data.getCount();

        //bagArray = new ArrayList<>();
        hmap = new HashMap<String, String>();
        hmapval = new HashMap<String, String>();

        mAuth = FirebaseAuth.getInstance();
        ctx = getApplicationContext();
        user = mAuth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference(root_prop).child(user.getUid()).child(root_trip_prop).child(trip_val).child(trip_bag_prop);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //hmap = new HashMap<String, String>();

        //to check if its bag details are empty and removing the progress dialog
        myDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    System.out.println(TAG+ " onDataChange -> Empty" );
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        myDbRef.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println(TAG+ " onChildAdded" + dataSnapshot);
                progressDialog.dismiss();
                BagData bagData = dataSnapshot.getValue(BagData.class);

                //Key - Value : TripName - f_id
                hmap.put(String.valueOf(bagData.getId()), dataSnapshot.getKey());
                hmapval.put(bagData.getBagName(),dataSnapshot.getKey());

                /* Inserting data in DB*/
                int count = bagHelperDB.getListContent(bagData.getId());
                if(count <= 0){
                    System.out.println("Not Inserted!! Inserting Now..");
                    bagHelperDB.addDataCompleteSync(bagData.getId(), bagData.getBagName(), bagData.getItemQuantity(), bagData.getImageUrl1(),bagData.getImageUrl2(),bagData.getImageUrl3());
                }

                /* Adding in List */
                bagDataList.add(bagData);
                updateListView();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println(TAG + "onChildChanged" );
                BagData bagData = dataSnapshot.getValue(BagData.class);

                if(bagData != null){

                    /* Updating data in DB*/
                    boolean flag = bagHelperDB.updateDetails(bagData.getId(), bagData.getBagName(), bagData.getItemQuantity(), bagData.getImageUrl1(),bagData.getImageUrl2(),bagData.getImageUrl3());

                    /* updating in List */
                    if(flag){
                        Iterator<BagData> itr = bagDataList.iterator();
                        while (itr.hasNext()) {
                            BagData element = itr.next();
                            if(element.getId() == bagData.getId()) {
                                bagDataList.remove(element);
                                break;
                            }
                        }
                        bagDataList.add( bagData);
                        myAdapter.notifyDataSetChanged();
                        updateListView();
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                System.out.println(TAG + " onChildRemoved");
                BagData bagData = dataSnapshot.getValue(BagData.class);

                //Update Database and List Here
                boolean flag = bagHelperDB.deleteContent(bagData.getId());

                if(flag){
                    hmap.remove(String.valueOf(bagData.getId()));
                    Iterator<BagData> itr = bagDataList.iterator();
                    while (itr.hasNext()) {
                        BagData element = itr.next();
                        if(element.getId() == bagData.getId()) {
                            bagDataList.remove(element);
                            break;
                        }
                    }
                    updateListView();
                    myAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });


        /* List Trip Data */
        listBagTrip = (ListView) findViewById(R.id.listBags);
        // initiate the listadapter
        myAdapter = new BagAdapter(this, this, R.layout.bag_list_layout, bagDataList, myDbRef , mStorageRef);
        myAdapter.setNotifyOnChange(true);
        listBagTrip.setAdapter(myAdapter);

        /* Floating Button for moving to Add Bags */
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.floatingButtonAddBag);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createBags();
            }
        });

        new MaterialShowcaseView.Builder(this)
                .setTarget(myFab)
                .setDismissText("GOT IT")
                .setContentText("Click this to Add New Bags")
                .setDelay(1) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse("104") // provide a unique ID used to ensure it is only shown once
                .show();
    }

    public void createBags(){

        String editBagName = "Bag";
        int editBagItems = 0;

        long id = bagHelperDB.addData(editBagName, editBagItems, "", "", "");
        if(id == -1){
            System.out.println("Not Inserted");
        }
        BagData bagData = new BagData(id, editBagName, editBagItems, "", "", "");
        myDbRef.push().setValue(bagData);
        Toast.makeText(ctx, "Bag Added", Toast.LENGTH_SHORT).show();

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(BagListActivity.this,"105");
        sequence.setConfig(config);
        sequence.addSequenceItem(bagHeading,"Click on the Bag Name to Items for the bag.", "GOT IT");
        sequence.addSequenceItem(bagHeading,"Long Pressing the Bag Name allows you to Edit & Delete Bags", "GOT IT");
        sequence.addSequenceItem(bagHeading,"Pressing the Image once allows you to take the photo", "GOT IT");
        sequence.addSequenceItem(bagHeading,"Long pressing the Image shows the Latitude and Longitude as well the actual image", "GOT IT");
        sequence.start();
    }
    
    private void updateListView(){
        myAdapter.notifyDataSetChanged();
        listBagTrip.invalidate();
    }

    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                if( firebaseAuth.getCurrentUser() == null){
                    Intent loginScreenIntent = new Intent(BagListActivity.this, MainActivity.class);
                    loginScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginScreenIntent);
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == CAMERA_REQUEST_CODE) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
        }*/
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            BagAdapter.uploadPhoto();
        }

    }

    //Left Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public static HashMap<String, String> getTripMap(){
        return hmap;
    }

    private void checkInternet(){
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                NetworkInfo info = (NetworkInfo) extras.getParcelable("networkInfo");
                NetworkInfo.State state = info.getState();
                Log.d("TEST Internet", info.toString() + " " + state.toString());
                final AlertDialog alertDialog = new AlertDialog.Builder(BagListActivity.this).create();

                if (state != NetworkInfo.State.CONNECTED) {
                    alertDialog.setTitle("Network Problem");
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage("No Network Available. Check Internet Connection");
                    alertDialog.setIcon(R.mipmap.ic_error);
                    alertDialog.show();
                }

                else {
                    if(alertDialog!=null )
                    alertDialog.dismiss();
                }
            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver((BroadcastReceiver) br, intentFilter);
    }

    public void checkLocationPermisssion() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(BagListActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(BagListActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                        Snackbar.make(findViewById(R.id.activity_bag_list), "Suitcase Manager requires this permission to locate your contacts", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                ActivityCompat.requestPermissions(BagListActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
                            }
                        }).show();

                    } else {
                        ActivityCompat.requestPermissions(BagListActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
                    }
                } else {
                    startLocationUpdates();
                }
            } else {
                startLocationUpdates();
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Error requesting permission", Toast.LENGTH_LONG).show();
        }

    }

    private void startLocationUpdates() {
        if ( Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getBaseContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission( getBaseContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
            networklistener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    ApplicationConstant.latitude = location.getLatitude();
                    ApplicationConstant.longitude  = location.getLongitude();
                    ApplicationConstant.accuracy = location.getAccuracy();
                    //Toast.makeText(ctx, "Location Updated in Background"+ApplicationConstant.latitude ,Toast.LENGTH_LONG).show();
                    Log.d("Location Update", "Location Updated in Background"+ApplicationConstant.latitude);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ApplicationConstant.minTime, ApplicationConstant.minDistance, networklistener);//2 min, 150meter
        }
        catch(Exception e){
            Toast.makeText(getBaseContext(), "Error, Location not available", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(BagListActivity.this, "Geo-Tagging for bag images won't work.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if ( Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getBaseContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission( getBaseContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            locationManager.removeUpdates(gpsListener);
            locationManager.removeUpdates(networklistener);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
