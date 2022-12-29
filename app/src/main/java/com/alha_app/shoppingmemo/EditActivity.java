package com.alha_app.shoppingmemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.room.util.StringUtil;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class EditActivity extends AppCompatActivity {

    private LinearLayout mainLayout;    // メインのレイアウト
    private Button tempbutton;      // 追加ボタンの保存用
    private Button calcButton;      // 計算ボタン
    private TextView totalView;
    private boolean isfirst = true;     // 最初か確認

    // 作成したコンポーネントへのアクセス用
    private LinearLayout[] layouts;
    private EditText[] names;
    private EditText[] prices;
    private deleteButtonListener[] dbListener;
    private TextView[] currencyTexts;

    private int currentNum = 0;     // 現在のレイアウトの数を記録
    private final int maxBox = 20;      // 作成できるレイアウトの最大数

    private String mFileName = "";
    private boolean mNotSave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        // 最大数まで作成
        layouts = new LinearLayout[maxBox];
        names = new EditText[maxBox];
        prices = new EditText[maxBox];
        dbListener = new deleteButtonListener[maxBox];
        currencyTexts = new TextView[maxBox];

        //コンポーネントの取得
        mainLayout = findViewById(R.id.main_layout);
        totalView = findViewById(R.id.total);
        calcButton = findViewById(R.id.calcbutton);

        //MAINからの値を取得
        Intent intent = getIntent();
        // タイトルの取得
        EditText editTitle = findViewById(R.id.titleText);
        if(intent.getStringExtra("title") != null){
            editTitle.setText(intent.getStringExtra("title"));
            mFileName = intent.getStringExtra("filename");
            int size = Integer.parseInt(intent.getStringExtra("size"));
            String nstr[] = new String[size];
            String pstr[] = new String[size];
            intent.getStringArrayListExtra("name").toArray(nstr);
            intent.getStringArrayListExtra("price").toArray(pstr);
            for(int i = 0; i < size; i++) {
                CreateViews();
                names[i].setText(nstr[i]);
                prices[i].setText(pstr[i]);
            }
        }else{
            CreateViews();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        saveFile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                saveFile();
                finish();
                break;
            case R.id.action_delete:
                DialogFragment dialogFragment = new CheckDialog();
                dialogFragment.show(getSupportFragmentManager(), "my_dialog");
                break;
            default:
                break;
        }

        return true;
    }

    public void onCalc(View view){
        Calc();
    }

    // 追加ボタン用のリスナー
    private View.OnClickListener addButtonListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            CreateViews();
        }
    };

    // 削除ボタン用のリスナー
    private class deleteButtonListener implements View.OnClickListener{
        private int id;

        deleteButtonListener(int i){
            super();

            id = i;
        }

        @Override
        public void onClick(View v) {
            mainLayout.removeView(layouts[id]);
            for(int i = id; i < currentNum - 1; i++){
                layouts[i] = layouts[i+1];
                names[i] = names[i+1];
                prices[i] = prices[i+1];
                dbListener[i] = dbListener[i+1];
                dbListener[i].id--;
                currencyTexts[i] = currencyTexts[i+1];
            }
            if(currentNum == 19){
                CreateAddButton();
            }
            currentNum--;
        }
    };

    // 追加ボタンを作成
    public void CreateAddButton(){
        Button addbutton = new Button(this);

        tempbutton = addbutton;        // 配置済みのボタンを保存

        LinearLayout.LayoutParams abuttonlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        addbutton.setText("+");
        addbutton.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        addbutton.setBackgroundColor(Color.WHITE);
        addbutton.setOnClickListener(addButtonListener);

        mainLayout.addView(addbutton, abuttonlp);
    }

    // 入力欄を作成
    public void CreateViews(){
        // ボタンを削除
        if(!isfirst) {
            mainLayout.removeView(tempbutton);
        }else {
            isfirst = false;
        }

        // コンポーネント用のレイアウトの生成
        layouts[currentNum] = new LinearLayout(this);
        layouts[currentNum].setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
        layouts[currentNum].setOrientation(LinearLayout.HORIZONTAL);
        layouts[currentNum].setWeightSum(10);       // 割合の合計値を設定

        //内部の部品
        ImageButton deletebutton = new ImageButton(this);
        names[currentNum] = new EditText(this);     // 名前用
        prices[currentNum] = new EditText(this);     // 数字用
        currencyTexts[currentNum] = new TextView(this);     // 単位

        names[currentNum].setId(currentNum);
        prices[currentNum].setInputType(2);      // 数字のみに制限

        // 最大文字数の設定
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(7);
        prices[currentNum].setFilters(filters);

        //コンポーネントの大きさ設定
        LinearLayout.LayoutParams dbuttonlp = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT/2);
        LinearLayout.LayoutParams namelp = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams numlp = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams textlp = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        //配置割合
        dbuttonlp.weight = 1;
        namelp.weight = 6;
        numlp.weight = 2;
        textlp.weight = 1;

        //ボタンの設定
        dbListener[currentNum] = new deleteButtonListener(currentNum);
        deletebutton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        deletebutton.setImageResource(R.drawable.cross);
        deletebutton.setOnClickListener(dbListener[currentNum]);

        names[currentNum].setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        prices[currentNum].setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        currencyTexts[currentNum].setText(R.string.current);
        currencyTexts[currentNum].setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        currencyTexts[currentNum].setGravity(Gravity.RIGHT);

        // レイアウトに追加
        layouts[currentNum].addView(deletebutton, dbuttonlp);
        layouts[currentNum].addView(names[currentNum], namelp);
        layouts[currentNum].addView(prices[currentNum], numlp);
        layouts[currentNum].addView(currencyTexts[currentNum], 2, textlp);

        mainLayout.addView(layouts[currentNum]);

        currentNum++;

        // 最大数なら追加ボタンを表示しない
        if(currentNum < maxBox - 1) {
            CreateAddButton();
        }
    }

    // 通貨変換用
    public static String printValue(int value, Locale locale){
        NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
        Currency c = Currency.getInstance(locale);
        double d = (double) value / Math.pow(10, c.getDefaultFractionDigits());

        return nf.format(d);
    }

    // 値段計算
    public void Calc() {
        int total = 0;
        SpannableStringBuilder sb;
        boolean isint;
        for (int i = 0; i < currentNum; i++) {
            sb = (SpannableStringBuilder) prices[i].getText();
            isint = checkString(sb.toString());
            if(isint){
                total += Integer.parseInt(sb.toString());
            }else{
                continue;
            }
        }

        totalView.setText(printValue(total, Locale.JAPAN));
    }

    // テキストが数字か確認
    public static boolean checkString(String text) {

        boolean res = true;

        Pattern pattern = Pattern.compile("^[0-9]+$|-[0-9]+$");
        res = pattern.matcher(text).matches();

        return res;
    }

    public void saveFile(){
        // [削除] で画面を閉じるときは、保存しない
        if (mNotSave) {
            return;
        }

        // タイトル、内容を取得
        EditText editTitle = findViewById(R.id.titleText);
        String title = editTitle.getText().toString();
        String contents[] = new String[maxBox];
        String sprices[] = new String[maxBox];

        for(int i = 0; i < currentNum; i++){
            contents[i] = names[i].getText().toString();
            sprices[i] = prices[i].getText().toString();
        }

        // ファイル名を生成  ファイル名 ： yyyyMMdd_HHmmssSSS.txt
        // （既に保存されているファイルは、そのままのファイル名とする）
        if (mFileName.isEmpty()) {
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.JAPAN);
            mFileName = sdf.format(date) + ".txt";
        }

        // 保存
        OutputStream out = null;
        PrintWriter writer = null;
        try{
            out = this.openFileOutput(mFileName, Context.MODE_PRIVATE);
            writer = new PrintWriter(new OutputStreamWriter(out,"UTF-8"));
            // タイトル書き込み
            writer.println(title);
            // 内容書き込み
            for(int i = 0; i < currentNum; i++) {
                writer.println(contents[i]);
                writer.println(sprices[i]);
            }
            writer.close();
            out.close();
        }catch(Exception e){
            Toast.makeText(this, "File save error!", Toast.LENGTH_LONG).show();
        }
    }

    // ファイルを保存せずに削除
    public void removeFile(){
        if (!mFileName.isEmpty()) {
            this.deleteFile(mFileName);
        }
        // 保存せずに、画面を閉じる
        mNotSave = true;
        this.finish();
    }
}
