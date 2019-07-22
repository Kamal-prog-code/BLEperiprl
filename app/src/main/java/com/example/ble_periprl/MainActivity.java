package com.example.ble_periprl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private BluetoothLeScanner BS;
    private BluetoothManager BM;
    private BluetoothAdapter BA;
    ArrayAdapter<String> listAdapter;
    ArrayList<String> listArray;
    ArrayList<BluetoothDevice> devicesList;
    boolean newDevice = true;
    int bleMACAddressLength = 12;
    ArrayList<String> pairedList;

    Button Scan_on,button2;
    Button Scan_off;
    ListView plv;
    TextView Heading;
    boolean scanningstatus;
    BluetoothDevice SelectedDevice;
    String SelectedDeviceAddress;
    String SelectedDeviceName;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button Scan_on =  (Button) findViewById(R.id.button);
        button2 =(Button) findViewById(R.id.button2);
        Scan_off= (Button) findViewById(R.id.button2);
        plv = (ListView) findViewById(R.id.perlv);
        Heading = (TextView) findViewById(R.id.textView2);



        BM = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BA = BM.getAdapter();
        BS =BA.getBluetoothLeScanner();

        devicesList = new ArrayList<BluetoothDevice>();

        listArray = new ArrayList<String>();

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listArray);
        plv.setAdapter(listAdapter);

        plv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                if(scanningstatus)
                    {
                        StopScanning();
                    }
                SelectedDevice = (BluetoothDevice) devicesList.get(position);
                    SelectedDeviceName = getDeviceName(SelectedDevice.getName());
                    SelectedDeviceAddress = SelectedDevice.getAddress();

                    if(SelectedDeviceName==null)
                    {
                        SelectedDeviceName= SelectedDeviceAddress;
                    }

                        Intent intent = new Intent(getBaseContext(), ControlActivity.class);
                        intent.putExtra("DEVICE_ADDRESS", SelectedDeviceAddress);
                        intent.putExtra("DEVICE_NAME", SelectedDeviceName);
                        intent.putExtra("X",SelectedDevice);
                        startActivity(intent);
                    }
        });

        if(!BA.isEnabled())
        {
            Intent enabint = new Intent(BA.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabint,REQUEST_BT);
        }

        else
        {
            Toast.makeText(getApplicationContext(),"Already Bluetooth is on",Toast.LENGTH_LONG).show();
        }

        Scan_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopScanning();
            }
        });

        Scan_on.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                StartScanning();
            }
        });

        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    public ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            try {
                BluetoothDevice device = result.getDevice();

                String deviceName = getDeviceName(device.getName());

                String deviceMACAddress = device.getAddress();

                boolean isEmptyName = TextUtils.isEmpty(deviceName);
                if (isEmptyName)
                    deviceName = deviceMACAddress;

                newDevice = true;
                for (int i = 0; i < plv.getAdapter().getCount(); i++) {
                    if (deviceName.equals((String) plv.getAdapter().getItem(i).toString())) {
                        newDevice = false;
                        return;
                    }
                }

                if (newDevice) {
                    devicesList.add(device);
                    listArray.add(deviceName);
                    listAdapter.notifyDataSetChanged();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    /*public String getDeviceAddress(String deviceName)
    {
        if(deviceName.length()>bleMACAddressLength) {
            int addressIndex = deviceName.length() - bleMACAddressLength;
            String deviceAddress = deviceName.substring(addressIndex);
            return deviceAddress;
        }
        return null;
    }*/


    public String getDeviceName(String deviceName)
    {
        if(deviceName.length()>bleMACAddressLength) {
            int addressIndex = deviceName.length() - bleMACAddressLength;
            String bleDeviceName = deviceName.substring(0, addressIndex);
            return bleDeviceName;
        }
        return null;
    }

    /*private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(MainActivity.TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: "+errorCode);
        }
    };

    public void startAdvertising() {

        BLA = BA.getBluetoothLeAdvertiser();
        if (BLA == null) {
            Toast.makeText(this, "Failed to create advertiser",Toast.LENGTH_SHORT).show();
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .build();

        BLA
                .startAdvertising(settings, data, mAdvertiseCallback);
    }*/

   @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public  void StartScanning()
    {
        /*ParcelUuid parcelUuid = new ParcelUuid(UUID.fromString("0000a002-0000-1000-8000-00805f9b34fb"));
        ParcelUuid parcelUuidMask = new ParcelUuid( UUID.fromString("0000FFFF-0000-0000-0000-000000000000"));
        ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(parcelUuid, parcelUuidMask).build();
        List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
        scanFilters.add(scanFilter);
        ScanSettings scanSettings = new ScanSettings.Builder().build();*/
        BS.startScan(/*scanFilters,scanSettings,*/mScanCallback);
    }

    public void StopScanning()
    {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                BS.stopScan(mScanCallback);
            }
        });
    }
}