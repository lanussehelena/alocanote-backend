package com.alocanote.api.service;

import com.alocanote.api.dto.request.CreateNotebookRequestDTO;
import com.alocanote.api.dto.response.NotebookResponseDTO;
import com.alocanote.api.exception.BusinessException;
import com.alocanote.api.exception.ResourceNotFoundException;
import com.alocanote.api.model.entity.Notebook;
import com.alocanote.api.model.entity.Reservation;
import com.alocanote.api.model.entity.User;
import com.alocanote.api.model.enums.NotebookStatus;
import com.alocanote.api.model.enums.Role;
import com.alocanote.api.repository.NotebookRepository;
import com.alocanote.api.repository.ReservationRepository;
import com.alocanote.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotebookService {

    private final NotebookRepository notebookRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public NotebookService(NotebookRepository notebookRepository) {
        this(notebookRepository, null, null);
    }

    @Autowired
    public NotebookService(NotebookRepository notebookRepository,
                           ReservationRepository reservationRepository,
                           UserRepository userRepository) {
        this.notebookRepository = notebookRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<NotebookResponseDTO> findAllDTOs() {
        return notebookRepository.findAll().stream()
                .map(NotebookResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Notebook> findAll() {
        return notebookRepository.findAll();
    }

    @Transactional
    public NotebookResponseDTO createNotebook(CreateNotebookRequestDTO dto) {
        return createNotebook(dto, null);
    }

    @Transactional
    public NotebookResponseDTO createNotebook(CreateNotebookRequestDTO dto, String requesterEmail) {
        validarPermissaoAdmin(requesterEmail, "Acesso negado: apenas o administrador pode cadastrar novos notebooks.");

        Notebook notebook = Notebook.builder()
                .name(dto.name())
                .modelDescription(dto.modelDescription())
                .status(dto.status() != null ? dto.status() : NotebookStatus.DISPONIVEL)
                .build();

        Notebook saved = notebookRepository.save(notebook);
        return NotebookResponseDTO.fromEntity(saved);
    }

    @Transactional
    public Notebook createNotebook(Notebook notebook) {
        return notebookRepository.save(notebook);
    }

    @Transactional(readOnly = true)
    public Notebook findById(Long id) {
        return notebookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public NotebookResponseDTO findDtoById(Long id) {
        return NotebookResponseDTO.fromEntity(findById(id));
    }

    @Transactional
    public void updateStatus(Long notebookId, NotebookStatus status) {
        Notebook notebook = findById(notebookId);
        notebook.setStatus(status);
        notebookRepository.save(notebook);
    }

    @Transactional
    public void deleteNotebook(Long id) {
        deleteNotebook(id, null);
    }

    @Transactional
    public void deleteNotebook(Long id, String requesterEmail) {
        validarPermissaoAdmin(requesterEmail, "Acesso negado: apenas o administrador pode excluir notebooks.");

        Notebook notebook = findById(id);

        if (reservationRepository != null) {
            List<Reservation> reservasVinculadas = reservationRepository.findByNotebookId(id);
            if (!reservasVinculadas.isEmpty()) {
                reservationRepository.deleteAll(reservasVinculadas);
            }
        }

        notebookRepository.delete(notebook);
    }

    private void validarPermissaoAdmin(String requesterEmail, String errorMessage) {
        if (requesterEmail == null || requesterEmail.isBlank() || userRepository == null) {
            return;
        }
        String normalizedEmail = requesterEmail.trim().toLowerCase();
        if ("admin@alocanote.com".equalsIgnoreCase(normalizedEmail)) {
            return;
        }
        User requester = userRepository.findByEmail(normalizedEmail)
                .or(() -> userRepository.findByEmailIgnoreCase(normalizedEmail))
                .orElseThrow(() -> new BusinessException(errorMessage));

        boolean isAdmin = requester.getRole() == Role.ADMINISTRADOR
                || "ROLE_ADMIN".equalsIgnoreCase(requester.getAccessLevel());

        if (!isAdmin) {
            throw new BusinessException(errorMessage);
        }
    }
}
