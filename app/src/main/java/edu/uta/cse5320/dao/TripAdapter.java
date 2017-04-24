package edu.uta.cse5320.dao;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import edu.uta.cse5320.suitcasemanager.AddTripActivity;
import edu.uta.cse5320.suitcasemanager.AirlineActivity;
import edu.uta.cse5320.suitcasemanager.BagListActivity;
import edu.uta.cse5320.suitcasemanager.R;
import edu.uta.cse5320.suitcasemanager.TripListActivity;

/**
 * Created by Akshay on 3/16/2017.
 */

public class TripAdapter extends ArrayAdapter<TripData> {

    private LayoutInflater mInflater;
    private ArrayList<TripData> tripDatas;
    private int mViewResourceId;
    private Context context;
    public static final String EXTRA_MESSAGE = "edu.uta.cse5320.MESSAGE";
    public static HashMap<String, String> hashMapTrip;

    public TripAdapter(Context context, int textViewResourceId,
                       ArrayList<TripData> tripData) {
        super(context, textViewResourceId, tripData);
        this.context = context;
        this.tripDatas = tripData;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(mViewResourceId, null);

        final TripData tripData = tripDatas.get(position);

//        if(convertView == null){
//            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
//        }
        if (tripData != null) {
            TextView tripName = (TextView) convertView.findViewById(R.id.tripListLabelName);
            TextView tripAirline = (TextView) convertView.findViewById(R.id.textViewAirline);
            TextView tripStartDate = (TextView) convertView.findViewById(R.id.textViewStartDateVal);
            TextView tripEndDate = (TextView) convertView.findViewById(R.id.textViewEndDateVal);

            ImageView imageViewEdit = (ImageView) convertView.findViewById(R.id.imageViewEdit);
            ImageView imageViewDelete = (ImageView) convertView.findViewById(R.id.imageViewDelete);
            ImageView imageViewPublish = (ImageView) convertView.findViewById(R.id.imageViewPublish);

            if (tripName != null) {
                tripName.setText(tripData.getTripName());
            }
            if(tripAirline != null && !tripData.getTripAirlineName().isEmpty()){
                tripAirline.setText(tripData.getTripAirlineName());
            }
            if (tripStartDate != null) {
                if(tripData.getTripStartDate().isEmpty()){
                    tripStartDate.setText("-");
                }else{
                    tripStartDate.setText(tripData.getTripStartDate());
                }
            }
            if (tripEndDate != null) {
                if(tripData.getTripEndDate().isEmpty()){
                    tripEndDate.setText("-");
                }else{
                    tripEndDate.setText(tripData.getTripEndDate());
                }
            }

            // Setting Listener for TripAdapter
            tripName.setTag(tripData.getTripName());
            tripName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //int position = listViewTrip.getPositionForView((View) v.getParent());
                    Toast.makeText(context, "Clicked on  - "+ v.getTag().toString(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, BagListActivity.class);
                    hashMapTrip = TripListActivity.getTripMap();
                    intent.putExtra(EXTRA_MESSAGE, hashMapTrip.get(v.getTag().toString()));
                    context.startActivity(intent);
                }
            });

            imageViewPublish.setTag(tripData.getTripName());
            imageViewPublish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hashMapTrip = TripListActivity.getTripMap();
                    Toast.makeText(context, "Airline Information For the Trip : "+ v.getTag().toString(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, AirlineActivity.class);
                    intent.putExtra(EXTRA_MESSAGE, hashMapTrip.get(v.getTag().toString()));
                    intent.putExtra("airlineName", tripData.getTripAirlineName());
                    context.startActivity(intent);
                }
            });


            imageViewEdit.setTag(tripData.getTripName());
            imageViewEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Clicked on  - "+ v.getTag().toString(), Toast.LENGTH_SHORT).show();
                    hashMapTrip = TripListActivity.getTripMap();
                    Intent intent = new Intent(context, AddTripActivity.class);
                    intent.putExtra(EXTRA_MESSAGE, hashMapTrip.get(v.getTag().toString()));
                    context.startActivity(intent);
                }
            });

            imageViewDelete.setTag(tripData.getTripName());
            imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Deleted  - "+ v.getTag().toString(), Toast.LENGTH_SHORT).show();
                    hashMapTrip = TripListActivity.getTripMap();
                    String key = hashMapTrip.get(v.getTag().toString());
                    if(!key.isEmpty()){
                        TripListActivity.myDbRef.child(key).setValue(null);
                    }
                }
            });
        }

        return convertView;
    }
}
