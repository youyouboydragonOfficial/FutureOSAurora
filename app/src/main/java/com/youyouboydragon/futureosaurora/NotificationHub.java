package com.youyouboydragon.futureosaurora;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class NotificationHub {
    interface Listener {
        void onNotificationCountChanged(int count);
    }

    private static final Map<String, String> ACTIVE = new HashMap<>();
    private static final List<Listener> LISTENERS = new ArrayList<>();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private NotificationHub() {
    }

    static void put(String key, String title) {
        synchronized (ACTIVE) {
            ACTIVE.put(key, title == null ? "" : title);
        }
        notifyListeners();
    }

    static void remove(String key) {
        synchronized (ACTIVE) {
            ACTIVE.remove(key);
        }
        notifyListeners();
    }

    static int count() {
        synchronized (ACTIVE) {
            return ACTIVE.size();
        }
    }

    static void addListener(Listener listener) {
        synchronized (LISTENERS) {
            LISTENERS.add(listener);
        }
        listener.onNotificationCountChanged(count());
    }

    static void removeListener(Listener listener) {
        synchronized (LISTENERS) {
            LISTENERS.remove(listener);
        }
    }

    private static void notifyListeners() {
        MAIN.post(() -> {
            int count = count();
            List<Listener> copy;
            synchronized (LISTENERS) {
                copy = new ArrayList<>(LISTENERS);
            }
            for (Listener listener : copy) {
                listener.onNotificationCountChanged(count);
            }
        });
    }
}
