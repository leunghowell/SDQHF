package com.nuaa.shoudaoqinghuifu;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.Calendar;
import java.util.Vector;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

public class Activity_Msg extends AppCompatActivity {
    @Bind(R.id.textView_msg_empty)
    TextView tv_empty;

    @Bind(R.id.listView_Msg)
    ListView lv_msg;

    @Bind(R.id.toolbar_msg)
    Toolbar tb_msg;

    @Bind(R.id.navigationView_msg)
    NavigationView nv_msg;

    @Bind(R.id.drawerLayout_msg)
    DrawerLayout dw_msg;

    public static Vector<Msg> vector_msgs = new Vector<>();
    private DBHelper helper = new DBHelper(this, "MsgTbl");
    public static Vector<String> vector_checkedName = new Vector<>();
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeUtil.onSetTheme(this, "msg");

        setContentView(R.layout.xml_msg);

        ButterKnife.bind(this);

        helper.creatTable();
        readDataBase();

        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_items_main, menu);
        return true;
    }

    private void initView() {
        setSupportActionBar(tb_msg);
        tb_msg.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dw_msg.openDrawer(GravityCompat.START);
            }
        });
        tb_msg.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_add:
                        Intent intent = new Intent(Activity_Msg.this, Activity_SendMsg.class);
                        startActivityForResult(intent, Value.ADD_MSG);
                        overridePendingTransition(R.anim.in_right_left, R.anim.scale_stay);
                        break;
                }
                return true;
            }
        });

        nv_msg.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
        nv_msg.setItemIconTintList(null);
        nv_msg.getMenu().getItem(0).setChecked(true);
        nv_msg.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent intent = null;
                dw_msg.closeDrawer(GravityCompat.START);
                switch (menuItem.getItemId()) {
                    case R.id.navItem_msg:
                        intent = null;
                        break;

                    case R.id.navItem_group:
                        intent = new Intent(Activity_Msg.this, Activity_Group.class);
                        Activity_Msg.this.finish();
                        break;

                    case R.id.navItem_memo:
                        intent = new Intent(Activity_Msg.this, Activity_Memo.class);
                        Activity_Msg.this.finish();
                        break;

                    case R.id.navItem_temp:
                        intent = new Intent(Activity_Msg.this, Activity_Temp.class);
                        Activity_Msg.this.finish();
                        break;

                    case R.id.navItem_setting:
                        intent = new Intent(Activity_Msg.this, Activity_Preference.class);
                        Activity_Msg.this.finish();
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                }

                return true;
            }
        });

        lv_msg.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(Activity_Msg.this,
                        Activity_CheckMsg.class);
                intent.putExtra("msg", vector_msgs.get(position));
                intent.putExtra("position", position);
                startActivity(intent);
                overridePendingTransition(R.anim.in_right_left,
                        R.anim.out_right_left);
            }
        });

        lv_msg.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int p, long id) {
                final String[] names = {"添加到备忘录", "删除"};

                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Msg.this);

                builder.setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            // 添加到备忘录
                            case 0:
                                Intent intent = new Intent(Activity_Msg.this,
                                        Activity_AddMemo.class);
                                intent.putExtra("msg", vector_msgs.get(p));
                                startActivity(intent);
                                Activity_Msg.this.finish();
                                overridePendingTransition(R.anim.in_right_left,
                                        R.anim.out_right_left);
                                break;

                            // 删除
                            case 1:
                                final MaterialDialog mDialog = new MaterialDialog(Activity_Msg.this)
                                        .setCanceledOnTouchOutside(true)
                                        .setTitle("删除")
                                        .setMessage("真的要删除该消息吗？");
                                mDialog.setPositiveButton("真的",
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // 设置退出动画
                                                setListViewAnimation();

                                                // 刷新页面
                                                Cursor cursor = helper.query();
                                                cursor.moveToPosition(vector_msgs.size() - p - 1);
                                                helper.delete(cursor.getInt(0));
                                                vector_msgs.remove(p);
                                                setAdapter();

                                                Toast.makeText(Activity_Msg.this,
                                                        "已删除", Toast.LENGTH_SHORT)
                                                        .show();
                                                mDialog.dismiss();
                                            }
                                        })
                                        .setNegativeButton("假的", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mDialog.dismiss();
                                            }
                                        });
                                mDialog.show();

                                break;
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                return true;
            }

        });
    }

    // 设置列表动画
    private void setListViewAnimation() {
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animation.setDuration(500);
        LayoutAnimationController lac = new LayoutAnimationController(animation);
        lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
        lac.setDelay(0.3f);

        lv_msg.setLayoutAnimation(lac);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        helper.close();
    }

    // 从数据库中读取数据
    private void readDataBase() {
        vector_msgs.clear();

        Cursor cursor = helper.query();

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            Msg msg = readDataWithCursor(cursor);

            vector_msgs.add(0, msg);

            cursor.moveToNext();
        }

        setAdapter();
    }

    // 读取一条消息
    private Msg readDataWithCursor(Cursor cursor) {
        String name = cursor.getString(1);
        String content = cursor.getString(2);
        String sendtime = cursor.getString(3);

        String[] sendtimearray = sendtime.split("-");
        MyDate send_time = new MyDate(
                Integer.parseInt(sendtimearray[0]),
                Integer.parseInt(sendtimearray[1]),
                Integer.parseInt(sendtimearray[2]),
                Integer.parseInt(sendtimearray[3]),
                Integer.parseInt(sendtimearray[4]));

        String new_names = "";
        String cname = "";
        String[] group_split = name.split("@@@");
        if (group_split.length == 2) {
            new_names = group_split[0];

            String[] names_split = group_split[1].split(",");
            for (String aNames_split : names_split) {
                if (aNames_split.substring(0, 1).equals("1")) {
                    cname += aNames_split.substring(1) + ",";
                }
            }
        } else {
            String[] names_split = name.split(",");
            for (int i = 0; i < names_split.length; i++) {
                if (i != names_split.length - 1) {
                    new_names += names_split[i].substring(1) + ",";
                } else {
                    new_names += names_split[i].substring(1);
                }

                if (names_split[i].substring(0, 1).equals("1")) {
                    cname += names_split[i].substring(1) + ",";
                }
            }
        }

        vector_checkedName.add(0, cname);

        return new Msg(new_names, content, send_time);
    }

    // 刷新列表
    private void setAdapter() {
        ListAdapter adapter = new ListAdapter();
        lv_msg.setAdapter(adapter);
        setListViewAnimation();

        if (vector_msgs.isEmpty()) {
            tv_empty.setVisibility(View.VISIBLE);
        } else {
            tv_empty.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Value.ADD_MSG:
                if (resultCode == RESULT_OK) {
                    setListViewAnimation();

                    Msg msg = (Msg) data.getSerializableExtra("msg");

                    msg.setName(msg.getName().replace(" ", ""));

                    vector_msgs.add(0, msg);

                    setAdapter();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            // 收起抽屉
            if (dw_msg.isDrawerOpen(GravityCompat.START)) {
                dw_msg.closeDrawer(GravityCompat.START);
            } else if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                SharedPreferences sp = getSharedPreferences("setting", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("launch", "msg");
                editor.apply();

                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return vector_msgs.size();
        }

        @Override
        public Object getItem(int position) {
            return vector_msgs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.xml_msg_item, null);

                holder = new ViewHolder();
                holder.iv_photo = (ImageView) convertView
                        .findViewById(R.id.imageView_msg_item_left);
                holder.tv_content = (TextView) convertView
                        .findViewById(R.id.textView_msg_item_content);
                holder.tv_name = (TextView) convertView
                        .findViewById(R.id.textView_msg_item_name);
                holder.tv_sendtime = (TextView) convertView
                        .findViewById(R.id.textView_msg_item_sendtime);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Msg tmp = vector_msgs.get(position);

            holder.tv_name.setText(tmp.getName().replace(",", ", "));
            holder.tv_content.setText(tmp.getContent());

            Calendar current = Calendar.getInstance();
            if ((current.get(Calendar.YEAR) == tmp.sendtime.getYear())
                    && (current.get(Calendar.MONTH) + 1 == tmp.sendtime.getMonth())
                    && (current.get(Calendar.DAY_OF_MONTH) == tmp.sendtime.getDay())) {
                if (tmp.sendtime.getMinute() < 10) {
                    holder.tv_sendtime.setText(tmp.sendtime.getHour() + ":0" + tmp.sendtime.getMinute());
                } else {
                    holder.tv_sendtime.setText(tmp.sendtime.getHour() + ":" + tmp.sendtime.getMinute());
                }
            } else if (current.get(Calendar.YEAR) != tmp.sendtime.getYear()) {
                holder.tv_sendtime.setText(tmp.sendtime.getYear() + "年" + tmp.sendtime.getMonth() + "月" + tmp.sendtime.getDay() + "日");
            } else {
                holder.tv_sendtime.setText(tmp.sendtime.getMonth() + "月" + tmp.sendtime.getDay() + "日");
            }

            // 设置头像颜色
            GradientDrawable bgShape = (GradientDrawable) holder.iv_photo.getBackground();
            bgShape.setColor(getResources().getColor(Value.colors[position % Value.colors.length]));

            return convertView;
        }
    }

    class ViewHolder {
        ImageView iv_photo;
        TextView tv_content;
        TextView tv_name;
        TextView tv_sendtime;
    }
}
