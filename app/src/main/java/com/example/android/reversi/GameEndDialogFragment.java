package com.example.android.reversi;


import android.os.Bundle;
import android.app.Fragment;
import android.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameEndDialogFragment extends DialogFragment {


    public GameEndDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_game_end_dialog, container, false);
        String message = getArguments().getString("message");
        if (message.equals(GameEndingStates.BLACK_WIN)){
            ((ImageView)rootView.findViewById(R.id.winner)).setImageResource(R.drawable.black_chess);
            ((TextView)rootView.findViewById(R.id.tvEndGameMessage)).setText("WINS GG");
        }else if (message.equals(GameEndingStates.WHITE_WIN)){
            ((ImageView)rootView.findViewById(R.id.winner)).setImageResource(R.drawable.white_chess);
            ((TextView)rootView.findViewById(R.id.tvEndGameMessage)).setText("WINS GG");
        }else{
            ((TextView)rootView.findViewById(R.id.tvEndGameMessage)).setText("DRAW");
        }

        rootView.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RestartCallBack) getActivity()).restartGame();
                dismiss();
            }
        });
        return rootView;
    }

    public interface RestartCallBack{
        public void restartGame();
    }


}
