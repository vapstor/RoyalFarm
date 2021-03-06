package br.com.royalfarma.ui.finalizacao;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
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
import com.google.gson.Gson;

import java.util.ArrayList;

import br.com.royalfarma.R;
import br.com.royalfarma.database.DataBaseConnection;
import br.com.royalfarma.interfaces.iCEPHelper;
import br.com.royalfarma.model.Endereco;
import br.com.royalfarma.model.Produto;
import br.com.royalfarma.model.Usuario;
import br.com.royalfarma.ui.carrinho.CarrinhoViewModel;
import br.com.royalfarma.ui.login.LoginViewModel;
import br.com.royalfarma.utils.CepTextWatcher;
import br.com.royalfarma.utils.CustomAutoCompleteTextView;
import br.com.royalfarma.utils.MoneyTextWatcher;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.fromRStoDouble;

public class FinalizarCompraFragment extends Fragment implements iCEPHelper {

    private AppCompatEditText inputTroco, inputRua, inputNumero, inputCEP, inputBairro, inputCidade, inputComplemento, inputNomeEndereco;
    private CustomAutoCompleteTextView inputEstado, inputSpinnerEndereco;
    private TextInputLayout layoutEndereco, layoutNumero, layoutCEP, layoutBairro, layoutCidade, layoutEstado, layoutTroco, layoutInputSpinnerEndereco, layoutInputNomeEndereco;
    private RadioGroup formaPagamentoRadioGroup, freteRadioGroup;
    private SwitchCompat switchTroco;
    private AppCompatButton btnAvancar, btnFinalizarCompra;
    private Endereco endereco;
    private String nomeEndereco, rua, numero, cep, bairro, cidade, estado, dinheiroDoCliente, labelFormaPagamento, complemento;
    private int radioButtonCartaoID, radioButtonDinheiroID;
    private CarrinhoViewModel carrinhoViewModel;
    private String subtotal;
    private NavController navController;
    private String[] arrayEstadosBR;
    private int orderPayment, formaFrete;
    private LoginViewModel loginViewModel;
    private Usuario usuario;
    private ImageButton btnSaveInfos, btnEditInfos, btnClearAddEndereco, btnAddEndereco;
    private Handler handler;
    private ProgressBar progressBarSaveAddr, progressBarFinaliza, progressBarAvancarFinalizacaoCompra;
    private CepTextWatcher cepTextWatcher;
    private boolean erro;
    private boolean finalizado;
    private AlertDialog formaPagamentoEFreteDialog;
    private String idPedido;

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
        loginViewModel = new ViewModelProvider(getParentFragment()).get(LoginViewModel.class);
        usuario = loginViewModel.getUsuarioMutableLiveData().getValue();
        loginViewModel.getUsuarioMutableLiveData().observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                ArrayList<Endereco> enderecos = usuario.getEnderecos();
                if (enderecos != null && enderecos.size() > 0) {
                    //Encontrou endereços do cliente
                    toggleButtonEditVisibility(true, false);
                    if (isInputsEnabled())
                        setInputsEnabled(false);
                    configSpinnerWithAdress(enderecos);
                } else {
                    btnAddEndereco.setVisibility(View.INVISIBLE);
                    toggleButtonEditVisibility(false, false);
                    toggleSpinnerEnderecoVisibility(false);
                    if (!isInputsEnabled())
                        setInputsEnabled(true);
                }
            }
        });
        subtotal = carrinhoViewModel.getSubtotalLiveData().getValue();

        View view = inflater.inflate(R.layout.fragment_finalizar_compra, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //        if (usuario.getAddrID() != 0) {
//            //Usuário com endereço salvo
//            final AlertDialog.Builder d = new AlertDialog.Builder(getContext(), R.style.MaterialAlertDialog_Rounded);
//            LayoutInflater inflater = ((AppCompatActivity) getContext()).getLayoutInflater();
//            View dialogView = inflater.inflate(R.layout.layout_dialog_item_quantity, null);
//            d.setTitle(R.string.endereco_encontrado);
//            d.setMessage(R.string.em_seu_perfil_foi_encontrado_um_endereço_previamente_cadastrado);
//            d.setView(dialogView);
//            d.setPositiveButton("OK", (dialogInterface, i) -> {
//            });
//            AlertDialog alertDialog = d.create();
//            alertDialog.show();
//        } else {
//            final AlertDialog.Builder d = new AlertDialog.Builder(getContext(), R.style.MaterialAlertDialog_Rounded);
//            LayoutInflater inflater = ((AppCompatActivity) getContext()).getLayoutInflater();
//            View dialogView = inflater.inflate(R.layout.layout_dialog_item_quantity, null);
//            d.setTitle(R.string.atencao);
//            d.setMessage("Não foram encontrados endereços de entrega cadastrados em seu perfil.\nCadastre um a seguir!");
//            d.setView(dialogView);
//            d.setPositiveButton("OK", (dialogInterface, i) -> {
//            });
//            AlertDialog alertDialog = d.create();
//            alertDialog.show();
//
//        }
//
        initViews(view);
    }

    private void configSpinnerWithAdress(ArrayList<Endereco> enderecos) {
        endereco = enderecos.get(enderecos.size() - 1);
        if (endereco != null) {
            String[] nomesEnderecos = new String[enderecos.size()];
            for (int i = 0; i < enderecos.size(); i++) {
                String nome = enderecos.get(i).getAddrName();
                if (nome == null) {
                    endereco.setAddrName("Endereço sem nome");
                    nomesEnderecos[i] = "Endereço sem nome";
                } else {
                    nomesEnderecos[i] = nome;
                }
            }
            if (getActivity() != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_menu_popup_item, nomesEnderecos);
                inputSpinnerEndereco.setAdapter(adapter);
                setInfosUserOnViews(endereco);
                inputSpinnerEndereco.setText(inputSpinnerEndereco.getAdapter().getItem(enderecos.size() - 1).toString(), false);
                inputSpinnerEndereco.setOnItemClickListener((parent, view, position, id) -> {
                    endereco = enderecos.get(position);
                    setInfosUserOnViews(endereco);
                });
            }
        }

    }

    private void insertOrUpdateEnderecoCliente() {
        DataBaseConnection.InsertClientAddress getClientInfo = new DataBaseConnection.InsertClientAddress(handler, usuario, endereco);
        getClientInfo.execute();
    }

    private void updateUI(Message msg) {
        if (msg.what == 0) {
            if ("tentando_conectar".equals(msg.obj)) {
                toggleButtonEditVisibility(false, true);
            }
        } else if (msg.what == 1) {
            String obj = (String) msg.obj;
            if (obj.contains("orderIdValue")) {
                idPedido = ((String) msg.obj).replace("orderIdValue", "");
            } else {

                int key = 0;
                if (obj.contains("inserido")) {
                    if (obj.contains("_")) {
                        key = Integer.parseInt(obj.replace("inserido_", ""));
                        obj = "inserido";
                    }
                }
                switch (obj) {
                    case "onPreExecute":
                        erro = false;
                        toggleButtonEditVisibility(false, true);
                        break;
                    case "onPreExecuteInsereOrdem":
//                        toggleBtnFinalizarVisibility(false);
                        break;
                    case "pedidoInserido":
                        finalizado = true;
                        toggleBtnFinalizarVisibility(true);
                        if (formaPagamentoEFreteDialog != null && formaPagamentoEFreteDialog.isShowing()) {
                            formaPagamentoEFreteDialog.dismiss();
                        }
                        Toast.makeText(getContext(), R.string.pedido_efetuado_com_sucesso, Toast.LENGTH_LONG).show();
                        navigateToAcompanhamento();
                        break;
                    case "inserido":
                        erro = false;
                        endereco.setAddrID(key);
                        usuario.getEnderecos().add(endereco);
                        Toast.makeText(getContext(), R.string.endereco_inserido_com_sucesso, Toast.LENGTH_LONG).show();
                        break;
                    case "atualizado":
                        erro = false;
                        Toast.makeText(getContext(), R.string.endereco_atualizado_com_sucesso, Toast.LENGTH_LONG).show();
                        break;
                    case "falhou_inserir":
                        erro = true;
                        Toast.makeText(getContext(), R.string.falhou_ao_inserir_endereco, Toast.LENGTH_SHORT).show();
                        break;
                    case "falhouInserirPedido":
                        erro = true;
                        toggleBtnFinalizarVisibility(true);
                        Toast.makeText(getContext(), R.string.falhou_ao_processar_o_pedido, Toast.LENGTH_LONG).show();
                        break;
                    case "erro_sql":
                        erro = true;
                        Toast.makeText(getContext(), "Erro de SQL", Toast.LENGTH_SHORT).show();
                        break;
                    case "onPostExecute":
                        if (!erro) {
                            if (!finalizado) {
                                toggleButtonEditVisibility(true, false);
                                setInputsEnabled(false);
                                setInfosUserOnViews(endereco);
                                loginViewModel.getUsuarioMutableLiveData().setValue(usuario);
                            }
                        } else {
                            toggleButtonEditVisibility(false, false);
                            setInputsEnabled(true);
                        }
                        break;
                }
            }
        } else {
            Toast.makeText(getContext(), "Ocorreu um erro!", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleBtnFinalizarVisibility(boolean visibility) {
        final ViewGroup root = formaPagamentoEFreteDialog.findViewById(R.id.layout_dialog_pagamento_frete_root);
        if (root != null) {
            TransitionManager.beginDelayedTransition(root, new Fade());
            if (visibility) {
                btnFinalizarCompra.setVisibility(View.VISIBLE);
                progressBarFinaliza.setVisibility(View.INVISIBLE);
            } else {
                progressBarFinaliza.setVisibility(View.VISIBLE);
                btnFinalizarCompra.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void toggleBtnAvancarVisibility(boolean visibility) {
        final ViewGroup root = getActivity().findViewById(R.id.container_info_entrega);
        TransitionManager.beginDelayedTransition(root, new Fade());
        if (visibility) {
            if (btnAvancar.getVisibility() != View.VISIBLE)
                btnAvancar.setVisibility(View.VISIBLE);
            if (progressBarAvancarFinalizacaoCompra.getVisibility() != View.INVISIBLE)
                progressBarAvancarFinalizacaoCompra.setVisibility(View.INVISIBLE);
        } else {
            if (progressBarAvancarFinalizacaoCompra.getVisibility() != View.VISIBLE)
                progressBarAvancarFinalizacaoCompra.setVisibility(View.VISIBLE);
            if (btnAvancar.getVisibility() != View.INVISIBLE)
                btnAvancar.setVisibility(View.INVISIBLE);
        }
    }

    private void setInfosUserOnViews(Endereco endereco) {
        inputNomeEndereco.setText(endereco.getAddrName());
        inputRua.setText(endereco.getAddrStreet());
        inputComplemento.setText(endereco.getAddrComplement());
        inputNumero.setText(endereco.getAddrNumber());
        inputBairro.setText(endereco.getAddrDistrict());
        inputCEP.setText(endereco.getAddrZipCode());
        inputCidade.setText(endereco.getAddrCity());
        inputEstado.setText(endereco.getAddrState());
    }

    private void setInputsEnabled(boolean enabled) {
        inputRua.setEnabled(enabled);
        inputComplemento.setEnabled(enabled);
        inputNumero.setEnabled(enabled);
        inputBairro.setEnabled(enabled);
        inputCEP.setEnabled(enabled);
        inputCidade.setEnabled(enabled);
        if (enabled) {
            inputEstado.setEnabled(true);
            resetInputEstados();
        } else {
            inputEstado.setEnabled(false);
        }
        inputNomeEndereco.setEnabled(enabled);
    }

    private boolean isInputsEnabled() {
        return inputNomeEndereco.isEnabled();
    }

    private void clearInputs() {
        inputNomeEndereco.setText("");
        inputRua.setText("");
        inputNumero.setText("");
        inputComplemento.setText("");
        inputBairro.setText("");
        inputCEP.setText("");
        inputCidade.setText("");
        resetInputEstados();
    }

    private void resetInputEstados() {
        arrayEstadosBR = getResources().getStringArray(R.array.ufs);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_menu_popup_item, arrayEstadosBR);
        inputEstado.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        navController.navigate(R.id.action_finalizarCompraFragment_to_navigation_carrinho);
        return super.onOptionsItemSelected(item);
    }

    private void toggleVisibilityCtnrTroco(ViewGroup root, boolean containerVisibility) {
        if (root != null) {
            ConstraintLayout containerTroco = root.findViewById(R.id.container_troco);
            TransitionManager.beginDelayedTransition(root, new Fade().setDuration(500));
            if (containerVisibility) {
                containerTroco.setVisibility(View.VISIBLE);
            } else {
                containerTroco.setVisibility(View.GONE);
            }
        }
    }

    private void toggleSpinnerEnderecoVisibility(boolean spinnerVisibility) {
        final ViewGroup root = getActivity().findViewById(R.id.container_info_entrega);
        TransitionManager.beginDelayedTransition(root, new Fade());
        if (spinnerVisibility) {
            if (layoutInputNomeEndereco.getVisibility() != View.INVISIBLE)
                layoutInputNomeEndereco.setVisibility(View.INVISIBLE);
            if (layoutInputSpinnerEndereco.getVisibility() != View.VISIBLE)
                layoutInputSpinnerEndereco.setVisibility(View.VISIBLE);
        } else {
            if (layoutInputSpinnerEndereco.getVisibility() != View.INVISIBLE)
                layoutInputSpinnerEndereco.setVisibility(View.INVISIBLE);
            if (layoutInputNomeEndereco.getVisibility() != View.VISIBLE)
                layoutInputNomeEndereco.setVisibility(View.VISIBLE);

        }

    }

    private void toggleVisibilityInputLayoutTroco(ViewGroup root) {
        if (root != null) {
            TransitionManager.beginDelayedTransition(root, new Fade().setDuration(500));
            if (switchTroco.isChecked()) {
                if (layoutTroco.getVisibility() != View.VISIBLE)
                    layoutTroco.setVisibility(View.VISIBLE);
            } else {
                layoutTroco.setErrorEnabled(false);
                if (layoutTroco.getVisibility() != View.GONE)
                    layoutTroco.setVisibility(View.GONE);
            }
        }
    }

    private void toggleButtonEditVisibility(boolean isEditVisible, boolean isLoading) {
        if (getActivity() != null) {
            final ViewGroup root = getActivity().findViewById(R.id.container_info_entrega);
            if (root != null) {
                TransitionManager.beginDelayedTransition(root, new Fade());
                if (isLoading) {
                    btnEditInfos.setVisibility(View.INVISIBLE);
                    btnSaveInfos.setVisibility(View.INVISIBLE);
                    layoutInputNomeEndereco.setVisibility(View.INVISIBLE);
                    progressBarSaveAddr.setVisibility(View.VISIBLE);
                    layoutInputSpinnerEndereco.setVisibility(View.VISIBLE);
                } else {
                    progressBarSaveAddr.setVisibility(View.INVISIBLE);
                    if (isEditVisible) {
                        btnSaveInfos.setVisibility(View.INVISIBLE);
                        layoutInputNomeEndereco.setVisibility(View.INVISIBLE);
                        btnEditInfos.setVisibility(View.VISIBLE);
                        layoutInputSpinnerEndereco.setVisibility(View.VISIBLE);
                    } else {
                        btnEditInfos.setVisibility(View.INVISIBLE);
                        layoutInputSpinnerEndereco.setVisibility(View.INVISIBLE);
                        btnSaveInfos.setVisibility(View.VISIBLE);
                        layoutInputNomeEndereco.setVisibility(View.VISIBLE);

                    }
                }
            }
        }
    }

    private void toggleBtnAddEnderecoVisibility(boolean isVisible) {
        if (isVisible) {
            if (btnAddEndereco.getVisibility() != View.VISIBLE)
                btnAddEndereco.setVisibility(View.VISIBLE);
            if (btnClearAddEndereco.getVisibility() != View.INVISIBLE)
                btnClearAddEndereco.setVisibility(View.INVISIBLE);
        } else {
            if (btnClearAddEndereco.getVisibility() != View.VISIBLE)
                btnClearAddEndereco.setVisibility(View.VISIBLE);
            if (btnAddEndereco.getVisibility() != View.INVISIBLE)
                btnAddEndereco.setVisibility(View.INVISIBLE);
        }
    }

    private boolean validateFormaDePagamento(ViewGroup root) {
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
                                userChoose(root, true);
                            })
                            .setPositiveButton(R.string.informar_valor, (dialog, which) -> {
                                toggleVisibilityInputLayoutTroco(root);
                                inputTroco.requestFocus();
                                userChoose(root, false);
                            })
                            .setCancelable(false);
                    builder.show();
                } else {
                    //Troco
                    if (inputTroco.getText() != null) {
                        dinheiroDoCliente = inputTroco.getText().toString();
                        if (dinheiroDoCliente.isEmpty()) {
                            layoutTroco.setErrorEnabled(true);
                            layoutTroco.setError("preencha um valor");
                        } else if (fromRStoDouble(dinheiroDoCliente) < fromRStoDouble(subtotal)) {
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

    private boolean validateFrete() {
        if (formaFrete == 0) {
            ((RadioButton) freteRadioGroup.getChildAt(0)).setError("Escolha uma opção");
            return false;
        } else {
            return true;
        }
    }

    private boolean validateFieldsEntrega() {
        //Nome endereco
        if (inputNomeEndereco.getText() != null) {
            nomeEndereco = inputNomeEndereco.getText().toString();
            if (nomeEndereco.isEmpty()) {
                layoutInputNomeEndereco.setErrorEnabled(true);
                layoutInputNomeEndereco.setError(" ");
            } else {
                layoutInputNomeEndereco.setErrorEnabled(false);
            }
        } else {
            layoutInputNomeEndereco.setErrorEnabled(true);
            Toast.makeText(getContext(), "Endereço nulo!", Toast.LENGTH_SHORT).show();
        }

        //Endereco
        if (inputRua.getText() != null) {
            rua = inputRua.getText().toString();
            if (rua.isEmpty()) {
                layoutEndereco.setErrorEnabled(true);
                layoutEndereco.setError(" ");
            } else {
                layoutEndereco.setErrorEnabled(false);
            }
        } else {
            layoutEndereco.setErrorEnabled(true);
            Toast.makeText(getContext(), "Endereço nulo!", Toast.LENGTH_SHORT).show();
        }

        //Complemento
        if (inputComplemento.getText() != null) {
            complemento = inputComplemento.getText().toString();
        } else {
            Toast.makeText(getContext(), "Complemento nulo!", Toast.LENGTH_SHORT).show();
        }

        //Numero
        if (inputNumero.getText() != null) {
            numero = inputNumero.getText().toString();
            if (numero.isEmpty()) {
                layoutNumero.setErrorEnabled(true);
                layoutNumero.setError(" ");
            } else {
                layoutNumero.setErrorEnabled(false);
            }
        } else {
            layoutNumero.setErrorEnabled(true);
            Toast.makeText(getContext(), "Numero nulo!", Toast.LENGTH_SHORT).show();
        }

        //Bairro
        if (inputBairro.getText() != null) {
            bairro = inputBairro.getText().toString();
            if (bairro.isEmpty()) {
                layoutBairro.setErrorEnabled(true);
                layoutBairro.setError(" ");
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
                layoutCEP.setError(" ");
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
                layoutCidade.setError(" ");
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
                layoutEstado.setError(" ");
            } else {
                layoutEstado.setErrorEnabled(false);
            }
        } else {
            layoutEstado.setErrorEnabled(true);
            Toast.makeText(getContext(), "Estado nulo!", Toast.LENGTH_SHORT).show();
        }

        if (layoutInputNomeEndereco.isErrorEnabled() ||
                layoutNumero.isErrorEnabled() ||
                layoutCEP.isErrorEnabled() ||
                layoutBairro.isErrorEnabled() ||
                layoutCidade.isErrorEnabled() ||
                layoutEstado.isErrorEnabled()
        ) {
            return false;
        } else {
            return true;
        }
    }

    public void userChoose(ViewGroup root, boolean quantiaExata) {
        if (quantiaExata) {
            toggleBtnFinalizarVisibility(false);
            insereCompraNoBanco();
        } else {
            switchTroco.setChecked(true);
            toggleVisibilityInputLayoutTroco(root);
        }
    }

    private void initViews(View view) {
        inputRua = view.findViewById(R.id.inputEndereco);
        inputNumero = view.findViewById(R.id.inputNumero);
        inputCEP = view.findViewById(R.id.inputCEP);
        inputBairro = view.findViewById(R.id.inputBairro);
        inputCidade = view.findViewById(R.id.inputCidade);
        inputEstado = view.findViewById(R.id.inputUF);
        inputComplemento = view.findViewById(R.id.inputComplemento);

        layoutEndereco = view.findViewById(R.id.layoutInputEndereco);
        layoutNumero = view.findViewById(R.id.layoutInputNumero);
        layoutCEP = view.findViewById(R.id.layoutInputCEP);
        layoutBairro = view.findViewById(R.id.layoutInputBairro);
        layoutCidade = view.findViewById(R.id.layoutInputCidade);
        layoutEstado = view.findViewById(R.id.layoutInputUF);


        layoutInputSpinnerEndereco = view.findViewById(R.id.layoutInputSpinnerEndereco);
        inputSpinnerEndereco = view.findViewById(R.id.inputSpinnerEndereco);

        layoutInputNomeEndereco = view.findViewById(R.id.layoutInputNomeEndereco);
        inputNomeEndereco = view.findViewById(R.id.inputNomeEndereco);

        btnAddEndereco = view.findViewById(R.id.btnAddEndereco);
        btnAddEndereco.setOnClickListener(v -> {
            toggleSpinnerEnderecoVisibility(false);
            toggleBtnAddEnderecoVisibility(false);
            toggleButtonEditVisibility(false, false);
            clearInputs();
            setInputsEnabled(true);
            if (endereco != null)
                endereco = null;
        });

        btnClearAddEndereco = view.findViewById(R.id.btnClearAddEndereco);
        btnClearAddEndereco.setOnClickListener(v -> {
            toggleSpinnerEnderecoVisibility(true);
            toggleBtnAddEnderecoVisibility(true);
            inputSpinnerEndereco.setListSelection(usuario.getEnderecos().size() - 1);
            endereco = usuario.getEnderecos().get(usuario.getEnderecos().size() - 1);
            setInfosUserOnViews(usuario.getEnderecos().get(usuario.getEnderecos().size() - 1));
            setInputsEnabled(false);
            toggleButtonEditVisibility(true, false);
        });


        btnSaveInfos = view.findViewById(R.id.button_save_infos);
        btnEditInfos = view.findViewById(R.id.button_edit_infos);
        progressBarSaveAddr = view.findViewById(R.id.progressBarSaveAddress);
        progressBarAvancarFinalizacaoCompra = view.findViewById(R.id.progressBarAvancarFinalizacaoCompra);

        btnAvancar = view.findViewById(R.id.btnAvancarFinalizacaoCompra);
        btnAvancar.setOnClickListener(v -> {
            avancaFinalizacaoCompra();
        });

        if (getActivity() != null) {
            arrayEstadosBR = getResources().getStringArray(R.array.ufs);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_menu_popup_item, arrayEstadosBR);
            inputEstado.setAdapter(adapter);
        } else {
            Log.e(MY_LOG_TAG, "Atividade Nula!");
        }

        cepTextWatcher = new CepTextWatcher(inputCEP);
        inputCEP.addTextChangedListener(cepTextWatcher);

        btnEditInfos.setOnClickListener(v -> {
            toggleSpinnerEnderecoVisibility(true);
            toggleButtonEditVisibility(false, false);
            if (!isInputsEnabled())
                setInputsEnabled(true);

        });
        btnSaveInfos.setOnClickListener(v -> {
            if (validateFieldsEntrega()) {
                Endereco newEndereco = new Endereco();
                if (layoutInputSpinnerEndereco.getVisibility() != View.VISIBLE) {
                    newEndereco.setAddrName(nomeEndereco);
                    //Está editando não ADD
                    if (endereco != null) {
                        newEndereco.setAddrID(endereco.getAddrID());
                    }
                } else {
                    //Add ou Cancelou Add
                    newEndereco.setAddrName(endereco.getAddrName());
                    newEndereco.setAddrID(endereco.getAddrID());
                }
                newEndereco.setAddrZipCode(cep);
                newEndereco.setAddrStreet(rua);
                newEndereco.setAddrNumber(numero);
                newEndereco.setAddrComplement(complemento);
                newEndereco.setAddrDistrict(bairro);
                newEndereco.setAddrCity(cidade);
                newEndereco.setAddrState(estado);
                newEndereco.setAddrCountry("Brasil");

                if (endereco != null) {
                    for (int i = 0; i < usuario.getEnderecos().size(); i++) {
                        if (usuario.getEnderecos().get(i).getAddrID() == endereco.getAddrID()) {
                            usuario.getEnderecos().set(i, newEndereco);
                        }
                    }
                }
                endereco = newEndereco;

                insertOrUpdateEnderecoCliente();
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                updateUI(msg);
            }
        };
    }

    private void avancaFinalizacaoCompra() {
        if (validateFieldsEntrega()) {
            toggleBtnAvancarVisibility(false);
            DataBaseConnection.ValidateCEPBH validateCEPBH = new DataBaseConnection.ValidateCEPBH(this);
            validateCEPBH.execute(cep);
        } else {
            Toast.makeText(getContext(), "Confira as informações", Toast.LENGTH_SHORT).show();
        }
    }

    private void insereCompraNoBanco() {
        ArrayList<String> infosCompra = new ArrayList<>();
        ArrayList<Produto> produtos = carrinhoViewModel.getCartProductsLiveData().getValue();
        infosCompra.add(String.valueOf(usuario.getId())); //userid
        infosCompra.add(String.valueOf(orderPayment));//formapagamento
        String valorCompra = subtotal.replace("R$ ", "").replace("R$", "").replace(",", ".");
        infosCompra.add(valorCompra);//valorcompra
        //dinheiro
//        if(orderPayment == 103) {
        infosCompra.add("1");
        infosCompra.add(valorCompra); //instalment [valor da parcela - 1 parcela]
//        } else {
//            infosCompra.add("1");
//        } //cartao

        infosCompra.add(String.valueOf(endereco.getAddrID()));//idendereco
        infosCompra.add(String.valueOf(formaFrete));
        if (switchTroco.isChecked()) {
            String valorDoTroco = dinheiroDoCliente.replace("R$ ", "").replace("R$", "").replace(",", ".");
            infosCompra.add(valorDoTroco); //troco
        } else {
            infosCompra.add("0.00"); //troco
        }

        /**seguinte a coluna order_installments se for dinheiro ela recebe 1 e
         se for cartao ela recebe o numero de paracelas que foi
         divido a compra ou 1 pra parcela unica. e o order_instalment
         recebe o valor de cada prestação ou o valor total se for pagamento unico!
         **/

        DataBaseConnection.InsertPurchase insertPurchase = new DataBaseConnection.InsertPurchase(handler, infosCompra, produtos);
        insertPurchase.execute();
    }

    private void navigateToAcompanhamento() {
        Bundle bundle = new Bundle();
        Gson g = new Gson();
        String enderecoJSON = g.toJson(endereco);
        bundle.putString("enderecoJson", enderecoJSON);
        bundle.putString("formaPagamentoValue", labelFormaPagamento);
        bundle.putString("subtotalValue", subtotal);
        bundle.putString("idPedido", idPedido);
        if (switchTroco.isChecked()) {
            bundle.putString("trocoParaQuantoValue", dinheiroDoCliente);
            String valorDoTroco = String.valueOf(fromRStoDouble(dinheiroDoCliente) - fromRStoDouble(subtotal)).replace("R$ ", "").replace(".", ",");
            bundle.putString("valorDoTroco", valorDoTroco);
        } else {
            bundle.putString("trocoParaQuantoValue", null);
            bundle.putString("valorDoTroco", "0,00");
        }
        navController.navigate(R.id.action_finalizarCompraFragment_to_acompanhamentoCompraFragment, bundle);
    }

    public void openDialogPagamentoFrete() {
        // create an alert builder
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_Rounded);
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.layout_dialog_pagamento_frete, null);
        final ViewGroup root = customLayout.findViewById(R.id.scrollViewCntrFinalizar);

        btnFinalizarCompra = customLayout.findViewById(R.id.btnFinalizarCompra);
        progressBarFinaliza = customLayout.findViewById(R.id.progressBarFinaliza);

        formaPagamentoRadioGroup = customLayout.findViewById(R.id.formaDePagamentoRadioGroup);
        freteRadioGroup = customLayout.findViewById(R.id.freteRadioGroup);
        AppCompatButton btnFinalizarCompra = customLayout.findViewById(R.id.btnFinalizarCompra);
        radioButtonCartaoID = customLayout.findViewById(R.id.radioBtnCartao).getId();
        radioButtonDinheiroID = customLayout.findViewById(R.id.radioBtnDinheiro).getId();
        switchTroco = customLayout.findViewById(R.id.switchCompat);
        inputTroco = customLayout.findViewById(R.id.inputTroco);
        layoutTroco = customLayout.findViewById(R.id.layoutInputTroco);
        MoneyTextWatcher moneyTextWatcher = new MoneyTextWatcher(inputTroco);
        inputTroco.addTextChangedListener(moneyTextWatcher);
        formaPagamentoRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            ((RadioButton) formaPagamentoRadioGroup.getChildAt(0)).setError(null);
            if (checkedId == group.findViewById(R.id.radioBtnDinheiro).getId()) {
                labelFormaPagamento = "Dinheiro";
                orderPayment = 103;
                toggleVisibilityCtnrTroco(root, true);
            } else {
                labelFormaPagamento = "Cartão (Máquina)";
                orderPayment = 104;
                toggleVisibilityCtnrTroco(root, false);
            }
        });

        freteRadioGroup.setOnCheckedChangeListener(((group, checkedId) -> {
            ((RadioButton) freteRadioGroup.getChildAt(0)).setError(null);
            if (checkedId == group.findViewById(R.id.radioBtn1DiaUtil).getId()) {
                formaFrete = 10002;
            } else {
                formaFrete = 10003;
            }
        }));

        switchTroco.setOnClickListener(v -> toggleVisibilityInputLayoutTroco(root));
        btnFinalizarCompra.setOnClickListener(v -> {
            if (validateFormaDePagamento(root) && validateFrete()) {
                toggleBtnFinalizarVisibility(false);
                insereCompraNoBanco();
            }
        });
        builder.setView(customLayout);
        formaPagamentoEFreteDialog = builder.create();
        if (!formaPagamentoEFreteDialog.isShowing()) {
            formaPagamentoEFreteDialog.show();
        }
    }

    @Override
    public void onCEPResult(String cidade) {
        if (cidade.equals("Belo Horizonte")) {
            openDialogPagamentoFrete();
        } else {
            Toast.makeText(getContext(), "CEP Fora de Belo Horizonte!", Toast.LENGTH_SHORT).show();
        }
        toggleBtnAvancarVisibility(true);
    }
}
