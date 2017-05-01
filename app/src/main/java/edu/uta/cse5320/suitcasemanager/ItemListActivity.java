package edu.uta.cse5320.suitcasemanager;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.uta.cse5320.dao.ItemAdapter;
import edu.uta.cse5320.dao.ItemData;
import edu.uta.cse5320.dao.ItemHelper;
import edu.uta.cse5320.dao.TripData;
import edu.uta.cse5320.util.ApplicationConstant;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static edu.uta.cse5320.dao.BagAdapter.hashMapBag;
import static edu.uta.cse5320.suitcasemanager.TripListActivity.EXTRA_MESSAGE;
import static edu.uta.cse5320.util.ApplicationConstant.bag_item_prop;
import static edu.uta.cse5320.util.ApplicationConstant.bag_val;
import static edu.uta.cse5320.util.ApplicationConstant.root_prop;
import static edu.uta.cse5320.util.ApplicationConstant.root_trip_prop;
import static edu.uta.cse5320.util.ApplicationConstant.root_val;
import static edu.uta.cse5320.util.ApplicationConstant.trip_bag_prop;
import static edu.uta.cse5320.util.ApplicationConstant.trip_val;

public class ItemListActivity extends AppCompatActivity {

    FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context ctx;
    public static DatabaseReference myDbRef, imageURLRef;
    private StorageReference mStorageRef;
    private ListView listItemTrip;
    private boolean isEditMode = false;
    private EditText edtItemName, edtItemQuantity;
    private View v1;
    private Button btnSave;
    static ItemAdapter myAdapter;
    private String message;
    private static HashMap<String, String> hmap;
    private static ItemData itemDataVal;
    //List<String> itemArray ;
    String TAG = "Suitcase Manager::ItemScreen";
    int index = 1, i = 1;

    private static int CAMERA_REQUEST_CODE = 200;
    private ProgressDialog progressDialog;
    String mCurrentPhotoPath;
    Uri photoURI;
    String lastTouchedImageView;
    private ItemHelper itemHelperDB;
    static ArrayList<ItemData> itemDataList;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private GoogleApiClient mGoogleApiClient;

    // for font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        //shared prefernece for tip flag
        final SharedPreferences pref = getApplicationContext().getSharedPreferences(ApplicationConstant.MySharedPrefName, MODE_PRIVATE);
        final SharedPreferences.Editor editor = getSharedPreferences(ApplicationConstant.MySharedPrefName, MODE_PRIVATE).edit();


        v1 = new View(this);
        edtItemName = (EditText) findViewById(R.id.editItemName1);
        edtItemQuantity = (EditText) findViewById(R.id.editItemQuantity1);
        btnSave = (Button) findViewById(R.id.btnItemSave1);
        // Left Menu / Navigational Layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_item_list);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.menu_open, R.string.menu_close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final NavigationView nv = (NavigationView)findViewById(R.id.nv2);

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
                    System.out.println("--- Reached Here -- "+ menuItem.getItemId());
                }
                return true;
            }
        });

        checkInternet();
        //Progress for operations
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retrieving Your Data..");
        progressDialog.show();

        /* Getting Data from the data */
        Intent intent = getIntent();
        bag_val = intent.getStringExtra(EXTRA_MESSAGE);

        itemHelperDB = new ItemHelper(this);
        itemDataList = new ArrayList<>();
        Cursor data = itemHelperDB.getListContents();
        int numRows = data.getCount();

        //itemArray = new ArrayList<>();
        hmap = new HashMap<String, String>();

        mAuth = FirebaseAuth.getInstance();
        ctx = getApplicationContext();
        user = mAuth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference(root_prop).child(root_val).child(root_trip_prop).child(trip_val).child(trip_bag_prop).child(bag_val).child(bag_item_prop);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //to check if its item details are empty and removing the progress dialog
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
                ++index;
                ItemData itemData = dataSnapshot.getValue(ItemData.class);
                itemDataVal = itemData;

                //Key - Value : TripName - f_id
                hmap.put(itemData.getItemName(), dataSnapshot.getKey());

                /* Inserting data in DB*/
                int count = itemHelperDB.getListContent(itemData.getId());
                if(count <= 0){
                    System.out.println("Not Inserted!! Inserting Now..");
                    itemHelperDB.addDataCompleteSync(itemData.getId(), itemData.getItemName(), itemData.getItemQuantity());
                }

                /* Adding in List */
                itemDataList.add(itemData);
                updateListView();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println(TAG + "onChildChanged" );
                ItemData itemDat = dataSnapshot.getValue(ItemData.class);

                if(itemDat != null){
                    /* Updating data in DB*/
                    boolean flag = itemHelperDB.updateDetails(itemDat.getId(), itemDat.getItemName(), itemDat.getItemQuantity());

                    /* updating in List */
                    if(flag){
                        Iterator<ItemData> itr = itemDataList.iterator();
                        while (itr.hasNext()) {
                            ItemData element = itr.next();
                            if(element.getId() == itemDat.getId()) {
                                itemDataList.remove(element);
                                break;
                            }
                        }
                        itemDataList.add(itemDat);
                        myAdapter.notifyDataSetChanged();
                        updateListView();
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                System.out.println(TAG + " onChildRemoved");
                ItemData itemData = dataSnapshot.getValue(ItemData.class);

                //Update Database and List Here
                boolean flag = itemHelperDB.deleteContent(itemData.getId());

                if(flag){
                    hmap.remove(itemData.getItemName());
                    Iterator<ItemData> itr = itemDataList.iterator();
                    while (itr.hasNext()) {
                        ItemData element = itr.next();
                        if(element.getId() == itemData.getId()) {
                            itemDataList.remove(element);
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
        listItemTrip = (ListView) findViewById(R.id.listItems);
        // initiate the listadapter
        myAdapter = new ItemAdapter(this, R.layout.item_list_layout, itemDataList, myDbRef , mStorageRef);
        myAdapter.setNotifyOnChange(true);
        listItemTrip.setAdapter(myAdapter);


        listItemTrip.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                edtItemName.setVisibility(View.GONE);
                edtItemQuantity.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
            }
        });
        /* Floating Button for moving to Add Items */
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.floatingButtonAddItem);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                edtItemName.setVisibility(View.VISIBLE);
                edtItemQuantity.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.VISIBLE);
            }
        });
        v1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtItemName.setVisibility(View.GONE);
                edtItemQuantity.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
                return false;
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtItemName.getText().toString().equals("") || edtItemQuantity.getText().toString().equals("")) {
                    Toast.makeText(ctx, "Item Name and Quantity Cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    String itemName = String.valueOf(edtItemName.getText());
                    int itemQuantity = Integer.parseInt(String.valueOf(edtItemQuantity.getText()));
                    long id = itemHelperDB.addData(itemName, itemQuantity);
                    if (id == -1) {
                        System.out.println("Not Inserted");
                    }
                    ItemData itemData = new ItemData(id, itemName, itemQuantity);
                    myDbRef.push().setValue(itemData);
                    edtItemName.setVisibility(View.GONE);
                    edtItemQuantity.setVisibility(View.GONE);
                    btnSave.setVisibility(View.GONE);
                    Toast.makeText(ctx, "Item Added", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void updateListView(){
        myAdapter.notifyDataSetChanged();
        listItemTrip.invalidate();
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
                    Intent loginScreenIntent = new Intent(ItemListActivity.this, MainActivity.class);
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
    public static HashMap<String, String> getItemMap(){
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

                if (state != NetworkInfo.State.CONNECTED) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(ItemListActivity.this).create();
                    alertDialog.setTitle("Network Problem");
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage("No Network Available. Check Internet Connection");
                    alertDialog.setIcon(R.mipmap.ic_error);
                    alertDialog.show();
                }

            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver((BroadcastReceiver) br, intentFilter);
    }
}
