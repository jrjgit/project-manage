-- =============================================
-- management.sql 数据 → 适配实际数据库（V1~V37 迁移后）的 INSERT 语句
-- 注意：init.sql 已过时（含 V37 已删除的 wechat_id/dingtalk_id/feishu_id 列）
-- 实际数据库结构与 management.sql 一致，无需大幅调整
-- =============================================

-- ============================================================
-- 1. users（实际数据库经 V37 已删除 wechat_id/dingtalk_id/feishu_id）
--    列顺序与 management.sql 一致：id, name, password, account, role, email, skills, created_at
-- ============================================================
INSERT IGNORE INTO users (id, name, password, account, role, email, skills, created_at) VALUES
(13, 'admin', '$2a$10$aJF885siaj8Eeg7HHitx9eRsb46zXCbbpQ7Qaq0ngsfqanKXB2Kne', 'admin', 'pm', NULL, NULL, '2026-05-26 16:01:30'),
(14, '黄恒闹', '$2a$10$NB0e4O15ft4TJtj3hJdKyeOzqCe3H.hmd5q6U2ey3MbHrF.YgtNvm', 'huanghn', 'dev_lead', NULL, 'backend', '2026-05-26 16:48:05'),
(15, '陶天德', '$2a$10$GAONognHvEEtwkkssm9HUOzvfUaILW.KEjtw/JPorL23jL7l7s/yG', 'taotd', 'dev', NULL, 'android,harmony,h5', '2026-05-27 16:08:39'),
(16, '伍开敏', '$2a$10$ySpxTVBaYmryao8N7ih2eOETGBad4vpuCI4gL5nj4LLmZi6icAdYe', 'wukm', 'dev', NULL, 'ios,miniapp,h5', '2026-05-27 16:08:56'),
(17, '郭才伟', '$2a$10$Q80htt00MS2W8QksG9W95OoRnnD6VLHAwJmVtafOD1yxA7S9Jyanq', 'guocw', 'dev', NULL, 'backend', '2026-05-27 16:09:11'),
(18, '韦俊', '$2a$10$GgnugPiTFByf1IpAubEBH.FdybzqLCOF9P8oQq4AdjbNz4lztknHO', 'weij', 'tester', NULL, NULL, '2026-05-27 16:09:30'),
(19, '刘婧怡', '$2a$10$FYuL2OZADxSM08Z3GmrwQ.o3tuQfU584DuvVWYZ0fLy4hEmm0zscC', 'liujy', 'tester', NULL, NULL, '2026-05-27 16:09:51'),
(20, '兰伟民', '$2a$10$mNUyMWaMb8DVz2vmvOk50.yC3yVijIPsO8EQBDK.qX9K9zodBukK2', 'lanwm', 'pm', NULL, NULL, '2026-06-01 15:03:58'),
(21, '邓乃月', '$2a$10$YpZSH4.GuzM87poQ8b458ur/gpCH9dodnURAlUB4FlMZUZsQcgQHW', 'dengny', 'dev', NULL, 'backend', '2026-06-01 15:07:14'),
(22, '黄俊钢', '$2a$10$3Db6hkPSNk8fp.YRmgYexe4vcJusOifnh79fgXrlQuXSkXeizsffq', 'huangjg', 'dev', NULL, 'backend', '2026-06-01 15:07:34'),
(23, '黄盟琅', '$2a$10$iF45KOoJBua.zcLWO8MH1epQ3u5RTjyLILnApU6blCyI/WURICqTa', 'huangml', 'dev', NULL, 'backend', '2026-06-01 15:08:41'),
(24, '黄新洪', '$2a$10$RGHzVRXJIhdU3V.hMRNEfOW1EYmx/yG0/ZA.InLMbUybdvLqWNewe', 'huangxh', 'dev', NULL, 'backend', '2026-06-01 15:09:09'),
(25, '蒋荣江', '$2a$10$iWxlWYO29gh2xtHs78tWm.nupn9R8Y2VXHrLdaIONsR/.IvHzUr2a', 'jiangrj', 'dev', NULL, 'backend', '2026-06-01 15:09:45'),
(26, '兰素琼', '$2a$10$SiFOw3xaKtM3nR501/Bbve6m62wLFetbflh0XukkSo7rS.uzdj1B2', 'lansq', 'pm', NULL, NULL, '2026-06-01 15:11:36'),
(27, '李均连', '$2a$10$lG0KTV4.3q/1TLDMIFW/8ei.fqxbLf5MbwjOqtVeiAPhddc9zSuTa', 'lijl', 'pm', NULL, NULL, '2026-06-01 15:11:50'),
(28, '刘新', '$2a$10$l6Cv9q2y4o01QzEsXIQCn.F2wfurIk/5V4s/LcNWYAmjONJyzU.LK', 'liux', 'dev_lead', NULL, 'backend', '2026-06-01 15:12:08'),
(29, '罗世根', '$2a$10$a4o7ZA2dUcLo/bx/HET3juo9th8TVcgVCgoSIxXgQggHIugntmrou', 'luosg', 'dev_lead', NULL, 'backend,harmony,android', '2026-06-01 15:12:19'),
(30, '秦雪峰', '$2a$10$I17RoEBi04qvFWySUPx2sOIcI2AtRnZ.F4j0gRoidloAleNSYAadO', 'qinxf', 'dev', NULL, 'harmony,miniapp,h5', '2026-06-01 15:12:33'),
(31, '王家程', '$2a$10$i2piVYnB05H.csOC5s2dI..AoBDmuCILVGLdbMAPOU4hVWRP6B83.', 'wangjc', 'dev_lead', NULL, 'backend', '2026-06-01 15:12:49'),
(32, '韦黄顺', '$2a$10$TJY6tkNGeHaz0KQW6EW0b.XWMKBjnKU8u5yUXVCYQm4DWD4K6zsKi', 'weihs', 'dev', NULL, 'backend', '2026-06-01 15:13:01'),
(33, '代忠英', '$2a$10$lnE2fxqta/RD0AYPhGP4o.YDrtXlQW8dIa0PSI1jkoj3M.79vynlO', 'daizy', 'tester', NULL, NULL, '2026-06-02 11:06:55');

-- ============================================================
-- 2. projects（列顺序不同，management 多 created_at 在中间）
--    列顺序与 management.sql / 实际 DB 一致
-- ============================================================
INSERT IGNORE INTO projects (id, name, code, project_type, system_scope, hr_scope, pm_id, created_by, created_at) VALUES
(4, '2026固定人力420', NULL, 'ops', '4,3', '14,15,16,17,23,25,30,31,32,21', 13, 13, '2026-05-26 16:33:10'),
(5, '2025SAP固定人力140', NULL, 'ops', '4,3,5,6', '14,15,16,17', 13, 13, '2026-05-26 16:33:38'),
(6, '2025-2026 年信息系统应需开发服务项目', NULL, 'ops', '4,3', '14,15,16,17,21,22,23,25,29,30,31,32', 13, 13, '2026-05-26 16:34:00'),
(7, '2025商城社区优化项目', 'DFLQ-2025-IT0897-CV', 'invite_bidding', '4,3', '14,15,16,17,21,25,30,31,23,32', 13, 13, '2026-05-26 16:46:45'),
(8, 'PVCV触点免费支持', NULL, 'ops', '3,4', '17,16,15,14,20,21,23,30,31', 13, 13, '2026-05-29 11:45:37');

-- ============================================================
-- 3. tasks（列顺序与 management.sql / 实际 DB 一致）
-- ============================================================
INSERT IGNORE INTO tasks (id, title, description, status, priority, project_id, creator_id, assignee_id, dev_lead_id, tester_id, reject_reason, deadline, requirement_id, terminal, progress, performance, test_performance, iteration_id, created_at, updated_at) VALUES
(32, '安卓开发', '测试', 'developing', 'medium', 4, 13, 15, 14, 18, NULL, '2026-06-03 00:00:00', 5, 'android', 100, '2', NULL, NULL, '2026-05-28 14:37:40', '2026-05-28 14:37:40'),
(34, '后台', '测试', 'testing', 'medium', 4, 13, 14, 14, 18, NULL, '2026-06-04 00:00:00', 5, 'backend', 100, '2', NULL, NULL, '2026-05-28 14:37:40', '2026-05-28 14:37:40'),
(39, '复用PV：秒杀/限购需求XQ_DFFX20251014_01：H5相关功能开发', '无', 'testing', 'medium', 4, 13, 15, 14, NULL, NULL, '2026-05-29 16:00:00', 5, 'h5', 47, '1', NULL, NULL, '2026-05-29 10:54:53', '2026-05-29 10:54:53'),
(40, '134', 't', 'testing', 'medium', 4, 13, 25, 14, NULL, NULL, '2026-06-20 16:00:00', 12, 'backend', 70, '2', NULL, NULL, '2026-06-01 17:12:40', '2026-06-01 17:12:40'),
(41, '5', 't', 'pending', 'medium', 4, 13, 17, 14, NULL, NULL, '2026-06-20 16:00:00', 12, 'backend', 0, '1', NULL, NULL, '2026-06-01 17:12:41', '2026-06-01 17:12:41'),
(43, '1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。', '测试新界面', 'testing', 'medium', 4, 14, 15, 14, NULL, NULL, '2026-05-31 08:00:00', 12, 'android', 100, '2', '1', NULL, '2026-06-02 17:54:18', '2026-06-02 17:54:18'),
(50, '车主认证提示文案修改：证件验证失败行驶证vin与购车发票被授权vin不一致，是否进行车主认证申诉 >>修改为：行驶证VIN与购车发票的VIN以及授权书的VIN不一致，是否进行车主认证申诉', '任务2,3', 'testing', 'medium', 8, 31, 31, 31, NULL, NULL, '2026-06-04 08:00:00', 10, 'backend', 100, '3', '3', NULL, '2026-06-03 11:05:59', '2026-06-03 11:05:59'),
(51, '车主认证提示文案修改：证件验证失败行驶证vin与购车发票被授权vin不一致，是否进行车主认证申诉 >>修改为：行驶证VIN与购车发票的VIN以及授权书的VIN不一致，是否进行车主认证申诉', '任务1', 'pending', 'medium', 8, 31, 30, 31, NULL, NULL, '2026-06-04 16:00:00', 10, 'harmony', 0, '2', '3', NULL, '2026-06-03 11:06:43', '2026-06-03 11:06:43'),
(52, '车主认证提示文案修改：证件验证失败行驶证vin与购车发票被授权vin不一致，是否进行车主认证申诉 >>修改为：行驶证VIN与购车发票的VIN以及授权书的VIN不一致，是否进行车主认证申诉', '任务1', 'pending', 'medium', 8, 31, 30, 31, NULL, NULL, '2026-06-04 16:00:00', 10, 'miniapp', 0, '3', '3', NULL, '2026-06-03 11:06:43', '2026-06-03 11:06:43'),
(53, '1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。', NULL, 'pending', 'medium', 4, 14, 21, 14, NULL, NULL, NULL, 12, 'backend', 0, NULL, NULL, NULL, '2026-06-03 15:06:42', '2026-06-03 15:06:42'),
(54, '1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。', NULL, 'developing', 'medium', 4, 14, 14, 14, NULL, NULL, NULL, 12, 'backend', 25, NULL, NULL, NULL, '2026-06-03 15:07:35', '2026-06-03 15:07:35'),
(55, 'APP，小程序首页快捷入口：新增入口"卡券福利"。点击跳转-我的-卡券中心', '2', 'testing', 'medium', 8, 13, 15, NULL, NULL, NULL, '2026-06-27 16:00:00', 9, 'android', 100, '3', '1', NULL, '2026-06-03 15:12:17', '2026-06-03 15:12:17'),
(56, 'APP，小程序首页快捷入口：新增入口"卡券福利"。点击跳转-我的-卡券中心', '3', 'pending', 'medium', 8, 13, 15, NULL, NULL, NULL, '2026-06-27 16:00:00', 9, 'harmony', 0, '3', '2', NULL, '2026-06-03 15:12:17', '2026-06-03 15:12:17'),
(64, '1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。', '测试，测试', 'pending', 'medium', 4, 13, 14, 14, NULL, NULL, '2026-06-08 16:00:00', 12, 'backend', 0, '1', '1', NULL, '2026-06-08 01:50:56', '2026-06-08 01:50:56');

-- ============================================================
-- 4. task_assignees（列顺序一致，无需调整）
-- ============================================================
INSERT IGNORE INTO task_assignees (id, task_id, user_id, platform, status, created_at, updated_at) VALUES
(5, 8, 16, NULL, 'pending', '2026-05-27 16:32:02', '2026-05-27 16:32:02'),
(6, 9, 14, NULL, 'pending', '2026-05-27 17:20:17', '2026-05-27 17:20:17'),
(7, 10, 15, NULL, 'pending', '2026-05-27 17:20:17', '2026-05-27 17:20:17'),
(8, 11, 15, NULL, 'pending', '2026-05-27 17:20:17', '2026-05-27 17:20:17'),
(9, 12, 15, NULL, 'pending', '2026-05-27 17:20:18', '2026-05-27 17:20:18'),
(10, 13, 14, NULL, 'pending', '2026-05-27 17:30:35', '2026-05-27 17:30:35'),
(11, 14, 15, NULL, 'pending', '2026-05-27 17:30:35', '2026-05-27 17:30:35'),
(12, 15, 15, NULL, 'pending', '2026-05-27 17:30:35', '2026-05-27 17:30:35'),
(13, 16, 15, NULL, 'pending', '2026-05-27 17:30:35', '2026-05-27 17:30:35'),
(14, 17, 14, NULL, 'pending', '2026-05-27 17:38:00', '2026-05-27 17:38:00'),
(15, 18, 15, NULL, 'pending', '2026-05-27 17:38:00', '2026-05-27 17:38:00'),
(16, 19, 15, NULL, 'pending', '2026-05-27 17:38:00', '2026-05-27 17:38:00'),
(17, 20, 15, NULL, 'pending', '2026-05-27 17:38:00', '2026-05-27 17:38:00'),
(18, 21, 16, NULL, 'pending', '2026-05-27 17:38:00', '2026-05-27 17:38:00'),
(19, 22, 16, NULL, 'pending', '2026-05-27 17:38:01', '2026-05-27 17:38:01'),
(20, 23, 16, NULL, 'pending', '2026-05-27 17:38:01', '2026-05-27 17:38:01'),
(29, 32, 15, NULL, 'developing', '2026-05-28 14:37:40', '2026-05-28 14:37:40'),
(31, 34, 17, NULL, 'pending', '2026-05-28 14:37:40', '2026-05-28 14:37:40'),
(36, 39, 15, NULL, 'developing', '2026-05-29 10:54:53', '2026-05-29 10:54:53'),
(37, 40, 25, NULL, 'developing', '2026-06-01 17:12:40', '2026-06-01 17:12:40'),
(38, 41, 17, NULL, 'pending', '2026-06-01 17:12:41', '2026-06-01 17:12:41'),
(40, 43, 15, NULL, 'developing', '2026-06-02 17:54:18', '2026-06-02 17:54:18'),
(47, 50, 31, NULL, 'pending', '2026-06-03 11:05:59', '2026-06-03 11:05:59'),
(48, 51, 30, NULL, 'pending', '2026-06-03 11:06:43', '2026-06-03 11:06:43'),
(49, 52, 30, NULL, 'pending', '2026-06-03 11:06:43', '2026-06-03 11:06:43'),
(50, 53, 21, NULL, 'pending', '2026-06-03 15:06:42', '2026-06-03 15:06:42'),
(51, 54, 14, NULL, 'pending', '2026-06-03 15:07:35', '2026-06-03 15:07:35'),
(52, 55, 15, NULL, 'developing', '2026-06-03 15:12:17', '2026-06-03 15:12:17'),
(53, 56, 15, NULL, 'pending', '2026-06-03 15:12:17', '2026-06-03 15:12:17'),
(61, 64, 14, NULL, 'pending', '2026-06-08 01:50:56', '2026-06-08 01:50:56');

-- ============================================================
-- 5. task_status_histories（列顺序一致）
-- ============================================================
INSERT IGNORE INTO task_status_histories (id, task_id, from_status, to_status, changed_by, changed_at, comment) VALUES
(1, 3, 'pending', 'assigned_lead', 3, '2026-05-24 01:18:18', ''),
(2, 4, 'pending', 'assigned_lead', 1, '2026-05-24 18:25:34', ''),
(3, 5, 'pending', 'assigned_lead', 1, '2026-05-24 19:43:07', ''),
(4, 5, 'assigned_lead', 'developing', 4, '2026-05-24 19:46:20', ''),
(7, 5, 'developing', 'developed', 5, '2026-05-24 19:50:40', ''),
(8, 5, 'developed', 'pending_test', 5, '2026-05-24 19:50:40', '系统自动将任务加入测试池'),
(9, 5, 'pending_test', 'testing', 6, '2026-05-24 19:59:36', ''),
(10, 5, 'testing', 'passed', 6, '2026-05-24 19:59:44', ''),
(11, 5, 'passed', 'closed', 1, '2026-05-24 20:01:06', ''),
(12, 6, 'pending', 'assigned_lead', 1, '2026-05-24 21:25:03', NULL),
(13, 6, 'assigned_lead', 'developing', 8, '2026-05-24 21:30:01', NULL),
(14, 6, 'developing', 'developed', 7, '2026-05-24 21:38:20', NULL),
(15, 6, 'developed', 'pending_test', 7, '2026-05-24 21:38:20', '系统自动将任务加入测试池'),
(16, 32, 'pending', 'developing', 15, '2026-05-28 19:58:36', ''),
(17, 32, 'developing', 'testing', 15, '2026-05-29 10:06:52', ''),
(18, 34, 'pending', 'developing', 14, '2026-05-29 10:13:18', ''),
(19, 34, 'developing', 'testing', 14, '2026-05-29 10:16:59', ''),
(20, 39, 'pending', 'developing', 15, '2026-05-29 15:34:02', ''),
(21, 39, 'developing', 'testing', 15, '2026-05-29 16:18:02', ''),
(22, 40, 'pending', 'developing', 25, '2026-06-01 17:14:51', ''),
(23, 40, 'developing', 'testing', 25, '2026-06-01 17:15:17', ''),
(24, 42, 'pending', 'developing', 15, '2026-06-02 17:34:14', ''),
(25, 42, 'developing', 'testing', 15, '2026-06-02 17:44:53', ''),
(26, 43, 'pending', 'developing', 15, '2026-06-02 17:55:43', ''),
(27, 43, 'developing', 'testing', 15, '2026-06-02 18:15:05', ''),
(28, 50, 'pending', 'developing', 31, '2026-06-03 11:06:49', ''),
(29, 50, 'developing', 'testing', 31, '2026-06-03 11:10:11', ''),
(30, 54, 'pending', 'developing', 14, '2026-06-03 15:07:47', ''),
(31, 55, 'pending', 'developing', 15, '2026-06-03 15:12:31', ''),
(32, 55, 'developing', 'testing', 15, '2026-06-03 15:12:32', ''),
(33, 32, 'testing', 'developing', 18, '2026-06-04 18:06:51', '');

-- ============================================================
-- 6. task_progress_history（列顺序一致）
-- ============================================================
INSERT IGNORE INTO task_progress_history (id, task_id, progress, comment, created_by, created_at) VALUES
(1, 32, 30, NULL, 15, '2026-05-29 09:55:25'),
(2, 32, 80, NULL, 15, '2026-05-29 09:58:29'),
(3, 32, 100, NULL, 15, '2026-05-29 10:06:48'),
(4, 34, 100, NULL, 14, '2026-05-29 10:13:24'),
(5, 39, 47, NULL, 15, '2026-05-29 15:34:18'),
(6, 40, 70, NULL, 25, '2026-06-01 17:15:01'),
(7, 42, 26, NULL, 15, '2026-06-02 17:34:18'),
(8, 43, 33, NULL, 15, '2026-06-02 17:55:55'),
(9, 43, 15, NULL, 15, '2026-06-02 18:15:00'),
(10, 43, 100, '系统自动将进度设为100%（开发完成）', 15, '2026-06-02 18:15:05'),
(11, 50, 11, NULL, 31, '2026-06-03 11:06:52'),
(12, 50, 13, NULL, 31, '2026-06-03 11:07:53'),
(13, 50, 0, NULL, 31, '2026-06-03 11:07:59'),
(14, 50, 25, NULL, 31, '2026-06-03 11:08:00'),
(15, 50, 100, '系统自动将进度设为100%（开发完成）', 31, '2026-06-03 11:10:11'),
(16, 54, 32, NULL, 14, '2026-06-03 15:07:52'),
(17, 55, 100, '系统自动将进度设为100%（开发完成）', 15, '2026-06-03 15:12:32'),
(18, 54, 25, NULL, 14, '2026-06-07 22:50:50');

-- ============================================================
-- 7. bugs（列顺序与 management.sql / 实际 DB 一致）
-- ============================================================
INSERT IGNORE INTO bugs (id, title, description, severity, status, task_id, creator_id, assignee_id, remark, requirement_id, test_type, created_at, updated_at) VALUES
(2, '测试bug', '测试', 'medium', 'closed', 32, 18, 15, '111\n[历史重新打开原因] 未修复', 5, 'integration', '2026-05-29 16:54:15', '2026-05-29 16:54:15'),
(3, '111', '111', 'medium', 'closed', 43, 18, 15, '111', 12, 'integration', '2026-06-03 11:39:11', '2026-06-03 11:39:11'),
(4, '复用PV：秒杀/限购需求XQ_DFFX20251014_01：H5相关功能开发', '', 'medium', 'closed', 39, 18, 15, '已修复', 5, 'integration', '2026-06-07 18:54:37', '2026-06-07 18:54:37'),
(5, '后台', '111', 'medium', 'fixed', 34, 18, 14, '111', 5, 'integration', '2026-06-07 22:47:15', '2026-06-07 22:47:15'),
(6, '后台', '测试', 'medium', 'unfixed', 34, 19, 14, NULL, 5, 'integration', '2026-06-07 22:49:03', '2026-06-07 22:49:03');

-- ============================================================
-- 8. bug_images（列顺序一致）
-- ============================================================
INSERT IGNORE INTO bug_images (id, bug_id, image_path, image_name, image_size, created_at) VALUES
(1, 4, 'bugs/4/c3dd615d-b1a2-437c-ab71-83f7fa9ea649.jfif', 'OIP-A.jfif', 11568, '2026-06-07 18:54:37');

-- ============================================================
-- 9. bug_status_histories（列顺序一致）
-- ============================================================
INSERT IGNORE INTO bug_status_histories (id, bug_id, from_status, to_status, changed_by, changed_at, comment) VALUES
(7, 2, 'assigned', 'fixing', 15, '2026-05-29 16:55:11', ''),
(8, 2, 'fixing', 'fixed', 15, '2026-05-29 16:55:42', '1'),
(9, 2, 'fixed', 'pending_verify', 15, '2026-05-29 16:55:42', '系统自动将 Bug 置为待验证'),
(10, 3, 'assigned', 'fixing', 15, '2026-06-03 11:39:33', ''),
(11, 3, 'fixing', 'fixed', 15, '2026-06-03 11:39:41', '111'),
(12, 3, 'fixed', 'pending_verify', 15, '2026-06-03 11:39:41', '系统自动将 Bug 置为待验证'),
(13, 3, 'pending_verify', 'closed', 18, '2026-06-03 11:48:40', ''),
(14, 2, 'pending_verify', 'reopened', 18, '2026-06-03 11:48:55', '未修复'),
(15, 2, 'reopened', 'fixing', 15, '2026-06-03 11:53:37', ''),
(16, 2, 'fixing', 'fixed', 15, '2026-06-03 11:53:43', '111'),
(17, 2, 'pending_verify', 'closed', 18, '2026-06-03 11:54:11', ''),
(18, 4, 'unfixed', 'fixed', 15, '2026-06-07 21:57:16', '已修复'),
(19, 4, 'fixed', 'closed', 18, '2026-06-07 21:58:16', ''),
(20, 5, 'unfixed', 'fixed', 14, '2026-06-07 22:53:19', '111');

-- ============================================================
-- 10. workflow_rules（列顺序一致）
--     包含 management 的完整集合（含 feature 类型规则，init.sql/V19 不含）
--     使用 IGNORE 避免与已有数据冲突
-- ============================================================
INSERT IGNORE INTO workflow_rules (id, rule_type, role, from_status, to_status) VALUES
-- bug
(69, 'bug', 'dev', 'unfixed', 'fixed'),
(70, 'bug', 'dev', 'unfixed', 'not_a_bug'),
(71, 'bug', 'dev_lead', 'unfixed', 'fixed'),
(72, 'bug', 'dev_lead', 'unfixed', 'not_a_bug'),
(66, 'bug', 'tester', 'fixed', 'closed'),
(65, 'bug', 'tester', 'fixed', 'unfixed'),
(68, 'bug', 'tester', 'not_a_bug', 'closed'),
(67, 'bug', 'tester', 'not_a_bug', 'unfixed'),
-- feature (init 不含)
(44, 'feature', 'dev', 'developing', 'pending_test'),
(46, 'feature', 'pm', 'pending_test', 'closed'),
(43, 'feature', 'pm', 'planned', 'developing'),
(45, 'feature', 'tester', 'pending_test', 'closed'),
-- requirement
(40, 'requirement', 'pm', 'business_test', 'closed'),
(39, 'requirement', 'pm', 'business_test', 'pending_release'),
(36, 'requirement', 'pm', 'in_progress', 'closed'),
(35, 'requirement', 'pm', 'in_progress', 'integration_test'),
(37, 'requirement', 'pm', 'integration_test', 'business_test'),
(38, 'requirement', 'pm', 'integration_test', 'closed'),
(42, 'requirement', 'pm', 'pending_release', 'business_test'),
(41, 'requirement', 'pm', 'pending_release', 'released'),
(34, 'requirement', 'pm', 'planned', 'closed'),
(33, 'requirement', 'pm', 'planned', 'in_progress'),
-- task
(57, 'task', 'dev', 'developing', 'testing'),
(60, 'task', 'dev', 'pending', 'developing'),
(63, 'task', 'dev_lead', 'developing', 'testing'),
(56, 'task', 'dev_lead', 'pending', 'developing'),
(53, 'task', 'pm', 'developing', 'closed'),
(52, 'task', 'pm', 'developing', 'testing'),
(51, 'task', 'pm', 'pending', 'closed'),
(49, 'task', 'pm', 'pending', 'developing'),
(50, 'task', 'pm', 'pending', 'testing'),
(54, 'task', 'pm', 'testing', 'closed'),
(55, 'task', 'pm', 'testing', 'developing'),
(62, 'task', 'tester', 'pending', 'testing'),
(58, 'task', 'tester', 'testing', 'closed'),
(59, 'task', 'tester', 'testing', 'developing');

-- ============================================================
-- 11. requirements（列顺序与 management.sql / 实际 DB 一致）
-- ============================================================
INSERT IGNORE INTO requirements (id, requirement_id, number, title, description, notes, status, priority, `system`, project_id, project_type, person_id, person_name, total_amount, total_price, dev_total, dev_price, test_total, test_price, release_time, planned_completion_time, dev_lead_id, business_status, iteration_id, document_path, document_name, document_size, created_at, updated_at) VALUES
(5, 'REQ-2026-005', 'XQ_SUNNYCVM20260228_02', '复用PV：秒杀/限购需求XQ_DFFX20251014_01', '复用PV：秒杀/限购需求XQ_DFFX20251014_01', '第三点不需要（该需求需要强制更新）', 'in_progress', 'low', 'CV乘龙在线', 4, 'ops', NULL, '赖成勇', '5', '3500', '3', '700', '1', '700', NULL, '2026-05-26 00:00:00', 14, NULL, '2', '.\\uploads\\requirements\\5\\e351090f-6822-4427-a289-c1df650b4211.docx', 'XQ_DFFX20251014_01-秒杀需求设计.docx', 852284, '2026-05-26 16:37:46', '2026-05-26 16:37:46'),
(6, 'REQ-2026-006', 'XQ_SUNNYCVM20260513_01', NULL, '复用PV问卷星需求', '', 'in_progress', 'medium', 'CV乘龙在线', 4, 'ops', NULL, '', '7', '4900', '5', '700', '2', '700', NULL, '2026-05-28 08:00:00', 31, NULL, NULL, '.\\uploads\\requirements\\6\\1617c4ae-f8b0-48cf-9f76-48407b4daef2.docx', 'XQ_SUNNYCVM20260513_01-CV-问卷星需求设计方案.docx', 319646, '2026-05-29 11:41:08', '2026-05-29 11:41:08'),
(7, 'REQ-2026-007', 'XQ_SUNNYCVM20260520_03', NULL, '1、通过主数据车籍接口判断VIN的品系是否为"专用车"，若为专用车则不允许申请交车礼包，提示："抱歉！专用车品系不在新车大礼包活动享受范围内! 请更换其他车辆尝试!"。若不为专用车，则可以申请交车礼包。  2、APP/小程序：我的-设置-个人信息中的"详细信息"，参照商城的收货地址填写进行优化：选择所属地区、所属街道，填写详细地址', NULL, 'in_progress', 'medium', 'CV乘龙在线', 4, 'ops', NULL, '', '1.5', '1050', '1', '700', '0.5', '700', NULL, NULL, 31, NULL, '2', NULL, NULL, NULL, '2026-05-29 11:47:33', '2026-05-29 11:47:33'),
(8, 'REQ-2026-008', 'XQ_CLZX20251024_01', NULL, '复用PV：1、商城首页入口改造，新增在线点餐入口/聚合页+各产品商家(星巴克、肯德基、瑞幸、电影票)入口页（小程序/IOS/安卓/鸿蒙），商家清单信息，跳转链接可支持后台配置控制显示  2、星巴克、肯德基、瑞幸、电影票产品跳转H5及用户交互验证  3、APP（小程序/IOS/安卓/鸿蒙）新增点餐订单板块，点击跳转对方H5订单板块页面及用户交互验证，对方订单界面H5界面是聚合页可以查看各个商家的订单', NULL, 'in_progress', 'medium', 'CV乘龙在线', 6, 'ops', NULL, '', '10', '7000', '7', '700', '2', '700', NULL, NULL, 14, NULL, NULL, NULL, NULL, NULL, '2026-05-29 11:47:51', '2026-05-29 11:47:51'),
(9, 'REQ-2026-009', 'REQ-2026-009', NULL, 'APP，小程序首页快捷入口：新增入口"卡券福利"。点击跳转-我的-卡券中心', NULL, 'pending_task', 'medium', 'CV乘龙在线', 8, 'ops', NULL, '', '', '', '', '', '', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-29 11:49:00', '2026-05-29 11:49:00'),
(10, 'REQ-2026-010', 'REQ-2026-010', NULL, '车主认证提示文案修改：证件验证失败行驶证vin与购车发票被授权vin不一致，是否进行车主认证申诉 >>修改为：行驶证VIN与购车发票的VIN以及授权书的VIN不一致，是否进行车主认证申诉', NULL, 'in_progress', 'medium', 'CV乘龙在线', 8, 'ops', NULL, '', '', '', '', '', '', '', NULL, NULL, 31, NULL, NULL, NULL, NULL, NULL, '2026-05-29 11:49:12', '2026-05-29 11:49:12'),
(11, 'REQ-2026-011', 'REQ-2026-011', NULL, '1、单位车主认证-授权书模板下载，把原有模板换成PDF，APP支持下载，PDF见需求文档', NULL, 'in_progress', 'medium', 'CV乘龙在线', 8, 'ops', NULL, '', '', '', '', '', '', '', NULL, NULL, 14, NULL, NULL, '.\\uploads\\requirements\\11\\1d7bdd08-66e0-498f-8e3d-6ca965cbdb9b.pdf', '企业授权书模版.pdf', 147714, '2026-05-29 11:49:25', '2026-05-29 11:49:25'),
(12, 'REQ-2026-012', 'XQ_DFFX20260518_01', NULL, '1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。', NULL, 'pending_task', 'medium', 'PV东风风行', 4, 'ops', NULL, '', '8.5', '5950', '5.5', '700', '2', '700', NULL, NULL, 14, NULL, NULL, '.\\uploads\\requirements\\12\\3a2aa526-0a38-418b-a7b0-8cff83e3d082.docx', 'XQ_DFFX20260518_01-虚拟商品规格_线上点餐2阶段.docx', 604911, '2026-06-01 15:02:39', '2026-06-01 15:02:39');

-- ============================================================
-- 12. iterations（列顺序与 management.sql / 实际 DB 一致）
-- ============================================================
INSERT IGNORE INTO iterations (id, name, release_time, notes, release_notes, created_by, created_at) VALUES
(2, '2026-6月份迭代', '2026-06-03 16:00:00', NULL, NULL, 13, '2026-05-26 16:38:51');

-- ============================================================
-- 13. systems（列顺序与 management.sql / 实际 DB 一致）
-- ============================================================
INSERT IGNORE INTO systems (id, name, it_contact, biz_contact, tech_contact, address, created_by, created_at) VALUES
(3, 'CV乘龙在线', '罗谭菲', '赖成勇（商城负责人）\n陈语熠（客户运营负责人）\n丘俊欢（三方商城运营）\n席章璐（三方客户运营）', '罗世根、黄恒闹', '乘龙在线\nhttps://lqdm-cvapp-uat.dflzm.com/customer/cvapp-web-platform/ \nadmin /sdn_@KSFJ9@884!5\nBCenter\nhttps://lqdm-scrm-uat.dflzm.com/welcome/system', 13, '2026-05-26 16:21:36'),
(4, 'PV东风风行', '陆华丽', '赖成勇（商城负责人）\n农燕（商城负责人）\n姚悦（客户运营）\n韦小露（车联网）', '罗世根、黄恒闹', NULL, 13, '2026-05-26 16:22:06'),
(5, 'PVSCRM', '罗谭菲', '', '罗谭菲', NULL, 13, '2026-05-26 16:26:22'),
(6, 'CVSCRM', '罗谭菲', '', '', NULL, 13, '2026-05-26 16:27:20'),
(7, 'PV制程质量', '李林峰', '', '', NULL, 13, '2026-05-26 16:27:38'),
(8, 'CV制程质量', '李林峰', '', '', NULL, 13, '2026-05-26 16:27:51'),
(9, 'SBOM', '兰呈龙', '', '', NULL, 13, '2026-05-26 16:28:24'),
(10, 'CVCP', '李迺萍', '', '罗世根', '', 13, '2026-05-26 16:28:41'),
(11, 'PVCV', '李迺萍', '', '罗世根', '', 13, '2026-05-26 16:28:59'),
(12, 'CV服务配件系统', '韦涵', '', '', '测试环境账号地址：http://172.20.110.48:8888/\n内部公司账号：15778231743  密码：Jerry123456!\n柳汽关怀部账号：weihan  密码：Aa123456789*\n贵阳鸿兴账号：账号:1535ZZ 密码：oGe2iundefined7N\n', 13, '2026-05-26 16:30:36'),
(13, 'PV配件供应链管理系统', '刘玉善', '', '', NULL, 13, '2026-05-26 16:31:20'),
(14, 'E3S', '胡婷', '', '', NULL, 13, '2026-05-26 16:31:35'),
(15, 'VSM', '唐昱', '', '', NULL, 13, '2026-05-26 16:31:43'),
(16, '柳新CP', '李迺萍', '', '罗世根', NULL, 13, '2026-05-26 16:32:24');

-- ============================================================
-- 14. dictionaries（使用 IGNORE 避免与已有数据冲突）
-- ============================================================
INSERT IGNORE INTO dictionaries (id, dict_type, dict_key, dict_value, sort_order) VALUES
(7, 'source', 'internal', '内部需求', 1),
(8, 'source', 'external', '客户需求', 2),
(9, 'project_type', 'ops', '运维需求', 1),
(10, 'project_type', 'project', '项目需求', 2),
(11, 'skill', 'backend', '后台开发', 1),
(12, 'skill', 'ios', 'iOS开发', 2),
(13, 'skill', 'android', '安卓开发', 3),
(14, 'skill', 'harmony', '鸿蒙开发', 4),
(15, 'skill', 'miniapp', '小程序开发', 5),
(16, 'skill', 'h5', 'H5开发', 6),
(18, 'skill', 'other', '其他', 8);

-- ============================================================
-- 15. site_messages（列顺序一致）
-- ============================================================
INSERT IGNORE INTO site_messages (id, user_id, title, content, type, related_id, is_read, created_at, read_at) VALUES
(73, 14, '任务【1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同...', '任务【1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。】已创建，操作人：admin。请及时处理。', 'task', 64, 1, '2026-06-08 01:50:56', '2026-06-08 01:51:08');
