package edu.uta.cse5320.suitcasemanager;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import edu.uta.cse5320.dao.ItemAdapter;
import edu.uta.cse5320.dao.ItemData;
import edu.uta.cse5320.dao.ItemHelper;
import edu.uta.cse5320.util.ApplicationConstant;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ItemListActivity extends AppCompatActivity {

    FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context ctx;
    private DatabaseReference myDbRef, imageURLRef;
    private StorageReference mStorageRef;
    private ListView listItemTrip;
    ItemAdapter myAdapter;
    HashMap<String, String> hmap ;
    //List<String> itemArray ;
    String TAG = "Suitcase Manager::ItemScreen";
    int index = 1, i = 1;

    private static int CAMERA_REQUEST_CODE = 200;
    private ProgressDialog progressDialog;
    String mCurrentPhotoPath;
    Uri photoURI;
    String lastTouchedImageView;
    private String tripID ,itemID;
    private ItemHelper itemHelperDB;
    ArrayList<ItemData> itemDataList;

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

        // Left Menu / Navigational Layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_item_list);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.menu_open, R.string.menu_close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView nv = (NavigationView)findViewById(R.id.nv2);

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
                }else{
                    System.out.println("--- Reached Here -- "+ menuItem.getItemId());
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
        tripID = intent.getStringExtra(TripListActivity.EXTRA_MESSAGE);

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
        myDbRef = database.getReference("test").child(user.getUid()).child("Items").child(tripID).child("Items");
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

                //Key - Value : TripName - f_id
                hmap.put(itemData.getItemName(), dataSnapshot.getKey());

                /* Inserting data in DB*/
                int count = itemHelperDB.getListContent(itemData.getId());
                if(count <= 0){
                    System.out.println("Not Inserted!! Inserting Now..");
                    itemHelperDB.addDataCompleteSync(itemData.getId(), itemData.getItemName(), itemData.getItemQuantity(), itemData.getImageUrl1(),itemData.getImageUrl2(),itemData.getImageUrl3());
                }

                /* Adding in List */
                itemDataList.add(itemData);
                updateListView();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println(TAG + "onChildChanged" );
                ItemData itemData = dataSnapshot.getValue(ItemData.class);

                if(itemData != null){

                    /* Updating data in DB*/
                    boolean flag = itemHelperDB.updateDetails(itemData.getId(), itemData.getItemName(), itemData.getItemQuantity(), itemData.getImageUrl1(),itemData.getImageUrl2(),itemData.getImageUrl3());

                    /* updating in List */
                    if(flag){
                        Iterator<ItemData> itr = itemDataList.iterator();
                        while (itr.hasNext()) {
                            ItemData element = itr.next();
                            if(element.getId() == itemData.getId()) {
                                itemDataList.remove(element);
                                break;
                            }
                        }
                        itemDataList.add( itemData);
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

        /* Floating Button for moving to Add Items */
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.floatingButtonAddItem);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createItems();
            }
        });

        /* List Listner */
       /* listItemTrip.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ImageView imageViewItemPicture1 = (ImageView) view.findViewById(R.id.imageViewItemPicture1);
                final ImageView imageViewItemPicture2 = (ImageView) view.findViewById(R.id.imageViewItemPicture2);
                final ImageView imageViewItemPicture3 = (ImageView) view.findViewById(R.id.imageViewItemPicture3);

                imageViewItemPicture1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewGroup row = (ViewGroup) v.getParent();
                        TextView textView = (TextView) row.findViewById(R.id.tripItemLabelName);
                        String itemID = hmap.get(textView.getText().toString());
                        System.out.println(TAG+" Icon of  - "+ textView.getText().toString() +"Delete : "+itemID);
                        imageURLRef = myDbRef.child(itemID);
                        //myDbRef.child(key).setValue(null);
                        dispatchTakePictureIntent(imageViewItemPicture1);
                    }
                });
                imageViewItemPicture2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewGroup row = (ViewGroup) v.getParent();
                        TextView textView = (TextView) row.findViewById(R.id.tripItemLabelName);
                        String itemID = hmap.get(textView.getText().toString());
                        System.out.println(TAG+" Icon of  - "+ textView.getText().toString() +"Delete : "+itemID);
                        imageURLRef = myDbRef.child(itemID);
                        //myDbRef.child(key).setValue(null);
                        dispatchTakePictureIntent(imageViewItemPicture2);
                    }
                });
                imageViewItemPicture3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewGroup row = (ViewGroup) v.getParent();
                        TextView textView = (TextView) row.findViewById(R.id.tripItemLabelName);
                        String itemID = hmap.get(textView.getText().toString());
                        System.out.println(TAG+" Icon of  - "+ textView.getText().toString() +"Delete : "+itemID);
                        imageURLRef = myDbRef.child(itemID);
                        //myDbRef.child(key).setValue(null);
                        dispatchTakePictureIntent(imageViewItemPicture3);
                    }
                });

            }
        }); */
    }

    private void dispatchTakePictureIntent(ImageView imageViewItemPicture) {
        lastTouchedImageView =  getResources().getResourceEntryName(imageViewItemPicture.getId());
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
            StorageReference filePath = mStorageRef.child("Photos").child(user.getUid()).child(tripID).child(photoURI.getLastPathSegment());

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
                            Toast.makeText(ctx, "Image Uploade Finished...", Toast.LENGTH_SHORT).show();
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

    public void createItems(){

        String editItemName = "Item Item-"+index;
        int editItemItems = 12;//editTextTripStartDate.getText().toString();

        long id = itemHelperDB.addData(editItemName, editItemItems, "", "", "");
        if(id == -1){
            System.out.println("Not Inserted");
        }
        ItemData itemData = new ItemData(id, editItemName, editItemItems, "", "", "");
        myDbRef.push().setValue(itemData);
        Toast.makeText(ctx, "Item Added", Toast.LENGTH_SHORT).show();
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
}
