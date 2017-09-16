package com.example.emery.contact.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

import static java.io.File.separator;

/**
 * Created by MyPC on 2017/2/4.
 */

public class BaseDaoFactory {

    private String sqliteDataBasePath;
    private SQLiteDatabase mSQLiteDatabase;
    private static BaseDaoFactory baseDaoFactory;
    public BaseDaoFactory(String dbName){
     String filePath= Environment.getExternalStorageDirectory().getAbsolutePath()+
             separator+"contacts";
        checkPath(filePath);
        sqliteDataBasePath=filePath+File.separator +dbName;
        openDataBase(sqliteDataBasePath);
    }

    private void checkPath(String filePath) {
        File file = new File(filePath);
        if(!file.exists()){
            file.mkdir();
        }
    }

    private void openDataBase(String sqliteDataBasePath) {
        this.mSQLiteDatabase=SQLiteDatabase.openOrCreateDatabase(sqliteDataBasePath, null);
    }

    public synchronized <T extends BaseDao<M>,M> T getDataHelper(Class<T> daoClazz,Class<M> entityClazz){
        BaseDao baseDao=null;
        try {
            baseDao=  daoClazz.newInstance();
            baseDao.init(entityClazz,this.mSQLiteDatabase);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return (T)baseDao;
    }
    public SQLiteDatabase getSQLiteDatabase(){
        return mSQLiteDatabase;
    }
   public static BaseDaoFactory getInstance(String dbName){
       if(baseDaoFactory==null){
           synchronized (BaseDaoFactory.class){
               if(baseDaoFactory==null){
                   baseDaoFactory=new BaseDaoFactory(dbName);
               }
           }
       }
       return baseDaoFactory;
   }
}
