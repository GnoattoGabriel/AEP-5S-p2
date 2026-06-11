package com.aep.servicos.controller;

import com.aep.servicos.model.Usuario;
import com.aep.servicos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final String VIEW_CADASTRO = "sistema/cadastro";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "sistema/login";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return VIEW_CADASTRO;
    }

    @PostMapping("/cadastro")
    public String cadastrar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            @RequestParam String role,
            Model model) {

        if (nome == null || nome.trim().length() < 3) {
            model.addAttribute("erro", "Nome deve ter pelo menos 3 caracteres.");
            return VIEW_CADASTRO;
        }
        if (email == null || !email.matches("^[\\w.\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            model.addAttribute("erro", "Email inválido.");
            return VIEW_CADASTRO;
        }
        if (senha == null || senha.length() < 6) {
            model.addAttribute("erro", "Senha deve ter pelo menos 6 caracteres.");
            return VIEW_CADASTRO;
        }
        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("erro", "Senhas não conferem.");
            return VIEW_CADASTRO;
        }
        if (!role.equals("ROLE_CLIENTE") && !role.equals("ROLE_PRESTADOR")) {
            model.addAttribute("erro", "Tipo de conta inválido.");
            return VIEW_CADASTRO;
        }
        if (usuarioRepository.existsByEmailAndRole(email.trim().toLowerCase(), role)) {
            model.addAttribute("erro", "Este email já está cadastrado com este tipo de conta.");
            return VIEW_CADASTRO;
        }

        Usuario usuario = Usuario.builder()
                .nome(nome.trim())
                .email(email.trim().toLowerCase())
                .senha(passwordEncoder.encode(senha))
                .role(role)
                .build();

        usuarioRepository.save(usuario);

        return "redirect:/login?cadastro=ok";
    }
}
