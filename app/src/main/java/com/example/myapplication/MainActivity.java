package com.example.myapplication;

import android.content.Intent;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button b1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1=findViewById(R.id.b1);

        b1.setOnClickListener(click);


    }
    Button.OnClickListener click=new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent mintent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mintent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mintent.putExtra(RecognizerIntent.EXTRA_PROMPT,"說些什麼...");
            startActivityForResult(mintent,10);

        }
    };
}
