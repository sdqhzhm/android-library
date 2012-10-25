package com.sky.libhttptest;

import org.apache.http.client.CookieStore;

import android.app.Application;


public class MyApplication extends Application{
	
	private boolean loginStatus = false;	
	
	public boolean getLoginStatus(){
		return loginStatus;
	}
	
	public void setLibcookie(boolean status){
		loginStatus = status;
	}
	
	private boolean netStatus = false;
	
	public boolean getNetStatus(){
		return netStatus;
	}
	
	public void setNetStatus(boolean status){
		netStatus = status;
	}
}