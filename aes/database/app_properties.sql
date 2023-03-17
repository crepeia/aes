CREATE TABLE `aes`.`app_properties` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `latest_app_version` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO `aes`.`app_properties`
(`id`, `latest_app_version`)
VALUES
(1, "1.5.1");
