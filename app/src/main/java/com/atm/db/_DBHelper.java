package com.atm.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class _DBHelper extends SQLiteOpenHelper {

// Esto est� disponible a partir de Level 13...mientras CASCAR�
//	@Override
//	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		onUpgrade(db,oldVersion,newVersion);
//		//super.onDowngrade(db, oldVersion, newVersion);
//	}

	private static final String TAG = _DBHelper.class.getSimpleName();

	public static final String DATABASE_NAME = "eed_db";
	private static final int DATABASE_VERSION = 1;

	// /////////////////////////////////////////////////
	// /////////////////////////////////////////////////
	// /////////////////////////////////////////////////
	// ENTIDADES CONTROLADAS
	public US_Users_DAO	us_Users = null;
	public PS_countries_DAO	ps_countries = null;
	public TF_phone_DAO	tf_phone = null;

	// /////////////////////////////////////////////////
	// /////////////////////////////////////////////////
	// /////////////////////////////////////////////////

	public SQLiteDatabase mDB;

	public _DBHelper(Context context) {
		// El constructor primero llama a 'onCreate() / onUpgrade()':
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mDB = getWritableDatabase();
		Log.i(TAG,"constructor _DBHelper()");

		// Construtor de cada entidad controlada:
		if ( us_Users == null ) us_Users = new US_Users_DAO();
		if ( ps_countries == null ) ps_countries = new PS_countries_DAO();
		if ( tf_phone == null ) tf_phone = new TF_phone_DAO();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG,"onCreate()");

		// Construtor de cada entidad controlada:
		if ( us_Users == null ) us_Users = new US_Users_DAO();
		if ( ps_countries == null ) ps_countries = new PS_countries_DAO();
		if ( tf_phone == null ) tf_phone = new TF_phone_DAO();

		// Create tables;
		us_Users.create(db);
		ps_countries.create(db);
		tf_phone.create(db);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG,"onUpgrade()");

		// Construtor de cada entidad controlada:
		if ( us_Users == null ) us_Users = new US_Users_DAO();
		if ( ps_countries == null ) ps_countries = new PS_countries_DAO();
		if ( tf_phone == null ) tf_phone = new TF_phone_DAO();

		// Drop tables;
		us_Users.dropTb(db);
		ps_countries.dropTb(db);
		tf_phone.dropTb(db);

		// Create tables;
		us_Users.create(db);
		ps_countries.create(db);
		tf_phone.create(db);

	}
}
