//Activity for verifying otp.

package com.techfreaks.safetrigger.coptracking;

//Importing all required libraries.

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Otp extends AppCompatActivity {

    //Declaring all required objects.
    private String phone;
    private TextView txt_mobile;
    private EditText txt_code;
    private String verificationId;
    private FirebaseAuth mAuth;
    //Automatically accessing the otp from device.
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                txt_code.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(Otp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            verificationId = s;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        //Declaring FirebaseAuth object for user account authentication.
        mAuth = FirebaseAuth.getInstance();
        txt_code = findViewById(R.id.txt_code);
        txt_mobile = findViewById(R.id.txt_mobile);

        phone = "+91" + getIntent().getStringExtra("phone");
        txt_mobile.append(phone);

        //Calling the method for sending otp code.
        sendVerificationCode(phone);

    }

    //Method for verifying the otp.
    private void verifyCode(String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    //Signing in the user when verification success.
    private void signInWithCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Intent intent = new Intent(Otp.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(Otp.this, "error", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    //Method for sending otp code to user account.
    private void sendVerificationCode(String phone) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
    }

    //Method called when automatic verification fails.
    public void manualVerifyCode(View v) {
        String code = txt_code.getText().toString();
        if (code.isEmpty() || code.length() < 6) {
            txt_code.setError("Enter Code");
            txt_code.requestFocus();
            return;
        }
        verifyCode(code);
    }
}
