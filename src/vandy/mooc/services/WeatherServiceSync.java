package vandy.mooc.services;

import java.util.ArrayList;
import java.util.List;

import vandy.mooc.aidl.WeatherCall;
import vandy.mooc.aidl.WeatherData;
import vandy.mooc.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class WeatherServiceSync extends LifecycleLoggingService {

    public static Intent makeIntent(Context context) {
        return new Intent(context,
                          WeatherServiceSync.class);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mWeatherCallImpl;
    }
    
    private final WeatherCall.Stub mWeatherCallImpl =
            new WeatherCall.Stub() {
                @Override
                public List<WeatherData> getCurrentWeather(String location)
                    throws RemoteException {

                    final List<WeatherData> weatherResults = 
                        Utils.getResults(location);

                    if (weatherResults != null) {
                        Log.d(TAG, "" 
                              + weatherResults.size() 
                              + " results for Weather: " 
                              + location);

                        return weatherResults;
                    } else {
                        return new ArrayList<WeatherData>();
                    }
                }
    	};
}
