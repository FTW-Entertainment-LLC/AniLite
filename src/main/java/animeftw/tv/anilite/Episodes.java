package animeftw.tv.anilite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adam Treadway on 4/12/2016.
 */
public class Episodes extends AppCompatActivity {
    Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episodes);

        // Set API with token
        SharedPreferences preferences = getSharedPreferences("Anilite", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);
        api = new Api(token);

        // Get series ID from instance
        Bundle bundle = getIntent().getExtras();
        long id = bundle.getLong("id");

        // Loading animation
        loading();

        ListView listBox = (ListView) findViewById(R.id.listeps);
        Api.ApiCallback callback = new Api.ApiCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    JSONArray results = result.getJSONArray("results");
                    runOnUiThread(() -> {
                        findViewById(R.id.loader).setVisibility(View.GONE);
                        listBox.setAdapter(new EpisodeAdapter(results));
                        listBox.setOnItemClickListener((parent, view, position, id) -> {
                            JSONObject episode = (JSONObject) listBox.getItemAtPosition(position);
                            Intent intent2 = new Intent(getApplicationContext(), Video.class);
                            try {
                                intent2.putExtra("url", episode.getString("video"));
                                intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(intent2);
                            }
                            catch (JSONException e) {
                                onError(e);
                            }
                        });
                    });
                }
                catch (JSONException e) {
                    onError(e);
                }
            }

            @Override
            public void onFailure(JSONObject result) {
                try {
                    String message = result.getString("message");
                    Snackbar.make(listBox, "Oops, our bad: " + message, Snackbar.LENGTH_INDEFINITE).show();
                }
                catch (JSONException e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(listBox, "Oops, our bad: " + e.getMessage(), Snackbar.LENGTH_INDEFINITE).show();
            }
        };

        api.getEpisodes(0, id, callback);
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

    private class EpisodeAdapter extends BaseAdapter implements ListAdapter {
        private JSONArray jsonArray;
        EpisodeAdapter(JSONArray jsonArray) {
            this.jsonArray = jsonArray;
        }

        @Override
        public int getCount() {
            return jsonArray.length();
        }

        @Override
        public JSONObject getItem(int position) {
            return jsonArray.optJSONObject(position);
        }

        @Override
        public long getItemId(int position) {
            JSONObject item = getItem(position);
            return item.optLong("id");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                LinearLayout listing_layout =
                        (LinearLayout) getLayoutInflater().inflate(R.layout.episode_listing, null);
                JSONObject data = getItem(position);
                TextView name = (TextView) listing_layout.findViewById(R.id.episode_name);
                name.setText(data.getString("epname"));
                TextView epnum = (TextView) listing_layout.findViewById(R.id.episode_number);
                epnum.setText("Episode " + (position + 1));

                return listing_layout;
            }
            catch (JSONException e) {
                return null;
            }
        }
    }
}
