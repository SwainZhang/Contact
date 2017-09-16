package com.example.emery.contact.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by emery on 2017/4/22.
 */

public class CommonUtil {
    public static void toast(Context context,String content){
        Toast.makeText(context,content,Toast.LENGTH_SHORT).show();
    }


}
