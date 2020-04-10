package br.com.royalfarma.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import br.com.royalfarma.ProductDetailActivity;
import br.com.royalfarma.R;
import br.com.royalfarma.holder.ProdutosViewHolder;
import br.com.royalfarma.model.Produto;

import static android.widget.Toast.makeText;
import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.RSmask;

public class ProdutosAdapter extends RecyclerView.Adapter {

    private final Toast toast;
    private Context context;
    private ArrayList<Produto> itens;
    private BottomNavigationView navBottomView;
    private int badgePreviousValue;

    public ProdutosAdapter(ArrayList<Produto> itens, Context context) {
        this.context = context;
        this.itens = itens;
        toast = makeText(context, "", Toast.LENGTH_LONG);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_produto, parent, false);
        return new ProdutosViewHolder(view);
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

        ProdutosViewHolder itemProdutoHolder = (ProdutosViewHolder) holder;
        Produto item = itens.get(position);

        descricaoItem = itemProdutoHolder.tituloItemProduto;
        descricaoItem.setText(item.getNome());

        valorTotalLabel = itemProdutoHolder.precoItemProduto;
        valorTotalLabel.setText(RSmask(item.getPreco()));

        qntdItem = itemProdutoHolder.qntdItem;
        qntdItem.setText(String.valueOf(item.getQuantidade()));

        qntdMinus = itemProdutoHolder.qntdMinus;
        qntdPlus = itemProdutoHolder.qntdPlus;

        btnAddCarrinho = itemProdutoHolder.addToCart;

        if (item.getQuantidade() > 0) {
            qntdItem.setTextColor(context.getResources().getColor(R.color.colorAccent));
            btnAddCarrinho.setEnabled(true);
        }

        AppCompatImageView imagemItem = itemProdutoHolder.imgProduto;
        String url = "https://www.royalfarma.com.br/uploads/" + item.getImagemURL();

        if (item.getImagemURL() == null || item.getImagemURL().equals("")) {
            url = "drawable/empty_product_image";
            int productImageId = this.context.getResources().getIdentifier(url, "drawable", context.getPackageName());
            Picasso.get().load(productImageId).into(imagemItem);
        } else {
            Log.d(MY_LOG_TAG, "URL: " + url);
            Log.d(MY_LOG_TAG, "Product Image URL DB: " + item.getImagemURL());
            Picasso.get().load(url).into(imagemItem);
        }

        //Open detail page
        imagemItem.setOnClickListener(v -> openDetailProductActivity(item));

        qntdPlus.setOnClickListener(v -> {
            item.setQuantidade(item.getQuantidade() + 1);
            if (item.getQuantidade() <= item.getEstoqueAtual()) {
                qntdItem.setTextColor(context.getResources().getColor(R.color.colorAccent));
                if (!btnAddCarrinho.isEnabled())
                    btnAddCarrinho.setEnabled(true);
                if (item.getQuantidade() == 1) {
                    item.setTotalPrecoItem(item.getPreco());
                } else {
                    item.setTotalPrecoItem(item.getTotalPrecoItem() + item.getPreco());
                    valorTotalLabel.setText(RSmask(item.getTotalPrecoItem()));
                }
                qntdItem.setText(String.valueOf(item.getQuantidade()));
            } else {
                makeText(context, "Não há produtos suficientes no estoque", Toast.LENGTH_SHORT).show();
            }
        });

        qntdMinus.setOnClickListener(v -> {
            if (item.getQuantidade() == 0) {
                makeText(context, "Não é possível diminuir", Toast.LENGTH_SHORT).show();
            } else if (item.getQuantidade() == 1) {
                item.setQuantidade(0);
                item.setTotalPrecoItem(item.getPreco());
                valorTotalLabel.setText(RSmask(item.getTotalPrecoItem()));
                qntdItem.setText(String.valueOf(item.getQuantidade()));
                qntdItem.setTextColor(context.getResources().getColor(R.color.white));
                btnAddCarrinho.setEnabled(false);
            } else {
                item.setQuantidade(item.getQuantidade() - 1);
                qntdItem.setText(String.valueOf(item.getQuantidade()));
                item.setTotalPrecoItem(item.getTotalPrecoItem() - item.getPreco());
                valorTotalLabel.setText(RSmask(item.getTotalPrecoItem()));
            }
        });

        btnAddCarrinho.setOnClickListener(v -> {
            String label;
            int qtdItensAdd = item.getQuantidade();

                if (qtdItensAdd > 1)
                    label = " Itens foram adicionados ao carrinho.";
                else
                    label = " Item foi adicionado ao carrinho.";
                //TODO verificação de remoção
                Snackbar.make(v, qtdItensAdd + label, Snackbar.LENGTH_LONG).setActionTextColor(context.getResources().getColor(R.color.colorSecondaryLight)).setAction("Desfazer", v1 -> {
                    int oldValue = Integer.parseInt(qntdItem.getText().toString()) - qtdItensAdd;
                    if (oldValue == 0) {
                        qntdItem.setText("0");
                        item.setQuantidade(0);
                        valorTotalLabel.setText(RSmask(item.getPreco()));
                        btnAddCarrinho.setEnabled(false);
                    } else {
                        qntdItem.setText(String.valueOf(oldValue));
                        item.setQuantidade(oldValue);
                        valorTotalLabel.setText(RSmask(oldValue * item.getPreco()));
                    }
                    BadgeDrawable badgeDrawable = navBottomView.getBadge(R.id.navigation_carrinho);
                    if (badgeDrawable == null) {
                        navBottomView.getOrCreateBadge(R.id.navigation_carrinho).setNumber(item.getQuantidade());
                    } else {
                        if (badgePreviousValue == 0) {
                            navBottomView.removeBadge(R.id.navigation_carrinho);
                        } else {
                            badgeDrawable.setNumber(badgePreviousValue);
                        }
                    }
                    makeText(context, "Itens removidos do carrinho", Toast.LENGTH_SHORT).show();
                }).show();


                navBottomView = ((AppCompatActivity) this.context).findViewById(R.id.nav_view);
                BadgeDrawable badgeDrawable = navBottomView.getBadge(R.id.navigation_carrinho);
                if (badgeDrawable == null) {
                    navBottomView.getOrCreateBadge(R.id.navigation_carrinho).setNumber(item.getQuantidade());
                } else {
                    badgePreviousValue = badgeDrawable.getNumber();
                    badgeDrawable.setNumber(badgePreviousValue + item.getQuantidade());
                }
        });


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

    @Override
    public long getItemId(int position) {
        return itens.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }


    public void setItems(ArrayList<Produto> newItens) {
        if (newItens == null) {
            throw new IllegalArgumentException("Cannot set `null` item to the Recycler adapter");
        }
        this.itens.clear();
        this.itens.addAll(newItens);
        notifyDataSetChanged();
    }
}
