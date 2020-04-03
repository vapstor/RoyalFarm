package br.com.royalfarma.holder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import br.com.royalfarma.R;

public class ProdutosViewHolder extends RecyclerView.ViewHolder {
    public Button qntdPlus;
    public Button qntdMinus;
    public Button addToCart;
    public TextView tituloItemProduto, qntdItem, precoItemProduto;
    public ImageView imgProduto;

    public ProdutosViewHolder(View itemView) {
        super(itemView);
        tituloItemProduto = itemView.findViewById(R.id.title_item_produto);
        qntdPlus = itemView.findViewById(R.id.btn_add_item_produto);
        qntdMinus = itemView.findViewById(R.id.btn_remove_item_produto);
        precoItemProduto = itemView.findViewById(R.id.preco_item_produto);
        qntdItem = itemView.findViewById(R.id.qntd_item_produto);
        imgProduto = itemView.findViewById(R.id.image_item_produto);
    }
}