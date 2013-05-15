package com.sjsu.yelpSearch;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.sjsu.yelp.Yelp;
import com.yelp.parcelgen.JsonUtil;

public abstract class DashboardActivity extends Activity implements
		LocationListener {
	private Yelp mYelp;
	private LocationManager locationManager;
	String longitude, latitude;
	private Location loc;
	public static final int DIALOG_PROGRESS = 42;
	private String APP_ID = "250193238458695";
	private Facebook fb;
	private AsyncFacebookRunner mAsyncRunner;
	String FILENAME = "AndroidSSO_data";
	private SharedPreferences mPrefs;
	private YelpAuth y;
	private static int count = 0;

	private void countAuth() {
		if (count == 1) {
			fb = new Facebook(APP_ID);
			mAsyncRunner = new AsyncFacebookRunner(fb);
			loginToFacebook();
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		y = new YelpAuth();
		mYelp = new Yelp(y.getYelpConsumerKey(), y.getYelpConsumerSecret(),
				y.getYelpToken(), y.getYelpTokenSecret());
		count++;
		countAuth();

	}

	@Override
    public void onConfigurationChanged(Configuration newConfig) {
//		if(count == 1) count++;
		super.onConfigurationChanged(newConfig);
        

    }
	@SuppressWarnings("deprecation")
	private void loginToFacebook() {
		mPrefs = getPreferences(MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);
		if (access_token != null) {
			fb.setAccessToken(access_token);
		}
		if (expires != 0) {
			fb.setAccessExpires(expires);
		}
		if (!fb.isSessionValid()) {
			fb.authorize(this, new String[] { "email", "publish_stream" },
					new DialogListener() {
						@Override
						public void onCancel() {
							// Function to handle cancel event
							count=0;
							finish();
							
						}

						@Override
						public void onComplete(Bundle values) {
							// Function to handle complete event
							// Edit Preferences and update facebook acess_token
							SharedPreferences.Editor editor = mPrefs.edit();
							editor.putString("access_token",
									fb.getAccessToken());
							editor.putLong("access_expires",
									fb.getAccessExpires());
							editor.commit();
						}

						@Override
						public void onError(DialogError error) {
							// Function to handle error
							
							count = 0;finish();
							
						}

						@Override
						public void onFacebookError(FacebookError fberror) {
							// Function to handle Facebook errors
							count = 0;finish();

						}

					});
		}
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onRestart() {
		super.onRestart();
	}

	protected void onResume() {
		super.onResume();
	}

	protected void onStart() {
		super.onStart();
	}

	protected void onStop() {
		super.onStop();
	}

	public void onClickHome(View v) {
		goHome(this);
	}

	public void onClickSearch(View v) {
		goSearch(this);
	}

	public void onClickAbout(View v) {
		goAbout(this);
	}

	public void onClickFeature(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.home_btn_feature1:
			System.out.println(v.getContext().getString(id));
			search(v.getContext().getString(R.string.description_feature1));
			break;
		case R.id.home_btn_feature2:
			search(v.getContext().getString(R.string.description_feature2));
			break;
		case R.id.home_btn_feature3:
			search(v.getContext().getString(R.string.description_feature3));
			break;
		case R.id.home_btn_feature4:
			search(v.getContext().getString(R.string.description_feature4));
			break;
		case R.id.home_btn_feature5:
			search(v.getContext().getString(R.string.description_feature5));
			break;
		case R.id.home_btn_feature6:
			search(v.getContext().getString(R.string.description_feature6));
			break;
		case R.id.home_btn_feature7:
			search(v.getContext().getString(R.string.description_feature7));
			break;
		case R.id.home_btn_feature8:
			search(v.getContext().getString(R.string.description_feature8));
			break;
		case R.id.home_btn_feature9:
			search(v.getContext().getString(R.string.description_feature9));
			break;
		case R.id.home_btn_feature10:
			search(v.getContext().getString(R.string.description_feature10));
			break;
		case R.id.home_btn_feature11:
			search(v.getContext().getString(R.string.description_feature11));
			break;
		case R.id.home_btn_feature12:
			search(v.getContext().getString(R.string.description_feature12));
			break;
		default:
			break;
		}

	}

	public void search(String terms) {
		System.out.println("Inside Search" + terms);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		System.out.println("Inside Search: locationmanager" + locationManager);
		loc = locationManager.getLastKnownLocation("gps");
		String message = String.format(
				"Current Location \n Longitude: %1$s \n Latitude: %2$s",
				loc.getLongitude(), loc.getLatitude());
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		longitude = String.valueOf(loc.getLongitude());
		System.out.println("Inside button longitude is " + longitude);
		latitude = String.valueOf(loc.getLatitude());
		System.out.println("Inside button latitude is " + latitude);
		showDialog(DIALOG_PROGRESS);
		new AsyncTask<String, Void, ArrayList<Business>>() {
			@Override
			protected ArrayList<Business> doInBackground(String... params) {
				String result = mYelp.search(params[0],
						Double.parseDouble(params[1]),
						Double.parseDouble(params[2]));
				System.out.println("Result" + result);
				try {
					JSONObject response = new JSONObject(result);
					if (response.has("businesses")) {
						return JsonUtil.parseJsonList(
								response.getJSONArray("businesses"),
								Business.CREATOR);
					}
				} catch (JSONException e) {
					return null;
				}
				return null;
			}

			@Override
			protected void onPostExecute(ArrayList<Business> businesses) {
				onSuccess(businesses);
			}

		}.execute(terms, latitude, longitude);
	}

	public void onSuccess(ArrayList<Business> businesses) {
		dismissDialog(DIALOG_PROGRESS);
		if (businesses != null) {
			Intent intent = new Intent(DashboardActivity.this,
					BusinessesActivity.class);
			intent.putParcelableArrayListExtra(
					BusinessesActivity.EXTRA_BUSINESSES, businesses);
			startActivity(intent);
		} else {
			Toast.makeText(this, "An error occured during search",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		if (id == DIALOG_PROGRESS) {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("Loading...");
			return dialog;
		} else {
			return null;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		location.getLatitude();
		location.getLongitude();
		String myLocation = "Latitude = " + location.getLatitude()
				+ " Longitude = " + location.getLongitude();
		System.out.println("Location is " + myLocation);
		Log.e("MY CURRENT LOCATION", myLocation);
	}

	public void goSearch(Context context) {
		final Intent intent = new Intent(context, SearchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	public void goAbout(Context context) {
		final Intent intent = new Intent(context, AboutActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	public void goHome(Context context) {
		final Intent intent = new Intent(context, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	public void setTitleFromActivityLabel(int textViewId) {
		TextView tv = (TextView) findViewById(textViewId);
		if (tv != null)
			tv.setText(getTitle());
	}

	public void toast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	public void trace(String msg) {
		Log.d("Demo", msg);
		toast(msg);
	}

}
