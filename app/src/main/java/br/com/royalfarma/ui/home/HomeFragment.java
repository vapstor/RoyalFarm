package br.com.royalfarma.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.adapter.ProdutosHomeAdapter;
import br.com.royalfarma.database.DataBaseConnection;
import br.com.royalfarma.interfaces.IFetchProducts;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;

public class HomeFragment extends Fragment implements IFetchProducts {
    private RecyclerView recyclerMaisVendidos, recyclerNovidades, recyclerPopulares;
    private ProdutosHomeAdapter adapterNovidades, adapterPopulares, adapterMaisVendidos;
    private ProductsViewModel productsViewModel;
    private Handler handler;
    private SwipeRefreshLayout swipeContainer;
    private NavController navController;
    private CountDownTimer countdown;
    private LinearLayoutManager linearLayoutManager;
    private CarrinhoViewModel carrinhoViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                updateUI(msg);
            }
        };

        countdown = new CountDownTimer(5000, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Toast.makeText(getContext(), "Por favor, aguarde...", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void fetchProducts() {
        //resgata produtos assíncronamente
        new Thread(() -> {
            DataBaseConnection.FetchProducts fetchProducts = new DataBaseConnection.FetchProducts(handler, true, 15, this);
            fetchProducts.execute();
            //Inicia counter para avisar sobre carregamento lento
            countdown.start();
        }).start();
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
//                    Toast.makeText(getActivity(), "Produtos recuperados com sucesso!", Toast.LENGTH_SHORT).show();
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
            TransitionManager.beginDelayedTransition(root, new Fade().setDuration(750));
            if (frameVisibility) {
                frameLayout.setVisibility(View.VISIBLE);
                frameLayout.setClickable(true);
                frameLayout.setOnClickListener(v -> Toast.makeText(getContext(), "Aguarde", Toast.LENGTH_SHORT).show());
            } else {
                frameLayout.setVisibility(View.INVISIBLE);
            }
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        productsViewModel = new ViewModelProvider(requireParentFragment()).get(ProductsViewModel.class);
        carrinhoViewModel = new ViewModelProvider(requireParentFragment()).get(CarrinhoViewModel.class);
        productsViewModel.getProductLiveData().observe(getViewLifecycleOwner(), position -> {
            if (position < 5) {
                if (adapterNovidades != null) {
                    adapterNovidades.notifyItemChanged(position);
                }
            } else if (position > 5 && position <= 10) {
                if (adapterPopulares != null) {
                    adapterPopulares.notifyItemChanged(position);
                }
            } else {
                if (adapterMaisVendidos != null) {
                    adapterMaisVendidos.notifyItemChanged(position);
                }
            }
        });
        productsViewModel.getTodosOsProdutosLiveData().observe(getViewLifecycleOwner(), listaDeTodosOsProdutos -> {
            if (listaDeTodosOsProdutos.size() == 0) {
                fetchProducts();
            } else {
                //Confirm that frame don't block ui
                toggleFrameLoadingVisibility(false);

                linearLayoutManager = new LinearLayoutManager(getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                ArrayList<Produto> novidades = new ArrayList<>(listaDeTodosOsProdutos.subList(0, 5));
                adapterNovidades = new ProdutosHomeAdapter(novidades, getContext(), carrinhoViewModel, productsViewModel);
                adapterNovidades.setHasStableIds(true);
                recyclerNovidades.setLayoutManager(linearLayoutManager);
                recyclerNovidades.setAdapter(adapterNovidades);

                linearLayoutManager = new LinearLayoutManager(getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                ArrayList<Produto> populares = new ArrayList<>(listaDeTodosOsProdutos.subList(5, 10));
                adapterPopulares = new ProdutosHomeAdapter(populares, getContext(), carrinhoViewModel, productsViewModel);
                adapterPopulares.setHasStableIds(true);
                recyclerPopulares.setLayoutManager(linearLayoutManager);
                recyclerPopulares.setAdapter(adapterPopulares);

                linearLayoutManager = new LinearLayoutManager(getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                ArrayList<Produto> mais_vendidos = new ArrayList<>(listaDeTodosOsProdutos.subList(10, 15));
                adapterMaisVendidos = new ProdutosHomeAdapter(mais_vendidos, getContext(), carrinhoViewModel, productsViewModel);
                adapterMaisVendidos.setHasStableIds(true);
                recyclerMaisVendidos.setLayoutManager(linearLayoutManager);
                recyclerMaisVendidos.setAdapter(adapterMaisVendidos);
            }
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
                if (getActivity() != null) {
                    navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    Bundle extras = new Bundle();
                    extras.putString("title", "Novidades");
                    navController.navigate(R.id.action_navigation_home_to_navigation_lista_produtos, extras);
                }
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
    public void onGetDataDone(ArrayList<Produto> todosOsProdutos) {
        //Cancela countdown pois os dados chegaram
        if (countdown != null) {
            countdown.cancel();
        }

        /**Para uso no SwipeRefresh*/
        if (adapterNovidades != null) {
            //Remember to CLEAR OUT old items before appending in the new ones
            adapterNovidades.clear();
            //...the data has come back, add new items to your adapter...
            adapterNovidades.addAll(new ArrayList<>(todosOsProdutos.subList(0, 5)));
        }
        if (adapterPopulares != null) {
            adapterPopulares.clear();
            adapterPopulares.addAll(new ArrayList<>(todosOsProdutos.subList(5, 10)));
        }
        if (adapterMaisVendidos != null) {
            adapterMaisVendidos.clear();
            adapterMaisVendidos.addAll(new ArrayList<>(todosOsProdutos.subList(10, 15)));
        }
        /**Fim Para uso no SwipeRefresh*/

        productsViewModel.setTodosOsProdutos(todosOsProdutos);
        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainer.setRefreshing(false);
    }
}

