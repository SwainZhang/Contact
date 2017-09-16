package com.example.emery.contact.db;

/**
 * Created by emery on 2017/4/23.
 */

public class ContactDao extends BaseDao {
    @Override
    protected String createTable() {
        return "create table if not exists tb_contacts(_id integer  primary key autoincrement, name varchar(20),phone varchar(20))";
    }
}
