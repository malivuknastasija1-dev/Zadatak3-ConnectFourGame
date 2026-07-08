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
    private NetworkConnection networkConnection;
    private String myRole= "";
    private Button rematchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rematchButton = findViewById(R.id.rematchButton);
        rematchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkConnection.sendMessage("RESTART");
                Toast.makeText(MainActivity.this, "Zahtev za revans poslat!", Toast.LENGTH_SHORT).show();
                rematchButton.setEnabled(false);
            }
        });
        
        networkConnection = NetworkConnection.getInstance();
        networkConnection.setCallback(this);

        Intent intent = getIntent();
        if (intent != null){
            myRole = intent.getStringExtra("ROLE");
            String rivalName = intent.getStringExtra("RIVAL_NAME");
            activeGame = true;
            firstPlayersTurn = true;
            Toast.makeText(this, "Protivnik: "+ rivalName, Toast.LENGTH_SHORT).show();
        }

        boardInitialization();
        updateStatusText();
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

                         if ((myRole.equals("RED_PLAYER") && !firstPlayersTurn) || (myRole.equals("BLUE_PLAYER") && firstPlayersTurn)){
                            Toast.makeText(MainActivity.this, "Sacekaj protibvnikov potez!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        int columnClicked = (int) v.getTag();
                        networkConnection.sendMessage("TURN;" + columnClicked);
                        playTheMove(columnClicked);
                    }
                });
                screenFields[i][j] = field;
                boardGridLayout.addView(field);
            }
        }
    }

    private void playTheMove(int column){
        for(int i = 5; i >= 0; i--){
            if (matrix[i][column] == EMPTY){
                int trenPlayer = firstPlayersTurn ? RED_PLAYER : BLUE_PLAYER;
                matrix[i][column] = trenPlayer;

                if (trenPlayer == RED_PLAYER){
                    screenFields[i][column].setImageResource(R.drawable.red_circle);
                }else{
                    screenFields[i][column].setImageResource(R.drawable.blue_circle);
                }

                if (checkedVictory(i, column, trenPlayer)){
                    activeGame = false;
                    String winner = firstPlayersTurn ? "Red Player" : "Blue Player";
                    statusTextView.setText(winner + " won!");
                    rematchButton.setVisibility(View.VISIBLE);
                    rematchButton.setEnabled(true);
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
            statusTextView.setText(myRole.equals("RED_PLAYER") ? "Na redu je CRVENI igrac" : "CRVENI igrac na redu, sacekajte...");
        }else{
            statusTextView.setText(myRole.equals("BLUE_PLAYER") ? "Na redu je PLAVI igrac" : "PLAVI igrac na redu, sacekajte...");
        }
    }

    
    @Override
    public void onPlayersListReceived(String[] players){
        for(final String p : players){
            final String trimmedPlayer = p.trim();
            if (!trimmedPlayer.isEmpty() && !trimmedPlayer.equals("Mobilni_Telefon")){
                Toast.makeText(MainActivity.this, "Automatski izazivam: " + trimmedPlayer, Toast.LENGTH_SHORT).show();
                networkConnection.sendMessage("INVITATION;" + p);
                break;
            }
        }
    }

    @Override
    public void onGameStarted(String playerRole, String rivalName){
        this.myRole = playerRole;
        this.activeGame = true;
        this.firstPlayersTurn = true;
        boardInitialization();
        Toast.makeText(this, "Partija je pocela! Protivnik: " + rivalName, Toast.LENGTH_LONG).show();
        updateStatusText();
    }

    @Override
    public void onMoveReceived(int column){
        playTheMove(column);
    }

    @Override
    public void onRivalLeft(){
        activeGame = false;
        statusTextView.setText("Protivnik je napustio igru.");
        Toast.makeText(this, "Protivnik se diskonektovao.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRestartReceived(){
        if (!activeGame) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Revans?");
            builder.setMessage("Protivnik vam nudi revans. Da li prihvatate?");
            builder.setCancelable(false);

            builder.setPositiveButton("Prihvati", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    networkConnection.sendMessage("RESTART");
                    resetBoardLocal();
                }
            });

            builder.setNegativeButton("Odbij", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "Odbili ste revans.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            builder.create().show();
        } else {
            resetBoardLocal();
        }
    }

    @Override public void onPlayersListReceived(String[] players) {}
    @Override public void onChallengeReceived(String challengerName) {}
    @Override public void onChallengeRejected(String rivalName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Protivnik je odbio revans.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
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

    private void resetBoardLocal() {
        boardInitialization();
        activeGame = true;
        firstPlayersTurn = myRole.equals("RED_PLAYER");
        updateStatusText();
        rematchButton.setVisibility(View.GONE);
    }

     @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}














