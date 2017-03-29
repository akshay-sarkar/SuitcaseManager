package edu.uta.cse5320.dao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import edu.uta.cse5320.suitcasemanager.R;

/**
 * Created by Akshay on 3/16/2017.
 */

public class TripAdapter extends ArrayAdapter<TripData> {

    private LayoutInflater mInflater;
    private ArrayList<TripData> tripDatas;
    private int mViewResourceId;
    private Context context;

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

        TripData tripData = tripDatas.get(position);

        if (tripData != null) {
            TextView tripName = (TextView) convertView.findViewById(R.id.tripListLabelName);
            TextView tripAirline = (TextView) convertView.findViewById(R.id.textViewAirline);

            if (tripName != null) {
                tripName.setText(tripData.getTripName());
            }
            if(tripAirline != null && !tripData.getTripAirlineName().isEmpty()){
                tripAirline.setText(tripData.getTripAirlineName());
            }
        }

        return convertView;
    }

}
