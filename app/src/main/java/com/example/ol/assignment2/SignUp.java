package com.example.ol.assignment2;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ol.assignment2.model.Book;
import com.example.ol.assignment2.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    private TextView txtLogin, txtSignUp;
    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword, etRePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Typeface myFont;
        txtLogin = (TextView) findViewById(R.id.txtLogin);
        txtSignUp = (TextView) findViewById(R.id.txtSignUp);
        myFont = Typeface.createFromAsset(this.getAssets(), "fonts/impact.ttf");
        txtLogin.setTypeface(myFont);
        txtSignUp.setTypeface(myFont);
        mAuth = FirebaseAuth.getInstance();

        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, SignIn.class);
                startActivity(intent);
            }
        });
    }

    public void onClickRegister(View view) {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRePassword = findViewById(R.id.etRePassword);
        boolean validInput = checkValidInput();

        if (validInput == true) {
            mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                                User theUser = new User(mAuth.getCurrentUser().getEmail(), 0,null);
                                userRef.child(mAuth.getCurrentUser().getUid()).setValue(theUser);
                                Toast.makeText(SignUp.this, "Register is successfully!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SignUp.this, BookLibrary.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SignUp.this, task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private static boolean valEmail(String i_Input) {
        String emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern emailPat = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = emailPat.matcher(i_Input);
        return matcher.find();
    }

    private boolean checkValidInput() {

        String emailInput = etEmail.getText().toString();
        String passwordInput = etPassword.getText().toString();
        String rePasswordInput = etRePassword.getText().toString();
        String noteError = "";
        boolean checkFields = true;


        if (!valEmail(emailInput)) {
            checkFields = false;
            noteError += "- Email address is'nt valid.\n";
        }

        if (passwordInput.length() < 6) {
            checkFields = false;
            noteError += "- Password is less than 6 digits.\n";
        }

        if (passwordInput.equals(rePasswordInput) == false) {
            checkFields = false;
            noteError += "- Password and Re-Password are not equals.\n";
        }

        noteError += "Please try again.";

        if (checkFields == false)
            Toast.makeText(getApplicationContext(), noteError, Toast.LENGTH_LONG).show();

        return checkFields;
    }
}
