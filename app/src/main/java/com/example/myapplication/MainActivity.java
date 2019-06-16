package com.example.myapplication;

import android.content.Intent;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

import java.sql.Array;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnInitListener {

    Button b1;
    TextToSpeech tts;  //語音輸出
    String[] joke = {"大像是誰害的? 氣象局",
            "在成功嶺集訓的某天，正在上基本教練時，有一個大頭兵突然尿急，所以就跑過去向班長說：｢報告班長，我想上二號。」，腳果班長就若無其事的大喊一聲：｢二號過來，有人想上你!」"
    ,"為什麼模範生容易被綁架? 因為他一副好綁樣","客人吃完東西，服務生問：那我收囉？客人：好然後服務生就開始跳舞了",
            "有一天有個帥哥走在路上，一個阿嬤突然上前搭訕：「帥哥，你超會搭耶」然後帥哥就冒煙了"
    ,"為什麼放連假的時候不能去工作？ 因為會變成連假勞工","小狗跟貓咪誰先上台背課文? 小狗 旺旺仙貝","有一天檸檬跑步，汗滴到腳上。 檸檬：我的腿好酸"
    ,"在非洲每過六十秒，台灣就過了一分鐘","液晶的媽媽叫什麼名字？液晶螢幕"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1=findViewById(R.id.b1);
        tts = new TextToSpeech(this,this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String s = "";
        if (resultCode==RESULT_OK){
            List<String> mList=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            s=mList.get(0);
            switch (s){
                case "講笑話":
                    int num = (int) (Math.random()*9);
                    tts.speak(joke[num], TextToSpeech.QUEUE_FLUSH, null);     //發音
                    break;
                case "猜我幾歲":
                    startActivity(new Intent(this, DetectFaceActivity.class));
                    break;
                case "我在哪裡":
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this,MapsActivity.class);
                    startActivity(intent);
                    break;
                case "你是誰":
                    tts.speak("我是你的個人助理，pepper!", TextToSpeech.QUEUE_FLUSH, null);     //發音
                    break;
                default:
                    Toast.makeText(MainActivity.this, "不能辨識", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(Locale.TAIWAN); //設定語音語言
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.d("TTS","This Language is not supported");
            }else{
                tts.setPitch(1);    //語調(1為正常語調；0.5比正常語調低一倍；2比正常語調高一倍)
                tts.setSpeechRate(1);    //速度(1為正常速度；0.5比正常速度慢一倍；2比正常速度快一倍)
            }
        }else{
            Log.d("TTS", "Initilization Failed!");
        }
    }
}
