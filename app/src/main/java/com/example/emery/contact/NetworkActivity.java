package com.example.emery.contact;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.example.emery.contact.constant.Constants;
import com.example.emery.contact.utils.CommonUtil;

/**
 * Created by emery on 2017/4/23.
 */

public class NetworkActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        jump();
        requestPermission(this);
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission(final Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            //如果要解释为什么需要这个权限，应该弹出一个提示框，这里省略

            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.INTERNET}, 0);


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                jump();
            } else {
                CommonUtil.toast(this, "通讯录权限被禁止，请到设置中开启");
            }
        }
    }

    public void jump(){
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.ISFORMACREATE,false);
        Fragment fragment_net=CreateAndCommitFragment.newInstance(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.container_net,fragment_net,"fragment_net").commit();

    }
}
