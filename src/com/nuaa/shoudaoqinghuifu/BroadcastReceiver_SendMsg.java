package com.nuaa.shoudaoqinghuifu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class BroadcastReceiver_SendMsg extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 定时发送短信
        if (Value.ACTION_SENDMSG.equals(intent.getAction())) {
            SmsManager smsManager = SmsManager.getDefault();

            Msg msg = (Msg) intent.getSerializableExtra("msg");
            ArrayList<String> phones = intent.getStringArrayListExtra("phones");

            // 把短信进行拆分
            ArrayList<String> contents = smsManager
                    .divideMessage(msg.content + "【来自“收到请回复”客户端】");

            // 发送短信
            for (int i = 0; i < phones.size(); i++) {
                // 参数（要发送的号码，信息中心的号码Null，内容，，)
                smsManager.sendMultipartTextMessage(phones.get(i), null, contents, null, null);
            }
        }
    }
}
