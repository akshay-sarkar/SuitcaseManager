package edu.uta.cse5320.suitcasemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import edu.uta.cse5320.dao.BagAdapter;
import edu.uta.cse5320.dao.BagData;
import edu.uta.cse5320.dao.BagHelper;
import edu.uta.cse5320.util.ApplicationConstant;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static edu.uta.cse5320.util.ApplicationConstant.root_prop;
import static edu.uta.cse5320.util.ApplicationConstant.root_trip_prop;
import static edu.uta.cse5320.util.ApplicationConstant.trip_bag_prop;
import static edu.uta.cse5320.util.ApplicationConstant.trip_val;

public class BagListActivity extends AppCompatActivity {

    FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context ctx;
    private DatabaseReference myDbRef, imageURLRef;
    private StorageReference mStorageRef;
    private ListView listBagTrip;
    BagAdapter myAdapter;
    public static HashMap<String, String> hmap ;
    //List<String> bagArray ;
    String TAG = "Suitcase Manager::BagScreen";
    int index = 1, i = 1;

    private static int CAMERA_REQUEST_CODE = 200;
    private ProgressDialog progressDialog;
    String mCurrentPhotoPath;
    Uri photoURI;
    String lastTouchedImageView;
    private BagHelper bagHelperDB;
    ArrayList<BagData> bagDataList;

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
                ++index;
                BagData bagData = dataSnapshot.getValue(BagData.class);

                //Key - Value : TripName - f_id
                hmap.put(bagData.getBagName(), dataSnapshot.getKey());

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
                    hmap.remove(bagData.getBagName());
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
        myAdapter = new BagAdapter(this, R.layout.bag_list_layout, bagDataList, myDbRef , mStorageRef);
        myAdapter.setNotifyOnChange(true);
        listBagTrip.setAdapter(myAdapter);

        /* Floating Button for moving to Add Bags */
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.floatingButtonAddBag);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createBags();
            }
        });

        /* List Listner */
        listBagTrip.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ImageView imageViewBagPicture1 = (ImageView) view.findViewById(R.id.imageViewBagPicture1);
                final ImageView imageViewBagPicture2 = (ImageView) view.findViewById(R.id.imageViewBagPicture2);
                final ImageView imageViewBagPicture3 = (ImageView) view.findViewById(R.id.imageViewBagPicture3);

                imageViewBagPicture1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewGroup row = (ViewGroup) v.getParent();
                        TextView textView = (TextView) row.findViewById(R.id.tripBagLabelName);
                        String bagID = hmap.get(textView.getText().toString());
                        System.out.println(TAG+" Icon of  - "+ textView.getText().toString() +"Delete : "+bagID);
                        imageURLRef = myDbRef.child(bagID);
                        //myDbRef.child(key).setValue(null);
                        dispatchTakePictureIntent(imageViewBagPicture1);
                    }
                });
                imageViewBagPicture2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewGroup row = (ViewGroup) v.getParent();
                        TextView textView = (TextView) row.findViewById(R.id.tripBagLabelName);
                        String bagID = hmap.get(textView.getText().toString());
                        System.out.println(TAG+" Icon of  - "+ textView.getText().toString() +"Delete : "+bagID);
                        imageURLRef = myDbRef.child(bagID);
                        //myDbRef.child(key).setValue(null);
                        dispatchTakePictureIntent(imageViewBagPicture2);
                    }
                });
                imageViewBagPicture3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewGroup row = (ViewGroup) v.getParent();
                        TextView textView = (TextView) row.findViewById(R.id.tripBagLabelName);
                        String bagID = hmap.get(textView.getText().toString());
                        System.out.println(TAG+" Icon of  - "+ textView.getText().toString() +"Delete : "+bagID);
                        imageURLRef = myDbRef.child(bagID);
                        //myDbRef.child(key).setValue(null);
                        dispatchTakePictureIntent(imageViewBagPicture3);
                    }
                });

            }
        });
    }

    private void dispatchTakePictureIntent(ImageView imageViewBagPicture) {
        lastTouchedImageView =  getResources().getResourceEntryName(imageViewBagPicture.getId());
        lastTouchedImageView = "imageUrl" + lastTouchedImageView.charAt(lastTouchedImageView.length()-1);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(intent1, CAMERA_REQUEST_CODE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this, "edu.uta.cse5320.suitcasemanager.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            progressDialog.setMessage("Uploading Image..");
            progressDialog.show();

            FirebaseUser user = mAuth.getCurrentUser();
            StorageReference filePath = mStorageRef.child("Photos").child(user.getUid()).child(trip_val).child(photoURI.getLastPathSegment());

            filePath.putFile(photoURI)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        //Updating the URL in Database
                        Map<String, Object> hopperUpdates = new HashMap<String, Object>();
                        hopperUpdates.put(lastTouchedImageView, downloadUrl.toString());

                        imageURLRef.updateChildren(hopperUpdates);

                        progressDialog.dismiss();
                        Toast.makeText(ctx, "Image Upload Finished...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(ctx, "Upload Failed!", Toast.LENGTH_SHORT).show();
                    }
                    });
        }

    }

    public void createBags(){

        String editBagName = "Bag Item-"+index;
        int editBagItems = 12;//editTextTripStartDate.getText().toString();

        long id = bagHelperDB.addData(editBagName, editBagItems, "", "", "");
        if(id == -1){
            System.out.println("Not Inserted");
        }
        BagData bagData = new BagData(id, editBagName, editBagItems, "", "", "");
        myDbRef.push().setValue(bagData);
        Toast.makeText(ctx, "Bag Added", Toast.LENGTH_SHORT).show();
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

}
