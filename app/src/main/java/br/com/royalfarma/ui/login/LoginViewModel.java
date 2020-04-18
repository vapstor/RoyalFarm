package br.com.royalfarma.ui.login;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import br.com.royalfarma.model.Usuario;

public class LoginViewModel extends ViewModel {
    private MutableLiveData<Usuario> usuarioMutableLiveData;

    public LoginViewModel() {
        createNewUser("", "");
    }

    private void createNewUser(String user, String password) {
        if (user.equals("") || password.equals("")) {
            usuarioMutableLiveData = new MutableLiveData<>();
            usuarioMutableLiveData.setValue(null);
        } else {
            usuarioMutableLiveData = new MutableLiveData<>();
            usuarioMutableLiveData.setValue(new Usuario(user, password));
        }
    }

    public void updateUsuarioInfo(Usuario usuario) {
        usuarioMutableLiveData.setValue(usuario);
    }

    public MutableLiveData<Usuario> getUsuarioMutableLiveData() {
        return usuarioMutableLiveData;
    }
}
