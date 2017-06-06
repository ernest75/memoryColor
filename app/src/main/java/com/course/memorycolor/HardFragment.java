package com.course.memorycolor;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.course.memorycolor.data.DataBaseHandler;
import com.course.memorycolor.data.PlayerNameAndScoreHandler;

/**
 * Created by Ernest on 11/10/2016.
 */

public class HardFragment extends Fragment {

    public HardFragment()
    {

    }

    Context mContext;
    PlayerNameAndScoreHandler mPlayerNameAndScoreHandler;
    int mLevel = 3;

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragments_records_game, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        ListView lvScoreRecords = (ListView) getView().findViewById(R.id.lvRecordsScore);

        mPlayerNameAndScoreHandler = new PlayerNameAndScoreHandler(getContext());
        Cursor c = mPlayerNameAndScoreHandler.queryNameAndScoreForLevel(mLevel);
        SimpleCursorAdapter easyAdapter = new SimpleCursorAdapter(
                mContext,
                R.layout.lv_records_game_item,
                c,
                new String[]{DataBaseHandler.NAME_COLUMN ,DataBaseHandler.SCORE_COLUMN},
                new int[]{R.id.tvName,R.id.tvScore},
                0);
        lvScoreRecords.setAdapter(easyAdapter);
        super.onViewCreated(view, savedInstanceState);
    }
}
