package br.com.royalfarma.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import br.com.royalfarma.model.Produto;

public class HomeViewModel extends ViewModel {
    private final SavedStateHandle homeState;
//    private final String PRODUTOS_POPULARES_LIVE_DATA = "PRODUTOS_POPULARES_LIVE_DATA";
//    private final String PRODUTOS_NOVIDADES_LIVE_DATA = "PRODUTOS_NOVIDADES_LIVE_DATA";
//    private final String PRODUTOS_MAIS_VENDIDOS_LIVE_DATA = "PRODUTOS_MAIS_VENDIDOS_LIVE_DATA";

    private MutableLiveData<ArrayList<Produto>> produtosPopularesLiveData, produtosNovidadesLiveData, produtosMaisVendidosLiveData;
    private ArrayList<Produto> produtosPopulares, produtosNovidades, produtosMaisVendidos;

    public HomeViewModel(SavedStateHandle savedStateHandle) {
        homeState = savedStateHandle;
        produtosPopularesLiveData = new MutableLiveData<>();
        produtosNovidadesLiveData = new MutableLiveData<>();
        produtosMaisVendidosLiveData = new MutableLiveData<>();
        init();
    }


    private void init() {
        createProdutosPopularesList();
        createProdutosNovidadesList();
        createProdutosMaisVendidosList();
        produtosPopularesLiveData.setValue(produtosPopulares);
        produtosNovidadesLiveData.setValue(produtosNovidades);
        produtosMaisVendidosLiveData.setValue(produtosMaisVendidos);
    }

    private void createProdutosNovidadesList() {
        produtosNovidades = new ArrayList<>();
//        Produto produto = new Produto("null_path", 0, "Vacina Corona 1", 10.25, 123456, 100);
//        produtosNovidades.add(produto);
    }

    private void createProdutosPopularesList() {
        produtosPopulares = new ArrayList<>();
    }

    private void createProdutosMaisVendidosList() {
        produtosMaisVendidos = new ArrayList<>();
//        Produto produtoMaisVendido = new Produto("null_path", 0, "Vacina Corona 5", 10.25, 123456, 100);
//        produtosMaisVendidos.add(produtoMaisVendido);
    }

    public LiveData<ArrayList<Produto>> getProdutosPopularesLiveData() {
        return produtosPopularesLiveData;
    }

    public LiveData<ArrayList<Produto>> getProdutosNovidadesLiveData() {
        return produtosNovidadesLiveData;
    }

    public LiveData<ArrayList<Produto>> getProdutosMaisVendidosLiveData() {
        return produtosMaisVendidosLiveData;
    }

    public void setProdutosPopulares(ArrayList<Produto> produtosPopulares) {
        produtosPopularesLiveData.setValue(produtosPopulares);
    }

    public void setProdutosNovidades(ArrayList<Produto> produtosNovidades) {
        produtosNovidadesLiveData.setValue(produtosNovidades);
    }

    public void setProdutosMaisVendidos(ArrayList<Produto> produtosMaisVendidos) {
        produtosMaisVendidosLiveData.setValue(produtosMaisVendidos);
    }
}