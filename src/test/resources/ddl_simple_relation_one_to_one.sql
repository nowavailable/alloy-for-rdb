DROP TABLE IF EXISTS `cups`;
CREATE TABLE `cups` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `saucers`;
CREATE TABLE `saucers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cup_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_saucers_fkey` (`cup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
