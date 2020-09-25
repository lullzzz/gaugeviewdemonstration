package lullzzzz.gshubina.testapp.client.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TachometerDataViewModel extends ViewModel {
    private final MutableLiveData<Double> mTachometerData = new MutableLiveData<>();

    public void receive(Double item) {
        mTachometerData.setValue(item);
    }

    public LiveData<Double> getTachometerData() {
        return mTachometerData;
    }
}
