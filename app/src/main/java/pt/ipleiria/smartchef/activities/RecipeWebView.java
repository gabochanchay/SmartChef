package pt.ipleiria.smartchef.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import pt.ipleiria.smartchef.R;
import pt.ipleiria.smartchef.model.Recipe;

public class RecipeWebView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_web_view);

        Intent intent = getIntent();
        Recipe recipe = (Recipe) intent.getSerializableExtra("recipe");
        WebView webView;
//        setContentView(R.layout.webviewlayout);
        webView = (WebView)findViewById(R.id.help_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(recipe.getUrl());
    }
}
