package com.example.android.reversi;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class GameActivity extends Activity implements GameFragment.EndGameCallBack,GameEndDialogFragment.RestartCallBack {
    GameFragment gameFragment;
    static final String tag = "GameActivity";
    static final String fragmentTag = "gameFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(tag,"just rotated0");
        gameFragment = new GameFragment();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //http://stackoverflow.com/questions/13509495/uses-of-fragment-tags
        if (savedInstanceState == null) {
            Log.d(tag,"just rotated1");
            getFragmentManager().beginTransaction()
                    .add(R.id.container, gameFragment,"fragmentTag")//tag the created fragment so I dont need to recreate a fragment
                    .commit();
            Log.d(tag,"just rotated2");
        }else{
            gameFragment = (GameFragment) getFragmentManager().findFragmentByTag("fragmentTag");
        }
        Log.d(tag,"just rotated3");
    }

    @Override
    public void onEndGame(String message) {
        //FragmentManager fm = this.getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putString("message",message);

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        // Create and show the dialog.
        DialogFragment newFragment = new GameEndDialogFragment();
        newFragment.setArguments(args);
        newFragment.show(ft, "dialog");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void restartGame() {
        Log.d(tag,"gameFragment is null = " + (gameFragment== null));
        Log.d(tag,"gameFragment.btnRestart is null = " + (gameFragment.btnRestart== null));
        gameFragment.btnRestart.performClick();
    }
}
