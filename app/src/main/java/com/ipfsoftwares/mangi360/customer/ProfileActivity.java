package com.ipfsoftwares.mangi360.customer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        EditText displayNameEditText = (EditText) findViewById(R.id.username_edit_text);
        EditText phoneNumberEditText = (EditText) findViewById(R.id.phone_edit_text);
        ImageView avatarImageView = (ImageView) findViewById(R.id.avatar_image_view);

        Bundle extras = getIntent().getExtras();
        displayNameEditText.setText(extras.getString("displayName"));
        phoneNumberEditText.setText(extras.getString("phoneNumber"));

        Glide.with(ProfileActivity.this)
                .load(extras.getString("photoUrl"))
                .into(avatarImageView);
    }

    @Override
    public void onStart() {
        super.onStart();
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
            case R.id.sign_out_menu:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}

