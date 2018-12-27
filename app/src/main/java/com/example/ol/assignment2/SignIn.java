package com.example.ol.assignment2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ol.assignment2.model.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignIn extends AppCompatActivity {
    private TextView txtSignIn, txtSignUp, txtContinueGuest;
    private ImageView ivGoogleIcon;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private SignInButton btnGoogleLogin;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private LoginButton btnFacebookLogin;
    private CallbackManager callbackManager;
    private boolean allow_anonymous_user = true;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(SignIn.this, BookLibrary.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        Typeface myFont;
        ivGoogleIcon = (ImageView) findViewById(R.id.ivGoogleIcon);
        txtSignIn = (TextView) findViewById(R.id.txtLogin);
        txtSignUp = (TextView) findViewById(R.id.txtSignUp);
        txtContinueGuest = findViewById(R.id.txtContinueGuest);
        myFont = Typeface.createFromAsset(this.getAssets(), "fonts/impact.ttf");
        txtSignIn.setTypeface(myFont);
        txtSignUp.setTypeface(myFont);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.etGoogleButton);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = firebaseAuth.getCurrentUser();

        if (allow_anonymous_user == true) {
            txtContinueGuest.setVisibility(View.VISIBLE);
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // if click on the regular button login.
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validInput(etEmail.getText().toString(), etPassword.getText().toString())) {
                    final ProgressDialog progressDialog = ProgressDialog.show(SignIn.this, "Please wait...", "Proccessing...", true);
                    (firebaseAuth.signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()))
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();

                                    if (task.isSuccessful()) {
                                        ToastToScreen("User logged successfully!");
                                        Intent intent = new Intent(SignIn.this, BookLibrary.class);
                                        startActivity(intent);
                                    } else {
                                        Log.e("ERROR", task.getException().toString());
                                        ToastToScreen("User name or password is not correct.");
                                    }
                                }
                            });
                }
            }
        });

        // If Click on google login button.
        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
            }
        });

        // If click in sign up button
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if is google login.
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {

                // ...
            }
        } else { // if is facebook login
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            boolean isNewUser = false;
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (isNewUser)
                            {
                                transferDetailsToDatabase();
                            }
                            ToastToScreen("User logged successfully with Google!");
                            Intent intent = new Intent(SignIn.this, BookLibrary.class);
                            startActivity(intent);
                        } else {
                            ToastToScreen("Failed login with google account.");
                        }
                    }
                });
    }

    public void OnClickFacebookLoginIcon(View v) {
        com.facebook.login.widget.LoginButton btn = findViewById(R.id.etFacebookButton);
        btn.performClick();
    }

    public void OnClickFacebookLogin(View v) {
        callbackManager = CallbackManager.Factory.create();
        btnFacebookLogin = findViewById(R.id.etFacebookButton);
        btnFacebookLogin.setReadPermissions("email", "public_profile");
        btnFacebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // transferDetailsToDatabase();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
    }

    public void onClickResetPassword(View v) {
        Intent intent = new Intent(SignIn.this, resetPassword.class);
        startActivity(intent);
    }

    public void OnClickGoogleLogin(View v) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 101);
    }


    private void handleFacebookAccessToken(AccessToken token) {

        Log.d(null, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            boolean isNewUser = false;
                            ToastToScreen("User logged successfully with Facebook!");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (isNewUser)
                            {
                                transferDetailsToDatabase();
                            }
                            Intent intent = new Intent(SignIn.this, BookLibrary.class);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(null, "signInWithCredential:failure", task.getException());
                            ToastToScreen("Authentication failed.");

                        }
                    }
                });
    }

    private static boolean valEmail(String i_Input) {
        String emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern emailPat = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = emailPat.matcher(i_Input);
        return matcher.find();
    }

    private boolean validInput(String email, String password) {
        boolean bool = true;
        if (!email.isEmpty() && !password.isEmpty()) {
            if (!valEmail(email)) {
                ToastToScreen("Email is not valid");
                bool = false;
            }
        } else {
            bool = false;
            ToastToScreen("Fields are missing");
        }
        return bool;
    }

    private void ToastToScreen(String msg) {
        Toast.makeText(SignIn.this, msg, Toast.LENGTH_LONG).show();
    }


    public void OnClickGuestUser(View v) {
        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            ToastToScreen("User logged successfully as guest!");
                            Intent intent = new Intent(SignIn.this, BookLibrary.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignIn.this, "Authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


        public void transferDetailsToDatabase()
        {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.child(firebaseAuth.getCurrentUser().getUid()).setValue(new User(firebaseAuth.getCurrentUser().getEmail(),0,null));
    }

    }

