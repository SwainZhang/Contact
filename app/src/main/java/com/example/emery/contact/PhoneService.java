package com.example.emery.contact;


import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.WindowManager;

/**
 * Created by emery on 2017/4/23.
 */

public class PhoneService extends Service {


    private OutgoingCallReceiver mOutgoingCallReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("888888888");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(Integer.MAX_VALUE);
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        mOutgoingCallReceiver = new OutgoingCallReceiver();
        registerReceiver(mOutgoingCallReceiver, intentFilter);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("00000000000000");


        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mOutgoingCallReceiver);
    }


    class OutgoingCallReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            String phone = getResultData();//得到外拔电话
            System.out.println("phone" + phone);
            if (phone.equals("10086")) {
                System.out.println("++++++++");
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("亲爱的用户，您正在拨出的号码是 “10086”，此号码存在风险，请小心!!\n");
                builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.show();

            }


        }
    }
}