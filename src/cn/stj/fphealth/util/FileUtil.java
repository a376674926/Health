package cn.stj.fphealth.util;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.tech.IsoDep;
import android.os.Environment;
import android.widget.Toast;

import cn.stj.fphealth.R;
import cn.stj.fphealth.activity.HealthMainActivity;
import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.app.FPHealthApplication;
import cn.stj.fphealth.app.Utils;
import cn.stj.fphealth.entity.RemindInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {

	private static String HEART_RATE_PATH = "/sys/bus/i2c/devices/2-005a/heart_rate";
	private static String START_DETECT_PATH = "/sys/bus/i2c/devices/2-005a/layout";
	private static String STOP_DETECT_PATH = "/sys/bus/i2c/devices/2-005a/disable";

	/**
	 * 保存生成的二維碼圖片
	 */
	public static void saveQrCodePicture(Context context, Bitmap qrCodeBitmap) {
	    if(!PermissionsChecker.lacksPermission(Constants.WRITE_EXTERNAL_STORAGE_PERMISSION)){
	        if (Environment.getExternalStorageState().equals(
	                Environment.MEDIA_MOUNTED)) {
	            LogUtil.i("debug",
	                    "==FileUtil===============getExternalStorageState======"
	                            + Environment.getExternalStorageDirectory());
	            final File qrImage = new File(
	                    Environment.getExternalStorageDirectory(),
	                    "/health/qrcode/" + Constants.BIND_QRCODE_FILENAME);
	            if (qrImage.exists()) {
	                qrImage.delete();
	            }
	            try {
	                if (!qrImage.getParentFile().exists()) {
	                    qrImage.getParentFile().mkdirs();// 构建文件夹
                    }
	                qrImage.createNewFile();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	            FileOutputStream fOut = null;
	            try {
	                fOut = new FileOutputStream(qrImage);
	            } catch (FileNotFoundException e) {
	                e.printStackTrace();
	            }
	            if (qrCodeBitmap == null) {
	                return;
	            }
	            qrCodeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
	            try {
	                fOut.flush();
	                fOut.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }finally{
	                /*if(FPHealthApplication.mHasQrcodeImg){//如果当前界面有显示二维码提示框，会更新原来提示框
	                    Intent intent = intent
	                }*/
	                //下载完二维码图片，通知界面显示二维码图片
	                Intent intent = new Intent(Constants.HEALTH_RECEIVER_ACTION);
	                intent.putExtra(HealthMainActivity.RECEIVER_FLAG, HealthMainActivity.RECEIVER_SHOW_QRCODE);
	                FPHealthApplication.getInstance().sendBroadcast(intent);
	            }
	        } else {
	            Toast.makeText(context, R.string.sdcard_error, Toast.LENGTH_LONG)
	                    .show();
	        }
	    }
	}

	/**
	 * 保存提醒语音
	 */
	public static void saveRemindVoice(Context context, byte[] remindVoiceByte,
			RemindInfo remindInfo) {
	    if(!PermissionsChecker.lacksPermission(Constants.WRITE_EXTERNAL_STORAGE_PERMISSION)){
    		if (Environment.getExternalStorageState().equals(
    				Environment.MEDIA_MOUNTED)) {
    			File remindVoiceFile = new File(
    					Environment.getExternalStorageDirectory() + "/health/remindVoice/"+ 
    			remindInfo.getRemindId() + "_" + remindInfo.getContent() + ".amr");
    			LogUtil.i("debug",
    	                "==FileUtil===============saveRemindVoice======remindVoice path:" + remindVoiceFile.getName());
    			try {
    				if (remindVoiceFile.exists()) {
    					remindVoiceFile.delete();
    				} else {
    					if (!remindVoiceFile.getParentFile().exists()) {
    						remindVoiceFile.getParentFile().mkdirs();// 构建文件夹
    					}
    					remindVoiceFile.createNewFile();// 构建文件
    				}
    				FileOutputStream fos = new FileOutputStream(remindVoiceFile);
    				fos.write(remindVoiceByte, 0, remindVoiceByte.length);
    				fos.flush();
    				fos.close();
    			} catch (Exception e) {
    			    LogUtil.i("debug", "=========saveRemindVoice=======exception:" + e.getMessage());
    				e.printStackTrace();
    			}
    		} else {
    			Toast.makeText(context, R.string.sdcard_error, Toast.LENGTH_LONG)
    					.show();
    		}
	    }

	}

	/**
	 * 更新提醒语音
	 */
	public static void updateRemindVoice(Context context, RemindInfo remindInfo) {
	    if(!PermissionsChecker.lacksPermission(Constants.WRITE_EXTERNAL_STORAGE_PERMISSION)){
    		if (Environment.getExternalStorageState().equals(
    				Environment.MEDIA_MOUNTED)) {
    			File remindVoiceFile = new File(Environment.getExternalStorageDirectory()
    					+ "/health/remindVoice/" + remindInfo.getRemindId() + "_"
    					+ remindInfo.getContent() + ".amr");
    			try {
    				if (!remindVoiceFile.exists()) {//已更新提醒的语音文件不存在，说明语音文件有变动需要更新
    					File remindVoiceDirFile = new File(Environment.getExternalStorageDirectory()
    							+ "/health/remindVoice/");
    				    if(remindVoiceDirFile.exists()){
    				    	//取出文件列表：  
    				    	final File[] files = remindVoiceDirFile.listFiles(); 
    				    	if(files != null && files.length != 0){
    				    	    for(int i = 0;i < files.length;i++){
    	                            File file = files[i];
    	                            String fileName = file.getName();  
    	                            String remindId = fileName.substring(0, fileName.indexOf("_"));
    	                            if(remindId.equals(remindInfo.getRemindId()+"")){
    	                                file.delete();
    	                                Utils.downRemindVoiceByHttp(context, remindInfo);
    	                                break;
    	                            }
    	                        }   
    				    	}else{
    				    	    Utils.downRemindVoiceByHttp(context, remindInfo);
    				    	}
    				    }else{
    				        Utils.downRemindVoiceByHttp(context, remindInfo);
    				    }
    				}
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		} else {
    			Toast.makeText(context, R.string.sdcard_error, Toast.LENGTH_LONG)
    					.show();
    		}
	    }
	}

	/**
	 * 删除提醒语音
	 */
	public static void deleteRemindVoice(Context context, RemindInfo remindInfo) {
	    if(!PermissionsChecker.lacksPermission(Constants.WRITE_EXTERNAL_STORAGE_PERMISSION)){
    	    if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                File remindVoiceFile = new File(Environment.getExternalStorageDirectory()
                        + "/health/remindVoice/" + remindInfo.getRemindId() + "_"
                        + remindInfo.getContent() + ".amr");
        		try {
        			if (remindVoiceFile.exists()) {
        				remindVoiceFile.delete();
        			}
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
    	    } else {
                Toast.makeText(context, R.string.sdcard_error, Toast.LENGTH_LONG)
                        .show();
            }
	    }

	}
	
	/**
     * 删除所有提醒语音
     */
    public static void deleteAllRemindVoice(Context context) {
        if(!PermissionsChecker.lacksPermission(Constants.WRITE_EXTERNAL_STORAGE_PERMISSION)){
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                File remindVoiceDir = new File(Environment.getExternalStorageDirectory()
                        + "/health/remindVoice/");
                try {
                    if (remindVoiceDir.isDirectory()) {
                        String[] children = remindVoiceDir.list();
                        for (int i=0; i<children.length; i++) {
                            File remindVoiceFile = new File(remindVoiceDir, children[i]);
                            remindVoiceFile.delete();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, R.string.sdcard_error, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }

	public static Bitmap loadQrCodePicture() {
	    if(!PermissionsChecker.lacksPermission(Constants.WRITE_EXTERNAL_STORAGE_PERMISSION)){
    		if (Environment.getExternalStorageState().equals(
    				Environment.MEDIA_MOUNTED)) {
    			final File qrImage = new File(
    					Environment.getExternalStorageDirectory(),
    					"/health/qrcode/" + Constants.BIND_QRCODE_FILENAME);
    			if (qrImage.exists()) {
    				FileInputStream fis = null;
    				try {
    					fis = new FileInputStream(qrImage);
    					Bitmap qrBitmap = BitmapFactory.decodeStream(fis);
    					return qrBitmap;
    				} catch (FileNotFoundException e) {
    					e.printStackTrace();
    				}finally{
    				    try {
    				        if(fis != null){
    				            fis.close();
    				        }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
    				}
    			}
    		}
    	}
	    return null;
	}

	public static int[] getHeartRateData(Context context) {
		int[] heartRate = new int[6];
		Process process = null;
		InputStream is = null;
		DataInputStream dis = null;
		try {
			Runtime runtime = Runtime.getRuntime();
			process = runtime.exec("cat " + HEART_RATE_PATH); // 此处进行读操作
			is = process.getInputStream();
			dis = new DataInputStream(is);
			dis.skip(7);
			heartRate[0] = (int) dis.readByte();
			heartRate[1] = (int) dis.readByte();
			heartRate[2] = (int) dis.readByte();
			heartRate[3] = (int) dis.readByte();
			heartRate[4] = (int) dis.readByte();
			dis.skip(3);
			heartRate[5] = (int) dis.readByte();
            /*LogUtil.i("debug", "=======getHeartRateData=================progress:" + heartRate[2] 
                    + " ---rate:" + heartRate[4] + "---completed:" + heartRate[5]);*/
			return heartRate;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (process != null) {
				process.destroy();
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return heartRate;
	}

	public static void startDetectHeartRate(Context context) {
		FileReader fReader = null;
		BufferedReader reader = null;
		try {
			fReader = new FileReader(START_DETECT_PATH);
			reader = new BufferedReader(fReader);
			String line = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fReader != null) {
					fReader.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void stopDetectHeartRate(Context context) {
		LogUtil.i("debug",
				"============FileUtil============stopDetectHeartRate=========");
		FileReader fReader = null;
		BufferedReader reader = null;
		try {
			fReader = new FileReader(STOP_DETECT_PATH);
			reader = new BufferedReader(fReader);
			String line = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fReader != null) {
					fReader.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
