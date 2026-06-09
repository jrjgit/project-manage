/*
 Navicat Premium Data Transfer

 Source Server         : 本地Mysql库
 Source Server Type    : MySQL
 Source Server Version : 80016
 Source Host           : localhost:3306
 Source Schema         : management

 Target Server Type    : MySQL
 Target Server Version : 80016
 File Encoding         : 65001

 Date: 08/06/2026 20:42:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bug_images
-- ----------------------------
DROP TABLE IF EXISTS `bug_images`;
CREATE TABLE `bug_images`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `bug_id` bigint(20) NOT NULL COMMENT '关联Bug ID',
  `image_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片存储路径',
  `image_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原始文件名',
  `image_size` bigint(20) NULL DEFAULT NULL COMMENT '文件大小(字节)',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_bug_id`(`bug_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Bug截图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bug_images
-- ----------------------------
INSERT INTO `bug_images` VALUES (1, 4, 'bugs/4/c3dd615d-b1a2-437c-ab71-83f7fa9ea649.jfif', 'OIP-A.jfif', 11568, '2026-06-07 18:54:37');

-- ----------------------------
-- Table structure for bug_status_histories
-- ----------------------------
DROP TABLE IF EXISTS `bug_status_histories`;
CREATE TABLE `bug_status_histories`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bug_id` bigint(20) NOT NULL,
  `from_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `to_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `changed_by` bigint(20) NOT NULL,
  `changed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `bug_id`(`bug_id`) USING BTREE,
  INDEX `changed_by`(`changed_by`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bug_status_histories
-- ----------------------------
INSERT INTO `bug_status_histories` VALUES (7, 2, 'assigned', 'fixing', 15, '2026-05-29 16:55:11', '');
INSERT INTO `bug_status_histories` VALUES (8, 2, 'fixing', 'fixed', 15, '2026-05-29 16:55:42', '1');
INSERT INTO `bug_status_histories` VALUES (9, 2, 'fixed', 'pending_verify', 15, '2026-05-29 16:55:42', '系统自动将 Bug 置为待验证');
INSERT INTO `bug_status_histories` VALUES (10, 3, 'assigned', 'fixing', 15, '2026-06-03 11:39:33', '');
INSERT INTO `bug_status_histories` VALUES (11, 3, 'fixing', 'fixed', 15, '2026-06-03 11:39:41', '111');
INSERT INTO `bug_status_histories` VALUES (12, 3, 'fixed', 'pending_verify', 15, '2026-06-03 11:39:41', '系统自动将 Bug 置为待验证');
INSERT INTO `bug_status_histories` VALUES (13, 3, 'pending_verify', 'closed', 18, '2026-06-03 11:48:40', '');
INSERT INTO `bug_status_histories` VALUES (14, 2, 'pending_verify', 'reopened', 18, '2026-06-03 11:48:55', '未修复');
INSERT INTO `bug_status_histories` VALUES (15, 2, 'reopened', 'fixing', 15, '2026-06-03 11:53:37', '');
INSERT INTO `bug_status_histories` VALUES (16, 2, 'fixing', 'fixed', 15, '2026-06-03 11:53:43', '111');
INSERT INTO `bug_status_histories` VALUES (17, 2, 'pending_verify', 'closed', 18, '2026-06-03 11:54:11', '');
INSERT INTO `bug_status_histories` VALUES (18, 4, 'unfixed', 'fixed', 15, '2026-06-07 21:57:16', '已修复');
INSERT INTO `bug_status_histories` VALUES (19, 4, 'fixed', 'closed', 18, '2026-06-07 21:58:16', '');
INSERT INTO `bug_status_histories` VALUES (20, 5, 'unfixed', 'fixed', 14, '2026-06-07 22:53:19', '111');

-- ----------------------------
-- Table structure for bugs
-- ----------------------------
DROP TABLE IF EXISTS `bugs`;
CREATE TABLE `bugs`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `severity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'medium',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'unfixed',
  `task_id` bigint(20) NOT NULL,
  `creator_id` bigint(20) NOT NULL,
  `assignee_id` bigint(20) NULL DEFAULT NULL,
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `requirement_id` bigint(20) NULL DEFAULT NULL COMMENT '关联需求ID',
  `test_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'integration' COMMENT '测试类型: integration-综合测试, business-业务测试',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `task_id`(`task_id`) USING BTREE,
  INDEX `creator_id`(`creator_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bugs
-- ----------------------------
INSERT INTO `bugs` VALUES (2, '测试bug', '测试', 'medium', 'closed', 32, 18, 15, '111\n[历史重新打开原因] 未修复', '2026-05-29 16:54:15', '2026-05-29 16:54:15', 5, 'integration');
INSERT INTO `bugs` VALUES (3, '111', '111', 'medium', 'closed', 43, 18, 15, '111', '2026-06-03 11:39:11', '2026-06-03 11:39:11', 12, 'integration');
INSERT INTO `bugs` VALUES (4, '复用PV：秒杀/限购需求XQ_DFFX20251014_01：H5相关功能开发', '', 'medium', 'closed', 39, 18, 15, '已修复', '2026-06-07 18:54:37', '2026-06-07 18:54:37', 5, 'integration');
INSERT INTO `bugs` VALUES (5, '后台', '111', 'medium', 'fixed', 34, 18, 14, '111', '2026-06-07 22:47:15', '2026-06-07 22:47:15', 5, 'integration');
INSERT INTO `bugs` VALUES (6, '后台', '测试', 'medium', 'unfixed', 34, 19, 14, NULL, '2026-06-07 22:49:03', '2026-06-07 22:49:03', 5, 'integration');

-- ----------------------------
-- Table structure for dictionaries
-- ----------------------------
DROP TABLE IF EXISTS `dictionaries`;
CREATE TABLE `dictionaries`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dict_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型: system/source/project_type',
  `dict_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典键',
  `dict_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典值',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '基础字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dictionaries
-- ----------------------------
INSERT INTO `dictionaries` VALUES (7, 'source', 'internal', '内部需求', 1);
INSERT INTO `dictionaries` VALUES (8, 'source', 'external', '客户需求', 2);
INSERT INTO `dictionaries` VALUES (9, 'project_type', 'ops', '运维需求', 1);
INSERT INTO `dictionaries` VALUES (10, 'project_type', 'project', '项目需求', 2);
INSERT INTO `dictionaries` VALUES (11, 'skill', 'backend', '后台开发', 1);
INSERT INTO `dictionaries` VALUES (12, 'skill', 'ios', 'iOS开发', 2);
INSERT INTO `dictionaries` VALUES (13, 'skill', 'android', '安卓开发', 3);
INSERT INTO `dictionaries` VALUES (14, 'skill', 'harmony', '鸿蒙开发', 4);
INSERT INTO `dictionaries` VALUES (15, 'skill', 'miniapp', '小程序开发', 5);
INSERT INTO `dictionaries` VALUES (16, 'skill', 'h5', 'H5开发', 6);
INSERT INTO `dictionaries` VALUES (18, 'skill', 'other', '其他', 8);

-- ----------------------------
-- Table structure for groups
-- ----------------------------
DROP TABLE IF EXISTS `groups`;
CREATE TABLE `groups`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `pm_id` bigint(20) NOT NULL COMMENT '项目经理ID',
  `dev_lead_id` bigint(20) NOT NULL COMMENT '组长ID',
  `lead_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'dev' COMMENT 'dev=开发组 / test=测试组',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pm`(`pm_id`) USING BTREE,
  INDEX `idx_dev_lead`(`dev_lead_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '小组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of groups
-- ----------------------------

-- ----------------------------
-- Table structure for iterations
-- ----------------------------
DROP TABLE IF EXISTS `iterations`;
CREATE TABLE `iterations`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `iteration_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '迭代名称',
  `release_time` datetime NULL DEFAULT NULL COMMENT '计划发布时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `created_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `release_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '发布说明及注意事项',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_created_by`(`created_by`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '发布迭代表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iterations
-- ----------------------------
INSERT INTO `iterations` VALUES (2, 'ITER-2026-002', '2026-6月份迭代', '2026-06-03 16:00:00', '2026-05-26 16:38:51', '2026-05-26 16:38:51', NULL, 13, NULL);

-- ----------------------------
-- Table structure for projects
-- ----------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `pm_id` bigint(20) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目编号',
  `project_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目类型: invite_bidding/ops',
  `system_scope` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '系统范围(系统ID列表,逗号分隔)',
  `hr_scope` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '人力资源范围(用户ID列表,逗号分隔)',
  `created_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `pm_id`(`pm_id`) USING BTREE,
  INDEX `idx_created_by`(`created_by`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of projects
-- ----------------------------
INSERT INTO `projects` VALUES (4, '2026固定人力420', 13, '2026-05-26 16:33:10', NULL, 'ops', '4,3', '14,15,16,17,23,25,30,31,32,21', 13);
INSERT INTO `projects` VALUES (5, '2025SAP固定人力140', 13, '2026-05-26 16:33:38', NULL, 'ops', '4,3,5,6', '14,15,16,17', 13);
INSERT INTO `projects` VALUES (6, '2025-2026 年信息系统应需开发服务项目', 13, '2026-05-26 16:34:00', NULL, 'ops', '4,3', '14,15,16,17,21,22,23,25,29,30,31,32', 13);
INSERT INTO `projects` VALUES (7, '2025商城社区优化项目', 13, '2026-05-26 16:46:45', 'DFLQ-2025-IT0897-CV', 'invite_bidding', '4,3', '14,15,16,17,21,25,30,31,23,32', 13);
INSERT INTO `projects` VALUES (8, 'PVCV触点免费支持', 13, '2026-05-29 11:45:37', NULL, 'ops', '3,4', '17,16,15,14,20,21,23,30,31', 13);

-- ----------------------------
-- Table structure for requirements
-- ----------------------------
DROP TABLE IF EXISTS `requirements`;
CREATE TABLE `requirements`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `requirement_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '唯一标识 REQ-2026-001',
  `number` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '需求编号',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '需求标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详细描述',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'planned' COMMENT 'planned/in_progress/integration_test/business_test/pending_release/released/closed',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'medium' COMMENT 'low/medium/high/critical',
  `system` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属系统: backend/iOS/Android/鸿蒙/小程序/H5',
  `project_id` bigint(20) NULL DEFAULT NULL COMMENT '关联项目ID',
  `project_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ops=运维需求 / project=项目需求',
  `person_id` bigint(20) NULL DEFAULT NULL COMMENT '业务负责人ID',
  `total_amount` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '总工时/总价',
  `dev_total` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开发总工时',
  `dev_price` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开发单价',
  `test_total` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '测试总工时',
  `test_price` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '测试单价',
  `release_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `iteration_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联发布迭代ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注信息',
  `document_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文档存储路径',
  `document_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文档原始文件名',
  `document_size` bigint(20) NULL DEFAULT NULL COMMENT '文档大小(字节)',
  `dev_lead_id` bigint(20) NULL DEFAULT NULL COMMENT '开发组长ID',
  `total_price` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '总价',
  `planned_completion_time` datetime NULL DEFAULT NULL COMMENT '计划完成时间',
  `person_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务负责人(手输)',
  `business_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商务状态: pending_bid-待发标, pending_offer-待报价, bidding-待投标, won-已中标',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_project`(`project_id`) USING BTREE,
  INDEX `idx_person`(`person_id`) USING BTREE,
  INDEX `idx_iteration`(`iteration_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '需求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of requirements
-- ----------------------------
INSERT INTO `requirements` VALUES (5, 'REQ-2026-005', 'XQ_SUNNYCVM20260228_02', '复用PV：秒杀/限购需求XQ_DFFX20251014_01', '复用PV：秒杀/限购需求XQ_DFFX20251014_01', 'in_progress', 'low', 'CV乘龙在线', 4, 'ops', NULL, '5', '3', '700', '1', '700', NULL, '2', '2026-05-26 16:37:46', '2026-05-26 16:37:46', '第三点不需要（该需求需要强制更新）', '.\\uploads\\requirements\\5\\e351090f-6822-4427-a289-c1df650b4211.docx', 'XQ_DFFX20251014_01-秒杀需求设计.docx', 852284, 14, '3500', '2026-05-26 00:00:00', '赖成勇', NULL);
INSERT INTO `requirements` VALUES (6, 'REQ-2026-006', 'XQ_SUNNYCVM20260513_01', NULL, '复用PV问卷星需求', 'in_progress', 'medium', 'CV乘龙在线', 4, 'ops', NULL, '7', '5', '700', '2', '700', NULL, NULL, '2026-05-29 11:41:08', '2026-05-29 11:41:08', '', '.\\uploads\\requirements\\6\\1617c4ae-f8b0-48cf-9f76-48407b4daef2.docx', 'XQ_SUNNYCVM20260513_01-CV-问卷星需求设计方案.docx', 319646, 31, '4900', '2026-05-28 08:00:00', '', NULL);
INSERT INTO `requirements` VALUES (7, 'REQ-2026-007', 'XQ_SUNNYCVM20260520_03', NULL, '1、通过主数据车籍接口判断VIN的品系是否为“专用车”，若为专用车则不允许申请交车礼包，提示：“抱歉！专用车品系不在新车大礼包活动享受范围内! 请更换其他车辆尝试!”。若不为专用车，则可以申请交车礼包。  2、APP/小程序：我的-设置-个人信息中的“详细信息”，参照商城的收货地址填写进行优化：选择所属地区、所属街道，填写详细地址', 'in_progress', 'medium', 'CV乘龙在线', 4, 'ops', NULL, '1.5', '1', '700', '0.5', '700', NULL, '2', '2026-05-29 11:47:33', '2026-05-29 11:47:33', NULL, NULL, NULL, NULL, 31, '1050', NULL, '', NULL);
INSERT INTO `requirements` VALUES (8, 'REQ-2026-008', 'XQ_CLZX20251024_01', NULL, '复用PV：1、商城首页入口改造，新增在线点餐入口/聚合页+各产品商家(星巴克、肯德基、瑞幸、电影票)入口页（小程序/IOS/安卓/鸿蒙），商家清单信息，跳转链接可支持后台配置控制显示  2、星巴克、肯德基、瑞幸、电影票产品跳转H5及用户交互验证  3、APP（小程序/IOS/安卓/鸿蒙）新增点餐订单板块，点击跳转对方H5订单板块页面及用户交互验证，对方订单界面H5界面是聚合页可以查看各个商家的订单', 'in_progress', 'medium', 'CV乘龙在线', 6, 'ops', NULL, '10', '7', '700', '2', '700', NULL, NULL, '2026-05-29 11:47:51', '2026-05-29 11:47:51', NULL, NULL, NULL, NULL, 14, '7000', NULL, '', NULL);
INSERT INTO `requirements` VALUES (9, 'REQ-2026-009', 'REQ-2026-009', NULL, 'APP，小程序首页快捷入口：新增入口“卡券福利”。点击跳转-我的-卡券中心', 'pending_task', 'medium', 'CV乘龙在线', 8, 'ops', NULL, '', '', '', '', '', NULL, NULL, '2026-05-29 11:49:00', '2026-05-29 11:49:00', NULL, NULL, NULL, NULL, NULL, '', NULL, '', NULL);
INSERT INTO `requirements` VALUES (10, 'REQ-2026-010', 'REQ-2026-010', NULL, '车主认证提示文案修改：证件验证失败行驶证vin与购车发票被授权vin不一致，是否进行车主认证申诉 >>修改为：行驶证VIN与购车发票的VIN以及授权书的VIN不一致，是否进行车主认证申诉', 'in_progress', 'medium', 'CV乘龙在线', 8, 'ops', NULL, '', '', '', '', '', NULL, NULL, '2026-05-29 11:49:12', '2026-05-29 11:49:12', NULL, NULL, NULL, NULL, 31, '', NULL, '', NULL);
INSERT INTO `requirements` VALUES (11, 'REQ-2026-011', 'REQ-2026-011', NULL, '1、单位车主认证-授权书模板下载，把原有模板换成PDF，APP支持下载，PDF见需求文档', 'in_progress', 'medium', 'CV乘龙在线', 8, 'ops', NULL, '', '', '', '', '', NULL, NULL, '2026-05-29 11:49:25', '2026-05-29 11:49:25', NULL, '.\\uploads\\requirements\\11\\1d7bdd08-66e0-498f-8e3d-6ca965cbdb9b.pdf', '企业授权书模版.pdf', 147714, 14, '', NULL, '', NULL);
INSERT INTO `requirements` VALUES (12, 'REQ-2026-012', 'XQ_DFFX20260518_01', NULL, '1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。', 'pending_task', 'medium', 'PV东风风行', 4, 'ops', NULL, '8.5', '5.5', '700', '2', '700', NULL, NULL, '2026-06-01 15:02:39', '2026-06-01 15:02:39', NULL, '.\\uploads\\requirements\\12\\3a2aa526-0a38-418b-a7b0-8cff83e3d082.docx', 'XQ_DFFX20260518_01-虚拟商品规格_线上点餐2阶段.docx', 604911, 14, '5950', NULL, '', NULL);

-- ----------------------------
-- Table structure for site_messages
-- ----------------------------
DROP TABLE IF EXISTS `site_messages`;
CREATE TABLE `site_messages`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'system',
  `related_id` bigint(20) NULL DEFAULT NULL,
  `is_read` tinyint(4) NOT NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `read_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_user_read`(`user_id`, `is_read`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 73 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of site_messages
-- ----------------------------
INSERT INTO `site_messages` VALUES (73, 14, '任务【1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同...', '任务【1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。】已创建，操作人：admin。请及时处理。', 'task', 64, 1, '2026-06-08 01:50:56', '2026-06-08 01:51:08');

-- ----------------------------
-- Table structure for systems
-- ----------------------------
DROP TABLE IF EXISTS `systems`;
CREATE TABLE `systems`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '系统名称',
  `it_contact` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '甲方IT负责人',
  `biz_contact` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '甲方业务负责人',
  `tech_contact` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内部技术权限负责人',
  `created_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '系统地址',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_created_by`(`created_by`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of systems
-- ----------------------------
INSERT INTO `systems` VALUES (3, 'CV乘龙在线', '罗谭菲', '赖成勇（商城负责人）\n陈语熠（客户运营负责人）\n丘俊欢（三方商城运营）\n席章璐（三方客户运营）', '罗世根、黄恒闹', 13, '2026-05-26 16:21:36', '2026-05-26 16:21:36', '乘龙在线\nhttps://lqdm-cvapp-uat.dflzm.com/customer/cvapp-web-platform/ \nadmin /sdn_@KSFJ9@884!5\nBCenter\nhttps://lqdm-scrm-uat.dflzm.com/welcome/system');
INSERT INTO `systems` VALUES (4, 'PV东风风行', '陆华丽', '赖成勇（商城负责人）\n农燕（商城负责人）\n姚悦（客户运营）\n韦小露（车联网）', '罗世根、黄恒闹', 13, '2026-05-26 16:22:06', '2026-05-26 16:22:06', NULL);
INSERT INTO `systems` VALUES (5, 'PVSCRM', '罗谭菲', '', '罗谭菲', 13, '2026-05-26 16:26:22', '2026-05-26 16:26:22', NULL);
INSERT INTO `systems` VALUES (6, 'CVSCRM', '罗谭菲', '', '', 13, '2026-05-26 16:27:20', '2026-05-26 16:27:20', NULL);
INSERT INTO `systems` VALUES (7, 'PV制程质量', '李林峰', '', '', 13, '2026-05-26 16:27:38', '2026-05-26 16:27:38', NULL);
INSERT INTO `systems` VALUES (8, 'CV制程质量', '李林峰', '', '', 13, '2026-05-26 16:27:51', '2026-05-26 16:27:51', NULL);
INSERT INTO `systems` VALUES (9, 'SBOM', '兰呈龙', '', '', 13, '2026-05-26 16:28:24', '2026-05-26 16:28:24', NULL);
INSERT INTO `systems` VALUES (10, 'CVCP', '李迺萍', '', '罗世根', 13, '2026-05-26 16:28:41', '2026-05-26 16:28:41', NULL);
INSERT INTO `systems` VALUES (11, 'PVCV', '李迺萍', '', '罗世根', 13, '2026-05-26 16:28:59', '2026-05-26 16:28:59', '');
INSERT INTO `systems` VALUES (12, 'CV服务配件系统', '韦涵', '', '', 13, '2026-05-26 16:30:36', '2026-05-26 16:30:36', '测试环境账号地址：http://172.20.110.48:8888/\n内部公司账号：15778231743  密码：Jerry123456!\n柳汽关怀部账号：weihan  密码：Aa123456789*\n贵阳鸿兴账号：账号:1535ZZ 密码：oGe2iundefined7N\n');
INSERT INTO `systems` VALUES (13, 'PV配件供应链管理系统', '刘玉善', '', '', 13, '2026-05-26 16:31:20', '2026-05-26 16:31:20', NULL);
INSERT INTO `systems` VALUES (14, 'E3S', '胡婷', '', '', 13, '2026-05-26 16:31:35', '2026-05-26 16:31:35', NULL);
INSERT INTO `systems` VALUES (15, 'VSM', '唐昱', '', '', 13, '2026-05-26 16:31:43', '2026-05-26 16:31:43', NULL);
INSERT INTO `systems` VALUES (16, '柳新CP', '李迺萍', '', '罗世根', 13, '2026-05-26 16:32:24', '2026-05-26 16:32:24', NULL);

-- ----------------------------
-- Table structure for task_assignees
-- ----------------------------
DROP TABLE IF EXISTS `task_assignees`;
CREATE TABLE `task_assignees`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `platform` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `task_id`(`task_id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task_assignees
-- ----------------------------
INSERT INTO `task_assignees` VALUES (5, 8, 16, NULL, 'pending', '2026-05-27 16:32:02', '2026-05-27 16:32:02');
INSERT INTO `task_assignees` VALUES (6, 9, 14, NULL, 'pending', '2026-05-27 17:20:17', '2026-05-27 17:20:17');
INSERT INTO `task_assignees` VALUES (7, 10, 15, NULL, 'pending', '2026-05-27 17:20:17', '2026-05-27 17:20:17');
INSERT INTO `task_assignees` VALUES (8, 11, 15, NULL, 'pending', '2026-05-27 17:20:17', '2026-05-27 17:20:17');
INSERT INTO `task_assignees` VALUES (9, 12, 15, NULL, 'pending', '2026-05-27 17:20:18', '2026-05-27 17:20:18');
INSERT INTO `task_assignees` VALUES (10, 13, 14, NULL, 'pending', '2026-05-27 17:30:35', '2026-05-27 17:30:35');
INSERT INTO `task_assignees` VALUES (11, 14, 15, NULL, 'pending', '2026-05-27 17:30:35', '2026-05-27 17:30:35');
INSERT INTO `task_assignees` VALUES (12, 15, 15, NULL, 'pending', '2026-05-27 17:30:35', '2026-05-27 17:30:35');
INSERT INTO `task_assignees` VALUES (13, 16, 15, NULL, 'pending', '2026-05-27 17:30:35', '2026-05-27 17:30:35');
INSERT INTO `task_assignees` VALUES (14, 17, 14, NULL, 'pending', '2026-05-27 17:38:00', '2026-05-27 17:38:00');
INSERT INTO `task_assignees` VALUES (15, 18, 15, NULL, 'pending', '2026-05-27 17:38:00', '2026-05-27 17:38:00');
INSERT INTO `task_assignees` VALUES (16, 19, 15, NULL, 'pending', '2026-05-27 17:38:00', '2026-05-27 17:38:00');
INSERT INTO `task_assignees` VALUES (17, 20, 15, NULL, 'pending', '2026-05-27 17:38:00', '2026-05-27 17:38:00');
INSERT INTO `task_assignees` VALUES (18, 21, 16, NULL, 'pending', '2026-05-27 17:38:00', '2026-05-27 17:38:00');
INSERT INTO `task_assignees` VALUES (19, 22, 16, NULL, 'pending', '2026-05-27 17:38:01', '2026-05-27 17:38:01');
INSERT INTO `task_assignees` VALUES (20, 23, 16, NULL, 'pending', '2026-05-27 17:38:01', '2026-05-27 17:38:01');
INSERT INTO `task_assignees` VALUES (29, 32, 15, NULL, 'developing', '2026-05-28 14:37:40', '2026-05-28 14:37:40');
INSERT INTO `task_assignees` VALUES (31, 34, 17, NULL, 'pending', '2026-05-28 14:37:40', '2026-05-28 14:37:40');
INSERT INTO `task_assignees` VALUES (36, 39, 15, NULL, 'developing', '2026-05-29 10:54:53', '2026-05-29 10:54:53');
INSERT INTO `task_assignees` VALUES (37, 40, 25, NULL, 'developing', '2026-06-01 17:12:40', '2026-06-01 17:12:40');
INSERT INTO `task_assignees` VALUES (38, 41, 17, NULL, 'pending', '2026-06-01 17:12:41', '2026-06-01 17:12:41');
INSERT INTO `task_assignees` VALUES (40, 43, 15, NULL, 'developing', '2026-06-02 17:54:18', '2026-06-02 17:54:18');
INSERT INTO `task_assignees` VALUES (47, 50, 31, NULL, 'pending', '2026-06-03 11:05:59', '2026-06-03 11:05:59');
INSERT INTO `task_assignees` VALUES (48, 51, 30, NULL, 'pending', '2026-06-03 11:06:43', '2026-06-03 11:06:43');
INSERT INTO `task_assignees` VALUES (49, 52, 30, NULL, 'pending', '2026-06-03 11:06:43', '2026-06-03 11:06:43');
INSERT INTO `task_assignees` VALUES (50, 53, 21, NULL, 'pending', '2026-06-03 15:06:42', '2026-06-03 15:06:42');
INSERT INTO `task_assignees` VALUES (51, 54, 14, NULL, 'pending', '2026-06-03 15:07:35', '2026-06-03 15:07:35');
INSERT INTO `task_assignees` VALUES (52, 55, 15, NULL, 'developing', '2026-06-03 15:12:17', '2026-06-03 15:12:17');
INSERT INTO `task_assignees` VALUES (53, 56, 15, NULL, 'pending', '2026-06-03 15:12:17', '2026-06-03 15:12:17');
INSERT INTO `task_assignees` VALUES (61, 64, 14, NULL, 'pending', '2026-06-08 01:50:56', '2026-06-08 01:50:56');

-- ----------------------------
-- Table structure for task_progress_history
-- ----------------------------
DROP TABLE IF EXISTS `task_progress_history`;
CREATE TABLE `task_progress_history`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) NOT NULL,
  `progress` int(11) NOT NULL COMMENT '0-100',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_by` bigint(20) NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_id`(`task_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务进度上报历史' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task_progress_history
-- ----------------------------
INSERT INTO `task_progress_history` VALUES (1, 32, 30, NULL, 15, '2026-05-29 09:55:25');
INSERT INTO `task_progress_history` VALUES (2, 32, 80, NULL, 15, '2026-05-29 09:58:29');
INSERT INTO `task_progress_history` VALUES (3, 32, 100, NULL, 15, '2026-05-29 10:06:48');
INSERT INTO `task_progress_history` VALUES (4, 34, 100, NULL, 14, '2026-05-29 10:13:24');
INSERT INTO `task_progress_history` VALUES (5, 39, 47, NULL, 15, '2026-05-29 15:34:18');
INSERT INTO `task_progress_history` VALUES (6, 40, 70, NULL, 25, '2026-06-01 17:15:01');
INSERT INTO `task_progress_history` VALUES (7, 42, 26, NULL, 15, '2026-06-02 17:34:18');
INSERT INTO `task_progress_history` VALUES (8, 43, 33, NULL, 15, '2026-06-02 17:55:55');
INSERT INTO `task_progress_history` VALUES (9, 43, 15, NULL, 15, '2026-06-02 18:15:00');
INSERT INTO `task_progress_history` VALUES (10, 43, 100, '系统自动将进度设为100%（开发完成）', 15, '2026-06-02 18:15:05');
INSERT INTO `task_progress_history` VALUES (11, 50, 11, NULL, 31, '2026-06-03 11:06:52');
INSERT INTO `task_progress_history` VALUES (12, 50, 13, NULL, 31, '2026-06-03 11:07:53');
INSERT INTO `task_progress_history` VALUES (13, 50, 0, NULL, 31, '2026-06-03 11:07:59');
INSERT INTO `task_progress_history` VALUES (14, 50, 25, NULL, 31, '2026-06-03 11:08:00');
INSERT INTO `task_progress_history` VALUES (15, 50, 100, '系统自动将进度设为100%（开发完成）', 31, '2026-06-03 11:10:11');
INSERT INTO `task_progress_history` VALUES (16, 54, 32, NULL, 14, '2026-06-03 15:07:52');
INSERT INTO `task_progress_history` VALUES (17, 55, 100, '系统自动将进度设为100%（开发完成）', 15, '2026-06-03 15:12:32');
INSERT INTO `task_progress_history` VALUES (18, 54, 25, NULL, 14, '2026-06-07 22:50:50');

-- ----------------------------
-- Table structure for task_status_histories
-- ----------------------------
DROP TABLE IF EXISTS `task_status_histories`;
CREATE TABLE `task_status_histories`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) NOT NULL,
  `from_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `to_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `changed_by` bigint(20) NOT NULL,
  `changed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `task_id`(`task_id`) USING BTREE,
  INDEX `changed_by`(`changed_by`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task_status_histories
-- ----------------------------
INSERT INTO `task_status_histories` VALUES (1, 3, 'pending', 'assigned_lead', 3, '2026-05-24 01:18:18', '');
INSERT INTO `task_status_histories` VALUES (2, 4, 'pending', 'assigned_lead', 1, '2026-05-24 18:25:34', '');
INSERT INTO `task_status_histories` VALUES (3, 5, 'pending', 'assigned_lead', 1, '2026-05-24 19:43:07', '');
INSERT INTO `task_status_histories` VALUES (4, 5, 'assigned_lead', 'developing', 4, '2026-05-24 19:46:20', '');
INSERT INTO `task_status_histories` VALUES (7, 5, 'developing', 'developed', 5, '2026-05-24 19:50:40', '');
INSERT INTO `task_status_histories` VALUES (8, 5, 'developed', 'pending_test', 5, '2026-05-24 19:50:40', '系统自动将任务加入测试池');
INSERT INTO `task_status_histories` VALUES (9, 5, 'pending_test', 'testing', 6, '2026-05-24 19:59:36', '');
INSERT INTO `task_status_histories` VALUES (10, 5, 'testing', 'passed', 6, '2026-05-24 19:59:44', '');
INSERT INTO `task_status_histories` VALUES (11, 5, 'passed', 'closed', 1, '2026-05-24 20:01:06', '');
INSERT INTO `task_status_histories` VALUES (12, 6, 'pending', 'assigned_lead', 1, '2026-05-24 21:25:03', NULL);
INSERT INTO `task_status_histories` VALUES (13, 6, 'assigned_lead', 'developing', 8, '2026-05-24 21:30:01', NULL);
INSERT INTO `task_status_histories` VALUES (14, 6, 'developing', 'developed', 7, '2026-05-24 21:38:20', NULL);
INSERT INTO `task_status_histories` VALUES (15, 6, 'developed', 'pending_test', 7, '2026-05-24 21:38:20', '系统自动将任务加入测试池');
INSERT INTO `task_status_histories` VALUES (16, 32, 'pending', 'developing', 15, '2026-05-28 19:58:36', '');
INSERT INTO `task_status_histories` VALUES (17, 32, 'developing', 'testing', 15, '2026-05-29 10:06:52', '');
INSERT INTO `task_status_histories` VALUES (18, 34, 'pending', 'developing', 14, '2026-05-29 10:13:18', '');
INSERT INTO `task_status_histories` VALUES (19, 34, 'developing', 'testing', 14, '2026-05-29 10:16:59', '');
INSERT INTO `task_status_histories` VALUES (20, 39, 'pending', 'developing', 15, '2026-05-29 15:34:02', '');
INSERT INTO `task_status_histories` VALUES (21, 39, 'developing', 'testing', 15, '2026-05-29 16:18:02', '');
INSERT INTO `task_status_histories` VALUES (22, 40, 'pending', 'developing', 25, '2026-06-01 17:14:51', '');
INSERT INTO `task_status_histories` VALUES (23, 40, 'developing', 'testing', 25, '2026-06-01 17:15:17', '');
INSERT INTO `task_status_histories` VALUES (24, 42, 'pending', 'developing', 15, '2026-06-02 17:34:14', '');
INSERT INTO `task_status_histories` VALUES (25, 42, 'developing', 'testing', 15, '2026-06-02 17:44:53', '');
INSERT INTO `task_status_histories` VALUES (26, 43, 'pending', 'developing', 15, '2026-06-02 17:55:43', '');
INSERT INTO `task_status_histories` VALUES (27, 43, 'developing', 'testing', 15, '2026-06-02 18:15:05', '');
INSERT INTO `task_status_histories` VALUES (28, 50, 'pending', 'developing', 31, '2026-06-03 11:06:49', '');
INSERT INTO `task_status_histories` VALUES (29, 50, 'developing', 'testing', 31, '2026-06-03 11:10:11', '');
INSERT INTO `task_status_histories` VALUES (30, 54, 'pending', 'developing', 14, '2026-06-03 15:07:47', '');
INSERT INTO `task_status_histories` VALUES (31, 55, 'pending', 'developing', 15, '2026-06-03 15:12:31', '');
INSERT INTO `task_status_histories` VALUES (32, 55, 'developing', 'testing', 15, '2026-06-03 15:12:32', '');
INSERT INTO `task_status_histories` VALUES (33, 32, 'testing', 'developing', 18, '2026-06-04 18:06:51', '');

-- ----------------------------
-- Table structure for tasks
-- ----------------------------
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pending',
  `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'medium',
  `project_id` bigint(20) NULL DEFAULT NULL,
  `creator_id` bigint(20) NOT NULL,
  `assignee_id` bigint(20) NULL DEFAULT NULL,
  `dev_lead_id` bigint(20) NULL DEFAULT NULL,
  `tester_id` bigint(20) NULL DEFAULT NULL,
  `reject_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `deadline` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `requirement_id` bigint(20) NULL DEFAULT NULL COMMENT '关联需求ID',
  `terminal` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '终端',
  `progress` int(11) NULL DEFAULT 0 COMMENT '进度百分比',
  `performance` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绩效数值',
  `iteration_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联发布迭代ID',
  `test_performance` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '测试绩效',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `project_id`(`project_id`) USING BTREE,
  INDEX `creator_id`(`creator_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tasks
-- ----------------------------
INSERT INTO `tasks` VALUES (32, '安卓开发', '测试', 'developing', 'medium', 4, 13, 15, 14, 18, NULL, '2026-06-03 00:00:00', '2026-05-28 14:37:40', '2026-05-28 14:37:40', 5, 'android', 100, '2', NULL, NULL);
INSERT INTO `tasks` VALUES (34, '后台', '测试', 'testing', 'medium', 4, 13, 14, 14, 18, NULL, '2026-06-04 00:00:00', '2026-05-28 14:37:40', '2026-05-28 14:37:40', 5, 'backend', 100, '2', NULL, NULL);
INSERT INTO `tasks` VALUES (39, '复用PV：秒杀/限购需求XQ_DFFX20251014_01：H5相关功能开发', '无', 'testing', 'medium', 4, 13, 15, 14, NULL, NULL, '2026-05-29 16:00:00', '2026-05-29 10:54:53', '2026-05-29 10:54:53', 5, 'h5', 47, '1', NULL, NULL);
INSERT INTO `tasks` VALUES (40, '134', 't', 'testing', 'medium', 4, 13, 25, 14, NULL, NULL, '2026-06-20 16:00:00', '2026-06-01 17:12:40', '2026-06-01 17:12:40', 12, 'backend', 70, '2', NULL, NULL);
INSERT INTO `tasks` VALUES (41, '5', 't', 'pending', 'medium', 4, 13, 17, 14, NULL, NULL, '2026-06-20 16:00:00', '2026-06-01 17:12:41', '2026-06-01 17:12:41', 12, 'backend', 0, '1', NULL, NULL);
INSERT INTO `tasks` VALUES (43, '1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。', '测试新界面', 'testing', 'medium', 4, 14, 15, 14, NULL, NULL, '2026-05-31 08:00:00', '2026-06-02 17:54:18', '2026-06-02 17:54:18', 12, 'android', 100, '2', NULL, '1');
INSERT INTO `tasks` VALUES (50, '车主认证提示文案修改：证件验证失败行驶证vin与购车发票被授权vin不一致，是否进行车主认证申诉 >>修改为：行驶证VIN与购车发票的VIN以及授权书的VIN不一致，是否进行车主认证申诉', '任务2,3', 'testing', 'medium', 8, 31, 31, 31, NULL, NULL, '2026-06-04 08:00:00', '2026-06-03 11:05:59', '2026-06-03 11:05:59', 10, 'backend', 100, '3', NULL, '3');
INSERT INTO `tasks` VALUES (51, '车主认证提示文案修改：证件验证失败行驶证vin与购车发票被授权vin不一致，是否进行车主认证申诉 >>修改为：行驶证VIN与购车发票的VIN以及授权书的VIN不一致，是否进行车主认证申诉', '任务1', 'pending', 'medium', 8, 31, 30, 31, NULL, NULL, '2026-06-04 16:00:00', '2026-06-03 11:06:43', '2026-06-03 11:06:43', 10, 'harmony', 0, '2', NULL, '3');
INSERT INTO `tasks` VALUES (52, '车主认证提示文案修改：证件验证失败行驶证vin与购车发票被授权vin不一致，是否进行车主认证申诉 >>修改为：行驶证VIN与购车发票的VIN以及授权书的VIN不一致，是否进行车主认证申诉', '任务1', 'pending', 'medium', 8, 31, 30, 31, NULL, NULL, '2026-06-04 16:00:00', '2026-06-03 11:06:43', '2026-06-03 11:06:43', 10, 'miniapp', 0, '3', NULL, '3');
INSERT INTO `tasks` VALUES (53, '1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。', NULL, 'pending', 'medium', 4, 14, 21, 14, NULL, NULL, NULL, '2026-06-03 15:06:42', '2026-06-03 15:06:42', 12, 'backend', 0, NULL, NULL, NULL);
INSERT INTO `tasks` VALUES (54, '1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。', NULL, 'developing', 'medium', 4, 14, 14, 14, NULL, NULL, NULL, '2026-06-03 15:07:35', '2026-06-03 15:07:35', 12, 'backend', 25, NULL, NULL, NULL);
INSERT INTO `tasks` VALUES (55, 'APP，小程序首页快捷入口：新增入口“卡券福利”。点击跳转-我的-卡券中心', '2', 'testing', 'medium', 8, 13, 15, NULL, NULL, NULL, '2026-06-27 16:00:00', '2026-06-03 15:12:17', '2026-06-03 15:12:17', 9, 'android', 100, '3', NULL, '1');
INSERT INTO `tasks` VALUES (56, 'APP，小程序首页快捷入口：新增入口“卡券福利”。点击跳转-我的-卡券中心', '3', 'pending', 'medium', 8, 13, 15, NULL, NULL, NULL, '2026-06-27 16:00:00', '2026-06-03 15:12:17', '2026-06-03 15:12:17', 9, 'harmony', 0, '3', NULL, '2');
INSERT INTO `tasks` VALUES (64, '1、增加虚拟商品规格迭代2功能开发\n2、线上点餐在内嵌鸿联九五H5的基础上，增加订单关联数据同步。如部分退款、整单退款。', '测试，测试', 'pending', 'medium', 4, 13, 14, 14, NULL, NULL, '2026-06-08 16:00:00', '2026-06-08 01:50:56', '2026-06-08 01:50:56', 12, 'backend', 0, '1', NULL, '1');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录账号',
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `skills` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '技能列表,逗号分隔: iOS,Android,鸿蒙,小程序,H5,后台,前端,其他',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (13, 'admin', '$2a$10$aJF885siaj8Eeg7HHitx9eRsb46zXCbbpQ7Qaq0ngsfqanKXB2Kne', 'admin', 'pm', NULL, '2026-05-26 16:01:30', NULL);
INSERT INTO `users` VALUES (14, '黄恒闹', '$2a$10$NB0e4O15ft4TJtj3hJdKyeOzqCe3H.hmd5q6U2ey3MbHrF.YgtNvm', 'huanghn', 'dev_lead', NULL, '2026-05-26 16:48:05', 'backend');
INSERT INTO `users` VALUES (15, '陶天德', '$2a$10$GAONognHvEEtwkkssm9HUOzvfUaILW.KEjtw/JPorL23jL7l7s/yG', 'taotd', 'dev', NULL, '2026-05-27 16:08:39', 'android,harmony,h5');
INSERT INTO `users` VALUES (16, '伍开敏', '$2a$10$ySpxTVBaYmryao8N7ih2eOETGBad4vpuCI4gL5nj4LLmZi6icAdYe', 'wukm', 'dev', NULL, '2026-05-27 16:08:56', 'ios,miniapp,h5');
INSERT INTO `users` VALUES (17, '郭才伟', '$2a$10$Q80htt00MS2W8QksG9W95OoRnnD6VLHAwJmVtafOD1yxA7S9Jyanq', 'guocw', 'dev', NULL, '2026-05-27 16:09:11', 'backend');
INSERT INTO `users` VALUES (18, '韦俊', '$2a$10$GgnugPiTFByf1IpAubEBH.FdybzqLCOF9P8oQq4AdjbNz4lztknHO', 'weij', 'tester', NULL, '2026-05-27 16:09:30', NULL);
INSERT INTO `users` VALUES (19, '刘婧怡', '$2a$10$FYuL2OZADxSM08Z3GmrwQ.o3tuQfU584DuvVWYZ0fLy4hEmm0zscC', 'liujy', 'tester', NULL, '2026-05-27 16:09:51', NULL);
INSERT INTO `users` VALUES (20, '兰伟民', '$2a$10$mNUyMWaMb8DVz2vmvOk50.yC3yVijIPsO8EQBDK.qX9K9zodBukK2', 'lanwm', 'pm', NULL, '2026-06-01 15:03:58', NULL);
INSERT INTO `users` VALUES (21, '邓乃月', '$2a$10$YpZSH4.GuzM87poQ8b458ur/gpCH9dodnURAlUB4FlMZUZsQcgQHW', 'dengny', 'dev', NULL, '2026-06-01 15:07:14', 'backend');
INSERT INTO `users` VALUES (22, '黄俊钢', '$2a$10$3Db6hkPSNk8fp.YRmgYexe4vcJusOifnh79fgXrlQuXSkXeizsffq', 'huangjg', 'dev', NULL, '2026-06-01 15:07:34', 'backend');
INSERT INTO `users` VALUES (23, '黄盟琅', '$2a$10$iF45KOoJBua.zcLWO8MH1epQ3u5RTjyLILnApU6blCyI/WURICqTa', 'huangml', 'dev', NULL, '2026-06-01 15:08:41', 'backend');
INSERT INTO `users` VALUES (24, '黄新洪', '$2a$10$RGHzVRXJIhdU3V.hMRNEfOW1EYmx/yG0/ZA.InLMbUybdvLqWNewe', 'huangxh', 'dev', NULL, '2026-06-01 15:09:09', 'backend');
INSERT INTO `users` VALUES (25, '蒋荣江', '$2a$10$iWxlWYO29gh2xtHs78tWm.nupn9R8Y2VXHrLdaIONsR/.IvHzUr2a', 'jiangrj', 'dev', NULL, '2026-06-01 15:09:45', 'backend');
INSERT INTO `users` VALUES (26, '兰素琼', '$2a$10$SiFOw3xaKtM3nR501/Bbve6m62wLFetbflh0XukkSo7rS.uzdj1B2', 'lansq', 'pm', NULL, '2026-06-01 15:11:36', NULL);
INSERT INTO `users` VALUES (27, '李均连', '$2a$10$lG0KTV4.3q/1TLDMIFW/8ei.fqxbLf5MbwjOqtVeiAPhddc9zSuTa', 'lijl', 'pm', NULL, '2026-06-01 15:11:50', NULL);
INSERT INTO `users` VALUES (28, '刘新', '$2a$10$l6Cv9q2y4o01QzEsXIQCn.F2wfurIk/5V4s/LcNWYAmjONJyzU.LK', 'liux', 'dev_lead', NULL, '2026-06-01 15:12:08', 'backend');
INSERT INTO `users` VALUES (29, '罗世根', '$2a$10$a4o7ZA2dUcLo/bx/HET3juo9th8TVcgVCgoSIxXgQggHIugntmrou', 'luosg', 'dev_lead', NULL, '2026-06-01 15:12:19', 'backend,harmony,android');
INSERT INTO `users` VALUES (30, '秦雪峰', '$2a$10$I17RoEBi04qvFWySUPx2sOIcI2AtRnZ.F4j0gRoidloAleNSYAadO', 'qinxf', 'dev', NULL, '2026-06-01 15:12:33', 'harmony,miniapp,h5');
INSERT INTO `users` VALUES (31, '王家程', '$2a$10$i2piVYnB05H.csOC5s2dI..AoBDmuCILVGLdbMAPOU4hVWRP6B83.', 'wangjc', 'dev_lead', NULL, '2026-06-01 15:12:49', 'backend');
INSERT INTO `users` VALUES (32, '韦黄顺', '$2a$10$TJY6tkNGeHaz0KQW6EW0b.XWMKBjnKU8u5yUXVCYQm4DWD4K6zsKi', 'weihs', 'dev', NULL, '2026-06-01 15:13:01', 'backend');
INSERT INTO `users` VALUES (33, '代忠英', '$2a$10$lnE2fxqta/RD0AYPhGP4o.YDrtXlQW8dIa0PSI1jkoj3M.79vynlO', 'daizy', 'tester', NULL, '2026-06-02 11:06:55', NULL);

-- ----------------------------
-- Table structure for workflow_rules
-- ----------------------------
DROP TABLE IF EXISTS `workflow_rules`;
CREATE TABLE `workflow_rules`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rule_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'task / bug / requirement / feature',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'pm / dev_lead / dev / tester_lead / tester',
  `from_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `to_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rule`(`rule_type`, `role`, `from_status`, `to_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 72 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工作流规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of workflow_rules
-- ----------------------------
INSERT INTO `workflow_rules` VALUES (69, 'bug', 'dev', 'unfixed', 'fixed');
INSERT INTO `workflow_rules` VALUES (70, 'bug', 'dev', 'unfixed', 'not_a_bug');
INSERT INTO `workflow_rules` VALUES (71, 'bug', 'dev_lead', 'unfixed', 'fixed');
INSERT INTO `workflow_rules` VALUES (72, 'bug', 'dev_lead', 'unfixed', 'not_a_bug');
INSERT INTO `workflow_rules` VALUES (66, 'bug', 'tester', 'fixed', 'closed');
INSERT INTO `workflow_rules` VALUES (65, 'bug', 'tester', 'fixed', 'unfixed');
INSERT INTO `workflow_rules` VALUES (68, 'bug', 'tester', 'not_a_bug', 'closed');
INSERT INTO `workflow_rules` VALUES (67, 'bug', 'tester', 'not_a_bug', 'unfixed');
INSERT INTO `workflow_rules` VALUES (44, 'feature', 'dev', 'developing', 'pending_test');
INSERT INTO `workflow_rules` VALUES (46, 'feature', 'pm', 'pending_test', 'closed');
INSERT INTO `workflow_rules` VALUES (43, 'feature', 'pm', 'planned', 'developing');
INSERT INTO `workflow_rules` VALUES (45, 'feature', 'tester', 'pending_test', 'closed');
INSERT INTO `workflow_rules` VALUES (40, 'requirement', 'pm', 'business_test', 'closed');
INSERT INTO `workflow_rules` VALUES (39, 'requirement', 'pm', 'business_test', 'pending_release');
INSERT INTO `workflow_rules` VALUES (36, 'requirement', 'pm', 'in_progress', 'closed');
INSERT INTO `workflow_rules` VALUES (35, 'requirement', 'pm', 'in_progress', 'integration_test');
INSERT INTO `workflow_rules` VALUES (37, 'requirement', 'pm', 'integration_test', 'business_test');
INSERT INTO `workflow_rules` VALUES (38, 'requirement', 'pm', 'integration_test', 'closed');
INSERT INTO `workflow_rules` VALUES (42, 'requirement', 'pm', 'pending_release', 'business_test');
INSERT INTO `workflow_rules` VALUES (41, 'requirement', 'pm', 'pending_release', 'released');
INSERT INTO `workflow_rules` VALUES (34, 'requirement', 'pm', 'planned', 'closed');
INSERT INTO `workflow_rules` VALUES (33, 'requirement', 'pm', 'planned', 'in_progress');
INSERT INTO `workflow_rules` VALUES (57, 'task', 'dev', 'developing', 'testing');
INSERT INTO `workflow_rules` VALUES (60, 'task', 'dev', 'pending', 'developing');
INSERT INTO `workflow_rules` VALUES (63, 'task', 'dev_lead', 'developing', 'testing');
INSERT INTO `workflow_rules` VALUES (56, 'task', 'dev_lead', 'pending', 'developing');
INSERT INTO `workflow_rules` VALUES (53, 'task', 'pm', 'developing', 'closed');
INSERT INTO `workflow_rules` VALUES (52, 'task', 'pm', 'developing', 'testing');
INSERT INTO `workflow_rules` VALUES (51, 'task', 'pm', 'pending', 'closed');
INSERT INTO `workflow_rules` VALUES (49, 'task', 'pm', 'pending', 'developing');
INSERT INTO `workflow_rules` VALUES (50, 'task', 'pm', 'pending', 'testing');
INSERT INTO `workflow_rules` VALUES (54, 'task', 'pm', 'testing', 'closed');
INSERT INTO `workflow_rules` VALUES (55, 'task', 'pm', 'testing', 'developing');
INSERT INTO `workflow_rules` VALUES (62, 'task', 'tester', 'pending', 'testing');
INSERT INTO `workflow_rules` VALUES (58, 'task', 'tester', 'testing', 'closed');
INSERT INTO `workflow_rules` VALUES (59, 'task', 'tester', 'testing', 'developing');

-- ----------------------------
-- Procedure structure for drop_col_if_exists
-- ----------------------------
DROP PROCEDURE IF EXISTS `drop_col_if_exists`;
delimiter ;;
CREATE PROCEDURE `drop_col_if_exists`(IN tbl VARCHAR(255), IN col VARCHAR(255))
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', tbl, ' DROP COLUMN ', col);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
