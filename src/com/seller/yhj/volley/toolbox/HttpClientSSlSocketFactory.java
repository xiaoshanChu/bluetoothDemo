package com.seller.yhj.volley.toolbox;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import org.apache.http.conn.ssl.SSLSocketFactory;

import android.content.Context;

public class HttpClientSSlSocketFactory extends SSLSocketFactory {

	private static HttpClientSSlSocketFactory mInstance;
	private static final String LOCK = "lock";
	
	private static final String CER_NAME ="ehking.cer";
	private static final String CER_TYPE ="X.509";
	private static final String CER_ALIAS ="ehking";

	public HttpClientSSlSocketFactory(KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		super(truststore);
	}

	public static HttpClientSSlSocketFactory getSSlSocketFactory(Context context) {
		KeyStore keystore = null;
		InputStream in = null;
		try {
			if (null == mInstance) {
				synchronized (LOCK) {
					if (null == mInstance) {
						CertificateFactory cf = CertificateFactory.getInstance(CER_TYPE);
						in = context.getAssets().open(CER_NAME);
						Certificate ca = cf.generateCertificate(in);
			
						keystore = KeyStore.getInstance(KeyStore.getDefaultType());
						keystore.load(null, null);
						keystore.setCertificateEntry(CER_ALIAS, ca);
					
						mInstance = new HttpClientSSlSocketFactory(keystore);
					}
				}
			}
		} catch (Exception e) {
			if(in != null){
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return mInstance;
	}

	@Override
	public Socket createSocket() throws IOException {
		return super.createSocket();
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return super.createSocket(socket, host, port, autoClose);
	}

}
