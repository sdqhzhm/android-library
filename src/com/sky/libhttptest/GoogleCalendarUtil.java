package com.sky.libhttptest;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class GoogleCalendarUtil {
	
	private static final String TAG = "GoogleCalendarUtil";
	private Context mContext;
	private static String calendarURI = "";
	private static String calendarEventURI = "";
	private static String calendarRemiderURI = "";

	private String calId = "";

	static {
		calendarURI = "content://com.android.calendar/calendars";  
		calendarEventURI = "content://com.android.calendar/events";  
		calendarRemiderURI = "content://com.android.calendar/reminders";
	}

	public GoogleCalendarUtil(Context context) {
		super();
		this.mContext = context;

		Cursor userCursor = mContext.getContentResolver().query(Uri.parse(calendarURI), null,   
                null, null, null);
		
		Log.i(TAG,""+userCursor.getCount());
		
		if (userCursor.getCount() > 0) {
			userCursor.moveToLast();
			calId = userCursor.getString(userCursor.getColumnIndex("_id"));
			Log.i(TAG,"Id:"+userCursor.getString(userCursor.getColumnIndex("name")));
		}

	}
	
	public void addToGoogleCalendar(String _title,String _description,Calendar _calendar){
		
		String title = _title;
		String description = _description;
		
		ContentValues event = new ContentValues();
		event.put("title", title);
		event.put("description",description);
		
		event.put("calendar_id",calId); 
		
		Calendar mCalendar = _calendar;
		mCalendar.set(Calendar.HOUR_OF_DAY,10);  
        long start = mCalendar.getTime().getTime();  
        mCalendar.set(Calendar.HOUR_OF_DAY,13);  
        long end = mCalendar.getTime().getTime();
        
        event.put("dtstart", start);
        event.put("dtend",end);
        event.put("allDay", 1);
        event.put("hasAlarm", 1);
        //android sdk API > 3.0 需要加入eventTimezone 
        //http://developer.android.com/guide/topics/providers/calendar-provider.html#add-event
        event.put("eventTimezone", "Asia/Shanghai");
        Uri newEvent = mContext.getContentResolver().insert(Uri.parse(calendarEventURI), event);
        
        long id = Long.parseLong(newEvent.getLastPathSegment());
        ContentValues values = new ContentValues();
        values.put("event_id", id);
        
        values.put("minutes", 10);
        
        mContext.getContentResolver().insert(Uri.parse(calendarRemiderURI), values);
        
	}

}