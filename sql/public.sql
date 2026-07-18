/*
 Navicat Premium Data Transfer

 Source Server         : dev_pg
 Source Server Type    : PostgreSQL
 Source Server Version : 160008 (160008)
 Source Host           : 192.168.0.23:25432
 Source Catalog        : carrot
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 160008 (160008)
 File Encoding         : 65001

 Date: 30/06/2026 14:13:33
*/


-- ----------------------------
-- Table structure for admin_node
-- ----------------------------
DROP TABLE IF EXISTS "public"."admin_node";
CREATE TABLE "public"."admin_node" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "ip" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "port" int4 NOT NULL DEFAULT 0,
  "weight" float8 NOT NULL DEFAULT 0,
  "at_time" int8 NOT NULL DEFAULT 0
)
;
COMMENT ON TABLE "public"."admin_node" IS '管理节点';

-- ----------------------------
-- Records of admin_node
-- ----------------------------
INSERT INTO "public"."admin_node" VALUES ('192.168.0.19:8848', '192.168.0.19', 8848, 0, 1782790040007);

-- ----------------------------
-- Table structure for config_item
-- ----------------------------
DROP TABLE IF EXISTS "public"."config_item";
CREATE TABLE "public"."config_item" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "namespace" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "group_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "data_id" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "content" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "md5" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "content_type" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "create_time" int8 NOT NULL DEFAULT 0,
  "update_time" int8 NOT NULL DEFAULT 0
)
;
COMMENT ON TABLE "public"."config_item" IS '配置中心-配置项';

-- ----------------------------
-- Records of config_item
-- ----------------------------
INSERT INTO "public"."config_item" VALUES ('public@DEFAULT_GROUP@test-demo.yml', 'public', 'DEFAULT_GROUP', 'test-demo.yml', 'server:
  port: 8101
test: test103', 'aa1c3c3d5cf300a101811faeeddcc7bc', 'YAML', 1782120900304, 1782554131290);

-- ----------------------------
-- Table structure for discovery_instance
-- ----------------------------
DROP TABLE IF EXISTS "public"."discovery_instance";
CREATE TABLE "public"."discovery_instance" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "namespace" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "group_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "service_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "instance_id" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "ip" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "port" int4 NOT NULL DEFAULT 0,
  "weight" float8 NOT NULL DEFAULT 0,
  "metadata" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "last_heartbeat_at" int8 NOT NULL DEFAULT 0,
  "create_time" int8 NOT NULL DEFAULT 0,
  "update_time" int8 NOT NULL DEFAULT 0
)
;
COMMENT ON TABLE "public"."discovery_instance" IS '注册发现-实例';

-- ----------------------------
-- Records of discovery_instance
-- ----------------------------

-- ----------------------------
-- Table structure for discovery_service
-- ----------------------------
DROP TABLE IF EXISTS "public"."discovery_service";
CREATE TABLE "public"."discovery_service" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "namespace" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "group_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "service_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "instance_count" int8 NOT NULL DEFAULT 0
)
;
COMMENT ON TABLE "public"."discovery_service" IS '服务发现-服务';

-- ----------------------------
-- Records of discovery_service
-- ----------------------------
INSERT INTO "public"."discovery_service" VALUES ('public:DEFAULT_GROUP:discovery-demo', 'public', 'DEFAULT_GROUP', 'discovery-demo', 1);

-- ----------------------------
-- Table structure for namespace
-- ----------------------------
DROP TABLE IF EXISTS "public"."namespace";
CREATE TABLE "public"."namespace" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" int8 NOT NULL DEFAULT 0,
  "name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "description" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
)
;
COMMENT ON TABLE "public"."namespace" IS '命名空间';

-- ----------------------------
-- Records of namespace
-- ----------------------------
INSERT INTO "public"."namespace" VALUES ('public', 0, 'public', 'public');
INSERT INTO "public"."namespace" VALUES ('a5168a3f-f3dd-430d-abc4-4bfd0578337f', 1782462179548, 'test123', 'test');
INSERT INTO "public"."namespace" VALUES ('carrot@admin', 0, 'carrot@admin', 'admin namespace');

-- ----------------------------
-- Table structure for scheduling_executor_node
-- ----------------------------
DROP TABLE IF EXISTS "public"."scheduling_executor_node";
CREATE TABLE "public"."scheduling_executor_node" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "ip" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "port" int4 NOT NULL DEFAULT 0,
  "update_time" int8 NOT NULL DEFAULT 0,
  "group_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "secret" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "executor_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "namespace_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "weight" float8 NOT NULL DEFAULT 0
)
;
COMMENT ON TABLE "public"."scheduling_executor_node" IS '执行器节点id';

-- ----------------------------
-- Records of scheduling_executor_node
-- ----------------------------
INSERT INTO "public"."scheduling_executor_node" VALUES ('carrot@admin:carrot-admin:admin:192.168.0.19:9966', '192.168.0.19', 9966, 1782790038313, 'admin', '6205a50a-9fd5-4a88-914b-71947d27c529', 'carrot-admin', 'carrot@admin', 0);

-- ----------------------------
-- Table structure for scheduling_job_record
-- ----------------------------
DROP TABLE IF EXISTS "public"."scheduling_job_record";
CREATE TABLE "public"."scheduling_job_record" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "unit_id" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "namespace_id" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "executor_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "group_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "ip" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "port" int4 NOT NULL DEFAULT 0,
  "secret" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "unit_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "plan_start_time" int8 NOT NULL DEFAULT 0,
  "actual_start_time" int8 NOT NULL DEFAULT 0,
  "actual_end_time" int8 NOT NULL DEFAULT 0,
  "code" int2 NOT NULL DEFAULT 0,
  "message" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "result" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "log" varchar[] COLLATE "pg_catalog"."default" NOT NULL DEFAULT ARRAY[]::character varying[],
  "hash_value" int8 NOT NULL DEFAULT 0
)
;
COMMENT ON TABLE "public"."scheduling_job_record" IS '调度执行日志';

-- ----------------------------
-- Records of scheduling_job_record
-- ----------------------------

-- ----------------------------
-- Table structure for scheduling_job_unit
-- ----------------------------
DROP TABLE IF EXISTS "public"."scheduling_job_unit";
CREATE TABLE "public"."scheduling_job_unit" (
  "id" varchar COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "executor_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "group_name" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "type" int2 NOT NULL DEFAULT 0,
  "type_value" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "namespace_id" varchar COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "last_plan_time" int8 NOT NULL DEFAULT 0,
  "hash_value" int8 NOT NULL DEFAULT 0,
  "enable" int2 NOT NULL DEFAULT 0
)
;
COMMENT ON TABLE "public"."scheduling_job_unit" IS '调度单元';

-- ----------------------------
-- Records of scheduling_job_unit
-- ----------------------------
INSERT INTO "public"."scheduling_job_unit" VALUES ('carrot@admin:carrot-admin:admin:clearExpiredExecutorNode', 'clearExpiredExecutorNode', 'carrot-admin', 'admin', 2, '29 * * * * ? ', 'carrot@admin', 1782790049000, 1083656731, 0);
INSERT INTO "public"."scheduling_job_unit" VALUES ('carrot@admin:carrot-admin:admin:clearExpiredAdmin', 'clearExpiredAdmin', 'carrot-admin', 'admin', 1, '15', 'carrot@admin', 1782790065000, 1550050527, 0);
INSERT INTO "public"."scheduling_job_unit" VALUES ('public:carrot-admin:default:removeJobRecordExpiredData', 'removeJobRecordExpiredData', 'carrot-admin', 'default', 2, '0 0,10,20,30,40,50 * * * ? ', 'public', 1782438600000, 1632571742, 1);
INSERT INTO "public"."scheduling_job_unit" VALUES ('public:carrot-admin:default:clearExpiredAdmin', 'clearExpiredAdmin', 'carrot-admin', 'default', 1, '15', 'public', 1782439200000, 10700442, 1);
INSERT INTO "public"."scheduling_job_unit" VALUES ('public:carrot-admin:default:clearExpiredExecutorNode', 'clearExpiredExecutorNode', 'carrot-admin', 'default', 2, '29 * * * * ? ', 'public', 1782439169000, 641336374, 1);
INSERT INTO "public"."scheduling_job_unit" VALUES ('carrot@admin:carrot-admin:admin:removeJobRecordExpiredData', 'removeJobRecordExpiredData', 'carrot-admin', 'admin', 2, '0 0,10,20,30,40,50 * * * ? ', 'carrot@admin', 1782789600000, 1500672515, 0);

-- ----------------------------
-- Table structure for scheduling_task_lock
-- ----------------------------
DROP TABLE IF EXISTS "public"."scheduling_task_lock";
CREATE TABLE "public"."scheduling_task_lock" (
  "value" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON TABLE "public"."scheduling_task_lock" IS '调度执行锁';

-- ----------------------------
-- Records of scheduling_task_lock
-- ----------------------------
INSERT INTO "public"."scheduling_task_lock" VALUES ('carrot@admin:carrot-admin:admin:clearExpiredAdmin1782696015000');
INSERT INTO "public"."scheduling_task_lock" VALUES ('carrot@admin:carrot-admin:admin:clearExpiredAdmin1782369960000');

-- ----------------------------
-- Primary Key structure for table admin_node
-- ----------------------------
ALTER TABLE "public"."admin_node" ADD CONSTRAINT "admin_node_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table config_item
-- ----------------------------
ALTER TABLE "public"."config_item" ADD CONSTRAINT "config_item_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table discovery_instance
-- ----------------------------
ALTER TABLE "public"."discovery_instance" ADD CONSTRAINT "discovery_instance_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table discovery_service
-- ----------------------------
ALTER TABLE "public"."discovery_service" ADD CONSTRAINT "discovery_service_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table namespace
-- ----------------------------
ALTER TABLE "public"."namespace" ADD CONSTRAINT "namespace_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table scheduling_executor_node
-- ----------------------------
ALTER TABLE "public"."scheduling_executor_node" ADD CONSTRAINT "scheduling_executor_node_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table scheduling_job_record
-- ----------------------------
ALTER TABLE "public"."scheduling_job_record" ADD CONSTRAINT "scheduling_job_record_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table scheduling_job_unit
-- ----------------------------
ALTER TABLE "public"."scheduling_job_unit" ADD CONSTRAINT "scheduling_job_unit_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table scheduling_task_lock
-- ----------------------------
ALTER TABLE "public"."scheduling_task_lock" ADD CONSTRAINT "scheduling_task_lock_pkey" PRIMARY KEY ("value");
