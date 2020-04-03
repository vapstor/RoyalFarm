package br.com.royalfarma;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileDescriptor;
import java.io.IOException;

import br.com.royalfarma.database.DataBaseConnection;

import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.confirmQuitApp;
import static br.com.royalfarma.utils.Util.elapsedTime;

public class LoginActivity extends AppCompatActivity {
    private Handler handler;
    private ActionBar toolbar;
    private ProgressBar circularBar;
    private TextInputLayout loginLayout, passLayout;
    private TextInputEditText loginInput, passInput;
    private String login, pass;
    private AlertDialog webViewDialog;
    private DataBaseConnection dataBaseConnetion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        instanceViews();
    }

    private void instanceViews() {
        toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle("Entrar");
        }

        circularBar = findViewById(R.id.progressBarLogin);
        circularBar.setVisibility(View.INVISIBLE);

        loginLayout = findViewById(R.id.loginInputLayout);
        loginInput = findViewById(R.id.loginInput);
        passLayout = findViewById(R.id.senhaInputLayout);
        passInput = findViewById(R.id.senhaInput);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                updateUI(msg);
            }
        };

        dataBaseConnetion = new DataBaseConnection(handler);
    }

    private void updateUI(Message msg) {
        // 0 = status da conexao ao banco
        // 1 = status da async task
        //também mostra ao usuario que estamos tentando conectar
        if (msg.what == 0) {
            if ("tentando_conectar".equals(msg.obj)) {
                toggleCircularBarBtn(true);
            }
        } else if (msg.what == 1) {
            switch ((String) msg.obj) {
                case "onPreExecute":
                    toggleCircularBarBtn(true);
                    break;
                case "autenticado":
                    resultLogin(true);
                    toggleCircularBarBtn(false);
                    break;
                case "erro_autenticacao":
                    resultLogin(false);
                    toggleCircularBarBtn(false);
                    break;
                case "erro_sql":
                    Toast.makeText(this, "Erro de SQL", Toast.LENGTH_SHORT).show();
                    toggleCircularBarBtn(false);
                case "erro_sha":
                    Toast.makeText(this, "Erro codificação senha", Toast.LENGTH_SHORT).show();
                case "onPostExecute":
                    toggleCircularBarBtn(false);
                    break;
            }
        } else {
            Toast.makeText(this, "Ocorreu um erro!", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleCircularBarBtn(boolean circularBarVisibility) {
        final ViewGroup root = this.findViewById(R.id.activity_login_root_layout);
        Button btn = findViewById(R.id.buttonLogin);
        TransitionManager.beginDelayedTransition(root, new Fade());
        if (circularBarVisibility) {
            circularBar.setVisibility(View.VISIBLE);
            btn.setVisibility(View.INVISIBLE);
        } else {
            btn.setVisibility(View.VISIBLE);
            circularBar.setVisibility(View.INVISIBLE);
        }
    }

    public void entrar(View v) {
        if (passInput.getText() != null) {
            pass = passInput.getText().toString();
            if (pass.equals("")) {
                passLayout.setErrorEnabled(true);
                passLayout.setError("insira sua senha");
            } else {
                passLayout.setErrorEnabled(false);
            }
        } else {
            passLayout.setErrorEnabled(true);
            passLayout.setError("nulo");
        }

        if (loginInput.getText() != null) {
            login = loginInput.getText().toString();
            if (login.equals("")) {
                loginLayout.setErrorEnabled(true);
                loginLayout.setError("insira seu e-mail");
            } else {
                loginLayout.setErrorEnabled(false);
            }
        } else {
            loginLayout.setErrorEnabled(true);
            loginLayout.setError("nulo");
        }

        if (!loginLayout.isErrorEnabled() || !passLayout.isErrorEnabled()) {
            authenticateLogin();
        } else {
            Toast.makeText(this, "Confira as informações!", Toast.LENGTH_SHORT).show();
        }

    }

    public void cadastrarUsuario(View v) {
        if (SystemClock.elapsedRealtime() - elapsedTime < 1000) {
            return;
        }
        elapsedTime = SystemClock.elapsedRealtime();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        WebView myWebView = new WebView(this);
        myWebView.loadUrl("https://www.royalfarma.com.br/conta/cadastro#acc");
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        //Interface para mostrar que está carregando...
        ProgressDialog progressBar = new ProgressDialog(this);
        progressBar.setMessage("Por favor, aguarde...");
        progressBar.setCancelable(false);

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //teste para verificar se conta foi criada corretamente e fechar caixa de dialogo
                if (url.equals("https://www.royalfarma.com.br/conta/home#acc")) {
                    Toast.makeText(webViewDialog.getContext(), "Cadastro efetuado com sucesso!", Toast.LENGTH_SHORT).show();
                    if (progressBar.isShowing()) {
                        progressBar.dismiss();
                    }
                    webViewDialog.dismiss();
                    return false;
                } else {
                    view.loadUrl(url);
                    return true;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (url.equals("https://www.royalfarma.com.br/conta/home#acc")) {
                    if (progressBar.isShowing()) {
                        progressBar.dismiss();
                    }
                } else {
                    if (!progressBar.isShowing()) {
                        progressBar.show();
                    }
                }
            }

            public void onPageFinished(WebView view, String url) {
//                view.loadUrl("javascript:document.body.style.margin=\"8%\"; void 0");
                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }
            }
        });
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        builder.setView(myWebView);
        builder.setNegativeButton("Fechar", (dialog, id) -> dialog.dismiss());
        webViewDialog = builder.show();
    }

    @Override
    public void onBackPressed() {
        confirmQuitApp(this);
        super.onBackPressed();
    }

    //tenta autenticar assíncronamente
    private void authenticateLogin() {
        DataBaseConnection.AuthenticateLogin authenticateLogin = new DataBaseConnection.AuthenticateLogin(handler);
        authenticateLogin.execute(login, pass);
    }

    private void resultLogin(boolean success) {
        if (success) {
            try {
                final AssetFileDescriptor afd = this.getResources().getAssets().openFd("hero_decorative-celebration-02.wav");
                final FileDescriptor fileDescriptor = afd.getFileDescriptor();
                MediaPlayer player = new MediaPlayer();
                player.setDataSource(fileDescriptor, afd.getStartOffset(), afd.getLength());
                player.setLooping(false);
                player.prepare();
                player.setOnCompletionListener(mp -> {
                    Bundle bundle = makeSceneTransitionAnimation(LoginActivity.this).toBundle();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent, bundle);
                });
                player.start();
                Toast.makeText(this, "Conectado com sucesso", Toast.LENGTH_SHORT).show();
                toggleCircularBarBtn(false);
            } catch (IOException ex) {
                Log.e(MY_LOG_TAG, "Erro: " + ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Informações incorretas", Toast.LENGTH_SHORT).show();
        }
    }
}
