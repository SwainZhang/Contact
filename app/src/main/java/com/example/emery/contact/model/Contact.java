package com.example.emery.contact.model;

import android.graphics.Bitmap;

import com.example.emery.contact.db.annotation.DbFiled;
import com.example.emery.contact.db.annotation.DbTable;

/**
 * Created by emery on 2017/4/21.
 */
@DbTable(tableName = "tb_contacts")
public class Contact {
    public Bitmap icon;
    @DbFiled("name")
    public String name;
    @DbFiled("phone")
    public String phoneNumber;

    public Contact() {

    }

    public Contact(Bitmap icon, String name, String phoneNumber) {
        this.icon = icon;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "icon=" + icon +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
