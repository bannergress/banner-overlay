package com.bannergress.overlay;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().getScheme().equals("geo")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                    startActivity(intent);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.loadUrl("javascript:(function() { " +
                        "       MutationObserver = window.MutationObserver || window.WebKitMutationObserver;" +
                        "       var observer = new MutationObserver(function(mutationsList) {" +
                        "       for (var mutation of mutationsList) {" +
                        "           if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {" +
                        "               var addedNode = mutation.addedNodes[0];" +
                        "               if (addedNode.classList && addedNode.classList.contains('banner-info-page')) {" +
                        "                   var button = addedNode.querySelector('.banner-info-button');" +
                        "                   if (button && !document.getElementById('open-overlay-button')) { " +
                        "                       var overlayButton = document.createElement('a');" +
                        "                       overlayButton.id = 'open-overlay-button';" +
                        "                       overlayButton.innerHTML = 'Open overlay';" +
                        "                       overlayButton.classList.add('banner-info-button');" +
                        "                       overlayButton.onclick = (event) => { " +
                        "                           window.android.openOverlay(window.location.href);" +
                        "                           event.preventDefault();" +
                        "                       };" +
                        "                       button.parentNode.append(overlayButton);" +
                        "                   }" +
                        "                   return;" +
                        "               }" +
                        "           }" +
                        "       }" +
                        "   });" +
                        "observer.observe(document.body, { childList: true, subtree: true });})()");
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36");
        webView.addJavascriptInterface(new JavaScriptInterface(), "android");

        webView.loadUrl("https://bannergress.com");
    }

    public class JavaScriptInterface {
        @JavascriptInterface
        public void openOverlay(String url) {
            Intent intent = new Intent(MainActivity.this, OverlayActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, url);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
