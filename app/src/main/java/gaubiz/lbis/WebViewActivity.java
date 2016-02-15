package gaubiz.lbis;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {

    private WebView mWebView;

    private String url = "http://m.naver.com";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView = (WebView) findViewById(R.id.webView);

        //웹뷰 호출
        mWebView.getSettings().setJavaScriptEnabled(true);  //웹뷰에서 자바스크립트 사용
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient());
    }
}