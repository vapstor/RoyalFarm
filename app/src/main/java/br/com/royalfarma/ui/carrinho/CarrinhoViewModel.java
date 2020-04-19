package br.com.royalfarma.ui.carrinho;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.math.BigDecimal;
import java.util.ArrayList;

import br.com.royalfarma.model.Produto;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.RSmask;

public class CarrinhoViewModel extends ViewModel {

    private int badgeNumber;
    private String subtotal;
    private MutableLiveData<String> subTotalLiveData;
    private MutableLiveData<Integer> badgeDisplayLiveData;
    private MutableLiveData<Integer> produtoLiveData;
    private MutableLiveData<ArrayList<Produto>> cartProductsLiveData;
    private ArrayList<Produto> cartProducts;

    public CarrinhoViewModel() {
        cartProductsLiveData = new MutableLiveData<>();
        produtoLiveData = new MutableLiveData<>();
        badgeDisplayLiveData = new MutableLiveData<>();
        subTotalLiveData = new MutableLiveData<>();
        cartProducts = new ArrayList<>();
        cartProductsLiveData.setValue(cartProducts);
        subtotal = "R$ 0,00";
        subTotalLiveData.setValue(subtotal);
        badgeNumber = 0;
        badgeDisplayLiveData.setValue(badgeNumber);
    }


    public LiveData<ArrayList<Produto>> getCartProductsLiveData() {
        return cartProductsLiveData;
    }

    public LiveData<String> getSubtotalLiveData() {
        return subTotalLiveData;
    }

    public LiveData<Integer> getProdutoLiveData() {
        return produtoLiveData;
    }

    public LiveData<Integer> getBadgeDisplayLiveData() {
        return badgeDisplayLiveData;
    }


    public void addProductToCart(Produto produtoAdd) {
        boolean atualizado = false;
        ArrayList<Produto> cartProducts = cartProductsLiveData.getValue();
        if (cartProducts != null) {
            if (cartProducts.size() > 0) {
                Produto produtoNoCart;
                for (int i = 0; i < cartProducts.size(); i++) {
                    produtoNoCart = cartProducts.get(i);
                    //checa se já existe
                    if (produtoNoCart.getId() == produtoAdd.getId()) {
                        if (produtoAdd.getQtdNoCarrinho() > produtoNoCart.getEstoqueAtual()) {
                            produtoAdd.setQtdNoCarrinho(produtoNoCart.getEstoqueAtual());
                            cartProducts.set(i, produtoAdd);
                        } else {
                            Log.d(MY_LOG_TAG, "produtoNoCart.getQtdNoCarrinho() : " + produtoNoCart.getQtdNoCarrinho());
                            Log.d(MY_LOG_TAG, "produtoAdd.getQtdNoCarrinho() : " + produtoNoCart.getQtdNoCarrinho());
                            produtoNoCart.setQtdNoCarrinho(produtoAdd.getQtdNoCarrinho());
                            cartProducts.set(i, produtoNoCart);
                        }
                        atualizado = true;
                        break;
                    }
                }
                if (!atualizado) {
                    cartProducts.add(produtoAdd);
                }
                cartProductsLiveData.setValue(cartProducts);
                updateSubtotal();
                updateBadgeDisplay();
            } else {
                cartProducts.add(produtoAdd);
                cartProductsLiveData.setValue(cartProducts);
                updateSubtotal();
                updateBadgeDisplay();
            }
        } else {
            Log.d(MY_LOG_TAG, "addProductToCart Error cartProducts == NULL");
        }
    }

    public void updateSubtotal() {
        subTotalLiveData.setValue("Calculando");
        BigDecimal newSubtotal = new BigDecimal(0.00);
        ArrayList<Produto> cartProducts = cartProductsLiveData.getValue();
        if (cartProducts != null) {
            for (Produto cartProduct : cartProducts) {
                if (cartProduct.isDesconto()) {
                    newSubtotal = newSubtotal.add(new BigDecimal(cartProduct.getQtdNoCarrinho() * cartProduct.getPrecoOferta()));
                } else {
                    newSubtotal = newSubtotal.add(new BigDecimal(cartProduct.getQtdNoCarrinho() * cartProduct.getPreco()));
                }
            }
        }
        this.subtotal = RSmask(newSubtotal);
        subTotalLiveData.setValue(this.subtotal);
    }

    //    public void updateProductOnCartList(int i) {
    public void updateProductOnCartList(Produto produto) {
        ArrayList<Produto> cartProducts = cartProductsLiveData.getValue();
        if (cartProducts != null) {
            for (int i = 0; i < cartProducts.size(); i++) {
                if (cartProducts.get(i).getId() == produto.getId()) {
                    //Confere quantidade de itens do carrinho
                    if (produto.getQtdNoCarrinho() == 0) {
                        cartProducts.remove(i);
                        updateProductsList(cartProducts);
                    } else {
                        cartProducts.set(i, produto);
                        produtoLiveData.setValue(i);
                        updateProductsList(cartProducts);
                    }
                } else {
                    Log.e(MY_LOG_TAG, "ERRO: Produto não está na lista!");
                }
            }
            updateSubtotal();
            updateBadgeDisplay();
        } else {
            Log.d(MY_LOG_TAG, "removeProductFromCartList Error cartProducts == NULL");
        }
    }

    public void updateProductsList(ArrayList<Produto> newProducts) {
        cartProductsLiveData.setValue(newProducts);
    }

    public void updateBadgeDisplay() {
        int badgeValue = 0;
        if (getCartProductsLiveData().getValue() != null) {
            for (Produto p : getCartProductsLiveData().getValue()) {
                badgeValue = badgeValue + p.getQtdNoCarrinho();
            }
        }
        badgeDisplayLiveData.setValue(badgeValue);
    }

    private double formatSubtotalString4Double(String subtotal) {
        return Double.parseDouble((subtotal).replace("-", "").replace("R$", "").replace(".", "").replace(",", "").trim()) / 100;
    }

}