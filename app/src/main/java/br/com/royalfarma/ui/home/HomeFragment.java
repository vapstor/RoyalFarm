package br.com.royalfarma.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.adapter.ProdutosAdapter;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.utils.ItemClickSupport;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private ArrayList<Produto> produtosNovidades;
    private RecyclerView recyclerNovidades;
    private Produto selectedProduto;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
//        final TextView textView = root.findViewById(R.id.text_dashboard);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });

//        (int id, String titulo, int quantidade, double valor, long codBarra) {
//
//        }
        produtosNovidades = new ArrayList<Produto>();
        Produto produto = new Produto(0, "Vacina Corona 1", 0, 10.25, 123456);
        produtosNovidades.add(produto);
        produto = new Produto(1, "Vacina Corona 2", 0, 10.25, 123456);
        produtosNovidades.add(produto);
        produto = new Produto(2, "Vacina Corona 3", 0, 10.25, 123456);
        produtosNovidades.add(produto);
        produto = new Produto(3, "Vacina Corona 4", 0, 10.25, 123456);
        produtosNovidades.add(produto);
        produto = new Produto(4, "Vacina Corona 5", 0, 10.25, 123456);
        produtosNovidades.add(produto);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerNovidades = view.findViewById(R.id.recyclerProdutosNovidades);

        ProdutosAdapter adapter = new ProdutosAdapter(produtosNovidades, getContext());
        adapter.setHasStableIds(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerNovidades.setLayoutManager(linearLayoutManager);

        recyclerNovidades.setAdapter(adapter);
        ItemClickSupport.addTo(recyclerNovidades).setOnItemClickListener((recyclerView, position, v) -> {
            selectedProduto = produtosNovidades.get(position);
            Toast.makeText(HomeFragment.this.getContext(), "Item Clicado: " + selectedProduto.id, Toast.LENGTH_SHORT).show();
        });
    }
}
