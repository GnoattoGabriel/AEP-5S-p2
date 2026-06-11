package com.aep.servicos.service;

import com.aep.servicos.model.Usuario;
import com.aep.servicos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String cadastrar(String nome, String email, String senha,
                            String confirmarSenha, String role) {
        if (nome == null || nome.trim().length() < 3) {
            return "Nome deve ter pelo menos 3 caracteres.";
        }
        if (email == null || !email.matches("^[\\w.\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            return "Email inválido.";
        }
        if (senha == null || senha.length() < 6) {
            return "Senha deve ter pelo menos 6 caracteres.";
        }
        if (!senha.equals(confirmarSenha)) {
            return "Senhas não conferem.";
        }
        if (!role.equals("ROLE_CLIENTE") && !role.equals("ROLE_PRESTADOR")) {
            return "Tipo de conta inválido.";
        }
        if (usuarioRepository.existsByEmailAndRole(email.trim().toLowerCase(), role)) {
            return "Este email já está cadastrado com este tipo de conta.";
        }

        Usuario usuario = Usuario.builder()
                .nome(nome.trim())
                .email(email.trim().toLowerCase())
                .senha(passwordEncoder.encode(senha))
                .role(role)
                .build();
        usuarioRepository.save(usuario);
        return null;
    }
}
