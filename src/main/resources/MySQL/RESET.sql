-- region DROP
-- region Reddits
DROP TABLE IF EXISTS BootResponses;
DROP TABLE IF EXISTS BirdResponses;
DROP TABLE IF EXISTS CatResponses;
DROP TABLE IF EXISTS DogResponses;
DROP TABLE IF EXISTS EightBallResponses;

DROP TABLE IF EXISTS Subreddits;
-- endregion
-- region Images
DROP TABLE IF EXISTS BootImages;
DROP TABLE IF EXISTS ExtraGoodDogs;
DROP TABLE IF EXISTS Reacts;
-- endregion
-- region Quotes
DROP TABLE IF EXISTS Quotes;
-- endregion
-- region Messages
DROP TABLE IF EXISTS Messages;
DROP TABLE IF EXISTS Humans;
DROP TABLE IF EXISTS Images;
DROP TABLE IF EXISTS Threads;
-- endregion
-- endregion

-- region CREATE
-- region Messages
CREATE TABLE Humans (
  ID   INT(8) NOT NULL AUTO_INCREMENT,
  name TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE Images (
  ID  INT(8) NOT NULL AUTO_INCREMENT,
  url TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE Threads (
  ID   INT(8) NOT NULL AUTO_INCREMENT,
  name TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE Messages (
  thread_id INT(8) NOT NULL,
  ID        INT(8) NOT NULL,
  sender_id INT(8) NOT NULL,
  date      DATE   NOT NULL,
  message   TEXT,
  image_id  INT(8),

  PRIMARY KEY (ID, thread_id),
  FOREIGN KEY (thread_id) REFERENCES Threads (ID),
  FOREIGN KEY (sender_id) REFERENCES Humans (ID),
  FOREIGN KEY (image_id) REFERENCES Images (ID)
);
-- endregion
-- region Quotes
CREATE TABLE Quotes (
  ID        INT(8) NOT NULL,
  thread_id INT(8) NOT NULL,

  PRIMARY KEY (ID, thread_id),
  FOREIGN KEY (ID, thread_id) REFERENCES Messages (ID, thread_id)
);
-- endregion
-- region Images
CREATE TABLE BootImages (
  ID       INT(8) NOT NULL AUTO_INCREMENT,
  image_id INT(8) NOT NULL,

  PRIMARY KEY (ID),
  FOREIGN KEY (image_id) REFERENCES Images (ID)
);

CREATE TABLE ExtraGoodDogs (
  ID       INT(8) NOT NULL AUTO_INCREMENT,
  image_id INT(8) NOT NULL,

  PRIMARY KEY (ID),
  FOREIGN KEY (image_id) REFERENCES Images (ID)
);

CREATE TABLE Reacts (
  ID       INT(8) NOT NULL AUTO_INCREMENT,
  image_id INT(8) NOT NULL,

  PRIMARY KEY (ID),
  FOREIGN KEY (image_id) REFERENCES Images (ID)
);

-- endregion
-- region Reddits
CREATE TABLE BootResponses (
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE BirdResponses (
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE CatResponses (
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE DogResponses (
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE EightBallResponses (
  ID      INT(8) NOT NULL AUTO_INCREMENT,
  message TEXT   NOT NULL,

  PRIMARY KEY (ID)
);

CREATE TABLE Subreddits (
  ID   INT(8) NOT NULL AUTO_INCREMENT,
  type TEXT   NOT NULL,
  link TEXT   NOT NULL,

  PRIMARY KEY (ID)
);
-- endregion
-- endregion