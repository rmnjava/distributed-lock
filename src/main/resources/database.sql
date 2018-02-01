use distributed_lock;

CREATE TABLE user(
  id int auto_increment,
  name varchar(100) NOT NULL DEFAULT '' COMMENT '姓名',
  score double NOT NULL DEFAULT 0.0 COMMENT '积分',
  PRIMARY KEY(`id`)
) ENGINE INNODB CHARSET=UTF8;