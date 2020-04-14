package br.com.royalfarma.holder;

import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import br.com.royalfarma.R;

public class ProdutosListaViewHolder extends RecyclerView.ViewHolder {
    public AppCompatImageButton visualizar;
    public ProgressBar progressBar;
    public AppCompatTextView tituloItemProduto, precoItemProduto;
    public AppCompatImageView imgProduto;

    public ProdutosListaViewHolder(View itemView) {
        super(itemView);
        tituloItemProduto = itemView.findViewById(R.id.title_item_produto);
        precoItemProduto = itemView.findViewById(R.id.preco_item_produto);
        imgProduto = itemView.findViewById(R.id.image_item_produto);
        visualizar = itemView.findViewById(R.id.btn_view_product);
        progressBar = itemView.findViewById(R.id.loader);
    }
}