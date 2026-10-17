package com.alocanote.api.repository;

import com.alocanote.api.model.entity.Location;
import com.alocanote.api.model.entity.Notebook;
import com.alocanote.api.model.entity.Reservation;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.NotebookStatus;
import com.alocanote.api.model.enums.ReservationStatus;
import com.alocanote.api.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Notebook notebook;
    private User user;
    private Location location;
    private final List<ReservationStatus> activeStatuses = List.of(ReservationStatus.AGENDADO, ReservationStatus.CHECKED_IN);

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("Maria Silva")
                .email("maria@alocanote.com")
                .phone("83988887777")
                .password("hash-senha")
                .role(Role.CONSULTOR_VENDAS)
                .accessLevel("ROLE_USER")
                .build();
        entityManager.persist(user);

        notebook = Notebook.builder()
                .name("Notebook 01 - Dell")
                .modelDescription("Dell Latitude 3420")
                .status(NotebookStatus.DISPONIVEL)
                .build();
        entityManager.persist(notebook);

        location = new Location();
        location.setName("Showroom Principal");
        location.setAddress("Av. Epitácio Pessoa, 1000");
        location.setCep("58000-000");
        entityManager.persist(location);

        entityManager.flush();
    }

    private Reservation persistirReserva(LocalDateTime departure, LocalDateTime arrival, ReservationStatus status) {
        Reservation r = Reservation.builder()
                .notebook(notebook)
                .user(user)
                .location(location)
                .departureDateTime(departure)
                .returnDateTime(arrival)
                .purpose("Uso em apresentação")
                .status(status)
                .build();
        entityManager.persist(r);
        entityManager.flush();
        return r;
    }

    @Test
    @DisplayName("Deve detectar conflito quando existe reserva AGENDADA com horários sobrepostos")
    void shouldFindConflictWhenOverlappingWithAgendado() {
        LocalDateTime base = LocalDateTime.of(2026, 11, 10, 10, 0);
        persistirReserva(base, base.plusHours(2), ReservationStatus.AGENDADO); // 10:00 às 12:00

        // Nova tentativa: 11:00 às 13:00
        List<Reservation> conflitos = reservationRepository.findConflictingReservations(
                notebook.getId(), base.plusHours(1), base.plusHours(3), activeStatuses
        );

        assertEquals(1, conflitos.size());
    }

    @Test
    @DisplayName("Deve detectar conflito quando existe reserva CHECKED_IN com horários sobrepostos")
    void shouldFindConflictWhenOverlappingWithCheckedIn() {
        LocalDateTime base = LocalDateTime.of(2026, 11, 10, 14, 0);
        persistirReserva(base, base.plusHours(3), ReservationStatus.CHECKED_IN); // 14:00 às 17:00

        // Nova tentativa: 15:00 às 16:00 (dentro do intervalo)
        List<Reservation> conflitos = reservationRepository.findConflictingReservations(
                notebook.getId(), base.plusHours(1), base.plusHours(2), activeStatuses
        );

        assertEquals(1, conflitos.size());
    }

    @Test
    @DisplayName("Não deve considerar conflito quando a reserva existente estiver CANCELADA ou FINALIZADA")
    void shouldNotConflictWithCancelledOrFinishedReservations() {
        LocalDateTime base = LocalDateTime.of(2026, 11, 10, 8, 0);
        persistirReserva(base, base.plusHours(2), ReservationStatus.CANCELADO);
        persistirReserva(base.plusHours(2), base.plusHours(4), ReservationStatus.FINALIZADO);

        // Nova tentativa no mesmo horário da cancelada (08:00 às 10:00)
        List<Reservation> conflitos = reservationRepository.findConflictingReservations(
                notebook.getId(), base, base.plusHours(2), activeStatuses
        );

        assertTrue(conflitos.isEmpty());
    }

    @Test
    @DisplayName("Não deve considerar conflito quando os intervalos forem estritamente contíguos (adjacentes)")
    void shouldNotConflictWhenIntervalsAreContiguous() {
        LocalDateTime base = LocalDateTime.of(2026, 11, 10, 9, 0);
        persistirReserva(base, base.plusHours(2), ReservationStatus.AGENDADO); // 09:00 às 11:00

        // Nova tentativa adjacente posterior: começa exatamente às 11:00 (quando a anterior termina)
        List<Reservation> conflitosPosterior = reservationRepository.findConflictingReservations(
                notebook.getId(), base.plusHours(2), base.plusHours(4), activeStatuses
        );
        assertTrue(conflitosPosterior.isEmpty());

        // Nova tentativa adjacente anterior: termina exatamente às 09:00 (quando a existente começa)
        List<Reservation> conflitosAnterior = reservationRepository.findConflictingReservations(
                notebook.getId(), base.minusHours(2), base, activeStatuses
        );
        assertTrue(conflitosAnterior.isEmpty());
    }
}
