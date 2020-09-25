package lullzzzz.gshubina.testapp;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    float mScaleFactor = 1;

    private float calculateNegativeScaleFactor(double value, float factor) {
        if (value == 0 || value >= 1) {
            return factor;
        } else {
            factor /= 10.0f;
            value = value * 10.0f;
            return calculateNegativeScaleFactor(value, factor);
        }
    }

    @Test
    public void negativeScale(){
        assertEquals(1f, calculateNegativeScaleFactor(0f, 1), 0.001f);
        assertEquals(1f, calculateNegativeScaleFactor(8f, 1), 0.001f);
        assertEquals(1f, calculateNegativeScaleFactor(1f, 1), 0.001f);
        assertEquals(0.1f, calculateNegativeScaleFactor(0.8f, 1), 0.001f);
        assertEquals(0.01f, calculateNegativeScaleFactor(0.08f, 1), 0.001f);
        assertEquals(0.001f, calculateNegativeScaleFactor(0.008f, 1), 0.001f);
        assertEquals(0.0001f, calculateNegativeScaleFactor(0.0008f, 1), 0.001f);

    }
}