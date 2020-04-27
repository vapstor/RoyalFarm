package br.com.royalfarma.utils;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class BuscaCEPHelper {

    public static String getEndereco(String CEP) throws IOException {

        //***************************************************
        try {
            Document doc = Jsoup.connect("http://www.qualocep.com/busca-cep/" + CEP)
                    .timeout(120000)
                    .get();
            Elements urlPesquisa = doc.select("span[itemprop=streetAddress]");
            for (Element urlEndereco : urlPesquisa) {
                return urlEndereco.text();
            }

        } catch (SocketTimeoutException e) {

        } catch (HttpStatusException w) {

        }
        return CEP;
    }

    public String getBairro(String CEP) throws IOException {

        //***************************************************
        try {

            Document doc = Jsoup.connect("http://www.qualocep.com/busca-cep/" + CEP)
                    .timeout(120000)
                    .get();
            Elements urlPesquisa = doc.select("td:gt(1)");
            for (Element urlBairro : urlPesquisa) {
                return urlBairro.text();
            }

        } catch (SocketTimeoutException e) {

        } catch (HttpStatusException w) {

        }
        return CEP;
    }

    public static String getCidadeByCEP(String CEP) throws IOException {

        //***************************************************
        try {

            Document doc = Jsoup.connect("https://www.qualocep.com/busca-cep/" + CEP)
                    .timeout(12000)
                    .get();
            Elements urlPesquisa = doc.select("span[itemprop=addressLocality]");
            for (Element urlCidade : urlPesquisa) {
                return urlCidade.text();
            }

        } catch (SocketTimeoutException e) {

        } catch (HttpStatusException w) {

        }
        return CEP;
    }

    public static String getUF(String CEP) throws IOException {

        //***************************************************
        try {

            Document doc = Jsoup.connect("http://www.qualocep.com/busca-cep/" + CEP)
                    .timeout(120000)
                    .get();
            Elements urlPesquisa = doc.select("span[itemprop=addressRegion]");
            for (Element urlUF : urlPesquisa) {
                return urlUF.text();
            }

        } catch (SocketTimeoutException e) {

        } catch (HttpStatusException w) {

        }
        return CEP;
    }
}
