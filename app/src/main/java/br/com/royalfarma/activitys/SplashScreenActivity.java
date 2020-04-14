package br.com.royalfarma.activitys;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import br.com.royalfarma.R;

import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static java.lang.Thread.sleep;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(() -> {
            try {
                sleep(500);
                SplashScreenActivity.this.runOnUiThread(() -> {
                    Bundle bundle = makeSceneTransitionAnimation(SplashScreenActivity.this).toBundle();
                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    SplashScreenActivity.this.startActivity(intent, bundle);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
