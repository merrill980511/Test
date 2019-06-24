package com.merrill.onlinetest.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.merrill.onlinetest.R;
import com.merrill.onlinetest.sqlite.DBManager;

public class MainActivity extends Activity implements OnClickListener {
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resetTitleBar();
        overridePendingTransition(R.anim.alpha_scale_rotate, R.anim.push_left_out);

        TextView order = findViewById(R.id.order);
        TextView simulate = findViewById(R.id.simulate);
        TextView question = findViewById(R.id.question);
        LinearLayout collect = findViewById(R.id.collect);
        LinearLayout wrong = findViewById(R.id.wrong);
        LinearLayout history = findViewById(R.id.history);
        order.setOnClickListener(this);
        simulate.setOnClickListener(this);
        collect.setOnClickListener(this);
        wrong.setOnClickListener(this);
        history.setOnClickListener(this);
        question.setOnClickListener(this);

//        this.deleteDatabase(this.getFilesDir().toString() + "onlinetest.db");
        DBManager.openOrCreateDatabase(this.getFilesDir().toString());
    }

    //重设标题栏
    private void resetTitleBar() {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_mainactivity);
        TextView title = findViewById(R.id.titlebar_title);
        title.setText(R.string.app_name);
    }

    //覆写finish()，目的是保证返回有动画效果
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.zoom_exit);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.order:
                startActivity(new Intent(MainActivity.this, OrderActivity.class));
                break;
            case R.id.simulate:
                View layout = getLayoutInflater().inflate(R.layout.enter_simulate, null);
                final Dialog dialog = new Dialog(this);
                dialog.setTitle("温馨提示");
                dialog.show();
                dialog.getWindow().setContentView(layout);
                final EditText et_name = layout.findViewById(R.id.et_name);
                final EditText et_time = layout.findViewById(R.id.et_time);
                final EditText et_number = layout.findViewById(R.id.et_number);
                TextView confirm = layout.findViewById(R.id.confirm);
                TextView cancel = layout.findViewById(R.id.cancel);
                confirm.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(et_name.getText().toString().trim()) ||
                                TextUtils.isEmpty(et_time.getText().toString().trim()) ||
                                TextUtils.isEmpty(et_number.getText().toString().trim())) {
                            Toast.makeText(MainActivity.this, "请先输入测试信息", Toast.LENGTH_SHORT).show();
                        } else {
                            int num1 = Integer.parseInt(et_number.getText().toString().trim());
                            int num2 = DBManager.query("practiceTable").getCount();
                            if (num1 > num2) {
                                Toast.makeText(MainActivity.this, "题数不够", Toast.LENGTH_SHORT).show();
                            } else {
                                ResultActivity.NAME = et_name.getText().toString().trim();
                                String number = et_number.getText().toString().trim();
                                ExamActivity.TIME = et_time.getText().toString().trim();
                                ExamActivity.NUMBER = number;
                                DBManager.insertExamTable(Integer.parseInt(number));
                                startActivity(new Intent(MainActivity.this, ExamActivity.class));
                                Toast.makeText(MainActivity.this, "测试开始", Toast.LENGTH_SHORT).show();
                            }
                        }
                        dialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case R.id.question:
                startActivity(new Intent(this, QuestionActivity.class));
                break;
            case R.id.collect:
                Cursor cursor2 = null;
                try {
                    cursor2 = DBManager.query("collectTable");
                    if (cursor2.getCount() != 0) {
                        startActivity(new Intent(this, CollectActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "您还没有收藏记录", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                }
                break;
            case R.id.wrong:
                Cursor cursor = null;
                try {
                    cursor = DBManager.query("errorTable");
                    if (cursor.getCount() != 0) {
                        startActivity(new Intent(this, ErrorActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "你还没有错题记录", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                break;
            case R.id.history:
                Cursor cursor1 = null;
                try {
                    cursor1 = DBManager.query("historyTable");
                    if (cursor1.getCount() != 0) {
                        startActivity(new Intent(this, HisResultActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "你还没有模拟测试记录", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor1 != null) {
                        cursor1.close();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
