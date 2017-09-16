package com.example.emery.contact;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.emery.contact.constant.Constants;
import com.example.emery.contact.utils.CommonUtil;
import com.example.emery.contact.utils.HttpUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by emery on 2017/4/23.
 */

public class CreateAndCommitFragment extends Fragment implements View.OnClickListener {
    private Button mBt_add_all;
    private Button mBt_delete_all;
    private Button mBt_back;
    private EditText mEd_contact_name;
    private Button mBt_save;
    private EditText mEd_contact_phone;
    private int begin = 0;
    private boolean mIsFromCreate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {

        View root = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_contact_create,
                null);

        initData();
        initUI(root);
        return root;
    }

    private void initData() {

        Bundle arguments = getArguments();
        mIsFromCreate = arguments.getBoolean(Constants.ISFORMACREATE, false);
    }

    public static CreateAndCommitFragment newInstance(Bundle bundle) {
        CreateAndCommitFragment fragment = new CreateAndCommitFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void initUI(View root) {

        mBt_add_all = (Button) root.findViewById(R.id.contact_create_add_all);
        mBt_delete_all = (Button) root.findViewById(R.id.contact_crate_delete_all);
        mBt_back = (Button) root.findViewById(R.id.contact_create_back);
        mEd_contact_name = (EditText) root.findViewById(R.id.contact_create_name);
        mBt_save = (Button) root.findViewById(R.id.contact_create_save);
        mEd_contact_phone = (EditText) root.findViewById(R.id.contact_create_phone);
        if (mIsFromCreate) {
            mBt_add_all.setOnClickListener(this);
            mBt_delete_all.setOnClickListener(this);
        }

        mBt_back.setOnClickListener(this);
        mBt_save.setOnClickListener(this);

        if (mIsFromCreate) {
            mBt_save.setText("保存");
        } else {
            mBt_save.setText("提交");
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contact_create_add_all:
                begin++;
                System.out.println("begin=" + begin);
                CommonUtil.toast(getActivity(), "第" + begin + "次批量添加3000条");
                Intent intent = new Intent(getActivity(), ContactAddService.class);
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.INDEX, begin);
                intent.putExtras(bundle);
                getActivity().startService(intent);

                break;
            case R.id.contact_crate_delete_all:

                deleteBacth();

                break;
            case R.id.contact_create_back:
                getActivity().finish();
                break;
            case R.id.contact_create_save:
                String name = mEd_contact_name.getText().toString().trim();
                String phone = mEd_contact_phone.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                    CommonUtil.toast(getActivity(), "姓名或者手机不能为空");
                    return;
                }
                if (mIsFromCreate) {
                    insertContact(name, phone);
                } else {
                    commit(name ,phone);
                }
                mEd_contact_name.setText("");
                mEd_contact_phone.setText("");

                break;


            default:
                break;
        }

    }

    private void commit(String name, String phone) {
        final String url="http://115.28.192.235/interview/postTest.php";
        final Map<String,String> params=new HashMap<>();
        params.put("sName",name);
        params.put("sPhone",phone);
        HttpUtil.post(url, params, new DataResonseListener() {
            @Override
            public void onResponse(String result) {
                System.out.println("post="+result);
            }
        });






    }

    public void insertContact(String name, String phone) {

        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = getActivity().getContentResolver();
        ContentValues values = new ContentValues();
        long contactid = ContentUris.parseId(resolver.insert(uri, values));

        uri = Uri.parse("content://com.android.contacts/data");

        //添加姓名
        values.put("raw_contact_id", contactid);
        values.put(ContactsContract.Contacts.Data.MIMETYPE, "vnd.android.cursor.item/name");
        values.put("data1", name);
        resolver.insert(uri, values);
        values.clear();

        //添加电话
        values.put("raw_contact_id", contactid);
        values.put(ContactsContract.Contacts.Data.MIMETYPE, "vnd.android.cursor.item/phone_v2");
        values.put("data1", phone);
        resolver.insert(uri, values);


        //添加Email
           /* values.put("raw_contact_id", contactid);
            values.put(ContactsContract.Contacts.Data.MIMETYPE, "vnd.android.cursor.item/email_v2");
            values.put("data1", "1234120155@qq.com");*/

    }

    public void deleteBacth() {
        //根据姓名求id
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
                ContentResolver resolver = getActivity().getContentResolver();
                Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts.Data._ID},
                        null, null, null);
                if (cursor.moveToFirst()) {
                    resolver.delete(uri, null, null);
                    uri = Uri.parse("content://com.android.contacts/data");
                    resolver.delete(uri, null, null);
                }
                cursor.close();
            }
        }).start();

    }

    public void delete(String name) {
        //根据姓名求id
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = getActivity().getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts.Data._ID},
                "display_name=?", new String[]{name}, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            //根据id删除data中的相应数据
            resolver.delete(uri, "display_name=?", new String[]{name});
            uri = Uri.parse("content://com.android.contacts/data");
            resolver.delete(uri, "raw_contact_id=?", new String[]{id + ""});

        }
        cursor.close();

    }

    public void insertBacth(String name, String phone) {
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = getActivity().getContentResolver();
        ArrayList<ContentProviderOperation> operations = new ArrayList();
        ContentProviderOperation op1 = ContentProviderOperation.newInsert(uri)
                .withValue("account_name", null)
                .build();
        operations.add(op1);

        uri = Uri.parse("content://com.android.contacts/data");
        //添加姓名
        ContentProviderOperation op2 = ContentProviderOperation.newInsert(uri)
                .withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/name")
                .withValue("data2", name)
                .build();
        operations.add(op2);
        //添加电话号码
        ContentProviderOperation op3 = ContentProviderOperation.newInsert(uri)
                .withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/phone_v2")
                .withValue("data1", phone)
                .build();
        operations.add(op3);

        try {
            resolver.applyBatch("com.android" + ".contacts", operations);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }


}
