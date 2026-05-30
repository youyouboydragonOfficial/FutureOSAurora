package com.youyouboydragon.futureosaurora;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private final FutureRenderState renderState = new FutureRenderState();
    private TextView status;
    private TextView permissionStatus;
    private VisualizerEngine visualizerEngine;
    private MediaReactiveController mediaController;
    private final NotificationHub.Listener notificationListener = count -> {
        renderState.notificationCount = count;
        updateStatus();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        renderState.showHud = true;
        visualizerEngine = new VisualizerEngine(new VisualizerEngine.Listener() {
            @Override
            public void onBands(float bass, float mid, float treble) {
                runOnUiThread(() -> {
                    renderState.bass = lerp(renderState.bass, bass, 0.35f);
                    renderState.mid = lerp(renderState.mid, mid, 0.28f);
                    renderState.treble = lerp(renderState.treble, treble, 0.42f);
                });
            }

            @Override
            public void onVisualizerError(String message) {
                runOnUiThread(() -> status.setText(message));
            }
        });
        mediaController = new MediaReactiveController(this, (title, subtitle, playing, cinema) -> {
            renderState.mediaTitle = title;
            renderState.mediaSubtitle = subtitle;
            renderState.musicActive = playing && !cinema;
            renderState.cinemaActive = playing && cinema;
            if (playing && hasAudioPermission()) {
                visualizerEngine.start();
            } else {
                visualizerEngine.stop();
                renderState.bass *= 0.5f;
                renderState.mid *= 0.5f;
                renderState.treble *= 0.5f;
            }
            updateStatus();
        });
        setContentView(buildUi());
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationHub.addListener(notificationListener);
        mediaController.start();
        renderState.notificationCount = NotificationHub.count();
        updateStatus();
    }

    @Override
    protected void onPause() {
        NotificationHub.removeListener(notificationListener);
        mediaController.stop();
        visualizerEngine.stop();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        updateStatus();
    }

    private View buildUi() {
        FrameLayout root = new FrameLayout(this);
        root.addView(new FutureAuroraView(this, renderState), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(20), dp(20), dp(20), dp(20));
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        panel.setBackgroundColor(Color.argb(112, 4, 8, 14));

        TextView title = new TextView(this);
        title.setText("FutureOS Aurora");
        title.setTextColor(Color.rgb(232, 226, 208));
        title.setTextSize(27);
        title.setGravity(Gravity.CENTER);
        title.setIncludeFontPadding(false);
        title.setPadding(0, 0, 0, dp(10));
        panel.addView(title, fullWidth());

        status = label(15, Color.rgb(190, 230, 236), Gravity.CENTER);
        status.setPadding(0, 0, 0, dp(10));
        panel.addView(status, fullWidth());

        permissionStatus = label(14, Color.rgb(232, 226, 208), Gravity.LEFT);
        permissionStatus.setPadding(dp(12), dp(10), dp(12), dp(10));
        permissionStatus.setBackgroundColor(Color.argb(96, 0, 0, 0));
        panel.addView(permissionStatus, fullWidth());

        panel.addView(button("通知アクセスを開く", v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))), fullWidth());
        panel.addView(button("ライブ壁紙に設定", v -> openWallpaperPicker()), fullWidth());
        panel.addView(button("音声解析を許可", v -> requestAudioPermission()), fullWidth());
        panel.addView(button("オーバーレイ権限を開く", v -> openOverlaySettings()), fullWidth());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        params.setMargins(dp(16), dp(16), dp(16), dp(20));
        root.addView(panel, params);
        return root;
    }

    private TextView label(int sp, int color, int gravity) {
        TextView view = new TextView(this);
        view.setTextColor(color);
        view.setTextSize(sp);
        view.setGravity(gravity);
        view.setLineSpacing(3f, 1.05f);
        return view;
    }

    private Button button(String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(Color.rgb(232, 226, 208));
        button.setBackgroundColor(Color.argb(150, 13, 24, 32));
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout.LayoutParams fullWidth() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(5), 0, dp(5));
        return params;
    }

    private void openWallpaperPicker() {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this, FutureAuroraWallpaperService.class));
        try {
            startActivity(intent);
        } catch (RuntimeException ignored) {
            startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
        }
    }

    private void requestAudioPermission() {
        if (!hasAudioPermission()) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 10);
        } else {
            visualizerEngine.start();
            updateStatus();
        }
    }

    private void openOverlaySettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        try {
            startActivity(intent);
        } catch (RuntimeException ignored) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        }
    }

    private boolean hasAudioPermission() {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void updateStatus() {
        if (status == null || permissionStatus == null) {
            return;
        }
        String mode = renderState.musicActive ? "音楽連動モード" : renderState.cinemaActive ? "シネマモード" : "通常オーロラモード";
        status.setText(mode + "\n" + renderState.mediaTitle);

        permissionStatus.setText(
                mark(isNotificationAccessEnabled()) + " 通知アクセス\n"
                        + mark(hasAudioPermission()) + " 音声解析\n"
                        + mark(Settings.canDrawOverlays(this)) + " オーバーレイ\n"
                        + "通知数: " + renderState.notificationCount);
    }

    private boolean isNotificationAccessEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (TextUtils.isEmpty(enabled)) {
            return false;
        }
        return enabled.toLowerCase().contains(getPackageName().toLowerCase());
    }

    private static String mark(boolean enabled) {
        return enabled ? "[ON] " : "[OFF]";
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * amount;
    }
}
