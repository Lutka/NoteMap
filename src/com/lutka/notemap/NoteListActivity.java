package com.lutka.notemap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.AlteredCharSequence;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;

public class NoteListActivity extends NoteCollectionActivity implements OnItemClickListener, OnItemLongClickListener
{
	private ListView listView;
	
	int sortingOption = 1;
	Location location;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_note_list);
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		listView = (ListView) findViewById(android.R.id.list);		
		listView.setOnItemClickListener(this);
		
		/*SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
		sortAlphabeticly = sharedPreferences.getBoolean(key, false);*/	
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu)
	{
		
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.activity_note_list, menu);
		
		return true;
	}
	
	@Override
	protected void loadNotes()
	{
		super.loadNotes();
		updateList();
	}
	
	private void updateList()
	{
		// display diffrent layout when list is empty
		if(listOfNotes.isEmpty())
		{
			setContentView(R.layout.empty_list);
		}
		else
		{
			listView.setAdapter(new NoteListAdapter(this, new ArrayList<Note>(listOfNotes)));
		}
	}
	
	private void updateList(List<Note> list)
	{
		// display diffrent layout when list is empty
		if(listOfNotes.isEmpty())
		{
			setContentView(R.layout.empty_list);
		}
		else
		{
			listView.setAdapter(new NoteListAdapter(this, list));
		}
	}
	
	@Override
	protected void onNoteUpdated(Note note)
	{
		super.onNoteUpdated(note);
		updateList();
	}
	
	@Override
	public void deleteNote(Note note, boolean showUndo)
	{
		super.deleteNote(note, showUndo);
		updateList();
	}
	
	@Override
	public void addNote(Note note)
	{
		super.addNote(note);
		updateList();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	
	private void setupActionBar()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	public void refreshListOfNotes()
	{
		ListView listView =  (ListView) findViewById(android.R.id.list);
		
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);

		updateListOrder();
	}

	void updateListOrder()
	{
		listView.setAdapter(new NoteListAdapter(this, sortList(1)));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{		
		
		switch (item.getItemId())
		{
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;				
			
		case R.id.action_sort_byDate:
			menuAction(1);
			return true;	
			
		case R.id.action_sort_byDistance:
			menuAction(2);
			return true;
			
		case R.id.action_sort_alphabeticly:
			menuAction(3);
			return true;
		}
		
		updateList();
		return super.onOptionsItemSelected(item);
	}
	
	public void menuAction(int sortingOption)
	{
		List sorted;
		sorted = sortList(sortingOption);
		updateList(sorted);		
	}
	
	//sort hashSet
	private List<Note> sortList(int sortOption)
	{
		return sortList(listOfNotes, sortOption);
	}

	// sorting options
	private List<Note> sortList(Collection<Note> listOfNotes, int sortingOption)
	{
		List<Note> list = new ArrayList<Note>(listOfNotes);
		
		// ordinary sort by date = id
		if (sortingOption == 3)Collections.sort(list);
		
		// by distance
		else if(sortingOption == 2)
		{
			// compare by distance to users location
			Collections.sort(list, new Comparator<Note>()
			{
				@Override
				public int compare(Note note1, Note note2)
				{
					LatLng latLngNote1 = new LatLng(note1.latitude, note1.longitude);
					LatLng latLngNote2 = new LatLng(note2.latitude, note2.longitude);					
					
					return distanceTo(latLngNote1).compareTo(distanceTo(latLngNote2));
				}
			});
		}
		
		// alphabetically
		else
		{
		Collections.sort(list, new Comparator<Note>()
		{
			@Override
			public int compare(Note note1, Note note2)
			{
				return note1.id.compareTo(note2.id);
			}
		});
		}
		return list;
	}
	
	public Double distanceTo(LatLng point)
	{
		getLocation();
		return distance(point, new LatLng(location.getLatitude(), location.getLongitude()));
	}
	public static double distance(LatLng from, LatLng to) 
	{
	if (from == null || to == null) 
		return Double.NaN;

	   double lat1 = from.latitude,
	   	lat2 = to.latitude,
	   lon1 = from.longitude,
	   lon2 = to.longitude;
	   
	   double dLat = Math.toRadians(lat2-lat1);
	   double dLon = Math.toRadians(lon2-lon1);
	   double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	   Math.sin(dLon/2) * Math.sin(dLon/2);
	   double c = 2 * Math.asin(Math.sqrt(a));
	   return 6366000 * c;
	}
	
	public List <Note> sortListByLocation (Location location, Collection <Note> listOfNotes)
	{
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		LatLng latLng = new LatLng(latitude, longitude);
		
		
		return null;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
	{
		openNote((Note) adapterView.getItemAtPosition(position));		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,
			long id)
	{
		final Note note =  (Note) adapterView.getItemAtPosition(position);
		deleteNoteWindow(note);		
		return true;
	}
	
	public void deleteNoteWindow(final Note currentNote)
	{		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.delete_dialog_title);
		alert.setMessage(R.string.delete_note_dialog);

		alert.setNegativeButton(android.R.string.cancel,null); 
		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() 
		{
		    public void onClick(DialogInterface dialog, int whichButton) 
		    {
		    	deleteNote(currentNote, false);				
				refreshListOfNotes();
		    }
		});
		alert.show();
	}	
	
/*	@Override
	public void onDestroy()
	{
			
			Editor preferencesEditor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();		
		
			preferencesEditor.putString("valueIn", valueIn.getText().toString());
			preferencesEditor.putInt("spinnerProduct", spinnerProduct.getSelectedItemPosition());
			preferencesEditor.putInt("spinnerIn", spinnerIn.getSelectedItemPosition());
			preferencesEditor.putInt("spinnerOut", spinnerOut.getSelectedItemPosition());
			
			preferencesEditor.commit();
		
		
		super.onDestroy();
	}*/
	
	private void getLocation() {
	    // Get the location manager
	    LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
	    double lat,lon;
	    Criteria criteria = new Criteria();
	    String bestProvider = locationManager.getBestProvider(criteria, false);
	    location = locationManager.getLastKnownLocation(bestProvider);
	    LocationListener loc_listener = new LocationListener() 	    
	    {
	        public void onLocationChanged(Location l) {}
	        public void onProviderEnabled(String p) {}
	        public void onProviderDisabled(String p) {}
	        public void onStatusChanged(String p, int status, Bundle extras) {}
	    };
	    locationManager.requestLocationUpdates(bestProvider, 0, 0, loc_listener);
	    location = locationManager.getLastKnownLocation(bestProvider);
	    try {
	        lat = location.getLatitude();
	        lon = location.getLongitude();
	        
	    } catch (NullPointerException e) {
	        lat = -1.0;
	        lon = -1.0;	        
	    }
		return;
	}

}
