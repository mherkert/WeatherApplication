package vandy.mooc.views;

import vandy.mooc.R;
import vandy.mooc.aidl.WeatherData;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WeatherDataView extends RelativeLayout {

	private Context mContext;
	private TextView mName;
	private TextView mTemp;
	private ImageView mIcon;

	private TextView mSpeed;
	private ImageView mDeg;
	private TextView mHumidity;

	public WeatherDataView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.weather_data, this);

		mName = (TextView) findViewById(R.id.weather_name);
		mTemp = (TextView) findViewById(R.id.weather_temp);
		mIcon = (ImageView) findViewById(R.id.weather_icon);
		mSpeed = (TextView) findViewById(R.id.weather_speed);
		mDeg = (ImageView) findViewById(R.id.weather_deg);
		mHumidity = (TextView) findViewById(R.id.weather_humidity);
	}

	public void clear() {
		setVisibility(INVISIBLE);
	}

	public void setWeatherData(WeatherData data) {
		mName.setText(data.mName + ", " + data.mCountry);
		mTemp.setText("" + round(data.mTemp) + "°C");
		mIcon.setImageDrawable(getResources().getDrawable(
				getDrawableId("i_" + data.mIcon), null));
		mSpeed.setText("Wind: " + round(data.mSpeed) + " m/s");
		mHumidity.setText("Humidity: " + data.mHumidity + "%");
		setVisibility(VISIBLE);
	}

	private int getDrawableId(String name) {
		int drawableId = getResources().getIdentifier(name, "drawable",
				mContext.getPackageName());
		return drawableId;
	}

	private long round(double d) {
		return Math.round(d);
	}
}
