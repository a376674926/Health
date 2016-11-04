package cn.stj.fphealth.app;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import cn.stj.fphealth.db.Database;
import cn.stj.fphealth.db.LitepalDatabaseImpl;
import cn.stj.fphealth.tcp.mina.MinaClient;
import cn.stj.fphealth.util.LogUtil;

import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;

import java.io.File;

public class FPHealthApplication extends LitePalApplication {
    private static FPHealthApplication mInstance;
    private Database mDatabase;
    public static boolean mHasQrcodeImg ;//是否获取到二维码图片
    public static boolean mIsShowConfirmBind;//界面当前是否显示确认绑定提示框

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mInstance = this;
        LogUtil.i("debug", "======FPHealthApplication======@@@@@@@@@@@@@@@@@#######======onCreate===");
        
        MinaClient.getInstance(this).connect();
        mDatabase = new LitepalDatabaseImpl();
        // 更新升级应用
        SQLiteDatabase db = Connector.getDatabase();
        //频繁执行查询会抛出异常SQLiteCantOpenDatabaseException
        //根本原因sqlite临时文件目录不可用，解决方法是第一次建立连接时设置临时文件目录
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File sqliteTmpDir = new File(Environment.getExternalStorageDirectory()
                    + "/sqlite/");
            if(!sqliteTmpDir.exists()){
                sqliteTmpDir.mkdirs();
            }
//            db.execSQL("PRAGMA temp_store_directory = '"+sqliteTmpDir.getPath()+"'");
        }
    }
    
    public static FPHealthApplication getInstance(){
        return mInstance;
    }

    public Database getmDatabase() {
        return mDatabase;
    }
    
}
