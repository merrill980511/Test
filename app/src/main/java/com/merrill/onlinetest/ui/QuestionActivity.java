package com.merrill.onlinetest.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
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

public class QuestionActivity extends Activity {

    private SimpleAdapter sim_adapter;
    private List<Map<String, String>> resultItemList = new ArrayList<>();
    private ListView questionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        resetTitleBar();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        getResultItemList();
        String[] from = {"timu"};
        int[] to = {R.id.timu};
        sim_adapter = new SimpleAdapter(this, resultItemList, R.layout.item_listview_question, from, to);
        questionView = findViewById(R.id.questionView);
        questionView.setAdapter(sim_adapter);
        questionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获得选中项的HashMap对象
                HashMap<String, String> map = (HashMap<String, String>) questionView.getItemAtPosition(position);
                String daan = map.get("daan");
                final String timu = map.get("timu");
                final String tijie = map.get("tijie");
                final String daans[] = daan.split("\\|");
                Toast.makeText(getApplicationContext(),
                        "你选择了第" + (position + 1) + "个题目，题目是：" + timu,
                        Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(QuestionActivity.this).setTitle("请选择操作")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("修改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                final View layout = getLayoutInflater().inflate(R.layout.modify_question, null);
                                EditText et_timu = layout.findViewById(R.id.et_timu);
                                et_timu.setText(timu);
                                EditText option1 = layout.findViewById(R.id.et_option1);
                                option1.setText(daans[0]);
                                EditText option2 = layout.findViewById(R.id.et_option2);
                                option2.setText(daans[1]);
                                EditText option3 = layout.findViewById(R.id.et_option3);
                                option3.setText(daans[2]);
                                EditText option4 = layout.findViewById(R.id.et_option4);
                                option4.setText(daans[3]);
                                EditText et_right = layout.findViewById(R.id.et_right);
                                et_right.setText(daans[4]);
                                EditText et_tijie = layout.findViewById(R.id.et_tijie);
                                et_tijie.setText(tijie);
                                final Dialog dialog = new Dialog(QuestionActivity.this);
                                dialog.setTitle("温馨提示");
                                dialog.show();
                                dialog.getWindow().setContentView(layout);
                                TextView confirm = layout.findViewById(R.id.confirm);
                                TextView cancel = layout.findViewById(R.id.cancel);
                                confirm.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ContentValues values = new ContentValues();
                                        EditText et_timu = layout.findViewById(R.id.et_timu);
                                        EditText e1 = layout.findViewById(R.id.et_option1);
                                        EditText e2 = layout.findViewById(R.id.et_option2);
                                        EditText e3 = layout.findViewById(R.id.et_option3);
                                        EditText e4 = layout.findViewById(R.id.et_option4);
                                        EditText right = layout.findViewById(R.id.et_right);
                                        EditText et_tijie = layout.findViewById(R.id.et_tijie);
                                        values.put("timu", et_timu.getText().toString());
                                        values.put("tijie", et_tijie.getText().toString());
                                        values.put("daan", e1.getText().toString() + "|" +
                                                e2.getText().toString() + "|" + e3.getText().toString() + "|" +
                                                e4.getText().toString() + "|" + right.getText().toString());
                                        DBManager.update("practiceTable", values, "timu=?", new String[]{timu});
                                        resultItemList.clear();
                                        getResultItemList();
                                        sim_adapter.notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                });
                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                DBManager.delete("practiceTable", "timu=?", new String[]{timu});
                                resultItemList.clear();
                                getResultItemList();
                                sim_adapter.notifyDataSetChanged();
                            }
                        }).show();
            }
        });
    }

    private void resetTitleBar() {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.view_comm_titlebar);
        TextView title = findViewById(R.id.titlebar_title);
        TextView right = findViewById(R.id.titlebar_right_text);
        LinearLayout back = findViewById(R.id.titlebar_left_layout);
        title.setText("题库管理");
        right.setText("添加");
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View layout = getLayoutInflater().inflate(R.layout.modify_question, null);
                final Dialog dialog = new Dialog(QuestionActivity.this);
                dialog.setTitle("温馨提示");
                dialog.show();
                dialog.getWindow().setContentView(layout);
                TextView confirm = layout.findViewById(R.id.confirm);
                TextView cancel = layout.findViewById(R.id.cancel);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentValues values = new ContentValues();
                        EditText et_timu = layout.findViewById(R.id.et_timu);
                        EditText e1 = layout.findViewById(R.id.et_option1);
                        EditText e2 = layout.findViewById(R.id.et_option2);
                        EditText e3 = layout.findViewById(R.id.et_option3);
                        EditText e4 = layout.findViewById(R.id.et_option4);
                        EditText right = layout.findViewById(R.id.et_right);
                        EditText et_tijie = layout.findViewById(R.id.et_tijie);
                        values.put("timu", et_timu.getText().toString());
                        values.put("tijie", et_tijie.getText().toString());
                        values.put("daan", e1.getText().toString() + "|" +
                                e2.getText().toString() + "|" + e3.getText().toString() + "|" +
                                e4.getText().toString() + "|" + right.getText().toString());
                        DBManager.insert("practiceTable", null, values);
                        resultItemList.clear();
                        getResultItemList();
                        sim_adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //提取并返回选择题目中item的数据list
    private List<Map<String, String>> getResultItemList() {
        Cursor cursor = null;
        try {
            cursor = DBManager.query("practiceTable");
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                Map<String, String> map = new HashMap<>();
                map.put("timu", cursor.getString(0));
                map.put("daan", cursor.getString(1));
                map.put("tijie", cursor.getString(2));
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
