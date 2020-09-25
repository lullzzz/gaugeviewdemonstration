package lullzzzz.gshubina.testapp.client;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import lullzzzz.gshubina.testapp.client.fragment.FullscreenFragment;
import lullzzzz.gshubina.testapp.client.fragment.SpeedometerFragment;
import lullzzzz.gshubina.testapp.client.fragment.TachometerFragment;

public class ViewPagerFragmentStateAdapter extends FragmentStateAdapter {
    private final int FRAGMENTS_NUM = 2;

    public ViewPagerFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        FullscreenFragment fragment;
        if (ViewType.values()[position] == ViewType.TACHOMETER) {
            fragment = new TachometerFragment();
        } else {
            fragment = new SpeedometerFragment();
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return FRAGMENTS_NUM;
    }

    public enum ViewType {
        SPEEDOMETER,
        TACHOMETER
    }
}
