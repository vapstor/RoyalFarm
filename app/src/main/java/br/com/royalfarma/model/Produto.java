package br.com.royalfarma.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Produto implements Parcelable {

    private String imagemURL;
    private String tituloProduto;
    private double preco;
    private int id, quantidade;
    private long codBarra;
    private int estoqueAtual;
    private boolean desconto;
    private double precoOferta;
    private String inicioOferta, fimOferta;
    private String valorTotal;
    private int qtdNoCarrinho;

    public Produto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int ItemId) {
        this.id = ItemId;
    }

    public String getNome() {
        return tituloProduto;
    }

    public void setNome(String ItemName) {
        this.tituloProduto = ItemName;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(int qntd) {
        quantidade = qntd;
    }

    public String getImagemURL() {
        return imagemURL;
    }

    public void setImagemURL(String imagemURL) {
        this.imagemURL = imagemURL;
    }

//    public String getJsonObject() {
//        return "{id:" + id + ",nome:" + tituloProduto + ",preco:" + preco + ",quantidade:" + quantidade + "}";
//    }

    public long getCodBarra() {
        return codBarra;
    }

    public void setCodBarra(long codBarra) {
        this.codBarra = codBarra;
    }

    public void setEstoqueAtual(int estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    public int getEstoqueAtual() {
        return estoqueAtual;
    }

    public boolean isDesconto() {
        return desconto;
    }

    public void setDesconto(boolean desconto) {
        this.desconto = desconto;
    }

    public String getFimOferta() {
        return fimOferta;
    }

    public void setFimOferta(String fimOferta) {
        this.fimOferta = fimOferta;
    }

    public double getPrecoOferta() {
        return precoOferta;
    }

    public void setPrecoOferta(double precoOferta) {
        this.precoOferta = precoOferta;
    }

    public String getInicioOferta() {
        return inicioOferta;
    }

    public void setInicioOferta(String inicioOferta) {
        this.inicioOferta = inicioOferta;
    }

    public String getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(String valorTotal) {
        this.valorTotal = valorTotal;
    }

    public int getQtdNoCarrinho() {
        return qtdNoCarrinho;
    }

    public void setQtdNoCarrinho(int qtdNoCarrinho) {
        this.qtdNoCarrinho = qtdNoCarrinho;
    }


    /**
     * PARCELABLE
     **/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imagemURL);
        dest.writeString(tituloProduto);
        dest.writeDouble(preco);
        dest.writeInt(id);
        dest.writeInt(quantidade);
        dest.writeLong(codBarra);
        dest.writeInt(estoqueAtual);
        dest.writeString(valorTotal);
        dest.writeInt(qtdNoCarrinho);
    }

    public static final Creator<Produto> CREATOR = new Creator<Produto>() {
        @Override
        public Produto createFromParcel(Parcel in) {
            return new Produto(in);
        }

        @Override
        public Produto[] newArray(int size) {
            return new Produto[size];
        }
    };

    private Produto(Parcel in) {
        imagemURL = in.readString();
        tituloProduto = in.readString();
        preco = in.readDouble();
        id = in.readInt();
        quantidade = in.readInt();
        codBarra = in.readLong();
        estoqueAtual = in.readInt();
        valorTotal = in.readString();
        qtdNoCarrinho = in.readInt();
    }
}
