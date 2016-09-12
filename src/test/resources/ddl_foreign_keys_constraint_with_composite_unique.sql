DROP TABLE IF EXISTS `items`;
CREATE TABLE `items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `customers`;
CREATE TABLE `customers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `order_details`;
CREATE TABLE `order_details` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `customer_key` int(11) NOT NULL,
  `item_key` int(11) NOT NULL,
  `order_key` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_details_unique` (`customer_key`,`item_key`,`order_key`),
  CONSTRAINT fk_order_details_order
  FOREIGN KEY (`order_key`)
  REFERENCES `orders` (`id`),
  CONSTRAINT fk_order_details_customer
  FOREIGN KEY (`customer_key`)
  REFERENCES `customers` (`id`),
  CONSTRAINT fk_order_details_item
  FOREIGN KEY (`item_key`)
  REFERENCES `items` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
