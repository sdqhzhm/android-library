package com.sky.libhttptest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.sky.libhttptest.LendFragment.AllCalendarRunnable;
import com.sky.libhttptest.LendFragment.CalendarRunnable;
import com.sky.libhttptest.LendFragment.HtmlThread;
import com.sky.libhttptest.LendFragment.MyHandler;
import com.sky.libhttptest.LendFragment.RenewRunnable;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ReserveFragment extends Fragment {
	
	private final static String TAG = "ReserveFragment";
	
	private final static int ADD_RESERVE_INFO = 0;
	private final static int CANCEL_IS_OK = 1;
	private final static int CANCEL_IS_LOSE = 2;

	private final static String URL = "http://210.32.33.91:8080/reader/preg.php";
	
	private ListView lv_reserve;
	
	private MyHandler mHandler;
	
	private List<Map<String, Object>> list;
	public SimpleAdapter adapter;

	public String reserveName;
	public String reserveDay;
	public String endDay;
	public String bookLocal;

	public String result;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.reservefragment_layout, container, false);
        return v;
    }
	
	@Override
	public void onStart(){
		super.onStart();
		
		lv_reserve = (ListView) getActivity().findViewById(R.id.lv_reservefragment);
		
		new HtmlThread().start();
		mHandler = new MyHandler();
		
		//列表点击
		lv_reserve.setOnItemClickListener(new OnItemClickListener(){
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
					long arg3) {
				
				//选项实现
				new AlertDialog.Builder(getActivity())
                .setTitle("请选择")
                .setItems(R.array.select_dialog2_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	
                    	switch(which){
                    	
                    	case 0:
                    		CancelRunnable cr = new CancelRunnable(arg2);
                    		new Thread(cr).start();                    			
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

			case ADD_RESERVE_INFO:
				lv_reserve.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				break;
			case CANCEL_IS_OK:
				Toast.makeText(getActivity(), "预约已取消", Toast.LENGTH_SHORT).show();
				break;
			case CANCEL_IS_LOSE:
				Toast.makeText(getActivity(), "预约取消失败", Toast.LENGTH_SHORT).show();
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
							map.put("local_code", tds.get(j).select("input").attr("value")
									.toString());
							break;
						case 1:
							map.put("reserve_name", tds.get(j).select("a").get(0).html()
									.toString());
							map.put("marc_no", tds.get(j).select("a").attr("href")
									.toString());
							break;
						case 4:
							map.put("reserve_day", "预约日期："+tds.get(j).html()
									.toString());
							break;
						case 5:
							map.put("end_day", "截止日期："+tds.get(j).html()
									.toString());
							break;
						case 6:
							map.put("book_local", "取书地点："+tds.get(j).html()
									.toString()); 
						case 7:
							map.put("reserve_status", "状态："+tds.get(j).html()
									.toString()); 
							break;
						}
					}

					list.add(map);
					adapter = new SimpleAdapter(getActivity(), list,
							R.layout.book_reserve_item, new String[] {
									"reserve_name", "reserve_day",
									"end_day", "book_local","reserve_status" },
							new int[] { R.id.reserve_name, R.id.reserve_day,
									R.id.end_day, R.id.book_local, R.id.reserve_status });
				}

				// 提前传递List加快载入速度
				Message msg = new Message();
				msg.what = ADD_RESERVE_INFO;
				mHandler.sendMessage(msg);
			}
			
		}
	}
	
	class CancelRunnable implements Runnable {
		private int num;
		public CancelRunnable(int _num){
			num = _num;
		}
		@Override
		public void run() {
			
			HashMap<String,String> map=(HashMap<String,String>)lv_reserve.getItemAtPosition(num);
			
			String local_code = map.get("local_code");
			String marc_no = map.get("marc_no");
			marc_no = marc_no.substring(17);
			
			result = new HttpUrl().get("http://210.32.33.91:8080/reader/ajax_preg.php?call_no="+local_code+"&"+marc_no+"&loca=00003&time=1349611847285");
			
			Document doc = Jsoup.parse(result);
			Elements fonts = doc.select("font");
			String sCancel = fonts.get(0).html().toString();
			Log.i(TAG,"返回结果="+sCancel+"\nlength="+sCancel.length());
			
			if(sCancel.length()==3){
				Message msg = new Message();
				msg.what = CANCEL_IS_OK;
				mHandler.sendMessage(msg);
			}else{
				Message msg = new Message();
				msg.what = CANCEL_IS_LOSE;
				mHandler.sendMessage(msg);
			}
		}
		
	}
	
}