package com.ivhong.androidcommonlibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;


/**
 * 键值对存储，使用SQLite实现，键为String，值也固定String，不支持其他类型的值
 * 使用方法：
 *
 * //获得实例
 * KVStorage kvs = KVStorage.getInstance(getApplicationContext());
 * //设置键值对
 * kvs.set("key111", "value111");
 * //获取方法1，如果key不存在，返回 null
 * String key111= kvs.get("key111");
 * //获取方法2，如果key不存在，返回 "value";
 * String key111= kvs.get("key111", "value");
 *
 * Created by 王长宏 on 2017/12/15.
 */

public class KVStorage extends SQLiteOpenHelper {
    //实例
    private static KVStorage kvStorage = null;
    //数据库（文件）名
    private static String dbname = "IvhongKVStorage";
    //表明
    private static String tname = "IvhongKVStorage";

    //写实例
    private SQLiteDatabase writer = null;
    //读实例
    private SQLiteDatabase reader = null;
    //测试
//    private static final String TAG = "test";

    private KVStorage(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        writer = this.getWritableDatabase();
        reader = this.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table "+tname+"(" +
                "id integer primary key autoincrement," +
                "key text," +
                "value text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+tname);
    }

    /**
     * 单例模式
     *
     * @param context app 上下文
     * @return KVStorage 实例
     */
    public static KVStorage getInstance(Context context){
        if(KVStorage.kvStorage == null){
            KVStorage.kvStorage = new KVStorage(context, KVStorage.dbname, null, 1);
        }

        return KVStorage.kvStorage;
    }

    /**
     * 设置键值
     *
     * @param key 键的名称
     * @param value 键所对应的值
     */
    public void set(String key, String value){
        Map<Integer,String> old = _get(key);

        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        if(old == null){//insert
            writer.insert(tname, null, values);
        }else{
            Integer id = old.keySet().iterator().next();
            writer.update(tname,values,"id = ?", new String[]{id+""});
        }
    }

    /**
     * 获取某个key的值
     *
     * @param key 键的名称
     * @return key 所对应的值，如果没有返回null
     */
    public String get(String key){
        Map<Integer,String> value = _get(key);
        String res = null;
        if(value != null){
            res = value.values().iterator().next();
        }

        return res;
    }

    /**
     * 获取某个key的值，如果key 不存在，返回默认值
     *
     * @param key 键的名称
     * @param defaultValue 默认值
     * @return key 所对应的值，如果没有返回 defaultValue
     */
    public String get(String key, String defaultValue){
        Map<Integer,String> value = _get(key);
        if(value != null){
            defaultValue = value.values().iterator().next();
        }

        return defaultValue;
    }

    /**
     * 查询数据库，获取某行数据
     *
     * @param key 键的名称
     * @return Map<id, value>
     */
    private Map<Integer,String> _get(String key){
        Map<Integer,String> value = null;
        Cursor query = reader.query(tname, new String[]{"id","value"}, "key = ?", new String[]{key}, null, null, null, null);
        if(query.getCount() > 0){
            value = new HashMap();
            query.moveToFirst();
            Integer id = query.getInt(query.getColumnIndex("id"));
            String val = query.getString( query.getColumnIndex("value") );
            value.put(id, val);
        }

        return value;
    }
}
