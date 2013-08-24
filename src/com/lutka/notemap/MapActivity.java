package com.lutka.notemap;

import java.io.IOException;

import org.json.JSONException;

import android.app.Dialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MapActivity extends NoteCollectionActivity implements OnMapClickListener, OnInfoWindowClickListener, OnMapLongClickListener, OnMarkerDragListener, OnCameraChangeListener, OnMarkerClickListener

{
	public GoogleMap googleMap;
	ActionMode actionMode = null;
	
	private int currentZoom = 10;
	
	boolean addingNote = false;
		
	//savedInstanceState - there are parameters which are saved from previous instance of this activity eg.particular chosen or inputed values
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
		//when activity is created the map has to be set
		setupMaps();
		// to load notes from file
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.wtf("Map aactivity", "onResume called");
		try
		{
			loadNotes();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(Note note: listOfNotes)
		{
			if(note.isAddressEmpty())
			{
				note.findNoteAddress(this, currentZoom);
			}			
		}
	}
	
	@Override
	public void addNote(Note note)
	{
		super.addNote(note);
		note.addToMap(googleMap);
		note.updateMarker();	
	}
	
	@Override
	public void openNote(Note note)
	{
		if (actionMode != null) 
			actionMode.finish();
		
		super.openNote(note);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu)
	{
		super.onCreateOptionsMenu(menu);		
		getSupportMenuInflater().inflate(R.menu.activity_map, menu);
		
		if(addingNote)
		{
			menu.findItem(R.id.action_cancel).setVisible(true);
			menu.findItem(R.id.action_add_note_here).setVisible(true);
			menu.findItem(R.id.action_create).setVisible(false);
			menu.findItem(R.id.action_show_list_of_notes).setVisible(false);
		}
		return true;
	}

	
	/**
	 * Initialize a setup of the map and implements necessary "Listeners"
	 * 
	 */
	public void setupMaps()
	{
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
		
		// showing status
		
		if(status != ConnectionResult.SUCCESS) //google play services are not available
		{
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
			dialog.show();
		}
		
		// google play services are available
		else 
		{	
			// Getting reference to the SupportMapFragment of activity_main.xml
			SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
					
			// Getting GoogleMap object form the fragment		
			googleMap = mapFragment.getMap();		
			
			// Enabling MyLocation Layer of Google Map
			googleMap.setMyLocationEnabled(true);
			
			// Getting LocationManager object from System Service LOCATION_SERVICE
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			
			// getting a criteria object to retrieve provider
			Criteria criteria = new Criteria();
			
			if(locationManager.getBestProvider(criteria, true) != null)
			{
				// Getting the name of the best provider
				String provider = locationManager.getBestProvider(criteria, true);
				
				// Getting current location
				Location location = locationManager.getLastKnownLocation(provider);
				
				if(location != null)
				{
					onLocationChange(location);
				}
			}
			
			// "this" is used to have the "implements onMyLocationChangeListener" as a parameter
			//googleMap.setOnMyLocationChangeListener(this); 
			googleMap.setOnMapClickListener(this);
			googleMap.setOnInfoWindowClickListener(this); //infoWindow - chmurka z notatka
			
			googleMap.setOnMapLongClickListener(this);
			googleMap.setOnMarkerDragListener(this);
			googleMap.setOnMarkerClickListener(this);
			googleMap.setOnCameraChangeListener(this);
			
			googleMap.setOnMapClickListener(this);
		}		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_create:
				addingNote = true;
				Toast.makeText(this, R.string.tap_on_the_map, Toast.LENGTH_SHORT).show();
				supportInvalidateOptionsMenu();
				return true;
			
			case R.id.action_cancel:
				addingNote = false;
				supportInvalidateOptionsMenu();
				return true;
				
			case R.id.action_show_list_of_notes:
				Intent intent = new Intent(getApplicationContext(), NoteListActivity.class);
				startActivity(intent);
				return true;
			
			
			case R.id.action_add_note_here:
				if(googleMap.getMyLocation() != null)
				{
					LatLng myLocation =new LatLng(googleMap.getMyLocation().getLatitude(),
						googleMap.getMyLocation().getLongitude());
				
					onMapLongClick(myLocation);						
				}
				else
				{
					Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_SHORT).show();					
				}
				addingNote = false;
				supportInvalidateOptionsMenu();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	//that what should happen when a location change - currently not working
	
	
	//@Override
	public void onLocationChange(Location location)
	{
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		
		// create LatLng object for the current location
		LatLng latLng = new LatLng(latitude, longitude);
		
		// showing the current location in Google Map
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		
		// zoom in google map
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom));		
	}
	

	// action taken when a map was clicked
	@Override
	public void onMapClick(LatLng location)
	{
		dismissUndoDialog();
		if (addingNote == true)
		{
			onMapLongClick(location);			
			addingNote = false;
			supportInvalidateOptionsMenu();
		}				

		if (actionMode != null)
		{
			actionMode.finish();
		}
	}
	
	// what happen when a info related to marker is clicked
	@Override
	public void onInfoWindowClick(Marker marker)
	{
//		Note clickedNote = hashMapOfNotes.get(marker);
		for (Note note : listOfNotes)
		{
			if (note.getNoteMarker().equals(marker))
			{
				openNote(note);	
				break;
			}
		}
		
	}
		
	@Override
	public void onMapLongClick(LatLng location)
	{
		dismissUndoDialog();
		Note newNote = new Note ("", "", "", location);	
		
//		addNote(newNote);
//		newNote.findNoteAddress(this, currentZoom);
//		newNote.updateMarker();
		openNote(newNote);		
	}
	
	@Override
	public void onMarkerDrag(Marker marker)
	{
		// TODO Auto-generated method stub		
	}
	
	private Note getNoteByMarker(Marker marker)
	{
		for (Note note : listOfNotes)
			if (note.getNoteMarker().equals(marker)) return note;
		
		return null;
	}
	
	// when marker was dragged
	@Override
	public void onMarkerDragEnd(Marker marker)
	{
		Note note = getNoteByMarker(marker);
		if (note == null) marker.remove();
		note.setNoteLocation(marker.getPosition());
		try
		{
			saveToFile();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		note.findNoteAddress(this, currentZoom);
	}
	@Override
	public void onMarkerDragStart(Marker marker)
	{
		if (actionMode != null)
			actionMode.finish();
	}
		
	@Override
	public void onCameraChange(CameraPosition position)
	{
		if(position.zoom != currentZoom)
		{
			currentZoom = (int) position.zoom;
			// zoomOfCamera = String.valueOf(currentZoom);
		}		
	}

	@Override
	public boolean onMarkerClick(Marker marker)
	{
		dismissUndoDialog();
		final Note note = getNoteByMarker(marker);
		startActionMode(new Callback()
		{
			
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu)
			{
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode)
			{
				actionMode = null;
				
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu)
			{
				getSupportMenuInflater().inflate(R.menu.map_note_context, menu);
				actionMode = mode;
				return true;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item)
			{
				switch (item.getItemId())
				{
					case R.id.action_delete:
						deleteNote(note);
						break;
						
					case R.id.action_edit:
						openNote(note);
						break;
						
					case R.id.action_change_pin:
						note.showPinDialog(MapActivity.this, new OnItemClickListener()
						{

							@Override
							public void onItemClick(AdapterView<?> arg0,
									View arg1, int arg2, long arg3)
							{
								
								note.updateMarker();
							}
						});
						break;
						
					
					default:
						return false;
					
				}
				mode.finish();
				return true;
			}
		});
		return false;
	}

}
