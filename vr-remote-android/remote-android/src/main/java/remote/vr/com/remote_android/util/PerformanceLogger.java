package remote.vr.com.remote_android.util;

import android.util.Log;

public class PerformanceLogger {

    private static final String TAG = "VR-REMOTE";

    private int counter = 0;
    private long startTime = 0;
    private long stopTime = 0;
    private double maxTime = 0;
    private double minTime = Double.MAX_VALUE;
    private double totalTime = 0;

    private String prefix = "";
    private int nrOfRounds = 1000;


    public PerformanceLogger(int nrOfRounds, String prefix) {
        this.prefix = prefix;
        this.nrOfRounds = 100;
    }

    public void start() {
        startTime = System.nanoTime();
    }

    public void stop() {
        stopTime = System.nanoTime();

        double timeInMs = (stopTime - startTime) / (1000 * 1000);

        counter++;
        totalTime += timeInMs;
        maxTime = Math.max(maxTime, timeInMs);
        minTime = Math.min(minTime, timeInMs);

        if(counter >= nrOfRounds) {
            Log.d(TAG, prefix +
                " - average: " + String.format("%.3f", totalTime / counter) + "ms, " +
                "max: " + String.format("%.3f", maxTime) + "ms, " +
                "min: " + String.format("%.3f", minTime) + "ms");

            maxTime = 0;
            counter = 0;
            totalTime = 0;
        }
    }
}

