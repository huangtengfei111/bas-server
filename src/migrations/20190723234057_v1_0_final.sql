-- ct_labels
call AddColumnUnlessExists(Database(), 'ct_labels', 'cp_lat', 'VARCHAR(255)');
call AddColumnUnlessExists(Database(), 'ct_labels', 'cp_lng', 'VARCHAR(255)');
call AddColumnUnlessExists(Database(), 'ct_labels', 'cp_name', 'VARCHAR(255)');

-- pbills
call AddColumnUnlessExists(Database(), 'pbills', 'call_attribution', 'VARCHAR(100)');
call AddColumnUnlessExists(Database(), 'pbills', 'residence', 'VARCHAR(255)');

-- users
call AddColumnUnlessExists(Database(), 'users', 'last_login_at', 'DATETIME');
call AddColumnUnlessExists(Database(), 'users', 'last_remote_host', 'VARCHAR(100)');

-- 0624

-- pnum_labels
call AddColumnUnlessExists(Database(), 'pnum_labels', 'color_order', 'INT');
call AddColumnUnlessExists(Database(), 'pnum_labels', 'ptags', 'VARCHAR(255)');

-- ct_labels
call AddColumnUnlessExists(Database(), 'ct_labels', 'color_order', 'INT');


-- ALTER TABLE `rel_numbers` ADD UNIQUE INDEX `d_key_index` (`case_id`, `num`);
-- ALTER TABLE `ven_numbers` ADD UNIQUE INDEX `d_key_index` (`case_id`, `num`);
-- ALTER TABLE `pbills` DROP COLUMN `ven_network`;
-- ALTER TABLE `pbills` DROP COLUMN `rel_network`;
-- ALTER TABLE `case_breakpoints` DROP COLUMN `ended_at`;

-- cell_towers
call AddColumnUnlessExists(Database(), 'cell_towers', 'xlat', 'DECIMAL(14, 8)');
call AddColumnUnlessExists(Database(), 'cell_towers', 'xlng', 'DECIMAL(14, 8)');
call AddColumnUnlessExists(Database(), 'cell_towers', 'xaddr', 'VARCHAR(400)');

-- color default value
-- ALTER TABLE `pnum_labels` ALTER `color_order` SET DEFAULT 16;
-- ALTER TABLE `pnum_labels` ALTER `label_bg_color` SET DEFAULT "#607d8b";
-- ALTER TABLE `ct_labels`   ALTER `marker_color` SET DEFAULT "#607d8b";
-- DROP INDEX case_id_index ON `ct_labels`;
-- ALTER TABLE `ct_labels` ADD UNIQUE INDEX `case_code_index` (`case_id`, `ct_code`);
-- ALTER TABLE `ven_numbers` ADD UNIQUE INDEX `d_key_index` (case_id, num);
-- ALTER TABLE `rel_numbers` ADD UNIQUE INDEX `d_key_index` (case_id, num);
-- 
-- call AddColumnUnlessExists(Database(), 'citizen_books', 'category2', 'VARCHAR(10)'); 
-- ALTER TABLE `citizen_books` DROP COLUMN `category`;
-- ALTER TABLE `citizen_books` CHANGE `category2` `category` VARCHAR(10);

-- 0815
call AddColumnUnlessExists(Database(), 'cases',    'pb_city', 'VARCHAR(200)');
call AddColumnUnlessExists(Database(), 'licenses', 'path', 'VARCHAR(200)');

-- 0819
-- call AddColumnUnlessExists(Database(), 'pbill_records', 'owner_mnc2', 'BIGINT'); 
-- call AddColumnUnlessExists(Database(), 'pbill_records', 'peer_mnc2', 'BIGINT'); 
-- update pbill_records set owner_mnc2 = owner_mnc where (owner_mnc is not null or owner_mnc != '' or owner_mnc != -1) ; 
-- update pbill_records set peer_mnc2 = peer_mnc where (peer_mnc is not null or peer_mnc != '' or peer_mnc != -1); 
-- ALTER TABLE `pbill_records` DROP COLUMN `owner_mnc`;
-- ALTER TABLE `pbill_records` DROP COLUMN `peer_mnc`;
-- ALTER TABLE `pbill_records` CHANGE `owner_mnc2` `owner_mnc` BIGINT;
-- ALTER TABLE `pbill_records` CHANGE `peer_mnc2` `peer_mnc` BIGINT;
-- CREATE INDEX owner_mnc_index ON pbill_records(owner_mnc);
-- CREATE INDEX peer_mnc_index ON pbill_records(peer_mnc);

-- select owner_lac, owner_ci, owner_mnc, CONCAT(HEX(owner_lac), ":", HEX(owner_ci), ":", HEX(owner_mnc)) as ct_code from pbill_records where (owner_lac > 0 and owner_ci > 0) limit 20;
-- select peer_lac, peer_ci, peer_mnc, CONCAT(HEX(peer_lac), ":", HEX(peer_ci), ":", HEX(peer_mnc)) as ct_code from pbill_records where (peer_lac > 0 and peer_ci > 0) limit 20;
-- update pbill_records set owner_ct_code = CONCAT(HEX(owner_lac), ":", HEX(owner_ci), ":", HEX(owner_mnc)) where (owner_lac > 0 and owner_ci > 0) ;
-- update pbill_records set peer_ct_code = CONCAT(HEX(peer_lac), ":", HEX(peer_ci), ":", HEX(peer_mnc)) where (peer_lac > 0 and peer_ci > 0) ;

-- call AddColumnUnlessExists(Database(), 'cell_towers', 'mnc2', 'BIGINT'); 
-- update cell_towers set mnc2 = mnc;
-- ALTER TABLE `cell_towers` DROP COLUMN `mnc`;
-- ALTER TABLE `cell_towers` CHANGE `mnc2` `mnc` BIGINT;
-- select lac, ci, mnc, CONCAT(HEX(lac), ":", HEX(ci), ":", HEX(mnc)) as code from cell_towers limit 10;
-- update cell_towers set code = CONCAT(HEX(lac), ":", HEX(ci), ":", HEX(mnc)) where (lac > 0 and ci > 0) ;
-- CREATE INDEX mnc_index ON cell_towers(mnc);

-- 0823
-- call AddColumnUnlessExists(Database(), 'users', 'avatar', 'VARCHAR(255)');

-- ALTER TABLE `cell_towers` CHANGE `addr` `addr` VARCHAR(400);
-- ALTER TABLE `cell_towers` CHANGE `xaddr` `xaddr` VARCHAR(800);

-- call AddColumnUnlessExists(Database(), 'accounts', 'built_in', 'INT');
-- CREATE INDEX xlat_index ON cell_towers(xlat);
-- CREATE INDEX xlng_index ON cell_towers(xlng);

-- 0912
call AddColumnUnlessExists(Database(), 'citizens',    'category', 'VARCHAR(10)');
call AddColumnUnlessExists(Database(), 'citizens',    'version', 'VARCHAR(200)');
call AddColumnUnlessExists(Database(), 'citizen_addresses',    'area_code', 'VARCHAR(200)');