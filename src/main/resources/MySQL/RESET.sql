-- region DROP
-- region Boot
DROP TABLE IF EXISTS BootResponses;
-- endregion
-- region Messages
DROP TABLE IF EXISTS Messages;
DROP TABLE IF EXISTS Humans;
DROP TABLE IF EXISTS Images;
-- endregion
-- endregion

-- region CREATE
-- region Messages
CREATE TABLE Images
(
  ID  INT(8) NOT NULL AUTO_INCREMENT,
  url TEXT   NOT NULL,

  PRIMARY KEY (ID)
) COLLATE utf8mb4_general_ci;

CREATE TABLE Humans
(
  ID   INT(8) NOT NULL AUTO_INCREMENT,
  url  TEXT   NOT NULL,
  name TEXT   NOT NULL,

  PRIMARY KEY (ID)
) COLLATE utf8mb4_general_ci;

CREATE TABLE Messages
(
  ID        INT(8) NOT NULL AUTO_INCREMENT,
  sender_id INT(8) NOT NULL,
  date      DATE   NOT NULL,
  message   TEXT COLLATE utf8mb4_bin,

  PRIMARY KEY (ID),
  FOREIGN KEY (sender_id) REFERENCES Humans (ID)
) COLLATE utf8mb4_general_ci;
-- endregion
-- region Boot
CREATE TABLE BootResponses
(
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
) COLLATE utf8mb4_general_ci;
-- endregion
-- endregion

-- region Procedures
DROP PROCEDURE IF EXISTS SaveMessage;
DROP PROCEDURE IF EXISTS SaveHuman;
DROP PROCEDURE IF EXISTS SaveImage;

CREATE PROCEDURE SaveMessage(IN senderID INT(8), IN dt date, IN messageContent text COLLATE utf8mb4_general_ci)
BEGIN
  INSERT INTO Messages (sender_id, date, message) VALUES (senderID, dt, messageContent);
  SELECT M.ID      as M_ID,
         H.ID      as H_ID,
         H.url     as H_url,
         H.name    as H_name,
         M.date    as M_date,
         M.message as M_message
  FROM Messages M
         JOIN Humans H
  WHERE M.ID = last_insert_id();
END;

CREATE PROCEDURE SaveHuman(IN humanUrl text, IN humanName text)
BEGIN
  -- Only Check against url since this is unique
  DECLARE humanID INT(8) DEFAULT (SELECT H.ID FROM Humans H WHERE H.url = humanUrl);

  IF (ISNULL(humanID)) THEN
    INSERT INTO Humans (url, name) VALUES (humanUrl, humanName);
    SET humanID = LAST_INSERT_ID();
  END IF;

  SELECT H.ID   as H_ID,
         H.url  as H_url,
         H.name as H_name
  FROM Humans H
  WHERE H.ID = humanID
  LIMIT 1;
END;

CREATE PROCEDURE SaveImage(IN imageUrl text)
BEGIN
  -- Only Check against url since this is unique
  DECLARE imageID INT(8) DEFAULT (SELECT I.ID FROM Images I WHERE I.url = imageUrl);

  IF (ISNULL(imageID)) THEN
    INSERT INTO Images (url) VALUES (imageUrl);
    SET imageID = LAST_INSERT_ID();
  END IF;

  SELECT I.ID  as I_ID,
         I.url as I_url
  FROM Images I
  WHERE I.ID = imageID
  LIMIT 1;
END;
-- endregion

-- region preset data
INSERT INTO Humans (url, name)
VALUES ('100029054943415', 'John Smith');
-- endregion