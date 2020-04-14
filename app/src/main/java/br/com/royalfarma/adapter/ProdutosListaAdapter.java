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

import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import br.com.royalfarma.R;
import br.com.royalfarma.activitys.ProductDetailActivity;
import br.com.royalfarma.holder.ProdutosListaViewHolder;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.utils.RoundedBackgroundSpan;
import br.com.royalfarma.utils.RoundedTransformation;

import static android.widget.Toast.makeText;
import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.RSmask;

public class ProdutosListaAdapter extends RecyclerView.Adapter {

    private final Toast toast;
    private Context context;
    private ArrayList<Produto> itens;

    public ProdutosListaAdapter(ArrayList<Produto> itens, Context context) {
        this.context = context;
        this.itens = itens;
        toast = makeText(context, "", Toast.LENGTH_LONG);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_produto_listagem, parent, false);
        return new ProdutosListaViewHolder(view);
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

        ProdutosListaViewHolder itemProdutoHolder = (ProdutosListaViewHolder) holder;
        Produto item = itens.get(position);

        descricaoItem = itemProdutoHolder.tituloItemProduto;
        AppCompatImageButton btnViewDetail = itemProdutoHolder.visualizar;
        descricaoItem.setText(item.getNome());
        descricaoItem.setOnLongClickListener(v -> {
            makeText(context, item.getNome(), Toast.LENGTH_SHORT).show();
            return true;
        });

        valorTotalLabel = itemProdutoHolder.precoItemProduto;

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
            item.setValorTotal(precoEmOferta);
            valorTotalLabel.setText(ssb, TextView.BufferType.EDITABLE);
        } else {
            if (item.getQuantidade() > 0) {
                item.setValorTotal(RSmask(item.getQuantidade() * item.getPreco()));
                valorTotalLabel.setText(RSmask(item.getPreco() * item.getQuantidade()));
            } else {
                item.setValorTotal(RSmask(item.getPreco()));
                valorTotalLabel.setText(RSmask(item.getPreco()));
            }
        }

        AppCompatImageView imagemItem = itemProdutoHolder.imgProduto;
        String url = "https://www.royalfarma.com.br/uploads/" + item.getImagemURL();

        //LOADER
        itemProdutoHolder.progressBar.setVisibility(View.VISIBLE);
        imagemItem.setVisibility(View.VISIBLE);
        final ProgressBar progressView = itemProdutoHolder.progressBar;
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
        imagemItem.setOnClickListener(v -> openDetailProductActivity(item));
        btnViewDetail.setOnClickListener(v -> openDetailProductActivity(item));
    }

    private void openDetailProductActivity(Produto produto) {
        Gson gson = new Gson();
        Intent intent = new Intent(context, ProductDetailActivity.class);
        Bundle bundle = makeSceneTransitionAnimation(Objects.requireNonNull((AppCompatActivity) context)).toBundle();
        if (bundle != null) {
            String product = gson.toJson(produto);
            intent.putExtra("selectedProduct", product);
            context.startActivity(intent, bundle);
        } else {
            Toast.makeText(context, "Erro ao empacotar produto!", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    public long getItemId(int position) {
        return itens.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }
}
