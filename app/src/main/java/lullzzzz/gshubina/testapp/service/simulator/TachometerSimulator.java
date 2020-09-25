package lullzzzz.gshubina.testapp.service.simulator;

import android.util.Log;

import java.time.Instant;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TachometerSimulator {
    private final String LOG_TAG = TachometerSimulator.class.getSimpleName();
    private final int DATA_GENERATION_PERIOD_MS = 300;

    Predicate<Double> mFilterFunction;

    public TachometerSimulator(Predicate<Double> filterFunction){
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

    private Double tachometerFunction(Long t) {
        return Math.abs((10000 * Math.sin(0.0001 * t)));
    }

    public Stream<Double> tachometerStream() {
        return buildStream()
                .map(this::tachometerFunction)
                .filter(mFilterFunction);
    }
}
