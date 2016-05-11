package animeftw.tv.anilite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class Start extends AppCompatActivity implements GestureDetector.OnGestureListener {
    boolean loginOpen = false;
    GestureDetector detector;
    private static String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Gesture detector
        detector = new GestureDetector(this,this);

        // Login button action
        Button login = (Button) findViewById(R.id.loginButton);
        login.setOnClickListener(v -> tryLogin());

        // Check for token
        SharedPreferences preferences = getSharedPreferences("Anilite", Context.MODE_PRIVATE);
        token = preferences.getString("token",null);
}

    void swipeLogin() {
        View startScreen = findViewById(R.id.startScreen);

        // Fade out
        Animation fadeout = AnimationUtils.loadAnimation(this,R.anim.fadeout);
        fadeout.setFillAfter(true);
        startScreen.startAnimation(fadeout);

        // If token exists, try it first
        if(token != null) {
            tryLogin();
        }

        // Otherwise, show the login screen
        else {
            showLogin();
        }
    }

    void showLogin() {
        // Slide up
        View loginScreen = findViewById(R.id.login);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slideup);
        loginScreen.setVisibility(View.VISIBLE);
        loginScreen.startAnimation(slideUp);
    }

    void tryLogin() {
        SharedPreferences preferences = getSharedPreferences("Anilite", Context.MODE_PRIVATE);
        EditText username = (EditText) findViewById(R.id.username);
        EditText password = (EditText) findViewById(R.id.password);

        // Get token (if available)
        String token = preferences.getString("token",null);

        // Dismiss keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(username.getWindowToken(), 0);

        // Build callback
        Api api = new Api();
        Api.ApiCallback callback = new Api.ApiCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    View content = findViewById(android.R.id.content);
                    Snackbar.make(content, "Logged in successfully", Snackbar.LENGTH_SHORT).show();

                    String new_token = result.getString("message");

                    if(token == null) {
                        // Save token in preferences
                        SharedPreferences preferences = getSharedPreferences("Anilite", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("token", new_token);
                        editor.commit();
                    }

                    Intent intent = new Intent(getApplicationContext(), Series.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                } catch (JSONException e) {
                    onError(e);
                }
            }

            @Override
            public void onFailure(JSONObject result) {
                View content = findViewById(android.R.id.content);
                if(token != null) {
                    Start.token = null;
                    SharedPreferences preferences = getSharedPreferences("Anilite", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("token");
                    editor.commit();

                    Snackbar.make(content, "Oops! We couldn't log you in!\nYour authentication has expired, please login again.", Snackbar.LENGTH_LONG).show();
                    showLogin();
                }
                else {
                    Snackbar.make(content, "Oops! We couldn't log you in!\nPlease check your username and password.", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Exception e) {
                View content = findViewById(android.R.id.content);
                Snackbar.make(content, "Oops, our bad: " + e.getMessage(), Snackbar.LENGTH_INDEFINITE).show();
            }
        };

        if( token != null) {
            api.login(token, callback);
        }
        else if (username.getText().toString().length() > 0 && password.getText().toString().length() > 0) {
            api.login(username.getText().toString(), password.getText().toString(), callback);
        }
        else {
            if (username.getText().toString().length() == 0) {
                username.setError("You must enter a username");
            }
            if (password.getText().toString().length() == 0) {
                password.setError("You must enter a password");
            }
        }
    }

    // Touch method
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    // Gesture methods
    @Override public void onShowPress(MotionEvent e) {}
    @Override public void onLongPress(MotionEvent e) {}
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(e2.getY() < e1.getY()) {
            if(!loginOpen) {
                loginOpen = true;
                swipeLogin();
            }
        }
        return true;
    }
}
