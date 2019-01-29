package com.atm.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public final class K {
	
	// Tratar de incluir en esta clase solo funciones que:
	//		operen sobre parametros recibidos para que puedan ser 'static'.
	//		que utilicen 'Context' o 'Views' desde las 'activities'.
	
    public static final int K_BASE 						= 0x0000F000;
	
    public static final int MSG_CARGANDO				= K_BASE + 0x00000001;
    public static final int MSG_CARGANDO_ESPERE 		= K_BASE + 0x00000002;
    public static final int MSG_FIN_ACCION_SOLICITADA 	= K_BASE + 0x00000003;
	public static final int MSG_MNU_REFRESCAR 			= K_BASE + 0x00000004;
	public static final int MSG_MNU_NUEVO	 			= K_BASE + 0x00000005;
	public static final int MSG_MNU_NUEVO_OK 			= K_BASE + 0x00000006;
	public static final int MSG_MNU_EDITAR 				= K_BASE + 0x00000007;
	public static final int MSG_MNU_EDITAR_OK			= K_BASE + 0x00000008;
	public static final int MSG_MNU_SUPRIMIR			= K_BASE + 0x00000009;
	public static final int MSG_MNU_SUPRIMIR_OK			= K_BASE + 0x00000010;
	public static final int MSG_ACEPTAR					= K_BASE + 0x00000011;
	public static final int MSG_CANCELAR				= K_BASE + 0x00000012;
    
    public static final String NODATA 					= "Sin datos a mostrar";

	public static final boolean IS_PANTALLAS_NO_ATRAS	= false;
	public static final boolean IS_PANTALLAS_GIRAR 		= true;
	public static final int 	DEFAULT_ORIENTATION 	= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;//Configuration.ORIENTATION_PORTRAIT;
	
	public static void LeftToRight_Animation( View v, long msDuracion ) {
		////////////////////////////////////////////
		Animation animation = new TranslateAnimation( 
				Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, 
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f 
		); 
		animation.setDuration( msDuracion );		
		////////////////////////////////////////////
		v.startAnimation(animation); 
	}

	public static void dialogoSiNo( Context ctx, final Handler handler, CharSequence rotulo, final Message mensajeSi, final Message mensajeNo ) {
		///////////////////////////////////
		new AlertDialog.Builder( ctx )
		.setMessage( rotulo )
		.setCancelable(false)
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				///////////////////////////////////
				if ( mensajeNo != null ) {
					handler.sendMessage( mensajeNo );
				} else {
					dialog.cancel();
				}
				///////////////////////////////////
			}
		})
		.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				///////////////////////////////////
				if ( mensajeSi != null ) {
					handler.sendMessage( mensajeSi );
				} else {
					dialog.cancel();
				}
				////////////////////////////////////
			}
		}).create().show();
		///////////////////////////////////
		return;
	}

}
