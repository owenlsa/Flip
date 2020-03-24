package com.lsa.flip;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
    private float lastRotateStrength = 0;
    private float[] gameVal_START = new float[4];
    private int isRotate = 0; // 1是转了，0是没
    private int beRotating = 0;// 1是已触发开始计算角度
    private int halfRotate = 0;// 1是转了半圈
    private float rotateX = 0;
    private float rotateY = 0;
    private float rotateZ = 0;
    private static float rotateSTRENGTH = 2;
    private static float ROTATE_ANGLE_THRESHOLD = 45;

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

        sensorManager.registerListener(sensorListener, gyroscopeSensor, sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, gameSensor,sensorManager.SENSOR_DELAY_NORMAL);
        }

    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(sensorListener);
    }

    public void onReset(View view) {
        rotationTimes = 0;
        flipTimes = 0;
        rotationHold = 0;
        lastRotateStrength = 0;
        isRotate = 0; // 1是转了，0是没
        beRotating = 0;// 1是已触发开始计算角度
        halfRotate = 0;// 1是转了半圈
        rotateX = 0;
        rotateY = 0;
        rotateZ = 0;
        tvOutput2.setText("");
        tvOutTimes.setText("Flip Times\n" + flipTimes);

    }

    //监听器
    private SensorEventListener sensorListener = new SensorEventListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) { //这是陀螺仪的event
                rotateX = event.values[0];
                rotateY = event.values[1];
                rotateZ = event.values[2];
                //tvOutput2.setText("\nrotationTimes: " + rotationTimes + "\nlastRotateStrength: " + lastRotateStrength);
                if (Math.sqrt(Math.pow(rotateX,2)+Math.pow(rotateY,2)+Math.pow(rotateZ,2))
                        > rotateSTRENGTH && isRotate == 0) {
                    isRotate = 1;
                    lastRotateStrength = (float) Math.sqrt(Math.pow(rotateX,2)+Math.pow(rotateY,2)+Math.pow(rotateZ,2));

                }
                if (Math.sqrt(Math.pow(rotateX,2)+Math.pow(rotateY,2)+Math.pow(rotateZ,2))
                        < rotateSTRENGTH &&  isRotate == 1) {
                    rotationHold = rotationHold + 1; //一个周期没有转动加速度，累加
                    if (rotationHold > 10) { //多少个周期没有转动加速度
                        rotationHold = 0;
                        isRotate = 0; //转动有加速度的标记归零
                        gameVal_START = new float[4];
                        beRotating = 0; //处于转动的标记归零
                        rotationTimes = rotationTimes + 1;

                    }

                }

            }

            if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) { //这是游戏传感器的event
                float[] gameValues = event.values;

                for (int gameId = 0; gameId < gameValues.length; gameId++) {
                    gameValues[gameId] = (float) Math.toDegrees(Math.asin(event.values[gameId])*2);
                }

                tvOutput1.setText("gameX: " + gameValues[0] + "\ngameY: "
                        + gameValues[1] + "\ngameZ: " +  gameValues[2]);
                tvOutput2.setText("gameX_Start: " + gameVal_START[0] + "\ngameY_Start: "
                        + gameVal_START[1] + "\ngameZ_Start: " +  gameVal_START[2]
                        + "\n beRotating: " +beRotating + "\nisRotate: " + isRotate);


                if (beRotating == 0){  //不处于旋转
                    if (isRotate == 1) {//有转动的加速度且当前并不处于旋转
                        System.out.println("这里重新设了初值");
                        System.arraycopy(gameValues, 0, gameVal_START, 0 ,3);
//                        gameVal_START = gameValues;//数组不是这么赋值的
                        beRotating = 1;
                    }

                } else { //处于旋转状态
                    if (halfRotate == 0) {
                        if ((Math.abs(180 - Math.abs(gameValues[0]-gameVal_START[0])) < ROTATE_ANGLE_THRESHOLD)
                                ||(Math.abs(180 - Math.abs(gameValues[1]-gameVal_START[1])) < ROTATE_ANGLE_THRESHOLD)
                                ||(Math.abs(180 - Math.abs(gameValues[2]-gameVal_START[2])) < ROTATE_ANGLE_THRESHOLD)){
                            //误差范围设置20，这里判断是否转了半圈
                            halfRotate = 1;
                        }
                    } else { //halfRotate为1时，代表已经转了半圈了
                        if ((Math.abs(gameValues[0]-gameVal_START[0]) < ROTATE_ANGLE_THRESHOLD)
                                ||(Math.abs(gameValues[1]-gameVal_START[1]) < ROTATE_ANGLE_THRESHOLD)
                                ||(Math.abs(gameValues[2]-gameVal_START[2]) < ROTATE_ANGLE_THRESHOLD)) {
                            //误差范围设置??，这里判断是否转了半圈
                            halfRotate = 0; //转完半圈
                            beRotating = 0;
                            flipTimes = flipTimes + 1;
                            tvOutTimes.setText("Flip Times\n" + flipTimes);
                        }
                    }

                }
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
