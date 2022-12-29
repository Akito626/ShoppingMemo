package com.alha_app.shoppingmemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        loadFile();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        getMenuInflater().inflate(R.menu.main_context, menu);
    }

    public void loadFile(){
        // アプリの保存フォルダ内のファイル一覧を取得
        String savePath = this.getFilesDir().getPath().toString();
        File[] files = new File(savePath).listFiles();
        // ファイル名の降順でソート
        Arrays.sort (files, Collections.reverseOrder());
        // テキストファイル(*.txt)を取得し、ListView用アダプタのリストにセット

        System.out.println(savePath);
        ArrayList<String> title = new ArrayList<String>();
        ArrayList<ArrayList<String>> name = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> price = new ArrayList<ArrayList<String>>();

        for (int i=0; i<files.length; i++) {
            String fileName = files[i].getName();
            if (files[i].isFile() && fileName.endsWith(".txt")) {
                //　ファイルを読み込み
                try {
                    // ファイルオープン
                    InputStream in = this.openFileInput(fileName);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    char[] buf = new char[(int)files[i].length()];

                    // タイトル（1行目）を読み込み
                    title.add(reader.readLine());
                    // 内容（2行目以降）を読み込み
                    ArrayList<String> nlist = new ArrayList<>();
                    ArrayList<String> plist = new ArrayList<>();

                    // 偶数は名前、奇数は値段を読み込む
                    String str = null;
                    for(int j = 0;; j++){
                        if((str = reader.readLine()) == null) break;
                        System.out.println(str);
                        nlist.add(str);
                        if((str = reader.readLine()) == null) break;
                        System.out.println(str);
                        plist.add(str);
                    }
                    name.add(nlist);
                    price.add(plist);
                    // ファイルクローズ
                    reader.close();
                    in.close();
                } catch (Exception e) {
                    Toast.makeText(this, "File read error!", Toast.LENGTH_LONG).show();
                }
            }
        }

        ArrayList<Map<String, String>> listData = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            Map<String, String> item = new HashMap<>();
            item.put("title", title.get(i));
            item.put("name", "・" + name.get(i).get(0));
            item.put("price", price.get(i).get(0) + "円");
            listData.add(item);
        }

        SimpleAdapter mAdapter = new SimpleAdapter(
                this,
                listData,
                R.layout.list_item,
                new String[] {"title", "name", "price"},
                new int[] {R.id.title, R.id.name, R.id.price}
        );

        // ListViewにデータをセットする
        ListView list = findViewById(R.id.listView);
        list.setAdapter(mAdapter);

        // ListView のアイテム選択イベント
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(
                    AdapterView<?> parent, View view, int pos, long id) {
                // 編集画面に渡すデータをセットし、表示
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("TITLE", listData.get(pos).get("title"));
                intent.putExtra("CONTENT", listData.get(pos).get("content"));
                startActivity(intent);
            }
        });

        // ListView をコンテキストメニューに登録
        registerForContextMenu(list);
        mAdapter.notifyDataSetChanged();
    }


}