package br.com.royalfarma;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import br.com.royalfarma.model.Produto;

import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static br.com.royalfarma.utils.Util.MY_LOG_TAG;

public class ProductDetailActivity extends AppCompatActivity {

    private AppCompatImageView imagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Produto produto;
        Bundle extras = getIntent().getExtras();
        Gson gson = new Gson();
        if (extras == null) {
            Toast.makeText(this, "Ocorreu um erro!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            String product = extras.getString("selectedProduct");
            produto = gson.fromJson(product, Produto.class);
            setContentView(R.layout.activity_product_detail);
            Toolbar toolbar = findViewById(R.id.toolbar);
            imagem = findViewById(R.id.header);

            String url = "https://www.royalfarma.com.br/uploads/" + produto.getImagemURL();

            if (produto.getImagemURL() == null || produto.getImagemURL().equals("")) {
                url = "drawable/empty_product_image";
                int productImageId = this.getResources().getIdentifier(url, "drawable", getPackageName());
                Picasso.get().load(productImageId).into(imagem);
            } else {
                Log.d(MY_LOG_TAG, "URL: " + url);
                Log.d(MY_LOG_TAG, "Product Image URL DB: " + produto.getImagemURL());
                Picasso.get().load(url).into(imagem);
            }

            String finalUrl = url;
            imagem.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProductDetailImage.class);
                Bundle bundle = makeSceneTransitionAnimation(Objects.requireNonNull(this)).toBundle();
                intent.putExtra("imageURL", finalUrl);
                startActivity(intent, bundle);
            });
//            toolbar.setBackground(getDrawable(R.drawable.android_01));
            toolbar.setTitle(produto.getNome());
            setSupportActionBar(toolbar);

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show());
        }
    }

}
