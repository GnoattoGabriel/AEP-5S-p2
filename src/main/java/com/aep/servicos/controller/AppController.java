package com.aep.servicos.controller;

import com.aep.servicos.model.Servico;
import com.aep.servicos.model.Solicitacao;
import com.aep.servicos.repository.ServicoRepository;
import com.aep.servicos.repository.SolicitacaoRepository;
import com.aep.servicos.service.ServicoService;
import com.aep.servicos.service.SolicitacaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    private final ServicoRepository servicoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final SolicitacaoService solicitacaoService;
    private final ServicoService servicoService;

    public AppController(ServicoRepository servicoRepository,
                         SolicitacaoRepository solicitacaoRepository,
                         SolicitacaoService solicitacaoService,
                         ServicoService servicoService) {
        this.servicoRepository = servicoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.solicitacaoService = solicitacaoService;
        this.servicoService = servicoService;
    }

    @GetMapping("/")
    public String index(Model model, @ModelAttribute("tipoUsuario") String tipoUsuario,
            @ModelAttribute("usuarioEmail") String usuarioEmail) {
        model.addAttribute(ACTIVE_PAGE, "inicio");

        if (TIPO_PRESTADOR.equals(tipoUsuario)) {
            var dto = solicitacaoService.obterDashboardPrestador(usuarioEmail);
            model.addAttribute("totalSolicitacoes", dto.totalSolicitacoes());
            model.addAttribute("pendentes", dto.pendentes());
            model.addAttribute("emAndamento", dto.emAndamento());
            model.addAttribute("finalizadas", dto.finalizadas());
            model.addAttribute("totalServicos", dto.totalServicos());
            return "prestador/index";
        }

        model.addAttribute("minhasSolicitacoes", solicitacaoService.obterSolicitacoesCliente(usuarioEmail));
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
            var servicoOpt = servicoRepository.findById(servicoId);
            if (servicoOpt.isPresent()) {
                model.addAttribute("servicoSelecionado", servicoOpt.get());
                categoria = servicoOpt.get().getCategoria();
            }
        }

        model.addAttribute("categorias", servicoService.buscarCategorias());
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
        Solicitacao solicitacao = solicitacaoService.criarSolicitacao(
                nomeCliente, telefone, descricao, endereco, dataAtendimento,
                horarioAtendimento, categoria, servicoId, valor, usuarioEmail);
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

        var solicitacaoOpt = solicitacaoService.consultarPorProtocolo(codigo);
        model.addAttribute(SOLICITACAO, solicitacaoOpt.orElse(null));
        model.addAttribute(SEARCHED, codigo != null && !codigo.trim().isEmpty());

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

        Solicitacao solicitacao = solicitacaoService.atualizarStatus(id, status);

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
        var dto = solicitacaoService.obterAgendaPrestador(usuarioEmail);
        model.addAttribute("dataHoje", dto.dataHoje());
        model.addAttribute("hoje", dto.hoje());
        model.addAttribute("proximos", dto.proximos());
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
        servicoService.criarServico(nome, categoria, profissional, valor, avaliacao, usuarioEmail);
        return "redirect:/prestador/servicos";
    }

    @GetMapping("/prestador/disponiveis")
    public String prestadorDisponiveis(Model model) {
        model.addAttribute(ACTIVE_PAGE, "prestador-disponiveis");
        List<Solicitacao> disponiveis = solicitacaoRepository
                .findByEmailPrestadorIsNullAndStatus(SolicitacaoStatus.PENDENTE);
        model.addAttribute(SOLICITACOES, disponiveis);
        return "prestador/disponiveis";
    }

    @PostMapping("/prestador/claim/{id}")
    public String claimSolicitacao(@PathVariable Long id, @ModelAttribute("usuarioEmail") String usuarioEmail) {
        solicitacaoService.assumirSolicitacao(id, usuarioEmail);
        return "redirect:/prestador/solicitacoes";
    }

    @GetMapping("/prestador/solicitacoes")
    public String prestadorSolicitacoes(
            @RequestParam(defaultValue = ORDENAR_DATA_DESC) String ordenar,
            Model model,
            @ModelAttribute("usuarioEmail") String usuarioEmail) {
        model.addAttribute(ACTIVE_PAGE, "prestador-solicitacoes");
        model.addAttribute(SOLICITACOES, solicitacaoService.listarSolicitacoesPrestador(usuarioEmail, ordenar));
        model.addAttribute("ordenar", ordenar);
        return "prestador/solicitacoes";
    }

    @GetMapping("/notificacoes")
    public String notificacoes(Model model, @ModelAttribute("tipoUsuario") String tipoUsuario,
            @ModelAttribute("usuarioEmail") String usuarioEmail) {
        model.addAttribute("tipoUsuario", tipoUsuario);

        if (TIPO_PRESTADOR.equals(tipoUsuario)) {
            var dto = solicitacaoService.obterNotificacoesPrestador(usuarioEmail);
            model.addAttribute("pendentes", dto.pendentes());
            model.addAttribute("countPendentes", dto.countPendentes());
        } else {
            model.addAttribute("recentes", solicitacaoService.obterNotificacoesCliente());
        }

        return "fragments/notificacoes";
    }
}
