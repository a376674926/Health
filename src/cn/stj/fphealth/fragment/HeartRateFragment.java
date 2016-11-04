
package cn.stj.fphealth.fragment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.stj.fphealth.R;
import cn.stj.fphealth.app.Constants;
import cn.stj.fphealth.base.BasePageFragment;
import cn.stj.fphealth.entity.HeartRate;
import cn.stj.fphealth.util.FileUtil;
import cn.stj.fphealth.util.LogUtil;
import cn.stj.fphealth.views.HeartRateCircleBar;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author hhj@20160804
 */
public class HeartRateFragment extends BasePageFragment implements View.OnClickListener {

    private static final int HIGH = 120;
    private String mTitle;
    private TextView mLowPressureTextView;
    private TextView mHighPressureTextView;
    private TextView mHeartRateTextView;
    private TextView mHighTipTextView;
    private ImageView mReDetectBgImageView;
    private Button mReDetectButton;
    private HeartRateListener mHeartRateListener;
    private double rate = 0;
    private int progress = 0;
    private Timer mTimer;
    private RelativeLayout mDetectLoadingLayout;
    private long mFromTime;
    private long mToTime;
    private boolean flag = true;
    private TextView mHeatRateDetectingTV;
    private HeartRateCircleBar mHeartRateCirclebar;
    // 图表相关
    private XYSeries series;
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView mHeartRateChart;
    private XYMultipleSeriesRenderer mRenderer;
    private Context context;
    private int mYMax = 12;// y轴最大值，根据不同传感器变化
    private int mXMax = 50;// 一屏显示测量次数
    private int mYMin = 0;
    private LinearLayout mHeartRateLayout;
    private double mAverage;
    private Thread mChartThread;
    private Handler mChartHandler;
    private int mAddX = -1;
    private double mAddY = 0;
    public final static int INTERVAL = 50;//每半秒刷新心率图标
    private double mJitterCount = 0;//心率抖动次数
    private boolean mIsFirstJitter = true;//是否第一次心率抖动
    private long mIntervalTime = 0;//每隔一段时间心率抖动
    private double mPhonyRate = 0;//假心率（当心率rate为0时）
    private double mLastRate = 0;//心率（当心率rate>mAverage）
    private boolean mIsHeartRatePause;//界面是否可见。
    private boolean mIsDetecting;//设置是否在检测
    private boolean mIsOneKeyOpen;//设置是否一键打开心率检测
    private TextView mTitleTextView;
    private RelativeLayout mNetUnavailableView;

    public static HeartRateFragment newInstance(String title) {
        HeartRateFragment fragment = new HeartRateFragment();
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
        mAverage = mYMax / 2 * 10;
        mPhonyRate = mAverage;
        mLastRate = mAverage;
        mChartHandler = new ChartHandler();
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsHeartRatePause = false;
        if(!mIsDetecting && mIsOneKeyOpen){
            if(mDetectLoadingLayout.getAlpha() == 0.0f){
                mDetectLoadingLayout.setAlpha(1.0f);
                mReDetectButton.setVisibility(View.GONE);
                mReDetectBgImageView.setVisibility(View.GONE);
            }
            mHeartRateCirclebar.performClick();
            mIsOneKeyOpen = false;
        }
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        mNetUnavailableView = (RelativeLayout) view.findViewById(R.id.rlayout_network_unavailable);
        mTitleTextView = (TextView) view.findViewById(R.id.tv_title);
        mTitleTextView.setText(getResources().getString(R.string.network_unavailable));
        
        mReDetectButton = (Button) view.findViewById(R.id.btn_re_detect);
        mReDetectButton.setOnClickListener(this);
        mReDetectButton.setVisibility(View.GONE);
        mHeartRateTextView = (TextView) view.findViewById(R.id.tv_heart_rate);
        mLowPressureTextView = (TextView) view.findViewById(R.id.tv_low_blood_pressure);
        mHighPressureTextView = (TextView) view.findViewById(R.id.tv_high_blood_pressure);
        mHighTipTextView = (TextView) view.findViewById(R.id.tv_high_pressure_tip);
        mReDetectBgImageView = (ImageView) view.findViewById(R.id.iv_re_detect_bg);
        mDetectLoadingLayout = (RelativeLayout) view.findViewById(R.id.rlayout_detect_loading);

        mHeatRateDetectingTV = (TextView) view.findViewById(R.id.tv_heart_rate_detecting);

        mHeartRateCirclebar = (HeartRateCircleBar) view
                .findViewById(R.id.circlebar_progress_heart_rate);
        mHeartRateCirclebar.setOnClickListener(this);

        // 这里获得main界面上的布局，下面会把图表画在这个布局里面
        mHeartRateLayout = (LinearLayout) view.findViewById(R.id.llayout_chart_heart_rate);
        
        // 初始化图表
        initChart(0, mXMax, mYMin, mYMax);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_heart_rate;
    }

    @Override
    public void fetchData() {

    }

    public void refreshUI() {

    }

    public void setHeartRateListener(HeartRateListener heartRateListener) {
        mHeartRateListener = heartRateListener;
    }

    public interface HeartRateListener {
        public void detectHeartRate();

        public void sendHeartRate(HeartRate heartRate);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.circlebar_progress_heart_rate:
                mHeartRateCirclebar.setClickable(false);
                LogUtil.i("debug", "================@@===onClick===========>>");
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                
                mHeartRateCirclebar.setMax(Constants.HEART_RATE_MAX_PREGRESS);
                mHeartRateCirclebar.setProgress(0, 1);
                mHeartRateCirclebar.startCustomAnimation();
                mHeatRateDetectingTV.setVisibility(View.VISIBLE);
                flag = true;
                mChartThread = new Thread(runnable);//定期更新平均值的线程启动
                mChartThread.start();
                resetHeartRateChart();
                // 开启检测
                FileUtil.startDetectHeartRate(mActivity);
                mIsDetecting = true;
                mFromTime = System.currentTimeMillis();
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // 检测数据
                        final int[] heartrate = FileUtil.getHeartRateData(mActivity);
                        progress = heartrate[2];
                        rate = heartrate[4];
                        if ((progress == 10 && mTimer != null) || heartrate[5] == 1) {
                            mTimer.cancel();
                            mTimer = null;
                        }
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progress > 0 && !mIsHeartRatePause) {
                                    mHeartRateCirclebar.setProgress(progress, 1);
                                }
                                
                                if(rate > 0 && !mIsHeartRatePause){
                                    mHeartRateCirclebar.setRateCount((int)rate, 1);
                                }
                                if(heartrate[5] == 1){
                                   Toast.makeText(mActivity, R.string.detect_error, Toast.LENGTH_LONG).show();  
                                   mHeartRateCirclebar.reset();
                                   mHeartRateCirclebar.setClickable(true);
                                   mHeatRateDetectingTV.setVisibility(View.GONE);
                                   flag = false;//停止绘制心电图线程
                                   mIsDetecting = false;
                                   mActivity.getWindow().clearFlags(
                                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                }
                                if (progress == 10) {
                                    mToTime = System.currentTimeMillis();
                                    mHeartRateCirclebar.reset();
                                    mHeartRateCirclebar.setClickable(true);
                                    resetHeartRateChart();
                                    flag = false;
                                    mIsDetecting = false;
                                    mHeatRateDetectingTV.setVisibility(View.GONE);
                                    mReDetectButton.setVisibility(View.VISIBLE);
                                    mReDetectBgImageView.setVisibility(View.VISIBLE);
                                    // 更新并上传心率血压
                                    updateHeartRate(Math.abs(heartrate[4]) + "", Math.abs(heartrate[0]) + "",
                                            Math.abs(heartrate[1]) + "");
                                    startDetectCompleteAnimation();
                                    mActivity.getWindow().clearFlags(
                                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                }
                            }

                        });
                    }
                }, 500, 50);
                 
                break;
            case R.id.btn_re_detect:
                startReDectAnimation();
                mReDetectButton.setVisibility(View.GONE);
                mReDetectBgImageView.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * 初始化图表
     */
    private void initChart(int minX, int maxX, int minY, int maxY) {
        // 这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
        series = new XYSeries("");
        // 创建一个数据集的实例，这个数据集将被用来创建图表
        mDataset = new XYMultipleSeriesDataset();
        // 将点集添加到这个数据集中
        mDataset.addSeries(series);

        // 以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
        int lineColor = Color.WHITE;
        PointStyle style = PointStyle.CIRCLE;
        mRenderer = buildRenderer(lineColor, style, true);

        // 设置好图表的样式
        setChartSettings(mRenderer,
                minX, maxX, // x轴最小最大值
                minY, maxY, // y轴最小最大值
                Color.BLACK, // 坐标轴颜色
                Color.WHITE// 标签颜色
        );

        // 生成图表
        mHeartRateChart = ChartFactory.getLineChartView(mActivity, mDataset, mRenderer);

        mHeartRateLayout.removeView(mHeartRateChart);
        // 将图表添加到布局中去
        mHeartRateLayout.addView(mHeartRateChart, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
    }

    protected XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        // 设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(color);
        r.setPointStyle(style);
        r.setFillPoints(fill);
        r.setLineWidth(1);// 这是线宽
        renderer.addSeriesRenderer(r);
        return renderer;
    }

    /**
     * 初始化图表
     * 
     * @param renderer
     * @param xMin
     * @param xMax
     * @param yMin
     * @param yMax
     * @param axesColor
     * @param labelsColor
     */
    protected void setChartSettings(XYMultipleSeriesRenderer renderer,
            double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
        renderer.setXAxisMin(xMin);// 设置x轴范围的起始点
        renderer.setXAxisMax(xMax);// 设置X轴范围的终点
        renderer.setYAxisMin(yMin);// 设置Y轴范围的起始点
        renderer.setYAxisMax(yMax);// 设置Y轴范围的终点
        renderer.setLabelsColor(Color.YELLOW);// 坐标颜色
        renderer.setAxesColor(axesColor);// 坐标轴颜色
        renderer.setShowGrid(false);// 是否显示网格
        renderer.setXLabels(10);// 设置x轴显示20个点,根据setChartSettings的最大值和最小值自动计算点的间隔
        renderer.setYLabels(20);// 设置y轴显示20个点,根据setChartSettings的最大值和最小值自动计算点的间隔
        renderer.setLabelsTextSize(5);// 设置坐标字号
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setXLabelsAlign(Align.CENTER);// 刻度线与刻度标注之间的相对位置关系
        renderer.setPointSize((float) 0);
        renderer.setShowLegend(false);// 设置是否显示图例.说明文字

        renderer.setPanEnabled(false);//设置不允许拖动
        renderer.setZoomEnabled(false, false);// 设置是否允许放大和缩小.
        renderer.setShowAxes(false);// 设置是否显示轴
        renderer.setShowLabels(false);// 设置是否显示坐标
        renderer.setMargins(new int[] {
                0, 0, 0, 0
        });// 设置空白区大小
        renderer.setMarginsColor(Color.TRANSPARENT);// 设置空白区颜色
        renderer.setLabelsTextSize(0);// 设置坐标轴的字体大小
        renderer.setAxisTitleTextSize(0);// 设置坐标轴标题的字体大小
    }

    /**
     * 更新图表的函数，其实就是重绘
     */
    private void updateChart() {
        if (rate == 0) {
            if (mIntervalTime % 800 == 0 && mPhonyRate == mAverage) {
                mPhonyRate = mAverage + 10;
            } else if (mPhonyRate == mAverage + 10) {
                mPhonyRate = mAverage - 10;
                mIntervalTime = 0;
            } else if (mPhonyRate == mAverage - 10) {
                mPhonyRate = mAverage;
                mIntervalTime = 0;
            }
            mAddY = mPhonyRate / 10;// 需要增加的值
            mIsFirstJitter = true;
        } else {
            if (rate > mAverage) {
                if (mIsFirstJitter || mIntervalTime % 1000 == 0 || mJitterCount != 0) {
                    double differ = rate - mAverage;
                    if (mJitterCount == 0) {
                        if (differ < 10) {
                            rate = mAverage + differ * 5;
                        } else {
                            rate = mAverage + differ * 1.2;
                        }
                        mLastRate = rate;
                        mJitterCount++;
                    } else if (mJitterCount == 1) {
                        if (differ < 10) {
                            rate = mAverage - differ * 5;
                        } else {
                            rate = mAverage - differ * 1.2;
                        }
                        mLastRate = rate;
                        mJitterCount++;
                        mIntervalTime = 0;
                    } else {
                        mJitterCount = 0;
                        mIsFirstJitter = false;
                        mLastRate = mAverage;
                        mIntervalTime = 0;
                    }
                }
            } else {
                if (mIsFirstJitter || mIntervalTime % 1000 == 0 || mJitterCount != 0) {
                    double differ = rate - mAverage;
                    if (mJitterCount == 0) {
                        if (differ < 10) {
                            rate = mAverage - differ * 5;
                        } else {
                            rate = mAverage - differ * 1.2;
                        }
                        mLastRate = rate;
                        mJitterCount++;
                    } else if (mJitterCount == 1) {
                        if (differ < 10) {
                            rate = mAverage + differ * 5;
                        } else {
                            rate = mAverage + differ;
                        }
                        mLastRate = rate;
                        mJitterCount++;
                        mIntervalTime = 0;
                    } else {
                        mJitterCount = 0;
                        mIsFirstJitter = false;
                        mLastRate = mAverage;
                        mIntervalTime = 0;
                    }
                }
            }
            mAddY = mLastRate / 10;// 需要增加的值
        }
        
        // 移除数据集中旧的点集
        if(mDataset != null){
            mDataset.removeSeries(series); 
        }

        // 判断当前点集中到底有多少点，因为屏幕总共只能容纳100个，所以当点数超过100时，长度永远是100
        int length = series.getItemCount();
        if (length > 5000) {// 设置最多5000个数
            length = 5000;
        }

        // 注释掉的文字为window资源管理器效果

        // 将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
        // for (int i = 0; i < length; i++) {
        // xv[i] = (int) series.getX(i) + 1;
        // yv[i] = (int) series.getY(i);
        // }

        // 点集先清空，为了做成新的点集而准备
        // series.clear();

        // 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
        // 这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
        // 每一个新点坐标都后移一位
        series.add(mAddX++, mAddY);// 最重要的一句话，以xy对的方式往里放值
        // for (int k = 0; k < length; k++) {
        // series.add(xv[k], yv[k]);//把之前的数据放进去
        // }
        if (mAddX > mXMax) {
            mRenderer.setXAxisMin(mAddX - mXMax);
            mRenderer.setXAxisMax(mAddX);
        }

        // 重要：在数据集中添加新的点集
        mDataset.addSeries(series);

        // 视图更新，没有这一步，曲线不会呈现动态
        // 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
        if(mHeartRateChart != null){
            mHeartRateChart.invalidate(); 
        }
    }
    Runnable runnable = new Runnable() {
        
        @Override
        public void run() {
            while(flag){
            try {
                Thread.sleep(INTERVAL);//每隔固定时间求平均数
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                mChartThread = new Thread(runnable);
                mChartThread.start();
            }
            mIntervalTime += INTERVAL;
            mChartHandler.sendEmptyMessage(1);
            }
        }
    };
    
    /**
     * 每隔固定时间更新心率图表的操作
     * @author love fang
     *
     */
    class ChartHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            updateChart();//更新图表，非常重要的方法
        }
    }

    public void resetHeartRateChart(){
        mAddX = -1;
        mAddY = 0;
        mJitterCount = 0;//心率抖动次数
        mIsFirstJitter = true;//是否第一次心率抖动
        mIntervalTime = 0;
        rate = 0;
        mPhonyRate = mAverage;
        mLastRate = mAverage;
        series.clear();
        mRenderer.setXAxisMin(0);
        mRenderer.setXAxisMax(mXMax);
    }
    
    private void updateHeartRate(String rate, String high, String low) {
        String highStr = mActivity.getResources().getString(R.string.blood_pressure_mmhg, high);
        String lowStr = mActivity.getResources().getString(R.string.blood_pressure_mmhg, low);
        String rateStr = mActivity.getResources().getString(R.string.heart_rate_bmg, rate);
        mHeartRateTextView.setText(rateStr);
        mHighPressureTextView.setText(highStr);
        mLowPressureTextView.setText(lowStr);
        if(Integer.valueOf(high) > HIGH){
            mHighTipTextView.setVisibility(View.VISIBLE);
        }else{
            mHighTipTextView.setVisibility(View.GONE);
        }
        HeartRate heartRate = new HeartRate();
        heartRate.setRate(rate);
        heartRate.setHigh(high);
        heartRate.setLow(low);
        heartRate.setFromTime(mFromTime + "");
        heartRate.setToTime(mToTime + "");
        mHeartRateListener.sendHeartRate(heartRate);
    }

    private void startReDectAnimation() {
        ObjectAnimator.ofFloat(mDetectLoadingLayout, "alpha", 0.0F, 1.0F)
             .setDuration(1000)
             .start();
    }

    public void startDetectCompleteAnimation() {
        ObjectAnimator
                 .ofFloat(mDetectLoadingLayout, "alpha", 1.0F, 0.0F)
                .setDuration(1000)
                 .start();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mHeatRateDetectingTV.setVisibility(View.GONE);
        mHeartRateCirclebar.reset();
        mHeartRateCirclebar.setClickable(true);
        resetHeartRateChart();
        FileUtil.stopDetectHeartRate(mActivity);
        mIsDetecting = false;
        flag = false;
        mIsHeartRatePause = true;
    }

    public boolean ismIsDetecting() {
        return mIsDetecting;
    }

    public void setmIsOneKeyOpen(boolean mIsOneKeyOpen) {
        this.mIsOneKeyOpen = mIsOneKeyOpen;
    }
    
    public void showNetUnavailable(boolean isNetUnAvailable){
        if(mNetUnavailableView != null){
            if(isNetUnAvailable){
                mNetUnavailableView.setVisibility(View.VISIBLE);
            }else{
                mNetUnavailableView.setVisibility(View.GONE);
            } 
        }
    }
}
