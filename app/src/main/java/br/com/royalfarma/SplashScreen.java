package br.com.royalfarma;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static java.lang.Thread.sleep;

public class SplashScreen extends AppCompatActivity {

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
                SplashScreen.this.runOnUiThread(() -> {
                    Bundle bundle = makeSceneTransitionAnimation(SplashScreen.this).toBundle();
                    Intent intent = new Intent(SplashScreen.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    SplashScreen.this.startActivity(intent, bundle);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
