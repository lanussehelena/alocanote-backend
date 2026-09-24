-- 1. Criação da tabela de Utilizadores
CREATE TABLE tb_user (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) UNIQUE NOT NULL,
                         phone VARCHAR(255) UNIQUE NOT NULL,
                         role_enum VARCHAR(50) NOT NULL,
                         custom_role VARCHAR(255),
                         access_level VARCHAR(50) NOT NULL,
                         profile_picture_url VARCHAR(500),
                         street VARCHAR(255),
                         number VARCHAR(50),
                         neighborhood VARCHAR(255),
                         city VARCHAR(255),
                         state VARCHAR(100),
                         zip_code VARCHAR(20),
                         created_at TIMESTAMP
);

-- 2. Criação da tabela de Notebooks (Alinhada com o Enum e tamanhos)
CREATE TABLE tb_notebook (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(255) NOT NULL,
                             model_description VARCHAR(255),
                             status VARCHAR(50) NOT NULL,
                             created_at TIMESTAMP
);

-- 3. Criação da tabela de Localizações
CREATE TABLE locations (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           address VARCHAR(255)
);

-- 4. Criação da tabela de Reservas
CREATE TABLE reservations (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              purpose VARCHAR(255),
                              status VARCHAR(50) NOT NULL,
                              departure_date_time TIMESTAMP(6),
                              return_date_time TIMESTAMP(6),
                              user_id BIGINT,
                              location_id BIGINT,
                              CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES tb_user(id),
                              CONSTRAINT fk_reservation_location FOREIGN KEY (location_id) REFERENCES locations(id)
);