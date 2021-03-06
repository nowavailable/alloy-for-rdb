DROP TABLE IF EXISTS `actors`;
CREATE TABLE `actors` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `characters`;
CREATE TABLE `characters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `actor_key` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT fk_characters
  FOREIGN KEY (`actor_key`)
  REFERENCES `actors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
