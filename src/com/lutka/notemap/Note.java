package com.lutka.notemap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lutka.notemap.AddressFinder.OnAddressFoundListener;

public class Note 
{
	String noteTitle;
	String noteDestription;
	LatLng noteLocation;
	String noteSubTitle;
	Marker noteMarker = null;
	//int cameraZoom;                  
	
	String pinName = "red.png";
	
	public static final String [] pinNames = {"blue.png", "green.png", "red.png", 
											"blue1.png","green1.png", "orange.png",  
											"blue2.png", "yellow.png", "pink.png",
											"violet.png", "brown.png", "black.png","car.png", "gift.png", "key.png", "music.png", "tools.png", "shopping_cart.png" };
	
	public static Drawable getPinDrawable(Context contex, String pinName) throws IOException
	{
		InputStream inputStream = contex.getAssets().open("pins/"+pinName);
		return Drawable.createFromStream(inputStream, null);
	}
	
	public Drawable getPinDrawable(Context context) throws IOException
	{
		if(pinName == null) return null;
		return getPinDrawable(context, pinName);
	}
		
	public Note(String noteTitle, String noteSubTitle, String noteDescription,LatLng noteLocation)
	{
		this.noteTitle = noteTitle;
		this.noteSubTitle = noteSubTitle;
		this.noteDestription =  noteDescription;
		this.noteLocation = noteLocation;		
	}
	
	public Note (JSONObject jsonObject) throws JSONException
	{
		importNote(jsonObject);
	}
	
	public Marker addToMap(GoogleMap map)
	{
		/*String description = this.getNoteDestription();
		String snipper;
		if(description.length() <= 10) snipper = description;
		else snipper =  description.substring(0, 10);*/
			
		// creat a mapMarker to use it later in the order to connect a note with its marker on a map 
		noteMarker = map.addMarker(new MarkerOptions()
		.position(this.getNoteLocation()).title(this.getNoteTitle())
		.snippet(this.getNoteDestription()).draggable(true));
		
		return noteMarker;
	}
	
	public void removeFromMap ()
	{
		if (noteMarker != null)	noteMarker.remove();
	}
	
	public void updateMarker()
	{
		if(noteMarker != null)
		{
			// to make a snipper have a particular size/length 
			//String description = noteDestription;
			String subTitle = noteSubTitle;
			String content = noteDestription;
			String snipper;
			if(subTitle.length() == 0) 
				{
					if(content.length() == 0)snipper = "";
					else if (content.length() <= noteTitle.length()) snipper = content;
					else snipper =  content.substring(0, noteTitle.length());
				
				}
			else if(subTitle.length() <= noteTitle.length()) snipper = noteSubTitle;
			else snipper =  subTitle.substring(0, noteTitle.length()); //
			
			noteMarker.setTitle(noteTitle);
			noteMarker.setSnippet(snipper);	
			//change the pin only if the pin isn't default
			if(pinName != null)noteMarker.setIcon(getBitmapDescriptor());
			
			if(noteMarker.isInfoWindowShown())
			{
				noteMarker.hideInfoWindow();
				noteMarker.showInfoWindow();
			}
		}		
	}
	
	public BitmapDescriptor getBitmapDescriptor()
	{
		return BitmapDescriptorFactory.fromAsset(getPinName());
	}
	
	public String getPinName() 
	{
		if(pinName == null) return null;		
		else return "pins/"+pinName;
	}
	
	
	public LatLng getNoteLocation()
	{
		return noteLocation;
	}
	
	public String getNoteTitle()
	{
		return noteTitle;
	}
	public String getNoteSubTitle() 
	{
		return noteSubTitle;
	}
	
	public String getNoteDestription()
	{
		return noteDestription;
	}
	public void setPinName(String pinName) 
	{
		this.pinName = pinName;
		updateMarker();
	}
	
	public void setNoteTitle(String noteTitle)
	{
		this.noteTitle = noteTitle;
		updateMarker();
	}
	
	public void setNoteSubTitle(String noteSubTitle) 
	{
		this.noteSubTitle = noteSubTitle;
		updateMarker();
	}
	
	public void setNoteDestription(String noteDestription)
	{
		this.noteDestription = noteDestription;
		updateMarker();
	}
	public void setNoteLocation(LatLng noteLocation)
	{
		this.noteLocation = noteLocation;
	}
	

	public void findNoteAddress( Context context, final int zoom)
	{		
		Log.d(toString(), "Wyszukiwanie lokalizacji");
			new AddressFinder(context).
				setOnAddressFoundListener(new OnAddressFoundListener() 
			{				
				@Override
				public void onAddressFound(Address address) 
				{
					Log.d(toString(), "Address Found "+address.toString());
					String countryName = address.getCountryName();
					String subAdminArea = address.getSubAdminArea();
					String addressLine_0 = address.getAddressLine(0);
					String addressLine_1 = address.getAddressLine(1);
					
			/*		if(countryName == null) countryName = "";
					if(subAdminArea == null) subAdminArea = "";
					if(addressLine_0 == null) addressLine_0 = "";
					if(addressLine_1 == null) addressLine_1 = "";*/
					
					
					if(zoom >= 12)
					{
						if(subAdminArea != null && countryName!=null) noteTitle = String.format("%s, %s", subAdminArea, countryName); //noteTitle = subAdminArea+", "+countryName;
						else noteTitle = String.format("%s, %s", addressLine_0, addressLine_1);
						//noteDestription =  String.valueOf(zoom);					
					}
					
					else if (zoom < 12 &&  zoom >= 4) 					
					{
						if(subAdminArea != null ) noteTitle = String.format("%s, %s",addressLine_0 , subAdminArea);
						else noteTitle = String.format("%s, %s",addressLine_0 , addressLine_1);
						//noteDestription =  String.valueOf(zoom);					
					}
					
					else
					{
						noteTitle = String.format("%s, %s",addressLine_0 , addressLine_1);
						//noteDestription =  String.valueOf(zoom);
					}
					
					/*
					// --> for setting the right parameters in title
					noteTitle = String.valueOf(zoom);
					
					noteDestription = "1)"+address.getAddressLine(0)+"\n 2) "+address.getAddressLine(1)+"\n 3) "
							+ address.getAddressLine(2)+"\n Country name: "+address.getCountryName().toUpperCase()
							+"\n Feature name: "+address.getFeatureName().toUpperCase()+"\n Country code: "
							+ address.getCountryCode().toUpperCase()+"\n SubAdminArea: "+address.getSubAdminArea().toUpperCase();				
					// <--	ends here
					 * */						
						
					updateMarker();	
					
				}
			})
			.execute(noteLocation);		
			
	}
	
	///////// 
	//JSONObject similar to bundle, but it save data as text
	//used to send data from server to the app
	public JSONObject exportNote() throws JSONException
	{		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("title", noteTitle);
		jsonObject.put("description", noteDestription);
		jsonObject.put("subTitle", noteSubTitle);
		jsonObject.put("pinName", pinName);
		jsonObject.put("latitude", noteLocation.latitude);
		jsonObject.put("longitude", noteLocation.longitude);
		return jsonObject;
	}
	
	public void importNote(JSONObject jsonObject) throws JSONException
	{
		noteTitle = jsonObject.getString("title");
		noteDestription = jsonObject.getString("description");
		noteSubTitle = jsonObject.getString("subTitle");
		
		if(jsonObject.has("pinName")) pinName = jsonObject.getString("pinName");
		
		noteLocation = new LatLng (jsonObject.getDouble("latitude")
				,jsonObject.getDouble("longitude"));	
	}

}
