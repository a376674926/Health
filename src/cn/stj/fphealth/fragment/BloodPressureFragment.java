
package cn.stj.fphealth.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.stj.fphealth.R;
import cn.stj.fphealth.base.BasePageFragment;

/**
 * @author hhj@20160804
 */
public class BloodPressureFragment extends BasePageFragment {

    private String mTitle;
    private TextView mTitleTextView;
    private TextView mHightLowPressureTextView;

    public static BloodPressureFragment newInstance(String title) {
        BloodPressureFragment fragment = new BloodPressureFragment();
        Bundle args = new Bundle();
        args.putString(KEY_FRAGMENT_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = getArguments().getString(KEY_FRAGMENT_TITLE);
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        mTitleTextView = (TextView) view.findViewById(R.id.tv_title);
        mTitleTextView.setText(mTitle);

        mHightLowPressureTextView = (TextView) view.findViewById(R.id.tv_hight_Low_pressure);
        String boolPressure = getResources().getString(R.string.hight_and_low_pressure, "0.0", "0");
        mHightLowPressureTextView.setText(boolPressure);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_blood_pressure;
    }

    @Override
    public void fetchData() {

    }
}
