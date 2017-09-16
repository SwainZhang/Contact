package com.example.emery.contact;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.emery.contact.db.BaseDaoFactory;
import com.example.emery.contact.db.ContactDao;
import com.example.emery.contact.model.Contact;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import static java.io.File.separator;

/**
 * Created by emery on 2017/4/23.
 */

public class ContactBackupService extends IntentService {

    private SQLiteDatabase mSqLiteDatabase;
    private int total_contact = 0;
    private int mIndex = -1;
    private final int selectCount = 100;
    private static final String filePath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() +
            separator + "contacts";
    private SQLiteDatabase mBackupDatabase;
    private boolean mExit;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ContactBackupService(String name) {
        super(name);
    }

    public ContactBackupService() {
        super("");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDataHelper = BaseDaoFactory.getInstance("contacts.db").getDataHelper(ContactDao.class,
                Contact.class);
        mSqLiteDatabase = BaseDaoFactory.getInstance("contacts.db").getSQLiteDatabase();
        checkPath(filePath);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            backup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ContactDao mDataHelper;

    private void backup() throws Exception {

        doBackup();
        dividContacts();

    }

    private void dividContacts() throws Exception {

        while (!mExit) {
            System.out.println("分割数据库----------》");
            getAndDivdDBContacts();
        }
    }

    private void checkPath(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public ArrayList<Contact> doBackup() throws Exception {
        ArrayList<Contact> contacts = new ArrayList<>();

        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        //获得一个ContentResolver数据共享的对象
        ContentResolver reslover = getContentResolver();
        //取得联系人中开始的游标，通过content://com.android.contacts/contacts这个路径获得
        Cursor cursor = reslover.query(uri, null, null, null, null);
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
            StringBuffer sb = new StringBuffer("Service查询contactid=").append(id + "  name=")
                    .append(name);
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
            mDataHelper.insert(contact);
            phone.close();

        }
        cursor.close();
        return contacts;
    }

    /**
     * 调用一次加载selectCount
     *
     * @return
     * @throws Exception
     */
    public void getAndDivdDBContacts() throws Exception {
        String tb_name = "tb_contacts";

        //此处原来使用了单例，创建数据库并打开数据库，需要修改
        ContactDao dataHelper = BaseDaoFactory.getInstance("contacts" + mIndex + ".db")
                .getDataHelper(ContactDao.class, Contact.class);


        Cursor query = mSqLiteDatabase.query(tb_name, null, null, null, null, null, null);
        total_contact = query.getCount();
        Cursor cursor = null;
        mIndex++;
        if (total_contact % selectCount != 0) {

            if (mIndex <= (total_contact / selectCount)) {
                String sql = "select * from " + tb_name + " limit " + selectCount + " offset " +
                        selectCount * mIndex;
                cursor = mSqLiteDatabase.rawQuery(sql, null);
                /*cursor = mSqLiteDatabase.query(tb_name, null, null, null, null, null, "_id asc ",
                        " " + selectCount + "" +
                        " offset " + selectCount * mIndex);*/
            } else if (mIndex <= (total_contact / selectCount) + 1) {
                int last = (total_contact % selectCount);
                String sql="select * from " + tb_name + " limit " + last + " offset " + selectCount * mIndex;
                cursor=mSqLiteDatabase.rawQuery(sql,null);
                /*cursor = mSqLiteDatabase.query(tb_name, null, null, null, null, null, "_id asc ",
                        "" + last + " offset" +
                        " " + selectCount * mIndex);*/
            }
        } else {

            if (mIndex <= (total_contact / selectCount)) {
                String sql = "select * from " + tb_name + " limit " + selectCount + " offset " +
                        selectCount * mIndex;
                System.out.println("sql----->" + sql);
                cursor = mSqLiteDatabase.rawQuery(sql, null);
                /*cursor = mSqLiteDatabase.query(tb_name, null, null, null,null,null, "_id asc ",
                " " + selectCount + "" +
                        " offset " + selectCount * mIndex);*/
            }
        }

        //没有更多的数据的时候cussor=null;
        if (cursor == null) {
            mExit = true;
            return;
        }
        if (mIndex > (total_contact / selectCount) + 1) {
            mExit = true;
            return;
        }

        while (cursor.moveToNext()) {
            //获得联系人ID
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            Contact contact = new Contact();
            //获得联系人姓名
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String phone = cursor.getString(cursor.getColumnIndex("phone"));
            contact.setName(name);
            contact.setPhoneNumber(phone);
            System.out.println("分割--" + contact.toString());
            dataHelper.insert(contact);

        }
        cursor.close();
        query.close();


    }
}
