package com.bignerdranch.android.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bignerdranch.android.criminalintent.database.CrimeBaseHelper;
import com.bignerdranch.android.criminalintent.database.CrimeCursorWrapper;
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema;
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.jar.Pack200;

/**
 * Created by zhenghao on 2017-06-29.
 */

//创建单例（只允许一个instance存在）
public class CrimeLab {
    //s prefix ---> 静态变量
    private static CrimeLab sCrimeLab;

    //private List<Crime> mCrimes;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        /*
        这里调用getWritableDatabase()方法时，CrimeBaseHelper要做如下工作。
(1) 打开/data/data/com.bignerdranch.android.criminalintent/databases/crimeBase.db数据库；如果
不存在，就先创建crimeBase.db数据库文件。
(2) 如果是首次创建数据库，就调用onCreate(SQLiteDatabase)方法，然后保存最新的版
本号。
(3) 如果已创建过数据库，首先检查它的版本号。如果CrimeOpenHelper中的版本号更高，
就调用onUpgrade(SQLiteDatabase, int, int)方法升级。
最后，再做个总结：onCreate(SQLiteDatabase)方法负责创建初始数据库；onUpgrade
(SQLiteDatabase, int, int)方法负责与升级相关的工作。
CriminalIntent当前只有一个版本，暂时可以不用操心onUpgrade(...)方法。我们在
onCreate(...)方法中创建数据库表，这需要导入CrimeDbSchema类的CrimeTable内部类。
         */
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();

        //mCrimes = new ArrayList<>();
        /*
        for (int i = 0; i < 100; i++) {
            Crime crime = new Crime();
            crime.setTitle("Crime #" + i);
            crime.setSolved(i % 2 == 0); // Every other one
            mCrimes.add(crime);
        }
         */


    }

    public void addCrime(Crime c) {
        /*
        insert(String, String, ContentValues)方法有两个重要的参数，还有一个很少用到。
传入的第一个参数是数据库表名，最后一个是要写入的数据。
第二个参数称为nullColumnHack。它有什么用途呢？
别急，举个例子你就明白了。假设你想调用insert(...)方法，但传入了ContentValues
空值。这时，SQLite不干了，insert(...)方法调用只能以失败告终。
然而，如果能以uuid值作为nullColumnHack传入的话，SQLite就可以忽略ContentValues
空值，而且还会自动传入一个带uuid且值为null的ContentValues。结果，insert(...)方法
得以成功调用并插入了一条新记录。
         */
        //mCrimes.add(c);
        ContentValues values = getContentValues(c);

        mDatabase.insert(CrimeTable.NAME, null, values);

    }

    public void deleteCrime(Crime c) {
        //mCrimes.remove(c);
    }


    /*
    数据库cursor之所以被称为cursor，是因为它内部就像有根手指似的，总是指向查询的某个地
方。因此，要从cursor中取出数据，首先要调用moveToFirst()方法移动虚拟手指指向第一个元
素。读取行记录后，再调用moveToNext()方法，读取下一行记录，直到isAfterLast()告诉我
们没有数据可取为止。
最后，别忘了调用Cursor的close()方法关闭它。否则，后果很严重：轻则看到应用报错，
重则导致应用崩溃。
     */
    public List<Crime> getCrimes() {
        //return mCrimes;
        //return new ArrayList<>();
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return crimes;
    }

    public Crime getCrime(UUID id) {
        /*
        for (Crime crime : mCrimes) {
            if (crime.getId().equals(id)){
                return crime;

            }
        }
        */
        //return null;
        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                new String[] { id.toString() }

        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    /*
    上述代码的作用如下。
 现在可以插入crime记录了。也就是说，点击New Crime菜单项，实现将Crime添加到
CrimeLab的代码可以正常工作了。
 数据库查询没有问题了。CrimePagerActivity现在能够看见CrimeLab中的所有Crime了。
 CrimeLab.getCrime(UUID) 方法也能正常工作了。CrimePagerActivity 托管的
CrimeFragment终于可以显示真正的Crime对象了。
现在，点击New Crime菜单项可以在CrimePagerActivity界面看到新增Crime了。运行
CriminalIntent应用确认无问题发生。
     */

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID + " = ?",
                new String[] { uuidString });
    }


    /*
负责处理数据库写入和更新操作的辅助类是ContentValues。它是个键值存储类，类似于
Java的HashMap和前面用过的Bundle。不同的是，ContentValues只能用于处理SQLite数据。
将Crime记录转换为ContentValues实际就是在CrimeLab中创建ContentValues实例。我们
需要新建一个私有方法
 */
    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1:0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());
        return values;
    }

    /*
    参数table是要查询的数据表。参数columns指定要依次获取哪些字段的值。参数where和
whereArgs的作用与update(...)方法中的一样。
新增一个便利方法调用query(...)方法查询CrimeTable中的记录
     */
    //private Cursor queryCrimes(String whereClause, String[] whereArgs) {
    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null, //Columns  - null selects all columns
                whereClause,
                whereArgs,
                null, //groupBy
                null, //having
                null  //orderBy
        );
        return new CrimeCursorWrapper(cursor);
    }

}


