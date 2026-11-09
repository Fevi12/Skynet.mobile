package no.skynet.mobile;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import android.graphics.Color;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
  static final int SPEECH=4107;
  TextToSpeech tts; boolean ttsReady=false, auditOn=true;
  EditText input; TextView output; WebView web;
  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
  TextView label(String s,int size){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(size);v.setPadding(dp(5),dp(9),dp(5),dp(9));return v;}
  Button btn(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setOnClickListener(v->r.run());return b;}
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(5,12,22));getWindow().setNavigationBarColor(Color.rgb(5,12,22));tts=new TextToSpeech(this,x->{if(x==TextToSpeech.SUCCESS){tts.setLanguage(new Locale("nb","NO"));ttsReady=true;}});home();audit("app_started");}
  void home(){
    LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(18),dp(14),dp(18),dp(24));l.setBackgroundColor(Color.rgb(5,12,22));
    l.addView(label("SKYNET",30));l.addView(label("PERSONLIG ASSISTENT • VERSJON 1.1",13));l.addView(label("Klar • Android "+android.os.Build.VERSION.RELEASE,14));
    l.addView(label("Skriv eller snakk til Skynet",18));input=new EditText(this);input.setHint("Skriv en kommando …");input.setTextColor(Color.WHITE);input.setHintTextColor(Color.LTGRAY);input.setMinLines(2);input.setBackgroundColor(Color.rgb(22,34,49));l.addView(input,new LinearLayout.LayoutParams(-1,dp(82)));
    l.addView(btn("🎙 Snakk til SKYNET",this::listen));l.addView(btn("Kjør kommando",()->command(input.getText().toString())));
    output=label("Hei! Trykk mikrofonen eller skriv «hjelp».",16);output.setBackgroundColor(Color.rgb(17,29,43));l.addView(output);
    l.addView(label("Personvern",18));CheckBox cb=new CheckBox(this);cb.setText("Lokal hendelseslogg (valgfritt)");cb.setTextColor(Color.WHITE);cb.setChecked(true);cb.setOnCheckedChangeListener((v,on)->{auditOn=on;if(on)audit("audit_enabled");});l.addView(cb);
    l.addView(label("Ingen alltid-lytting. Mikrofonen starter bare etter trykk. Ingen sensordata sendes ut.",12));
    l.addView(btn("Åpne lokalt SKYNET-dashboard",this::dashboard));l.addView(btn("Hjelp",()->command("hjelp")));
    ScrollView sc=new ScrollView(this);sc.addView(l);setContentView(sc);
  }
  void listen(){Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"nb-NO");i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Snakk til SKYNET");try{startActivityForResult(i,SPEECH);}catch(Exception e){reply("Taletjeneste mangler. Skriv kommandoen i feltet.");}}
  @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==SPEECH&&c==RESULT_OK&&d!=null){ArrayList<String>a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(a!=null&&!a.isEmpty()){input.setText(a.get(0));command(a.get(0));}}}
  void command(String raw){String q=raw==null?"":raw.trim(),s=q.toLowerCase(new Locale("nb","NO"));if(q.isEmpty()){reply("Skriv eller si en kommando.");return;}audit("command:"+q);
    if(s.contains("hjelp"))reply("Jeg kan status, personvern, les opp og åpne dashboard. Ukjente kommandoer utføres ikke.");
    else if(s.contains("status"))reply("SKYNET er klar. Android "+android.os.Build.VERSION.RELEASE+". Lokal logg "+(auditOn?"på":"av")+".");
    else if(s.contains("personvern")||s.contains("sikkerhet"))reply("Ingen bakgrunnslytting. Mikrofon brukes manuelt. Loggen er lokal. Ingen sensordata sendes ut.");
    else if(s.contains("dashboard")||s.contains("åpne skynet"))dashboard();
    else if(s.startsWith("les opp")){String t=q.length()>7?q.substring(7).trim():"SKYNET er klar.";reply(t);if(ttsReady)tts.speak(t,TextToSpeech.QUEUE_FLUSH,null,"skynet");}
    else reply("Mottatt: «"+q+"». Denne versjonen utfører bare godkjente lokale kommandoer. Ingen vilkårlig terminalkjøring.");
  }
  void reply(String s){if(output!=null)output.setText(s);audit("response");}
  void dashboard(){audit("dashboard_open");web=new WebView(this);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.setWebViewClient(new WebViewClient());web.loadUrl("http://127.0.0.1:8766");setContentView(web);}
  void audit(String e){if(!auditOn)return;try{FileWriter w=new FileWriter(new File(getFilesDir(),"skynet-audit.log"),true);w.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date())+" "+e.replace("\n"," ")+"\n");w.close();}catch(Exception ignored){}}
  @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else{web=null;home();}}
  @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}if(web!=null)web.destroy();super.onDestroy();}
}
