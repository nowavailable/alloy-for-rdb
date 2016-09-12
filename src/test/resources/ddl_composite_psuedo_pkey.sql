
DROP TABLE IF EXISTS `actors`;
CREATE TABLE `actors` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `movies`;
CREATE TABLE `movies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `characters`;
CREATE TABLE `characters` (
  `name` varchar(255) NOT NULL,
  `actor_id` int(11) NOT NULL,
  `movie_id` int(11) NOT NULL,
  UNIQUE KEY `uq_characters_fkeys` (`actor_id`, `movie_id`),
  KEY `fk_characters_actor_id` (`actor_id`),
  KEY `fk_characters_movie_id` (`movie_id`),
  CONSTRAINT `fk_characters_actor_id` FOREIGN KEY (`actor_id`) REFERENCES `actors` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_characters_movie_id` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
