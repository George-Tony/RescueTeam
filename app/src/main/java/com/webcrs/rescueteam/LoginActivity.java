package com.webcrs.rescueteam;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    public static final String loginURL = "";

    @BindView(R.id.getName)
    EditText getName;

    @BindView(R.id.getPhoneNo)
    EditText getPhoneNo;

    @BindView(R.id.getEmailID)
    EditText getEmailID;

    @BindView(R.id.submit)
    Button submit;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        sessionManager = new SessionManager(getApplicationContext());


    }

    @OnClick(R.id.submit)
    public void submitRescueDetails() {

        String name = getName.getText().toString();
        String phoneNo = getPhoneNo.getText().toString();
        String emailID = getEmailID.getText().toString();
        sessionManager.savedata(name,phoneNo,emailID);
        sessionManager.saveUserID("123");

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

}
