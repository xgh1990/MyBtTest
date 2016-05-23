package com.xgh.mybttest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zj.btsdk.BluetoothService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BtdeviceActivity extends Activity implements View.OnClickListener {
    private Context context;
    private TextView discoveryBtnTv,testBtnTv;
    private ListView deviceLv;
    private TextView toastTv;

    private List<BluetoothDevice> bluetoothDevices;
    private BtdeviceAdapter adapter;

    private BluetoothService btService;


    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE://蓝牙状态
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:   //已连接
                            Toast.makeText(context, "已连接",
                                    Toast.LENGTH_SHORT).show();
                            adapter.setConnected(true);
                            break;
                        case BluetoothService.STATE_CONNECTING:  //正在连接
                            Toast.makeText(context, "正在连接",
                                    Toast.LENGTH_SHORT).show();
                            adapter.setConnected(false);
                            break;
                        case BluetoothService.STATE_LISTEN:     //监听连接的到来
                        case BluetoothService.STATE_NONE:
                            Toast.makeText(context, "正在连接2",
                                    Toast.LENGTH_SHORT).show();
                            adapter.setConnected(false);
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                    Toast.makeText(context, "连接已断开",
                            Toast.LENGTH_SHORT).show();
                    adapter.setConnected(false);
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:     //无法连接设备
                    Toast.makeText(context, "无法连接到设备",
                            Toast.LENGTH_SHORT).show();
                    adapter.setConnected(false);
                    break;
            }
            adapter.notifyDataSetChanged();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btdevice);
        context = this;
        findAllViewById();
        initData();
        setListener();
        processLogic();
    }

    /**
     * 寻找控件的方法
     */
    protected void findAllViewById() {
        testBtnTv = (TextView) findViewById(R.id.tv_test);
        discoveryBtnTv = (TextView) findViewById(R.id.tv_discovery);
        deviceLv = (ListView) findViewById(R.id.lv_device);

        toastTv = new TextView(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
        toastTv.setLayoutParams(params);
        toastTv.setGravity(Gravity.CENTER);
        toastTv.setText("没有检测到设备，请刷新…");
        toastTv.setTag("提示");
    }

    /**
     * 初始化数据
     */
    protected void initData() {

        // 注册广播监听搜索设备
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(discoverReceiver, filter);

        // 注册广播监听搜索结束
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(discoverReceiver, filter);

        btService = new BluetoothService(this, handler);

        bluetoothDevices = new ArrayList<>();
        adapter = new BtdeviceAdapter(context, bluetoothDevices);
        deviceLv.setAdapter(adapter);
    }

    /**
     * 为控件设置监听
     */
    protected void setListener() {
        testBtnTv.setOnClickListener(this);
        discoveryBtnTv.setOnClickListener(this);
        deviceLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bluetoothDevices.size()>0){
                    if ("连接".equals(((TextView) view.findViewById(R.id.tv_state)).getText().toString())) {
                        adapter.setConnectedAddress(bluetoothDevices.get(position).getAddress());
                        btService.connect(bluetoothDevices.get(position));
                    } else {
                        BtPrintUtil printUtil = new BtPrintUtil(btService);
                        printUtil.addSeparate(3);
//                        printUtil.addImage(BitmapFactory.decodeResource(getResources(), R.drawable.abc));
                        printUtil.addTitle("进货单",true);
                        printUtil.addSeparate(3);
                        printUtil.addLineText("单号：12345876411");
                        printUtil.addLineText("时间：2016-5-20");
                        printUtil.addLineText("往来单位：北京布业");
                        printUtil.addLineText("仓库：河北仓库");
                        printUtil.addLineText("经手人：张麻子");
                        printUtil.addSplitLine();

                        PrintTableData data = new PrintTableData();
                        data.setNewLineKey("goodsName");
                        List<PrintTableData.HeaderLable> headers = new ArrayList<PrintTableData.HeaderLable>();
                        headers.add(new PrintTableData.HeaderLable("商品","goodsNo",12));
                        headers.add(new PrintTableData.HeaderLable("数量","num",4));
                        headers.add(new PrintTableData.HeaderLable("单价","uPrice",4));
                        headers.add(new PrintTableData.HeaderLable("金额","totalPrice",4));

                        data.setHeaders(headers);
                        List<Map<String,String>> dataList = new ArrayList<Map<String, String>>();
                        for (int i = 0; i < 5; i++) {
                            Map<String,String> map = new HashMap<String, String>();
                            map.put("goodsName","品名"+i);
                            map.put("goodsNo","编号"+i);
                            map.put("uPrice","2"+i);
                            map.put("num",""+(i+2));
                            map.put("totalPrice",""+(i+20));
                            dataList.add(map);
                        }
                        data.setDataList(dataList);
                        printUtil.addTableData(data);

                        printUtil.addLineText("合计数量：");
                        printUtil.addLineText("合计金额：");
                        printUtil.addLineText("制单人：");
                        printUtil.addLineText("备注：");
                        printUtil.addSeparate(6);
                        printUtil.print();

                    }

                }
            }
        });
    }

    /**
     * 逻辑操作，数据请求
     */
    protected void processLogic() {

        if (btService.isAvailable() == false) {//蓝牙不可用
            Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_LONG).show();
            finish();
        }
        if (btService.isBTopen() == false) {//蓝牙未打开，打开蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 101);
        }
        Set<BluetoothDevice> pairedDevices = btService.getPairedDev();
        for (BluetoothDevice device : pairedDevices) {
            bluetoothDevices.add(device);
        }

        if (deviceLv.findViewWithTag("提示") != null) {
            deviceLv.removeHeaderView(toastTv);
        }
        if (bluetoothDevices.size() == 0) {
            deviceLv.addHeaderView(toastTv);
        }
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_test://测试
                btService.sendMessage("这是测试，你懂吗？","GBK");
                Toast.makeText(this, "打印测试…", Toast.LENGTH_LONG).show();
                break;
            case R.id.tv_discovery://搜索
                if (btService.isDiscovering()) {
                    btService.cancelDiscovery();
                }
                btService.startDiscovery();
                Toast.makeText(this, "搜索中…", Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 101://蓝牙打开返回
                if (resultCode == Activity.RESULT_OK) {   //蓝牙已经打开
                    Toast.makeText(this, "蓝牙已打开", Toast.LENGTH_LONG).show();
                } else {//用户不允许打开蓝牙
                    Toast.makeText(this, "不允许打开蓝牙", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private final BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {//发现一个设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!bluetoothDevices.contains(device)) {
                    Toast.makeText(context, "发现新设备" + device.getName(),
                            Toast.LENGTH_SHORT).show();
                    bluetoothDevices.add(device);
                }else {
                    Toast.makeText(context, "发现已有设备" + device.getName(),
                            Toast.LENGTH_SHORT).show();
                }

                if (deviceLv.findViewWithTag("提示") != null) {
                    deviceLv.removeHeaderView(toastTv);
                }
                if (bluetoothDevices.size() == 0) {
                    deviceLv.addHeaderView(toastTv);
                }
                adapter.notifyDataSetChanged();
                //                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                //
                //                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {//搜索结束
                Toast.makeText(context, "搜索结束",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

}
