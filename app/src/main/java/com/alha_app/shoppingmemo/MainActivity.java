package com.alha_app.shoppingmemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
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
import java.io.FilenameFilter;
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
    private ArrayList<Map<String, String>> listData;
    private SimpleAdapter mAdapter;

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
                String savePath = this.getFilesDir().getPath().toString();
                File file = new File(savePath);
                if(file.getFreeSpace() < 1000000000l){  // 容量が一定以下なら作成できない
                    DialogFragment dialogFragment = new NoSpaceDialog();
                    dialogFragment.show(getSupportFragmentManager(), "my_dialog");
                    break;
                }
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
                break;
            case R.id.action_delete:
                DialogFragment dialogFragment = new RemoveFileDialog();
                dialogFragment.show(getSupportFragmentManager(), "my_dialog");
                break;
            default:
                break;
        }
        return true;
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

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        // アプリの保存フォルダ内のファイル一覧を取得
        String savePath = this.getFilesDir().getPath().toString();
        File[] files = new File(savePath).listFiles();
        // ファイル名の降順でソート
        Arrays.sort (files, Collections.reverseOrder());

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        System.out.println(listData.get(info.position).get("filename"));
        switch (item.getItemId()) {
            case R.id.context_del:
                // ファイル削除
                this.deleteFile(files[info.position].getName());
                // リストからアイテム処理
                listData.remove(info.position);
                // ListView のデータ変更を表示に反映
                mAdapter.notifyDataSetChanged();
                break;
      }

        String message = "ファイルを削除しました";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        return true;
    }

    public void loadFile(){
        FilenameFilter filter = new FilenameFilter(){
            public boolean accept(File file, String str){

                //指定文字列でフィルタする
                if(str.indexOf(".txt") != -1) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        // アプリの保存フォルダ内のファイル一覧を取得
        String savePath = this.getFilesDir().getPath().toString();
        File[] files = new File(savePath).listFiles(filter);

        // ファイル名の降順でソート
        Arrays.sort (files, Collections.reverseOrder());

        ArrayList<String> title = new ArrayList<String>();
        ArrayList<ArrayList<String>> name = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> price = new ArrayList<ArrayList<String>>();

        for (int i=0; i<files.length; i++) {
            String fileName = files[i].getName();
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
                        nlist.add(str);
                        if((str = reader.readLine()) == null) break;
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

        listData = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            Map<String, String> item = new HashMap<>();
            item.put("title", title.get(i));
            item.put("name", "・" + name.get(i).get(0));
            item.put("price", price.get(i).get(0) + "円");
            listData.add(item);
        }

        mAdapter = new SimpleAdapter(
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
                System.out.println("書き込み：" + files[pos].getName());
                intent.putExtra("filename", files[pos].getName());
                intent.putExtra("size", String.valueOf(name.get(pos).size()));
                intent.putExtra("title", title.get(pos));
                intent.putStringArrayListExtra("name", name.get(pos));
                intent.putStringArrayListExtra("price", price.get(pos));
                startActivity(intent);
            }
        });

        // ListView をコンテキストメニューに登録
        registerForContextMenu(list);
        mAdapter.notifyDataSetChanged();
    }

    public void removeallFiles(){
        // アプリの保存フォルダ内のファイル一覧を取得
        String savePath = this.getFilesDir().getPath().toString();
        File[] files = new File(savePath).listFiles();

        for(int i = 0; i < files.length; i++){
            // ファイル削除
            this.deleteFile(files[i].getName());
            // リストからアイテム処理
        }
        listData.removeAll(listData);
        // ListView のデータ変更を表示に反映
        mAdapter.notifyDataSetChanged();
    }
}