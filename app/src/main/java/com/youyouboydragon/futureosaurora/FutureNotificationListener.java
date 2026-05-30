package com.youyouboydragon.futureosaurora;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public final class FutureNotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        CharSequence title = extras == null ? null : extras.getCharSequence(Notification.EXTRA_TITLE);
        NotificationHub.put(sbn.getKey(), title == null ? sbn.getPackageName() : title.toString());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        NotificationHub.remove(sbn.getKey());
    }
}
