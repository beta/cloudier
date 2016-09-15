package net.kyouko.cloudier.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.kyouko.cloudier.Config;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.model.Account;
import net.kyouko.cloudier.util.AuthUtil;
import net.kyouko.cloudier.util.MessageUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.webview) WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        ButterKnife.bind(this);

        initView();

        loadAuthPage();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_auth, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadAuthPage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void initView() {
        initToolbar();
        initWebView();
    }


    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new AuthWebViewClient(new AuthWebViewClient.OnAuthSuccessListener() {
            @Override
            public void onAuthSuccess(Account account) {
                AuthUtil.saveAccount(AuthActivity.this, account);
                MessageUtil.showToast(AuthActivity.this, R.string.text_info_auth_success);
                startActivity(new Intent(AuthActivity.this, HomeActivity.class));
            }
        }));
    }


    private void loadAuthPage() {
        try {
            webView.loadUrl("https://open.t.qq.com/cgi-bin/oauth2/authorize?client_id=" +
                    Config.TENCENT_APP_KEY + "&response_type=token&redirect_uri=" +
                    URLEncoder.encode(Config.TENCENT_REDIRECT_URL, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            // Ignore
        }
    }


    private static class AuthWebViewClient extends WebViewClient {

        public interface OnAuthSuccessListener {
            void onAuthSuccess(Account account);
        }


        private OnAuthSuccessListener onAuthSuccessListener;


        public AuthWebViewClient(OnAuthSuccessListener onAuthSuccessListener) {
            this.onAuthSuccessListener = onAuthSuccessListener;
        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(Config.TENCENT_REDIRECT_URL)) {
                Account account = AuthUtil.parseAccountFromUrl(url);

                if (onAuthSuccessListener != null) {
                    onAuthSuccessListener.onAuthSuccess(account);
                }

                return true;
            } else if (url.startsWith("http")) {
                view.loadUrl(url);
                return true;
            } else {
                return false;
            }
        }

    }

}
