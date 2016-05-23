package com.xgh.mybttest;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * 蓝牙设备列表适配器
 * Created by Administrator on 2016/5/19.
 */
public class BtdeviceAdapter extends BaseAdapter {
    private Context context;
    private List<BluetoothDevice> bluetoothDevices;
    private String connectedAddress;
    private boolean connected;

    public BtdeviceAdapter(Context context, List<BluetoothDevice> bluetoothDevices) {
        this.context = context;
        this.bluetoothDevices = bluetoothDevices;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setConnectedAddress(String connectedAddress) {
        this.connectedAddress = connectedAddress;
    }

    @Override
    public int getCount() {
        return bluetoothDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_btdevice, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvname.setText(bluetoothDevices.get(position).getName());
        if (connected&&bluetoothDevices.get(position).getAddress().equals(connectedAddress)) {//已绑定
            holder.tvstate.setText("打印测试页");
        } else {
            holder.tvstate.setText("连接");
        }
        return convertView;
    }

    public class ViewHolder {
        public final TextView tvname;
        public final TextView tvstate;
        public final View root;

        public ViewHolder(View root) {
            tvname = (TextView) root.findViewById(R.id.tv_name);
            tvstate = (TextView) root.findViewById(R.id.tv_state);
            this.root = root;
        }
    }
}
