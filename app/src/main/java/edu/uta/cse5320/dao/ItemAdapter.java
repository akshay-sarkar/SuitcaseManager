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
import java.util.HashMap;

import edu.uta.cse5320.suitcasemanager.BagListActivity;
import edu.uta.cse5320.suitcasemanager.ItemListActivity;
import edu.uta.cse5320.suitcasemanager.R;
import edu.uta.cse5320.util.ApplicationConstant;


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

    public View getView(int position, View convertView, final ViewGroup parent) {


        ItemData itemData = itemDatas.get(position);

        if (itemData != null) {
            convertView = mInflater.inflate(mViewResourceId, null);
            TextView itemName = (TextView) convertView.findViewById(R.id.tripItemLabelName);
            TextView itemQuantity = (TextView) convertView.findViewById(R.id.tripItemLabelQuantity);

            if (itemName != null) {
                itemName.setText(itemData.getItemName());
                ApplicationConstant.hashMapItem = ItemListActivity.getItemMap();
            }

            if(itemQuantity != null){
                itemQuantity.setText(String.valueOf(itemData.getItemQuantity()));
            }

        }

       return convertView;
   }
}
