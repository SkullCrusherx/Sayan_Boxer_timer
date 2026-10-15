package com.sayanboxer.boxing;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public final class BoxingData {
    private static final String STORE="boxing_v1";
    public static final String[] DEFAULT={"1-1-2","2-3-2","1-2","1-2-3","1-2-3-2","1-1-2-3","2-3-2-3","1-2-5-2","3-2-3","1-6-3-2"};
    private BoxingData(){}
    public static SharedPreferences prefs(Context c){return c.getSharedPreferences(STORE,Context.MODE_PRIVATE);}
    public static ArrayList<String> combos(Context c){
        ArrayList<String> list=new ArrayList<>(); String raw=prefs(c).getString("combos",null);
        if(raw==null){for(String s:DEFAULT)list.add(s);return list;}
        try {JSONArray a=new JSONArray(raw);for(int i=0;i<a.length();i++){String c=normalize(a.getString(i));if(valid(c)&&!list.contains(c))list.add(c);}}catch(Exception ex){for(String s:DEFAULT)list.add(s);}
        return list;
    }
    public static void saveCombos(Context c,ArrayList<String> cs){JSONArray a=new JSONArray();for(String s:cs)a.put(s);prefs(c).edit().putString("combos",a.toString()).apply();}
    public static String move(String token){
        switch(token.toUpperCase(Locale.ROOT).trim()){
            case "1": return "Jab";case "2":return "Cross";case "3":return "Lead Hook";case "4":return "Rear Hook";
            case "5":return "Lead Uppercut";case "6":return "Rear Uppercut";
            case "SL":return "Slip Left";case "SR":return "Slip Right";
            case "DL":return "Duck Left";case "DR":return "Duck Right";
            case "B":return "Block";case "SP":return "Step Back";default:return "";
        }
    }
    /** Convert user input such as "1 1 2", "1,1,2" or "1-1-2" into 1-1-2. */
    public static String normalize(String raw){
        if(raw==null)return "";
        return raw.trim().replaceAll("[\\s,;]+","-").replaceAll("-+","-").replaceAll("^-|-$","");
    }
    /** The only words the coach is permitted to speak during a round. */
    public static String numberWord(String token){
        switch(token){case "1":return "one";case "2":return "two";case "3":return "three";
            case "4":return "four";case "5":return "five";case "6":return "six";default:return "";}
    }
    public static String display(String combo){return combo.replace('-', ' ');}
    public static String spoken(String combo){
        StringBuilder b=new StringBuilder();
        for(String t:combo.split("-")){String w=numberWord(t);if(!w.isEmpty()){if(b.length()>0)b.append(" … ");b.append(w);}}
        return b.toString();
    }
    public static String prettify(String combo){return combo.replace("SL","↙").replace("SR","↘").replace("DL","↓L").replace("DR","↓R");}
    public static boolean valid(String s){
        if(s==null||s.length()==0||s.length()>47)return false;
        String[] tokens=s.split("-",-1);
        if(tokens.length<2||tokens.length>12)return false;
        for(String t:tokens)if(numberWord(t).isEmpty())return false;
        return true;
    }
    public static void log(Context c,String mode,int seconds,int rounds){
        if(seconds<15)return;
        SharedPreferences p=prefs(c);int count=p.getInt("sessions",0);
        p.edit().putInt("sessions",count+1).putInt("seconds",p.getInt("seconds",0)+seconds).putInt("rounds_done",p.getInt("rounds_done",0)+rounds).apply();
        try{JSONArray old=new JSONArray(p.getString("history","[]"));JSONArray next=new JSONArray();JSONObject event=new JSONObject();
            event.put("time",new SimpleDateFormat("dd MMM yyyy  •  HH:mm", Locale.getDefault()).format(new Date()));event.put("mode",mode);event.put("seconds",seconds);event.put("rounds",rounds);
            next.put(event);for(int i=0;i<Math.min(29,old.length());i++)next.put(old.getJSONObject(i));p.edit().putString("history",next.toString()).apply();
        }catch(JSONException ignored){}
    }
}
