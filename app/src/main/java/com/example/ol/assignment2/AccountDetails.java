package com.example.ol.assignment2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import static java.security.AccessController.getContext;

public class AccountDetails extends AppCompatActivity {

    private TextView txtEmailTitle, txtEmail, txtNameTitle, txtName;
    private ImageView imageProfilePicture;
    private Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);
        txtNameTitle = findViewById(R.id.txtNameTitle);
        txtEmail = findViewById(R.id.txtEmail);
        txtEmailTitle = findViewById(R.id.txtEmailTitle);
        btnSignOut = findViewById(R.id.buttonSignOut);
        txtName = findViewById(R.id.txtName);
        imageProfilePicture = findViewById(R.id.imageProfilePicture);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (!user.isAnonymous()) {
            txtEmail.setText(user.getEmail());
            if (user.getDisplayName() != null) {
                txtNameTitle.setVisibility(View.VISIBLE);
                txtName.setText(user.getDisplayName());
            }

            if (user.getPhotoUrl() != null) {
                Picasso.with(AccountDetails.this).load(user.getPhotoUrl()).into(imageProfilePicture);
            }
        } else {
            txtEmailTitle.setText("Guest user, No information available.");
            btnSignOut.setText("Canceling be a guest user");
        }
    }

    public void onClickSignOut(View v) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(AccountDetails.this, SignIn.class);
        startActivity(intent);

    }

    public void GoToLibrary(View v)
    {
        Intent intent = new Intent(AccountDetails.this, BookLibrary.class);
        startActivity(intent);
    }
}
