package net.kyouko.cloudier.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.webkit.WebView;

import net.kyouko.cloudier.R;

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
    }


    private void initView() {
        initToolbar();
    }


    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_auth, menu);
        return true;
    }

}
