package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InTheChat extends Activity {           // 어느 채팅방에 들어갔을때의 화면
    private final int GET_GALLERY_IMAGE = 300;//갤러리
    private final int GET_FILES = 200;//파일인데 안함
    private final int GET_GOOGLE = 100;//구글 위치 정보
    ListView m_ListView;
    MyAdapterChat m_Adapter;
    EditText editText;
    public int origin_height;
    ImageView icon_menu;
    static SQLiteDatabase sqliteDB;
    static String UID;
    ImageView image, file, calendar, loc;
    Activity activity;
//    String d1, d2;
//    int d3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_the_chat);
        activity = this;
        sqliteDB = Intro.DB;
        editText = findViewById(R.id.editText1);
        m_Adapter = new MyAdapterChat(this);
        m_ListView = (ListView) findViewById(R.id.listView1);
        m_ListView.setAdapter(m_Adapter);
        icon_menu = findViewById(R.id.menu);
        UID = getIntent().getStringExtra("id");

        calendar = findViewById(R.id.imageView1);
        file = findViewById(R.id.imageView2);
        image = findViewById(R.id.imageView3);
        loc = findViewById(R.id.imageView4);


        /**디비 초기화부분*/

        init_select();
        init_ActionBar();
        ChatListActivity.flag = false;
        new Thread() {
            @Override
            public void run() {
                super.run();
                String t = "";
                String[] data;
                while (!ChatListActivity.flag) {
                    char temp[] = null;
                    try {
                        temp = new char[255];
                        Login.bufferedWriter.write("채팅방§" + Intro.ID + "§" + UID + "§");
                        Login.bufferedWriter.flush();
                        if (Login.bufferedReader.read(temp) == -1) {
                            return;
                        }
                    } catch (IOException e) {
                        Log.d("asd", "에러에러러" + e);
                        e.printStackTrace();
                    }
                    t = String.valueOf(temp);

                    Log.d("asd값", t);
                    data = t.split("§");//아이디 데이터  날짜   구분값   상대방아이디 순 을 역순으로 가져오기
                    for (int i =  0; i <data.length; i += 5) {
                        if (i + 1 == data.length)           //안할경우 공백도 인식후 outofbounds
                            break;
                        if (data[i + 4].equals(UID)) {//들어간채팅ㅇ방의 아이디 교
                            if (data[i + 3].equals("3")) {        //구분값이 이미지라면  이미지 저장
                                try {
                                    Login.bufferedWriter.write("이미지받기§" + data[i + 1] + "§");
                                    Login.bufferedWriter.flush();

                                    if (Login.bufferedReader.read() == 1) {//파일서버열어줌
                                        Socket socket = new Socket(Login.host, 12345);  //파일입출력은 다른 포트 사용, openFileOutput은 사용할 수 있는 파일경로, 스트림 제공
                                        BufferedOutputStream bw = (new BufferedOutputStream(openFileOutput(data[i + 1], MODE_PRIVATE)));
                                        BufferedInputStream br = new BufferedInputStream((socket.getInputStream()));
                                        byte[] buf = new byte[1024];
                                        int read_length = 0;
                                        while ((read_length = br.read(buf)) > 0) {
                                            Log.d("asd", "다운받는중");
                                            bw.write(buf, 0, read_length);
                                            bw.flush();
                                        }
                                        bw.close();
                                        br.close();
                                        socket.close();
                                    }
                                } catch (IOException e) {
                                    Log.d("asd", "ioexxxc" + e);
                                }
                            }
                            final String d1 = data[i + 1];
                            final String d2 = data[i + 2];
                            final int d3 = Integer.parseInt(data[i + 3]);
                            Log.d("asd",d1);
                            (InTheChat.this).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    m_Adapter.add(d1, d3, d2, UID);
                                    insert(d1, d2, d3);
                                    m_Adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                    t = "";
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();


        //지도 클릭
        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(InTheChat.this,MainActivity.class));

                Intent intent = new Intent(InTheChat.this, Google_Map.class);
                startActivityForResult(intent, GET_GOOGLE);
            }
        });
        //이미지메뉴 클릭
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });
        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(InTheChat.this, "준비중", Toast.LENGTH_LONG).show();
            }
        });
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(InTheChat.this, Calender.class));
            }
        });


        //메뉴버튼눌렀을때
        icon_menu.setOnClickListener(onClickListener_default);
        icon_menu.setTag(2);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                icon_menu.setTag(3);
                findViewById(R.id.menu_layout).setVisibility(View.GONE);
            }
        });

        //아이콘바꾸는거( 메뉴랑 보내는거랑
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!keybordOn()) {
                    if (((int) icon_menu.getTag()) == 1)
                        icon_menu.setImageResource(R.drawable.icon_x);
                    else if (((int) icon_menu.getTag()) == 2 || ((int) icon_menu.getTag()) == 3)
                        icon_menu.setImageResource(R.drawable.menu_icon);
                } else
                    icon_menu.setImageResource(R.drawable.checkimg);
                handler.postDelayed(this, 50);
            }
        });
    }

    private void init_select() {
        if (sqliteDB != null) {
            String sqlQueryTbl = "SELECT * FROM chat where id= '" + Intro.ID + "' and uid = '" + UID + "'";
            Cursor cursor = null;
            // 쿼리 실행
            cursor = sqliteDB.rawQuery(sqlQueryTbl, null);

            while (cursor.moveToNext()) { // 레코드가 존재한다면,
                //메시지 타입 날짜 아이디
                m_Adapter.add(cursor.getString(2), cursor.getInt(4), cursor.getString(3), cursor.getString(5));
            }
        } else {
            Toast.makeText(this, "DB creation failed.", Toast.LENGTH_SHORT).show();
        }
        m_Adapter.notifyDataSetChanged();
        m_ListView.setSelection(m_Adapter.getCount() - 1);
    }

    //내가 입력하는insert
    private static void insert(String msg, int code) {
        final String m = msg;
        final int c = code;
        if (sqliteDB != null) {
            sqliteDB.execSQL("insert into chat(id,data,date,code,uid) values ('" +
                    Intro.ID + "','" +
                    msg + "','" +
                    new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "','" +
                    code + "','" +
                    UID + "')");
            new Thread() {
                @Override
                public void run() {//보내기§보낸아이디§데이터§날짜§구분값§받는아이디§
                    super.run();
                    try {
                        if (c != 2) {//코드값 2인 이미지보내기는 다른곳에서구현
                            Login.bufferedWriter.write("보내기§" + Intro.ID + "§" + m + "§" + new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "§" + (c + 1) + "§" + UID + "§");
                            Login.bufferedWriter.flush();
                        }
                    } catch (IOException e) {
                        Log.d("asd", "에러 통신" + e);
                        e.printStackTrace();
                    }
                }
            }.start();


        } else {
//            Toast.makeText(this, "DB creation failed.", Toast.LENGTH_SHORT).show();
        }
    }

    //상대방 말 디비에 넣기
    private static void insert(String msg, String date, int code) {
        final String m = msg;
        final int c = code;
        if (sqliteDB != null) {
            sqliteDB.execSQL("insert into chat(id,data,date,code,uid) values ('" +
                    Intro.ID + "','" +
                    msg + "','" +
                    date + "','" +
                    code + "','" +
                    UID + "')");

        } else {
//            Toast.makeText(this, "DB creation failed.", Toast.LENGTH_SHORT).show();
        }
    }

    //이밑에는 아래쪽레이아웃
    @Override//포커스가잡혔을때
    public void onWindowFocusChanged(boolean hasFocus) {
        origin_height = findViewById(R.id.linearParent).getHeight();
    }

    public boolean keybordOn() {
        return origin_height > findViewById(R.id.linearParent).getHeight() + 150;//10은 안전상
    }

    //메뉴버튼 누르면 바뀌는거 때문에 만든 리스너 두개
    View.OnClickListener onClickListener_default = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (keybordOn()) {
                if (!((editText.getText() + "").equals(""))) {
                    m_Adapter.add(editText.getText() + "", 0, new SimpleDateFormat("yyyy/MM/dd").format(new Date()), "");

                    insert(editText.getText() + "", 0);
                    editText.setText("");
                    m_Adapter.notifyDataSetChanged();
                }
            } else {
                findViewById(R.id.menu_layout).setVisibility(View.VISIBLE);
                icon_menu.setTag(1);
                icon_menu.setOnClickListener(onClickListener_close);
            }
        }
    };
    View.OnClickListener onClickListener_close = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            findViewById(R.id.menu_layout).setVisibility(View.GONE);
            icon_menu.setOnClickListener(onClickListener_default);
            icon_menu.setTag(2);
        }
    };
    public String fileName;

    //갤러리 불러오는 매서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //갤러리 이미지를 가져오기 성공했을때
        //파일로 저장
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == GET_GALLERY_IMAGE && data.getData() != null) {
                final Intent d = data;
                final int size = sqliteDB.rawQuery("select * from chat", null).getCount();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        try {
                            Login.bufferedWriter.write("파일명§");
                            Login.bufferedWriter.flush();
                            char[] a = new char[255];
                            Login.bufferedReader.read(a);
                            String[] s = String.valueOf(a).split("§");
                            fileName = s[0];
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String path = getRealPathFromURI(d.getData());
                        try {
                            BufferedOutputStream fileOutputStream = (new BufferedOutputStream(openFileOutput(fileName, MODE_PRIVATE)));
                            BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)));
                            byte temp[] = new byte[1024];
                            while (in.read(temp) != -1)
                                fileOutputStream.write(temp);
                            in.close();
                            fileOutputStream.close();


                            Login.bufferedWriter.write("이미지보내기§" + Intro.ID + "§" + fileName + "§" + new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "§" + (3) + "§" + UID + "§");
                            Login.bufferedWriter.flush();

                            if (Login.bufferedReader.read() == 1) {//파일서버열어줌
                                Socket socket = new Socket(Login.host, 12345);
                                BufferedInputStream br = (new BufferedInputStream(new FileInputStream("/data/user/0/com.example.myapplication/files/" + fileName)));
                                BufferedOutputStream bw = new BufferedOutputStream((socket.getOutputStream()));
                                byte[] buf = new byte[1024];
                                int read_length = 0;
                                while ((read_length = br.read(buf)) > 0) {
                                    bw.write(buf, 0, read_length);
                                    bw.flush();
                                }
                                bw.close();
                                br.close();
                                socket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("asd", "에러에러" + e);
                        }
                        (InTheChat.this).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                m_Adapter.add(fileName, 2, new SimpleDateFormat("yyyy/MM/dd").format(new Date()), UID);
                                insert(fileName, 2);
                                m_Adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }.start();

            } else if (requestCode == GET_GOOGLE) {//구글을 리퀘스트로 받았을때 >> 내가 위치보낼떄
                m_Adapter.add(data.getStringExtra("msg"), 4, new SimpleDateFormat("yyyy/MM/dd").format(new Date()), "");
                insert(data.getStringExtra("msg"), 4);
                m_Adapter.notifyDataSetChanged();
            }
        }

    }

    //인터넷에서 Uri포멧의 객체의 실제 파일 저장 경로를 찾는 함수 예제 따옴
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        return cursor.getString(column_index);
    }

    //여기선 딱히 세팅할게 없으므로 짧음
    private void init_ActionBar() {
        ImageView imageView1 = findViewById(R.id.btnBack);
        imageView1.setImageResource(R.drawable.left_arrow);
        ((TextView) findViewById(R.id.title)).setText(UID);
        ((ImageView) findViewById(R.id.btnMenu)).setVisibility(View.GONE);
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
}