
package cn.stj.fphealth.tcp.mina;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import cn.stj.fphealth.R;
import cn.stj.fphealth.activity.HealthMainActivity;
import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.FPHealthApplication;
import cn.stj.fphealth.app.Utils;
import cn.stj.fphealth.db.Database;
import cn.stj.fphealth.db.LitepalDatabaseImpl;
import cn.stj.fphealth.entity.RemindInfo;
import cn.stj.fphealth.model.HealthMinaModelImpl;
import cn.stj.fphealth.model.HealthModel;
import cn.stj.fphealth.receiver.RemindReceiver;
import cn.stj.fphealth.service.LocationUploadService;
import cn.stj.fphealth.service.RemindService;
import cn.stj.fphealth.service.StepService;
import cn.stj.fphealth.util.FileUtil;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.util.NetworkUtil;
import cn.stj.fphealth.util.PreferencesUtils;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.Util;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

public class ClientMessageHandlerAdapter extends IoHandlerAdapter {

    private final static Logger log = LoggerFactory.getLogger(ClientMessageHandlerAdapter.class);
    private HealthModel mHealthModel;
    private Database mDatabase;
    private Context mContext;

    public ClientMessageHandlerAdapter(Context context) {
        super();
        this.mContext = context;
        mHealthModel = new HealthMinaModelImpl(context);
        mDatabase = new LitepalDatabaseImpl();
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        LogUtil.i("debug", "messageReceived 客户端接收消息：" + message);
        TcpProtocol receTcpProtocol = (TcpProtocol) message;
        switch (receTcpProtocol.getCommand()) {
            case Constants.TCP.COMMAND_DEVICE_HELLO:
            case Constants.TCP.COMMAND_DEVICE_BIND:
                Intent intent = new Intent(Constants.HEALTH_RECEIVER_ACTION);
                intent.putExtra("data", receTcpProtocol);
                FPHealthApplication.getInstance().sendBroadcast(intent);
                break;
            case Constants.TCP.COMMAND_DEVICE_UNBIND:
                doDeviceUnbind();
                break;
            case Constants.TCP.COMMAND_REMIND_ADD:
//                mHealthModel.remindAdd(Utils.getRemindInfos(receTcpProtocol));
                Message remindAddMsg = mHandler.obtainMessage();
                remindAddMsg.what = Constants.TCP.COMMAND_REMIND_ADD;
                remindAddMsg.obj = receTcpProtocol;
                mHandler.sendMessage(remindAddMsg);
                break;
            case Constants.TCP.COMMAND_REMIND_EDIT:
//                mHealthModel.remindEdit(Utils.getRemindInfos(receTcpProtocol));
                Message remindEditmsg = mHandler.obtainMessage();
                remindEditmsg.what = Constants.TCP.COMMAND_REMIND_EDIT;
                remindEditmsg.obj = receTcpProtocol;
                mHandler.sendMessage(remindEditmsg);
                break;
            case Constants.TCP.COMMAND_REMIND_DEL:
//                mHealthModel.remindDel(Utils.getRemindInfos(receTcpProtocol));
                Message remindDelmsg = mHandler.obtainMessage();
                remindDelmsg.what = Constants.TCP.COMMAND_REMIND_DEL;
                remindDelmsg.obj = receTcpProtocol;
                mHandler.sendMessage(remindDelmsg);
                break;
            case Constants.TCP.COMMAND_LOCATION_QUERY:
                mHealthModel.sendLocationQuery();
                Utils.cleanLocationData();
                break;
            case Constants.TCP.COMMAND_LOCATION_TRRIGE:
                mHealthModel.sendLocationTrrige();
                Utils.cleanLocationData();
                break;
            case Constants.TCP.COMMAND_LOCATION_UPLOAD:
                Utils.cleanLocationData();
                break;
            case Constants.TCP.COMMAND_PARAM_GET:
//                deviceParamsGet(receTcpProtocol);
                Message paramsGetmsg = mHandler.obtainMessage();
                paramsGetmsg.what = Constants.TCP.COMMAND_PARAM_GET;
                paramsGetmsg.obj = receTcpProtocol;
                mHandler.sendMessage(paramsGetmsg);
                break;
            case Constants.TCP.COMMAND_PARAM_SET:
//                deviceParamsSet(receTcpProtocol);
                Message paramsSetmsg = mHandler.obtainMessage();
                paramsSetmsg.what = Constants.TCP.COMMAND_PARAM_SET;
                paramsSetmsg.obj = receTcpProtocol;
                mHandler.sendMessage(paramsSetmsg);
                break;
            case Constants.TCP.COMMAND_WALK_UPLOAD:
            	if(mDatabase != null){
            		mDatabase.deleteAllPedometer();
            	}
                break;
            case Constants.TCP.COMMAND_BLOOD_PRESSUE_UPLOAD:
            	if(mDatabase != null){
            		mDatabase.deleteAllHeartRate();
            	}
            	break;
            default:
                break;
        }

    }

    public void messageSent(IoSession session, Object message) throws Exception {
        LogUtil.i("debug", "messageSent 客户端发送消息：" + message);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LogUtil.i("debug", "客户端发生异常：" + cause.getMessage());
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        LogUtil.i("debug", "服务器与客户端创建连接...");
        super.sessionCreated(session);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        LogUtil.i("debug", "服务器与客户端连接打开...");
        super.sessionOpened(session);
        boolean mIsDeviceBind = PreferencesUtils.getBoolean(FPHealthApplication.getInstance(),
                Constants.IS_CONFIRM_BIND, false);
        LogUtil.i("debug", "=====@@==sessionOpened=@@@=======mHasQrcodeImg:" + FPHealthApplication.mHasQrcodeImg);
        if (!mIsDeviceBind && FPHealthApplication.mHasQrcodeImg) {
            mHealthModel.sendDeviceHello(null);
        } else {
            // 在断线重连后发送心跳包能及时获取服务器推送的数据
            mHealthModel.sendHeartBeat(1);
        }

        if (NetworkUtil.checkNetwork(mContext)) {
            Intent intent = new Intent(Constants.HEALTH_RECEIVER_ACTION);
            intent.putExtra(Constants.IS_NETWORK_AVAILABLE, true);
            FPHealthApplication.getInstance().sendBroadcast(intent);
        }
    }

    // 关闭与客户端的连接时会调用此方法
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        LogUtil.i("debug", "服务器与客户端断开连接...");
        super.sessionClosed(session);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        LogUtil.i("debug", "客户端进入空闲状态...");
        super.sessionIdle(session, status);
    }

    /**
     * 获取服务器响应数据中设备参数并保存到本地
     * 
     * @param receTcpProtocol
     */
    private void deviceParamsSet(TcpProtocol receTcpProtocol) {
        LogUtil.i("debug", "====ClientMessageHandler==================deviceParamsSet===");
        List<Map<String, Object>> datas = receTcpProtocol.getDatas();
        String paramCode = "";
        String paramValue = "";
        if (datas.size() > 0) {
            for (int i = 0; i < datas.size(); i++) {
                Map<String, Object> dataMap = datas.get(i);
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    if (Constants.DEVICE_PARAM.CODE.equals(entry.getKey())) {
                        paramCode = (String) entry.getValue();
                    } else {
                        paramValue = (String) entry.getValue();
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(paramCode) && !TextUtils.isEmpty(paramValue)) {
            mHealthModel.deviceParamSet(paramCode, paramValue);
        }
    }

    /**
     * 获取服务器响应数据中设备参数编码，并把参数编码对应的参数值发送给服务器
     * 
     * @param receTcpProtocol
     */
    private void deviceParamsGet(TcpProtocol receTcpProtocol) {
        LogUtil.i("debug", "====HttpTcpClient==================deviceParamsGet===");
        List<Map<String, Object>> datas = receTcpProtocol.getDatas();
        String paramCode = "";
        if (datas.size() > 0) {
            for (int i = 0; i < datas.size(); i++) {
                Map<String, Object> dataMap = datas.get(i);
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    if (Constants.DEVICE_PARAM.CODE.equals(entry.getKey())) {
                        paramCode = (String) entry.getValue();
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(paramCode)) {
            mHealthModel.deviceParamGet(paramCode);
        }
    }

    /**
     * 处理解绑
     */
    private void doDeviceUnbind() {
        Utils.clearUserInfo(mContext);
        // 发送解除绑定通知
        sendUnbindNotification();
        // 在当前退出应用情况下，清零计步数据
        PreferencesUtils.putInt(mContext, Constants.KEY_TOTAL_STEP, 0);
        // 停止定位上传服务以及删除缓存定位数据
        mContext.stopService(new Intent(mContext, LocationUploadService.class));
        mDatabase.deleteGPSInfo();
        mDatabase.deleteMobileInfo();
        mDatabase.deleteWifiInfo();
        // 停止提醒服务以及删除提醒相关数据
        mContext.stopService(new Intent(mContext, RemindService.class));
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        List<RemindInfo> remindInfos = mDatabase.getRemindInfos();
        for (int i = 0; i < remindInfos.size(); i++) {
            RemindInfo remindInfo = remindInfos.get(i);
            Intent intent = new Intent(mContext, RemindReceiver.class);
            PendingIntent remindIntent = PendingIntent.getBroadcast(mContext,
                    remindInfo.getRemindId(),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(remindIntent);
        }
        mDatabase.deleteAllRemindInfo();
        FileUtil.deleteAllRemindVoice(mContext);

        // 没有退出应用情况下清零计步数据以及提醒数据
        Intent unbindIntent = new Intent(Constants.DEVICE_UNBIND_ACTION);
        mContext.sendBroadcast(unbindIntent);
        mHealthModel.sendDeviceUnBind();
    }

    private void sendUnbindNotification() {
        LogUtil.i("debug", "==========clientMessagehandler========sendUnbindNotification");
        String content = FPHealthApplication.getInstance().getResources()
                .getString(R.string.device_unbind);
        String contentTitle = FPHealthApplication.getInstance().getResources()
                .getString(R.string.device_unbind_title);
        NotificationManager notificationManager = (NotificationManager)
                FPHealthApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(FPHealthApplication.getInstance());
        builder.setSmallIcon(R.drawable.ic_launcher)
                // 设置状态栏里面的图标（小图标） 　
                .setLargeIcon(
                        BitmapFactory.decodeResource(FPHealthApplication.getInstance()
                                .getResources(), R.drawable.ic_launcher))// 下拉下拉列表里面的图标（大图标）
                                                                         // 　　　　　　　
                .setTicker(content) // 设置状态栏的显示的信息
                .setWhen(System.currentTimeMillis())// 设置时间发生时间
                .setAutoCancel(true)// 设置可以清除
                .setContentTitle(contentTitle)
                .setContentText(content);// 设置上下文内容
        // 定义Notification的各种属性
        Notification notification = builder.build();// 获取一个Notification
        notification.defaults = Notification.DEFAULT_SOUND;// 设置为默认的声音
        notificationManager.notify(Constants.UNBIND_NOTIFICATION_ID, notification);// 显示通知
    }
    
    private Handler mHandler = new Handler(Looper.getMainLooper()){
        
         public void handleMessage(android.os.Message msg) {
             LogUtil.i("debug", "clientMessageHandler========mHandler=======Thread:" + Thread.currentThread().getName());
             TcpProtocol receTcpProtocol = (TcpProtocol) msg.obj;
             if(receTcpProtocol != null){
                 switch (msg.what) {
                     case Constants.TCP.COMMAND_REMIND_EDIT:
                         if(mHealthModel != null){
                             mHealthModel.remindEdit(Utils.getRemindInfos(receTcpProtocol));
                         }
                         break;
                     case Constants.TCP.COMMAND_REMIND_ADD:
                         if(mHealthModel != null){
                             mHealthModel.remindAdd(Utils.getRemindInfos(receTcpProtocol));
                         }
                         break;
                     case Constants.TCP.COMMAND_REMIND_DEL:
                         if(mHealthModel != null){
                             mHealthModel.remindDel(Utils.getRemindInfos(receTcpProtocol));
                         }
                         break;
                     case Constants.TCP.COMMAND_PARAM_GET:
                         deviceParamsGet(receTcpProtocol);
                         break;
                     case Constants.TCP.COMMAND_PARAM_SET:
                         deviceParamsSet(receTcpProtocol);
                         break;
                     default:
                         break;
                 }
             }
         };  
    };
}
