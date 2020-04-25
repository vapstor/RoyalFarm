package br.com.royalfarma.ui.acompanhamento;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.google.gson.Gson;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.adapter.ProdutosPesquisaAdapter;
import br.com.royalfarma.model.Endereco;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;

public class AcompanhamentoCompraFragment extends Fragment {

    private AcompanhamentoCompraViewModel mViewModel;
    private NavController navController;
    private CarrinhoViewModel carrinhoViewModel;
    private ArrayList<Produto> produtosDaCompra;
    private ProdutosPesquisaAdapter adapter;
    private Bundle infosEntrega;
    private RecyclerView recycler;
    private LinearLayoutManager linearLayout;
    private AnimationDrawable arrowAnimation;
    private boolean showContact = true;
    private AppCompatImageView arrowImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infosEntrega = getArguments();
        if (infosEntrega == null) {
            Log.e(MY_LOG_TAG, "Informações de Entrega Vazia!");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        carrinhoViewModel = new ViewModelProvider(getParentFragment()).get(CarrinhoViewModel.class);
        produtosDaCompra = carrinhoViewModel.getCartProductsLiveData().getValue();
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        adapter = new ProdutosPesquisaAdapter(produtosDaCompra, getContext(), null);
        adapter.setHasStableIds(true);
        linearLayout = new LinearLayoutManager(getContext());
        linearLayout.setOrientation(LinearLayoutManager.VERTICAL);
        return inflater.inflate(R.layout.acompanhamento_compra_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        arrowImage = view.findViewById(R.id.iconArrow);
        arrowImage.setOnClickListener(v -> {
            if (showContact) {
                arrowImage.setBackgroundResource(R.drawable.arrow_state_list_down);
                arrowAnimation = (AnimationDrawable) arrowImage.getBackground();
                arrowAnimation.start();
                showContact = false;
            } else {
                arrowImage.setBackgroundResource(R.drawable.arrow_state_list_up);
                arrowAnimation = (AnimationDrawable) arrowImage.getBackground();
                arrowAnimation.start();
                showContact = true;
            }

            View cardLayout = getActivity().findViewById(R.id.card_contact);
            ViewGroup parent = getActivity().findViewById(R.id.layout_acompanhamento_compra_root);

            Transition transition = new Slide(Gravity.BOTTOM);
            transition.setDuration(400);
            transition.addTarget(R.id.card_contact);

            TransitionManager.beginDelayedTransition(parent, transition);
            cardLayout.setVisibility(showContact ? View.VISIBLE : View.GONE);
        });
    }


    private void initViews(View view) {
        AppCompatTextView ruaValue = view.findViewById(R.id.ruaValue);
        AppCompatTextView numeroValue = view.findViewById(R.id.numeroValue);
        AppCompatTextView cepValue = view.findViewById(R.id.cepValue);
        AppCompatTextView cidadeValue = view.findViewById(R.id.cidadeValue);
        AppCompatTextView estadoValue = view.findViewById(R.id.estadoValue);
        AppCompatTextView formaDePagamentoValue = view.findViewById(R.id.formaDePagamentoValue);
        AppCompatTextView subtotalValue = view.findViewById(R.id.subtotalValue);
        AppCompatTextView complementValue = view.findViewById(R.id.complementoValue);
        AppCompatTextView trocoParaQntValue = view.findViewById(R.id.trocoParaQntValue);
        AppCompatTextView bairroValue = view.findViewById(R.id.bairroValue);
        AppCompatTextView paisValue = view.findViewById(R.id.paisValue);
        recycler = view.findViewById(R.id.recyclerViewItensCompra);

        if (infosEntrega != null) {
            Gson g = new Gson();
            Endereco endereco = g.fromJson(infosEntrega.getString("enderecoJson"), Endereco.class);
            String rua = endereco.getAddrStreet();
            String numero = endereco.getAddrNumber();
            String cep = endereco.getAddrZipCode();
            String cidade = endereco.getAddrCity();
            String estado = endereco.getAddrState();
            String complement = endereco.getAddrComplement();
            String bairro = endereco.getAddrDistrict();
            String formaDePagamento = infosEntrega.getString("formaPagamentoValue");
            String subtotal = infosEntrega.getString("subtotalValue");
            String trocoParaQuanto = infosEntrega.getString("trocoParaQuantoValue");
            if (trocoParaQuanto == null) {
                trocoParaQntValue.setVisibility(View.GONE);
            } else {
                trocoParaQntValue.setVisibility(View.VISIBLE);
                trocoParaQntValue.setText("Troco para: " + trocoParaQuanto);
            }
            ruaValue.setText(rua);
            numeroValue.setText(numero);
            cepValue.setText(cep);
            cidadeValue.setText(cidade);
            estadoValue.setText(estado);
            formaDePagamentoValue.setText(formaDePagamento);
            subtotalValue.setText("Subtotal: " + subtotal);
            bairroValue.setText(bairro);
            paisValue.setText("Brasil");
            complementValue.setText(complement);
        }

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recycler.getContext(), linearLayout.getOrientation());
        recycler.addItemDecoration(dividerItemDecoration);
        recycler.setLayoutManager(linearLayout);
        recycler.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(getParentFragment()).get(AcompanhamentoCompraViewModel.class);
    }
}
