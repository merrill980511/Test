package com.merrill.onlinetest.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamErrorActivity extends Activity implements OnClickListener, OnItemClickListener {

    private TextView selectOne, selectTwo, selectThree, selectFour;
    private ImageView imageOne, imageTwo, imageThree, imageFour;
    private RelativeLayout relOne, relTwo, relThree, relFour;
    private TextView title;
    private TextView subjectTop;
    private TextView answer, rightAnswer, help;

    private int timuMax, timuCurrent = 0;
    private String[] selectRight, selected;
    private Dialog dialog;
    private GridView gridView1;
    private View select_subject_layout;
    private int[] gridViewItemColor;
    private String[] gridViewItemText;
    private List<Map<String, Object>> gridViewItemList;
    private SimpleAdapter sim_adapter;

    private String timu, daan, tijie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_error);
        resetTitleBar();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

        title = findViewById(R.id.title);
        selectOne = findViewById(R.id.selectOne);
        selectTwo = findViewById(R.id.selectTwo);
        selectThree = findViewById(R.id.selectThree);
        selectFour = findViewById(R.id.selectFour);
        rightAnswer = findViewById(R.id.rightAnswer);
        answer = findViewById(R.id.answer);
        help = findViewById(R.id.help);
        RelativeLayout back = findViewById(R.id.back);
        RelativeLayout subject = findViewById(R.id.subject);
        RelativeLayout collect = findViewById(R.id.collect);
        RelativeLayout forward = findViewById(R.id.forward);
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
        subjectTop = findViewById(R.id.tv_subjectTop);

        back.setOnClickListener(this);
        subject.setOnClickListener(this);
        collect.setOnClickListener(this);
        forward.setOnClickListener(this);

        initialTimuMaxandRightselected();
        initialGridViewItem(timuMax);
        setGridColorText();

        showTimu();
    }

    private void resetTitleBar() {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.view_comm_titlebar);
        TextView title = findViewById(R.id.titlebar_title);
        LinearLayout back = findViewById(R.id.titlebar_left_layout);
        title.setText("查看所有错题");
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    //初始化选题框item颜色、选题框item数字
    private void initialGridViewItem(int this_timuMax) {
        gridViewItemColor = new int[this_timuMax];
        gridViewItemText = new String[this_timuMax];
    }

    public boolean hasTheSame() {
        Cursor cursor = null;
        try {
            cursor = DBManager.query("collectTable");
            cursor.moveToFirst();
            if (cursor.getString(0).equals(timu)) {
                return true;
            } else {
            }
            while (cursor.moveToNext()) {
                if (cursor.getString(0).equals(timu)) {
                    return true;
                } else {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    //得到题目总数timuMax和每题的正确答案集合selectRight[]以及读取selected
    public void initialTimuMaxandRightselected() {
        Cursor cursor = null;
        try {
            cursor = DBManager.query("examerrorTable");
            //设定timuMax数值
            timuMax = cursor.getCount();
            //设定sxelectRight数组大小
            selectRight = new String[timuMax];
            selected = new String[timuMax];
            //遍历cursor，提取所有正确答案
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                selectRight[i] = cursor.getString(1).split("\\|")[4];
                selected[i] = cursor.getString(2);
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
            daan = cursor.getString(1);
            tijie = cursor.getString(2);
            String[] daans = daan.split("\\|");
            title.setText((timuCurrent + 1) + "." + timu);
            selectOne.setText(daans[0]);
            selectTwo.setText(daans[1]);
            selectThree.setText(daans[2]);
            selectFour.setText(daans[3]);
            subjectTop.setText(timuCurrent + 1 + "/" + timuMax);

            rightAnswer.setText("正确答案是：" + selectRight[timuCurrent]);
            if (selected[timuCurrent] == null) {
                answer.setText("该题您未解答");
            } else {
                answer.setText("您的答案是：" + selected[timuCurrent]);
            }
            help.setText("【提示：】" + tijie);
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
            //选题框
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
                gridView1.setAdapter(sim_adapter);
                gridView1.smoothScrollToPosition(getBoundPosition());
                gridView1.setOnItemClickListener(this);
                break;
            //收藏题目
            case R.id.collect:
                TextView text = findViewById(R.id.tv_remove);
                if (hasTheSame()) {
                    String[] s = {timu};
                    DBManager.delete("collectTable", "timu=?", s);
                    Toast.makeText(this, "取消收藏", Toast.LENGTH_SHORT).show();
                    text.setText("收藏此题");
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put("timu", timu);
                    cv.put("daan", daan);
                    cv.put("tijie", tijie);
                    DBManager.insert("collectTable", null, cv);
                    Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
                    text.setText("取消收藏");
                }
                break;
            default:
                break;
        }
    }

    public int getBoundPosition() {
        if (timuCurrent + 5 > timuMax - 1) {
            return timuMax - 1;
        }
        return timuCurrent + 5;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        timuCurrent = position;
        showTimu();
        dialog.dismiss();
    }

    public void setGridColorText() {
        for (int i = 0; i < timuMax; i++) {
            if (selected[i] == null) {
                gridViewItemColor[i] = R.color.select_agodefault;
            } else if (selected[i].equals(selectRight[i])) {
                gridViewItemColor[i] = R.color.select_right;
            } else if (selected[i] != selectRight[i]) {
                gridViewItemColor[i] = R.color.select_error;
            }
            gridViewItemText[i] = String.valueOf(i + 1);
        }
    }

    //返回选择题目中item的数据list
    public List<Map<String, Object>> getGridViewList() {
        gridViewItemList = new ArrayList<>();
        for (int i = 0; i < timuMax; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("gridViewItemImage2", gridViewItemColor[i]);
            map.put("gridViewItemText2", gridViewItemText[i]);
            gridViewItemList.add(map);
        }
        return gridViewItemList;
    }

    public void setSelectBgColor() {
        setDefaultColor();
        if (selected[timuCurrent] == null) {

        } else if (selected[timuCurrent].equals("A")) {
            setErrorColor(selectOne, relOne, imageOne);
        } else if (selected[timuCurrent].equals("B")) {
            setErrorColor(selectTwo, relTwo, imageTwo);
        } else if (selected[timuCurrent].equals("C")) {
            setErrorColor(selectThree, relThree, imageThree);
        } else if (selected[timuCurrent].equals("D")) {
            setErrorColor(selectFour, relFour, imageFour);
        }

        if (selectRight[timuCurrent].equals("A")) {
            setRightColor(selectOne, relOne, imageOne);
        } else if (selectRight[timuCurrent].equals("B")) {
            setRightColor(selectTwo, relTwo, imageTwo);
        } else if (selectRight[timuCurrent].equals("C")) {
            setRightColor(selectThree, relThree, imageThree);
        } else {
            setRightColor(selectFour, relFour, imageFour);
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

    private void setErrorColor(TextView a, RelativeLayout b, ImageView c) {
        a.setBackgroundResource(R.color.select_error);
        b.setBackgroundResource(R.color.select_error);
        c.setImageResource(R.drawable.wrong);
    }

    private void setRightColor(TextView a, RelativeLayout b, ImageView c) {
        a.setBackgroundResource(R.color.select_right);
        b.setBackgroundResource(R.color.select_right);
        c.setImageResource(R.drawable.right);
    }
}