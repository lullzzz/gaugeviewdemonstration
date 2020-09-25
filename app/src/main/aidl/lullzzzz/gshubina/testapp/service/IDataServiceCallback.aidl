// IDataServiceCallback.aidl
package lullzzzz.gshubina.testapp.service;

// Declare any non-default types here with import statements

oneway interface IDataServiceCallback {
    void onSpeedometerDataUpdate(double data);
    void onTachometerDataUpdate(double data);
}
