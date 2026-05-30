package com.youyouboydragon.futureosaurora;

import android.content.ComponentName;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class MediaReactiveController {
    interface Listener {
        void onMediaState(String title, String subtitle, boolean playing, boolean cinema);
    }

    private final Context context;
    private final Listener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<String> cinemaPackages = new HashSet<>();
    private boolean running;

    MediaReactiveController(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        cinemaPackages.add("com.google.android.youtube");
        cinemaPackages.add("com.netflix.mediaclient");
        cinemaPackages.add("com.amazon.avod.thirdpartyclient");
        cinemaPackages.add("com.disney.disneyplus");
        cinemaPackages.add("jp.happyon.android");
        cinemaPackages.add("com.google.android.apps.youtube.music");
    }

    void start() {
        running = true;
        poll();
    }

    void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void poll() {
        if (!running) {
            return;
        }
        boolean playing = false;
        boolean cinema = false;
        String title = "No media";
        String subtitle = "FutureOS Aurora";
        try {
            MediaSessionManager manager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
            ComponentName component = new ComponentName(context, FutureNotificationListener.class);
            List<MediaController> controllers = manager.getActiveSessions(component);
            for (MediaController controller : controllers) {
                PlaybackState state = controller.getPlaybackState();
                if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
                    playing = true;
                    String packageName = controller.getPackageName();
                    cinema = cinemaPackages.contains(packageName);
                    MediaMetadata metadata = controller.getMetadata();
                    if (metadata != null) {
                        String mediaTitle = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
                        String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                        title = mediaTitle == null || mediaTitle.isEmpty() ? packageName : mediaTitle;
                        subtitle = artist == null || artist.isEmpty() ? packageName : artist;
                    } else {
                        title = packageName;
                        subtitle = cinema ? "Cinema source" : "Media session";
                    }
                    break;
                }
            }
        } catch (SecurityException ignored) {
            title = "Enable notification access";
            subtitle = "MediaSession needs permission";
        }
        listener.onMediaState(title, subtitle, playing, cinema);
        handler.postDelayed(this::poll, playing ? 1200L : 2600L);
    }
}
