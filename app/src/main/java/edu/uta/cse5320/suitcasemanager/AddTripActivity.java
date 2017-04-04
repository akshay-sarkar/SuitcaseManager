package edu.uta.cse5320.suitcasemanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

import edu.uta.cse5320.dao.TripData;
import edu.uta.cse5320.dao.TripHelper;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddTripActivity extends AppCompatActivity {

    private Button mSaveTripButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference myDbRef;
    private Context ctx;
    private EditText editTextTripName, editTextTripAirline, editTextTripDetails, editTextTripEndDate, editTextTripStartDate;
    private boolean isEditMode = false;
    private TripHelper tripHelperDB;
    private String message;

    // for font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        editTextTripName = (EditText) findViewById(R.id.editTextTripName);
        editTextTripAirline = (EditText) findViewById(R.id.editTextTripAirline);
        editTextTripDetails = (EditText) findViewById(R.id.editTextTripDetails);
        editTextTripEndDate = (EditText) findViewById(R.id.editTextTripEndDate);
        editTextTripStartDate = (EditText) findViewById(R.id.editTextTripStartDate);
        mSaveTripButton = (Button) findViewById(R.id.saveTripData);



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

    }

    public void saveTripDetails(){
        String editTripName = editTextTripName.getText().toString();
        String editTripStartDate = editTextTripStartDate.getText().toString();
        String editTripEndDate = editTextTripEndDate.getText().toString();
        String editTripAirline = editTextTripAirline.getText().toString();
        String editTripDetails = editTextTripDetails.getText().toString();

        if(isEditMode){
            TripData tripData = new TripData(editTripName, editTripStartDate, editTripEndDate, editTripAirline, editTripDetails );

            //Updating the URL in Database
            Map<String, Object> updateTripDetails = new HashMap<String, Object>();
            updateTripDetails.put("tripName", editTripName);
            updateTripDetails.put("tripStartDate", editTripStartDate);
            updateTripDetails.put("tripEndDate", editTripEndDate);
            updateTripDetails.put("tripAirlineName", editTripAirline);
            updateTripDetails.put("tripDetails", editTripDetails);

            myDbRef.updateChildren(updateTripDetails);

            //myDbRef.setValue(tripData);//update
            Toast.makeText(ctx, "Trip Updated", Toast.LENGTH_SHORT).show();
        }else{
            /* Add Case*/
            long id = tripHelperDB.addData(editTripName, editTripStartDate, editTripEndDate, editTripName, editTripDetails);
            if(id == -1){
                System.out.println("Not Inserted");
            }

            TripData tripData = new TripData(id, editTripName, editTripStartDate, editTripEndDate, editTripAirline, editTripDetails );
            myDbRef.push().setValue(tripData); //add
            Toast.makeText(ctx, "Trip Created", Toast.LENGTH_SHORT).show();
        }

        /* Clear Data and Move to Back Screen */
        clearFormAndBack();
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
                editTextTripName.setText(td.getTripName().toString());
                editTextTripDetails.setText(td.getTripDetails().toString());
                editTextTripAirline.setText(td.getTripAirlineName().toString());
                editTextTripStartDate.setText(td.getTripStartDate().toString());
                editTextTripEndDate.setText(td.getTripEndDate().toString());
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

}
