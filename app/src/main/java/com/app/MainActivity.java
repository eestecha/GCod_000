package com.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.db.PS_countries_DAO;
import com.app.db._DBHelper;
import com.app.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Context mCtx;
    private _DBHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCtx = this;
        if (mDBHelper==null) mDBHelper = new _DBHelper(this);

        ((Button) findViewById(R.id.btUs))
                .setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                Toast.makeText(mCtx, "Button US Clicked", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(mCtx, "Button PS Clicked", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(mCtx,PS_countries_DSPFIL.class);
                                //	intent.putExtra ( "ps_reg", reg );
                                startActivity( intent );
                            }
                        }
                );

        ((Button) findViewById(R.id.btLogin))
                .setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                Toast.makeText(mCtx, "Button LOGIN Clicked", Toast.LENGTH_LONG).show();
                                String nivel = Util.getNivelBateria(mCtx);
                                EditText campo = (EditText) findViewById(R.id.etUSR);
                                campo.setText(nivel);
                            }
                        }
                );

        ((Button) findViewById(R.id.btLoadPS))
                .setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {
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
                                        Toast.makeText(mCtx, "Países leídos: " + i + ", cargados en BD: " + j, Toast.LENGTH_LONG).show();
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
}
