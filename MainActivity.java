package com.example.connectfourgame;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int EMPTY = 0;
    private static final int RED_PLAYER = 1;
    private static final int BLUE_PLAYER = 2;
    private int[][] matrix = new int[6][7];
    private ImageView[][] screenFields = new ImageView[6][7];
    private boolean firstPlayersTurn = true;
    private boolean activeGame = true;
    private boolean firstWasPlayerOne = true;
    private TextView statusTextView;
    private GridLayout boardGridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        boardGridLayout = findViewById(R.id.boardGridLayout);

        boardInitialization();
    }

    private void boardInitialization(){
        boardGridLayout.removeAllViews();
        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 7; j++){
                matrix[i][j] = EMPTY;
                ImageView field = new ImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = dpToPx(45);
                params.height = dpToPx(45);
                params.setMargins(6,6,6,6);
                field.setLayoutParams(params);

                field.setImageResource(R.drawable.empty_field);
                field.setTag(j);
                field.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        if (!activeGame) return;
                        int columnClicked = (int) v.getTag();
                        playTheMove(columnClicked);
                    }
                });
                screenFields[i][j] = field;
                boardGridLayout.addView(field);
            }
        }
        activeGame= true;
        updateStatusText();
    }

    private void playTheMove(int column){
        for(int i = 5; i >= 0; i--){
            if (matrix[i][column] == EMPTY){
                int trenPlayer = firstPlayersTurn ? RED_PLAYER : BLUE_PLAYER;
                matrix[i][column] = trenPlayer;

                if (firstPlayersTurn){
                    screenFields[i][column].setImageResource(R.drawable.red_circle);
                }else{
                    screenFields[i][column].setImageResource(R.drawable.blue_circle);
                }

                if (checkedVictory(i, column, trenPlayer)){
                    activeGame = false;
                    String winner = firstPlayersTurn ? "Red Player" : "Blue Player";
                    statusTextView.setText(winner + " won!");
                    newGameSwitchPlayer();
                    return;
                }
                firstPlayersTurn = !firstPlayersTurn;
                updateStatusText();
                return;
            }
        }
        Toast.makeText(this, "Column is full! Choose another one.", Toast.LENGTH_SHORT).show();
    }

    private void updateStatusText(){
        if (firstPlayersTurn){
            statusTextView.setText("Red Player's turn");
        }else{
            statusTextView.setText("Blue Player's turn");
        }
    }

    private int dpToPx(int dp){
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private boolean checkedVictory(int row, int column, int player){

        int counterHorizontaly = 0;
        for(int j = 0; j < 7; j++){
            if(matrix[row][j] == player){
                counterHorizontaly++;
                if (counterHorizontaly == 4) return true;
            }else{
                counterHorizontaly = 0;
            }
        }
        int counterVerticaly = 0;
        for(int i = 0; i < 6; i++){
            if(matrix[i][column] == player){
                counterVerticaly++;
                if (counterVerticaly == 4) return true;
            }else{
                counterVerticaly = 0;
            }
        }
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 4; j++){
                if (matrix[i][j] == player &&
                    matrix[i+1][j+1] == player &&
                        matrix[i+2][j+2] == player &&
                        matrix[i+3][j+3] ==  player){
                    return true;
                }
            }
        }

        for(int i = 3; i < 6; i++){
            for(int j = 0; j < 4; j++){
                if (matrix[i][j] == player &&
                        matrix[i-1][j+1] == player &&
                        matrix[i-2][j+2] == player &&
                        matrix[i-3][j+3] ==  player){
                    return true;
                }
            }
        }
        return false;
    }

    private void newGameSwitchPlayer(){
        boardGridLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                firstWasPlayerOne = !firstWasPlayerOne;
                firstPlayersTurn = firstWasPlayerOne;
                boardInitialization();

            }
        }, 5000);
    }
}














