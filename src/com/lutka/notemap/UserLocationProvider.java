package com.lutka.notemap;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

public class UserLocationProvider
	implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener
{
	public static interface UserLocationListener
	{
		void onLocationAcquired(LatLng latLng);
	}
	
	public static final String
		PREF_USER_LAT = "user_latitude",
		PREF_USER_LONG = "user_longitude",
		PREF_FILE_NAME = "locationNoteMap.dat";
	private static String LOG_TAG = UserLocationProvider.class.getSimpleName();
	
	private final Context context;
	private final Set<UserLocationListener> locationListeners = new HashSet<UserLocationListener>();
	private LatLng lastKnownLocation = null;

	private LocationClient locationClient;
	
	public UserLocationProvider(Context context, LatLng lastKnownLocation)
	{
		super();
		this.context = context;
		this.lastKnownLocation = lastKnownLocation;
	}
	
	public UserLocationProvider(Context context)
	{
		this(context, readLastLocation(context));
	}
	
	/**
	 * Gets last user location and starts polling current GPS location
	 * @param userLocationListener listener for user location, 
	 * will be called multiple times: first with cached location, 
	 * then again when current location is acquired.
	 */
	public void getUserLocation(UserLocationListener userLocationListener)
	{
		if (lastKnownLocation != null)
			userLocationListener.onLocationAcquired(lastKnownLocation);
		
		synchronized (locationListeners)
		{
			locationListeners.add(userLocationListener);
		}
		locationClient = new LocationClient(context, this, this);
		locationClient.connect();
	}
	
	/**
	 * Read last known location from SharedPreferences
	 */
	private static LatLng readLastLocation(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		if (preferences.contains(PREF_USER_LAT))
		{
			return new LatLng(preferences.getFloat(PREF_USER_LAT, 0f), preferences.getFloat(PREF_USER_LONG, 0f));
		}
		else return null;		
	}
	
	private void saveLastLocation()
	{
		saveLastLocation(lastKnownLocation);
	}
	
	private void saveLastLocation(LatLng latLng)
	{
		context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
			.edit()
			.putFloat(PREF_USER_LAT, (float) latLng.latitude)
			.putFloat(PREF_USER_LONG, (float) latLng.longitude)
			.commit();	
	}
	
	public LatLng getLastKnownLocation()
	{
		return lastKnownLocation;
	}

	@Override
	public void onLocationChanged(Location location)
	{
		Log.i(LOG_TAG, "User location acquired, accuracy "+String.valueOf(location.getAccuracy()));
		
		this.lastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
		saveLastLocation();
		
		synchronized (locationListeners)
		{
			for (UserLocationListener listener : locationListeners)
				listener.onLocationAcquired(lastKnownLocation);
			locationListeners.clear();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0)
	{
		Log.e(LOG_TAG, "Location Client connection failed");		
	}

	@Override
	public void onConnected(Bundle arg0)
	{
		LocationRequest locationRequest = new LocationRequest().setNumUpdates(1).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		locationClient.requestLocationUpdates(locationRequest, this);
		Log.i(LOG_TAG, "Location Client connected");
	}

	@Override
	public void onDisconnected()
	{
		Log.i(LOG_TAG, "Location Client disconnected");		
	}
}
