package lullzzzz.gshubina.testapp.service.simulator;

import android.util.Log;

import java.time.Instant;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SpeedSimulator {
    private final String LOG_TAG = SpeedSimulator.class.getSimpleName();
    private final int DATA_GENERATION_PERIOD_MS = 300;

    Predicate<Double> mFilterFunction;

    public SpeedSimulator(Predicate<Double> filterFunction){
        mFilterFunction = filterFunction;
    }

    private Stream<Long> buildStream() {
        return Stream.generate(s);
    }

    Supplier<Long> s = () -> {
        try {
            Thread.sleep(DATA_GENERATION_PERIOD_MS);
        } catch (InterruptedException e) {
            Log.w(LOG_TAG, e.getMessage());
        }
        return Instant.now().toEpochMilli();
    };

    private Double speedFunction(Long t) {
        return Math.abs(200 * Math.sin(0.0001 * t));
    }

    public Stream<Double> speedStream() {
        return buildStream()
                .map(this::speedFunction)
                .filter(mFilterFunction);
    }
}
