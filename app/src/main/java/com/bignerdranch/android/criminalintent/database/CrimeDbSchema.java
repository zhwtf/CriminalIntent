package com.bignerdranch.android.criminalintent.database;

/**
 * Created by zhenghao on 2017-07-03.
 */

public class CrimeDbSchema {
    public static final class CrimeTable {
        //CrimeTable内部类唯一的用途就是定义描述数据表元素的String常量。首先要定义的是数
        //据库表名（CrimeTable.NAME）。
        public static final String NAME = "crimes";

        /*
        有了这些数据表元素，就可以在Java代码中安全地引用了。例如，CrimeTable.Cols.TITLE就
是指crime记录的title字段。此外，这种定义方式还给修改字段名称或新增表元素带来了方便。
         */
        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String SUSPECT = "suspect";
        }
    }
}
