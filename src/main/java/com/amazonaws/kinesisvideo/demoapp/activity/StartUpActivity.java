package com.amazonaws.kinesisvideo.demoapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.kinesisvideo.demoapp.util.ActivityUtils;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class StartUpActivity extends AppCompatActivity {
    private static final String TAG = StartUpActivity.class.getSimpleName();

    public static final String KEY_DLINK_CHANNEL_NAME = "channelName";
    public static final String KEY_DLINK_ISMASTER = "isMaster";
    public static final String KEY_DLINK_REGION = "region";
    public static final String KEY_DLINK_CLIENTID = "clientId";

    Map<String, String> getQueryKeyValueMap(Uri uri){
        HashMap<String, String> keyValueMap = new HashMap<>();
        String key;
        String value;

        Set<String> keyNamesList = uri.getQueryParameterNames();

        for (String s : keyNamesList) {
            key = s;
            value = uri.getQueryParameter(key);
            keyValueMap.put(key, value);
        }
        return keyValueMap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AWSMobileClient auth = AWSMobileClient.getInstance();
        final AppCompatActivity thisActivity = this;

        // Intel instrumentation for testing
        Intent intent = getIntent();
        String action = intent.getAction();
        Bundle bundle = null;
        if (!action.equalsIgnoreCase("android.intent.action.MAIN")) {
            Uri data = intent.getData();
            Map<String, String> params = getQueryKeyValueMap(data);
            String region = null;
            String channel = null;
            String clientid = null;
            Boolean master = null;

            if (params.containsKey("region")) {
                region = params.get("region");
            } else {
                Log.e(TAG, "[DEEPLINK] region not specified. Deep liking will not work");
            }

            if (params.containsKey("channel")) {
                channel = params.get("channel");
            } else {
                Log.e(TAG, "[DEEPLINK] channel not specified. Deep liking will not work");
            }

            if (params.containsKey("role")) {
                String role = Objects.requireNonNull(params.get("role"));
                if (role.equalsIgnoreCase("master") || role.equalsIgnoreCase("viewer")) {
                    master = Objects.requireNonNull(params.get("role")).equalsIgnoreCase("master");
                } else {
                    Log.e(TAG, "[DEEPLINK] role can either be 'master' or 'viewer'. Deep liking will not work");
                }
            } else {
                Log.e(TAG, "[DEEPLINK] role not specified. Deep liking will not work");
            }

            if (params.containsKey("clientid")) {
                clientid = params.get("clientid");
            }

            if (channel != null && region != null && master != null) {
                bundle = new Bundle();
                bundle.putBoolean(KEY_DLINK_ISMASTER, master);
                bundle.putString(KEY_DLINK_CHANNEL_NAME, channel);
                bundle.putString(KEY_DLINK_REGION, region);
                if (clientid != null) {
                    bundle.putString(KEY_DLINK_CLIENTID, clientid);
                }
            }
        }

        final Bundle finalBundle = bundle;
        AsyncTask.execute(() -> {
//                try {
//                    auth.signIn("youssef@lakecarmel.com", "careAI@123", null);
//                    auth.refresh();
//                } catch (Exception exception) {
//                    throw new RuntimeException(e);
//                }

            if (auth.isSignedIn()) {
                ActivityUtils.startActivity(thisActivity, SimpleNavActivity.class, finalBundle);
            } else {
                auth.showSignIn(thisActivity,
                        SignInUIOptions.builder()
                                .logo(R.mipmap.kinesisvideo_logo)
                                .backgroundColor(Color.WHITE)
                                .nextActivity(SimpleNavActivity.class)
                                .build(),
                        new Callback<UserStateDetails>() {
                            @Override
                            public void onResult(UserStateDetails result) {
                                Log.d(TAG, "onResult: User signed-in " + result.getUserState());
                            }

                            @Override
                            public void onError(final Exception e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e(TAG, "onError: User sign-in error", e);
                                        Toast.makeText(StartUpActivity.this, "User sign-in error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
            }
        });
    }
}
