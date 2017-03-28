package edu.uta.cse5320;

/**
 * Created by Akshay on 3/28/2017.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import edu.uta.cse5320.dao.AirlineData;
import edu.uta.cse5320.suitcasemanager.R;

public class AirlineAdapter extends BaseAdapter {

    // Declare Variables
    Context mContext;
    LayoutInflater inflater;
    private List<AirlineData> airlinesList = null;
    private ArrayList<AirlineData> arraylist;

    public AirlineAdapter(Context context,
                           List<AirlineData> airlinesList) {
        mContext = context;
        this.airlinesList = airlinesList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<AirlineData>();
        this.arraylist.addAll(airlinesList);
    }

    public class ViewHolder {
        TextView rank;
        TextView country;
        TextView population;
        //ImageView flag;
    }

    @Override
    public int getCount() {
        return airlinesList.size();
    }

    @Override
    public AirlineData getItem(int position) {
        return airlinesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.airline_list_layout, null);
            // Locate the TextViews in airlines_list_layout.xml
            holder.rank = (TextView) view.findViewById(R.id.rank);
            holder.country = (TextView) view.findViewById(R.id.country);
            holder.population = (TextView) view.findViewById(R.id.population);
            // Locate the ImageView in airlines_list_layout.xml
            //holder.flag = (ImageView) view.findViewById(R.id.flag);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.rank.setText(airlinesList.get(position).getAirlineCustomerCare());
        holder.country.setText(airlinesList.get(position).getAirlineName());
        holder.population.setText(airlinesList.get(position)
                .getAirlineEmail());
        // Set the results into ImageView
        //holder.flag.setImageResource(airlinesList.get(position).getAirlineUrl());
        // Listen for ListView Item Click
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
//                // Send single item click data to SingleItemView Class
//                Intent intent = new Intent(mContext, SingleItemView.class);
//                // Pass all data rank
//                intent.putExtra("rank",
//                        (airlinesList.get(position).getRank()));
//                // Pass all data country
//                intent.putExtra("country",
//                        (airlinesList.get(position).getCountry()));
//                // Pass all data population
//                intent.putExtra("population",
//                        (airlinesList.get(position).getPopulation()));
//                // Pass all data flag
//                intent.putExtra("flag",
//                        (airlinesList.get(position).getFlag()));
//                // Start SingleItemView Class
//                mContext.startActivity(intent);
            }
        });

        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        airlinesList.clear();
        if (charText.length() == 0) {
            airlinesList.addAll(arraylist);
        } else {
            for (AirlineData wp : arraylist) {
                if (wp.getAirlineName().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    airlinesList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

}
