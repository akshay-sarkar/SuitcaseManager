package edu.uta.cse5320.dao;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.uta.cse5320.suitcasemanager.BagListActivity;
import edu.uta.cse5320.suitcasemanager.ItemListActivity;
import edu.uta.cse5320.suitcasemanager.R;
import edu.uta.cse5320.suitcasemanager.TripListActivity;

/**
 * Created by Akshay on 3/16/2017.
 */

public class BagAdapter extends ArrayAdapter<BagData>{

    private LayoutInflater mInflater;
    private ArrayList<BagData> bagDatas;
    private int mViewResourceId;
    private Context context;
    private DatabaseReference myDbRef, imageURLRef;
    private StorageReference mStorageRef;
    public static final String EXTRA_MESSAGE = "edu.uta.cse5320.MESSAGE";
    public static HashMap<String, String> hashMapBag;
    private InputMethodManager imm;






    public BagAdapter(Context context, int textViewResourceId, ArrayList<BagData> bagData, DatabaseReference myDBRef, StorageReference myStorageRef) {
        super(context, textViewResourceId, bagData);
        this.context = context;
        this.bagDatas = bagData;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
        this.myDbRef = myDBRef;
        this.mStorageRef = myStorageRef;
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


            if (bagName != null) {
                bagName.setText(bagData.getBagName());
                bagName.setTag(bagData.getBagName());
                bagName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //int position = listViewTrip.getPositionForView((View) v.getParent());
                        //Toast.makeText(context, "Clicked on  - "+ v.getTag().toString(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, ItemListActivity.class);
                        hashMapBag = BagListActivity.getTripMap();
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

                btnSave.setTag(bagData.getBagName());
                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hashMapBag = BagListActivity.getTripMap();
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

                btnDelete.setTag(bagData.getBagName());
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "Deleted  Bag", Toast.LENGTH_SHORT).show();
                        hashMapBag = BagListActivity.getTripMap();
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
                        .error(R.drawable.ic_add_a_photo_black_48dp)
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
                        .error(R.drawable.ic_add_a_photo_black_48dp)
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
                        .error(R.drawable.ic_add_a_photo_black_48dp)
                        .into(imageView3);

                imageView3.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showImage(bagData.getImageUrl3());
                        return true;
                    }
                });
            }
        }
        return convertView;
    }

    private void showImage(String imageURI){
        ImageView image = new ImageView(context);
        Picasso.with(context)
                .load(imageURI)
                .resize(400,600)
                .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                .error(R.drawable.ic_add_a_photo_black_48dp)
                .into(image);
        //
        // image.setImageResource(R.drawable.YOUR_IMAGE_ID);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(context).
                        setMessage("Zoomed-In Image").
                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).
                        setView(image);
        builder.create().show();
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
