package com.aep.servicos.repository;

import com.aep.servicos.model.Solicitacao;
import com.aep.servicos.model.SolicitacaoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    Optional<Solicitacao> findByProtocolo(String protocolo);

    List<Solicitacao> findByDataAtendimentoOrderByHorarioAtendimentoAsc(LocalDate data);

    List<Solicitacao> findByDataAtendimentoAfterOrderByDataAtendimentoAscHorarioAtendimentoAsc(LocalDate data);

    long countByStatus(SolicitacaoStatus status);

    List<Solicitacao> findByEmailClienteOrderByDataCriacaoDesc(String email);

    List<Solicitacao> findByEmailPrestadorAndStatusNot(String email, SolicitacaoStatus status);

    List<Solicitacao> findByEmailPrestadorIsNullAndStatus(SolicitacaoStatus status);

    @Query("SELECT s FROM Solicitacao s JOIN FETCH s.servico WHERE s.dataAtendimento = :data AND s.emailPrestador = :email AND s.status <> com.aep.servicos.model.SolicitacaoStatus.CANCELADA ORDER BY s.horarioAtendimento ASC")
    List<Solicitacao> findByDataAtendimentoAndEmailPrestador(@Param("data") LocalDate data, @Param("email") String email);

    @Query("SELECT s FROM Solicitacao s JOIN FETCH s.servico WHERE s.dataAtendimento > :data AND s.emailPrestador = :email AND s.status <> com.aep.servicos.model.SolicitacaoStatus.CANCELADA ORDER BY s.dataAtendimento ASC, s.horarioAtendimento ASC")
    List<Solicitacao> findByDataAtendimentoAfterAndEmailPrestador(@Param("data") LocalDate data, @Param("email") String email);
}
