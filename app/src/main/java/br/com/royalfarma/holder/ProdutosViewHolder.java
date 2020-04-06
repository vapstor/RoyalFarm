package br.com.royalfarma.holder;

import android.view.View;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import br.com.royalfarma.R;

public class ProdutosViewHolder extends RecyclerView.ViewHolder {
    public AppCompatImageButton qntdPlus, qntdMinus;
    public AppCompatButton addToCart;
    public AppCompatTextView tituloItemProduto, qntdItem, precoItemProduto;
    public AppCompatImageView imgProduto;

    public ProdutosViewHolder(View itemView) {
        super(itemView);
        tituloItemProduto = itemView.findViewById(R.id.title_item_produto);
        qntdPlus = itemView.findViewById(R.id.btn_add_item_produto);
        qntdMinus = itemView.findViewById(R.id.btn_remove_item_produto);
        precoItemProduto = itemView.findViewById(R.id.preco_item_produto);
        qntdItem = itemView.findViewById(R.id.qntd_item_produto);
        imgProduto = itemView.findViewById(R.id.image_item_produto);
        addToCart = itemView.findViewById(R.id.btn_add_produto_carrinho);
    }
}