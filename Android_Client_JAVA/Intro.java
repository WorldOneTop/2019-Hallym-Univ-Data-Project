package com.example.myapplication;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Intro extends Activity implements DialogInterface.OnDismissListener{// 맨처음 시작부분, 처음시작하므로
    public static int WIDTH,HEIGHT;//화면의 너비                                             static변수는 세팅 및 다른 함수에서도 사용
    public static Resources resources;
    static SQLiteDatabase DB;       //사용할 데이터베이스 객체
    public static String ID;        //사용자의 아이디
    Dialog dialog;
    public static boolean logined;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        /*인트로부분으로 옮겨야할 것 퍼미션 설정부분*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }

        logined = false;
        DB = init_database();//디비 초기화
        init_tables();



        final TextView textView = findViewById(R.id.textview);
        textView.setText("made by\n20175259 최슬기\n20185157 이윤석\n20185159 이제일");

        resources = getResources();
        WIDTH = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        HEIGHT= getApplicationContext().getResources().getDisplayMetrics().heightPixels;

        findViewById(R.id.imageView2).setOnClickListener(new View.OnClickListener() {//회원가입
            @Override
            public void onClick(View view) {
                dialog = new Dialog(Intro.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_login);
                TextView textView1 = dialog.findViewById(R.id.textview_login);
                textView1.setText("회원가입");
                textView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String e1 = ((EditText)dialog.findViewById(R.id.login_et1)).getText()+"";
                        String e2 = ((EditText)dialog.findViewById(R.id.login_et2)).getText()+"";

                        if(e1.equals("") || e2.equals(""))
                            Toast.makeText(Intro.this,"아이디 비번을 모두 입력해주세요",Toast.LENGTH_LONG).show();
                        else {
                            if(e1.contains(".")||e1.contains("§")||e1.contains(",")||e2.contains(".")||e2.contains("§")||e2.contains(","))
                                Toast.makeText(Intro.this,"포함할 수 없는 문자가 있습니다.",Toast.LENGTH_LONG).show();
                            else
                                new Login(e1, e2, false, Intro.this,dialog).start();
                        }
                    }
                });
                dialog.show();
                dialog.setOnDismissListener((DialogInterface.OnDismissListener)Intro.this);
            }
        });
        findViewById(R.id.imageView3).setOnClickListener(new View.OnClickListener() {//로그인
            @Override
            public void onClick(View view) {
                dialog = new Dialog(Intro.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_login);
                TextView textView1 = dialog.findViewById(R.id.textview_login);
                textView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String e1 = ((EditText)dialog.findViewById(R.id.login_et1)).getText()+"";
                        String e2 = ((EditText)dialog.findViewById(R.id.login_et2)).getText()+"";

                        if(e1.equals("") || e2.equals(""))
                            Toast.makeText(Intro.this,"아이디 비번을 모두 입력해주세요",Toast.LENGTH_LONG).show();
                        else {
                            if(e1.contains(".")||e1.contains("§")||e1.contains(",")||e2.contains(".")||e2.contains("§")||e2.contains(","))
                                Toast.makeText(Intro.this,"포함할 수 없는 문자가 있습니다.",Toast.LENGTH_LONG).show();
                            else
                                new Login(e1, e2, true, Intro.this,dialog).start();
                        }
                    }
                });

                dialog.show();
                dialog.setOnDismissListener((DialogInterface.OnDismissListener)Intro.this);
            }
        });

//        new Handler().postDelayed(new Runnable() {
//           @Override
//           public void run() {
//               startActivity(new Intent(Intro.this, ChatListActivity.class));
//               finish();
//           }
//       }, 2000);
    }

    //이밑에는 데이터베이스 관리부분
    private SQLiteDatabase init_database() {//디비가 없다면 만듦

        SQLiteDatabase db = null;

        try {
            db = SQLiteDatabase.openOrCreateDatabase(new File(getFilesDir(), "chatDB.db"), null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if (db == null) {
            Log.d("asd", "DB creation failed. ");
            Toast.makeText(this, "DB creation failed.", Toast.LENGTH_SHORT).show();

        }
        return db;
    }

    private void init_tables() {//테이블이 없다면 만듦
        if (DB != null) {
            String sqlCreateTbl = "CREATE TABLE IF NOT EXISTS chat (" +
                    "keyNum " + "INTEGER primary key autoincrement," +
                    "id " + "TEXT," +
                    "data " + "TEXT," +
                    "date " + "TEXT," +
                    "code " + "INTEGER,"+
                    "UID " + "TEXT"+ ")";
            Log.d("asd", sqlCreateTbl);
            DB.execSQL(sqlCreateTbl);
//            DB.execSQL("drop table chat");
        }
    }

    @Override           //다이얼로그창이 꺼지면 실행되는 이벤트리스너
    public void onDismiss(DialogInterface dialog){
       if(logined) {
           startActivity(new Intent(Intro.this, ChatListActivity.class));
               finish();
       }
    }
}