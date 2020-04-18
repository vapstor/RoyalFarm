package br.com.royalfarma.ui.login;

import android.app.ProgressDialog;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileDescriptor;
import java.io.IOException;

import br.com.royalfarma.R;
import br.com.royalfarma.database.DataBaseConnection;
import br.com.royalfarma.model.Usuario;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.elapsedTime;
import static br.com.royalfarma.utils.Util.getSha512FromString;

public class LoginFragment extends Fragment {

    private Handler handler;
    private ActionBar toolbar;
    private ProgressBar circularBar;
    private TextInputLayout loginLayout, passLayout;
    private TextInputEditText loginInput, passInput;
    private String login, pass;
    private AlertDialog webViewDialog;
    private DataBaseConnection dataBaseConnetion;


    private LoginViewModel loginViewModel;
    private String subtotal;
    private BottomNavigationView navView;
    private AppCompatButton cadastrarBtn, buttonLogin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navView = ((AppCompatActivity) getContext()).findViewById(R.id.nav_view);
        if (navView != null) {
            navView.setVisibility(View.GONE);
        }
        Bundle extras = getArguments();
        if (extras != null) {
            subtotal = extras.getString("subtotal");
        }

    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (navView != null) {
            navView.setVisibility(View.GONE);
        }
        loginViewModel = new ViewModelProvider(requireParentFragment()).get(LoginViewModel.class);
        loginViewModel.getUsuarioMutableLiveData().observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null)
                loginViewModel.updateUsuarioInfo(usuario);
            else
                Log.e(MY_LOG_TAG, "Usuario Nulo!");
        });
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }


    @Override
    public void onResume() {
        if (navView != null) {
            navView.setVisibility(View.GONE);
        }
        super.onResume();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (navView != null) {
            navView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        instanceViews();
        Toast.makeText(getContext(), "Subtotal: " + subtotal, Toast.LENGTH_SHORT).show();
    }

    private void instanceViews() {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null) {
            fragmentActivity.setTitle("Entrar");

            circularBar = fragmentActivity.findViewById(R.id.progressBarLogin);
            circularBar.setVisibility(View.INVISIBLE);

            loginLayout = fragmentActivity.findViewById(R.id.loginInputLayout);
            loginInput = fragmentActivity.findViewById(R.id.loginInput);
            passLayout = fragmentActivity.findViewById(R.id.senhaInputLayout);
            passInput = fragmentActivity.findViewById(R.id.senhaInput);
            cadastrarBtn = fragmentActivity.findViewById(R.id.btnCadastreSe);
            buttonLogin = fragmentActivity.findViewById(R.id.buttonLogin);
            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(fragmentActivity, "Tela de finalizar não implementada!", Toast.LENGTH_SHORT).show();
                }
            });
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    updateUI(msg);
                }
            };

            dataBaseConnetion = new DataBaseConnection(handler);
        }
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
                    Toast.makeText(getContext(), "Erro de SQL", Toast.LENGTH_SHORT).show();
                    toggleCircularBarBtn(false);
                case "erro_sha":
                    Toast.makeText(getContext(), "Erro codificação senha", Toast.LENGTH_SHORT).show();
                case "onPostExecute":
                    toggleCircularBarBtn(false);
                    break;
            }
        } else {
            Toast.makeText(getContext(), "Ocorreu um erro!", Toast.LENGTH_SHORT).show();
        }
    }


    private void toggleCircularBarBtn(boolean circularBarVisibility) {
        final ViewGroup root = getActivity().findViewById(R.id.activity_login_root_layout);
        Button btn = getActivity().findViewById(R.id.buttonLogin);
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
//        if (passInput.getText() != null) {
//            pass = passInput.getText().toString();
//            if (pass.equals("")) {
//                passLayout.setErrorEnabled(true);
//                passLayout.setError("insira sua senha");
//            } else {
//                passLayout.setErrorEnabled(false);
//            }
//        } else {
//            passLayout.setErrorEnabled(true);
//            passLayout.setError("nulo");
//        }
//
//        if (loginInput.getText() != null) {
//            login = loginInput.getText().toString();
//            if (login.equals("")) {
//                loginLayout.setErrorEnabled(true);
//                loginLayout.setError("insira seu e-mail");
//            } else {
//                loginLayout.setErrorEnabled(false);
//            }
//        } else {
//            loginLayout.setErrorEnabled(true);
//            loginLayout.setError("nulo");
//        }
//
//        if (!loginLayout.isErrorEnabled() && !passLayout.isErrorEnabled()) {
//            authenticateLogin();
//        } else {
//            Toast.makeText(getContext(), "Confira as informações!", Toast.LENGTH_SHORT).show();
//        }
        debugApp();
    }

    public void cadastrarUsuario() {
        if (SystemClock.elapsedRealtime() - elapsedTime < 1000) {
            return;
        }
        elapsedTime = SystemClock.elapsedRealtime();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        WebView myWebView = new WebView(getContext()) {
            @Override
            public boolean onCheckIsTextEditor() {
                return true;
            }
        };
        myWebView.loadUrl("https://www.royalfarma.com.br/conta/cadastro#acc");
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.requestFocusFromTouch();
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.requestFocus(View.FOCUS_DOWN);
        myWebView.setOnTouchListener((v1, event) -> {
            v1.performClick();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    if (!v1.hasFocus()) {
                        v1.requestFocus();
                    }
                    break;
            }
            return false;
        });

        webSettings.setUseWideViewPort(true);
        //Interface para mostrar que está carregando...
        ProgressDialog progressBar = new ProgressDialog(getContext());
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

    //tenta autenticar assíncronamente
    private void authenticateLogin() {
        DataBaseConnection.AuthenticateLogin authenticateLogin = new DataBaseConnection.AuthenticateLogin(handler);
        authenticateLogin.execute(login, pass);
    }

    private void resultLogin(boolean success) {
        if (success) {
            try {
                //Atualiza modelo com info do usuario
                loginViewModel.updateUsuarioInfo(new Usuario(login, getSha512FromString(pass)));

                final AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.beep_login);
                final FileDescriptor fileDescriptor = afd.getFileDescriptor();
                MediaPlayer player = new MediaPlayer();
                player.setDataSource(fileDescriptor, afd.getStartOffset(), afd.getLength());
                player.setLooping(false);
                player.prepare();
                player.start();
//                Bundle bundle = makeSceneTransitionAnimation(LoginActivity.this).toBundle();
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent, bundle);
                Log.d(MY_LOG_TAG, "Logado com sucesso!");
//                toggleCircularBarBtn(false);
            } catch (IOException ex) {
                Log.e(MY_LOG_TAG, "Erro: " + ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), "Informações incorretas", Toast.LENGTH_SHORT).show();
        }
    }

    private void debugApp() {
        //navigate to
    }

}
