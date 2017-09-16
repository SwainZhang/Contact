package com.example.emery.contact;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.emery.contact.utils.CommonUtil;

import static com.example.emery.contact.constant.Constants.SD_REQUEST_CODE;

/**
 * Created by emery on 2017/4/23.
 */

public class ContactBackupActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
    }

    public void backup(View view) {
        checkPermission();
    }



    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission
                            .WRITE_EXTERNAL_STORAGE}, SD_REQUEST_CODE);
        } else {
            try {
                jump();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case SD_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                      jump();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    CommonUtil.toast(this, "读取内存权限被禁止，请到设置中开启");
                }
                break;
        }

    }
    public void jump(){
        Intent intent=new Intent(this,ContactBackupService.class);
        startService(intent);
    }

}
