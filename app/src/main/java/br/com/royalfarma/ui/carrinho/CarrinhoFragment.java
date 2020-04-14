package br.com.royalfarma.ui.carrinho;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import br.com.royalfarma.R;
import br.com.royalfarma.adapter.ProdutosCarrinhoAdapter;

public class CarrinhoFragment extends Fragment {

    private CarrinhoViewModel carrinhoViewModel;
    private RecyclerView recyclerCart;
    private ProdutosCarrinhoAdapter adapter;
    private BottomNavigationView navView;
    private AppCompatTextView subtotal;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navView = getActivity().findViewById(R.id.nav_view);
        navView.setVisibility(View.GONE);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        carrinhoViewModel = new ViewModelProvider(requireParentFragment()).get(CarrinhoViewModel.class);
        View root = inflater.inflate(R.layout.fragment_carrinho, container, false);
        carrinhoViewModel.getSubtotalLiveData().observe(getViewLifecycleOwner(), s -> {
            subtotal.setText(s);
        });
        carrinhoViewModel.getProdutoLiveData().observe(getViewLifecycleOwner(), position -> {
            if (adapter != null) {
                adapter.notifyItemChanged(position);
            }
        });
        carrinhoViewModel.getCartProductsLiveData().observe(getViewLifecycleOwner(), produtos -> {
            if (produtos.size() == 0) {
                Toast.makeText(getContext(), "Carrinho vazio!", Toast.LENGTH_SHORT).show();
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerCart = getActivity().findViewById(R.id.recyclerCarrinho);
        subtotal = getActivity().findViewById(R.id.subtotal);
    }
}
