package cn.icomon.icdemo;

import static cn.icomon.icdevicemanager.model.other.ICConstant.ICPeopleType.ICPeopleTypeNormal;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.icomon.icdevicemanager.ICDeviceManager;
import cn.icomon.icdevicemanager.ICDeviceManagerDelegate;
import cn.icomon.icdevicemanager.ICDeviceManagerSettingManager;
import cn.icomon.icdevicemanager.callback.ICScanDeviceDelegate;
import cn.icomon.icdevicemanager.model.data.ICCoordData;
import cn.icomon.icdevicemanager.model.data.ICKitchenScaleData;
import cn.icomon.icdevicemanager.model.data.ICRulerData;
import cn.icomon.icdevicemanager.model.data.ICSkipData;
import cn.icomon.icdevicemanager.model.data.ICSkipFreqData;
import cn.icomon.icdevicemanager.model.data.ICWeightCenterData;
import cn.icomon.icdevicemanager.model.data.ICWeightData;
import cn.icomon.icdevicemanager.model.data.ICWeightHistoryData;
import cn.icomon.icdevicemanager.model.device.ICDevice;
import cn.icomon.icdevicemanager.model.device.ICDeviceInfo;
import cn.icomon.icdevicemanager.model.device.ICScanDeviceInfo;
import cn.icomon.icdevicemanager.model.device.ICUserInfo;
import cn.icomon.icdevicemanager.model.other.ICConstant;
import cn.icomon.icdevicemanager.model.other.ICDeviceManagerConfig;

public class MainActivity extends Activity implements ICScanDeviceDelegate, ICDeviceManagerDelegate, EventMgr.Event {
    public static String tag = "ICDM_TAG";
    Button btn_scan;
    Button btn_del;
    EditText txt_log;
    ICDevice _device;
    ICScanDeviceInfo _deviceInfo;
    HashMap<Integer, ArrayList<String>> _units = new HashMap<>();
    ICDevice device;
    int height = 170;
    int age = 24;
    int sex = 1;
    ArrayAdapter<String> adapter;
    ArrayList<String> data = new ArrayList<>();
    public static final int REQUEST_PERMISSION_LOCATION = 1;
    public static final int REQUEST_PERMISSION_BLUETOOTH = 2;


    @Override
    public void onCallBack(Object obj) {
        _deviceInfo = (ICScanDeviceInfo) obj;
        if (device == null)
            device = new ICDevice();
        device.setMacAddr(_deviceInfo.getMacAddr());
        btn_scan.setEnabled(false);
        btn_del.setEnabled(true);

        ICDeviceManager.shared().addDevice(device, new ICConstant.ICAddDeviceCallBack() {
            @Override
            public void onCallBack(ICDevice device, ICConstant.ICAddDeviceCallBackCode code) {
                addLog("add device state : " + code);
            }
        });
    }

    void addLog(String log) {
        // String srcLog = txt_log.getText().toString();
        // srcLog += "\r\n";
        // srcLog += log;
        txt_log.setText(log);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_scan = findViewById(R.id.btn_scan);
        btn_del = findViewById(R.id.btn_del);
        txt_log = findViewById(R.id.txt_log);
        txt_log.setCursorVisible(false);
        txt_log.setFocusable(false);

        txt_log.setFocusableInTouchMode(false);

        EventMgr.addEvent("SCAN", this);


        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });

        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device != null) {
                    ICDeviceManager.shared().removeDevice(device, new ICConstant.ICRemoveDeviceCallBack() {
                        @Override
                        public void onCallBack(ICDevice device, ICConstant.ICRemoveDeviceCallBackCode code) {
                            addLog("delete device state : " + code);
                        }
                    });
                }
                btn_scan.setEnabled(true);
                btn_del.setEnabled(false);
            }
        });

        final EditText inputServer = new EditText(this);
        inputServer.setText("170");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Height").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton("Cancel", null);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                height = Integer.parseInt(inputServer.getText().toString());
                updateUserInfo();
            }
        });
        builder.show();

        final EditText inputServer2 = new EditText(this);
        inputServer2.setText("24");
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Age").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer2)
                .setNegativeButton("Cancel", null);
        builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                age = Integer.parseInt(inputServer2.getText().toString());
                updateUserInfo();
            }
        });
        builder2.show();

        final EditText inputServer3 = new EditText(this);
        inputServer3.setText("1");
        AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
        builder3.setTitle("Sex(1:Male,2:Female)").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer3)
                .setNegativeButton("Cancel", null);
        builder3.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                sex = Integer.parseInt(inputServer3.getText().toString());
                updateUserInfo();
            }
        });
        builder3.show();

        if (!checkBlePermission(this.getBaseContext())) {
            requestBlePermission(this);
        } else {
            initSDK();
        }

    }

    public static boolean checkBlePermission(Context context) {
        //Android target >=31(android 12)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }
        //Android target >=23(android 6)&& <31(android 12)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }


    public static void requestBlePermission(Activity activity) {
        //Android target >=31(android 12)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, REQUEST_PERMISSION_BLUETOOTH);
        }
        //如果系统版本 >=23(android 6)&& <31(android 12)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        }

    }

    void initSDK() {
        ICDeviceManagerConfig config = new ICDeviceManagerConfig();
        config.context = this.getApplicationContext();

        // TODO: set user info
        ICUserInfo userInfo = new ICUserInfo();
        userInfo.age = age;
        userInfo.height = height;
        userInfo.sex = ICConstant.ICSexType.ICSexTypeMale;
        userInfo.peopleType = ICPeopleTypeNormal;
        ICDeviceManager.shared().setDelegate(this);
        ICDeviceManager.shared().updateUserInfo(userInfo);

        ICDeviceManager.shared().initMgrWithConfig(config);
    }

    void updateUserInfo() {
        ICUserInfo userInfo = new ICUserInfo();
        userInfo.age = age;
        userInfo.height = height;
        userInfo.sex = ICConstant.ICSexType.ICSexTypeMale;
        userInfo.peopleType = ICPeopleTypeNormal;
        ICDeviceManager.shared().updateUserInfo(userInfo);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_LOCATION||requestCode==REQUEST_PERMISSION_BLUETOOTH) {
            if(checkBlePermission(this))
            {
                initSDK();
            }
        }
    }

    @Override
    public void onScanResult(ICScanDeviceInfo deviceInfo) {

    }

    @Override
    public void onInitFinish(boolean bSuccess) {
        addLog("SDK init result:" + bSuccess);


    }

    @Override
    public void onBleState(ICConstant.ICBleState state) {
        addLog("ble state:" + state);
        final int[] index = { 0 };
        if (state == ICConstant.ICBleState.ICBleStatePoweredOn) {

        }
    }

    @Override
    public void onDeviceConnectionChanged(final ICDevice device, final ICConstant.ICDeviceConnectState state) {
        addLog(device.getMacAddr() + ": connect state :" + state);
    }

    @Override
    public void onNodeConnectionChanged(ICDevice device, int nodeId, ICConstant.ICDeviceConnectState state) {

    }

    @Override
    public void onReceiveWeightData(ICDevice device, ICWeightData data) {

        if (data.isStabilized) {
            if (data.imp != 0) {
                String t = String.format(
                        "bmi=%.2f,body fat=%.2f,muscle=%.2f,water=%.2f,bone=%.2f,protein=%.2f,bmr=%d,visceral=%.2f,skeletal muscle=%.2f,physical age=%d",
                        data.bmi, data.bodyFatPercent, data.musclePercent, data.moisturePercent, data.boneMass,
                        data.proteinPercent, data.bmr, data.visceralFat, data.smPercent, (int) data.physicalAge);
                addLog(t);
            }
        } else {
            addLog(String.format("weight: %.2f", data.weight_kg));
        }

        // addLog(device.getMacAddr() + ": weight data :" + data.weight_kg + " " +
        // data.temperature + " " + data.imp
        // + " " + data.isStabilized);
        if (data.isStabilized) {
            int i = 0;
        }
    }

    @Override
    public void onReceiveKitchenScaleData(ICDevice device, ICKitchenScaleData data) {
        addLog(device.getMacAddr() + ": kitchen data:" + data.value_g + "\t" + data.value_lb + "\t" + data.value_lb_oz
                + "\t" + data.isStabilized);
    }

    @Override
    public void onReceiveKitchenScaleUnitChanged(ICDevice device, ICConstant.ICKitchenScaleUnit unit) {
        addLog(device.getMacAddr() + ": kitchen unit changed :" + unit);
    }

    @Override
    public void onReceiveCoordData(ICDevice device, ICCoordData data) {
        addLog(device.getMacAddr() + ": coord data:" + data.getX() + "\t" + data.getY() + "\t" + data.getTime());

    }

    @Override
    public void onReceiveRulerData(ICDevice device, ICRulerData data) {
        addLog(device.getMacAddr() + ": ruler data :" + data.getDistance_cm() + "\t" + data.getPartsType() + "\t"
                + data.getTime() + "\t" + data.isStabilized());
        if (data.isStabilized()) {
            // demo, auto change device show body parts type
            if (data.getPartsType() == ICConstant.ICRulerBodyPartsType.ICRulerPartsTypeCalf) {
                return;
            }

            ICDeviceManager.shared().getSettingManager().setRulerBodyPartsType(device,
                    ICConstant.ICRulerBodyPartsType.valueOf(data.getPartsType().getValue() + 1),
                    new ICDeviceManagerSettingManager.ICSettingCallback() {
                        @Override
                        public void onCallBack(ICConstant.ICSettingCallBackCode code) {

                        }
                    });
        }
    }

    @Override
    public void onReceiveRulerHistoryData(ICDevice icDevice, ICRulerData icRulerData) {

    }

    @Override
    public void onReceiveWeightCenterData(ICDevice icDevice, ICWeightCenterData data) {
        addLog(device.getMacAddr() + ": center data :L=" + data.getLeftPercent() + "   R=" + data.getRightPercent()
                + "\t" + data.getTime() + "\t" + data.isStabilized());
    }

    @Override
    public void onReceiveWeightUnitChanged(ICDevice icDevice, ICConstant.ICWeightUnit unit) {
        addLog(device.getMacAddr() + ": weigth unit changed :" + unit);
    }

    @Override
    public void onReceiveRulerUnitChanged(ICDevice icDevice, ICConstant.ICRulerUnit unit) {
        addLog(device.getMacAddr() + ": ruler unit changed :" + unit);

    }

    @Override
    public void onReceiveRulerMeasureModeChanged(ICDevice icDevice, ICConstant.ICRulerMeasureMode mode) {
        addLog(device.getMacAddr() + ": ruler measure mode changed :" + mode);

    }

    // eight eletrode scale callback
    @Override
    public void onReceiveMeasureStepData(ICDevice icDevice, ICConstant.ICMeasureStep step, Object data2) {
        switch (step) {
            case ICMeasureStepMeasureWeightData: {
                ICWeightData data = (ICWeightData) data2;
                onReceiveWeightData(device, data);
            }
            break;
            case ICMeasureStepMeasureCenterData: {
                ICWeightCenterData data = (ICWeightCenterData) data2;
                onReceiveWeightCenterData(device, data);
            }
            break;
            case ICMeasureStepAdcStart: {
                addLog(device.getMacAddr() + ": start imp... ");
            }
            break;
            case ICMeasureStepAdcResult: {
                addLog(device.getMacAddr() + ": imp over");
            }
            break;
            case ICMeasureStepHrStart: {
                addLog(device.getMacAddr() + ": start hr");
            }
            break;

            case ICMeasureStepHrResult: {
                ICWeightData hrData = (ICWeightData) data2;
                addLog(device.getMacAddr() + ": over hr: " + hrData.hr);

            }
            break;
            case ICMeasureStepMeasureOver: {
                ICWeightData data = (ICWeightData) data2;
                data.isStabilized = true;
                addLog(device.getMacAddr() + ": over measure");
                onReceiveWeightData(device, data);
            }
            break;

            default:
                break;
        }
    }

    @Override
    public void onReceiveWeightHistoryData(ICDevice icDevice, ICWeightHistoryData icWeightHistoryData) {
        addLog(device.getMacAddr() + ": history weight_kg=" + icWeightHistoryData.weight_kg + ", imp="
                + icWeightHistoryData.imp);
    }

    @Override
    public void onReceiveSkipData(ICDevice icDevice, ICSkipData data) {
        addLog(device.getMacAddr() + ": skip data: mode=" + data.mode + ", param=" + data.setting + ",use_time="
                + data.elapsed_time + ",count=" + data.skip_count);
        if (data.isStabilized) {
            txt_log.setText("");
            StringBuilder freqs = new StringBuilder();
            freqs.append("[");
            for (ICSkipFreqData freqData : data.freqs) {
                freqs.append("dur=").append(freqData.duration).append(", jumpcount=").append(freqData.skip_count)
                        .append(";");
            }
            freqs.append("]");
            addLog(device.getMacAddr() + ": skip data2 : time=" + data.time + " mode=" + data.mode + ", param="
                    + data.setting + ",use_time=" + data.elapsed_time + ",count=" + data.skip_count + ", avg="
                    + data.avg_freq + ", fastest=" + data.fastest_freq + ", freqs=" + freqs);
        }
    }

    @Override
    public void onReceiveHistorySkipData(ICDevice icDevice, ICSkipData icSkipData) {

    }

    @Override
    public void onReceiveBattery(ICDevice device, int battery, Object ext) {

    }

    @Override
    public void onReceiveUpgradePercent(ICDevice icDevice, ICConstant.ICUpgradeStatus icUpgradeStatus, int i) {

    }

    @Override
    public void onReceiveDeviceInfo(ICDevice icDevice, ICDeviceInfo icDeviceInfo) {

    }

    @Override
    public void onReceiveDebugData(ICDevice icDevice, int i, Object o) {

    }

    @Override
    public void onReceiveConfigWifiResult(ICDevice icDevice, ICConstant.ICConfigWifiState icConfigWifiState) {

    }

    @Override
    public void onReceiveHR(ICDevice device, int hr) {

    }

    @Override
    public void onReceiveUserInfo(ICDevice device, ICUserInfo userInfo) {

    }

    @Override
    public void onReceiveUserInfoList(ICDevice icDevice, List<ICUserInfo> list) {

    }

    @Override
    public void onReceiveRSSI(ICDevice device, int rssi) {

    }

    @Override
    public void onReceiveDeviceLightSetting(ICDevice icDevice, Object o) {

    }
}
