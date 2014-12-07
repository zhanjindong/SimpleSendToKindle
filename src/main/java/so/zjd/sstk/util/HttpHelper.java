package so.zjd.sstk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 
 * Http helper.
 * 
 * @author jdzhan,2014-12-6
 * 
 */
public class HttpHelper {

	public static StringBuilder download(String url, int timeout) throws IOException {
		return download(url, timeout, "UTF-8");
	}

	public static StringBuilder download(String url, int timeout, String encoding) throws IOException {

		HttpURLConnection urlConnection = null;
		InputStream is = null;
		BufferedReader br = null;
		StringBuilder result = new StringBuilder();

		try {
			urlConnection = cretateConnection("GET", url, timeout);
			is = urlConnection.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			String readLine = null;
			while ((readLine = br.readLine()) != null) {
				result.append(readLine);
			}
		} finally {
			close(br);
			close(is);
			close(urlConnection);
		}
		return result;
	}

	public static boolean download(String url, int timeout, OutputStream os) throws IOException {
		return download(url, timeout, "UTF-8", os);
	}

	public static boolean download(String url, int timeout, String encoding, OutputStream os) throws IOException {
		HttpURLConnection urlConnection = null;
		InputStream is = null;
		BufferedReader br = null;

		try {
			url = url.replace(" ", "%20");
			urlConnection = cretateConnection("GET", url, timeout);
			is = urlConnection.getInputStream();
			IOUtils.write(is, os);
		} finally {
			close(br);
			close(is);
			close(urlConnection);
		}
		return true;
	}

	private static HttpURLConnection cretateConnection(String method, String reqString, int timeout) throws IOException {
		URL url = new URL(reqString);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setReadTimeout(timeout);
		urlConnection.setConnectTimeout(timeout);

		//模拟浏览器User-Agent
		urlConnection.addRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36");
		
		urlConnection.setRequestMethod(method); // request method, default
												// GET
		if (method.equals("POST")) {
			urlConnection.setUseCaches(false); // Post can not user cache
			urlConnection.setDoOutput(true); // set output from urlconn
			urlConnection.setDoInput(true); // set input from urlconn
		}
		return urlConnection;
	}

	private static void close(BufferedReader br) {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				// ignored
			}
		}
	}

	private static void close(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				// ignored
			}
		}
	}

	private static void close(HttpURLConnection conn) {
		if (conn != null) {
			conn.disconnect();
		}
	}
}
