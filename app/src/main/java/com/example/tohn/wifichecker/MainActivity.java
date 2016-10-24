package com.example.tohn.wifichecker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
    private int timer_status = 0;
    private String file_name;
    private String dir_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText interval_num = (EditText) findViewById(R.id.interval);
        interval_num.setText("5000");

        Button btn_check = (Button) findViewById(R.id.button_scan);
        View.OnClickListener btb1ClickListener = new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                Button btn_check = (Button) findViewById(R.id.button_scan);
                if (timer_status == 0) {
                    if(makeSaveFile()) {
                        startTimer();
                        btn_check.setText("STOP");
                    }
                }

                if (timer_status == 1) {
                    stopTimer();
                    btn_check.setText("SCAN");
                }
            }
        };
        btn_check.setOnClickListener(btb1ClickListener);
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
            Intent intent1 = new android.content.Intent(this, SettingsActivity.class);
            startActivity(intent1);
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

        File file = new File(dir_name + "/" + file_name);

        String[] aps = new String[apList.size()];
        for(int i=0; i<apList.size(); i++) {
            aps[i] = date + "," + apList.get(i).SSID + "," + apList.get(i).frequency + ", " + apList.get(i).level;

            try {
                FileWriter fw = new FileWriter(file, true);
                fw.write(aps[i]);
                fw.write("\r\n");
                fw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("file_error","FileNotFound");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("file_error","IOException");
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
                        writeScanData();
                        echoScanData();
                        timer_status = 1;
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
        timer_status = 0;
        Toast.makeText(this, "Stop Wi-Fi Logging", Toast.LENGTH_LONG).show();
    }

    private boolean makeSaveFile(){
        Time time = new Time("Asia/Tokyo");
        time.setToNow();
        String path = Environment.getExternalStorageDirectory().getPath();
        File dir = new File(path+"/WiFiChecker");
        dir.mkdir();
        File dir_day = new File(path+"/WiFiChecker/"+ time.year + "-" + (time.month+1) + "-" + time.monthDay);
        dir_day.mkdir();
        dir_name = dir_day.getPath();

        String now = String.format("%02d%02d%02d", time.hour, time.minute, time.second);
        file_name = now + ".csv";
        File file = new File(dir_day.getAbsolutePath() + "/" + file_name);

        try{
            return file.createNewFile();
        }catch(IOException e){
            System.out.println(e);
            Toast.makeText(this, "Failed make File!", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
