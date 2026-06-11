package com.aep.servicos.controller;

import com.aep.servicos.model.Servico;
import com.aep.servicos.model.Solicitacao;
import com.aep.servicos.repository.ServicoRepository;
import com.aep.servicos.repository.SolicitacaoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import com.aep.servicos.model.SolicitacaoStatus;

@Controller
public class AppController {

    private static final String ACTIVE_PAGE = "activePage";
    private static final String TIPO_PRESTADOR = "PRESTADOR";
    private static final String ORDENAR_DATA_DESC = "data_desc";
    private static final String CATEGORIA_OUTROS = "Outros";
    private static final String SERVICOS = "servicos";
    private static final String PATH_SERVICOS = "/servicos";
    private static final String SOLICITACAO = "solicitacao";
    private static final String SOLICITACOES = "solicitacoes";
    private static final String SEARCHED = "searched";
    private static final Set<SolicitacaoStatus> STATUS_VALIDOS = Set.of(SolicitacaoStatus.PENDENTE, SolicitacaoStatus.EM_ANDAMENTO, SolicitacaoStatus.FINALIZADA, SolicitacaoStatus.CANCELADA);
    private final ServicoRepository servicoRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public AppController(ServicoRepository servicoRepository, SolicitacaoRepository solicitacaoRepository) {
        this.servicoRepository = servicoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @GetMapping("/")
    public String index(Model model, @ModelAttribute("tipoUsuario") String tipoUsuario, @ModelAttribute("usuarioEmail") String usuarioEmail) {
        model.addAttribute(ACTIVE_PAGE, "inicio");

        if (TIPO_PRESTADOR.equals(tipoUsuario)) {
            List<Solicitacao> solicitacoes = solicitacaoRepository.findByEmailPrestadorAndStatusNot(usuarioEmail, SolicitacaoStatus.CANCELADA);
            long pendentes = 0, emAndamento = 0, finalizadas = 0;
            for (Solicitacao s : solicitacoes) {
                if (s.getStatus() == SolicitacaoStatus.PENDENTE) pendentes++;
                else if (s.getStatus() == SolicitacaoStatus.EM_ANDAMENTO) emAndamento++;
                else if (s.getStatus() == SolicitacaoStatus.FINALIZADA) finalizadas++;
            }
            System.out.println(">>> Dashboard prestador: " + usuarioEmail + " | pendentes=" + pendentes);
            long totalServicos = servicoRepository.findByEmailPrestadorOrderByNomeAsc(usuarioEmail).size();

            model.addAttribute("totalSolicitacoes", solicitacoes.size());
            model.addAttribute("pendentes", pendentes);
            model.addAttribute("emAndamento", emAndamento);
            model.addAttribute("finalizadas", finalizadas);
            model.addAttribute("totalServicos", totalServicos);
            return "prestador/index";
        }

        List<Solicitacao> minhasSolicitacoes = solicitacaoRepository.findByEmailClienteOrderByDataCriacaoDesc(usuarioEmail);
        model.addAttribute("minhasSolicitacoes", minhasSolicitacoes);
        return "cliente/index";
    }

    @GetMapping(PATH_SERVICOS)
    public String listaServicos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String query,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            Model model,
            @ModelAttribute("tipoUsuario") String tipoUsuario) {

        if (TIPO_PRESTADOR.equals(tipoUsuario)) {
            return "redirect:/";
        }

        List<Servico> servicos = servicoRepository.searchServicos(categoria, query);
        List<String> categorias = servicoRepository.findAllCategorias();

        model.addAttribute(ACTIVE_PAGE, SERVICOS);
        model.addAttribute(SERVICOS, servicos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("selectedCategoria", categoria);
        model.addAttribute("searchQuery", query);

        if (hxRequest != null) {
            return "cliente/servicos :: list-grid";
        }
        return "cliente/servicos";
    }

    @GetMapping("/solicitar")
    public String solicitarForm(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long servicoId,
            Model model) {
        model.addAttribute(ACTIVE_PAGE, "solicitar");

        if (servicoId != null) {
            Optional<Servico> servicoOpt = servicoRepository.findById(servicoId);
            if (servicoOpt.isPresent()) {
                Servico servico = servicoOpt.get();
                model.addAttribute("servicoSelecionado", servico);
                categoria = servico.getCategoria();
            }
        }

        List<String> categorias = servicoRepository.findAllCategorias();
        if (categorias.isEmpty()) {
            categorias = List.of("Reformas", "Assistência Técnica", "Limpeza", "Design", "Pet", CATEGORIA_OUTROS);
        } else if (!categorias.contains(CATEGORIA_OUTROS)) {
            categorias = new java.util.ArrayList<>(categorias);
            categorias.add(CATEGORIA_OUTROS);
        }
        model.addAttribute("categorias", categorias);
        model.addAttribute("selectedCategoria", categoria);
        return "cliente/solicitar";
    }

    @PostMapping("/solicitar")
    public String solicitarSubmit(
            @RequestParam String nomeCliente,
            @RequestParam String telefone,
            @RequestParam String descricao,
            @RequestParam String endereco,
            @RequestParam String dataAtendimento,
            @RequestParam(required = false) String horarioAtendimento,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long servicoId,
            @RequestParam(required = false) Double valor,
            @ModelAttribute("usuarioEmail") String usuarioEmail,
            Model model) {

        Servico servico;
        if (servicoId != null) {
            servico = servicoRepository.findById(servicoId)
                    .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
        } else {
            if (categoria == null || categoria.isBlank()) {
                throw new IllegalArgumentException("Categoria do serviço é obrigatória");
            }
            if (valor == null || valor <= 0) {
                throw new IllegalArgumentException("Valor do serviço é obrigatório");
            }
            servico = Servico.builder()
                    .nome("Solicitação: " + categoria)
                    .categoria(categoria)
                    .profissional(nomeCliente)
                    .valor(valor)
                    .avaliacao(0.0)
                    .build();
            servicoRepository.save(servico);
        }

        // Basic validation of the textual fields
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente é obrigatório");
        }
        if (telefone == null || telefone.isBlank()) {
            throw new IllegalArgumentException("Telefone para contato é obrigatório");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
        if (endereco == null || endereco.isBlank()) {
            throw new IllegalArgumentException("Endereço é obrigatório");
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data;
        try {
            data = LocalDate.parse(dataAtendimento, fmt);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data de atendimento inválida");
        }

        LocalTime horario = null;
        if (horarioAtendimento != null && !horarioAtendimento.isEmpty()) {
            try {
                horario = LocalTime.parse(horarioAtendimento);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Horário de atendimento inválido");
            }
        }

        Solicitacao solicitacao = Solicitacao.builder()
                .nomeCliente(nomeCliente)
                .emailCliente(usuarioEmail)
                .telefone(telefone)
                .descricao(descricao)
                .endereco(endereco)
                .dataAtendimento(data)
                .horarioAtendimento(horario)
                .servico(servico)
                .emailPrestador(servico.getEmailPrestador())
                .build();

        solicitacaoRepository.save(solicitacao);
        System.out.println(">>> SOLICITACAO CRIADA: " + solicitacao.getProtocolo() + " | Cliente: " + nomeCliente);

        model.addAttribute(SOLICITACAO, solicitacao);
        return "cliente/solicitar :: sucesso-card";
    }

    @GetMapping("/meus-pedidos")
    public String meusPedidos(Model model, @ModelAttribute("usuarioEmail") String usuarioEmail) {
        model.addAttribute(ACTIVE_PAGE, "meus-pedidos");
        List<Solicitacao> solicitacoes = solicitacaoRepository.findByEmailClienteOrderByDataCriacaoDesc(usuarioEmail);
        model.addAttribute(SOLICITACOES, solicitacoes);
        return "cliente/meus-pedidos";
    }

    @GetMapping("/protocolo")
    public String consultarProtocolo(
            @RequestParam(required = false) String codigo,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            Model model) {

        model.addAttribute(ACTIVE_PAGE, "protocolo");
        model.addAttribute("codigo", codigo);

        if (codigo != null && !codigo.trim().isEmpty()) {
            Optional<Solicitacao> solicitacaoOpt = solicitacaoRepository.findByProtocolo(codigo.trim().toUpperCase());
            model.addAttribute(SOLICITACAO, solicitacaoOpt.orElse(null));
            model.addAttribute(SEARCHED, true);
        } else {
            model.addAttribute(SEARCHED, false);
        }

        if (hxRequest != null) {
            return "cliente/protocolo :: detalhe-card";
        }
        return "cliente/protocolo";
    }

    @PostMapping("/protocolo/status")
    public String atualizarStatus(
            @RequestParam Long id,
            @RequestParam String status,
            @RequestParam(required = false) String redirect,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            Model model) {

        SolicitacaoStatus newStatus;
        try {
            newStatus = SolicitacaoStatus.valueOf(status);
        } catch (Exception e) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
        if (!STATUS_VALIDOS.contains(newStatus)) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }

        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        solicitacao.setStatus(newStatus);
        solicitacaoRepository.save(solicitacao);
        System.out.println(">>> STATUS ATUALIZADO: " + solicitacao.getProtocolo() + " -> " + newStatus);

        if (hxRequest != null) {
            model.addAttribute(SOLICITACAO, solicitacao);
            model.addAttribute(SEARCHED, true);
            model.addAttribute("codigo", solicitacao.getProtocolo());
            return "cliente/protocolo :: detalhe-card";
        }

        if (redirect != null && !redirect.isBlank()) {
            return "redirect:" + redirect;
        }
        return "redirect:/prestador/solicitacoes";
    }

    @GetMapping("/agenda")
    public String agenda(Model model, @ModelAttribute("usuarioEmail") String usuarioEmail) {
        model.addAttribute(ACTIVE_PAGE, "agenda");
        LocalDate hoje = LocalDate.now();
        // expose current date for consistent display in view
        model.addAttribute("dataHoje", hoje);

        List<Solicitacao> atendimentosHoje = solicitacaoRepository.findByDataAtendimentoAndEmailPrestador(hoje, usuarioEmail)
                .stream().filter(s -> s.getStatus() != SolicitacaoStatus.CANCELADA && s.getStatus() != SolicitacaoStatus.PENDENTE)
                .sorted((a,b) -> b.getHorarioAtendimento().compareTo(a.getHorarioAtendimento()))
                .toList();
        List<Solicitacao> proximosAtendimentos = solicitacaoRepository.findByDataAtendimentoAfterAndEmailPrestador(hoje, usuarioEmail)
                .stream().filter(s -> s.getStatus() != SolicitacaoStatus.CANCELADA && s.getStatus() != SolicitacaoStatus.PENDENTE)
                .sorted((a,b) -> a.getDataAtendimento().compareTo(b.getDataAtendimento()))
                .toList();
        System.out.println(">>> Agenda: " + usuarioEmail + " | hoje=" + atendimentosHoje.size() + " | proximos=" + proximosAtendimentos.size());

        model.addAttribute("hoje", atendimentosHoje);
        model.addAttribute("proximos", proximosAtendimentos);
        return "prestador/agenda";
    }

    @GetMapping("/prestador/servicos")
    public String prestadorServicos(Model model, @ModelAttribute("usuarioEmail") String usuarioEmail) {
        model.addAttribute(ACTIVE_PAGE, "prestador-servicos");
        List<Servico> servicos = servicoRepository.findByEmailPrestadorOrderByNomeAsc(usuarioEmail);
        model.addAttribute(SERVICOS, servicos);
        return "prestador/servicos";
    }

    @GetMapping("/prestador/servicos/cadastrar")
    public String prestadorServicoForm(Model model) {
        model.addAttribute(ACTIVE_PAGE, "prestador-cadastrar");
        model.addAttribute("servico", new Servico());
        return "prestador/servico-form";
    }

    @PostMapping("/prestador/servicos/cadastrar")
    public String prestadorServicoSubmit(
            @RequestParam String nome,
            @RequestParam String categoria,
            @RequestParam String profissional,
            @RequestParam Double valor,
            @RequestParam(required = false, defaultValue = "5.0") Double avaliacao,
            @ModelAttribute("usuarioEmail") String usuarioEmail) {

        Servico servico = Servico.builder()
                .nome(nome)
                .categoria(categoria)
                .profissional(profissional)
                .avaliacao(avaliacao)
                .valor(valor)
                .emailPrestador(usuarioEmail)
                .build();

        servicoRepository.save(servico);
        return "redirect:/prestador/servicos";
    }

    @GetMapping("/prestador/disponiveis")
    public String prestadorDisponiveis(Model model) {
        model.addAttribute(ACTIVE_PAGE, "prestador-disponiveis");
        List<Solicitacao> disponiveis = solicitacaoRepository.findByEmailPrestadorIsNullAndStatus(SolicitacaoStatus.PENDENTE);
        model.addAttribute(SOLICITACOES, disponiveis);
        return "prestador/disponiveis";
    }

    @PostMapping("/prestador/claim/{id}")
    public String claimSolicitacao(@PathVariable Long id, @ModelAttribute("usuarioEmail") String usuarioEmail) {
        Solicitacao s = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        if (s.getEmailPrestador() != null) {
            throw new IllegalArgumentException("Esta solicitação já foi assumida por outro prestador");
        }
        s.setEmailPrestador(usuarioEmail);
        s.setStatus(SolicitacaoStatus.EM_ANDAMENTO);
        Servico servico = s.getServico();
        if (servico.getEmailPrestador() == null) {
            servico.setEmailPrestador(usuarioEmail);
            servicoRepository.save(servico);
        }
        solicitacaoRepository.save(s);
        System.out.println(">>> CLAIM: " + s.getProtocolo() + " assumida por " + usuarioEmail);
        return "redirect:/prestador/solicitacoes";
    }

    @GetMapping("/prestador/solicitacoes")
    public String prestadorSolicitacoes(
            @RequestParam(defaultValue = ORDENAR_DATA_DESC) String ordenar,
            Model model,
            @ModelAttribute("usuarioEmail") String usuarioEmail) {
        model.addAttribute(ACTIVE_PAGE, "prestador-solicitacoes");
        List<Solicitacao> todas = solicitacaoRepository.findByEmailPrestadorAndStatusNot(usuarioEmail, SolicitacaoStatus.CANCELADA);

        Comparator<Solicitacao> comparator;
        switch (ordenar) {
            case "data_asc":
                comparator = Comparator.comparing(Solicitacao::getDataAtendimento, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Solicitacao::getHorarioAtendimento, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "nome_asc":
                comparator = Comparator.comparing(Solicitacao::getNomeCliente, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Solicitacao::getDataAtendimento, Comparator.nullsLast(Comparator.reverseOrder()));
                break;
            case "nome_desc":
                comparator = Comparator.comparing(Solicitacao::getNomeCliente, String.CASE_INSENSITIVE_ORDER).reversed()
                        .thenComparing(Solicitacao::getDataAtendimento, Comparator.nullsLast(Comparator.reverseOrder()));
                break;
            case ORDENAR_DATA_DESC:
            default:
                comparator = Comparator.comparing(Solicitacao::getDataAtendimento, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Solicitacao::getHorarioAtendimento, Comparator.nullsLast(Comparator.reverseOrder()));
                break;
        }

        List<Solicitacao> solicitacoes = todas.stream()
                .filter(s -> s.getStatus() != SolicitacaoStatus.CANCELADA)
                .sorted(comparator)
                .toList();
        model.addAttribute(SOLICITACOES, solicitacoes);
        model.addAttribute("ordenar", ordenar);
        return "prestador/solicitacoes";
    }

    @GetMapping("/notificacoes")
    public String notificacoes(Model model, @ModelAttribute("tipoUsuario") String tipoUsuario, @ModelAttribute("usuarioEmail") String usuarioEmail) {
        String tipo = tipoUsuario;

        if (TIPO_PRESTADOR.equals(tipo)) {
            List<Solicitacao> todas = solicitacaoRepository.findByEmailPrestadorAndStatusNot(usuarioEmail, SolicitacaoStatus.CANCELADA);
            List<Solicitacao> pendentes = new java.util.ArrayList<>();
            for (Solicitacao s : todas) {
                if (s.getStatus() == SolicitacaoStatus.PENDENTE) {
                    pendentes.add(s);
                    if (pendentes.size() == 5) break;
                }
            }
            long countPendentes = 0;
            for (Solicitacao s : todas) {
                if (s.getStatus() == SolicitacaoStatus.PENDENTE) countPendentes++;
            }
            model.addAttribute("pendentes", pendentes);
            model.addAttribute("countPendentes", countPendentes);
        } else {
            List<Solicitacao> todasClientes = solicitacaoRepository.findAll();
            todasClientes.sort((a, b) -> b.getDataCriacao().compareTo(a.getDataCriacao()));
            List<Solicitacao> recentes = new java.util.ArrayList<>();
            for (int i = 0; i < Math.min(5, todasClientes.size()); i++) {
                recentes.add(todasClientes.get(i));
            }
            model.addAttribute("recentes", recentes);
        }

        model.addAttribute("tipoUsuario", tipo);
        return "fragments/notificacoes";
    }
}
