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
import com.app.db.US_Users_DAO;

public class US_Adapter extends BaseAdapter {

	private final Context mCtx;
	private final List<US_Users_DAO.Registro> mItems;
	private Handler mHandler;
	private LayoutInflater mInflater; 

	public US_Adapter( Context ctx, List<US_Users_DAO.Registro> items, Handler handler) {
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
			convertView = mInflater.inflate(R.layout.us_list_item, parent, false); 

		// Instancia controles de pantalla y carga sus valores:
		((TextView) convertView.findViewById( R.id.us_tv_sincro )).setText(  mItems.get(position).sincro );	// sincro
		((TextView) convertView.findViewById( R.id.us_tv_mark )).setText(  mItems.get(position).mark );	// mark
		((TextView) convertView.findViewById( R.id.us_tv_is_deleted )).setText(  mItems.get(position).is_deleted );	// is_deleted
		((TextView) convertView.findViewById( R.id.us_tv_author )).setText(  mItems.get(position).author );	// author
		((TextView) convertView.findViewById( R.id.us_tv_user_id )).setText(  mItems.get(position).user_id );	// user_id
		((TextView) convertView.findViewById( R.id.us_tv_password )).setText(  mItems.get(position).password );	// password
		((TextView) convertView.findViewById( R.id.us_tv_first_name )).setText(  mItems.get(position).first_name );	// first_name
		((TextView) convertView.findViewById( R.id.us_tv_last_name )).setText(  mItems.get(position).last_name );	// last_name
		((TextView) convertView.findViewById( R.id.us_tv_email )).setText(  mItems.get(position).email );	// email
		((TextView) convertView.findViewById( R.id.us_tv_phone )).setText(  mItems.get(position).phone );	// phone
		((TextView) convertView.findViewById( R.id.us_tv_country_id )).setText(  mItems.get(position).country_id );	// country_id
		((TextView) convertView.findViewById( R.id.us_tv_PS_name )).setText(  mItems.get(position).PS_name );	// PS_name
		((TextView) convertView.findViewById( R.id.us_tv_PS_flag_base64 )).setText(  mItems.get(position).PS_flag_base64 );	// PS_flag_base64
		((TextView) convertView.findViewById( R.id.us_tv_hash_code )).setText(  mItems.get(position).hash_code );	// hash_code
		((TextView) convertView.findViewById( R.id.us_tv_avatar )).setText(  mItems.get(position).avatar );	// avatar
		((TextView) convertView.findViewById( R.id.us_tv_isInactive )).setText(  mItems.get(position).isInactive );	// isInactive
		((TextView) convertView.findViewById( R.id.us_tv_inactivated_date )).setText(  mItems.get(position).inactivated_date );	// inactivated_date
		((TextView) convertView.findViewById( R.id.us_tv_inactivated_by )).setText(  mItems.get(position).inactivated_by );	// inactivated_by
		((TextView) convertView.findViewById( R.id.us_tv_json )).setText(  mItems.get(position).json );	// json

		
		// Eventos de acciones:
		
		
		return convertView;
	}
}
