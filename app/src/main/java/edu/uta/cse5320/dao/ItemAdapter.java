package edu.uta.cse5320.dao;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.uta.cse5320.suitcasemanager.BagListActivity;
import edu.uta.cse5320.suitcasemanager.ItemListActivity;
import edu.uta.cse5320.suitcasemanager.R;
import edu.uta.cse5320.suitcasemanager.TripListActivity;
import edu.uta.cse5320.util.ApplicationConstant;

import static edu.uta.cse5320.util.ApplicationConstant.bag_item_prop;
import static edu.uta.cse5320.util.ApplicationConstant.bag_val;
import static edu.uta.cse5320.util.ApplicationConstant.root_prop;
import static edu.uta.cse5320.util.ApplicationConstant.root_trip_prop;
import static edu.uta.cse5320.util.ApplicationConstant.root_val;
import static edu.uta.cse5320.util.ApplicationConstant.trip_bag_prop;
import static edu.uta.cse5320.util.ApplicationConstant.trip_val;


public class ItemAdapter extends ArrayAdapter<ItemData>{

    private LayoutInflater mInflater;
    private ArrayList<ItemData> itemDatas;
    private int mViewResourceId;
    private Context context;
    private DatabaseReference myDbRef, imageURLRef;
    private StorageReference mStorageRef;
    public static HashMap<String, String> hashMapItem;

    private TextView itemName,itemQuantity;
    private Button btnDelete,btnSave;
    private ImageView itemEdit;
    private EditText editItemName,editItemQuantity;



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
            final TextView itemName = (TextView) convertView.findViewById(R.id.tripItemLabelName);
            final TextView itemQuantity = (TextView) convertView.findViewById(R.id.tripItemLabelQuantity);
            final EditText editItemName = (EditText) convertView.findViewById(R.id.editItemName);
            final EditText editItemQuantity = (EditText) convertView.findViewById(R.id.editItemQuantity);
            final Button btnDelete = (Button) convertView.findViewById(R.id.btnItemDelete);
            final Button btnSave = (Button) convertView.findViewById(R.id.btnItemSave);
            final ImageView itemEdit = (ImageView) convertView.findViewById(R.id.itemEdit);

            if (itemName != null && itemQuantity!=null) {
                itemName.setText(itemData.getItemName());
                hashMapItem = ItemListActivity.getItemMap();
                itemQuantity.setText(String.valueOf(itemData.getItemQuantity()));
                itemName.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        getVisibilityDelete(itemName,itemQuantity,itemEdit,btnDelete,View.GONE,View.VISIBLE);
                        return false;
                    }
                });

                convertView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        getVisibility(editItemName,editItemQuantity,itemName,itemQuantity,btnSave,itemEdit,View.GONE,View.VISIBLE);
                        getVisibilityDelete(itemName,itemQuantity,itemEdit,btnDelete,View.VISIBLE,View.GONE);
                        return false;
                    }
                });

                itemEdit.setTag(itemData.getItemName());
                itemEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //getVisibilityDelete(itemName,itemQuantity,itemEdit,btnDelete,View.VISIBLE,View.GONE);
                        getVisibility(editItemName,editItemQuantity,itemName,itemQuantity,btnSave,itemEdit,View.VISIBLE,View.GONE);
                        editItemName.setText(itemName.getText().toString());
                        editItemQuantity.setText(itemQuantity.getText().toString());
                    }
                });

                btnDelete.setTag(itemData.getItemName());
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "Deleted  - "+ v.getTag().toString(), Toast.LENGTH_SHORT).show();
                        hashMapItem = ItemListActivity.getItemMap();
                        String key = hashMapItem.get(v.getTag().toString());
                        if(!key.isEmpty()){
                            ItemListActivity.myDbRef.child(key).setValue(null);
                            btnDelete.setVisibility(View.GONE);
                        }
                    }
                });

                /*itemName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getVisibility(editItemName,editItemQuantity,itemName,itemQuantity,btnSave,itemEdit,View.GONE,View.VISIBLE);
                        getVisibilityDelete(itemName,itemQuantity,itemEdit,btnDelete,View.VISIBLE,View.GONE);
                    }
                });*/

                btnSave.setTag(itemData.getItemName());
                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hashMapItem = ItemListActivity.getItemMap();
                        String key = hashMapItem.get(v.getTag().toString());
                        if(!key.isEmpty()){
                            Map<String, Object> updateTripDetails = new HashMap<String, Object>();
                            updateTripDetails.put("itemName", editItemName.getText().toString());
                            updateTripDetails.put("itemQuantity", Integer.parseInt(editItemQuantity.getText().toString()));
                            ItemListActivity.myDbRef.child(key).updateChildren(updateTripDetails);
                        }
                        Toast.makeText(context, "Item Updated", Toast.LENGTH_SHORT).show();
                        getVisibility(editItemName,editItemQuantity,itemName,itemQuantity,btnSave,itemEdit,View.GONE,View.VISIBLE);
                    }
                });
            }

        }

       return convertView;
   }

   public void getVisibility(EditText eN, EditText eQ, TextView iN, TextView iQ, Button bS, ImageView iE, int s1, int s2){
       eN.setVisibility(s1);
       eQ.setVisibility(s1);
       bS.setVisibility(s1);
       iN.setVisibility(s2);
       iQ.setVisibility(s2);
       iE.setVisibility(s2);
   }

   public void getVisibilityDelete(TextView iN, TextView iQ, ImageView iE, Button bD, int s1, int s2){
       iN.setVisibility(s1);
       iQ.setVisibility(s1);
       iE.setVisibility(s1);
       bD.setVisibility(s2);

   }

}
