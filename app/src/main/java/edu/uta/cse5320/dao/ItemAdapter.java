package edu.uta.cse5320.dao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import edu.uta.cse5320.suitcasemanager.R;



public class ItemAdapter extends ArrayAdapter<ItemData>{

    private LayoutInflater mInflater;
    private ArrayList<ItemData> itemDatas;
    private int mViewResourceId;
    private Context context;
    private DatabaseReference myDbRef, imageURLRef;
    private StorageReference mStorageRef;

    public ItemAdapter(Context context, int textViewResourceId, ArrayList<ItemData> itemData, DatabaseReference myDBRef, StorageReference myStorageRef) {
        super(context, textViewResourceId, itemData);
        this.context = context;
        this.itemDatas = itemData;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
        this.myDbRef = myDBRef;
        this.mStorageRef = myStorageRef;
    }

   /* public View getView(int position, View convertView, final ViewGroup parent) {


        ItemData itemData = itemDatas.get(position);

        if (itemData != null) {
            convertView = mInflater.inflate(mViewResourceId, null);
            TextView itemName = (TextView) convertView.findViewById(R.id.tripItemLabelName);
            ImageView imageView1 = (ImageView) convertView.findViewById(R.id.imageViewItemPicture1);
            ImageView imageView2 = (ImageView) convertView.findViewById(R.id.imageViewItemPicture2);
            ImageView imageView3 = (ImageView) convertView.findViewById(R.id.imageViewItemPicture3);

            if (itemName != null) {
                itemName.setText(itemData.getItemName());
            }
            if(imageView1 != null && itemData.getImageUrl1()!=null && !itemData.getImageUrl1().isEmpty()){
                Picasso.with(context)
                        .load(itemData.getImageUrl1())
                        .fit().centerCrop()
                        .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                        .error(R.drawable.ic_add_a_photo_black_48dp)
                        .into(imageView1);
            }

            if(imageView2 != null && itemData.getImageUrl2()!=null &&!itemData.getImageUrl2().isEmpty()){
                Picasso.with(context)
                        .load(itemData.getImageUrl2())
                        .fit().centerCrop()
                        .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                        .error(R.drawable.ic_add_a_photo_black_48dp)
                        .into(imageView2);
            }
            if(imageView3 != null && itemData.getImageUrl3()!=null && !itemData.getImageUrl3().isEmpty()){
                Picasso.with(context)
                        .load(itemData.getImageUrl3())
                        .fit().centerCrop()
                        .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                        .error(R.drawable.ic_add_a_photo_black_48dp)
                        .into(imageView3);
            }

        }*/

       // return convertView;
   // }
}
