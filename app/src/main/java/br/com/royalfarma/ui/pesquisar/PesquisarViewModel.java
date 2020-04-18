package br.com.royalfarma.ui.pesquisar;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import br.com.royalfarma.model.Produto;

public class PesquisarViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<Produto>> allSearchedProductsLiveData;
    private MutableLiveData<String> queryLiveData;

    public PesquisarViewModel() {
        queryLiveData = new MutableLiveData<>();
        allSearchedProductsLiveData = new MutableLiveData<>();
        setNewQuery("");
        updateProductsList(new ArrayList<>());
    }

    public MutableLiveData<String> getQueryLiveData() {
        return queryLiveData;
    }

    public MutableLiveData<ArrayList<Produto>> getAllSearchedProductsLiveData() {
        return allSearchedProductsLiveData;
    }

    public void setNewQuery(String query) {
        queryLiveData.setValue(query);
    }

    public void updateProductsList(ArrayList<Produto> newList) {
        allSearchedProductsLiveData.setValue(newList);
    }
}