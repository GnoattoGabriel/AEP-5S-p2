package com.aep.servicos.repository;

import com.aep.servicos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailAndRole(String email, String role);
    boolean existsByEmailAndRole(String email, String role);
}
