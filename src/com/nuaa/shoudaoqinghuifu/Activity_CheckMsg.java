package com.nuaa.shoudaoqinghuifu;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Activity_CheckMsg extends AppCompatActivity {
    @Bind(R.id.textView_checkmsg_content)
    TextView tv_content;

    @Bind(R.id.textView_checkmsg_sendtime)
    TextView tv_sendtime;

    @Bind(R.id.toolbar_checkmsg)
    Toolbar tb_checkmsg;

    private float startX = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.xml_checkmsg);

        ButterKnife.bind(this);

        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initView() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.red_status));
        }

        setSupportActionBar(tb_checkmsg);
        tb_checkmsg.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity_CheckMsg.this.finish();
                overridePendingTransition(R.anim.scale_stay,
                        R.anim.out_left_right);
            }
        });

        Msg msg = (Msg) getIntent().getSerializableExtra("msg");
        tv_content.setText(msg.content);
        tv_sendtime.setText(msg.sendtime.toString());
        tb_checkmsg.setTitle(msg.name);
    }

    // 按下返回键返回上层
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            this.finish();
            overridePendingTransition(R.anim.scale_stay,
                    R.anim.out_left_right);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 按下
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startX = event.getX();
        }
        // 松开且为从左向右滑
        else if (event.getAction() == MotionEvent.ACTION_UP
                && event.getX() - startX > 200) {

            this.finish();
            overridePendingTransition(R.anim.scale_stay,
                    R.anim.out_left_right);
        }

        return super.onTouchEvent(event);
    }
}
