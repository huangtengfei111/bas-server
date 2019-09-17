-- SELECT "Data for init system";


INSERT INTO roles set name = "系统管理员", value = "admin", created_at = NOW(), updated_at = NOW();
INSERT INTO roles set name = "普通办案人员", value = "user", created_at = NOW(), updated_at = NOW();

INSERT INTO users set name = "系统管理员", created_at = NOW(), updated_at = NOW();

INSERT INTO accounts SET username = "root", salt = "QZuPGmJycUsF8cKBYrq6HQ==", password = "DMQHrKajEUWpmAy71QLyGOpZvyH1K0ToHdXcrMHhdDk=", role_id = 1, user_id = 1, built_in = 1, created_at = NOW(), updated_at = NOW();


-- 
INSERT INTO settings 
SET account_id = 0, k = "system.id",   v = "1", memo = "系统标识", created_at = NOW(), updated_at = NOW();
INSERT INTO settings 
SET account_id = 0, k = "system.login.mode",   v = "mixed", memo = "系统登录模式", created_at = NOW(), updated_at = NOW();
INSERT INTO settings 
SET account_id = 0, k = "center.bas.hp",   v = "47.103.61.224:9091", memo = "BAS中心服务器地址", created_at = NOW(), updated_at = NOW();
INSERT INTO settings
SET account_id = 0, k = "maps.vendor", v = "Baidu Maps", memo = "地图控件", created_at = NOW(), updated_at = NOW();
INSERT INTO settings
SET account_id = 0, k = "bmaps.appId", v = "", memo = "Baidu Maps的AppId", created_at = NOW(), updated_at = NOW();
INSERT INTO settings
SET account_id = 0, k = "bmaps.appKey", v = "", memo = "Baidu Maps的AppKey", created_at = NOW(), updated_at = NOW();

-- pub_service_nums  grup 1--公众服务电话号码、常用电话、紧急电话 2--全国国家机构监督、投诉、抢修、举报电话 3--全国通信机构服务电话号码
-- 4--全国银行客户服务电话号码 5--快递公司客服电话 6--外卖订餐电话 7--旅行预订电话 8--保险客服电话
INSERT INTO pub_service_nums 
SET num = 119, memo = "火警电话", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 110, memo = "报警服务台", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 120, memo = "急救电话", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 122, memo = "道路交通事故报警台", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12110, memo = "公安短信报警号码", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 114, memo = "查号台", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 11185, memo = "邮政客户服务电话", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12348, memo = "全国法律服务热线", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12395, memo = "水上遇险求救电话", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12121, memo = "气象服务电话", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12117, memo = "报时服务电话", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 999, memo = "红十字会急救台", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95119, memo = "森林火警电话", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 112, memo = "紧急呼叫中心", grup = 1, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12306, memo = "全国铁路客服中心", grup = 1, created_at = NOW(), updated_at = NOW();

INSERT INTO pub_service_nums 
SET num = 95598, memo = "电力系统客服电话", grup = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12315, memo = "消费者投诉举报专线电话", grup = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12365, memo = "质量监督电话", grup = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12369, memo = "环保局监督电话", grup = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12345, memo = "政府公益服务接入网", grup = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12318, memo = "文化市场统一举报电话", grup = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12358, memo = "价格监督举报电话", grup = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12310, memo = "机构编制违规举报热线", grup = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12333, memo = "民工维权热线电话", grup = 2, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 12320, memo = "公共卫生环境投诉", grup = 2, created_at = NOW(), updated_at = NOW();


INSERT INTO pub_service_nums 
SET num = 10000, memo = "中国电信客户服务热线", grup = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 10086, memo = "中国移动客服热线", grup = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 17911, memo = "中国联通IP号码", grup = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 116114, memo = "中国联通的“电话导航”业务", grup = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 10050, memo = "铁通客户服务", grup = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 10010, memo = "中国联通客服热线", grup = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 17900, memo = "中国电信IP电话卡", grup = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 17951, memo = "中国移动IP号码", grup = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 118114, memo = "中国电信号码百事通", grup = 3, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 10086999, memo = "中国垃圾短信投诉号码", grup = 3, created_at = NOW(), updated_at = NOW();

INSERT INTO pub_service_nums 
SET num = 95555, memo = "招商银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95566, memo = "中国银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95533, memo = "建设银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95588, memo = "工商银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95558, memo = "中信银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95528, memo = "浦发银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95501, memo = "深发银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95599, memo = "农业银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95568, memo = "民生银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95595, memo = "光大银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95559, memo = "交通银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95508, memo = "广发银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95577, memo = "华夏银行", grup = 4, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95561, memo = "兴业银行", grup = 4, created_at = NOW(), updated_at = NOW();

INSERT INTO pub_service_nums 
SET num = 95543, memo = "申通快递", grup = 5, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95546, memo = "韵达快递", grup = 5, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95311, memo = "中通快递", grup = 5, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 4009565656, memo = "百世汇通", grup = 5, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95338, memo = "顺丰速运", grup = 5, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95554, memo = "圆通快递", grup = 5, created_at = NOW(), updated_at = NOW();

INSERT INTO pub_service_nums 
SET num = 4008517517, memo = "麦当劳", grup = 6, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 4008123123, memo = "必胜客", grup = 6, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 4008823823, memo = "肯德基", grup = 6, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 4008800400, memo = "丽华快餐", grup = 6, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 4006927927, memo = "真功夫", grup = 6, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 4000979797, memo = "永和大王", grup = 6, created_at = NOW(), updated_at = NOW();

INSERT INTO pub_service_nums 
SET num = 4008206666, memo = "携程网", grup = 7, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 4007777777, memo = "同程网", grup = 7, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 4007999999, memo = "途牛网", grup = 7, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 4009333333, memo = "艺龙网", grup = 7, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 4006640066, memo = "芒果网", grup = 7, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 10101234, memo = "去哪儿网", grup = 7, created_at = NOW(), updated_at = NOW();

INSERT INTO pub_service_nums 
SET num = 95500, memo = "太平洋保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95511, memo = "平安保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95515, memo = "合众人寿保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95519, memo = "中国人寿保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95522, memo = "合众人寿保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95567, memo = "新华人寿保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95589, memo = "太平保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95596, memo = "民生人寿保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95510, memo = "阳光保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95512, memo = "平安产险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95518, memo = "中国人民保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95535, memo = "生命人寿保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95585, memo = "中华保险", grup = 8, created_at = NOW(), updated_at = NOW();
INSERT INTO pub_service_nums 
SET num = 95590, memo = "大地保险", grup = 8, created_at = NOW(), updated_at = NOW();
