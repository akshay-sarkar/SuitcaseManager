package edu.uta.cse5320.suitcasemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import edu.uta.cse5320.dao.AirlineAdapter;
import edu.uta.cse5320.dao.AirlineData;
import edu.uta.cse5320.util.ApplicationConstant;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AirlineActivity extends AppCompatActivity {

    // Declare Variables
    ListView list;
    AirlineAdapter adapter;
    EditText editSearch;
    ArrayList<AirlineData> airlineArrayList;
    private DatabaseReference myDbRef;
    private DrawerLayout mDrawerLayout;
    private ProgressDialog progressDialog;
    private ActionBarDrawerToggle mToggle;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context ctx;
    private GoogleApiClient mGoogleApiClient;
    FirebaseUser user;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    // for font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airline);

        // Left Menu / Navigational Layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_airline);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.menu_open, R.string.menu_close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView nv = (NavigationView)findViewById(R.id.nv1);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if(menuItem.getTitle().equals(ApplicationConstant.logout)) {
                    mAuth.signOut();
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                }else if(menuItem.getTitle().equals(ApplicationConstant.Airline_Information)) {
//                    Intent intent = new Intent(ctx, AirlineActivity.class);
//                    startActivity(intent);
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }else if(menuItem.getTitle().equals(ApplicationConstant.Home)) {
                    finish();
                }else{
                    System.out.println("--- Reache Here -- "+ menuItem.getItemId());
                }
                return true;
            }
        });

        //Progress for operations
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retrieving Your Data..");
        progressDialog.show();

        mAuth = FirebaseAuth.getInstance();
        ctx = getApplicationContext();
        user = mAuth.getCurrentUser();

        airlineArrayList = new ArrayList<AirlineData>();
        // Locate the ListView in listview_main.xml
        list = (ListView) findViewById(R.id.listViewAirline);
        list.setTextFilterEnabled(true);

        // Pass results to ListViewAdapter Class
        adapter = new AirlineAdapter(this, R.layout.airline_list_layout, airlineArrayList);
        adapter.setNotifyOnChange(true);
        list.setAdapter(adapter);

        // Locate the EditText in listview_main.xml
        editSearch = (EditText) findViewById(R.id.editTextTripAirlineSearch);

        // Capture Text in EditText
        editSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                if (adapter != null) {
                    adapter.getFilter().filter(arg0.toString());
                } else {
                    Log.d("filter", "no filter availible");
                }
            }
        });


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference(ApplicationConstant.airline_prop);

        myDbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println(" onChildAdded"+dataSnapshot);
                AirlineData airlineData = dataSnapshot.getValue(AirlineData.class);
                airlineArrayList.add(airlineData);

                // Dimiss the dialog box
                progressDialog.dismiss();
//                //Key - Value : TripName - f_id
//                hmap.put(tripData.getTripName(), dataSnapshot.getKey());
//
//                /* Inserting data in DB only when not existed*/
//                int count = tripHelperDB.getListContent(tripData.getId());
//                if(count <= 0){
//                    System.out.println("Not Inserted!! Inserting Now..");
//                    tripHelperDB.addDataCompleteSync(tripData.getId(), tripData.getTripName(), tripData.getTripStartDate(), tripData.getTripEndDate(),tripData.getTripAirlineName(),tripData.getTripDetails());
//                }
//                /* Adding in List */
//                tripDataList.add(tripData);
//                updateListView();
//
//                // Dimiss the dialog box
//                progressDialog.dismiss();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
//                System.out.println(TAG + "onChildChanged" + dataSnapshot.getKey() +" Value :"+ dataSnapshot.getValue().toString());
//                TripData tripData = dataSnapshot.getValue(TripData.class);
//                if(tripData != null){
//
//                    Object oldKey = getKeyFromValue(hmap, dataSnapshot.getKey());
//                    //Key - Value : TripName - f_id
//                    hmap.remove(oldKey);
//                    hmap.put(tripData.getTripName(), dataSnapshot.getKey());
//                    /* Updating data in DB*/
//                    boolean flag = tripHelperDB.updateDetails(tripData.getId(), tripData.getTripName(), tripData.getTripStartDate(), tripData.getTripEndDate(),tripData.getTripAirlineName(),tripData.getTripDetails());
//
//                    /* updating in List */
//                    if(flag){
//                        Iterator<TripData> itr = tripDataList.iterator();
//                        while (itr.hasNext()) {
//                            TripData element = itr.next();
//                            if(element.getId() == tripData.getId()) {
//                                tripDataList.remove(element);
//                                break;
//                            }
//                        }
//                        tripDataList.add(tripData);
//                        myTripAdapter.notifyDataSetChanged();
//                        updateListView();
//                    }
//                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                System.out.println(TAG + " onChildRemoved");
//                TripData tripData = dataSnapshot.getValue(TripData.class);
//
//                //Update Database and List Here
//                boolean flag = tripHelperDB.deleteContent(tripData.getId());
//
//                if(flag){
//                    hmap.remove(tripData.getTripName());
//                    Iterator<TripData> itr = tripDataList.iterator();
//                    while (itr.hasNext()) {
//                        TripData element = itr.next();
//                        if(element.getId() == tripData.getId()) {
//                            tripDataList.remove(element);
//                            break;
//                        }
//                    }
//                    updateListView();
//                    myTripAdapter.notifyDataSetChanged();
//                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
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
                    Intent loginScreenIntent = new Intent(AirlineActivity.this, MainActivity.class);
                    loginScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginScreenIntent);
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

        super.onStart();
    }

    //Left Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
