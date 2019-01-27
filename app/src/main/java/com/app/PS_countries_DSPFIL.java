package com.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.db._DBHelper;
import com.app.util.K;

public class PS_countries_DSPFIL extends AppCompatActivity {

	// Poniendo en el 'manifiest.xml' en esta Activity: android:configChanges="orientation"
	//	se evita que rearranque la activity al rotar la pantalla.
	// ( ..así no cascarán los dlg "espere..." al rotar la pantalla)

	// 2016-07-10: PARA TENER TOOLBAR: se ha cambiado "extends Activity" por "extends AppCompatActivity"

	private static final String TAG = PS_countries_DSPFIL.class.getSimpleName();

	private _DBHelper mDBHelper;
	private Context mCtx;
	private View mViewLayout;
	private ProgressDialog mProgressDialog = null;

	//////////
	// Declarar controles de pantalla
	private TextView		mTv_titulo;
//	private ListView		mLv_lista;
	private GridView 		mGv_lista;
	private ListAdapter		mLstAdpt;

	//////////

	////////////////////
	// Proceso de mensajes del programa. (Sobretodo los de pantalla)
	private final Handler mHandler;

	public PS_countries_DSPFIL() {

		this.mHandler = new Handler() {

			public void handleMessage( final Message msg ) {

				//////////////////////////
				// Por si el mensaje trae par�metro 'uno' --> position de la lista:
				com.app.db.PS_countries_DAO.Registro reg = null;
				if ( msg.arg1 != ListView.INVALID_POSITION ) {
					if ( mLstAdpt!= null && ! mLstAdpt.isEmpty() ) {
						Object o =  mLstAdpt.getItem(msg.arg1);
						if ( "Registro".equals( o.getClass().getSimpleName() ) ) {
							reg = (com.app.db.PS_countries_DAO.Registro)o;
						}
					}
				}
				//////////////////////////

				switch ( msg.what ) {

					case K.MSG_CARGANDO:
						Log.i(TAG,"MSG_CARGANDO()");

						//				new Thread( new Runnable() { public void run() {
						cargarAdapters();
						mHandler.sendEmptyMessage( K.MSG_FIN_ACCION_SOLICITADA );
						//				} } ).start();

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

					case K.MSG_MNU_REFRESCAR:
						Log.i(TAG,"MSG_MNU_REFRESCAR()");
						mHandler.sendEmptyMessage( K.MSG_CARGANDO_ESPERE );
						break;

					case K.MSG_MNU_NUEVO:
						Log.i(TAG,"MSG_MNU_NUEVO()");
						K.dialogoSiNo(mCtx,mHandler,"¿Añadir nuevo?",mHandler.obtainMessage(K.MSG_MNU_NUEVO_OK, msg.arg1, msg.arg2),null);
						break;

					case K.MSG_MNU_EDITAR:
						Log.i(TAG,"MSG_MNU_EDITAR() --> " + msg.arg1);
						K.dialogoSiNo(mCtx,mHandler,"¿Modificar datos?",mHandler.obtainMessage(K.MSG_MNU_EDITAR_OK, msg.arg1, msg.arg2),null);
						break;

					case K.MSG_MNU_SUPRIMIR:
						Log.i(TAG,"MSG_MNU_SUPRIMIR() --> " + msg.arg1);
						K.dialogoSiNo(mCtx,mHandler,"¿Suprimir datos?",mHandler.obtainMessage(K.MSG_MNU_SUPRIMIR_OK, msg.arg1, msg.arg2),null);
						break;

					case K.MSG_MNU_NUEVO_OK:
						Log.i(TAG,"MSG_MNU_NUEVO()");
						startActivity( new Intent(mCtx,PS_countries_ADDRCD.class) );
						break;

					case K.MSG_MNU_EDITAR_OK:
						Log.i(TAG,"MSG_MNU_EDITAR_OK() --> " + msg.arg1);
						if ( reg != null ) {
							Intent intent = new Intent(mCtx,PS_countries_EDTRCD.class);
							intent.putExtra ( "ps_reg", reg );
							startActivity( intent );
						}
						break;

					case K.MSG_MNU_SUPRIMIR_OK:
						Log.i(TAG,"MSG_MNU_SUPRIMIR_OK() --> " + msg.arg1);
						if ( reg != null ) {
							mDBHelper.ps_countries.mRegistro.clean();
							mDBHelper.ps_countries.mRegistro.copyFrom(reg);
							mDBHelper.ps_countries.dltObj(mDBHelper.mDB);
							mHandler.sendEmptyMessage( K.MSG_CARGANDO_ESPERE );
						}
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
		//setContentView(R.layout.ps_list);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
//		mViewLayout = inflater.inflate(R.layout.ps_list, null, false);
		mViewLayout = inflater.inflate(R.layout.ps_list_grid, null, false);
		setContentView(mViewLayout);

		mCtx = this;
		if (mDBHelper==null) mDBHelper = new _DBHelper(this);

		// Liberar...
		mTv_titulo = null;
//		mLv_lista = null;
		// Instanciar controles de pantalla
		mTv_titulo = (TextView) findViewById(R.id.ps_tv_titulo);
//		mLv_lista = (ListView) findViewById(R.id.ps_lv_lista);
		mGv_lista = (GridView) findViewById(R.id.ps_gv_lista);

//		registerForContextMenu(mLv_lista);
		registerForContextMenu(mGv_lista);

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
			if ( K.IS_PANTALLAS_NO_ATRAS ) {
				Log.w(TAG,"NAVEGAR ATRAS NO PERMITIDO.");
				Toast.makeText(mCtx, "Navegar atrás no permitido", Toast.LENGTH_SHORT).show();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater( this ).inflate( R.menu.ps_menu, menu );
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
// 2016-07-10: PARA TENER TOOLBAR: se ha cambiado "extends Activity" por "extends AppCompatActivity"
//	@Override
//	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch ( item.getItemId() ) {

			case R.id.ps_mnu_refrescar: mHandler.sendEmptyMessage( K.MSG_MNU_REFRESCAR ); return true;

			case R.id.ps_mnu_nuevo: mHandler.sendEmptyMessage( K.MSG_MNU_NUEVO ); return true;

			default: break;

		}

// 2016-07-10: PARA TENER TOOLBAR: se ha cambiado "extends Activity" por "extends AppCompatActivity"
//		return super.onMenuItemSelected(featureId, item);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle("Seleccione opción");
		menu.setHeaderIcon(android.R.drawable.ic_menu_preferences);
		new MenuInflater( this ).inflate( R.menu.ps_menu_ctx, menu);

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

	    switch (item.getItemId()) {

			case R.id.ps_mnu_editar:
//				mHandler.sendMessage( Message.obtain(mHandler, K.MSG_MNU_EDITAR, info.position, 0 ) );
				mHandler.sendMessage( Message.obtain(mHandler, K.MSG_MNU_EDITAR_OK, info.position, 0 ) );
				return true;

			case R.id.ps_mnu_suprimir:
				mHandler.sendMessage( Message.obtain(mHandler, K.MSG_MNU_SUPRIMIR, info.position, 0 ) );
				return true;

			default: break;

	    }

	    return super.onContextItemSelected(item);

	}

	////////////////////
	// Funciones internas

	private void cargarAdapters() {
		Log.i(TAG,"cargarAdapters()"); if ( ! mDBHelper.mDB.isOpen() ) { Log.w(TAG,"OPEN DB (...estaba cerrada!!)"); mDBHelper.mDB = mDBHelper.getWritableDatabase(); }

		// Inicializar filtro metiendo valores en 'DAO.mRegistro'
		mDBHelper.ps_countries.mRegistro.clean();

		// Recuperar en 'DAO.mRegistros' los registros que cumplan el filtro en 'DAO.mRegistro'
		mLstAdpt = null;
		mLstAdpt = mDBHelper.ps_countries.getAdapter(mDBHelper.mDB, mCtx, mHandler);
		if ( mDBHelper.ps_countries.mRegistros.isEmpty() ) {
			mLstAdpt = null;
			String[] lstTmp = {K.NODATA};
			mLstAdpt = new ArrayAdapter<String>(mCtx, android.R.layout.simple_list_item_1,lstTmp);
		}

	}

	private void cargarPantalla() {
		Log.i(TAG,"cargarPantalla()");

		mTv_titulo.setText( "Países" );

//		if ( mLstAdpt != null) mLv_lista.setAdapter(mLstAdpt);
		if ( mLstAdpt != null) mGv_lista.setAdapter(mLstAdpt);

//		K.LeftToRight_Animation(mLv_lista,500);
		K.LeftToRight_Animation(mGv_lista,500);
		K.LeftToRight_Animation(mViewLayout,350);

	}
	////////////////////

	////////////////////
}
