package com.example.emery.contact.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.emery.contact.db.annotation.DbFiled;
import com.example.emery.contact.db.annotation.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by MyPC on 2017/2/4.
 */

public abstract class BaseDao<T> implements IBaseDao<T> {
    protected SQLiteDatabase mSQLiteDatabase;
    private boolean isInit = false;
    private Class<T> entityClass;
    private String tableName;

    /**
     * 维护表名与成员变量名之间的映射关系
     * key 表名
     */
    private HashMap<String, Field> cacheMap = new HashMap<String, Field>();

    public String getTableName(){
       return this.tableName;
    }
    protected boolean init(Class<T> entity, SQLiteDatabase sqLiteDatabase) {
        if (!isInit) {
            entityClass = entity;
            this.mSQLiteDatabase = sqLiteDatabase;
            if (entity.getAnnotation(DbTable.class) != null) {
                tableName = entity.getAnnotation(DbTable.class).tableName();
            } else {
                tableName = entity.getClass().getSimpleName();//如果注解为空那么就以类名为数据库名
            }

            if (!mSQLiteDatabase.isOpen()) {
                return false;//数据库是否成功
            }
            if (!TextUtils.isEmpty(createTable())) {
                mSQLiteDatabase.execSQL(createTable());
            }
            initMap();
        }
        isInit = true;//初始化成功


        return isInit;
    }

    private void initMap() {
        Cursor mCursor = null;
        try {
            String sql = "select * from " + this.tableName + " limit 1 , 0";//查一条从0条开始

            mCursor = mSQLiteDatabase.rawQuery(sql, null);
            String[] columnNames = mCursor.getColumnNames();

            Field[] columns = entityClass.getFields();//实体类的成员变量就是表的列名

            for (Field field : columns) {
                field.setAccessible(true);
            }

            /**
             * 找对应关系
             */
            for (String columnName : columnNames) {

                //如果找到就赋值给他
                Field colomn = null;
                for (Field field : columns) {
                    String fieldName = null;
                    if (field.getAnnotation(DbFiled.class) != null) {
                        //拿到实体类中的成员变量上面的注解（也就是实体类对应的表名及其成员变量在表中对应的列名）
                        fieldName = field.getAnnotation(DbFiled.class).value();
                    } else {
                        //如果成员变量上面注解没有写列名，就以成员变量名作为列名
                        fieldName = field.getName();
                    }

                    //如果表的列名等于实体类中成员变量的注解的列名
                    if (columnName.equals(fieldName)) {
                        colomn = field;
                        break;
                    }
                }

                //找到了对应关系
                if (colomn != null) {
                    //表的列名，反射得到实体类的成员变量field
                    cacheMap.put(columnName, colomn);
                }
            }
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }

    }

    /**
     * 将实体类的成员变量的值及其注解(表的列名)分别封装成map 的value 和 key(表的列名）
     *
     * @param entity
     * @return
     */
    private Map<String, String> getValues(T entity) {

        Map<String, String> result = new HashMap<String, String>();
        Iterator<Field> iterator = cacheMap.values().iterator();
        while (iterator.hasNext()) {
            Field columFiled = iterator.next();
            String cacheKey = null;
            String cacheValue = null;
            if (columFiled.getAnnotation(DbFiled.class) != null) {
                cacheKey = columFiled.getAnnotation(DbFiled.class).value();
            } else {
                cacheKey = columFiled.getName();
            }


            try {
                if (columFiled.get(entity) == null) {
                    continue;
                }

                cacheValue = columFiled.get(entity).toString();//拿到实体类成员变量的值。

                result.put(cacheKey, cacheValue);//存入集合

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public long insert(T entity) {
        Map<String, String> values = getValues(entity);
        ContentValues contentValues = getContentValue(values);
        long insert = mSQLiteDatabase.insert(tableName, null, contentValues);
        return insert;
    }

    private ContentValues getContentValue(Map<String, String> values) {
        ContentValues contentValues = new ContentValues();
        Set<Map.Entry<String, String>> entries = values.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            contentValues.put(entry.getKey(), entry.getValue());
        }
        return contentValues;
    }

    @Override
    public int update(T entity, T where) {

        //需要更新的字段及其值,如 update table * set K=V where $$;
        Map<String, String> columns = getValues(entity);
        /**
         * where条件对象实体类转换成map
         */
        Map<String, String> whereClause = getValues(where);
        Conditon conditon= new Conditon(whereClause);
        ContentValues contentValue = getContentValue(columns);
        int update = mSQLiteDatabase.update(tableName, contentValue, conditon.whereClause,
                conditon.whereArgs);
        System.out.println("-----whereArgs="+conditon.whereArgs[0]);
        return update;
    }

    @Override
    public long delete(T where) {
        Map<String, String> values = getValues(where);
        Conditon conditon=new Conditon(values);
        int delete = mSQLiteDatabase.delete(tableName, conditon.whereClause, conditon.whereArgs);
        return delete;
    }

    @Override
    public ArrayList<T> query(T where,String g,String groupBy,String having,String orderBy,String limit) {

        // select * from table tb_common_user
        Map<String, String> whereClause = getValues(where);
        Conditon conditon=new Conditon(whereClause);
        Cursor query = mSQLiteDatabase.query(tableName, null, conditon.whereClause, conditon
                .whereArgs, groupBy, having, orderBy, limit);
        ArrayList<T> queryResult = getQueryResult(query, where);
        return queryResult;
    }

    private ArrayList<T>  getQueryResult(Cursor cursor,T where){

        ArrayList<T> result=new ArrayList<>();
        while (cursor.moveToNext()){
            try {
                Object item = where.getClass().newInstance();//实体bean
                Set<Map.Entry<String, Field>> entries = cacheMap.entrySet();

                for(Map.Entry<String, Field> entry:entries){

                    String columnName = entry.getKey();
                    int columnIndex = cursor.getColumnIndex(columnName);

                    if(columnIndex!=-1){

                        Field field = entry.getValue();
                        Class<?> type = field.getType();
                        if(type==String.class){
                            String content = cursor.getString(columnIndex);
                            field.set(item,content);//把成员变量赋值给对象
                        }else if(type==Double.class){
                            Double content=cursor.getDouble(columnIndex);
                            field.set(item,content);
                        }else if(type==Float.class){
                            Float content=cursor.getFloat(columnIndex);
                            field.set(item,content);
                        }else if(type==Integer.class){
                            Integer content = cursor.getInt(columnIndex);
                            field.set(item,content);
                        }else if(type==byte[].class){
                            byte[] blob = cursor.getBlob(columnIndex);
                            field.set(item,blob);
                        }else{
                            continue;
                        }
                    }
                }
                result.add((T) item);

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return  result;
    }

    protected abstract String createTable();

    /**
     * 封装查询语句
     */
    class Conditon {
        /**
         * where 查询条件
         * name=?&&password=?;
         */
        private String whereClause;

        /**
         * 查询条件的参数
         */
        private String[] whereArgs;


        public Conditon(Map<String, String> whereClause) {
            ArrayList<String> whereArgs=new ArrayList<>();//条件参数；
            StringBuilder builder=new StringBuilder();//查询条件；
            builder.append(" 1=1 ");
            Set<Map.Entry<String, String>> entries = whereClause.entrySet();
            for(Map.Entry<String, String> entry:entries){
                builder.append(" and "+entry.getKey()+"=?");
                whereArgs.add(entry.getValue());
            }
            this.whereArgs= whereArgs.toArray(new String[]{});
            this.whereClause=builder.toString();

        }
    }
}
