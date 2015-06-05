package vandy.mooc.jsonweather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class WeatherJSONParser {

	private final String TAG = this.getClass().getCanonicalName();

	public List<JsonWeather> parseJsonStream(InputStream inputStream)
			throws Exception {

		List<JsonWeather> jsonWeatherObjects = new LinkedList<>();

		JSONObject weatherData = new JSONObject(convertToString(inputStream));

		int cod = weatherData.optInt(JsonWeather.cod_JSON);

		// Return null if location could not be found or any other error occurred.
		if (cod != 200)
			return null;

		String name = weatherData.optString(JsonWeather.name_JSON);
		int id = weatherData.optInt(JsonWeather.id_JSON);
		long dt = weatherData.optLong(JsonWeather.dt_JSON);
		String base = weatherData.optString(JsonWeather.base_JSON);

		Wind wind = parseWind(weatherData.getJSONObject(JsonWeather.wind_JSON));
		Main main = parseMain(weatherData.getJSONObject(JsonWeather.main_JSON));
		List<Weather> weatherObjects = parseWeather(weatherData
				.getJSONArray(JsonWeather.weather_JSON));
		Sys sys = parseSys(weatherData.getJSONObject(JsonWeather.sys_JSON));

		jsonWeatherObjects.add(new JsonWeather(sys, base, main, weatherObjects,
				wind, dt, id, name, cod));

		return jsonWeatherObjects;
	}

	private Wind parseWind(JSONObject windData) throws JSONException {
		Wind wind = new Wind();
		wind.setSpeed(windData.optDouble(Wind.speed_JSON));
		wind.setDeg(windData.optDouble(Wind.deg_JSON));
		return wind;
	}

	private Main parseMain(JSONObject mainData) throws JSONException {
		Main main = new Main();
		main.setTemp(mainData.optDouble(Main.temp_JSON));
		main.setTempMin(mainData.optDouble(Main.tempMin_JSON));
		main.setTempMax(mainData.optDouble(Main.tempMax_JSON));
		main.setPressure(mainData.optDouble(Main.pressure_JSON));
		main.setSeaLevel(mainData.optDouble(Main.seaLevel_JSON));
		main.setGrndLevel(mainData.optDouble(Main.grndLevel_JSON));
		main.setHumidity(mainData.optLong(Main.humidity_JSON));
		return main;
	}

	private List<Weather> parseWeather(JSONArray weatherData)
			throws JSONException {
		List<Weather> weatherObjects = new LinkedList<Weather>();
		for (int i = 0; i < weatherData.length(); i++) {
			JSONObject weatherDataObject = weatherData.getJSONObject(i);
			Weather weatherObject = new Weather();
			weatherObject.setDescription(weatherDataObject
					.optString(Weather.description_JSON));
			weatherObject.setIcon(weatherDataObject.optString(Weather.icon_JSON));
			weatherObject.setId(weatherDataObject.optLong(Weather.id_JSON));
			weatherObject.setMain(weatherDataObject.optString(Weather.main_JSON));
			weatherObjects.add(weatherObject);
		}
		return weatherObjects;
	}

	private Sys parseSys(JSONObject sysData) throws JSONException {
		Sys sys = new Sys();
		sys.setCountry(sysData.optString(Sys.country_JSON));
		sys.setMessage(sysData.optDouble(Sys.message_JSON));
		sys.setSunrise(sysData.optLong(Sys.sunrise_JSON));
		sys.setSunset(sysData.optLong(Sys.sunset_JSON));
		return sys;
	}

	private String convertToString(InputStream inputStream) {
		String jsonData = "";
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
				jsonData += line + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		Log.v(TAG, "Retrieved weather data: " + jsonData);
		return jsonData;
	}
}
