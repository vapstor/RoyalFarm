package br.com.royalfarma.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.activitys.MainActivity;
import br.com.royalfarma.activitys.ProductDetail;
import br.com.royalfarma.adapter.ProdutosHomeAdapter;
import br.com.royalfarma.database.DataBaseConnection;
import br.com.royalfarma.interfaces.IDetailViewClick;
import br.com.royalfarma.interfaces.IFetchProducts;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;
import br.com.royalfarma.ui.pesquisar.PesquisarViewModel;
import br.com.royalfarma.utils.CustomSwipeRefreshLayout;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;

public class HomeFragment extends Fragment implements IFetchProducts, IDetailViewClick {
    private RecyclerView recyclerMaisVendidos, recyclerNovidades, recyclerPopulares;
    private ProdutosHomeAdapter adapterNovidades, adapterPopulares, adapterMaisVendidos;
    private ProductsViewModel productsViewModel;
    private Handler handler;
    private CustomSwipeRefreshLayout swipeContainer;
    private NavController navController;
    private CountDownTimer countdown;
    private LinearLayoutManager linearLayoutManager;
    private CarrinhoViewModel carrinhoViewModel;
    private BottomNavigationView navView;
    private BadgeDrawable badge;
    private PesquisarViewModel pesquisarViewModel;
    private boolean jaLogado;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                updateUI(msg);
            }
        };

        Bundle extras = getArguments();
        if (extras != null)
            jaLogado = extras.getBoolean("jaLogado");

        countdown = new CountDownTimer(5000, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (getActivity() != null) {
                    if (navController != null && navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.navigation_home) {
                        Toast.makeText(getContext(), "Por favor, aguarde...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_option_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = new SearchView(((MainActivity) getContext()).getSupportActionBar().getThemedContext());
        searchView.setQueryHint(getString(R.string.nome_cod_barras));
        item.setActionView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Bundle bundle = new Bundle();
                bundle.putString("query", query);
                navController.navigate(R.id.action_navigation_home_to_navigation_pesquisar, bundle);
                item.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    pesquisarViewModel.setNewQuery(newText);
                }
                return false;
            }
        });
    }

    private void fetchProducts() {
        //resgata produtos assíncronamente
        DataBaseConnection.FetchProducts fetchProducts = new DataBaseConnection.FetchProducts(handler, true, "15", this);
        fetchProducts.execute();
        //Inicia counter para avisar sobre carregamento lento
        countdown.start();
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
            if (root != null) {
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
    }

    @Override
    public void onDestroyView() {
        //confirm frame do not block UI
        toggleFrameLoadingVisibility(false);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        if (productsViewModel != null && productsViewModel.getTodosOsProdutosLiveData().getValue() != null && productsViewModel.getTodosOsProdutosLiveData().getValue().size() > 0)
            toggleFrameLoadingVisibility(false);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //confirm frame do not block UI
        toggleFrameLoadingVisibility(false);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        if (getActivity() != null) {
            navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navView = getActivity().findViewById(R.id.nav_view);
            badge = navView.getBadge(R.id.navigation_carrinho);
        }

        productsViewModel = new ViewModelProvider(requireParentFragment()).get(ProductsViewModel.class);
        carrinhoViewModel = new ViewModelProvider(requireParentFragment()).get(CarrinhoViewModel.class);
        pesquisarViewModel = new ViewModelProvider(requireParentFragment()).get(PesquisarViewModel.class);
        carrinhoViewModel.getBadgeDisplayLiveData().observe(getViewLifecycleOwner(), badgeNumber -> {
            if (navView != null) {
                if (badge != null) {
                    if (badgeNumber <= 0) {
                        badge.setNumber(0);
                        badge.setVisible(false);
                    } else {
                        badge.setVisible(true);
                        badge.setNumber(badgeNumber);
                    }
                } else {
                    badge = navView.getOrCreateBadge(R.id.navigation_carrinho);
                    if (badge != null) {
                        if (badgeNumber == 0) {
                            badge.setNumber(0);
                            badge.setVisible(false);
                        } else {
                            badge.setVisible(true);
                            badge.setNumber(badgeNumber);
                        }
                    } else {
                        Log.e(MY_LOG_TAG, "Falhou ao criar badge");
                    }
                }
            }
        });

        productsViewModel.getProductLiveData().observe(getViewLifecycleOwner(), produto -> {
            ArrayList<Produto> produtosIniciais = productsViewModel.getTodosOsProdutosLiveData().getValue();
            if (produtosIniciais != null) {
                for (int i = 0; i < produtosIniciais.size(); i++) {
                    if (produto.getId() == produtosIniciais.get(i).getId()) {
                        if (i < 5) {
                            if (adapterNovidades != null) {
                                adapterNovidades.notifyItemChanged(i);
                            }
                        } else if (i < 10) {
                            if (adapterPopulares != null) {
                                adapterPopulares.notifyItemChanged(i - 5);
                            }
                        } else {
                            if (adapterMaisVendidos != null) {
                                adapterMaisVendidos.notifyItemChanged(i - 10);
                            }
                        }
                        break;
                    }
                }
            }
        });
        productsViewModel.getTodosOsProdutosLiveData().observe(getViewLifecycleOwner(), listaDeTodosOsProdutos -> {
            if (listaDeTodosOsProdutos.size() == 0) {
                fetchProducts();
            } else {
                //Confirm that frame don't block ui
                toggleFrameLoadingVisibility(false);
                if (listaDeTodosOsProdutos.size() > 5) {
                    linearLayoutManager = new LinearLayoutManager(getContext());
                    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    ArrayList<Produto> novidades = new ArrayList<>(listaDeTodosOsProdutos.subList(0, 5));
                    adapterNovidades = new ProdutosHomeAdapter(novidades, getContext(), carrinhoViewModel, productsViewModel, "novidades", this);
                    adapterNovidades.setHasStableIds(true);
                    recyclerNovidades.setLayoutManager(linearLayoutManager);
                    recyclerNovidades.setAdapter(adapterNovidades);

                    linearLayoutManager = new LinearLayoutManager(getContext());
                    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    ArrayList<Produto> populares = new ArrayList<>(listaDeTodosOsProdutos.subList(5, 10));
                    adapterPopulares = new ProdutosHomeAdapter(populares, getContext(), carrinhoViewModel, productsViewModel, "populares", this);
                    adapterPopulares.setHasStableIds(true);
                    recyclerPopulares.setLayoutManager(linearLayoutManager);
                    recyclerPopulares.setAdapter(adapterPopulares);

                    linearLayoutManager = new LinearLayoutManager(getContext());
                    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    ArrayList<Produto> mais_vendidos = new ArrayList<>(listaDeTodosOsProdutos.subList(10, 15));
                    adapterMaisVendidos = new ProdutosHomeAdapter(mais_vendidos, getContext(), carrinhoViewModel, productsViewModel, "mais_vendidos", this);
                    adapterMaisVendidos.setHasStableIds(true);
                    recyclerMaisVendidos.setLayoutManager(linearLayoutManager);
                    recyclerMaisVendidos.setAdapter(adapterMaisVendidos);
                }
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
            swipeContainer.setOnRefreshListener(this::fetchProducts);
            swipeContainer.setColorSchemeResources(R.color.colorAccent);

            view.findViewById(R.id.btnVerMaisNovidades).setOnClickListener(v -> {
                Bundle extras = new Bundle();
                extras.putString("title", "Novidades");
                ArrayList<Produto> produtosNovidades = new ArrayList<>(productsViewModel.getTodosOsProdutosLiveData().getValue().subList(0, 5));
                extras.putParcelableArrayList("initialProducts", produtosNovidades);
                navController.navigate(R.id.action_navigation_home_to_navigation_lista_produtos, extras);
            });
            view.findViewById(R.id.btnVerMaisPopulares).setOnClickListener(v -> {
                Bundle extras = new Bundle();
                extras.putString("title", "Populares");

                navController.navigate(R.id.action_navigation_home_to_navigation_lista_produtos, extras);
            });
            view.findViewById(R.id.btnVerMaisMaisVendidos).setOnClickListener(v -> {
                Bundle extras = new Bundle();
                extras.putString("title", "Mais Vendidos");
                ArrayList<Produto> produtosMaisVendidos = new ArrayList<>(productsViewModel.getTodosOsProdutosLiveData().getValue().subList(10, 15));
                extras.putParcelableArrayList("initialProducts", produtosMaisVendidos);
                navController.navigate(R.id.action_navigation_home_to_navigation_lista_produtos, extras);
            });

            if (jaLogado) {
                fetchProducts();
            }
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

    public void onDetailViewClick(Produto produto) {
        Intent intent = new Intent(getContext(), ProductDetail.class);
        intent.putExtra("selectedProduct", produto);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            Produto produto = data.getParcelableExtra("selectedProduct");
            if (produto != null) {
                int qntdAddCart = data.getIntExtra("qntdAddCart", 0);
                produto.setQtdNoCarrinho(qntdAddCart);
                carrinhoViewModel.addProductToCart(produto);
            }
        }
        Log.d(MY_LOG_TAG, "on Activity Result");
        super.onActivityResult(requestCode, resultCode, data);
    }
}

