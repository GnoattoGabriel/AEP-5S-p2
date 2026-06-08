package com.aep.servicos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import com.aep.servicos.model.SolicitacaoStatus;

@Entity
@Table(name = "solicitacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "servico")
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String protocolo;

    @Column(nullable = false)
    private String nomeCliente;

    @Column(nullable = false)
    private String emailCliente;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private String endereco;

    private LocalDate dataAtendimento;
    private LocalTime horarioAtendimento;

    @Enumerated(EnumType.STRING)
    private SolicitacaoStatus status; // PENDENTE, EM_ANDAMENTO, FINALIZADA, CANCELADA

    private String emailPrestador;

    private String telefone;

    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @PrePersist
    protected void onCreate() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = SolicitacaoStatus.PENDENTE;
        }
        if (this.protocolo == null) {
            // Gera protocolo tipo: #PROT-YYYYMMDD-XXXX (4 digitos aleatorios)
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            int randomNum = 1000 + (int) (Math.random() * 9000); // 1000 a 9999
            this.protocolo = "#PROT-" + dateStr + "-" + randomNum;
        }
        if (this.horarioAtendimento == null) {
            // Horário padrão aleatório entre 08:00 e 17:00
            int hour = 8 + (int) (Math.random() * 10);
            this.horarioAtendimento = LocalTime.of(hour, 0);
        }
    }
}
