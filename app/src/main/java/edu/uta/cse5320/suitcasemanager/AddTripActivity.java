package edu.uta.cse5320.suitcasemanager;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import edu.uta.cse5320.dao.TripData;
import edu.uta.cse5320.dao.TripHelper;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class AddTripActivity extends AppCompatActivity{

    private Button mSaveTripButton, mBtnSetStartDate, mBtnSetEndDate;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference myDbRef;
    private Context ctx;
    private EditText editTextTripName, editTextTripDetails;
    private Spinner spinnerTripAirline;
    //TextView editTextTripEndDate, editTextTripStartDate;
    private boolean isEditMode = false;
    private TripHelper tripHelperDB;
    private String message;
    Calendar myCalendar,currDate;
    DatePickerDialog.OnDateSetListener date;
    boolean startDateTriggerFlag = false;
    // for font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void updateLabel() throws MyException{
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        if(startDateTriggerFlag) {
            String currentDate = sdf.format(currDate.getTime());
            String btnTripStartDate = sdf.format(myCalendar.getTime());
            if (currentDate.compareTo(btnTripStartDate) > 0) {
                mBtnSetStartDate.setText("MM/DD/YYYY");
                throw new MyException("Only Future Dates are Allowed");
            }
            else{
                mBtnSetStartDate.setText(sdf.format(myCalendar.getTime()));
            }
        }
        else{
            String btnTripStartDate = mBtnSetStartDate.getText().toString();
            String btnTripEndDate = sdf.format(myCalendar.getTime());
            if (btnTripStartDate.compareTo(btnTripEndDate) > 0) {
                mBtnSetEndDate.setText("MM/DD/YYYY");
                throw new MyException("End Date is Older than Start Date");
            }
            else{
                mBtnSetEndDate.setText(sdf.format(myCalendar.getTime()));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        checkInternet();
        editTextTripName = (EditText) findViewById(R.id.editTextTripName);
        //editTextTripAirline = (EditText) findViewById(R.id.editTextTripAirline);
        spinnerTripAirline = (Spinner) findViewById(R.id.spinnerTripAirline);
        editTextTripDetails = (EditText) findViewById(R.id.editTextTripDetails);
        //editTextTripEndDate = (TextView) findViewById(R.id.editTextTripEndDate);
        //editTextTripStartDate = (TextView) findViewById(R.id.editTextTripStartDate);
        mSaveTripButton = (Button) findViewById(R.id.saveTripData);
        mBtnSetStartDate = (Button) findViewById(R.id.btnSetStartDate);
        mBtnSetEndDate = (Button) findViewById(R.id.btnSetEndDate);
        myCalendar = Calendar.getInstance();
        currDate = Calendar.getInstance();

        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                try{
                    updateLabel();
                }
                catch (MyException e){
                    e.printStackTrace();
                    Toast.makeText(ctx, "Error:  "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        mBtnSetStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDateTriggerFlag = true;
                new DatePickerDialog(AddTripActivity.this, date , myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        mBtnSetEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    startDateTriggerFlag = false;
                    new DatePickerDialog(AddTripActivity.this, date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        ctx = getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        /* Getting Data from the data */
        Intent intent = getIntent();
        message = intent.getStringExtra(TripListActivity.EXTRA_MESSAGE);
        //Toast.makeText(ctx, "Message::  "+message, Toast.LENGTH_SHORT).show();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        tripHelperDB = new TripHelper(this);

        /* Edit Case */
        if(message !=null && !message.isEmpty()){
            isEditMode = true;
            myDbRef = database.getReference("test").child(user.getUid()).child("Trips").child(message);
            //dbUpdates(myDbRef);
            System.out.println("myDbRef ..."+ myDbRef.getKey());
            editTextTripName.setFocusable(false);
            editTextTripName.setEnabled(false);

        }else{
            myDbRef = database.getReference("test").child(user.getUid()).child("Trips");
        }

        /* Google Sign In */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,  new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Toast.makeText(ctx, "Failed Connection..."+ result.hasResolution(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                if( firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(AddTripActivity.this, MainActivity.class));
                }
            }
        };
        /* Google Sign In - End Here*/

        //Save on Cloud
        mSaveTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTripDetails();
            }
        });


        if(isEditMode){
            //dbUpdates(myDbRef);
            myDbRef.addValueEventListener(postListener);
        }

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(300);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "101");
        sequence.setConfig(config);
        sequence.addSequenceItem(editTextTripName,"Enter a name for your trip", "GOT IT");
        sequence.addSequenceItem(mBtnSetStartDate,"Choose a date today/future as a start date for the trip", "GOT IT");
        sequence.addSequenceItem(mBtnSetEndDate,"Choose a date in future after the start date", "GOT IT");
        sequence.addSequenceItem(spinnerTripAirline,"Choose an Airline you wish to travel", "GOT IT");
        sequence.addSequenceItem(editTextTripDetails,"Give more description about the trip", "GOT IT");
        sequence.addSequenceItem(mSaveTripButton,"Save the trip instance", "GOT IT");
        sequence.start();

    }

    public void saveTripDetails(){
        String editTripName = editTextTripName.getText().toString();
        //String editTripStartDate = editTextTripStartDate.getText().toString();
        //String editTripEndDate = editTextTripEndDate.getText().toString();
        //String editTripAirline = editTextTripAirline.getText().toString();
        String spinnerTextTripAirline = String.valueOf(spinnerTripAirline.getSelectedItem());
        String editTripDetails = editTextTripDetails.getText().toString();
        String btnTripStartDate = mBtnSetStartDate.getText().toString();
        String btnTripEndDate = mBtnSetEndDate.getText().toString();
        if(editTripName.equals("")) {
            Toast.makeText(ctx, "Trip Name cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else{
            if(isEditMode){
                //TripData tripData = new TripData(editTripName, btnTripStartDate, btnTripEndDate, spinnerTextTripAirline, editTripDetails );

                //Updating the URL in Database
                Map<String, Object> updateTripDetails = new HashMap<String, Object>();
                updateTripDetails.put("tripName", editTripName);
                updateTripDetails.put("tripStartDate", btnTripStartDate);
                updateTripDetails.put("tripEndDate", btnTripEndDate);
                updateTripDetails.put("tripAirlineName", spinnerTextTripAirline);
                updateTripDetails.put("tripDetails", editTripDetails);

                myDbRef.updateChildren(updateTripDetails);

                //myDbRef.setValue(tripData);//update
                Toast.makeText(ctx, "Trip Updated", Toast.LENGTH_SHORT).show();
            }else{
                /* Add Case*/
                long id = tripHelperDB.addData(editTripName, btnTripStartDate, btnTripEndDate, editTripName, editTripDetails);
                if(id == -1){
                    System.out.println("Not Inserted");
                }

                TripData tripData = new TripData(id, editTripName, btnTripStartDate, btnTripEndDate, spinnerTextTripAirline, editTripDetails );
                myDbRef.push().setValue(tripData); //add
                Toast.makeText(ctx, "Trip Created", Toast.LENGTH_SHORT).show();
            }
            clearFormAndBack();
        }

        /* Clear Data and Move to Back Screen */
    }

    @Override
    protected void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void clearFormAndBack(){
        finish();
    }

    ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get Post object and use the values to update the UI
            TripData td = dataSnapshot.getValue(TripData.class);
            if(td != null){ // in case of delete issue persist
                editTextTripName.setText(td.getTripName());
                editTextTripDetails.setText(td.getTripDetails());
                spinnerTripAirline.setSelection(((ArrayAdapter<String>)spinnerTripAirline.getAdapter()).getPosition(td.getTripAirlineName()));
                //editTextTripAirline.setText(td.getTripAirlineName());
                mBtnSetStartDate.setText(td.getTripStartDate());
                mBtnSetEndDate.setText(td.getTripEndDate());
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

    private void checkInternet(){
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                NetworkInfo info = (NetworkInfo) extras.getParcelable("networkInfo");
                NetworkInfo.State state = info.getState();
                Log.d("TEST Internet", info.toString() + " " + state.toString());
                final AlertDialog alertDialog = new AlertDialog.Builder(AddTripActivity.this).create();

                if (state != NetworkInfo.State.CONNECTED) {
                    alertDialog.setTitle("Network Problem");
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage("No Network Available. Check Internet Connection");
                    alertDialog.setIcon(R.mipmap.ic_error);
                    alertDialog.show();
                }
                else {
                    if(alertDialog!=null )
                    alertDialog.dismiss();
                }


            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver((BroadcastReceiver) br, intentFilter);
    }

}
