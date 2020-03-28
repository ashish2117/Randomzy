package com.ash.randomzy.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ash.randomzy.R;
import com.ash.randomzy.model.User;
import com.ash.randomzy.utility.ActivityLauncher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText name, password, emailTxt, confPass;
    private Button registerButton;
    private TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        password = (EditText) findViewById(R.id.password);
        name = (EditText) findViewById(R.id.name);
        emailTxt = (EditText) findViewById(R.id.email);
        confPass = (EditText) findViewById(R.id.confpassword);
        registerButton = (Button) findViewById(R.id.registerButton);
        login = findViewById(R.id.login);

        login.setOnClickListener((view)->{
            startLoginActivity();
        });

        registerButton.setOnClickListener((view)->{
            registerUser();
        });
    }

    private void startLoginActivity(){
        ActivityLauncher.startActivityClearCurrentTask(RegisterActivity.this,LoginActivity.class);
    }

    private void registerUser() {
        String password = this.password.getText().toString();
        String emailId = this.emailTxt.getText().toString();
        String confPass = this.confPass.getText().toString();
        User user = getUserObj();

        if(!password.equals(confPass)){
            Toast.makeText(this,"Passwords don't match",Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailId,password).addOnCompleteListener((task)->{
            if(task.isSuccessful()){
                    ActivityLauncher.startActivityClearCurrentTask(RegisterActivity.this, MainActivity.class);
                    user.setuId(mAuth.getCurrentUser().getUid());
                    UserProfileChangeRequest request=new UserProfileChangeRequest.Builder().setDisplayName(user.getName()).build();
                    mAuth.getCurrentUser().updateProfile(request);
                    addUserDetails(user);
            }
        }).addOnFailureListener((task)->{
            Toast.makeText(this,task.getMessage(),Toast.LENGTH_SHORT).show();
        });

    }

    private User getUserObj(){
        User user = new User();
        user.setEmailId(this.emailTxt.getText().toString());
        user.setName(this.name.getText().toString());
        user.setBio("");
        user.setSex("");
        user.setDateOfBirth(new Date());
        return user;
    }

    private void addUserDetails(User user) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(mAuth.getCurrentUser().getUid()).setValue(user).addOnFailureListener((failure)->{
           Log.d("Failll",failure.getMessage());
        });
    }
}
