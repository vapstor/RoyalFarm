package br.com.royalfarma;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import br.com.royalfarma.model.Produto;

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
//            toolbar.setBackground(getDrawable(R.drawable.android_01));
            toolbar.setTitle(produto.getNome());
            setSupportActionBar(toolbar);

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show());
        }
    }

    public void expandeImagemItem(View view) {
        Toast.makeText(this, "Expandida!", Toast.LENGTH_SHORT).show();
    }
}
