package com.lutka.notemap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.michaldabski.msqlite.MSQLiteOpenHelper;

public class DatabaseHelper extends MSQLiteOpenHelper
{

	private static int DB_VERSION = 1;
	private static String DB_NAME = "notemap.db";
	
	public DatabaseHelper(Context context) 
	{
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO Auto-generated method stub

	}

}
