package ideamaxwu.crowdweatherv2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class Client {

	String uriAPI = "http://10.77.20.22:8080/CrowdWeatherServer/";

	String Send(String[] key, String[] value) {
		HttpPost httpRequest = new HttpPost(uriAPI);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (int i = 0; i < key.length; i++) {
			params.add(new BasicNameValuePair(key[i], value[i]));
		}

		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {

				String strResult = EntityUtils.toString(httpResponse
						.getEntity());
				Log.v("OutPut", strResult);
				return strResult;
			} else {
				Log.v("Error", httpResponse.getStatusLine().toString());
			}
		} catch (ClientProtocolException e) {
			Log.v("OutPut", e.getMessage().toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.v("OutPut", e.getMessage().toString());
			e.printStackTrace();
		}
		return null;
	}
}
