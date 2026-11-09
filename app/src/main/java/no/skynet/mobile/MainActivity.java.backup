package no.skynet.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Button;
import android.graphics.Color;

public class MainActivity extends Activity {
    WebView web;

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.rgb(5,12,22));
        getWindow().setNavigationBarColor(Color.rgb(5,12,22));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setBackgroundColor(Color.rgb(5,12,22));

        Button refresh = new Button(this);
        refresh.setText("SKYNET MOBILE  |  REFRESH");
        refresh.setOnClickListener(v ->
            web.loadUrl("http://127.0.0.1:8766"));

        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(5,12,22));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient());

        layout.addView(refresh);
        layout.addView(web,
            new LinearLayout.LayoutParams(-1,0,1));

        setContentView(layout);
        web.loadUrl("http://127.0.0.1:8766");
    }
}
