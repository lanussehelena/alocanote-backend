package com.alocanote.api.config;

import com.alocanote.api.model.entity.Location;
import com.alocanote.api.model.entity.Notebook;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.NotebookStatus;
import com.alocanote.api.model.enums.Role;
import com.alocanote.api.repository.LocationRepository;
import com.alocanote.api.repository.NotebookRepository;
import com.alocanote.api.repository.UserRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationRepository locationRepository;
    private final NotebookRepository notebookRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           LocationRepository locationRepository,
                           NotebookRepository notebookRepository,
                           JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.locationRepository = locationRepository;
        this.notebookRepository = notebookRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Garante que o banco H2 local aceite tanto o usuário "sa" (Spring Boot)
     * quanto usuário em branco "" (criado pela ferramenta Database do IntelliJ),
     * evitando erro 28000 (Wrong user name or password) na inicialização.
     */
    @Bean
    public static BeanPostProcessor h2CredentialsSynchronizer() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSourceProperties props) {
                    String url = props.getUrl();
                    if (url != null && url.startsWith("jdbc:h2:file:")) {
                        garantirUsuariosH2(url,
                                props.getUsername() != null ? props.getUsername() : "sa",
                                props.getPassword() != null ? props.getPassword() : "");
                    }
                }
                return bean;
            }
        };
    }

    private static void garantirUsuariosH2(String url, String username, String password) {
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE USER IF NOT EXISTS \"\" PASSWORD '' ADMIN");
        } catch (SQLException ex) {
            // Caso o arquivo .mv.db tenha sido criado pelo IntelliJ sem o usuário SA (erro 28000)
            try (Connection fallbackConn = DriverManager.getConnection(url, "", "");
                 Statement stmt = fallbackConn.createStatement()) {
                stmt.execute("CREATE USER IF NOT EXISTS SA PASSWORD '' ADMIN");
                stmt.execute("ALTER USER SA SET PASSWORD ''");
                stmt.execute("ALTER USER SA ADMIN TRUE");
            } catch (SQLException ignored) {
                // Ignora caso o banco esteja inacessível ou com outra configuração
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        // Sincroniza sequências H2 no início para evitar violações de chave primária
        sincronizarSequencias();
        // --- Seed do usuário Administrador ---
        String adminEmail = "admin@alocanote.com";

        Optional<User> existingAdminOpt = userRepository.findByEmail(adminEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(adminEmail));

        if (existingAdminOpt.isEmpty()) {
            User admin = User.builder()
                    .name("Administrador do Sistema")
                    .email(adminEmail)
                    .phone("83999999999")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMINISTRADOR)
                    .accessLevel("ROLE_ADMIN")
                    .emailVerified(true)
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Usuário ADMINISTRADOR criado com sucesso: " + adminEmail);
        } else {
            User admin = existingAdminOpt.get();
            boolean atualizado = false;
            if (admin.getPassword() == null || admin.getPassword().isBlank()) {
                admin.setPassword(passwordEncoder.encode("admin123"));
                atualizado = true;
            }
            if (admin.getRole() != Role.ADMINISTRADOR) {
                admin.setRole(Role.ADMINISTRADOR);
                atualizado = true;
            }
            if (!"ROLE_ADMIN".equals(admin.getAccessLevel())) {
                admin.setAccessLevel("ROLE_ADMIN");
                atualizado = true;
            }
            if (atualizado) {
                userRepository.save(admin);
                System.out.println("✅ Credenciais do usuário ADMINISTRADOR sincronizadas: " + adminEmail);
            } else {
                System.out.println("ℹ️ Usuário ADMINISTRADOR já existe no banco de dados.");
            }
        }

        // --- Seed dos Locais de Retirada ---
        // Necessário pois o banco H2 é em memória e perde os dados a cada reinício.
        // Sem locais cadastrados, o campo obrigatório "local de retirada" da reserva
        // não consegue ser preenchido no app, impedindo o fluxo de agendamento.
        if (locationRepository.count() == 0) {
            List<Location> locaisIniciais = List.of(
                    criarLocal("Loja", "Loja", "00000-000"),
                    criarLocal("Casa de um colega", "Casa de um colega", "00000-000"),
                    criarLocal("Outro local", "Outro local", "00000-000")
            );
            locationRepository.saveAll(locaisIniciais);
            System.out.println("✅ " + locaisIniciais.size() + " locais de retirada criados com sucesso.");
        } else {
            System.out.println("ℹ️ Locais de retirada já existem no banco de dados.");
        }

        // --- Seed dos Notebooks Iniciais ---
        if (notebookRepository.count() == 0) {
            List<Notebook> notebooksIniciais = List.of(
                    Notebook.builder()
                            .name("Dell Inspiron 15")
                            .modelDescription("Intel i7, 16GB RAM, RTX 3050 - Projetos 3D")
                            .status(NotebookStatus.DISPONIVEL)
                            .build(),
                    Notebook.builder()
                            .name("Dell Latitude 3420")
                            .modelDescription("Intel Core i5, 16GB RAM, SSD 512GB")
                            .status(NotebookStatus.DISPONIVEL)
                            .build(),
                    Notebook.builder()
                            .name("Lenovo ThinkPad E14")
                            .modelDescription("Ryzen 7, 16GB RAM, SSD 512GB")
                            .status(NotebookStatus.DISPONIVEL)
                            .build()
            );
            notebookRepository.saveAll(notebooksIniciais);
            System.out.println("✅ " + notebooksIniciais.size() + " notebooks iniciais criados com sucesso.");
        } else {
            System.out.println("ℹ️ Notebooks já existem no banco de dados.");
        }

        sincronizarSequencias();
    }

    private void sincronizarSequencias() {
        List<String> tabelas = List.of("RESERVATIONS", "TB_USER", "TB_NOTEBOOK", "LOCATIONS");
        for (String tabela : tabelas) {
            try {
                jdbcTemplate.execute(
                        "ALTER TABLE " + tabela + " ALTER COLUMN id RESTART WITH (SELECT COALESCE(MAX(id), 0) + 1 FROM " + tabela + ")"
                );
            } catch (Exception e) {
                // Silencia se a tabela ainda não existir ou banco for diferente de H2
            }
        }
        System.out.println("✅ Sequências de chaves primárias sincronizadas com sucesso.");
    }

    private Location criarLocal(String name, String address, String cep) {
        Location loc = new Location();
        loc.setName(name);
        loc.setAddress(address);
        loc.setCep(cep);
        return loc;
    }
}