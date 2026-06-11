package com.aep.servicos.service;

import com.aep.servicos.model.Servico;
import com.aep.servicos.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {

    private static final String CATEGORIA_OUTROS = "Outros";

    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    public Servico criarServico(String nome, String categoria, String profissional,
                                 Double valor, Double avaliacao, String emailPrestador) {
        Servico servico = Servico.builder()
                .nome(nome)
                .categoria(categoria)
                .profissional(profissional)
                .avaliacao(avaliacao)
                .valor(valor)
                .emailPrestador(emailPrestador)
                .build();
        servicoRepository.save(servico);
        return servico;
    }

    public List<String> buscarCategorias() {
        List<String> categorias = servicoRepository.findAllCategorias();
        if (categorias.isEmpty()) {
            return List.of("Reformas", "Assistência Técnica", "Limpeza", "Design", "Pet", CATEGORIA_OUTROS);
        }
        if (!categorias.contains(CATEGORIA_OUTROS)) {
            var mutable = new ArrayList<>(categorias);
            mutable.add(CATEGORIA_OUTROS);
            return mutable;
        }
        return categorias;
    }

    public Optional<Servico> buscarPorId(Long id) {
        return servicoRepository.findById(id);
    }

    public ServicosListaDTO pesquisarServicos(String categoria, String query) {
        return new ServicosListaDTO(
                servicoRepository.searchServicos(categoria, query),
                servicoRepository.findAllCategorias());
    }

    public List<Servico> listarPorEmailPrestador(String email) {
        return servicoRepository.findByEmailPrestadorOrderByNomeAsc(email);
    }

    public record ServicosListaDTO(List<Servico> servicos, List<String> categorias) {}
}
