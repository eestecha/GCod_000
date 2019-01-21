package com.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.db._DBHelper;
import com.app.util.K;

public class US_Users_EDTRCD extends Activity {

	// Poniendo en el 'manifiest.xml' en esta Activity: android:configChanges="orientation"
	//	se evita que rearranque la activity al rotar la pantalla.
	// ( ..asi no cascaran los dlg "espere..." al rotar la pantalla) 

	private static final String TAG = US_Users_EDTRCD.class.getSimpleName(); 

	private _DBHelper mDBHelper;
	private Context mCtx;
	private View mViewLayout;
	private ProgressDialog mProgressDialog = null;

	//////////
	// Declarar controles de pantalla
	private TextView mTv_titulo;
	// Fmt.RTV:
	public EditText mEt_sincro;	// sincro
	public EditText mEt_mark;	// mark
	public EditText mEt_is_deleted;	// is_deleted
	public EditText mEt_author;	// author
	public EditText mEt_user_id;	// user_id
	public EditText mEt_password;	// password
	public EditText mEt_first_name;	// first_name
	public EditText mEt_last_name;	// last_name
	public EditText mEt_email;	// email
	public EditText mEt_phone;	// phone
	public EditText mEt_country_id;	// country_id
	public EditText mEt_PS_name;	// PS_name
	public EditText mEt_PS_flag_base64;	// PS_flag_base64
	public EditText mEt_hash_code;	// hash_code
	public EditText mEt_avatar;	// avatar
	public EditText mEt_isInactive;	// isInactive
	public EditText mEt_inactivated_date;	// inactivated_date
	public EditText mEt_inactivated_by;	// inactivated_by
	public EditText mEt_json;	// json

	//////////

	////////////////////
	// Proceso de mensajes del programa. (Sobretodo los de pantalla)
	private final Handler mHandler = new Handler() {

		public void handleMessage( final Message msg ) {

			boolean rc = false;

			switch ( msg.what ) {

			case K.MSG_CARGANDO: 
				Log.i(TAG,"MSG_CARGANDO()");

				new Thread( new Runnable() { public void run() {
					cargarAdapters();
					mHandler.sendEmptyMessage( K.MSG_FIN_ACCION_SOLICITADA );
				} } ).start();

				break;

			case K.MSG_CARGANDO_ESPERE: 
				Log.i(TAG,"MSG_CARGANDO_ESPERE()");
				mProgressDialog = ProgressDialog.show(mCtx,"Proceso de carga","Ejecutando...", true);
				mHandler.sendEmptyMessage( K.MSG_CARGANDO );
				break;

			case K.MSG_FIN_ACCION_SOLICITADA: 
				Log.i(TAG,"MSG_FIN_ACCION_SOLICITADA()");

				cargarPantalla();

				if ( mProgressDialog != null ) mProgressDialog.dismiss();
				mProgressDialog = null;
				break;

			case K.MSG_CANCELAR: 
				Log.i(TAG,"MSG_CANCELAR()");
				finish();
				break;

			case K.MSG_ACEPTAR: 
				Log.i(TAG,"MSG_ACEPTAR()");
				StringBuilder msgs = new StringBuilder(); 
				rc = pMSG_ACEPTAR(msgs);
				Toast.makeText(mCtx, msgs.toString(), Toast.LENGTH_LONG).show();
				if ( rc ) { finish(); }
				break;

			}

		}

	};

	////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"onCreate()");
		super.onCreate(savedInstanceState);
		
		//setContentView(R.layout.us_item);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE ); 
		mViewLayout = inflater.inflate(R.layout.us_item, null, false);
		setContentView(mViewLayout);

		mCtx = this;
		if (mDBHelper==null) mDBHelper = new _DBHelper(this);

		// Liberar...
		mTv_titulo = null;
		mEt_sincro = null;	// sincro
		mEt_mark = null;	// mark
		mEt_is_deleted = null;	// is_deleted
		mEt_author = null;	// author
		mEt_user_id = null;	// user_id
		mEt_password = null;	// password
		mEt_first_name = null;	// first_name
		mEt_last_name = null;	// last_name
		mEt_email = null;	// email
		mEt_phone = null;	// phone
		mEt_country_id = null;	// country_id
		mEt_PS_name = null;	// PS_name
		mEt_PS_flag_base64 = null;	// PS_flag_base64
		mEt_hash_code = null;	// hash_code
		mEt_avatar = null;	// avatar
		mEt_isInactive = null;	// isInactive
		mEt_inactivated_date = null;	// inactivated_date
		mEt_inactivated_by = null;	// inactivated_by
		mEt_json = null;	// json

		// Instanciar controles de pantalla
		mTv_titulo = (TextView) findViewById(R.id.us_tv_titulo);
		mEt_sincro = (EditText) findViewById(R.id.us_et_sincro);	// sincro
		mEt_mark = (EditText) findViewById(R.id.us_et_mark);	// mark
		mEt_is_deleted = (EditText) findViewById(R.id.us_et_is_deleted);	// is_deleted
		mEt_author = (EditText) findViewById(R.id.us_et_author);	// author
		mEt_user_id = (EditText) findViewById(R.id.us_et_user_id);	// user_id
		mEt_password = (EditText) findViewById(R.id.us_et_password);	// password
		mEt_first_name = (EditText) findViewById(R.id.us_et_first_name);	// first_name
		mEt_last_name = (EditText) findViewById(R.id.us_et_last_name);	// last_name
		mEt_email = (EditText) findViewById(R.id.us_et_email);	// email
		mEt_phone = (EditText) findViewById(R.id.us_et_phone);	// phone
		mEt_country_id = (EditText) findViewById(R.id.us_et_country_id);	// country_id
		mEt_PS_name = (EditText) findViewById(R.id.us_et_PS_name);	// PS_name
		mEt_PS_flag_base64 = (EditText) findViewById(R.id.us_et_PS_flag_base64);	// PS_flag_base64
		mEt_hash_code = (EditText) findViewById(R.id.us_et_hash_code);	// hash_code
		mEt_avatar = (EditText) findViewById(R.id.us_et_avatar);	// avatar
		mEt_isInactive = (EditText) findViewById(R.id.us_et_isInactive);	// isInactive
		mEt_inactivated_date = (EditText) findViewById(R.id.us_et_inactivated_date);	// inactivated_date
		mEt_inactivated_by = (EditText) findViewById(R.id.us_et_inactivated_by);	// inactivated_by
		mEt_json = (EditText) findViewById(R.id.us_et_json);	// json

		// Inactivar edicion de campos PK:
		mEt_user_id.setFocusable(false); mEt_user_id.setEnabled(false);	// user_id

		((Button) findViewById(R.id.bt_cancelar)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { mHandler.sendEmptyMessage( K.MSG_CANCELAR); }
		});
		((Button) findViewById(R.id.bt_aceptar)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { mHandler.sendEmptyMessage( K.MSG_ACEPTAR); }
		});

	}

	@Override
	protected void onPause() {
		Log.i(TAG,"onPause()"); if (mDBHelper != null) { Log.i(TAG,"CLOSE DB"); mDBHelper.close(); }
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.i(TAG,"onResume()"); if ( ! mDBHelper.mDB.isOpen() ) { Log.i(TAG,"OPEN DB"); mDBHelper.mDB = mDBHelper.getWritableDatabase(); }

		if ( ! K.IS_PANTALLAS_GIRAR ) { setRequestedOrientation( K.DEFAULT_ORIENTATION ); }

		mHandler.sendEmptyMessage( K.MSG_CARGANDO_ESPERE );

		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ( (keyCode == KeyEvent.KEYCODE_BACK) ) {
//			if ( K.IS_PANTALLAS_NO_ATRAS ) {
				Log.w(TAG,"NAVEGAR ATRAS NO PERMITIDO.");
				Toast.makeText(mCtx, "Navegar atrás no permitido", Toast.LENGTH_SHORT).show();
				return true;
//			}
		}
		return super.onKeyDown(keyCode, event);
	}

	////////////////////
	// Funciones internas

	private void cargarAdapters() {
		Log.i(TAG,"cargarAdapters()"); if ( ! mDBHelper.mDB.isOpen() ) { Log.w(TAG,"OPEN DB (...estaba cerrada!!)"); mDBHelper.mDB = mDBHelper.getWritableDatabase(); }

		// Registro a editar en la pantalla:
		mDBHelper.us_Users.mRegistro.clean();

		//	DEBE HABERSE LLAMADO: intent.putExtra ( "xx_reg", mDBHelper.xx_aliasEntidad.mRegistro );
		// Lee parametro de entrada para PK a editar:
		com.app.db.US_Users_DAO.Registro reg = getIntent().getExtras().getParcelable( "us_reg" );

		if ( reg != null ) {
			// Mover campos desde parametros:
			mDBHelper.us_Users.mRegistro.copyFrom(reg);	
			// No tiene por que estar completo el registro...solo se esperaba la PK:
			mDBHelper.us_Users.getRcd(mDBHelper.mDB);
		}
	}

	private void cargarPantalla() {
		Log.i(TAG,"cargarPantalla()");
		mTv_titulo.setText( "Editar Users" );
		K.LeftToRight_Animation(mViewLayout,350);

		// Mover campos desde la base de datos:
		mEt_sincro.setText( mDBHelper.us_Users.mRegistro.sincro );	// sincro
		mEt_mark.setText( mDBHelper.us_Users.mRegistro.mark );	// mark
		mEt_is_deleted.setText( mDBHelper.us_Users.mRegistro.is_deleted );	// is_deleted
		mEt_author.setText( mDBHelper.us_Users.mRegistro.author );	// author
		mEt_user_id.setText( mDBHelper.us_Users.mRegistro.user_id );	// user_id
		mEt_password.setText( mDBHelper.us_Users.mRegistro.password );	// password
		mEt_first_name.setText( mDBHelper.us_Users.mRegistro.first_name );	// first_name
		mEt_last_name.setText( mDBHelper.us_Users.mRegistro.last_name );	// last_name
		mEt_email.setText( mDBHelper.us_Users.mRegistro.email );	// email
		mEt_phone.setText( mDBHelper.us_Users.mRegistro.phone );	// phone
		mEt_country_id.setText( mDBHelper.us_Users.mRegistro.country_id );	// country_id
		mEt_PS_name.setText( mDBHelper.us_Users.mRegistro.PS_name );	// PS_name
		mEt_PS_flag_base64.setText( mDBHelper.us_Users.mRegistro.PS_flag_base64 );	// PS_flag_base64
		mEt_hash_code.setText( mDBHelper.us_Users.mRegistro.hash_code );	// hash_code
		mEt_avatar.setText( mDBHelper.us_Users.mRegistro.avatar );	// avatar
		mEt_isInactive.setText( mDBHelper.us_Users.mRegistro.isInactive );	// isInactive
		mEt_inactivated_date.setText( mDBHelper.us_Users.mRegistro.inactivated_date );	// inactivated_date
		mEt_inactivated_by.setText( mDBHelper.us_Users.mRegistro.inactivated_by );	// inactivated_by
		mEt_json.setText( mDBHelper.us_Users.mRegistro.json );	// json
		
		// Se autoselecciona contenido del campo con el focus:
		if (mEt_sincro.isFocused()) { mEt_sincro.selectAll(); }	// sincro
		if (mEt_mark.isFocused()) { mEt_mark.selectAll(); }	// mark
		if (mEt_is_deleted.isFocused()) { mEt_is_deleted.selectAll(); }	// is_deleted
		if (mEt_author.isFocused()) { mEt_author.selectAll(); }	// author
		if (mEt_user_id.isFocused()) { mEt_user_id.selectAll(); }	// user_id
		if (mEt_password.isFocused()) { mEt_password.selectAll(); }	// password
		if (mEt_first_name.isFocused()) { mEt_first_name.selectAll(); }	// first_name
		if (mEt_last_name.isFocused()) { mEt_last_name.selectAll(); }	// last_name
		if (mEt_email.isFocused()) { mEt_email.selectAll(); }	// email
		if (mEt_phone.isFocused()) { mEt_phone.selectAll(); }	// phone
		if (mEt_country_id.isFocused()) { mEt_country_id.selectAll(); }	// country_id
		if (mEt_PS_name.isFocused()) { mEt_PS_name.selectAll(); }	// PS_name
		if (mEt_PS_flag_base64.isFocused()) { mEt_PS_flag_base64.selectAll(); }	// PS_flag_base64
		if (mEt_hash_code.isFocused()) { mEt_hash_code.selectAll(); }	// hash_code
		if (mEt_avatar.isFocused()) { mEt_avatar.selectAll(); }	// avatar
		if (mEt_isInactive.isFocused()) { mEt_isInactive.selectAll(); }	// isInactive
		if (mEt_inactivated_date.isFocused()) { mEt_inactivated_date.selectAll(); }	// inactivated_date
		if (mEt_inactivated_by.isFocused()) { mEt_inactivated_by.selectAll(); }	// inactivated_by
		if (mEt_json.isFocused()) { mEt_json.selectAll(); }	// json

	}

	protected boolean pMSG_ACEPTAR( StringBuilder msgs ) {
		boolean resultado = false;
		
		// Mover campos desde la pantalla:
		mDBHelper.us_Users.mRegistro.clean();
		mDBHelper.us_Users.mRegistro.sincro = mEt_sincro.getText().toString();	// sincro
		mDBHelper.us_Users.mRegistro.mark = mEt_mark.getText().toString();	// mark
		mDBHelper.us_Users.mRegistro.is_deleted = mEt_is_deleted.getText().toString();	// is_deleted
		mDBHelper.us_Users.mRegistro.author = mEt_author.getText().toString();	// author
		mDBHelper.us_Users.mRegistro.user_id = mEt_user_id.getText().toString();	// user_id
		mDBHelper.us_Users.mRegistro.password = mEt_password.getText().toString();	// password
		mDBHelper.us_Users.mRegistro.first_name = mEt_first_name.getText().toString();	// first_name
		mDBHelper.us_Users.mRegistro.last_name = mEt_last_name.getText().toString();	// last_name
		mDBHelper.us_Users.mRegistro.email = mEt_email.getText().toString();	// email
		mDBHelper.us_Users.mRegistro.phone = mEt_phone.getText().toString();	// phone
		mDBHelper.us_Users.mRegistro.country_id = mEt_country_id.getText().toString();	// country_id
		mDBHelper.us_Users.mRegistro.PS_name = mEt_PS_name.getText().toString();	// PS_name
		mDBHelper.us_Users.mRegistro.PS_flag_base64 = mEt_PS_flag_base64.getText().toString();	// PS_flag_base64
		mDBHelper.us_Users.mRegistro.hash_code = mEt_hash_code.getText().toString();	// hash_code
		mDBHelper.us_Users.mRegistro.avatar = mEt_avatar.getText().toString();	// avatar
		mDBHelper.us_Users.mRegistro.isInactive = mEt_isInactive.getText().toString();	// isInactive
		mDBHelper.us_Users.mRegistro.inactivated_date = mEt_inactivated_date.getText().toString();	// inactivated_date
		mDBHelper.us_Users.mRegistro.inactivated_by = mEt_inactivated_by.getText().toString();	// inactivated_by
		mDBHelper.us_Users.mRegistro.json = mEt_json.getText().toString();	// json
		
		// Validar PK:
		if ( 
			   mDBHelper.us_Users.mRegistro.user_id == null || mDBHelper.us_Users.mRegistro.user_id.trim().length() < 1
			) {
			msgs.append("Claves obligatorias.");
		}
		
		// Validar los valores de los campos:
		
		// Validar las relaciones de los campos:
		
		// Grabar:
		if ( 0 == msgs.length() ) {
			msgs.append( Long.toString( 
				mDBHelper.us_Users.updObj(mDBHelper.mDB)
			) + " registro cambiado." );
			resultado = true;
		}

		return resultado;

	}
	////////////////////

	////////////////////
}
