package com.atm.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.atm.util.HttpRestClient;
import com.atm.adapter.TF_Adapter;

public class TF_phone_DAO {
	private static final String TAG = TF_phone_DAO.class.getSimpleName(); 

	public static final String DATABASE_TABLE 	= "TF_phone";
	private static final String SERVLET_ENTIDAD = "tf_servlet";
	
	public String mDirectorioDatos;
	public String mNombreCompletoFichero;

	public Registro 			mRegistro;
	public ArrayList<Registro> 	mRegistros;
	public Cursor 				mCursor;

	private TF_Adapter mAdpt;

	// Resultados de operaciones http:
	public int 		mHttp_RC;		
	public String 	mHttp_ReasonPhrase;

	public TF_phone_DAO() {
		mRegistro  = new Registro();
		mRegistros = new ArrayList<Registro>();
		mDirectorioDatos = Environment.getExternalStorageDirectory().toString() + "/eed/";
		mNombreCompletoFichero = mDirectorioDatos + SERVLET_ENTIDAD + ".txt";
	}

	public static class Registro extends Object implements Parcelable {

		public Registro() {clean();}

		public static final Parcelable.Creator<Registro> CREATOR = 
			new Parcelable.Creator<Registro>() {
			public Registro createFromParcel(Parcel in) {
				return new Registro(in);
			}

			public Registro[] newArray(int size) {
				return new Registro[size];
			}
		};

		private Registro(Parcel in) {
			clean();
			String[] reg = new String[mColNombres.length]; 
			in.readStringArray(reg);
			if ( reg != null && reg.length > 0 ) {
				for ( int i=0; i<mColNombres.length; i++ ) {
					this.setVal(i,reg[i]);
				}
			}
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			String[] reg = new String[mColNombres.length]; 
			for ( int i=0; i<mColNombres.length; i++ ) {
				reg[i] = this.getVal(i);
			}
			dest.writeStringArray(reg);
		}

		@Override
		public int describeContents() {return 0;}

		@Override
		public String toString() {
			// Fmt.KEY:
			return
			id	// id
			;
			// return super.toString();
		}

		// Fmt.RTV:
		public String id; // id
		public String name; // name
		public String json; // json
	
		
		public void clean() {
			for (int i = 0; i < mColNombres.length; i++) {
				setVal(i, "");
			}
		}

		public void copyFrom( Registro origen ) {
			for ( int i=0; i<mColNombres.length; i++ ) {
				setVal( i, origen.getVal(i) );
			}
		}
		
		public int getIdx( String nombreCampo) {
			for(int i=0;i<mColNombres.length; i++) {
				if ( mColNombres[i].trim().equalsIgnoreCase( nombreCampo ) ) {
					return i;
				}
			}
			return -1;
		}

		public String getVal(int i) {

			// Para poder usar SIEMPRE el tipo "text" en la aplicación,
			// se aprovechan las "Rule Used To Determine Affinity".
			// Así un "text" de entrada/salida lo traduce al tipo definido en la creación.
			// Ver "http://www.sqlite.org/datatype3.html"

			switch (i) {
				case 0 : return id; // id
				case 1 : return name; // name
				case 2 : return json; // json
			}
			return null;
		}

		public void setVal(int i, String newVal) {

			// Para poder usar SIEMPRE el tipo "text" en la aplicación,
			// me aprovecho de las "Rule Used To Determine Affinity".
			// Así un "text" de entrada/salida lo traduce al tipo definido en la
			// creación.
			// Ver "http://www.sqlite.org/datatype3.html"

			switch (i) {
				case 0 : id = newVal; break;  // id
				case 1 : name = newVal; break;  // name
				case 2 : json = newVal; break;  // json
			}
			return;
		}
	}

	public static final String[] mColNombres = {
			 "id"	// id
			,"name"	// name
			,"json"	// json
	};
	public static final String[] mColTipos   = {
			 "numeric"	// id
			,"text"	// name
			,"text"	// json
	};
	public static final String[] mColPrimary = {
			 "id"	// id
	};

	// //////////////////////////////////////
	// Existencia:
	public void dropTb(SQLiteDatabase db) {
		String sqlCmd = "DROP TABLE IF EXISTS " + DATABASE_TABLE;
		try {
			db.execSQL(sqlCmd);
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	public void create(SQLiteDatabase db) {

		TF_phone_DAO oDAO = new TF_phone_DAO();

		String sqlCmd = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " ( ";
		for (int i = 0; i < oDAO.mColNombres.length; i++) {
			sqlCmd += (i == 0) ? "" : ", ";
			sqlCmd += oDAO.mColNombres[i] + " " + oDAO.mColTipos[i];
		}
		sqlCmd += ", PRIMARY KEY ( ";
		for (int i = 0; i < oDAO.mColPrimary.length; i++) {
			sqlCmd += (i == 0) ? "" : ", ";
			sqlCmd += oDAO.mColPrimary[i];
		}
		sqlCmd += " ) ON CONFLICT ABORT )";
		try {
			db.execSQL(sqlCmd);
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	// //////////////////////////////////////
	// PARA CONEXIÓN A LA PRESENTACIÓN:
	public TF_Adapter getAdapter( SQLiteDatabase db, Context ctx, Handler handler ) {
		// Cargar los datos:
		getSeq(db);
		// Adaptar su presentación:
		if (mAdpt != null) mAdpt = null;
		mAdpt = new TF_Adapter(ctx, mRegistros, handler);
		System.gc();
		///////////////////////////////////
		return mAdpt;
	}
	
	// //////////////////////////////////////
	// Sincronización con servidor:
	public boolean enviar( SQLiteDatabase db ) {
		// REALMENTE ESTE METODO NO EMITE INFORMACión, SOLO LA PERSISTE EN LOCAL.
		// Ejecuta getSeq() cargando 'mRegistros' y esos regs. se persisten en un fichero formateado.
		// Como siempre antes de un getSeq(), los valores del filtro deben estar en 'mRegistro'. 
		
//		////////////////////////////////////
//		// ENVIAR DATOS EN UN PAQUETE ZIP
//      // (Este código sirve para copiarse, pegarse y personalizarse en la clase que realice el proceso)
//      // Da por supuesto que existe un objeto 'mDBHelper'.
//		////////////////////////////////////
//		// Ejemplo de uso en la aplicación:
//		// Paso 1 . Para cada entidad a enviar en el paquete:
//		//          se ejecuta su método 'enviar()' (generandose un fichero local)
//		//          y se anota su nombre en una lista.
//		// Paso 2 . A partir de la lista de nombres, se empaquetan los ficheros en un zip y se emiten al servidor.
//		////////////////////////////////////
//
//		// EJEMPLO de código para copiar y pegar donde corresponda:
//		
//		final String nomFicheroZIP = mDirectorioDatos + "snd_ejemplo.zip";
//		final ArrayList<String> mListaErrores  = new ArrayList<String>();
//		final ArrayList<String> listaFicheros  = new ArrayList<String>();
//
//		/////////////////////////////
//		
//		// Paso 1
//		// Para cada entidad, se ejecuta su método 'enviar()' y se anota su nombre en una lista.
//		
//		// Generar fichero local de xx_uno y anadir su nombre a la lista para luego poder incorporarlo al ZIP:
//		mDBHelper.xx_uno.mRegistro.clean();
//		// Poner datos de filtro en 'mRegistro' aqui...
//		if ( mDBHelper.xx_uno.enviar( mDBHelper.mDB ) ) {  
//			listaFicheros.add(mDBHelper.xx_uno.mNombreCompletoFichero);
//		} else { mListaErrores.add(" Error al generar fichero: " + mDBHelper.xx_uno.mNombreCompletoFichero); }
//		
//		// Generar fichero local de xx_dos y anadir su nombre a la lista para luego poder incorporarlo al ZIP:
//		mDBHelper.xx_dos.mRegistro.clean();
//		// Poner datos de filtro en 'mRegistro' aqui...
//		if ( mDBHelper.xx_dos.enviar( mDBHelper.mDB ) ) {  
//			listaFicheros.add(mDBHelper.xx_dos.mNombreCompletoFichero);
//		} else { mListaErrores.add(" Error al generar fichero: " + mDBHelper.xx_dos.mNombreCompletoFichero); }
//
//		/////////////////////////////
//		
//		// Paso 2...si la lista de errores 'mListaErrores' esta vacía...
//		// A partir de la lista de nombres se empaquetan los ficheros en un zip y se emiten al servidor.
//
//		int num = 0;
//    	if ( listaFicheros.size() > 0 ) {
//
//    		String[] fileNames = new String[listaFicheros.size()];
//    		listaFicheros.toArray( fileNames );
//
//    		num = Util.ZIP_addFiles( fileNames, nomFicheroZIP );
//    		if ( num > 0 ) {
//				byte[] contenido = Util.readFileBin( nomFicheroZIP );    
//				if ( contenido != null && contenido.length > 0 ) {
//					
//					Util util = new Util();
//					String resp = 
//						util.emitirResultados_sndHttp(
//								"http://laUrl/",	// Por ejemplo: "http://eos.casbega.net/TMServer/"
//								"elServlet", 		// Por ejemplo: "atmServlet"
//								nomFicheroZIP,
//							Base64.encodeToString( contenido, Base64.DEFAULT  )
//							);
//					mHttp_RC = util.mHttp_RC;
//					mHttp_ReasonPhrase = util.mHttp_ReasonPhrase;
//					
//					resp = resp == null ? "" : resp;
//
//					// Tratamiento de la respuesta del servidor en 'resp'...
//
//				}
//    		}
//    	}
//		///////////////////////////
		
		StringBuilder sb = new StringBuilder();
		String sep = "\t";
		String eol = "\r\n";

		getSeq(db);

		if ( mRegistros.size() > 0 ) {
			for (int i=0;i<mRegistros.size(); i++) {
				mRegistro.copyFrom( mRegistros.get(i) );
				////////////////
				boolean primeraVez = true;
				for (int j = 0; j < mColNombres.length; j++) {
					if (!primeraVez) { sb.append(sep); }
					sb.append( mRegistro.getVal(j) );
					primeraVez = false;
				}
				sb.append( " " + eol );	// Con tap�n " "...
				////////////////
			}
		}

		/////////////////////////////////
		// 1. Generar los datos (NETOS) en un fichero en SDCARD.
		/////////////////////////////////
		
		new File( mDirectorioDatos ).mkdirs();	// Los crea si no existen...
		File file = new File( mNombreCompletoFichero );
		file.delete();
		return recibirDatos_Persistir(file,sb.toString());
	}

	public int recibir( SQLiteDatabase db, String urlServer, boolean integrar ) {
		int nRegistros = 0;
		// Los parametros deben estar en 'mRegistro'. Los campos no usados deben estar vac�os.
		// Ejemplo datos:  EMP=02 DEL=11 FPV=1111026
		String url_servlet = urlServer + "/" + SERVLET_ENTIDAD; // "http://181.75.100.28:8080/TMServer/tf_servlet";

		new File( mDirectorioDatos ).mkdirs();	// Los crea si no existen...
		File file = new File( mNombreCompletoFichero );
		file.delete();

		/////////////////////////////////
		// 1. Descargar los datos desde la central mediante un HTTP SERVLET.
		// 2. Persistir la descarga en un fichero en SDCARD.
		// 3. Parse del fichero descargado e inserción en la BD.
		/////////////////////////////////

		String datos = recibirDatos_Descargar( url_servlet );

		if ( datos != null && datos.trim().length() > 0 ) {
			datos = datos.trim();
			recibirDatos_Persistir( file, datos );
			if (integrar) nRegistros = recibirDatos_Integrar( db, datos );
		}
		return nRegistros;
	}

	public String recibirDatos_Descargar ( String url_servlet ) {
		String resultado = null;
		/////////////////////////////////
		// Descargar los datos desde la central mediante un HTTP SERVLET.
		/////////////////////////////////
		HttpRestClient clienteHttp = new HttpRestClient( url_servlet );
		// Parametros del servlet:
		Map<String,Object> nameValuePairs = new HashMap<String,Object>();
		// Se propone la KEY como parametros cargados en mRegistro (debe revisarse para cada caso...)
		nameValuePairs.put("ID", mRegistro.id);	// id

		resultado = clienteHttp.postData(nameValuePairs, 5000);

		/////////////////////////////////
		mHttp_RC = clienteHttp.mHttp_RC;
		mHttp_ReasonPhrase = clienteHttp.mHttp_ReasonPhrase;
		/////////////////////////////////
		return resultado;
	}

	public boolean recibirDatos_Persistir ( File file, String datos ) {
		boolean resultado = false;
		/////////////////////////////////
		// Persistir la descarga en un fichero en SdCard.
		/////////////////////////////////
		if ( datos != null && datos.trim().length() > 0 ) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream( file );
				fos.write( datos.getBytes() );
				resultado = true;
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			} finally {
				if (fos!=null) try { fos.close(); } catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
		/////////////////////////////////
		return resultado;
	}

	public int recibirDatos_Integrar( SQLiteDatabase db, String datos ) {
		int nRegistros = 0;
		/////////////////////////////////
		String[] registros = datos.split("\r\n");
		if ( registros != null ) {
			for (String registro : registros) {
				String[] valores = registro.split("\t");
				if ( valores != null && valores.length == mColNombres.length ) {
					
					Registro tmpReg = new Registro();
					
					tmpReg.clean();
					// El orden de los campos en el registro DEBE SER el MISMO que en la colección interna !!!
					for ( int j=0; j < valores.length; j++ ) {
						tmpReg.setVal(j, (valores[j]!=null)?valores[j].trim():"" );
					}
					//crtObj( db );
					mRegistros.add( tmpReg );
					nRegistros++;
				}
			}
			// Graba 'mRegistros'
			crtObj_multiple( db );
			mRegistros.clear();
		}
		/////////////////////////////////
		Log.i(TAG,"Regs.grabados: "+nRegistros);
		return nRegistros;
	}

	// ////////////////////////////////////////////////////
	// CRUD
	public long crtObj_multiple(SQLiteDatabase db) {
		// Los datos RCD deben estar en el miembro 'mRegistros'
		// (Internamente utilizara 'mRegistro')
		long res = 0;

		/////////////////////////////////////
		setVirtuals(db);
		/////////////////////////////////////

		ContentValues initialValues = new ContentValues();

		db.beginTransaction();
		try {
			
			for (int i=0; i<mRegistros.size(); i++) {
				mRegistro.copyFrom( mRegistros.get(i) );
				initialValues.clear();
				for (int j = 0; j < mColNombres.length; j++) {
					initialValues.put(mColNombres[j], mRegistro.getVal(j));
				}
				// Returns: the row ID of the newly inserted row
				// OR the primary key of the existing row if the input param
				// 'conflictAlgorithm' = CONFLICT_IGNORE
				// OR -1 if any error
				res = db.insertWithOnConflict(
						DATABASE_TABLE, 
						null, 
						initialValues,
						SQLiteDatabase.CONFLICT_IGNORE
						);
			}
			
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG,e.getMessage());
		} finally {
			db.endTransaction();
		}
		return res;
	}

	public long crtObj(SQLiteDatabase db) {
		// Los datos RCD deben estar en el miembro 'mRegistro'
		long res = 0;

		/////////////////////////////////////
		setVirtuals(db);
		/////////////////////////////////////

		ContentValues initialValues = new ContentValues();
		for (int i = 0; i < mColNombres.length; i++) {
			initialValues.put(mColNombres[i], mRegistro.getVal(i));
		}

		db.beginTransaction();
		try {
			// Returns: the row ID of the newly inserted row
			// OR the primary key of the existing row if the input param
			// 'conflictAlgorithm' = CONFLICT_IGNORE
			// OR -1 if any error
			res = db.insertWithOnConflict(
					DATABASE_TABLE, 
					null, 
					initialValues,
					SQLiteDatabase.CONFLICT_IGNORE
					);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG,e.getMessage());
		} finally {
			db.endTransaction();
		}
		return res;
	}
	
	public long updObj(SQLiteDatabase db) {
		// Los datos RCD deben estar en el miembro 'mRegistro'
		long res = 0;

		/////////////////////////////////////
		setVirtuals(db);
		/////////////////////////////////////

		ContentValues values = new ContentValues();
		for (int i = 0; i < mColNombres.length; i++) {
			values.put(mColNombres[i], mRegistro.getVal(i));
		}

		// Primary key:
		String whereClause = "";
		int idx = -1;
		for (int i = 0; i < mColPrimary.length; i++) {
			idx = mRegistro.getIdx( mColPrimary[i].trim() );
			if ( idx > -1 ) {
				whereClause += (i == 0) ? "" : " and ";
				whereClause += mColPrimary[i];
				whereClause += "=";
				// Ver nota en getRcd()...
//				if ( mColTipos[idx].trim().equalsIgnoreCase("text") ) {
					whereClause += "'" + mRegistro.getVal( idx ) + "'";
//				} else {
//					whereClause += mRegistro.getVal( idx );
//				}
			}
		}
		
		db.beginTransaction();
		try {
			// Returns: the row ID of the newly inserted row
			// OR the primary key of the existing row if the input param
			// 'conflictAlgorithm' = CONFLICT_IGNORE
			// OR -1 if any error
			res = db.updateWithOnConflict(
					DATABASE_TABLE, 
					values, 
					whereClause, 
					null, 
					SQLiteDatabase.CONFLICT_IGNORE
					);

			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG,e.getMessage());
		} finally {
			db.endTransaction();
		}
		return res;
	}

	public long dltObj(SQLiteDatabase db) {
		// Los datos KEY deben estar en el miembro 'mRegistro'
		long res = 0;

		// Primary key:
		String whereClause = "";
		int idx = -1;
		for (int i = 0; i < mColPrimary.length; i++) {
			idx = mRegistro.getIdx( mColPrimary[i].trim() );
			if ( idx > -1 ) {
				whereClause += (i == 0) ? "" : " and ";
				whereClause += mColPrimary[i];
				whereClause += "=";
				// Ver nota en getRcd()...
//				if ( mColTipos[idx].trim().equalsIgnoreCase("text") ) {
					whereClause += "'" + mRegistro.getVal( idx ) + "'";
//				} else {
//					whereClause += mRegistro.getVal( idx );
//				}
			}
		}

		db.beginTransaction();
		try {
			// Returns: the number of rows affected if a whereClause is passed
			// in, 0 otherwise.
			// To remove all rows and get a count pass "1" as the whereClause.
			res = db.delete(
					DATABASE_TABLE, 
					whereClause, 
					null
					);
			db.setTransactionSuccessful(); // implies commit at endTransaction
		} catch (SQLException e) {
			Log.e(TAG,e.getMessage());
		} finally {
			db.endTransaction();
		}
		return res;
	}

	public long dltAll(SQLiteDatabase db) {
		long res = 0;
		db.beginTransaction();
		try {
			// Returns: the number of rows affected if a whereClause is passed in, 0 otherwise.
			// To remove all rows and get a count pass "1" as the whereClause.
			res = db.delete(DATABASE_TABLE, null, null);
			db.setTransactionSuccessful(); // implies commit at endTransaction
		} catch (SQLException e) {
			Log.e(TAG,e.getMessage());
		} finally {
			db.endTransaction();
		}
		return res;
	}

	public TF_phone_DAO getRcd(SQLiteDatabase db) {
		// Los datos KEY deben estar en el miembro 'mRegistro'
		TF_phone_DAO res = null;

		String whereClause = "";
		// Primary key:
		int idx = -1;
		for (int i = 0; i < mColPrimary.length; i++) {
			idx = mRegistro.getIdx( mColPrimary[i].trim() );
			if ( idx > -1 ) {
				whereClause += (i == 0) ? "" : " and ";
				whereClause += mColPrimary[i];
				whereClause += "=";
// Da problemas cuando en un campo num�rico hay caracteres...como no es un n�mero cree que el valor es un nombre de campo en vez de un literal.
				// Siempre funciona metiendo comillas (..siempre?)
//				if ( mColTipos[idx].trim().equalsIgnoreCase("text") ) {
					whereClause += "'" + mRegistro.getVal( idx ) + "'";
//				} else {
//					whereClause += mRegistro.getVal( idx );
//				}
			}
		}

		mCursor = db.query(
				DATABASE_TABLE, 
				mColNombres, 
				whereClause, 
				null, null, null, null);

		mRegistro.clean();
		if (mCursor != null) {
			if (mCursor.moveToFirst()) {
				for (int i = 0; i < mColNombres.length; i++) {
					String v = mCursor.getString(i);
					mRegistro.setVal(i, v);
				}
				res = this;
			}
			mCursor.close();
		}

		return res; // Puede ser null!
	}

	public TF_phone_DAO getSeq(SQLiteDatabase db) {
		mRegistros.clear();

		// Los datos de FILTRO deben estar en el miembro 'mRegistro'
		String selection = "";
		String v = null;
		String t = null;
		String n = null;
		for (int i = 0; i < mColNombres.length; i++) {
			n = mColNombres[i];
			t = mColTipos[i];
			v = mRegistro.getVal(i);
			if ( v != null && v.trim().length() > 0 ) {
				selection += (selection.trim().length()<1)?" ":" and ";
				selection += n + "=";
				// Ver nota en getRcd()...
//				if ( "text".equalsIgnoreCase( t ) ) {
					selection += "'";
					selection += v.trim();
					selection += "'";
//				} else  {
//					selection += v;
//				}
			}
		}

		// Normalmente es la Primary Key, pero no necesariamente:
		String orderBy = 
			  " id ASC"	// id
			;

		mCursor = db.query(DATABASE_TABLE, mColNombres, selection, null, null, null, orderBy);

		TF_phone_DAO.Registro newReg = null;

		if (mCursor != null) {
			if (mCursor.moveToFirst()) {
				do {
					newReg = new Registro();
					for (int i = 0; i < mColNombres.length; i++) {
						newReg.setVal(i, mCursor.getString(i));
					}
					mRegistros.add(newReg);
				} while (mCursor.moveToNext());
			}
			mCursor.close();
		}

		return this;
	}

	private void setVirtuals(SQLiteDatabase db) {

///////////////////////////////////////
// Ejemplo: Nombre y bandera del país (PS):
//		mRegistro.PS_name = "";
//		mRegistro.PS_flag_base64 = "";
//		if ( mRegistro.country_id != null && mRegistro.country_id.trim().length() > 0 ) {
//			PS_countries_DAO ps_dao = new PS_countries_DAO();
//			ps_dao.mRegistro.country_id = mRegistro.country_id;
//			if ( ps_dao.getRcd(db) != null ) {
//				mRegistro.PS_name = ps_dao.mRegistro.name;
//				mRegistro.PS_flag_base64 = ps_dao.mRegistro.flag_base64;
//			}
//		}
///////////////////////////////////////

	}


	// //////////////////////////////////////
	public long getRegCount(SQLiteDatabase db) {
		long nRegs = -1;
		String sqlCmd = "select COUNT(*) nRegs from " + DATABASE_TABLE;

		try {
			mCursor = db.rawQuery(sqlCmd, null);
			// nRegs = entidad.mCursor.getCount();
			if (mCursor != null) {
				if (mCursor.moveToFirst()) {
					nRegs = mCursor.getLong(0);
				}
			}
		} catch (Exception e) {
			Log.e(TAG,e.getMessage());
		} finally {
			if (mCursor != null) {
				mCursor.close();
			}
		}
		
		return nRegs;
	}
	// ////////////////////////////////////////////////////
}
