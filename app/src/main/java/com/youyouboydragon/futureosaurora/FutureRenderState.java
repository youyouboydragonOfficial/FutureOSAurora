package com.youyouboydragon.futureosaurora;

final class FutureRenderState {
    volatile float bass;
    volatile float mid;
    volatile float treble;
    volatile boolean musicActive;
    volatile boolean cinemaActive;
    volatile boolean showHud = true;
    volatile int notificationCount;
    volatile String mediaTitle = "No media";
    volatile String mediaSubtitle = "FutureOS Aurora";

    int targetFrameDelayMs() {
        if (musicActive) {
            return 16;
        }
        if (cinemaActive) {
            return 22;
        }
        return 33;
    }
}
