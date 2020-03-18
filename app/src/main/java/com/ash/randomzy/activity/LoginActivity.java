package com.ash.randomzy.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ash.randomzy.R;
import com.ash.randomzy.utility.ActivityLauncher;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextView registerUser,errorMsgTextView;
    private EditText emailId, password;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        registerUser = (TextView)findViewById(R.id.register);
        emailId = (EditText)findViewById(R.id.emailId);
        password = (EditText)findViewById(R.id.password);
        loginButton = (Button)findViewById(R.id.loginButton);
        errorMsgTextView = findViewById(R.id.errorMessageTextView);
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(LoginActivity.this);

        loginButton.setOnClickListener((view)->{
            String emailId, password;
            emailId = this.emailId.getText().toString();
            password = this.password.getText().toString();
            if(emailId.isEmpty() ) {
                this.emailId.setError("Can't be blank");
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailId).matches()) {
                this.emailId.setError("Not an email Id");
            }else if (password.equals("")) {
                this.password.setError("Can't be blank");
            } else {
                pd.setMessage("Loading...");
                pd.show();
                loginUser(emailId,password);
            }
        });

        registerUser.setOnClickListener((view)->{
            openRegisterActivity();
        });

    }

    private void openRegisterActivity() {
        ActivityLauncher.startActivityClearCurrentTask(this, RegisterActivity.class);
    }

    private void loginUser(String emailId, String password) {
        this.mAuth.signInWithEmailAndPassword(emailId,password).addOnCompleteListener((task)->{
            if(task.isSuccessful()){
                pd.dismiss();
                ActivityLauncher.startActivityClearCurrentTask(LoginActivity.this,MainActivity.class);
            }
        }).addOnFailureListener((failure)->{
            pd.dismiss();
            errorMsgTextView.setText(failure.getMessage());
        });
    }
}
