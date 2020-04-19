package br.com.royalfarma.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.activitys.ProductDetailImageActivity;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;
import br.com.royalfarma.utils.RoundedBackgroundSpan;

import static androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation;
import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.RSmask;

public class ProductDetailFragment extends Fragment {
    private AppCompatImageView imagem;
    private Produto produto;
    private AppCompatTextView tvTitulo, tvPreco, tvQuantidadeEstoque, tvDescricao;
    private boolean fromCart;
    private FloatingActionButton fabCart;
    private CarrinhoViewModel carrinhoViewModel;

    private ProductDetailViewModel mViewModel;
    private ActionBar actionBar;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private BottomNavigationView navView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getArguments();
        if (extras != null) {
            produto = extras.getParcelable("selectedProduct");
        }
    }

    @Override
    public void onDestroy() {
        if (!((AppCompatActivity) getActivity()).getSupportActionBar().isShowing())
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (((AppCompatActivity) getActivity()).getSupportActionBar().isShowing())
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        super.onResume();
    }

    public static ProductDetailFragment newInstance() {
        return new ProductDetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_product_detail, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (((AppCompatActivity) getActivity()).getSupportActionBar().isShowing())
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        navView = getActivity().findViewById(R.id.nav_view);

        super.onActivityCreated(savedInstanceState);
//        Product = new ViewModelProvider(requireParentFragment()).get(ProductDetailViewModel.class);
    }

    @Override
    public void onDestroyView() {
        if (!((AppCompatActivity) getActivity()).getSupportActionBar().isShowing())
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        super.onDestroyView();
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
            ssb.setSpan(new RoundedBackgroundSpan(getContext()), ssb.length() - precoNormal.length() - 2, ssb.length() - precoNormal.length() + precoOferta.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvPreco.setText(ssb, TextView.BufferType.EDITABLE);
        } else {
            tvPreco.setText(precoNormal);
        }
        tvQuantidadeEstoque.setText("Estoque: " + produto.getEstoqueAtual());
        tvDescricao.setText(R.string.este_produto_nao_contem_descricao);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (fromCart) {
            fabCart.setVisibility(View.GONE);
        } else {
            fabCart.setVisibility(View.VISIBLE);
        }
        setTextViews();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle extras = getArguments();
        fromCart = extras.getBoolean("fromCart");
        carrinhoViewModel = new ViewModelProvider(this).get(CarrinhoViewModel.class);

        imagem = view.findViewById(R.id.header);

        String url = "https://www.royalfarma.com.br/uploads/" + produto.getImagemURL();

        if (produto.getImagemURL() == null || produto.getImagemURL().equals("")) {
            url = "drawable/empty_product_image";
            int productImageId = this.getResources().getIdentifier(url, "drawable", getContext().getPackageName());
            Picasso.get().load(productImageId).into(imagem);
        } else {
            Log.d(MY_LOG_TAG, "URL: " + url);
            Log.d(MY_LOG_TAG, "Product Image URL DB: " + produto.getImagemURL());
            Picasso.get().load(url).into(imagem);
        }

        String finalUrl = url;
        imagem.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProductDetailImageActivity.class);
            Bundle bundle = makeSceneTransitionAnimation(getActivity()).toBundle();
            intent.putExtra("imageURL", finalUrl);
            startActivity(intent, bundle);
        });

        collapsingToolbarLayout = view.findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitle(produto.getNome());

        fabCart = getActivity().findViewById(R.id.fab);
        fabCart.setOnClickListener(fabView -> {
            Produto produtoNoCarrinho = null;
            ArrayList<Produto> produtosCarrinho = carrinhoViewModel.getCartProductsLiveData().getValue();
            if (produtosCarrinho != null) {
                for (int i = 0; i < produtosCarrinho.size(); i++) {
                    produtoNoCarrinho = produtosCarrinho.get(i);
                }

                final AlertDialog.Builder d = new AlertDialog.Builder(getContext(), R.style.MaterialAlertDialog_Rounded);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.layout_dialog_item_quantity, null);
                d.setTitle("Selecione uma quantidade");
                d.setView(dialogView);
                final NumberPicker numberPicker = dialogView.findViewById(R.id.itemQtdNumberPicker);

                if (produtoNoCarrinho != null) {

                    numberPicker.setMaxValue(produto.getEstoqueAtual() - produtoNoCarrinho.getQtdNoCarrinho());
                    numberPicker.setMinValue(1);
                    numberPicker.setWrapSelectorWheel(false);
//                        numberPicker.setOnValueChangedListener((numberPicker1, i, i1) -> {
//                        });
                    Produto finalProdutoNoCarrinho = produtoNoCarrinho;
                    d.setPositiveButton("Selecionar", (dialogInterface, i) -> {
                        finalProdutoNoCarrinho.setQtdNoCarrinho(numberPicker.getValue());
                        carrinhoViewModel.updateProductOnCartList(finalProdutoNoCarrinho);
                        if (finalProdutoNoCarrinho.getQtdNoCarrinho() < numberPicker.getValue()) {
                            carrinhoViewModel.updateBadgeDisplay();
                            int finalQntdItensAdd = numberPicker.getValue();
                            Snackbar.make(view, "Quantidade atualizada com sucesso", Snackbar.LENGTH_SHORT).setActionTextColor(this.getResources().getColor(R.color.colorSecondaryLight)).setAction("Desfazer", v1 -> {
                                //Remove o último adicionado
                                produto.setQtdNoCarrinho(produto.getQtdNoCarrinho() - finalQntdItensAdd);
                                carrinhoViewModel.updateProductOnCartList(produto);
                                Integer badgeValue = carrinhoViewModel.getBadgeDisplayLiveData().getValue();
                                if (badgeValue != null) {
                                    carrinhoViewModel.updateBadgeDisplay();
                                }
                                Toast.makeText(getContext(), "Quantidade atualizada com sucesso", Toast.LENGTH_SHORT).show();
                            }).show();
                        } else if (finalProdutoNoCarrinho.getQtdNoCarrinho() > numberPicker.getValue()) {
                            carrinhoViewModel.updateBadgeDisplay();
                            int finalQntdItensAdd = numberPicker.getValue();
                            Snackbar.make(view, "Quantidade atualizada com sucesso", Snackbar.LENGTH_SHORT).setActionTextColor(this.getResources().getColor(R.color.colorSecondaryLight)).setAction("Desfazer", v1 -> {
                                //Remove o último adicionado
                                produto.setQtdNoCarrinho(produto.getQtdNoCarrinho() - finalQntdItensAdd);
                                carrinhoViewModel.updateProductOnCartList(produto);
                                Integer badgeValue = carrinhoViewModel.getBadgeDisplayLiveData().getValue();
                                if (badgeValue != null) {
                                    carrinhoViewModel.updateBadgeDisplay();
                                }
                                Toast.makeText(getContext(), "Quantidade atualizada com sucesso", Toast.LENGTH_SHORT).show();
                            }).show();
                        }
                    });
                    d.setNegativeButton("Cancelar", (dialogInterface, i) -> {
                    });
                    AlertDialog alertDialog = d.create();
                    alertDialog.show();
                } else {
                    numberPicker.setMaxValue(produto.getEstoqueAtual());
                    numberPicker.setMinValue(1);
                    numberPicker.setWrapSelectorWheel(false);
//                        numberPicker.setOnValueChangedListener((numberPicker1, i, i1) -> {
//                        });
                    d.setPositiveButton("Selecionar", (dialogInterface, i) -> {
                        produto.setQtdNoCarrinho(numberPicker.getValue());
                        carrinhoViewModel.updateProductOnCartList(produto);
                        final Integer[] badgeValue = {carrinhoViewModel.getBadgeDisplayLiveData().getValue()};
                        if (badgeValue[0] != null) {
                            carrinhoViewModel.updateBadgeDisplay();
                        }
                        String label, labelExclusao;
                        if (numberPicker.getValue() > 1) {
                            label = " Itens foram adicionados ao carrinho.";
                            labelExclusao = " Itens removidos do carrinho.";
                        } else {
                            label = " Item foi adicionado ao carrinho.";
                            labelExclusao = "Itens removidos do carrinho.";
                        }
                        int finaQtdItensAdd = numberPicker.getValue();
                        Snackbar.make(view, numberPicker.getValue() + label, Snackbar.LENGTH_SHORT).setActionTextColor(this.getResources().getColor(R.color.colorSecondaryLight)).setAction("Desfazer", v1 -> {
                            //Remove o último adicionado
                            produto.setQtdNoCarrinho(produto.getQtdNoCarrinho() - finaQtdItensAdd);
                            carrinhoViewModel.updateProductOnCartList(produto);
                            badgeValue[0] = carrinhoViewModel.getBadgeDisplayLiveData().getValue();
                            if (badgeValue[0] != null) {
                                carrinhoViewModel.updateBadgeDisplay();
                            }
                            Toast.makeText(getContext(), finaQtdItensAdd + labelExclusao, Toast.LENGTH_SHORT).show();
                        }).show();
                    });
                    d.setNegativeButton("Cancelar", (dialogInterface, i) -> {
                    });
                    AlertDialog alertDialog = d.create();
                    alertDialog.show();

                }
            }
        });
        tvTitulo = view.findViewById(R.id.tvTitulo);
        tvPreco = view.findViewById(R.id.tvPreco);
        tvQuantidadeEstoque = view.findViewById(R.id.tvQuantidadeEstoque);
        tvDescricao = view.findViewById(R.id.tvDescricao);

    }
}
