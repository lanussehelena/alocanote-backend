-- Criação da tabela de Utilizadores
CREATE TABLE tb_user (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) UNIQUE NOT NULL,
                         access_level VARCHAR(50)
);

-- Criação da tabela de Notebooks
CREATE TABLE tb_notebook (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(255) NOT NULL,
                             model_description VARCHAR(500),
                             status VARCHAR(50),
                             created_at TIMESTAMP
);

-- Criação da tabela de Localizações
CREATE TABLE locations (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           address VARCHAR(255)
);

-- Criação da tabela de Reservas
CREATE TABLE reservations (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              purpose VARCHAR(255),
                              status VARCHAR(50),
                              departure_date_time TIMESTAMP,
                              return_date_time TIMESTAMP,
                              user_id BIGINT,
                              location_id BIGINT,
                              CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES tb_user(id),
                              CONSTRAINT fk_reservation_location FOREIGN KEY (location_id) REFERENCES locations(id)
);