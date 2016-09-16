package net.kyouko.cloudier.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.kyouko.cloudier.Config;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.api.TencentWeiboApi;
import net.kyouko.cloudier.model.Account;
import net.kyouko.cloudier.model.User;
import net.kyouko.cloudier.util.AuthUtil;
import net.kyouko.cloudier.util.MessageUtil;
import net.kyouko.cloudier.util.RequestUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {

    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
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
                loadAccountInfo(account);
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


    private void loadAccountInfo(final Account account) {
        TencentWeiboApi api = RequestUtil.getApiInstance();
        Call<User> userCall = api.getUser(RequestUtil.createOAuthParams(this), account.username);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                account.nickname = response.body().nickname;
                account.avatarUrl = response.body().avatarUrl;

                AuthUtil.saveAccount(AuthActivity.this, account);
                MessageUtil.showToast(AuthActivity.this, R.string.text_info_auth_success);

                Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_account,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadAccountInfo(account);
                            }
                        })
                        .show();
            }
        });
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
