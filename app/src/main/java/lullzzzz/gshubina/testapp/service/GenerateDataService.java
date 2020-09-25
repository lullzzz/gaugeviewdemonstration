package lullzzzz.gshubina.testapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.Instant;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lullzzzz.gshubina.testapp.R;
import lullzzzz.gshubina.testapp.service.simulator.SpeedSimulator;
import lullzzzz.gshubina.testapp.service.simulator.TachometerSimulator;

public class GenerateDataService extends Service {
    private final String LOG_TAG = GenerateDataService.class.getSimpleName();

    private final int NOTIFICATION_ID = 456784;
    private final String NOTIFICATION_CHANNEL_ID = "SERVICE_FOREGROUND_NOTIFICATION_CHANNEL";
    private final String ACTION_PAUSE = "ACTION_PAUSE";
    private final String ACTION_RESUME = "ACTION_RESUME";
    private final String ACTION_STOP = "ACTION_STOP";

    private NotificationManagerCompat mNotificationManager;
    private NotificationCompat.Builder mForegroundNotificationBuilder;

    private final HandlerThread mSpeedThread = new HandlerThread("SpeedDataHandler");
    private final HandlerThread mTachometerThread = new HandlerThread("TachometerDataHandler");

    private final RemoteCallbackList<IDataServiceCallback> mCallbackList = new RemoteCallbackList<IDataServiceCallback>() {
        @Override
        public void onCallbackDied(IDataServiceCallback callback, Object cookie) {
            Log.w(LOG_TAG, "Client connection is dead: " + callback.asBinder().toString());
            super.onCallbackDied(callback, cookie);
        }
    };
    private boolean mIsStarted;

    private boolean mIsSimulationStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!mIsStarted) {
            mIsStarted = true;
            mSpeedThread.start();
            mTachometerThread.start();
            startForegroundService(new Intent(this, GenerateDataService.class));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            Log.i(LOG_TAG, "Starting service...");
            showNotification();
            mIsSimulationStarted = true;
        } else {
            switch (intent.getAction()) {
                case ACTION_PAUSE:
                    Log.i(LOG_TAG, "Pausing sending data...");
                    mIsSimulationStarted = false;
                    refreshPauseNotification();
                    break;
                case ACTION_RESUME:
                    Log.i(LOG_TAG, "Resuming sending data...");
                    mIsSimulationStarted = true;
                    refreshResumeNotification();
                    break;
                case ACTION_STOP:
                    Log.i(LOG_TAG, "Stop service");
                    stopService();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    private void stopService() {
        mCallbackList.kill();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        mSpeedThread.quitSafely();
        mTachometerThread.quitSafely();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "Client is bind to service: " + intent.getPackage());
        return mBinder;
    }

    private NotificationCompat.Builder makeDefaultNotificationBuilder() {
        Intent stopIntent = new Intent(this, GenerateDataService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(getResources().getString(R.string.notification_content_title))
                .setContentText(getResources().getString(R.string.notification_content_text))
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_speed_24)
                .setCategory(Notification.CATEGORY_SERVICE)
                .addAction(0, getResources().getString(R.string.notification_action_stop), pendingStopIntent);
        return builder;
    }

    private void showNotification() {
        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                getResources().getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getResources().getString(R.string.notification_channel_description));
        notificationManager.createNotificationChannel(channel);

        Intent pauseIntent = new Intent(this, GenerateDataService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

        mForegroundNotificationBuilder = makeDefaultNotificationBuilder();

        Notification foregroundNotification = makeDefaultNotificationBuilder()
                .addAction(0, getResources().getString(R.string.notification_action_pause), pendingPrevIntent)
                .build();

        startForeground(NOTIFICATION_ID, foregroundNotification);
    }

    private void refreshPauseNotification() {
        Intent resumeIntent = new Intent(this, GenerateDataService.class);
        resumeIntent.setAction(ACTION_RESUME);
        PendingIntent pendingResumeIntent = PendingIntent.getService(this, 0, resumeIntent, 0);

        Notification pausedNotification = mForegroundNotificationBuilder
                .addAction(0, getResources().getString(R.string.notification_action_resume), pendingResumeIntent)
                .build();
        mNotificationManager.notify(NOTIFICATION_ID, pausedNotification);
    }

    private void refreshResumeNotification() {
        Intent pauseIntent = new Intent(this, GenerateDataService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pendingPrevIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

        mForegroundNotificationBuilder = makeDefaultNotificationBuilder();

        Notification foregroundNotification = makeDefaultNotificationBuilder()
                .addAction(0, getResources().getString(R.string.notification_action_pause), pendingPrevIntent)
                .build();
        mNotificationManager.notify(NOTIFICATION_ID, foregroundNotification);
    }

    private final IDataProvideService.Stub mBinder = new IDataProvideService.Stub() {
        @Override
        public void registerCallback(IDataServiceCallback callback) throws RemoteException {
            if (callback != null) {
                mCallbackList.register(callback);
            }
        }

        @Override
        public void unregisterCallback(IDataServiceCallback callback) throws RemoteException {
            if (callback != null) {
                mCallbackList.unregister(callback);
            }
        }

        @Override
        public void requestSpeedData() throws RemoteException {
            Handler handler = new Handler(mSpeedThread.getLooper());
            handler.post(() -> {
                new SpeedSimulator((data) -> mIsSimulationStarted).speedStream().forEach((data) -> {
                    sendSpeedData(data);
                });
            });

        }

        @Override
        public void requestTachometerData() throws RemoteException {
            Handler handler = new Handler(mTachometerThread.getLooper());
            handler.post(() -> {
                new TachometerSimulator((data) -> mIsSimulationStarted).tachometerStream().forEach((data) -> {
                    sendTachometerData(data);
                });
            });
        }
    };

    private synchronized int sendSpeedData(double data) {
        final int n = mCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < n; i++) {
                mCallbackList.getBroadcastItem(i).onSpeedometerDataUpdate(data);
            }
        } catch (RemoteException e) {
            // RemoteCallbackList should processed the case, just log it
            Log.w(LOG_TAG, e.getMessage());
        } finally {
            mCallbackList.finishBroadcast();
        }
        return n;
    }

    private synchronized int sendTachometerData(double data) {
        final int n = mCallbackList.beginBroadcast();
        try {
            for (int i = 0; i < n; i++) {
                mCallbackList.getBroadcastItem(i).onTachometerDataUpdate(data);
            }
        } catch (RemoteException e) {
            // RemoteCallbackList should processed the case, just log it
            Log.w(LOG_TAG, e.getMessage());
        } finally {
            mCallbackList.finishBroadcast();
        }
        return n;
    }

}
