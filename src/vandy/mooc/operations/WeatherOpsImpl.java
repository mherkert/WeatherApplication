package vandy.mooc.operations;

import java.lang.ref.WeakReference;
import java.util.List;

import vandy.mooc.activities.WeatherActivity;
import vandy.mooc.aidl.WeatherCall;
import vandy.mooc.aidl.WeatherData;
import vandy.mooc.aidl.WeatherRequest;
import vandy.mooc.aidl.WeatherResults;
import vandy.mooc.services.WeatherServiceAsync;
import vandy.mooc.services.WeatherServiceSync;
import vandy.mooc.utils.GenericServiceConnection;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

public class WeatherOpsImpl implements WeatherOps {

	protected final String TAG = getClass().getSimpleName();

	private WeakReference<WeatherActivity> mActivity;

	private GenericServiceConnection<WeatherCall> mServiceConnectionSync;

	private GenericServiceConnection<WeatherRequest> mServiceConnectionAsync;

	/**
	 * Use this handler to post runnables to the UI as the Activity might be
	 * destroyed during configuration changes.
	 */
	private final Handler mDisplayHandler = new Handler();

	protected List<WeatherData> mResults;

	public WeatherOpsImpl(WeatherActivity activity) {
		mActivity = new WeakReference<>(activity);

		mServiceConnectionSync = new GenericServiceConnection<WeatherCall>(
				WeatherCall.class);

		mServiceConnectionAsync = new GenericServiceConnection<WeatherRequest>(
				WeatherRequest.class);
	}

	private final WeatherResults.Stub mWeatherResults = new WeatherResults.Stub() {

		@Override
		public void sendResults(final List<WeatherData> weatherDataList)
				throws RemoteException {

			mDisplayHandler.post(new Runnable() {
				public void run() {
					mResults = weatherDataList;
					mActivity.get().displayResults(weatherDataList, null);
				}
			});
		}

	};

	@Override
	public void bindService() {
		Log.d(TAG, "calling bindService()");

		if (mServiceConnectionSync.getInterface() == null) {
			Log.d(TAG, "Bind service to WeatherServiceSync");
			mActivity
					.get()
					.getApplicationContext()
					.bindService(
							WeatherServiceSync.makeIntent(mActivity.get()),
							mServiceConnectionSync, Context.BIND_AUTO_CREATE);
		}
		if (mServiceConnectionAsync.getInterface() == null) {
			Log.d(TAG, "Bind service to WeatherServiceAsync");
			mActivity
					.get()
					.getApplicationContext()
					.bindService(
							WeatherServiceAsync.makeIntent(mActivity.get()),
							mServiceConnectionAsync, Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	public void unbindService() {
		if (mActivity.get().isChangingConfigurations())
			Log.d(TAG,
					"just a configuration change - unbindService() not called");
		else {
			Log.d(TAG, "calling unbindService()");

			// Unbind the Async Service if it is connected.
			if (mServiceConnectionAsync.getInterface() != null)
				mActivity.get().getApplicationContext()
						.unbindService(mServiceConnectionAsync);

			// Unbind the Sync Service if it is connected.
			if (mServiceConnectionSync.getInterface() != null)
				mActivity.get().getApplicationContext()
						.unbindService(mServiceConnectionSync);
		}

	}

	@Override
	public void getCurrentWeatherAsync(String location) {
		final WeatherRequest weatherRequest = mServiceConnectionAsync
				.getInterface();

		if (weatherRequest != null) {
			try {
				weatherRequest.getCurrentWeather(location, mWeatherResults);
			} catch (RemoteException e) {
				Log.e(TAG, "RemoteException:" + e.getMessage());
			}
		} else {
			Log.d(TAG, "weatherRequest was null.");
		}
	}

	@Override
	public void getCurrentWeatherSync(String location) {
		final WeatherCall weatherCall = mServiceConnectionSync.getInterface();

		if (weatherCall != null) {
			new AsyncTask<String, Void, List<WeatherData>>() {
				/**
				 * Location for which we're trying to retrieve weather data.
				 */
				private String mLocation;

				protected List<WeatherData> doInBackground(String... locations) {
					try {
						mLocation = locations[0];
						return weatherCall.getCurrentWeather(mLocation);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					return null;
				}

				/**
				 * Display the results in the UI Thread.
				 */
				protected void onPostExecute(List<WeatherData> weatherDataList) {
					mResults = weatherDataList;
					mActivity.get().displayResults(weatherDataList,
							"no weather data for " + mLocation + " found");
				}
			}.execute(location);
		} else {
			Log.d(TAG, "weatherCall was null.");
		}
	}

	@Override
	public void onConfigurationChange(WeatherActivity activity) {
		Log.d(TAG, "onConfigurationChange() called");

		// Reset the mActivity WeakReference.
		mActivity = new WeakReference<>(activity);

		updateResultsDisplay();
	}

	private void updateResultsDisplay() {
		if (mResults != null)
			mActivity.get().displayResults(mResults, null);
	}

}
