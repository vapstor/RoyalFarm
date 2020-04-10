package br.com.royalfarma.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Produto implements Parcelable {

    public String imagemURL;
    public String tituloProduto;
    public double preco;
    public int id, quantidade;
    public long codBarra;
    public double totalPrecoItem;
    public int estoqueAtual;

    protected Produto(Parcel in) {
        imagemURL = in.readString();
        tituloProduto = in.readString();
        preco = in.readDouble();
        id = in.readInt();
        quantidade = in.readInt();
        codBarra = in.readLong();
        totalPrecoItem = in.readDouble();
        estoqueAtual = in.readInt();
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

    public double getTotalPrecoItem() {
        return totalPrecoItem;
    }

    public void setTotalPrecoItem(double totalPrecoItem) {
        this.totalPrecoItem = totalPrecoItem;
    }

    public Produto(String pathToImage, int id, String titulo, int quantidade, double valor, long codBarra, int estoqueAtual) {
        this.imagemURL = pathToImage;
        this.id = id;
        this.tituloProduto = titulo;
        this.preco = valor;
        this.quantidade = quantidade;
        this.codBarra = codBarra;
        this.estoqueAtual = estoqueAtual;
    }

    public Produto(String pathToImage, int id, String titulo, double valor, long codBarra, int estoqueAtual) {
        this.imagemURL = pathToImage;
        this.id = id;
        this.tituloProduto = titulo;
        this.preco = valor;
        this.codBarra = codBarra;
        this.estoqueAtual = estoqueAtual;
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

    public String getJsonObject() {
        return "{id:" + id + ",nome:" + tituloProduto + ",preco:" + preco + ",quantidade:" + quantidade + "}";
    }

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
        dest.writeDouble(totalPrecoItem);
        dest.writeInt(estoqueAtual);
    }
}
