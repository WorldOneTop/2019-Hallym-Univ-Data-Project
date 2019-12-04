package com.example.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static android.app.Activity.RESULT_OK;


public class Login extends Thread {//로그인,회원가입에서 통신에 쓰이는 클래스, 여기서의 소켓은 종료하지않고 다른 클래스에서 사용, 앱 종료시 다시 사용가능
    String id, pwd;
    boolean islogin;
    Activity context;
    Dialog dialog;
    public static String host;
    public static Socket socket;
    public static BufferedReader bufferedReader;
    public static BufferedWriter bufferedWriter;
    Login(String i, String pw, boolean is, Activity c,Dialog dia) {
        id = i;
        pwd = pw;
        islogin = is;
        context = c;
        dialog = dia;
        host = "192.168.1.136";
    }

    @Override
    public void run() {

        try {

            socket = new Socket(host, 12344);
             bufferedReader= new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
             bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));

            bufferedWriter.write((islogin ? "로그인§" : "회원가입§")+id+"§"+pwd+"§");
            bufferedWriter.flush();
            int a =socket.getInputStream().read();
            if(a  == 1) {
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, islogin ?  "로그인 성공" : "회원가입 성공" , Toast.LENGTH_SHORT).show();
                        Intro.logined=true;
                        Intro.ID = id;
                        dialog.dismiss();
                    }
                });
            }
            else if(a==0){
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, islogin ? "아이디 또는 비밀번호 확인" :"아이디 중복", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
