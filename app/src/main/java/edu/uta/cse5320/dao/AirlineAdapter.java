package edu.uta.cse5320.dao;
/**
 * Created by Akshay on 3/28/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.uta.cse5320.suitcasemanager.R;

public class AirlineAdapter extends ArrayAdapter<AirlineData> {

    // Declare Variables
    Context mContext;
    LayoutInflater inflater;
    private ArrayList<AirlineData> airlinesList = null;
    private int mViewResourceId;
    private Filter filter;


    public AirlineAdapter(Context context, int textViewResourceId,
                           ArrayList<AirlineData> airlinesList) {
        super(context, textViewResourceId, airlinesList);
        mContext = context;
        this.airlinesList = airlinesList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
    }

    @Override
    public int getCount() {
        return airlinesList.size();
    }

    @Override
    public AirlineData getItem(int position) {
        return airlinesList.get(position);
    }

    public View getView(final int position, View view, ViewGroup parent) {

        view = inflater.inflate(mViewResourceId, null);

        AirlineData airlineData = airlinesList.get(position);

        if(airlineData !=null){
            final TextView airlineEmail = (TextView) view.findViewById(R.id.airlineEmail);
            TextView airlineName = (TextView) view.findViewById(R.id.airlineName);
            final TextView airlineURL = (TextView) view.findViewById(R.id.airlineURL);
            TextView airlinePhone = (TextView) view.findViewById(R.id.airlinePhone);

            if (airlineName != null) {
                airlineName.setText(airlineData.getAirlineName());
            }
            if (airlineEmail != null) {
                airlineEmail.setText(airlineData.getAirlineEmail());
                /*
                // Create the Intent
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                // Fill it with Data
                                emailIntent.setType("plain/text");
                                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"to@email.com"});
                                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
                                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");

                // Send it off to the Activity-Chooser
                context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                 */
                airlineEmail.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String emailAirline = airlineEmail.getText().toString();
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {emailAirline});
                        try {
                            mContext.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(mContext, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
            if (airlineURL != null && !airlineData.getAirlineUrl().isEmpty()) {
                airlineURL.setText(airlineData.getAirlineUrl());
                airlineURL.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TextView airlineURLTextView = (TextView) v.findViewById(R.id.airlineURL);
                        String urlAirline = airlineURL.getText().toString();
                        if(!urlAirline.startsWith("http://") && !urlAirline.startsWith("https://"))
                            urlAirline = "http://" + urlAirline;
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAirline));
                        mContext.startActivity(browserIntent);
                    }
                });
            }
            if (airlinePhone != null) {
                airlinePhone.setText(airlineData.getAirlineCustomerCare());
                airlinePhone.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView airlinePhoneTextView = (TextView) v.findViewById(R.id.airlinePhone);
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+airlinePhoneTextView.getText().toString()));
                        mContext.startActivity(intent);
                    }
                });

            }
        }
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

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new AppFilter<AirlineData>(airlinesList);
        return filter;
    }

    private class AppFilter<T> extends Filter {

        private ArrayList<T> sourceObjects;

        public AppFilter(List<T> objects) {
            sourceObjects = new ArrayList<T>();
            synchronized (this) {
                sourceObjects.addAll(objects);
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence chars) {
            String filterSeq = chars.toString().toLowerCase();
            FilterResults result = new FilterResults();

            if (filterSeq != null && filterSeq.length() > 0) {
                ArrayList<T> filter = new ArrayList<T>();

                for (T object : sourceObjects) {
                    // the filtering itself:
                    if (((AirlineData) object).getAirlineName().toString().toLowerCase().contains(filterSeq))
                        filter.add(object);
                }
                result.count = filter.size();
                result.values = filter;
            } else {
                // add all objects
                synchronized (this) {
                    result.values = sourceObjects;
                    result.count = sourceObjects.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // NOTE: this function is *always* called from the UI thread.
            ArrayList<T> filtered = (ArrayList<T>) results.values;
            notifyDataSetChanged();
            clear();
            for (int i = 0, l = filtered.size(); i < l; i++)
                add((AirlineData) filtered.get(i));
            notifyDataSetInvalidated();
        }
    }

}