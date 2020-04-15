package br.com.royalfarma.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import br.com.royalfarma.model.Produto;

public class ProductsViewModel extends ViewModel {
    private final MutableLiveData<Produto> produtoLiveData;
    private MutableLiveData<ArrayList<Produto>> todosOsProdutosLiveData;
    private ArrayList<Produto> todosOsProdutos;

    public ProductsViewModel(SavedStateHandle savedStateHandle) {
//        homeState = savedStateHandle;
        todosOsProdutosLiveData = new MutableLiveData<>();
        todosOsProdutos = new ArrayList<>();
        produtoLiveData = new MutableLiveData<>();
        todosOsProdutosLiveData.setValue(todosOsProdutos);
    }

    public LiveData<ArrayList<Produto>> getTodosOsProdutosLiveData() {
        return todosOsProdutosLiveData;
    }

    public void setTodosOsProdutos(ArrayList<Produto> todosOsProdutos) {
        todosOsProdutosLiveData.setValue(todosOsProdutos);
    }

    public LiveData<Produto> getProductLiveData() {
        return produtoLiveData;
    }

    public void updateProduct(Integer updatePosition) {
        produtoLiveData.setValue(todosOsProdutosLiveData.getValue().get(updatePosition));
    }
}