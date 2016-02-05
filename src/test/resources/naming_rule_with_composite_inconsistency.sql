
DROP TABLE IF EXISTS `actors`;
CREATE TABLE `actors` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
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
