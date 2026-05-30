package com.youyouboydragon.futureosaurora;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.View;

final class FutureAuroraView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF card = new RectF();
    private final FutureRenderState state;
    private final long startMs = SystemClock.uptimeMillis();
    private boolean running;

    FutureAuroraView(Context context, FutureRenderState state) {
        super(context);
        this.state = state;
        glowPaint.setMaskFilter(new BlurMaskFilter(32f, BlurMaskFilter.Blur.NORMAL));
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        running = true;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        running = false;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAurora(canvas, getWidth(), getHeight(), (SystemClock.uptimeMillis() - startMs) / 1000f, state);
        if (running) {
            postInvalidateDelayed(state.targetFrameDelayMs());
        }
    }

    void drawAurora(Canvas canvas, int width, int height, float time, FutureRenderState renderState) {
        canvas.drawColor(renderState.cinemaActive ? Color.rgb(3, 4, 8) : Color.rgb(5, 7, 13));
        float bass = smooth(renderState.bass);
        float mid = smooth(renderState.mid);
        float treble = smooth(renderState.treble);
        float musicBoost = renderState.musicActive ? 1f : 0.35f;

        paint.setShader(new RadialGradient(
                width * 0.5f,
                height * 0.38f,
                Math.max(width, height) * (0.8f + bass * 0.22f),
                new int[]{
                        argb(118, 0, 212, 255),
                        argb(82, 124, 92, 255),
                        argb(12, 24, 242, 178),
                        Color.TRANSPARENT
                },
                new float[]{0f, 0.38f, 0.68f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);

        drawBand(canvas, width, height, time * 0.45f, 0.24f, argb(115, 0, 212, 255), argb(42, 255, 79, 216), 0.45f + bass * musicBoost);
        drawBand(canvas, width, height, time * -0.32f, 0.48f, argb(86, 24, 242, 178), argb(58, 217, 183, 111), 0.32f + mid * 0.28f);
        drawBand(canvas, width, height, time * 0.72f, 0.68f, argb(52, 124, 92, 255), argb(35, 0, 212, 255), 0.22f + treble * 0.18f);

        if (treble > 0.08f || renderState.musicActive) {
            drawSparkLines(canvas, width, height, time, treble, musicBoost);
        }

        drawFutureCards(canvas, width, height, time, renderState);
        if (renderState.cinemaActive) {
            drawCinemaShade(canvas, width, height);
        }
    }

    private void drawBand(Canvas canvas, int width, int height, float time, float center, int colorA, int colorB, float amplitude) {
        path.reset();
        float baseY = height * center;
        float waveHeight = height * (0.06f + amplitude * 0.08f);
        path.moveTo(0, baseY);
        for (int x = 0; x <= width; x += 20) {
            float nx = x / (float) Math.max(1, width);
            float y = baseY
                    + (float) Math.sin(nx * 8.0f + time * 2.2f) * waveHeight
                    + (float) Math.sin(nx * 17.0f - time * 1.5f) * waveHeight * 0.34f;
            path.lineTo(x, y);
        }
        path.lineTo(width, baseY + height * 0.22f);
        path.lineTo(0, baseY + height * 0.22f);
        path.close();
        paint.setShader(new LinearGradient(0, baseY - waveHeight, width, baseY + waveHeight, colorA, colorB, Shader.TileMode.CLAMP));
        paint.setAlpha(210);
        canvas.drawPath(path, paint);
        paint.setShader(null);
        paint.setAlpha(255);
    }

    private void drawSparkLines(Canvas canvas, int width, int height, float time, float treble, float musicBoost) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f + treble * 4f);
        paint.setColor(argb((int) (26 + 90 * treble * musicBoost), 232, 226, 208));
        for (int i = 0; i < 5; i++) {
            float y = height * (0.18f + i * 0.13f) + (float) Math.sin(time * (1.4f + i * 0.2f)) * 22f;
            canvas.drawLine(width * 0.08f, y, width * 0.92f, y + (float) Math.sin(time + i) * 18f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawFutureCards(Canvas canvas, int width, int height, float time, FutureRenderState renderState) {
        float pulse = 1f + (float) Math.sin(time * 2.0f) * 0.015f + renderState.bass * 0.025f;
        float cardW = width * 0.84f * pulse;
        float cardH = Math.min(height * 0.22f, 190f) * pulse;
        float cx = width * 0.5f;
        float cy = height * 0.73f + (float) Math.sin(time * 1.15f) * 7f;
        card.set(cx - cardW / 2f, cy - cardH / 2f, cx + cardW / 2f, cy + cardH / 2f);

        glowPaint.setColor(argb(renderState.musicActive ? 88 : 44, 0, 212, 255));
        canvas.drawRoundRect(card, 28f, 28f, glowPaint);

        paint.setShader(new LinearGradient(
                card.left,
                card.top,
                card.right,
                card.bottom,
                new int[]{argb(188, 16, 19, 26), argb(120, 9, 25, 38), argb(94, 68, 52, 111)},
                new float[]{0f, 0.58f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(card, 26f, 26f, paint);
        paint.setShader(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(argb(128, 217, 183, 111));
        canvas.drawRoundRect(card, 26f, 26f, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.rgb(232, 226, 208));
        paint.setTextSize(Math.max(18f, width * 0.045f));
        paint.setFakeBoldText(true);
        canvas.drawText(trim(renderState.mediaTitle, 28), card.left + 28f, card.top + 54f, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(Math.max(13f, width * 0.032f));
        paint.setColor(argb(210, 0, 212, 255));
        String status = renderState.musicActive ? "Music reactive mode" : renderState.cinemaActive ? "Cinema immersive mode" : "Balanced aurora mode";
        canvas.drawText(status, card.left + 28f, card.top + 88f, paint);
        paint.setColor(argb(180, 232, 226, 208));
        canvas.drawText("Notifications " + renderState.notificationCount + "  /  " + trim(renderState.mediaSubtitle, 30), card.left + 28f, card.top + 124f, paint);
    }

    private void drawCinemaShade(Canvas canvas, int width, int height) {
        paint.setShader(new LinearGradient(0, 0, 0, height * 0.22f, Color.argb(190, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height * 0.22f, paint);
        paint.setShader(new LinearGradient(0, height, 0, height * 0.78f, Color.argb(190, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawRect(0, height * 0.78f, width, height, paint);
        paint.setShader(null);
    }

    private static float smooth(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static int argb(int a, int r, int g, int b) {
        return Color.argb(Math.max(0, Math.min(255, a)), r, g, b);
    }

    private static String trim(String value, int max) {
        if (value == null || value.length() <= max) {
            return value == null ? "" : value;
        }
        return value.substring(0, max - 1) + "...";
    }
}
