package it.polito.mad.lab3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import android.widget.Button;
import android.graphics.Paint;



public class SignInActivity extends AppCompatActivity
        implements View.OnClickListener{
    private final String TAG = "FB_SIGNIN";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText etPass;
    private EditText etEmail;
    private EditText confirmPass;
    private boolean signup = false;

    private Button createButton;
    private Button signinButton;
    private Button confirmButton;
    private ImageButton undoButton;

    /**
     * Standard Activity lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        // Set up click handlers and view item references
        findViewById(R.id.btnCreate).setOnClickListener(this);
        findViewById(R.id.btnSignIn).setOnClickListener(this);
        findViewById(R.id.btnSignOut).setOnClickListener(this);
        findViewById(R.id.btnConfirm).setOnClickListener(this);
        findViewById(R.id.undo).setOnClickListener(this);

        //to manage visibility
        createButton = findViewById(R.id.btnCreate);
        createButton.setBackgroundColor(Color.TRANSPARENT);
        createButton.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        createButton.setPaintFlags(createButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        signinButton = findViewById(R.id.btnSignIn);
        confirmButton = findViewById(R.id.btnConfirm);
        undoButton = findViewById(R.id.undo);

        etEmail = findViewById(R.id.your_email);
        etPass = findViewById(R.id.your_pass);
        confirmPass = findViewById(R.id.confirm_pass);
        confirmPass.setVisibility(View.INVISIBLE);


        // TODO: Get a reference to the Firebase auth object
        mAuth = FirebaseAuth.getInstance();

        // TODO: Attach a new AuthListener to detect sign in and out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "Signed in: " + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "Currently signed out");
                }
            }
        };

        updateStatus();
    }

    /**
     * When the Activity starts and stops, the app needs to connect and
     * disconnect the AuthListener
     */
    @Override
    public void onStart() {
        super.onStart();
        // TODO: add the AuthListener
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // TODO: Remove the AuthListener
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }



    private boolean checkFormFields() {
        String email, password;

        email = etEmail.getText().toString();
        password = etPass.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("Email Required");
            return false;
        }
        if (password.isEmpty()){
            etPass.setError("Password Required");
            return false;
        }

        return true;
    }

    private void updateStatus() {
        TextView tvStat = (TextView)findViewById(R.id.tvSignInStatus);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvStat.setText("Signed in: " + user.getEmail());
        }
        else {
            tvStat.setText("Signed Out");
        }
    }

    private void updateStatus(String stat) {
        TextView tvStat = (TextView)findViewById(R.id.tvSignInStatus);
        tvStat.setText(stat);
    }

    private void signUserIn() {
        if (!checkFormFields())
            return;

        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        // TODO: sign the user in with email and password credentials
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignInActivity.this, "Signed in", Toast.LENGTH_SHORT)
                                            .show();
                                    goToShowProfile();
                                }
                                else {
                                    Toast.makeText(SignInActivity.this, "Sign in failed", Toast.LENGTH_SHORT)
                                            .show();
                                }

                                updateStatus();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            updateStatus("Invalid password.");
                        }
                        else if (e instanceof FirebaseAuthInvalidUserException) {
                            updateStatus("No account with this email.");
                        }
                        else {
                            updateStatus(e.getLocalizedMessage());
                        }
                    }
                });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignIn:
                signUserIn();
                break;

            case R.id.btnConfirm:
                if (!signup) {
                    confirmPass.setVisibility(View.VISIBLE);
                    confirmPass.setHint("Confirm Password");
                    signup = true;
                }
                else if (etPass.getText().toString().equals(confirmPass.getText().toString()) )
                        createUserAccount();
                            else
                                confirmPass.setError("The passwords are not equal");
                break;

            case R.id.btnSignOut:
                signUserOut();
                break;

            case R.id.btnCreate:
                confirmPass.setVisibility(View.VISIBLE);
                confirmPass.setHint("Confirm Password");

                signinButton.setVisibility(View.INVISIBLE);
                createButton.setVisibility(View.INVISIBLE);

                confirmButton.setVisibility(View.VISIBLE);
                undoButton.setVisibility(View.VISIBLE);
                undoButton.setBackgroundColor(Color.TRANSPARENT);
                break;

            case R.id.undo:
                confirmPass.setVisibility(View.INVISIBLE);
                signinButton.setVisibility(View.VISIBLE);
                createButton.setVisibility(View.VISIBLE);
                confirmButton.setVisibility(View.INVISIBLE);
                undoButton.setVisibility(View.INVISIBLE);
                break;
        }
    }


    private void signUserOut() {
        // TODO: sign the user out
        mAuth.signOut();
        updateStatus();
        goToShowProfile();
    }

    private void createUserAccount() {
        if (!checkFormFields())
            return;

        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        System.out.print("email e password "+email+password+"\n");

        // TODO: Create the user account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignInActivity.this, "User created", Toast.LENGTH_SHORT)
                                            .show();
                                    goToEditProfile();
                                } else {
                                    Toast.makeText(SignInActivity.this, "Account creation failed", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            updateStatus("This email address is already in use.");
                        }
                        else {
                            updateStatus(e.getLocalizedMessage());
                        }
                    }
                });
    }

    private void goToShowProfile () {

        saveUID();

        Intent intent = new Intent(getApplicationContext(), ShowProfile.class);
        startActivity(intent);
    }

    private void goToEditProfile () {

        saveUID();

        Intent intent = new Intent(getApplicationContext(), EditProfile.class);
        startActivity(intent);
    }


    private void saveUID() {

        SharedPreferences sharedPref = this.getSharedPreferences("shared_id",Context.MODE_PRIVATE); //to save and load small data
        SharedPreferences.Editor editor = sharedPref.edit();  //to modify shared preferences

        editor.putString("uID", mAuth.getUid());

        editor.apply();

    }

}
