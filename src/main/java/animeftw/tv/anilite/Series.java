package animeftw.tv.anilite;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

public class Series extends AppCompatActivity {
    Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series);

        // Set API with token
        SharedPreferences preferences = getSharedPreferences("Anilite", Context.MODE_PRIVATE);
        String token = preferences.getString("token", null);
        api = new Api(token);

        // Do loading dots animation
        loading();

        // Handle intent
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
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

    private void handleIntent(Intent intent) {
        ListView listBox = (ListView) findViewById(R.id.listbox);
        Api.ApiCallback callback = new Api.ApiCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    JSONArray results = result.getJSONArray("results");
                    runOnUiThread(() -> {
                        findViewById(R.id.loader).setVisibility(View.GONE);
                        listBox.setVisibility(View.VISIBLE);
                        listBox.setAdapter(new SeriesAdapter(results));
                        listBox.setOnItemClickListener((parent, view, position, id) -> {
                            long series_id = listBox.getItemIdAtPosition(position);
                            Intent intent1 = new Intent(getApplicationContext(), Episodes.class);
                            intent1.putExtra("id", series_id);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent1);
                        });
                    });
                }
                catch (JSONException e) {
                    onError(e);
                }
            }

            @Override
            public void onFailure(JSONObject result) {
                Snackbar.make(listBox, "No results found!", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(listBox, "Oops, our bad: " + e.getMessage(), Snackbar.LENGTH_INDEFINITE).show();
            }
        };

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            api.getSearch(query, 0, callback);
        }
        else {
            api.getSeries(0,callback);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.series_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        MenuItem searchItem = menu.findItem(R.id.search);
        MenuItemCompat.setOnActionExpandListener(searchItem,new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                findViewById(R.id.listbox).setVisibility(View.GONE);
                findViewById(R.id.loader).setVisibility(View.VISIBLE);
                Intent intent = new Intent();
                intent.setAction("default");
                handleIntent(intent);
                return true;
            }
        });

        return true;
    }

    private class SeriesAdapter extends BaseAdapter implements ListAdapter {
        private JSONArray jsonArray;
        SeriesAdapter(JSONArray jsonArray) {
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
                        (LinearLayout) getLayoutInflater().inflate(R.layout.series_listing, null);
                JSONObject data = getItem(position);
                TextView name = (TextView) listing_layout.findViewById(R.id.series_name);
                name.setText(data.getString("fullSeriesName"));

                return listing_layout;
            }
            catch (JSONException e) {
                return null;
            }
        }
    }
}
