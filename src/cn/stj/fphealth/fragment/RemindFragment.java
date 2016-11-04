
package cn.stj.fphealth.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.stj.fphealth.R;
import cn.stj.fphealth.adapter.RemindListAdapter;
import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.Utils;
import cn.stj.fphealth.base.BasePageFragment;
import cn.stj.fphealth.db.Database;
import cn.stj.fphealth.db.LitepalDatabaseImpl;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.service.StepDetector;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.PreferencesUtils;
import cn.stj.fphealth.views.togglebutton.ToggleButton;
import cn.stj.fphealth.views.togglebutton.ToggleButton.OnToggleChanged;

/**
 * 提醒列表
 * @author hhj@20160804
 */
public class RemindFragment extends BasePageFragment{
    private final static String TAG = "SedentaryRemindFragment";

    private String mTitle;
    private TextView mTitleTextView;
    private ListView mRemindListView;
    private RemindListAdapter mRemindListAdapter;
    private Database mDatabase;
    private int mSelectedPosition;
    private HealthUnbindReceiver mHealthUnbindReceiver;

    public static RemindFragment newInstance(String title) {
        RemindFragment fragment = new RemindFragment();
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
        mDatabase = new LitepalDatabaseImpl();
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.DEVICE_UNBIND_ACTION);  
        intentFilter.addAction(Constants.REFRESH_REMIND_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);  
        mHealthUnbindReceiver = new HealthUnbindReceiver();
        mActivity.registerReceiver(mHealthUnbindReceiver, intentFilter);
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        mTitleTextView = (TextView) view.findViewById(R.id.tv_title);
        mTitleTextView.setText(mTitle);

        mRemindListView = (ListView) view.findViewById(R.id.listview_remind);
        List<RemindInfo> remindInfos = mDatabase.getRemindedInfos();
        mRemindListAdapter = new RemindListAdapter(mActivity, remindInfos);
        mRemindListView.setAdapter(mRemindListAdapter);
        mRemindListView.setOnItemClickListener(new RemindItemClickListener());
        mRemindListView.setOnItemSelectedListener(new RemindItemSelectedListener());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_voice_remind;
    }

    @Override
    public void fetchData() {

    }

    private class RemindItemSelectedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        	mSelectedPosition = position;
        	mRemindListAdapter.setSelectedItemPosition(mSelectedPosition);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    private class RemindItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	RemindInfo remindInfo = mRemindListAdapter.getData(position);
        	playRemindVoice(remindInfo);
        }
    }   
    
    public void playRemindVoice(RemindInfo remindInfo) {
        Utils.playRemindVoice(mActivity, remindInfo);
	}
    
    private class OnOffOnToggleChanged implements OnToggleChanged {

        @Override
        public void onToggle(boolean onoff) {
            String onoffStr = "";
            if (onoff) {
                onoffStr = getResources().getString(R.string.on);
            } else {
                onoffStr = getResources().getString(R.string.off);
            }
            PreferencesUtils.putBoolean(mActivity, Constants.KEY_SEDENTARY_REMIND, onoff);
        }

    }

    private class HealthUnbindReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(mRemindListAdapter != null && mDatabase != null){
                List<RemindInfo> remindInfos = mDatabase.getRemindedInfos();
                mRemindListAdapter.setDatas(remindInfos);
            }
        }
    }
    
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(mHealthUnbindReceiver != null){
            mActivity.unregisterReceiver(mHealthUnbindReceiver);
        }
    }

}
