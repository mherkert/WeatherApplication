package vandy.mooc.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import vandy.mooc.aidl.WeatherData;
import vandy.mooc.cache.WeatherDataCache;
import vandy.mooc.jsonweather.JsonWeather;
import vandy.mooc.jsonweather.WeatherJSONParser;

public class Utils {

	/**
	 * Time to live for cached results: 10 seconds
	 */
	private final static int TTL = 10 * 1000;

	/**
	 * URL to retrieve weather data
	 */
	private final static String WEATHER_WEB_SERVICE_URL = "http://api.openweathermap.org/data/2.5/weather?units=metric&q=";

	public static List<WeatherData> getResults(final String location) {
		WeatherDataCache cache = WeatherDataCache.getInstance();
		List<WeatherData> cachedResults = cache.get(location);
		if (cachedResults != null)
			return cachedResults;
		else {
			List<WeatherData> freshResults = getFreshResults(location);
			cache.put(location, freshResults, TTL);
			return freshResults;
		}
	}

	private static List<WeatherData> getFreshResults(final String location) {
		// Create a List that will return the WeatherData obtained
		// from the Weather Service web service.
		final List<WeatherData> returnList = new ArrayList<WeatherData>();

		// A List of JsonWeather objects.
		List<JsonWeather> jsonWeathers = null;

		try {
			// Append the location to create the full URL.
			final URL url = new URL(WEATHER_WEB_SERVICE_URL + location);

			// Opens a connection to the Weather Service.
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();

			// Sends the GET request and reads the Json results.
			try (InputStream in = new BufferedInputStream(
					urlConnection.getInputStream())) {
				// Create the parser.
				final WeatherJSONParser parser = new WeatherJSONParser();

				// Parse the Json results and create JsonWeather data
				// objects.
				jsonWeathers = parser.parseJsonStream(in);
			} finally {
				urlConnection.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// See if we parsed any valid data.
		if (jsonWeathers != null && jsonWeathers.size() > 0) {
			// Convert the JsonWeather data objects to our WeatherData
			// object, which can be passed between processes.
			for (JsonWeather jsonWeather : jsonWeathers) {
				WeatherData data = new WeatherData();
				data.mName = jsonWeather.getName();
				data.mSpeed = jsonWeather.getWind().getSpeed();
				data.mDeg = jsonWeather.getWind().getDeg();
				data.mTemp = jsonWeather.getMain().getTemp();
				data.mHumidity = jsonWeather.getMain().getHumidity();
				data.mIcon = jsonWeather.getWeather().get(0).getIcon();
				data.mSunrise = jsonWeather.getSys().getSunrise();
				data.mSunset = jsonWeather.getSys().getSunset();
				data.mCountry = jsonWeather.getSys().getCountry();
				returnList.add(data);
			}
			// Return the List of WeatherData.
			return returnList;
		} else
			return null;
	}
}
