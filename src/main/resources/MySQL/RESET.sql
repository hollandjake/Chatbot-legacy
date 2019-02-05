-- region DROP
-- region Boot
DROP TABLE IF EXISTS BootResponses;
DROP TABLE IF EXISTS BootImages;
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
);

CREATE TABLE Humans
(
  ID   INT(8) NOT NULL AUTO_INCREMENT,
  url  TEXT   NOT NULL,
  name TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE Messages
(
  ID        INT(8) NOT NULL AUTO_INCREMENT,
  sender_id INT(8) NOT NULL,
  date      DATE   NOT NULL,
  message   TEXT,
  image_id  INT(8),

  PRIMARY KEY (ID),
  FOREIGN KEY (sender_id) REFERENCES Humans (ID),
  FOREIGN KEY (image_id) REFERENCES Images (ID)
);
-- endregion
-- region Boot
CREATE TABLE BootImages
(
  ID       INT(8) NOT NULL AUTO_INCREMENT,
  image_id INT(8) NOT NULL,

  PRIMARY KEY (ID),
  FOREIGN KEY (image_id) REFERENCES Images (ID)
);

CREATE TABLE BootResponses
(
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);
-- endregion
-- endregion