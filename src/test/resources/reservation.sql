CREATE TABLE `applies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deal_id` int(11) NOT NULL,
  `frame_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `deal_number` int(11) NOT NULL,
  `campaign_id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `room_number` int(11) NOT NULL,
  `from_to_str` varchar(255) NOT NULL,
  `quantity` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_applies_candidate_key` (`deal_number`,`campaign_id`,`shop_id`,`room_number`,`from_to_str`),
  KEY `index_applies_on_deal_id` (`deal_id`),
  KEY `index_applies_on_frame_id` (`frame_id`),
  KEY `index_applies_on_campaign_id_and_user_id` (`campaign_id`,`user_id`),
  KEY `fk_applies_to_frames` (`from_to_str`,`campaign_id`,`shop_id`,`room_number`),
  KEY `fk_applies_to_deals` (`campaign_id`,`deal_number`),
  CONSTRAINT `fk_applies_to_deals` FOREIGN KEY (`campaign_id`, `deal_number`) REFERENCES `deals` (`campaign_id`, `deal_number`),
  CONSTRAINT `fk_applies_to_frames` FOREIGN KEY (`from_to_str`, `campaign_id`, `shop_id`, `room_number`) REFERENCES `frames` (`from_to_str`, `campaign_id`, `shop_id`, `room_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `apply_cancels` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `apply_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_apply_cancels_on_apply_id` (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `campaigns` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `url_key` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `display_started_at` datetime NOT NULL,
  `display_ended_at` datetime NOT NULL,
  `deal_scheduling_type` int(11) NOT NULL DEFAULT '0',
  `limit_by_term` int(11) NOT NULL,
  `term_day_for_limit` int(11) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_campaigns_on_url_key` (`url_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `deals` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `campaign_id` int(11) NOT NULL,
  `deal_number` int(11) NOT NULL,
  `started_at` datetime NOT NULL,
  `ended_at` datetime NOT NULL,
  `lottery_started_at` datetime NOT NULL,
  `resume_datetime` datetime DEFAULT NULL,
  `status` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_deals_on_campaign_id_and_deal_number` (`campaign_id`,`deal_number`),
  KEY `index_deals_on_campaign_id` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `frames` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `room_id` int(11) NOT NULL,
  `from_to_str` varchar(255) NOT NULL,
  `campaign_id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `room_number` int(11) NOT NULL,
  `limit` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_frames_candidate_key` (`from_to_str`,`campaign_id`,`shop_id`,`room_number`),
  KEY `index_frames_on_room_id` (`room_id`),
  KEY `fk_frames_to_rooms` (`campaign_id`,`shop_id`,`room_number`),
  CONSTRAINT `fk_frames_to_rooms` FOREIGN KEY (`campaign_id`, `shop_id`, `room_number`) REFERENCES `rooms` (`campaign_id`, `shop_id`, `room_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `lottery_logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `apply_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_lottery_logs_on_apply_id` (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `lottery_wins` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lottery_log_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_lottery_wins_on_lottery_log_id` (`lottery_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `lottery_works` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sort_key` float NOT NULL,
  `win_times` int(11) NOT NULL DEFAULT '0',
  `deal_id` int(11) NOT NULL,
  `frame_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lottery_works_candidate_key` (`deal_id`,`frame_id`,`win_times`,`sort_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `manage_helps` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `con_name` varchar(255) DEFAULT NULL,
  `act_name` varchar(255) DEFAULT NULL,
  `message` text,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `manage_helps_names_index` (`con_name`,`act_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `manage_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `login` varchar(255) NOT NULL,
  `crypted_password` varchar(255) NOT NULL,
  `password_salt` varchar(255) NOT NULL,
  `persistence_token` varchar(255) NOT NULL,
  `login_count` int(11) NOT NULL DEFAULT '0',
  `last_request_at` datetime DEFAULT NULL,
  `last_login_at` datetime DEFAULT NULL,
  `current_login_at` datetime DEFAULT NULL,
  `last_login_ip` varchar(255) DEFAULT NULL,
  `current_login_ip` varchar(255) DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `index_manage_users_on_deleted_at` (`deleted_at`) USING BTREE,
  KEY `index_manage_users_on_last_request_at` (`last_request_at`) USING BTREE,
  KEY `index_manage_users_on_login` (`login`) USING BTREE,
  KEY `index_manage_users_on_persistence_token` (`persistence_token`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `reservation_cancels` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reservation_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_reservation_cancels_on_reservation_id` (`reservation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `reservations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `frame_id` int(11) NOT NULL,
  `reservation_number` int(11) NOT NULL,
  `lottery_win_id` int(11) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_reservations_on_frame_id_and_reservation_number` (`frame_id`,`reservation_number`),
  UNIQUE KEY `index_reservations_on_lottery_win_id` (`lottery_win_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `rooms` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `campaign_id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `room_number` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_rooms_on_campaign_id_and_shop_id_and_room_number` (`campaign_id`,`shop_id`,`room_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `shops` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `url_key` varchar(50) NOT NULL,
  `active_flag` tinyint(1) DEFAULT '0',
  `started_at` datetime DEFAULT NULL,
  `ended_at` datetime DEFAULT NULL,
  `tel` varchar(20) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `memo` text,
  `deleted_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `free_tag` text,
  `addition_button_text` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_shops_on_deleted_at` (`deleted_at`) USING BTREE,
  KEY `index_shops_on_url_key_and_active_flag` (`url_key`,`active_flag`) USING BTREE,
  KEY `index_shops_on_url_key` (`url_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


