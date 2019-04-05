package ca.mun.outshine;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ca.mun.outshine.service.SessionManager;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    // firebase
    private FirebaseAuth mAuth; // todo remove later
    private FirebaseAuth.AuthStateListener mAuthListener;

    // widgets
    private TextInputLayout mEmail, mPassword;
    private Button mSignIn, mBackToRegister, mResetPassword;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if user logged in -> main / otherwise -> continue
        if (SessionManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // set view layout
        setContentView(R.layout.activity_login);

        // widgets
        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);
        mProgressBar = findViewById(R.id.register_progressbar);
        mSignIn = findViewById(R.id.login_button_sign_in);
        mBackToRegister = findViewById(R.id.login_button_back_to_register);
        mResetPassword = findViewById(R.id.login_button_reset_password);

        // firebase Auth
        // todo remove later
        mAuth = FirebaseAuth.getInstance();

        setupFirebaseAuth();

        if(servicesOK()){
            mSignIn.setOnClickListener(this);
        }

        mBackToRegister.setOnClickListener(this);
        mResetPassword.setOnClickListener(this);

        hideSoftKeyboard();
    }

    // check user login status on the start
    @Override
    public void onStart() {
        super.onStart();
        // if logged in, go to Main
        // todo: we can replace this whole thing with auth firebase
        if (SessionManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        // register firebase listener
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    public void onClick(View v) {

        int i = v.getId();
        if (i == R.id.login_button_sign_in) {
            String emailInput = mEmail.getEditText().getText().toString().trim();
            String passwordInput = mPassword.getEditText().getText().toString().trim();
            Log.d(TAG, "onClick: attempting to authenticate.");
            signIn(emailInput, passwordInput);
        } else if (i == R.id.login_button_reset_password) {
            String emailInput = mEmail.getEditText().getText().toString().trim();
            resetPassword(emailInput);
        } else if (i == R.id.login_button_back_to_register) {
            goToRegister();
        }
    }

    // firebase setup
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(LoginActivity.this, "Signed in", Toast.LENGTH_SHORT).show();

                    // handle login here
                    // by going to main activity and terminating the current one
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    // user is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    public boolean servicesOK(){
        Log.d(TAG, "servicesOK: Checking Google Services.");

        int isAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this);

        if(isAvailable == ConnectionResult.SUCCESS){
            // everything is ok and the user can make mapping requests
            Log.d(TAG, "servicesOK: Play Services is OK");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable)){
            // an error occurred, but it's resolvable
            Log.d(TAG, "servicesOK: an error occured, but it's resolvable.");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(LoginActivity.this, isAvailable, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Toast.makeText(this, "Can't connect to services", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateInputs()) {
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        // check credentials
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            SessionManager.getInstance(getApplicationContext()).createLoginSession(user.getUid(), user.getEmail());
                            goToMain();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this,
                                    "Authentication failed" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void resetPassword(String email) {
        Log.d(TAG, "resetPassword:" + email);
        if (!validateEmail()) {
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        // reset password
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Log.d(TAG, "passwordReset:success");
                    Toast.makeText(LoginActivity.this,
                            "Password reset instructions was sent to your email", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "passwordReset:failure", task.getException());
                    Toast.makeText(LoginActivity.this,
                            "Password reset failed" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInputs() {
        return (validateEmail() && validatePassword());
    }

    private boolean validateEmail() {
        String emailInput = mEmail.getEditText().getText().toString().trim();
        if (emailInput.isEmpty()) {
            mEmail.setError("Required.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            mEmail.setError("Invalid email address.");
            return false;
        } else {
            mEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = mPassword.getEditText().getText().toString().trim();
        if (passwordInput.isEmpty()) {
            mPassword.setError("Required.");
            return false;
        } else {
            mPassword.setError(null);
            return true;
        }
    }

    public void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void goToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
