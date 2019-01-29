package com.atm.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReceiverDeAutoArranque extends BroadcastReceiver {

	private String TAG = getClass().getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.i( TAG, "Recibido ACTION_BOOT_COMPLETED" );

/*
			context.startService(new Intent(context, ServicioDeLocalizacion.class));

			Log.i( TAG, "Iniciado el servicio: " + ServicioDeLocalizacion.class.getSimpleName() );

//			BroadcastReceiver receiver = this;
//			IntentFilter filter = new IntentFilter( Intent.ACTION_TIME_TICK );
//			context.registerReceiver(receiver, filter);
*/
		}
	}

}
