package com.example.christian.albumoftheday;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.picasso.Picasso;
import com.vstechlab.easyfonts.EasyFonts;

public class MainActivity extends Activity implements

        PlayerNotificationCallback, ConnectionStateCallback, View.OnClickListener {

    public ImageView artwork;
    public TextView track;
    public TextView artist;
    public TextView album;
    public TextView year;

    public ServeAsyncTask serveAsyncTask = new ServeAsyncTask();
    public ComeAsyncTask comeAsyncTask = new ComeAsyncTask();
    public JuicyAsyncTask juicyAsyncTask = new JuicyAsyncTask();

    private boolean mIsPlaying = true;
    private Button mPlayButton;
    private Button mForwardButton;

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "b9fafb55d91a467e9741ebc5e4e3e6da";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "albumoftheday://callback";

    private static final String WEB_CLIENT_ID = "b91850f53bb543a187617c4d2c1e5491";

    private static final String WEB_REDIRECT_URI = "http://localhost:8888/callback";

    private Player mPlayer;
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        TextView title = (TextView) findViewById(R.id.title_text);
        title.setTypeface(EasyFonts.captureIt(this));

        track = (TextView) findViewById(R.id.track_name);
        artist = (TextView) findViewById(R.id.artist_name);
        album = (TextView) findViewById(R.id.album_name);
        year = (TextView) findViewById(R.id.album_year);
        artwork = (ImageView) findViewById(R.id.album_art);

        mPlayButton = (Button) findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(this);
        setButtonText();

        mForwardButton = (Button) findViewById(R.id.forward_button);
        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serveAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                    serveAsyncTask.cancel(true);
                    comeAsyncTask.execute();
                }

                if (comeAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                    comeAsyncTask.cancel(true);
                    juicyAsyncTask.execute();
                }

                if (juicyAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                    juicyAsyncTask.cancel(true);
                }
            }
        });

    }

//    @Override
//    public void onClick(View v) {
//        Intent i = new Intent(this, PlayerNotificationCallback.class);
//        if (mIsPlaying) {
//            startService(i);
//        } else {
//            stopService(i);
//        }
//        mIsPlaying = !mIsPlaying;
//        setButtonText();
//
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                    }


                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(this, PlayerNotificationCallback.class);
        if (mIsPlaying) {
            startService(i);
            serveAsyncTask.execute();
            comeAsyncTask.execute();
            juicyAsyncTask.execute();
        } else {
            stopService(i);
            mPlayer.pause();
        }
        mIsPlaying = !mIsPlaying;
        setButtonText();

    }


    void setButtonText() {
        mPlayButton.setText(mIsPlaying ? "PLAY" : "PAUSE");
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    public class ServeAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mPlayer.addConnectionStateCallback(MainActivity.this);
            mPlayer.addPlayerNotificationCallback(MainActivity.this);
            mPlayer.play("spotify:track:1Ic9pKxGSJGM0LKeqf6lGe");
            return null;
        }

        @Override
        protected void onPreExecute() {
            track.setText("All Apologies");
            artist.setText("Nirvana");
            album.setText("In Utero");
            year.setText("1993");
            Picasso.with(MainActivity.this).load("https://i.scdn.co/image/85ed8e478b36c6d65726b02dccedc32a7620dcce").into(artwork);
        }
    }

    public class ComeAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mPlayer.addConnectionStateCallback(MainActivity.this);
            mPlayer.addPlayerNotificationCallback(MainActivity.this);
            mPlayer.play("spotify:track:2EqlS6tkEnglzr7tkKAAYD");
            return null;
        }

        @Override
        protected void onPreExecute() {
            track.setText("Come Together");
            artist.setText("The Beatles");
            album.setText("Abbey Road");
            year.setText("1969");
            Picasso.with(MainActivity.this).load("https://i.scdn.co/image/31327f9fe6b6e0bd6e431a4add681397e95c6329").into(artwork);
        }
    }

    public class JuicyAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mPlayer.addConnectionStateCallback(MainActivity.this);
            mPlayer.addPlayerNotificationCallback(MainActivity.this);
            mPlayer.play("spotify:track:3xX9hoGEq1EuMGWZl8LQL1");
            return null;
        }

        @Override
        protected void onPreExecute() {
            track.setText("Juicy");
            artist.setText("The Notorious B.I.G");
            album.setText("Ready To Die");
            year.setText("1994");
            Picasso.with(MainActivity.this).load("https://i.scdn.co/image/987bfedb8be463a19340eebbbc2998694708616c").into(artwork);
        }
    }
}