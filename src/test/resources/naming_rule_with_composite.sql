
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

DROP TABLE IF EXISTS `charactors`;
CREATE TABLE `charactors` (
  `name` varchar(255) NOT NULL,
  `actor_id` int(11) DEFAULT NULL,
  `movie_id` int(11) DEFAULT NULL,
  UNIQUE KEY `uq_charactors_fkeys` (`actor_id`, `movie_id`),
  KEY `fk_charactors_actor_id` (`actor_id`),
  KEY `fk_charactors_movie_id` (`movie_id`),
  CONSTRAINT `fk_charactors_actor_id` FOREIGN KEY (`actor_id`) REFERENCES `actors` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_charactors_movie_id` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `actor_id` int(11) NOT NULL,
  `movie_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_goods_fkeys` (`actor_id`, `movie_id`),
  UNIQUE KEY `uq_goods_fkeys` (`actor_id`, `movie_id`),
  CONSTRAINT fk_goods_fkeys
    FOREIGN KEY (`actor_id`, `movie_id`)
    REFERENCES `charactors` (`actor_id`, `movie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-12-23 14:58:23
