package com.example.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MyAdapterChat extends BaseAdapter {
    Activity inthechat;
    public class ListContents{
        String msg;
        int type;
        String date;
        String id;
        ListContents(String _msg,int _type,String d,String i){
            this.msg = _msg;
            this.type = _type;
            date = d;
            id = i;
        }
    }
    private class CustomHolder {// 홀더로 뷰들을 정의 및 세팅해줘야지 원하는 레이아웃 구현가능
        TextView    m_TextView,m_TextView2;
        LinearLayout    layout;
        View viewRight;
        View viewLeft;
        ImageView m_ImageView;
    }
    private ArrayList<ListContents> m_List;
    public MyAdapterChat(Activity c) {
        m_List = new ArrayList<ListContents>(); inthechat = c;
    }
    // 외부에서 아이템 추가 요청 시 사용
    public void add(String _msg,int _type,String d,String id) {
        if(m_List.size()==0 ||!m_List.get(m_List.size()-1).date.equals(d))//데이터가 없거나 전에 보낸 메시지랑 다르면 날짜데이터 추가
            m_List.add(new ListContents("",-1,d,id));

        m_List.add(new ListContents(_msg,_type,d,id));
    }

    // 외부에서 아이템 삭제 요청 시 사용
    public void remove(int _position) {
        m_List.remove(_position);
    }
    @Override
    public int getCount() {
        return m_List.size();
    }

    @Override
    public Object getItem(int position) {
        return m_List.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        TextView        text    = null;
        CustomHolder    holder  = null;
        LinearLayout    layout  = null;
        View            viewRight = null;
        View            viewLeft = null;
        ImageView       imageView = null;
        TextView        name = null;

        // 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 converView가 null인 상태로 들어 옴
        if ( convertView == null ) {
            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_inthe_chat, parent, false);

            layout    = (LinearLayout) convertView.findViewById(R.id.layout);
            text    = (TextView) convertView.findViewById(R.id.text);
            viewRight    = (View) convertView.findViewById(R.id.imageViewright);
            viewLeft    = (View) convertView.findViewById(R.id.imageViewleft);
            imageView   = convertView.findViewById(R.id.image);
            name = convertView.findViewById(R.id.name);

            // 홀더 생성 및 Tag로 등록
            holder = new CustomHolder();
            holder.m_TextView   = text;
            holder.layout = layout;
            holder.viewRight = viewRight;
            holder.viewLeft = viewLeft;
            holder.m_ImageView = imageView;
            holder.m_TextView2 = name;
            convertView.setTag(holder);
        }
        else {
            holder  = (CustomHolder) convertView.getTag();
            text    = holder.m_TextView;
            layout  = holder.layout;
            viewRight = holder.viewRight;
            viewLeft = holder.viewLeft;
            imageView = holder.m_ImageView;
            name = holder.m_TextView2;
        }

        // Text 등록
        if(m_List.get(position).type %2 == 0){//내가쓴 글일 경우 오른쪽으로 붙임
            layout.setGravity(Gravity.RIGHT);
            viewRight.setVisibility(View.GONE);
            viewLeft.setVisibility(View.GONE);
            name.setVisibility(View.GONE);
        }
        else{//상대방이 쓴 글일 경우 왼쪽으로 붙임
            name.setText(m_List.get(position).id);
            layout.setGravity(Gravity.LEFT);
            viewRight.setVisibility(View.GONE);
            viewLeft.setVisibility(View.GONE);
            name.setVisibility(View.VISIBLE);
        }
        final int type = m_List.get(position).type;


        switch (type){
            case 0: case 4://내가 텍스트, 지도로
                text.setBackgroundResource(R.drawable.talk_me);
                text.setText(m_List.get(position).msg);
                imageView.setVisibility(View.GONE);
                text.setVisibility(View.VISIBLE);
                break;
            case 1: case 5://상대방 텍스트, 지도
                text.setText(m_List.get(position).msg);
                imageView.setVisibility(View.GONE);
                text.setBackgroundResource(R.drawable.talk_you);
                text.setVisibility(View.VISIBLE);
                break;
            case 2: case 3://이미지
                text.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Bitmap bm = BitmapFactory.decodeFile("/data/user/0/com.example.myapplication/files/"+m_List.get(position).msg);
                imageView.setImageBitmap(bm);
                break;

            case 6:
                break;
            case 7:
                break;
            case -1:                            // 날짜데이터
                text.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                text.setBackgroundColor(0);
                text.setText(m_List.get(position).date);
                layout.setGravity(Gravity.CENTER);
                viewRight.setVisibility(View.VISIBLE);
                viewLeft.setVisibility(View.VISIBLE);
                name.setVisibility(View.GONE);
                break;
        }

        // 리스트 아이템을 클릭 했을 때 이벤트 발생
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(type<=1)//텍스트, 날짜 클릭햇을때
                    return;
                else if(type <=3){//이미지 클릭했을때
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_normal);
                    dialog.setTitle("Custom Dialog");
                    ImageView iv =  dialog.findViewById(R.id.main_img);
                    try {
                        iv.setImageBitmap(BitmapFactory.decodeStream(context.openFileInput(m_List.get(position).msg)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    dialog.show();
                }
                else if(type <=5){//지도 클릭했을떄
                    Intent intent = new Intent(context, Google_Map.class);
                    String []str = m_List.get(position).msg.substring(5).split(",");
                    intent.putExtra("x",Double.parseDouble(str[0]));
                    intent.putExtra("y",Double.parseDouble(str[1]));
//                    context.startActivity(intent);
                    inthechat.startActivityForResult(intent, 100);
                }
            }
        });

        return convertView;
    }

}
