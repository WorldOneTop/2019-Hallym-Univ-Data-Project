package com.example.myapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatListActivity extends AppCompatActivity {//로그인후 처음들어갔을때 보이는 채팅방목록 화면
    ListView mListView;
    MyAdapter mMyAdapter;
    SQLiteDatabase sqliteDB;
    Handler handler;
    public static boolean flag;//현재 클래스가 포커스일때 true

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        mListView = findViewById(R.id.listView);
        mMyAdapter = new MyAdapter();

        mListView.setAdapter(mMyAdapter);
        sqliteDB = Intro.DB;
        init_view(true);
        init_ActionBar();
        flag = true;

        handler = new Handler();//1초마다 채팅방 목록 업데이트
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                init_view(false);
                handler.postDelayed(this,1000);
            }
        }, 1000);

        mMyAdapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//리스트뷰 아이템 클릭이벤트
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String __id = mMyAdapter.getItem(position).getuserid();
                Intent intent = new Intent(ChatListActivity.this, InTheChat.class);
                intent.putExtra("id", __id);
                startActivity(intent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return true;//다른이벤트로안넘김, 롱클릭 처리 안함으로써 화면 스크롤하기 수월하게
            }
        });

    }
//                         프로필사진 구현위해서
    //리스트뷰에 추가할 데이터    R.drawable형식, 이름,    글내용,           안읽은 수,   유저아이디
    private void addData(int pictureID, String name, String contents, int unread, String userid) {
        mMyAdapter.addItem(ContextCompat.getDrawable(getApplicationContext(), pictureID), name, contents, unread, userid);
    }

    private void init_view(final boolean isFirst) {
        //데이터베이스이용해서 초기화 onRestart에도 호출
//              닉네임             보낸글             안읽은 수           유저아이디
//        addData(R.drawable.defaultimg, "상대방 이름", "상대방 글", 3, "test001"); 형식 보기 수월하게 주석처리

        if (sqliteDB != null) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        char temp[] = new char[2048];//한번만 읽기에 많은 글자로 처리
                        String t = "";
                        try {
                            Login.bufferedWriter.write("초기화§" + Intro.ID + "§");
                            Login.bufferedWriter.flush();
                            if (Login.bufferedReader.read(temp) == -1) {
                                return;
                            }
                            t = String.valueOf(temp);
                        } catch (IOException e) {
                            Log.d("asd", "에러에러러" + e);
                            e.printStackTrace();
                        }
                        String[] data = t.split("§");
                        HashMap<String, Integer> hashMap = new HashMap<>();         //키에따른 안읽은 수 확인
                        HashMap<String, String> hashMap_data = new HashMap<>();         //키에따른 안읽은 데이터 확인

                        ArrayList<String> temp_str = new ArrayList<>();//채팅방생성용


                        for (int i = 0; i < data.length; i += 5) {           //unread 세팅 구문
                            if (i + 1 == data.length)           //안할경우 공백도 인식후 outofbounds
                                break;
                            hashMap_data.put(data[i + 4],data[i + 1]);
                            temp_str.add(data[i+4]);
                            if (hashMap.containsKey(data[i+4])) {//받는사람에 대해서 보낸사람의 아이디를 키값으로 안읽은수 넣음
                                hashMap.put(data[i + 4], hashMap.get(data[i + 4]) + 1);//해쉬맵에 있다면 값+=1
                            }
                            else {
                                hashMap.put(data[i + 4], 1); //해쉬맵에 아이디 없다면 1개 추가
                            }
                        }
                        if (!hashMap.isEmpty() || isFirst){Log.d("asd","그려야할때"+isFirst);
                            mMyAdapter.clear();}//ListView를 다시그리기 위해서
                        else {Log.d("asd","처음"+isFirst);
                            return;
                        }

                                boolean []temp_b = new boolean[temp_str.size()];//이미만든방체크용
                        for(int i=0;i<temp_b.length;i++)
                            temp_b[i]=false;

                            mMyAdapter.clear();

                            String sqlQueryTbl = "select * from chat where keynum IN" +     //보낸사람들 아이디별로 가장 늦게들어온 데이터를 받는사람과 매치하는 쿼리문
                                    "(SELECT max(keynum) FROM chat group by uid) and id = '" + Intro.ID + "'  order by keynum desc ";
                            Cursor cursor = null;
                            // 쿼리 실행
                            cursor = sqliteDB.rawQuery(sqlQueryTbl, null);
                            while (cursor.moveToNext()) { // 레코드가 존재한다면,
                                int unread = hashMap.containsKey(cursor.getString(5)) ? hashMap.get(cursor.getString(5)) : 0;
                                String d = hashMap.isEmpty() ? cursor.getString(2): hashMap_data.get(cursor.getString(5)) == null ? cursor.getString(2) : hashMap_data.get(cursor.getString(5));
                                if(d.contains(".png"))
                                    d = "사진";

                                /*데이터 추가*/
                                addData(R.drawable.defaultimg, cursor.getString(5), d, unread, cursor.getString(5));
                            }                           //사진데이터일경우 "사진"이란 문구로 대체

                        //새로운 채팅방은 들어갈 방만 만들어줌
                        for(int i=0;i<mMyAdapter.getCount();i++){
                            for(int j=0;j<temp_str.size();j++) {
                                if (!mMyAdapter.getItem(i).getuserid().equals(temp_str.get(j)) &&!temp_b[j] ){//만약 매치가 안되고 체크가 안됐으면 채팅방 생성
                                    addData(R.drawable.defaultimg, temp_str.get(j), hashMap_data.get(temp_str.get(j)), hashMap.get(temp_str.get(j)), temp_str.get(j));//id를 가진 임의의 방 생성으로 참여 유도
                                    temp_b[j]= true;
                                }
                            }
                        }

                    } catch (Exception e) {
                        Log.d("asd", "에에러" + e);
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {      // 데이터 다 받고 다시그려주기 위해서
                e.printStackTrace();
            }
            mMyAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "DB creation failed.", Toast.LENGTH_SHORT).show();
        }


    }



    //액션바 및 툴바 API 사용하려다 실패 후 고정 XML로 사용하는데 이에 대한 그림 및 글자 세팅
    private void init_ActionBar() {//새 채팅방 눌렀을 때
        findViewById(R.id.btnMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {        //새 채팅방 고르는 UI는 다이얼로그로 구현
                final Dialog dialog = new Dialog(ChatListActivity.this);
                final Activity context = ChatListActivity.this;
                dialog.setContentView(R.layout.dialog_newtalk);
                final ImageView imageView1 = dialog.findViewById(R.id.dialog_img1);
                final ImageView imageView2 = dialog.findViewById(R.id.dialog_img2);
                final LinearLayout layout = dialog.findViewById(R.id.dialog_layout);
                imageView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {            //새로운 단톡방 만드는 버튼 클릭했을때
                        imageView1.setVisibility(View.GONE);
                        imageView2.setVisibility(View.GONE);
                        layout.setVisibility(View.VISIBLE);
                        ((ImageView) dialog.findViewById(R.id.dialog_sendimg)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        try {
                                            String id = ((EditText) dialog.findViewById(R.id.dialog_edittext)).getText() + "";
                                            Login.bufferedWriter.write("개인톡§" + id + "§");
                                            Login.bufferedWriter.flush();
                                            int a;
                                            if ((a = Login.bufferedReader.read()) == 1) {//해당아이디잆음 채팅방 생성
                                                addData(R.drawable.defaultimg, id, " ", 0, id);//id를 가진 임의의 방 생성으로 참여 유도
                                                context.runOnUiThread(new Runnable() {
                                                    public void run() {                             //들어가서 메시지를 보낼경우 내부DB에 저장함으로 채팅방생성
                                                        mMyAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                                dialog.dismiss();
                                            } else if (a == 0) {//해당아이디없음
                                                context.runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(context, "해당 하는 회원이 없습니다.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();

                            }
                        });
                    }
                });
                final ArrayList<String> arrayList = new ArrayList<>();      //단톡 이름 명단
                imageView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {                //단톡 생성 클릭리스너, 사람 추가 후 가능한지까지만 구현
                        imageView1.setVisibility(View.GONE);
                        imageView2.setVisibility(View.GONE);
                        layout.setVisibility(View.VISIBLE);
                        ((TextView) dialog.findViewById(R.id.dialog_absol)).setVisibility(View.VISIBLE);
                        ((TextView) dialog.findViewById(R.id.dialog_absol)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        try {
                                            for (int i = 0; i < arrayList.size(); i++) {
                                                Login.bufferedWriter.write(arrayList.get(i) + "§");
                                            }
                                            Login.bufferedWriter.flush();

                                            if (Login.bufferedReader.read() == 1) {

                                            } else {
                                                context.runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(context, "없는 회원이 있습니다.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();

                                Toast.makeText(ChatListActivity.this, "단체톡 ", Toast.LENGTH_LONG).show();
                            }
                        });
                        ((ImageView) dialog.findViewById(R.id.dialog_sendimg)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                arrayList.add(((EditText) dialog.findViewById(R.id.dialog_edittext)).getText() + "");
                                TextView textView = new TextView(context);
                                textView.setText(((EditText) dialog.findViewById(R.id.dialog_edittext)).getText());
                                textView.setTextSize(20);
                                layout.addView(textView);
                                ((EditText) dialog.findViewById(R.id.dialog_edittext)).setText("");
                            }
                        });
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();flag = true;
        init_view(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                init_view(false);
                handler.postDelayed(this,1000);
            }
        }, 1000);
    }
    @Override
    protected void onStop() {
        super.onStop();flag = false;
        handler.removeCallbacksAndMessages(null);
    }
}