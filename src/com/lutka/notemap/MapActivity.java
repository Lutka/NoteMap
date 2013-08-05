package com.lutka.notemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Dialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
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

public class MapActivity extends SherlockFragmentActivity implements OnMapClickListener, OnInfoWindowClickListener, OnMapLongClickListener, OnMarkerDragListener, OnCameraChangeListener, OnMarkerClickListener

{
	public GoogleMap googleMap;
	ActionMode actionMode = null;
	
	ArrayList<Note> listOfNotes = new ArrayList<Note>();
	HashMap<Marker, Note> hashMapOfNotes = new HashMap<Marker, Note>();
	
	Note openedNote = null;
	
	final int REQUEST_EDIT = 1;
	
	final String FILE_NAME = "notes.json";
	
	private int currentZoom = 10;
	
	public static MapActivity instance;
	
	boolean addingNote = false;
		
	//savedInstanceState - there are parameters which are saved from previous instance of this activity eg.particular chosen or inputed values
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		instance = this;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
		//when activity is created the map has to be set
		setupMaps();
		// to load notes from file
		try
		{
			loadFromFile();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu)
	{
		super.onCreateOptionsMenu(menu);		
		getSupportMenuInflater().inflate(R.menu.activity_map, menu);
		
		if(addingNote)
		{
			menu.findItem(R.id.action_cancel).setVisible(true);
			menu.findItem(R.id.action_create).setVisible(false);
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
		}
				
		// "this" is used to have the "implements onMyLocationChangeListener" as a parameter
		//googleMap.setOnMyLocationChangeListener(this); 
		googleMap.setOnMapClickListener(this);
		//googleMap.setOnMarkerClickListener(this);
		googleMap.setOnInfoWindowClickListener(this); //infoWindow - chmurka z notatka
		
		googleMap.setOnMapLongClickListener(this);
		googleMap.setOnMarkerDragListener(this);
		googleMap.setOnMarkerClickListener(this);
		googleMap.setOnCameraChangeListener(this);
		
		googleMap.setOnMapClickListener(this);
		
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_create:
			{
				addingNote = true;
				Toast.makeText(this, "Tap on the map to add note", Toast.LENGTH_SHORT).show();
				invalidateOptionsMenu();
				return true;
			}
			case R.id.action_cancel:
			{
				addingNote = false;
				invalidateOptionsMenu();
				return true;
			}
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
		if (addingNote == true)
		{
			onMapLongClick(location);			
			addingNote = false;
		}				

		if (actionMode != null)
		{
			actionMode.finish();
		}
	}
	
	/**
	 * Called when a user wants to add a note
	 */
	public void addNote(Note note)
	{
		listOfNotes.add(note);		
		Marker mapMarker = note.addToMap(googleMap);
		
		// link note with its corresponding marker
		hashMapOfNotes.put(mapMarker, note);
		note.updateMarker();
		
		invalidateOptionsMenu();
	}
	
	/*
	 * Removes note from the list
	 */
	public void deleteNote(Note note)
	{
		listOfNotes.remove(note);
		
		Marker marker = note.noteMarker;
		hashMapOfNotes.remove(marker);
		note.removeFromMap();
	}

	// what happen when a info related to marker is clicked
	@Override
	public void onInfoWindowClick(Marker marker)
	{
		Note clickedNote = hashMapOfNotes.get(marker);
		openNote(clickedNote);	
	}
		
	/**
	 * Opens a notes editor
	 */
	public void openNote(Note note)
	{
		if (actionMode != null) 
			actionMode.finish();
		
		// intent has a bundle and by intent.putExtra it allows to put values into the bundle
		Intent intent = new Intent(this, NoteActivity.class);
		
		// puts values into the bundle
		intent.putExtra(NoteActivity.EXTRA_NOTE_TITLE, note.getNoteTitle());
		intent.putExtra(NoteActivity.EXTRA_NOTE_SUBTITLE, note.getNoteSubTitle());
		intent.putExtra(NoteActivity.EXTRA_NOTE_CONTENT, note.getNoteDestription());
		//intent.putExtra(NoteActivity.EXTRA_CAMERA_ZOOM, note.getNoteZoom());
		
		openedNote = note;
		
		// start Activity, eg.edit note, and returns the new values (updated note)
		startActivityForResult(intent, REQUEST_EDIT);
	}
	
	
	// is called when activity noteEditor exists
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		Log.d(toString(), "on activity result was called with request code: "+requestCode);
		
		// to make sure that this is a note that has been edited
		if(requestCode == REQUEST_EDIT)
		{
			// to make sure that the note was actually saved, Result_ok comes form NoteActivity.savedNote()
			if(resultCode == RESULT_OK)
			{				
				// to make sure that activity is not opened any time apart when the note has actually been opened
				if(openedNote != null)
				{
					// create a basket in the order to get values which were put into bundle before - when a note was saved
					Bundle bundle = data.getExtras();
					
					if(bundle != null)
					{
						String title = bundle.getString(NoteActivity.EXTRA_NOTE_TITLE);
						String content = bundle.getString(NoteActivity.EXTRA_NOTE_CONTENT);
						String subtitle = bundle.getString(NoteActivity.EXTRA_NOTE_SUBTITLE);
						
						// updated made changes title and the content of a note
												
						// openedNote.setNoteTitle(title);
						if(!content.isEmpty() || !subtitle.isEmpty())
						{
							openedNote.setNoteDestription(content);
							openedNote.setNoteSubTitle(subtitle);
							openedNote.updateMarker();
						}
						else
						{
							deleteNote(openedNote);								
						}
						
						openedNote = null;
						
						// it saves all notes to file
						try
						{
							saveToFile();
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		// here we have to run the super method which is onActivityResult before it was override 
		//to be sure that the onActivityResult will work so, the app won't crash - Has to be there!! 
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onDestroy() 
	{
		instance = null;
		super.onDestroy();
	}
	
	public Note getOpenedNote() 
	{
		return openedNote;
	}
	
	public JSONArray exportNotes ()
	{
		JSONArray jsonArray = new JSONArray();
		
		for(int i = 0; i < listOfNotes.size(); i++)
		{
			try
			{
				jsonArray.put(listOfNotes.get(i).exportNote());
			} catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return jsonArray;		
	}
	
	public void importNotes(JSONArray jsonArray)
	{
		for(int i = 0; i< jsonArray.length(); i++)
		{
			try
			{
				Note note = new Note (jsonArray.getJSONObject(i));
				addNote(note);
			} catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	// writes all notes to file
	public void saveToFile() throws IOException
	{
		OutputStreamWriter out = new OutputStreamWriter(openFileOutput(FILE_NAME, MODE_PRIVATE));
		out.write(exportNotes().toString());
		out.close();
	}
	
	public void loadFromFile() throws IOException, JSONException
	{
		InputStream inStream = openFileInput(FILE_NAME);
		
		if(inStream != null)
		{
			InputStreamReader fileReader = new InputStreamReader(inStream);			
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			String line;
			StringBuilder stringBuilder = new StringBuilder();
			
			while((line = bufferedReader.readLine()) != null)
			{
				// add line to stringBuilder
				stringBuilder.append(line);
			}
			inStream.close();
			importNotes(new JSONArray(stringBuilder.toString()));
		}
		
	}
	@Override
	public void onMapLongClick(LatLng location)
	{
		Note newNote = new Note ("Note", "", "", location);	
		
		addNote(newNote);
		newNote.findNoteAddress(this, currentZoom);
		openNote(newNote);		
	}
	
	@Override
	public void onMarkerDrag(Marker marker)
	{
		// TODO Auto-generated method stub		
	}
	
	// when marker was dragged
	@Override
	public void onMarkerDragEnd(Marker marker)
	{
		Note note = hashMapOfNotes.get(marker);
		note.noteLocation = marker.getPosition();
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
		final Note note = hashMapOfNotes.get(marker);
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
