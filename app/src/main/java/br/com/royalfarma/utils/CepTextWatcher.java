package br.com.royalfarma.utils;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.widget.AppCompatEditText;

import java.text.NumberFormat;
import java.util.Locale;

public class CepTextWatcher implements TextWatcher {
    final AppCompatEditText campo;

    public CepTextWatcher(AppCompatEditText campo) {
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
        // Quando o texto é alterado o onTextChange é chamado
        // Essa flag evita a chamada infinita desse método
        if (isUpdating) {
            isUpdating = false;
            return;
        }

        // Ao apagar o texto, a máscara é removida,
        // então o posicionamento do cursor precisa
        // saber se o texto atual tinha ou não, máscara
        boolean hasMask =
                s.toString().indexOf('.') > -1 ||
                        s.toString().indexOf('-') > -1;

        // Remove o '.' e '-' da String
        String str = s.toString()
                .replaceAll("[.]", "")
                .replaceAll("[-]", "");

        // as variáveis before e after dizem o tamanho
        // anterior e atual da String, se after > before
        // é pq está apagando
        if (after > before) {
            // Se tem mais de 5 caracteres (sem máscara)
            // coloca o '.' e o '-'
            if (str.length() > 5) {
                str =
                        str.substring(0, 5) + '-' +
                                str.substring(5);
            }
            // Seta a flag pra evitar chamada infinita
            isUpdating = true;
            // seta o novo texto
            campo.setText(str);
            // seta a posição do cursor
            campo.setSelection(campo.getText().length());

        } else {
            isUpdating = true;
            campo.setText(str);
            // Se estiver apagando posiciona o cursor
            // no local correto. Isso trata a deleção dos
            // caracteres da máscara.
            campo.setSelection(
                    Math.max(0, Math.min(
                            hasMask ? start - before : start,
                            str.length())));
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

