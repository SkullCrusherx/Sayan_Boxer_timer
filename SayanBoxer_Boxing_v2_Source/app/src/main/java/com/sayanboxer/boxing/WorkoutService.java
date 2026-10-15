package com.sayanboxer.boxing;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class WorkoutService extends Service {
    public static final String START="com.sayanboxer.boxing.START", TOGGLE="com.sayanboxer.boxing.TOGGLE", STOP="com.sayanboxer.boxing.STOP", SKIP="com.sayanboxer.boxing.SKIP", MUTE="com.sayanboxer.boxing.MUTE";
    public static volatile boolean running=false, paused=false, resting=false, voiceEnabled=true;
    public static volatile int round=1, rounds=3, remaining=0, elapsed=0, completedRounds=0;
    public static volatile String currentCombo="GET READY", mode="Number Coach";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Random rng=new Random();
    private int workSec=180,restSec=60,comboGapMs=2000,comboIndex=0,lastNotifiedSecond=-1;
    private boolean inOrder=false, bellEnabled=true,warningEnabled=true,warningPlayed=false,logged=false,ending=false;
    private long phaseEndsAt,pausedAt,startedAt,pausedTotal;
    private int sequenceToken=0,numberIndex=0;
    private String[] combos=BoxingData.DEFAULT, currentDigits=new String[0];
    private MediaPlayer voicePlayer, effectPlayer;
    private PowerManager.WakeLock wakeLock;
    private NotificationManager notificationManager;
    private static final String CHANNEL="sayan_boxer_training_v2";
    private final Runnable heartbeat=new Runnable(){@Override public void run(){tick();if(running)handler.postDelayed(this,100);}};

    @Override public void onCreate(){super.onCreate();
        notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel=new NotificationChannel(CHANNEL,"Boxing workout",NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Boxing timer and number-only voice coach");notificationManager.createNotificationChannel(channel);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent==null)return START_NOT_STICKY;
        String a=intent.getAction();
        if(START.equals(a))begin(intent);
        else if(TOGGLE.equals(a))toggle();
        else if(SKIP.equals(a))skip();
        else if(MUTE.equals(a))mute();
        else if(STOP.equals(a))finish(false);
        return START_NOT_STICKY;
    }
    private void begin(Intent i){
        if(running)finish(false);
        rounds=Math.max(1,Math.min(30,i.getIntExtra("rounds",3)));
        workSec=Math.max(15,Math.min(1200,i.getIntExtra("work",180)));
        restSec=Math.max(5,Math.min(600,i.getIntExtra("rest",60)));
        comboGapMs=Math.max(0,Math.min(60000,i.getIntExtra("combo_gap_ms",2000)));
        voiceEnabled=i.getBooleanExtra("voice_enabled",true);
        bellEnabled=i.getBooleanExtra("bell_enabled",true);
        warningEnabled=i.getBooleanExtra("warning_enabled",true);
        inOrder=i.getBooleanExtra("in_order",false);
        mode=voiceEnabled?"Number Coach":"Timer Only";
        ArrayList<String> cs=BoxingData.combos(this);
        if(cs.isEmpty())for(String c:BoxingData.DEFAULT)cs.add(c);
        combos=cs.toArray(new String[0]);comboIndex=0;
        round=1;completedRounds=0;resting=false;paused=false;logged=false;ending=false;running=true;
        currentCombo="GET READY";remaining=workSec;elapsed=0;pausedTotal=0;
        startedAt=SystemClock.elapsedRealtime();phaseEndsAt=startedAt+workSec*1000L;lastNotifiedSecond=-1;
        PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);
        try{wakeLock=power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SayanBoxer:Training");wakeLock.acquire(4*60*60*1000L);}catch(Exception ignored){}
        if(Build.VERSION.SDK_INT>=29)startForeground(10,notification(),ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        else startForeground(10,notification());
        if(bellEnabled)playEffect(R.raw.round_bell);
        stopVoiceChain();
        if(voiceEnabled)queueNextCombo(1800);
        handler.removeCallbacks(heartbeat);handler.post(heartbeat);
    }
    private void tick(){
        if(!running||paused||ending)return;
        long now=SystemClock.elapsedRealtime();
        elapsed=(int)((now-startedAt-pausedTotal)/1000L);
        remaining=Math.max(0,(int)Math.ceil((phaseEndsAt-now)/1000.0));
        if(!resting&&warningEnabled&&!warningPlayed&&remaining<=10&&remaining>0){warningPlayed=true;playEffect(R.raw.warning_10s);}
        if(now>=phaseEndsAt){advance();return;}
        if(remaining!=lastNotifiedSecond&&remaining%2==0){lastNotifiedSecond=remaining;notificationManager.notify(10,notification());}
    }
    private void advance(){
        stopVoiceChain();
        long now=SystemClock.elapsedRealtime();
        if(!resting){
            completedRounds=round;
            if(bellEnabled)playEffect(R.raw.end_bell);
            if(round>=rounds){ending=true;remaining=0;currentCombo="WORKOUT COMPLETE";handler.postDelayed(()->finish(true),1100);return;}
            resting=true;remaining=restSec;phaseEndsAt=now+restSec*1000L;currentCombo="REST";
        }else{
            round++;resting=false;remaining=workSec;phaseEndsAt=now+workSec*1000L;
            currentCombo="GET READY";warningPlayed=false;
            if(bellEnabled)playEffect(R.raw.round_bell);
            if(voiceEnabled)queueNextCombo(1400);
        }
        lastNotifiedSecond=-1;notificationManager.notify(10,notification());
    }
    /** Called ONLY when a complete combination has finished playing. */
    private void queueNextCombo(long delayMs){
        final int validToken=sequenceToken;
        handler.postDelayed(()->{
            if(validToken!=sequenceToken||!running||paused||resting||!voiceEnabled)return;
            String next=inOrder?combos[(comboIndex++)%combos.length]:combos[rng.nextInt(combos.length)];
            currentCombo=BoxingData.display(next);currentDigits=next.split("-");numberIndex=0;
            playNextNumber(validToken);
        },delayMs);
    }
    private void playNextNumber(int token){
        if(token!=sequenceToken||!running||paused||resting||!voiceEnabled)return;
        if(numberIndex>=currentDigits.length){
            releaseVoice();
            queueNextCombo(comboGapMs);
            return;
        }
        int resource=resourceFor(currentDigits[numberIndex++]);
        if(resource==0){playNextNumber(token);return;}
        releaseVoice();
        try{
            voicePlayer=MediaPlayer.create(this,resource);
            if(voicePlayer==null){handler.postDelayed(()->playNextNumber(token),200);return;}
            voicePlayer.setVolume(1f,1f);
            voicePlayer.setOnCompletionListener(p->{
                if(token!=sequenceToken)return;
                releaseVoice();
                handler.postDelayed(()->playNextNumber(token),95);
            });
            voicePlayer.start();
        }catch(Exception ignored){releaseVoice();handler.postDelayed(()->playNextNumber(token),250);}
    }
    private int resourceFor(String code){
        switch(code){case "1":return R.raw.one;case "2":return R.raw.two;
            case "3":return R.raw.three;case "4":return R.raw.four;
            case "5":return R.raw.five;case "6":return R.raw.six;default:return 0;}
    }
    private void playEffect(int resource){
        releaseEffect();
        try{effectPlayer=MediaPlayer.create(this,resource);if(effectPlayer!=null){
            effectPlayer.setVolume(0.9f,0.9f);
            effectPlayer.setOnCompletionListener(mp->releaseEffect());effectPlayer.start();}}
        catch(Exception ignored){releaseEffect();}
    }
    private void releaseVoice(){if(voicePlayer!=null){try{voicePlayer.reset();voicePlayer.release();}catch(Exception ignored){}voicePlayer=null;}}
    private void releaseEffect(){if(effectPlayer!=null){try{effectPlayer.reset();effectPlayer.release();}catch(Exception ignored){}effectPlayer=null;}}
    private void stopVoiceChain(){sequenceToken++;releaseVoice();}
    private void toggle(){
        if(!running)return;
        if(!paused){paused=true;pausedAt=SystemClock.elapsedRealtime();stopVoiceChain();releaseEffect();currentCombo="PAUSED";}
        else{long shift=SystemClock.elapsedRealtime()-pausedAt;phaseEndsAt+=shift;pausedTotal+=shift;
            paused=false;currentCombo=resting?"REST":"GET READY";
            if(!resting&&voiceEnabled)queueNextCombo(400);
        }
        notificationManager.notify(10,notification());
    }
    private void mute(){
        voiceEnabled=!voiceEnabled;
        BoxingData.prefs(this).edit().putBoolean("cfg_voice",voiceEnabled).apply();
        mode=voiceEnabled?"Number Coach":"Timer Only";
        stopVoiceChain();
        if(running&&!paused&&!resting&&voiceEnabled)queueNextCombo(350);
        notificationManager.notify(10,notification());
    }
    private void skip(){if(running){if(paused)toggle();advance();}}
    private void finish(boolean complete){
        if(!running){stopSelf();return;}
        int seconds=(int)((SystemClock.elapsedRealtime()-startedAt-pausedTotal)/1000L);
        running=false;paused=false;resting=false;handler.removeCallbacks(heartbeat);stopVoiceChain();
        if(!logged){BoxingData.log(this,mode,seconds,completedRounds);logged=true;}
        try{stopForeground(STOP_FOREGROUND_REMOVE);}catch(Exception ignored){}
        if(wakeLock!=null&&wakeLock.isHeld())wakeLock.release();
        stopSelf();
    }
    private PendingIntent pending(String action,int code){Intent i=new Intent(this,WorkoutService.class).setAction(action);return PendingIntent.getService(this,code,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    private Notification notification(){
        Intent screen=new Intent(this,SessionActivity.class);
        PendingIntent content=PendingIntent.getActivity(this,15,screen,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        String subtitle=paused?"Paused":resting?"Rest time":voiceEnabled?"Number voice coach":"Timer only";
        String title="Round "+round+"/"+rounds+" · "+String.format(Locale.US,"%02d:%02d",remaining/60,remaining%60);
        return new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.ic_glove)
            .setContentTitle(title).setContentText(subtitle+" · "+currentCombo).setContentIntent(content)
            .setOngoing(true).setOnlyAlertOnce(true).setCategory(Notification.CATEGORY_SERVICE)
            .addAction(new Notification.Action.Builder(null,paused?"Resume":"Pause",pending(TOGGLE,12)).build())
            .addAction(new Notification.Action.Builder(null,voiceEnabled?"Mute":"Voice on",pending(MUTE,14)).build())
            .addAction(new Notification.Action.Builder(null,"Stop",pending(STOP,13)).build()).build();
    }
    @Override public void onDestroy(){handler.removeCallbacks(heartbeat);stopVoiceChain();releaseEffect();
        if(wakeLock!=null&&wakeLock.isHeld())wakeLock.release();running=false;super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
