package com.course.memorycolor.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.course.memorycolor.R;
import com.course.memorycolor.data.PlayerNameAndScoreHandler;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Ernest on 07/10/2017.
 */

public class ModelMemoryColor {

    private static ModelMemoryColor mInstance;
    int[] colorsOn;
    TypedArray drawablesOff;
    public SharedPreferences mSharedPreferences;
    public int mPreviousPlayer;
    public ModelMemoryColor(Context context){

        mContext = context;

        //Initialization of the variable member mPlayerScoreHandler that will be used to operate
        // with the database
        mPlayerNameAndScoreHandler = new PlayerNameAndScoreHandler(mContext);

        //Get the arrays colors
        colorsOn = mContext.getResources().getIntArray(R.array.colorsOn);
        drawablesOff = mContext.getResources().obtainTypedArray(R.array.drawables_off);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }


//    public static ModelMemoryColor initialize(Context context){
//        if (mInstance == null)
//        {
//            mInstance = new ModelMemoryColor(context);
//        }
//        return mInstance;
//    }
//
//    public static ModelMemoryColor getInstance() throws Exception {
//        if (mInstance == null)
//        {
//            throw new Exception("No ModelMemoryColor Initialized, initialize the model first");
//        }
//        return mInstance;
//    }

    public enum GameState {
        GameInactive, GameAnimating, GameUserPlay
    }

    //Difficulty levels
    // To Model
    public enum GameDifficulty {
        GameEasy, GameMedium, GameHard
    }

    //Array list of boolean of player state, to know if a player is still playing(true) or not
    // (false)
    // To Model
    public ArrayList<Boolean> mPlayerState = new ArrayList<>();

    //Database handler object
    public PlayerNameAndScoreHandler mPlayerNameAndScoreHandler;

    //int to determine the current player, it's related with array index so will be initiate
    // with 0
    public int mCurrentPlayer;

    //int to determine the number of players of the game
    // To Model
    public int mNumberOfPlayers;

    //Array list that contains all the player names of the game
    // To Model
    public ArrayList<String> mPlayerNames = new ArrayList<>();

    //Array list that contains the scores of the players at the current game, it's meant to be
    // related with mPlayerNames so mPlayersScore.get(0)it's the score of mPlayerNames.get(0)
    // To Model
    public ArrayList<Integer> mPlayersScore = new ArrayList<>();

    //Int to determine the difficulty of the game being easy 1 medium 2 and hard 3
    // To Model
    public int mDifficultyInt;

    //Array list of the Arrays lists that hold the sequence of colors to be shown for each player
    // To Model
    public ArrayList<ArrayList<Integer>> mColorSequence = new ArrayList<ArrayList<Integer>>();

    //Int that keeps the number of buttons clicked by the user, it's used to compare it with
    // the index of the array of colors to know if the color it's the right one or not
    // To Model
    public int mNumClicksUser = 0;

    //**These set of variables are related to the animations
    // and are initialized on easy level**\\

    //The number of animations
    // To Model
    // Change Name to : mNumberOfColorsToShow
    public int mCurrNumberOfAnimations = 2;

    //These three ints are also related and basically they determine the time that the
    // buttons animation will take. They have to comply the rule mChangeButtonColorToOn
    // + mAnimationsTime > mChangeButtonColorToOff.

    //int that determines when the next set of animations will take part in ms
    // To Model
    public int mAnimationsTime = 1100;

    //int that determines when ,in ms, the color of the button will change to the color version
    //selected
    // To Model
    public int mChangeButtonColorToOn = 1500;

    //int that determines , in ms, when the color of the button will return to his default state
    // To Model
    public int mChangeButtonColorToOff = 2500;


    //the string default player name, used when the player doesn't introduce a name, the app will
    // take this default name as a player name and it will be when the app it's first installed
    // "player1 and from now on will be the last name introduced by the user, in a multi player game
    // the default name to stored will be the name of the first player
    // To Model
    public String mDefaultPlayerName;

    //boolean related with settings that tels if user has selected mode vibration on or off
    // To Model
    public boolean mVibrationOn;

    //boolean to check if the game is stopped or not
    // To Model
    public boolean mGameIsPlaying;

    //int to keep count of the number of players alive on the game
    // To Model
    public int mNumPlayersAlive;

    private Context mContext;

    //Variable that tels us the state of the game, initialized in inactive state
    // To Model
    public GameState mGameState = GameState.GameInactive;

    //Variable that tels us the difficulty of the game, initialized on easy level by default
    // To Model
    public GameDifficulty mDifficulty = GameDifficulty.GameEasy;

    //public MediaPlayer mpRightAnswer = MediaPlayer.create(mContext, R.raw.right_answer);

    public int getIdFromPlayerName(String playerName){
        return mPlayerNameAndScoreHandler.getIdFromPlayerName(playerName);
    }

    public void insertScore(int score, int difficulty, int playerId){
        mPlayerNameAndScoreHandler.insertScore(score, difficulty, playerId);
    }

    public void retrieveSaveInstanceState(Bundle savedInstanceState) {
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
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            mDefaultPlayerName = preferences.getString("defaultName", mContext.getResources().
                    getString(R.string.default_player_name));
            mPlayerNames.add(0, mDefaultPlayerName);
            mNumberOfPlayers = 1;

        }
    }

    //Method that returns a random integer assuring that it's not repeated so there'nt will
    // be the same color repeated on the sequence
    public int getNextRandomColor(Random rand) {

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

    public void generateNextColorSequence() {

        mGameState = ModelMemoryColor.GameState.GameAnimating;
        //Creation of the object Random
        Random rand = new Random();

        if (mCurrNumberOfAnimations == getStartingAnimations()) {
            for (int i = 0; i < mCurrNumberOfAnimations; i++) {

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
    }

    public int getButtonIndex(int index){
        return mColorSequence.get(mCurrentPlayer).get(index);
    }

    public int getColorOn(int index){
        return colorsOn[mColorSequence.get(mCurrentPlayer).get(index)];
    }

    public int getColorOff(int index){
        return drawablesOff.getResourceId(mColorSequence.get(mCurrentPlayer).get(index), 0);
    }

    public int computeOnTime(int idxAnimInSeq){
        return idxAnimInSeq * mAnimationsTime + mChangeButtonColorToOn;
    }

    public int computeOffTime(int idxAnimInSeq){
        return idxAnimInSeq * mAnimationsTime + mChangeButtonColorToOff;
    }

    public int computeChangeStateTime(){
        return (mCurrNumberOfAnimations - 1) * mAnimationsTime + mChangeButtonColorToOff;
    }

    //method that returns the number of the first set of animations depending on the difficulty
    public int getStartingAnimations() {
        if (mDifficulty == ModelMemoryColor.GameDifficulty.GameEasy) {
            return 2;
        } else if (mDifficulty == ModelMemoryColor.GameDifficulty.GameMedium) {
            return 3;
        }
        return 4;
    }

    public void finishGame() {
        mCurrNumberOfAnimations = getStartingAnimations();
        mNumClicksUser = 0;
        mColorSequence.clear();
        mGameState = ModelMemoryColor.GameState.GameInactive;
        mGameIsPlaying = false;
    }

    public void settingVariablesForNewGame() {
        mCurrNumberOfAnimations = getStartingAnimations();
        //resetting the players alive variable
        mNumPlayersAlive = mNumberOfPlayers;
        mGameIsPlaying = true;
        //Setting the Game state to animating
        mGameState = ModelMemoryColor.GameState.GameAnimating;
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
        mCurrNumberOfAnimations = getStartingAnimations();

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
    }

    public void setEasyLevel() {
        mDifficulty = GameDifficulty.GameEasy;
        mDifficultyInt = 1;
        mChangeButtonColorToOn = 1500;
        mChangeButtonColorToOff = 2500;
        mAnimationsTime = 1100;

    }

    public void setMediumLevel() {
        mDifficulty = GameDifficulty.GameMedium;
        mDifficultyInt = 2;
        mAnimationsTime = 800;
        mChangeButtonColorToOn = 1000;
        mChangeButtonColorToOff = 1700;
    }

    public void setHardLevel() {
        mDifficulty = GameDifficulty.GameHard;
        mDifficultyInt = 3;
        mChangeButtonColorToOn = 1000;
        mChangeButtonColorToOff = 1500;
        mAnimationsTime = 600;
    }

    public String getDifficulty(){
       String difficulty = mSharedPreferences.getString("level",null);
       return difficulty;
    }

    public void saveDefaultPlayerName() {

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("defaultName", mDefaultPlayerName);
        editor.commit();
    }

    public void finishGameByUser() {
        mGameState = ModelMemoryColor.GameState.GameInactive;
        mNumClicksUser = 0;
        mGameIsPlaying = false;
    }

    public void resetPlayersScore() {
        for (int i = 0; i < mNumberOfPlayers - 1; i++) {
            mPlayersScore.add(i, 0);
        }
    }

    public void endRightClickTurn() {
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
        if (mPreviousPlayer>= mCurrentPlayer) {
            mCurrNumberOfAnimations++;
        }

        //Here we reset the variable nums of click user so will be ready for the next
        // sequence
        mNumClicksUser = 0;

    }

    public boolean checkUserAnswer (int colorNumber){
        if (mColorSequence.get(mCurrentPlayer).get(mNumClicksUser) == colorNumber){
            return true;
        }else {
            return false;
        }
    }

    public boolean checkUserHasFinishedSequence(){
        if(mColorSequence.get(mCurrentPlayer).size() == mNumClicksUser){
            return true;
        }else{
            return false;
        }
    }

}



