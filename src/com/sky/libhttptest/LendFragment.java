package com.sky.libhttptest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LendFragment extends Fragment{
	
	private final static String TAG = "LendFragment";

	private final static int ADD_LEND_INFO = 1;
	private final static int ADD_TO_CALENDAR = 2;
	private final static int RENEW_IS_OK = 3;
	private final static int RENEW_IS_LOSE = 4;
	private final static int RENEW_IS_TIME = 5;
	private final static int ALL_TO_CALENDAR = 6;

	private final static String URL = "http://210.32.33.91:8080/reader/book_lst.php";

	private ListView lv_lend;
	private TextView lend_name;
	private TextView lend_day;
	private TextView back_day;
	private TextView lend_times;

	private MyHandler mHandler;
	
	private Context mContext;
	private Calendar mCalendar;
	private GoogleCalendarUtil mCalendarUtil;

	private List<Map<String, Object>> list;
	public SimpleAdapter adapter;

	public String lendName;
	public String lendDay;
	public String backDay;
	public String lendTimes;

	public String result;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		mContext = getActivity().getApplicationContext();
	    mCalendar = Calendar.getInstance();
	    	    
	}
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.lendfragment_layout, container, false);
        return v;
    } 
	
	@Override
	public void onStart(){
		super.onStart();
		
		lv_lend = (ListView) getActivity().findViewById(R.id.lv_lendfragment);
		
		Toast.makeText(getActivity(), "正在获取信息，请稍后...", Toast.LENGTH_SHORT).show();
		
		new HtmlThread().start();
		mHandler = new MyHandler();
		
		//列表点击
		lv_lend.setOnItemClickListener(new OnItemClickListener(){
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
					long arg3) {
				
				//选项实现
				new AlertDialog.Builder(getActivity())
                .setTitle("请选择")
                .setItems(R.array.select_dialog_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	
                    	switch(which){
                    	//实现日历同步
                    	case 0:
                    		CalendarRunnable cr = new CalendarRunnable(arg2);
                    		new Thread(cr).start();                    			
                    		break;
                    	//实现续借
                    	case 1:
                    		Toast.makeText(getActivity(), "正在续借，请稍后...", Toast.LENGTH_SHORT).show();
                    		RenewRunnable rr = new RenewRunnable(arg2);
                    		new Thread(rr).start();
                    		break;
                    	//实现全部日历tongbu
                    	case 2:
                    		AllCalendarRunnable acr = new AllCalendarRunnable();
                    		new Thread(acr).start();
                    		break;
                    	}
                    }
                })
                .show();								

			}

		});
	}
	
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {

			case ADD_LEND_INFO:
				lv_lend.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				break;
			case ADD_TO_CALENDAR:
				Toast.makeText(getActivity(), "图书到期提醒已同步到日历", Toast.LENGTH_SHORT).show();
				break;
			case RENEW_IS_OK:
				Toast.makeText(getActivity(), "续借成功", Toast.LENGTH_SHORT).show();
				break;
			case RENEW_IS_LOSE:
				Toast.makeText(getActivity(), "超过最大续借次数，不得续借！", Toast.LENGTH_SHORT).show();
				break;
			case RENEW_IS_TIME:
				Toast.makeText(getActivity(), "不到续借时间，不得续借！", Toast.LENGTH_SHORT).show();
				break;
			case ALL_TO_CALENDAR:
				Toast.makeText(getActivity(), "所有图书到期提醒已同步到日历", Toast.LENGTH_SHORT).show();
				break;
			}

		}
	}

	class HtmlThread extends Thread {

		@Override
		public void run() {

			result = new HttpUrl().get(URL);

			Document doc = Jsoup.parse(result);
			Elements trs = doc.select("tr");
			int totalTrs = trs.size();

			if (totalTrs > 0) {

				list = new ArrayList<Map<String, Object>>();

				for (int i = 0; i < totalTrs - 1; i++) {

					Elements tds = trs.get(i + 1).select("td");
					int totalTds = tds.size();

					Map<String, Object> map = new HashMap<String, Object>();

					for (int j = 0; j < totalTds; j++) {

						switch (j) {
						case 0:
							map.put("lend_code", tds.get(j).html()
									.toString());
							break;
						case 1:
							map.put("lend_name", tds.get(j).select("a").get(0).html()
									.toString());
							break;
						case 2:
							map.put("lend_day", "借阅日期："+tds.get(j).html()
									.toString());
							break;
						case 3:
							map.put("back_day", "归还日期："+tds.get(j).select("font").get(0).html()
									.toString());
							break;
						case 4:
							map.put("lend_times", "续借次数："+tds.get(j).html()
									.toString());
							break;
						}
					}

					list.add(map);
					adapter = new SimpleAdapter(getActivity(), list,
							R.layout.book_lend_item, new String[] {
									"lend_name", "lend_day",
									"back_day", "lend_times" },
							new int[] { R.id.lend_name, R.id.lend_day,
									R.id.back_day, R.id.lend_times });
				}

				// 提前传递List加快载入速度
				Message msg = new Message();
				msg.what = ADD_LEND_INFO;
				mHandler.sendMessage(msg);
			}

		}
	}
	
	class CalendarRunnable implements Runnable {
		
		private int num;
		
		public CalendarRunnable(int _num){
			this.num = _num;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			HashMap<String,String> map=(HashMap<String,String>)lv_lend.getItemAtPosition(num);
			String name = map.get("lend_name");
			String endDay = map.get("back_day");
			endDay = endDay.substring(5, endDay.length());
			Log.i(TAG, name);
			Log.i(TAG, endDay);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = null;
			try {
				date = sdf.parse(endDay);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mCalendar.setTime(date);
    		
    		mCalendarUtil = new GoogleCalendarUtil(mContext);
			mCalendarUtil.addToGoogleCalendar("图书到期提醒", "书籍名称:\n"+name, mCalendar);
			
			Message msg = new Message();
			msg.what = ADD_TO_CALENDAR;
			mHandler.sendMessage(msg);
			
		}
		
	}
	
	class RenewRunnable implements Runnable {
		
		private int num;
		
		public RenewRunnable(int _num){
			this.num = _num;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			HashMap<String,String> map=(HashMap<String,String>)lv_lend.getItemAtPosition(num);
			String code = map.get("lend_code");
			
			result = new HttpUrl().get("http://210.32.33.91:8080/reader/ajax_renew.php?bar_code="+code+"&time=1349076666666");
						
			Document doc = Jsoup.parse(result);
			Elements ps = doc.select("font");
			String sRenew = ps.get(0).html().toString();
			Log.i(TAG,"返回结果="+sRenew+"\nlength="+sRenew.length());
			
			if(sRenew.length() == 4){
				Message msg = new Message();
				msg.what = RENEW_IS_OK;
				mHandler.sendMessage(msg);
			}else if(sRenew.length() == 14){
				Message msg = new Message();
				msg.what = RENEW_IS_LOSE;
				mHandler.sendMessage(msg);
			}else if(sRenew.length() == 12){
				Message msg = new Message();
				msg.what = RENEW_IS_TIME;
				mHandler.sendMessage(msg);
			}
		}
		
	}
	
	class AllCalendarRunnable implements Runnable {
				
		@Override
		public void run() {
			
			int num = lv_lend.getCount();
			// TODO Auto-generated method stub
			for(int i=0;i < num;i++){
				HashMap<String,String> map=(HashMap<String,String>)lv_lend.getItemAtPosition(i);
				String name = map.get("lend_name");
				String endDay = map.get("back_day");
				endDay = endDay.substring(5, endDay.length());
				Log.i(TAG, name);
				Log.i(TAG, endDay);
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = null;
				try {
					date = sdf.parse(endDay);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mCalendar.setTime(date);
	    		
	    		mCalendarUtil = new GoogleCalendarUtil(mContext);
				mCalendarUtil.addToGoogleCalendar("图书到期提醒", "书籍名称:\n"+name, mCalendar);
			}
			
			Message msg = new Message();
			msg.what = ALL_TO_CALENDAR;
			mHandler.sendMessage(msg);
		}
		
	}
}