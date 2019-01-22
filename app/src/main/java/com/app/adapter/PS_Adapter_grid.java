package com.app.adapter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.app.R;
import com.app.db.PS_countries_DAO;
import com.app.util.Util;

import java.util.List;

public class PS_Adapter_grid extends BaseAdapter {
    private Context mCtxt;
    private final List<PS_countries_DAO.Registro> mItems;

    public PS_Adapter_grid(Context ctx, List<PS_countries_DAO.Registro> items, Handler handler) {
        mCtxt = ctx;
        this.mItems = items;
    }

    public int getCount() {
        return this.mItems.size(); // mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.i(this.getClass().getSimpleName()+ "getView( position ) : ", "" + position);

        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mCtxt);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(300,300));
//            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap( Util.decodeB64ToBitmap(mItems.get(position).flag_base64) );
//        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to internal images
    private Integer[] mThumbIds = {
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7
    };
}