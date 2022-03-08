package m.ashutosh.videos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.PictureInPictureParams;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import static m.ashutosh.videos.VideoAdapter.videoFiles;

public class PlayerActivity extends AppCompatActivity {

    PlayerView playerView;
    SimpleExoPlayer simpleExoPlayer;
    private String vidID;
    private String EXO_PREFS = "exo_player_preferences";
    private long vidTotalDuration;
    int position = -1;
    private long quitTime;
    private Toast quitToast;
    private PictureInPictureParams.Builder pipBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            playerView = findViewById(R.id.exoplayer);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                pipBuilder = new PictureInPictureParams.Builder();
            }

            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            position = getIntent().getIntExtra("position", -1);
            String path = videoFiles.get(position).getPath();
            vidID = videoFiles.get(position).getId();
            vidTotalDuration = Integer.parseInt(videoFiles.get(position).getDuration());
            lastPlayed(path);

        } else {
            Toast.makeText(this, "No Permission", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (quitTime + 2000 > System.currentTimeMillis()) {

            quitToast.cancel();
            super.onBackPressed();
            return;

        } else {
            quitToast = Toast.makeText(getBaseContext(), "Press again to exit", Toast.LENGTH_SHORT);
            quitToast.show();
        }
        quitTime = System.currentTimeMillis();

    }

    private void playVideo(String path) {
        if (path != null) {
            Uri uri = Uri.parse(path);
            simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
            DataSource.Factory factory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Videos"));
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(factory, extractorsFactory).createMediaSource(uri);
            playerView.setPlayer(simpleExoPlayer);
            playerView.setKeepScreenOn(true);
            simpleExoPlayer.prepare(mediaSource);
            simpleExoPlayer.setPlayWhenReady(true);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(PlayerActivity.this);
            boolean landscapeView = preferences.getBoolean("autoRotate",false);
            if(landscapeView){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

        }
    }

    private void lastPlayed(String path) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(PlayerActivity.this);
        Boolean resumePlay = preferences.getBoolean("resume_play", false);
        if (resumePlay) {
            SharedPreferences preferences01 = getSharedPreferences(EXO_PREFS, MODE_PRIVATE);
            long mPosition = preferences01.getLong(vidID, 101);
            if (mPosition != 101) {
                playVideo(path);
                simpleExoPlayer.seekTo(mPosition);
            } else {
                playVideo(path);
            }
        } else {
            playVideo(path);
        }

    }

    private void recordPosition() {
        if ((vidTotalDuration - 5000) > simpleExoPlayer.getCurrentPosition()) {
            SharedPreferences preferences = getSharedPreferences(EXO_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(vidID, simpleExoPlayer.getCurrentPosition());
            editor.apply();
        } else {
            SharedPreferences preferences = getSharedPreferences(EXO_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(vidID, 0);
            editor.apply();
        }
    }

    private void pipMode(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            try {
                Rational aspectRational = new Rational(simpleExoPlayer.getVideoFormat().width,simpleExoPlayer.getVideoFormat().height);
                pipBuilder.setAspectRatio(aspectRational);
                enterPictureInPictureMode(pipBuilder.build());
            } catch (Exception e) {
                Toast.makeText(this, "PIP Error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            if(!isInPictureInPictureMode()){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                boolean usePIP = preferences.getBoolean("pip",false);
                if(usePIP){
                    pipMode();
                }
            }
        }
    }

    /*
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if(!isInPictureInPictureMode){
        }else {
        }
    }
    */

    @Override
    protected void onStop() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(PlayerActivity.this);
        boolean resumePlay = preferences.getBoolean("resume_play", false);
        if (resumePlay) {
            if (vidID != null) {
                recordPosition();
            }
        }

        try {
            simpleExoPlayer.stop(true);
            simpleExoPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean usePIP = preferences.getBoolean("pip",false);
        if(usePIP){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.finishAndRemoveTask();
            }else {
                this.finish();
            }
        }else {
            this.finish();
        }
        super.onStop();
    }
}
