package com.app.adapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.R;
import com.app.db.TF_phone_DAO;

public class TF_Adapter extends BaseAdapter {

	private final Context mCtx;
	private final List<TF_phone_DAO.Registro> mItems;
	private Handler mHandler;
	private LayoutInflater mInflater; 

	public TF_Adapter( Context ctx, List<TF_phone_DAO.Registro> items, Handler handler) {
		this.mCtx = ctx;
		this.mItems = items;
		this.mHandler = handler;
		this.mInflater = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE ); 
	}

	public int getCount() { return mItems.size(); }

	public Object getItem(int position) { return mItems.get(position); }

	public long getItemId(int position) { return position; }

	public View getView(int position, View convertView, ViewGroup parent) {
		
		if ( convertView == null ) 
			convertView = mInflater.inflate(R.layout.tf_list_item, parent, false); 

		// Instancia controles de pantalla y carga sus valores:
		((TextView) convertView.findViewById( R.id.tf_tv_id )).setText(  mItems.get(position).id );	// id
		((TextView) convertView.findViewById( R.id.tf_tv_name )).setText(  mItems.get(position).name );	// name
		((TextView) convertView.findViewById( R.id.tf_tv_json )).setText(  mItems.get(position).json );	// json

		
		// Eventos de acciones:
		
		
		return convertView;
	}
}
