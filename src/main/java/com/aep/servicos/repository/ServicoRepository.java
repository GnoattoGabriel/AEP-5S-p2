package com.aep.servicos.repository;

import com.aep.servicos.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    @Query("SELECT s FROM Servico s WHERE s.emailPrestador IS NOT NULL AND " +
           "(:categoria IS NULL OR :categoria = '' OR s.categoria = :categoria) AND " +
           "(:query IS NULL OR :query = '' OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.profissional) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Servico> searchServicos(@Param("categoria") String categoria, @Param("query") String query);

    @Query("SELECT DISTINCT s.categoria FROM Servico s")
    List<String> findAllCategorias();

    List<Servico> findByEmailPrestadorOrderByNomeAsc(String emailPrestador);
}
