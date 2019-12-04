package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {//채팅방목록에 있는 리스트뷰에서 한줄의 객체를 설정하기 위한 어뎁터

    /* 아이템을 세트로 담기 위한 어레이 */
    private ArrayList<MyItem> mItems = new ArrayList<>();
    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public MyItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();
        /* 'listview_custom' Layout을 inflate하여 convertView 참조 획득 */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_custom, parent, false);
        }

        /* 'listview_custom'에 정의된 위젯에 대한 참조 획득 */
        ImageView iv_img =  convertView.findViewById(R.id.iv_img) ;
        final TextView tv_name =  convertView.findViewById(R.id.tv_name) ;
        TextView iv_isRead =  convertView.findViewById(R.id.iv_isRead) ;
        TextView tv_contents =   convertView.findViewById(R.id.tv_contents) ;

        /* 각 리스트에 뿌려줄 아이템을 받아오는데 mMyItem 재활용 */
        MyItem myItem = getItem(position);

        /* 각 위젯에 세팅된 아이템을 뿌려준다 */
        iv_img.setImageDrawable(myItem.getIcon());
        tv_name.setText(myItem.getName());
        if(myItem.getIsRead()==0)
            iv_isRead.setVisibility(View.GONE);
        else
            iv_isRead.setVisibility(View.VISIBLE);

        iv_isRead.setText("     "+myItem.getIsRead()+"     ");
        tv_contents.setText(myItem.getContents());
        tv_name.setWidth(Intro.WIDTH*5/8);//크기
        tv_contents.setWidth(Intro.WIDTH*5/8);//크기

        /* (위젯에 대한 이벤트리스너를 지정하고 싶다면 여기에 작성하면된다..)  */

        return convertView;
    }
    // 아이템 데이터 추가를 위한 함수.
    public void addItem(Drawable img, String name, String contents,int unread, String userid) {

        MyItem mItem = new MyItem();

        /* MyItem에 아이템을 setting한다. */
        mItem.setIcon(img);
        mItem.setName(name);
        mItem.setIsRead(unread);
        mItem.setContents(contents);
        mItem.setuserid(userid);
        /* mItems에 MyItem을 추가한다. */
        mItems.add(mItem);
    }
    public void setUnRead(String id, int unread){
        for(int i=0;i<mItems.size();i++)
            if(mItems.get(i).getuserid().equals(id)) {
                mItems.get(i).setIsRead(mItems.get(i).getIsRead()+unread);
            }
    }
    public void clear(){
        mItems.clear();
    }
}