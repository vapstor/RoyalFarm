package br.com.royalfarma.ui.carrinho;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CarrinhoViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CarrinhoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}