package br.com.royalfarma.ui.carrinho;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import br.com.royalfarma.model.Produto;

import static br.com.royalfarma.utils.Util.MY_LOG_TAG;
import static br.com.royalfarma.utils.Util.RSmask;

public class CarrinhoViewModel extends ViewModel {

    private MutableLiveData<Integer> valorTotalProdutoLiveData;
    private String subtotal;
    private MutableLiveData<String> subTotalLiveData;
    private MutableLiveData<Integer> produtoLiveData;
    private MutableLiveData<ArrayList<Produto>> cartProductsLiveData;
    private ArrayList<Produto> cartProducts;

    public CarrinhoViewModel() {
        cartProductsLiveData = new MutableLiveData<>();
        produtoLiveData = new MutableLiveData<>();
        subTotalLiveData = new MutableLiveData<>();
        cartProducts = new ArrayList<>();
        cartProductsLiveData.setValue(cartProducts);
        subtotal = "R$ 0,00";
        subTotalLiveData.setValue(subtotal);
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

    public void addProductToCart(Produto produto) {
        boolean jaExisteNaLista = false;
        ArrayList<Produto> cartProducts = cartProductsLiveData.getValue();
        if (cartProducts != null) {
            for (Produto p : cartProducts) {
                if (p.getId() == produto.getId()) {
                    jaExisteNaLista = true;
                }
            }
            if (jaExisteNaLista) {
                updateItemCartQuantity(produto);
            } else {
                cartProducts.add(produto);
                cartProductsLiveData.setValue(cartProducts);
                if (produto.isDesconto()) {
                    double novoValorCompra = formatSubtotalString4Double(this.subtotal) + produto.getPrecoOferta();
                    updateSubtotal(novoValorCompra);
                } else {
                    updateSubtotal(formatSubtotalString4Double(this.subtotal) + produto.getPreco());
                }
            }
        } else {
            Log.d(MY_LOG_TAG, "addProductToCart Error cartProducts == NULL");
        }
    }

    public void updateSubtotal(double newSubtotal) {
        this.subtotal = RSmask(newSubtotal);
        subTotalLiveData.setValue(this.subtotal);
    }

    public void updateProduct(int positionUpdated) {
        produtoLiveData.setValue(positionUpdated);
    }

    public void updateItemCartQuantity(Produto item) {
        ArrayList<Produto> cartProducts = cartProductsLiveData.getValue();
        if (cartProducts != null) {
            for (int i = 0; i < cartProducts.size(); i++) {
                Produto produto = cartProducts.get(i);
                if (produto.getId() == item.getId()) {
                    if (item.getQuantidade() == 0) {
                        if (item.isDesconto()) {
                            updateSubtotal(formatSubtotalString4Double(this.subtotal) - cartProducts.get(i).getPrecoOferta());
                        } else {
                            updateSubtotal(formatSubtotalString4Double(this.subtotal) - cartProducts.get(i).getPreco());
                        }
                        removeProductFromCartList(i);
                    } else {
                        cartProducts.get(i).setQuantidade(item.getQuantidade());
                        if (item.isDesconto()) {
                            cartProducts.get(i).setValorTotal(RSmask(item.getQuantidade() * item.getPrecoOferta()));
                            updateSubtotal(formatSubtotalString4Double(this.subtotal) + cartProducts.get(i).getPrecoOferta());
                        } else {
                            cartProducts.get(i).setValorTotal(RSmask(item.getQuantidade() * item.getPreco()));
                            updateSubtotal(formatSubtotalString4Double(this.subtotal) + cartProducts.get(i).getPreco());
                        }
                        updateProduct(i);
                    }
                    return;
                }
            }
        } else {
            Log.d(MY_LOG_TAG, "updateItemCartQuantity Error cartProducts == NULL");
        }
    }

    private void removeProductFromCartList(int i) {
        ArrayList<Produto> cartProducts = cartProductsLiveData.getValue();
        if (cartProducts != null) {
            cartProducts.remove(i);
            cartProductsLiveData.setValue(cartProducts);
        } else {
            Log.d(MY_LOG_TAG, "removeProductFromCartList Error cartProducts == NULL");
        }

    }


    private double formatSubtotalString4Double(String subtotal) {
        return Double.parseDouble((subtotal).replace("-", "").replace("R$", "").replace(".", "").replace(",", "").trim()) / 100;
    }
}