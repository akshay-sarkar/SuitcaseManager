package edu.uta.cse5320.suitcasemanager;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

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
    private Button btnSave,btnViewQR,btnSaveQR,btnShareQR,btnBackQR;
    static ItemAdapter myAdapter;
    private String message;
    private static HashMap<String, String> hmap;
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
    private String content;

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


        edtItemName = (EditText) findViewById(R.id.editItemName1);
        edtItemQuantity = (EditText) findViewById(R.id.editItemQuantity1);
        btnSave = (Button) findViewById(R.id.btnItemSave1);
        btnViewQR = (Button) findViewById(R.id.buttonViewQR);
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
        ctx = this.getApplicationContext();
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
                    content = "Item Name, \tQuantity;\n";
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
                if(index == 1)
                    content = "Item Name, \tQuantity;\n";

                ++index;

                ItemData itemData = dataSnapshot.getValue(ItemData.class);

                if(index>1)
                    content = content + itemData.getItemName() + ", \t" + itemData.getItemQuantity() + ";\n";
                //Key - Value : TripName - f_id
                if(!hmap.containsKey(String.valueOf(itemData.getId()))){
                    hmap.put(String.valueOf(itemData.getId()), dataSnapshot.getKey());

                /* Inserting data in DB*/
                    int count = itemHelperDB.getListContent(itemData.getId());
                    if (count <= 0) {
                        System.out.println("Not Inserted!! Inserting Now..");
                        itemHelperDB.addDataCompleteSync(itemData.getId(), itemData.getItemName(), itemData.getItemQuantity());
                    }

                /* Adding in List */
                    itemDataList.add(itemData);
                    myAdapter.notifyDataSetChanged();
                    updateListView();
                }
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
                    if(!hmap.containsKey(String.valueOf(itemData.getId()))) {
                        hmap.remove(String.valueOf(itemData.getId()));
                        Iterator<ItemData> itr = itemDataList.iterator();
                        while (itr.hasNext()) {
                            ItemData element = itr.next();
                            if (element.getId() == itemData.getId()) {
                                itemDataList.remove(element);
                                break;
                            }
                        }
                        updateListView();
                    }
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

                ShowcaseConfig config = new ShowcaseConfig();
                config.setDelay(300);
                MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(ItemListActivity.this,"107");
                sequence.setConfig(config);
                sequence.addSequenceItem(edtItemName,"Enter Value for Item Name in the first text box.", "GOT IT");
                sequence.addSequenceItem(edtItemQuantity,"Enter value for Item Quantity in the second text box(Above save button)", "GOT IT");
                sequence.addSequenceItem(btnSave,"Press save to create new items", "GOT IT");
                sequence.start();

            }
        });
        /*v1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtItemName.setVisibility(View.GONE);
                edtItemQuantity.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
                return false;
            }
        });*/
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
                    ItemData itemDataNew = new ItemData(id, itemName, itemQuantity);
                    myDbRef.push().setValue(itemDataNew);
                    edtItemName.setVisibility(View.GONE);
                    edtItemQuantity.setVisibility(View.GONE);
                    btnSave.setVisibility(View.GONE);
                    Toast.makeText(ctx, "Item Added", Toast.LENGTH_SHORT).show();
                    updateListView();
                }
            }
        });

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(300);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(ItemListActivity.this,"106");
        sequence.setConfig(config);
        sequence.addSequenceItem(myFab,"Click this to Add New Items", "GOT IT");
        sequence.addSequenceItem(btnViewQR,"Click to generate the QR code of the items in the bag", "GOT IT");
        sequence.start();

        btnViewQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final Dialog dialog = new Dialog(ItemListActivity.this);
                dialog.setContentView(R.layout.qr_display);
                dialog.setTitle(ApplicationConstant.bag_name);
                final ImageView imageQR = (ImageView) dialog.findViewById(R.id.imageViewQR);
                btnSaveQR = (Button) dialog.findViewById(R.id.buttonSaveQR);
                btnShareQR = (Button) dialog.findViewById(R.id.buttonShareQR);
                btnBackQR = (Button) dialog.findViewById(R.id.buttonBack);

                QRCodeWriter writer = new QRCodeWriter();
                try {
                    if(content.equals("")){
                        content = "No Items";
                    }
                    BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }
                    imageQR.setImageBitmap(bmp);
                } catch (WriterException e) {
                    e.printStackTrace();
                }


                    btnBackQR.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                btnSaveQR.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageQR.buildDrawingCache();
                        Bitmap bm=imageQR.getDrawingCache();
                        OutputStream fOut = null;
                        //Uri outputFileUri;
                        try {
                            File root = new File(Environment.getExternalStorageDirectory()
                                    + File.separator + "Suitcase Manager" + File.separator);
                            root.mkdirs();
                            File sdImageMainDirectory = new File(root, ApplicationConstant.bag_name + "QRCode.jpg");
                            //outputFileUri = Uri.fromFile(sdImageMainDirectory);
                            fOut = new FileOutputStream(sdImageMainDirectory);
                            Toast.makeText(ItemListActivity.this, "Image Saved to SD Card", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(ItemListActivity.this, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                            fOut.flush();
                            fOut.close();
                        } catch (Exception e) {
                            Toast.makeText(ItemListActivity.this, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                btnShareQR.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageQR.buildDrawingCache();
                        Bitmap mBitmap=imageQR.getDrawingCache();
                        Bitmap icon = mBitmap;
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("image/jpeg");
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
                        try {
                            f.createNewFile();
                            FileOutputStream fo = new FileOutputStream(f);
                            fo.write(bytes.toByteArray());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
                        startActivity(Intent.createChooser(share, "Share Image"));
                    }
                });
                dialog.show();
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
                final AlertDialog alertDialog = new AlertDialog.Builder(ItemListActivity.this).create();

                if (state != NetworkInfo.State.CONNECTED) {
                    alertDialog.setTitle("Network Problem");
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage("No Network Available. Check Internet Connection");
                    alertDialog.setIcon(R.mipmap.ic_error);
                    alertDialog.show();
                }
                else{
                    if(alertDialog!=null )
                    alertDialog.dismiss();
                }

            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver((BroadcastReceiver) br, intentFilter);
    }
}
