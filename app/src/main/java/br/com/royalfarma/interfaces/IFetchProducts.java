package br.com.royalfarma.interfaces;

import java.util.ArrayList;

import br.com.royalfarma.model.Produto;

public interface IFetchProducts {
    public void onGetDataDone(ArrayList<Produto> listaDeProdutos);
}
