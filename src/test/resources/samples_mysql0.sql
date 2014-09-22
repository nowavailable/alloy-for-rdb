CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `screen_name` varchar(9) NOT NULL,
  `name` varchar(255) NOT NULL,
  `current_level` int(11) DEFAULT '0' NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_screen_name` (`screen_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user_activities` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `last_logged_in` datetime DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_activities_user_id` (`user_id`),
  CONSTRAINT fk_user_activities_user_id
    FOREIGN KEY (`user_id`)
    REFERENCES `users`(`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `urls` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_urls_url` (`url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `bookmarks` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `memo` varchar(255) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `url_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT fk_bookmarks_user_id
    FOREIGN KEY (`user_id`)
    REFERENCES `users`(`id`)
    ON DELETE CASCADE,
  CONSTRAINT fk_bookmarks_url_id
    FOREIGN KEY (`url_id`)
    REFERENCES `urls`(`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `diaries` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `body` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `photos` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `data` blob,
  `parent_id` int(11) NOT NULL,
  `parent_type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `books` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `isbn` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_books_isbn` (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `authors` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `authors_books` (
  `book_id` int(11) NOT NULL,
  `author_id` int(11) NOT NULL,
  CONSTRAINT fk_authors_books_book_id
    FOREIGN KEY (`book_id`)
    REFERENCES `books`(`id`)
    ON DELETE RESTRICT,
  CONSTRAINT fk_authors_books_author_id
    FOREIGN KEY (`author_id`)
    REFERENCES `authors`(`id`)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `actors` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `movies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `charactors` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `actor_id` int(11) DEFAULT NULL,
  `movie_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT fk_charactors_actor_id
    FOREIGN KEY (`actor_id`)
    REFERENCES `actors`(`id`)
    ON DELETE SET NULL,
  CONSTRAINT fk_charactors_movie_id
    FOREIGN KEY (`movie_id`)
    REFERENCES `movies`(`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--    CREATE TABLE `user_points` (
--      `id` int(11) NOT NULL AUTO_INCREMENT,
--      `user_id` int(11) NOT NULL,
--      `snapshot_level` int(11) NOT NULL,
--      `point_summary_by_level` int(11) NOT NULL,
--      PRIMARY KEY (`id`),
--      CONSTRAINT fk_user_points_user_id_current_level
--        FOREIGN KEY (`user_id`, `snapshot_level`)
--        REFERENCES `users`(`id`, `current_level`)
--        ON DELETE RESTRICT
--    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
--
--        CREATE TABLE `user_points` (
--          `id` int(11) NOT NULL AUTO_INCREMENT,
--          `user_id` int(11) NOT NULL,
--          `snapshot_level` int(11) NOT NULL,
--          `point_summary_by_level` int(11) NOT NULL,
--          PRIMARY KEY (`id`),
--          CONSTRAINT fk_user_points_user_id_current_level
--            FOREIGN KEY (`user_id`)
--            REFERENCES `users`(`id`)
--            ON DELETE RESTRICT
--        ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user_points` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `snapshot_level` int(11) NOT NULL,
  `point_summary_by_level` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_user_points_user_id_current_level` 
    FOREIGN KEY (`user_id`, `snapshot_level`) 
    REFERENCES `users` (`id`, `current_level`)
    ON DELETE RESTRICT
) ;


--drop table authors_books   ;
--drop table authors         ;
--drop table bookmarks       ;
--drop table books           ;
--drop table diaries         ;
--drop table photos          ;
--drop table urls            ;
--drop table user_activities ;
--drop table users           ;
--drop table charactors;
--drop table actors;
--drop table movies;
--drop table user_points;