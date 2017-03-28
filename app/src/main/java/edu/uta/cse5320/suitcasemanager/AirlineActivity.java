package edu.uta.cse5320.suitcasemanager;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Locale;

import edu.uta.cse5320.AirlineAdapter;
import edu.uta.cse5320.dao.AirlineData;

public class AirlineActivity extends AppCompatActivity {

    // Declare Variables
    ListView list;
    AirlineAdapter adapter;
    EditText editSearch;
    ArrayList<AirlineData> airlineArrayList = new ArrayList<AirlineData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airline);
        // Locate the ListView in listview_main.xml
        list = (ListView) findViewById(R.id.listViewAirline);
        airlineArrayList.add(new AirlineData("Air India", "http://www.google.com", "18008901231", "air_india@airindia.com"));
        airlineArrayList.add(new AirlineData("Air France", "http://www.google.com", "18008901231", "air_india@airindia.com"));
        airlineArrayList.add(new AirlineData("British Airways", "http://www.google.com", "18008901231", "air_india@airindia.com"));
        airlineArrayList.add(new AirlineData("American Airline", "http://www.google.com", "18008901231", "air_india@airindia.com"));

        // Pass results to ListViewAdapter Class
        adapter = new AirlineAdapter(this, airlineArrayList);

        list.setAdapter(adapter);

        // Locate the EditText in listview_main.xml
        editSearch = (EditText) findViewById(R.id.editTextTripAirlineSearch);

        // Capture Text in EditText
        editSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                String text = editSearch.getText().toString().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                // TODO Auto-generated method stub
            }
        });
    }

}
