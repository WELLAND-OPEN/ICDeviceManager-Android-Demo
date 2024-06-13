package cn.icomon.icdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import cn.icomon.icdevicemanager.ICDeviceManager;
import cn.icomon.icdevicemanager.callback.ICScanDeviceDelegate;
import cn.icomon.icdevicemanager.model.device.ICScanDeviceInfo;
import cn.icomon.icdemo.R;

public class ScanActivity extends Activity implements ICScanDeviceDelegate {

    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<ICScanDeviceInfo> _devices = new ArrayList<>();

    private ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        listView = (ListView) findViewById(R.id.lv_scan);

        adapter = new ArrayAdapter<String>(
                ScanActivity.this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ICDeviceManager.shared().stopScan();
                ICScanDeviceInfo device = _devices.get(i);
                EventMgr.post("SCAN", device);
                finish();
            }
        });


        ICDeviceManager.shared().scanDevice(this);

    }

    @Override
    protected void finalize() throws Throwable {
        ICDeviceManager.shared().stopScan();
        super.finalize();
    }

    @Override
    public void onScanResult(ICScanDeviceInfo deviceInfo) {
        boolean isE = false;
        for (ICScanDeviceInfo deviceInfo1 : _devices) {
            if (deviceInfo1.getMacAddr().equalsIgnoreCase(deviceInfo.getMacAddr())) {
                deviceInfo1.setRssi(deviceInfo.getRssi());
                isE = true;
                break;
            }
        }
        if (!isE) {
            _devices.add(deviceInfo);
        }
        data.clear();
        for (ICScanDeviceInfo deviceInfo1 : _devices) {
            String str = deviceInfo1.getName() + "   " + deviceInfo1.getMacAddr() + "   " + deviceInfo1.getRssi();
            data.add(str);
        }
        adapter.notifyDataSetChanged();
    }
}
