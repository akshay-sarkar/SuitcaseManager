package edu.uta.cse5320.dao;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

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


        final BagData bagData = bagDatas.get(position);

        if (bagData != null) {
            convertView = mInflater.inflate(mViewResourceId, null);
            TextView bagName = (TextView) convertView.findViewById(R.id.tripBagLabelName);
            ImageView imageView1 = (ImageView) convertView.findViewById(R.id.imageViewBagPicture1);
            ImageView imageView2 = (ImageView) convertView.findViewById(R.id.imageViewBagPicture2);
            ImageView imageView3 = (ImageView) convertView.findViewById(R.id.imageViewBagPicture3);

            if (bagName != null) {
                bagName.setText(bagData.getBagName());
                bagName.setTag(bagData.getBagName());
                bagName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //int position = listViewTrip.getPositionForView((View) v.getParent());
                        Toast.makeText(context, "Clicked on  - "+ v.getTag().toString(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, ItemListActivity.class);
                        hashMapBag = BagListActivity.getTripMap();
                        intent.putExtra(EXTRA_MESSAGE, hashMapBag.get(v.getTag().toString()));
                        context.startActivity(intent);
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
            }
            if(imageView3 != null && bagData.getImageUrl3()!=null && !bagData.getImageUrl3().isEmpty()){
                Picasso.with(context)
                        .load(bagData.getImageUrl3())
                        .fit().centerCrop()
                        .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                        .error(R.drawable.ic_add_a_photo_black_48dp)
                        .into(imageView3);
            }


        }

        return convertView;
    }
}
