package pl.edu.lab4.i256991;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;


public class MainActivity extends AppCompatActivity implements SensorEventListener  {
    private float xPos, xAccel, xVel = 0.0f;
    private float yPos, yAccel, yVel = 0.0f;
    private float xVelMax =80 , yVelMax = 80;
    private float xVelMin = -80, yVelMin = -80;
    private float xMax, yMax;
    private float xReset, yReset;
    private Bitmap ball;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;



    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // setContentView(R.layout.activity_main);
        BallView ballView = new BallView(this);
        setContentView(ballView);

        Point size = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(size);
        xMax = (float) size.x - 100;
        yMax = (float) size.y - 100;

        //save the center of the screen coordinates for reset
        xReset = xMax/2;
        yReset = yMax/2;

        xPos = xReset;
        yPos = yReset;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManager != null;
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            xAccel = sensorEvent.values[0];
            yAccel = -sensorEvent.values[1];
            updateBall();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void updateBall() {
        float frameTime = 0.666f;
        xVel += (xAccel * frameTime);
        yVel += (yAccel * frameTime);

        //in this section we prevent the xVel from exceeding the limits
        //if left unchecked it ends up increasing/decreasing forever
        //and adds significant delay to the app when changing directions
        if (xVel > xVelMax) {
            xVel = xVelMax;
        } else if (xVel < xVelMin) {
            xVel = xVelMin;
        }

        //in this section we prevent the yVel from exceeding the limits
        //if left unchecked it ends up increasing/decreasing forever
        //and adds significant delay to the app when changing directions
        if (yVel > yVelMax) {
            yVel = yVelMax;
        } else if (yVel < yVelMin) {
            yVel = yVelMin;
        }

        float xS = (xVel / 2) * frameTime;
        float yS = (yVel / 2) * frameTime;

        xPos -= xS;
        yPos -= yS;

        //in this section we prevent the xPos from exceeding the limits of the screen
        if (xPos > xMax) {
            xPos = xMax;
        } else if (xPos < 0) {
            xPos = 0;
        }

        //in this section we prevent the yPos from exceeding the limits of the screen
        if (yPos > yMax) {
            yPos = yMax;
        } else if (yPos < 0) {
            yPos = 0;
        }

        //deal with game overs
        if(xPos == 0 | yPos == 0| xPos == xMax | yPos == yMax){
            gameOver();
        }

    }

    private void gameOver(){
        xPos=xReset;
        yPos=yReset;

        xVel=0;
        yVel=0;
        xAccel=0;
        yAccel=0;
        onStop();
        AlertDialog game_over = new AlertDialog.Builder(this)
                .setTitle("GAME OVER")
                .setMessage("The ball hit the edge of the screen!")
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onStart();
                    }
                })
                .setCancelable(false)
                .show();
    }


    private class BallView extends View {

        public BallView(Context context) {
            super(context);
            Bitmap ballSrc = BitmapFactory.decodeResource(getResources(), R.drawable.metal_ball);
            final int dstWidth = 100;
            final int dstHeight = 100;
            ball = Bitmap.createScaledBitmap(ballSrc, dstWidth, dstHeight, true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(ball, xPos, yPos, null);
            invalidate();
        }
    }
}
