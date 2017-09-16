package com.example.emery.contact.db;

import java.util.ArrayList;

/**
 * Created by MyPC on 2017/2/4.
 */

public interface IBaseDao<T> {
    /**
     * 插入数据的封装的实体类，如 user
     * @param entity
     * @return
     */
    long insert(T entity);

    /**
     * 更新数据
     * @param entity
     * @param where
     * @return
     */
    int update(T entity,T where);
    long delete(T where);
    ArrayList<T> query(T where, String g, String groupBy, String having, String orderBy, String limit);
}
