package lullzzzz.gshubina.testapp.client.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import lullzzzz.gshubina.testapp.R;
import lullzzzz.gshubina.testapp.client.view.GaugeView;
import lullzzzz.gshubina.testapp.client.viewmodel.TachometerDataViewModel;

/**
 * Fragment to show tachometer view
 */
public class TachometerFragment extends FullscreenFragment {

    private TachometerDataViewModel tachometerDataViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_tachometer, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GaugeView tachometerView = view.findViewById(R.id.tachometer_view);
        if (tachometerView != null) {
            tachometerDataViewModel = new ViewModelProvider(requireActivity()).get(TachometerDataViewModel.class);
            tachometerDataViewModel.getTachometerData().observe(getViewLifecycleOwner(), value -> tachometerView.setCurrentValue(value));
        }
    }
}
