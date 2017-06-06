package com.course.memorycolor.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ernest on 04/10/2016.
 */

public class DataBaseHandler extends SQLiteOpenHelper {


    private static final int DB_VERSION = 9;

    public static final String DB_NAME = "names.db";

    public static final String NAME_COLUMN = "name";

    public static final String SCORE_COLUMN = "score";

    public static final String DIFFICULTY_COLUMN = "difficulty";

    public static final String ID_PLAYER_COLUMN = "idPlayer";

    public static final String NAMES_OF_PLAYER_TABLE = "namesOfPlayer";

    public static final String SCORE_AND_DIFFICULTY_TABLE = "scoreAndDifficulty";

    public static final String DEFAULT_NAMES_TABLE ="defaultNames";

    public static final String DEFAULT_NAME_COLUMN ="defaultNamesColumn";


    public DataBaseHandler(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // String sql = "create table namesOfGameWithScore(_id integer primary key,playerName text not null,score integer)";

        String createNameTable = "create table " + NAMES_OF_PLAYER_TABLE + "(" +
                "_id integer primary key," +
                NAME_COLUMN + " text not null unique)";

        String createScoresAndDifficultyTable = "create table " + SCORE_AND_DIFFICULTY_TABLE+ "(" +
                "_id integer primary key," +
                SCORE_COLUMN + " integer, " +
                DIFFICULTY_COLUMN + " integer not null, "+
                ID_PLAYER_COLUMN + " integer not null," +
                " Foreign Key ("+ ID_PLAYER_COLUMN + ") references " + NAMES_OF_PLAYER_TABLE + "(_id) )";

        String createDefaultNamesTable = "create table " + DEFAULT_NAMES_TABLE + "(" +
                "_id integer primary key," +
                DEFAULT_NAME_COLUMN + " text not null unique)";

        db.execSQL(createNameTable);
        db.execSQL(createScoresAndDifficultyTable);
        db.execSQL(createDefaultNamesTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + NAMES_OF_PLAYER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SCORE_AND_DIFFICULTY_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DEFAULT_NAMES_TABLE);
        onCreate(db);

    }
}
