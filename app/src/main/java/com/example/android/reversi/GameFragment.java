package com.example.android.reversi;

/**
 * Created by wlau on 2015-10-24.
 */
import android.view.ViewGroup.LayoutParams;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class GameFragment extends Fragment {
    static final String COMMAND_RESTORE="RESTORE";
    static final String COMMAND_CREATE="CREATE";
    Context context;
    LinearLayout layout;
    //TableRow gameGridRowLayout;
    Button btnRestart;
    Button btnExtraFeature;
    Button btnExtraFeature2;
    TextView tvTurn;
    TextView tvBlackScore;
    TextView tvWhiteScore;
    Map<String,GridCoordinate> map;
    String turn;
    int whiteScore = 2;
    int blackScore = 2;
    Stack<List<GridCoordinate>> stack;
    Boolean hintOn = false;
    List<GridCoordinate> hints;
    //boolean testMode = true;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("map",(HashMap)map);
        outState.putString("turn",turn);
        outState.putInt("whiteScore",whiteScore);
        outState.putInt("blackScore",blackScore);
        outState.putSerializable("stack",stack);
//        outState.putBoolean("hintOn",hintOn);
//        outState.putParcelable("hints",hints);
//        outState.putSerializable("hints",hints);

    }


    public interface EndGameCallBack{
        public void onEndGame(String message);
    }
    public GameFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);
        context = getActivity();
        layout = (LinearLayout)rootView.findViewById(R.id.gameGridLayout);
        //gameGridRowLayout = (TableRow) rootView.findViewById(R.id.gameGridRowLayout);

        btnRestart = (Button) rootView.findViewById(R.id.btnRestart);
        btnExtraFeature2 = (Button) rootView.findViewById(R.id.btnExtraFeature2);
        btnExtraFeature = (Button) rootView.findViewById(R.id.btnExtraFeature);
        tvTurn = (TextView) rootView.findViewById(R.id.tvTurn);
        tvBlackScore = (TextView) rootView.findViewById(R.id.tvBlackScore);
        tvWhiteScore = (TextView) rootView.findViewById(R.id.tvWhiteScore);



        if (savedInstanceState != null){
            //restore
            map = (HashMap<String,GridCoordinate>)savedInstanceState.getSerializable("map");
            turn = savedInstanceState.getString("turn");
            whiteScore = savedInstanceState.getInt("whiteScore");
            blackScore = savedInstanceState.getInt("blackScore");
            stack = (Stack<List<GridCoordinate>>) savedInstanceState.getSerializable("stack");
            hintOn = false;
            restoreGrid();

        }else{
            initGrid();
        }

        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initGrid();
            }
        });
        btnExtraFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stack.size() > 0){
                    List<GridCoordinate> list = stack.pop();
                    GridCoordinate lastMove = list.get(list.size() - 1);
                    setTvTurn(lastMove.getState());
                    String invertedColor = lastMove.getState().equals(States.BLACK)?States.WHITE:States.BLACK;
                    for (int i = 0 ; i < list.size() - 1; i ++){
                        GridCoordinate coordinate = list.get(i);
                        coordinate.setState(invertedColor);
                        if (invertedColor.equals(States.BLACK)){
                            blackScore++;
                            whiteScore--;
                        }else{
                            blackScore--;
                            whiteScore++;
                        }
                    }
                    lastMove.setState(States.NONE);
                    if (invertedColor.equals(States.BLACK)){
                        whiteScore--;
                    }else{
                        blackScore--;
                    }

                    synTvWhiteScore();
                    synTvBlackScore();
                    turnOffHint();
                }
            }
        });


        btnExtraFeature2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                turnOnHint();
                if (!hintOn) {
                    turnOnHint();
                }else{
                    turnOffHint();
                }
            }
        });



        return rootView;
    }

    public void turnOnHint(){
        String player = turn;
        hints = lookForPossibleMove(player);
        for (GridCoordinate coordinate : hints) {
            //coordinate.getButton().setBackgroundColor(Color.YELLOW);
            if (player.equals(States.BLACK)) {
                coordinate.getButton().setImageResource(R.drawable.black_chess_t);
            } else {
                coordinate.getButton().setImageResource(R.drawable.white_chess_t);
            }
        }
        hintOn = true;
    }

    public void turnOffHint(){
        if ((hints != null) && (hints.size() > 0)) {
            for (GridCoordinate coordinate : hints) {
                //if (!((coordinate.getX() + "" + coordinate.getY()).equals(hints.get(hints.size() - 1).getX() + hints.get(hints.size() - 1).getY()))){
                    coordinate.setState(States.NONE);
                //}
            }
        }
        hints = null;
        hintOn = false;
    }


    public void constructGrid(String command){
        for (int i = 0 ; i < 8 ; i ++) {
//            TableRow rowLayout = new TableRow(context);
//            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT,0);
            LinearLayout rowLayout = new LinearLayout(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,0,1);
            params.weight = 1;
            rowLayout.setLayoutParams(params);
            for (int j = 0 ; j < 8 ; j ++) {
                ImageView button = new ImageView(context);
                button.setBackgroundResource(R.drawable.border);
                //ImageButton button = new ImageButton(context);
                //TableRow.LayoutParams btnParams = new TableRow.LayoutParams(0,TableRow.LayoutParams.FILL_PARENT);
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.FILL_PARENT,1);
                btnParams.weight = 1;
                button.setLayoutParams(btnParams);
                final int I = i;
                final int J = j;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(GameActivity.tag,I+""+J);
                        controller(I,J);
                        if (hintOn){
                            if ((hints != null) && (hints.size() > 0)) {
                                for (GridCoordinate coordinate : hints) {
                                    if (!((coordinate.getX() + "" + coordinate.getY()).equals(I + "" + J))) {
                                        coordinate.setState(States.NONE);
                                    }
                                }
                            }
                            hints = null;
//                            hintOn = false;
                            turnOnHint();
                        }
                    }
                });
                rowLayout.addView(button);
                if (command.equals(COMMAND_RESTORE)) {
                    map.get(i + "" + j).setButton(button);
                    map.get(i + "" + j).setState(map.get(i + "" + j).getState());
                }else{
                    map.put(i+""+j,new GridCoordinate(i,j,button));
                }
            }
            layout.addView(rowLayout);
        }
    }

    public void restoreGrid(){
//        for (int i = 0 ; i < 8 ; i ++) {
////            TableRow rowLayout = new TableRow(context);
////            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT,0);
//            LinearLayout rowLayout = new LinearLayout(context);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,0,1);
//            params.weight = 1;
//            rowLayout.setLayoutParams(params);
//            for (int j = 0 ; j < 8 ; j ++) {
//                ImageView button = new ImageView(context);
//                button.setBackgroundResource(R.drawable.border);
//                //ImageButton button = new ImageButton(context);
//                //TableRow.LayoutParams btnParams = new TableRow.LayoutParams(0,TableRow.LayoutParams.FILL_PARENT);
//                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.FILL_PARENT,1);
//                btnParams.weight = 1;
//                button.setLayoutParams(btnParams);
//                final int I = i;
//                final int J = j;
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Log.d(GameActivity.tag,I+""+J);
//                        controller(I,J);
//                        if (hintOn){
//                            if ((hints != null) && (hints.size() > 0)) {
//                                for (GridCoordinate coordinate : hints) {
//                                    if (!((coordinate.getX() + "" + coordinate.getY()).equals(I + "" + J))) {
//                                        coordinate.setState(States.NONE);
//                                    }
//                                }
//                            }
//                            hints = null;
//                        }
//
//                    }
//                });
//                rowLayout.addView(button);
//                map.get(i+""+j).setButton(button);
//                map.get(i+""+j).setState(map.get(i+""+j).getState());
//                //map.put(i+""+j,new GridCoordinate(i,j,button));
//            }
//            layout.addView(rowLayout);
//        }
        constructGrid(COMMAND_RESTORE);
        synTvBlackScore();
        synTvWhiteScore();
        setTvTurn(turn);
    }
    public void initGrid(){
        //Init
        hintOn = false;
        hints = null;
        stack = new Stack<>();
        turn = States.BLACK;
        blackScore = 2;
        synTvBlackScore();
        whiteScore = 2;
        synTvWhiteScore();
        layout.removeAllViews();
        map = new HashMap<>();

        constructGrid(COMMAND_CREATE);

//        for (int i = 0 ; i < 8 ; i ++) {
////            TableRow rowLayout = new TableRow(context);
////            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT,0);
//            LinearLayout rowLayout = new LinearLayout(context);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,0,1);
//            params.weight = 1;
//            rowLayout.setLayoutParams(params);
//            for (int j = 0 ; j < 8 ; j ++) {
//                ImageView button = new ImageView(context);
//                button.setBackgroundResource(R.drawable.border);
//                //ImageButton button = new ImageButton(context);
//                //TableRow.LayoutParams btnParams = new TableRow.LayoutParams(0,TableRow.LayoutParams.FILL_PARENT);
//                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.FILL_PARENT,1);
//                btnParams.weight = 1;
//                button.setLayoutParams(btnParams);
//                final int I = i;
//                final int J = j;
//                button.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Log.d(GameActivity.tag,I+""+J);
//                        controller(I,J);
//                        if (hintOn){
//                            if ((hints != null) && (hints.size() > 0)) {
//                                for (GridCoordinate coordinate : hints) {
//                                    if (!((coordinate.getX() + "" + coordinate.getY()).equals(I + "" + J))) {
//                                        coordinate.setState(States.NONE);
//                                    }
//                                }
//                            }
//                            hints = null;
//                        }
//
//                    }
//                });
//                rowLayout.addView(button);
//                map.put(i+""+j,new GridCoordinate(i,j,button));
//            }
//            layout.addView(rowLayout);
//        }

        //initial setting
        map.get(3+""+3).setState(States.WHITE);
        map.get(3+""+4).setState(States.BLACK);
        map.get(4+""+3).setState(States.BLACK);
        map.get(4+""+4).setState(States.WHITE);
        setTvTurn(States.BLACK);

//        //test case1
//        map.get(3+""+0).setState(States.BLACK);
//        map.get(3+""+1).setState(States.WHITE);
//        map.get(3+""+2).setState(States.WHITE);
//        map.get(3+""+4).setState(States.WHITE);
//        map.get(3+""+5).setState(States.WHITE);
//        map.get(3+""+6).setState(States.BLACK);
//        map.get(3+""+7).setState(States.WHITE);
//        setTvTurn(States.BLACK);

//        //test case2
//        map.get(0+""+3).setState(States.BLACK);
//        map.get(1+""+3).setState(States.WHITE);
//        map.get(2+""+3).setState(States.WHITE);
//        map.get(4+""+3).setState(States.WHITE);
//        map.get(5+""+3).setState(States.WHITE);
//        map.get(6+""+3).setState(States.BLACK);
//        map.get(7+""+3).setState(States.WHITE);
//        setTvTurn(States.BLACK);

//        //test case3
//        map.get(0+""+0).setState(States.BLACK);
//        map.get(1+""+1).setState(States.WHITE);
//        map.get(2+""+2).setState(States.WHITE);
//        map.get(4+""+4).setState(States.WHITE);
//        map.get(5+""+5).setState(States.WHITE);
//        map.get(6+""+6).setState(States.BLACK);
//        map.get(7+""+7).setState(States.WHITE);
//        setTvTurn(States.BLACK);

//        //test case4
//        map.get(0+""+(7-0)).setState(States.BLACK);
//        map.get(1+""+(7-1)).setState(States.WHITE);
//        map.get(2+""+(7-2)).setState(States.WHITE);
//        map.get(4+""+(7-4)).setState(States.WHITE);
//        map.get(5+""+(7-5)).setState(States.WHITE);
//        map.get(6+""+(7-6)).setState(States.BLACK);
//        map.get(7+""+(7-7)).setState(States.WHITE);
//        setTvTurn(States.BLACK);

//        //test case5
//        map.get(0+""+1).setState(States.BLACK);
//            map.get(1+""+2).setState(States.WHITE);
//            map.get(2+""+3).setState(States.WHITE);
//        map.get(0+""+4).setState(States.BLACK);
//            map.get(1+""+4).setState(States.WHITE);
//            map.get(2+""+4).setState(States.WHITE);
//        map.get(0+""+7).setState(States.BLACK);
//            map.get(1+""+6).setState(States.WHITE);
//            map.get(2+""+5).setState(States.WHITE);
//        map.get(3+""+7).setState(States.BLACK);
//            map.get(3+""+6).setState(States.WHITE);
//            map.get(3+""+5).setState(States.WHITE);
//        map.get(6+""+7).setState(States.BLACK);
//            map.get(5+""+6).setState(States.WHITE);
//            map.get(4+""+5).setState(States.WHITE);
//        map.get(7+""+0).setState(States.BLACK);
//            map.get(6+""+1).setState(States.WHITE);
//            map.get(5+""+2).setState(States.WHITE);
//            map.get(4+""+3).setState(States.WHITE);
//        map.get(7+""+4).setState(States.BLACK);
//            map.get(6+""+4).setState(States.WHITE);
//            map.get(5+""+4).setState(States.WHITE);
//            map.get(4+""+4).setState(States.WHITE);
//        map.get(3+""+0).setState(States.BLACK);
//            map.get(3+""+1).setState(States.WHITE);
//            map.get(3+""+2).setState(States.WHITE);
//            map.get(3+""+3).setState(States.WHITE);
//        setTvTurn(States.BLACK);

//        //test case6
//        map.get(3+""+2).setState(States.WHITE);
//        map.get(3+""+3).setState(States.WHITE);
//        map.get(3+""+4).setState(States.WHITE);
//        map.get(3+""+5).setState(States.WHITE);
//        map.get(4+""+2).setState(States.WHITE);
//        map.get(4+""+3).setState(States.WHITE);
//        map.get(4+""+4).setState(States.WHITE);
//        map.get(4+""+5).setState(States.WHITE);
//        map.get(5+""+2).setState(States.WHITE);
//        map.get(5+""+3).setState(States.WHITE);
//        map.get(5+""+4).setState(States.WHITE);
//        map.get(5+""+5).setState(States.WHITE);
//        map.get(5+""+6).setState(States.WHITE);
//        map.get(6+""+2).setState(States.WHITE);
//        map.get(6+""+3).setState(States.WHITE);
//        map.get(6+""+4).setState(States.WHITE);
//        map.get(6+""+5).setState(States.WHITE);
//        map.get(6+""+0).setState(States.BLACK);
//        map.get(6+""+1).setState(States.BLACK);
//        map.get(6+""+6).setState(States.BLACK);
//        map.get(6+""+7).setState(States.BLACK);
//        map.get(7+""+0).setState(States.BLACK);
//        map.get(7+""+1).setState(States.BLACK);
//        map.get(7+""+2).setState(States.BLACK);
//        map.get(7+""+3).setState(States.BLACK);
//        map.get(7+""+4).setState(States.BLACK);
//        map.get(7+""+5).setState(States.BLACK);
//        map.get(7+""+6).setState(States.BLACK);
//        map.get(7+""+7).setState(States.BLACK);
//        setTvTurn(States.WHITE);
//        controller(6,3);

////        test case7
//        map.get(0+""+0).setState(States.WHITE);
//        map.get(0+""+1).setState(States.WHITE);
//        map.get(0+""+2).setState(States.WHITE);
//        map.get(0+""+3).setState(States.WHITE);
//        map.get(0+""+4).setState(States.NONE);
//        map.get(0+""+5).setState(States.BLACK);
//        map.get(0+""+6).setState(States.BLACK);
//        map.get(0+""+7).setState(States.BLACK);
//        map.get(1+""+0).setState(States.WHITE);
//        map.get(1+""+1).setState(States.WHITE);
//        map.get(1+""+2).setState(States.WHITE);
//        map.get(1+""+3).setState(States.WHITE);
//        map.get(1+""+4).setState(States.WHITE);
//        map.get(1+""+5).setState(States.BLACK);
//        map.get(1+""+6).setState(States.WHITE);
//        map.get(1+""+7).setState(States.WHITE);
//        map.get(2+""+0).setState(States.BLACK);
//        map.get(2+""+1).setState(States.BLACK);
//        map.get(2+""+2).setState(States.BLACK);
//        map.get(2+""+3).setState(States.BLACK);
//        map.get(2+""+4).setState(States.BLACK);
//        map.get(2+""+5).setState(States.BLACK);
//        map.get(2+""+6).setState(States.WHITE);
//        map.get(2+""+7).setState(States.WHITE);
//        map.get(3+""+0).setState(States.BLACK);
//        map.get(3+""+1).setState(States.WHITE);
//        map.get(3+""+2).setState(States.BLACK);
//        map.get(3+""+3).setState(States.WHITE);
//        map.get(3+""+4).setState(States.WHITE);
//        map.get(3+""+5).setState(States.WHITE);
//        map.get(3+""+6).setState(States.WHITE);
//        map.get(3+""+7).setState(States.WHITE);
//        map.get(4+""+0).setState(States.BLACK);
//        map.get(4+""+1).setState(States.WHITE);
//        map.get(4+""+2).setState(States.BLACK);
//        map.get(4+""+3).setState(States.WHITE);
//        map.get(4+""+4).setState(States.WHITE);
//        map.get(4+""+5).setState(States.WHITE);
//        map.get(4+""+6).setState(States.BLACK);
//        map.get(4+""+7).setState(States.BLACK);
//        map.get(5+""+0).setState(States.BLACK);
//        map.get(5+""+1).setState(States.WHITE);
//        map.get(5+""+2).setState(States.BLACK);
//        map.get(5+""+3).setState(States.WHITE);
//        map.get(5+""+4).setState(States.WHITE);
//        map.get(5+""+5).setState(States.WHITE);
//        map.get(5+""+6).setState(States.WHITE);
//        map.get(5+""+7).setState(States.BLACK);
//        map.get(6+""+0).setState(States.BLACK);
//        map.get(6+""+1).setState(States.BLACK);
//        map.get(6+""+2).setState(States.WHITE);
//        map.get(6+""+3).setState(States.WHITE);
//        map.get(6+""+4).setState(States.WHITE);
//        map.get(6+""+5).setState(States.WHITE);
//        map.get(6+""+6).setState(States.BLACK);
//        map.get(6+""+7).setState(States.BLACK);
//        map.get(7+""+0).setState(States.BLACK);
//        map.get(7+""+1).setState(States.BLACK);
//        map.get(7+""+2).setState(States.BLACK);
//        map.get(7+""+3).setState(States.BLACK);
//        map.get(7+""+4).setState(States.BLACK);
//        map.get(7+""+5).setState(States.BLACK);
//        map.get(7+""+6).setState(States.BLACK);
//        map.get(7+""+7).setState(States.BLACK);
//        blackScore = 31;
//        synTvBlackScore();
//        whiteScore = 32;
//        synTvWhiteScore();
//        setTvTurn(States.BLACK);


////        test case8
//        map.get(0+""+0).setState(States.NONE);
//        map.get(0+""+1).setState(States.NONE);
//        map.get(0+""+2).setState(States.NONE);
//        map.get(0+""+3).setState(States.NONE);
//        map.get(0+""+4).setState(States.NONE);
//        map.get(0+""+5).setState(States.WHITE);
//        map.get(0+""+6).setState(States.WHITE);
//        map.get(0+""+7).setState(States.WHITE);
//        map.get(1+""+0).setState(States.NONE);
//        map.get(1+""+1).setState(States.NONE);
//        map.get(1+""+2).setState(States.NONE);
//        map.get(1+""+3).setState(States.NONE);
//        map.get(1+""+4).setState(States.BLACK);
//        map.get(1+""+5).setState(States.WHITE);
//        map.get(1+""+6).setState(States.WHITE);
//        map.get(1+""+7).setState(States.WHITE);
//        map.get(2+""+0).setState(States.BLACK);
//        map.get(2+""+1).setState(States.NONE);
//        map.get(2+""+2).setState(States.NONE);
//        map.get(2+""+3).setState(States.NONE);
//        map.get(2+""+4).setState(States.WHITE);
//        map.get(2+""+5).setState(States.WHITE);
//        map.get(2+""+6).setState(States.WHITE);
//        map.get(2+""+7).setState(States.WHITE);
//        map.get(3+""+0).setState(States.BLACK);
//        map.get(3+""+1).setState(States.BLACK);
//        map.get(3+""+2).setState(States.NONE);
//        map.get(3+""+3).setState(States.WHITE);
//        map.get(3+""+4).setState(States.WHITE);
//        map.get(3+""+5).setState(States.WHITE);
//        map.get(3+""+6).setState(States.WHITE);
//        map.get(3+""+7).setState(States.WHITE);
//        map.get(4+""+0).setState(States.BLACK);
//        map.get(4+""+1).setState(States.BLACK);
//        map.get(4+""+2).setState(States.BLACK);
//        map.get(4+""+3).setState(States.WHITE);
//        map.get(4+""+4).setState(States.WHITE);
//        map.get(4+""+5).setState(States.WHITE);
//        map.get(4+""+6).setState(States.WHITE);
//        map.get(4+""+7).setState(States.WHITE);
//        map.get(5+""+0).setState(States.BLACK);
//        map.get(5+""+1).setState(States.BLACK);
//        map.get(5+""+2).setState(States.BLACK);
//        map.get(5+""+3).setState(States.WHITE);
//        map.get(5+""+4).setState(States.WHITE);
//        map.get(5+""+5).setState(States.WHITE);
//        map.get(5+""+6).setState(States.WHITE);
//        map.get(5+""+7).setState(States.WHITE);
//        map.get(6+""+0).setState(States.BLACK);
//        map.get(6+""+1).setState(States.BLACK);
//        map.get(6+""+2).setState(States.BLACK);
//        map.get(6+""+3).setState(States.WHITE);
//        map.get(6+""+4).setState(States.WHITE);
//        map.get(6+""+5).setState(States.WHITE);
//        map.get(6+""+6).setState(States.WHITE);
//        map.get(6+""+7).setState(States.WHITE);
//        map.get(7+""+0).setState(States.BLACK);
//        map.get(7+""+1).setState(States.BLACK);
//        map.get(7+""+2).setState(States.BLACK);
//        map.get(7+""+3).setState(States.WHITE);
//        map.get(7+""+4).setState(States.BLACK);
//        map.get(7+""+5).setState(States.BLACK);
//        map.get(7+""+6).setState(States.WHITE);
//        map.get(7+""+7).setState(States.WHITE);
//        blackScore = 17;
//        synTvBlackScore();
//        whiteScore = 35;
//        synTvWhiteScore();
//        setTvTurn(States.WHITE);


////        test case9
//        map.get(0+""+0).setState(States.NONE);
//        map.get(0+""+1).setState(States.NONE);
//        map.get(0+""+2).setState(States.NONE);
//        map.get(0+""+3).setState(States.NONE);
//        map.get(0+""+4).setState(States.WHITE);
//        map.get(0+""+5).setState(States.WHITE);
//        map.get(0+""+6).setState(States.WHITE);
//        map.get(0+""+7).setState(States.WHITE);
//        map.get(1+""+0).setState(States.NONE);
//        map.get(1+""+1).setState(States.NONE);
//        map.get(1+""+2).setState(States.NONE);
//        map.get(1+""+3).setState(States.NONE);
//        map.get(1+""+4).setState(States.WHITE);
//        map.get(1+""+5).setState(States.WHITE);
//        map.get(1+""+6).setState(States.WHITE);
//        map.get(1+""+7).setState(States.WHITE);
//        map.get(2+""+0).setState(States.NONE);
//        map.get(2+""+1).setState(States.NONE);
//        map.get(2+""+2).setState(States.NONE);
//        map.get(2+""+3).setState(States.NONE);
//        map.get(2+""+4).setState(States.WHITE);
//        map.get(2+""+5).setState(States.WHITE);
//        map.get(2+""+6).setState(States.WHITE);
//        map.get(2+""+7).setState(States.WHITE);
//        map.get(3+""+0).setState(States.NONE);
//        map.get(3+""+1).setState(States.NONE);
//        map.get(3+""+2).setState(States.NONE);
//        map.get(3+""+3).setState(States.NONE);
//        map.get(3+""+4).setState(States.WHITE);
//        map.get(3+""+5).setState(States.WHITE);
//        map.get(3+""+6).setState(States.WHITE);
//        map.get(3+""+7).setState(States.WHITE);
//        map.get(4+""+0).setState(States.NONE);
//        map.get(4+""+1).setState(States.NONE);
//        map.get(4+""+2).setState(States.NONE);
//        map.get(4+""+3).setState(States.BLACK);
//        map.get(4+""+4).setState(States.WHITE);
//        map.get(4+""+5).setState(States.WHITE);
//        map.get(4+""+6).setState(States.WHITE);
//        map.get(4+""+7).setState(States.WHITE);
//        map.get(5+""+0).setState(States.NONE);
//        map.get(5+""+1).setState(States.NONE);
//        map.get(5+""+2).setState(States.NONE);
//        map.get(5+""+3).setState(States.WHITE);
//        map.get(5+""+4).setState(States.WHITE);
//        map.get(5+""+5).setState(States.WHITE);
//        map.get(5+""+6).setState(States.WHITE);
//        map.get(5+""+7).setState(States.WHITE);
//        map.get(6+""+0).setState(States.NONE);
//        map.get(6+""+1).setState(States.NONE);
//        map.get(6+""+2).setState(States.NONE);
//        map.get(6+""+3).setState(States.WHITE);
//        map.get(6+""+4).setState(States.WHITE);
//        map.get(6+""+5).setState(States.WHITE);
//        map.get(6+""+6).setState(States.WHITE);
//        map.get(6+""+7).setState(States.WHITE);
//        map.get(7+""+0).setState(States.NONE);
//        map.get(7+""+1).setState(States.NONE);
//        map.get(7+""+2).setState(States.NONE);
//        map.get(7+""+3).setState(States.WHITE);
//        map.get(7+""+4).setState(States.WHITE);
//        map.get(7+""+5).setState(States.WHITE);
//        map.get(7+""+6).setState(States.WHITE);
//        map.get(7+""+7).setState(States.WHITE);
//        blackScore = 1;
//        synTvBlackScore();
//        whiteScore = 35;
//        synTvWhiteScore();
//        setTvTurn(States.WHITE);

////        test case10
//        map.get(0+""+0).setState(States.WHITE);
//        map.get(0+""+1).setState(States.WHITE);
//        map.get(0+""+2).setState(States.WHITE);
//        map.get(7+""+0).setState(States.BLACK);
//        map.get(7+""+1).setState(States.WHITE);
//        map.get(7+""+2).setState(States.NONE);
//        blackScore = 1;
//        synTvBlackScore();
//        whiteScore = 4;
//        synTvWhiteScore();
//        setTvTurn(States.BLACK);
    }

    public void setTvTurn(String player){
        turn = player;
        tvTurn.setText(turn);
    }

    public void synTvBlackScore(){
        tvBlackScore.setText(""+blackScore);
    }


    public void synTvWhiteScore(){
        tvWhiteScore.setText(""+whiteScore);
    }




    public void controller(int i,int j){
        //get whos turn
        String player = turn;
        String opponent = player.equals(States.BLACK)?States.WHITE:States.BLACK;

            //validate the move
            List<GridCoordinate> list = validateMove(i,j,player);

            if (list.size() == 0) {//invalid move
                return;
            }

            //make moves and update scores
            for (GridCoordinate coordinate : list) {
                coordinate.setState(player);
                if (player.equals(States.BLACK)) {
                    blackScore++;
                    whiteScore--;
                } else {
                    blackScore--;
                    whiteScore++;
                }
            }
            map.get(i + "" + j).setState(player);
            if (player.equals(States.BLACK)) {
                blackScore++;
            } else {
                whiteScore++;
            }
            synTvWhiteScore();
            synTvBlackScore();

            //keep records of what we did
            list.add(map.get(i + "" + j));//ensure that the last element is always the players move
            stack.push(list);


        //anyone win? and if no 1 win, decide who goes next
        if (blackScore + whiteScore == 64){
            //someone win
            Log.d(GameActivity.tag,"64 pieces gg");
            if (blackScore > whiteScore){
                ((EndGameCallBack)(getActivity())).onEndGame(GameEndingStates.BLACK_WIN);
            }else if (blackScore < whiteScore){
                ((EndGameCallBack)(getActivity())).onEndGame(GameEndingStates.WHITE_WIN);
            }else{
                ((EndGameCallBack)(getActivity())).onEndGame(GameEndingStates.DRAW);
            }
            return;
        }
        if ((blackScore == 0) || (whiteScore == 0)){
            //someone win
            Log.d(GameActivity.tag,"one of them has 0 pieces gg");
            if (blackScore > whiteScore){
                ((EndGameCallBack)(getActivity())).onEndGame(GameEndingStates.BLACK_WIN);
            }else if (blackScore < whiteScore){
                ((EndGameCallBack)(getActivity())).onEndGame(GameEndingStates.WHITE_WIN);
            }else{
                ((EndGameCallBack)(getActivity())).onEndGame(GameEndingStates.DRAW);
            }
            return;
        }
        if (lookForPossibleMove(opponent).size() > 0){
            setTvTurn(opponent);
        }else{
            if (lookForPossibleMove(player).size() > 0 ){
                setTvTurn(player);
            }else{
                //no more moves from both players
                Log.d(GameActivity.tag,"no more moves from both gg");
                if (blackScore > whiteScore){
                    ((EndGameCallBack)(getActivity())).onEndGame(GameEndingStates.BLACK_WIN);
                }else if (blackScore < whiteScore){
                    ((EndGameCallBack)(getActivity())).onEndGame(GameEndingStates.WHITE_WIN);
                }else{
                    ((EndGameCallBack)(getActivity())).onEndGame(GameEndingStates.DRAW);
                }
                return;
            }
        }
    }

    public List<GridCoordinate> lookForPossibleMove(String role){
        List<GridCoordinate> list = new ArrayList<>();
        for (int i = 0 ; i < 8 ; i ++){
            for (int j = 0 ; j < 8 ; j++){
                if (validateMove(i,j,role).size() > 0){
                    list.add(map.get(i+""+j));
                }
            }
        }
        return list;
    }

    public List<GridCoordinate> validateMove(int i,int j,String player){
        List<GridCoordinate> list = new ArrayList<>();
        //get whos turn
        String opponent = player.equals(States.BLACK)?States.WHITE:States.BLACK;
        //validate the move
        if (map.get(i+""+j).getState().equals(States.NONE)) {
            //x-1,y-1
            list.addAll(explore(i, j, -1, -1, player));
            //x-1,y
            list.addAll(explore(i, j, -1, 0, player));
            //x-1,y+1
            list.addAll(explore(i, j, -1, 1, player));
            //x,y-1
            list.addAll(explore(i, j, 0, -1, player));
            //x,y+1
            list.addAll(explore(i, j, 0, 1, player));
            //x+1,y-1
            list.addAll(explore(i, j, 1, -1, player));
            //x+1,y
            list.addAll(explore(i, j, 1, 0, player));
            //x+1,y+1
            list.addAll(explore(i, j, 1, 1, player));
        }
        return list;
    }

    public List<GridCoordinate> explore(int x, int y,int dir_x,int dir_y,String player){
        List<GridCoordinate> list = new ArrayList<>();
        int newX = x + dir_x;
        int newY = y + dir_y;
        GridCoordinate current = map.get(newX+""+newY);
        while (true){
            if(current == null){
                return new ArrayList<>();
            }else{
                if (current.getState().equals(States.NONE)){
                    return new ArrayList<>();
                }else if (current.getState().equals(player)){
                    return list;
                } else{
                    list.add(current);
                    newX += dir_x;
                    newY += dir_y;
                    current = map.get(newX+""+newY);
                }
            }
        }
    }
}
