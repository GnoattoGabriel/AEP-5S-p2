package com.aep.servicos.controller;

import com.aep.servicos.model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalAuthAdvice {

    @ModelAttribute("tipoUsuario")
    public String tipoUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Usuario usuario) {
            return switch (usuario.getRole()) {
                case "ROLE_CLIENTE" -> "CLIENTE";
                case "ROLE_PRESTADOR" -> "PRESTADOR";
                default -> null;
            };
        }
        return null;
    }

    @ModelAttribute("usuarioNome")
    public String usuarioNome() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario.getNome();
        }
        return null;
    }

    @ModelAttribute("usuarioEmail")
    public String usuarioEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario.getEmail();
        }
        return null;
    }
}
