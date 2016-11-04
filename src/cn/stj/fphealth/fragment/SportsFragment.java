
package cn.stj.fphealth.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import cn.stj.fphealth.R;
import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.FPHealthApplication;
import cn.stj.fphealth.base.BasePageFragment;
import cn.stj.fphealth.service.StepDetector;
import cn.stj.fphealth.service.StepService;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.PreferencesUtils;
import cn.stj.fphealth.views.StepCircleBar;

import java.math.BigDecimal;

/**
 * @author hhj@20160804
 */
public class SportsFragment extends BasePageFragment implements StepDetector.StepDetectorListener {
    private final static String TAG = "SportsFragment";

    private String mTitle;
    private TextView mTitleTextView;
    private int mTotalStep = 0;
    private int mStepLength = 50;
    private int mWeight;
    private Double mDistance = 0.0;
    private Double mCalories = 0.0;
    private StepCircleBar mStepCircleBar;
    private HealthUnbindReceiver mHealthUnbindReceiver;

    public static SportsFragment newInstance(String title) {
        SportsFragment fragment = new SportsFragment();
        Bundle args = new Bundle();
        args.putString(KEY_FRAGMENT_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(KEY_FRAGMENT_TITLE);
        }
        mWeight = PreferencesUtils.getInt(mActivity, Constants.DEVICE_PARAM.WEIGHT, 70);
        StepService.CURRENT_SETP = PreferencesUtils.getInt(mActivity, Constants.KEY_TOTAL_STEP, 0);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.DEVICE_UNBIND_ACTION);  
        intentFilter.addAction(Constants.CLEAR_STEP_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);  
        mHealthUnbindReceiver = new HealthUnbindReceiver();
        mActivity.registerReceiver(mHealthUnbindReceiver, intentFilter);
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        mStepCircleBar = (StepCircleBar) view.findViewById(R.id.circlebar_progress_pedometer);
        mStepCircleBar.setMax(Constants.TARGET_STEP_NUM);
        mStepCircleBar.setProgress(StepService.CURRENT_SETP, 1);
        mStepCircleBar.startCustomAnimation();
        refreshUI();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void refreshUI() {
        countDistance();
        countStep();
        LogUtil.i("debug", "=====SportsFragment===========refreshUI====mTotalStep:" + mTotalStep);
        if (mStepCircleBar != null) {
            mStepCircleBar.setProgress(mTotalStep, 1);
        }
        PreferencesUtils.putInt(FPHealthApplication.getInstance(), Constants.KEY_TOTAL_STEP, mTotalStep);
        //保存计步当前时间，重新开机后比较开机时间进行判断是否需要清零步数
        PreferencesUtils.putLong(FPHealthApplication.getInstance(), Constants.KEY_STEP_TIME, System.currentTimeMillis());
    };

    /**
     * 计算行走的距离
     */
    private void countDistance() {
        if (StepService.CURRENT_SETP % 2 == 0) {
            mDistance = (StepService.CURRENT_SETP / 2) * 3 * mStepLength * 0.01 * 0.001;
        } else {
            mDistance = ((StepService.CURRENT_SETP / 2) * 3 + 1) * mStepLength * 0.01 * 0.001;
        }
    }

    /**
     * 实际的步数
     */
    private void countStep() {
        if (StepService.CURRENT_SETP % 2 == 0) {
            mTotalStep = StepService.CURRENT_SETP;
        } else {
            mTotalStep = StepService.CURRENT_SETP + 1;
        }

        mTotalStep = StepService.CURRENT_SETP;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sports;
    }

    @Override
    public void fetchData() {

    }

    public double doubleFormat(double data) {
        BigDecimal bigDecimal = new BigDecimal(data);
        double f = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mHealthUnbindReceiver != null){
            mActivity.unregisterReceiver(mHealthUnbindReceiver);
        }
    }

    @Override
    public void stepDetectCallBack() {
        refreshUI();
    }
    
    private class HealthUnbindReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            StepService.CURRENT_SETP = 0;
            refreshUI();
            if(Constants.CLEAR_STEP_ACTION.equals(intent.getAction())){
                //再重新绑定后，需要清零数据，需要重置中间文字内容以及扇形弧度
                mStepCircleBar.reset();
            }
        } 
    }
    
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // TODO Auto-generated method stub
        super.setUserVisibleHint(isVisibleToUser);
    }
}
