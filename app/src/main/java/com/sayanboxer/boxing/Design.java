package com.sayanboxer.boxing;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class Design {
    public static final int BG = Color.rgb(8,15,25), CARD = Color.rgb(22,33,47), CARD2 = Color.rgb(31,45,61);
    public static final int WHITE = Color.rgb(244,248,255), MUTED = Color.rgb(151,167,184);
    public static final int RED = Color.rgb(245,85,63), ORANGE = Color.rgb(255,163,79), GREEN = Color.rgb(58,214,167);
    private Design(){}
    public static int dp(Context c, int n){return (int)(n*c.getResources().getDisplayMetrics().density+0.5f);}
    public static GradientDrawable shape(int color, int radius){ GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d; }
    public static GradientDrawable border(int color, int radius, int strokeColor){ GradientDrawable d=shape(color,radius); d.setStroke(2,strokeColor); return d; }
    public static TextView text(Context c,String s,int sp,int color,boolean bold){ TextView t=new TextView(c);t.setText(s);t.setTextSize(sp);t.setTextColor(color); t.setGravity(Gravity.CENTER_VERTICAL);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t; }
    public static LinearLayout vertical(Context c){LinearLayout v=new LinearLayout(c);v.setOrientation(LinearLayout.VERTICAL);return v;}
    public static LinearLayout horizontal(Context c){LinearLayout v=new LinearLayout(c);v.setOrientation(LinearLayout.HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    public static LinearLayout.LayoutParams lp(Context c,int w,int h,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w<0?w:dp(c,w),h<0?h:dp(c,h));p.topMargin=dp(c,top);return p;}
    public static void pad(View v,int a,int b,int c,int d){Context x=v.getContext();v.setPadding(dp(x,a),dp(x,b),dp(x,c),dp(x,d));}
    public static Button button(Context c,String label,int bg){ Button b=new Button(c); b.setText(label);b.setTextSize(14);b.setAllCaps(false);b.setTextColor(WHITE);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(shape(bg,dp(c,15)));b.setMinHeight(0); b.setMinimumHeight(0); b.setElevation(0); return b; }
    public static LinearLayout card(Context c){LinearLayout x=vertical(c);x.setBackground(shape(CARD,dp(c,20)));pad(x,18,17,18,17);return x;}
    public static TextView label(Context c,String s){return text(c,s,13,MUTED,false);}
    public static TextView title(Context c,String s){return text(c,s,24,WHITE,true);}
    public static LinearLayout.LayoutParams weight(Context c){return new LinearLayout.LayoutParams(0,dp(c,50),1);}
    public static TextView section(Context c,String s){ TextView x=text(c,s,19,WHITE,true); x.setPadding(0,dp(c,15),0,dp(c,7));return x;}
    /** Keep headers/buttons outside Android 15/16's mandatory edge-to-edge system bars. */
    public static void safeArea(View root){
        final int l=root.getPaddingLeft(), t=root.getPaddingTop(), r=root.getPaddingRight(), b=root.getPaddingBottom();
        root.setOnApplyWindowInsetsListener((v,insets)->{
            int top,bottom;
            if(Build.VERSION.SDK_INT>=30){
                android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());top=bars.top;bottom=bars.bottom;
            }else{top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();}
            v.setPadding(l,t+top,r,b+bottom);
            return insets;
        });
    }
    public static ScrollView scroll(Context c){ ScrollView s=new ScrollView(c);s.setFillViewport(false);s.setVerticalScrollBarEnabled(false);return s; }
}
