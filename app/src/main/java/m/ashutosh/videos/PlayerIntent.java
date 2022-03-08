package m.ashutosh.videos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
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

public class PlayerIntent extends AppCompatActivity {

    private PlayerView playerView;
    private SimpleExoPlayer simpleExoPlayer;
    private long quitTime;
    private Toast quitToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_intent);

        if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            playerView = findViewById(R.id.exoplayer_intent);
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            Intent playerIntent = getIntent();
            String action = playerIntent.getAction();
            String fileType = playerIntent.getType();

            if (action != null) {
                if (fileType.startsWith("video/") || fileType.startsWith("audio/")) {
                    Uri uri = playerIntent.getData();

                    if(uri != null){
                        String locale = uri.getPath();
                        if (locale.contains("root")) {
                            String path = locale.substring(5);
                            playVideo(path);
                        } else if (locale.contains("emulated")) {
                            playVideo(locale);
                        }else {
                            String videoLocale = resolvedPath(uri);
                            if (!videoLocale.equals("x")) {
                                playVideo(videoLocale);
                            } else {
                                Toast.makeText(this, "Path not found\n" + uri.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }else {
                        Toast.makeText(this, "Can't play this file", Toast.LENGTH_LONG).show();
                    }
                } else{
                    Toast.makeText(this, "Not audio/video file", Toast.LENGTH_LONG).show();
                }
            }

        }else {
            Toast.makeText(this, "Allow Permission(s)", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:" + getPackageName()));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
            PlayerIntent.this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (quitTime + 2000 > System.currentTimeMillis()) {
            quitToast.cancel();
            super.onBackPressed();
            try {
                simpleExoPlayer.stop(true);
                simpleExoPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PlayerIntent.this.finishAndRemoveTask();
            }else {
                PlayerIntent.this.finish();
            }
            return;
        } else {
            quitToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            quitToast.show();
        }
        quitTime = System.currentTimeMillis();
    }

    private String resolvedPath(Uri uri) {
        String videoPath = null;
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getBaseContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(projection[0]);
                videoPath = cursor.getString(index);
            }
            cursor.close();
        }
        if (videoPath == null) {
            videoPath = "x";
        }
        return videoPath;
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

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(PlayerIntent.this);
            boolean landscapeView = preferences.getBoolean("autoRotate",false);
            if(landscapeView){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

        }
    }

}
