package br.com.royalfarma.ui.listagem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.activitys.ProductDetail;
import br.com.royalfarma.adapter.ProdutosListaAdapter;
import br.com.royalfarma.database.DataBaseConnection;
import br.com.royalfarma.interfaces.IFetchProducts;
import br.com.royalfarma.interfaces.OnDetailViewClick;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;
import br.com.royalfarma.ui.home.ProductsViewModel;
import br.com.royalfarma.utils.EndlessRecyclerViewScrollListener;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;

public class ListaDeProdutosFragment extends Fragment implements IFetchProducts, OnDetailViewClick {

    private ProductsViewModel productsViewModel;
    private RecyclerView recycler;
    private ProdutosListaAdapter adapter;
    private NavController navController;
    private String pageTitle;
    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;
    private Handler handler;
    private CountDownTimer countdown;
    private int myPage;
    private ArrayList<Produto> initialProducts;
    private ListaDeProdutosViewModel pagedProductsViewModel;
    private CarrinhoViewModel carrinhoVieModel;
    private BottomNavigationView navView;
    private BadgeDrawable badge;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getArguments();
        if (extras != null) {
            pageTitle = extras.getString("title");
        }

        if (getActivity() != null) {
            navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navView = getActivity().findViewById(R.id.nav_view);
            badge = navView.getBadge(R.id.navigation_carrinho);
        }

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                navController.navigate(R.id.action_navigation_lista_produtos_to_navigation_home);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        callback.setEnabled(true);

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

        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        navController.navigate(R.id.action_navigation_lista_produtos_to_navigation_home);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        if (countdown != null) {
            countdown.cancel();
        }
        super.onDestroy();
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
            final ViewGroup root = getActivity().findViewById(R.id.scrollViewListaProdutosContainer);
            if (root != null) {
                FrameLayout frameLayout = root.findViewById(R.id.frameLoadingLayout);
                TransitionManager.beginDelayedTransition(root, new androidx.transition.Fade().setDuration(750));
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            Produto produto = data.getParcelableExtra("selectedProduct");
            if (produto != null) {
                int qntdAddCart = data.getIntExtra("qntdAddCart", 0);
                produto.setQtdNoCarrinho(qntdAddCart);
                carrinhoVieModel.addProductToCart(produto);
            }
        }
        Log.d(MY_LOG_TAG, "on Activity Result");
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        productsViewModel = new ViewModelProvider(requireParentFragment()).get(ProductsViewModel.class);
        pagedProductsViewModel = new ViewModelProvider(requireParentFragment()).get(ListaDeProdutosViewModel.class);
        carrinhoVieModel = new ViewModelProvider(requireParentFragment()).get(CarrinhoViewModel.class);
        carrinhoVieModel.getBadgeDisplayLiveData().observe(getViewLifecycleOwner(), badgeNumber -> {
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


        ArrayList<Produto> homeProductsBackup = productsViewModel.getTodosOsProdutosLiveData().getValue();
        if (pageTitle.equals("Novidades")) {
            initialProducts = new ArrayList<>(homeProductsBackup.subList(0, 5));
            pagedProductsViewModel.setListaDeProdutos(initialProducts);
        } else if (pageTitle.equals("Populares")) {
            initialProducts = new ArrayList<>(homeProductsBackup.subList(5, 10));
            pagedProductsViewModel.setListaDeProdutos(initialProducts);
        } else {
            initialProducts = new ArrayList<>(homeProductsBackup.subList(10, 15));
            pagedProductsViewModel.setListaDeProdutos(initialProducts);
        }

        pagedProductsViewModel.getListaDeProdutos().observe(getViewLifecycleOwner(), produtos -> {
            ArrayList<Produto> teste = new ArrayList<>(produtos);
            if (teste.size() > 5) {
                adapter.clear();
                adapter.addAll(teste);
                adapter.notifyItemRangeInserted(adapter.getItemCount() - 10, 10);
            }
        });
        View view = inflater.inflate(R.layout.fragment_lista_de_produtos, container, false);
        return view;
    }

    private void fetchProducts(int page) {
        //resgata produtos assíncronamente
        DataBaseConnection.FetchProducts fetchProducts = new DataBaseConnection.FetchProducts(handler, true, "10 OFFSET " + page * 10, this);
        fetchProducts.execute();
        //Inicia counter para avisar sobre carregamento lento
        countdown.start();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getContext();
        if (context != null) {
            toggleFrameLoadingVisibility(false);

            recycler = view.findViewById(R.id.recyclerListaDeProdutos);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            adapter = new ProdutosListaAdapter(initialProducts, getContext(), this);
            adapter.setHasStableIds(true);
            recycler.setLayoutManager(gridLayoutManager);
            recycler.setAdapter(adapter);

            // Retain an instance so that you can call `resetState()` for fresh searches
            scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    recycler.post(() -> fetchProducts(page));
                }
            };
            recycler.addOnScrollListener(scrollListener);
        }
    }

    @Override
    public void onDestroyView() {
        toggleFrameLoadingVisibility(false);
        pagedProductsViewModel.setListaDeProdutos(new ArrayList<>());
        super.onDestroyView();
    }

    private void setupWindowAnimations() {
        if (getActivity() != null) {
            Fade fade = new Fade();
            fade.setDuration(1000);
            getActivity().getWindow().setEnterTransition(fade);

            Slide slide = new Slide();
            slide.setDuration(1000);
            getActivity().getWindow().setReturnTransition(slide);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupWindowAnimations();
    }

    @Override
    public void onGetDataDone(ArrayList<Produto> listaDeProdutos) {
        ArrayList<Produto> productsInPersistence = pagedProductsViewModel.getListaDeProdutos().getValue();
//        ArrayList<Produto> listaDeProdutosClean = new ArrayList<>();
        if (productsInPersistence != null) {
//            for (Produto p : listaDeProdutos) {
//                for (Produto produtoPersistent : productsInPersistence) {
//                    if (p.getId() != produtoPersistent.getId()) {
//                        Log.d(MY_LOG_TAG, p.getNome());
//                        listaDeProdutosClean.add(p);
//                    }
//                }
//            }
//            if (listaDeProdutos.size() > 0) {
            boolean success = productsInPersistence.addAll(listaDeProdutos);
            if (success) {
                pagedProductsViewModel.setListaDeProdutos(productsInPersistence);
            } else {
                pagedProductsViewModel.setListaDeProdutos(productsInPersistence);
            }
        } else {
            pagedProductsViewModel.setListaDeProdutos(listaDeProdutos);
        }
    }

    public void onDetailViewClick(Produto produto) {
        Intent intent = new Intent(getContext(), ProductDetail.class);
        intent.putExtra("selectedProduct", produto);
        startActivityForResult(intent, 1);
    }
}
