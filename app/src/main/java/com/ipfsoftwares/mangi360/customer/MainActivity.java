package com.ipfsoftwares.mangi360.customer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
        CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }

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

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;

    // Firebase instance variables
	private FirebaseAuth mFirebaseAuth;
	private FirebaseUser mFirebaseUser;
	private DatabaseReference mFirebaseReference;
	private FirebaseRecyclerAdapter<ProductDAO, MessageViewHolder> mFirebaseAdapter;

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

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        // Initialize firebase database instance variables
        // and add all existing messages.
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ProductDAO, MessageViewHolder>(
        		ProductDAO.class,
        		R.layout.item_message,
        		MessageViewHolder.class,
        		mFirebaseReference.child(MESSAGES_CHILD)) {

			@Override
			public void populateViewHolder(MessageViewHolder viewHolder, ProductDAO productDAO, int position) {
				mProgressBar.setVisibility(ProgressBar.INVISIBLE);
				viewHolder.messageImageView.setVisibility(ImageView.GONE);

				if (productDAO.getText() != null) {
					viewHolder.messageTextView.setText(productDAO.getText());
					viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
				} else {
					// TODO: Implement fetch and display image messages
				}

				viewHolder.messengerTextView.setText(productDAO.getName());
				if (productDAO.getPhotoUrl() != null) {
					Glide.with(MainActivity.this)
						.load(productDAO.getPhotoUrl())
						.into(viewHolder.messengerImageView);
				}
			}
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
        	@Override
        	public void onItemRangeInserted(int positionStart, int itemCount) {
        		super.onItemRangeInserted(positionStart, itemCount);

				int friendlyMessageCount = mFirebaseAdapter.getItemCount();
        		int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
        		if (lastVisiblePosition == -1 ||
        				(positionStart >= (friendlyMessageCount - 1) &&
        				lastVisiblePosition == (positionStart - 1))) {
					mMessageRecyclerView.scrollToPosition(positionStart);
        		}
        	}
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	ProductDAO friendlyMessage = new ProductDAO(
            			mMessageEditText.getText().toString(),
            			mUsername,
            			mPhotoUrl,
            			null /* no image */);

            	mFirebaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessage);
            	mMessageEditText.setText("");
            }
        });

        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Select image for image message on click.
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

    	if (requestCode == RESULT_OK) {
    		String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
    		Log.d(TAG, "Invitations sent: " + ids.length);
    	}
    }

    private void causeCrash() {
    	throw new NullPointerException("Whhhopssies! Something went wrong.");
    }

    private void sendInvitation() {
		Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
			.setMessage(getString(R.string.invitation_message))
			.setCallToActionText(getString(R.string.invitation_cta))
			.build();

		startActivityForResult(intent, REQUEST_INVITE);
    }
}
