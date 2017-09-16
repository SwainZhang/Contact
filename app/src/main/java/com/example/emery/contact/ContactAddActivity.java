package com.example.emery.contact;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.example.emery.contact.constant.Constants;

/**
 * Created by emery on 2017/4/21.
 */

public class ContactAddActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_create);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.ISFORMACREATE,true);
        Fragment fragment=CreateAndCommitFragment.newInstance(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.container_create,fragment,"fragment").commit();
    }
}
