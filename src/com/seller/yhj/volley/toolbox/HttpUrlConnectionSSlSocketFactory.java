package com.seller.yhj.volley.toolbox;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;

public class HttpUrlConnectionSSlSocketFactory {

	private static SSLSocketFactory mInstance;
	private static final String LOCK = "lock";
	
	private static final String CER_NAME ="ehking.cer";
	private static final String CER_TYPE ="X.509";
	private static final String CER_ALIAS ="ehking";
	
	public static SSLSocketFactory getSSlSocketFactory(Context context) {
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
		
					    String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
					    TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
					    tmf.init(keystore);
		
					    SSLContext sslContext = SSLContext.getInstance("TLS");
					    sslContext.init(null, tmf.getTrustManagers(), null);
					    mInstance = sslContext.getSocketFactory();
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
}
