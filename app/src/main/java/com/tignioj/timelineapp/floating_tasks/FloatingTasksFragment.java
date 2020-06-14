package com.tignioj.timelineapp.floating_tasks;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tignioj.timelineapp.MyViewModel;
import com.tignioj.timelineapp.R;
import com.tignioj.timelineapp.entity.MyTaskPoJo;
import com.tignioj.timelineapp.utils.WindowManagerUtils;

import java.util.List;
import java.util.Set;


public class FloatingTasksFragment extends Fragment {
    private static final int UPDATE_LIST = 0x100;
    private MyViewModel myViewModel;
    private MutableLiveData<List<MyTaskPoJo>> myTaskLiveData;

    private TextView textView;
    private Set<String> timeLineSummary;
    private RecyclerView rcv;
    //数据适配器
    private FloatingTasksAdapter floatingTasksAdapter;
    private View viewInWindowManager;

    public FloatingTasksAdapter getFloatingTasksAdapter() {
        return floatingTasksAdapter;
    }

    public FloatingTasksFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);


        floatingTasksAdapter = new FloatingTasksAdapter(myViewModel, this);

        this.rcv.setAdapter(floatingTasksAdapter);
        this.rcv.setLayoutManager(new LinearLayoutManager(requireActivity()));

        //注意：observe方法观察对象(requireActivity()指向了MainActivity)
        // 当观察对象处于非活跃状态的时候(即非RESUME或者START状态)则失效，解决办法就是直接用 observeForever方法
        myTaskLiveData = myViewModel.getTodayMyTaskLiveDataByCurrentTimeLine();
        myTaskLiveData.observeForever(observer);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }



     Observer<List<MyTaskPoJo>> observer = new Observer<List<MyTaskPoJo>>() {
        @Override
        public void onChanged(List<MyTaskPoJo> myTaskPoJos) {
            floatingTasksAdapter.submitList(myTaskPoJos);
            floatingTasksAdapter.notifyDataSetChanged();

            if (myTaskPoJos.size() == 0) {
                if (viewInWindowManager != null) {
                    viewInWindowManager.setVisibility(View.INVISIBLE);
                }
            } else {
                if (viewInWindowManager != null) {
                    viewInWindowManager.setVisibility(View.VISIBLE);
                }
            }
            Log.d("floatingTasks", "update floating tasks");
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rcv = view.findViewById(R.id.rcv_floating);
        view.setOnTouchListener(new View.OnTouchListener() {
            private WindowManager.LayoutParams updatedParameters = WindowManagerUtils.getFloatingTasksWindowManagerParams();
            int x, y;
            float touchedX, touchedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager wm = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = updatedParameters.x;
                        y = updatedParameters.y;

                        touchedX = event.getRawX();
                        touchedY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (event.getRawX() - touchedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - touchedY));
                        wm.updateViewLayout(v, updatedParameters);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        viewInWindowManager = inflater.inflate(R.layout.fragment_floating_window, container, false);
        if (!myViewModel.isHasTasksFloating()) {
            WindowManager wm = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = WindowManagerUtils.getFloatingTasksWindowManagerParams();
            viewInWindowManager.setVisibility(View.INVISIBLE);
            myViewModel.setHasTasksFloating(true);
            wm.addView(viewInWindowManager, p);
        }
        return viewInWindowManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewInWindowManager.isAttachedToWindow()) {
            myViewModel.setHasTasksFloating(false);
            //移除悬浮窗
            WindowManager wm = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(viewInWindowManager);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    //    /**
//     * 更新悬浮窗的数据
//     */
//    public void refreshTasks() {
//        myViewModel.refreshFloatingTasks();
//        myTaskLiveData = myViewModel.getTodayMyTaskLiveDataByCurrentTimeLine();
//        myTaskLiveData.observeForever(observer);
//    }
}
