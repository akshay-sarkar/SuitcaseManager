package edu.uta.cse5320.suitcasemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
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
import android.widget.ArrayAdapter;
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
import java.util.List;

import edu.uta.cse5320.dao.BagData;
import edu.uta.cse5320.dao.TripData;

public class BagListActivity extends AppCompatActivity {

    FirebaseUser user;
    private FirebaseAuth mAuth;
    private Context ctx;
    private DatabaseReference myDbRef;
    private StorageReference mStorageRef;
    private ListView listBagTrip;
    ArrayAdapter<String> myAdapter;
    HashMap<String, String> hmap ;
    List<String> bagArray ;
    String TAG = "Suitcase Manager::BagScreen";
    int index = 1;

    private static int CAMERA_REQUEST_CODE = 200;
    private ProgressDialog progressDialog;
    String mCurrentPhotoPath;
    Uri photoURI;
    ImageView lastTouchedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bag_list);

        progressDialog = new ProgressDialog(this);

        /* Getting Data from the data */
        Intent intent = getIntent();
        String tripID = intent.getStringExtra(TripListActivity.EXTRA_MESSAGE);
        //Toast.makeText(ctx, "Message::  "+message, Toast.LENGTH_SHORT).show();
        bagArray = new ArrayList<String>();
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
                bagArray.add(bagData.getBagName());
                //Key - Value : TripName - f_id
                hmap.put(bagData.getBagName(), dataSnapshot.getKey());
                updateListView();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println(TAG + "onChildChanged" );
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
        myAdapter = new ArrayAdapter<String>(this,
                R.layout.bag_list_layout, R.id.tripBagLabelName, bagArray);

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
                imageViewBagPicture1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewGroup row = (ViewGroup) v.getParent();
                        TextView textView = (TextView) row.findViewById(R.id.tripBagLabelName);
                        String key = hmap.get(textView.getText().toString());
                        System.out.println(TAG+" Icon of  - "+ textView.getText().toString() +"Delete :"+key);
                       //myDbRef.child(key).setValue(null);
                        dispatchTakePictureIntent(imageViewBagPicture1);

                    }
                });
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

//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            //mImageView.setImageBitmap(imageBitmap);
//
//            Uri uri = data.getData();
            FirebaseUser user = mAuth.getCurrentUser();
            StorageReference filePath = mStorageRef.child("Photos").child(user.getUid()).child(photoURI.getLastPathSegment());

            filePath.putFile(photoURI)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        progressDialog.dismiss();
                        Toast.makeText(ctx, "Uploading Finished...", Toast.LENGTH_SHORT).show();

                        /* Not Able to access the file from the File System */
                        /* At this point of time, i have variable photoURI */

                       // Trial-1
                        File imgFile = new  File("file://edu.uta.cse5320.suitcasemanager/files/Pictures/JPEG_20170315_190100_156454929.jpg");

                        //Trial-2
                        File imagePath = new File(ctx.getFilesDir(), "images");
                        File newFile = new File(imagePath, "JPEG_20170315_194413_1570892274.jpg");
                        //newFile has Path - "/data/user/0/edu.uta.cse5320.suitcasemanager/files/Pictures/JPEG_20170315_194413_1570892274.jpg"

                        /* I need to create a Bitmap Image later on ..*/
                        if(imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            lastTouchedImageView.setImageBitmap(myBitmap);
                        }

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

        String editBagName = "Bag Item-"+index;//editName.getText().toString();
        ++index;
        int editBagItems = 12;//editTextTripStartDate.getText().toString();

        BagData bagData = new BagData(editBagName, editBagItems);
        myDbRef.push().setValue(bagData);
        Toast.makeText(ctx, "Bag Created", Toast.LENGTH_SHORT).show();
    }
    
    private void updateListView(){
        myAdapter.notifyDataSetChanged();
        listBagTrip.invalidate();
        //Log.d(TAG, "Length: " + tripArray.size());
    }
}
