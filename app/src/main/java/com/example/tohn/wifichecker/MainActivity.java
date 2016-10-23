package com.example.tohn.wifichecker;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.text.format.Time;
import android.widget.Toast;

import static android.R.id.message;


public class MainActivity extends AppCompatActivity {
    private Timer timer;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText interval_num = (EditText) findViewById(R.id.interval);
        interval_num.setText("5000");

        Button btn_start = (Button) findViewById(R.id.button_scan);
        View.OnClickListener btb1ClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText file_name = (EditText) findViewById(R.id.file_name);
                String file_name_str = file_name.getText().toString();

                if (! file_name_str.equals("")) {
                    startTimer();
                }
            }
        };
        btn_start.setOnClickListener(btb1ClickListener);

        Button btn_stop = (Button) findViewById(R.id.button_stop);
        View.OnClickListener btb2ClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        };
        btn_stop.setOnClickListener(btb2ClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void echoScanData() {
        WifiManager wifi = (WifiManager)getSystemService(WIFI_SERVICE);
        List<ScanResult> apList  = wifi.getScanResults();

        String[] aps = new String[apList.size()];
        for(int i=0; i<apList.size(); i++) {
            aps[i] = apList.get(i).SSID + "\n" + apList.get(i).frequency + "MHz " + apList.get(i).level + "dBm";
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, aps);
        ListView listView = (ListView) findViewById(R.id.list_ap);
        listView.setAdapter(adapter);
        //listView.setOnItemClickListener(this);
    }

    private void writeScanData() {
        WifiManager wifi = (WifiManager)getSystemService(WIFI_SERVICE);
        List<ScanResult> apList  = wifi.getScanResults();

        Time time = new Time("Asia/Tokyo");
        time.setToNow();
        String date = time.year + "/" + (time.month+1) + "/" + time.monthDay + " " + time.hour + ":" + time.minute + ":" + time.second ;

        EditText file_name = (EditText) findViewById(R.id.file_name);
        String fileName = file_name.getText().toString();

        String[] aps = new String[apList.size()];
        for(int i=0; i<apList.size(); i++) {
            aps[i] = date + "," + apList.get(i).SSID + "," + apList.get(i).frequency + ", " + apList.get(i).level;

            try {
                FileOutputStream outStream = openFileOutput(fileName + ".txt", MODE_APPEND|MODE_PRIVATE);
                OutputStreamWriter writer = new OutputStreamWriter(outStream);
                writer.write(aps[i]);
                writer.flush();
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** タイマーを開始する */
    private void startTimer() {
        EditText interval_num = (EditText) findViewById(R.id.interval);
        String interval_str = interval_num.getText().toString();

        int firstInterval = 0;
        int interval = Integer.parseInt(interval_str);

        // Timerオブジェクトの生成
        timer = new Timer();

        // タイマーを開始する
        timer.schedule(new TimerTask() {

            // タイマーが満了した
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        WifiManager wifi = (WifiManager)getSystemService(WIFI_SERVICE);
                        wifi.startScan();
                        echoScanData();
                        writeScanData();
                    }
                });
            }

        }, firstInterval, interval);
        Toast.makeText(this, "Start Wi-Fi Logging", Toast.LENGTH_LONG).show();
    }

    /** タイマーを停止する */
    private void stopTimer() {
        this.timer.cancel();
        this.timer.purge();
        this.timer = null;
        Toast.makeText(this, "Stop Wi-Fi Logging", Toast.LENGTH_LONG).show();
    }
}
