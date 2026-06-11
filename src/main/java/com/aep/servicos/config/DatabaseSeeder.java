package com.aep.servicos.config;

import com.aep.servicos.model.Servico;
import com.aep.servicos.model.Solicitacao;
import com.aep.servicos.model.Usuario;
import com.aep.servicos.repository.ServicoRepository;
import com.aep.servicos.repository.SolicitacaoRepository;
import com.aep.servicos.repository.UsuarioRepository;
import com.aep.servicos.model.SolicitacaoStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final String PRESTADOR_EMAIL = "gabrielgnoatto@gmail.com";
    private static final String ADMIN_EMAIL = "admin";
    private static final String ADMIN_CLIENTE_NOME = "Admin Cliente";
    private static final String LOG_SEED_PREFIX = ">> Banco de dados semeado com ";
    private static final String CATEGORIA_REFORMAS = "Reformas";

    private final ServicoRepository servicoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(ServicoRepository servicoRepository, SolicitacaoRepository solicitacaoRepository,
            UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.servicoRepository = servicoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            List<Usuario> usuarios = Arrays.asList(
                    Usuario.builder().nome("Dr. Neymar Junior").email("neymar.junior@gmail.com")
                            .senha(passwordEncoder.encode("senha123")).role("ROLE_CLIENTE").build(),
                    Usuario.builder().nome("Dr. Gabriel Gnoatto").email(PRESTADOR_EMAIL)
                            .senha(passwordEncoder.encode("senha123")).role("ROLE_PRESTADOR").build(),
                    Usuario.builder().nome(ADMIN_CLIENTE_NOME).email(ADMIN_EMAIL)
                            .senha(passwordEncoder.encode(ADMIN_EMAIL)).role("ROLE_CLIENTE").build(),
                    Usuario.builder().nome("Admin Prestador").email(ADMIN_EMAIL)
                            .senha(passwordEncoder.encode(ADMIN_EMAIL)).role("ROLE_PRESTADOR").build());
            usuarioRepository.saveAll(usuarios);
            System.out.println(LOG_SEED_PREFIX + usuarios.size() + " usuários.");
        }

        if (servicoRepository.count() == 0) {
            // Seed Servicos
            List<Servico> servicos = Arrays.asList(
                    Servico.builder().nome("Pintura Residencial e Comercial").categoria(CATEGORIA_REFORMAS)
                            .profissional("Carlos Silva").avaliacao(4.8).valor(350.0).emailPrestador(PRESTADOR_EMAIL)
                            .build(),
                    Servico.builder().nome("Limpeza de Ar Condicionado").categoria("Assistência Técnica")
                            .profissional("Mariana Souza").avaliacao(4.9).valor(120.0).emailPrestador(PRESTADOR_EMAIL)
                            .build(),
                    Servico.builder().nome("Instalação e Manutenção Elétrica").categoria(CATEGORIA_REFORMAS)
                            .profissional("Julio Nogueira").avaliacao(4.7).valor(220.0).emailPrestador(PRESTADOR_EMAIL)
                            .build(),
                    Servico.builder().nome("Formatação e Limpeza de PC/Notebook").categoria("Assistência Técnica")
                            .profissional("Felipe Costa").avaliacao(4.6).valor(180.0).emailPrestador(PRESTADOR_EMAIL)
                            .build(),
                    Servico.builder().nome("Limpeza Residencial Completa (Faxina)").categoria("Limpeza")
                            .profissional("Ana Maria").avaliacao(4.9).valor(160.0).emailPrestador(PRESTADOR_EMAIL)
                            .build(),
                    Servico.builder().nome("Jardinagem, Poda e Paisagismo").categoria(CATEGORIA_REFORMAS)
                            .profissional("Roberto Alves").avaliacao(4.5).valor(250.0).emailPrestador(PRESTADOR_EMAIL)
                            .build(),
                    Servico.builder().nome("Consultoria de Design de Interiores").categoria("Design")
                            .profissional("Paula Mendes").avaliacao(5.0).valor(600.0).emailPrestador(PRESTADOR_EMAIL)
                            .build(),
                    Servico.builder().nome("Adestramento e Comportamento Canino").categoria("Pet")
                            .profissional("Lucas Rocha").avaliacao(4.8).valor(130.0).emailPrestador(PRESTADOR_EMAIL)
                            .build());

            servicoRepository.saveAll(servicos);
            System.out.println(LOG_SEED_PREFIX + servicos.size() + " serviços.");

            // Pegando alguns servicos para as solicitacoes
            Servico s1 = servicoRepository.findAll().get(0); // Pintura
            Servico s2 = servicoRepository.findAll().get(1); // Ar Condicionado
            Servico s3 = servicoRepository.findAll().get(4); // Faxina
            Servico s4 = servicoRepository.findAll().get(2); // Eletrica

            // Seed Solicitacoes
            List<Solicitacao> solicitacoes = Arrays.asList(
                    Solicitacao.builder()
                            .nomeCliente("João Pedro Santos")
                            .emailCliente("joao.pedro@gmail.com")
                            .telefone("(11) 99999-1111")
                            .descricao(
                                    "Gostaria de pintar a sala de estar e um quarto de casal. Já tenho a tinta branca, preciso que o profissional traga as ferramentas.")
                            .endereco("Av. Brasil, 1500 - Bloco B, Apto 402 - Centro")
                            .dataAtendimento(LocalDate.now())
                            .horarioAtendimento(LocalTime.of(9, 0))
                            .status(SolicitacaoStatus.EM_ANDAMENTO)
                            .servico(s1)
                            .emailPrestador(s1.getEmailPrestador())
                            .build(),
                    Solicitacao.builder()
                            .nomeCliente("Maria Clara Oliveira")
                            .emailCliente("mclara@yahoo.com.br")
                            .telefone("(11) 98888-2222")
                            .descricao(
                                    "O ar condicionado do quarto não está gelando bem e está fazendo barulho. Preciso de uma limpeza e revisão geral.")
                            .endereco("Rua das Flores, 84 - Jardim das Oliveiras")
                            .dataAtendimento(LocalDate.now())
                            .horarioAtendimento(LocalTime.of(14, 30))
                            .status(SolicitacaoStatus.PENDENTE)
                            .servico(s2)
                            .emailPrestador(s2.getEmailPrestador())
                            .build(),
                    Solicitacao.builder()
                            .nomeCliente("Rodrigo Souza Lima")
                            .emailCliente("rodrigo.lima@outlook.com")
                            .telefone("(21) 97777-3333")
                            .descricao("Preciso de uma limpeza completa pré-mudança em um apartamento de 2 quartos.")
                            .endereco("Av. Paulista, 2100 - Apto 101 - Cerqueira César")
                            .dataAtendimento(LocalDate.now().plusDays(1))
                            .horarioAtendimento(LocalTime.of(10, 0))
                            .status(SolicitacaoStatus.PENDENTE)
                            .servico(s3)
                            .emailPrestador(s3.getEmailPrestador())
                            .build(),
                    Solicitacao.builder()
                            .nomeCliente("Beatriz Nogueira")
                            .emailCliente("bia.nogueira@hotmail.com")
                            .telefone("(11) 96666-4444")
                            .descricao("Instalação de 3 novas luminárias de teto (spots) na cozinha e sala de jantar.")
                            .endereco("Rua Augusta, 412 - Consolação")
                            .dataAtendimento(LocalDate.now().plusDays(2))
                            .horarioAtendimento(LocalTime.of(16, 0))
                            .status(SolicitacaoStatus.PENDENTE)
                            .servico(s4)
                            .emailPrestador(s4.getEmailPrestador())
                            .build(),
                    Solicitacao.builder()
                            .nomeCliente("Lucas Ferreira Ramos")
                            .emailCliente("lucas.ferreira@gmail.com")
                            .telefone("(31) 95555-5555")
                            .descricao("Troca de fiação do chuveiro que está desarmando o disjuntor constantemente.")
                            .endereco("Rua Sergipe, 895 - Higienópolis")
                            .dataAtendimento(LocalDate.now().minusDays(1))
                            .horarioAtendimento(LocalTime.of(14, 0))
                            .status(SolicitacaoStatus.FINALIZADA)
                            .servico(s4)
                            .emailPrestador(s4.getEmailPrestador())
                            .build(),
                    Solicitacao.builder()
                            .nomeCliente("Patrícia Guedes")
                            .emailCliente("paty.guedes@gmail.com")
                            .telefone("(11) 94444-6666")
                            .descricao("Serviço de pintura externa no muro da frente da residência.")
                            .endereco("Alameda Lorena, 1020 - Cerqueira César")
                            .dataAtendimento(LocalDate.now().minusDays(2))
                            .horarioAtendimento(LocalTime.of(8, 0))
                            .status(SolicitacaoStatus.CANCELADA)
                            .servico(s1)
                            .emailPrestador(s1.getEmailPrestador())
                            .build());

            solicitacaoRepository.saveAll(solicitacoes);
            System.out.println(LOG_SEED_PREFIX + solicitacoes.size() + " solicitações iniciais.");

            // Seed client posts (para job board do prestador)
            Servico clientPost1 = Servico.builder()
                    .nome("Solicitação: Limpeza")
                    .categoria("Limpeza")
                    .profissional(ADMIN_CLIENTE_NOME)
                    .valor(0.0)
                    .avaliacao(0.0)
                    .emailPrestador(null)
                    .build();
            servicoRepository.save(clientPost1);

            Servico clientPost2 = Servico.builder()
                    .nome("Solicitação: Reformas")
                    .categoria(CATEGORIA_REFORMAS)
                    .profissional(ADMIN_CLIENTE_NOME)
                    .valor(0.0)
                    .avaliacao(0.0)
                    .emailPrestador(null)
                    .build();
            servicoRepository.save(clientPost2);

            List<Solicitacao> clientPosts = Arrays.asList(
                    Solicitacao.builder()
                            .nomeCliente(ADMIN_CLIENTE_NOME)
                            .emailCliente(ADMIN_EMAIL)
                            .telefone("(11) 99999-0000")
                            .descricao(
                                    "Preciso de uma faxina completa no apartamento, 3 quartos, sala, cozinha e 2 banheiros.")
                            .endereco("Rua Exemplo, 123 - Centro")
                            .dataAtendimento(LocalDate.now().plusDays(3))
                            .horarioAtendimento(LocalTime.of(9, 0))
                            .status(SolicitacaoStatus.PENDENTE)
                            .servico(clientPost1)
                            .emailPrestador(null)
                            .build(),
                    Solicitacao.builder()
                            .nomeCliente(ADMIN_CLIENTE_NOME)
                            .emailCliente(ADMIN_EMAIL)
                            .telefone("(11) 98888-0000")
                            .descricao("Quero reformar o banheiro: trocar revestimento, pia e vaso sanitário.")
                            .endereco("Av. Principal, 456 - Jardim América")
                            .dataAtendimento(LocalDate.now().plusDays(5))
                            .horarioAtendimento(LocalTime.of(14, 0))
                            .status(SolicitacaoStatus.PENDENTE)
                            .servico(clientPost2)
                            .emailPrestador(null)
                            .build());

            solicitacaoRepository.saveAll(clientPosts);
            System.out.println(LOG_SEED_PREFIX + clientPosts.size() + " client posts.");
        }
    }
}
