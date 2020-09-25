// IDataProvideService.aidl
package lullzzzz.gshubina.testapp.service;

import lullzzzz.gshubina.testapp.service.IDataServiceCallback;

// Declare any non-default types here with import statements

oneway interface IDataProvideService {
     void registerCallback(IDataServiceCallback callback);
     void unregisterCallback(IDataServiceCallback callback);
     void requestSpeedData();
     void requestTachometerData();
}
