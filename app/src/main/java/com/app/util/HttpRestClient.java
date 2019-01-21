package com.app.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Emilio on 02/07/2016.
 */
public class HttpRestClient {

    private static final String TAG = HttpRestClient.class.getSimpleName();

    public URI mUri; // "http://181.75.100.28:8084/PDAServer/GPSData"
    public URL mUrl; // "http://181.75.100.28:8084/PDAServer/GPSData"
    public int mHttp_RC = 0;
    public String mHttp_ReasonPhrase = "";

    public HttpRestClient(String url) {
        mUri = URI.create(url);
        try {
            mUrl = new URL( url );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public HttpRestClient(URI url) {
        mUri = url;
    }

    public String postData( Map<String,Object> nameValuePairs, int msTimeOut ) {
        String resultado = null;
        byte[] cosa = postDataBin(nameValuePairs,msTimeOut);
        if ( cosa != null ) {
            try {
                resultado = new String( cosa, "UTF-8" );
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return resultado;
    }

    public byte[] postDataBin( Map<String,Object> nameValuePairs, int msTimeOut ) {

        List<Byte> resultado = new ArrayList<Byte>();

        StringBuilder postData = new StringBuilder();
        byte[] postDataBytes = new byte[0];
        try {
            for (Map.Entry<String,Object> param : nameValuePairs.entrySet()) {
                // if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                postData.append('\n');
            }
            postDataBytes = postData.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpURLConnection conn = null;
        try {

            conn = (HttpURLConnection) mUrl.openConnection();
            conn.setReadTimeout(msTimeOut /* milliseconds */);
            conn.setConnectTimeout(msTimeOut /* milliseconds */);

            conn.setDoOutput( true );
//            conn.setInstanceFollowRedirects( false );
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty( "Content-Length", Integer.toString( postDataBytes.length ));
            conn.setUseCaches( false );

            conn.getOutputStream().write(postDataBytes);

//            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            InputStream in = new BufferedInputStream(conn.getInputStream());

            this.mHttp_RC = conn.getResponseCode();
            this.mHttp_ReasonPhrase = conn.getResponseMessage();

            if ( 200 == this.mHttp_RC ) {
                byte[] buff = new byte[1024];
                int leidos = in.read(buff);
                while ( leidos > 0 ) {
                    for ( byte item : buff ) { resultado.add( item ); }
                    leidos = in.read(buff);
                };
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }

        byte[] mierda = new byte[ resultado.size() ];
        int i = 0;
        for ( Byte item : resultado ) {
            mierda[i++] = item;
        }

        return mierda;

    }
}
