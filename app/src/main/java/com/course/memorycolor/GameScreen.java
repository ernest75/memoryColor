package com.course.memorycolor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.course.memorycolor.dagger.MemoryColor;
import com.course.memorycolor.model.ModelMemoryColor;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

public class GameScreen extends AppCompatActivity {

    private static final String LOG_TAG = "GameScreen";

    //** Variables member **\\

    public ArrayList<Button> mButtonArrayList;

    //Reference to the context of the app
    @Inject
    @Named("application_context")
    public Context mContext;

    //Object menu
    private Menu mMenu;

    @Inject
    ModelMemoryColor mModel;

    public MediaPlayer mpRightAnswer;
    public Vibrator mVibrator;
    public MediaPlayer mpWrongAnswer;
    public MediaPlayer mpWrongAnswerShort;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //injecting dagger2 to GameScreen
        ((MemoryColor)getApplication()).getMemoryComponent().injectGameScreen(this);

        mButtonArrayList = new ArrayList<>();
        mpRightAnswer = MediaPlayer.create(mContext, R.raw.right_answer);
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mpWrongAnswer = MediaPlayer.create(mContext, R.raw.wrong_answer);
        mpWrongAnswerShort = MediaPlayer.create(mContext, R.raw.wrong_answer_short);

        //mModel = ModelMemoryColor.initialize(this);

        //** Retrieving data from the saveInstanceState Bundle **\\

        mModel.retrieveSaveInstanceState(savedInstanceState);

        //fix the orientation so the screen doesn't rotate
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    // onResume used to retrieve the data of the preferences with shared preferences
    @Override
    protected void onResume() {
        super.onResume();
        settingLayoutWithDifficulty();
        settingSoundAndVibrationPreferences();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mModel.saveDefaultPlayerName();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game_screen, menu);

        //getting reference of the menu of this activity
        mMenu = menu;

        //Making sure that play icon it's displayed and more important has the same constantState variable
        mMenu.getItem(0).setIcon(ContextCompat.getDrawable(mContext, android.R.drawable.ic_media_play));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.action_new_game) {

            //checking witch icon it's already on the menu if
            //it's play icon start a new game and set the stop
            //icon
            if (mMenu.getItem(0).getIcon().getConstantState()
                    .equals(ContextCompat.getDrawable(mContext, android.R.drawable.ic_media_play)
                            .getConstantState())) {
                mMenu.getItem(0).setIcon(ContextCompat.getDrawable(mContext, R.drawable.ic_stop));
                gameStart();

                //else means that it's icon stop who is visible
                // so build a dialog to stop or not the app, if finally
                // the app it's stopped set the play icon
            } else {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        mContext);

                // set title
                alertDialogBuilder.setTitle("EXIT ALERT");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Click yes to exit!")
                        .setCancelable(false)

                        // if this button is clicked, close
                        // current game and set play icon
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                mMenu.getItem(0).setIcon(ContextCompat.getDrawable(mContext,
                                        android.R.drawable.ic_media_play));
                                mModel.finishGameByUser();
                            }
                        })

                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }

        } else if (id == R.id.action_player_data) {

            //Setting intent and passing the data so PlayerData
            // will be able to retrieved there
            Intent intent = new Intent(this, PlayerData.class);

            //starting activity with startActivity for result
            // so we can send back the data that this activity
            // started here, will store on the return intent
            startActivityForResult(intent, 1);

        } else if (id == R.id.action_records_game) {
            //Starting RecordsGameActivity
            Intent intent = new Intent(this, RecordsGameActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void settingDataForEasyLevel() {

        //setting reference to the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setting references to the buttons
        final Button btnRed = (Button) findViewById(R.id.btn_red);
        final Button btnBlue = (Button) findViewById(R.id.btn_blue);
        final Button btnGreen = (Button) findViewById(R.id.btn_green);
        final Button btnCyan = (Button) findViewById(R.id.btn_cyan);
        final Button btnPink = (Button) findViewById(R.id.btn_pink);
        final Button btnYellow = (Button) findViewById(R.id.btn_yellow);

        //setting onClickListeners for all buttons
        btnRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int colorNumber = Integer.parseInt(v.getTag().toString());
                colorBtnClicked(colorNumber);
            }
        });
        btnBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int colorNumber = Integer.parseInt(v.getTag().toString());
                colorBtnClicked(colorNumber);
            }
        });
        btnGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int colorNumber = Integer.parseInt(v.getTag().toString());
                colorBtnClicked(colorNumber);
            }
        });

        btnCyan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int colorNumber = Integer.parseInt(v.getTag().toString());
                colorBtnClicked(colorNumber);
            }
        });
        btnPink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int colorNumber = Integer.parseInt(v.getTag().toString());
                colorBtnClicked(colorNumber);
            }
        });

        btnYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int colorNumber = Integer.parseInt(v.getTag().toString());
                colorBtnClicked(colorNumber);
            }
        });

        storingButtonsForEasyLevel(mButtonArrayList);
    }

    private void settingDataForMediumLevel() {

        //setting the data from easy level
        settingDataForEasyLevel();

        //adding the new buttons
        final Button btnOrange = (Button) findViewById(R.id.btn_orange);
        final Button btnGrey = (Button) findViewById(R.id.btn_grey);

        //setting listeners
        btnOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorNumber = Integer.parseInt(v.getTag().toString());
                colorBtnClicked(colorNumber);
            }
        });

        btnGrey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorNumber = Integer.parseInt(v.getTag().toString());
                colorBtnClicked(colorNumber);
            }
        });

        storingButtonsForMediumLevel(mButtonArrayList);
    }

    private void settingDataForHardLevel() {

        //setting the data from medium level
        settingDataForMediumLevel();

        //adding the new buttons
        final Button btnStrongGreen = (Button) findViewById(R.id.btn_strong_green);
        final Button btnPurple = (Button) findViewById(R.id.btn_purple);

        //setting listeners
        btnStrongGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorNumber = Integer.parseInt(v.getTag().toString());
                colorBtnClicked(colorNumber);
            }
        });

        btnPurple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorNumber = Integer.parseInt(v.getTag().toString());
                colorBtnClicked(colorNumber);
            }
        });

        storingButtonsForHardLevel(mButtonArrayList);
    }

    //method to finish the game and reset all variables
    private void gameFinish() {
        mMenu.getItem(0).setIcon(ContextCompat.getDrawable(mContext,android.R.drawable.ic_media_play));
        mModel.finishGame();
    }

    public void gameStart() {
        mModel.settingVariablesForNewGame();
        //starting animation
        gameAnimation();
    }

    //Method to make the animations
    public void gameAnimation() {


        //calculating sequence
        //checking if the game just started and if so adding to the array list of the color sequence
        // of the player the number of starting animations
        mModel.generateNextColorSequence();

        //First checking if the state it's the correct one, in this case animating state
        if (mModel.mGameState == ModelMemoryColor.GameState.GameAnimating) {

            //Making toast for every new turn for each player, first on multiplayer and with more
            // than 1 player alive
            showPlayerTurn();

            //Creating animations handler, here we recreate the animations of the buttons with the
            // stored colors in mColorsequence
            for (int numAnimation = 0; numAnimation < mModel.mCurrNumberOfAnimations; numAnimation++) {
                //setting a reference to i, that is needed cause we need a final int to pass to the
                // handler, so each step of the for will create a new final int referencing i
                final int indexOfColorSeq = numAnimation;

                //Creation of the first handler, that one will change the color of the button to
                // it's version on
                final Handler handler = new Handler();

                //Calling the post delayed method of the handler to take control when the animation
                // will take place. We take the corresponding button from the array buttons and set
                // his color to the version selected. We use mChaneButtonColorToOn as a constant
                // time and then will be incremented by the mAnimationtime each time.We need to do
                // that to be able to set the time of the animation each time
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mButtonArrayList.get(mModel.getButtonIndex(indexOfColorSeq)).setBackgroundColor(mModel.getColorOn(indexOfColorSeq));
                    }
                }, mModel.computeOnTime(numAnimation));

                //Creation of a second handler, that one will change the color of the button to his
                // normal state.
                final Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        mButtonArrayList.get(mModel.getButtonIndex(indexOfColorSeq))
                                .setBackground(ResourcesCompat.getDrawable(getResources()
                                        , mModel.getColorOff(indexOfColorSeq), null));
                    }
                }, mModel.computeOffTime(numAnimation));
            }

            //Creation of the third and final handler that will change the state of the app to
            // UserPlay once the sequence it's finished, we make sure of that setting the time
            // related to the number of animations
            Handler handlerChangeState = new Handler();
            handlerChangeState.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mModel.mGameIsPlaying) {
                        mModel.mGameState = ModelMemoryColor.GameState.GameUserPlay;
                    }
                }
            }, mModel.computeChangeStateTime());

        }
    }

    //storing buttons for easy level
    private void storingButtonsForEasyLevel(ArrayList<Button> btnArray) {

        btnArray.clear();
        //Take reference of all the buttons on the layout
        final Button btnRed = (Button) findViewById(R.id.btn_red);
        final Button btnBlue = (Button) findViewById(R.id.btn_blue);
        final Button btnGreen = (Button) findViewById(R.id.btn_green);
        final Button btnCyan = (Button) findViewById(R.id.btn_cyan);
        final Button btnPink = (Button) findViewById(R.id.btn_pink);
        final Button btnYellow = (Button) findViewById(R.id.btn_yellow);

        //Storage of the buttons
        btnArray.add(btnRed);
        btnArray.add(btnBlue);
        btnArray.add(btnGreen);
        btnArray.add(btnCyan);
        btnArray.add(btnPink);
        btnArray.add(btnYellow);
    }

    //storing buttons for medium level
    private void storingButtonsForMediumLevel(ArrayList<Button> btnArray) {

        //first we call that method to storage the buttons from easy level
        storingButtonsForEasyLevel(btnArray);

        //Taking references of the new buttons on medium level
        final Button btnOrange = (Button) findViewById(R.id.btn_orange);
        final Button btnGrey = (Button) findViewById(R.id.btn_grey);

        //And storing it
        btnArray.add(btnOrange);
        btnArray.add(btnGrey);
    }

    //Method to storage the buttons for hard level
    private void storingButtonsForHardLevel(ArrayList<Button> btnArray) {

        //first we call that method to storage the buttons from medium level
        storingButtonsForMediumLevel(btnArray);

        //Taking references of the new buttons on hard level
        final Button btnStrongGreen = (Button) findViewById(R.id.btn_strong_green);
        final Button btnPurple = (Button) findViewById(R.id.btn_purple);

        //And storing it
        btnArray.add(btnStrongGreen);
        btnArray.add(btnPurple);
    }

    //Method to handle the onClicklisteners of the buttons
    private void colorBtnClicked(int colorNumber) {

        //First we make sure that the state of the app it's GameUserPlay if it's not return so the
        // button when clicked will do nothing
        if (mModel.mGameState != ModelMemoryColor.GameState.GameUserPlay) {
            return;
        }

        //Creation of a reference to the current user to be used later on to check if the current
        // user is the last of the current round
        mModel.mPreviousPlayer = mModel.mCurrentPlayer;

        //We check if the color stored on the array list of colors of current player matches with
        // the color given by the method witch is the int stored on the buttons as a Tag, in other
        // words we check if the user has clicked the right button

        if (mModel.checkUserAnswer(colorNumber)) {
            rightClickHandle();
            //We check if the user has arrived at the end of the sequence
            if (mModel.checkUserHasFinishedSequence()) {
                mModel.endRightClickTurn();
                gameAnimation();
            }

        } else {
            wrongClickHandle();
            //Here we check if there are any players left by checking if goToNextPlayer() returns
            // false, if so that means that there are no more players alive so we set the state of
            // the game to inactive as the game it's over, we call the method gameFinish() to reset
            // the variables of the game and show a toast indicating that the game it's over
            if (!mModel.goToNextPlayer()) {
                mModel.mGameState = ModelMemoryColor.GameState.GameInactive;
                gameFinish();
                Toast.makeText(getBaseContext(),
                        "Incorrect color! No more players alive! GAME OVER!",
                        Toast.LENGTH_SHORT).show();

                //If there is any more players alive first we check if the round it's over by
                // comparing the previous player reference with the current one (note that the
                // current one has changed since we've called goToNextPlayer()) if the previous
                // player it's bigger than the current one that means that the round it's over or
                // if are equal that there's just one player left playing, either way the round
                // it's over so we increment the number of animations by 1, we reset the
                // mNumClicksUser, we call the method gameAnimation() and after checking that
                // the new players name it's correct we show a message with toast indicating the
                // next turn with the new player name
            } else {
                prepareNextTurn();

                gameAnimation();
            }
        }
    }

    // Method to retrieve the data from the activity started by startActivityForResult() given
    // a request code witch will return the name of the players introduced by the user with an
    // ArrayList and the number of players to play with an int witch by default we
    // set to 1. We also start the array list that holds the score of each player by setting all
    // index till (the number of players - 1) to 0.

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == AppCompatActivity.RESULT_OK) {

                mModel.resetPlayersScore();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //Here we save the data to be retrieved on onCreate. We save the number of players
    // and their names.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("numberOfPlayers", mModel.mNumberOfPlayers);
        for (int i = 0; i < mModel.mNumberOfPlayers; i++) {
            savedInstanceState.putString("player" + Integer.toString(i), mModel.mPlayerNames.get(i));
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    public void settingLayoutWithDifficulty() {
        String difficulty = mModel.getDifficulty();

        if (difficulty == null) {
            difficulty = "Easy";
        }

        if (difficulty.compareTo("Easy") == 0) {
            mModel.setEasyLevel();
            setContentView(R.layout.activity_game_screen_easy);
            settingDataForEasyLevel();

        } else if (difficulty.compareTo("Medium") == 0) {
            mModel.setMediumLevel();
            setContentView(R.layout.activity_game_screen_medium);
            settingDataForMediumLevel();

        } else if (difficulty.compareTo("Hard") == 0) {
            mModel.setHardLevel();
            setContentView(R.layout.activity_game_screen_hard);
            settingDataForHardLevel();

        } else {
            mModel.setEasyLevel();
            setContentView(R.layout.activity_game_screen_easy);
            settingDataForEasyLevel();
        }
    }

    public void showPlayerTurn() {
        if (mModel.mPlayerNames.get(mModel.mCurrentPlayer) != null) {
            Toast.makeText(mContext, mModel.mPlayerNames.get(mModel.mCurrentPlayer) + " your turn!",
                    Toast.LENGTH_SHORT).show();

            //Making toast for every new turn in multiplayer when there's just 1 player alive or
            // in single player mode
        } else {
            Toast.makeText(mContext, "New round " + mModel.mPlayerNames.get(mModel.mCurrentPlayer),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void prepareNextTurn() {
        mModel.mNumClicksUser = 0;
        if (mModel.mPreviousPlayer>= mModel.mCurrentPlayer) {
            mModel.mCurrNumberOfAnimations++;
            if ((mModel.mPlayerNames.get(mModel.mCurrentPlayer) != null)) {
                Toast.makeText(mContext, mModel.mPlayerNames.get(mModel.mCurrentPlayer)
                        + " your turn!", Toast.LENGTH_SHORT).show();
            }
            //Just in case by any unknown reason the player name it's null we set an
            // alternative message without using the player name, this code should never be
            // executed just for reassurance
            else {
                Toast.makeText(mContext, " Next round! Keep it up!!"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void settingSoundAndVibrationPreferences() {

        Boolean soundSwitch = mModel.mSharedPreferences.getBoolean("soundSwitch", true);

        if (soundSwitch) {
            mpRightAnswer.setVolume(1, 1);
            mpWrongAnswer.setVolume(1, 1);
            mpWrongAnswerShort.setVolume(1, 1);

        } else {
            mpRightAnswer.setVolume(0, 0);
            mpWrongAnswer.setVolume(0, 0);
            mpWrongAnswerShort.setVolume(0, 0);
        }

        Boolean vibrationSwitch = mModel.mSharedPreferences.getBoolean("vibrationSwitch", true);

        if (vibrationSwitch) {
            mModel.mVibrationOn = true;
        } else {
            mModel.mVibrationOn = false;
        }
    }

    public void rightClickHandle() {
        //The user has clicked the right color so first we check if the media object
        // mRightAnswer is playing if so we'll stop it and prepare it to finally
        // play it again
        if (mpRightAnswer.isPlaying()) {
            mpRightAnswer.stop();
            try {
                mpRightAnswer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mpRightAnswer.start();

            //if mRightAnswer is not playing we just reproduce it
        } else {
            mpRightAnswer.start();
        }

        //We increment the variable that holds the number of clicks that the user has done
        mModel.mNumClicksUser++;
    }

    public void wrongClickHandle() {
        // The user has clicked the wrong button, first we reproduce the media object that
        // contains the sound of the wrong answer and make the device vibrate if it's not
        // turned off
        if (mModel.mNumPlayersAlive > 1) {
            mModel.mNumPlayersAlive--;
            mpWrongAnswerShort.start();
        } else {
            mpWrongAnswer.start();
        }

        if (mModel.mVibrationOn) {

            mVibrator.vibrate(700);
        }

        //Here we'll take the score, the difficulty and the player id to store the score of the
        // player that just ended his game
        int score = mModel.mPlayersScore.get(mModel.mCurrentPlayer);
        String playerName = mModel.mPlayerNames.get(mModel.mCurrentPlayer);
        int playerId = mModel.getIdFromPlayerName(playerName);

        //First we check if the player id is correct and if so, we insert into scores table
        if (playerId > 0) {
            mModel.insertScore(score, mModel.mDifficultyInt, playerId);
        }

        //Here we set the boolean of the mModel.mCurrentPlayer of the ArrayList<Boolean> to false to
        // indicate that the player is dead
        mModel.mPlayerState.set(mModel.mCurrentPlayer, Boolean.FALSE);
    }

}




