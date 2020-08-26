ALTER TABLE aes.tb_record
CHANGE COLUMN daily_goal daily_goal FLOAT NULL DEFAULT 0 ,
CHANGE COLUMN weekly_goal weekly_goal FLOAT NULL DEFAULT 0 ;
