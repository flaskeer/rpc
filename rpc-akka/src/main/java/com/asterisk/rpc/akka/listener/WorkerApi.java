package com.asterisk.rpc.akka.listener;

/**
 * @author dongh38@ziroom.com
 * @version 1.0.0
 */
public interface WorkerApi {


    Object START = "Start";

    Object Do = "Do";

    class Progress {
        double percent;

        public Progress(double percent) {
            this.percent = percent;
        }

        @Override
        public String toString() {
            return String.format("%s (%s)", getClass().getSimpleName(), percent);
        }
    }
}
