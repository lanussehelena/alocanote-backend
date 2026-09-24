ALTER TABLE reservations
    ADD COLUMN notebook_id BIGINT;

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservation_notebook
        FOREIGN KEY (notebook_id)
            REFERENCES tb_notebook(id);