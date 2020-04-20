package br.com.royalfarma.utils;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.widget.AppCompatEditText;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import static br.com.royalfarma.utils.Util.unmask;

public class MoneyTextWatcher implements TextWatcher {
    final AppCompatEditText campo;

    public MoneyTextWatcher(AppCompatEditText campo) {
        super();
        this.campo = campo;
    }

    private boolean isUpdating = false;
    // Pega a formatacao do sistema, se for brasil R$ se EUA US$
    final Locale myLocale = new Locale("pt", "BR");
    private NumberFormat nf = NumberFormat.getCurrencyInstance(myLocale);

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int after) {
        // Evita que o método seja executado varias vezes.
        // Se tirar ele entre em loop
        if (isUpdating) {
            isUpdating = false;
            return;
        }

        isUpdating = true;
        String str = s.toString();
        str = str.replace("-", "");
        // Verifica se já existe a máscara no texto.
        boolean hasMask = ((str.contains("R$") || str.contains("$")) && (str.contains(".") || str.contains(",") || str.contains("-")));
        // Verificamos se existe máscara
        if (hasMask) {
            // Retiramos a máscara.
//            str = str.replaceAll("[R$]", "").replaceAll("[,]", "")
//                    .replaceAll("[.]", "").replace(" ", "").replace("-", "");
            str = unmask(str);
        }
        try {
            // Transformamos o número que está escrito no EditText em
            // monetário.
            BigDecimal bigDecimal = new BigDecimal(Double.parseDouble(str)).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            str = nf.format(Double.parseDouble(bigDecimal.toString()));
//            str = nf.format(Double.parseDouble(str) / 100);
            campo.setText(str);
            campo.setSelection(campo.getText().length());
        } catch (NumberFormatException e) {
            s = "";
            campo.setText(str);
            campo.setSelection(campo.getText().length());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // Não utilizado
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Não utilizado
    }
}

