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

public class TF_phone_EDTRCD extends Activity {

	// Poniendo en el 'manifiest.xml' en esta Activity: android:configChanges="orientation"
	//	se evita que rearranque la activity al rotar la pantalla.
	// ( ..asi no cascaran los dlg "espere..." al rotar la pantalla) 

	private static final String TAG = TF_phone_EDTRCD.class.getSimpleName(); 

	private _DBHelper mDBHelper;
	private Context mCtx;
	private View mViewLayout;
	private ProgressDialog mProgressDialog = null;

	//////////
	// Declarar controles de pantalla
	private TextView mTv_titulo;
	// Fmt.RTV:
	public EditText mEt_id;	// id
	public EditText mEt_name;	// name
	public EditText mEt_json;	// json

	//////////

	////////////////////
	// Proceso de mensajes del programa. (Sobretodo los de pantalla)
	private final Handler mHandler;

	public TF_phone_EDTRCD() {

		this.mHandler = new Handler() {

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
	}

	////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"onCreate()");
		super.onCreate(savedInstanceState);
		
		//setContentView(R.layout.tf_item);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE ); 
		mViewLayout = inflater.inflate(R.layout.tf_item, null, false);
		setContentView(mViewLayout);

		mCtx = this;
		if (mDBHelper==null) mDBHelper = new _DBHelper(this);

		// Liberar...
		mTv_titulo = null;
		mEt_id = null;	// id
		mEt_name = null;	// name
		mEt_json = null;	// json

		// Instanciar controles de pantalla
		mTv_titulo = (TextView) findViewById(R.id.tf_tv_titulo);
		mEt_id = (EditText) findViewById(R.id.tf_et_id);	// id
		mEt_name = (EditText) findViewById(R.id.tf_et_name);	// name
		mEt_json = (EditText) findViewById(R.id.tf_et_json);	// json

		// Inactivar edicion de campos PK:
		mEt_id.setFocusable(false); mEt_id.setEnabled(false);	// id

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
		mDBHelper.tf_phone.mRegistro.clean();

		//	DEBE HABERSE LLAMADO: intent.putExtra ( "xx_reg", mDBHelper.xx_aliasEntidad.mRegistro );
		// Lee parametro de entrada para PK a editar:
		com.app.db.TF_phone_DAO.Registro reg = getIntent().getExtras().getParcelable( "tf_reg" );

		if ( reg != null ) {
			// Mover campos desde parametros:
			mDBHelper.tf_phone.mRegistro.copyFrom(reg);	
			// No tiene por que estar completo el registro...solo se esperaba la PK:
			mDBHelper.tf_phone.getRcd(mDBHelper.mDB);
		}
	}

	private void cargarPantalla() {
		Log.i(TAG,"cargarPantalla()");
		mTv_titulo.setText( "Editar phone" );
		K.LeftToRight_Animation(mViewLayout,350);

		// Mover campos desde la base de datos:
		mEt_id.setText( mDBHelper.tf_phone.mRegistro.id );	// id
		mEt_name.setText( mDBHelper.tf_phone.mRegistro.name );	// name
		mEt_json.setText( mDBHelper.tf_phone.mRegistro.json );	// json
		
		// Se autoselecciona contenido del campo con el focus:
		if (mEt_id.isFocused()) { mEt_id.selectAll(); }	// id
		if (mEt_name.isFocused()) { mEt_name.selectAll(); }	// name
		if (mEt_json.isFocused()) { mEt_json.selectAll(); }	// json

	}

	protected boolean pMSG_ACEPTAR( StringBuilder msgs ) {
		boolean resultado = false;
		
		// Mover campos desde la pantalla:
		mDBHelper.tf_phone.mRegistro.clean();
		mDBHelper.tf_phone.mRegistro.id = mEt_id.getText().toString();	// id
		mDBHelper.tf_phone.mRegistro.name = mEt_name.getText().toString();	// name
		mDBHelper.tf_phone.mRegistro.json = mEt_json.getText().toString();	// json
		
		// Validar PK:
		if ( 
			   mDBHelper.tf_phone.mRegistro.id == null || mDBHelper.tf_phone.mRegistro.id.trim().length() < 1
			) {
			msgs.append("Claves obligatorias.");
		}
		
		// Validar los valores de los campos:
		
		// Validar las relaciones de los campos:
		
		// Grabar:
		if ( 0 == msgs.length() ) {
			msgs.append( Long.toString( 
				mDBHelper.tf_phone.updObj(mDBHelper.mDB)
			) + " registro cambiado." );
			resultado = true;
		}

		return resultado;

	}
	////////////////////

	////////////////////
}
