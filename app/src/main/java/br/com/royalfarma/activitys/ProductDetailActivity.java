package br.com.royalfarma.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import br.com.royalfarma.R;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.utils.RoundedBackgroundSpan;

import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.RSmask;

public class ProductDetailActivity extends AppCompatActivity {

    private AppCompatImageView imagem;
    private Produto produto;
    private AppCompatTextView tvTitulo, tvPreco, tvQuantidadeEstoque, tvDescricao;
    private boolean fromCart;
    private FloatingActionButton fabCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        Gson gson = new Gson();
        if (extras == null) {
            Toast.makeText(this, "Ocorreu um erro!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            fromCart = extras.getBoolean("fromCart");
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
                Intent intent = new Intent(this, ProductDetailImageActivity.class);
                Bundle bundle = makeSceneTransitionAnimation(Objects.requireNonNull(this)).toBundle();
                intent.putExtra("imageURL", finalUrl);
                startActivity(intent, bundle);
            });
//            toolbar.setBackground(getDrawable(R.drawable.android_01));
            toolbar.setTitle(produto.getNome());
            setSupportActionBar(toolbar);

            fabCart = findViewById(R.id.fab);
            fabCart.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show());
            tvTitulo = findViewById(R.id.tvTitulo);
            tvPreco = findViewById(R.id.tvPreco);
            tvQuantidadeEstoque = findViewById(R.id.tvQuantidadeEstoque);
            tvDescricao = findViewById(R.id.tvDescricao);
        }
    }

    @Override
    protected void onStart() {
        if (fromCart) {
            fabCart.setVisibility(View.GONE);
        } else {
            fabCart.setVisibility(View.VISIBLE);
        }
        setTextViews();
        super.onStart();
    }

    private void setTextViews() {
        String precoNormal = RSmask(produto.getPreco());
        String precoOferta = RSmask(produto.getPreco());
        tvTitulo.setText(produto.getNome());
        tvPreco.setText(RSmask(produto.getPreco()));
        if (produto.isDesconto()) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(precoNormal);
            StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
            ssb.setSpan(strikethroughSpan, 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append("  ").append(RSmask(produto.getPrecoOferta())).append(" ");
            ssb.setSpan(new RoundedBackgroundSpan(this), ssb.length() - precoNormal.length() - 2, ssb.length() - precoNormal.length() + precoOferta.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvPreco.setText(ssb, TextView.BufferType.EDITABLE);
        } else {
            tvPreco.setText(precoNormal);
        }
        tvQuantidadeEstoque.setText(String.valueOf(produto.getEstoqueAtual()));
        tvDescricao.setText(R.string.este_produto_nao_contem_descricao);
    }


}
