package com.lutka.notemap;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
		
		TextView tvListItem = (TextView) view.findViewById(R.id.tvListItem);
		
		tvListItem.setText(getItem(position).getNoteDestription());
		return view;
	}

}
