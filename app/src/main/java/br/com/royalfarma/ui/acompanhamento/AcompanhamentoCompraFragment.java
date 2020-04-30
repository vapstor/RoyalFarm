package br.com.royalfarma.ui.acompanhamento;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.adapter.ProdutosPesquisaAdapter;
import br.com.royalfarma.database.DataBaseConnection;
import br.com.royalfarma.interfaces.iOrderStatus;
import br.com.royalfarma.model.Endereco;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;

public class AcompanhamentoCompraFragment extends Fragment implements iOrderStatus {

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
    private Handler handler;
    private AppCompatTextView numPedidoValue;
    private String idPedido;
    private CountDownTimer countdown;
    private AppCompatImageView iconeAceito;
    private AppCompatTextView textViewAceito;
    private ProgressBar progressBarAceito;
    private AppCompatImageView iconeEmTransporte;
    private AppCompatTextView textViewEmTransporte;
    private ProgressBar progressBarPedidoTransporte;
    private AppCompatImageView iconeFinalizado;
    private AppCompatTextView textViewFinalizado;
    private int orderStatus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infosEntrega = getArguments();
        if (infosEntrega == null) {
            Log.e(MY_LOG_TAG, "Informações de Entrega Vazia!");
        }
        setHasOptionsMenu(false);
    }

    private void createCountDown() {
        getOrderStatusDB();
        //Countdown de 1/2 Hora
        countdown = new CountDownTimer(120000, 60000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(MY_LOG_TAG, "milis: " + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                if (orderStatus == 1 || orderStatus == 2) {
                    //finalizou ou cancelou
                } else {
                    this.cancel();
                    createCountDown();
                }
            }
        }.start();
    }

    private void navigateToHome() {
        Bundle extras = new Bundle();
        extras.putBoolean("jaLogado", true);
        navController.navigate(R.id.action_acompanhamentoCompraFragment_to_navigation_home, extras);
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
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
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
        createCountDown();
    }


    private void initViews(View view) {
        AppCompatTextView ruaValue = view.findViewById(R.id.ruaValue);
        AppCompatTextView numPedidoValue = view.findViewById(R.id.numPedidoValue);
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

        iconeAceito = view.findViewById(R.id.iconeAceito);
        textViewAceito = view.findViewById(R.id.textViewAceito);
        progressBarAceito = view.findViewById(R.id.progressBarPedidoAceito);

        iconeEmTransporte = view.findViewById(R.id.iconeEmTransporte);
        textViewEmTransporte = view.findViewById(R.id.textViewEmTransporte);
        progressBarPedidoTransporte = view.findViewById(R.id.progressBarPedidoTransporte);

        iconeFinalizado = view.findViewById(R.id.iconeFinalizado);
        textViewFinalizado = view.findViewById(R.id.textViewFinalizado);

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
            idPedido = infosEntrega.getString("idPedido");
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
            numPedidoValue.setText(String.valueOf(idPedido));
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


    private void getOrderStatusDB() {
        DataBaseConnection.GetOrderStatus orderStatus = new DataBaseConnection.GetOrderStatus(this);
        orderStatus.execute(idPedido);
    }

    @Override
    public void orderStatus(int status) {
        //Order_status 1- concluido 2 - cancelado 3 - novo pedido 4 - aguardando pagamento 6 - processando
        orderStatus = status;
        Log.d(MY_LOG_TAG, "Order Status: " + orderStatus);
        switch (orderStatus) {
            case 3:
                iconeAceito.setImageDrawable(getResources().getDrawable(R.drawable.ic_thumb_up_accent_24dp));
                textViewAceito.setTextColor(getResources().getColor(R.color.colorAccent));
                progressBarAceito.setProgress(100);
                break;
            case 6:
                iconeEmTransporte.setImageDrawable(getResources().getDrawable(R.drawable.ic_motorcycle_accent_24dp));
                textViewEmTransporte.setTextColor(getResources().getColor(R.color.colorAccent));
                progressBarPedidoTransporte.setProgress(100);
                break;
            case 2:
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Rounded);
                builder.setTitle("Pedido Cancelado")
                        .setMessage("Seu pedido (nº " + idPedido + ") encontra-se com o status cancelado.\nEntre em contato para obter mais detalhes.")
                        .setPositiveButton("Ok", (dialog, which) -> navigateToHome())
                        .setCancelable(false)
                        .create()
                        .show();
            case 1:
                //finalizou
                iconeFinalizado.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_circle_accent_24dp));
                textViewFinalizado.setTextColor(getResources().getColor(R.color.colorAccent));

                //zera carrinho
                carrinhoViewModel.updateProductsList(new ArrayList<>());
                carrinhoViewModel.updateBadgeDisplay();

                MaterialAlertDialogBuilder builder2 = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Rounded);
                builder2.setTitle("Pedido Finalizado")
                        .setMessage("Seu pedido (nº " + idPedido + ") encontra-se com o status finalizado.\nAgradecemos pela preferência!")
                        .setPositiveButton("Ok", (dialog, which) -> navigateToHome())
                        .setCancelable(false)
                        .create()
                        .show();
                break;
            case -1:
                Log.e(MY_LOG_TAG, "Erro de SQL");
                Toast.makeText(getContext(), "Erro de SQL", Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.d(MY_LOG_TAG, "Switch não capturado: " + orderStatus);
                break;
        }

    }
}
