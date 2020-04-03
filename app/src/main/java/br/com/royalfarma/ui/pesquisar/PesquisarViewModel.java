package br.com.royalfarma.ui.pesquisar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PesquisarViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PesquisarViewModel() {
        mText = new MutableLiveData<String>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}