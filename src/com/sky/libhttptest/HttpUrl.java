package com.sky.libhttptest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class HttpUrl {

	private final String TAG = "HttpUrl";

	private static CookieStore cookieStore;

	public String get(String uri) {

		BufferedReader in = null;
		String result = null;
		try {

			DefaultHttpClient client = new DefaultHttpClient();
			// cookie °ó¶¨
			if (cookieStore != null) {
				client.setCookieStore(cookieStore);
			}
			HttpGet request = new HttpGet(uri);
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent(), "UTF-8"));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			result = new String(sb.toString().getBytes(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public String post(String url, List<NameValuePair> params) {

		String result = null;

		try {

			HttpPost httpPost = new HttpPost(url);

			if (params != null && params.size() > 0) {

				HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
				httpPost.setEntity(entity);
			}

			DefaultHttpClient httpClient = new DefaultHttpClient();

			// cookie °ó¶¨
			if (cookieStore != null) {
				httpClient.setCookieStore(cookieStore);
			}

			HttpResponse httpResponse = httpClient.execute(httpPost);

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpResponse.getEntity().getContent()));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				builder.append(s);
			}
			result = builder.toString();
			Log.d(TAG, "result is ( " + result + " )");

			// cookie ±£´æ
			cookieStore = ((AbstractHttpClient) httpClient).getCookieStore();

		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

		Log.d(TAG, "over");
		return result;
	}

	public void setCookie(CookieStore cookie) {
		cookieStore = cookie;
	}
}