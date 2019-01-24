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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.db._DBHelper;
import com.app.util.K;
import com.app.util.Util;

public class PS_countries_EDTRCD extends Activity {

	// Poniendo en el 'manifiest.xml' en esta Activity: android:configChanges="orientation"
	//	se evita que rearranque la activity al rotar la pantalla.
	// ( ..asi no cascaran los dlg "espere..." al rotar la pantalla) 

	private static final String TAG = PS_countries_EDTRCD.class.getSimpleName(); 

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
	public EditText mEt_country_id;	// country_id
	public EditText mEt_name;	// name
	public EditText mEt_alpha_2;	// alpha_2
	public EditText mEt_alpha_3;	// alpha_3
	public EditText mEt_flag_base64;	// flag_base64
	public EditText mEt_json;	// json

	public ImageView mIv_flag;	// flag image
	public String mBCK_flag_base64;	// flag_base64

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
		
		//setContentView(R.layout.ps_item);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE ); 
		mViewLayout = inflater.inflate(R.layout.ps_item, null, false);
		setContentView(mViewLayout);

		mCtx = this;
		if (mDBHelper==null) mDBHelper = new _DBHelper(this);

		// Liberar...
		mTv_titulo = null;
		mEt_sincro = null;	// sincro
		mEt_mark = null;	// mark
		mEt_is_deleted = null;	// is_deleted
		mEt_author = null;	// author
		mEt_country_id = null;	// country_id
		mEt_name = null;	// name
		mEt_alpha_2 = null;	// alpha_2
		mEt_alpha_3 = null;	// alpha_3
		mEt_flag_base64 = null;	// flag_base64
		mEt_json = null;	// json

		mIv_flag = null;

		// Instanciar controles de pantalla
		mTv_titulo = (TextView) findViewById(R.id.ps_tv_titulo);
		mEt_sincro = (EditText) findViewById(R.id.ps_et_sincro);	// sincro
		mEt_mark = (EditText) findViewById(R.id.ps_et_mark);	// mark
		mEt_is_deleted = (EditText) findViewById(R.id.ps_et_is_deleted);	// is_deleted
		mEt_author = (EditText) findViewById(R.id.ps_et_author);	// author
		mEt_country_id = (EditText) findViewById(R.id.ps_et_country_id);	// country_id
		mEt_name = (EditText) findViewById(R.id.ps_et_name);	// name
		mEt_alpha_2 = (EditText) findViewById(R.id.ps_et_alpha_2);	// alpha_2
		mEt_alpha_3 = (EditText) findViewById(R.id.ps_et_alpha_3);	// alpha_3
		mEt_flag_base64 = (EditText) findViewById(R.id.ps_et_flag_base64);	// flag_base64
		mEt_json = (EditText) findViewById(R.id.ps_et_json);	// json

		mIv_flag = (ImageView) findViewById(R.id.ps_iv_flag);	// json

		// Inactivar edicion de campos PK:
		mEt_country_id.setFocusable(false); mEt_country_id.setEnabled(false);	// country_id

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
		mDBHelper.ps_countries.mRegistro.clean();

		//	DEBE HABERSE LLAMADO: intent.putExtra ( "xx_reg", mDBHelper.xx_aliasEntidad.mRegistro );
		// Lee parametro de entrada para PK a editar:
		com.app.db.PS_countries_DAO.Registro reg = getIntent().getExtras().getParcelable( "ps_reg" );

		if ( reg != null ) {
			// Mover campos desde parametros:
			mDBHelper.ps_countries.mRegistro.copyFrom(reg);	
			// No tiene por que estar completo el registro...solo se esperaba la PK:
			mDBHelper.ps_countries.getRcd(mDBHelper.mDB);
		}
	}

	private void cargarPantalla() {
		Log.i(TAG,"cargarPantalla()");
		mTv_titulo.setText( "Editar país" );
		K.LeftToRight_Animation(mViewLayout,350);

		// Mover campos desde la base de datos:
		mEt_sincro.setText( mDBHelper.ps_countries.mRegistro.sincro );	// sincro
		mEt_mark.setText( mDBHelper.ps_countries.mRegistro.mark );	// mark
		mEt_is_deleted.setText( mDBHelper.ps_countries.mRegistro.is_deleted );	// is_deleted
		mEt_author.setText( mDBHelper.ps_countries.mRegistro.author );	// author
		mEt_country_id.setText( mDBHelper.ps_countries.mRegistro.country_id );	// country_id
		mEt_name.setText( mDBHelper.ps_countries.mRegistro.name );	// name
		mEt_alpha_2.setText( mDBHelper.ps_countries.mRegistro.alpha_2 );	// alpha_2
		mEt_alpha_3.setText( mDBHelper.ps_countries.mRegistro.alpha_3 );	// alpha_3
//		mEt_flag_base64.setText( mDBHelper.ps_countries.mRegistro.flag_base64 );	// flag_base64
		mEt_json.setText( mDBHelper.ps_countries.mRegistro.json );	// json

		mBCK_flag_base64 = mDBHelper.ps_countries.mRegistro.flag_base64;
		mIv_flag.setImageBitmap( Util.decodeB64ToBitmap( mDBHelper.ps_countries.mRegistro.flag_base64 ) );

		// Se autoselecciona contenido del campo con el focus:
		if (mEt_sincro.isFocused()) { mEt_sincro.selectAll(); }	// sincro
		if (mEt_mark.isFocused()) { mEt_mark.selectAll(); }	// mark
		if (mEt_is_deleted.isFocused()) { mEt_is_deleted.selectAll(); }	// is_deleted
		if (mEt_author.isFocused()) { mEt_author.selectAll(); }	// author
		if (mEt_country_id.isFocused()) { mEt_country_id.selectAll(); }	// country_id
		if (mEt_name.isFocused()) { mEt_name.selectAll(); }	// name
		if (mEt_alpha_2.isFocused()) { mEt_alpha_2.selectAll(); }	// alpha_2
		if (mEt_alpha_3.isFocused()) { mEt_alpha_3.selectAll(); }	// alpha_3
		if (mEt_flag_base64.isFocused()) { mEt_flag_base64.selectAll(); }	// flag_base64
		if (mEt_json.isFocused()) { mEt_json.selectAll(); }	// json

	}

	protected boolean pMSG_ACEPTAR( StringBuilder msgs ) {
		boolean resultado = false;
		
		// Mover campos desde la pantalla:
		mDBHelper.ps_countries.mRegistro.clean();
		mDBHelper.ps_countries.mRegistro.sincro = mEt_sincro.getText().toString();	// sincro
		mDBHelper.ps_countries.mRegistro.mark = mEt_mark.getText().toString();	// mark
		mDBHelper.ps_countries.mRegistro.is_deleted = mEt_is_deleted.getText().toString();	// is_deleted
		mDBHelper.ps_countries.mRegistro.author = mEt_author.getText().toString();	// author
		mDBHelper.ps_countries.mRegistro.country_id = mEt_country_id.getText().toString();	// country_id
		mDBHelper.ps_countries.mRegistro.name = mEt_name.getText().toString();	// name
		mDBHelper.ps_countries.mRegistro.alpha_2 = mEt_alpha_2.getText().toString();	// alpha_2
		mDBHelper.ps_countries.mRegistro.alpha_3 = mEt_alpha_3.getText().toString();	// alpha_3
//		mDBHelper.ps_countries.mRegistro.flag_base64 = mEt_flag_base64.getText().toString();	// flag_base64
		mDBHelper.ps_countries.mRegistro.json = mEt_json.getText().toString();	// json

		mDBHelper.ps_countries.mRegistro.flag_base64 = mBCK_flag_base64;	// flag_base64 BCK

		// Validar PK:
		if ( 
			   mDBHelper.ps_countries.mRegistro.country_id == null || mDBHelper.ps_countries.mRegistro.country_id.trim().length() < 1
			) {
			msgs.append("Claves obligatorias.");
		}
		
		// Validar los valores de los campos:
		
		// Validar las relaciones de los campos:
		
		// Grabar:
		if ( 0 == msgs.length() ) {
			msgs.append( Long.toString( 
				mDBHelper.ps_countries.updObj(mDBHelper.mDB)
			) + " registro cambiado." );
			resultado = true;
		}

		return resultado;

	}
	////////////////////

	////////////////////
}
