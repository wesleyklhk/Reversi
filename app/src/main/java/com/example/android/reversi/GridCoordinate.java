package com.example.android.reversi;

import android.graphics.Color;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by wlau on 2015-10-21.
 */
public class GridCoordinate {
    private int x;
    private int y;
    private String state;
    private ImageView button;

    public GridCoordinate(int x,int y, String state,ImageView button){
        setX(x);
        setY(y);
        setButton(button);
        setState(state);

    }

    public GridCoordinate(int x,int y, ImageView button){
        setX(x);
        setY(y);
        setButton(button);
        setState(States.NONE);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getState() {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
        switch (state){
            case States.NONE:
                //button.setBackgroundColor(Color.GREEN);
                button.setImageResource(R.drawable.transparent);
                break;
            case States.BLACK:
                //button.setBackgroundColor(Color.BLACK);
                button.setImageResource(R.drawable.black_chess);
                break;
            case States.WHITE:
                //button.setBackgroundColor(Color.WHITE);
                button.setImageResource(R.drawable.white_chess);
                break;
        }
    }

    public ImageView getButton() {
        return button;
    }

    public void setButton(ImageView button) {
        this.button = button;
    }
}
