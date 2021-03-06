package com.tignioj.timelineapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.tignioj.timelineapp.service.UpdateService;

public class MainActivity extends AppCompatActivity {

    NavController controller;


    private static final int UPDATE_LIST = 0x100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //显示导航栏的返回按钮
        controller = Navigation.findNavController(this, R.id.fragment);
        NavigationUI.setupActionBarWithNavController(this, controller);


        //开启更新任务的服务
//        Intent service = new Intent(getApplicationContext(), UpdateTasksService.class);
//        startService(service);

        //震动提醒服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, UpdateService.class));
        } else {
            startService(new Intent(this, UpdateService.class));
        }



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    @Override
    public boolean onSupportNavigateUp() {
        //请求隐藏键盘
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.fragment).getWindowToken(), 0);

        if (controller.getCurrentDestination().getId() == R.id.addTimeLineFragment) {
            controller.navigateUp();
        } else if (controller.getCurrentDestination().getId() == R.id.timeLineFragment) {
            finish();
//        } else if (controller.getCurrentDestination().getId() ==  R.id.) {

        } else {
            controller.navigate(R.id.timeLineFragment);
        }
        return super.onSupportNavigateUp();
    }


    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_FLOATING_WINDOW: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    Toast.makeText(getApplicationContext(), "不能悬浮的TimelineApp 是没有灵魂的~", Toast.LENGTH_SHORT).show();
////                    finish();
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request.
//        }
//    }
}
