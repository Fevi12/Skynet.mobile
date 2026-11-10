package no.skynet.mobile;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
  static final int SPEECH=4107;
  final int BG=Color.rgb(5,12,22), PANEL=Color.rgb(17,29,43);
  TextToSpeech tts; boolean ttsReady=false, auditOn=true;
  EditText input; TextView output; WebView web; String currentQuery="";
  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
  TextView label(String s,int size){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(size);v.setPadding(dp(6),dp(9),dp(6),dp(9));return v;}
  Button btn(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setOnClickListener(v->r.run());return b;}
  LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(16),dp(12),dp(16),dp(20));l.setBackgroundColor(BG);return l;}
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);tts=new TextToSpeech(this,x->{if(x==TextToSpeech.SUCCESS){int r=tts.setLanguage(new Locale("nb","NO"));ttsReady=r!=TextToSpeech.LANG_MISSING_DATA&&r!=TextToSpeech.LANG_NOT_SUPPORTED;}});home();audit("app_started");}
  void home(){
    LinearLayout root=column();root.addView(label("SKYNET",30));root.addView(label("PERSONLIG ASSISTENT • SEARCH",13));root.addView(label("Klar • Android "+android.os.Build.VERSION.RELEASE,14));
    root.addView(label("Søk på nettet, apper og lokale data",19));
    input=new EditText(this);input.setSingleLine(true);input.setTextColor(Color.WHITE);input.setHintTextColor(Color.LTGRAY);input.setHint("Søk med SKYNET …");input.setPadding(dp(12),dp(10),dp(12),dp(10));input.setBackgroundColor(PANEL);root.addView(input,new LinearLayout.LayoutParams(-1,dp(54)));
    root.addView(btn("⌕  Søk på nettet (Google)",()->searchWeb()));
    root.addView(btn("▦  Finn installerte apper",()->searchApps()));
    root.addView(btn("▤  Søk i lokal logg",()->searchLocal()));
    root.addView(btn("🎙  Stemmesøk",this::listen));
    output=label("Hei! Søk på nettet, finn en app eller søk i SKYNETs lokale logg.",15);output.setBackgroundColor(PANEL);root.addView(output);
    root.addView(label("Hurtigvalg",18));root.addView(btn("Status",()->command("status")));root.addView(btn("Personvern",()->command("personvern")));root.addView(btn("Åpne lokalt SKYNET-dashboard",this::dashboard));
    CheckBox cb=new CheckBox(this);cb.setText("Lokal hendelseslogg");cb.setTextColor(Color.WHITE);cb.setChecked(auditOn);cb.setOnCheckedChangeListener((v,on)->{auditOn=on;if(on)audit("audit_enabled");});root.addView(cb);
    root.addView(label("Ingen bakgrunnslytting. Mikrofonen brukes manuelt. Lokal logg lagres på enheten.",12));
    ScrollView sc=new ScrollView(this);sc.addView(root);setContentView(sc);
  }
  String query(){return input==null?"":input.getText().toString().trim();}
  boolean needQuery(){if(query().isEmpty()){reply("Skriv hva du vil søke etter.");return false;}return true;}
  void searchWeb(){if(!needQuery())return;currentQuery=query();audit("web_search:"+currentQuery);showWeb("https://www.google.com/search?q="+Uri.encode(currentQuery));}
  void showWeb(String url){
    LinearLayout page=column();page.addView(label("SKYNET SEARCH",20));page.addView(btn("Tilbake til søk",()->home()));
    web=new WebView(this);WebSettings st=web.getSettings();st.setJavaScriptEnabled(true);st.setDomStorageEnabled(true);st.setLoadsImagesAutomatically(true);st.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
    web.setBackgroundColor(BG);web.setWebViewClient(new WebViewClient(){
      @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req){String scheme=req.getUrl().getScheme();return !("https".equalsIgnoreCase(scheme)||"http".equalsIgnoreCase(scheme));}
      @Override public void onReceivedSslError(WebView v,android.webkit.SslErrorHandler h,android.net.http.SslError e){h.cancel();}
    });
    page.addView(web,new LinearLayout.LayoutParams(-1,0,1));setContentView(page);web.loadUrl(url);
  }
  void searchApps(){
    if(!needQuery())return;String q=query().toLowerCase(new Locale("nb","NO"));audit("app_search:"+query());
    LinearLayout page=column();page.addView(label("APP-SØK",22));page.addView(label("Installerte apper som matcher «"+query()+"»",14));page.addView(btn("Tilbake til søk",()->home()));
    ScrollView sc=new ScrollView(this);LinearLayout results=new LinearLayout(this);results.setOrientation(1);
    Intent launcher=new Intent(Intent.ACTION_MAIN,null);launcher.addCategory(Intent.CATEGORY_LAUNCHER);
    PackageManager pm=getPackageManager();List<ResolveInfo> found=pm.queryIntentActivities(launcher,0);int count=0;
    Collections.sort(found,(a,b)->String.CASE_INSENSITIVE_ORDER.compare(String.valueOf(a.loadLabel(pm)),String.valueOf(b.loadLabel(pm))));
    for(ResolveInfo ri:found){String name=String.valueOf(ri.loadLabel(pm));String pkg=ri.activityInfo.packageName;if(!name.toLowerCase(new Locale("nb","NO")).contains(q)&&!pkg.toLowerCase(Locale.ROOT).contains(q))continue;
      LinearLayout item=new LinearLayout(this);item.setOrientation(0);item.setPadding(dp(8),dp(8),dp(8),dp(8));item.setBackgroundColor(PANEL);
      ImageView icon=new ImageView(this);icon.setImageDrawable(ri.loadIcon(pm));item.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(42)));
      LinearLayout texts=new LinearLayout(this);texts.setOrientation(1);texts.addView(label(name,16));texts.addView(label(pkg,11));item.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
      item.setOnClickListener(v->{try{Intent app=pm.getLaunchIntentForPackage(pkg);if(app!=null){audit("app_launch:"+pkg);startActivity(app);}else reply("Android tilbyr ingen startaktivitet for denne appen.");}catch(Exception e){reply("Kunne ikke åpne appen.");}});
      results.addView(item,new LinearLayout.LayoutParams(-1,-2));View gap=new View(this);results.addView(gap,new LinearLayout.LayoutParams(1,dp(6)));count++;
    }
    if(count==0)results.addView(label("Ingen synlige apper samsvarer med søket.",15));else page.addView(label(count+" treff • trykk en app for å åpne",13));
    sc.addView(results);page.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(page);
  }
  void searchLocal(){
    if(!needQuery())return;String q=query().toLowerCase(Locale.ROOT);audit("local_search:"+query());
    LinearLayout page=column();page.addView(label("LOKALT SØK",22));page.addView(label("Treff i SKYNETs lokale hendelseslogg",14));page.addView(btn("Tilbake til søk",()->home()));
    TextView results=label("",14);StringBuilder out=new StringBuilder();File f=new File(getFilesDir(),"skynet-audit.log");
    if(f.exists()){try(BufferedReader r=new BufferedReader(new FileReader(f))){String line;int n=0;while((line=r.readLine())!=null){if(line.toLowerCase(Locale.ROOT).contains(q)){out.append("• ").append(line).append("\n\n");n++;}}if(n==0)out.append("Ingen lokale loggtreff.");}catch(Exception e){out.append("Kunne ikke lese lokal logg.");}}
    else out.append("Ingen lokal logg er opprettet ennå.");
    results.setText(out.toString());results.setBackgroundColor(PANEL);page.addView(results);setContentView(page);
  }
  void listen(){Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"nb-NO");i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Søk med SKYNET");try{startActivityForResult(i,SPEECH);}catch(Exception e){reply("Taletjeneste mangler. Skriv søket.");}}
  @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==SPEECH&&c==RESULT_OK&&d!=null){ArrayList<String>a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(a!=null&&!a.isEmpty()){if(input!=null)input.setText(a.get(0));searchWeb();}}}
  void command(String raw){String q=raw==null?"":raw.trim(),s=q.toLowerCase(new Locale("nb","NO"));if(q.isEmpty()){reply("Skriv eller si en kommando.");return;}audit("command:"+q);
    if(s.contains("hjelp"))reply("SKYNET kan nettsøk, app-søk, lokalt loggsøk, stemmesøk, status og dashboard.");
    else if(s.contains("status"))reply("SKYNET er klar. Android "+android.os.Build.VERSION.RELEASE+". Lokal logg "+(auditOn?"på":"av")+".");
    else if(s.contains("personvern")||s.contains("sikkerhet"))reply("Ingen bakgrunnslytting. Mikrofon brukes manuelt. Loggen lagres lokalt.");
    else if(s.contains("dashboard")||s.contains("åpne skynet"))dashboard();
    else if(s.startsWith("les opp")){String t=q.length()>7?q.substring(7).trim():"SKYNET er klar.";reply(t);if(ttsReady)tts.speak(t,TextToSpeech.QUEUE_FLUSH,null,"skynet");}
    else {if(input!=null)input.setText(q);searchWeb();}
  }
  void reply(String s){if(output!=null)output.setText(s);audit("response");}
  void dashboard(){audit("dashboard_open");showWeb("http://127.0.0.1:8766");}
  void audit(String e){if(!auditOn)return;try(FileWriter w=new FileWriter(new File(getFilesDir(),"skynet-audit.log"),true)){w.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date())+" "+e.replace("\n"," ")+"\n");}catch(Exception ignored){}}
  @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else{web=null;home();}}
  @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}if(web!=null)web.destroy();super.onDestroy();}
}
