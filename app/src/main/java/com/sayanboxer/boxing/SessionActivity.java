package com.sayanboxer.boxing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.Locale;

public class SessionActivity extends Activity {
    private TextView heading,timer,combo,roundLabel,status,subhead;
    private Button pause,mute;
    private boolean hasStarted=false;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable refresh=new Runnable(){@Override public void run(){update();handler.postDelayed(this,200);}};
    private String format(int sec){return String.format(Locale.US,"%02d:%02d",sec/60,sec%60);}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);getWindow().setStatusBarColor(Design.BG);getWindow().setNavigationBarColor(Design.BG);render();}
    private void render(){
        LinearLayout root=Design.vertical(this);root.setBackgroundColor(Design.BG);Design.pad(root,24,20,24,24);Design.safeArea(root);setContentView(root);
        TextView logo=Design.text(this,"🥊  SAYAN BOXER",16,Design.ORANGE,true);root.addView(logo,Design.lp(this,-1,40,0));
        subhead=Design.text(this,"LIVE BOXING COACH",13,Design.MUTED,true);root.addView(subhead,Design.lp(this,-1,28,4));
        heading=Design.text(this,"ROUND 1",30,Design.WHITE,true);heading.setGravity(Gravity.CENTER);
        root.addView(heading,Design.lp(this,-1,68,34));
        roundLabel=Design.text(this,"ROUND 1 OF 3",14,Design.ORANGE,true);roundLabel.setGravity(Gravity.CENTER);
        root.addView(roundLabel,Design.lp(this,-1,28,2));
        timer=Design.text(this,"02:00",86,Design.WHITE,true);timer.setGravity(Gravity.CENTER);
        root.addView(timer,Design.lp(this,-1,150,13));
        status=Design.text(this,"KEEP YOUR HANDS UP",13,Design.MUTED,true);status.setGravity(Gravity.CENTER);
        root.addView(status,Design.lp(this,-1,30,0));
        LinearLayout comboPanel=Design.card(this);comboPanel.setGravity(Gravity.CENTER);
        comboPanel.setBackground(Design.border(Design.CARD,Design.dp(this,21),Design.RED));
        TextView instruction=Design.text(this,"SPOKEN NUMBERS",12,Design.ORANGE,true);instruction.setGravity(Gravity.CENTER);
        comboPanel.addView(instruction,Design.lp(this,-1,30,0));
        combo=Design.text(this,"READY",34,Design.WHITE,true);combo.setGravity(Gravity.CENTER);
        comboPanel.addView(combo,Design.lp(this,-1,102,0));
        TextView guide=Design.text(this,"Follow the punches you hear",13,Design.MUTED,false);guide.setGravity(Gravity.CENTER);
        comboPanel.addView(guide,Design.lp(this,-1,25,0));
        root.addView(comboPanel,Design.lp(this,-1,-2,30));
        root.addView(new View(this),new LinearLayout.LayoutParams(1,0,1));
        pause=Design.button(this,"Ⅱ  Pause",Design.RED);root.addView(pause,Design.lp(this,-1,56,16));
        pause.setOnClickListener(v->action(WorkoutService.TOGGLE));
        mute=Design.button(this,"🔊  MUTE NUMBERS",Design.CARD2);root.addView(mute,Design.lp(this,-1,48,10));
        mute.setOnClickListener(v->action(WorkoutService.MUTE));
        LinearLayout row=Design.horizontal(this);
        Button skip=Design.button(this,"Skip round  »",Design.CARD2);
        Button finish=Design.button(this,"■  Finish",Design.CARD2);
        LinearLayout.LayoutParams first=Design.weight(this);first.rightMargin=Design.dp(this,7);row.addView(skip,first);
        LinearLayout.LayoutParams second=Design.weight(this);second.leftMargin=Design.dp(this,7);row.addView(finish,second);
        root.addView(row,Design.lp(this,-1,53,13));
        skip.setOnClickListener(v->action(WorkoutService.SKIP));
        finish.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Finish workout?").setMessage("Your completed training will be saved.")
            .setNegativeButton("Keep training",null).setPositiveButton("Finish",(d,w)->{action(WorkoutService.STOP);finish();}).show());
        TextView back=Design.text(this,"⌄  Minimize — workout continues in background",12,Design.MUTED,false);
        back.setGravity(Gravity.CENTER);root.addView(back,Design.lp(this,-1,44,7));back.setOnClickListener(v->finish());
    }
    private void action(String action){startService(new Intent(this,WorkoutService.class).setAction(action));handler.postDelayed(this::update,80);}
    private void update(){
        if(heading==null)return;
        if(!WorkoutService.running){
            if(hasStarted){heading.setText("SESSION ENDED");timer.setText("00:00");status.setText("View your progress on the Stats tab");combo.setText("WELL DONE!");pause.setEnabled(false);}
            else{heading.setText("GET READY");status.setText("Starting your training coach…");}
            return;
        }
        hasStarted=true;pause.setEnabled(true);
        boolean rest=WorkoutService.resting;
        heading.setText(WorkoutService.paused?"PAUSED":rest?"REST & RECOVER":"FIGHT!");
        heading.setTextColor(rest?Design.GREEN:Design.WHITE);
        timer.setText(format(WorkoutService.remaining));
        roundLabel.setText("ROUND "+WorkoutService.round+" OF "+WorkoutService.rounds);
        combo.setText(rest?"REST":WorkoutService.voiceEnabled?WorkoutService.currentCombo:"VOICE OFF");
        status.setText(rest?"NEXT ROUND IS COMING":WorkoutService.voiceEnabled?"MOVE • PUNCH • DEFEND":"TIMER ONLY • VOICE MUTED");
        pause.setText(WorkoutService.paused?"▶  Resume":"Ⅱ  Pause");
        mute.setText(WorkoutService.voiceEnabled?"🔇  MUTE NUMBERS":"🔊  ENABLE NUMBER VOICE");
    }
    @Override public void onResume(){super.onResume();handler.post(refresh);}
    @Override public void onPause(){handler.removeCallbacks(refresh);super.onPause();}
}
