package br.com.royalfarma.activitys;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import br.com.royalfarma.R;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemReselectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                finishAffinity();
                overridePendingTransition(0,0);
                startActivity(getIntent());
                overridePendingTransition(0,0);
//                navController.navigate(R.id.action_navigation_main_activity_to_navigation_home);
            }
        });
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_pesquisar, R.id.navigation_home, R.id.navigation_carrinho).build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                Log.d(MY_LOG_TAG, " ID -> " + destination.getId());
                switch (destination.getId()) {
                    case R.id.navigation_lista_produtos:
                        getSupportActionBar().setTitle(arguments.getString("title"));
                        break;
                    case R.id.navigation_home:
                        getSupportActionBar().setTitle("RoyalFarma");
                        break;
                }
            }
        });
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
}
//    @Override
//    public void onBackPressed() {
//        if (navView.getSelectedItemId() == R.id.navigation_home) {
//            Bundle bundle = makeSceneTransitionAnimation(this).toBundle();
//            Intent intent = new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent, bundle);
//        } else {
//            super.onBackPressed();
//        }
//    }
//}
