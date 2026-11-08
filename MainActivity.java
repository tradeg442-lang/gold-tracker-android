package com.goldtracker.nativeapp;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    static final int GOLD=Color.rgb(231,184,75), BG=Color.rgb(11,18,32), CARD=Color.rgb(20,31,48),
        TEXT=Color.rgb(237,243,251), MUTED=Color.rgb(154,171,192), GREEN=Color.rgb(49,196,141), RED=Color.rgb(240,107,115);
    DB db; LinearLayout page; TextView monthLabel; String selectedMonth;
    @Override public void onCreate(Bundle b){super.onCreate(b); db=new DB(this); selectedMonth=new SimpleDateFormat("yyyy-MM",Locale.US).format(new Date()); build();}
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);return l;}
    void pad(View v,int x,int y){v.setPadding(dp(x),dp(y),dp(x),dp(y));}
    void card(View v){v.setBackgroundColor(CARD);pad(v,14,13);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(14),dp(6),dp(14),dp(6));page.addView(v,p);}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.rgb(23,32,44));b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GOLD));return b;}
    void build(){
        ScrollView sc=new ScrollView(this);sc.setBackgroundColor(BG);page=col();sc.addView(page);setContentView(sc);
        LinearLayout head=col();pad(head,18,18);head.setBackgroundColor(Color.rgb(16,24,39));
        head.addView(text("GOLD TRACKER ◆",24,GOLD,true));head.addView(text("Jurnal performa XAUUSD · TP +303 pips · SL −303 pips · Maks. 50 trade/bulan",12,MUTED,false));page.addView(head);
        LinearLayout m=col();m.addView(text("Bulan statistik (YYYY-MM)",12,MUTED,false));
        EditText month=new EditText(this);month.setSingleLine(true);month.setText(selectedMonth);month.setTextColor(TEXT);month.setHintTextColor(MUTED);month.setHint("2026-11");m.addView(month);
        Button apply=button("TAMPILKAN BULAN");apply.setOnClickListener(v->{String x=month.getText().toString().trim();if(x.matches("\\d{4}-\\d{2}")){selectedMonth=x;build();}else Toast.makeText(this,"Format bulan harus YYYY-MM",Toast.LENGTH_SHORT).show();});m.addView(apply);card(m);
        renderStats();
        LinearLayout form=col();form.addView(text("Catat posisi setelah close",18,TEXT,true));
        EditText date=new EditText(this);date.setText(new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()));date.setTextColor(TEXT);date.setHint("Tanggal close YYYY-MM-DD");form.addView(date);
        Spinner side=new Spinner(this);side.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"BUY","SELL"}));form.addView(side);
        Spinner result=new Spinner(this);result.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"TP / WIN (+303 pips)","SL / LOSS (-303 pips)"}));form.addView(result);
        Button save=button("SIMPAN TRADE");save.setOnClickListener(v->{
            String d=date.getText().toString().trim(); if(!d.matches("\\d{4}-\\d{2}-\\d{2}")){Toast.makeText(this,"Tanggal gunakan YYYY-MM-DD",Toast.LENGTH_SHORT).show();return;}
            String mo=d.substring(0,7); if(db.count(mo)>=50){Toast.makeText(this,"Batas 50 trade bulan ini tercapai",Toast.LENGTH_LONG).show();return;}
            db.add(d,side.getSelectedItem().toString(),result.getSelectedItemPosition()==0?"WIN":"LOSS");
            selectedMonth=mo;build();Toast.makeText(this,"Trade tersimpan",Toast.LENGTH_SHORT).show();
        });form.addView(save);card(form);
        LinearLayout sim=col();sim.addView(text("Simulasi 50 trade · RR 1:1",18,TEXT,true));
        for(int wr:new int[]{55,60,65,70,75,80}){double wins=50*wr/100.0,loss=50-wins,net=(wins-loss)*303;sim.addView(text(wr+"% WR  |  "+wins+" WIN / "+loss+" LOSS  |  "+String.format(Locale.US,"%+.0f",net)+" pips",14,TEXT,false));}card(sim);
        LinearLayout hist=col();hist.addView(text("Riwayat "+selectedMonth,18,TEXT,true));
        Cursor c=db.list(selectedMonth);if(c.getCount()==0)hist.addView(text("Belum ada trade.",13,MUTED,false));
        while(c.moveToNext()){int id=c.getInt(0);String d=c.getString(1),s=c.getString(2),r=c.getString(3);LinearLayout rr=row();rr.setGravity(Gravity.CENTER_VERTICAL);TextView label=text(d+"   "+s+"   "+r+"   "+(r.equals("WIN")?"+303":"-303"),13,r.equals("WIN")?GREEN:RED,false);rr.addView(label,new LinearLayout.LayoutParams(0,-2,1));Button del=new Button(this);del.setText("Hapus");del.setTextSize(10);del.setOnClickListener(v->{new AlertDialog.Builder(this).setMessage("Hapus trade ini?").setNegativeButton("Batal",null).setPositiveButton("Hapus",(a,b)->{db.delete(id);build();}).show();});rr.addView(del);hist.addView(rr);}
        c.close();card(hist);
        LinearLayout backup=col();backup.addView(text("Data tersimpan lokal di HP ini.",12,MUTED,false));backup.addView(text("Catatan: aplikasi tidak terhubung ke broker. Input dilakukan manual setelah posisi ditutup.",12,MUTED,false));card(backup);
    }
    void renderStats(){
        int total=db.count(selectedMonth),wins=db.wins(selectedMonth),loss=total-wins,net=(wins-loss)*303;double wr=total==0?0:wins*100.0/total;
        LinearLayout s=col();s.addView(text("TOTAL TRADE     "+total+"/50",18,TEXT,true));s.addView(text("WIN RATE     "+String.format(Locale.US,"%.1f",wr)+"%",18,GOLD,true));s.addView(text("WIN / LOSS     "+wins+" / "+loss,17,TEXT,true));s.addView(text("PROFIT BERSIH     "+(net>0?"+":"")+net+" pips",17,net>0?GREEN:net<0?RED:TEXT,true));s.addView(text("Kuota tersisa: "+(50-total),12,MUTED,false));card(s);
    }
    static class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"goldtracker.db",null,1);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE trades(id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, side TEXT, result TEXT)");}
        public void onUpgrade(SQLiteDatabase d,int a,int b){}
        void add(String date,String side,String result){getWritableDatabase().execSQL("INSERT INTO trades(date,side,result) VALUES(?,?,?)",new Object[]{date,side,result});}
        int count(String m){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM trades WHERE substr(date,1,7)=?",new String[]{m});c.moveToFirst();int n=c.getInt(0);c.close();return n;}
        int wins(String m){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM trades WHERE substr(date,1,7)=? AND result='WIN'",new String[]{m});c.moveToFirst();int n=c.getInt(0);c.close();return n;}
        Cursor list(String m){return getReadableDatabase().rawQuery("SELECT id,date,side,result FROM trades WHERE substr(date,1,7)=? ORDER BY date DESC,id DESC",new String[]{m});}
        void delete(int id){getWritableDatabase().delete("trades","id=?",new String[]{String.valueOf(id)});}
    }
}