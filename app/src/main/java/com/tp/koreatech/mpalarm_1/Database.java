package com.tp.koreatech.mpalarm_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SANGGYUN on 2016-12-20.
 */

public class Database extends SQLiteOpenHelper{
    static Database instance = null;
    static SQLiteDatabase database = null;

    static final String DATABASE_NAME = "DB";
    static final int DATABASE_VERSION = 1;

    public static final String ALARM_TABLE = "alarm";
    public static final String COLUMN_ALARM_ID = "_id";
    public static final String COLUMN_ALARM_ACTIVE = "alarm_active";
    public static final String COLUMN_ALARM_TIME = "alarm_time";
    public static final String COLUMN_ALARM_DAYS = "alarm_days";
    public static final String COLUMN_ALARM_TONE = "alarm_tone";
    public static final String COLUMN_ALARM_VIBRATE = "alarm_vibrate";
    public static final String COLUMN_ALARM_NAME = "alarm_name";
    public static final String COLUMN_ALARM_ACCELEROMETER = "alarm_accelerometer";
    public static final String COLUMN_ALARM_ACCCOUNT = "alarm_acccount";
    public static final String COLUMN_ALARM_DELAYTIME = "alarm_delaytime";

    public static String[] columns = new String[] {
            COLUMN_ALARM_ID,
            COLUMN_ALARM_ACTIVE,
            COLUMN_ALARM_TIME,
            COLUMN_ALARM_DAYS,
            COLUMN_ALARM_TONE,
            COLUMN_ALARM_VIBRATE,
            COLUMN_ALARM_NAME,
            COLUMN_ALARM_ACCELEROMETER,
            COLUMN_ALARM_ACCCOUNT,
            COLUMN_ALARM_DELAYTIME
    };

    Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public static void init(Context context) {
        if (null == instance) {
            instance = new Database(context);
        }
    }
    public static SQLiteDatabase getDatabase() {
        if (null == database) {
            database = instance.getWritableDatabase();
        }
        return database;
    }
    public static void deactivate() {
        if (null != database && database.isOpen()) {
            database.close();
        }
        database = null;
        instance = null;
    }
    public static long create(Alarm alarm) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ALARM_ACTIVE, alarm.getAlarmActive());
        cv.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(bos);
            oos.writeObject(alarm.getDays());
            byte[] buff = bos.toByteArray();

            cv.put(COLUMN_ALARM_DAYS, buff);

        } catch (Exception e){
        }

        cv.put(COLUMN_ALARM_TONE, alarm.getAlarmTonePath());
        cv.put(COLUMN_ALARM_VIBRATE, alarm.getVibrate());
        cv.put(COLUMN_ALARM_NAME, alarm.getAlarmName());
        cv.put(COLUMN_ALARM_ACCELEROMETER, alarm.getAccelerometer());
        cv.put(COLUMN_ALARM_ACCCOUNT, alarm.getAccCount());
        cv.put(COLUMN_ALARM_DELAYTIME, alarm.getDelayTime());

        return getDatabase().insert(ALARM_TABLE, null, cv);
    }
    public static int update(Alarm alarm) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ALARM_ACTIVE, alarm.getAlarmActive());
        cv.put(COLUMN_ALARM_TIME, alarm.getAlarmTimeString());

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(bos);
            oos.writeObject(alarm.getDays());
            byte[] buff = bos.toByteArray();

            cv.put(COLUMN_ALARM_DAYS, buff);

        } catch (Exception e){
        }

        cv.put(COLUMN_ALARM_TONE, alarm.getAlarmTonePath());
        cv.put(COLUMN_ALARM_VIBRATE, alarm.getVibrate());
        cv.put(COLUMN_ALARM_NAME, alarm.getAlarmName());
        cv.put(COLUMN_ALARM_ACCELEROMETER, alarm.getAccelerometer());
        cv.put(COLUMN_ALARM_ACCCOUNT, alarm.getAccCount());
        cv.put(COLUMN_ALARM_DELAYTIME, alarm.getDelayTime());

        return getDatabase().update(ALARM_TABLE, cv, "_id=" + alarm.getId(), null);
    }

    public static int deleteEntry(Alarm alarm){
        return deleteEntry(alarm.getId());
    }
    public static int deleteEntry(int id){
        return getDatabase().delete(ALARM_TABLE, COLUMN_ALARM_ID + "=" + id, null);
    }
    public static int deleteAll(){
        return getDatabase().delete(ALARM_TABLE, "1", null);
    }


    public static Alarm getAlarm(int id) {
        Cursor c = getDatabase().query(ALARM_TABLE, columns, COLUMN_ALARM_ID+"="+id, null, null, null,
                null);
        Alarm alarm = null;

        if(c.moveToFirst()){

            alarm =  new Alarm();
            alarm.setId(c.getInt(0));
            alarm.setAlarmActive(c.getInt(1)==1);
            alarm.setAlarmTime(c.getString(2));
            byte[] repeatDaysBytes = c.getBlob(3);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(repeatDaysBytes);
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                Alarm.Day[] repeatDays;
                Object object = objectInputStream.readObject();
                if(object instanceof Alarm.Day[]){
                    repeatDays = (Alarm.Day[]) object;
                    alarm.setDays(repeatDays);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            alarm.setAlarmTonePath(c.getString(4));
            alarm.setVibrate(c.getInt(5)==1);
            alarm.setAlarmName(c.getString(6));
            alarm.setAccelerometer(c.getInt(7) == 1);
            alarm.setAccCount(c.getInt(8));
            alarm.setDelayTime(c.getInt(9));
        }
        c.close();
        return alarm;
    }

    public static Cursor getCursor() {
        return getDatabase().query(ALARM_TABLE, columns, null, null, null, null,
                null);
    }

    public static Cursor getAscCursor() {
        return getDatabase().rawQuery("SELECT * FROM alarm order by alarm_time asc", null);
    }
    public static List<Alarm> getAll() {
        List<Alarm> alarms = new ArrayList<Alarm>();
        //execSQL("SELECT * FROM address order by hour*1, minute*1 asc");

        Cursor cursor = Database.getAscCursor();

        if (cursor.moveToFirst()) {

            do {
                Alarm alarm = new Alarm();
                alarm.setId(cursor.getInt(0));
                alarm.setAlarmActive(cursor.getInt(1) == 1);
                alarm.setAlarmTime(cursor.getString(2));
                byte[] repeatDaysBytes = cursor.getBlob(3);

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                        repeatDaysBytes);
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(
                            byteArrayInputStream);
                    Alarm.Day[] repeatDays;
                    Object object = objectInputStream.readObject();
                    if (object instanceof Alarm.Day[]) {
                        repeatDays = (Alarm.Day[]) object;
                        alarm.setDays(repeatDays);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                alarm.setAlarmTonePath(cursor.getString(4));
                alarm.setVibrate(cursor.getInt(5) == 1);
                alarm.setAlarmName(cursor.getString(6));
                alarm.setAccelerometer(cursor.getInt(7) == 1);
                alarm.setAccCount(cursor.getInt(8));
                alarm.setDelayTime(cursor.getInt(9));

                alarms.add(alarm);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarms;
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + ALARM_TABLE + " ( "
                + COLUMN_ALARM_ID + " INTEGER primary key autoincrement, "
                + COLUMN_ALARM_ACTIVE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_TIME + " TEXT NOT NULL, "
                + COLUMN_ALARM_DAYS + " BLOB NOT NULL, "
                + COLUMN_ALARM_TONE + " TEXT NOT NULL, "
                + COLUMN_ALARM_VIBRATE + " INTEGER NOT NULL, "
                + COLUMN_ALARM_NAME + " TEXT NOT NULL,"
                + COLUMN_ALARM_ACCELEROMETER + " INTEGER NOT NULL,"
                + COLUMN_ALARM_ACCCOUNT + " INTEGER NOT NULL,"
                + COLUMN_ALARM_DELAYTIME + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
        onCreate(sqLiteDatabase);
    }


}
