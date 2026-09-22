package com.alocanote.api.repository;

import com.alocanote.api.model.Notebook;
import com.alocanote.api.model.enums.NotebookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotebookRepository extends JpaRepository<Notebook, Long> {
    List<Notebook> findByStatus(NotebookStatus status);
}
