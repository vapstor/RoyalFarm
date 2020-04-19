package br.com.royalfarma.adapter;

import android.content.Context;
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
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import br.com.royalfarma.R;
import br.com.royalfarma.holder.ProdutosHomeViewHolder;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;
import br.com.royalfarma.ui.home.ProductsViewModel;
import br.com.royalfarma.utils.RoundedBackgroundSpan;
import br.com.royalfarma.utils.RoundedTransformation;

import static android.widget.Toast.makeText;
import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.RSmask;

public class ProdutosHomeAdapter extends RecyclerView.Adapter {

    private final String owner;
    private ProductsViewModel productsViewModel;
    private CarrinhoViewModel carrinhoViewModel;
    private final Toast toast;
    private Context context;
    private ArrayList<Produto> itens;
    private int badgePreviousValue;
    private BottomNavigationView navBottomView;
    private BadgeDrawable badgeDrawable;

    public ProdutosHomeAdapter(ArrayList<Produto> itens, Context context, CarrinhoViewModel carrinhoViewModel, ProductsViewModel productsViewModel, String owner) {
        this.context = context;
        this.itens = itens;
        toast = makeText(context, "", Toast.LENGTH_LONG);
        this.carrinhoViewModel = carrinhoViewModel;
        this.productsViewModel = productsViewModel;
        this.owner = owner;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_produto_home, parent, false);
        return new ProdutosHomeViewHolder(view);
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
        AppCompatTextView qntdItem, descricaoItem, valorTotalLabel;
        AppCompatImageButton qntdMinus, qntdPlus;
        AppCompatButton btnAddCarrinho;
        ProdutosHomeViewHolder itemProdutoHolder = (ProdutosHomeViewHolder) holder;

        //Check Necessário pois para alterar o item correto
        int realPosition;
        if ("populares".equals(owner)) {
            realPosition = position + 5;
        } else if ("mais_vendidos".equals(owner)) {
            realPosition = position + 10;
        } else {
            realPosition = position;
        }

        Produto item = itens.get(position);

        descricaoItem = itemProdutoHolder.tituloItemProduto;
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

            item.setValorTotal(precoEmOferta);
            SpannableStringBuilder ssb = configValueWithDiscount(precoNormal, precoEmOferta);
            valorTotalLabel.setText(ssb, TextView.BufferType.EDITABLE);
        } else {
            if (item.getQuantidade() > 0) {
                item.setValorTotal(RSmask(item.getPreco() * item.getQuantidade()));
                valorTotalLabel.setText(RSmask(item.getPreco() * item.getQuantidade()));
            } else {
                item.setValorTotal(RSmask(item.getPreco()));
                valorTotalLabel.setText(RSmask(item.getPreco()));
            }
        }

        qntdItem = itemProdutoHolder.qntdItem;
        qntdItem.setText(String.valueOf(item.getQuantidade()));

        qntdMinus = itemProdutoHolder.qntdMinus;
        qntdPlus = itemProdutoHolder.qntdPlus;

        btnAddCarrinho = itemProdutoHolder.addToCart;

        if (item.getQuantidade() > 0) {
            if (item.getQtdNoCarrinho() < item.getEstoqueAtual()) {
                qntdItem.setTextColor(context.getResources().getColor(R.color.colorAccent));
                btnAddCarrinho.setEnabled(true);
            } else {
                qntdItem.setTextColor(context.getResources().getColor(R.color.colorAccent));
                btnAddCarrinho.setEnabled(false);
            }
        } else {
            qntdItem.setTextColor(context.getResources().getColor(R.color.white));
            btnAddCarrinho.setEnabled(false);
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

        //Add QTD do item
        qntdPlus.setOnClickListener(v -> {
            if (item.getQuantidade() < item.getEstoqueAtual()) {
                item.setQuantidade(item.getQuantidade() + 1);
                productsViewModel.updateProduct(realPosition);
            } else {
                Snackbar.make(v, "Sem estoque", Snackbar.LENGTH_LONG).setActionTextColor(context.getResources().getColor(R.color.colorSecondaryLight)).setAction("Fechar", v1 -> {
                }).show();
            }
        });

        //Remove QTD do item
        qntdMinus.setOnClickListener(v -> {
            if (item.getQuantidade() == 0) {
                Snackbar.make(v, "Não é possível diminuir", Snackbar.LENGTH_LONG).setActionTextColor(context.getResources().getColor(R.color.colorSecondaryLight)).setAction("Fechar", v1 -> {
                }).show();
            } else {
                if (btnAddCarrinho.isEnabled()) {
                    if (item.getQuantidade() == 1) {
                        item.setQuantidade(0);
                        productsViewModel.updateProduct(realPosition);
                    } else {
                        item.setQuantidade(item.getQuantidade() - 1);
                        productsViewModel.updateProduct(realPosition);
                    }
                } else {
                    makeText(context, "Não permitido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Adiciona ao carrinho
        btnAddCarrinho.setOnClickListener(v -> {
            int qntdItensAdd = item.getQuantidade();
            if (qntdItensAdd + item.getQtdNoCarrinho() > item.getEstoqueAtual()) {

                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.atencao)
                        .setMessage("Existem apenas " + item.getEstoqueAtual() + " produtos no estoque. Deseja adicionar esta quantidade?")
                        .setNegativeButton(R.string.cancelar, (dialog, which) -> {
                        })
                        .setPositiveButton(R.string.adicionar, (dialog, which) -> {
                            //Check na quantidade adicionada
                            item.setQtdNoCarrinho(item.getEstoqueAtual());
                            carrinhoViewModel.updateSubtotal();
                            carrinhoViewModel.updateProductOnCartList(item);

                            String label, labelExclusao;
                            if (item.getEstoqueAtual() > 1) {
                                label = " Itens foram adicionados ao carrinho.";
                                labelExclusao = " Itens removidos do carrinho.";
                            } else {
                                label = " Item foi adicionado ao carrinho.";
                                labelExclusao = "Itens removidos do carrinho.";
                            }

                            carrinhoViewModel.updateSubtotal();

                            Snackbar.make(v, item.getEstoqueAtual() + label, Snackbar.LENGTH_SHORT).setActionTextColor(context.getResources().getColor(R.color.colorSecondaryLight)).setAction("Desfazer", v1 -> {
                                //Remove o último adicionado
                                item.setQtdNoCarrinho(-item.getEstoqueAtual());
                                carrinhoViewModel.updateProductOnCartList(item);
                                productsViewModel.updateProduct(realPosition);
                                carrinhoViewModel.updateSubtotal();
                                carrinhoViewModel.updateBadgeDisplay();
                                notifyItemChanged(position);
                                makeText(context, labelExclusao, Toast.LENGTH_SHORT).show();
                            }).show();

                            notifyItemChanged(position);
                            item.setQtdNoCarrinho(item.getEstoqueAtual());
                            carrinhoViewModel.updateBadgeDisplay();
                        }).show();
            } else {
                item.setQtdNoCarrinho(item.getQtdNoCarrinho() + qntdItensAdd);
                carrinhoViewModel.addProductToCart(item);

                String label, labelExclusao;
                if (qntdItensAdd > 1) {
                    label = " Itens foram adicionados ao carrinho.";
                    labelExclusao = " Itens removidos do carrinho.";
                } else {
                    label = " Item foi adicionado ao carrinho.";
                    labelExclusao = "Itens removidos do carrinho.";
                }

                int finalQntdItensAdd = qntdItensAdd;
                Snackbar.make(v, qntdItensAdd + label, Snackbar.LENGTH_SHORT).setActionTextColor(context.getResources().getColor(R.color.colorSecondaryLight)).setAction("Desfazer", v1 -> {
                    //Remove o último adicionado
                    item.setQtdNoCarrinho(item.getQtdNoCarrinho() - finalQntdItensAdd);

                    carrinhoViewModel.updateProductOnCartList(item);
                    productsViewModel.updateProduct(realPosition);
                    notifyItemChanged(position);
                    carrinhoViewModel.updateBadgeDisplay();

                    makeText(context, labelExclusao, Toast.LENGTH_SHORT).show();
                }).show();

                notifyItemChanged(position);
                carrinhoViewModel.updateBadgeDisplay();
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

    private void openDetailProductActivity(Produto produto) {
        Bundle bundle = makeSceneTransitionAnimation(Objects.requireNonNull((AppCompatActivity) context)).toBundle();
        if (bundle != null) {
            bundle.putParcelable("selectedProduct", produto);
//            //T
//            context.startActivity(intent, bundle);
        } else {
            Toast.makeText(context, "Erro ao empacotar produto!", Toast.LENGTH_SHORT).show();
        }
        //TODO navigation

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
