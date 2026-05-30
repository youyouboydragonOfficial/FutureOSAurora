package com.youyouboydragon.futureosaurora;

import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public final class FutureAuroraWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new AuroraEngine();
    }

    private final class AuroraEngine extends Engine {
        private final FutureRenderState state = new FutureRenderState();
        private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        private final FutureAuroraView renderer;
        private boolean visible;
        private long startMs;

        AuroraEngine() {
            state.showHud = false;
            renderer = new FutureAuroraView(FutureAuroraWallpaperService.this, state);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            startMs = android.os.SystemClock.uptimeMillis();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                drawFrame();
            } else {
                handler.removeCallbacksAndMessages(null);
            }
        }

        @Override
        public void onDestroy() {
            handler.removeCallbacksAndMessages(null);
            super.onDestroy();
        }

        private void drawFrame() {
            SurfaceHolder holder = getSurfaceHolder();
            android.graphics.Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    float time = (android.os.SystemClock.uptimeMillis() - startMs) / 1000f;
                    state.bass = 0.18f + (float) Math.sin(time * 0.8f) * 0.08f;
                    state.mid = 0.16f + (float) Math.sin(time * 0.45f) * 0.05f;
                    state.treble = 0.10f + (float) Math.sin(time * 1.6f) * 0.04f;
                    renderer.drawAurora(canvas, canvas.getWidth(), canvas.getHeight(), time, state);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            handler.removeCallbacksAndMessages(null);
            if (visible) {
                handler.postDelayed(this::drawFrame, state.targetFrameDelayMs());
            }
        }
    }
}
