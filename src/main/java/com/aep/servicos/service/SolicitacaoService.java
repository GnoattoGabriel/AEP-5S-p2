package com.aep.servicos.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aep.servicos.model.Servico;
import com.aep.servicos.model.Solicitacao;
import com.aep.servicos.model.SolicitacaoStatus;
import com.aep.servicos.repository.ServicoRepository;
import com.aep.servicos.repository.SolicitacaoRepository;

@Service
public class SolicitacaoService {

    private static final Set<SolicitacaoStatus> STATUS_VALIDOS = Set.of(
            SolicitacaoStatus.PENDENTE,
            SolicitacaoStatus.EM_ANDAMENTO,
            SolicitacaoStatus.FINALIZADA,
            SolicitacaoStatus.CANCELADA);

    private final SolicitacaoRepository solicitacaoRepository;
    private final ServicoRepository servicoRepository;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              ServicoRepository servicoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.servicoRepository = servicoRepository;
    }

    public DashboardDTO obterDashboardPrestador(String email) {
        List<Solicitacao> solicitacoes = solicitacaoRepository
                .findByEmailPrestadorAndStatusNot(email, SolicitacaoStatus.CANCELADA);
        long pendentes = 0, emAndamento = 0, finalizadas = 0;
        for (Solicitacao s : solicitacoes) {
            if (s.getStatus() == SolicitacaoStatus.PENDENTE)
                pendentes++;
            else if (s.getStatus() == SolicitacaoStatus.EM_ANDAMENTO)
                emAndamento++;
            else if (s.getStatus() == SolicitacaoStatus.FINALIZADA)
                finalizadas++;
        }
        long totalServicos = servicoRepository.findByEmailPrestadorOrderByNomeAsc(email).size();
        System.out.println(">>> Dashboard prestador: " + email + " | pendentes=" + pendentes);
        return new DashboardDTO(solicitacoes.size(), pendentes, emAndamento, finalizadas, totalServicos);
    }

    @Transactional
    public Solicitacao criarSolicitacao(
            String nomeCliente, String telefone, String descricao,
            String endereco, String dataAtendimento, String horarioAtendimento,
            String categoria, Long servicoId, Double valor,
            String usuarioEmail) {

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
        return solicitacao;
    }

    public Solicitacao atualizarStatus(Long id, String statusStr) {
        SolicitacaoStatus newStatus;
        try {
            newStatus = SolicitacaoStatus.valueOf(statusStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Status inválido: " + statusStr);
        }
        if (!STATUS_VALIDOS.contains(newStatus)) {
            throw new IllegalArgumentException("Status inválido: " + statusStr);
        }
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        solicitacao.setStatus(newStatus);
        solicitacaoRepository.save(solicitacao);
        System.out.println(">>> STATUS ATUALIZADO: " + solicitacao.getProtocolo() + " -> " + newStatus);
        return solicitacao;
    }

    public AgendaDTO obterAgendaPrestador(String email) {
        LocalDate hoje = LocalDate.now();
        List<Solicitacao> atendimentosHoje = solicitacaoRepository
                .findByDataAtendimentoAndEmailPrestador(hoje, email)
                .stream()
                .filter(s -> s.getStatus() != SolicitacaoStatus.CANCELADA
                        && s.getStatus() != SolicitacaoStatus.PENDENTE)
                .sorted((a, b) -> b.getHorarioAtendimento().compareTo(a.getHorarioAtendimento()))
                .toList();
        List<Solicitacao> proximosAtendimentos = solicitacaoRepository
                .findByDataAtendimentoAfterAndEmailPrestador(hoje, email)
                .stream()
                .filter(s -> s.getStatus() != SolicitacaoStatus.CANCELADA
                        && s.getStatus() != SolicitacaoStatus.PENDENTE)
                .sorted((a, b) -> a.getDataAtendimento().compareTo(b.getDataAtendimento()))
                .toList();
        System.out.println(">>> Agenda: " + email + " | hoje=" + atendimentosHoje.size() + " | proximos="
                + proximosAtendimentos.size());
        return new AgendaDTO(hoje, atendimentosHoje, proximosAtendimentos);
    }

    @Transactional
    public Solicitacao assumirSolicitacao(Long id, String emailPrestador) {
        Solicitacao s = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        if (s.getEmailPrestador() != null) {
            throw new IllegalArgumentException("Esta solicitação já foi assumida por outro prestador");
        }
        s.setEmailPrestador(emailPrestador);
        s.setStatus(SolicitacaoStatus.EM_ANDAMENTO);
        Servico servico = s.getServico();
        if (servico.getEmailPrestador() == null) {
            servico.setEmailPrestador(emailPrestador);
            servicoRepository.save(servico);
        }
        solicitacaoRepository.save(s);
        System.out.println(">>> CLAIM: " + s.getProtocolo() + " assumida por " + emailPrestador);
        return s;
    }

    public List<Solicitacao> listarSolicitacoesPrestador(String email, String ordenar) {
        List<Solicitacao> todas = solicitacaoRepository.findByEmailPrestadorAndStatusNot(email,
                SolicitacaoStatus.CANCELADA);

        Comparator<Solicitacao> comparator;
        switch (ordenar) {
            case "data_asc":
                comparator = Comparator
                        .comparing(Solicitacao::getDataAtendimento, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Solicitacao::getHorarioAtendimento,
                                Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "nome_asc":
                comparator = Comparator.comparing(Solicitacao::getNomeCliente, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Solicitacao::getDataAtendimento,
                                Comparator.nullsLast(Comparator.reverseOrder()));
                break;
            case "nome_desc":
                comparator = Comparator.comparing(Solicitacao::getNomeCliente, String.CASE_INSENSITIVE_ORDER).reversed()
                        .thenComparing(Solicitacao::getDataAtendimento,
                                Comparator.nullsLast(Comparator.reverseOrder()));
                break;
            case "data_desc":
            default:
                comparator = Comparator
                        .comparing(Solicitacao::getDataAtendimento, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Solicitacao::getHorarioAtendimento,
                                Comparator.nullsLast(Comparator.reverseOrder()));
                break;
        }

        return todas.stream()
                .filter(s -> s.getStatus() != SolicitacaoStatus.CANCELADA)
                .sorted(comparator)
                .toList();
    }

    public PrestadorNotificacaoDTO obterNotificacoesPrestador(String email) {
        List<Solicitacao> todas = solicitacaoRepository.findByEmailPrestadorAndStatusNot(email,
                SolicitacaoStatus.CANCELADA);
        List<Solicitacao> pendentes = new ArrayList<>();
        for (Solicitacao s : todas) {
            if (s.getStatus() == SolicitacaoStatus.PENDENTE) {
                pendentes.add(s);
                if (pendentes.size() == 5)
                    break;
            }
        }
        long countPendentes = 0;
        for (Solicitacao s : todas) {
            if (s.getStatus() == SolicitacaoStatus.PENDENTE)
                countPendentes++;
        }
        return new PrestadorNotificacaoDTO(pendentes, countPendentes);
    }

    public List<Solicitacao> obterNotificacoesCliente() {
        List<Solicitacao> todas = solicitacaoRepository.findAll();
        todas.sort((a, b) -> b.getDataCriacao().compareTo(a.getDataCriacao()));
        List<Solicitacao> recentes = new ArrayList<>();
        for (int i = 0; i < Math.min(5, todas.size()); i++) {
            recentes.add(todas.get(i));
        }
        return recentes;
    }

    public List<Solicitacao> obterSolicitacoesCliente(String email) {
        return solicitacaoRepository.findByEmailClienteOrderByDataCriacaoDesc(email);
    }

    public Optional<Solicitacao> consultarPorProtocolo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return Optional.empty();
        }
        return solicitacaoRepository.findByProtocolo(codigo.trim().toUpperCase());
    }

    public record DashboardDTO(long totalSolicitacoes, long pendentes,
                               long emAndamento, long finalizadas, long totalServicos) {}

    public record AgendaDTO(LocalDate dataHoje, List<Solicitacao> hoje,
                            List<Solicitacao> proximos) {}

    public record PrestadorNotificacaoDTO(List<Solicitacao> pendentes, long countPendentes) {}
}
