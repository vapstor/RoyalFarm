package br.com.royalfarma;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;

public class MainActivity extends AppCompatActivity {
    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private BottomNavigationView navView;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_pesquisar, R.id.navigation_home, R.id.navigation_carrinho).build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        navView.setSelectedItemId(R.id.navigation_home);
//        navView.getOrCreateBadge(R.id.navigation_carrinho);
    }

    @Override
    public void onBackPressed() {
        if (navView.getSelectedItemId() == R.id.navigation_home) {
            Bundle bundle = makeSceneTransitionAnimation(this).toBundle();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent, bundle);
        } else {
            super.onBackPressed();
        }
    }
}
