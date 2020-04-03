package br.com.royalfarma.model;

import java.io.Serializable;

public class Produto implements Serializable {

    public int imagemURL;
    public String tituloProduto;
    public double preco;
    public int id, quantidade;
    public long codBarra;
    public double totalPrecoItem;
    private int estoqueAtual;

    public Produto() {

    }

    public double getTotalPrecoItem() {
        return totalPrecoItem;
    }

    public void setTotalPrecoItem(double totalPrecoItem) {
        this.totalPrecoItem = totalPrecoItem;
    }

    public Produto(int id, String titulo, int quantidade, double valor, long codBarra) {
        this.id = id;
        this.tituloProduto = titulo;
        this.preco = valor;
        this.quantidade = quantidade;
        this.codBarra = codBarra;
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

    public int getImagemURL() {
        return imagemURL;
    }

    public void setImagemURL(int imagemURL) {
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
}
