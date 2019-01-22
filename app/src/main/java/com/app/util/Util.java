package com.app.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class Util { 
	
	private static final String TAG = Util.class.getSimpleName();

	public static String KEY_Bat_level			= "BatLvl";	// Estado batería LEVEL
	public static String KEY_Bat_scale			= "BatScl";	// Estado batería SCALE

	//////////////////////
	public static int ZIP_addFiles( final String[] fileNames,  final String nomCompletoArchivoZipDestino ) {
		int resultado = 0;
		Log.i(TAG,"ZIP_addFiles( " + nomCompletoArchivoZipDestino + " ) >>>>>>>>>>>>>>");
		
		if ( fileNames == null || fileNames.length < 1 ) return resultado;

		try {
			File file = new File( nomCompletoArchivoZipDestino );
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			ZipOutputStream  zos = new ZipOutputStream(fos);
			try {
				for (int i = 0; i < fileNames.length; ++i) {
					byte[] bytes = readFileBin( fileNames[i] );
					try {
						Log.i(TAG,"ZIP_addFiles() ADD '" + fileNames[i] + "'...");
						zos.putNextEntry( new ZipEntry( fileNames[i] ) );
						zos.write(bytes);
						++resultado;
					} catch (IOException e) {
						Log.e(TAG,e.getMessage());
					} finally {
						zos.closeEntry();
						bytes = null;
					}
				}
			} catch (IOException e) {
				Log.e(TAG,e.getMessage());
			} finally {
				zos.close();
			}
		} catch (IOException e) {
			Log.e(TAG,e.getMessage());
		}
		Log.i(TAG,"ZIP_addFiles( " + nomCompletoArchivoZipDestino + " ) <<<<<<<<<<<<<<");
		return resultado;
	}
	public static int ZIP_extraerConFiltro( final String nomZipArchivo, final String dirDestino, final String filtroDeNombres_patron) {
		int numExtraidos = 0;
		String patron = (filtroDeNombres_patron==null)?"":filtroDeNombres_patron.trim();
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ZipEntry ze = null;
		File zipFile = new File( nomZipArchivo );
		//////////////////////////////////////
		if ( zipFile.exists() && zipFile.canRead() ) {
			try {
				fis = new FileInputStream( zipFile );
				zis = new ZipInputStream( new BufferedInputStream(fis) );
				try {
					try {
						while ((ze = zis.getNextEntry()) != null) {

							String filename = ze.getName();
							
							// Elimina el path del nombre del archivo
							int idx = filename.lastIndexOf("/");
							if (idx>-1) { filename = filename.substring(++idx); }

							if ( !ze.isDirectory() && filename.indexOf( patron ) > -1 ) {
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								byte[] buffer = new byte[65536];
								int count;
								while ( (count = zis.read(buffer)) != -1 ) {
									baos.write(buffer, 0, count);
								}
								//////////////////////////////////////////
								if ( grabFile( dirDestino + filename, baos.toByteArray() ) )
									numExtraidos++;
								//////////////////////////////////////////
							}

						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} finally {
					try {
						if ( zis != null ) zis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		//////////////////////////////////////
		return numExtraidos;
	}
	public static boolean grabFile( final String nomDestinoCompleto, final byte[] contenido ) {
		boolean resultado = false;

		String nomCamino = nomDestinoCompleto;
		int idx = nomCamino.lastIndexOf('/');
		if (idx > -1 ) nomCamino = nomCamino.substring( 0, idx );

		crtDir(nomCamino);

		FileOutputStream fos = null;
		try {
			File file = new File( nomDestinoCompleto );
			file.delete();
			file.createNewFile();
			fos = new FileOutputStream(file);
			fos.write( contenido );
			resultado = true;
		} catch (FileNotFoundException e) {
			Log.i("grabFile",e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.i("grabFile",e.getMessage());
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					Log.i("grabFile",e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return resultado;
	}
	public static String  readFile( String nombreFichero ) {
		String contenido = "";
		try {
			byte[] buff = readFileBin( nombreFichero );
			if ( buff != null ) {
				contenido = new String( buff , "ISO-8859-1");
			}
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,e.getMessage());
		}
		return contenido;
	}
	public static byte[]  readFileBin( String nombreFichero ) {
		
		byte[] bytes = null;
		
		File fichero = new File ( nombreFichero );
		int lenFic = (int) fichero.length();
		bytes = new byte[ lenFic ];

		DataInputStream disr = null;
		try {
			disr = new DataInputStream( new FileInputStream(fichero) );
			disr.readFully(bytes, 0, lenFic);
		} catch (IOException e) {
			Log.e(TAG,e.getMessage());
		}
		
		return bytes;
	}
	public static boolean crtDir( final String nomDirectorio ) {
		boolean resultado = false;
		//////////////////////////////////
		File dir = new File( nomDirectorio );
		if ( !dir.exists() ) dir.mkdirs();
		if ( dir.exists() && dir.canWrite() ) resultado = true;
		//////////////////////////////////
		return resultado;
	}
	//////////////////////

	//////////////////////
	public int mHttp_RC; public String mHttp_ReasonPhrase;
	public String emitirResultados_sndHttp(String urlServer, String servlet, String nomFichero, String cuerpo) {
		Log.i(TAG,"emitirResultados_sndHttp()");
		
		String resultado = null;
		/////////////////////////////////
		// Enviar los datos a la central mediante HTTP (SERVLET).
		/////////////////////////////////
		HttpRestClient clienteHttp = new HttpRestClient( urlServer + servlet );

		// Parametros del servlet:
		Map<String,Object> nameValuePairs = new HashMap<String,Object>();
		nameValuePairs.put("ACC", "UPLOAD"); // Accion del servlet
		nameValuePairs.put("SND", nomFichero);
		nameValuePairs.put("ZIP", cuerpo);

		// Llamada
		byte[] resultadoBin = clienteHttp.postDataBin(nameValuePairs, 5000);
		mHttp_RC = clienteHttp.mHttp_RC;
		mHttp_ReasonPhrase = clienteHttp.mHttp_ReasonPhrase;

		if ( resultadoBin != null ) {
			try {
				resultado = new String(resultadoBin, "ISO-8859-1");		// A capon !!
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, e.getMessage());
			}
		}

		return resultado;
	}
	//////////////////////

	//////////////////////
	public static String  atm_getGlobalProperty( Context ctx, String key ) {
		final String prefNom = "atm";
		String dato = "";
		try {
			dato = ctx.getSharedPreferences( prefNom, Context.MODE_PRIVATE ).getString(key, "");
		} catch (Exception e) {;}
		return dato;
	}
	public static int     atm_getGlobalPropertyNum(Context ctx, String key ) {
		final String prefNom = "atm";
		int dato = -1;
		try {
			dato = Integer.parseInt( ctx.getSharedPreferences( prefNom, Context.MODE_PRIVATE ).getString(key, "") );
		} catch (NumberFormatException e) {;}
		return dato;
	}
	public static boolean atm_getGlobalPropertyBoolean( Context ctx, String key ) {
		final String prefNom = "atm";
		boolean dato = false;
		try {
			dato = ctx.getSharedPreferences( prefNom, Context.MODE_PRIVATE ).getBoolean(key, false);
		} catch (NumberFormatException e) {;}
		return dato;
	}
	public static void    atm_setGlobalProperty( Context ctx, String key, String value) {
		final String prefNom = "atm";
		ctx.getSharedPreferences( prefNom, Context.MODE_PRIVATE )
				.edit()
				.putString( key, value )
				.commit();
	}
	public static void    atm_setGlobalPropertyBoolean( Context ctx, String key, boolean value) {
		final String prefNom = "atm";
		ctx.getSharedPreferences( prefNom, Context.MODE_PRIVATE )
				.edit()
				.putBoolean( key, value )
				.commit();
	}
	//////////////////////
	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());

			// Create Hex String
			return new BigInteger(1, digest.digest()).toString(16);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getNivelBateria(Context ctx) {

		IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = ctx.registerReceiver(null, iFilter);

		int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
		int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

		float batteryPct = level / (float) scale;

		return (int) (batteryPct * 100.0);
	}

	public static boolean decodeB64ToImageView(String b64_encodedImage, ImageView inOut_iv) {
		Bitmap bm = decodeB64ToBitmap(b64_encodedImage);
		if ( bm != null ) {
			inOut_iv.setImageBitmap( bm );
			return true;
		}
		return false;
	}
	public static Bitmap decodeB64ToBitmap(String b64_encodedImage) {
		Bitmap decodedByte = null;
		try {
			byte[] decodedString = Base64.decode(b64_encodedImage.getBytes(), Base64.DEFAULT);
			decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decodedByte;
	}

	public static DisplayMetrics pantalla_getMetrics(Context ctx ) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics;
	}
	public static boolean 		 pantalla_isHorizontal( Context ctx ) {
		boolean isHorizontal = false;
		// Ritación de la pantalla:
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		switch ( wm.getDefaultDisplay().getRotation() ) {
			case Surface.ROTATION_0   : break;
			case Surface.ROTATION_90  : isHorizontal = true; break;
			case Surface.ROTATION_180 : break;
			case Surface.ROTATION_270 : isHorizontal = true; break;
		}
		return isHorizontal;
	}

	public static void keyboard_show( View v ) {
		if ( v==null ) return;
		// only will trigger it if no physical keyboard is open
		InputMethodManager mgr = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.showSoftInput(v, InputMethodManager.SHOW_FORCED);
	}
	public static void keyboard_hide( View v ) {
		if ( v==null ) return;
		InputMethodManager mgr = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public static Intent getIntent_App(Context ctx, String nombreApp ) {

		Log.i( TAG, "getIntentApp() >>>>>>>>>>" );

		Intent resultado = null;

		if ( ctx == null  )			{ return resultado; }
		if ( nombreApp == null )	{ return resultado; }

		PackageManager pm = ctx.getPackageManager();

		Intent iRef = new Intent(Intent.ACTION_MAIN, null);
		iRef.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> appList = pm.queryIntentActivities (iRef, 0);
		Collections.sort( appList, new ResolveInfo.DisplayNameComparator(pm) );
		for(int i=0; i<appList.size(); i++) {
			Log.i( TAG,
					"-->" +
							(String) appList.get(i).loadLabel(pm) +
							" ComponentName: " +
							appList.get(i).activityInfo.applicationInfo.packageName +
							", " +
							appList.get(i).activityInfo.name
			);
			if ( nombreApp.equalsIgnoreCase( (String) appList.get(i).loadLabel(pm) ) ) {
				resultado = new Intent();
				resultado.setAction(Intent.ACTION_MAIN);
				resultado.addCategory(Intent.CATEGORY_LAUNCHER);
				resultado.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				resultado.setComponent(
						new ComponentName(
								appList.get(i).activityInfo.applicationInfo.packageName,
								appList.get(i).activityInfo.name
						)
				);
				// ctx.startActivity(resultado);
				break;
			}
		}

		iRef = null;
		appList = null;
		Log.i( TAG, "getIntentApp() <<<<<<<<<<" );
		return resultado;
	}
	public static void   gps_startSettings( Context ctx, boolean aunqueEsteEncendido ) {
		// Para poder abrir el panel de conexión del GPS si no está conectado:
		String provider = "gps";
		if (
				aunqueEsteEncendido
						||
						!
								((LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE))
										.isProviderEnabled ( provider )
				) {
			ctx.startActivity( new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS) );
		}
	}
	//////////////////////

}
