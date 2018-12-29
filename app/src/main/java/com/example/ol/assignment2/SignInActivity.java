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

import com.example.ol.assignment2.model.Book;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {
    private TextView txtSignIn, txtSignUp, txtContinueGuest;
    private ImageView ivGoogleIcon;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private SignInButton btnGoogleLogin;
    private GoogleSignInClient mGoogleSignInClient;
    private LoginButton btnFacebookLogin;

    private FirebaseAuth m_FirebaseAuth;
    private DatabaseReference m_MyUserRef;
    private CallbackManager m_CallbackManager;
    private boolean m_Allow_anonymous_user = true;
    private boolean m_ComeHereFromGuestExit = false;
    private User m_User;
    private ArrayList<Book> m_ListBooks;
    private Book m_BookFromGuest;

    @Override
    public void onStart() {
        super.onStart();
        // Check if m_User is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(SignInActivity.this, BookLibraryActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        initCreate();
        checkIfComeFromGuest();
        FirebaseUser mUser = m_FirebaseAuth.getCurrentUser();

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
                afterClickOnLoginButton();
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
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if is google login.
        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {

            }
        } else { // if is facebook login
            m_CallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        m_FirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in m_User's information
                            boolean isNewUser = false;
                            FirebaseUser user = m_FirebaseAuth.getCurrentUser();
                            isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (isNewUser) {
                                transferDetailsToDatabase();
                            }
                            ToastToScreen("User logged successfully with Google!");
                            intentToApplication(user);
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
        m_CallbackManager = CallbackManager.Factory.create();
        btnFacebookLogin = findViewById(R.id.etFacebookButton);
        btnFacebookLogin.setReadPermissions("email", "public_profile");
        btnFacebookLogin.registerCallback(m_CallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
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
        Intent intent = new Intent(SignInActivity.this, resetPasswordActivity.class);
        startActivity(intent);
    }

    public void OnClickGoogleLogin(View v) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 101);
    }


    private void handleFacebookAccessToken(AccessToken token) {

        Log.d(null, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        m_FirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            boolean isNewUser = false;
                            ToastToScreen("User logged successfully with Facebook!");
                            FirebaseUser user = m_FirebaseAuth.getCurrentUser();
                            isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (isNewUser) {
                                transferDetailsToDatabase();
                            }
                            intentToApplication(user);

                        } else {
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
        Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_LONG).show();
    }


    public void OnClickGuestUser(View v) {
        m_FirebaseAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in m_User's information
                            FirebaseUser user = m_FirebaseAuth.getCurrentUser();
                            ToastToScreen("User logged successfully as guest!");
                            Intent intent = new Intent(SignInActivity.this, BookLibraryActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the m_User.
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void transferDetailsToDatabase() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.child(m_FirebaseAuth.getCurrentUser().getUid()).setValue(new User(m_FirebaseAuth.getCurrentUser().getEmail(), 0, null));
    }


    private void intentToApplicationHelper(User i_User) {
        Intent intent;
        if (m_ComeHereFromGuestExit == false) {
            intent = new Intent(SignInActivity.this, BookLibraryActivity.class);
            startActivity(intent);
        } else {
            if (getIntent().getStringExtra("activityFrom").equals("OrdersActivity")) {
                intent = new Intent(SignInActivity.this, OrdersActivity.class);
                intent.putExtra("user", i_User);
                intent.putExtra("booksList", m_ListBooks);
                startActivity(intent);
            } else if (getIntent().getStringExtra("activityFrom").equals("BookActivity")) {
                intent = new Intent(SignInActivity.this, BookActivity.class);
                intent.putExtra("user", i_User);
                intent.putExtra("booksList", m_ListBooks);
                intent.putExtra("choseBook", m_BookFromGuest);
                intent.putExtra("activityFrom", "SignInActivity");
                startActivity(intent);
            }
        }
    }

    private void intentToApplication(FirebaseUser i_User) {
        m_MyUserRef = FirebaseDatabase.getInstance().getReference("Users/" + i_User.getUid());
        m_MyUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                m_User = snapshot.getValue(User.class);
                intentToApplicationHelper(m_User);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void initCreate() {
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
        m_FirebaseAuth = FirebaseAuth.getInstance();
        m_ComeHereFromGuestExit = getIntent().getStringExtra("activityFrom") != null;
    }

    private void checkIfComeFromGuest() {
        if (m_ComeHereFromGuestExit) {
            if (getIntent().getStringExtra("activityFrom").equals("BookActivity")) {
                txtContinueGuest.setVisibility(View.GONE);
                m_BookFromGuest = (Book) getIntent().getSerializableExtra("choseBook");
                m_ListBooks = (ArrayList<Book>) getIntent().getSerializableExtra("booksList");

            } else if (getIntent().getStringExtra("activityFrom").equals("OrdersActivity")) {
                txtContinueGuest.setVisibility(View.GONE);
                m_ListBooks = (ArrayList<Book>) getIntent().getSerializableExtra("booksList");
            }
        } else if (m_Allow_anonymous_user == true) {
            txtContinueGuest.setVisibility(View.VISIBLE);
        }
    }

    private void afterClickOnLoginButton() {
        if (validInput(etEmail.getText().toString(), etPassword.getText().toString())) {
            final ProgressDialog progressDialog = ProgressDialog.show(SignInActivity.this, "Please wait...", "Proccessing...", true);
            (m_FirebaseAuth.signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()))
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();

                            if (task.isSuccessful()) {
                                ToastToScreen("User logged successfully!");
                                intentToApplication(m_FirebaseAuth.getCurrentUser());
                            } else {
                                Log.e("ERROR", task.getException().toString());
                                ToastToScreen("User name or password is not correct.");
                            }
                        }
                    });
        }
    }
}

