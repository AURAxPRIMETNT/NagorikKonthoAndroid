package com.nagorikkontho.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private static final String APP_ORIGIN = "https://appassets.local/";

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.setBackgroundColor(0xFF0A0A0A);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString(settings.getUserAgentString() + " NagorikKonthoAndroid/1.0");

        webView.setWebViewClient(new LocalAssetWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = callback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(
                        Intent.createChooser(intent, "ছবি নির্বাচন করুন"),
                        FILE_CHOOSER_REQUEST
                );
                return true;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return true;
            }
        });

        FrameLayout root = new FrameLayout(this);
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);

        webView.loadUrl(APP_ORIGIN + "index.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER_REQUEST || filePathCallback == null) {
            return;
        }

        Uri[] results = null;
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            results = new Uri[]{data.getData()};
        }
        filePathCallback.onReceiveValue(results);
        filePathCallback = null;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private class LocalAssetWebViewClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if (isLocalAsset(uri)) {
                return serveAsset(uri.getPath());
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            try {
                Uri uri = Uri.parse(url);
                if (isLocalAsset(uri)) {
                    return serveAsset(uri.getPath());
                }
            } catch (Exception ignored) {
                // Let WebView handle unexpected URLs normally.
            }
            return super.shouldInterceptRequest(view, url);
        }

        private boolean isLocalAsset(Uri uri) {
            return uri != null
                    && "https".equalsIgnoreCase(uri.getScheme())
                    && "appassets.local".equalsIgnoreCase(uri.getHost());
        }

        private WebResourceResponse serveAsset(String path) {
            if (path == null || path.isEmpty() || "/".equals(path)) {
                path = "/index.html";
            }

            String assetPath = path.startsWith("/") ? path.substring(1) : path;
            if (assetPath.contains("..")) {
                return null;
            }

            try {
                InputStream input = getAssets().open(assetPath);
                String mime = URLConnection.guessContentTypeFromName(assetPath);
                if (mime == null) {
                    String ext = MimeTypeMap.getFileExtensionFromUrl(assetPath);
                    mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                }
                if (mime == null) {
                    mime = "application/octet-stream";
                }

                String encoding = (mime.startsWith("text/")
                        || mime.contains("javascript")
                        || mime.contains("json")
                        || mime.contains("svg")) ? "UTF-8" : null;

                return new WebResourceResponse(mime, encoding, input);
            } catch (IOException e) {
                return null;
            }
        }
    }
}
