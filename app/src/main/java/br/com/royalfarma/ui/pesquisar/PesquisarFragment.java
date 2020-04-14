package br.com.royalfarma.ui.pesquisar;

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

public class PesquisarFragment extends Fragment {

    private PesquisarViewModel pesquisarViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        pesquisarViewModel = ViewModelProviders.of(this).get(PesquisarViewModel.class);
        View root = inflater.inflate(R.layout.fragment_pesquisar, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        pesquisarViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}
