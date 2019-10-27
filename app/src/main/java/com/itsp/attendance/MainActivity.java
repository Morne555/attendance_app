package com.itsp.attendance;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hadiidbouk.appauthwebview.AppAuthWebView;
import com.hadiidbouk.appauthwebview.AppAuthWebViewData;
import com.hadiidbouk.appauthwebview.IAppAuthWebViewListener;
import com.itsp.attendance.barcodereader.BarcodeCaptureActivity;
import com.itsp.attendance.ui.Data;
import com.itsp.attendance.ui.FragmentViewModel;
import com.itsp.attendance.ui.home.Subject;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import net.openid.appauth.AuthState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements IAppAuthWebViewListener
{
    static String TAG = MainActivity.class.getSimpleName();
    private static final int RC_BARCODE_CAPTURE = 9001;
    private AppAuthWebViewData authWebData;
    AppAuthWebView appAuthWebView;
    private FrameLayout mErrorLayout;
    private FrameLayout mLoadingLayout;
    private WebView mWebView;
    private FloatingActionButton qrReaderButton;
    Barcode barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Config.url = Utility.loadRawResourceKey(this, R.raw.config, "url");

        FragmentViewModel.data = new MutableLiveData<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // TODO(Morne): Set document path as user id, maybe user id should be student number?
        // NOTE(Morne): Listen for firebase data changes, if they change, update ViewModel.
        final DocumentReference docRef = db.collection("users").document("gMVLly7mnWl7XRkgcZ7Z");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>()
        {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e)
            {
                if(e != null)
                {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if(snapshot != null && snapshot.exists())
                {

                    // NOTE(Morne): Get firebase data
                    Map<String, Object> firebaseData = snapshot.getData();
                    Log.d(TAG, "Current data: " + firebaseData);
                    Data data = new Data();

                    // NOTE(Morne): Retrieve dashboard screen data
                    data.studentNumber = (String) firebaseData.get("studentNumber");
                    data.studentName = (String) firebaseData.get("studentName");
                    data.attendedTotal = (int) (long) firebaseData.get("attendedTotal");
                    data.classTotal = (int) (long) firebaseData.get("classTotal");

                    // NOTE(Morne): Retrieve home screen data
                    try
                    {
                        JSONArray subjectsJSON = new JSONArray(firebaseData.get("subjectList").toString());
                        List<Subject> subjectList = new ArrayList<Subject>();

                        for(int subjectIndex = 0;
                            subjectIndex < subjectsJSON.length();
                            subjectIndex++)
                        {
                            JSONObject subjectJSON = subjectsJSON.getJSONObject(subjectIndex);

                            Subject subject = new Subject();
                            subject.subjectName = subjectJSON.getString("subjectName");
                            subject.attendedTotal = subjectJSON.getInt("attendedTotal");
                            subject.classTotal = subjectJSON.getInt("classTotal");

                            subjectList.add(subject);
                        }
                        data.subjectList = subjectList;

                    }catch(JSONException ex)
                    {
                        ex.printStackTrace();
                    }

                    FragmentViewModel.updateData(data);
                }
                else
                {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        if(getIntent().getExtras() != null)
        {
            for(String key : getIntent().getExtras().keySet())
            {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task)
                    {
                        if(!task.isSuccessful())
                        {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // TODO(Morne): Send the instance token to the server to use for notifications
                        String token = task.getResult().getToken();
                        Log.d(TAG, "Token: " + token);
                    }
                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_login);

        //NOTE(Morne): Auth setup
        authWebData = new AppAuthWebViewData();
        authWebData.setClientId("ITSP300_ANDROID_APP");
        authWebData.setClientSecret("");
        authWebData.setAuthorizationEndpointUri(Config.url + "/connect/authorize");
        authWebData.setDiscoveryUri(Config.url + "/.well-known/openid-configuration");
        authWebData.setRedirectLoginUri("itsp300-attendance://oauth2callback");
        authWebData.setRedirectLogoutUri("");
        authWebData.setScope("openid backend.server");
        authWebData.setTokenEndpointUri(Config.url + "/connect/token");
        authWebData.setRegistrationEndpointUri("");
        authWebData.setResponseType("code");
        authWebData.setGenerateCodeVerifier(true);

        mErrorLayout = findViewById(R.id.ErrorLayout);
        mLoadingLayout = findViewById(R.id.LoadingLayout);
        mWebView = findViewById(R.id.WebView);

        appAuthWebView = new AppAuthWebView
                .Builder()
                .webView(mWebView)
                .authData(authWebData)
                .listener(this)
                .build();

        // TODO(Morne): Wrap to ensure view is set correctly
        appAuthWebView.performLoginRequest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_logout:
                appAuthWebView.performLogoutRequest();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Barcode result
        if (requestCode == RC_BARCODE_CAPTURE)
        {
            if (resultCode == CommonStatusCodes.SUCCESS)
            {
                if (data != null)
                {
                    barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    if (barcode != null)
                    {
                        Toast.makeText(this, barcode.displayValue, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Barcode read: " + barcode.displayValue);
/*
                        Map<String, String> postParam = new HashMap<String, String>();
                        postParam.put("qrCode", barcode.displayValue);

                        String api_path = "/api/secure/scan_code";
                        JsonObjectRequest qrObjectRequest = new JsonObjectRequest(Request.Method.POST,
                                Config.url + api_path, new JSONObject(postParam),
                                new Response.Listener<JSONObject>()
                                {
                                    @Override
                                    public void onResponse(JSONObject response)
                                    {
                                        // TODO(Morne): Response should not echo the value sent
                                        Log.d(TAG, "onResponse: " + response.toString());
                                    }
                                }, new Response.ErrorListener()
                        {

                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                NetworkResponse networkResponse = error.networkResponse;
                                if (networkResponse != null)
                                {
                                    String data = new String(networkResponse.data);
                                    Log.e(TAG, "onErrorResponse:\nbody:\n" + data);

                                    if (networkResponse.statusCode == 100 || networkResponse.statusCode == 401)
                                    {
                                        login();
                                    }
                                    else
                                    {
                                        Toast.makeText(MainActivity.this, "Network error!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this, "Network error!\nNo response received!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError
                            {
                                HashMap<String, String> headers = new HashMap<String, String>();
                                headers.put("Authorization", "Bearer " + Config.accessToken);
                                headers.put("Content-Type", "application/json; charset=utf-8");
                                return headers;
                            }
                        };

                        VolleySingleton.getInstance(this).addToRequestQueue(qrObjectRequest);
*/
                    }
                    else
                    {
                        Log.d(TAG, "Barcode was null.");
                    }
                }
                else
                {
                    Log.d(TAG, "No barcode captured, intent data is null.");
                }
            }
            else
            {
                Log.d(TAG, "onActivityResult: " + CommonStatusCodes.getStatusCodeString(resultCode));
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onUserAuthorize(AuthState authState)
    {
        Log.d(TAG, "onUserAuthorize: " + authState.getAccessToken());

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        qrReaderButton = findViewById(R.id.main_floating_scan);
        qrReaderButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, BarcodeCaptureActivity.class);

                // TODO(Morne): Decide whether the auto focus and flash should be used for QR scanning
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
            }
        });
    }

    @Override
    public void showConnectionErrorLayout()
    {
        mErrorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideConnectionErrorLayout()
    {
        mErrorLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showLoadingLayout()
    {
        mLoadingLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingLayout()
    {
        mLoadingLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLogoutFinish()
    {
        Log.d(TAG, "onLogoutFinished: Logged out successfully");
        appAuthWebView.performLoginRequest();
    }
}
