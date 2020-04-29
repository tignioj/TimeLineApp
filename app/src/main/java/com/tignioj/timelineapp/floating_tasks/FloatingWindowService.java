package com.tignioj.timelineapp.floating_tasks;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class FloatingWindowService extends Service {
    /**
     * 作为Activity和Service的中介
     * 当Activity绑定Service的时候，执行onServiceConnected(ComponentName name, IBinder service)
     * 其中第二个参数可以强转为这个类型，进而调用这个类的方法
     * 由于这个类属于MyService的一个内部类，因此它的实例可以访问到Service的成员
     */
    public class FloatingWindowIBinder extends Binder {
        public void hello() {
            Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_SHORT).show();
        }

        public void startFloating() {
            //显示悬浮窗
//            getSupportFragmentManager().beginTransaction().add(new FloatingWindowFragment(), "tag1").commit();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new FloatingWindowIBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

}
