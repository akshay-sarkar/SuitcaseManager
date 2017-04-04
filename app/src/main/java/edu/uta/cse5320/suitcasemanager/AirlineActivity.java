package edu.uta.cse5320.suitcasemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

import edu.uta.cse5320.dao.AirlineAdapter;
import edu.uta.cse5320.dao.AirlineData;
import edu.uta.cse5320.util.ApplicationConstant;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AirlineActivity extends AppCompatActivity {

    // Declare Variables
    ListView list;
    AirlineAdapter adapter;
    EditText editSearch;
    ArrayList<AirlineData> airlineArrayList;
    private DatabaseReference myDbRef;

    private ProgressDialog progressDialog;

    // for font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airline);

        //Progress for operations
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Retrieving Your Data..");
        progressDialog.show();

        airlineArrayList = new ArrayList<AirlineData>();
        // Locate the ListView in listview_main.xml
        list = (ListView) findViewById(R.id.listViewAirline);
        list.setTextFilterEnabled(true);
//        airlineArrayList.add(new AirlineData("Air India", "http://www.google.com", "18008901231", "air_india@airindia.com"));
//        airlineArrayList.add(new AirlineData("Air France", "http://www.google.com", "18008901231", "air_india@airindia.com"));
//        airlineArrayList.add(new AirlineData("British Airways", "http://www.google.com", "18008901231", "air_india@airindia.com"));
//        airlineArrayList.add(new AirlineData("American Airline", "http://www.google.com", "18008901231", "air_india@airindia.com"));

        // Pass results to ListViewAdapter Class
        adapter = new AirlineAdapter(this, R.layout.airline_list_layout, airlineArrayList);
        adapter.setNotifyOnChange(true);
        list.setAdapter(adapter);

        // Locate the EditText in listview_main.xml
        editSearch = (EditText) findViewById(R.id.editTextTripAirlineSearch);

        // Capture Text in EditText
        editSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                if (adapter != null) {
                    adapter.getFilter().filter(arg0.toString());
                } else {
                    Log.d("filter", "no filter availible");
                }
            }
        });


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myDbRef = database.getReference(ApplicationConstant.airline_prop);

        myDbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println(" onChildAdded"+dataSnapshot);
                AirlineData airlineData = dataSnapshot.getValue(AirlineData.class);
                airlineArrayList.add(airlineData);

                // Dimiss the dialog box
                progressDialog.dismiss();
//                //Key - Value : TripName - f_id
//                hmap.put(tripData.getTripName(), dataSnapshot.getKey());
//
//                /* Inserting data in DB only when not existed*/
//                int count = tripHelperDB.getListContent(tripData.getId());
//                if(count <= 0){
//                    System.out.println("Not Inserted!! Inserting Now..");
//                    tripHelperDB.addDataCompleteSync(tripData.getId(), tripData.getTripName(), tripData.getTripStartDate(), tripData.getTripEndDate(),tripData.getTripAirlineName(),tripData.getTripDetails());
//                }
//                /* Adding in List */
//                tripDataList.add(tripData);
//                updateListView();
//
//                // Dimiss the dialog box
//                progressDialog.dismiss();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
//                System.out.println(TAG + "onChildChanged" + dataSnapshot.getKey() +" Value :"+ dataSnapshot.getValue().toString());
//                TripData tripData = dataSnapshot.getValue(TripData.class);
//                if(tripData != null){
//
//                    Object oldKey = getKeyFromValue(hmap, dataSnapshot.getKey());
//                    //Key - Value : TripName - f_id
//                    hmap.remove(oldKey);
//                    hmap.put(tripData.getTripName(), dataSnapshot.getKey());
//                    /* Updating data in DB*/
//                    boolean flag = tripHelperDB.updateDetails(tripData.getId(), tripData.getTripName(), tripData.getTripStartDate(), tripData.getTripEndDate(),tripData.getTripAirlineName(),tripData.getTripDetails());
//
//                    /* updating in List */
//                    if(flag){
//                        Iterator<TripData> itr = tripDataList.iterator();
//                        while (itr.hasNext()) {
//                            TripData element = itr.next();
//                            if(element.getId() == tripData.getId()) {
//                                tripDataList.remove(element);
//                                break;
//                            }
//                        }
//                        tripDataList.add(tripData);
//                        myTripAdapter.notifyDataSetChanged();
//                        updateListView();
//                    }
//                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                System.out.println(TAG + " onChildRemoved");
//                TripData tripData = dataSnapshot.getValue(TripData.class);
//
//                //Update Database and List Here
//                boolean flag = tripHelperDB.deleteContent(tripData.getId());
//
//                if(flag){
//                    hmap.remove(tripData.getTripName());
//                    Iterator<TripData> itr = tripDataList.iterator();
//                    while (itr.hasNext()) {
//                        TripData element = itr.next();
//                        if(element.getId() == tripData.getId()) {
//                            tripDataList.remove(element);
//                            break;
//                        }
//                    }
//                    updateListView();
//                    myTripAdapter.notifyDataSetChanged();
//                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

}
