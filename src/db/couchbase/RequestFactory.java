package db.couchbase;

import java.net.HttpURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpGet;

public class RequestFactory 
{
	// --- Fields ---
	
	// --- Constructors ---
	public RequestFactory() {}
	
	// --- Methods ---
	/**
	 * Builds a JSON get request
	 * @param url
	 * @return
	 * 		HttpGet Object
	 */
	public static HttpGet jsonGetReq(String url) 
	{
		HttpGet get_req = new HttpGet(url);
		get_req.addHeader("Content-Type", "text/plain; charset=UTF-8");
		get_req.addHeader("accept", "application/json");
		return get_req;
	}
	
	/**
	 * Builds a JSON get request with basic authentication 
	 * @param url
	 * @return
	 * 		HttpGet Object
	 */
	public static HttpGet jsonGetReq(String url, String user, String pass) 
	{
		HttpGet get_req = new HttpGet(url);
		get_req.addHeader("Content-Type", "text/plain; charset=UTF-8");
		get_req.addHeader("accept", "application/json");
		get_req.addHeader("Authorization: Basic", encodeAuth(user, pass));
		return get_req;
	}

	/**
	 * Encodes the auth header string 
	 * @return
	 * 		BASE64 encoded auth string
	 */
	public static String encodeAuth(String user, String pass) 
	{
		String auth = user + ":" + pass;
		byte[] encoded_bytes = Base64.encodeBase64(auth.getBytes());
		return encoded_bytes.toString();
	}
}
