package com.lutka.notemap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;

public class NoteListActivity extends NoteCollectionActivity implements OnItemClickListener, OnItemLongClickListener
{
	private ListView listView;
	
	boolean sortAlphabeticly = false;
	boolean sortByDate = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_note_list);
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		listView = (ListView) findViewById(android.R.id.list);		
		listView.setOnItemClickListener(this);

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
		listView.setAdapter(new NoteListAdapter(this, sortList(true)));
	}
/*	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.note_list, menu);
		return true;
	}*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_sort_alphabeticly:
			sortList(true);
			//sortowanie
			return true;
			
		case R.id.action_sort_byId:
			//sortowanie
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private List<Note> sortList(boolean alphabetically)
	{
		return sortList(listOfNotes, alphabetically);
	}

	private List<Note> sortList(Collection<Note> listOfNotes, boolean alphabetically)
	{
		List<Note> list = new ArrayList<Note>(listOfNotes);
		if (alphabetically) Collections.sort(list);
		else
		{
			Collections.sort(list, new Comparator<Note>()
			{

				@Override
				public int compare(Note lhs, Note rhs)
				{
					return lhs.id.compareTo(rhs.id);
				}
			});
		}
		
		return list;
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

}
