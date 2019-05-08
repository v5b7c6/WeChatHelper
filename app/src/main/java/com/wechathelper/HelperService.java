package com.wechathelper;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class HelperService extends AccessibilityService {

    private final String TAG = "HelperService";

    private Handler handler = new Handler() {

    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "eventType:" + event.getEventType());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String className = event.getClassName().toString();
            Log.d(TAG, className);
            switch (className) {
                case "com.tencent.mm.ui.LauncherUI":
                    openRedPacket();
                    break;
                case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI":
                    clickRedPacket();
                    break;
                case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI":
                    performBackClick();
                    break;
            }
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Parcelable data = event.getParcelableData();
            if (data instanceof Notification) {
                Notification notification = (Notification) data;
                Object fn = notification.extras.get(Notification.EXTRA_TITLE);
                Object txt = notification.extras.get(Notification.EXTRA_TEXT);
                if (fn == null || txt == null) {
                    return;
                }
                String text = txt.toString();
                if (text.contains("[微信红包]")) {
                    PendingIntent pendingIntent = notification.contentIntent;
                    try {
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    //遍历获得未打开红包
    private void openRedPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        // 找到领取红包的点击事件
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ap9");
        if (list != null && list.size() > 0) {
            Log.d(TAG, list.toString());
            for (AccessibilityNodeInfo parent : list) {
                List<AccessibilityNodeInfo> rpStatusNode = parent.findAccessibilityNodeInfosByText("已领取");
                List<AccessibilityNodeInfo> rpStatusNode2 = parent.findAccessibilityNodeInfosByText("已被领完");
                if ((rpStatusNode == null || rpStatusNode.size() == 0) && (rpStatusNode2 == null || rpStatusNode2.size() == 0)) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break; // 只领最新的一个红包
                }
            }
        }
    }

    //打开红包
    private void clickRedPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        // 找到领取红包的点击事件
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d02");
        if (list != null && list.size() > 0) {
            Log.d(TAG, list.toString());
            list.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            performBackClick();
        }
    }

    //返回
    private void performBackClick() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            }
        }, 1300L);
        Log.e(TAG, "点击返回");
    }

}
