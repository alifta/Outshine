package ca.mun.outshine;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import ca.mun.outshine.model.Challenge;
import ca.mun.outshine.model.User;
import ca.mun.outshine.service.Constants;
import ca.mun.outshine.service.HealthService;
import ca.mun.outshine.service.SessionManager;
import ca.mun.outshine.util.TimeUtil;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Register";

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    //"(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 4 characters
                    "$");

    private TextInputLayout textInputName;
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPassword;
    private TextInputLayout textInputConfPassword;
    private ProgressBar progressBar;

    // firebase authentication
    private FirebaseAuth auth;

    // firestore
    FirebaseFirestore db;

    // variable
    private int score;
    private String resultType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if logged in -> main / otherwise -> continue
        if (SessionManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // set content
        setContentView(R.layout.activity_register);

        // initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // widgets
        textInputName = findViewById(R.id.register_name);
        textInputEmail = findViewById(R.id.register_email);
        textInputPassword = findViewById(R.id.register_password);
        textInputConfPassword = findViewById(R.id.register_confirm_password);
        progressBar = findViewById(R.id.register_progressbar);

        findViewById(R.id.register_create_account).setOnClickListener(this);
        findViewById(R.id.register_back_to_login).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // check user login status on the start, if logged in -> go to Main
        if (SessionManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // register broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(Constants.HEALTH_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(healthReceiver, filter);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        // make progress bar invisible
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(this).unregisterReceiver(healthReceiver);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.register_create_account) {
            String emailInput = textInputEmail.getEditText().getText().toString().trim();
            String passwordInput = textInputPassword.getEditText().getText().toString().trim();
            String nameInput = textInputName.getEditText().getText().toString().trim();
            if (nameInput.equals("") || nameInput.isEmpty() || nameInput.length() == 0 || nameInput == null) {
                String[] splited = emailInput.split("[@._]");
                nameInput = splited[0];
            }
            createAccount(emailInput, passwordInput, nameInput);
        } else if (i == R.id.register_back_to_login) {
            goToLogin();
        }
    }

    // create a new user account when click on sign up
    private void createAccount(final String email, final String password, final String name) {
        Log.d(TAG, "createAccount:" + email);

        if (!validateInputs()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Create user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");

                            // save user to the session
                            FirebaseUser user = auth.getCurrentUser();

                            // update user display name in firebase auth
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build();
                            user.updateProfile(profileUpdates);

                            // update session
                            SessionManager.getInstance(getApplicationContext())
                                    .createLoginSession(user.getUid(), user.getEmail(), user.getDisplayName());

                            // create a user object to save its extra info into firestore
                            User localUser = new User(user.getUid(), user.getEmail(), name);
                            localUser.setRole("user");

                            DocumentReference userRef = db.collection("Users").document(user.getUid());
                            userRef.set(localUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Added new user.");
                                    } else {
                                        Log.d(TAG, "Adding new user failed. Check log.");
                                    }
                                }
                            });

                            // search for public challenges created by "admin" to enroll the user in them
                            // since it is complicated to be done here but easy in my day fragment
                            final ArrayList<Challenge> challengeList = new ArrayList<>();

                            db.collection("Challenges")
                                    .whereEqualTo("privacy", "public")
                                    .whereEqualTo("creator", "admin")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Log.d("Firestore", document.getId() + " => " + document.getData());

                                                    Challenge challenge = document.toObject(Challenge.class);
                                                    challengeList.add(challenge);
                                                }
                                            }
                                        }
                                    });

                            for (Challenge challenge : challengeList) {

                                Map<String, Object> competitor = new HashMap<>();
                                competitor.put("user_id", user.getUid());
                                competitor.put("user_name", user.getDisplayName());
                                competitor.put("user_photo_url", "");

                                // read total score
                                Date d = challenge.getTime_starts();
                                Calendar c = Calendar.getInstance();
                                c.setTime(d);
                                int elapsedDays = TimeUtil.elapsedDays(c);

                                Intent i = new Intent(getApplicationContext() , HealthService.class);
                                i.putExtra(Constants.HEALTH_LOG, true);
                                i.putExtra(Constants.HEALTH_DAY, elapsedDays);
                                // adding an extra flag to say we want from challenge_start_time until now
                                i.putExtra(Constants.HEALTH_NOW, true);
                                i.putExtra(Constants.HEALTH_TYPE, challenge.getType());
                                startService(i);

                                Log.d(TAG, "onScoreReceived: " + score);

                                competitor.put("total_score", score);
                                competitor.put("total_score_update_time", TimeUtil.todayMidnight());

                                Log.d(TAG, "onCompetitorCreated " + competitor);

                                // todo complete this
//                                competitor.put("active_score", score);
//                                competitor.put("active_score_update_time", ChallengeUtil.getRandomTime());

                                // competitors.put(userId, competitor);
                                challenge.addCompetitor(user.getUid(), competitor);

                                db.collection("Challenges")
                                        .document(challenge.getId())
                                        .set(challenge)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error updating document", e);
                                            }
                                        });
                            }

                            // done with everything, go to main
                            progressBar.setVisibility(View.GONE);
                            goToMain();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Authentication failed" + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private boolean validateInputs() {
        return (validateEmail() && validatePassword());
    }

    private boolean validateEmail() {
        String emailInput = textInputEmail.getEditText().getText().toString().trim();
        if (emailInput.isEmpty()) {
            textInputEmail.setError("Required.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            textInputEmail.setError("Invalid email address.");
            return false;
        } else {
            textInputEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();
        String confPasswordInput = textInputConfPassword.getEditText().getText().toString().trim();
        textInputConfPassword.setError(null);
        if (passwordInput.equals(confPasswordInput)) {
            if (passwordInput.isEmpty()) {
                textInputPassword.setError("Required.");
                return false;
            } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
                textInputPassword.setError("Password is weak.");
                return false;
            } else {
                textInputPassword.setError(null);
                return true;
            }
        } else {
            textInputConfPassword.setError("Password doesn't match.");
            if (passwordInput.isEmpty()) {
                textInputPassword.setError("Required.");
            }
            if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
                textInputPassword.setError("Password too weak");
            }
            return false;
        }
    }

    public void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    // callback when data is received from broadcast
    public BroadcastReceiver healthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            int progress;
            if (intent.getAction().equals(Constants.HEALTH_ACTION)) {
                int resultCode = intent.getIntExtra(Constants.HEALTH_RESULT_CODE, Activity.RESULT_CANCELED);
                if (resultCode == Activity.RESULT_OK) {
                    resultType = intent.getStringExtra(Constants.HEALTH_RESULT_TYPE);
                    score = intent.getIntExtra(Constants.HEALTH_RESULT_VALUE, 0);
                }
            }
        }
    };
}
