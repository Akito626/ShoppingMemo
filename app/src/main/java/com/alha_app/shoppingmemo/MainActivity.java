package com.alha_app.shoppingmemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private List<Map<String, String>> mList = null;
    private SimpleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       setListView();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public void setListView(){
        // ListView 用アダプタのリストを生成
        mList = new ArrayList<Map<String, String>>();

        // ListView 用アダプタを生成
        mAdapter = new SimpleAdapter(
                this,
                mList,
                android.R.layout.simple_list_item_2,
                new String [] {"title", "content"},
                new int[] {android.R.id.text1, android.R.id.text2}
        );

        // ListView にアダプターをセット
        ListView list = (ListView)findViewById(R.id.listView);
        list.setAdapter(mAdapter);

        // ListView のアイテム選択イベント
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(
                    AdapterView<?> parent, View view, int pos, long id) {
                // 編集画面に渡すデータをセットし、表示
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("TITLE", mList.get(pos).get("title"));
                intent.putExtra("CONTENT", mList.get(pos).get("content"));
                startActivity(intent);
            }
        });

        // ListView をコンテキストメニューに登録
        registerForContextMenu(list);
        mAdapter.notifyDataSetChanged();
    }

    public void loadFile(){

    }

    public void saveFile(){
        EditText editText = (EditText) findViewById(R.id.titleText);
    }

}