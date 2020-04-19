package br.com.royalfarma.ui.listagem;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import br.com.royalfarma.model.Produto;

public class ListaDeProdutosViewModel extends ViewModel {
    private final SavedStateHandle listaDeProdutosState;
    private MutableLiveData<String> pageTitle;
    private MutableLiveData<ArrayList<Produto>> produtosDaLista;

    public ListaDeProdutosViewModel(SavedStateHandle savedStateHandle) {
        listaDeProdutosState = savedStateHandle;
        pageTitle = new MutableLiveData<>();
        produtosDaLista = new MutableLiveData<>();
    }


    public MutableLiveData<ArrayList<Produto>> getListaDeProdutos() {
        return produtosDaLista;
    }

    public void setListaDeProdutos(ArrayList<Produto> produtos) {
        produtosDaLista.setValue(produtos);
    }
}
