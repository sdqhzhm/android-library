package com.sky.libhttptest;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

public class AboutDialog extends AlertDialog{
	
	public AboutDialog(Context context) {   
        super(context);   
        final View view = getLayoutInflater().inflate(R.layout.about,   
                null);   
        setButton(context.getText(R.string.close), (OnClickListener) null);   
        setIcon(R.drawable.logo);   
        setTitle("Lib v1.6" );
        setView(view);   
    }  
}