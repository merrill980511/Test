package com.merrill.onlinetest.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.merrill.onlinetest.R;
import com.merrill.onlinetest.sqlite.DBManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HisResultActivity extends Activity implements OnClickListener {

    private SimpleAdapter sim_adapter;
    private List<Map<String, String>> resultItemList = new ArrayList<Map<String, String>>();
    private ListView hisListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_his_result);
        resetTitlebar();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        getResultItemList();
        //准备好item里面两个组件的id
        String[] from = {"hisName", "curTime", "useTime", "hisResult"};
        //准备好item里面两个组件的实际物理地址int值
        int[] to = {R.id.hisName, R.id.curTime, R.id.useTime, R.id.hisResult};
        //把所有准备好的资料装进适配器
        sim_adapter = new SimpleAdapter(this, resultItemList, R.layout.item_listview_hisresult, from, to);
        //把适配器装进gridView里面
        hisListView =  findViewById(R.id.hisListView);
        hisListView.setAdapter(sim_adapter);
    }

    //自定义标题栏
    private void resetTitlebar() {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.view_comm_titlebar);
        TextView title =  findViewById(R.id.titlebar_title);
        TextView right =  findViewById(R.id.titlebar_right_text);
        LinearLayout back =  findViewById(R.id.titlebar_left_layout);
        title.setText("历史成绩");
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
    public void onClick(View v) {
        switch (v.getId()) {
            //清空记录
            case R.id.titlebar_right_text:
                AlertDialog dialog = new AlertDialog.Builder(this).setMessage("确定要清空吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBManager.delete("historyTable", null, null);
                                finish();
                                Toast.makeText(HisResultActivity.this, "已清空全部考试结果记录", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("取消", null).create();
                dialog.show();
                break;
            default:
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    //提取并返回选择题目中item的数据list
    private List<Map<String, String>> getResultItemList() {
        Cursor cursor = null;
        try {
            cursor = DBManager.query("historyTable");
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                Map<String, String> map = new HashMap<String, String>();
                map.put("hisName", cursor.getString(0));
                map.put("curTime", cursor.getString(1));
                map.put("useTime", cursor.getString(2));
                map.put("hisResult", cursor.getString(3));
                resultItemList.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return resultItemList;
    }


}