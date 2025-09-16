/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.live2d.demo.full;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.live2d.demo.LAppDefine;
import com.live2d.demo.full.rtc.RtcManager;
import com.live2d.sdk.cubism.framework.CubismFramework;

import kotlin.random.Random;

public class MainActivity extends Activity {

    private RtcManager rtcManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);       // OpenGL ES 2.0を利用

        glRenderer = new GLRenderer();

        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(glSurfaceView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.addView(linearLayout, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT));
//        for (int i = 0; i < 9; i++) {
//            Button button = new Button(this);
//            button.setText("IDEA_"+i);
//            final int finalI = i;
//            button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    LAppLive2DManager live2DManager = LAppLive2DManager.getInstance();
//                    live2DManager.getModel(0).startMotion(LAppDefine.MotionGroup.IDLE.getId(),finalI, LAppDefine.Priority.FORCE.getPriority());
//                }
//            });
//            linearLayout.addView(button, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
//        }

        TextView speakText = new TextView(this);
        speakText.setTextColor(Color.WHITE);
        speakText.setTextSize(15);
        speakText.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 400);
        lp.gravity = Gravity.BOTTOM;
        frameLayout.addView(speakText, lp);


        Button playPcmBtn = new Button(this);
        playPcmBtn.setText("眨眼");
        playPcmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtcManager.playPcm();
            }
        });
        linearLayout.addView(playPcmBtn, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        Button leaveRoom = new Button(this);
        leaveRoom.setText("离开房间");
        leaveRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtcManager.leaveRoom();
            }
        });
        linearLayout.addView(leaveRoom, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));



        Button autoSpeak = new Button(this);
        autoSpeak.setText("托管");
        autoSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtcManager.setAutoSpeak(true);
            }
        });
        linearLayout.addView(autoSpeak, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));




//
//        Button blinkBtn = new Button(this);
//        blinkBtn.setText("眨眼");
//        blinkBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LAppLive2DManager live2DManager = LAppLive2DManager.getInstance();
//                live2DManager.getModel(0).blinkOnce();
//            }
//        });
//        linearLayout.addView(blinkBtn, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
//
//        Button button2 = new Button(this);
//        button2.setText("说话");
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LAppLive2DManager live2DManager = LAppLive2DManager.getInstance();
//                live2DManager.getModel(0).setLipSync(true);
//            }
//        });
//        linearLayout.addView(button2, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
//
//        Button button3 = new Button(this);
//        button3.setText("停止");
//        button3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LAppLive2DManager live2DManager = LAppLive2DManager.getInstance();
//                live2DManager.getModel(0).setLipSync(false);
//            }
//        });
//        linearLayout.addView(button3, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
//
//        Button button4 = new Button(this);
//        button4.setText("加入rtc");
//        button4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rtcManager.createRoom();
//            }
//        });
//        linearLayout.addView(button4, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
//
//        Button button5 = new Button(this);
//        button5.setText("离开rtc");
//        button5.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rtcManager.leaveRoom();
//            }
//        });
//        linearLayout.addView(button5, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        setContentView(frameLayout);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        } else {
            getWindow().getInsetsController().hide(WindowInsets.Type.navigationBars() | WindowInsets.Type.statusBars());

            getWindow().getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        rtcManager = new RtcManager(this, new RtcManager.RtcListener() {
            @Override
            public void talkingText(@NonNull String text) {
                speakText.setText(text);
            }

            @Override
            public void onMoodSwings(int number) {
                LAppLive2DManager live2DManager = LAppLive2DManager.getInstance();
                live2DManager.getModel(0).startMotion(LAppDefine.MotionGroup.IDLE.getId(),number, LAppDefine.Priority.FORCE.getPriority());
            }

            @Override
            public void onStartTalk() {
                LAppLive2DManager live2DManager = LAppLive2DManager.getInstance();
                live2DManager.getModel(0).setLipSync(true);
            }

            @Override
            public void onStopTalk() {
                LAppLive2DManager live2DManager = LAppLive2DManager.getInstance();
                live2DManager.getModel(0).setLipSync(false);
            }
        });

        rtcManager.requestPermission(this);
        rtcManager.createRtc(this);
        rtcManager.createRoom();



        rtcManager.speak("你好呀，第一次见面");
    }

    @Override
    protected void onStart() {
        super.onStart();

        LAppDelegate.getInstance().onStart(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        glSurfaceView.onResume();

        View decor = this.getWindow().getDecorView();
        decor.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        glSurfaceView.onPause();
        if (CubismFramework.isInitialized()) {
            LAppDelegate.getInstance().onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (CubismFramework.isInitialized()) {
            LAppDelegate.getInstance().onStop();
        }

        rtcManager.leaveRoom();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LAppDelegate.getInstance().onDestroy();

    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
//        final float pointX = event.getX();
//        final float pointY = event.getY();
//
//        // GLSurfaceViewのイベント処理キューにタッチイベントを追加する。
//        glSurfaceView.queueEvent(
//            new Runnable() {
//                @Override
//                public void run() {
//                    switch (event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            LAppDelegate.getInstance().onTouchBegan(pointX, pointY);
//                            break;
//                        case MotionEvent.ACTION_UP:
//                            LAppDelegate.getInstance().onTouchEnd(pointX, pointY);
//                            break;
//                        case MotionEvent.ACTION_MOVE:
//                            LAppDelegate.getInstance().onTouchMoved(pointX, pointY);
//                            break;
//                    }
//                }
//            }
//        );
        return super.onTouchEvent(event);
    }

    private GLSurfaceView glSurfaceView;
    private GLRenderer glRenderer;
}
