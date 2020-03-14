package com.lsa.flip;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView tvOutput1;
    private TextView tvOutput2;
    private TextView tvOutTimes;
    private SensorManager sensorManager;
    private Sensor gameSensor;
    private Sensor gyroscopeSensor;
    private int rotationTimes = 0;
    private int flipTimes = 0;
    private int rotationHold = 0;
    private float lightLx = 0;
    private float lightLx_Last = 0;
    private float lightLx_Low = 0;
    private int upDown = 0; // 1是暗了，0是亮
    private int isRotate = 0; // 1是转了，0是没
    private float rotateX = 0;
    private float rotateY = 0;
    private float rotateZ = 0;
    private float lightGRADIENT = 3;
    private float rotateSTRENGTH = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //输出结果的TextView
        tvOutput1 = findViewById(R.id.tvData1);
        tvOutput2 = findViewById(R.id.tvData2);
        tvOutTimes = findViewById(R.id.tvTimes);

        //获取SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gameSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener(sensorListener, gyroscopeSensor, 300);
        sensorManager.registerListener(sensorListener, gameSensor,300);
        }

    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(sensorListener);
    }

    public void onReset(View view) {
        rotationTimes = 0;
        flipTimes = 0;
        rotationHold = 0;
        lightLx = 0;
        lightLx_Last = 0;
        lightLx_Low = 0;
        upDown = 0;
        isRotate = 0;
        rotateX = 0;
        rotateY = 0;
        rotateZ = 0;
        lightGRADIENT = 3;
        rotateSTRENGTH = 7;
        tvOutput1.setText("light: " + lightLx + "\nlightDarkTriger: " + lightLx_Low
                + "\nupDown: " + upDown + "\nGRADITENT: " + lightGRADIENT);
        tvOutput2.setText("\nrotationTimes: " + rotationTimes);
        tvOutTimes.setText("Flip Times\n" + flipTimes);

    }

    //监听器
    private SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) { //这是陀螺仪的event
                rotateX = event.values[0];
                rotateY = event.values[1];
                rotateZ = event.values[2];
                tvOutput2.setText("\nrotationTimes: " + rotationTimes);
                if (Math.sqrt(Math.pow(rotateX,2)+Math.pow(rotateY,2)+Math.pow(rotateZ,2))
                        > rotateSTRENGTH && isRotate == 0) {
                    isRotate = 1;

                }
                if (Math.sqrt(Math.pow(rotateX,2)+Math.pow(rotateY,2)+Math.pow(rotateZ,2))
                        < rotateSTRENGTH &&  isRotate == 1) {
                    rotationHold = rotationHold + 1;
                    if (rotationHold > 15) {
                        rotationHold = 0;
                        isRotate = 0;
                        rotationTimes = rotationTimes + 1;
                    }

                }

            }

            if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) { //这是游戏传感器的event
                    lightLx = event.values[0]; //获取光线强度
                    if (lightLx_Last - lightLx >= lightGRADIENT && lightLx_Last != 0
                            && isRotate == 1) {
                        //亮度下降将upDown置为1
                        lightLx_Low = lightLx;
                        lightGRADIENT = (float) (lightLx_Last*0.1);
                        if (lightGRADIENT > 50) {
                            lightGRADIENT = 50;
                        }
                        upDown = 1;
                    }
                    if (lightLx - lightLx_Low > lightGRADIENT && lightLx_Low != 0) {
                        //亮度上升将upDown归0，同时增加times次数
                        if (upDown == 1) {
                            flipTimes = flipTimes + 1;
                            upDown = 0;
                        }
                    }
                    tvOutput1.setText("light: " + lightLx + "\nlightDarkTriger: " + lightLx_Low
                            + "\nupDown: " + upDown + "\nGRADITENT: " + lightGRADIENT);
                    tvOutTimes.setText("Flip Times\n" + flipTimes);
                    lightLx_Last = lightLx;
                }



        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public void writeLog(View view) {
        //定义时间格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");

        //获取当前时间
        Date curTime = new Date(System.currentTimeMillis());
        String timeData = simpleDateFormat.format(curTime);

        String context ="Created time: " + timeData + "\n" + tvOutput1.getText();
        String filename = "FlipLog.txt";
        WriteSDFile(context,filename);

    }

//输出到外部sd卡的函数，不用了这个
//    public void writeLog(View view) {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            //Permissions not granted
//            ActivityCompat.requestPermissions(this,
//            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
//            ActivityCompat.requestPermissions(this,
//            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//
//        } else {
//            // Permission has already been granted
//            String filename = "FlipLog.txt";
//
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss");
//            //获取当前时间
//            Date curTime = new Date(System.currentTimeMillis());
//            String timeData = simpleDateFormat.format(curTime);
//
//            String context ="Created time: " + timeData + "\n" + tvOutput1.getText();
////            String context = "test";
//            WriteSDFile(context,filename);
//        }
//    }

    public void WriteSDFile(String context, String filename) {

        try {
            //File sdPath =  Environment.getExternalStorageDirectory(); //安卓10 api不能用
            File sdPath =  getExternalFilesDir(null); //获取Android/data/com.lsa.flip路径
            if (!sdPath.exists()) {
                Toast.makeText(this,"Directory not exist",Toast.LENGTH_SHORT).show();
                return;
            }
            File newFile = new File(sdPath, filename);
            if (newFile.createNewFile()) {
                //Toast.makeText(this, "File created:", Toast.LENGTH_SHORT).show();
            } else{
                //Toast.makeText(this,"File already exists",Toast.LENGTH_SHORT).show();
            }
            FileOutputStream outStream = new FileOutputStream(newFile);
            outStream.write(context.getBytes());
            outStream.close();
            Toast.makeText(this, sdPath + newFile.getName()
                    + " created.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this,"Oops",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void toAbout(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

}
