package com.example.emery.contact;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.emery.contact.constant.Constants;
import com.example.emery.contact.model.Contact;
import com.example.emery.contact.utils.CommonUtil;
import com.example.emery.contact.view.SwipeRefreshLayout;

import java.io.InputStream;
import java.util.ArrayList;

import static com.example.emery.contact.constant.Constants.CALL_REQUEST_CODE;
import static com.example.emery.contact.constant.Constants.Contact.CONTACT_REQUST_CODE;
import static com.example.emery.contact.constant.Constants.Contact.PHONES_PROJECTION;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    private int total_contact = 0;
    private ListView mLv_contacts;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ContactAdapter mContactAdapter;
    private ArrayList<Contact> mContactArrayList;
    private int mIndex = -1;
    private final int selectCount = 50;
    private RadioGroup mRg_tab;
    private RadioButton mRb_post;
    private Intent mCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_listview);
        mLv_contacts = (ListView) findViewById(R.id.lv_contacts);
        mSwipeRefreshLayout.setRefreshing(false);
        mRg_tab = (RadioGroup) findViewById(R.id.rg_tab);
        mRb_post = (RadioButton) findViewById(R.id.rb_post);
        mRg_tab.setOnCheckedChangeListener(this);
        requestWindow();
        mSwipeRefreshLayout.setOnLoadListener(new SwipeRefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ArrayList<Contact> sdContacts = getSDContacts();
                            if (sdContacts == null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSwipeRefreshLayout.setLoading(false);
                                        CommonUtil.toast(MainActivity.this, "别拉了,没有更多的数据了！");
                                    }
                                });
                                return;
                            } else {
                                mContactArrayList.addAll(sdContacts);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mContactAdapter.notifyDataSetChanged();
                                    mSwipeRefreshLayout.setLoading(false);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }).start();

            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (showRequestPermission()) {
            requestPermission(this);
        } else {
            mIndex = -1;
            mContactArrayList = null;
            mContactArrayList = new ArrayList<>();
            initContactList();

        }
        System.out.println("======");
    }


    private void initContactList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mContactArrayList = getSDContacts();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mContactAdapter = new ContactAdapter(mContactArrayList);
                        mLv_contacts.setAdapter(mContactAdapter);
                    }
                });
            }
        }).start();
    }

    public void add(View view) throws Exception {
        Intent intent = new Intent(MainActivity.this, ContactAddActivity.class);
        startActivity(intent);

    }


    private ArrayList<Contact> getSIMContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();
        ContentResolver resolver = getContentResolver();
        // 获取Sims卡联系人
        Uri uri = Uri.parse("content://icc/adn");
        Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,
                null);
        //获得一个ContentResolver数据共享的对象
        ContentResolver reslover = getContentResolver();


        while (phoneCursor.moveToNext()) {
            Contact contact = new Contact();
            // 得到手机号码
            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex
                    (ContactsContract.CommonDataKinds
                            .Phone.NUMBER));
            // 当手机号码为空的或者为空字段 跳过当前循环
            if (TextUtils.isEmpty(phoneNumber))
                continue;
            // 得到联系人名称
            String contactName = phoneCursor
                    .getString(phoneCursor.getColumnIndex(android.provider
                            .ContactsContract.Contacts.DISPLAY_NAME));

            //Sim卡中没有联系人头像

            contact.setName(contactName);
            contact.setPhoneNumber(phoneNumber);
            contacts.add(contact);
        }

        phoneCursor.close();
        return contacts;

    }

    /**
     * 调用一次加载selectCount
     * @return
     * @throws Exception
     */
    public ArrayList<Contact> getSDContacts() throws Exception {
        ArrayList<Contact> contacts = new ArrayList<>();

        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        //获得一个ContentResolver数据共享的对象
        ContentResolver reslover = getContentResolver();
        //取得联系人中开始的游标，通过content://com.android.contacts/contacts这个路径获得
        Cursor query = reslover.query(uri, null, null, null, null);
        total_contact = query.getCount();
        Cursor cursor = null;
        mIndex++;
        if (total_contact % selectCount != 0) {

            if (mIndex <= (total_contact / selectCount)) {
                cursor = reslover.query(uri, null, null, null, "_id asc limit " + selectCount + "" +
                        " offset " + selectCount * mIndex);
            } else {
                int last = (total_contact % selectCount);
                cursor = reslover.query(uri, null, null, null, "_id asc limit " + last + " offset" +
                        " " + selectCount * mIndex);
            }
        } else {

            if (mIndex <= (total_contact / selectCount)) {
                cursor = reslover.query(uri, null, null, null, "_id asc limit " + selectCount + "" +
                        " offset " + selectCount * mIndex);
            }
        }


        if (cursor == null) {
            return null;
        }

        while (cursor.moveToNext()) {
            //获得联系人ID
            String id = cursor.getString(cursor.getColumnIndex(android.provider.ContactsContract
                    .Contacts._ID));
            Contact contact = new Contact();

            //获得联系人姓名
            String name = cursor.getString(cursor.getColumnIndex(android.provider
                    .ContactsContract.Contacts.DISPLAY_NAME));
            //获得联系人手机号码
            Cursor phone = reslover.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
            contact.setName(name);
            StringBuffer sb = new StringBuffer("contactid=").append(id + "  name=").append(name);
            while (phone.moveToNext()) { //取得电话号码(可能存在多个号码)

                int phoneFieldColumnIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds
                        .Phone.NUMBER);
                String phoneNumber = phone.getString(phoneFieldColumnIndex);
                sb.append(" phone=" + phoneNumber);
                //得到联系人头像ID
                Long photoid = phone.getLong(phone.getColumnIndex(ContactsContract
                        .CommonDataKinds.Phone.PHOTO_ID));

                //得到联系人头像Bitamp
                Bitmap contactPhoto = null;

                //photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的
                if (photoid > 0) {
                    Uri photo_uri = ContentUris.withAppendedId(ContactsContract.Contacts
                            .CONTENT_URI, Long.parseLong(id));
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream
                            (reslover, photo_uri);
                    contactPhoto = BitmapFactory.decodeStream(input);
                    contact.setIcon(contactPhoto);
                } else {
                    contactPhoto = BitmapFactory.decodeResource(getResources(), R.mipmap
                            .ic_launcher);
                    contact.setIcon(contactPhoto);
                }
                contact.setPhoneNumber(phoneNumber);
            }

            //建立一个Log，使得可以在LogCat视图查看结果
            Log.i("TAG", sb.toString());
            contacts.add(contact);
            phone.close();

        }
        cursor.close();
        query.close();
        return contacts;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (group.getCheckedRadioButtonId()) {
            case R.id.rb_post:
                Intent intent = new Intent(this, NetworkActivity.class);
                startActivity(intent);
                break;
            case R.id.start_receiver:
                Intent service = new Intent(this, PhoneService.class);
                startService(service);

                CommonUtil.toast(this, "开启监听拨号服务");
            case R.id.rb_call:
                mCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:10086"));
                mCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CALL_PHONE}, CALL_REQUEST_CODE);
                }else{
                    startActivity(mCall);
                }

                break;

            case R.id.rb_backup:
                Intent backup=new Intent(this,ContactBackupActivity.class);
                startActivity(backup);
                break;
            default:
                break;
        }
    }


    public class ContactAdapter extends BaseAdapter {
        ArrayList<Contact> mContacts;

        public ContactAdapter(ArrayList<Contact> contacts) {
            this.mContacts = contacts;
        }

        @Override
        public int getCount() {
            return mContacts == null ? 0 : mContacts.size();
        }

        @Override
        public Contact getItem(int position) {
            return mContacts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContactHolder holder = new ContactHolder();
            if (convertView != null) {
                holder = (ContactHolder) convertView.getTag();
            } else {
                holder = new ContactHolder();
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout
                        .item_contact, null);
                holder.icon = (ImageView) convertView.findViewById(R.id.item_icon);
                holder.name = (TextView) convertView.findViewById(R.id.item_contact_name);
                holder.phoneNumber = (TextView) convertView.findViewById(R.id.item_contact_phone);
                convertView.setTag(holder);
            }
            Contact item = getItem(position);
            holder.icon.setImageBitmap(item.getIcon());
            holder.name.setText(item.getName());
            holder.phoneNumber.setText(item.getPhoneNumber());
            return convertView;
        }
    }

    class ContactHolder {
        public ImageView icon;
        public TextView name;
        public TextView phoneNumber;
    }

    public boolean showRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission(final Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            //如果要解释为什么需要这个权限，应该弹出一个提示框，这里省略

            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission
                            .WRITE_CONTACTS}, CONTACT_REQUST_CODE);


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constants.Contact.CONTACT_REQUST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initContactList();
                } else {
                    CommonUtil.toast(this, "通讯录权限被禁止，请到设置中开启");
                }
                break;
            case CALL_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(mCall);
                } else {
                    CommonUtil.toast(this, "拨号权限被禁止，请到设置中开启");
                }
                break;

            default:
                break;
        }

    }
   public void requestWindow(){
       if (Build.VERSION.SDK_INT >= 23) {
           if (! Settings.canDrawOverlays(MainActivity.this)) {
               Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                       Uri.parse("package:" + getPackageName()));
               startActivityForResult(intent,10);
           }
       }
   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    Toast.makeText(this,"系统对话框请求失败",Toast.LENGTH_SHORT);
                }
            }
        }
    }
}
