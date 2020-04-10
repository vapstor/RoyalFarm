package br.com.royalfarma.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.adapter.ProdutosAdapter;
import br.com.royalfarma.database.DataBaseConnection;
import br.com.royalfarma.interfaces.IFetchHomeProducts;
import br.com.royalfarma.model.Produto;

public class HomeFragment extends Fragment implements IFetchHomeProducts {
    private RecyclerView recyclerMaisVendidos, recyclerNovidades, recyclerPopulares;
    private ProdutosAdapter adapterNovidades, adapterPopulares, adapterMaisVendidos;
    private HomeViewModel homeViewModel;
    private Handler handler;
    private DataBaseConnection dataBaseConnetion;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                updateUI(msg);
            }
        };

//        fetchProducts();
    }

    private void fetchProducts() {
        //resgata produtos assíncronamente
        DataBaseConnection.FetchHomeProducts fetchHomeProducts = new DataBaseConnection.FetchHomeProducts(handler, this);
        fetchHomeProducts.execute();

    }

    private void updateUI(Message msg) {
        if (msg.what == 0) {
            if ("tentando_conectar".equals(msg.obj)) {
                toggleFrameLoadingVisibility(true);
            }
        } else if (msg.what == 1) {
            switch ((String) msg.obj) {
                case "onPreExecute":
                    toggleFrameLoadingVisibility(true);
                    break;
                case "erro_sql":
                    Toast.makeText(getContext(), "Erro de SQL", Toast.LENGTH_SHORT).show();
                    toggleFrameLoadingVisibility(false);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                case "onPostExecute":
                    toggleFrameLoadingVisibility(false);
                    Toast.makeText(getActivity(), "Produtos recuperados com sucesso!", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            Toast.makeText(getContext(), "Ocorreu um erro!", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFrameLoadingVisibility(boolean frameVisibility) {
        if (getActivity() != null) {
            final ViewGroup root = getActivity().findViewById(R.id.home_scroll_vertical);
            FrameLayout frameLayout = root.findViewById(R.id.frameLoadingLayout);
            TransitionManager.beginDelayedTransition(root, new Fade());
            if (frameVisibility) {
                frameLayout.setVisibility(View.VISIBLE);
            } else {
                frameLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(requireParentFragment()).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        homeViewModel.getProdutosPopularesLiveData().observe(getViewLifecycleOwner(), produtosPopularesList -> {
            if (produtosPopularesList.size() == 0) {
                fetchProducts();
            } else {
                //Confirm that frame not block ui
                toggleFrameLoadingVisibility(false);

                adapterPopulares = new ProdutosAdapter(produtosPopularesList, getContext());
                adapterPopulares.setHasStableIds(true);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerPopulares.setLayoutManager(linearLayoutManager);
                recyclerPopulares.setAdapter(adapterPopulares);
            }
        });
        homeViewModel.getProdutosNovidadesLiveData().observe(getViewLifecycleOwner(), produtosNovidadesList -> {
            adapterNovidades = new ProdutosAdapter(produtosNovidadesList, getContext());
            adapterNovidades.setHasStableIds(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerNovidades.setLayoutManager(linearLayoutManager);
            recyclerNovidades.setAdapter(adapterNovidades);

        });

        homeViewModel.getProdutosMaisVendidosLiveData().observe(getViewLifecycleOwner(), produtosMaisVendidosList -> {
            adapterMaisVendidos = new ProdutosAdapter(produtosMaisVendidosList, getContext());
            adapterMaisVendidos.setHasStableIds(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerMaisVendidos.setLayoutManager(linearLayoutManager);
            recyclerMaisVendidos.setAdapter(adapterMaisVendidos);

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

            swipeContainer = view.findViewById(R.id.swipe_container);
            // Setup refresh listener which triggers new data loading
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            swipeContainer.setOnRefreshListener(this::fetchProducts);
            swipeContainer.setColorSchemeResources(R.color.colorAccent);

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

        //Click nas views não utilizado, apenas nos botoes e imagem
//        ItemClickSupport.addTo(recyclerNovidades).setOnItemClickListener((recyclerView, position, v) -> {
//        });
//        ItemClickSupport.addTo(recyclerPopulares).setOnItemClickListener((recyclerView, position, v) -> {
//        });
//        ItemClickSupport.addTo(recyclerMaisVendidos).setOnItemClickListener((recyclerView, position, v) -> {
//        });
    }

    @Override
    public void updateHomeWithData(ArrayList<ArrayList<Produto>> listaDeProdutos) {
        /**Para uso no SwipeRefresh*/
        if (adapterPopulares != null) {
            // Remember to CLEAR OUT old items before appending in the new ones
            adapterPopulares.clear();
            // ...the data has come back, add new items to your adapter...
            adapterPopulares.addAll(listaDeProdutos.get(0));
        }
        if (adapterNovidades != null) {
            adapterNovidades.clear();
            adapterNovidades.addAll(listaDeProdutos.get(1));
        }
        if (adapterMaisVendidos != null) {
            adapterMaisVendidos.clear();
            adapterMaisVendidos.addAll(listaDeProdutos.get(2));
        }
        /**Fim Para uso no SwipeRefresh*/

        homeViewModel.setProdutosPopulares(listaDeProdutos.get(0));
        homeViewModel.setProdutosNovidades(listaDeProdutos.get(1));
        homeViewModel.setProdutosMaisVendidos(listaDeProdutos.get(2));

        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainer.setRefreshing(false);
    }
}

