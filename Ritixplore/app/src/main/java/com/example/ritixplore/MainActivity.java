package com.example.ritixplore;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlEditText;
    private Button goButton;
    private TextView factTextView;
    private String[] facts;
    private Handler factHandler = new Handler();
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        webView = findViewById(R.id.webView);
        urlEditText = findViewById(R.id.urlEditText);
        goButton = findViewById(R.id.goButton);
        factTextView = findViewById(R.id.factTextView);

        // Set up facts array
        facts = new String[]{
            getString(R.string.fact_1),
            getString(R.string.fact_2),
            getString(R.string.fact_3),
            getString(R.string.fact_4),
            getString(R.string.fact_5)
        };

        // Configure WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
        // Important: This disables image loading
        webSettings.setLoadsImagesAutomatically(false);
        
        // Custom WebViewClient to block media content
        webView.setWebViewClient(new TextOnlyWebViewClient());

        // Set up the search functionality
        goButton.setOnClickListener(v -> loadUrl());

        // Handle "Enter" key on keyboard
        urlEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadUrl();
                return true;
            }
            return false;
        });

        // Set up rotating facts
        startFactRotation();

        // Load a default page
        webView.loadUrl("https://duckduckgo.com/lite");
    }

    private void startFactRotation() {
        // Runnable to rotate facts
        Runnable factRunnable = new Runnable() {
            @Override
            public void run() {
                // Display a random fact
                int index = random.nextInt(facts.length);
                factTextView.setText(facts[index]);
                
                // Schedule next fact change
                factHandler.postDelayed(this, 10000); // Change every 10 seconds
            }
        };
        
        // Start fact rotation
        factHandler.post(factRunnable);
    }

    private void loadUrl() {
        String url = urlEditText.getText().toString();
        
        // Add http:// prefix if needed
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            // If it doesn't look like a URL, treat as search query
            if (!url.contains(".")) {
                url = "https://duckduckgo.com/lite?q=" + url;
            } else {
                url = "https://" + url;
            }
        }
        
        webView.loadUrl(url);
    }

    // Handle back button to navigate webview history
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to prevent leaks
        factHandler.removeCallbacksAndMessages(null);
    }

    // Custom WebViewClient to block images and videos
    private class TextOnlyWebViewClient extends WebViewClient {
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString().toLowerCase();
            
            // Block image and video content
            if (url.endsWith(".jpg") || url.endsWith(".jpeg") || 
                url.endsWith(".png") || url.endsWith(".gif") || 
                url.endsWith(".webp") || url.endsWith(".svg") ||
                url.endsWith(".mp4") || url.endsWith(".webm") || 
                url.endsWith(".ogg") || url.endsWith(".mov") ||
                url.contains("/video/") || url.contains("/image/")) {
                
                // Return empty response
                return new WebResourceResponse("text/plain", "UTF-8", 
                    new ByteArrayInputStream("[media content blocked]".getBytes()));
            }
            
            return super.shouldInterceptRequest(view, request);
        }
    }
}