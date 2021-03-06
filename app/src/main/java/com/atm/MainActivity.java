package com.atm;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.atm.ar.AugmentedReality;
import com.atm.db.PS_countries_DAO;
import com.atm.db._DBHelper;
import com.atm.util.K;

import org.openintents.intents.WikitudePOI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Context mCtx;
    private _DBHelper mDBHelper;

	private ListAdapter mLstAdpt;
	private ListView mLv_main;

	private AugmentedReality mAugmentedReality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Log.i(TAG,"onCreate()");

		setContentView(R.layout.activity_main);

		mCtx = this;
		if (mDBHelper==null) mDBHelper = new _DBHelper(this);

//		Intent intent = getIntent();
//		String frase_phone_receiver = intent.getStringExtra("phone_receiver");
//		Log.i(TAG,"**********  " + frase_phone_receiver);

        mLv_main = null;
        mLv_main = (ListView) findViewById(R.id.lv_main);

		((Button) findViewById(R.id.btUs))
                .setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
//                            Toast.makeText(mCtx, "Button US Clicked", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(mCtx,US_Users_DSPFIL.class);
                            //	intent.putExtra ( "us_reg", reg );
                            startActivity( intent );
                        }
                    }
                );

		((Button) findViewById(R.id.btPs))
				.setOnClickListener(
						new View.OnClickListener() {
							public void onClick(View v) {
//                                Toast.makeText(mCtx, "Button PS Clicked", Toast.LENGTH_LONG).show();
								Intent intent = new Intent(mCtx,PS_countries_DSPFIL.class);
								//	intent.putExtra ( "ps_reg", reg );
								startActivity( intent );
							}
						}
				);

		((Button) findViewById(R.id.btTf))
				.setOnClickListener(
						new View.OnClickListener() {
							public void onClick(View v) {
//                                Toast.makeText(mCtx, "Button PS Clicked", Toast.LENGTH_LONG).show();
								Intent intent = new Intent(mCtx,TF_phone_DSPFIL.class);
								//	intent.putExtra ( "ps_reg", reg );
								startActivity( intent );
							}
						}
				);

        ((Button) findViewById(R.id.btLoadPS))
                .setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {

                            	ar();

                                /*Toast.makeText(mCtx, "Button LOAD PS Clicked", Toast.LENGTH_LONG).show();*/

                                // Tip: If you need to package a file in your app that is accessible at install time,
                                // save the file in your project's res/raw/ directory.
                                // You can open these files with openRawResource(), passing the R.raw.filename resource ID.
                                // This method returns an InputStream that you can use to read the file. You cannot write to the original file.

                                InputStream is;
                                try {
                                    is = getResources().openRawResource(R.raw.paises);
                                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                                    // Read the entire resource into a local StringBuffer.
                                    StringBuffer sb = new StringBuffer();
                                    String line;
                                    int i = 0;
                                    while( (line=br.readLine()) != null ){
                                        sb.append(line);
                                        sb.append("\r\n");
                                        ++i;
                                    }

                                    if ( i > 0 ) {
                                        int j;
                                        PS_countries_DAO dao = new PS_countries_DAO();
                                        dao.dropTb(mDBHelper.mDB);
                                        dao.create(mDBHelper.mDB);
                                        j = dao.recibirDatos_Integrar(mDBHelper.mDB, sb.toString());
                                        /*Log.d(TAG, "Países cargados en BD: " + i);*/
                                        Toast.makeText(mCtx, "Países leídos: " + i + ", cargados en BD: " + j, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(mCtx, "No se han detectado países a cargar eb BD", Toast.LENGTH_LONG).show();
                                    }


                                } catch (Resources.NotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

/*
                                String nivel = Util.getNivelBateria(mCtx);
                                EditText campo = (EditText) findViewById(R.id.etUSR);
                                campo.setText(nivel);
*/
                            }
                        }
                );

    }

	@Override
	protected void onResume() {
		Log.i(TAG,"onResume()"); if ( ! mDBHelper.mDB.isOpen() ) { Log.w(TAG,"OPEN DB (...estaba cerrada!!)"); mDBHelper.mDB = mDBHelper.getWritableDatabase(); }

		cargarLista();

		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.i(TAG,"onPause()"); if (mDBHelper != null) { Log.i(TAG,"CLOSE DB"); mDBHelper.close(); }

		super.onPause();
	}

	private void cargarLista() {

		Log.i(TAG,"cargarLista()");

		// Inicializar filtro metiendo valores en 'DAO.mRegistro'
		mDBHelper.ps_countries.mRegistro.clean();

		// Recuperar en 'DAO.mRegistros' los registros que cumplan el filtro en 'DAO.mRegistro'
		mLstAdpt = null;
		mLstAdpt = mDBHelper.tf_phone.getAdapter(mDBHelper.mDB, mCtx, null);
		if ( mDBHelper.tf_phone.mRegistros.isEmpty() ) {
			mLstAdpt = null;
			String[] lstTmp = {K.NODATA};
			mLstAdpt = new ArrayAdapter<String>(mCtx, android.R.layout.simple_list_item_1,lstTmp);
		}

		mLv_main.setAdapter(mLstAdpt);

	}

	private void ar() {

		List<WikitudePOI> wikiPOIs = new ArrayList<WikitudePOI>();
		WikitudePOI poi = null;
		double dLat = 0.0;
		double dLon = 0.0;
		/////////////////////////////////////////////////////////////
    	// Tonterias:
    	dLat = 40.447992; dLon = -3.577701;
    	poi = new WikitudePOI(
    			dLat,	// Lat
    			dLon,	// Lon
    			600.0,	// Altitud (?)
    			"Goye", // Nom.Cliente
    			"Despacho de Javier Goyeneche"   // Direcc.
    	);
    	poi.setIconresource( this.getResources().getResourceName( R.drawable.pin_azul ));
    	wikiPOIs.add( poi );

    	dLat = 40.448049; dLon = -3.577679;
    	poi = new WikitudePOI(
    			dLat,	// Lat
    			dLon,	// Lon
    			600.0,	// Altitud (?)
    			"Unai", // Nom.Cliente
    			"Despacho de Unai García"   // Direcc.
    	);
    	// poi.setIconresource( this.getResources().getResourceName( R.drawable.pin_azul ));
    	wikiPOIs.add( poi );

    	dLat = 40.449095; dLon = -3.576778;
    	poi = new WikitudePOI(
    			dLat,	// Lat
    			dLon,	// Lon
    			600.0,	// Altitud (?)
    			"Hotel", // Nom.Cliente
    			"El hotel de ahí."   // Direcc.
    	);
    	// poi.setIconresource( this.getResources().getResourceName( R.drawable.pin_azul ));
    	wikiPOIs.add( poi );


		mAugmentedReality = new AugmentedReality( this );
		mAugmentedReality.launchARView( wikiPOIs, R.drawable.pin_rojo );

	}

}
