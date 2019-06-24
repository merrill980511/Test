package com.merrill.onlinetest.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
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

public class OrderActivity extends Activity implements OnClickListener, OnItemClickListener {

    private TextView title, type;
    private ImageView imageOne, imageTwo, imageThree, imageFour;
    private TextView selectOne, selectTwo, selectThree, selectFour;
    private RelativeLayout relOne, relTwo, relThree, relFour;
    private TextView answer, rightAnswer, help;

    private TextView right;

    private int timuMax, timuCurrent = 0;
    private String[] selectRight, selected;

    private TextView subjectTop;
    private Dialog dialog;
    private View select_subject_layout;
    private GridView gridView1;
    private SimpleAdapter sim_adapter;
    private List<Map<String, Object>> gridViewItemList;
    private int[] gridViewItemColor;
    private String[] gridViewItemText;

    private String timu, daan, tijie;
    private boolean tijieFlage = false;

    private MediaPlayer player;
    private boolean musicFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        resetTitleBar();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

        title = findViewById(R.id.title);
        selectOne = findViewById(R.id.selectOne);
        selectTwo = findViewById(R.id.selectTwo);
        selectThree = findViewById(R.id.selectThree);
        selectFour = findViewById(R.id.selectFour);

        rightAnswer = findViewById(R.id.rightAnswer);
        answer = findViewById(R.id.answer);
        type = findViewById(R.id.type);
        type.setText("题型:单选");
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
        help = findViewById(R.id.help);
        RelativeLayout rel_help = findViewById(R.id.rel_help);

        selectOne.setOnClickListener(this);
        selectTwo.setOnClickListener(this);
        selectThree.setOnClickListener(this);
        selectFour.setOnClickListener(this);
        back.setOnClickListener(this);
        subject.setOnClickListener(this);
        collect.setOnClickListener(this);
        forward.setOnClickListener(this);
        rel_help.setOnClickListener(this);

        initialTimuMaxandRight();
        initialSelected(timuMax);
        setGridColorText();
        showTimu();
    }

    //重设标题栏
    private void resetTitleBar() {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.view_comm_titlebar);
        TextView title = findViewById(R.id.titlebar_title);
        right = findViewById(R.id.titlebar_right_text);
        LinearLayout back = findViewById(R.id.titlebar_left_layout);
        right.setText("music");
        title.setText("顺序练习");
        right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player == null) {
                    player = MediaPlayer.create(OrderActivity.this, R.raw.music);
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setLooping(true);
                    player.start();
                    right.setText("pause");
                    musicFlag = true;
                } else {
                    if (musicFlag) {
                        player.pause();
                        right.setText("start");
                        musicFlag = !musicFlag;
                    } else {
                        player.start();
                        right.setText("pause");
                        musicFlag = !musicFlag;
                    }
                }
            }
        });
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
        if (player != null){
            player.stop();
        }
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    //初始化答题情况、答题结果
    public void initialSelected(int this_timuMax) {
        selected = new String[this_timuMax];
        for (int i = 0; i < selected.length; i++) {
            selected[i] = null;
        }
    }

    //用来确认收藏表单里面是否已经有这一题了，有相同的返回true值
    public boolean hasTheSame() {
        Cursor cursor = null;
        try {
            cursor = DBManager.query("collectTable");
            cursor.moveToFirst();
            if (cursor.getString(0).equals(timu)) {
                return true;
            }
            while (cursor.moveToNext()) {
                if (cursor.getString(0).equals(timu)) {
                    return true;
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

    //得到题目总数timuMax和每题的正确答案集合selectRight[]
    public void initialTimuMaxandRight() {
        Cursor cursor = null;
        try {
            cursor = DBManager.query("practiceTable");
            timuMax = cursor.getCount();
            selectRight = new String[timuMax];
            cursor.moveToFirst();
            int i = 0;
            selectRight[i] = cursor.getString(1).split("\\|")[4];
            while (cursor.moveToNext()) {
                i++;
                selectRight[i] = cursor.getString(1).split("\\|")[4];
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
        tijieFlage = false;
        setSelectBgColor();
        if (selected[timuCurrent] == null) {
            rightAnswer.setText("");
            answer.setText("");
            help.setText("");
        }
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TextView text = findViewById(R.id.tv_remove);
            if (hasTheSame()) {
                text.setText("取消收藏");
            } else {
                text.setText("收藏此题");
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onClick(View v) {
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
                if (selected[timuCurrent] == null) {
                    selected[timuCurrent] = "A";
                    setSelectBgColor();
                }
                break;
            case R.id.selectTwo:
                if (selected[timuCurrent] == null) {
                    selected[timuCurrent] = "B";
                    setSelectBgColor();
                }
                break;
            case R.id.selectThree:
                if (selected[timuCurrent] == null) {
                    selected[timuCurrent] = "C";
                    setSelectBgColor();
                }
                break;
            case R.id.selectFour:
                if (selected[timuCurrent] == null) {
                    selected[timuCurrent] = "D";
                    setSelectBgColor();
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
            case R.id.rel_help:
                if (!tijieFlage) {
                    help.setText(tijie);
                } else {
                    help.setText("");
                }
                tijieFlage = !tijieFlage;
            default:
                break;
        }
    }

    //设定一个int值，使打开选题框时始终把当前的题目为中心
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

    //设置答题对错结果集合以及gridView的item里面要输入的list
    public void setGridColorText() {
        gridViewItemColor = new int[timuMax];
        gridViewItemText = new String[timuMax];
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
        setGridColorText();
        for (int i = 0; i < timuMax; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("gridViewItemImage2", gridViewItemColor[i]);
            map.put("gridViewItemText2", gridViewItemText[i]);
            gridViewItemList.add(map);
        }
        return gridViewItemList;
    }

    //设定题目所有选项正确与否的背景色，onCreate()运行一次，每次showTimu()都要运行一次
    public void setSelectBgColor() {
        setDefaultColor();
        if (selected[timuCurrent] != null) {
            if (selected[timuCurrent] == "A") {
                setErrorColor(selectOne, relOne, imageOne);
            } else if (selected[timuCurrent] == "B") {
                setErrorColor(selectTwo, relTwo, imageTwo);
            } else if (selected[timuCurrent] == "C") {
                setErrorColor(selectThree, relThree, imageThree);
            } else {
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
            answer.setText("你选择了：" + selected[timuCurrent]);
            rightAnswer.setText("正确答案是：" + selectRight[timuCurrent]);
            help.setText("");
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