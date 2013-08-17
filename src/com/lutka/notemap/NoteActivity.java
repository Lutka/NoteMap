package com.lutka.notemap;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class NoteActivity extends SherlockActivity
{
	
	public final static String EXTRA_NOTE = "note";
	
	public final static float EXTRA_CAMERA_ZOOM = 10;

	Note currentNote;
	
	// this  method is run when the note is created
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note);
		// Show the Up button in the action bar.
		setupActionBar();
		
		
		Intent intent = getIntent();
		
		if(intent != null)
		{
			Bundle bundle = intent.getExtras();
			
			if(bundle != null)
			{
				this.currentNote = (Note) bundle.getSerializable(EXTRA_NOTE);
				
				setTitle(currentNote.noteTitle);
				EditText subTitle = (EditText) findViewById(R.id.etSubTitle);
				subTitle.setText(currentNote.noteSubTitle);
				EditText editText = (EditText) findViewById(R.id.etContent);
				editText.setText(currentNote.noteDestription);
				
				// to set the cursor at the end of the word
				editText.setSelection(editText.length());
				subTitle.setSelection(subTitle.length());
			}
		}
	}
	
	void updateIcon()
	{
		ActionBar actionBar = getSupportActionBar();
		Drawable drawable = null;
		try 
		{
			drawable = currentNote.getPinDrawable(this);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(drawable == null) actionBar.setIcon(R.drawable.ic_launcher);
		else actionBar.setIcon(drawable);
	}
	
	
	void showPinDialog()
	{
		currentNote.showPinDialog(this, new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				updateIcon();
				
			}
		});		
	}
	
	private void setupActionBar()
	{		
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);	
			updateIcon();
	}
	
	//menu z sherlock
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getSupportMenuInflater().inflate(R.menu.note, menu);
		return true;
	}
	

	// this method is used for menu; in this case when icon on a top left is pressed - 
	//what should it happen	 ; menu z sherlock
	@Override  
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				break;
				
			case R.id.action_save: // save
				saveNote(); 
				finish(); 
				break;
				
			case R.id.action_undo_changes: // undo
				setResult(RESULT_CANCELED);
				finish(); 
				break;
				
			case R.id.action_change_pin: // change pin
				showPinDialog(); 
				break;
				
			case R.id.action_delete: 
				deleteNoteWindow();				
				break;	
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void deleteNoteWindow()
	{		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.delete_dialog_title);
		alert.setMessage(R.string.delete_note_dialog);

		alert.setNegativeButton(android.R.string.cancel,null); 
		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() 
		{
		    public void onClick(DialogInterface dialog, int whichButton) 
		    {
		    	Intent intent = new Intent();
				intent.putExtra(EXTRA_NOTE, currentNote);
				setResult(RESULT_OK, intent);
				finish();
		    }
		});
		alert.show();
	}
	
	// what happen when back Button is pressed - the note should be saved
	@Override
	public void onBackPressed()
	{
		saveNote();
		finish();
//		super.onBackPressed();
	}
	
	// the actions taken to save note
	
	/**
	 * Saves note changes as a result, which will be send later to the previous activity
	 * 
	 */
	public void saveNote()
	{
		
		EditText editTextSubtitle = (EditText) findViewById(R.id.etSubTitle);
		EditText editText = (EditText) findViewById(R.id.etContent);	
		
		currentNote.setNoteSubTitle(editTextSubtitle.getText().toString());
		currentNote.setNoteDestription( editText.getText().toString());
		
		// intent has a bundle and by intent.putExtra it allows to put values into bundle
		Intent intent = new Intent(); 
		intent.putExtra(EXTRA_NOTE, currentNote);
		
		//when I go to new window the first activity has to return something for it in the order to work
		setResult(RESULT_OK, intent);
	}

}
