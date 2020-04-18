package br.com.royalfarma.ui.carrinho;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
import br.com.royalfarma.model.Usuario;
import br.com.royalfarma.ui.login.LoginViewModel;
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
    private AppCompatButton finalizarCompraButton;
    private LoginViewModel loginViewModel;
    private Usuario usuario;
    private NavController navController;

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
        loginViewModel = new ViewModelProvider(requireParentFragment()).get(LoginViewModel.class);
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
        loginViewModel.getUsuarioMutableLiveData().observe(getViewLifecycleOwner(), usuario -> {
            this.usuario = usuario;
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

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);

        finalizarCompraButton = getActivity().findViewById(R.id.finalizarCompraButton);
        finalizarCompraButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                ArrayList<Produto> produtosCarrinho = carrinhoViewModel.getCartProductsLiveData().getValue();
                if (produtosCarrinho != null && produtosCarrinho.size() > 0) {
                    String subtotal = carrinhoViewModel.getSubtotalLiveData().getValue();
                    Bundle extras = new Bundle();
                    extras.putString("subtotal", subtotal);
                    navController.navigate(R.id.action_navigation_carrinho_to_loginFragment, extras);
                } else {
                    Toast.makeText(getContext(), "Adicione itens ao carrinho para prosseguir!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerCart);

        ItemClickSupport.addTo(recyclerCart).setOnItemClickListener((recyclerView, position, v) -> {
            final AlertDialog.Builder d = new AlertDialog.Builder(getContext(), R.style.MaterialAlertDialog_Rounded);
            LayoutInflater inflater = ((AppCompatActivity) getContext()).getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.layout_dialog_item_quantity, null);
            d.setTitle("Selecione uma quantidade");
            d.setView(dialogView);
            final NumberPicker numberPicker = dialogView.findViewById(R.id.itemQtdNumberPicker);
            numberPicker.setMaxValue(carrinhoViewModel.getCartProductsLiveData().getValue().get(position).getEstoqueAtual());
            numberPicker.setMinValue(1);
            numberPicker.setWrapSelectorWheel(false);
            numberPicker.setOnValueChangedListener((numberPicker1, i, i1) -> {
            });
            d.setPositiveButton("Selecionar", (dialogInterface, i) -> {
                Produto p = carrinhoViewModel.getCartProductsLiveData().getValue().get(position);
                if (numberPicker.getValue() > p.getQtdNoCarrinho()) {
                    //aumentando
                    int qntdAnterior = p.getQtdNoCarrinho();
                    p.setQtdNoCarrinho(numberPicker.getValue());
                    carrinhoViewModel.updateProductOnCartList(p);
                    carrinhoViewModel.updateBadgeDisplay();
                } else if (numberPicker.getValue() < p.getQtdNoCarrinho()) {
                    //diminuindo
                    p.setQtdNoCarrinho(numberPicker.getValue());
                    carrinhoViewModel.updateProductOnCartList(p);
                    carrinhoViewModel.updateBadgeDisplay();
                }

            });
            d.setNegativeButton("Cancelar", (dialogInterface, i) -> {
            });
            AlertDialog alertDialog = d.create();
            alertDialog.show();
        });
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        Log.d(MY_LOG_TAG, "viewHolder.getAdapterPosition() : " + viewHolder.getAdapterPosition());
        Log.d(MY_LOG_TAG, "POSITION: " + position);
        new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Rounded)
                .setTitle(R.string.excluir)
                .setMessage(R.string.deseja_realmente_excluir_este_item_do_carrinho_)
                .setNegativeButton(R.string.cancelar, (dialog, which) -> {
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemInserted(position);
                })
                .setPositiveButton(R.string.excluir, (dialog, which) -> {
                    cartProducts = carrinhoViewModel.getCartProductsLiveData().getValue();
                    if (cartProducts != null) {
                        if (badgeDrawable != null) {
                            carrinhoViewModel.updateBadgeDisplay();
                        }
                        cartProducts.get(position).setQtdNoCarrinho(0);

                        //não existe navview no carrinho
                        carrinhoViewModel.updateProductOnCartList(cartProducts.get(position));
                        adapter.notifyItemRemoved(position);

                    }
                }).setOnDismissListener(dialog -> adapter.notifyItemChanged(position)).show();
    }
}
