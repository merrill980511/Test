package com.merrill.onlinetest.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.merrill.onlinetest.R;
import com.merrill.onlinetest.sqlite.DBManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ResultActivity extends Activity {
    private int resultHas = 0, resultNull = 0, resultRight = 0, resultScore = 0, resultWrong = 0, timuMax = 0;
    private TextView yida, weida, dadui, search, score, haoshi;
    private String[] timu, daan;
    private List<String> resultWrongTimu = new ArrayList<>();
    private List<String> resultWrongDaan = new ArrayList<>();
    private List<String> resultRightTimu = new ArrayList<>();
    private List<String> resultRightDaan = new ArrayList<>();
    private List<String> errorTableTimu = new ArrayList<>();//errorTable中的题目
    private List<String> selectedList = new ArrayList<>();
    private String curTime;
    public static String NAME, NUMBER, TIME;
    public static int RemainingTime;
    private String useTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        curTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        setContentView(R.layout.activity_result);
        haoshi =  findViewById(R.id.haoshi);
        useTime = getUseTime(RemainingTime);
        getTimu();
        setResult();
        insertExamerrorTable();
        queryErrorTable();
        insertErrorTable();
        insertHistoryTable();
        TextView textView = findViewById(R.id.tv_content);
        textView.setText("本次测试共"+NUMBER+"题");
        yida = findViewById(R.id.yida);
        weida = findViewById(R.id.weida);
        dadui = findViewById(R.id.dadui);
        score = findViewById(R.id.score);
        search = findViewById(R.id.search);
        yida.setText("已答：" + resultHas);
        weida.setText("未答：" + resultNull);
        dadui.setText("答对：" + resultRight);
        score.setText("得分：" + resultScore);
        haoshi.setText("耗时：" + useTime);
        timeIsUp();
        search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ResultActivity.this, ExamErrorActivity.class));
                finish();
            }
        });
    }

    //如果因考试时间到了而结束，则进行
    private void timeIsUp() {
        if (RemainingTime == 0) {
            score.setText("时间结束！得分：" + resultScore);
        }
    }

    //载入examresultTable，得出result各数值，同时向examerrorTable插入错题
    private void setResult() {
        Cursor cursor = null;
        try {
            cursor = DBManager.query("examresultTable");
            timuMax = cursor.getCount();
            for (int i = 0; i < timuMax; i++) {
                cursor.moveToPosition(i);
                String selected = cursor.getString(0);
                String selectRight = cursor.getString(1);
                if (selected == null) {
                    resultNull++;
                    resultWrongTimu.add(timu[i]);
                    resultWrongDaan.add(daan[i]);
                    selectedList.add(selected);
                } else if (selected.equals(selectRight)) {
                    resultRight++;
                    resultRightTimu.add(timu[i]);
                    resultRightDaan.add(daan[i]);
                } else if (!selected.equals(selectRight)) {
                    resultWrong++;
                    resultWrongTimu.add(timu[i]);
                    resultWrongDaan.add(daan[i]);
                    selectedList.add(selected);
                }
            }
            resultHas = timuMax - resultNull;
            resultScore = resultRight * 5;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //得到examTable的每个题目
    private void getTimu() {
        Cursor cursor = null;
        try {
            cursor = DBManager.query("examTable");
            timu = new String[cursor.getCount()];
            daan = new String[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                timu[i] = cursor.getString(0);
                daan[i] = cursor.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //往examerrorTable中插入错题
    private void insertExamerrorTable() {
        for (int i = 0; i < timuMax - resultRight; i++) {
            ContentValues cValue = new ContentValues();
            cValue.put("timu", resultWrongTimu.get(i));
            cValue.put("daan", resultWrongDaan.get(i));
            cValue.put("selected", selectedList.get(i));
            DBManager.insert("examerrorTable", null, cValue);
        }
    }

    //往errorTable中插入错题
    private void insertErrorTable() {
        //除了相同的错题，不新的错题加进去
        for (int i = 0; i < resultWrongTimu.size(); i++) {
            if (hasSameTimu(resultWrongTimu.get(i))) {

            } else {
                ContentValues cValue = new ContentValues();
                cValue.put("timu", resultWrongTimu.get(i));
                cValue.put("daan", resultWrongDaan.get(i));
                DBManager.insert("errorTable", null, cValue);
            }
        }
        //这次做对了以往的错题，则错题库中这一题可以去掉了
        for (int i = 0; i < resultRightTimu.size(); i++) {
            if (hasSameTimu(resultRightTimu.get(i))) {
                String[] whereArgs = {resultRightTimu.get(i)};
                DBManager.delete("errorTable", "timu=?", whereArgs);
            }
        }
    }

    //返回一个boolean值，判断errorTable中有没有相同的题目
    private boolean hasSameTimu(String thisTimu) {
        for (int i = 0; i < errorTableTimu.size(); i++) {
            if (thisTimu.equals(errorTableTimu.get(i))) {
                return true;
            }
        }
        return false;
    }

    //提取errorTable中的错题
    private void queryErrorTable() {
        Cursor cursor = null;
        try {
            cursor = DBManager.query("errorTable");
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                errorTableTimu.add(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //往historyTable里插入姓名、得分
    private void insertHistoryTable() {
        ContentValues cValue = new ContentValues();
        cValue.put("name", ResultActivity.NAME);
        cValue.put("curtime", curTime);
        cValue.put("usetime", useTime);
        cValue.put("score", resultScore);
        DBManager.insert("historyTable", null, cValue);
    }

    private String getUseTime(int thisTime) {
        int x = Integer.parseInt(TIME) * 60 - thisTime;
        int y = x / 60;
        int z = x - y * 60;
        String str;
        if (y == 0) {
            str = z + "秒";
        } else {
            str = y + "分" + z + "秒";
        }
        return str;
    }


}