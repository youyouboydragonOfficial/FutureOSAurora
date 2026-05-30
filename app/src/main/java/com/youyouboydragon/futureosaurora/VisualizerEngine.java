package com.youyouboydragon.futureosaurora;

import android.media.audiofx.Visualizer;
import android.util.Log;

final class VisualizerEngine {
    interface Listener {
        void onBands(float bass, float mid, float treble);
        void onVisualizerError(String message);
    }

    private Visualizer visualizer;
    private final Listener listener;

    VisualizerEngine(Listener listener) {
        this.listener = listener;
    }

    void start() {
        stop();
        try {
            visualizer = new Visualizer(0);
            int[] range = Visualizer.getCaptureSizeRange();
            visualizer.setCaptureSize(range[1]);
            int rate = Math.min(Visualizer.getMaxCaptureRate() / 2, 16000);
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                    analyze(fft);
                }
            }, rate, false, true);
            visualizer.setEnabled(true);
        } catch (RuntimeException error) {
            Log.w("FutureOS", "Visualizer unavailable", error);
            listener.onVisualizerError("Visualizer unavailable: " + error.getClass().getSimpleName());
            stop();
        }
    }

    void stop() {
        if (visualizer != null) {
            try {
                visualizer.setEnabled(false);
                visualizer.release();
            } catch (RuntimeException ignored) {
            }
            visualizer = null;
        }
    }

    private void analyze(byte[] fft) {
        float bass = 0f;
        float mid = 0f;
        float treble = 0f;
        int bassBins = 0;
        int midBins = 0;
        int trebleBins = 0;
        for (int i = 2; i + 1 < fft.length; i += 2) {
            float real = fft[i];
            float imag = fft[i + 1];
            float magnitude = (float) Math.sqrt(real * real + imag * imag);
            int bin = i / 2;
            if (bin < 16) {
                bass += magnitude;
                bassBins++;
            } else if (bin < 64) {
                mid += magnitude;
                midBins++;
            } else if (bin < 160) {
                treble += magnitude;
                trebleBins++;
            }
        }
        listener.onBands(normalize(bass, bassBins, 82f), normalize(mid, midBins, 58f), normalize(treble, trebleBins, 40f));
    }

    private static float normalize(float sum, int bins, float scale) {
        if (bins == 0) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, (sum / bins) / scale));
    }
}
