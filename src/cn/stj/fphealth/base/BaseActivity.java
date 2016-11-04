
package cn.stj.fphealth.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.Toast;

import cn.stj.fphealth.R;

/**
 * @author hhj@20160804
 */
public abstract class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(getContentViewId());
    }

    protected void launchActivity(Class<?> pClass) {
        launchActivity(pClass, null);
    }

    protected void launchActivity(Class<?> pClass, Bundle pBundle) {
        launchActivity(pClass, pBundle, -1);
    }

    protected void launchActivity(Class<?> pClass, Bundle pBundle, int intentFlag) {
        Intent intent = new Intent(this, pClass);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        if (intentFlag != -1) {
            intent.addFlags(intentFlag);
        }
        startActivity(intent);
    }

    protected void launchActivity(String pAction) {
        launchActivity(pAction, null);
    }

    protected void launchActivity(String pAction, Bundle pBundle) {
        Intent intent = new Intent(pAction);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        startActivity(intent);
    }

    protected void showToastMsg(int msg)
    {
        showToastMsg(getString(msg));
    }

    protected void showToastMsg(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    protected abstract int getContentViewId();

}
