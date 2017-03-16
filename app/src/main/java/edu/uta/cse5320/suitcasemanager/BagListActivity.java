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
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class BagListActivity extends AppCompatActivity {

    FirebaseUser user;
    private FirebaseAuth mAuth;
    private Context ctx;
    private DatabaseReference myDbRef, imageURLRef;
    private StorageReference mStorageRef;
    private ListView listBagTrip;
    BagAdapter myAdapter;
    HashMap<String, String> hmap ;
    //List<String> bagArray ;
    String TAG = "Suitcase Manager::BagScreen";
    int index = 1, i = 1;

    private static int CAMERA_REQUEST_CODE = 200;
    private ProgressDialog progressDialog;
    String mCurrentPhotoPath;
    Uri photoURI;
    ImageView lastTouchedImageView;
    private String tripID ,bagID;
    private BagHelper bagHelperDB;
    ArrayList<BagData> bagDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bag_list);

        //Progress for operations
        progressDialog = new ProgressDialog(this);

        /* Getting Data from the data */
        Intent intent = getIntent();
        tripID = intent.getStringExtra(TripListActivity.EXTRA_MESSAGE);

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
        myDbRef = database.getReference("test").child(user.getUid()).child("Trips").child(tripID).child("Bags");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        myDbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println(TAG+ " onChildAdded");
                BagData bagData = dataSnapshot.getValue(BagData.class);
                //bagArray.add(bagData.getBagName());
                //Key - Value : TripName - f_id
                hmap.put(bagData.getBagName(), dataSnapshot.getKey());

                /* Inserting data in DB*/
                long id = bagHelperDB.addData(bagData.getBagName(), bagData.getItemQuantity(), bagData.getImageUrl1(),bagData.getImageUrl2(),bagData.getImageUrl3() );
                if(id == -1){
                    System.out.println("Not Inserted");
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
                    //lastTouchedImageView
                    System.out.println(TAG + "flag ="+ flag );
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
                System.out.println(TAG + "onChildRemoved");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });


        /* List Trip Data */
        listBagTrip = (ListView) findViewById(R.id.listBags);
        // initiate the listadapter
        myAdapter = new BagAdapter(this, R.layout.bag_list_layout, bagDataList);
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
                ImageView imageViewBagPicture1 = (ImageView) view.findViewById(R.id.imageViewBagPicture1);
                ImageView imageViewBagPicture2 = (ImageView) view.findViewById(R.id.imageViewBagPicture2);
                ImageView imageViewBagPicture3 = (ImageView) view.findViewById(R.id.imageViewBagPicture3);
                final ImageView[] imageViews = {imageViewBagPicture1, imageViewBagPicture2, imageViewBagPicture3};

                for( int j=0; j<imageViews.length; j++){
                    final int index = j;
                    imageViews[j].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ViewGroup row = (ViewGroup) v.getParent();
                            TextView textView = (TextView) row.findViewById(R.id.tripBagLabelName);
                            String bagID = hmap.get(textView.getText().toString());
                            System.out.println(TAG+" Icon of  - "+ textView.getText().toString() +"Delete : "+bagID);
                            imageURLRef = myDbRef.child(bagID);
                            //myDbRef.child(key).setValue(null);
                            dispatchTakePictureIntent(imageViews[index]);

                        }
                    });
                }
            }
        });
    }

    private void dispatchTakePictureIntent(ImageView imageViewBagPicture1) {
        lastTouchedImageView = imageViewBagPicture1;
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
                        hopperUpdates.put("imageUrl1", downloadUrl.toString());

                        imageURLRef.updateChildren(hopperUpdates);

                        progressDialog.dismiss();
                        Toast.makeText(ctx, "Image Uploade Finished...", Toast.LENGTH_SHORT).show();

                        /* Unable to access the file from the File System */
                        /* At this point of time, i have variable photoURI, i used for uploading the image on firebase. */
                    /*
                        // Trial-1
                        File imgFile = new  File("file://edu.uta.cse5320.suitcasemanager/files/Pictures/JPEG_20170315_190100_156454929.jpg");

                        //Trial-2
                        File imagePath = new File(ctx.getFilesDir(), "images");
                        File newFile = new File(imagePath, "JPEG_20170315_190100_156454929.jpg");
                        //newFile has Path - /data/user/0/edu.uta.cse5320.suitcasemanager/files/Picture/JPEG_20170315_190100_156454929.jpg

                        // I need to create a Bitmap Image later on ..
                        if(imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            lastTouchedImageView.setImageBitmap(myBitmap);
                        }
                    */
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
        ++index;
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
}
