package lullzzzz.gshubina.testapp.client.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SpeedometerDataViewModel extends ViewModel {
    private final MutableLiveData<Double> mSpeedometerData = new MutableLiveData<>();

    public void receive(Double item) {
        mSpeedometerData.setValue(item);
    }

    public LiveData<Double> getSpeedometerData() {
        return mSpeedometerData;
    }
}
