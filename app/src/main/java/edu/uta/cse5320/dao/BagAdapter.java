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

public class BagAdapter extends ArrayAdapter<BagData>{

    private LayoutInflater mInflater;
    private ArrayList<BagData> bagDatas;
    private int mViewResourceId;
    private Context context;

    public BagAdapter(Context context, int textViewResourceId, ArrayList<BagData> bagData) {
        super(context, textViewResourceId, bagData);
        this.context = context;
        this.bagDatas = bagData;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(mViewResourceId, null);

        BagData bagData = bagDatas.get(position);

        if (bagData != null) {
            TextView bangName = (TextView) convertView.findViewById(R.id.tripBagLabelName);
            ImageView imageView1 = (ImageView) convertView.findViewById(R.id.imageViewBagPicture1);
            ImageView imageView2 = (ImageView) convertView.findViewById(R.id.imageViewBagPicture2);
            ImageView imageView3 = (ImageView) convertView.findViewById(R.id.imageViewBagPicture3);

            if (bangName != null) {
                bangName.setText(bagData.getBagName());
            }
            if(imageView1 != null && bagData.getImageUrl1()!=null && !bagData.getImageUrl1().isEmpty()){
                Picasso.with(context)
                        .load(bagData.getImageUrl1())
                        .fit().centerCrop()
                        .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                        .error(R.drawable.ic_add_a_photo_black_48dp)
                        .into(imageView1);
            }
            if(imageView2 != null && bagData.getImageUrl2()!=null &&!bagData.getImageUrl2().isEmpty()){
                Picasso.with(context)
                        .load(bagData.getImageUrl2())
                        .fit().centerCrop()
                        .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                        .error(R.drawable.ic_add_a_photo_black_48dp)
                        .into(imageView2);
            }
            if(imageView3 != null && bagData.getImageUrl3()!=null && !bagData.getImageUrl3().isEmpty()){
                Picasso.with(context)
                        .load(bagData.getImageUrl3())
                        .fit().centerCrop()
                        .placeholder(R.drawable.ic_add_a_photo_black_48dp)
                        .error(R.drawable.ic_add_a_photo_black_48dp)
                        .into(imageView3);
            }

        }

        return convertView;
    }
}
