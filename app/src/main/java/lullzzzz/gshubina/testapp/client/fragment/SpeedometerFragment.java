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
import lullzzzz.gshubina.testapp.client.viewmodel.SpeedometerDataViewModel;

/**
 * Fragment to show speedometer view
 */
public class SpeedometerFragment extends FullscreenFragment {

    private SpeedometerDataViewModel speedometerDataViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_speedometer, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GaugeView speedometerView = view.findViewById(R.id.speedometer_view);
        if (speedometerView != null) {
            speedometerDataViewModel = new ViewModelProvider(requireActivity()).get(SpeedometerDataViewModel.class);
            speedometerDataViewModel.getSpeedometerData().observe(getViewLifecycleOwner(), value -> speedometerView.setCurrentValue(value));
        }
    }
}
