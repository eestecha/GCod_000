package com.app.adapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.R;
import com.app.db.PS_countries_DAO;
import com.app.util.Util;

public class PS_Adapter extends BaseAdapter {

	private final Context mCtx;
	private final List<PS_countries_DAO.Registro> mItems;
	private Handler mHandler;
	private LayoutInflater mInflater; 

	public PS_Adapter( Context ctx, List<PS_countries_DAO.Registro> items, Handler handler) {
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
			convertView = mInflater.inflate(R.layout.ps_list_item, parent, false); 

		// Instancia controles de pantalla y carga sus valores:
		((TextView) convertView.findViewById( R.id.ps_tv_sincro )).setText(  mItems.get(position).sincro );	// sincro
		((TextView) convertView.findViewById( R.id.ps_tv_mark )).setText(  mItems.get(position).mark );	// mark
		((TextView) convertView.findViewById( R.id.ps_tv_is_deleted )).setText(  mItems.get(position).is_deleted );	// is_deleted
		((TextView) convertView.findViewById( R.id.ps_tv_author )).setText(  mItems.get(position).author );	// author
		((TextView) convertView.findViewById( R.id.ps_tv_country_id )).setText(  mItems.get(position).country_id );	// country_id
		((TextView) convertView.findViewById( R.id.ps_tv_name )).setText(  mItems.get(position).name );	// name
		((TextView) convertView.findViewById( R.id.ps_tv_alpha_2 )).setText(  mItems.get(position).alpha_2 );	// alpha_2
		((TextView) convertView.findViewById( R.id.ps_tv_alpha_3 )).setText(  mItems.get(position).alpha_3 );	// alpha_3
		((TextView) convertView.findViewById( R.id.ps_tv_flag_base64 )).setText(  mItems.get(position).flag_base64 );	// flag_base64
		((TextView) convertView.findViewById( R.id.ps_tv_json )).setText(  mItems.get(position).json );	// json

		ImageView iv = (ImageView) convertView.findViewById(R.id.ps_iv_flag);
		Util.decodeB64ToImageView(mItems.get(position).flag_base64, iv);

		// Eventos de acciones:
		
		
		return convertView;
	}
}
