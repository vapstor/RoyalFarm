package br.com.royalfarma.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.adapter.ProdutosAdapter;
import br.com.royalfarma.model.Produto;

public class HomeFragment extends Fragment {

    private ArrayList<Produto> produtosNovidades, produtosPopulares, produtosMaisVendidos;
    private RecyclerView recyclerMaisVendidos, recyclerNovidades, recyclerPopulares;
    private Parcelable mListState;
    private ArrayList<Parcelable> mDataset;
    private ProdutosAdapter adapterNovidades, adapterPopulares, adapterMaisVendidos;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //        (int id, String titulo, int quantidade, double valor, long codBarra)
        produtosNovidades = new ArrayList<>();
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

        produtosPopulares = new ArrayList<>();
        Produto produtoPopular = new Produto(0, "Vacina Corona 5", 0, 10.25, 123456);
        produtosPopulares.add(produtoPopular);

        produtoPopular = new Produto(1, "Vacina Corona 4", 0, 10.25, 123456);
        produtosPopulares.add(produtoPopular);

        produtoPopular = new Produto(2, "Vacina Corona 3", 0, 10.25, 123456);
        produtosPopulares.add(produtoPopular);

        produtoPopular = new Produto(3, "Vacina Corona 2", 0, 10.25, 123456);
        produtosPopulares.add(produtoPopular);

        produtoPopular = new Produto(4, "Vacina Corona 1", 0, 10.25, 123456);
        produtosPopulares.add(produtoPopular);


        produtosMaisVendidos = new ArrayList<>();
        Produto produtoMaisVendido = new Produto(0, "Vacina Corona 5", 0, 10.25, 123456);
        produtosMaisVendidos.add(produtoMaisVendido);

        produtoMaisVendido = new Produto(1, "Vacina Corona 4", 0, 10.25, 123456);
        produtosMaisVendidos.add(produtoMaisVendido);

        produtoMaisVendido = new Produto(2, "Vacina Corona 3", 0, 10.25, 123456);
        produtosMaisVendidos.add(produtoMaisVendido);

        produtoMaisVendido = new Produto(3, "Vacina Corona 2", 0, 10.25, 123456);
        produtosMaisVendidos.add(produtoMaisVendido);

        produtoMaisVendido = new Produto(4, "Vacina Corona 1", 0, 10.25, 123456);
        produtosMaisVendidos.add(produtoMaisVendido);

        adapterNovidades = new ProdutosAdapter(produtosNovidades, getContext());
        adapterPopulares = new ProdutosAdapter(produtosPopulares, getContext());
        adapterMaisVendidos = new ProdutosAdapter(produtosMaisVendidos, getContext());
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        homeViewModel.getText().observe(getViewLifecycleOwner(), s -> {
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getContext();
        if (context != null) {
            recyclerNovidades = view.findViewById(R.id.recyclerNovidades);
            recyclerPopulares = view.findViewById(R.id.recyclerPopulares);
            recyclerMaisVendidos = view.findViewById(R.id.recyclerMaisVendidos);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerNovidades.setLayoutManager(linearLayoutManager);

            linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerPopulares.setLayoutManager(linearLayoutManager);

            linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerMaisVendidos.setLayoutManager(linearLayoutManager);

            adapterNovidades.setHasStableIds(true);
            adapterPopulares.setHasStableIds(true);
            adapterMaisVendidos.setHasStableIds(true);

            recyclerNovidades.setAdapter(adapterNovidades);
            recyclerPopulares.setAdapter(adapterPopulares);
            recyclerMaisVendidos.setAdapter(adapterMaisVendidos);


            ((AppCompatActivity) context).findViewById(R.id.btnVerMaisNovidades).setOnClickListener(v -> {
                Toast.makeText(context, "Em implementação agora", Toast.LENGTH_SHORT).show();
            });

            ((AppCompatActivity) context).findViewById(R.id.btnVerMaisPopulares).setOnClickListener(v -> {
                Toast.makeText(context, "Em implementação agora", Toast.LENGTH_SHORT).show();
            });

            ((AppCompatActivity) context).findViewById(R.id.btnVerMaisMaisVendidos).setOnClickListener(v -> {
                Toast.makeText(context, "Em implementação agora", Toast.LENGTH_SHORT).show();
            });
        }

        //Click nos adaptadores não usado, apenas nos botoes e imagem
//        ItemClickSupport.addTo(recyclerNovidades).setOnItemClickListener((recyclerView, position, v) -> {
//        });
//        ItemClickSupport.addTo(recyclerPopulares).setOnItemClickListener((recyclerView, position, v) -> {
//        });
//        ItemClickSupport.addTo(recyclerMaisVendidos).setOnItemClickListener((recyclerView, position, v) -> {
//        });
    }
}

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        if (recyclerNovidades.getLayoutManager() != null) {
//            Parcelable novidadesListState = recyclerNovidades.getLayoutManager().onSaveInstanceState();
//            // putting recyclerview position
//            outState.putParcelable(SAVED_RECYCLER_VIEW_STATUS_ID, novidadesListState);
//            // putting recyclerview items
//            outState.putParcelableArrayList(SAVED_RECYCLER_VIEW_DATASET_ID, mDataset);
//        }
//        super.onSaveInstanceState(outState);
//    }
//
//
//    public void restorePreviousState(Bundle savedInstanceState) {
//        // getting recyclerview position
//        mListState = savedInstanceState.getParcelable(SAVED_RECYCLER_VIEW_STATUS_ID);
//        // getting recyclerview items
//        mDataset = savedInstanceState.getParcelableArrayList(SAVED_RECYCLER_VIEW_DATASET_ID);
//        // Restoring adapter items
//        mAdapter.setItems(mDataset);
//        // Restoring recycler view position
//        mRvMedia.getLayoutManager().