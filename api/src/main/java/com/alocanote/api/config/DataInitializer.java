package com.alocanote.api.config;

import com.alocanote.api.model.entity.Location;
import com.alocanote.api.model.entity.Notebook;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.NotebookStatus;
import com.alocanote.api.model.enums.Role;
import com.alocanote.api.repository.LocationRepository;
import com.alocanote.api.repository.NotebookRepository;
import com.alocanote.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationRepository locationRepository;
    private final NotebookRepository notebookRepository;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           LocationRepository locationRepository,
                           NotebookRepository notebookRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.locationRepository = locationRepository;
        this.notebookRepository = notebookRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // --- Seed do usuário Administrador ---
        String adminEmail = "admin@alocanote.com";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .name("Administrador do Sistema")
                    .email(adminEmail)
                    .phone("83999999999")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMINISTRADOR)
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Usuário ADMINISTRADOR criado com sucesso: " + adminEmail);
        } else {
            System.out.println("ℹ️ Usuário ADMINISTRADOR já existe no banco de dados.");
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
    }

    private Location criarLocal(String name, String address, String cep) {
        Location loc = new Location();
        loc.setName(name);
        loc.setAddress(address);
        loc.setCep(cep);
        return loc;
    }
}