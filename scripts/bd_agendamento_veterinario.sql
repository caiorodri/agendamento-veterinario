CREATE DATABASE IF NOT EXISTS agendamento_veterinario;

USE agendamento_veterinario;

CREATE TABLE estado (
    sigla CHAR(2) NOT NULL PRIMARY KEY,
    nome VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO estado (nome, sigla) VALUES ('Acre', 'AC'),
 ('Alagoas', 'AL'),
 ('Amazonas', 'AM'),
 ('Amapá', 'AP'),
 ('Bahia', 'BA'),
 ('Ceará', 'CE'),
 ('Distrito Federal', 'DF'),
 ('Espírito Santo', 'ES'),
 ('Goiás', 'GO'),
 ('Maranhão', 'MA'),
 ('Minas Gerais', 'MG'),
 ('Mato Grosso do Sul', 'MS'),
 ('Mato Grosso', 'MT'),
 ('Pará', 'PA'),
 ('Paraíba', 'PB'),
 ('Pernambuco', 'PE'),
 ('Piauí', 'PI'),
 ('Paraná', 'PR'),
 ('Rio de Janeiro', 'RJ'),
 ('Rio Grande do Norte', 'RN'),
 ('Rondônia', 'RO'),
 ('Roraima', 'RR'),
 ('Rio Grande do Sul', 'RS'),
 ('Santa Catarina', 'SC'),
 ('Sergipe', 'SE'),
 ('São Paulo', 'SP'),
 ('Tocantins', 'TO');
 
 CREATE TABLE perfil (
	id INT PRIMARY KEY,
    nome VARCHAR(15) NOT NULL
);
 
 INSERT INTO perfil VALUES (1, "Cliente"), (2, "Recepcionista"), (3, "Veterinario"), (4, "Administrador");
 
CREATE TABLE usuario_status(
    id INT NOT NULL PRIMARY KEY,
    nome VARCHAR(20) NOT NULL
);

INSERT INTO usuario_status(id, nome) VALUES
(1, "Ativo"),
(2, "Inativo");
 
CREATE TABLE usuario (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(30) NOT NULL,
    senha VARCHAR(100) NOT NULL,
    cpf CHAR(11) NOT NULL,
    email VARCHAR(80) NOT NULL UNIQUE,
    logradouro VARCHAR(50) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    complemento VARCHAR(40),
    cidade VARCHAR(40) NOT NULL,
    sigla_estado CHAR(2) NOT NULL,
    cep CHAR(8) NOT NULL,
    codigo_recuperacao CHAR(5),
    expiracao_codigo DATETIME,
    data_nascimento DATE,
    id_status INT NOT NULL,
    id_perfil INT NOT NULL,
    email_realizar_consulta_recebido BOOLEAN DEFAULT FALSE,
    receber_email BOOLEAN DEFAULT FALSE,
    CONSTRAINT usuario_estado_fk FOREIGN KEY (sigla_estado) REFERENCES estado(sigla),
    CONSTRAINT usuario_status_fk FOREIGN KEY (id_status) REFERENCES usuario_status(id),
    CONSTRAINT usuario_perfil_fk FOREIGN KEY (id_perfil) REFERENCES perfil(id)
);

CREATE TABLE usuario_telefone (
	id_usuario BIGINT NOT NULL,
    telefone VARCHAR(30),
    CONSTRAINT usuario_telefone_fk FOREIGN KEY (id_usuario) REFERENCES usuario(id),
    CONSTRAINT usuario_telefone_pk PRIMARY KEY (id_usuario, telefone)
);

CREATE TABLE animal_especie (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
);

INSERT INTO animal_especie (nome) VALUES
('Cachorro'),
('Gato'),
('Ave'),
('Coelho'),
('Tartaruga'),
('Peixe'),
('Cavalo'),
('Porquinho-da-índia'),
('Réptil'),
('Rato'),
('Furão'),
('Hamster'),
('Outros');

CREATE TABLE animal_raca (

	id INT PRIMARY KEY AUTO_INCREMENT,
    id_especie INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    CONSTRAINT animal_raca_especie_fk FOREIGN KEY (id_especie) REFERENCES animal_especie (id)

);

INSERT INTO animal_raca (id_especie, nome) VALUES
(1, 'Desconhecido'),
(1, 'Vira Lata'),
(1, 'Pastor Alemão'),
(1, 'Poodle'),
(1, 'Labrador Retriever'),
(1, 'Bulldog Francês'),
(1, 'Golden Retriever'),
(1, 'Chihuahua'),
(1, 'Beagle'),
(1, 'Shih Tzu'),
(1, 'Dachshund'),
(1, 'Boxer'),
(1, 'Rottweiler'),
(2, 'Desconhecido'),
(2, 'Siamês'),
(2, 'Persa'),
(2, 'Maine Coon'),
(2, 'Angorá'),
(2, 'Sphynx'),
(2, 'British Shorthair'),
(2, 'Bengal'),
(2, 'Ragdoll'),
(3, 'Desconhecido'),
(3, 'Periquito'),
(3, 'Calopsita'),
(3, 'Papagaio'),
(3, 'Canário'),
(3, 'Arara'),
(4, 'Desconhecido'),
(4, 'Mini Lop'),
(4, 'Lionhead'),
(4, 'Rex'),
(4, 'Angorá Inglês'),
(5, 'Desconhecido'),
(6, 'Desconhecido'),
(6, 'Betta'),
(6, 'Goldfish'),
(6, 'Tetra Neon'),
(7, 'Desconhecido'),
(7, 'Mangalarga Marchador'),
(7, 'Puro Sangue Árabe'),
(8, 'Desconhecido'),
(9, 'Desconhecido'),
(10, 'Desconhecido'),
(11, 'Desconhecido'),
(12, 'Desconhecido'),
(12, 'Hamster Sírio'),
(12, 'Hamster Anão Russo'),
(13, 'Desconhecido');

CREATE TABLE animal_sexo (

    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(15) NOT NULL

);

INSERT INTO animal_sexo (nome) VALUES
('Macho'),
('Femea'),
('Desconhecido');

CREATE TABLE IF NOT EXISTS animal (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_raca INT NOT NULL,
    id_sexo INT NOT NULL,
    id_dono BIGINT NOT NULL,
    nome VARCHAR(40),
    data_nascimento DATE NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    castrado BOOLEAN DEFAULT FALSE,
    peso FLOAT,
    altura FLOAT,
    cor VARCHAR(50),
    CONSTRAINT animal_sexo_fk FOREIGN KEY (id_sexo) REFERENCES animal_sexo(id),
    CONSTRAINT animal_raca_fk FOREIGN KEY (id_raca) REFERENCES animal_raca(id),
    CONSTRAINT animal_usuario_fk FOREIGN KEY (id_dono) REFERENCES usuario(id)
);

CREATE TABLE agendamento_status(
    id INT NOT NULL PRIMARY KEY,
    nome VARCHAR(20) NOT NULL
);

INSERT INTO agendamento_status(id, nome) VALUES
(1, "Aberto"),
(2, "Cancelado"),
(3, "Concluido");

CREATE TABLE agendamento_tipo (

	id INT PRIMARY KEY,
    nome VARCHAR(50)

);

INSERT INTO agendamento_tipo VALUES (1, 'Consulta'), (2, 'Cirurgia');

CREATE TABLE agendamento (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_animal BIGINT NOT NULL,
    id_cliente BIGINT NOT NULL,
    id_veterinario BIGINT NOT NULL,
    id_recepcionista BIGINT NOT NULL,
    id_agendamento_status INT NOT NULL,
    id_agendamento_tipo INT NOT NULL,
    data_criacao DATE NOT NULL,
    data_agendamento_inicio DATE NOT NULL,
    data_agendamento_final DATE NOT NULL,
    descricao VARCHAR(255),
    CONSTRAINT agendamento_animal_fk FOREIGN KEY (id_animal) REFERENCES animal(id),
    CONSTRAINT agendamento_cliente_fk FOREIGN KEY (id_cliente) REFERENCES usuario(id),
    CONSTRAINT agendamento_veterinario_fk FOREIGN KEY (id_veterinario) REFERENCES usuario(id),
    CONSTRAINT agendamento_recepcionista_fk FOREIGN KEY (id_recepcionista) REFERENCES usuario(id),
    CONSTRAINT agendamento_agendamento_status_fk FOREIGN KEY (id_agendamento_status) REFERENCES agendamento_status(id),
    CONSTRAINT agendamento_agendamento_tipo_fk FOREIGN KEY (id_agendamento_tipo) REFERENCES agendamento_tipo(id)
);
