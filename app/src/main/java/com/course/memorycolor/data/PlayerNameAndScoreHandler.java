package com.course.memorycolor.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by Ernest on 04/10/2016.
 */

public class PlayerNameAndScoreHandler implements Serializable {

    private SQLiteDatabase db;

    public PlayerNameAndScoreHandler(Context context) {
        DataBaseHandler dbHandler = new DataBaseHandler(context);
        db = dbHandler.getWritableDatabase();
    }

    public void insertPlayerName(String name) {

        ContentValues contentValues = new ContentValues();

        contentValues.put("name", name);

        db.insert(DataBaseHandler.NAMES_OF_PLAYER_TABLE, null, contentValues);
    }

    public Cursor queryAllPlayers() {
        Cursor c = db.query(DataBaseHandler.NAMES_OF_PLAYER_TABLE,
                new String[]{"_id", DataBaseHandler.NAME_COLUMN},
                null,
                null,
                null,
                null,
                null);

        return c;
    }

    public void deletePlayer(int _id) {
        db.delete(
                DataBaseHandler.NAMES_OF_PLAYER_TABLE,
                "_id=?",
                new String[]{_id + ""}
        );
    }

    public void insertScore(int score, int difficulty, int idPlayer) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DataBaseHandler.SCORE_COLUMN, score);
        contentValues.put(DataBaseHandler.DIFFICULTY_COLUMN, difficulty);
        contentValues.put(DataBaseHandler.ID_PLAYER_COLUMN, idPlayer);
        db.insert(DataBaseHandler.SCORE_AND_DIFFICULTY_TABLE, null, contentValues);

    }

    public int getIdFromPlayerName(String name) {
        Cursor c = db.query(DataBaseHandler.NAMES_OF_PLAYER_TABLE,
                new String[]{"_id"},
                DataBaseHandler.NAME_COLUMN + " = ?",
                new String[]{name},
                null,
                null,
                null
        );
        if (c.moveToFirst()) {
            int idx = c.getColumnIndex("_id");
            int idPLayer = c.getInt(idx);
            return idPLayer;
        }
        return -1;
    }



    public Cursor queryNameAndScoreForLevel(int level) {

        String rawQuery = "SELECT " + DataBaseHandler.SCORE_COLUMN + "," + DataBaseHandler.NAME_COLUMN
                + "," + DataBaseHandler.SCORE_AND_DIFFICULTY_TABLE + "._id as _id"
                + "," + DataBaseHandler.DIFFICULTY_COLUMN
                + " FROM " + DataBaseHandler.SCORE_AND_DIFFICULTY_TABLE
                + " INNER JOIN " + DataBaseHandler.NAMES_OF_PLAYER_TABLE + " ON " + DataBaseHandler.ID_PLAYER_COLUMN + " = "
                + DataBaseHandler.NAMES_OF_PLAYER_TABLE + "._id"
                + " WHERE " + DataBaseHandler.DIFFICULTY_COLUMN + " = " + level
                + " ORDER BY " + DataBaseHandler.SCORE_COLUMN + " DESC "
                + "LIMIT 6";


        return db.rawQuery(rawQuery, null);
    }



}
