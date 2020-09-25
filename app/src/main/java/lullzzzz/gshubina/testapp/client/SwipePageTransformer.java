package lullzzzz.gshubina.testapp.client;

import android.view.View;

import androidx.viewpager2.widget.ViewPager2;


public class SwipePageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) {
            view.setAlpha(0f);
            view.setTranslationX(0);
            view.setTranslationZ(-1f);

        } else if (position >= -1 && position <= -0.5) {
            view.setX(-(position + 1) * pageWidth);
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
            view.setTranslationZ(-1f);
            view.setAlpha(1 + position);

        } else if (position >= -0.5 && position <= 0) {
            view.setTranslationX((position + 0.5f));
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
            view.setTranslationZ(0f);
            view.setAlpha(1);

        } else if (position > 0 && position < 0.5) {
            view.setTranslationX((position - 0.5f));
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
            view.setTranslationZ(-1f);
            view.setAlpha(1);

        } else if (position >= 0.5 && position <= 1) {
            view.setX((1 - position) * pageWidth);
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
            view.setTranslationZ(-1f);
            view.setAlpha(1 - position);

        } else if (position > 1) {
            view.setAlpha(0f);
            view.setTranslationZ(-1f);
        }
    }
}
