package br.com.royalfarma.activitys;

import android.app.SearchManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import br.com.royalfarma.R;
import br.com.royalfarma.database.DataBaseConnection;
import br.com.royalfarma.ui.pesquisar.PesquisarViewModel;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.confirmQuitApp;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private BottomNavigationView navView;
    private Handler handler;
    private DataBaseConnection dataBaseConnetion;
    private PesquisarViewModel pesquisarViewModel;
    private SearchManager searchManager;
    private SearchView searchView;
    private String queryAnterior;
    private static int displayOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(MY_LOG_TAG, msg.toString());
//                updateUI(msg);
            }
        };

        dataBaseConnetion = new DataBaseConnection(handler);

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemReselectedListener(item -> {
            if (navController.getCurrentDestination().getId() == R.id.navigation_lista_produtos) {
                navController.navigate(R.id.action_navigation_lista_produtos_to_navigation_home);
            } else if (navController.getCurrentDestination().getId() == R.id.navigation_home) {
                navController.navigate(R.id.action_navigation_home_self);
            }
        });
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_pesquisar, R.id.navigation_home, R.id.navigation_carrinho).build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            switch (destination.getId()) {
                case R.id.navigation_lista_produtos:
                    if (navView != null) {
                        navView.setVisibility(View.VISIBLE);
                    }
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setDisplayOptions(displayOption);
                        getSupportActionBar().setTitle(arguments.getString("title"));
                    }
                    break;
                case R.id.navigation_home:
                    if (navView != null) {
                        navView.setVisibility(View.VISIBLE);
                    }
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setCustomView(null);
                        displayOption = getSupportActionBar().getDisplayOptions();
                        AppCompatTextView mTitleTextView = new AppCompatTextView(getApplicationContext());
                        mTitleTextView.setSingleLine();
                        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.CENTER;
                        getSupportActionBar().setCustomView(mTitleTextView, layoutParams);
                        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP);
                        mTitleTextView.setText("RoyalFarma");
//                        mTitleTextView.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium);
                        mTitleTextView.setTextColor(getResources().getColor(R.color.white));
                        mTitleTextView.setTextSize(20);
                    }
                    break;
                case R.id.navigation_login:
                case R.id.navigation_finalizar:
                case R.id.navigation_acompanhamento:
                    if (navView != null) {
                        navView.setVisibility(View.GONE);
                    }
                    break;
                default:
                    if (navView != null) {
                        navView.setVisibility(View.VISIBLE);
                    }
            }
        });
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

//    private void updateUI(Message msg) {
//        // 0 = status da conexao ao banco
//        // 1 = status da async task
//        //também mostra ao usuario que estamos tentando conectar
//        if (msg.what == 0) {
//            if ("tentando_conectar".equals(msg.obj)) {
//                toggleCircularBarBtn(true);
//            }
//        } else if (msg.what == 1) {
//            switch ((String) msg.obj) {
//                case "onPreExecute":
//                    toggleCircularBarBtn(true);
//                    break;
//                case "autenticado":
//                    resultLogin(true);
//                    toggleCircularBarBtn(false);
//                    break;
//                case "erro_autenticacao":
//                    resultLogin(false);
//                    toggleCircularBarBtn(false);
//                    break;
//                case "erro_sql":
//                    Toast.makeText(this, "Erro de SQL", Toast.LENGTH_SHORT).show();
//                    toggleCircularBarBtn(false);
//                case "erro_sha":
//                    Toast.makeText(this, "Erro codificação senha", Toast.LENGTH_SHORT).show();
//                case "onPostExecute":
//                    toggleCircularBarBtn(false);
//                    break;
//            }
//        } else {
//            Toast.makeText(this, "Ocorreu um erro!", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void toggleCircularBarBtn(boolean circularBarVisibility) {
//        final ViewGroup root = this.findViewById(R.id.activity_login_root_layout);
//        Button btn = findViewById(R.id.buttonLogin);
//        TransitionManager.beginDelayedTransition(root, new Fade());
//        if (circularBarVisibility) {
//            circularBar.setVisibility(View.VISIBLE);
//            btn.setVisibility(View.INVISIBLE);
//        } else {
//            btn.setVisibility(View.VISIBLE);
//            circularBar.setVisibility(View.INVISIBLE);
//        }
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.search_option_menu, menu);
//
//        // Associate searchable configuration with the SearchView
//        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        searchView = (SearchView) menu.findItem(R.id.search).getActionView();

//        searchView.setSubmitButtonEnabled(true);
//        return true;
//    }


    @Override
    public void onBackPressed() {
        if (navView.getSelectedItemId() == R.id.navigation_home) {
            if (confirmQuitApp()) {
                finishAffinity();
            } else {
                Toast.makeText(this, "Clique mais uma vez para sair", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (navController.getCurrentDestination().getId() == R.id.navigation_acompanhamento) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle("Atenção")
                        .setMessage("Se você sair desta tela não poderá acompanhar seu pedido.")
                        .setPositiveButton("Sair", (dialog, which) -> super.onBackPressed())
                        .setNegativeButton("Cancelar", (dialog, which) -> {})
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                super.onBackPressed();
            }
        }
    }
}
