package edu.uta.cse5320.suitcasemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import edu.uta.cse5320.util.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        int status = NetworkUtil.getConnectivityStatusString(context);
        if(status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
            Toast.makeText(context, "No Network Available. Check Internet Connection", Toast.LENGTH_SHORT ).show();
            //new ForceExitPause(context).execute();
        }else{
            //new ResumeForceExitPause(context).execute();
        }

    }
}
