package br.com.royalfarma.interfaces;

import java.util.ArrayList;

import br.com.royalfarma.model.Produto;

public interface IFetchHomeProducts {
    public void updateHomeWithData(ArrayList<ArrayList<Produto>> listaDeProdutos);
}
