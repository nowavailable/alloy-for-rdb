package com.testdatadesigner.tdalloy.core.io.impl;

import junit.framework.TestCase;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MySQLSchemaParserTest extends TestCase {

    public void testInboundParse() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("/structure.sql");
        //InputStream in = this.getClass().getResourceAsStream("/sampledatas.dump");
        MySQLSchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        ddlSplitter.prepare(in);
        List<String> results = ddlSplitter.getRawTables();

        MySQLSchemaParser parser = new MySQLSchemaParser();
        parser.inboundParse(results);
//        parser.inboundParse(new ArrayList<String>(){{
//            add(
//                //"CREATE TABLE `trn_wholesale_stores` (  `id` int(11) NOT NULL AUTO_INCREMENT,  `name` varchar(255) NOT NULL,  `code` varchar(6) NOT NULL,  `order_number` int(11) DEFAULT NULL,  `created_at` datetime DEFAULT NULL,  `updated_at` datetime DEFAULT NULL,  `deleted_at` datetime DEFAULT NULL,  PRIMARY KEY (`id`),  UNIQUE KEY `index_trn_wholesale_stores_uniq` (`code`,`deleted_at`)) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;"
//                //"CREATE TABLE `point_campaign_games_test` (  `id` int(11) NOT NULL AUTO_INCREMENT,  `point_campaign_item_id` int(11) NOT NULL,  `point_campaign_user_id` int(11) NOT NULL,  `status` int(11) NOT NULL,  `base_user_id` int(11) DEFAULT NULL,  `deleted_at` datetime DEFAULT NULL,  `created_at` datetime NOT NULL,  `updated_at` datetime NOT NULL,  `serial_ids_hash` varchar(255) NOT NULL,  PRIMARY KEY (`id`),  CONSTRAINT cnst_unique_serial_ids_hash UNIQUE (serial_ids_hash),  CONSTRAINT cnst_base_user_id UNIQUE (base_user_id));"
//                //"CREATE TABLE `point_campaign_games_test` (  `id` int(11) NOT NULL AUTO_INCREMENT,  `point_campaign_item_id` int(11) NOT NULL,  `status` int(11) NOT NULL,  `base_user_id` int(11) DEFAULT NULL,  `deleted_at` datetime DEFAULT NULL,  `updated_at` datetime NOT NULL,  `serial_ids_hash` varchar(255) NOT NULL,  CONSTRAINT aa PRIMARY KEY (`id`),  CONSTRAINT cnst_unique_serial_ids_hash UNIQUE (serial_ids_hash),  CONSTRAINT cnst_base_user_id UNIQUE (base_user_id));"
//                //"CREATE TABLE `charactors` (  `id` int NOT NULL ,  `name` varchar(255) NOT NULL,  `actor_id` int DEFAULT NULL,  `movie_id` int DEFAULT NULL,  PRIMARY KEY (`id`) ON DELETE SET NULL)"
//                "CREATE TABLE `bookmarks` (  `id` int(11) NOT NULL AUTO_INCREMENT,  `memo` varchar(255) DEFAULT NULL,  `user_id` int(11) NOT NULL,  `url_id` int(11) NOT NULL,  PRIMARY KEY (`id`),  CONSTRAINT fk_bookmarks_user_id    FOREIGN KEY (`user_id`)    REFERENCES `users`(`id`)    ON DELETE CASCADE,  CONSTRAINT fk_bookmarks_url_id    FOREIGN KEY (`url_id`)    REFERENCES `urls`(`id`)    ON DELETE SET NULL)"
//            );
//        }});

    }

    public void testInboundParseSample() throws Exception {
        //InputStream in = this.getClass().getResourceAsStream("/samples_mysql0.dump");
        InputStream in = this.getClass().getResourceAsStream("/samples_mysql0.sql");
        MySQLSchemaSplitter ddlSplitter = new MySQLSchemaSplitter();
        ddlSplitter.prepare(in);
        List<String> results = ddlSplitter.getRawTables();
        MySQLSchemaParser parser = new MySQLSchemaParser();
        parser.inboundParse(results);
    }
}