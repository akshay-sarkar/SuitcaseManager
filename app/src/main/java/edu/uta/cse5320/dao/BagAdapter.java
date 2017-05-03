package edu.uta.cse5320.dao;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.uta.cse5320.suitcasemanager.BagListActivity;
import edu.uta.cse5320.suitcasemanager.ItemListActivity;
import edu.uta.cse5320.suitcasemanager.R;
import edu.uta.cse5320.util.ApplicationConstant;

import static edu.uta.cse5320.util.ApplicationConstant.root_val;
import static edu.uta.cse5320.util.ApplicationConstant.trip_val;

/**
 * Created by Akshay on 3/16/2017.
 */

public class BagAdapter extends ArrayAdapter<BagData>{

    private LayoutInflater mInflater;
    private ArrayList<BagData> bagDatas;
    private int mViewResourceId;
    private static Context context;
    private static DatabaseReference myDbRef, imageURLRef;
    private static StorageReference mStorageRef;
    public static final String EXTRA_MESSAGE = "edu.uta.cse5320.MESSAGE";
    public static HashMap<String, String> hashMapBag;
    private InputMethodManager imm;
    private Activity activity;
    private static Uri photoURI;
    private static String lastTouchedImageView,mCurrentPhotoPath;
    private static File photoFile;
    private static int CAMERA_REQUEST_CODE = 200;
    private static ProgressDialog progressDialog;

    public BagAdapter(Activity activity, Context context, int textViewResourceId, ArrayList<BagData> bagData, DatabaseReference myDBRef, StorageReference myStorageRef) {
        super(context, textViewResourceId, bagData);
        this.context = context;
        this.bagDatas = bagData;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
        this.myDbRef = myDBRef;
        this.mStorageRef = myStorageRef;
        this.activity = activity;
    }

    public View getView(int position, View convertView, final ViewGroup parent) {

        imm = (InputMethodManager)context.getSystemService(context.INPUT_METHOD_SERVICE);

        final BagData bagData = bagDatas.get(position);

        if (bagData != null) {
            convertView = mInflater.inflate(mViewResourceId, null);
            final TextView bagName = (TextView) convertView.findViewById(R.id.tripBagLabelName);
            final ImageView imageView1 = (ImageView) convertView.findViewById(R.id.imageViewBagPicture1);
            final ImageView imageView2 = (ImageView) convertView.findViewById(R.id.imageViewBagPicture2);
            final ImageView imageView3 = (ImageView) convertView.findViewById(R.id.imageViewBagPicture3);
            final Button btnEdit = (Button) convertView.findViewById(R.id.btnBagEdit);
            final Button btnDelete = (Button) convertView.findViewById(R.id.btnBagDelete);
            final Button btnCancel = (Button) convertView.findViewById(R.id.btnBagCancel);
            final Button btnSave = (Button) convertView.findViewById(R.id.btnBagSave);
            final Button btnSaveCancel = (Button) convertView.findViewById(R.id.btnBagSaveCancel);
            final EditText editBagName = (EditText) convertView.findViewById(R.id.editBagName);
            hashMapBag = BagListActivity.getTripMap();
            progressDialog = new ProgressDialog(activity);




            if (bagName != null) {
                bagName.setText(bagData.getBagName());
                bagName.setTag(bagData.getId());
                bagName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //int position = listViewTrip.getPositionForView((View) v.getParent());
                        //Toast.makeText(context, "Clicked on  - "+ v.getTag().toString(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, ItemListActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, hashMapBag.get(v.getTag().toString()));
                        context.startActivity(intent);
                    }
                });

                btnSaveCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getVisibilityEdit(bagName,imageView1,imageView2,imageView3,btnEdit,btnDelete,btnCancel,editBagName,btnSave,btnSaveCancel,View.VISIBLE,View.GONE,true);

                    }
                });

                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getVisibilityEdit(bagName,imageView1,imageView2,imageView3,btnEdit,btnDelete,btnCancel,editBagName,btnSave,btnSaveCancel,View.GONE,View.VISIBLE,false);
                        editBagName.setText(bagName.getText().toString());
                    }
                });

                btnSave.setTag(bagData.getId());
                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //hashMapBag = BagListActivity.getTripMap();
                        String key = hashMapBag.get(v.getTag().toString());
                        if(!key.isEmpty()){
                            Map<String, Object> updateBagDetails = new HashMap<String, Object>();
                            updateBagDetails.put("bagName", editBagName.getText().toString());
                            BagListActivity.myDbRef.child(key).updateChildren(updateBagDetails);
                        }
                        Toast.makeText(context, "Updated Bag", Toast.LENGTH_SHORT).show();
                        getVisibilityEdit(bagName,imageView1,imageView2,imageView3,btnEdit,btnDelete,btnCancel,editBagName,btnSave,btnSaveCancel,View.VISIBLE,View.GONE,true);
                    }
                });

                btnDelete.setTag(bagData.getId());
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "Deleted  Bag", Toast.LENGTH_SHORT).show();
                        //hashMapBag = BagListActivity.getTripMap();
                        String key = hashMapBag.get(v.getTag().toString());
                        if (!key.isEmpty()) {
                            BagListActivity.myDbRef.child(key).setValue(null);
                            getVisibility(bagName, imageView1, imageView2, imageView3, btnEdit, btnDelete, btnCancel, View.GONE, View.VISIBLE);
                        }
                    }
                });


                bagName.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        getVisibility(bagName,imageView1,imageView2,imageView3,btnEdit,btnDelete,btnCancel,View.GONE,View.VISIBLE);
                        return true;
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getVisibility(bagName,imageView1,imageView2,imageView3,btnEdit,btnDelete,btnCancel,View.VISIBLE,View.GONE);
                    }
                });
            }
            if(imageView1 != null && bagData.getImageUrl1()!=null && !bagData.getImageUrl1().isEmpty()){


                Picasso.with(context)
                        .load(bagData.getImageUrl1())
                        .fit().centerCrop()
                        .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                        .error(R.drawable.common_google_signin_btn_icon_dark_focused)
                        .into(imageView1);

                        imageView1.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                            showImage(bagData.getImageUrl1());
                        return true;
                    }
                });
            }
//            if(imageView1 != null ){
//                final String imageUrl1 = bagData.getImageUrl1();
//                imageView1.setTag(new Integer(position));
//                imageView1.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // Do the stuff you want for the case when the row TextView is clicked
//                        // you may want to set as the tag for the TextView the position paremeter of the `getView` method and then retrieve it here
//                        Integer realPosition = (Integer) v.getTag();
//                        Picasso.with(context)
//                                .load(imageUrl1)
//                                .fit().centerCrop()
//                                .placeholder(R.drawable.ic_add_a_photo_black_48dp)
//                                .error(R.drawable.ic_add_a_photo_black_48dp)
//                                .into((ImageView) v);
//                    }
//                });
//                //{
//                    // Do the stuff you want for the case when the row TextView is clicked
//                    // you may want to set as the tag for the TextView the position paremeter of the `getView` method and then retrieve it here
//                //    Integer realPosition = (Integer) v.getTag();
//                    // using realPosition , now you know the row where this TextView was clicked
//                //}
//            }
            if(imageView2 != null && bagData.getImageUrl2()!=null &&!bagData.getImageUrl2().isEmpty()){
                Picasso.with(context)
                        .load(bagData.getImageUrl2())
                        .fit().centerCrop()
                        .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                        .error(R.drawable.common_google_signin_btn_icon_dark_focused)
                        .into(imageView2);

                imageView2.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showImage(bagData.getImageUrl2());
                        return true;
                    }
                });
            }
            if(imageView3 != null && bagData.getImageUrl3()!=null && !bagData.getImageUrl3().isEmpty()){
                Picasso.with(context)
                        .load(bagData.getImageUrl3())
                        .fit().centerCrop()
                        .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                        .error(R.drawable.common_google_signin_btn_icon_dark_focused)
                        .into(imageView3);

                imageView3.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showImage(bagData.getImageUrl3());
                        return true;
                    }
                });
            }

            //final ImageView imageViewBagPicture1 = (ImageView) findViewById(R.id.imageViewBagPicture1);
            //final ImageView imageViewBagPicture2 = (ImageView) findViewById(R.id.imageViewBagPicture2);
            //final ImageView imageViewBagPicture3 = (ImageView) findViewById(R.id.imageViewBagPicture3);

            imageView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewGroup row = (ViewGroup) v.getParent();
                    TextView textView = (TextView) row.findViewById(R.id.tripBagLabelName);
                    String bagID = hashMapBag.get(String.valueOf(bagData.getId()));
                    //System.out.println(TAG+" Icon of  - "+ textView.getText().toString() +"Delete : "+bagID);
                    imageURLRef = myDbRef.child(bagID);
                    //myDbRef.child(key).setValue(null);
                    dispatchTakePictureIntent(imageView1);
                }
            });
            imageView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewGroup row = (ViewGroup) v.getParent();
                    TextView textView = (TextView) row.findViewById(R.id.tripBagLabelName);
                    String bagID = hashMapBag.get(String.valueOf(bagData.getId()));
                    //System.out.println(TAG+" Icon of  - "+ textView.getText().toString() +"Delete : "+bagID);
                    imageURLRef = myDbRef.child(bagID);
                    //myDbRef.child(key).setValue(null);
                    dispatchTakePictureIntent(imageView2);
                }
            });
            imageView3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ViewGroup row = (ViewGroup) v.getParent();
                    //TextView textView = (TextView) row.findViewById(R.id.tripBagLabelName);
                    String bagID = hashMapBag.get(String.valueOf(bagData.getId()));
                    //System.out.println(TAG+" Icon of  - "+ textView.getText().toString() +"Delete : "+bagID);
                    imageURLRef = myDbRef.child(bagID);
                    //myDbRef.child(key).setValue(null);
                    dispatchTakePictureIntent(imageView3);
                }
            });
        }
        return convertView;
    }

    private void showImage(String imageURI){
        String localURI = imageURI;
        localURI = localURI.substring(localURI.lastIndexOf("JPEG_"), localURI.indexOf(".jpg?"));
        String[] locate = localURI.split("%3D");
        List<Address> addresses = null;


        try {
            //JPEG_20170503_053731%3D32.7323633%3D-97.1138993%3D-1635670069.jpg?alt=media&token=671b96ec-1ba8-4354-9af8-97b9cbca5fde
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            addresses = geocoder.getFromLocation(Double.parseDouble(locate[1]), Double.parseDouble(locate[2]), 1);
        }
        catch (Exception e){
            addresses = null;
        }

        ImageView image = new ImageView(context);
        Picasso.with(context)
                .load(imageURI)
                .resize(400,600)
                .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                .error(R.drawable.ic_add_a_photo_black_48dp)
                .into(image);
        //
        // image.setImageResource(R.drawable.YOUR_IMAGE_ID);
        String add = "No Address Found";
        if(addresses != null){
            add = addresses.get(0).getAddressLine(0)+"\n"+
                    addresses.get(0).getAddressLine(1)+"\n"+
                    addresses.get(0).getAddressLine(2);

//                    currentContact.getStreetAddress() + ", " +
//                    currentContact.getCity() + ", " +
//                    currentContact.getState() + " " +
//                    currentContact.getZipCode();
        }
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context).
                        setView(image).
                        setMessage("Address : \n"+add+"\n").
                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
        builder.create().show();
    }

    private void dispatchTakePictureIntent(ImageView imageViewBagPicture) {
        lastTouchedImageView =  context.getResources().getResourceEntryName(imageViewBagPicture.getId());
        lastTouchedImageView = "imageUrl" + lastTouchedImageView.charAt(lastTouchedImageView.length()-1);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(intent1, CAMERA_REQUEST_CODE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(context, "edu.uta.cse5320.suitcasemanager.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    public static void uploadPhoto(){
        int targetW = 400;
        int targetH = 500;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFile.getAbsolutePath(),bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW,photoH/targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(),bmOptions);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] byteFormat = stream.toByteArray();
        //String encodedImage = Base64.encodeToString(byteFormat, Base64.DEFAULT);

        progressDialog.setMessage("Uploading Image..");
        progressDialog.show();
        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 8;
        //Bitmap bitmap = BitmapFactory.decodeFile(photoURI.getPath());
        //Uri imageURI = data.getData();
        //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageURI);

        //Bitmap bmp = ImagePicker.getImageFromResult(this,resultCode,data);
        //FirebaseUser user = mAuth.getCurrentUser();
        StorageReference filePath = mStorageRef.child("Photos").child(root_val).child(trip_val).child(photoURI.getLastPathSegment());

        filePath.putBytes(byteFormat)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        // Get a URL to the uploaded content
                        @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        //Updating the URL in Database
                        Map<String, Object> hopperUpdates = new HashMap<String, Object>();
                        hopperUpdates.put(lastTouchedImageView, downloadUrl.toString());
                        imageURLRef.updateChildren(hopperUpdates);

                        Toast.makeText(context, "Image Upload Finished...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Upload Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //OutputStream outputStream = null;
        String imageFileName = "JPEG_" + timeStamp + "="+ ApplicationConstant.latitude+"="+ApplicationConstant.longitude+"=";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        /*Bitmap tempImage = BitmapFactory.decodeFile(image.getName());
        outputStream = new FileOutputStream(image);
        tempImage.compress(Bitmap.CompressFormat.JPEG,20,outputStream);
        outputStream.flush();
        outputStream.close();*/
        //Bitmap lqImage = Bitmap.createScaledBitmap(tempImage,512,512,false);

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void getVisibility(TextView bN, ImageView i1, ImageView i2, ImageView i3, Button bE,Button bD,Button bC,int s1,int s2){
        bN.setVisibility(s1);
        i1.setVisibility(s1);
        i2.setVisibility(s1);
        i3.setVisibility(s1);
        bE.setVisibility(s2);
        bD.setVisibility(s2);
        bC.setVisibility(s2);
    }

    private void getVisibilityEdit(TextView bN, ImageView i1, ImageView i2, ImageView i3, Button bE, Button bD, Button bC, EditText eN, Button sB, Button sBc, int s1, int s2, boolean ch){
        if(ch){
            bN.setVisibility(s1);
            i1.setVisibility(s1);
            i2.setVisibility(s1);
            i3.setVisibility(s1);
        }else{
            bE.setVisibility(s1);
            bD.setVisibility(s1);
            bC.setVisibility(s1);

        }
        eN.setVisibility(s2);
        sB.setVisibility(s2);
        sBc.setVisibility(s2);
    }
}
