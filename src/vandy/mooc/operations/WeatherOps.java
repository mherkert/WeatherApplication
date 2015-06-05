package vandy.mooc.operations;

import vandy.mooc.activities.WeatherActivity;


public interface WeatherOps {

    public void bindService();

    public void unbindService();

    public void getCurrentWeatherSync(String location);

    public void getCurrentWeatherAsync(String location);

    public void onConfigurationChange(WeatherActivity activity);
}
