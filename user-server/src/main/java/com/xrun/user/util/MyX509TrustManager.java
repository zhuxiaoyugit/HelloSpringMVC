package com.xrun.user.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.springframework.util.ResourceUtils;

public class MyX509TrustManager implements X509TrustManager
{
	private static Certificate cert = null;
	
	static
	{
		try 
		{
			FileInputStream fis = new FileInputStream(ResourceUtils.getFile("classpath:server.crt"));
			BufferedInputStream bis = new BufferedInputStream(fis);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			while (bis.available() > 0) 
			{
				cert = cf.generateCertificate(bis);
			}
			bis.close();
		} 
		catch (CertificateException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public X509Certificate[] getAcceptedIssuers()
	{
		return new X509Certificate[] { (X509Certificate) cert };
	}
	
	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
	{
		for (X509Certificate cert : chain) 
		{
			if (cert.toString().equals(cert.toString()))
			{
				return;
			}
		}
		throw new CertificateException("certificate is illegal");
	}
	
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException{}
}
