package com.lutka.notemap;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;


public class AddressFinder extends AsyncTask<LatLng, Void, Address> 
{
	public interface OnAddressFoundListener
	{
		void onAddressFound(Address address);
	}
	private final Context context;
	private OnAddressFoundListener onAddressFoundListener;
	
	public AddressFinder setOnAddressFoundListener(OnAddressFoundListener onAddressFoundListener) 
	{
		this.onAddressFoundListener = onAddressFoundListener;
		return this;
	}
	
	public AddressFinder(Context context) 
	{
		this.context = context;
	}

	@Override
	protected Address doInBackground(LatLng... params) 
	{
		LatLng latLng = params[0];
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		
		List<Address> listOfAddress = null;
		try {
			listOfAddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(!listOfAddress.isEmpty())
		{
			return listOfAddress.get(0);
		}
						
		return null;
	}
	
	@Override
	protected void onPostExecute(Address result) 
	{	
		onAddressFoundListener.onAddressFound(result);
		super.onPostExecute(result);
	}

}
