package com.course.memorycolor;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;

import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.course.memorycolor.dagger.MemoryColor;
import com.course.memorycolor.data.DataBaseHandler;
import com.course.memorycolor.model.ModelMemoryColor;

import java.util.ArrayList;

import javax.inject.Inject;

public class PlayerData extends AppCompatActivity {

    //Variables member
    static final String LOG_TAG = "PlayerData";
    ArrayAdapter<String> mStringArrayAdapter;
    private LayoutInflater mInflator;
    private boolean selected;
    private Spinner spnNumberOfPlayers;
    TextView tvChooseNumberOfPlayersFix;

    @Inject
    ModelMemoryColor mModel;

    //Array to store the AutocompleteTextViews
    final ArrayList<AutoCompleteTextView> autoCompleteTextViewsArray = new ArrayList<AutoCompleteTextView>();

    AutoCompleteTextView etPlayer1Name;
    AutoCompleteTextView etPlayer2Name;
    AutoCompleteTextView etPlayer3Name;
    AutoCompleteTextView etPlayer4Name;
    AutoCompleteTextView etPlayer5Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_data);

        //This is used to fix the orientation so the screen doesn't rotate in this activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ((MemoryColor)getApplication()).getMemoryComponent().injectPlayerData(this);


        //for make back arrow work on activity you have to decalre parent activity on manifest too
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getting references of AutoCompleteTextViews
        etPlayer1Name = (AutoCompleteTextView) findViewById(R.id.etPlayer1Name);
        etPlayer2Name = (AutoCompleteTextView) findViewById(R.id.etPlayer2Name);
        etPlayer3Name = (AutoCompleteTextView) findViewById(R.id.etPlayer3Name);
        etPlayer4Name = (AutoCompleteTextView) findViewById(R.id.etPlayer4Name);
        etPlayer5Name = (AutoCompleteTextView) findViewById(R.id.etPlayer5Name);

        //Here we set the text to the first edit text, this is made to keep the name of
        // the last time the app was played on the device
        if (mModel.mDefaultPlayerName != null) {
            etPlayer1Name.setText(mModel.mDefaultPlayerName);

        }

       //Storage of the AutocompleteTextViews
        autoCompleteTextViewsArray.add(etPlayer1Name);
        autoCompleteTextViewsArray.add(etPlayer2Name);
        autoCompleteTextViewsArray.add(etPlayer3Name);
        autoCompleteTextViewsArray.add(etPlayer4Name);
        autoCompleteTextViewsArray.add(etPlayer5Name);

        //calling the method initUI that is made to be able to set our spinner
        initUI();

        for (int i = 0; i < mModel.mNumberOfPlayers; i++) {
            final int iFinal = i;

            //Setting the text for the autocompleteTextViews
            autoCompleteTextViewsArray.get(i).setText(mModel.mPlayerNames.get(i));

            //Setting on touch listeners for the autocompleteTextViews
            autoCompleteTextViewsArray.get(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    autoCompleteTextViewsArray.get(iFinal).setText("");
                    return false;
                }
            });

            //Here we set text changed listener, this is made for cancel the touch listener when
            // some text is entered on the autoCompleteTextViews, the aim of that is to delete the
            // text when the user first enters on the activity by just touching the autocomplete,
            // but changing it afterwards so the text doesn't get erase by one touch all the time
            autoCompleteTextViewsArray.get(i).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    //on text changed the touch listener is canceled
                    //autoCompleteTextViewsArray.get(iFinal).setOnClickListener(null);
                    autoCompleteTextViewsArray.get(iFinal).setOnTouchListener(null);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        //ArrayList to contain all the names of the database to set the adapter for autocomplete
        ArrayList<String> namesArray = new ArrayList<String>();
        Cursor c = mModel.mPlayerNameAndScoreHandler.queryAllPlayers();
        if (c.moveToFirst()) {

            int indx = c.getColumnIndex(DataBaseHandler.NAME_COLUMN);
            String name = c.getString(indx);
            namesArray.add(name);

            while (c.moveToNext()) {
                indx = c.getColumnIndex(DataBaseHandler.NAME_COLUMN);
                name = c.getString(indx);
                namesArray.add(name);
            }
        }
        //Initialization of the adapter for autocomplete
        mStringArrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                namesArray);

        //Setting the adatper to autocompletetextsviews
        etPlayer1Name.setAdapter(mStringArrayAdapter);
        etPlayer2Name.setAdapter(mStringArrayAdapter);
        etPlayer3Name.setAdapter(mStringArrayAdapter);
        etPlayer4Name.setAdapter(mStringArrayAdapter);
        etPlayer5Name.setAdapter(mStringArrayAdapter);

        //getting reference to the ok button and setting on clilck listener
        Button btnOK = (Button) findViewById(R.id.btn_Ok);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //getting info from the textviews, inserting into database and resetting them
                mModel.mPlayerNames.clear();
                //Checking that the names introduced by user are correct
                for (int i = 0; i < mModel.mNumberOfPlayers; i++) {
                    String name;
                    name = autoCompleteTextViewsArray.get(i).getText().toString();
                    if (name.length() < 3) {
                        Toast.makeText(getBaseContext(),
                                "All names must be at least 3 characters long",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                //Storing names to database
                for (int i = 0; i < mModel.mNumberOfPlayers; i++) {
                    String name;
                    name = autoCompleteTextViewsArray.get(i).getText().toString().toLowerCase();
                    mModel.mPlayerNameAndScoreHandler.insertPlayerName(name);
                    mModel.mPlayerNames.add(name);
                }

                //saving the first player name introduced as default player
                mModel.mDefaultPlayerName = autoCompleteTextViewsArray.get(0).getText().toString()
                        .toLowerCase();
                //creating the return intent, saving number of players, their names and the default
                // player name and also setting the result of the createActivityForResult
                Intent returnIntent = new Intent();
                setResult(AppCompatActivity.RESULT_OK, returnIntent);
                finish();

            }
        });
    }

    //Method to initiate the UI, we do that to be able to personalize the spinner
    private void initUI() {
        selected = false;
        spnNumberOfPlayers = (Spinner) findViewById(R.id.spnNumberOfPlayers);
        spnNumberOfPlayers.setAdapter(typeSpinnerAdapter);
        spnNumberOfPlayers.setOnItemSelectedListener(typeSelectedListener);
        spnNumberOfPlayers.setOnTouchListener(typeSpinnerTouchListener);
        spnNumberOfPlayers.setSelection(mModel.mNumberOfPlayers - 1);
        mInflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        tvChooseNumberOfPlayersFix = (TextView)findViewById(R.id.tvChooseNumberOfPlayersFix);
        tvChooseNumberOfPlayersFix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spnNumberOfPlayers.performClick();

            }
        });
    }

    //Creation of our own spinner adapter
    private SpinnerAdapter typeSpinnerAdapter = new BaseAdapter() {

        //TextView to show the option selected on the spinner
        private TextView text;

        //the options on the spinner
        private String[] data = {"1 Player", "2 Players", "3 Players", "4 Players", "5 Players"};
        private int count = 5;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflator.inflate(R.layout.row_spinner, null);
            }
            text = (TextView) convertView.findViewById(R.id.spinnerTarget);

            text.setText(data[position]);

            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return data[position];
        }

        @Override
        public int getCount() {
            return count;
        }

        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflator.inflate(
                        R.layout.spinner_item_player, null);
            }
            text = (TextView) convertView.findViewById(android.R.id.text1);
            text.setText(data[position]);
            return convertView;
        }

    };

    //Listener of the spinner, here we set the variable number of players and the number of
    // TextViews to enter the players names accordingly
    private AdapterView.OnItemSelectedListener typeSelectedListener =
            new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            mModel.mNumberOfPlayers = position + 1;

            for (int i = 0; i < autoCompleteTextViewsArray.size(); i++) {
                if (position >= i) {
                    autoCompleteTextViewsArray.get(i).setVisibility(View.VISIBLE);
                } else {
                    autoCompleteTextViewsArray.get(i).setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private View.OnTouchListener typeSpinnerTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            selected = true;
            ((BaseAdapter) typeSpinnerAdapter).notifyDataSetChanged();

            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
