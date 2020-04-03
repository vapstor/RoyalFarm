package br.com.royalfarma.ui.carrinho;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import br.com.royalfarma.R;

public class CarrinhoFragment extends Fragment {

    private CarrinhoViewModel carrinhoViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        carrinhoViewModel = ViewModelProviders.of(this).get(CarrinhoViewModel.class);
        View root = inflater.inflate(R.layout.fragment_carrinho, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        carrinhoViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}
