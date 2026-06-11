package com.aep.servicos.controller;

import com.aep.servicos.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final String VIEW_CADASTRO = "sistema/cadastro";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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

        String erro = authService.cadastrar(nome, email, senha, confirmarSenha, role);
        if (erro != null) {
            model.addAttribute("erro", erro);
            return VIEW_CADASTRO;
        }
        return "redirect:/login?cadastro=ok";
    }
}
