package no.skynet.mobile;

import no.skynet.mobile.distributed.SkynetPersonality;
import no.skynet.mobile.distributed.SkynetMemoryStore;
import no.skynet.mobile.distributed.SkynetRecovery;
import no.skynet.mobile.distributed.SkynetDistributedCore;
import no.skynet.mobile.intelligence.SkynetAiEngine;
import no.skynet.mobile.intelligence.IntelligenceAi;
import no.skynet.mobile.intelligence.CognitivePsychologyAi;
import no.skynet.mobile.intelligence.SentinelCollector;

import android.app.Activity;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
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
  SkynetPersonality personality;
  SkynetMemoryStore distributedMemory;
  SkynetRecovery recovery;
  SkynetDistributedCore distributedCore;
  TextView distributedOutput;
  static final int DIST_BT_PERMISSION=5202, DIST_EXPORT=5203, DIST_IMPORT=5204;
  EditText input; TextView output; WebView web; String currentQuery="";
  int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
  TextView label(String s,int size){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(size);v.setPadding(dp(6),dp(9),dp(6),dp(9));return v;}
  Button btn(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setOnClickListener(v->r.run());return b;}
  LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(16),dp(12),dp(16),dp(20));l.setBackgroundColor(BG);return l;}
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);personality=new SkynetPersonality(this);
    distributedMemory=new SkynetMemoryStore(this);
    recovery=new SkynetRecovery(this);
    tts=new TextToSpeech(this,x->{if(x==TextToSpeech.SUCCESS){int r=tts.setLanguage(new Locale("nb","NO"));ttsReady=r!=TextToSpeech.LANG_MISSING_DATA&&r!=TextToSpeech.LANG_NOT_SUPPORTED;}});home();audit("app_started");}
  void home(){
    LinearLayout root=column();root.addView(label("SKYNET",30));root.addView(label("PERSONLIG ASSISTENT • SEARCH",13));root.addView(label("Klar • Android "+android.os.Build.VERSION.RELEASE,14));
    root.addView(label("Søk på nettet, apper og lokale data",19));
    input=new EditText(this);input.setSingleLine(true);input.setTextColor(Color.WHITE);input.setHintTextColor(Color.LTGRAY);input.setHint("Søk med SKYNET …");input.setPadding(dp(12),dp(10),dp(12),dp(10));input.setBackgroundColor(PANEL);root.addView(input,new LinearLayout.LayoutParams(-1,dp(54)));
    root.addView(btn("⌕  Søk på nettet (Google)",()->searchWeb()));
    root.addView(btn("▦  Finn installerte apper",()->searchApps()));
    root.addView(btn("▤  Søk i lokal logg",()->searchLocal()));
    root.addView(btn("🎙  Stemmesøk",this::listen));
    root.addView(btn("📡  Wi-Fi + Bluetooth-analyse",this::deviceIntel));
    root.addView(btn("🌐  SKYNET nettverksnoder",()->startActivity(new Intent(this,NetworkNodesActivity.class))));
    root.addView(btn("GOD'S EYE VIEW",()->startActivity(new Intent(this,GodsEyeActivity.class))));
    root.addView(btn("🧠  SKYNET AI CORE",()->openAiCenter("")));
    root.addView(btn("🌐  SKYNET DISTRIBUTED",this::openDistributed));
    output=label("Hei! Søk på nettet, finn en app eller søk i SKYNETs lokale logg.",15);output.setBackgroundColor(PANEL);root.addView(output);
    root.addView(label("Hurtigvalg",18));root.addView(btn("Status",()->command("status")));root.addView(btn("Personvern",()->command("personvern")));root.addView(btn("Åpne lokalt SKYNET-dashboard",this::dashboard));
    CheckBox cb=new CheckBox(this);cb.setText("Lokal hendelseslogg");cb.setTextColor(Color.WHITE);cb.setChecked(auditOn);cb.setOnCheckedChangeListener((v,on)->{auditOn=on;if(on)audit("audit_enabled");});root.addView(cb);
    root.addView(label("Ingen bakgrunnslytting. Mikrofonen brukes manuelt. Lokal logg lagres på enheten.",12));
    ScrollView sc=new ScrollView(this);sc.addView(root);setContentView(sc);
  }
  String query(){return input==null?"":input.getText().toString().trim();}
  boolean needQuery(){if(query().isEmpty()){reply("Skriv hva du vil søke etter.");return false;}return true;}
  void searchWeb(){if(!needQuery())return;currentQuery=query();if(distributedMemory!=null)distributedMemory.remember("user",currentQuery);audit("web_search:"+currentQuery);showWeb("https://www.google.com/search?q="+Uri.encode(currentQuery));}
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
  @Override protected void onActivityResult(int r,int c,Intent d){
    super.onActivityResult(r,c,d);
    if(r==SPEECH&&c==RESULT_OK&&d!=null){
      ArrayList<String>a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      if(a!=null&&!a.isEmpty()){if(input!=null)input.setText(a.get(0));searchWeb();}
    } else if(r==DIST_EXPORT&&c==RESULT_OK&&d!=null&&d.getData()!=null){
      try(InputStream in=new FileInputStream(new File(getFilesDir(),"skynet-memory-backup.json"));
          OutputStream out=getContentResolver().openOutputStream(d.getData(),"wt")){
        if(out!=null){byte[] buf=new byte[4096];int n;while((n=in.read(buf))>0)out.write(buf,0,n);out.flush();appendDistributed("Sikkerhetskopi eksportert.");}
      }catch(Exception e){appendDistributed("Eksport feilet: "+e.getClass().getSimpleName());}
    } else if(r==DIST_IMPORT&&c==RESULT_OK&&d!=null&&d.getData()!=null){
      try(InputStream in=getContentResolver().openInputStream(d.getData())){
        File tmp=new File(getCacheDir(),"skynet-import.json");
        try(OutputStream out=new FileOutputStream(tmp)){byte[] buf=new byte[4096];int n;while((n=in.read(buf))>0)out.write(buf,0,n);}
        appendDistributed(recovery.restoreMemory(tmp)?"Sikkerhetskopi gjenopprettet.":"Sikkerhetskopien kunne ikke gjenopprettes.");
        audit("distributed_backup_imported");
      }catch(Exception e){appendDistributed("Import feilet: "+e.getClass().getSimpleName());}
    }
  }
  void command(String raw){String q=raw==null?"":raw.trim(),s=q.toLowerCase(new Locale("nb","NO"));if(q.isEmpty()){reply("Skriv eller si en kommando.");return;}audit("command:"+q);
    if(s.contains("hjelp"))reply("SKYNET kan nettsøk, app-søk, lokalt loggsøk, stemmesøk, status og dashboard.");
    else if(s.contains("status"))reply("SKYNET er klar. Android "+android.os.Build.VERSION.RELEASE+". Lokal logg "+(auditOn?"på":"av")+".");
    else if(s.contains("personvern")||s.contains("sikkerhet"))reply("Ingen bakgrunnslytting. Mikrofon brukes manuelt. Loggen lagres lokalt.");
    else if(s.contains("sentinel")||s.contains("ai motor")||s.contains("intelligence ai")||s.contains("kognitiv")||s.equals("ai"))openAiCenter(q);
    else if(s.contains("wifi")||s.contains("wi-fi")||s.contains("bluetooth")||s.contains("nettverk"))deviceIntel();
    else if(s.contains("dashboard")||s.contains("åpne skynet"))dashboard();
    else if(s.startsWith("les opp")){String t=q.length()>7?q.substring(7).trim():"SKYNET er klar.";reply(t);if(ttsReady)tts.speak(t,TextToSpeech.QUEUE_FLUSH,null,"skynet");}
    else {if(input!=null)input.setText(q);searchWeb();}
  }


  void openAiCenter(String initial){
    LinearLayout page=column();
    page.addView(label("SKYNET AI CORE",24));
    page.addView(label("Fire lokale analysemoduler • brukerinitiert kjøring",13));
    page.addView(btn("Tilbake",()->home()));

    EditText prompt=new EditText(this);
    prompt.setTextColor(Color.WHITE);
    prompt.setHintTextColor(Color.LTGRAY);
    prompt.setHint("Skriv en problemstilling for analyse");
    prompt.setText(initial);
    prompt.setMinLines(2);
    prompt.setGravity(android.view.Gravity.TOP);
    prompt.setBackgroundColor(PANEL);
    page.addView(prompt,new LinearLayout.LayoutParams(-1,dp(90)));

    TextView result=label("SKYNET AI CORE er klar. Velg en modul.",14);
    result.setBackgroundColor(PANEL);

    page.addView(btn("🧠 Kjør hele AI-motoren",()->{
      String q=prompt.getText().toString().trim();
      result.setText(SkynetAiEngine.run(q,getApplicationContext()));
      audit("ai_engine_run");
    }));

    page.addView(btn("🔎 Intelligence AI",()->{
      result.setText(IntelligenceAi.analyze(prompt.getText().toString()));
      audit("intelligence_ai_run");
    }));

    page.addView(btn("🧩 Cognitive Psychology AI",()->{
      result.setText(CognitivePsychologyAi.analyze(prompt.getText().toString()));
      audit("cognitive_psychology_run");
    }));

    page.addView(btn("🛡 SENTINEL_COLLECTOR",()->{
      result.setText(SentinelCollector.collect(getApplicationContext()));
      audit("sentinel_collector_run");
    }));

    page.addView(label("RESULTAT",17));
    ScrollView scroll=new ScrollView(this);
    scroll.addView(result);
    page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    setContentView(page);
  }

  void deviceIntel(){
    if(android.os.Build.VERSION.SDK_INT>=31 &&
       checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){
      requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},5201);
      return;
    }

    StringBuilder report=new StringBuilder();
    report.append("SKYNET • ENHETSINTELLIGENS\n\n");

    try {
      WifiManager wm=(WifiManager)getApplicationContext()
          .getSystemService(Context.WIFI_SERVICE);
      report.append("WI-FI\n");

      if(wm!=null && wm.isWifiEnabled()){
        WifiInfo wi=wm.getConnectionInfo();
        if(wi!=null){
          report.append("Tilkobling: ")
              .append(wi.getNetworkId()>=0?"aktiv":"ikke bekreftet").append("\n");
          report.append("Signal: ").append(wi.getRssi()).append(" dBm\n");
          report.append("Frekvens: ").append(wi.getFrequency()).append(" MHz\n");
          report.append("Linkhastighet: ").append(wi.getLinkSpeed()).append(" Mbps\n");
        } else {
          report.append("Tilkoblingsinformasjon utilgjengelig.\n");
        }
      } else {
        report.append("Wi-Fi deaktivert eller utilgjengelig.\n");
      }
    } catch(Exception e) {
      report.append("Wi-Fi-data utilgjengelig: ").append(e.getClass().getSimpleName()).append("\n");
    }

    try {
      ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
      Network active=cm==null?null:cm.getActiveNetwork();
      NetworkCapabilities caps=cm==null||active==null?null:cm.getNetworkCapabilities(active);
      report.append("Internett: ")
          .append(caps!=null&&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
              ?"nettverk tilgjengelig":"ikke bekreftet").append("\n");
    } catch(Exception e) {
      report.append("Internettstatus utilgjengelig.\n");
    }

    report.append("\nBLUETOOTH • PAREDE ENHETER\n");
    try {
      BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
      if(adapter==null) {
        report.append("Bluetooth støttes ikke.\n");
      } else if(!adapter.isEnabled()) {
        report.append("Bluetooth er deaktivert.\n");
      } else {
        java.util.Set<BluetoothDevice> devices=adapter.getBondedDevices();
        report.append("Antall parede enheter: ")
            .append(devices==null?0:devices.size()).append("\n");

        if(devices!=null) {
          for(BluetoothDevice device:devices) {
            String name=device.getName();
            report.append("• ")
                .append(name==null||name.trim().isEmpty()?"Ukjent enhet":name)
                .append("\n");
          }
        }
      }
    } catch(SecurityException e) {
      report.append("Bluetooth krever Android-tillatelse.\n");
    } catch(Exception e) {
      report.append("Bluetooth-data utilgjengelig.\n");
    }

    report.append("\nANALYSE\n");
    report.append("Dette er en lokal øyeblikksmåling av nettverk og parede enheter.\n");
    report.append("Ingen skjult skanning, avlytting eller automatisk opplasting.\n");

    audit("device_intelligence_viewed");

    LinearLayout page=column();
    page.addView(label("SKYNET INTELLIGENCE",22));
    page.addView(btn("Tilbake",()->home()));

    TextView result=label(report.toString(),14);
    result.setBackgroundColor(PANEL);
    page.addView(result);

    page.addView(btn("Oppdater målinger",this::deviceIntel));
    setContentView(page);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
      String[] permissions,int[] grantResults) {
    super.onRequestPermissionsResult(requestCode,permissions,grantResults);
    if(requestCode==DIST_BT_PERMISSION) {
      if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) startDistributedDiscovery();
      else appendDistributed("Bluetooth-tillatelse avslått.");
    }
    if(requestCode==5201) {
      if(grantResults.length>0 &&
          grantResults[0]==PackageManager.PERMISSION_GRANTED) {
        deviceIntel();
      } else {
        reply("Bluetooth-tillatelse avslått. Wi-Fi-analysen er fortsatt tilgjengelig.");
      }
    }
  }


  void openDistributed(){
    LinearLayout page=column();
    page.addView(label("SKYNET DISTRIBUTED",24));
    page.addView(label("Personlighet • lokal hukommelse • enhetsoppdagelse",13));
    page.addView(btn("Tilbake",()->home()));
    distributedOutput=label(personality.greeting(),14);
    distributedOutput.setBackgroundColor(PANEL);
    page.addView(distributedOutput);
    page.addView(btn("⚙ Konfigurer SKYNET-personlighet",this::configurePersonality));
    page.addView(btn("🧠 Vis lokal samtalehukommelse",this::showDistributedMemory));
    page.addView(btn("🗑 Tøm lokal samtalehukommelse",()->{
      new android.app.AlertDialog.Builder(this).setTitle("Tømme hukommelsen?")
        .setMessage("Dette sletter SKYNETs lokale samtalehistorikk på denne enheten.")
        .setNegativeButton("Avbryt",null).setPositiveButton("Tøm",(d,w)->{
          distributedMemory.clear(); distributedOutput.setText("Lokal hukommelse er tømt.");
          audit("distributed_memory_cleared");
        }).show();
    }));
    page.addView(btn("💾 Eksporter sikkerhetskopi",this::exportDistributedMemory));
    page.addView(btn("📥 Gjenopprett sikkerhetskopi",this::importDistributedMemory));
    page.addView(btn("📡 Start enhetsoppdagelse",this::startDistributedDiscovery));
    page.addView(btn("⏹ Stopp enhetsoppdagelse",()->{
      if(distributedCore!=null) distributedCore.stop();
      appendDistributed("Enhetsoppdagelse stoppet.");
    }));
    page.addView(label("Oppdager bare parede Bluetooth-enheter og enheter som frivillig annonserer SKYNET-tjenesten på lokalt Wi-Fi. Ingen automatisk fjernstyring eller skjult tilkobling.",12));
    setContentView(page);
  }

  void appendDistributed(String message){
    runOnUiThread(()->{ if(distributedOutput!=null){
      distributedOutput.setText(distributedOutput.getText().toString()+"\n\n"+message);
    }});
  }

  void configurePersonality(){
    LinearLayout form=new LinearLayout(this); form.setOrientation(1);
    EditText name=new EditText(this); name.setSingleLine(true); name.setHint("Navn");
    name.setText(personality.getName()); form.addView(name);
    Spinner style=new Spinner(this); String[] styles={"friendly","concise","curious"};
    style.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,styles));
    for(int i=0;i<styles.length;i++) if(styles[i].equals(personality.getStyle())) style.setSelection(i);
    form.addView(style);
    new android.app.AlertDialog.Builder(this).setTitle("SKYNET-personlighet").setView(form)
      .setNegativeButton("Avbryt",null).setPositiveButton("Lagre",(d,w)->{
        personality.configure(name.getText().toString(),String.valueOf(style.getSelectedItem()));
        appendDistributed(personality.greeting()); audit("distributed_personality_updated");
      }).show();
  }

  void showDistributedMemory(){
    org.json.JSONArray entries=distributedMemory.readAll();
    StringBuilder b=new StringBuilder("LOKAL HUKOMMELSE • "+entries.length()+" oppføringer\n\n");
    for(int i=0;i<entries.length();i++){
      org.json.JSONObject item=entries.optJSONObject(i);
      if(item!=null) b.append(item.optString("role","?")).append(": ")
        .append(item.optString("message","")).append("\n\n");
    }
    TextView view=label(b.toString(),13); view.setBackgroundColor(PANEL);
    ScrollView sc=new ScrollView(this); sc.addView(view);
    new android.app.AlertDialog.Builder(this).setTitle("SKYNET-hukommelse")
      .setView(sc).setPositiveButton("Lukk",null).show();
  }

  void exportDistributedMemory(){
    try{
      recovery.exportMemory();
      Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE);
      i.setType("application/json"); i.putExtra(Intent.EXTRA_TITLE,"skynet-memory-backup.json");
      startActivityForResult(i,DIST_EXPORT); audit("distributed_backup_export_requested");
    }catch(Exception e){appendDistributed("Kunne ikke klargjøre sikkerhetskopi.");}
  }

  void importDistributedMemory(){
    Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE);
    i.setType("application/json"); startActivityForResult(i,DIST_IMPORT);
  }

  void startDistributedDiscovery(){
    if(android.os.Build.VERSION.SDK_INT>=31 &&
       checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){
      requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},DIST_BT_PERMISSION); return;
    }
    if(distributedCore!=null) distributedCore.stop();
    distributedCore=new SkynetDistributedCore(this,new SkynetDistributedCore.Listener(){
      public void onWifiPeer(String name,String host,int port){appendDistributed("Wi-Fi-enhet: "+name+" • "+host+":"+port);}
      public void onBluetoothPairedDevice(String name,String address){appendDistributed("Parede Bluetooth-enhet: "+name+" • "+address);}
      public void onStatus(String message){appendDistributed(message);}
    });
    distributedCore.start(); audit("distributed_discovery_started");
  }

  void reply(String s){if(output!=null)output.setText(s);if(distributedMemory!=null)distributedMemory.remember("assistant",s);audit("response");}
  void dashboard(){audit("dashboard_open");showWeb("https://fevi12.github.io/Skynet.mobile/");}
  void audit(String e){if(!auditOn)return;try(FileWriter w=new FileWriter(new File(getFilesDir(),"skynet-audit.log"),true)){w.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date())+" "+e.replace("\n"," ")+"\n");}catch(Exception ignored){}}
  @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else{web=null;home();}}
  @Override protected void onDestroy(){if(distributedCore!=null)distributedCore.stop();if(tts!=null){tts.stop();tts.shutdown();}if(web!=null)web.destroy();super.onDestroy();}
}
