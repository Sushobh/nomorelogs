package com.ranrings.nomorelogslib;

import android.content.Context;

import com.chuckerteam.chucker.api.ChuckerInterceptor;

public class StackWithLogAndChucker extends StackWithLogs{

    public StackWithLogAndChucker(Context context){
        interceptors.add(new ChuckerInterceptor(context));
    }

}
