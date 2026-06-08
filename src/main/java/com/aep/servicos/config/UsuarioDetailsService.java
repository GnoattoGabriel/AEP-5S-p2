package com.aep.servicos.config;

import com.aep.servicos.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] parts = username.split("::", 2);
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Formato de usuário inválido");
        }
        String email = parts[0];
        String role = "ROLE_" + parts[1];
        return usuarioRepository.findByEmailAndRole(email, role)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }
}
