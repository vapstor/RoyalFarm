package br.com.royalfarma.ui.finalizacao;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import br.com.royalfarma.R;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;
import br.com.royalfarma.utils.MoneyTextWatcher;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.fromRStoDouble;

public class FinalizarCompraFragment extends Fragment {

    private AppCompatEditText inputTroco, inputEndereco, inputNumero, inputCEP, inputBairro, inputCidade;
    private AppCompatAutoCompleteTextView inputEstado;
    private TextInputLayout layoutEndereco, layoutNumero, layoutCEP, layoutBairro, layoutCidade, layoutEstado, layoutTroco;
    private RadioGroup formaPagamentoRadioGroup;
    private SwitchCompat switchTroco;
    private AppCompatButton btnFinaliza;
    private String endereco, numero, cep, bairro, cidade, estado, troco, formaPagamento;
    private int radioButtonCartaoID, radioButtonDinheiroID;
    private CarrinhoViewModel carrinhoViewModel;
    private String subtotal;
    private NavController navController;
    private AppCompatImageView iconeInfoTroco;
    private String[] arrayEstadosBR;
    private MoneyTextWatcher moneyTextWatcher;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        carrinhoViewModel = new ViewModelProvider(getParentFragment()).get(CarrinhoViewModel.class);
        subtotal = carrinhoViewModel.getSubtotalLiveData().getValue();

        View view = inflater.inflate(R.layout.finalizar_compra_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        formaPagamentoRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            ((RadioButton) formaPagamentoRadioGroup.getChildAt(0)).setError(null);
            if (checkedId == group.findViewById(R.id.radioBtnDinheiro).getId()) {
                formaPagamento = "Dinheiro";
                toggleVisibilityCtnrTroco(true);
            } else {
                formaPagamento = "Cartão (Máquina Pessoalmente)";
                toggleVisibilityCtnrTroco(false);
            }
        });
        iconeInfoTroco.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Rounded)
                    .setTitle(R.string.necessidade_de_troco)
                    .setMessage(R.string.utilize_para_especificar_ao_motoboy_quanto_ele_precisara_levar_de_troco)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    })
                    .setCancelable(true)
                    .show();
        });
        switchTroco.setOnClickListener(v -> toggleVisibilityInputLayoutTroco());
        toggleVisibilityInputLayoutTroco();

        btnFinaliza.setOnClickListener(v -> {
            boolean validatedFields = validateFieldsEntrega();
            boolean validatedFormaDePagamento = validateFormaDePagamento();
            if (validatedFormaDePagamento && validatedFields) {
                Bundle infosEntrega = new Bundle();
                infosEntrega.putString("ruaValue", endereco);
                infosEntrega.putString("numeroValue", numero);
                infosEntrega.putString("cepValue", cep);
                infosEntrega.putString("cidadeValue", cidade);
                infosEntrega.putString("estadoValue", estado);
                infosEntrega.putString("formaPagamentoValue", formaPagamento);
                infosEntrega.putString("subtotalValue", subtotal);
                if (switchTroco.isChecked())
                    infosEntrega.putString("trocoParaQuantoValue", troco);
                else
                    infosEntrega.putString("trocoParaQuantoValue", null);

                navController.navigate(R.id.action_finalizarCompraFragment_to_acompanhamentoCompraFragment, infosEntrega);
            } else {
                Toast.makeText(getContext(), "Confira as informações", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        navController.navigate(R.id.action_finalizarCompraFragment_to_navigation_carrinho);
        return super.onOptionsItemSelected(item);
    }

    private void toggleVisibilityCtnrTroco(boolean containerVisibility) {
        if (getActivity() != null) {
            final ViewGroup root = getActivity().findViewById(R.id.scrollViewCntrFinalizar);
            if (root != null) {
                ConstraintLayout containerTroco = root.findViewById(R.id.container_troco);
                TransitionManager.beginDelayedTransition(root, new Fade().setDuration(500));
                if (containerVisibility) {
                    containerTroco.setVisibility(View.VISIBLE);
                    iconeInfoTroco.setVisibility(View.VISIBLE);
                } else {
                    containerTroco.setVisibility(View.INVISIBLE);
                    iconeInfoTroco.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void toggleVisibilityInputLayoutTroco() {
        final ViewGroup root = getActivity().findViewById(R.id.container_troco);
        if (root != null) {
            TransitionManager.beginDelayedTransition(root, new Fade().setDuration(500));
            if (switchTroco.isChecked()) {
                if (layoutTroco.getVisibility() != View.VISIBLE)
                    layoutTroco.setVisibility(View.VISIBLE);
            } else {
                layoutTroco.setErrorEnabled(false);
                if (layoutTroco.getVisibility() != View.INVISIBLE)
                    layoutTroco.setVisibility(View.INVISIBLE);
            }
        }
    }

    private boolean validateFormaDePagamento() {
        int checkedId = formaPagamentoRadioGroup.getCheckedRadioButtonId();
        if (checkedId != -1) {
            ((RadioButton) formaPagamentoRadioGroup.getChildAt(0)).setError(null);
            if (checkedId == radioButtonCartaoID) {
                return true;
            } else if (checkedId == radioButtonDinheiroID) {
                if (!switchTroco.isChecked()) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Rounded)
                            .setTitle(R.string.atencao)
                            .setMessage(R.string.voce_tem_certeza_que_nao_deseja_informar_um_valor_para_o_motoboy_levar_troco_)
                            .setNegativeButton(R.string.tenho_a_quantia_exata, (dialog, which) -> {
                                userChoose(true);
                            })
                            .setPositiveButton(R.string.informar_valor, (dialog, which) -> {
                                toggleVisibilityInputLayoutTroco();
                                inputTroco.requestFocus();
                                userChoose(false);
                            })
                            .setCancelable(false);
                    builder.show();
                } else {
                    //Troco
                    if (inputTroco.getText() != null) {
                        troco = inputTroco.getText().toString();
                        if (troco.isEmpty()) {
                            layoutTroco.setErrorEnabled(true);
                            layoutTroco.setError("preencha um valor");
                        } else if (fromRStoDouble(troco) < fromRStoDouble(subtotal)) {
                            layoutTroco.setErrorEnabled(true);
                            layoutTroco.setError("mínimo: " + subtotal);
                        } else {
                            layoutTroco.setErrorEnabled(false);
                            return true;
                        }
                    } else {
                        layoutTroco.setErrorEnabled(true);
                        Toast.makeText(getContext(), "Endereço nulo!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            ((RadioButton) formaPagamentoRadioGroup.getChildAt(0)).setError("Escolha uma opção");
        }
        return false;
    }

    public void userChoose(boolean quantiaExata) {
        if (quantiaExata) {
            navController.navigate(R.id.action_finalizarCompraFragment_to_acompanhamentoCompraFragment);
        } else {
            switchTroco.setChecked(true);
            toggleVisibilityInputLayoutTroco();
        }
    }

    private boolean validateFieldsEntrega() {
        //Endereco
        if (inputEndereco.getText() != null) {
            endereco = inputEndereco.getText().toString();
            if (endereco.isEmpty()) {
                layoutEndereco.setErrorEnabled(true);
                layoutEndereco.setError("preencha o endereço");
            } else {
                layoutEndereco.setErrorEnabled(false);
            }
        } else {
            layoutEndereco.setErrorEnabled(true);
            Toast.makeText(getContext(), "Endereço nulo!", Toast.LENGTH_SHORT).show();
        }

        //Numero
        if (inputNumero.getText() != null) {
            numero = inputNumero.getText().toString();
            layoutNumero.setErrorEnabled(false);
        } else {
            layoutNumero.setErrorEnabled(true);
            Toast.makeText(getContext(), "Numero nulo!", Toast.LENGTH_SHORT).show();
        }

        //Bairro
        if (inputBairro.getText() != null) {
            bairro = inputBairro.getText().toString();
            if (bairro.isEmpty()) {
                layoutBairro.setErrorEnabled(true);
                layoutBairro.setError("preencha o bairro");
            } else {
                layoutBairro.setErrorEnabled(false);
            }
        } else {
            layoutBairro.setErrorEnabled(true);
            Toast.makeText(getContext(), "Bairro nulo!", Toast.LENGTH_SHORT).show();
        }

        //CEP
        if (inputCEP.getText() != null) {
            cep = inputCEP.getText().toString();
            if (cep.isEmpty()) {
                layoutCEP.setErrorEnabled(true);
                layoutCEP.setError("preencha o cep");
            } else if (cep.length() < 8) {
                layoutCEP.setErrorEnabled(true);
                layoutCEP.setError("cep inválido");
            } else {
                layoutCEP.setErrorEnabled(false);
            }
        } else {
            layoutCEP.setErrorEnabled(true);
            Toast.makeText(getContext(), "CEP nulo!", Toast.LENGTH_SHORT).show();
        }

        //Cidade
        if (inputCidade.getText() != null) {
            cidade = inputCidade.getText().toString();
            if (cidade.isEmpty()) {
                layoutCidade.setErrorEnabled(true);
                layoutCidade.setError("preencha a cidade");
            } else {
                layoutCidade.setErrorEnabled(false);
            }
        } else {
            layoutCidade.setErrorEnabled(true);
            Toast.makeText(getContext(), "Cidade nula!", Toast.LENGTH_SHORT).show();
        }

        //Estado
        if (inputEstado.getText() != null) {
            estado = inputEstado.getText().toString();
            if (estado.isEmpty()) {
                layoutEstado.setErrorEnabled(true);
                layoutEstado.setError("preencha o estado");
            } else {
                layoutEstado.setErrorEnabled(false);
            }
        } else {
            layoutEstado.setErrorEnabled(true);
            Toast.makeText(getContext(), "Estado nulo!", Toast.LENGTH_SHORT).show();
        }

        if (layoutEndereco.isErrorEnabled() ||
                layoutNumero.isErrorEnabled() ||
                layoutCEP.isErrorEnabled() ||
                layoutBairro.isErrorEnabled() ||
                layoutCidade.isErrorEnabled() ||
                layoutEstado.isErrorEnabled()
        ) {
            return false;
        }
        return true;
    }

    private void initViews(View view) {
        inputEndereco = view.findViewById(R.id.inputEndereco);
        inputNumero = view.findViewById(R.id.inputNumero);
        inputCEP = view.findViewById(R.id.inputCEP);
        inputBairro = view.findViewById(R.id.inputBairro);
        inputCidade = view.findViewById(R.id.inputCidade);
        inputEstado = view.findViewById(R.id.inputUF);
        inputTroco = view.findViewById(R.id.inputTroco);

        layoutEndereco = view.findViewById(R.id.layoutInputEndereco);
        layoutNumero = view.findViewById(R.id.layoutInputNumero);
        layoutCEP = view.findViewById(R.id.layoutInputCEP);
        layoutBairro = view.findViewById(R.id.layoutInputBairro);
        layoutCidade = view.findViewById(R.id.layoutInputCidade);
        layoutEstado = view.findViewById(R.id.layoutInputUF);
        layoutTroco = view.findViewById(R.id.layoutInputTroco);

        iconeInfoTroco = view.findViewById(R.id.iconeInfoTroco);

        formaPagamentoRadioGroup = view.findViewById(R.id.formaDePagamentoRadioGroup);
        radioButtonCartaoID = view.findViewById(R.id.radioBtnCartao).getId();
        radioButtonDinheiroID = view.findViewById(R.id.radioBtnDinheiro).getId();

        switchTroco = view.findViewById(R.id.switchCompat);
        btnFinaliza = view.findViewById(R.id.btnFinalizarCompra);

        if (getActivity() != null) {
            arrayEstadosBR = getResources().getStringArray(R.array.ufs);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_menu_popup__item, arrayEstadosBR);
            inputEstado.setAdapter(adapter);
        } else {
            Log.e(MY_LOG_TAG, "Atividade Nula!");
        }

        moneyTextWatcher = new MoneyTextWatcher(inputTroco);
        inputTroco.addTextChangedListener(moneyTextWatcher);
    }

}
