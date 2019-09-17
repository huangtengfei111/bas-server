-- SELECT "Data for init system";


INSERT INTO roles set value = "tester", name = "测试", created_at = NOW(), updated_at = NOW();
INSERT INTO users set name = "路人甲", created_at = NOW(), updated_at = NOW();
INSERT INTO users set name = "路人乙", created_at = NOW(), updated_at = NOW();

INSERT INTO accounts SET username = "test01", role_id = 3, user_id = 3, salt = "QZuPGmJycUsF8cKBYrq6HQ==", password = "DMQHrKajEUWpmAy71QLyGOpZvyH1K0ToHdXcrMHhdDk=", created_at = NOW(), updated_at = NOW();
INSERT INTO accounts SET username = "test02", role_id = 3, user_id = 3, salt = "QZuPGmJycUsF8cKBYrq6HQ==", password = "DMQHrKajEUWpmAy71QLyGOpZvyH1K0ToHdXcrMHhdDk=", created_at = NOW(), updated_at = NOW();
INSERT INTO accounts SET username = "test03", role_id = 3, user_id = 3, salt = "QZuPGmJycUsF8cKBYrq6HQ==", password = "DMQHrKajEUWpmAy71QLyGOpZvyH1K0ToHdXcrMHhdDk=", created_at = NOW(), updated_at = NOW();
INSERT INTO accounts SET username = "test04", role_id = 4, user_id = 3, salt = "QZuPGmJycUsF8cKBYrq6HQ==", password = "DMQHrKajEUWpmAy71QLyGOpZvyH1K0ToHdXcrMHhdDk=", created_at = NOW(), updated_at = NOW();



-- 案件
INSERT INTO cases SET created_by = 3, name = "沙雕侠旅",  num = "ANB-20190101", operator = "何警官", started_at = DATE_SUB(NOW(), INTERVAL 30 DAY), ended_at = DATE_SUB(NOW(), INTERVAL 5 DAY), status = 0, created_at = NOW(), updated_at = NOW();
INSERT INTO cases SET created_by = 3, name = "电信欺诈",  num = "ANB-20190201", operator = "方警官", started_at = DATE_SUB(NOW(), INTERVAL 20 DAY),  status = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO cases SET created_by = 3, name = "远扬货轮",  num = "ANB-20190301", operator = "周警官", started_at = DATE_SUB(NOW(), INTERVAL 10 DAY),  status = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO cases SET created_by = 3, name = "古巴比伦",  num = "ANB-20190501", operator = "赵警官", started_at = DATE_SUB(NOW(), INTERVAL 7 DAY),   status = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO cases SET created_by = 3, name = "东方快车",  num = "ANB-20190401", operator = "吴警官", started_at = DATE_SUB(NOW(), INTERVAL 1 DAY),   status = 1, created_at = NOW(), updated_at = NOW();

-- 虚拟网
INSERT INTO ven_numbers 
SET case_id = 2, num = "15788739123", short_num = "123", network = "沙家电网", source = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO ven_numbers 
SET case_id = 2, num = "13325886779", short_num = "779", network = "沙家电网", source = 1, created_at = NOW(), updated_at = NOW();

-- 亲情网
INSERT INTO rel_numbers 
SET case_id = 3, num = "19378873911", short_num = "873", network = "杨康一家", label = "杨康", source = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO rel_numbers 
SET case_id = 3, num = "19378345911", short_num = "874", network = "杨康一家", label = "暮念慈", source = 2, created_at = NOW(), updated_at = NOW();

-- 时间标注
INSERT INTO case_events
SET case_id = 1, name = "金兵调度", started_at = DATE_SUB(NOW(), INTERVAL 18 DAY), ended_at = DATE_SUB(NOW(), INTERVAL 15 DAY), color = '#ff0000', created_at = NOW(), updated_at = NOW(); 
INSERT INTO case_events
SET case_id = 1, name = "捡到秘笈", started_at = DATE_SUB(NOW(), INTERVAL 7 DAY), ended_at = DATE_SUB(NOW(), INTERVAL 2 DAY), color = '#00ff00', created_at = NOW(), updated_at = NOW(); 
INSERT INTO case_events
SET case_id = 1, name = "掉到古墓", started_at = DATE_SUB(NOW(), INTERVAL 1 DAY), color = '#00ff00', created_at = NOW(), updated_at = NOW(); 

-- 时间分割点
INSERT INTO case_breakpoints
SET case_id = 1, name = "练成神功", started_at = DATE_SUB(NOW(), INTERVAL 12 DAY), memo = "练就打狗棒法", color = '#ff0000', created_at = NOW(), updated_at = NOW();     
INSERT INTO case_breakpoints
SET case_id = 1, name = "比武大会", started_at = DATE_SUB(NOW(), INTERVAL 10 DAY), memo = "夺得武林盟主", color = '#00ff00', created_at = NOW(), updated_at = NOW();     
INSERT INTO case_breakpoints
SET case_id = 1, name = "守卫襄阳城", started_at = DATE_SUB(NOW(), INTERVAL 8 DAY), memo = "抵抗入侵", color = '#00ff00', created_at = NOW(), updated_at = NOW();        

-- 号码归属地
INSERT INTO call_attributions
SET num = "1350651", city = "温州", isp = "CMCC", created_at = NOW(), updated_at = NOW();
INSERT INTO call_attributions
SET num = "1370666", city = "温州", isp = "CMCC", created_at = NOW(), updated_at = NOW();
INSERT INTO call_attributions
SET num = "1380655", city = "温州", isp = "CMCC", created_at = NOW(), updated_at = NOW();

-- 分类标签
INSERT INTO label_groups
SET case_id = 5, name = "神雕侠侣", topic = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO label_groups
SET case_id = 5, name = "金庸武侠", topic = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO label_groups
SET case_id = 5, name = "丐帮", topic = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO label_groups
SET case_id = 5, name = "活死人墓", topic = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO label_groups
SET case_id = 5, name = "终南山", topic = 3, created_at = NOW(), updated_at = NOW();

-- 号码标注
INSERT INTO pnum_labels
SET case_id = 5, num = "13806685559", short_num = "8571", label = "x杨过", created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels
SET case_id = 5, num = "13906637600", short_num = "8572", label = "东邪黄药师", created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels
SET case_id = 5, num = "13806871968", label = "西毒欧阳锋", created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels
SET case_id = 5, num = "13868889917", label = "南帝段智兴", created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels
SET case_id = 5, num = "13906633778", label = "北丐洪七公", created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels
SET case_id = 5, num = "15858819012", short_num = "8573", label = "中神通王重阳", created_at = NOW(), updated_at = NOW();

-- 号码标注分类标签关联表
INSERT INTO pnum_labels_label_groups
SET pnum_label_id = 1, label_group_id = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels_label_groups
SET pnum_label_id = 2, label_group_id = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels_label_groups
SET pnum_label_id = 3, label_group_id = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels_label_groups
SET pnum_label_id = 4, label_group_id = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels_label_groups
SET pnum_label_id = 5, label_group_id = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels_label_groups
SET pnum_label_id = 6, label_group_id = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels_label_groups
SET pnum_label_id = 3, label_group_id = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels_label_groups
SET pnum_label_id = 4, label_group_id = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels_label_groups
SET pnum_label_id = 5, label_group_id = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pnum_labels_label_groups
SET pnum_label_id = 6, label_group_id = 2, created_at = NOW(), updated_at = NOW();

-- 基站标注
INSERT INTO ct_labels
SET case_id = 5, ct_code = "6F6D:6547:0", label = "丐帮聚集点", marker_color = "#FF0000", memo = "上海市青浦区金泽镇张林路;培雅路与沪青平公路路口东北81米", created_at = NOW(), updated_at = NOW(); 
INSERT INTO ct_labels
SET case_id = 5, ct_code = "1536:8A6D:0", label = "乔峰最后一次通讯", marker_color = "#FF0000", memo = "上海市嘉定区安亭镇新源路;和静路与胜巷路路口北202米", created_at = NOW(), updated_at = NOW(); 
INSERT INTO ct_labels
SET case_id = 5, ct_code = "8563:4C5A:0", label = "杨过首次通话", marker_color = "#FF0000", memo = "上海市嘉定区徐行镇朱桥出口(G15沈海高速出口东南向);G15沈海高速世盛路出口与G15沈海高速路口东23米", created_at = NOW(), updated_at = NOW(); 

-- 基站标注分类标签关联表
INSERT INTO ct_labels_label_groups
SET ct_label_id = 1, label_group_id = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO ct_labels_label_groups
SET ct_label_id = 2, label_group_id = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO ct_labels_label_groups
SET ct_label_id = 2, label_group_id = 5, created_at = NOW(), updated_at = NOW();
INSERT INTO ct_labels_label_groups
SET ct_label_id = 3, label_group_id = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO ct_labels_label_groups
SET ct_label_id = 3, label_group_id = 5, created_at = NOW(), updated_at = NOW();


-- 话单
INSERT INTO pbills
SET owner_name = "武一一", owner_num = "19378873911", total = 123, created_at = NOW(), updated_at = NOW();
INSERT INTO pbills
SET owner_name = "武三思", owner_num = "15788739123", total = 123, created_at = NOW(), updated_at = NOW();
INSERT INTO pbills
SET owner_name = "武三通", owner_num = "13355109834", total = 123, created_at = NOW(), updated_at = NOW();
INSERT INTO pbills
SET owner_name = "郭襄", owner_num = "13968797370", total = 100, created_at = NOW(), updated_at = NOW();
INSERT INTO pbills
SET owner_name = "郭芙", owner_num = "13957717725", total = 257, created_at = NOW(), updated_at = NOW();
INSERT INTO pbills
SET owner_name = "胡斐", owner_num = "15899658525", total = 154, created_at = NOW(), updated_at = NOW();
INSERT INTO pbills
SET owner_name = "韦小宝", owner_num = "13806871968", total = 267, created_at = NOW(), updated_at = NOW();

-- 话单案件关联表
INSERT INTO cases_pbills
SET case_id = 2, pbill_id = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO cases_pbills
SET case_id = 3, pbill_id = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO cases_pbills
SET case_id = 1, pbill_id = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO cases_pbills
SET case_id = 1, pbill_id = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO cases_pbills
SET case_id = 1, pbill_id = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO cases_pbills
SET case_id = 1, pbill_id = 5, created_at = NOW(), updated_at = NOW();


INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "19378873911",  peer_short_num = "874", peer_num_type = 61, peer_num_attr = "浙江南都", peer_num_isp = "", ven = 0, bill_type = 1, started_at = '2011-04-01 11:04:34',  ended_at = '2011-04-10 11:06:17', weekday = 1, 
started_day ='2011-04-01', alyz_day = '2011-04-01', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 1, started_hour_class = 1, time_class = 1, duration = 45, duration_class = 1, comm_direction = 11, owner_num_status = 0, 
owner_comm_loc = "南都", peer_comm_loc = "南都", long_dist = 0, owner_ct_code = "6779:2A7E:0", created_at = NOW(), updated_at = NOW(), owner_lac = 34860;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "15788739123",  peer_short_num = "779", peer_num_type = 61, peer_num_attr = "浙江南都", peer_num_isp = "", ven = 0, bill_type = 1, started_at = '2011-04-01 11:04:34',  ended_at = '2011-04-10 11:06:17', weekday = 1, 
started_day ='2011-04-01', alyz_day = '2011-04-01', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 1, started_hour_class = 1, time_class = 1, duration = 45, duration_class = 1, comm_direction = 11, owner_num_status = 0, 
owner_comm_loc = "南都", peer_comm_loc = "南都", long_dist = 0, owner_ct_code = "6779:2A7E:0", created_at = NOW(), updated_at = NOW(), owner_lac = 34860;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "13355109834", peer_num = "13564789478", peer_short_num = "5471", peer_num_type = 61, peer_num_attr = "浙江南都", peer_num_isp = "固话", ven = 0, bill_type = 1, started_at = '2011-04-01 11:04:34',  ended_at = '2011-04-10 11:06:17', weekday = 1, 
started_day ='2011-04-01', alyz_day = '2011-04-01', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 1, started_hour_class = 1, time_class = 1, duration = 45, duration_class = 1, comm_direction = 11, owner_num_status = 0, 
owner_comm_loc = "南都", peer_comm_loc = "南都", long_dist = 0, owner_ct_code = "6779:2A7E:0", created_at = NOW(), updated_at = NOW(), owner_lac = 34860;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "13355109834", peer_num = "13564789478", peer_short_num = "5471", peer_num_type = 61, peer_num_attr = "浙江南都", peer_num_isp = "固话", ven = 0, bill_type = 1, started_at = '2011-04-01 12:04:34',  ended_at = '2011-04-15 11:06:17', weekday = 1, 
started_day ='2011-04-01', alyz_day = '2011-04-01', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 2, started_hour_class = 1, time_class = 1, duration = 45, duration_class = 1, comm_direction =11, owner_num_status = 0, 
owner_comm_loc = "南都", peer_comm_loc = "南都", long_dist = 0, owner_ct_code = "6779:2A7E:0",  created_at = NOW(), updated_at = NOW(), owner_lac = 34860;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "13355109834", peer_num = "13564789478", peer_short_num = "5471", peer_num_type = 61, peer_num_attr = "浙江南都", peer_num_isp = "固话", ven = 0, bill_type = 1, started_at = '2011-04-01 13:04:34',  ended_at = '2011-04-20 11:06:17', weekday = 1, 
started_day ='2011-04-01', alyz_day = '2011-04-01', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 3, started_hour_class = 1, time_class = 1, duration = 45, duration_class = 1, comm_direction =11, owner_num_status = 0, 
owner_comm_loc = "南都", peer_comm_loc = "南都", long_dist = 0, owner_ct_code = "6779:2A7E:0",  created_at = NOW(), updated_at = NOW(), owner_lac = 34860;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "13355109834", peer_num = "13564789478", peer_short_num = "5471", peer_num_type = 61, peer_num_attr = "浙江南都", peer_num_isp = "固话", ven = 0, bill_type = 1, started_at = '2011-04-01 14:04:34',  ended_at = '2011-04-22 11:06:17', weekday = 1, 
started_day ='2011-04-01', alyz_day = '2011-04-01', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 4, started_hour_class = 1, time_class = 1, duration = 45, duration_class = 1, comm_direction = 11, owner_num_status = 0, 
owner_comm_loc = "南都", peer_comm_loc = "南都", long_dist = 0, owner_ct_code = "6779:2A7E:0",  created_at = NOW(), updated_at = NOW(), owner_lac = 34860;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "13355109834", peer_num = "13865778177", peer_short_num = "5479", peer_num_type = 11, peer_num_attr = "福建厦门", peer_num_isp = "移动", ven = 0, bill_type = 0, started_at = '2011-04-02 15:04:34',  ended_at = '2011-04-23 11:06:17', weekday = 2, 
started_day ='2011-04-02', alyz_day = '2011-04-02', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 0, started_time_l2_class = 1, started_hour_class = 0, time_class = 1, duration = 49, duration_class = 1, comm_direction = 11, owner_num_status = 0, 
owner_comm_loc = "厦门", peer_comm_loc = "厦门", long_dist = 0, owner_ct_code = "6777:28DD:0",  created_at = NOW(), updated_at = NOW(), owner_lac = 26489;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "13745784254", peer_num = "13545781247", peer_short_num = "5676", peer_num_type = 11, peer_num_attr = "河南郑州", peer_num_isp = "移动", ven = 0, bill_type = 0, started_at = '2011-04-03 08:04:34',  ended_at = '2011-04-24 10:06:17', weekday = 3, 
started_day ='2011-04-03', alyz_day = '2011-04-03', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 0, started_hour_class = 1, time_class = 0, duration = 79, duration_class = 0, comm_direction = 0, owner_num_status = 1, 
owner_comm_loc = "郑州", peer_comm_loc = "郑州", long_dist = 0, owner_ct_code = "57FB:5CD5:0",  created_at = NOW(), updated_at = NOW(), owner_lac = 26489;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "13745784254", peer_num = "13545781247", peer_short_num = "5676", peer_num_type = 11, peer_num_attr = "河南郑州", peer_num_isp = "移动", ven = 0, bill_type = 0, started_at = '2011-04-03 07:04:34',  ended_at = '2011-04-25 10:06:17', weekday = 3, 
started_day ='2011-04-03', alyz_day = '2011-04-03', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 0, started_hour_class = 1, time_class = 0, duration = 79, duration_class = 0, comm_direction = 0, owner_num_status = 1, 
owner_comm_loc = "郑州", peer_comm_loc = "郑州", long_dist = 0, owner_ct_code = "57FB:5CD5:0",  created_at = NOW(), updated_at = NOW(), owner_lac = 26489;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "13745784254", peer_num = "55555", peer_short_num = "5676", peer_num_type = 11, peer_num_attr = "河南郑州", peer_num_isp = "移动", ven = 0, bill_type = 0, started_at = '2011-04-03 05:04:34',  ended_at = '2011-04-26 10:06:17', weekday = 3, 
started_day ='2011-04-03', alyz_day = '2011-04-03', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 0, started_hour_class = 1, time_class = 0, duration = 79, duration_class = 0, comm_direction = 0, owner_num_status = 1, 
owner_comm_loc = "郑州", peer_comm_loc = "郑州", long_dist = 0, owner_ct_code = "57FB:5CD5:0", created_at = NOW(), updated_at = NOW(), owner_lac = 26489;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "15899658525", peer_num = "10086001", peer_short_num = "5676", peer_num_type = 11, peer_num_attr = "河南郑州", peer_num_isp = "移动", ven = 0, bill_type = 0, started_at = '2011-04-03 06:04:34',  ended_at = '2011-04-27 10:06:17', weekday = 3, 
started_day ='2011-04-03', alyz_day = '2011-04-03', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 0, started_hour_class = 1, time_class = 0, duration = 79, duration_class = 0, comm_direction = 0, owner_num_status = 1, 
owner_comm_loc = "郑州", peer_comm_loc = "郑州", long_dist = 0, owner_ct_code = "57FB:5CD5:0", created_at = NOW(), updated_at = NOW(), owner_lac = 26489;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "13745784254", peer_num = "55555", peer_short_num = "5676", peer_num_type = 11, peer_num_attr = "河南郑州", peer_num_isp = "移动", ven = 0, bill_type = 0, started_at = '2011-04-03 10:14:34',  ended_at = '2011-04-28 10:06:17', weekday = 3, 
started_day ='2011-04-03', alyz_day = '2011-04-03', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 0, started_hour_class = 1, time_class = 0, duration = 79, duration_class = 0, comm_direction = 0, owner_num_status = 1, 
owner_comm_loc = "郑州", peer_comm_loc = "郑州", long_dist = 0, owner_ct_code = "57FB:5CD5:0", created_at = NOW(), updated_at = NOW(), owner_lac = 26489;
INSERT INTO pbill_records
SET pbill_id = 1, owner_num = "13696562536", peer_num = "10086001", peer_short_num = "5676", peer_num_type = 11, peer_num_attr = "河南郑州", peer_num_isp = "移动", ven = 0, bill_type = 0, started_at = '2011-04-03 6:24:34',  ended_at = '2011-04-29 6:26:17', weekday = 3, 
started_day ='2011-04-03', alyz_day = '2011-04-03', alyz_day_type = 0, started_time = '11:11', started_time_l1_class = 1, started_time_l2_class = 0, started_hour_class = 1, time_class = 0, duration = 79, duration_class = 0, comm_direction = 0, owner_num_status = 1, 
owner_comm_loc = "郑州", peer_comm_loc = "郑州", long_dist = 0, owner_ct_code = "57FB:5CD5:0", created_at = NOW(), updated_at = NOW(), owner_lac = 26489;


-- 基站信息
INSERT INTO cell_towers
SET id = 2, lac = "1", ci = "2", mnc = "3";
INSERT INTO cell_towers
SET id = 3,  lac = "4", ci = "5", mnc = "6";
INSERT INTO cell_towers
SET id = 4, lac = "7", ci = "8", mnc = "9";
INSERT INTO cell_towers
SET code = "1", ci = 2, mnc = "0", mcc = 3, lac = 4, isp = 5, ci_hex = "", lac_hex = "", name = "", province = "", city = "杭州", district = "", town = "", branch = "", addr = "", angle = 6, radius = 8, created_at = NOW(), updated_at = NOW(); 
INSERT INTO cell_towers
SET code = "1", ci = 62041, mnc = "00", mcc = 460, lac = 34860, lat = 22.0162, lng =  100.7493, glat =  22.0162, glng =  100.7493, isp = 5, ci_hex = "", lac_hex = "", name = "", province = "云南省", city = "西双版纳傣族自治州", district = "", town = "", branch = "", addr = "云南省西双版纳傣族自治州景洪市景洪工业园区西双版纳主题公园万达国际度假区", angle = 6, radius = 8, created_at = NOW(), updated_at = NOW(); 

-- 搜索条件
INSERT INTO searches
SET account_id = 1, case_id = 1, name = "张三的搜索", subject = "pbills", value = "{\"criteria\": {\"peer_num\":[\"IN\",[\"11222\",\"99922\"]]}}";
INSERT INTO searches
SET account_id = 1, case_id = 1, name = "李四的搜索", subject = "pbills", value = "{\"criteria\": {\"owner_comm_loc\": [\"FUZZY\", \"南京\"]} }";

-- 人员手机号
INSERT INTO citizen_phones
SET citizen_id = 1, num = "333333", memo = "办电", ven_name = "", citizen_book_id = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO citizen_phones
SET citizen_id = 1, num = "55555", memo = "宅电", ven_name = "", citizen_book_id = 1, created_at = NOW(), updated_at = NOW();

-- 人员通讯录
INSERT INTO citizen_books
SET name = "张三通讯录", area_code = "111", version = "一版", category = "z", filename = "", created_at = NOW();
INSERT INTO citizen_books
SET name = "李思通讯录", area_code = "222", version = "一版", category = "z", filename = "", created_at = NOW();

-- 人员库信息
INSERT INTO citizens
SET social_no = "123456789", name = "张三", phone = "029-222222",category = "z", mobile = "222222", ven_num = "123", ven_name = "虚拟", company = "百度", position = "经理", citizen_book_id = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO citizens
SET social_no = "987654321", name = "李思", phone = "029-111",category = "z", mobile = "111111", ven_num = "666", ven_name = "模拟", company = "阿里", position = "经理", citizen_book_id = 2, created_at = NOW(), updated_at = NOW();
-- ./scripts/remigrate.sh