/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


DROP DATABASE IF EXISTS `webdb2`;
CREATE DATABASE `webdb2`; 
DROP USER IF EXISTS 'website'@'localhost';
CREATE USER 'website'@'localhost' IDENTIFIED BY 'webpass';
GRANT ALL PRIVILEGES ON webdb2.* TO 'website'@'localhost';

USE `webdb2`;

/* CREAZIONE TABELLE */

CREATE TABLE categoria (
    ID int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    nome varchar(255) UNIQUE NOT NULL,
    padre int(11) DEFAULT NULL,
    CONSTRAINT categoria_padre FOREIGN KEY (padre)
        REFERENCES categoria(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE caratteristica (
    ID int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    nome varchar(255) NOT NULL,
    categoria_id int(11) NOT NULL,
    CONSTRAINT categoria_caratteristica FOREIGN KEY (categoria_id)
        REFERENCES categoria(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE utente (
    ID int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    email varchar(255) UNIQUE NOT NULL,
    password varchar(255) NOT NULL,
    tipologia_utente ENUM('Ordinante', 'Tecnico', 'Amministratore') NOT NULL
);

CREATE TABLE richiesta_ordine (
    ID int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    note varchar(255),
    stato tinyint(1) NOT NULL,
    data DATE NOT NULL,
    codice_richiesta varchar(255) UNIQUE NOT NULL,
    utente int(11) NOT NULL,
    tecnico int(11),
    categoria_id int(11) NOT NULL,
    CONSTRAINT id_utente FOREIGN KEY (utente)
        REFERENCES utente(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT id_tecnico FOREIGN KEY (tecnico)
        REFERENCES utente(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT id_categoria FOREIGN KEY (categoria_id)
        REFERENCES categoria(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE caratteristica_richiesta ( /* "compone" */
    ID int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    richiesta_id int(11) NOT NULL,
    caratteristica_id int(11) NOT NULL,
    valore varchar(200) NOT NULL DEFAULT 'Indifferente',
    CONSTRAINT id_richiesta FOREIGN KEY (richiesta_id)
        REFERENCES richiesta_ordine(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT id_caratteristica FOREIGN KEY (caratteristica_id)
        REFERENCES caratteristica(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE proposta_acquisto (
    ID int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    produttore varchar(500) NOT NULL,
    prodotto varchar(500) NOT NULL,
    codice varchar(500) NOT NULL,
    prezzo float NOT NULL,
    URL text NOT NULL,
    note varchar(255) NOT NULL,
    stato ENUM('Accettato','Rifiutato','In attesa') NOT NULL,
    motivazione text DEFAULT NULL,
    richiesta_id int(11) NOT NULL,
    CONSTRAINT id_richiesta_proposta FOREIGN KEY (richiesta_id)
        REFERENCES richiesta_ordine(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE ordine (
    ID int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    stato ENUM('Accettato','Respinto perché non conforme','Respinto perché non funzionante') NOT NULL,
    proposta_id int(11) NOT NULL,
    CONSTRAINT id_proposta FOREIGN KEY (proposta_id)
        REFERENCES proposta_acquisto(ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


/* FUNCTIONS */

/* per generare un codice alfanumerico automaticamente */
DROP FUNCTION IF EXISTS generate_codice;
DELIMITER $$

CREATE FUNCTION generate_codice()
RETURNS CHAR(10)
DETERMINISTIC
BEGIN
    DECLARE chars CHAR(62) DEFAULT 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    DECLARE result CHAR(10) DEFAULT '';
    DECLARE i INT DEFAULT 0;

    WHILE i < 10 DO
        SET result = CONCAT(result, SUBSTRING(chars, FLOOR(1 + RAND() * 62), 1));
        SET i = i + 1;
    END WHILE;

    RETURN result;
END$$

DELIMITER ;


/* per evitare cicli nella struttura ad albero delle categorie */
DROP FUNCTION IF EXISTS isCyclic;
DELIMITER $$

CREATE FUNCTION isCyclic(new_parent_id INT, current_id INT)
RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE parent INT;
    SET parent = new_parent_id;

    WHILE parent IS NOT NULL DO
        IF parent = current_id THEN
            RETURN TRUE;
        END IF;
        SELECT padre INTO parent FROM categoria WHERE ID = parent LIMIT 1;
    END WHILE;

    RETURN FALSE;
END$$

DELIMITER ;


/* TRIGGERS */

/* per generare codice automaticamente in proposta_acquisto */
DROP TRIGGER IF EXISTS before_insert_proposta;

DELIMITER $$

CREATE TRIGGER before_insert_proposta
BEFORE INSERT ON proposta_acquisto
FOR EACH ROW
BEGIN
    IF NEW.codice IS NULL OR NEW.codice = '' THEN
        SET NEW.codice = generate_codice();
    END IF;
END$$

DELIMITER ;


/* per generare codice automaticamente in richiesta_ordine*/
DROP TRIGGER IF EXISTS before_insert_richiesta;

DELIMITER $$

CREATE TRIGGER before_insert_richiesta
BEFORE INSERT ON richiesta_ordine
FOR EACH ROW
BEGIN
    IF NEW.codice_richiesta IS NULL OR NEW.codice_richiesta = '' THEN
        SET NEW.codice_richiesta = generate_codice();
    END IF;
END$$

DELIMITER ;


/* per evitare cicli nella struttura ad albero delle categorie */
/* insert */
DROP TRIGGER IF EXISTS before_categoria_insert;
DELIMITER $$

CREATE TRIGGER before_categoria_insert
BEFORE INSERT ON categoria
FOR EACH ROW
BEGIN
    IF NEW.padre IS NOT NULL AND isCyclic(NEW.padre, NEW.ID) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cycle detected: Invalid parent_id';
    END IF;
END$$

DELIMITER ;

/* update */
DROP TRIGGER IF EXISTS before_categoria_update;
DELIMITER $$

CREATE TRIGGER before_categoria_update
BEFORE UPDATE ON categoria
FOR EACH ROW
BEGIN
    IF NEW.padre IS NOT NULL AND isCyclic(NEW.padre, NEW.ID) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cycle detected: Invalid parent_id';
    END IF;
END$$

DELIMITER ;


/* POPOLAMENTO TABELLE */

INSERT INTO categoria (nome, padre) VALUES
    ('root', NULL), #1
        ('Informatica', 1), #2
            ('Hardware', 2), #3
                ('Portatili', 3), #4
                ('Desktop', 3), #5
                ('Periferiche', 3), #6
            ('Software', 2), #7
                ('Sistemi operativi', 7), #8
                ('Programmi', 7), #9
        ('Telefonia', 1), #10
            ('Smartphone', 10), #11
            ('Tablet', 10), #12
        ('Console videogiochi', 1), #13
        ('Foto e video', 1), #14
            ('Fotocamere', 14), #15
            ('Videocamere', 14); #16

INSERT INTO caratteristica (nome, categoria_id) VALUES
    /* Portatili */
    ('Marca', 4), #1
    ('Colore', 4), #2
    ('Anno', 4), #3
    ('RAM', 4), #4
    ('Memoria archiviazione', 4), #5
    ('Processore', 4), #6
    ('Scheda video', 4), #7
    ('Sistema operativo', 4), #8
    ('Schermo', 4), #9

    /* Desktop */
    ('Marca', 5), #10
    ('Colore', 5), #11
    ('Anno', 5), #12
    ('RAM', 5), #13
    ('Memoria archiviazione', 5), #14
    ('Processore', 5), #15
    ('Scheda video', 5), #16
    ('Sistema operativo', 5), #17

    /* Periferiche */
    ('Tipologia', 6), #18
    ('Marca', 6), #19
    ('Colore', 6), #20

    /* Sistemi operativi */
    ('Nome', 8), #21
    ('Versione', 8), #22
 
    /* Programmi */
    ('Nome', 9), #23
    ('Versione', 9), #24
    ('Anno', 9), #25

    /* Smartphone */
    ('Marca', 11), #26
    ('Modello', 11), #27
    ('Colore', 11), #28
    ('RAM', 11), #29
    ('Memoria archiviazione', 11), #30
    ('Processore', 11), #31
    ('Sistema operativo', 11), #32
    ('Schermo', 11), #33

    /* Tablet */
    ('Marca', 12), #34
    ('Modello', 12), #35
    ('Colore', 12), #36
    ('RAM', 12), #37
    ('Memoria archiviazione', 12), #38
    ('Processore', 12), #39
    ('Sistema operativo', 12), #40
    ('Schermo', 12), #41

    /* Console videogiochi */
    ('Marca', 13), #42
    ('Modello', 13), #43
    ('Colore', 13), #44
    ('Anno', 13), #45
    ('Schermo', 13), #46
    ('RAM', 13), #47
    ('Memoria archiviazione', 13), #48
    ('Processore', 13), #49

    /* Fotocamere */
    ('Marca', 15), #50
    ('Modello', 15), #51
    ('Colore', 15), #52
    ('Schermo', 15), #53
    ('Batteria', 15), #54
    ('Obiettivo', 15), #55

    /* Videocamere */
    ('Marca', 16), #56
    ('Modello', 16), #57
    ('Colore', 16), #58
    ('Schermo', 16), #59
    ('Batteria', 16), #60
    ('Obiettivo', 16); #61



INSERT INTO utente (email, password, tipologia_utente) VALUES
    ('giulia@example.com', 'giulia', 'Ordinante'), #1
    ('gea@example.com', 'gea', 'Ordinante'), #2
    ('samanta@example.com', 'samanta', 'Ordinante'), #3
    ('tecnico1@example.com', 'tecnico1', 'Tecnico'), #4
    ('tecnico2@example.com', 'tecnico2', 'Tecnico'), #5
    ('tecnico3@example.com', 'tecnico3', 'Tecnico'), #6
    ('admin@example.com', 'admin', 'Amministratore'); #7

INSERT INTO richiesta_ordine (note, stato, data, utente, tecnico, categoria_id) VALUES
    (NULL, 0, '2024-08-06', 1, NULL, 4), /* 1) Giulia -> Portatili */
    ('Vorrei un telefono impermeabile', 1, '2023-01-29', 2, 5, 11), /* 2) Gea -> Tecnico2 -> Smartphone */
    (NULL, 1, '2023-01-29', 3, 4, 13); /* 3) Samanta -> Tecnico1 -> Console videogiochi */

INSERT INTO caratteristica_richiesta (richiesta_id, caratteristica_id, valore) VALUES
    /* 1) Giulia -> Portatili */
    (1, 1, 'Indifferente'), #Marca
    (1, 2, 'Grigio'), #Colore
    (1, 3, 'Indifferente'), #Anno
    (1, 4, '8 GB'), #RAM
    (1, 5, '512 GB'), #Memoria archiviazione
    (1, 6, 'Indifferente'), #Processore
    (1, 7, 'Indifferente'), #Scheda video
    (1, 8, 'Windows'), #Sistema operativo
    (1, 9, 'Indifferente'), #Schermo

    /* Gea -> Smartphone */
    (2, 26, 'Apple'), #Marca
    (2, 27, 'iPhone 15 Pro'), #Modello
    (2, 28, 'Indifferente'), #Colore
    (2, 29, 'Indifferente'), #RAM
    (2, 30, 'Indifferente'), #Memoria archiviazione
    (2, 31, 'Indifferente'), #Processore
    (2, 32, 'Indifferente'), #Sistema operativo
    (2, 33, 'Indifferente'), #Schermo

    /* Samanta -> Console videogiochi */
    (3, 42, 'Nintendo'), #Marca
    (3, 43, 'Switch'), #Modello
    (3, 44, 'Indifferente'), #Colore
    (3, 45, '2023'), #Anno
    (3, 46, 'Indifferente'), #Schermo
    (3, 47, 'Indifferente'), #RAM
    (3, 48, 'Indifferente'), #Memoria archiviazione
    (3, 49, 'Indifferente'); #Processore

INSERT INTO proposta_acquisto (produttore, prodotto, prezzo, URL, note, stato, motivazione, richiesta_id) VALUES
    ('Apple', 'iPhone 15 Pro 256GB Titanio Blu', 1369, 'https://www.apple.com/it/shop/buy-iphone/iphone-15-pro/display-da-6,1%22-256gb-titanio-blu', 'Dal sito potrà tranquillamente cambiare colore o capacità di archiviazione', 'In attesa', NULL, 2), #1
    ('Nintendo', 'Nintendo Switch Modello OLED (bianco)', 349.99, 'https://store.nintendo.it/it/nintendo-switch-modello-oled-bianco-000000000010007454', 'La nuova Switch con schermo OLED', 'Rifiutato', 'Troppo vecchia, è uscita nel 2021, la voglio più nuova', 3), #2
    ('Nintendo', 'Nintendo Switch Modello OLED edizione speciale Mario (rossa)', 349.99, 'https://store.nintendo.it/it/nintendo-switch-modello-oled-edizione-speciale-mario-rossa-000000000010011772', 'Questa è la versione speciale Mario, è tutta rossa ed è uscita a fine 2023!', 'Accettato', NULL, 3); #3

INSERT INTO ordine (stato, proposta_id) VALUES
    ('Accettato' , 3); #1