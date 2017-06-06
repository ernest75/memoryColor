package com.course.memorycolor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.course.memorycolor.data.PlayerNameAndScoreHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;




public class GameScreen extends AppCompatActivity {


    //States of the game
    enum GameState {
        GameInactive, GameAnimating, GameUserPlay
    }

    //Difficulty levels
    enum GameDifficulty {
        GameEasy, GameMedium, GameHard
    }

    //** Variables member **\\

    //Reference to the context of the class
    private Context mContext = this;

    //Database handler object
    PlayerNameAndScoreHandler mPlayerNameAndScoreHandler;

    //Declaration of MediaPlayer objects as private here to be able to access it outside oncreate()
    private MediaPlayer mpRightAnswer;
    private MediaPlayer mpWrongAnswer;
    private MediaPlayer mpWrongAnswerShort;

    //Object menu
    private Menu mMenu;

    //Array list of boolean of player state, to know if a player is still playing(true) or not
    // (false)
    ArrayList<Boolean> mPlayerState = new ArrayList<>();

    //int to determine the current player, it's related with array index so will be initiate
    // with 0
    int mCurrentPlayer;

    //int to determine the number of players of the game
    private int mNumberOfPlayers;

    //Array list that contains all the player names of the game
    ArrayList<String> mPlayerNames = new ArrayList<>();

    //Array list that contains the scores of the players at the current game, it's meant to be
    // related with mPlayerNames so mPlayersScore.get(0)it's the score of mPlayerNames.get(0)
    ArrayList<Integer> mPlayersScore = new ArrayList<>();

    //Int to determine the difficulty of the game being easy 1 medium 2 and hard 3
    int mDifficultyInt;

    //Array list of the Arrays lists that hold the sequence of colors to be shown for each player
    ArrayList<ArrayList<Integer>> mColorSequence = new ArrayList<ArrayList<Integer>>();

    //Int that keeps the number of buttons clicked by the user, it's used to compare it with
    // the index of the array of colors to know if the color it's the right one or not
    int mNumClicksUser = 0;

    //Variable that tels us the state of the game, initialized in inactive state
    GameState mGameState = GameState.GameInactive;

    //Variable that tels us the difficulty of the game, initialized on easy level by default
    GameDifficulty mDifficulty = GameDifficulty.GameEasy;

    //**These set of variables are related to the animations
    // and are initialized on easy level**\\

    //The number of animations
    int mNumberOfAnimations = 2;

    //These three ints are also related and basically they determine the time that the
    // buttons animation will take. They have to comply the rule mChangeButtonColorToOn
    // + mAnimationsTime > mChangeButtonColorToOff.

    //int that determines when the next set of animations will take part in ms
    int mAnimationsTime = 1100;

    //int that determines when ,in ms, the color of the button will change to the color version
    //selected
    int mChangeButtonColorToOn = 1500;

    //int that determines , in ms, when the color of the button will return to his default state
    int mChangeButtonColorToOff = 2500;


    //the string default player name, used when the player doesn't introduce a name, the app will
    // take this default name as a player name and it will be when the app it's first installed
    // "player1 and from now on will be the last name introduced by the user, in a multi player game
    // the default name to stored will be the name of the first player
    String mDefaultPlayerName;

    //Object Vibrator to make the device vibrate when the user fails
    Vibrator mVibrator;

    //boolean related with settings that tels if user has selected mode vibration on or off
    boolean mVibrationOn;

    //boolean to check if the game is stopped or not
    boolean mGameIsPlaying;

    //int to keep count of the number of players alive on the game
    int mNumPlayersAlive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //** Retrieving data from the saveInstanceState Bundle **\\
        //First check if savedInstanceState is not null so has data on it
        if (savedInstanceState != null) {

            //Resetting the player names array to make sure that no name it's left on it
            mPlayerNames.clear();

            //Getting the number of players stored
            mNumberOfPlayers = savedInstanceState.getInt("numberOfPlayers", 0);

            //Getting the players names stored
            for (int i = 0; i < mNumberOfPlayers; i++) {
                mPlayerNames.add(i, savedInstanceState.getString("player"
                        + Integer.toString(i), ""));
            }


            //savedInstanceState is null so we set the default player name and the number of
            // players to 1
        } else {

            //We use shared preferences to retrieve the string stored as the default player name,
            // and if there isn't any will use "player1", that string should be used only when the
            // app it's first installed on the device if everything works smoothly...
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            mDefaultPlayerName = preferences.getString("defaultName", "player1");
            mPlayerNames.add(0, mDefaultPlayerName);
            mNumberOfPlayers = 1;

        }

        //Setting the players alive variable
        mNumPlayersAlive = mNumberOfPlayers;

        //Setting toolbar and layout, we have to take in consideration the int mDifficultyInt,
        // cause depending on the level of difficulty we'll charge a layout or another, to be able
        // to do that we have to set the data every time the user changes the difficulty cause the
        // layout will be changed as well and every time you charge a layout you lose all
        // references and have to set the data again. We use 3 methods to set the data
        // for each level
        if (mDifficultyInt == 1) {
            setContentView(R.layout.activity_game_screen_easy);
            settingDataForEasyLevel();
        } else if (mDifficultyInt == 2) {
            setContentView(R.layout.activity_game_screen_medium);
            settingDataForMediumLevel();
        } else if (mDifficultyInt == 3) {
            setContentView(R.layout.activity_game_screen_hard);
            settingDataForHardLevel();
        } else {
            setContentView(R.layout.activity_game_screen_easy);
            settingDataForEasyLevel();
        }

        //This is used to fix the orientation so the screen doesn't rotate in this activity
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Initialization of the variable member mPlayerScoreHandler that will be used to operate
        // with the database
        mPlayerNameAndScoreHandler = new PlayerNameAndScoreHandler(this);

        //Variable that tels us the state of the game, initialized in inactive state
        mGameState = GameState.GameInactive;

        //Initialization of MediaPLayer objects with the sounds to be reproduced either when the
        // user response is correct or not
        mpRightAnswer = MediaPlayer.create(this, R.raw.right_answer);
        mpWrongAnswer = MediaPlayer.create(this, R.raw.wrong_answer);
        mpWrongAnswerShort = MediaPlayer.create(this, R.raw.wrong_answer_short);




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


    }

    private void settingDataForMediumLevel() {

        //setting the data from easy level with this method
        settingDataForEasyLevel();

        //And adding the new buttons
        final Button btnOrange = (Button) findViewById(R.id.btn_orange);
        final Button btnGrey = (Button) findViewById(R.id.btn_grey);

        //setting listeners for this new buttons
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
    }

    private void settingDataForHardLevel() {

        //setting the data from medium level with this method
        settingDataForMediumLevel();

        //And adding the new buttons
        final Button btnStrongGreen = (Button) findViewById(R.id.btn_strong_green);
        final Button btnPurple = (Button) findViewById(R.id.btn_purple);

        //setting for this new buttons
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


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game_screen, menu);

        //getting reference of the menu of this activity
        mMenu = menu;
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

                mNumberOfAnimations = getStartingAnimations();
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
                                mGameState = GameState.GameInactive;
                                mNumClicksUser = 0;
                                mGameIsPlaying = false;
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
            intent.putStringArrayListExtra("arrayListNames", mPlayerNames);
            intent.putExtra("numberOfPlayers", mNumberOfPlayers);

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

    //method to finish the game and reset all variables
    private void gameFinish() {
        mMenu.getItem(0).setIcon(ContextCompat.getDrawable(
                mContext,
                android.R.drawable.ic_media_play));
        mNumberOfAnimations = getStartingAnimations();
        mNumClicksUser = 0;
        mColorSequence.clear();
        mGameState = GameState.GameInactive;
        mGameIsPlaying = false;
    }

    //method that returns the number of the first set of animations depending on the difficulty
    public int getStartingAnimations() {
        if (mDifficulty == GameDifficulty.GameEasy) {
            return 2;
        } else if (mDifficulty == GameDifficulty.GameMedium) {
            return 3;
        }
        return 4;
    }

    //method to start the game
    public void gameStart() {

        //resetting the players alive variable
        mNumPlayersAlive = mNumberOfPlayers;
        
        mGameIsPlaying = true;
        //Setting the Game state to animating
        mGameState = GameState.GameAnimating;
        //starting the game we set the current player to
        // 0, that is cause we are going to store the
        // scores and the player state to an ArrayList
        // and we are going to relate it.
        mCurrentPlayer = 0;

        //Resetting array of scores
        mPlayersScore.clear();

        //Setting the int array list that will store
        // the score of each player of the game
        // note that all scores are set to 0 to begin
        for (int i = 0; i < mNumberOfPlayers; i++) {
            mPlayersScore.add(i, 0);
        }

        //clearing the players state array (the one
        // that tells with a boolean if the player is
        // still playing(true) or not(false)) and
        // creating a new one with the number
        // of the current game players
        mPlayerState.clear();
        for (int i = 0; i < mNumberOfPlayers; i++) {
            mPlayerState.add(Boolean.TRUE);
        }

        //Setting the numberOFAnimations with the method
        // getStartingAnimations that will check
        // the difficulty and set the starting
        // animations accordingly
        mNumberOfAnimations = getStartingAnimations();

        //Resetting the variable that holds the number of clicks
        // of the user
        mNumClicksUser = 0;

        //Clearing the ArrayList of Arrays each
        // one of it contains the animation for each player
        mColorSequence.clear();

        //Adding the new Array Lists that will contain
        // the animation for each player
        for (int i = 0; i < mNumberOfPlayers; i++) {
            mColorSequence.add(new ArrayList<Integer>());
        }
        //And finally starting animation
        gameAnimation();

    }

    //Method to make the animations
    public void gameAnimation() {

        //Setting the state of the app to animating
        mGameState = GameState.GameAnimating;

        //Array list to store the buttons of the app, we need
        // to declare it here cause next methods will need it
        final ArrayList<Button> btnArray = new ArrayList<>();

        //checking difficulty of the game and storing buttons
        // with each proper method
        if (mDifficultyInt == 1) {
            storingButtonsForEasyLevel(btnArray);

        } else if (mDifficultyInt == 2) {

            storingButtonsForMediumLevel(btnArray);

        } else {

            storingButtonsForHardLevel(btnArray);

        }

        //Get the arrays colors
        final int[] colorsOn = getBaseContext().getResources().getIntArray(R.array.colorsOn);
        final TypedArray drawablesOff = getResources().obtainTypedArray(R.array.drawables_off);

        //Creation of the object Random
        Random rand = new Random();

        //calculating sequence
        //checking if the game just started and if so adding to the array list of the color sequence
        // of the player the number of starting animations
        if (mNumberOfAnimations == getStartingAnimations()) {
            for (int i = 0; i < mNumberOfAnimations; i++) {

                //Int to determine witch button it's going
                // to be on sequence randomly
                int random = getNextRandomColor(rand);

                //Adding the new color to the array list of the player
                mColorSequence.get(mCurrentPlayer).add(random);

            }

            //if the game already started we just add a new single color each time
        } else {
            int random = getNextRandomColor(rand);
            mColorSequence.get(mCurrentPlayer).add(random);


        }

        //First checking if the state it's the correct one, in this case animating state
        if (mGameState == GameState.GameAnimating) {

            //Making toast for every new turn for each player, first on multiplayer and with more
            // than 1 player alive
            if (mPlayerNames.get(mCurrentPlayer) != null) {
                Toast.makeText(getBaseContext(), mPlayerNames.get(mCurrentPlayer) + " your turn!",
                        Toast.LENGTH_SHORT).show();

                //Making toast for every new turn in multiplayer when there's just 1 player alive or
                // in single player mode
            } else {
                Toast.makeText(getBaseContext(), "New round " + mPlayerNames.get(mCurrentPlayer),
                        Toast.LENGTH_SHORT).show();
            }

            //Creating animations handler, here we recreate the animations of the buttons with the
            // stored colors in mColorsequence
            for (int i = 0; i < mNumberOfAnimations; i++) {

                //setting a reference to i, that is needed cause we need a final int to pass to the
                // handler, so each step of the for will create a new final int referencing i
                final int indexOfColorSeq = i;

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
                        btnArray.get(mColorSequence.get(mCurrentPlayer).get(indexOfColorSeq))
                                .setBackgroundColor(colorsOn[mColorSequence.get(mCurrentPlayer)
                                        .get(indexOfColorSeq)]);
                    }
                }, i * mAnimationsTime + mChangeButtonColorToOn);

                //Creation of a second handler, that one will change the color of the button to his
                // normal state.
                final Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        btnArray.get(mColorSequence.get(mCurrentPlayer).get(indexOfColorSeq))
                                .setBackground(ResourcesCompat.getDrawable(getResources()
                                        , drawablesOff.getResourceId(mColorSequence.
                                                get(mCurrentPlayer)
                                                .get(indexOfColorSeq), 0), null));
                    }
                }, i * mAnimationsTime + mChangeButtonColorToOff);
            }

            //Creation of the third and final handler that will change the state of the app to
            // UserPlay once the sequence it's finished, we make sure of that setting the time
            // related to the number of animations
            Handler handlerChangeState = new Handler();
            handlerChangeState.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mGameIsPlaying) {
                        mGameState = GameState.GameUserPlay;
                    }
                }
            }, (mNumberOfAnimations - 1) * mAnimationsTime + mChangeButtonColorToOff);


        }
    }

    //storing buttons for easy level
    private void storingButtonsForEasyLevel(ArrayList<Button> btnArray) {

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


    //Method that returns a random integer assuring that it's not repeated so there'nt will
    // be the same color repeated on the sequence
    private int getNextRandomColor(Random rand) {

        //Int that will be used to determine the range of the object random creation
        // initialized as 6, cause it's the number of buttons that the easy level
        // layout has
        int maxColorNumber = 6;

        //checking the difficulty and changing accordingly the value of maxColorNumber
        if (mDifficultyInt == 2) {
            maxColorNumber = 8;
        } else if (mDifficultyInt == 3) {
            maxColorNumber = 10;
        }

        //int random to return, but first we make sure that is not the same as the last one
        // created so the animation sequence doesn't repeat the same color
        int random = rand.nextInt(maxColorNumber);
        if (mColorSequence.get(mCurrentPlayer).size() > 0) {
            if (random == mColorSequence.get(mCurrentPlayer)
                    .get(mColorSequence.get(mCurrentPlayer).size() - 1)) {
                while (random == mColorSequence.get(mCurrentPlayer)
                        .get(mColorSequence.get(mCurrentPlayer).size() - 1)) {
                    random = rand.nextInt(maxColorNumber);
                }

            }
        }
        return random;
    }

    //Method to handle the onClicklisteners of the buttons
    private void colorBtnClicked(int colorNumber) {

        //First we make sure that the state of the app it's GameUserPlay if it's not return so the
        // button when clicked will do nothing
        if (mGameState != GameState.GameUserPlay) {
            return;
        }

        //Creation of a reference to the current user to be used later on to check if the current
        // user is the last of the current round
        int previousPlayer = mCurrentPlayer;

        //We check if the color stored on the array list of colors of current player matches with
        // the color given by the method witch is the int stored on the buttons as a Tag, in other
        // words we check if the user has clicked the right button, if the if it's true he has

        if (mColorSequence.get(mCurrentPlayer).get(mNumClicksUser) == colorNumber) {

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
            mNumClicksUser++;

            //We check if the user has arrived at the end of the sequence
            if (mColorSequence.get(mCurrentPlayer).size() == mNumClicksUser) {

                //If the user has arrived at the en of the sequence we increment his score by 10
                mPlayersScore.set(mCurrentPlayer, mPlayersScore.get(mCurrentPlayer) + 10);

                //And we go the next player
                goToNextPlayer();

                //Here we use the reference that we had of the current player to check with the
                //next player if it's on the same round or we have to start a new round, we do that
                // by comparing the reference of the last player stored with the current player
                // after we called the method goToNextPlayer(), if previousPlayer it's bigger than
                // current one that means that it's arrived at the end of the round and if it's
                // equal it means that there is just one player left playing or the game it's on
                // single mode either way the round it's over so we increment the numberOfAnimations
                // by 1
                if (previousPlayer >= mCurrentPlayer) {
                    mNumberOfAnimations++;
                }

                //Here we reset the variable nums of click user so will be ready for the next
                // sequence
                mNumClicksUser = 0;

                //An finally we call the gameAnimation() method that will start another set of
                // animations with one more color
                gameAnimation();
            }

        } else {

            // The user has clicked the wrong button, first we reproduce the media object that
            // contains the sound of the wrong answer and make the device vibrate if it's not
            // turned off
            

                if (mNumPlayersAlive > 1) {
                    mNumPlayersAlive--;
                    mpWrongAnswerShort.start();
                } else {
                    mpWrongAnswer.start();
                }

            if (mVibrationOn) {
                mVibrator = (Vibrator) this.mContext.getSystemService(Context.VIBRATOR_SERVICE);
                mVibrator.vibrate(700);
            }

            //Here we'll take the score, the difficulty and the player id to store the score of the
            // player that just ended his game
            int score = mPlayersScore.get(mCurrentPlayer);
            int difficulty = mDifficultyInt;
            String playerName = mPlayerNames.get(mCurrentPlayer);
            int playerId = mPlayerNameAndScoreHandler.getIdFromPlayerName(playerName);

            //First we check if the player id is correct and if so, we insert into scores table
            if (playerId > 0) {
                mPlayerNameAndScoreHandler.insertScore(score, difficulty, playerId);
            }

            //Here we set the boolean of the mCurrentPLayer of the ArrayList<Boolean> to false to
            // indicate that the player is dead
            mPlayerState.set(mCurrentPlayer, Boolean.FALSE);

            //Here we check if there are any players left by checking if goToNextPlayer() returns
            // false, if so that means that there are no more players alive so we set the state of
            // the game to inactive as the game it's over, we call the method gameFinish() to reset
            // the variables of the game and show a toast indicating that the game it's over
            if (!goToNextPlayer()) {
                mGameState = GameState.GameInactive;
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
                mNumClicksUser = 0;
                if (previousPlayer >= mCurrentPlayer) {
                    mNumberOfAnimations++;
                    if ((mPlayerNames.get(mCurrentPlayer) != null)) {
                        Toast.makeText(getBaseContext(), mPlayerNames.get(mCurrentPlayer)
                                + " your turn!", Toast.LENGTH_SHORT).show();
                    }
                    //Just in case by any unknown reason the player name it's null we set an
                    // alternative message without using the player name, this code should never be
                    // executed just for reassurance
                    else {
                        Toast.makeText(getBaseContext(), " Next round! Keep it up!!"
                                , Toast.LENGTH_SHORT).show();
                    }
                }

                gameAnimation();
            }
        }

    }

    // onResume used to retrieve the data of the preferences with shared preferences
    @Override
    protected void onResume() {
        super.onResume();

        // To use the preferences when the activity starts and when the user navigates back from
        // the settings activity.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String difficulty = preferences.getString("level", null);


        if (difficulty == null) {
            difficulty = "Easy";
            mDifficultyInt = 1;
            mDifficulty = GameDifficulty.GameEasy;

        }

        if (difficulty.compareTo("Easy") == 0) {
            mDifficulty = GameDifficulty.GameEasy;
            mDifficultyInt = 1;
            mChangeButtonColorToOn = 1500;
            mChangeButtonColorToOff = 2500;
            mAnimationsTime = 1100;
            setContentView(R.layout.activity_game_screen_easy);
            settingDataForEasyLevel();


        } else if (difficulty.compareTo("Medium") == 0) {
            mDifficulty = GameDifficulty.GameMedium;
            mDifficultyInt = 2;
            mAnimationsTime = 800;
            mChangeButtonColorToOn = 1000;
            mChangeButtonColorToOff = 1700;
            setContentView(R.layout.activity_game_screen_medium);
            settingDataForMediumLevel();

        } else if (difficulty.compareTo("Hard") == 0) {
            mDifficulty = GameDifficulty.GameHard;
            mDifficultyInt = 3;
            mChangeButtonColorToOn = 1000;
            mChangeButtonColorToOff = 1500;
            mAnimationsTime = 600;
            setContentView(R.layout.activity_game_screen_hard);
            settingDataForHardLevel();
        } else {
            mDifficulty = GameDifficulty.GameEasy;
            mDifficultyInt = 1;
            mAnimationsTime = 1100;
            mChangeButtonColorToOn = 1500;
            mChangeButtonColorToOff = 2500;
            setContentView(R.layout.activity_game_screen_easy);
            settingDataForEasyLevel();
        }

        Boolean soundSwitch = preferences.getBoolean("soundSwitch", true);

        if (soundSwitch) {
            mpRightAnswer.setVolume(1, 1);
            mpWrongAnswer.setVolume(1, 1);
            mpWrongAnswerShort.setVolume(1, 1);

        } else {
            mpRightAnswer.setVolume(0, 0);
            mpWrongAnswer.setVolume(0, 0);
            mpWrongAnswerShort.setVolume(0, 0);
        }

        Boolean vibrationSwitch = preferences.getBoolean("vibrationSwitch", true);

        if (vibrationSwitch) {
            mVibrationOn = true;
        } else {
            mVibrationOn = false;
        }


    }

    // Method used to go to next player on the game , it returns a boolean that will be true when
    // it finds another player alive and false when not.
    // We use a int to keep reference of the count of each time the while loop works.
    // On the while loop, the parameters to keep going are not to found a next player alive witch
    // is expressed with the boolean found, and to be sure that count is smaller than the
    // numberOfPlayers. We go to the next player by incrementing mCurrentPlayer variable,
    // then we check if we arrived to the last player on the array by comparing the value of the
    // current with the total number of players if are equal it means that we arrived to the end
    // of the array (Note: currentPlayer shouldn't be never equal to number total of players as we
    // initiate mCurrentPlayer to 0), and then we go to the first player by setting mCurrentPlayer
    // to 0. Then we ask if that player is still alive by checking the array list of Boolean, if
    // returns true we set the boolean found to true that will break the while loop and will finish
    // the method by returning true as we found a player alive, if next player isn't alive we
    // increment the count by 1 and the while loop will continue until it finds a new player alive
    // or the count is equal to the number of players witch it means that all players are dead
    // witch will make the method to finish and returning false as there are no players alive.

    public boolean goToNextPlayer() {

        boolean found = false;
        int count = 0;
        while (!found && count < mNumberOfPlayers) {
            mCurrentPlayer++;
            if (mCurrentPlayer == mNumberOfPlayers) {
                mCurrentPlayer = 0;

            }
            if (mPlayerState.get(mCurrentPlayer) == true) {
                found = true;
            } else {
                count++;
            }
        }

        if (count == mNumberOfPlayers) {
            return false;
        }
        return true;

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
                mNumberOfPlayers = data.getIntExtra("numberOfPlayers", 1);
                mPlayerNames = data.getStringArrayListExtra("namesArray");
                for (int i = 0; i < mNumberOfPlayers - 1; i++) {
                    mPlayersScore.add(i, 0);
                }
                mDefaultPlayerName = data.getStringExtra("defaultPLayer");
            }

            super.onActivityResult(requestCode, resultCode, data);
        }

      
    }

    //Here we save the data to be retrieved on onCreate. We save the number of players
    // and their names.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putInt("numberOfPlayers", mNumberOfPlayers);

        for (int i = 0; i < mNumberOfPlayers; i++) {
            savedInstanceState.putString("player" + Integer.toString(i), mPlayerNames.get(i));
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    //Here we save the default name player
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences defaultNamePreference = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = defaultNamePreference.edit();
        editor.putString("defaultName", mDefaultPlayerName);
        editor.commit();

    }
}




