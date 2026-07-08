package com.example.connectfourgame;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class HomeScreen extends AppCompatActivity implements NetworkConnection.NetworkCallback {

    private EditText ipAddressEditText;
    private EditText usernameEditText;
    private Button connectButton;
    private ListView playersListView;

    private ArrayList<String> activePlayersList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private NetworkConnection networkConnection;
    private String myUsername = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        ipAddressEditText = findViewById(R.id.ipAddressEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        connectButton = findViewById(R.id.connectButton);
        playersListView = findViewById(R.id.playersListView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, activePlayersList);
        playersListView.setAdapter(adapter);

        networkConnection = NetworkConnection.getInstance();

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipAddressEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();

                if (username.isEmpty() || ip.isEmpty()) {
                    Toast.makeText(HomeScreen.this, "Sva polja moraju biti popunjena!", Toast.LENGTH_SHORT).show();
                    return;
                }

                myUsername = username;
                networkConnection.setCallback(HomeScreen.this);
                networkConnection.connect(ip, 4925, username);

                connectButton.setEnabled(false);
                usernameEditText.setEnabled(false);
                ipAddressEditText.setEnabled(false);
                Toast.makeText(HomeScreen.this, "Povezivanje...", Toast.LENGTH_SHORT).show();
            }
        });

        playersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedPlayer = activePlayersList.get(position);
                networkConnection.sendMessage("INVITATION;" + clickedPlayer);
                Toast.makeText(HomeScreen.this, "Igrac: " + clickedPlayer + " je izazvan.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkConnection.setCallback(this);
    }

    @Override
    public void onPlayersListReceived(String[] players) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activePlayersList.clear();
                for (String p : players) {
                    String trimmed = p.trim();
                    if (!trimmed.isEmpty() && !trimmed.equals(myUsername)) {
                        activePlayersList.add(trimmed);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onChallengeReceived(final String challengerName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
                builder.setTitle("Stigao je izazov!");
                builder.setMessage("Igrač '" + challengerName + "' vas izaziva na partiju Connect Four. Da li prihvatate?");
                builder.setCancelable(false);

                builder.setPositiveButton("Prihvati", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        networkConnection.sendMessage("RESPONSE;ACCEPT;" + challengerName);
                    }
                });

                builder.setNegativeButton("Odbij", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        networkConnection.sendMessage("RESPONSE;DECLINE;" + challengerName);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public void onGameStarted(final String role, final String rivalName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HomeScreen.this, "onGameStarted pokrenut! Uloga: " + role, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(HomeScreen.this, MainActivity.class);
                intent.putExtra("ROLE", role);
                intent.putExtra("RIVAL_NAME", rivalName);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onChallengeRejected(String rivalName) {
        Toast.makeText(this, "Igrač " + rivalName + " je odbio vašu pozivnicu.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMoveReceived(int column) {}

    @Override
    public void onRivalLeft() {}

    @Override
    public void onRestartReceived() {}
}