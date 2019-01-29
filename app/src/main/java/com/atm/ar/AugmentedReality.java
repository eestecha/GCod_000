package com.atm.ar;

import java.util.List;

import org.openintents.intents.AbstractWikitudeARIntent;
import org.openintents.intents.WikitudeARIntent;
import org.openintents.intents.WikitudePOI;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.res.Resources;

public class AugmentedReality extends Object {
	
	private Activity mActivity;
	private List<WikitudePOI> mWikiPOIs;
	private int mIconID;
	
	public AugmentedReality( Activity activity ) {
		mActivity = activity;
	}

	public void launchARView( List<WikitudePOI> wikiPOIs, int defaultIconId ) {
		mWikiPOIs = wikiPOIs;
		mIconID = defaultIconId;
		
////////////////////////////////////////
//	Los parámetros necesarios para el constructor son:
//
//	WikitudeARIntent (
//		Application application, 
//		String applicationKey, 
//		String developerName, 
//		boolean debugMode )
//
//	Los argumentos applicationKey y developerName se utilizan cuando 
// se ha registrado la aplicación a través de la página Mobilizy.
// Si rellenas el formulario de registro te enviarán por correo 
// una API Key que podrás usar en la aplicación y que 
// permitirá eliminar la marca de agua de la vista de cámara.
// Para obtener esta API key se puede hacer con la siguiente pagina:
//		http://w4client.mobilizy.mobi/w4/jsp/keyGenerator.jsp
////////////////////////////////////////
// Web de login para descargas y tal: (usr:eestecha@gmail.com pwd:09....61)
//		http://devzone.wikitude.com/w4/wme/login.jsp
////////////////////////////////////////
//		
//		Dear Emilio Estecha,
//
//		Thank you for registering your application "atm". You provided the following information to our system:
//		Application name: atm
//		Application description: Gestión en ruta.
//		Application package: com.atm
//		Developer Name: Emilio Estecha
//		Developer E-Mail: eestecha@gmail.com
//		Platform: Android
//
//		Please use the following API-key to unlock the Wikitude camera-screen:
//
//		d4cee6ed-eaeb-4380-83f3-0d643f030b87
//
//		We hope you will enjoy using the WIKITUDE API. 
//		If you have any questions, please feel free to contact us on www.mobilizy.com or www.wikitude.org.
//
//		Best regards,
//		The WIKITUDE team.
////////////////////////////////////////
		
		WikitudeARIntent intent = new WikitudeARIntent(
				mActivity.getApplication(), 			// Application
				"d4cee6ed-eaeb-4380-83f3-0d643f030b87",	// Application Key
				"Emilio Estecha", 						// Developer Name
				false									// Debug Mode
				);

		addPois(intent);

		try {

			intent.startIntent( mActivity );

		} catch (ActivityNotFoundException e) {

			AbstractWikitudeARIntent.handleWikitudeNotFound( mActivity );

		}

	}

	private void addPois(WikitudeARIntent intent) {

		Resources res = mActivity.getResources();

		// Añadir puntos de interés:
		WikitudePOI poi = null;
		for(int i=0; i<mWikiPOIs.size(); i++) {
			poi = mWikiPOIs.get(i); // new WikitudePOI(40.455411, -3.695348,636, "Nombre", "Descripción");
			poi.setLink("http://inventario.casbega.net");
			if ( poi.getIconresource() == null || poi.getIconresource().trim().length() < 1 ) {
				poi.setIconresource(res.getResourceName( mIconID ));
			}
//			poi.setDetailAction("wikitudeapi.mycallbackactivity");  
			intent.addPOI(poi);
		}
	}

}
