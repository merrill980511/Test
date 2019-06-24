package com.merrill.onlinetest.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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

public class ErrorActivity extends Activity implements OnClickListener, OnItemClickListener {

    private TextView selectOne, selectTwo, selectThree, selectFour;
    private TextView title;//题目栏
    private ImageView imageOne, imageTwo, imageThree, imageFour;
    private RelativeLayout relOne, relTwo, relThree, relFour;
    private TextView subjectTop;
    private TextView submit;
    private int timuMax;//题目的总数量
    private int timuCurrent = 0;//显示当前题目是第几题，0表示第一题
    private String[] selectRight, selected;
    private String timu, daan, tijie;
    private boolean hasCollect = true;//true则collectTable表中有收藏至少一个题目
    private Dialog dialog;//选题框相关
    private GridView gridView1;//选题框相关
    private View select_subject_layout;//选题框相关
    private int[] gridViewItemColor;//选题框相关
    private String[] gridViewItemText;//选题框相关
    private List<Map<String, Object>> gridViewItemList;//选题框相关
    private SimpleAdapter sim_adapter;//选题框相关

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        resetTitlebar();
        //设定进入该activity的动画效果
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        //以下是各种组件的实例
        title = findViewById(R.id.title);
        selectOne = findViewById(R.id.selectOne);
        selectTwo = findViewById(R.id.selectTwo);
        selectThree = findViewById(R.id.selectThree);
        selectFour = findViewById(R.id.selectFour);
        RelativeLayout back = findViewById(R.id.back);
        RelativeLayout subject = findViewById(R.id.subject);
        RelativeLayout collect = findViewById(R.id.remove);
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
        submit = findViewById(R.id.submit);
        RelativeLayout rel_help = findViewById(R.id.rel_help);

        submit.setOnClickListener(this);
        selectOne.setOnClickListener(this);
        selectTwo.setOnClickListener(this);
        selectThree.setOnClickListener(this);
        selectFour.setOnClickListener(this);
        back.setOnClickListener(this);
        subject.setOnClickListener(this);
        collect.setOnClickListener(this);
        forward.setOnClickListener(this);
        rel_help.setOnClickListener(this);
        setTimuMaxandRight();
        initialSelected(timuMax);
        initialGridViewItem(timuMax);
        setSelectTfandgridColorText();

        rel_help.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.baidu.com/s?wd="+timu);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        showTimu();
    }

    //重设标题栏
    private void resetTitlebar() {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.view_comm_titlebar);
        TextView title = findViewById(R.id.titlebar_title);
        TextView right = findViewById(R.id.titlebar_right_text);
        LinearLayout back = (LinearLayout) findViewById(R.id.titlebar_left_layout);
        title.setText("我的错题");
        right.setText("清空");
        right.setOnClickListener(this);
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

    //得到题目总数timuMax和每题的正确答案集合selectRight[]以及每题已答的答案selected
    public void setTimuMaxandRight() {
        //submit.setText("进入了getTimuMaxandRight()");
        Cursor cursor = null;
        try {
            cursor = DBManager.query("errorTable");
            //设定timuMax数值
            timuMax = cursor.getCount();
            //设定sxelectRight数组大小
            selectRight = new String[timuMax];
            //遍历cursor，提取所有正确答案
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
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
        setSelectBgColor();
        Cursor cursor = null;
        try {
            cursor = DBManager.query("errorTable");
            if (cursor.moveToFirst()) {
                cursor.move(timuCurrent);
                timu = cursor.getString(0);
                daan = cursor.getString(1);
            }
            String[] daans = daan.split("\\|");
            title.setText((timuCurrent + 1) + "." + timu);
            selectOne.setText(daans[0]);
            selectTwo.setText(daans[1]);
            selectThree.setText(daans[2]);
            selectFour.setText(daans[3]);
            subjectTop.setText(timuCurrent + 1 + "/" + timuMax);
            //submit.setText("selectRight为"+selectRight[timuCurrent]+"  selected为"+selected[timuCurrent]);
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
                if (selected[timuCurrent] == null && hasCollect) {
                    selected[timuCurrent] = "A";
                    setSelectBgColor();
                }
                break;
            case R.id.selectTwo:
                if (selected[timuCurrent] == null && hasCollect) {
                    selected[timuCurrent] = "B";
                    setSelectBgColor();
                }
                break;
            case R.id.selectThree:
                if (selected[timuCurrent] == null && hasCollect) {
                    selected[timuCurrent] = "C";
                    setSelectBgColor();
                }
                break;
            case R.id.selectFour:
                if (selected[timuCurrent] == null && hasCollect) {
                    selected[timuCurrent] = "D";
                    setSelectBgColor();
                }
                break;

            //选题框
            case R.id.subject:
                dialog = new AlertDialog.Builder(this).create();
                //显示dialog
                dialog.show();
                //实例选题框布局
                select_subject_layout = getLayoutInflater().inflate(R.layout.select_subject, null);
                //把选题框布局装进dialog
                dialog.getWindow().setContentView(select_subject_layout);
                //得到gridview网格组件里面item的组件里的属性list，背景色、下面的数字题号
                getGridViewList();
                //准备好item里面两个组件的id
                String[] from = {"gridViewItemImage2", "gridViewItemText2"};
                //准备好item里面两个组件的实际物理地址int值
                int[] to = {R.id.gridViewItemImage2, R.id.gridViewItemText2};
                //把所有准备好的资料装进适配器
                sim_adapter = new SimpleAdapter(this, gridViewItemList, R.layout.item2_gridview_selectsubject, from, to);
                //把适配器装进gridView里面
                gridView1 = (GridView) select_subject_layout.findViewById(R.id.gridView1);
                gridView1.setAdapter(sim_adapter);
                gridView1.smoothScrollToPosition(getBoundPosition());
                //给gridView装监听器
                gridView1.setOnItemClickListener(this);
                break;

            //取消收藏
            case R.id.remove:
                //题目总数大于1，则删除此题
                if (timuMax > 1) {
                    String[] whereArgs = {timu};
                    DBManager.delete("errorTable", "timu=?", whereArgs);
                    Toast.makeText(this, "取消成功", Toast.LENGTH_SHORT).show();
                    selected = getStringArray(selected, timuCurrent);
                    setTimuMaxandRight();
                    if (timuCurrent > timuMax - 1) {
                        timuCurrent--;
                    } else {
                    }
                    //题目总数只剩一题，则把它变成空题
                } else if (timuMax == 1) {
                    String[] whereArgs = {timu};
                    DBManager.delete("errorTable", "timu=?", whereArgs);
                    finish();
                    Toast.makeText(this, "已清空全部错题记录", Toast.LENGTH_SHORT).show();
                } else {

                }
                showTimu();
                break;
            //清空收藏
            case R.id.titlebar_right_text:
                AlertDialog dialog = new AlertDialog.Builder(this).setMessage("确定要清空吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBManager.delete("errorTable", null, null);
                                finish();
                                Toast.makeText(ErrorActivity.this, "已清空全部错题记录", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("取消", null).create();
                dialog.show();
                break;
            default:
                break;
        }
    }

    //这个方法是我自己写的用来去掉字符串数组中指定位置的元素用的
    public String[] getStringArray(String[] strs, int index) {
        List<String> strlist = new ArrayList<String>();
        int m = 0;
        for (String str : strs) {
            if (m != index) {
                strlist.add(str);
            } else {
            }
            m++;
        }
        strs = new String[strlist.size()];
        strs = strlist.toArray(strs);
        return strs;
    }

    //设定一个int值，使打开选题框时始终把当前的题目为中心
    public int getBoundPosition() {
        if (timuCurrent + 5 > timuMax - 1) {
            return timuMax - 1;
        } else {
        }
        return timuCurrent + 5;
    }

    @Override//gridView中的item的点击监听器
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        // TODO Auto-generated method stub
        timuCurrent = position;
        showTimu();
        dialog.dismiss();
    }

    //设置答题对错结果集合以及gridView的item里面要输入的list
    public void setSelectTfandgridColorText() {
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
        gridViewItemList = new ArrayList<Map<String, Object>>();
        setSelectTfandgridColorText();
        for (int i = 0; i < timuMax; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("gridViewItemImage2", gridViewItemColor[i]);
            map.put("gridViewItemText2", gridViewItemText[i]);
            gridViewItemList.add(map);
        }
        return gridViewItemList;
    }

    //设定题目所有选项正确与否的背景色，onCreate()运行一次，每次showTimu()都要运行一次
    public void setSelectBgColor() {
        if (selected[timuCurrent] == null) {
            setDefaultColor();
        } else {
            setDefaultColor();
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