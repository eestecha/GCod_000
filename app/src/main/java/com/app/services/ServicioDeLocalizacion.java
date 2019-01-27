package com.app.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.app.R;
import com.app.util.Util;

public class ServicioDeLocalizacion extends Service {

	// Ver http://developer.android.com/training/basics/location/currentlocation.html

	private final static String TAG = ServicioDeLocalizacion.class.getSimpleName();

	private LocationManager 	mLocationManager;
	private LocationListener 	mLocationListener;

	private NotificationManager mNotificationManager;
	private static final int ID_NOTIFCATION_1 = 1;
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	////////////////////

	public ServicioDeLocalizacion() {super();}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.i(TAG,"onCreate()");

		//////////////////////////////////////////////////////////
		// Notifications:
		mNotificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE ); // No olvidar en 'onDestroy' el '.cancelAll()'.

		//////////////////////////////////////////////////////////
		// Location manager:
		mLocationManager  = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE );	// No olvidar en 'onDestroy' el '.removeUpdates(...)'

		//////////////////////////////////////////////////////////
		mLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) { 
				// A new location update is received. 
				updateLocation(location);
			}
			public void onProviderEnabled(String provider) { 
				Log.i(TAG,"===> onProviderEnabled(" + provider + ")"); 
				// Toast.makeText( getApplicationContext(), "onProviderEnabled(" + provider + ")", Toast.LENGTH_SHORT).show();
			}
			public void onProviderDisabled(String provider) { 
				Log.i(TAG,"===> onProviderDisabled(" + provider + ")");
				// Toast.makeText( getApplicationContext(), "onProviderDisabled(" + provider + ")", Toast.LENGTH_SHORT).show();
			}
			public void onStatusChanged(String provider, int status, Bundle extras) {
				String frase = provider + " ";
				switch (status) {
				case LocationProvider.AVAILABLE : frase = "disponible"; break;
				case LocationProvider.OUT_OF_SERVICE : frase = "fuera de servicio"; break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE : frase = "temporalmente no disponible"; break;
				default : frase = "???"; break;
				}
				Log.i(TAG,"===> onStatusChanged(" + frase + ")");
				// Toast.makeText( getApplicationContext(), frase, Toast.LENGTH_SHORT).show();
			}
		};
		//////////////////////////////////////////////////////////
		// Suscripciones a los Location Providers:
		Location gpsLocation = null, networkLocation = null;
/*

		try {
			Log.i(TAG,"Establecer NETWORK_PROVIDER ---> " + LocationManager.NETWORK_PROVIDER );
			 mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 50, mLocationListener);
			 networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
		} catch (Exception e) { Log.e(TAG,e.getMessage()); }
		try {
			Log.i(TAG,"Establecer GPS_PROVIDER ---> " + LocationManager.GPS_PROVIDER );
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER    , 60000, 50, mLocationListener);
			gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER    , 0, 0, mLocationListener);
		} catch (Exception e) { Log.e(TAG,e.getMessage()); }
		//////////////////////////////////////////////////////////
		// Request updates from both fine (gps) and coarse (network) providers.        

        // If both providers return last known locations, compare the two and use the better
        // one to update the UI.  If only one provider returns a location, use it.
        if (gpsLocation != null && networkLocation != null) {
            updateLocation(getBetterLocation(gpsLocation, networkLocation));
        } else if (gpsLocation != null) {
            updateLocation(gpsLocation);
        } else if (networkLocation != null) {
            updateLocation(networkLocation);
        }
*/

		
		
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG,"onDestroy()");

		mLocationManager.removeUpdates( mLocationListener );
		mNotificationManager.cancelAll();

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG,"onStartCommand( " + startId + " )");

		if ( startId > 1 ) {
			Toast.makeText(this, "SERVICIO OPERATIVO", Toast.LENGTH_SHORT).show();
			// return START_STICKY;
		}

		// Genero (o regenero) su notificaci�n:
		String contentTitle = getApplicationContext().getResources().getString( com.app.R.string.app_name );
		String contentText  = getApplicationContext().getResources().getString( com.app.R.string.local_service_notif_action );
		lanzaNotificacion( contentTitle, contentText );

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	///////////////////////////////////////

	private void lanzaNotificacion( CharSequence contentTitle, CharSequence contentText ) {
		Log.i(TAG,"lanzaNotificacion()");
/*

		int icon = R.drawable.ic_cochecito;

		// Por si hay que verificar su tama�o o algo...
		// Drawable ic = getApplicationContext().getResources().getDrawable(icon);

		CharSequence tickerText = "CONVERSIA: Servicio en fondo";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		Intent notificationIntent = new Intent(this, ServicioDeLocalizacion_Activity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(
				getApplicationContext(), 
				contentTitle, 
				contentText, 
				contentIntent);

		// Disparo de la notificaci�n:
		mNotificationManager.notify( ID_NOTIFCATION_1, notification );
*/

		return;
	}

	protected Location getBetterLocation(Location newLocation, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return newLocation;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return newLocation;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return currentBestLocation;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return newLocation;
        } else if (isNewer && !isLessAccurate) {
            return newLocation;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return newLocation;
        }
        return currentBestLocation;
    }
	
	protected boolean isBetterLocation(Location newLocation, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 75;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
	
    private void updateLocation(Location location) {
    	if ( location == null ) { return; }

		Log.v(TAG,"===> updateLocation( "+ location.toString() + " )");
/*

		if ( Util.mColaPosiciones.size() > 0 ) {
			Location ultLoc = (Location)Util.mColaPosiciones.getLast();
			// Y merece la pena?
			//if ( location.getLatitude() == ultLoc.getLatitude() && location.getLongitude() == ultLoc.getLongitude() ) {
			//	return;
			//}			
			
			if ( ! isBetterLocation(location, ultLoc) ) {
				return;
			}
			
			if ( location.getLatitude() == ultLoc.getLatitude() && location.getLongitude() == ultLoc.getLongitude() ) {
				Log.v(TAG,"===> onLocationChanged( nuevo dato NO guardado: "+ location.getProvider()+";"+location.getLatitude() +";"+ location.getLongitude() + " )");
				return;
			}		
			
		}

		// Actualizar variable global de referencia para los programas de eos:
		Util.atm_setGlobalProperty(
				getApplicationContext(), 
				Util.KEY_UltimaLocalizacion, 
				location.getProvider()+";"+location.getLatitude() +";"+ location.getLongitude()
		);

		// Actualizar el buffer global de emisi�n a central para tracking de geoposiciones:
		try { Util.mColaPosiciones.add(location); } catch (Exception e) {;}

		Log.v(TAG,"===> onLocationChanged( nuevo dato guardado: "+ Util.atm_getGlobalProperty(getApplicationContext(), Util.KEY_UltimaLocalizacion) + " )");

		// Intenta emitir el buffer de pendientes:
		//	new Thread( new Runnable() { public void run() {
		//		Util.emitirPosicionesPendientes( getApplicationContext() );
		//	}}).start();
*/
    }

    
///////////////////////////////////////
}
