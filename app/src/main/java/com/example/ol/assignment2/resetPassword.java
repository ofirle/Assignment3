package com.example.ol.assignment2;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class resetPassword extends AppCompatActivity {

    private EditText etMail;
    private TextView txtTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        Typeface myFont;
        etMail = findViewById(R.id.etEmail);
        txtTitle = findViewById(R.id.tvTitle);
        myFont = Typeface.createFromAsset(this.getAssets(), "fonts/impact.ttf");
        txtTitle.setTypeface(myFont);
    }

    public void onClickResetPassword(View v) {
        if (valEmail(etMail.getText().toString())) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(etMail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Email for reset password was sent to you.", Toast.LENGTH_LONG).show();
                                ToNextActivity();
                            } else {
                                Toast.makeText(getApplicationContext(), "Email is not exist.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {

            if (etMail.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Email filled is empty.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Email is not valid.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static boolean valEmail(String i_Input) {
        String emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern emailPat = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = emailPat.matcher(i_Input);
        return matcher.find();
    }

    private void ToNextActivity() {
        Timer timer;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(resetPassword.this, SignIn.class);
                startActivity(intent);
                finish();
            }
        }, 1500);
    }
}
