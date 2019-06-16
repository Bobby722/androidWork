package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetectFaceActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private File systemFile;
    private ProgressDialog progressDialog;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_detect_face);
        startCameraIntent();
        textToSpeech = new TextToSpeech(this, this);

    }

    private void startCameraIntent() {
        if (!Util.checkAndRequestPermission(this, PERMISSIONS_REQUEST_CODE)) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/img";
        File file = new File(path);
        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @SuppressLint("SimpleDateFormat")
        String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        systemFile = new File(path, filename + ".jpg");
        systemFile.getParentFile().mkdirs();
        String authority = getPackageName() + ".provider";
        Uri imageUri = FileProvider.getUriForFile(this, authority, systemFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                return;
            }
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    return;
                }
            }
            startCameraIntent();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    Bitmap photoBitmap = BitmapFactory.decodeFile(systemFile.getAbsolutePath(), options);
                    int bitmapDegree = Util.getBitmapDegree(systemFile.getAbsolutePath());
                    if (bitmapDegree != 0) {
                        photoBitmap = Util.rotateBitmapByDegree(photoBitmap, bitmapDegree);
                    }
                    displayPhotoBitmap(photoBitmap);
                    getDetectResultFromServer(photoBitmap);
                    break;
                default:
                    break;
            }
        }
    }

    private void displayPhotoBitmap(Bitmap bitmap) {
        Glide.with(this).load(bitmap).into(((ImageView) findViewById(R.id.activityDetectFace_imageView)));
    }

    public void getDetectResultFromServer(final Bitmap photo) {
        FaceService faceService = createRetrofit();
        faceService.getFaceInformation(BuildConfig.API_KEY, BuildConfig.API_SECRET,
                Util.base64(photo), 1, "gender,age,smiling,emotion,beauty")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FaceppBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        showProgressDialog();
                    }

                    @Override
                    public void onNext(FaceppBean faceppBean) {
                        handleDetectResult(faceppBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressDialog.hide();
                    }

                    @Override
                    public void onComplete() {
                        progressDialog.hide();
                    }
                });
    }

    private void handleDetectResult(FaceppBean faceppBean) {
        List<FaceppBean.FacesBean> faces = faceppBean.getFaces();
        if (faces == null || faces.size() == 0) {
            guessAgeToast(null);
        } else {
            guessAgeToast(faces);
        }
    }

    public void guessAgeToast(List<FaceppBean.FacesBean> faces) {
        if (faces == null || faces.size() <= 0) {
            Toast.makeText(this, "沒有檢查到臉部...", Toast.LENGTH_LONG).show();
        } else {
            textToSpeech.speak("我猜測您大概是 : " + faces.get(0).getAttributes().getAge().getValue() + " 歲", TextToSpeech.QUEUE_FLUSH, null);     //發音
            Toast.makeText(this, "我猜測您大概是 : " + faces.get(0).getAttributes().getAge().getValue() + " 歲", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("請稍後.....");
            progressDialog.setCancelable(false);
        }
        if (!isFinishing()) {
            progressDialog.show();
        }
    }

    private FaceService createRetrofit() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api-cn.faceplusplus.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        return retrofit.create(FaceService.class);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.TAIWAN); //設定語音語言
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d("TTS", "This Language is not supported");
            } else {
                textToSpeech.setPitch(1);    //語調(1為正常語調；0.5比正常語調低一倍；2比正常語調高一倍)
                textToSpeech.setSpeechRate(1);    //速度(1為正常速度；0.5比正常速度慢一倍；2比正常速度快一倍)
            }
        } else {
            Log.d("TTS", "Initilization Failed!");
        }
    }
}
