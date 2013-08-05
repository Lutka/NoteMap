package com.lutka.notemap;

import java.io.IOException;
import java.util.zip.Inflater;

import android.R.anim;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class PinAdapter extends ArrayAdapter<String>
{
	Context context;
	public PinAdapter(Context context, int resource, int textViewResourceId, String[] pinNames) 
	{		
		super(context, resource, textViewResourceId, pinNames);
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	
	public PinAdapter(Context context, String[] pinNames) 
	{
		this(context, R.layout.pin_item, android.R.id.text1, pinNames);
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.pin_item, parent, false);
		ImageView pinImageView = (ImageView) view.findViewById(R.id.pinItem);
		
		try 
		{
			pinImageView.setImageDrawable(Note.getPinDrawable(context, getItem(position)));
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return view;
	}
	
}
