package br.com.royalfarma.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.activitys.ProductDetailImageActivity;
import br.com.royalfarma.holder.ProdutosCarrinhoViewHolder;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;
import br.com.royalfarma.utils.RoundedBackgroundSpan;
import br.com.royalfarma.utils.RoundedTransformation;

import static android.widget.Toast.makeText;
import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.RSmask;

public class ProdutosCarrinhoAdapter extends RecyclerView.Adapter {

    private final Toast toast;
    private final CarrinhoViewModel carrinhoViewModel;
    private Context context;
    private ArrayList<Produto> itens;
    private AppCompatTextView qntdItem;
    private AppCompatImageButton qntdMinus, qntdPlus;
    private BottomNavigationView navBottomView;
    private int badgePreviousValue;
    private BadgeDrawable badgeDrawable;


    public ProdutosCarrinhoAdapter(ArrayList<Produto> itens, Context context, CarrinhoViewModel carrinhoViewModel) {
        this.context = context;
        this.itens = itens;
        toast = makeText(context, "", Toast.LENGTH_LONG);
        navBottomView = ((AppCompatActivity) this.context).findViewById(R.id.nav_view);
        this.carrinhoViewModel = carrinhoViewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_cartlist_item, parent, false);
        return new ProdutosCarrinhoViewHolder(view);
    }

    // Clean all elements of the recycler
    public void clear() {
        itens.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(ArrayList<Produto> list) {
        itens.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AppCompatTextView descricaoItem, valorTotalLabel;

        ProdutosCarrinhoViewHolder produtoCarrinhoHolder = (ProdutosCarrinhoViewHolder) holder;
        Produto item = itens.get(position);

        qntdItem = produtoCarrinhoHolder.qntdItem;
        qntdItem.setText(String.valueOf(item.getQuantidade()));

        qntdMinus = produtoCarrinhoHolder.qntdMinus;
        qntdPlus = produtoCarrinhoHolder.qntdPlus;

        descricaoItem = produtoCarrinhoHolder.tituloItemProduto;
        descricaoItem.setText(item.getNome());
        descricaoItem.setOnLongClickListener(v -> {
            makeText(context, item.getNome(), Toast.LENGTH_SHORT).show();
            return true;
        });

        valorTotalLabel = produtoCarrinhoHolder.precoItemProduto;

        if (item.isDesconto()) {
            String precoNormal, precoEmOferta;
            if (item.getQuantidade() > 0) {
                precoNormal = RSmask(item.getPreco() * item.getQuantidade());
                precoEmOferta = RSmask(item.getPrecoOferta() * item.getQuantidade());
            } else {
                precoNormal = RSmask(item.getPreco());
                precoEmOferta = RSmask(item.getPrecoOferta());
            }
            SpannableStringBuilder ssb = configValueWithDiscount(precoNormal, precoEmOferta);
            valorTotalLabel.setText(ssb, TextView.BufferType.EDITABLE);
            item.setValorTotal(precoEmOferta);
        } else {
            if (item.getQuantidade() > 0) {
                item.setValorTotal(RSmask(item.getPreco() * item.getQuantidade()));
                valorTotalLabel.setText(RSmask(item.getPreco() * item.getQuantidade()));
            } else {
                item.setValorTotal(RSmask(item.getPreco()));
                valorTotalLabel.setText(RSmask(item.getPreco()));
            }
        }

        AppCompatImageView imagemItem = produtoCarrinhoHolder.imgProduto;
        String url = "https://www.royalfarma.com.br/uploads/" + item.getImagemURL();

        //LOADER
        final ProgressBar progressView = produtoCarrinhoHolder.progressBar;
        progressView.setVisibility(View.VISIBLE);
        imagemItem.setVisibility(View.VISIBLE);

        final Callback loadedCallback = new Callback() {
            @Override
            public void onSuccess() {
                progressView.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                String url = "drawable/empty_product_image";
                int productImageId = context.getResources().getIdentifier(url, "drawable", context.getPackageName());
                Picasso.get().load(productImageId).into(imagemItem);
            }
        };
        imagemItem.setTag(loadedCallback);
        if (item.getImagemURL() == null || item.getImagemURL().equals("")) {
            url = "drawable/empty_product_image";
            int productImageId = this.context.getResources().getIdentifier(url, "drawable", context.getPackageName());
            Picasso.get()
                    .load(productImageId)
                    .transform(new RoundedTransformation(15, 0))
                    .fit()
                    .into(imagemItem, loadedCallback);
        } else {
            Log.d(MY_LOG_TAG, "URL: " + url);
            Log.d(MY_LOG_TAG, "Product Image URL DB: " + item.getImagemURL());
            Picasso.get()
                    .load(url)
                    .transform(new RoundedTransformation(15, 0))
                    .fit()
                    .into(imagemItem, loadedCallback);
        }
        //FIM LOADER

        //Open detail page
        String finalUrl = url;
        imagemItem.setOnClickListener(v -> openDetailImageActivity(finalUrl));

        qntdPlus.setClickable(true);
        if (item.getQuantidade() > 0) {
            qntdMinus.setEnabled(true);
            qntdMinus.setClickable(true);
        } else {
            qntdMinus.setClickable(false);
            qntdMinus.setEnabled(false);
        }

        badgeDrawable = navBottomView.getBadge(R.id.navigation_carrinho);

        qntdPlus.setOnClickListener(v -> {
            if (item.getQuantidade() + 1 < item.getEstoqueAtual()) {
                qntdMinus.setEnabled(true);
                item.setQuantidade(item.getQuantidade() + 1);
                badgePreviousValue = badgeDrawable.getNumber();
                badgeDrawable.setNumber(badgePreviousValue + 1);
                carrinhoViewModel.updateItemCartQuantity(item);
            } else {
                qntdPlus.setClickable(true);
                qntdPlus.setEnabled(true);
                Snackbar.make(v, "Não há mais produtos suficientes no estoque", Snackbar.LENGTH_LONG).setActionTextColor(context.getResources().getColor(R.color.colorSecondaryLight)).setAction("Fechar", v1 -> {
                }).show();
            }
        });

        qntdMinus.setOnClickListener(v -> {
            qntdPlus.setEnabled(true);
            if (item.getQuantidade() > 0) {
                item.setQuantidade(item.getQuantidade() - 1);
                badgePreviousValue = badgeDrawable.getNumber();
                badgeDrawable.setNumber(badgePreviousValue - 1);
                carrinhoViewModel.updateItemCartQuantity(item);
            } else {
                qntdMinus.setEnabled(false);
                Snackbar.make(v, "Não é possível diminuir", Snackbar.LENGTH_LONG).setActionTextColor(context.getResources().getColor(R.color.colorSecondaryLight)).setAction("Fechar", v1 -> {
                }).show();
            }
        });

    }

    private SpannableStringBuilder configValueWithDiscount(String precoNormal, String precoEmOferta) {
        // Use a SpannableStringBuilder so that both the text and the spans are mutable
        SpannableStringBuilder ssb = new SpannableStringBuilder(precoNormal);

        // Create a span that will strikethrough the text
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();

        // Add the secondWord and apply the strikethrough span to only the second word
        ssb.setSpan(
                strikethroughSpan,
                0,
                ssb.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ssb.append("  ").append(precoEmOferta).append(" ");

        ssb.setSpan(new RoundedBackgroundSpan(context), ssb.length() - precoNormal.length() - 2, ssb.length() - precoNormal.length() + precoEmOferta.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // SPAN_EXCLUSIVE_EXCLUSIVE means to not extend the span when additional
        // Set the TextView text and denote that it is Editable
        // since it's a SpannableStringBuilder
        return ssb;
    }

    private void openDetailImageActivity(String url) {
        Intent intent = new Intent(context, ProductDetailImageActivity.class);
        Bundle bundle = makeSceneTransitionAnimation((AppCompatActivity) context).toBundle();
        intent.putExtra("imageURL", url);
        context.startActivity(intent, bundle);
    }

    @Override
    public long getItemId(int position) {
        return itens.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }
}
