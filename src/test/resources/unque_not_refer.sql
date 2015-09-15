
CREATE TABLE `families` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `twitter_id` varchar(255) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_families_first_name_last_name` (`first_name`,`last_name`),
  UNIQUE KEY `uq_families_twitter_id` (`twitter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
