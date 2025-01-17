//Activity for registering the user using Firebase Authentication service.

package com.techfreaks.safetrigger.coptracking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    //Declaring all required layout views.
    private EditText txt_name, txt_id, txt_email, txt_phone, txt_pass, txt_confirmPass;
    private String name, id, email, phone, pass, confirmPass;

    //DatabaseReference object for accessing the Firebase db.
    private DatabaseReference myRef;

    //FirebaseAuth object for registering the user on Firebase.
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txt_name = findViewById(R.id.txt_name);
        txt_email = findViewById(R.id.txt_email);
        txt_id = findViewById(R.id.txt_id);
        txt_pass = findViewById(R.id.txt_pass);
        txt_confirmPass = findViewById(R.id.txt_confirmPass);
        txt_phone = findViewById(R.id.txt_phone);

        //Getting the instance of FirebaseAuth service.
        firebaseAuth = FirebaseAuth.getInstance();
    }

    //Method to validate the entered details by the user.
    private boolean validateEnteredDetails() {

        name = txt_name.getText().toString();
        email = txt_email.getText().toString();
        pass = txt_pass.getText().toString();
        confirmPass = txt_confirmPass.getText().toString();
        phone = txt_phone.getText().toString();
        id = txt_id.getText().toString();

        if (TextUtils.isEmpty(name)) {
            txt_name.setError("Enter Name");
        } else if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txt_email.setError("Enter a Valid Email");
        } else if (pass.length() < 6) {
            txt_pass.setError("Password Length at least 6 characters");
        } else if (id.length() != 5) {
            txt_id.setError("Enter 5 digit ID number");
        } else if (!TextUtils.equals(pass, confirmPass)) {
            txt_confirmPass.setError("Passwords Do Not Match");
        } else if (phone.length() != 10) {
            txt_phone.setError("Enter 10 digit Phone number");
        } else {
            return true;
        }
        return false;

    }

    //Method fore registering the user.
    public void registerUser(View view) {

        //Registering user on Firebase if entered details are validated.
        if (validateEnteredDetails()) {

            //Creating/Registering user on Firebase,
            firebaseAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                //If user is registered in Firebase Auth service, then add user details to Firebase db.
                                FirebaseDatabase.getInstance().getReference("actors")
                                        .child("cops")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                myRef = FirebaseDatabase.getInstance().getReference("actors")
                                        .child("cops")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                myRef.child("name").setValue(name);
                                myRef.child("email").setValue(email);
                                myRef.child("phone").setValue(phone);
                                myRef.child("id").setValue(id);

                                //Redirecting to the otp activity for otp verification.
                                Toast.makeText(getApplicationContext(), name+email+phone, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Register.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("name", name );
                                intent.putExtra("email", email);
                                intent.putExtra("phone", phone);
                                intent.putExtra("id", id);
                                startActivity(intent);
                                finish();
                            } else {

                                //Displaying toast for unsuccessful registration.
                                Toast.makeText(Register.this, "Register unsuccessful: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
