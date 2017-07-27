package com.xrun.user.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

/**
 * AES 加密算法
 * 
 * @author jimiao
 *
 */
public class AESSecretUtil
{

	private static String encryption = "AES";
	private static String scheme = "CBC";
	private static String complementWay = "PKCS5Padding";
	public static Cipher encryptCipher = null;
	public static Cipher decryptCipher = null;

	public static String Encrypt(String content, String pwdKey, String iv)
	{
		try
		{
			if (encryptCipher == null)
			{
				byte[] raw = pwdKey.getBytes("ASCII");
				SecretKeySpec skeySpec = new SecretKeySpec(raw, encryption);
				encryptCipher = Cipher.getInstance(encryption + "/" + scheme + "/" + complementWay);
				IvParameterSpec iv2 = new IvParameterSpec(iv.getBytes());
				encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv2);
			}

			byte[] encrypted = encryptCipher.doFinal(StringUtils.getBytesUtf8(content));
			return Base64.encodeBase64String(encrypted);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static String Decrypt(String content, String pwdKey, String iv)
	{
		try
		{
			if (decryptCipher == null)
			{
				byte[] raw = pwdKey.getBytes("ASCII");
				SecretKeySpec skeySpec = new SecretKeySpec(raw, encryption);
				decryptCipher = Cipher.getInstance(encryption + "/" + scheme + "/" + complementWay);
				IvParameterSpec iv2 = new IvParameterSpec(iv.getBytes());
				decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec, iv2);
			}

			byte[] encrypted = Base64.decodeBase64(content);
			byte[] original = decryptCipher.doFinal(encrypted);

			return StringUtils.newStringUtf8(original);

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}

	public static String Decrypt(byte[] content, String pwdKey, String iv)
	{
		try
		{
			if (decryptCipher == null)
			{
				byte[] raw = pwdKey.getBytes("ASCII");
				SecretKeySpec skeySpec = new SecretKeySpec(raw, encryption);
				decryptCipher = Cipher.getInstance(encryption + "/" + scheme + "/" + complementWay);
				IvParameterSpec iv2 = new IvParameterSpec(iv.getBytes());
				decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec, iv2);
			}

			byte[] encrypted = Base64.decodeBase64(content);
			byte[] original = decryptCipher.doFinal(encrypted);

			return StringUtils.newStringUtf8(original);

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}

	//{ uuid:xuchenyang@imohoo.com,third_token: UDnid00000014956126196140lqBZRZlKVxTOtx0yaJQBD9spl3rX5i7, third_type: 1}
	public static void main(String[] args) throws Exception
	{
		//		String content = "{\"uuid\":\"wangm@imohoo.com\",\"third_type\":1,\"third_token\":\"UDnid111111114956126196140lqBZRZlKVxTOtx0yaJQBD9spl3rX5i7\"}";
		String content = " {\"user_id\": 1082,\"user_token\": \"92D05209D8CD7FAE6E043275CA215C88\",\"first_name\": \"Levy\",\"last_name\": \"🔥\",\"gender\":0 }";
		//		String content = "{\"uuid\":\"5790A56F1B4B74151AFC04BFB6A77ED4\",\"third_token\":\"5891424\",\"third_type\":2,\"mac_id\":2}";
		////		content = URLEncoder.encode("NIJ5UUxUh5i2HZ2hFnFmR45hofZkYQmucWXFURYg4YgLk5YYoIhNAwEzXjgHEntvLL6T5fGGBTJbPy+zFowyJPWH2IOkA3yiEP0n2PUsKncs84dzARshrfnru7hWfCFbLPySLhJpNTYOt9l1rptV9g==","utf-8");
		//		String content = "lRW9mDWSx1cZXe53uxoLkQo72Y6jOLyJLYIZ9eqNckvZWeoIgBJTN1uCioBfAeJAjOTWwwI/duf7QSCWgp3h1onJWja7XpaxY1h7TJgaQjMIu1EaWYWiOLzFmpyF30YtWOqTSGXu4Ea5yiNAvNKDkqDBhJB0iyq1UiQttcpAoEbxz9UTfK3ILTFMnCT8zaZTlDsEpotQYlGd7q/q2/vnFgJlcx19vKEB/9XIDpTSyESRtETCkMdv+rjlTbDeUN3XxC3pmefWLulGHLKTy/5V61NFRquqX9qTgRsQmQtXPPekAXej+eKUHxmu3mSU+3qJd5o+nngGfrnzfmE7MKpmmAlI1pdGUQ1DK2LdW0UOKhk=";
		content = AESSecretUtil.Encrypt(content, Constant.AES_PASSWORD_KEY, Constant.AES_IV);
		content = URLEncoder.encode(content, "utf-8");
		System.out.println(content);

		String content2 = "2RxfgxRAykpooYh1TN/BW73wtTZyrQXzwPzMUi+rJtCfMhYn1lk3pwdI9Y7BjaOE1lXF6xIt0JD0qHoU0uReKw==";
		//		// System.out.println(content);
//				 content2 = URLDecoder.decode(content2, "UTF-8");
		//		 System.out.println(content);
		String res2 = AESSecretUtil.Decrypt(content2, Constant.AES_PASSWORD_KEY, Constant.AES_IV);
		System.out.println(res2);

		//		String[] tZone = TimeZone.getAvailableIDs();
		//		for(String z : tZone)
		//		{
		//			System.out.println(z);
		//		}
		//		TimeZone tz = TimeZone.getTimeZone("Asia/Chongqing");
		//		DateFormat df = DateFormat.getInstance();
		//		df.setTimeZone(tz);
		//		String str = df.format(new Date());
		//		System.out.println(str);

		//		Calendar calendar = Calendar.getInstance();
		//		calendar.setTimeInMillis(System.currentTimeMillis());
		//		System.out.println(calendar.getTime());
		//		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		//		UUID uuid= UUID.randomUUID();
		//		System.out.println(uuid);

	}
}
