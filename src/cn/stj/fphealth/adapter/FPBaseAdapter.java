package cn.stj.fphealth.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import cn.stj.fphealth.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 适配器基类
 * @author jackey
 *
 * @param <T>
 */
public abstract class FPBaseAdapter<T> extends BaseAdapter {

    protected Context mContext;
    protected List<T> datas;
    protected LayoutInflater mInflater;

    /**
     * 显示Toast提示框
     * @param msg 显示提示的字符串
     */
    protected void showToastMsg(String msg)
    {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    }

    public FPBaseAdapter(Context context, List<T> datas) {
        mContext = context;
        this.datas = datas;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        return createView(position, view, parent);
    }

    public abstract View createView(int position, View view, ViewGroup parent);

    public List<T> getDatas() {
        return datas;
    }

    public T getData(int position) {
        return datas.get(position);
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    public void clear() {
        datas.clear();
        notifyDataSetChanged();
    }

    public void remove(int position) {
        datas.remove(position);
        notifyDataSetChanged();
    }
}