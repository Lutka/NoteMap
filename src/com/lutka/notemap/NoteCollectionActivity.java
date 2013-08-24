package com.lutka.notemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public abstract class NoteCollectionActivity extends SherlockFragmentActivity
{

	public Set<Note> listOfNotes = new HashSet<Note>();
	final int REQUEST_EDIT = 1;
	final String FILE_NAME = "notes.json";

	public NoteCollectionActivity()
	{
		super();
	}

	/**
	 * Called when a user wants to add a note
	 */
	public void addNote(Note note)
	{
		listOfNotes.add(note);					
	}

	public void deleteNote(final Note note)
	{
		deleteNote(note, true);
	}

	public void deleteNote(final Note note, boolean showUndo)
	{
		if (note == null) return;
		listOfNotes.remove(note);		
		note.removeFromMap();
		
		try
		{
			saveNotesToFile();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (showUndo && note.isEmpty() == false) showUndoButton("Note deleted", new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				addNote(note);
				try
				{
					saveNotesToFile();
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	
		
	}

	/**
	 * Opens a notes editor
	 */
	public void openNote(Note note)
	{		
		// intent has a bundle and by intent.putExtra it allows to put values into the bundle
		Intent intent = new Intent(this, NoteActivity.class);
		
		// puts values into the bundle
		intent.putExtra(NoteActivity.EXTRA_NOTE, note);
		//intent.putExtra(NoteActivity.EXTRA_CAMERA_ZOOM, note.getNoteZoom());
		
		// start Activity, eg.edit note, and returns the new values (updated note)
		startActivityForResult(intent, REQUEST_EDIT);
	}

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
				// create a basket in the order to get values which were put into bundle before - when a note was saved
				Bundle bundle = data.getExtras();
				
				if(bundle != null)
				{
					Note editedNote = (Note) bundle.getSerializable(NoteActivity.EXTRA_NOTE);
					
					// updated made changes title and the content of a note
											
					//remove old instance of note from list
					Note oldNote = null;
					for (Note note : listOfNotes)
						if (note.equals(editedNote)) 
						{
							oldNote = note;
							break;
						}
					deleteNote(oldNote, false);
					
					if(editedNote.isEmpty() == false)
					{	
						addNote(editedNote);
					}
					
					// it saves all notes to file
					try
					{
						saveNotesToFile();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		// here we have to run the super method which is onActivityResult before it was override 
		//to be sure that the onActivityResult will work so, the app won't crash - Has to be there!! 
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Deprecated
	public JSONArray exportNotes()
	{
		JSONArray jsonArray = new JSONArray();
		
		for (Note note : listOfNotes)
		{
			try
			{
				jsonArray.put(note.exportNote());
			} catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return jsonArray;		
	}

	@Deprecated
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

	@Deprecated
	public void saveNotesToFile() throws IOException
	{
		OutputStreamWriter out = new OutputStreamWriter(openFileOutput(FILE_NAME, MODE_PRIVATE));
		out.write(exportNotes().toString());
		out.close();
	}

	@Deprecated
	public void loadNotesFromFile() throws IOException, JSONException
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

	private void showUndoButton(CharSequence undoText, final OnClickListener onUndoClickListener)
	{
		final View view = findViewById(R.id.layout_undo);
		final View btnUndo = view.findViewById(R.id.btnUndo);
		TextView tvUndoText = (TextView) view.findViewById(R.id.tvUndoText);
		tvUndoText.setText(undoText);
		
		btnUndo.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				onUndoClickListener.onClick(v);
				dismissUndoDialog();
			}
		});
		
		view.setVisibility(View.VISIBLE);
	}

	protected void dismissUndoDialog()
	{
		final View view = findViewById(R.id.layout_undo);
		final View btnUndo = view.findViewById(R.id.btnUndo);
		view.setVisibility(View.GONE);
		btnUndo.setOnClickListener(null);
	}


}