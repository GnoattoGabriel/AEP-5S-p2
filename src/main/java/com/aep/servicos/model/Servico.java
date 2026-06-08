package com.aep.servicos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "servicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String categoria;
    private String profissional;
    private Double avaliacao;
    private Double valor;
    private String emailPrestador;
}
