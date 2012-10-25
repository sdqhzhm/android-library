package com.sky.libhttptest;

import com.viewpagerindicator.LinePageIndicator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class InfoActivity extends FragmentActivity {
	
	static final int NUM_ITEMS = 2;
	
	private MyViewPagerAdapter mAdapter;
	private ViewPager mPager;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        
        mAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.viewPager);
        mPager.setAdapter(mAdapter);
        
        LinePageIndicator lineIndicator = (LinePageIndicator)findViewById(R.id.indicator);
        lineIndicator.setViewPager(mPager);
        
    }
    
    public class MyViewPagerAdapter extends FragmentPagerAdapter{
    	
    	public MyViewPagerAdapter(FragmentManager fm){
    		super(fm);
    	}

		@Override
		public Fragment getItem(int position) {
			switch(position){
			case 0:
				return new LendFragment();
			case 1:
				return new ReserveFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return NUM_ITEMS;
		}
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
}
