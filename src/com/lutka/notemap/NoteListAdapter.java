package com.lutka.notemap;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NoteListAdapter extends ArrayAdapter<Note>
{
	Context context;

	public NoteListAdapter(Context context, int resource, int textViewResourceId, List <Note> listOfNotes)
	{
		super(context, resource, textViewResourceId, listOfNotes);
		this.context = context;
	}
	
	public NoteListAdapter(Context context, List <Note> listOfNotes)
	{
		this(context, R.layout.note_list_item, android.R.id.text1, listOfNotes);		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.note_list_item, parent, false);
		
		Note note = getItem(position);

		ImageView noteMarker = (ImageView) view.findViewById(R.id.ivNoteMarker);
		
		TextView tvNoteTitle = (TextView) view.findViewById(R.id.tvNoteTitle);
		TextView tvNoteSubtitle = (TextView) view.findViewById(R.id.tvNoteSubtitle);
		TextView tvNoteDescription = (TextView) view.findViewById(R.id.tvNoteDescription);			
		
		noteMarker.setImageDrawable();		//note.getPinDrawable(context)
		
		tvNoteTitle.setText(note.getNoteTitle());
		tvNoteSubtitle.setText(note.getNoteSubTitle());
		tvNoteDescription.setText(note.getNoteDestription());
		
		return view;
	}

}
