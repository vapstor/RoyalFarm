package br.com.royalfarma.ui.carrinho;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.adapter.ProdutosCarrinhoAdapter;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.utils.ItemClickSupport;
import br.com.royalfarma.utils.RecyclerItemTouchHelper;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;

public class CarrinhoFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private CarrinhoViewModel carrinhoViewModel;
    private RecyclerView recyclerCart;
    private ProdutosCarrinhoAdapter adapter;
    private BottomNavigationView navView;
    private AppCompatTextView subtotal;
    private ArrayList<Produto> cartProducts;
    private BadgeDrawable badgeDrawable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navView = ((AppCompatActivity) getContext()).findViewById(R.id.nav_view);
        if (navView != null) {
            badgeDrawable = navView.getBadge(R.id.navigation_carrinho);
            navView.setVisibility(View.GONE);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (navView != null) {
            navView.setVisibility(View.GONE);
        }

        carrinhoViewModel = new ViewModelProvider(requireParentFragment()).get(CarrinhoViewModel.class);
        View root = inflater.inflate(R.layout.fragment_carrinho, container, false);
        carrinhoViewModel.getSubtotalLiveData().observe(getViewLifecycleOwner(), s -> subtotal.setText(s));
        carrinhoViewModel.getProdutoLiveData().observe(getViewLifecycleOwner(), position -> {
            if (adapter != null) {
                recyclerCart.post(() -> adapter.notifyItemChanged(position));
            }
        });
        carrinhoViewModel.getCartProductsLiveData().observe(getViewLifecycleOwner(), produtos -> {
            if (produtos.size() == 0) {
                Toast.makeText(getContext(), "Carrinho vazio!", Toast.LENGTH_SHORT).show();
            } else {
                if (cartProducts != null && adapter != null) {
                    Produto p;
                    for (int i = 0; i < cartProducts.size(); i++) {
                        p = cartProducts.get(i);
                        if (p.getQtdNoCarrinho() == 0) {
                            produtos.remove(i);
                            adapter.notifyItemRemoved(i);
                            carrinhoViewModel.updateProductsList(produtos);
                        }
                    }
                } else {
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    adapter = new ProdutosCarrinhoAdapter(produtos, getContext(), carrinhoViewModel);
                    adapter.setHasStableIds(true);
                    recyclerCart.setLayoutManager(linearLayoutManager);
                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerCart.getContext(), linearLayoutManager.getOrientation());
                    recyclerCart.addItemDecoration(dividerItemDecoration);
                    recyclerCart.setAdapter(adapter);
                }
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (navView != null) {
            navView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        if (navView != null) {
            navView.setVisibility(View.GONE);
        }
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerCart = getActivity().findViewById(R.id.recyclerCarrinho);
        subtotal = getActivity().findViewById(R.id.subtotal);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerCart);

        ItemClickSupport.addTo(recyclerCart).setOnItemClickListener((recyclerView, position, v) -> {

        });
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        Log.d(MY_LOG_TAG, "viewHolder.getAdapterPosition() : " + viewHolder.getAdapterPosition());
        Log.d(MY_LOG_TAG, "POSITION: " + position);
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.excluir)
                .setMessage(R.string.deseja_realmente_excluir_este_item_do_carrinho_)
                .setNegativeButton(R.string.cancelar, (dialog, which) -> {
                    adapter.notifyItemChanged(position);
                })
                .setPositiveButton(R.string.excluir, (dialog, which) -> {
                    cartProducts = carrinhoViewModel.getCartProductsLiveData().getValue();
                    if (cartProducts != null) {
                        cartProducts.get(position).setQtdNoCarrinho(0);
                        carrinhoViewModel.updateProductOnCartList(cartProducts.get(position));
                        adapter.notifyItemRemoved(position);
                        if (badgeDrawable != null) {
                            int previousNumber = badgeDrawable.getNumber();
                            if (carrinhoViewModel.getCartProductsLiveData().getValue().size() == 0) {
                                badgeDrawable.clearNumber();
                                badgeDrawable.setVisible(false);
                            } else {
//                                badgeDrawable.setNumber(previousNumber - carrinhoViewModel.getCartProductsLiveData().getValue().get(position).getQuantidade());
                            }
                        }
                    }
                }).setOnDismissListener(dialog -> {
            adapter.notifyItemChanged(position);
        }).show();
    }
}
