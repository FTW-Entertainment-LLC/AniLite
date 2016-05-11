package animeftw.tv.anilite;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adam Treadway on 4/12/2016.
 */
public class Video extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video);

        // Get bundle data
        Bundle bundle = getIntent().getExtras();
        String video = bundle.getString("url");

        // Loading animation
        loading();

        VideoView videoView = (VideoView) findViewById(R.id.video);
        Uri uri = Uri.parse(video);
        videoView.setVideoURI(uri);

        // Set Media controller
        MediaController controller = new MediaController(this);
        videoView.setMediaController(controller);

        // Set user agent
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("User-Agent", "my user agent value");
        //videoView.setDataSource(context, uri, headerMap);

        videoView.setOnPreparedListener(mp -> {
            findViewById(R.id.loader).setVisibility(View.GONE);
            mp.start();
        });
        videoView.setOnCompletionListener(mp -> finish());
    }

    private void loading() {
        // Loading dots animation
        Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.loader);
        Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.loader);
        Animation anim3 = AnimationUtils.loadAnimation(this, R.anim.loader);

        anim2.setStartOffset(250);
        anim3.setStartOffset(750);

        findViewById(R.id.load_dot1).startAnimation(anim1);
        findViewById(R.id.load_dot2).startAnimation(anim2);
        findViewById(R.id.load_dot3).startAnimation(anim3);
    }
}
