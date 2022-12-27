package com.alha_app.shoppingmemo;

import android.app.Activity;
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

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class EditActivity extends AppCompatActivity {

    private LinearLayout mainLayout;    // メインのレイアウト
    private Button tempbutton;      // 追加ボタンの保存用
    private Button calcButton;      // 計算ボタン
    private TextView totalView;
    private boolean isfirst = true;     // 最初か確認
    private Spinner spinner;

    // 作成したコンポーネントへのアクセス用
    private LinearLayout[] layouts = new LinearLayout[20];
    private EditText[] editTexts = new EditText[20];
    private deleteButtonListener[] dbListener = new deleteButtonListener[20];
    private TextView[] currencyTexts = new TextView[20];

    /*
    private ArrayList<LinearLayout> layouts = new ArrayList<LinearLayout>();
    private ArrayList<EditText> editTexts = new ArrayList<EditText>();
    private ArrayList<deleteButtonListener> dpListener = new ArrayList<deleteButtonListener>();
    private ArrayList<TextView> currencyTexts = new ArrayList<TextView>();
    */

    private int currentNum = 0;     // 現在のレイアウトの数を記録
    private final int maxBox = 20;      // 作成できるレイアウトの最大数

    private final Locale[] currency = {Locale.JAPAN, Locale.US, Locale.UK, Locale.FRANCE};  // 通貨の設定

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        // スピナーの準備
        spinner = findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.custom_spinner,
                getResources().getStringArray(R.array.list)
        );
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinner.setAdapter(adapter);

        AdapterView.OnItemSelectedListener selectListener = new AdapterView.OnItemSelectedListener(){
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                changeCurrencyText();
            }

            //　アイテムが選択されなかった時
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spinner.setOnItemSelectedListener(selectListener);

        //コンポーネントの取得
        mainLayout = findViewById(R.id.main_layout);
        totalView = findViewById(R.id.total);
        calcButton = findViewById(R.id.calcbutton);

        CreateViews();
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
                editTexts[i] = editTexts[i+1];
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
        EditText name = new EditText(this);     // 名前用
        editTexts[currentNum] = new EditText(this);     // 数字用
        currencyTexts[currentNum] = new TextView(this);     // 単位

        editTexts[currentNum].setInputType(2);      // 数字のみに制限

        // 最大文字数の設定
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(7);
        editTexts[currentNum].setFilters(filters);

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

        name.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        editTexts[currentNum].setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        currencyTexts[currentNum].setText(spinner.getSelectedItem().toString());
        currencyTexts[currentNum].setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        currencyTexts[currentNum].setGravity(Gravity.RIGHT);

        // レイアウトに追加
        layouts[currentNum].addView(deletebutton, dbuttonlp);
        layouts[currentNum].addView(name, namelp);
        layouts[currentNum].addView(editTexts[currentNum], numlp);
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

    // 現在選択されている通貨を取得
    public Locale nowCurrency(){
        int num = 0;

        num = spinner.getSelectedItemPosition();

        return currency[num];
    }

    public void changeCurrencyText(){
        for(int i = 0; i < currentNum; i++){
            currencyTexts[i].setText(spinner.getSelectedItem().toString());
        }
    }

    // 値段計算
    public void Calc() {
        int total = 0;
        SpannableStringBuilder sb;
        boolean isint;
        for (int i = 0; i < currentNum; i++) {
            sb = (SpannableStringBuilder) editTexts[i].getText();
            isint = checkString(sb.toString());
            if(isint){
                total += Integer.parseInt(sb.toString());
            }else{
                continue;
            }
        }

        totalView.setText(printValue(total, nowCurrency()));
    }

    // テキストが数字か確認
    public static boolean checkString(String text) {

        boolean res = true;

        Pattern pattern = Pattern.compile("^[0-9]+$|-[0-9]+$");
        res = pattern.matcher(text).matches();

        return res;
    }

    public void removeAll(){
        EditText editText = findViewById(R.id.titleText);

        isfirst = true;
        mainLayout.removeAllViews();
        currentNum = 0;
        editText.setText("");
    }

    public void save(){

    }
}
