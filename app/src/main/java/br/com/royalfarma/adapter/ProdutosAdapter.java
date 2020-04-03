package br.com.royalfarma.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.holder.ProdutosViewHolder;
import br.com.royalfarma.model.Produto;

import static android.widget.Toast.makeText;
import static br.com.royalfarma.utils.Util.MY_LOG_TAG;

public class ProdutosAdapter extends RecyclerView.Adapter {

    private final Toast toast;
    private Context context;
    private ArrayList<Produto> itens;

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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TextView qntdItem, descricaoItem, valorTotalLabel;
        Button qntdMinus, qntdPlus;
        StringBuilder builder = new StringBuilder();

        ProdutosViewHolder itemProdutoHolder = (ProdutosViewHolder) holder;
        Produto item = itens.get(position);

        descricaoItem = itemProdutoHolder.tituloItemProduto;
//        builder.append(item.getNome()).append("\n").append(RSmask(item.getPreco()));
//        descricaoItem.setText(builder.toString());
        descricaoItem.setText(item.getNome());

//        valorTotalLabel = itemProdutoHolder.precoItemProduto;
//        valorTotalLabel.setText("Valor Total: " + RSmask(ListaProdutosLoja.valorTotal));

        qntdItem = itemProdutoHolder.qntdItem;
        qntdItem.setText(String.valueOf(item.getQuantidade()));

        qntdMinus = itemProdutoHolder.qntdMinus;
        qntdPlus = itemProdutoHolder.qntdPlus;

        if (item.getQuantidade() > 0) {
            qntdItem.setTextColor(Color.parseColor("#FFFF0000"));
        }

        ImageView imagemItem = itemProdutoHolder.imgProduto;
        String url = "drawable/produto_" + item.getCodBarra();
        int productImageId = this.context.getResources().getIdentifier(
                url,
                "drawable",
                context.getPackageName()
        );

        if (productImageId == 0) {
            url = "drawable/android_02";
            productImageId = this.context.getResources().getIdentifier(url, "drawable", context.getPackageName()
            );
        }

        Log.d(MY_LOG_TAG, "URL: " + url);
        Log.d(MY_LOG_TAG, "Product Image Id: " + url);

        Picasso.get().load(productImageId).into(imagemItem);
//
//        if (item.getQuantidade() == 0) {
//            qntdItem.setTextColor(context.getResources().getColor(R.color.white));
//        }

//        //Aumenta quantidade do item
//        qntdPlus.setOnClickListener(v -> {
//            //Inicia-se no 1 por isso < e não <=
//            if (item.getQuantidade() < item.getEstoqueAtual()) {
//                item.setQuantidade(item.getQuantidade() + 1);
//                ListaProdutosLoja.valorTotal = ListaProdutosLoja.valorTotal + item.getPreco();
//                qntdItem.setText("" + item.getQuantidade());
//                if (item.getQuantidade() > 0) {
//                    qntdItem.setTextColor(context.getResources().getColor(R.color.colorPrimary));
//                }
//                valorTotalLabel.setText("Valor Total: " + RSmask(ListaProdutosLoja.valorTotal));
//            } else {
//                try {
//                    try {
//                        beepObject().startTone(toneType, toneDuration);
//                    } catch (RuntimeException e) {
//                        e.printStackTrace();
//                        makeText(context, "Erro no som!", Toast.LENGTH_SHORT).show();
//                    }
//                    makeText(context, "Não há mais quantidades deste produto no estoque!", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        //Diminui quantidade do item
//        qntdMinus.setOnClickListener(v -> {
//            if (possivelDiminuir(item)) {
//                item.setQuantidade(item.getQuantidade() - 1);
//                qntdItem.setText("" + item.getQuantidade());
//                if (item.getQuantidade() == 0) {
//                    qntdItem.setTextColor(context.getResources().getColor(R.color.white));
//                }
//                ListaProdutosLoja.valorTotal = ListaProdutosLoja.valorTotal - item.getPreco();
//                valorTotalLabel.setText("Valor Total: " + RSmask(ListaProdutosLoja.valorTotal));
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }


//
//
//    private Context ctx;
//    private int[] lista;
//
//    public ProdutosNovidadesAdapter(Context ctx, int[] lista){
//        this.ctx = ctx;
//        this.lista = lista;
//    }
//
//    @Override
//    public int getCount() {
//        // TODO Auto-generated method stub
//        return lista.length;
//    }
//
//    @Override
//    public Object getItem(int position) {
//        // TODO Auto-generated method stub
//        return lista[position];
//    }
//
//    @Override
//    public long getItemId(int position) {
//        // TODO Auto-generated method stub
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        // TODO Auto-generated method stub
//
//        ImageView iv = new ImageView(ctx);
//        iv.setImageResource(lista[position]);
//        iv.setAdjustViewBounds(true);
//
//        return iv;
//    }
}
