package lullzzzz.gshubina.testapp.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import lullzzzz.gshubina.testapp.R;
import lullzzzz.gshubina.testapp.client.viewmodel.SpeedometerDataViewModel;
import lullzzzz.gshubina.testapp.client.viewmodel.TachometerDataViewModel;
import lullzzzz.gshubina.testapp.service.GenerateDataService;
import lullzzzz.gshubina.testapp.service.IDataProvideService;
import lullzzzz.gshubina.testapp.service.IDataServiceCallback;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    private ViewPager2 mViewPager;
    private FragmentStateAdapter mPagerAdapter;

    private SpeedometerDataViewModel mSpeedometerDataViewModel;
    private TachometerDataViewModel mTachometerDataViewModel;

    private IDataProvideService mService;
    private Intent mServiceIntent;

    private IDataServiceCallback mDataCallback = new IDataServiceCallback.Stub() {
        @Override
        public void onSpeedometerDataUpdate(double data) {
            runOnUiThread(() -> {
                mSpeedometerDataViewModel.receive(data);
            });
        }

        @Override
        public void onTachometerDataUpdate(double data) {
            runOnUiThread(() -> {
                mTachometerDataViewModel.receive(data);
            });
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Bind to service");
            mService = IDataProvideService.Stub.asInterface(service);
            try {
                mService.registerCallback(mDataCallback);
                mService.requestSpeedData();
                mService.requestTachometerData();
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to complete service request " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Unbind from service");
            mService = null;
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.i(TAG, "Service connection died. Trying to reconnect...");
            bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSpeedometerDataViewModel =
                new ViewModelProvider(this).get(SpeedometerDataViewModel.class);
        mTachometerDataViewModel =
                new ViewModelProvider(this).get(TachometerDataViewModel.class);

        mViewPager = findViewById(R.id.fragments_pager);
        mPagerAdapter = new ViewPagerFragmentStateAdapter(this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setPageTransformer(new SwipePageTransformer());

        // set custom gesture handling
        mViewPager.setUserInputEnabled(false);
        mViewPager.setOnTouchListener(mViewPagerOnTouchListener);

        mServiceIntent = new Intent(getApplicationContext(), GenerateDataService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (mService != null) {
            try {
                mService.unregisterCallback(mDataCallback);
            } catch (RemoteException e) {
                Log.w(TAG, e.getMessage());
            }
            unbindService(mConnection);
        }
        super.onStop();
    }

    private View.OnTouchListener mViewPagerOnTouchListener = new View.OnTouchListener() {
        private boolean mTwoFingerSwipeFlag = false;
        private float mStartX, mStopX;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (event.getPointerCount() != 2) break;
                    mTwoFingerSwipeFlag = true;
                    mStartX = event.getX(0);
                    if (!mViewPager.isFakeDragging()) {
                        mViewPager.beginFakeDrag();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() != 2) {
                        mTwoFingerSwipeFlag = false;
                        break;
                    }
                    if (mTwoFingerSwipeFlag) {
                        mStopX = event.getX(0);
                        mViewPager.fakeDragBy(mStopX - mStartX);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mTwoFingerSwipeFlag = false;
                    mViewPager.endFakeDrag();
                    break;
            }
            return true;
        }
    };
}
