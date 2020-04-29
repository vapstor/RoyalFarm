package br.com.royalfarma.ui.pesquisar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import br.com.royalfarma.adapter.ProdutosPesquisaAdapter;
import br.com.royalfarma.database.DataBaseConnection;
import br.com.royalfarma.interfaces.IDetailViewClick;
import br.com.royalfarma.interfaces.IFetchProducts;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;
import br.com.royalfarma.utils.ItemClickSupport;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;

public class PesquisarFragment extends Fragment implements IFetchProducts, IDetailViewClick {

    private PesquisarViewModel pesquisarViewModel;
    private String query;
    private ProgressBar searchProgressBar;
    private Handler handler;
    private ProdutosPesquisaAdapter adapter;
    private RecyclerView recycler;
    private AppCompatImageView semItensDrawable;
    private AppCompatTextView semItensLabel;
    private int progresBarSize;
    private NavController navController;
    private CarrinhoViewModel carrinhoViewModel;
    private BottomNavigationView navView;
    private BadgeDrawable badge;
    private ArrayList<Produto> searchProductsList;
    private DividerItemDecoration dividerItemDecoration;
    private LinearLayoutManager linearLayout;
    private ConstraintLayout layoutNoItems;
    private ViewGroup root;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            query = arguments.getString("query");
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                updateUI(msg);
            }
        };
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_option_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = new SearchView(((MainActivity) getContext()).getSupportActionBar().getThemedContext());
        searchView.setQueryHint(getString(R.string.nome_cod_barras));
        item.setActionView(searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                item.collapseActionView();
                fetchProductsByQuery(query);
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
        if (query != null && !query.equals("")) {
            searchView.setQuery(query, true);
            query = null;
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pesquisar, container, false);

        if (getActivity() != null) {
            navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            navView = getActivity().findViewById(R.id.nav_view);
            badge = navView.getBadge(R.id.navigation_carrinho);
        }

        pesquisarViewModel = new ViewModelProvider(requireParentFragment()).get(PesquisarViewModel.class);
        carrinhoViewModel = new ViewModelProvider(requireParentFragment()).get(CarrinhoViewModel.class);
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
        pesquisarViewModel.getAllSearchedProductsLiveData().observe(getViewLifecycleOwner(), produtos -> {
            if (produtos != null) {
                if (produtos.size() > 0) {
                    toggleRecyclerVisibility(true);
                    adapter.clear();
                    adapter.addAll(produtos);
                    adapter.notifyDataSetChanged();
                } else {
                    toggleRecyclerVisibility(false);
                }
            }
        });
        return root;
    }

    private void fetchProductsByQuery(String query) {
        DataBaseConnection.FetchProductsByQuery fetchProductsByQuery = new DataBaseConnection.FetchProductsByQuery(handler, true, 15, this);
        fetchProductsByQuery.execute(query);
        toggleProgressBarVisibility(true);
    }
//

    private void toggleVisibilityLayoutNoItems(boolean visibility) {
        TransitionManager.beginDelayedTransition(root, new Fade());
        if (visibility)
            layoutNoItems.setVisibility(View.VISIBLE);
        else
            layoutNoItems.setVisibility(View.GONE);
    }

    private void toggleRecyclerVisibility(boolean recyclerVisibility) {
        TransitionManager.beginDelayedTransition(root, new Fade());
        if (recyclerVisibility) {
            recycler.setVisibility(View.VISIBLE);
            layoutNoItems.setVisibility(View.GONE);
        } else {
            recycler.setVisibility(View.GONE);
            layoutNoItems.setVisibility(View.VISIBLE);
        }
    }

    private void toggleProgressBarVisibility(boolean visibility) {
        TransitionManager.beginDelayedTransition(root, new Fade());
        if (visibility) {
            toggleRecyclerVisibility(false);
            toggleVisibilityLayoutNoItems(false);
            searchProgressBar.setVisibility(View.VISIBLE);
        } else {
            searchProgressBar.setVisibility(View.GONE);
        }
    }


    private void updateUI(Message msg) {
        if (msg.what == 0) {
            if ("tentando_conectar".equals(msg.obj)) {
                toggleProgressBarVisibility(true);
            }
        } else if (msg.what == 1) {
            switch ((String) msg.obj) {
                case "onPreExecute":
//                    searchProgressBar.setIndeterminate(false);
                    break;
                case "erro_sql":
                    Toast.makeText(getContext(), "Erro de SQL", Toast.LENGTH_SHORT).show();
                    toggleProgressBarVisibility(false);
                    break;
                case "onPostExecute":
                    break;
                default:
                    if (((String) msg.obj).contains("size: ")) {
//                        progresBarSize = Integer.parseInt(((String) msg.obj).replace("size: ", ""));
//                        if (progresBarSize == 0) {
//                            progresBarSize = 1;
//                        }
//                        searchProgressBar.setMax(progresBarSize * 10);
                    } else if (((String) msg.obj).contains("progress: ")) {
//                        int progress = Integer.parseInt(((String) msg.obj).replace("progress: ", ""));
//                        searchProgressBar.setProgress(progress * 10);
                    }
                    break;
            }
        } else {
            Toast.makeText(getContext(), "Ocorreu um erro!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null) {
            root = getActivity().findViewById(R.id.pesquisar_root_layout);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
        }
        searchProgressBar = view.findViewById(R.id.searchProgressBar);
        searchProgressBar.setIndeterminate(true);
        recycler = view.findViewById(R.id.recyclerPesquisa);
        semItensDrawable = view.findViewById(R.id.noItensDrawable);
        semItensLabel = view.findViewById(R.id.noItensLabel);
        layoutNoItems = view.findViewById(R.id.layoutNoItems);

        searchProductsList = new ArrayList<>();

        adapter = new ProdutosPesquisaAdapter(searchProductsList, getContext(), this);
        adapter.setHasStableIds(true);
        linearLayout = new LinearLayoutManager(getContext());
        linearLayout.setOrientation(LinearLayoutManager.VERTICAL);
        dividerItemDecoration = new DividerItemDecoration(recycler.getContext(), linearLayout.getOrientation());
        recycler.addItemDecoration(dividerItemDecoration);
        recycler.setLayoutManager(linearLayout);
        recycler.setAdapter(adapter);

        ItemClickSupport.addTo(recycler).setOnItemClickListener((recyclerView, position, v) -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), ProductDetail.class);
                intent.putExtra("selectedProduct", pesquisarViewModel.getAllSearchedProductsLiveData().getValue().get(position));
                startActivityForResult(intent, 1);
            }
        });
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

    @Override
    public void onGetDataDone(ArrayList<Produto> listaDeProdutos) {
        toggleProgressBarVisibility(false);
        pesquisarViewModel.updateProductsList(listaDeProdutos);
    }

    @Override
    public void onDetailViewClick(Produto produto) {
        Intent intent = new Intent(getContext(), ProductDetail.class);
        intent.putExtra("selectedProduct", produto);
        startActivityForResult(intent, 1);
    }
}
