package ideamaxwu.crowdweatherv2;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonWebData {
	String http = "http://api.map.baidu.com/telematics/v3/weather";

	@SuppressWarnings("unused")
	List<String> getData(String location) {
		int error = 0;
		String status = "";
		String date = "";
		JSONArray results = null;
		JSONObject result = null;

		String currentCity = "";
		String pm25 = "";
		JSONArray index = null;
		JSONArray weather_data = null;

		String rawstr = "";
		rawstr = Send(location, http);
		System.out.println(rawstr);

		List<String> wData = new ArrayList<String>();

		try {
			JSONTokener jsonParser = new JSONTokener(rawstr);
			JSONObject all = (JSONObject) jsonParser.nextValue();
			error = all.getInt("error");
			status = all.getString("status");
			date = all.getString("date");
			results = all.getJSONArray("results");

			result = results.getJSONObject(0);

			currentCity = result.getString("currentCity");
			pm25 = result.getString("pm25");
			index = result.getJSONArray("index");
			weather_data = result.getJSONArray("weather_data");

			wData.add(currentCity);

			for (int i = 0; i < weather_data.length(); i++) {

				String weekday = weather_data.getJSONObject(i)
						.getString("date");
				String dayPictureUrl = weather_data.getJSONObject(i).getString(
						"dayPictureUrl");
				String nightPictureUrl = weather_data.getJSONObject(i)
						.getString("nightPictureUrl");
				String weather = weather_data.getJSONObject(i).getString(
						"weather");
				String wind = weather_data.getJSONObject(i).getString("wind");
				String temperature = weather_data.getJSONObject(i).getString(
						"temperature");
				System.out.println(weekday + "," + dayPictureUrl + ","
						+ nightPictureUrl + "," + weather + "," + wind + ","
						+ temperature);

				wData.add(weather);
				wData.add(wind);
				int start = temperature.indexOf('~');
				int end = temperature.indexOf('℃');
				if (start == -1) {
					String low = temperature.substring(0, end).trim();
					String high = (Integer.parseInt(low)+8)+"";
					wData.add(high);
					wData.add(low);
				} else {
					String low = temperature.substring(start + 1, end).trim();
					String high = temperature.substring(0, start).trim();
					wData.add(high);
					wData.add(low);
				}
			}
			// log
			System.out.println("error: " + error + ", status: " + status
					+ ", date: " + date + ", results: " + results);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wData;
	}

	String Send(String location, String uriAPI) {
		String url = uriAPI + "?" + "location=" + location
				+ "&output=json&ak=g1IMxCUNl9NMfKkGLFW77Tvf";
		HttpGet get = new HttpGet(url);
		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(get);// 执行get方法
			String resultString = EntityUtils.toString(response.getEntity());
			return resultString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;

	}

}
