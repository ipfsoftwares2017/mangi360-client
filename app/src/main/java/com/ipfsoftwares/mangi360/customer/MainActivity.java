package com.ipfsoftwares.mangi360.customer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.support.annotation.NonNull;

import android.support.v4.app.DialogFragment;

import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.LinearLayoutManager;

import com.google.android.gms.appinvite.AppInviteInvitation;

import com.google.android.gms.auth.api.Auth;

import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.ConnectionResult;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;

import android.widget.*;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private Button mMakePaymentButton;
    private LinearLayoutManager mLinearLayoutManager;

    // Firebase instance variables
	private FirebaseAuth mFirebaseAuth;
	private FirebaseUser mFirebaseUser;
	private DatabaseReference mFirebaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
        	startActivity(new Intent(this, SignInActivity.class));
        	finish();
        	return;
        } else {
        	mUsername = mFirebaseUser.getDisplayName();

        	if(mFirebaseUser.getPhotoUrl() != null) {
        		mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        	}
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mMakePaymentButton = (Button) findViewById(R.id.makePaymentButton);
        mMakePaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				new IntentIntegrator(MainActivity.this).initiateScan(); // `this` is the current Activity
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
			case R.id.invite_menu:
				sendInvitation();
				return true;
			case R.id.sign_out_menu:
				mFirebaseAuth.signOut();
				Auth.GoogleSignInApi.signOut(mGoogleApiClient);
				mUsername = ANONYMOUS;
				startActivity(new Intent(this, SignInActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);

		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if(result != null) {
			if(result.getContents() == null) {
				Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
			} else {
				JSONTokener tokener = new JSONTokener(result.getContents());
                JSONObject root = null;
                try {
                    root = new JSONObject(tokener);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String grandTotal = null;
                try {
                    grandTotal = root.getString("total");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject merchantObject = null;
                try {
                    merchantObject = new JSONObject(root.getString("merchant"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String merchantName = null;
                try {
                    merchantName = merchantObject.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String merchantNumber = null;
                try {
                    merchantNumber = merchantObject.getString("phoneNumber");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

				Toast.makeText(this, "Scanned: " +
						merchantName + " " +
						merchantNumber + " " +
						grandTotal,
						Toast.LENGTH_LONG).show();

				confirmPayment();
            }
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
    }

    private void sendInvitation() {
		Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
			.setMessage(getString(R.string.invitation_message))
			.setCallToActionText(getString(R.string.invitation_cta))
			.build();

		startActivityForResult(intent, REQUEST_INVITE);
    }

	public void confirmPayment() {
		DialogFragment newFragment = new ConfirmPaymentDialogFragment();
		newFragment.show(getSupportFragmentManager(), "Confirm Payment");
	}
}
