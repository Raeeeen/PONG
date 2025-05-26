package com.rod.pong;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private View playerPaddle, aiPaddle;
    private ConstraintLayout pongTable;
    private LinearLayout blockMIDDLE, blockTOP, blockBOTTOM, blockRIGHT, blockLEFT;
    private ImageView ball;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        functions();
    }

    private void functions() {
        pongTable = findViewById(R.id.pongTable);
        playerPaddle = findViewById(R.id.playerPaddle);
        aiPaddle = findViewById(R.id.aiPaddle);
        blockMIDDLE = findViewById(R.id.blockMIDDLE);
        blockBOTTOM = findViewById(R.id.blockBOTTOM);
        blockLEFT = findViewById(R.id.blockLEFT);
        blockTOP = findViewById(R.id.blockTOP);
        blockRIGHT = findViewById(R.id.blockRIGHT);
        ball = findViewById(R.id.ball);

        playerPaddle.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        final float speedFactor = 5f;
        final float playerSmoothnessFactor = 0.2f;

        playerPaddle.setOnTouchListener(new View.OnTouchListener() {
            float startX = 0f;
            float startPaddleX = 0f;
            boolean isMoving = false;
            boolean isHandlingTouch = false;
            VelocityTracker velocityTracker;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isHandlingTouch) {
                            startX = event.getX();
                            startPaddleX = playerPaddle.getX();
                            isMoving = true;
                            isHandlingTouch = true;

                            if (velocityTracker == null) {
                                velocityTracker = VelocityTracker.obtain();
                            } else {
                                velocityTracker.clear();
                            }
                            velocityTracker.addMovement(event);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isMoving && isHandlingTouch) {
                            velocityTracker.addMovement(event);
                            velocityTracker.computeCurrentVelocity(1000);
                            float currentX = event.getX();
                            float deltaX = (currentX - startX) * speedFactor;

                            float paddleX = startPaddleX + deltaX;
                            float maxPaddleX = pongTable.getWidth() - playerPaddle.getWidth();

                            paddleX = Math.max(0, Math.min(paddleX, maxPaddleX));

                            float smoothPaddleX = playerPaddle.getX() + playerSmoothnessFactor * (paddleX - playerPaddle.getX());
                            playerPaddle.setX(smoothPaddleX);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isMoving = false;

                        if (velocityTracker != null) {
                            velocityTracker.recycle();
                            velocityTracker = null;
                        }
                        isHandlingTouch = false;
                        break;
                }
                return true;
            }
        });

        final float[] ballSpeedFactor = {15f};
        float ballX = (float) (Math.random() * (pongTable.getWidth() - ball.getWidth()));
        float ballY = (float) (Math.random() * (pongTable.getHeight() - ball.getHeight()));
        final float[] directionX = {(Math.random() > 0.5) ? 1 : -1};
        final float[] directionY = {(Math.random() > 0.5) ? 1 : -1};

        ball.setX(ballX);
        ball.setY(ballY);

        TimerTask ballUpdateTask = new TimerTask() {
            private Handler handler = new Handler(MainActivity.this.getMainLooper());

            @Override
            public void run() {
                float ballX = ball.getX() + (directionX[0] * ballSpeedFactor[0]);
                float ballY = ball.getY() + (directionY[0] * ballSpeedFactor[0]);

                boolean shouldRespawn = false;

                if (ballY + ball.getHeight() >= playerPaddle.getY() && ballX + ball.getWidth() >= playerPaddle.getX() && ballX <= playerPaddle.getX() + playerPaddle.getWidth()) {
                    directionY[0] *= -1;
                    float paddleCenterX = playerPaddle.getX() + playerPaddle.getWidth() / 2f;
                    float ballCenterX = ballX + ball.getWidth() / 2f;
                    directionX[0] = (ballCenterX - paddleCenterX) / (playerPaddle.getWidth() / 2f);
                    ballY = playerPaddle.getY() - ball.getHeight() - 1;
                } else if (ballY >= playerPaddle.getY() + playerPaddle.getHeight()) {
                    shouldRespawn = true;
                }

                if (ballY <= aiPaddle.getY() + aiPaddle.getHeight() && ballX + ball.getWidth() >= aiPaddle.getX() && ballX <= aiPaddle.getX() + aiPaddle.getWidth()) {
                    directionY[0] *= -1;
                    float paddleCenterX = aiPaddle.getX() + aiPaddle.getWidth() / 2f;
                    float ballCenterX = ballX + ball.getWidth() / 2f;
                    directionX[0] = (ballCenterX - paddleCenterX) / (aiPaddle.getWidth() / 2f);
                    ballY = aiPaddle.getY() + aiPaddle.getHeight() + 1;

                } else if (ballY <= aiPaddle.getY() - ball.getHeight()) {
                    shouldRespawn = true;
                }

                if (ballX <= blockLEFT.getRight() && ballY + ball.getHeight() >= blockLEFT.getTop() && ballY <= blockLEFT.getBottom()) {
                    directionX[0] *= -1;
                }

                if (ballX + ball.getWidth() >= blockRIGHT.getLeft() && ballY + ball.getHeight() >= blockRIGHT.getTop() && ballY <= blockRIGHT.getBottom()) {
                    directionX[0] *= -1;
                }


                if (ballY <= blockTOP.getBottom() && ballX + ball.getWidth() >= blockTOP.getLeft() && ballX <= blockTOP.getRight()) {
                    directionY[0] *= -1;
                }

                if (ballY + ball.getHeight() >= blockBOTTOM.getTop() && ballX + ball.getWidth() >= blockBOTTOM.getLeft() && ballX <= blockBOTTOM.getRight()) {
                    directionY[0] *= -1;
                }

                if (shouldRespawn) {
                    ballX = blockTOP.getLeft() + (blockTOP.getWidth() - ball.getWidth()) / 2f;
                    ballY = blockTOP.getTop() + (blockTOP.getHeight() - ball.getHeight()) / 2f;
                    directionX[0] = (Math.random() > 0.5) ? 1 : -1;
                    directionY[0] = (Math.random() > 0.5) ? -1 : 1;
                }

                ball.setX(ballX);
                ball.setY(ballY);

                }

        };

        Timer ballUpdateTimer = new Timer();
        ballUpdateTimer.schedule(ballUpdateTask, 0, 16);
    }



}
