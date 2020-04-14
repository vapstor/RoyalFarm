package br.com.royalfarma.ui.listaDeProdutos;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.transition.Fade;
import android.transition.Slide;
import android.view.LayoutInflater;
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

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.adapter.ProdutosListaAdapter;
import br.com.royalfarma.database.DataBaseConnection;
import br.com.royalfarma.interfaces.IFetchProducts;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.ui.home.ProductsViewModel;
import br.com.royalfarma.utils.EndlessRecyclerViewScrollListener;

public class ListaDeProdutosFragment extends Fragment implements IFetchProducts {

    private ProductsViewModel productsViewModel;
    private RecyclerView recycler;
    private ProdutosListaAdapter adapter;
    private NavController navController;
    private String pageTitle;
    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;
    private ArrayList<Produto> produtosShort;
    private Handler handler;
    private CountDownTimer countdown;
    private ArrayList<Produto> listaDeProdutosClean = new ArrayList<>();
    private int myPage;
    private ArrayList<Produto> productsInPersistence;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getArguments();
        if (extras != null) {
            pageTitle = extras.getString("title");
        }

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        productsViewModel = new ViewModelProvider(requireParentFragment()).get(ProductsViewModel.class);
        productsViewModel.getTodosOsProdutosLiveData().observe(getViewLifecycleOwner(), produtos -> {

            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            if (produtos.size() == 15) {
                if (pageTitle.equals("Novidades")) {
                    adapter = new ProdutosListaAdapter(new ArrayList<>(produtos.subList(0, 5)), getContext());
                } else if (pageTitle.equals("Populares")) {
                    adapter = new ProdutosListaAdapter(new ArrayList<>(produtos.subList(5, 10)), getContext());
                } else {
                    adapter = new ProdutosListaAdapter(new ArrayList<>(produtos.subList(10, 15)), getContext());
                }
            } else {
                adapter = new ProdutosListaAdapter(produtos, getContext());
            }
            adapter.setHasStableIds(true);
            recycler.setLayoutManager(gridLayoutManager);
            recycler.setAdapter(adapter);

            // Retain an instance so that you can call `resetState()` for fresh searches
            scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    // Triggered only when new data needs to be appended to the list
                    // Add whatever code is needed to append new items to the bottom of the list
                    getMoreItens();
                }
            };


            // Adds the scroll listener to RecyclerView
            recycler.addOnScrollListener(scrollListener);

        });
        View view = inflater.inflate(R.layout.fragment_lista_de_produtos, container, false);
        return view;
    }

    //A cada 10 itens = 1 página
    private void getMoreItens() {
        recycler.post(this::fetchProducts);
    }

    private void fetchProducts() {
        //resgata produtos assíncronamente
        new Thread(() -> {
            DataBaseConnection.FetchProducts fetchProducts = new DataBaseConnection.FetchProducts(handler, true, 10, this);
            fetchProducts.execute();
            //Inicia counter para avisar sobre carregamento lento
            countdown.start();
        }).start();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getContext();
        if (context != null) {
            recycler = view.findViewById(R.id.recyclerListaDeProdutos);
        }
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
        productsInPersistence = productsViewModel.getTodosOsProdutosLiveData().getValue();
        if (productsInPersistence != null) {
            for (Produto produtoPersistent : productsInPersistence) {
                for (Produto p : listaDeProdutos) {
                    if (p.getId() != produtoPersistent.getId()) {
                        listaDeProdutosClean.add(p);
                    }
                }
            }
            if (listaDeProdutosClean.size() > 0) {
                boolean success = productsInPersistence.addAll(listaDeProdutosClean);
                if (success) {
                    productsViewModel.setTodosOsProdutos(productsInPersistence);
                }
                adapter.notifyItemRangeInserted(productsInPersistence.size(), listaDeProdutosClean.size());
            }
        } else {
            productsViewModel.setTodosOsProdutos(listaDeProdutos);
        }
    }
}
