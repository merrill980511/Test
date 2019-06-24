package com.merrill.onlinetest.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.merrill.onlinetest.R;
import com.merrill.onlinetest.sqlite.DBManager;
import com.merrill.onlinetest.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ExamActivity extends Activity implements OnClickListener, OnItemClickListener {
    private TextView selectOne, selectTwo, selectThree, selectFour;
    private ImageView imageOne, imageTwo, imageThree, imageFour;
    private RelativeLayout relOne, relTwo, relThree, relFour;
    private TextView title;
    private TextView subjectTop;
    private int timuMax, timuCurrent = 0;
    private String[] selectRight, selected, helps;

    public static String TIME, NUMBER;

    private Dialog dialog;
    private GridView gridView1;
    private View select_subject_layout;//选题框相关
    private int[] gridViewItemColor;//选题框相关
    private String[] gridViewItemText;//选题框相关
    private List<Map<String, Object>> gridViewItemList;//选题框相关
    private SimpleAdapter sim_adapter;//选题框相关

    private String timu;
    private int time;
    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

        time = Integer.parseInt(TIME) * 60;

        title = findViewById(R.id.title);
        RelativeLayout back = findViewById(R.id.back);
        RelativeLayout subject = findViewById(R.id.subject);
        RelativeLayout forward = findViewById(R.id.forward);
        subjectTop = findViewById(R.id.tv_subjectTop);

        selectOne = findViewById(R.id.selectOne);
        selectTwo = findViewById(R.id.selectTwo);
        selectThree = findViewById(R.id.selectThree);
        selectFour = findViewById(R.id.selectFour);
        relOne = findViewById(R.id.relOne);
        relTwo = findViewById(R.id.relTwo);
        relThree = findViewById(R.id.relThree);
        relFour = findViewById(R.id.relFour);
        selectOne = findViewById(R.id.selectOne);
        selectTwo = findViewById(R.id.selectTwo);
        selectThree = findViewById(R.id.selectThree);
        selectFour = findViewById(R.id.selectFour);
        imageOne = findViewById(R.id.imageOne);
        imageTwo = findViewById(R.id.imageTwo);
        imageThree = findViewById(R.id.imageThree);
        imageFour = findViewById(R.id.imageFour);

        selectOne.setOnClickListener(this);
        selectTwo.setOnClickListener(this);
        selectThree.setOnClickListener(this);
        selectFour.setOnClickListener(this);
        back.setOnClickListener(this);
        subject.setOnClickListener(this);
        forward.setOnClickListener(this);

        initialTimuMaxandRight();
        initialSelected(timuMax);
        initialGridViewItem(timuMax);
        setGridColorText();

        resetTitlebar();
        showTimu();
    }

    //重设标题栏
    private void resetTitlebar() {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.view_comm_titlebar);
        final TextView title = findViewById(R.id.titlebar_title);
        TextView right = findViewById(R.id.titlebar_right_text);
        LinearLayout back = findViewById(R.id.titlebar_left_layout);
        right.setText("交卷");
        right.setOnClickListener(this);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (this == null) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time--;
                        title.setText("模拟考试" + " " + TimeUtils.secToTime(time));
                        if (time == 0) {
                            cancel();
                            DBManager.insertExamResultTable(selected, selectRight, helps);
                            ResultActivity.TIME = TIME;
                            ResultActivity.NUMBER = NUMBER;
                            startActivity(new Intent(ExamActivity.this, ResultActivity.class));
                            finish();
                        }
                    }
                });
            }
        };
        time = Integer.parseInt(TIME) * 60;
        timer.schedule(task, 0, 1000);
        back.setOnClickListener(this);
    }

    @Override
    public void finish() {
        timer.cancel();
        ResultActivity.RemainingTime = time;
        super.finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog dialog = new AlertDialog.Builder(this).setMessage("确定要退出考试吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton("取消", null).create();
            dialog.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    //初始化答题情况、答题结果
    public void initialSelected(int this_timuMax) {
        selected = new String[this_timuMax];
        for (int i = 0; i < selected.length; i++) {
            selected[i] = null;
        }
    }

    //初始化选题框item颜色、选题框item数字
    private void initialGridViewItem(int this_timuMax) {
        gridViewItemColor = new int[this_timuMax];
        gridViewItemText = new String[this_timuMax];
    }

    public void initialTimuMaxandRight() {
        Cursor cursor = null;
        try {
            cursor = DBManager.query("examTable");
            timuMax = cursor.getCount();
            selectRight = new String[timuMax];
            helps = new String[timuMax];
            cursor.moveToFirst();
            int i = 0;
            selectRight[i] = cursor.getString(1).split("\\|")[4];
            while (cursor.moveToNext()) {
                i++;
                selectRight[i] = cursor.getString(1).split("\\|")[4];
                helps[i] = cursor.getString(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    //显示题目
    public void showTimu() {
        setSelectBgColor();
        Cursor cursor = null;
        try {
            cursor = DBManager.query("practiceTable");
            cursor.moveToPosition(timuCurrent);
            timu = cursor.getString(0);
            String daan = cursor.getString(1);
            String[] daans = daan.split("\\|");
            title.setText((timuCurrent + 1) + "." + timu);
            selectOne.setText(daans[0]);
            selectTwo.setText(daans[1]);
            selectThree.setText(daans[2]);
            selectFour.setText(daans[3]);
            subjectTop.setText(timuCurrent + 1 + "/" + timuMax);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override//答题监听器
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.forward:
                if (timuCurrent < timuMax - 1) {
                    timuCurrent++;
                    showTimu();
                }
                break;
            case R.id.back:
                if (timuCurrent > 0) {
                    timuCurrent--;
                    showTimu();
                }
                break;
            case R.id.selectOne:
                selected[timuCurrent] = "A";
                setSelectBgColor();
                Toast.makeText(this, (timuCurrent + 1) + "题您选择了A", Toast.LENGTH_SHORT).show();
                autoNext();
                break;
            case R.id.selectTwo:
                selected[timuCurrent] = "B";
                setSelectBgColor();
                Toast.makeText(this, (timuCurrent + 1) + "题您选择了B", Toast.LENGTH_SHORT).show();
                autoNext();
                break;
            case R.id.selectThree:
                selected[timuCurrent] = "C";
                setSelectBgColor();
                Toast.makeText(this, (timuCurrent + 1) + "题您选择了C", Toast.LENGTH_SHORT).show();
                autoNext();
                break;
            case R.id.selectFour:
                selected[timuCurrent] = "D";
                setSelectBgColor();
                Toast.makeText(this, (timuCurrent + 1) + "题您选择了D", Toast.LENGTH_SHORT).show();
                autoNext();
                break;
            case R.id.subject:
                dialog = new AlertDialog.Builder(this).create();
                dialog.show();
                select_subject_layout = getLayoutInflater().inflate(R.layout.select_subject, null);
                dialog.getWindow().setContentView(select_subject_layout);
                getGridViewList();
                String[] from = {"gridViewItemImage2", "gridViewItemText2"};
                int[] to = {R.id.gridViewItemImage2, R.id.gridViewItemText2};
                sim_adapter = new SimpleAdapter(this, gridViewItemList, R.layout.item2_gridview_selectsubject, from, to);
                gridView1 = select_subject_layout.findViewById(R.id.gridView1);
                LinearLayout state1, state2;
                state1 = select_subject_layout.findViewById(R.id.state1);
                state2 = select_subject_layout.findViewById(R.id.state2);
                state1.setVisibility(View.GONE);
                state2.setVisibility(View.VISIBLE);
                gridView1.setAdapter(sim_adapter);
                gridView1.smoothScrollToPosition(getBoundPosition());
                gridView1.setOnItemClickListener(this);
                break;
            //交卷
            case R.id.titlebar_right_text:
                AlertDialog dialog = new AlertDialog.Builder(this).setMessage("是否交卷")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBManager.insertExamResultTable(selected, selectRight, helps);
                                ResultActivity.TIME = TIME;
                                ResultActivity.NUMBER = NUMBER;
                                startActivity(new Intent(ExamActivity.this, ResultActivity.class));
                                finish();
                            }
                        }).setNegativeButton("取消", null).create();
                dialog.show();
                break;
            case R.id.titlebar_left_layout:
                AlertDialog dialog1 = new AlertDialog.Builder(this).setMessage("确定要退出考试吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setNegativeButton("取消", null).create();
                dialog1.show();
            default:
                break;
        }
    }

    public void autoNext() {
        if (timuCurrent < timuMax - 1) {
            timuCurrent++;
            showTimu();
        } else {
            Toast.makeText(this, "已经是最后一题了", Toast.LENGTH_SHORT).show();
        }
    }

    public int getBoundPosition() {
        if (timuCurrent + 5 > timuMax - 1) {
            return timuMax - 1;
        } else {
            return timuCurrent + 5;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        timuCurrent = position;
        showTimu();
        dialog.dismiss();
    }

    //已答与未答的区分
    public void setGridColorText() {
        for (int i = 0; i < timuMax; i++) {
            if (selected[i] == null) {
                gridViewItemColor[i] = R.color.select_agodefault;
            } else {
                gridViewItemColor[i] = R.color.select_answered;
            }
            gridViewItemText[i] = String.valueOf(i + 1);
        }
    }

    public List<Map<String, Object>> getGridViewList() {
        gridViewItemList = new ArrayList<>();
        setGridColorText();
        for (int i = 0; i < timuMax; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("gridViewItemImage2", gridViewItemColor[i]);
            map.put("gridViewItemText2", gridViewItemText[i]);
            gridViewItemList.add(map);
        }
        return gridViewItemList;
    }

    public void setSelectBgColor() {
        if (selected[timuCurrent] == null) {
            setDefaultColor();
        } else {
            setDefaultColor();
            if (selected[timuCurrent] == "A") {
                setSelectColor(selectOne, relOne, imageOne);
            } else if (selected[timuCurrent] == "B") {
                setSelectColor(selectTwo, relTwo, imageTwo);
            } else if (selected[timuCurrent] == "C") {
                setSelectColor(selectThree, relThree, imageThree);
            } else {
                setSelectColor(selectFour, relFour, imageFour);
            }
        }
    }

    private void setDefaultColor() {
        selectOne.setBackgroundResource(R.color.select_default);
        selectTwo.setBackgroundResource(R.color.select_default);
        selectThree.setBackgroundResource(R.color.select_default);
        selectFour.setBackgroundResource(R.color.select_default);

        relOne.setBackgroundResource(R.color.select_default);
        relTwo.setBackgroundResource(R.color.select_default);
        relThree.setBackgroundResource(R.color.select_default);
        relFour.setBackgroundResource(R.color.select_default);

        imageOne.setImageResource(R.drawable.defaults);
        imageTwo.setImageResource(R.drawable.defaults);
        imageThree.setImageResource(R.drawable.defaults);
        imageFour.setImageResource(R.drawable.defaults);
    }

    private void setSelectColor(TextView a, RelativeLayout b, ImageView c) {
        a.setBackgroundResource(R.color.select_answered);
        b.setBackgroundResource(R.color.select_answered);
        c.setImageResource(R.drawable.more_select);
    }
}