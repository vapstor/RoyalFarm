package br.com.royalfarma.ui.listaDeProdutos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class ListaDeProdutosViewModel extends ViewModel {
    private final SavedStateHandle listaDeProdutosState;
    private MutableLiveData<String> pageTitle;

    public ListaDeProdutosViewModel(SavedStateHandle savedStateHandle) {
        listaDeProdutosState = savedStateHandle;
        pageTitle = new MutableLiveData<>();
    }

    public void setPageTitle(String title) {
        pageTitle.setValue(title);
    }

    public LiveData<String> getPageTitle() {
        return pageTitle;
    }
}
