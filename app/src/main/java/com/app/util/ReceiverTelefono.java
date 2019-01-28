package com.app.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.app.MainActivity;
import com.app.db._DBHelper;

import java.util.Calendar;

public class ReceiverTelefono extends BroadcastReceiver {


	private String TAG = getClass().getSimpleName();

	private Context mCtx;
	private _DBHelper mDBHelper;

	private static String mNumero = "?";
	private static boolean mIsAtendida = false;
	private static boolean mIsHaSonado = false;
//	private static boolean mIsSaliente = false;

	@Override
	public void onReceive(Context context, Intent intent) {

		mCtx = context;

		TelephonyManager telMan = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		proceso( telMan.getCallState(), intent );

	}

	private void proceso( int state, Intent intent ) {

		//SI "DESCUELGA" SIN "RING" PREVIO, ES SALIENTE...

//		Log.i(TAG,"onReceive: intent ---> " + intent );
//		Log.i(TAG,"onReceive: EXTRAS:  " + intent.getExtras().size() );
//		Bundle map = intent.getExtras();
//		for ( int i=0; i<map.size(); i++ ) {
//			Log.i(TAG,"onReceive: EXTRA ---> " + i + " " + map.toString() );
//		}


//		if ( intent.ACTION_NEW_OUTGOING_CALL.equals( intent.getAction() ) ) {
//			Log.i(TAG,"onReceive: intent.ACTION_NEW_OUTGOING_CALL ---> " + mNumero);
//			mIsSaliente = true;
//			return;
//		}

		switch(state) {
			// Al colgar
			case TelephonyManager.CALL_STATE_IDLE:

				Log.i(TAG,"onReceive.CALL_STATE_IDLE ---> " + mNumero);
				if ( ! mIsAtendida ) {
					persistir(mNumero,mIsAtendida,mIsHaSonado);	// Si no ha sido atendida es ENTRANTE NO ATENDIDA
				}

				// RESET: (Son variables ESTÁTICAS...)
				mNumero = "?";
				mIsAtendida = false;
				mIsHaSonado = false;

				break;

			// Al atender/desatender o lanzar operación de llamada(atendida)
			case TelephonyManager.CALL_STATE_OFFHOOK:
				Log.i(TAG,"onReceive.CALL_STATE_OFFHOOK ---> " + intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));

				mIsAtendida = true;

				persistir(mNumero,mIsAtendida,mIsHaSonado);		// Si no ha sonado ES SALIENTE,  si ha sonado es ENTRANTE ATENDIDA

				break;

			// Llamada sonando
			case TelephonyManager.CALL_STATE_RINGING:

				mIsHaSonado = true;

				// "Extra" Sólo disponible en 'CALL_STATE_RINGING':
				mNumero = new String(intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));
				mNumero = mNumero == null ? "?" : mNumero;

				Log.i(TAG,"onReceive.CALL_STATE_RINGING ---> " + mNumero);
				break;
		}
	}

	private void persistir( String numero, boolean isAtendida, boolean mIsHaSonado) {
		Log.i(TAG,"persistir() >>>>>>");

		String frase = numero + " ";

		if (mIsHaSonado && isAtendida) {
			frase += "llamada atendida";
		}
		if (mIsHaSonado && !isAtendida) {
			frase += "llamada perdida";
		}
		if (!mIsHaSonado && isAtendida) {
			frase += "llamada saliente";
		}
		Calendar c = Calendar.getInstance();

		mDBHelper = new _DBHelper(mCtx);
		if ( ! mDBHelper.mDB.isOpen() ) { Log.w(TAG,"OPEN DB (...estaba cerrada!!)"); mDBHelper.mDB = mDBHelper.getWritableDatabase(); }
		mDBHelper.tf_phone.mRegistro.id = "" + c.getTimeInMillis();
		mDBHelper.tf_phone.mRegistro.name = frase;
		mDBHelper.tf_phone.mRegistro.json = "{}";
		mDBHelper.tf_phone.crtObj(mDBHelper.mDB);
		if (mDBHelper != null) { Log.i(TAG,"CLOSE DB"); mDBHelper.close(); }

		Log.i(TAG,"*** REGISTRO en BD: " + mDBHelper.tf_phone.mRegistro.id + " " + frase);

		Intent intent = new Intent(mCtx, MainActivity.class);
		intent.putExtra ( "phone_receiver", frase  );
		// intent.setClassName("com.test", "com.test.MainActivity");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mCtx.startActivity(intent);




/*

		_DBHelper dbHelper = new _DBHelper(mCtx);
//		if (dbHelper != null) { Log.i(TAG,"CLOSE DB"); dbHelper.close(); }
		if ( ! dbHelper.mDB.isOpen() ) { Log.i(TAG,"OPEN DB"); dbHelper.mDB = dbHelper.getWritableDatabase(); }
		///////////////////

		Log.i(TAG,"persistir() --> " + numero + (isAtendida?" Atendida":" No atendida") + ((mIsHaSonado?" Entrante":" Saliente")) );

		// La función estática de 'Util.getHoy_aaaa_mm_dd_hh_mm_ss()' siempre devuelve el mismo valor en esta llamada...
		dbHelper.tf_llamadas.mRegistro.clean();
		dbHelper.tf_llamadas.mRegistro.mClave = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());
		dbHelper.tf_llamadas.mRegistro.mTelefono = numero;
		dbHelper.tf_llamadas.mRegistro.mAtendida = (isAtendida?"+":"-");
		dbHelper.tf_llamadas.mRegistro.mAtendida += (mIsHaSonado?"E":"S");	// Si había sonado: Entrante...
		////////////////////////
		// Campos deducidos del número de teléfono:
		dbHelper.hq_cliente.getClienteDesdeTfno(dbHelper.mDB, numero);
		if ( dbHelper.hq_cliente.mRegistros.size() > 0 ) {
			dbHelper.tf_llamadas.mRegistro.mDescriptivo = dbHelper.hq_cliente.mRegistros.get(0).HQSYTX;  // Nom.Cliente
			dbHelper.tf_llamadas.mRegistro.mKCliente = dbHelper.hq_cliente.mRegistros.get(0).HQO6NB;  // Cliente
		}
		////////////////////////
		dbHelper.tf_llamadas.crtObj(dbHelper.mDB);

		///////////////////
		if (dbHelper != null) { Log.i(TAG,"CLOSE DB"); dbHelper.close(); }
*/
		Log.i(TAG,"persistir() <<<<<<");
	}

}

// OTRO MÉTODO:
//1. In your manifest you should have
//<uses-permission android:name="android.permission.CALL_PHONE" />
//<uses-permission
//android:name="android.permission.PROCESS_OUTGOING_CALLS"/>
//<uses-permission
//android:name="android.permission.READ_PHONE_STATE"/>
//<intent-filter>
//      <action android:name="android.intent.action.PHONE_STATE"/>
//      <action android:name="Test" />
//</intent-filter>
//2. Register your broadcast receiver in the main activity
//    IntentFilter filter  = new IntentFilter
//("bct.com.MyEventReceiver");
//    eventRcvr = new MyEventReceiver();
//    this.registerReceiver(eventRcvr, filter);
//3. Implement a Broadcast receiver
//public class MyEventReceiver extends BroadcastReceiver {
//...
//public void onReceive(Context context, Intent intent) {
//    System.out.println("Receiver Object in onReceive: "+this);
//    NewPhoneStateListener phoneListener=new NewPhoneStateListener
//();
//    TelephonyManager telephony = (TelephonyManager)
//                     context.getSystemService
//(Context.TELEPHONY_SERVICE);
//    telephony.listen
//(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
//    Log.d(TAG,"intent:"+intent.toString());
//}
//...
//}
//
//4. New Class
//public class NewPhoneStateListener extends PhoneStateListener {
//public void onCallStateChanged(int state,String incomingNumber){
//  switch(state)
//  {
//    //whatever you want to do here
//  }
//}
//}

