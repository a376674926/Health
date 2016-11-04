
package cn.stj.fphealth.activity;

import android.R.integer;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

import cn.stj.fphealth.R;
import cn.stj.fphealth.adapter.MainFragmentPagerAdapter;
import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.FPHealthApplication;
import cn.stj.fphealth.app.Utils;
import cn.stj.fphealth.base.BaseActivity;
import cn.stj.fphealth.db.Database;
import cn.stj.fphealth.db.LitepalDatabaseImpl;
import cn.stj.fphealth.entity.HeartRate;
import cn.stj.fphealth.fragment.BloodPressureFragment;
import cn.stj.fphealth.fragment.HeartRateFragment;
import cn.stj.fphealth.fragment.HeartRateFragment.HeartRateListener;
import cn.stj.fphealth.fragment.RemindFragment;
import cn.stj.fphealth.fragment.SportsFragment;
import cn.stj.fphealth.model.HealthMinaModelImpl;
import cn.stj.fphealth.model.HealthModel;
import cn.stj.fphealth.service.BootService;
import cn.stj.fphealth.service.HealthListener;
import cn.stj.fphealth.service.HealthService;
import cn.stj.fphealth.service.HeartbeatService;
import cn.stj.fphealth.service.LocationUploadService;
import cn.stj.fphealth.service.StepDetector;
import cn.stj.fphealth.service.StepService;
import cn.stj.fphealth.tcp.mina.MinaClient;
import cn.stj.fphealth.tcp.mina.TcpProtocol;
import cn.stj.fphealth.util.FileUtil;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.NetworkUtil;
import cn.stj.fphealth.util.PermissionsChecker;
import cn.stj.fphealth.util.PreferencesUtils;
import cn.stj.fphealth.views.DotsView;
import cn.stj.fphealth.views.dialog.DeviceBindDialog;
import cn.stj.fphealth.views.dialog.QrcodeDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hhj@20160804
 */
public class HealthMainActivity extends BaseActivity implements HeartRateListener,
        DeviceBindDialog.OnDeviceBindDialogClickListener, QrcodeDialog.OnQrcodeDialogClickListener {

    private static final String TAG = "HealthMainActivity";
    private static final String TAG_DEVICE_BIND = "tag_device_bind";
    private static final String TAG_DEVICE_QRCODE = "tag_device_qrcode";
    private static final int WHAT_QRCODE = 100;
    private ViewPager mHealthViewPager;
    private DotsView mDotsView;
    private MainFragmentPagerAdapter mFragmentPagerAdapter;
    private int mCurrentPosition;
    private Context mContext;
    private boolean mIsServiceStarted = false;
    private boolean mIsServiceBinded = false;
    private boolean mIsStepServiceStarted = false;
    private boolean mIsStepServiceBinded = false;
    private HealthService mService = null;
    private HeartRateFragment mHeartRateFragment;
    private SportsFragment mSportsFragment;
    private RemindFragment mSedentaryRemindFragment;
    private BloodPressureFragment mBloodPressureFragment;
    private FragmentManager mFragmentManager;
    private StepService mStepService = null;
//    private boolean mHasQrcodeImg = true;
    private QrcodeDialog mQrcodeDialog;
    private boolean mIsDeviceBind;
    private AlarmManager mAlarmManager;
    private KeyguardLock mLock;
    private boolean mIsOneKeyOpen;
    private HealthReceiver mHealthReceiver;
    private HealthModel mHealthModel;
    private int mDeviceBindStatus;
    private Database mDatabase;
    private boolean isRequireCheck; // 是否需要系统权限检测, 防止和系统提示框重叠
    private static final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数
    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案
    public static final int RECEIVER_SHOW_QRCODE = 1;
    public static final String RECEIVER_FLAG = "receiver_flag";
    private int mReceiverFlag;
    private AlertDialog mNetworkSettingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mFragmentManager = getSupportFragmentManager();
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = getIntent();
        mIsOneKeyOpen = intent.getBooleanExtra("oneKeyOpen", false);
        init();

        mIsDeviceBind = PreferencesUtils.getBoolean(mContext, Constants.IS_CONFIRM_BIND, false);
        //modify begin hhj@20161025
        /*if (!mIsDeviceBind) {
            loadLocalQrcodeImg();
        } else {
            // 如果已经绑定了，进入应用后开启定位服务
            startService(new Intent(this, LocationUploadService.class));
            // 如果已经绑定了，进入应用后开启心跳服务
            startService(new Intent(HealthMainActivity.this, HeartbeatService.class));
        }
        // 无论绑不绑定了，进入应用后开启计步服务
        startStepDetect();*/
        if (mIsDeviceBind) {
            // 如果已经绑定了，进入应用后开启定位服务
            startService(new Intent(this, LocationUploadService.class));
            // 如果已经绑定了，进入应用后开启心跳服务
            startService(new Intent(HealthMainActivity.this, HeartbeatService.class));
        }
        // 无论绑不绑定了，进入应用后开启计步服务
        startStepDetect();
        //modify end
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.HEALTH_RECEIVER_ACTION); 
        intentFilter.addAction(Constants.DEVICE_UNBIND_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);  
        mHealthReceiver = new HealthReceiver();
        registerReceiver(mHealthReceiver, intentFilter);
        mHealthModel = new HealthMinaModelImpl(this);
        
        mDatabase = new LitepalDatabaseImpl();
        
        isRequireCheck = true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        LogUtil.i("debug", "=====HealthMainActivity=============onResume()======isRequireCheck：" + isRequireCheck);
        mIsDeviceBind = PreferencesUtils.getBoolean(mContext, Constants.IS_CONFIRM_BIND, false);
        if(!mIsDeviceBind){//如果未绑定，检测当前权限是否满足
         // 缺少权限时, 进入权限配置页面
            if (PermissionsChecker.lacksPermissions(Constants.PERMISSIONS)) {
                if (isRequireCheck) {
                    String[] permissions = Constants.PERMISSIONS;
                    if (PermissionsChecker.lacksPermissions(permissions)) {
                        requestPermissions(permissions); // 请求权限
                    } else {
                        allPermissionsGranted(); // 全部权限都已获取
                    }
                } else {
                    LogUtil.i("debug", "=====HealthMainActivity=============onResume()BBB======isRequireCheck：" + isRequireCheck);
                    isRequireCheck = true;
                }
            }else{
                if(isRequireCheck){
                    allPermissionsGranted(); // 全部权限都已获取
                }else{
                    isRequireCheck = true;
                }
            } 
        }else{
            allPermissionsGranted(); // 全部权限都已获取
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        /*if (!mIsDeviceBind && mHasQrcodeImg && MinaClient.getInstance(this).isConnect()) {
            mHealthModel.sendDeviceHello(null);
        }*/
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        /**
         * called by system when bind service
         * 
         * @param className component name
         * @param service service binder
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((HealthService.ServiceBinder) service).getService();
            if (null == mService) {
                LogUtil.e(TAG, "onServiceConnected, mService is null");
                finish();
                return;
            }
            Log.d(TAG, "onServiceConnected, mService is not null");
            mService.registerHealthListener(mHealthListener);
            // if token is null,device is not bind by the child'app
            if (!mIsDeviceBind && FPHealthApplication.mHasQrcodeImg) {
                mService.sendDeviceHelloAsync();
            }
        }

        /**
         * When unbind service will call this method
         * 
         * @param className The component name
         */
        @Override
        public void onServiceDisconnected(ComponentName className) {
        }
    };

    @Override
    protected int getContentViewId() {
        // TODO Auto-generated method stub
        return R.layout.activity_health_main;
    }

    private void init() {
        initView();
    }
    
    private void initView() {
        mHealthViewPager = (ViewPager) findViewById(R.id.health_pages_viewPager);
        mBloodPressureFragment = BloodPressureFragment
                .newInstance(getResources().getString(R.string.blood_pressure_title));
        mHeartRateFragment = HeartRateFragment.newInstance(getResources()
                .getString(R.string.heart_rate_title));
        mHeartRateFragment.setmIsOneKeyOpen(mIsOneKeyOpen);
        mHeartRateFragment.setHeartRateListener(this);
        mSportsFragment = SportsFragment.newInstance(getResources().getString(
                R.string.sport_title));
        mSedentaryRemindFragment = RemindFragment
                .newInstance(getResources().getString(R.string.sedentary_remind));
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(mHeartRateFragment);
        fragments.add(mSportsFragment);
        fragments.add(mSedentaryRemindFragment);

        mFragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        mHealthViewPager.setAdapter(mFragmentPagerAdapter);
        mHealthViewPager.setCurrentItem(0, false);
        mHealthViewPager.setOnPageChangeListener(new OnPageChangeListener());

        mDotsView = (DotsView) findViewById(R.id.health_main_dotsview);
        mDotsView.setDotRessource(R.drawable.dot_selected, R.drawable.dot_unselected);
        mDotsView.setNumberOfPage(mFragmentPagerAdapter.getCount());

    }

    private class OnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int i) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageSelected(int position) {
            mCurrentPosition = position;
            mDotsView.selectDot(position);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mHeartRateFragment.ismIsDetecting() && keyCode != KeyEvent.KEYCODE_BACK){//假如当前正在检测心率，则不允许按键滑动界面
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (mCurrentPosition <= 0) {
                    mCurrentPosition = mFragmentPagerAdapter.getCount() - 1;
                }
                mHealthViewPager.setCurrentItem(mCurrentPosition--, false);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (mCurrentPosition >= mFragmentPagerAdapter.getCount() - 1) {
                    mCurrentPosition = 0;
                }
                mHealthViewPager.setCurrentItem(mCurrentPosition++, false);
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Service listener
    private HealthListener mHealthListener = new HealthListener() {
        @Override
        public void onCallBack(Bundle bundle) {
            int flag = bundle.getInt(HealthListener.CALLBACK_FLAG);

            // remove tag message first, avoid too many same messages in queue.
            Message msg = mHandler.obtainMessage(flag);
            msg.setData(bundle);
            mHandler.removeMessages(flag);
            mHandler.sendMessage(msg);
        }
    };

    /**
     * Main thread handler to update UI
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_QRCODE:
                    Bitmap qrcodeBitmap = (android.graphics.Bitmap) msg.obj;
                    if(mQrcodeDialog != null && mQrcodeDialog.getDialog() != null && mQrcodeDialog.getDialog().isShowing()){
                        //当前qrcodeDialog正在显示
                    }else{
                        showQrcodeDialog(qrcodeBitmap);
                    }
                    break;
                default:
                    break;
            }
        }

    };

    private void saveUserInfo(List<Map<String, Object>> datas) {
        for (int i = 0; i < datas.size(); i++) {
            Map<String, Object> dataMap = datas.get(i);
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                if (entry.getKey().equals(Constants.NICKNAME)
                        || entry.getKey().equals(Constants.PHONENUMBER)
                        || entry.getKey().equals(Constants.FAMILYNAME)
                        || entry.getKey().equals(Constants.TIME)) {
                    PreferencesUtils.putString(mContext, entry.getKey(), entry.getValue()
                            .toString());
                } else {
                    PreferencesUtils.putInt(mContext, entry.getKey(),
                            Integer.valueOf(entry.getValue().toString()));
                }
            }
        }
    }

    private void refreshHeartRateFragment() {
        mHeartRateFragment.refreshUI();
    }

    /**
     * Exit hearth service
     */
    private void exitService() {
        if (mIsServiceBinded) {
            unbindService(mServiceConnection);
            mIsServiceBinded = false;
        }

        if (mIsServiceStarted) {
            stopService(new Intent(HealthMainActivity.this, HealthService.class));
            mIsServiceStarted = false;
        }
    }

    /**
     * Exit step service
     */
    private void exitStepService() {
        if (mIsStepServiceBinded) {
            unbindService(mStepServiceConnection);
            mIsStepServiceBinded = false;
        }

        if (mIsStepServiceStarted) {
            stopService(new Intent(HealthMainActivity.this, StepService.class));
            mIsStepServiceStarted = false;
        }
    }

    @Override
    public void detectHeartRate() {
        mService.detectHeartRateAsync();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       /* mHandler.removeCallbacksAndMessages(null);
        if (mService != null) {
            mService.unregisterHealthListener(mHealthListener);
        }
        mHealthListener = null;
        exitService();

        stopService(new Intent(HealthMainActivity.this, HeartbeatService.class));

        // exitStepService();
        // HealthTcpClient.getInstance(this).stopReceive();
        stopService(new Intent(HealthMainActivity.this, LocationUploadService.class));
        stopService(new Intent(HealthMainActivity.this, RemindService.class));*/
//        MinaClient.getInstance(this).close();
        if(mHealthReceiver != null){
            unregisterReceiver(mHealthReceiver); 
        }
        if (mIsStepServiceBinded && mStepServiceConnection != null) {
            unbindService(mStepServiceConnection);
            mIsStepServiceBinded = false;
        }
    }

    @Override
    public void onDeviceBindDialogClick(int status) {
        FPHealthApplication.mIsShowConfirmBind = false;
        mDeviceBindStatus = status;
        mHealthModel.sendDeviceBind(status, null);
    }

    private void startStepDetect() {
        if (null == startService(new Intent(HealthMainActivity.this, StepService.class))) {
            LogUtil.i(TAG, "onStart, cannot start Step service");
            return;
        }
        mIsStepServiceStarted = true;
        mIsStepServiceBinded = bindService(new Intent(HealthMainActivity.this,
                StepService.class),
                mStepServiceConnection, Context.BIND_AUTO_CREATE);
        LogUtil.i("debug", "====HealthMainActivity==========startStepDetect======mIsStepServiceBinded@@@:" + mIsStepServiceBinded);
        if (!mIsStepServiceBinded) {
            LogUtil.e(TAG, "onStart, cannot bind Step service");
            return;
        }
    }

    private final ServiceConnection mStepServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mStepService = ((StepService.ServiceBinder) service).getService();
            mStepService.registerStepDetectLister(mSportsFragment);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
        }
    };

    @Override
    public void onQrcodeDialogClick() {
        LogUtil.i("debug", "========HealthMainActivity=====######====onQrcodeDialogClick==#####=关闭二维码图片");
        FPHealthApplication.mHasQrcodeImg = false;
    }

    private void loadLocalQrcodeImg() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Bitmap qrBitmap = FileUtil.loadQrCodePicture();
                if (qrBitmap != null) {
                    Message msg = mHandler.obtainMessage(WHAT_QRCODE);
                    msg.obj = qrBitmap;
                    mHandler.sendMessage(msg);
                } else {
                    LogUtil.i("debug", "========HealthMainActivity=========onQrcodeDialogClick===mHasQrcodeImg is Flase");
                    FPHealthApplication.mHasQrcodeImg = false;
                }
            }
        }).start();
    }

    private void showQrcodeDialog(Bitmap qrcodeBitmap) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mQrcodeDialog = new QrcodeDialog(qrcodeBitmap);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(mQrcodeDialog, TAG_DEVICE_QRCODE);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void sendHeartRate(HeartRate heartRate) {
        List<HeartRate> heartRates = mDatabase.getHeartRates();
        heartRates.add(heartRate);
        //缓存心率数据
        heartRate.save();
        mHealthModel.sendBloodPressueUpload(heartRates);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
        mIsOneKeyOpen = intent.getBooleanExtra("oneKeyOpen", false);
        mHeartRateFragment.setmIsOneKeyOpen(mIsOneKeyOpen);
    }
    
    private class HealthReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(Constants.DEVICE_UNBIND_ACTION.equals(intent.getAction())){
                showReBindDialog();
            }else{
                int receiverFlag = intent.getIntExtra(RECEIVER_FLAG, -1);
                if(receiverFlag == RECEIVER_SHOW_QRCODE){//从本低加载并显示二维码，同时发起20101请求
                    doDevicebind();
                    return;
                }
                boolean isNetAvailable = intent.getBooleanExtra(Constants.IS_NETWORK_AVAILABLE, true);
                if(!isNetAvailable){
                    mHeartRateFragment.showNetUnavailable(true);
                }else{
                    mHeartRateFragment.showNetUnavailable(false);
                }
                TcpProtocol responseTcp = intent.getParcelableExtra("data");
                if(responseTcp != null){
                    switch (responseTcp.getCommand()) {
                        case Constants.TCP.COMMAND_DEVICE_HELLO:  
                            int deviceBindStatus = responseTcp.getStatus();
                            // if status is 0,there are binding demand
                            if (deviceBindStatus == Constants.TCP.STATUS_DEVICE_BIND) {
                                if (mQrcodeDialog != null) {
                                    getWindow().clearFlags(
                                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                    mQrcodeDialog.dismissAllowingStateLoss();
                                    mQrcodeDialog = null;
                                }
                                DeviceBindDialog deviceBindDialog = new DeviceBindDialog();
                                FragmentTransaction ft = mFragmentManager.beginTransaction();
                                ft.add(deviceBindDialog, TAG_DEVICE_BIND);
                                ft.commitAllowingStateLoss();
                                FPHealthApplication.mIsShowConfirmBind = true;
                                saveUserInfo(responseTcp.getDatas());
                                PreferencesUtils.putString(mContext, Constants.TOKEN,
                                        responseTcp.getToken());
                            } else {
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                LogUtil.i("debug", "========#########===设备没有绑定请求=====######=====FPHealthApplication.mHasQrcodeImg:" + FPHealthApplication.mHasQrcodeImg);
                                if (FPHealthApplication.mHasQrcodeImg) {
                                    mHealthModel.sendDeviceHello(null);
                                }
                            }
                            break;
                        case Constants.TCP.COMMAND_DEVICE_BIND:  
                            int responseStatus = responseTcp.getStatus();
                            // if status is 1,confirm Bind,otherwise do not
                            if (mDeviceBindStatus == Constants.DEVICE_BIND_STATUS) {
                                if (responseStatus == 0) {
                                    PreferencesUtils.putBoolean(mContext, Constants.IS_CONFIRM_BIND, true);
                                    // 确认绑定后，开启心跳服务
                                    startService(new Intent(HealthMainActivity.this, HeartbeatService.class));
                                    // 确认绑定后，开启定位服务
                                    startService(new Intent(HealthMainActivity.this,
                                            LocationUploadService.class));
                                    //确认绑定后，需要重启计步服务（不绑定的时候，定时器有可能已经关闭了）
                                    startService(new Intent(HealthMainActivity.this,
                                            StepService.class));
                                    //确定绑定后清零计步
                                    mContext.sendBroadcast(new Intent(Constants.CLEAR_STEP_ACTION));
                                    //清除解绑通知
                                    NotificationManager notificationManager = (NotificationManager)
                                            FPHealthApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.cancel(Constants.UNBIND_NOTIFICATION_ID);
                                }
                            } else {
                                PreferencesUtils.putBoolean(mContext, Constants.IS_CONFIRM_BIND, false);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            
        }
        
    }
    
    // 请求权限兼容低版本
    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }
    
    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = false;
            Log.i("debug","=====MainActivity=====onRequestPermissionsResultAAA=======isRequireCheck:" + isRequireCheck);
            allPermissionsGranted();
        } else {
            isRequireCheck = false;
            Log.i("debug","=====MainActivity=====onRequestPermissionsResultBBB=======isRequireCheck:" + isRequireCheck);
            showMissingPermissionDialog();
        }
    }
    
 // 含有全部的权限
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
    
 // 显示缺失权限提示
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HealthMainActivity.this);
        builder.setTitle(R.string.help);
        builder.setMessage(R.string.string_help_text);

        // 拒绝, 关闭提示框进入主界面
        builder.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
            }
        });
        //
        builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });

        builder.setCancelable(false);

        builder.show();
        Log.i("debug", "=====MainActivity=====showMissingPermissionDialog=======");
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }
    
    // 全部权限均已获取
    private void allPermissionsGranted() {
        //判断当前网络是否可用
        if(NetworkUtil.checkNetwork(this)){
            LogUtil.i("debug", "=====@@@@@@@@@==HealthMainActivity======allPermissionsGranted=====网络可用====mIsDeviceBind:" + mIsDeviceBind
                    + "====mHasQrcodeImg:" + FPHealthApplication.mHasQrcodeImg + 
                    "===mIsShowConfirmBind:" + FPHealthApplication.mIsShowConfirmBind);
            if(!mIsDeviceBind && !FPHealthApplication.mHasQrcodeImg && !FPHealthApplication.mIsShowConfirmBind){//如果当前没有绑定，且没有显示二维码图片或者确认绑定提示框，不会执行http流程
                this.startService(new Intent(this,BootService.class)); 
            }
        }else{
            LogUtil.i("debug", "=====HealthMainActivity=============allPermissionsGranted======网络不可用");
            showNetworkSettingDialog();
        }
    }
    
    // 显示连接网络提示
    private void showNetworkSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HealthMainActivity.this);
        builder.setTitle(R.string.help);
        builder.setMessage(R.string.string_help_network_text);

        // 拒绝, 关闭提示框进入主界面
        builder.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
            }
        });
        //
        builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startNetworkSettings();
            }
        });

        builder.setCancelable(false);
        mNetworkSettingDialog = builder.create();
        mNetworkSettingDialog.show();
        Log.i("debug", "=====MainActivity=====showNetworkSettingDialog=======");
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if(mNetworkSettingDialog != null && mNetworkSettingDialog.isShowing()){
            mNetworkSettingDialog.dismiss();
            mNetworkSettingDialog = null;
        }
    }
    
 // 启动应用的设置
    private void startNetworkSettings() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        startActivity(intent);
    }
    
    // 显示重新绑定提示
    private void showReBindDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HealthMainActivity.this);
        builder.setTitle(R.string.help);
        builder.setMessage(R.string.string_help_rebind_text);
        // 拒绝, 关闭提示框进入主界面
        builder.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
                 Toast.makeText(HealthMainActivity.this,R.string.help_rebind_text, Toast.LENGTH_LONG).show();
            }
        });

        builder.setPositiveButton(R.string.rebind, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doDevicebind();
            }
        });

        builder.setCancelable(false);
        mNetworkSettingDialog = builder.create();
        mNetworkSettingDialog.show();
        Log.i("debug", "=====MainActivity=====showNetworkSettingDialog=======");
    }
    
    //进行设备绑定
    public void doDevicebind(){
        FPHealthApplication.mHasQrcodeImg = true;
        mIsDeviceBind = PreferencesUtils.getBoolean(mContext, Constants.IS_CONFIRM_BIND, false);
        if (!mIsDeviceBind) {
            loadLocalQrcodeImg();
        }
        LogUtil.i("debug", "==========HealthReceiver=====onReceive===@@@@@@@@===mIsDeviceBind:" + mIsDeviceBind 
                + "==mHasQrcodeImg:" + FPHealthApplication.mHasQrcodeImg
                + "==isConnect:" + MinaClient.getInstance(HealthMainActivity.this).isConnect());
        if (!mIsDeviceBind && MinaClient.getInstance(HealthMainActivity.this).isConnect()) {
            LogUtil.i("debug", "==========HealthReceiver=====onReceive===@@@@@@@@===发送app设备hello:");
            mHealthModel.sendDeviceHello(null);
        }
    }

}
