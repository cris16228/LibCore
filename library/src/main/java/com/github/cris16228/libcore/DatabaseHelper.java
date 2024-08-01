package com.github.cris16228.libcore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.RawRes;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "database.db";
    public static final String COLUMN_ID = "_id";
    public static int DATABASE_VERSION = 1;
    public static String[] TABLE;
    private static String[] CREATE_TABLE;
    private Context context;

    public DatabaseHelper(Context context, boolean addID) {
        this(context, DATABASE_NAME, DATABASE_VERSION, "", "", addID);
        CREATE_TABLE = new String[]{};
        this.context = context;
    }

    public DatabaseHelper(Context context, String databaseName, int databaseVersion, String[] table, String[] tableColumns) {
        super(context, databaseName.endsWith(".db") ? databaseName : databaseName + ".db", null, databaseVersion);
        TABLE = table;
        DATABASE_VERSION = databaseVersion;
        CREATE_TABLE = tableColumns;
        this.context = context;
    }

    public DatabaseHelper(Context context, String databaseName, int databaseVersion, String table, String tableColumns, boolean addID) {
        super(context, databaseName.endsWith(".db") ? databaseName : databaseName + ".db", null, databaseVersion);
        TABLE[0] = table;
        DATABASE_VERSION = databaseVersion;
        CREATE_TABLE[0] = buildTable(tableColumns, addID, table);
        this.context = context;
    }

    public static void copyDatabase(Context context, @RawRes int database, String databaseName) {
        InputStream mInput = context.getResources().openRawResource(database);
        String outFileName = context.getDatabasePath(databaseName).getPath();
        OutputStream mOutput;
        try {
            mOutput = new FileOutputStream(outFileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        byte[] mBuffer = new byte[1024];
        int mLength;
        while (true) {
            try {
                if (!((mLength = mInput.read(mBuffer)) > 0)) break;
                mOutput.write(mBuffer, 0, mLength);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            mOutput.flush();
            mOutput.close();
            mInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String s : CREATE_TABLE) {
            db.execSQL(s);
        }
    }

    private String buildTable(String columns, boolean addID, String table) {
        if (StringUtils.isEmpty(columns)) {
            return "CREATE TABLE " + table + (addID ? "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT)" : "");
        } else {
            return "CREATE TABLE " + table + (addID ? "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " : "") + columns + ")";
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}