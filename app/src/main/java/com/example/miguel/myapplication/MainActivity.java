package com.example.miguel.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Intent it = new Intent(getApplicationContext(), TrackActivity.class);
            startActivity(it);
        }
    }

    TextInputEditText usr, pass;
    TextView lost;
    Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }
    public void init(){
        usr = findViewById(R.id.txt_field_user);
        pass = findViewById(R.id.txt_field_pass);
        lost = findViewById(R.id.txt_lost_pass);
        loginButton = findViewById(R.id.button_login);

        auth = FirebaseAuth.getInstance();

        lost.setOnClickListener(this);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.txt_lost_pass){
            lost_pass();
        }
        if(v.getId() == R.id.button_login){
            flogin();
        }
    }
    public void lost_pass (){
        final String Nombre = usr.getText().toString();
        //inicar un activity temporal para cambiar contraseña
        if(Nombre.length() > 1 ) {
            auth.sendPasswordResetEmail(Nombre).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), getResources().getText(R.string.recuperacion), Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), getResources().getText(R.string.eroor_recupercaion), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    public void flogin (){
        //iniciar sesion
        final String Nombre = usr.getText().toString();
        final String Pass = pass.getText().toString();

        auth.signInWithEmailAndPassword(Nombre, Pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent it = new Intent(getApplicationContext(), TrackActivity.class);
                    startActivity(it);
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(MainActivity.this, getResources().getText(R.string.error_login), Toast.LENGTH_SHORT).show();

                }
            }
        });


    }
}
