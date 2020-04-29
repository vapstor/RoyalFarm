package br.com.royalfarma.activitys;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import br.com.royalfarma.R;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;
import br.com.royalfarma.utils.RoundedBackgroundSpan;

import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.RSmask;

public class ProductDetail extends AppCompatActivity {

    private AppCompatImageView imagem;
    private Produto produto;
    private AppCompatTextView tvTitulo, tvPreco, tvQuantidadeEstoque, tvDescricao;
    private boolean fromCart;
    private FloatingActionButton fabCart;
    private CarrinhoViewModel carrinhoViewModel;
    private int qntdAddCart = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Toast.makeText(this, "Ocorreu um erro!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            produto = extras.getParcelable("selectedProduct");
            fromCart = extras.getBoolean("fromCart");
            carrinhoViewModel = new ViewModelProvider(this).get(CarrinhoViewModel.class);
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
            fabCart.setOnClickListener(view -> {
                AppCompatTextView textView = new AppCompatTextView(this);
                textView.setText("Selecione uma quantidade");
                textView.setPadding(20, 30, 20, 30);
                textView.setTextSize(18F);
                textView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                textView.setTextColor(Color.WHITE);
                final MaterialAlertDialogBuilder d = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded);
                d.setCustomTitle(textView);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.layout_dialog_item_quantity, null);
                d.setView(dialogView);
                final NumberPicker numberPicker = dialogView.findViewById(R.id.itemQtdNumberPicker);
                numberPicker.setMaxValue(produto.getEstoqueAtual());
                numberPicker.setMinValue(1);
                numberPicker.setWrapSelectorWheel(false);
                //                        numberPicker.setOnValueChangedListener((numberPicker1, i, i1) -> {
//                        });

                d.setPositiveButton("Selecionar", (dialogInterface, i) -> {
                    retornaIntent(produto, numberPicker.getValue());
                });
                d.setNegativeButton("Cancelar", (dialogInterface, i) -> {
                });
                AlertDialog alertDialog = d.create();
                alertDialog.show();
            });
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
        tvQuantidadeEstoque.setText("Estoque: " + produto.getEstoqueAtual());
        tvDescricao.setText(R.string.este_produto_nao_contem_descricao);
    }

    private void retornaIntent(Produto produto, int qntdAddCart) {
        Intent devolve = new Intent();
        devolve.putExtra("selectedProduct", produto);
        devolve.putExtra("qntdAddCart", qntdAddCart);
        setResult(RESULT_OK, devolve);
        finish();
    }

}