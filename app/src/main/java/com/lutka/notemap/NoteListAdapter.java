package com.lutka.notemap;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NoteListAdapter extends ArrayAdapter<Note>
{
	Context context;
	
	private static class ViewHolder
	{
		ImageView noteMarker;
		TextView tvNoteTitle, tvNoteSubtitle, tvNoteDescription, tvDistance;
		
		public ViewHolder(View view)
		{
			noteMarker = (ImageView) view.findViewById(R.id.ivNoteMarker);
			tvNoteTitle = (TextView) view.findViewById(R.id.tvNoteTitle);
			tvNoteSubtitle = (TextView) view.findViewById(R.id.tvNoteSubtitle);
			tvNoteDescription = (TextView) view.findViewById(R.id.tvNoteDescription);
			tvDistance = (TextView) view.findViewById(R.id.tvDistance);
		}
	}

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
		final View view;
		final ViewHolder holder;
		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.note_list_item, parent, false);
			holder = new ViewHolder(view);
			view.setTag(holder);
		}
		else  
		{
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		
		Note note = getItem(position);
		
		try
		{
			holder.noteMarker.setImageDrawable(note.getPinDrawable(context));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		holder.tvNoteTitle.setText(note.getNoteTitle());
		holder.tvNoteSubtitle.setText(note.getNoteSubTitle());
		holder.tvNoteDescription.setText(note.getNoteDestription());
		
		if(note.distance.isInfinite())
			holder.tvDistance.setText("");		
		else
			holder.tvDistance.setText(note.getDistance().toString()+" km");	
		
		return view;
	}

}
