package edu.uta.cse5320.suitcasemanager;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

import edu.uta.cse5320.util.ApplicationConstant;

/**
 * Created by Akshay on 4/30/2017.
 */

public class TipDialogClass extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button dismiss;
    public TextView tipHeading, tipBody;
    int min = 0;
    int max = ApplicationConstant.tipheading.length;

    public TipDialogClass(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tip_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dismiss = (Button) findViewById(R.id.btn_no);
        dismiss.setOnClickListener(this);
        tipHeading = (TextView) findViewById(R.id.proTipHeading);
        tipBody = (TextView) findViewById(R.id.proTipBody);
        //Random Generation of Tips
        int n = (int)(Math.random() * (max - min) + min);
        tipHeading.setText(ApplicationConstant.tipheading[n]);
        tipBody.setText(ApplicationConstant.tipBody[n]);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
