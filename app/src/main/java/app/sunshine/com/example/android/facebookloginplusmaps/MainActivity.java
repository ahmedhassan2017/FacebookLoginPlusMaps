package app.sunshine.com.example.android.facebookloginplusmaps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final String EMAIL = "email";
    private static final String PROFILE = "public_profile";
    LoginButton loginButton;
    Button goMap;
    CallbackManager callbackManager;


    // map
    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // checking for gps enabled
        Boolean gps_enabled = check_if_Location_enabled(getApplicationContext());
        if (!gps_enabled)
            showGPSSettingAlert(MainActivity.this);




        loginButton = findViewById(R.id.login_button);
        goMap=findViewById(R.id.goto_map);


        if (!isNetworkConnected()) {
            showWiFiSettingAlert(MainActivity.this);

        }

        goMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent);
            }
        });

        // permission to read email and profile information
        loginButton.setReadPermissions(EMAIL, PROFILE);
//        loginButton.setReadPermissions(Arrays.asList(EMAIL));

// checking if user is logged in or not
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        final boolean isLoggedIn = accessToken != null && !accessToken.isExpired();


        callbackManager = CallbackManager.Factory.create();




        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Toast.makeText(MainActivity.this, "successfully logged in", Toast.LENGTH_SHORT).show();
                // to get user id
                AccessToken accessToken = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {


                            @Override
                            public void onCompleted(JSONObject object,
                                                    GraphResponse response) {

                                displayUserInfo(object);

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "email,name,first_name,last_name,picture");

                request.setParameters(parameters);
                request.executeAsync();
//                if (isServicesOK()) {
//
//                }
            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(MainActivity.this, "login failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(MainActivity.this, "login error", Toast.LENGTH_SHORT).show();
                Log.e("ahmed", "onError: " + exception.toString());

            }
        });


//
//        gotoMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i =new Intent(MainActivity.this,MapActivity.class);
//                startActivity(i);
//            }
//        });


    }

    private boolean check_if_Location_enabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }


        if (!gps_enabled) {
            return false;
        }
        return true;
    }


    void displayUserInfo(JSONObject object) {


        String firstName = null;
        String lastName = null;
        String picPath = null;
        String id = null;
        try {
            firstName = object.getString("first_name");
            lastName = object.getString("last_name");
            id = object.getString("id");
            picPath = "http://graph.facebook.com/" + id + "/picture";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (isServicesOK()) {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("profilePic", picPath);
            startActivity(intent);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    public void showGPSSettingAlert(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS setting!");
        alertDialog.setMessage("GPS is not enabled, you need to enable it please, go to settings menu? ");
        alertDialog.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();

    }

    public void showWiFiSettingAlert(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Wifi setting!");
        alertDialog.setMessage("Wifi is not enabled, you need to enable it please, go to settings menu? ");
        alertDialog.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent myIntent = new Intent( Settings.ACTION_WIFI_SETTINGS);
                context.startActivity(myIntent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();

    }


}
