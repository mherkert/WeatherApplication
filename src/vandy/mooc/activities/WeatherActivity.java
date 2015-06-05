package vandy.mooc.activities;

import java.util.List;

import vandy.mooc.R;
import vandy.mooc.aidl.WeatherData;
import vandy.mooc.operations.WeatherOps;
import vandy.mooc.operations.WeatherOpsImpl;
import vandy.mooc.utils.RetainedFragmentManager;
import vandy.mooc.views.WeatherDataView;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class WeatherActivity extends LifecycleLoggingActivity {

	protected final RetainedFragmentManager mRetainedFragmentManager = new RetainedFragmentManager(
			this.getFragmentManager(), TAG);

	private WeatherOps mWeatherOps;

	private EditText mEditText;
	private WeatherDataView mWeatherDataView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Always call super class for necessary
		// initialization/implementation.
		super.onCreate(savedInstanceState);

		// Get references to the UI components.
		setContentView(R.layout.weather_activity);

		mEditText = (EditText) findViewById(R.id.location);
		mWeatherDataView = (WeatherDataView) findViewById(R.id.weatherData);

		// Handle any configuration change.
		handleConfigurationChanges();
	}

	@Override
	protected void onDestroy() {
		// Unbind from the Service.
		mWeatherOps.unbindService();

		// Always call super class for necessary operations when an
		// Activity is destroyed.
		super.onDestroy();
	}

	/**
	 * Handle hardware reconfigurations, such as rotating the display.
	 */
	protected void handleConfigurationChanges() {
		// If this method returns true then this is the first time the
		// Activity has been created.
		if (mRetainedFragmentManager.firstTimeIn()) {
			Log.d(TAG, "First time onCreate() call");

			// Create the WeatherOps object one time. The "true"
			// parameter instructs WeatherOps to use the
			// DownloadImagesBoundService.
			mWeatherOps = new WeatherOpsImpl(this);

			// Store the WeatherOps into the RetainedFragmentManager.
			mRetainedFragmentManager.put("WEATHER_OPS_STATE", mWeatherOps);

			// Initiate the service binding protocol (which may be a
			// no-op, depending on which type of DownloadImages*Service is
			// used).
			mWeatherOps.bindService();
		} else {
			// The RetainedFragmentManager was previously initialized,
			// which means that a runtime configuration change
			// occured.

			Log.d(TAG, "Second or subsequent onCreate() call");

			// Obtain the WeatherOps object from the
			// RetainedFragmentManager.
			mWeatherOps = mRetainedFragmentManager.get("WEATHER_OPS_STATE");

			// This check shouldn't be necessary under normal
			// circumtances, but it's better to lose state than to
			// crash!
			if (mWeatherOps == null) {
				// Create the WeatherOps object one time. The "true"
				// parameter instructs WeatherOps to use the
				// DownloadImagesBoundService.
				mWeatherOps = new WeatherOpsImpl(this);

				// Store the WeatherOps into the RetainedFragmentManager.
				mRetainedFragmentManager.put("WEATHER_OPS_STATE", mWeatherOps);

				// Initiate the service binding protocol (which may be
				// a no-op, depending on which type of
				// DownloadImages*Service is used).
				mWeatherOps.bindService();
			} else
				// Inform it that the runtime configuration change has
				// completed.
				mWeatherOps.onConfigurationChange(this);
		}
	}

	/**
	 * Initiate the retrieval of weather data when the user hits the 'Get
	 * Weather Sync' button.
	 * 
	 * @param v
	 */
	public void getCurrentWeatherSync(View v) {
		// Get the location entered by the user.
		final String location = mEditText.getText().toString();

		// Reset the display.
		resetDisplay();

		if (location.isEmpty())
			showToast(getString(R.string.enter_location_message));
		else
			// Synchronously retrieve weather data for given location.
			mWeatherOps.getCurrentWeatherSync(location);
	}

	/**
	 * Initiate the retrieval of weather data when the user hits the 'Get
	 * Weather Async' button.
	 * 
	 * @param v
	 */
	public void getCurrentWeatherAsync(View v) {
		// Get the location entered by the user.
		final String location = mEditText.getText().toString();

		// Reset the display.
		resetDisplay();
		if (location.isEmpty())
			showToast(getString(R.string.enter_location_message));
		else
			// Asynchronously retrieve weather data for given location.
			mWeatherOps.getCurrentWeatherAsync(location);
	}

	public void displayResults(List<WeatherData> results, String errorMessage) {
		if (results == null || results.size() == 0){
			Log.d(TAG, "displayResults() = " + errorMessage);
			showToast("Unable to find weather data.");
		}
		else {
			Log.d(TAG, "displayResults() = " + results.get(0).toString());

			// Set/change data set.
			mWeatherDataView.clear();
			mWeatherDataView.setWeatherData(results.get(0));
		}
	}

	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void resetDisplay() {
		hideKeyboard(this, mEditText.getWindowToken());
		mWeatherDataView.clear();
	}

	private void hideKeyboard(Activity activity, IBinder windowToken) {
		InputMethodManager mgr = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(windowToken, 0);
	}
}
