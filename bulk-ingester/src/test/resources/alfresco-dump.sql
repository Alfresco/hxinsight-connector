--
-- PostgreSQL database dump
--

-- Dumped from database version 14.4 (Debian 14.4-1.pgdg110+1)
-- Dumped by pg_dump version 16.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: alfresco
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO alfresco;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: act_evt_log; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_evt_log (
    log_nr_ integer NOT NULL,
    type_ character varying(64),
    proc_def_id_ character varying(64),
    proc_inst_id_ character varying(64),
    execution_id_ character varying(64),
    task_id_ character varying(64),
    time_stamp_ timestamp without time zone NOT NULL,
    user_id_ character varying(255),
    data_ bytea,
    lock_owner_ character varying(255),
    lock_time_ timestamp without time zone,
    is_processed_ smallint DEFAULT 0
);


ALTER TABLE public.act_evt_log OWNER TO alfresco;

--
-- Name: act_evt_log_log_nr__seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.act_evt_log_log_nr__seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.act_evt_log_log_nr__seq OWNER TO alfresco;

--
-- Name: act_evt_log_log_nr__seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: alfresco
--

ALTER SEQUENCE public.act_evt_log_log_nr__seq OWNED BY public.act_evt_log.log_nr_;


--
-- Name: act_ge_bytearray; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_ge_bytearray (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    name_ character varying(255),
    deployment_id_ character varying(64),
    bytes_ bytea,
    generated_ boolean
);


ALTER TABLE public.act_ge_bytearray OWNER TO alfresco;

--
-- Name: act_ge_property; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_ge_property (
    name_ character varying(64) NOT NULL,
    value_ character varying(300),
    rev_ integer
);


ALTER TABLE public.act_ge_property OWNER TO alfresco;

--
-- Name: act_hi_actinst; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_hi_actinst (
    id_ character varying(64) NOT NULL,
    proc_def_id_ character varying(64) NOT NULL,
    proc_inst_id_ character varying(64) NOT NULL,
    execution_id_ character varying(64) NOT NULL,
    act_id_ character varying(255) NOT NULL,
    task_id_ character varying(64),
    call_proc_inst_id_ character varying(64),
    act_name_ character varying(255),
    act_type_ character varying(255) NOT NULL,
    assignee_ character varying(255),
    start_time_ timestamp without time zone NOT NULL,
    end_time_ timestamp without time zone,
    duration_ bigint,
    tenant_id_ character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.act_hi_actinst OWNER TO alfresco;

--
-- Name: act_hi_attachment; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_hi_attachment (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    user_id_ character varying(255),
    name_ character varying(255),
    description_ character varying(4000),
    type_ character varying(255),
    task_id_ character varying(64),
    proc_inst_id_ character varying(64),
    url_ character varying(4000),
    content_id_ character varying(64),
    time_ timestamp without time zone
);


ALTER TABLE public.act_hi_attachment OWNER TO alfresco;

--
-- Name: act_hi_comment; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_hi_comment (
    id_ character varying(64) NOT NULL,
    type_ character varying(255),
    time_ timestamp without time zone NOT NULL,
    user_id_ character varying(255),
    task_id_ character varying(64),
    proc_inst_id_ character varying(64),
    action_ character varying(255),
    message_ character varying(4000),
    full_msg_ bytea
);


ALTER TABLE public.act_hi_comment OWNER TO alfresco;

--
-- Name: act_hi_detail; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_hi_detail (
    id_ character varying(64) NOT NULL,
    type_ character varying(255) NOT NULL,
    proc_inst_id_ character varying(64),
    execution_id_ character varying(64),
    task_id_ character varying(64),
    act_inst_id_ character varying(64),
    name_ character varying(255) NOT NULL,
    var_type_ character varying(64),
    rev_ integer,
    time_ timestamp without time zone NOT NULL,
    bytearray_id_ character varying(64),
    double_ double precision,
    long_ bigint,
    text_ character varying(4000),
    text2_ character varying(4000)
);


ALTER TABLE public.act_hi_detail OWNER TO alfresco;

--
-- Name: act_hi_identitylink; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_hi_identitylink (
    id_ character varying(64) NOT NULL,
    group_id_ character varying(255),
    type_ character varying(255),
    user_id_ character varying(255),
    task_id_ character varying(64),
    proc_inst_id_ character varying(64)
);


ALTER TABLE public.act_hi_identitylink OWNER TO alfresco;

--
-- Name: act_hi_procinst; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_hi_procinst (
    id_ character varying(64) NOT NULL,
    proc_inst_id_ character varying(64) NOT NULL,
    business_key_ character varying(255),
    proc_def_id_ character varying(64) NOT NULL,
    start_time_ timestamp without time zone NOT NULL,
    end_time_ timestamp without time zone,
    duration_ bigint,
    start_user_id_ character varying(255),
    start_act_id_ character varying(255),
    end_act_id_ character varying(255),
    super_process_instance_id_ character varying(64),
    delete_reason_ character varying(4000),
    tenant_id_ character varying(255) DEFAULT ''::character varying,
    name_ character varying(255)
);


ALTER TABLE public.act_hi_procinst OWNER TO alfresco;

--
-- Name: act_hi_taskinst; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_hi_taskinst (
    id_ character varying(64) NOT NULL,
    proc_def_id_ character varying(64),
    task_def_key_ character varying(255),
    proc_inst_id_ character varying(64),
    execution_id_ character varying(64),
    name_ character varying(255),
    parent_task_id_ character varying(64),
    description_ character varying(4000),
    owner_ character varying(255),
    assignee_ character varying(255),
    start_time_ timestamp without time zone NOT NULL,
    claim_time_ timestamp without time zone,
    end_time_ timestamp without time zone,
    duration_ bigint,
    delete_reason_ character varying(4000),
    priority_ integer,
    due_date_ timestamp without time zone,
    form_key_ character varying(255),
    category_ character varying(255),
    tenant_id_ character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.act_hi_taskinst OWNER TO alfresco;

--
-- Name: act_hi_varinst; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_hi_varinst (
    id_ character varying(64) NOT NULL,
    proc_inst_id_ character varying(64),
    execution_id_ character varying(64),
    task_id_ character varying(64),
    name_ character varying(255) NOT NULL,
    var_type_ character varying(100),
    rev_ integer,
    bytearray_id_ character varying(64),
    double_ double precision,
    long_ bigint,
    text_ character varying(4000),
    text2_ character varying(4000),
    create_time_ timestamp without time zone,
    last_updated_time_ timestamp without time zone
);


ALTER TABLE public.act_hi_varinst OWNER TO alfresco;

--
-- Name: act_id_group; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_id_group (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    name_ character varying(255),
    type_ character varying(255)
);


ALTER TABLE public.act_id_group OWNER TO alfresco;

--
-- Name: act_id_info; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_id_info (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    user_id_ character varying(64),
    type_ character varying(64),
    key_ character varying(255),
    value_ character varying(255),
    password_ bytea,
    parent_id_ character varying(255)
);


ALTER TABLE public.act_id_info OWNER TO alfresco;

--
-- Name: act_id_membership; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_id_membership (
    user_id_ character varying(64) NOT NULL,
    group_id_ character varying(64) NOT NULL
);


ALTER TABLE public.act_id_membership OWNER TO alfresco;

--
-- Name: act_id_user; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_id_user (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    first_ character varying(255),
    last_ character varying(255),
    email_ character varying(255),
    pwd_ character varying(255),
    picture_id_ character varying(64)
);


ALTER TABLE public.act_id_user OWNER TO alfresco;

--
-- Name: act_procdef_info; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_procdef_info (
    id_ character varying(64) NOT NULL,
    proc_def_id_ character varying(64) NOT NULL,
    rev_ integer,
    info_json_id_ character varying(64)
);


ALTER TABLE public.act_procdef_info OWNER TO alfresco;

--
-- Name: act_re_deployment; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_re_deployment (
    id_ character varying(64) NOT NULL,
    name_ character varying(255),
    category_ character varying(255),
    tenant_id_ character varying(255) DEFAULT ''::character varying,
    deploy_time_ timestamp without time zone
);


ALTER TABLE public.act_re_deployment OWNER TO alfresco;

--
-- Name: act_re_model; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_re_model (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    name_ character varying(255),
    key_ character varying(255),
    category_ character varying(255),
    create_time_ timestamp without time zone,
    last_update_time_ timestamp without time zone,
    version_ integer,
    meta_info_ character varying(4000),
    deployment_id_ character varying(64),
    editor_source_value_id_ character varying(64),
    editor_source_extra_value_id_ character varying(64),
    tenant_id_ character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.act_re_model OWNER TO alfresco;

--
-- Name: act_re_procdef; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_re_procdef (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    category_ character varying(255),
    name_ character varying(255),
    key_ character varying(255) NOT NULL,
    version_ integer NOT NULL,
    deployment_id_ character varying(64),
    resource_name_ character varying(4000),
    dgrm_resource_name_ character varying(4000),
    description_ character varying(4000),
    has_start_form_key_ boolean,
    has_graphical_notation_ boolean,
    suspension_state_ integer,
    tenant_id_ character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.act_re_procdef OWNER TO alfresco;

--
-- Name: act_ru_event_subscr; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_ru_event_subscr (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    event_type_ character varying(255) NOT NULL,
    event_name_ character varying(255),
    execution_id_ character varying(64),
    proc_inst_id_ character varying(64),
    activity_id_ character varying(64),
    configuration_ character varying(255),
    created_ timestamp without time zone NOT NULL,
    proc_def_id_ character varying(64),
    tenant_id_ character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.act_ru_event_subscr OWNER TO alfresco;

--
-- Name: act_ru_execution; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_ru_execution (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    proc_inst_id_ character varying(64),
    business_key_ character varying(255),
    parent_id_ character varying(64),
    proc_def_id_ character varying(64),
    super_exec_ character varying(64),
    act_id_ character varying(255),
    is_active_ boolean,
    is_concurrent_ boolean,
    is_scope_ boolean,
    is_event_scope_ boolean,
    suspension_state_ integer,
    cached_ent_state_ integer,
    tenant_id_ character varying(255) DEFAULT ''::character varying,
    name_ character varying(255),
    lock_time_ timestamp without time zone
);


ALTER TABLE public.act_ru_execution OWNER TO alfresco;

--
-- Name: act_ru_identitylink; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_ru_identitylink (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    group_id_ character varying(255),
    type_ character varying(255),
    user_id_ character varying(255),
    task_id_ character varying(64),
    proc_inst_id_ character varying(64),
    proc_def_id_ character varying(64)
);


ALTER TABLE public.act_ru_identitylink OWNER TO alfresco;

--
-- Name: act_ru_job; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_ru_job (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    type_ character varying(255) NOT NULL,
    lock_exp_time_ timestamp without time zone,
    lock_owner_ character varying(255),
    exclusive_ boolean,
    execution_id_ character varying(64),
    process_instance_id_ character varying(64),
    proc_def_id_ character varying(64),
    retries_ integer,
    exception_stack_id_ character varying(64),
    exception_msg_ character varying(4000),
    duedate_ timestamp without time zone,
    repeat_ character varying(255),
    handler_type_ character varying(255),
    handler_cfg_ character varying(4000),
    tenant_id_ character varying(255) DEFAULT ''::character varying
);


ALTER TABLE public.act_ru_job OWNER TO alfresco;

--
-- Name: act_ru_task; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_ru_task (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    execution_id_ character varying(64),
    proc_inst_id_ character varying(64),
    proc_def_id_ character varying(64),
    name_ character varying(255),
    parent_task_id_ character varying(64),
    description_ character varying(4000),
    task_def_key_ character varying(255),
    owner_ character varying(255),
    assignee_ character varying(255),
    delegation_ character varying(64),
    priority_ integer,
    create_time_ timestamp without time zone,
    due_date_ timestamp without time zone,
    category_ character varying(255),
    suspension_state_ integer,
    tenant_id_ character varying(255) DEFAULT ''::character varying,
    form_key_ character varying(255)
);


ALTER TABLE public.act_ru_task OWNER TO alfresco;

--
-- Name: act_ru_variable; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.act_ru_variable (
    id_ character varying(64) NOT NULL,
    rev_ integer,
    type_ character varying(255) NOT NULL,
    name_ character varying(255) NOT NULL,
    execution_id_ character varying(64),
    proc_inst_id_ character varying(64),
    task_id_ character varying(64),
    bytearray_id_ character varying(64),
    double_ double precision,
    long_ bigint,
    text_ character varying(4000),
    text2_ character varying(4000)
);


ALTER TABLE public.act_ru_variable OWNER TO alfresco;

--
-- Name: alf_access_control_entry; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_access_control_entry (
    id bigint NOT NULL,
    version bigint NOT NULL,
    permission_id bigint NOT NULL,
    authority_id bigint NOT NULL,
    allowed boolean NOT NULL,
    applies integer NOT NULL,
    context_id bigint
);


ALTER TABLE public.alf_access_control_entry OWNER TO alfresco;

--
-- Name: alf_access_control_entry_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_access_control_entry_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_access_control_entry_seq OWNER TO alfresco;

--
-- Name: alf_access_control_list; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_access_control_list (
    id bigint NOT NULL,
    version bigint NOT NULL,
    acl_id character varying(36) NOT NULL,
    latest boolean NOT NULL,
    acl_version bigint NOT NULL,
    inherits boolean NOT NULL,
    inherits_from bigint,
    type integer NOT NULL,
    inherited_acl bigint,
    is_versioned boolean NOT NULL,
    requires_version boolean NOT NULL,
    acl_change_set bigint
);


ALTER TABLE public.alf_access_control_list OWNER TO alfresco;

--
-- Name: alf_access_control_list_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_access_control_list_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_access_control_list_seq OWNER TO alfresco;

--
-- Name: alf_ace_context; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_ace_context (
    id bigint NOT NULL,
    version bigint NOT NULL,
    class_context character varying(1024),
    property_context character varying(1024),
    kvp_context character varying(1024)
);


ALTER TABLE public.alf_ace_context OWNER TO alfresco;

--
-- Name: alf_ace_context_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_ace_context_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_ace_context_seq OWNER TO alfresco;

--
-- Name: alf_acl_change_set; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_acl_change_set (
    id bigint NOT NULL,
    commit_time_ms bigint
);


ALTER TABLE public.alf_acl_change_set OWNER TO alfresco;

--
-- Name: alf_acl_change_set_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_acl_change_set_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_acl_change_set_seq OWNER TO alfresco;

--
-- Name: alf_acl_member; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_acl_member (
    id bigint NOT NULL,
    version bigint NOT NULL,
    acl_id bigint NOT NULL,
    ace_id bigint NOT NULL,
    pos integer NOT NULL
);


ALTER TABLE public.alf_acl_member OWNER TO alfresco;

--
-- Name: alf_acl_member_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_acl_member_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_acl_member_seq OWNER TO alfresco;

--
-- Name: alf_activity_feed; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_activity_feed (
    id bigint NOT NULL,
    post_id bigint,
    post_date timestamp without time zone NOT NULL,
    activity_summary character varying(1024),
    feed_user_id character varying(255),
    activity_type character varying(255) NOT NULL,
    site_network character varying(255),
    app_tool character varying(36),
    post_user_id character varying(255) NOT NULL,
    feed_date timestamp without time zone NOT NULL
);


ALTER TABLE public.alf_activity_feed OWNER TO alfresco;

--
-- Name: alf_activity_feed_control; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_activity_feed_control (
    id bigint NOT NULL,
    feed_user_id character varying(255) NOT NULL,
    site_network character varying(255),
    app_tool character varying(36),
    last_modified timestamp without time zone NOT NULL
);


ALTER TABLE public.alf_activity_feed_control OWNER TO alfresco;

--
-- Name: alf_activity_feed_control_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_activity_feed_control_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_activity_feed_control_seq OWNER TO alfresco;

--
-- Name: alf_activity_feed_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_activity_feed_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_activity_feed_seq OWNER TO alfresco;

--
-- Name: alf_activity_post; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_activity_post (
    sequence_id bigint NOT NULL,
    post_date timestamp without time zone NOT NULL,
    status character varying(10) NOT NULL,
    activity_data character varying(1024) NOT NULL,
    post_user_id character varying(255) NOT NULL,
    job_task_node integer NOT NULL,
    site_network character varying(255),
    app_tool character varying(36),
    activity_type character varying(255) NOT NULL,
    last_modified timestamp without time zone NOT NULL
);


ALTER TABLE public.alf_activity_post OWNER TO alfresco;

--
-- Name: alf_activity_post_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_activity_post_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_activity_post_seq OWNER TO alfresco;

--
-- Name: alf_applied_patch; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_applied_patch (
    id character varying(64) NOT NULL,
    description character varying(1024),
    fixes_from_schema integer,
    fixes_to_schema integer,
    applied_to_schema integer,
    target_schema integer,
    applied_on_date timestamp without time zone,
    applied_to_server character varying(64),
    was_executed boolean,
    succeeded boolean,
    report character varying(1024)
);


ALTER TABLE public.alf_applied_patch OWNER TO alfresco;

--
-- Name: alf_audit_app; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_audit_app (
    id bigint NOT NULL,
    version integer NOT NULL,
    app_name_id bigint NOT NULL,
    audit_model_id bigint NOT NULL,
    disabled_paths_id bigint NOT NULL
);


ALTER TABLE public.alf_audit_app OWNER TO alfresco;

--
-- Name: alf_audit_app_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_audit_app_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_audit_app_seq OWNER TO alfresco;

--
-- Name: alf_audit_entry; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_audit_entry (
    id bigint NOT NULL,
    audit_app_id bigint NOT NULL,
    audit_time bigint NOT NULL,
    audit_user_id bigint,
    audit_values_id bigint
);


ALTER TABLE public.alf_audit_entry OWNER TO alfresco;

--
-- Name: alf_audit_entry_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_audit_entry_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_audit_entry_seq OWNER TO alfresco;

--
-- Name: alf_audit_model; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_audit_model (
    id bigint NOT NULL,
    content_data_id bigint NOT NULL,
    content_crc bigint NOT NULL
);


ALTER TABLE public.alf_audit_model OWNER TO alfresco;

--
-- Name: alf_audit_model_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_audit_model_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_audit_model_seq OWNER TO alfresco;

--
-- Name: alf_auth_status; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_auth_status (
    id bigint NOT NULL,
    username character varying(100) NOT NULL,
    deleted boolean NOT NULL,
    authorized boolean NOT NULL,
    checksum bytea NOT NULL,
    authaction character varying(10) NOT NULL
);


ALTER TABLE public.alf_auth_status OWNER TO alfresco;

--
-- Name: alf_auth_status_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_auth_status_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_auth_status_seq OWNER TO alfresco;

--
-- Name: alf_authority; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_authority (
    id bigint NOT NULL,
    version bigint NOT NULL,
    authority character varying(100),
    crc bigint
);


ALTER TABLE public.alf_authority OWNER TO alfresco;

--
-- Name: alf_authority_alias; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_authority_alias (
    id bigint NOT NULL,
    version bigint NOT NULL,
    auth_id bigint NOT NULL,
    alias_id bigint NOT NULL
);


ALTER TABLE public.alf_authority_alias OWNER TO alfresco;

--
-- Name: alf_authority_alias_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_authority_alias_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_authority_alias_seq OWNER TO alfresco;

--
-- Name: alf_authority_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_authority_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_authority_seq OWNER TO alfresco;

--
-- Name: alf_child_assoc; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_child_assoc (
    id bigint NOT NULL,
    version bigint NOT NULL,
    parent_node_id bigint NOT NULL,
    type_qname_id bigint NOT NULL,
    child_node_name_crc bigint NOT NULL,
    child_node_name character varying(50) NOT NULL,
    child_node_id bigint NOT NULL,
    qname_ns_id bigint NOT NULL,
    qname_localname character varying(255) NOT NULL,
    qname_crc bigint NOT NULL,
    is_primary boolean,
    assoc_index integer
);


ALTER TABLE public.alf_child_assoc OWNER TO alfresco;

--
-- Name: alf_child_assoc_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_child_assoc_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_child_assoc_seq OWNER TO alfresco;

--
-- Name: alf_content_data; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_content_data (
    id bigint NOT NULL,
    version bigint NOT NULL,
    content_url_id bigint,
    content_mimetype_id bigint,
    content_encoding_id bigint,
    content_locale_id bigint
);


ALTER TABLE public.alf_content_data OWNER TO alfresco;

--
-- Name: alf_content_data_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_content_data_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_content_data_seq OWNER TO alfresco;

--
-- Name: alf_content_url; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_content_url (
    id bigint NOT NULL,
    content_url character varying(255) NOT NULL,
    content_url_short character varying(12) NOT NULL,
    content_url_crc bigint NOT NULL,
    content_size bigint NOT NULL,
    orphan_time bigint
);


ALTER TABLE public.alf_content_url OWNER TO alfresco;

--
-- Name: alf_content_url_enc_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_content_url_enc_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_content_url_enc_seq OWNER TO alfresco;

--
-- Name: alf_content_url_encryption; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_content_url_encryption (
    id bigint NOT NULL,
    content_url_id bigint NOT NULL,
    algorithm character varying(10) NOT NULL,
    key_size integer NOT NULL,
    encrypted_key bytea NOT NULL,
    master_keystore_id character varying(20) NOT NULL,
    master_key_alias character varying(15) NOT NULL,
    unencrypted_file_size bigint
);


ALTER TABLE public.alf_content_url_encryption OWNER TO alfresco;

--
-- Name: alf_content_url_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_content_url_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_content_url_seq OWNER TO alfresco;

--
-- Name: alf_encoding; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_encoding (
    id bigint NOT NULL,
    version bigint NOT NULL,
    encoding_str character varying(100) NOT NULL
);


ALTER TABLE public.alf_encoding OWNER TO alfresco;

--
-- Name: alf_encoding_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_encoding_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_encoding_seq OWNER TO alfresco;

--
-- Name: alf_locale; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_locale (
    id bigint NOT NULL,
    version bigint NOT NULL,
    locale_str character varying(20) NOT NULL
);


ALTER TABLE public.alf_locale OWNER TO alfresco;

--
-- Name: alf_locale_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_locale_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_locale_seq OWNER TO alfresco;

--
-- Name: alf_lock; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_lock (
    id bigint NOT NULL,
    version bigint NOT NULL,
    shared_resource_id bigint NOT NULL,
    excl_resource_id bigint NOT NULL,
    lock_token character varying(36) NOT NULL,
    start_time bigint NOT NULL,
    expiry_time bigint NOT NULL
);


ALTER TABLE public.alf_lock OWNER TO alfresco;

--
-- Name: alf_lock_resource; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_lock_resource (
    id bigint NOT NULL,
    version bigint NOT NULL,
    qname_ns_id bigint NOT NULL,
    qname_localname character varying(255) NOT NULL
);


ALTER TABLE public.alf_lock_resource OWNER TO alfresco;

--
-- Name: alf_lock_resource_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_lock_resource_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_lock_resource_seq OWNER TO alfresco;

--
-- Name: alf_lock_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_lock_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_lock_seq OWNER TO alfresco;

--
-- Name: alf_mimetype; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_mimetype (
    id bigint NOT NULL,
    version bigint NOT NULL,
    mimetype_str character varying(100) NOT NULL
);


ALTER TABLE public.alf_mimetype OWNER TO alfresco;

--
-- Name: alf_mimetype_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_mimetype_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_mimetype_seq OWNER TO alfresco;

--
-- Name: alf_namespace; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_namespace (
    id bigint NOT NULL,
    version bigint NOT NULL,
    uri character varying(100) NOT NULL
);


ALTER TABLE public.alf_namespace OWNER TO alfresco;

--
-- Name: alf_namespace_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_namespace_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_namespace_seq OWNER TO alfresco;

--
-- Name: alf_node; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_node (
    id bigint NOT NULL,
    version bigint NOT NULL,
    store_id bigint NOT NULL,
    uuid character varying(36) NOT NULL,
    transaction_id bigint NOT NULL,
    type_qname_id bigint NOT NULL,
    locale_id bigint NOT NULL,
    acl_id bigint,
    audit_creator character varying(255),
    audit_created character varying(30),
    audit_modifier character varying(255),
    audit_modified character varying(30),
    audit_accessed character varying(30)
);


ALTER TABLE public.alf_node OWNER TO alfresco;

--
-- Name: alf_node_aspects; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_node_aspects (
    node_id bigint NOT NULL,
    qname_id bigint NOT NULL
);


ALTER TABLE public.alf_node_aspects OWNER TO alfresco;

--
-- Name: alf_node_assoc; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_node_assoc (
    id bigint NOT NULL,
    version bigint NOT NULL,
    source_node_id bigint NOT NULL,
    target_node_id bigint NOT NULL,
    type_qname_id bigint NOT NULL,
    assoc_index bigint NOT NULL
);


ALTER TABLE public.alf_node_assoc OWNER TO alfresco;

--
-- Name: alf_node_assoc_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_node_assoc_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_node_assoc_seq OWNER TO alfresco;

--
-- Name: alf_node_properties; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_node_properties (
    node_id bigint NOT NULL,
    actual_type_n integer NOT NULL,
    persisted_type_n integer NOT NULL,
    boolean_value boolean,
    long_value bigint,
    float_value real,
    double_value double precision,
    string_value character varying(1024),
    serializable_value bytea,
    qname_id bigint NOT NULL,
    list_index integer NOT NULL,
    locale_id bigint NOT NULL
);


ALTER TABLE public.alf_node_properties OWNER TO alfresco;

--
-- Name: alf_node_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_node_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_node_seq OWNER TO alfresco;

--
-- Name: alf_permission; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_permission (
    id bigint NOT NULL,
    version bigint NOT NULL,
    type_qname_id bigint NOT NULL,
    name character varying(100) NOT NULL
);


ALTER TABLE public.alf_permission OWNER TO alfresco;

--
-- Name: alf_permission_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_permission_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_permission_seq OWNER TO alfresco;

--
-- Name: alf_prop_class; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_prop_class (
    id bigint NOT NULL,
    java_class_name character varying(255) NOT NULL,
    java_class_name_short character varying(32) NOT NULL,
    java_class_name_crc bigint NOT NULL
);


ALTER TABLE public.alf_prop_class OWNER TO alfresco;

--
-- Name: alf_prop_class_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_prop_class_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_prop_class_seq OWNER TO alfresco;

--
-- Name: alf_prop_date_value; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_prop_date_value (
    date_value bigint NOT NULL,
    full_year integer NOT NULL,
    half_of_year smallint NOT NULL,
    quarter_of_year smallint NOT NULL,
    month_of_year smallint NOT NULL,
    week_of_year smallint NOT NULL,
    week_of_month smallint NOT NULL,
    day_of_year integer NOT NULL,
    day_of_month smallint NOT NULL,
    day_of_week smallint NOT NULL
);


ALTER TABLE public.alf_prop_date_value OWNER TO alfresco;

--
-- Name: alf_prop_double_value; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_prop_double_value (
    id bigint NOT NULL,
    double_value double precision NOT NULL
);


ALTER TABLE public.alf_prop_double_value OWNER TO alfresco;

--
-- Name: alf_prop_double_value_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_prop_double_value_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_prop_double_value_seq OWNER TO alfresco;

--
-- Name: alf_prop_link; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_prop_link (
    root_prop_id bigint NOT NULL,
    prop_index bigint NOT NULL,
    contained_in bigint NOT NULL,
    key_prop_id bigint NOT NULL,
    value_prop_id bigint NOT NULL
);


ALTER TABLE public.alf_prop_link OWNER TO alfresco;

--
-- Name: alf_prop_root; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_prop_root (
    id bigint NOT NULL,
    version integer NOT NULL
);


ALTER TABLE public.alf_prop_root OWNER TO alfresco;

--
-- Name: alf_prop_root_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_prop_root_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_prop_root_seq OWNER TO alfresco;

--
-- Name: alf_prop_serializable_value; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_prop_serializable_value (
    id bigint NOT NULL,
    serializable_value bytea NOT NULL
);


ALTER TABLE public.alf_prop_serializable_value OWNER TO alfresco;

--
-- Name: alf_prop_serializable_value_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_prop_serializable_value_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_prop_serializable_value_seq OWNER TO alfresco;

--
-- Name: alf_prop_string_value; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_prop_string_value (
    id bigint NOT NULL,
    string_value character varying(1024) NOT NULL,
    string_end_lower character varying(16) NOT NULL,
    string_crc bigint NOT NULL
);


ALTER TABLE public.alf_prop_string_value OWNER TO alfresco;

--
-- Name: alf_prop_string_value_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_prop_string_value_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_prop_string_value_seq OWNER TO alfresco;

--
-- Name: alf_prop_unique_ctx; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_prop_unique_ctx (
    id bigint NOT NULL,
    version integer NOT NULL,
    value1_prop_id bigint NOT NULL,
    value2_prop_id bigint NOT NULL,
    value3_prop_id bigint NOT NULL,
    prop1_id bigint
);


ALTER TABLE public.alf_prop_unique_ctx OWNER TO alfresco;

--
-- Name: alf_prop_unique_ctx_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_prop_unique_ctx_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_prop_unique_ctx_seq OWNER TO alfresco;

--
-- Name: alf_prop_value; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_prop_value (
    id bigint NOT NULL,
    actual_type_id bigint NOT NULL,
    persisted_type smallint NOT NULL,
    long_value bigint NOT NULL
);


ALTER TABLE public.alf_prop_value OWNER TO alfresco;

--
-- Name: alf_prop_value_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_prop_value_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_prop_value_seq OWNER TO alfresco;

--
-- Name: alf_qname; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_qname (
    id bigint NOT NULL,
    version bigint NOT NULL,
    ns_id bigint NOT NULL,
    local_name character varying(200) NOT NULL
);


ALTER TABLE public.alf_qname OWNER TO alfresco;

--
-- Name: alf_qname_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_qname_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_qname_seq OWNER TO alfresco;

--
-- Name: alf_store; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_store (
    id bigint NOT NULL,
    version bigint NOT NULL,
    protocol character varying(50) NOT NULL,
    identifier character varying(100) NOT NULL,
    root_node_id bigint
);


ALTER TABLE public.alf_store OWNER TO alfresco;

--
-- Name: alf_store_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_store_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_store_seq OWNER TO alfresco;

--
-- Name: alf_subscriptions; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_subscriptions (
    user_node_id bigint NOT NULL,
    node_id bigint NOT NULL
);


ALTER TABLE public.alf_subscriptions OWNER TO alfresco;

--
-- Name: alf_tenant; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_tenant (
    tenant_domain character varying(75) NOT NULL,
    version bigint NOT NULL,
    enabled boolean NOT NULL,
    tenant_name character varying(75),
    content_root character varying(255),
    db_url character varying(255)
);


ALTER TABLE public.alf_tenant OWNER TO alfresco;

--
-- Name: alf_transaction; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_transaction (
    id bigint NOT NULL,
    version bigint NOT NULL,
    change_txn_id character varying(56) NOT NULL,
    commit_time_ms bigint
);


ALTER TABLE public.alf_transaction OWNER TO alfresco;

--
-- Name: alf_transaction_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_transaction_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_transaction_seq OWNER TO alfresco;

--
-- Name: alf_usage_delta; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_usage_delta (
    id bigint NOT NULL,
    version bigint NOT NULL,
    node_id bigint NOT NULL,
    delta_size bigint NOT NULL
);


ALTER TABLE public.alf_usage_delta OWNER TO alfresco;

--
-- Name: alf_usage_delta_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_usage_delta_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alf_usage_delta_seq OWNER TO alfresco;

--
-- Name: act_evt_log log_nr_; Type: DEFAULT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_evt_log ALTER COLUMN log_nr_ SET DEFAULT nextval('public.act_evt_log_log_nr__seq'::regclass);


--
-- Data for Name: act_evt_log; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_evt_log (log_nr_, type_, proc_def_id_, proc_inst_id_, execution_id_, task_id_, time_stamp_, user_id_, data_, lock_owner_, lock_time_, is_processed_) FROM stdin;
\.


--
-- Data for Name: act_ge_bytearray; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_ge_bytearray (id_, rev_, name_, deployment_id_, bytes_, generated_) FROM stdin;
2	1	8842f71f-334d-4425-9968-f1c2b0fcf461bpmn20.xml	1	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0d0a0d0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220d0a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220d0a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220d0a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220d0a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f7267223e0d0a0d0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d2261637469766974694164686f6322206e616d653d224164686f632041637469766974692050726f63657373223e0d0a0d0a20202020202020203c73746172744576656e742069643d227374617274220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a7375626d69744164686f635461736b22202f3e0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773127200d0a202020202020202020202020736f757263655265663d277374617274270d0a2020202020202020202020207461726765745265663d276164686f635461736b27202f3e0d0a0d0a20202020202020203c757365725461736b2069643d226164686f635461736b22206e616d653d224164686f63205461736b220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a6164686f635461736b223e0d0a20202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a2020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a2020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b62706d5f61737369676e65652e70726f706572746965732e757365724e616d657d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773227200d0a2020202020202020736f757263655265663d276164686f635461736b270d0a2020202020202020202020207461726765745265663d277665726966795461736b446f6e6527202f3e0d0a0d0a20202020202020203c757365725461736b2069643d227665726966795461736b446f6e6522206e616d653d22566572696679204164686f63205461736b20436f6d706c657465642e220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a636f6d706c657465644164686f635461736b22203e0d0a2020202020202020202020203c646f63756d656e746174696f6e3e0d0a2020202020202020202020202020202056657269667920746865207461736b2077617320636f6d706c657465642e0d0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020202020200d0a2020202020202020202020202020202020202020202020206966202877665f6e6f746966794d65290d0a0909092020202020202020202020207b0d0a090909202020202020202020202020202020766172206d61696c203d20616374696f6e732e63726561746528226d61696c22293b0d0a0909092020202020202020202020202020206d61696c2e706172616d65746572732e746f203d20696e69746961746f722e70726f706572746965732e656d61696c3b0d0a0909092020202020202020202020202020206d61696c2e706172616d65746572732e7375626a656374203d20224164686f63205461736b2022202b2062706d5f776f726b666c6f774465736372697074696f6e3b0d0a0909092020202020202020202020202020206d61696c2e706172616d65746572732e66726f6d203d2062706d5f61737369676e65652e70726f706572746965732e656d61696c3b0d0a0909092020202020202020202020202020206d61696c2e706172616d65746572732e74657874203d20224974277320646f6e65223b0d0a0909092020202020202020202020202020206d61696c2e657865637574652862706d5f7061636b616765293b0d0a0909092020202020202020202020207d0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77332720736f757263655265663d277665726966795461736b446f6e65270d0a2020202020202020202020207461726765745265663d27746865456e6427202f3e0d0a0d0a20202020202020203c656e644576656e742069643d22746865456e6422202f3e0d0a0d0a2020203c2f70726f636573733e0d0a0d0a2020203c212d2d2047726170686963616c20726570726573656e7461696f6e206f66206469616772616d202d2d3e0d0a2020203c62706d6e64693a42504d4e4469616772616d2069643d2242504d4e4469616772616d5f61637469766974694164686f63223e0d0a2020202020203c62706d6e64693a42504d4e506c616e652062706d6e456c656d656e743d2261637469766974694164686f63222069643d2242504d4e506c616e655f61637469766974694164686f63223e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227374617274220d0a20202020202020202020202069643d2242504d4e53686170655f7374617274223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d2233302220793d22323030223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d226164686f635461736b220d0a20202020202020202020202069643d2242504d4e53686170655f6164686f635461736b223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22313330220d0a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227665726966795461736b446f6e65220d0a20202020202020202020202069643d2242504d4e53686170655f7665726966795461736b446f6e65223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22323930220d0a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22746865456e64220d0a20202020202020202020202069643d2242504d4e53686170655f746865456e64223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d223435352220793d22323030223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7731222069643d2242504d4e456467655f666c6f7731223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d2236352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223133302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7732222069643d2242504d4e456467655f666c6f7732223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223233352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223239302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7733222069643d2242504d4e456467655f666c6f7733223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223435352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020203c2f62706d6e64693a42504d4e506c616e653e0d0a2020203c2f62706d6e64693a42504d4e4469616772616d3e0d0a0d0a3c2f646566696e6974696f6e733e	f
3	1	8842f71f-334d-4425-9968-f1c2b0fcf461activitiAdhoc.png	1	\\x89504e470d0a1a0a0000000d49484452000001f4000000ff0806000000076624fd0000144949444154785eeddd79b054d5990070b7e84c8cd154b462d4c454529549a25395542c3331498d9599d2228af90b1ee08622485c52710b2e65505414c84c8d5bf48fa4269ad2b8e244270611d1c1058d611079c42886e8a020820ba22208dc39a77df7557bba1fbce7db6e9ff7fb557dd5ef2eaffb74f7d7dfd7f7f6eddbdb6d070000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000545c51143b2f5bb6ecb6f9f3e76f7ce081078afbefbf5f5420c273b165debc792be6ce9dfbc3f439cb89fcab660c95fc83ac84627a7b78e116ab56ad2ad6af5f5f6cd8b0415420e273119f93871e7a686d28b047a4cf5b2ee45f3563a8e41f64256e19c5176efa8216d588952b57ae0905f58fe9f3960bf957edc83dff202b7137a72da3ea467c6e42417d2f7dde7221ffaa1db9e71f64257e5e96be8845b5223e47e9f3960bf957fdc839ff202bdd2da86fbfb9b258f6e47f164be64ca945fc3bce4bd7137d1f391754f957fdc839ff202bdd29a8ebde78b968bfefc262d1efcff950c4797159babee8dbc8b9a0cabfea47cef90759e94e415dbee4ee86625ac64b4bee69585ff46de45c50e55ff523e7fc83ac74a7a03ef3e0b486425a465c96ae2ffa36722ea8f2affa9173fe4156ba5350dbefbfa8a190961197a5eb8bbe8d9c0baafcab7ee49c7fbd356cd8b05ddadada0e1b3d7af47f84cb59a3468dfadf102f8528e26598b7305cde172eaf0a97470c1f3efce3e9750c1923468cd8f1baebae3bf1e73ffff9c3175c70c1ab679e79e63b13274edc141fac7879c61967bc13e74f9b36edd12bafbc7242587fe7f43ad83a05b5fa917341957fd58f9cf3efa30a0dfcdba149df117ad1ba8ee6dddd58dfd1e087ce19f866cc9871e025975cb260dcb8719bcf3ffffce28e3bee28162d5a54bcf0c20bc56bafbd5644afbffe7a6dfae9a79f2e66ce9c594c9e3cb9183b766c5c7fc979e79df7cfe975d25c770a6a3caa382da465c465e9faa26f23e7822affaa1f39e75f4f85467e4068c8ffd5a4517f94885bf347a5b7918dd094f7993265ca43279d74d2e6db6fbfbdd6b47be2edb7df2e66cf9e5d9c72ca299bc3d67c7bd88a3f38bd0d3eac3b0575e9a3d73414d232e2b2747dd1b7917341957fd58f9cf3af2742f33d3f446d0f717d845e53dc7cf3cdc5534f3dd574a3336e8cde7aebad45d8d04c1b7a19b3478c18b1577a7b2d6dead4a9474e983061c30d37dc50bcf5d65b49abee99f7df7fbfd6d8c3f56dfcd18f7e34fda28b2eda21bd3d3ed09d82fadacb4f17edb3273714d3382f2e4bd7177d1b391754f957fdc839ffba2334dbbf0f4df7e6fa263c66cc98e217bff8453c356eda7eb6ea8d37dea835f7134e38216deacb478e1cf99df4b65bd225975c72d1f8f1e3372f5cb830bdffbd121fbcb0d5bf216cf13f367af4e83dd3dba57b0535c6dffe746343418df3d2f544df47ce0555fe553f72cebf6de968e6f3eb9b6fe857c5f2e5cbd376d323ebd6ad2b6ebae9a6da1b83baebded8d6d6765c3a869672e9a5975e7cfae9a76f79f9e597d3fbdc27366fde1c1fb8f78f3ffef865e1c1fb547afb435db70aea7bef154b1fbbaea1a0c6797159c3faa24f23e7822affaa1f39e7dfb6a45be6710f72ec297da5bdbdbd38f9e493eb9bfaa6d0d447a5e368091dbbd937f75733af179e8875c71e7bece363c78efdbb741c43d9b60a6a3c13d7738f5edd504ccb88cb9cadab7f23e7822affaa1f39e7dfd68c1e3dfabcfa667edf7df7a56da54fc4cfda274d9a54dfd4df0d71503a9e4abbe0820bf68d9f99f7f56ef6ae6cd9b2a5983163c69ab0957e5b3a96a1accb821ab67c562e9d5b2c9e757e43114d23ae13d7b5b5d43f917341957fd58f9cf3af2b1d47b3771e0017b7ccfb536ceaa79d765a7d537fbea5beb31e8f66efef0729150f961b3f7efc2b6d6d6d47a6e319aa9a15d46d6d157515b696fa27722ea8f2affa9173fe756554dd57d3e267e67db99bbd2b2fbdf45271e28927d637f519e9b82a297ecf3c7e356deddab5e97dea770b172e7c33bcfb7ade49683ed0aca07667aba8ab88ff9b5e9fe85de45c50e55ff523e7fc6b266cf0fd5368a65b62538d07adf5f600b89e98356b567d435f7ff4d147ef978eaf722ebef8e205f17be68365d2a4497f094dfdac745c4351b3829a16c99e467a7da277917341957fd58f9cf3af998e33c0d59a6afc6ada40dab4695371ce39e77436f5d0a7fe2d1d5fa5c4d3b98e1b376e534f4f1ad397dadbdb578607ebaf6138dba7e31b6a9a155451adc8b9a0cabfea47cef9978ae7660f4df4adb2a1ae58b1226d1ffd6ec18205f55be9ab2bbd37399e9b3d9ece7530c503e48e39e69855e1c13a241ddf50a3a0563f722ea8f2affa9173fea5c68c197378d94ccf3efbecb4750c88b8957ed249277536f5912347fe6b3aceca983e7dfa2383b9bbbd3475ead4c7c38375513abe5c84fbf6608843d3f92905b5fad18a0555fee513ad987fcd742727c3f22bcb461a4ffc3258aebffeface86ded6d6362d1d67655c78e185ab06eaab6a5b1392f4c9f040fd3e1d5f2eca64d856122ba8d58f562ca8f22f9f68c5fc6ba63b3919e6df5bae37987d6afefcf99d0d3dc47de9382b23fe046a3c61fd606b6f6ffff3a80f3e47cf525d326c358915d4ea472b1654f9974fb462fe35d39d9c1cf5c12fa0d5960f669f5aba7469fd38ff563fc64a993061c2a6788ef5c1b67af5ea57c203b5361d5f2e9a246fd3245650ab1fad58509be49dfc6bd168c5fc6ba6492e36e464b85c51ce1fcc3e157fb1ad6e7cd5ed53471f7d74ed042f836de3c68def85076a633abe5c3449da346a49aca0563f5ab1a036c9b734e45f8b442be65f334d72308d07dbdadade2fa707b34f85fe543faeeaf6a9e38f3f7ecb60bef329756ca1a74fe8908bc128a8210d6ad1d574ab467fdd8ff439cb290623ff7a12e9737ae79d77165ffce2178b1d76d8a15f9eeb34d2db1f8c489fb3a11283d9a7922df4973ada67f54c9c3871c3607e36515ab26449fc0cfd89747cb94893b32e1e1cd50fbb3ce3697cb7eb283ebff9cd6f1a96d747b95e57d3fd15e5ed7415e9fa3d8dbeba9e345a710ba949def56bfe1d70c001b5c77ef6ecd99df366cd9a559b77e0810736acdfdd88ff1fa39cfedce73e579b8e074ca5eb76377af35a198c68c5fc6ba6492e36e464b85c5ece1fcc3ef5fcf3cfd78fafba7deadc73cf5df3d4534fa5e31f7073e6ccf96378a066a6e3cbc5d692b65e5f15d461c386153bedb4532d7ef0831f342caf8fed9222954e0f44f4c76df6c775c668c5823ad0f917cfb71daeaef8f18f7fdc39eff4d34fafcdbbecb2cb1ad6ffa811af2f463abf27d19bd7ca60442be65f33ddc9c9d1a347ff4fb97c308f727fe28927eac759dd3e15bfff3d73e6cc74fc036edab4698f86076a723abe5c6c2d69ebf545418d6753fad8c73e561c7ef8e1c561871d56fb3bce2b973ff7dc73c5c1071f5c6cbffdf69d052a46b9bc7e5e8ccf7ce6331fda7279e59557e289808a3df7dcb316f1388c38af5c1e774f9d7aeaa9c5befbeedb791be918d348c7d06cd96ebbed561c72c821c5238f3cd2b9ecb6db6e2bbefce52fd7ee637a1df5d34f3ef964f1d9cf7eb656b4afbcf2ca86dbe849b462411dc8fc8b118f0a8ecffd97bef4a5ce79f1ef382f6eedc4e9356bd6c41f67aae5d7eebbef5e0c1f3ebc58b66c59e7fadb2579583fafd9f218dffad6b76a9771ab3bae73c71d77d4a6bffbddef368c31466f5f2b8311ad987fcd742727c3fcebcaf56ebdf5d6b46d0c985ffdea579d0d3dbcc9b83c1d676584867ef6e4c993d3f10fb8d0209687c6f08fe9f872b1b5a4add71705f5aaabaeea2c6abffef5af6b7f5f7df5d59dcb63e18cf362932e97c7289797d3d75c734dedfb97f1efbdf7debb73796ce6e9ffc779e5f271e3c6d5e6fdf4a73f2dde7aebad86f1358b740ccd62f1e2c5b575bef6b5af75cedb6bafbd6a45f8de7bef6d58bfbcceb8abf7939ffc64b1c71e7b147ff8c31f1ad6eb69b462411dc8fc2b23bef90a57593cfdf4d3c5a2458b6a7fd737d6134e38a136efaebbee2a1e7becb1dadfdffffef73b97c7e918f1fcddef75fc146b392f5da79c8e271f89d3471e79e4876ee39e7bee69185f8cdebe5606235a31ff9ae94e4eb6b5b51d5d36d2f3ce3b2f6d1b03229ec974e2c489f50dfddbe9382b63c48811bb1f77dc715bd6ad5b97de8f0113deb1bf1c1ea8e7d2b10d457d51506321fdc4273e513b882446fcbbbe90c6c6166eaa78f3cd376bcbe3df31cae5e5745cbe7efdfa86e59ffef4a71bfe3f6ea997cb63938df356ad5ad530b6ae22bd8d326233fee637bf59ecbaebae9debc403a0cae5fbecb34f6ddefefbef5f9c71c6194db7f062c38fe36b6f6f6fb8fe8f12b914d466fa22ffca289be515575c515c7ef9e5b5bfeb9b657c4ee2bcfad865975d3a9797f3e2af40a6f3ba9a7ef7dd778bcf7ffef3c5ce3bef5cdb6b14b7febff18d6f348cad8cdebe56062372cebf54e84f7b8feaf81df4d04807e5c0b8679e79a6b3998ffae080b86affe6c8b9e79efb4c3c7865b05c7ae9a54f8607ea92745c43516f0b6ab9ab73bba450d6efeadc5691dad6f44036f4b8db3ece7ff4d1476b853d5d2f7eb675d4514775ee72fffad7bfde709db1c0c7cba953a7365cff47899c0b6a6ff3af3ecaddd9dffbdef76a4d32dd9d5de649fcdde9f47f63c46531b6362f9d8e317dfaf4dabc934f3eb97679cb2db7345c778cbe78ad0c46e49c7fcd84463eb76ca883b1db3dd68dba867e4d3abecaf9c94f7e72c4a9a79eba7930bee7b77cf9f2d7c383f4ea9831633e958e6b28ea6d412d0f468ac957ce1b3972646d5e793052dc1d19a7bbda8db8ade9eeee720f6f147bbdcbbd2cfa0f3ffc70edb3efaed68b9fabc7f9f173f2f43a63e18e5bf0f1ef2953a634fc6f4f23e782dadbfc4b231e70b6e38e3bd6223de0accc93507f3eb4155e465c16636bf3d2e918ab57afae1d6f111bf357bef295da5ea6f4ba630cc46b259dee8bc839ff9a696b6b1b5536d4f811ca40ee4d8e1f15d535f38d23468cf887747c95141afa5f07632bfdacb3ce5a1c1ea833d2f10c55bd2da8e5d785eebefbeece79bffbddef6af3caaf0b3dfbecb3c541071dd4b07552aebfade9f2a0b8b8a51ea3ab83e2e2eef0de1e14f7dbdffeb6f6f97dfd389b8d2d6efd7df5ab5ffdd0fdae5f376e717de10b5fa84dffec673f6bb89d9e44ce05b5b7f997c68d37ded8f93cc4bfeb97c53c39edb4d38afdf6dbaff37be431cae5e974b379e97419e5d6793c98295d56c640bc56d2e9be889cf3af99430f3d74a7d023fe5636d681fa9196783299fadf420f6f2cae4dc7565913274efcce840913360ee4671473e6ccf96b78a09656faf76507585f1754d1f7917341cd21ffe21b85f8d14b3caafe9d77de6958deea9173fe7565745036d63163c6d48e87e96fd75e7b6dfdd6f9dad0a7f64ac75569e3c78fbf6ef2e4c91b366fde9cdeb73e17def9ae0ecfd1eaf0aee7c0741c43590e0535f7c8b9a0b67afec5061eee46edd88b79f3e6352ccf2172cebfadd87ed40747c5d71a6cdc03f3faebafa76da5cfc43d3575cd3c1e9077723aa0ca1b366cd82ee3c68d5b74d34d37f5eb87e9ab57af5e7fcc31c7bc129af991e91886ba562fa84321722ea8f2affa9173fe6d4dd832df3ff48c37ca261bbf16db1f4d3d36f378447d4bee6a4fc5dd0ac71d77dc8b37dc70c3dbf1fb777d2d6e998766be2a3c60a7a4b78d82da0a917341957fd58f9cf36f5b42733d6c54c7d7d862c46330e2b724fa42fccc3cd9cd1e634efc0c3f1d474b89dffd0b4df7d9193366acd9b469537abf3fb2f89979c76e765be65d5050ab1f391754f957fdc839ffba23f490e3eb9bfa89279e583b5f456f7a553c9abdfe00b8b2998f1d3b768ff4f65bd251471db5db983163ee1a3f7efc2b0b172e7c337d007a227e35ade368f6a53e33df3a05b5fa917341957fd58f9cf3afbb3abecaf66e7d038e0d79c18205dd6eec710f743c694cf23df35ac4ddec2dbf65de4c7837744c885593264dfacbe2c58b57a60fcad6c433c05d76d9657f0a0fd0abe1013ad3d1ecdba6a0563f722ea8f2affa9173fef544e82b0785783e6dc6f1fc06d75f7f7df1f8e38fd7cec51f7f33208a97f19c14f16454bffce52f3f743ad7ba58db9207c0f5c4b1c71ebb6b68c8e7843bbb2c7efe1d7fd02524d593a1c1fff9d5575f5d111fac78d9dedebe24fe6adaf4e9d31f89e7660feb2f0d31c54963ba4f41ad7ee45c50e55ff523e7fceba9e1c3877f3c34e0e9a1cfbcd7a439f72436c6adf296fb6a5a6f8d1c39f23be1ce5f14e2bf477dd0b0dfee7840fe2fc4fc107786989cf30fadf42705b5fa917341957fd58f9cf3efa30afd66bfd077fe3dc4ea26cd7a6bf15247236f8d33c0d15a14d4ea47ce0555fe553f72cebfde8a5fbd0e4dfa5f42939e162eef0db16cd487373a1f0f3133c415210e49ff1ffa94825afdc8b9a0cabfea47cef907595150ab1f391754f957fdc839ff202b0a6af523e7822affaa1f39e71f644541ad7ee45c50e55ff523e7fc83ac28a8d58f9c0baafcab7ee49c7f9095071e7860cbfaf5eb1b5ec4a21a119e9b15a1a0be973e6fb9907fd58edcf30fb2326fdebc15ab56ad6a78218b6ac48b2fbe784b28a87f4c9fb75cc8bf6a47eef90759993b77ee0f1f7ae8a1b52b57ae5c634ba93a119e8b952fbcf0c2cda198fe5f8823d2e72d17f2af9a3154f20fb2135fb0f15d78880df1f332518988cf457c4eb22fa6f13e76dc57f9579d1832f907000000000000000000000000000000000095f3ff88208ee01f35ceac0000000049454e44ae426082	t
6	1	e05cec22-8df0-41b4-bc62-e30913e29af2bpmn20.xml	5	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0d0a0d0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220d0a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220d0a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220d0a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220d0a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f7267223e0d0a0d0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d22616374697669746952657669657722206e616d653d2252657669657720416e6420417070726f76652041637469766974692050726f63657373223e0d0a0d0a20202020202020203c73746172744576656e742069643d227374617274220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a7375626d69745265766965775461736b22202f3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773127200d0a202020202020202020202020736f757263655265663d277374617274270d0a2020202020202020202020207461726765745265663d277265766965775461736b27202f3e0d0a0d0a20202020202020203c757365725461736b2069643d227265766965775461736b22206e616d653d22526576696577205461736b220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a61637469766974695265766965775461736b223e0d0a20202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f7265766965774f7574636f6d65272c207461736b2e6765745661726961626c65282777665f7265766965774f7574636f6d652729293b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b62706d5f61737369676e65652e70726f706572746965732e757365724e616d657d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773227200d0a2020202020202020736f757263655265663d277265766965775461736b270d0a2020202020202020202020207461726765745265663d277265766965774465636973696f6e27202f3e0d0a0d0a20202020202020203c6578636c757369766547617465776179202069643d227265766965774465636973696f6e22206e616d653d22526576696577204465636973696f6e22202f3e200d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77332720736f757263655265663d277265766965774465636973696f6e27207461726765745265663d27617070726f76656427203e0d0a2020202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b77665f7265766965774f7574636f6d65203d3d2027417070726f7665277d3c2f636f6e646974696f6e45787072657373696f6e3e0d0a20202020202020203c2f73657175656e6365466c6f773e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773427200d0a2020202020202020736f757263655265663d277265766965774465636973696f6e270d0a2020202020202020202020207461726765745265663d2772656a656374656427202f3e0d0a0d0a202020202020203c757365725461736b2069643d22617070726f76656422206e616d653d22446f63756d656e7420417070726f766564220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a617070726f7665645461736b22203e0d0a2020202020202020202020203c646f63756d656e746174696f6e3e0d0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e6420617070726f7665642e0d0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a20202020202020200d0a20202020202020203c757365725461736b2069643d2272656a656374656422206e616d653d22446f63756d656e742052656a6563746564220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a72656a65637465645461736b22203e0d0a2020202020202020202020203c646f63756d656e746174696f6e3e0d0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e642072656a65637465642e0d0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77352720736f757263655265663d27617070726f766564270d0a2020202020202020202020207461726765745265663d27656e6427202f3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77362720736f757263655265663d2772656a6563746564270d0a2020202020202020202020207461726765745265663d27656e6427202f3e0d0a0d0a20202020202020203c656e644576656e742069643d22656e6422202f3e0d0a0d0a202020203c2f70726f636573733e0d0a202020200d0a2020203c212d2d2047726170686963616c20726570726573656e7461696f6e206f66206469616772616d202d2d3e0d0a2020203c62706d6e64693a42504d4e4469616772616d2069643d2242504d4e4469616772616d5f6163746976697469526576696577223e0d0a2020202020203c62706d6e64693a42504d4e506c616e652062706d6e456c656d656e743d226163746976697469526576696577220d0a20202020202020202069643d2242504d4e506c616e655f6163746976697469526576696577223e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227374617274220d0a20202020202020202020202069643d2242504d4e53686170655f7374617274223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d2233302220793d22323030223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965775461736b220d0a20202020202020202020202069643d2242504d4e53686170655f7265766965775461736b223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22313235220d0a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965774465636973696f6e220d0a20202020202020202020202069643d2242504d4e53686170655f7265766965774465636973696f6e223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223430222077696474683d2234302220783d223239302220793d22313937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22617070726f766564220d0a20202020202020202020202069643d2242504d4e53686170655f617070726f766564223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220d0a202020202020202020202020202020793d223937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d2272656a6563746564220d0a20202020202020202020202069643d2242504d4e53686170655f72656a6563746564223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220d0a202020202020202020202020202020793d22323937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22656e64222069643d2242504d4e53686170655f656e64223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d223535352220793d22333037223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7731222069643d2242504d4e456467655f666c6f7731223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d2236352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223132352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7732222069643d2242504d4e456467655f666c6f7732223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223233302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223239302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7733222069643d2242504d4e456467655f666c6f7733223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313937223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7734222069643d2242504d4e456467655f666c6f7734223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22323337223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7735222069643d2242504d4e456467655f666c6f7735223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22333037223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7736222069643d2242504d4e456467655f666c6f7736223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223535352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020203c2f62706d6e64693a42504d4e506c616e653e0d0a2020203c2f62706d6e64693a42504d4e4469616772616d3e0d0a0d0a3c2f646566696e6974696f6e733e	f
7	1	e05cec22-8df0-41b4-bc62-e30913e29af2activitiReview.png	5	\\x89504e470d0a1a0a0000000d49484452000002580000016a0806000000c63c24e70000223749444154785eeddd7b901c65dd2ff07055f1fa965c14102dadf2c67baa44df3ae5ab65113da88508feb56c3689dcc2250a5649245c4bc34551c073ea7094cb1f5a1a2d50a32fde8e9a401272a21008f286c8465e09866042364b124212649390a44f3fe3f63a79667677669fd96cefcee753f5addde9eeed69b637bff9d2333b3b691200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0789765d9e16bd6ac99b76cd9b2dd8b162dcaeebbef3e2941f273b16fe9d2a51b162f5efc99f89c0100259797ab9fe60fe4596f6f6fd6d7d797eddab54b4a90702ec23959b264c9b6bc709d169f3700a0c4c295abf0401e3fc04b39d2d3d3b3392f58cbe3f306009458785ad095abf2269c9bbc60ed8ccf1b005062e1f53ef183ba942be11cc5e70d0028b1460bd68b2ff4646b1ef95eb66ae1f59584cfc3b2783b697d142c0018671a29583bb63e9b752ff872b6f237b3f74b5816d6c5db4b6ba36001c038d348c15ab7ea5735e5aac8fa55bfaed95e5a1b050b00c699460ad613f7df5453ac8a8475f1f6d2da28580030ce3452b0baefbbb6a6581509ebe2eda5b551b000609c51b0ca1f050b00c699460a56f8adc1b8581509ebe2eda5b551b000609c69a460ad7ee0db35c5aa4858176f2fad8d820500e34c23056bcbb37fcabaef9d5353aec2b2b02ede5e5a1b050b00c699460a56c8d37ffc414dc10acbe2eda4f551b000609c69a860eddc99ad7ef08e9a821596857535db4b4ba36001c038335cc10aefd4fee403dfaa295745c23aefe63eba51b000609c19b460eddc99f5ac5e9c3d3effea9a5215276c13b675356b74a26001c03853af600d77d56ab0b89a353a51b000609ca957b01ab96a3558c2d7c6fb93b428580030ced42b5871696a36f1fe242d0a16008c33f50a96942b0a160094c4942953eecf33395e1e53b0ca1f050b004a222f57597f862c5a0a56f9a3600140495415ac218b968255fe2858005012750a56dda2a560953f0a160094449d6215a752b414acf247c102e080ab531ca4892858e54f7ccea4b1c4b302802618a4f5c50f3655b97f8aa708c7555cc16a9eb90090c820ad6fb862553890052bbfbb4a0e3becb0ecc8238fcc4e39e51457d01a8882d53c73012091415adf70c5aa70200bcea4fe82d5d7d7972d5fbe3c3bf1c413b3430e39245bb06041cdb6f2cf2858cd3317001219a4f50d57ac0a6351b08adbf3e7cfafdc9e3c79f2c0b28d1b3766d3a74faf5ce10a99366d5a6559b17ecb962dd9c5175f9c1d77dc71d941071db4dffee2fd0f76bbc811471c919d74d249d909279c901d7ef8e1d9f1c71f9fcd9b37afe6b8c73a0a56f3cc05804406699ab12c589b366daadc7ee31bdf38b02c94abb0ec873ffc61f6fdef7fbff2795856ac9f31634665d9e5975f9e6ddfbebd66ff071f7cf0a0f757dcbeedb6db2a57d0aa6f3ffae8a395cf8f3df6d8fdf659862858cd3317001219a469c6b2603df7dc7395dbe14a55b12c94adb0ec85175ec8b66edd5ab3fea8a38eaa2cebededadbbfff094e360f757dc0efb7ee9a597066e87fba9b77d59a26035cf5c00486490a619cb8255ef29c2d482555cc10a5f1fdf5fb3b7cb1205ab79e60240228334cd5814aca15ee4dee85384575e7965cd5384c71c734c65dd430f3d94dd7efbed3585a999db43adab777b34a36035cf5c00486490a6198b8275e8a18756ae547df4a31fad799b86e245ee617dc8602f720faf958a5fe43e77eedcece8a38f1eb89f22f1fd37727ba875f56e8f6614ace6990b00890cd23471c191f245c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e602402283348d8255fe2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e60240228334cda2458bf6f5f5f5d53ca84b39929f9b0d79c1da199f3786662e00243248d32c5dba74436f6f6fcd03bb9423cf3cf3cc8ff382b53c3e6f0ccd5c00486490a659bc78f167962c59b2ada7a767b32b59e5497e2e7ad6ae5d7b775eaefe96e7b4f8bc313473012091419a2e3c8087ab24797685d7fb482912ce453827cad508980b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e30eab22c3b7ccd9a35f3962d5bb67bd1a245d97df7dd2725487e2ef62d5dba74c3e2c58b3f139f339a639002317381519797ab9fe60fe4596f6f6fd6d7d797eddab54b4a90702ec23959b264c9b6bc709d169f371a6790023173815117ae5c8507f2f8015eca919e9e9ecd79c15a1e9f371a67900231738151179e1674e5aabc09e7262f583be3f346e30c5220662e30eac2eb7de207752957c2398acf1b8d33488198b9c0a86bb460bdf8424fb6e691ef65ab165e5f49f83c2c8bb793d647c14a63900231738151d748c1dab1f5d9ac7bc197b395bf99bd5fc2b2b02ede5e5a1b052b8d410ac4cc05465d23056bddaa5fd594ab22eb57fdba667b696d14ac2427f70fd293e31540fb52b018758d14ac27eebfa9a6581509ebe2eda5b551b0462c94aade3c97f57f54b2800a058b51d748c1eabeefda9a625524ac8bb797d646c11a91a25c15a52abe0db43105ab8e8e8e8e43eeb8e38ef3bef9cd6ffefe9a6bae796ed6ac597f9f3973e69ef0cd0a1f2fbdf4d2bf87e537dd74d303b7de7aeb85f9f687c7fbe09f14acf247c16ada60656ab0e5409b51b0aadc72cb2dff7ac30d373c3a63c68cbd575f7d75f6b39ffd2c5bb97265b676edda6ccb962d59f0fcf3cf576effe94f7fcaeeb9e79e6cce9c39d939e79c13b65f75d5555719aa753452b0c26f0dc6c5aa4858176f2fad8d82d594e14ad470eb8136a060e5f29274ecf5d75fbfe4fcf3cfdffbd39ffeb452a29af1e28b2f66f7de7b6ff6f9cf7f7eefac59b3ba67ce9cf9dfe3fb68678d14acd50f7cbba6581509ebe2eda5b551b01ad668796a743b60826afb8275e38d377efac20b2fdc3577eedc6cfbf6ed71776acacb2fbf5c295af9fe767fee739fbbf9da6baf3d38bebf76d448c1daf2ec9fb2ee7be7d494abb02cac8bb797d646c16a48b3a5a9d9ed8109a4ad0bd60d37dc70ed05175cb077c58a1571574ab275ebd6f0d4e1aef3cf3fffc1aeaeae23e3fb6d378d14ac90a7fff8839a821596c5db49eba3600d6ba46569a45f078c736d5bb0befad5af5ef7852f7c61dfb3cf3e1bf7a396d8bb776f76d75d77bd7cf6d967af993a75eabfc4f7df4e1a2a583b7766ab1fbca3a6608565615dcdf6d2d22858434a2d49a95f0f8c436d59b0fa9f16dc3b5ae5aadadcb973777cf6b39f7de89c73ce79657c1ced62b88215dea9fdc907be5553ae8a8475decd7d74a3600daa55e5a855fb01c689b62b58d75c73cd71e13557ad7e5a7030fbf6edcb6eb9e596cd53a74e9d171f4bbb18b460eddc99f5ac5e9c3d3effea9a5215276c13b675356b74a260d5d5ea52d4eafd0125d676052bfcb6607841fb81145efc7ec105176cecececfc747c3ceda05ec11aeeaad56071356b74a260d588cbd04179ceeeffd88cf8ebe2fd0213545b15acf03e57e1ad18b66ddb1677a051b762c58a17bababa9e6ac73725ad57b01ab96a3558c2d7c6fb93b42858fb894b502847dfcd13be47e163a3256bb0af8bf70f4c406d55b0aebbeeba47c3fb5c8d952baeb8e2bff292f5a5f8b826ba7a052b2e4dcd26de9fa445c11a50affc842b50e1fb53a49192555dae8a9c55b5bedefd001348db14acf0e76f66cc98b1a7d937116da5eeeeee9efc1bfed749c30fe709a55ec1927245c1aa18acf4d42b4b4395ac7adb7fa77f79b5c1ee0f9800daa66085bf2d18fefccd580a2f789f3e7d7a6ffe4dff507c7c13998255fe4c948295ffdbba3fcfe4787903862b3bf54a53bd92556fbb7ae5aa30dcfd02e354db14ac9b6fbef90f63f9f460e1c61b6f7c28ffa65f1b1fdf78d4e883998255fe4ca08295f5a7a19fcd7e8d969c7ae5a9ba64d55b3f54b92a347affc038d23605ebcb5ffe72ef817a6b86a1e40f648f747676fe263ebef1a8d1073305abfc998005aba19fcd49cd979b7a252adc0e7f162b5ede48b92a347b1c40c9b54dc19a356bd6dfd7ae5d1bf79d03aebbbbfbcf53fef13aac71afd1073305abfc99c0056ba89fcd91969a7a25eb2fd1ed66ca5561a4c7039450db14ac0b2fbc704ff81b81636dd3a64d1bf36ffab6f8f8c6a33a0f62751fcc14acf2a70d0a56fcb3995a66ea95ac947255483d2ea024c2cc89974d48d3a64dabbce1e758dbbd7bf7cefc9bbe3b3ebef1a8ce83579cca83998255fed4397713361d1d1dd92b5ff9ca30f82e8b7fa69b149e168caf5c85db61798a705ca1641d11af00c68f306fe26513d2d9679fbdaf4457b06a86fe444e190bd6a4fe07c478f9784babfe3bdae00ad6fd53f67f8a30f54ad15057b0eafd7661a3528f0b2889307be26513d2cc99337795e13558ab56ad0aafc17a383ebef1a8ce8358dd07b39116ac49550f5aaf79cd6bb20f7ef083d91ffef0879aed469262bff1f256a7fabfa15ee2ed9b4dabf633810b565cacaa9d3c696465a65eb98aaf648da4648df47880126a9b8275e595576e7eecb1c7e2be73c02d5cb87079fe4dbf273ebef1a8d107b3d482153effd5af7e55f9fc3def794fcd76e325d5ff3dad4aabf639010b56dd9fc53a9a2d35f5ca5578cd55bddf226ca664357b1c40c9b54dc10aef3f75cf3df7c47de780bbe9a69b1ec8bfe973e2e31b8f1a7d306b45c1dab16347e5f357bdea5503eb376fde1cfe887676cc31c764af7ffdebb3d34f3f3d5bb3664dd6d3d3931d71c411d93bdef18e6ce7ce9d956dc3c7b7bffdedd9ab5ffdea6ce3c68dfbed7ba87d15eb8f3efae8eca8a38eaa7cfef5af7fbdf2b5dff8c6372ab7c3f2f075f1f1c789efb3debad7bef6b5d9873ef4a1fdaed4cd9b372f7be73bdf991d76d86135fba8befdc8238f646f7ef39bb3430f3d34bbf5d65b6bee63a84ca08235e4cfe2204e9ed458b919ac5c0df53e588d94ac46ef1f1847daa9605d3667ce9cb8ef1c70d3a74f5f376ddab4ff161fdf78d4e883592b0a567105eb539ffad4c0fa73cf3db7b2ece73fff79f6e0830f563effd8c73e565977d14517556efff297bfacdcfec52f7e51b93d73e6cc9a7d0fb7af904f7ce2139565ebd7afcfdefdee77573e7fd7bbde95ad5bb7aef2f9273ff9c99ae38f13df67bd3cfef8e3956ddefbdef70e2c0b052e94abdffef6b735db17fb9c3f7f7ef6bad7bd2e7bc31bde90fdee77bfabd96eb84c948295e0e44943979c7ae5a9de6f0bd6db6ea89235dcfd02e354db14ac8e8e8ed79f75d659fbc29590b1f2d4534f3d9b7fc39f8c8f6da24b2d5845def296b7644f3ffdf4c0fa238f3cb2669b57bce2159575ab56adca0e3ef8e04a310ab73ffef18f576efff9cf7fde6fdf8dec2b64f6ecd99565d75d775de563676767e5e357bef295cac72baeb8a2e6f8e3c4f7592494a30f7ce00395ab6bc536e1588bf5c71e7b6c65d95bdffad6ecd24b2fddefca5ab17d2860e1bfa1bbbbbb66ff8d44c1aa387952fdb253af34d52b57857adbd72b5983dd1f3001b44dc10aaebcf2ca27eebdf7deb8f71c305ffdea571fc9bfe137c4c735d1a516aceddbb7675ff8c2172a9f575f550a5776c2b2705529feda90d34e3b2d3be8a0832a57afc2c7f0b45fbcef46f775d75d7755d687a7f08e3beeb86cdbb66d958f4529bafbeebb6bbe264e7c9f45c27ec2f2071e78a0b2df78bb871f7e383be38c33069e227cdffbde57b3cf134e38a1f2f1c61b6facd97f2351b0069c3ca9b6f49c3da9fffbdc9fa1ca55a15ec93aab6a7dbdfb012690b62a585ffce2174fbbf8e28bf78ec5fb61ad5bb7eef9fc9bfddcd4a953ff253eae892eb56085cfc35b6c1457998aa7ca66cc9851b99d9fd74a3189bfbebf34547e03317c5cb87061dd7d37b2af7045acf89aebafbfbeb2acb89a15525c191b2af17d1629caddef7ffffbca6ba706db2ebc2e2b2c0fafb38af7b97af5eaca15aeeae36b260ad67e4e9eb47ff9a92e4b8d94abc2605f17ef1f9880daaa600579c1faeb585cc5fad297bef478fecdbe343e9e76d08a8215128a43b8fd918f7ca4727bcb962dd925975c921d7ffcf195a7d4e2ed43defffef75796858f43ed7bb87d8517c987ab57e169c3e22a57f8186e87e5c58be9874abccf223ffad18fb237bde94d03ebe3ed8adbe10a56f82dcaf07ab4785df8fca9a79ecadef6b6b7556e87a72ee3fb192a0a568d9327d596ac7005aad1725588bf2ede2f3041b55dc19a3973e6872fbcf0c2dd07f24d47172e5cf8d7fc1bbdbaa3a3e3f0f878dac1480b961cb8285875b5ba0cb57a7f4089b55dc10a2eb8e0823be6cc99b36befdebd71176ab9bffce52f9bbababa36757676fe6b7c1ced42c12a7f14ac41b5aa14b56a3fc038d19605ebd4534f7dc58c193356de75d75da3fa62ac4d9b36f54d9f3e7d635eae3e1d1f433b51b0ca1f056b48a9e528f5eb8171a82d0b56d0d1d171d459679df5ccdcb9735fdcb76f5fdc8d92852b5779b9eaedeaeafa7c7cdfed46c12a7f14ac618db4248df4eb8071ae6d0b569097ac37e525e82fb7dc72cbe63d7bf6c41d69c4c26baefa9f166ceb2b570505abfc51b01ad26c596a767b600269eb82159c71c619af9d3a75eacf2fb8e0828d2b56ac78212e4bcd086fc5d0ffdb82abdbf935573105abfc51b01ad668696a743b60826afb8255e8eaea9a9ea7f78a2baef8afc71f7fbc272e4f4309efd0feb5af7ded8fe17daef26235ab5d7f5b70300a56f9a3603565b8f234dc7aa00d2858553efbd9cfbe3a2f48b3f36fca9af0faa9f007a2f3079e47f2c2f5e7e79e7b6e432853e1637777f7aa850b172ebff9e69bff10feb660b86295e7fa767c13d1462858e58f82d5b4c14ad460cb8136a3600de2cc33cffc70fecdb936cfffed2f502f866f569ebfe55996e73ff2cc99287fb879342958e58f82352271998a6f036d4cc162d42958e58f82356245a9baacffa372055428588c3a05abfc51b0929cdc3f48952b608082c5a853b0ca1f052b8d410ac4cc05469d8255fe2858690c5220662e30ea14acf247c14a63900231738151a760953f0a561a831488990b8cba458b16edebebebab79509772243f371bf282b5333e6f34ce200562e602a36ee9d2a51b7a7b7b6b1ed8a51c79e699677e9c17ace5f179a371062910331718758b172ffecc92254bb6f5f4f46c7625ab3cc9cf45cfdab56befcecbd5dff29c169f371a67900231738103223c8087ab24797685d7fb482912ce453827ca5522831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e602402283344d966587af59b366deb265cb762f5ab428bbefbefba404c9cfc5bea54b976e58bc78f167e273c6fef219707f9803c3e4fef8eb0018828295262f573fcd1fc8b3dededeacafaf2fdbb56b979420e15c8473b264c9926d79e13a2d3e6ffc533e0326d729547126c75f07c01014ac34e1ca5578208f1fe0a51ce9e9e9d99c17ace5f179637fe10a559d52e5ea15c048295869c2d382ae5c9537e1dce4056b677cded85fb84255a758b97a0530520a569af07a9ff8415dca95708ee2f346ad70a5aa4eb972f50a602414ac348d16ac175fe8c9d63cf2bd6cd5c2eb2b099f8765f176d2fa28588d19e42ad6e4783b001aa060a569a460edd8fa6cd6bde0cbd9cadfccde2f615958176f2fad8d82d5b8e82a96ab570023a560a569a460ad5bf5ab9a725564fdaa5fd76c2fad8d82d5b8e82ad6e4783d000d52b0d23452b09eb8ffa69a625524ac8bb797d646c16a4e71152b5e0e40130cd2348d14aceefbaead295645c2ba787b696d14acc19d7aeaa9afe8ececfc445757d7ffce3fcecfe7c17fe6d9d47f056b7dbe6c45fe7141fef1ffe41f4f3bfdf4d38f88f701401d0a561a05abfc51b06ae585eadff3d2f4b3fcdfff8eaaa7041b495f7fe1f20ef9004351b0d23452b0c26f0dc6c5aa4858176f2fad8d82f54f79b13a31ff37ff8b3ac569240957bbce88ef0380490a56aa460ad6ea07be5d53ac8a8475f1f6d2da2858ff90ff5bbf3acf9eb828cd9a352bbbfbeebbb3c71e7b2c5bbb766db665cb962c78fef9e72bb757ae5c99fde4273fc9aebaeaaab86015b9b7a3a3e3a8f8fe00da9a8295a69182b5e5d93f65ddf7cea92957615958176f2fad4dbb17acbcfcbc2aff777e7775299a3a756a76fbedb7873f255429538ddaba756ba56c9d7beeb971c95a77e699677e38be6f80b6a560a569a460853cfdc71fd414acb02cde4e5a9f762e58fde56a597519bae1861bb275ebd6c5dda9293b76ecc8eebaebae4a51abdaf7eecececeb3e26300684b0a569a860ad6ce9dd9ea07efa8295861595857b3bdb434ed5cb0a64457aee6ce9d9bedddbb37ee4b23d6dddd9d5d74d145d5256b4f5eb2a6c4c701d07614ac34c315acf04eed4f3ef0ad9a725524acf36eeea39b762d585d5d5d575597ab050b16c4fda825c26bb5aeb8e28aea92f5529e7f8b8f07a0ad285869062d583b77663dab17678fcfbfbaa654c509db846d5dcd1a9db463c1eaff6dc18117b4872b57a32994ac4b2eb9a4ba643de53db380b6a660a5a957b086bb6a35585ccd1a9db463c19a52f5560ce13557ad7c5a7030ebd7afcfce3befbcea92754b7c5c006d43c14a53af603572d56ab084af8df7276969b782d5d9d9f9c1fcdff5bef06f3bbc083df505edcd983f7f7e75c1ea9b366ddaf1f1f101b405052b4dbd821597a66613ef4fd2d286052bbc437ba5e484b7623890f6ecd993cd9e3d7ba064757575fdcff8f800da828295a65ec19272a59d0a56f8db8279a9d95e149c0d1b36c41d68d43dfae8a3d557b1367574741c1e1f27c084a760d5977f5feecf33395e1e53b0ca9f762a5853a74efd64516e2ebbecb2b8fb1c10e12ad6f9e79f3f50b2ce3cf3cc53e2e30498f014acfaaafe0f7cc8a2a560953f13a5600df7b318e4eb6f2d7e76c31b818e953befbc73a060757676de141f27c084a760d55755b0862c5a0a56f933810ad6903f8b41befcb7c5762b56ac887bcf01b36cd9b2ea7f3b0be2e30498f0c2008c9751b760d57d7053b0ca9f0958b0eafe2cf66ff39fc5faf0879ac7caead5abab8ff3e9ea6304680b6100c6cba8fb6016a7f2e0a660953f13b860edf7b3d8bfcd866279f8c3cc6365cb962dd5c7b72dfa4f0198f8ea0c6b69220a56f9d30605abc8fd9d9d9d2f17b75f7ef9e5b8f71c30bb77efae3eaeddf17f0b006daace83d7c083d8144f118eabd439876d91125dc15a5ff54f0b8076163f584da9f37a97e04016acfcee2a39ecb0c3b2238f3c323be594535c416b206d70056be06733ffb8ae583e96afc17aeaa9a7aa8fefe1e83f05807655efc1ab9e03597026f517acbebebe6cf9f2e5d989279e981d72c821d982050b6ab6957f660217ac9a9fcdaeaeaeff57ac1fcbdf227cf8e187ab8ff39eea6304a08dd57bf0aa672c0a56717bfefcf995db93274f1e58b671e3c66cfaf4e9952b5c21d3a64dab2c2bd687a76e2ebef8e2ecb8e38ecb0e3ae8a0fdf617ef7fb0db458e38e288eca4934eca4e38e184ecf0c30fcf8e3ffef86cdebc7935c73dd69980056bd09fcd7cf91dc5763ff9c94fe2de73c07cf7bbdf1d285879e9fb7a7c9c0030a4b12c589b366daadc7ee31bdf38b02c94abb0ec873ffc61f6fdef7fbff2795856ac9f31634665d9e5975f9e6ddfbebd66ff071f7cf0a0f757dcbeedb6db2a57d0aa6f873f8f123e3ff6d863f7db671932810ad6a0c5aad0d9d939ad2836575d7555dc7b0e887dfbf6653367ceac2e58ff1e1f27000c692c0bd673cf3d57b91dae5415cb42d90acb5e78e185ca8b9ce3f5471d755465596f6f6fddfd87a71c07bbbfe276d8f74b2fbd34703bdc4fbdedcb928952b01ad1d1d1f1a6bcd4ece92f3663f242f7279e78a2fae9c1f002f783e2e30480218d65c1aaf714616ac12aae6085af8fefafd9db65493b15ac202f568bc7f269c21b6fbcb1ba607d3b3e3e0018d65814aca15ee4dee85384575e7965cd5384c71c734c65dd430f3d94dd7efbed3585a999db43adab777b34d36e05abb3b3734a5170ce3df7dc6cc78e1d71071a352b57aeac2e57bb3b3a3ade151f1f000c6b2c0ad6a1871e5ab952f5d18f7eb4e66d1a8a17b987f52183bdc83dbc562a7e91fbdcb973b3a38f3e7ae07e8ac4f7dfc8eda1d6d5bb3d9a69b7823579f2e443f372f37451740ed41f7d0e6f2e3a7bf6ec81829517bddbe263038086c40547ca97762b584157ae283a53a74ecdbabbbbe33ed472e1971daaae5e6debe8e8382a3e2e0068888255feb463c1ca1d34e51fbf7558293c175d7451f6fcf3cfc79da8657ef9cb5f5697abf002fb8be2030280862958e54f9b16ac4953a74e7d6b6767e7d6a2f484b7e6188d9215ca55f88d454f0d02d0320a56f9d3ae052bc8cbce27a6f4bf6d43c825975c92ad5fbf3eee4823125e73153d2d18b230bc062c3e0e00688a8255feb473c10abababaceae2e59e79d775ee52d3ef6ecd91377a68685df16ac7e417b51aece39e79c37c4f70f004d53b0ca9f762f5841ff5b37bc545d8842410aefc0df68d10aefd01ede44347a9fab4ac2d382ae5c01d0320a56f9a360fd435e84fe2dcf5371390aef8d76e79d7756deffecc9279fcc366fde5c2954e1e3ead5ab2b7fb8f93bdff9ce7e7ffea62adbbca01d809653b0ca1f05eb9f4e3ffdf423f24274735e8c76d6294bcd6477b86ae5ad180018150a56f9a360d59a366ddaf17949fa5f7936d5294f43657d7fb1f20eed008c1e05abfc51b00677eaa9a7be222f4dff232f4d37e51f7f9b674d9e17fbcbd4dff23c94e79e3cdfc8f3a1f8eb0160542858e58f820500e38c8255fe28580030ce2858e58f820500e38c8255fe28580030ce2858e58f820500e3cca2458bf6f5f5f5d53ca84b39929f9b0d79c1da199f3700a0c4962e5dbaa1b7b7b7e6815dca91679e79e6c779c15a1e9f3700a0c4162f5efc99254b966cebe9e9d9ec4a5679929f8b9eb56bd7de9d97abbfe5392d3e6f0040c98507f0709524cfaef07a1f2945c2b908e744b90200000000000000000000000000000000000000003810fe3fcb3727a59f8818ae0000000049454e44ae426082	t
10	1	5d8bc39a-5ac4-4c8a-bdcf-dbf9aa80e253bpmn20.xml	9	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0d0a0d0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220d0a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220d0a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220d0a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220d0a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f7267223e0d0a0d0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469526576696577506f6f6c656422206e616d653d22506f6f6c65642052657669657720416e6420417070726f76652041637469766974692050726f63657373223e0d0a0d0a20202020202020203c73746172744576656e742069643d227374617274220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a7375626d697447726f75705265766965775461736b22202f3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773127200d0a202020202020202020202020736f757263655265663d277374617274270d0a2020202020202020202020207461726765745265663d277265766965775461736b27202f3e0d0a0d0a20202020202020203c757365725461736b2069643d227265766965775461736b22206e616d653d22526576696577205461736b220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a61637469766974695265766965775461736b223e0d0a20202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f7265766965774f7574636f6d65272c207461736b2e6765745661726961626c65282777665f7265766965774f7574636f6d652729293b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a20202020202020202020202020200d0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a20202020202020202020203c706f74656e7469616c4f776e65723e0d0a09092020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a090920202020202020203c666f726d616c45787072657373696f6e3e247b62706d5f67726f757041737369676e65652e70726f706572746965732e617574686f726974794e616d657d3c2f666f726d616c45787072657373696f6e3e0d0a09092020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a09092020203c2f706f74656e7469616c4f776e65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773227200d0a2020202020202020736f757263655265663d277265766965775461736b270d0a2020202020202020202020207461726765745265663d277265766965774465636973696f6e27202f3e0d0a0d0a20202020202020203c6578636c757369766547617465776179202069643d227265766965774465636973696f6e22206e616d653d22526576696577204465636973696f6e22202f3e200d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77332720736f757263655265663d277265766965774465636973696f6e27207461726765745265663d27617070726f76656427203e0d0a2020202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b77665f7265766965774f7574636f6d65203d3d2027417070726f7665277d3c2f636f6e646974696f6e45787072657373696f6e3e0d0a20202020202020203c2f73657175656e6365466c6f773e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773427200d0a2020202020202020736f757263655265663d277265766965774465636973696f6e270d0a2020202020202020202020207461726765745265663d2772656a656374656427202f3e0d0a0d0a202020202020203c757365725461736b2069643d22617070726f76656422206e616d653d22446f63756d656e7420417070726f766564220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a617070726f7665645461736b22203e0d0a2020202020202020202020203c646f63756d656e746174696f6e3e0d0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e6420617070726f7665642e0d0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282762706d5f61737369676e6565272c20706572736f6e293b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a20202020202020200d0a20202020202020203c757365725461736b2069643d2272656a656374656422206e616d653d22446f63756d656e742052656a6563746564220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a72656a65637465645461736b22203e0d0a2020202020202020202020203c646f63756d656e746174696f6e3e0d0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e642072656a65637465642e0d0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282762706d5f61737369676e6565272c20706572736f6e293b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77352720736f757263655265663d27617070726f766564270d0a2020202020202020202020207461726765745265663d27656e6427202f3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77362720736f757263655265663d2772656a6563746564270d0a2020202020202020202020207461726765745265663d27656e6427202f3e0d0a0d0a20202020202020203c656e644576656e742069643d22656e6422202f3e0d0a0d0a202020203c2f70726f636573733e0d0a202020200d0a2020203c212d2d2047726170686963616c20726570726573656e7461696f6e206f66206469616772616d202d2d3e0d0a2020203c62706d6e64693a42504d4e4469616772616d2069643d2242504d4e4469616772616d5f6163746976697469526576696577506f6f6c6564223e0d0a2020202020203c62706d6e64693a42504d4e506c616e652062706d6e456c656d656e743d226163746976697469526576696577506f6f6c6564220d0a20202020202020202069643d2242504d4e506c616e655f6163746976697469526576696577506f6f6c6564223e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227374617274220d0a20202020202020202020202069643d2242504d4e53686170655f7374617274223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d2233302220793d22323030223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965775461736b220d0a20202020202020202020202069643d2242504d4e53686170655f7265766965775461736b223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22313235220d0a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965774465636973696f6e220d0a20202020202020202020202069643d2242504d4e53686170655f7265766965774465636973696f6e223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223430222077696474683d2234302220783d223239302220793d22313937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22617070726f766564220d0a20202020202020202020202069643d2242504d4e53686170655f617070726f766564223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220d0a202020202020202020202020202020793d223937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d2272656a6563746564220d0a20202020202020202020202069643d2242504d4e53686170655f72656a6563746564223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220d0a202020202020202020202020202020793d22323937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22656e64222069643d2242504d4e53686170655f656e64223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d223535352220793d22333037223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7731222069643d2242504d4e456467655f666c6f7731223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d2236352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223132352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7732222069643d2242504d4e456467655f666c6f7732223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223233302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223239302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7733222069643d2242504d4e456467655f666c6f7733223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313937223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7734222069643d2242504d4e456467655f666c6f7734223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22323337223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7735222069643d2242504d4e456467655f666c6f7735223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22333037223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7736222069643d2242504d4e456467655f666c6f7736223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223535352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020203c2f62706d6e64693a42504d4e506c616e653e0d0a2020203c2f62706d6e64693a42504d4e4469616772616d3e0d0a0d0a3c2f646566696e6974696f6e733e	f
11	1	5d8bc39a-5ac4-4c8a-bdcf-dbf9aa80e253activitiReviewPooled.png	9	\\x89504e470d0a1a0a0000000d49484452000002580000016a0806000000c63c24e70000223749444154785eeddd7b901c65dd2ff07055f1fa965c14102dadf2c67baa44df3ae5ab65113da88508feb56c3689dcc2250a5649245c4bc34551c073ea7094cb1f5a1a2d50a32fde8e9a401272a21008f286c8465e09866042364b124212649390a44f3fe3f63a79667677669fd96cefcee753f5addde9eeed69b637bff9d2333b3b691200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0789765d9e16bd6ac99b76cd9b2dd8b162dcaeebbef3e2941f273b16fe9d2a51b162f5efc99f89c0100259797ab9fe60fe4596f6f6fd6d7d797eddab54b4a90702ec23959b264c9b6bc709d169f3700a0c4c295abf0401e3fc04b39d2d3d3b3392f58cbe3f306009458785ad095abf2269c9bbc60ed8ccf1b005062e1f53ef183ba942be11cc5e70d0028b1460bd68b2ff4646b1ef95eb66ae1f59584cfc3b2783b697d142c0018671a29583bb63e9b752ff872b6f237b3f74b5816d6c5db4b6ba36001c038d348c15ab7ea5735e5aac8fa55bfaed95e5a1b050b00c699460ad613f7df5453ac8a8475f1f6d2da28580030ce3452b0baefbbb6a6581509ebe2eda5b551b000609c51b0ca1f050b00c699460a56f8adc1b8581509ebe2eda5b551b000609c69a460ad7ee0db35c5aa4858176f2fad8d820500e34c23056bcbb37fcabaef9d5353aec2b2b02ede5e5a1b050b00c699460a56c8d37ffc414dc10acbe2eda4f551b000609c69a860eddc99ad7ef08e9a821596857535db4b4ba36001c038335cc10aefd4fee403dfaa295745c23aefe63eba51b000609c19b460eddc99f5ac5e9c3d3effea9a5215276c13b675356b74a26001c03853af600d77d56ab0b89a353a51b000609ca957b01ab96a3558c2d7c6fb93b428580030ced42b5871696a36f1fe242d0a16008c33f50a96942b0a160094c4942953eecf33395e1e53b0ca1f050b004a222f57597f862c5a0a56f9a3600140495415ac218b968255fe2858005012750a56dda2a560953f0a160094449d6215a752b414acf247c102e080ab531ca4892858e54f7ccea4b1c4b302802618a4f5c50f3655b97f8aa708c7555cc16a9eb90090c820ad6fb862553890052bbfbb4a0e3becb0ecc8238fcc4e39e51457d01a8882d53c73012091415adf70c5aa70200bcea4fe82d5d7d7972d5fbe3c3bf1c413b3430e39245bb06041cdb6f2cf2858cd3317001219a4f50d57ac0a6351b08adbf3e7cfafdc9e3c79f2c0b28d1b3766d3a74faf5ce10a99366d5a6559b17ecb962dd9c5175f9c1d77dc71d941071db4dffee2fd0f76bbc811471c919d74d249d909279c901d7ef8e1d9f1c71f9fcd9b37afe6b8c73a0a56f3cc05804406699ab12c589b366daadc7ee31bdf38b02c94abb0ec873ffc61f6fdef7fbff2795856ac9f31634665d9e5975f9e6ddfbebd66ff071f7cf0a0f757dcbeedb6db2a57d0aa6f3ffae8a395cf8f3df6d8fdf659862858cd3317001219a469c6b2603df7dc7395dbe14a55b12c94adb0ec85175ec8b66edd5ab3fea8a38eaa2cebededadbbfff094e360f757dc0efb7ee9a597066e87fba9b77d59a26035cf5c00486490a619cb8255ef29c2d482555cc10a5f1fdf5fb3b7cb1205ab79e60240228334cd5814aca15ee4dee85384575e7965cd5384c71c734c65dd430f3d94dd7efbed3585a999db43adab777b34a36035cf5c00486490a6198b8275e8a18756ae547df4a31fad799b86e245ee617dc8602f720faf958a5fe43e77eedcece8a38f1eb89f22f1fd37727ba875f56e8f6614ace6990b00890cd23471c191f245c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e602402283348d8255fe2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e60240228334cda2458bf6f5f5f5d53ca84b39929f9b0d79c1da199f3786662e00243248d32c5dba74436f6f6fcd03bb9423cf3cf3cc8ff382b53c3e6f0ccd5c00486490a659bc78f167962c59b2ada7a767b32b59e5497e2e7ad6ae5d7b775eaefe96e7b4f8bc313473012091419a2e3c8087ab24797685d7fb482912ce453827cad508980b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e30eab22c3b7ccd9a35f3962d5bb67bd1a245d97df7dd2725487e2ef62d5dba74c3e2c58b3f139f339a639002317381519797ab9fe60fe4596f6f6fd6d7d797eddab54b4a90702ec23959b264c9b6bc709d169f371a6790023173815117ae5c8507f2f8015eca919e9e9ecd79c15a1e9f371a67900231738151179e1674e5aabc09e7262f583be3f346e30c5220662e30eac2eb7de207752957c2398acf1b8d33488198b9c0a86bb460bdf8424fb6e691ef65ab165e5f49f83c2c8bb793d647c14a63900231738151d748c1dab1f5d9ac7bc197b395bf99bd5fc2b2b02ede5e5a1b052b8d410ac4cc05465d23056bddaa5fd594ab22eb57fdba667b696d14ac2427f70fd293e31540fb52b018758d14ac27eebfa9a6581509ebe2eda5b551b0462c94aade3c97f57f54b2800a058b51d748c1eabeefda9a625524ac8bb797d646c11a91a25c15a52abe0db43105ab8e8e8e8e43eeb8e38ef3bef9cd6ffefe9a6bae796ed6ac597f9f3973e69ef0cd0a1f2fbdf4d2bf87e537dd74d303b7de7aeb85f9f687c7fbe09f14acf247c16ada60656ab0e5409b51b0aadc72cb2dff7ac30d373c3a63c68cbd575f7d75f6b39ffd2c5bb97265b676edda6ccb962d59f0fcf3cf576effe94f7fcaeeb9e79e6cce9c39d939e79c13b65f75d5555719aa753452b0c26f0dc6c5aa4858176f2fad8d82d594e14ad470eb8136a060e5f29274ecf5d75fbfe4fcf3cfdffbd39ffeb452a29af1e28b2f66f7de7b6ff6f9cf7f7eefac59b3ba67ce9cf9dfe3fb68678d14acd50f7cbba6581509ebe2eda5b551b01ad668796a743b60826afb8275e38d377efac20b2fdc3577eedc6cfbf6ed71776acacb2fbf5c295af9fe767fee739fbbf9da6baf3d38bebf76d448c1daf2ec9fb2ee7be7d494abb02cac8bb797d646c16a48b3a5a9d9ed8109a4ad0bd60d37dc70ed05175cb077c58a1571574ab275ebd6f0d4e1aef3cf3fffc1aeaeae23e3fb6d378d14ac90a7fff8839a821596c5db49eba3600d6ba46569a45f078c736d5bb0befad5af5ef7852f7c61dfb3cf3e1bf7a396d8bb776f76d75d77bd7cf6d967af993a75eabfc4f7df4e1a2a583b7766ab1fbca3a6608565615dcdf6d2d22858434a2d49a95f0f8c436d59b0fa9f16dc3b5ae5aadadcb973777cf6b39f7de89c73ce79657c1ced62b88215dea9fdc907be5553ae8a8475decd7d74a3600daa55e5a855fb01c689b62b58d75c73cd71e13557ad7e5a7030fbf6edcb6eb9e596cd53a74e9d171f4bbb18b460eddc99f5ac5e9c3d3effea9a5215276c13b675356b74a260d5d5ea52d4eafd0125d676052bfcb6607841fb81145efc7ec105176cecececfc747c3ceda05ec11aeeaad56071356b74a260d588cbd04179ceeeffd88cf8ebe2fd0213545b15acf03e57e1ad18b66ddb1677a051b762c58a17bababa9e6ac73725ad57b01ab96a3558c2d7c6fb93b42858fb894b502847dfcd13be47e163a3256bb0af8bf70f4c406d55b0aebbeeba47c3fb5c8d952baeb8e2bff292f5a5f8b826ba7a052b2e4dcd26de9fa445c11a50affc842b50e1fb53a49192555dae8a9c55b5bedefd001348db14acf0e76f66cc98b1a7d937116da5eeeeee9efc1bfed749c30fe709a55ec1927245c1aa18acf4d42b4b4395ac7adb7fa77f79b5c1ee0f9800daa66085bf2d18fefccd580a2f789f3e7d7a6ffe4dff507c7c13998255fe4c948295ffdbba3fcfe4787903862b3bf54a53bd92556fbb7ae5aa30dcfd02e354db14ac9b6fbef90f63f9f460e1c61b6f7c28ffa65f1b1fdf78d4e883998255fe4ca08295f5a7a19fcd7e8d969c7ae5a9ba64d55b3f54b92a347affc038d23605ebcb5ffe72ef817a6b86a1e40f648f747676fe263ebef1a8d1073305abfc998005aba19fcd49cd979b7a252adc0e7f162b5ede48b92a347b1c40c9b54dc19a356bd6dfd7ae5d1bf79d03aebbbbfbcf53fef13aac71afd1073305abfc99c0056ba89fcd91969a7a25eb2fd1ed66ca5561a4c7039450db14ac0b2fbc704ff81b81636dd3a64d1bf36ffab6f8f8c6a33a0f62751fcc14acf2a70d0a56fcb3995a66ea95ac947255483d2ea024c2cc89974d48d3a64dabbce1e758dbbd7bf7cefc9bbe3b3ebef1a8ce83579cca83998255fed4397713361d1d1dd92b5ff9ca30f82e8b7fa69b149e168caf5c85db61798a705ca1641d11af00c68f306fe26513d2d9679fbdaf4457b06a86fe444e190bd6a4fe07c478f9784babfe3bdae00ad6fd53f67f8a30f54ad15057b0eafd7661a3528f0b2889307be26513d2cc99337795e13558ab56ad0aafc17a383ebef1a8ce8358dd07b39116ac49550f5aaf79cd6bb20f7ef083d91ffef0879aed469262bff1f256a7fabfa15ee2ed9b4dabf633810b565cacaa9d3c696465a65eb98aaf648da4648df47880126a9b8275e595576e7eecb1c7e2be73c02d5cb87079fe4dbf273ebef1a8d107b3d482153effd5af7e55f9fc3def794fcd76e325d5ff3dad4aabf639010b56dd9fc53a9a2d35f5ca5578cd55bddf226ca664357b1c40c9b54dc10aef3f75cf3df7c47de780bbe9a69b1ec8bfe973e2e31b8f1a7d306b45c1dab16347e5f357bdea5503eb376fde1cfe887676cc31c764af7ffdebb3d34f3f3d5bb3664dd6d3d3931d71c411d93bdef18e6ce7ce9d956dc3c7b7bffdedd9ab5ffdea6ce3c68dfbed7ba87d15eb8f3efae8eca8a38eaa7cfef5af7fbdf2b5dff8c6372ab7c3f2f075f1f1c789efb3debad7bef6b5d9873ef4a1fdaed4cd9b372f7be73bdf991d76d86135fba8befdc8238f646f7ef39bb3430f3d34bbf5d65b6bee63a84ca08235e4cfe2204e9ed458b919ac5c0df53e588d94ac46ef1f1847daa9605d3667ce9cb8ef1c70d3a74f5f376ddab4ff161fdf78d4e883592b0a567105eb539ffad4c0fa73cf3db7b2ece73fff79f6e0830f563effd8c73e565977d14517556efff297bfacdcfec52f7e51b93d73e6cc9a7d0fb7af904f7ce2139565ebd7afcfdefdee77573e7fd7bbde95ad5bb7aef2f9273ff9c99ae38f13df67bd3cfef8e3956ddefbdef70e2c0b052e94abdffef6b735db17fb9c3f7f7ef6bad7bd2e7bc31bde90fdee77bfabd96eb84c948295e0e44943979c7ae5a9de6f0bd6db6ea89235dcfd02e354db14ac8e8e8ed79f75d659fbc29590b1f2d4534f3d9b7fc39f8c8f6da24b2d5845def296b7644f3ffdf4c0fa238f3cb2669b57bce2159575ab56adca0e3ef8e04a310ab73ffef18f576efff9cf7fde6fdf8dec2b64f6ecd99565d75d775de563676767e5e357bef295cac72baeb8a2e6f8e3c4f7592494a30f7ce00395ab6bc536e1588bf5c71e7b6c65d95bdffad6ecd24b2fddefca5ab17d2860e1bfa1bbbbbb66ff8d44c1aa387952fdb253af34d52b57857adbd72b5983dd1f3001b44dc10aaebcf2ca27eebdf7deb8f71c305ffdea571fc9bfe137c4c735d1a516aceddbb7675ff8c2172a9f575f550a5776c2b2705529feda90d34e3b2d3be8a0832a57afc2c7f0b45fbcef46f775d75d7755d687a7f08e3beeb86cdbb66d958f4529bafbeebb6bbe264e7c9f45c27ec2f2071e78a0b2df78bb871f7e383be38c33069e227cdffbde57b3cf134e38a1f2f1c61b6facd97f2351b0069c3ca9b6f49c3da9fffbdc9fa1ca55a15ec93aab6a7dbdfb012690b62a585ffce2174fbbf8e28bf78ec5fb61ad5bb7eef9fc9bfddcd4a953ff253eae892eb56085cfc35b6c1457998aa7ca66cc9851b99d9fd74a3189bfbebf34547e03317c5cb87061dd7d37b2af7045acf89aebafbfbeb2acb89a15525c191b2af17d1629caddef7ffffbca6ba706db2ebc2e2b2c0fafb38af7b97af5eaca15aeeae36b260ad67e4e9eb47ff9a92e4b8d94abc2605f17ef1f9880daaa600579c1faeb585cc5fad297bef478fecdbe343e9e76d08a8215128a43b8fd918f7ca4727bcb962dd925975c921d7ffcf195a7d4e2ed43defffef75796858f43ed7bb87d8517c987ab57e169c3e22a57f8186e87e5c58be9874abccf223ffad18fb237bde94d03ebe3ed8adbe10a56f82dcaf07ab4785df8fca9a79ecadef6b6b7556e87a72ee3fb192a0a568d9327d596ac7005aad1725588bf2ede2f3041b55dc19a3973e6872fbcf0c2dd07f24d47172e5cf8d7fc1bbdbaa3a3e3f0f878dac1480b961cb8285875b5ba0cb57a7f4089b55dc10a2eb8e0823be6cc99b36befdebd71176ab9bffce52f9bbababa36757676fe6b7c1ced42c12a7f14ac41b5aa14b56a3fc038d19605ebd4534f7dc58c193356de75d75da3fa62ac4d9b36f54d9f3e7d635eae3e1d1f433b51b0ca1f056b48a9e528f5eb8171a82d0b56d0d1d171d459679df5ccdcb9735fdcb76f5fdc8d92852b5779b9eaedeaeafa7c7cdfed46c12a7f14ac618db4248df4eb8071ae6d0b569097ac37e525e82fb7dc72cbe63d7bf6c41d69c4c26baefa9f166ceb2b570505abfc51b01ad26c596a767b600269eb82159c71c619af9d3a75eacf2fb8e0828d2b56ac78212e4bcd086fc5d0ffdb82abdbf935573105abfc51b01ad668696a743b60826afb8255e8eaea9a9ea7f78a2baef8afc71f7fbc272e4f4309efd0feb5af7ded8fe17daef26235ab5d7f5b70300a56f9a3603565b8f234dc7aa00d2858553efbd9cfbe3a2f48b3f36fca9af0faa9f007a2f3079e47f2c2f5e7e79e7b6e432853e1637777f7aa850b172ebff9e69bff10feb660b86295e7fa767c13d1462858e58f82d5b4c14ad460cb8136a3600de2cc33cffc70fecdb936cfffed2f502f866f569ebfe55996e73ff2cc99287fb879342958e58f82352271998a6f036d4cc162d42958e58f82356245a9baacffa372055428588c3a05abfc51b0929cdc3f48952b608082c5a853b0ca1f052b8d410ac4cc05469d8255fe2858690c5220662e30ea14acf247c14a63900231738151a760953f0a561a831488990b8cba458b16edebebebab79509772243f371bf282b5333e6f34ce200562e602a36ee9d2a51b7a7b7b6b1ed8a51c79e699677e9c17ace5f179a371062910331718758b172ffecc92254bb6f5f4f46c7625ab3cc9cf45cfdab56befcecbd5dff29c169f371a67900231738103223c8087ab24797685d7fb482912ce453827ca5522831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e602402283344d966587af59b366deb265cb762f5ab428bbefbefba404c9cfc5bea54b976e58bc78f167e273c6fef219707f9803c3e4fef8eb0018828295262f573fcd1fc8b3dededeacafaf2fdbb56b979420e15c8473b264c9926d79e13a2d3e6ffc533e0326d729547126c75f07c01014ac34e1ca5578208f1fe0a51ce9e9e9d99c17ace5f179637fe10a559d52e5ea15c048295869c2d382ae5c9537e1dce4056b677cded85fb84255a758b97a0530520a569af07a9ff8415dca95708ee2f346ad70a5aa4eb972f50a602414ac348d16ac175fe8c9d63cf2bd6cd5c2eb2b099f8765f176d2fa28588d19e42ad6e4783b001aa060a569a460edd8fa6cd6bde0cbd9cadfccde2f615958176f2fad8d82d5b8e82a96ab570023a560a569a460ad5bf5ab9a725564fdaa5fd76c2fad8d82d5b8e82ad6e4783d000d52b0d23452b09eb8ffa69a625524ac8bb797d646c16a4e71152b5e0e40130cd2348d14aceefbaead295645c2ba787b696d14acc19d7aeaa9afe8ececfc445757d7ffce3fcecfe7c17fe6d9d47f056b7dbe6c45fe7141fef1ffe41f4f3bfdf4d38f88f701401d0a561a05abfc51b06ae585eadff3d2f4b3fcdfff8eaaa7041b495f7fe1f20ef9004351b0d23452b0c26f0dc6c5aa4858176f2fad8d82f54f79b13a31ff37ff8b3ac569240957bbce88ef0380490a56aa460ad6ea07be5d53ac8a8475f1f6d2da2858ff90ff5bbf3acf9eb828cd9a352bbbfbeebbb3c71e7b2c5bbb766db665cb962c78fef9e72bb757ae5c99fde4273fc9aebaeaaab86015b9b7a3a3e3a8f8fe00da9a8295a69182b5e5d93f65ddf7cea92957615958176f2fad4dbb17acbcfcbc2aff777e7775299a3a756a76fbedb7873f255429538ddaba756ba56c9d7beeb971c95a77e699677e38be6f80b6a560a569a460853cfdc71fd414acb02cde4e5a9f762e58fde56a597519bae1861bb275ebd6c5dda9293b76ecc8eebaebae4a51abdaf7eecececeb3e26300684b0a569a860ad6ce9dd9ea07efa8295861595857b3bdb434ed5cb0a64457aee6ce9d9bedddbb37ee4b23d6dddd9d5d74d145d5256b4f5eb2a6c4c701d07614ac34c315acf04eed4f3ef0ad9a725524acf36eeea39b762d585d5d5d575597ab050b16c4fda825c26bb5aeb8e28aea92f5529e7f8b8f07a0ad285869062d583b77663dab17678fcfbfbaa654c509db846d5dcd1a9db463c1eaff6dc18117b4872b57a32994ac4b2eb9a4ba643de53db380b6a660a5a957b086bb6a35585ccd1a9db463c19a52f5560ce13557ad7c5a7030ebd7afcfce3befbcea92754b7c5c006d43c14a53af603572d56ab084af8df7276969b782d5d9d9f9c1fcdff5bef06f3bbc083df505edcd983f7f7e75c1ea9b366ddaf1f1f101b405052b4dbd821597a66613ef4fd2d286052bbc437ba5e484b7623890f6ecd993cd9e3d7ba064757575fdcff8f800da828295a65ec19272a59d0a56f8db8279a9d95e149c0d1b36c41d68d43dfae8a3d557b1367574741c1e1f27c084a760d5977f5feecf33395e1e53b0ca9f762a5853a74efd64516e2ebbecb2b8fb1c10e12ad6f9e79f3f50b2ce3cf3cc53e2e30498f014acfaaafe0f7cc8a2a560953f13a5600df7b318e4eb6f2d7e76c31b818e953befbc73a060757676de141f27c084a760d55755b0862c5a0a56f933810ad6903f8b41befcb7c5762b56ac887bcf01b36cd9b2ea7f3b0be2e30498f0c2008c9751b760d57d7053b0ca9f0958b0eafe2cf66ff39fc5faf0879ac7caead5abab8ff3e9ea6304680b6100c6cba8fb6016a7f2e0a660953f13b860edf7b3d8bfcd866279f8c3cc6365cb962dd5c7b72dfa4f0198f8ea0c6b69220a56f9d30605abc8fd9d9d9d2f17b75f7ef9e5b8f71c30bb77efae3eaeddf17f0b006daace83d7c083d8144f118eabd439876d91125dc15a5ff54f0b8076163f584da9f37a97e04016acfcee2a39ecb0c3b2238f3c323be594535c416b206d70056be06733ffb8ae583e96afc17aeaa9a7aa8fefe1e83f05807655efc1ab9e03597026f517acbebebe6cf9f2e5d989279e981d72c821d982050b6ab6957f660217ac9a9fcdaeaeaeff57ac1fcbdf227cf8e187ab8ff39eea6304a08dd57bf0aa672c0a56717bfefcf995db93274f1e58b671e3c66cfaf4e9952b5c21d3a64dab2c2bd687a76e2ebef8e2ecb8e38ecb0e3ae8a0fdf617ef7fb0db458e38e288eca4934eca4e38e184ecf0c30fcf8e3ffef86cdebc7935c73dd69980056bd09fcd7cf91dc5763ff9c94fe2de73c07cf7bbdf1d285879e9fb7a7c9c0030a4b12c589b366daadc7ee31bdf38b02c94abb0ec873ffc61f6fdef7fbff2795856ac9f31634665d9e5975f9e6ddfbebd66ff071f7cf0a0f757dcbeedb6db2a57d0aa6f873f8f123e3ff6d863f7db671932810ad6a0c5aad0d9d939ad2836575d7555dc7b0e887dfbf6653367ceac2e58ff1e1f27000c692c0bd673cf3d57b91dae5415cb42d90acb5e78e185ca8b9ce3f5471d755465596f6f6fddfd87a71c07bbbfe276d8f74b2fbd34703bdc4fbdedcb928952b01ad1d1d1f1a6bcd4ece92f3663f242f7279e78a2fae9c1f002f783e2e30480218d65c1aaf714616ac12aae6085af8fefafd9db65493b15ac202f568bc7f269c21b6fbcb1ba607d3b3e3e0018d65814aca15ee4dee85384575e7965cd5384c71c734c65dd430f3d94dd7efbed3585a999db43adab777b34d36e05abb3b3734a5170ce3df7dc6cc78e1d71071a352b57aeac2e57bb3b3a3ade151f1f000c6b2c0ad6a1871e5ab952f5d18f7eb4e66d1a8a17b987f52183bdc83dbc562a7e91fbdcb973b3a38f3e7ae07e8ac4f7dfc8eda1d6d5bb3d9a69b7823579f2e443f372f37451740ed41f7d0e6f2e3a7bf6ec81829517bddbe263038086c40547ca97762b584157ae283a53a74ecdbabbbbe33ed472e1971daaae5e6debe8e8382a3e2e0068888255feb463c1ca1d34e51fbf7558293c175d7451f6fcf3cfc79da8657ef9cb5f5697abf002fb8be2030280862958e54f9b16ac4953a74e7d6b6767e7d6a2f484b7e6188d9215ca55f88d454f0d02d0320a56f9d3ae052bc8cbce27a6f4bf6d43c825975c92ad5fbf3eee4823125e73153d2d18b230bc062c3e0e00688a8255feb473c10abababaceae2e59e79d775ee52d3ef6ecd91377a68685df16ac7e417b51aece39e79c37c4f70f004d53b0ca9f762f5841ff5b37bc545d8842410aefc0df68d10aefd01ede44347a9fab4ac2d382ae5c01d0320a56f9a360fd435e84fe2dcf5371390aef8d76e79d7756deffecc9279fcc366fde5c2954e1e3ead5ab2b7fb8f93bdff9ce7e7ffea62adbbca01d809653b0ca1f05eb9f4e3ffdf423f24274735e8c76d6294bcd6477b86ae5ad180018150a56f9a360d59a366ddaf17949fa5f7936d5294f43657d7fb1f20eed008c1e05abfc51b00677eaa9a7be222f4dff232f4d37e51f7f9b674d9e17fbcbd4dff23c94e79e3cdfc8f3a1f8eb0160542858e58f820500e38c8255fe28580030ce2858e58f820500e38c8255fe28580030ce2858e58f820500e3cca2458bf6f5f5f5d53ca84b39929f9b0d79c1da199f3700a0c4962e5dbaa1b7b7b7e6815dca91679e79e6c779c15a1e9f3700a0c4162f5efc99254b966cebe9e9d9ec4a5679929f8b9eb56bd7de9d97abbfe5392d3e6f0040c98507f0709524cfaef07a1f2945c2b908e744b90200000000000000000000000000000000000000003810fe3fcb3727a59f8818ae0000000049454e44ae426082	t
14	1	563c2242-5935-49a3-a706-a23a1535bebbbpmn20.xml	13	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0a0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f7267223e0a0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469506172616c6c656c52657669657722206e616d653d22506172616c6c656c2052657669657720416e6420417070726f76652041637469766974692050726f63657373223e0a0a20202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c61637469766974693a657865637574696f6e4c697374656e6572206576656e743d2273746172742220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e6c697374656e65722e536372697074457865637574696f6e4c697374656e6572223e0a202020202020202009093c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f617070726f7665436f756e74272c2030293b0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f72656a656374436f756e74272c2030293b0a2020202020202020202020202020202020202020202020200a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c50657263656e74272c2030293b0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c52656a65637450657263656e74272c2030293b0a2020202020202020202020202020202020202020202020200a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f7265766965776572436f756e74272c2062706d5f61737369676e6565732e73697a652829293b0a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f726571756972656450657263656e74272c2077665f7265717569726564417070726f766550657263656e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020203c2f61637469766974693a657865637574696f6e4c697374656e65723e0a20202020202020203c2f657874656e73696f6e456c656d656e74733e0a20202020202020200a20202020202020203c73746172744576656e742069643d227374617274220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a7375626d6974506172616c6c656c5265766965775461736b22202f3e0a20202020202020200a0909090a20202020202020203c73657175656e6365466c6f772069643d27666c6f773127200a202020202020202020202020736f757263655265663d277374617274270a2020202020202020202020207461726765745265663d277265766965775461736b27202f3e0a0a20202020202020203c757365725461736b2069643d227265766965775461736b22206e616d653d22526576696577205461736b220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a61637469766974695265766965775461736b223e0a20202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a2020202020202020202020202020202020202020202020206966287461736b2e6765745661726961626c654c6f63616c282777665f7265766965774f7574636f6d652729203d3d2027417070726f76652729207b0a20202020202020202020202020202020202020202020202020202020766172206e6577417070726f766564436f756e74203d2077665f617070726f7665436f756e74202b20313b0a0920202020202020202020202020202020202020202020202020766172206e6577417070726f76656450657263656e74616765203d20286e6577417070726f766564436f756e74202f2077665f7265766965776572436f756e7429202a203130303b0a09202020202020202020202020202020202020202020202020200a0920202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f617070726f7665436f756e74272c206e6577417070726f766564436f756e74293b0a0920202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c50657263656e74272c206e6577417070726f76656450657263656e74616765293b0a2020202020202020202020202020202020202020202020207d20656c7365207b0a20202020202020202020202020202020202020202020202020202020766172206e657752656a656374436f756e74203d2077665f72656a656374436f756e74202b20313b0a20202020202020202020202020202020202020202020202020202020766172206e657752656a65637450657263656e74616765203d20286e657752656a656374436f756e74202f2077665f7265766965776572436f756e7429202a203130303b0a202020202020202020202020202020202020202020202020202020200a20202020202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f72656a656374436f756e74272c206e657752656a656374436f756e74293b0a20202020202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c52656a65637450657263656e74272c206e657752656a65637450657263656e74616765293b0a2020202020202020202020202020202020202020202020207d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a20202020202020202020200a20202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b72657669657741737369676e65652e70726f706572746965732e757365724e616d657d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020202020200a20202020202020202020203c212d2d20466f7220656163682061737369676e65652c207461736b2069732063726561746564202d2d3e0a20202020202020202020203c6d756c7469496e7374616e63654c6f6f7043686172616374657269737469637320697353657175656e7469616c3d2266616c7365223e0a20202020202020202020200920203c6c6f6f7044617461496e7075745265663e62706d5f61737369676e6565733c2f6c6f6f7044617461496e7075745265663e0a20202020202020202020200920203c696e707574446174614974656d206e616d653d2272657669657741737369676e656522202f3e0a20202020202020202020200920203c636f6d706c6574696f6e436f6e646974696f6e3e247b77665f61637475616c50657263656e74203e3d2077665f7265717569726564417070726f766550657263656e74207c7c2077665f7265717569726564417070726f766550657263656e74203e2028313030202d2077665f61637475616c52656a65637450657263656e74297d3c2f636f6d706c6574696f6e436f6e646974696f6e3e0a20202020202020202020203c2f6d756c7469496e7374616e63654c6f6f704368617261637465726973746963733e0a20202020202020203c2f757365725461736b3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773227200a202020202020202009736f757263655265663d277265766965775461736b270a2020202020202020202020207461726765745265663d277265766965774465636973696f6e27202f3e0a0a20202020202020203c6578636c757369766547617465776179202069643d227265766965774465636973696f6e22206e616d653d22526576696577204465636973696f6e22202f3e200a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77332720736f757263655265663d277265766965774465636973696f6e27207461726765745265663d27617070726f76656427203e0a2020202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b77665f61637475616c50657263656e74203e3d2077665f7265717569726564417070726f766550657263656e747d3c2f636f6e646974696f6e45787072657373696f6e3e0a20202020202020203c2f73657175656e6365466c6f773e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773427200a2020202020202020736f757263655265663d277265766965774465636973696f6e270a2020202020202020202020207461726765745265663d2772656a656374656427202f3e0a0a202020202020203c757365725461736b2069643d22617070726f76656422206e616d653d22446f63756d656e7420417070726f766564220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a617070726f766564506172616c6c656c5461736b22203e0a2020202020202020202020203c646f63756d656e746174696f6e3e0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e6420617070726f7665642e0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020202020200a2020202020202020202020202020202020202020202020202f2f2053657420706172616c6c656c2072657669657720706172616d73206f6e207461736b2c20746f206265206b65707420696e20686973746f72790a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f7265766965776572436f756e74272c2077665f7265766965776572436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f726571756972656450657263656e74272c2077665f726571756972656450657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c50657263656e74272c2077665f61637475616c50657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f617070726f7665436f756e74272c2077665f617070726f7665436f756e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a2020202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020203c2f757365725461736b3e0a20202020202020200a20202020202020203c757365725461736b2069643d2272656a656374656422206e616d653d22446f63756d656e742052656a6563746564220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a72656a6563746564506172616c6c656c5461736b22203e0a2020202020202020202020203c646f63756d656e746174696f6e3e0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e642072656a65637465642e0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020202020200a2020202020202020202020202020202020202020202020202f2f2053657420706172616c6c656c2072657669657720706172616d73206f6e207461736b2c20746f206265206b65707420696e20686973746f72790a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f7265766965776572436f756e74272c2077665f7265766965776572436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f726571756972656450657263656e74272c2077665f726571756972656450657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c50657263656e74272c2077665f61637475616c50657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f617070726f7665436f756e74272c2077665f617070726f7665436f756e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a2020202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020203c2f757365725461736b3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77352720736f757263655265663d27617070726f766564270a2020202020202020202020207461726765745265663d27656e6427202f3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77362720736f757263655265663d2772656a6563746564270a2020202020202020202020207461726765745265663d27656e6427202f3e0a0a20202020202020203c656e644576656e742069643d22656e6422202f3e0a0a202020203c2f70726f636573733e0a202020200a2020203c212d2d2047726170686963616c20726570726573656e7461696f6e206f66206469616772616d202d2d3e0a2020203c62706d6e64693a42504d4e4469616772616d2069643d2242504d4e4469616772616d5f6163746976697469506172616c6c656c526576696577223e0a2020202020203c62706d6e64693a42504d4e506c616e652062706d6e456c656d656e743d226163746976697469506172616c6c656c526576696577220a20202020202020202069643d2242504d4e506c616e655f6163746976697469506172616c6c656c526576696577223e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227374617274220a20202020202020202020202069643d2242504d4e53686170655f7374617274223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d2233302220793d22323030223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965775461736b220a20202020202020202020202069643d2242504d4e53686170655f7265766965775461736b223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22313235220a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965774465636973696f6e220a20202020202020202020202069643d2242504d4e53686170655f7265766965774465636973696f6e223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223430222077696474683d2234302220783d223239302220793d22313937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22617070726f766564220a20202020202020202020202069643d2242504d4e53686170655f617070726f766564223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220a202020202020202020202020202020793d223937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d2272656a6563746564220a20202020202020202020202069643d2242504d4e53686170655f72656a6563746564223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220a202020202020202020202020202020793d22323937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22656e64222069643d2242504d4e53686170655f656e64223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d223535352220793d22333037223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7731222069643d2242504d4e456467655f666c6f7731223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d2236352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223132352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7732222069643d2242504d4e456467655f666c6f7732223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223233302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223239302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7733222069643d2242504d4e456467655f666c6f7733223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313937223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7734222069643d2242504d4e456467655f666c6f7734223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22323337223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7735222069643d2242504d4e456467655f666c6f7735223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22333037223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7736222069643d2242504d4e456467655f666c6f7736223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223535352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020203c2f62706d6e64693a42504d4e506c616e653e0a2020203c2f62706d6e64693a42504d4e4469616772616d3e0a0a3c2f646566696e6974696f6e733e	f
15	1	563c2242-5935-49a3-a706-a23a1535bebbactivitiParallelReview.png	13	\\x89504e470d0a1a0a0000000d49484452000002580000016a0806000000c63c24e70000226749444154785eeddd7d905c659d2ff0f0aae2baba252f0a889656f9c6de2a5fb66eb95a5bc4bda8c522f8d7309924f2165ea2609544222f961b5e5c14d87beb7257c03fb4345aa04617dfae9a401272a31008b23132919560082664322421e46599242439f73ced9cb1f374cf4cf73c3d99d3d39f4fd5b766fa9c33a70f7326bffe72baa767ca140000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080769765d9b1ebd6ad5bb062c58a7d4b962cc91e78e0012941f2737170f9f2e59b962e5dfa89f89c0100259797ab1fe40fe4597f7f7f36303090edddbb574a90702ec23959b66cd98ebc709d1d9f3700a0c4c295abf0401e3fc04b39d2d7d7b7352f582be3f306009458785ad095abf2269c9bbc60ed89cf1b005062e1f53ef183ba942be11cc5e70d0028b1460bd6ee17fbb2758f7d335bb3f8a64ac2e76159bc9db43e0a1600b499460ad6aeedcf65bd8bbe98adfef9dc4312968575f1f6d2da285800d0661a29581bd6fcb4a65c15d9b8e66735db4b6ba36001409b69a4603df9e0ad35c5aa4858176f2fad8d8205006da69182d5fbc00d35c5aa4858176f2fad8d8205006d46c12a7f142c0068338d14acf05b8371b12a12d6c5db4b6ba36001409b69a460ad7de8ab35c5aa4858176f2fad8d8205006da69182b5edb9df65bdf7cfab2957615958176f2fad8d8205006da6918215f2cc6fbe5d53b0c2b2783b697d142c0068330d15ac3d7bb2b50fdf5d53b0c2b2b0ae667b6969142c006833a315acf04eed4f3df46f35e5aa4858e7dddcc7370a1600b499610bd69e3d59dfdaa5d9130bafaf295571c236615b57b3c6270a1600b4997a056bb4ab56c3c5d5acf1898205006da65ec16ae4aad570095f1bef4fd2a26001409ba957b0e2d2d46ce2fd495a142c006833f50a96942b0a160094c4b469d31ecc33355e1e53b0ca1f050b004a222f57d960462c5a0a56f9a3600140495415ac118b968255fe2858005012750a56dda2a560953f0a160094449d6215a752b414acf247c102e0b0ab531ca4892858e54f7ccea4b1c4b302802618a4f5c50f365579709aa708db2aae6035cf5c00486490d6375ab12a1cce8295df5d25c71c734c76fcf1c767679e79a62b680d44c16a9eb90090c820ad6fb46255389c0567ca60c11a1818c856ae5c999d7efae9d951471d952d5ab4a8665bf94b14ace6990b00890cd2fa462b5685892858c5ed850b17566e4f9d3a7568d9e6cd9bb399336756ae7085cc9831a3b2ac58bf6ddbb6ec8a2baec84e39e594ec88238e38647ff1fe87bb5de4b8e38ecbdefbdef766a79d765a76ecb1c766a79e7a6ab660c1829ae39ee82858cd3317001219a46926b2606dd9b2a572fbf5af7ffdd0b250aec2b2ef7ce73bd9b7bef5adcae76159b17ed6ac5995659ffffce7b39d3b77d6ecffc8238f1cf6fe8adb77de7967e50a5af5edc71f7fbcf2f9c9279f7cc83ecb1005ab79e60240228334cd4416ace79f7fbe723b5ca92a9685b21596bdf8e28bd9f6eddb6bd69f70c2099565fdfdfd75f71f9e721ceefe8adb61df2fbdf4d2d0ed703ff5b62f4b14ace6990b00890cd2341359b0ea3d45985ab08a2b58e1ebe3fb6bf67659a26035cf5c00486490a699888235d28bdc1b7d8af0da6bafad798af0a4934eaaac7be49147b2bbeebaaba63035737ba475f56e8f6714ace6990b00890cd2341351b08e3efae8ca95aa0f7ff8c3356fd350bcc83dac0f19ee45eee1b552f18bdce7cf9f9f9d78e28943f75324beff466e8fb4aedeedf18c82d53c73012091419a262e3852be2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e602402283348d8255fe2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a659b264c9c18181819a07752947f273b3292f587be2f3c6c8cc05804406699ae5cb976feaefefaf79609772e4d9679ffd5e5eb056c6e78d91990b00890cd2344b972efdc4b265cb76f4f5f56d7525ab3cc9cf45dffaf5ebefcdcbd59ff29c1d9f3746662e00243248d38507f0709524cfdef07a1f2945c2b908e744b91a0373012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc05c65d9665c7ae5bb76ec18a152bf62d59b2247be08107a404c9cfc5c1e5cb976f5aba74e927e27346730c5220662e30eef272f583fc813cebefefcf060606b2bd7bf74a0912ce453827cb962ddb9117aeb3e3f346e30c5220662e30eec295abf0401e3fc04b39d2d7d7b7352f582be3f346e30c5220662e30eec2d382ae5c9537e1dce4056b4f7cde689c410ac4cc05c65d78bd4ffca02ee54a3847f179a371062910331718778d16acdd2ff665eb1efb66b666f14d9584cfc3b2783b697d14ac34062910331718778d14ac5ddb9fcb7a177d315bfdf3b987242c0bebe2eda5b551b0d218a440cc5c60dc3552b036acf9694db92ab271cdcf6ab697d646c14a72c6e0203d235e01742e058b71d748c17af2c15b6b8a5591b02ede5e5a1b056bcc42a9eacf73f5e047250ba850b018778d14acde076ea8295645c2ba787b696d14ac3129ca5551aae2db400753b0eae8eaea3aeaeebbefbef85ffff55f7ff5852f7ce1f93973e6fcd7ecd9b3f7876f56f878d55557fd57587eebadb73e74c71d775c966f7f6cbc0ffe42c12a7f14aca60d57a6865b0e741805abcaedb7dffeb737df7cf3e3b366cd3a70fdf5d7673ffce10fb3d5ab5767ebd7afcfb66ddb96052fbcf042e5f6ef7ef7bbecbefbeecbe6cd9b975d78e18561fb35d75d779da15a4723052bfcd6605cac8a8475f1f6d2da28584d19ad448db61ee8000a562e2f4927df74d34dcb2eb9e492033ff8c10f2a25aa19bb77efceeebffffeecd39ffef4813973e6f4ce9e3dfbbfc7f7d1c91a29586b1ffa6a4db12a12d6c5db4b6ba36035acd1f2d4e876c024d5f105eb965b6ef9f865975db677fefcf9d9ce9d3be3eed494975f7eb952b4f2fdedfbd4a73e75db0d37dc70647c7f9da89182b5edb9df65bdf7cfab2957615958176f2fad8d82d590664b53b3db0393484717ac9b6fbef9864b2fbdf4c0aa55abe2ae9464fbf6ede1a9c3bd975c72c9c33d3d3dc7c7f7db691a295821cffce6db35052b2c8bb793d647c11ad558cbd258bf0e68731d5bb0bef4a52fddf899cf7ce6e073cf3d17f7a3963870e04076cf3df7bc7cc10517ac9b3e7dfadfc4f7df491a2a587bf6646b1fbebba6608565615dcdf6d2d22858234a2d49a95f0fb4a18e2c58834f0b1e18af72556dfefcf9bb3ef9c94f3e72e18517be323e8e4e315ac10aefd4fed443ff5653ae8a8475decd7d7ca3600dab55e5a855fb01da44c715ac2f7ce10ba784d75cb5fa69c1e11c3c7830bbfdf6dbb74e9f3e7d417c2c9d62d882b5674fd6b77669f6c4c2eb6b4a559cb04dd8d6d5acf189825557ab4b51abf7079458c715acf0db82e105ed875378f1fba5975ebab9bbbbfbe3f1f174827a056bb4ab56c3c5d5acf1898255232e4347e4b960f06333e2af8bf70b4c521d55b0c2fb5c85b762d8b16347dc81c6ddaa55ab5eece9e979ba13df94b45ec16ae4aad570095f1bef4fd2a2601d222e41a11c7d234ff81e858f8d96ace1be2ede3f30097554c1baf1c61b1f0fef733551aeb9e69affcc4bd6e7e2e39aecea15acb834359b787f9216056b48bdf213ae4085ef4f91464a5675b92a727ed5fa7af7034c221d53b0c29fbf99356bd6fe66df44b4957a7b7bfbf26ff81fa78c3e9c27957a054bca1505ab62b8d253af2c8d54b2ea6dfff5c1e5d586bb3f6012e8988215feb660f8f3371329bce07de6cc99fdf937fd83f1f14d660a56f933590a56fe6febc13c53e3e50d18adecd42b4df54a56bdedea95abc268f70bb4a98e2958b7dd76dbaf27f2e9c1c22db7dcf248fe4dbf213ebe76d4e883998255fe4ca282950da6a19fcd418d969c7ae5a9ba64d55b3f52b92a347aff401be99882f5c52f7eb1ff70bd35c348f207b2c7babbbb7f1e1f5f3b6af4c14cc12a7f2661c16ae867734af3e5a65e890ab7c39fc58a973752ae0acd1e0750721d53b0e6cc99f35febd7af8ffbce61d7dbdbfbfb697f7e1d56db6bf4c14cc12a7f2671c11ae96773aca5a65ec9fa4374bb99725518ebf10025d43105ebb2cb2edb1ffe46e044dbb265cbe6fc9bbe233ebe7654e741acee83998255fe7440c18a7f3653cb4cbd929552ae0aa9c70594449839f1b24969c68c199537fc9c68fbf6eddb937fd3f7c5c7d78eea3c78c5a93c982958e54f9d733769d3d5d595bdf295af0c83efeaf867ba49e169c1f8ca55b81d96a708c7154ad671f10aa07d8479132f9b942eb8e0828325ba825533f42773ca58b0a60c3e20c6cbdb2dadfaefe8802b580f4e3bf429c2d42b45235dc1aaf7db858d4a3d2ea024c2ec89974d4ab367cfde5b86d760ad59b326bc06ebd1f8f8da519d07b1ba0f66632d5853aa1eb4feeaaffe2afbc0073e90fdfad7bfaed96e2c29f61b2f6f75aaff1bea25debed9b46a3f93b860c5c5aada1953c65666ea95abf84ad6584ad6588f0728a18e2958d75e7bedd6dffef6b771df39ec162f5ebc32ffa6df171f5f3b6af4c12cb56085cf7ffad39f563e7fd7bbde55b35dbba4fabfa75569d53e2761c1aafbb35847b3a5a65eb90aafb9aaf75b84cd94ac668f0328b98e2958e1fda7eebbefbeb8ef1c76b7de7aeb43f9377d5e7c7ceda8d107b35614ac5dbb76553e7fd5ab5e35b47eebd6ade18f6867279d7452f6dad7be363be79c73b275ebd6657d7d7dd971c71d97bded6d6fcbf6ecd953d9367c7ceb5bdf9abdfad5afce366fde7cc8be47da57b1fec4134fcc4e38e184cae75ffef2972b5ffb95af7ca5723b2c0f5f171f7f9cf83eebad7bcd6b5e937df0831f3ce44add82050bb2b7bffdedd931c71c53b38feadb8f3df658f6c637be313bfae8a3b33beeb8a3e63e46ca242a5823fe2c0ee38c298d959be1cad548ef83d548c96af4fe8136d24905ebea79f3e6c57de7b09b3973e686193366fcb7f8f8da51a30f66ad2858c515ac7ffaa77f1a5a7fd145175596fde8473fca1e7ef8e1cae7fff88fff585977f9e597576effe4273fa9dcfef18f7f5cb93d7bf6ec9a7d8fb6af908f7ef4a395651b376eccdef9ce77563e7fc73bde916dd8b0a1f2f9c73ef6b19ae38f13df67bd3cf1c413956ddefdee770f2d0b052e94ab5ffce21735db17fb5cb87061f6d77ffdd7d9eb5ef7baec97bffc65cd76a365b214ac04674c19b9e4d42b4ff57e5bb0de762395acd1ee1768531d53b0bababa5e7bfef9e71f0c574226cad34f3ffd5cfe0d7f2a3eb6c92eb5601579d39bde943df3cc3343eb8f3ffef89a6d5ef18a5754d6ad59b3263bf2c8232bc528dcfec8473e52b9fdfbdffffe907d37b2af90b973e75696dd78e38d958fdddddd958ffffccfff5cf978cd35d7d41c7f9cf83e8b8472f4fef7bfbf7275add8261c6bb1fee4934fae2c7bf39bdf9c5d75d555875c592bb60f052cfc37f4f6f6d6ecbf91285815674ca95f76ea95a67ae5aa506ffb7a256bb8fb0326818e2958c1b5d75efbe4fdf7df1ff79ec3e64b5ffad263f937fce6f8b826bbd482b573e7ceec339ff94ce5f3eaab4ae1ca4e5816ae2ac55f1b72f6d96767471c7144e5ea55f8189ef68bf7dde8beeeb9e79ecafaf014de29a79c92edd8b1a3f2b12845f7de7b6fcdd7c489efb348d84f58fed0430f55f61b6ff7e8a38f66e79e7beed05384ef79cf7b6af679da69a7553ede72cb2d35fb6f240ad69033a6d4969e0ba60c7e9f073352b92ad42b59e757adaf773fc024d25105ebb39ffdecd9575c71c58189783fac0d1b36bc907fb39f9f3e7dfadfc4c735d9a516acf079788b8de22a53f154d9ac59b32ab7f3f35a2926f1d70f9686ca6f20868f8b172faebbef46f615ae88155f73d34d3755961557b3428a2b632325becf2245b9fbd5af7e5579edd470db85d76585e5e17556f13ed7ae5d5bb9c2557d7ccd44c13ac419530e2d3fd565a991725518eeebe2fd0393504715ac202f587f9c88ab589ffbdce79ec8bfd957c5c7d3095a51b042427108b7ffe11ffea1727bdbb66dd995575e999d7aeaa995a7d4e2ed43def7bef75596858f23ed7bb47d8517c987ab57e169c3e22a57f8186e87e5c58be9474abccf22dffdee77b337bce10d43ebe3ed8adbe10a56f82dcaf07ab4785df8fce9a79fcedef296b7546e87a72ee3fb19290a568d33a6d496ac7005aad1725588bf2ede2f3049755cc19a3d7bf6872ebbecb27d87f34d47172f5efcc7fc1bbdb6ababebd8f8783ac1580b961cbe285875b5ba0cb57a7f4089755cc10a2ebdf4d2bbe7cd9bb7f7c0810371176ab93ffce10f5b7a7a7ab6747777ff6d7c1c9d42c12a7f14ac61b5aa14b56a3f409be8c88275d65967bd62d6ac59abefb9e79e717d31d6962d5b0666ce9cb9392f571f8f8fa193b463c1dabd7b77e537f2c2c794e5ed12056b44a9e528f5eb8136d491052be8eaea3ae1fcf3cf7f76fefcf9bb0f1e3c1877a364e1ca555eaefa7b7a7a3e1ddf77a769c78215ca527ee8356f7bd0ecf276898235aab196a4b17e1dd0e63ab6600579c97a435e82fe70fbedb76fddbf7f7fdc91c62cbce66af069c18ebe725550b0ca1f05ab21cd96a566b70726918e2e58c1b9e79efb9ae9d3a7ffe8d24b2fddbc6ad5aa17e3b2d48cf0560c83bf2db8b6935f731553b0ca1f05ab618d96a646b70326a98e2f58859e9e9e9979faafb9e69aff7ce28927fae2f23492f00eedfff22ffff29bf03e5779b19ad3a9bf2d381c05abfc51b09a325a791a6d3dd00114ac2a9ffce4275f9d17a4b9f937655d78fd54f803d1f903cf6379e1fafdf3cf3fbf2994a9f0317f105db378f1e295b7dd76dbafc3df160c57acf2dcd4896f22da0805abfc51b09a365c891a6e39d06114ac619c77de791fcabf3937e4f9bf83056a77f866e5f9539e1579fe3dcfbcc9f2879bc7938255fe2858631297a9f836d0c1142cc69d8255fe2858635694aaab073f2a57408582c5b853b0ca1f052bc919838354b9028628588c3b05abfc51b0d218a440cc5c60dc2958e58f8295c6200562e602e34ec12a7f14ac34062910331718770a56f9a360a531488198b9c0b85bb264c9c18181819a07f53267b8c2d4ecf276487e6e36e5056b4f74da6882410ac4cc05c6ddf2e5cb37f5f7f7d73cb09739bb77efae94a5f03165793be4d9679ffd5e5eb056c6e78dc619a440cc5c60dc2d5dbaf413cb962ddbd1d7d7b7b5ddae644de6e4e7a26ffdfaf5f7e6e5ea4f79ce8ecf1b8d33488198b9c061111ec0c355923c7bc3eb7da41409e7229c13e52a91410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091419a26cbb263d7ad5bb760c58a15fb962c59923df0c0035282e4e7e2e0f2e5cb372d5dbaf413f139e350f90c7830cc8151f260fc75008c40c14a9397ab1fe40fe4597f7f7f36303090edddbb574a90702ec23959b66cd98ebc709d1d9f37fe229f0153eb14aa3853e3af0360040a569a70e52a3c90c70ff0528ef4f5f56dcd0bd6caf8bc71a87085aa4ea972f50a60ac14ac34e1694157aeca9b706ef282b5273e6f1c2a5ca1aa53ac5cbd02182b052b4d78bd4ffca02ee54a3847f179a356b85255a75cb97a0530160a569a460bd6ee17fbb2758f7d335bb3f8a64ac2e76159bc9db43e0a566386b98a3535de0e80062858691a2958bbb63f97f52efa62b6fae7730f495816d6c5db4b6ba360352eba8ae5ea15c0582958691a29581bd6fcb4a65c15d9b8e66735db4b6ba360352eba8a35355e0f408314ac348d14ac271fbcb5a6581509ebe2eda5b551b09a535cc58a9703d00483344d2305abf7811b6a8a5591b02ede5e5a1b056b78679d75d62bbabbbb3fdad3d3f3bff38f0bf379f01f79b60c5ec1da982f5b957f5c947ffc3ff9c7b3cf39e79ce3e27d005087829546c12a7f14ac5a79a1fafbbc34fd30fff7bfabea29c146323058b8bc433ec04814ac348d14acf05b8371b12a12d6c5db4b6ba360fd455eac4ecfffcdffb84e711a4bc2d5ae73e3fb00608a8295aa9182b5f6a1afd614ab22615dbcbdb4360ad69fe5ffd6afcfb33f2e4a73e6ccc9eebdf7deecb7bffd6db67efdfa6cdbb66d59f0c20b2f546eaf5ebd3afbfef7bf9f5d77dd7571c12a727f5757d709f1fd017434052b4d23056bdb73bfcb7aef9f5753aec2b2b02ede5e5a9b4e2f5879f97955feeffcdeea52347dfaf4ecaebbee0a7f4aa852a61ab57dfbf64ad9bae8a28be292b5e1bcf3cefb507cdf001d4bc14ad348c10a79e637dfae29586159bc9db43e9d5cb006cbd58aea3274f3cd37671b366c88bb535376edda95dd73cf3d95a256b5ef7ddddddde7c7c700d09114ac340d15ac3d7bb2b50fdf5d53b0c2b2b0ae667b69693ab9604d8bae5ccd9f3f3f3b70e040dc97c6acb7b737bbfcf2cbab4bd6febc644d8b8f03a0e3285869462b58e19dda9f7ae8df6aca5591b0cebbb98f6f3ab560f5f4f45c575dae162d5a14f7a39608afd5bae69a6baa4bd64b79fe2e3e1e808ea260a519b660edd993f5ad5d9a3db1f0fa9a5215276c13b675356b7cd289056bf0b705875ed01eae5c8da750b2aebcf2caea92f5b4f7cc023a9a8295a65ec11aedaad57071356b7cd289056b5ad55b3184d75cb5f269c1e16cdcb831bbf8e28bab4bd6edf17101740c052b4dbd82d5c855abe112be36de9fa4a5d30a567777f707f27fd707c3bfedf022f4d417b43763e1c285d5056b60c68c19a7c6c707d01114ac34f50a565c9a9a4dbc3f494b0716acf00eed959213de8ae170dabf7f7f3677eedca192d5d3d3f33fe3e303e8080a569a7a054bca954e2a58e16f0be6a566675170366dda1477a071f7f8e38f575fc5dad2d5d5756c7c9c00939e82555ffe7d7930cfd478794cc12a7f3aa9604d9f3efd6345b9b9faeaabe3ee735884ab58975c72c950c93aefbcf3ce8c8f1360d253b0eaabfa3ff0118b968255fe4c968235dacf6290afbfa3f8d90d6f043a51bef6b5af0d15aceeeeee5be3e30498f414acfaaa0ad688454bc12a7f2651c11af16731c897ffa2d86ed5aa5571ef396c56ac5851fd6f67517c9c00935e1880f132ea16acba0f6e0a56f933090b56dd9fc5c16dfea3581ffe50f34459bb766df5713e537d8c001d210cc07819751fcce2541edc14acf2671217ac437e1607b7d9542c0f7f9879a26cdbb6adfaf87644ff2900935f9d612d4d44c12a7f3aa0601579b0bbbbfbe5e2f6cb2fbf1cf79ec366dfbe7dd5c7b52ffe6f01a043d579f01a7a109be629c2b64a9d73d81129d115ac8d55ffb400e864f183d5b43aaf77090e67c1caefae92638e39263bfef8e3b333cf3cd315b406d20157b0867e36f38f1b8ae513f91aaca79f7ebafaf81e8dfe5300e854f51ebcea399c0567ca60c11a1818c856ae5c999d7efae9d951471d952d5ab4a8665bf94b2671c1aaf9d9ece9e9f97fc5fa89fc2dc2471f7db4fa38efab3e46003a58bd07af7a26a26015b7172e5c58b93d75ead4a1659b376fce66ce9c59b9c2153263c68ccab2627d78eae68a2baec84e39e594ec88238e38647ff1fe87bb5de4b8e38ecbdefbdef766a79d765a76ecb1c766a79e7a6ab660c1829ae39ee84cc28235eccf66befcee62bbef7ffffb71ef396cbef18d6f0c15acbcf47d393e4e0018d14416ac2d5bb6546ebffef5af1f5a16ca5558f69def7c27fbd6b7be55f93c2c2bd6cf9a35abb2ecf39fff7cb673e7ce9afd1f79e491c3de5f71fbce3befac5c41abbe1dfe3c4af8fce4934f3e649f65c8242a58c316ab427777f78ca2d85c77dd7571ef392c0e1e3c98cd9e3dbbba60fd7d7c9c0030a2892c58cf3fff7ce576b852552c0b652b2c7bf1c5172b2f728ed79f70c2099565fdfdfd75f71f9e721ceefe8adb61df2fbdf4d2d0ed703ff5b62f4b264bc16a445757d71bf252b37fb0d84cc80bdd9f7cf2c9eaa707c30bdc8f888f130046349105abde5384a905abb88215be3ebebf666f97259d54b082bc582d9dc8a7096fb9e596ea82f5d5f8f80060541351b0467a917ba34f115e7bedb5354f119e74d24995758f3cf24876d75d77d514a6666e8fb4aedeedf14ca715aceeeeee6945c1b9e8a28bb25dbb76c51d68dcac5ebdbaba5cedebeaea7a477c7c0030aa892858471f7d74e54ad5873ffce19ab769285ee41ed6870cf722f7f05aa9f845eef3e7cfcf4e3cf1c4a1fb2912df7f23b7475a57eff678a6d30ad6d4a9538fcecbcd3345d1395c7ff439bcb9e8dcb973870a565ef4ee8c8f0d001a12171c295f3aad60053db9a2e84c9f3e3debeded8dfb50cb855f76a8ba7ab5a3ababeb84f8b800a0210a56f9d389052b77c4b43fffd661a5f05c7ef9e5d90b2fbc1077a296f9c94f7e525daec20bec2f8f0f08001aa660953f1d5ab0a64c9f3efdcddddddddb8bd213de9a633c4a562857e137163d350840cb2858e54fa716ac202f3b1f9d36f8b60d21575e7965b671e3c6b8238d4978cd55f4b460c8e2f01ab0f83800a0290a56f9d3c9052be8e9e9b9a0ba645d7cf1c595b7f8d8bf7f7fdc991a167e5bb0fa05ed45b9baf0c20b5f17df3f00344dc12a7f3abd6005836fddf05275210a0529bc037fa3452bbc437b7813d1e87dae2a094f0bba720540cb2858e58f82f5677911fabb3c4fc7e528bc37dad7bef6b5cafb9f3df5d453d9d6ad5b2b852a7c5cbb766de50f377ffdeb5f3fe4cfdf54658717b403d0720a56f9a360fdc539e79c735c5e886ecb8bd19e3a65a999ec0b57adbc150300e342c12a7f14ac5a3366cc38352f49ff2bcf963ae569a46c1c2c56dea11d80f1a360953f0ad6f0ce3aebac57e4a5e97fe4a5e9d6fce32ff2accbb37bb04cfd29cf2379eecbf3953c1f8cbf1e00c6858255fe285800d06614acf247c1028036a360953f0a1600b41905abfc51b000a0cd2858e58f8205006d66c99225070706066a1ed4a51cc9cfcda6bc60ed89cf1b005062cb972fdfd4dfdf5ff3c02ee5c8b3cf3efbbdbc60ad8ccf1b0050624b972efdc4b265cb76f4f5f56d7525ab3cc9cf45dffaf5ebefcdcbd59ff29c1d9f3700a0e4c20378b84a92676f78bd8f9422e15c8473a25c0100000000000000000000000000000000000000001c0eff1f474890a9b43061420000000049454e44ae426082	t
18	1	73736d67-f472-413a-9b55-92ef702cb7fbbpmn20.xml	17	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0a0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f7267223e0a0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469506172616c6c656c47726f757052657669657722206e616d653d22506172616c6c656c2047726f75702052657669657720416e6420417070726f76652041637469766974692050726f63657373223e0a0a20202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c61637469766974693a657865637574696f6e4c697374656e6572206576656e743d2273746172742220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e6c697374656e65722e536372697074457865637574696f6e4c697374656e6572223e0a202020202020202009093c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f617070726f7665436f756e74272c2030293b0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f72656a656374436f756e74272c2030293b0a2020202020202020202020202020202020202020202020200a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f61637475616c50657263656e74272c2030293b0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c52656a65637450657263656e74272c2030293b0a2020202020202020202020202020202020202020202020200a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f726571756972656450657263656e74272c2077665f7265717569726564417070726f766550657263656e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020203c2f61637469766974693a657865637574696f6e4c697374656e65723e0a20202020202020203c2f657874656e73696f6e456c656d656e74733e0a20202020202020200a20202020202020203c73746172744576656e742069643d227374617274220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a7375626d697447726f75705265766965775461736b22202f3e0a20202020202020200a0909090a20202020202020203c73657175656e6365466c6f772069643d27666c6f773127200a202020202020202020202020736f757263655265663d277374617274270a2020202020202020202020207461726765745265663d277265766965775461736b273e0a2020202020202020202020203c212d2d20544f444f3a204f6e6365206d756c7469496e7374616e63654c6f6f7043686172616374657269737469637320737570706f7274207573696e672065787072657373696f6e206173206c6f6f7044617461496e7075745265662c2072656d6f7665202777665f67726f75704d656d6265727327207661726961626c6520202d2d3e0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0a09202020202020202020202020203c61637469766974693a657865637574696f6e4c697374656e6572206576656e743d2273746172742220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e6c697374656e65722e536372697074457865637574696f6e4c697374656e6572223e0a09202020202020202009093c61637469766974693a6669656c64206e616d653d22736372697074223e0a092020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a09202020202020202020202020202020202020202020202020766172206d656d62657273203d2070656f706c652e6765744d656d626572732862706d5f67726f757041737369676e6565293b0a2020202020202020202020202020202020202020202020202020202020202020696628776f726b666c6f772e6d617847726f7570526576696577657273203e20302026616d703b26616d703b206d656d626572732e6c656e677468203e20776f726b666c6f772e6d617847726f7570526576696577657273290a20202020202020202020202020202020202020202020202020202020202020207b0a20202020202020202020202020202020202020202020202020202020202020202020207468726f77206e6577204572726f7228224e756d626572206f6620726576696577657273206578636565647320746865206d6178696d756d3a2022202b206d656d626572732e6c656e677468202b2022286d61782069732022202b20776f726b666c6f772e6d617847726f7570526576696577657273202b20222922293b0a20202020202020202020202020202020202020202020202020202020202020207d0a09202020202020202020202020202020202020202020202020766172206d656d6265724e616d6573203d206e6577206a6176612e7574696c2e41727261794c69737428293b0a092020202020202020202020202020202020202020202020200a09202020202020202020202020202020202020202020202020666f7228766172206920696e206d656d6265727329200a2020202020202020202020202020202020202020202020202020207b0a09202020202020202020202020202020202020202020202020202020206d656d6265724e616d65732e616464286d656d626572735b695d2e70726f706572746965732e757365724e616d65293b0a092020202020202020202020202020202020202020202020207d0a09202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f67726f75704d656d62657273272c206d656d6265724e616d6573293b0a09202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f7265766965776572436f756e74272c206d656d6265724e616d65732e73697a652829293b0a092020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a092020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a092020202020202020202020203c2f61637469766974693a657865637574696f6e4c697374656e65723e0a092020202020202020203c2f657874656e73696f6e456c656d656e74733e0a20202020202020203c2f73657175656e6365466c6f773e0a0a20202020202020203c757365725461736b2069643d227265766965775461736b22206e616d653d22526576696577205461736b220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a61637469766974695265766965775461736b223e0a20202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a2020202020202020202020202020202020202020202020206966287461736b2e6765745661726961626c654c6f63616c282777665f7265766965774f7574636f6d652729203d3d2027417070726f76652729207b0a20202020202020202020202020202020202020202020202020202020766172206e6577417070726f766564436f756e74203d2077665f617070726f7665436f756e74202b20313b0a0920202020202020202020202020202020202020202020202020766172206e6577417070726f76656450657263656e74616765203d20286e6577417070726f766564436f756e74202f2077665f7265766965776572436f756e7429202a203130303b0a09202020202020202020202020202020202020202020202020200a0920202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f617070726f7665436f756e74272c206e6577417070726f766564436f756e74293b0a0920202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c50657263656e74272c206e6577417070726f76656450657263656e74616765293b0a2020202020202020202020202020202020202020202020207d20656c7365207b0a20202020202020202020202020202020202020202020202020202020766172206e657752656a656374436f756e74203d2077665f72656a656374436f756e74202b20313b0a20202020202020202020202020202020202020202020202020202020766172206e657752656a65637450657263656e74616765203d20286e657752656a656374436f756e74202f2077665f7265766965776572436f756e7429202a203130303b0a202020202020202020202020202020202020202020202020202020200a20202020202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f72656a656374436f756e74272c206e657752656a656374436f756e74293b0a20202020202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c52656a65637450657263656e74272c206e657752656a65637450657263656e74616765293b0a2020202020202020202020202020202020202020202020207d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a20202020202020202020200a20202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b72657669657741737369676e65657d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020202020200a20202020202020202020203c212d2d20466f7220656163682061737369676e65652c207461736b2069732063726561746564202d2d3e0a20202020202020202020203c6d756c7469496e7374616e63654c6f6f7043686172616374657269737469637320697353657175656e7469616c3d2266616c7365223e0a20202020202020202020200920203c6c6f6f7044617461496e7075745265663e77665f67726f75704d656d626572733c2f6c6f6f7044617461496e7075745265663e0a20202020202020202020200920203c696e707574446174614974656d206e616d653d2272657669657741737369676e656522202f3e0a20202020202020202020200920203c636f6d706c6574696f6e436f6e646974696f6e3e247b77665f61637475616c50657263656e74203e3d2077665f7265717569726564417070726f766550657263656e74207c7c2077665f7265717569726564417070726f766550657263656e74203e2028313030202d2077665f61637475616c52656a65637450657263656e74297d3c2f636f6d706c6574696f6e436f6e646974696f6e3e0a20202020202020202020203c2f6d756c7469496e7374616e63654c6f6f704368617261637465726973746963733e0a20202020202020203c2f757365725461736b3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773227200a202020202020202009736f757263655265663d277265766965775461736b270a2020202020202020202020207461726765745265663d277265766965774465636973696f6e27202f3e0a0a20202020202020203c6578636c757369766547617465776179202069643d227265766965774465636973696f6e22206e616d653d22526576696577204465636973696f6e22202f3e200a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77332720736f757263655265663d277265766965774465636973696f6e27207461726765745265663d27617070726f76656427203e0a2020202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b77665f61637475616c50657263656e74203e3d2077665f7265717569726564417070726f766550657263656e747d3c2f636f6e646974696f6e45787072657373696f6e3e0a20202020202020203c2f73657175656e6365466c6f773e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773427200a2020202020202020736f757263655265663d277265766965774465636973696f6e270a2020202020202020202020207461726765745265663d2772656a656374656427202f3e0a0a202020202020203c757365725461736b2069643d22617070726f76656422206e616d653d22446f63756d656e7420417070726f766564220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a617070726f766564506172616c6c656c5461736b22203e0a2020202020202020202020203c646f63756d656e746174696f6e3e0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e6420617070726f7665642e0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020202020200a2020202020202020202020202020202020202020202020202f2f2053657420706172616c6c656c2072657669657720706172616d73206f6e207461736b2c20746f206265206b65707420696e20686973746f72790a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f7265766965776572436f756e74272c2077665f7265766965776572436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f726571756972656450657263656e74272c2077665f726571756972656450657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c50657263656e74272c2077665f61637475616c50657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f617070726f7665436f756e74272c2077665f617070726f7665436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f72656a656374436f756e74272c2077665f72656a656374436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c52656a65637450657263656e74272c2077665f61637475616c52656a65637450657263656e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a2020202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020203c2f757365725461736b3e0a20202020202020200a20202020202020203c757365725461736b2069643d2272656a656374656422206e616d653d22446f63756d656e742052656a6563746564220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a72656a6563746564506172616c6c656c5461736b22203e0a2020202020202020202020203c646f63756d656e746174696f6e3e0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e642072656a65637465642e0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020202020200a2020202020202020202020202020202020202020202020202f2f2053657420706172616c6c656c2072657669657720706172616d73206f6e207461736b2c20746f206265206b65707420696e20686973746f72790a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f7265766965776572436f756e74272c2077665f7265766965776572436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f726571756972656450657263656e74272c2077665f726571756972656450657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c50657263656e74272c2077665f61637475616c50657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f617070726f7665436f756e74272c2077665f617070726f7665436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f72656a656374436f756e74272c2077665f72656a656374436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c52656a65637450657263656e74272c2077665f61637475616c52656a65637450657263656e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a2020202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020203c2f757365725461736b3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77352720736f757263655265663d27617070726f766564270a2020202020202020202020207461726765745265663d27656e6427202f3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77362720736f757263655265663d2772656a6563746564270a2020202020202020202020207461726765745265663d27656e6427202f3e0a0a20202020202020203c656e644576656e742069643d22656e6422202f3e0a0a202020203c2f70726f636573733e0a0a2020203c212d2d2047726170686963616c20726570726573656e7461696f6e206f66206469616772616d202d2d3e0a2020203c62706d6e64693a42504d4e4469616772616d2069643d2242504d4e4469616772616d5f6163746976697469506172616c6c656c47726f7570526576696577223e0a2020202020203c62706d6e64693a42504d4e506c616e652062706d6e456c656d656e743d226163746976697469506172616c6c656c47726f7570526576696577220a20202020202020202069643d2242504d4e506c616e655f6163746976697469506172616c6c656c47726f7570526576696577223e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227374617274220a20202020202020202020202069643d2242504d4e53686170655f7374617274223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d2233302220793d22323030223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965775461736b220a20202020202020202020202069643d2242504d4e53686170655f7265766965775461736b223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22313235220a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965774465636973696f6e220a20202020202020202020202069643d2242504d4e53686170655f7265766965774465636973696f6e223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223430222077696474683d2234302220783d223239302220793d22313937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22617070726f766564220a20202020202020202020202069643d2242504d4e53686170655f617070726f766564223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220a202020202020202020202020202020793d223937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d2272656a6563746564220a20202020202020202020202069643d2242504d4e53686170655f72656a6563746564223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220a202020202020202020202020202020793d22323937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22656e64222069643d2242504d4e53686170655f656e64223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d223535352220793d22333037223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7731222069643d2242504d4e456467655f666c6f7731223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d2236352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223132352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7732222069643d2242504d4e456467655f666c6f7732223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223233302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223239302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7733222069643d2242504d4e456467655f666c6f7733223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313937223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7734222069643d2242504d4e456467655f666c6f7734223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22323337223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7735222069643d2242504d4e456467655f666c6f7735223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22333037223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7736222069643d2242504d4e456467655f666c6f7736223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223535352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020203c2f62706d6e64693a42504d4e506c616e653e0a2020203c2f62706d6e64693a42504d4e4469616772616d3e0a3c2f646566696e6974696f6e733e	f
19	1	73736d67-f472-413a-9b55-92ef702cb7fbactivitiParallelGroupReview.png	17	\\x89504e470d0a1a0a0000000d49484452000002580000016a0806000000c63c24e70000226749444154785eeddd7d905c659d2ff0f0aae2baba252f0a889656f9c6de2a5fb66eb95a5bc4bda8c522f8d7309924f2165ea2609544222f961b5e5c14d87beb7257c03fb4345aa04617dfae9a401272a31008b23132919560082664322421e46599242439f73ced9cb1f374cf4cf73c3d99d3d39f4fd5b766fa9c33a70f7326bffe72baa767ca140000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080769765d9b1ebd6ad5bb062c58a7d4b962cc91e78e0012941f2737170f9f2e59b962e5dfa89f89c0100259797ab1fe40fe4597f7f7f36303090edddbb574a90702ec23959b66cd98ebc709d1d9f3700a0c4c295abf0401e3fc04b39d2d7d7b7352f582be3f306009458785ad095abf2269c9bbc60ed89cf1b005062e1f53ef183ba942be11cc5e70d0028b1460bd6ee17fbb2758f7d335bb3f8a64ac2e76159bc9db43e0a1600b499460ad6aeedcf65bd8bbe98adfef9dc4312968575f1f6d2da285800d0661a29581bd6fcb4a65c15d9b8e66735db4b6ba36001409b69a4603df9e0ad35c5aa4858176f2fad8d8205006da69182d5fbc00d35c5aa4858176f2fad8d8205006d46c12a7f142c0068338d14acf05b8371b12a12d6c5db4b6ba36001409b69a460ad7de8ab35c5aa4858176f2fad8d8205006da69182b5edb9df65bdf7cfab2957615958176f2fad8d8205006da6918215f2cc6fbe5d53b0c2b2783b697d142c0068330d15ac3d7bb2b50fdf5d53b0c2b2b0ae667b6969142c006833a315acf04eed4f3df46f35e5aa4858e7dddcc7370a1600b499610bd69e3d59dfdaa5d9130bafaf295571c236615b57b3c6270a1600b4997a056bb4ab56c3c5d5acf1898205006da65ec16ae4aad570095f1bef4fd2a26001409ba957b0e2d2d46ce2fd495a142c006833f50a96942b0a160094c4b469d31ecc33355e1e53b0ca1f050b004a222f57d960462c5a0a56f9a3600140495415ac118b968255fe2858005012750a56dda2a560953f0a160094449d6215a752b414acf247c102e0b0ab531ca4892858e54f7ccea4b1c4b302802618a4f5c50f365579709aa708db2aae6035cf5c00486490d6375ab12a1cce8295df5d25c71c734c76fcf1c767679e79a62b680d44c16a9eb90090c820ad6fb46255389c0567ca60c11a1818c856ae5c999d7efae9d951471d952d5ab4a8665bf94b14ace6990b00890cd2fa462b5685892858c5ed850b17566e4f9d3a7568d9e6cd9bb399336756ae7085cc9831a3b2ac58bf6ddbb6ec8a2baec84e39e594ec88238e38647ff1fe87bb5de4b8e38ecbdefbdef766a79d765a76ecb1c766a79e7a6ab660c1829ae39ee82858cd3317001219a46926b2606dd9b2a572fbf5af7ffdd0b250aec2b2ef7ce73bd9b7bef5adcae76159b17ed6ac5995659ffffce7b39d3b77d6ecffc8238f1cf6fe8adb77de7967e50a5af5edc71f7fbcf2f9c9279f7cc83ecb1005ab79e60240228334cd4416ace79f7fbe723b5ca92a9685b21596bdf8e28bd9f6eddb6bd69f70c2099565fdfdfd75f71f9e721ceefe8adb61df2fbdf4d2d0ed703ff5b62f4b14ace6990b00890cd2341359b0ea3d45985ab08a2b58e1ebe3fb6bf67659a26035cf5c00486490a699888235d28bdc1b7d8af0da6bafad798af0a4934eaaac7be49147b2bbeebaaba63035737ba475f56e8f6714ace6990b00890cd2341351b08e3efae8ca95aa0f7ff8c3356fd350bcc83dac0f19ee45eee1b552f18bdce7cf9f9f9d78e28943f75324beff466e8fb4aedeedf18c82d53c73012091419a262e3852be2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e602402283348d8255fe2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a659b264c9c18181819a07752947f273b3292f587be2f3c6c8cc05804406699ae5cb976feaefefaf79609772e4d9679ffd5e5eb056c6e78d91990b00890cd2344b972efdc4b265cb76f4f5f56d7525ab3cc9cf45dffaf5ebefcdcbd59ff29c1d9f3746662e00243248d38507f0709524cfdef07a1f2945c2b908e744b91a0373012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc05c65d9665c7ae5bb76ec18a152bf62d59b2247be08107a404c9cfc5c1e5cb976f5aba74e927e27346730c5220662e30eef272f583fc813cebefefcf060606b2bd7bf74a0912ce453827cb962ddb9117aeb3e3f346e30c5220662e30eec295abf0401e3fc04b39d2d7d7b7352f582be3f346e30c5220662e30eec2d382ae5c9537e1dce4056b4f7cde689c410ac4cc05c65d78bd4ffca02ee54a3847f179a371062910331718778d16acdd2ff665eb1efb66b666f14d9584cfc3b2783b697d14ac34062910331718778d14ac5ddb9fcb7a177d315bfdf3b987242c0bebe2eda5b551b0d218a440cc5c60dc3552b036acf9694db92ab271cdcf6ab697d646c14a72c6e0203d235e01742e058b71d748c17af2c15b6b8a5591b02ede5e5a1b056bcc42a9eacf73f5e047250ba850b018778d14acde076ea8295645c2ba787b696d14ac3129ca5551aae2db400753b0eae8eaea3aeaeebbefbef85ffff55f7ff5852f7ce1f93973e6fcd7ecd9b3f7876f56f878d55557fd57587eebadb73e74c71d775c966f7f6cbc0ffe42c12a7f14aca60d57a6865b0e741805abcaedb7dffeb737df7cf3e3b366cd3a70fdf5d7673ffce10fb3d5ab5767ebd7afcfb66ddb96052fbcf042e5f6ef7ef7bbecbefbeecbe6cd9b975d78e18561fb35d75d779da15a4723052bfcd6605cac8a8475f1f6d2da28584d19ad448db61ee8000a562e2f4927df74d34dcb2eb9e492033ff8c10f2a25aa19bb77efceeebffffeecd39ffef4813973e6f4ce9e3dfbbfc7f7d1c91a29586b1ffa6a4db12a12d6c5db4b6ba36035acd1f2d4e876c024d5f105eb965b6ef9f865975db677fefcf9d9ce9d3be3eed494975f7eb952b4f2fdedfbd4a73e75db0d37dc70647c7f9da89182b5edb9df65bdf7cfab2957615958176f2fad8d82d590664b53b3db0393484717ac9b6fbef9864b2fbdf4c0aa55abe2ae9464fbf6ede1a9c3bd975c72c9c33d3d3dc7c7f7db691a295821cffce6db35052b2c8bb793d647c11ad558cbd258bf0e68731d5bb0bef4a52fddf899cf7ce6e073cf3d17f7a3963870e04076cf3df7bc7cc10517ac9b3e7dfadfc4f7df491a2a587bf6646b1fbebba6608565615dcdf6d2d22858234a2d49a95f0fb4a18e2c58834f0b1e18af72556dfefcf9bb3ef9c94f3e72e18517be323e8e4e315ac10aefd4fed443ff5653ae8a8475decd7d7ca3600dab55e5a855fb01da44c715ac2f7ce10ba784d75cb5fa69c1e11c3c7830bbfdf6dbb74e9f3e7d417c2c9d62d882b5674fd6b77669f6c4c2eb6b4a559cb04dd8d6d5acf189825557ab4b51abf7079458c715acf0db82e105ed875378f1fba5975ebab9bbbbfbe3f1f174827a056bb4ab56c3c5d5acf1898255232e4347e4b960f06333e2af8bf70b4c521d55b0c2fb5c85b762d8b16347dc81c6ddaa55ab5eece9e979ba13df94b45ec16ae4aad570095f1bef4fd2a2601d222e41a11c7d234ff81e858f8d96ace1be2ede3f30097554c1baf1c61b1f0fef733551aeb9e69affcc4bd6e7e2e39aecea15acb834359b787f9216056b48bdf213ae4085ef4f91464a5675b92a727ed5fa7af7034c221d53b0c29fbf99356bd6fe66df44b4957a7b7bfbf26ff81fa78c3e9c27957a054bca1505ab62b8d253af2c8d54b2ea6dfff5c1e5d586bb3f6012e8988215feb660f8f3371329bce07de6cc99fdf937fd83f1f14d660a56f933590a56fe6febc13c53e3e50d18adecd42b4df54a56bdedea95abc268f70bb4a98e2958b7dd76dbaf27f2e9c1c22db7dcf248fe4dbf213ebe76d4e883998255fe4ca282950da6a19fcd418d969c7ae5a9ba64d55b3f52b92a347aff401be99882f5c52f7eb1ff70bd35c348f207b2c7babbbb7f1e1f5f3b6af4c14cc12a7f2661c16ae867734af3e5a65e890ab7c39fc58a973752ae0acd1e0750721d53b0e6cc99f35febd7af8ffbce61d7dbdbfbfb697f7e1d56db6bf4c14cc12a7f2671c11ae96773aca5a65ec9fa4374bb99725518ebf10025d43105ebb2cb2edb1ffe46e044dbb265cbe6fc9bbe233ebe7654e741acee83998255fe7440c18a7f3653cb4cbd929552ae0aa9c70594449839f1b24969c68c199537fc9c68fbf6eddb937fd3f7c5c7d78eea3c78c5a93c982958e54f9d733769d3d5d595bdf295af0c83efeaf867ba49e169c1f8ca55b81d96a708c7154ad671f10aa07d8479132f9b942eb8e0828325ba825533f42773ca58b0a60c3e20c6cbdb2dadfaefe8802b580f4e3bf429c2d42b45235dc1aaf7db858d4a3d2ea024c2ec89974d4ab367cfde5b86d760ad59b326bc06ebd1f8f8da519d07b1ba0f66632d5853aa1eb4feeaaffe2afbc0073e90fdfad7bfaed96e2c29f61b2f6f75aaff1bea25debed9b46a3f93b860c5c5aada1953c65666ea95abf84ad6584ad6588f0728a18e2958d75e7bedd6dffef6b771df39ec162f5ebc32ffa6df171f5f3b6af4c12cb56085cf7ffad39f563e7fd7bbde55b35dbba4fabfa75569d53e2761c1aafbb35847b3a5a65eb90aafb9aaf75b84cd94ac668f0328b98e2958e1fda7eebbefbeb8ef1c76b7de7aeb43f9377d5e7c7ceda8d107b35614ac5dbb76553e7fd5ab5e35b47eebd6ade18f6867279d7452f6dad7be363be79c73b275ebd6657d7d7dd971c71d97bded6d6fcbf6ecd953d9367c7ceb5bdf9abdfad5afce366fde7cc8be47da57b1fec4134fcc4e38e184cae75ffef2972b5ffb95af7ca5723b2c0f5f171f7f9cf83eebad7bcd6b5e937df0831f3ce44add82050bb2b7bffdedd931c71c53b38feadb8f3df658f6c637be313bfae8a3b33beeb8a3e63e46ca242a5823fe2c0ee38c298d959be1cad548ef83d548c96af4fe8136d24905ebea79f3e6c57de7b09b3973e686193366fcb7f8f8da51a30f66ad2858c515ac7ffaa77f1a5a7fd145175596fde8473fca1e7ef8e1cae7fff88fff585977f9e597576effe4273fa9dcfef18f7f5cb93d7bf6ec9a7d8fb6af908f7ef4a395651b376eccdef9ce77563e7fc73bde916dd8b0a1f2f9c73ef6b19ae38f13df67bd3cf1c413956ddefdee770f2d0b052e94ab5ffce21735db17fb5cb87061f6d77ffdd7d9eb5ef7baec97bffc65cd76a365b214ac04674c19b9e4d42b4ff57e5bb0de762395acd1ee1768531d53b0bababa5e7bfef9e71f0c574226cad34f3ffd5cfe0d7f2a3eb6c92eb5601579d39bde943df3cc3343eb8f3ffef89a6d5ef18a5754d6ad59b3263bf2c8232bc528dcfec8473e52b9fdfbdffffe907d37b2af90b973e75696dd78e38d958fdddddd958ffffccfff5cf978cd35d7d41c7f9cf83e8b8472f4fef7bfbf7275add8261c6bb1fee4934fae2c7bf39bdf9c5d75d555875c592bb60f052cfc37f4f6f6d6ecbf91285815674ca95f76ea95a67ae5aa506ffb7a256bb8fb0326818e2958c1b5d75efbe4fdf7df1ff79ec3e64b5ffad263f937fce6f8b826bbd482b573e7ceec339ff94ce5f3eaab4ae1ca4e5816ae2ac55f1b72f6d96767471c7144e5ea55f8189ef68bf7dde8beeeb9e79ecafaf014de29a79c92edd8b1a3f2b12845f7de7b6fcdd7c489efb348d84f58fed0430f55f61b6ff7e8a38f66e79e7beed05384ef79cf7b6af679da69a7553ede72cb2d35fb6f240ad69033a6d4969e0ba60c7e9f073352b92ad42b59e757adaf773fc024d25105ebb39ffdecd9575c71c58189783fac0d1b36bc907fb39f9f3e7dfadfc4c735d9a516acf079788b8de22a53f154d9ac59b32ab7f3f35a2926f1d70f9686ca6f20868f8b172faebbef46f615ae88155f73d34d3755961557b3428a2b632325becf2245b9fbd5af7e5579edd470db85d76585e5e17556f13ed7ae5d5bb9c2557d7ccd44c13ac419530e2d3fd565a991725518eeebe2fd0393504715ac202f587f9c88ab589ffbdce79ec8bfd957c5c7d3095a51b042427108b7ffe11ffea1727bdbb66dd995575e999d7aeaa995a7d4e2ed43def7bef75596858f23ed7bb47d8517c987ab57e169c3e22a57f8186e87e5c58be9474abccf22dffdee77b337bce10d43ebe3ed8adbe10a56f82dcaf07ab4785df8fce9a79fcedef296b7546e87a72ee3fb19290a568d33a6d496ac7005aad1725588bf2ede2f3049755cc19a3d7bf6872ebbecb27d87f34d47172f5efcc7fc1bbdb6ababebd8f8783ac1580b961cbe285875b5ba0cb57a7f4089755cc10a2ebdf4d2bbe7cd9bb7f7c0810371176ab93ffce10f5b7a7a7ab6747777ff6d7c1c9d42c12a7f14ac61b5aa14b56a3f409be8c88275d65967bd62d6ac59abefb9e79e717d31d6962d5b0666ce9cb9392f571f8f8fa193b463c1dabd7b77e537f2c2c794e5ed12056b44a9e528f5eb8136d491052be8eaea3ae1fcf3cf7f76fefcf9bb0f1e3c1877a364e1ca555eaefa7b7a7a3e1ddf77a769c78215ca527ee8356f7bd0ecf276898235aab196a4b17e1dd0e63ab6600579c97a435e82fe70fbedb76fddbf7f7fdc91c62cbce66af069c18ebe725550b0ca1f05ab21cd96a566b70726918e2e58c1b9e79efb9ae9d3a7ffe8d24b2fddbc6ad5aa17e3b2d48cf0560c83bf2db8b6935f731553b0ca1f05ab618d96a646b70326a98e2f58859e9e9e9979faafb9e69aff7ce28927fae2f23492f00eedfff22ffff29bf03e5779b19ad3a9bf2d381c05abfc51b09a325a791a6d3dd00114ac2a9ffce4275f9d17a4b9f937655d78fd54f803d1f903cf6379e1fafdf3cf3fbf2994a9f0317f105db378f1e295b7dd76dbafc3df160c57acf2dcd4896f22da0805abfc51b09a365c891a6e39d06114ac619c77de791fcabf3937e4f9bf83056a77f866e5f9539e1579fe3dcfbcc9f2879bc7938255fe2858631297a9f836d0c1142cc69d8255fe2858635694aaab073f2a57408582c5b853b0ca1f052bc919838354b9028628588c3b05abfc51b0d218a440cc5c60dc2958e58f8295c6200562e602e34ec12a7f14ac34062910331718770a56f9a360a531488198b9c0b85bb264c9c18181819a07f53267b8c2d4ecf276487e6e36e5056b4f74da6882410ac4cc05c6ddf2e5cb37f5f7f7d73cb09739bb77efae94a5f03165793be4d9679ffd5e5eb056c6e78dc619a440cc5c60dc2d5dbaf413cb962ddbd1d7d7b7b5ddae644de6e4e7a26ffdfaf5f7e6e5ea4f79ce8ecf1b8d33488198b9c061111ec0c355923c7bc3eb7da41409e7229c13e52a91410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091419a26cbb263d7ad5bb760c58a15fb962c59923df0c0035282e4e7e2e0f2e5cb372d5dbaf413f139e350f90c7830cc8151f260fc75008c40c14a9397ab1fe40fe4597f7f7f36303090edddbb574a90702ec23959b66cd98ebc709d1d9f37fe229f0153eb14aa3853e3af0360040a569a70e52a3c90c70ff0528ef4f5f56dcd0bd6caf8bc71a87085aa4ea972f50a60ac14ac34e1694157aeca9b706ef282b5273e6f1c2a5ca1aa53ac5cbd02182b052b4d78bd4ffca02ee54a3847f179a356b85255a75cb97a0530160a569a460bd6ee17fbb2758f7d335bb3f8a64ac2e76159bc9db43e0a566386b98a3535de0e80062858691a2958bbb63f97f52efa62b6fae7730f495816d6c5db4b6ba360352eba8ae5ea15c0582958691a29581bd6fcb4a65c15d9b8e66735db4b6ba360352eba8a35355e0f408314ac348d14ac271fbcb5a6581509ebe2eda5b551b09a535cc58a9703d00483344d2305abf7811b6a8a5591b02ede5e5a1b056b78679d75d62bbabbbb3fdad3d3f3bff38f0bf379f01f79b60c5ec1da982f5b957f5c947ffc3ff9c7b3cf39e79ce3e27d005087829546c12a7f14ac5a79a1fafbbc34fd30fff7bfabea29c146323058b8bc433ec04814ac348d14acf05b8371b12a12d6c5db4b6ba360fd455eac4ecfffcdffb84e711a4bc2d5ae73e3fb00608a8295aa9182b5f6a1afd614ab22615dbcbdb4360ad69fe5ffd6afcfb33f2e4a73e6ccc9eebdf7deecb7bffd6db67efdfa6cdbb66d59f0c20b2f546eaf5ebd3afbfef7bf9f5d77dd7571c12a727f5757d709f1fd017434052b4d23056bdb73bfcb7aef9f5753aec2b2b02ede5e5a9b4e2f5879f97955feeffcdeea52347dfaf4ecaebbee0a7f4aa852a61ab57dfbf64ad9bae8a28be292b5e1bcf3cefb507cdf001d4bc14ad348c10a79e637dfae29586159bc9db43e9d5cb006cbd58aea3274f3cd37671b366c88bb535376edda95dd73cf3d95a256b5ef7ddddddde7c7c700d09114ac340d15ac3d7bb2b50fdf5d53b0c2b2b0ae667b69693ab9604d8bae5ccd9f3f3f3b70e040dc97c6acb7b737bbfcf2cbab4bd6febc644d8b8f03a0e3285869462b58e19dda9f7ae8df6aca5591b0cebbb98f6f3ab560f5f4f45c575dae162d5a14f7a39608afd5bae69a6baa4bd64b79fe2e3e1e808ea260a519b660edd993f5ad5d9a3db1f0fa9a5215276c13b675356b7cd289056bf0b705875ed01eae5c8da750b2aebcf2caea92f5b4f7cc023a9a8295a65ec11aedaad57071356b7cd289056b5ad55b3184d75cb5f269c1e16cdcb831bbf8e28bab4bd6edf17101740c052b4dbd82d5c855abe112be36de9fa4a5d30a567777f707f27fd707c3bfedf022f4d417b43763e1c285d5056b60c68c19a7c6c707d01114ac34f50a565c9a9a4dbc3f494b0716acf00eed959213de8ae170dabf7f7f3677eedca192d5d3d3f33fe3e303e8080a569a7a054bca954e2a58e16f0be6a566675170366dda1477a071f7f8e38f575fc5dad2d5d5756c7c9c00939e82555ffe7d7930cfd478794cc12a7f3aa9604d9f3efd6345b9b9faeaabe3ee735884ab58975c72c950c93aefbcf3ce8c8f1360d253b0eaabfa3ff0118b968255fe4c968235dacf6290afbfa3f8d90d6f043a51bef6b5af0d15aceeeeee5be3e30498f414acfaaa0ad688454bc12a7f2651c11af16731c897ffa2d86ed5aa5571ef396c56ac5851fd6f67517c9c00935e1880f132ea16acba0f6e0a56f933090b56dd9fc5c16dfea3581ffe50f34459bb766df5713e537d8c001d210cc07819751fcce2541edc14acf2671217ac437e1607b7d9542c0f7f9879a26cdbb6adfaf87644ff2900935f9d612d4d44c12a7f3aa0601579b0bbbbfbe5e2f6cb2fbf1cf79ec366dfbe7dd5c7b52ffe6f01a043d579f01a7a109be629c2b64a9d73d81129d115ac8d55ffb400e864f183d5b43aaf77090e67c1caefae92638e39263bfef8e3b333cf3cd315b406d20157b0867e36f38f1b8ae513f91aaca79f7ebafaf81e8dfe5300e854f51ebcea399c0567ca60c11a1818c856ae5c999d7efae9d951471d952d5ab4a8665bf94b2671c1aaf9d9ece9e9f97fc5fa89fc2dc2471f7db4fa38efab3e46003a58bd07af7a26a26015b7172e5c58b93d75ead4a1659b376fce66ce9c59b9c2153263c68ccab2627d78eae68a2baec84e39e594ec88238e38647ff1fe87bb5de4b8e38ecbdefbdef766a79d765a76ecb1c766a79e7a6ab660c1829ae39ee84cc28235eccf66befcee62bbef7ffffb71ef396cbef18d6f0c15acbcf47d393e4e0018d14416ac2d5bb6546ebffef5af1f5a16ca5558f69def7c27fbd6b7be55f93c2c2bd6cf9a35abb2ecf39fff7cb673e7ce9afd1f79e491c3de5f71fbce3befac5c41abbe1dfe3c4af8fce4934f3e649f65c8242a58c316ab427777f78ca2d85c77dd7571ef392c0e1e3c98cd9e3dbbba60fd7d7c9c0030a2892c58cf3fff7ce576b852552c0b652b2c7bf1c5172b2f728ed79f70c2099565fdfdfd75f71f9e721ceefe8adb61df2fbdf4d2d0ed703ff5b62f4b264bc16a445757d71bf252b37fb0d84cc80bdd9f7cf2c9eaa707c30bdc8f888f130046349105abde5384a905abb88215be3ebebf666f97259d54b082bc582d9dc8a7096fb9e596ea82f5d5f8f80060541351b0467a917ba34f115e7bedb5354f119e74d24995758f3cf24876d75d77d514a6666e8fb4aedeedf14ca715aceeeeee6945c1b9e8a28bb25dbb76c51d68dcac5ebdbaba5cedebeaea7a477c7c0030aa892858471f7d74e54ad5873ffce19ab769285ee41ed6870cf722f7f05aa9f845eef3e7cfcf4e3cf1c4a1fb2912df7f23b7475a57eff678a6d30ad6d4a9538fcecbcd3345d1395c7ff439bcb9e8dcb973870a565ef4ee8c8f0d001a12171c295f3aad60053db9a2e84c9f3e3debeded8dfb50cb855f76a8ba7ab5a3ababeb84f8b800a0210a56f9d389052b77c4b43fffd661a5f05c7ef9e5d90b2fbc1077a296f9c94f7e525daec20bec2f8f0f08001aa660953f1d5ab0a64c9f3efdcddddddddb8bd213de9a633c4a562857e137163d350840cb2858e54fa716ac202f3b1f9d36f8b60d21575e7965b671e3c6b8238d4978cd55f4b460c8e2f01ab0f83800a0290a56f9d3c9052be8e9e9b9a0ba645d7cf1c595b7f8d8bf7f7fdc991a167e5bb0fa05ed45b9baf0c20b5f17df3f00344dc12a7f3abd6005836fddf05275210a0529bc037fa3452bbc437b7813d1e87dae2a094f0bba720540cb2858e58f82f5677911fabb3c4fc7e528bc37dad7bef6b5cafb9f3df5d453d9d6ad5b2b852a7c5cbb766de50f377ffdeb5f3fe4cfdf54658717b403d0720a56f9a360fdc539e79c735c5e886ecb8bd19e3a65a999ec0b57adbc150300e342c12a7f14ac5a3366cc38352f49ff2bcf963ae569a46c1c2c56dea11d80f1a360953f0ad6f0ce3aebac57e4a5e97fe4a5e9d6fce32ff2accbb37bb04cfd29cf2379eecbf3953c1f8cbf1e00c6858255fe285800d06614acf247c1028036a360953f0a1600b41905abfc51b000a0cd2858e58f8205006d66c99225070706066a1ed4a51cc9cfcda6bc60ed89cf1b005062cb972fdfd4dfdf5ff3c02ee5c8b3cf3efbbdbc60ad8ccf1b0050624b972efdc4b265cb76f4f5f56d7525ab3cc9cf45dffaf5ebefcdcbd59ff29c1d9f3700a0e4c20378b84a92676f78bd8f9422e15c8473a25c0100000000000000000000000000000000000000001c0eff1f474890a9b43061420000000049454e44ae426082	t
22	1	4efbadec-092a-4569-90c3-2e402e71db22bpmn20.xml	21	\\x3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220d0a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220d0a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220d0a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220d0a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f72672f776f726b666c6f77732f696e7465726e616c223e0d0a0d0a2020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469496e7669746174696f6e4d6f6465726174656422206e616d653d224d6f6465726174656420616374697669746920696e7669746174696f6e2070726f63657373223e0d0a0d0a2020202020203c73746172744576656e742069643d227374617274222061637469766974693a666f726d4b65793d22696d77663a6d6f64657261746564496e7669746174696f6e5375626d69745461736b22202f3e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77312220736f757263655265663d22737461727422207461726765745265663d2273656e64496e7669746522202f3e0d0a2020202020200d0a2020202020203c736572766963655461736b2069643d2273656e64496e7669746522206e616d653d2253656e6420496e76697465222061637469766974693a64656c656761746545787072657373696f6e3d22247b53656e644d6f64657261746564496e7669746544656c65676174657d223e3c2f736572766963655461736b3e0d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77322220736f757263655265663d2273656e64496e7669746522207461726765745265663d227265766965775461736b22202f3e0d0a0d0a2020202020203c757365725461736b2069643d227265766965775461736b22206e616d653d22526576696577205461736b220d0a20202020202020202061637469766974693a666f726d4b65793d22696d77663a61637469766974694d6f64657261746564496e7669746174696f6e5265766965775461736b223e0d0a2020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22637265617465220d0a202020202020202020202020202020636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e656427290d0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c65282762706d5f64756544617465272c2062706d5f776f726b666c6f7744756544617465293b0d0a20202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729200d0a2020202020202020202020202020202020202020202020207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c652827696d77665f7265766965774f7574636f6d65272c207461736b2e6765745661726961626c652827696d77665f7265766965774f7574636f6d652729293b0d0a202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c652827696d77665f726576696577436f6d6d656e7473272c207461736b2e6765745661726961626c65282762706d5f636f6d6d656e742729293b0d0a202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c652827696d77665f7265766965776572272c20706572736f6e2e70726f706572746965732e757365724e616d65293b0d0a2020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020203c706f74656e7469616c4f776e65723e0d0a2020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b62706d5f67726f757041737369676e65657d3c2f666f726d616c45787072657373696f6e3e0d0a2020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020203c2f706f74656e7469616c4f776e65723e0d0a2020202020203c2f757365725461736b3e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77332220736f757263655265663d227265766965775461736b22207461726765745265663d227265766965774465636973696f6e22202f3e0d0a0d0a2020202020203c6578636c7573697665476174657761792069643d227265766965774465636973696f6e22206e616d653d22526576696577204465636973696f6e22202f3e200d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77342220736f757263655265663d227265766965774465636973696f6e22207461726765745265663d22617070726f766564223e0d0a202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b696d77665f7265766965774f7574636f6d653d3d27617070726f7665277d3c2f636f6e646974696f6e45787072657373696f6e3e0d0a2020202020203c2f73657175656e6365466c6f773e0d0a2020202020200d0a2020202020203c736572766963655461736b2069643d22617070726f76656422206e616d653d22417070726f766564222061637469766974693a64656c656761746545787072657373696f6e3d22247b417070726f76654d6f64657261746564496e7669746544656c65676174657d22202f3e0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77352220736f757263655265663d22617070726f76656422207461726765745265663d22656e6422202f3e0d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77362220736f757263655265663d227265766965774465636973696f6e22207461726765745265663d2272656a656374656422202f3e0d0a0d0a2020202020203c736572766963655461736b2069643d2272656a656374656422206e616d653d2252656a6563746564222061637469766974693a64656c656761746545787072657373696f6e3d22247b52656a6563744d6f64657261746564496e7669746544656c65676174657d22202f3e0d0a2020202020203c73657175656e6365466c6f772069643d27666c6f77372720736f757263655265663d2772656a656374656427207461726765745265663d27656e6427202f3e0d0a2020202020200d0a2020202020203c656e644576656e742069643d22656e6422202f3e0d0a2020203c2f70726f636573733e0d0a0d0a3c2f646566696e6974696f6e733e	f
25	1	ca6eb1aa-229b-437b-9b33-2c9bfb08269dbpmn20.xml	24	\\x3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220d0a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220d0a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220d0a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220d0a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f72672f776f726b666c6f77732f696e7465726e616c223e0d0a0d0a2020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469496e7669746174696f6e4e6f6d696e6174656422206e616d653d224e6f6d696e6174656420616374697669746920696e7669746174696f6e2070726f63657373223e0d0a0d0a2020202020203c73746172744576656e742069643d227374617274222061637469766974693a666f726d4b65793d22696e77663a696e76697465546f536974655461736b22202f3e0d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77312220736f757263655265663d22737461727422207461726765745265663d2273656e64496e7669746522202f3e0d0a2020202020200d0a2020202020203c736572766963655461736b2069643d2273656e64496e7669746522206e616d653d2253656e6420496e76697465222061637469766974693a64656c656761746545787072657373696f6e3d22247b53656e644e6f6d696e61746564496e7669746544656c65676174657d22202f3e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77322220736f757263655265663d2273656e64496e7669746522207461726765745265663d22696e7669746550656e64696e6722202f3e0d0a0d0a2020202020203c757365725461736b2069643d22696e7669746550656e64696e6722206e616d653d22496e766974652050656e64696e67220d0a20202020202020202061637469766974693a666f726d4b65793d22696e77663a6163746976697469496e7669746550656e64696e675461736b223e0d0a2020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22637265617465220d0a202020202020202020202020202020636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e656427290d0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c65282762706d5f64756544617465272c2062706d5f776f726b666c6f7744756544617465293b0d0a20202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729200d0a2020202020202020202020202020202020202020202020207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c652827696e77665f696e766974654f7574636f6d65272c207461736b2e6765745661726961626c652827696e77665f696e766974654f7574636f6d652729293b0d0a2020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020203c68756d616e506572666f726d65723e0d0a2020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b62706d5f61737369676e65652e70726f706572746965732e757365724e616d657d3c2f666f726d616c45787072657373696f6e3e0d0a2020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020203c2f68756d616e506572666f726d65723e0d0a2020202020203c2f757365725461736b3e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77332220736f757263655265663d22696e7669746550656e64696e6722207461726765745265663d22696e766974654761746577617922202f3e0d0a0d0a2020202020203c6578636c7573697665476174657761792069643d22696e766974654761746577617922206e616d653d22496e76697465204761746577617922202f3e200d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77342220736f757263655265663d22696e766974654761746577617922207461726765745265663d226163636570746564223e0d0a202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b696e77665f696e766974654f7574636f6d65203d3d2027616363657074277d3c2f636f6e646974696f6e45787072657373696f6e3e0d0a2020202020203c2f73657175656e6365466c6f773e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77352220736f757263655265663d22696e766974654761746577617922207461726765745265663d2272656a6563746564223e0d0a202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b696e77665f696e766974654f7574636f6d65203d3d202772656a656374277d3c2f636f6e646974696f6e45787072657373696f6e3e0d0a2020202020203c2f73657175656e6365466c6f773e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77372220736f757263655265663d22696e766974654761746577617922207461726765745265663d2263616e63656c6c656422202f3e0d0a2020202020200d0a2020202020203c736572766963655461736b2069643d22616363657074656422206e616d653d224163636570746564222061637469766974693a64656c656761746545787072657373696f6e3d22247b4163636570744e6f6d696e61746564496e7669746544656c65676174657d22202f3e0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77362220736f757263655265663d22616363657074656422207461726765745265663d22696e76697465416363657074656422202f3e0d0a0d0a2020202020203c736572766963655461736b2069643d2272656a656374656422206e616d653d2252656a6563746564222061637469766974693a64656c656761746545787072657373696f6e3d22247b52656a6563744e6f6d696e61746564496e7669746544656c65676174657d22202f3e0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77382220736f757263655265663d2272656a656374656422207461726765745265663d22696e7669746552656a656374656422202f3e0d0a2020202020200d0a2020202020203c736572766963655461736b2069643d2263616e63656c6c656422206e616d653d2243616e63656c6c6564222061637469766974693a64656c656761746545787072657373696f6e3d22247b43616e63656c4e6f6d696e61746564496e7669746544656c65676174657d22202f3e0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77392220736f757263655265663d2263616e63656c6c656422207461726765745265663d22656e6422202f3e0d0a2020202020200d0a202020202020203c757365725461736b2069643d22696e76697465416363657074656422206e616d653d22496e7669746174696f6e204163636570746564220d0a20202020202020202020202061637469766974693a666f726d4b65793d22696e77663a616363657074496e766974655461736b22203e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f7731302220736f757263655265663d22696e76697465416363657074656422207461726765745265663d22656e6422202f3e0d0a20202020202020200d0a202020202020203c757365725461736b2069643d22696e7669746552656a656374656422206e616d653d22496e7669746174696f6e2052656a6563746564220d0a20202020202020202020202061637469766974693a666f726d4b65793d22696e77663a72656a656374496e766974655461736b22203e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a20202020202020200d0a20202020202020202020202020203c73657175656e6365466c6f772069643d22666c6f7731312220736f757263655265663d22696e7669746552656a656374656422207461726765745265663d22656e6422202f3e0d0a20202020202020200d0a2020202020203c656e644576656e742069643d22656e6422202f3e0d0a2020203c2f70726f636573733e0d0a0d0a3c2f646566696e6974696f6e733e	f
28	1	b18edc1f-0bed-4c2b-9f90-8001ecf0bb65bpmn20.xml	27	\\x3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f72672f776f726b666c6f77732f696e7465726e616c223e0a0a2020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469496e7669746174696f6e4e6f6d696e6174656441646444697265637422206e616d653d22416464207573657220616374697669746920696e7669746174696f6e2070726f63657373223e0a0a2020202020203c73746172744576656e742069643d227374617274222061637469766974693a666f726d4b65793d22696e77663a696e76697465546f536974655461736b22202f3e0a0a2020202020203c73657175656e6365466c6f772069643d22666c6f77312220736f757263655265663d22737461727422207461726765745265663d22616363657074656422202f3e0a2020202020200a2020202020203c736572766963655461736b2069643d22616363657074656422206e616d653d224163636570746564222061637469766974693a64656c656761746545787072657373696f6e3d22247b4163636570744e6f6d696e61746564496e7669746544656c65676174657d22202f3e0a2020202020203c73657175656e6365466c6f772069643d22666c6f77322220736f757263655265663d22616363657074656422207461726765745265663d2273656e64496e7669746522202f3e0a2020202020200a2020202020203c736572766963655461736b2069643d2273656e64496e7669746522206e616d653d2253656e6420496e76697465222061637469766974693a64656c656761746545787072657373696f6e3d22247b53656e644e6f6d696e61746564496e7669746541646444697265637444656c65676174657d22202f3e0a2020202020200a2020202020203c73657175656e6365466c6f772069643d22666c6f77332220736f757263655265663d2273656e64496e7669746522207461726765745265663d22656e6422202f3e0a0a2020202020203c656e644576656e742069643d22656e6422202f3e0a2020203c2f70726f636573733e0a0a3c2f646566696e6974696f6e733e	f
31	1	8095d7d0-f403-4576-bd4d-705085944736bpmn20.xml	30	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0a0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220a20202020202020202020202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e6365220a20202020202020202020202020786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220a20202020202020202020202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4449220a20202020202020202020202020747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220a202020202020202020202020207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f72672f776f726b666c6f77732f696e7465726e616c223e0a0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d22726573657450617373776f726422206e616d653d224f6e205072656d6973652052657365742050617373776f72642070726f63657373223e0a0a20202020202020203c73746172744576656e742069643d227374617274222061637469766974693a666f726d4b65793d22726573657470617373776f726477663a7265717565737450617373776f726452657365745461736b222f3e0a0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77312220736f757263655265663d22737461727422207461726765745265663d2273656e64526573657450617373776f7264456d61696c5461736b222f3e0a0a20202020202020203c736572766963655461736b2069643d2273656e64526573657450617373776f7264456d61696c5461736b22206e616d653d2253656e642052657365742050617373776f726420456d61696c222061637469766974693a64656c656761746545787072657373696f6e3d22247b53656e64526573657450617373776f7264456d61696c44656c65676174657d220a20202020202020202020202020202020202020202061637469766974693a666f726d4b65793d22726573657470617373776f726477663a73656e64526573657450617373776f7264456d61696c5461736b222f3e0a0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77322220736f757263655265663d2273656e64526573657450617373776f7264456d61696c5461736b22207461726765745265663d22726573657450617373776f72645461736b222f3e0a0a20202020202020203c212d2d205468652070617373776f726420726573657420686173206265656e2072657175657374656420616e64206973206e6f772077616974696e6720666f7220746865207573657220746f20636f6d706c65746520627920636c69636b696e67206f6e20746865206c696e6b20696e2074686520656d61696c2e202d2d3e0a20202020202020203c212d2d204e6f7465207468617420776520646f206e6f742073746f7265207468652070617373776f726420617320616e20657865637574696f6e207661726961626c6520666f7220736563757269747920726561736f6e732e202d2d3e0a20202020202020203c757365725461736b2069643d22726573657450617373776f72645461736b22206e616d653d2250617373776f72642052657365742050656e64696e67222061637469766974693a666f726d4b65793d22726573657470617373776f726477663a726573657450617373776f72645461736b222f3e0a0a20202020202020203c212d2d204166746572202773797374656d2e72657365742d70617373776f72642e656e6454696d657227206f662077616974696e6720666f72207573657220746f2072657365742070617373776f72642c20656e64207468652070726f63657373202d2d3e0a20202020202020203c626f756e646172794576656e742069643d22656e6450726f6365737354696d6572222063616e63656c41637469766974793d227472756522206174746163686564546f5265663d22726573657450617373776f72645461736b223e0a2020202020202020202020203c74696d65724576656e74446566696e6974696f6e3e0a202020202020202020202020202020203c74696d654475726174696f6e3e247b726573657470617373776f726477665f656e6454696d65727d3c2f74696d654475726174696f6e3e0a2020202020202020202020203c2f74696d65724576656e74446566696e6974696f6e3e0a20202020202020203c2f626f756e646172794576656e743e0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77332220736f757263655265663d22656e6450726f6365737354696d657222207461726765745265663d2265787069726564222f3e0a0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77342220736f757263655265663d22726573657450617373776f72645461736b22207461726765745265663d22706572666f726d526573657450617373776f7264222f3e0a0a20202020202020203c212d2d20546865207573657220686173207375626d697474656420746865206e6563657373617279206461746120746f207265736574207468652070617373776f72642e202d2d3e0a20202020202020203c736572766963655461736b2069643d22706572666f726d526573657450617373776f726422206e616d653d22506572666f726d2052657365742050617373776f7264222061637469766974693a64656c656761746545787072657373696f6e3d22247b506572666f726d526573657450617373776f726444656c65676174657d220a20202020202020202020202020202020202020202061637469766974693a666f726d4b65793d22726573657470617373776f726477663a70617373776f72645265736574222f3e0a0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77352220736f757263655265663d22706572666f726d526573657450617373776f726422207461726765745265663d2273656e64526573657450617373776f7264436f6e6669726d6174696f6e456d61696c5461736b222f3e0a0a20202020202020203c736572766963655461736b2069643d2273656e64526573657450617373776f7264436f6e6669726d6174696f6e456d61696c5461736b22206e616d653d2253656e642052657365742050617373776f726420436f6e6669726d6174696f6e20456d61696c222061637469766974693a64656c656761746545787072657373696f6e3d22247b53656e64526573657450617373776f7264436f6e6669726d6174696f6e456d61696c44656c65676174657d220a20202020202020202020202020202020202020202061637469766974693a666f726d4b65793d22726573657470617373776f726477663a73656e64526573657450617373776f7264436f6e6669726d6174696f6e456d61696c5461736b222f3e0a0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77362220736f757263655265663d2273656e64526573657450617373776f7264436f6e6669726d6174696f6e456d61696c5461736b22207461726765745265663d22656e64222f3e0a0a20202020202020203c656e644576656e742069643d22656e64222f3e0a20202020202020203c656e644576656e742069643d2265787069726564222f3e0a202020203c2f70726f636573733e0a0a3c2f646566696e6974696f6e733e0a	f
\.


--
-- Data for Name: act_ge_property; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_ge_property (name_, value_, rev_) FROM stdin;
schema.version	5.23.0.0	1
schema.history	create(5.23.0.0)	1
next.dbid	101	2
\.


--
-- Data for Name: act_hi_actinst; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_hi_actinst (id_, proc_def_id_, proc_inst_id_, execution_id_, act_id_, task_id_, call_proc_inst_id_, act_name_, act_type_, assignee_, start_time_, end_time_, duration_, tenant_id_) FROM stdin;
\.


--
-- Data for Name: act_hi_attachment; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_hi_attachment (id_, rev_, user_id_, name_, description_, type_, task_id_, proc_inst_id_, url_, content_id_, time_) FROM stdin;
\.


--
-- Data for Name: act_hi_comment; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_hi_comment (id_, type_, time_, user_id_, task_id_, proc_inst_id_, action_, message_, full_msg_) FROM stdin;
\.


--
-- Data for Name: act_hi_detail; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_hi_detail (id_, type_, proc_inst_id_, execution_id_, task_id_, act_inst_id_, name_, var_type_, rev_, time_, bytearray_id_, double_, long_, text_, text2_) FROM stdin;
\.


--
-- Data for Name: act_hi_identitylink; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_hi_identitylink (id_, group_id_, type_, user_id_, task_id_, proc_inst_id_) FROM stdin;
\.


--
-- Data for Name: act_hi_procinst; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_hi_procinst (id_, proc_inst_id_, business_key_, proc_def_id_, start_time_, end_time_, duration_, start_user_id_, start_act_id_, end_act_id_, super_process_instance_id_, delete_reason_, tenant_id_, name_) FROM stdin;
\.


--
-- Data for Name: act_hi_taskinst; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_hi_taskinst (id_, proc_def_id_, task_def_key_, proc_inst_id_, execution_id_, name_, parent_task_id_, description_, owner_, assignee_, start_time_, claim_time_, end_time_, duration_, delete_reason_, priority_, due_date_, form_key_, category_, tenant_id_) FROM stdin;
\.


--
-- Data for Name: act_hi_varinst; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_hi_varinst (id_, proc_inst_id_, execution_id_, task_id_, name_, var_type_, rev_, bytearray_id_, double_, long_, text_, text2_, create_time_, last_updated_time_) FROM stdin;
\.


--
-- Data for Name: act_id_group; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_id_group (id_, rev_, name_, type_) FROM stdin;
\.


--
-- Data for Name: act_id_info; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_id_info (id_, rev_, user_id_, type_, key_, value_, password_, parent_id_) FROM stdin;
\.


--
-- Data for Name: act_id_membership; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_id_membership (user_id_, group_id_) FROM stdin;
\.


--
-- Data for Name: act_id_user; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) FROM stdin;
\.


--
-- Data for Name: act_procdef_info; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_procdef_info (id_, proc_def_id_, rev_, info_json_id_) FROM stdin;
\.


--
-- Data for Name: act_re_deployment; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_re_deployment (id_, name_, category_, tenant_id_, deploy_time_) FROM stdin;
1	adhoc.bpmn20.xml	http://alfresco.org/workflows/fullAccess		2024-02-19 07:51:13.973
5	review.bpmn20.xml	http://alfresco.org/workflows/fullAccess		2024-02-19 07:51:14.238
9	review-pooled.bpmn20.xml	http://alfresco.org/workflows/fullAccess		2024-02-19 07:51:14.279
13	parallel-review.bpmn20.xml	http://alfresco.org/workflows/fullAccess		2024-02-19 07:51:14.311
17	parallel-review-group.bpmn20.xml	http://alfresco.org/workflows/fullAccess		2024-02-19 07:51:14.346
21	invitation-moderated.bpmn20.xml	http://alfresco.org/workflows/internal		2024-02-19 07:51:14.378
24	invitation-nominated.bpmn20.xml	http://alfresco.org/workflows/internal		2024-02-19 07:51:14.39
27	invitation-add-direct.bpmn20.xml	http://alfresco.org/workflows/internal		2024-02-19 07:51:14.401
30	reset-password_processdefinition.bpmn20.xml	http://alfresco.org/workflows/internal		2024-02-19 07:51:14.411
\.


--
-- Data for Name: act_re_model; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_re_model (id_, rev_, name_, key_, category_, create_time_, last_update_time_, version_, meta_info_, deployment_id_, editor_source_value_id_, editor_source_extra_value_id_, tenant_id_) FROM stdin;
\.


--
-- Data for Name: act_re_procdef; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_re_procdef (id_, rev_, category_, name_, key_, version_, deployment_id_, resource_name_, dgrm_resource_name_, description_, has_start_form_key_, has_graphical_notation_, suspension_state_, tenant_id_) FROM stdin;
\.


--
-- Data for Name: act_ru_event_subscr; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_ru_event_subscr (id_, rev_, event_type_, event_name_, execution_id_, proc_inst_id_, activity_id_, configuration_, created_, proc_def_id_, tenant_id_) FROM stdin;
\.


--
-- Data for Name: act_ru_execution; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_ru_execution (id_, rev_, proc_inst_id_, business_key_, parent_id_, proc_def_id_, super_exec_, act_id_, is_active_, is_concurrent_, is_scope_, is_event_scope_, suspension_state_, cached_ent_state_, tenant_id_, name_, lock_time_) FROM stdin;
\.


--
-- Data for Name: act_ru_identitylink; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_ru_identitylink (id_, rev_, group_id_, type_, user_id_, task_id_, proc_inst_id_, proc_def_id_) FROM stdin;
\.


--
-- Data for Name: act_ru_job; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_ru_job (id_, rev_, type_, lock_exp_time_, lock_owner_, exclusive_, execution_id_, process_instance_id_, proc_def_id_, retries_, exception_stack_id_, exception_msg_, duedate_, repeat_, handler_type_, handler_cfg_, tenant_id_) FROM stdin;
\.


--
-- Data for Name: act_ru_task; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_ru_task (id_, rev_, execution_id_, proc_inst_id_, proc_def_id_, name_, parent_task_id_, description_, task_def_key_, owner_, assignee_, delegation_, priority_, create_time_, due_date_, category_, suspension_state_, tenant_id_, form_key_) FROM stdin;
\.


--
-- Data for Name: act_ru_variable; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_ru_variable (id_, rev_, type_, name_, execution_id_, proc_inst_id_, task_id_, bytearray_id_, double_, long_, text_, text2_) FROM stdin;
\.


--
-- Data for Name: alf_access_control_entry; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_access_control_entry (id, version, permission_id, authority_id, allowed, applies, context_id) FROM stdin;
1	0	1	1	t	0	\N
2	0	2	1	t	0	\N
3	0	2	2	t	0	\N
4	0	3	1	t	0	\N
5	0	4	1	t	0	\N
6	0	4	2	t	0	\N
7	0	1	3	t	0	\N
8	0	1	4	t	0	\N
9	0	5	1	t	0	\N
10	0	5	5	t	0	\N
11	0	6	1	t	0	\N
12	0	7	4	t	0	\N
13	0	8	6	t	0	\N
14	0	9	7	t	0	\N
15	0	10	8	t	0	\N
16	0	11	9	t	0	\N
17	0	11	1	t	0	\N
18	0	12	1	t	0	\N
19	0	1	10	t	0	\N
20	0	1	11	t	0	\N
\.


--
-- Data for Name: alf_access_control_list; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_access_control_list (id, version, acl_id, latest, acl_version, inherits, inherits_from, type, inherited_acl, is_versioned, requires_version, acl_change_set) FROM stdin;
6	3	c27a5b3b-4019-4647-9caa-ee57036ec357	t	1	t	\N	1	60	f	f	7
1	2	e25d1caa-e705-454a-a7cd-2ff3f327cc81	t	1	t	\N	1	2	f	f	1
2	3	00dc3bea-c149-4360-bde8-7e7480a442f7	t	1	t	1	2	2	f	f	1
4	3	e9e87569-3f72-4d48-90b1-e5709d54642e	t	1	t	3	2	4	f	f	8
3	2	350245a8-479f-4f70-8955-f01f46a522af	t	1	t	\N	1	4	f	f	2
5	2	ba25eda7-84db-44fa-90b1-bedcfc9e9ad6	t	1	t	\N	1	\N	f	f	3
7	1	1921207d-d975-4a19-b5fa-377e37dcf181	t	1	t	\N	1	\N	f	f	5
10	7	0005681a-d96f-4239-93cb-bbced7631c16	t	1	f	9	1	11	f	f	6
11	5	0e943d8e-eba4-43f5-b6a9-a740b5038e57	t	1	t	10	2	11	f	f	6
12	7	e52ddabc-4c8a-4181-be03-5b930a73a1c2	t	1	f	11	1	13	f	f	6
13	5	a9a605d2-6a95-4e8b-958e-0dcb362b88e3	t	1	t	12	2	13	f	f	6
14	4	efa9363d-9a64-4bf5-a129-e17733d647ec	t	1	t	13	1	15	f	f	6
15	3	1c005b30-1ca8-4d4e-8dfa-91cb827da75c	t	1	t	14	2	\N	f	f	6
16	7	ba7366c8-ac45-4ea4-8b83-66e269860d8e	t	1	f	13	1	17	f	f	6
17	4	9dc34566-94c4-4989-a82a-ae08c1e1a010	t	1	t	16	2	\N	f	f	6
18	7	131ac146-18b4-4ceb-8f1f-5050e3c19096	t	1	f	13	1	19	f	f	6
19	4	ae9a455e-0b48-4e7a-b12d-9e71cd1e8121	t	1	t	18	2	\N	f	f	6
20	7	9cb806c7-4d93-43a1-a1ab-72cd7b82ceb2	t	1	f	13	1	21	f	f	6
21	4	9b57eccb-0add-4cf7-898a-eae41943f3a8	t	1	t	20	2	\N	f	f	6
22	8	4b6cefac-4386-43d5-8682-0d0a4fb0fca5	t	1	f	11	1	23	f	f	6
23	5	b69c7c89-0aff-4580-b7dc-9423570cc403	t	1	t	22	2	\N	f	f	6
24	7	2aca3cd8-677e-400b-a44f-1e08de587821	t	1	f	11	1	25	f	f	6
25	4	a1eafe52-ebcb-4e13-8450-0908773f1797	t	1	t	24	2	\N	f	f	6
8	4	98a25ecf-9f52-4741-a2b6-74e237434123	t	1	t	\N	1	9	f	f	6
9	5	a4ec95d8-5e34-4d02-a425-56e360fa929d	t	1	t	8	2	9	f	f	6
26	7	413a653c-2299-4790-a09c-e042d4832afa	t	1	f	9	1	27	f	f	6
27	5	79967e5c-efe4-4836-ae41-179923acbf97	t	1	t	26	2	27	f	f	6
28	5	a3d2a9c6-a056-4cfe-998d-80a8027b24d0	t	1	t	27	1	29	f	f	6
29	4	33582505-1417-4090-b5c0-953f523abdc4	t	1	t	28	2	\N	f	f	6
30	4	0f540d0a-cce1-4df3-bc37-078256da091c	t	1	t	27	1	31	f	f	6
31	3	c7a3ef7d-1c1c-4251-a4cf-dbea4d4ef2e8	t	1	t	30	2	\N	f	f	6
32	4	eb50a83a-514f-448e-8489-4da128d6b4bf	t	1	t	9	1	33	f	f	6
33	3	9511bee5-d454-4bcf-9322-d0597d507881	t	1	t	32	2	\N	f	f	6
34	7	7a5a2385-d527-4ce0-860f-5fa8003a120b	t	1	f	9	1	35	f	f	6
35	4	bac082bf-3e9c-4757-9990-5428ca0c4840	t	1	t	34	2	\N	f	f	6
36	7	0cc4da6d-233d-4342-b92b-d24de8689dec	t	1	f	9	1	37	f	f	6
37	4	c3d72646-2ee4-4426-b6ef-9a2f7d7434bd	t	1	t	36	2	\N	f	f	6
38	4	a7f6b02c-a2fc-4b55-bef1-ae5ad7a0729b	t	1	t	11	1	39	f	f	6
39	3	51300128-56b9-46dd-995d-d354074e04a4	t	1	t	38	2	\N	f	f	6
40	4	76617ba6-d6a9-4bf7-8079-6a9aae94fab0	t	1	t	11	1	41	f	f	6
41	3	1e5e3977-830d-48f4-8b46-64f0a621b125	t	1	t	40	2	\N	f	f	6
42	4	e321fcb8-d4d8-40c9-973f-68d700c9695b	t	1	t	27	1	43	f	f	6
43	3	f75b0ebf-3aac-4cd7-994a-3dcb903d5876	t	1	t	42	2	\N	f	f	6
44	4	6546eb82-fe93-4198-8b5a-7a190c4e7bb3	t	1	t	27	1	45	f	f	6
45	3	b780da1a-0d83-4b23-b51c-5691b0cebf41	t	1	t	44	2	\N	f	f	6
46	9	036360ac-9984-4fcd-af2b-385cc9f1653e	t	1	f	27	1	47	f	f	6
47	6	701eeb69-91ef-4056-ae6b-3812aea90195	t	1	t	46	2	\N	f	f	6
54	5	038a1adb-2a03-453a-b507-f22f3f4aa730	t	1	t	27	1	55	f	f	7
48	9	53f47295-81e8-4796-9291-691d4511240b	t	1	f	27	1	49	f	f	6
49	6	e2c11289-185f-4774-adf8-2ee27aa84d22	t	1	t	48	2	\N	f	f	6
55	5	dd13d3bb-f5d1-447d-8d51-e58e10e02038	t	1	t	54	2	55	f	f	7
50	8	1c1772e7-0482-4ad7-ba62-dd9b06ac6b64	t	1	f	27	1	51	f	f	6
51	5	3a2708e4-e6c2-4649-9eac-2f8de2b571a4	t	1	t	50	2	\N	f	f	6
53	9	0f9836cf-f2f0-413c-87a5-7e94272644a2	t	1	t	52	2	\N	f	f	7
56	5	e75267ad-c3c8-4c57-8989-15d17c57a83b	t	1	t	27	1	57	f	f	7
57	5	2b2de942-5b54-4759-90c9-e57950a54870	t	1	t	56	2	57	f	f	7
58	12	9494b3a3-0ccb-4863-bdde-37df37d06427	t	1	f	11	1	59	f	f	7
59	10	5d9d8ac4-a47b-4b5c-a7c2-70186e46bdde	t	1	t	58	2	59	f	f	7
60	3	93edf65b-eb18-4bb7-9147-bc2f5857edc6	t	1	t	6	2	60	f	f	7
\.


--
-- Data for Name: alf_ace_context; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_ace_context (id, version, class_context, property_context, kvp_context) FROM stdin;
\.


--
-- Data for Name: alf_acl_change_set; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_acl_change_set (id, commit_time_ms) FROM stdin;
1	1708329071385
2	1708329071433
3	1708329071452
4	1708329071465
5	1708329071471
6	1708329073751
7	1708329088537
8	1708329088650
\.


--
-- Data for Name: alf_acl_member; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_acl_member (id, version, acl_id, ace_id, pos) FROM stdin;
1	0	5	1	0
2	0	6	1	0
3	0	10	2	0
4	0	11	2	1
7	0	12	2	0
8	0	13	2	1
9	0	14	2	2
10	0	15	2	3
11	0	14	3	0
12	0	15	3	1
15	0	16	4	0
16	0	17	4	1
19	0	18	4	0
20	0	19	4	1
23	0	20	4	0
24	0	21	4	1
27	0	22	3	0
28	0	23	3	1
29	0	22	2	0
30	0	23	2	1
33	0	24	4	0
34	0	25	4	1
35	0	8	5	0
36	0	9	5	1
37	0	8	6	0
38	0	9	6	1
43	0	26	5	0
44	0	27	5	1
45	0	28	5	2
46	0	29	5	3
47	0	28	7	0
48	0	29	7	1
49	0	28	8	0
50	0	29	8	1
51	0	30	5	2
52	0	31	5	3
53	0	30	6	0
54	0	31	6	1
55	0	32	5	2
56	0	32	6	2
57	0	33	5	3
58	0	33	6	3
59	0	32	4	0
60	0	33	4	1
65	0	34	2	0
66	0	35	2	1
71	0	36	9	0
72	0	37	9	1
73	0	38	2	2
74	0	39	2	3
75	0	38	10	0
76	0	39	10	1
77	0	40	2	2
78	0	41	2	3
79	0	40	10	0
80	0	41	10	1
81	0	42	5	2
82	0	43	5	3
83	0	42	5	0
84	0	43	5	1
85	0	44	5	2
86	0	45	5	3
87	0	44	5	0
88	0	45	5	1
91	0	46	5	0
92	0	47	5	1
93	0	46	11	0
94	0	47	11	1
95	0	46	12	0
96	0	47	12	1
99	0	48	5	0
100	0	49	5	1
101	0	48	11	0
102	0	49	11	1
103	0	48	12	0
104	0	49	12	1
107	0	50	11	0
108	0	51	11	1
109	0	50	12	0
110	0	51	12	1
114	0	53	13	1
116	0	53	14	1
118	0	53	15	1
120	0	53	16	1
122	0	53	17	1
124	0	53	18	1
125	0	54	5	2
126	0	55	5	3
127	0	54	19	0
128	0	55	19	1
129	0	54	8	0
130	0	55	8	1
131	0	56	5	2
132	0	57	5	3
133	0	56	20	0
134	0	57	20	1
135	0	56	8	0
136	0	57	8	1
139	0	58	18	0
140	0	59	18	1
141	0	58	15	0
142	0	59	15	1
143	0	58	16	0
144	0	59	16	1
145	0	58	13	0
146	0	59	13	1
147	0	58	17	0
148	0	59	17	1
149	0	58	14	0
150	0	59	14	1
151	0	60	1	1
\.


--
-- Data for Name: alf_activity_feed; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_activity_feed (id, post_id, post_date, activity_summary, feed_user_id, activity_type, site_network, app_tool, post_user_id, feed_date) FROM stdin;
\.


--
-- Data for Name: alf_activity_feed_control; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_activity_feed_control (id, feed_user_id, site_network, app_tool, last_modified) FROM stdin;
\.


--
-- Data for Name: alf_activity_post; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_activity_post (sequence_id, post_date, status, activity_data, post_user_id, job_task_node, site_network, app_tool, activity_type, last_modified) FROM stdin;
\.


--
-- Data for Name: alf_applied_patch; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_applied_patch (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report) FROM stdin;
patch.db-V5.0-upgrade-to-activiti-5.16.2	patch.db-V5.0-upgrade-to-activiti-5.16.2.description	0	8003	-1	8004	2024-02-19 07:51:10.103	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V5.0-upgrade-to-activiti-5.16.4	patch.db-V5.0-upgrade-to-activiti-5.16.4.description	0	8008	-1	8009	2024-02-19 07:51:10.115	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V5.0-remove-columns-after-upgrade-to-activiti-5.16.2	patch.db-V5.0-remove-columns-after-upgrade-to-activiti-5.16.2.description	0	9002	-1	9003	2024-02-19 07:51:10.115	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V5.0-update-activiti-default-timestamp-column	patch.db-V5.0-update-activiti-default-timestamp-column.description	0	9012	-1	9013	2024-02-19 07:51:10.116	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V5.0-activiti-correct-tenant-id-MSSQL	patch.db-V5.0-activiti-correct-tenant-id-MSSQL.description	0	9016	-1	9017	2024-02-19 07:51:10.116	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V5.1-upgrade-to-activiti-5.19.0	patch.db-V5.1-upgrade-to-activiti-5.19.0	0	9013	-1	9014	2024-02-19 07:51:10.117	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V3.4-UsageTables	Manually executed script upgrade V3.4: Usage Tables	0	113	-1	114	2024-02-19 07:51:27.123	UNKNOWN	t	t	Script completed
patch.db-V4.0-TenantTables	Manually executed script upgrade V4.0: Tenant Tables	0	6004	-1	6005	2024-02-19 07:51:27.124	UNKNOWN	t	t	Script completed
patch.db-V4.1-AuthorizationTables	Manually executed script upgrade V4.1: Authorization status tables	0	6075	-1	6076	2024-02-19 07:51:27.124	UNKNOWN	t	t	Script completed
patch.db-V5.0-ContentUrlEncryptionTables	Manually executed script upgrade V5.0: Content Url Encryption Tables	0	8001	-1	8002	2024-02-19 07:51:27.124	UNKNOWN	t	t	Script completed
patch.savedSearchesFolder	Ensures the existence of the 'Saved Searches' folder.	0	1	19100	2	2024-02-19 07:51:27.127	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updatePermissionData	Update permissions from 'folder' to 'cmobject' [JIRA: AR-344].	0	2	19100	3	2024-02-19 07:51:27.128	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.guestUser	Add the guest user, guest home space; and fix permissions on company home, guest home and guest person. 	0	2	19100	3	2024-02-19 07:51:27.131	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.fixNodeSerializableValues	Ensure that property values are not stored as Serializable if at all possible	0	3	19100	4	2024-02-19 07:51:27.132	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.savedSearchesPermission	Sets required permissions on 'Saved Searches' folder.	0	4	19100	5	2024-02-19 07:51:27.132	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateGuestPermission	Rename guest permission from 'Guest' to 'Consumer'	0	5	19100	6	2024-02-19 07:51:27.133	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.guestPersonPermission	Change Guest Person permission from 'Consumer' to 'Read'	0	5	19100	6	2024-02-19 07:51:27.135	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.spacesRootPermission	Change Spaces store root permission from 'Consumer' to 'Read'	0	5	19100	6	2024-02-19 07:51:27.135	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.categoryRootPermission	Sets required permissions on 'Category Root' folder.	0	5	19100	6	2024-02-19 07:51:27.136	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.contentPermission	Update permission entries from 'cm:content' to 'sys:base'.	0	6	19100	7	2024-02-19 07:51:27.137	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.descriptorUpdate	Update Repository descriptor	0	11	19100	12	2024-02-19 07:51:27.138	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.forumsIcons	Updates forums icon references	0	12	19100	13	2024-02-19 07:51:27.138	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.emailTemplatesFolder	Ensures the existence of the 'Email Templates' folder.	0	12	19100	13	2024-02-19 07:51:27.139	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.emailTemplatesContent	Loads the email templates into the Email Templates folder.	0	12	19100	13	2024-02-19 07:51:27.14	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.scriptsFolder	Ensures the existence of the 'Scripts' folder.	0	12	19100	13	2024-02-19 07:51:27.141	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.topLevelGroupParentChildAssociationTypePatch	Ensure top level groups have the correct child association type.	0	13	19100	14	2024-02-19 07:51:27.141	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.actionRuleDecouplingPatch	Migrate existing rules to the updated model where rules are decoupled from actions.	0	14	19100	15	2024-02-19 07:51:27.142	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.systemWorkflowFolderPatch	Ensures the existence of the system workflow container.	0	15	19100	16	2024-02-19 07:51:27.143	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.rssFolder	Ensures the existence of the 'RSS Templates' folder.	0	16	19100	17	2024-02-19 07:51:27.144	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.uifacetsTemplates	Removes the incorrectly applied uifacets aspect from presentation template files.	0	17	19100	18	2024-02-19 07:51:27.145	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.guestPersonPermission2	Change Guest Person permission to visible by all users as 'Consumer'.	0	18	19100	19	2024-02-19 07:51:27.145	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.schemaUpdateScript-V1.4-1	Ensures that the database upgrade script has been run.	0	19	19100	20	2024-02-19 07:51:27.146	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.uniqueChildName	Checks and renames duplicate children.	0	19	19100	20	2024-02-19 07:51:27.147	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.schemaUpdateScript-V1.4-2	Ensures that the database upgrade script has been run.	0	20	19100	21	2024-02-19 07:51:27.148	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.InvalidNameEnding	Fixes names ending with a space or full stop.	0	21	19100	22	2024-02-19 07:51:27.149	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.systemDescriptorContent	Adds the version properties content to the system descriptor.	0	22	19100	23	2024-02-19 07:51:27.149	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.multilingualBootstrap	Bootstraps the node that will hold the multilingual containers.	0	29	19100	30	2024-02-19 07:51:27.15	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.LinkNodeFileExtension	Fixes link node file extensions to have a .url extension.	0	33	19100	34	2024-02-19 07:51:27.151	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.systemRegistryBootstrap	Bootstraps the node that will hold system registry metadata.	0	34	19100	35	2024-02-19 07:51:27.152	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.userAndPersonUserNamesAsIdentifiers	Reindex usr:user and cm:person uids as identifiers	0	35	19100	36	2024-02-19 07:51:27.153	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.contentFormFolderType	Update WCM Content Form folder type.	0	36	19100	37	2024-02-19 07:51:27.153	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.versionHistoryPerformance	Improves the performance of version history lookups.	0	38	19100	39	2024-02-19 07:51:27.154	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.webscripts	Adds Web Scripts to Data Dictionary.	0	50	19100	51	2024-02-19 07:51:27.154	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.1-JBPMUpdate	Ensures that the database upgrade script has been run.	0	51	19100	52	2024-02-19 07:51:27.155	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.1-NotNullColumns	Ensures that the database upgrade script has been run.	0	51	19100	52	2024-02-19 07:51:27.155	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.groupNamesAsIdentifiers	Reindex usr:authorityContainer gids as identifiers	0	51	19100	52	2024-02-19 07:51:27.156	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.invalidUserPersonAndGroup	Fix up invalid uids for people and users; and invalid gids for groups	0	51	19100	52	2024-02-19 07:51:27.156	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.webscriptsExtension	Adds Web Scripts Extension to Data Dictionary.	0	54	19100	55	2024-02-19 07:51:27.157	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.groupMembersAsIdentifiers	Reindex usr:authorityContainer members as identifiers	0	56	19100	57	2024-02-19 07:51:27.157	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.redeploySubmitProcess	Re-deploy WCM Submit Process Definition.	0	57	19100	58	2024-02-19 07:51:27.158	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.ReadmeTemplate	Deployed ReadMe Template	0	59	19100	60	2024-02-19 07:51:27.159	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.webScriptsReadme	Applied ReadMe template to Web Scripts folders	0	59	19100	60	2024-02-19 07:51:27.159	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.1-JBPMProcessKey	Ensures that the database upgrade script has been run.	0	62	19100	63	2024-02-19 07:51:27.16	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.1-VersionColumns2	Ensures that the database upgrade script has been run.	0	63	19100	64	2024-02-19 07:51:27.16	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.2-CleanNodeStatuses	Ensures that the database upgrade script has been run.	0	89	19100	90	2024-02-19 07:51:27.161	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.webscripts2	Adds Web Scripts (second set) to Data Dictionary.	0	100	19100	101	2024-02-19 07:51:27.161	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.customModels	Adds 'Models' folder to Data Dictionary	0	101	19100	102	2024-02-19 07:51:27.162	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.customMessages	Adds 'Messages' folder to Data Dictionary	0	101	19100	102	2024-02-19 07:51:27.162	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.customWebClientExtension	Adds 'Web Client Extension' folder to Data Dictionary	0	101	19100	102	2024-02-19 07:51:27.163	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.webscripts3	Update Web Scripts ReadMe.	0	104	19100	105	2024-02-19 07:51:27.164	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.customWorkflowDefs	Adds 'Workflow Definitions' folder to Data Dictionary.	0	105	19100	106	2024-02-19 07:51:27.164	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V1.4-TxnCommitTimeIndex	Ensures that the database upgrade script has been run.	0	110	19100	111	2024-02-19 07:51:27.165	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.formsFolder	Adds 'Forms' folder to Data Dictionary.	0	112	19100	113	2024-02-19 07:51:27.165	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.tagRootCategory	Adds 'Tags' as new top-level category root.	0	113	19100	114	2024-02-19 07:51:27.166	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.2-ACL-From-2.1-A	Ensures that the database upgrade script has been run.	0	119	19100	120	2024-02-19 07:51:27.166	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.2-ACL	Ensures that the database upgrade script has been run.	0	119	19100	120	2024-02-19 07:51:27.167	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.2-0-CreateMissingTables	Ensures that the database upgrade script has been run.	0	120	19100	121	2024-02-19 07:51:27.167	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.2-2-MoveQNames	A placeholder patch; usually marks a superceded patch.	0	120	19100	121	2024-02-19 07:51:27.168	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.0-ContentUrls	Ensures that the database upgrade script has been run.	0	123	19100	124	2024-02-19 07:51:27.168	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateDmPermissions	Update ACLs on all DM node objects to the new 3.0 permission model	0	124	19100	125	2024-02-19 07:51:27.169	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.0-0-CreateActivitiesExtras	Replaced by 'patch.db-V3.0-ActivityTables', which must run first.	0	125	19100	126	2024-02-19 07:51:27.169	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.0-ActivityTables	Ensures that the database upgrade script has been run.	0	125	19100	126	2024-02-19 07:51:27.17	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.createSiteStore	A placeholder patch; usually marks a superceded patch.	0	126	19100	127	2024-02-19 07:51:27.17	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.sitesFolder	Adds 'Sites' folder to Company Home.	0	127	19100	128	2024-02-19 07:51:27.171	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.sitePermissionRefactorPatch	Create permission groups for sites.	0	128	19100	129	2024-02-19 07:51:27.171	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.migrateVersionStore	Version Store migration (from lightWeightVersionStore to version2Store)	0	129	19100	130	2024-02-19 07:51:27.172	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.inviteEmailTemplate	Adds invite email template to invite space	0	130	19100	131	2024-02-19 07:51:27.173	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.calendarNamespaceUri	Update the Calendar model namespace URI and reindex all calendar objects.	0	131	19100	132	2024-02-19 07:51:27.173	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.1-AuditPathIndex	Ensures that the database upgrade script has been run.	0	132	19100	133	2024-02-19 07:51:27.174	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.spacesStoreGuestPermission	Sets READ permissions for GUEST on root node of the SpacesStore.	0	133	19100	134	2024-02-19 07:51:27.174	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.2-Upgrade-From-2.1	Ensures that the database upgrade script has been run.	0	120	19100	135	2024-02-19 07:51:27.175	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.2-Upgrade-From-2.2SP1	Ensures that the database upgrade script has been run.	0	134	19100	135	2024-02-19 07:51:27.175	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.webSiteAddModerated	Changing Web Site visibility from a boolean to enum.	0	2006	19100	2007	2024-02-19 07:51:27.176	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.invitationMigration	Migrate invitations from old invite service to invitation service	0	2006	19100	2007	2024-02-19 07:51:27.176	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.mtShareExistingTenants	Update existing tenants for MT Share.	0	2008	19100	2009	2024-02-19 07:51:27.177	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.redeployInvitationProcess	Re-deploy Invitation Process Definitions.	0	2009	19100	2010	2024-02-19 07:51:27.177	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.2-LockTables	Ensures that the database upgrade script has been run.	0	2010	19100	2011	2024-02-19 07:51:27.178	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.zonedAuthorities	Adds the remodelled cm:authority container to the spaces store	0	2011	19100	2012	2024-02-19 07:51:27.178	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.authorityMigration	Copies any old authorities from the user store to the spaces store.	0	2012	19100	2013	2024-02-19 07:51:27.179	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.authorityDefaultZonesPatch	Adds groups and people to the appropriate zones for share and everything else.	0	2013	19100	2014	2024-02-19 07:51:27.179	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.2-ContentTables	Ensures that the database upgrade script has been run.	0	2015	19100	2016	2024-02-19 07:51:27.18	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.4-Upgrade-JBPM	Ensures that the database upgrade script has been run.	0	2017	19100	2018	2024-02-19 07:51:27.18	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.2-Upgrade-JBPM	A placeholder patch; usually marks a superceded patch.	0	2017	19100	2018	2024-02-19 07:51:27.181	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.imapFolders	Creates folders tree necessary for IMAP functionality	0	2018	19100	2019	2024-02-19 07:51:27.181	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.2-PropertyValueTables	Ensures that the database upgrade script has been run.	0	3000	19100	3001	2024-02-19 07:51:27.182	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.2-AuditTables	Ensures that the database upgrade script has been run.	0	3001	19100	3002	2024-02-19 07:51:27.182	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V2.2-Person-3	Ensures that the database upgrade script has been run.	0	3002	19100	3003	2024-02-19 07:51:27.183	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.1-Allow-IPv6	Ensures that the database upgrade script has been run.	0	3003	19100	3004	2024-02-19 07:51:27.184	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.personUsagePatch	Add person 'cm:sizeCurrent' property (if missing).	0	3004	19100	3005	2024-02-19 07:51:27.184	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.2-Child-Assoc-QName-CRC	Ensures that the database upgrade script has been run.	0	3005	19100	3006	2024-02-19 07:51:27.185	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.fixNameCrcValues-2	Fixes name and qname CRC32 values to match UTF-8 encoding.	0	3006	19100	3007	2024-02-19 07:51:27.186	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.redeployNominatedInvitationProcessWithPropsForShare	Redeploy nominated invitation workflow	0	4000	19100	4001	2024-02-19 07:51:27.187	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.2-ContentTables2	Ensures that the database upgrade script has been run.	0	4001	19100	4002	2024-02-19 07:51:27.187	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.3-Remove-VersionCount	Ensures that the database upgrade script has been run.	0	4002	19100	4003	2024-02-19 07:51:27.188	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.rendition.rendering_actions	Creates the Rendering Actions folder.	0	4003	19100	4004	2024-02-19 07:51:27.188	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.thumbnailsAssocQName	Update the 'cm:thumbnails' association QName to 'rn:rendition'.	0	4004	19100	4005	2024-02-19 07:51:27.189	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.emailInviteAndNotifyTemplatesFolder	Ensures the existence of the 'Email Invite Templates' and 'Email Notify Templates' folders.	0	4006	19100	4007	2024-02-19 07:51:27.189	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.convertContentUrls	Converts pre-3.2 content URLs to use the alf_content_data table.  The conversion work can also be done on a schedule; please contact Alfresco Support for further details.	0	4007	19100	4008	2024-02-19 07:51:27.19	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.transferServiceFolder	Add transfer definitions folder to data dictionary.	0	4008	19100	4009	2024-02-19 07:51:27.19	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.4-authority-unique-idx	Ensures that the database upgrade script has been run.	0	4099	19100	4100	2024-02-19 07:51:27.191	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.fixAuthoritiesCrcValues	Fixes authority CRC32 values to match UTF-8 encoding.	0	4100	19100	4101	2024-02-19 07:51:27.192	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateMimetypes1	Fix mimetypes for Excel and Powerpoint.	0	4101	19100	4102	2024-02-19 07:51:27.193	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.3-modify-index-permission_id	Ensures that the database upgrade script has been run.	0	4102	19100	4103	2024-02-19 07:51:27.193	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.2-AddFKIndexes	Ensures that the database upgrade script has been run.	3007	4103	19100	4104	2024-02-19 07:51:27.194	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.3-Fix-Repo-Seqs	Ensures that the database upgrade script has been run.	0	4104	19100	4105	2024-02-19 07:51:27.195	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.4-property-unique-ctx-value	Ensures that the database upgrade script has been run.	0	4104	19100	4105	2024-02-19 07:51:27.195	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.4-property-unique-ctx-idx	Ensures that the database upgrade script has been run.	0	4104	19100	4105	2024-02-19 07:51:27.196	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.4-child-assoc-indexes	Ensures that the database upgrade script has been run.	0	4104	19100	4105	2024-02-19 07:51:27.196	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.3-JBPM-Extra	Ensures that the database upgrade script has been run.	0	4105	19100	4106	2024-02-19 07:51:27.197	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.3-Node-Prop-Serializable	Ensures that the database upgrade script has been run.	0	4105	19100	4106	2024-02-19 07:51:27.197	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.migrateAttrTenants	Migrate old Tenant attributes	0	4105	19100	4106	2024-02-19 07:51:27.198	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.migrateAttrPropBackedBeans	Migrate old Property-Backed Bean component attributes	0	4106	19100	4107	2024-02-19 07:51:27.199	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.migrateAttrChainingURS	Migrate old Chaining User Registry Synchronizer attributes	0	4106	19100	4107	2024-02-19 07:51:27.2	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.migrateAttrDelete	A placeholder patch; usually marks a superceded patch.	0	4106	19100	4107	2024-02-19 07:51:27.2	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.replication.replication_actions	Creates the Replication Actions folder.	0	4107	19100	4108	2024-02-19 07:51:27.201	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.transfer.targetrulefolder	Creates the transfer target rule folder for the default transfer group.	0	4108	19100	4109	2024-02-19 07:51:27.201	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.transfer.targetrule	Creates the transfer target rule for the default transfer group.	0	4108	19100	4109	2024-02-19 07:51:27.202	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.actions.scheduledfolder	Creates the scheduled actions folder in the Data Dictionary.	0	4109	19100	4110	2024-02-19 07:51:27.203	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateMimetypes2	Fix mimetypes for Excel and Powerpoint.	0	4110	19100	4111	2024-02-19 07:51:27.204	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.2-AddFKIndexes-2	Ensures that the database upgrade script has been run.	0	4111	19100	4112	2024-02-19 07:51:27.204	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.redeployJbpmAdhocWorkflow	Redeploy JBPM adhoc workflow	0	4204	19100	4205	2024-02-19 07:51:27.205	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.imapSpacesLocaleTemplates	A placeholder patch; usually marks a superceded patch.	0	4302	19100	4305	2024-02-19 07:51:27.205	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.mtFixAdminExistingTenants	Fix bootstrapped creator/modifier	0	5002	19100	5003	2024-02-19 07:51:27.206	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.fixUserQNames	Fixes user store qnames to improve native authentication performance	0	5003	19100	5004	2024-02-19 07:51:27.206	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.activitiesEmailTemplate	Creates activities email templates.	0	5005	19100	5006	2024-02-19 07:51:27.207	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.newUserEmailTemplates	Adds the email templates for notifying new users of their accounts	0	5005	19100	5006	2024-02-19 07:51:27.208	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.inviteEmailTemplates	Adds the email templates for inviting users to a Site	0	5005	19100	5006	2024-02-19 07:51:27.208	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.htmlNotificationMailTemplates	Adds HTML email templates for notifying users of new content	0	5005	19100	5006	2024-02-19 07:51:27.209	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.fixAclInheritance	Fixes any ACL inheritance issues.	0	5005	19100	5006	2024-02-19 07:51:27.209	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.4-JBPM-FK-indexes	Ensures that the database upgrade script has been run.	0	5005	19100	5006	2024-02-19 07:51:27.21	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.imap.clear.old.messages	Remove old IMAP message templates	0	5005	19100	5006	2024-02-19 07:51:27.21	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.imapSpacesTemplates2	Replaces content templates for IMAP clients	0	5005	19100	5006	2024-02-19 07:51:27.211	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.migrateAttrDropOldTables	Drops old alf_*attribute* tables and sequence	0	5006	19100	5007	2024-02-19 07:51:27.212	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.0-AclChangeSet	Ensures that the database upgrade script has been run.	0	5007	19100	5008	2024-02-19 07:51:27.212	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.0-NodeAssoc-Ordering	Ensures that the database upgrade script has been run.	0	5008	19100	5009	2024-02-19 07:51:27.215	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.0-Node-Locale	Ensures that the database upgrade script has been run.	0	5009	19100	5010	2024-02-19 07:51:27.216	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.activitiesTemplatesUpdate	Updates activities email templates.	0	5010	19100	5011	2024-02-19 07:51:27.216	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.followingMailTemplates	Adds email templates for following notifications	0	5010	19100	5011	2024-02-19 07:51:27.217	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.0-SubscriptionTables	Ensures that the database upgrade script has been run.	0	5010	19100	5011	2024-02-19 07:51:27.217	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.copiedFromAspect	Adds peer associations for cm:copiedfrom and cm:workingcopy (new model) and removes cm:source property	0	5012	19100	5013	2024-02-19 07:51:27.218	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.publishing.root	Creates the publishing root folder in the Data Dictionary	0	5013	19100	5014	2024-02-19 07:51:27.218	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.workflowNotification	Patch to add workflow email notification email folder and template.	0	5014	19100	5015	2024-02-19 07:51:27.219	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.nodeTemplatesFolder	Patch to create new Data Dictionary folder for Share - Create Node by Template	0	5015	19100	5016	2024-02-19 07:51:27.219	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateMimetypes3	Fix mimetype for MPEG Audio	0	5016	19100	5017	2024-02-19 07:51:27.22	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.sitesSpacePermissions	Patch to remove the EVERYONE Contributor permissions on the Sites Space (parent container of all Sites)	0	5017	19100	5018	2024-02-19 07:51:27.221	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateWorkflowNotificationTemplates	Patch to update the workflow notification templates.	0	5018	19100	5019	2024-02-19 07:51:27.221	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateMimetypes4	Rationalise mimetypes for PhotoShop and AutoCad	0	5019	19100	5020	2024-02-19 07:51:27.222	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateMimetypesWMA	Fix mimetype for MS WMA Streaming Audio	0	5020	19100	5021	2024-02-19 07:51:27.223	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateFollowingEmailTemplatesPatch	Patch to update the following notification email templates.	0	5021	19100	5022	2024-02-19 07:51:27.223	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.0-SolrTracking	Ensures that the database upgrade script has been run.	0	5022	19100	5023	2024-02-19 07:51:27.224	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.addDutchEmailTemplatesPatch	Patch to add Dutch email templates.	0	5023	19100	5024	2024-02-19 07:51:27.224	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.fixBpmPackages	Corrects workflow package types and associations 	0	5024	19100	5025	2024-02-19 07:51:27.225	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.4-alter-jBPM331-CLOB-columns-to-nvarchar	Altering CLOB columns in the jBPM 3.3.1 tables to introduce Unicode characters support for jBPM 3.3.1	0	6000	19100	6001	2024-02-19 07:51:27.225	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.imapUnsubscribedAspect	Patch to remove deprecated "imap:nonSubscribed" aspect from folders.	0	6001	19100	6002	2024-02-19 07:51:27.226	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.0-Activiti-task-id-indexes	Ensures that the database upgrade script has been run.	0	6003	19100	6004	2024-02-19 07:51:27.226	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.remoteCredentialsContainer	Patch to add the root folder for Shared Remote Credentials	0	6005	19100	6006	2024-02-19 07:51:27.228	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.syncSetDefinitionsContainer	Patch to add the root folder for SyncSet Definitions	0	6005	19100	6006	2024-02-19 07:51:27.229	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.migrateTenantsFromAttrsToTable	Migrate Tenant attributes to Tenant table	0	6006	19100	6007	2024-02-19 07:51:27.229	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.swsdpPatch	Patch to fix up the Sample: Web Site Design Project.	0	6007	19100	6008	2024-02-19 07:51:27.23	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.0-AclChangeSet2	Ensures that the database upgrade script has been run.	0	6008	19100	6009	2024-02-19 07:51:27.23	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.redeployParallelActivitiWorkflows	Patch that redeploys both parallel activiti workflows, completion-condition now takes into account if minimum approval percentage can still be achived.	0	6009	19100	6010	2024-02-19 07:51:27.231	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.4-remove-redundant-jbpm-indexes	Ensures that the database upgrade script has been run.	0	6010	19100	6011	2024-02-19 07:51:27.231	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.show.audit	Updates show_audit.ftl file for upgrade from v3.3.5 to v3.4.x (ALF-13929)	0	6011	19100	6012	2024-02-19 07:51:27.231	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.1-increase-column-size-activiti	ALF-14983 : Upgrade scripts to increase column sizes for Activiti	0	6012	19100	6013	2024-02-19 07:51:27.232	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateMimetypesSVG	Fix mimetype for Scalable Vector Graphics Image	0	6013	19100	6014	2024-02-19 07:51:27.233	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.1-NodeDeleted	Ensures that the database upgrade script has been run.	0	6014	19100	6015	2024-02-19 07:51:27.233	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateMimetypesVISIO	Fix mimetype for Microsoft Visio	0	6015	19100	6016	2024-02-19 07:51:27.234	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V3.4-JBPM-varinst-indexes	Ensures that the database upgrade script has been run.	0	6016	19100	6017	2024-02-19 07:51:27.234	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.2-upgrade-to-activiti-5.10	Upgraded Activiti tables to 5.10 version	0	6018	19100	6019	2024-02-19 07:51:27.234	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.emailAliasableAspect	Add email aliases to attrubute table	0	6019	19100	6020	2024-02-19 07:51:27.235	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.2-add-activti-index-historic-activity	Additional index for activiti on historic activity (PROC_INST_ID_ and ACTIVITY_ID_)	0	6021	19100	6022	2024-02-19 07:51:27.236	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.2-upgrade-to-activiti-5.11	Upgraded Activiti tables to 5.11 version	0	6022	19100	6023	2024-02-19 07:51:27.236	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.1-drop-alfqname-fk-indexes	Ensures that the database upgrade script has been run.	0	6023	19100	6024	2024-02-19 07:51:27.236	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.sharedFolder	Add Shared Folder	0	6023	19100	6024	2024-02-19 07:51:27.237	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.2-remove-index-acl_id	ALF-12284 : Update ALF_ACL_MEMBER_member table. Remove index acl_id.	0	6024	19100	6025	2024-02-19 07:51:27.237	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.1-drop-activiti-feed-format	Ensures that the database upgrade script has been run.	0	6025	19100	6026	2024-02-19 07:51:27.238	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.calendarAllDayEventDatesCorrectingPatch	This patch corrects 'to' and 'from' dates for Calendar 'All Day' Events from version 3.4 which did not take account of time zone offsets	0	6026	19100	6027	2024-02-19 07:51:27.238	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.2-remove-old-index-act	Delete unnecessary indexes add with older version of Activiti in 4.0 branch.	0	6027	19100	6028	2024-02-19 07:51:27.239	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.redeployActivitiWorkflowsForCategory	Redeploy internal process definitions for category update	0	6027	19100	6028	2024-02-19 07:51:27.239	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.2-upgrade-to-activiti-5.13	Upgraded Activiti tables to 5.13 version	0	6028	19100	6029	2024-02-19 07:51:27.24	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.redeployParallelActivitiWorkflows-after-5-11-upgrade	Patch that redeploys both parallel activiti workflows, completion-condition now takes into account if minimum approval percentage can still be achived.	0	6029	19100	6030	2024-02-19 07:51:27.24	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.1-fix-Repo-seqs-order	Ensures that the database upgrade script has been run.	0	6030	19100	6031	2024-02-19 07:51:27.241	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.1-ChildAssoc-OrderBy	Ensures that the database upgrade script has been run.	0	6032	19100	6033	2024-02-19 07:51:27.241	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.1-createIdxAlfNodeTQN	Ensures that the database upgrade script has been run.	0	7000	19100	7001	2024-02-19 07:51:27.242	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.2-restructure-idx_alf_nprop_s-MSSQL	Ensures that the database upgrade script has been run.	0	7001	19100	7002	2024-02-19 07:51:27.242	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.siteAdministrators	Adds the 'GROUP_SITE_ADMINISTRATORS' group	0	7002	19100	7003	2024-02-19 07:51:27.243	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.alfrescoSearchAdministrators	Adds the 'GROUP_ALFRESCO_SEARCH_ADMINISTRATORS' group	0	7003	19100	7004	2024-02-19 07:51:27.244	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.surfConfigFolder	Adds cm:indexControl aspect to surf-config children	0	7004	19100	7005	2024-02-19 07:51:27.244	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.renameSiteAuthorityDisplayName	Update authority display name for sites	0	8000	19100	8001	2024-02-19 07:51:27.245	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.1-update-activiti-nullable-columns	Ensures that the database upgrade script has been run.	0	8005	19100	8006	2024-02-19 07:51:27.245	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.eol-wcmwf	Undeploys deprecated WCMWF Workflows	0	8007	19100	8008	2024-02-19 07:51:27.246	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V4.2-migrate-locale-multilingual	Ensures that the database upgrade script has been run.	0	8018	19100	8019	2024-02-19 07:51:27.246	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.fixWebscriptTemplate	Reimport fixed sample template.	0	9000	19100	9001	2024-02-19 07:51:27.247	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.imapSpacesTemplates3	Replaces content templates for IMAP clients	0	9001	19100	9002	2024-02-19 07:51:27.247	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.addUnmovableAspect	Add unmovable aspect to sites.	0	9003	19100	9004	2024-02-19 07:51:27.248	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.deleteClassifibleAspectForFailedThumbnail	Deletes 'cm:generalclassifiable' aspect and associated properties for nodes of 'cm:failedThumbnail' type	0	9004	19100	9005	2024-02-19 07:51:27.248	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.downloadsFolder	Ensures the Syste Downloads folder exists.	0	9005	19100	9006	2024-02-19 07:51:27.249	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.fixPersonSizeCurrentType	Fix type of cm:sizeCurrent property.	0	9007	19100	9008	2024-02-19 07:51:27.249	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.alfrescoModelAdministrators	Adds the 'GROUP_ALFRESCO_MODEL_ADMINISTRATORS' group	0	9008	19100	9009	2024-02-19 07:51:27.25	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.addInviteAddDirectEmailTemplates	Adds the email templates for the add-direct invite flow	0	9009	19100	9010	2024-02-19 07:51:27.25	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.imapSpacesTemplates4	Replaces content templates for IMAP clients	0	9011	19100	9012	2024-02-19 07:51:27.251	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.addSurfConfigFolders	Adds 'cm:extensions' and 'cm:module-deployments' folders into surf-config folder.	0	9014	19100	9015	2024-02-19 07:51:27.252	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.spacesBootstrapSmartTemplatesFolder	Adds Smart Templates Folder in Data Dictionary.	0	9015	19100	9016	2024-02-19 07:51:27.252	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.spacesBootstrapSmartFolderExample	Adds smartFoldersExample.json file in Smart Templates Folder.	0	9015	19100	9016	2024-02-19 07:51:27.253	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.spacesBootstrapSmartDownloadFolder	Adds Smart Download Folder in Data Dictionary.	0	9015	19100	9016	2024-02-19 07:51:27.253	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-v4.2-migrate-activiti-workflows	Migrated workflow variables into newly created table.	0	10000	19100	10001	2024-02-19 07:51:27.254	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.redeployInvitationNominatedAddDirectActivitiWorkflow	Patch that redeploys activitiInvitationNominatedAddDirect workflow after upgrade, needed for tenants created before 5.1	0	10001	19100	10002	2024-02-19 07:51:27.255	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.updateAdminUserWhenDefault	Update Admin User by removing the default SHA256 and falling back to the MD4 (please consider using BCRYPT instead)	0	10002	19100	10003	2024-02-19 07:51:27.255	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.addInviteModeratedEmailTemplates	Adds the email template for the invite moderated flow	0	10050	19100	10051	2024-02-19 07:51:27.256	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.redeployInvitationModeratedActivitiWorkflow	Redeploy invitation moderated workflow.	0	10052	19100	10053	2024-02-19 07:51:27.256	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.db-V6.0-change-set-indexes	Add additional indexes to support acl tracking.	0	10200	19100	10201	2024-02-19 07:51:27.256	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.alfrescoSystemAdministrators	Adds the 'GROUP_ALFRESCO_SYSTEM_ADMINISTRATORS' group	0	15001	19100	15002	2024-02-19 07:51:27.257	23.1.1 (r3a759f72-blocal) - Enterprise	f	t	Not relevant to schema 19,100
patch.exampleJavaScript	Loads sample Javascript file into datadictionary scripts folder	0	19100	19100	100000	2024-02-19 07:51:27.281	23.1.1 (r3a759f72-blocal) - Enterprise	t	t	Imported view into bootstrap location: /app:company_home/app:dictionary/app:scripts (workspace://SpacesStore/e7a273da-2974-4581-a219-5e897342844a)
patch.siteLoadPatch.swsdp	Loads a sample site into the repository.	0	19100	19100	100000	2024-02-19 07:51:28.608	23.1.1 (r3a759f72-blocal) - Enterprise	t	t	Site swsdp imported.
\.


--
-- Data for Name: alf_audit_app; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_audit_app (id, version, app_name_id, audit_model_id, disabled_paths_id) FROM stdin;
1	0	5	2	2
\.


--
-- Data for Name: alf_audit_entry; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_audit_entry (id, audit_app_id, audit_time, audit_user_id, audit_values_id) FROM stdin;
\.


--
-- Data for Name: alf_audit_model; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_audit_model (id, content_data_id, content_crc) FROM stdin;
1	1	4227823780
2	2	252222308
3	3	1341618561
\.


--
-- Data for Name: alf_auth_status; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_auth_status (id, username, deleted, authorized, checksum, authaction) FROM stdin;
1	admin	f	t	\\xaced0005737200196a617661782e63727970746f2e5365616c65644f626a6563743e363da6c3b754700200045b000d656e636f646564506172616d737400025b425b0010656e63727970746564436f6e74656e7471007e00014c0009706172616d73416c677400124c6a6176612f6c616e672f537472696e673b4c00077365616c416c6771007e00027870757200025b42acf317f8060854e002000078700000000a0408bad33cb9f59a35b57571007e0004000000583461d1914b8e5528c638305e9bfc6ff00a998f4d5a1c6b334c8d5d8adcfe1611d630fdde2517733353ee4d3cdc03425c8cc10781a581d5bf208958be74e18c0bc7e199e42f102a07d2eee1bfb962f1a1580f10ae7ae71caf7400064445536564657400174445536564652f4342432f504b43533550616464696e67	ADD
\.


--
-- Data for Name: alf_authority; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_authority (id, version, authority, crc) FROM stdin;
1	0	GROUP_EVERYONE	1514782197
2	0	guest	2897713717
3	0	admin	2282622326
4	0	ROLE_OWNER	881792602
5	0	GROUP_ALFRESCO_MODEL_ADMINISTRATORS	1510054418
6	0	GROUP_site_swsdp_SiteManager	2553824491
7	0	GROUP_site_swsdp_SiteCollaborator	3822262144
8	0	GROUP_site_swsdp_SiteContributor	3082136708
9	0	GROUP_site_swsdp_SiteConsumer	4116454302
10	0	abeecher	2776041939
11	0	mjackson	4006557174
\.


--
-- Data for Name: alf_authority_alias; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_authority_alias (id, version, auth_id, alias_id) FROM stdin;
\.


--
-- Data for Name: alf_child_assoc; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_child_assoc (id, version, parent_node_id, type_qname_id, child_node_name_crc, child_node_name, child_node_id, qname_ns_id, qname_localname, qname_crc, is_primary, assoc_index) FROM stdin;
1	1	1	4	-1908164941	a1874f36-d820-497c-b85a-54b5f0a5a2c0	2	1	system	1509831379	t	-1
2	1	2	4	-3446154566	20b9ac2c-47f8-4004-b720-5de031831cf5	3	1	people	3097839998	t	-1
3	1	3	4	-764986929	e8775618-5247-4856-88c7-82d41a896941	4	2	admin	3226179863	t	-1
4	1	5	4	-3008508678	d2993a8a-2be4-45d0-baaf-dc5496784200	6	1	descriptor	147310537	t	-1
5	1	5	4	-354297007	2c2fdbac-b9ec-4799-a952-aad1951e484f	7	1	descriptor-current	369154895	t	-1
6	1	5	4	-1963835738	1a52d9c5-c992-4a19-972e-75211245a1d6	8	1	system-registry	3365896830	t	-1
7	1	12	4	-447362129	59f2feff-d57a-4085-a06a-54373d2838d2	13	7	company_home	1502496580	t	-1
8	1	13	33	3176102346	data dictionary	14	7	dictionary	3252140894	t	-1
9	1	14	33	1103883742	space templates	15	7	space_templates	3942413840	t	-1
10	1	14	33	1976539493	presentation templates	16	7	content_templates	1001730099	t	-1
11	1	14	33	1734425776	email templates	17	7	email_templates	3428231038	t	-1
12	1	17	33	3353481431	invite	18	6	invite	866784651	t	-1
13	1	14	33	666141259	rss templates	19	7	rss_templates	2421027117	t	-1
14	1	14	33	568537909	saved searches	20	7	saved_searches	3343229978	t	-1
15	1	14	33	3709769534	scripts	21	7	scripts	454145674	t	-1
16	1	14	33	1943383032	node templates	22	7	node_templates	3173772998	t	-1
17	1	14	33	3744615100	smart folder templates	23	7	smart_folders	804844994	t	-1
18	1	14	33	4218000263	smart folder downloads	24	7	smart_downloads	3878312324	t	-1
19	1	13	33	787359379	guest home	25	7	guest_home	2333806581	t	-1
20	1	13	33	2233110431	user homes	26	7	user_homes	2702798579	t	-1
21	1	13	33	328004795	shared	27	7	shared	276099713	t	-1
22	1	13	33	702905850	imap attachments	28	6	Imap Attachments	1001418059	t	-1
23	1	13	33	3935772771	imap home	29	6	Imap Home	2827151161	t	-1
24	1	12	4	-140507408	46e0061d-960e-443f-8d16-5eac73f8e7a9	30	1	system	1509831379	t	-1
25	1	30	4	-101531065	4c00efdc-8231-4fa1-bc91-4490645309f5	31	1	people	3097839998	t	-1
26	1	31	4	-3706233973	977e36f2-cb00-40f4-805a-89ed75e92109	32	6	admin	347996256	t	-1
27	1	31	4	-2172454689	94c198da-033c-4c3d-8dd6-82f52053a430	33	6	guest	805803811	t	-1
28	1	30	4	-2323332566	0fec04e7-c967-4ea8-826b-8005a5cb8d25	34	1	workflow	3049303691	t	-1
29	1	12	4	-3378182672	5954746e-43fa-4003-927e-884665849a47	35	6	categoryRoot	2175667943	t	-1
30	1	35	48	-658633255	75331de5-ddb7-45dd-bfda-bd06eecd5303	36	6	generalclassifiable	1686288257	t	-1
31	1	36	49	2698881118	software document classification	37	6	Software Document Classification	3819543068	t	-1
32	1	37	49	28469387	software descriptions	38	6	Software Descriptions	1635852989	t	-1
33	1	38	49	2073851679	main software descriptions	39	6	Main Software Descriptions	1138654932	t	-1
34	1	39	49	2907738123	short system description	40	6	Short System Description	1810876073	t	-1
35	1	39	49	1606402406	requirement description	41	6	Requirement Description	189951905	t	-1
36	1	39	49	1896099312	architecture description	42	6	Architecture Description	3942499170	t	-1
37	1	39	49	2399881991	implementation description	43	6	Implementation Description	3190567739	t	-1
38	1	39	49	1353530587	configuration description	44	6	Configuration Description	3499778930	t	-1
39	1	38	49	4043393805	software description appendices	45	6	Software Description Appendices	284764992	t	-1
40	1	45	49	2484979098	terminology description	46	6	Terminology Description	3236981597	t	-1
41	1	45	49	665542653	internal message description	47	6	Internal Message Description	434134045	t	-1
42	1	45	49	3472799219	external message description	48	6	External Message Description	4038440467	t	-1
43	1	45	49	4056602909	record description	49	6	Record Description	608272631	t	-1
44	1	45	49	393869945	user interface description	50	6	User Interface Description	2979440541	t	-1
45	1	45	49	894410162	process description	51	6	Process Description	3957868962	t	-1
46	1	45	49	3995652705	initialization description	52	6	Initialization Description	3742345821	t	-1
47	1	37	49	3227434245	utilization documents	53	6	Utilization Documents	984460904	t	-1
48	1	53	49	2192566828	user's manual	54	6	User's Manual	3812762855	t	-1
49	1	53	49	3053746966	operator's manual	55	6	Operator's Manual	367883218	t	-1
50	1	53	49	2619173183	installation manual	56	6	Installation Manual	3280615374	t	-1
51	1	53	49	876354352	service manual	57	6	Service Manual	4042969452	t	-1
52	1	53	49	1856239369	user's help	58	6	User's Help	2355756505	t	-1
53	1	53	49	2623414449	operator's help	59	6	Operator's Help	2445831430	t	-1
54	1	53	49	2979080622	installations help	60	6	Installations Help	1119113389	t	-1
55	1	53	49	3175522142	service help	61	6	Service Help	1518602170	t	-1
56	1	37	49	2368576440	development plans	62	6	Development Plans	3903437134	t	-1
57	1	62	49	2270310850	responsibility plan	63	6	Responsibility Plan	2136720699	t	-1
58	1	62	49	864431939	work breakdown plan	64	6	Work Breakdown Plan	116106082	t	-1
59	1	62	49	1183845432	schedule plan	65	6	Schedule Plan	2162387195	t	-1
60	1	62	49	310338049	expense plan	66	6	Expense Plan	4122868453	t	-1
61	1	62	49	1449103693	phase plan	67	6	Phase Plan	3159407706	t	-1
62	1	62	49	1972689447	risk plan	68	6	Risk Plan	931208573	t	-1
63	1	62	49	2745239212	test plan	69	6	Test Plan	3786719734	t	-1
64	1	62	49	2401478941	acceptance plan	70	6	Acceptance Plan	2192958634	t	-1
65	1	62	49	3520848714	manual plan	71	6	Manual Plan	856811930	t	-1
66	1	62	49	3121370645	method plan	72	6	Method Plan	1489394885	t	-1
67	1	62	49	155996287	quality plan	73	6	Quality Plan	4002334875	t	-1
68	1	62	49	2453626510	documentation plan	74	6	Documentation Plan	1629104013	t	-1
69	1	62	49	3287744536	version control plan	75	6	Version Control Plan	2125207702	t	-1
70	1	37	49	4069423408	quality documents	76	6	Quality Documents	3560633953	t	-1
71	1	76	49	3202395658	change request	77	6	Change Request	2989971708	t	-1
72	1	76	49	875981477	analysis request	78	6	Analysis Request	1866648671	t	-1
73	1	76	49	1406577714	information request	79	6	Information Request	3293630057	t	-1
74	1	76	49	610673743	reader's report	80	6	Reader's Report	2389623792	t	-1
75	1	76	49	1417252998	review report	81	6	Review Report	898915917	t	-1
76	1	76	49	3972239595	inspection report	82	6	Inspection Report	1328191535	t	-1
77	1	76	49	1238814824	test report	83	6	Test Report	209875120	t	-1
78	1	76	49	1438127416	review call	84	6	Review Call	3077709800	t	-1
79	1	76	49	2636004156	inspection call	85	6	Inspection Call	2424862347	t	-1
80	1	76	49	2994041583	test call	86	6	Test Call	4032869813	t	-1
81	1	37	49	458235642	administrative documents	87	6	Administrative Documents	2227502364	t	-1
82	1	87	49	3029136191	preliminary contract	88	6	Preliminary Contract	1218641185	t	-1
83	1	87	49	330113795	development contract	89	6	Development Contract	4018351389	t	-1
84	1	87	49	1310544536	extended contract	90	6	Extended Contract	321797436	t	-1
85	1	87	49	3639418992	maintenance contract	91	6	Maintenance Contract	616771182	t	-1
86	1	87	49	2494063934	contract review minutes	92	6	Contract Review Minutes	1140852074	t	-1
87	1	87	49	611590337	project meeting minutes	93	6	Project Meeting Minutes	2977760979	t	-1
88	1	36	49	2698072953	languages	94	6	Languages	1123433245	t	-1
89	1	94	49	746783232	english	95	6	English	2165796422	t	-1
90	1	95	49	25551090	british english	96	6	British English	1671183335	t	-1
91	1	95	49	2309252311	american english	97	6	American English	3536896557	t	-1
92	1	95	49	3115211888	australian english	98	6	Australian English	621065169	t	-1
93	1	95	49	1896104131	canadian english	99	6	Canadian English	712042041	t	-1
94	1	95	49	3858082522	indian english	100	6	Indian English	3911343148	t	-1
95	1	94	49	2943733342	french	101	6	French	1553059380	t	-1
96	1	101	49	810812125	french french	102	6	French French	1371130902	t	-1
97	1	101	49	1076629412	canadian french	103	6	Canadian French	3928287259	t	-1
98	1	94	49	2721333409	german	104	6	German	1372602571	t	-1
99	1	104	49	476060318	german german	105	6	German German	2106446933	t	-1
100	1	104	49	3139883389	austrian german	106	6	Austrian German	288241346	t	-1
101	1	104	49	3847244892	swiss german	107	6	Swiss German	2769059504	t	-1
102	1	94	49	874050868	spanish	108	6	Spanish	2576128370	t	-1
103	1	108	49	874050868	spanish	109	6	Spanish	2576128370	t	-1
104	1	108	49	119644513	mexican spanish	110	6	Mexican Spanish	1698200180	t	-1
105	1	108	49	2436862947	american spanish	111	6	American Spanish	3393926425	t	-1
106	1	36	49	2724690419	regions	112	6	Regions	267589045	t	-1
107	1	112	49	2521632419	africa	113	6	AFRICA	2466106489	t	-1
108	1	113	49	3544705375	eastern africa	114	6	Eastern Africa	394339075	t	-1
109	1	114	49	1928293556	burundi	115	6	Burundi	3749380338	t	-1
110	1	114	49	2691402736	comoros	116	6	Comoros	234874806	t	-1
111	1	114	49	2618175708	djibouti	117	6	Djibouti	52183061	t	-1
112	1	114	49	121454611	eritrea	118	6	Eritrea	2863213653	t	-1
113	1	114	49	750064544	ethiopia	119	6	Ethiopia	3014091625	t	-1
114	1	114	49	2721694507	kenya	120	6	Kenya	4282966329	t	-1
115	1	114	49	3049179811	madagascar	121	6	Madagascar	4286793866	t	-1
116	1	114	49	3440327867	malawi	122	6	Malawi	1055603921	t	-1
117	1	114	49	65273427	mauritius	123	6	Mauritius	3787757111	t	-1
118	1	114	49	2131622377	mozambique	124	6	Mozambique	892369856	t	-1
119	1	114	49	1526768770	reunion	125	6	Reunion	4136945860	t	-1
120	1	114	49	1177401495	rwanda	126	6	Rwanda	3049896189	t	-1
121	1	114	49	3046112852	seychelles	127	6	Seychelles	4289887357	t	-1
122	1	114	49	2486253753	somalia	128	6	Somalia	967136511	t	-1
123	1	114	49	1002971053	uganda	129	6	Uganda	3358334919	t	-1
124	1	114	49	1120322014	united republic of tanzania	130	6	United Republic of Tanzania	2518905732	t	-1
125	1	114	49	3015467081	zambia	131	6	Zambia	1079533603	t	-1
126	1	114	49	2221643360	zimbabwe	132	6	Zimbabwe	460973737	t	-1
127	1	113	49	20947320	middle africa	133	6	Middle Africa	1624403891	t	-1
128	1	133	49	1122992818	angola	134	6	Angola	2970321624	t	-1
129	1	133	49	971834596	cameroon	135	6	Cameroon	2801759277	t	-1
130	1	133	49	725561226	central african republic	136	6	Central African Republic	3625146340	t	-1
131	1	133	49	2016076494	chad	137	6	Chad	1628697140	t	-1
132	1	133	49	3456989782	congo	138	6	Congo	2474399812	t	-1
133	1	133	49	1584510254	democratic republic of the congo	139	6	Democratic Republic of the Congo	2223897072	t	-1
134	1	133	49	2985204337	equatorial guinea	140	6	Equatorial Guinea	302486197	t	-1
135	1	133	49	1838606431	gabon	141	6	Gabon	820496973	t	-1
136	1	133	49	3242386644	sao tome and principe	142	6	Sao Tome and Principe	1855268278	t	-1
137	1	113	49	1911316965	northern africa	143	6	Northern Africa	3689186906	t	-1
138	1	143	49	831440508	algeria	144	6	Algeria	2618993210	t	-1
139	1	143	49	309747606	egypt	145	6	Egypt	1325890948	t	-1
140	1	143	49	6336724	libyan arab jamahiriya	146	6	Libyan Arab Jamahiriya	3143393468	t	-1
141	1	143	49	3082038010	morocco	147	6	Morocco	438322876	t	-1
142	1	143	49	3179497705	sudan	148	6	Sudan	3773983483	t	-1
143	1	143	49	1723175352	tunisia	149	6	Tunisia	3408002558	t	-1
144	1	143	49	175292891	western sahara	150	6	Western Sahara	3467905927	t	-1
145	1	113	49	3035859710	southern africa	151	6	Southern Africa	519793985	t	-1
146	1	151	49	3672489918	botswana	152	6	Botswana	1173868407	t	-1
147	1	151	49	2007320486	lesotho	153	6	Lesotho	3660655584	t	-1
148	1	151	49	1729667362	namibia	154	6	Namibia	3398225252	t	-1
149	1	151	49	3174958594	south africa	155	6	South Africa	4251071726	t	-1
150	1	151	49	330040633	swaziland	156	6	Swaziland	4052446557	t	-1
151	1	113	49	2485287688	western africa	157	6	Western Africa	1357164884	t	-1
152	1	157	49	1968483994	benin	158	6	Benin	673549448	t	-1
153	1	157	49	2218827890	burkina faso	159	6	Burkina Faso	1669478550	t	-1
154	1	157	49	4050001938	cape verde	160	6	Cape Verde	2056949055	t	-1
155	1	157	49	504304762	cote d'ivoire	161	6	Cote d'Ivoire	2145573553	t	-1
156	1	157	49	217346316	gambia	162	6	Gambia	4279267686	t	-1
157	1	157	49	2608608604	ghana	163	6	Ghana	3322746702	t	-1
158	1	157	49	3008979313	guinea	164	6	Guinea	1086152987	t	-1
159	1	157	49	585069007	guinea-bissau	165	6	Guinea-Bissau	1127399172	t	-1
160	1	157	49	3363092863	liberia	166	6	Liberia	1709233465	t	-1
161	1	157	49	1547414658	mali	167	6	Mali	1157807224	t	-1
162	1	157	49	2134688598	mauritania	168	6	Mauritania	889274751	t	-1
163	1	157	49	1293407471	niger	169	6	Niger	275183357	t	-1
164	1	157	49	2534090534	nigeria	170	6	Nigeria	983518048	t	-1
165	1	157	49	2902273140	saint helena	171	6	Saint Helena	3969982104	t	-1
166	1	157	49	1196814092	senegal	172	6	Senegal	3938550602	t	-1
167	1	157	49	4211871653	sierra leone	173	6	Sierra Leone	2101155707	t	-1
168	1	157	49	1898177891	togo	174	6	Togo	1746597273	t	-1
169	1	112	49	2061703251	asia	175	6	ASIA	4118628899	t	-1
170	1	175	49	2704003441	eastern asia	176	6	Eastern Asia	1189693845	t	-1
171	1	176	49	1626325540	china	177	6	China	1033822262	t	-1
172	1	176	49	3890259192	democratic people's republic of korea	178	6	Democratic People's Republic of Korea	1741765450	t	-1
173	1	176	49	1876027915	hong kong sar	179	6	Hong Kong SAR	1715536170	t	-1
174	1	176	49	338130558	japan	180	6	Japan	1230394476	t	-1
175	1	176	49	2366445845	macao, china	181	6	Macao, China	188481995	t	-1
176	1	176	49	3873228617	mongolia	182	6	Mongolia	2043590528	t	-1
177	1	176	49	2622242312	republic of korea	183	6	Republic of Korea	4190641406	t	-1
178	1	175	49	2844876198	south-central asia	184	6	South-central Asia	1521755301	t	-1
179	1	184	49	1789607488	afghanistan	185	6	Afghanistan	676567982	t	-1
180	1	184	49	2427965986	bangladesh	186	6	Bangladesh	3666497547	t	-1
181	1	184	49	2219423205	bhutan	187	6	Bhutan	2007872911	t	-1
182	1	184	49	1219330253	india	188	6	India	366781151	t	-1
183	1	184	49	1077224957	iran (islamic republic of)	189	6	Iran (Islamic Republic of)	4114831845	t	-1
184	1	184	49	2787889741	kazakhstan	190	6	Kazakhstan	3960885348	t	-1
185	1	184	49	1429765081	kyrgyzstan	191	6	Kyrgyzstan	520485360	t	-1
186	1	184	49	3613204721	maldives	192	6	Maldives	1213157432	t	-1
187	1	184	49	2160797381	nepal	193	6	Nepal	3719955671	t	-1
188	1	184	49	3996924490	pakistan	194	6	Pakistan	1898859139	t	-1
189	1	184	49	4185751281	sri lanka	195	6	Sri Lanka	3667605905	t	-1
190	1	184	49	45768404	tajikistan	196	6	Tajikistan	1216863485	t	-1
191	1	184	49	927251696	turkmenistan	197	6	Turkmenistan	1890880298	t	-1
192	1	184	49	3618150535	uzbekistan	198	6	Uzbekistan	2643794606	t	-1
193	1	175	49	1235590902	south-eastern asia	199	6	South-eastern Asia	3129010165	t	-1
194	1	199	49	428996190	brunei darussalam	200	6	Brunei Darussalam	4069128316	t	-1
195	1	199	49	3809346112	cambodia	201	6	Cambodia	2082239113	t	-1
196	1	199	49	2805259669	indonesia	202	6	Indonesia	1158919665	t	-1
197	1	199	49	711454169	lao people's democratic republic	203	6	Lao People's Democratic Republic	1340021817	t	-1
198	1	199	49	375588017	malaysia	204	6	Malaysia	2305913976	t	-1
199	1	199	49	2791924250	myanmar	205	6	Myanmar	201133660	t	-1
200	1	199	49	422453359	philippines	206	6	Philippines	1540785537	t	-1
201	1	199	49	2194449579	singapore	207	6	Singapore	1626066127	t	-1
202	1	199	49	732922246	thailand	208	6	Thailand	3032346959	t	-1
203	1	199	49	480960120	timor-leste	209	6	Timor-Leste	2677232786	t	-1
204	1	199	49	3905329337	viet nam	210	6	Viet Nam	1335471760	t	-1
205	1	175	49	4057059728	western asia	211	6	Western Asia	370362740	t	-1
206	1	211	49	1474225954	armenia	212	6	Armenia	4199189348	t	-1
207	1	211	49	1865560516	azerbaijan	213	6	Azerbaijan	621786093	t	-1
208	1	211	49	2109290352	bahrain	214	6	Bahrain	3492633398	t	-1
209	1	211	49	1819209598	cyprus	215	6	Cyprus	2676682516	t	-1
210	1	211	49	2449663986	georgia	216	6	Georgia	1066830772	t	-1
211	1	211	49	1811661031	iraq	217	6	Iraq	1925369885	t	-1
212	1	211	49	4108721855	israel	218	6	Israel	117579477	t	-1
213	1	211	49	1540059217	jordan	219	6	Jordan	2821681211	t	-1
214	1	211	49	3194580348	kuwait	220	6	Kuwait	1301114134	t	-1
215	1	211	49	3809283664	lebanon	221	6	Lebanon	1318674966	t	-1
216	1	211	49	1717312488	occupied palestinian territory	222	6	Occupied Palestinian Territory	2426687139	t	-1
217	1	211	49	3571611267	oman	223	6	Oman	3453707897	t	-1
218	1	211	49	270476414	qatar	224	6	Qatar	1299056236	t	-1
219	1	211	49	2924782003	saudi arabia	225	6	Saudi Arabia	3993539423	t	-1
220	1	211	49	2197609827	syrian arab republic	226	6	Syrian Arab Republic	3450640520	t	-1
221	1	211	49	2762251111	turkey	227	6	Turkey	1463851789	t	-1
222	1	211	49	1491873575	united arab emirates	228	6	United Arab Emirates	398224076	t	-1
223	1	211	49	3432511836	yemen	229	6	Yemen	2447825742	t	-1
224	1	112	49	1217616094	europe	230	6	EUROPE	1277401604	t	-1
225	1	230	49	228179746	eastern europe	231	6	Eastern Europe	3378476414	t	-1
226	1	231	49	3390375802	belarus	232	6	Belarus	1736534844	t	-1
227	1	231	49	1207782951	bulgaria	233	6	Bulgaria	3639557870	t	-1
228	1	231	49	623353472	czech republic	234	6	Czech Republic	523973564	t	-1
229	1	231	49	2213295331	hungary	235	6	Hungary	779638949	t	-1
230	1	231	49	684082746	poland	236	6	Poland	3676456528	t	-1
231	1	231	49	93569048	republic of moldova	237	6	Republic of Moldova	2450891331	t	-1
232	1	231	49	834856803	romania	238	6	Romania	2622917413	t	-1
233	1	231	49	579704207	russian federation	239	6	Russian Federation	1053245538	t	-1
234	1	231	49	3444175596	slovakia	240	6	Slovakia	1381728805	t	-1
235	1	231	49	3223838707	ukraine	241	6	Ukraine	1840493493	t	-1
236	1	230	49	2939303832	northern europe	242	6	Northern Europe	87740455	t	-1
237	1	242	49	2571859775	channel islands	243	6	Channel Islands	4216499242	t	-1
238	1	242	49	3684617504	denmark	244	6	Denmark	1980408166	t	-1
239	1	242	49	1786774787	estonia	245	6	Estonia	3340003653	t	-1
240	1	242	49	1761911701	faeroe islands	246	6	Faeroe Islands	1708348771	t	-1
241	1	242	49	4122426331	finland	247	6	Finland	1478711197	t	-1
242	1	242	49	3585366341	iceland	248	6	Iceland	2015376643	t	-1
243	1	242	49	490637435	ireland	249	6	Ireland	2963989565	t	-1
244	1	242	49	1770003631	isle of man	250	6	Isle of Man	322305953	t	-1
245	1	242	49	1343066742	latvia	251	6	Latvia	2749993500	t	-1
246	1	242	49	286342226	lithuania	252	6	Lithuania	4080423990	t	-1
247	1	242	49	1575594665	norway	253	6	Norway	2920131267	t	-1
248	1	242	49	2250265955	sweden	254	6	Sweden	1975801097	t	-1
249	1	242	49	3344183625	united kingdom	255	6	United Kingdom	3414411199	t	-1
250	1	230	49	1781390467	southern europe	256	6	Southern Europe	3223665468	t	-1
251	1	256	49	1664123983	albania	257	6	Albania	3466913801	t	-1
252	1	256	49	4251792985	andorra	258	6	Andorra	1358518815	t	-1
253	1	256	49	3285898834	bosnia and herzegovina	259	6	Bosnia and Herzegovina	971158691	t	-1
254	1	256	49	609359002	croatia	260	6	Croatia	2311485660	t	-1
255	1	256	49	266022505	gibraltar	261	6	Gibraltar	3992733197	t	-1
256	1	256	49	382570151	greece	262	6	Greece	3844705997	t	-1
257	1	256	49	1139894006	holy see	263	6	Holy See	3836629215	t	-1
258	1	256	49	534354382	italy	264	6	Italy	1118354396	t	-1
259	1	256	49	338758655	malta	265	6	Malta	1228941805	t	-1
260	1	256	49	3618595502	portugal	266	6	Portugal	1220350567	t	-1
261	1	256	49	256616510	san marino	267	6	San Marino	1121595169	t	-1
262	1	256	49	1155573584	slovenia	268	6	Slovenia	3690125209	t	-1
263	1	256	49	1146089528	spain	269	6	Spain	423547434	t	-1
264	1	256	49	2437011711	the former yugoslav republic of macedonia	270	6	The Former Yugoslav Republic of Macedonia	1072209099	t	-1
265	1	256	49	2016032151	yugoslavia	271	6	Yugoslavia	840415166	t	-1
266	1	230	49	1258040693	western europe	272	6	Western Europe	2386224937	t	-1
267	1	272	49	1754436759	austria	273	6	Austria	3305568465	t	-1
268	1	272	49	84027732	belgium	274	6	Belgium	2828420370	t	-1
269	1	272	49	1587946932	france	275	6	France	2906793438	t	-1
270	1	272	49	1520992555	germany	276	6	Germany	4147965293	t	-1
271	1	272	49	457342887	liechtenstein	277	6	Liechtenstein	2097394778	t	-1
272	1	272	49	2623961533	luxembourg	278	6	Luxembourg	3596352404	t	-1
273	1	272	49	536557617	monaco	279	6	Monaco	3961469019	t	-1
274	1	272	49	977422236	netherlands	280	6	Netherlands	2025513586	t	-1
275	1	272	49	3665763213	switzerland	281	6	Switzerland	2559032931	t	-1
276	1	112	49	473456913	latin america	282	6	LATIN AMERICA	373129642	t	-1
277	1	282	49	562533037	caribbean	283	6	Caribbean	3282107081	t	-1
278	1	283	49	1916266241	anguilla	284	6	Anguilla	3978663880	t	-1
279	1	283	49	361675714	antigua and barbuda	285	6	Antigua and Barbuda	2181618073	t	-1
280	1	283	49	2617389991	aruba	286	6	Aruba	3245543861	t	-1
281	1	283	49	2647707828	bahamas	287	6	Bahamas	809811186	t	-1
282	1	283	49	2551714669	barbados	288	6	Barbados	118121380	t	-1
283	1	283	49	145415630	british virgin islands	289	6	British Virgin Islands	1993830828	t	-1
284	1	283	49	1160246246	cayman islands	290	6	Cayman Islands	1240466704	t	-1
285	1	283	49	932410785	cuba	291	6	Cuba	782919003	t	-1
286	1	283	49	2226085289	dominica	292	6	Dominica	465382752	t	-1
287	1	283	49	3401324796	dominican republic	293	6	Dominican Republic	1625140375	t	-1
288	1	283	49	1837967272	grenada	294	6	Grenada	3222883310	t	-1
289	1	283	49	777952348	guadeloupe	295	6	Guadeloupe	1684217461	t	-1
290	1	283	49	3569471062	haiti	296	6	Haiti	2310073412	t	-1
291	1	283	49	2690230965	jamaica	297	6	Jamaica	231589619	t	-1
292	1	283	49	263005616	martinique	298	6	Martinique	1167107993	t	-1
293	1	283	49	4171649994	montserrat	299	6	Montserrat	2996556259	t	-1
294	1	283	49	1066837157	netherlands antilles	300	6	Netherlands Antilles	3283659451	t	-1
295	1	283	49	1223509347	puerto rico	301	6	Puerto Rico	2854703027	t	-1
296	1	283	49	693817849	saint kitts and nevis	302	6	Saint Kitts and Nevis	408703405	t	-1
297	1	283	49	3698368659	saint lucia	303	6	Saint Lucia	1598630521	t	-1
298	1	283	49	1528299237	saint vincent and grenadines	304	6	Saint Vincent and Grenadines	42374613	t	-1
299	1	283	49	3335563666	trinidad and tobago	305	6	Trinidad and Tobago	2571630435	t	-1
300	1	283	49	2563879240	turks and caicos islands	306	6	Turks and Caicos Islands	2272104777	t	-1
301	1	283	49	2958894253	united states virgin islands	307	6	United States Virgin Islands	3139418642	t	-1
302	1	282	49	3201394492	central america	308	6	Central America	3704142889	t	-1
303	1	308	49	2077379440	belize	309	6	Belize	2285259546	t	-1
304	1	308	49	2420439506	costa rica	310	6	Costa Rica	2051790021	t	-1
305	1	308	49	2512286277	el salvador	311	6	El Salvador	774944253	t	-1
306	1	308	49	2780785696	guatemala	312	6	Guatemala	1201144900	t	-1
307	1	308	49	4225790268	honduras	313	6	Honduras	1693581813	t	-1
308	1	308	49	774535531	mexico	314	6	Mexico	3721296129	t	-1
309	1	308	49	3776404894	nicaragua	315	6	Nicaragua	53491194	t	-1
310	1	308	49	1723373468	panama	316	6	Panama	2505873398	t	-1
311	1	282	49	112881138	south america	317	6	South America	2940641171	t	-1
312	1	317	49	299819657	argentina	318	6	Argentina	4093217517	t	-1
313	1	317	49	3170732090	bolivia	319	6	Bolivia	292152444	t	-1
314	1	317	49	1563114528	brazil	320	6	Brazil	2932816970	t	-1
315	1	317	49	1437885631	chile	321	6	Chile	147146413	t	-1
316	1	317	49	779737648	colombia	322	6	Colombia	2976623353	t	-1
317	1	317	49	1805269653	ecuador	323	6	Ecuador	3322828499	t	-1
318	1	317	49	311414191	falkland islands (malvinas)	324	6	Falkland Islands (Malvinas)	4200036155	t	-1
319	1	317	49	3301978884	french guiana	325	6	French Guiana	2772303311	t	-1
320	1	317	49	199796760	guyana	326	6	Guyana	4161579122	t	-1
321	1	317	49	2818510304	paraguay	327	6	Paraguay	955088169	t	-1
322	1	317	49	956426380	peru	328	6	Peru	540604534	t	-1
323	1	317	49	1409461882	suriname	329	6	Suriname	3406880435	t	-1
324	1	317	49	4111472120	uruguay	330	6	Uruguay	1485076926	t	-1
325	1	317	49	745110801	venezuela	331	6	Venezuela	3461223797	t	-1
326	1	112	49	941148238	northern america	332	6	NORTHERN AMERICA	910848477	t	-1
327	1	332	49	4198290940	bermuda	333	6	Bermuda	1470675386	t	-1
328	1	332	49	842554592	canada	334	6	Canada	3252444298	t	-1
329	1	332	49	512708717	greenland	335	6	Greenland	4238882825	t	-1
330	1	332	49	74993950	saint pierre and miquelon	336	6	Saint Pierre and Miquelon	3709965602	t	-1
331	1	332	49	895026901	united states of america	337	6	United States of America	3374476534	t	-1
332	1	112	49	1887015383	oceania	338	6	OCEANIA	756365847	t	-1
333	1	338	49	834064608	australia and new zealand	339	6	Australia and New Zealand	2117908181	t	-1
334	1	339	49	3411872625	australia	340	6	Australia	695886613	t	-1
335	1	339	49	241715737	new zealand	341	6	New Zealand	2200819307	t	-1
336	1	339	49	254052704	norfolk island	342	6	Norfolk Island	3420603196	t	-1
337	1	338	49	2924986328	melanesia	343	6	Melanesia	1282463676	t	-1
338	1	343	49	3547585981	fiji	344	6	Fiji	3393899847	t	-1
339	1	343	49	2103458618	new caledonia	345	6	New Caledonia	2580574820	t	-1
340	1	343	49	44967114	papua new guinea	346	6	Papua New Guinea	3734420810	t	-1
341	1	343	49	3294343407	solomon islands	347	6	Solomon Islands	2789370874	t	-1
342	1	343	49	2310465348	vanuatu	348	6	Vanuatu	606243586	t	-1
343	1	338	49	430281384	micronesia	349	6	Micronesia	1402474625	t	-1
344	1	349	49	3428697731	federated states of micronesia	350	6	Federated States of Micronesia	2050844520	t	-1
345	1	349	49	2590702878	guam	351	6	Guam	2203315684	t	-1
346	1	349	49	3029445841	johnston island	352	6	Johnston Island	513362798	t	-1
347	1	349	49	2202151125	kiribati	353	6	Kiribati	475027484	t	-1
348	1	349	49	3667971656	marshall islands	354	6	Marshall Islands	2178181298	t	-1
349	1	349	49	216439339	nauru	355	6	Nauru	1368897593	t	-1
350	1	349	49	1941069965	northern mariana islands	356	6	Northern Mariana Islands	699336394	t	-1
351	1	349	49	2704173733	palau	357	6	Palau	4234102967	t	-1
352	1	338	49	2375911104	polynesia	358	6	Polynesia	1874521764	t	-1
353	1	358	49	2990942542	american samoa	359	6	American Samoa	2968373536	t	-1
354	1	358	49	646786247	cook islands	360	6	Cook Islands	2931827329	t	-1
355	1	358	49	1170779034	french polynesia	361	6	French Polynesia	1408284255	t	-1
356	1	358	49	3526080487	niue	362	6	Niue	3406997277	t	-1
357	1	358	49	3940692583	pitcairn	363	6	Pitcairn	1978679982	t	-1
358	1	358	49	1662354353	samoa	364	6	Samoa	1046782371	t	-1
359	1	358	49	2209823413	tokelau	365	6	Tokelau	774067955	t	-1
360	1	358	49	4218756291	tonga	366	6	Tonga	2785296081	t	-1
361	1	358	49	4263870084	tuvalu	367	6	Tuvalu	230784750	t	-1
362	1	358	49	2381817393	wallis and futuna islands	368	6	Wallis and Futuna Islands	2307208971	t	-1
363	1	36	49	1874629670	tags	369	6	Tags	1988461788	t	-1
364	1	12	4	-1505074117	85c16284-c132-4e19-a1fd-74407d0e0ba5	370	6	multilingualRoot	2349380271	t	-1
365	1	15	33	3705542279	software engineering project	371	6	Software Engineering Project	2880742188	t	-1
366	1	371	33	1943382331	documentation	372	6	Documentation	362046150	t	-1
367	1	372	33	3962234048	drafts	373	6	Drafts	533652650	t	-1
368	1	372	33	3191428362	pending approval	374	6	Pending Approval	3548817466	t	-1
369	1	372	33	1748787223	published	375	6	Published	2317027443	t	-1
370	1	372	33	429021047	samples	376	6	Samples	3020307249	t	-1
371	1	376	33	2405749559	system-overview.html	377	6	system-overview.html	2428563316	t	-1
372	1	371	33	2339466083	discussions	378	6	Discussions	3381251725	t	-1
373	1	371	33	548006827	ui design	379	6	UI Design	1012196527	t	-1
374	1	371	33	1922263837	presentations	380	6	Presentations	349348064	t	-1
375	1	371	33	1750475286	quality assurance	381	6	Quality Assurance	1323478343	t	-1
376	1	16	33	3600071903	doc_info.ftl	382	6	doc_info.ftl	2304069885	t	-1
377	1	16	33	893522989	localizable.ftl	383	6	localizable.ftl	270780474	t	-1
378	1	16	33	1306378654	my_docs.ftl	384	6	my_docs.ftl	2310663079	t	-1
379	1	16	33	3127353310	my_spaces.ftl	385	6	my_spaces.ftl	1868046294	t	-1
380	1	16	33	2490960680	my_summary.ftl	386	6	my_summary.ftl	2591538330	t	-1
381	1	16	33	468580800	translatable.ftl	387	6	translatable.ftl	2551913947	t	-1
382	1	16	33	955890133	recent_docs.ftl	388	6	recent_docs.ftl	496526786	t	-1
383	1	16	33	3917212590	general_example.ftl	389	6	general_example.ftl	2546255485	t	-1
384	1	16	33	1772051304	my_docs_inline.ftl	390	6	my_docs_inline.ftl	1741567333	t	-1
385	1	16	33	647826368	show_audit.ftl	391	6	show_audit.ftl	680770674	t	-1
386	1	16	33	2183277388	readme.ftl	392	6	readme.ftl	2269293237	t	-1
387	1	17	33	3898432343	invite email templates	393	7	invite_email_templates	2118996995	t	-1
388	1	393	33	3989214391	invite_user_email.ftl	394	6	invite_user_email.ftl	34349217	t	-1
389	1	17	33	1907250866	notify email templates	395	7	notify_email_templates	3888011750	t	-1
390	1	395	33	3142229900	notify_user_email.ftl.sample	396	6	notify_user_email.ftl.sample	329696783	t	-1
391	1	17	33	3052515301	activities	397	6	activities	2962304540	t	-1
392	1	397	33	4067496925	activities-email.ftl	398	6	activities-email.ftl	3990171550	t	-1
393	1	397	33	2343757601	activities-email_fr.ftl	399	6	activities-email_fr.ftl	1529268905	t	-1
394	1	397	33	809942079	activities-email_es.ftl	400	6	activities-email_es.ftl	3771896247	t	-1
395	1	397	33	347737528	activities-email_de.ftl	401	6	activities-email_de.ftl	3291357232	t	-1
396	1	397	33	4121206868	activities-email_it.ftl	402	6	activities-email_it.ftl	623939036	t	-1
397	1	397	33	3677407752	activities-email_ja.ftl	403	6	activities-email_ja.ftl	195356544	t	-1
398	1	397	33	3090238639	activities-email_nl.ftl	404	6	activities-email_nl.ftl	1755668775	t	-1
399	1	17	33	2895700968	following email templates	405	7	following	3179229198	t	-1
400	1	405	33	496994026	following-email.html.ftl	406	6	following-email.html.ftl	4264303061	t	-1
401	1	405	33	2272498959	following-email_de.html.ftl	407	6	following-email_de.html.ftl	217400226	t	-1
402	1	405	33	1817033568	following-email_es.html.ftl	408	6	following-email_es.html.ftl	3888868813	t	-1
403	1	405	33	443397727	following-email_fr.html.ftl	409	6	following-email_fr.html.ftl	2448122098	t	-1
404	1	405	33	2223715038	following-email_it.html.ftl	410	6	following-email_it.html.ftl	252568691	t	-1
405	1	405	33	3706592883	following-email_ja.html.ftl	411	6	following-email_ja.html.ftl	1466484958	t	-1
406	1	405	33	2580145729	following-email_nl.html.ftl	412	6	following-email_nl.html.ftl	307179756	t	-1
407	1	17	33	3783950129	workflow notification	413	6	workflownotification	2825065354	t	-1
408	1	413	33	999600869	wf-email.html.ftl	414	6	invite-email.html.ftl	868286096	t	-1
409	1	413	33	2698508395	wf-email_de.html.ftl	415	6	invite-email_de.html.ftl	3257386812	t	-1
410	1	413	33	1273615876	wf-email_es.html.ftl	416	6	invite-email_es.html.ftl	689563987	t	-1
411	1	413	33	1036639035	wf-email_fr.html.ftl	417	6	invite-email_fr.html.ftl	1597629548	t	-1
412	1	413	33	2737790906	wf-email_it.html.ftl	418	6	invite-email_it.html.ftl	3252642029	t	-1
413	1	413	33	4215960343	wf-email_ja.html.ftl	419	6	invite-email_ja.html.ftl	2579123264	t	-1
414	1	413	33	3194850085	wf-email_nl.html.ftl	420	6	wf-email_nl.html.ftl	2714351462	t	-1
415	1	19	33	607514309	rss_2.0_recent_docs.ftl	421	6	RSS_2.0_recent_docs.ftl	3910367438	t	-1
416	1	21	33	2543791401	backup.js.sample	422	6	backup.js.sample	342477106	t	-1
417	1	21	33	1955080497	example test script.js.sample	423	6	example test script.js.sample	9508850	t	-1
418	1	21	33	2206926736	backup and log.js.sample	424	6	backup and log.js.sample	1614321839	t	-1
419	1	21	33	3426364731	append copyright.js.sample	425	6	append copyright.js.sample	4099172961	t	-1
420	1	21	33	1369286586	alfresco docs.js.sample	426	6	alfresco docs.js.sample	2164886066	t	-1
421	1	21	33	1270332241	test return value.js.sample	427	6	test return value.js.sample	3224464892	t	-1
422	1	14	33	1086438044	web scripts	428	6	webscripts	909597699	t	-1
423	1	428	33	271231772	readme.html	429	6	readme.html	3561914661	t	-1
424	1	428	33	2413130038	readme_ja.html	430	6	readme_ja.html	2178664068	t	-1
425	1	428	33	1148930173	readme_de.html	431	6	readme_de.html	1249247183	t	-1
426	1	428	33	3442961271	readme_fr.html	432	6	readme_fr.html	3275349189	t	-1
427	1	428	33	1914026624	org	433	6	org	528381942	t	-1
428	1	433	33	2706936736	alfresco	434	6	alfresco	3342631231	t	-1
429	1	434	33	4044060355	sample	435	6	sample	88306591	t	-1
430	1	435	33	3859521624	blogsearch.get.js	436	6	blogsearch.get.js	1827512144	t	-1
431	1	435	33	1484475657	blogsearch.get.atom.ftl	437	6	blogsearch.get.atom.ftl	2297389185	t	-1
432	1	435	33	2544799732	blogsearch.get.desc.xml	438	6	blogsearch.get.desc.xml	1195025020	t	-1
433	1	435	33	933027481	blogsearch.get.html.ftl	439	6	blogsearch.get.html.ftl	3876123409	t	-1
434	1	435	33	3721553185	blogsearch.get.html.400.ftl	440	6	blogsearch.get.html.400.ftl	1448391564	t	-1
435	1	435	33	1408605134	blogsearch.get.atom.400.ftl	441	6	blogsearch.get.atom.400.ftl	3631435107	t	-1
436	1	435	33	1711303757	categorysearch.get.js	442	6	categorysearch.get.js	2311772251	t	-1
437	1	435	33	296830842	categorysearch.get.atom.ftl	443	6	categorysearch.get.atom.ftl	2587333079	t	-1
438	1	435	33	3731129735	categorysearch.get.desc.xml	444	6	categorysearch.get.desc.xml	1440895786	t	-1
439	1	435	33	2119619818	categorysearch.get.html.ftl	445	6	categorysearch.get.html.ftl	4124049991	t	-1
440	1	435	33	1650102197	categorysearch.get.html.404.ftl	446	6	categorysearch.get.html.404.ftl	1139565194	t	-1
441	1	435	33	3967643994	categorysearch.get.atom.404.ftl	447	6	categorysearch.get.atom.404.ftl	3452673125	t	-1
442	1	435	33	3671472159	folder.get.js	448	6	folder.get.js	266975255	t	-1
443	1	435	33	1377789628	folder.get.atom.ftl	449	6	folder.get.atom.ftl	748702575	t	-1
444	1	435	33	2647303233	folder.get.desc.xml	450	6	folder.get.desc.xml	3816130962	t	-1
445	1	435	33	1039725868	folder.get.html.ftl	451	6	folder.get.html.ftl	1128782079	t	-1
446	1	14	33	395929441	web scripts extensions	452	6	extensionwebscripts	1135644710	t	-1
447	1	452	33	271231772	readme.html	453	6	readme.html	3561914661	t	-1
448	1	14	33	3839242249	models	454	7	models	3878565427	t	-1
449	1	454	60	-2830541409	397e5416-a4da-4022-9e5d-e01c5cf6d8ef	455	8	ruleFolder	864502120	t	-1
450	1	455	33	791594146	ceca5a11-5c57-11dc-ad6c-5136d620963c	456	8	rulescc915a00-5c57-11dc-ad6c-5136d620963c	3752486723	t	-1
451	1	456	67	-751000606	0c8a3846-aaae-4bf5-8ff1-88a62f20557c	457	8	action	2726102540	t	-1
452	1	457	73	-3176916505	5ea6ac79-ada7-4490-8a97-0c552d4582b8	458	9	conditions	3127092715	t	-1
453	1	458	76	-1270797715	6c308d2f-dd01-4475-bdcc-b227401c5e40	459	9	parameters	1217848508	t	-1
454	1	457	80	-3335362043	9922ceb3-b5cc-4942-9eda-f1d7cd8a95c0	460	9	actions	3055149561	t	-1
455	1	460	76	-1262585723	de2553d3-8182-43e7-a9db-93bf28b8e80a	461	9	parameters	1217848508	t	-1
456	1	14	33	3674349206	messages	462	7	messages	399009314	t	-1
457	1	14	33	663197873	web client extension	463	7	webclient_extension	3725604994	t	-1
458	1	14	33	2476626106	workflow definitions	464	7	workflow_defs	3566462828	t	-1
459	1	464	60	-1548815551	4c6dfaf1-3ee5-4a32-bf2f-deef7338ce3d	465	8	ruleFolder	864502120	t	-1
460	1	465	33	269678609	1e40c8cc-607e-11dc-af48-8b100325f217	466	8	rules1e40539b-607e-11dc-af48-8b100325f217	1892892178	t	-1
461	1	466	67	-3584289349	72b23e79-ff8f-4808-a77f-3c73b033d52b	467	8	action	2726102540	t	-1
462	1	467	73	-1914915284	0961505b-eafc-4254-88c4-c8f0c076dcee	468	9	conditions	3127092715	t	-1
463	1	468	76	-3030880821	94f3c437-3621-4727-bd24-52fa8c1cd554	469	9	parameters	1217848508	t	-1
464	1	467	80	-2251871339	88061bb8-5bf0-473a-a31c-adc32eb61fc6	470	9	actions	3055149561	t	-1
465	1	470	76	-781265879	274b7d73-7f15-427d-aab3-d4c48d6116a9	471	9	parameters	1217848508	t	-1
466	1	35	48	-3987783064	29b02151-7f92-4532-92c0-1c18e678c816	472	6	taggable	69713702	t	-1
467	1	13	33	3154160227	sites	473	10	sites	411395544	t	-1
468	1	473	33	534011694	surf-config	474	6	surf-config	3685774615	t	-1
469	1	474	33	3589315753	extensions	475	6	extensions	3499105616	t	-1
470	1	474	33	1902532204	module-deployments	476	6	module-deployments	2134325345	t	-1
471	1	30	4	-2441963324	837d03bb-a4c3-494a-9b3b-e6a09452476e	477	1	authorities	2510957676	t	-1
472	1	477	4	-329795101	8f96c04f-2feb-4e0c-a45c-1f01e30a443a	478	6	GROUP_ALFRESCO_ADMINISTRATORS	778018641	t	-1
473	1	477	4	-3357769701	6597e25f-48a9-40c6-b1fc-3a673f322f84	479	6	GROUP_EMAIL_CONTRIBUTORS	2786248885	t	-1
474	1	477	4	-3446854244	f7a50d15-ceb5-4d72-96dc-b70d3d96cf45	480	6	GROUP_SITE_ADMINISTRATORS	3539425948	t	-1
475	1	477	4	-1049830442	056224bb-bea3-4030-a149-beb6d6e0804e	481	6	GROUP_ALFRESCO_SEARCH_ADMINISTRATORS	4014781877	t	-1
476	1	477	4	-1970914376	ba4f54f9-14e6-4b5e-a2db-42deb4b26c32	482	6	GROUP_ALFRESCO_MODEL_ADMINISTRATORS	2911185014	t	-1
477	1	477	4	-364633706	9bc5b676-3d9d-474c-abb0-43920febaf07	483	6	GROUP_ALFRESCO_SYSTEM_ADMINISTRATORS	1750845467	t	-1
478	1	30	4	-1100925282	4b827095-5ef8-48a6-bd88-ca5b663770f9	484	1	zones	2314500199	t	-1
479	1	484	4	-1446059027	e1c73965-cc36-408c-a2e6-28bbb5327ba8	485	6	AUTH.ALF	2596686762	t	-1
480	1	485	92	2536743080	group_alfresco_administrators	478	6	GROUP_ALFRESCO_ADMINISTRATORS	778018641	f	-1
481	1	485	92	141606041	group_email_contributors	479	6	GROUP_EMAIL_CONTRIBUTORS	2786248885	f	-1
482	1	485	92	2655924869	group_site_administrators	480	6	GROUP_SITE_ADMINISTRATORS	3539425948	f	-1
483	1	485	92	1623073823	group_alfresco_search_administrators	481	6	GROUP_ALFRESCO_SEARCH_ADMINISTRATORS	4014781877	f	-1
484	1	485	92	2133539788	group_alfresco_model_administrators	482	6	GROUP_ALFRESCO_MODEL_ADMINISTRATORS	2911185014	f	-1
485	1	485	92	3886635441	group_alfresco_system_administrators	483	6	GROUP_ALFRESCO_SYSTEM_ADMINISTRATORS	1750845467	f	-1
486	1	484	4	-1555392212	ceaa5a71-8890-4fa8-9d1d-9ac0ef4d7471	486	6	APP.DEFAULT	3739798299	t	-1
487	1	486	92	2536743080	group_alfresco_administrators	478	6	GROUP_ALFRESCO_ADMINISTRATORS	778018641	f	-1
488	1	486	92	141606041	group_email_contributors	479	6	GROUP_EMAIL_CONTRIBUTORS	2786248885	f	-1
489	1	486	92	2655924869	group_site_administrators	480	6	GROUP_SITE_ADMINISTRATORS	3539425948	f	-1
490	1	486	92	1623073823	group_alfresco_search_administrators	481	6	GROUP_ALFRESCO_SEARCH_ADMINISTRATORS	4014781877	f	-1
491	1	486	92	2133539788	group_alfresco_model_administrators	482	6	GROUP_ALFRESCO_MODEL_ADMINISTRATORS	2911185014	f	-1
492	1	486	92	3886635441	group_alfresco_system_administrators	483	6	GROUP_ALFRESCO_SYSTEM_ADMINISTRATORS	1750845467	f	-1
493	1	478	93	3805218049	80b93068-67ad-4fba-bb45-e07da8c7084f	32	6	admin	347996256	f	-1
494	1	479	93	3805218049	80b93068-67ad-4fba-bb45-e07da8c7084f	32	6	admin	347996256	f	-1
495	1	480	93	3805218049	80b93068-67ad-4fba-bb45-e07da8c7084f	32	6	admin	347996256	f	-1
496	1	481	93	3805218049	80b93068-67ad-4fba-bb45-e07da8c7084f	32	6	admin	347996256	f	-1
497	1	482	93	3805218049	80b93068-67ad-4fba-bb45-e07da8c7084f	32	6	admin	347996256	f	-1
498	1	483	93	3805218049	80b93068-67ad-4fba-bb45-e07da8c7084f	32	6	admin	347996256	f	-1
499	1	485	92	3805218049	80b93068-67ad-4fba-bb45-e07da8c7084f	32	6	admin	347996256	f	-1
500	1	485	92	551892827	756a3630-041e-4cc1-8f54-62a940dad589	33	6	guest	805803811	f	-1
501	1	486	92	3805218049	80b93068-67ad-4fba-bb45-e07da8c7084f	32	6	admin	347996256	f	-1
502	1	486	92	551892827	756a3630-041e-4cc1-8f54-62a940dad589	33	6	guest	805803811	f	-1
503	1	30	4	-2719258229	ba88bb93-b1b7-4df4-a327-9ae1c97a5c50	487	1	remote_credentials	3212737544	t	-1
504	1	30	4	-4267307119	772889be-ce39-472f-a4eb-bc706ce99137	488	1	syncset_definitions	4198702871	t	-1
505	1	14	33	3181060681	imap configs	489	7	imap_configs	2749296655	t	-1
506	1	489	33	1864924558	templates	490	7	imap_templates	4290309844	t	-1
507	1	490	33	1875732140	emailbody-textplain.ftl	491	6	emailbody-textplain.ftl	3210300196	t	-1
508	1	490	33	3090409056	emailbody-texthtml.ftl	492	6	emailbody-texthtml.ftl	1275632005	t	-1
509	1	14	33	2150250776	transfers	493	7	transfers	1290437877	t	-1
510	1	493	33	1570715444	transfer target groups	494	7	transfer_groups	957030808	t	-1
511	1	494	33	2123542495	default group	495	6	default	2172081413	t	-1
512	1	493	33	3801670724	inbound transfer records	496	7	inbound_transfer_records	310355882	t	-1
513	1	493	33	190023114	temp	497	7	temp	3957740328	t	-1
514	1	14	33	1529518448	rendering actions space	498	7	rendering_actions	1996713548	t	-1
515	1	14	33	3343785569	replication actions space	499	7	replication_actions	2660722499	t	-1
516	1	495	60	-3322839897	45c1215c-cb7a-41da-a05c-d52f591fabd0	500	8	ruleFolder	864502120	t	-1
517	1	500	33	2750781699	2969804d-45b0-4f1f-97be-a8da4306a0b7	501	8	rules3245de8b-2cfe-42ed-8f8b-44089f99b265	3083479138	t	-1
518	1	501	67	-3163311210	8d5cadc9-04f6-4279-b725-aebb8e22438d	502	8	action	2726102540	t	-1
519	1	502	102	-3642010692	991b40ec-07ab-40bb-88f6-c0e04c8d5e3e	503	9	actionFolder	2614599834	t	-1
520	1	502	80	-2072992125	a077e509-c518-4e3c-9009-aa150c8d15f8	504	9	actions	3055149561	t	-1
521	1	504	76	-647627955	9752cd35-56a0-4c9f-bd50-4c97fca6f5d5	505	9	parameters	1217848508	t	-1
522	1	502	73	-1217736382	af5e5fac-8544-4aaa-a52f-9305b5701393	506	9	conditions	3127092715	t	-1
523	1	506	76	-2823906715	219e8952-2829-4ae6-ad56-56db096059b3	507	9	parameters	1217848508	t	-1
524	1	14	33	352265367	scheduled actions	508	6	Scheduled Actions	2131169529	t	-1
525	1	18	33	445231378	new-user-email.html.ftl	509	6	new-user-email.html.ftl	3390938266	t	-1
526	1	18	33	3312025941	new-user-email_fr.html.ftl	510	6	new-user-email_fr.html.ftl	4245145103	t	-1
527	1	18	33	3007941738	new-user-email_es.html.ftl	511	6	new-user-email_es.html.ftl	2334640944	t	-1
528	1	18	33	1484235269	new-user-email_de.html.ftl	512	6	new-user-email_de.html.ftl	1612309855	t	-1
529	1	18	33	1536099796	new-user-email_it.html.ftl	513	6	new-user-email_it.html.ftl	1675707022	t	-1
530	1	18	33	65677689	new-user-email_ja.html.ftl	514	6	new-user-email_ja.html.ftl	998534691	t	-1
531	1	18	33	1187893579	new-user-email_nl.html.ftl	515	6	new-user-email_nl.html.ftl	2124650001	t	-1
532	1	18	33	3691661958	invite-email.html.ftl	516	6	invite-email.html.ftl	868286096	t	-1
533	1	18	33	3163192147	invite-email_fr.html.ftl	517	6	invite-email_fr.html.ftl	1597629548	t	-1
534	1	18	33	3400169068	invite-email_es.html.ftl	518	6	invite-email_es.html.ftl	689563987	t	-1
535	1	18	33	563369987	invite-email_de.html.ftl	519	6	invite-email_de.html.ftl	3257386812	t	-1
536	1	18	33	577568722	invite-email_it.html.ftl	520	6	invite-email_it.html.ftl	3252642029	t	-1
537	1	18	33	2047464319	invite-email_ja.html.ftl	521	6	invite-email_ja.html.ftl	2579123264	t	-1
538	1	18	33	1059990349	invite-email_nl.html.ftl	522	6	invite-email_nl.html.ftl	3701322866	t	-1
539	1	18	33	1305785334	invite-email-add-direct.html.ftl	523	6	invite-email-add-direct.html.ftl	4220725286	t	-1
540	1	18	33	3071386897	invite-email-add-direct.html_fr.ftl	524	6	invite-email-add-direct_fr.html.ftl	3152676049	t	-1
541	1	18	33	216399375	invite-email-add-direct.html_es.ftl	525	6	invite-email-add-direct_es.html.ftl	3452570094	t	-1
542	1	18	33	672747400	invite-email-add-direct.html_de.ftl	526	6	invite-email-add-direct_de.html.ftl	653535105	t	-1
543	1	18	33	3372737124	invite-email-add-direct.html_it.ftl	527	6	invite-email-add-direct_it.html.ftl	621576272	t	-1
544	1	18	33	3885205560	invite-email-add-direct.html_ja.ftl	528	6	invite-email-add-direct_ja.html.ftl	2104077565	t	-1
545	1	18	33	2224164511	invite-email-add-direct.html_nl.ftl	529	6	invite-email-add-direct_nl.html.ftl	944657615	t	-1
546	1	18	33	2574136958	invite-email-moderated.html.ftl	530	6	invite-email-moderated.html.ftl	3101219649	t	-1
547	1	395	33	4141493435	notify_user_email.html.ftl	531	6	notify_user_email.html.ftl	3467930593	t	-1
548	1	395	33	3092459551	notify_user_email_de.html.ftl	532	6	notify_user_email_de.html.ftl	3427405532	t	-1
549	1	395	33	1399660144	notify_user_email_es.html.ftl	533	6	notify_user_email_es.html.ftl	661921971	t	-1
550	1	395	33	625808207	notify_user_email_fr.html.ftl	534	6	notify_user_email_fr.html.ftl	1364471180	t	-1
551	1	395	33	3148584910	notify_user_email_it.html.ftl	535	6	notify_user_email_it.html.ftl	3484592397	t	-1
552	1	395	33	3821976419	notify_user_email_ja.html.ftl	536	6	notify_user_email_ja.html.ftl	2547484064	t	-1
553	1	395	33	2800304977	notify_user_email_nl.html.ftl	537	6	notify_user_email_nl.html.ftl	3538976146	t	-1
554	1	490	33	2852742012	emailbody_textplain_share.ftl	538	6	emailbody_textplain_share.ftl	3725614527	t	-1
555	1	490	33	1349507622	emailbody_textplain_alfresco.ftl	539	6	emailbody_textplain_alfresco.ftl	3861398006	t	-1
556	1	490	33	1214296187	emailbody_texthtml_alfresco.ftl	540	6	emailbody_texthtml_alfresco.ftl	1775656260	t	-1
557	1	490	33	2597111155	emailbody_texthtml_share.ftl	541	6	emailbody_texthtml_share.ftl	840998128	t	-1
558	1	490	33	3185687119	emailbody_textplain_share_de.ftl	542	6	emailbody_textplain_share_de.ftl	195436959	t	-1
559	1	490	33	1293729871	emailbody_textplain_alfresco_de.ftl	543	6	emailbody_textplain_alfresco_de.ftl	3130552875	t	-1
560	1	490	33	651551496	emailbody_texthtml_alfresco_de.ftl	544	6	emailbody_texthtml_alfresco_de.ftl	3224427164	t	-1
561	1	490	33	2783891474	emailbody_texthtml_share_de.ftl	545	6	emailbody_texthtml_share_de.ftl	2220375341	t	-1
562	1	490	33	2568817608	emailbody_textplain_share_es.ftl	546	6	emailbody_textplain_share_es.ftl	794464280	t	-1
563	1	490	33	1776316872	emailbody_textplain_alfresco_es.ftl	547	6	emailbody_textplain_alfresco_es.ftl	2657403820	t	-1
564	1	490	33	36262543	emailbody_texthtml_alfresco_es.ftl	548	6	emailbody_texthtml_alfresco_es.ftl	3838602011	t	-1
565	1	490	33	2165465493	emailbody_texthtml_share_es.ftl	549	6	emailbody_texthtml_share_es.ftl	2695138474	t	-1
566	1	490	33	585715926	emailbody_textplain_share_fr.ftl	550	6	emailbody_textplain_share_fr.ftl	2494483206	t	-1
567	1	490	33	3524537046	emailbody_textplain_alfresco_fr.ftl	551	6	emailbody_textplain_alfresco_fr.ftl	630233266	t	-1
568	1	490	33	3118270865	emailbody_texthtml_alfresco_fr.ftl	552	6	emailbody_texthtml_alfresco_fr.ftl	1597517829	t	-1
569	1	490	33	988166795	emailbody_texthtml_share_fr.ftl	553	6	emailbody_texthtml_share_fr.ftl	458282932	t	-1
570	1	490	33	1560221603	emailbody_textplain_share_it.ftl	554	6	emailbody_textplain_share_it.ftl	3937963123	t	-1
571	1	490	33	2885840291	emailbody_textplain_alfresco_it.ftl	555	6	emailbody_textplain_alfresco_it.ftl	1535565767	t	-1
572	1	490	33	3351989988	emailbody_texthtml_alfresco_it.ftl	556	6	emailbody_texthtml_alfresco_it.ftl	556693360	t	-1
573	1	490	33	1156611582	emailbody_texthtml_share_it.ftl	557	6	emailbody_texthtml_share_it.ftl	1699123393	t	-1
677	1	639	33	4004676575	sample 3.png	645	6	sample 3.png	2977132541	t	-1
574	1	490	33	1919647231	emailbody_textplain_share_ja.ftl	558	6	emailbody_textplain_share_ja.ftl	3291276847	t	-1
575	1	490	33	2190870527	emailbody_textplain_alfresco_ja.ftl	559	6	emailbody_textplain_alfresco_ja.ftl	1964160411	t	-1
576	1	490	33	3915326648	emailbody_texthtml_alfresco_ja.ftl	560	6	emailbody_texthtml_alfresco_ja.ftl	263855404	t	-1
577	1	490	33	1784944546	emailbody_texthtml_share_ja.ftl	561	6	emailbody_texthtml_share_ja.ftl	1272117917	t	-1
578	1	490	33	2925151545	emailbody_textplain_share_nb.ftl	562	6	emailbody_textplain_share_nb.ftl	404610793	t	-1
579	1	490	33	1588019001	emailbody_textplain_alfresco_nb.ftl	563	6	emailbody_textplain_alfresco_nb.ftl	2837679453	t	-1
580	1	490	33	896408702	emailbody_texthtml_alfresco_nb.ftl	564	6	emailbody_texthtml_alfresco_nb.ftl	3549111786	t	-1
581	1	490	33	3059036004	emailbody_texthtml_share_nb.ftl	565	6	emailbody_texthtml_share_nb.ftl	2548286043	t	-1
582	1	490	33	292223832	emailbody_textplain_share_nl.ftl	566	6	emailbody_textplain_share_nl.ftl	2804768904	t	-1
583	1	490	33	3784802648	emailbody_textplain_alfresco_nl.ftl	567	6	emailbody_textplain_alfresco_nl.ftl	370357052	t	-1
584	1	490	33	2321457695	emailbody_texthtml_alfresco_nl.ftl	568	6	emailbody_texthtml_alfresco_nl.ftl	1824234379	t	-1
585	1	490	33	157651205	emailbody_texthtml_share_nl.ftl	569	6	emailbody_texthtml_share_nl.ftl	684948538	t	-1
586	1	490	33	541294538	emailbody_textplain_share_pt_br.ftl	570	6	emailbody_textplain_share_pt_BR.ftl	296413084	t	-1
587	1	490	33	916414695	emailbody_textplain_alfresco_pt_br.ftl	571	6	emailbody_textplain_alfresco_pt_BR.ftl	2198512349	t	-1
588	1	490	33	526120636	emailbody_texthtml_alfresco_pt_br.ftl	572	6	emailbody_texthtml_alfresco_pt_BR.ftl	4257765129	t	-1
589	1	490	33	1267358861	emailbody_texthtml_share_pt_br.ftl	573	6	emailbody_texthtml_share_pt_BR.ftl	1795313451	t	-1
590	1	490	33	143576139	emailbody_textplain_share_ru.ftl	574	6	emailbody_textplain_share_ru.ftl	3200866203	t	-1
591	1	490	33	4168332875	emailbody_textplain_alfresco_ru.ftl	575	6	emailbody_textplain_alfresco_ru.ftl	267846703	t	-1
592	1	490	33	2478478604	emailbody_texthtml_alfresco_ru.ftl	576	6	emailbody_texthtml_alfresco_ru.ftl	1969204376	t	-1
593	1	490	33	276932118	emailbody_texthtml_share_ru.ftl	577	6	emailbody_texthtml_share_ru.ftl	825707305	t	-1
594	1	490	33	4030035990	emailbody_textplain_share_zh_cn.ftl	578	6	emailbody_textplain_share_zh_CN.ftl	3252478016	t	-1
595	1	490	33	3874055995	emailbody_textplain_alfresco_zh_cn.ftl	579	6	emailbody_textplain_alfresco_zh_CN.ftl	1400677633	t	-1
596	1	490	33	3475894624	emailbody_texthtml_alfresco_zh_cn.ftl	580	6	emailbody_texthtml_alfresco_zh_CN.ftl	767447253	t	-1
597	1	490	33	2617006929	emailbody_texthtml_share_zh_cn.ftl	581	6	emailbody_texthtml_share_zh_CN.ftl	3144960247	t	-1
598	1	30	4	-2622526885	0336ff8d-671f-44b4-ae76-9e6fcab6762e	582	1	downloads	3307032186	t	-1
599	1	23	33	2732812139	smartfoldersexample.json	583	13	smartfolder.sample	3192301734	t	-1
600	1	21	33	1918060495	start-pooled-review-workflow.js	584	6	example.js	1403687563	t	-1
602	1	477	4	-1927997677	a6a3e03d-8686-4966-8136-064d0feaff90	586	6	GROUP_site_swsdp	3760915499	t	-1
603	1	484	4	-372067986	8f831b1e-fc91-44c8-a5de-97dba4d3ae6f	587	6	APP.SHARE	3658694637	t	-1
604	1	485	92	3284435889	4f78dc6e000a27fc3bb7e9c70aa6b5d9	586	6	GROUP_site_swsdp	3760915499	f	-1
605	1	587	92	3284435889	4f78dc6e000a27fc3bb7e9c70aa6b5d9	586	6	GROUP_site_swsdp	3760915499	f	-1
606	1	477	4	-1331687168	fa9e778c-30c5-4b6f-b3d2-4574f59a66f8	588	6	GROUP_site_swsdp_SiteManager	819207528	t	-1
607	1	485	92	2601610584	9e5cb3fa1850083495559ca2a4ca2de9	588	6	GROUP_site_swsdp_SiteManager	819207528	f	-1
608	1	587	92	2601610584	9e5cb3fa1850083495559ca2a4ca2de9	588	6	GROUP_site_swsdp_SiteManager	819207528	f	-1
609	1	586	93	2601610584	9e5cb3fa1850083495559ca2a4ca2de9	588	6	GROUP_site_swsdp_SiteManager	819207528	f	-1
610	1	477	4	-3147242010	d9ea8746-3943-4b2f-b8c0-30497d935ed1	589	6	GROUP_site_swsdp_SiteCollaborator	1706459855	t	-1
611	1	485	92	1727778803	58d3dfc926fbcb0ce0a1213c37dc4711	589	6	GROUP_site_swsdp_SiteCollaborator	1706459855	f	-1
612	1	587	92	1727778803	58d3dfc926fbcb0ce0a1213c37dc4711	589	6	GROUP_site_swsdp_SiteCollaborator	1706459855	f	-1
613	1	586	93	1727778803	58d3dfc926fbcb0ce0a1213c37dc4711	589	6	GROUP_site_swsdp_SiteCollaborator	1706459855	f	-1
614	1	477	4	-3126463018	9707147a-aeae-4aa0-a4e1-05ff70e70295	590	6	GROUP_site_swsdp_SiteContributor	32651092	t	-1
615	1	485	92	2523597308	5b487fd6a02f7430721726163ba0daa9	590	6	GROUP_site_swsdp_SiteContributor	32651092	f	-1
616	1	587	92	2523597308	5b487fd6a02f7430721726163ba0daa9	590	6	GROUP_site_swsdp_SiteContributor	32651092	f	-1
617	1	586	93	2523597308	5b487fd6a02f7430721726163ba0daa9	590	6	GROUP_site_swsdp_SiteContributor	32651092	f	-1
618	1	477	4	-202860619	94aa5dff-0b87-4952-9588-21ef09b1df49	591	6	GROUP_site_swsdp_SiteConsumer	2168792413	t	-1
619	1	485	92	2118386964	73714588eb587e2a207a436130080c9e	591	6	GROUP_site_swsdp_SiteConsumer	2168792413	f	-1
620	1	587	92	2118386964	73714588eb587e2a207a436130080c9e	591	6	GROUP_site_swsdp_SiteConsumer	2168792413	f	-1
760	1	8	4	-3594204013	e370fc3c-706d-434c-b587-344dc772f2ae	728	19	modules	462710347	t	-1
621	1	586	93	2118386964	73714588eb587e2a207a436130080c9e	591	6	GROUP_site_swsdp_SiteConsumer	2168792413	f	-1
622	1	588	93	3805218049	80b93068-67ad-4fba-bb45-e07da8c7084f	32	6	admin	347996256	f	-1
623	1	3	4	-906530853	71592e5d-7669-498a-8e88-019912d638c7	592	2	abeecher	4294529677	t	-1
624	1	3	4	-289199221	76fe49bd-5060-4eb6-a440-9fb770a0f7ea	593	2	mjackson	3024187048	t	-1
625	1	31	4	-1113514266	4d701f96-9e0d-45cd-ba5f-5dc19b039179	594	6	abeecher	3272809292	t	-1
626	1	594	137	-1000516660	813bd670-9c96-49d7-b9e7-ba4284d1687b	595	6	abeecher-avatar.jpg	1156794817	t	-1
627	1	595	142	-3984665296	d354537d-5b91-4690-b6f1-107ff8f985b1	596	6	avatar	3795810163	t	-1
628	1	31	4	-3286256592	0f9c1357-9bcf-43c8-a7cf-dd25f0a476a7	597	6	mjackson	2292918121	t	-1
629	1	597	137	-2800456024	9b9cc31a-87c9-4e96-9b05-96ade7732676	598	6	mjackson-avatar.jpg	1186772188	t	-1
630	1	598	142	-1152519222	5fc46e63-f82e-4cc3-9467-74716d0317c1	599	6	avatar	3795810163	t	-1
631	1	589	93	3063427348	dc103838-645f-43c1-8a2a-bc187e13c343	594	6	abeecher	3272809292	f	-1
632	1	588	93	504535620	b6d80d49-21cc-4f04-9c92-e7063037543f	597	6	mjackson	2292918121	f	-1
633	1	473	33	2308701361	swsdp	601	6	swsdp	355201447	t	-1
634	1	601	33	3872509529	documentlibrary	602	6	documentLibrary	202189778	t	-1
635	1	602	33	4069355619	agency files	603	6	Agency Files	1958504637	t	-1
636	1	603	33	156281203	contracts	604	6	Contracts	3950493975	t	-1
637	1	604	33	3266167157	project contract.pdf	605	6	Project Contract.pdf	3754194629	t	-1
638	1	10	164	4047806828	1a0b110f-1e09-4ca2-b367-fe25e4964a4e	606	5	1a0b110f-1e09-4ca2-b367-fe25e4964a4e	2976416666	t	-1
639	1	606	166	-683583580	2fee3f85-8343-412c-bc60-83a9455926d9	607	5	version-0	3486964613	t	-1
640	1	605	142	-1259516542	2917d267-5472-41c2-925c-cb2d5e7c592b	608	6	doclib	2991633180	t	-1
641	1	605	142	-4283533258	f41ed96e-cbf5-45bd-8cab-9d58463eb2af	609	6	webpreview	1387062285	t	-1
642	1	603	33	3760176746	images	610	6	Images	335265280	t	-1
643	1	610	33	3788176685	coins.jpg	611	6	coins.JPG	398153056	t	-1
644	1	611	142	-2491975305	ad2ebda5-29b1-4fc6-bc25-2a407006e169	612	6	doclib	2991633180	t	-1
645	1	610	33	1184564164	graph.jpg	613	6	graph.JPG	2968169353	t	-1
646	1	613	142	-400231419	92b0de4b-030c-4994-bbe5-aa802cda6236	614	6	doclib	2991633180	t	-1
647	1	610	33	3611285497	grass.jpg	615	6	grass.jpg	3085516094	t	-1
648	1	615	142	-2017431245	6b95a4c2-e718-4db9-8141-1e477e51602d	616	6	doclib	2991633180	t	-1
649	1	615	142	-1820161258	fed64ecd-4f97-4e20-b2aa-32a9d66e9ea2	617	6	imgpreview	2566125866	t	-1
650	1	610	33	3436769363	money.jpg	618	6	money.JPG	984176670	t	-1
651	1	618	142	-1357696292	a7f2a312-705f-498f-9ac9-882df60d4def	619	6	doclib	2991633180	t	-1
652	1	610	33	2105090824	plugs.jpg	620	6	plugs.jpg	498059727	t	-1
653	1	620	142	-3611705946	d6041811-8f0d-4bf6-8082-b3e4744b4479	621	6	doclib	2991633180	t	-1
654	1	610	33	4257289731	turbine.jpg	622	6	turbine.JPG	2936176304	t	-1
655	1	622	142	-535193816	649d6b44-3b03-46df-bcdd-dc756b626011	623	6	doclib	2991633180	t	-1
656	1	610	33	3650608928	wires.jpg	624	6	wires.JPG	803633005	t	-1
657	1	624	142	-293576713	49f65385-e1b2-42d5-8670-6160b91df634	625	6	doclib	2991633180	t	-1
658	1	610	33	4245898435	wind turbine.jpg	626	6	wind turbine.JPG	3896697426	t	-1
659	1	626	142	-1168335318	579c5c4e-1b61-4b89-b4b4-d50fe1d88ff9	627	6	doclib	2991633180	t	-1
660	1	610	33	3010006787	header.png	628	6	header.png	3054079738	t	-1
661	1	628	142	-1922140728	25c883e5-dba7-499e-9574-fe5bdcfba2ed	629	6	doclib	2991633180	t	-1
662	1	610	33	2227112316	windmill.png	630	6	windmill.png	3682526558	t	-1
663	1	630	142	-2285545105	f65bc6e7-81ea-49ea-8d02-5bc2280d18d4	631	6	doclib	2991633180	t	-1
664	1	610	33	4117066625	low consumption bulb.png	632	6	low consumption bulb.png	383184062	t	-1
665	1	632	142	-2582807787	e0c0f339-065d-4479-9580-0741bbe605e5	633	6	doclib	2991633180	t	-1
666	1	603	33	3420513569	logo files	634	6	Logo Files	1075595276	t	-1
667	1	634	33	1875520457	ge logo.png	635	6	GE Logo.png	2602168225	t	-1
668	1	635	142	-1028879446	a69b105a-e13d-480e-9655-5f27a631f548	636	6	doclib	2991633180	t	-1
669	1	634	33	2915011424	logo.png	637	6	logo.png	3420166655	t	-1
670	1	637	142	-4212983759	5f5738f8-becd-48ef-a517-5469da9f0b14	638	6	doclib	2991633180	t	-1
671	1	603	33	3588352399	mock-ups	639	6	Mock-Ups	1925111718	t	-1
672	1	639	33	2490552511	sample 1.png	640	6	sample 1.png	3417521309	t	-1
673	1	640	142	-745327009	efb21ccc-20fd-4d43-8b5a-60df8f87fb41	641	6	doclib	2991633180	t	-1
674	1	639	33	3553798767	sample 2.png	642	6	sample 2.png	2350073421	t	-1
675	1	642	142	-680006257	7e6f014f-bed1-4bb5-a8f4-6021310a6582	643	6	doclib	2991633180	t	-1
676	1	642	142	-3397220099	232f1d5f-f4cd-4c20-aa25-30f2274ed89f	644	6	imgpreview	2566125866	t	-1
678	1	645	142	-1789868030	e590695d-ddf5-4f8d-af7c-2a2ff788e040	646	6	doclib	2991633180	t	-1
679	1	645	142	-650848317	d84cbe13-56f7-4832-b0b1-2a2c38c05c61	647	6	imgpreview	2566125866	t	-1
680	1	603	33	1931736815	video files	648	6	Video Files	4028267525	t	-1
681	1	648	33	2956299995	websitereview.mp4	649	6	WebSiteReview.mp4	2521430049	t	-1
682	1	602	33	2538629819	budget files	650	6	Budget Files	291598949	t	-1
683	1	650	33	1781477269	invoices	651	6	Invoices	4114440028	t	-1
684	1	651	33	2109261446	inv i200-109.png	652	6	inv I200-109.png	3867146597	t	-1
685	1	652	142	-1075258331	f4b1efb9-373c-4542-9c68-b69604fed4cc	653	6	doclib	2991633180	t	-1
686	1	651	33	2448114923	inv i200-189.png	654	6	inv I200-189.png	170687240	t	-1
687	1	654	142	-3370695471	16e6aab7-9cc4-4e7d-99ab-aa27bcca6710	655	6	doclib	2991633180	t	-1
688	1	650	33	3627867045	budget.xls	656	6	budget.xls	3713817180	t	-1
689	1	656	142	-2698940877	9926bc6d-b86b-480d-ac12-03ec77b66e7e	657	6	doclib	2991633180	t	-1
690	1	656	142	-1599364837	ecfb9d6a-a585-4b81-b97e-2cf7799e16a9	658	6	webpreview	1387062285	t	-1
691	1	656	196	4094595993	budget.xls discussion	659	16	discussion	2764908846	t	-1
692	1	659	33	1604228650	comments	660	6	Comments	3230459619	t	-1
693	1	660	33	4186633902	comment-1297852210661_622	661	6	comment-1297852210661_622	1326409740	t	-1
694	1	602	33	2472434584	meeting notes	662	6	Meeting Notes	887013217	t	-1
695	1	662	33	554498960	meeting notes 2011-01-27.doc	663	6	Meeting Notes 2011-01-27.doc	2778943928	t	-1
696	1	663	142	-3788726341	d50066e6-1069-4ed4-b321-e4ed53f07851	664	6	doclib	2991633180	t	-1
697	1	663	142	-1764277282	ad7c4a14-9604-429a-a311-d789b2e7c93a	665	6	webpreview	1387062285	t	-1
698	1	662	33	399244216	meeting notes 2011-02-03.doc	666	6	Meeting Notes 2011-02-03.doc	2472825232	t	-1
699	1	666	142	-615172406	7f8c7985-f8dc-4a73-958f-34439a8e2214	667	6	doclib	2991633180	t	-1
700	1	666	142	-2653623998	48276979-cfb1-4719-adc4-54550e4d0443	668	6	webpreview	1387062285	t	-1
701	1	662	33	2604094157	meeting notes 2011-02-10.doc	669	6	Meeting Notes 2011-02-10.doc	530118885	t	-1
702	1	669	142	-1365155082	e598e738-60d1-475e-accc-7470dba5db95	670	6	doclib	2991633180	t	-1
703	1	669	142	-111037626	4717e703-07c9-495d-97d9-264a26ebbbec	671	6	webpreview	1387062285	t	-1
704	1	602	33	1922263837	presentations	672	6	Presentations	349348064	t	-1
705	1	672	33	1616623070	project objectives.ppt	673	6	Project Objectives.ppt	3516604448	t	-1
706	1	673	142	-4066991567	1ee18e33-1a6f-4647-8fbb-e03e25934edb	674	6	doclib	2991633180	t	-1
707	1	673	142	-1604637524	79ae00ef-f197-4791-b469-96b95bb3aef3	675	6	webpreview	1387062285	t	-1
708	1	672	33	1655540618	project overview.ppt	676	6	Project Overview.ppt	2143566906	t	-1
709	1	676	142	-1542720669	155bcae1-fc66-4e93-bdc9-4621a150d21e	677	6	doclib	2991633180	t	-1
710	1	676	142	-4289511309	af5ddb41-1cab-4187-993e-c4bba0bf816b	678	6	webpreview	1387062285	t	-1
711	1	601	33	3514999064	links	679	6	links	1295166478	t	-1
712	1	679	33	2679163169	link-1297806194371_850	680	6	link-1297806194371_850	1804318404	t	-1
713	1	679	33	358194127	link-1297806244007_178	681	6	link-1297806244007_178	3781354538	t	-1
714	1	601	33	3997967452	datalists	682	6	dataLists	1331421599	t	-1
715	1	682	33	260683937	71824d77-9cd8-44c3-b3e4-dbca7e17dc49	683	6	71824d77-9cd8-44c3-b3e4-dbca7e17dc49	1168218450	t	-1
716	1	683	33	3513423167	e6fc15e9-5caf-4f17-857e-7b0cfbc655a9	684	6	e6fc15e9-5caf-4f17-857e-7b0cfbc655a9	2604840140	t	-1
717	1	683	33	64120555	42fcbae6-b1fe-4028-9f85-9ad7f81a8e3b	685	6	42fcbae6-b1fe-4028-9f85-9ad7f81a8e3b	1241138968	t	-1
718	1	683	33	2622673710	66028f46-c074-4cf5-9f37-8490e51ca540	686	6	66028f46-c074-4cf5-9f37-8490e51ca540	3598381789	t	-1
719	1	683	33	1496050538	50046ccd-9034-420f-925b-0530836488c4	687	6	50046ccd-9034-420f-925b-0530836488c4	319015577	t	-1
720	1	682	33	734398789	aea88103-517e-4aa0-a3be-de258d0e6465	688	6	aea88103-517e-4aa0-a3be-de258d0e6465	1642997942	t	-1
721	1	688	33	3109944928	9198bd31-a664-4584-a271-b529daf4793b	689	6	9198bd31-a664-4584-a271-b529daf4793b	4084588435	t	-1
722	1	688	33	138801921	eb1c2fda-4868-4384-b29e-78c01b6601ec	690	6	eb1c2fda-4868-4384-b29e-78c01b6601ec	1114493682	t	-1
723	1	688	33	2512162572	35b8be80-170f-40af-a173-513758b83165	691	6	35b8be80-170f-40af-a173-513758b83165	3751063295	t	-1
724	1	688	33	2362257682	567ee439-4ebc-40cf-a783-3e561ad5a605	692	6	567ee439-4ebc-40cf-a783-3e561ad5a605	3336900833	t	-1
725	1	688	33	3797031659	7a0bb872-bf7c-457b-831e-95f94efb9816	693	6	7a0bb872-bf7c-457b-831e-95f94efb9816	2826582808	t	-1
726	1	601	33	583916550	wiki	694	6	wiki	2613470146	t	-1
727	1	694	33	4105798722	main_page	695	6	Main_Page	3064742680	t	-1
728	1	10	164	417166766	d6f3a279-ce86-4a12-8985-93b71afbb71d	696	5	d6f3a279-ce86-4a12-8985-93b71afbb71d	1492211544	t	-1
729	1	696	166	-3005261724	8ee1151a-3b9d-4341-ae30-b255bfe81412	697	5	version-0	3486964613	t	-1
730	1	694	33	1157518050	meetings	698	6	Meetings	3689685547	t	-1
731	1	10	164	955032534	1373739a-2849-4647-9e97-7a4e05cc5841	699	5	1373739a-2849-4647-9e97-7a4e05cc5841	2025881888	t	-1
732	1	699	166	-854933671	fa9687f6-acd8-443e-b6a0-5a587663fc95	700	5	version-0	3486964613	t	-1
733	1	694	33	418873732	milestones	701	6	Milestones	1388970925	t	-1
734	1	10	164	1094547242	3c73aace-9f54-420d-a1c0-c54b6a116dcf	702	5	3c73aace-9f54-420d-a1c0-c54b6a116dcf	17913308	t	-1
735	1	702	166	-4142507388	7fa266c8-2207-4b30-8dd3-dd6055dab9ba	703	5	version-0	3486964613	t	-1
736	1	601	33	2339466083	discussions	704	6	discussions	1326793050	t	-1
737	1	704	33	1324234952	post-1297807546884_964	705	6	post-1297807546884_964	3134539565	t	-1
738	1	705	33	1324234952	post-1297807546884_964	706	6	post-1297807546884_964	3134539565	t	-1
739	1	705	33	2030775859	post-1297807619797_315	707	6	post-1297807619797_315	2368754134	t	-1
740	1	705	33	1963213525	post-1297807729794_112	708	6	post-1297807729794_112	2168405296	t	-1
741	1	705	33	1848061172	post-1297807767790_183	709	6	post-1297807767790_183	2585547537	t	-1
742	1	704	33	3424006769	post-1297807581026_873	710	6	post-1297807581026_873	942493076	t	-1
743	1	710	33	3424006769	post-1297807581026_873	711	6	post-1297807581026_873	942493076	t	-1
744	1	710	33	881747235	post-1297807650635_649	712	6	post-1297807650635_649	3233094342	t	-1
745	1	601	33	534011694	surf-config	713	6	surf-config	3685774615	t	-1
746	1	713	33	544531829	pages	714	6	pages	3167021155	t	-1
747	1	714	33	1766001124	site	715	6	site	3494426144	t	-1
748	1	715	33	2308701361	swsdp	716	6	swsdp	355201447	t	-1
749	1	716	33	631660036	dashboard.xml	717	6	dashboard.xml	4036615692	t	-1
750	1	713	33	3997758973	components	718	6	components	3945296900	t	-1
751	1	718	33	1334852793	page.component-1-1.site~swsdp~dashboard.xml	719	6	page.component-1-1.site~swsdp~dashboard.xml	3555596018	t	-1
752	1	718	33	1524266966	page.component-1-3.site~swsdp~dashboard.xml	720	6	page.component-1-3.site~swsdp~dashboard.xml	3332660637	t	-1
753	1	718	33	1296972702	page.component-2-1.site~swsdp~dashboard.xml	721	6	page.component-2-1.site~swsdp~dashboard.xml	3509588437	t	-1
754	1	718	33	3206141542	page.component-2-2.site~swsdp~dashboard.xml	722	6	page.component-2-2.site~swsdp~dashboard.xml	594017325	t	-1
755	1	718	33	1476686065	page.component-2-3.site~swsdp~dashboard.xml	723	6	page.component-2-3.site~swsdp~dashboard.xml	3296352954	t	-1
756	1	718	33	3046069903	page.navigation.site~swsdp~dashboard.xml	724	6	page.navigation.site~swsdp~dashboard.xml	3548890421	t	-1
757	1	718	33	861589342	page.title.site~swsdp~dashboard.xml	725	6	page.title.site~swsdp~dashboard.xml	3302915386	t	-1
758	1	718	33	3183989057	page.component-1-2.site~swsdp~dashboard.xml	726	6	page.component-1-2.site~swsdp~dashboard.xml	565836554	t	-1
759	1	718	33	2182679792	page.component-1-4.site~swsdp~dashboard.xml	727	6	page.component-1-4.site~swsdp~dashboard.xml	510076603	t	-1
761	1	728	4	-4031825140	daa9232c-ead5-457c-a3db-b37baab297cd	729	19	alfresco-aos-module	2155693883	t	-1
762	1	728	4	-2043891819	34468226-e7af-46e0-8e2c-cf978e854bf3	730	19	org.alfresco.integrations.google.docs	3196163045	t	-1
763	1	728	4	-3881764946	9dab81d4-ae8e-4d8c-9a5b-d9d6404ce3d9	731	19	org_alfresco_device_sync_repo	3706539048	t	-1
764	1	728	4	-1821359285	b5891614-9edc-49ee-bf18-b73979945dd0	732	19	alfresco-share-services	3303942366	t	-1
765	1	728	4	-2615615155	d33cf3dd-b119-4055-880c-fbdbdb539d0b	733	19	alfresco-trashcan-cleaner	2285549386	t	-1
766	1	498	33	2096162074	d1808b0e-2501-4543-872d-830a21aa2f36	734	6	imgpreview	2566125866	t	-1
767	1	734	76	-2155121358	74aeae1b-8e62-4358-a76a-fd63fe6a8829	735	9	parameters	1217848508	t	-1
768	1	734	76	-1383676581	c67bb387-f116-4e5b-a945-d7d9ee76876e	736	9	parameters	1217848508	t	-1
769	1	734	76	-3595407527	85686a78-fa59-43b6-8200-ff43167d1b89	737	9	parameters	1217848508	t	-1
770	1	734	76	-751959379	1284534d-38ec-43b5-9a8f-d57de3cf180c	738	9	parameters	1217848508	t	-1
771	1	734	76	-2484058267	ff0fb71e-279e-4121-a829-ea2e9eeb8b12	739	9	parameters	1217848508	t	-1
772	1	734	76	-473050878	75fe3d81-61ff-4ee5-832c-87b566abaebf	740	9	parameters	1217848508	t	-1
773	1	734	76	-2629885415	fa8f78c7-9d28-4f9f-a1cc-608b7b47079c	741	9	parameters	1217848508	t	-1
774	1	734	76	-3409005429	6c15f078-21b9-4362-878a-33034f127f8d	742	9	parameters	1217848508	t	-1
775	1	734	76	-3534180724	f6845dae-ce6f-4dcd-ae5e-afe9e9b06747	743	9	parameters	1217848508	t	-1
776	1	734	76	-2174222067	5d7046fe-1616-42e7-ab71-ff07791e82ce	744	9	parameters	1217848508	t	-1
777	1	734	76	-18557849	867b0a81-209c-452b-86af-6bd147588572	745	9	parameters	1217848508	t	-1
778	1	734	76	-3115431767	8e2fd10b-e9da-4fdd-89ff-891ab62705b5	746	9	parameters	1217848508	t	-1
779	1	734	76	-4190941542	dc1196ff-2f4b-4306-a974-c24c6d316ec2	747	9	parameters	1217848508	t	-1
780	1	734	76	-587221257	b9110de3-5314-4f11-9e47-2747de861024	748	9	parameters	1217848508	t	-1
781	1	734	76	-2694319399	bbebf85a-c66e-42ab-a8e9-e4061558988f	749	9	parameters	1217848508	t	-1
782	1	734	76	-1585417268	c5f10260-0557-441f-a30a-c2dd67f49e3d	750	9	parameters	1217848508	t	-1
783	1	734	76	-3493146524	a054eda3-2c2a-4492-a77f-15f05d8d3716	751	9	parameters	1217848508	t	-1
784	1	734	76	-3874915373	367122a1-ba59-45d7-b219-1190fe37ae73	752	9	parameters	1217848508	t	-1
785	1	734	76	-3845079790	96368a92-ff0b-4708-b19d-9dc7b862ef0c	753	9	parameters	1217848508	t	-1
786	1	734	76	-3832383268	66b6af2c-062a-4cf1-821b-778146384872	754	9	parameters	1217848508	t	-1
787	1	498	33	406756776	5bf0b30c-2321-43eb-b752-460d2d74dca6	755	6	doclib	2991633180	t	-1
788	1	755	76	-1061454805	9693b0b0-ec51-48cd-ae68-0c410e4a7189	756	9	parameters	1217848508	t	-1
789	1	755	76	-1618385339	51587af5-a955-4669-961a-e3f590dcc344	757	9	parameters	1217848508	t	-1
790	1	755	76	-2031089437	99a264f1-5985-4d5e-b4c4-be7683d1e2f8	758	9	parameters	1217848508	t	-1
791	1	755	76	-4023889962	72e09153-d843-4d77-a091-5a2bba4934c6	759	9	parameters	1217848508	t	-1
792	1	755	76	-2151330027	7c724fd3-486c-4c65-8aa6-aa6a47b3207f	760	9	parameters	1217848508	t	-1
793	1	755	76	-584105844	ccc12d12-8a00-4bac-9b3f-198af739d838	761	9	parameters	1217848508	t	-1
794	1	755	76	-1126783293	0515bd8c-faa2-42d4-8c83-9028c307b6f5	762	9	parameters	1217848508	t	-1
795	1	755	76	-3185207211	53491ac2-b4cd-4112-8888-2a51cd052ed8	763	9	parameters	1217848508	t	-1
796	1	755	76	-1876356322	993428e8-5ccb-47f7-8e2c-62687382785c	764	9	parameters	1217848508	t	-1
797	1	755	76	-3874784771	43c0d1fc-27af-4ceb-8148-76d411cdd1d9	765	9	parameters	1217848508	t	-1
798	1	755	76	-629589362	88098d6f-e8bb-47ef-85ea-392f80d845d9	766	9	parameters	1217848508	t	-1
799	1	755	76	-1736415732	07e7d850-dc04-416c-8349-33f64c1f026d	767	9	parameters	1217848508	t	-1
800	1	755	76	-3777081542	4a977bd9-8109-4392-bc32-8ff7987cd411	768	9	parameters	1217848508	t	-1
801	1	755	76	-2520365952	76d33b92-87e8-4561-b720-5e87c1d40fd9	769	9	parameters	1217848508	t	-1
802	1	755	76	-1304359672	29d808cd-fbcc-41c8-8e7a-fca5420929ec	770	9	parameters	1217848508	t	-1
803	1	755	76	-1340612136	d5bada98-9436-439e-a1df-29104cc56d49	771	9	parameters	1217848508	t	-1
804	1	755	76	-1767731300	66c0b850-4844-479d-be53-d4c81b57c521	772	9	parameters	1217848508	t	-1
805	1	755	76	-1240725316	1308594a-6431-4912-ac07-3c930eda5528	773	9	parameters	1217848508	t	-1
806	1	755	76	-3547538168	9d8ae01c-6ef5-4530-9ab2-6e4e86e9902d	774	9	parameters	1217848508	t	-1
807	1	755	76	-2150186056	a3489cd1-dc79-463d-bafa-366523d88969	775	9	parameters	1217848508	t	-1
808	1	498	33	3676342880	b7578005-b821-40f4-8ac7-d13c811deda9	776	6	pdf	1671108346	t	-1
809	1	776	76	-586998081	0549fe79-78f3-4e4f-a20b-af01f8e96211	777	9	parameters	1217848508	t	-1
810	1	776	76	-1384008904	c5ce45a2-5391-4da3-be2a-9e9375bc26c2	778	9	parameters	1217848508	t	-1
811	1	776	76	-8667996	f1e2df35-5281-4ada-845a-c0893fed5a00	779	9	parameters	1217848508	t	-1
812	1	776	76	-1246409000	2662ee18-3318-40a2-94d2-2b3a1a6bb002	780	9	parameters	1217848508	t	-1
813	1	776	76	-356531818	0d9c3066-dfb3-4859-a23c-256a5ffb8e70	781	9	parameters	1217848508	t	-1
814	1	776	76	-3984961114	239729b4-1e89-4d72-8aa7-a2d3d5887bba	782	9	parameters	1217848508	t	-1
815	1	776	76	-3773818455	19716bd4-3b73-4465-b410-327063ad884b	783	9	parameters	1217848508	t	-1
816	1	776	76	-3708963409	b841c851-28fd-4ff2-abc5-4d6dbf03e947	784	9	parameters	1217848508	t	-1
817	1	776	76	-2761411911	9d17bc16-0cfa-4680-9f7f-5c9317e14da7	785	9	parameters	1217848508	t	-1
818	1	776	76	-1641952763	acba0494-e96e-4ec4-a60d-1516e5e226df	786	9	parameters	1217848508	t	-1
819	1	776	76	-1237351844	4e5a082a-9b3d-4496-9663-c3917da865a8	787	9	parameters	1217848508	t	-1
820	1	498	33	3011658141	5eead6ab-477e-46a6-abc9-1d3fdf453642	788	6	medium	842744043	t	-1
821	1	788	76	-3239526269	fa0716a7-5fdd-466e-b1e0-1f46099adebe	789	9	parameters	1217848508	t	-1
822	1	788	76	-977915268	e0e00d1a-7089-401e-91a6-bb746081d3e3	790	9	parameters	1217848508	t	-1
823	1	788	76	-3682535279	16423820-cd1d-4a44-b2aa-1048b6277790	791	9	parameters	1217848508	t	-1
824	1	788	76	-1314540982	ad71d677-e179-4c39-a398-c7263d5235d1	792	9	parameters	1217848508	t	-1
825	1	788	76	-3904329021	bec30e44-10d8-47ab-a31d-57c1e406bad9	793	9	parameters	1217848508	t	-1
826	1	788	76	-3891130983	4f2ed8ac-1a3d-4f49-a682-ed00ac990a46	794	9	parameters	1217848508	t	-1
827	1	788	76	-2274175273	27e1ddce-51ea-455d-8f22-9f5b311ebbdd	795	9	parameters	1217848508	t	-1
828	1	788	76	-1979707737	359b13b6-0815-4be4-8f72-cdba0a45a25b	796	9	parameters	1217848508	t	-1
829	1	788	76	-4259147087	f3235c12-9356-4890-88bb-eeb2f7ac642c	797	9	parameters	1217848508	t	-1
830	1	788	76	-2461232911	53959226-3f12-4f6d-87ed-47abaa42c6de	798	9	parameters	1217848508	t	-1
831	1	788	76	-4214903676	35690960-1c62-4e77-8dcc-7aea8f70692c	799	9	parameters	1217848508	t	-1
832	1	788	76	-1885045959	3bcd94d5-99fb-4842-b5a1-87081ebdabac	800	9	parameters	1217848508	t	-1
833	1	788	76	-2752737299	4383c7b5-e307-47e4-98d5-0b37c471fd6d	801	9	parameters	1217848508	t	-1
834	1	788	76	-3741944118	9ed99c92-c36b-44e4-93b8-81df5b499796	802	9	parameters	1217848508	t	-1
835	1	788	76	-3262135569	0b039af9-481a-4869-8d74-ba867143abac	803	9	parameters	1217848508	t	-1
836	1	788	76	-150859462	6fa43678-e331-4cfb-beb4-a7b99964deda	804	9	parameters	1217848508	t	-1
837	1	788	76	-3946439086	c800d1b6-6567-4d5b-a31d-da20756be90b	805	9	parameters	1217848508	t	-1
838	1	788	76	-1348697924	5c7ae461-db17-455a-87ec-95f45da39a39	806	9	parameters	1217848508	t	-1
839	1	788	76	-684593837	a098acd4-e22e-447a-9002-d2c26f7ed199	807	9	parameters	1217848508	t	-1
840	1	788	76	-1493590799	f6e79aad-1d8c-46bb-a079-03eb4e8fb2dd	808	9	parameters	1217848508	t	-1
841	1	498	33	2363332548	8ab35f60-95d0-4067-88d7-7e5d89fab6af	809	6	avatar	3795810163	t	-1
842	1	809	76	-2282034659	19b08d7a-b7d1-4963-937a-906bcaafa6af	810	9	parameters	1217848508	t	-1
843	1	809	76	-2862215227	5078925b-8cd5-437c-bfa8-3569ff3b5cf5	811	9	parameters	1217848508	t	-1
844	1	809	76	-645454143	606699d3-6cc6-4738-8fdb-decd6ee6d57a	812	9	parameters	1217848508	t	-1
845	1	809	76	-597015012	30c986bb-67fb-49f3-8a8f-476abf1bdd47	813	9	parameters	1217848508	t	-1
846	1	809	76	-1105628114	67f90708-30fe-48ff-b81a-da19075bd5b6	814	9	parameters	1217848508	t	-1
847	1	809	76	-2590409765	66f7c203-9a74-4adc-bc49-556c4511a28e	815	9	parameters	1217848508	t	-1
848	1	809	76	-2452150976	ee044b36-e966-4be9-a872-d6efd890241f	816	9	parameters	1217848508	t	-1
849	1	809	76	-3545095144	b75be963-3629-406a-a2f5-3ccf30d53cc3	817	9	parameters	1217848508	t	-1
850	1	809	76	-3652938954	7fa6a057-732e-48d2-acef-c540f83c58ff	818	9	parameters	1217848508	t	-1
851	1	809	76	-1878549997	4af6fc15-8d97-4a04-88aa-c002b2f45e52	819	9	parameters	1217848508	t	-1
852	1	809	76	-536105094	554796c2-09f3-44f2-b269-78cc2b255733	820	9	parameters	1217848508	t	-1
853	1	809	76	-1613549058	0e1b8f30-0952-4d64-b073-cd1b2e643aa0	821	9	parameters	1217848508	t	-1
854	1	809	76	-4244597108	93539cd9-957c-4af6-8361-6ecb5a756cc8	822	9	parameters	1217848508	t	-1
855	1	809	76	-3538333328	863495ff-3a83-46cb-8bc9-f8d669bfdcfe	823	9	parameters	1217848508	t	-1
856	1	809	76	-2905529648	a0b8a043-873c-4baf-a2c8-37f23110dbfd	824	9	parameters	1217848508	t	-1
857	1	809	76	-4197043851	17c7787c-f9b0-49df-a91c-c935e37024df	825	9	parameters	1217848508	t	-1
858	1	809	76	-1843397296	18f72aca-6c49-46c7-a0fd-e5a797a269a1	826	9	parameters	1217848508	t	-1
859	1	809	76	-241662568	3291b3a2-b3be-4ffc-bf21-d9fbd67c62ac	827	9	parameters	1217848508	t	-1
860	1	809	76	-2884883164	b7040e4c-ff31-44c3-9c9a-6ca728101902	828	9	parameters	1217848508	t	-1
861	1	809	76	-908244945	18e3a725-b7dd-4c70-850a-fc2e44a1e3aa	829	9	parameters	1217848508	t	-1
862	1	498	33	1611500805	8dc9d683-e51f-4a39-a2f2-6c2a5d529aab	830	6	webpreview	1387062285	t	-1
863	1	830	76	-2882421065	dcda766c-31a4-43ac-b162-20dbe5ac33ae	831	9	parameters	1217848508	t	-1
864	1	830	76	-376376072	5f1f78b0-b16b-49d6-929e-9c3dc3432688	832	9	parameters	1217848508	t	-1
865	1	830	76	-1429209132	dc1f9357-f3f0-49dd-867c-8c44d09ffc8b	833	9	parameters	1217848508	t	-1
866	1	830	76	-4013078823	eea034c9-0176-424d-8e60-4e141d553243	834	9	parameters	1217848508	t	-1
867	1	830	76	-3035113327	fa1daa66-d6de-48b7-bff2-a62cceea2f46	835	9	parameters	1217848508	t	-1
868	1	830	76	-3116064050	6a1bc9d9-d58f-4942-8e06-cce93e90b131	836	9	parameters	1217848508	t	-1
869	1	830	76	-4210810289	12179aea-0de5-49fd-9d52-df7f5ad774ef	837	9	parameters	1217848508	t	-1
870	1	830	76	-1000160090	071b3e8c-db27-4f9e-b886-6d539d4bd024	838	9	parameters	1217848508	t	-1
871	1	830	76	-3196894923	7e687dc0-2204-4e2c-9766-90023ebdc077	839	9	parameters	1217848508	t	-1
872	1	830	76	-992418267	1cc5c241-8576-4357-b831-61dd17a3c18d	840	9	parameters	1217848508	t	-1
873	1	830	76	-4145111131	c897ddfd-1079-430a-b214-fce4f2ac2118	841	9	parameters	1217848508	t	-1
874	1	830	76	-3743068634	59e29858-0903-45c5-91fd-7a06e3e037cc	842	9	parameters	1217848508	t	-1
875	1	498	33	1737744739	e91dfa63-1121-4491-9f46-b91304e7b2cd	843	6	avatar32	3071675098	t	-1
876	1	843	76	-1673900511	764a55c8-b81c-48c4-892c-b5c97069e9b2	844	9	parameters	1217848508	t	-1
877	1	843	76	-196137570	aaa8d3a4-882b-4835-bd9a-0ed9d9d34605	845	9	parameters	1217848508	t	-1
878	1	843	76	-2011661167	392f462d-fd0d-493c-98cf-a86f4d2af322	846	9	parameters	1217848508	t	-1
879	1	843	76	-1359663080	650c9ab9-de10-496c-8c3d-8d936dc9f158	847	9	parameters	1217848508	t	-1
880	1	843	76	-4069792896	2c2012b8-cb3f-443f-bd20-898999fc1551	848	9	parameters	1217848508	t	-1
881	1	843	76	-1562342695	d26e9d33-8012-4afa-85d0-2138e31a687a	849	9	parameters	1217848508	t	-1
882	1	843	76	-3853487769	0ac0c2a2-0c92-4527-afbd-b284abec19df	850	9	parameters	1217848508	t	-1
883	1	843	76	-1623325361	69e33841-74a8-48bd-874d-b45d261a01bf	851	9	parameters	1217848508	t	-1
884	1	843	76	-3309869838	d7308806-f39c-499b-822b-49c1c07c78a5	852	9	parameters	1217848508	t	-1
885	1	843	76	-3884228357	a4f41945-c40e-4a63-8ad1-bf4ebd6284fc	853	9	parameters	1217848508	t	-1
886	1	843	76	-1004517432	9ef982ce-3537-42b6-9d4e-bde9509eabf4	854	9	parameters	1217848508	t	-1
887	1	843	76	-1153014331	243007c8-dc89-40d1-a8cc-256d03340e5b	855	9	parameters	1217848508	t	-1
888	1	843	76	-3264070282	03ae53c2-ee66-4e1a-8498-528bc27ac081	856	9	parameters	1217848508	t	-1
889	1	843	76	-3805415122	b169a19d-151a-418e-beea-fee9e5d74796	857	9	parameters	1217848508	t	-1
890	1	843	76	-2331451685	f2ac724b-608f-4e50-94f6-63ed37f3765e	858	9	parameters	1217848508	t	-1
891	1	843	76	-2528164867	5e9287e9-c734-441c-8730-c79a19428b9d	859	9	parameters	1217848508	t	-1
892	1	843	76	-1295661720	6d7f2a59-bced-48c8-bab7-71d5d14e67eb	860	9	parameters	1217848508	t	-1
893	1	843	76	-2054267623	fd4aa8c6-3c74-437a-9336-4c1444b01a02	861	9	parameters	1217848508	t	-1
894	1	843	76	-2623694719	536be51e-d1bb-4dc3-b123-b0741a054ff0	862	9	parameters	1217848508	t	-1
895	1	843	76	-3581188866	526c4fe7-fc48-43a6-8442-042368cb4529	863	9	parameters	1217848508	t	-1
896	1	14	33	2443821992	solr facets space	864	20	facets	1198935629	t	-1
897	1	474	33	3997758973	components	865	6	components	3945296900	t	-1
898	1	865	33	2786798001	page.title.user~admin~dashboard.xml	866	6	page.title.user~admin~dashboard.xml	1369414613	t	-1
899	1	865	33	3900866617	page.full-width-dashlet.user~admin~dashboard.xml	867	6	page.full-width-dashlet.user~admin~dashboard.xml	2343904887	t	-1
900	1	865	33	3671186006	page.component-1-1.user~admin~dashboard.xml	868	6	page.component-1-1.user~admin~dashboard.xml	1185935389	t	-1
901	1	865	33	679884718	page.component-1-2.user~admin~dashboard.xml	869	6	page.component-1-2.user~admin~dashboard.xml	3036159461	t	-1
902	1	865	33	3624918385	page.component-2-1.user~admin~dashboard.xml	870	6	page.component-2-1.user~admin~dashboard.xml	1148319546	t	-1
903	1	865	33	710422665	page.component-2-2.user~admin~dashboard.xml	871	6	page.component-2-2.user~admin~dashboard.xml	3055950530	t	-1
904	1	474	33	544531829	pages	872	6	pages	3167021155	t	-1
905	1	872	33	2375276105	user	873	6	user	882403725	t	-1
906	1	873	33	2282622326	admin	874	6	admin	347996256	t	-1
907	1	874	33	631660036	dashboard.xml	875	6	dashboard.xml	4036615692	t	-1
908	1	13	33	2523687389	animals	876	6	Animals	1006110107	t	-1
909	1	36	49	1604328212	animal species	877	6	Animal Species	1400368610	t	-1
910	1	877	49	274457570	mammal	878	6	Mammal	3821003656	t	-1
911	1	877	49	1064584243	fish	879	6	Fish	642602185	t	-1
912	1	876	33	871556004	animals list	880	6	Animals list	1946342526	t	-1
914	1	880	142	-2180048150	a995bbb3-ae18-4e02-913b-63348a39fe2f	882	6	pdf	1671108346	t	-1
915	1	880	142	-4096577862	6d74d982-8927-4ab6-a01e-f5b4655f598a	884	6	doclib	2991633180	t	-1
916	1	876	33	417573366	carp.pdf	885	6	carp.pdf	2122824553	t	-1
917	1	10	164	3605047892	02acf462-533d-4e1b-9825-05fa934140da	886	5	02acf462-533d-4e1b-9825-05fa934140da	2529986722	t	-1
918	1	886	166	-4139272015	5e9b7f57-fac6-4d31-9b1b-5edbaafef10e	887	5	version-0	3486964613	t	-1
919	1	885	142	-765167442	f5ad5ad3-8817-481f-b94f-d487c8bf3722	888	6	doclib	2991633180	t	-1
920	1	876	33	734781869	giraffe.pdf	889	6	giraffe.pdf	4021275540	t	-1
921	1	10	164	1409533291	f9d6264e-426b-41cd-9f4b-b660dc582311	890	5	f9d6264e-426b-41cd-9f4b-b660dc582311	338683805	t	-1
922	1	890	166	-770743412	54bbecb6-20e1-4326-9c98-cdb6da05c473	891	5	version-0	3486964613	t	-1
923	1	876	33	2850213766	porcupine.pdf	892	6	porcupine.pdf	2094878606	t	-1
924	1	10	164	4079727331	71b5b65b-d92a-4944-9403-48b7ebf8664c	893	5	71b5b65b-d92a-4944-9403-48b7ebf8664c	3003617301	t	-1
925	1	893	166	-1095372473	247d10c8-ae5a-45ae-8a1d-9defd4a63386	894	5	version-0	3486964613	t	-1
927	1	889	142	-2048056070	57b219d0-ba8a-4855-84dd-0011cb334e68	896	6	doclib	2991633180	t	-1
926	1	892	142	-15040027	79518897-c3a9-4ded-becc-b694e24eebe2	895	6	doclib	2991633180	t	-1
\.


--
-- Data for Name: alf_content_data; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_content_data (id, version, content_url_id, content_mimetype_id, content_encoding_id, content_locale_id) FROM stdin;
1	0	1	1	1	2
2	0	2	1	1	2
3	0	3	1	1	2
4	0	4	2	2	2
5	0	5	3	\N	2
6	0	6	2	1	2
7	0	7	2	1	2
8	0	8	2	1	2
9	0	9	2	1	2
10	0	10	2	1	2
11	0	11	2	1	2
12	0	12	2	1	2
13	0	13	2	1	2
14	0	14	2	1	2
15	0	15	2	1	2
16	0	16	2	1	2
17	0	17	2	1	2
18	0	18	2	1	2
19	0	19	2	1	2
20	0	20	2	1	2
21	0	21	2	1	2
22	0	22	2	1	2
23	0	23	2	1	2
24	0	24	2	1	2
25	0	25	2	1	2
26	0	26	2	1	2
27	0	27	2	1	2
28	0	28	2	1	2
29	0	29	2	1	2
30	0	30	2	1	2
31	0	31	2	1	2
32	0	32	2	1	2
33	0	33	2	1	2
34	0	34	2	1	2
35	0	35	2	1	2
36	0	36	2	1	2
37	0	37	2	1	2
38	0	38	2	1	2
39	0	39	2	1	2
40	0	40	2	1	2
41	0	41	4	1	2
42	0	42	4	1	2
43	0	43	4	1	2
44	0	44	4	1	2
45	0	45	4	1	2
46	0	46	4	1	2
47	0	47	3	1	2
48	0	48	3	1	2
49	0	49	3	1	2
50	0	50	3	1	2
51	0	51	4	1	2
52	0	52	2	1	2
53	0	53	1	1	2
54	0	54	2	1	2
55	0	55	2	1	2
56	0	56	2	1	2
57	0	57	4	1	2
58	0	58	2	1	2
59	0	59	1	1	2
60	0	60	2	1	2
61	0	61	2	1	2
62	0	62	2	1	2
63	0	63	4	1	2
64	0	64	2	1	2
65	0	65	1	1	2
66	0	66	2	1	2
67	0	67	3	1	2
68	0	68	2	1	2
69	0	69	2	1	2
70	0	70	2	1	2
71	0	71	2	1	2
72	0	72	2	1	2
73	0	73	2	1	2
74	0	74	2	1	2
75	0	75	2	1	2
76	0	76	2	1	2
77	0	77	2	1	2
78	0	78	2	1	2
79	0	79	2	1	2
80	0	80	2	1	2
81	0	81	2	1	2
82	0	82	2	1	2
83	0	83	2	1	2
84	0	84	2	1	2
85	0	85	2	1	2
86	0	86	2	1	2
87	0	87	2	1	2
88	0	88	2	1	2
89	0	89	2	1	2
90	0	90	2	1	2
91	0	91	2	1	2
92	0	92	2	1	2
93	0	93	2	1	2
94	0	94	2	1	2
95	0	95	2	1	2
96	0	96	2	1	2
97	0	97	2	1	2
98	0	98	2	1	2
99	0	99	2	1	2
100	0	100	2	1	2
101	0	101	2	1	2
102	0	102	2	1	2
103	0	103	2	1	2
104	0	104	2	1	2
105	0	105	2	1	2
106	0	106	2	1	2
107	0	107	2	1	2
108	0	108	2	1	2
109	0	109	2	1	2
110	0	110	2	1	2
111	0	111	2	1	2
112	0	112	2	1	2
113	0	113	2	1	2
114	0	114	2	1	2
115	0	115	2	1	2
116	0	116	2	1	2
117	0	117	2	1	2
118	0	118	2	1	2
119	0	119	2	1	2
120	0	120	2	1	2
121	0	121	2	1	2
122	0	122	2	1	2
123	0	123	2	1	2
124	0	124	2	1	2
125	0	125	2	1	2
126	0	126	2	1	2
127	0	127	2	1	2
128	0	128	2	1	2
129	0	129	2	1	2
130	0	130	2	1	2
131	0	131	2	1	2
132	0	132	2	1	2
133	0	133	2	1	2
134	0	134	2	1	2
135	0	135	2	1	2
136	0	136	2	1	2
137	0	137	2	1	2
138	0	138	2	1	2
139	0	139	2	1	2
140	0	140	2	1	2
141	0	141	2	1	2
142	0	142	2	1	2
143	0	143	5	\N	2
144	0	144	6	1	2
145	0	145	4	1	2
146	0	146	2	1	2
147	0	147	6	1	2
148	0	148	7	1	2
149	0	149	8	1	2
150	0	150	2	1	2
151	0	151	6	1	2
152	0	152	7	1	2
153	0	153	8	1	2
154	0	154	2	1	2
155	0	155	2	1	2
156	0	156	9	1	2
157	0	156	9	1	2
158	0	158	8	1	2
159	0	159	10	1	2
160	0	160	7	1	2
161	0	161	8	1	2
162	0	162	7	1	2
163	0	163	8	1	2
164	0	164	7	1	2
165	0	165	8	1	2
166	0	166	8	1	2
167	0	167	7	1	2
168	0	168	8	1	2
169	0	169	7	1	2
170	0	170	8	1	2
171	0	171	7	1	2
172	0	172	8	1	2
173	0	173	7	1	2
174	0	174	8	1	2
175	0	175	7	1	2
176	0	176	8	1	2
177	0	177	8	1	2
178	0	178	8	1	2
179	0	179	8	1	2
180	0	180	8	1	2
181	0	181	8	1	2
182	0	182	8	1	2
183	0	183	8	1	2
184	0	184	8	1	2
185	0	185	8	1	2
186	0	186	8	1	2
187	0	187	8	1	2
188	0	188	8	1	2
189	0	189	8	1	2
190	0	190	8	1	2
191	0	191	8	1	2
192	0	192	8	1	2
193	0	193	8	1	2
194	0	194	8	1	2
195	0	195	11	1	2
196	0	196	8	1	2
197	0	197	8	1	2
198	0	198	8	1	2
199	0	199	8	1	2
200	0	200	12	1	2
201	0	201	8	1	2
202	0	202	10	1	2
203	0	203	3	1	2
204	0	204	13	1	2
205	0	205	8	1	2
206	0	206	10	1	2
207	0	207	13	1	2
208	0	208	8	1	2
209	0	209	10	1	2
210	0	210	13	1	2
211	0	211	8	1	2
212	0	212	10	1	2
213	0	213	14	1	2
214	0	214	8	1	2
215	0	215	10	1	2
216	0	216	14	1	2
217	0	217	8	1	2
218	0	218	10	1	2
219	0	219	3	1	2
220	0	220	3	1	2
221	0	221	3	1	2
222	0	221	3	1	2
223	0	223	3	1	2
224	0	223	3	1	2
225	0	225	3	1	2
226	0	225	3	1	2
227	0	227	3	1	2
228	0	228	3	1	2
229	0	229	3	1	2
230	0	230	3	1	2
231	0	231	3	1	2
232	0	232	3	1	2
233	0	233	1	3	2
234	0	234	1	3	2
235	0	235	1	3	2
236	0	236	1	3	2
237	0	237	1	3	2
238	0	238	1	3	2
239	0	239	1	3	2
240	0	240	1	3	2
241	0	241	1	3	2
242	0	242	1	3	2
244	0	244	6	1	2
252	0	252	1	1	2
263	0	263	8	1	2
265	0	264	9	1	4
266	0	264	9	1	4
253	0	253	1	1	2
254	0	254	1	1	2
255	0	255	1	1	2
256	0	256	1	1	2
257	0	257	1	1	2
258	0	258	1	1	2
260	0	259	2	1	4
262	0	261	9	1	4
267	0	266	8	1	2
269	0	267	9	1	4
270	0	267	9	1	4
272	0	269	9	1	4
273	0	269	9	1	4
274	0	271	8	1	2
275	0	272	8	1	2
\.


--
-- Data for Name: alf_content_url; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_content_url (id, content_url, content_url_short, content_url_crc, content_size, orphan_time) FROM stdin;
1	store://2024/2/19/7/51/73929959-5754-4d14-ab5e-90b4fd052d4f.bin	fd052d4f.bin	1868987260	6083	\N
2	store://2024/2/19/7/51/cfe2bc3d-ca86-4d1c-bb6d-49f136ed35a5.bin	36ed35a5.bin	2054027427	1026	\N
3	store://2024/2/19/7/51/2a13032d-9453-44d8-a008-a0adc93ed4cf.bin	c93ed4cf.bin	2670016831	3845	\N
4	store://2024/2/19/7/51/3b41b0fd-3690-4cf8-a440-f70c49441361.bin	49441361.bin	2877392951	320	\N
5	store://2024/2/19/7/51/3a3fead4-4b6b-41f3-a267-2d0ff4e2e779.bin	f4e2e779.bin	3764651204	549	\N
6	store://2024/2/19/7/51/1bf7009d-a42f-4229-98c7-e25997a1b52c.bin	97a1b52c.bin	2084335123	2625	\N
7	store://2024/2/19/7/51/87185fd3-a7de-4c62-a3f8-7b62ae1983d5.bin	ae1983d5.bin	632908463	555	\N
8	store://2024/2/19/7/51/ab32de6f-f4f4-4661-a8ba-4b5dc141fcee.bin	c141fcee.bin	2460851396	923	\N
9	store://2024/2/19/7/51/12698418-d506-4ef1-9ad3-d5aca83b620e.bin	a83b620e.bin	637924825	690	\N
10	store://2024/2/19/7/51/1af76944-6932-4614-8b3f-f8ed08adb7a9.bin	08adb7a9.bin	1943003306	715	\N
11	store://2024/2/19/7/51/2b82df15-3a14-4c91-8776-9a0485861f15.bin	85861f15.bin	21994326	643	\N
12	store://2024/2/19/7/51/9eaab1ee-953e-4e75-95e5-2bf2ca08c722.bin	ca08c722.bin	1711437405	1106	\N
13	store://2024/2/19/7/51/ffc3743d-14e8-44ee-af7f-8ee14084b689.bin	4084b689.bin	3753271244	1963	\N
14	store://2024/2/19/7/51/9e3274f7-4d90-44ba-8331-6f1533870ab8.bin	33870ab8.bin	2961253964	641	\N
15	store://2024/2/19/7/51/8a6b406f-b1f1-4bb4-93e6-d8df126827ba.bin	126827ba.bin	4252034057	3500	\N
16	store://2024/2/19/7/51/178b6cd7-ea8f-4e9a-ad8e-e61cdd3c505d.bin	dd3c505d.bin	2673440800	909	\N
17	store://2024/2/19/7/51/d5b305ec-5a7b-418d-8eeb-9e69c2b31e50.bin	c2b31e50.bin	1924076765	489	\N
18	store://2024/2/19/7/51/42ee1b9f-e65a-4802-90c8-12c98aa2d47b.bin	8aa2d47b.bin	2118259962	539	\N
19	store://2024/2/19/7/51/079509eb-a008-447b-8927-cb4deef9c3b2.bin	eef9c3b2.bin	3125628579	9132	\N
20	store://2024/2/19/7/51/c59d7372-075c-4330-a874-c4c7c3dd90d7.bin	c3dd90d7.bin	2848477235	9140	\N
21	store://2024/2/19/7/51/b3240673-7f6e-416c-82e9-4a04ec746f4c.bin	ec746f4c.bin	1079467279	9148	\N
22	store://2024/2/19/7/51/a732e849-0d92-4e77-a01a-1134ec1d8cc9.bin	ec1d8cc9.bin	1812789523	9150	\N
23	store://2024/2/19/7/51/f7c7ab11-1c4b-4239-8382-aeefbf3c520d.bin	bf3c520d.bin	248180650	9134	\N
24	store://2024/2/19/7/51/ace11540-c95b-4a4b-acdd-c16f8be21805.bin	8be21805.bin	2703582396	9165	\N
25	store://2024/2/19/7/51/3ec48559-27af-4b9c-b07c-215e5ab1de66.bin	5ab1de66.bin	3840969171	9125	\N
26	store://2024/2/19/7/51/1c3a36c8-0e8e-4ea5-9ebe-25400c67f9f0.bin	0c67f9f0.bin	3355240676	3701	\N
27	store://2024/2/19/7/51/fb9a4d43-b2e5-4467-9a68-7d3b5b025aa3.bin	5b025aa3.bin	4083563410	3721	\N
28	store://2024/2/19/7/51/ae6b1bce-ea48-4308-a1e1-f6fd018e20db.bin	018e20db.bin	1768115333	3717	\N
29	store://2024/2/19/7/51/586380de-4c0e-4fd5-92c6-461cd2620607.bin	d2620607.bin	1549317950	3708	\N
30	store://2024/2/19/7/51/1c3a7d08-add5-4698-b2f8-ab39dad62f6f.bin	dad62f6f.bin	2601692653	3701	\N
31	store://2024/2/19/7/51/9d34385b-49e1-420b-8728-5e383147ae35.bin	3147ae35.bin	3972952374	3780	\N
32	store://2024/2/19/7/51/f5113ee4-8671-4b61-a517-9f54270cdfa0.bin	270cdfa0.bin	1988548760	3700	\N
33	store://2024/2/19/7/51/20c1c680-6628-4447-b6ec-4e3d056006c6.bin	056006c6.bin	2270460333	10436	\N
34	store://2024/2/19/7/51/9e8977d5-6266-4fc4-98c2-5bc85bc75c1b.bin	5bc75c1b.bin	2685636562	10348	\N
35	store://2024/2/19/7/51/6eb1085a-2254-4e33-9a91-c96317f02975.bin	17f02975.bin	3785544910	10297	\N
36	store://2024/2/19/7/51/ad6950b1-43ae-416b-89b8-4b4007ac5f7c.bin	07ac5f7c.bin	1936371678	10299	\N
37	store://2024/2/19/7/51/66f83e0a-9d48-49fc-acab-2330974bdcd8.bin	974bdcd8.bin	2422111318	10318	\N
38	store://2024/2/19/7/51/b44f46d1-8acd-40a3-be11-d6caebb9aa83.bin	ebb9aa83.bin	2918701300	10353	\N
39	store://2024/2/19/7/51/10f62c36-fb9c-4618-bdac-17e5e9d3ed00.bin	e9d3ed00.bin	3823333817	10137	\N
40	store://2024/2/19/7/51/7cc64a9a-6bb6-470e-910a-79524f6b4993.bin	4f6b4993.bin	1973725656	1859	\N
41	store://2024/2/19/7/51/8d732182-cb05-480d-bde1-e608e69d492f.bin	e69d492f.bin	2030721282	596	\N
42	store://2024/2/19/7/51/575196fe-885c-44fd-ac0e-782b31818a5a.bin	31818a5a.bin	1152277127	2271	\N
43	store://2024/2/19/7/51/db01dcf0-711c-4ea6-b14f-3760e7044374.bin	e7044374.bin	3244253800	1061	\N
44	store://2024/2/19/7/51/af4465ac-7731-4808-a4cd-d55fdebca0c1.bin	debca0c1.bin	4132088058	341	\N
45	store://2024/2/19/7/51/d121d5ab-4889-4bf6-a079-7711baa00a69.bin	baa00a69.bin	3492451020	535	\N
46	store://2024/2/19/7/51/dfe07a60-96c3-4e73-af4d-3d9ca45d1782.bin	a45d1782.bin	341158592	118	\N
47	store://2024/2/19/7/51/93a4eed5-6638-47a0-b3fe-3d11e868e412.bin	e868e412.bin	3156947970	1349	\N
48	store://2024/2/19/7/51/911f4bdd-6b2e-4273-8492-960821f584ba.bin	21f584ba.bin	2254459856	1252	\N
49	store://2024/2/19/7/51/5c57c505-7a4f-41ac-ae59-5dfbd92c60ad.bin	d92c60ad.bin	743700880	1252	\N
50	store://2024/2/19/7/51/99dbceb2-bd48-4233-83d1-9e325a7b05b6.bin	5a7b05b6.bin	510142686	1252	\N
51	store://2024/2/19/7/51/8ad1a85b-2cd7-48f6-9387-3b6ca2960b89.bin	a2960b89.bin	2685751690	314	\N
52	store://2024/2/19/7/51/f6421759-45f9-400d-9ab9-a48375a99ad2.bin	75a99ad2.bin	759901721	878	\N
53	store://2024/2/19/7/51/6e43622b-82d8-4ae5-8502-f8ccdfd84504.bin	dfd84504.bin	2591109073	543	\N
54	store://2024/2/19/7/51/1c0e6c26-d3c8-4a14-a661-95bcc1f6af5b.bin	c1f6af5b.bin	1188558973	469	\N
55	store://2024/2/19/7/51/84ac1e7f-ba6f-4db6-995d-d52dfabe412f.bin	fabe412f.bin	1678013483	60	\N
56	store://2024/2/19/7/51/cff7019d-6baa-4ceb-a966-08abfc321425.bin	fc321425.bin	3727322610	235	\N
57	store://2024/2/19/7/51/6aa3d458-8859-4999-b22e-824824ca0508.bin	24ca0508.bin	1981308394	474	\N
58	store://2024/2/19/7/51/eb53b828-dc44-45c8-9f2f-d2b1817c4fd2.bin	817c4fd2.bin	418695221	885	\N
59	store://2024/2/19/7/51/3e2db68a-f616-4946-be6b-011b9aa2b27d.bin	9aa2b27d.bin	3349149183	369	\N
60	store://2024/2/19/7/51/ac4da1d6-eda6-4d9e-8546-49321e861474.bin	1e861474.bin	3001323095	481	\N
61	store://2024/2/19/7/51/3a43bc7a-7d0e-4aa2-8467-98e33648e8d8.bin	3648e8d8.bin	2509384219	60	\N
62	store://2024/2/19/7/51/3004b57a-e582-4ff6-9fb5-2c514082dc1f.bin	4082dc1f.bin	4284435463	235	\N
63	store://2024/2/19/7/51/e27c63ad-fab7-4dfc-af60-ee75635f11a3.bin	635f11a3.bin	158646347	334	\N
64	store://2024/2/19/7/51/52a72da7-01a7-400d-b2d1-ba2cd62b215d.bin	d62b215d.bin	3998933331	1199	\N
65	store://2024/2/19/7/51/ec16b171-0455-4993-ba4e-07c9db62ce53.bin	db62ce53.bin	3312281302	347	\N
66	store://2024/2/19/7/51/6ab259ca-980c-4e18-a7e4-d4ee6d0d763b.bin	6d0d763b.bin	1591181297	904	\N
67	store://2024/2/19/7/51/17b3b8f7-0149-436f-9752-3b79a2f7244c.bin	a2f7244c.bin	807304044	1116	\N
68	store://2024/2/19/7/51/56ea1893-26fb-4a3d-a0a7-75e60daa6a70.bin	0daa6a70.bin	3428384558	735	\N
69	store://2024/2/19/7/51/8cd27eb4-996d-4ae0-9d64-6d03922c459c.bin	922c459c.bin	3145527403	2294	\N
70	store://2024/2/19/7/51/aefe7ca1-5ba3-4416-bc0b-d2ffbcfc2008.bin	bcfc2008.bin	3579944388	5093	\N
71	store://2024/2/19/7/51/cb13d1b2-1801-4c78-b8eb-adb440f8d4ea.bin	40f8d4ea.bin	2395007240	5196	\N
72	store://2024/2/19/7/51/e00a3369-7fef-4f5c-9da0-9656a3e054e7.bin	a3e054e7.bin	3733127355	5155	\N
73	store://2024/2/19/7/51/5122dd91-8151-4213-9c60-e2b7667027ca.bin	667027ca.bin	3303113650	5144	\N
74	store://2024/2/19/7/51/34627963-9e37-4920-9719-c34a1b7c3b4f.bin	1b7c3b4f.bin	4043275193	5171	\N
75	store://2024/2/19/7/51/870b0099-293b-411f-9477-0bfa6690ffb2.bin	6690ffb2.bin	2218808767	5361	\N
76	store://2024/2/19/7/51/c9eacc2f-a9e8-4a45-a9f8-7e67ddff63a7.bin	ddff63a7.bin	3094841428	5086	\N
77	store://2024/2/19/7/51/380ee1eb-48c6-45ef-bf06-d3f91d59dae2.bin	1d59dae2.bin	2421530476	6069	\N
78	store://2024/2/19/7/51/992b3549-1ac5-4a1a-8662-ed9ca7b4f48e.bin	a7b4f48e.bin	1366849522	6180	\N
79	store://2024/2/19/7/51/4f5ff822-0b4c-40b7-ab6f-369439999ed1.bin	39999ed1.bin	881556057	6120	\N
80	store://2024/2/19/7/51/9832f41e-652c-4c41-a3b0-23e48f39b31d.bin	8f39b31d.bin	384650150	6144	\N
81	store://2024/2/19/7/51/5ccf38c4-54fb-49c4-97bc-516960ab8b72.bin	60ab8b72.bin	4057357022	6156	\N
82	store://2024/2/19/7/51/914b733c-7209-46ed-8625-a21f673ba9b6.bin	673ba9b6.bin	4227079056	6333	\N
83	store://2024/2/19/7/51/fadbea9a-6b5d-4530-82c3-89e23f228080.bin	3f228080.bin	1278186635	6125	\N
84	store://2024/2/19/7/51/f68a026e-371e-47ef-9795-5c77af1400eb.bin	af1400eb.bin	580612552	5321	\N
85	store://2024/2/19/7/51/b26bebc4-2fcf-454d-b151-3cf74242e9f7.bin	4242e9f7.bin	562215069	5301	\N
86	store://2024/2/19/7/51/ac0f5982-5981-46bc-9fb9-c73aa75d20f9.bin	a75d20f9.bin	1387869907	5301	\N
87	store://2024/2/19/7/51/55457088-edff-4f44-a254-d7780fa5678d.bin	0fa5678d.bin	508424625	5301	\N
88	store://2024/2/19/7/51/e0b7ee34-c9f3-439e-94ad-7cd8a3cdbc18.bin	a3cdbc18.bin	3343721092	5301	\N
89	store://2024/2/19/7/51/81020303-bbc5-493a-886c-a7f0a31d6ce9.bin	a31d6ce9.bin	258075914	5301	\N
90	store://2024/2/19/7/51/3c8ab49f-97b6-467b-b1a6-f27fd373a7e0.bin	d373a7e0.bin	2026648730	5301	\N
91	store://2024/2/19/7/51/4aa9da26-cfd2-4614-b0c0-162895503ca4.bin	95503ca4.bin	1723025944	4572	\N
92	store://2024/2/19/7/51/8b0e3c70-eabb-42ea-bc73-2e0054f11c79.bin	54f11c79.bin	1047196589	4120	\N
93	store://2024/2/19/7/51/ae989844-eac3-4793-9e88-9e5b3dbd9d6b.bin	3dbd9d6b.bin	3048786352	4225	\N
94	store://2024/2/19/7/51/5914ab6b-fcad-4077-97d3-e77b61ffd719.bin	61ffd719.bin	994485902	4210	\N
95	store://2024/2/19/7/51/3cf12a42-d1cc-425c-899a-b99ed164e423.bin	d164e423.bin	3285087741	4205	\N
96	store://2024/2/19/7/51/79805189-d834-4603-aa16-1df243ea1379.bin	43ea1379.bin	1394947517	4213	\N
97	store://2024/2/19/7/51/d84b6e88-7f58-4d34-824b-11525ef8697c.bin	5ef8697c.bin	3868643461	4355	\N
98	store://2024/2/19/7/51/498eae54-2e20-43ce-bd18-575167081fca.bin	67081fca.bin	4185755258	4060	\N
99	store://2024/2/19/7/51/b0347ab3-a883-4194-ac09-d1f0615ff2a6.bin	615ff2a6.bin	3176060969	1067	\N
100	store://2024/2/19/7/51/345ebe82-87c5-47d4-baaf-77d3a1607230.bin	a1607230.bin	1609859819	1101	\N
101	store://2024/2/19/7/51/536cbc33-d2d0-495d-9a23-342ac76aa503.bin	c76aa503.bin	1316856162	3144	\N
102	store://2024/2/19/7/51/45d660a4-42ff-44c9-b83a-00db74bf647b.bin	74bf647b.bin	3025167427	3442	\N
103	store://2024/2/19/7/51/9048ce1e-ccab-470f-8c90-9aef1b9c65a8.bin	1b9c65a8.bin	468911735	1091	\N
104	store://2024/2/19/7/51/46dd9c27-d6dd-4ccb-8ebe-3857f7c07b26.bin	f7c07b26.bin	2571920652	1121	\N
105	store://2024/2/19/7/51/cffcb47f-b59c-48e0-bc28-92498f33c284.bin	8f33c284.bin	1075308843	3086	\N
106	store://2024/2/19/7/51/d023468f-b9bf-4829-92fb-be82e6ecba2a.bin	e6ecba2a.bin	2142051094	3462	\N
107	store://2024/2/19/7/51/f481833f-0787-4192-8eae-4a2c91c0be11.bin	91c0be11.bin	4152382969	1121	\N
108	store://2024/2/19/7/51/2620aa96-5b5a-412c-8fd4-6501f3ea32e3.bin	f3ea32e3.bin	3677417816	1157	\N
109	store://2024/2/19/7/51/a54167a4-b872-45ca-88c1-385b3d77ab43.bin	3d77ab43.bin	2476764277	3106	\N
110	store://2024/2/19/7/51/48bac9b3-fc0e-4ed4-a45d-06e5466b016f.bin	466b016f.bin	1585064372	3480	\N
111	store://2024/2/19/7/51/1f0ae5fa-6539-4ded-83a9-78ba315f7727.bin	315f7727.bin	1732855455	1133	\N
112	store://2024/2/19/7/51/2fbcad6a-29cf-4a0c-9d06-d9111ad6c13a.bin	1ad6c13a.bin	4075629184	1169	\N
113	store://2024/2/19/7/51/88092ad3-7ce2-4d48-a2c0-8f6faa39db4f.bin	aa39db4f.bin	687631698	3111	\N
114	store://2024/2/19/7/51/32d5cd86-911b-4590-b0b5-b1aecc3cf475.bin	cc3cf475.bin	951293963	3490	\N
115	store://2024/2/19/7/51/ad2665b3-b401-49d3-a366-b484affc8fdc.bin	affc8fdc.bin	3764647631	1123	\N
116	store://2024/2/19/7/51/769527d3-8369-435b-9215-1e65345129aa.bin	345129aa.bin	366482877	1161	\N
117	store://2024/2/19/7/51/f73ed642-ff2a-4de6-ba29-4a3288761eb7.bin	88761eb7.bin	1448047342	3101	\N
118	store://2024/2/19/7/51/338ca01f-3f20-407a-9684-c76b4d92b82d.bin	4d92b82d.bin	3420837223	3479	\N
119	store://2024/2/19/7/51/5d703102-52d7-48fa-b67f-f9c127a7a06f.bin	27a7a06f.bin	2936267554	1138	\N
120	store://2024/2/19/7/51/e580811a-0978-46a0-9d0b-c81d2be19648.bin	2be19648.bin	1105200159	1168	\N
121	store://2024/2/19/7/51/ab43e3df-0940-41d2-9736-0264f2016460.bin	f2016460.bin	3255163856	3149	\N
122	store://2024/2/19/7/51/f69bb321-5197-40de-bda1-5435cba912cb.bin	cba912cb.bin	663354311	3521	\N
123	store://2024/2/19/7/51/a4046547-7c98-4dba-b3dc-fc392b398446.bin	2b398446.bin	1464908448	1097	\N
124	store://2024/2/19/7/51/5d69ad7a-96cd-436e-be26-d8f1f1192c2c.bin	f1192c2c.bin	815154944	1134	\N
125	store://2024/2/19/7/51/de50ee48-01f7-4612-a1b9-42a015263500.bin	15263500.bin	636593388	3100	\N
126	store://2024/2/19/7/51/8db3a382-20b7-4c3c-b85c-5b82d234c719.bin	d234c719.bin	1021118589	3477	\N
127	store://2024/2/19/7/51/95268c78-2bd8-4ec6-af56-af62f0bae690.bin	f0bae690.bin	3112260450	1068	\N
128	store://2024/2/19/7/51/ce21aa73-5d1a-4dee-a3cf-072fa4bb2826.bin	a4bb2826.bin	493327242	1099	\N
129	store://2024/2/19/7/51/d96633fa-dc4d-4e8f-8ff7-06fae7c36c6f.bin	e7c36c6f.bin	942003285	3080	\N
130	store://2024/2/19/7/51/9223b80e-84a9-41aa-b096-a35ffcc2c691.bin	fcc2c691.bin	3689885921	3458	\N
131	store://2024/2/19/7/51/081b19b7-bfa7-4169-b9e3-c75af10f2057.bin	f10f2057.bin	3806018550	1107	\N
132	store://2024/2/19/7/51/5ca88363-1b64-4a89-bdca-ef3db98a3f41.bin	b98a3f41.bin	1756836699	1143	\N
133	store://2024/2/19/7/51/49db5619-e837-4a8e-bf6a-7b5d4975bdfc.bin	4975bdfc.bin	1817934076	3098	\N
134	store://2024/2/19/7/51/c0ee96f1-d747-481b-ade9-0a8b549dc71e.bin	549dc71e.bin	3770190622	3472	\N
135	store://2024/2/19/7/51/8ef3d164-85a2-4fc6-be61-dd0d243bc382.bin	243bc382.bin	3736256426	1277	\N
136	store://2024/2/19/7/51/3a40f510-fb9c-42f5-8b1a-93638435cba5.bin	8435cba5.bin	2636873109	1330	\N
137	store://2024/2/19/7/51/ee9843b9-00af-4dfd-8edb-fb18cdb25796.bin	cdb25796.bin	765859503	3262	\N
138	store://2024/2/19/7/51/5a2a4157-48db-4fee-95f6-8e2bcc78b0bb.bin	cc78b0bb.bin	893220024	3631	\N
139	store://2024/2/19/7/51/1a0e50e1-7634-4a9e-a164-88492a135416.bin	2a135416.bin	2619149912	1077	\N
140	store://2024/2/19/7/51/d9e6265e-5087-4a6b-b7d8-623b04644027.bin	04644027.bin	944997853	1106	\N
141	store://2024/2/19/7/51/98a9d293-fc72-46d5-b226-1812b6215829.bin	b6215829.bin	1133945983	3101	\N
142	store://2024/2/19/7/51/b0f5c704-2f44-47f0-9cf4-931047ce94ea.bin	47ce94ea.bin	3820307255	3576	\N
143	store://2024/2/19/7/51/289db418-78e8-48d7-9526-6c2ec7877614.bin	c7877614.bin	4207825538	9453	\N
144	store://2024/2/19/7/51/59c55add-656b-478b-8ed9-15bb72129306.bin	72129306.bin	1838968556	760	\N
145	store://2024/2/19/7/51/eef5f143-5621-41ae-9224-f3028a914c42.bin	8a914c42.bin	1188177694	1490	\N
146	store://2024/2/19/7/51/4149fb97-f62b-44cc-ab02-7dd81bc2e590.bin	1bc2e590.bin	2295996348	709	\N
147	store://2024/2/19/7/51/b4f145d3-2711-44bc-8577-7382faebca72.bin	faebca72.bin	2936657028	55	\N
148	store://2024/2/19/7/51/0bed4a5f-c22e-4ef7-a2ee-ed426738dae9.bin	6738dae9.bin	3388959517	2347	\N
149	store://2024/2/19/7/51/96db743f-16c0-4dd5-ae6a-5a1681217f3e.bin	81217f3e.bin	315966999	6573	\N
150	store://2024/2/19/7/51/c549618b-5adc-4575-9750-d6f71d6790af.bin	1d6790af.bin	2794074813	817	\N
151	store://2024/2/19/7/51/00527c6f-b3f0-4e66-b7b8-2f83046c86a1.bin	046c86a1.bin	1906940604	54	\N
152	store://2024/2/19/7/51/6a418a1c-aec5-44fb-b697-838ee29b127d.bin	e29b127d.bin	214336873	2823	\N
153	store://2024/2/19/7/51/89ae9108-2119-4085-ad06-d5827e3518ad.bin	7e3518ad.bin	318821252	8910	\N
154	store://2024/2/19/7/51/2f33d98d-8b7a-42ef-8133-af2fd6a1f942.bin	d6a1f942.bin	3282102075	0	\N
155	store://2024/2/19/7/51/37f32b6c-596a-4ebe-a42f-67dc70f7d899.bin	70f7d899.bin	1507888982	0	\N
156	store://2024/2/19/7/51/35eb2078-212f-4627-b0f2-c89ac4722209.bin	c4722209.bin	1631958405	381778	\N
158	store://2024/2/19/7/51/dfbf8fff-9d1e-4ec5-8d35-9d4cb644b2b6.bin	b644b2b6.bin	4087181793	3526	\N
159	store://2024/2/19/7/51/6eeada16-559f-4947-a6b5-695749fe8e52.bin	49fe8e52.bin	2876336107	87522	\N
160	store://2024/2/19/7/51/a564ee40-466b-44ba-87a3-19a92d9ba468.bin	2d9ba468.bin	1499082140	501641	\N
161	store://2024/2/19/7/51/eb1be39f-d6de-4245-8cea-5026c4eb1e9f.bin	c4eb1e9f.bin	4146414902	17951	\N
162	store://2024/2/19/7/51/dbb7f3cc-26f5-4814-aafd-2351c892f7e6.bin	c892f7e6.bin	4285201839	342155	\N
163	store://2024/2/19/7/51/c859b7e6-b85f-4ea3-9a9c-7f8231e79761.bin	31e79761.bin	4240752194	19847	\N
164	store://2024/2/19/7/51/6f628320-6a86-402f-b3cf-3be22ff9b83d.bin	2ff9b83d.bin	1494216189	145863	\N
165	store://2024/2/19/7/51/3e07313a-d9f5-4ac9-a107-1dffe641c60d.bin	e641c60d.bin	1291124365	33644	\N
166	store://2024/2/19/7/51/8c59dde7-1c1b-4e79-a937-11a0f0cd0f17.bin	f0cd0f17.bin	3525565020	266338	\N
167	store://2024/2/19/7/51/25aa3418-0907-41f8-9433-0422cb665793.bin	cb665793.bin	2098760433	540412	\N
168	store://2024/2/19/7/51/54875386-84d9-413f-a4df-8ff31a0b4c75.bin	1a0b4c75.bin	1267788601	39387	\N
169	store://2024/2/19/7/51/c2857dae-1376-4893-8127-191d921d1a09.bin	921d1a09.bin	496425245	105685	\N
170	store://2024/2/19/7/51/71be2d41-f54d-4180-b3c8-0aa4e5de42b5.bin	e5de42b5.bin	3731648841	22473	\N
171	store://2024/2/19/7/51/ff89eb87-d959-4fad-887d-1e70fdb1aa6f.bin	fdb1aa6f.bin	3883753490	165798	\N
172	store://2024/2/19/7/51/bbf92402-a808-48d7-8f3c-22654c3cefc3.bin	4c3cefc3.bin	1811837580	32865	\N
173	store://2024/2/19/7/51/af097e4c-3a4b-4612-8f9b-20ef9ed94990.bin	9ed94990.bin	1000190915	64724	\N
174	store://2024/2/19/7/51/5b4c1b72-116f-4aa8-b36e-057b925236eb.bin	925236eb.bin	2449604737	40385	\N
175	store://2024/2/19/7/51/effc6238-b23b-4ee0-a349-e136b173f242.bin	b173f242.bin	3768575669	37453	\N
176	store://2024/2/19/7/51/3dcab9be-a276-4531-a189-09ea129fdd46.bin	129fdd46.bin	1526199297	13516	\N
177	store://2024/2/19/7/51/5a083784-a2f1-4072-b75b-2aa84a34b28e.bin	4a34b28e.bin	1848386978	146544	\N
178	store://2024/2/19/7/51/ce8578c4-a9e6-4c35-832c-bcee5d124a1b.bin	5d124a1b.bin	565554952	6016	\N
179	store://2024/2/19/7/51/3a72062b-98dc-4a23-aaa2-ff53ed78c2bc.bin	ed78c2bc.bin	1491301371	679602	\N
180	store://2024/2/19/7/51/90e6ed93-bf6d-4753-893b-93c80c6b550e.bin	0c6b550e.bin	1912722753	11610	\N
181	store://2024/2/19/7/51/13a5c634-251d-40ff-aaac-9fabc117d512.bin	c117d512.bin	730066772	172648	\N
182	store://2024/2/19/7/51/cf278cc3-6cae-4b61-b923-6761c616bdf4.bin	c616bdf4.bin	1755661443	9680	\N
183	store://2024/2/19/7/51/c14aef72-987f-49b0-8c38-8d7147adb3a0.bin	47adb3a0.bin	165311945	34482	\N
184	store://2024/2/19/7/51/2fc86f8c-0ad1-487b-9a15-17e3d65d63df.bin	d65d63df.bin	2273303516	4213	\N
185	store://2024/2/19/7/51/bd0da2c8-a62b-49a7-8468-73ba4ec9ed0d.bin	4ec9ed0d.bin	1005203929	90797	\N
186	store://2024/2/19/7/51/1891dc09-b492-427e-8610-70867dd432af.bin	7dd432af.bin	2189846698	12701	\N
187	store://2024/2/19/7/51/373ee802-70d1-421a-9dc3-30eada6a2d9a.bin	da6a2d9a.bin	2696084386	188533	\N
188	store://2024/2/19/7/51/754db698-a08c-48e9-86dc-a9ab7e412b1f.bin	7e412b1f.bin	3150860158	9025	\N
189	store://2024/2/19/7/51/6740d818-d21a-4604-bd31-6b6a08646200.bin	08646200.bin	609311030	375396	\N
190	store://2024/2/19/7/51/03c568be-5fbf-4c58-9634-8bc0811268d4.bin	811268d4.bin	122334281	12288	\N
191	store://2024/2/19/7/51/c6649e76-413e-4ecf-a61f-554288bd1d4d.bin	88bd1d4d.bin	2373782904	155620	\N
192	store://2024/2/19/7/51/a295e391-d018-43ad-967e-a97b6c7de0a3.bin	6c7de0a3.bin	3056547519	350217	\N
193	store://2024/2/19/7/51/bb059ea6-d5d4-41c9-a3a9-7f2be6dad287.bin	e6dad287.bin	1578580279	14569	\N
194	store://2024/2/19/7/51/81730985-677a-46f0-b470-ed3e92b5d307.bin	92b5d307.bin	3651100647	196506	\N
195	store://2024/2/19/7/51/84a17063-864e-47d2-84db-ef58accfeec3.bin	accfeec3.bin	1970072834	3737049	\N
196	store://2024/2/19/7/51/c1bd97a2-7169-41f3-9539-a7e7f4bbd279.bin	f4bbd279.bin	890874452	212734	\N
197	store://2024/2/19/7/51/3530f2e5-1adb-4c9a-b27a-8079626dc506.bin	626dc506.bin	2360492586	6217	\N
198	store://2024/2/19/7/51/fc6e19ae-ffab-4ebb-8209-ab254d9650ce.bin	4d9650ce.bin	1182731039	777461	\N
199	store://2024/2/19/7/51/80e700df-ed06-47d4-acb9-30198d42d11f.bin	8d42d11f.bin	633497775	8085	\N
200	store://2024/2/19/7/51/08f723f8-7369-452b-935c-037c34c7f133.bin	34c7f133.bin	170710078	26112	\N
201	store://2024/2/19/7/51/a17d3da4-6177-4e25-b8f9-0cf04393ee63.bin	4393ee63.bin	4257117001	2388	\N
202	store://2024/2/19/7/51/06f45f3b-a93d-42d5-8557-c7887c8c05b6.bin	7c8c05b6.bin	276912043	20964	\N
203	store://2024/2/19/7/51/2dd04d0c-3201-4469-8ae2-eeb3f330fc31.bin	f330fc31.bin	3791814663	162	\N
204	store://2024/2/19/7/51/10ecb146-02e3-4d67-af95-4ca8f72b3667.bin	f72b3667.bin	3158487519	73728	\N
205	store://2024/2/19/7/51/0ffd1919-90f7-4272-907b-7c9fd814c6fa.bin	d814c6fa.bin	1649194497	4778	\N
206	store://2024/2/19/7/51/e79d602f-21b6-4920-90a6-7c09658b452a.bin	658b452a.bin	2435341404	42016	\N
207	store://2024/2/19/7/51/ece51b76-0b18-443d-b6e2-95cdd9f569c1.bin	d9f569c1.bin	1199402455	73728	\N
208	store://2024/2/19/7/51/1441ac9f-eb32-44dd-b850-d80353337d5d.bin	53337d5d.bin	4243417891	4774	\N
209	store://2024/2/19/7/51/9b87e323-eea9-494e-b1cd-dcb9e721b323.bin	e721b323.bin	2927622457	42502	\N
210	store://2024/2/19/7/51/b58f91a5-f5f3-47ca-8bac-23a5a4deec4d.bin	a4deec4d.bin	419525301	74240	\N
211	store://2024/2/19/7/51/0585d06d-23a5-410f-8c5b-3187d0b7bcbc.bin	d0b7bcbc.bin	2780005808	4849	\N
212	store://2024/2/19/7/51/72b0c949-f53c-4a8c-938f-2c2b2b03b3bc.bin	2b03b3bc.bin	3610802834	42890	\N
213	store://2024/2/19/7/51/ae4f0890-91c6-43cf-b53b-139fc83b21ca.bin	c83b21ca.bin	2133413121	2117632	\N
214	store://2024/2/19/7/51/7a0cdd06-dc4f-4557-a50d-7b14bd03dd2a.bin	bd03dd2a.bin	4047713335	6540	\N
215	store://2024/2/19/7/51/70d874ac-cc22-439c-84a4-c33cf38ad710.bin	f38ad710.bin	2635911922	672905	\N
216	store://2024/2/19/7/51/4250e719-adb8-4bc9-9d63-e201a403c21d.bin	a403c21d.bin	198338531	2898432	\N
217	store://2024/2/19/7/51/8b68ef79-5802-4601-8398-cc9278b8ff48.bin	78b8ff48.bin	2664212100	6414	\N
218	store://2024/2/19/7/51/1999671c-5dfc-4222-938a-adb9074fa078.bin	074fa078.bin	746195310	976492	\N
219	store://2024/2/19/7/51/53684854-8646-4ff8-9e68-95e671d8eaa5.bin	71d8eaa5.bin	1894675347	25	\N
220	store://2024/2/19/7/51/8b1c8539-032a-4367-b262-9b99a0cc3d51.bin	a0cc3d51.bin	1747805446	38	\N
221	store://2024/2/19/7/51/5930fc0a-a25f-4ce7-b79b-46cc78e46ca7.bin	78e46ca7.bin	3822029880	1175	\N
223	store://2024/2/19/7/51/3cb77afc-3bf4-4aa5-9a11-113e4a5eb241.bin	4a5eb241.bin	1354773633	1771	\N
225	store://2024/2/19/7/51/565e3b87-9e41-479d-aff3-aac5982c3623.bin	982c3623.bin	3994961006	3430	\N
227	store://2024/2/19/7/51/1bd7255b-7f3a-4fdf-9159-333aa6e5857c.bin	a6e5857c.bin	1917247419	105	\N
228	store://2024/2/19/7/51/902383d2-a94b-40c2-83ef-68c5400cc153.bin	400cc153.bin	2526834383	230	\N
229	store://2024/2/19/7/51/770a26c1-6446-4904-bc05-417a93606c13.bin	93606c13.bin	727419844	151	\N
230	store://2024/2/19/7/51/e1718602-a5ad-417b-8cb8-51db3633c927.bin	3633c927.bin	2462119743	317	\N
231	store://2024/2/19/7/51/d58ceef3-c032-473e-ad48-ba5bffc6b344.bin	ffc6b344.bin	4133821857	110	\N
232	store://2024/2/19/7/51/7e08d8ca-f244-4e51-be37-a9d8c1282bd5.bin	c1282bd5.bin	1484856800	153	\N
233	store://2024/2/19/7/51/dbe33de3-d6f6-491f-b850-7703018b40aa.bin	018b40aa.bin	1147884162	797	\N
234	store://2024/2/19/7/51/c96170ff-11c9-4384-9c96-3e282f4c2308.bin	2f4c2308.bin	1884587708	274	\N
235	store://2024/2/19/7/51/ca169bcb-e564-4437-af1c-dcb127abfcf9.bin	27abfcf9.bin	3241304348	341	\N
236	store://2024/2/19/7/51/c6bff688-9bc0-4d07-b8a1-deeff35ee454.bin	f35ee454.bin	2318368219	330	\N
237	store://2024/2/19/7/51/4427670d-9c3f-4cb9-aff9-9dc3c5ff0eda.bin	c5ff0eda.bin	1218519870	328	\N
238	store://2024/2/19/7/51/3e636fe4-0ed7-417d-95b7-d98ad2a2a7d6.bin	d2a2a7d6.bin	268469411	330	\N
239	store://2024/2/19/7/51/907fe375-d7f5-400f-9d4b-03db9f518dc0.bin	9f518dc0.bin	4009245796	282	\N
240	store://2024/2/19/7/51/2aed1183-544b-47ec-b16d-1b2e5d1c96df.bin	5d1c96df.bin	2392833801	262	\N
241	store://2024/2/19/7/51/c37ecdd2-2269-4c02-b1e1-c7ba78ff19ac.bin	78ff19ac.bin	994974770	272	\N
242	store://2024/2/19/7/51/e0938af1-2de7-4b28-a144-9937e2650fe1.bin	e2650fe1.bin	2129656092	346	\N
244	store://2024/2/19/7/51/fc2cddbe-a14d-4779-b8cd-938a0ce030ea.bin	0ce030ea.bin	574394124	652	\N
243	store://2024/2/19/7/51/0d97ffeb-2aae-4c55-8f52-a8d41fe87f23.bin	1fe87f23.bin	1955444232	32	1708329088961
252	store://2024/2/19/7/53/ee52b5ea-8d2b-45a5-a9a6-6183bdbc8c4f.bin	bdbc8c4f.bin	3445361180	284	\N
253	store://2024/2/19/7/53/36c60da0-990b-4a87-a9b1-a3f6d1cf6ac7.bin	d1cf6ac7.bin	1715847258	391	\N
254	store://2024/2/19/7/53/c9b8badc-77a0-4bec-bc6f-6e4bf25e02ec.bin	f25e02ec.bin	1983139217	291	\N
255	store://2024/2/19/7/53/e97896a8-3b72-4916-a2ac-8c829339dbc7.bin	9339dbc7.bin	1347595226	291	\N
256	store://2024/2/19/7/53/6bd2651d-cb94-4915-9968-814b203ed7c9.bin	203ed7c9.bin	4257759261	296	\N
257	store://2024/2/19/7/53/182cf3c4-0a27-4b39-979b-0ba24913e129.bin	4913e129.bin	1235433320	363	\N
258	store://2024/2/19/7/53/b33752ac-b0d7-4957-91ef-2304184049a3.bin	184049a3.bin	1268818716	388	\N
249	store://2024/2/19/7/53/6526c7ab-a362-4c82-90e8-e88ac6b60f09.bin	c6b60f09.bin	881024489	296	1708329206077
251	store://2024/2/19/7/53/49877126-0d55-4180-a71e-7f4c1a37f6d4.bin	1a37f6d4.bin	1251106825	388	1708329206077
246	store://2024/2/19/7/53/9414218e-4503-4b23-b276-bc9534d0de83.bin	34d0de83.bin	1258353930	391	1708329206077
250	store://2024/2/19/7/53/8dd50ab4-c773-4f98-87a2-9d7749e4dca8.bin	49e4dca8.bin	1776447229	363	1708329206077
247	store://2024/2/19/7/53/3344b831-5b9d-4070-b7ce-20432c9d1555.bin	2c9d1555.bin	1102175828	291	1708329206077
245	store://2024/2/19/7/53/81eef5ac-050a-4a9a-ae61-2be950d8c523.bin	50d8c523.bin	4173324202	284	1708329206077
248	store://2024/2/19/7/53/ded9bced-2b13-4805-8404-cc20b5a68337.bin	b5a68337.bin	3811825230	291	1708329206077
259	store://2024/2/19/8/0/399e41c4-93cf-4aaf-9690-5ed08b4962a0.bin	8b4962a0.bin	1869130787	28	\N
261	store://2024/2/19/8/0/530f71e9-247f-4cb4-8ffa-f79925da8832.bin	25da8832.bin	182683569	8123	\N
263	store://2024/2/19/8/0/f2b1de70-6028-449b-9d98-a4dd9f5a7d3f.bin	9f5a7d3f.bin	3063106038	195	\N
264	store://2024/2/19/8/9/a05b6411-f710-4af5-a485-8c57b7945fe8.bin	b7945fe8.bin	303567727	119625	\N
266	store://2024/2/19/8/9/4a919d97-5de1-4015-97e1-e9da85699619.bin	85699619.bin	1828013354	2245	\N
267	store://2024/2/19/8/9/c80e72be-7d12-4c20-a9e2-53b81e78c2b1.bin	1e78c2b1.bin	3629662744	2431571	\N
269	store://2024/2/19/8/9/18f74f31-1765-45a1-88d5-371e87a70a83.bin	87a70a83.bin	229899180	1496650	\N
272	store://2024/2/19/8/9/01a80bb0-e7cc-4a99-b6d2-2f58d1bd9324.bin	d1bd9324.bin	1698387071	4475	\N
271	store://2024/2/19/8/9/bbce2439-a329-4d03-89f3-032838a87c8e.bin	38a87c8e.bin	510448494	5842	\N
\.


--
-- Data for Name: alf_content_url_encryption; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_content_url_encryption (id, content_url_id, algorithm, key_size, encrypted_key, master_keystore_id, master_key_alias, unencrypted_file_size) FROM stdin;
\.


--
-- Data for Name: alf_encoding; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_encoding (id, version, encoding_str) FROM stdin;
1	0	utf-8
2	0	utf8
3	0	iso-8859-1
\.


--
-- Data for Name: alf_locale; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_locale (id, version, locale_str) FROM stdin;
1	0	.default
2	0	en_
3	0	en_US_
4	0	en_GB_
\.


--
-- Data for Name: alf_lock; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_lock (id, version, shared_resource_id, excl_resource_id, lock_token, start_time, expiry_time) FROM stdin;
1	9	1	1	not-locked	0	0
35	1	27	27	not-locked	0	0
2	3	2	2	not-locked	0	0
3	3	3	3	not-locked	0	0
15	309	13	13	not-locked	0	0
4	3	5	4	not-locked	0	0
5	3	6	4	not-locked	0	0
6	3	7	4	not-locked	0	0
7	3	8	4	not-locked	0	0
8	3	4	4	not-locked	0	0
9	3	9	9	not-locked	0	0
16	619	12	12	not-locked	0	0
10	3	5	10	not-locked	0	0
11	3	6	10	not-locked	0	0
12	3	7	10	not-locked	0	0
13	3	11	10	not-locked	0	0
14	3	10	10	not-locked	0	0
17	13	5	15	not-locked	0	0
18	13	6	15	not-locked	0	0
19	13	7	15	not-locked	0	0
20	13	17	15	not-locked	0	0
21	13	18	15	not-locked	0	0
22	13	19	15	not-locked	0	0
23	13	15	15	not-locked	0	0
27	13	5	21	not-locked	0	0
29	13	6	21	not-locked	0	0
30	13	7	21	not-locked	0	0
31	13	17	21	not-locked	0	0
32	13	25	21	not-locked	0	0
33	13	26	21	not-locked	0	0
34	13	21	21	not-locked	0	0
28	3	20	20	not-locked	0	0
24	3	22	20	not-locked	0	0
25	3	23	20	not-locked	0	0
26	3	24	20	not-locked	0	0
\.


--
-- Data for Name: alf_lock_resource; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_lock_resource (id, version, qname_ns_id, qname_localname) FROM stdin;
1	0	1	elasticsearchconfiguration
2	0	6	verifylicense
3	0	1	clusterservice
4	0	1	org.alfresco.repo.usage.userusagetrackingcomponent
5	0	1	org
6	0	1	org.alfresco
7	0	1	org.alfresco.repo
8	0	1	org.alfresco.repo.usage
9	0	1	chaininguserregistrysynchronizer
10	0	1	org.alfresco.repo.thumbnail.thumbnailregistry
11	0	1	org.alfresco.repo.thumbnail
12	0	1	activitypostlookup
13	0	1	feedgenerator
15	0	1	org.alfresco.repo.activities.post.cleanup.postcleaner
17	0	1	org.alfresco.repo.activities
18	0	1	org.alfresco.repo.activities.post
19	0	1	org.alfresco.repo.activities.post.cleanup
20	0	1	acs.repository.usage.sessions
21	0	1	org.alfresco.repo.activities.feed.cleanup.feedcleaner
22	0	1	acs
23	0	1	acs.repository
24	0	1	acs.repository.usage
25	0	1	org.alfresco.repo.activities.feed
26	0	1	org.alfresco.repo.activities.feed.cleanup
27	0	1	repousagemonitor
\.


--
-- Data for Name: alf_mimetype; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_mimetype (id, version, mimetype_str) FROM stdin;
1	0	text/xml
2	0	text/plain
3	0	text/html
4	0	application/x-javascript
5	0	application/json
6	0	application/octet-stream
7	0	image/jpeg
8	0	image/png
9	0	application/pdf
10	0	application/x-shockwave-flash
11	0	video/mp4
12	0	application/vnd.ms-excel
13	0	application/msword
14	0	application/vnd.ms-powerpoint
\.


--
-- Data for Name: alf_namespace; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_namespace (id, version, uri) FROM stdin;
1	0	http://www.alfresco.org/model/system/1.0
2	0	http://www.alfresco.org/model/user/1.0
3	0	http://www.alfresco.org/model/security/1.0
4	0	http://www.alfresco.org/model/versionstore/1.0
5	0	http://www.alfresco.org/model/versionstore/2.0
6	0	http://www.alfresco.org/model/content/1.0
7	0	http://www.alfresco.org/model/application/1.0
8	0	http://www.alfresco.org/model/rule/1.0
9	0	http://www.alfresco.org/model/action/1.0
10	0	http://www.alfresco.org/model/site/1.0
11	0	http://www.alfresco.org/model/transfer/1.0
12	0	http://www.alfresco.org/model/content/smartfolder/1.0
13	0	.empty
14	0	http://www.alfresco.org/model/rendition/1.0
15	0	http://www.alfresco.org/model/exif/1.0
16	0	http://www.alfresco.org/model/forum/1.0
17	0	http://www.alfresco.org/model/linksmodel/1.0
18	0	http://www.alfresco.org/model/datalist/1.0
19	0	http://www.alfresco.org/system/modules/1.0
20	0	http://www.alfresco.org/model/solrfacet/1.0
\.


--
-- Data for Name: alf_node; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_node (id, version, store_id, uuid, transaction_id, type_qname_id, locale_id, acl_id, audit_creator, audit_created, audit_modifier, audit_modified, audit_accessed) FROM stdin;
1	1	1	8e5c6ce3-e8f0-449f-9ef4-a22112050d8a	1	1	1	1	\N	\N	\N	\N	\N
2	1	1	e66f791a-5851-41e4-9800-359472090c01	1	3	2	2	\N	\N	\N	\N	\N
3	1	1	3cc7b60d-edf3-4fb4-be76-680eef157d2d	1	3	2	2	\N	\N	\N	\N	\N
5	1	2	913ae25a-58f9-497c-a5c3-f5772c55c1d1	2	1	1	3	\N	\N	\N	\N	\N
6	1	2	a370c411-0fa6-436f-a757-59030a3d0b19	2	12	2	4	\N	\N	\N	\N	\N
4	2	1	5aadcb4f-eb49-4c87-a32d-976018e4040c	19	5	2	2	\N	\N	\N	\N	\N
8	1	2	077a66cc-0007-46b8-add2-8002cb3c0738	2	3	2	4	\N	\N	\N	\N	\N
9	1	3	9858744e-70f9-4cb1-8586-6d9474460f12	3	1	1	5	\N	\N	\N	\N	\N
10	1	4	e5d5d773-0bb0-4026-893c-7a7e61978052	4	1	1	6	\N	\N	\N	\N	\N
11	1	5	a37cd383-87cc-4059-ba1f-3923a5ab20e4	5	1	1	7	\N	\N	\N	\N	\N
12	1	6	e7a273da-2974-4581-a219-5e897342844a	6	1	1	8	\N	\N	\N	\N	\N
865	1	6	733f3573-1e3c-4329-84cd-104144d8ce0d	20	24	2	11	admin	2024-02-19T07:53:25.807Z	admin	2024-02-19T07:53:25.807Z	\N
18	1	6	d37ab7e8-f181-41ed-822a-8f22f6626429	6	24	2	13	System	2024-02-19T07:51:11.554Z	System	2024-02-19T07:51:11.554Z	\N
874	1	6	f1f770e5-2436-4603-952b-f991142de0ce	20	24	2	11	admin	2024-02-19T07:53:25.936Z	admin	2024-02-19T07:53:25.936Z	\N
20	2	6	cf939649-ae9f-413b-af5d-f2e6ebd45c63	6	24	2	16	System	2024-02-19T07:51:11.573Z	System	2024-02-19T07:51:11.573Z	\N
22	1	6	38825a69-02ea-45e2-8cda-c14e7c90c471	6	24	2	13	System	2024-02-19T07:51:11.594Z	System	2024-02-19T07:51:11.594Z	\N
872	3	6	1e200347-b609-4156-bbbf-79d0769cb4f8	20	24	2	11	admin	2024-02-19T07:53:25.929Z	admin	2024-02-19T07:53:25.929Z	\N
873	3	6	0aba200c-b44e-47f7-baac-c67d3190c930	20	24	2	11	admin	2024-02-19T07:53:25.932Z	admin	2024-02-19T07:53:25.932Z	\N
24	2	6	7131ee35-6e4c-4794-a938-aecd91237a7b	6	24	2	20	System	2024-02-19T07:51:11.612Z	System	2024-02-19T07:51:11.612Z	\N
25	2	6	64d6c528-7ece-44a9-827d-6ae2d7b67200	6	24	2	22	System	2024-02-19T07:51:11.624Z	System	2024-02-19T07:51:11.624Z	\N
26	1	6	81dcd7a7-c892-4354-8537-07f164eb6116	6	24	2	11	System	2024-02-19T07:51:11.638Z	System	2024-02-19T07:51:11.638Z	\N
866	2	6	0218e1fe-48d6-40b4-aeae-7916b0411976	21	51	2	11	admin	2024-02-19T07:53:25.838Z	admin	2024-02-19T07:53:26.027Z	\N
27	2	6	3909ca5e-607c-403e-bbe4-992fbdbed0de	6	24	2	24	System	2024-02-19T07:51:11.642Z	System	2024-02-19T07:51:11.642Z	\N
28	1	6	69482b42-a90f-4857-a27f-1a16b76d74e2	6	24	2	11	System	2024-02-19T07:51:11.653Z	System	2024-02-19T07:51:11.653Z	\N
29	1	6	f3e091c7-93d7-45e4-b46d-b2bcd004e388	6	24	2	11	System	2024-02-19T07:51:11.658Z	System	2024-02-19T07:51:11.658Z	\N
867	2	6	c2e7fd01-bf43-4b0a-ad67-e9c85fe2b87e	21	51	2	11	admin	2024-02-19T07:53:25.881Z	admin	2024-02-19T07:53:26.035Z	\N
30	2	6	84b1e20c-3892-4085-ab11-1596b718908e	6	3	2	26	\N	\N	\N	\N	\N
31	1	6	5c51d760-16ef-4243-aa4b-7d61e65a33cb	6	3	2	27	\N	\N	\N	\N	\N
868	2	6	5c3b45b3-dd30-4919-b389-c13aa8b74ab7	21	51	2	11	admin	2024-02-19T07:53:25.891Z	admin	2024-02-19T07:53:26.042Z	\N
869	2	6	eb17d44d-35ef-4683-bf56-d957b1373348	21	51	2	11	admin	2024-02-19T07:53:25.902Z	admin	2024-02-19T07:53:26.049Z	\N
870	2	6	10e5a036-a6b1-42fa-87a9-c865c48402cb	21	51	2	11	admin	2024-02-19T07:53:25.911Z	admin	2024-02-19T07:53:26.057Z	\N
33	2	6	756a3630-041e-4cc1-8f54-62a940dad589	6	35	2	30	\N	\N	\N	\N	\N
34	1	6	bf37ae02-33c9-41b7-89bb-5bda4260a692	6	3	2	27	\N	\N	\N	\N	\N
35	1	6	88b473b0-bcb3-4c5a-8b43-6d8cf9d5cccc	6	46	2	9	System	2024-02-19T07:51:11.714Z	System	2024-02-19T07:51:11.714Z	\N
36	1	6	02b5a54d-3a5d-4efd-99c4-c5dd87f0bb0a	6	47	2	9	System	2024-02-19T07:51:11.717Z	System	2024-02-19T07:51:11.717Z	\N
37	1	6	09b7dc5a-a4fe-4f72-92ae-6eaeaf29ef6e	6	47	2	9	System	2024-02-19T07:51:11.719Z	System	2024-02-19T07:51:11.719Z	\N
871	2	6	03e56642-3118-4362-a30a-6688696edad5	21	51	2	11	admin	2024-02-19T07:53:25.921Z	admin	2024-02-19T07:53:26.065Z	\N
875	2	6	e2ef873e-1c17-43bf-bf92-baa787cbd98f	21	51	2	11	admin	2024-02-19T07:53:25.943Z	admin	2024-02-19T07:53:26.073Z	\N
32	3	6	80b93068-67ad-4fba-bb45-e07da8c7084f	11	35	2	28	\N	\N	\N	\N	\N
7	4	2	8a59adc0-e00b-4d7b-b1e8-e87efbc1ed16	14	12	2	4	\N	\N	\N	\N	\N
884	1	6	3669c5b2-8eda-4496-b742-3efc3391fc08	31	141	2	11	admin	2024-02-19T08:00:33.941Z	admin	2024-02-19T08:00:33.941Z	\N
886	1	4	7710873a-2146-47a8-8031-1727a4b2e4e4	33	164	4	60	admin	2024-02-19T08:09:32.205Z	admin	2024-02-19T08:09:32.205Z	\N
887	1	4	51af6416-e5bf-4f8b-9511-7bdd8bb5b098	33	51	4	60	admin	2024-02-19T08:09:32.210Z	admin	2024-02-19T08:09:32.210Z	\N
38	1	6	68fca92b-7a4c-4c59-acd9-20f259cc7139	6	47	2	9	System	2024-02-19T07:51:11.721Z	System	2024-02-19T07:51:11.721Z	\N
39	1	6	c331a96e-6a6c-4faa-b1bc-d091904bae21	6	47	2	9	System	2024-02-19T07:51:11.724Z	System	2024-02-19T07:51:11.724Z	\N
40	1	6	c51f8f82-02ac-4542-b13c-2309bf15498d	6	47	2	9	System	2024-02-19T07:51:11.726Z	System	2024-02-19T07:51:11.726Z	\N
41	1	6	ebff74b7-cbaf-4d33-9233-312bad2ba6c5	6	47	2	9	System	2024-02-19T07:51:11.729Z	System	2024-02-19T07:51:11.729Z	\N
42	1	6	6aafd2fd-a004-4270-b621-c7a42b679ab4	6	47	2	9	System	2024-02-19T07:51:11.732Z	System	2024-02-19T07:51:11.732Z	\N
43	1	6	3405d75e-368c-4496-88e0-44faf8c81fe6	6	47	2	9	System	2024-02-19T07:51:11.735Z	System	2024-02-19T07:51:11.735Z	\N
44	1	6	957f8a03-4dd4-486a-85be-522987549fa6	6	47	2	9	System	2024-02-19T07:51:11.739Z	System	2024-02-19T07:51:11.739Z	\N
45	1	6	5a6dda91-4c63-4efc-af65-fc58f904af6e	6	47	2	9	System	2024-02-19T07:51:11.741Z	System	2024-02-19T07:51:11.741Z	\N
46	1	6	3290998a-d605-496b-a70a-a5af643c4d8f	6	47	2	9	System	2024-02-19T07:51:11.743Z	System	2024-02-19T07:51:11.743Z	\N
47	1	6	f339e93a-42ce-4043-aa13-f520d8ba17b2	6	47	2	9	System	2024-02-19T07:51:11.745Z	System	2024-02-19T07:51:11.745Z	\N
48	1	6	68f3ad54-2b86-48b9-a613-b73953b8266e	6	47	2	9	System	2024-02-19T07:51:11.747Z	System	2024-02-19T07:51:11.747Z	\N
49	1	6	abde394a-2138-4c46-82d3-7b9a6657051a	6	47	2	9	System	2024-02-19T07:51:11.749Z	System	2024-02-19T07:51:11.749Z	\N
50	1	6	7b49cfd3-1702-44dc-8e37-e0353a895033	6	47	2	9	System	2024-02-19T07:51:11.751Z	System	2024-02-19T07:51:11.751Z	\N
51	1	6	7b3a153f-bdf6-4b5d-9fdd-7de179e12642	6	47	2	9	System	2024-02-19T07:51:11.753Z	System	2024-02-19T07:51:11.753Z	\N
52	1	6	403d2c0d-02c2-4db7-9146-e2c458032fe3	6	47	2	9	System	2024-02-19T07:51:11.756Z	System	2024-02-19T07:51:11.756Z	\N
53	1	6	9ed55c58-a2f2-4fc2-9ce4-099304cac093	6	47	2	9	System	2024-02-19T07:51:11.758Z	System	2024-02-19T07:51:11.758Z	\N
54	1	6	3f8c9a03-e367-480a-847a-442cd8325db3	6	47	2	9	System	2024-02-19T07:51:11.761Z	System	2024-02-19T07:51:11.761Z	\N
55	1	6	861ab78b-280a-451e-8683-31ea450218dd	6	47	2	9	System	2024-02-19T07:51:11.764Z	System	2024-02-19T07:51:11.764Z	\N
56	1	6	d2647f11-193c-4fb2-89aa-9865fc7d65d0	6	47	2	9	System	2024-02-19T07:51:11.766Z	System	2024-02-19T07:51:11.766Z	\N
57	1	6	337d6833-02ab-4eb2-b0d2-0211bb4c137f	6	47	2	9	System	2024-02-19T07:51:11.768Z	System	2024-02-19T07:51:11.768Z	\N
58	1	6	6464d4fa-82ce-4825-a936-caaf31be910c	6	47	2	9	System	2024-02-19T07:51:11.770Z	System	2024-02-19T07:51:11.770Z	\N
59	1	6	085d9dc6-9cc5-4387-b312-1f7153b0534a	6	47	2	9	System	2024-02-19T07:51:11.772Z	System	2024-02-19T07:51:11.772Z	\N
60	1	6	2c4f28e6-3356-45ae-8858-c0d811189988	6	47	2	9	System	2024-02-19T07:51:11.774Z	System	2024-02-19T07:51:11.774Z	\N
61	1	6	af5bdf58-0d50-48fe-8c32-fda62e3005d9	6	47	2	9	System	2024-02-19T07:51:11.776Z	System	2024-02-19T07:51:11.776Z	\N
62	1	6	de7ef501-d044-4b8c-983d-0897dd3a8fd9	6	47	2	9	System	2024-02-19T07:51:11.777Z	System	2024-02-19T07:51:11.777Z	\N
63	1	6	9615717e-05ce-44ce-9bdd-146f89c884c4	6	47	2	9	System	2024-02-19T07:51:11.931Z	System	2024-02-19T07:51:11.931Z	\N
64	1	6	5d9fa746-4cb7-4e08-a6b6-d773813ae946	6	47	2	9	System	2024-02-19T07:51:11.934Z	System	2024-02-19T07:51:11.934Z	\N
65	1	6	066eec83-fef5-4c4f-9cea-b7b0d72ffc44	6	47	2	9	System	2024-02-19T07:51:11.936Z	System	2024-02-19T07:51:11.936Z	\N
66	1	6	eed96662-aca4-40be-937d-6d7447626d7e	6	47	2	9	System	2024-02-19T07:51:11.938Z	System	2024-02-19T07:51:11.938Z	\N
67	1	6	a080e731-0ca8-4a14-ab5d-35ea6e1fc08e	6	47	2	9	System	2024-02-19T07:51:11.940Z	System	2024-02-19T07:51:11.940Z	\N
68	1	6	c7b84fba-e873-45b5-a894-e50836c37b1d	6	47	2	9	System	2024-02-19T07:51:11.942Z	System	2024-02-19T07:51:11.942Z	\N
69	1	6	2db55c8b-287a-4dea-bb3a-9f226d17ba3e	6	47	2	9	System	2024-02-19T07:51:11.944Z	System	2024-02-19T07:51:11.944Z	\N
70	1	6	fb958685-2316-46c3-93f1-6f18bd4b4fbd	6	47	2	9	System	2024-02-19T07:51:11.945Z	System	2024-02-19T07:51:11.945Z	\N
71	1	6	f16d97fe-7a1d-4c83-b885-9c6767a79cf3	6	47	2	9	System	2024-02-19T07:51:11.947Z	System	2024-02-19T07:51:11.947Z	\N
72	1	6	2ba2bcf8-bf32-478a-8b74-be7dae6c1af6	6	47	2	9	System	2024-02-19T07:51:11.949Z	System	2024-02-19T07:51:11.949Z	\N
73	1	6	0dc525e1-6a32-4676-9dd1-e44c78cbd366	6	47	2	9	System	2024-02-19T07:51:11.951Z	System	2024-02-19T07:51:11.951Z	\N
74	1	6	1424243c-f8d7-4e8f-8f2e-09552db6bcf5	6	47	2	9	System	2024-02-19T07:51:11.953Z	System	2024-02-19T07:51:11.953Z	\N
75	1	6	4d20dcd8-d47e-4df4-81a8-e5e462665679	6	47	2	9	System	2024-02-19T07:51:11.955Z	System	2024-02-19T07:51:11.955Z	\N
76	1	6	bfd6fbbe-8aa4-4be1-b68a-10505100c5aa	6	47	2	9	System	2024-02-19T07:51:11.957Z	System	2024-02-19T07:51:11.957Z	\N
77	1	6	723f29da-3293-459b-98be-06f985db64b8	6	47	2	9	System	2024-02-19T07:51:11.958Z	System	2024-02-19T07:51:11.958Z	\N
78	1	6	4c25bf48-8aeb-43cc-84f9-2c867d98ec8d	6	47	2	9	System	2024-02-19T07:51:11.960Z	System	2024-02-19T07:51:11.960Z	\N
79	1	6	510f1f45-c904-446f-a1f3-4cb285e6e5b7	6	47	2	9	System	2024-02-19T07:51:11.962Z	System	2024-02-19T07:51:11.962Z	\N
80	1	6	4b5af67b-f08e-446e-9e1a-1f65fe3797a1	6	47	2	9	System	2024-02-19T07:51:11.964Z	System	2024-02-19T07:51:11.964Z	\N
81	1	6	d1bc574e-86f3-49c1-acd9-cf754f2931e8	6	47	2	9	System	2024-02-19T07:51:11.966Z	System	2024-02-19T07:51:11.966Z	\N
82	1	6	6d032ceb-d317-4da7-9970-7935b6e63d79	6	47	2	9	System	2024-02-19T07:51:11.968Z	System	2024-02-19T07:51:11.968Z	\N
83	1	6	fae53487-e4f4-425d-ba08-74eee21b990e	6	47	2	9	System	2024-02-19T07:51:11.970Z	System	2024-02-19T07:51:11.970Z	\N
84	1	6	771c16b2-70a4-4854-80b3-d984ee11f023	6	47	2	9	System	2024-02-19T07:51:11.972Z	System	2024-02-19T07:51:11.972Z	\N
85	1	6	0d526019-b01f-4510-92c3-34f5c7a49bfe	6	47	2	9	System	2024-02-19T07:51:11.974Z	System	2024-02-19T07:51:11.974Z	\N
86	1	6	c4a9489a-1dbd-45a0-9067-84f8bbe395e8	6	47	2	9	System	2024-02-19T07:51:11.976Z	System	2024-02-19T07:51:11.976Z	\N
87	1	6	8cbc9f6d-f891-49bf-ab19-004c601b49f3	6	47	2	9	System	2024-02-19T07:51:11.978Z	System	2024-02-19T07:51:11.978Z	\N
88	1	6	ed82f703-24c5-4b68-8ea9-0ece097b283a	6	47	2	9	System	2024-02-19T07:51:11.980Z	System	2024-02-19T07:51:11.980Z	\N
89	1	6	ddf7d2b1-c274-4a76-b43c-b609ac1f1031	6	47	2	9	System	2024-02-19T07:51:11.982Z	System	2024-02-19T07:51:11.982Z	\N
90	1	6	795b8258-664c-49b3-93da-134be5fc606f	6	47	2	9	System	2024-02-19T07:51:11.983Z	System	2024-02-19T07:51:11.983Z	\N
91	1	6	bd3eff4f-ef3d-47a6-9343-25d2b9a96846	6	47	2	9	System	2024-02-19T07:51:11.985Z	System	2024-02-19T07:51:11.985Z	\N
92	1	6	2c29b0a4-5ec8-448a-b7df-2a3614fa0b20	6	47	2	9	System	2024-02-19T07:51:11.987Z	System	2024-02-19T07:51:11.987Z	\N
93	1	6	a65fee71-7391-4812-8ad8-77630d3bf0ae	6	47	2	9	System	2024-02-19T07:51:11.988Z	System	2024-02-19T07:51:11.988Z	\N
94	1	6	d57528d9-dcb7-478e-81f4-afb066cad915	6	47	2	9	System	2024-02-19T07:51:11.990Z	System	2024-02-19T07:51:11.990Z	\N
95	1	6	25805c3b-dc41-4a3d-9b03-a44ac7963c70	6	47	2	9	System	2024-02-19T07:51:11.991Z	System	2024-02-19T07:51:11.991Z	\N
96	1	6	73b53440-09c8-484c-80c8-b874b949715b	6	47	2	9	System	2024-02-19T07:51:11.993Z	System	2024-02-19T07:51:11.993Z	\N
97	1	6	44f3ca4f-92d3-4ce6-af8b-a7308f5a6d53	6	47	2	9	System	2024-02-19T07:51:11.995Z	System	2024-02-19T07:51:11.995Z	\N
98	1	6	100b11df-0879-42c3-90ae-5667f2ad9d79	6	47	2	9	System	2024-02-19T07:51:11.996Z	System	2024-02-19T07:51:11.996Z	\N
99	1	6	ac6ffe87-24c9-494d-8874-5f717dadd007	6	47	2	9	System	2024-02-19T07:51:11.998Z	System	2024-02-19T07:51:11.998Z	\N
100	1	6	33c5ffc2-a629-4f1a-85ec-d4f44f4edc8f	6	47	2	9	System	2024-02-19T07:51:12.000Z	System	2024-02-19T07:51:12.000Z	\N
101	1	6	0cae1d98-fa1b-4142-b7a2-f021ea5bf262	6	47	2	9	System	2024-02-19T07:51:12.001Z	System	2024-02-19T07:51:12.001Z	\N
102	1	6	f59ff026-e40e-4ce9-aeee-66117ef41d1c	6	47	2	9	System	2024-02-19T07:51:12.003Z	System	2024-02-19T07:51:12.003Z	\N
103	1	6	6f43039f-def2-48b7-822e-f5ccab234b73	6	47	2	9	System	2024-02-19T07:51:12.004Z	System	2024-02-19T07:51:12.004Z	\N
104	1	6	c7942154-94cb-48f4-8aad-17792618fd4d	6	47	2	9	System	2024-02-19T07:51:12.006Z	System	2024-02-19T07:51:12.006Z	\N
105	1	6	4dcadca2-dc27-4c8f-aad8-a343dec4d520	6	47	2	9	System	2024-02-19T07:51:12.008Z	System	2024-02-19T07:51:12.008Z	\N
106	1	6	4cd9c479-24b7-44ff-aaa7-fac1a9fd07db	6	47	2	9	System	2024-02-19T07:51:12.009Z	System	2024-02-19T07:51:12.009Z	\N
107	1	6	205a8b27-5d21-4047-ab93-38fabe65ab46	6	47	2	9	System	2024-02-19T07:51:12.011Z	System	2024-02-19T07:51:12.011Z	\N
108	1	6	42936696-c2d5-4a7b-aa36-e0c90554ec6b	6	47	2	9	System	2024-02-19T07:51:12.012Z	System	2024-02-19T07:51:12.012Z	\N
109	1	6	64c6c0e2-859a-4fbe-87d8-18ed536adbc1	6	47	2	9	System	2024-02-19T07:51:12.014Z	System	2024-02-19T07:51:12.014Z	\N
110	1	6	dca15aad-5857-47a7-8a12-cdd514c731c1	6	47	2	9	System	2024-02-19T07:51:12.016Z	System	2024-02-19T07:51:12.016Z	\N
111	1	6	51ffd4bb-4d28-482e-bd18-4f8bb5e1b1c6	6	47	2	9	System	2024-02-19T07:51:12.018Z	System	2024-02-19T07:51:12.018Z	\N
112	1	6	95bfc8fc-f0b1-42af-9495-acb21b91a108	6	47	2	9	System	2024-02-19T07:51:12.020Z	System	2024-02-19T07:51:12.020Z	\N
113	1	6	aca020a8-3780-4728-bdaf-68ff7ab43ce6	6	47	2	9	System	2024-02-19T07:51:12.021Z	System	2024-02-19T07:51:12.021Z	\N
114	1	6	b150ac41-7ef4-4d8c-8084-a5ea3337cb13	6	47	2	9	System	2024-02-19T07:51:12.023Z	System	2024-02-19T07:51:12.023Z	\N
115	1	6	0d9a046e-571e-4d2c-83c2-4cbeca9c8d15	6	47	2	9	System	2024-02-19T07:51:12.024Z	System	2024-02-19T07:51:12.024Z	\N
116	1	6	619f132b-6360-431d-b92c-fc0a140856e5	6	47	2	9	System	2024-02-19T07:51:12.028Z	System	2024-02-19T07:51:12.028Z	\N
117	1	6	c6e8f923-79af-49e2-9113-f45c1b8c6305	6	47	2	9	System	2024-02-19T07:51:12.030Z	System	2024-02-19T07:51:12.030Z	\N
118	1	6	cc744852-4eaf-4c18-822c-505aae8ab0b6	6	47	2	9	System	2024-02-19T07:51:12.032Z	System	2024-02-19T07:51:12.032Z	\N
119	1	6	b09abd23-52da-42f4-8d9d-f0aefa694377	6	47	2	9	System	2024-02-19T07:51:12.033Z	System	2024-02-19T07:51:12.033Z	\N
120	1	6	59fe539b-cd84-4cc6-a2bf-0b74411c7e79	6	47	2	9	System	2024-02-19T07:51:12.035Z	System	2024-02-19T07:51:12.035Z	\N
121	1	6	6def5ee4-3176-4170-9796-4f59e895dfdb	6	47	2	9	System	2024-02-19T07:51:12.036Z	System	2024-02-19T07:51:12.036Z	\N
122	1	6	cf6b66b1-8441-4747-9120-d6e65f0b51d3	6	47	2	9	System	2024-02-19T07:51:12.038Z	System	2024-02-19T07:51:12.038Z	\N
123	1	6	f073d1d6-4a55-427e-8ca0-5f7f774cfbe1	6	47	2	9	System	2024-02-19T07:51:12.040Z	System	2024-02-19T07:51:12.040Z	\N
124	1	6	620bde3e-95b9-4bd9-8e9d-5c7a24d2ed35	6	47	2	9	System	2024-02-19T07:51:12.042Z	System	2024-02-19T07:51:12.042Z	\N
125	1	6	6255dccc-7296-4fc9-aef6-c460fee54eda	6	47	2	9	System	2024-02-19T07:51:12.043Z	System	2024-02-19T07:51:12.043Z	\N
126	1	6	e9e2b877-609b-497b-a61c-e3037a31c804	6	47	2	9	System	2024-02-19T07:51:12.045Z	System	2024-02-19T07:51:12.045Z	\N
127	1	6	87b51fbd-972a-4ba7-ab29-afcedc93d7de	6	47	2	9	System	2024-02-19T07:51:12.047Z	System	2024-02-19T07:51:12.047Z	\N
128	1	6	dd3f715b-ddad-4827-b113-bf175bba3aae	6	47	2	9	System	2024-02-19T07:51:12.048Z	System	2024-02-19T07:51:12.048Z	\N
129	1	6	9dfee88d-6119-4411-954f-2484320bf72b	6	47	2	9	System	2024-02-19T07:51:12.050Z	System	2024-02-19T07:51:12.050Z	\N
130	1	6	4508e6fc-98a2-4062-9302-70336adc9eb2	6	47	2	9	System	2024-02-19T07:51:12.051Z	System	2024-02-19T07:51:12.051Z	\N
131	1	6	d1044a5e-2387-446d-9401-51b4f432fe83	6	47	2	9	System	2024-02-19T07:51:12.053Z	System	2024-02-19T07:51:12.053Z	\N
132	1	6	e03abda9-ec66-4c01-8298-6478a9cc1a8d	6	47	2	9	System	2024-02-19T07:51:12.055Z	System	2024-02-19T07:51:12.055Z	\N
133	1	6	fd6449d6-23ef-4ccc-adc1-6195f15d393e	6	47	2	9	System	2024-02-19T07:51:12.056Z	System	2024-02-19T07:51:12.056Z	\N
134	1	6	50debb56-bf8e-47c3-845f-455036f19e65	6	47	2	9	System	2024-02-19T07:51:12.058Z	System	2024-02-19T07:51:12.058Z	\N
135	1	6	bc431fc8-70c1-42ed-9236-721d71538001	6	47	2	9	System	2024-02-19T07:51:12.062Z	System	2024-02-19T07:51:12.062Z	\N
136	1	6	9b1b986b-018e-4b86-9ec7-c205c7637e88	6	47	2	9	System	2024-02-19T07:51:12.064Z	System	2024-02-19T07:51:12.064Z	\N
137	1	6	45fb0c34-9ece-46a1-8694-ab64639e4a9f	6	47	2	9	System	2024-02-19T07:51:12.065Z	System	2024-02-19T07:51:12.065Z	\N
138	1	6	bba8f6e5-39ab-45e6-a1a9-a18e07d04049	6	47	2	9	System	2024-02-19T07:51:12.067Z	System	2024-02-19T07:51:12.067Z	\N
139	1	6	b070a340-688b-47ce-bc77-9a18dbdcc0d2	6	47	2	9	System	2024-02-19T07:51:12.068Z	System	2024-02-19T07:51:12.068Z	\N
140	1	6	897ef02a-c55e-4270-a06e-bd4a165e2b26	6	47	2	9	System	2024-02-19T07:51:12.070Z	System	2024-02-19T07:51:12.070Z	\N
141	1	6	258c97af-e631-4df1-8345-75dc643c1be6	6	47	2	9	System	2024-02-19T07:51:12.072Z	System	2024-02-19T07:51:12.072Z	\N
142	1	6	1abf5c78-008e-4f69-9bd1-36e3abace3ed	6	47	2	9	System	2024-02-19T07:51:12.073Z	System	2024-02-19T07:51:12.073Z	\N
143	1	6	d03a002e-925a-4b7b-9667-b9767890ed19	6	47	2	9	System	2024-02-19T07:51:12.075Z	System	2024-02-19T07:51:12.075Z	\N
144	1	6	5baa94bd-d828-4fb7-9c3d-13b3ee3eb9ef	6	47	2	9	System	2024-02-19T07:51:12.076Z	System	2024-02-19T07:51:12.076Z	\N
145	1	6	bcd238b4-a0cf-49d1-a264-c7caba5f5d1d	6	47	2	9	System	2024-02-19T07:51:12.078Z	System	2024-02-19T07:51:12.078Z	\N
146	1	6	452d5813-c1fe-4906-bf79-cc0405bc64ac	6	47	2	9	System	2024-02-19T07:51:12.079Z	System	2024-02-19T07:51:12.079Z	\N
147	1	6	cb1b3688-b549-4159-96b2-eb57b088262d	6	47	2	9	System	2024-02-19T07:51:12.081Z	System	2024-02-19T07:51:12.081Z	\N
148	1	6	60d23560-9a2f-4594-8b8d-76967d2604ef	6	47	2	9	System	2024-02-19T07:51:12.082Z	System	2024-02-19T07:51:12.082Z	\N
149	1	6	aa35d404-16d8-4d94-a332-4b2cae01a628	6	47	2	9	System	2024-02-19T07:51:12.084Z	System	2024-02-19T07:51:12.084Z	\N
150	1	6	344ae6c6-1e40-4662-803c-d1467b312992	6	47	2	9	System	2024-02-19T07:51:12.085Z	System	2024-02-19T07:51:12.085Z	\N
151	1	6	0eebac25-60d9-41e1-a7a6-a4cf1b99f6be	6	47	2	9	System	2024-02-19T07:51:12.087Z	System	2024-02-19T07:51:12.087Z	\N
152	1	6	ff2839ec-35fa-4749-a03e-bcff561857f0	6	47	2	9	System	2024-02-19T07:51:12.089Z	System	2024-02-19T07:51:12.089Z	\N
153	1	6	c76b6274-6cbd-4f26-8f2c-c5ab82ebc848	6	47	2	9	System	2024-02-19T07:51:12.090Z	System	2024-02-19T07:51:12.090Z	\N
154	1	6	1b1d3713-08bb-41ce-b636-bb1531621803	6	47	2	9	System	2024-02-19T07:51:12.092Z	System	2024-02-19T07:51:12.092Z	\N
155	1	6	44abc13d-ba02-4996-a4b4-629d5502656b	6	47	2	9	System	2024-02-19T07:51:12.094Z	System	2024-02-19T07:51:12.094Z	\N
156	1	6	bdf22486-9807-4344-a48d-51cf45f744f9	6	47	2	9	System	2024-02-19T07:51:12.096Z	System	2024-02-19T07:51:12.096Z	\N
157	1	6	a0ec4c19-edf2-4228-a4a6-bdc341c1947e	6	47	2	9	System	2024-02-19T07:51:12.097Z	System	2024-02-19T07:51:12.097Z	\N
158	1	6	942e188a-e191-4af0-b897-63c1594f8faf	6	47	2	9	System	2024-02-19T07:51:12.099Z	System	2024-02-19T07:51:12.099Z	\N
159	1	6	e6fa768a-0a00-4f36-94fd-41c6e2a0234a	6	47	2	9	System	2024-02-19T07:51:12.100Z	System	2024-02-19T07:51:12.100Z	\N
160	1	6	ef002262-f0e9-4d2b-9197-cf6f2d22e3a2	6	47	2	9	System	2024-02-19T07:51:12.102Z	System	2024-02-19T07:51:12.102Z	\N
161	1	6	0fe13372-d3d4-4123-8c3f-81693d60d700	6	47	2	9	System	2024-02-19T07:51:12.103Z	System	2024-02-19T07:51:12.103Z	\N
162	1	6	306b1b74-6100-4f97-b7fb-e56f9e04911b	6	47	2	9	System	2024-02-19T07:51:12.105Z	System	2024-02-19T07:51:12.105Z	\N
163	1	6	272a68f3-3d3c-4d2f-b2f0-8f95b6126d1c	6	47	2	9	System	2024-02-19T07:51:12.106Z	System	2024-02-19T07:51:12.106Z	\N
164	1	6	2d5b83d3-f402-4d56-b0c2-2edb82689575	6	47	2	9	System	2024-02-19T07:51:12.108Z	System	2024-02-19T07:51:12.108Z	\N
165	1	6	020c2282-87cc-480e-86cc-3278b349e70d	6	47	2	9	System	2024-02-19T07:51:12.109Z	System	2024-02-19T07:51:12.109Z	\N
166	1	6	d90520f4-49f1-49b9-933c-c8fba08c666f	6	47	2	9	System	2024-02-19T07:51:12.110Z	System	2024-02-19T07:51:12.110Z	\N
167	1	6	2976b6aa-3b56-43f4-b879-ce07e1b89c43	6	47	2	9	System	2024-02-19T07:51:12.112Z	System	2024-02-19T07:51:12.112Z	\N
168	1	6	57e3066d-48ab-4fb2-a6eb-00e3a8a70cd9	6	47	2	9	System	2024-02-19T07:51:12.113Z	System	2024-02-19T07:51:12.113Z	\N
169	1	6	5e7cc0e6-75e3-48cc-9719-3df258c6fd22	6	47	2	9	System	2024-02-19T07:51:12.115Z	System	2024-02-19T07:51:12.115Z	\N
170	1	6	d8450be5-493a-41fe-be96-6c6727b13c75	6	47	2	9	System	2024-02-19T07:51:12.116Z	System	2024-02-19T07:51:12.116Z	\N
171	1	6	12d7922f-3b7d-4dc0-b3ff-6394af567d6b	6	47	2	9	System	2024-02-19T07:51:12.118Z	System	2024-02-19T07:51:12.118Z	\N
172	1	6	05fd021c-6170-480f-9eca-63714250daf2	6	47	2	9	System	2024-02-19T07:51:12.120Z	System	2024-02-19T07:51:12.120Z	\N
173	1	6	653118cb-3bf1-440d-b484-cb849b1de384	6	47	2	9	System	2024-02-19T07:51:12.121Z	System	2024-02-19T07:51:12.121Z	\N
174	1	6	2838ec54-3706-4f15-a638-a2266d38fd62	6	47	2	9	System	2024-02-19T07:51:12.123Z	System	2024-02-19T07:51:12.123Z	\N
175	1	6	42ec4dd5-cbc4-4415-bcd3-9772ac98fd0f	6	47	2	9	System	2024-02-19T07:51:12.125Z	System	2024-02-19T07:51:12.125Z	\N
176	1	6	e682b9d1-024b-4a88-a291-c37b0be32dc9	6	47	2	9	System	2024-02-19T07:51:12.126Z	System	2024-02-19T07:51:12.126Z	\N
177	1	6	1cccf5a6-3798-4c61-8847-c033787e4769	6	47	2	9	System	2024-02-19T07:51:12.128Z	System	2024-02-19T07:51:12.128Z	\N
178	1	6	8b89f3fe-960b-48ae-beee-94c9292e286c	6	47	2	9	System	2024-02-19T07:51:12.131Z	System	2024-02-19T07:51:12.131Z	\N
179	1	6	09f3ce9d-013a-40d5-b2a4-cca15cea3a63	6	47	2	9	System	2024-02-19T07:51:12.133Z	System	2024-02-19T07:51:12.133Z	\N
180	1	6	1483e0e6-a6c4-4d30-92ac-fce1187312b5	6	47	2	9	System	2024-02-19T07:51:12.135Z	System	2024-02-19T07:51:12.135Z	\N
181	1	6	655820a5-f93c-45c1-ba84-299ed0ceb5aa	6	47	2	9	System	2024-02-19T07:51:12.136Z	System	2024-02-19T07:51:12.136Z	\N
182	1	6	30b6f8df-e1ab-488a-97ae-ae0b7664ff81	6	47	2	9	System	2024-02-19T07:51:12.138Z	System	2024-02-19T07:51:12.138Z	\N
183	1	6	35da4a2c-1a39-43ec-b174-f750fd199e15	6	47	2	9	System	2024-02-19T07:51:12.140Z	System	2024-02-19T07:51:12.140Z	\N
184	1	6	22389c7c-9421-43df-b434-1aa8e5e1f3e3	6	47	2	9	System	2024-02-19T07:51:12.141Z	System	2024-02-19T07:51:12.141Z	\N
185	1	6	d198a0d3-8ef6-4878-b209-b8108867f2eb	6	47	2	9	System	2024-02-19T07:51:12.143Z	System	2024-02-19T07:51:12.143Z	\N
186	1	6	60030206-04cc-440e-99ab-7490fa1c03e8	6	47	2	9	System	2024-02-19T07:51:12.145Z	System	2024-02-19T07:51:12.145Z	\N
187	1	6	a4dd9adc-cbf5-49b0-b82c-fae1d76cb59f	6	47	2	9	System	2024-02-19T07:51:12.146Z	System	2024-02-19T07:51:12.146Z	\N
188	1	6	30d79494-1929-432a-b6d4-4215b8c09ec1	6	47	2	9	System	2024-02-19T07:51:12.148Z	System	2024-02-19T07:51:12.148Z	\N
189	1	6	2b9583ab-1f3b-49f4-8918-b34f23971404	6	47	2	9	System	2024-02-19T07:51:12.149Z	System	2024-02-19T07:51:12.149Z	\N
190	1	6	55459c33-bada-419f-9cd7-ee4c245b956e	6	47	2	9	System	2024-02-19T07:51:12.151Z	System	2024-02-19T07:51:12.151Z	\N
191	1	6	e3a3e715-62d9-4d74-9e4c-4ee058cca96b	6	47	2	9	System	2024-02-19T07:51:12.153Z	System	2024-02-19T07:51:12.153Z	\N
192	1	6	b051ddaf-fd6e-4e20-920e-55987ca4f9ba	6	47	2	9	System	2024-02-19T07:51:12.154Z	System	2024-02-19T07:51:12.154Z	\N
193	1	6	201435a8-fbfd-4808-a119-ef0d1a3d9d3c	6	47	2	9	System	2024-02-19T07:51:12.155Z	System	2024-02-19T07:51:12.155Z	\N
194	1	6	11f19b19-366a-46b7-b441-bf80c9e12e3c	6	47	2	9	System	2024-02-19T07:51:12.157Z	System	2024-02-19T07:51:12.157Z	\N
195	1	6	4a016029-4293-4de4-a0f2-f41f169956c2	6	47	2	9	System	2024-02-19T07:51:12.159Z	System	2024-02-19T07:51:12.159Z	\N
196	1	6	488cc546-6152-4543-aa65-255d1b367727	6	47	2	9	System	2024-02-19T07:51:12.160Z	System	2024-02-19T07:51:12.160Z	\N
197	1	6	b08598ca-28c7-470e-9192-707a4f01c55e	6	47	2	9	System	2024-02-19T07:51:12.162Z	System	2024-02-19T07:51:12.162Z	\N
198	1	6	b5d4e956-189e-4c77-b833-d2244307fa58	6	47	2	9	System	2024-02-19T07:51:12.163Z	System	2024-02-19T07:51:12.163Z	\N
199	1	6	301f975f-aa12-4548-adb6-5d7d7b556737	6	47	2	9	System	2024-02-19T07:51:12.165Z	System	2024-02-19T07:51:12.165Z	\N
200	1	6	98f070f1-1e45-4167-adc0-4efee41c2440	6	47	2	9	System	2024-02-19T07:51:12.167Z	System	2024-02-19T07:51:12.167Z	\N
201	1	6	1abad29f-1ee1-40e3-b139-8b1d5cd558c1	6	47	2	9	System	2024-02-19T07:51:12.168Z	System	2024-02-19T07:51:12.168Z	\N
202	1	6	72359284-9260-4ba3-b64f-546b3f973cf8	6	47	2	9	System	2024-02-19T07:51:12.170Z	System	2024-02-19T07:51:12.170Z	\N
203	1	6	3a0246e3-275b-4ff9-8f75-1f2a49e7c8e2	6	47	2	9	System	2024-02-19T07:51:12.174Z	System	2024-02-19T07:51:12.174Z	\N
204	1	6	dc23330d-0926-4972-a054-2f716cb1bcef	6	47	2	9	System	2024-02-19T07:51:12.175Z	System	2024-02-19T07:51:12.175Z	\N
205	1	6	dd092e23-c3e4-4676-9d76-080b6e17f999	6	47	2	9	System	2024-02-19T07:51:12.177Z	System	2024-02-19T07:51:12.177Z	\N
206	1	6	9160afb3-2605-4f35-9826-d30c10d793e7	6	47	2	9	System	2024-02-19T07:51:12.178Z	System	2024-02-19T07:51:12.178Z	\N
207	1	6	24319943-f57b-40c1-979e-331072252ba2	6	47	2	9	System	2024-02-19T07:51:12.182Z	System	2024-02-19T07:51:12.182Z	\N
208	1	6	e98196c6-5a75-4e47-a7ad-7a70029e5cfb	6	47	2	9	System	2024-02-19T07:51:12.183Z	System	2024-02-19T07:51:12.183Z	\N
209	1	6	8a89ac6e-ccb8-4115-9b30-a2ce963c1fee	6	47	2	9	System	2024-02-19T07:51:12.185Z	System	2024-02-19T07:51:12.185Z	\N
210	1	6	77962b97-04d9-426d-b47e-00573e467d77	6	47	2	9	System	2024-02-19T07:51:12.187Z	System	2024-02-19T07:51:12.187Z	\N
211	1	6	ef162965-0c75-46f1-91d9-31e5902e75e4	6	47	2	9	System	2024-02-19T07:51:12.188Z	System	2024-02-19T07:51:12.188Z	\N
212	1	6	1ccff802-6ca4-4e5b-90ac-d89561333039	6	47	2	9	System	2024-02-19T07:51:12.189Z	System	2024-02-19T07:51:12.189Z	\N
213	1	6	61aba97a-6d6f-4100-8c3a-e0d24c589ce3	6	47	2	9	System	2024-02-19T07:51:12.191Z	System	2024-02-19T07:51:12.191Z	\N
214	1	6	e670b9c3-d509-4d5d-b710-83534fb62c31	6	47	2	9	System	2024-02-19T07:51:12.193Z	System	2024-02-19T07:51:12.193Z	\N
215	1	6	d0421998-c969-4557-9eee-07222c68da97	6	47	2	9	System	2024-02-19T07:51:12.194Z	System	2024-02-19T07:51:12.194Z	\N
216	1	6	8cb623ba-feef-437d-9a1c-d1fd40eb2d68	6	47	2	9	System	2024-02-19T07:51:12.196Z	System	2024-02-19T07:51:12.196Z	\N
217	1	6	0abe4db7-a20c-44c0-9a9d-bf8913c6f894	6	47	2	9	System	2024-02-19T07:51:12.213Z	System	2024-02-19T07:51:12.213Z	\N
218	1	6	727a8bf6-7e4f-451e-8eab-a92ce0d529d4	6	47	2	9	System	2024-02-19T07:51:12.215Z	System	2024-02-19T07:51:12.215Z	\N
219	1	6	c6e0f7e6-a234-489a-97c7-0320e5cb8dc9	6	47	2	9	System	2024-02-19T07:51:12.216Z	System	2024-02-19T07:51:12.216Z	\N
220	1	6	a27b7d48-3609-4ba5-aa09-6be2ad08dcdf	6	47	2	9	System	2024-02-19T07:51:12.217Z	System	2024-02-19T07:51:12.217Z	\N
221	1	6	e5555662-3362-49bd-acf3-9ff5fc3b849d	6	47	2	9	System	2024-02-19T07:51:12.219Z	System	2024-02-19T07:51:12.219Z	\N
222	1	6	449bf022-5581-4b15-a48a-9a19473e7432	6	47	2	9	System	2024-02-19T07:51:12.220Z	System	2024-02-19T07:51:12.220Z	\N
223	1	6	81aabebc-b696-48e6-a9cb-cc8e1c71a18b	6	47	2	9	System	2024-02-19T07:51:12.221Z	System	2024-02-19T07:51:12.221Z	\N
224	1	6	f14c5667-a78a-4c19-83ae-ccfed309291d	6	47	2	9	System	2024-02-19T07:51:12.223Z	System	2024-02-19T07:51:12.223Z	\N
225	1	6	edc600f7-93a3-4a8b-943b-b0a0d338f470	6	47	2	9	System	2024-02-19T07:51:12.224Z	System	2024-02-19T07:51:12.224Z	\N
226	1	6	6aaa29c2-7f1d-408b-9d07-89c0b4e84749	6	47	2	9	System	2024-02-19T07:51:12.226Z	System	2024-02-19T07:51:12.226Z	\N
227	1	6	81dc91d4-88b5-4c1e-b070-7eebb5030a4e	6	47	2	9	System	2024-02-19T07:51:12.227Z	System	2024-02-19T07:51:12.227Z	\N
228	1	6	f43e13fd-55dc-4e8a-8921-90db5b77ae00	6	47	2	9	System	2024-02-19T07:51:12.228Z	System	2024-02-19T07:51:12.228Z	\N
229	1	6	68fc967d-6964-4bd7-b525-a436e126da37	6	47	2	9	System	2024-02-19T07:51:12.230Z	System	2024-02-19T07:51:12.230Z	\N
230	1	6	f5618831-e270-4f03-adef-552ffc03e7e3	6	47	2	9	System	2024-02-19T07:51:12.231Z	System	2024-02-19T07:51:12.231Z	\N
231	1	6	7dd91426-6191-489d-b7c9-2c08c21e5be7	6	47	2	9	System	2024-02-19T07:51:12.232Z	System	2024-02-19T07:51:12.232Z	\N
232	1	6	8041b817-4ccd-41fa-81fa-43e2e86c3804	6	47	2	9	System	2024-02-19T07:51:12.234Z	System	2024-02-19T07:51:12.234Z	\N
233	1	6	cb4a078f-676d-4484-ba14-3d58dd9dbb23	6	47	2	9	System	2024-02-19T07:51:12.235Z	System	2024-02-19T07:51:12.235Z	\N
234	1	6	3357b18a-0c35-4eb4-b2a8-63c1c6165172	6	47	2	9	System	2024-02-19T07:51:12.237Z	System	2024-02-19T07:51:12.237Z	\N
235	1	6	a05b9ee8-d6b9-4d2b-a881-ee6ea2fee57a	6	47	2	9	System	2024-02-19T07:51:12.238Z	System	2024-02-19T07:51:12.238Z	\N
236	1	6	24a2090d-d862-4840-a417-c964a8e5b86b	6	47	2	9	System	2024-02-19T07:51:12.240Z	System	2024-02-19T07:51:12.240Z	\N
237	1	6	64e726ea-4997-4fd8-ba36-f9ab0ef94e82	6	47	2	9	System	2024-02-19T07:51:12.241Z	System	2024-02-19T07:51:12.241Z	\N
238	1	6	f605a880-13b3-4362-b0d9-eb58c88ce0ed	6	47	2	9	System	2024-02-19T07:51:12.243Z	System	2024-02-19T07:51:12.243Z	\N
239	1	6	1bc8cf3c-47c7-4490-acd0-30feadb5cd2a	6	47	2	9	System	2024-02-19T07:51:12.244Z	System	2024-02-19T07:51:12.244Z	\N
240	1	6	4dc955c6-7260-416e-8959-69ca78ceb6e4	6	47	2	9	System	2024-02-19T07:51:12.246Z	System	2024-02-19T07:51:12.246Z	\N
241	1	6	174067f3-98c2-4123-869c-24337b3c329a	6	47	2	9	System	2024-02-19T07:51:12.247Z	System	2024-02-19T07:51:12.247Z	\N
242	1	6	2774f5aa-9abf-4136-9ee4-aed1db752bd5	6	47	2	9	System	2024-02-19T07:51:12.249Z	System	2024-02-19T07:51:12.249Z	\N
243	1	6	40667e41-fd83-474d-a5ad-29fee9e39aad	6	47	2	9	System	2024-02-19T07:51:12.250Z	System	2024-02-19T07:51:12.250Z	\N
244	1	6	1490e608-8e10-489f-b01f-af6fcce6d66b	6	47	2	9	System	2024-02-19T07:51:12.251Z	System	2024-02-19T07:51:12.251Z	\N
245	1	6	43c31e0e-6f93-4884-b883-b1429e8b2ce5	6	47	2	9	System	2024-02-19T07:51:12.253Z	System	2024-02-19T07:51:12.253Z	\N
246	1	6	95a06e04-d1ef-4c86-8954-a41dd5ccb01d	6	47	2	9	System	2024-02-19T07:51:12.254Z	System	2024-02-19T07:51:12.254Z	\N
247	1	6	5b2b6f9e-f8a7-4b2b-bf33-4491f9313772	6	47	2	9	System	2024-02-19T07:51:12.256Z	System	2024-02-19T07:51:12.256Z	\N
248	1	6	47290e71-0c4b-4f23-b45b-4d4953401e54	6	47	2	9	System	2024-02-19T07:51:12.259Z	System	2024-02-19T07:51:12.259Z	\N
249	1	6	ab26495e-d7ed-48a8-8e05-fa6474b15242	6	47	2	9	System	2024-02-19T07:51:12.260Z	System	2024-02-19T07:51:12.260Z	\N
250	1	6	5300a291-41e7-4a45-9f8d-4f2e2d9595b5	6	47	2	9	System	2024-02-19T07:51:12.262Z	System	2024-02-19T07:51:12.262Z	\N
251	1	6	013c577b-b3bd-43df-b9bb-f6bcf1ad6b37	6	47	2	9	System	2024-02-19T07:51:12.263Z	System	2024-02-19T07:51:12.263Z	\N
252	1	6	8f84235e-b2d6-4643-9321-fe8255e4f056	6	47	2	9	System	2024-02-19T07:51:12.265Z	System	2024-02-19T07:51:12.265Z	\N
253	1	6	9211ef22-fae5-4ed7-a5dc-7a23a41fc6b0	6	47	2	9	System	2024-02-19T07:51:12.266Z	System	2024-02-19T07:51:12.266Z	\N
254	1	6	fe1e6d7c-929e-4237-ba61-2988616f9d03	6	47	2	9	System	2024-02-19T07:51:12.267Z	System	2024-02-19T07:51:12.267Z	\N
255	1	6	24aec100-2fd6-4c86-b870-2541112726f6	6	47	2	9	System	2024-02-19T07:51:12.269Z	System	2024-02-19T07:51:12.269Z	\N
256	1	6	e5009665-ef32-4c55-ba5b-94bdd2b4873a	6	47	2	9	System	2024-02-19T07:51:12.270Z	System	2024-02-19T07:51:12.270Z	\N
257	1	6	ddd3ca16-6daf-4300-8392-119457d4a103	6	47	2	9	System	2024-02-19T07:51:12.272Z	System	2024-02-19T07:51:12.272Z	\N
258	1	6	f58dec40-b026-4b44-9200-5bb52e50aeb9	6	47	2	9	System	2024-02-19T07:51:12.273Z	System	2024-02-19T07:51:12.273Z	\N
259	1	6	90de8665-ad3c-472d-bdf1-e63d6530946b	6	47	2	9	System	2024-02-19T07:51:12.275Z	System	2024-02-19T07:51:12.275Z	\N
260	1	6	f1c045c3-7ce0-4e92-9f24-cd60d86e3f27	6	47	2	9	System	2024-02-19T07:51:12.276Z	System	2024-02-19T07:51:12.276Z	\N
261	1	6	c03c083e-2331-4214-9ecb-41c78892a68f	6	47	2	9	System	2024-02-19T07:51:12.278Z	System	2024-02-19T07:51:12.278Z	\N
262	1	6	5ae2fedb-1fb0-4640-825c-633fd8ead472	6	47	2	9	System	2024-02-19T07:51:12.280Z	System	2024-02-19T07:51:12.280Z	\N
263	1	6	faa0fad1-94ac-49b4-9128-48e2db2e4696	6	47	2	9	System	2024-02-19T07:51:12.281Z	System	2024-02-19T07:51:12.281Z	\N
264	1	6	83d2f8d3-83eb-4fc8-b1c6-234cb0aaacda	6	47	2	9	System	2024-02-19T07:51:12.283Z	System	2024-02-19T07:51:12.283Z	\N
265	1	6	d042ae6b-9fc7-4485-b0e3-5adae4d66574	6	47	2	9	System	2024-02-19T07:51:12.285Z	System	2024-02-19T07:51:12.285Z	\N
266	1	6	1aab1537-b1e5-4894-87d7-15f20a86f426	6	47	2	9	System	2024-02-19T07:51:12.286Z	System	2024-02-19T07:51:12.286Z	\N
267	1	6	35de838f-1067-4c70-a60f-1f282d42e5f9	6	47	2	9	System	2024-02-19T07:51:12.288Z	System	2024-02-19T07:51:12.288Z	\N
268	1	6	0d2a08d9-384d-44c6-86ed-b837bf595bd0	6	47	2	9	System	2024-02-19T07:51:12.289Z	System	2024-02-19T07:51:12.289Z	\N
269	1	6	04d7c470-de8c-4dea-8c59-f5b52f674707	6	47	2	9	System	2024-02-19T07:51:12.291Z	System	2024-02-19T07:51:12.291Z	\N
270	1	6	8c957bfe-12a3-4774-8d5b-230d4bfd240b	6	47	2	9	System	2024-02-19T07:51:12.292Z	System	2024-02-19T07:51:12.292Z	\N
271	1	6	f4c87856-f264-4a04-a2f9-fb23488c5887	6	47	2	9	System	2024-02-19T07:51:12.294Z	System	2024-02-19T07:51:12.294Z	\N
272	1	6	a03e57e8-fd93-485b-a5c9-310dd19ee27b	6	47	2	9	System	2024-02-19T07:51:12.295Z	System	2024-02-19T07:51:12.295Z	\N
273	1	6	84a80ae8-f091-48c9-a1db-8e945bf8396f	6	47	2	9	System	2024-02-19T07:51:12.296Z	System	2024-02-19T07:51:12.296Z	\N
274	1	6	35d68aa5-f081-464a-b14a-5fc6926eb01e	6	47	2	9	System	2024-02-19T07:51:12.298Z	System	2024-02-19T07:51:12.298Z	\N
275	1	6	60afd021-6075-4ff3-a541-475953c106bf	6	47	2	9	System	2024-02-19T07:51:12.299Z	System	2024-02-19T07:51:12.299Z	\N
276	1	6	08ec6441-6963-465c-8b52-6679385b2868	6	47	2	9	System	2024-02-19T07:51:12.301Z	System	2024-02-19T07:51:12.301Z	\N
277	1	6	c2f3833a-5a44-4d62-b8e3-3bf938f869ca	6	47	2	9	System	2024-02-19T07:51:12.302Z	System	2024-02-19T07:51:12.302Z	\N
278	1	6	f2611352-96cb-449f-9cdd-0b0acd758aed	6	47	2	9	System	2024-02-19T07:51:12.304Z	System	2024-02-19T07:51:12.304Z	\N
279	1	6	dc0cb958-1374-42c2-a596-f95bf2c15cb3	6	47	2	9	System	2024-02-19T07:51:12.305Z	System	2024-02-19T07:51:12.305Z	\N
280	1	6	321e33b1-b256-4235-9812-19d4d7a2c3bc	6	47	2	9	System	2024-02-19T07:51:12.307Z	System	2024-02-19T07:51:12.307Z	\N
281	1	6	6b8767ce-e399-48e0-b4bb-1d3786fe6854	6	47	2	9	System	2024-02-19T07:51:12.308Z	System	2024-02-19T07:51:12.308Z	\N
282	1	6	5e06a416-6f6a-41cc-966d-08f9eacd5b64	6	47	2	9	System	2024-02-19T07:51:12.310Z	System	2024-02-19T07:51:12.310Z	\N
283	1	6	fb9c8a12-1270-4469-bddc-09f7ed3d6086	6	47	2	9	System	2024-02-19T07:51:12.311Z	System	2024-02-19T07:51:12.311Z	\N
284	1	6	094d2e96-5c1d-4938-8d1a-d7204525a01e	6	47	2	9	System	2024-02-19T07:51:12.312Z	System	2024-02-19T07:51:12.312Z	\N
285	1	6	06129358-22be-447a-a4b9-eb4837d54893	6	47	2	9	System	2024-02-19T07:51:12.313Z	System	2024-02-19T07:51:12.313Z	\N
286	1	6	8013e077-a5b8-4122-994c-3201625b71c9	6	47	2	9	System	2024-02-19T07:51:12.315Z	System	2024-02-19T07:51:12.315Z	\N
287	1	6	78a95cc6-3256-4204-b5bf-588f567ff5d4	6	47	2	9	System	2024-02-19T07:51:12.316Z	System	2024-02-19T07:51:12.316Z	\N
288	1	6	6fd0bf46-de67-4c19-9235-d6d52883e316	6	47	2	9	System	2024-02-19T07:51:12.318Z	System	2024-02-19T07:51:12.318Z	\N
289	1	6	77010569-5843-41c2-b74d-552943b5eccb	6	47	2	9	System	2024-02-19T07:51:12.319Z	System	2024-02-19T07:51:12.319Z	\N
290	1	6	895763ca-f9aa-4ecd-ae6d-660a1899a5f3	6	47	2	9	System	2024-02-19T07:51:12.320Z	System	2024-02-19T07:51:12.320Z	\N
291	1	6	394be180-9332-46b4-b5a2-53a9c0a962c2	6	47	2	9	System	2024-02-19T07:51:12.321Z	System	2024-02-19T07:51:12.321Z	\N
292	1	6	e1119073-2a0c-4269-a862-250bacfcca5a	6	47	2	9	System	2024-02-19T07:51:12.322Z	System	2024-02-19T07:51:12.322Z	\N
293	1	6	a64a026f-f580-4264-b6d7-d29961a5767b	6	47	2	9	System	2024-02-19T07:51:12.323Z	System	2024-02-19T07:51:12.323Z	\N
294	1	6	73512d65-3b42-43a1-8f07-b6d727291e60	6	47	2	9	System	2024-02-19T07:51:12.324Z	System	2024-02-19T07:51:12.324Z	\N
295	1	6	792ae42c-d631-4322-9f0f-baaba2f45468	6	47	2	9	System	2024-02-19T07:51:12.326Z	System	2024-02-19T07:51:12.326Z	\N
296	1	6	2a5729a5-2253-4055-ad16-9f10118cc3ea	6	47	2	9	System	2024-02-19T07:51:12.327Z	System	2024-02-19T07:51:12.327Z	\N
297	1	6	171cb931-da5a-4f85-a927-8a2800a11ad4	6	47	2	9	System	2024-02-19T07:51:12.328Z	System	2024-02-19T07:51:12.328Z	\N
298	1	6	895fe0ae-b959-4894-99c8-221de9e18410	6	47	2	9	System	2024-02-19T07:51:12.329Z	System	2024-02-19T07:51:12.329Z	\N
299	1	6	5225bd5e-115c-4eb6-8b00-53e1bc15b75d	6	47	2	9	System	2024-02-19T07:51:12.331Z	System	2024-02-19T07:51:12.331Z	\N
300	1	6	0bdcc373-b3e3-43a1-8ba4-14dc8edc3926	6	47	2	9	System	2024-02-19T07:51:12.332Z	System	2024-02-19T07:51:12.332Z	\N
301	1	6	b62ced3a-c2cd-4ed1-848d-c7289563b912	6	47	2	9	System	2024-02-19T07:51:12.333Z	System	2024-02-19T07:51:12.333Z	\N
302	1	6	c3f6a085-1511-4ba7-977a-a4ef01f2d8be	6	47	2	9	System	2024-02-19T07:51:12.335Z	System	2024-02-19T07:51:12.335Z	\N
303	1	6	22cddf6b-3825-4efb-9ce4-ce2ab0d38a9c	6	47	2	9	System	2024-02-19T07:51:12.336Z	System	2024-02-19T07:51:12.336Z	\N
304	1	6	09dd5753-4f2c-4d61-870e-8408d48cad6b	6	47	2	9	System	2024-02-19T07:51:12.338Z	System	2024-02-19T07:51:12.338Z	\N
305	1	6	3b1dd2fd-02d2-4a29-be32-90f707b5b445	6	47	2	9	System	2024-02-19T07:51:12.339Z	System	2024-02-19T07:51:12.339Z	\N
306	1	6	1c7bfab0-f5cc-47ea-a628-140450f60c69	6	47	2	9	System	2024-02-19T07:51:12.341Z	System	2024-02-19T07:51:12.341Z	\N
307	1	6	1ca538b7-a35c-4d9b-9fa1-6635a79e0433	6	47	2	9	System	2024-02-19T07:51:12.342Z	System	2024-02-19T07:51:12.342Z	\N
308	1	6	7cf01477-b157-4ad0-baca-eb6e0ee36335	6	47	2	9	System	2024-02-19T07:51:12.343Z	System	2024-02-19T07:51:12.343Z	\N
309	1	6	65a15a34-8b73-463f-a71a-294dcbc7d57d	6	47	2	9	System	2024-02-19T07:51:12.345Z	System	2024-02-19T07:51:12.345Z	\N
310	1	6	fe035ff3-35c4-4755-92af-4b95a811416b	6	47	2	9	System	2024-02-19T07:51:12.346Z	System	2024-02-19T07:51:12.346Z	\N
311	1	6	8f7a3dda-2fe1-4c14-9998-9f90bcd5dd9e	6	47	2	9	System	2024-02-19T07:51:12.347Z	System	2024-02-19T07:51:12.347Z	\N
312	1	6	6fcaf807-f1a1-4876-b14a-fcbc7828075c	6	47	2	9	System	2024-02-19T07:51:12.350Z	System	2024-02-19T07:51:12.350Z	\N
313	1	6	b38adc29-6653-46b3-aff4-fb37e97a7df4	6	47	2	9	System	2024-02-19T07:51:12.352Z	System	2024-02-19T07:51:12.352Z	\N
314	1	6	1afaec00-6806-4794-a2f3-e21fd631ca5c	6	47	2	9	System	2024-02-19T07:51:12.353Z	System	2024-02-19T07:51:12.353Z	\N
315	1	6	152cb379-c215-434b-b5f0-273632df61d0	6	47	2	9	System	2024-02-19T07:51:12.354Z	System	2024-02-19T07:51:12.354Z	\N
316	1	6	ab0e44bf-dea5-41f0-9897-c9a5e262b07f	6	47	2	9	System	2024-02-19T07:51:12.356Z	System	2024-02-19T07:51:12.356Z	\N
317	1	6	7e8b92bb-70a7-4791-b34b-95e09f02cf63	6	47	2	9	System	2024-02-19T07:51:12.357Z	System	2024-02-19T07:51:12.357Z	\N
318	1	6	ad7f7aff-d94f-43bb-a522-56a707e65e4f	6	47	2	9	System	2024-02-19T07:51:12.358Z	System	2024-02-19T07:51:12.358Z	\N
319	1	6	d5d3ce48-5431-4af8-b525-19e07a00c2ad	6	47	2	9	System	2024-02-19T07:51:12.360Z	System	2024-02-19T07:51:12.360Z	\N
320	1	6	03f8bb1e-8ad3-429d-826c-389d84d9f4be	6	47	2	9	System	2024-02-19T07:51:12.361Z	System	2024-02-19T07:51:12.361Z	\N
321	1	6	b281932e-c817-4e29-bef3-8aad9e1a1a78	6	47	2	9	System	2024-02-19T07:51:12.362Z	System	2024-02-19T07:51:12.362Z	\N
322	1	6	b384d69f-b5bd-45eb-a6bb-202f73b8c903	6	47	2	9	System	2024-02-19T07:51:12.364Z	System	2024-02-19T07:51:12.364Z	\N
323	1	6	383066e4-f76e-4d8f-95ea-fe9c7da4cac0	6	47	2	9	System	2024-02-19T07:51:12.365Z	System	2024-02-19T07:51:12.365Z	\N
324	1	6	b6491b1a-6f6c-4bab-abeb-c4b1b3361d70	6	47	2	9	System	2024-02-19T07:51:12.367Z	System	2024-02-19T07:51:12.367Z	\N
325	1	6	9ec0b996-6ebe-4d75-9861-3f2217781215	6	47	2	9	System	2024-02-19T07:51:12.368Z	System	2024-02-19T07:51:12.368Z	\N
326	1	6	8185c499-2bea-482c-a4fc-d93bf817ffc3	6	47	2	9	System	2024-02-19T07:51:12.369Z	System	2024-02-19T07:51:12.369Z	\N
327	1	6	fb060810-a4aa-452e-be34-c4937765ab92	6	47	2	9	System	2024-02-19T07:51:12.371Z	System	2024-02-19T07:51:12.371Z	\N
328	1	6	5acb6ff9-8b6c-4ec0-83dd-f6272b01a161	6	47	2	9	System	2024-02-19T07:51:12.372Z	System	2024-02-19T07:51:12.372Z	\N
329	1	6	25e93e8d-feeb-4575-bafe-67b1536885e5	6	47	2	9	System	2024-02-19T07:51:12.373Z	System	2024-02-19T07:51:12.373Z	\N
330	1	6	a423377c-34c6-433e-b2da-bd55f83673b0	6	47	2	9	System	2024-02-19T07:51:12.374Z	System	2024-02-19T07:51:12.374Z	\N
331	1	6	2ea900e6-0f37-4fee-85d9-b62c52c95589	6	47	2	9	System	2024-02-19T07:51:12.376Z	System	2024-02-19T07:51:12.376Z	\N
332	1	6	2d491da2-04c0-4b06-bbcb-4fe810d36773	6	47	2	9	System	2024-02-19T07:51:12.378Z	System	2024-02-19T07:51:12.378Z	\N
333	1	6	c39ce6b4-4783-4618-8c13-8ae489baf339	6	47	2	9	System	2024-02-19T07:51:12.379Z	System	2024-02-19T07:51:12.379Z	\N
334	1	6	4cf78ab8-770e-46bb-bfb7-7fd108f6d3cf	6	47	2	9	System	2024-02-19T07:51:12.380Z	System	2024-02-19T07:51:12.380Z	\N
335	1	6	ae03e9f4-f476-467c-90bf-ceff31d03241	6	47	2	9	System	2024-02-19T07:51:12.381Z	System	2024-02-19T07:51:12.381Z	\N
336	1	6	80c236f4-65c6-4c39-bcf3-d83db5ba32cc	6	47	2	9	System	2024-02-19T07:51:12.383Z	System	2024-02-19T07:51:12.383Z	\N
337	1	6	8a048658-3440-4fb1-a2ca-98308a45911e	6	47	2	9	System	2024-02-19T07:51:12.384Z	System	2024-02-19T07:51:12.384Z	\N
338	1	6	0365e461-5699-4ae7-87f2-fcba3bd01280	6	47	2	9	System	2024-02-19T07:51:12.385Z	System	2024-02-19T07:51:12.385Z	\N
339	1	6	3b491ee4-f40f-4341-8cc8-d84803b7174d	6	47	2	9	System	2024-02-19T07:51:12.386Z	System	2024-02-19T07:51:12.386Z	\N
340	1	6	d29a2553-3dcc-427c-ad6e-de41b8f6b5ff	6	47	2	9	System	2024-02-19T07:51:12.387Z	System	2024-02-19T07:51:12.387Z	\N
341	1	6	d8aef21e-d1d7-4362-917e-aa529e4b1a17	6	47	2	9	System	2024-02-19T07:51:12.389Z	System	2024-02-19T07:51:12.389Z	\N
342	1	6	206c4aed-9d6e-4867-a587-bf3ca5b259cc	6	47	2	9	System	2024-02-19T07:51:12.390Z	System	2024-02-19T07:51:12.390Z	\N
343	1	6	efb88db0-084f-4ced-a275-f3f58e889d1e	6	47	2	9	System	2024-02-19T07:51:12.391Z	System	2024-02-19T07:51:12.391Z	\N
344	1	6	d9baaad3-2ec1-4ce3-8995-a242a224e4c9	6	47	2	9	System	2024-02-19T07:51:12.393Z	System	2024-02-19T07:51:12.393Z	\N
345	1	6	9d9adfa4-2915-4ac9-bf6e-75aea8c0a4e9	6	47	2	9	System	2024-02-19T07:51:12.394Z	System	2024-02-19T07:51:12.394Z	\N
346	1	6	a662e9e7-f915-45d4-9ae1-ce91eab07492	6	47	2	9	System	2024-02-19T07:51:12.395Z	System	2024-02-19T07:51:12.395Z	\N
347	1	6	8c28e76d-55ba-41bf-8936-c04319c597f4	6	47	2	9	System	2024-02-19T07:51:12.398Z	System	2024-02-19T07:51:12.398Z	\N
348	1	6	69ea414c-d8ea-4736-a669-052f66819f2e	6	47	2	9	System	2024-02-19T07:51:12.399Z	System	2024-02-19T07:51:12.399Z	\N
349	1	6	6a5a414d-6802-44e1-bdd7-f629fb2daa26	6	47	2	9	System	2024-02-19T07:51:12.400Z	System	2024-02-19T07:51:12.400Z	\N
350	1	6	3e073dd7-649e-465b-91ac-dc0682cb9c0b	6	47	2	9	System	2024-02-19T07:51:12.402Z	System	2024-02-19T07:51:12.402Z	\N
351	1	6	b9639b51-b2ff-47cf-ad60-f3e2c1376ea9	6	47	2	9	System	2024-02-19T07:51:12.403Z	System	2024-02-19T07:51:12.403Z	\N
352	1	6	873a5e82-2898-4037-919e-c761bf0a482e	6	47	2	9	System	2024-02-19T07:51:12.404Z	System	2024-02-19T07:51:12.404Z	\N
353	1	6	9624508e-83a5-40ee-a896-a2042b2db1db	6	47	2	9	System	2024-02-19T07:51:12.405Z	System	2024-02-19T07:51:12.405Z	\N
354	1	6	703c93a9-4b66-40bb-bfe8-77a8a39f78bd	6	47	2	9	System	2024-02-19T07:51:12.406Z	System	2024-02-19T07:51:12.406Z	\N
355	1	6	5f5b52c8-c2fc-4dd5-970a-5284c304cffb	6	47	2	9	System	2024-02-19T07:51:12.408Z	System	2024-02-19T07:51:12.408Z	\N
356	1	6	6dd6d0c2-7b67-4594-8d1f-0efffec4df5a	6	47	2	9	System	2024-02-19T07:51:12.409Z	System	2024-02-19T07:51:12.409Z	\N
357	1	6	e5240388-16c3-4d05-9b66-6904f77d9fb8	6	47	2	9	System	2024-02-19T07:51:12.410Z	System	2024-02-19T07:51:12.410Z	\N
358	1	6	28916cc4-cc50-4b43-8255-621e311a5d83	6	47	2	9	System	2024-02-19T07:51:12.412Z	System	2024-02-19T07:51:12.412Z	\N
359	1	6	9f625ea6-e1da-45e3-976a-bebb79a72804	6	47	2	9	System	2024-02-19T07:51:12.413Z	System	2024-02-19T07:51:12.413Z	\N
360	1	6	39d6d055-e83d-4f89-a177-b7e1d813464c	6	47	2	9	System	2024-02-19T07:51:12.415Z	System	2024-02-19T07:51:12.415Z	\N
361	1	6	ab740942-cf58-482c-99c8-0a0a1a94c745	6	47	2	9	System	2024-02-19T07:51:12.416Z	System	2024-02-19T07:51:12.416Z	\N
362	1	6	d80d5e2f-2527-48a9-b84d-19db3d9cf5aa	6	47	2	9	System	2024-02-19T07:51:12.418Z	System	2024-02-19T07:51:12.418Z	\N
363	1	6	c678d2c0-07a4-4338-aa1e-9788ee23b5fe	6	47	2	9	System	2024-02-19T07:51:12.419Z	System	2024-02-19T07:51:12.419Z	\N
364	1	6	d3c22ec9-07d7-4b79-acc4-06eac91b23d8	6	47	2	9	System	2024-02-19T07:51:12.420Z	System	2024-02-19T07:51:12.420Z	\N
365	1	6	17db91a3-c9c5-4b5f-ade2-ffe698cc8d34	6	47	2	9	System	2024-02-19T07:51:12.421Z	System	2024-02-19T07:51:12.421Z	\N
366	1	6	8e839f2b-5823-419a-b3e6-91bc7001e2ad	6	47	2	9	System	2024-02-19T07:51:12.423Z	System	2024-02-19T07:51:12.423Z	\N
367	1	6	83eaaf12-7918-45a9-8e42-7e63c28e3704	6	47	2	9	System	2024-02-19T07:51:12.424Z	System	2024-02-19T07:51:12.424Z	\N
368	1	6	ea06cccf-71ac-4712-bc78-ea7ba2ca1be8	6	47	2	9	System	2024-02-19T07:51:12.426Z	System	2024-02-19T07:51:12.426Z	\N
369	2	6	5a067ae1-2d2a-4687-aeeb-8684a43ba144	6	47	2	32	System	2024-02-19T07:51:12.428Z	System	2024-02-19T07:51:12.428Z	\N
370	2	6	a2d8964d-1b84-4ec4-8a02-800448326ad5	6	50	2	34	\N	\N	\N	\N	\N
371	1	6	f38ff132-3784-4990-ab45-5ea827c1b546	6	24	2	13	System	2024-02-19T07:51:12.446Z	System	2024-02-19T07:51:12.446Z	\N
372	1	6	28210dc1-3c81-4fbb-8429-1131ec3883c8	6	24	2	13	System	2024-02-19T07:51:12.448Z	System	2024-02-19T07:51:12.448Z	\N
373	1	6	f06c3f77-0b50-46ee-8b73-26124d9ce750	6	24	2	13	System	2024-02-19T07:51:12.450Z	System	2024-02-19T07:51:12.450Z	\N
374	1	6	ae91e521-86ea-400e-b167-c53af70068cd	6	24	2	13	System	2024-02-19T07:51:12.451Z	System	2024-02-19T07:51:12.451Z	\N
375	1	6	9c523af5-1776-4916-b477-5aaa7c6d489a	6	24	2	13	System	2024-02-19T07:51:12.453Z	System	2024-02-19T07:51:12.453Z	\N
376	1	6	10433a69-f6c2-4efc-b82b-0110f7936954	6	24	2	13	System	2024-02-19T07:51:12.455Z	System	2024-02-19T07:51:12.455Z	\N
888	1	6	6ef523d7-460c-4fe4-9325-dfc6db26a7f4	35	141	2	11	admin	2024-02-19T08:09:32.467Z	admin	2024-02-19T08:09:32.467Z	\N
377	3	6	e4698376-f443-4eef-aa3b-e127be1d168b	6	51	2	13	System	2024-02-19T07:51:12.457Z	System	2024-02-19T07:51:12.457Z	\N
378	1	6	7e397762-bfc7-445d-bfb9-3c121821f36a	6	24	2	13	System	2024-02-19T07:51:12.465Z	System	2024-02-19T07:51:12.465Z	\N
379	1	6	521bb3da-bfe5-4cb5-bc1b-adf46671ad3a	6	24	2	13	System	2024-02-19T07:51:12.467Z	System	2024-02-19T07:51:12.467Z	\N
380	1	6	c90bde40-65da-4620-a6a1-f3ceace5be2a	6	24	2	13	System	2024-02-19T07:51:12.468Z	System	2024-02-19T07:51:12.468Z	\N
381	1	6	54f2fabd-878d-4c35-87bb-a7cd3288e621	6	24	2	13	System	2024-02-19T07:51:12.470Z	System	2024-02-19T07:51:12.470Z	\N
876	4	6	dad275aa-affc-487d-a7ed-92cf8e6ce351	39	24	4	11	admin	2024-02-19T07:55:23.195Z	admin	2024-02-19T08:09:40.888Z	\N
382	2	6	54cafcdd-978c-4ecb-858f-cdaba94d6d8a	6	51	2	13	System	2024-02-19T07:51:12.475Z	System	2024-02-19T07:51:12.475Z	\N
383	2	6	46632cfb-5684-4c78-8361-c9fb1302cc7e	6	51	2	13	System	2024-02-19T07:51:12.482Z	System	2024-02-19T07:51:12.482Z	\N
384	2	6	678db05e-893e-4793-b208-68fdf05158cb	6	51	2	13	System	2024-02-19T07:51:12.488Z	System	2024-02-19T07:51:12.488Z	\N
385	2	6	72dfac80-5ff2-4396-9639-f3cff0f1e6e7	6	51	2	13	System	2024-02-19T07:51:12.493Z	System	2024-02-19T07:51:12.493Z	\N
386	2	6	fed7c911-aaed-4f79-a25c-b66c6cd0cb71	6	51	2	13	System	2024-02-19T07:51:12.499Z	System	2024-02-19T07:51:12.499Z	\N
387	2	6	c4830c84-bf49-4902-ac7e-4f3c574d6705	6	51	2	13	System	2024-02-19T07:51:12.504Z	System	2024-02-19T07:51:12.504Z	\N
388	2	6	df8abab4-7cc6-4324-8106-d7c670f4165a	6	51	2	13	System	2024-02-19T07:51:12.509Z	System	2024-02-19T07:51:12.509Z	\N
389	2	6	ee6a0ed0-dce0-47e7-8c53-5da3d6587b8a	6	51	2	13	System	2024-02-19T07:51:12.515Z	System	2024-02-19T07:51:12.515Z	\N
390	2	6	763c4945-0033-44f9-b279-7f9f477ccc84	6	51	2	13	System	2024-02-19T07:51:12.520Z	System	2024-02-19T07:51:12.520Z	\N
391	2	6	ea0fb790-e932-43bb-9a62-2dcebf351854	6	51	2	13	System	2024-02-19T07:51:12.526Z	System	2024-02-19T07:51:12.526Z	\N
392	2	6	e8e9ac89-2a54-4abb-89fa-b69bf364614e	6	51	2	13	System	2024-02-19T07:51:12.534Z	System	2024-02-19T07:51:12.534Z	\N
393	1	6	392202ef-4b7a-44a0-a076-7d12456e131b	6	24	2	13	System	2024-02-19T07:51:12.554Z	System	2024-02-19T07:51:12.554Z	\N
877	1	6	94e0b276-6447-4dbc-b32a-1d37836a8066	24	47	4	9	admin	2024-02-19T07:56:50.034Z	admin	2024-02-19T07:56:50.034Z	\N
394	2	6	ac2e9aad-880d-44f3-9ec1-9e5ca54f5bd3	6	51	2	13	System	2024-02-19T07:51:12.557Z	System	2024-02-19T07:51:12.557Z	\N
878	1	6	11dedf84-4ebb-431e-adbf-7e92b2792674	25	47	4	9	admin	2024-02-19T07:57:10.868Z	admin	2024-02-19T07:57:10.868Z	\N
879	1	6	fa6b38cd-442a-4f77-9d3e-dc212a6b809e	26	47	4	9	admin	2024-02-19T07:57:44.072Z	admin	2024-02-19T07:57:44.072Z	\N
396	2	6	b4fa9a99-d38e-4c90-bae9-97cf6b14fd5f	6	51	2	13	System	2024-02-19T07:51:12.564Z	System	2024-02-19T07:51:12.564Z	\N
397	1	6	75ce687c-0316-4f51-881c-ad69f5506d6b	6	24	2	13	System	2024-02-19T07:51:12.571Z	System	2024-02-19T07:51:12.571Z	\N
398	2	6	9f78408d-a805-4cf4-b798-929ca90a777d	6	51	2	13	System	2024-02-19T07:51:12.573Z	System	2024-02-19T07:51:12.573Z	\N
399	2	6	1aaf68d4-1b7b-45a3-aada-9532deadaaea	6	51	2	13	System	2024-02-19T07:51:12.578Z	System	2024-02-19T07:51:12.578Z	\N
883	1	6	9f8b106a-6755-42ee-80b0-b7db55cd664a	29	148	1	\N	\N	\N	\N	\N	\N
400	2	6	7e91521a-dbf1-457e-880c-50729e80e7a4	6	51	2	13	System	2024-02-19T07:51:12.582Z	System	2024-02-19T07:51:12.582Z	\N
882	2	6	6fae2210-0054-4926-a8ea-9dd274bc4efb	29	141	4	11	admin	2024-02-19T08:00:29.285Z	admin	2024-02-19T08:00:29.285Z	\N
401	2	6	67e8716a-8182-4778-a81b-20cfae22cb60	6	51	2	13	System	2024-02-19T07:51:12.587Z	System	2024-02-19T07:51:12.587Z	\N
895	1	6	7332ee25-d659-4a5d-872b-2b52bedc6ffe	43	141	2	11	admin	2024-02-19T08:09:41.340Z	admin	2024-02-19T08:09:41.340Z	\N
402	2	6	6ddec323-e4ad-45e2-a057-b00e6b6c35fe	6	51	2	13	System	2024-02-19T07:51:12.591Z	System	2024-02-19T07:51:12.591Z	\N
403	2	6	26108a3e-8a4f-4eda-8e76-1c634629c848	6	51	2	13	System	2024-02-19T07:51:12.596Z	System	2024-02-19T07:51:12.596Z	\N
404	2	6	781fe011-1eb8-4157-bd3e-d44fe5f80a38	6	51	2	13	System	2024-02-19T07:51:12.600Z	System	2024-02-19T07:51:12.600Z	\N
405	1	6	693862e3-56b0-41ae-8dae-4c32fbf8b863	6	24	2	13	System	2024-02-19T07:51:12.606Z	System	2024-02-19T07:51:12.606Z	\N
406	4	6	f4686196-d2da-48d0-9965-dd45ac84ea10	6	51	2	13	System	2024-02-19T07:51:12.607Z	System	2024-02-19T07:51:12.607Z	\N
407	4	6	c2ebbe14-5d81-48a5-adaf-425240d7f019	6	51	2	13	System	2024-02-19T07:51:12.613Z	System	2024-02-19T07:51:12.613Z	\N
408	4	6	66080df8-50d0-48dc-89c6-71c692943f6b	6	51	2	13	System	2024-02-19T07:51:12.618Z	System	2024-02-19T07:51:12.618Z	\N
409	4	6	e636eafd-b138-4f01-a674-2e2716cda9e2	6	51	2	13	System	2024-02-19T07:51:12.623Z	System	2024-02-19T07:51:12.623Z	\N
410	4	6	64ee9bf8-423b-41ef-ac8d-a976d628970a	6	51	2	13	System	2024-02-19T07:51:12.627Z	System	2024-02-19T07:51:12.627Z	\N
411	4	6	151911da-7588-4572-a357-38ccce92f87a	6	51	2	13	System	2024-02-19T07:51:12.632Z	System	2024-02-19T07:51:12.632Z	\N
412	4	6	f697f24c-082f-4d19-bbeb-6ca26df76f21	6	51	2	13	System	2024-02-19T07:51:12.636Z	System	2024-02-19T07:51:12.636Z	\N
413	1	6	faa1af9f-013a-4d5d-a2ba-72d8c4b81caa	6	24	2	13	System	2024-02-19T07:51:12.642Z	System	2024-02-19T07:51:12.642Z	\N
414	4	6	wf-email-html-ftl	6	51	2	13	System	2024-02-19T07:51:12.645Z	System	2024-02-19T07:51:12.645Z	\N
890	1	4	460127f7-23e2-4705-b3b4-ce0300a70add	38	164	4	60	admin	2024-02-19T08:09:40.835Z	admin	2024-02-19T08:09:40.835Z	\N
891	1	4	b6941c17-49f7-4c07-bd67-0b0920cab7f1	38	51	4	60	admin	2024-02-19T08:09:40.839Z	admin	2024-02-19T08:09:40.839Z	\N
415	4	6	981d7b54-15e4-4145-8653-0a03db8c3b86	6	51	2	13	System	2024-02-19T07:51:12.649Z	System	2024-02-19T07:51:12.649Z	\N
416	4	6	070602e0-5e12-452a-8563-efd3c176c016	6	51	2	13	System	2024-02-19T07:51:12.655Z	System	2024-02-19T07:51:12.655Z	\N
417	4	6	0820b627-20f1-4000-8ee1-71ce006dbcac	6	51	2	13	System	2024-02-19T07:51:12.662Z	System	2024-02-19T07:51:12.662Z	\N
880	8	6	44545a62-0f64-4d3e-838a-9f8ba23df0c7	51	51	4	11	admin	2024-02-19T08:00:28.040Z	admin	2024-02-19T09:31:46.154Z	\N
418	4	6	2cae564b-005c-4e2e-9f8c-2312cd09d07f	6	51	2	13	System	2024-02-19T07:51:12.667Z	System	2024-02-19T07:51:12.667Z	\N
889	12	6	f9d6264e-426b-41cd-9f4b-b660dc582311	53	51	4	11	admin	2024-02-19T08:09:40.810Z	admin	2024-02-19T09:32:05.282Z	\N
419	4	6	077d175e-8ae1-46cd-97b2-011dd7097d16	6	51	2	13	System	2024-02-19T07:51:12.672Z	System	2024-02-19T07:51:12.672Z	\N
420	4	6	9b6cb045-da19-41a5-91f1-07d0d3e11baa	6	51	2	13	System	2024-02-19T07:51:12.676Z	System	2024-02-19T07:51:12.676Z	\N
421	2	6	514af9a1-67a3-49e9-b404-082ef4e1aba8	6	51	2	15	System	2024-02-19T07:51:12.683Z	System	2024-02-19T07:51:12.683Z	\N
422	2	6	9cbf9627-bea9-4f2e-b90d-9fa2a09d03b7	6	51	2	13	System	2024-02-19T07:51:12.692Z	System	2024-02-19T07:51:12.692Z	\N
423	2	6	d36d925d-f524-46f3-8a36-af7e8b434dc3	6	51	2	13	System	2024-02-19T07:51:12.697Z	System	2024-02-19T07:51:12.697Z	\N
424	2	6	fd9c5f74-1391-4c16-bd8c-55f56338e3d6	6	51	2	13	System	2024-02-19T07:51:12.701Z	System	2024-02-19T07:51:12.701Z	\N
425	2	6	c933b227-5c38-42bd-a2a9-f611b2e86a14	6	51	2	13	System	2024-02-19T07:51:12.706Z	System	2024-02-19T07:51:12.706Z	\N
426	2	6	e42dc92b-4c4d-4307-ab22-18b0e46f4933	6	51	2	13	System	2024-02-19T07:51:12.710Z	System	2024-02-19T07:51:12.710Z	\N
427	2	6	ce9e61cb-2355-47c8-a88b-01f64463de35	6	51	2	13	System	2024-02-19T07:51:12.714Z	System	2024-02-19T07:51:12.714Z	\N
429	2	6	9ebce2be-0813-4f84-a3da-a9435e1ebfe9	6	51	2	13	System	2024-02-19T07:51:12.723Z	System	2024-02-19T07:51:12.723Z	\N
430	2	6	9f7a8327-46ac-47fd-96d8-5a16bd4e94af	6	51	2	13	System	2024-02-19T07:51:12.728Z	System	2024-02-19T07:51:12.728Z	\N
885	12	6	02acf462-533d-4e1b-9825-05fa934140da	52	51	4	11	admin	2024-02-19T08:09:32.182Z	admin	2024-02-19T09:31:54.728Z	\N
431	2	6	9d84d7a0-0ce7-48bc-9b7d-ec7d550ea9cb	6	51	2	13	System	2024-02-19T07:51:12.732Z	System	2024-02-19T07:51:12.732Z	\N
432	2	6	fc4116a5-70e2-4276-8bb5-8be387731ac4	6	51	2	13	System	2024-02-19T07:51:12.736Z	System	2024-02-19T07:51:12.736Z	\N
433	1	6	e19dbdac-65f7-4f20-b779-6870662f0f79	6	24	2	13	System	2024-02-19T07:51:12.739Z	System	2024-02-19T07:51:12.739Z	\N
434	1	6	1fbe671f-8ef5-47bc-9ab0-5d5c2e27db84	6	24	2	13	System	2024-02-19T07:51:12.742Z	System	2024-02-19T07:51:12.742Z	\N
435	1	6	2221c277-7e95-4d45-879e-043c49e9d078	6	24	2	13	System	2024-02-19T07:51:12.743Z	System	2024-02-19T07:51:12.743Z	\N
436	2	6	4bf7a241-ede6-4fc1-a857-f4ed52b6818a	6	51	2	13	System	2024-02-19T07:51:12.746Z	System	2024-02-19T07:51:12.746Z	\N
437	2	6	66462100-5fbb-45ac-b2d6-659c67294909	6	51	2	13	System	2024-02-19T07:51:12.750Z	System	2024-02-19T07:51:12.750Z	\N
438	2	6	432c36c2-0b77-47b8-ba44-9e08ac96be5c	6	51	2	13	System	2024-02-19T07:51:12.755Z	System	2024-02-19T07:51:12.755Z	\N
439	2	6	f0dc11e2-ae10-47da-9a6c-2793dfd8661f	6	51	2	13	System	2024-02-19T07:51:12.759Z	System	2024-02-19T07:51:12.759Z	\N
440	2	6	09b8576b-77fe-4069-8703-3ffeefb0c681	6	51	2	13	System	2024-02-19T07:51:12.764Z	System	2024-02-19T07:51:12.764Z	\N
441	2	6	68eecbb9-6af8-4f60-af52-6cadf94063c5	6	51	2	13	System	2024-02-19T07:51:12.768Z	System	2024-02-19T07:51:12.768Z	\N
442	2	6	407b8438-56f9-4854-b4ec-889773a5bc41	6	51	2	13	System	2024-02-19T07:51:12.774Z	System	2024-02-19T07:51:12.774Z	\N
443	2	6	2a3952f8-b85d-4e82-ab68-04642096dc72	6	51	2	13	System	2024-02-19T07:51:12.778Z	System	2024-02-19T07:51:12.778Z	\N
444	2	6	607981d3-0e0b-4692-81a8-17f58c1dff51	6	51	2	13	System	2024-02-19T07:51:12.783Z	System	2024-02-19T07:51:12.783Z	\N
445	2	6	833d8ef5-183d-4e82-a632-1512df92d8e9	6	51	2	13	System	2024-02-19T07:51:12.786Z	System	2024-02-19T07:51:12.786Z	\N
446	2	6	e586b18d-5c72-421f-8772-a5ee913eff9e	6	51	2	13	System	2024-02-19T07:51:12.790Z	System	2024-02-19T07:51:12.790Z	\N
447	2	6	6cbba862-1ef2-4b49-b019-4af68e7db684	6	51	2	13	System	2024-02-19T07:51:12.794Z	System	2024-02-19T07:51:12.794Z	\N
448	2	6	c9c82c4a-557f-4550-a67c-691b23e36d9b	6	51	2	13	System	2024-02-19T07:51:12.799Z	System	2024-02-19T07:51:12.799Z	\N
893	1	4	717cf9b6-c811-4fb5-a38a-585fb560f613	40	164	4	60	admin	2024-02-19T08:09:40.948Z	admin	2024-02-19T08:09:40.948Z	\N
449	2	6	327a8579-30a0-4ec4-ba29-13de28387ad8	6	51	2	13	System	2024-02-19T07:51:12.803Z	System	2024-02-19T07:51:12.803Z	\N
894	1	4	5dfa6d2b-8693-45e1-b616-059fcc577820	40	51	4	60	admin	2024-02-19T08:09:40.958Z	admin	2024-02-19T08:09:40.958Z	\N
450	2	6	c13f0b5e-9e78-4449-92ce-8ace0bf89e7e	6	51	2	13	System	2024-02-19T07:51:12.806Z	System	2024-02-19T07:51:12.806Z	\N
451	2	6	5f08523c-4de5-4f9d-b19e-f217f3feedde	6	51	2	13	System	2024-02-19T07:51:12.811Z	System	2024-02-19T07:51:12.811Z	\N
453	2	6	536ec208-74fd-4716-a8b4-fc2bbf3a0de6	6	51	2	13	System	2024-02-19T07:51:12.819Z	System	2024-02-19T07:51:12.819Z	\N
428	2	6	f477b751-7409-4004-8a90-d46924fcd13e	6	24	2	13	System	2024-02-19T07:51:12.720Z	System	2024-02-19T07:51:12.720Z	\N
452	2	6	26bdb6fa-b59d-4447-9208-3c63a429da67	6	24	2	13	System	2024-02-19T07:51:12.817Z	System	2024-02-19T07:51:12.817Z	\N
454	4	6	a9a9f477-5c57-11dc-ad6c-5136d620963c	6	24	2	13	System	2024-02-19T07:51:12.833Z	System	2024-02-19T07:51:12.833Z	\N
455	1	6	c317f789-5c57-11dc-ad6c-5136d620963c	6	59	2	13	System	2024-02-19T07:51:12.838Z	System	2024-02-19T07:51:12.838Z	\N
896	1	6	077184a9-c028-4619-bee7-f14b4ac04e61	45	141	2	11	admin	2024-02-19T08:09:41.376Z	admin	2024-02-19T08:09:41.376Z	\N
456	3	6	ceca5a11-5c57-11dc-ad6c-5136d620963c	6	61	2	13	System	2024-02-19T07:51:12.840Z	System	2024-02-19T07:51:12.840Z	\N
457	2	6	c074eb05-5c57-11dc-ad6c-5136d620963c	6	66	2	13	System	2024-02-19T07:51:12.845Z	System	2024-02-19T07:51:12.845Z	\N
892	12	6	71b5b65b-d92a-4944-9403-48b7ebf8664c	54	51	4	11	admin	2024-02-19T08:09:40.919Z	admin	2024-02-19T09:32:15.922Z	\N
458	2	6	c074eb06-5c57-11dc-ad6c-5136d620963c	6	72	2	13	System	2024-02-19T07:51:12.853Z	System	2024-02-19T07:51:12.853Z	\N
459	2	6	d11a167c-5c57-11dc-ad6c-5136d620963c	6	75	2	13	System	2024-02-19T07:51:12.857Z	System	2024-02-19T07:51:12.857Z	\N
460	2	6	c074eb07-5c57-11dc-ad6c-5136d620963c	6	79	2	13	System	2024-02-19T07:51:12.860Z	System	2024-02-19T07:51:12.860Z	\N
461	2	6	d34fbb36-5c57-11dc-ad6c-5136d620963c	6	75	2	13	System	2024-02-19T07:51:12.862Z	System	2024-02-19T07:51:12.862Z	\N
462	1	6	7ea92c92-2c52-47de-a56d-248893c67d58	6	24	2	13	System	2024-02-19T07:51:12.866Z	System	2024-02-19T07:51:12.866Z	\N
463	1	6	d09d01fa-c8c0-4b88-9057-b6ce2ec55379	6	24	2	13	System	2024-02-19T07:51:12.869Z	System	2024-02-19T07:51:12.869Z	\N
464	4	6	05590cd0-607e-11dc-af48-8b100325f217	6	24	2	13	System	2024-02-19T07:51:12.873Z	System	2024-02-19T07:51:12.873Z	\N
465	1	6	1e3a3916-607e-11dc-af48-8b100325f217	6	59	2	13	System	2024-02-19T07:51:12.877Z	System	2024-02-19T07:51:12.877Z	\N
466	3	6	1e40c8cc-607e-11dc-af48-8b100325f217	6	61	2	13	System	2024-02-19T07:51:12.878Z	System	2024-02-19T07:51:12.878Z	\N
467	2	6	1e18a751-607e-11dc-af48-8b100325f217	6	66	2	13	System	2024-02-19T07:51:12.881Z	System	2024-02-19T07:51:12.881Z	\N
468	2	6	1e2ddd02-607e-11dc-af48-8b100325f217	6	72	2	13	System	2024-02-19T07:51:12.886Z	System	2024-02-19T07:51:12.886Z	\N
469	2	6	1e74d127-607e-11dc-af48-8b100325f217	6	75	2	13	System	2024-02-19T07:51:12.888Z	System	2024-02-19T07:51:12.888Z	\N
470	2	6	1e2ddd03-607e-11dc-af48-8b100325f217	6	79	2	13	System	2024-02-19T07:51:12.891Z	System	2024-02-19T07:51:12.891Z	\N
471	2	6	1e88cd61-607e-11dc-af48-8b100325f217	6	75	2	13	System	2024-02-19T07:51:12.893Z	System	2024-02-19T07:51:12.893Z	\N
472	2	6	tag:tag-root	6	47	2	36	System	2024-02-19T07:51:12.895Z	System	2024-02-19T07:51:12.895Z	\N
474	5	6	352e677b-940e-454b-8b1d-2f5675591168	6	24	2	11	System	2024-02-19T07:51:12.909Z	System	2024-02-19T07:51:12.909Z	\N
475	4	6	7f7f8071-aca5-47ed-85e9-d6e10aefd2af	6	24	2	38	System	2024-02-19T07:51:12.917Z	System	2024-02-19T07:51:12.917Z	\N
476	4	6	19b0979e-2c62-4866-853f-50f481f70694	6	24	2	40	System	2024-02-19T07:51:12.930Z	System	2024-02-19T07:51:12.930Z	\N
477	2	6	14bddaab-94ef-4081-97eb-db3d19944f21	6	3	2	42	\N	\N	\N	\N	\N
478	1	6	GROUP_ALFRESCO_ADMINISTRATORS	6	89	2	43	\N	\N	\N	\N	\N
479	1	6	GROUP_EMAIL_CONTRIBUTORS	6	89	2	43	\N	\N	\N	\N	\N
480	1	6	GROUP_SITE_ADMINISTRATORS	6	89	2	43	\N	\N	\N	\N	\N
481	1	6	GROUP_ALFRESCO_SEARCH_ADMINISTRATORS	6	89	2	43	\N	\N	\N	\N	\N
482	1	6	GROUP_ALFRESCO_MODEL_ADMINISTRATORS	6	89	2	43	\N	\N	\N	\N	\N
483	1	6	GROUP_ALFRESCO_SYSTEM_ADMINISTRATORS	6	89	2	43	\N	\N	\N	\N	\N
484	2	6	a852d4ec-8abf-41f0-9a45-0c451a52a837	6	3	2	44	\N	\N	\N	\N	\N
485	1	6	AUTH.ALF	6	91	2	45	System	2024-02-19T07:51:12.962Z	System	2024-02-19T07:51:12.962Z	\N
486	1	6	APP.DEFAULT	6	91	2	45	System	2024-02-19T07:51:12.975Z	System	2024-02-19T07:51:12.975Z	\N
487	2	6	remote_credentials_container	6	3	2	46	\N	\N	\N	\N	\N
488	2	6	syncset_definitions_container	6	3	2	48	\N	\N	\N	\N	\N
473	2	6	6db4f814-cdae-4b17-82e5-4f2b3b427205	12	81	2	11	System	2024-02-19T07:51:12.906Z	System	2024-02-19T07:51:28.603Z	\N
489	1	6	d042300f-22b0-4c2e-acec-f0277066f9f2	6	24	2	13	System	2024-02-19T07:51:13.262Z	System	2024-02-19T07:51:13.262Z	\N
490	1	6	f886e287-1763-4152-af78-d3be63bdac07	6	24	2	13	System	2024-02-19T07:51:13.264Z	System	2024-02-19T07:51:13.264Z	\N
491	2	6	d9f68200-b9cb-498d-bfd3-a3c42abe7288	6	51	2	13	System	2024-02-19T07:51:13.266Z	System	2024-02-19T07:51:13.266Z	\N
492	2	6	351377aa-8e75-45b8-8e02-b3ae66abee4e	6	51	2	13	System	2024-02-19T07:51:13.270Z	System	2024-02-19T07:51:13.270Z	\N
493	1	6	f93996af-56dd-4077-a400-ebc4e34ddccd	6	24	2	13	System	2024-02-19T07:51:13.275Z	System	2024-02-19T07:51:13.275Z	\N
494	1	6	7bd434ca-a83a-4e74-b922-340e11116487	6	24	2	13	System	2024-02-19T07:51:13.277Z	System	2024-02-19T07:51:13.277Z	\N
495	1	6	f5ee6a37-b4ea-41c5-8463-a1187d026989	6	97	2	13	System	2024-02-19T07:51:13.280Z	System	2024-02-19T07:51:13.280Z	\N
496	1	6	73c9bc69-e306-4b1a-90ac-7e6c0b4876fb	6	24	2	13	System	2024-02-19T07:51:13.282Z	System	2024-02-19T07:51:13.282Z	\N
497	1	6	5b0840e9-d167-4c1d-b444-4f59209c4ed3	6	24	2	13	System	2024-02-19T07:51:13.284Z	System	2024-02-19T07:51:13.284Z	\N
499	1	6	replication_actions_space	6	24	2	13	System	2024-02-19T07:51:13.292Z	System	2024-02-19T07:51:13.292Z	\N
500	1	6	bf4e44a7-6a45-4a67-aef4-d851b7fc1933	6	59	2	13	System	2024-02-19T07:51:13.297Z	System	2024-02-19T07:51:13.297Z	\N
501	1	6	2969804d-45b0-4f1f-97be-a8da4306a0b7	6	61	2	13	System	2024-02-19T07:51:13.303Z	System	2024-02-19T07:51:13.303Z	\N
502	2	6	01396b33-f231-4751-b7ee-1f8a10c208bb	6	66	2	13	System	2024-02-19T07:51:13.309Z	System	2024-02-19T07:51:13.309Z	\N
503	1	6	9d843405-6c73-418a-b243-62ea0d72828e	6	59	2	13	System	2024-02-19T07:51:13.319Z	System	2024-02-19T07:51:13.319Z	\N
504	1	6	08f5cd30-d760-4ed5-9af0-0327c3a262eb	6	79	2	13	System	2024-02-19T07:51:13.322Z	System	2024-02-19T07:51:13.322Z	\N
505	1	6	e04d9bd4-81cd-4f1d-be15-8ef5cd2b273d	6	75	2	13	System	2024-02-19T07:51:13.324Z	System	2024-02-19T07:51:13.324Z	\N
506	1	6	b3ac9eac-e055-439f-b7a4-851c0aca358f	6	72	2	13	System	2024-02-19T07:51:13.325Z	System	2024-02-19T07:51:13.325Z	\N
507	1	6	c1457e36-6d3d-49aa-a55c-115b9f5fa6d8	6	75	2	13	System	2024-02-19T07:51:13.326Z	System	2024-02-19T07:51:13.326Z	\N
508	1	6	91d9663b-6c3e-4595-9d65-9f2d678d1a95	6	24	2	13	System	2024-02-19T07:51:13.329Z	System	2024-02-19T07:51:13.329Z	\N
509	4	6	fa5cc5f1-c35b-4d53-9953-4330300ef311	6	51	2	13	System	2024-02-19T07:51:13.332Z	System	2024-02-19T07:51:13.332Z	\N
510	4	6	0275f30f-8e4b-4a65-bbbe-b1e276e8da05	6	51	2	13	System	2024-02-19T07:51:13.338Z	System	2024-02-19T07:51:13.338Z	\N
511	4	6	f3988afa-bc6c-4095-a563-3741394608ff	6	51	2	13	System	2024-02-19T07:51:13.342Z	System	2024-02-19T07:51:13.342Z	\N
512	4	6	b13c5f88-c10d-4d52-b090-4ef7e239ff02	6	51	2	13	System	2024-02-19T07:51:13.348Z	System	2024-02-19T07:51:13.348Z	\N
498	2	6	rendering_actions_space	16	24	2	13	System	2024-02-19T07:51:13.289Z	System	2024-02-19T07:51:29.239Z	\N
513	4	6	c48de60e-fb45-467f-a376-dae96152ac81	6	51	2	13	System	2024-02-19T07:51:13.353Z	System	2024-02-19T07:51:13.353Z	\N
514	4	6	ee992cec-a3ae-44c3-8601-5bc572898963	6	51	2	13	System	2024-02-19T07:51:13.357Z	System	2024-02-19T07:51:13.357Z	\N
515	4	6	104ed68c-19ab-4b31-9859-a5af4e366010	6	51	2	13	System	2024-02-19T07:51:13.362Z	System	2024-02-19T07:51:13.362Z	\N
516	4	6	950afb19-5ff8-442b-8c5f-c80653cdde26	6	51	2	13	System	2024-02-19T07:51:13.370Z	System	2024-02-19T07:51:13.370Z	\N
517	4	6	ea03b806-18c6-460c-964b-f1de1179fc6f	6	51	2	13	System	2024-02-19T07:51:13.375Z	System	2024-02-19T07:51:13.375Z	\N
518	4	6	d64d95ba-61a2-4f02-958e-17512994895f	6	51	2	13	System	2024-02-19T07:51:13.380Z	System	2024-02-19T07:51:13.380Z	\N
519	4	6	6d6a532b-6fae-456b-82d8-76de393797bb	6	51	2	13	System	2024-02-19T07:51:13.386Z	System	2024-02-19T07:51:13.386Z	\N
520	4	6	3d022f89-1ee0-49af-ac54-55c16702b188	6	51	2	13	System	2024-02-19T07:51:13.391Z	System	2024-02-19T07:51:13.391Z	\N
521	4	6	6f92590a-da33-4928-96bf-35c56e33f9e8	6	51	2	13	System	2024-02-19T07:51:13.397Z	System	2024-02-19T07:51:13.397Z	\N
522	4	6	861e89b6-f0ed-48e0-9439-27e311676eff	6	51	2	13	System	2024-02-19T07:51:13.402Z	System	2024-02-19T07:51:13.402Z	\N
523	4	6	9a09a20b-7321-4449-8eb2-ea82d1adfe97	6	51	2	13	System	2024-02-19T07:51:13.408Z	System	2024-02-19T07:51:13.408Z	\N
524	4	6	c8cd8cd5-c45f-452a-aea6-769c50d8134b	6	51	2	13	System	2024-02-19T07:51:13.414Z	System	2024-02-19T07:51:13.414Z	\N
525	4	6	acb1c1ab-0255-4a8e-ad78-f563679964b7	6	51	2	13	System	2024-02-19T07:51:13.418Z	System	2024-02-19T07:51:13.418Z	\N
526	4	6	df3313f2-62dd-476f-b00b-264cdf9f8431	6	51	2	13	System	2024-02-19T07:51:13.423Z	System	2024-02-19T07:51:13.423Z	\N
527	4	6	13dffb0a-4462-4e1c-bcda-12b5161eb679	6	51	2	13	System	2024-02-19T07:51:13.430Z	System	2024-02-19T07:51:13.430Z	\N
528	4	6	f5ebde97-626b-455e-bcc7-15b9b9f67d32	6	51	2	13	System	2024-02-19T07:51:13.435Z	System	2024-02-19T07:51:13.435Z	\N
529	4	6	87d3a748-51fc-4fb6-b7a4-c60baf9ece1b	6	51	2	13	System	2024-02-19T07:51:13.439Z	System	2024-02-19T07:51:13.439Z	\N
530	4	6	8942f711-54d8-4968-b492-a54e81f24914	6	51	2	13	System	2024-02-19T07:51:13.445Z	System	2024-02-19T07:51:13.445Z	\N
531	2	6	8344a2a3-d72e-41b8-95d4-43401f5cf6d3	6	51	2	13	System	2024-02-19T07:51:13.452Z	System	2024-02-19T07:51:13.452Z	\N
532	2	6	5ab94998-938d-4d78-9304-8e2d9e5a5a6c	6	51	2	13	System	2024-02-19T07:51:13.457Z	System	2024-02-19T07:51:13.457Z	\N
533	2	6	e08581de-523b-404a-9a17-0729a33653a1	6	51	2	13	System	2024-02-19T07:51:13.460Z	System	2024-02-19T07:51:13.460Z	\N
534	2	6	1633111b-a991-45d3-b2e1-1569025cbd42	6	51	2	13	System	2024-02-19T07:51:13.465Z	System	2024-02-19T07:51:13.465Z	\N
535	2	6	62ea3391-a337-400a-ae2f-664dabd5e317	6	51	2	13	System	2024-02-19T07:51:13.468Z	System	2024-02-19T07:51:13.468Z	\N
536	2	6	a47a3d0e-a7b9-4878-b74a-fc92670089aa	6	51	2	13	System	2024-02-19T07:51:13.472Z	System	2024-02-19T07:51:13.472Z	\N
537	2	6	08b18521-b86b-4a57-8d89-6746dfc98dba	6	51	2	13	System	2024-02-19T07:51:13.477Z	System	2024-02-19T07:51:13.477Z	\N
538	2	6	109d5f9f-20a9-4b02-a692-043d9231b68a	6	51	2	13	System	2024-02-19T07:51:13.483Z	System	2024-02-19T07:51:13.483Z	\N
539	2	6	9efd0924-e8e3-49d1-90e3-dba4bb2d35e6	6	51	2	13	System	2024-02-19T07:51:13.488Z	System	2024-02-19T07:51:13.488Z	\N
540	2	6	0d53c195-d012-4d1a-b98a-f569231fdcd6	6	51	2	13	System	2024-02-19T07:51:13.492Z	System	2024-02-19T07:51:13.492Z	\N
541	2	6	d04bf0e5-3cae-44e0-9ae5-72ed680690db	6	51	2	13	System	2024-02-19T07:51:13.500Z	System	2024-02-19T07:51:13.500Z	\N
542	2	6	a3abf45b-2ef8-4d4a-9ca9-12b0b7a08ae8	6	51	2	13	System	2024-02-19T07:51:13.505Z	System	2024-02-19T07:51:13.505Z	\N
543	2	6	e12dc5f2-cd56-4368-832f-ea5087b6dfc6	6	51	2	13	System	2024-02-19T07:51:13.509Z	System	2024-02-19T07:51:13.509Z	\N
544	2	6	fe51a7b7-d29f-465f-9bbf-5ca506bc405d	6	51	2	13	System	2024-02-19T07:51:13.514Z	System	2024-02-19T07:51:13.514Z	\N
545	2	6	77aef235-c49a-4422-a8be-0b10e92c3c4c	6	51	2	13	System	2024-02-19T07:51:13.518Z	System	2024-02-19T07:51:13.518Z	\N
546	2	6	419054ce-0b15-4e95-b793-f6024f1c1c7d	6	51	2	13	System	2024-02-19T07:51:13.523Z	System	2024-02-19T07:51:13.523Z	\N
547	2	6	623fb626-98d1-4939-9ed6-6bc8231b2d83	6	51	2	13	System	2024-02-19T07:51:13.528Z	System	2024-02-19T07:51:13.528Z	\N
548	2	6	da39d72a-daea-40b4-9b1e-f5a7c8073093	6	51	2	13	System	2024-02-19T07:51:13.532Z	System	2024-02-19T07:51:13.532Z	\N
549	2	6	60e5641f-63c2-4e8f-b8af-d0394385ce39	6	51	2	13	System	2024-02-19T07:51:13.536Z	System	2024-02-19T07:51:13.536Z	\N
550	2	6	e5b2e093-694d-470e-8fe6-37be4253d3e7	6	51	2	13	System	2024-02-19T07:51:13.540Z	System	2024-02-19T07:51:13.540Z	\N
551	2	6	dc410625-f784-41e2-858d-263c79c76b10	6	51	2	13	System	2024-02-19T07:51:13.546Z	System	2024-02-19T07:51:13.546Z	\N
552	2	6	5962cd90-e282-4889-b110-3f5dec052d5f	6	51	2	13	System	2024-02-19T07:51:13.551Z	System	2024-02-19T07:51:13.551Z	\N
553	2	6	d4cb75c9-3faa-4c85-8142-8e855d4f287a	6	51	2	13	System	2024-02-19T07:51:13.556Z	System	2024-02-19T07:51:13.556Z	\N
554	2	6	7a159e48-a7bf-42c3-9a39-83f645f6fe1d	6	51	2	13	System	2024-02-19T07:51:13.578Z	System	2024-02-19T07:51:13.578Z	\N
555	2	6	8ade99cb-fd1c-43e9-b01a-057c61cf472d	6	51	2	13	System	2024-02-19T07:51:13.583Z	System	2024-02-19T07:51:13.583Z	\N
556	2	6	84040179-36e8-420e-8fca-b5d7800c57cd	6	51	2	13	System	2024-02-19T07:51:13.589Z	System	2024-02-19T07:51:13.589Z	\N
557	2	6	9fecfb12-bf59-4313-99db-60ea86ede654	6	51	2	13	System	2024-02-19T07:51:13.594Z	System	2024-02-19T07:51:13.594Z	\N
558	2	6	f74aa801-eeb3-49af-b1d3-d234bcbc2f40	6	51	2	13	System	2024-02-19T07:51:13.599Z	System	2024-02-19T07:51:13.599Z	\N
559	2	6	26861d38-f43f-4ea9-842a-d144c51c827d	6	51	2	13	System	2024-02-19T07:51:13.604Z	System	2024-02-19T07:51:13.604Z	\N
560	2	6	49857273-5c48-463c-8938-36608507ff12	6	51	2	13	System	2024-02-19T07:51:13.609Z	System	2024-02-19T07:51:13.609Z	\N
561	2	6	b27b0689-2a4d-4b0a-bbe3-ecb0599233fa	6	51	2	13	System	2024-02-19T07:51:13.614Z	System	2024-02-19T07:51:13.614Z	\N
562	2	6	c0d19f3a-ff54-472a-8222-0e74eff20b32	6	51	2	13	System	2024-02-19T07:51:13.619Z	System	2024-02-19T07:51:13.619Z	\N
563	2	6	56e3b869-4f79-462c-bfe7-e7bfad651b85	6	51	2	13	System	2024-02-19T07:51:13.625Z	System	2024-02-19T07:51:13.625Z	\N
564	2	6	3beed24a-1300-4792-b713-0a83c3dbcff4	6	51	2	13	System	2024-02-19T07:51:13.630Z	System	2024-02-19T07:51:13.630Z	\N
565	2	6	fcbdf95f-29a8-4c13-a517-0550e6745336	6	51	2	13	System	2024-02-19T07:51:13.635Z	System	2024-02-19T07:51:13.635Z	\N
566	2	6	e00a5739-f8f4-41a5-9b55-b52f29d0f6c5	6	51	2	13	System	2024-02-19T07:51:13.640Z	System	2024-02-19T07:51:13.640Z	\N
567	2	6	f6079b30-273f-4de2-8ed5-74c08acfe97b	6	51	2	13	System	2024-02-19T07:51:13.646Z	System	2024-02-19T07:51:13.646Z	\N
568	2	6	90be6dc7-e22a-49a9-916c-bc06e0b69d5b	6	51	2	13	System	2024-02-19T07:51:13.651Z	System	2024-02-19T07:51:13.651Z	\N
569	2	6	fdf3f8ea-f5d8-4841-95c3-95f16741da46	6	51	2	13	System	2024-02-19T07:51:13.657Z	System	2024-02-19T07:51:13.657Z	\N
570	2	6	b5331a2a-61bc-475f-9864-dd27809f470c	6	51	2	13	System	2024-02-19T07:51:13.663Z	System	2024-02-19T07:51:13.663Z	\N
571	2	6	2ea76f3f-e277-4420-ae87-44076864f65e	6	51	2	13	System	2024-02-19T07:51:13.669Z	System	2024-02-19T07:51:13.669Z	\N
572	2	6	de6b1313-97e2-4153-9029-58247e099203	6	51	2	13	System	2024-02-19T07:51:13.675Z	System	2024-02-19T07:51:13.675Z	\N
573	2	6	adf420e1-a7ac-4833-b903-049a75222639	6	51	2	13	System	2024-02-19T07:51:13.681Z	System	2024-02-19T07:51:13.681Z	\N
574	2	6	7e8c97a8-f343-4884-b960-fb460b3346fc	6	51	2	13	System	2024-02-19T07:51:13.687Z	System	2024-02-19T07:51:13.687Z	\N
575	2	6	e3332757-f9c0-43b2-a13c-5d9238e38523	6	51	2	13	System	2024-02-19T07:51:13.693Z	System	2024-02-19T07:51:13.693Z	\N
576	2	6	ed5bd9e9-cd56-4df2-8b0d-2380bf97dda9	6	51	2	13	System	2024-02-19T07:51:13.698Z	System	2024-02-19T07:51:13.698Z	\N
577	2	6	ebbc37af-1bd9-4ba4-ac51-6fa4b3d1ad65	6	51	2	13	System	2024-02-19T07:51:13.704Z	System	2024-02-19T07:51:13.704Z	\N
13	4	6	6d7c466b-efd0-4b88-b77f-a941f3a2f025	23	24	2	10	System	2024-02-19T07:51:11.479Z	admin	2024-02-19T07:55:23.214Z	\N
578	2	6	9c7f2e39-9f00-44c7-b41b-b7d88a859fa4	6	51	2	13	System	2024-02-19T07:51:13.709Z	System	2024-02-19T07:51:13.709Z	\N
579	2	6	7844d344-8e8b-48ed-9ec8-16a7923431a3	6	51	2	13	System	2024-02-19T07:51:13.714Z	System	2024-02-19T07:51:13.714Z	\N
580	2	6	93f5a353-e656-44b9-a1cf-d65ad5773acc	6	51	2	13	System	2024-02-19T07:51:13.720Z	System	2024-02-19T07:51:13.720Z	\N
581	2	6	e0c63b25-bb6c-4e95-a7b5-f5eb94c6c754	6	51	2	13	System	2024-02-19T07:51:13.725Z	System	2024-02-19T07:51:13.725Z	\N
582	2	6	downloads_container	6	3	2	50	\N	\N	\N	\N	\N
583	3	6	9c4d8796-d8fe-42bd-b765-2f5dfcf9a5e2	6	103	2	19	System	2024-02-19T07:51:13.745Z	System	2024-02-19T07:51:13.745Z	\N
395	2	6	bed1cc35-c3ab-46c9-9322-fd2fa6789fbf	6	24	2	13	System	2024-02-19T07:51:12.562Z	System	2024-02-19T07:51:13.772Z	\N
15	2	6	613ccb27-b758-49f5-b973-c24fa86ae629	6	24	2	13	System	2024-02-19T07:51:11.537Z	System	2024-02-19T07:51:13.772Z	\N
16	2	6	716473d4-4a4c-4e0c-a9f3-497e33f2e55e	6	24	2	13	System	2024-02-19T07:51:11.543Z	System	2024-02-19T07:51:13.772Z	\N
17	2	6	832b097a-c6ab-4e37-a0e3-a1c52d0e79bf	6	24	2	13	System	2024-02-19T07:51:11.549Z	System	2024-02-19T07:51:13.772Z	\N
19	3	6	85b287be-c8c3-4845-9b5c-697d6ea9da3f	6	24	2	14	System	2024-02-19T07:51:11.559Z	System	2024-02-19T07:51:13.772Z	\N
23	3	6	4b351ebb-0198-4773-9a5a-f82f36d27c0f	6	24	2	18	System	2024-02-19T07:51:11.600Z	System	2024-02-19T07:51:13.772Z	\N
584	2	6	cdd0a458-9ae0-446f-a503-cfed2cfc9e3c	9	51	2	13	System	2024-02-19T07:51:27.267Z	System	2024-02-19T07:51:27.267Z	\N
21	3	6	7024e0e8-8271-48d7-80df-9823bb0a7456	10	24	2	13	System	2024-02-19T07:51:11.589Z	System	2024-02-19T07:51:27.278Z	\N
586	1	6	1dd48c8a-095f-4a30-a746-7e7c8ebd3124	11	89	2	43	\N	\N	\N	\N	\N
587	1	6	21cd1377-e650-431a-af45-d07ca51037d3	11	91	2	45	admin	2024-02-19T07:51:27.329Z	admin	2024-02-19T07:51:27.329Z	\N
588	1	6	e0a860fe-91c8-4ca8-a006-03b62e36da30	11	89	2	43	\N	\N	\N	\N	\N
589	1	6	fa0a3229-3020-4e79-8293-b0234a420d8a	11	89	2	43	\N	\N	\N	\N	\N
590	1	6	df0fd27a-d76c-4aa5-b731-55914514c618	11	89	2	43	\N	\N	\N	\N	\N
591	1	6	46bfa60d-243b-4788-adc3-84ade62df691	11	89	2	43	\N	\N	\N	\N	\N
592	1	1	eedbc083-548e-4a25-a54d-45671ebd54d0	11	5	2	2	\N	\N	\N	\N	\N
593	1	1	7b6c2624-aec7-479c-a754-d6047fb1a225	11	5	2	2	\N	\N	\N	\N	\N
594	3	6	dc103838-645f-43c1-8a2a-bc187e13c343	11	35	3	54	\N	\N	\N	\N	\N
595	5	6	d65a4795-578e-4780-9f27-96ce43bde700	11	51	3	55	abeecher	2015-09-29T10:45:15.729Z	abeecher	2015-09-29T10:45:15.729Z	\N
596	5	6	198500fc-1e99-4f5f-8926-248cea433366	11	141	3	55	abeecher	2015-09-29T10:45:16.111Z	abeecher	2015-09-29T10:45:16.111Z	\N
597	3	6	b6d80d49-21cc-4f04-9c92-e7063037543f	11	35	3	56	\N	\N	\N	\N	\N
728	1	2	457a3c09-1528-4178-bc1f-64100ca663b4	13	3	2	4	\N	\N	\N	\N	\N
729	1	2	5864f1f9-42aa-448a-a328-c535296709fb	13	3	2	4	\N	\N	\N	\N	\N
730	1	2	48c19e8f-0482-4f66-80d3-f523269b3537	13	3	2	4	\N	\N	\N	\N	\N
731	1	2	5b7c52e3-9832-4c1a-9bd8-caa8b232bd6b	13	3	2	4	\N	\N	\N	\N	\N
732	1	2	98e9b31d-38e5-4834-9144-aec282e79cb5	13	3	2	4	\N	\N	\N	\N	\N
733	1	2	348f2af1-6a0a-4151-b624-b0175596aab4	13	3	2	4	\N	\N	\N	\N	\N
14	4	6	811e21ac-7d5a-469b-ab6e-ec3c8cd8a864	18	24	2	12	System	2024-02-19T07:51:11.520Z	System	2024-02-19T07:51:29.473Z	\N
598	5	6	42881f63-38cf-479d-a303-52e7ff99cb75	11	51	3	57	mjackson	2015-09-29T10:44:47.877Z	mjackson	2015-09-29T10:44:47.877Z	\N
599	5	6	3fbde500-298b-4e80-ae50-e65a5cbc2c4d	11	141	3	57	mjackson	2015-09-29T10:44:48.322Z	mjackson	2015-09-29T10:44:48.322Z	\N
600	1	6	969d8465-1cb2-4993-a7df-70c2cabe9c83	11	148	1	\N	\N	\N	\N	\N	\N
601	5	6	b4cff62a-664d-4d45-9302-98723eac1319	11	106	4	58	mjackson	2011-02-15T20:16:27.080Z	mjackson	2011-02-15T20:16:27.080Z	\N
602	4	6	8f2105b4-daaf-4874-9e8a-2152569d109b	11	24	4	59	mjackson	2011-02-15T20:16:28.292Z	mjackson	2011-02-15T20:16:28.292Z	\N
604	3	6	e0856836-ed5e-4eee-b8e5-bd7e8fb9384c	11	24	4	59	mjackson	2011-02-15T21:01:29.482Z	mjackson	2011-02-15T21:01:29.482Z	\N
606	1	4	e4301c05-8b0e-445c-a658-f3ec55d2a7e5	11	164	2	60	System	2024-02-19T07:51:27.755Z	System	2024-02-19T07:51:27.755Z	\N
607	2	4	96b5916f-67e2-48ce-a8dc-37a25a5cc6ff	11	51	4	60	System	2024-02-19T07:51:27.760Z	System	2024-02-19T07:51:27.760Z	\N
605	9	6	1a0b110f-1e09-4ca2-b367-fe25e4964a4e	11	51	4	59	abeecher	2011-02-15T21:26:54.600Z	admin	2011-06-14T10:28:54.714Z	\N
608	5	6	d27ef7b9-7f2f-41c3-8069-cdd48c8714a1	11	141	4	59	abeecher	2011-02-15T21:26:55.401Z	abeecher	2011-02-16T10:34:06.014Z	\N
609	5	6	3f66a51e-580c-4c67-b183-a5d73cdbdd78	11	141	4	59	mjackson	2011-02-16T10:13:43.173Z	abeecher	2011-02-16T10:34:06.012Z	\N
610	3	6	880a0f47-31b1-4101-b20b-4d325e54e8b1	11	24	4	59	mjackson	2011-02-15T21:04:41.894Z	mjackson	2011-02-15T21:04:41.894Z	\N
611	6	6	7bb9c846-fcc5-43b5-a893-39e46ebe94d4	11	51	4	59	abeecher	2011-03-03T10:34:52.092Z	abeecher	2011-03-03T10:34:52.092Z	\N
612	5	6	7e72bcb1-23e3-4d38-a61b-02f8cbe7b8c9	11	141	4	59	abeecher	2011-03-03T10:34:58.796Z	abeecher	2011-03-03T10:34:58.796Z	\N
613	6	6	74cd8a96-8a21-47e5-9b3b-a1b3e296787d	11	51	4	59	abeecher	2011-03-03T10:34:52.102Z	abeecher	2011-03-03T10:34:52.102Z	\N
614	5	6	ac5c46db-719c-40d3-aa0c-e51b23fe5c32	11	141	4	59	abeecher	2011-03-03T10:34:58.758Z	abeecher	2011-03-03T10:34:58.758Z	\N
615	6	6	80a94ac8-3ece-47ad-864e-5d939424c47c	11	51	4	59	abeecher	2011-03-03T10:34:52.139Z	abeecher	2011-03-03T10:34:52.139Z	\N
616	5	6	242ddf76-d864-4db3-8705-b012c4a48e51	11	141	4	59	abeecher	2011-03-03T10:34:59.473Z	abeecher	2011-03-03T10:34:59.473Z	\N
617	5	6	f2f3dc4b-7ced-49fc-8e85-ce53a459dd87	11	141	4	59	mjackson	2011-03-03T11:00:15.197Z	mjackson	2011-03-03T11:00:15.197Z	\N
618	6	6	267839b2-f466-42c5-9a35-cb3e41281bb9	11	51	4	59	abeecher	2011-03-03T10:34:52.620Z	abeecher	2011-03-03T10:34:52.620Z	\N
619	5	6	9a7a891a-0db3-49dd-bd8d-8340f9c1cee8	11	141	4	59	abeecher	2011-03-03T10:35:00.221Z	abeecher	2011-03-03T10:35:00.221Z	\N
620	6	6	1f4ce811-1c61-4553-ac23-63b68bf1d121	11	51	4	59	abeecher	2011-03-03T10:34:52.623Z	abeecher	2011-03-03T10:34:52.623Z	\N
621	5	6	d20da3de-d4cd-4e28-892c-261e4c3b2669	11	141	4	59	abeecher	2011-03-03T10:35:00.614Z	abeecher	2011-03-03T10:35:00.614Z	\N
622	6	6	0516a5cc-fc04-4512-a4ed-b595b7c3908b	11	51	4	59	abeecher	2011-03-03T10:34:52.784Z	abeecher	2011-03-03T10:34:52.784Z	\N
623	5	6	38db832f-8279-460f-99b8-fed560c8da8e	11	141	4	59	abeecher	2011-03-03T10:35:00.838Z	abeecher	2011-03-03T10:35:00.838Z	\N
624	6	6	0f672fb8-bbdb-41bb-84f3-7b9bb1c39b30	11	51	4	59	abeecher	2011-03-03T10:34:53.458Z	abeecher	2011-03-03T10:34:53.458Z	\N
625	5	6	900ebb9a-2596-40e5-ab51-add0bfdf751f	11	141	4	59	abeecher	2011-03-03T10:35:01.920Z	abeecher	2011-03-03T10:35:01.920Z	\N
626	6	6	14e2200e-9f1c-4274-8b6b-95dc9d59d204	11	51	4	59	abeecher	2011-03-03T10:34:53.482Z	abeecher	2011-03-03T10:34:53.482Z	\N
627	5	6	a6bd3f86-99d3-4cde-b945-887fc3255859	11	141	4	59	abeecher	2011-03-03T10:35:01.509Z	abeecher	2011-03-03T10:35:01.509Z	\N
628	6	6	052539b7-872d-46cc-a7f1-1e0757ed4a5b	11	51	4	59	abeecher	2011-03-03T10:34:53.551Z	abeecher	2011-03-03T10:34:53.551Z	\N
629	5	6	0cbca5ac-84f9-4d07-a2fe-c54c911eebb5	11	141	4	59	abeecher	2011-03-03T10:34:59.319Z	abeecher	2011-03-03T10:34:59.319Z	\N
630	6	6	edcbdf18-36ac-4602-ac3d-79dd7aab7ae4	11	51	4	59	abeecher	2011-03-03T10:34:53.892Z	abeecher	2011-03-03T10:34:53.892Z	\N
631	5	6	bf581ca9-e270-413d-9796-635544674781	11	141	4	59	abeecher	2011-03-03T10:35:01.313Z	abeecher	2011-03-03T10:35:01.313Z	\N
632	6	6	72948f84-4bf1-4ec5-8378-1bed0951600a	11	51	4	59	abeecher	2011-03-03T10:34:53.982Z	abeecher	2011-03-03T10:34:53.982Z	\N
633	5	6	82ee74ea-a596-4d62-b689-ddfe0f7e8c16	11	141	4	59	abeecher	2011-03-03T10:34:59.646Z	abeecher	2011-03-03T10:34:59.646Z	\N
634	3	6	b1a98357-4f7a-470d-bf4c-327501158689	11	24	4	59	mjackson	2011-02-15T21:05:49.308Z	mjackson	2011-02-15T21:05:49.308Z	\N
635	7	6	79a03a3e-a027-4b91-9f14-02b62723591e	11	51	4	59	abeecher	2011-03-03T10:36:45.396Z	abeecher	2011-03-03T10:36:45.396Z	\N
636	5	6	e6758089-d817-4bd8-b328-7c2c693c8cf1	11	141	4	59	abeecher	2011-03-03T10:36:48.359Z	abeecher	2011-03-03T10:36:48.359Z	\N
637	7	6	3deb5413-2c1d-4015-b9c9-2be9648446bc	11	51	4	59	abeecher	2011-03-03T10:36:45.431Z	abeecher	2011-03-03T10:36:45.431Z	\N
638	5	6	e751b9c9-de51-4eac-8b5c-905afbeb667a	11	141	4	59	abeecher	2011-03-03T10:36:48.376Z	abeecher	2011-03-03T10:36:48.376Z	\N
639	3	6	610771be-4d82-479a-a2d7-796adf498084	11	24	4	59	mjackson	2011-02-15T21:14:44.396Z	mjackson	2011-02-15T21:14:44.396Z	\N
640	6	6	43485b48-2ca7-4077-a00c-9bfe810f9fa1	11	51	4	59	abeecher	2011-03-03T10:37:17.994Z	abeecher	2011-03-03T10:37:17.994Z	\N
641	5	6	ad46dd6b-51b3-4bc5-bab8-4b9d4e64f07f	11	141	4	59	abeecher	2011-03-03T10:37:20.937Z	abeecher	2011-03-03T10:37:20.937Z	\N
642	6	6	4d4a272d-60d5-4810-8164-4a1e595d92f2	11	51	4	59	abeecher	2011-03-03T10:37:18.155Z	abeecher	2011-03-03T10:37:18.155Z	\N
643	5	6	8aa76706-4a23-468a-b7b6-4ae8907d629d	11	141	4	59	abeecher	2011-03-03T10:37:20.795Z	abeecher	2011-03-03T10:37:20.795Z	\N
644	5	6	582260e9-672c-4aff-a970-8129650adb72	11	141	4	59	mjackson	2011-03-03T11:00:01.795Z	mjackson	2011-03-03T11:00:01.795Z	\N
645	7	6	7d90c94c-fcf7-4f79-9273-bd1352bbb612	11	51	4	59	abeecher	2011-03-03T10:37:18.155Z	abeecher	2011-03-03T10:37:18.155Z	\N
646	5	6	285b85d9-bcfe-4315-b92a-4cb5b8988871	11	141	4	59	abeecher	2011-03-03T10:37:21.297Z	abeecher	2011-03-03T10:37:21.297Z	\N
647	5	6	9c52032a-576f-4ab3-8620-7598f67592bd	11	141	4	59	mjackson	2011-03-03T10:57:02.996Z	mjackson	2011-03-03T10:57:02.996Z	\N
648	3	6	1d26e465-dea3-42f3-b415-faa8364b9692	11	24	4	59	abeecher	2011-03-08T10:34:50.822Z	abeecher	2011-03-08T10:34:50.822Z	\N
651	3	6	d56afdc3-0174-4f8c-bce8-977cafd712ab	11	24	4	59	mjackson	2011-02-15T21:12:14.908Z	mjackson	2011-02-15T21:12:14.908Z	\N
652	6	6	723a0cff-3fce-495d-baa3-a3cd245ea5dc	11	51	4	59	mjackson	2011-03-03T10:33:35.274Z	mjackson	2011-03-03T10:33:35.274Z	\N
653	5	6	eb9d0356-9f9e-4503-a9f0-0ed4f0075ca7	11	141	4	59	mjackson	2011-03-03T10:33:39.687Z	mjackson	2011-03-03T10:33:39.687Z	\N
654	6	6	7bb7bfa8-997e-4c55-8bd9-2e5029653bc8	11	51	4	59	mjackson	2011-03-03T10:33:35.301Z	mjackson	2011-03-03T10:33:35.301Z	\N
655	5	6	234e1322-d8c5-4d06-8690-7a96dbb9e914	11	141	4	59	mjackson	2011-03-03T10:33:39.695Z	mjackson	2011-03-03T10:33:39.695Z	\N
656	6	6	5fa74ad3-9b5b-461b-9df5-de407f1f4fe7	11	51	4	59	mjackson	2011-02-15T21:35:26.467Z	mjackson	2011-02-15T21:35:26.467Z	\N
657	5	6	09d1a4bf-d8f2-4de6-913c-934205a3e910	11	141	4	59	mjackson	2011-02-16T10:17:05.469Z	mjackson	2011-02-16T10:17:05.469Z	\N
658	5	6	1d74ba8f-b858-4e72-83fd-c1433ef2b1b7	11	141	4	59	mjackson	2011-02-16T10:17:06.658Z	mjackson	2011-02-16T10:17:06.658Z	\N
659	3	6	18a9bfef-81ca-4cf1-9dae-07eef1c5b175	11	195	4	59	mjackson	2011-02-16T10:30:10.642Z	mjackson	2011-02-16T10:30:10.642Z	\N
660	3	6	b7ba359a-b3df-4c0a-a763-df0b8b19a737	11	197	4	59	mjackson	2011-02-16T10:30:10.656Z	mjackson	2011-02-16T10:30:10.656Z	\N
661	4	6	86796712-4dc6-4b8d-973f-a943ef7f23ed	11	198	4	59	mjackson	2011-02-16T10:30:10.663Z	mjackson	2011-02-16T10:30:10.663Z	\N
662	3	6	a211774d-ba6d-4a35-b97f-dacfaac7bde3	11	24	4	59	mjackson	2011-02-15T21:16:26.500Z	mjackson	2011-02-15T21:16:26.500Z	\N
663	5	6	f3bb5d08-9fd1-46da-a94a-97f20f1ef208	11	51	4	59	mjackson	2011-02-24T16:16:37.286Z	mjackson	2011-02-24T16:16:37.286Z	\N
664	5	6	cbff1e97-dc79-41a9-adf0-09a2f697759e	11	141	4	59	mjackson	2011-02-24T16:16:44.342Z	mjackson	2011-02-24T16:16:44.342Z	\N
665	5	6	a2361514-eec8-4a7f-a16a-bed654b097b2	11	141	4	59	mjackson	2011-02-24T16:16:47.280Z	mjackson	2011-02-24T16:16:47.280Z	\N
666	5	6	150398b3-7f82-4cf6-af63-c450ef6c5eb8	11	51	4	59	mjackson	2011-02-24T16:16:37.300Z	mjackson	2011-02-24T16:16:37.300Z	\N
667	5	6	e54bccbb-6b04-449b-a9e6-830882d9b978	11	141	4	59	mjackson	2011-02-24T16:16:43.674Z	mjackson	2011-02-24T16:16:43.674Z	\N
668	5	6	59b60ea3-5fa8-440c-a846-8222d62e07a3	11	141	4	59	mjackson	2011-02-24T17:16:04.464Z	mjackson	2011-02-24T17:16:04.464Z	\N
669	5	6	a8290263-4178-48f5-a0b0-be155a424828	11	51	4	59	mjackson	2011-02-24T16:16:37.332Z	mjackson	2011-02-24T16:16:37.332Z	\N
670	5	6	9b8ce6d7-67eb-4918-ae0b-f5f9f0533c0b	11	141	4	59	mjackson	2011-02-24T16:16:45.489Z	mjackson	2011-02-24T16:16:45.489Z	\N
671	5	6	928d2e14-8869-41f6-a2bc-489a37baa560	11	141	4	59	mjackson	2011-02-24T17:16:25.660Z	mjackson	2011-02-24T17:16:25.660Z	\N
672	3	6	38745585-816a-403f-8005-0a55c0aec813	11	24	4	59	mjackson	2011-02-15T21:18:38.144Z	mjackson	2011-02-15T21:18:38.144Z	\N
673	5	6	5515d3e1-bb2a-42ed-833c-52802a367033	11	51	4	59	mjackson	2011-03-03T10:31:30.596Z	mjackson	2011-03-03T10:31:31.651Z	\N
674	5	6	e725ee47-62c6-4ae9-a761-9b69ba2835c5	11	141	4	59	mjackson	2011-03-03T10:32:15.048Z	mjackson	2011-03-03T10:32:15.048Z	\N
675	5	6	41c25437-ce2e-47e1-8e3d-a2f3008e7456	11	141	4	59	mjackson	2011-03-03T10:32:32.773Z	mjackson	2011-03-03T10:32:32.773Z	\N
676	5	6	99cb2789-f67e-41ff-bea9-505c138a6b23	11	51	4	59	mjackson	2011-03-03T10:31:30.596Z	mjackson	2011-03-03T10:31:31.651Z	\N
677	5	6	8024c190-0aa9-4d69-b5a3-cd1cfe9c7ecc	11	141	4	59	mjackson	2011-03-03T10:32:23.462Z	mjackson	2011-03-03T10:32:23.462Z	\N
678	5	6	1cffebce-c758-4071-a6ae-1e5730015e81	11	141	4	59	mjackson	2011-03-03T10:33:17.246Z	mjackson	2011-03-03T10:33:17.246Z	\N
679	4	6	0e24b99c-41f0-43e1-a55e-fb9f50d73820	11	24	4	59	mjackson	2011-02-15T20:18:59.808Z	mjackson	2011-02-15T20:18:59.808Z	\N
682	4	6	64f69e69-f61e-42a3-8697-95eea1f2bda2	11	24	4	59	mjackson	2011-02-15T20:19:00.007Z	mjackson	2011-02-15T20:19:00.007Z	\N
683	3	6	a534356f-8dd6-4d9a-8ffb-dc1adb140c01	11	203	4	59	abeecher	2011-02-15T22:14:39.548Z	abeecher	2011-02-15T22:14:39.548Z	\N
684	3	6	4b9ebe73-7b19-4aaf-b596-5e545544e2a6	11	205	4	59	abeecher	2011-02-15T22:15:49.142Z	abeecher	2011-02-15T22:15:49.142Z	\N
686	3	6	a53c7a85-12d0-4eb1-8e03-f030e0778da3	11	205	4	59	abeecher	2011-02-15T22:19:20.437Z	abeecher	2011-02-15T22:19:20.437Z	\N
687	3	6	e57195d3-aeda-432d-bfc4-0a556b2d8ab9	11	205	4	59	abeecher	2011-02-15T22:23:00.750Z	abeecher	2011-02-15T22:23:00.750Z	\N
688	3	6	fc50d8a0-1bac-430e-b13d-3ac271c6578e	11	203	4	59	abeecher	2011-02-15T22:27:06.046Z	abeecher	2011-02-24T17:41:49.912Z	\N
691	3	6	8d4429e7-804f-43cf-bd81-288e561db9a8	11	212	4	59	abeecher	2011-02-15T22:30:39.843Z	abeecher	2011-02-15T22:30:39.843Z	\N
694	4	6	cdefb3a9-8f55-4771-a9e3-06fa370250f6	11	24	4	59	mjackson	2011-02-15T21:46:01.603Z	mjackson	2011-02-15T21:46:01.603Z	\N
696	1	4	1b0156c2-5d00-48d4-9869-78673ead6211	11	164	2	60	System	2024-02-19T07:51:28.342Z	System	2024-02-19T07:51:28.342Z	\N
697	2	4	c055f8f5-d491-49ef-9e20-7d46abb848a7	11	51	4	60	System	2024-02-19T07:51:28.344Z	System	2024-02-19T07:51:28.344Z	\N
699	1	4	4d99ed8b-cda1-4b9a-baa1-bba569834a23	11	164	2	60	System	2024-02-19T07:51:28.363Z	System	2024-02-19T07:51:28.363Z	\N
700	2	4	565bbdcf-6bfd-4dec-8868-e44abb6aad52	11	51	4	60	System	2024-02-19T07:51:28.365Z	System	2024-02-19T07:51:28.365Z	\N
702	1	4	9831d4cf-09e9-4d14-a22b-8a4d5a946a90	11	164	2	60	System	2024-02-19T07:51:28.384Z	System	2024-02-19T07:51:28.384Z	\N
703	2	4	83a1fcca-2d39-4e93-b2dd-395eb6015626	11	51	4	60	System	2024-02-19T07:51:28.387Z	System	2024-02-19T07:51:28.387Z	\N
704	4	6	059c5bc7-2d38-4dc5-96b8-d09cd3c69b4c	11	24	4	59	mjackson	2011-02-15T22:04:54.290Z	mjackson	2011-02-15T22:04:54.290Z	\N
706	4	6	42226a03-34a8-43b0-bb37-d86cd09353f7	11	198	4	59	mjackson	2011-02-15T22:05:46.902Z	mjackson	2011-02-15T22:05:46.902Z	\N
711	4	6	308ad851-b4ab-4f41-bbd0-c83398d2afe4	11	198	4	59	mjackson	2011-02-15T22:06:21.034Z	mjackson	2011-02-15T22:06:21.034Z	\N
713	3	6	0db6f5ce-35b6-40df-9216-c9cf0aaf0961	11	24	4	59	admin	2011-06-14T14:11:32.858Z	admin	2011-06-14T14:11:32.858Z	\N
714	3	6	fa555056-bd0c-4c59-ac9f-1ab5b0e18e27	11	24	4	59	admin	2011-06-14T14:15:58.746Z	admin	2011-06-14T14:15:58.746Z	\N
715	3	6	2edddd3a-2df8-4887-93ad-b33aa073c539	11	24	4	59	admin	2011-06-14T14:16:06.229Z	admin	2011-06-14T14:16:06.229Z	\N
716	3	6	0f746f38-4163-492e-92b2-73ecf7b31fa2	11	24	4	59	admin	2011-06-14T14:16:36.303Z	admin	2011-06-14T14:16:36.303Z	\N
717	4	6	8915ce60-7845-40a2-b74e-4837afdb45a0	11	51	4	59	admin	2011-06-14T14:16:40.866Z	admin	2011-06-14T14:16:40.933Z	\N
718	3	6	ea915509-f71c-4b81-a600-cd96ddb9fce6	11	24	4	59	admin	2011-06-14T14:16:51.808Z	admin	2011-06-14T14:16:51.808Z	\N
719	4	6	0fb80b90-4b71-4c00-b9d2-279ce1beca5d	11	51	4	59	admin	2011-06-14T14:16:56.437Z	admin	2011-06-14T14:16:56.497Z	\N
720	4	6	6c38c7da-ac2c-40d0-a38d-f8517dba80b5	11	51	4	59	admin	2011-06-14T14:16:56.455Z	admin	2011-06-14T14:16:56.517Z	\N
721	4	6	37be157c-741c-4e51-b781-20d36e4e335a	11	51	4	59	admin	2011-06-14T14:16:56.597Z	admin	2011-06-14T14:16:56.658Z	\N
722	4	6	81171dd1-865b-49d1-8c8a-27c6eb260774	11	51	4	59	admin	2011-06-14T14:16:56.600Z	admin	2011-06-14T14:16:56.658Z	\N
723	4	6	8b44ac01-d864-4de8-a86c-8f7ec1cfe07d	11	51	4	59	admin	2011-06-14T14:16:56.735Z	admin	2011-06-14T14:16:56.786Z	\N
724	4	6	a1f014db-b5a4-4ccd-8305-edc12e4a6f7b	11	51	4	59	admin	2011-06-14T14:16:56.919Z	admin	2011-06-14T14:16:56.971Z	\N
725	4	6	1d981316-baaf-4ef7-801b-c6aa5b1e102d	11	51	4	59	admin	2011-06-14T14:16:57.168Z	admin	2011-06-14T14:16:57.244Z	\N
726	4	6	fe0de3c8-5cc8-4852-bc36-bdcaef8c9989	11	51	4	59	admin	2011-06-14T14:16:57.453Z	admin	2011-06-14T14:16:57.530Z	\N
727	4	6	aa62394d-b2db-4489-8a1c-5120ab61a6a5	11	51	4	59	admin	2011-06-14T14:16:58.489Z	admin	2011-06-14T14:16:58.546Z	\N
709	6	6	45210491-2d7c-4a85-ab7c-e6997d32ff02	11	198	4	59	abeecher	2011-02-15T22:09:27.794Z	admin	2011-06-14T10:28:57.790Z	\N
708	6	6	9f37fbee-5a28-4732-8fce-95577e003ad5	11	198	4	59	mjackson	2011-02-15T22:08:49.798Z	admin	2011-06-14T10:28:57.681Z	\N
707	6	6	5eda88dd-74dc-4166-a3df-bb4500ed8877	11	198	4	59	abeecher	2011-02-15T22:06:59.801Z	admin	2011-06-14T10:28:57.647Z	\N
690	4	6	a10b3171-ea96-4d34-b090-f8db09f4efb1	11	212	4	59	abeecher	2011-02-15T22:29:40.511Z	admin	2011-06-14T10:28:57.702Z	\N
692	4	6	7778cf88-836f-4833-a0df-3056d2b20e7a	11	212	4	59	abeecher	2011-02-15T22:31:26.282Z	admin	2011-06-14T10:28:57.751Z	\N
685	4	6	db31dce5-2469-4c68-8641-9becad64a756	11	205	4	59	abeecher	2011-02-15T22:18:03.303Z	admin	2011-06-14T10:28:57.624Z	\N
689	4	6	05571c6d-bf6c-4509-8242-5d159726ec80	11	212	4	59	abeecher	2011-02-15T22:28:52.496Z	admin	2011-06-14T10:28:57.574Z	\N
693	4	6	06b07aa0-c8f5-494b-a2ff-ff134d7bcd9b	11	212	4	59	abeecher	2011-03-08T10:39:44.210Z	admin	2011-06-14T10:28:57.727Z	\N
712	6	6	f85d87f1-79d9-450f-b01c-5fed4b44f86b	11	198	4	59	abeecher	2011-02-15T22:07:30.638Z	admin	2011-06-14T10:28:57.600Z	\N
603	5	6	8bb36efb-c26d-4d2b-9199-ab6922f53c28	11	24	4	59	mjackson	2011-02-15T20:47:03.951Z	mjackson	2011-02-15T21:00:43.616Z	\N
649	6	6	05dedd34-9d9d-48d9-9af6-c81b555541c9	11	51	4	59	abeecher	2011-03-08T10:35:10.064Z	abeecher	2011-03-08T10:37:43.961Z	\N
650	5	6	8ab12916-4897-47fb-94eb-1ab699822ecb	11	24	4	59	mjackson	2011-02-15T20:50:25.839Z	mjackson	2011-02-15T21:08:20.590Z	\N
680	6	6	a38308f8-6f30-4d8a-8576-eaf6703fb9d3	11	199	4	59	mjackson	2011-02-15T21:43:14.377Z	mjackson	2011-02-15T21:43:14.377Z	\N
681	6	6	602b72e5-e365-4eee-b68d-b3dd26270ee3	11	199	4	59	mjackson	2011-02-15T21:44:04.010Z	mjackson	2011-02-15T21:44:04.010Z	\N
695	9	6	d6f3a279-ce86-4a12-8985-93b71afbb71d	11	51	4	59	mjackson	2011-02-15T21:46:47.847Z	admin	2011-06-14T10:28:57.221Z	\N
698	9	6	1373739a-2849-4647-9e97-7a4e05cc5841	11	51	4	59	mjackson	2011-02-15T21:50:49.999Z	admin	2011-06-14T10:28:57.304Z	\N
701	9	6	3c73aace-9f54-420d-a1c0-c54b6a116dcf	11	51	4	59	mjackson	2011-02-15T21:59:31.855Z	admin	2011-06-14T10:28:57.370Z	\N
705	5	6	53e8664e-e1fb-40d0-9104-019d57f06bee	11	197	4	59	mjackson	2011-02-15T22:05:46.889Z	mjackson	2011-02-15T22:05:46.889Z	\N
710	5	6	700ef542-9e3c-44dd-b7ea-027934010656	11	197	4	59	mjackson	2011-02-15T22:06:21.030Z	mjackson	2011-02-15T22:06:21.030Z	\N
734	1	6	d1808b0e-2501-4543-872d-830a21aa2f36	15	79	2	13	System	2024-02-19T07:51:28.992Z	System	2024-02-19T07:51:28.992Z	\N
735	1	6	a9e462ce-36d0-4889-b1a9-cf146278fcc7	15	75	2	13	System	2024-02-19T07:51:29.005Z	System	2024-02-19T07:51:29.005Z	\N
736	1	6	c2a6cd92-dfa3-4400-92c2-30659d4a2e05	15	75	2	13	System	2024-02-19T07:51:29.007Z	System	2024-02-19T07:51:29.007Z	\N
737	1	6	07e1d3e2-e6ab-48a4-8d2d-72a1b5a07d71	15	75	2	13	System	2024-02-19T07:51:29.009Z	System	2024-02-19T07:51:29.009Z	\N
738	1	6	458bea68-f4ca-44ea-ae4d-38c61047257b	15	75	2	13	System	2024-02-19T07:51:29.011Z	System	2024-02-19T07:51:29.011Z	\N
739	1	6	2711982f-00ff-4fb5-b8a4-581c332b73a3	15	75	2	13	System	2024-02-19T07:51:29.013Z	System	2024-02-19T07:51:29.013Z	\N
740	1	6	5ba76b9f-e675-4032-966b-dd25b1b3bb79	15	75	2	13	System	2024-02-19T07:51:29.014Z	System	2024-02-19T07:51:29.014Z	\N
741	1	6	17252959-42c3-4549-862d-6b6a07feae24	15	75	2	13	System	2024-02-19T07:51:29.016Z	System	2024-02-19T07:51:29.016Z	\N
742	1	6	aeda42fa-eda9-45ea-bcfe-0df9c78ac5d4	15	75	2	13	System	2024-02-19T07:51:29.017Z	System	2024-02-19T07:51:29.017Z	\N
743	1	6	36e3e92d-7240-443c-a0ad-e18d5d782fb3	15	75	2	13	System	2024-02-19T07:51:29.019Z	System	2024-02-19T07:51:29.019Z	\N
744	1	6	7fcfb08c-7e13-4e9e-a06b-2710be96cb37	15	75	2	13	System	2024-02-19T07:51:29.020Z	System	2024-02-19T07:51:29.020Z	\N
745	1	6	53be7b5e-ba62-4c69-aeba-8ac1caa10dc2	15	75	2	13	System	2024-02-19T07:51:29.022Z	System	2024-02-19T07:51:29.022Z	\N
746	1	6	9728d60f-ba5b-4ebf-81c0-41234263fc1e	15	75	2	13	System	2024-02-19T07:51:29.024Z	System	2024-02-19T07:51:29.024Z	\N
747	1	6	11b50d02-45a5-4579-b26a-41d3c89d4f11	15	75	2	13	System	2024-02-19T07:51:29.026Z	System	2024-02-19T07:51:29.026Z	\N
748	1	6	8932750e-b01f-4fcb-b5b3-85a04de3ce2d	15	75	2	13	System	2024-02-19T07:51:29.027Z	System	2024-02-19T07:51:29.027Z	\N
749	1	6	55fc1219-ab60-45aa-a9c0-aa82c5ad8bf8	15	75	2	13	System	2024-02-19T07:51:29.028Z	System	2024-02-19T07:51:29.028Z	\N
750	1	6	fea3449d-cf4d-45f3-9b5a-d376633e48f4	15	75	2	13	System	2024-02-19T07:51:29.029Z	System	2024-02-19T07:51:29.029Z	\N
751	1	6	4704fd90-9a0e-4c9a-b453-4fc73cf9efb5	15	75	2	13	System	2024-02-19T07:51:29.030Z	System	2024-02-19T07:51:29.030Z	\N
752	1	6	d2bd1c0f-69c4-4ef6-8ba4-26f3259b7f28	15	75	2	13	System	2024-02-19T07:51:29.031Z	System	2024-02-19T07:51:29.031Z	\N
753	1	6	de2c5326-0b0b-450b-948c-3946322ba23e	15	75	2	13	System	2024-02-19T07:51:29.032Z	System	2024-02-19T07:51:29.032Z	\N
754	1	6	fba8a890-723d-4989-895b-3f0b4f131c1b	15	75	2	13	System	2024-02-19T07:51:29.033Z	System	2024-02-19T07:51:29.033Z	\N
755	1	6	5bf0b30c-2321-43eb-b752-460d2d74dca6	15	79	2	13	System	2024-02-19T07:51:29.035Z	System	2024-02-19T07:51:29.035Z	\N
756	1	6	e8e68a29-043b-4af0-bc2b-45789c253db1	15	75	2	13	System	2024-02-19T07:51:29.039Z	System	2024-02-19T07:51:29.039Z	\N
757	1	6	5a387329-7279-4984-ae85-7db53b2feb74	15	75	2	13	System	2024-02-19T07:51:29.041Z	System	2024-02-19T07:51:29.041Z	\N
758	1	6	cadf3406-4e21-4e8c-83fc-1aabf0493113	15	75	2	13	System	2024-02-19T07:51:29.042Z	System	2024-02-19T07:51:29.042Z	\N
759	1	6	3531d2bc-0b7e-459e-ab7d-d4d1f5264724	15	75	2	13	System	2024-02-19T07:51:29.043Z	System	2024-02-19T07:51:29.043Z	\N
760	1	6	2bcb2311-17b6-4018-8734-2d4841ab4683	15	75	2	13	System	2024-02-19T07:51:29.045Z	System	2024-02-19T07:51:29.045Z	\N
761	1	6	2a911f1e-0361-402a-958f-2484477983fc	15	75	2	13	System	2024-02-19T07:51:29.046Z	System	2024-02-19T07:51:29.046Z	\N
762	1	6	11273d35-de09-461b-8ab2-6ecdd7544446	15	75	2	13	System	2024-02-19T07:51:29.047Z	System	2024-02-19T07:51:29.047Z	\N
763	1	6	2e8e61e7-7262-424d-8ea2-5c94841f01f2	15	75	2	13	System	2024-02-19T07:51:29.049Z	System	2024-02-19T07:51:29.049Z	\N
764	1	6	e6dfbbcd-f189-45d0-8aad-648efdce2aa0	15	75	2	13	System	2024-02-19T07:51:29.050Z	System	2024-02-19T07:51:29.050Z	\N
765	1	6	96876c34-9367-4686-a2ff-f8ec6561b328	15	75	2	13	System	2024-02-19T07:51:29.051Z	System	2024-02-19T07:51:29.051Z	\N
766	1	6	e697de7c-de38-40d0-bdee-397ef00f2a60	15	75	2	13	System	2024-02-19T07:51:29.053Z	System	2024-02-19T07:51:29.053Z	\N
767	1	6	ffb5c99f-1289-4070-9803-790af1947719	15	75	2	13	System	2024-02-19T07:51:29.054Z	System	2024-02-19T07:51:29.054Z	\N
768	1	6	51de7309-8367-463d-adf4-442a0ebfaaed	15	75	2	13	System	2024-02-19T07:51:29.055Z	System	2024-02-19T07:51:29.055Z	\N
769	1	6	c91c59e9-d84e-4a1d-8388-ab71dc8b1e2d	15	75	2	13	System	2024-02-19T07:51:29.056Z	System	2024-02-19T07:51:29.056Z	\N
770	1	6	5594ce31-2155-4f3d-99af-3307037eb844	15	75	2	13	System	2024-02-19T07:51:29.058Z	System	2024-02-19T07:51:29.058Z	\N
771	1	6	fff263e2-103d-4e0c-8af2-4cf8303b94ec	15	75	2	13	System	2024-02-19T07:51:29.059Z	System	2024-02-19T07:51:29.059Z	\N
772	1	6	f5f8679c-2e64-40bb-aa4b-ade9e6975583	15	75	2	13	System	2024-02-19T07:51:29.060Z	System	2024-02-19T07:51:29.060Z	\N
773	1	6	f87b5197-44ae-42d5-8675-95d97d3ab10c	15	75	2	13	System	2024-02-19T07:51:29.062Z	System	2024-02-19T07:51:29.062Z	\N
774	1	6	5e22a4e2-5ea1-40d1-8d4c-71e309a32a56	15	75	2	13	System	2024-02-19T07:51:29.063Z	System	2024-02-19T07:51:29.063Z	\N
775	1	6	f285ea6d-7ece-4681-bcf5-c8c2c38006f3	15	75	2	13	System	2024-02-19T07:51:29.064Z	System	2024-02-19T07:51:29.064Z	\N
776	1	6	b7578005-b821-40f4-8ac7-d13c811deda9	15	79	2	13	System	2024-02-19T07:51:29.067Z	System	2024-02-19T07:51:29.067Z	\N
777	1	6	f00eb977-ea10-41bc-b11a-c4c3b5a63670	15	75	2	13	System	2024-02-19T07:51:29.070Z	System	2024-02-19T07:51:29.070Z	\N
778	1	6	5b21fda3-0e7c-4fd6-8bc0-01ebcbf3bebe	15	75	2	13	System	2024-02-19T07:51:29.071Z	System	2024-02-19T07:51:29.071Z	\N
779	1	6	16040538-2c53-470e-81b7-3655e753c347	15	75	2	13	System	2024-02-19T07:51:29.073Z	System	2024-02-19T07:51:29.073Z	\N
780	1	6	176d84e4-a21d-4ee2-a14f-17d254992329	15	75	2	13	System	2024-02-19T07:51:29.074Z	System	2024-02-19T07:51:29.074Z	\N
781	1	6	07e725ae-c508-4149-b59c-24158c949e95	15	75	2	13	System	2024-02-19T07:51:29.075Z	System	2024-02-19T07:51:29.075Z	\N
782	1	6	df80638f-6dd8-4712-a793-d23767d0d6e9	15	75	2	13	System	2024-02-19T07:51:29.077Z	System	2024-02-19T07:51:29.077Z	\N
783	1	6	008b2dff-c2ea-4e6d-a354-1603a33f7d31	15	75	2	13	System	2024-02-19T07:51:29.078Z	System	2024-02-19T07:51:29.078Z	\N
784	1	6	9d8eb487-bb09-47f6-9296-d32c61dfdafd	15	75	2	13	System	2024-02-19T07:51:29.079Z	System	2024-02-19T07:51:29.079Z	\N
785	1	6	0f3479d8-d0e6-4ab1-b2f2-c318e7cdc1e9	15	75	2	13	System	2024-02-19T07:51:29.081Z	System	2024-02-19T07:51:29.081Z	\N
786	1	6	dfbdc52d-c206-4ae9-89fb-23c514788403	15	75	2	13	System	2024-02-19T07:51:29.082Z	System	2024-02-19T07:51:29.082Z	\N
787	1	6	a9ca385a-b6c3-4c68-93c0-407c59281149	15	75	2	13	System	2024-02-19T07:51:29.083Z	System	2024-02-19T07:51:29.083Z	\N
788	1	6	5eead6ab-477e-46a6-abc9-1d3fdf453642	15	79	2	13	System	2024-02-19T07:51:29.085Z	System	2024-02-19T07:51:29.085Z	\N
789	1	6	f864b354-8032-4de6-b407-40407dd28719	15	75	2	13	System	2024-02-19T07:51:29.088Z	System	2024-02-19T07:51:29.088Z	\N
790	1	6	2206fdff-5958-4ddf-993a-9f8fa8187934	15	75	2	13	System	2024-02-19T07:51:29.090Z	System	2024-02-19T07:51:29.090Z	\N
791	1	6	ee3c2a3c-5875-4cae-98e6-5e2e98e89301	15	75	2	13	System	2024-02-19T07:51:29.091Z	System	2024-02-19T07:51:29.091Z	\N
792	1	6	dc0db29e-0218-405d-b77c-c63d44dfec08	15	75	2	13	System	2024-02-19T07:51:29.092Z	System	2024-02-19T07:51:29.092Z	\N
793	1	6	d798784c-06af-435d-81d2-62b8601c5369	15	75	2	13	System	2024-02-19T07:51:29.094Z	System	2024-02-19T07:51:29.094Z	\N
794	1	6	bdd89390-3604-43e4-b39f-2e1b404516ae	15	75	2	13	System	2024-02-19T07:51:29.095Z	System	2024-02-19T07:51:29.095Z	\N
795	1	6	fe7c4fff-9702-4b8b-8967-33c2356b6fb9	15	75	2	13	System	2024-02-19T07:51:29.096Z	System	2024-02-19T07:51:29.096Z	\N
796	1	6	1e64c8bc-70cf-4a01-90d8-25eedf671332	15	75	2	13	System	2024-02-19T07:51:29.098Z	System	2024-02-19T07:51:29.098Z	\N
797	1	6	c48f9569-f0c9-4055-996c-aeff16492774	15	75	2	13	System	2024-02-19T07:51:29.099Z	System	2024-02-19T07:51:29.099Z	\N
798	1	6	578f2c19-3c47-4011-8caf-35ae03c6cf2a	15	75	2	13	System	2024-02-19T07:51:29.100Z	System	2024-02-19T07:51:29.100Z	\N
799	1	6	7b5423d8-d243-4496-912b-8227871f3869	15	75	2	13	System	2024-02-19T07:51:29.101Z	System	2024-02-19T07:51:29.101Z	\N
800	1	6	8b3a1159-40a4-46eb-a7a9-620b4ed91a88	15	75	2	13	System	2024-02-19T07:51:29.103Z	System	2024-02-19T07:51:29.103Z	\N
801	1	6	99c7bf28-e2b4-4bb9-b1a6-a26af498f8c2	15	75	2	13	System	2024-02-19T07:51:29.104Z	System	2024-02-19T07:51:29.104Z	\N
802	1	6	3a3af2b8-57ab-4246-be3a-8145a68cfad2	15	75	2	13	System	2024-02-19T07:51:29.105Z	System	2024-02-19T07:51:29.105Z	\N
803	1	6	a0ce27ff-558a-4579-b008-43918a455a2e	15	75	2	13	System	2024-02-19T07:51:29.106Z	System	2024-02-19T07:51:29.106Z	\N
804	1	6	3ddded25-88d1-47fb-98af-dac9a7eb1769	15	75	2	13	System	2024-02-19T07:51:29.108Z	System	2024-02-19T07:51:29.108Z	\N
805	1	6	f93df728-9a1a-41c3-8ff0-a04f5ba8ea03	15	75	2	13	System	2024-02-19T07:51:29.109Z	System	2024-02-19T07:51:29.109Z	\N
806	1	6	f30f5c8f-3475-4567-b2cd-5f71271a0702	15	75	2	13	System	2024-02-19T07:51:29.110Z	System	2024-02-19T07:51:29.110Z	\N
807	1	6	d687cc7d-c831-4496-a27b-c8c724438b94	15	75	2	13	System	2024-02-19T07:51:29.112Z	System	2024-02-19T07:51:29.112Z	\N
808	1	6	6d1244fa-ce45-417b-8510-e32943d135fd	15	75	2	13	System	2024-02-19T07:51:29.113Z	System	2024-02-19T07:51:29.113Z	\N
809	1	6	8ab35f60-95d0-4067-88d7-7e5d89fab6af	15	79	2	13	System	2024-02-19T07:51:29.115Z	System	2024-02-19T07:51:29.115Z	\N
810	1	6	31413b28-19c9-42e3-9fc3-11080991250c	15	75	2	13	System	2024-02-19T07:51:29.118Z	System	2024-02-19T07:51:29.118Z	\N
811	1	6	f6eceefa-bdc4-4036-a7eb-3e4160299af1	15	75	2	13	System	2024-02-19T07:51:29.119Z	System	2024-02-19T07:51:29.119Z	\N
812	1	6	0e9a0e5e-365d-4774-bf04-0d3b8eefac1c	15	75	2	13	System	2024-02-19T07:51:29.121Z	System	2024-02-19T07:51:29.121Z	\N
813	1	6	0eec3cf7-a522-4e24-941b-d8b614f0986c	15	75	2	13	System	2024-02-19T07:51:29.122Z	System	2024-02-19T07:51:29.122Z	\N
814	1	6	9d9698b8-13f2-42b8-92d3-55257e10dd55	15	75	2	13	System	2024-02-19T07:51:29.123Z	System	2024-02-19T07:51:29.123Z	\N
815	1	6	5b88d0eb-6326-439e-83ec-49218bdd138a	15	75	2	13	System	2024-02-19T07:51:29.125Z	System	2024-02-19T07:51:29.125Z	\N
816	1	6	1b4dff8d-a9a9-4f7d-9dea-bac55533439d	15	75	2	13	System	2024-02-19T07:51:29.126Z	System	2024-02-19T07:51:29.126Z	\N
817	1	6	6cac77cd-262b-43c2-b6aa-440aeaa8b6c9	15	75	2	13	System	2024-02-19T07:51:29.127Z	System	2024-02-19T07:51:29.127Z	\N
818	1	6	0d4231e6-0399-4311-8cf2-56644835299a	15	75	2	13	System	2024-02-19T07:51:29.128Z	System	2024-02-19T07:51:29.128Z	\N
819	1	6	061c00e3-e085-4148-9b10-2a9cef15c8e2	15	75	2	13	System	2024-02-19T07:51:29.130Z	System	2024-02-19T07:51:29.130Z	\N
820	1	6	018ebd2c-6455-4949-832b-c3110d584cf1	15	75	2	13	System	2024-02-19T07:51:29.131Z	System	2024-02-19T07:51:29.131Z	\N
821	1	6	3a615d36-7ff0-4dff-a2a0-020ef224bc33	15	75	2	13	System	2024-02-19T07:51:29.132Z	System	2024-02-19T07:51:29.132Z	\N
822	1	6	d18da2c6-095b-4389-9998-d29ea10ff445	15	75	2	13	System	2024-02-19T07:51:29.134Z	System	2024-02-19T07:51:29.134Z	\N
823	1	6	9081e80a-a442-4aec-8a89-7d76ba13bf81	15	75	2	13	System	2024-02-19T07:51:29.135Z	System	2024-02-19T07:51:29.135Z	\N
824	1	6	b2404f0e-e87d-4671-a2dc-faeab52d9ca3	15	75	2	13	System	2024-02-19T07:51:29.136Z	System	2024-02-19T07:51:29.136Z	\N
825	1	6	56b74ed8-1ebe-482b-b292-aa90357a1ff0	15	75	2	13	System	2024-02-19T07:51:29.137Z	System	2024-02-19T07:51:29.137Z	\N
826	1	6	3b090b54-cd80-4c61-a614-3e5d892ec1ac	15	75	2	13	System	2024-02-19T07:51:29.139Z	System	2024-02-19T07:51:29.139Z	\N
827	1	6	45106a8c-aee1-4763-a47e-1049c6f3414a	15	75	2	13	System	2024-02-19T07:51:29.140Z	System	2024-02-19T07:51:29.140Z	\N
828	1	6	4905d867-79b7-4f23-96f6-0ccec55e2773	15	75	2	13	System	2024-02-19T07:51:29.141Z	System	2024-02-19T07:51:29.141Z	\N
829	1	6	288f9ba8-0984-4e37-bb13-9ec00528e349	15	75	2	13	System	2024-02-19T07:51:29.142Z	System	2024-02-19T07:51:29.142Z	\N
830	1	6	8dc9d683-e51f-4a39-a2f2-6c2a5d529aab	15	79	2	13	System	2024-02-19T07:51:29.144Z	System	2024-02-19T07:51:29.144Z	\N
831	1	6	bb171be9-d95b-4d00-801b-1b567ac557dd	15	75	2	13	System	2024-02-19T07:51:29.146Z	System	2024-02-19T07:51:29.146Z	\N
832	1	6	fcd90e97-2a22-4ab2-8cf0-621b89b23611	15	75	2	13	System	2024-02-19T07:51:29.148Z	System	2024-02-19T07:51:29.148Z	\N
833	1	6	e3c0b4e3-00e5-4bd2-81d7-600cc2cc912a	15	75	2	13	System	2024-02-19T07:51:29.149Z	System	2024-02-19T07:51:29.149Z	\N
834	1	6	89d29911-6383-40f9-9635-740e9c81308c	15	75	2	13	System	2024-02-19T07:51:29.150Z	System	2024-02-19T07:51:29.150Z	\N
835	1	6	da972d85-4e99-4065-b89d-378d028b05ce	15	75	2	13	System	2024-02-19T07:51:29.151Z	System	2024-02-19T07:51:29.151Z	\N
836	1	6	0423649b-ee17-4f01-af92-599abdcc4613	15	75	2	13	System	2024-02-19T07:51:29.153Z	System	2024-02-19T07:51:29.153Z	\N
837	1	6	c4c48435-4bf1-43e3-bf0f-4be9bdbeaa2d	15	75	2	13	System	2024-02-19T07:51:29.154Z	System	2024-02-19T07:51:29.154Z	\N
838	1	6	41aad875-db29-4c9b-b275-668faf176964	15	75	2	13	System	2024-02-19T07:51:29.155Z	System	2024-02-19T07:51:29.155Z	\N
839	1	6	31d8a19d-546a-4ca5-ae7a-348633e53106	15	75	2	13	System	2024-02-19T07:51:29.157Z	System	2024-02-19T07:51:29.157Z	\N
840	1	6	466fbcf3-655e-4c30-b30e-0ba24fcbcfe4	15	75	2	13	System	2024-02-19T07:51:29.158Z	System	2024-02-19T07:51:29.158Z	\N
841	1	6	6fe591cc-5b53-4f2c-abe2-e2043ebd95f4	15	75	2	13	System	2024-02-19T07:51:29.159Z	System	2024-02-19T07:51:29.159Z	\N
842	1	6	bd4edff4-e23f-43e4-848f-365a956def93	15	75	2	13	System	2024-02-19T07:51:29.161Z	System	2024-02-19T07:51:29.161Z	\N
843	1	6	e91dfa63-1121-4491-9f46-b91304e7b2cd	15	79	2	13	System	2024-02-19T07:51:29.162Z	System	2024-02-19T07:51:29.162Z	\N
844	1	6	43d9e577-62ff-4e5c-82ce-ed5ef2dcbcae	15	75	2	13	System	2024-02-19T07:51:29.204Z	System	2024-02-19T07:51:29.204Z	\N
845	1	6	58723e4c-5636-45a4-87ae-238ca7ab4751	15	75	2	13	System	2024-02-19T07:51:29.206Z	System	2024-02-19T07:51:29.206Z	\N
846	1	6	fbe2cc68-ef77-4af3-ba84-514f9b6e5ad2	15	75	2	13	System	2024-02-19T07:51:29.207Z	System	2024-02-19T07:51:29.207Z	\N
847	1	6	8d1a51c7-5622-447a-96da-500d03802979	15	75	2	13	System	2024-02-19T07:51:29.208Z	System	2024-02-19T07:51:29.208Z	\N
848	1	6	12645da0-2cd7-4f64-8bd2-9ac559582895	15	75	2	13	System	2024-02-19T07:51:29.210Z	System	2024-02-19T07:51:29.210Z	\N
849	1	6	64baa409-de2f-4aaf-ba8d-08b6768a975b	15	75	2	13	System	2024-02-19T07:51:29.211Z	System	2024-02-19T07:51:29.211Z	\N
850	1	6	5fd3af98-f74f-4501-b955-ae841162eae1	15	75	2	13	System	2024-02-19T07:51:29.212Z	System	2024-02-19T07:51:29.212Z	\N
851	1	6	1b3d8dce-106c-401c-a020-ef7fda906eb3	15	75	2	13	System	2024-02-19T07:51:29.213Z	System	2024-02-19T07:51:29.213Z	\N
852	1	6	02915ad6-7f83-4b4f-80bb-1b77c39e9da4	15	75	2	13	System	2024-02-19T07:51:29.215Z	System	2024-02-19T07:51:29.215Z	\N
853	1	6	cbc37db8-5ec8-49b9-a7e7-b0d573c089bb	15	75	2	13	System	2024-02-19T07:51:29.216Z	System	2024-02-19T07:51:29.216Z	\N
854	1	6	7d094d8d-815c-4926-ab13-f506ded2387a	15	75	2	13	System	2024-02-19T07:51:29.217Z	System	2024-02-19T07:51:29.217Z	\N
855	1	6	7f5d6a7f-696a-4bf3-bb52-39e914ddc387	15	75	2	13	System	2024-02-19T07:51:29.218Z	System	2024-02-19T07:51:29.218Z	\N
856	1	6	1988e92c-f73f-4848-af3a-e93b7403f728	15	75	2	13	System	2024-02-19T07:51:29.220Z	System	2024-02-19T07:51:29.220Z	\N
857	1	6	1aa5db58-0962-4692-86d2-6ca96ac7c773	15	75	2	13	System	2024-02-19T07:51:29.221Z	System	2024-02-19T07:51:29.221Z	\N
858	1	6	86b73504-e014-4109-b2b8-cc8b3661e73b	15	75	2	13	System	2024-02-19T07:51:29.222Z	System	2024-02-19T07:51:29.222Z	\N
859	1	6	6579ad4d-611a-4534-894f-51d658df1f7b	15	75	2	13	System	2024-02-19T07:51:29.224Z	System	2024-02-19T07:51:29.224Z	\N
860	1	6	77f38a3e-9a10-413f-afd8-28e2e1b2346d	15	75	2	13	System	2024-02-19T07:51:29.225Z	System	2024-02-19T07:51:29.225Z	\N
861	1	6	95a4b1fe-f5d4-4233-a0ee-fcf85c209de8	15	75	2	13	System	2024-02-19T07:51:29.226Z	System	2024-02-19T07:51:29.226Z	\N
862	1	6	c978a703-0ba7-4660-8733-124836d031eb	15	75	2	13	System	2024-02-19T07:51:29.228Z	System	2024-02-19T07:51:29.228Z	\N
863	1	6	8dfbbd6c-46c9-4ec0-b63f-aeb7c40e3643	15	75	2	13	System	2024-02-19T07:51:29.229Z	System	2024-02-19T07:51:29.229Z	\N
864	1	6	solr_facets_root_space	17	230	2	13	System	2024-02-19T07:51:29.463Z	System	2024-02-19T07:51:29.463Z	\N
\.


--
-- Data for Name: alf_node_aspects; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_node_aspects (node_id, qname_id) FROM stdin;
1	2
5	2
9	2
9	22
10	2
10	23
11	2
12	2
13	25
13	30
13	31
14	25
14	30
14	31
15	25
15	30
15	31
16	25
16	30
16	31
17	25
17	30
17	31
18	25
18	30
18	31
19	25
19	30
19	31
20	25
20	30
20	31
21	25
21	30
21	31
22	25
22	30
22	31
23	25
23	30
23	31
24	25
24	30
24	31
25	25
25	30
25	31
26	25
26	30
26	31
27	25
27	30
27	31
28	25
28	30
28	31
29	25
29	30
29	31
32	44
35	25
35	2
36	25
37	25
38	25
39	25
40	25
41	25
42	25
43	25
44	25
45	25
46	25
47	25
48	25
49	25
50	25
51	25
52	25
53	25
54	25
55	25
56	25
57	25
58	25
59	25
60	25
61	25
62	25
63	25
64	25
65	25
66	25
67	25
68	25
69	25
70	25
71	25
72	25
73	25
74	25
75	25
76	25
77	25
78	25
79	25
80	25
81	25
82	25
83	25
84	25
85	25
86	25
87	25
88	25
89	25
90	25
91	25
92	25
93	25
94	25
95	25
96	25
97	25
98	25
99	25
100	25
101	25
102	25
103	25
104	25
105	25
106	25
107	25
108	25
109	25
110	25
111	25
112	25
113	25
114	25
115	25
116	25
117	25
118	25
119	25
120	25
121	25
122	25
123	25
124	25
125	25
126	25
127	25
128	25
129	25
130	25
131	25
132	25
133	25
134	25
135	25
136	25
137	25
138	25
139	25
140	25
141	25
142	25
143	25
144	25
145	25
146	25
147	25
148	25
149	25
150	25
151	25
152	25
153	25
154	25
155	25
156	25
157	25
158	25
159	25
160	25
161	25
162	25
163	25
164	25
165	25
166	25
167	25
168	25
169	25
170	25
171	25
172	25
173	25
174	25
175	25
176	25
177	25
178	25
179	25
180	25
181	25
182	25
183	25
184	25
185	25
186	25
187	25
188	25
189	25
190	25
191	25
192	25
193	25
194	25
195	25
196	25
197	25
198	25
199	25
200	25
201	25
202	25
203	25
204	25
205	25
206	25
207	25
208	25
209	25
210	25
211	25
212	25
213	25
214	25
215	25
216	25
217	25
218	25
219	25
220	25
221	25
222	25
223	25
224	25
225	25
226	25
227	25
228	25
229	25
230	25
231	25
232	25
233	25
234	25
235	25
236	25
237	25
238	25
239	25
240	25
241	25
242	25
243	25
244	25
245	25
246	25
247	25
248	25
249	25
250	25
251	25
252	25
253	25
254	25
255	25
256	25
257	25
258	25
259	25
260	25
261	25
262	25
263	25
264	25
265	25
266	25
267	25
268	25
269	25
270	25
271	25
272	25
273	25
274	25
275	25
276	25
277	25
278	25
279	25
280	25
281	25
282	25
283	25
284	25
285	25
286	25
287	25
288	25
289	25
290	25
291	25
292	25
293	25
294	25
295	25
296	25
297	25
298	25
299	25
300	25
301	25
302	25
303	25
304	25
305	25
306	25
307	25
308	25
309	25
310	25
311	25
312	25
313	25
314	25
315	25
316	25
317	25
318	25
319	25
320	25
321	25
322	25
323	25
324	25
325	25
326	25
327	25
328	25
329	25
330	25
331	25
332	25
333	25
334	25
335	25
336	25
337	25
338	25
339	25
340	25
341	25
342	25
343	25
344	25
345	25
346	25
347	25
348	25
349	25
350	25
351	25
352	25
353	25
354	25
355	25
356	25
357	25
358	25
359	25
360	25
361	25
362	25
363	25
364	25
365	25
366	25
367	25
368	25
369	25
371	25
371	31
372	25
372	31
373	25
373	31
374	25
374	31
375	25
375	31
376	25
376	31
377	25
377	30
377	31
378	25
378	31
379	25
379	31
380	25
380	31
381	25
381	31
382	25
382	30
382	53
383	25
383	30
383	53
384	25
384	30
384	53
385	25
385	30
385	53
386	25
386	30
386	53
387	25
387	30
387	53
388	25
388	30
388	53
389	25
389	30
389	53
390	25
390	30
390	53
391	25
391	30
391	53
392	25
392	53
392	30
392	54
393	25
393	30
393	31
394	25
394	30
394	53
395	25
395	30
395	31
396	25
396	30
396	53
397	25
397	30
397	31
398	25
398	53
398	30
398	54
399	25
399	53
399	30
399	54
400	25
400	53
400	30
400	54
401	25
401	53
401	30
401	54
402	25
402	53
402	30
402	54
403	25
403	53
403	30
403	54
404	25
404	53
404	30
404	54
405	25
405	30
406	25
406	30
406	53
407	25
407	30
407	53
408	25
408	30
408	53
409	25
409	30
409	53
410	25
410	30
410	53
411	25
411	30
411	53
412	25
412	30
412	53
413	25
413	30
413	31
414	25
414	30
414	53
415	25
415	30
415	53
416	25
416	30
416	53
417	25
417	30
417	53
418	25
418	30
418	53
419	25
419	30
419	53
420	25
420	30
420	53
421	25
421	30
421	53
422	25
422	53
422	30
422	54
423	25
423	53
423	30
423	54
424	25
424	53
424	30
424	54
425	25
425	53
425	30
425	54
426	25
426	53
426	30
426	54
427	25
427	30
427	53
428	25
428	30
428	31
429	25
429	53
429	30
429	54
430	25
430	53
430	30
430	54
431	25
431	53
431	30
431	54
432	25
432	53
432	30
432	54
433	25
433	30
433	31
434	25
434	30
434	31
435	25
435	30
435	31
436	25
436	53
436	30
436	54
437	25
437	53
437	30
437	54
438	25
438	53
438	30
438	54
439	25
439	53
439	30
439	54
440	25
440	53
440	30
440	54
441	25
441	53
441	30
441	54
442	25
442	53
442	30
442	54
443	25
443	53
443	30
443	54
444	25
444	53
444	30
444	54
445	25
445	53
445	30
445	54
446	25
446	53
446	30
446	54
447	25
447	53
447	30
447	54
448	25
448	53
448	30
448	54
449	25
449	53
449	30
449	54
450	25
450	53
450	30
450	54
451	25
451	53
451	30
451	54
452	25
452	30
452	31
453	25
453	53
453	30
453	54
428	56
452	56
454	25
454	30
454	31
454	58
455	25
456	25
456	30
457	25
458	25
459	25
460	25
461	25
462	25
462	30
462	31
463	25
463	30
463	31
464	25
464	30
464	31
464	58
465	25
466	25
466	30
467	25
468	25
469	25
470	25
471	25
472	25
473	25
473	30
473	31
474	25
474	30
474	85
474	88
475	25
475	88
475	30
475	85
476	25
476	88
476	30
476	85
485	25
486	25
32	95
487	30
488	30
489	25
489	30
489	31
490	25
490	30
490	31
491	25
491	53
491	30
491	54
492	25
492	53
492	30
492	54
493	25
493	30
493	31
494	25
494	30
494	31
495	25
495	30
495	31
496	25
496	30
496	31
497	25
497	30
497	31
498	25
498	30
499	25
499	30
495	58
500	25
501	25
501	30
502	25
502	80
503	25
504	25
505	25
506	25
507	25
508	25
508	30
509	25
509	30
509	53
510	25
510	30
510	53
511	25
511	30
511	53
512	25
512	30
512	53
513	25
513	30
513	53
514	25
514	30
514	53
515	25
515	30
515	53
516	25
516	30
516	53
517	25
517	30
517	53
518	25
518	30
518	53
519	25
519	30
519	53
520	25
520	30
520	53
521	25
521	30
521	53
522	25
522	30
522	53
523	25
523	30
523	53
524	25
524	30
524	53
525	25
525	30
525	53
526	25
526	30
526	53
527	25
527	30
527	53
528	25
528	30
528	53
529	25
529	30
529	53
530	25
530	30
530	53
531	25
531	30
531	53
532	25
532	30
532	53
533	25
533	30
533	53
534	25
534	30
534	53
535	25
535	30
535	53
536	25
536	30
536	53
537	25
537	30
537	53
538	25
538	53
538	30
538	54
539	25
539	53
539	30
539	54
540	25
540	53
540	30
540	54
541	25
541	53
541	30
541	54
542	25
542	53
542	30
542	54
543	25
543	53
543	30
543	54
544	25
544	53
544	30
544	54
545	25
545	53
545	30
545	54
546	25
546	53
546	30
546	54
547	25
547	53
547	30
547	54
548	25
548	53
548	30
548	54
549	25
549	53
549	30
549	54
550	25
550	53
550	30
550	54
551	25
551	53
551	30
551	54
552	25
552	53
552	30
552	54
553	25
553	53
553	30
553	54
554	25
554	53
554	30
554	54
555	25
555	53
555	30
555	54
556	25
556	53
556	30
556	54
557	25
557	53
557	30
557	54
558	25
558	53
558	30
558	54
559	25
559	53
559	30
559	54
560	25
560	53
560	30
560	54
561	25
561	53
561	30
561	54
562	25
562	53
562	30
562	54
563	25
563	53
563	30
563	54
564	25
564	53
564	30
564	54
565	25
565	53
565	30
565	54
566	25
566	53
566	30
566	54
567	25
567	53
567	30
567	54
568	25
568	53
568	30
568	54
569	25
569	53
569	30
569	54
570	25
570	53
570	30
570	54
571	25
571	53
571	30
571	54
572	25
572	53
572	30
572	54
573	25
573	53
573	30
573	54
574	25
574	53
574	30
574	54
575	25
575	53
575	30
575	54
576	25
576	53
576	30
576	54
577	25
577	53
577	30
577	54
578	25
578	53
578	30
578	54
579	25
579	53
579	30
579	54
580	25
580	53
580	30
580	54
581	25
581	53
581	30
581	54
582	30
583	25
583	30
583	53
583	54
7	88
584	25
584	53
584	30
584	54
865	25
865	88
865	44
866	25
866	44
587	25
588	95
589	95
590	95
591	95
594	44
594	133
594	134
595	25
595	139
595	140
596	25
596	88
596	145
597	44
597	133
597	134
598	25
598	139
598	140
599	25
599	88
599	145
594	95
597	95
866	88
601	25
601	44
601	109
601	30
601	110
601	111
602	25
602	44
602	153
602	30
602	111
603	25
603	30
603	44
603	154
604	25
604	30
604	44
605	25
605	161
605	162
605	44
605	30
605	54
605	140
605	163
606	25
607	25
607	166
607	140
607	161
607	30
607	44
607	162
607	163
607	54
608	25
608	88
608	44
608	145
609	25
609	88
609	44
609	145
610	25
610	30
610	44
611	25
611	44
611	193
611	30
611	140
611	54
612	25
612	88
612	44
612	145
613	25
613	44
613	193
613	30
613	140
613	54
614	25
614	88
614	44
614	145
615	25
615	44
615	193
615	30
615	140
615	54
616	25
616	88
616	44
616	145
617	25
617	88
617	44
617	145
618	25
618	44
618	193
618	30
618	140
618	54
619	25
619	88
619	44
619	145
620	25
620	44
620	193
620	30
620	140
620	54
621	25
621	88
621	44
621	145
622	25
622	44
622	193
622	30
622	140
622	54
623	25
623	88
623	44
623	145
624	25
624	44
624	193
624	30
624	140
624	54
625	25
625	88
625	44
625	145
626	25
626	44
626	193
626	30
626	140
626	54
627	25
627	88
627	44
627	145
628	25
628	44
628	193
628	30
628	140
628	54
629	25
629	88
629	44
629	145
630	25
630	44
630	193
630	30
630	140
630	54
631	25
631	88
631	44
631	145
632	25
632	44
632	193
632	30
632	140
632	54
633	25
633	88
633	44
633	145
634	25
634	30
634	44
635	25
635	44
635	193
635	30
635	162
635	140
635	163
635	54
636	25
636	88
636	44
636	145
637	25
637	44
637	193
637	30
637	162
637	140
637	163
637	54
638	25
638	88
638	44
638	145
639	25
639	30
639	44
640	25
640	44
640	193
640	30
640	140
640	54
641	25
641	88
641	44
641	145
642	25
642	44
642	193
642	30
642	140
642	54
643	25
643	88
643	44
643	145
644	25
644	88
644	44
644	145
645	25
645	44
645	193
645	30
645	162
645	140
645	163
645	54
646	25
646	88
646	44
646	145
647	25
647	88
647	44
647	145
648	25
648	30
648	44
649	25
649	44
649	30
649	54
649	154
650	25
650	30
650	44
650	154
651	25
651	30
651	44
652	25
652	44
652	193
652	30
652	140
652	54
653	25
653	88
653	44
653	145
654	25
654	44
654	193
654	30
654	140
654	54
655	25
655	88
655	44
655	145
656	25
656	44
656	30
656	54
656	140
656	194
657	25
657	88
657	44
657	145
658	25
658	88
658	44
658	145
659	25
659	44
659	31
660	25
660	44
661	25
661	44
661	30
662	25
662	30
662	44
663	25
663	44
663	30
663	140
664	25
664	88
664	44
664	145
665	25
665	88
665	44
665	145
666	25
666	44
666	30
666	140
667	25
667	88
667	44
667	145
668	25
668	88
668	44
668	145
669	25
669	44
669	30
669	140
670	25
670	88
670	44
670	145
671	25
671	44
671	145
672	25
672	30
672	44
673	25
673	44
673	30
673	140
674	25
674	88
674	44
674	145
675	25
675	88
675	44
675	145
676	25
676	44
676	30
676	140
677	25
677	88
677	44
677	145
678	25
678	88
678	44
678	145
679	25
679	44
679	153
679	111
680	25
680	44
680	154
681	25
681	44
681	154
682	25
682	44
682	153
682	30
682	111
683	25
683	30
683	44
684	25
684	44
684	30
684	211
685	25
685	44
685	30
685	211
686	25
686	44
686	30
686	211
687	25
687	44
687	30
687	211
688	25
688	30
688	44
689	25
689	44
690	25
690	44
691	25
691	44
692	25
692	44
693	25
693	44
694	25
694	44
694	153
694	111
695	25
695	44
695	161
695	30
695	154
696	25
697	25
697	166
697	161
697	44
697	30
697	154
698	25
698	44
698	161
698	30
698	154
699	25
700	25
700	166
700	161
700	44
700	30
700	154
701	25
701	44
701	161
701	30
701	154
702	25
703	25
703	166
703	161
703	44
703	30
703	154
704	25
704	44
704	153
704	111
705	25
705	44
705	154
706	25
706	44
706	30
706	219
707	25
707	44
707	30
707	219
707	220
708	25
708	44
708	30
708	219
708	220
709	25
709	44
709	30
709	219
709	220
710	25
710	44
710	154
711	25
711	44
711	30
711	219
712	25
712	44
712	30
712	219
712	220
713	25
713	88
713	85
714	25
714	88
715	25
715	88
716	25
716	88
717	25
717	88
718	25
718	88
719	25
719	88
720	25
720	88
721	25
721	88
722	25
722	88
723	25
723	88
724	25
724	88
725	25
725	88
726	25
726	88
727	25
727	88
597	226
594	226
734	25
735	25
736	25
737	25
738	25
739	25
740	25
741	25
742	25
743	25
744	25
745	25
746	25
747	25
748	25
749	25
750	25
751	25
752	25
753	25
754	25
755	25
756	25
757	25
758	25
759	25
760	25
761	25
762	25
763	25
764	25
765	25
766	25
767	25
768	25
769	25
770	25
771	25
772	25
773	25
774	25
775	25
776	25
777	25
778	25
779	25
780	25
781	25
782	25
783	25
784	25
785	25
786	25
787	25
788	25
789	25
790	25
791	25
792	25
793	25
794	25
795	25
796	25
797	25
798	25
799	25
800	25
801	25
802	25
803	25
804	25
805	25
806	25
807	25
808	25
809	25
810	25
811	25
812	25
813	25
814	25
815	25
816	25
817	25
818	25
819	25
820	25
821	25
822	25
823	25
824	25
825	25
826	25
827	25
828	25
829	25
830	25
831	25
832	25
833	25
834	25
835	25
836	25
837	25
838	25
839	25
840	25
841	25
842	25
843	25
844	25
845	25
846	25
847	25
848	25
849	25
850	25
851	25
852	25
853	25
854	25
855	25
856	25
857	25
858	25
859	25
860	25
861	25
862	25
863	25
864	25
864	88
864	30
867	25
867	44
867	88
868	25
868	44
868	88
869	25
869	44
869	88
870	25
870	44
870	88
871	25
871	44
871	88
872	25
872	88
873	25
873	88
874	25
874	88
872	44
873	44
874	44
875	25
875	44
875	88
876	25
876	30
877	25
878	25
879	25
880	25
880	30
880	53
880	140
882	25
882	88
882	145
880	139
884	25
884	88
884	234
884	145
885	25
885	161
886	25
887	25
887	166
887	161
888	25
888	88
885	140
888	234
888	145
885	139
885	54
885	30
889	25
889	161
890	25
891	25
891	166
891	161
892	25
892	161
893	25
894	25
894	166
894	161
889	54
889	30
892	54
892	30
895	25
895	88
892	140
895	234
895	145
892	139
896	25
896	88
889	140
896	234
896	145
889	139
880	237
885	237
889	237
892	237
880	154
885	154
889	154
892	154
\.


--
-- Data for Name: alf_node_assoc; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_node_assoc (id, version, source_node_id, target_node_id, type_qname_id, assoc_index) FROM stdin;
1	1	594	595	146	1
2	1	597	598	146	1
3	1	606	607	177	1
4	1	696	697	177	1
5	1	699	700	177	1
6	1	702	703	177	1
7	1	709	708	223	1
8	1	708	707	223	1
9	1	707	706	223	1
10	1	690	656	224	1
11	1	692	605	224	1
12	1	685	656	225	1
13	1	689	673	224	1
14	1	693	649	224	1
15	1	712	711	223	1
16	1	886	887	177	1
17	1	890	891	177	1
18	1	893	894	177	1
\.


--
-- Data for Name: alf_node_properties; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_node_properties (node_id, actual_type_n, persisted_type_n, boolean_value, long_value, float_value, double_value, string_value, serializable_value, qname_id, list_index, locale_id) FROM stdin;
4	1	1	t	0	0	0	\N	\N	6	-1	1
4	1	1	f	0	0	0	\N	\N	7	-1	1
4	1	1	f	0	0	0	\N	\N	8	-1	1
4	6	6	f	0	0	0	admin	\N	9	-1	1
4	1	1	f	0	0	0	\N	\N	11	-1	1
6	6	6	f	0	0	0		\N	17	-1	1
6	2	3	f	19100	0	0	\N	\N	18	-1	1
6	6	6	f	0	0	0	1	\N	19	-1	1
6	6	6	f	0	0	0	23	\N	13	-1	1
6	6	6	f	0	0	0	r3a759f72-blocal	\N	14	-1	1
6	6	6	f	0	0	0	1	\N	15	-1	1
6	6	6	f	0	0	0		\N	16	-1	1
7	6	6	f	0	0	0		\N	17	-1	1
7	2	3	f	19100	0	0	\N	\N	18	-1	1
7	6	6	f	0	0	0	1	\N	19	-1	1
7	6	6	f	0	0	0	23	\N	13	-1	1
7	6	6	f	0	0	0	r3a759f72-blocal	\N	14	-1	1
7	6	6	f	0	0	0	1	\N	15	-1	1
4	6	6	f	0	0	0	596f1ddf-41e1-4c4a-8267-feffe5d334bb	\N	113	-1	1
7	21	3	f	4	0	0	\N	\N	20	-1	1
13	6	6	f	0	0	0	Company Home	\N	26	-1	1
13	6	6	f	0	0	0	The company root space	\N	27	-1	2
13	6	6	f	0	0	0	Company Home	\N	28	-1	2
13	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
14	6	6	f	0	0	0	Data Dictionary	\N	26	-1	1
14	6	6	f	0	0	0	User managed definitions	\N	27	-1	2
14	6	6	f	0	0	0	Data Dictionary	\N	28	-1	2
14	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
15	6	6	f	0	0	0	Space Templates	\N	26	-1	1
15	6	6	f	0	0	0	Space folder templates	\N	27	-1	2
15	6	6	f	0	0	0	Space Templates	\N	28	-1	2
15	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
16	6	6	f	0	0	0	Presentation Templates	\N	26	-1	1
16	6	6	f	0	0	0	Presentation templates	\N	27	-1	2
16	6	6	f	0	0	0	Presentation Templates	\N	28	-1	2
16	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
17	6	6	f	0	0	0	Email Templates	\N	26	-1	1
17	6	6	f	0	0	0	Email templates	\N	27	-1	2
17	6	6	f	0	0	0	Email Templates	\N	28	-1	2
17	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
18	6	6	f	0	0	0	invite	\N	26	-1	1
18	6	6	f	0	0	0	Invite email templates	\N	27	-1	2
18	6	6	f	0	0	0	invite	\N	28	-1	2
18	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
19	6	6	f	0	0	0	RSS Templates	\N	26	-1	1
19	6	6	f	0	0	0	RSS templates	\N	27	-1	2
19	6	6	f	0	0	0	RSS Templates	\N	28	-1	2
19	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
20	6	6	f	0	0	0	Saved Searches	\N	26	-1	1
20	6	6	f	0	0	0	Saved Searches	\N	27	-1	2
20	6	6	f	0	0	0	Saved Searches	\N	28	-1	2
20	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
21	6	6	f	0	0	0	Scripts	\N	26	-1	1
21	6	6	f	0	0	0	JavaScript files	\N	27	-1	2
21	6	6	f	0	0	0	Scripts	\N	28	-1	2
21	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
22	6	6	f	0	0	0	Node Templates	\N	26	-1	1
22	6	6	f	0	0	0	Template Nodes for Share - Create New document	\N	27	-1	2
22	6	6	f	0	0	0	Node Templates	\N	28	-1	2
22	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
23	6	6	f	0	0	0	Smart Folder Templates	\N	26	-1	1
23	6	6	f	0	0	0	Smart Folder Templates	\N	27	-1	2
23	6	6	f	0	0	0	Smart Folder Templates	\N	28	-1	2
23	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
24	6	6	f	0	0	0	Smart Folder Downloads	\N	26	-1	1
24	6	6	f	0	0	0	Smart Folder downloads temporary association data	\N	27	-1	2
24	6	6	f	0	0	0	Smart Folder Downloads	\N	28	-1	2
24	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
25	6	6	f	0	0	0	Guest Home	\N	26	-1	1
25	6	6	f	0	0	0	The guest root space	\N	27	-1	2
25	6	6	f	0	0	0	Guest Home	\N	28	-1	2
25	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
26	6	6	f	0	0	0	User Homes	\N	26	-1	1
26	6	6	f	0	0	0	User Homes	\N	27	-1	2
26	6	6	f	0	0	0	User Homes	\N	28	-1	2
26	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
27	6	6	f	0	0	0	Shared	\N	26	-1	1
27	6	6	f	0	0	0	Folder to store shared stuff	\N	27	-1	2
27	6	6	f	0	0	0	Shared Folder	\N	28	-1	2
27	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
28	6	6	f	0	0	0	Imap Attachments	\N	26	-1	1
28	6	6	f	0	0	0	Imap Attachments	\N	27	-1	2
28	6	6	f	0	0	0	Imap Attachments	\N	28	-1	2
28	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
29	6	6	f	0	0	0	IMAP Home	\N	26	-1	1
29	6	6	f	0	0	0	IMAP Home	\N	27	-1	2
29	6	6	f	0	0	0	IMAP Home	\N	28	-1	2
29	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
32	3	3	f	0	0	0	\N	\N	36	-1	1
32	6	6	f	0	0	0	admin	\N	37	-1	1
32	6	6	f	0	0	0	admin@alfresco.com	\N	38	-1	1
32	6	6	f	0	0	0	bootstrapHomeFolderProvider	\N	39	-1	1
32	6	6	f	0	0	0	admin	\N	40	-1	1
32	6	6	f	0	0	0		\N	41	-1	1
32	6	6	f	0	0	0		\N	42	-1	1
32	6	6	f	0	0	0	Administrator	\N	43	-1	1
33	3	3	f	0	0	0	\N	\N	36	-1	1
33	6	6	f	0	0	0	guest	\N	37	-1	1
33	6	6	f	0	0	0		\N	38	-1	1
33	6	6	f	0	0	0	bootstrapHomeFolderProvider	\N	39	-1	1
33	6	6	f	0	0	0		\N	41	-1	1
33	6	6	f	0	0	0		\N	42	-1	1
33	6	6	f	0	0	0	Guest	\N	43	-1	1
32	12	6	f	0	0	0	workspace://SpacesStore/6d7c466b-efd0-4b88-b77f-a941f3a2f025	\N	45	-1	1
33	12	6	f	0	0	0	workspace://SpacesStore/64d6c528-7ece-44a9-827d-6ae2d7b67200	\N	45	-1	1
35	6	6	f	0	0	0	categories	\N	26	-1	1
36	6	6	f	0	0	0	General	\N	26	-1	1
37	6	6	f	0	0	0	Software Document Classification	\N	26	-1	1
38	6	6	f	0	0	0	Software Descriptions	\N	26	-1	1
39	6	6	f	0	0	0	Main Software Descriptions	\N	26	-1	1
40	6	6	f	0	0	0	Short System Description	\N	26	-1	1
41	6	6	f	0	0	0	Requirement Description	\N	26	-1	1
42	6	6	f	0	0	0	Architecture Description	\N	26	-1	1
43	6	6	f	0	0	0	Implementation Description	\N	26	-1	1
44	6	6	f	0	0	0	Configuration Description	\N	26	-1	1
45	6	6	f	0	0	0	Software Description Appendices	\N	26	-1	1
46	6	6	f	0	0	0	Terminology Description	\N	26	-1	1
47	6	6	f	0	0	0	Internal Message Description	\N	26	-1	1
48	6	6	f	0	0	0	External Message Description	\N	26	-1	1
49	6	6	f	0	0	0	Record Description	\N	26	-1	1
50	6	6	f	0	0	0	User Interface Description	\N	26	-1	1
51	6	6	f	0	0	0	Process Description	\N	26	-1	1
52	6	6	f	0	0	0	Initialization Description	\N	26	-1	1
53	6	6	f	0	0	0	Utilization Documents	\N	26	-1	1
54	6	6	f	0	0	0	User's Manual	\N	26	-1	1
55	6	6	f	0	0	0	Operator's Manual	\N	26	-1	1
56	6	6	f	0	0	0	Installation Manual	\N	26	-1	1
57	6	6	f	0	0	0	Service Manual	\N	26	-1	1
58	6	6	f	0	0	0	User's Help	\N	26	-1	1
59	6	6	f	0	0	0	Operator's Help	\N	26	-1	1
60	6	6	f	0	0	0	Installations Help	\N	26	-1	1
61	6	6	f	0	0	0	Service Help	\N	26	-1	1
62	6	6	f	0	0	0	Development Plans	\N	26	-1	1
63	6	6	f	0	0	0	Responsibility Plan	\N	26	-1	1
64	6	6	f	0	0	0	Work Breakdown Plan	\N	26	-1	1
65	6	6	f	0	0	0	Schedule Plan	\N	26	-1	1
66	6	6	f	0	0	0	Expense Plan	\N	26	-1	1
67	6	6	f	0	0	0	Phase Plan	\N	26	-1	1
68	6	6	f	0	0	0	Risk Plan	\N	26	-1	1
69	6	6	f	0	0	0	Test Plan	\N	26	-1	1
70	6	6	f	0	0	0	Acceptance Plan	\N	26	-1	1
71	6	6	f	0	0	0	Manual Plan	\N	26	-1	1
72	6	6	f	0	0	0	Method Plan	\N	26	-1	1
73	6	6	f	0	0	0	Quality Plan	\N	26	-1	1
74	6	6	f	0	0	0	Documentation Plan	\N	26	-1	1
75	6	6	f	0	0	0	Version Control Plan	\N	26	-1	1
76	6	6	f	0	0	0	Quality Documents	\N	26	-1	1
77	6	6	f	0	0	0	Change Request	\N	26	-1	1
78	6	6	f	0	0	0	Analysis Request	\N	26	-1	1
79	6	6	f	0	0	0	Information Request	\N	26	-1	1
80	6	6	f	0	0	0	Reader's Report	\N	26	-1	1
81	6	6	f	0	0	0	Review Report	\N	26	-1	1
82	6	6	f	0	0	0	Inspection Report	\N	26	-1	1
83	6	6	f	0	0	0	Test Report	\N	26	-1	1
84	6	6	f	0	0	0	Review Call	\N	26	-1	1
85	6	6	f	0	0	0	Inspection Call	\N	26	-1	1
86	6	6	f	0	0	0	Test Call	\N	26	-1	1
87	6	6	f	0	0	0	Administrative Documents	\N	26	-1	1
88	6	6	f	0	0	0	Preliminary Contract	\N	26	-1	1
89	6	6	f	0	0	0	Development Contract	\N	26	-1	1
90	6	6	f	0	0	0	Extended Contract	\N	26	-1	1
91	6	6	f	0	0	0	Maintenance Contract	\N	26	-1	1
92	6	6	f	0	0	0	Contract Review Minutes	\N	26	-1	1
93	6	6	f	0	0	0	Project Meeting Minutes	\N	26	-1	1
94	6	6	f	0	0	0	Languages	\N	26	-1	1
95	6	6	f	0	0	0	English	\N	26	-1	1
96	6	6	f	0	0	0	British English	\N	26	-1	1
97	6	6	f	0	0	0	American English	\N	26	-1	1
98	6	6	f	0	0	0	Australian English	\N	26	-1	1
99	6	6	f	0	0	0	Canadian English	\N	26	-1	1
100	6	6	f	0	0	0	Indian English	\N	26	-1	1
101	6	6	f	0	0	0	French	\N	26	-1	1
102	6	6	f	0	0	0	French French	\N	26	-1	1
103	6	6	f	0	0	0	Canadian French	\N	26	-1	1
104	6	6	f	0	0	0	German	\N	26	-1	1
105	6	6	f	0	0	0	German German	\N	26	-1	1
106	6	6	f	0	0	0	Austrian German	\N	26	-1	1
107	6	6	f	0	0	0	Swiss German	\N	26	-1	1
108	6	6	f	0	0	0	Spanish	\N	26	-1	1
109	6	6	f	0	0	0	Spanish	\N	26	-1	1
110	6	6	f	0	0	0	Mexican Spanish	\N	26	-1	1
111	6	6	f	0	0	0	American Spanish	\N	26	-1	1
112	6	6	f	0	0	0	Regions	\N	26	-1	1
113	6	6	f	0	0	0	AFRICA	\N	26	-1	1
114	6	6	f	0	0	0	Eastern Africa	\N	26	-1	1
115	6	6	f	0	0	0	Burundi	\N	26	-1	1
116	6	6	f	0	0	0	Comoros	\N	26	-1	1
117	6	6	f	0	0	0	Djibouti	\N	26	-1	1
118	6	6	f	0	0	0	Eritrea	\N	26	-1	1
119	6	6	f	0	0	0	Ethiopia	\N	26	-1	1
120	6	6	f	0	0	0	Kenya	\N	26	-1	1
121	6	6	f	0	0	0	Madagascar	\N	26	-1	1
122	6	6	f	0	0	0	Malawi	\N	26	-1	1
123	6	6	f	0	0	0	Mauritius	\N	26	-1	1
124	6	6	f	0	0	0	Mozambique	\N	26	-1	1
125	6	6	f	0	0	0	Reunion	\N	26	-1	1
126	6	6	f	0	0	0	Rwanda	\N	26	-1	1
127	6	6	f	0	0	0	Seychelles	\N	26	-1	1
128	6	6	f	0	0	0	Somalia	\N	26	-1	1
129	6	6	f	0	0	0	Uganda	\N	26	-1	1
130	6	6	f	0	0	0	United Republic of Tanzania	\N	26	-1	1
131	6	6	f	0	0	0	Zambia	\N	26	-1	1
132	6	6	f	0	0	0	Zimbabwe	\N	26	-1	1
133	6	6	f	0	0	0	Middle Africa	\N	26	-1	1
134	6	6	f	0	0	0	Angola	\N	26	-1	1
135	6	6	f	0	0	0	Cameroon	\N	26	-1	1
136	6	6	f	0	0	0	Central African Republic	\N	26	-1	1
137	6	6	f	0	0	0	Chad	\N	26	-1	1
138	6	6	f	0	0	0	Congo	\N	26	-1	1
139	6	6	f	0	0	0	Democratic Republic of the Congo	\N	26	-1	1
140	6	6	f	0	0	0	Equatorial Guinea	\N	26	-1	1
141	6	6	f	0	0	0	Gabon	\N	26	-1	1
142	6	6	f	0	0	0	Sao Tome and Principe	\N	26	-1	1
143	6	6	f	0	0	0	Northern Africa	\N	26	-1	1
144	6	6	f	0	0	0	Algeria	\N	26	-1	1
145	6	6	f	0	0	0	Egypt	\N	26	-1	1
146	6	6	f	0	0	0	Libyan Arab Jamahiriya	\N	26	-1	1
147	6	6	f	0	0	0	Morocco	\N	26	-1	1
148	6	6	f	0	0	0	Sudan	\N	26	-1	1
149	6	6	f	0	0	0	Tunisia	\N	26	-1	1
150	6	6	f	0	0	0	Western Sahara	\N	26	-1	1
151	6	6	f	0	0	0	Southern Africa	\N	26	-1	1
152	6	6	f	0	0	0	Botswana	\N	26	-1	1
153	6	6	f	0	0	0	Lesotho	\N	26	-1	1
154	6	6	f	0	0	0	Namibia	\N	26	-1	1
155	6	6	f	0	0	0	South Africa	\N	26	-1	1
156	6	6	f	0	0	0	Swaziland	\N	26	-1	1
157	6	6	f	0	0	0	Western Africa	\N	26	-1	1
158	6	6	f	0	0	0	Benin	\N	26	-1	1
159	6	6	f	0	0	0	Burkina Faso	\N	26	-1	1
160	6	6	f	0	0	0	Cape Verde	\N	26	-1	1
161	6	6	f	0	0	0	Cote d'Ivoire	\N	26	-1	1
162	6	6	f	0	0	0	Gambia	\N	26	-1	1
163	6	6	f	0	0	0	Ghana	\N	26	-1	1
164	6	6	f	0	0	0	Guinea	\N	26	-1	1
165	6	6	f	0	0	0	Guinea-Bissau	\N	26	-1	1
166	6	6	f	0	0	0	Liberia	\N	26	-1	1
167	6	6	f	0	0	0	Mali	\N	26	-1	1
168	6	6	f	0	0	0	Mauritania	\N	26	-1	1
169	6	6	f	0	0	0	Niger	\N	26	-1	1
170	6	6	f	0	0	0	Nigeria	\N	26	-1	1
171	6	6	f	0	0	0	Saint Helena	\N	26	-1	1
172	6	6	f	0	0	0	Senegal	\N	26	-1	1
173	6	6	f	0	0	0	Sierra Leone	\N	26	-1	1
174	6	6	f	0	0	0	Togo	\N	26	-1	1
175	6	6	f	0	0	0	ASIA	\N	26	-1	1
176	6	6	f	0	0	0	Eastern Asia	\N	26	-1	1
177	6	6	f	0	0	0	China	\N	26	-1	1
178	6	6	f	0	0	0	Democratic People's Republic of Korea	\N	26	-1	1
179	6	6	f	0	0	0	Hong Kong SAR	\N	26	-1	1
180	6	6	f	0	0	0	Japan	\N	26	-1	1
181	6	6	f	0	0	0	Macao, China	\N	26	-1	1
182	6	6	f	0	0	0	Mongolia	\N	26	-1	1
183	6	6	f	0	0	0	Republic of Korea	\N	26	-1	1
184	6	6	f	0	0	0	South-central Asia	\N	26	-1	1
185	6	6	f	0	0	0	Afghanistan	\N	26	-1	1
186	6	6	f	0	0	0	Bangladesh	\N	26	-1	1
187	6	6	f	0	0	0	Bhutan	\N	26	-1	1
188	6	6	f	0	0	0	India	\N	26	-1	1
189	6	6	f	0	0	0	Iran (Islamic Republic of)	\N	26	-1	1
190	6	6	f	0	0	0	Kazakhstan	\N	26	-1	1
191	6	6	f	0	0	0	Kyrgyzstan	\N	26	-1	1
192	6	6	f	0	0	0	Maldives	\N	26	-1	1
193	6	6	f	0	0	0	Nepal	\N	26	-1	1
194	6	6	f	0	0	0	Pakistan	\N	26	-1	1
195	6	6	f	0	0	0	Sri Lanka	\N	26	-1	1
196	6	6	f	0	0	0	Tajikistan	\N	26	-1	1
197	6	6	f	0	0	0	Turkmenistan	\N	26	-1	1
198	6	6	f	0	0	0	Uzbekistan	\N	26	-1	1
199	6	6	f	0	0	0	South-eastern Asia	\N	26	-1	1
200	6	6	f	0	0	0	Brunei Darussalam	\N	26	-1	1
201	6	6	f	0	0	0	Cambodia	\N	26	-1	1
202	6	6	f	0	0	0	Indonesia	\N	26	-1	1
203	6	6	f	0	0	0	Lao People's Democratic Republic	\N	26	-1	1
204	6	6	f	0	0	0	Malaysia	\N	26	-1	1
205	6	6	f	0	0	0	Myanmar	\N	26	-1	1
206	6	6	f	0	0	0	Philippines	\N	26	-1	1
207	6	6	f	0	0	0	Singapore	\N	26	-1	1
208	6	6	f	0	0	0	Thailand	\N	26	-1	1
209	6	6	f	0	0	0	Timor-Leste	\N	26	-1	1
210	6	6	f	0	0	0	Viet Nam	\N	26	-1	1
211	6	6	f	0	0	0	Western Asia	\N	26	-1	1
212	6	6	f	0	0	0	Armenia	\N	26	-1	1
213	6	6	f	0	0	0	Azerbaijan	\N	26	-1	1
214	6	6	f	0	0	0	Bahrain	\N	26	-1	1
215	6	6	f	0	0	0	Cyprus	\N	26	-1	1
216	6	6	f	0	0	0	Georgia	\N	26	-1	1
217	6	6	f	0	0	0	Iraq	\N	26	-1	1
218	6	6	f	0	0	0	Israel	\N	26	-1	1
219	6	6	f	0	0	0	Jordan	\N	26	-1	1
220	6	6	f	0	0	0	Kuwait	\N	26	-1	1
221	6	6	f	0	0	0	Lebanon	\N	26	-1	1
222	6	6	f	0	0	0	Occupied Palestinian Territory	\N	26	-1	1
223	6	6	f	0	0	0	Oman	\N	26	-1	1
224	6	6	f	0	0	0	Qatar	\N	26	-1	1
225	6	6	f	0	0	0	Saudi Arabia	\N	26	-1	1
226	6	6	f	0	0	0	Syrian Arab Republic	\N	26	-1	1
227	6	6	f	0	0	0	Turkey	\N	26	-1	1
228	6	6	f	0	0	0	United Arab Emirates	\N	26	-1	1
229	6	6	f	0	0	0	Yemen	\N	26	-1	1
230	6	6	f	0	0	0	EUROPE	\N	26	-1	1
231	6	6	f	0	0	0	Eastern Europe	\N	26	-1	1
232	6	6	f	0	0	0	Belarus	\N	26	-1	1
233	6	6	f	0	0	0	Bulgaria	\N	26	-1	1
234	6	6	f	0	0	0	Czech Republic	\N	26	-1	1
235	6	6	f	0	0	0	Hungary	\N	26	-1	1
236	6	6	f	0	0	0	Poland	\N	26	-1	1
237	6	6	f	0	0	0	Republic of Moldova	\N	26	-1	1
238	6	6	f	0	0	0	Romania	\N	26	-1	1
239	6	6	f	0	0	0	Russian Federation	\N	26	-1	1
240	6	6	f	0	0	0	Slovakia	\N	26	-1	1
241	6	6	f	0	0	0	Ukraine	\N	26	-1	1
242	6	6	f	0	0	0	Northern Europe	\N	26	-1	1
243	6	6	f	0	0	0	Channel Islands	\N	26	-1	1
244	6	6	f	0	0	0	Denmark	\N	26	-1	1
245	6	6	f	0	0	0	Estonia	\N	26	-1	1
246	6	6	f	0	0	0	Faeroe Islands	\N	26	-1	1
247	6	6	f	0	0	0	Finland	\N	26	-1	1
248	6	6	f	0	0	0	Iceland	\N	26	-1	1
249	6	6	f	0	0	0	Ireland	\N	26	-1	1
250	6	6	f	0	0	0	Isle of Man	\N	26	-1	1
251	6	6	f	0	0	0	Latvia	\N	26	-1	1
252	6	6	f	0	0	0	Lithuania	\N	26	-1	1
253	6	6	f	0	0	0	Norway	\N	26	-1	1
254	6	6	f	0	0	0	Sweden	\N	26	-1	1
255	6	6	f	0	0	0	United Kingdom	\N	26	-1	1
256	6	6	f	0	0	0	Southern Europe	\N	26	-1	1
257	6	6	f	0	0	0	Albania	\N	26	-1	1
258	6	6	f	0	0	0	Andorra	\N	26	-1	1
259	6	6	f	0	0	0	Bosnia and Herzegovina	\N	26	-1	1
260	6	6	f	0	0	0	Croatia	\N	26	-1	1
261	6	6	f	0	0	0	Gibraltar	\N	26	-1	1
262	6	6	f	0	0	0	Greece	\N	26	-1	1
263	6	6	f	0	0	0	Holy See	\N	26	-1	1
264	6	6	f	0	0	0	Italy	\N	26	-1	1
265	6	6	f	0	0	0	Malta	\N	26	-1	1
266	6	6	f	0	0	0	Portugal	\N	26	-1	1
267	6	6	f	0	0	0	San Marino	\N	26	-1	1
268	6	6	f	0	0	0	Slovenia	\N	26	-1	1
269	6	6	f	0	0	0	Spain	\N	26	-1	1
270	6	6	f	0	0	0	The Former Yugoslav Republic of Macedonia	\N	26	-1	1
271	6	6	f	0	0	0	Yugoslavia	\N	26	-1	1
272	6	6	f	0	0	0	Western Europe	\N	26	-1	1
273	6	6	f	0	0	0	Austria	\N	26	-1	1
274	6	6	f	0	0	0	Belgium	\N	26	-1	1
275	6	6	f	0	0	0	France	\N	26	-1	1
276	6	6	f	0	0	0	Germany	\N	26	-1	1
277	6	6	f	0	0	0	Liechtenstein	\N	26	-1	1
278	6	6	f	0	0	0	Luxembourg	\N	26	-1	1
279	6	6	f	0	0	0	Monaco	\N	26	-1	1
280	6	6	f	0	0	0	Netherlands	\N	26	-1	1
281	6	6	f	0	0	0	Switzerland	\N	26	-1	1
282	6	6	f	0	0	0	LATIN AMERICA	\N	26	-1	1
283	6	6	f	0	0	0	Caribbean	\N	26	-1	1
284	6	6	f	0	0	0	Anguilla	\N	26	-1	1
285	6	6	f	0	0	0	Antigua and Barbuda	\N	26	-1	1
286	6	6	f	0	0	0	Aruba	\N	26	-1	1
287	6	6	f	0	0	0	Bahamas	\N	26	-1	1
288	6	6	f	0	0	0	Barbados	\N	26	-1	1
289	6	6	f	0	0	0	British Virgin Islands	\N	26	-1	1
290	6	6	f	0	0	0	Cayman Islands	\N	26	-1	1
291	6	6	f	0	0	0	Cuba	\N	26	-1	1
292	6	6	f	0	0	0	Dominica	\N	26	-1	1
293	6	6	f	0	0	0	Dominican Republic	\N	26	-1	1
294	6	6	f	0	0	0	Grenada	\N	26	-1	1
295	6	6	f	0	0	0	Guadeloupe	\N	26	-1	1
296	6	6	f	0	0	0	Haiti	\N	26	-1	1
297	6	6	f	0	0	0	Jamaica	\N	26	-1	1
298	6	6	f	0	0	0	Martinique	\N	26	-1	1
299	6	6	f	0	0	0	Montserrat	\N	26	-1	1
300	6	6	f	0	0	0	Netherlands Antilles	\N	26	-1	1
301	6	6	f	0	0	0	Puerto Rico	\N	26	-1	1
302	6	6	f	0	0	0	Saint Kitts and Nevis	\N	26	-1	1
303	6	6	f	0	0	0	Saint Lucia	\N	26	-1	1
304	6	6	f	0	0	0	Saint Vincent and Grenadines	\N	26	-1	1
305	6	6	f	0	0	0	Trinidad and Tobago	\N	26	-1	1
306	6	6	f	0	0	0	Turks and Caicos Islands	\N	26	-1	1
307	6	6	f	0	0	0	United States Virgin Islands	\N	26	-1	1
308	6	6	f	0	0	0	Central America	\N	26	-1	1
309	6	6	f	0	0	0	Belize	\N	26	-1	1
310	6	6	f	0	0	0	Costa Rica	\N	26	-1	1
311	6	6	f	0	0	0	El Salvador	\N	26	-1	1
312	6	6	f	0	0	0	Guatemala	\N	26	-1	1
313	6	6	f	0	0	0	Honduras	\N	26	-1	1
314	6	6	f	0	0	0	Mexico	\N	26	-1	1
315	6	6	f	0	0	0	Nicaragua	\N	26	-1	1
316	6	6	f	0	0	0	Panama	\N	26	-1	1
317	6	6	f	0	0	0	South America	\N	26	-1	1
318	6	6	f	0	0	0	Argentina	\N	26	-1	1
319	6	6	f	0	0	0	Bolivia	\N	26	-1	1
320	6	6	f	0	0	0	Brazil	\N	26	-1	1
321	6	6	f	0	0	0	Chile	\N	26	-1	1
322	6	6	f	0	0	0	Colombia	\N	26	-1	1
323	6	6	f	0	0	0	Ecuador	\N	26	-1	1
324	6	6	f	0	0	0	Falkland Islands (Malvinas)	\N	26	-1	1
325	6	6	f	0	0	0	French Guiana	\N	26	-1	1
326	6	6	f	0	0	0	Guyana	\N	26	-1	1
327	6	6	f	0	0	0	Paraguay	\N	26	-1	1
328	6	6	f	0	0	0	Peru	\N	26	-1	1
329	6	6	f	0	0	0	Suriname	\N	26	-1	1
330	6	6	f	0	0	0	Uruguay	\N	26	-1	1
331	6	6	f	0	0	0	Venezuela	\N	26	-1	1
332	6	6	f	0	0	0	NORTHERN AMERICA	\N	26	-1	1
333	6	6	f	0	0	0	Bermuda	\N	26	-1	1
334	6	6	f	0	0	0	Canada	\N	26	-1	1
335	6	6	f	0	0	0	Greenland	\N	26	-1	1
336	6	6	f	0	0	0	Saint Pierre and Miquelon	\N	26	-1	1
337	6	6	f	0	0	0	United States of America	\N	26	-1	1
338	6	6	f	0	0	0	OCEANIA	\N	26	-1	1
339	6	6	f	0	0	0	Australia and New Zealand	\N	26	-1	1
340	6	6	f	0	0	0	Australia	\N	26	-1	1
341	6	6	f	0	0	0	New Zealand	\N	26	-1	1
342	6	6	f	0	0	0	Norfolk Island	\N	26	-1	1
343	6	6	f	0	0	0	Melanesia	\N	26	-1	1
344	6	6	f	0	0	0	Fiji	\N	26	-1	1
345	6	6	f	0	0	0	New Caledonia	\N	26	-1	1
346	6	6	f	0	0	0	Papua New Guinea	\N	26	-1	1
347	6	6	f	0	0	0	Solomon Islands	\N	26	-1	1
348	6	6	f	0	0	0	Vanuatu	\N	26	-1	1
349	6	6	f	0	0	0	Micronesia	\N	26	-1	1
350	6	6	f	0	0	0	Federated States of Micronesia	\N	26	-1	1
351	6	6	f	0	0	0	Guam	\N	26	-1	1
352	6	6	f	0	0	0	Johnston Island	\N	26	-1	1
353	6	6	f	0	0	0	Kiribati	\N	26	-1	1
354	6	6	f	0	0	0	Marshall Islands	\N	26	-1	1
355	6	6	f	0	0	0	Nauru	\N	26	-1	1
356	6	6	f	0	0	0	Northern Mariana Islands	\N	26	-1	1
357	6	6	f	0	0	0	Palau	\N	26	-1	1
358	6	6	f	0	0	0	Polynesia	\N	26	-1	1
359	6	6	f	0	0	0	American Samoa	\N	26	-1	1
360	6	6	f	0	0	0	Cook Islands	\N	26	-1	1
361	6	6	f	0	0	0	French Polynesia	\N	26	-1	1
362	6	6	f	0	0	0	Niue	\N	26	-1	1
363	6	6	f	0	0	0	Pitcairn	\N	26	-1	1
364	6	6	f	0	0	0	Samoa	\N	26	-1	1
365	6	6	f	0	0	0	Tokelau	\N	26	-1	1
366	6	6	f	0	0	0	Tonga	\N	26	-1	1
367	6	6	f	0	0	0	Tuvalu	\N	26	-1	1
368	6	6	f	0	0	0	Wallis and Futuna Islands	\N	26	-1	1
369	6	6	f	0	0	0	Tags	\N	26	-1	1
371	6	6	f	0	0	0	Software Engineering Project	\N	26	-1	1
371	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
372	6	6	f	0	0	0	Documentation	\N	26	-1	1
372	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
373	6	6	f	0	0	0	Drafts	\N	26	-1	1
373	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
374	6	6	f	0	0	0	Pending Approval	\N	26	-1	1
374	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
375	6	6	f	0	0	0	Published	\N	26	-1	1
375	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
376	6	6	f	0	0	0	Samples	\N	26	-1	1
376	6	6	f	0	0	0	space-icon-doc	\N	29	-1	1
377	6	6	f	0	0	0	system-overview.html	\N	26	-1	1
377	6	6	f	0	0	0	system-overview.html	\N	27	-1	2
377	6	6	f	0	0	0	System Overview	\N	28	-1	2
377	21	3	f	5	0	0	\N	\N	51	-1	1
378	6	6	f	0	0	0	Discussions	\N	26	-1	1
378	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
379	6	6	f	0	0	0	UI Design	\N	26	-1	1
379	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
380	6	6	f	0	0	0	Presentations	\N	26	-1	1
380	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
381	6	6	f	0	0	0	Quality Assurance	\N	26	-1	1
381	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
382	1	1	t	0	0	0	\N	\N	52	-1	1
382	6	6	f	0	0	0	doc_info.ftl	\N	26	-1	1
382	6	6	f	0	0	0	Displays useful information about the current document	\N	27	-1	2
382	6	6	f	0	0	0	doc_info.ftl	\N	28	-1	2
382	21	3	f	6	0	0	\N	\N	51	-1	1
383	1	1	t	0	0	0	\N	\N	52	-1	1
383	6	6	f	0	0	0	localizable.ftl	\N	26	-1	1
383	6	6	f	0	0	0	Calculates if the document has the localizable aspect applied	\N	27	-1	2
383	6	6	f	0	0	0	localizable.ftl	\N	28	-1	2
383	21	3	f	7	0	0	\N	\N	51	-1	1
384	1	1	t	0	0	0	\N	\N	52	-1	1
384	6	6	f	0	0	0	my_docs.ftl	\N	26	-1	1
384	6	6	f	0	0	0	Displays a list of the documents in the current user Home Space	\N	27	-1	2
384	6	6	f	0	0	0	my_docs.ftl	\N	28	-1	2
384	21	3	f	8	0	0	\N	\N	51	-1	1
385	1	1	t	0	0	0	\N	\N	52	-1	1
385	6	6	f	0	0	0	my_spaces.ftl	\N	26	-1	1
385	6	6	f	0	0	0	Displays a list of spaces in the current user Home Space	\N	27	-1	2
385	6	6	f	0	0	0	my_spaces.ftl	\N	28	-1	2
385	21	3	f	9	0	0	\N	\N	51	-1	1
386	1	1	t	0	0	0	\N	\N	52	-1	1
386	6	6	f	0	0	0	my_summary.ftl	\N	26	-1	1
386	6	6	f	0	0	0	Shows a simple summary page about the current user and their Home Space	\N	27	-1	2
386	6	6	f	0	0	0	my_summary.ftl	\N	28	-1	2
386	21	3	f	10	0	0	\N	\N	51	-1	1
387	1	1	t	0	0	0	\N	\N	52	-1	1
387	6	6	f	0	0	0	translatable.ftl	\N	26	-1	1
387	6	6	f	0	0	0	Calculates if the document has the translatable aspect applied	\N	27	-1	2
387	6	6	f	0	0	0	translatable.ftl	\N	28	-1	2
387	21	3	f	11	0	0	\N	\N	51	-1	1
388	1	1	t	0	0	0	\N	\N	52	-1	1
388	6	6	f	0	0	0	recent_docs.ftl	\N	26	-1	1
388	6	6	f	0	0	0	Displays a list of the documents in the current space created or modified in the last 7 days	\N	27	-1	2
388	6	6	f	0	0	0	recent_docs.ftl	\N	28	-1	2
388	21	3	f	12	0	0	\N	\N	51	-1	1
389	1	1	t	0	0	0	\N	\N	52	-1	1
389	6	6	f	0	0	0	general_example.ftl	\N	26	-1	1
389	6	6	f	0	0	0	Example of various lists of documents, spaces and summary information about the current user	\N	27	-1	2
389	6	6	f	0	0	0	general_example.ftl	\N	28	-1	2
389	21	3	f	13	0	0	\N	\N	51	-1	1
390	1	1	t	0	0	0	\N	\N	52	-1	1
390	6	6	f	0	0	0	my_docs_inline.ftl	\N	26	-1	1
390	6	6	f	0	0	0	Displays a list of the documents in the current user Home Space. Text document content is shown inline, as is JPG content as small thumbnail images.	\N	27	-1	2
390	6	6	f	0	0	0	my_docs_inline.ftl	\N	28	-1	2
390	21	3	f	14	0	0	\N	\N	51	-1	1
391	1	1	t	0	0	0	\N	\N	52	-1	1
391	6	6	f	0	0	0	show_audit.ftl	\N	26	-1	1
391	6	6	f	0	0	0	Displays the audit trail for an object.	\N	27	-1	2
391	6	6	f	0	0	0	show_audit.ftl	\N	28	-1	2
391	21	3	f	15	0	0	\N	\N	51	-1	1
392	1	1	t	0	0	0	\N	\N	52	-1	1
392	6	6	f	0	0	0	Mike Farman	\N	54	-1	1
392	6	6	f	0	0	0	readme.ftl	\N	26	-1	1
392	6	6	f	0	0	0	Display the contents of a readme file (named readme.html or readme.ftl)	\N	27	-1	2
392	6	6	f	0	0	0	readme.ftl	\N	28	-1	2
392	21	3	f	16	0	0	\N	\N	51	-1	1
393	6	6	f	0	0	0	Invite Email Templates	\N	26	-1	1
393	6	6	f	0	0	0	Invite Email Templates	\N	27	-1	2
393	6	6	f	0	0	0	Invite Email Templates	\N	28	-1	2
393	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
394	1	1	t	0	0	0	\N	\N	52	-1	1
394	6	6	f	0	0	0	invite_user_email.ftl	\N	26	-1	1
394	6	6	f	0	0	0	Email template for notifying users of an Invite to a space or document	\N	27	-1	2
394	6	6	f	0	0	0	invite_user_email.ftl	\N	28	-1	2
394	21	3	f	17	0	0	\N	\N	51	-1	1
395	6	6	f	0	0	0	Notify Email Templates	\N	26	-1	1
395	6	6	f	0	0	0	Notify Email Templates	\N	27	-1	2
395	6	6	f	0	0	0	Notify Email Templates	\N	28	-1	2
395	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
396	1	1	t	0	0	0	\N	\N	52	-1	1
396	6	6	f	0	0	0	notify_user_email.ftl.sample	\N	26	-1	1
396	6	6	f	0	0	0	Sample Email template for notifying users from a rule or action	\N	27	-1	2
396	6	6	f	0	0	0	notify_user_email.ftl.sample	\N	28	-1	2
396	21	3	f	18	0	0	\N	\N	51	-1	1
397	6	6	f	0	0	0	activities	\N	26	-1	1
397	6	6	f	0	0	0	Activities email templates	\N	27	-1	2
397	6	6	f	0	0	0	activities	\N	28	-1	2
397	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
398	1	1	t	0	0	0	\N	\N	52	-1	1
398	6	6	f	0	0	0		\N	54	-1	1
398	6	6	f	0	0	0	activities-email.ftl	\N	26	-1	1
398	6	6	f	0	0	0	Email template used to generate the activities email for Alfresco Share - Default version	\N	27	-1	2
398	6	6	f	0	0	0	activities-email.ftl	\N	28	-1	2
398	21	3	f	19	0	0	\N	\N	51	-1	1
399	1	1	t	0	0	0	\N	\N	52	-1	1
399	6	6	f	0	0	0		\N	54	-1	1
399	6	6	f	0	0	0	activities-email_fr.ftl	\N	26	-1	1
399	6	6	f	0	0	0	Email template used to generate the activities email for Alfresco Share - French version	\N	27	-1	2
399	6	6	f	0	0	0	activities-email_fr.ftl	\N	28	-1	2
399	21	3	f	20	0	0	\N	\N	51	-1	1
400	1	1	t	0	0	0	\N	\N	52	-1	1
400	6	6	f	0	0	0		\N	54	-1	1
400	6	6	f	0	0	0	activities-email_es.ftl	\N	26	-1	1
400	6	6	f	0	0	0	Email template used to generate the activities email for Alfresco Share - Spanish version	\N	27	-1	2
400	6	6	f	0	0	0	activities-email_es.ftl	\N	28	-1	2
400	21	3	f	21	0	0	\N	\N	51	-1	1
401	1	1	t	0	0	0	\N	\N	52	-1	1
401	6	6	f	0	0	0		\N	54	-1	1
401	6	6	f	0	0	0	activities-email_de.ftl	\N	26	-1	1
401	6	6	f	0	0	0	Email template used to generate the activities email for Alfresco Share - German version	\N	27	-1	2
401	6	6	f	0	0	0	activities-email_de.ftl	\N	28	-1	2
401	21	3	f	22	0	0	\N	\N	51	-1	1
402	1	1	t	0	0	0	\N	\N	52	-1	1
402	6	6	f	0	0	0		\N	54	-1	1
402	6	6	f	0	0	0	activities-email_it.ftl	\N	26	-1	1
402	6	6	f	0	0	0	Email template used to generate the activities email for Alfresco Share - Italian version	\N	27	-1	2
402	6	6	f	0	0	0	activities-email_it.ftl	\N	28	-1	2
402	21	3	f	23	0	0	\N	\N	51	-1	1
403	1	1	t	0	0	0	\N	\N	52	-1	1
403	6	6	f	0	0	0		\N	54	-1	1
403	6	6	f	0	0	0	activities-email_ja.ftl	\N	26	-1	1
403	6	6	f	0	0	0	Email template used to generate the activities email for Alfresco Share - Japanese version	\N	27	-1	2
403	6	6	f	0	0	0	activities-email_ja.ftl	\N	28	-1	2
403	21	3	f	24	0	0	\N	\N	51	-1	1
404	1	1	t	0	0	0	\N	\N	52	-1	1
404	6	6	f	0	0	0		\N	54	-1	1
404	6	6	f	0	0	0	activities-email_nl.ftl	\N	26	-1	1
404	6	6	f	0	0	0	Email template used to generate the activities email for Alfresco Share - Dutch version	\N	27	-1	2
404	6	6	f	0	0	0	activities-email_nl.ftl	\N	28	-1	2
404	21	3	f	25	0	0	\N	\N	51	-1	1
405	6	6	f	0	0	0	Following Email Templates	\N	26	-1	1
405	6	6	f	0	0	0	Following Email Templates	\N	27	-1	2
405	6	6	f	0	0	0	Following Email Templates	\N	28	-1	2
406	1	1	t	0	0	0	\N	\N	52	-1	1
406	6	6	f	0	0	0	following-email.html.ftl	\N	26	-1	1
406	6	6	f	0	0	0	Email template used to generate following notification emails - Default version	\N	27	-1	2
406	6	6	f	0	0	0	following-email.html.ftl	\N	28	-1	2
406	21	3	f	26	0	0	\N	\N	51	-1	1
407	1	1	t	0	0	0	\N	\N	52	-1	1
407	6	6	f	0	0	0	following-email_de.html.ftl	\N	26	-1	1
407	6	6	f	0	0	0	Email template used to generate following notification emails - Default version	\N	27	-1	2
407	6	6	f	0	0	0	following-email_de.html.ftl	\N	28	-1	2
407	21	3	f	27	0	0	\N	\N	51	-1	1
408	1	1	t	0	0	0	\N	\N	52	-1	1
408	6	6	f	0	0	0	following-email_es.html.ftl	\N	26	-1	1
408	6	6	f	0	0	0	Email template used to generate following notification emails - Default version	\N	27	-1	2
408	6	6	f	0	0	0	following-email_es.html.ftl	\N	28	-1	2
408	21	3	f	28	0	0	\N	\N	51	-1	1
409	1	1	t	0	0	0	\N	\N	52	-1	1
409	6	6	f	0	0	0	following-email_fr.html.ftl	\N	26	-1	1
409	6	6	f	0	0	0	Email template used to generate following notification emails - Default version	\N	27	-1	2
409	6	6	f	0	0	0	following-email_fr.html.ftl	\N	28	-1	2
409	21	3	f	29	0	0	\N	\N	51	-1	1
410	1	1	t	0	0	0	\N	\N	52	-1	1
410	6	6	f	0	0	0	following-email_it.html.ftl	\N	26	-1	1
410	6	6	f	0	0	0	Email template used to generate following notification emails - Default version	\N	27	-1	2
410	6	6	f	0	0	0	following-email_it.html.ftl	\N	28	-1	2
410	21	3	f	30	0	0	\N	\N	51	-1	1
411	1	1	t	0	0	0	\N	\N	52	-1	1
411	6	6	f	0	0	0	following-email_ja.html.ftl	\N	26	-1	1
411	6	6	f	0	0	0	Email template used to generate following notification emails - Default version	\N	27	-1	2
411	6	6	f	0	0	0	following-email_ja.html.ftl	\N	28	-1	2
411	21	3	f	31	0	0	\N	\N	51	-1	1
412	1	1	t	0	0	0	\N	\N	52	-1	1
412	6	6	f	0	0	0	following-email_nl.html.ftl	\N	26	-1	1
412	6	6	f	0	0	0	Email template used to generate following notification emails - Default version	\N	27	-1	2
412	6	6	f	0	0	0	following-email_nl.html.ftl	\N	28	-1	2
412	21	3	f	32	0	0	\N	\N	51	-1	1
413	6	6	f	0	0	0	Workflow Notification	\N	26	-1	1
413	6	6	f	0	0	0	Workflow notification email templates	\N	27	-1	2
413	6	6	f	0	0	0	Workflow Notification	\N	28	-1	2
413	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
414	1	1	t	0	0	0	\N	\N	52	-1	1
414	6	6	f	0	0	0	wf-email.html.ftl	\N	26	-1	1
414	6	6	f	0	0	0	Email template for notifying users of new a workflow task - Default version	\N	27	-1	2
414	6	6	f	0	0	0	wf-email.html.ftl	\N	28	-1	2
414	21	3	f	33	0	0	\N	\N	51	-1	1
415	1	1	t	0	0	0	\N	\N	52	-1	1
415	6	6	f	0	0	0	wf-email_de.html.ftl	\N	26	-1	1
415	6	6	f	0	0	0	Email template for notifying users of new a workflow task - Default version	\N	27	-1	2
415	6	6	f	0	0	0	wf-email_de.html.ftl	\N	28	-1	2
415	21	3	f	34	0	0	\N	\N	51	-1	1
416	1	1	t	0	0	0	\N	\N	52	-1	1
416	6	6	f	0	0	0	wf-email_es.html.ftl	\N	26	-1	1
416	6	6	f	0	0	0	Email template for notifying users of new a workflow task - Default version	\N	27	-1	2
416	6	6	f	0	0	0	wf-email_es.html.ftl	\N	28	-1	2
416	21	3	f	35	0	0	\N	\N	51	-1	1
417	1	1	t	0	0	0	\N	\N	52	-1	1
417	6	6	f	0	0	0	wf-email_fr.html.ftl	\N	26	-1	1
417	6	6	f	0	0	0	Email template for notifying users of new a workflow task - Default version	\N	27	-1	2
417	6	6	f	0	0	0	wf-email_fr.html.ftl	\N	28	-1	2
417	21	3	f	36	0	0	\N	\N	51	-1	1
418	1	1	t	0	0	0	\N	\N	52	-1	1
418	6	6	f	0	0	0	wf-email_it.html.ftl	\N	26	-1	1
418	6	6	f	0	0	0	Email template for notifying users of new a workflow task - Default version	\N	27	-1	2
418	6	6	f	0	0	0	wf-email_it.html.ftl	\N	28	-1	2
418	21	3	f	37	0	0	\N	\N	51	-1	1
419	1	1	t	0	0	0	\N	\N	52	-1	1
419	6	6	f	0	0	0	wf-email_ja.html.ftl	\N	26	-1	1
419	6	6	f	0	0	0	Email template for notifying users of new a workflow task - Default version	\N	27	-1	2
419	6	6	f	0	0	0	wf-email_ja.html.ftl	\N	28	-1	2
419	21	3	f	38	0	0	\N	\N	51	-1	1
420	1	1	t	0	0	0	\N	\N	52	-1	1
420	6	6	f	0	0	0	wf-email_nl.html.ftl	\N	26	-1	1
420	6	6	f	0	0	0	Email template for notifying users of new a workflow task - Default version	\N	27	-1	2
420	6	6	f	0	0	0	wf-email_nl.html.ftl	\N	28	-1	2
420	21	3	f	39	0	0	\N	\N	51	-1	1
421	1	1	t	0	0	0	\N	\N	52	-1	1
421	0	0	f	0	0	0	\N	\N	55	-1	1
421	6	6	f	0	0	0	RSS_2.0_recent_docs.ftl	\N	26	-1	1
421	6	6	f	0	0	0	Renders a valid RSS2.0 XML document showing the documents in the current space created or modified in the last 7 days. The template should be configured to use the appropriate server and port before use.	\N	27	-1	2
421	6	6	f	0	0	0	RSS recent docs	\N	28	-1	2
421	21	3	f	40	0	0	\N	\N	51	-1	1
422	1	1	t	0	0	0	\N	\N	52	-1	1
422	6	6	f	0	0	0	Kevin Roast	\N	54	-1	1
422	6	6	f	0	0	0	backup.js.sample	\N	26	-1	1
422	6	6	f	0	0	0	Simple document backup script	\N	27	-1	2
422	6	6	f	0	0	0	Backup Script	\N	28	-1	2
422	21	3	f	41	0	0	\N	\N	51	-1	1
423	1	1	t	0	0	0	\N	\N	52	-1	1
423	6	6	f	0	0	0	Kevin Roast	\N	54	-1	1
423	6	6	f	0	0	0	example test script.js.sample	\N	26	-1	1
423	6	6	f	0	0	0	Example of various API calls	\N	27	-1	2
423	6	6	f	0	0	0	Example Test Script	\N	28	-1	2
423	21	3	f	42	0	0	\N	\N	51	-1	1
424	1	1	t	0	0	0	\N	\N	52	-1	1
424	6	6	f	0	0	0	Kevin Roast	\N	54	-1	1
424	6	6	f	0	0	0	backup and log.js.sample	\N	26	-1	1
424	6	6	f	0	0	0	Backup files and log the date and time	\N	27	-1	2
424	6	6	f	0	0	0	Backup and logging Script	\N	28	-1	2
424	21	3	f	43	0	0	\N	\N	51	-1	1
425	1	1	t	0	0	0	\N	\N	52	-1	1
425	6	6	f	0	0	0	Kevin Roast	\N	54	-1	1
425	6	6	f	0	0	0	append copyright.js.sample	\N	26	-1	1
425	6	6	f	0	0	0	Append Copyright line to text or HTML files	\N	27	-1	2
425	6	6	f	0	0	0	Append Copyright to file	\N	28	-1	2
425	21	3	f	44	0	0	\N	\N	51	-1	1
426	1	1	t	0	0	0	\N	\N	52	-1	1
426	6	6	f	0	0	0	Kevin Roast	\N	54	-1	1
426	6	6	f	0	0	0	alfresco docs.js.sample	\N	26	-1	1
426	6	6	f	0	0	0	Search and log all docs containing text	\N	27	-1	2
426	6	6	f	0	0	0	Lucene Search	\N	28	-1	2
426	21	3	f	45	0	0	\N	\N	51	-1	1
427	1	1	t	0	0	0	\N	\N	52	-1	1
427	6	6	f	0	0	0	test return value.js.sample	\N	26	-1	1
439	21	3	f	54	0	0	\N	\N	51	-1	1
427	6	6	f	0	0	0	Return a value from a script - for the command servlet	\N	27	-1	2
427	6	6	f	0	0	0	Return Value Example	\N	28	-1	2
427	21	3	f	46	0	0	\N	\N	51	-1	1
428	6	6	f	0	0	0	Web Scripts	\N	26	-1	1
428	6	6	f	0	0	0	URL addressable Web Services	\N	27	-1	2
428	6	6	f	0	0	0	URL addressable Web Services	\N	28	-1	2
428	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
429	1	1	t	0	0	0	\N	\N	52	-1	1
429	6	6	f	0	0	0		\N	54	-1	1
429	6	6	f	0	0	0	readme.html	\N	26	-1	1
429	6	6	f	0	0	0	What are Web Scripts and how do you develop them?	\N	27	-1	2
429	6	6	f	0	0	0		\N	28	-1	2
429	21	3	f	47	0	0	\N	\N	51	-1	1
430	1	1	t	0	0	0	\N	\N	52	-1	1
430	6	6	f	0	0	0		\N	54	-1	1
430	6	6	f	0	0	0	readme_ja.html	\N	26	-1	1
430	6	6	f	0	0	0	What are Web Scripts and how do you develop them?	\N	27	-1	2
430	6	6	f	0	0	0		\N	28	-1	2
430	21	3	f	48	0	0	\N	\N	51	-1	1
431	1	1	t	0	0	0	\N	\N	52	-1	1
431	6	6	f	0	0	0		\N	54	-1	1
431	6	6	f	0	0	0	readme_de.html	\N	26	-1	1
431	6	6	f	0	0	0	What are Web Scripts and how do you develop them?	\N	27	-1	2
431	6	6	f	0	0	0		\N	28	-1	2
431	21	3	f	49	0	0	\N	\N	51	-1	1
432	1	1	t	0	0	0	\N	\N	52	-1	1
432	6	6	f	0	0	0		\N	54	-1	1
432	6	6	f	0	0	0	readme_fr.html	\N	26	-1	1
432	6	6	f	0	0	0	What are Web Scripts and how do you develop them?	\N	27	-1	2
432	6	6	f	0	0	0		\N	28	-1	2
432	21	3	f	50	0	0	\N	\N	51	-1	1
433	6	6	f	0	0	0	org	\N	26	-1	1
433	6	6	f	0	0	0		\N	27	-1	2
433	6	6	f	0	0	0		\N	28	-1	2
433	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
434	6	6	f	0	0	0	alfresco	\N	26	-1	1
434	6	6	f	0	0	0		\N	27	-1	2
434	6	6	f	0	0	0		\N	28	-1	2
434	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
435	6	6	f	0	0	0	sample	\N	26	-1	1
435	6	6	f	0	0	0		\N	27	-1	2
435	6	6	f	0	0	0		\N	28	-1	2
435	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
436	1	1	t	0	0	0	\N	\N	52	-1	1
436	6	6	f	0	0	0		\N	54	-1	1
436	6	6	f	0	0	0	blogsearch.get.js	\N	26	-1	1
436	6	6	f	0	0	0		\N	27	-1	2
436	6	6	f	0	0	0	blogsearch.get.js	\N	28	-1	2
436	21	3	f	51	0	0	\N	\N	51	-1	1
437	1	1	t	0	0	0	\N	\N	52	-1	1
437	6	6	f	0	0	0		\N	54	-1	1
437	6	6	f	0	0	0	blogsearch.get.atom.ftl	\N	26	-1	1
437	6	6	f	0	0	0		\N	27	-1	2
437	6	6	f	0	0	0	blogsearch.get.atom.ftl	\N	28	-1	2
437	21	3	f	52	0	0	\N	\N	51	-1	1
438	1	1	t	0	0	0	\N	\N	52	-1	1
438	6	6	f	0	0	0		\N	54	-1	1
438	6	6	f	0	0	0	blogsearch.get.desc.xml	\N	26	-1	1
438	6	6	f	0	0	0		\N	27	-1	2
438	6	6	f	0	0	0	blogsearch.get.desc.xml	\N	28	-1	2
438	21	3	f	53	0	0	\N	\N	51	-1	1
439	1	1	t	0	0	0	\N	\N	52	-1	1
439	6	6	f	0	0	0		\N	54	-1	1
439	6	6	f	0	0	0	blogsearch.get.html.ftl	\N	26	-1	1
439	6	6	f	0	0	0		\N	27	-1	2
439	6	6	f	0	0	0	blogsearch.get.html.ftl	\N	28	-1	2
440	1	1	t	0	0	0	\N	\N	52	-1	1
440	6	6	f	0	0	0		\N	54	-1	1
440	6	6	f	0	0	0	blogsearch.get.html.400.ftl	\N	26	-1	1
440	6	6	f	0	0	0		\N	27	-1	2
440	6	6	f	0	0	0	blogsearch.get.html.400.ftl	\N	28	-1	2
440	21	3	f	55	0	0	\N	\N	51	-1	1
441	1	1	t	0	0	0	\N	\N	52	-1	1
441	6	6	f	0	0	0		\N	54	-1	1
441	6	6	f	0	0	0	blogsearch.get.atom.400.ftl	\N	26	-1	1
441	6	6	f	0	0	0		\N	27	-1	2
441	6	6	f	0	0	0	blogsearch.get.atom.400.ftl	\N	28	-1	2
441	21	3	f	56	0	0	\N	\N	51	-1	1
442	1	1	t	0	0	0	\N	\N	52	-1	1
442	6	6	f	0	0	0		\N	54	-1	1
442	6	6	f	0	0	0	categorysearch.get.js	\N	26	-1	1
442	6	6	f	0	0	0		\N	27	-1	2
442	6	6	f	0	0	0	categorysearch.get.js	\N	28	-1	2
442	21	3	f	57	0	0	\N	\N	51	-1	1
443	1	1	t	0	0	0	\N	\N	52	-1	1
443	6	6	f	0	0	0		\N	54	-1	1
443	6	6	f	0	0	0	categorysearch.get.atom.ftl	\N	26	-1	1
443	6	6	f	0	0	0		\N	27	-1	2
443	6	6	f	0	0	0	categorysearch.get.atom.ftl	\N	28	-1	2
443	21	3	f	58	0	0	\N	\N	51	-1	1
444	1	1	t	0	0	0	\N	\N	52	-1	1
444	6	6	f	0	0	0		\N	54	-1	1
444	6	6	f	0	0	0	categorysearch.get.desc.xml	\N	26	-1	1
444	6	6	f	0	0	0		\N	27	-1	2
444	6	6	f	0	0	0	categorysearch.get.desc.xml	\N	28	-1	2
444	21	3	f	59	0	0	\N	\N	51	-1	1
445	1	1	t	0	0	0	\N	\N	52	-1	1
445	6	6	f	0	0	0		\N	54	-1	1
445	6	6	f	0	0	0	categorysearch.get.html.ftl	\N	26	-1	1
445	6	6	f	0	0	0		\N	27	-1	2
445	6	6	f	0	0	0	categorysearch.get.html.ftl	\N	28	-1	2
445	21	3	f	60	0	0	\N	\N	51	-1	1
446	1	1	t	0	0	0	\N	\N	52	-1	1
446	6	6	f	0	0	0		\N	54	-1	1
446	6	6	f	0	0	0	categorysearch.get.html.404.ftl	\N	26	-1	1
446	6	6	f	0	0	0		\N	27	-1	2
446	6	6	f	0	0	0	categorysearch.get.html.404.ftl	\N	28	-1	2
446	21	3	f	61	0	0	\N	\N	51	-1	1
447	1	1	t	0	0	0	\N	\N	52	-1	1
447	6	6	f	0	0	0		\N	54	-1	1
447	6	6	f	0	0	0	categorysearch.get.atom.404.ftl	\N	26	-1	1
447	6	6	f	0	0	0		\N	27	-1	2
447	6	6	f	0	0	0	categorysearch.get.atom.404.ftl	\N	28	-1	2
447	21	3	f	62	0	0	\N	\N	51	-1	1
448	1	1	t	0	0	0	\N	\N	52	-1	1
448	6	6	f	0	0	0		\N	54	-1	1
448	6	6	f	0	0	0	folder.get.js	\N	26	-1	1
448	6	6	f	0	0	0		\N	27	-1	2
448	6	6	f	0	0	0	folder.get.js	\N	28	-1	2
448	21	3	f	63	0	0	\N	\N	51	-1	1
449	1	1	t	0	0	0	\N	\N	52	-1	1
449	6	6	f	0	0	0		\N	54	-1	1
449	6	6	f	0	0	0	folder.get.atom.ftl	\N	26	-1	1
449	6	6	f	0	0	0		\N	27	-1	2
449	6	6	f	0	0	0	folder.get.atom.ftl	\N	28	-1	2
449	21	3	f	64	0	0	\N	\N	51	-1	1
450	1	1	t	0	0	0	\N	\N	52	-1	1
450	6	6	f	0	0	0		\N	54	-1	1
450	6	6	f	0	0	0	folder.get.desc.xml	\N	26	-1	1
450	6	6	f	0	0	0		\N	27	-1	2
450	6	6	f	0	0	0	folder.get.desc.xml	\N	28	-1	2
450	21	3	f	65	0	0	\N	\N	51	-1	1
451	1	1	t	0	0	0	\N	\N	52	-1	1
451	6	6	f	0	0	0		\N	54	-1	1
451	6	6	f	0	0	0	folder.get.html.ftl	\N	26	-1	1
451	6	6	f	0	0	0		\N	27	-1	2
451	6	6	f	0	0	0	folder.get.html.ftl	\N	28	-1	2
451	21	3	f	66	0	0	\N	\N	51	-1	1
452	6	6	f	0	0	0	Web Scripts Extensions	\N	26	-1	1
452	6	6	f	0	0	0	Customized Web Scripts	\N	27	-1	2
452	6	6	f	0	0	0	URL addressable Web Service Extensions	\N	28	-1	2
452	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
453	1	1	t	0	0	0	\N	\N	52	-1	1
453	6	6	f	0	0	0		\N	54	-1	1
453	6	6	f	0	0	0	readme.html	\N	26	-1	1
453	6	6	f	0	0	0	How to customize an existing Web Script	\N	27	-1	2
453	6	6	f	0	0	0		\N	28	-1	2
453	21	3	f	67	0	0	\N	\N	51	-1	1
428	12	6	f	0	0	0	workspace://SpacesStore/e8e9ac89-2a54-4abb-89fa-b69bf364614e	\N	57	-1	1
452	12	6	f	0	0	0	workspace://SpacesStore/e8e9ac89-2a54-4abb-89fa-b69bf364614e	\N	57	-1	1
454	6	6	f	0	0	0	Models	\N	26	-1	1
454	6	6	f	0	0	0	Customized Models	\N	27	-1	2
454	6	6	f	0	0	0	Customized Models	\N	28	-1	2
454	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
456	6	6	f	0	0	0	inbound	\N	65	0	1
456	6	6	f	0	0	0	Specialise Type to Dictionary Model	\N	27	-1	2
456	6	6	f	0	0	0	Specialise Type to Dictionary Model	\N	28	-1	2
456	1	1	f	0	0	0	\N	\N	62	-1	1
456	1	1	f	0	0	0	\N	\N	63	-1	1
456	1	1	f	0	0	0	\N	\N	64	-1	1
457	1	1	f	0	0	0	\N	\N	68	-1	1
457	0	0	f	0	0	0	\N	\N	69	-1	1
457	6	6	f	0	0	0	composite-action	\N	70	-1	1
457	0	0	f	0	0	0	\N	\N	71	-1	1
458	1	1	f	0	0	0	\N	\N	74	-1	1
458	6	6	f	0	0	0	compare-mime-type	\N	70	-1	1
459	6	6	f	0	0	0	text/xml	\N	77	-1	1
459	6	6	f	0	0	0	value	\N	78	-1	1
460	1	1	f	0	0	0	\N	\N	68	-1	1
460	0	0	f	0	0	0	\N	\N	69	-1	1
460	6	6	f	0	0	0	specialise-type	\N	70	-1	1
460	0	0	f	0	0	0	\N	\N	71	-1	1
461	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}dictionaryModel	\N	77	-1	1
461	6	6	f	0	0	0	type-name	\N	78	-1	1
462	6	6	f	0	0	0	Messages	\N	26	-1	1
462	6	6	f	0	0	0	Customized Messages	\N	27	-1	2
462	6	6	f	0	0	0	Customized Messages	\N	28	-1	2
462	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
463	6	6	f	0	0	0	Web Client Extension	\N	26	-1	1
463	6	6	f	0	0	0	Customized Web Client	\N	27	-1	2
463	6	6	f	0	0	0	Customized Web Client	\N	28	-1	2
463	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
464	6	6	f	0	0	0	Workflow Definitions	\N	26	-1	1
464	6	6	f	0	0	0	Customized Workflow Process Definitions	\N	27	-1	2
464	6	6	f	0	0	0	Customized Workflow Process Definitions	\N	28	-1	2
464	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
466	6	6	f	0	0	0	inbound	\N	65	0	1
466	6	6	f	0	0	0	Specialise Type to Workflow Process Definition	\N	27	-1	2
466	6	6	f	0	0	0	Specialise Type to Workflow Process Definition	\N	28	-1	2
466	1	1	f	0	0	0	\N	\N	62	-1	1
466	1	1	f	0	0	0	\N	\N	63	-1	1
466	1	1	f	0	0	0	\N	\N	64	-1	1
467	1	1	f	0	0	0	\N	\N	68	-1	1
467	0	0	f	0	0	0	\N	\N	69	-1	1
467	6	6	f	0	0	0	composite-action	\N	70	-1	1
467	0	0	f	0	0	0	\N	\N	71	-1	1
468	1	1	f	0	0	0	\N	\N	74	-1	1
468	6	6	f	0	0	0	compare-mime-type	\N	70	-1	1
469	6	6	f	0	0	0	text/xml	\N	77	-1	1
469	6	6	f	0	0	0	value	\N	78	-1	1
470	1	1	f	0	0	0	\N	\N	68	-1	1
470	0	0	f	0	0	0	\N	\N	69	-1	1
470	6	6	f	0	0	0	specialise-type	\N	70	-1	1
470	0	0	f	0	0	0	\N	\N	71	-1	1
471	15	6	f	0	0	0	{http://www.alfresco.org/model/bpm/1.0}workflowDefinition	\N	77	-1	1
471	6	6	f	0	0	0	type-name	\N	78	-1	1
472	6	6	f	0	0	0	Tags	\N	26	-1	1
473	6	6	f	0	0	0	Sites	\N	26	-1	1
473	6	6	f	0	0	0	Site Collaboration Spaces	\N	27	-1	2
473	6	6	f	0	0	0	Sites	\N	28	-1	2
473	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
474	6	6	f	0	0	0	surf-config	\N	26	-1	1
474	6	6	f	0	0	0		\N	27	-1	2
474	2	3	f	0	0	0	\N	\N	82	-1	1
474	1	1	f	0	0	0	\N	\N	83	-1	1
474	1	1	f	0	0	0	\N	\N	84	-1	1
474	1	1	f	0	0	0	\N	\N	86	-1	1
474	1	1	f	0	0	0	\N	\N	87	-1	1
475	1	1	f	0	0	0	\N	\N	86	-1	1
475	1	1	f	0	0	0	\N	\N	87	-1	1
475	6	6	f	0	0	0	extensions	\N	26	-1	1
475	6	6	f	0	0	0		\N	27	-1	2
475	2	3	f	0	0	0	\N	\N	82	-1	1
475	1	1	f	0	0	0	\N	\N	83	-1	1
475	1	1	f	0	0	0	\N	\N	84	-1	1
476	1	1	f	0	0	0	\N	\N	86	-1	1
476	1	1	f	0	0	0	\N	\N	87	-1	1
476	6	6	f	0	0	0	module-deployments	\N	26	-1	1
476	6	6	f	0	0	0		\N	27	-1	2
476	2	3	f	0	0	0	\N	\N	82	-1	1
476	1	1	f	0	0	0	\N	\N	83	-1	1
476	1	1	f	0	0	0	\N	\N	84	-1	1
478	6	6	f	0	0	0	GROUP_ALFRESCO_ADMINISTRATORS	\N	90	-1	1
479	6	6	f	0	0	0	GROUP_EMAIL_CONTRIBUTORS	\N	90	-1	1
480	6	6	f	0	0	0	GROUP_SITE_ADMINISTRATORS	\N	90	-1	1
481	6	6	f	0	0	0	GROUP_ALFRESCO_SEARCH_ADMINISTRATORS	\N	90	-1	1
482	6	6	f	0	0	0	GROUP_ALFRESCO_MODEL_ADMINISTRATORS	\N	90	-1	1
483	6	6	f	0	0	0	GROUP_ALFRESCO_SYSTEM_ADMINISTRATORS	\N	90	-1	1
4	6	6	f	0	0	0	bcrypt10	\N	233	0	1
4	6	6	f	0	0	0	$2a$10$lyabGhB0Wf3iVyuqjijBeuwWv4sZ.jSG6vXbwf205O3uHhcqLMEDu	\N	232	-1	1
871	6	6	f	0	0	0	page.component-2-2.user~admin~dashboard.xml	\N	26	-1	1
871	6	6	f	0	0	0	admin	\N	40	-1	1
487	6	6	f	0	0	0	Remote Credentials	\N	26	-1	1
487	6	6	f	0	0	0	Root folder for Shared Remote Credentials	\N	27	-1	2
487	6	6	f	0	0	0	Remote Credentials	\N	28	-1	2
488	6	6	f	0	0	0	SyncSet Definitions	\N	26	-1	1
488	6	6	f	0	0	0	Root folder for SyncSet Definitions	\N	27	-1	2
488	6	6	f	0	0	0	SyncSet Definitions	\N	28	-1	2
489	6	6	f	0	0	0	Imap Configs	\N	26	-1	1
489	6	6	f	0	0	0	Imap Configs	\N	27	-1	2
489	6	6	f	0	0	0	Imap Configs	\N	28	-1	2
489	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
490	6	6	f	0	0	0	Templates	\N	26	-1	1
490	6	6	f	0	0	0	Templates for IMAP generated messages	\N	27	-1	2
490	6	6	f	0	0	0	Templates	\N	28	-1	2
490	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
491	1	1	t	0	0	0	\N	\N	52	-1	1
491	6	6	f	0	0	0		\N	54	-1	1
491	6	6	f	0	0	0	emailbody-textplain.ftl	\N	26	-1	1
491	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Default version	\N	27	-1	2
491	6	6	f	0	0	0	emailbody-textplain.ftl	\N	28	-1	2
491	21	3	f	68	0	0	\N	\N	51	-1	1
492	1	1	t	0	0	0	\N	\N	52	-1	1
492	6	6	f	0	0	0		\N	54	-1	1
492	6	6	f	0	0	0	emailbody-texthtml.ftl	\N	26	-1	1
492	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Default version	\N	27	-1	2
492	6	6	f	0	0	0	emailbody-texthtml.ftl	\N	28	-1	2
492	21	3	f	69	0	0	\N	\N	51	-1	1
493	6	6	f	0	0	0	Transfers	\N	26	-1	1
493	6	6	f	0	0	0	Folder used by the Transfer subsystem	\N	27	-1	2
493	6	6	f	0	0	0	Transfers	\N	28	-1	2
493	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
494	6	6	f	0	0	0	Transfer Target Groups	\N	26	-1	1
494	6	6	f	0	0	0	Folder containing groups of transfer targets	\N	27	-1	2
494	6	6	f	0	0	0	Transfer Target Groups	\N	28	-1	2
494	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
495	6	6	f	0	0	0	Default Group	\N	26	-1	1
495	6	6	f	0	0	0	Put your transfer targets in this folder	\N	27	-1	2
495	6	6	f	0	0	0	Default Group	\N	28	-1	2
495	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
496	6	6	f	0	0	0	Inbound Transfer Records	\N	26	-1	1
496	6	6	f	0	0	0	Folder containing records of inbound transfers	\N	27	-1	2
496	6	6	f	0	0	0	Inbound Transfer Records	\N	28	-1	2
496	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
497	6	6	f	0	0	0	Temp	\N	26	-1	1
497	6	6	f	0	0	0	Folder to store temporary nodes during transfer	\N	27	-1	2
497	6	6	f	0	0	0	Temp	\N	28	-1	2
497	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
498	6	6	f	0	0	0	Rendering Actions Space	\N	26	-1	1
498	6	6	f	0	0	0	A space used by the system to persist rendering actions.	\N	27	-1	2
498	6	6	f	0	0	0	Rendering Actions Space	\N	28	-1	2
499	6	6	f	0	0	0	Replication Actions Space	\N	26	-1	1
499	6	6	f	0	0	0	A space used by the system to persist replication actions.	\N	27	-1	2
499	6	6	f	0	0	0	Replication Actions Space	\N	28	-1	2
501	6	6	f	0	0	0	inbound	\N	65	0	1
501	6	6	f	0	0	0		\N	27	-1	3
501	6	6	f	0	0	0	Specialise child folders into Transfer Targets	\N	28	-1	3
501	1	1	f	0	0	0	\N	\N	62	-1	1
501	1	1	f	0	0	0	\N	\N	63	-1	1
501	1	1	f	0	0	0	\N	\N	64	-1	1
502	0	0	f	0	0	0	\N	\N	98	-1	1
502	7	6	f	0	0	0	2010-08-11T12:06:18.419Z	\N	99	-1	1
502	1	1	f	0	0	0	\N	\N	68	-1	1
502	7	6	f	0	0	0	2010-08-11T12:06:18.408Z	\N	100	-1	1
502	0	0	f	0	0	0	\N	\N	69	-1	1
502	6	6	f	0	0	0	Completed	\N	101	-1	1
502	6	6	f	0	0	0	composite-action	\N	70	-1	1
502	0	0	f	0	0	0	\N	\N	71	-1	1
504	0	0	f	0	0	0	\N	\N	98	-1	1
504	0	0	f	0	0	0	\N	\N	99	-1	1
504	1	1	f	0	0	0	\N	\N	68	-1	1
504	0	0	f	0	0	0	\N	\N	100	-1	1
504	0	0	f	0	0	0	\N	\N	69	-1	1
504	6	6	f	0	0	0	New	\N	101	-1	1
504	6	6	f	0	0	0	specialise-type	\N	70	-1	1
504	0	0	f	0	0	0	\N	\N	71	-1	1
505	15	6	f	0	0	0	{http://www.alfresco.org/model/transfer/1.0}transferTarget	\N	77	-1	1
505	6	6	f	0	0	0	type-name	\N	78	-1	1
506	1	1	f	0	0	0	\N	\N	74	-1	1
506	6	6	f	0	0	0	is-subtype	\N	70	-1	1
507	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}folder	\N	77	-1	1
507	6	6	f	0	0	0	type	\N	78	-1	1
508	6	6	f	0	0	0	Scheduled Actions	\N	26	-1	1
551	21	3	f	112	0	0	\N	\N	51	-1	1
508	6	6	f	0	0	0	Schedule of when persistent actions are executed	\N	27	-1	2
508	6	6	f	0	0	0	Scheduled Actions	\N	28	-1	2
509	1	1	t	0	0	0	\N	\N	52	-1	1
509	6	6	f	0	0	0	new-user-email.html.ftl	\N	26	-1	1
509	6	6	f	0	0	0	Email template used to inform new users of their accounts - Default version	\N	27	-1	2
509	6	6	f	0	0	0	new-user-email.html.ftl	\N	28	-1	2
509	21	3	f	70	0	0	\N	\N	51	-1	1
510	1	1	t	0	0	0	\N	\N	52	-1	1
510	6	6	f	0	0	0	new-user-email_fr.html.ftl	\N	26	-1	1
510	6	6	f	0	0	0	Email template used to inform new users of their accounts - French version	\N	27	-1	2
510	6	6	f	0	0	0	new-user-email_fr.html.ftl	\N	28	-1	2
510	21	3	f	71	0	0	\N	\N	51	-1	1
511	1	1	t	0	0	0	\N	\N	52	-1	1
511	6	6	f	0	0	0	new-user-email_es.html.ftl	\N	26	-1	1
511	6	6	f	0	0	0	Email template used to inform new users of their accounts - Spanish version	\N	27	-1	2
511	6	6	f	0	0	0	new-user-email_es.html.ftl	\N	28	-1	2
511	21	3	f	72	0	0	\N	\N	51	-1	1
512	1	1	t	0	0	0	\N	\N	52	-1	1
512	6	6	f	0	0	0	new-user-email_de.html.ftl	\N	26	-1	1
512	6	6	f	0	0	0	Email template used to inform new users of their accounts - German version	\N	27	-1	2
512	6	6	f	0	0	0	new-user-email_de.html.ftl	\N	28	-1	2
512	21	3	f	73	0	0	\N	\N	51	-1	1
513	1	1	t	0	0	0	\N	\N	52	-1	1
513	6	6	f	0	0	0	new-user-email_it.html.ftl	\N	26	-1	1
513	6	6	f	0	0	0	Email template used to inform new users of their accounts - Italian version	\N	27	-1	2
513	6	6	f	0	0	0	new-user-email_it.html.ftl	\N	28	-1	2
513	21	3	f	74	0	0	\N	\N	51	-1	1
514	1	1	t	0	0	0	\N	\N	52	-1	1
514	6	6	f	0	0	0	new-user-email_ja.html.ftl	\N	26	-1	1
514	6	6	f	0	0	0	Email template used to inform new users of their accounts - Japanese version	\N	27	-1	2
514	6	6	f	0	0	0	new-user-email_ja.html.ftl	\N	28	-1	2
514	21	3	f	75	0	0	\N	\N	51	-1	1
515	1	1	t	0	0	0	\N	\N	52	-1	1
515	6	6	f	0	0	0	new-user-email_nl.html.ftl	\N	26	-1	1
515	6	6	f	0	0	0	Email template used to inform new users of their accounts - Dutch version	\N	27	-1	2
515	6	6	f	0	0	0	new-user-email_nl.html.ftl	\N	28	-1	2
515	21	3	f	76	0	0	\N	\N	51	-1	1
516	1	1	t	0	0	0	\N	\N	52	-1	1
516	6	6	f	0	0	0	invite-email.html.ftl	\N	26	-1	1
516	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - Default version	\N	27	-1	2
516	6	6	f	0	0	0	invite-email.html.ftl	\N	28	-1	2
516	21	3	f	77	0	0	\N	\N	51	-1	1
517	1	1	t	0	0	0	\N	\N	52	-1	1
517	6	6	f	0	0	0	invite-email_fr.html.ftl	\N	26	-1	1
517	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - French version	\N	27	-1	2
517	6	6	f	0	0	0	invite-email_fr.html.ftl	\N	28	-1	2
517	21	3	f	78	0	0	\N	\N	51	-1	1
518	1	1	t	0	0	0	\N	\N	52	-1	1
518	6	6	f	0	0	0	invite-email_es.html.ftl	\N	26	-1	1
518	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - Spanish version	\N	27	-1	2
518	6	6	f	0	0	0	invite-email_es.html.ftl	\N	28	-1	2
518	21	3	f	79	0	0	\N	\N	51	-1	1
519	1	1	t	0	0	0	\N	\N	52	-1	1
519	6	6	f	0	0	0	invite-email_de.html.ftl	\N	26	-1	1
519	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - German version	\N	27	-1	2
519	6	6	f	0	0	0	invite-email_de.html.ftl	\N	28	-1	2
519	21	3	f	80	0	0	\N	\N	51	-1	1
520	1	1	t	0	0	0	\N	\N	52	-1	1
520	6	6	f	0	0	0	invite-email_it.html.ftl	\N	26	-1	1
520	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - Italian version	\N	27	-1	2
520	6	6	f	0	0	0	invite-email_it.html.ftl	\N	28	-1	2
520	21	3	f	81	0	0	\N	\N	51	-1	1
521	1	1	t	0	0	0	\N	\N	52	-1	1
521	6	6	f	0	0	0	invite-email_ja.html.ftl	\N	26	-1	1
521	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - Japanese version	\N	27	-1	2
521	6	6	f	0	0	0	invite-email_ja.html.ftl	\N	28	-1	2
521	21	3	f	82	0	0	\N	\N	51	-1	1
522	1	1	t	0	0	0	\N	\N	52	-1	1
522	6	6	f	0	0	0	invite-email_nl.html.ftl	\N	26	-1	1
522	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - Dutch version	\N	27	-1	2
522	6	6	f	0	0	0	invite-email_nl.html.ftl	\N	28	-1	2
522	21	3	f	83	0	0	\N	\N	51	-1	1
523	1	1	t	0	0	0	\N	\N	52	-1	1
523	6	6	f	0	0	0	invite-email-add-direct.html.ftl	\N	26	-1	1
523	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - Default version	\N	27	-1	2
523	6	6	f	0	0	0	invite-email-add-direct.html.ftl	\N	28	-1	2
523	21	3	f	84	0	0	\N	\N	51	-1	1
524	1	1	t	0	0	0	\N	\N	52	-1	1
524	6	6	f	0	0	0	invite-email-add-direct.html_fr.ftl	\N	26	-1	1
524	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - French version	\N	27	-1	2
524	6	6	f	0	0	0	invite-email-add-direct.html_fr.ftl	\N	28	-1	2
524	21	3	f	85	0	0	\N	\N	51	-1	1
525	1	1	t	0	0	0	\N	\N	52	-1	1
525	6	6	f	0	0	0	invite-email-add-direct.html_es.ftl	\N	26	-1	1
525	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - Spanish version	\N	27	-1	2
525	6	6	f	0	0	0	invite-email-add-direct.html_es.ftl	\N	28	-1	2
525	21	3	f	86	0	0	\N	\N	51	-1	1
526	1	1	t	0	0	0	\N	\N	52	-1	1
526	6	6	f	0	0	0	invite-email-add-direct.html_de.ftl	\N	26	-1	1
526	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - German version	\N	27	-1	2
526	6	6	f	0	0	0	invite-email-add-direct.html_de.ftl	\N	28	-1	2
526	21	3	f	87	0	0	\N	\N	51	-1	1
527	1	1	t	0	0	0	\N	\N	52	-1	1
527	6	6	f	0	0	0	invite-email-add-direct.html_it.ftl	\N	26	-1	1
527	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - Italian version	\N	27	-1	2
527	6	6	f	0	0	0	invite-email-add-direct.html_it.ftl	\N	28	-1	2
527	21	3	f	88	0	0	\N	\N	51	-1	1
528	1	1	t	0	0	0	\N	\N	52	-1	1
528	6	6	f	0	0	0	invite-email-add-direct.html_ja.ftl	\N	26	-1	1
528	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - Japanese version	\N	27	-1	2
528	6	6	f	0	0	0	invite-email-add-direct.html_ja.ftl	\N	28	-1	2
528	21	3	f	89	0	0	\N	\N	51	-1	1
529	1	1	t	0	0	0	\N	\N	52	-1	1
529	6	6	f	0	0	0	invite-email-add-direct.html_nl.ftl	\N	26	-1	1
529	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - Dutch version	\N	27	-1	2
529	6	6	f	0	0	0	invite-email-add-direct.html_nl.ftl	\N	28	-1	2
529	21	3	f	90	0	0	\N	\N	51	-1	1
530	1	1	t	0	0	0	\N	\N	52	-1	1
530	6	6	f	0	0	0	invite-email-moderated.html.ftl	\N	26	-1	1
530	6	6	f	0	0	0	Email template used to generate the request to join site email for Alfresco Share - Default version	\N	27	-1	2
530	6	6	f	0	0	0	invite-email-moderated.html.ftl	\N	28	-1	2
530	21	3	f	91	0	0	\N	\N	51	-1	1
531	1	1	t	0	0	0	\N	\N	52	-1	1
531	6	6	f	0	0	0	notify_user_email.html.ftl	\N	26	-1	1
531	6	6	f	0	0	0	Email template for notifying users from a rule or action - Default version	\N	27	-1	2
552	1	1	t	0	0	0	\N	\N	52	-1	1
531	6	6	f	0	0	0	notify_user_email.html.ftl	\N	28	-1	2
531	21	3	f	92	0	0	\N	\N	51	-1	1
532	1	1	t	0	0	0	\N	\N	52	-1	1
532	6	6	f	0	0	0	notify_user_email_de.html.ftl	\N	26	-1	1
532	6	6	f	0	0	0	Email template for notifying users from a rule or action - German version	\N	27	-1	2
532	6	6	f	0	0	0	notify_user_email_de.html.ftl	\N	28	-1	2
532	21	3	f	93	0	0	\N	\N	51	-1	1
533	1	1	t	0	0	0	\N	\N	52	-1	1
533	6	6	f	0	0	0	notify_user_email_es.html.ftl	\N	26	-1	1
533	6	6	f	0	0	0	Email template for notifying users from a rule or action - Spanish version	\N	27	-1	2
533	6	6	f	0	0	0	notify_user_email_es.html.ftl	\N	28	-1	2
533	21	3	f	94	0	0	\N	\N	51	-1	1
534	1	1	t	0	0	0	\N	\N	52	-1	1
534	6	6	f	0	0	0	notify_user_email_fr.html.ftl	\N	26	-1	1
534	6	6	f	0	0	0	Email template for notifying users from a rule or action - French version	\N	27	-1	2
534	6	6	f	0	0	0	notify_user_email_fr.html.ftl	\N	28	-1	2
534	21	3	f	95	0	0	\N	\N	51	-1	1
535	1	1	t	0	0	0	\N	\N	52	-1	1
535	6	6	f	0	0	0	notify_user_email_it.html.ftl	\N	26	-1	1
535	6	6	f	0	0	0	Email template for notifying users from a rule or action - Italian version	\N	27	-1	2
535	6	6	f	0	0	0	notify_user_email_it.html.ftl	\N	28	-1	2
535	21	3	f	96	0	0	\N	\N	51	-1	1
536	1	1	t	0	0	0	\N	\N	52	-1	1
536	6	6	f	0	0	0	notify_user_email_ja.html.ftl	\N	26	-1	1
536	6	6	f	0	0	0	Email template for notifying users from a rule or action - Japanese version	\N	27	-1	2
536	6	6	f	0	0	0	notify_user_email_ja.html.ftl	\N	28	-1	2
536	21	3	f	97	0	0	\N	\N	51	-1	1
537	1	1	t	0	0	0	\N	\N	52	-1	1
537	6	6	f	0	0	0	notify_user_email_nl.html.ftl	\N	26	-1	1
537	6	6	f	0	0	0	Email template for notifying users from a rule or action - Dutch version	\N	27	-1	2
537	6	6	f	0	0	0	notify_user_email_nl.html.ftl	\N	28	-1	2
537	21	3	f	98	0	0	\N	\N	51	-1	1
538	1	1	t	0	0	0	\N	\N	52	-1	1
538	6	6	f	0	0	0		\N	54	-1	1
538	6	6	f	0	0	0	emailbody_textplain_share.ftl	\N	26	-1	1
538	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Default version	\N	27	-1	2
538	6	6	f	0	0	0	emailbody_textplain_share.ftl	\N	28	-1	2
538	21	3	f	99	0	0	\N	\N	51	-1	1
539	1	1	t	0	0	0	\N	\N	52	-1	1
539	6	6	f	0	0	0		\N	54	-1	1
539	6	6	f	0	0	0	emailbody_textplain_alfresco.ftl	\N	26	-1	1
539	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Default version	\N	27	-1	2
539	6	6	f	0	0	0	emailbody_textplain_alfresco.ftl	\N	28	-1	2
539	21	3	f	100	0	0	\N	\N	51	-1	1
540	1	1	t	0	0	0	\N	\N	52	-1	1
540	6	6	f	0	0	0		\N	54	-1	1
540	6	6	f	0	0	0	emailbody_texthtml_alfresco.ftl	\N	26	-1	1
540	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Default version	\N	27	-1	2
540	6	6	f	0	0	0	emailbody_texthtml_alfresco.ftl	\N	28	-1	2
540	21	3	f	101	0	0	\N	\N	51	-1	1
541	1	1	t	0	0	0	\N	\N	52	-1	1
541	6	6	f	0	0	0		\N	54	-1	1
541	6	6	f	0	0	0	emailbody_texthtml_share.ftl	\N	26	-1	1
541	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Default version	\N	27	-1	2
541	6	6	f	0	0	0	emailbody_texthtml_share.ftl	\N	28	-1	2
541	21	3	f	102	0	0	\N	\N	51	-1	1
542	1	1	t	0	0	0	\N	\N	52	-1	1
542	6	6	f	0	0	0		\N	54	-1	1
542	6	6	f	0	0	0	emailbody_textplain_share_de.ftl	\N	26	-1	1
542	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - German version	\N	27	-1	2
542	6	6	f	0	0	0	emailbody_textplain_share_de.ftl	\N	28	-1	2
542	21	3	f	103	0	0	\N	\N	51	-1	1
543	1	1	t	0	0	0	\N	\N	52	-1	1
543	6	6	f	0	0	0		\N	54	-1	1
543	6	6	f	0	0	0	emailbody_textplain_alfresco_de.ftl	\N	26	-1	1
543	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - German version	\N	27	-1	2
543	6	6	f	0	0	0	emailbody_textplain_alfresco_de.ftl	\N	28	-1	2
543	21	3	f	104	0	0	\N	\N	51	-1	1
544	1	1	t	0	0	0	\N	\N	52	-1	1
544	6	6	f	0	0	0		\N	54	-1	1
544	6	6	f	0	0	0	emailbody_texthtml_alfresco_de.ftl	\N	26	-1	1
544	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - German version	\N	27	-1	2
544	6	6	f	0	0	0	emailbody_texthtml_alfresco_de.ftl	\N	28	-1	2
544	21	3	f	105	0	0	\N	\N	51	-1	1
545	1	1	t	0	0	0	\N	\N	52	-1	1
545	6	6	f	0	0	0		\N	54	-1	1
545	6	6	f	0	0	0	emailbody_texthtml_share_de.ftl	\N	26	-1	1
545	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - German version	\N	27	-1	2
545	6	6	f	0	0	0	emailbody_texthtml_share_de.ftl	\N	28	-1	2
545	21	3	f	106	0	0	\N	\N	51	-1	1
546	1	1	t	0	0	0	\N	\N	52	-1	1
546	6	6	f	0	0	0		\N	54	-1	1
546	6	6	f	0	0	0	emailbody_textplain_share_es.ftl	\N	26	-1	1
546	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Spanish version	\N	27	-1	2
546	6	6	f	0	0	0	emailbody_textplain_share_es.ftl	\N	28	-1	2
546	21	3	f	107	0	0	\N	\N	51	-1	1
547	1	1	t	0	0	0	\N	\N	52	-1	1
547	6	6	f	0	0	0		\N	54	-1	1
547	6	6	f	0	0	0	emailbody_textplain_alfresco_es.ftl	\N	26	-1	1
547	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Spanish version	\N	27	-1	2
547	6	6	f	0	0	0	emailbody_textplain_alfresco_es.ftl	\N	28	-1	2
547	21	3	f	108	0	0	\N	\N	51	-1	1
548	1	1	t	0	0	0	\N	\N	52	-1	1
548	6	6	f	0	0	0		\N	54	-1	1
548	6	6	f	0	0	0	emailbody_texthtml_alfresco_es.ftl	\N	26	-1	1
548	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Spanish version	\N	27	-1	2
548	6	6	f	0	0	0	emailbody_texthtml_alfresco_es.ftl	\N	28	-1	2
548	21	3	f	109	0	0	\N	\N	51	-1	1
549	1	1	t	0	0	0	\N	\N	52	-1	1
549	6	6	f	0	0	0		\N	54	-1	1
549	6	6	f	0	0	0	emailbody_texthtml_share_es.ftl	\N	26	-1	1
549	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Spanish version	\N	27	-1	2
549	6	6	f	0	0	0	emailbody_texthtml_share_es.ftl	\N	28	-1	2
549	21	3	f	110	0	0	\N	\N	51	-1	1
550	1	1	t	0	0	0	\N	\N	52	-1	1
550	6	6	f	0	0	0		\N	54	-1	1
550	6	6	f	0	0	0	emailbody_textplain_share_fr.ftl	\N	26	-1	1
550	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - French version	\N	27	-1	2
550	6	6	f	0	0	0	emailbody_textplain_share_fr.ftl	\N	28	-1	2
550	21	3	f	111	0	0	\N	\N	51	-1	1
551	1	1	t	0	0	0	\N	\N	52	-1	1
551	6	6	f	0	0	0		\N	54	-1	1
551	6	6	f	0	0	0	emailbody_textplain_alfresco_fr.ftl	\N	26	-1	1
551	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - French version	\N	27	-1	2
551	6	6	f	0	0	0	emailbody_textplain_alfresco_fr.ftl	\N	28	-1	2
552	6	6	f	0	0	0		\N	54	-1	1
552	6	6	f	0	0	0	emailbody_texthtml_alfresco_fr.ftl	\N	26	-1	1
552	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - French version	\N	27	-1	2
552	6	6	f	0	0	0	emailbody_texthtml_alfresco_fr.ftl	\N	28	-1	2
552	21	3	f	113	0	0	\N	\N	51	-1	1
553	1	1	t	0	0	0	\N	\N	52	-1	1
553	6	6	f	0	0	0		\N	54	-1	1
553	6	6	f	0	0	0	emailbody_texthtml_share_fr.ftl	\N	26	-1	1
553	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - French version	\N	27	-1	2
553	6	6	f	0	0	0	emailbody_texthtml_share_fr.ftl	\N	28	-1	2
553	21	3	f	114	0	0	\N	\N	51	-1	1
554	1	1	t	0	0	0	\N	\N	52	-1	1
554	6	6	f	0	0	0		\N	54	-1	1
554	6	6	f	0	0	0	emailbody_textplain_share_it.ftl	\N	26	-1	1
554	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Italian version	\N	27	-1	2
554	6	6	f	0	0	0	emailbody_textplain_share_it.ftl	\N	28	-1	2
554	21	3	f	115	0	0	\N	\N	51	-1	1
555	1	1	t	0	0	0	\N	\N	52	-1	1
555	6	6	f	0	0	0		\N	54	-1	1
555	6	6	f	0	0	0	emailbody_textplain_alfresco_it.ftl	\N	26	-1	1
555	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Italian version	\N	27	-1	2
555	6	6	f	0	0	0	emailbody_textplain_alfresco_it.ftl	\N	28	-1	2
555	21	3	f	116	0	0	\N	\N	51	-1	1
556	1	1	t	0	0	0	\N	\N	52	-1	1
556	6	6	f	0	0	0		\N	54	-1	1
556	6	6	f	0	0	0	emailbody_texthtml_alfresco_it.ftl	\N	26	-1	1
556	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Italian version	\N	27	-1	2
556	6	6	f	0	0	0	emailbody_texthtml_alfresco_it.ftl	\N	28	-1	2
556	21	3	f	117	0	0	\N	\N	51	-1	1
557	1	1	t	0	0	0	\N	\N	52	-1	1
557	6	6	f	0	0	0		\N	54	-1	1
557	6	6	f	0	0	0	emailbody_texthtml_share_it.ftl	\N	26	-1	1
557	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Italian version	\N	27	-1	2
557	6	6	f	0	0	0	emailbody_texthtml_share_it.ftl	\N	28	-1	2
557	21	3	f	118	0	0	\N	\N	51	-1	1
558	1	1	t	0	0	0	\N	\N	52	-1	1
558	6	6	f	0	0	0		\N	54	-1	1
558	6	6	f	0	0	0	emailbody_textplain_share_ja.ftl	\N	26	-1	1
558	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Japanese version	\N	27	-1	2
558	6	6	f	0	0	0	emailbody_textplain_share_ja.ftl	\N	28	-1	2
558	21	3	f	119	0	0	\N	\N	51	-1	1
559	1	1	t	0	0	0	\N	\N	52	-1	1
559	6	6	f	0	0	0		\N	54	-1	1
559	6	6	f	0	0	0	emailbody_textplain_alfresco_ja.ftl	\N	26	-1	1
559	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Japanese version	\N	27	-1	2
559	6	6	f	0	0	0	emailbody_textplain_alfresco_ja.ftl	\N	28	-1	2
559	21	3	f	120	0	0	\N	\N	51	-1	1
560	1	1	t	0	0	0	\N	\N	52	-1	1
560	6	6	f	0	0	0		\N	54	-1	1
560	6	6	f	0	0	0	emailbody_texthtml_alfresco_ja.ftl	\N	26	-1	1
560	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Japanese version	\N	27	-1	2
560	6	6	f	0	0	0	emailbody_texthtml_alfresco_ja.ftl	\N	28	-1	2
560	21	3	f	121	0	0	\N	\N	51	-1	1
561	1	1	t	0	0	0	\N	\N	52	-1	1
561	6	6	f	0	0	0		\N	54	-1	1
561	6	6	f	0	0	0	emailbody_texthtml_share_ja.ftl	\N	26	-1	1
580	1	1	t	0	0	0	\N	\N	52	-1	1
561	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Japanese version	\N	27	-1	2
561	6	6	f	0	0	0	emailbody_texthtml_share_ja.ftl	\N	28	-1	2
561	21	3	f	122	0	0	\N	\N	51	-1	1
562	1	1	t	0	0	0	\N	\N	52	-1	1
562	6	6	f	0	0	0		\N	54	-1	1
562	6	6	f	0	0	0	emailbody_textplain_share_nb.ftl	\N	26	-1	1
562	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Norwegian Bokmal version	\N	27	-1	2
562	6	6	f	0	0	0	emailbody_textplain_share_nb.ftl	\N	28	-1	2
562	21	3	f	123	0	0	\N	\N	51	-1	1
563	1	1	t	0	0	0	\N	\N	52	-1	1
563	6	6	f	0	0	0		\N	54	-1	1
563	6	6	f	0	0	0	emailbody_textplain_alfresco_nb.ftl	\N	26	-1	1
563	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Norwegian Bokmal version	\N	27	-1	2
563	6	6	f	0	0	0	emailbody_textplain_alfresco_nb.ftl	\N	28	-1	2
563	21	3	f	124	0	0	\N	\N	51	-1	1
564	1	1	t	0	0	0	\N	\N	52	-1	1
564	6	6	f	0	0	0		\N	54	-1	1
564	6	6	f	0	0	0	emailbody_texthtml_alfresco_nb.ftl	\N	26	-1	1
564	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Norwegian Bokmal version	\N	27	-1	2
564	6	6	f	0	0	0	emailbody_texthtml_alfresco_nb.ftl	\N	28	-1	2
564	21	3	f	125	0	0	\N	\N	51	-1	1
565	1	1	t	0	0	0	\N	\N	52	-1	1
565	6	6	f	0	0	0		\N	54	-1	1
565	6	6	f	0	0	0	emailbody_texthtml_share_nb.ftl	\N	26	-1	1
565	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Norwegian Bokmal version	\N	27	-1	2
565	6	6	f	0	0	0	emailbody_texthtml_share_nb.ftl	\N	28	-1	2
565	21	3	f	126	0	0	\N	\N	51	-1	1
566	1	1	t	0	0	0	\N	\N	52	-1	1
566	6	6	f	0	0	0		\N	54	-1	1
566	6	6	f	0	0	0	emailbody_textplain_share_nl.ftl	\N	26	-1	1
566	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Dutch version	\N	27	-1	2
566	6	6	f	0	0	0	emailbody_textplain_share_nl.ftl	\N	28	-1	2
566	21	3	f	127	0	0	\N	\N	51	-1	1
567	1	1	t	0	0	0	\N	\N	52	-1	1
567	6	6	f	0	0	0		\N	54	-1	1
567	6	6	f	0	0	0	emailbody_textplain_alfresco_nl.ftl	\N	26	-1	1
567	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Dutch version	\N	27	-1	2
567	6	6	f	0	0	0	emailbody_textplain_alfresco_nl.ftl	\N	28	-1	2
567	21	3	f	128	0	0	\N	\N	51	-1	1
568	1	1	t	0	0	0	\N	\N	52	-1	1
568	6	6	f	0	0	0		\N	54	-1	1
568	6	6	f	0	0	0	emailbody_texthtml_alfresco_nl.ftl	\N	26	-1	1
568	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Dutch version	\N	27	-1	2
568	6	6	f	0	0	0	emailbody_texthtml_alfresco_nl.ftl	\N	28	-1	2
568	21	3	f	129	0	0	\N	\N	51	-1	1
569	1	1	t	0	0	0	\N	\N	52	-1	1
569	6	6	f	0	0	0		\N	54	-1	1
569	6	6	f	0	0	0	emailbody_texthtml_share_nl.ftl	\N	26	-1	1
569	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Dutch version	\N	27	-1	2
569	6	6	f	0	0	0	emailbody_texthtml_share_nl.ftl	\N	28	-1	2
569	21	3	f	130	0	0	\N	\N	51	-1	1
570	1	1	t	0	0	0	\N	\N	52	-1	1
570	6	6	f	0	0	0		\N	54	-1	1
570	6	6	f	0	0	0	emailbody_textplain_share_pt_BR.ftl	\N	26	-1	1
570	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Brazilian Portuguese version	\N	27	-1	2
570	6	6	f	0	0	0	emailbody_textplain_share_pt_BR.ftl	\N	28	-1	2
570	21	3	f	131	0	0	\N	\N	51	-1	1
571	1	1	t	0	0	0	\N	\N	52	-1	1
571	6	6	f	0	0	0		\N	54	-1	1
571	6	6	f	0	0	0	emailbody_textplain_alfresco_pt_BR.ftl	\N	26	-1	1
571	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Brazilian Portuguese version	\N	27	-1	2
571	6	6	f	0	0	0	emailbody_textplain_alfresco_pt_BR.ftl	\N	28	-1	2
571	21	3	f	132	0	0	\N	\N	51	-1	1
572	1	1	t	0	0	0	\N	\N	52	-1	1
572	6	6	f	0	0	0		\N	54	-1	1
572	6	6	f	0	0	0	emailbody_texthtml_alfresco_pt_BR.ftl	\N	26	-1	1
572	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Brazilian Portuguese version	\N	27	-1	2
572	6	6	f	0	0	0	emailbody_texthtml_alfresco_pt_BR.ftl	\N	28	-1	2
572	21	3	f	133	0	0	\N	\N	51	-1	1
573	1	1	t	0	0	0	\N	\N	52	-1	1
573	6	6	f	0	0	0		\N	54	-1	1
573	6	6	f	0	0	0	emailbody_texthtml_share_pt_BR.ftl	\N	26	-1	1
573	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Brazilian Portuguese version	\N	27	-1	2
573	6	6	f	0	0	0	emailbody_texthtml_share_pt_BR.ftl	\N	28	-1	2
573	21	3	f	134	0	0	\N	\N	51	-1	1
574	1	1	t	0	0	0	\N	\N	52	-1	1
574	6	6	f	0	0	0		\N	54	-1	1
574	6	6	f	0	0	0	emailbody_textplain_share_ru.ftl	\N	26	-1	1
574	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Russian version	\N	27	-1	2
574	6	6	f	0	0	0	emailbody_textplain_share_ru.ftl	\N	28	-1	2
574	21	3	f	135	0	0	\N	\N	51	-1	1
575	1	1	t	0	0	0	\N	\N	52	-1	1
575	6	6	f	0	0	0		\N	54	-1	1
575	6	6	f	0	0	0	emailbody_textplain_alfresco_ru.ftl	\N	26	-1	1
575	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Russian version	\N	27	-1	2
575	6	6	f	0	0	0	emailbody_textplain_alfresco_ru.ftl	\N	28	-1	2
575	21	3	f	136	0	0	\N	\N	51	-1	1
576	1	1	t	0	0	0	\N	\N	52	-1	1
576	6	6	f	0	0	0		\N	54	-1	1
576	6	6	f	0	0	0	emailbody_texthtml_alfresco_ru.ftl	\N	26	-1	1
576	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Russian version	\N	27	-1	2
576	6	6	f	0	0	0	emailbody_texthtml_alfresco_ru.ftl	\N	28	-1	2
576	21	3	f	137	0	0	\N	\N	51	-1	1
577	1	1	t	0	0	0	\N	\N	52	-1	1
577	6	6	f	0	0	0		\N	54	-1	1
577	6	6	f	0	0	0	emailbody_texthtml_share_ru.ftl	\N	26	-1	1
577	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Russian version	\N	27	-1	2
577	6	6	f	0	0	0	emailbody_texthtml_share_ru.ftl	\N	28	-1	2
577	21	3	f	138	0	0	\N	\N	51	-1	1
578	1	1	t	0	0	0	\N	\N	52	-1	1
578	6	6	f	0	0	0		\N	54	-1	1
578	6	6	f	0	0	0	emailbody_textplain_share_zh_CN.ftl	\N	26	-1	1
578	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Simplified Chinese version	\N	27	-1	2
578	6	6	f	0	0	0	emailbody_textplain_share_zh_CN.ftl	\N	28	-1	2
578	21	3	f	139	0	0	\N	\N	51	-1	1
579	1	1	t	0	0	0	\N	\N	52	-1	1
579	6	6	f	0	0	0		\N	54	-1	1
579	6	6	f	0	0	0	emailbody_textplain_alfresco_zh_CN.ftl	\N	26	-1	1
579	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Simplified Chinese version	\N	27	-1	2
579	6	6	f	0	0	0	emailbody_textplain_alfresco_zh_CN.ftl	\N	28	-1	2
579	21	3	f	140	0	0	\N	\N	51	-1	1
580	6	6	f	0	0	0		\N	54	-1	1
580	6	6	f	0	0	0	emailbody_texthtml_alfresco_zh_CN.ftl	\N	26	-1	1
580	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Simplified Chinese version	\N	27	-1	2
580	6	6	f	0	0	0	emailbody_texthtml_alfresco_zh_CN.ftl	\N	28	-1	2
580	21	3	f	141	0	0	\N	\N	51	-1	1
581	1	1	t	0	0	0	\N	\N	52	-1	1
581	6	6	f	0	0	0		\N	54	-1	1
581	6	6	f	0	0	0	emailbody_texthtml_share_zh_CN.ftl	\N	26	-1	1
581	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Simplified Chinese version	\N	27	-1	2
581	6	6	f	0	0	0	emailbody_texthtml_share_zh_CN.ftl	\N	28	-1	2
581	21	3	f	142	0	0	\N	\N	51	-1	1
582	6	6	f	0	0	0	Downloads	\N	26	-1	1
582	6	6	f	0	0	0	Root folder for downloads	\N	27	-1	2
582	6	6	f	0	0	0	Downloads	\N	28	-1	2
583	1	1	t	0	0	0	\N	\N	52	-1	1
583	6	6	f	0	0	0	smartFoldersExample.json	\N	26	-1	1
583	6	6	f	0	0	0	Smart Folder Template Sample	\N	27	-1	2
583	6	6	f	0	0	0	Smart Folder Template Sample	\N	28	-1	2
583	21	3	f	143	0	0	\N	\N	51	-1	1
7	21	3	f	144	0	0	\N	\N	104	-1	1
7	1	1	f	0	0	0	\N	\N	86	-1	1
7	1	1	f	0	0	0	\N	\N	87	-1	1
7	6	6	f	0	0	0	ENTERPRISE	\N	105	-1	1
7	6	6	f	0	0	0	Main Repository	\N	16	-1	1
584	1	1	t	0	0	0	\N	\N	52	-1	1
584	6	6	f	0	0	0	Alfresco	\N	54	-1	1
584	6	6	f	0	0	0	start-pooled-review-workflow.js	\N	26	-1	1
584	6	6	f	0	0	0	Starts the Pooled Review and Approve workflow for all members of the site the document belongs to	\N	27	-1	2
584	6	6	f	0	0	0	Start Pooled Review and Approve Workflow	\N	28	-1	2
584	21	3	f	145	0	0	\N	\N	51	-1	1
586	6	6	f	0	0	0	4f78dc6e000a27fc3bb7e9c70aa6b5d9	\N	26	-1	1
586	6	6	f	0	0	0	GROUP_site_swsdp	\N	90	-1	1
586	6	6	f	0	0	0	site_swsdp	\N	112	-1	1
587	6	6	f	0	0	0	APP.SHARE	\N	26	-1	1
588	6	6	f	0	0	0	9e5cb3fa1850083495559ca2a4ca2de9	\N	26	-1	1
588	6	6	f	0	0	0	GROUP_site_swsdp_SiteManager	\N	90	-1	1
588	6	6	f	0	0	0	site_swsdp_SiteManager	\N	112	-1	1
588	3	3	f	3147235388	0	0	\N	\N	94	-1	1
588	3	3	f	11	0	0	\N	\N	96	-1	1
589	6	6	f	0	0	0	58d3dfc926fbcb0ce0a1213c37dc4711	\N	26	-1	1
589	6	6	f	0	0	0	GROUP_site_swsdp_SiteCollaborator	\N	90	-1	1
589	6	6	f	0	0	0	site_swsdp_SiteCollaborator	\N	112	-1	1
589	3	3	f	586215559	0	0	\N	\N	94	-1	1
589	3	3	f	11	0	0	\N	\N	96	-1	1
590	6	6	f	0	0	0	5b487fd6a02f7430721726163ba0daa9	\N	26	-1	1
590	6	6	f	0	0	0	GROUP_site_swsdp_SiteContributor	\N	90	-1	1
590	6	6	f	0	0	0	site_swsdp_SiteContributor	\N	112	-1	1
590	3	3	f	1144403915	0	0	\N	\N	94	-1	1
590	3	3	f	11	0	0	\N	\N	96	-1	1
591	6	6	f	0	0	0	73714588eb587e2a207a436130080c9e	\N	26	-1	1
591	6	6	f	0	0	0	GROUP_site_swsdp_SiteConsumer	\N	90	-1	1
591	6	6	f	0	0	0	site_swsdp_SiteConsumer	\N	112	-1	1
591	3	3	f	630299707	0	0	\N	\N	94	-1	1
591	3	3	f	11	0	0	\N	\N	96	-1	1
32	3	3	f	322806322	0	0	\N	\N	94	-1	1
32	3	3	f	11	0	0	\N	\N	96	-1	1
592	0	0	f	0	0	0	\N	\N	113	-1	1
728	6	6	f	0	0	0	modules	\N	26	-1	1
729	6	6	f	0	0	0	alfresco-aos-module	\N	26	-1	1
729	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005322e302e3078	227	-1	1
729	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005322e302e3078	228	-1	1
592	1	1	f	0	0	0	\N	\N	6	-1	1
592	1	1	f	0	0	0	\N	\N	7	-1	1
592	1	1	f	0	0	0	\N	\N	8	-1	1
592	6	6	f	0	0	0	abeecher	\N	9	-1	1
592	6	6	f	0	0	0	0eeb8b5ee6e96f1a8443edfb1dfc36ad	\N	10	-1	1
592	1	1	f	0	0	0	\N	\N	11	-1	1
593	0	0	f	0	0	0	\N	\N	113	-1	1
593	1	1	f	0	0	0	\N	\N	6	-1	1
593	1	1	f	0	0	0	\N	\N	7	-1	1
593	1	1	f	0	0	0	\N	\N	8	-1	1
593	6	6	f	0	0	0	mjackson	\N	9	-1	1
593	6	6	f	0	0	0	0eeb8b5ee6e96f1a8443edfb1dfc36ad	\N	10	-1	1
593	1	1	f	0	0	0	\N	\N	11	-1	1
594	6	6	f	0	0	0		\N	129	-1	1
594	6	6	f	0	0	0	Helping to design the look and feel of the new web site	\N	130	-1	1
594	3	3	f	440	0	0	\N	\N	131	-1	1
594	7	6	f	0	0	0	2011-02-15T20:20:13.432Z	\N	132	-1	1
594	3	3	f	8382006	0	0	\N	\N	36	-1	1
594	6	6	f	0	0	0	abeecher	\N	37	-1	1
594	6	6	f	0	0	0	abeecher@example.com	\N	38	-1	1
594	6	6	f	0	0	0	userHomesHomeFolderProvider	\N	39	-1	1
594	6	6	f	0	0	0	abeecher	\N	40	-1	1
594	6	6	f	0	0	0	Beecher	\N	41	-1	1
594	6	6	f	0	0	0	Alice	\N	43	-1	1
594	6	6	f	0	0	0	Graphic Designer	\N	114	-1	1
594	6	6	f	0	0	0	Tilbury, UK	\N	115	-1	1
594	6	6	f	0	0	0	0112211001100	\N	116	-1	1
594	6	6	f	0	0	0	abeecher	\N	117	-1	1
594	6	6	f	0	0	0	200 Butterwick Street	\N	118	-1	1
594	6	6	f	0	0	0	0112211001100	\N	119	-1	1
594	3	3	f	-1	0	0	\N	\N	120	-1	1
594	6	6	f	0	0	0		\N	121	-1	1
594	6	6	f	0	0	0	ALF1 SAM1	\N	122	-1	1
594	6	6	f	0	0	0		\N	123	-1	1
594	6	6	f	0	0	0	UK	\N	124	-1	1
594	6	6	f	0	0	0		\N	125	-1	1
594	6	6	f	0	0	0		\N	126	-1	1
594	6	6	f	0	0	0	Tilbury	\N	127	-1	1
594	6	6	f	0	0	0	Moresby, Garland and Wedge	\N	128	-1	1
594	21	3	f	146	0	0	\N	\N	135	-1	1
594	21	3	f	147	0	0	\N	\N	136	-1	1
595	6	6	f	0	0	0	abeecher-avatar.jpg	\N	26	-1	1
595	6	6	f	0	0	0	avatar:1443523516088	\N	138	0	1
595	21	3	f	148	0	0	\N	\N	51	-1	1
596	1	1	t	0	0	0	\N	\N	86	-1	1
596	1	1	f	0	0	0	\N	\N	87	-1	1
596	6	6	f	0	0	0	avatar	\N	26	-1	1
596	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
596	6	6	f	0	0	0	avatar	\N	144	-1	1
596	21	3	f	149	0	0	\N	\N	51	-1	1
597	6	6	f	0	0	0		\N	129	-1	1
597	6	6	f	0	0	0	Working on a new web design for the corporate site	\N	130	-1	1
597	3	3	f	442	0	0	\N	\N	131	-1	1
597	7	6	f	0	0	0	2011-02-15T20:13:09.649Z	\N	132	-1	1
597	3	3	f	8834773	0	0	\N	\N	36	-1	1
597	6	6	f	0	0	0	mjackson	\N	37	-1	1
597	6	6	f	0	0	0	mjackson@example.com	\N	38	-1	1
597	6	6	f	0	0	0	userHomesHomeFolderProvider	\N	39	-1	1
597	6	6	f	0	0	0	mjackson	\N	40	-1	1
597	6	6	f	0	0	0	Jackson	\N	41	-1	1
597	6	6	f	0	0	0	Mike	\N	43	-1	1
597	6	6	f	0	0	0	Web Site Manager	\N	114	-1	1
597	6	6	f	0	0	0	Threepwood, UK	\N	115	-1	1
597	6	6	f	0	0	0	012211331100	\N	116	-1	1
597	6	6	f	0	0	0	mjackson	\N	117	-1	1
597	6	6	f	0	0	0	100 Cavendish Street	\N	118	-1	1
597	6	6	f	0	0	0	012211331100	\N	119	-1	1
597	3	3	f	-1	0	0	\N	\N	120	-1	1
597	6	6	f	0	0	0		\N	121	-1	1
597	6	6	f	0	0	0	ALF1 SAM1	\N	122	-1	1
597	6	6	f	0	0	0		\N	123	-1	1
597	6	6	f	0	0	0	UK	\N	124	-1	1
597	6	6	f	0	0	0		\N	125	-1	1
597	6	6	f	0	0	0		\N	126	-1	1
597	6	6	f	0	0	0	Threepwood	\N	127	-1	1
597	6	6	f	0	0	0	Green Energy	\N	128	-1	1
597	21	3	f	150	0	0	\N	\N	135	-1	1
597	21	3	f	151	0	0	\N	\N	136	-1	1
598	6	6	f	0	0	0	mjackson-avatar.jpg	\N	26	-1	1
598	6	6	f	0	0	0	avatar:1443523488273	\N	138	0	1
598	21	3	f	152	0	0	\N	\N	51	-1	1
599	1	1	t	0	0	0	\N	\N	86	-1	1
599	1	1	f	0	0	0	\N	\N	87	-1	1
599	6	6	f	0	0	0	avatar	\N	26	-1	1
599	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
599	6	6	f	0	0	0	avatar	\N	144	-1	1
599	21	3	f	153	0	0	\N	\N	51	-1	1
594	0	0	f	0	0	0	\N	\N	45	-1	1
597	0	0	f	0	0	0	\N	\N	45	-1	1
594	3	3	f	4088753790	0	0	\N	\N	94	-1	1
594	3	3	f	11	0	0	\N	\N	96	-1	1
597	3	3	f	3168894086	0	0	\N	\N	94	-1	1
597	3	3	f	11	0	0	\N	\N	96	-1	1
600	3	3	f	585	0	0	\N	\N	149	-1	1
601	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	150	-1	1
601	6	6	f	0	0	0	admin	\N	40	-1	1
601	6	6	f	0	0	0	swsdp	\N	26	-1	1
601	6	6	f	0	0	0	PUBLIC	\N	107	-1	1
601	6	6	f	0	0	0	This is a Sample Alfresco Team site.	\N	27	-1	3
601	6	6	f	0	0	0	Sample: Web Site Design Project	\N	28	-1	3
601	6	6	f	0	0	0	site-dashboard	\N	108	-1	1
601	21	3	f	154	0	0	\N	\N	151	-1	1
602	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	150	-1	1
602	6	6	f	0	0	0	admin	\N	40	-1	1
602	6	6	f	0	0	0	documentLibrary	\N	152	-1	1
602	6	6	f	0	0	0	documentLibrary	\N	26	-1	1
602	6	6	f	0	0	0	Document Library	\N	27	-1	3
602	21	3	f	155	0	0	\N	\N	151	-1	1
603	6	6	f	0	0	0	admin	\N	40	-1	1
603	6	6	f	0	0	0	Agency Files	\N	26	-1	1
603	6	6	f	0	0	0	This folder holds the agency related files for the project	\N	27	-1	3
603	6	6	f	0	0	0	Agency related files	\N	28	-1	3
604	6	6	f	0	0	0	admin	\N	40	-1	1
604	6	6	f	0	0	0	Contracts	\N	26	-1	1
604	6	6	f	0	0	0	This folder holds the agency contracts	\N	27	-1	3
604	6	6	f	0	0	0	Project contracts	\N	28	-1	3
605	6	6	f	0	0	0	Alice Beecher	\N	54	-1	1
605	6	6	f	0	0	0	admin	\N	40	-1	1
605	6	6	f	0	0	0	Project Contract.pdf	\N	26	-1	1
605	1	1	f	0	0	0	\N	\N	155	-1	1
605	6	6	f	0	0	0	Contract for the Green Energy project	\N	27	-1	3
605	4	4	f	0	0	0	\N	\N	156	-1	1
605	6	6	f	0	0	0	Project Contract for Green Energy	\N	28	-1	3
865	6	6	f	0	0	0	components	\N	26	-1	1
605	1	1	t	0	0	0	\N	\N	158	-1	1
605	1	1	t	0	0	0	\N	\N	159	-1	1
605	2	3	f	0	0	0	\N	\N	160	-1	1
605	21	3	f	156	0	0	\N	\N	51	-1	1
606	6	6	f	0	0	0	1a0b110f-1e09-4ca2-b367-fe25e4964a4e	\N	26	-1	1
606	6	6	f	0	0	0	1a0b110f-1e09-4ca2-b367-fe25e4964a4e	\N	165	-1	1
607	6	6	f	0	0	0	Project Contract.pdf	\N	26	-1	1
607	21	3	f	157	0	0	\N	\N	51	-1	1
607	12	6	f	0	0	0	workspace://SpacesStore/1a0b110f-1e09-4ca2-b367-fe25e4964a4e	\N	167	-1	1
607	0	0	f	0	0	0	\N	\N	168	-1	1
865	1	1	f	0	0	0	\N	\N	86	-1	1
607	3	3	f	605	0	0	\N	\N	170	-1	1
607	1	1	f	0	0	0	\N	\N	155	-1	1
607	0	0	f	0	0	0	\N	\N	171	-1	1
865	1	1	f	0	0	0	\N	\N	87	-1	1
607	1	1	t	0	0	0	\N	\N	158	-1	1
607	1	1	t	0	0	0	\N	\N	159	-1	1
607	6	6	f	0	0	0	Contract for the Green Energy project	\N	27	-1	3
607	6	6	f	0	0	0	Project Contract for Green Energy	\N	28	-1	3
607	6	6	f	0	0	0	admin	\N	40	-1	1
607	6	6	f	0	0	0	abeecher	\N	172	-1	1
607	7	6	f	0	0	0	2011-02-15T21:26:54.600Z	\N	173	-1	1
607	6	6	f	0	0	0	admin	\N	174	-1	1
607	7	6	f	0	0	0	2011-06-14T10:28:54.714Z	\N	175	-1	1
607	0	0	f	0	0	0	\N	\N	176	-1	1
607	4	4	f	0	0	0	\N	\N	156	-1	1
607	2	3	f	0	0	0	\N	\N	160	-1	1
607	6	6	f	0	0	0	Alice Beecher	\N	54	-1	1
865	6	6	f	0	0	0	admin	\N	40	-1	1
605	0	0	f	0	0	0	\N	\N	171	-1	1
605	6	6	f	0	0	0	1.1	\N	157	-1	1
607	0	0	f	0	0	0	\N	\N	157	-1	1
607	6	6	f	0	0	0	1.1	\N	169	-1	1
608	1	1	t	0	0	0	\N	\N	86	-1	1
608	1	1	f	0	0	0	\N	\N	87	-1	1
608	6	6	f	0	0	0	admin	\N	40	-1	1
608	6	6	f	0	0	0	doclib	\N	26	-1	1
608	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
608	21	3	f	158	0	0	\N	\N	51	-1	1
609	1	1	t	0	0	0	\N	\N	86	-1	1
609	1	1	f	0	0	0	\N	\N	87	-1	1
609	6	6	f	0	0	0	admin	\N	40	-1	1
609	6	6	f	0	0	0	webpreview	\N	26	-1	1
609	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
609	21	3	f	159	0	0	\N	\N	51	-1	1
610	6	6	f	0	0	0	admin	\N	40	-1	1
610	6	6	f	0	0	0	Images	\N	26	-1	1
610	6	6	f	0	0	0	This folder holds new web site images	\N	27	-1	3
610	6	6	f	0	0	0	Project images	\N	28	-1	3
611	6	6	f	0	0	0	admin	\N	40	-1	1
611	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
611	5	5	f	0	0	22	\N	\N	179	-1	1
611	5	5	f	0	0	72	\N	\N	180	-1	1
611	6	6	f	0	0	0	Inch	\N	181	-1	1
611	7	6	f	0	0	0	2003-09-23T14:55:24.000Z	\N	182	-1	1
611	2	3	f	1	0	0	\N	\N	183	-1	1
611	2	3	f	840	0	0	\N	\N	184	-1	1
611	6	6	f	0	0	0	E-10	\N	185	-1	1
611	6	6	f	0	0	0	coins.JPG	\N	26	-1	1
611	5	5	f	0	0	72	\N	\N	186	-1	1
611	6	6	f	0	0	0	OLYMPUS DIGITAL CAMERA	\N	27	-1	4
611	1	1	f	0	0	0	\N	\N	187	-1	1
611	6	6	f	0	0	0	coins.JPG	\N	28	-1	4
611	5	5	f	0	0	9	\N	\N	188	-1	1
611	6	6	f	0	0	0	80	\N	189	-1	1
611	6	6	f	0	0	0	OLYMPUS OPTICAL CO.,LTD	\N	190	-1	1
611	2	3	f	1120	0	0	\N	\N	191	-1	1
611	5	5	f	0	0	0.003125	\N	\N	192	-1	1
611	21	3	f	160	0	0	\N	\N	51	-1	1
612	1	1	t	0	0	0	\N	\N	86	-1	1
612	1	1	f	0	0	0	\N	\N	87	-1	1
612	6	6	f	0	0	0	admin	\N	40	-1	1
612	6	6	f	0	0	0	doclib	\N	26	-1	1
612	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
612	6	6	f	0	0	0	doclib	\N	144	-1	1
612	21	3	f	161	0	0	\N	\N	51	-1	1
613	6	6	f	0	0	0	admin	\N	40	-1	1
613	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
613	5	5	f	0	0	230	\N	\N	179	-1	1
613	5	5	f	0	0	72	\N	\N	180	-1	1
613	6	6	f	0	0	0	Inch	\N	181	-1	1
613	7	6	f	0	0	0	2003-12-30T15:17:54.000Z	\N	182	-1	1
613	2	3	f	1	0	0	\N	\N	183	-1	1
613	2	3	f	664	0	0	\N	\N	184	-1	1
613	6	6	f	0	0	0	PENTAX K20D	\N	185	-1	1
613	6	6	f	0	0	0	graph.JPG	\N	26	-1	1
613	5	5	f	0	0	72	\N	\N	186	-1	1
613	1	1	f	0	0	0	\N	\N	187	-1	1
613	6	6	f	0	0	0	graph.JPG	\N	28	-1	4
613	5	5	f	0	0	6.3	\N	\N	188	-1	1
613	6	6	f	0	0	0	100	\N	189	-1	1
613	6	6	f	0	0	0	PENTAX Corporation	\N	190	-1	1
613	2	3	f	1000	0	0	\N	\N	191	-1	1
613	5	5	f	0	0	0.00555555555555556	\N	\N	192	-1	1
613	21	3	f	162	0	0	\N	\N	51	-1	1
614	1	1	t	0	0	0	\N	\N	86	-1	1
614	1	1	f	0	0	0	\N	\N	87	-1	1
614	6	6	f	0	0	0	admin	\N	40	-1	1
614	6	6	f	0	0	0	doclib	\N	26	-1	1
614	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
614	6	6	f	0	0	0	doclib	\N	144	-1	1
614	21	3	f	163	0	0	\N	\N	51	-1	1
615	6	6	f	0	0	0	admin	\N	40	-1	1
615	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
615	5	5	f	0	0	23.42	\N	\N	179	-1	1
615	5	5	f	0	0	72	\N	\N	180	-1	1
615	6	6	f	0	0	0	Inch	\N	181	-1	1
615	7	6	f	0	0	0	2003-12-30T15:17:54.000Z	\N	182	-1	1
615	2	3	f	1	0	0	\N	\N	183	-1	1
615	2	3	f	754	0	0	\N	\N	184	-1	1
615	6	6	f	0	0	0	HP PhotoSmart C850 (V05.27)	\N	185	-1	1
615	6	6	f	0	0	0	grass.jpg	\N	26	-1	1
615	5	5	f	0	0	72	\N	\N	186	-1	1
615	1	1	t	0	0	0	\N	\N	187	-1	1
615	6	6	f	0	0	0	grass.jpg	\N	28	-1	4
615	5	5	f	0	0	3	\N	\N	188	-1	1
615	6	6	f	0	0	0	100	\N	189	-1	1
615	6	6	f	0	0	0	Hewlett-Packard	\N	190	-1	1
615	2	3	f	1000	0	0	\N	\N	191	-1	1
615	5	5	f	0	0	0.008	\N	\N	192	-1	1
615	21	3	f	164	0	0	\N	\N	51	-1	1
616	1	1	t	0	0	0	\N	\N	86	-1	1
616	1	1	f	0	0	0	\N	\N	87	-1	1
616	6	6	f	0	0	0	admin	\N	40	-1	1
616	6	6	f	0	0	0	doclib	\N	26	-1	1
616	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
616	6	6	f	0	0	0	doclib	\N	144	-1	1
616	21	3	f	165	0	0	\N	\N	51	-1	1
617	1	1	t	0	0	0	\N	\N	86	-1	1
617	1	1	f	0	0	0	\N	\N	87	-1	1
617	6	6	f	0	0	0	admin	\N	40	-1	1
617	6	6	f	0	0	0	imgpreview	\N	26	-1	1
617	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
617	6	6	f	0	0	0	imgpreview	\N	144	-1	1
617	21	3	f	166	0	0	\N	\N	51	-1	1
618	6	6	f	0	0	0	admin	\N	40	-1	1
618	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
618	5	5	f	0	0	8	\N	\N	179	-1	1
618	5	5	f	0	0	72	\N	\N	180	-1	1
618	6	6	f	0	0	0	Inch	\N	181	-1	1
618	7	6	f	0	0	0	2005-07-06T09:07:21.000Z	\N	182	-1	1
618	2	3	f	1	0	0	\N	\N	183	-1	1
618	2	3	f	1932	0	0	\N	\N	184	-1	1
618	6	6	f	0	0	0	KODAK DX4530 ZOOM DIGITAL CAMERA	\N	185	-1	1
618	6	6	f	0	0	0	money.JPG	\N	26	-1	1
618	5	5	f	0	0	72	\N	\N	186	-1	1
618	1	1	f	0	0	0	\N	\N	187	-1	1
618	6	6	f	0	0	0	money.JPG	\N	28	-1	4
618	5	5	f	0	0	2.8	\N	\N	188	-1	1
618	6	6	f	0	0	0	200	\N	189	-1	1
618	6	6	f	0	0	0	EASTMAN KODAK COMPANY	\N	190	-1	1
618	2	3	f	2580	0	0	\N	\N	191	-1	1
618	5	5	f	0	0	0.0333333333333333	\N	\N	192	-1	1
618	21	3	f	167	0	0	\N	\N	51	-1	1
619	1	1	t	0	0	0	\N	\N	86	-1	1
619	1	1	f	0	0	0	\N	\N	87	-1	1
619	6	6	f	0	0	0	admin	\N	40	-1	1
619	6	6	f	0	0	0	doclib	\N	26	-1	1
619	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
619	6	6	f	0	0	0	doclib	\N	144	-1	1
619	21	3	f	168	0	0	\N	\N	51	-1	1
620	6	6	f	0	0	0	admin	\N	40	-1	1
620	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
620	5	5	f	0	0	16	\N	\N	179	-1	1
620	5	5	f	0	0	72	\N	\N	180	-1	1
620	6	6	f	0	0	0	Inch	\N	181	-1	1
620	7	6	f	0	0	0	2008-12-13T17:05:16.000Z	\N	182	-1	1
620	2	3	f	1	0	0	\N	\N	183	-1	1
620	2	3	f	2448	0	0	\N	\N	184	-1	1
620	6	6	f	0	0	0	Canon PowerShot A590 IS	\N	185	-1	1
620	6	6	f	0	0	0	plugs.jpg	\N	26	-1	1
620	5	5	f	0	0	72	\N	\N	186	-1	1
620	1	1	f	0	0	0	\N	\N	187	-1	1
620	6	6	f	0	0	0	plugs.jpg	\N	28	-1	4
620	5	5	f	0	0	4.5	\N	\N	188	-1	1
620	6	6	f	0	0	0	200	\N	189	-1	1
620	6	6	f	0	0	0	Canon	\N	190	-1	1
620	2	3	f	3264	0	0	\N	\N	191	-1	1
620	5	5	f	0	0	0.0333333333333333	\N	\N	192	-1	1
620	21	3	f	169	0	0	\N	\N	51	-1	1
621	1	1	t	0	0	0	\N	\N	86	-1	1
621	1	1	f	0	0	0	\N	\N	87	-1	1
621	6	6	f	0	0	0	admin	\N	40	-1	1
621	6	6	f	0	0	0	doclib	\N	26	-1	1
621	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
621	6	6	f	0	0	0	doclib	\N	144	-1	1
621	21	3	f	170	0	0	\N	\N	51	-1	1
622	6	6	f	0	0	0	admin	\N	40	-1	1
622	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
622	5	5	f	0	0	18	\N	\N	179	-1	1
622	5	5	f	0	0	72	\N	\N	180	-1	1
622	6	6	f	0	0	0	Inch	\N	181	-1	1
622	7	6	f	0	0	0	2005-01-09T16:00:55.000Z	\N	182	-1	1
622	2	3	f	1	0	0	\N	\N	183	-1	1
622	2	3	f	2048	0	0	\N	\N	184	-1	1
622	6	6	f	0	0	0	Canon EOS 300D DIGITAL	\N	185	-1	1
622	6	6	f	0	0	0	turbine.JPG	\N	26	-1	1
622	5	5	f	0	0	72	\N	\N	186	-1	1
622	1	1	t	0	0	0	\N	\N	187	-1	1
622	6	6	f	0	0	0	turbine.JPG	\N	28	-1	4
622	5	5	f	0	0	3.5	\N	\N	188	-1	1
622	6	6	f	0	0	0	400	\N	189	-1	1
622	6	6	f	0	0	0	Canon	\N	190	-1	1
622	2	3	f	3072	0	0	\N	\N	191	-1	1
622	5	5	f	0	0	0.4	\N	\N	192	-1	1
622	21	3	f	171	0	0	\N	\N	51	-1	1
623	1	1	t	0	0	0	\N	\N	86	-1	1
623	1	1	f	0	0	0	\N	\N	87	-1	1
623	6	6	f	0	0	0	admin	\N	40	-1	1
623	6	6	f	0	0	0	doclib	\N	26	-1	1
623	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
623	6	6	f	0	0	0	doclib	\N	144	-1	1
623	21	3	f	172	0	0	\N	\N	51	-1	1
624	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
624	5	5	f	0	0	72	\N	\N	180	-1	1
624	6	6	f	0	0	0	Inch	\N	181	-1	1
624	2	3	f	1	0	0	\N	\N	183	-1	1
624	6	6	f	0	0	0	admin	\N	40	-1	1
624	2	3	f	912	0	0	\N	\N	184	-1	1
624	5	5	f	0	0	72	\N	\N	186	-1	1
624	6	6	f	0	0	0	wires.JPG	\N	26	-1	1
624	6	6	f	0	0	0	wires.JPG	\N	28	-1	4
624	2	3	f	1216	0	0	\N	\N	191	-1	1
624	21	3	f	173	0	0	\N	\N	51	-1	1
625	1	1	t	0	0	0	\N	\N	86	-1	1
625	1	1	f	0	0	0	\N	\N	87	-1	1
625	6	6	f	0	0	0	admin	\N	40	-1	1
625	6	6	f	0	0	0	doclib	\N	26	-1	1
625	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
625	6	6	f	0	0	0	doclib	\N	144	-1	1
625	21	3	f	174	0	0	\N	\N	51	-1	1
626	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
626	5	5	f	0	0	72	\N	\N	180	-1	1
626	6	6	f	0	0	0	Inch	\N	181	-1	1
626	2	3	f	1	0	0	\N	\N	183	-1	1
626	6	6	f	0	0	0	admin	\N	40	-1	1
626	2	3	f	3008	0	0	\N	\N	184	-1	1
626	5	5	f	0	0	72	\N	\N	186	-1	1
626	6	6	f	0	0	0	wind turbine.JPG	\N	26	-1	1
626	6	6	f	0	0	0	wind turbine.JPG	\N	28	-1	4
626	2	3	f	2000	0	0	\N	\N	191	-1	1
626	21	3	f	175	0	0	\N	\N	51	-1	1
627	1	1	t	0	0	0	\N	\N	86	-1	1
627	1	1	f	0	0	0	\N	\N	87	-1	1
627	6	6	f	0	0	0	admin	\N	40	-1	1
627	6	6	f	0	0	0	doclib	\N	26	-1	1
627	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
627	6	6	f	0	0	0	doclib	\N	144	-1	1
627	21	3	f	176	0	0	\N	\N	51	-1	1
628	6	6	f	0	0	0	admin	\N	40	-1	1
628	2	3	f	217	0	0	\N	\N	184	-1	1
628	6	6	f	0	0	0	header.png	\N	26	-1	1
628	6	6	f	0	0	0	header.png	\N	28	-1	4
628	2	3	f	793	0	0	\N	\N	191	-1	1
628	21	3	f	177	0	0	\N	\N	51	-1	1
629	1	1	t	0	0	0	\N	\N	86	-1	1
629	1	1	f	0	0	0	\N	\N	87	-1	1
629	6	6	f	0	0	0	admin	\N	40	-1	1
629	6	6	f	0	0	0	doclib	\N	26	-1	1
629	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
629	6	6	f	0	0	0	doclib	\N	144	-1	1
629	21	3	f	178	0	0	\N	\N	51	-1	1
630	6	6	f	0	0	0	admin	\N	40	-1	1
630	2	3	f	1000	0	0	\N	\N	184	-1	1
630	6	6	f	0	0	0	windmill.png	\N	26	-1	1
630	6	6	f	0	0	0	windmill.png	\N	28	-1	4
630	2	3	f	591	0	0	\N	\N	191	-1	1
630	21	3	f	179	0	0	\N	\N	51	-1	1
631	1	1	t	0	0	0	\N	\N	86	-1	1
631	1	1	f	0	0	0	\N	\N	87	-1	1
631	6	6	f	0	0	0	admin	\N	40	-1	1
631	6	6	f	0	0	0	doclib	\N	26	-1	1
631	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
631	6	6	f	0	0	0	doclib	\N	144	-1	1
631	21	3	f	180	0	0	\N	\N	51	-1	1
632	6	6	f	0	0	0	admin	\N	40	-1	1
632	2	3	f	245	0	0	\N	\N	184	-1	1
632	6	6	f	0	0	0	low consumption bulb.png	\N	26	-1	1
632	6	6	f	0	0	0	low consumption bulb.png	\N	28	-1	4
632	2	3	f	625	0	0	\N	\N	191	-1	1
632	21	3	f	181	0	0	\N	\N	51	-1	1
633	1	1	t	0	0	0	\N	\N	86	-1	1
633	1	1	f	0	0	0	\N	\N	87	-1	1
633	6	6	f	0	0	0	admin	\N	40	-1	1
633	6	6	f	0	0	0	doclib	\N	26	-1	1
633	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
633	6	6	f	0	0	0	doclib	\N	144	-1	1
633	21	3	f	182	0	0	\N	\N	51	-1	1
634	6	6	f	0	0	0	admin	\N	40	-1	1
634	6	6	f	0	0	0	Logo Files	\N	26	-1	1
634	6	6	f	0	0	0	This folder holds new logo files for the web site	\N	27	-1	3
634	6	6	f	0	0	0	Project logo files	\N	28	-1	3
635	6	6	f	0	0	0	admin	\N	40	-1	1
635	2	3	f	192	0	0	\N	\N	184	-1	1
635	6	6	f	0	0	0	GE Logo.png	\N	26	-1	1
635	4	4	f	0	1	0	\N	\N	156	-1	1
635	6	6	f	0	0	0	GE Logo.png	\N	28	-1	4
635	2	3	f	400	0	0	\N	\N	191	-1	1
635	2	3	f	1	0	0	\N	\N	160	-1	1
635	21	3	f	183	0	0	\N	\N	51	-1	1
636	1	1	t	0	0	0	\N	\N	86	-1	1
636	1	1	f	0	0	0	\N	\N	87	-1	1
636	6	6	f	0	0	0	admin	\N	40	-1	1
636	6	6	f	0	0	0	doclib	\N	26	-1	1
636	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
636	6	6	f	0	0	0	doclib	\N	144	-1	1
636	21	3	f	184	0	0	\N	\N	51	-1	1
637	6	6	f	0	0	0	admin	\N	40	-1	1
637	2	3	f	398	0	0	\N	\N	184	-1	1
637	6	6	f	0	0	0	logo.png	\N	26	-1	1
637	4	4	f	0	1	0	\N	\N	156	-1	1
637	6	6	f	0	0	0	logo.png	\N	28	-1	4
637	2	3	f	414	0	0	\N	\N	191	-1	1
637	2	3	f	1	0	0	\N	\N	160	-1	1
637	21	3	f	185	0	0	\N	\N	51	-1	1
638	1	1	t	0	0	0	\N	\N	86	-1	1
638	1	1	f	0	0	0	\N	\N	87	-1	1
638	6	6	f	0	0	0	admin	\N	40	-1	1
638	6	6	f	0	0	0	doclib	\N	26	-1	1
638	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
638	6	6	f	0	0	0	doclib	\N	144	-1	1
638	21	3	f	186	0	0	\N	\N	51	-1	1
639	6	6	f	0	0	0	admin	\N	40	-1	1
639	6	6	f	0	0	0	Mock-Ups	\N	26	-1	1
639	6	6	f	0	0	0	This folder holds the web site mock-ups or wireframes	\N	27	-1	3
639	6	6	f	0	0	0	Web wireframes	\N	28	-1	3
640	6	6	f	0	0	0	admin	\N	40	-1	1
640	2	3	f	893	0	0	\N	\N	184	-1	1
640	6	6	f	0	0	0	sample 1.png	\N	26	-1	1
640	6	6	f	0	0	0	sample 1.png	\N	28	-1	4
640	2	3	f	1067	0	0	\N	\N	191	-1	1
640	21	3	f	187	0	0	\N	\N	51	-1	1
641	1	1	t	0	0	0	\N	\N	86	-1	1
641	1	1	f	0	0	0	\N	\N	87	-1	1
641	6	6	f	0	0	0	admin	\N	40	-1	1
641	6	6	f	0	0	0	doclib	\N	26	-1	1
641	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
641	6	6	f	0	0	0	doclib	\N	144	-1	1
641	21	3	f	188	0	0	\N	\N	51	-1	1
642	6	6	f	0	0	0	admin	\N	40	-1	1
642	2	3	f	921	0	0	\N	\N	184	-1	1
642	6	6	f	0	0	0	sample 2.png	\N	26	-1	1
642	6	6	f	0	0	0	sample 2.png	\N	28	-1	4
642	2	3	f	778	0	0	\N	\N	191	-1	1
642	21	3	f	189	0	0	\N	\N	51	-1	1
643	1	1	t	0	0	0	\N	\N	86	-1	1
643	1	1	f	0	0	0	\N	\N	87	-1	1
643	6	6	f	0	0	0	admin	\N	40	-1	1
643	6	6	f	0	0	0	doclib	\N	26	-1	1
643	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
643	6	6	f	0	0	0	doclib	\N	144	-1	1
643	21	3	f	190	0	0	\N	\N	51	-1	1
644	1	1	t	0	0	0	\N	\N	86	-1	1
644	1	1	f	0	0	0	\N	\N	87	-1	1
644	6	6	f	0	0	0	admin	\N	40	-1	1
644	6	6	f	0	0	0	imgpreview	\N	26	-1	1
644	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
644	6	6	f	0	0	0	imgpreview	\N	144	-1	1
644	21	3	f	191	0	0	\N	\N	51	-1	1
645	6	6	f	0	0	0	admin	\N	40	-1	1
645	2	3	f	769	0	0	\N	\N	184	-1	1
645	6	6	f	0	0	0	sample 3.png	\N	26	-1	1
645	4	4	f	0	2	0	\N	\N	156	-1	1
645	6	6	f	0	0	0	sample 3.png	\N	28	-1	4
645	2	3	f	782	0	0	\N	\N	191	-1	1
645	2	3	f	2	0	0	\N	\N	160	-1	1
645	21	3	f	192	0	0	\N	\N	51	-1	1
646	1	1	t	0	0	0	\N	\N	86	-1	1
646	1	1	f	0	0	0	\N	\N	87	-1	1
646	6	6	f	0	0	0	admin	\N	40	-1	1
646	6	6	f	0	0	0	doclib	\N	26	-1	1
646	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
646	6	6	f	0	0	0	doclib	\N	144	-1	1
646	21	3	f	193	0	0	\N	\N	51	-1	1
647	1	1	t	0	0	0	\N	\N	86	-1	1
647	1	1	f	0	0	0	\N	\N	87	-1	1
647	6	6	f	0	0	0	admin	\N	40	-1	1
647	6	6	f	0	0	0	imgpreview	\N	26	-1	1
647	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
647	6	6	f	0	0	0	imgpreview	\N	144	-1	1
647	21	3	f	194	0	0	\N	\N	51	-1	1
648	6	6	f	0	0	0	admin	\N	40	-1	1
648	6	6	f	0	0	0	Video Files	\N	26	-1	1
648	6	6	f	0	0	0	This folder holds any video files related to the project	\N	27	-1	4
648	6	6	f	0	0	0	Folder for video files	\N	28	-1	4
649	6	6	f	0	0	0	Created by John Cavendish	\N	54	-1	1
649	6	6	f	0	0	0	admin	\N	40	-1	1
649	6	6	f	0	0	0	WebSiteReview.mp4	\N	26	-1	1
649	6	6	f	0	0	0	This is a video of the mock up to show the planned structure for the new web site.	\N	27	-1	4
649	6	6	f	0	0	0	WebSiteReview.mp4	\N	28	-1	4
649	21	3	f	195	0	0	\N	\N	51	-1	1
650	6	6	f	0	0	0	admin	\N	40	-1	1
650	6	6	f	0	0	0	Budget Files	\N	26	-1	1
650	6	6	f	0	0	0	This folder holds the project budget and invoices	\N	27	-1	3
650	6	6	f	0	0	0	Project finance files	\N	28	-1	3
651	6	6	f	0	0	0	admin	\N	40	-1	1
651	6	6	f	0	0	0	Invoices	\N	26	-1	1
651	6	6	f	0	0	0	This folder holds invoices for the project	\N	27	-1	3
651	6	6	f	0	0	0	Project invoices	\N	28	-1	3
652	6	6	f	0	0	0	admin	\N	40	-1	1
652	2	3	f	974	0	0	\N	\N	184	-1	1
652	6	6	f	0	0	0	inv I200-109.png	\N	26	-1	1
652	6	6	f	0	0	0	inv I200-109.png	\N	28	-1	4
652	2	3	f	749	0	0	\N	\N	191	-1	1
652	21	3	f	196	0	0	\N	\N	51	-1	1
653	1	1	t	0	0	0	\N	\N	86	-1	1
653	1	1	f	0	0	0	\N	\N	87	-1	1
653	6	6	f	0	0	0	admin	\N	40	-1	1
653	6	6	f	0	0	0	doclib	\N	26	-1	1
653	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
653	6	6	f	0	0	0	doclib	\N	144	-1	1
653	21	3	f	197	0	0	\N	\N	51	-1	1
654	6	6	f	0	0	0	admin	\N	40	-1	1
654	2	3	f	970	0	0	\N	\N	184	-1	1
654	6	6	f	0	0	0	inv I200-189.png	\N	26	-1	1
654	6	6	f	0	0	0	inv I200-189.png	\N	28	-1	4
654	2	3	f	751	0	0	\N	\N	191	-1	1
654	21	3	f	198	0	0	\N	\N	51	-1	1
655	1	1	t	0	0	0	\N	\N	86	-1	1
655	1	1	f	0	0	0	\N	\N	87	-1	1
655	6	6	f	0	0	0	admin	\N	40	-1	1
655	6	6	f	0	0	0	doclib	\N	26	-1	1
655	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
655	6	6	f	0	0	0	doclib	\N	144	-1	1
655	21	3	f	199	0	0	\N	\N	51	-1	1
656	6	6	f	0	0	0	Mike Jackson	\N	54	-1	1
656	6	6	f	0	0	0	admin	\N	40	-1	1
656	6	6	f	0	0	0	budget.xls	\N	26	-1	1
656	6	6	f	0	0	0	Budget file for the web site redesign	\N	27	-1	3
656	6	6	f	0	0	0	Web Site Design - Budget	\N	28	-1	3
656	21	3	f	200	0	0	\N	\N	51	-1	1
657	1	1	t	0	0	0	\N	\N	86	-1	1
657	1	1	f	0	0	0	\N	\N	87	-1	1
657	6	6	f	0	0	0	admin	\N	40	-1	1
657	6	6	f	0	0	0	doclib	\N	26	-1	1
657	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
657	6	6	f	0	0	0	doclib	\N	144	-1	1
657	21	3	f	201	0	0	\N	\N	51	-1	1
658	1	1	t	0	0	0	\N	\N	86	-1	1
658	1	1	f	0	0	0	\N	\N	87	-1	1
658	6	6	f	0	0	0	admin	\N	40	-1	1
658	6	6	f	0	0	0	webpreview	\N	26	-1	1
658	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
658	6	6	f	0	0	0	webpreview	\N	144	-1	1
658	21	3	f	202	0	0	\N	\N	51	-1	1
659	6	6	f	0	0	0	budget.xls discussion	\N	26	-1	1
659	6	6	f	0	0	0	forum	\N	29	-1	1
659	6	6	f	0	0	0	admin	\N	40	-1	1
660	6	6	f	0	0	0	Comments	\N	26	-1	1
660	6	6	f	0	0	0	admin	\N	40	-1	1
661	6	6	f	0	0	0	comment-1297852210661_622	\N	26	-1	1
661	6	6	f	0	0	0		\N	28	-1	3
661	6	6	f	0	0	0	admin	\N	40	-1	1
661	21	3	f	203	0	0	\N	\N	51	-1	1
662	6	6	f	0	0	0	admin	\N	40	-1	1
662	6	6	f	0	0	0	Meeting Notes	\N	26	-1	1
662	6	6	f	0	0	0	This folder holds notes from the project review meetings	\N	27	-1	3
662	6	6	f	0	0	0	Project meeting notes	\N	28	-1	3
663	6	6	f	0	0	0	Meeting Notes 2011-01-27.doc	\N	26	-1	1
663	6	6	f	0	0	0	Meeting Notes 2011-01-27.doc	\N	28	-1	3
663	6	6	f	0	0	0	admin	\N	40	-1	1
663	21	3	f	204	0	0	\N	\N	51	-1	1
664	1	1	t	0	0	0	\N	\N	86	-1	1
664	1	1	f	0	0	0	\N	\N	87	-1	1
664	6	6	f	0	0	0	admin	\N	40	-1	1
664	6	6	f	0	0	0	doclib	\N	26	-1	1
664	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
664	6	6	f	0	0	0	doclib	\N	144	-1	1
664	21	3	f	205	0	0	\N	\N	51	-1	1
665	1	1	t	0	0	0	\N	\N	86	-1	1
665	1	1	f	0	0	0	\N	\N	87	-1	1
665	6	6	f	0	0	0	admin	\N	40	-1	1
665	6	6	f	0	0	0	webpreview	\N	26	-1	1
665	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
665	6	6	f	0	0	0	webpreview	\N	144	-1	1
665	21	3	f	206	0	0	\N	\N	51	-1	1
666	6	6	f	0	0	0	Meeting Notes 2011-02-03.doc	\N	26	-1	1
666	6	6	f	0	0	0	Meeting Notes 2011-02-03.doc	\N	28	-1	3
666	6	6	f	0	0	0	admin	\N	40	-1	1
666	21	3	f	207	0	0	\N	\N	51	-1	1
667	1	1	t	0	0	0	\N	\N	86	-1	1
667	1	1	f	0	0	0	\N	\N	87	-1	1
667	6	6	f	0	0	0	admin	\N	40	-1	1
667	6	6	f	0	0	0	doclib	\N	26	-1	1
667	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
667	6	6	f	0	0	0	doclib	\N	144	-1	1
667	21	3	f	208	0	0	\N	\N	51	-1	1
668	1	1	t	0	0	0	\N	\N	86	-1	1
668	1	1	f	0	0	0	\N	\N	87	-1	1
668	6	6	f	0	0	0	admin	\N	40	-1	1
668	6	6	f	0	0	0	webpreview	\N	26	-1	1
668	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
668	6	6	f	0	0	0	webpreview	\N	144	-1	1
668	21	3	f	209	0	0	\N	\N	51	-1	1
669	6	6	f	0	0	0	Meeting Notes 2011-02-10.doc	\N	26	-1	1
669	6	6	f	0	0	0	Meeting Notes 2011-02-10.doc	\N	28	-1	3
669	6	6	f	0	0	0	admin	\N	40	-1	1
669	21	3	f	210	0	0	\N	\N	51	-1	1
670	1	1	t	0	0	0	\N	\N	86	-1	1
670	1	1	f	0	0	0	\N	\N	87	-1	1
670	6	6	f	0	0	0	admin	\N	40	-1	1
670	6	6	f	0	0	0	doclib	\N	26	-1	1
670	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
670	6	6	f	0	0	0	doclib	\N	144	-1	1
670	21	3	f	211	0	0	\N	\N	51	-1	1
671	6	6	f	0	0	0	admin	\N	40	-1	1
671	6	6	f	0	0	0	webpreview	\N	26	-1	1
671	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
671	6	6	f	0	0	0	webpreview	\N	144	-1	1
671	21	3	f	212	0	0	\N	\N	51	-1	1
672	6	6	f	0	0	0	admin	\N	40	-1	1
672	6	6	f	0	0	0	Presentations	\N	26	-1	1
672	6	6	f	0	0	0	This folder holds presentations from the project	\N	27	-1	3
672	6	6	f	0	0	0	Project presentations	\N	28	-1	3
673	6	6	f	0	0	0	Project Objectives.ppt	\N	26	-1	1
673	6	6	f	0	0	0	Project Objectives.ppt	\N	28	-1	4
673	6	6	f	0	0	0	admin	\N	40	-1	1
673	21	3	f	213	0	0	\N	\N	51	-1	1
674	1	1	t	0	0	0	\N	\N	86	-1	1
674	1	1	f	0	0	0	\N	\N	87	-1	1
674	6	6	f	0	0	0	admin	\N	40	-1	1
674	6	6	f	0	0	0	doclib	\N	26	-1	1
674	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
674	6	6	f	0	0	0	doclib	\N	144	-1	1
674	21	3	f	214	0	0	\N	\N	51	-1	1
675	1	1	t	0	0	0	\N	\N	86	-1	1
675	1	1	f	0	0	0	\N	\N	87	-1	1
675	6	6	f	0	0	0	admin	\N	40	-1	1
675	6	6	f	0	0	0	webpreview	\N	26	-1	1
675	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
675	6	6	f	0	0	0	webpreview	\N	144	-1	1
675	21	3	f	215	0	0	\N	\N	51	-1	1
676	6	6	f	0	0	0	Project Overview.ppt	\N	26	-1	1
676	6	6	f	0	0	0	Project Overview.ppt	\N	28	-1	4
676	6	6	f	0	0	0	admin	\N	40	-1	1
676	21	3	f	216	0	0	\N	\N	51	-1	1
677	1	1	t	0	0	0	\N	\N	86	-1	1
677	1	1	f	0	0	0	\N	\N	87	-1	1
677	6	6	f	0	0	0	admin	\N	40	-1	1
677	6	6	f	0	0	0	doclib	\N	26	-1	1
677	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
677	6	6	f	0	0	0	doclib	\N	144	-1	1
677	21	3	f	217	0	0	\N	\N	51	-1	1
678	1	1	t	0	0	0	\N	\N	86	-1	1
678	1	1	f	0	0	0	\N	\N	87	-1	1
678	6	6	f	0	0	0	admin	\N	40	-1	1
678	6	6	f	0	0	0	webpreview	\N	26	-1	1
678	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
678	6	6	f	0	0	0	webpreview	\N	144	-1	1
678	21	3	f	218	0	0	\N	\N	51	-1	1
679	6	6	f	0	0	0	links	\N	26	-1	1
679	6	6	f	0	0	0	admin	\N	40	-1	1
679	6	6	f	0	0	0	links	\N	152	-1	1
680	6	6	f	0	0	0	The W3 Schools web site has some good guides (with interactive examples) on how to create websites	\N	200	-1	1
680	6	6	f	0	0	0	admin	\N	40	-1	1
680	6	6	f	0	0	0	W3 Schools	\N	201	-1	1
680	6	6	f	0	0	0	link-1297806194371_850	\N	26	-1	1
680	6	6	f	0	0	0	http://www.w3schools.com/	\N	202	-1	1
680	21	3	f	219	0	0	\N	\N	51	-1	1
681	6	6	f	0	0	0	W3C website. Includes some good guides to web design and application	\N	200	-1	1
681	6	6	f	0	0	0	admin	\N	40	-1	1
681	6	6	f	0	0	0	Web Design and Applications	\N	201	-1	1
681	6	6	f	0	0	0	link-1297806244007_178	\N	26	-1	1
681	6	6	f	0	0	0	http://www.w3.org/standards/webdesign/	\N	202	-1	1
681	21	3	f	220	0	0	\N	\N	51	-1	1
682	6	6	f	0	0	0	admin	\N	40	-1	1
682	6	6	f	0	0	0	dataLists	\N	152	-1	1
682	6	6	f	0	0	0	dataLists	\N	26	-1	1
682	6	6	f	0	0	0	Data Lists	\N	27	-1	3
683	6	6	f	0	0	0	admin	\N	40	-1	1
683	6	6	f	0	0	0	71824d77-9cd8-44c3-b3e4-dbca7e17dc49	\N	26	-1	1
683	6	6	f	0	0	0	Project issues	\N	27	-1	3
683	6	6	f	0	0	0	Issue Log	\N	28	-1	3
683	6	6	f	0	0	0	dl:issue	\N	204	-1	1
684	7	6	f	0	0	0	2011-03-09T00:00:00.000Z	\N	209	-1	1
684	6	6	f	0	0	0	Issue 1	\N	210	-1	1
684	6	6	f	0	0	0	admin	\N	40	-1	1
684	6	6	f	0	0	0	e6fc15e9-5caf-4f17-857e-7b0cfbc655a9	\N	26	-1	1
684	6	6	f	0	0	0	Support need to be able to access and update content of the corporate web site. Need to find a solution.	\N	27	-1	3
684	6	6	f	0	0	0	Support access	\N	28	-1	3
684	6	6	f	0	0	0	Not Started	\N	206	-1	1
684	6	6	f	0	0	0	Normal	\N	207	-1	1
684	6	6	f	0	0	0		\N	208	-1	1
685	7	6	f	0	0	0	2011-02-24T00:00:00.000Z	\N	209	-1	1
685	6	6	f	0	0	0	Issue 3	\N	210	-1	1
685	6	6	f	0	0	0	admin	\N	40	-1	1
685	6	6	f	0	0	0	42fcbae6-b1fe-4028-9f85-9ad7f81a8e3b	\N	26	-1	1
685	6	6	f	0	0	0	The budget has been cut. Need to address the cuts and work out how accomodate the project.	\N	27	-1	3
685	6	6	f	0	0	0	Budget cut	\N	28	-1	3
685	6	6	f	0	0	0	Not Started	\N	206	-1	1
685	6	6	f	0	0	0	High	\N	207	-1	1
685	6	6	f	0	0	0	Looking to use an Open Source solution	\N	208	-1	1
686	7	6	f	0	0	0	2011-03-02T00:00:00.000Z	\N	209	-1	1
686	6	6	f	0	0	0	Issue 2	\N	210	-1	1
686	6	6	f	0	0	0	admin	\N	40	-1	1
686	6	6	f	0	0	0	66028f46-c074-4cf5-9f37-8490e51ca540	\N	26	-1	1
686	6	6	f	0	0	0	There is an issue with the copyright of one of the images selected. Need to source a replacement.	\N	27	-1	3
686	6	6	f	0	0	0	Copyright issue	\N	28	-1	3
686	6	6	f	0	0	0	In Progress	\N	206	-1	1
686	6	6	f	0	0	0	High	\N	207	-1	1
686	6	6	f	0	0	0	Alice is actively trying to sort this	\N	208	-1	1
687	7	6	f	0	0	0	2011-02-17T00:00:00.000Z	\N	209	-1	1
687	6	6	f	0	0	0	Issue 4	\N	210	-1	1
687	6	6	f	0	0	0	admin	\N	40	-1	1
687	6	6	f	0	0	0	50046ccd-9034-420f-925b-0530836488c4	\N	26	-1	1
687	6	6	f	0	0	0	The Web Manager has resigned. Need to find a replacement.	\N	27	-1	3
687	6	6	f	0	0	0	Web Manager	\N	28	-1	3
687	6	6	f	0	0	0	Complete	\N	206	-1	1
687	6	6	f	0	0	0	High	\N	207	-1	1
687	6	6	f	0	0	0	This has been solved. Izzy Previn has joined the team.	\N	208	-1	1
688	6	6	f	0	0	0	admin	\N	40	-1	1
688	6	6	f	0	0	0	aea88103-517e-4aa0-a3be-de258d0e6465	\N	26	-1	1
688	6	6	f	0	0	0	Project to do list	\N	27	-1	4
688	6	6	f	0	0	0	Project to do list	\N	27	-1	3
688	6	6	f	0	0	0	To-Do	\N	28	-1	4
688	6	6	f	0	0	0	Task Log	\N	28	-1	3
688	6	6	f	0	0	0	dl:todoList	\N	204	-1	1
689	6	6	f	0	0	0	Not Started	\N	213	-1	1
689	2	3	f	3	0	0	\N	\N	214	-1	1
689	6	6	f	0	0	0		\N	215	-1	1
689	6	6	f	0	0	0	Revise Project Objectives	\N	216	-1	1
689	6	6	f	0	0	0	admin	\N	40	-1	1
689	7	6	f	0	0	0	2011-03-08T00:00:00.000Z	\N	217	-1	1
689	6	6	f	0	0	0	9198bd31-a664-4584-a271-b529daf4793b	\N	26	-1	1
690	6	6	f	0	0	0	In Progress	\N	213	-1	1
690	2	3	f	1	0	0	\N	\N	214	-1	1
690	6	6	f	0	0	0		\N	215	-1	1
690	6	6	f	0	0	0	Update budget	\N	216	-1	1
690	6	6	f	0	0	0	admin	\N	40	-1	1
690	7	6	f	0	0	0	2011-03-14T00:00:00.000Z	\N	217	-1	1
690	6	6	f	0	0	0	eb1c2fda-4868-4384-b29e-78c01b6601ec	\N	26	-1	1
691	6	6	f	0	0	0	On Hold	\N	213	-1	1
691	2	3	f	5	0	0	\N	\N	214	-1	1
691	6	6	f	0	0	0		\N	215	-1	1
691	6	6	f	0	0	0	Upload new images	\N	216	-1	1
691	6	6	f	0	0	0	admin	\N	40	-1	1
691	7	6	f	0	0	0	2011-03-16T00:00:00.000Z	\N	217	-1	1
691	6	6	f	0	0	0	35b8be80-170f-40af-a173-513758b83165	\N	26	-1	1
692	6	6	f	0	0	0	Complete	\N	213	-1	1
692	2	3	f	2	0	0	\N	\N	214	-1	1
692	6	6	f	0	0	0		\N	215	-1	1
692	6	6	f	0	0	0	Contract	\N	216	-1	1
692	6	6	f	0	0	0	admin	\N	40	-1	1
692	7	6	f	0	0	0	2011-02-01T00:00:00.000Z	\N	217	-1	1
692	6	6	f	0	0	0	567ee439-4ebc-40cf-a783-3e561ad5a605	\N	26	-1	1
693	6	6	f	0	0	0	Not Started	\N	213	-1	1
693	2	3	f	2	0	0	\N	\N	214	-1	1
693	6	6	f	0	0	0	Please take a look at the structure video and provide your feedback	\N	215	-1	1
693	6	6	f	0	0	0	Review the web structure video	\N	216	-1	1
693	6	6	f	0	0	0	admin	\N	40	-1	1
693	7	6	f	0	0	0	2011-03-30T23:00:00.000Z	\N	217	-1	1
693	6	6	f	0	0	0	7a0bb872-bf7c-457b-831e-95f94efb9816	\N	26	-1	1
694	6	6	f	0	0	0	wiki	\N	26	-1	1
694	6	6	f	0	0	0	admin	\N	40	-1	1
694	6	6	f	0	0	0	wiki	\N	152	-1	1
695	6	6	f	0	0	0	admin	\N	40	-1	1
695	6	6	f	0	0	0	Main_Page	\N	26	-1	1
695	1	1	t	0	0	0	\N	\N	155	-1	1
695	6	6	f	0	0	0	Main Page	\N	28	-1	4
695	6	6	f	0	0	0	Main Page	\N	28	-1	3
866	6	6	f	0	0	0	page.title.user~admin~dashboard.xml	\N	26	-1	1
695	1	1	t	0	0	0	\N	\N	158	-1	1
695	1	1	t	0	0	0	\N	\N	159	-1	1
695	21	3	f	221	0	0	\N	\N	51	-1	1
696	6	6	f	0	0	0	d6f3a279-ce86-4a12-8985-93b71afbb71d	\N	26	-1	1
696	6	6	f	0	0	0	d6f3a279-ce86-4a12-8985-93b71afbb71d	\N	165	-1	1
697	6	6	f	0	0	0	Main_Page	\N	26	-1	1
697	21	3	f	222	0	0	\N	\N	51	-1	1
697	12	6	f	0	0	0	workspace://SpacesStore/d6f3a279-ce86-4a12-8985-93b71afbb71d	\N	167	-1	1
697	0	0	f	0	0	0	\N	\N	168	-1	1
866	6	6	f	0	0	0	admin	\N	40	-1	1
697	3	3	f	695	0	0	\N	\N	170	-1	1
697	1	1	t	0	0	0	\N	\N	155	-1	1
697	0	0	f	0	0	0	\N	\N	171	-1	1
866	1	1	t	0	0	0	\N	\N	86	-1	1
697	1	1	t	0	0	0	\N	\N	158	-1	1
697	1	1	t	0	0	0	\N	\N	159	-1	1
697	6	6	f	0	0	0	admin	\N	40	-1	1
697	0	0	f	0	0	0	\N	\N	27	-1	2
697	6	6	f	0	0	0	Main Page	\N	28	-1	4
697	6	6	f	0	0	0	Main Page	\N	28	-1	3
697	6	6	f	0	0	0	mjackson	\N	172	-1	1
697	7	6	f	0	0	0	2011-02-15T21:46:47.847Z	\N	173	-1	1
697	6	6	f	0	0	0	admin	\N	174	-1	1
697	7	6	f	0	0	0	2011-06-14T10:28:57.221Z	\N	175	-1	1
697	0	0	f	0	0	0	\N	\N	176	-1	1
697	0	0	f	0	0	0	\N	\N	154	-1	1
866	1	1	f	0	0	0	\N	\N	87	-1	1
695	0	0	f	0	0	0	\N	\N	171	-1	1
695	6	6	f	0	0	0	1.15	\N	157	-1	1
697	0	0	f	0	0	0	\N	\N	157	-1	1
697	6	6	f	0	0	0	1.15	\N	169	-1	1
698	6	6	f	0	0	0	admin	\N	40	-1	1
698	6	6	f	0	0	0	Meetings	\N	26	-1	1
698	1	1	t	0	0	0	\N	\N	155	-1	1
698	6	6	f	0	0	0	Meetings	\N	28	-1	4
698	6	6	f	0	0	0	Meetings	\N	28	-1	3
698	1	1	t	0	0	0	\N	\N	158	-1	1
698	1	1	t	0	0	0	\N	\N	159	-1	1
698	21	3	f	223	0	0	\N	\N	51	-1	1
699	6	6	f	0	0	0	1373739a-2849-4647-9e97-7a4e05cc5841	\N	26	-1	1
699	6	6	f	0	0	0	1373739a-2849-4647-9e97-7a4e05cc5841	\N	165	-1	1
700	6	6	f	0	0	0	Meetings	\N	26	-1	1
700	21	3	f	224	0	0	\N	\N	51	-1	1
700	12	6	f	0	0	0	workspace://SpacesStore/1373739a-2849-4647-9e97-7a4e05cc5841	\N	167	-1	1
700	0	0	f	0	0	0	\N	\N	168	-1	1
867	6	6	f	0	0	0	page.full-width-dashlet.user~admin~dashboard.xml	\N	26	-1	1
700	3	3	f	698	0	0	\N	\N	170	-1	1
700	1	1	t	0	0	0	\N	\N	155	-1	1
700	0	0	f	0	0	0	\N	\N	171	-1	1
867	6	6	f	0	0	0	admin	\N	40	-1	1
700	1	1	t	0	0	0	\N	\N	158	-1	1
700	1	1	t	0	0	0	\N	\N	159	-1	1
700	6	6	f	0	0	0	admin	\N	40	-1	1
700	0	0	f	0	0	0	\N	\N	27	-1	2
700	6	6	f	0	0	0	Meetings	\N	28	-1	4
700	6	6	f	0	0	0	Meetings	\N	28	-1	3
700	6	6	f	0	0	0	mjackson	\N	172	-1	1
700	7	6	f	0	0	0	2011-02-15T21:50:49.999Z	\N	173	-1	1
700	6	6	f	0	0	0	admin	\N	174	-1	1
700	7	6	f	0	0	0	2011-06-14T10:28:57.304Z	\N	175	-1	1
700	0	0	f	0	0	0	\N	\N	176	-1	1
700	0	0	f	0	0	0	\N	\N	154	-1	1
867	1	1	t	0	0	0	\N	\N	86	-1	1
698	0	0	f	0	0	0	\N	\N	171	-1	1
698	6	6	f	0	0	0	1.2	\N	157	-1	1
700	0	0	f	0	0	0	\N	\N	157	-1	1
700	6	6	f	0	0	0	1.2	\N	169	-1	1
701	6	6	f	0	0	0	admin	\N	40	-1	1
701	6	6	f	0	0	0	Milestones	\N	26	-1	1
701	1	1	t	0	0	0	\N	\N	155	-1	1
701	6	6	f	0	0	0	Milestones	\N	28	-1	3
867	1	1	f	0	0	0	\N	\N	87	-1	1
701	1	1	t	0	0	0	\N	\N	158	-1	1
701	1	1	t	0	0	0	\N	\N	159	-1	1
701	21	3	f	225	0	0	\N	\N	51	-1	1
702	6	6	f	0	0	0	3c73aace-9f54-420d-a1c0-c54b6a116dcf	\N	26	-1	1
702	6	6	f	0	0	0	3c73aace-9f54-420d-a1c0-c54b6a116dcf	\N	165	-1	1
703	6	6	f	0	0	0	Milestones	\N	26	-1	1
703	21	3	f	226	0	0	\N	\N	51	-1	1
703	12	6	f	0	0	0	workspace://SpacesStore/3c73aace-9f54-420d-a1c0-c54b6a116dcf	\N	167	-1	1
703	0	0	f	0	0	0	\N	\N	168	-1	1
703	3	3	f	701	0	0	\N	\N	170	-1	1
703	1	1	t	0	0	0	\N	\N	155	-1	1
703	0	0	f	0	0	0	\N	\N	171	-1	1
868	6	6	f	0	0	0	page.component-1-1.user~admin~dashboard.xml	\N	26	-1	1
703	1	1	t	0	0	0	\N	\N	158	-1	1
703	1	1	t	0	0	0	\N	\N	159	-1	1
703	6	6	f	0	0	0	admin	\N	40	-1	1
703	0	0	f	0	0	0	\N	\N	27	-1	2
703	6	6	f	0	0	0	Milestones	\N	28	-1	3
703	6	6	f	0	0	0	mjackson	\N	172	-1	1
703	7	6	f	0	0	0	2011-02-15T21:59:31.855Z	\N	173	-1	1
703	6	6	f	0	0	0	admin	\N	174	-1	1
703	7	6	f	0	0	0	2011-06-14T10:28:57.370Z	\N	175	-1	1
703	0	0	f	0	0	0	\N	\N	176	-1	1
703	0	0	f	0	0	0	\N	\N	154	-1	1
701	0	0	f	0	0	0	\N	\N	171	-1	1
701	6	6	f	0	0	0	1.0	\N	157	-1	1
703	0	0	f	0	0	0	\N	\N	157	-1	1
703	6	6	f	0	0	0	1.0	\N	169	-1	1
704	6	6	f	0	0	0	discussions	\N	26	-1	1
704	6	6	f	0	0	0	admin	\N	40	-1	1
704	6	6	f	0	0	0	discussions	\N	152	-1	1
705	6	6	f	0	0	0	post-1297807546884_964	\N	26	-1	1
705	6	6	f	0	0	0	admin	\N	40	-1	1
706	6	6	f	0	0	0	admin	\N	40	-1	1
706	6	6	f	0	0	0	post-1297807546884_964	\N	26	-1	1
706	7	6	f	0	0	0	2011-02-15T22:05:46.921Z	\N	218	-1	1
706	6	6	f	0	0	0	Images for the web site	\N	28	-1	3
706	21	3	f	227	0	0	\N	\N	51	-1	1
707	6	6	f	0	0	0	admin	\N	40	-1	1
707	6	6	f	0	0	0	post-1297807619797_315	\N	26	-1	1
707	7	6	f	0	0	0	2011-02-15T22:06:59.836Z	\N	218	-1	1
707	6	6	f	0	0	0		\N	28	-1	3
707	21	3	f	228	0	0	\N	\N	51	-1	1
708	6	6	f	0	0	0	admin	\N	40	-1	1
708	6	6	f	0	0	0	post-1297807729794_112	\N	26	-1	1
708	7	6	f	0	0	0	2011-02-15T22:08:49.829Z	\N	218	-1	1
708	6	6	f	0	0	0		\N	28	-1	3
708	21	3	f	229	0	0	\N	\N	51	-1	1
709	6	6	f	0	0	0	admin	\N	40	-1	1
709	6	6	f	0	0	0	post-1297807767790_183	\N	26	-1	1
709	7	6	f	0	0	0	2011-02-15T22:09:27.840Z	\N	218	-1	1
709	6	6	f	0	0	0		\N	28	-1	3
709	21	3	f	230	0	0	\N	\N	51	-1	1
710	6	6	f	0	0	0	post-1297807581026_873	\N	26	-1	1
710	6	6	f	0	0	0	admin	\N	40	-1	1
711	6	6	f	0	0	0	admin	\N	40	-1	1
711	6	6	f	0	0	0	post-1297807581026_873	\N	26	-1	1
711	7	6	f	0	0	0	2011-02-15T22:06:21.056Z	\N	218	-1	1
711	6	6	f	0	0	0	Web Content Management Technology	\N	28	-1	3
711	21	3	f	231	0	0	\N	\N	51	-1	1
712	6	6	f	0	0	0	admin	\N	40	-1	1
712	6	6	f	0	0	0	post-1297807650635_649	\N	26	-1	1
712	7	6	f	0	0	0	2011-02-15T22:07:30.663Z	\N	218	-1	1
712	6	6	f	0	0	0		\N	28	-1	3
712	7	6	f	0	0	0	2011-02-15T22:08:02.670Z	\N	221	-1	1
712	21	3	f	232	0	0	\N	\N	51	-1	1
713	2	3	f	0	0	0	\N	\N	82	-1	1
713	1	1	f	0	0	0	\N	\N	83	-1	1
713	1	1	f	0	0	0	\N	\N	84	-1	1
713	1	1	t	0	0	0	\N	\N	86	-1	1
713	1	1	f	0	0	0	\N	\N	87	-1	1
713	6	6	f	0	0	0	surf-config	\N	26	-1	1
713	1	1	f	0	0	0	\N	\N	222	-1	1
714	6	6	f	0	0	0	pages	\N	26	-1	1
714	1	1	t	0	0	0	\N	\N	86	-1	1
714	1	1	f	0	0	0	\N	\N	87	-1	1
715	6	6	f	0	0	0	site	\N	26	-1	1
715	1	1	t	0	0	0	\N	\N	86	-1	1
715	1	1	f	0	0	0	\N	\N	87	-1	1
716	6	6	f	0	0	0	swsdp	\N	26	-1	1
716	1	1	t	0	0	0	\N	\N	86	-1	1
716	1	1	f	0	0	0	\N	\N	87	-1	1
717	6	6	f	0	0	0	dashboard.xml	\N	26	-1	1
717	1	1	t	0	0	0	\N	\N	86	-1	1
717	1	1	f	0	0	0	\N	\N	87	-1	1
717	21	3	f	233	0	0	\N	\N	51	-1	1
718	6	6	f	0	0	0	components	\N	26	-1	1
718	1	1	t	0	0	0	\N	\N	86	-1	1
718	1	1	f	0	0	0	\N	\N	87	-1	1
719	6	6	f	0	0	0	page.component-1-1.site~swsdp~dashboard.xml	\N	26	-1	1
719	1	1	t	0	0	0	\N	\N	86	-1	1
719	1	1	f	0	0	0	\N	\N	87	-1	1
719	21	3	f	234	0	0	\N	\N	51	-1	1
720	6	6	f	0	0	0	page.component-1-3.site~swsdp~dashboard.xml	\N	26	-1	1
720	1	1	t	0	0	0	\N	\N	86	-1	1
720	1	1	f	0	0	0	\N	\N	87	-1	1
720	21	3	f	235	0	0	\N	\N	51	-1	1
721	6	6	f	0	0	0	page.component-2-1.site~swsdp~dashboard.xml	\N	26	-1	1
721	1	1	t	0	0	0	\N	\N	86	-1	1
721	1	1	f	0	0	0	\N	\N	87	-1	1
721	21	3	f	236	0	0	\N	\N	51	-1	1
722	6	6	f	0	0	0	page.component-2-2.site~swsdp~dashboard.xml	\N	26	-1	1
722	1	1	t	0	0	0	\N	\N	86	-1	1
722	1	1	f	0	0	0	\N	\N	87	-1	1
722	21	3	f	237	0	0	\N	\N	51	-1	1
723	6	6	f	0	0	0	page.component-2-3.site~swsdp~dashboard.xml	\N	26	-1	1
723	1	1	t	0	0	0	\N	\N	86	-1	1
723	1	1	f	0	0	0	\N	\N	87	-1	1
723	21	3	f	238	0	0	\N	\N	51	-1	1
724	6	6	f	0	0	0	page.navigation.site~swsdp~dashboard.xml	\N	26	-1	1
724	1	1	t	0	0	0	\N	\N	86	-1	1
724	1	1	f	0	0	0	\N	\N	87	-1	1
724	21	3	f	239	0	0	\N	\N	51	-1	1
725	6	6	f	0	0	0	page.title.site~swsdp~dashboard.xml	\N	26	-1	1
725	1	1	t	0	0	0	\N	\N	86	-1	1
725	1	1	f	0	0	0	\N	\N	87	-1	1
725	21	3	f	240	0	0	\N	\N	51	-1	1
726	6	6	f	0	0	0	page.component-1-2.site~swsdp~dashboard.xml	\N	26	-1	1
726	1	1	t	0	0	0	\N	\N	86	-1	1
726	1	1	f	0	0	0	\N	\N	87	-1	1
726	21	3	f	241	0	0	\N	\N	51	-1	1
727	6	6	f	0	0	0	page.component-1-4.site~swsdp~dashboard.xml	\N	26	-1	1
727	1	1	t	0	0	0	\N	\N	86	-1	1
727	1	1	f	0	0	0	\N	\N	87	-1	1
727	21	3	f	242	0	0	\N	\N	51	-1	1
603	0	0	f	0	0	0	\N	\N	154	-1	1
649	0	0	f	0	0	0	\N	\N	154	-1	1
650	0	0	f	0	0	0	\N	\N	154	-1	1
680	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
681	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
695	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
698	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
701	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
705	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
710	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
868	6	6	f	0	0	0	admin	\N	40	-1	1
868	1	1	t	0	0	0	\N	\N	86	-1	1
868	1	1	f	0	0	0	\N	\N	87	-1	1
869	6	6	f	0	0	0	page.component-1-2.user~admin~dashboard.xml	\N	26	-1	1
869	6	6	f	0	0	0	admin	\N	40	-1	1
730	6	6	f	0	0	0	org.alfresco.integrations.google.docs	\N	26	-1	1
730	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005342e302e3078	227	-1	1
730	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005342e302e3078	228	-1	1
731	6	6	f	0	0	0	org_alfresco_device_sync_repo	\N	26	-1	1
731	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005342e302e3078	227	-1	1
731	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005342e302e3078	228	-1	1
732	6	6	f	0	0	0	alfresco-share-services	\N	26	-1	1
732	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c00007870770a000832332e312e312e3478	227	-1	1
732	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c00007870770a000832332e312e312e3478	228	-1	1
733	6	6	f	0	0	0	alfresco-trashcan-cleaner	\N	26	-1	1
733	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005322e342e3278	227	-1	1
733	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005322e342e3278	228	-1	1
869	1	1	t	0	0	0	\N	\N	86	-1	1
7	21	3	f	244	0	0	\N	\N	229	-1	1
734	6	6	f	0	0	0	imageRenderingEngine	\N	70	-1	1
734	0	0	f	0	0	0	\N	\N	98	-1	1
734	0	0	f	0	0	0	\N	\N	99	-1	1
734	1	1	f	0	0	0	\N	\N	68	-1	1
734	0	0	f	0	0	0	\N	\N	100	-1	1
734	0	0	f	0	0	0	\N	\N	69	-1	1
734	6	6	f	0	0	0	New	\N	101	-1	1
734	0	0	f	0	0	0	\N	\N	71	-1	1
735	3	3	f	-1	0	0	\N	\N	77	-1	1
735	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
736	6	6	f	0	0	0	System	\N	77	-1	1
736	6	6	f	0	0	0	runAs	\N	78	-1	1
737	1	1	t	0	0	0	\N	\N	77	-1	1
737	6	6	f	0	0	0	autoOrientation	\N	78	-1	1
738	6	6	f	0	0	0	image/jpeg	\N	77	-1	1
738	6	6	f	0	0	0	mime-type	\N	78	-1	1
739	2	3	f	960	0	0	\N	\N	77	-1	1
739	6	6	f	0	0	0	ysize	\N	78	-1	1
740	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
740	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
741	6	6	f	0	0	0	imgpreview	\N	77	-1	1
741	6	6	f	0	0	0	use	\N	78	-1	1
742	3	3	f	-1	0	0	\N	\N	77	-1	1
742	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
743	2	3	f	960	0	0	\N	\N	77	-1	1
743	6	6	f	0	0	0	xsize	\N	78	-1	1
744	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}imgpreview	\N	77	-1	1
744	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
745	6	6	f	0	0	0	alfresco/thumbnail/thumbnail_placeholder_256.png	\N	77	-1	1
745	6	6	f	0	0	0	placeHolderResourcePath	\N	78	-1	1
746	2	3	f	-1	0	0	\N	\N	77	-1	1
746	6	6	f	0	0	0	pageLimit	\N	78	-1	1
747	3	3	f	-1	0	0	\N	\N	77	-1	1
747	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
748	2	3	f	-1	0	0	\N	\N	77	-1	1
748	6	6	f	0	0	0	maxPages	\N	78	-1	1
749	1	1	f	0	0	0	\N	\N	77	-1	1
749	6	6	f	0	0	0	isAbsolute	\N	78	-1	1
750	1	1	t	0	0	0	\N	\N	77	-1	1
750	6	6	f	0	0	0	resizeToThumbnail	\N	78	-1	1
751	6	6	f	0	0	0		\N	77	-1	1
751	6	6	f	0	0	0	commandOptions	\N	78	-1	1
752	1	1	f	0	0	0	\N	\N	77	-1	1
752	6	6	f	0	0	0	allowEnlargement	\N	78	-1	1
753	1	1	t	0	0	0	\N	\N	77	-1	1
753	6	6	f	0	0	0	maintainAspectRatio	\N	78	-1	1
754	3	3	f	-1	0	0	\N	\N	77	-1	1
754	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
869	1	1	f	0	0	0	\N	\N	87	-1	1
755	6	6	f	0	0	0	imageRenderingEngine	\N	70	-1	1
755	0	0	f	0	0	0	\N	\N	98	-1	1
755	0	0	f	0	0	0	\N	\N	99	-1	1
755	1	1	f	0	0	0	\N	\N	68	-1	1
755	0	0	f	0	0	0	\N	\N	100	-1	1
755	0	0	f	0	0	0	\N	\N	69	-1	1
755	6	6	f	0	0	0	New	\N	101	-1	1
755	0	0	f	0	0	0	\N	\N	71	-1	1
756	3	3	f	-1	0	0	\N	\N	77	-1	1
756	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
757	6	6	f	0	0	0	System	\N	77	-1	1
757	6	6	f	0	0	0	runAs	\N	78	-1	1
758	1	1	t	0	0	0	\N	\N	77	-1	1
758	6	6	f	0	0	0	autoOrientation	\N	78	-1	1
759	6	6	f	0	0	0	image/png	\N	77	-1	1
759	6	6	f	0	0	0	mime-type	\N	78	-1	1
760	2	3	f	100	0	0	\N	\N	77	-1	1
760	6	6	f	0	0	0	ysize	\N	78	-1	1
761	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
761	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
762	6	6	f	0	0	0	doclib	\N	77	-1	1
762	6	6	f	0	0	0	use	\N	78	-1	1
763	3	3	f	-1	0	0	\N	\N	77	-1	1
763	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
764	2	3	f	100	0	0	\N	\N	77	-1	1
764	6	6	f	0	0	0	xsize	\N	78	-1	1
765	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}doclib	\N	77	-1	1
765	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
766	6	6	f	0	0	0	alfresco/thumbnail/thumbnail_placeholder_doclib.png	\N	77	-1	1
766	6	6	f	0	0	0	placeHolderResourcePath	\N	78	-1	1
767	2	3	f	-1	0	0	\N	\N	77	-1	1
767	6	6	f	0	0	0	pageLimit	\N	78	-1	1
768	3	3	f	-1	0	0	\N	\N	77	-1	1
768	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
769	2	3	f	-1	0	0	\N	\N	77	-1	1
769	6	6	f	0	0	0	maxPages	\N	78	-1	1
770	1	1	f	0	0	0	\N	\N	77	-1	1
770	6	6	f	0	0	0	isAbsolute	\N	78	-1	1
771	1	1	t	0	0	0	\N	\N	77	-1	1
771	6	6	f	0	0	0	resizeToThumbnail	\N	78	-1	1
772	6	6	f	0	0	0		\N	77	-1	1
772	6	6	f	0	0	0	commandOptions	\N	78	-1	1
773	1	1	f	0	0	0	\N	\N	77	-1	1
773	6	6	f	0	0	0	allowEnlargement	\N	78	-1	1
774	1	1	t	0	0	0	\N	\N	77	-1	1
774	6	6	f	0	0	0	maintainAspectRatio	\N	78	-1	1
775	3	3	f	-1	0	0	\N	\N	77	-1	1
775	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
776	6	6	f	0	0	0	reformat	\N	70	-1	1
776	0	0	f	0	0	0	\N	\N	98	-1	1
776	0	0	f	0	0	0	\N	\N	99	-1	1
776	1	1	f	0	0	0	\N	\N	68	-1	1
776	0	0	f	0	0	0	\N	\N	100	-1	1
776	0	0	f	0	0	0	\N	\N	69	-1	1
776	6	6	f	0	0	0	New	\N	101	-1	1
776	0	0	f	0	0	0	\N	\N	71	-1	1
777	3	3	f	-1	0	0	\N	\N	77	-1	1
777	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
778	2	3	f	-1	0	0	\N	\N	77	-1	1
778	6	6	f	0	0	0	pageLimit	\N	78	-1	1
779	6	6	f	0	0	0	application/pdf	\N	77	-1	1
779	6	6	f	0	0	0	mime-type	\N	78	-1	1
780	6	6	f	0	0	0	9	\N	77	-1	1
780	6	6	f	0	0	0	flashVersion	\N	78	-1	1
781	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
781	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
782	3	3	f	-1	0	0	\N	\N	77	-1	1
782	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
783	6	6	f	0	0	0	pdf	\N	77	-1	1
783	6	6	f	0	0	0	use	\N	78	-1	1
784	2	3	f	-1	0	0	\N	\N	77	-1	1
784	6	6	f	0	0	0	maxPages	\N	78	-1	1
785	3	3	f	-1	0	0	\N	\N	77	-1	1
785	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
786	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}pdf	\N	77	-1	1
786	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
787	3	3	f	-1	0	0	\N	\N	77	-1	1
787	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
788	6	6	f	0	0	0	imageRenderingEngine	\N	70	-1	1
788	0	0	f	0	0	0	\N	\N	98	-1	1
788	0	0	f	0	0	0	\N	\N	99	-1	1
788	1	1	f	0	0	0	\N	\N	68	-1	1
788	0	0	f	0	0	0	\N	\N	100	-1	1
788	0	0	f	0	0	0	\N	\N	69	-1	1
788	6	6	f	0	0	0	New	\N	101	-1	1
788	0	0	f	0	0	0	\N	\N	71	-1	1
789	3	3	f	-1	0	0	\N	\N	77	-1	1
789	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
790	6	6	f	0	0	0	System	\N	77	-1	1
790	6	6	f	0	0	0	runAs	\N	78	-1	1
791	1	1	t	0	0	0	\N	\N	77	-1	1
791	6	6	f	0	0	0	autoOrientation	\N	78	-1	1
792	6	6	f	0	0	0	image/jpeg	\N	77	-1	1
792	6	6	f	0	0	0	mime-type	\N	78	-1	1
793	2	3	f	100	0	0	\N	\N	77	-1	1
793	6	6	f	0	0	0	ysize	\N	78	-1	1
794	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
794	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
795	6	6	f	0	0	0	medium	\N	77	-1	1
795	6	6	f	0	0	0	use	\N	78	-1	1
796	3	3	f	-1	0	0	\N	\N	77	-1	1
796	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
797	2	3	f	100	0	0	\N	\N	77	-1	1
797	6	6	f	0	0	0	xsize	\N	78	-1	1
798	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}medium	\N	77	-1	1
798	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
799	6	6	f	0	0	0	alfresco/thumbnail/thumbnail_placeholder_medium.jpg	\N	77	-1	1
799	6	6	f	0	0	0	placeHolderResourcePath	\N	78	-1	1
800	2	3	f	-1	0	0	\N	\N	77	-1	1
800	6	6	f	0	0	0	pageLimit	\N	78	-1	1
801	3	3	f	-1	0	0	\N	\N	77	-1	1
801	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
802	2	3	f	-1	0	0	\N	\N	77	-1	1
802	6	6	f	0	0	0	maxPages	\N	78	-1	1
803	1	1	f	0	0	0	\N	\N	77	-1	1
803	6	6	f	0	0	0	isAbsolute	\N	78	-1	1
804	1	1	t	0	0	0	\N	\N	77	-1	1
804	6	6	f	0	0	0	resizeToThumbnail	\N	78	-1	1
805	6	6	f	0	0	0		\N	77	-1	1
805	6	6	f	0	0	0	commandOptions	\N	78	-1	1
806	1	1	t	0	0	0	\N	\N	77	-1	1
806	6	6	f	0	0	0	allowEnlargement	\N	78	-1	1
807	1	1	t	0	0	0	\N	\N	77	-1	1
807	6	6	f	0	0	0	maintainAspectRatio	\N	78	-1	1
808	3	3	f	-1	0	0	\N	\N	77	-1	1
808	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
809	6	6	f	0	0	0	imageRenderingEngine	\N	70	-1	1
809	0	0	f	0	0	0	\N	\N	98	-1	1
809	0	0	f	0	0	0	\N	\N	99	-1	1
809	1	1	f	0	0	0	\N	\N	68	-1	1
809	0	0	f	0	0	0	\N	\N	100	-1	1
809	0	0	f	0	0	0	\N	\N	69	-1	1
809	6	6	f	0	0	0	New	\N	101	-1	1
809	0	0	f	0	0	0	\N	\N	71	-1	1
810	3	3	f	-1	0	0	\N	\N	77	-1	1
810	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
811	6	6	f	0	0	0	System	\N	77	-1	1
811	6	6	f	0	0	0	runAs	\N	78	-1	1
812	1	1	t	0	0	0	\N	\N	77	-1	1
812	6	6	f	0	0	0	autoOrientation	\N	78	-1	1
813	6	6	f	0	0	0	image/png	\N	77	-1	1
813	6	6	f	0	0	0	mime-type	\N	78	-1	1
814	2	3	f	64	0	0	\N	\N	77	-1	1
814	6	6	f	0	0	0	ysize	\N	78	-1	1
815	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
815	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
816	6	6	f	0	0	0	avatar	\N	77	-1	1
816	6	6	f	0	0	0	use	\N	78	-1	1
817	3	3	f	-1	0	0	\N	\N	77	-1	1
817	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
818	2	3	f	64	0	0	\N	\N	77	-1	1
818	6	6	f	0	0	0	xsize	\N	78	-1	1
819	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}avatar	\N	77	-1	1
819	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
820	6	6	f	0	0	0	alfresco/thumbnail/thumbnail_placeholder_avatar.png	\N	77	-1	1
820	6	6	f	0	0	0	placeHolderResourcePath	\N	78	-1	1
821	2	3	f	-1	0	0	\N	\N	77	-1	1
821	6	6	f	0	0	0	pageLimit	\N	78	-1	1
822	3	3	f	-1	0	0	\N	\N	77	-1	1
822	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
823	2	3	f	-1	0	0	\N	\N	77	-1	1
823	6	6	f	0	0	0	maxPages	\N	78	-1	1
824	1	1	f	0	0	0	\N	\N	77	-1	1
824	6	6	f	0	0	0	isAbsolute	\N	78	-1	1
825	1	1	t	0	0	0	\N	\N	77	-1	1
825	6	6	f	0	0	0	resizeToThumbnail	\N	78	-1	1
826	6	6	f	0	0	0		\N	77	-1	1
826	6	6	f	0	0	0	commandOptions	\N	78	-1	1
827	1	1	f	0	0	0	\N	\N	77	-1	1
827	6	6	f	0	0	0	allowEnlargement	\N	78	-1	1
828	1	1	t	0	0	0	\N	\N	77	-1	1
828	6	6	f	0	0	0	maintainAspectRatio	\N	78	-1	1
829	3	3	f	-1	0	0	\N	\N	77	-1	1
829	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
830	6	6	f	0	0	0	reformat	\N	70	-1	1
830	0	0	f	0	0	0	\N	\N	98	-1	1
830	0	0	f	0	0	0	\N	\N	99	-1	1
830	1	1	f	0	0	0	\N	\N	68	-1	1
830	0	0	f	0	0	0	\N	\N	100	-1	1
830	0	0	f	0	0	0	\N	\N	69	-1	1
830	6	6	f	0	0	0	New	\N	101	-1	1
830	0	0	f	0	0	0	\N	\N	71	-1	1
831	3	3	f	-1	0	0	\N	\N	77	-1	1
831	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
832	6	6	f	0	0	0	System	\N	77	-1	1
832	6	6	f	0	0	0	runAs	\N	78	-1	1
833	6	6	f	0	0	0	application/x-shockwave-flash	\N	77	-1	1
833	6	6	f	0	0	0	mime-type	\N	78	-1	1
834	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
834	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
835	6	6	f	0	0	0	webpreview	\N	77	-1	1
835	6	6	f	0	0	0	use	\N	78	-1	1
836	3	3	f	-1	0	0	\N	\N	77	-1	1
836	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
837	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}webpreview	\N	77	-1	1
837	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
838	2	3	f	-1	0	0	\N	\N	77	-1	1
838	6	6	f	0	0	0	pageLimit	\N	78	-1	1
839	6	6	f	0	0	0	9	\N	77	-1	1
839	6	6	f	0	0	0	flashVersion	\N	78	-1	1
840	3	3	f	-1	0	0	\N	\N	77	-1	1
840	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
841	2	3	f	-1	0	0	\N	\N	77	-1	1
841	6	6	f	0	0	0	maxPages	\N	78	-1	1
842	3	3	f	-1	0	0	\N	\N	77	-1	1
842	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
843	6	6	f	0	0	0	imageRenderingEngine	\N	70	-1	1
843	0	0	f	0	0	0	\N	\N	98	-1	1
843	0	0	f	0	0	0	\N	\N	99	-1	1
843	1	1	f	0	0	0	\N	\N	68	-1	1
843	0	0	f	0	0	0	\N	\N	100	-1	1
843	0	0	f	0	0	0	\N	\N	69	-1	1
843	6	6	f	0	0	0	New	\N	101	-1	1
843	0	0	f	0	0	0	\N	\N	71	-1	1
844	3	3	f	-1	0	0	\N	\N	77	-1	1
844	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
845	6	6	f	0	0	0	System	\N	77	-1	1
845	6	6	f	0	0	0	runAs	\N	78	-1	1
846	1	1	t	0	0	0	\N	\N	77	-1	1
846	6	6	f	0	0	0	autoOrientation	\N	78	-1	1
847	6	6	f	0	0	0	image/png	\N	77	-1	1
847	6	6	f	0	0	0	mime-type	\N	78	-1	1
848	2	3	f	32	0	0	\N	\N	77	-1	1
848	6	6	f	0	0	0	ysize	\N	78	-1	1
849	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
849	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
850	6	6	f	0	0	0	avatar32	\N	77	-1	1
850	6	6	f	0	0	0	use	\N	78	-1	1
851	3	3	f	-1	0	0	\N	\N	77	-1	1
851	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
852	2	3	f	32	0	0	\N	\N	77	-1	1
852	6	6	f	0	0	0	xsize	\N	78	-1	1
853	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}avatar32	\N	77	-1	1
853	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
854	6	6	f	0	0	0	alfresco/thumbnail/thumbnail_placeholder_avatar32.png	\N	77	-1	1
854	6	6	f	0	0	0	placeHolderResourcePath	\N	78	-1	1
855	2	3	f	-1	0	0	\N	\N	77	-1	1
855	6	6	f	0	0	0	pageLimit	\N	78	-1	1
856	3	3	f	-1	0	0	\N	\N	77	-1	1
856	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
857	2	3	f	-1	0	0	\N	\N	77	-1	1
857	6	6	f	0	0	0	maxPages	\N	78	-1	1
858	1	1	f	0	0	0	\N	\N	77	-1	1
858	6	6	f	0	0	0	isAbsolute	\N	78	-1	1
859	1	1	t	0	0	0	\N	\N	77	-1	1
859	6	6	f	0	0	0	resizeToThumbnail	\N	78	-1	1
860	6	6	f	0	0	0		\N	77	-1	1
860	6	6	f	0	0	0	commandOptions	\N	78	-1	1
861	1	1	f	0	0	0	\N	\N	77	-1	1
861	6	6	f	0	0	0	allowEnlargement	\N	78	-1	1
862	1	1	t	0	0	0	\N	\N	77	-1	1
862	6	6	f	0	0	0	maintainAspectRatio	\N	78	-1	1
863	3	3	f	-1	0	0	\N	\N	77	-1	1
863	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
864	1	1	t	0	0	0	\N	\N	86	-1	1
864	1	1	f	0	0	0	\N	\N	87	-1	1
864	6	6	f	0	0	0	Solr Facets Space	\N	26	-1	1
864	6	6	f	0	0	0	Root folder for the Solr Facet properties\n         	\N	27	-1	2
864	6	6	f	0	0	0	Solr Facets Space	\N	28	-1	2
864	6	6	f	0	0	0	filter_mimetype	\N	231	1	1
864	6	6	f	0	0	0	filter_created	\N	231	2	1
864	6	6	f	0	0	0	filter_content_size	\N	231	3	1
864	6	6	f	0	0	0	filter_modifier	\N	231	4	1
864	6	6	f	0	0	0	filter_modified	\N	231	5	1
864	6	6	f	0	0	0	filter_creator	\N	231	0	1
870	6	6	f	0	0	0	page.component-2-1.user~admin~dashboard.xml	\N	26	-1	1
870	6	6	f	0	0	0	admin	\N	40	-1	1
870	1	1	t	0	0	0	\N	\N	86	-1	1
870	1	1	f	0	0	0	\N	\N	87	-1	1
871	1	1	t	0	0	0	\N	\N	86	-1	1
871	1	1	f	0	0	0	\N	\N	87	-1	1
872	6	6	f	0	0	0	pages	\N	26	-1	1
872	1	1	f	0	0	0	\N	\N	86	-1	1
872	1	1	f	0	0	0	\N	\N	87	-1	1
873	6	6	f	0	0	0	user	\N	26	-1	1
873	1	1	f	0	0	0	\N	\N	86	-1	1
873	1	1	f	0	0	0	\N	\N	87	-1	1
874	6	6	f	0	0	0	admin	\N	26	-1	1
874	1	1	f	0	0	0	\N	\N	86	-1	1
874	1	1	f	0	0	0	\N	\N	87	-1	1
872	6	6	f	0	0	0	admin	\N	40	-1	1
873	6	6	f	0	0	0	admin	\N	40	-1	1
874	6	6	f	0	0	0	admin	\N	40	-1	1
875	6	6	f	0	0	0	dashboard.xml	\N	26	-1	1
875	6	6	f	0	0	0	admin	\N	40	-1	1
875	1	1	t	0	0	0	\N	\N	86	-1	1
875	1	1	f	0	0	0	\N	\N	87	-1	1
866	21	3	f	252	0	0	\N	\N	51	-1	1
867	21	3	f	253	0	0	\N	\N	51	-1	1
868	21	3	f	254	0	0	\N	\N	51	-1	1
869	21	3	f	255	0	0	\N	\N	51	-1	1
870	21	3	f	256	0	0	\N	\N	51	-1	1
871	21	3	f	257	0	0	\N	\N	51	-1	1
875	21	3	f	258	0	0	\N	\N	51	-1	1
876	6	6	f	0	0	0	Animals	\N	26	-1	1
876	6	6	f	0	0	0	This folder contains overview of the animals in our zoo	\N	27	-1	2
876	6	6	f	0	0	0	Animals overview	\N	28	-1	2
877	6	6	f	0	0	0	Animal Species	\N	26	-1	1
878	6	6	f	0	0	0	Mammal	\N	26	-1	1
879	6	6	f	0	0	0	Fish	\N	26	-1	1
880	6	6	f	0	0	0	Animals list	\N	26	-1	1
880	21	3	f	260	0	0	\N	\N	51	-1	1
880	1	1	t	0	0	0	\N	\N	52	-1	1
880	6	6	f	0	0	0	List of animals in our zoo	\N	27	-1	2
880	6	6	f	0	0	0	List of animals	\N	28	-1	2
882	1	1	t	0	0	0	\N	\N	86	-1	1
882	1	1	f	0	0	0	\N	\N	87	-1	1
882	6	6	f	0	0	0	pdf	\N	26	-1	1
882	21	3	f	262	0	0	\N	\N	51	-1	1
882	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
883	3	3	f	881	0	0	\N	\N	149	-1	1
882	6	6	f	0	0	0	pdf	\N	144	-1	1
884	1	1	t	0	0	0	\N	\N	86	-1	1
884	1	1	f	0	0	0	\N	\N	87	-1	1
884	6	6	f	0	0	0	doclib	\N	26	-1	1
884	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
884	6	6	f	0	0	0	doclib	\N	144	-1	1
884	21	3	f	263	0	0	\N	\N	51	-1	1
884	2	3	f	655266242	0	0	\N	\N	235	-1	1
880	6	6	f	0	0	0	pdf:1708329629285	\N	138	0	1
880	6	6	f	0	0	0	doclib:1708329633941	\N	138	1	1
885	6	6	f	0	0	0	carp.pdf	\N	26	-1	1
885	21	3	f	265	0	0	\N	\N	51	-1	1
885	1	1	f	0	0	0	\N	\N	155	-1	1
885	1	1	t	0	0	0	\N	\N	158	-1	1
885	1	1	t	0	0	0	\N	\N	159	-1	1
886	6	6	f	0	0	0	02acf462-533d-4e1b-9825-05fa934140da	\N	26	-1	1
886	6	6	f	0	0	0	02acf462-533d-4e1b-9825-05fa934140da	\N	165	-1	1
887	6	6	f	0	0	0	carp.pdf	\N	26	-1	1
887	21	3	f	266	0	0	\N	\N	51	-1	1
887	12	6	f	0	0	0	workspace://SpacesStore/02acf462-533d-4e1b-9825-05fa934140da	\N	167	-1	1
887	0	0	f	0	0	0	\N	\N	168	-1	1
887	6	6	f	0	0	0	1.0	\N	169	-1	1
887	3	3	f	885	0	0	\N	\N	170	-1	1
887	9	9	f	0	0	0	\N	\\xaced00057e72002c6f72672e616c66726573636f2e736572766963652e636d722e76657273696f6e2e56657273696f6e5479706500000000000000001200007872000e6a6176612e6c616e672e456e756d000000000000000012000078707400054d414a4f52	236	-1	1
887	1	1	f	0	0	0	\N	\N	155	-1	1
888	1	1	t	0	0	0	\N	\N	86	-1	1
887	0	0	f	0	0	0	\N	\N	171	-1	1
887	0	0	f	0	0	0	\N	\N	157	-1	1
887	1	1	t	0	0	0	\N	\N	158	-1	1
887	1	1	t	0	0	0	\N	\N	159	-1	1
887	6	6	f	0	0	0	admin	\N	172	-1	1
887	7	6	f	0	0	0	2024-02-19T08:09:32.182Z	\N	173	-1	1
887	6	6	f	0	0	0	admin	\N	174	-1	1
887	7	6	f	0	0	0	2024-02-19T08:09:32.182Z	\N	175	-1	1
887	0	0	f	0	0	0	\N	\N	176	-1	1
885	6	6	f	0	0	0	1.0	\N	157	-1	1
885	6	6	f	0	0	0	MAJOR	\N	171	-1	1
888	1	1	f	0	0	0	\N	\N	87	-1	1
888	6	6	f	0	0	0	doclib	\N	26	-1	1
888	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
888	6	6	f	0	0	0	doclib	\N	144	-1	1
888	21	3	f	267	0	0	\N	\N	51	-1	1
888	2	3	f	-1625856254	0	0	\N	\N	235	-1	1
885	6	6	f	0	0	0	doclib:1708330172467	\N	138	0	1
889	6	6	f	0	0	0	giraffe.pdf	\N	26	-1	1
889	21	3	f	269	0	0	\N	\N	51	-1	1
889	1	1	f	0	0	0	\N	\N	155	-1	1
889	1	1	t	0	0	0	\N	\N	158	-1	1
889	1	1	t	0	0	0	\N	\N	159	-1	1
890	6	6	f	0	0	0	f9d6264e-426b-41cd-9f4b-b660dc582311	\N	26	-1	1
890	6	6	f	0	0	0	f9d6264e-426b-41cd-9f4b-b660dc582311	\N	165	-1	1
891	6	6	f	0	0	0	giraffe.pdf	\N	26	-1	1
891	21	3	f	270	0	0	\N	\N	51	-1	1
891	12	6	f	0	0	0	workspace://SpacesStore/f9d6264e-426b-41cd-9f4b-b660dc582311	\N	167	-1	1
891	0	0	f	0	0	0	\N	\N	168	-1	1
891	6	6	f	0	0	0	1.0	\N	169	-1	1
891	3	3	f	889	0	0	\N	\N	170	-1	1
891	9	9	f	0	0	0	\N	\\xaced00057e72002c6f72672e616c66726573636f2e736572766963652e636d722e76657273696f6e2e56657273696f6e5479706500000000000000001200007872000e6a6176612e6c616e672e456e756d000000000000000012000078707400054d414a4f52	236	-1	1
891	1	1	f	0	0	0	\N	\N	155	-1	1
891	0	0	f	0	0	0	\N	\N	171	-1	1
891	0	0	f	0	0	0	\N	\N	157	-1	1
891	1	1	t	0	0	0	\N	\N	158	-1	1
891	1	1	t	0	0	0	\N	\N	159	-1	1
891	6	6	f	0	0	0	admin	\N	172	-1	1
891	7	6	f	0	0	0	2024-02-19T08:09:40.810Z	\N	173	-1	1
891	6	6	f	0	0	0	admin	\N	174	-1	1
891	7	6	f	0	0	0	2024-02-19T08:09:40.810Z	\N	175	-1	1
891	0	0	f	0	0	0	\N	\N	176	-1	1
889	6	6	f	0	0	0	1.0	\N	157	-1	1
889	6	6	f	0	0	0	MAJOR	\N	171	-1	1
892	6	6	f	0	0	0	porcupine.pdf	\N	26	-1	1
892	21	3	f	272	0	0	\N	\N	51	-1	1
892	1	1	f	0	0	0	\N	\N	155	-1	1
892	1	1	t	0	0	0	\N	\N	158	-1	1
892	1	1	t	0	0	0	\N	\N	159	-1	1
893	6	6	f	0	0	0	71b5b65b-d92a-4944-9403-48b7ebf8664c	\N	26	-1	1
893	6	6	f	0	0	0	71b5b65b-d92a-4944-9403-48b7ebf8664c	\N	165	-1	1
894	6	6	f	0	0	0	porcupine.pdf	\N	26	-1	1
894	21	3	f	273	0	0	\N	\N	51	-1	1
894	12	6	f	0	0	0	workspace://SpacesStore/71b5b65b-d92a-4944-9403-48b7ebf8664c	\N	167	-1	1
894	0	0	f	0	0	0	\N	\N	168	-1	1
894	6	6	f	0	0	0	1.0	\N	169	-1	1
894	3	3	f	892	0	0	\N	\N	170	-1	1
894	9	9	f	0	0	0	\N	\\xaced00057e72002c6f72672e616c66726573636f2e736572766963652e636d722e76657273696f6e2e56657273696f6e5479706500000000000000001200007872000e6a6176612e6c616e672e456e756d000000000000000012000078707400054d414a4f52	236	-1	1
894	1	1	f	0	0	0	\N	\N	155	-1	1
894	0	0	f	0	0	0	\N	\N	171	-1	1
894	0	0	f	0	0	0	\N	\N	157	-1	1
894	1	1	t	0	0	0	\N	\N	158	-1	1
894	1	1	t	0	0	0	\N	\N	159	-1	1
894	6	6	f	0	0	0	admin	\N	172	-1	1
894	7	6	f	0	0	0	2024-02-19T08:09:40.919Z	\N	173	-1	1
894	6	6	f	0	0	0	admin	\N	174	-1	1
894	7	6	f	0	0	0	2024-02-19T08:09:40.919Z	\N	175	-1	1
894	0	0	f	0	0	0	\N	\N	176	-1	1
892	6	6	f	0	0	0	1.0	\N	157	-1	1
892	6	6	f	0	0	0	MAJOR	\N	171	-1	1
892	6	6	f	0	0	0	doclib:1708330181340	\N	138	0	1
896	1	1	t	0	0	0	\N	\N	86	-1	1
896	1	1	f	0	0	0	\N	\N	87	-1	1
896	6	6	f	0	0	0	doclib	\N	26	-1	1
896	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
896	6	6	f	0	0	0	doclib	\N	144	-1	1
896	21	3	f	275	0	0	\N	\N	51	-1	1
896	2	3	f	1454908048	0	0	\N	\N	235	-1	1
895	1	1	t	0	0	0	\N	\N	86	-1	1
895	1	1	f	0	0	0	\N	\N	87	-1	1
895	6	6	f	0	0	0	doclib	\N	26	-1	1
895	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
895	6	6	f	0	0	0	doclib	\N	144	-1	1
895	21	3	f	274	0	0	\N	\N	51	-1	1
895	2	3	f	608112509	0	0	\N	\N	235	-1	1
889	6	6	f	0	0	0	doclib:1708330181376	\N	138	0	1
880	12	6	f	0	0	0	workspace://SpacesStore/25805c3b-dc41-4a3d-9b03-a44ac7963c70	\N	48	0	1
880	0	0	f	0	0	0	\N	\N	154	-1	1
885	12	6	f	0	0	0	workspace://SpacesStore/fa6b38cd-442a-4f77-9d3e-dc212a6b809e	\N	48	0	1
885	0	0	f	0	0	0	\N	\N	154	-1	1
885	6	6	f	0	0	0		\N	27	-1	2
885	6	6	f	0	0	0		\N	28	-1	2
889	12	6	f	0	0	0	workspace://SpacesStore/11dedf84-4ebb-431e-adbf-7e92b2792674	\N	48	0	1
889	0	0	f	0	0	0	\N	\N	154	-1	1
889	6	6	f	0	0	0		\N	27	-1	2
889	6	6	f	0	0	0		\N	28	-1	2
892	12	6	f	0	0	0	workspace://SpacesStore/11dedf84-4ebb-431e-adbf-7e92b2792674	\N	48	0	1
892	0	0	f	0	0	0	\N	\N	154	-1	1
892	6	6	f	0	0	0		\N	27	-1	2
892	6	6	f	0	0	0		\N	28	-1	2
\.


--
-- Data for Name: alf_permission; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_permission (id, version, type_qname_id, name) FROM stdin;
1	0	21	All
2	0	32	Consumer
3	0	32	Contributor
4	0	34	Read
5	0	32	Collaborator
6	0	34	AddChildren
7	0	34	FullControl
8	0	106	SiteManager
9	0	106	SiteCollaborator
10	0	106	SiteContributor
11	0	106	SiteConsumer
12	0	34	ReadPermissions
\.


--
-- Data for Name: alf_prop_class; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_prop_class (id, java_class_name, java_class_name_short, java_class_name_crc) FROM stdin;
1	java.lang.String	java.lang.string	2004016611
2	java.lang.Object	java.lang.object	1096926374
3	org.alfresco.util.Pair	org.alfresco.util.pair	1801692418
4	java.util.HashSet	java.util.hashset	1058840703
5	java.lang.Long	java.lang.long	4227064769
6	java.lang.Integer	java.lang.integer	3438268394
7	java.lang.Boolean	java.lang.boolean	476441737
8	java.util.Date	java.util.date	4053956859
\.


--
-- Data for Name: alf_prop_date_value; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_prop_date_value (date_value, full_year, half_of_year, quarter_of_year, month_of_year, week_of_year, week_of_month, day_of_year, day_of_month, day_of_week) FROM stdin;
\.


--
-- Data for Name: alf_prop_double_value; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_prop_double_value (id, double_value) FROM stdin;
\.


--
-- Data for Name: alf_prop_link; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_prop_link (root_prop_id, prop_index, contained_in, key_prop_id, value_prop_id) FROM stdin;
1	0	0	4	4
2	0	0	6	6
3	0	0	10	10
4	0	0	12	12
5	0	0	14	14
6	0	0	12	12
7	0	0	18	18
8	0	0	22	22
9	0	0	24	24
10	0	0	26	26
11	0	0	28	28
12	0	0	30	30
13	0	0	32	32
14	0	0	35	35
15	0	0	37	37
16	0	0	39	39
17	0	0	41	41
18	0	0	44	44
19	0	0	45	45
20	0	0	46	46
21	0	0	48	48
22	0	0	49	49
23	0	0	50	50
24	0	0	51	51
25	0	0	50	50
26	0	0	52	52
27	0	0	12	12
28	0	0	22	22
29	0	0	54	54
30	0	0	26	26
31	0	0	28	28
32	0	0	55	55
33	0	0	32	32
34	0	0	56	56
35	0	0	37	37
36	0	0	39	39
37	0	0	41	41
38	0	0	44	44
39	0	0	45	45
40	0	0	57	57
\.


--
-- Data for Name: alf_prop_root; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_prop_root (id, version) FROM stdin;
1	0
2	0
3	0
4	0
5	0
6	0
7	0
8	0
9	0
10	0
11	0
12	0
13	0
14	0
15	0
16	0
17	0
18	0
19	0
20	0
21	0
22	0
23	0
24	0
25	0
26	0
27	0
28	0
29	0
30	0
31	0
32	0
33	0
34	0
35	0
36	0
37	0
38	0
39	0
40	0
\.


--
-- Data for Name: alf_prop_serializable_value; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_prop_serializable_value (id, serializable_value) FROM stdin;
1	\\xaced0005737200166f72672e616c66726573636f2e7574696c2e506169729937b84d08eece6c0200024c000566697273747400124c6a6176612f6c616e672f4f626a6563743b4c00067365636f6e6471007e0001787074002438313436366565342d646666612d343265342d383564662d313737333533343064646438737200196a617661782e63727970746f2e5365616c65644f626a6563743e363da6c3b754700200045b000d656e636f646564506172616d737400025b425b0010656e63727970746564436f6e74656e7471007e00054c0009706172616d73416c677400124c6a6176612f6c616e672f537472696e673b4c00077365616c416c6771007e00067870757200025b42acf317f8060854e002000078700000000a0408f312e8c0303d82477571007e000800000030c8cc43cf72d62276b1f1c81b9d290fb9c7fb733df09b8f23c3523ba4d052008b5ac77ecbd4142081b2b853da3c45dc637400064445536564657400174445536564652f4342432f504b43533550616464696e67
2	\\xaced0005737200166f72672e616c66726573636f2e7574696c2e506169729937b84d08eece6c0200024c000566697273747400124c6a6176612f6c616e672f4f626a6563743b4c00067365636f6e6471007e0001787074002438313562343336612d343565392d346135642d616463372d333731636438666261353661737200196a617661782e63727970746f2e5365616c65644f626a6563743e363da6c3b754700200045b000d656e636f646564506172616d737400025b425b0010656e63727970746564436f6e74656e7471007e00054c0009706172616d73416c677400124c6a6176612f6c616e672f537472696e673b4c00077365616c416c6771007e00067870757200025b42acf317f8060854e002000078700000000a0408c3e8dc74272b41627571007e000800000030402b8f5b4a5ca2e98e72fd2bee78f51566859de0ba3a2fc3ac7a856ac82c2f8bec71f99fb42024e69bdf6f327e9e33c17400064445536564657400174445536564652f4342432f504b43533550616464696e67
\.


--
-- Data for Name: alf_prop_string_value; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_prop_string_value (id, string_value, string_end_lower, string_crc) FROM stdin;
1	keyCheck	keycheck	644688069
2	metadata	metadata	1326724116
3	Alfresco Tagging Service	 tagging service	590014304
4	java.util.HashSet	ava.util.hashset	1058840703
5	.repoUsages	.repousages	2000607251
6	current	current	3706926091
7	lastUpdateUsers	lastupdateusers	1353234476
8	authorizedUsers	authorizedusers	3011678754
9	lastUpdateDocuments	tupdatedocuments	4028376191
10	documents	documents	2729472648
11	.clusterInfo	.clusterinfo	2569032364
12	.cluster_name	.cluster_name	3908740078
13	MainRepository-3db64f38-f2c7-4f77-ac4a-b1c8d30d541c	c4a-b1c8d30d541c	170564554
14	.clusterMembers	.clustermembers	501968308
15	172.18.0.2:5701	172.18.0.2:5701	4066841211
16	.host_name	.host_name	3564746463
17	8340fd5d057c	8340fd5d057c	1344176205
18	.ip_address	.ip_address	2115545287
19	172.18.0.2	172.18.0.2	2409658580
20	.port	.port	3657884840
21	.clustering_enabled	ustering_enabled	2216157857
22	.last_registered	.last_registered	2925481710
23	.cluster_node_type	luster_node_type	3072798820
24	Repository server	epository server	1564686447
25	.ChainingUserRegistrySynchronizer	strysynchronizer	3393647685
26	START_TIME	start_time	2591279036
27	END_TIME	end_time	1185195849
28	LAST_RUN_HOST	last_run_host	1321510405
29	localhost:8080	localhost:8080	859665970
30	STATUS	status	2348327578
31	IN_PROGRESS	in_progress	1779080785
32	LAST_ERROR	last_error	866488931
33	SUMMARY	summary	4050491513
34	.empty	.empty	578883388
35	COMPLETE	complete	2220117103
36	authorization	authorization	2053999599
37	172.18.0.5:5701	172.18.0.5:5701	4016186051
38	172.18.0.5	172.18.0.5	298118519
\.


--
-- Data for Name: alf_prop_unique_ctx; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_prop_unique_ctx (id, version, value1_prop_id, value2_prop_id, value3_prop_id, prop1_id) FROM stdin;
1	0	1	2	3	1
6	0	16	17	3	7
7	0	19	20	21	8
8	0	19	20	23	9
9	0	19	20	25	10
10	0	19	20	27	11
11	0	19	20	29	12
12	0	19	20	31	13
19	0	1	47	3	21
2	2	7	8	9	24
3	2	7	8	11	25
4	1	7	8	13	26
5	1	7	8	15	27
20	0	19	53	21	28
21	0	19	53	23	29
22	0	19	53	25	30
23	0	19	53	27	31
24	0	19	53	29	32
25	0	19	53	31	33
13	1	33	34	3	34
15	1	33	38	3	36
17	1	33	42	3	\N
18	1	33	43	3	38
16	3	33	40	3	39
14	3	33	36	3	40
\.


--
-- Data for Name: alf_prop_value; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_prop_value (id, actual_type_id, persisted_type, long_value) FROM stdin;
1	1	3	1
2	1	3	2
3	2	0	0
4	3	4	1
5	1	3	3
6	4	5	4
7	1	3	5
8	1	3	6
9	1	3	7
10	5	1	1708329073927
11	1	3	8
12	5	1	0
13	1	3	9
14	5	1	1708329073933
15	1	3	10
16	1	3	11
17	1	3	12
18	1	3	13
19	1	3	14
20	1	3	15
21	1	3	16
22	1	3	17
23	1	3	18
24	1	3	19
25	1	3	20
26	6	1	5701
27	1	3	21
28	7	1	1
29	1	3	22
30	8	1	1708329086940
31	1	3	23
32	1	3	24
33	1	3	25
34	1	3	26
35	5	1	1708329088815
36	1	3	27
37	5	1	-1
38	1	3	28
39	1	3	29
40	1	3	30
41	1	3	31
42	1	3	32
43	1	3	33
44	1	3	34
45	1	3	35
46	5	1	1708329088824
47	1	3	36
48	3	4	2
49	5	1	1708329088977
50	5	1	1
51	5	1	1708334944968
52	5	1	1708334944974
53	1	3	37
54	1	3	38
55	8	1	1708334947128
56	5	1	1708334947698
57	5	1	1708334947710
\.


--
-- Data for Name: alf_qname; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_qname (id, version, ns_id, local_name) FROM stdin;
1	0	1	store_root
2	0	1	aspect_root
3	0	1	container
4	0	1	children
5	0	2	user
6	0	2	enabled
7	0	2	accountLocked
8	0	2	credentialsExpire
9	0	2	username
10	0	2	password
11	0	2	accountExpires
12	0	1	descriptor
13	0	1	versionMajor
14	0	1	versionBuild
15	0	1	versionRevision
16	0	1	name
17	0	1	versionLabel
18	0	1	versionSchema
19	0	1	versionMinor
20	0	1	versionProperties
21	0	3	All
22	0	4	versionStoreRoot
23	0	5	versionStoreRoot
24	0	6	folder
25	0	6	auditable
26	0	6	name
27	0	6	description
28	0	6	title
29	0	7	icon
30	0	6	titled
31	0	7	uifacets
32	0	6	cmobject
33	0	6	contains
34	0	1	base
35	0	6	person
36	0	6	sizeCurrent
37	0	6	userName
38	0	6	email
39	0	6	homeFolderProvider
40	0	6	owner
41	0	6	lastName
42	0	6	organizationId
43	0	6	firstName
44	0	6	ownable
45	0	6	homeFolder
46	0	6	category_root
47	0	6	category
48	0	6	categories
49	0	6	subcategories
50	0	6	mlRoot
51	0	6	content
52	0	7	editInline
53	0	7	inlineeditable
54	0	6	author
55	0	6	source
56	0	6	templatable
57	0	6	template
58	0	8	rules
59	0	6	systemfolder
60	0	8	ruleFolder
61	0	8	rule
62	0	8	applyToChildren
63	0	8	disabled
64	0	8	executeAsynchronously
65	0	8	ruleType
66	0	9	compositeaction
67	0	8	action
68	0	9	executeAsynchronously
69	0	9	actionTitle
70	0	9	definitionName
71	0	9	actionDescription
72	0	9	actioncondition
73	0	9	conditions
74	0	9	invert
75	0	9	actionparameter
76	0	9	parameters
77	0	9	parameterValue
78	0	9	parameterName
79	0	9	action
80	0	9	actions
81	0	10	sites
82	0	1	clientVisibilityMask
83	0	1	cascadeHidden
84	0	1	cascadeIndexControl
85	0	1	hidden
86	0	6	isContentIndexed
87	0	6	isIndexed
88	0	6	indexControl
89	0	6	authorityContainer
90	0	6	authorityName
91	0	6	zone
92	0	6	inZone
93	0	6	member
94	0	1	cascadeCRC
95	0	1	cascadeUpdate
96	0	1	cascadeTx
97	0	11	transferGroup
98	0	9	executionFailureMessage
99	0	9	executionEndDate
100	0	9	executionStartDate
101	0	9	executionActionStatus
102	0	9	actionFolder
103	0	12	smartFolderTemplate
104	0	1	versionEdition
105	0	1	licenseMode
106	0	10	site
107	0	10	siteVisibility
108	0	10	sitePreset
109	0	1	unmovable
110	0	1	undeletable
111	0	6	tagscope
112	0	6	authorityDisplayName
113	0	2	salt
114	0	6	jobtitle
115	0	6	location
116	0	6	mobile
117	0	6	skype
118	0	6	companyaddress1
119	0	6	telephone
120	0	6	sizeQuota
121	0	6	instantmsg
122	0	6	companypostcode
123	0	6	googleusername
124	0	6	companyaddress3
125	0	6	companytelephone
126	0	6	companyemail
127	0	6	companyaddress2
128	0	6	organization
129	0	6	companyfax
130	0	6	userStatus
131	0	6	emailFeedId
132	0	6	userStatusTime
133	0	6	personDisabled
134	0	6	preferences
135	0	6	preferenceValues
136	0	6	persondescription
137	0	6	preferenceImage
138	0	6	lastThumbnailModification
139	0	6	thumbnailModification
140	0	14	renditioned
141	0	6	thumbnail
142	0	14	rendition
143	0	6	contentPropertyName
144	0	6	thumbnailName
145	0	14	hiddenRendition
146	0	6	avatar
147	0	1	temporary
148	0	1	deleted
149	0	1	originalId
150	0	6	tagScopeSummary
151	0	6	tagScopeCache
152	0	10	componentId
153	0	10	siteContainer
154	0	6	taggable
155	0	6	autoVersionOnUpdateProps
156	0	6	likesRatingSchemeTotal
157	0	6	versionLabel
158	0	6	autoVersion
159	0	6	initialVersion
160	0	6	likesRatingSchemeCount
161	0	6	versionable
162	0	6	likesRatingSchemeRollups
163	0	6	rateable
164	0	5	versionHistory
165	0	5	versionedNodeId
166	0	5	version
167	0	5	frozenNodeRef
168	0	5	versionDescription
169	0	5	versionLabel
170	0	5	frozenNodeDbId
171	0	6	versionType
172	0	5	frozenCreator
173	0	5	frozenCreated
174	0	5	frozenModifier
175	0	5	frozenModified
176	0	5	frozenAccessed
177	0	5	rootVersion
178	0	15	software
179	0	15	focalLength
180	0	15	yResolution
181	0	15	resolutionUnit
182	0	15	dateTimeOriginal
183	0	15	orientation
184	0	15	pixelYDimension
185	0	15	model
186	0	15	xResolution
187	0	15	flash
188	0	15	fNumber
189	0	15	isoSpeedRatings
190	0	15	manufacturer
191	0	15	pixelXDimension
192	0	15	exposureTime
193	0	15	exif
194	0	16	discussable
195	0	16	forum
196	0	16	discussion
197	0	16	topic
198	0	16	post
199	0	17	link
200	0	17	description
201	0	17	title
202	0	17	url
203	0	18	dataList
204	0	18	dataListItemType
205	0	18	issue
206	0	18	issueStatus
207	0	18	issuePriority
208	0	18	issueComments
209	0	18	issueDueDate
210	0	18	issueID
211	0	6	attachable
212	0	18	todoList
213	0	18	todoStatus
214	0	18	todoPriority
215	0	18	todoNotes
216	0	18	todoTitle
217	0	18	todoDueDate
218	0	6	published
219	0	6	syndication
220	0	6	referencing
221	0	6	updated
222	0	1	clientControlled
223	0	6	references
224	0	18	attachments
225	0	6	attachments
226	0	1	incomplete
227	0	19	installedVersion
228	0	19	currentVersion
229	0	1	keyStore
230	0	20	facets
231	0	20	facetOrder
232	0	2	passwordHash
233	0	2	hashIndicator
234	0	14	rendition2
235	0	14	contentHashCode
236	0	5	metadata-versionType
237	0	6	generalclassifiable
\.


--
-- Data for Name: alf_store; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_store (id, version, protocol, identifier, root_node_id) FROM stdin;
1	1	user	alfrescoUserStore	1
2	1	system	system	5
3	1	workspace	lightWeightVersionStore	9
4	1	workspace	version2Store	10
5	1	archive	SpacesStore	11
6	1	workspace	SpacesStore	12
\.


--
-- Data for Name: alf_subscriptions; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_subscriptions (user_node_id, node_id) FROM stdin;
\.


--
-- Data for Name: alf_tenant; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_tenant (tenant_domain, version, enabled, tenant_name, content_root, db_url) FROM stdin;
\.


--
-- Data for Name: alf_transaction; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_transaction (id, version, change_txn_id, commit_time_ms) FROM stdin;
1	1	d722f9a4-c9b1-4872-803c-9d7dfaa4ada9	1708329071387
2	1	277a38e9-196e-4af3-9650-facab3a86f9e	1708329071434
3	1	c1dd33d5-a451-41e0-a259-84faaaf4e7ed	1708329071452
4	1	d620e68d-95e7-435a-904a-79dc04997d2b	1708329071465
5	1	dec5e023-b8b5-4e88-9f82-074b4f3f1876	1708329071471
6	1	1b1a6b52-e8dc-4914-ac25-b0f8f008b71f	1708329073823
7	1	26574f3d-cd2e-49bd-985c-b460d3e31bb4	1708329073918
8	1	2c771708-2977-4de6-92a6-6151bf39c43a	1708329073941
9	1	8213d2f5-d803-40d6-8a2d-f60ce1cf209e	1708329087275
10	1	b8d7caef-2c8e-4438-b3b2-34859971db85	1708329087280
11	1	c98a7662-92bd-4901-b12e-13c4889be5b9	1708329088554
12	1	352aeff3-f6cd-4450-a530-ee44edfc8c5a	1708329088606
13	1	160f6ea5-f144-47ae-8a3e-5c3b42c2c93a	1708329088651
14	1	a6d9bdf0-830d-4730-9824-9fc5dc001090	1708329088962
15	1	82d947d4-a350-46d7-9d26-49d340b9303c	1708329089238
16	1	2ab23be4-d60e-4a9e-bf9b-45430f60ed6b	1708329089242
17	1	29d92e16-bd1c-475b-b048-3beb38e21c19	1708329089470
18	1	b55915ac-a2d0-4989-826b-399ebc40da19	1708329089475
19	1	75d48a06-285a-40e0-96a0-4b64b1f99ab4	1708329205593
20	1	cf9b9a14-3b6b-41a5-872b-9b7213ce31a2	1708329205996
21	1	81f357fe-9e3c-447c-803d-b753e791700e	1708329206088
22	1	ff694d53-2bf6-4e8b-a226-f6ef33b75a8b	1708329323211
23	1	7b03ac72-f386-4962-bee1-71f6c95b8cfe	1708329323216
24	1	30bfb2e6-4275-4116-b9cd-b8e9e5780e6a	1708329410051
25	1	fa10c1a4-3eec-494d-95a4-41eae3eefcec	1708329430910
26	1	08c1556b-8f94-4ad1-b0ba-b8d8be39cca2	1708329464084
27	1	41d4027f-902e-4ac3-b489-2393d296a6c2	1708329628094
28	1	2196eb5b-255e-46ad-8c03-2a1c3d52b0b8	1708329628100
29	1	d158d737-6368-4f1e-9f6e-92fa3ec41d59	1708329629312
30	1	c4abbf4f-8f00-414f-9ad3-41898d84b68a	1708329629324
31	1	e22e6581-051d-40c9-bbd7-ae71a9871b33	1708329633964
32	1	196245f0-dc57-4fdd-bc48-0dc2cc0d5a85	1708329633978
33	1	f6d92a14-96c4-4241-b346-7a18d6905895	1708330172234
34	1	f7c54617-0e95-485a-b90f-0328c440e91e	1708330172238
35	1	6b1fb5bd-9550-439d-a5a5-5e3a432049ca	1708330172494
36	1	fd8012cd-0573-4e64-9ffb-70275ad9d34b	1708330172508
37	1	039797d2-974d-4489-985c-f7a23270e9a1	1708330172526
38	1	213b0c0b-b368-4d83-8a6d-b721d2c56553	1708330180885
39	1	68b51553-e207-4a49-b124-ff104016f931	1708330180890
40	1	ed693728-1a93-47df-a1b3-e185e9675cfc	1708330180991
41	1	d8c9992e-a431-4b1f-84dc-5bcebc142293	1708330181112
42	1	5867943e-caab-4a55-b663-df78827ed972	1708330181173
43	1	d38b773c-71e6-4071-8f67-333ab747b146	1708330181360
44	1	27c17b7e-0689-49a8-995c-7f5ee46abaeb	1708330181369
45	1	878408df-0e50-4fdc-b6fd-748336a9ee32	1708330181397
46	1	a60b6b75-a9c1-436a-b026-05f5256b303e	1708330181406
47	1	e3ebc9a1-5d1e-4eed-933b-e03fd1038ff7	1708335061889
48	1	c41f6a74-124f-4c42-b9be-c5e1be322188	1708335070073
49	1	3be15d06-d77d-4efc-bd5f-888ec72511fe	1708335078277
50	1	c128984a-4783-418c-bf8d-97b84a2aaed6	1708335083557
51	1	af775313-7bec-4ee4-a255-9b11932e848a	1708335106182
52	1	18f6f080-0874-4d64-9101-07539b88dcc6	1708335114766
53	1	14a602a3-241d-46f4-b5e8-2bad4c1a5f91	1708335125330
54	1	e27b4710-e4c2-4a8c-a2eb-2933d2936420	1708335135963
\.


--
-- Data for Name: alf_usage_delta; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_usage_delta (id, version, node_id, delta_size) FROM stdin;
\.


--
-- Name: act_evt_log_log_nr__seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.act_evt_log_log_nr__seq', 1, false);


--
-- Name: alf_access_control_entry_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_access_control_entry_seq', 20, true);


--
-- Name: alf_access_control_list_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_access_control_list_seq', 60, true);


--
-- Name: alf_ace_context_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_ace_context_seq', 1, false);


--
-- Name: alf_acl_change_set_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_acl_change_set_seq', 8, true);


--
-- Name: alf_acl_member_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_acl_member_seq', 151, true);


--
-- Name: alf_activity_feed_control_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_activity_feed_control_seq', 1, false);


--
-- Name: alf_activity_feed_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_activity_feed_seq', 1, false);


--
-- Name: alf_activity_post_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_activity_post_seq', 1, false);


--
-- Name: alf_audit_app_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_audit_app_seq', 1, true);


--
-- Name: alf_audit_entry_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_audit_entry_seq', 1, false);


--
-- Name: alf_audit_model_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_audit_model_seq', 3, true);


--
-- Name: alf_auth_status_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_auth_status_seq', 1, true);


--
-- Name: alf_authority_alias_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_authority_alias_seq', 1, false);


--
-- Name: alf_authority_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_authority_seq', 11, true);


--
-- Name: alf_child_assoc_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_child_assoc_seq', 927, true);


--
-- Name: alf_content_data_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_content_data_seq', 275, true);


--
-- Name: alf_content_url_enc_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_content_url_enc_seq', 1, false);


--
-- Name: alf_content_url_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_content_url_seq', 272, true);


--
-- Name: alf_encoding_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_encoding_seq', 3, true);


--
-- Name: alf_locale_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_locale_seq', 4, true);


--
-- Name: alf_lock_resource_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_lock_resource_seq', 27, true);


--
-- Name: alf_lock_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_lock_seq', 35, true);


--
-- Name: alf_mimetype_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_mimetype_seq', 14, true);


--
-- Name: alf_namespace_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_namespace_seq', 20, true);


--
-- Name: alf_node_assoc_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_node_assoc_seq', 18, true);


--
-- Name: alf_node_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_node_seq', 896, true);


--
-- Name: alf_permission_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_permission_seq', 12, true);


--
-- Name: alf_prop_class_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_prop_class_seq', 8, true);


--
-- Name: alf_prop_double_value_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_prop_double_value_seq', 1, false);


--
-- Name: alf_prop_root_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_prop_root_seq', 40, true);


--
-- Name: alf_prop_serializable_value_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_prop_serializable_value_seq', 2, true);


--
-- Name: alf_prop_string_value_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_prop_string_value_seq', 38, true);


--
-- Name: alf_prop_unique_ctx_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_prop_unique_ctx_seq', 25, true);


--
-- Name: alf_prop_value_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_prop_value_seq', 57, true);


--
-- Name: alf_qname_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_qname_seq', 237, true);


--
-- Name: alf_store_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_store_seq', 6, true);


--
-- Name: alf_transaction_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_transaction_seq', 54, true);


--
-- Name: alf_usage_delta_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_usage_delta_seq', 1, false);


--
-- Name: act_evt_log act_evt_log_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_evt_log
    ADD CONSTRAINT act_evt_log_pkey PRIMARY KEY (log_nr_);


--
-- Name: act_ge_bytearray act_ge_bytearray_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ge_bytearray
    ADD CONSTRAINT act_ge_bytearray_pkey PRIMARY KEY (id_);


--
-- Name: act_ge_property act_ge_property_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ge_property
    ADD CONSTRAINT act_ge_property_pkey PRIMARY KEY (name_);


--
-- Name: act_hi_actinst act_hi_actinst_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_hi_actinst
    ADD CONSTRAINT act_hi_actinst_pkey PRIMARY KEY (id_);


--
-- Name: act_hi_attachment act_hi_attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_hi_attachment
    ADD CONSTRAINT act_hi_attachment_pkey PRIMARY KEY (id_);


--
-- Name: act_hi_comment act_hi_comment_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_hi_comment
    ADD CONSTRAINT act_hi_comment_pkey PRIMARY KEY (id_);


--
-- Name: act_hi_detail act_hi_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_hi_detail
    ADD CONSTRAINT act_hi_detail_pkey PRIMARY KEY (id_);


--
-- Name: act_hi_identitylink act_hi_identitylink_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_hi_identitylink
    ADD CONSTRAINT act_hi_identitylink_pkey PRIMARY KEY (id_);


--
-- Name: act_hi_procinst act_hi_procinst_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_hi_procinst
    ADD CONSTRAINT act_hi_procinst_pkey PRIMARY KEY (id_);


--
-- Name: act_hi_procinst act_hi_procinst_proc_inst_id__key; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_hi_procinst
    ADD CONSTRAINT act_hi_procinst_proc_inst_id__key UNIQUE (proc_inst_id_);


--
-- Name: act_hi_taskinst act_hi_taskinst_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_hi_taskinst
    ADD CONSTRAINT act_hi_taskinst_pkey PRIMARY KEY (id_);


--
-- Name: act_hi_varinst act_hi_varinst_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_hi_varinst
    ADD CONSTRAINT act_hi_varinst_pkey PRIMARY KEY (id_);


--
-- Name: act_id_group act_id_group_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_id_group
    ADD CONSTRAINT act_id_group_pkey PRIMARY KEY (id_);


--
-- Name: act_id_info act_id_info_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_id_info
    ADD CONSTRAINT act_id_info_pkey PRIMARY KEY (id_);


--
-- Name: act_id_membership act_id_membership_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_id_membership
    ADD CONSTRAINT act_id_membership_pkey PRIMARY KEY (user_id_, group_id_);


--
-- Name: act_id_user act_id_user_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_id_user
    ADD CONSTRAINT act_id_user_pkey PRIMARY KEY (id_);


--
-- Name: act_procdef_info act_procdef_info_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_procdef_info
    ADD CONSTRAINT act_procdef_info_pkey PRIMARY KEY (id_);


--
-- Name: act_re_deployment act_re_deployment_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_re_deployment
    ADD CONSTRAINT act_re_deployment_pkey PRIMARY KEY (id_);


--
-- Name: act_re_model act_re_model_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_re_model
    ADD CONSTRAINT act_re_model_pkey PRIMARY KEY (id_);


--
-- Name: act_re_procdef act_re_procdef_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_re_procdef
    ADD CONSTRAINT act_re_procdef_pkey PRIMARY KEY (id_);


--
-- Name: act_ru_event_subscr act_ru_event_subscr_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_event_subscr
    ADD CONSTRAINT act_ru_event_subscr_pkey PRIMARY KEY (id_);


--
-- Name: act_ru_execution act_ru_execution_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_execution
    ADD CONSTRAINT act_ru_execution_pkey PRIMARY KEY (id_);


--
-- Name: act_ru_identitylink act_ru_identitylink_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_identitylink
    ADD CONSTRAINT act_ru_identitylink_pkey PRIMARY KEY (id_);


--
-- Name: act_ru_job act_ru_job_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_job
    ADD CONSTRAINT act_ru_job_pkey PRIMARY KEY (id_);


--
-- Name: act_ru_task act_ru_task_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_task
    ADD CONSTRAINT act_ru_task_pkey PRIMARY KEY (id_);


--
-- Name: act_ru_variable act_ru_variable_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_variable
    ADD CONSTRAINT act_ru_variable_pkey PRIMARY KEY (id_);


--
-- Name: act_procdef_info act_uniq_info_procdef; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_procdef_info
    ADD CONSTRAINT act_uniq_info_procdef UNIQUE (proc_def_id_);


--
-- Name: act_re_procdef act_uniq_procdef; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_re_procdef
    ADD CONSTRAINT act_uniq_procdef UNIQUE (key_, version_, tenant_id_);


--
-- Name: alf_access_control_entry alf_access_control_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_access_control_entry
    ADD CONSTRAINT alf_access_control_entry_pkey PRIMARY KEY (id);


--
-- Name: alf_access_control_list alf_access_control_list_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_access_control_list
    ADD CONSTRAINT alf_access_control_list_pkey PRIMARY KEY (id);


--
-- Name: alf_ace_context alf_ace_context_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_ace_context
    ADD CONSTRAINT alf_ace_context_pkey PRIMARY KEY (id);


--
-- Name: alf_acl_change_set alf_acl_change_set_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_acl_change_set
    ADD CONSTRAINT alf_acl_change_set_pkey PRIMARY KEY (id);


--
-- Name: alf_acl_member alf_acl_member_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_acl_member
    ADD CONSTRAINT alf_acl_member_pkey PRIMARY KEY (id);


--
-- Name: alf_activity_feed_control alf_activity_feed_control_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_activity_feed_control
    ADD CONSTRAINT alf_activity_feed_control_pkey PRIMARY KEY (id);


--
-- Name: alf_activity_feed alf_activity_feed_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_activity_feed
    ADD CONSTRAINT alf_activity_feed_pkey PRIMARY KEY (id);


--
-- Name: alf_activity_post alf_activity_post_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_activity_post
    ADD CONSTRAINT alf_activity_post_pkey PRIMARY KEY (sequence_id);


--
-- Name: alf_applied_patch alf_applied_patch_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_applied_patch
    ADD CONSTRAINT alf_applied_patch_pkey PRIMARY KEY (id);


--
-- Name: alf_audit_app alf_audit_app_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_audit_app
    ADD CONSTRAINT alf_audit_app_pkey PRIMARY KEY (id);


--
-- Name: alf_audit_entry alf_audit_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_audit_entry
    ADD CONSTRAINT alf_audit_entry_pkey PRIMARY KEY (id);


--
-- Name: alf_audit_model alf_audit_model_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_audit_model
    ADD CONSTRAINT alf_audit_model_pkey PRIMARY KEY (id);


--
-- Name: alf_auth_status alf_auth_status_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_auth_status
    ADD CONSTRAINT alf_auth_status_pkey PRIMARY KEY (id);


--
-- Name: alf_authority_alias alf_authority_alias_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_authority_alias
    ADD CONSTRAINT alf_authority_alias_pkey PRIMARY KEY (id);


--
-- Name: alf_authority alf_authority_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_authority
    ADD CONSTRAINT alf_authority_pkey PRIMARY KEY (id);


--
-- Name: alf_child_assoc alf_child_assoc_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_child_assoc
    ADD CONSTRAINT alf_child_assoc_pkey PRIMARY KEY (id);


--
-- Name: alf_content_data alf_content_data_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_content_data
    ADD CONSTRAINT alf_content_data_pkey PRIMARY KEY (id);


--
-- Name: alf_content_url_encryption alf_content_url_encryption_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_content_url_encryption
    ADD CONSTRAINT alf_content_url_encryption_pkey PRIMARY KEY (id);


--
-- Name: alf_content_url alf_content_url_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_content_url
    ADD CONSTRAINT alf_content_url_pkey PRIMARY KEY (id);


--
-- Name: alf_encoding alf_encoding_encoding_str_key; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_encoding
    ADD CONSTRAINT alf_encoding_encoding_str_key UNIQUE (encoding_str);


--
-- Name: alf_encoding alf_encoding_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_encoding
    ADD CONSTRAINT alf_encoding_pkey PRIMARY KEY (id);


--
-- Name: alf_locale alf_locale_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_locale
    ADD CONSTRAINT alf_locale_pkey PRIMARY KEY (id);


--
-- Name: alf_lock alf_lock_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_lock
    ADD CONSTRAINT alf_lock_pkey PRIMARY KEY (id);


--
-- Name: alf_lock_resource alf_lock_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_lock_resource
    ADD CONSTRAINT alf_lock_resource_pkey PRIMARY KEY (id);


--
-- Name: alf_mimetype alf_mimetype_mimetype_str_key; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_mimetype
    ADD CONSTRAINT alf_mimetype_mimetype_str_key UNIQUE (mimetype_str);


--
-- Name: alf_mimetype alf_mimetype_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_mimetype
    ADD CONSTRAINT alf_mimetype_pkey PRIMARY KEY (id);


--
-- Name: alf_namespace alf_namespace_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_namespace
    ADD CONSTRAINT alf_namespace_pkey PRIMARY KEY (id);


--
-- Name: alf_node_aspects alf_node_aspects_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node_aspects
    ADD CONSTRAINT alf_node_aspects_pkey PRIMARY KEY (node_id, qname_id);


--
-- Name: alf_node_assoc alf_node_assoc_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node_assoc
    ADD CONSTRAINT alf_node_assoc_pkey PRIMARY KEY (id);


--
-- Name: alf_node alf_node_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node
    ADD CONSTRAINT alf_node_pkey PRIMARY KEY (id);


--
-- Name: alf_node_properties alf_node_properties_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node_properties
    ADD CONSTRAINT alf_node_properties_pkey PRIMARY KEY (node_id, qname_id, list_index, locale_id);


--
-- Name: alf_permission alf_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_permission
    ADD CONSTRAINT alf_permission_pkey PRIMARY KEY (id);


--
-- Name: alf_prop_class alf_prop_class_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_class
    ADD CONSTRAINT alf_prop_class_pkey PRIMARY KEY (id);


--
-- Name: alf_prop_date_value alf_prop_date_value_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_date_value
    ADD CONSTRAINT alf_prop_date_value_pkey PRIMARY KEY (date_value);


--
-- Name: alf_prop_double_value alf_prop_double_value_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_double_value
    ADD CONSTRAINT alf_prop_double_value_pkey PRIMARY KEY (id);


--
-- Name: alf_prop_link alf_prop_link_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_link
    ADD CONSTRAINT alf_prop_link_pkey PRIMARY KEY (root_prop_id, contained_in, prop_index);


--
-- Name: alf_prop_root alf_prop_root_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_root
    ADD CONSTRAINT alf_prop_root_pkey PRIMARY KEY (id);


--
-- Name: alf_prop_serializable_value alf_prop_serializable_value_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_serializable_value
    ADD CONSTRAINT alf_prop_serializable_value_pkey PRIMARY KEY (id);


--
-- Name: alf_prop_string_value alf_prop_string_value_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_string_value
    ADD CONSTRAINT alf_prop_string_value_pkey PRIMARY KEY (id);


--
-- Name: alf_prop_unique_ctx alf_prop_unique_ctx_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_unique_ctx
    ADD CONSTRAINT alf_prop_unique_ctx_pkey PRIMARY KEY (id);


--
-- Name: alf_prop_value alf_prop_value_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_value
    ADD CONSTRAINT alf_prop_value_pkey PRIMARY KEY (id);


--
-- Name: alf_qname alf_qname_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_qname
    ADD CONSTRAINT alf_qname_pkey PRIMARY KEY (id);


--
-- Name: alf_store alf_store_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_store
    ADD CONSTRAINT alf_store_pkey PRIMARY KEY (id);


--
-- Name: alf_subscriptions alf_subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_subscriptions
    ADD CONSTRAINT alf_subscriptions_pkey PRIMARY KEY (user_node_id, node_id);


--
-- Name: alf_tenant alf_tenant_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_tenant
    ADD CONSTRAINT alf_tenant_pkey PRIMARY KEY (tenant_domain);


--
-- Name: alf_transaction alf_transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_transaction
    ADD CONSTRAINT alf_transaction_pkey PRIMARY KEY (id);


--
-- Name: alf_usage_delta alf_usage_delta_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_usage_delta
    ADD CONSTRAINT alf_usage_delta_pkey PRIMARY KEY (id);


--
-- Name: alf_audit_app idx_alf_aud_app_an; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_audit_app
    ADD CONSTRAINT idx_alf_aud_app_an UNIQUE (app_name_id);


--
-- Name: acl_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX acl_id ON public.alf_access_control_list USING btree (acl_id, latest, acl_version);


--
-- Name: aclm_acl_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX aclm_acl_id ON public.alf_acl_member USING btree (acl_id, ace_id, pos);


--
-- Name: act_idx_athrz_procedef; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_athrz_procedef ON public.act_ru_identitylink USING btree (proc_def_id_);


--
-- Name: act_idx_bytear_depl; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_bytear_depl ON public.act_ge_bytearray USING btree (deployment_id_);


--
-- Name: act_idx_event_subscr; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_event_subscr ON public.act_ru_event_subscr USING btree (execution_id_);


--
-- Name: act_idx_event_subscr_config_; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_event_subscr_config_ ON public.act_ru_event_subscr USING btree (configuration_);


--
-- Name: act_idx_exe_parent; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_exe_parent ON public.act_ru_execution USING btree (parent_id_);


--
-- Name: act_idx_exe_procdef; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_exe_procdef ON public.act_ru_execution USING btree (proc_def_id_);


--
-- Name: act_idx_exe_procinst; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_exe_procinst ON public.act_ru_execution USING btree (proc_inst_id_);


--
-- Name: act_idx_exe_super; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_exe_super ON public.act_ru_execution USING btree (super_exec_);


--
-- Name: act_idx_exec_buskey; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_exec_buskey ON public.act_ru_execution USING btree (business_key_);


--
-- Name: act_idx_hi_act_inst_end; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_act_inst_end ON public.act_hi_actinst USING btree (end_time_);


--
-- Name: act_idx_hi_act_inst_exec; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_act_inst_exec ON public.act_hi_actinst USING btree (execution_id_, act_id_);


--
-- Name: act_idx_hi_act_inst_procinst; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_act_inst_procinst ON public.act_hi_actinst USING btree (proc_inst_id_, act_id_);


--
-- Name: act_idx_hi_act_inst_start; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_act_inst_start ON public.act_hi_actinst USING btree (start_time_);


--
-- Name: act_idx_hi_detail_act_inst; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_detail_act_inst ON public.act_hi_detail USING btree (act_inst_id_);


--
-- Name: act_idx_hi_detail_name; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_detail_name ON public.act_hi_detail USING btree (name_);


--
-- Name: act_idx_hi_detail_proc_inst; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_detail_proc_inst ON public.act_hi_detail USING btree (proc_inst_id_);


--
-- Name: act_idx_hi_detail_task_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_detail_task_id ON public.act_hi_detail USING btree (task_id_);


--
-- Name: act_idx_hi_detail_time; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_detail_time ON public.act_hi_detail USING btree (time_);


--
-- Name: act_idx_hi_ident_lnk_procinst; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_ident_lnk_procinst ON public.act_hi_identitylink USING btree (proc_inst_id_);


--
-- Name: act_idx_hi_ident_lnk_task; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_ident_lnk_task ON public.act_hi_identitylink USING btree (task_id_);


--
-- Name: act_idx_hi_ident_lnk_user; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_ident_lnk_user ON public.act_hi_identitylink USING btree (user_id_);


--
-- Name: act_idx_hi_pro_i_buskey; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_pro_i_buskey ON public.act_hi_procinst USING btree (business_key_);


--
-- Name: act_idx_hi_pro_inst_end; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_pro_inst_end ON public.act_hi_procinst USING btree (end_time_);


--
-- Name: act_idx_hi_procvar_name_type; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_procvar_name_type ON public.act_hi_varinst USING btree (name_, var_type_);


--
-- Name: act_idx_hi_procvar_proc_inst; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_procvar_proc_inst ON public.act_hi_varinst USING btree (proc_inst_id_);


--
-- Name: act_idx_hi_procvar_task_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_procvar_task_id ON public.act_hi_varinst USING btree (task_id_);


--
-- Name: act_idx_hi_task_inst_procinst; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_hi_task_inst_procinst ON public.act_hi_taskinst USING btree (proc_inst_id_);


--
-- Name: act_idx_ident_lnk_group; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_ident_lnk_group ON public.act_ru_identitylink USING btree (group_id_);


--
-- Name: act_idx_ident_lnk_user; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_ident_lnk_user ON public.act_ru_identitylink USING btree (user_id_);


--
-- Name: act_idx_idl_procinst; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_idl_procinst ON public.act_ru_identitylink USING btree (proc_inst_id_);


--
-- Name: act_idx_job_exception; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_job_exception ON public.act_ru_job USING btree (exception_stack_id_);


--
-- Name: act_idx_memb_group; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_memb_group ON public.act_id_membership USING btree (group_id_);


--
-- Name: act_idx_memb_user; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_memb_user ON public.act_id_membership USING btree (user_id_);


--
-- Name: act_idx_model_deployment; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_model_deployment ON public.act_re_model USING btree (deployment_id_);


--
-- Name: act_idx_model_source; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_model_source ON public.act_re_model USING btree (editor_source_value_id_);


--
-- Name: act_idx_model_source_extra; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_model_source_extra ON public.act_re_model USING btree (editor_source_extra_value_id_);


--
-- Name: act_idx_procdef_info_json; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_procdef_info_json ON public.act_procdef_info USING btree (info_json_id_);


--
-- Name: act_idx_procdef_info_proc; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_procdef_info_proc ON public.act_procdef_info USING btree (proc_def_id_);


--
-- Name: act_idx_task_create; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_task_create ON public.act_ru_task USING btree (create_time_);


--
-- Name: act_idx_task_exec; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_task_exec ON public.act_ru_task USING btree (execution_id_);


--
-- Name: act_idx_task_procdef; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_task_procdef ON public.act_ru_task USING btree (proc_def_id_);


--
-- Name: act_idx_task_procinst; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_task_procinst ON public.act_ru_task USING btree (proc_inst_id_);


--
-- Name: act_idx_tskass_task; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_tskass_task ON public.act_ru_identitylink USING btree (task_id_);


--
-- Name: act_idx_var_bytearray; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_var_bytearray ON public.act_ru_variable USING btree (bytearray_id_);


--
-- Name: act_idx_var_exe; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_var_exe ON public.act_ru_variable USING btree (execution_id_);


--
-- Name: act_idx_var_procinst; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_var_procinst ON public.act_ru_variable USING btree (proc_inst_id_);


--
-- Name: act_idx_variable_task_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX act_idx_variable_task_id ON public.act_ru_variable USING btree (task_id_);


--
-- Name: auth_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX auth_id ON public.alf_authority_alias USING btree (auth_id, alias_id);


--
-- Name: authority; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX authority ON public.alf_authority USING btree (authority, crc);


--
-- Name: feed_feeduserid_idx; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX feed_feeduserid_idx ON public.alf_activity_feed USING btree (feed_user_id);


--
-- Name: feed_postdate_idx; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX feed_postdate_idx ON public.alf_activity_feed USING btree (post_date);


--
-- Name: feed_postuserid_idx; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX feed_postuserid_idx ON public.alf_activity_feed USING btree (post_user_id);


--
-- Name: feed_sitenetwork_idx; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX feed_sitenetwork_idx ON public.alf_activity_feed USING btree (site_network);


--
-- Name: feedctrl_feeduserid_idx; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX feedctrl_feeduserid_idx ON public.alf_activity_feed_control USING btree (feed_user_id);


--
-- Name: fk_alf_ace_auth; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_ace_auth ON public.alf_access_control_entry USING btree (authority_id);


--
-- Name: fk_alf_ace_ctx; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_ace_ctx ON public.alf_access_control_entry USING btree (context_id);


--
-- Name: fk_alf_ace_perm; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_ace_perm ON public.alf_access_control_entry USING btree (permission_id);


--
-- Name: fk_alf_acl_acs; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_acl_acs ON public.alf_access_control_list USING btree (acl_change_set);


--
-- Name: fk_alf_aclm_ace; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_aclm_ace ON public.alf_acl_member USING btree (ace_id);


--
-- Name: fk_alf_aclm_acl; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_aclm_acl ON public.alf_acl_member USING btree (acl_id);


--
-- Name: fk_alf_aud_app_dis; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_aud_app_dis ON public.alf_audit_app USING btree (disabled_paths_id);


--
-- Name: fk_alf_aud_app_mod; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_aud_app_mod ON public.alf_audit_app USING btree (audit_model_id);


--
-- Name: fk_alf_aud_ent_app; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_aud_ent_app ON public.alf_audit_entry USING btree (audit_app_id);


--
-- Name: fk_alf_aud_ent_pro; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_aud_ent_pro ON public.alf_audit_entry USING btree (audit_values_id);


--
-- Name: fk_alf_aud_ent_use; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_aud_ent_use ON public.alf_audit_entry USING btree (audit_user_id);


--
-- Name: fk_alf_aud_mod_cd; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_aud_mod_cd ON public.alf_audit_model USING btree (content_data_id);


--
-- Name: fk_alf_autha_ali; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_autha_ali ON public.alf_authority_alias USING btree (alias_id);


--
-- Name: fk_alf_autha_aut; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_autha_aut ON public.alf_authority_alias USING btree (auth_id);


--
-- Name: fk_alf_cass_cnode; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_cass_cnode ON public.alf_child_assoc USING btree (child_node_id);


--
-- Name: fk_alf_cass_qnns; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_cass_qnns ON public.alf_child_assoc USING btree (qname_ns_id);


--
-- Name: fk_alf_cass_tqn; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_cass_tqn ON public.alf_child_assoc USING btree (type_qname_id);


--
-- Name: fk_alf_cont_enc; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_cont_enc ON public.alf_content_data USING btree (content_encoding_id);


--
-- Name: fk_alf_cont_loc; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_cont_loc ON public.alf_content_data USING btree (content_locale_id);


--
-- Name: fk_alf_cont_mim; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_cont_mim ON public.alf_content_data USING btree (content_mimetype_id);


--
-- Name: fk_alf_cont_url; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_cont_url ON public.alf_content_data USING btree (content_url_id);


--
-- Name: fk_alf_lock_excl; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_lock_excl ON public.alf_lock USING btree (excl_resource_id);


--
-- Name: fk_alf_nasp_n; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_nasp_n ON public.alf_node_aspects USING btree (node_id);


--
-- Name: fk_alf_nasp_qn; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_nasp_qn ON public.alf_node_aspects USING btree (qname_id);


--
-- Name: fk_alf_nass_snode; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_nass_snode ON public.alf_node_assoc USING btree (source_node_id, type_qname_id, assoc_index);


--
-- Name: fk_alf_nass_tnode; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_nass_tnode ON public.alf_node_assoc USING btree (target_node_id, type_qname_id);


--
-- Name: fk_alf_nass_tqn; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_nass_tqn ON public.alf_node_assoc USING btree (type_qname_id);


--
-- Name: fk_alf_node_acl; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_node_acl ON public.alf_node USING btree (acl_id);


--
-- Name: fk_alf_node_loc; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_node_loc ON public.alf_node USING btree (locale_id);


--
-- Name: fk_alf_node_store; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_node_store ON public.alf_node USING btree (store_id);


--
-- Name: fk_alf_nprop_loc; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_nprop_loc ON public.alf_node_properties USING btree (locale_id);


--
-- Name: fk_alf_nprop_n; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_nprop_n ON public.alf_node_properties USING btree (node_id);


--
-- Name: fk_alf_nprop_qn; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_nprop_qn ON public.alf_node_properties USING btree (qname_id);


--
-- Name: fk_alf_perm_tqn; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_perm_tqn ON public.alf_permission USING btree (type_qname_id);


--
-- Name: fk_alf_propln_key; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_propln_key ON public.alf_prop_link USING btree (key_prop_id);


--
-- Name: fk_alf_propln_val; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_propln_val ON public.alf_prop_link USING btree (value_prop_id);


--
-- Name: fk_alf_propuctx_p1; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_propuctx_p1 ON public.alf_prop_unique_ctx USING btree (prop1_id);


--
-- Name: fk_alf_propuctx_v2; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_propuctx_v2 ON public.alf_prop_unique_ctx USING btree (value2_prop_id);


--
-- Name: fk_alf_propuctx_v3; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_propuctx_v3 ON public.alf_prop_unique_ctx USING btree (value3_prop_id);


--
-- Name: fk_alf_store_root; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_store_root ON public.alf_store USING btree (root_node_id);


--
-- Name: fk_alf_sub_node; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_sub_node ON public.alf_subscriptions USING btree (node_id);


--
-- Name: fk_alf_usaged_n; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_usaged_n ON public.alf_usage_delta USING btree (node_id);


--
-- Name: idx_alf_acl_acs; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_acl_acs ON public.alf_access_control_list USING btree (acl_change_set, id);


--
-- Name: idx_alf_acl_inh; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_acl_inh ON public.alf_access_control_list USING btree (inherits, inherits_from);


--
-- Name: idx_alf_acs_ctms; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_acs_ctms ON public.alf_acl_change_set USING btree (commit_time_ms, id);


--
-- Name: idx_alf_aud_ent_tm; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_aud_ent_tm ON public.alf_audit_entry USING btree (audit_time);


--
-- Name: idx_alf_aud_mod_cr; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX idx_alf_aud_mod_cr ON public.alf_audit_model USING btree (content_crc);


--
-- Name: idx_alf_auth_action; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_auth_action ON public.alf_auth_status USING btree (authaction);


--
-- Name: idx_alf_auth_aut; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_auth_aut ON public.alf_authority USING btree (authority);


--
-- Name: idx_alf_auth_deleted; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_auth_deleted ON public.alf_auth_status USING btree (deleted);


--
-- Name: idx_alf_auth_usr_stat; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX idx_alf_auth_usr_stat ON public.alf_auth_status USING btree (username, authorized);


--
-- Name: idx_alf_cass_pnode; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_cass_pnode ON public.alf_child_assoc USING btree (parent_node_id, assoc_index, id);


--
-- Name: idx_alf_cass_pri; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_cass_pri ON public.alf_child_assoc USING btree (parent_node_id, is_primary, child_node_id);


--
-- Name: idx_alf_cass_qncrc; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_cass_qncrc ON public.alf_child_assoc USING btree (qname_crc, type_qname_id, parent_node_id);


--
-- Name: idx_alf_cont_enc_mka; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_cont_enc_mka ON public.alf_content_url_encryption USING btree (master_key_alias);


--
-- Name: idx_alf_cont_enc_url; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX idx_alf_cont_enc_url ON public.alf_content_url_encryption USING btree (content_url_id);


--
-- Name: idx_alf_conturl_cr; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX idx_alf_conturl_cr ON public.alf_content_url USING btree (content_url_short, content_url_crc);


--
-- Name: idx_alf_conturl_ot; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_conturl_ot ON public.alf_content_url USING btree (orphan_time);


--
-- Name: idx_alf_conturl_sz; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_conturl_sz ON public.alf_content_url USING btree (content_size, id);


--
-- Name: idx_alf_lock_key; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX idx_alf_lock_key ON public.alf_lock USING btree (shared_resource_id, excl_resource_id);


--
-- Name: idx_alf_lockr_key; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX idx_alf_lockr_key ON public.alf_lock_resource USING btree (qname_ns_id, qname_localname);


--
-- Name: idx_alf_node_cor; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_node_cor ON public.alf_node USING btree (audit_creator, store_id, type_qname_id, id);


--
-- Name: idx_alf_node_crd; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_node_crd ON public.alf_node USING btree (audit_created, store_id, type_qname_id, id);


--
-- Name: idx_alf_node_mdq; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_node_mdq ON public.alf_node USING btree (store_id, type_qname_id, id);


--
-- Name: idx_alf_node_mod; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_node_mod ON public.alf_node USING btree (audit_modified, store_id, type_qname_id, id);


--
-- Name: idx_alf_node_mor; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_node_mor ON public.alf_node USING btree (audit_modifier, store_id, type_qname_id, id);


--
-- Name: idx_alf_node_tqn; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_node_tqn ON public.alf_node USING btree (type_qname_id, store_id, id);


--
-- Name: idx_alf_node_txn; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_node_txn ON public.alf_node USING btree (transaction_id);


--
-- Name: idx_alf_node_txn_type; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_node_txn_type ON public.alf_node USING btree (transaction_id, type_qname_id);


--
-- Name: idx_alf_node_ver; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_node_ver ON public.alf_node USING btree (version);


--
-- Name: idx_alf_nprop_b; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_nprop_b ON public.alf_node_properties USING btree (qname_id, boolean_value, node_id);


--
-- Name: idx_alf_nprop_d; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_nprop_d ON public.alf_node_properties USING btree (qname_id, double_value, node_id);


--
-- Name: idx_alf_nprop_f; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_nprop_f ON public.alf_node_properties USING btree (qname_id, float_value, node_id);


--
-- Name: idx_alf_nprop_l; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_nprop_l ON public.alf_node_properties USING btree (qname_id, long_value, node_id);


--
-- Name: idx_alf_nprop_s; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_nprop_s ON public.alf_node_properties USING btree (qname_id, string_value, node_id);


--
-- Name: idx_alf_propc_clas; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_propc_clas ON public.alf_prop_class USING btree (java_class_name);


--
-- Name: idx_alf_propc_crc; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX idx_alf_propc_crc ON public.alf_prop_class USING btree (java_class_name_crc, java_class_name_short);


--
-- Name: idx_alf_propd_val; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX idx_alf_propd_val ON public.alf_prop_double_value USING btree (double_value);


--
-- Name: idx_alf_propdt_dt; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_propdt_dt ON public.alf_prop_date_value USING btree (full_year, month_of_year, day_of_month);


--
-- Name: idx_alf_propln_for; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_propln_for ON public.alf_prop_link USING btree (root_prop_id, key_prop_id, value_prop_id);


--
-- Name: idx_alf_props_crc; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX idx_alf_props_crc ON public.alf_prop_string_value USING btree (string_end_lower, string_crc);


--
-- Name: idx_alf_props_str; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_props_str ON public.alf_prop_string_value USING btree (string_value);


--
-- Name: idx_alf_propuctx; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX idx_alf_propuctx ON public.alf_prop_unique_ctx USING btree (value1_prop_id, value2_prop_id, value3_prop_id);


--
-- Name: idx_alf_propv_act; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX idx_alf_propv_act ON public.alf_prop_value USING btree (actual_type_id, long_value);


--
-- Name: idx_alf_propv_per; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_propv_per ON public.alf_prop_value USING btree (persisted_type, long_value);


--
-- Name: idx_alf_txn_ctms; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_txn_ctms ON public.alf_transaction USING btree (commit_time_ms, id);


--
-- Name: idx_alf_txn_ctms_sc; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_txn_ctms_sc ON public.alf_transaction USING btree (commit_time_ms);


--
-- Name: idx_alf_txn_id_ctms; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX idx_alf_txn_id_ctms ON public.alf_transaction USING btree (id, commit_time_ms);


--
-- Name: locale_str; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX locale_str ON public.alf_locale USING btree (locale_str);


--
-- Name: ns_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX ns_id ON public.alf_qname USING btree (ns_id, local_name);


--
-- Name: parent_node_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX parent_node_id ON public.alf_child_assoc USING btree (parent_node_id, type_qname_id, child_node_name_crc, child_node_name);


--
-- Name: permission_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX permission_id ON public.alf_access_control_entry USING btree (permission_id, authority_id, allowed, applies);


--
-- Name: post_jobtasknode_idx; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX post_jobtasknode_idx ON public.alf_activity_post USING btree (job_task_node);


--
-- Name: post_status_idx; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX post_status_idx ON public.alf_activity_post USING btree (status);


--
-- Name: protocol; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX protocol ON public.alf_store USING btree (protocol, identifier);


--
-- Name: source_node_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX source_node_id ON public.alf_node_assoc USING btree (source_node_id, target_node_id, type_qname_id);


--
-- Name: store_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX store_id ON public.alf_node USING btree (store_id, uuid);


--
-- Name: type_qname_id; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX type_qname_id ON public.alf_permission USING btree (type_qname_id, name);


--
-- Name: uri; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX uri ON public.alf_namespace USING btree (uri);


--
-- Name: act_ru_identitylink act_fk_athrz_procedef; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_identitylink
    ADD CONSTRAINT act_fk_athrz_procedef FOREIGN KEY (proc_def_id_) REFERENCES public.act_re_procdef(id_);


--
-- Name: act_ge_bytearray act_fk_bytearr_depl; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ge_bytearray
    ADD CONSTRAINT act_fk_bytearr_depl FOREIGN KEY (deployment_id_) REFERENCES public.act_re_deployment(id_);


--
-- Name: act_ru_event_subscr act_fk_event_exec; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_event_subscr
    ADD CONSTRAINT act_fk_event_exec FOREIGN KEY (execution_id_) REFERENCES public.act_ru_execution(id_);


--
-- Name: act_ru_execution act_fk_exe_parent; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_execution
    ADD CONSTRAINT act_fk_exe_parent FOREIGN KEY (parent_id_) REFERENCES public.act_ru_execution(id_);


--
-- Name: act_ru_execution act_fk_exe_procdef; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_execution
    ADD CONSTRAINT act_fk_exe_procdef FOREIGN KEY (proc_def_id_) REFERENCES public.act_re_procdef(id_);


--
-- Name: act_ru_execution act_fk_exe_procinst; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_execution
    ADD CONSTRAINT act_fk_exe_procinst FOREIGN KEY (proc_inst_id_) REFERENCES public.act_ru_execution(id_);


--
-- Name: act_ru_execution act_fk_exe_super; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_execution
    ADD CONSTRAINT act_fk_exe_super FOREIGN KEY (super_exec_) REFERENCES public.act_ru_execution(id_);


--
-- Name: act_ru_identitylink act_fk_idl_procinst; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_identitylink
    ADD CONSTRAINT act_fk_idl_procinst FOREIGN KEY (proc_inst_id_) REFERENCES public.act_ru_execution(id_);


--
-- Name: act_procdef_info act_fk_info_json_ba; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_procdef_info
    ADD CONSTRAINT act_fk_info_json_ba FOREIGN KEY (info_json_id_) REFERENCES public.act_ge_bytearray(id_);


--
-- Name: act_procdef_info act_fk_info_procdef; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_procdef_info
    ADD CONSTRAINT act_fk_info_procdef FOREIGN KEY (proc_def_id_) REFERENCES public.act_re_procdef(id_);


--
-- Name: act_ru_job act_fk_job_exception; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_job
    ADD CONSTRAINT act_fk_job_exception FOREIGN KEY (exception_stack_id_) REFERENCES public.act_ge_bytearray(id_);


--
-- Name: act_id_membership act_fk_memb_group; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_id_membership
    ADD CONSTRAINT act_fk_memb_group FOREIGN KEY (group_id_) REFERENCES public.act_id_group(id_);


--
-- Name: act_id_membership act_fk_memb_user; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_id_membership
    ADD CONSTRAINT act_fk_memb_user FOREIGN KEY (user_id_) REFERENCES public.act_id_user(id_);


--
-- Name: act_re_model act_fk_model_deployment; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_re_model
    ADD CONSTRAINT act_fk_model_deployment FOREIGN KEY (deployment_id_) REFERENCES public.act_re_deployment(id_);


--
-- Name: act_re_model act_fk_model_source; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_re_model
    ADD CONSTRAINT act_fk_model_source FOREIGN KEY (editor_source_value_id_) REFERENCES public.act_ge_bytearray(id_);


--
-- Name: act_re_model act_fk_model_source_extra; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_re_model
    ADD CONSTRAINT act_fk_model_source_extra FOREIGN KEY (editor_source_extra_value_id_) REFERENCES public.act_ge_bytearray(id_);


--
-- Name: act_ru_task act_fk_task_exe; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_task
    ADD CONSTRAINT act_fk_task_exe FOREIGN KEY (execution_id_) REFERENCES public.act_ru_execution(id_);


--
-- Name: act_ru_task act_fk_task_procdef; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_task
    ADD CONSTRAINT act_fk_task_procdef FOREIGN KEY (proc_def_id_) REFERENCES public.act_re_procdef(id_);


--
-- Name: act_ru_task act_fk_task_procinst; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_task
    ADD CONSTRAINT act_fk_task_procinst FOREIGN KEY (proc_inst_id_) REFERENCES public.act_ru_execution(id_);


--
-- Name: act_ru_identitylink act_fk_tskass_task; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_identitylink
    ADD CONSTRAINT act_fk_tskass_task FOREIGN KEY (task_id_) REFERENCES public.act_ru_task(id_);


--
-- Name: act_ru_variable act_fk_var_bytearray; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_variable
    ADD CONSTRAINT act_fk_var_bytearray FOREIGN KEY (bytearray_id_) REFERENCES public.act_ge_bytearray(id_);


--
-- Name: act_ru_variable act_fk_var_exe; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_variable
    ADD CONSTRAINT act_fk_var_exe FOREIGN KEY (execution_id_) REFERENCES public.act_ru_execution(id_);


--
-- Name: act_ru_variable act_fk_var_procinst; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.act_ru_variable
    ADD CONSTRAINT act_fk_var_procinst FOREIGN KEY (proc_inst_id_) REFERENCES public.act_ru_execution(id_);


--
-- Name: alf_access_control_entry fk_alf_ace_auth; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_access_control_entry
    ADD CONSTRAINT fk_alf_ace_auth FOREIGN KEY (authority_id) REFERENCES public.alf_authority(id);


--
-- Name: alf_access_control_entry fk_alf_ace_ctx; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_access_control_entry
    ADD CONSTRAINT fk_alf_ace_ctx FOREIGN KEY (context_id) REFERENCES public.alf_ace_context(id);


--
-- Name: alf_access_control_entry fk_alf_ace_perm; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_access_control_entry
    ADD CONSTRAINT fk_alf_ace_perm FOREIGN KEY (permission_id) REFERENCES public.alf_permission(id);


--
-- Name: alf_access_control_list fk_alf_acl_acs; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_access_control_list
    ADD CONSTRAINT fk_alf_acl_acs FOREIGN KEY (acl_change_set) REFERENCES public.alf_acl_change_set(id);


--
-- Name: alf_acl_member fk_alf_aclm_ace; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_acl_member
    ADD CONSTRAINT fk_alf_aclm_ace FOREIGN KEY (ace_id) REFERENCES public.alf_access_control_entry(id);


--
-- Name: alf_acl_member fk_alf_aclm_acl; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_acl_member
    ADD CONSTRAINT fk_alf_aclm_acl FOREIGN KEY (acl_id) REFERENCES public.alf_access_control_list(id);


--
-- Name: alf_audit_app fk_alf_aud_app_an; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_audit_app
    ADD CONSTRAINT fk_alf_aud_app_an FOREIGN KEY (app_name_id) REFERENCES public.alf_prop_value(id);


--
-- Name: alf_audit_app fk_alf_aud_app_dis; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_audit_app
    ADD CONSTRAINT fk_alf_aud_app_dis FOREIGN KEY (disabled_paths_id) REFERENCES public.alf_prop_root(id);


--
-- Name: alf_audit_app fk_alf_aud_app_mod; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_audit_app
    ADD CONSTRAINT fk_alf_aud_app_mod FOREIGN KEY (audit_model_id) REFERENCES public.alf_audit_model(id) ON DELETE CASCADE;


--
-- Name: alf_audit_entry fk_alf_aud_ent_app; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_audit_entry
    ADD CONSTRAINT fk_alf_aud_ent_app FOREIGN KEY (audit_app_id) REFERENCES public.alf_audit_app(id) ON DELETE CASCADE;


--
-- Name: alf_audit_entry fk_alf_aud_ent_pro; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_audit_entry
    ADD CONSTRAINT fk_alf_aud_ent_pro FOREIGN KEY (audit_values_id) REFERENCES public.alf_prop_root(id);


--
-- Name: alf_audit_entry fk_alf_aud_ent_use; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_audit_entry
    ADD CONSTRAINT fk_alf_aud_ent_use FOREIGN KEY (audit_user_id) REFERENCES public.alf_prop_value(id);


--
-- Name: alf_audit_model fk_alf_aud_mod_cd; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_audit_model
    ADD CONSTRAINT fk_alf_aud_mod_cd FOREIGN KEY (content_data_id) REFERENCES public.alf_content_data(id);


--
-- Name: alf_authority_alias fk_alf_autha_ali; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_authority_alias
    ADD CONSTRAINT fk_alf_autha_ali FOREIGN KEY (alias_id) REFERENCES public.alf_authority(id);


--
-- Name: alf_authority_alias fk_alf_autha_aut; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_authority_alias
    ADD CONSTRAINT fk_alf_autha_aut FOREIGN KEY (auth_id) REFERENCES public.alf_authority(id);


--
-- Name: alf_child_assoc fk_alf_cass_cnode; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_child_assoc
    ADD CONSTRAINT fk_alf_cass_cnode FOREIGN KEY (child_node_id) REFERENCES public.alf_node(id);


--
-- Name: alf_child_assoc fk_alf_cass_pnode; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_child_assoc
    ADD CONSTRAINT fk_alf_cass_pnode FOREIGN KEY (parent_node_id) REFERENCES public.alf_node(id);


--
-- Name: alf_child_assoc fk_alf_cass_qnns; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_child_assoc
    ADD CONSTRAINT fk_alf_cass_qnns FOREIGN KEY (qname_ns_id) REFERENCES public.alf_namespace(id);


--
-- Name: alf_child_assoc fk_alf_cass_tqn; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_child_assoc
    ADD CONSTRAINT fk_alf_cass_tqn FOREIGN KEY (type_qname_id) REFERENCES public.alf_qname(id);


--
-- Name: alf_content_data fk_alf_cont_enc; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_content_data
    ADD CONSTRAINT fk_alf_cont_enc FOREIGN KEY (content_encoding_id) REFERENCES public.alf_encoding(id);


--
-- Name: alf_content_url_encryption fk_alf_cont_enc_url; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_content_url_encryption
    ADD CONSTRAINT fk_alf_cont_enc_url FOREIGN KEY (content_url_id) REFERENCES public.alf_content_url(id) ON DELETE CASCADE;


--
-- Name: alf_content_data fk_alf_cont_loc; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_content_data
    ADD CONSTRAINT fk_alf_cont_loc FOREIGN KEY (content_locale_id) REFERENCES public.alf_locale(id);


--
-- Name: alf_content_data fk_alf_cont_mim; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_content_data
    ADD CONSTRAINT fk_alf_cont_mim FOREIGN KEY (content_mimetype_id) REFERENCES public.alf_mimetype(id);


--
-- Name: alf_content_data fk_alf_cont_url; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_content_data
    ADD CONSTRAINT fk_alf_cont_url FOREIGN KEY (content_url_id) REFERENCES public.alf_content_url(id);


--
-- Name: alf_lock fk_alf_lock_excl; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_lock
    ADD CONSTRAINT fk_alf_lock_excl FOREIGN KEY (excl_resource_id) REFERENCES public.alf_lock_resource(id);


--
-- Name: alf_lock fk_alf_lock_shared; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_lock
    ADD CONSTRAINT fk_alf_lock_shared FOREIGN KEY (shared_resource_id) REFERENCES public.alf_lock_resource(id);


--
-- Name: alf_lock_resource fk_alf_lockr_ns; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_lock_resource
    ADD CONSTRAINT fk_alf_lockr_ns FOREIGN KEY (qname_ns_id) REFERENCES public.alf_namespace(id);


--
-- Name: alf_node_aspects fk_alf_nasp_n; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node_aspects
    ADD CONSTRAINT fk_alf_nasp_n FOREIGN KEY (node_id) REFERENCES public.alf_node(id);


--
-- Name: alf_node_aspects fk_alf_nasp_qn; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node_aspects
    ADD CONSTRAINT fk_alf_nasp_qn FOREIGN KEY (qname_id) REFERENCES public.alf_qname(id);


--
-- Name: alf_node_assoc fk_alf_nass_snode; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node_assoc
    ADD CONSTRAINT fk_alf_nass_snode FOREIGN KEY (source_node_id) REFERENCES public.alf_node(id);


--
-- Name: alf_node_assoc fk_alf_nass_tnode; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node_assoc
    ADD CONSTRAINT fk_alf_nass_tnode FOREIGN KEY (target_node_id) REFERENCES public.alf_node(id);


--
-- Name: alf_node_assoc fk_alf_nass_tqn; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node_assoc
    ADD CONSTRAINT fk_alf_nass_tqn FOREIGN KEY (type_qname_id) REFERENCES public.alf_qname(id);


--
-- Name: alf_node fk_alf_node_acl; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node
    ADD CONSTRAINT fk_alf_node_acl FOREIGN KEY (acl_id) REFERENCES public.alf_access_control_list(id);


--
-- Name: alf_node fk_alf_node_loc; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node
    ADD CONSTRAINT fk_alf_node_loc FOREIGN KEY (locale_id) REFERENCES public.alf_locale(id);


--
-- Name: alf_node fk_alf_node_store; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node
    ADD CONSTRAINT fk_alf_node_store FOREIGN KEY (store_id) REFERENCES public.alf_store(id);


--
-- Name: alf_node fk_alf_node_tqn; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node
    ADD CONSTRAINT fk_alf_node_tqn FOREIGN KEY (type_qname_id) REFERENCES public.alf_qname(id);


--
-- Name: alf_node fk_alf_node_txn; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node
    ADD CONSTRAINT fk_alf_node_txn FOREIGN KEY (transaction_id) REFERENCES public.alf_transaction(id);


--
-- Name: alf_node_properties fk_alf_nprop_loc; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node_properties
    ADD CONSTRAINT fk_alf_nprop_loc FOREIGN KEY (locale_id) REFERENCES public.alf_locale(id);


--
-- Name: alf_node_properties fk_alf_nprop_n; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node_properties
    ADD CONSTRAINT fk_alf_nprop_n FOREIGN KEY (node_id) REFERENCES public.alf_node(id);


--
-- Name: alf_node_properties fk_alf_nprop_qn; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_node_properties
    ADD CONSTRAINT fk_alf_nprop_qn FOREIGN KEY (qname_id) REFERENCES public.alf_qname(id);


--
-- Name: alf_permission fk_alf_perm_tqn; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_permission
    ADD CONSTRAINT fk_alf_perm_tqn FOREIGN KEY (type_qname_id) REFERENCES public.alf_qname(id);


--
-- Name: alf_prop_link fk_alf_propln_key; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_link
    ADD CONSTRAINT fk_alf_propln_key FOREIGN KEY (key_prop_id) REFERENCES public.alf_prop_value(id) ON DELETE CASCADE;


--
-- Name: alf_prop_link fk_alf_propln_root; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_link
    ADD CONSTRAINT fk_alf_propln_root FOREIGN KEY (root_prop_id) REFERENCES public.alf_prop_root(id) ON DELETE CASCADE;


--
-- Name: alf_prop_link fk_alf_propln_val; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_link
    ADD CONSTRAINT fk_alf_propln_val FOREIGN KEY (value_prop_id) REFERENCES public.alf_prop_value(id) ON DELETE CASCADE;


--
-- Name: alf_prop_unique_ctx fk_alf_propuctx_p1; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_unique_ctx
    ADD CONSTRAINT fk_alf_propuctx_p1 FOREIGN KEY (prop1_id) REFERENCES public.alf_prop_root(id);


--
-- Name: alf_prop_unique_ctx fk_alf_propuctx_v1; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_unique_ctx
    ADD CONSTRAINT fk_alf_propuctx_v1 FOREIGN KEY (value1_prop_id) REFERENCES public.alf_prop_value(id) ON DELETE CASCADE;


--
-- Name: alf_prop_unique_ctx fk_alf_propuctx_v2; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_unique_ctx
    ADD CONSTRAINT fk_alf_propuctx_v2 FOREIGN KEY (value2_prop_id) REFERENCES public.alf_prop_value(id) ON DELETE CASCADE;


--
-- Name: alf_prop_unique_ctx fk_alf_propuctx_v3; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_prop_unique_ctx
    ADD CONSTRAINT fk_alf_propuctx_v3 FOREIGN KEY (value3_prop_id) REFERENCES public.alf_prop_value(id) ON DELETE CASCADE;


--
-- Name: alf_qname fk_alf_qname_ns; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_qname
    ADD CONSTRAINT fk_alf_qname_ns FOREIGN KEY (ns_id) REFERENCES public.alf_namespace(id);


--
-- Name: alf_store fk_alf_store_root; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_store
    ADD CONSTRAINT fk_alf_store_root FOREIGN KEY (root_node_id) REFERENCES public.alf_node(id);


--
-- Name: alf_subscriptions fk_alf_sub_node; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_subscriptions
    ADD CONSTRAINT fk_alf_sub_node FOREIGN KEY (node_id) REFERENCES public.alf_node(id) ON DELETE CASCADE;


--
-- Name: alf_subscriptions fk_alf_sub_user; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_subscriptions
    ADD CONSTRAINT fk_alf_sub_user FOREIGN KEY (user_node_id) REFERENCES public.alf_node(id) ON DELETE CASCADE;


--
-- Name: alf_usage_delta fk_alf_usaged_n; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_usage_delta
    ADD CONSTRAINT fk_alf_usaged_n FOREIGN KEY (node_id) REFERENCES public.alf_node(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: alfresco
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--
