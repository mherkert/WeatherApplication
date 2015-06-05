package vandy.mooc.cache;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;
import vandy.mooc.aidl.WeatherData;

/**
 * TODO evict entries periodically or when cache is accessed
 *
 */
public class WeatherDataCache implements Cache<String, List<WeatherData>> {

	private final String TAG = getClass().getSimpleName();

	private Map<String, WeatherDataCacheObject> mWeatherDataCache;

	private static WeatherDataCache instance;

	private class WeatherDataCacheObject {
		public long expires;
		public List<WeatherData> value;

		protected WeatherDataCacheObject(List<WeatherData> value, long timeToLive) {
			this.value = value;
			this.expires = now() + timeToLive;
		}
	}

	public static WeatherDataCache getInstance() {
		if (instance == null) {
			synchronized (WeatherDataCache.class) {
				if (instance == null) {
					instance = new WeatherDataCache();
				}
			}
		}
		return instance;
	}

	private WeatherDataCache() {
		mWeatherDataCache = new ConcurrentHashMap<String, WeatherDataCache.WeatherDataCacheObject>();
	}

	@Override
	public void put(String key, List<WeatherData> value, long timeToLive) {
		mWeatherDataCache.put(getCaseInsensitiveKey(key),
				new WeatherDataCacheObject(value, timeToLive));
	}

	@Override
	public List<WeatherData> get(String key) {
		String k = getCaseInsensitiveKey(key);

		synchronized (mWeatherDataCache) {
			WeatherDataCacheObject c = mWeatherDataCache.get(k);

			if (c == null){
				Log.d(TAG, "No weather data for location in cache. Return null.");
				return null;
			}
			else if (hasExpired(c)) {
				Log.d(TAG, "Weather data in cache expired. Remove entry and return null.");
				mWeatherDataCache.remove(k);
				return null;
			} else {
				Log.d(TAG, "Return weather data from cache.");
				return c.value;
			}
		}
	}

	private boolean hasExpired(WeatherDataCacheObject c) {
		return now() >= c.expires;
	}

	private long now() {
		return System.currentTimeMillis();
	}

	private String getCaseInsensitiveKey(String key) {
		return key.toLowerCase(Locale.getDefault());
	}
}
