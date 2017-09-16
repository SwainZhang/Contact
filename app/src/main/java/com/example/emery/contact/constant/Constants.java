package com.example.emery.contact.constant;

import android.provider.ContactsContract;

/**
 * Created by emery on 2017/4/22.
 */

public class Constants {

    public static class Contact {
        public static final int CONTACT_REQUST_CODE = 1 << 1;
        public static final String[] PHONES_PROJECTION = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract
                .CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Photo.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
    }
    public static final  String  INDEX="INDEX";
    public static  String ISFORMACREATE="isFromCreate";
    public static  final int CALL_REQUEST_CODE=1<<2;
    public static  final int SD_REQUEST_CODE=1<<3;

}

