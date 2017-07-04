package com.mytrendin.loginlinked;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    private ImageView btnLogin, imgProfile;
    private Button btnLogout;
    private TextView txtDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        computePakageHash();
        init();
    }

    private void init() {
        btnLogin = (ImageView) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        btnLogout = (Button) findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogout();
            }
        });

        imgProfile = (ImageView) findViewById(R.id.img_profile);

        txtDetails = (TextView) findViewById(R.id.txt_details);

        btnLogin.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.GONE);
        imgProfile.setVisibility(View.GONE);
        txtDetails.setVisibility(View.GONE);
    }

    private void computePakageHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.mytrendin.loginlinked",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }

    }

    private void handleLogin() {
        LISessionManager.getInstance(getApplicationContext()).init(this, buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {
                // Authentication was successful.  You can now do
                // other calls with the SDK.

                btnLogin.setVisibility(View.GONE);
                btnLogout.setVisibility(View.VISIBLE);
                imgProfile.setVisibility(View.VISIBLE);
                txtDetails.setVisibility(View.VISIBLE);

                fetchPersonalInfo();
            }

            @Override
            public void onAuthError(LIAuthError error) {
                Log.e("DEBAJYOTI", error.toString());
            }
        }, true);
    }

    private void handleLogout(){
        LISessionManager.getInstance(getApplicationContext()).clearSession();
        btnLogin.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.GONE);
        imgProfile.setVisibility(View.GONE);
        txtDetails.setVisibility(View.GONE);
    }

    // Build the list of member permissions our LinkedIn session requires
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE, Scope.R_EMAILADDRESS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Add this line to your existing onActivityResult() method
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
    }

    private void fetchPersonalInfo() {
        String url = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,picture-url,email-Address)?format=json";

        final APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                // Success!
                try {
                    JSONObject jsonObject = apiResponse.getResponseDataAsJson();
                    String firstName = jsonObject.getString("firstName");
                    String lastName = jsonObject.getString("lastName");
                    String pictureURL = jsonObject.getString("pictureUrl");
                    String emailAddress = jsonObject.getString("emailAddress");

                    Picasso.with(getApplicationContext()).load(pictureURL).into(imgProfile);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("First Name: " +firstName);
                    stringBuilder.append("\n\n");
                    stringBuilder.append("Last Name: " +lastName);
                    stringBuilder.append("\n\n");
                    stringBuilder.append("Email Address: " +emailAddress);

                    txtDetails.setText(stringBuilder);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onApiError(LIApiError liApiError) {
                // Error making GET request!
                Log.e("DEBAJYOTI", liApiError.getMessage());
            }
        });
    }
}