package animeftw.tv.anilite;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Adam Treadway on 4/6/2016.
 */
public class Api {
    private final String Api_key = "d90q-f8ht-d0g7-7amc2";
    private String token;

    Api() {}
    Api(String token) {
        this.token = token;
    }

    void getSeries(int start, ApiCallback apiCallback) {
        new CallApi("action=display-series&start=" + start + "&count=1500", token, getCallback(apiCallback));
    }

    void getEpisodes(int start, long id, ApiCallback apiCallback) {
        new CallApi("action=display-episodes&id=" + id + "&start=" + start + "&count=1500", token, getCallback(apiCallback));
    }

    void getSearch(String query, int start, ApiCallback apiCallback) {
        try {
            String q = URLEncoder.encode(query, "UTF-8");
            new CallApi("action=search&for=" + q + "&start=" + start, token, getCallback(apiCallback));
        }
        catch (UnsupportedEncodingException e) {
            // Should throw an error. Don't think I need to.
        }
    }

    void login(String token, ApiCallback apiCallback) {
        new CallApi("action=app-settings", token, getCallback(apiCallback));
    }

    void login(String username, String password, ApiCallback apiCallback) {
        new CallApi("username=" + username + "&password=" + password + "&remember=true", getCallback(apiCallback));
    }

    private InternalApiCallback getCallback(ApiCallback apiCallback) {
        return new InternalApiCallback(apiCallback) {
            @Override
            public void onCallback(JSONObject result) {
                try {
                    int status = result.getInt("status");
                    if (status == 200) {
                        apiCallback.onSuccess(result);
                    } else {
                        apiCallback.onFailure(result);
                    }
                } catch (JSONException e) {
                    apiCallback.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                apiCallback.onError(e);
            }
        };
    }


    interface ApiCallback {
        void onSuccess(JSONObject result);
        void onFailure(JSONObject result);
        void onError(Exception e);
    }

    private class CallApi extends Thread {
        String request;
        String token;
        InternalApiCallback callback;
        CallApi(String request, InternalApiCallback callback) {
            this.request = request;
            this.callback = callback;
            start();
        }
        CallApi(String request, String token, InternalApiCallback callback) {
            this.request = request;
            this.token = token;
            this.callback = callback;
            start();
        }
        @Override
        public void run() {
            try {
                // Build URL
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("https://www.animeftw.tv/api/v2/?devkey=").append(Api_key);
                if (token != null) {
                    stringBuilder.append("&token=").append(token);
                }
                stringBuilder.append("&").append(request);

                // Convert to URL object
                URL url = new URL(stringBuilder.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent","AniLite");
                connection.connect();

                // Read data from the connection
                StringBuilder connectionBuilder = new StringBuilder();
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                // Reading data to a String builder
                String line;
                while((line = bufferedReader.readLine()) != null ) {
                    connectionBuilder.append(line);
                }

                JSONObject result = new JSONObject(connectionBuilder.toString());
                callback.onCallback(result);
            }
            catch (IOException|JSONException ioe) {
                callback.onError(ioe);
            }
        }
    }

    private abstract class InternalApiCallback {
        ApiCallback apiCallback;
        InternalApiCallback(ApiCallback apiCallback) {
            this.apiCallback = apiCallback;
        }
        abstract void onCallback(JSONObject result);
        abstract void onError(Exception e);
    }
}
