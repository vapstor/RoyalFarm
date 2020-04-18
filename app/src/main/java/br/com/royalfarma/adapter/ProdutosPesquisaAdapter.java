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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.activitys.ProductDetailImageActivity;
import br.com.royalfarma.holder.ProdutosCarrinhoViewHolder;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.utils.RoundedBackgroundSpan;
import br.com.royalfarma.utils.RoundedTransformation;

import static android.widget.Toast.makeText;
import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.RSmask;

public class ProdutosPesquisaAdapter extends RecyclerView.Adapter<ProdutosCarrinhoViewHolder> {

    private final Context context;
    private final ArrayList<Produto> itens;
    private final Toast toast;

    public ProdutosPesquisaAdapter(ArrayList<Produto> itens, Context context) {
        this.context = context;
        this.itens = itens;
        toast = makeText(context, "", Toast.LENGTH_LONG);
    }

    @NonNull
    @Override
    public ProdutosCarrinhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_item_search, parent, false);
        return new ProdutosCarrinhoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutosCarrinhoViewHolder holder, int position) {
        Produto item = itens.get(position);

        AppCompatTextView descricaoItem = holder.tituloItemProduto;
        descricaoItem.setText(item.getNome());
        descricaoItem.setOnLongClickListener(v -> {
            makeText(context, item.getNome(), Toast.LENGTH_SHORT).show();
            return true;
        });

        AppCompatTextView preco = holder.precoItemProduto;
        //Preco
        if (item.isDesconto()) {
            String precoNormal, precoEmOferta;
            if (item.getQtdNoCarrinho() > 1) {
                precoNormal = RSmask(item.getPreco() * item.getQtdNoCarrinho());
                precoEmOferta = RSmask(item.getPrecoOferta() * item.getQtdNoCarrinho());
            } else {
                precoNormal = RSmask(item.getPreco());
                precoEmOferta = RSmask(item.getPrecoOferta());
            }
            SpannableStringBuilder ssb = configValueWithDiscount(precoNormal, precoEmOferta);
            preco.setText(ssb, TextView.BufferType.EDITABLE);
            item.setValorTotal(precoEmOferta);
        } else {
            if (item.getQtdNoCarrinho() > 1) {
                item.setValorTotal(RSmask(item.getPreco() * item.getQtdNoCarrinho()));
                preco.setText(RSmask(item.getPreco() * item.getQtdNoCarrinho()));
            } else {
                item.setValorTotal(RSmask(item.getPreco()));
                preco.setText(RSmask(item.getPreco()));
            }
        }

        //Imagem
        AppCompatImageView imagemItem = holder.imgProduto;
        String url = "https://www.royalfarma.com.br/uploads/" + item.getImagemURL();

        //LOADER
        final ProgressBar progressView = holder.progressBar;
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
        //fim imagem

        //Open detail page
        String finalUrl = url;
        imagemItem.setOnClickListener(v -> openDetailImageActivity(finalUrl));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    private void openDetailImageActivity(String url) {
        Intent intent = new Intent(context, ProductDetailImageActivity.class);
        Bundle bundle = makeSceneTransitionAnimation((AppCompatActivity) context).toBundle();
        intent.putExtra("imageURL", url);
        context.startActivity(intent, bundle);
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
}
