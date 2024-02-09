--
-- PostgreSQL database dump
--

-- Dumped from database version 11.7 (Debian 11.7-2.pgdg90+1)
-- Dumped by pg_dump version 13.2

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

SET default_tablespace = '';

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


ALTER TABLE public.act_evt_log_log_nr__seq OWNER TO alfresco;

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


ALTER TABLE public.alf_access_control_entry_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_access_control_list_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_ace_context_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_acl_change_set_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_acl_member_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_activity_feed_control_seq OWNER TO alfresco;

--
-- Name: alf_activity_feed_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_activity_feed_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.alf_activity_feed_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_activity_post_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_audit_app_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_audit_entry_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_audit_model_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_auth_status_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_authority_alias_seq OWNER TO alfresco;

--
-- Name: alf_authority_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_authority_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.alf_authority_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_child_assoc_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_content_data_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_content_url_enc_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_content_url_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_encoding_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_locale_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_lock_resource_seq OWNER TO alfresco;

--
-- Name: alf_lock_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_lock_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.alf_lock_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_mimetype_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_namespace_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_node_assoc_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_node_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_permission_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_prop_class_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_prop_double_value_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_prop_root_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_prop_serializable_value_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_prop_string_value_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_prop_unique_ctx_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_prop_value_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_qname_seq OWNER TO alfresco;

--
-- Name: alf_server; Type: TABLE; Schema: public; Owner: alfresco
--

CREATE TABLE public.alf_server (
    id bigint NOT NULL,
    version bigint NOT NULL,
    ip_address character varying(39) NOT NULL
);


ALTER TABLE public.alf_server OWNER TO alfresco;

--
-- Name: alf_server_seq; Type: SEQUENCE; Schema: public; Owner: alfresco
--

CREATE SEQUENCE public.alf_server_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.alf_server_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_store_seq OWNER TO alfresco;

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
    server_id bigint,
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


ALTER TABLE public.alf_transaction_seq OWNER TO alfresco;

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


ALTER TABLE public.alf_usage_delta_seq OWNER TO alfresco;

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
2	1	2f6a6dc1-54c5-4829-aec9-c4c75c4e4268bpmn20.xml	1	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0d0a0d0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220d0a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220d0a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220d0a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220d0a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f7267223e0d0a0d0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d2261637469766974694164686f6322206e616d653d224164686f632041637469766974692050726f63657373223e0d0a0d0a20202020202020203c73746172744576656e742069643d227374617274220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a7375626d69744164686f635461736b22202f3e0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773127200d0a202020202020202020202020736f757263655265663d277374617274270d0a2020202020202020202020207461726765745265663d276164686f635461736b27202f3e0d0a0d0a20202020202020203c757365725461736b2069643d226164686f635461736b22206e616d653d224164686f63205461736b220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a6164686f635461736b223e0d0a20202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a2020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a2020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b62706d5f61737369676e65652e70726f706572746965732e757365724e616d657d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773227200d0a2020202020202020736f757263655265663d276164686f635461736b270d0a2020202020202020202020207461726765745265663d277665726966795461736b446f6e6527202f3e0d0a0d0a20202020202020203c757365725461736b2069643d227665726966795461736b446f6e6522206e616d653d22566572696679204164686f63205461736b20436f6d706c657465642e220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a636f6d706c657465644164686f635461736b22203e0d0a2020202020202020202020203c646f63756d656e746174696f6e3e0d0a2020202020202020202020202020202056657269667920746865207461736b2077617320636f6d706c657465642e0d0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020202020200d0a2020202020202020202020202020202020202020202020206966202877665f6e6f746966794d65290d0a0909092020202020202020202020207b0d0a090909202020202020202020202020202020766172206d61696c203d20616374696f6e732e63726561746528226d61696c22293b0d0a0909092020202020202020202020202020206d61696c2e706172616d65746572732e746f203d20696e69746961746f722e70726f706572746965732e656d61696c3b0d0a0909092020202020202020202020202020206d61696c2e706172616d65746572732e7375626a656374203d20224164686f63205461736b2022202b2062706d5f776f726b666c6f774465736372697074696f6e3b0d0a0909092020202020202020202020202020206d61696c2e706172616d65746572732e66726f6d203d2062706d5f61737369676e65652e70726f706572746965732e656d61696c3b0d0a0909092020202020202020202020202020206d61696c2e706172616d65746572732e74657874203d20224974277320646f6e65223b0d0a0909092020202020202020202020202020206d61696c2e657865637574652862706d5f7061636b616765293b0d0a0909092020202020202020202020207d0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77332720736f757263655265663d277665726966795461736b446f6e65270d0a2020202020202020202020207461726765745265663d27746865456e6427202f3e0d0a0d0a20202020202020203c656e644576656e742069643d22746865456e6422202f3e0d0a0d0a2020203c2f70726f636573733e0d0a0d0a2020203c212d2d2047726170686963616c20726570726573656e7461696f6e206f66206469616772616d202d2d3e0d0a2020203c62706d6e64693a42504d4e4469616772616d2069643d2242504d4e4469616772616d5f61637469766974694164686f63223e0d0a2020202020203c62706d6e64693a42504d4e506c616e652062706d6e456c656d656e743d2261637469766974694164686f63222069643d2242504d4e506c616e655f61637469766974694164686f63223e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227374617274220d0a20202020202020202020202069643d2242504d4e53686170655f7374617274223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d2233302220793d22323030223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d226164686f635461736b220d0a20202020202020202020202069643d2242504d4e53686170655f6164686f635461736b223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22313330220d0a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227665726966795461736b446f6e65220d0a20202020202020202020202069643d2242504d4e53686170655f7665726966795461736b446f6e65223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22323930220d0a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22746865456e64220d0a20202020202020202020202069643d2242504d4e53686170655f746865456e64223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d223435352220793d22323030223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7731222069643d2242504d4e456467655f666c6f7731223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d2236352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223133302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7732222069643d2242504d4e456467655f666c6f7732223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223233352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223239302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7733222069643d2242504d4e456467655f666c6f7733223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223435352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020203c2f62706d6e64693a42504d4e506c616e653e0d0a2020203c2f62706d6e64693a42504d4e4469616772616d3e0d0a0d0a3c2f646566696e6974696f6e733e	f
3	1	2f6a6dc1-54c5-4829-aec9-c4c75c4e4268activitiAdhoc.png	1	\\x89504e470d0a1a0a0000000d49484452000001f4000000ff0806000000076624fd0000144949444154785eeddd79b054d5990070b7e84c8cd154b462d4c454529549a25395542c3331498d9599d2228af90b1ee08622485c52710b2e65505414c84c8d5bf48fa4269ad2b8e244270611d1c1058d611079c42886e8a020820ba22208dc39a77df7557bba1fbce7db6e9ff7fb557dd5ef2eaffb74f7d7dfd7f7f6eddbdb6d070000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000545c51143b2f5bb6ecb6f9f3e76f7ce081078afbefbf5f5420c273b165debc792be6ce9dfbc3f439cb89fcab660c95fc83ac84627a7b78e116ab56ad2ad6af5f5f6cd8b0415420e273119f93871e7a686d28b047a4cf5b2ee45f3563a8e41f64256e19c5176efa8216d588952b57ae0905f58fe9f3960bf957edc83dff202b7137a72da3ea467c6e42417d2f7dde7221ffaa1db9e71f64257e5e96be8845b5223e47e9f3960bf957fdc839ff202bdd2da86fbfb9b258f6e47f164be64ca945fc3bce4bd7137d1f391754f957fdc839ff202bdd29a8ebde78b968bfefc262d1efcff950c4797159babee8dbc8b9a0cabfea47cef90759e94e415dbee4ee86625ac64b4bee69585ff46de45c50e55ff523e7fc83ac74a7a03ef3e0b486425a465c96ae2ffa36722ea8f2affa9173fe4156ba5350dbefbfa8a190961197a5eb8bbe8d9c0baafcab7ee49c7fbd356cd8b05ddadada0e1b3d7af47f84cb59a3468dfadf102f8528e26598b7305cde172eaf0a97470c1f3efce3e9750c1923468cd8f1baebae3bf1e73ffff9c3175c70c1ab679e79e63b13274edc141fac7879c61967bc13e74f9b36edd12bafbc7242587fe7f43ad83a05b5fa917341957fd58f9cf3efa30a0dfcdba149df117ad1ba8ee6dddd58dfd1e087ce19f866cc9871e025975cb260dcb8719bcf3ffffce28e3bee28162d5a54bcf0c20bc56bafbd5644afbffe7a6dfae9a79f2e66ce9c594c9e3cb9183b766c5c7fc979e79df7cfe975d25c770a6a3caa382da465c465e9faa26f23e7822affaa1f39e75f4f85467e4068c8ffd5a4517f94885bf347a5b7918dd094f7993265ca43279d74d2e6db6fbfbdd6b47be2edb7df2e66cf9e5d9c72ca299bc3d67c7bd88a3f38bd0d3eac3b0575e9a3d73414d232e2b2747dd1b7917341957fd58f9cf3af2742f33d3f446d0f717d845e53dc7cf3cdc5534f3dd574a3336e8cde7aebad45d8d04c1b7a19b3478c18b1577a7b2d6dead4a9474e983061c30d37dc50bcf5d65b49abee99f7df7fbfd6d8c3f56dfcd18f7e34fda28b2eda21bd3d3ed09d82fadacb4f17edb3273714d3382f2e4bd7177d1b391754f957fdc839ffba2334dbbf0f4df7e6fa263c66cc98e217bff8453c356eda7eb6ea8d37dea835f7134e38216deacb478e1cf99df4b65bd225975c72d1f8f1e3372f5cb830bdffbd121fbcb0d5bf216cf13f367af4e83dd3dba57b0535c6dffe746343418df3d2f544df47ce0555fe553f72cebf6de968e6f3eb9b6fe857c5f2e5cbd376d323ebd6ad2b6ebae9a6da1b83baebded8d6d6765c3a869672e9a5975e7cfae9a76f79f9e597d3fbdc27366fde1c1fb8f78f3ffef865e1c1fb547afb435db70aea7bef154b1fbbaea1a0c6797159c3faa24f23e7822affaa1f39e7dfb6a45be6710f72ec297da5bdbdbd38f9e493eb9bfaa6d0d447a5e368091dbbd937f75733af179e8875c71e7bece363c78efdbb741c43d9b60a6a3c13d7738f5edd504ccb88cb9cadab7f23e7822affaa1f39e7dfd68c1e3dfabcfa667edf7df7a56da54fc4cfda274d9a54dfd4df0d71503a9e4abbe0820bf68d9f99f7f56ef6ae6cd9b2a5983163c69ab0957e5b3a96a1accb821ab67c562e9d5b2c9e757e43114d23ae13d7b5b5d43f917341957fd58f9cf3af2b1d47b3771e0017b7ccfb536ceaa79d765a7d537fbea5beb31e8f66efef0729150f961b3f7efc2b6d6d6d47a6e319aa9a15d46d6d157515b696fa27722ea8f2affa9173fe756554dd57d3e267e67db99bbd2b2fbdf45271e28927d637f519e9b82a297ecf3c7e356deddab5e97dea770b172e7c33bcfb7ade49683ed0aca07667aba8ab88ff9b5e9fe85de45c50e55ff523e7fc6b266cf0fd5368a65b62538d07adf5f600b89e98356b567d435f7ff4d147ef978eaf722ebef8e205f17be68365d2a4497f094dfdac745c4351b3829a16c99e467a7da277917341957fd58f9cf3af998e33c0d59a6afc6ada40dab4695371ce39e77436f5d0a7fe2d1d5fa5c4d3b98e1b376e534f4f1ad397dadbdb578607ebaf6138dba7e31b6a9a155451adc8b9a0cabfea47cef9978ae7660f4df4adb2a1ae58b1226d1ffd6ec18205f55be9ab2bbd37399e9b3d9ece7530c503e48e39e69855e1c13a241ddf50a3a0563f722ea8f2affa9173fea5c68c197378d94ccf3efbecb4750c88b8957ed249277536f5912347fe6b3aceca983e7dfa2383b9bbbd3475ead4c7c38375513abe5c84fbf6608843d3f92905b5fad18a0555fee513ad987fcd742727c3f22bcb461a4ffc3258aebffeface86ded6d6362d1d67655c78e185ab06eaab6a5b1392f4c9f040fd3e1d5f2eca64d856122ba8d58f562ca8f22f9f68c5fc6ba63b3919e6df5bae37987d6afefcf99d0d3dc47de9382b23fe046a3c61fd606b6f6ffff3a80f3e47cf525d326c358915d4ea472b1654f9974fb462fe35d39d9c1cf5c12fa0d5960f669f5aba7469fd38ff563fc64a993061c2a6788ef5c1b67af5ea57c203b5361d5f2e9a246fd3245650ab1fad58509be49dfc6bd168c5fc6ba6492e36e464b85c51ce1fcc3e157fb1ad6e7cd5ed53471f7d74ed042f836de3c68def85076a633abe5c3449da346a49aca0563f5ab1a036c9b734e45f8b442be65f334d72308d07dbdadade2fa707b34f85fe543faeeaf6a9e38f3f7ecb60bef329756ca1a74fe8908bc128a8210d6ad1d574ab467fdd8ff439cb290623ff7a12e9737ae79d77165ffce2178b1d76d8a15f9eeb34d2db1f8c489fb3a11283d9a7922df4973ada67f54c9c3871c3607e36515ab26449fc0cfd89747cb94893b32e1e1cd50fbb3ce3697cb7eb283ebff9cd6f1a96d747b95e57d3fd15e5ed7415e9fa3d8dbeba9e345a710ba949def56bfe1d70c001b5c77ef6ecd99df366cd9a559b77e0810736acdfdd88ff1fa39cfedce73e579b8e074ca5eb76377af35a198c68c5fc6ba6492e36e464b85c5ece1fcc3ef5fcf3cfd78fafba7deadc73cf5df3d4534fa5e31f7073e6ccf96378a066a6e3cbc5d692b65e5f15d461c386153bedb4532d7ef0831f342caf8fed9222954e0f44f4c76df6c775c668c5823ad0f917cfb71daeaef8f18f7fdc39eff4d34fafcdbbecb2cb1ad6ffa811af2f463abf27d19bd7ca60442be65f33ddc9c9d1a347ff4fb97c308f727fe28927eac759dd3e15bfff3d73e6cc74fc036edab4698f86076a723abe5c6c2d69ebf545418d6753fad8c73e561c7ef8e1c561871d56fb3bce2b973ff7dc73c5c1071f5c6cbffdf69d052a46b9bc7e5e8ccf7ce6331fda7279e59557e289808a3df7dcb316f1388c38af5c1e774f9d7aeaa9c5befbeedb791be918d348c7d06cd96ebbed561c72c821c5238f3cd2b9ecb6db6e2bbefce52fd7ee637a1df5d34f3ef964f1d9cf7eb656b4afbcf2ca86dbe849b462411dc8fc8b118f0a8ecffd97bef4a5ce79f1ef382f6eedc4e9356bd6c41f67aae5d7eebbef5e0c1f3ebc58b66c59e7fadb2579583fafd9f218dffad6b76a9771ab3bae73c71d77d4a6bffbddef368c31466f5f2b8311ad987fcd742727c3fcebcaf56ebdf5d6b46d0c985ffdea579d0d3dbcc9b83c1d676584867ef6e4c993d3f10fb8d0209687c6f08fe9f872b1b5a4add71705f5aaabaeea2c6abffef5af6b7f5f7df5d59dcb63e18cf362932e97c7289797d3d75c734dedfb97f1efbdf7debb73796ce6e9ffc779e5f271e3c6d5e6fdf4a73f2dde7aebad86f1358b740ccd62f1e2c5b575bef6b5af75cedb6bafbd6a45f8de7bef6d58bfbcceb8abf7939ffc64b1c71e7b147ff8c31f1ad6eb69b462411dc8fc2b23bef90a57593cfdf4d3c5a2458b6a7fd737d6134e38a136efaebbee2a1e7becb1dadfdffffef73b97c7e918f1fcddef75fc146b392f5da79c8e271f89d3471e79e4876ee39e7bee69185f8cdebe5606235a31ff9ae94e4eb6b5b51d5d36d2f3ce3b2f6d1b03229ec974e2c489f50dfddbe9382b63c48811bb1f77dc715bd6ad5b97de8f0113deb1bf1c1ea8e7d2b10d457d51506321fdc4273e513b882446fcbbbe90c6c6166eaa78f3cd376bcbe3df31cae5e5745cbe7efdfa86e59ffef4a71bfe3f6ea997cb63938df356ad5ad530b6ae22bd8d326233fee637bf59ecbaebae9debc403a0cae5fbecb34f6ddefefbef5f9c71c6194db7f062c38fe36b6f6f6fb8fe8f12b914d466fa22ffca289be515575c515c7ef9e5b5bfeb9b657c4ee2bcfad865975d3a9797f3e2af40a6f3ba9a7ef7dd778bcf7ffef3c5ce3bef5cdb6b14b7febff18d6f348cad8cdebe56062372cebf54e84f7b8feaf81df4d04807e5c0b8679e79a6b3998ffae080b86affe6c8b9e79efb4c3c7865b05c7ae9a54f8607ea92745c43516f0b6ab9ab73bba450d6efeadc5691dad6f44036f4b8db3ece7ff4d1476b853d5d2f7eb675d4514775ee72fffad7bfde709db1c0c7cba953a7365cff47899c0b6a6ff3af3ecaddd9dffbdef76a4d32dd9d5de649fcdde9f47f63c46531b6362f9d8e317dfaf4dabc934f3eb97679cb2db7345c778cbe78ad0c46e49c7fcd84463eb76ca883b1db3dd68dba867e4d3abecaf9c94f7e72c4a9a79eba7930bee7b77cf9f2d7c383f4ea9831633e958e6b28ea6d412d0f468ac957ce1b3972646d5e793052dc1d19a7bbda8db8ade9eeee720f6f147bbdcbbd2cfa0f3ffc70edb3efaed68b9fabc7f9f173f2f43a63e18e5bf0f1ef2953a634fc6f4f23e782dadbfc4b231e70b6e38e3bd6223de0accc93507f3eb4155e465c16636bf3d2e918ab57afae1d6f111bf357bef295da5ea6f4ba630cc46b259dee8bc839ff9a696b6b1b5536d4f811ca40ee4d8e1f15d535f38d23468cf887747c95141afa5f07632bfdacb3ce5a1c1ea833d2f10c55bd2da8e5d785eebefbeece79bffbddef6af3caaf0b3dfbecb3c541071dd4b07552aebfade9f2a0b8b8a51ea3ab83e2e2eef0de1e14f7dbdffeb6f6f97dfd389b8d2d6efd7df5ab5ffdd0fdae5f376e717de10b5fa84dffec673f6bb89d9e44ce05b5b7f997c68d37ded8f93cc4bfeb97c53c39edb4d38afdf6dbaff37be431cae5e974b379e97419e5d6793c98295d56c640bc56d2e9be889cf3af99430f3d74a7d023fe5636d681fa9196783299fadf420f6f2cae4dc7565913274efcce840913360ee4671473e6ccf96b78a09656faf76507585f1754d1f7917341cd21ffe21b85f8d14b3caafe9d77de6958deea9173fe7565745036d63163c6d48e87e96fd75e7b6dfdd6f9dad0a7f64ac75569e3c78fbf6ef2e4c91b366fde9cdeb73e17def9ae0ecfd1eaf0aee7c0741c43590e0535f7c8b9a0b67afec5061eee46edd88b79f3e6352ccf2172cebfadd87ed40747c5d71a6cdc03f3faebafa76da5cfc43d3575cd3c1e9077723aa0ca1b366cd82ee3c68d5b74d34d37f5eb87e9ab57af5e7fcc31c7bc129af991e91886ba562fa84321722ea8f2affa9173fe6d4dd832df3ff48c37ca261bbf16db1f4d3d36f378447d4bee6a4fc5dd0ac71d77dc8b37dc70c3dbf1fb777d2d6e998766be2a3c60a7a4b78d82da0a917341957fd58f9cf36f5b42733d6c54c7d7d862c46330e2b724fa42fccc3cd9cd1e634efc0c3f1d474b89dffd0b4df7d9193366acd9b469537abf3fb2f89979c76e765be65d5050ab1f391754f957fdc839ffba23f490e3eb9bfa89279e583b5f456f7a553c9abdfe00b8b2998f1d3b768ff4f65bd251471db5db983163ee1a3f7efc2b0b172e7c337d007a227e35ade368f6a53e33df3a05b5fa917341957fd58f9cf3afbb3abecaf66e7d038e0d79c18205dd6eec710f743c694cf23df35ac4ddec2dbf65de4c7837744c885593264dfacbe2c58b57a60fcad6c433c05d76d9657f0a0fd0abe1013ad3d1ecdba6a0563f722ea8f2affa9173fef544e82b0785783e6dc6f1fc06d75f7f7df1f8e38fd7cec51f7f33208a97f19c14f16454bffce52f3f743ad7ba58db9207c0f5c4b1c71ebb6b68c8e7843bbb2c7efe1d7fd02524d593a1c1fff9d5575f5d111fac78d9dedebe24fe6adaf4e9d31f89e7660feb2f0d31c54963ba4f41ad7ee45c50e55ff523e7fceba9e1c3877f3c34e0e9a1cfbcd7a439f72436c6adf296fb6a5a6f8d1c39f23be1ce5f14e2bf477dd0b0dfee7840fe2fc4fc107786989cf30fadf42705b5fa917341957fd58f9cf3efa30afd66bfd077fe3dc4ea26cd7a6bf15247236f8d33c0d15a14d4ea47ce0555fe553f72cebfde8a5fbd0e4dfa5f42939e162eef0db16cd487373a1f0f3133c415210e49ff1ffa94825afdc8b9a0cabfea47cef907595150ab1f391754f957fdc839ff202b0a6af523e7822affaa1f39e71f644541ad7ee45c50e55ff523e7fc83ac28a8d58f9c0baafcab7ee49c7f9095071e7860cbfaf5eb1b5ec4a21a119e9b15a1a0be973e6fb9907fd58edcf30fb2326fdebc15ab56ad6a78218b6ac48b2fbe784b28a87f4c9fb75cc8bf6a47eef90759993b77ee0f1f7ae8a1b52b57ae5c634ba93a119e8b952fbcf0c2cda198fe5f8823d2e72d17f2af9a3154f20fb2135fb0f15d78880df1f332518988cf457c4eb22fa6f13e76dc57f9579d1832f907000000000000000000000000000000000095f3ff88208ee01f35ceac0000000049454e44ae426082	t
6	1	243b225e-d44e-46ab-b805-881f0c50f8c8bpmn20.xml	5	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0d0a0d0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220d0a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220d0a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220d0a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220d0a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f7267223e0d0a0d0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d22616374697669746952657669657722206e616d653d2252657669657720416e6420417070726f76652041637469766974692050726f63657373223e0d0a0d0a20202020202020203c73746172744576656e742069643d227374617274220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a7375626d69745265766965775461736b22202f3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773127200d0a202020202020202020202020736f757263655265663d277374617274270d0a2020202020202020202020207461726765745265663d277265766965775461736b27202f3e0d0a0d0a20202020202020203c757365725461736b2069643d227265766965775461736b22206e616d653d22526576696577205461736b220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a61637469766974695265766965775461736b223e0d0a20202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f7265766965774f7574636f6d65272c207461736b2e6765745661726961626c65282777665f7265766965774f7574636f6d652729293b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b62706d5f61737369676e65652e70726f706572746965732e757365724e616d657d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773227200d0a2020202020202020736f757263655265663d277265766965775461736b270d0a2020202020202020202020207461726765745265663d277265766965774465636973696f6e27202f3e0d0a0d0a20202020202020203c6578636c757369766547617465776179202069643d227265766965774465636973696f6e22206e616d653d22526576696577204465636973696f6e22202f3e200d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77332720736f757263655265663d277265766965774465636973696f6e27207461726765745265663d27617070726f76656427203e0d0a2020202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b77665f7265766965774f7574636f6d65203d3d2027417070726f7665277d3c2f636f6e646974696f6e45787072657373696f6e3e0d0a20202020202020203c2f73657175656e6365466c6f773e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773427200d0a2020202020202020736f757263655265663d277265766965774465636973696f6e270d0a2020202020202020202020207461726765745265663d2772656a656374656427202f3e0d0a0d0a202020202020203c757365725461736b2069643d22617070726f76656422206e616d653d22446f63756d656e7420417070726f766564220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a617070726f7665645461736b22203e0d0a2020202020202020202020203c646f63756d656e746174696f6e3e0d0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e6420617070726f7665642e0d0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a20202020202020200d0a20202020202020203c757365725461736b2069643d2272656a656374656422206e616d653d22446f63756d656e742052656a6563746564220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a72656a65637465645461736b22203e0d0a2020202020202020202020203c646f63756d656e746174696f6e3e0d0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e642072656a65637465642e0d0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77352720736f757263655265663d27617070726f766564270d0a2020202020202020202020207461726765745265663d27656e6427202f3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77362720736f757263655265663d2772656a6563746564270d0a2020202020202020202020207461726765745265663d27656e6427202f3e0d0a0d0a20202020202020203c656e644576656e742069643d22656e6422202f3e0d0a0d0a202020203c2f70726f636573733e0d0a202020200d0a2020203c212d2d2047726170686963616c20726570726573656e7461696f6e206f66206469616772616d202d2d3e0d0a2020203c62706d6e64693a42504d4e4469616772616d2069643d2242504d4e4469616772616d5f6163746976697469526576696577223e0d0a2020202020203c62706d6e64693a42504d4e506c616e652062706d6e456c656d656e743d226163746976697469526576696577220d0a20202020202020202069643d2242504d4e506c616e655f6163746976697469526576696577223e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227374617274220d0a20202020202020202020202069643d2242504d4e53686170655f7374617274223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d2233302220793d22323030223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965775461736b220d0a20202020202020202020202069643d2242504d4e53686170655f7265766965775461736b223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22313235220d0a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965774465636973696f6e220d0a20202020202020202020202069643d2242504d4e53686170655f7265766965774465636973696f6e223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223430222077696474683d2234302220783d223239302220793d22313937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22617070726f766564220d0a20202020202020202020202069643d2242504d4e53686170655f617070726f766564223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220d0a202020202020202020202020202020793d223937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d2272656a6563746564220d0a20202020202020202020202069643d2242504d4e53686170655f72656a6563746564223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220d0a202020202020202020202020202020793d22323937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22656e64222069643d2242504d4e53686170655f656e64223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d223535352220793d22333037223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7731222069643d2242504d4e456467655f666c6f7731223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d2236352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223132352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7732222069643d2242504d4e456467655f666c6f7732223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223233302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223239302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7733222069643d2242504d4e456467655f666c6f7733223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313937223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7734222069643d2242504d4e456467655f666c6f7734223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22323337223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7735222069643d2242504d4e456467655f666c6f7735223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22333037223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7736222069643d2242504d4e456467655f666c6f7736223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223535352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020203c2f62706d6e64693a42504d4e506c616e653e0d0a2020203c2f62706d6e64693a42504d4e4469616772616d3e0d0a0d0a3c2f646566696e6974696f6e733e	f
7	1	243b225e-d44e-46ab-b805-881f0c50f8c8activitiReview.png	5	\\x89504e470d0a1a0a0000000d49484452000002580000016a0806000000c63c24e70000223749444154785eeddd7b901c65dd2ff07055f1fa965c14102dadf2c67baa44df3ae5ab65113da88508feb56c3689dcc2250a5649245c4bc34551c073ea7094cb1f5a1a2d50a32fde8e9a401272a21008f286c8465e09866042364b124212649390a44f3fe3f63a79667677669fd96cefcee753f5addde9eeed69b637bff9d2333b3b691200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0789765d9e16bd6ac99b76cd9b2dd8b162dcaeebbef3e2941f273b16fe9d2a51b162f5efc99f89c0100259797ab9fe60fe4596f6f6fd6d7d797eddab54b4a90702ec23959b264c9b6bc709d169f3700a0c4c295abf0401e3fc04b39d2d3d3b3392f58cbe3f306009458785ad095abf2269c9bbc60ed8ccf1b005062e1f53ef183ba942be11cc5e70d0028b1460bd68b2ff4646b1ef95eb66ae1f59584cfc3b2783b697d142c0018671a29583bb63e9b752ff872b6f237b3f74b5816d6c5db4b6ba36001c038d348c15ab7ea5735e5aac8fa55bfaed95e5a1b050b00c699460ad613f7df5453ac8a8475f1f6d2da28580030ce3452b0baefbbb6a6581509ebe2eda5b551b000609c51b0ca1f050b00c699460a56f8adc1b8581509ebe2eda5b551b000609c69a460ad7ee0db35c5aa4858176f2fad8d820500e34c23056bcbb37fcabaef9d5353aec2b2b02ede5e5a1b050b00c699460a56c8d37ffc414dc10acbe2eda4f551b000609c69a860eddc99ad7ef08e9a821596857535db4b4ba36001c038335cc10aefd4fee403dfaa295745c23aefe63eba51b000609c19b460eddc99f5ac5e9c3d3effea9a5215276c13b675356b74a26001c03853af600d77d56ab0b89a353a51b000609ca957b01ab96a3558c2d7c6fb93b428580030ced42b5871696a36f1fe242d0a16008c33f50a96942b0a160094c4942953eecf33395e1e53b0ca1f050b004a222f57597f862c5a0a56f9a3600140495415ac218b968255fe2858005012750a56dda2a560953f0a160094449d6215a752b414acf247c102e080ab531ca4892858e54f7ccea4b1c4b302802618a4f5c50f3655b97f8aa708c7555cc16a9eb90090c820ad6fb862553890052bbfbb4a0e3becb0ecc8238fcc4e39e51457d01a8882d53c73012091415adf70c5aa70200bcea4fe82d5d7d7972d5fbe3c3bf1c413b3430e39245bb06041cdb6f2cf2858cd3317001219a4f50d57ac0a6351b08adbf3e7cfafdc9e3c79f2c0b28d1b3766d3a74faf5ce10a99366d5a6559b17ecb962dd9c5175f9c1d77dc71d941071db4dffee2fd0f76bbc811471c919d74d249d909279c901d7ef8e1d9f1c71f9fcd9b37afe6b8c73a0a56f3cc05804406699ab12c589b366daadc7ee31bdf38b02c94abb0ec873ffc61f6fdef7fbff2795856ac9f31634665d9e5975f9e6ddfbebd66ff071f7cf0a0f757dcbeedb6db2a57d0aa6f3ffae8a395cf8f3df6d8fdf659862858cd3317001219a469c6b2603df7dc7395dbe14a55b12c94adb0ec85175ec8b66edd5ab3fea8a38eaa2cebededadbbfff094e360f757dc0efb7ee9a597066e87fba9b77d59a26035cf5c00486490a619cb8255ef29c2d482555cc10a5f1fdf5fb3b7cb1205ab79e60240228334cd5814aca15ee4dee85384575e7965cd5384c71c734c65dd430f3d94dd7efbed3585a999db43adab777b34a36035cf5c00486490a6198b8275e8a18756ae547df4a31fad799b86e245ee617dc8602f720faf958a5fe43e77eedcece8a38f1eb89f22f1fd37727ba875f56e8f6614ace6990b00890cd23471c191f245c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e602402283348d8255fe2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e60240228334cda2458bf6f5f5f5d53ca84b39929f9b0d79c1da199f3786662e00243248d32c5dba74436f6f6fcd03bb9423cf3cf3cc8ff382b53c3e6f0ccd5c00486490a659bc78f167962c59b2ada7a767b32b59e5497e2e7ad6ae5d7b775eaefe96e7b4f8bc313473012091419a2e3c8087ab24797685d7fb482912ce453827cad508980b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e30eab22c3b7ccd9a35f3962d5bb67bd1a245d97df7dd2725487e2ef62d5dba74c3e2c58b3f139f339a639002317381519797ab9fe60fe4596f6f6fd6d7d797eddab54b4a90702ec23959b264c9b6bc709d169f371a6790023173815117ae5c8507f2f8015eca919e9e9ecd79c15a1e9f371a67900231738151179e1674e5aabc09e7262f583be3f346e30c5220662e30eac2eb7de207752957c2398acf1b8d33488198b9c0a86bb460bdf8424fb6e691ef65ab165e5f49f83c2c8bb793d647c14a63900231738151d748c1dab1f5d9ac7bc197b395bf99bd5fc2b2b02ede5e5a1b052b8d410ac4cc05465d23056bddaa5fd594ab22eb57fdba667b696d14ac2427f70fd293e31540fb52b018758d14ac27eebfa9a6581509ebe2eda5b551b0462c94aade3c97f57f54b2800a058b51d748c1eabeefda9a625524ac8bb797d646c11a91a25c15a52abe0db43105ab8e8e8e8e43eeb8e38ef3bef9cd6ffefe9a6bae796ed6ac597f9f3973e69ef0cd0a1f2fbdf4d2bf87e537dd74d303b7de7aeb85f9f687c7fbe09f14acf247c16ada60656ab0e5409b51b0aadc72cb2dff7ac30d373c3a63c68cbd575f7d75f6b39ffd2c5bb97265b676edda6ccb962d59f0fcf3cf576effe94f7fcaeeb9e79e6cce9c39d939e79c13b65f75d5555719aa753452b0c26f0dc6c5aa4858176f2fad8d82d594e14ad470eb8136a060e5f29274ecf5d75fbfe4fcf3cfdffbd39ffeb452a29af1e28b2f66f7de7b6ff6f9cf7f7eefac59b3ba67ce9cf9dfe3fb68678d14acd50f7cbba6581509ebe2eda5b551b01ad668796a743b60826afb8275e38d377efac20b2fdc3577eedc6cfbf6ed71776acacb2fbf5c295af9fe767fee739fbbf9da6baf3d38bebf76d448c1daf2ec9fb2ee7be7d494abb02cac8bb797d646c16a48b3a5a9d9ed8109a4ad0bd60d37dc70ed05175cb077c58a1571574ab275ebd6f0d4e1aef3cf3fffc1aeaeae23e3fb6d378d14ac90a7fff8839a821596c5db49eba3600d6ba46569a45f078c736d5bb0befad5af5ef7852f7c61dfb3cf3e1bf7a396d8bb776f76d75d77bd7cf6d967af993a75eabfc4f7df4e1a2a583b7766ab1fbca3a6608565615dcdf6d2d22858434a2d49a95f0f8c436d59b0fa9f16dc3b5ae5aadadcb973777cf6b39f7de89c73ce79657c1ced62b88215dea9fdc907be5553ae8a8475decd7d74a3600daa55e5a855fb01c689b62b58d75c73cd71e13557ad7e5a7030fbf6edcb6eb9e596cd53a74e9d171f4bbb18b460eddc99f5ac5e9c3d3effea9a5215276c13b675356b74a260d5d5ea52d4eafd0125d676052bfcb6607841fb81145efc7ec105176cecececfc747c3ceda05ec11aeeaad56071356b74a260d588cbd04179ceeeffd88cf8ebe2fd0213545b15acf03e57e1ad18b66ddb1677a051b762c58a17bababa9e6ac73725ad57b01ab96a3558c2d7c6fb93b42858fb894b502847dfcd13be47e163a3256bb0af8bf70f4c406d55b0aebbeeba47c3fb5c8d952baeb8e2bff292f5a5f8b826ba7a052b2e4dcd26de9fa445c11a50affc842b50e1fb53a49192555dae8a9c55b5bedefd001348db14acf0e76f66cc98b1a7d937116da5eeeeee9efc1bfed749c30fe709a55ec1927245c1aa18acf4d42b4b4395ac7adb7fa77f79b5c1ee0f9800daa66085bf2d18fefccd580a2f789f3e7d7a6ffe4dff507c7c13998255fe4c948295ffdbba3fcfe4787903862b3bf54a53bd92556fbb7ae5aa30dcfd02e354db14ac9b6fbef90f63f9f460e1c61b6f7c28ffa65f1b1fdf78d4e883998255fe4ca08295f5a7a19fcd7e8d969c7ae5a9ba64d55b3f54b92a347affc038d23605ebcb5ffe72ef817a6b86a1e40f648f747676fe263ebef1a8d1073305abfc998005aba19fcd49cd979b7a252adc0e7f162b5ede48b92a347b1c40c9b54dc19a356bd6dfd7ae5d1bf79d03aebbbbfbcf53fef13aac71afd1073305abfc99c0056ba89fcd91969a7a25eb2fd1ed66ca5561a4c7039450db14ac0b2fbc704ff81b81636dd3a64d1bf36ffab6f8f8c6a33a0f62751fcc14acf2a70d0a56fcb3995a66ea95ac947255483d2ea024c2cc89974d48d3a64dabbce1e758dbbd7bf7cefc9bbe3b3ebef1a8ce83579cca83998255fed4397713361d1d1dd92b5ff9ca30f82e8b7fa69b149e168caf5c85db61798a705ca1641d11af00c68f306fe26513d2d9679fbdaf4457b06a86fe444e190bd6a4fe07c478f9784babfe3bdae00ad6fd53f67f8a30f54ad15057b0eafd7661a3528f0b2889307be26513d2cc99337795e13558ab56ad0aafc17a383ebef1a8ce8358dd07b39116ac49550f5aaf79cd6bb20f7ef083d91ffef0879aed469262bff1f256a7fabfa15ee2ed9b4dabf633810b565cacaa9d3c696465a65eb98aaf648da4648df47880126a9b8275e595576e7eecb1c7e2be73c02d5cb87079fe4dbf273ebef1a8d107b3d482153effd5af7e55f9fc3def794fcd76e325d5ff3dad4aabf639010b56dd9fc53a9a2d35f5ca5578cd55bddf226ca664357b1c40c9b54dc10aef3f75cf3df7c47de780bbe9a69b1ec8bfe973e2e31b8f1a7d306b45c1dab16347e5f357bdea5503eb376fde1cfe887676cc31c764af7ffdebb3d34f3f3d5bb3664dd6d3d3931d71c411d93bdef18e6ce7ce9d956dc3c7b7bffdedd9ab5ffdea6ce3c68dfbed7ba87d15eb8f3efae8eca8a38eaa7cfef5af7fbdf2b5dff8c6372ab7c3f2f075f1f1c789efb3debad7bef6b5d9873ef4a1fdaed4cd9b372f7be73bdf991d76d86135fba8befdc8238f646f7ef39bb3430f3d34bbf5d65b6bee63a84ca08235e4cfe2204e9ed458b919ac5c0df53e588d94ac46ef1f1847daa9605d3667ce9cb8ef1c70d3a74f5f376ddab4ff161fdf78d4e883592b0a567105eb539ffad4c0fa73cf3db7b2ece73fff79f6e0830f563effd8c73e565977d14517556efff297bfacdcfec52f7e51b93d73e6cc9a7d0fb7af904f7ce2139565ebd7afcfdefdee77573e7fd7bbde95ad5bb7aef2f9273ff9c99ae38f13df67bd3cfef8e3956ddefbdef70e2c0b052e94abdffef6b735db17fb9c3f7f7ef6bad7bd2e7bc31bde90fdee77bfabd96eb84c948295e0e44943979c7ae5a9de6f0bd6db6ea89235dcfd02e354db14ac8e8e8ed79f75d659fbc29590b1f2d4534f3d9b7fc39f8c8f6da24b2d5845def296b7644f3ffdf4c0fa238f3cb2669b57bce2159575ab56adca0e3ef8e04a310ab73ffef18f576efff9cf7fde6fdf8dec2b64f6ecd99565d75d775de563676767e5e357bef295cac72baeb8a2e6f8e3c4f7592494a30f7ce00395ab6bc536e1588bf5c71e7b6c65d95bdffad6ecd24b2fddefca5ab17d2860e1bfa1bbbbbb66ff8d44c1aa387952fdb253af34d52b57857adbd72b5983dd1f3001b44dc10aaebcf2ca27eebdf7deb8f71c305ffdea571fc9bfe137c4c735d1a516aceddbb7675ff8c2172a9f575f550a5776c2b2705529feda90d34e3b2d3be8a0832a57afc2c7f0b45fbcef46f775d75d7755d687a7f08e3beeb86cdbb66d958f4529bafbeebb6bbe264e7c9f45c27ec2f2071e78a0b2df78bb871f7e383be38c33069e227cdffbde57b3cf134e38a1f2f1c61b6facd97f2351b0069c3ca9b6f49c3da9fffbdc9fa1ca55a15ec93aab6a7dbdfb012690b62a585ffce2174fbbf8e28bf78ec5fb61ad5bb7eef9fc9bfddcd4a953ff253eae892eb56085cfc35b6c1457998aa7ca66cc9851b99d9fd74a3189bfbebf34547e03317c5cb87061dd7d37b2af7045acf89aebafbfbeb2acb89a15525c191b2af17d1629caddef7ffffbca6ba706db2ebc2e2b2c0fafb38af7b97af5eaca15aeeae36b260ad67e4e9eb47ff9a92e4b8d94abc2605f17ef1f9880daaa600579c1faeb585cc5fad297bef478fecdbe343e9e76d08a8215128a43b8fd918f7ca4727bcb962dd925975c921d7ffcf195a7d4e2ed43defffef75796858f43ed7bb87d8517c987ab57e169c3e22a57f8186e87e5c58be9874abccf223ffad18fb237bde94d03ebe3ed8adbe10a56f82dcaf07ab4785df8fca9a79ecadef6b6b7556e87a72ee3fb192a0a568d9327d596ac7005aad1725588bf2ede2f3041b55dc19a3973e6872fbcf0c2dd07f24d47172e5cf8d7fc1bbdbaa3a3e3f0f878dac1480b961cb8285875b5ba0cb57a7f4089b55dc10a2eb8e0823be6cc99b36befdebd71176ab9bffce52f9bbababa36757676fe6b7c1ced42c12a7f14ac41b5aa14b56a3fc038d19605ebd4534f7dc58c193356de75d75da3fa62ac4d9b36f54d9f3e7d635eae3e1d1f433b51b0ca1f056b48a9e528f5eb8171a82d0b56d0d1d171d459679df5ccdcb9735fdcb76f5fdc8d92852b5779b9eaedeaeafa7c7cdfed46c12a7f14ac618db4248df4eb8071ae6d0b569097ac37e525e82fb7dc72cbe63d7bf6c41d69c4c26baefa9f166ceb2b570505abfc51b01ad26c596a767b600269eb82159c71c619af9d3a75eacf2fb8e0828d2b56ac78212e4bcd086fc5d0ffdb82abdbf935573105abfc51b01ad668696a743b60826afb8255e8eaea9a9ea7f78a2baef8afc71f7fbc272e4f4309efd0feb5af7ded8fe17daef26235ab5d7f5b70300a56f9a3603565b8f234dc7aa00d2858553efbd9cfbe3a2f48b3f36fca9af0faa9f007a2f3079e47f2c2f5e7e79e7b6e432853e1637777f7aa850b172ebff9e69bff10feb660b86295e7fa767c13d1462858e58f82d5b4c14ad460cb8136a3600de2cc33cffc70fecdb936cfffed2f502f866f569ebfe55996e73ff2cc99287fb879342958e58f82352271998a6f036d4cc162d42958e58f82356245a9baacffa372055428588c3a05abfc51b0929cdc3f48952b608082c5a853b0ca1f052b8d410ac4cc05469d8255fe2858690c5220662e30ea14acf247c14a63900231738151a760953f0a561a831488990b8cba458b16edebebebab79509772243f371bf282b5333e6f34ce200562e602a36ee9d2a51b7a7b7b6b1ed8a51c79e699677e9c17ace5f179a371062910331718758b172ffecc92254bb6f5f4f46c7625ab3cc9cf45cfdab56befcecbd5dff29c169f371a67900231738103223c8087ab24797685d7fb482912ce453827ca5522831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e602402283344d966587af59b366deb265cb762f5ab428bbefbefba404c9cfc5bea54b976e58bc78f167e273c6fef219707f9803c3e4fef8eb0018828295262f573fcd1fc8b3dededeacafaf2fdbb56b979420e15c8473b264c9926d79e13a2d3e6ffc533e0326d729547126c75f07c01014ac34e1ca5578208f1fe0a51ce9e9e9d99c17ace5f179637fe10a559d52e5ea15c048295869c2d382ae5c9537e1dce4056b677cded85fb84255a758b97a0530520a569af07a9ff8415dca95708ee2f346ad70a5aa4eb972f50a602414ac348d16ac175fe8c9d63cf2bd6cd5c2eb2b099f8765f176d2fa28588d19e42ad6e4783b001aa060a569a460edd8fa6cd6bde0cbd9cadfccde2f615958176f2fad8d82d5b8e82a96ab570023a560a569a460ad5bf5ab9a725564fdaa5fd76c2fad8d82d5b8e82ad6e4783d000d52b0d23452b09eb8ffa69a625524ac8bb797d646c16a4e71152b5e0e40130cd2348d14aceefbaead295645c2ba787b696d14acc19d7aeaa9afe8ececfc445757d7ffce3fcecfe7c17fe6d9d47f056b7dbe6c45fe7141fef1ffe41f4f3bfdf4d38f88f701401d0a561a05abfc51b06ae585eadff3d2f4b3fcdfff8eaaa7041b495f7fe1f20ef9004351b0d23452b0c26f0dc6c5aa4858176f2fad8d82f54f79b13a31ff37ff8b3ac569240957bbce88ef0380490a56aa460ad6ea07be5d53ac8a8475f1f6d2da2858ff90ff5bbf3acf9eb828cd9a352bbbfbeebbb3c71e7b2c5bbb766db665cb962c78fef9e72bb757ae5c99fde4273fc9aebaeaaab86015b9b7a3a3e3a8f8fe00da9a8295a69182b5e5d93f65ddf7cea92957615958176f2fad4dbb17acbcfcbc2aff777e7775299a3a756a76fbedb7873f255429538ddaba756ba56c9d7beeb971c95a77e699677e38be6f80b6a560a569a460853cfdc71fd414acb02cde4e5a9f762e58fde56a597519bae1861bb275ebd6c5dda9293b76ecc8eebaebae4a51abdaf7eecececeb3e26300684b0a569a860ad6ce9dd9ea07efa8295861595857b3bdb434ed5cb0a64457aee6ce9d9bedddbb37ee4b23d6dddd9d5d74d145d5256b4f5eb2a6c4c701d07614ac34c315acf04eed4f3ef0ad9a725524acf36eeea39b762d585d5d5d575597ab050b16c4fda825c26bb5aeb8e28aea92f5529e7f8b8f07a0ad285869062d583b77663dab17678fcfbfbaa654c509db846d5dcd1a9db463c1eaff6dc18117b4872b57a32994ac4b2eb9a4ba643de53db380b6a660a5a957b086bb6a35585ccd1a9db463c19a52f5560ce13557ad7c5a7030ebd7afcfce3befbcea92754b7c5c006d43c14a53af603572d56ab084af8df7276969b782d5d9d9f9c1fcdff5bef06f3bbc083df505edcd983f7f7e75c1ea9b366ddaf1f1f101b405052b4dbd821597a66613ef4fd2d286052bbc437ba5e484b7623890f6ecd993cd9e3d7ba064757575fdcff8f800da828295a65ec19272a59d0a56f8db8279a9d95e149c0d1b36c41d68d43dfae8a3d557b1367574741c1e1f27c084a760d5977f5feecf33395e1e53b0ca9f762a5853a74efd64516e2ebbecb2b8fb1c10e12ad6f9e79f3f50b2ce3cf3cc53e2e30498f014acfaaafe0f7cc8a2a560953f13a5600df7b318e4eb6f2d7e76c31b818e953befbc73a060757676de141f27c084a760d55755b0862c5a0a56f933810ad6903f8b41befcb7c5762b56ac887bcf01b36cd9b2ea7f3b0be2e30498f0c2008c9751b760d57d7053b0ca9f0958b0eafe2cf66ff39fc5faf0879ac7caead5abab8ff3e9ea6304680b6100c6cba8fb6016a7f2e0a660953f13b860edf7b3d8bfcd866279f8c3cc6365cb962dd5c7b72dfa4f0198f8ea0c6b69220a56f9d30605abc8fd9d9d9d2f17b75f7ef9e5b8f71c30bb77efae3eaeddf17f0b006daace83d7c083d8144f118eabd439876d91125dc15a5ff54f0b8076163f584da9f37a97e04016acfcee2a39ecb0c3b2238f3c323be594535c416b206d70056be06733ffb8ae583e96afc17aeaa9a7aa8fefe1e83f05807655efc1ab9e03597026f517acbebebe6cf9f2e5d989279e981d72c821d982050b6ab6957f660217ac9a9fcdaeaeaeff57ac1fcbdf227cf8e187ab8ff39eea6304a08dd57bf0aa672c0a56717bfefcf995db93274f1e58b671e3c66cfaf4e9952b5c21d3a64dab2c2bd687a76e2ebef8e2ecb8e38ecb0e3ae8a0fdf617ef7fb0db458e38e288eca4934eca4e38e184ecf0c30fcf8e3ffef86cdebc7935c73dd69980056bd09fcd7cf91dc5763ff9c94fe2de73c07cf7bbdf1d285879e9fb7a7c9c0030a4b12c589b366daadc7ee31bdf38b02c94abb0ec873ffc61f6fdef7fbff2795856ac9f31634665d9e5975f9e6ddfbebd66ff071f7cf0a0f757dcbeedb6db2a57d0aa6f873f8f123e3ff6d863f7db671932810ad6a0c5aad0d9d939ad2836575d7555dc7b0e887dfbf6653367ceac2e58ff1e1f27000c692c0bd673cf3d57b91dae5415cb42d90acb5e78e185ca8b9ce3f5471d755465596f6f6fddfd87a71c07bbbfe276d8f74b2fbd34703bdc4fbdedcb928952b01ad1d1d1f1a6bcd4ece92f3663f242f7279e78a2fae9c1f002f783e2e30480218d65c1aaf714616ac12aae6085af8fefafd9db65493b15ac202f568bc7f269c21b6fbcb1ba607d3b3e3e0018d65814aca15ee4dee85384575e7965cd5384c71c734c65dd430f3d94dd7efbed3585a999db43adab777b34d36e05abb3b3734a5170ce3df7dc6cc78e1d71071a352b57aeac2e57bb3b3a3ade151f1f000c6b2c0ad6a1871e5ab952f5d18f7eb4e66d1a8a17b987f52183bdc83dbc562a7e91fbdcb973b3a38f3e7ae07e8ac4f7dfc8eda1d6d5bb3d9a69b7823579f2e443f372f37451740ed41f7d0e6f2e3a7bf6ec81829517bddbe263038086c40547ca97762b584157ae283a53a74ecdbabbbbe33ed472e1971daaae5e6debe8e8382a3e2e0068888255feb463c1ca1d34e51fbf7558293c175d7451f6fcf3cfc79da8657ef9cb5f5697abf002fb8be2030280862958e54f9b16ac4953a74e7d6b6767e7d6a2f484b7e6188d9215ca55f88d454f0d02d0320a56f9d3ae052bc8cbce27a6f4bf6d43c825975c92ad5fbf3eee4823125e73153d2d18b230bc062c3e0e00688a8255feb473c10abababaceae2e59e79d775ee52d3ef6ecd91377a68685df16ac7e417b51aece39e79c37c4f70f004d53b0ca9f762f5841ff5b37bc545d8842410aefc0df68d10aefd01ede44347a9fab4ac2d382ae5c01d0320a56f9a360fd435e84fe2dcf5371390aef8d76e79d7756deffecc9279fcc366fde5c2954e1e3ead5ab2b7fb8f93bdff9ce7e7ffea62adbbca01d809653b0ca1f05eb9f4e3ffdf423f24274735e8c76d6294bcd6477b86ae5ad180018150a56f9a360d59a366ddaf17949fa5f7936d5294f43657d7fb1f20eed008c1e05abfc51b00677eaa9a7be222f4dff232f4d37e51f7f9b674d9e17fbcbd4dff23c94e79e3cdfc8f3a1f8eb0160542858e58f820500e38c8255fe28580030ce2858e58f820500e38c8255fe28580030ce2858e58f820500e3cca2458bf6f5f5f5d53ca84b39929f9b0d79c1da199f3700a0c4962e5dbaa1b7b7b7e6815dca91679e79e6c779c15a1e9f3700a0c4162f5efc99254b966cebe9e9d9ec4a5679929f8b9eb56bd7de9d97abbfe5392d3e6f0040c98507f0709524cfaef07a1f2945c2b908e744b90200000000000000000000000000000000000000003810fe3fcb3727a59f8818ae0000000049454e44ae426082	t
10	1	5659a13f-f2e4-44ed-bbbf-e1347bc51867bpmn20.xml	9	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0d0a0d0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220d0a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220d0a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220d0a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220d0a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f7267223e0d0a0d0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469526576696577506f6f6c656422206e616d653d22506f6f6c65642052657669657720416e6420417070726f76652041637469766974692050726f63657373223e0d0a0d0a20202020202020203c73746172744576656e742069643d227374617274220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a7375626d697447726f75705265766965775461736b22202f3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773127200d0a202020202020202020202020736f757263655265663d277374617274270d0a2020202020202020202020207461726765745265663d277265766965775461736b27202f3e0d0a0d0a20202020202020203c757365725461736b2069643d227265766965775461736b22206e616d653d22526576696577205461736b220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a61637469766974695265766965775461736b223e0d0a20202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f7265766965774f7574636f6d65272c207461736b2e6765745661726961626c65282777665f7265766965774f7574636f6d652729293b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a20202020202020202020202020200d0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a20202020202020202020203c706f74656e7469616c4f776e65723e0d0a09092020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a090920202020202020203c666f726d616c45787072657373696f6e3e247b62706d5f67726f757041737369676e65652e70726f706572746965732e617574686f726974794e616d657d3c2f666f726d616c45787072657373696f6e3e0d0a09092020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a09092020203c2f706f74656e7469616c4f776e65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773227200d0a2020202020202020736f757263655265663d277265766965775461736b270d0a2020202020202020202020207461726765745265663d277265766965774465636973696f6e27202f3e0d0a0d0a20202020202020203c6578636c757369766547617465776179202069643d227265766965774465636973696f6e22206e616d653d22526576696577204465636973696f6e22202f3e200d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77332720736f757263655265663d277265766965774465636973696f6e27207461726765745265663d27617070726f76656427203e0d0a2020202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b77665f7265766965774f7574636f6d65203d3d2027417070726f7665277d3c2f636f6e646974696f6e45787072657373696f6e3e0d0a20202020202020203c2f73657175656e6365466c6f773e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773427200d0a2020202020202020736f757263655265663d277265766965774465636973696f6e270d0a2020202020202020202020207461726765745265663d2772656a656374656427202f3e0d0a0d0a202020202020203c757365725461736b2069643d22617070726f76656422206e616d653d22446f63756d656e7420417070726f766564220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a617070726f7665645461736b22203e0d0a2020202020202020202020203c646f63756d656e746174696f6e3e0d0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e6420617070726f7665642e0d0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282762706d5f61737369676e6565272c20706572736f6e293b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a20202020202020200d0a20202020202020203c757365725461736b2069643d2272656a656374656422206e616d653d22446f63756d656e742052656a6563746564220d0a20202020202020202020202061637469766974693a666f726d4b65793d2277663a72656a65637465645461736b22203e0d0a2020202020202020202020203c646f63756d656e746174696f6e3e0d0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e642072656a65637465642e0d0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282762706d5f61737369676e6565272c20706572736f6e293b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77352720736f757263655265663d27617070726f766564270d0a2020202020202020202020207461726765745265663d27656e6427202f3e0d0a0d0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77362720736f757263655265663d2772656a6563746564270d0a2020202020202020202020207461726765745265663d27656e6427202f3e0d0a0d0a20202020202020203c656e644576656e742069643d22656e6422202f3e0d0a0d0a202020203c2f70726f636573733e0d0a202020200d0a2020203c212d2d2047726170686963616c20726570726573656e7461696f6e206f66206469616772616d202d2d3e0d0a2020203c62706d6e64693a42504d4e4469616772616d2069643d2242504d4e4469616772616d5f6163746976697469526576696577506f6f6c6564223e0d0a2020202020203c62706d6e64693a42504d4e506c616e652062706d6e456c656d656e743d226163746976697469526576696577506f6f6c6564220d0a20202020202020202069643d2242504d4e506c616e655f6163746976697469526576696577506f6f6c6564223e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227374617274220d0a20202020202020202020202069643d2242504d4e53686170655f7374617274223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d2233302220793d22323030223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965775461736b220d0a20202020202020202020202069643d2242504d4e53686170655f7265766965775461736b223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22313235220d0a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965774465636973696f6e220d0a20202020202020202020202069643d2242504d4e53686170655f7265766965774465636973696f6e223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223430222077696474683d2234302220783d223239302220793d22313937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22617070726f766564220d0a20202020202020202020202069643d2242504d4e53686170655f617070726f766564223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220d0a202020202020202020202020202020793d223937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d2272656a6563746564220d0a20202020202020202020202069643d2242504d4e53686170655f72656a6563746564223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220d0a202020202020202020202020202020793d22323937223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22656e64222069643d2242504d4e53686170655f656e64223e0d0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d223535352220793d22333037223e3c2f6f6d6764633a426f756e64733e0d0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7731222069643d2242504d4e456467655f666c6f7731223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d2236352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223132352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7732222069643d2242504d4e456467655f666c6f7732223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223233302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223239302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7733222069643d2242504d4e456467655f666c6f7733223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313937223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7734222069643d2242504d4e456467655f666c6f7734223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22323337223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7735222069643d2242504d4e456467655f666c6f7735223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22313234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22333037223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7736222069643d2242504d4e456467655f666c6f7736223e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223535352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0d0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0d0a2020202020203c2f62706d6e64693a42504d4e506c616e653e0d0a2020203c2f62706d6e64693a42504d4e4469616772616d3e0d0a0d0a3c2f646566696e6974696f6e733e	f
11	1	5659a13f-f2e4-44ed-bbbf-e1347bc51867activitiReviewPooled.png	9	\\x89504e470d0a1a0a0000000d49484452000002580000016a0806000000c63c24e70000223749444154785eeddd7b901c65dd2ff07055f1fa965c14102dadf2c67baa44df3ae5ab65113da88508feb56c3689dcc2250a5649245c4bc34551c073ea7094cb1f5a1a2d50a32fde8e9a401272a21008f286c8465e09866042364b124212649390a44f3fe3f63a79667677669fd96cefcee753f5addde9eeed69b637bff9d2333b3b691200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0789765d9e16bd6ac99b76cd9b2dd8b162dcaeebbef3e2941f273b16fe9d2a51b162f5efc99f89c0100259797ab9fe60fe4596f6f6fd6d7d797eddab54b4a90702ec23959b264c9b6bc709d169f3700a0c4c295abf0401e3fc04b39d2d3d3b3392f58cbe3f306009458785ad095abf2269c9bbc60ed8ccf1b005062e1f53ef183ba942be11cc5e70d0028b1460bd68b2ff4646b1ef95eb66ae1f59584cfc3b2783b697d142c0018671a29583bb63e9b752ff872b6f237b3f74b5816d6c5db4b6ba36001c038d348c15ab7ea5735e5aac8fa55bfaed95e5a1b050b00c699460ad613f7df5453ac8a8475f1f6d2da28580030ce3452b0baefbbb6a6581509ebe2eda5b551b000609c51b0ca1f050b00c699460a56f8adc1b8581509ebe2eda5b551b000609c69a460ad7ee0db35c5aa4858176f2fad8d820500e34c23056bcbb37fcabaef9d5353aec2b2b02ede5e5a1b050b00c699460a56c8d37ffc414dc10acbe2eda4f551b000609c69a860eddc99ad7ef08e9a821596857535db4b4ba36001c038335cc10aefd4fee403dfaa295745c23aefe63eba51b000609c19b460eddc99f5ac5e9c3d3effea9a5215276c13b675356b74a26001c03853af600d77d56ab0b89a353a51b000609ca957b01ab96a3558c2d7c6fb93b428580030ced42b5871696a36f1fe242d0a16008c33f50a96942b0a160094c4942953eecf33395e1e53b0ca1f050b004a222f57597f862c5a0a56f9a3600140495415ac218b968255fe2858005012750a56dda2a560953f0a160094449d6215a752b414acf247c102e080ab531ca4892858e54f7ccea4b1c4b302802618a4f5c50f3655b97f8aa708c7555cc16a9eb90090c820ad6fb862553890052bbfbb4a0e3becb0ecc8238fcc4e39e51457d01a8882d53c73012091415adf70c5aa70200bcea4fe82d5d7d7972d5fbe3c3bf1c413b3430e39245bb06041cdb6f2cf2858cd3317001219a4f50d57ac0a6351b08adbf3e7cfafdc9e3c79f2c0b28d1b3766d3a74faf5ce10a99366d5a6559b17ecb962dd9c5175f9c1d77dc71d941071db4dffee2fd0f76bbc811471c919d74d249d909279c901d7ef8e1d9f1c71f9fcd9b37afe6b8c73a0a56f3cc05804406699ab12c589b366daadc7ee31bdf38b02c94abb0ec873ffc61f6fdef7fbff2795856ac9f31634665d9e5975f9e6ddfbebd66ff071f7cf0a0f757dcbeedb6db2a57d0aa6f3ffae8a395cf8f3df6d8fdf659862858cd3317001219a469c6b2603df7dc7395dbe14a55b12c94adb0ec85175ec8b66edd5ab3fea8a38eaa2cebededadbbfff094e360f757dc0efb7ee9a597066e87fba9b77d59a26035cf5c00486490a619cb8255ef29c2d482555cc10a5f1fdf5fb3b7cb1205ab79e60240228334cd5814aca15ee4dee85384575e7965cd5384c71c734c65dd430f3d94dd7efbed3585a999db43adab777b34a36035cf5c00486490a6198b8275e8a18756ae547df4a31fad799b86e245ee617dc8602f720faf958a5fe43e77eedcece8a38f1eb89f22f1fd37727ba875f56e8f6614ace6990b00890cd23471c191f245c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e602402283348d8255fe2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e60240228334cda2458bf6f5f5f5d53ca84b39929f9b0d79c1da199f3786662e00243248d32c5dba74436f6f6fcd03bb9423cf3cf3cc8ff382b53c3e6f0ccd5c00486490a659bc78f167962c59b2ada7a767b32b59e5497e2e7ad6ae5d7b775eaefe96e7b4f8bc313473012091419a2e3c8087ab24797685d7fb482912ce453827cad508980b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e30eab22c3b7ccd9a35f3962d5bb67bd1a245d97df7dd2725487e2ef62d5dba74c3e2c58b3f139f339a639002317381519797ab9fe60fe4596f6f6fd6d7d797eddab54b4a90702ec23959b264c9b6bc709d169f371a6790023173815117ae5c8507f2f8015eca919e9e9ecd79c15a1e9f371a67900231738151179e1674e5aabc09e7262f583be3f346e30c5220662e30eac2eb7de207752957c2398acf1b8d33488198b9c0a86bb460bdf8424fb6e691ef65ab165e5f49f83c2c8bb793d647c14a63900231738151d748c1dab1f5d9ac7bc197b395bf99bd5fc2b2b02ede5e5a1b052b8d410ac4cc05465d23056bddaa5fd594ab22eb57fdba667b696d14ac2427f70fd293e31540fb52b018758d14ac27eebfa9a6581509ebe2eda5b551b0462c94aade3c97f57f54b2800a058b51d748c1eabeefda9a625524ac8bb797d646c11a91a25c15a52abe0db43105ab8e8e8e8e43eeb8e38ef3bef9cd6ffefe9a6bae796ed6ac597f9f3973e69ef0cd0a1f2fbdf4d2bf87e537dd74d303b7de7aeb85f9f687c7fbe09f14acf247c16ada60656ab0e5409b51b0aadc72cb2dff7ac30d373c3a63c68cbd575f7d75f6b39ffd2c5bb97265b676edda6ccb962d59f0fcf3cf576effe94f7fcaeeb9e79e6cce9c39d939e79c13b65f75d5555719aa753452b0c26f0dc6c5aa4858176f2fad8d82d594e14ad470eb8136a060e5f29274ecf5d75fbfe4fcf3cfdffbd39ffeb452a29af1e28b2f66f7de7b6ff6f9cf7f7eefac59b3ba67ce9cf9dfe3fb68678d14acd50f7cbba6581509ebe2eda5b551b01ad668796a743b60826afb8275e38d377efac20b2fdc3577eedc6cfbf6ed71776acacb2fbf5c295af9fe767fee739fbbf9da6baf3d38bebf76d448c1daf2ec9fb2ee7be7d494abb02cac8bb797d646c16a48b3a5a9d9ed8109a4ad0bd60d37dc70ed05175cb077c58a1571574ab275ebd6f0d4e1aef3cf3fffc1aeaeae23e3fb6d378d14ac90a7fff8839a821596c5db49eba3600d6ba46569a45f078c736d5bb0befad5af5ef7852f7c61dfb3cf3e1bf7a396d8bb776f76d75d77bd7cf6d967af993a75eabfc4f7df4e1a2a583b7766ab1fbca3a6608565615dcdf6d2d22858434a2d49a95f0f8c436d59b0fa9f16dc3b5ae5aadadcb973777cf6b39f7de89c73ce79657c1ced62b88215dea9fdc907be5553ae8a8475decd7d74a3600daa55e5a855fb01c689b62b58d75c73cd71e13557ad7e5a7030fbf6edcb6eb9e596cd53a74e9d171f4bbb18b460eddc99f5ac5e9c3d3effea9a5215276c13b675356b74a260d5d5ea52d4eafd0125d676052bfcb6607841fb81145efc7ec105176cecececfc747c3ceda05ec11aeeaad56071356b74a260d588cbd04179ceeeffd88cf8ebe2fd0213545b15acf03e57e1ad18b66ddb1677a051b762c58a17bababa9e6ac73725ad57b01ab96a3558c2d7c6fb93b42858fb894b502847dfcd13be47e163a3256bb0af8bf70f4c406d55b0aebbeeba47c3fb5c8d952baeb8e2bff292f5a5f8b826ba7a052b2e4dcd26de9fa445c11a50affc842b50e1fb53a49192555dae8a9c55b5bedefd001348db14acf0e76f66cc98b1a7d937116da5eeeeee9efc1bfed749c30fe709a55ec1927245c1aa18acf4d42b4b4395ac7adb7fa77f79b5c1ee0f9800daa66085bf2d18fefccd580a2f789f3e7d7a6ffe4dff507c7c13998255fe4c948295ffdbba3fcfe4787903862b3bf54a53bd92556fbb7ae5aa30dcfd02e354db14ac9b6fbef90f63f9f460e1c61b6f7c28ffa65f1b1fdf78d4e883998255fe4ca08295f5a7a19fcd7e8d969c7ae5a9ba64d55b3f54b92a347affc038d23605ebcb5ffe72ef817a6b86a1e40f648f747676fe263ebef1a8d1073305abfc998005aba19fcd49cd979b7a252adc0e7f162b5ede48b92a347b1c40c9b54dc19a356bd6dfd7ae5d1bf79d03aebbbbfbcf53fef13aac71afd1073305abfc99c0056ba89fcd91969a7a25eb2fd1ed66ca5561a4c7039450db14ac0b2fbc704ff81b81636dd3a64d1bf36ffab6f8f8c6a33a0f62751fcc14acf2a70d0a56fcb3995a66ea95ac947255483d2ea024c2cc89974d48d3a64dabbce1e758dbbd7bf7cefc9bbe3b3ebef1a8ce83579cca83998255fed4397713361d1d1dd92b5ff9ca30f82e8b7fa69b149e168caf5c85db61798a705ca1641d11af00c68f306fe26513d2d9679fbdaf4457b06a86fe444e190bd6a4fe07c478f9784babfe3bdae00ad6fd53f67f8a30f54ad15057b0eafd7661a3528f0b2889307be26513d2cc99337795e13558ab56ad0aafc17a383ebef1a8ce8358dd07b39116ac49550f5aaf79cd6bb20f7ef083d91ffef0879aed469262bff1f256a7fabfa15ee2ed9b4dabf633810b565cacaa9d3c696465a65eb98aaf648da4648df47880126a9b8275e595576e7eecb1c7e2be73c02d5cb87079fe4dbf273ebef1a8d107b3d482153effd5af7e55f9fc3def794fcd76e325d5ff3dad4aabf639010b56dd9fc53a9a2d35f5ca5578cd55bddf226ca664357b1c40c9b54dc10aef3f75cf3df7c47de780bbe9a69b1ec8bfe973e2e31b8f1a7d306b45c1dab16347e5f357bdea5503eb376fde1cfe887676cc31c764af7ffdebb3d34f3f3d5bb3664dd6d3d3931d71c411d93bdef18e6ce7ce9d956dc3c7b7bffdedd9ab5ffdea6ce3c68dfbed7ba87d15eb8f3efae8eca8a38eaa7cfef5af7fbdf2b5dff8c6372ab7c3f2f075f1f1c789efb3debad7bef6b5d9873ef4a1fdaed4cd9b372f7be73bdf991d76d86135fba8befdc8238f646f7ef39bb3430f3d34bbf5d65b6bee63a84ca08235e4cfe2204e9ed458b919ac5c0df53e588d94ac46ef1f1847daa9605d3667ce9cb8ef1c70d3a74f5f376ddab4ff161fdf78d4e883592b0a567105eb539ffad4c0fa73cf3db7b2ece73fff79f6e0830f563effd8c73e565977d14517556efff297bfacdcfec52f7e51b93d73e6cc9a7d0fb7af904f7ce2139565ebd7afcfdefdee77573e7fd7bbde95ad5bb7aef2f9273ff9c99ae38f13df67bd3cfef8e3956ddefbdef70e2c0b052e94abdffef6b735db17fb9c3f7f7ef6bad7bd2e7bc31bde90fdee77bfabd96eb84c948295e0e44943979c7ae5a9de6f0bd6db6ea89235dcfd02e354db14ac8e8e8ed79f75d659fbc29590b1f2d4534f3d9b7fc39f8c8f6da24b2d5845def296b7644f3ffdf4c0fa238f3cb2669b57bce2159575ab56adca0e3ef8e04a310ab73ffef18f576efff9cf7fde6fdf8dec2b64f6ecd99565d75d775de563676767e5e357bef295cac72baeb8a2e6f8e3c4f7592494a30f7ce00395ab6bc536e1588bf5c71e7b6c65d95bdffad6ecd24b2fddefca5ab17d2860e1bfa1bbbbbb66ff8d44c1aa387952fdb253af34d52b57857adbd72b5983dd1f3001b44dc10aaebcf2ca27eebdf7deb8f71c305ffdea571fc9bfe137c4c735d1a516aceddbb7675ff8c2172a9f575f550a5776c2b2705529feda90d34e3b2d3be8a0832a57afc2c7f0b45fbcef46f775d75d7755d687a7f08e3beeb86cdbb66d958f4529bafbeebb6bbe264e7c9f45c27ec2f2071e78a0b2df78bb871f7e383be38c33069e227cdffbde57b3cf134e38a1f2f1c61b6facd97f2351b0069c3ca9b6f49c3da9fffbdc9fa1ca55a15ec93aab6a7dbdfb012690b62a585ffce2174fbbf8e28bf78ec5fb61ad5bb7eef9fc9bfddcd4a953ff253eae892eb56085cfc35b6c1457998aa7ca66cc9851b99d9fd74a3189bfbebf34547e03317c5cb87061dd7d37b2af7045acf89aebafbfbeb2acb89a15525c191b2af17d1629caddef7ffffbca6ba706db2ebc2e2b2c0fafb38af7b97af5eaca15aeeae36b260ad67e4e9eb47ff9a92e4b8d94abc2605f17ef1f9880daaa600579c1faeb585cc5fad297bef478fecdbe343e9e76d08a8215128a43b8fd918f7ca4727bcb962dd925975c921d7ffcf195a7d4e2ed43defffef75796858f43ed7bb87d8517c987ab57e169c3e22a57f8186e87e5c58be9874abccf223ffad18fb237bde94d03ebe3ed8adbe10a56f82dcaf07ab4785df8fca9a79ecadef6b6b7556e87a72ee3fb192a0a568d9327d596ac7005aad1725588bf2ede2f3041b55dc19a3973e6872fbcf0c2dd07f24d47172e5cf8d7fc1bbdbaa3a3e3f0f878dac1480b961cb8285875b5ba0cb57a7f4089b55dc10a2eb8e0823be6cc99b36befdebd71176ab9bffce52f9bbababa36757676fe6b7c1ced42c12a7f14ac41b5aa14b56a3fc038d19605ebd4534f7dc58c193356de75d75da3fa62ac4d9b36f54d9f3e7d635eae3e1d1f433b51b0ca1f056b48a9e528f5eb8171a82d0b56d0d1d171d459679df5ccdcb9735fdcb76f5fdc8d92852b5779b9eaedeaeafa7c7cdfed46c12a7f14ac618db4248df4eb8071ae6d0b569097ac37e525e82fb7dc72cbe63d7bf6c41d69c4c26baefa9f166ceb2b570505abfc51b01ad26c596a767b600269eb82159c71c619af9d3a75eacf2fb8e0828d2b56ac78212e4bcd086fc5d0ffdb82abdbf935573105abfc51b01ad668696a743b60826afb8255e8eaea9a9ea7f78a2baef8afc71f7fbc272e4f4309efd0feb5af7ded8fe17daef26235ab5d7f5b70300a56f9a3603565b8f234dc7aa00d2858553efbd9cfbe3a2f48b3f36fca9af0faa9f007a2f3079e47f2c2f5e7e79e7b6e432853e1637777f7aa850b172ebff9e69bff10feb660b86295e7fa767c13d1462858e58f82d5b4c14ad460cb8136a3600de2cc33cffc70fecdb936cfffed2f502f866f569ebfe55996e73ff2cc99287fb879342958e58f82352271998a6f036d4cc162d42958e58f82356245a9baacffa372055428588c3a05abfc51b0929cdc3f48952b608082c5a853b0ca1f052b8d410ac4cc05469d8255fe2858690c5220662e30ea14acf247c14a63900231738151a760953f0a561a831488990b8cba458b16edebebebab79509772243f371bf282b5333e6f34ce200562e602a36ee9d2a51b7a7b7b6b1ed8a51c79e699677e9c17ace5f179a371062910331718758b172ffecc92254bb6f5f4f46c7625ab3cc9cf45cfdab56befcecbd5dff29c169f371a67900231738103223c8087ab24797685d7fb482912ce453827ca5522831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e6024022831488990b00890c5220662e002432488198b90090c8200562e602402283344d966587af59b366deb265cb762f5ab428bbefbefba404c9cfc5bea54b976e58bc78f167e273c6fef219707f9803c3e4fef8eb0018828295262f573fcd1fc8b3dededeacafaf2fdbb56b979420e15c8473b264c9926d79e13a2d3e6ffc533e0326d729547126c75f07c01014ac34e1ca5578208f1fe0a51ce9e9e9d99c17ace5f179637fe10a559d52e5ea15c048295869c2d382ae5c9537e1dce4056b677cded85fb84255a758b97a0530520a569af07a9ff8415dca95708ee2f346ad70a5aa4eb972f50a602414ac348d16ac175fe8c9d63cf2bd6cd5c2eb2b099f8765f176d2fa28588d19e42ad6e4783b001aa060a569a460edd8fa6cd6bde0cbd9cadfccde2f615958176f2fad8d82d5b8e82a96ab570023a560a569a460ad5bf5ab9a725564fdaa5fd76c2fad8d82d5b8e82ad6e4783d000d52b0d23452b09eb8ffa69a625524ac8bb797d646c16a4e71152b5e0e40130cd2348d14aceefbaead295645c2ba787b696d14acc19d7aeaa9afe8ececfc445757d7ffce3fcecfe7c17fe6d9d47f056b7dbe6c45fe7141fef1ffe41f4f3bfdf4d38f88f701401d0a561a05abfc51b06ae585eadff3d2f4b3fcdfff8eaaa7041b495f7fe1f20ef9004351b0d23452b0c26f0dc6c5aa4858176f2fad8d82f54f79b13a31ff37ff8b3ac569240957bbce88ef0380490a56aa460ad6ea07be5d53ac8a8475f1f6d2da2858ff90ff5bbf3acf9eb828cd9a352bbbfbeebbb3c71e7b2c5bbb766db665cb962c78fef9e72bb757ae5c99fde4273fc9aebaeaaab86015b9b7a3a3e3a8f8fe00da9a8295a69182b5e5d93f65ddf7cea92957615958176f2fad4dbb17acbcfcbc2aff777e7775299a3a756a76fbedb7873f255429538ddaba756ba56c9d7beeb971c95a77e699677e38be6f80b6a560a569a460853cfdc71fd414acb02cde4e5a9f762e58fde56a597519bae1861bb275ebd6c5dda9293b76ecc8eebaebae4a51abdaf7eecececeb3e26300684b0a569a860ad6ce9dd9ea07efa8295861595857b3bdb434ed5cb0a64457aee6ce9d9bedddbb37ee4b23d6dddd9d5d74d145d5256b4f5eb2a6c4c701d07614ac34c315acf04eed4f3ef0ad9a725524acf36eeea39b762d585d5d5d575597ab050b16c4fda825c26bb5aeb8e28aea92f5529e7f8b8f07a0ad285869062d583b77663dab17678fcfbfbaa654c509db846d5dcd1a9db463c1eaff6dc18117b4872b57a32994ac4b2eb9a4ba643de53db380b6a660a5a957b086bb6a35585ccd1a9db463c19a52f5560ce13557ad7c5a7030ebd7afcfce3befbcea92754b7c5c006d43c14a53af603572d56ab084af8df7276969b782d5d9d9f9c1fcdff5bef06f3bbc083df505edcd983f7f7e75c1ea9b366ddaf1f1f101b405052b4dbd821597a66613ef4fd2d286052bbc437ba5e484b7623890f6ecd993cd9e3d7ba064757575fdcff8f800da828295a65ec19272a59d0a56f8db8279a9d95e149c0d1b36c41d68d43dfae8a3d557b1367574741c1e1f27c084a760d5977f5feecf33395e1e53b0ca9f762a5853a74efd64516e2ebbecb2b8fb1c10e12ad6f9e79f3f50b2ce3cf3cc53e2e30498f014acfaaafe0f7cc8a2a560953f13a5600df7b318e4eb6f2d7e76c31b818e953befbc73a060757676de141f27c084a760d55755b0862c5a0a56f933810ad6903f8b41befcb7c5762b56ac887bcf01b36cd9b2ea7f3b0be2e30498f0c2008c9751b760d57d7053b0ca9f0958b0eafe2cf66ff39fc5faf0879ac7caead5abab8ff3e9ea6304680b6100c6cba8fb6016a7f2e0a660953f13b860edf7b3d8bfcd866279f8c3cc6365cb962dd5c7b72dfa4f0198f8ea0c6b69220a56f9d30605abc8fd9d9d9d2f17b75f7ef9e5b8f71c30bb77efae3eaeddf17f0b006daace83d7c083d8144f118eabd439876d91125dc15a5ff54f0b8076163f584da9f37a97e04016acfcee2a39ecb0c3b2238f3c323be594535c416b206d70056be06733ffb8ae583e96afc17aeaa9a7aa8fefe1e83f05807655efc1ab9e03597026f517acbebebe6cf9f2e5d989279e981d72c821d982050b6ab6957f660217ac9a9fcdaeaeaeff57ac1fcbdf227cf8e187ab8ff39eea6304a08dd57bf0aa672c0a56717bfefcf995db93274f1e58b671e3c66cfaf4e9952b5c21d3a64dab2c2bd687a76e2ebef8e2ecb8e38ecb0e3ae8a0fdf617ef7fb0db458e38e288eca4934eca4e38e184ecf0c30fcf8e3ffef86cdebc7935c73dd69980056bd09fcd7cf91dc5763ff9c94fe2de73c07cf7bbdf1d285879e9fb7a7c9c0030a4b12c589b366daadc7ee31bdf38b02c94abb0ec873ffc61f6fdef7fbff2795856ac9f31634665d9e5975f9e6ddfbebd66ff071f7cf0a0f757dcbeedb6db2a57d0aa6f873f8f123e3ff6d863f7db671932810ad6a0c5aad0d9d939ad2836575d7555dc7b0e887dfbf6653367ceac2e58ff1e1f27000c692c0bd673cf3d57b91dae5415cb42d90acb5e78e185ca8b9ce3f5471d755465596f6f6fddfd87a71c07bbbfe276d8f74b2fbd34703bdc4fbdedcb928952b01ad1d1d1f1a6bcd4ece92f3663f242f7279e78a2fae9c1f002f783e2e30480218d65c1aaf714616ac12aae6085af8fefafd9db65493b15ac202f568bc7f269c21b6fbcb1ba607d3b3e3e0018d65814aca15ee4dee85384575e7965cd5384c71c734c65dd430f3d94dd7efbed3585a999db43adab777b34d36e05abb3b3734a5170ce3df7dc6cc78e1d71071a352b57aeac2e57bb3b3a3ade151f1f000c6b2c0ad6a1871e5ab952f5d18f7eb4e66d1a8a17b987f52183bdc83dbc562a7e91fbdcb973b3a38f3e7ae07e8ac4f7dfc8eda1d6d5bb3d9a69b7823579f2e443f372f37451740ed41f7d0e6f2e3a7bf6ec81829517bddbe263038086c40547ca97762b584157ae283a53a74ecdbabbbbe33ed472e1971daaae5e6debe8e8382a3e2e0068888255feb463c1ca1d34e51fbf7558293c175d7451f6fcf3cfc79da8657ef9cb5f5697abf002fb8be2030280862958e54f9b16ac4953a74e7d6b6767e7d6a2f484b7e6188d9215ca55f88d454f0d02d0320a56f9d3ae052bc8cbce27a6f4bf6d43c825975c92ad5fbf3eee4823125e73153d2d18b230bc062c3e0e00688a8255feb473c10abababaceae2e59e79d775ee52d3ef6ecd91377a68685df16ac7e417b51aece39e79c37c4f70f004d53b0ca9f762f5841ff5b37bc545d8842410aefc0df68d10aefd01ede44347a9fab4ac2d382ae5c01d0320a56f9a360fd435e84fe2dcf5371390aef8d76e79d7756deffecc9279fcc366fde5c2954e1e3ead5ab2b7fb8f93bdff9ce7e7ffea62adbbca01d809653b0ca1f05eb9f4e3ffdf423f24274735e8c76d6294bcd6477b86ae5ad180018150a56f9a360d59a366ddaf17949fa5f7936d5294f43657d7fb1f20eed008c1e05abfc51b00677eaa9a7be222f4dff232f4d37e51f7f9b674d9e17fbcbd4dff23c94e79e3cdfc8f3a1f8eb0160542858e58f820500e38c8255fe28580030ce2858e58f820500e38c8255fe28580030ce2858e58f820500e3cca2458bf6f5f5f5d53ca84b39929f9b0d79c1da199f3700a0c4962e5dbaa1b7b7b7e6815dca91679e79e6c779c15a1e9f3700a0c4162f5efc99254b966cebe9e9d9ec4a5679929f8b9eb56bd7de9d97abbfe5392d3e6f0040c98507f0709524cfaef07a1f2945c2b908e744b90200000000000000000000000000000000000000003810fe3fcb3727a59f8818ae0000000049454e44ae426082	t
14	1	1efaf552-46bc-49d6-8c6b-edb62e7e4261bpmn20.xml	13	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0a0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f7267223e0a0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469506172616c6c656c52657669657722206e616d653d22506172616c6c656c2052657669657720416e6420417070726f76652041637469766974692050726f63657373223e0a0a20202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c61637469766974693a657865637574696f6e4c697374656e6572206576656e743d2273746172742220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e6c697374656e65722e536372697074457865637574696f6e4c697374656e6572223e0a202020202020202009093c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f617070726f7665436f756e74272c2030293b0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f72656a656374436f756e74272c2030293b0a2020202020202020202020202020202020202020202020200a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c50657263656e74272c2030293b0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c52656a65637450657263656e74272c2030293b0a2020202020202020202020202020202020202020202020200a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f7265766965776572436f756e74272c2062706d5f61737369676e6565732e73697a652829293b0a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f726571756972656450657263656e74272c2077665f7265717569726564417070726f766550657263656e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020203c2f61637469766974693a657865637574696f6e4c697374656e65723e0a20202020202020203c2f657874656e73696f6e456c656d656e74733e0a20202020202020200a20202020202020203c73746172744576656e742069643d227374617274220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a7375626d6974506172616c6c656c5265766965775461736b22202f3e0a20202020202020200a0909090a20202020202020203c73657175656e6365466c6f772069643d27666c6f773127200a202020202020202020202020736f757263655265663d277374617274270a2020202020202020202020207461726765745265663d277265766965775461736b27202f3e0a0a20202020202020203c757365725461736b2069643d227265766965775461736b22206e616d653d22526576696577205461736b220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a61637469766974695265766965775461736b223e0a20202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a2020202020202020202020202020202020202020202020206966287461736b2e6765745661726961626c654c6f63616c282777665f7265766965774f7574636f6d652729203d3d2027417070726f76652729207b0a20202020202020202020202020202020202020202020202020202020766172206e6577417070726f766564436f756e74203d2077665f617070726f7665436f756e74202b20313b0a0920202020202020202020202020202020202020202020202020766172206e6577417070726f76656450657263656e74616765203d20286e6577417070726f766564436f756e74202f2077665f7265766965776572436f756e7429202a203130303b0a09202020202020202020202020202020202020202020202020200a0920202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f617070726f7665436f756e74272c206e6577417070726f766564436f756e74293b0a0920202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c50657263656e74272c206e6577417070726f76656450657263656e74616765293b0a2020202020202020202020202020202020202020202020207d20656c7365207b0a20202020202020202020202020202020202020202020202020202020766172206e657752656a656374436f756e74203d2077665f72656a656374436f756e74202b20313b0a20202020202020202020202020202020202020202020202020202020766172206e657752656a65637450657263656e74616765203d20286e657752656a656374436f756e74202f2077665f7265766965776572436f756e7429202a203130303b0a202020202020202020202020202020202020202020202020202020200a20202020202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f72656a656374436f756e74272c206e657752656a656374436f756e74293b0a20202020202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c52656a65637450657263656e74272c206e657752656a65637450657263656e74616765293b0a2020202020202020202020202020202020202020202020207d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a20202020202020202020200a20202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b72657669657741737369676e65652e70726f706572746965732e757365724e616d657d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020202020200a20202020202020202020203c212d2d20466f7220656163682061737369676e65652c207461736b2069732063726561746564202d2d3e0a20202020202020202020203c6d756c7469496e7374616e63654c6f6f7043686172616374657269737469637320697353657175656e7469616c3d2266616c7365223e0a20202020202020202020200920203c6c6f6f7044617461496e7075745265663e62706d5f61737369676e6565733c2f6c6f6f7044617461496e7075745265663e0a20202020202020202020200920203c696e707574446174614974656d206e616d653d2272657669657741737369676e656522202f3e0a20202020202020202020200920203c636f6d706c6574696f6e436f6e646974696f6e3e247b77665f61637475616c50657263656e74203e3d2077665f7265717569726564417070726f766550657263656e74207c7c2077665f7265717569726564417070726f766550657263656e74203e2028313030202d2077665f61637475616c52656a65637450657263656e74297d3c2f636f6d706c6574696f6e436f6e646974696f6e3e0a20202020202020202020203c2f6d756c7469496e7374616e63654c6f6f704368617261637465726973746963733e0a20202020202020203c2f757365725461736b3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773227200a202020202020202009736f757263655265663d277265766965775461736b270a2020202020202020202020207461726765745265663d277265766965774465636973696f6e27202f3e0a0a20202020202020203c6578636c757369766547617465776179202069643d227265766965774465636973696f6e22206e616d653d22526576696577204465636973696f6e22202f3e200a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77332720736f757263655265663d277265766965774465636973696f6e27207461726765745265663d27617070726f76656427203e0a2020202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b77665f61637475616c50657263656e74203e3d2077665f7265717569726564417070726f766550657263656e747d3c2f636f6e646974696f6e45787072657373696f6e3e0a20202020202020203c2f73657175656e6365466c6f773e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773427200a2020202020202020736f757263655265663d277265766965774465636973696f6e270a2020202020202020202020207461726765745265663d2772656a656374656427202f3e0a0a202020202020203c757365725461736b2069643d22617070726f76656422206e616d653d22446f63756d656e7420417070726f766564220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a617070726f766564506172616c6c656c5461736b22203e0a2020202020202020202020203c646f63756d656e746174696f6e3e0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e6420617070726f7665642e0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020202020200a2020202020202020202020202020202020202020202020202f2f2053657420706172616c6c656c2072657669657720706172616d73206f6e207461736b2c20746f206265206b65707420696e20686973746f72790a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f7265766965776572436f756e74272c2077665f7265766965776572436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f726571756972656450657263656e74272c2077665f726571756972656450657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c50657263656e74272c2077665f61637475616c50657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f617070726f7665436f756e74272c2077665f617070726f7665436f756e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a2020202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020203c2f757365725461736b3e0a20202020202020200a20202020202020203c757365725461736b2069643d2272656a656374656422206e616d653d22446f63756d656e742052656a6563746564220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a72656a6563746564506172616c6c656c5461736b22203e0a2020202020202020202020203c646f63756d656e746174696f6e3e0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e642072656a65637465642e0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020202020200a2020202020202020202020202020202020202020202020202f2f2053657420706172616c6c656c2072657669657720706172616d73206f6e207461736b2c20746f206265206b65707420696e20686973746f72790a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f7265766965776572436f756e74272c2077665f7265766965776572436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f726571756972656450657263656e74272c2077665f726571756972656450657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c50657263656e74272c2077665f61637475616c50657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f617070726f7665436f756e74272c2077665f617070726f7665436f756e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a2020202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020203c2f757365725461736b3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77352720736f757263655265663d27617070726f766564270a2020202020202020202020207461726765745265663d27656e6427202f3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77362720736f757263655265663d2772656a6563746564270a2020202020202020202020207461726765745265663d27656e6427202f3e0a0a20202020202020203c656e644576656e742069643d22656e6422202f3e0a0a202020203c2f70726f636573733e0a202020200a2020203c212d2d2047726170686963616c20726570726573656e7461696f6e206f66206469616772616d202d2d3e0a2020203c62706d6e64693a42504d4e4469616772616d2069643d2242504d4e4469616772616d5f6163746976697469506172616c6c656c526576696577223e0a2020202020203c62706d6e64693a42504d4e506c616e652062706d6e456c656d656e743d226163746976697469506172616c6c656c526576696577220a20202020202020202069643d2242504d4e506c616e655f6163746976697469506172616c6c656c526576696577223e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227374617274220a20202020202020202020202069643d2242504d4e53686170655f7374617274223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d2233302220793d22323030223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965775461736b220a20202020202020202020202069643d2242504d4e53686170655f7265766965775461736b223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22313235220a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965774465636973696f6e220a20202020202020202020202069643d2242504d4e53686170655f7265766965774465636973696f6e223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223430222077696474683d2234302220783d223239302220793d22313937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22617070726f766564220a20202020202020202020202069643d2242504d4e53686170655f617070726f766564223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220a202020202020202020202020202020793d223937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d2272656a6563746564220a20202020202020202020202069643d2242504d4e53686170655f72656a6563746564223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220a202020202020202020202020202020793d22323937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22656e64222069643d2242504d4e53686170655f656e64223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d223535352220793d22333037223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7731222069643d2242504d4e456467655f666c6f7731223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d2236352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223132352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7732222069643d2242504d4e456467655f666c6f7732223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223233302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223239302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7733222069643d2242504d4e456467655f666c6f7733223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313937223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7734222069643d2242504d4e456467655f666c6f7734223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22323337223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7735222069643d2242504d4e456467655f666c6f7735223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22333037223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7736222069643d2242504d4e456467655f666c6f7736223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223535352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020203c2f62706d6e64693a42504d4e506c616e653e0a2020203c2f62706d6e64693a42504d4e4469616772616d3e0a0a3c2f646566696e6974696f6e733e	f
15	1	1efaf552-46bc-49d6-8c6b-edb62e7e4261activitiParallelReview.png	13	\\x89504e470d0a1a0a0000000d49484452000002580000016a0806000000c63c24e70000226749444154785eeddd7d905c659d2ff0f0aae2baba252f0a889656f9c6de2a5fb66eb95a5bc4bda8c522f8d7309924f2165ea2609544222f961b5e5c14d87beb7257c03fb4345aa04617dfae9a401272a31008b23132919560082664322421e46599242439f73ced9cb1f374cf4cf73c3d99d3d39f4fd5b766fa9c33a70f7326bffe72baa767ca140000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080769765d9b1ebd6ad5bb062c58a7d4b962cc91e78e0012941f2737170f9f2e59b962e5dfa89f89c0100259797ab1fe40fe4597f7f7f36303090edddbb574a90702ec23959b66cd98ebc709d1d9f3700a0c4c295abf0401e3fc04b39d2d7d7b7352f582be3f306009458785ad095abf2269c9bbc60ed89cf1b005062e1f53ef183ba942be11cc5e70d0028b1460bd6ee17fbb2758f7d335bb3f8a64ac2e76159bc9db43e0a1600b499460ad6aeedcf65bd8bbe98adfef9dc4312968575f1f6d2da285800d0661a29581bd6fcb4a65c15d9b8e66735db4b6ba36001409b69a4603df9e0ad35c5aa4858176f2fad8d8205006da69182d5fbc00d35c5aa4858176f2fad8d8205006d46c12a7f142c0068338d14acf05b8371b12a12d6c5db4b6ba36001409b69a460ad7de8ab35c5aa4858176f2fad8d8205006da69182b5edb9df65bdf7cfab2957615958176f2fad8d8205006da6918215f2cc6fbe5d53b0c2b2783b697d142c0068330d15ac3d7bb2b50fdf5d53b0c2b2b0ae667b6969142c006833a315acf04eed4f3df46f35e5aa4858e7dddcc7370a1600b499610bd69e3d59dfdaa5d9130bafaf295571c236615b57b3c6270a1600b4997a056bb4ab56c3c5d5acf1898205006da65ec16ae4aad570095f1bef4fd2a26001409ba957b0e2d2d46ce2fd495a142c006833f50a96942b0a160094c4b469d31ecc33355e1e53b0ca1f050b004a222f57d960462c5a0a56f9a3600140495415ac118b968255fe2858005012750a56dda2a560953f0a160094449d6215a752b414acf247c102e0b0ab531ca4892858e54f7ccea4b1c4b302802618a4f5c50f365579709aa708db2aae6035cf5c00486490d6375ab12a1cce8295df5d25c71c734c76fcf1c767679e79a62b680d44c16a9eb90090c820ad6fb46255389c0567ca60c11a1818c856ae5c999d7efae9d951471d952d5ab4a8665bf94b14ace6990b00890cd2fa462b5685892858c5ed850b17566e4f9d3a7568d9e6cd9bb399336756ae7085cc9831a3b2ac58bf6ddbb6ec8a2baec84e39e594ec88238e38647ff1fe87bb5de4b8e38ecbdefbdef766a79d765a76ecb1c766a79e7a6ab660c1829ae39ee82858cd3317001219a46926b2606dd9b2a572fbf5af7ffdd0b250aec2b2ef7ce73bd9b7bef5adcae76159b17ed6ac5995659ffffce7b39d3b77d6ecffc8238f1cf6fe8adb77de7967e50a5af5edc71f7fbcf2f9c9279f7cc83ecb1005ab79e60240228334cd4416ace79f7fbe723b5ca92a9685b21596bdf8e28bd9f6eddb6bd69f70c2099565fdfdfd75f71f9e721ceefe8adb61df2fbdf4d2d0ed703ff5b62f4b14ace6990b00890cd2341359b0ea3d45985ab08a2b58e1ebe3fb6bf67659a26035cf5c00486490a699888235d28bdc1b7d8af0da6bafad798af0a4934eaaac7be49147b2bbeebaaba63035737ba475f56e8f6714ace6990b00890cd2341351b08e3efae8ca95aa0f7ff8c3356fd350bcc83dac0f19ee45eee1b552f18bdce7cf9f9f9d78e28943f75324beff466e8fb4aedeedf18c82d53c73012091419a262e3852be2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e602402283348d8255fe2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a659b264c9c18181819a07752947f273b3292f587be2f3c6c8cc05804406699ae5cb976feaefefaf79609772e4d9679ffd5e5eb056c6e78d91990b00890cd2344b972efdc4b265cb76f4f5f56d7525ab3cc9cf45dffaf5ebefcdcbd59ff29c1d9f3746662e00243248d38507f0709524cfdef07a1f2945c2b908e744b91a0373012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc05c65d9665c7ae5bb76ec18a152bf62d59b2247be08107a404c9cfc5c1e5cb976f5aba74e927e27346730c5220662e30eef272f583fc813cebefefcf060606b2bd7bf74a0912ce453827cb962ddb9117aeb3e3f346e30c5220662e30eec295abf0401e3fc04b39d2d7d7b7352f582be3f346e30c5220662e30eec2d382ae5c9537e1dce4056b4f7cde689c410ac4cc05c65d78bd4ffca02ee54a3847f179a371062910331718778d16acdd2ff665eb1efb66b666f14d9584cfc3b2783b697d14ac34062910331718778d14ac5ddb9fcb7a177d315bfdf3b987242c0bebe2eda5b551b0d218a440cc5c60dc3552b036acf9694db92ab271cdcf6ab697d646c14a72c6e0203d235e01742e058b71d748c17af2c15b6b8a5591b02ede5e5a1b056bcc42a9eacf73f5e047250ba850b018778d14acde076ea8295645c2ba787b696d14ac3129ca5551aae2db400753b0eae8eaea3aeaeebbefbef85ffff55f7ff5852f7ce1f93973e6fcd7ecd9b3f7876f56f878d55557fd57587eebadb73e74c71d775c966f7f6cbc0ffe42c12a7f14aca60d57a6865b0e741805abcaedb7dffeb737df7cf3e3b366cd3a70fdf5d7673ffce10fb3d5ab5767ebd7afcfb66ddb96052fbcf042e5f6ef7ef7bbecbefbeecbe6cd9b975d78e18561fb35d75d779da15a4723052bfcd6605cac8a8475f1f6d2da28584d19ad448db61ee8000a562e2f4927df74d34dcb2eb9e492033ff8c10f2a25aa19bb77efceeebffffeecd39ffef4813973e6f4ce9e3dfbbfc7f7d1c91a29586b1ffa6a4db12a12d6c5db4b6ba36035acd1f2d4e876c024d5f105eb965b6ef9f865975db677fefcf9d9ce9d3be3eed494975f7eb952b4f2fdedfbd4a73e75db0d37dc70647c7f9da89182b5edb9df65bdf7cfab2957615958176f2fad8d82d590664b53b3db0393484717ac9b6fbef9864b2fbdf4c0aa55abe2ae9464fbf6ede1a9c3bd975c72c9c33d3d3dc7c7f7db691a295821cffce6db35052b2c8bb793d647c11ad558cbd258bf0e68731d5bb0bef4a52fddf899cf7ce6e073cf3d17f7a3963870e04076cf3df7bc7cc10517ac9b3e7dfadfc4f7df491a2a587bf6646b1fbebba6608565615dcdf6d2d22858234a2d49a95f0fb4a18e2c58834f0b1e18af72556dfefcf9bb3ef9c94f3e72e18517be323e8e4e315ac10aefd4fed443ff5653ae8a8475decd7d7ca3600dab55e5a855fb01da44c715ac2f7ce10ba784d75cb5fa69c1e11c3c7830bbfdf6dbb74e9f3e7d417c2c9d62d882b5674fd6b77669f6c4c2eb6b4a559cb04dd8d6d5acf189825557ab4b51abf7079458c715acf0db82e105ed875378f1fba5975ebab9bbbbfbe3f1f174827a056bb4ab56c3c5d5acf1898255232e4347e4b960f06333e2af8bf70b4c521d55b0c2fb5c85b762d8b16347dc81c6ddaa55ab5eece9e979ba13df94b45ec16ae4aad570095f1bef4fd2a2601d222e41a11c7d234ff81e858f8d96ace1be2ede3f30097554c1baf1c61b1f0fef733551aeb9e69affcc4bd6e7e2e39aecea15acb834359b787f9216056b48bdf213ae4085ef4f91464a5675b92a727ed5fa7af7034c221d53b0c29fbf99356bd6fe66df44b4957a7b7bfbf26ff81fa78c3e9c27957a054bca1505ab62b8d253af2c8d54b2ea6dfff5c1e5d586bb3f6012e8988215feb660f8f3371329bce07de6cc99fdf937fd83f1f14d660a56f933590a56fe6febc13c53e3e50d18adecd42b4df54a56bdedea95abc268f70bb4a98e2958b7dd76dbaf27f2e9c1c22db7dcf248fe4dbf213ebe76d4e883998255fe4ca282950da6a19fcd418d969c7ae5a9ba64d55b3f52b92a347aff401be99882f5c52f7eb1ff70bd35c348f207b2c7babbbb7f1e1f5f3b6af4c14cc12a7f2661c16ae867734af3e5a65e890ab7c39fc58a973752ae0acd1e0750721d53b0e6cc99f35febd7af8ffbce61d7dbdbfbfb697f7e1d56db6bf4c14cc12a7f2671c11ae96773aca5a65ec9fa4374bb99725518ebf10025d43105ebb2cb2edb1ffe46e044dbb265cbe6fc9bbe233ebe7654e741acee83998255fe7440c18a7f3653cb4cbd929552ae0aa9c70594449839f1b24969c68c199537fc9c68fbf6eddb937fd3f7c5c7d78eea3c78c5a93c982958e54f9d733769d3d5d595bdf295af0c83efeaf867ba49e169c1f8ca55b81d96a708c7154ad671f10aa07d8479132f9b942eb8e0828325ba825533f42773ca58b0a60c3e20c6cbdb2dadfaefe8802b580f4e3bf429c2d42b45235dc1aaf7db858d4a3d2ea024c2ec89974d4ab367cfde5b86d760ad59b326bc06ebd1f8f8da519d07b1ba0f66632d5853aa1eb4feeaaffe2afbc0073e90fdfad7bfaed96e2c29f61b2f6f75aaff1bea25debed9b46a3f93b860c5c5aada1953c65666ea95abf84ad6584ad6588f0728a18e2958d75e7bedd6dffef6b771df39ec162f5ebc32ffa6df171f5f3b6af4c12cb56085cf7ffad39f563e7fd7bbde55b35dbba4fabfa75569d53e2761c1aafbb35847b3a5a65eb90aafb9aaf75b84cd94ac668f0328b98e2958e1fda7eebbefbeb8ef1c76b7de7aeb43f9377d5e7c7ceda8d107b35614ac5dbb76553e7fd5ab5e35b47eebd6ade18f6867279d7452f6dad7be363be79c73b275ebd6657d7d7dd971c71d97bded6d6fcbf6ecd953d9367c7ceb5bdf9abdfad5afce366fde7cc8be47da57b1fec4134fcc4e38e184cae75ffef2972b5ffb95af7ca5723b2c0f5f171f7f9cf83eebad7bcd6b5e937df0831f3ce44add82050bb2b7bffdedd931c71c53b38feadb8f3df658f6c637be313bfae8a3b33beeb8a3e63e46ca242a5823fe2c0ee38c298d959be1cad548ef83d548c96af4fe8136d24905ebea79f3e6c57de7b09b3973e686193366fcb7f8f8da51a30f66ad2858c515ac7ffaa77f1a5a7fd145175596fde8473fca1e7ef8e1cae7fff88fff585977f9e597576effe4273fa9dcfef18f7f5cb93d7bf6ec9a7d8fb6af908f7ef4a395651b376eccdef9ce77563e7fc73bde916dd8b0a1f2f9c73ef6b19ae38f13df67bd3cf1c413956ddefdee770f2d0b052e94ab5ffce21735db17fb5cb87061f6d77ffdd7d9eb5ef7baec97bffc65cd76a365b214ac04674c19b9e4d42b4ff57e5bb0de762395acd1ee1768531d53b0bababa5e7bfef9e71f0c574226cad34f3ffd5cfe0d7f2a3eb6c92eb5601579d39bde943df3cc3343eb8f3ffef89a6d5ef18a5754d6ad59b3263bf2c8232bc528dcfec8473e52b9fdfbdffffe907d37b2af90b973e75696dd78e38d958fdddddd958ffffccfff5cf978cd35d7d41c7f9cf83e8b8472f4fef7bfbf7275add8261c6bb1fee4934fae2c7bf39bdf9c5d75d555875c592bb60f052cfc37f4f6f6d6ecbf91285815674ca95f76ea95a67ae5aa506ffb7a256bb8fb0326818e2958c1b5d75efbe4fdf7df1ff79ec3e64b5ffad263f937fce6f8b826bbd482b573e7ceec339ff94ce5f3eaab4ae1ca4e5816ae2ac55f1b72f6d96767471c7144e5ea55f8189ef68bf7dde8beeeb9e79ecafaf014de29a79c92edd8b1a3f2b12845f7de7b6fcdd7c489efb348d84f58fed0430f55f61b6ff7e8a38f66e79e7beed05384ef79cf7b6af679da69a7553ede72cb2d35fb6f240ad69033a6d4969e0ba60c7e9f073352b92ad42b59e757adaf773fc024d25105ebb39ffdecd9575c71c58189783fac0d1b36bc907fb39f9f3e7dfadfc4c735d9a516acf079788b8de22a53f154d9ac59b32ab7f3f35a2926f1d70f9686ca6f20868f8b172faebbef46f615ae88155f73d34d3755961557b3428a2b632325becf2245b9fbd5af7e5579edd470db85d76585e5e17556f13ed7ae5d5bb9c2557d7ccd44c13ac419530e2d3fd565a991725518eeebe2fd0393504715ac202f587f9c88ab589ffbdce79ec8bfd957c5c7d3095a51b042427108b7ffe11ffea1727bdbb66dd995575e999d7aeaa995a7d4e2ed43def7bef75596858f23ed7bb47d8517c987ab57e169c3e22a57f8186e87e5c58be9474abccf22dffdee77b337bce10d43ebe3ed8adbe10a56f82dcaf07ab4785df8fce9a79fcedef296b7546e87a72ee3fb19290a568d33a6d496ac7005aad1725588bf2ede2f3049755cc19a3d7bf6872ebbecb27d87f34d47172f5efcc7fc1bbdb6ababebd8f8783ac1580b961cbe285875b5ba0cb57a7f4089755cc10a2ebdf4d2bbe7cd9bb7f7c0810371176ab93ffce10f5b7a7a7ab6747777ff6d7c1c9d42c12a7f14ac61b5aa14b56a3f409be8c88275d65967bd62d6ac59abefb9e79e717d31d6962d5b0666ce9cb9392f571f8f8fa193b463c1dabd7b77e537f2c2c794e5ed12056b44a9e528f5eb8136d491052be8eaea3ae1fcf3cf7f76fefcf9bb0f1e3c1877a364e1ca555eaefa7b7a7a3e1ddf77a769c78215ca527ee8356f7bd0ecf276898235aab196a4b17e1dd0e63ab6600579c97a435e82fe70fbedb76fddbf7f7fdc91c62cbce66af069c18ebe725550b0ca1f05ab21cd96a566b70726918e2e58c1b9e79efb9ae9d3a7ffe8d24b2fddbc6ad5aa17e3b2d48cf0560c83bf2db8b6935f731553b0ca1f05ab618d96a646b70326a98e2f58859e9e9e9979faafb9e69aff7ce28927fae2f23492f00eedfff22ffff29bf03e5779b19ad3a9bf2d381c05abfc51b09a325a791a6d3dd00114ac2a9ffce4275f9d17a4b9f937655d78fd54f803d1f903cf6379e1fafdf3cf3fbf2994a9f0317f105db378f1e295b7dd76dbafc3df160c57acf2dcd4896f22da0805abfc51b09a365c891a6e39d06114ac619c77de791fcabf3937e4f9bf83056a77f866e5f9539e1579fe3dcfbcc9f2879bc7938255fe2858631297a9f836d0c1142cc69d8255fe2858635694aaab073f2a57408582c5b853b0ca1f052bc919838354b9028628588c3b05abfc51b0d218a440cc5c60dc2958e58f8295c6200562e602e34ec12a7f14ac34062910331718770a56f9a360a531488198b9c0b85bb264c9c18181819a07f53267b8c2d4ecf276487e6e36e5056b4f74da6882410ac4cc05c6ddf2e5cb37f5f7f7d73cb09739bb77efae94a5f03165793be4d9679ffd5e5eb056c6e78dc619a440cc5c60dc2d5dbaf413cb962ddbd1d7d7b7b5ddae644de6e4e7a26ffdfaf5f7e6e5ea4f79ce8ecf1b8d33488198b9c061111ec0c355923c7bc3eb7da41409e7229c13e52a91410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091419a26cbb263d7ad5bb760c58a15fb962c59923df0c0035282e4e7e2e0f2e5cb372d5dbaf413f139e350f90c7830cc8151f260fc75008c40c14a9397ab1fe40fe4597f7f7f36303090edddbb574a90702ec23959b66cd98ebc709d1d9f37fe229f0153eb14aa3853e3af0360040a569a70e52a3c90c70ff0528ef4f5f56dcd0bd6caf8bc71a87085aa4ea972f50a60ac14ac34e1694157aeca9b706ef282b5273e6f1c2a5ca1aa53ac5cbd02182b052b4d78bd4ffca02ee54a3847f179a356b85255a75cb97a0530160a569a460bd6ee17fbb2758f7d335bb3f8a64ac2e76159bc9db43e0a566386b98a3535de0e80062858691a2958bbb63f97f52efa62b6fae7730f495816d6c5db4b6ba360352eba8ae5ea15c0582958691a29581bd6fcb4a65c15d9b8e66735db4b6ba360352eba8a35355e0f408314ac348d14ac271fbcb5a6581509ebe2eda5b551b09a535cc58a9703d00483344d2305abf7811b6a8a5591b02ede5e5a1b056b78679d75d62bbabbbb3fdad3d3f3bff38f0bf379f01f79b60c5ec1da982f5b957f5c947ffc3ff9c7b3cf39e79ce3e27d005087829546c12a7f14ac5a79a1fafbbc34fd30fff7bfabea29c146323058b8bc433ec04814ac348d14acf05b8371b12a12d6c5db4b6ba360fd455eac4ecfffcdffb84e711a4bc2d5ae73e3fb00608a8295aa9182b5f6a1afd614ab22615dbcbdb4360ad69fe5ffd6afcfb33f2e4a73e6ccc9eebdf7deecb7bffd6db67efdfa6cdbb66d59f0c20b2f546eaf5ebd3afbfef7bf9f5d77dd7571c12a727f5757d709f1fd017434052b4d23056bdb73bfcb7aef9f5753aec2b2b02ede5e5a9b4e2f5879f97955feeffcdeea52347dfaf4ecaebbee0a7f4aa852a61ab57dfbf64ad9bae8a28be292b5e1bcf3cefb507cdf001d4bc14ad348c10a79e637dfae29586159bc9db43e9d5cb006cbd58aea3274f3cd37671b366c88bb535376edda95dd73cf3d95a256b5ef7ddddddde7c7c700d09114ac340d15ac3d7bb2b50fdf5d53b0c2b2b0ae667b69693ab9604d8bae5ccd9f3f3f3b70e040dc97c6acb7b737bbfcf2cbab4bd6febc644d8b8f03a0e3285869462b58e19dda9f7ae8df6aca5591b0cebbb98f6f3ab560f5f4f45c575dae162d5a14f7a39608afd5bae69a6baa4bd64b79fe2e3e1e808ea260a519b660edd993f5ad5d9a3db1f0fa9a5215276c13b675356b7cd289056bf0b705875ed01eae5c8da750b2aebcf2caea92f5b4f7cc023a9a8295a65ec11aedaad57071356b7cd289056b5ad55b3184d75cb5f269c1e16cdcb831bbf8e28bab4bd6edf17101740c052b4dbd82d5c855abe112be36de9fa4a5d30a567777f707f27fd707c3bfedf022f4d417b43763e1c285d5056b60c68c19a7c6c707d01114ac34f50a565c9a9a4dbc3f494b0716acf00eed959213de8ae170dabf7f7f3677eedca192d5d3d3f33fe3e303e8080a569a7a054bca954e2a58e16f0be6a566675170366dda1477a071f7f8e38f575fc5dad2d5d5756c7c9c00939e82555ffe7d7930cfd478794cc12a7f3aa9604d9f3efd6345b9b9faeaabe3ee735884ab58975c72c950c93aefbcf3ce8c8f1360d253b0eaabfa3ff0118b968255fe4c968235dacf6290afbfa3f8d90d6f043a51bef6b5af0d15aceeeeee5be3e30498f414acfaaa0ad688454bc12a7f2651c11af16731c897ffa2d86ed5aa5571ef396c56ac5851fd6f67517c9c00935e1880f132ea16acba0f6e0a56f933090b56dd9fc5c16dfea3581ffe50f34459bb766df5713e537d8c001d210cc07819751fcce2541edc14acf2671217ac437e1607b7d9542c0f7f9879a26cdbb6adfaf87644ff2900935f9d612d4d44c12a7f3aa0601579b0bbbbfbe5e2f6cb2fbf1cf79ec366dfbe7dd5c7b52ffe6f01a043d579f01a7a109be629c2b64a9d73d81129d115ac8d55ffb400e864f183d5b43aaf77090e67c1caefae92638e39263bfef8e3b333cf3cd315b406d20157b0867e36f38f1b8ae513f91aaca79f7ebafaf81e8dfe5300e854f51ebcea399c0567ca60c11a1818c856ae5c999d7efae9d951471d952d5ab4a8665bf94b2671c1aaf9d9ece9e9f97fc5fa89fc2dc2471f7db4fa38efab3e46003a58bd07af7a26a26015b7172e5c58b93d75ead4a1659b376fce66ce9c59b9c2153263c68ccab2627d78eae68a2baec84e39e594ec88238e38647ff1fe87bb5de4b8e38ecbdefbdef766a79d765a76ecb1c766a79e7a6ab660c1829ae39ee84cc28235eccf66befcee62bbef7ffffb71ef396cbef18d6f0c15acbcf47d393e4e0018d14416ac2d5bb6546ebffef5af1f5a16ca5558f69def7c27fbd6b7be55f93c2c2bd6cf9a35abb2ecf39fff7cb673e7ce9afd1f79e491c3de5f71fbce3befac5c41abbe1dfe3c4af8fce4934f3e649f65c8242a58c316ab427777f78ca2d85c77dd7571ef392c0e1e3c98cd9e3dbbba60fd7d7c9c0030a2892c58cf3fff7ce576b852552c0b652b2c7bf1c5172b2f728ed79f70c2099565fdfdfd75f71f9e721ceefe8adb61df2fbdf4d2d0ed703ff5b62f4b264bc16a445757d71bf252b37fb0d84cc80bdd9f7cf2c9eaa707c30bdc8f888f130046349105abde5384a905abb88215be3ebebf666f97259d54b082bc582d9dc8a7096fb9e596ea82f5d5f8f80060541351b0467a917ba34f115e7bedb5354f119e74d24995758f3cf24876d75d77d514a6666e8fb4aedeedf14ca715aceeeeee6945c1b9e8a28bb25dbb76c51d68dcac5ebdbaba5cedebeaea7a477c7c0030aa892858471f7d74e54ad5873ffce19ab769285ee41ed6870cf722f7f05aa9f845eef3e7cfcf4e3cf1c4a1fb2912df7f23b7475a57eff678a6d30ad6d4a9538fcecbcd3345d1395c7ff439bcb9e8dcb973870a565ef4ee8c8f0d001a12171c295f3aad60053db9a2e84c9f3e3debeded8dfb50cb855f76a8ba7ab5a3ababeb84f8b800a0210a56f9d389052b77c4b43fffd661a5f05c7ef9e5d90b2fbc1077a296f9c94f7e525daec20bec2f8f0f08001aa660953f1d5ab0a64c9f3efdcddddddddb8bd213de9a633c4a562857e137163d350840cb2858e54fa716ac202f3b1f9d36f8b60d21575e7965b671e3c6b8238d4978cd55f4b460c8e2f01ab0f83800a0290a56f9d3c9052be8e9e9b9a0ba645d7cf1c595b7f8d8bf7f7fdc991a167e5bb0fa05ed45b9baf0c20b5f17df3f00344dc12a7f3abd6005836fddf05275210a0529bc037fa3452bbc437b7813d1e87dae2a094f0bba720540cb2858e58f82f5677911fabb3c4fc7e528bc37dad7bef6b5cafb9f3df5d453d9d6ad5b2b852a7c5cbb766de50f377ffdeb5f3fe4cfdf54658717b403d0720a56f9a360fdc539e79c735c5e886ecb8bd19e3a65a999ec0b57adbc150300e342c12a7f14ac5a3366cc38352f49ff2bcf963ae569a46c1c2c56dea11d80f1a360953f0ad6f0ce3aebac57e4a5e97fe4a5e9d6fce32ff2accbb37bb04cfd29cf2379eecbf3953c1f8cbf1e00c6858255fe285800d06614acf247c1028036a360953f0a1600b41905abfc51b000a0cd2858e58f8205006d66c99225070706066a1ed4a51cc9cfcda6bc60ed89cf1b005062cb972fdfd4dfdf5ff3c02ee5c8b3cf3efbbdbc60ad8ccf1b0050624b972efdc4b265cb76f4f5f56d7525ab3cc9cf45dffaf5ebefcdcbd59ff29c1d9f3700a0e4c20378b84a92676f78bd8f9422e15c8473a25c0100000000000000000000000000000000000000001c0eff1f474890a9b43061420000000049454e44ae426082	t
18	1	4102fd9f-2bed-4d37-b46c-9ce37d819e74bpmn20.xml	17	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0a0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f7267223e0a0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469506172616c6c656c47726f757052657669657722206e616d653d22506172616c6c656c2047726f75702052657669657720416e6420417070726f76652041637469766974692050726f63657373223e0a0a20202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c61637469766974693a657865637574696f6e4c697374656e6572206576656e743d2273746172742220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e6c697374656e65722e536372697074457865637574696f6e4c697374656e6572223e0a202020202020202009093c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f617070726f7665436f756e74272c2030293b0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f72656a656374436f756e74272c2030293b0a2020202020202020202020202020202020202020202020200a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f61637475616c50657263656e74272c2030293b0a202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c52656a65637450657263656e74272c2030293b0a2020202020202020202020202020202020202020202020200a202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f726571756972656450657263656e74272c2077665f7265717569726564417070726f766550657263656e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020203c2f61637469766974693a657865637574696f6e4c697374656e65723e0a20202020202020203c2f657874656e73696f6e456c656d656e74733e0a20202020202020200a20202020202020203c73746172744576656e742069643d227374617274220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a7375626d697447726f75705265766965775461736b22202f3e0a20202020202020200a0909090a20202020202020203c73657175656e6365466c6f772069643d27666c6f773127200a202020202020202020202020736f757263655265663d277374617274270a2020202020202020202020207461726765745265663d277265766965775461736b273e0a2020202020202020202020203c212d2d20544f444f3a204f6e6365206d756c7469496e7374616e63654c6f6f7043686172616374657269737469637320737570706f7274207573696e672065787072657373696f6e206173206c6f6f7044617461496e7075745265662c2072656d6f7665202777665f67726f75704d656d6265727327207661726961626c6520202d2d3e0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0a09202020202020202020202020203c61637469766974693a657865637574696f6e4c697374656e6572206576656e743d2273746172742220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e6c697374656e65722e536372697074457865637574696f6e4c697374656e6572223e0a09202020202020202009093c61637469766974693a6669656c64206e616d653d22736372697074223e0a092020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a09202020202020202020202020202020202020202020202020766172206d656d62657273203d2070656f706c652e6765744d656d626572732862706d5f67726f757041737369676e6565293b0a2020202020202020202020202020202020202020202020202020202020202020696628776f726b666c6f772e6d617847726f7570526576696577657273203e20302026616d703b26616d703b206d656d626572732e6c656e677468203e20776f726b666c6f772e6d617847726f7570526576696577657273290a20202020202020202020202020202020202020202020202020202020202020207b0a20202020202020202020202020202020202020202020202020202020202020202020207468726f77206e6577204572726f7228224e756d626572206f6620726576696577657273206578636565647320746865206d6178696d756d3a2022202b206d656d626572732e6c656e677468202b2022286d61782069732022202b20776f726b666c6f772e6d617847726f7570526576696577657273202b20222922293b0a20202020202020202020202020202020202020202020202020202020202020207d0a09202020202020202020202020202020202020202020202020766172206d656d6265724e616d6573203d206e6577206a6176612e7574696c2e41727261794c69737428293b0a092020202020202020202020202020202020202020202020200a09202020202020202020202020202020202020202020202020666f7228766172206920696e206d656d6265727329200a2020202020202020202020202020202020202020202020202020207b0a09202020202020202020202020202020202020202020202020202020206d656d6265724e616d65732e616464286d656d626572735b695d2e70726f706572746965732e757365724e616d65293b0a092020202020202020202020202020202020202020202020207d0a09202020202020202020202020202020202020202020202009657865637574696f6e2e7365745661726961626c65282777665f67726f75704d656d62657273272c206d656d6265724e616d6573293b0a09202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f7265766965776572436f756e74272c206d656d6265724e616d65732e73697a652829293b0a092020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a092020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a092020202020202020202020203c2f61637469766974693a657865637574696f6e4c697374656e65723e0a092020202020202020203c2f657874656e73696f6e456c656d656e74733e0a20202020202020203c2f73657175656e6365466c6f773e0a0a20202020202020203c757365725461736b2069643d227265766965775461736b22206e616d653d22526576696577205461736b220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a61637469766974695265766965775461736b223e0a20202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a2020202020202020202020202020202020202020202020206966287461736b2e6765745661726961626c654c6f63616c282777665f7265766965774f7574636f6d652729203d3d2027417070726f76652729207b0a20202020202020202020202020202020202020202020202020202020766172206e6577417070726f766564436f756e74203d2077665f617070726f7665436f756e74202b20313b0a0920202020202020202020202020202020202020202020202020766172206e6577417070726f76656450657263656e74616765203d20286e6577417070726f766564436f756e74202f2077665f7265766965776572436f756e7429202a203130303b0a09202020202020202020202020202020202020202020202020200a0920202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f617070726f7665436f756e74272c206e6577417070726f766564436f756e74293b0a0920202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c50657263656e74272c206e6577417070726f76656450657263656e74616765293b0a2020202020202020202020202020202020202020202020207d20656c7365207b0a20202020202020202020202020202020202020202020202020202020766172206e657752656a656374436f756e74203d2077665f72656a656374436f756e74202b20313b0a20202020202020202020202020202020202020202020202020202020766172206e657752656a65637450657263656e74616765203d20286e657752656a656374436f756e74202f2077665f7265766965776572436f756e7429202a203130303b0a202020202020202020202020202020202020202020202020202020200a20202020202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f72656a656374436f756e74272c206e657752656a656374436f756e74293b0a20202020202020202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c65282777665f61637475616c52656a65637450657263656e74272c206e657752656a65637450657263656e74616765293b0a2020202020202020202020202020202020202020202020207d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a20202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a20202020202020202020200a20202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b72657669657741737369676e65657d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020202020200a20202020202020202020203c212d2d20466f7220656163682061737369676e65652c207461736b2069732063726561746564202d2d3e0a20202020202020202020203c6d756c7469496e7374616e63654c6f6f7043686172616374657269737469637320697353657175656e7469616c3d2266616c7365223e0a20202020202020202020200920203c6c6f6f7044617461496e7075745265663e77665f67726f75704d656d626572733c2f6c6f6f7044617461496e7075745265663e0a20202020202020202020200920203c696e707574446174614974656d206e616d653d2272657669657741737369676e656522202f3e0a20202020202020202020200920203c636f6d706c6574696f6e436f6e646974696f6e3e247b77665f61637475616c50657263656e74203e3d2077665f7265717569726564417070726f766550657263656e74207c7c2077665f7265717569726564417070726f766550657263656e74203e2028313030202d2077665f61637475616c52656a65637450657263656e74297d3c2f636f6d706c6574696f6e436f6e646974696f6e3e0a20202020202020202020203c2f6d756c7469496e7374616e63654c6f6f704368617261637465726973746963733e0a20202020202020203c2f757365725461736b3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773227200a202020202020202009736f757263655265663d277265766965775461736b270a2020202020202020202020207461726765745265663d277265766965774465636973696f6e27202f3e0a0a20202020202020203c6578636c757369766547617465776179202069643d227265766965774465636973696f6e22206e616d653d22526576696577204465636973696f6e22202f3e200a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77332720736f757263655265663d277265766965774465636973696f6e27207461726765745265663d27617070726f76656427203e0a2020202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b77665f61637475616c50657263656e74203e3d2077665f7265717569726564417070726f766550657263656e747d3c2f636f6e646974696f6e45787072657373696f6e3e0a20202020202020203c2f73657175656e6365466c6f773e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f773427200a2020202020202020736f757263655265663d277265766965774465636973696f6e270a2020202020202020202020207461726765745265663d2772656a656374656427202f3e0a0a202020202020203c757365725461736b2069643d22617070726f76656422206e616d653d22446f63756d656e7420417070726f766564220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a617070726f766564506172616c6c656c5461736b22203e0a2020202020202020202020203c646f63756d656e746174696f6e3e0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e6420617070726f7665642e0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020202020200a2020202020202020202020202020202020202020202020202f2f2053657420706172616c6c656c2072657669657720706172616d73206f6e207461736b2c20746f206265206b65707420696e20686973746f72790a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f7265766965776572436f756e74272c2077665f7265766965776572436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f726571756972656450657263656e74272c2077665f726571756972656450657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c50657263656e74272c2077665f61637475616c50657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f617070726f7665436f756e74272c2077665f617070726f7665436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f72656a656374436f756e74272c2077665f72656a656374436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c52656a65637450657263656e74272c2077665f61637475616c52656a65637450657263656e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a2020202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020203c2f757365725461736b3e0a20202020202020200a20202020202020203c757365725461736b2069643d2272656a656374656422206e616d653d22446f63756d656e742052656a6563746564220a20202020202020202020202061637469766974693a666f726d4b65793d2277663a72656a6563746564506172616c6c656c5461736b22203e0a2020202020202020202020203c646f63756d656e746174696f6e3e0a2020202020202020202020202020202054686520646f63756d656e742077617320726576696577656420616e642072656a65637465642e0a2020202020202020202020203c2f646f63756d656e746174696f6e3e0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e65642729207461736b2e64756544617465203d2062706d5f776f726b666c6f77447565446174650a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0a2020202020202020202020202020202020202020202020200a2020202020202020202020202020202020202020202020202f2f2053657420706172616c6c656c2072657669657720706172616d73206f6e207461736b2c20746f206265206b65707420696e20686973746f72790a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f7265766965776572436f756e74272c2077665f7265766965776572436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f726571756972656450657263656e74272c2077665f726571756972656450657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c50657263656e74272c2077665f61637475616c50657263656e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f617070726f7665436f756e74272c2077665f617070726f7665436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f72656a656374436f756e74272c2077665f72656a656374436f756e74293b0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c654c6f63616c282777665f61637475616c52656a65637450657263656e74272c2077665f61637475616c52656a65637450657263656e74293b0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0a2020202020202020202020203c68756d616e506572666f726d65723e0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0a2020202020202020202020203c2f68756d616e506572666f726d65723e0a20202020202020203c2f757365725461736b3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77352720736f757263655265663d27617070726f766564270a2020202020202020202020207461726765745265663d27656e6427202f3e0a0a20202020202020203c73657175656e6365466c6f772069643d27666c6f77362720736f757263655265663d2772656a6563746564270a2020202020202020202020207461726765745265663d27656e6427202f3e0a0a20202020202020203c656e644576656e742069643d22656e6422202f3e0a0a202020203c2f70726f636573733e0a0a2020203c212d2d2047726170686963616c20726570726573656e7461696f6e206f66206469616772616d202d2d3e0a2020203c62706d6e64693a42504d4e4469616772616d2069643d2242504d4e4469616772616d5f6163746976697469506172616c6c656c47726f7570526576696577223e0a2020202020203c62706d6e64693a42504d4e506c616e652062706d6e456c656d656e743d226163746976697469506172616c6c656c47726f7570526576696577220a20202020202020202069643d2242504d4e506c616e655f6163746976697469506172616c6c656c47726f7570526576696577223e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227374617274220a20202020202020202020202069643d2242504d4e53686170655f7374617274223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d2233302220793d22323030223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965775461736b220a20202020202020202020202069643d2242504d4e53686170655f7265766965775461736b223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22313235220a202020202020202020202020202020793d22313930223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d227265766965774465636973696f6e220a20202020202020202020202069643d2242504d4e53686170655f7265766965774465636973696f6e223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223430222077696474683d2234302220783d223239302220793d22313937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22617070726f766564220a20202020202020202020202069643d2242504d4e53686170655f617070726f766564223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220a202020202020202020202020202020793d223937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d2272656a6563746564220a20202020202020202020202069643d2242504d4e53686170655f72656a6563746564223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223535222077696474683d223130352220783d22333930220a202020202020202020202020202020793d22323937223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e53686170652062706d6e456c656d656e743d22656e64222069643d2242504d4e53686170655f656e64223e0a2020202020202020202020203c6f6d6764633a426f756e6473206865696768743d223335222077696474683d2233352220783d223535352220793d22333037223e3c2f6f6d6764633a426f756e64733e0a2020202020202020203c2f62706d6e64693a42504d4e53686170653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7731222069643d2242504d4e456467655f666c6f7731223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d2236352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223132352220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7732222069643d2242504d4e456467655f666c6f7732223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223233302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223239302220793d22323137223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7733222069643d2242504d4e456467655f666c6f7733223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313937223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7734222069643d2242504d4e456467655f666c6f7734223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22323337223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223331302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223339302220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7735222069643d2242504d4e456467655f666c6f7735223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22313234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223537322220793d22333037223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020202020203c62706d6e64693a42504d4e456467652062706d6e456c656d656e743d22666c6f7736222069643d2242504d4e456467655f666c6f7736223e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223439352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020202020203c6f6d6764693a776179706f696e7420783d223535352220793d22333234223e3c2f6f6d6764693a776179706f696e743e0a2020202020202020203c2f62706d6e64693a42504d4e456467653e0a2020202020203c2f62706d6e64693a42504d4e506c616e653e0a2020203c2f62706d6e64693a42504d4e4469616772616d3e0a3c2f646566696e6974696f6e733e	f
19	1	4102fd9f-2bed-4d37-b46c-9ce37d819e74activitiParallelGroupReview.png	17	\\x89504e470d0a1a0a0000000d49484452000002580000016a0806000000c63c24e70000226749444154785eeddd7d905c659d2ff0f0aae2baba252f0a889656f9c6de2a5fb66eb95a5bc4bda8c522f8d7309924f2165ea2609544222f961b5e5c14d87beb7257c03fb4345aa04617dfae9a401272a31008b23132919560082664322421e46599242439f73ced9cb1f374cf4cf73c3d99d3d39f4fd5b766fa9c33a70f7326bffe72baa767ca140000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080769765d9b1ebd6ad5bb062c58a7d4b962cc91e78e0012941f2737170f9f2e59b962e5dfa89f89c0100259797ab1fe40fe4597f7f7f36303090edddbb574a90702ec23959b66cd98ebc709d1d9f3700a0c4c295abf0401e3fc04b39d2d7d7b7352f582be3f306009458785ad095abf2269c9bbc60ed89cf1b005062e1f53ef183ba942be11cc5e70d0028b1460bd6ee17fbb2758f7d335bb3f8a64ac2e76159bc9db43e0a1600b499460ad6aeedcf65bd8bbe98adfef9dc4312968575f1f6d2da285800d0661a29581bd6fcb4a65c15d9b8e66735db4b6ba36001409b69a4603df9e0ad35c5aa4858176f2fad8d8205006da69182d5fbc00d35c5aa4858176f2fad8d8205006d46c12a7f142c0068338d14acf05b8371b12a12d6c5db4b6ba36001409b69a460ad7de8ab35c5aa4858176f2fad8d8205006da69182b5edb9df65bdf7cfab2957615958176f2fad8d8205006da6918215f2cc6fbe5d53b0c2b2783b697d142c0068330d15ac3d7bb2b50fdf5d53b0c2b2b0ae667b6969142c006833a315acf04eed4f3df46f35e5aa4858e7dddcc7370a1600b499610bd69e3d59dfdaa5d9130bafaf295571c236615b57b3c6270a1600b4997a056bb4ab56c3c5d5acf1898205006da65ec16ae4aad570095f1bef4fd2a26001409ba957b0e2d2d46ce2fd495a142c006833f50a96942b0a160094c4b469d31ecc33355e1e53b0ca1f050b004a222f57d960462c5a0a56f9a3600140495415ac118b968255fe2858005012750a56dda2a560953f0a160094449d6215a752b414acf247c102e0b0ab531ca4892858e54f7ccea4b1c4b302802618a4f5c50f365579709aa708db2aae6035cf5c00486490d6375ab12a1cce8295df5d25c71c734c76fcf1c767679e79a62b680d44c16a9eb90090c820ad6fb46255389c0567ca60c11a1818c856ae5c999d7efae9d951471d952d5ab4a8665bf94b14ace6990b00890cd2fa462b5685892858c5ed850b17566e4f9d3a7568d9e6cd9bb399336756ae7085cc9831a3b2ac58bf6ddbb6ec8a2baec84e39e594ec88238e38647ff1fe87bb5de4b8e38ecbdefbdef766a79d765a76ecb1c766a79e7a6ab660c1829ae39ee82858cd3317001219a46926b2606dd9b2a572fbf5af7ffdd0b250aec2b2ef7ce73bd9b7bef5adcae76159b17ed6ac5995659ffffce7b39d3b77d6ecffc8238f1cf6fe8adb77de7967e50a5af5edc71f7fbcf2f9c9279f7cc83ecb1005ab79e60240228334cd4416ace79f7fbe723b5ca92a9685b21596bdf8e28bd9f6eddb6bd69f70c2099565fdfdfd75f71f9e721ceefe8adb61df2fbdf4d2d0ed703ff5b62f4b14ace6990b00890cd2341359b0ea3d45985ab08a2b58e1ebe3fb6bf67659a26035cf5c00486490a699888235d28bdc1b7d8af0da6bafad798af0a4934eaaac7be49147b2bbeebaaba63035737ba475f56e8f6714ace6990b00890cd2341351b08e3efae8ca95aa0f7ff8c3356fd350bcc83dac0f19ee45eee1b552f18bdce7cf9f9f9d78e28943f75324beff466e8fb4aedeedf18c82d53c73012091419a262e3852be2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a651b0ca1f05ab79e602402283348d8255fe2858cd3317001219a46914acf247c16a9eb90090c8204da360953f0a56f3cc05804406691a05abfc51b09a672e00243248d32858e58f82d53c73012091419a46c12a7f14ace6990b00890cd2340a56f9a36035cf5c00486490a659b264c9c18181819a07752947f273b3292f587be2f3c6c8cc05804406699ae5cb976feaefefaf79609772e4d9679ffd5e5eb056c6e78d91990b00890cd2344b972efdc4b265cb76f4f5f56d7525ab3cc9cf45dffaf5ebefcdcbd59ff29c1d9f3746662e00243248d38507f0709524cfdef07a1f2945c2b908e744b91a0373012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc05c65d9665c7ae5bb76ec18a152bf62d59b2247be08107a404c9cfc5c1e5cb976f5aba74e927e27346730c5220662e30eef272f583fc813cebefefcf060606b2bd7bf74a0912ce453827cb962ddb9117aeb3e3f346e30c5220662e30eec295abf0401e3fc04b39d2d7d7b7352f582be3f346e30c5220662e30eec2d382ae5c9537e1dce4056b4f7cde689c410ac4cc05c65d78bd4ffca02ee54a3847f179a371062910331718778d16acdd2ff665eb1efb66b666f14d9584cfc3b2783b697d14ac34062910331718778d14ac5ddb9fcb7a177d315bfdf3b987242c0bebe2eda5b551b0d218a440cc5c60dc3552b036acf9694db92ab271cdcf6ab697d646c14a72c6e0203d235e01742e058b71d748c17af2c15b6b8a5591b02ede5e5a1b056bcc42a9eacf73f5e047250ba850b018778d14acde076ea8295645c2ba787b696d14ac3129ca5551aae2db400753b0eae8eaea3aeaeebbefbef85ffff55f7ff5852f7ce1f93973e6fcd7ecd9b3f7876f56f878d55557fd57587eebadb73e74c71d775c966f7f6cbc0ffe42c12a7f14aca60d57a6865b0e741805abcaedb7dffeb737df7cf3e3b366cd3a70fdf5d7673ffce10fb3d5ab5767ebd7afcfb66ddb96052fbcf042e5f6ef7ef7bbecbefbeecbe6cd9b975d78e18561fb35d75d779da15a4723052bfcd6605cac8a8475f1f6d2da28584d19ad448db61ee8000a562e2f4927df74d34dcb2eb9e492033ff8c10f2a25aa19bb77efceeebffffeecd39ffef4813973e6f4ce9e3dfbbfc7f7d1c91a29586b1ffa6a4db12a12d6c5db4b6ba36035acd1f2d4e876c024d5f105eb965b6ef9f865975db677fefcf9d9ce9d3be3eed494975f7eb952b4f2fdedfbd4a73e75db0d37dc70647c7f9da89182b5edb9df65bdf7cfab2957615958176f2fad8d82d590664b53b3db0393484717ac9b6fbef9864b2fbdf4c0aa55abe2ae9464fbf6ede1a9c3bd975c72c9c33d3d3dc7c7f7db691a295821cffce6db35052b2c8bb793d647c11ad558cbd258bf0e68731d5bb0bef4a52fddf899cf7ce6e073cf3d17f7a3963870e04076cf3df7bc7cc10517ac9b3e7dfadfc4f7df491a2a587bf6646b1fbebba6608565615dcdf6d2d22858234a2d49a95f0fb4a18e2c58834f0b1e18af72556dfefcf9bb3ef9c94f3e72e18517be323e8e4e315ac10aefd4fed443ff5653ae8a8475decd7d7ca3600dab55e5a855fb01da44c715ac2f7ce10ba784d75cb5fa69c1e11c3c7830bbfdf6dbb74e9f3e7d417c2c9d62d882b5674fd6b77669f6c4c2eb6b4a559cb04dd8d6d5acf189825557ab4b51abf7079458c715acf0db82e105ed875378f1fba5975ebab9bbbbfbe3f1f174827a056bb4ab56c3c5d5acf1898255232e4347e4b960f06333e2af8bf70b4c521d55b0c2fb5c85b762d8b16347dc81c6ddaa55ab5eece9e979ba13df94b45ec16ae4aad570095f1bef4fd2a2601d222e41a11c7d234ff81e858f8d96ace1be2ede3f30097554c1baf1c61b1f0fef733551aeb9e69affcc4bd6e7e2e39aecea15acb834359b787f9216056b48bdf213ae4085ef4f91464a5675b92a727ed5fa7af7034c221d53b0c29fbf99356bd6fe66df44b4957a7b7bfbf26ff81fa78c3e9c27957a054bca1505ab62b8d253af2c8d54b2ea6dfff5c1e5d586bb3f6012e8988215feb660f8f3371329bce07de6cc99fdf937fd83f1f14d660a56f933590a56fe6febc13c53e3e50d18adecd42b4df54a56bdedea95abc268f70bb4a98e2958b7dd76dbaf27f2e9c1c22db7dcf248fe4dbf213ebe76d4e883998255fe4ca282950da6a19fcd418d969c7ae5a9ba64d55b3f52b92a347aff401be99882f5c52f7eb1ff70bd35c348f207b2c7babbbb7f1e1f5f3b6af4c14cc12a7f2661c16ae867734af3e5a65e890ab7c39fc58a973752ae0acd1e0750721d53b0e6cc99f35febd7af8ffbce61d7dbdbfbfb697f7e1d56db6bf4c14cc12a7f2671c11ae96773aca5a65ec9fa4374bb99725518ebf10025d43105ebb2cb2edb1ffe46e044dbb265cbe6fc9bbe233ebe7654e741acee83998255fe7440c18a7f3653cb4cbd929552ae0aa9c70594449839f1b24969c68c199537fc9c68fbf6eddb937fd3f7c5c7d78eea3c78c5a93c982958e54f9d733769d3d5d595bdf295af0c83efeaf867ba49e169c1f8ca55b81d96a708c7154ad671f10aa07d8479132f9b942eb8e0828325ba825533f42773ca58b0a60c3e20c6cbdb2dadfaefe8802b580f4e3bf429c2d42b45235dc1aaf7db858d4a3d2ea024c2ec89974d4ab367cfde5b86d760ad59b326bc06ebd1f8f8da519d07b1ba0f66632d5853aa1eb4feeaaffe2afbc0073e90fdfad7bfaed96e2c29f61b2f6f75aaff1bea25debed9b46a3f93b860c5c5aada1953c65666ea95abf84ad6584ad6588f0728a18e2958d75e7bedd6dffef6b771df39ec162f5ebc32ffa6df171f5f3b6af4c12cb56085cf7ffad39f563e7fd7bbde55b35dbba4fabfa75569d53e2761c1aafbb35847b3a5a65eb90aafb9aaf75b84cd94ac668f0328b98e2958e1fda7eebbefbeb8ef1c76b7de7aeb43f9377d5e7c7ceda8d107b35614ac5dbb76553e7fd5ab5e35b47eebd6ade18f6867279d7452f6dad7be363be79c73b275ebd6657d7d7dd971c71d97bded6d6fcbf6ecd953d9367c7ceb5bdf9abdfad5afce366fde7cc8be47da57b1fec4134fcc4e38e184cae75ffef2972b5ffb95af7ca5723b2c0f5f171f7f9cf83eebad7bcd6b5e937df0831f3ce44add82050bb2b7bffdedd931c71c53b38feadb8f3df658f6c637be313bfae8a3b33beeb8a3e63e46ca242a5823fe2c0ee38c298d959be1cad548ef83d548c96af4fe8136d24905ebea79f3e6c57de7b09b3973e686193366fcb7f8f8da51a30f66ad2858c515ac7ffaa77f1a5a7fd145175596fde8473fca1e7ef8e1cae7fff88fff585977f9e597576effe4273fa9dcfef18f7f5cb93d7bf6ec9a7d8fb6af908f7ef4a395651b376eccdef9ce77563e7fc73bde916dd8b0a1f2f9c73ef6b19ae38f13df67bd3cf1c413956ddefdee770f2d0b052e94ab5ffce21735db17fb5cb87061f6d77ffdd7d9eb5ef7baec97bffc65cd76a365b214ac04674c19b9e4d42b4ff57e5bb0de762395acd1ee1768531d53b0bababa5e7bfef9e71f0c574226cad34f3ffd5cfe0d7f2a3eb6c92eb5601579d39bde943df3cc3343eb8f3ffef89a6d5ef18a5754d6ad59b3263bf2c8232bc528dcfec8473e52b9fdfbdffffe907d37b2af90b973e75696dd78e38d958fdddddd958ffffccfff5cf978cd35d7d41c7f9cf83e8b8472f4fef7bfbf7275add8261c6bb1fee4934fae2c7bf39bdf9c5d75d555875c592bb60f052cfc37f4f6f6d6ecbf91285815674ca95f76ea95a67ae5aa506ffb7a256bb8fb0326818e2958c1b5d75efbe4fdf7df1ff79ec3e64b5ffad263f937fce6f8b826bbd482b573e7ceec339ff94ce5f3eaab4ae1ca4e5816ae2ac55f1b72f6d96767471c7144e5ea55f8189ef68bf7dde8beeeb9e79ecafaf014de29a79c92edd8b1a3f2b12845f7de7b6fcdd7c489efb348d84f58fed0430f55f61b6ff7e8a38f66e79e7beed05384ef79cf7b6af679da69a7553ede72cb2d35fb6f240ad69033a6d4969e0ba60c7e9f073352b92ad42b59e757adaf773fc024d25105ebb39ffdecd9575c71c58189783fac0d1b36bc907fb39f9f3e7dfadfc4c735d9a516acf079788b8de22a53f154d9ac59b32ab7f3f35a2926f1d70f9686ca6f20868f8b172faebbef46f615ae88155f73d34d3755961557b3428a2b632325becf2245b9fbd5af7e5579edd470db85d76585e5e17556f13ed7ae5d5bb9c2557d7ccd44c13ac419530e2d3fd565a991725518eeebe2fd0393504715ac202f587f9c88ab589ffbdce79ec8bfd957c5c7d3095a51b042427108b7ffe11ffea1727bdbb66dd995575e999d7aeaa995a7d4e2ed43def7bef75596858f23ed7bb47d8517c987ab57e169c3e22a57f8186e87e5c58be9474abccf22dffdee77b337bce10d43ebe3ed8adbe10a56f82dcaf07ab4785df8fce9a79fcedef296b7546e87a72ee3fb19290a568d33a6d496ac7005aad1725588bf2ede2f3049755cc19a3d7bf6872ebbecb27d87f34d47172f5efcc7fc1bbdb6ababebd8f8783ac1580b961cbe285875b5ba0cb57a7f4089755cc10a2ebdf4d2bbe7cd9bb7f7c0810371176ab93ffce10f5b7a7a7ab6747777ff6d7c1c9d42c12a7f14ac61b5aa14b56a3f409be8c88275d65967bd62d6ac59abefb9e79e717d31d6962d5b0666ce9cb9392f571f8f8fa193b463c1dabd7b77e537f2c2c794e5ed12056b44a9e528f5eb8136d491052be8eaea3ae1fcf3cf7f76fefcf9bb0f1e3c1877a364e1ca555eaefa7b7a7a3e1ddf77a769c78215ca527ee8356f7bd0ecf276898235aab196a4b17e1dd0e63ab6600579c97a435e82fe70fbedb76fddbf7f7fdc91c62cbce66af069c18ebe725550b0ca1f05ab21cd96a566b70726918e2e58c1b9e79efb9ae9d3a7ffe8d24b2fddbc6ad5aa17e3b2d48cf0560c83bf2db8b6935f731553b0ca1f05ab618d96a646b70326a98e2f58859e9e9e9979faafb9e69aff7ce28927fae2f23492f00eedfff22ffff29bf03e5779b19ad3a9bf2d381c05abfc51b09a325a791a6d3dd00114ac2a9ffce4275f9d17a4b9f937655d78fd54f803d1f903cf6379e1fafdf3cf3fbf2994a9f0317f105db378f1e295b7dd76dbafc3df160c57acf2dcd4896f22da0805abfc51b09a365c891a6e39d06114ac619c77de791fcabf3937e4f9bf83056a77f866e5f9539e1579fe3dcfbcc9f2879bc7938255fe2858631297a9f836d0c1142cc69d8255fe2858635694aaab073f2a57408582c5b853b0ca1f052bc919838354b9028628588c3b05abfc51b0d218a440cc5c60dc2958e58f8295c6200562e602e34ec12a7f14ac34062910331718770a56f9a360a531488198b9c0b85bb264c9c18181819a07f53267b8c2d4ecf276487e6e36e5056b4f74da6882410ac4cc05c6ddf2e5cb37f5f7f7d73cb09739bb77efae94a5f03165793be4d9679ffd5e5eb056c6e78dc619a440cc5c60dc2d5dbaf413cb962ddbd1d7d7b7b5ddae644de6e4e7a26ffdfaf5f7e6e5ea4f79ce8ecf1b8d33488198b9c061111ec0c355923c7bc3eb7da41409e7229c13e52a91410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091410ac4cc0580440629103317001219a440cc5c00486490023173012091419a26cbb263d7ad5bb760c58a15fb962c59923df0c0035282e4e7e2e0f2e5cb372d5dbaf413f139e350f90c7830cc8151f260fc75008c40c14a9397ab1fe40fe4597f7f7f36303090edddbb574a90702ec23959b66cd98ebc709d1d9f37fe229f0153eb14aa3853e3af0360040a569a70e52a3c90c70ff0528ef4f5f56dcd0bd6caf8bc71a87085aa4ea972f50a60ac14ac34e1694157aeca9b706ef282b5273e6f1c2a5ca1aa53ac5cbd02182b052b4d78bd4ffca02ee54a3847f179a356b85255a75cb97a0530160a569a460bd6ee17fbb2758f7d335bb3f8a64ac2e76159bc9db43e0a566386b98a3535de0e80062858691a2958bbb63f97f52efa62b6fae7730f495816d6c5db4b6ba360352eba8ae5ea15c0582958691a29581bd6fcb4a65c15d9b8e66735db4b6ba360352eba8a35355e0f408314ac348d14ac271fbcb5a6581509ebe2eda5b551b09a535cc58a9703d00483344d2305abf7811b6a8a5591b02ede5e5a1b056b78679d75d62bbabbbb3fdad3d3f3bff38f0bf379f01f79b60c5ec1da982f5b957f5c947ffc3ff9c7b3cf39e79ce3e27d005087829546c12a7f14ac5a79a1fafbbc34fd30fff7bfabea29c146323058b8bc433ec04814ac348d14acf05b8371b12a12d6c5db4b6ba360fd455eac4ecfffcdffb84e711a4bc2d5ae73e3fb00608a8295aa9182b5f6a1afd614ab22615dbcbdb4360ad69fe5ffd6afcfb33f2e4a73e6ccc9eebdf7deecb7bffd6db67efdfa6cdbb66d59f0c20b2f546eaf5ebd3afbfef7bf9f5d77dd7571c12a727f5757d709f1fd017434052b4d23056bdb73bfcb7aef9f5753aec2b2b02ede5e5a9b4e2f5879f97955feeffcdeea52347dfaf4ecaebbee0a7f4aa852a61ab57dfbf64ad9bae8a28be292b5e1bcf3cefb507cdf001d4bc14ad348c10a79e637dfae29586159bc9db43e9d5cb006cbd58aea3274f3cd37671b366c88bb535376edda95dd73cf3d95a256b5ef7ddddddde7c7c700d09114ac340d15ac3d7bb2b50fdf5d53b0c2b2b0ae667b69693ab9604d8bae5ccd9f3f3f3b70e040dc97c6acb7b737bbfcf2cbab4bd6febc644d8b8f03a0e3285869462b58e19dda9f7ae8df6aca5591b0cebbb98f6f3ab560f5f4f45c575dae162d5a14f7a39608afd5bae69a6baa4bd64b79fe2e3e1e808ea260a519b660edd993f5ad5d9a3db1f0fa9a5215276c13b675356b7cd289056bf0b705875ed01eae5c8da750b2aebcf2caea92f5b4f7cc023a9a8295a65ec11aedaad57071356b7cd289056b5ad55b3184d75cb5f269c1e16cdcb831bbf8e28bab4bd6edf17101740c052b4dbd82d5c855abe112be36de9fa4a5d30a567777f707f27fd707c3bfedf022f4d417b43763e1c285d5056b60c68c19a7c6c707d01114ac34f50a565c9a9a4dbc3f494b0716acf00eed959213de8ae170dabf7f7f3677eedca192d5d3d3f33fe3e303e8080a569a7a054bca954e2a58e16f0be6a566675170366dda1477a071f7f8e38f575fc5dad2d5d5756c7c9c00939e82555ffe7d7930cfd478794cc12a7f3aa9604d9f3efd6345b9b9faeaabe3ee735884ab58975c72c950c93aefbcf3ce8c8f1360d253b0eaabfa3ff0118b968255fe4c968235dacf6290afbfa3f8d90d6f043a51bef6b5af0d15aceeeeee5be3e30498f414acfaaa0ad688454bc12a7f2651c11af16731c897ffa2d86ed5aa5571ef396c56ac5851fd6f67517c9c00935e1880f132ea16acba0f6e0a56f933090b56dd9fc5c16dfea3581ffe50f34459bb766df5713e537d8c001d210cc07819751fcce2541edc14acf2671217ac437e1607b7d9542c0f7f9879a26cdbb6adfaf87644ff2900935f9d612d4d44c12a7f3aa0601579b0bbbbfbe5e2f6cb2fbf1cf79ec366dfbe7dd5c7b52ffe6f01a043d579f01a7a109be629c2b64a9d73d81129d115ac8d55ffb400e864f183d5b43aaf77090e67c1caefae92638e39263bfef8e3b333cf3cd315b406d20157b0867e36f38f1b8ae513f91aaca79f7ebafaf81e8dfe5300e854f51ebcea399c0567ca60c11a1818c856ae5c999d7efae9d951471d952d5ab4a8665bf94b2671c1aaf9d9ece9e9f97fc5fa89fc2dc2471f7db4fa38efab3e46003a58bd07af7a26a26015b7172e5c58b93d75ead4a1659b376fce66ce9c59b9c2153263c68ccab2627d78eae68a2baec84e39e594ec88238e38647ff1fe87bb5de4b8e38ecbdefbdef766a79d765a76ecb1c766a79e7a6ab660c1829ae39ee84cc28235eccf66befcee62bbef7ffffb71ef396cbef18d6f0c15acbcf47d393e4e0018d14416ac2d5bb6546ebffef5af1f5a16ca5558f69def7c27fbd6b7be55f93c2c2bd6cf9a35abb2ecf39fff7cb673e7ce9afd1f79e491c3de5f71fbce3befac5c41abbe1dfe3c4af8fce4934f3e649f65c8242a58c316ab427777f78ca2d85c77dd7571ef392c0e1e3c98cd9e3dbbba60fd7d7c9c0030a2892c58cf3fff7ce576b852552c0b652b2c7bf1c5172b2f728ed79f70c2099565fdfdfd75f71f9e721ceefe8adb61df2fbdf4d2d0ed703ff5b62f4b264bc16a445757d71bf252b37fb0d84cc80bdd9f7cf2c9eaa707c30bdc8f888f130046349105abde5384a905abb88215be3ebebf666f97259d54b082bc582d9dc8a7096fb9e596ea82f5d5f8f80060541351b0467a917ba34f115e7bedb5354f119e74d24995758f3cf24876d75d77d514a6666e8fb4aedeedf14ca715aceeeeee6945c1b9e8a28bb25dbb76c51d68dcac5ebdbaba5cedebeaea7a477c7c0030aa892858471f7d74e54ad5873ffce19ab769285ee41ed6870cf722f7f05aa9f845eef3e7cfcf4e3cf1c4a1fb2912df7f23b7475a57eff678a6d30ad6d4a9538fcecbcd3345d1395c7ff439bcb9e8dcb973870a565ef4ee8c8f0d001a12171c295f3aad60053db9a2e84c9f3e3debeded8dfb50cb855f76a8ba7ab5a3ababeb84f8b800a0210a56f9d389052b77c4b43fffd661a5f05c7ef9e5d90b2fbc1077a296f9c94f7e525daec20bec2f8f0f08001aa660953f1d5ab0a64c9f3efdcddddddddb8bd213de9a633c4a562857e137163d350840cb2858e54fa716ac202f3b1f9d36f8b60d21575e7965b671e3c6b8238d4978cd55f4b460c8e2f01ab0f83800a0290a56f9d3c9052be8e9e9b9a0ba645d7cf1c595b7f8d8bf7f7fdc991a167e5bb0fa05ed45b9baf0c20b5f17df3f00344dc12a7f3abd6005836fddf05275210a0529bc037fa3452bbc437b7813d1e87dae2a094f0bba720540cb2858e58f82f5677911fabb3c4fc7e528bc37dad7bef6b5cafb9f3df5d453d9d6ad5b2b852a7c5cbb766de50f377ffdeb5f3fe4cfdf54658717b403d0720a56f9a360fdc539e79c735c5e886ecb8bd19e3a65a999ec0b57adbc150300e342c12a7f14ac5a3366cc38352f49ff2bcf963ae569a46c1c2c56dea11d80f1a360953f0ad6f0ce3aebac57e4a5e97fe4a5e9d6fce32ff2accbb37bb04cfd29cf2379eecbf3953c1f8cbf1e00c6858255fe285800d06614acf247c1028036a360953f0a1600b41905abfc51b000a0cd2858e58f8205006d66c99225070706066a1ed4a51cc9cfcda6bc60ed89cf1b005062cb972fdfd4dfdf5ff3c02ee5c8b3cf3efbbdbc60ad8ccf1b0050624b972efdc4b265cb76f4f5f56d7525ab3cc9cf45dffaf5ebefcdcbd59ff29c1d9f3700a0e4c20378b84a92676f78bd8f9422e15c8473a25c0100000000000000000000000000000000000000001c0eff1f474890a9b43061420000000049454e44ae426082	t
22	1	6b79e387-9c94-4d06-a9f3-0874e9389670bpmn20.xml	21	\\x3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220d0a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220d0a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220d0a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220d0a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f72672f776f726b666c6f77732f696e7465726e616c223e0d0a0d0a2020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469496e7669746174696f6e4d6f6465726174656422206e616d653d224d6f6465726174656420616374697669746920696e7669746174696f6e2070726f63657373223e0d0a0d0a2020202020203c73746172744576656e742069643d227374617274222061637469766974693a666f726d4b65793d22696d77663a6d6f64657261746564496e7669746174696f6e5375626d69745461736b22202f3e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77312220736f757263655265663d22737461727422207461726765745265663d2273656e64496e7669746522202f3e0d0a2020202020200d0a2020202020203c736572766963655461736b2069643d2273656e64496e7669746522206e616d653d2253656e6420496e76697465222061637469766974693a64656c656761746545787072657373696f6e3d22247b53656e644d6f64657261746564496e7669746544656c65676174657d223e3c2f736572766963655461736b3e0d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77322220736f757263655265663d2273656e64496e7669746522207461726765745265663d227265766965775461736b22202f3e0d0a0d0a2020202020203c757365725461736b2069643d227265766965775461736b22206e616d653d22526576696577205461736b220d0a20202020202020202061637469766974693a666f726d4b65793d22696d77663a61637469766974694d6f64657261746564496e7669746174696f6e5265766965775461736b223e0d0a2020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22637265617465220d0a202020202020202020202020202020636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e656427290d0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c65282762706d5f64756544617465272c2062706d5f776f726b666c6f7744756544617465293b0d0a20202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729200d0a2020202020202020202020202020202020202020202020207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c652827696d77665f7265766965774f7574636f6d65272c207461736b2e6765745661726961626c652827696d77665f7265766965774f7574636f6d652729293b0d0a202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c652827696d77665f726576696577436f6d6d656e7473272c207461736b2e6765745661726961626c65282762706d5f636f6d6d656e742729293b0d0a202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c652827696d77665f7265766965776572272c20706572736f6e2e70726f706572746965732e757365724e616d65293b0d0a2020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020203c706f74656e7469616c4f776e65723e0d0a2020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b62706d5f67726f757041737369676e65657d3c2f666f726d616c45787072657373696f6e3e0d0a2020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020203c2f706f74656e7469616c4f776e65723e0d0a2020202020203c2f757365725461736b3e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77332220736f757263655265663d227265766965775461736b22207461726765745265663d227265766965774465636973696f6e22202f3e0d0a0d0a2020202020203c6578636c7573697665476174657761792069643d227265766965774465636973696f6e22206e616d653d22526576696577204465636973696f6e22202f3e200d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77342220736f757263655265663d227265766965774465636973696f6e22207461726765745265663d22617070726f766564223e0d0a202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b696d77665f7265766965774f7574636f6d653d3d27617070726f7665277d3c2f636f6e646974696f6e45787072657373696f6e3e0d0a2020202020203c2f73657175656e6365466c6f773e0d0a2020202020200d0a2020202020203c736572766963655461736b2069643d22617070726f76656422206e616d653d22417070726f766564222061637469766974693a64656c656761746545787072657373696f6e3d22247b417070726f76654d6f64657261746564496e7669746544656c65676174657d22202f3e0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77352220736f757263655265663d22617070726f76656422207461726765745265663d22656e6422202f3e0d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77362220736f757263655265663d227265766965774465636973696f6e22207461726765745265663d2272656a656374656422202f3e0d0a0d0a2020202020203c736572766963655461736b2069643d2272656a656374656422206e616d653d2252656a6563746564222061637469766974693a64656c656761746545787072657373696f6e3d22247b52656a6563744d6f64657261746564496e7669746544656c65676174657d22202f3e0d0a2020202020203c73657175656e6365466c6f772069643d27666c6f77372720736f757263655265663d2772656a656374656427207461726765745265663d27656e6427202f3e0d0a2020202020200d0a2020202020203c656e644576656e742069643d22656e6422202f3e0d0a2020203c2f70726f636573733e0d0a0d0a3c2f646566696e6974696f6e733e	f
25	1	44558eb1-01ea-4757-be7a-245b766b3dd3bpmn20.xml	24	\\x3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220d0a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220d0a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220d0a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220d0a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f72672f776f726b666c6f77732f696e7465726e616c223e0d0a0d0a2020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469496e7669746174696f6e4e6f6d696e6174656422206e616d653d224e6f6d696e6174656420616374697669746920696e7669746174696f6e2070726f63657373223e0d0a0d0a2020202020203c73746172744576656e742069643d227374617274222061637469766974693a666f726d4b65793d22696e77663a696e76697465546f536974655461736b22202f3e0d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77312220736f757263655265663d22737461727422207461726765745265663d2273656e64496e7669746522202f3e0d0a2020202020200d0a2020202020203c736572766963655461736b2069643d2273656e64496e7669746522206e616d653d2253656e6420496e76697465222061637469766974693a64656c656761746545787072657373696f6e3d22247b53656e644e6f6d696e61746564496e7669746544656c65676174657d22202f3e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77322220736f757263655265663d2273656e64496e7669746522207461726765745265663d22696e7669746550656e64696e6722202f3e0d0a0d0a2020202020203c757365725461736b2069643d22696e7669746550656e64696e6722206e616d653d22496e766974652050656e64696e67220d0a20202020202020202061637469766974693a666f726d4b65793d22696e77663a6163746976697469496e7669746550656e64696e675461736b223e0d0a2020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22637265617465220d0a202020202020202020202020202020636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f774475654461746520213d2027756e646566696e656427290d0a2020202020202020202020202020202020202020202020207461736b2e7365745661726961626c65282762706d5f64756544617465272c2062706d5f776f726b666c6f7744756544617465293b0d0a20202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729200d0a2020202020202020202020202020202020202020202020207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d22636f6d706c6574652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a202020202020202020202020202020202020202020657865637574696f6e2e7365745661726961626c652827696e77665f696e766974654f7574636f6d65272c207461736b2e6765745661726961626c652827696e77665f696e766974654f7574636f6d652729293b0d0a2020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020203c68756d616e506572666f726d65723e0d0a2020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b62706d5f61737369676e65652e70726f706572746965732e757365724e616d657d3c2f666f726d616c45787072657373696f6e3e0d0a2020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020203c2f68756d616e506572666f726d65723e0d0a2020202020203c2f757365725461736b3e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77332220736f757263655265663d22696e7669746550656e64696e6722207461726765745265663d22696e766974654761746577617922202f3e0d0a0d0a2020202020203c6578636c7573697665476174657761792069643d22696e766974654761746577617922206e616d653d22496e76697465204761746577617922202f3e200d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77342220736f757263655265663d22696e766974654761746577617922207461726765745265663d226163636570746564223e0d0a202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b696e77665f696e766974654f7574636f6d65203d3d2027616363657074277d3c2f636f6e646974696f6e45787072657373696f6e3e0d0a2020202020203c2f73657175656e6365466c6f773e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77352220736f757263655265663d22696e766974654761746577617922207461726765745265663d2272656a6563746564223e0d0a202020202020202020203c636f6e646974696f6e45787072657373696f6e207873693a747970653d2274466f726d616c45787072657373696f6e223e247b696e77665f696e766974654f7574636f6d65203d3d202772656a656374277d3c2f636f6e646974696f6e45787072657373696f6e3e0d0a2020202020203c2f73657175656e6365466c6f773e0d0a2020202020200d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77372220736f757263655265663d22696e766974654761746577617922207461726765745265663d2263616e63656c6c656422202f3e0d0a2020202020200d0a2020202020203c736572766963655461736b2069643d22616363657074656422206e616d653d224163636570746564222061637469766974693a64656c656761746545787072657373696f6e3d22247b4163636570744e6f6d696e61746564496e7669746544656c65676174657d22202f3e0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77362220736f757263655265663d22616363657074656422207461726765745265663d22696e76697465416363657074656422202f3e0d0a0d0a2020202020203c736572766963655461736b2069643d2272656a656374656422206e616d653d2252656a6563746564222061637469766974693a64656c656761746545787072657373696f6e3d22247b52656a6563744e6f6d696e61746564496e7669746544656c65676174657d22202f3e0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77382220736f757263655265663d2272656a656374656422207461726765745265663d22696e7669746552656a656374656422202f3e0d0a2020202020200d0a2020202020203c736572766963655461736b2069643d2263616e63656c6c656422206e616d653d2243616e63656c6c6564222061637469766974693a64656c656761746545787072657373696f6e3d22247b43616e63656c4e6f6d696e61746564496e7669746544656c65676174657d22202f3e0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f77392220736f757263655265663d2263616e63656c6c656422207461726765745265663d22656e6422202f3e0d0a2020202020200d0a202020202020203c757365725461736b2069643d22696e76697465416363657074656422206e616d653d22496e7669746174696f6e204163636570746564220d0a20202020202020202020202061637469766974693a666f726d4b65793d22696e77663a616363657074496e766974655461736b22203e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a0d0a2020202020203c73657175656e6365466c6f772069643d22666c6f7731302220736f757263655265663d22696e76697465416363657074656422207461726765745265663d22656e6422202f3e0d0a20202020202020200d0a202020202020203c757365725461736b2069643d22696e7669746552656a656374656422206e616d653d22496e7669746174696f6e2052656a6563746564220d0a20202020202020202020202061637469766974693a666f726d4b65793d22696e77663a72656a656374496e766974655461736b22203e0d0a2020202020202020202020203c657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020202020203c61637469766974693a7461736b4c697374656e6572206576656e743d226372656174652220636c6173733d226f72672e616c66726573636f2e7265706f2e776f726b666c6f772e61637469766974692e7461736b6c697374656e65722e5363726970745461736b4c697374656e6572223e0d0a2020202020202020202020202020202020203c61637469766974693a6669656c64206e616d653d22736372697074223e0d0a2020202020202020202020202020202020202020203c61637469766974693a737472696e673e0d0a20202020202020202020202020202020202020202020202069662028747970656f662062706d5f776f726b666c6f775072696f7269747920213d2027756e646566696e65642729207461736b2e7072696f72697479203d2062706d5f776f726b666c6f775072696f726974793b0d0a2020202020202020202020202020202020202020203c2f61637469766974693a737472696e673e0d0a2020202020202020202020202020202020203c2f61637469766974693a6669656c643e0d0a2020202020202020202020202020203c2f61637469766974693a7461736b4c697374656e65723e0d0a2020202020202020202020203c2f657874656e73696f6e456c656d656e74733e0d0a2020202020202020202020203c68756d616e506572666f726d65723e0d0a202020202020202020202020202020203c7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a20202020202020202020202020202020202020203c666f726d616c45787072657373696f6e3e247b696e69746961746f722e6578697374732829203f20696e69746961746f722e70726f706572746965732e757365724e616d65203a202761646d696e277d3c2f666f726d616c45787072657373696f6e3e0d0a202020202020202020202020202020203c2f7265736f7572636541737369676e6d656e7445787072657373696f6e3e0d0a2020202020202020202020203c2f68756d616e506572666f726d65723e0d0a20202020202020203c2f757365725461736b3e0d0a20202020202020200d0a20202020202020202020202020203c73657175656e6365466c6f772069643d22666c6f7731312220736f757263655265663d22696e7669746552656a656374656422207461726765745265663d22656e6422202f3e0d0a20202020202020200d0a2020202020203c656e644576656e742069643d22656e6422202f3e0d0a2020203c2f70726f636573733e0d0a0d0a3c2f646566696e6974696f6e733e	f
28	1	afe12a68-638d-4157-8ecb-8dfa2139ab8cbpmn20.xml	27	\\x3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220a202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e63652220786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220a202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f44492220786d6c6e733a6f6d6764633d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f4443220a202020786d6c6e733a6f6d6764693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f44442f32303130303532342f44492220747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220a20202065787072657373696f6e4c616e67756167653d22687474703a2f2f7777772e77332e6f72672f313939392f585061746822207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f72672f776f726b666c6f77732f696e7465726e616c223e0a0a2020203c70726f6365737320697345786563757461626c653d2274727565222069643d226163746976697469496e7669746174696f6e4e6f6d696e6174656441646444697265637422206e616d653d22416464207573657220616374697669746920696e7669746174696f6e2070726f63657373223e0a0a2020202020203c73746172744576656e742069643d227374617274222061637469766974693a666f726d4b65793d22696e77663a696e76697465546f536974655461736b22202f3e0a0a2020202020203c73657175656e6365466c6f772069643d22666c6f77312220736f757263655265663d22737461727422207461726765745265663d22616363657074656422202f3e0a2020202020200a2020202020203c736572766963655461736b2069643d22616363657074656422206e616d653d224163636570746564222061637469766974693a64656c656761746545787072657373696f6e3d22247b4163636570744e6f6d696e61746564496e7669746544656c65676174657d22202f3e0a2020202020203c73657175656e6365466c6f772069643d22666c6f77322220736f757263655265663d22616363657074656422207461726765745265663d2273656e64496e7669746522202f3e0a2020202020200a2020202020203c736572766963655461736b2069643d2273656e64496e7669746522206e616d653d2253656e6420496e76697465222061637469766974693a64656c656761746545787072657373696f6e3d22247b53656e644e6f6d696e61746564496e7669746541646444697265637444656c65676174657d22202f3e0a2020202020200a2020202020203c73657175656e6365466c6f772069643d22666c6f77332220736f757263655265663d2273656e64496e7669746522207461726765745265663d22656e6422202f3e0a0a2020202020203c656e644576656e742069643d22656e6422202f3e0a2020203c2f70726f636573733e0a0a3c2f646566696e6974696f6e733e	f
31	1	832ee5db-4f49-4f9c-a81a-82cd590f4340bpmn20.xml	30	\\x3c3f786d6c2076657273696f6e3d22312e302220656e636f64696e673d225554462d3822203f3e0a0a3c646566696e6974696f6e7320786d6c6e733d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4d4f44454c220a20202020202020202020202020786d6c6e733a7873693d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d612d696e7374616e6365220a20202020202020202020202020786d6c6e733a61637469766974693d22687474703a2f2f61637469766974692e6f72672f62706d6e220a20202020202020202020202020786d6c6e733a62706d6e64693d22687474703a2f2f7777772e6f6d672e6f72672f737065632f42504d4e2f32303130303532342f4449220a20202020202020202020202020747970654c616e67756167653d22687474703a2f2f7777772e77332e6f72672f323030312f584d4c536368656d61220a202020202020202020202020207461726765744e616d6573706163653d22687474703a2f2f616c66726573636f2e6f72672f776f726b666c6f77732f696e7465726e616c223e0a0a202020203c70726f6365737320697345786563757461626c653d2274727565222069643d22726573657450617373776f726422206e616d653d224f6e205072656d6973652052657365742050617373776f72642070726f63657373223e0a0a20202020202020203c73746172744576656e742069643d227374617274222061637469766974693a666f726d4b65793d22726573657470617373776f726477663a7265717565737450617373776f726452657365745461736b222f3e0a0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77312220736f757263655265663d22737461727422207461726765745265663d2273656e64526573657450617373776f7264456d61696c5461736b222f3e0a0a20202020202020203c736572766963655461736b2069643d2273656e64526573657450617373776f7264456d61696c5461736b22206e616d653d2253656e642052657365742050617373776f726420456d61696c222061637469766974693a64656c656761746545787072657373696f6e3d22247b53656e64526573657450617373776f7264456d61696c44656c65676174657d220a20202020202020202020202020202020202020202061637469766974693a666f726d4b65793d22726573657470617373776f726477663a73656e64526573657450617373776f7264456d61696c5461736b222f3e0a0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77322220736f757263655265663d2273656e64526573657450617373776f7264456d61696c5461736b22207461726765745265663d22726573657450617373776f72645461736b222f3e0a0a20202020202020203c212d2d205468652070617373776f726420726573657420686173206265656e2072657175657374656420616e64206973206e6f772077616974696e6720666f7220746865207573657220746f20636f6d706c65746520627920636c69636b696e67206f6e20746865206c696e6b20696e2074686520656d61696c2e202d2d3e0a20202020202020203c212d2d204e6f7465207468617420776520646f206e6f742073746f7265207468652070617373776f726420617320616e20657865637574696f6e207661726961626c6520666f7220736563757269747920726561736f6e732e202d2d3e0a20202020202020203c757365725461736b2069643d22726573657450617373776f72645461736b22206e616d653d2250617373776f72642052657365742050656e64696e67222061637469766974693a666f726d4b65793d22726573657470617373776f726477663a726573657450617373776f72645461736b222f3e0a0a20202020202020203c212d2d204166746572202773797374656d2e72657365742d70617373776f72642e656e6454696d657227206f662077616974696e6720666f72207573657220746f2072657365742070617373776f72642c20656e64207468652070726f63657373202d2d3e0a20202020202020203c626f756e646172794576656e742069643d22656e6450726f6365737354696d6572222063616e63656c41637469766974793d227472756522206174746163686564546f5265663d22726573657450617373776f72645461736b223e0a2020202020202020202020203c74696d65724576656e74446566696e6974696f6e3e0a202020202020202020202020202020203c74696d654475726174696f6e3e247b726573657470617373776f726477665f656e6454696d65727d3c2f74696d654475726174696f6e3e0a2020202020202020202020203c2f74696d65724576656e74446566696e6974696f6e3e0a20202020202020203c2f626f756e646172794576656e743e0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77332220736f757263655265663d22656e6450726f6365737354696d657222207461726765745265663d2265787069726564222f3e0a0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77342220736f757263655265663d22726573657450617373776f72645461736b22207461726765745265663d22706572666f726d526573657450617373776f7264222f3e0a0a20202020202020203c212d2d20546865207573657220686173207375626d697474656420746865206e6563657373617279206461746120746f207265736574207468652070617373776f72642e202d2d3e0a20202020202020203c736572766963655461736b2069643d22706572666f726d526573657450617373776f726422206e616d653d22506572666f726d2052657365742050617373776f7264222061637469766974693a64656c656761746545787072657373696f6e3d22247b506572666f726d526573657450617373776f726444656c65676174657d220a20202020202020202020202020202020202020202061637469766974693a666f726d4b65793d22726573657470617373776f726477663a70617373776f72645265736574222f3e0a0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77352220736f757263655265663d22706572666f726d526573657450617373776f726422207461726765745265663d2273656e64526573657450617373776f7264436f6e6669726d6174696f6e456d61696c5461736b222f3e0a0a20202020202020203c736572766963655461736b2069643d2273656e64526573657450617373776f7264436f6e6669726d6174696f6e456d61696c5461736b22206e616d653d2253656e642052657365742050617373776f726420436f6e6669726d6174696f6e20456d61696c222061637469766974693a64656c656761746545787072657373696f6e3d22247b53656e64526573657450617373776f7264436f6e6669726d6174696f6e456d61696c44656c65676174657d220a20202020202020202020202020202020202020202061637469766974693a666f726d4b65793d22726573657470617373776f726477663a73656e64526573657450617373776f7264436f6e6669726d6174696f6e456d61696c5461736b222f3e0a0a20202020202020203c73657175656e6365466c6f772069643d22666c6f77362220736f757263655265663d2273656e64526573657450617373776f7264436f6e6669726d6174696f6e456d61696c5461736b22207461726765745265663d22656e64222f3e0a0a20202020202020203c656e644576656e742069643d22656e64222f3e0a20202020202020203c656e644576656e742069643d2265787069726564222f3e0a202020203c2f70726f636573733e0a0a3c2f646566696e6974696f6e733e0a	f
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
1	adhoc.bpmn20.xml	\N		2021-04-21 12:37:44.1
5	review.bpmn20.xml	\N		2021-04-21 12:37:44.48
9	review-pooled.bpmn20.xml	\N		2021-04-21 12:37:44.541
13	parallel-review.bpmn20.xml	\N		2021-04-21 12:37:44.605
17	parallel-review-group.bpmn20.xml	\N		2021-04-21 12:37:44.665
21	invitation-moderated.bpmn20.xml	http://alfresco.org/workflows/internal		2021-04-21 12:37:44.717
24	invitation-nominated.bpmn20.xml	http://alfresco.org/workflows/internal		2021-04-21 12:37:44.738
27	invitation-add-direct.bpmn20.xml	http://alfresco.org/workflows/internal		2021-04-21 12:37:44.754
30	reset-password_processdefinition.bpmn20.xml	http://alfresco.org/workflows/internal		2021-04-21 12:37:44.769
\.


--
-- Data for Name: act_re_model; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.act_re_model (id_, rev_, name_, key_, category_, create_time_, last_update_time_, version_, meta_info_, deployment_id_, editor_source_value_id_, editor_source_extra_value_id_, tenant_id_) FROM stdin;
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
21	0	1	12	t	0	\N
22	0	8	13	t	0	\N
23	0	9	14	t	0	\N
24	0	10	15	t	0	\N
25	0	11	16	t	0	\N
\.


--
-- Data for Name: alf_access_control_list; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_access_control_list (id, version, acl_id, latest, acl_version, inherits, inherits_from, type, inherited_acl, is_versioned, requires_version, acl_change_set) FROM stdin;
6	3	dfd9a556-6310-4d01-9eca-0a2193e67333	t	1	t	\N	1	60	f	f	7
1	2	d1cb718f-75ba-4c87-8de3-1d71f6d975a2	t	1	t	\N	1	2	f	f	1
2	3	359ed300-3ea3-468d-989f-425fb1fd32d3	t	1	t	1	2	2	f	f	1
4	3	662ca0e1-d329-4762-97ae-b9f9f308c157	t	1	t	3	2	4	f	f	8
7	2	3d6d4657-b0b9-437e-bad8-6f5de86be334	t	1	t	\N	1	61	f	f	9
3	2	5df4397d-8f24-454b-ba29-af5ae5a2e3c9	t	1	t	\N	1	4	f	f	2
5	2	2197bbcd-55be-4dd8-be0d-3f350ddf57f4	t	1	t	\N	1	\N	f	f	3
61	3	1c3a012f-e0ae-4836-9034-5a3321c75075	t	1	t	7	2	61	f	f	9
10	7	4f3151e8-7d53-406e-8f56-18573927e91b	t	1	f	9	1	11	f	f	6
11	5	76dc5cdf-fb36-414e-8a3f-4747080a24df	t	1	t	10	2	11	f	f	6
12	7	f5cf0b6c-9bec-4eb9-87fb-5b87b31ce036	t	1	f	11	1	13	f	f	6
13	5	7b73a10e-6ed5-46e1-8ffe-fabd36a82bfd	t	1	t	12	2	13	f	f	6
14	4	adf1652b-ddb3-4080-8865-52867cff56b4	t	1	t	13	1	15	f	f	6
15	3	5a96b153-f54f-41f5-b91d-e7d3c9fbb8ef	t	1	t	14	2	\N	f	f	6
16	7	67736f79-bd42-429c-8173-318220ee8d8e	t	1	f	13	1	17	f	f	6
17	4	2b986e73-4753-4cff-967c-65e658f26c8d	t	1	t	16	2	\N	f	f	6
69	8	9aa53957-6049-431e-8553-26d40b80118b	t	1	t	68	2	69	f	f	12
66	11	31b34b0e-0f6a-41fa-9857-dd7ae9e15048	t	1	f	11	1	67	f	f	11
67	9	1558c69e-d300-4d75-adb0-09bac1ece231	t	1	t	66	2	67	f	f	11
18	7	d71c487d-1723-4e7b-aa18-364d535c9b01	t	1	f	13	1	19	f	f	6
19	4	66d407c9-826d-4b5c-afd9-6ec66b0ddc59	t	1	t	18	2	\N	f	f	6
62	5	f8047a73-299e-4f04-803f-7f87fe71fc6e	t	1	t	27	1	63	f	f	10
63	4	d42991ae-f8d8-4668-820b-ffd226c4969d	t	1	t	62	2	\N	f	f	10
20	7	fd7ca347-071b-427b-b9d3-faea7d75c491	t	1	f	13	1	21	f	f	6
21	4	359658f4-00f4-4602-98ca-9de8829c0ec7	t	1	t	20	2	\N	f	f	6
64	8	ed8699da-9f21-4fb8-888a-e2cf167e8c73	t	1	f	11	1	65	f	f	10
65	5	5c13de61-0617-46f2-9414-24a234ad185a	t	1	t	64	2	\N	f	f	10
22	8	eb0e7cef-2469-4ca3-9a51-72e00ff63a85	t	1	f	11	1	23	f	f	6
23	5	8c4788b0-4aba-4923-822a-9e028c52ca20	t	1	t	22	2	\N	f	f	6
24	7	f1471a2c-240d-4e83-8b29-fa97e591720d	t	1	f	11	1	25	f	f	6
25	4	d0c87994-ca3e-4fb9-90cb-30954934a67c	t	1	t	24	2	\N	f	f	6
8	4	83c64008-84de-42f3-9b35-c9012be32141	t	1	t	\N	1	9	f	f	6
9	5	5fbd63e7-f0bd-472e-8483-eb864e2ea522	t	1	t	8	2	9	f	f	6
68	10	a3b9832e-cac0-457b-b23f-25129d53810c	t	1	f	67	1	69	f	f	11
26	7	de20ee80-86e8-4c2b-b65d-1036bb4138f3	t	1	f	9	1	27	f	f	6
27	5	0ba3b10c-1f04-4ca3-ad03-c8748c3661e0	t	1	t	26	2	27	f	f	6
28	5	005569e4-6b4b-4524-a974-d75c2f145b5a	t	1	t	27	1	29	f	f	6
29	4	496eeb41-2c0a-4cbd-8920-70d93798de8b	t	1	t	28	2	\N	f	f	6
30	4	58adf9a8-0bcb-43c1-af09-ca7be96b1d51	t	1	t	27	1	31	f	f	6
31	3	8cd99148-49c4-48b4-9c21-d2a23e083ff0	t	1	t	30	2	\N	f	f	6
32	4	264b3966-8ddb-4b3e-bce2-48cbee22d3a9	t	1	t	9	1	33	f	f	6
33	3	9a6af618-e2f8-4c70-879b-639954df6428	t	1	t	32	2	\N	f	f	6
34	7	846f181e-b0c6-44ae-89d9-81b5b5cf5318	t	1	f	9	1	35	f	f	6
35	4	ab8de5fb-7a27-409d-80b4-cd00fc361d27	t	1	t	34	2	\N	f	f	6
36	7	bb683491-cbb9-4cb0-a3bf-e59bcb8a9540	t	1	f	9	1	37	f	f	6
37	4	ce969edd-7122-453b-9ec1-a72950a221f9	t	1	t	36	2	\N	f	f	6
38	4	2f5855d7-7b2d-4f9f-813d-cb3d67325408	t	1	t	11	1	39	f	f	6
39	3	e5fa858f-d613-49d0-b218-2b083234e11e	t	1	t	38	2	\N	f	f	6
40	4	bc0a4c16-6985-4699-ab88-2c214942fe21	t	1	t	11	1	41	f	f	6
41	3	27fe2b66-c9dd-48ad-923f-b28bd638ec7d	t	1	t	40	2	\N	f	f	6
42	4	c1fccaa9-9365-4377-b00e-c63a26d84b45	t	1	t	27	1	43	f	f	6
43	3	0ba8d333-f6df-4ce3-815f-e81795af6279	t	1	t	42	2	\N	f	f	6
44	4	4e5cb4c9-ccde-4d69-9eb3-2867462e2878	t	1	t	27	1	45	f	f	6
45	3	f204bb01-1ab0-4766-aa18-8b7a92fa77eb	t	1	t	44	2	\N	f	f	6
46	9	8dd51589-533a-45b7-9561-96ede8d42ec8	t	1	f	27	1	47	f	f	6
47	6	e3b25dc2-cc67-464d-82dd-6ac4e37cf097	t	1	t	46	2	\N	f	f	6
54	5	87255089-d6d8-41b9-967d-bb6143c0b719	t	1	t	27	1	55	f	f	7
48	9	70693e60-c837-4f02-9870-16b1b3b9fd63	t	1	f	27	1	49	f	f	6
49	6	802160b8-6682-4214-9859-cde2fa24d3f2	t	1	t	48	2	\N	f	f	6
55	5	b6ff04c5-0889-40ea-88f7-51dbc18bb886	t	1	t	54	2	55	f	f	7
50	8	941205dd-8e9d-49cd-b730-aff6519ffb17	t	1	f	27	1	51	f	f	6
51	5	0da7edb6-2710-4b11-b5e6-aeba16e45e69	t	1	t	50	2	\N	f	f	6
53	9	9a949602-0091-46e2-93ee-960e608b26b4	t	1	t	52	2	\N	f	f	7
56	5	f40f8f30-ba33-4df0-8588-16fdaa2a350f	t	1	t	27	1	57	f	f	7
57	5	fc7fa09e-bd70-4474-9452-8370667bba00	t	1	t	56	2	57	f	f	7
58	12	7c2974b1-6545-4b73-b454-c123da0ff727	t	1	f	11	1	59	f	f	7
59	10	521a7772-2a9c-4c5a-bd27-185ad45cabe8	t	1	t	58	2	59	f	f	7
60	3	cd22f4b4-3c7b-422a-99ae-9ff3b2492548	t	1	t	6	2	60	f	f	7
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
1	1619008660347
2	1619008660426
3	1619008660462
4	1619008660485
5	1619008660497
6	1619008663869
7	1619008668788
8	1619008669005
9	1619008877477
10	1619008915421
11	1619008915905
12	1619008916829
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
152	0	62	5	2
153	0	63	5	3
154	0	62	8	0
155	0	63	8	1
156	0	62	21	0
157	0	63	21	1
160	0	64	8	0
161	0	65	8	1
162	0	64	21	0
163	0	65	21	1
166	0	66	22	0
167	0	67	22	1
168	0	66	23	0
169	0	67	23	1
170	0	66	24	0
171	0	67	24	1
172	0	66	25	0
173	0	67	25	1
174	0	66	18	0
175	0	67	18	1
186	0	68	22	0
187	0	69	22	1
188	0	68	23	0
189	0	69	23	1
190	0	68	24	0
191	0	69	24	1
192	0	68	25	0
193	0	69	25	1
\.


--
-- Data for Name: alf_activity_feed; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_activity_feed (id, post_id, post_date, activity_summary, feed_user_id, activity_type, site_network, app_tool, post_user_id, feed_date) FROM stdin;
1	1	2021-04-21 12:41:56.402	{"firstName":"FN-UserSearch-zGiNtcNRnaJPlLO","lastName":"LN-UserSearch-zGiNtcNRnaJPlLO","parentNodeRef":"workspace://SpacesStore/69858ec8-5a33-4ace-9c9d-bf7c25d8540a","nodeRef":"workspace://SpacesStore/d96c1efe-678b-4d37-ae8e-1a7071bfe857","page":"documentlibrary?path=/folder-TNiecQUWtKwqYRo","title":"folder-TNiecQUWtKwqYRo"}	usersearch-zgintcnrnajpllo	org.alfresco.documentlibrary.folder-added	SiteSearch-cZXBaCaYHqqynWr	CMIS	usersearch-zgintcnrnajpllo	2021-04-21 12:42:30.343
2	2	2021-04-21 12:41:56.828	{"firstName":"FN-UserSearch-zGiNtcNRnaJPlLO","lastName":"LN-UserSearch-zGiNtcNRnaJPlLO","parentNodeRef":"workspace://SpacesStore/d96c1efe-678b-4d37-ae8e-1a7071bfe857","nodeRef":"workspace://SpacesStore/a170644e-906e-427c-8909-99b62d445246","page":"document-details?nodeRef=workspace://SpacesStore/a170644e-906e-427c-8909-99b62d445246","title":"file-GZzBkqpJQZrNHpg.txt"}	usersearch-zgintcnrnajpllo	org.alfresco.documentlibrary.file-added	SiteSearch-cZXBaCaYHqqynWr	CMIS	usersearch-zgintcnrnajpllo	2021-04-21 12:42:30.354
3	3	2021-04-21 12:41:57.082	{"firstName":"FN-UserSearch-zGiNtcNRnaJPlLO","lastName":"LN-UserSearch-zGiNtcNRnaJPlLO","parentNodeRef":"workspace://SpacesStore/d96c1efe-678b-4d37-ae8e-1a7071bfe857","nodeRef":"workspace://SpacesStore/7b0ca868-9d44-4f54-be37-3565f3a0b0fb","page":"document-details?nodeRef=workspace://SpacesStore/7b0ca868-9d44-4f54-be37-3565f3a0b0fb","title":"file-ksihTJbQvNShWkz.txt"}	usersearch-zgintcnrnajpllo	org.alfresco.documentlibrary.file-added	SiteSearch-cZXBaCaYHqqynWr	CMIS	usersearch-zgintcnrnajpllo	2021-04-21 12:42:30.36
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
1	2021-04-21 12:41:56.402	PROCESSED	{"firstName":"FN-UserSearch-zGiNtcNRnaJPlLO","lastName":"LN-UserSearch-zGiNtcNRnaJPlLO","parentNodeRef":"workspace://SpacesStore/69858ec8-5a33-4ace-9c9d-bf7c25d8540a","nodeRef":"workspace://SpacesStore/d96c1efe-678b-4d37-ae8e-1a7071bfe857","page":"documentlibrary?path=/folder-TNiecQUWtKwqYRo","title":"folder-TNiecQUWtKwqYRo"}	usersearch-zgintcnrnajpllo	1	SiteSearch-cZXBaCaYHqqynWr	CMIS	org.alfresco.documentlibrary.folder-added	2021-04-21 12:42:30.346
2	2021-04-21 12:41:56.828	PROCESSED	{"firstName":"FN-UserSearch-zGiNtcNRnaJPlLO","lastName":"LN-UserSearch-zGiNtcNRnaJPlLO","parentNodeRef":"workspace://SpacesStore/d96c1efe-678b-4d37-ae8e-1a7071bfe857","nodeRef":"workspace://SpacesStore/a170644e-906e-427c-8909-99b62d445246","page":"document-details?nodeRef=workspace://SpacesStore/a170644e-906e-427c-8909-99b62d445246","title":"file-GZzBkqpJQZrNHpg.txt"}	usersearch-zgintcnrnajpllo	1	SiteSearch-cZXBaCaYHqqynWr	CMIS	org.alfresco.documentlibrary.file-added	2021-04-21 12:42:30.355
3	2021-04-21 12:41:57.082	PROCESSED	{"firstName":"FN-UserSearch-zGiNtcNRnaJPlLO","lastName":"LN-UserSearch-zGiNtcNRnaJPlLO","parentNodeRef":"workspace://SpacesStore/d96c1efe-678b-4d37-ae8e-1a7071bfe857","nodeRef":"workspace://SpacesStore/7b0ca868-9d44-4f54-be37-3565f3a0b0fb","page":"document-details?nodeRef=workspace://SpacesStore/7b0ca868-9d44-4f54-be37-3565f3a0b0fb","title":"file-ksihTJbQvNShWkz.txt"}	usersearch-zgintcnrnajpllo	1	SiteSearch-cZXBaCaYHqqynWr	CMIS	org.alfresco.documentlibrary.file-added	2021-04-21 12:42:30.361
\.


--
-- Data for Name: alf_applied_patch; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_applied_patch (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report) FROM stdin;
patch.db-V5.0-upgrade-to-activiti-5.16.2	patch.db-V5.0-upgrade-to-activiti-5.16.2.description	0	8003	-1	8004	2021-04-21 12:37:38.631	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V5.0-upgrade-to-activiti-5.16.4	patch.db-V5.0-upgrade-to-activiti-5.16.4.description	0	8008	-1	8009	2021-04-21 12:37:38.651	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V5.0-remove-columns-after-upgrade-to-activiti-5.16.2	patch.db-V5.0-remove-columns-after-upgrade-to-activiti-5.16.2.description	0	9002	-1	9003	2021-04-21 12:37:38.652	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V5.0-update-activiti-default-timestamp-column	patch.db-V5.0-update-activiti-default-timestamp-column.description	0	9012	-1	9013	2021-04-21 12:37:38.654	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V5.0-activiti-correct-tenant-id-MSSQL	patch.db-V5.0-activiti-correct-tenant-id-MSSQL.description	0	9016	-1	9017	2021-04-21 12:37:38.655	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V5.1-upgrade-to-activiti-5.19.0	patch.db-V5.1-upgrade-to-activiti-5.19.0	0	9013	-1	9014	2021-04-21 12:37:38.656	UNKNOWN	f	t	Placeholder for Activiti bootstrap at schema -1
patch.db-V3.4-UsageTables	Manually executed script upgrade V3.4: Usage Tables	0	113	-1	114	2021-04-21 12:37:46.578	UNKNOWN	t	t	Script completed
patch.db-V4.0-TenantTables	Manually executed script upgrade V4.0: Tenant Tables	0	6004	-1	6005	2021-04-21 12:37:46.579	UNKNOWN	t	t	Script completed
patch.db-V4.1-AuthorizationTables	Manually executed script upgrade V4.1: Authorization status tables	0	6075	-1	6076	2021-04-21 12:37:46.58	UNKNOWN	t	t	Script completed
patch.db-V5.0-ContentUrlEncryptionTables	Manually executed script upgrade V5.0: Content Url Encryption Tables	0	8001	-1	8002	2021-04-21 12:37:46.581	UNKNOWN	t	t	Script completed
patch.savedSearchesFolder	Ensures the existence of the 'Saved Searches' folder.	0	1	15000	2	2021-04-21 12:37:46.586	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updatePermissionData	Update permissions from 'folder' to 'cmobject' [JIRA: AR-344].	0	2	15000	3	2021-04-21 12:37:46.588	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.guestUser	Add the guest user, guest home space; and fix permissions on company home, guest home and guest person. 	0	2	15000	3	2021-04-21 12:37:46.59	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.fixNodeSerializableValues	Ensure that property values are not stored as Serializable if at all possible	0	3	15000	4	2021-04-21 12:37:46.591	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.savedSearchesPermission	Sets required permissions on 'Saved Searches' folder.	0	4	15000	5	2021-04-21 12:37:46.593	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateGuestPermission	Rename guest permission from 'Guest' to 'Consumer'	0	5	15000	6	2021-04-21 12:37:46.594	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.guestPersonPermission	Change Guest Person permission from 'Consumer' to 'Read'	0	5	15000	6	2021-04-21 12:37:46.597	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.spacesRootPermission	Change Spaces store root permission from 'Consumer' to 'Read'	0	5	15000	6	2021-04-21 12:37:46.599	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.categoryRootPermission	Sets required permissions on 'Category Root' folder.	0	5	15000	6	2021-04-21 12:37:46.6	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.contentPermission	Update permission entries from 'cm:content' to 'sys:base'.	0	6	15000	7	2021-04-21 12:37:46.602	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.descriptorUpdate	Update Repository descriptor	0	11	15000	12	2021-04-21 12:37:46.604	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.forumsIcons	Updates forums icon references	0	12	15000	13	2021-04-21 12:37:46.605	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.emailTemplatesFolder	Ensures the existence of the 'Email Templates' folder.	0	12	15000	13	2021-04-21 12:37:46.607	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.emailTemplatesContent	Loads the email templates into the Email Templates folder.	0	12	15000	13	2021-04-21 12:37:46.608	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.scriptsFolder	Ensures the existence of the 'Scripts' folder.	0	12	15000	13	2021-04-21 12:37:46.609	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.topLevelGroupParentChildAssociationTypePatch	Ensure top level groups have the correct child association type.	0	13	15000	14	2021-04-21 12:37:46.611	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.actionRuleDecouplingPatch	Migrate existing rules to the updated model where rules are decoupled from actions.	0	14	15000	15	2021-04-21 12:37:46.612	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.systemWorkflowFolderPatch	Ensures the existence of the system workflow container.	0	15	15000	16	2021-04-21 12:37:46.613	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.rssFolder	Ensures the existence of the 'RSS Templates' folder.	0	16	15000	17	2021-04-21 12:37:46.614	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.uifacetsTemplates	Removes the incorrectly applied uifacets aspect from presentation template files.	0	17	15000	18	2021-04-21 12:37:46.616	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.guestPersonPermission2	Change Guest Person permission to visible by all users as 'Consumer'.	0	18	15000	19	2021-04-21 12:37:46.617	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.schemaUpdateScript-V1.4-1	Ensures that the database upgrade script has been run.	0	19	15000	20	2021-04-21 12:37:46.618	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.uniqueChildName	Checks and renames duplicate children.	0	19	15000	20	2021-04-21 12:37:46.62	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.schemaUpdateScript-V1.4-2	Ensures that the database upgrade script has been run.	0	20	15000	21	2021-04-21 12:37:46.621	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.InvalidNameEnding	Fixes names ending with a space or full stop.	0	21	15000	22	2021-04-21 12:37:46.622	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.systemDescriptorContent	Adds the version properties content to the system descriptor.	0	22	15000	23	2021-04-21 12:37:46.623	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.multilingualBootstrap	Bootstraps the node that will hold the multilingual containers.	0	29	15000	30	2021-04-21 12:37:46.625	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.LinkNodeFileExtension	Fixes link node file extensions to have a .url extension.	0	33	15000	34	2021-04-21 12:37:46.626	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.systemRegistryBootstrap	Bootstraps the node that will hold system registry metadata.	0	34	15000	35	2021-04-21 12:37:46.627	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.userAndPersonUserNamesAsIdentifiers	Reindex usr:user and cm:person uids as identifiers	0	35	15000	36	2021-04-21 12:37:46.629	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.contentFormFolderType	Update WCM Content Form folder type.	0	36	15000	37	2021-04-21 12:37:46.63	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.versionHistoryPerformance	Improves the performance of version history lookups.	0	38	15000	39	2021-04-21 12:37:46.631	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.webscripts	Adds Web Scripts to Data Dictionary.	0	50	15000	51	2021-04-21 12:37:46.632	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.1-JBPMUpdate	Ensures that the database upgrade script has been run.	0	51	15000	52	2021-04-21 12:37:46.634	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.1-NotNullColumns	Ensures that the database upgrade script has been run.	0	51	15000	52	2021-04-21 12:37:46.635	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.groupNamesAsIdentifiers	Reindex usr:authorityContainer gids as identifiers	0	51	15000	52	2021-04-21 12:37:46.636	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.invalidUserPersonAndGroup	Fix up invalid uids for people and users; and invalid gids for groups	0	51	15000	52	2021-04-21 12:37:46.638	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.webscriptsExtension	Adds Web Scripts Extension to Data Dictionary.	0	54	15000	55	2021-04-21 12:37:46.639	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.groupMembersAsIdentifiers	Reindex usr:authorityContainer members as identifiers	0	56	15000	57	2021-04-21 12:37:46.64	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.redeploySubmitProcess	Re-deploy WCM Submit Process Definition.	0	57	15000	58	2021-04-21 12:37:46.641	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.ReadmeTemplate	Deployed ReadMe Template	0	59	15000	60	2021-04-21 12:37:46.643	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.webScriptsReadme	Applied ReadMe template to Web Scripts folders	0	59	15000	60	2021-04-21 12:37:46.644	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.1-JBPMProcessKey	Ensures that the database upgrade script has been run.	0	62	15000	63	2021-04-21 12:37:46.645	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.1-VersionColumns2	Ensures that the database upgrade script has been run.	0	63	15000	64	2021-04-21 12:37:46.646	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.2-CleanNodeStatuses	Ensures that the database upgrade script has been run.	0	89	15000	90	2021-04-21 12:37:46.648	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.webscripts2	Adds Web Scripts (second set) to Data Dictionary.	0	100	15000	101	2021-04-21 12:37:46.649	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.customModels	Adds 'Models' folder to Data Dictionary	0	101	15000	102	2021-04-21 12:37:46.65	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.customMessages	Adds 'Messages' folder to Data Dictionary	0	101	15000	102	2021-04-21 12:37:46.652	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.customWebClientExtension	Adds 'Web Client Extension' folder to Data Dictionary	0	101	15000	102	2021-04-21 12:37:46.653	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.webscripts3	Update Web Scripts ReadMe.	0	104	15000	105	2021-04-21 12:37:46.656	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.customWorkflowDefs	Adds 'Workflow Definitions' folder to Data Dictionary.	0	105	15000	106	2021-04-21 12:37:46.657	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V1.4-TxnCommitTimeIndex	Ensures that the database upgrade script has been run.	0	110	15000	111	2021-04-21 12:37:46.658	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.formsFolder	Adds 'Forms' folder to Data Dictionary.	0	112	15000	113	2021-04-21 12:37:46.659	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.tagRootCategory	Adds 'Tags' as new top-level category root.	0	113	15000	114	2021-04-21 12:37:46.661	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.2-ACL-From-2.1-A	Ensures that the database upgrade script has been run.	0	119	15000	120	2021-04-21 12:37:46.662	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.2-ACL	Ensures that the database upgrade script has been run.	0	119	15000	120	2021-04-21 12:37:46.664	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.2-0-CreateMissingTables	Ensures that the database upgrade script has been run.	0	120	15000	121	2021-04-21 12:37:46.665	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.2-2-MoveQNames	A placeholder patch; usually marks a superceded patch.	0	120	15000	121	2021-04-21 12:37:46.666	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.0-ContentUrls	Ensures that the database upgrade script has been run.	0	123	15000	124	2021-04-21 12:37:46.667	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateDmPermissions	Update ACLs on all DM node objects to the new 3.0 permission model	0	124	15000	125	2021-04-21 12:37:46.669	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.0-0-CreateActivitiesExtras	Replaced by 'patch.db-V3.0-ActivityTables', which must run first.	0	125	15000	126	2021-04-21 12:37:46.67	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.0-ActivityTables	Ensures that the database upgrade script has been run.	0	125	15000	126	2021-04-21 12:37:46.671	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.createSiteStore	A placeholder patch; usually marks a superceded patch.	0	126	15000	127	2021-04-21 12:37:46.672	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.sitesFolder	Adds 'Sites' folder to Company Home.	0	127	15000	128	2021-04-21 12:37:46.674	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.sitePermissionRefactorPatch	Create permission groups for sites.	0	128	15000	129	2021-04-21 12:37:46.675	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.migrateVersionStore	Version Store migration (from lightWeightVersionStore to version2Store)	0	129	15000	130	2021-04-21 12:37:46.676	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.inviteEmailTemplate	Adds invite email template to invite space	0	130	15000	131	2021-04-21 12:37:46.677	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.calendarNamespaceUri	Update the Calendar model namespace URI and reindex all calendar objects.	0	131	15000	132	2021-04-21 12:37:46.679	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.1-AuditPathIndex	Ensures that the database upgrade script has been run.	0	132	15000	133	2021-04-21 12:37:46.68	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.spacesStoreGuestPermission	Sets READ permissions for GUEST on root node of the SpacesStore.	0	133	15000	134	2021-04-21 12:37:46.681	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.2-Upgrade-From-2.1	Ensures that the database upgrade script has been run.	0	120	15000	135	2021-04-21 12:37:46.682	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.2-Upgrade-From-2.2SP1	Ensures that the database upgrade script has been run.	0	134	15000	135	2021-04-21 12:37:46.683	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.webSiteAddModerated	Changing Web Site visibility from a boolean to enum.	0	2006	15000	2007	2021-04-21 12:37:46.684	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.invitationMigration	Migrate invitations from old invite service to invitation service	0	2006	15000	2007	2021-04-21 12:37:46.686	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.mtShareExistingTenants	Update existing tenants for MT Share.	0	2008	15000	2009	2021-04-21 12:37:46.687	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.redeployInvitationProcess	Re-deploy Invitation Process Definitions.	0	2009	15000	2010	2021-04-21 12:37:46.688	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.2-LockTables	Ensures that the database upgrade script has been run.	0	2010	15000	2011	2021-04-21 12:37:46.69	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.zonedAuthorities	Adds the remodelled cm:authority container to the spaces store	0	2011	15000	2012	2021-04-21 12:37:46.691	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.authorityMigration	Copies any old authorities from the user store to the spaces store.	0	2012	15000	2013	2021-04-21 12:37:46.693	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.authorityDefaultZonesPatch	Adds groups and people to the appropriate zones for share and everything else.	0	2013	15000	2014	2021-04-21 12:37:46.694	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.2-ContentTables	Ensures that the database upgrade script has been run.	0	2015	15000	2016	2021-04-21 12:37:46.695	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.4-Upgrade-JBPM	Ensures that the database upgrade script has been run.	0	2017	15000	2018	2021-04-21 12:37:46.697	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.2-Upgrade-JBPM	A placeholder patch; usually marks a superceded patch.	0	2017	15000	2018	2021-04-21 12:37:46.698	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.imapFolders	Creates folders tree necessary for IMAP functionality	0	2018	15000	2019	2021-04-21 12:37:46.7	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.2-PropertyValueTables	Ensures that the database upgrade script has been run.	0	3000	15000	3001	2021-04-21 12:37:46.701	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.2-AuditTables	Ensures that the database upgrade script has been run.	0	3001	15000	3002	2021-04-21 12:37:46.703	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V2.2-Person-3	Ensures that the database upgrade script has been run.	0	3002	15000	3003	2021-04-21 12:37:46.705	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.1-Allow-IPv6	Ensures that the database upgrade script has been run.	0	3003	15000	3004	2021-04-21 12:37:46.706	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.personUsagePatch	Add person 'cm:sizeCurrent' property (if missing).	0	3004	15000	3005	2021-04-21 12:37:46.707	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.2-Child-Assoc-QName-CRC	Ensures that the database upgrade script has been run.	0	3005	15000	3006	2021-04-21 12:37:46.709	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.fixNameCrcValues-2	Fixes name and qname CRC32 values to match UTF-8 encoding.	0	3006	15000	3007	2021-04-21 12:37:46.71	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.redeployNominatedInvitationProcessWithPropsForShare	Redeploy nominated invitation workflow	0	4000	15000	4001	2021-04-21 12:37:46.711	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.2-ContentTables2	Ensures that the database upgrade script has been run.	0	4001	15000	4002	2021-04-21 12:37:46.712	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.3-Remove-VersionCount	Ensures that the database upgrade script has been run.	0	4002	15000	4003	2021-04-21 12:37:46.713	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.rendition.rendering_actions	Creates the Rendering Actions folder.	0	4003	15000	4004	2021-04-21 12:37:46.715	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.thumbnailsAssocQName	Update the 'cm:thumbnails' association QName to 'rn:rendition'.	0	4004	15000	4005	2021-04-21 12:37:46.716	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.emailInviteAndNotifyTemplatesFolder	Ensures the existence of the 'Email Invite Templates' and 'Email Notify Templates' folders.	0	4006	15000	4007	2021-04-21 12:37:46.717	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.convertContentUrls	Converts pre-3.2 content URLs to use the alf_content_data table.  The conversion work can also be done on a schedule; please contact Alfresco Support for further details.	0	4007	15000	4008	2021-04-21 12:37:46.718	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.transferServiceFolder	Add transfer definitions folder to data dictionary.	0	4008	15000	4009	2021-04-21 12:37:46.719	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.4-authority-unique-idx	Ensures that the database upgrade script has been run.	0	4099	15000	4100	2021-04-21 12:37:46.72	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.fixAuthoritiesCrcValues	Fixes authority CRC32 values to match UTF-8 encoding.	0	4100	15000	4101	2021-04-21 12:37:46.721	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateMimetypes1	Fix mimetypes for Excel and Powerpoint.	0	4101	15000	4102	2021-04-21 12:37:46.722	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.3-modify-index-permission_id	Ensures that the database upgrade script has been run.	0	4102	15000	4103	2021-04-21 12:37:46.724	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.2-AddFKIndexes	Ensures that the database upgrade script has been run.	3007	4103	15000	4104	2021-04-21 12:37:46.726	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.3-Fix-Repo-Seqs	Ensures that the database upgrade script has been run.	0	4104	15000	4105	2021-04-21 12:37:46.727	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.4-property-unique-ctx-value	Ensures that the database upgrade script has been run.	0	4104	15000	4105	2021-04-21 12:37:46.729	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.4-property-unique-ctx-idx	Ensures that the database upgrade script has been run.	0	4104	15000	4105	2021-04-21 12:37:46.73	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.4-child-assoc-indexes	Ensures that the database upgrade script has been run.	0	4104	15000	4105	2021-04-21 12:37:46.732	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.3-JBPM-Extra	Ensures that the database upgrade script has been run.	0	4105	15000	4106	2021-04-21 12:37:46.733	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.3-Node-Prop-Serializable	Ensures that the database upgrade script has been run.	0	4105	15000	4106	2021-04-21 12:37:46.734	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.migrateAttrTenants	Migrate old Tenant attributes	0	4105	15000	4106	2021-04-21 12:37:46.736	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.migrateAttrPropBackedBeans	Migrate old Property-Backed Bean component attributes	0	4106	15000	4107	2021-04-21 12:37:46.737	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.migrateAttrChainingURS	Migrate old Chaining User Registry Synchronizer attributes	0	4106	15000	4107	2021-04-21 12:37:46.738	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.migrateAttrDelete	A placeholder patch; usually marks a superceded patch.	0	4106	15000	4107	2021-04-21 12:37:46.739	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.replication.replication_actions	Creates the Replication Actions folder.	0	4107	15000	4108	2021-04-21 12:37:46.741	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.transfer.targetrulefolder	Creates the transfer target rule folder for the default transfer group.	0	4108	15000	4109	2021-04-21 12:37:46.742	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.transfer.targetrule	Creates the transfer target rule for the default transfer group.	0	4108	15000	4109	2021-04-21 12:37:46.743	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.actions.scheduledfolder	Creates the scheduled actions folder in the Data Dictionary.	0	4109	15000	4110	2021-04-21 12:37:46.744	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateMimetypes2	Fix mimetypes for Excel and Powerpoint.	0	4110	15000	4111	2021-04-21 12:37:46.745	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.2-AddFKIndexes-2	Ensures that the database upgrade script has been run.	0	4111	15000	4112	2021-04-21 12:37:46.747	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.redeployJbpmAdhocWorkflow	Redeploy JBPM adhoc workflow	0	4204	15000	4205	2021-04-21 12:37:46.748	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.imapSpacesLocaleTemplates	A placeholder patch; usually marks a superceded patch.	0	4302	15000	4305	2021-04-21 12:37:46.75	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.mtFixAdminExistingTenants	Fix bootstrapped creator/modifier	0	5002	15000	5003	2021-04-21 12:37:46.751	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.fixUserQNames	Fixes user store qnames to improve native authentication performance	0	5003	15000	5004	2021-04-21 12:37:46.752	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.activitiesEmailTemplate	Creates activities email templates.	0	5005	15000	5006	2021-04-21 12:37:46.753	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.newUserEmailTemplates	Adds the email templates for notifying new users of their accounts	0	5005	15000	5006	2021-04-21 12:37:46.755	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.inviteEmailTemplates	Adds the email templates for inviting users to a Site	0	5005	15000	5006	2021-04-21 12:37:46.756	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.htmlNotificationMailTemplates	Adds HTML email templates for notifying users of new content	0	5005	15000	5006	2021-04-21 12:37:46.757	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.fixAclInheritance	Fixes any ACL inheritance issues.	0	5005	15000	5006	2021-04-21 12:37:46.758	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.4-JBPM-FK-indexes	Ensures that the database upgrade script has been run.	0	5005	15000	5006	2021-04-21 12:37:46.759	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.imap.clear.old.messages	Remove old IMAP message templates	0	5005	15000	5006	2021-04-21 12:37:46.76	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.imapSpacesTemplates2	Replaces content templates for IMAP clients	0	5005	15000	5006	2021-04-21 12:37:46.762	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.migrateAttrDropOldTables	Drops old alf_*attribute* tables and sequence	0	5006	15000	5007	2021-04-21 12:37:46.763	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.0-AclChangeSet	Ensures that the database upgrade script has been run.	0	5007	15000	5008	2021-04-21 12:37:46.765	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.0-NodeAssoc-Ordering	Ensures that the database upgrade script has been run.	0	5008	15000	5009	2021-04-21 12:37:46.766	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.0-Node-Locale	Ensures that the database upgrade script has been run.	0	5009	15000	5010	2021-04-21 12:37:46.767	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.activitiesTemplatesUpdate	Updates activities email templates.	0	5010	15000	5011	2021-04-21 12:37:46.768	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.followingMailTemplates	Adds email templates for following notifications	0	5010	15000	5011	2021-04-21 12:37:46.769	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.0-SubscriptionTables	Ensures that the database upgrade script has been run.	0	5010	15000	5011	2021-04-21 12:37:46.77	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.copiedFromAspect	Adds peer associations for cm:copiedfrom and cm:workingcopy (new model) and removes cm:source property	0	5012	15000	5013	2021-04-21 12:37:46.772	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.publishing.root	Creates the publishing root folder in the Data Dictionary	0	5013	15000	5014	2021-04-21 12:37:46.773	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.workflowNotification	Patch to add workflow email notification email folder and template.	0	5014	15000	5015	2021-04-21 12:37:46.774	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.nodeTemplatesFolder	Patch to create new Data Dictionary folder for Share - Create Node by Template	0	5015	15000	5016	2021-04-21 12:37:46.775	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateMimetypes3	Fix mimetype for MPEG Audio	0	5016	15000	5017	2021-04-21 12:37:46.776	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.sitesSpacePermissions	Patch to remove the EVERYONE Contributor permissions on the Sites Space (parent container of all Sites)	0	5017	15000	5018	2021-04-21 12:37:46.778	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateWorkflowNotificationTemplates	Patch to update the workflow notification templates.	0	5018	15000	5019	2021-04-21 12:37:46.779	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateMimetypes4	Rationalise mimetypes for PhotoShop and AutoCad	0	5019	15000	5020	2021-04-21 12:37:46.78	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateMimetypesWMA	Fix mimetype for MS WMA Streaming Audio	0	5020	15000	5021	2021-04-21 12:37:46.782	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateFollowingEmailTemplatesPatch	Patch to update the following notification email templates.	0	5021	15000	5022	2021-04-21 12:37:46.783	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.0-SolrTracking	Ensures that the database upgrade script has been run.	0	5022	15000	5023	2021-04-21 12:37:46.785	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.addDutchEmailTemplatesPatch	Patch to add Dutch email templates.	0	5023	15000	5024	2021-04-21 12:37:46.786	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.fixBpmPackages	Corrects workflow package types and associations 	0	5024	15000	5025	2021-04-21 12:37:46.787	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.4-alter-jBPM331-CLOB-columns-to-nvarchar	Altering CLOB columns in the jBPM 3.3.1 tables to introduce Unicode characters support for jBPM 3.3.1	0	6000	15000	6001	2021-04-21 12:37:46.788	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.imapUnsubscribedAspect	Patch to remove deprecated "imap:nonSubscribed" aspect from folders.	0	6001	15000	6002	2021-04-21 12:37:46.789	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.0-Activiti-task-id-indexes	Ensures that the database upgrade script has been run.	0	6003	15000	6004	2021-04-21 12:37:46.791	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.remoteCredentialsContainer	Patch to add the root folder for Shared Remote Credentials	0	6005	15000	6006	2021-04-21 12:37:46.794	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.syncSetDefinitionsContainer	Patch to add the root folder for SyncSet Definitions	0	6005	15000	6006	2021-04-21 12:37:46.796	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.migrateTenantsFromAttrsToTable	Migrate Tenant attributes to Tenant table	0	6006	15000	6007	2021-04-21 12:37:46.797	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.swsdpPatch	Patch to fix up the Sample: Web Site Design Project.	0	6007	15000	6008	2021-04-21 12:37:46.799	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.0-AclChangeSet2	Ensures that the database upgrade script has been run.	0	6008	15000	6009	2021-04-21 12:37:46.8	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.redeployParallelActivitiWorkflows	Patch that redeploys both parallel activiti workflows, completion-condition now takes into account if minimum approval percentage can still be achived.	0	6009	15000	6010	2021-04-21 12:37:46.801	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.4-remove-redundant-jbpm-indexes	Ensures that the database upgrade script has been run.	0	6010	15000	6011	2021-04-21 12:37:46.802	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.show.audit	Updates show_audit.ftl file for upgrade from v3.3.5 to v3.4.x (ALF-13929)	0	6011	15000	6012	2021-04-21 12:37:46.804	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.1-increase-column-size-activiti	ALF-14983 : Upgrade scripts to increase column sizes for Activiti	0	6012	15000	6013	2021-04-21 12:37:46.805	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateMimetypesSVG	Fix mimetype for Scalable Vector Graphics Image	0	6013	15000	6014	2021-04-21 12:37:46.806	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.1-NodeDeleted	Ensures that the database upgrade script has been run.	0	6014	15000	6015	2021-04-21 12:37:46.807	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateMimetypesVISIO	Fix mimetype for Microsoft Visio	0	6015	15000	6016	2021-04-21 12:37:46.809	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V3.4-JBPM-varinst-indexes	Ensures that the database upgrade script has been run.	0	6016	15000	6017	2021-04-21 12:37:46.81	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.2-upgrade-to-activiti-5.10	Upgraded Activiti tables to 5.10 version	0	6018	15000	6019	2021-04-21 12:37:46.811	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.emailAliasableAspect	Add email aliases to attrubute table	0	6019	15000	6020	2021-04-21 12:37:46.812	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.2-add-activti-index-historic-activity	Additional index for activiti on historic activity (PROC_INST_ID_ and ACTIVITY_ID_)	0	6021	15000	6022	2021-04-21 12:37:46.814	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.2-upgrade-to-activiti-5.11	Upgraded Activiti tables to 5.11 version	0	6022	15000	6023	2021-04-21 12:37:46.815	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.1-drop-alfqname-fk-indexes	Ensures that the database upgrade script has been run.	0	6023	15000	6024	2021-04-21 12:37:46.816	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.sharedFolder	Add Shared Folder	0	6023	15000	6024	2021-04-21 12:37:46.817	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.2-remove-index-acl_id	ALF-12284 : Update ALF_ACL_MEMBER_member table. Remove index acl_id.	0	6024	15000	6025	2021-04-21 12:37:46.818	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.1-drop-activiti-feed-format	Ensures that the database upgrade script has been run.	0	6025	15000	6026	2021-04-21 12:37:46.819	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.calendarAllDayEventDatesCorrectingPatch	This patch corrects 'to' and 'from' dates for Calendar 'All Day' Events from version 3.4 which did not take account of time zone offsets	0	6026	15000	6027	2021-04-21 12:37:46.821	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.2-remove-old-index-act	Delete unnecessary indexes add with older version of Activiti in 4.0 branch.	0	6027	15000	6028	2021-04-21 12:37:46.822	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.redeployActivitiWorkflowsForCategory	Redeploy internal process definitions for category update	0	6027	15000	6028	2021-04-21 12:37:46.823	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.2-upgrade-to-activiti-5.13	Upgraded Activiti tables to 5.13 version	0	6028	15000	6029	2021-04-21 12:37:46.824	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.redeployParallelActivitiWorkflows-after-5-11-upgrade	Patch that redeploys both parallel activiti workflows, completion-condition now takes into account if minimum approval percentage can still be achived.	0	6029	15000	6030	2021-04-21 12:37:46.825	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.1-fix-Repo-seqs-order	Ensures that the database upgrade script has been run.	0	6030	15000	6031	2021-04-21 12:37:46.827	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.1-ChildAssoc-OrderBy	Ensures that the database upgrade script has been run.	0	6032	15000	6033	2021-04-21 12:37:46.828	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.1-createIdxAlfNodeTQN	Ensures that the database upgrade script has been run.	0	7000	15000	7001	2021-04-21 12:37:46.829	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.2-restructure-idx_alf_nprop_s-MSSQL	Ensures that the database upgrade script has been run.	0	7001	15000	7002	2021-04-21 12:37:46.83	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.siteAdministrators	Adds the 'GROUP_SITE_ADMINISTRATORS' group	0	7002	15000	7003	2021-04-21 12:37:46.832	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.alfrescoSearchAdministrators	Adds the 'GROUP_ALFRESCO_SEARCH_ADMINISTRATORS' group	0	7003	15000	7004	2021-04-21 12:37:46.833	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.surfConfigFolder	Adds cm:indexControl aspect to surf-config children	0	7004	15000	7005	2021-04-21 12:37:46.834	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.renameSiteAuthorityDisplayName	Update authority display name for sites	0	8000	15000	8001	2021-04-21 12:37:46.835	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.1-update-activiti-nullable-columns	Ensures that the database upgrade script has been run.	0	8005	15000	8006	2021-04-21 12:37:46.837	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.eol-wcmwf	Undeploys deprecated WCMWF Workflows	0	8007	15000	8008	2021-04-21 12:37:46.838	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V4.2-migrate-locale-multilingual	Ensures that the database upgrade script has been run.	0	8018	15000	8019	2021-04-21 12:37:46.84	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.fixWebscriptTemplate	Reimport fixed sample template.	0	9000	15000	9001	2021-04-21 12:37:46.841	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.imapSpacesTemplates3	Replaces content templates for IMAP clients	0	9001	15000	9002	2021-04-21 12:37:46.842	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.addUnmovableAspect	Add unmovable aspect to sites.	0	9003	15000	9004	2021-04-21 12:37:46.843	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.deleteClassifibleAspectForFailedThumbnail	Deletes 'cm:generalclassifiable' aspect and associated properties for nodes of 'cm:failedThumbnail' type	0	9004	15000	9005	2021-04-21 12:37:46.844	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.downloadsFolder	Ensures the Syste Downloads folder exists.	0	9005	15000	9006	2021-04-21 12:37:46.846	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.fixPersonSizeCurrentType	Fix type of cm:sizeCurrent property.	0	9007	15000	9008	2021-04-21 12:37:46.847	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.alfrescoModelAdministrators	Adds the 'GROUP_ALFRESCO_MODEL_ADMINISTRATORS' group	0	9008	15000	9009	2021-04-21 12:37:46.848	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.addInviteAddDirectEmailTemplates	Adds the email templates for the add-direct invite flow	0	9009	15000	9010	2021-04-21 12:37:46.849	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.imapSpacesTemplates4	Replaces content templates for IMAP clients	0	9011	15000	9012	2021-04-21 12:37:46.85	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.addSurfConfigFolders	Adds 'cm:extensions' and 'cm:module-deployments' folders into surf-config folder.	0	9014	15000	9015	2021-04-21 12:37:46.853	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.spacesBootstrapSmartTemplatesFolder	Adds Smart Templates Folder in Data Dictionary.	0	9015	15000	9016	2021-04-21 12:37:46.854	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.spacesBootstrapSmartFolderExample	Adds smartFoldersExample.json file in Smart Templates Folder.	0	9015	15000	9016	2021-04-21 12:37:46.855	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.spacesBootstrapSmartDownloadFolder	Adds Smart Download Folder in Data Dictionary.	0	9015	15000	9016	2021-04-21 12:37:46.856	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-v4.2-migrate-activiti-workflows	Migrated workflow variables into newly created table.	0	10000	15000	10001	2021-04-21 12:37:46.858	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.redeployInvitationNominatedAddDirectActivitiWorkflow	Patch that redeploys activitiInvitationNominatedAddDirect workflow after upgrade, needed for tenants created before 5.1	0	10001	15000	10002	2021-04-21 12:37:46.859	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.updateAdminUserWhenDefault	Update Admin User by removing the default SHA256 and falling back to the MD4 (please consider using BCRYPT instead)	0	10002	15000	10003	2021-04-21 12:37:46.86	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.addInviteModeratedEmailTemplates	Adds the email template for the invite moderated flow	0	10050	15000	10051	2021-04-21 12:37:46.861	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.redeployInvitationModeratedActivitiWorkflow	Redeploy invitation moderated workflow.	0	10052	15000	10053	2021-04-21 12:37:46.862	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.db-V6.0-change-set-indexes	Add additional indexes to support acl tracking.	0	10200	15000	10201	2021-04-21 12:37:46.863	7.1.0 (re0bf8708-blocal) - Enterprise	f	t	Not relevant to schema 15,000
patch.exampleJavaScript	Loads sample Javascript file into datadictionary scripts folder	0	15000	15000	100000	2021-04-21 12:37:46.906	7.1.0 (re0bf8708-blocal) - Enterprise	t	t	Imported view into bootstrap location: /app:company_home/app:dictionary/app:scripts (workspace://SpacesStore/9bede7ad-b219-4bcf-a2e7-22a953807930)
patch.siteLoadPatch.swsdp	Loads a sample site into the repository.	0	15000	15000	100000	2021-04-21 12:37:48.917	7.1.0 (re0bf8708-blocal) - Enterprise	t	t	Site swsdp imported.
\.


--
-- Data for Name: alf_audit_app; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_audit_app (id, version, app_name_id, audit_model_id, disabled_paths_id) FROM stdin;
1	0	5	3	2
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
2	2	1341618561
3	3	252222308
\.


--
-- Data for Name: alf_auth_status; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_auth_status (id, username, deleted, authorized, checksum, authaction) FROM stdin;
1	admin	f	t	\\xaced0005737200196a617661782e63727970746f2e5365616c65644f626a6563743e363da6c3b754700200045b000d656e636f646564506172616d737400025b425b0010656e63727970746564436f6e74656e7471007e00014c0009706172616d73416c677400124c6a6176612f6c616e672f537472696e673b4c00077365616c416c6771007e00027870757200025b42acf317f8060854e002000078700000000a04084d833fda628cd3637571007e000400000058690265b394a8e53a175a1b5ef7b2bb61afcafc3028b21182587bafefadc3310de9f32758c85ddac96a2bf2fc626c9dba8e49670347d44037cb50269c5baf791cfc661f0416ab07130b8747065c81ecef5f85567f520b75bf7400064445536564657400174445536564652f4342432f504b43533550616464696e67	ADD
2	UserSearch-zGiNtcNRnaJPlLO	f	t	\\xaced0005737200196a617661782e63727970746f2e5365616c65644f626a6563743e363da6c3b754700200045b000d656e636f646564506172616d737400025b425b0010656e63727970746564436f6e74656e7471007e00014c0009706172616d73416c677400124c6a6176612f6c616e672f537472696e673b4c00077365616c416c6771007e00027870757200025b42acf317f8060854e002000078700000000a040894409d650b679bd57571007e00040000005850ba9d0e239c5131bd3d7590a78e327dfdc053a2051c5aa016c29bf831ef0e6589edff7b39a8bd1ace7b2cde43704347f74be45ca552064adb0cb306d4a3fe29ba80a5ca1e54a9e37261dc3091d257f124ae9c376465f32d7400064445536564657400174445536564652f4342432f504b43533550616464696e67	ADD
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
12	0	UserSearch-zGiNtcNRnaJPlLO	2676952300
13	0	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteManager	1503971941
14	0	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteCollaborator	3328933856
15	0	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteContributor	1611623478
16	0	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteConsumer	4288489783
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
1	1	1	4	-2702420110	0ad47e1d-4240-4749-a4d8-178b74703e30	2	1	system	1509831379	t	-1
2	1	2	4	-2392161986	e02f3f88-3679-47fc-a58a-13f42302002b	3	1	people	3097839998	t	-1
3	1	3	4	-3895371028	577b09e9-0120-44e1-a1ea-81d3311f3f68	4	2	admin	3226179863	t	-1
4	1	5	4	-3263953072	42e79d6e-cd83-48b0-aead-9a04db859ec2	6	1	descriptor	147310537	t	-1
5	1	5	4	-2681433455	53066c5e-fad9-42f6-8ff9-8022b719e6f3	7	1	descriptor-current	369154895	t	-1
6	1	5	4	-4061740525	19ccb1a9-5a5e-474d-8df7-99cb13e25824	8	1	system-registry	3365896830	t	-1
7	1	12	4	-514601725	8c3adc1a-d1eb-457d-80c4-159f4e89edf0	13	7	company_home	1502496580	t	-1
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
24	1	12	4	-3000657637	a8c8fe23-1ed0-43fa-bd1d-cf70357a91ce	30	1	system	1509831379	t	-1
25	1	30	4	-3859129695	7a84ce34-48a0-4bcd-a54a-6deb65f97873	31	1	people	3097839998	t	-1
26	1	31	4	-1402081291	24ed5e39-93cb-4b4d-9201-b0427a66516d	32	6	admin	347996256	t	-1
27	1	31	4	-1137445842	02a185cf-df51-4791-8207-7216fb4c1db3	33	6	guest	805803811	t	-1
28	1	30	4	-2242391818	9abc5aa3-6dc2-4b49-a6bc-0ad9a8b6db26	34	1	workflow	3049303691	t	-1
29	1	12	4	-2227388154	1fe4b4c8-10ee-4c20-b26c-de95541df86a	35	6	categoryRoot	2175667943	t	-1
30	1	35	48	-2219690355	725b9714-2231-479d-a11f-55871d5eb5d0	36	6	generalclassifiable	1686288257	t	-1
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
364	1	12	4	-1624519208	a315e930-23ce-47a5-8ca7-84e0a73cea8f	370	6	multilingualRoot	2349380271	t	-1
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
449	1	454	60	-2029908079	bc3c5033-3bfd-4d8c-92fe-695a32a7e2e2	455	8	ruleFolder	864502120	t	-1
450	1	455	33	791594146	ceca5a11-5c57-11dc-ad6c-5136d620963c	456	8	rulescc915a00-5c57-11dc-ad6c-5136d620963c	3752486723	t	-1
451	1	456	67	-309367673	eca52c9b-a708-448c-9be1-a0ce800bfb30	457	8	action	2726102540	t	-1
452	1	457	73	-3026759063	8f9d168e-7d31-4a26-bbfb-9b9aa63f9d6a	458	9	conditions	3127092715	t	-1
453	1	458	76	-721301194	37c3a3aa-3fd7-421b-a3ca-6e259b0a2fa7	459	9	parameters	1217848508	t	-1
454	1	457	80	-209922812	4dcabeee-1ab0-4fda-807e-0c0c62cdd082	460	9	actions	3055149561	t	-1
455	1	460	76	-2698238002	05c16ed3-63d1-4def-9a8c-baa6292e1e7a	461	9	parameters	1217848508	t	-1
456	1	14	33	3674349206	messages	462	7	messages	399009314	t	-1
457	1	14	33	663197873	web client extension	463	7	webclient_extension	3725604994	t	-1
458	1	14	33	2476626106	workflow definitions	464	7	workflow_defs	3566462828	t	-1
459	1	464	60	-1126499295	184255ab-13e3-42f7-bf86-bcd6d3d060cd	465	8	ruleFolder	864502120	t	-1
460	1	465	33	269678609	1e40c8cc-607e-11dc-af48-8b100325f217	466	8	rules1e40539b-607e-11dc-af48-8b100325f217	1892892178	t	-1
461	1	466	67	-3505645558	3977b7ee-549e-449d-b833-f94525709a28	467	8	action	2726102540	t	-1
462	1	467	73	-3445810994	a4edd86c-dfd6-4cea-b118-27667e60841e	468	9	conditions	3127092715	t	-1
463	1	468	76	-3608519381	305c65ba-a859-4892-b03b-9d00c768a95c	469	9	parameters	1217848508	t	-1
464	1	467	80	-2864528079	4f0c5695-edfa-4b51-823c-da761050228d	470	9	actions	3055149561	t	-1
465	1	470	76	-3832864613	85b707e5-fbe4-4fd1-a093-dda6d209f1c4	471	9	parameters	1217848508	t	-1
466	1	35	48	-2651839370	68c4b655-60a2-4da9-ad36-4e69cdbf73ee	472	6	taggable	69713702	t	-1
467	1	13	33	3154160227	sites	473	10	sites	411395544	t	-1
468	1	473	33	534011694	surf-config	474	6	surf-config	3685774615	t	-1
469	1	474	33	3589315753	extensions	475	6	extensions	3499105616	t	-1
470	1	474	33	1902532204	module-deployments	476	6	module-deployments	2134325345	t	-1
471	1	30	4	-3573723322	e1570481-0aa4-4e09-af06-638d81cf03bf	477	1	authorities	2510957676	t	-1
472	1	477	4	-3996961830	c8c38912-0289-498e-86ab-6f2bb4b2cb42	478	6	GROUP_ALFRESCO_ADMINISTRATORS	778018641	t	-1
473	1	477	4	-2665990853	a7bff0b8-2770-4d76-9b07-b0f364c9f6b4	479	6	GROUP_EMAIL_CONTRIBUTORS	2786248885	t	-1
474	1	477	4	-937847113	80204c2d-b1a0-48c4-bfd2-9f8012f03926	480	6	GROUP_SITE_ADMINISTRATORS	3539425948	t	-1
475	1	477	4	-442099476	398f165b-ef67-4caf-a500-d631ce65033b	481	6	GROUP_ALFRESCO_SEARCH_ADMINISTRATORS	4014781877	t	-1
476	1	477	4	-1345581101	3e514f71-0ee7-439d-bab9-409176d65161	482	6	GROUP_ALFRESCO_MODEL_ADMINISTRATORS	2911185014	t	-1
477	1	30	4	-4264363741	9c064b77-c72c-4749-a997-6ab6febd485d	483	1	zones	2314500199	t	-1
478	1	483	4	-3242793248	0ed49a8e-9d10-4c0b-a7ad-e874d9656c61	484	6	AUTH.ALF	2596686762	t	-1
479	1	484	92	2536743080	group_alfresco_administrators	478	6	GROUP_ALFRESCO_ADMINISTRATORS	778018641	f	-1
480	1	484	92	141606041	group_email_contributors	479	6	GROUP_EMAIL_CONTRIBUTORS	2786248885	f	-1
481	1	484	92	2655924869	group_site_administrators	480	6	GROUP_SITE_ADMINISTRATORS	3539425948	f	-1
482	1	484	92	1623073823	group_alfresco_search_administrators	481	6	GROUP_ALFRESCO_SEARCH_ADMINISTRATORS	4014781877	f	-1
483	1	484	92	2133539788	group_alfresco_model_administrators	482	6	GROUP_ALFRESCO_MODEL_ADMINISTRATORS	2911185014	f	-1
484	1	483	4	-719400548	1cf2f776-44d2-423f-b28e-9235f48bf331	485	6	APP.DEFAULT	3739798299	t	-1
485	1	485	92	2536743080	group_alfresco_administrators	478	6	GROUP_ALFRESCO_ADMINISTRATORS	778018641	f	-1
486	1	485	92	141606041	group_email_contributors	479	6	GROUP_EMAIL_CONTRIBUTORS	2786248885	f	-1
487	1	485	92	2655924869	group_site_administrators	480	6	GROUP_SITE_ADMINISTRATORS	3539425948	f	-1
488	1	485	92	1623073823	group_alfresco_search_administrators	481	6	GROUP_ALFRESCO_SEARCH_ADMINISTRATORS	4014781877	f	-1
489	1	485	92	2133539788	group_alfresco_model_administrators	482	6	GROUP_ALFRESCO_MODEL_ADMINISTRATORS	2911185014	f	-1
490	1	478	93	2883793182	6743d698-1e80-424e-808f-d9fd9d604afc	32	6	admin	347996256	f	-1
491	1	479	93	2883793182	6743d698-1e80-424e-808f-d9fd9d604afc	32	6	admin	347996256	f	-1
492	1	480	93	2883793182	6743d698-1e80-424e-808f-d9fd9d604afc	32	6	admin	347996256	f	-1
493	1	481	93	2883793182	6743d698-1e80-424e-808f-d9fd9d604afc	32	6	admin	347996256	f	-1
494	1	482	93	2883793182	6743d698-1e80-424e-808f-d9fd9d604afc	32	6	admin	347996256	f	-1
495	1	484	92	2883793182	6743d698-1e80-424e-808f-d9fd9d604afc	32	6	admin	347996256	f	-1
496	1	484	92	2489655450	06717873-38e5-40c5-ba47-ddd7a8d81bce	33	6	guest	805803811	f	-1
497	1	485	92	2883793182	6743d698-1e80-424e-808f-d9fd9d604afc	32	6	admin	347996256	f	-1
498	1	485	92	2489655450	06717873-38e5-40c5-ba47-ddd7a8d81bce	33	6	guest	805803811	f	-1
499	1	30	4	-321535986	8037a6bf-7829-4973-83ef-2735943a8678	486	1	remote_credentials	3212737544	t	-1
500	1	30	4	-841776619	b0ff704c-8f4c-477d-9943-3234c60ee43e	487	1	syncset_definitions	4198702871	t	-1
501	1	14	33	3181060681	imap configs	488	7	imap_configs	2749296655	t	-1
502	1	488	33	1864924558	templates	489	7	imap_templates	4290309844	t	-1
503	1	489	33	1875732140	emailbody-textplain.ftl	490	6	emailbody-textplain.ftl	3210300196	t	-1
504	1	489	33	3090409056	emailbody-texthtml.ftl	491	6	emailbody-texthtml.ftl	1275632005	t	-1
505	1	14	33	2150250776	transfers	492	7	transfers	1290437877	t	-1
506	1	492	33	1570715444	transfer target groups	493	7	transfer_groups	957030808	t	-1
507	1	493	33	2123542495	default group	494	6	default	2172081413	t	-1
508	1	492	33	3801670724	inbound transfer records	495	7	inbound_transfer_records	310355882	t	-1
509	1	492	33	190023114	temp	496	7	temp	3957740328	t	-1
510	1	14	33	1529518448	rendering actions space	497	7	rendering_actions	1996713548	t	-1
511	1	14	33	3343785569	replication actions space	498	7	replication_actions	2660722499	t	-1
512	1	494	60	-3808356086	5e0ca233-fc99-4dde-b4f7-f81e40732da3	499	8	ruleFolder	864502120	t	-1
513	1	499	33	4275936056	03835973-e040-4139-b971-e130e9559b9e	500	8	rules3245de8b-2cfe-42ed-8f8b-44089f99b265	3083479138	t	-1
514	1	500	67	-143307088	7f4b4d02-6cce-46fe-b31c-3c96f7082605	501	8	action	2726102540	t	-1
515	1	501	102	-20776917	f177197c-763f-4111-ad1c-080d2ed7c840	502	9	actionFolder	2614599834	t	-1
516	1	501	80	-1477907666	def3d16d-f125-44fb-85c6-59824548c29d	503	9	actions	3055149561	t	-1
517	1	503	76	-136562240	51e3590f-29e1-4322-ba3c-9bdd76f10ebe	504	9	parameters	1217848508	t	-1
518	1	501	73	-2929228586	ba27b80a-748b-47d8-bded-0819f9c4735b	505	9	conditions	3127092715	t	-1
519	1	505	76	-3698872683	67b348d2-55a2-4644-8bd4-f89f6e704792	506	9	parameters	1217848508	t	-1
520	1	14	33	352265367	scheduled actions	507	6	Scheduled Actions	2131169529	t	-1
521	1	18	33	445231378	new-user-email.html.ftl	508	6	new-user-email.html.ftl	3390938266	t	-1
522	1	18	33	3312025941	new-user-email_fr.html.ftl	509	6	new-user-email_fr.html.ftl	4245145103	t	-1
523	1	18	33	3007941738	new-user-email_es.html.ftl	510	6	new-user-email_es.html.ftl	2334640944	t	-1
524	1	18	33	1484235269	new-user-email_de.html.ftl	511	6	new-user-email_de.html.ftl	1612309855	t	-1
525	1	18	33	1536099796	new-user-email_it.html.ftl	512	6	new-user-email_it.html.ftl	1675707022	t	-1
526	1	18	33	65677689	new-user-email_ja.html.ftl	513	6	new-user-email_ja.html.ftl	998534691	t	-1
527	1	18	33	1187893579	new-user-email_nl.html.ftl	514	6	new-user-email_nl.html.ftl	2124650001	t	-1
528	1	18	33	3691661958	invite-email.html.ftl	515	6	invite-email.html.ftl	868286096	t	-1
529	1	18	33	3163192147	invite-email_fr.html.ftl	516	6	invite-email_fr.html.ftl	1597629548	t	-1
530	1	18	33	3400169068	invite-email_es.html.ftl	517	6	invite-email_es.html.ftl	689563987	t	-1
531	1	18	33	563369987	invite-email_de.html.ftl	518	6	invite-email_de.html.ftl	3257386812	t	-1
532	1	18	33	577568722	invite-email_it.html.ftl	519	6	invite-email_it.html.ftl	3252642029	t	-1
533	1	18	33	2047464319	invite-email_ja.html.ftl	520	6	invite-email_ja.html.ftl	2579123264	t	-1
534	1	18	33	1059990349	invite-email_nl.html.ftl	521	6	invite-email_nl.html.ftl	3701322866	t	-1
535	1	18	33	1305785334	invite-email-add-direct.html.ftl	522	6	invite-email-add-direct.html.ftl	4220725286	t	-1
536	1	18	33	3071386897	invite-email-add-direct.html_fr.ftl	523	6	invite-email-add-direct_fr.html.ftl	3152676049	t	-1
537	1	18	33	216399375	invite-email-add-direct.html_es.ftl	524	6	invite-email-add-direct_es.html.ftl	3452570094	t	-1
538	1	18	33	672747400	invite-email-add-direct.html_de.ftl	525	6	invite-email-add-direct_de.html.ftl	653535105	t	-1
539	1	18	33	3372737124	invite-email-add-direct.html_it.ftl	526	6	invite-email-add-direct_it.html.ftl	621576272	t	-1
540	1	18	33	3885205560	invite-email-add-direct.html_ja.ftl	527	6	invite-email-add-direct_ja.html.ftl	2104077565	t	-1
541	1	18	33	2224164511	invite-email-add-direct.html_nl.ftl	528	6	invite-email-add-direct_nl.html.ftl	944657615	t	-1
542	1	18	33	2574136958	invite-email-moderated.html.ftl	529	6	invite-email-moderated.html.ftl	3101219649	t	-1
543	1	395	33	4141493435	notify_user_email.html.ftl	530	6	notify_user_email.html.ftl	3467930593	t	-1
544	1	395	33	3092459551	notify_user_email_de.html.ftl	531	6	notify_user_email_de.html.ftl	3427405532	t	-1
545	1	395	33	1399660144	notify_user_email_es.html.ftl	532	6	notify_user_email_es.html.ftl	661921971	t	-1
546	1	395	33	625808207	notify_user_email_fr.html.ftl	533	6	notify_user_email_fr.html.ftl	1364471180	t	-1
547	1	395	33	3148584910	notify_user_email_it.html.ftl	534	6	notify_user_email_it.html.ftl	3484592397	t	-1
548	1	395	33	3821976419	notify_user_email_ja.html.ftl	535	6	notify_user_email_ja.html.ftl	2547484064	t	-1
549	1	395	33	2800304977	notify_user_email_nl.html.ftl	536	6	notify_user_email_nl.html.ftl	3538976146	t	-1
550	1	489	33	2852742012	emailbody_textplain_share.ftl	537	6	emailbody_textplain_share.ftl	3725614527	t	-1
551	1	489	33	1349507622	emailbody_textplain_alfresco.ftl	538	6	emailbody_textplain_alfresco.ftl	3861398006	t	-1
552	1	489	33	1214296187	emailbody_texthtml_alfresco.ftl	539	6	emailbody_texthtml_alfresco.ftl	1775656260	t	-1
553	1	489	33	2597111155	emailbody_texthtml_share.ftl	540	6	emailbody_texthtml_share.ftl	840998128	t	-1
554	1	489	33	3185687119	emailbody_textplain_share_de.ftl	541	6	emailbody_textplain_share_de.ftl	195436959	t	-1
555	1	489	33	1293729871	emailbody_textplain_alfresco_de.ftl	542	6	emailbody_textplain_alfresco_de.ftl	3130552875	t	-1
556	1	489	33	651551496	emailbody_texthtml_alfresco_de.ftl	543	6	emailbody_texthtml_alfresco_de.ftl	3224427164	t	-1
557	1	489	33	2783891474	emailbody_texthtml_share_de.ftl	544	6	emailbody_texthtml_share_de.ftl	2220375341	t	-1
558	1	489	33	2568817608	emailbody_textplain_share_es.ftl	545	6	emailbody_textplain_share_es.ftl	794464280	t	-1
559	1	489	33	1776316872	emailbody_textplain_alfresco_es.ftl	546	6	emailbody_textplain_alfresco_es.ftl	2657403820	t	-1
560	1	489	33	36262543	emailbody_texthtml_alfresco_es.ftl	547	6	emailbody_texthtml_alfresco_es.ftl	3838602011	t	-1
561	1	489	33	2165465493	emailbody_texthtml_share_es.ftl	548	6	emailbody_texthtml_share_es.ftl	2695138474	t	-1
562	1	489	33	585715926	emailbody_textplain_share_fr.ftl	549	6	emailbody_textplain_share_fr.ftl	2494483206	t	-1
563	1	489	33	3524537046	emailbody_textplain_alfresco_fr.ftl	550	6	emailbody_textplain_alfresco_fr.ftl	630233266	t	-1
564	1	489	33	3118270865	emailbody_texthtml_alfresco_fr.ftl	551	6	emailbody_texthtml_alfresco_fr.ftl	1597517829	t	-1
565	1	489	33	988166795	emailbody_texthtml_share_fr.ftl	552	6	emailbody_texthtml_share_fr.ftl	458282932	t	-1
566	1	489	33	1560221603	emailbody_textplain_share_it.ftl	553	6	emailbody_textplain_share_it.ftl	3937963123	t	-1
567	1	489	33	2885840291	emailbody_textplain_alfresco_it.ftl	554	6	emailbody_textplain_alfresco_it.ftl	1535565767	t	-1
568	1	489	33	3351989988	emailbody_texthtml_alfresco_it.ftl	555	6	emailbody_texthtml_alfresco_it.ftl	556693360	t	-1
569	1	489	33	1156611582	emailbody_texthtml_share_it.ftl	556	6	emailbody_texthtml_share_it.ftl	1699123393	t	-1
570	1	489	33	1919647231	emailbody_textplain_share_ja.ftl	557	6	emailbody_textplain_share_ja.ftl	3291276847	t	-1
571	1	489	33	2190870527	emailbody_textplain_alfresco_ja.ftl	558	6	emailbody_textplain_alfresco_ja.ftl	1964160411	t	-1
572	1	489	33	3915326648	emailbody_texthtml_alfresco_ja.ftl	559	6	emailbody_texthtml_alfresco_ja.ftl	263855404	t	-1
573	1	489	33	1784944546	emailbody_texthtml_share_ja.ftl	560	6	emailbody_texthtml_share_ja.ftl	1272117917	t	-1
574	1	489	33	2925151545	emailbody_textplain_share_nb.ftl	561	6	emailbody_textplain_share_nb.ftl	404610793	t	-1
575	1	489	33	1588019001	emailbody_textplain_alfresco_nb.ftl	562	6	emailbody_textplain_alfresco_nb.ftl	2837679453	t	-1
576	1	489	33	896408702	emailbody_texthtml_alfresco_nb.ftl	563	6	emailbody_texthtml_alfresco_nb.ftl	3549111786	t	-1
577	1	489	33	3059036004	emailbody_texthtml_share_nb.ftl	564	6	emailbody_texthtml_share_nb.ftl	2548286043	t	-1
578	1	489	33	292223832	emailbody_textplain_share_nl.ftl	565	6	emailbody_textplain_share_nl.ftl	2804768904	t	-1
579	1	489	33	3784802648	emailbody_textplain_alfresco_nl.ftl	566	6	emailbody_textplain_alfresco_nl.ftl	370357052	t	-1
580	1	489	33	2321457695	emailbody_texthtml_alfresco_nl.ftl	567	6	emailbody_texthtml_alfresco_nl.ftl	1824234379	t	-1
581	1	489	33	157651205	emailbody_texthtml_share_nl.ftl	568	6	emailbody_texthtml_share_nl.ftl	684948538	t	-1
582	1	489	33	541294538	emailbody_textplain_share_pt_br.ftl	569	6	emailbody_textplain_share_pt_BR.ftl	296413084	t	-1
583	1	489	33	916414695	emailbody_textplain_alfresco_pt_br.ftl	570	6	emailbody_textplain_alfresco_pt_BR.ftl	2198512349	t	-1
584	1	489	33	526120636	emailbody_texthtml_alfresco_pt_br.ftl	571	6	emailbody_texthtml_alfresco_pt_BR.ftl	4257765129	t	-1
585	1	489	33	1267358861	emailbody_texthtml_share_pt_br.ftl	572	6	emailbody_texthtml_share_pt_BR.ftl	1795313451	t	-1
586	1	489	33	143576139	emailbody_textplain_share_ru.ftl	573	6	emailbody_textplain_share_ru.ftl	3200866203	t	-1
587	1	489	33	4168332875	emailbody_textplain_alfresco_ru.ftl	574	6	emailbody_textplain_alfresco_ru.ftl	267846703	t	-1
588	1	489	33	2478478604	emailbody_texthtml_alfresco_ru.ftl	575	6	emailbody_texthtml_alfresco_ru.ftl	1969204376	t	-1
589	1	489	33	276932118	emailbody_texthtml_share_ru.ftl	576	6	emailbody_texthtml_share_ru.ftl	825707305	t	-1
590	1	489	33	4030035990	emailbody_textplain_share_zh_cn.ftl	577	6	emailbody_textplain_share_zh_CN.ftl	3252478016	t	-1
591	1	489	33	3874055995	emailbody_textplain_alfresco_zh_cn.ftl	578	6	emailbody_textplain_alfresco_zh_CN.ftl	1400677633	t	-1
592	1	489	33	3475894624	emailbody_texthtml_alfresco_zh_cn.ftl	579	6	emailbody_texthtml_alfresco_zh_CN.ftl	767447253	t	-1
593	1	489	33	2617006929	emailbody_texthtml_share_zh_cn.ftl	580	6	emailbody_texthtml_share_zh_CN.ftl	3144960247	t	-1
594	1	30	4	-387547471	d786ea58-955f-4821-8d8b-e66414859415	581	1	downloads	3307032186	t	-1
595	1	23	33	2732812139	smartfoldersexample.json	582	13	smartfolder.sample	3192301734	t	-1
596	1	21	33	1918060495	start-pooled-review-workflow.js	583	6	example.js	1403687563	t	-1
598	1	477	4	-2794394289	aa88b479-6a84-410b-a17a-db05296e6ce1	585	6	GROUP_site_swsdp	3760915499	t	-1
599	1	483	4	-1500961159	426880bc-0d61-4dad-b2b7-50a53741625b	586	6	APP.SHARE	3658694637	t	-1
600	1	484	92	3284435889	4f78dc6e000a27fc3bb7e9c70aa6b5d9	585	6	GROUP_site_swsdp	3760915499	f	-1
601	1	586	92	3284435889	4f78dc6e000a27fc3bb7e9c70aa6b5d9	585	6	GROUP_site_swsdp	3760915499	f	-1
602	1	477	4	-246855688	65e084c8-8ad5-4fb3-ac1c-ecd55d0d1bae	587	6	GROUP_site_swsdp_SiteManager	819207528	t	-1
603	1	484	92	2601610584	9e5cb3fa1850083495559ca2a4ca2de9	587	6	GROUP_site_swsdp_SiteManager	819207528	f	-1
604	1	586	92	2601610584	9e5cb3fa1850083495559ca2a4ca2de9	587	6	GROUP_site_swsdp_SiteManager	819207528	f	-1
605	1	585	93	2601610584	9e5cb3fa1850083495559ca2a4ca2de9	587	6	GROUP_site_swsdp_SiteManager	819207528	f	-1
606	1	477	4	-1729519748	ae610d0f-7bdf-417f-a134-1e90f7269b61	588	6	GROUP_site_swsdp_SiteCollaborator	1706459855	t	-1
607	1	484	92	1727778803	58d3dfc926fbcb0ce0a1213c37dc4711	588	6	GROUP_site_swsdp_SiteCollaborator	1706459855	f	-1
608	1	586	92	1727778803	58d3dfc926fbcb0ce0a1213c37dc4711	588	6	GROUP_site_swsdp_SiteCollaborator	1706459855	f	-1
609	1	585	93	1727778803	58d3dfc926fbcb0ce0a1213c37dc4711	588	6	GROUP_site_swsdp_SiteCollaborator	1706459855	f	-1
610	1	477	4	-3893173724	5ca6887a-207a-49be-bd4a-48bf9fda9b6e	589	6	GROUP_site_swsdp_SiteContributor	32651092	t	-1
611	1	484	92	2523597308	5b487fd6a02f7430721726163ba0daa9	589	6	GROUP_site_swsdp_SiteContributor	32651092	f	-1
612	1	586	92	2523597308	5b487fd6a02f7430721726163ba0daa9	589	6	GROUP_site_swsdp_SiteContributor	32651092	f	-1
613	1	585	93	2523597308	5b487fd6a02f7430721726163ba0daa9	589	6	GROUP_site_swsdp_SiteContributor	32651092	f	-1
614	1	477	4	-2194296981	e79709a1-c997-4bab-a1be-ddb185c87872	590	6	GROUP_site_swsdp_SiteConsumer	2168792413	t	-1
615	1	484	92	2118386964	73714588eb587e2a207a436130080c9e	590	6	GROUP_site_swsdp_SiteConsumer	2168792413	f	-1
616	1	586	92	2118386964	73714588eb587e2a207a436130080c9e	590	6	GROUP_site_swsdp_SiteConsumer	2168792413	f	-1
617	1	585	93	2118386964	73714588eb587e2a207a436130080c9e	590	6	GROUP_site_swsdp_SiteConsumer	2168792413	f	-1
618	1	587	93	2883793182	6743d698-1e80-424e-808f-d9fd9d604afc	32	6	admin	347996256	f	-1
619	1	3	4	-171550432	ae9b40a4-0e78-45d9-a75c-80af649a4064	591	2	abeecher	4294529677	t	-1
620	1	3	4	-3357129233	eb047d6e-0a38-4fb8-bf04-7a84ccded73e	592	2	mjackson	3024187048	t	-1
621	1	31	4	-463268350	dba81df1-7239-4d34-b2c9-7a00a1c68f3f	593	6	abeecher	3272809292	t	-1
622	1	593	137	-3488099353	fce5527d-85c6-44b4-8341-836c157ba9c2	594	6	abeecher-avatar.jpg	1156794817	t	-1
623	1	594	142	-1287067542	05ddaa10-c8ff-4072-b749-15dc5851bd7b	595	6	avatar	3795810163	t	-1
624	1	31	4	-1449019807	62236ab9-b5e5-4da2-97bb-1ac8af534f61	596	6	mjackson	2292918121	t	-1
625	1	596	137	-3559672740	2fe12334-fe5e-43b8-87e8-e3a157816719	597	6	mjackson-avatar.jpg	1186772188	t	-1
626	1	597	142	-3957269601	c47bfd0c-f0a0-47fa-8e38-95f1f9e5de08	598	6	avatar	3795810163	t	-1
627	1	588	93	3063427348	dc103838-645f-43c1-8a2a-bc187e13c343	593	6	abeecher	3272809292	f	-1
628	1	587	93	504535620	b6d80d49-21cc-4f04-9c92-e7063037543f	596	6	mjackson	2292918121	f	-1
629	1	473	33	2308701361	swsdp	600	6	swsdp	355201447	t	-1
630	1	600	33	3872509529	documentlibrary	601	6	documentLibrary	202189778	t	-1
631	1	601	33	4069355619	agency files	602	6	Agency Files	1958504637	t	-1
632	1	602	33	156281203	contracts	603	6	Contracts	3950493975	t	-1
633	1	603	33	3266167157	project contract.pdf	604	6	Project Contract.pdf	3754194629	t	-1
634	1	10	164	4047806828	1a0b110f-1e09-4ca2-b367-fe25e4964a4e	605	5	1a0b110f-1e09-4ca2-b367-fe25e4964a4e	2976416666	t	-1
635	1	605	166	-1643563152	d36b8886-fcf0-47f8-bcb5-53cb848eb89d	606	5	version-0	3486964613	t	-1
636	1	604	142	-787479318	abe022e3-49fa-4e9a-8702-2d1ec5bcbdc7	607	6	doclib	2991633180	t	-1
637	1	604	142	-1034939309	ed2ec4e6-5122-43f4-825c-efa8d38844ae	608	6	webpreview	1387062285	t	-1
638	1	602	33	3760176746	images	609	6	Images	335265280	t	-1
639	1	609	33	3788176685	coins.jpg	610	6	coins.JPG	398153056	t	-1
640	1	610	142	-3116518673	10f8de2a-dd13-4fa0-8f8f-4538c3a9219e	611	6	doclib	2991633180	t	-1
641	1	609	33	1184564164	graph.jpg	612	6	graph.JPG	2968169353	t	-1
642	1	612	142	-4288715711	03bf5860-010f-4abb-9037-4b10fc75c3d0	613	6	doclib	2991633180	t	-1
643	1	609	33	3611285497	grass.jpg	614	6	grass.jpg	3085516094	t	-1
644	1	614	142	-848811204	c9e28eca-a227-45a2-a425-0484c655ef27	615	6	doclib	2991633180	t	-1
645	1	614	142	-3841501060	0b2fcc06-43db-4ace-9786-417b9a80f11b	616	6	imgpreview	2566125866	t	-1
646	1	609	33	3436769363	money.jpg	617	6	money.JPG	984176670	t	-1
647	1	617	142	-655318804	2137ae4c-947f-444b-a305-d4d7443ea42d	618	6	doclib	2991633180	t	-1
648	1	609	33	2105090824	plugs.jpg	619	6	plugs.jpg	498059727	t	-1
649	1	619	142	-3885543588	5955045c-c77a-4ccf-aae2-8d29ed024071	620	6	doclib	2991633180	t	-1
650	1	609	33	4257289731	turbine.jpg	621	6	turbine.JPG	2936176304	t	-1
651	1	621	142	-2320386606	1accfcbb-339a-48ea-b363-0a6addecf5fb	622	6	doclib	2991633180	t	-1
652	1	609	33	3650608928	wires.jpg	623	6	wires.JPG	803633005	t	-1
653	1	623	142	-646257069	1bf309d6-1d7a-44a5-bc3c-cac64b5391d0	624	6	doclib	2991633180	t	-1
654	1	609	33	4245898435	wind turbine.jpg	625	6	wind turbine.JPG	3896697426	t	-1
655	1	625	142	-2714631176	297890a5-68af-4279-814b-2a020f086d37	626	6	doclib	2991633180	t	-1
656	1	609	33	3010006787	header.png	627	6	header.png	3054079738	t	-1
657	1	627	142	-2980595432	a413ca56-0c54-4446-93a1-6f52da17a1d5	628	6	doclib	2991633180	t	-1
658	1	609	33	2227112316	windmill.png	629	6	windmill.png	3682526558	t	-1
659	1	629	142	-427248031	a1762b85-fdc6-45f0-abb3-3e0f72ed9ead	630	6	doclib	2991633180	t	-1
660	1	609	33	4117066625	low consumption bulb.png	631	6	low consumption bulb.png	383184062	t	-1
661	1	631	142	-824765588	29c1f054-9dd2-4555-ba76-484519c872c1	632	6	doclib	2991633180	t	-1
662	1	602	33	3420513569	logo files	633	6	Logo Files	1075595276	t	-1
663	1	633	33	1875520457	ge logo.png	634	6	GE Logo.png	2602168225	t	-1
664	1	634	142	-2253801835	e0138002-e8f2-407c-952f-a2d999f0ebd4	635	6	doclib	2991633180	t	-1
665	1	633	33	2915011424	logo.png	636	6	logo.png	3420166655	t	-1
666	1	636	142	-1230912292	c677a0c7-be3c-4477-b22f-24e2b72f3f67	637	6	doclib	2991633180	t	-1
667	1	602	33	3588352399	mock-ups	638	6	Mock-Ups	1925111718	t	-1
668	1	638	33	2490552511	sample 1.png	639	6	sample 1.png	3417521309	t	-1
669	1	639	142	-4294585520	43285fa0-7aed-4ffd-8afe-ec1e94d8ee10	640	6	doclib	2991633180	t	-1
670	1	638	33	3553798767	sample 2.png	641	6	sample 2.png	2350073421	t	-1
671	1	641	142	-1009691969	643b3b9e-4686-4438-bdb0-e0303a2e1f76	642	6	doclib	2991633180	t	-1
672	1	641	142	-1249867876	d1956cab-68c6-4d5e-832a-873815abadf9	643	6	imgpreview	2566125866	t	-1
673	1	638	33	4004676575	sample 3.png	644	6	sample 3.png	2977132541	t	-1
674	1	644	142	-1937799483	1aa0e3ce-aab5-4d42-b92f-910c66deff23	645	6	doclib	2991633180	t	-1
675	1	644	142	-3126006483	1ce432ab-8649-4a17-a451-2e499bbf6e1a	646	6	imgpreview	2566125866	t	-1
676	1	602	33	1931736815	video files	647	6	Video Files	4028267525	t	-1
677	1	647	33	2956299995	websitereview.mp4	648	6	WebSiteReview.mp4	2521430049	t	-1
678	1	601	33	2538629819	budget files	649	6	Budget Files	291598949	t	-1
679	1	649	33	1781477269	invoices	650	6	Invoices	4114440028	t	-1
680	1	650	33	2109261446	inv i200-109.png	651	6	inv I200-109.png	3867146597	t	-1
681	1	651	142	-1145350840	993bce71-b41a-47b7-81ed-31c44edcee5a	652	6	doclib	2991633180	t	-1
682	1	650	33	2448114923	inv i200-189.png	653	6	inv I200-189.png	170687240	t	-1
683	1	653	142	-31140534	db90d609-2ba8-4815-aa17-76d96773d868	654	6	doclib	2991633180	t	-1
684	1	649	33	3627867045	budget.xls	655	6	budget.xls	3713817180	t	-1
685	1	655	142	-192817294	db76e369-5a90-4ddd-8b66-6171b5e8e3a1	656	6	doclib	2991633180	t	-1
686	1	655	142	-1929241961	f8d97f48-ba53-44f6-85d1-ab0b07ee16e3	657	6	webpreview	1387062285	t	-1
687	1	655	196	4094595993	budget.xls discussion	658	16	discussion	2764908846	t	-1
688	1	658	33	1604228650	comments	659	6	Comments	3230459619	t	-1
689	1	659	33	4186633902	comment-1297852210661_622	660	6	comment-1297852210661_622	1326409740	t	-1
690	1	601	33	2472434584	meeting notes	661	6	Meeting Notes	887013217	t	-1
691	1	661	33	554498960	meeting notes 2011-01-27.doc	662	6	Meeting Notes 2011-01-27.doc	2778943928	t	-1
692	1	662	142	-1248239238	615fc62b-b64d-4ca6-8f82-59128529a1d5	663	6	doclib	2991633180	t	-1
693	1	662	142	-176676804	95eb33d9-c0e0-4ca6-85c7-5d2b8433bdf2	664	6	webpreview	1387062285	t	-1
694	1	661	33	399244216	meeting notes 2011-02-03.doc	665	6	Meeting Notes 2011-02-03.doc	2472825232	t	-1
695	1	665	142	-1977992961	7afd4b4a-d008-47fd-befa-d932483d30ac	666	6	doclib	2991633180	t	-1
696	1	665	142	-671125353	05e28024-3712-466d-8cb6-19d6a44c4071	667	6	webpreview	1387062285	t	-1
697	1	661	33	2604094157	meeting notes 2011-02-10.doc	668	6	Meeting Notes 2011-02-10.doc	530118885	t	-1
698	1	668	142	-3449208061	e8804eec-8fa9-4ec9-b644-e197253ac4b9	669	6	doclib	2991633180	t	-1
699	1	668	142	-3043611536	e2ded11e-5091-4d4c-b0fd-ac2b65977bfd	670	6	webpreview	1387062285	t	-1
700	1	601	33	1922263837	presentations	671	6	Presentations	349348064	t	-1
701	1	671	33	1616623070	project objectives.ppt	672	6	Project Objectives.ppt	3516604448	t	-1
702	1	672	142	-114849197	75ada9d6-7caa-4a9c-94b8-f98f19b84c8f	673	6	doclib	2991633180	t	-1
703	1	672	142	-3192840274	a144c608-07dc-4d6e-9248-9d7f67e9ea2e	674	6	webpreview	1387062285	t	-1
704	1	671	33	1655540618	project overview.ppt	675	6	Project Overview.ppt	2143566906	t	-1
705	1	675	142	-1407197765	f50a978c-eafd-4c8e-acde-792337256718	676	6	doclib	2991633180	t	-1
706	1	675	142	-974336451	b9e9454f-7d4e-45d6-ba84-f1ed49497a82	677	6	webpreview	1387062285	t	-1
707	1	600	33	3514999064	links	678	6	links	1295166478	t	-1
708	1	678	33	2679163169	link-1297806194371_850	679	6	link-1297806194371_850	1804318404	t	-1
709	1	678	33	358194127	link-1297806244007_178	680	6	link-1297806244007_178	3781354538	t	-1
710	1	600	33	3997967452	datalists	681	6	dataLists	1331421599	t	-1
711	1	681	33	260683937	71824d77-9cd8-44c3-b3e4-dbca7e17dc49	682	6	71824d77-9cd8-44c3-b3e4-dbca7e17dc49	1168218450	t	-1
712	1	682	33	3513423167	e6fc15e9-5caf-4f17-857e-7b0cfbc655a9	683	6	e6fc15e9-5caf-4f17-857e-7b0cfbc655a9	2604840140	t	-1
713	1	682	33	64120555	42fcbae6-b1fe-4028-9f85-9ad7f81a8e3b	684	6	42fcbae6-b1fe-4028-9f85-9ad7f81a8e3b	1241138968	t	-1
714	1	682	33	2622673710	66028f46-c074-4cf5-9f37-8490e51ca540	685	6	66028f46-c074-4cf5-9f37-8490e51ca540	3598381789	t	-1
715	1	682	33	1496050538	50046ccd-9034-420f-925b-0530836488c4	686	6	50046ccd-9034-420f-925b-0530836488c4	319015577	t	-1
716	1	681	33	734398789	aea88103-517e-4aa0-a3be-de258d0e6465	687	6	aea88103-517e-4aa0-a3be-de258d0e6465	1642997942	t	-1
717	1	687	33	3109944928	9198bd31-a664-4584-a271-b529daf4793b	688	6	9198bd31-a664-4584-a271-b529daf4793b	4084588435	t	-1
718	1	687	33	138801921	eb1c2fda-4868-4384-b29e-78c01b6601ec	689	6	eb1c2fda-4868-4384-b29e-78c01b6601ec	1114493682	t	-1
719	1	687	33	2512162572	35b8be80-170f-40af-a173-513758b83165	690	6	35b8be80-170f-40af-a173-513758b83165	3751063295	t	-1
720	1	687	33	2362257682	567ee439-4ebc-40cf-a783-3e561ad5a605	691	6	567ee439-4ebc-40cf-a783-3e561ad5a605	3336900833	t	-1
721	1	687	33	3797031659	7a0bb872-bf7c-457b-831e-95f94efb9816	692	6	7a0bb872-bf7c-457b-831e-95f94efb9816	2826582808	t	-1
722	1	600	33	583916550	wiki	693	6	wiki	2613470146	t	-1
723	1	693	33	4105798722	main_page	694	6	Main_Page	3064742680	t	-1
724	1	10	164	417166766	d6f3a279-ce86-4a12-8985-93b71afbb71d	695	5	d6f3a279-ce86-4a12-8985-93b71afbb71d	1492211544	t	-1
725	1	695	166	-3756937210	1115314c-b468-472a-8b13-e0d9717d7a20	696	5	version-0	3486964613	t	-1
726	1	693	33	1157518050	meetings	697	6	Meetings	3689685547	t	-1
727	1	10	164	955032534	1373739a-2849-4647-9e97-7a4e05cc5841	698	5	1373739a-2849-4647-9e97-7a4e05cc5841	2025881888	t	-1
728	1	698	166	-887744759	10a72cf3-3cfb-43a0-be9e-e73def8beaea	699	5	version-0	3486964613	t	-1
729	1	693	33	418873732	milestones	700	6	Milestones	1388970925	t	-1
730	1	10	164	1094547242	3c73aace-9f54-420d-a1c0-c54b6a116dcf	701	5	3c73aace-9f54-420d-a1c0-c54b6a116dcf	17913308	t	-1
731	1	701	166	-431968854	957fffcd-6ceb-463a-8c47-0e1fded3f7cf	702	5	version-0	3486964613	t	-1
732	1	600	33	2339466083	discussions	703	6	discussions	1326793050	t	-1
733	1	703	33	1324234952	post-1297807546884_964	704	6	post-1297807546884_964	3134539565	t	-1
734	1	704	33	1324234952	post-1297807546884_964	705	6	post-1297807546884_964	3134539565	t	-1
735	1	704	33	2030775859	post-1297807619797_315	706	6	post-1297807619797_315	2368754134	t	-1
736	1	704	33	1963213525	post-1297807729794_112	707	6	post-1297807729794_112	2168405296	t	-1
737	1	704	33	1848061172	post-1297807767790_183	708	6	post-1297807767790_183	2585547537	t	-1
738	1	703	33	3424006769	post-1297807581026_873	709	6	post-1297807581026_873	942493076	t	-1
739	1	709	33	3424006769	post-1297807581026_873	710	6	post-1297807581026_873	942493076	t	-1
740	1	709	33	881747235	post-1297807650635_649	711	6	post-1297807650635_649	3233094342	t	-1
741	1	600	33	534011694	surf-config	712	6	surf-config	3685774615	t	-1
742	1	712	33	544531829	pages	713	6	pages	3167021155	t	-1
743	1	713	33	1766001124	site	714	6	site	3494426144	t	-1
744	1	714	33	2308701361	swsdp	715	6	swsdp	355201447	t	-1
745	1	715	33	631660036	dashboard.xml	716	6	dashboard.xml	4036615692	t	-1
746	1	712	33	3997758973	components	717	6	components	3945296900	t	-1
747	1	717	33	1334852793	page.component-1-1.site~swsdp~dashboard.xml	718	6	page.component-1-1.site~swsdp~dashboard.xml	3555596018	t	-1
748	1	717	33	1524266966	page.component-1-3.site~swsdp~dashboard.xml	719	6	page.component-1-3.site~swsdp~dashboard.xml	3332660637	t	-1
749	1	717	33	1296972702	page.component-2-1.site~swsdp~dashboard.xml	720	6	page.component-2-1.site~swsdp~dashboard.xml	3509588437	t	-1
750	1	717	33	3206141542	page.component-2-2.site~swsdp~dashboard.xml	721	6	page.component-2-2.site~swsdp~dashboard.xml	594017325	t	-1
751	1	717	33	1476686065	page.component-2-3.site~swsdp~dashboard.xml	722	6	page.component-2-3.site~swsdp~dashboard.xml	3296352954	t	-1
752	1	717	33	3046069903	page.navigation.site~swsdp~dashboard.xml	723	6	page.navigation.site~swsdp~dashboard.xml	3548890421	t	-1
753	1	717	33	861589342	page.title.site~swsdp~dashboard.xml	724	6	page.title.site~swsdp~dashboard.xml	3302915386	t	-1
754	1	717	33	3183989057	page.component-1-2.site~swsdp~dashboard.xml	725	6	page.component-1-2.site~swsdp~dashboard.xml	565836554	t	-1
755	1	717	33	2182679792	page.component-1-4.site~swsdp~dashboard.xml	726	6	page.component-1-4.site~swsdp~dashboard.xml	510076603	t	-1
756	1	8	4	-1713150983	0da8230d-227a-43a6-a39a-2592e9c47840	727	19	modules	462710347	t	-1
757	1	727	4	-3441072407	3775a789-8baa-4de0-82d8-4e10216aeed3	728	19	bulkObjectMapper	1302368339	t	-1
758	1	727	4	-352590079	cf6a82e9-8727-4b18-84b5-fe818b2e8c3c	729	19	alfresco-aos-module	2155693883	t	-1
759	1	727	4	-1932372815	99021860-a480-4066-bdb5-d7784f3242bc	730	19	org.alfresco.integrations.google.docs	3196163045	t	-1
760	1	727	4	-3402633930	d41c8381-6347-4c13-b78e-8b9a66663a0a	731	19	org_alfresco_device_sync_repo	3706539048	t	-1
761	1	727	4	-3581432554	14c89e1a-e504-4b5b-8971-ed5e97ac4cad	732	19	large_txn_generator	1291955643	t	-1
762	1	727	4	-1317422418	df24cc4d-ab31-4f32-b633-1b6758db3917	733	19	alfresco-share-services	3303942366	t	-1
763	1	727	4	-2328682093	041a80ef-f72e-465d-ab8a-8d6da1ba265f	734	19	alfresco-trashcan-cleaner	2285549386	t	-1
764	1	497	33	3451532781	33cada63-4314-409c-9b88-b988505313ed	735	6	imgpreview	2566125866	t	-1
765	1	735	76	-2286152291	d9c33cb0-fd12-4b42-9239-bda6a23880cb	736	9	parameters	1217848508	t	-1
766	1	735	76	-1074550812	29f211d2-87ab-4188-82d7-ff3aefb8970f	737	9	parameters	1217848508	t	-1
767	1	735	76	-3743982328	94d7e0d5-ce42-499b-9663-97b9fc66f3d8	738	9	parameters	1217848508	t	-1
768	1	735	76	-4257417495	f3ba2172-f016-49c0-ab2e-582729c4b076	739	9	parameters	1217848508	t	-1
769	1	735	76	-423774059	071f9cf0-e85c-47e5-8026-2315ad9c1b23	740	9	parameters	1217848508	t	-1
770	1	735	76	-1936325297	daeb752f-aabe-48e7-9dd8-a83057e02d3e	741	9	parameters	1217848508	t	-1
771	1	735	76	-612486274	1a655fdb-62b3-4cfb-bbaf-b631c169b3a5	742	9	parameters	1217848508	t	-1
772	1	735	76	-3542508247	d10e99b6-4b4a-4211-9647-6756eed642a7	743	9	parameters	1217848508	t	-1
773	1	735	76	-133629437	4217a1e3-a094-4601-89cc-bd663c013eb8	744	9	parameters	1217848508	t	-1
774	1	735	76	-510324156	71b5bd13-d8d6-423f-97a2-749897011534	745	9	parameters	1217848508	t	-1
775	1	735	76	-1564619450	6f9ac2f9-1b44-42fb-b746-ea6101ba33d9	746	9	parameters	1217848508	t	-1
776	1	735	76	-823162172	4af4bef0-611f-46de-89f2-0a5ecef5a36c	747	9	parameters	1217848508	t	-1
777	1	735	76	-3324533121	d8ccb53c-8655-48e8-9433-046e0fdeb325	748	9	parameters	1217848508	t	-1
778	1	735	76	-1241325554	e6153c0d-ce2c-47e2-bbd3-b6fe3dcf829f	749	9	parameters	1217848508	t	-1
779	1	735	76	-1487201863	75d4d1e9-7e35-493a-8683-553f93fde4f0	750	9	parameters	1217848508	t	-1
780	1	735	76	-1095325780	7d1f3074-4364-47ac-b307-19d762c09e9a	751	9	parameters	1217848508	t	-1
781	1	735	76	-587661031	e703b167-f193-4935-9c76-59af0ca26fcb	752	9	parameters	1217848508	t	-1
782	1	735	76	-1086599023	34e88163-66f2-42bc-92d6-c22746a2c657	753	9	parameters	1217848508	t	-1
783	1	735	76	-3071579372	193e7c54-fd0d-42b3-b249-fa518595553a	754	9	parameters	1217848508	t	-1
784	1	735	76	-4009032995	8f79ef7b-42ac-432f-900c-408bdf72533c	755	9	parameters	1217848508	t	-1
785	1	497	33	1055703013	e9adb4b2-a6ad-4753-bfc3-bc3ebdf7f110	756	6	doclib	2991633180	t	-1
786	1	756	76	-2029114028	c5f5ab02-525b-4ab4-b6c2-b7fc8d7c35d9	757	9	parameters	1217848508	t	-1
787	1	756	76	-781100569	e39f9933-e9fe-4561-833e-e83f6b59084f	758	9	parameters	1217848508	t	-1
788	1	756	76	-3753527521	8bb63013-85d6-4605-a209-8603f314fbe4	759	9	parameters	1217848508	t	-1
789	1	756	76	-1598771728	6da42498-4f4d-42ed-8900-dd23254e9269	760	9	parameters	1217848508	t	-1
790	1	756	76	-870076897	639b6a8e-1f74-4569-a768-d992a6991be5	761	9	parameters	1217848508	t	-1
791	1	756	76	-966770983	ce4035fd-c4b4-4783-8c51-417f79797138	762	9	parameters	1217848508	t	-1
792	1	756	76	-786954792	c87c10e3-a41a-425f-ad47-8a142efcb2ee	763	9	parameters	1217848508	t	-1
793	1	756	76	-1108993392	c27977ef-a476-4f48-97dd-cd789ecb8c38	764	9	parameters	1217848508	t	-1
794	1	756	76	-1798241158	e20a5f64-0f4c-4563-a72a-6d139912b295	765	9	parameters	1217848508	t	-1
795	1	756	76	-2638749666	d1155425-2aab-4af6-b614-d7a046c1483c	766	9	parameters	1217848508	t	-1
796	1	756	76	-31198918	05d3fc45-e62b-432b-9d5a-4c7d10c31290	767	9	parameters	1217848508	t	-1
797	1	756	76	-2635591279	6e1d0b7d-7da5-407f-ae7a-d151165d12e9	768	9	parameters	1217848508	t	-1
798	1	756	76	-1306631337	b0961543-4970-447b-a721-a17020ae568b	769	9	parameters	1217848508	t	-1
799	1	756	76	-3585307666	651644cd-95b2-46a7-b127-a4585cc47e01	770	9	parameters	1217848508	t	-1
800	1	756	76	-3110981597	cd9c8256-03d6-4010-8616-72f11086c31c	771	9	parameters	1217848508	t	-1
801	1	756	76	-1708756659	90bb5176-ad7a-4692-bc21-e7a39d01311b	772	9	parameters	1217848508	t	-1
802	1	756	76	-783316552	963ed5b2-f93a-432b-ac12-3ff242769994	773	9	parameters	1217848508	t	-1
803	1	756	76	-2025262815	4136d916-555d-4594-ae1d-e70033a47c4e	774	9	parameters	1217848508	t	-1
804	1	756	76	-3371335683	2618a717-8892-4ce8-a8f4-f49ee1441a3d	775	9	parameters	1217848508	t	-1
805	1	756	76	-506272287	e7297bbb-632d-4f57-a3ba-90a291c67d98	776	9	parameters	1217848508	t	-1
806	1	497	33	1368741068	04c6126e-ebb5-4824-87d7-10c814cb69bf	777	6	pdf	1671108346	t	-1
1079	1	934	33	859491397	care might.txt	1037	6	Care might.txt	4031775535	t	-1
807	1	777	76	-1215598002	8791b76c-59eb-4edf-9016-9aa723304ecc	778	9	parameters	1217848508	t	-1
808	1	777	76	-331972602	8f269bf1-d8a5-470c-8164-5d0de4d94229	779	9	parameters	1217848508	t	-1
809	1	777	76	-1707644780	d376efa5-3c1b-4051-a59e-ae46330b0540	780	9	parameters	1217848508	t	-1
810	1	777	76	-522499820	32883827-a688-43d8-981f-b55eb4353f7d	781	9	parameters	1217848508	t	-1
811	1	777	76	-145204086	bc294d86-64fd-451c-9921-364938fe5bfd	782	9	parameters	1217848508	t	-1
812	1	777	76	-4225990137	ed7f737e-69a3-4199-b260-c2e83edea20d	783	9	parameters	1217848508	t	-1
813	1	777	76	-3587313078	de8811dd-d89f-4a6a-b9fe-2642ff2115ed	784	9	parameters	1217848508	t	-1
814	1	777	76	-3343360106	5cc180d5-e9bc-42b0-addc-ff8b300c53aa	785	9	parameters	1217848508	t	-1
815	1	777	76	-1418587712	3dbd8c26-23d8-457f-b0f2-cc5033a15085	786	9	parameters	1217848508	t	-1
816	1	777	76	-4089800133	96e9d6c7-1e29-4c4c-9fee-b82729c201c3	787	9	parameters	1217848508	t	-1
817	1	777	76	-1625892918	4a7b232d-5ddb-49b5-b84d-6e6358aa9cc1	788	9	parameters	1217848508	t	-1
818	1	497	33	1219996659	b892235b-fe73-440d-9e91-3e25b228f1d8	789	6	medium	842744043	t	-1
819	1	789	76	-2525138448	c8843379-9473-4fea-8bb6-2109f23cc925	790	9	parameters	1217848508	t	-1
820	1	789	76	-1375047651	7c850fab-f290-4462-aef2-4bb9fdb2233b	791	9	parameters	1217848508	t	-1
821	1	789	76	-149051092	a0327a80-4602-47c8-83e8-61fd91d3384b	792	9	parameters	1217848508	t	-1
822	1	789	76	-2776809795	ff05afc7-74f0-4350-b947-f8eb07005454	793	9	parameters	1217848508	t	-1
823	1	789	76	-938604173	e6728b4c-86a1-465f-bded-853b765bc324	794	9	parameters	1217848508	t	-1
824	1	789	76	-897991212	109f6ddc-60b8-48de-b411-5f4e114eecd7	795	9	parameters	1217848508	t	-1
825	1	789	76	-1717235562	d14145e5-2d1e-4bc4-903d-213a754d514e	796	9	parameters	1217848508	t	-1
826	1	789	76	-3832796717	2e949afc-e463-44b3-aa79-8ce6eefde6b1	797	9	parameters	1217848508	t	-1
827	1	789	76	-2907567424	e0f69373-1148-43b3-ae4a-ace4b98d74f9	798	9	parameters	1217848508	t	-1
828	1	789	76	-822161765	af40ca33-ab7a-4c69-9b1e-7801f22dc738	799	9	parameters	1217848508	t	-1
829	1	789	76	-1487178929	7cf08e06-dd72-4bdf-b211-fd518f902779	800	9	parameters	1217848508	t	-1
830	1	789	76	-1134069552	a246cf40-89ad-4575-9ba8-6e9219470d4e	801	9	parameters	1217848508	t	-1
831	1	789	76	-3345470566	5282547d-9b5b-4e76-be43-c2c066e4414c	802	9	parameters	1217848508	t	-1
832	1	789	76	-468452387	4dae420a-4f8f-4c03-8136-551133fd6747	803	9	parameters	1217848508	t	-1
833	1	789	76	-3915115976	9eef7843-e0d5-40c8-8204-db9d22fdd12d	804	9	parameters	1217848508	t	-1
834	1	789	76	-2691326439	652123ca-5878-4227-966d-3ebdcecc0f84	805	9	parameters	1217848508	t	-1
835	1	789	76	-852118050	151db1b3-631c-401b-b6de-b0d3b6d6f73e	806	9	parameters	1217848508	t	-1
836	1	789	76	-2580949369	8872cde1-9a9b-4b10-a3ff-fcbda165c64d	807	9	parameters	1217848508	t	-1
837	1	789	76	-549293332	8f103320-5c88-48b5-a81e-621533be1304	808	9	parameters	1217848508	t	-1
838	1	789	76	-2994994762	9397bf3b-3b31-448b-9f20-9c367817be1f	809	9	parameters	1217848508	t	-1
839	1	497	33	441928544	4cd31a9d-108c-4501-8397-3a5ab3ab48dd	810	6	avatar	3795810163	t	-1
840	1	810	76	-1048832601	59048183-42a9-463c-ac66-47f7080145d5	811	9	parameters	1217848508	t	-1
841	1	810	76	-3484070523	0349116d-3e8c-42e4-8018-1f1eca9438ff	812	9	parameters	1217848508	t	-1
842	1	810	76	-3691257117	01e6b5ae-bf5a-4c7f-b057-126e659fa473	813	9	parameters	1217848508	t	-1
843	1	810	76	-2952363017	34806ae8-5129-44b5-a587-c55b114ac3b2	814	9	parameters	1217848508	t	-1
844	1	810	76	-3104198832	549cb9a3-f065-418c-bcaa-2194f9499585	815	9	parameters	1217848508	t	-1
845	1	810	76	-2724428408	e91cac33-797d-487a-8852-5622ebfea232	816	9	parameters	1217848508	t	-1
846	1	810	76	-3985665872	97f687e7-79aa-4a90-a5ff-e0dbbbd2286f	817	9	parameters	1217848508	t	-1
847	1	810	76	-2842599431	c2b008f4-cdd4-46e1-b140-2c613fab910f	818	9	parameters	1217848508	t	-1
848	1	810	76	-1563022636	e7e247bd-8554-498f-aa15-f808301c0b2b	819	9	parameters	1217848508	t	-1
849	1	810	76	-3295775619	dc53f95a-5498-432b-a7e7-74ff689143ab	820	9	parameters	1217848508	t	-1
850	1	810	76	-3113880221	1767e6cc-748a-4e58-a965-84211d15b511	821	9	parameters	1217848508	t	-1
851	1	810	76	-2892089169	242875ef-e301-4e6a-8fe5-4ba243ac2d4d	822	9	parameters	1217848508	t	-1
852	1	810	76	-1898311565	5af11fae-15d3-44fc-82a5-c1b5383a4c2f	823	9	parameters	1217848508	t	-1
853	1	810	76	-1095865357	46a56e9f-4978-4024-8632-ab0cbc7643f9	824	9	parameters	1217848508	t	-1
854	1	810	76	-2663653514	9cbfef31-3526-463c-801e-db9bc42739e9	825	9	parameters	1217848508	t	-1
855	1	810	76	-2456679461	b4055dbb-382f-4fe7-ab1c-4e0bfba79fb2	826	9	parameters	1217848508	t	-1
856	1	810	76	-1097579546	e3f7d956-5d36-4016-afe4-a13ad9a2bdcc	827	9	parameters	1217848508	t	-1
857	1	810	76	-4036278722	19a620f6-1c82-4a00-aaf8-b3ce578c2e6d	828	9	parameters	1217848508	t	-1
858	1	810	76	-2726233859	c7aead74-7d46-44dc-8fe6-81be74eff6ec	829	9	parameters	1217848508	t	-1
859	1	810	76	-1350734797	c229eaa9-26ad-4cac-9398-bd40c4ba9fca	830	9	parameters	1217848508	t	-1
860	1	497	33	1358416701	619024bf-ef7f-4705-84bf-9d113634e616	831	6	webpreview	1387062285	t	-1
861	1	831	76	-1140553571	c6e5be79-84be-4316-9278-c2b8d6a31917	832	9	parameters	1217848508	t	-1
862	1	831	76	-2265101754	8782f6cc-8766-4f3f-a6ed-8ddebe297236	833	9	parameters	1217848508	t	-1
863	1	831	76	-2984341948	65f57aae-eddd-4020-adf1-6329031b5ae2	834	9	parameters	1217848508	t	-1
864	1	831	76	-4207777632	ec754a48-f9e1-4959-a103-ba3b7b7b7f5f	835	9	parameters	1217848508	t	-1
865	1	831	76	-4234963741	6a901579-3e27-40bd-8153-2dbc2bd34a57	836	9	parameters	1217848508	t	-1
866	1	831	76	-3551672215	3921362e-3be3-4e8a-9698-3c46d3b5ca08	837	9	parameters	1217848508	t	-1
867	1	831	76	-1732841241	cb66a9f0-2b07-4a17-a27e-d095770da207	838	9	parameters	1217848508	t	-1
868	1	831	76	-373742792	edc9b5d7-6c48-4aa7-a728-1c0e6e583157	839	9	parameters	1217848508	t	-1
869	1	831	76	-3843315347	0693bb1a-d545-4f8b-a5cb-7740f2d71006	840	9	parameters	1217848508	t	-1
870	1	831	76	-375924472	6219f111-e9ec-4cb8-b0df-120e37241e5b	841	9	parameters	1217848508	t	-1
871	1	831	76	-39400360	2b133510-960a-44a6-b306-31c40cc9c0e1	842	9	parameters	1217848508	t	-1
872	1	831	76	-3033303431	d0f56526-edc5-40fb-a862-9ad6286d1965	843	9	parameters	1217848508	t	-1
873	1	497	33	2603568931	df207dde-1a2b-4658-9a32-54b03a5069ca	844	6	avatar32	3071675098	t	-1
874	1	844	76	-680680237	42046713-43c2-4610-9fef-31479743d772	845	9	parameters	1217848508	t	-1
875	1	844	76	-1318999345	016acbb5-ed44-42b8-ba20-12c067ef51c3	846	9	parameters	1217848508	t	-1
876	1	844	76	-2724665595	a6558541-331b-4b9a-b33b-cf2a40cec5e3	847	9	parameters	1217848508	t	-1
877	1	844	76	-3581002403	a7470025-4148-4214-a430-30c7cb705ce5	848	9	parameters	1217848508	t	-1
878	1	844	76	-3078656655	721b26df-a876-4c6a-a48e-63eaa4705422	849	9	parameters	1217848508	t	-1
879	1	844	76	-3089751886	31fd7188-c802-4658-8893-eb224a2299d6	850	9	parameters	1217848508	t	-1
880	1	844	76	-147404760	87f3a0f2-5ae8-447d-bae5-db510593c7d3	851	9	parameters	1217848508	t	-1
881	1	844	76	-4112598275	417684e9-e931-4055-b78b-4142046cc767	852	9	parameters	1217848508	t	-1
882	1	844	76	-1051665574	23346ece-f6a1-4365-afd7-b0869b03038f	853	9	parameters	1217848508	t	-1
883	1	844	76	-1243628986	d3564cad-f625-4e1f-8623-ed3381375fa1	854	9	parameters	1217848508	t	-1
884	1	844	76	-4046786015	a62baf7d-d379-427a-b1be-e7c4be59a1e0	855	9	parameters	1217848508	t	-1
885	1	844	76	-3660464828	a604dc72-0598-406e-bfcf-51b7f6605df2	856	9	parameters	1217848508	t	-1
886	1	844	76	-1483970373	73ecc663-6e42-43dd-b6c1-4e1c4b791b7a	857	9	parameters	1217848508	t	-1
887	1	844	76	-1236789862	5308527b-9238-4705-a0c0-bcea59f3b089	858	9	parameters	1217848508	t	-1
888	1	844	76	-4017705272	171b4a05-99df-434e-97cb-d9b2f3c51485	859	9	parameters	1217848508	t	-1
889	1	844	76	-2190233257	acc9caf0-7a70-4508-b409-f018a3b6d63d	860	9	parameters	1217848508	t	-1
890	1	844	76	-3565688117	b7581886-eeb6-4216-b52b-bce0b87911a3	861	9	parameters	1217848508	t	-1
891	1	844	76	-1233910317	17dbee3d-e2dd-42bb-a755-72e50f418d05	862	9	parameters	1217848508	t	-1
892	1	844	76	-1431049705	6a8473f4-459a-484c-9b0e-d262949c8463	863	9	parameters	1217848508	t	-1
893	1	844	76	-2199945078	3dce8aa4-d20d-489a-9bb7-46466c36699e	864	9	parameters	1217848508	t	-1
894	1	14	33	2443821992	solr facets space	865	20	facets	1198935629	t	-1
895	1	474	33	3997758973	components	866	6	components	3945296900	t	-1
896	1	866	33	2786798001	page.title.user~admin~dashboard.xml	867	6	page.title.user~admin~dashboard.xml	1369414613	t	-1
897	1	866	33	3900866617	page.full-width-dashlet.user~admin~dashboard.xml	868	6	page.full-width-dashlet.user~admin~dashboard.xml	2343904887	t	-1
898	1	866	33	3671186006	page.component-1-1.user~admin~dashboard.xml	869	6	page.component-1-1.user~admin~dashboard.xml	1185935389	t	-1
899	1	866	33	679884718	page.component-1-2.user~admin~dashboard.xml	870	6	page.component-1-2.user~admin~dashboard.xml	3036159461	t	-1
900	1	866	33	3624918385	page.component-2-1.user~admin~dashboard.xml	871	6	page.component-2-1.user~admin~dashboard.xml	1148319546	t	-1
901	1	866	33	710422665	page.component-2-2.user~admin~dashboard.xml	872	6	page.component-2-2.user~admin~dashboard.xml	3055950530	t	-1
902	1	474	33	544531829	pages	873	6	pages	3167021155	t	-1
903	1	873	33	2375276105	user	874	6	user	882403725	t	-1
904	1	874	33	2282622326	admin	875	6	admin	347996256	t	-1
905	1	875	33	631660036	dashboard.xml	876	6	dashboard.xml	4036615692	t	-1
906	1	13	33	2545907250	uploaded	877	6	uploaded	4057687213	t	-1
908	1	877	142	-3347622828	fe02a02c-4bd4-4884-b728-02a3137f6ed4	879	6	pdf	1671108346	t	-1
909	1	877	142	-3128118209	07af10e9-c73a-47c2-b475-e8544c80c7c8	881	6	doclib	2991633180	t	-1
919	1	454	33	2546120289	music-model.xml	894	6	music-model.xml	2997054070	t	-1
920	1	10	164	2527831028	c8378689-c06d-4b71-9d73-63ce4192344e	895	5	c8378689-c06d-4b71-9d73-63ce4192344e	3599204610	t	-1
928	1	31	4	-1917508341	7c1d2de3-f14a-4345-b010-4d9258fd7a1c	903	6	usersearch-zgintcnrnajpllo	3244338753	t	-1
929	1	26	33	4178503963	usersearch-zgintcnrnajpllo	904	6	UserSearch-zGiNtcNRnaJPlLO	2816560054	t	-1
930	1	484	92	87753057	190bd5e4-0ba2-4f79-aec4-88cbe7959fc3	903	6	UserSearch-zGiNtcNRnaJPlLO	2816560054	f	-1
931	1	485	92	87753057	190bd5e4-0ba2-4f79-aec4-88cbe7959fc3	903	6	UserSearch-zGiNtcNRnaJPlLO	2816560054	f	-1
932	1	3	4	-1641448843	c531f65f-0386-47a6-986e-e34d17293d69	905	2	UserSearch-zGiNtcNRnaJPlLO	2029541621	t	-1
933	1	473	33	3121889924	sitesearch-czxbacayhqqynwr	906	6	SiteSearch-cZXBaCaYHqqynWr	252294081	t	-1
934	1	477	4	-1529366065	34038af5-0c52-4776-8ed2-89662ea9e39f	907	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr	116521866	t	-1
935	1	484	92	1016252642	b6cda8e17d77a66de7bc87c9ab79ba8f	907	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr	116521866	f	-1
936	1	586	92	1016252642	b6cda8e17d77a66de7bc87c9ab79ba8f	907	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr	116521866	f	-1
937	1	477	4	-2319136070	ec813e7f-14b2-4638-8ace-4ea5b54f5a69	908	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteManager	3366162772	t	-1
938	1	484	92	1185381727	619626a105e3cee9e05d52e3c8871120	908	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteManager	3366162772	f	-1
939	1	586	92	1185381727	619626a105e3cee9e05d52e3c8871120	908	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteManager	3366162772	f	-1
940	1	907	93	1185381727	619626a105e3cee9e05d52e3c8871120	908	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteManager	3366162772	f	-1
941	1	477	4	-3714506166	db2e36de-6ae1-4e46-baa6-dc93fa37e61a	909	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteCollaborator	724814763	t	-1
942	1	484	92	2213625394	4a8da09d9b0e7322b5d2a00347931eed	909	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteCollaborator	724814763	f	-1
943	1	586	92	2213625394	4a8da09d9b0e7322b5d2a00347931eed	909	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteCollaborator	724814763	f	-1
944	1	907	93	2213625394	4a8da09d9b0e7322b5d2a00347931eed	909	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteCollaborator	724814763	f	-1
945	1	477	4	-970316501	89dc1532-0859-4e00-a88a-eb82273f9a0e	910	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteContributor	2151155638	t	-1
946	1	484	92	2751046930	290f71d786839d0fa237fdbfdd7de322	910	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteContributor	2151155638	f	-1
947	1	586	92	2751046930	290f71d786839d0fa237fdbfdd7de322	910	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteContributor	2151155638	f	-1
948	1	907	93	2751046930	290f71d786839d0fa237fdbfdd7de322	910	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteContributor	2151155638	f	-1
949	1	477	4	-30631140	4c8e89ee-b21a-49a1-898b-908929c9085f	911	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteConsumer	2933010006	t	-1
950	1	484	92	617155535	1f42a80fc5f1011c4e345e7574d25829	911	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteConsumer	2933010006	f	-1
951	1	586	92	617155535	1f42a80fc5f1011c4e345e7574d25829	911	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteConsumer	2933010006	f	-1
952	1	907	93	617155535	1f42a80fc5f1011c4e345e7574d25829	911	6	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteConsumer	2933010006	f	-1
953	1	908	93	87753057	190bd5e4-0ba2-4f79-aec4-88cbe7959fc3	903	6	UserSearch-zGiNtcNRnaJPlLO	2816560054	f	-1
954	1	906	33	534011694	surf-config	912	6	surf-config	3685774615	t	-1
955	1	912	33	3997758973	components	913	6	components	3945296900	t	-1
956	1	913	33	3878145677	page.title.site~sitesearch-czxbacayhqqynwr~dash~~~	914	6	page.title.site~SiteSearch-cZXBaCaYHqqynWr~dashboard.xml	3772216941	t	-1
957	1	913	33	3691401628	page.navigation.site~sitesearch-czxbacayhqqynwr~~~	915	6	page.navigation.site~SiteSearch-cZXBaCaYHqqynWr~dashboard.xml	4015425648	t	-1
958	1	913	33	2875009487	page.component-1-1.site~sitesearch-czxbacayhqqy~~~	916	6	page.component-1-1.site~SiteSearch-cZXBaCaYHqqynWr~dashboard.xml	3830112078	t	-1
959	1	913	33	3787742907	page.component-2-1.site~sitesearch-czxbacayhqqy~~~	917	6	page.component-2-1.site~SiteSearch-cZXBaCaYHqqynWr~dashboard.xml	2933106746	t	-1
960	1	913	33	1515143956	page.component-2-2.site~sitesearch-czxbacayhqqy~~~	918	6	page.component-2-2.site~SiteSearch-cZXBaCaYHqqynWr~dashboard.xml	358135189	t	-1
961	1	912	33	544531829	pages	919	6	pages	3167021155	t	-1
962	1	919	33	1766001124	site	920	6	site	3494426144	t	-1
963	1	920	33	3121889924	sitesearch-czxbacayhqqynwr	921	6	SiteSearch-cZXBaCaYHqqynWr	252294081	t	-1
964	1	921	33	631660036	dashboard.xml	922	6	dashboard.xml	4036615692	t	-1
965	1	906	33	3872509529	documentlibrary	923	6	documentLibrary	202189778	t	-1
966	1	923	33	595805133	folder-tniecquwtkwqyro	924	6	folder-TNiecQUWtKwqYRo	212270655	t	-1
967	1	924	33	3449493002	file-gzzbkqpjqzrnhpg.txt	925	6	file-GZzBkqpJQZrNHpg.txt	2783850585	t	-1
968	1	10	164	2624601333	a170644e-906e-427c-8909-99b62d445246	926	5	a170644e-906e-427c-8909-99b62d445246	3697024515	t	-1
969	1	926	166	-2531102431	912d8fc1-706c-421b-b8b2-6d1a3edf3563	927	5	version-0	3486964613	t	-1
973	1	13	33	850733934	acl-benchmark	931	6	acl-benchmark	3884624742	t	-1
974	1	931	33	933606703	folder-1	932	6	folder-1	1371645872	t	-1
978	1	934	33	2916110238	direct being edition am.pptx	936	6	Direct being edition am.pptx	864269757	t	-1
982	1	934	33	1980142354	effects html.txt	940	6	Effects html.txt	3805219956	t	-1
914	1	11	4	-3074173621	51973315-7c22-4cb4-92db-9f789352e028	887	1	archivedItem	3449115622	t	-1
915	1	11	241	2282622326	admin	888	6	admin	347996256	t	-1
916	1	888	242	-2613810698	a340321b-1e58-409d-8c96-2106b0f3e4ff	887	1	deleted	2384143574	f	-1
917	1	887	142	-2330208450	bce8c7a0-c836-48ff-97fe-938b2c17a921	889	6	pdf	1671108346	t	-1
918	1	887	142	-404132143	99d62fd4-5449-4229-81d3-bc9c30a4ce3d	890	6	doclib	2991633180	t	-1
922	1	454	33	652155628	finance-model.xml	897	6	finance-model.xml	2889426404	t	-1
923	1	10	164	3536906676	c38fe89b-3ed3-4d26-84ea-be53bbcd2408	898	5	c38fe89b-3ed3-4d26-84ea-be53bbcd2408	2466040642	t	-1
924	1	898	166	-4292840475	64d5c7fe-1bb8-4b0c-a781-1bb2d541566e	899	5	version-0	3486964613	t	-1
984	1	934	33	1851055950	non activities has.pptx	942	6	Non activities has.pptx	3156606558	t	-1
986	1	934	33	2189696310	step counter already.txt	944	6	Step counter already.txt	2682354803	t	-1
990	1	934	33	4079653387	please discounted base alaska.pptx	948	6	Please discounted base alaska.pptx	1587779801	t	-1
994	1	934	33	2075268751	least under.jpg	952	6	Least under.jpg	3591828486	t	-1
998	1	934	33	3150843480	others solutions daughter address.docx	956	6	Others solutions daughter address.docx	2015495883	t	-1
1002	1	934	33	886596672	decent pr dollar.jpg	960	6	Decent pr dollar.jpg	830474248	t	-1
1019	1	934	33	343465059	o options her friend.jpg	977	6	O options her friend.jpg	152943910	t	-1
1023	1	934	33	2984545826	mortgage mar.pdf	981	6	Mortgage mar.pdf	623710532	t	-1
1027	1	934	33	388093290	landing introduce finance.pptx	985	6	Landing introduce finance.pptx	4108330842	t	-1
1031	1	934	33	3386065048	shall improve platforms.txt	989	6	Shall improve platforms.txt	4273782943	t	-1
1043	1	934	33	2957437970	weather appear tours.txt	1001	6	Weather appear tours.txt	2904788311	t	-1
1048	1	934	33	3070139623	free struggle patrick.txt	1006	6	Free struggle patrick.txt	2958380185	t	-1
1076	1	934	33	1558866825	working house.jpg	1034	6	Working house.jpg	4172244603	t	-1
1114	1	935	33	4290078310	being air payday.pdf	1073	6	Being air payday.pdf	4209838638	t	-1
1130	1	935	33	3978382172	my carrying call.jpg	1088	6	My carrying call.jpg	3900253972	t	-1
1133	1	935	33	2544602930	living set financial examples.txt	1091	6	Living set financial examples.txt	2032737112	t	-1
1139	1	935	33	1466277206	topics apnic model.pptx	1097	6	Topics apnic model.pptx	2232728646	t	-1
1142	1	935	33	3705662790	within comments job.docx	1100	6	Within comments job.docx	3250228227	t	-1
1151	1	935	33	2318770847	file offers execute.pdf	1109	6	File offers execute.pdf	1480885135	t	-1
1155	1	935	33	831526679	companies rating.pptx	1114	6	Companies rating.pptx	1234339545	t	-1
1159	1	935	33	3728981555	without due.docx	1117	6	Without due.docx	1250566485	t	-1
1179	1	935	33	637728690	apr nt response.docx	1137	6	Apr nt response.docx	593140730	t	-1
921	1	895	166	-749970536	37c2fc92-fa76-4a1a-bfee-9418a01fb727	896	5	version-0	3486964613	t	-1
925	1	454	33	3956816954	sharding-content-model.xml	900	6	sharding-content-model.xml	3551951712	t	-1
926	1	10	164	4134272057	a77289bc-0602-4a94-9bcf-ac2c2aa02c87	901	5	a77289bc-0602-4a94-9bcf-ac2c2aa02c87	3058163407	t	-1
927	1	901	166	-1298945385	39cc3a09-d656-4115-819c-d930b84466e9	902	5	version-0	3486964613	t	-1
970	1	924	33	1124402172	file-ksihtjbqvnshwkz.txt	928	6	file-ksihTJbQvNShWkz.txt	4281842066	t	-1
971	1	10	164	1902201177	7b0ca868-9d44-4f54-be37-3565f3a0b0fb	929	5	7b0ca868-9d44-4f54-be37-3565f3a0b0fb	827157423	t	-1
972	1	929	166	-3253744610	324fab38-4219-479b-8327-5b0c8bb943b9	930	5	version-0	3486964613	t	-1
975	1	932	33	1084392889	folder-0	933	6	folder-0	650549030	t	-1
976	1	933	33	2925133345	e7003d05-3b90-4e1a-8790-b4751f42cee9-json	934	6	e7003d05-3b90-4e1a-8790-b4751f42cee9-json	2240523296	t	-1
977	1	933	33	1182549988	f2627085-5a7b-4abe-89f2-aa6b82b3f291-json	935	6	f2627085-5a7b-4abe-89f2-aa6b82b3f291-json	1840125413	t	-1
983	1	934	33	4111550193	months misc communications champions.docx	941	6	Months misc communications champions.docx	1797350592	t	-1
985	1	934	33	631000991	nc september garden.docx	943	6	Nc september garden.docx	955854554	t	-1
991	1	934	33	4178740773	reviews college.txt	949	6	Reviews college.txt	2703875554	t	-1
992	1	934	33	2592546967	home ready current.pptx	950	6	Home ready current.pptx	1224081799	t	-1
995	1	934	33	855931155	type url.pptx	953	6	Type url.pptx	1430631150	t	-1
996	1	934	33	956037741	diabetes issues supporters already.pdf	954	6	Diabetes issues supporters already.pdf	4212650750	t	-1
997	1	934	33	1422619445	poland species too arise.pdf	955	6	Poland species too arise.pdf	3398994198	t	-1
999	1	934	33	2568188238	eq present.pptx	957	6	Eq present.pptx	884446151	t	-1
1003	1	934	33	1678742314	must autumn night find.pptx	961	6	Must autumn night find.pptx	1398805293	t	-1
1006	1	934	33	248429138	qualifying am left exclusive.pdf	964	6	Qualifying am left exclusive.pdf	2184969296	t	-1
1011	1	934	33	361853804	based students cancer california.txt	969	6	Based students cancer california.txt	1179291750	t	-1
1012	1	934	33	297422086	julie birthday.jpg	970	6	Julie birthday.jpg	1118673723	t	-1
1015	1	934	33	1176762941	derived general stores.pdf	973	6	Derived general stores.pdf	4053274070	t	-1
1016	1	934	33	3636965785	low purchase call control.jpg	974	6	Low purchase call control.jpg	2050921233	t	-1
1020	1	934	33	2981989206	also apr worth miss.pptx	979	6	Also apr worth miss.pptx	2899896851	t	-1
1035	1	934	33	1201424949	largest glass europe.txt	993	6	Largest glass europe.txt	1526279024	t	-1
1039	1	934	33	4024599924	changed article ohio me.docx	997	6	Changed article ohio me.docx	1907457879	t	-1
1044	1	934	33	3921627358	team messages goal massachusetts.pdf	1002	6	Team messages goal massachusetts.pdf	3127159764	t	-1
1047	1	934	33	3804235392	simply location try proceed.docx	1005	6	Simply location try proceed.docx	1848983682	t	-1
1051	1	934	33	1410793567	acceptance visit coral.pdf	1009	6	Acceptance visit coral.pdf	3819763636	t	-1
1052	1	934	33	2038659710	gone baseball this nov.pdf	1010	6	Gone baseball this nov.pdf	3459812757	t	-1
1055	1	934	33	1581517902	discount dr gear.docx	1013	6	Discount dr gear.docx	642162048	t	-1
1073	1	934	33	3262741796	direct reports keep.pdf	1031	6	Direct reports keep.pdf	268983348	t	-1
1085	1	935	33	3011680577	able management eyes audience.pdf	1043	6	Able management eyes audience.pdf	1560284459	t	-1
1087	1	935	33	3158754009	saddam add edition.pptx	1045	6	Saddam add edition.pptx	1849156553	t	-1
1089	1	935	33	2032955558	downloads version.pptx	1047	6	Downloads version.pptx	92839296	t	-1
1092	1	935	33	3288698304	teams total.jpg	1050	6	Teams total.jpg	1772187465	t	-1
1093	1	935	33	2441781534	still previous springfield.txt	1051	6	Still previous springfield.txt	1917539118	t	-1
1095	1	935	33	748881135	study ontario om.pdf	1053	6	Study ontario om.pdf	704342183	t	-1
1097	1	935	33	3187115503	chip order units.jpg	1055	6	Chip order units.jpg	3098536359	t	-1
1100	1	935	33	560732348	stock unity reserved disaster.docx	1058	6	Stock unity reserved disaster.docx	2363822702	t	-1
1102	1	935	33	1564618268	printers row chapel cbs.docx	1062	6	Printers row chapel cbs.docx	3272725567	t	-1
1103	1	935	33	115259996	fan working center.pptx	1060	6	Fan working center.pptx	3568278348	t	-1
1104	1	935	33	418609320	lab lines.pptx	1061	6	Lab lines.pptx	3684284354	t	-1
1105	1	935	33	79146444	rs show sci radiation.txt	1063	6	Rs show sci radiation.txt	35421618	t	-1
1106	1	935	33	499460797	wicked.jpg	1064	6	Wicked.jpg	1475913876	t	-1
1110	1	935	33	1789036934	real copyright.pdf	1068	6	Real copyright.pdf	968187835	t	-1
1109	1	935	33	2355044023	items not.docx	1067	6	Items not.docx	1328927197	t	-1
987	1	934	33	2826309808	wireless cover drives.pptx	945	6	Wireless cover drives.pptx	533587803	t	-1
988	1	934	33	2254144118	lead type.pptx	947	6	Lead type.pptx	1160858908	t	-1
989	1	934	33	2313997242	screw testing relaxation.jpg	946	6	Screw testing relaxation.jpg	398406041	t	-1
993	1	934	33	1092388584	reserved makes.pptx	951	6	Reserved makes.pptx	422042927	t	-1
1000	1	934	33	3875090983	addition shows start.txt	958	6	Addition shows start.txt	4221326178	t	-1
1001	1	934	33	3244000811	room m da.pptx	959	6	Room m da.pptx	36784449	t	-1
1004	1	934	33	2954083395	logos results york.jpg	962	6	Logos results york.jpg	3434565989	t	-1
1005	1	934	33	2224108802	tools courts style obtained.jpg	963	6	Tools courts style obtained.jpg	2729165916	t	-1
1007	1	934	33	4210215836	civil moved.jpg	965	6	Civil moved.jpg	1465264405	t	-1
1008	1	934	33	1476108105	risk mt projects description.jpg	966	6	Risk mt projects description.jpg	3675183435	t	-1
1009	1	934	33	2781569786	art wedding.docx	967	6	Art wedding.docx	822279580	t	-1
1010	1	934	33	3739814507	breakfast represent.jpg	968	6	Breakfast represent.jpg	211299195	t	-1
1013	1	934	33	482418326	culture smoking.pdf	971	6	Culture smoking.pdf	1157252433	t	-1
1014	1	934	33	2122980882	projects how.pdf	972	6	Projects how.pdf	3930292596	t	-1
1017	1	934	33	284691735	attack end date id.pdf	975	6	Attack end date id.pdf	1818026033	t	-1
1018	1	934	33	4167521697	bill thinking.pptx	976	6	Bill thinking.pptx	2876277660	t	-1
1021	1	934	33	1704826398	creative sale.jpg	978	6	Creative sale.jpg	3252198380	t	-1
1022	1	934	33	2385103723	friend wc active dc.jpg	980	6	Friend wc active dc.jpg	1549331067	t	-1
1024	1	934	33	2115800891	club gear including he.txt	982	6	Club gear including he.txt	3383195856	t	-1
1025	1	934	33	128629995	forums orlando.pdf	983	6	Forums orlando.pdf	1421699798	t	-1
1026	1	934	33	2278248636	child annual.txt	984	6	Child annual.txt	318951386	t	-1
1028	1	934	33	3954224821	and p children.jpg	986	6	And p children.jpg	3097983112	t	-1
1029	1	934	33	1406308974	short economic asia being.jpg	987	6	Short economic asia being.jpg	4046172390	t	-1
1030	1	934	33	769897079	word learning.pdf	988	6	Word learning.pdf	2309406597	t	-1
1032	1	934	33	2040392521	neck j profile.txt	990	6	Neck j profile.txt	713702772	t	-1
1033	1	934	33	1226984909	help hosted video.jpg	991	6	Help hosted video.jpg	824237059	t	-1
1034	1	934	33	2066476258	dvd description.jpg	992	6	Dvd description.jpg	588298021	t	-1
1036	1	934	33	1343674645	rights limited.docx	994	6	Rights limited.docx	137200338	t	-1
1037	1	934	33	124528585	dogs determined later rss.pdf	995	6	Dogs determined later rss.pdf	2778006849	t	-1
1038	1	934	33	4018338692	smooth october big.txt	996	6	Smooth october big.txt	2468746914	t	-1
1040	1	934	33	567081500	posts paper.jpg	998	6	Posts paper.jpg	2355632277	t	-1
1041	1	934	33	2067301286	size entries high.jpg	999	6	Size entries high.jpg	54137448	t	-1
1042	1	934	33	3739319619	air included.pdf	1000	6	Air included.pdf	1244193317	t	-1
1045	1	934	33	1725638209	index d singapore in.jpg	1003	6	Index d singapore in.jpg	2076067588	t	-1
1046	1	934	33	471029148	re dvd yes.pdf	1004	6	Re dvd yes.pdf	3749288694	t	-1
1049	1	934	33	4193589660	recipes.txt	1007	6	Recipes.txt	3138239602	t	-1
1050	1	934	33	2780522312	comparable how bonus.pptx	1008	6	Comparable how bonus.pptx	2735781686	t	-1
1053	1	934	33	491827112	jobs include order.jpg	1011	6	Jobs include order.jpg	1643397774	t	-1
1054	1	934	33	357412561	addressing person.pptx	1012	6	Addressing person.pptx	1776894967	t	-1
1056	1	934	33	3140223309	newsletter aug up models.docx	1014	6	Newsletter aug up models.docx	433398725	t	-1
1057	1	934	33	578682000	mac floppy demonstration.jpg	1015	6	Mac floppy demonstration.jpg	3157132979	t	-1
1058	1	934	33	2715139619	program co utility examination.pdf	1016	6	Program co utility examination.pdf	207402225	t	-1
1059	1	934	33	3102709607	p welcome.pptx	1017	6	P welcome.pptx	2072336397	t	-1
1061	1	934	33	3818200367	there sort.docx	1019	6	There sort.docx	1311899558	t	-1
1062	1	934	33	914518408	party ss world partner.docx	1020	6	Party ss world partner.docx	32371087	t	-1
1063	1	934	33	1480906062	caused philadelphia.jpg	1021	6	Caused philadelphia.jpg	2318791774	t	-1
1065	1	934	33	2944991943	line attendance.txt	1023	6	Line attendance.txt	4155660544	t	-1
1066	1	934	33	871741891	family stats.jpg	1024	6	Family stats.jpg	2805737125	t	-1
1067	1	934	33	2569858941	st saskatchewan shows.jpg	1025	6	St saskatchewan shows.jpg	2676440835	t	-1
1069	1	934	33	1354870533	going fax.pdf	1027	6	Going fax.pdf	914384120	t	-1
1070	1	934	33	4074399660	med.pdf	1028	6	Med.pdf	1598950378	t	-1
1071	1	934	33	1306787809	account profile cars.pdf	1029	6	Account profile cars.pdf	1350999716	t	-1
1074	1	934	33	1646264075	on just keywords.docx	1032	6	On just keywords.docx	438136517	t	-1
1075	1	934	33	3949409937	unless source ok drive.pdf	1033	6	Unless source ok drive.pdf	1557971322	t	-1
1060	1	934	33	123386539	way since jobs.pptx	1018	6	Way since jobs.pptx	1600250220	t	-1
1064	1	934	33	1710188311	optimization file deal.txt	1022	6	Optimization file deal.txt	3528757500	t	-1
1068	1	934	33	1669943293	a lyrics were s.docx	1026	6	A lyrics were s.docx	1724982197	t	-1
1072	1	934	33	483197086	be taken.docx	1030	6	Be taken.docx	2056047459	t	-1
1077	1	934	33	881070165	call lines years states.pptx	1035	6	Call lines years states.pptx	2866276982	t	-1
1080	1	934	33	3149890333	previous methods rating assistant.jpg	1038	6	Previous methods rating assistant.jpg	1530507744	t	-1
1082	1	935	33	3255237457	november their code property.pptx	1039	6	November their code property.pptx	746876731	t	-1
1086	1	935	33	2532564729	sir office widely education.txt	1044	6	Sir office widely education.txt	2965974951	t	-1
1090	1	935	33	1417608148	department neighborhood any.docx	1048	6	Department neighborhood any.docx	3633036758	t	-1
1094	1	935	33	4097833940	transfers students catholic channel.docx	1052	6	Transfers students catholic channel.docx	2693294547	t	-1
1120	1	935	33	4104820558	retro classes project.docx	1078	6	Retro classes project.docx	1125212325	t	-1
1137	1	935	33	479334122	limitation study.jpg	1095	6	Limitation study.jpg	432693922	t	-1
12147483647	1	935	33	1394704899	where lyrics.txt	1099	6	Where lyrics.txt	3353877861	t	-1
1148	1	935	33	3403152898	users metro there building.docx	1106	6	Users metro there building.docx	3974958940	t	-1
1152	1	935	33	4214423030	data university verzeichnis.docx	1110	6	Data university verzeichnis.docx	2009578484	t	-1
1161	1	935	33	787590685	from community point pill.pdf	1119	6	From community point pill.pdf	2349370517	t	-1
1167	1	935	33	397599560	value ebay.txt	1125	6	Value ebay.txt	3570936866	t	-1
1171	1	935	33	2358992049	competitions night.docx	1129	6	Competitions night.docx	1592425889	t	-1
1078	1	934	33	1253585356	between information kids.docx	1036	6	Between information kids.docx	3896578884	t	-1
1081	1	935	33	2321729579	ring wind office.pdf	1040	6	Ring wind office.pdf	2402985059	t	-1
1108	1	935	33	289941030	however every.pptx	1066	6	However every.pptx	1113583643	t	-1
1112	1	935	33	4139400561	center operates work years.txt	1070	6	Center operates work years.txt	360435521	t	-1
1113	1	935	33	2110376464	meetings using bench kingdom.pptx	1071	6	Meetings using bench kingdom.pptx	2471190138	t	-1
1118	1	935	33	798812714	wish devil price.pptx	1077	6	Wish devil price.pptx	1470004196	t	-1
1124	1	935	33	1814481710	turned wy.txt	1081	6	Turned wy.txt	174522579	t	-1
1131	1	935	33	3482832580	atmosphere fell half sign.txt	1089	6	Atmosphere fell half sign.txt	1835985996	t	-1
1135	1	935	33	1542574361	b failed.docx	1093	6	B failed.docx	1034951396	t	-1
1164	1	935	33	2775359037	regional seller.docx	1122	6	Regional seller.docx	2687824501	t	-1
1166	1	935	33	2942550216	interview together.txt	1124	6	Interview together.txt	3553045998	t	-1
1170	1	935	33	3004974187	annual discount.txt	1128	6	Annual discount.txt	3945209772	t	-1
1174	1	935	33	973272255	forward jobs.pptx	1133	6	Forward jobs.pptx	2655517005	t	-1
1177	1	935	33	2396037818	her find intelligence posts.pdf	1135	6	Her find intelligence posts.pdf	2833929188	t	-1
1084	1	935	33	3165641410	mail keep product.docx	1042	6	Mail keep product.docx	3221960676	t	-1
1096	1	935	33	790653597	today wa interview analysis.pptx	1054	6	Today wa interview analysis.pptx	2748656799	t	-1
1099	1	935	33	4063569672	person set.pdf	1057	6	Person set.pdf	828368994	t	-1
1115	1	935	33	4229404816	coast ago as write.docx	1074	6	Coast ago as write.docx	778484096	t	-1
1119	1	935	33	991535349	holidays dealt.txt	1076	6	Holidays dealt.txt	1745778376	t	-1
1122	1	935	33	1016038954	display please hunt.jpg	1080	6	Display please hunt.jpg	4009666362	t	-1
1126	1	935	33	650992957	martial between thumbs.docx	1084	6	Martial between thumbs.docx	295886138	t	-1
1140	1	935	33	3977538225	goes own hardware.txt	1098	6	Goes own hardware.txt	2501311359	t	-1
1165	1	935	33	21876670	adopted usa.txt	1123	6	Adopted usa.txt	2900962615	t	-1
1169	1	935	33	4135709532	desired score oct june.pdf	1127	6	Desired score oct june.pdf	1094327479	t	-1
1083	1	935	33	307165264	general performance plan.pdf	1041	6	General performance plan.pdf	2350711411	t	-1
1088	1	935	33	1721792767	disease refers chem.jpg	1046	6	Disease refers chem.jpg	3033618927	t	-1
1091	1	935	33	373427021	wells maritime ringtones.pdf	1049	6	Wells maritime ringtones.pdf	2282877294	t	-1
1098	1	935	33	2377738337	we away la.pdf	1056	6	We away la.pdf	1322493707	t	-1
1101	1	935	33	2668932078	says beauty.txt	1059	6	Says beauty.txt	850413927	t	-1
1107	1	935	33	3955762768	committed about mills.docx	1065	6	Committed about mills.docx	1551094203	t	-1
1128	1	935	33	3950378080	wordpress.pptx	1086	6	Wordpress.pptx	673133322	t	-1
1143	1	935	33	3974823422	registered boy amount.txt	1101	6	Registered boy amount.txt	3930140032	t	-1
1146	1	935	33	2421823632	times recognition.txt	1104	6	Times recognition.txt	3898116446	t	-1
1156	1	935	33	309041133	lynn golf.pdf	1113	6	Lynn golf.pdf	1948933136	t	-1
1160	1	935	33	2338649997	based planning categories.pdf	1118	6	Based planning categories.pdf	698209541	t	-1
1162	1	935	33	1090004375	move by position would.txt	1120	6	Move by position would.txt	4148417148	t	-1
1176	1	935	33	3186621902	rights melissa.pptx	1134	6	Rights melissa.pptx	3855918601	t	-1
1111	1	935	33	1227842862	focused reading.pdf	1069	6	Focused reading.pdf	286539497	t	-1
1116	1	935	33	3855647424	nonprofit obviously me together.txt	1072	6	Nonprofit obviously me together.txt	2376120370	t	-1
1136	1	935	33	4230935350	advanced university build.txt	1094	6	Advanced university build.txt	1591202238	t	-1
1154	1	935	33	247888930	schools air our.pdf	1112	6	Schools air our.pdf	1459431397	t	-1
1157	1	935	33	1760792969	public boxes methods mark.pdf	1116	6	Public boxes methods mark.pdf	3389698817	t	-1
1117	1	935	33	415275930	url singh.pptx	1075	6	Url singh.pptx	3685373168	t	-1
1121	1	935	33	2750627407	thought law.jpg	1079	6	Thought law.jpg	240639174	t	-1
1127	1	935	33	1194439800	automatically association.txt	1085	6	Automatically association.txt	3855128304	t	-1
1132	1	935	33	2908280349	july business.jpg	1090	6	July business.jpg	152951791	t	-1
1145	1	935	33	1924359893	xp organ paxil marketing.jpg	1105	6	Xp organ paxil marketing.jpg	3974143222	t	-1
1150	1	935	33	2189607058	international attempt.jpg	1108	6	International attempt.jpg	2217283820	t	-1
1175	1	935	33	750382948	disco library.txt	1132	6	Disco library.txt	2298274454	t	-1
1180	1	935	33	1619813344	instructions after so holiday.pptx	1138	6	Instructions after so holiday.pptx	3439683890	t	-1
1123	1	935	33	3941195705	people software.docx	1082	6	People software.docx	4021400561	t	-1
1125	1	935	33	497120920	composer rates into testing.txt	1083	6	Composer rates into testing.txt	1000048582	t	-1
1129	1	935	33	1815008479	international providers her.jpg	1087	6	International providers her.jpg	1242834305	t	-1
1134	1	935	33	2611364139	happen search.txt	1092	6	Happen search.txt	1071860953	t	-1
1138	1	935	33	2665485446	forced sublime instead.pptx	1096	6	Forced sublime instead.pptx	2844759169	t	-1
1144	1	935	33	3811730769	working specify dominican.jpg	1102	6	Working specify dominican.jpg	1103841241	t	-1
1147	1	935	33	2789233268	range home.pptx	1103	6	Range home.pptx	199536893	t	-1
1149	1	935	33	1445491146	beliefs annex image.jpg	1107	6	Beliefs annex image.jpg	2220445914	t	-1
1153	1	935	33	117228964	gain app.pptx	1111	6	Gain app.pptx	1623002713	t	-1
1163	1	935	33	3034965261	arts double.pdf	1121	6	Arts double.pdf	423692164	t	-1
1158	1	935	33	1100521437	resource california washington.pptx	1115	6	Resource california washington.pptx	703112495	t	-1
1168	1	935	33	3262347330	systems sort guide.pdf	1126	6	Systems sort guide.pdf	3201799524	t	-1
1172	1	935	33	863867312	vermont hotel smith before.txt	1130	6	Vermont hotel smith before.txt	3502007168	t	-1
1173	1	935	33	3839170436	genome production.jpg	1131	6	Genome production.jpg	2631370314	t	-1
1178	1	935	33	536138414	titled.txt	1136	6	Titled.txt	1439256711	t	-1
1181	1	472	49	4151227546	tag1	1139	6	tag1	1315244894	t	-1
1182	1	472	49	4151227546	tag2	1140	6	tag2	1315244894	t	-1
1183	1	472	49	4151227546	tag3	12147483647	6	tag3	1315244894	t	-1
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
244	0	244	6	1	2
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
252	0	252	1	1	2
253	0	253	1	1	2
272	0	272	1	1	2
273	0	272	1	1	2
276	0	276	1	1	2
277	0	276	1	1	2
254	0	254	1	1	2
255	0	255	1	1	2
256	0	256	1	1	2
257	0	257	1	1	2
258	0	258	1	1	2
260	0	259	2	1	2
262	0	261	9	1	2
263	0	263	8	1	2
269	0	264	2	1	2
270	0	266	9	1	2
271	0	268	8	1	2
274	0	274	1	1	2
275	0	274	1	1	2
278	0	278	6	1	2
279	0	279	6	1	2
280	0	280	6	1	2
281	0	281	6	1	2
282	0	282	6	1	2
283	0	283	6	1	2
284	0	284	2	1	2
285	0	285	2	3	2
286	0	285	2	3	2
287	0	287	2	3	2
288	0	287	2	3	2
\.


--
-- Data for Name: alf_content_url; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_content_url (id, content_url, content_url_short, content_url_crc, content_size, orphan_time) FROM stdin;
1	store://2021/4/21/12/37/b6f65827-0c89-4fad-9d84-e2427274bac8.bin	7274bac8.bin	1525078182	6083	\N
2	store://2021/4/21/12/37/2088007e-abf5-43d8-bb0b-bfd94c866574.bin	4c866574.bin	2240543595	3845	\N
3	store://2021/4/21/12/37/5008702a-04fc-4a7f-8715-0a8c2dff5562.bin	2dff5562.bin	247889793	1026	\N
4	store://2021/4/21/12/37/8c56123a-420c-4938-9b1a-d330b8a321df.bin	b8a321df.bin	2006129843	319	\N
5	store://2021/4/21/12/37/a2c3754a-574a-417a-bc79-74a699dd5687.bin	99dd5687.bin	3357831484	549	\N
6	store://2021/4/21/12/37/dee1f093-3d11-46a0-8e60-915fc43267c8.bin	c43267c8.bin	2671565993	2625	\N
7	store://2021/4/21/12/37/8228c431-0e28-4675-96ea-c27b17a4a9c8.bin	17a4a9c8.bin	3258848572	555	\N
8	store://2021/4/21/12/37/3b9bbe60-197e-46fa-9d6b-3f51c016ce99.bin	c016ce99.bin	568527284	923	\N
9	store://2021/4/21/12/37/a8d78891-0082-4f9f-bd45-f5b3024b3e26.bin	024b3e26.bin	1022356262	690	\N
10	store://2021/4/21/12/37/ad17396f-9dcc-42f7-b08a-667e53335dc6.bin	53335dc6.bin	2918771990	715	\N
11	store://2021/4/21/12/37/5df27763-6e0a-4664-b106-13fa047606e1.bin	047606e1.bin	3877753997	643	\N
12	store://2021/4/21/12/37/98acd0bb-4e3f-478e-8a8a-35a628d33357.bin	28d33357.bin	1663733247	1106	\N
13	store://2021/4/21/12/37/f6df1fd6-dd19-44ec-8801-9e0e071fdbfd.bin	071fdbfd.bin	327865283	1963	\N
14	store://2021/4/21/12/37/f22c392c-6201-44d0-a287-009fa440a3d2.bin	a440a3d2.bin	3748136099	641	\N
15	store://2021/4/21/12/37/f7bbd2af-978e-435b-ad3a-825fda9c2cf2.bin	da9c2cf2.bin	3142920720	3500	\N
16	store://2021/4/21/12/37/c542dd3a-867c-4b06-b49f-ba3b911dd567.bin	911dd567.bin	845256988	909	\N
17	store://2021/4/21/12/37/8ce36ea0-b9fe-4872-9680-80920fa6e71b.bin	0fa6e71b.bin	2387954103	489	\N
18	store://2021/4/21/12/37/9e576e4d-e617-4dc3-877b-f33f00a3c94b.bin	00a3c94b.bin	1883753058	539	\N
19	store://2021/4/21/12/37/6141fad6-a826-4ca3-8bbf-ab5c2498c280.bin	2498c280.bin	2575134259	9132	\N
20	store://2021/4/21/12/37/d7b02ec3-7efa-43e8-8206-413e0281ca2f.bin	0281ca2f.bin	3131953715	9140	\N
21	store://2021/4/21/12/37/3dc1050e-dccb-4482-8522-8978a3d2d127.bin	a3d2d127.bin	3028592226	9148	\N
22	store://2021/4/21/12/37/fb053441-2d82-4ab5-b5e5-15dd17f975b3.bin	17f975b3.bin	2156720422	9150	\N
23	store://2021/4/21/12/37/9e82cf92-d668-44fa-a1f9-decdef97f79c.bin	ef97f79c.bin	3157058957	9134	\N
24	store://2021/4/21/12/37/1bd11d7c-ae8f-4d64-873b-3d8cad0df834.bin	ad0df834.bin	1814375826	9165	\N
25	store://2021/4/21/12/37/b1ff1ac2-acbf-462a-a77c-30449b5b6a73.bin	9b5b6a73.bin	612189233	9125	\N
26	store://2021/4/21/12/37/d13c8086-f28b-4a1e-9d01-1665921387da.bin	921387da.bin	2156644910	3701	\N
27	store://2021/4/21/12/37/089546b5-5397-46a4-a40d-d5902f547654.bin	2f547654.bin	3257417801	3721	\N
28	store://2021/4/21/12/37/27991554-81c0-44d7-ad87-921dee5a613c.bin	ee5a613c.bin	2495166174	3717	\N
29	store://2021/4/21/12/37/7348b2ab-ac7d-4813-a88b-17b767b3274f.bin	67b3274f.bin	2135448319	3708	\N
30	store://2021/4/21/12/37/147e3a3e-fa74-4122-9eeb-02b5b57dfc58.bin	b57dfc58.bin	773430531	3701	\N
31	store://2021/4/21/12/37/c4df18e0-8788-4782-9f53-9297f6dbc3cb.bin	f6dbc3cb.bin	2956095906	3780	\N
32	store://2021/4/21/12/37/24fcc0e1-c813-48a8-9b2a-77e8cc436df0.bin	cc436df0.bin	3098974507	3700	\N
33	store://2021/4/21/12/37/c5fc81b9-c502-48e2-8f65-b9bd3cce36d6.bin	3cce36d6.bin	393264554	10436	\N
34	store://2021/4/21/12/37/353e82bc-a117-413c-971e-4c846480cd51.bin	6480cd51.bin	1144358053	10348	\N
35	store://2021/4/21/12/37/429b9b74-294c-43ac-842c-8fd2af36a1ca.bin	af36a1ca.bin	1958132505	10297	\N
36	store://2021/4/21/12/37/31b37863-73cb-4bbf-8c99-fe07ca1e4fca.bin	ca1e4fca.bin	1612313047	10299	\N
37	store://2021/4/21/12/37/117f0845-765e-43f4-b910-6d848a701871.bin	8a701871.bin	828598160	10318	\N
38	store://2021/4/21/12/37/bebde7b4-7080-4b67-84bd-30fc33f1d23f.bin	33f1d23f.bin	2208671729	10353	\N
39	store://2021/4/21/12/37/612857d2-a907-42c3-aacb-41fd5fe7893e.bin	5fe7893e.bin	4110810551	10137	\N
40	store://2021/4/21/12/37/5c363207-1d7e-40b4-8b5f-3743b76d7a00.bin	b76d7a00.bin	3704519474	1859	\N
41	store://2021/4/21/12/37/7de91e70-fe57-4d66-8737-53b9aec8887b.bin	aec8887b.bin	992583402	596	\N
42	store://2021/4/21/12/37/4d656233-5cb6-4cd9-9e77-b414a0c99507.bin	a0c99507.bin	404869163	2271	\N
43	store://2021/4/21/12/37/df4e3502-d29f-48be-85ec-956518211351.bin	18211351.bin	2526857	1061	\N
44	store://2021/4/21/12/37/b7bc110e-2b96-4762-bd02-61aa928d3fd8.bin	928d3fd8.bin	666566962	341	\N
45	store://2021/4/21/12/37/35bc6f04-d787-4062-bb70-f661087bd104.bin	087bd104.bin	1169593537	535	\N
46	store://2021/4/21/12/37/e2aaeb01-117b-42e9-af03-05df6f255449.bin	6f255449.bin	1342850811	118	\N
47	store://2021/4/21/12/37/96884e10-8fce-4210-af99-8cc7f3481bf2.bin	f3481bf2.bin	2969406126	1349	\N
48	store://2021/4/21/12/37/998d0a28-a950-4207-b6f0-befe2922f6f0.bin	2922f6f0.bin	1019287951	1252	\N
49	store://2021/4/21/12/37/4a9a6f8e-e7cc-41ef-83e0-05740c446f83.bin	0c446f83.bin	2571909751	1252	\N
50	store://2021/4/21/12/37/6241203d-3525-4d65-84d3-c77fbac6fa76.bin	bac6fa76.bin	2692598707	1252	\N
51	store://2021/4/21/12/37/279f1ab1-6993-4c29-b8ab-d2847ea51feb.bin	7ea51feb.bin	3125514588	314	\N
52	store://2021/4/21/12/37/2ec85f18-a754-4d5d-b20f-7a571b999f73.bin	1b999f73.bin	480876873	878	\N
53	store://2021/4/21/12/37/9c632e0c-decf-4aa0-b45b-1e7f0206cb6c.bin	0206cb6c.bin	2806626336	543	\N
54	store://2021/4/21/12/37/6c9f5fdd-afe0-43b0-ac84-6c79c3d5089b.bin	c3d5089b.bin	391712988	469	\N
55	store://2021/4/21/12/37/9d3ac886-915b-4a91-b522-b2e116a6ba7c.bin	16a6ba7c.bin	686178354	60	\N
56	store://2021/4/21/12/37/b24d0e51-6517-4d69-996d-ef3417f89d97.bin	17f89d97.bin	3236918721	235	\N
57	store://2021/4/21/12/37/4e74f9fc-0fa5-4316-805c-3d045427aa74.bin	5427aa74.bin	1532281291	441	\N
58	store://2021/4/21/12/37/8c38f769-9476-41aa-a67e-4dfb2e6d4e97.bin	2e6d4e97.bin	39439156	885	\N
59	store://2021/4/21/12/37/5b536d4a-ddcd-4d0f-b87d-26a93683f36e.bin	3683f36e.bin	1690433414	369	\N
60	store://2021/4/21/12/37/6c9e5aa8-0808-4c48-81e2-8c98882fddfd.bin	882fddfd.bin	791508256	481	\N
61	store://2021/4/21/12/37/cf1a67f4-701c-4d12-ad99-1bcb53aaeed8.bin	53aaeed8.bin	1983893089	60	\N
62	store://2021/4/21/12/37/532c6a1d-0f10-4dd5-a184-2a50251b95ff.bin	251b95ff.bin	2367173777	235	\N
63	store://2021/4/21/12/37/5f720a4e-f122-4ea7-bc06-cbc07b7d22fa.bin	7b7d22fa.bin	2966468109	334	\N
64	store://2021/4/21/12/37/013413fc-d97e-4668-a302-c3d3f8426c7a.bin	f8426c7a.bin	691609846	1199	\N
65	store://2021/4/21/12/37/349cbcef-74fc-4f67-a88b-ef29e367f41f.bin	e367f41f.bin	779829291	347	\N
66	store://2021/4/21/12/37/01c2dfad-f51b-40c7-a227-80a872b576e4.bin	72b576e4.bin	518653142	904	\N
67	store://2021/4/21/12/37/314e0acd-4f8c-4e59-af1a-cf166c95f34f.bin	6c95f34f.bin	1080093751	1116	\N
68	store://2021/4/21/12/37/432e1778-7c01-4ff3-96d0-5a60fdc83e4b.bin	fdc83e4b.bin	2144241441	735	\N
69	store://2021/4/21/12/37/d13f1b1d-bc49-4960-af1e-6cbef370a2e6.bin	f370a2e6.bin	3640797613	2294	\N
70	store://2021/4/21/12/37/d5e416cd-a564-433b-8f7b-e19309a1e407.bin	09a1e407.bin	1998269329	5093	\N
71	store://2021/4/21/12/37/a8928e5a-5484-40de-95d6-69d638aadbe6.bin	38aadbe6.bin	1461324473	5196	\N
72	store://2021/4/21/12/37/295f3358-bcf9-40d8-a2cb-f74868b1cc86.bin	68b1cc86.bin	893297874	5155	\N
73	store://2021/4/21/12/37/efb83248-c212-4639-8edb-99610a8b460f.bin	0a8b460f.bin	2825725297	5144	\N
74	store://2021/4/21/12/37/af1f1558-b49f-4df5-9d14-f37dc871b2a3.bin	c871b2a3.bin	621999250	5171	\N
75	store://2021/4/21/12/37/428275f2-8bcd-412a-b3d8-2f23f37146e0.bin	f37146e0.bin	604745455	5361	\N
76	store://2021/4/21/12/37/e4271ada-ccf7-423e-8f7b-790456647e4c.bin	56647e4c.bin	1046367504	5086	\N
77	store://2021/4/21/12/37/9f2d2e6a-9b69-4f0c-b2a4-ca1b32ab66b3.bin	32ab66b3.bin	373434979	6069	\N
78	store://2021/4/21/12/37/6281af97-5e25-4ae5-9b91-29a9430b0b7f.bin	430b0b7f.bin	2775591365	6180	\N
79	store://2021/4/21/12/37/fccd31d4-1209-448b-a5f1-73ec64dc4cbf.bin	64dc4cbf.bin	2567051401	6120	\N
80	store://2021/4/21/12/37/a6fc8594-3f97-46b1-bcfd-a0e07f0b25df.bin	7f0b25df.bin	1814606612	6144	\N
81	store://2021/4/21/12/37/1a135a5e-635a-4646-9399-b0ae5167534e.bin	5167534e.bin	4081787476	6156	\N
82	store://2021/4/21/12/37/2a59757e-f000-4145-8d53-382d695b99cb.bin	695b99cb.bin	2044795268	6333	\N
83	store://2021/4/21/12/37/9dea3a15-642b-4d0b-9369-47a88e18a622.bin	8e18a622.bin	689459235	6125	\N
84	store://2021/4/21/12/37/93c0a06c-d417-406f-81d9-b55d10a8ec82.bin	10a8ec82.bin	3524779380	5321	\N
85	store://2021/4/21/12/37/be35165d-b05e-4560-9ca3-6fb6c4e43aae.bin	c4e43aae.bin	3978360060	5301	\N
86	store://2021/4/21/12/37/83fcd4a1-7456-4045-bea8-3e22a6679bfd.bin	a6679bfd.bin	2790922544	5301	\N
87	store://2021/4/21/12/37/96330f86-602d-4651-89af-a35c8e05d1e0.bin	8e05d1e0.bin	1673576614	5301	\N
88	store://2021/4/21/12/37/5b2056e9-86ab-4632-88f1-5ffc95ec4b42.bin	95ec4b42.bin	3137649424	5301	\N
89	store://2021/4/21/12/37/60fecd66-7763-4c8e-8436-51cd5113a46d.bin	5113a46d.bin	2161126876	5301	\N
90	store://2021/4/21/12/37/492a535e-bf2e-4be5-9b3a-c6223ae6d676.bin	3ae6d676.bin	299881939	5301	\N
91	store://2021/4/21/12/37/0854eb43-60ed-4912-9b6f-1b9f630b0a47.bin	630b0a47.bin	3089228042	4572	\N
92	store://2021/4/21/12/37/ec4985c3-a50f-4c8e-89a1-552bb8750e51.bin	b8750e51.bin	2267938003	4120	\N
93	store://2021/4/21/12/37/768f727d-8236-4c41-9d99-d863e33668d3.bin	e33668d3.bin	2258156174	4225	\N
94	store://2021/4/21/12/37/548aecf0-2e75-471e-8198-fd4c0a029d9e.bin	0a029d9e.bin	2641707647	4210	\N
95	store://2021/4/21/12/37/d1bbeb85-756f-437e-9153-5c8b3166accc.bin	3166accc.bin	2862612738	4205	\N
96	store://2021/4/21/12/37/7c51800f-6028-4a12-9295-22b2713aa1d7.bin	713aa1d7.bin	4112831274	4213	\N
97	store://2021/4/21/12/37/1460c8e1-5a2a-4474-97c6-87ca235c3775.bin	235c3775.bin	2514174841	4355	\N
98	store://2021/4/21/12/37/14fd0b70-9527-4622-a61b-1fb23a46358f.bin	3a46358f.bin	1422068423	4060	\N
99	store://2021/4/21/12/37/8420c22e-bc36-49de-b148-1ba677d0a3df.bin	77d0a3df.bin	4176267132	1067	\N
100	store://2021/4/21/12/37/a1fa133d-ba03-46f3-8ec9-a62f27532086.bin	27532086.bin	3751147307	1101	\N
101	store://2021/4/21/12/37/5dc4f2f0-cca5-4505-a47f-5c67007f8c31.bin	007f8c31.bin	756734925	3144	\N
102	store://2021/4/21/12/37/a037e08b-9f60-4a99-8d3c-d875e4544fa4.bin	e4544fa4.bin	676362618	3442	\N
103	store://2021/4/21/12/37/4c656662-b414-4cc7-a7fd-663801baa989.bin	01baa989.bin	1401315547	1091	\N
104	store://2021/4/21/12/37/d695ae77-acb4-4aa0-9c88-f815f150f0f5.bin	f150f0f5.bin	3004900853	1121	\N
105	store://2021/4/21/12/37/3b93e85d-5caf-4fc2-9080-1b33b4fa6d00.bin	b4fa6d00.bin	2714591751	3086	\N
106	store://2021/4/21/12/37/52b55510-ae1a-44f0-987b-7d3f2cc78af3.bin	2cc78af3.bin	4233165164	3462	\N
107	store://2021/4/21/12/37/d229f8f8-9bdf-4cee-ac67-2577b0f18e1f.bin	b0f18e1f.bin	1798672321	1121	\N
108	store://2021/4/21/12/37/4bfb3170-cdf4-4087-90af-4e5cd925c7f0.bin	d925c7f0.bin	2428834903	1157	\N
109	store://2021/4/21/12/37/570d6598-0f2d-4550-b834-07aaae205386.bin	ae205386.bin	2102395063	3106	\N
110	store://2021/4/21/12/37/f7b19109-9702-4c8d-b043-3fd7f6d408dd.bin	f6d408dd.bin	3131292333	3480	\N
111	store://2021/4/21/12/37/c27794eb-3f01-4bb3-ba65-dce87b266009.bin	7b266009.bin	3812890321	1133	\N
112	store://2021/4/21/12/37/6ef8d235-2a56-4341-8e53-fae8b489224c.bin	b489224c.bin	4151573301	1169	\N
113	store://2021/4/21/12/37/e6c2e1dd-e432-43b7-9833-f807f90a610d.bin	f90a610d.bin	2993955023	3111	\N
114	store://2021/4/21/12/37/413109b1-48a7-4c95-a8d7-849cc9a1b18e.bin	c9a1b18e.bin	1483714170	3490	\N
115	store://2021/4/21/12/37/df58572b-55ff-4ca0-9b8b-673d9ffbba28.bin	9ffbba28.bin	2063378765	1123	\N
116	store://2021/4/21/12/37/ec3c2176-5382-4c9f-bf87-9e13ef512ccf.bin	ef512ccf.bin	3072515170	1161	\N
117	store://2021/4/21/12/37/fb63ca17-3a0e-4795-b5c7-3a12048c3169.bin	048c3169.bin	730352019	3101	\N
118	store://2021/4/21/12/37/e81c672d-6057-458e-8218-c970335b3c9e.bin	335b3c9e.bin	448839034	3479	\N
119	store://2021/4/21/12/37/a38c9b32-1e0a-4822-b270-4460cc5cfdba.bin	cc5cfdba.bin	431618241	1138	\N
120	store://2021/4/21/12/37/86fad478-a9b5-40d8-97e4-dd4c6eea0ce9.bin	6eea0ce9.bin	3218533168	1168	\N
121	store://2021/4/21/12/37/01d315c1-0185-466d-be84-4f6fd6916a92.bin	d6916a92.bin	32863004	3149	\N
122	store://2021/4/21/12/37/79dbfc2b-eb99-43a2-a13a-2a7379a5ef43.bin	79a5ef43.bin	4195509902	3521	\N
123	store://2021/4/21/12/37/39bf2450-9a6d-4b31-a97d-4508bbef43ba.bin	bbef43ba.bin	4133837038	1097	\N
124	store://2021/4/21/12/37/0d9e0718-7b59-4983-a430-c72631b1d147.bin	31b1d147.bin	3073173586	1134	\N
125	store://2021/4/21/12/37/3041134c-712b-4216-8998-eadd68e8a023.bin	68e8a023.bin	2916709680	3100	\N
126	store://2021/4/21/12/37/e99c8ae6-75b6-4418-9537-d79ebb03e635.bin	bb03e635.bin	1761392908	3477	\N
127	store://2021/4/21/12/37/eae748d5-70e6-4457-93e0-72c3cc17553f.bin	cc17553f.bin	1399257696	1068	\N
128	store://2021/4/21/12/37/a343ab97-7e81-4fb1-9511-9e2d26ae7b16.bin	26ae7b16.bin	1135236634	1099	\N
129	store://2021/4/21/12/37/d4b481c9-a42d-4220-99e6-a2bcd824e7a3.bin	d824e7a3.bin	1378794137	3080	\N
130	store://2021/4/21/12/37/bdddcb30-2b2d-4047-bdb3-e4f2cbf9b480.bin	cbf9b480.bin	4122376509	3458	\N
131	store://2021/4/21/12/37/8697b9d4-51ff-4d43-ae5e-28ece27bf4c1.bin	e27bf4c1.bin	163655480	1107	\N
132	store://2021/4/21/12/37/186f0a79-0e08-4f45-9706-9926abd24033.bin	abd24033.bin	3301380487	1143	\N
133	store://2021/4/21/12/37/074abf40-85ce-41ae-bff7-f94b5b496354.bin	5b496354.bin	341597687	3098	\N
134	store://2021/4/21/12/37/664b8530-45a1-4ad5-b2ec-e3a4cfc152d6.bin	cfc152d6.bin	3657099436	3472	\N
135	store://2021/4/21/12/37/8adffc2a-4497-4835-a335-2c18f47b0b2c.bin	f47b0b2c.bin	1777648799	1277	\N
136	store://2021/4/21/12/37/40ad6320-3478-4730-8dce-e64868ac1235.bin	68ac1235.bin	2918125128	1330	\N
137	store://2021/4/21/12/37/b5f5063b-9966-4f74-b5b4-30b57a666c8b.bin	7a666c8b.bin	2718978176	3262	\N
138	store://2021/4/21/12/37/1d9506fb-29ca-4505-8c05-fe27eb75f0f8.bin	eb75f0f8.bin	2423175365	3631	\N
139	store://2021/4/21/12/37/4cc7f005-4be5-4ff0-bbb0-dc9197a802bb.bin	97a802bb.bin	968171870	1077	\N
140	store://2021/4/21/12/37/b49ed038-1112-45d8-b2f5-caf41d8349a7.bin	1d8349a7.bin	956837235	1106	\N
141	store://2021/4/21/12/37/f9884ebf-d66f-46e1-94a5-b09190d12dbc.bin	90d12dbc.bin	811791719	3101	\N
142	store://2021/4/21/12/37/63263c78-050e-471c-9654-45bb11634858.bin	11634858.bin	3266094670	3576	\N
143	store://2021/4/21/12/37/d0e31089-2c06-4c0f-b0e1-3d76f9bb3a1a.bin	f9bb3a1a.bin	3560819182	9453	\N
144	store://2021/4/21/12/37/4ab49bb7-05e0-4fbe-ba06-013530945fac.bin	30945fac.bin	3936130378	760	\N
244	store://2021/4/21/12/37/3b0d211a-01da-4970-8927-1520698d6798.bin	698d6798.bin	1896217246	652	\N
243	store://2021/4/21/12/37/1c4d7e37-6dc9-49d7-b820-40ca1b3c9479.bin	1b3c9479.bin	1691210579	32	1619008669714
145	store://2021/4/21/12/37/1e709b7c-cac2-4105-adc1-27dc88f637b7.bin	88f637b7.bin	3811860793	1490	\N
146	store://2021/4/21/12/37/966a36c0-0a75-4dd8-944a-6e4d2930ac01.bin	2930ac01.bin	1920400564	709	\N
147	store://2021/4/21/12/37/55bce407-5d75-4d15-b0dc-fc8cc8c83700.bin	c8c83700.bin	2273911693	55	\N
148	store://2021/4/21/12/37/8cef88e8-fee8-40b1-adda-c7210ac8f9bf.bin	0ac8f9bf.bin	1737643343	2347	\N
149	store://2021/4/21/12/37/52b737e7-6b85-4890-bb6f-698137ccaf9b.bin	37ccaf9b.bin	4119999643	6573	\N
150	store://2021/4/21/12/37/9257fead-4a01-4540-b4f6-cca529078bdc.bin	29078bdc.bin	2701499029	817	\N
151	store://2021/4/21/12/37/4d51680f-0818-47a8-9b5b-d1905ba74d44.bin	5ba74d44.bin	1304609722	54	\N
152	store://2021/4/21/12/37/6076aace-c95a-4028-a7c3-82fb54cd13e6.bin	54cd13e6.bin	3754270768	2823	\N
153	store://2021/4/21/12/37/2d637943-561a-435e-8ed4-b8dde10c19da.bin	e10c19da.bin	1272524122	8910	\N
154	store://2021/4/21/12/37/cbf66e2b-85e3-45e0-b744-579c8d97f1a5.bin	8d97f1a5.bin	865841451	0	\N
155	store://2021/4/21/12/37/245bd3e9-2ccf-4866-ae93-3c615650d262.bin	5650d262.bin	2557229302	0	\N
156	store://2021/4/21/12/37/053f9495-aeca-4d6f-8fea-c81c9f30b961.bin	9f30b961.bin	1851470898	381778	\N
158	store://2021/4/21/12/37/348227d7-a9aa-4d25-b31d-3cf70950a2b0.bin	0950a2b0.bin	1189886725	3526	\N
159	store://2021/4/21/12/37/1ad9eb86-9850-45de-9b61-fcd90f040f9d.bin	0f040f9d.bin	3648737953	87522	\N
160	store://2021/4/21/12/37/2bc75826-72b7-4552-93fd-65c9029644d2.bin	029644d2.bin	274792927	501641	\N
161	store://2021/4/21/12/37/62785ad7-569f-41fa-b26e-3ed0000008b0.bin	000008b0.bin	2202628935	17951	\N
162	store://2021/4/21/12/37/fc67e963-fbed-4561-bbf2-749e3404b7f6.bin	3404b7f6.bin	1740167253	342155	\N
163	store://2021/4/21/12/37/4a320d35-dfc1-4d02-b023-bdbe504b7126.bin	504b7126.bin	2922614593	19847	\N
164	store://2021/4/21/12/37/934d7676-b304-417c-a23b-ac78951e44be.bin	951e44be.bin	4212689725	145863	\N
165	store://2021/4/21/12/37/b585a3d9-1b06-4baf-94a7-364515cd79b0.bin	15cd79b0.bin	1247111364	33644	\N
166	store://2021/4/21/12/37/62d52b6c-5406-4399-b23a-52303dbe1603.bin	3dbe1603.bin	1882467963	266338	\N
167	store://2021/4/21/12/37/83c8b112-c02f-4736-9cb1-938e6c3a7b13.bin	6c3a7b13.bin	1877841894	540412	\N
168	store://2021/4/21/12/37/7ff065d1-1a4a-44fd-8762-b5fbb559a651.bin	b559a651.bin	1839115661	39387	\N
169	store://2021/4/21/12/37/e886ce15-0846-451e-8b7e-851b29266764.bin	29266764.bin	2774620527	105685	\N
170	store://2021/4/21/12/37/16cd3fbf-de80-4de5-b2f7-3328f38e7ca1.bin	f38e7ca1.bin	1148034223	22473	\N
171	store://2021/4/21/12/37/a75090f3-d9ea-4d7f-933c-3f58890a74af.bin	890a74af.bin	953014167	165798	\N
172	store://2021/4/21/12/37/00fdad83-30e9-417e-b1ed-3f2cff4fc7ae.bin	ff4fc7ae.bin	903818836	32865	\N
173	store://2021/4/21/12/37/5e6f82e5-6400-4028-b9f4-8b3f75dcc757.bin	75dcc757.bin	2485453102	64724	\N
174	store://2021/4/21/12/37/9eb1a5e6-8900-4982-ba5c-52c4033f8c0e.bin	033f8c0e.bin	2198184695	40385	\N
175	store://2021/4/21/12/37/4c48a1e1-e0c9-487e-b7ae-901b3250f20c.bin	3250f20c.bin	2314809547	37453	\N
176	store://2021/4/21/12/37/3f6a2542-d0ec-435c-b734-4a6c220f237f.bin	220f237f.bin	1074481986	13516	\N
177	store://2021/4/21/12/37/1ce59926-4c94-44a8-99eb-62fcced2935e.bin	ced2935e.bin	3121948776	146544	\N
178	store://2021/4/21/12/37/dfeb70c7-c968-4614-89c4-2edbef2e0b82.bin	ef2e0b82.bin	2174761933	6016	\N
179	store://2021/4/21/12/37/42f324ec-d6b7-48b3-8cbf-efae4c8b7419.bin	4c8b7419.bin	1084572804	679602	\N
180	store://2021/4/21/12/37/b550a9ee-7bec-472b-a9c0-b7a1f8434120.bin	f8434120.bin	2238830700	11610	\N
181	store://2021/4/21/12/37/0e09eec9-c5af-4c62-90d0-bf6091152532.bin	91152532.bin	3530634573	172648	\N
182	store://2021/4/21/12/37/c22e2bfd-42b2-4266-9606-c7ee6f5c24c6.bin	6f5c24c6.bin	658584738	9680	\N
183	store://2021/4/21/12/37/d446e9b8-a233-4244-925f-829f15df6389.bin	15df6389.bin	3011941075	34482	\N
184	store://2021/4/21/12/37/b914bda9-fafe-4f70-ab24-26a2a9670a19.bin	a9670a19.bin	48557984	4213	\N
185	store://2021/4/21/12/37/a34d8a6b-9e84-46de-85fa-7c3e8cd9d28d.bin	8cd9d28d.bin	1341027759	90797	\N
186	store://2021/4/21/12/37/133ec667-2bea-498e-b806-63336897375d.bin	6897375d.bin	2044345822	12701	\N
187	store://2021/4/21/12/37/85df89b9-0d8b-4fa9-a26d-99a6237f1db9.bin	237f1db9.bin	1565993926	188533	\N
188	store://2021/4/21/12/37/f7ff3cfd-095f-440e-9914-189d4ab478d0.bin	4ab478d0.bin	3470296756	9025	\N
189	store://2021/4/21/12/37/0d9511aa-1a1f-4826-b133-3d00247a4fed.bin	247a4fed.bin	558687901	375396	\N
190	store://2021/4/21/12/37/4edfb410-6559-4427-bf3c-01e2e420831f.bin	e420831f.bin	3855473026	12288	\N
191	store://2021/4/21/12/37/f54e3f4a-8a72-4a14-ab68-762590ba2c6b.bin	90ba2c6b.bin	3729655848	155620	\N
192	store://2021/4/21/12/37/06511df7-0ce3-4a7c-b6c7-0e77d8b58c4d.bin	d8b58c4d.bin	2227123323	350217	\N
193	store://2021/4/21/12/37/d2d1bd82-4147-4cd5-8afe-60c19747f01b.bin	9747f01b.bin	514950863	14569	\N
194	store://2021/4/21/12/37/d326400c-fa68-4f75-beea-9d87419438bc.bin	419438bc.bin	4115371790	196506	\N
195	store://2021/4/21/12/37/bfcdd1e4-927b-4579-916f-5913ab042291.bin	ab042291.bin	2113432749	3737049	\N
196	store://2021/4/21/12/37/9f0061b3-ff9a-4b5c-ada7-62905be198d2.bin	5be198d2.bin	2908472321	212734	\N
197	store://2021/4/21/12/37/54d35d89-b1d1-4ea9-a814-4ef77d3b045d.bin	7d3b045d.bin	1984748859	6217	\N
198	store://2021/4/21/12/37/385a76ca-3d94-4a04-896e-3a854340440d.bin	4340440d.bin	2447211772	777461	\N
199	store://2021/4/21/12/37/4fa16d82-20a2-4252-95e4-04ae765ac92a.bin	765ac92a.bin	1777152148	8085	\N
200	store://2021/4/21/12/37/1ac52a2b-e47d-4da6-9290-2b0c5ff885f0.bin	5ff885f0.bin	4188244595	26112	\N
201	store://2021/4/21/12/37/fe947ca7-cc81-4ee6-87a5-b383aa1f15ed.bin	aa1f15ed.bin	589600643	2388	\N
202	store://2021/4/21/12/37/30363756-6d6f-4015-920a-030d1c0f62bd.bin	1c0f62bd.bin	436239272	20964	\N
203	store://2021/4/21/12/37/19934cc5-32e4-4759-bcd7-daef4aed2d82.bin	4aed2d82.bin	2766450880	162	\N
204	store://2021/4/21/12/37/178fbca5-6def-47ac-bf93-7c5cde372bf4.bin	de372bf4.bin	719631114	73728	\N
205	store://2021/4/21/12/37/c1bcaeaa-0ac5-4984-8877-a02f76ef33da.bin	76ef33da.bin	1096258768	4778	\N
206	store://2021/4/21/12/37/3067c0d8-cf7a-46ae-9ed0-e55cbf415ee3.bin	bf415ee3.bin	1722503197	42016	\N
207	store://2021/4/21/12/37/8b37f109-0b22-4ca7-b5d2-ca300c9e991f.bin	0c9e991f.bin	1743091308	73728	\N
208	store://2021/4/21/12/37/f733e30f-56e6-4cb1-a76c-8a7e7c7f9668.bin	7c7f9668.bin	2091174749	4774	\N
209	store://2021/4/21/12/37/a24c05b2-7cfc-43d0-8b7b-8b1316ed4996.bin	16ed4996.bin	3850106487	42502	\N
210	store://2021/4/21/12/37/70ed88e6-1738-4bd3-97e8-9329719396dd.bin	719396dd.bin	503989576	74240	\N
211	store://2021/4/21/12/37/426de53d-5437-45c4-9fe6-cb5af705355b.bin	f705355b.bin	3312952819	4849	\N
212	store://2021/4/21/12/37/fc940bea-3c4c-42d1-ade4-7c475ecc50c8.bin	5ecc50c8.bin	2101918615	42890	\N
213	store://2021/4/21/12/37/99ab9cea-1f8f-4124-8daa-2858cabe369f.bin	cabe369f.bin	371297290	2117632	\N
214	store://2021/4/21/12/37/37775faf-4c07-4416-8b76-f19bcc4b77a9.bin	cc4b77a9.bin	2692016841	6540	\N
215	store://2021/4/21/12/37/4dcbef78-7fc3-4d3f-a43e-5683d6935392.bin	d6935392.bin	429368053	672905	\N
216	store://2021/4/21/12/37/2bbf9188-4fa3-4146-a38b-bd77856de905.bin	856de905.bin	2964697411	2898432	\N
217	store://2021/4/21/12/37/25f7056b-0db1-427e-a051-1a6501133bd5.bin	01133bd5.bin	2173198964	6414	\N
218	store://2021/4/21/12/37/09792763-a9db-4c4e-a83e-b3afde872c57.bin	de872c57.bin	981341151	976492	\N
219	store://2021/4/21/12/37/a07586cc-b156-4f06-a464-e23a26e1f82f.bin	26e1f82f.bin	2735091700	25	\N
220	store://2021/4/21/12/37/ec89b786-1e50-43c6-8443-05497c5902a7.bin	7c5902a7.bin	1625274396	38	\N
221	store://2021/4/21/12/37/a734ff10-0a7f-4c6a-97bf-3f1555d3e8bd.bin	55d3e8bd.bin	3951871133	1175	\N
223	store://2021/4/21/12/37/bccf8851-eb69-4a1d-ba53-9cc8d71f1405.bin	d71f1405.bin	3036042688	1771	\N
225	store://2021/4/21/12/37/8745d88a-d08b-4ea8-8c10-db41a839da26.bin	a839da26.bin	1578584698	3430	\N
227	store://2021/4/21/12/37/f135fe65-e9cc-4486-b828-e0a8aec59284.bin	aec59284.bin	2436917826	105	\N
228	store://2021/4/21/12/37/d482e2eb-4ca6-4756-b515-cc12d780da15.bin	d780da15.bin	3529478686	230	\N
229	store://2021/4/21/12/37/d6baa731-e042-4ce4-a2bb-66c05c95214c.bin	5c95214c.bin	2461921893	151	\N
230	store://2021/4/21/12/37/aab94fa7-35dc-421f-9663-8f4fc77a7a07.bin	c77a7a07.bin	3229597990	317	\N
231	store://2021/4/21/12/37/7221b212-c0a5-481a-b814-0c8cf6a53bfc.bin	f6a53bfc.bin	2071501609	110	\N
232	store://2021/4/21/12/37/20398756-e291-45f2-85c8-2d02c2c47a17.bin	c2c47a17.bin	2889054962	153	\N
233	store://2021/4/21/12/37/619b94d1-4c1a-4c86-9135-051fe8743503.bin	e8743503.bin	1670430512	797	\N
234	store://2021/4/21/12/37/ad0c3f13-3292-46ef-81fe-96f9275855de.bin	275855de.bin	2390757519	274	\N
235	store://2021/4/21/12/37/2ce48cc0-8f0f-4d30-85be-53f4f11a68df.bin	f11a68df.bin	3340365793	341	\N
236	store://2021/4/21/12/37/c329f0bc-2c67-46ee-b31a-7c5f7c59abec.bin	7c59abec.bin	4259871777	330	\N
237	store://2021/4/21/12/37/01614308-9e3f-4a3e-aa08-d5c81ebd081d.bin	1ebd081d.bin	29710459	328	\N
238	store://2021/4/21/12/37/f074f8e6-56ea-4b88-b738-4f88425be6ca.bin	425be6ca.bin	3818201404	330	\N
239	store://2021/4/21/12/37/32864185-4834-4cb7-bc97-b456e957a833.bin	e957a833.bin	2230107948	282	\N
240	store://2021/4/21/12/37/dadb17c2-a9e5-4e45-84ba-f9925069b6b4.bin	5069b6b4.bin	1921678857	262	\N
241	store://2021/4/21/12/37/83ff2851-64c9-4b96-82a1-a015ca357e54.bin	ca357e54.bin	1058221872	272	\N
242	store://2021/4/21/12/37/01daca7e-27e4-4059-9e84-38a9572c415c.bin	572c415c.bin	2301787069	346	\N
252	store://2021/4/21/12/40/2ebe9261-832f-4167-8e29-d87e49a12b18.bin	49a12b18.bin	3992338017	284	\N
253	store://2021/4/21/12/40/7df5d3f0-f999-406c-8f11-8310b90a8c76.bin	b90a8c76.bin	961268009	391	\N
254	store://2021/4/21/12/40/1c22a82f-5177-402b-b777-a042fb73e2f4.bin	fb73e2f4.bin	4002699908	291	\N
255	store://2021/4/21/12/40/1784bfa4-a942-4238-91e3-73a32c59efd1.bin	2c59efd1.bin	1992990377	291	\N
256	store://2021/4/21/12/40/b0527e3c-5b27-4ea8-bf90-d830c14ad315.bin	c14ad315.bin	690424666	296	\N
257	store://2021/4/21/12/40/0b8dd6c4-c2b3-4f46-8838-c1032db4b66b.bin	2db4b66b.bin	2548685816	363	\N
258	store://2021/4/21/12/40/356172f4-d60b-47b3-b111-1f8c26487487.bin	26487487.bin	4245417698	388	\N
245	store://2021/4/21/12/40/1c94103c-6d7f-44c9-8e22-98410511d343.bin	0511d343.bin	2870248035	284	1619008800774
247	store://2021/4/21/12/40/7f2a61b9-8eea-49a1-a139-9ed0ad02101f.bin	ad02101f.bin	4116659225	291	1619008800774
246	store://2021/4/21/12/40/8f338cee-0054-414b-93cc-0e227e7293a0.bin	7e7293a0.bin	4205073093	391	1619008800774
248	store://2021/4/21/12/40/47406ec4-f23c-43ca-87a0-2113654279f1.bin	654279f1.bin	2373886413	291	1619008800774
251	store://2021/4/21/12/40/213cfc63-efa7-44bb-9324-20cbcb38c687.bin	cb38c687.bin	659889655	388	1619008800774
249	store://2021/4/21/12/40/4a4ce128-a1fc-49f9-b715-19ded49b11b9.bin	d49b11b9.bin	3401221148	296	1619008800774
250	store://2021/4/21/12/40/8e54e6a7-6f5a-4196-a5cd-72576c0b57a3.bin	6c0b57a3.bin	1919174289	363	1619008800774
259	store://2021/4/21/12/40/264e0366-5e55-4bec-bd20-6411c78d8ef5.bin	c78d8ef5.bin	134562244	18	\N
261	store://2021/4/21/12/40/ff1f7985-0145-4209-a962-9bddb32e749d.bin	b32e749d.bin	256166942	8163	\N
263	store://2021/4/21/12/40/54fc1d14-ddc3-4ec9-b660-be4d3c68e728.bin	3c68e728.bin	3157121918	184	\N
264	store://2021/4/21/12/41/48a38c95-747d-4c56-b028-7f58672a533b.bin	672a533b.bin	2972619124	19	\N
266	store://2021/4/21/12/41/17ad2332-1c4f-42b3-afd5-236e4fdc880b.bin	4fdc880b.bin	726279864	7944	\N
268	store://2021/4/21/12/41/1116d34a-219a-409f-a247-b187679fca90.bin	679fca90.bin	3462973652	192	\N
272	store://2021/4/21/12/41/dfc39bfa-76f9-4d65-8519-85195feb8984.bin	5feb8984.bin	3086917377	4164	\N
276	store://2021/4/21/12/41/26e4c419-f962-49f9-97f8-c8064d0973fc.bin	4d0973fc.bin	3220165260	836	\N
274	store://2021/4/21/12/41/e08d7fc8-56d8-46ba-ae02-a4a6a837cfac.bin	a837cfac.bin	2482956429	5595	\N
278	store://2021/4/21/12/41/f0b2b26b-0712-4cd3-a13e-ece18bd7bd48.bin	8bd7bd48.bin	4074190643	304	\N
279	store://2021/4/21/12/41/3f72b540-0658-43cc-88d9-84a5c8075ba4.bin	c8075ba4.bin	2007287177	324	\N
280	store://2021/4/21/12/41/0e4f20f5-b92a-4a08-9223-0e588e3b9f7b.bin	8e3b9f7b.bin	548880315	370	\N
281	store://2021/4/21/12/41/9843b9a7-6ea1-4619-8415-62e84b70d120.bin	4b70d120.bin	3746556595	314	\N
282	store://2021/4/21/12/41/9b2efb86-f916-4417-a414-b522927b0af2.bin	927b0af2.bin	1293592330	316	\N
283	store://2021/4/21/12/41/c8033095-0fbe-4117-96d5-0a44f5bc0f50.bin	f5bc0f50.bin	2064027139	481	\N
284	store://2021/4/21/12/41/58e64721-e6e7-476f-a4af-5e20dc3c4597.bin	dc3c4597.bin	3227636852	171	\N
285	store://2021/4/21/12/41/c516b0b4-24e4-4422-a893-e46c6ed2fa0d.bin	6ed2fa0d.bin	1921753967	14	\N
287	store://2021/4/21/12/41/e46048ba-66a8-4db3-abd3-41ebae0d303d.bin	ae0d303d.bin	518856087	14	\N
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
2	0	en_US_
3	0	en_
4	0	en_GB_
\.


--
-- Data for Name: alf_lock; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_lock (id, version, shared_resource_id, excl_resource_id, lock_token, start_time, expiry_time) FROM stdin;
1	1	1	1	not-locked	0	0
2	1	2	2	not-locked	0	0
3	1	4	3	not-locked	0	0
4	1	5	3	not-locked	0	0
5	1	6	3	not-locked	0	0
6	1	7	3	not-locked	0	0
7	1	3	3	not-locked	0	0
8	1	8	8	not-locked	0	0
9	1	4	9	not-locked	0	0
10	1	5	9	not-locked	0	0
11	1	6	9	not-locked	0	0
12	1	10	9	not-locked	0	0
13	1	9	9	not-locked	0	0
16	1	4	13	not-locked	0	0
17	1	5	13	not-locked	0	0
18	1	6	13	not-locked	0	0
19	1	14	13	not-locked	0	0
20	1	15	13	not-locked	0	0
21	1	17	13	not-locked	0	0
22	1	13	13	not-locked	0	0
23	1	4	19	not-locked	0	0
24	1	5	19	not-locked	0	0
25	1	6	19	not-locked	0	0
26	1	14	19	not-locked	0	0
27	1	20	19	not-locked	0	0
28	1	21	19	not-locked	0	0
29	1	19	19	not-locked	0	0
30	5	22	22	not-locked	0	0
14	33	12	12	not-locked	0	0
15	17	11	11	not-locked	0	0
\.


--
-- Data for Name: alf_lock_resource; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_lock_resource (id, version, qname_ns_id, qname_localname) FROM stdin;
1	0	6	verifylicense
2	0	1	clusterservice
3	0	1	org.alfresco.repo.usage.userusagetrackingcomponent
4	0	1	org
5	0	1	org.alfresco
6	0	1	org.alfresco.repo
7	0	1	org.alfresco.repo.usage
8	0	1	chaininguserregistrysynchronizer
9	0	1	org.alfresco.repo.thumbnail.thumbnailregistry
10	0	1	org.alfresco.repo.thumbnail
11	0	1	feedgenerator
12	0	1	activitypostlookup
13	0	1	org.alfresco.repo.activities.feed.cleanup.feedcleaner
14	0	1	org.alfresco.repo.activities
15	0	1	org.alfresco.repo.activities.feed
17	0	1	org.alfresco.repo.activities.feed.cleanup
19	0	1	org.alfresco.repo.activities.post.cleanup.postcleaner
20	0	1	org.alfresco.repo.activities.post
21	0	1	org.alfresco.repo.activities.post.cleanup
22	0	1	dictionarymodeltype
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
21	0	http://www.alfresco.org/model/consulting/bulkobject/1.0
\.


--
-- Data for Name: alf_node; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_node (id, version, store_id, uuid, transaction_id, type_qname_id, locale_id, acl_id, audit_creator, audit_created, audit_modifier, audit_modified, audit_accessed) FROM stdin;
1	1	1	80c1d7de-e671-4682-8844-0093b1798e24	1	1	1	1	\N	\N	\N	\N	\N
2	1	1	5fb7078e-cc81-4ef4-b86d-d1d8aa4b453c	1	3	2	2	\N	\N	\N	\N	\N
3	1	1	55db4d0b-9ca3-412a-b345-2db1aaf09aaa	1	3	2	2	\N	\N	\N	\N	\N
4	1	1	62a97fb9-74bf-4b82-acab-b36b04a0dcf7	1	5	2	2	\N	\N	\N	\N	\N
5	1	2	6d4251e2-e1ed-421b-b7d8-93398ffd2617	2	1	1	3	\N	\N	\N	\N	\N
6	1	2	921cf5b5-d12a-4efe-b272-4536df29c14f	2	12	2	4	\N	\N	\N	\N	\N
866	1	6	fe024910-bac8-4aef-b22b-1770af590dc6	19	24	2	11	admin	2021-04-21T12:40:00.192Z	admin	2021-04-21T12:40:00.192Z	\N
8	1	2	3891ca8d-aef9-43ff-a330-68e43c2cce61	2	3	2	4	\N	\N	\N	\N	\N
9	1	3	a38aa652-60cc-4419-a5c6-8084dd1329d1	3	1	1	5	\N	\N	\N	\N	\N
10	1	4	925f9770-8fd5-45fa-b644-a20df4cbecef	4	1	1	6	\N	\N	\N	\N	\N
12	1	6	9bede7ad-b219-4bcf-a2e7-22a953807930	6	1	1	8	\N	\N	\N	\N	\N
18	1	6	1f520b98-e4d1-44f8-a8fc-982b0a0e0c63	6	24	2	13	System	2021-04-21T12:37:40.633Z	System	2021-04-21T12:37:40.633Z	\N
875	1	6	0c0e5da5-2803-4532-bd34-f2d4b78ad15e	19	24	2	11	admin	2021-04-21T12:40:00.449Z	admin	2021-04-21T12:40:00.449Z	\N
20	2	6	cc019e0d-8f04-4079-b2bf-d96e32fbd501	6	24	2	16	System	2021-04-21T12:37:40.661Z	System	2021-04-21T12:37:40.661Z	\N
873	3	6	89ce9168-5323-4b49-95d1-8dffcfff9799	19	24	2	11	admin	2021-04-21T12:40:00.436Z	admin	2021-04-21T12:40:00.436Z	\N
22	1	6	4dc14f6c-d0ee-464d-96a9-a1f7607e6977	6	24	2	13	System	2021-04-21T12:37:40.697Z	System	2021-04-21T12:37:40.697Z	\N
874	3	6	c6c875a7-d60f-4380-a0e9-365210e36b35	19	24	2	11	admin	2021-04-21T12:40:00.443Z	admin	2021-04-21T12:40:00.443Z	\N
24	2	6	867417f9-4f5d-4131-929d-e48fbfe08b11	6	24	2	20	System	2021-04-21T12:37:40.725Z	System	2021-04-21T12:37:40.725Z	\N
867	2	6	f585ec2f-9ca8-4cf8-b23a-aca581e1e66f	20	51	2	11	admin	2021-04-21T12:40:00.248Z	admin	2021-04-21T12:40:00.639Z	\N
25	2	6	ddf90839-2914-4b07-81c5-b66ced26d99f	6	24	2	22	System	2021-04-21T12:37:40.743Z	System	2021-04-21T12:37:40.743Z	\N
868	2	6	d6ce061c-7276-4eb0-8301-be80d16cacb5	20	51	2	11	admin	2021-04-21T12:40:00.348Z	admin	2021-04-21T12:40:00.658Z	\N
27	2	6	a3c4f7b2-bdb2-43df-ab2a-28f09e57a374	6	24	2	24	System	2021-04-21T12:37:40.774Z	System	2021-04-21T12:37:40.774Z	\N
28	1	6	3d85fa42-2a5d-4760-96c3-3949814a9696	6	24	2	11	System	2021-04-21T12:37:40.789Z	System	2021-04-21T12:37:40.789Z	\N
29	1	6	dc3fde83-4e6d-48a2-83a1-f863f0613998	6	24	2	11	System	2021-04-21T12:37:40.797Z	System	2021-04-21T12:37:40.797Z	\N
869	2	6	4683c1b1-6328-442b-9e8f-f32103e78059	20	51	2	11	admin	2021-04-21T12:40:00.368Z	admin	2021-04-21T12:40:00.674Z	\N
30	2	6	8b2301ab-8349-46c1-b245-c91e75d5580b	6	3	2	26	\N	\N	\N	\N	\N
31	1	6	2ff6bb9d-cf2c-4fb9-b548-9c9fb7bcecef	6	3	2	27	\N	\N	\N	\N	\N
870	2	6	2fc89fd9-358d-4374-ac50-8303d76b553d	20	51	2	11	admin	2021-04-21T12:40:00.387Z	admin	2021-04-21T12:40:00.692Z	\N
871	2	6	4cc7d3b9-ba16-4ddb-b723-7e8f2ee4a586	20	51	2	11	admin	2021-04-21T12:40:00.405Z	admin	2021-04-21T12:40:00.712Z	\N
872	2	6	be32fbac-106f-456a-800b-99c108ca5b9d	20	51	2	11	admin	2021-04-21T12:40:00.421Z	admin	2021-04-21T12:40:00.730Z	\N
33	2	6	06717873-38e5-40c5-ba47-ddd7a8d81bce	6	35	2	30	\N	\N	\N	\N	\N
34	1	6	06291943-2ff3-41af-ad72-ce2eea9b7f9e	6	3	2	27	\N	\N	\N	\N	\N
35	1	6	a8cd33f2-41a4-425a-b4d3-5ec94e2c8cb5	6	46	2	9	System	2021-04-21T12:37:40.890Z	System	2021-04-21T12:37:40.890Z	\N
36	1	6	744ac88c-6818-4bdb-b0ab-2b159a6ca1f8	6	47	2	9	System	2021-04-21T12:37:40.896Z	System	2021-04-21T12:37:40.896Z	\N
37	1	6	124f1ed6-e994-4d93-9a66-d41515c4fb00	6	47	2	9	System	2021-04-21T12:37:40.900Z	System	2021-04-21T12:37:40.900Z	\N
876	2	6	22768f7a-7add-474a-80a2-ab56bcc7c48d	20	51	2	11	admin	2021-04-21T12:40:00.462Z	admin	2021-04-21T12:40:00.763Z	\N
11	2	5	6f636169-2394-4e91-abe5-958781e80757	33	1	1	7	\N	\N	\N	\N	\N
32	3	6	6743d698-1e80-424e-808f-d9fd9d604afc	11	35	2	28	\N	\N	\N	\N	\N
7	4	2	88588b6c-8d18-4587-9c52-b9b0980326ee	14	12	2	4	\N	\N	\N	\N	\N
895	1	4	bf68ecbc-0368-423a-8fbc-39de38a42bf4	35	164	2	60	admin	2021-04-21T12:41:53.559Z	admin	2021-04-21T12:41:53.559Z	\N
896	1	4	4feb9453-4a8b-45f8-a2e8-9c7bc7ab13a9	35	243	2	60	admin	2021-04-21T12:41:53.566Z	admin	2021-04-21T12:41:53.566Z	\N
894	4	6	c8378689-c06d-4b71-9d73-63ce4192344e	37	243	2	13	admin	2021-04-21T12:41:53.481Z	admin	2021-04-21T12:41:54.032Z	\N
901	1	4	a2e6257a-98aa-4e7d-b551-42c4118bf3a3	40	164	2	60	admin	2021-04-21T12:41:54.934Z	admin	2021-04-21T12:41:54.934Z	\N
902	1	4	fab7d888-60c4-4c23-bd20-92c266c03e11	40	243	2	60	admin	2021-04-21T12:41:54.940Z	admin	2021-04-21T12:41:54.940Z	\N
26	2	6	93336940-8369-4b4e-bad4-5a98bdb6e58a	44	24	2	11	System	2021-04-21T12:37:40.766Z	admin	2021-04-21T12:41:55.450Z	\N
38	1	6	d003f407-9b94-48a6-a449-7275a1d081e4	6	47	2	9	System	2021-04-21T12:37:40.906Z	System	2021-04-21T12:37:40.906Z	\N
39	1	6	87791b74-5c8f-4ac1-b8a4-e23c50276b2a	6	47	2	9	System	2021-04-21T12:37:40.910Z	System	2021-04-21T12:37:40.910Z	\N
40	1	6	b2c6657e-b206-40c4-a73d-35e4159e1f4f	6	47	2	9	System	2021-04-21T12:37:40.914Z	System	2021-04-21T12:37:40.914Z	\N
41	1	6	90c6c5cc-ce26-4bca-a08a-467b3e5ecfac	6	47	2	9	System	2021-04-21T12:37:40.918Z	System	2021-04-21T12:37:40.918Z	\N
42	1	6	f6a38c2f-be9b-412a-9c6b-b2a99f28cb24	6	47	2	9	System	2021-04-21T12:37:40.922Z	System	2021-04-21T12:37:40.922Z	\N
43	1	6	4917eb98-e10f-4811-b5a8-d2fe0e83bae6	6	47	2	9	System	2021-04-21T12:37:40.927Z	System	2021-04-21T12:37:40.927Z	\N
44	1	6	a0202884-e664-4432-b875-926cd8f00f04	6	47	2	9	System	2021-04-21T12:37:40.931Z	System	2021-04-21T12:37:40.931Z	\N
45	1	6	c73e26ff-4c19-41de-8c3c-a66dfd52ef68	6	47	2	9	System	2021-04-21T12:37:40.935Z	System	2021-04-21T12:37:40.935Z	\N
46	1	6	d2437db7-e45f-448d-8682-1a290ffbc082	6	47	2	9	System	2021-04-21T12:37:40.939Z	System	2021-04-21T12:37:40.939Z	\N
47	1	6	1ab83be3-2c36-4a88-87db-a81b55fc7a98	6	47	2	9	System	2021-04-21T12:37:40.943Z	System	2021-04-21T12:37:40.943Z	\N
48	1	6	f8755de7-c57a-4d3f-bdb9-efeeb9cdff40	6	47	2	9	System	2021-04-21T12:37:40.948Z	System	2021-04-21T12:37:40.948Z	\N
49	1	6	be0f279a-3d08-4c53-921a-b2c5c9effaf8	6	47	2	9	System	2021-04-21T12:37:40.952Z	System	2021-04-21T12:37:40.952Z	\N
50	1	6	dffa073b-c006-430e-af29-60e0ea8ba905	6	47	2	9	System	2021-04-21T12:37:40.955Z	System	2021-04-21T12:37:40.955Z	\N
51	1	6	15130765-3c2e-4b0e-828c-71675a3347c8	6	47	2	9	System	2021-04-21T12:37:40.959Z	System	2021-04-21T12:37:40.959Z	\N
52	1	6	5614616b-ca80-412c-9694-925d582760ec	6	47	2	9	System	2021-04-21T12:37:40.963Z	System	2021-04-21T12:37:40.963Z	\N
53	1	6	b5985606-555c-4457-805d-26d7e42b800c	6	47	2	9	System	2021-04-21T12:37:40.968Z	System	2021-04-21T12:37:40.968Z	\N
54	1	6	09d09601-83ea-43a9-8286-a675985ee7d5	6	47	2	9	System	2021-04-21T12:37:40.973Z	System	2021-04-21T12:37:40.973Z	\N
55	1	6	e9bf63c9-fc9f-4832-8c82-cb9e6f65dcf5	6	47	2	9	System	2021-04-21T12:37:40.979Z	System	2021-04-21T12:37:40.979Z	\N
56	1	6	cabe3821-c372-4b2e-8dc8-d228ffc88ef8	6	47	2	9	System	2021-04-21T12:37:40.983Z	System	2021-04-21T12:37:40.983Z	\N
57	1	6	db9a0202-1bcc-45f9-bf0a-f48e38a429da	6	47	2	9	System	2021-04-21T12:37:40.987Z	System	2021-04-21T12:37:40.987Z	\N
58	1	6	7d5e035a-84b4-4d54-8a20-bd54abbd21c7	6	47	2	9	System	2021-04-21T12:37:40.991Z	System	2021-04-21T12:37:40.991Z	\N
59	1	6	922b09a1-c6f6-431c-9641-eef06e1505bc	6	47	2	9	System	2021-04-21T12:37:40.994Z	System	2021-04-21T12:37:40.994Z	\N
60	1	6	8c15df3b-6020-4870-b218-b1bf79725712	6	47	2	9	System	2021-04-21T12:37:40.998Z	System	2021-04-21T12:37:40.998Z	\N
61	1	6	40f73e3d-f397-4ee1-ad78-b7b634d42618	6	47	2	9	System	2021-04-21T12:37:41.002Z	System	2021-04-21T12:37:41.002Z	\N
62	1	6	b7f679c9-01f7-4480-a240-41a8fcb433eb	6	47	2	9	System	2021-04-21T12:37:41.006Z	System	2021-04-21T12:37:41.006Z	\N
63	1	6	d4e9d5ea-6efc-42e6-b7dd-db92a80584d5	6	47	2	9	System	2021-04-21T12:37:41.010Z	System	2021-04-21T12:37:41.010Z	\N
64	1	6	fa1a08af-9980-4251-a2ef-0d9309b3454c	6	47	2	9	System	2021-04-21T12:37:41.013Z	System	2021-04-21T12:37:41.013Z	\N
65	1	6	a04c08b8-3a49-4c66-9bac-1aa8882a8a57	6	47	2	9	System	2021-04-21T12:37:41.017Z	System	2021-04-21T12:37:41.017Z	\N
66	1	6	d1d2ebc9-53f2-4b13-8719-b882ef536c1a	6	47	2	9	System	2021-04-21T12:37:41.020Z	System	2021-04-21T12:37:41.020Z	\N
67	1	6	34e88cb5-ac6e-4cbb-93c2-e09775134640	6	47	2	9	System	2021-04-21T12:37:41.023Z	System	2021-04-21T12:37:41.023Z	\N
68	1	6	2b3487ed-0321-4246-bb03-65227cb2e405	6	47	2	9	System	2021-04-21T12:37:41.027Z	System	2021-04-21T12:37:41.027Z	\N
69	1	6	b4fb60e8-846f-4aed-9a88-c5aec9c04346	6	47	2	9	System	2021-04-21T12:37:41.031Z	System	2021-04-21T12:37:41.031Z	\N
70	1	6	acb6e8c5-46ca-458f-b071-f2f3393ce112	6	47	2	9	System	2021-04-21T12:37:41.034Z	System	2021-04-21T12:37:41.034Z	\N
71	1	6	b6017f27-200b-47cd-8a92-da2517573932	6	47	2	9	System	2021-04-21T12:37:41.037Z	System	2021-04-21T12:37:41.037Z	\N
72	1	6	b8a6ff09-f6c3-457a-a486-c5739f86767b	6	47	2	9	System	2021-04-21T12:37:41.040Z	System	2021-04-21T12:37:41.040Z	\N
73	1	6	0e141510-67d3-4541-b909-7c7469fcf8b5	6	47	2	9	System	2021-04-21T12:37:41.043Z	System	2021-04-21T12:37:41.043Z	\N
74	1	6	8960d4a8-385b-43b7-8929-58dda3d09d51	6	47	2	9	System	2021-04-21T12:37:41.046Z	System	2021-04-21T12:37:41.046Z	\N
75	1	6	f8a6a01c-e637-4bc8-b9f8-9c6c5214d722	6	47	2	9	System	2021-04-21T12:37:41.049Z	System	2021-04-21T12:37:41.049Z	\N
76	1	6	f4d5d5d0-ec04-4710-b24a-d3adc336fd13	6	47	2	9	System	2021-04-21T12:37:41.052Z	System	2021-04-21T12:37:41.052Z	\N
77	1	6	53a101e8-63d8-4417-ac49-61013ff5d562	6	47	2	9	System	2021-04-21T12:37:41.055Z	System	2021-04-21T12:37:41.055Z	\N
78	1	6	c2a84ccb-9644-49f2-9a8a-fef98af2ffbb	6	47	2	9	System	2021-04-21T12:37:41.058Z	System	2021-04-21T12:37:41.058Z	\N
727	1	2	64084680-7400-4766-8749-f51d5b3a1fe2	13	3	2	4	\N	\N	\N	\N	\N
79	1	6	55805924-5cf6-4370-997a-3e4b494565da	6	47	2	9	System	2021-04-21T12:37:41.061Z	System	2021-04-21T12:37:41.061Z	\N
80	1	6	3c086384-8c3c-4e2c-8a70-2bcb43b84824	6	47	2	9	System	2021-04-21T12:37:41.064Z	System	2021-04-21T12:37:41.064Z	\N
81	1	6	db48f1b1-95e3-459b-af36-ce10020377b6	6	47	2	9	System	2021-04-21T12:37:41.067Z	System	2021-04-21T12:37:41.067Z	\N
82	1	6	bf80d8e8-eb99-4a85-b0d3-726e265765ab	6	47	2	9	System	2021-04-21T12:37:41.070Z	System	2021-04-21T12:37:41.070Z	\N
83	1	6	40a5effe-5239-46fb-8a09-f7fcbc25915e	6	47	2	9	System	2021-04-21T12:37:41.073Z	System	2021-04-21T12:37:41.073Z	\N
84	1	6	a4f9d6e0-f9c1-4a39-b146-603e0f27ddf1	6	47	2	9	System	2021-04-21T12:37:41.076Z	System	2021-04-21T12:37:41.076Z	\N
85	1	6	79c3692b-c0b4-40fa-a214-17ace5e9fd04	6	47	2	9	System	2021-04-21T12:37:41.079Z	System	2021-04-21T12:37:41.079Z	\N
86	1	6	12ef9f8c-5653-4372-bd14-3b36be4693b9	6	47	2	9	System	2021-04-21T12:37:41.081Z	System	2021-04-21T12:37:41.081Z	\N
87	1	6	e7846a96-5476-406e-8704-d3f593d1d596	6	47	2	9	System	2021-04-21T12:37:41.084Z	System	2021-04-21T12:37:41.084Z	\N
88	1	6	fea7ec5a-d573-4e32-a16f-a1d9fc816496	6	47	2	9	System	2021-04-21T12:37:41.087Z	System	2021-04-21T12:37:41.087Z	\N
89	1	6	4980e1e7-c5dc-44ec-8af0-207cf3117fb6	6	47	2	9	System	2021-04-21T12:37:41.090Z	System	2021-04-21T12:37:41.090Z	\N
90	1	6	597622ce-86ad-4334-980d-efd71c8db1e1	6	47	2	9	System	2021-04-21T12:37:41.093Z	System	2021-04-21T12:37:41.093Z	\N
91	1	6	d59fa657-b2e1-4f2f-b3ef-3b2216a025a5	6	47	2	9	System	2021-04-21T12:37:41.095Z	System	2021-04-21T12:37:41.095Z	\N
92	1	6	59731bf4-edef-43ad-a861-79edd92bbe22	6	47	2	9	System	2021-04-21T12:37:41.099Z	System	2021-04-21T12:37:41.099Z	\N
93	1	6	870fd4c1-ecb7-41ec-a5a3-0a3403fa0ff9	6	47	2	9	System	2021-04-21T12:37:41.102Z	System	2021-04-21T12:37:41.102Z	\N
94	1	6	4a292d18-5469-42e0-9d7c-b33c458a2ad2	6	47	2	9	System	2021-04-21T12:37:41.104Z	System	2021-04-21T12:37:41.104Z	\N
95	1	6	2af4955f-c61a-48b6-b62b-a2b15b6c04b0	6	47	2	9	System	2021-04-21T12:37:41.107Z	System	2021-04-21T12:37:41.107Z	\N
96	1	6	c0172f83-4316-4f00-ad86-d8587b8d3d7f	6	47	2	9	System	2021-04-21T12:37:41.109Z	System	2021-04-21T12:37:41.109Z	\N
97	1	6	bb3519ae-2820-41b4-aa14-0d85d4aae8af	6	47	2	9	System	2021-04-21T12:37:41.112Z	System	2021-04-21T12:37:41.112Z	\N
98	1	6	5764bd4d-7372-48b9-9fb6-9f357b346a3d	6	47	2	9	System	2021-04-21T12:37:41.115Z	System	2021-04-21T12:37:41.115Z	\N
99	1	6	52d1487e-29b8-4482-b3d7-89a20e7fb0f8	6	47	2	9	System	2021-04-21T12:37:41.117Z	System	2021-04-21T12:37:41.117Z	\N
100	1	6	fea0cf0f-c9c0-4938-b31f-d0dd1c5e8422	6	47	2	9	System	2021-04-21T12:37:41.120Z	System	2021-04-21T12:37:41.120Z	\N
101	1	6	713c0c6e-d2d2-4d53-90b6-0e44543cfc95	6	47	2	9	System	2021-04-21T12:37:41.122Z	System	2021-04-21T12:37:41.122Z	\N
102	1	6	4a9a3e0d-ec40-4783-9a53-75b5abb0a723	6	47	2	9	System	2021-04-21T12:37:41.125Z	System	2021-04-21T12:37:41.125Z	\N
103	1	6	f9b38bc1-8c19-472f-9118-688525fcf39a	6	47	2	9	System	2021-04-21T12:37:41.128Z	System	2021-04-21T12:37:41.128Z	\N
104	1	6	ce5d8c6f-c0c9-4b8c-846b-cb3ce75d74bc	6	47	2	9	System	2021-04-21T12:37:41.130Z	System	2021-04-21T12:37:41.130Z	\N
105	1	6	1abc2b80-97eb-4010-a0a7-4821cf8a5e05	6	47	2	9	System	2021-04-21T12:37:41.133Z	System	2021-04-21T12:37:41.133Z	\N
106	1	6	5d9a1190-bf31-4e07-84f4-c06c26a8eb9a	6	47	2	9	System	2021-04-21T12:37:41.136Z	System	2021-04-21T12:37:41.136Z	\N
107	1	6	417d8fd3-e4fa-4ec1-9993-b70e7abdaca2	6	47	2	9	System	2021-04-21T12:37:41.138Z	System	2021-04-21T12:37:41.138Z	\N
108	1	6	a38d4e11-58b0-4cd1-9c16-b53c8d32dc33	6	47	2	9	System	2021-04-21T12:37:41.141Z	System	2021-04-21T12:37:41.141Z	\N
109	1	6	53a7abab-9f78-4fc0-a7e1-a94a9bd02a51	6	47	2	9	System	2021-04-21T12:37:41.144Z	System	2021-04-21T12:37:41.144Z	\N
110	1	6	54e37f8c-679d-4392-ae7b-0e2b5a2a4a7a	6	47	2	9	System	2021-04-21T12:37:41.146Z	System	2021-04-21T12:37:41.146Z	\N
111	1	6	20768883-5843-43c8-be92-0ebafa1b1d5f	6	47	2	9	System	2021-04-21T12:37:41.149Z	System	2021-04-21T12:37:41.149Z	\N
112	1	6	eaf3a4bd-1011-472c-bc24-a4a672e9b284	6	47	2	9	System	2021-04-21T12:37:41.151Z	System	2021-04-21T12:37:41.151Z	\N
113	1	6	70ea8671-a037-4544-9a7c-2b1bbffa75ba	6	47	2	9	System	2021-04-21T12:37:41.154Z	System	2021-04-21T12:37:41.154Z	\N
114	1	6	d0938d41-2614-4e30-829e-6defb4cb281d	6	47	2	9	System	2021-04-21T12:37:41.156Z	System	2021-04-21T12:37:41.156Z	\N
115	1	6	fb798bed-872b-4cc4-9e94-cb0550ab3769	6	47	2	9	System	2021-04-21T12:37:41.159Z	System	2021-04-21T12:37:41.159Z	\N
116	1	6	6b90a7ff-141a-4d67-8d88-05f8b2a659ea	6	47	2	9	System	2021-04-21T12:37:41.162Z	System	2021-04-21T12:37:41.162Z	\N
117	1	6	71b32d53-1c9d-48b0-905c-5dc2f9297c8a	6	47	2	9	System	2021-04-21T12:37:41.165Z	System	2021-04-21T12:37:41.165Z	\N
118	1	6	a08fa1b4-d915-4f86-9277-8150616fea4d	6	47	2	9	System	2021-04-21T12:37:41.167Z	System	2021-04-21T12:37:41.167Z	\N
119	1	6	6badcfea-1317-4171-b21c-63f671521ee0	6	47	2	9	System	2021-04-21T12:37:41.170Z	System	2021-04-21T12:37:41.170Z	\N
728	1	2	5bcabafa-35c9-4570-8721-24ee29261fd0	13	3	2	4	\N	\N	\N	\N	\N
120	1	6	ca1241c5-eda5-4973-a22c-4f6771677d86	6	47	2	9	System	2021-04-21T12:37:41.172Z	System	2021-04-21T12:37:41.172Z	\N
121	1	6	314a41dc-9bf4-42d1-9e0b-6f11ed978377	6	47	2	9	System	2021-04-21T12:37:41.175Z	System	2021-04-21T12:37:41.175Z	\N
122	1	6	9ae50082-0aad-4dc6-8dc7-f7c4b5b03264	6	47	2	9	System	2021-04-21T12:37:41.177Z	System	2021-04-21T12:37:41.177Z	\N
123	1	6	0112691b-ebac-46c7-aac8-de75218d5e53	6	47	2	9	System	2021-04-21T12:37:41.180Z	System	2021-04-21T12:37:41.180Z	\N
124	1	6	07b52cc7-8c99-4793-a2f9-3533ae6fce3c	6	47	2	9	System	2021-04-21T12:37:41.182Z	System	2021-04-21T12:37:41.182Z	\N
125	1	6	da25fdce-3ac2-4c1a-9134-ebce894cda69	6	47	2	9	System	2021-04-21T12:37:41.185Z	System	2021-04-21T12:37:41.185Z	\N
126	1	6	29d759cb-7ad7-4a99-a2c7-4eae016ac058	6	47	2	9	System	2021-04-21T12:37:41.188Z	System	2021-04-21T12:37:41.188Z	\N
127	1	6	9aea872d-cc0a-448d-92bd-0bf4bd046842	6	47	2	9	System	2021-04-21T12:37:41.190Z	System	2021-04-21T12:37:41.190Z	\N
128	1	6	27bdb9e0-28ff-4d55-aa25-b420d515de11	6	47	2	9	System	2021-04-21T12:37:41.193Z	System	2021-04-21T12:37:41.193Z	\N
129	1	6	8b04c295-28d3-434c-aa00-e9a676f42fb9	6	47	2	9	System	2021-04-21T12:37:41.195Z	System	2021-04-21T12:37:41.195Z	\N
130	1	6	4a9e0e98-6db1-4b7f-8504-b2f8e97a4263	6	47	2	9	System	2021-04-21T12:37:41.198Z	System	2021-04-21T12:37:41.198Z	\N
131	1	6	a487a8ff-fd52-44bf-8cf3-00ece97714ba	6	47	2	9	System	2021-04-21T12:37:41.200Z	System	2021-04-21T12:37:41.200Z	\N
132	1	6	2571d917-e55a-4bce-aa22-29e227eee96d	6	47	2	9	System	2021-04-21T12:37:41.203Z	System	2021-04-21T12:37:41.203Z	\N
133	1	6	598631dd-3bf5-4a48-8fb5-85bb6cf90ac1	6	47	2	9	System	2021-04-21T12:37:41.205Z	System	2021-04-21T12:37:41.205Z	\N
134	1	6	3bf34473-f705-40c5-a0a9-5232c8b36746	6	47	2	9	System	2021-04-21T12:37:41.208Z	System	2021-04-21T12:37:41.208Z	\N
135	1	6	1fd320af-7032-499c-bc78-54961aa4a427	6	47	2	9	System	2021-04-21T12:37:41.210Z	System	2021-04-21T12:37:41.210Z	\N
136	1	6	d1a349f8-4bbc-4d65-8922-1ac34d00d1b7	6	47	2	9	System	2021-04-21T12:37:41.213Z	System	2021-04-21T12:37:41.213Z	\N
137	1	6	6c4bda2e-3f18-48d6-97ef-4fd8170aade8	6	47	2	9	System	2021-04-21T12:37:41.215Z	System	2021-04-21T12:37:41.215Z	\N
138	1	6	4a505c88-44b3-4093-9eeb-7eb977a6479c	6	47	2	9	System	2021-04-21T12:37:41.218Z	System	2021-04-21T12:37:41.218Z	\N
139	1	6	4a4899f2-fc12-4db8-a256-71654df44e9c	6	47	2	9	System	2021-04-21T12:37:41.220Z	System	2021-04-21T12:37:41.220Z	\N
140	1	6	a2529437-0e0d-4cff-b1e0-a92f5c1481e9	6	47	2	9	System	2021-04-21T12:37:41.223Z	System	2021-04-21T12:37:41.223Z	\N
141	1	6	453acc3d-72ac-485a-a2a6-48735f6550e8	6	47	2	9	System	2021-04-21T12:37:41.225Z	System	2021-04-21T12:37:41.225Z	\N
142	1	6	e2c634ad-2def-40ca-b017-9040467ef296	6	47	2	9	System	2021-04-21T12:37:41.228Z	System	2021-04-21T12:37:41.228Z	\N
143	1	6	28e2e4ea-8caf-4a56-9ba7-f2931c54e12b	6	47	2	9	System	2021-04-21T12:37:41.230Z	System	2021-04-21T12:37:41.230Z	\N
144	1	6	57535cb1-b86d-49de-bad2-caa389e8e19f	6	47	2	9	System	2021-04-21T12:37:41.233Z	System	2021-04-21T12:37:41.233Z	\N
145	1	6	fb69e0dc-c3da-444a-a4fc-2c41ac6dbb7e	6	47	2	9	System	2021-04-21T12:37:41.235Z	System	2021-04-21T12:37:41.235Z	\N
146	1	6	fcb6df69-f8f6-4aa5-bc54-f2923fe56c98	6	47	2	9	System	2021-04-21T12:37:41.238Z	System	2021-04-21T12:37:41.238Z	\N
147	1	6	4eb8c7c1-2428-4ab8-bcc1-3e581d71a29f	6	47	2	9	System	2021-04-21T12:37:41.240Z	System	2021-04-21T12:37:41.240Z	\N
148	1	6	824e29c9-46c7-43e9-9133-80d2d9afceff	6	47	2	9	System	2021-04-21T12:37:41.243Z	System	2021-04-21T12:37:41.243Z	\N
149	1	6	70c194b2-838d-4bb6-a24a-15389b9886c0	6	47	2	9	System	2021-04-21T12:37:41.245Z	System	2021-04-21T12:37:41.245Z	\N
150	1	6	5cd1fcbb-3a63-4657-90f5-43167064d35d	6	47	2	9	System	2021-04-21T12:37:41.248Z	System	2021-04-21T12:37:41.248Z	\N
151	1	6	6b9f8562-d2be-459a-a8c7-e15365491375	6	47	2	9	System	2021-04-21T12:37:41.250Z	System	2021-04-21T12:37:41.250Z	\N
152	1	6	a1f35d1e-d2e5-487e-8d2c-e8ccce086898	6	47	2	9	System	2021-04-21T12:37:41.252Z	System	2021-04-21T12:37:41.252Z	\N
153	1	6	e55b6adf-1a64-4567-b122-c22a2194fc1b	6	47	2	9	System	2021-04-21T12:37:41.255Z	System	2021-04-21T12:37:41.255Z	\N
154	1	6	afe4bf83-467b-4050-8039-c40d913e2fa5	6	47	2	9	System	2021-04-21T12:37:41.257Z	System	2021-04-21T12:37:41.257Z	\N
155	1	6	d8d2da15-c35e-4110-9f16-e82e0d04b0b0	6	47	2	9	System	2021-04-21T12:37:41.260Z	System	2021-04-21T12:37:41.260Z	\N
156	1	6	4a1a17e8-e7f3-465b-a93d-eab2db02e9df	6	47	2	9	System	2021-04-21T12:37:41.262Z	System	2021-04-21T12:37:41.262Z	\N
157	1	6	e07eb5d2-6554-4f35-8925-ac7303b1d6ba	6	47	2	9	System	2021-04-21T12:37:41.264Z	System	2021-04-21T12:37:41.264Z	\N
158	1	6	4eb6000a-a8c6-4aa9-86ce-ccd5803a2ee5	6	47	2	9	System	2021-04-21T12:37:41.266Z	System	2021-04-21T12:37:41.266Z	\N
159	1	6	90696b0b-05c4-457a-adc7-3c4ee45ef33b	6	47	2	9	System	2021-04-21T12:37:41.269Z	System	2021-04-21T12:37:41.269Z	\N
160	1	6	aa0a4089-2ee1-4e5e-935c-47a23cee52ff	6	47	2	9	System	2021-04-21T12:37:41.271Z	System	2021-04-21T12:37:41.271Z	\N
729	1	2	2ce572a1-abdd-4cc2-8ffe-8e710c74afd0	13	3	2	4	\N	\N	\N	\N	\N
161	1	6	40479945-89d8-466a-89b2-eeaf75e8f066	6	47	2	9	System	2021-04-21T12:37:41.273Z	System	2021-04-21T12:37:41.273Z	\N
162	1	6	d0ba0429-55d3-494a-8564-6b114d48fb5f	6	47	2	9	System	2021-04-21T12:37:41.275Z	System	2021-04-21T12:37:41.275Z	\N
163	1	6	f77b0396-c668-420d-9f1b-499e8ee85d34	6	47	2	9	System	2021-04-21T12:37:41.278Z	System	2021-04-21T12:37:41.278Z	\N
164	1	6	b353a1ee-2706-436e-8266-302ffe0eb446	6	47	2	9	System	2021-04-21T12:37:41.280Z	System	2021-04-21T12:37:41.280Z	\N
165	1	6	0c1b299c-68fe-4a25-826e-9df22f030a28	6	47	2	9	System	2021-04-21T12:37:41.282Z	System	2021-04-21T12:37:41.282Z	\N
166	1	6	5e200ae3-27ca-4144-a3a2-cb505a3b255b	6	47	2	9	System	2021-04-21T12:37:41.284Z	System	2021-04-21T12:37:41.284Z	\N
167	1	6	2c976c82-d029-460b-a10d-1428238c24c2	6	47	2	9	System	2021-04-21T12:37:41.286Z	System	2021-04-21T12:37:41.286Z	\N
168	1	6	6cbbcaa8-80dc-4124-a73a-8eac985d9977	6	47	2	9	System	2021-04-21T12:37:41.289Z	System	2021-04-21T12:37:41.289Z	\N
169	1	6	da40733c-2173-4b74-95d8-e749bc2176a4	6	47	2	9	System	2021-04-21T12:37:41.291Z	System	2021-04-21T12:37:41.291Z	\N
170	1	6	8c987ee6-664a-4e2a-b4b4-0fea62670841	6	47	2	9	System	2021-04-21T12:37:41.293Z	System	2021-04-21T12:37:41.293Z	\N
171	1	6	8c02e03d-6e33-477a-9156-6d144a5890f1	6	47	2	9	System	2021-04-21T12:37:41.295Z	System	2021-04-21T12:37:41.295Z	\N
172	1	6	9ab479a3-aef9-47d6-9b07-3f8d447acd89	6	47	2	9	System	2021-04-21T12:37:41.298Z	System	2021-04-21T12:37:41.298Z	\N
173	1	6	18f1fbd4-f433-461c-8638-d0adbea95bcc	6	47	2	9	System	2021-04-21T12:37:41.300Z	System	2021-04-21T12:37:41.300Z	\N
174	1	6	35a179e4-6a95-4c81-bebe-8972b70d4830	6	47	2	9	System	2021-04-21T12:37:41.302Z	System	2021-04-21T12:37:41.302Z	\N
175	1	6	69bfdd56-0407-4032-b7e3-8a6b922409e0	6	47	2	9	System	2021-04-21T12:37:41.304Z	System	2021-04-21T12:37:41.304Z	\N
176	1	6	fd3a2398-5a2e-48e9-ad74-d7cef89c5220	6	47	2	9	System	2021-04-21T12:37:41.307Z	System	2021-04-21T12:37:41.307Z	\N
177	1	6	2bf8a5cf-967d-4a8e-af35-799ee2bf0ab8	6	47	2	9	System	2021-04-21T12:37:41.310Z	System	2021-04-21T12:37:41.310Z	\N
178	1	6	3f8eeeb6-f1ff-4439-a8c4-0fe478965779	6	47	2	9	System	2021-04-21T12:37:41.314Z	System	2021-04-21T12:37:41.314Z	\N
179	1	6	89996740-5c12-4998-a546-3d459e7f52ce	6	47	2	9	System	2021-04-21T12:37:41.318Z	System	2021-04-21T12:37:41.318Z	\N
180	1	6	0ecd7465-4821-4028-945d-6f6043fc6b24	6	47	2	9	System	2021-04-21T12:37:41.322Z	System	2021-04-21T12:37:41.322Z	\N
181	1	6	6bcdad3e-4ac6-4fb4-934c-2f6ffb951929	6	47	2	9	System	2021-04-21T12:37:41.325Z	System	2021-04-21T12:37:41.325Z	\N
182	1	6	548ccebe-55e0-40cf-bf1a-0c3a79575c42	6	47	2	9	System	2021-04-21T12:37:41.329Z	System	2021-04-21T12:37:41.329Z	\N
183	1	6	b6e2b2b2-5899-41c9-b7df-1a7158f374ef	6	47	2	9	System	2021-04-21T12:37:41.332Z	System	2021-04-21T12:37:41.332Z	\N
184	1	6	539bde09-c464-4d77-ba4a-d33dcb4e3d19	6	47	2	9	System	2021-04-21T12:37:41.334Z	System	2021-04-21T12:37:41.334Z	\N
185	1	6	f3971210-4a81-4ac9-8512-22a92f3963f3	6	47	2	9	System	2021-04-21T12:37:41.337Z	System	2021-04-21T12:37:41.337Z	\N
186	1	6	8377be01-b389-4265-96d8-586ec741b5dc	6	47	2	9	System	2021-04-21T12:37:41.339Z	System	2021-04-21T12:37:41.339Z	\N
187	1	6	bb96304f-8654-4c1f-84fa-7480aa4affc8	6	47	2	9	System	2021-04-21T12:37:41.341Z	System	2021-04-21T12:37:41.341Z	\N
188	1	6	9fc86db3-31d7-492d-8daa-2137f7afc7f7	6	47	2	9	System	2021-04-21T12:37:41.343Z	System	2021-04-21T12:37:41.343Z	\N
189	1	6	7a35b484-da0f-45a5-bdb3-58fdb964b867	6	47	2	9	System	2021-04-21T12:37:41.346Z	System	2021-04-21T12:37:41.346Z	\N
190	1	6	a0e45a5f-60ca-4490-9431-c407625bd02f	6	47	2	9	System	2021-04-21T12:37:41.348Z	System	2021-04-21T12:37:41.348Z	\N
191	1	6	e30f52d3-6907-4ded-921f-c570f1d89d1f	6	47	2	9	System	2021-04-21T12:37:41.350Z	System	2021-04-21T12:37:41.350Z	\N
192	1	6	0bc6c2df-e351-4110-b0ca-c04bc74478d5	6	47	2	9	System	2021-04-21T12:37:41.352Z	System	2021-04-21T12:37:41.352Z	\N
193	1	6	7b86ad9b-077f-489e-9fb2-7faac5f13b4d	6	47	2	9	System	2021-04-21T12:37:41.355Z	System	2021-04-21T12:37:41.355Z	\N
194	1	6	976984b2-7b22-4daf-aa54-48df5cc6c109	6	47	2	9	System	2021-04-21T12:37:41.357Z	System	2021-04-21T12:37:41.357Z	\N
195	1	6	b1df838e-bc3c-4b55-8894-c19eee67bd6f	6	47	2	9	System	2021-04-21T12:37:41.360Z	System	2021-04-21T12:37:41.360Z	\N
196	1	6	d0cb6cb5-48a8-4676-b9ca-0c4cecf422cb	6	47	2	9	System	2021-04-21T12:37:41.362Z	System	2021-04-21T12:37:41.362Z	\N
197	1	6	f114ac6a-4a62-456b-a12c-b0510f6a6d44	6	47	2	9	System	2021-04-21T12:37:41.365Z	System	2021-04-21T12:37:41.365Z	\N
198	1	6	981b2a1a-43f7-46da-a08d-536167d2dff2	6	47	2	9	System	2021-04-21T12:37:41.367Z	System	2021-04-21T12:37:41.367Z	\N
199	1	6	d462fce9-28fc-4c98-88cf-5ad374abf1fc	6	47	2	9	System	2021-04-21T12:37:41.369Z	System	2021-04-21T12:37:41.369Z	\N
200	1	6	5d7090cf-f207-48ac-86c8-843f2e075f82	6	47	2	9	System	2021-04-21T12:37:41.372Z	System	2021-04-21T12:37:41.372Z	\N
201	1	6	f766cca5-750b-42d1-8866-0af5e2a03e90	6	47	2	9	System	2021-04-21T12:37:41.374Z	System	2021-04-21T12:37:41.374Z	\N
730	1	2	c98f8b57-657b-4456-b5b8-01743c2b6aff	13	3	2	4	\N	\N	\N	\N	\N
202	1	6	bbd63159-94da-4fe6-a45c-1ac8d15636f1	6	47	2	9	System	2021-04-21T12:37:41.377Z	System	2021-04-21T12:37:41.377Z	\N
203	1	6	ce6c5797-182b-478b-88e0-e8e65f4157ad	6	47	2	9	System	2021-04-21T12:37:41.379Z	System	2021-04-21T12:37:41.379Z	\N
204	1	6	3c03ab9e-0078-4948-9eb5-2bc407502060	6	47	2	9	System	2021-04-21T12:37:41.381Z	System	2021-04-21T12:37:41.381Z	\N
205	1	6	08191f13-c4a3-44ec-bc90-4e0193989477	6	47	2	9	System	2021-04-21T12:37:41.384Z	System	2021-04-21T12:37:41.384Z	\N
206	1	6	d1a8a117-c4d2-4ace-92c6-b1d2451b5717	6	47	2	9	System	2021-04-21T12:37:41.386Z	System	2021-04-21T12:37:41.386Z	\N
207	1	6	f09cc1dc-aef7-4481-855e-c33feb5e7c58	6	47	2	9	System	2021-04-21T12:37:41.388Z	System	2021-04-21T12:37:41.388Z	\N
208	1	6	87fa344b-91db-4bd9-813c-c05b49858392	6	47	2	9	System	2021-04-21T12:37:41.391Z	System	2021-04-21T12:37:41.391Z	\N
209	1	6	db38f239-8623-4a60-9f22-71a5ebcefc1d	6	47	2	9	System	2021-04-21T12:37:41.393Z	System	2021-04-21T12:37:41.393Z	\N
210	1	6	6f047de0-7f4f-4ab0-ada5-8a24306783d1	6	47	2	9	System	2021-04-21T12:37:41.395Z	System	2021-04-21T12:37:41.395Z	\N
211	1	6	4b2164d3-574a-4e32-ad83-4dae9ac4e758	6	47	2	9	System	2021-04-21T12:37:41.397Z	System	2021-04-21T12:37:41.397Z	\N
212	1	6	778324df-b74e-492d-bdda-68cdbf2c0cbe	6	47	2	9	System	2021-04-21T12:37:41.399Z	System	2021-04-21T12:37:41.399Z	\N
213	1	6	9ac7eff8-f7eb-4aa2-aae4-011ae0b017e4	6	47	2	9	System	2021-04-21T12:37:41.402Z	System	2021-04-21T12:37:41.402Z	\N
214	1	6	dbbdf383-dfb9-4c35-a34a-808a26ab55c4	6	47	2	9	System	2021-04-21T12:37:41.404Z	System	2021-04-21T12:37:41.404Z	\N
215	1	6	ea557e13-0b88-4283-a569-35d954a7433d	6	47	2	9	System	2021-04-21T12:37:41.407Z	System	2021-04-21T12:37:41.407Z	\N
216	1	6	e0050e48-11a9-4de0-960e-15f005410df6	6	47	2	9	System	2021-04-21T12:37:41.409Z	System	2021-04-21T12:37:41.409Z	\N
217	1	6	9d1319d3-28ba-4428-9801-b9bf2600d793	6	47	2	9	System	2021-04-21T12:37:41.411Z	System	2021-04-21T12:37:41.411Z	\N
218	1	6	723c5833-96bd-416c-aefc-8f13287845c5	6	47	2	9	System	2021-04-21T12:37:41.413Z	System	2021-04-21T12:37:41.413Z	\N
219	1	6	3a8aee79-974c-4a8b-ad7e-e5cdc82711ef	6	47	2	9	System	2021-04-21T12:37:41.416Z	System	2021-04-21T12:37:41.416Z	\N
220	1	6	81f96941-fd86-4207-a7f7-ec3c7f094db7	6	47	2	9	System	2021-04-21T12:37:41.418Z	System	2021-04-21T12:37:41.418Z	\N
221	1	6	b569e484-48f1-4d46-9121-bee4ae912831	6	47	2	9	System	2021-04-21T12:37:41.420Z	System	2021-04-21T12:37:41.420Z	\N
222	1	6	dafe2b88-a054-4b27-9512-42ebe5667045	6	47	2	9	System	2021-04-21T12:37:41.422Z	System	2021-04-21T12:37:41.422Z	\N
223	1	6	08218117-a492-488d-9bcf-2d28dd2acb3a	6	47	2	9	System	2021-04-21T12:37:41.425Z	System	2021-04-21T12:37:41.425Z	\N
224	1	6	1c214008-e1bc-4c3b-b039-ef58b0eb7f0a	6	47	2	9	System	2021-04-21T12:37:41.427Z	System	2021-04-21T12:37:41.427Z	\N
225	1	6	53322508-2dcd-4294-ac2b-1a365af06b43	6	47	2	9	System	2021-04-21T12:37:41.429Z	System	2021-04-21T12:37:41.429Z	\N
226	1	6	33e9ff5e-968a-4af3-a54f-01b93a92105d	6	47	2	9	System	2021-04-21T12:37:41.432Z	System	2021-04-21T12:37:41.432Z	\N
227	1	6	97d5b312-c948-4e6b-b060-c6830c975f17	6	47	2	9	System	2021-04-21T12:37:41.434Z	System	2021-04-21T12:37:41.434Z	\N
228	1	6	46ec7527-de02-4960-b622-4a302aeaf9cb	6	47	2	9	System	2021-04-21T12:37:41.436Z	System	2021-04-21T12:37:41.436Z	\N
229	1	6	fba550ee-ecde-4240-ba15-b577e0fa508a	6	47	2	9	System	2021-04-21T12:37:41.438Z	System	2021-04-21T12:37:41.438Z	\N
230	1	6	4680f1b1-11c1-4e0c-a110-deee4d11c1bf	6	47	2	9	System	2021-04-21T12:37:41.441Z	System	2021-04-21T12:37:41.441Z	\N
231	1	6	35a53d5d-07a0-4cb8-b9b1-e557d01c5865	6	47	2	9	System	2021-04-21T12:37:41.443Z	System	2021-04-21T12:37:41.443Z	\N
232	1	6	fdd17621-5291-4cb3-b14c-034d7e497227	6	47	2	9	System	2021-04-21T12:37:41.445Z	System	2021-04-21T12:37:41.445Z	\N
233	1	6	ef98ff2e-db32-4fc5-b18f-47152435c409	6	47	2	9	System	2021-04-21T12:37:41.447Z	System	2021-04-21T12:37:41.447Z	\N
234	1	6	069b00a4-7b12-4186-b650-51521c2a07b7	6	47	2	9	System	2021-04-21T12:37:41.450Z	System	2021-04-21T12:37:41.450Z	\N
235	1	6	79ea6f9a-8b55-4a60-9f79-ba93b4eaf988	6	47	2	9	System	2021-04-21T12:37:41.453Z	System	2021-04-21T12:37:41.453Z	\N
236	1	6	198cd493-d324-4306-9ae0-9876954be5f0	6	47	2	9	System	2021-04-21T12:37:41.455Z	System	2021-04-21T12:37:41.455Z	\N
237	1	6	fbec1a25-668e-44dc-966a-b2dd43aa92fd	6	47	2	9	System	2021-04-21T12:37:41.457Z	System	2021-04-21T12:37:41.457Z	\N
238	1	6	1477c895-a1ea-4739-a706-17e1b7c27785	6	47	2	9	System	2021-04-21T12:37:41.459Z	System	2021-04-21T12:37:41.459Z	\N
239	1	6	b9627af2-e9e2-40f8-8f12-a2391fb5c5ce	6	47	2	9	System	2021-04-21T12:37:41.461Z	System	2021-04-21T12:37:41.461Z	\N
240	1	6	8d85562a-14c8-44f8-b434-e8adaf3aab1e	6	47	2	9	System	2021-04-21T12:37:41.464Z	System	2021-04-21T12:37:41.464Z	\N
241	1	6	5e8855bb-8d78-4955-b47f-a3cd515e401d	6	47	2	9	System	2021-04-21T12:37:41.466Z	System	2021-04-21T12:37:41.466Z	\N
242	1	6	2c1aec74-3ebd-4c75-acee-2ec34eb481ef	6	47	2	9	System	2021-04-21T12:37:41.468Z	System	2021-04-21T12:37:41.468Z	\N
731	1	2	6feebf55-cf63-4604-99cc-11cba9a65be5	13	3	2	4	\N	\N	\N	\N	\N
243	1	6	3f92801f-f600-40f9-b096-0052e5f39f2c	6	47	2	9	System	2021-04-21T12:37:41.470Z	System	2021-04-21T12:37:41.470Z	\N
244	1	6	0922db6a-c447-4a7c-9057-1ba010e60325	6	47	2	9	System	2021-04-21T12:37:41.473Z	System	2021-04-21T12:37:41.473Z	\N
245	1	6	da66af52-03a0-428e-9e58-b01b38aeec53	6	47	2	9	System	2021-04-21T12:37:41.475Z	System	2021-04-21T12:37:41.475Z	\N
246	1	6	8add5bc7-4c03-4756-9e1e-c71ab0c9f0a5	6	47	2	9	System	2021-04-21T12:37:41.477Z	System	2021-04-21T12:37:41.477Z	\N
247	1	6	68246fef-0f1d-4011-8f5b-dd76631bbc3b	6	47	2	9	System	2021-04-21T12:37:41.480Z	System	2021-04-21T12:37:41.480Z	\N
248	1	6	7033912c-50fb-4a9d-9bb2-5c17cbc92df1	6	47	2	9	System	2021-04-21T12:37:41.482Z	System	2021-04-21T12:37:41.482Z	\N
249	1	6	b91adf90-58b7-43eb-af46-5fdeca76914a	6	47	2	9	System	2021-04-21T12:37:41.485Z	System	2021-04-21T12:37:41.485Z	\N
250	1	6	c349d6f1-bcaa-4dc3-8358-6528858c53db	6	47	2	9	System	2021-04-21T12:37:41.487Z	System	2021-04-21T12:37:41.487Z	\N
251	1	6	ab3f2304-343e-4cf7-a3a7-f717355e6eae	6	47	2	9	System	2021-04-21T12:37:41.489Z	System	2021-04-21T12:37:41.489Z	\N
252	1	6	349ff6a0-7ae3-4249-babe-84c7da313f65	6	47	2	9	System	2021-04-21T12:37:41.491Z	System	2021-04-21T12:37:41.491Z	\N
253	1	6	c37d8d5c-9cf2-4c81-8c63-8327fe46074c	6	47	2	9	System	2021-04-21T12:37:41.494Z	System	2021-04-21T12:37:41.494Z	\N
254	1	6	d1be1e5f-7c7d-4399-8e74-7d77f38b44c8	6	47	2	9	System	2021-04-21T12:37:41.496Z	System	2021-04-21T12:37:41.496Z	\N
255	1	6	d4f95997-bd0c-4d43-b939-a2fb8724300e	6	47	2	9	System	2021-04-21T12:37:41.498Z	System	2021-04-21T12:37:41.498Z	\N
256	1	6	d1d270e6-c353-4ee9-9121-9943c7b44b86	6	47	2	9	System	2021-04-21T12:37:41.500Z	System	2021-04-21T12:37:41.500Z	\N
257	1	6	b5cf7f24-1e09-44e1-bda6-d3ddacf1cec1	6	47	2	9	System	2021-04-21T12:37:41.502Z	System	2021-04-21T12:37:41.502Z	\N
258	1	6	b66da845-aabd-4f42-b058-b63cad3fc0ea	6	47	2	9	System	2021-04-21T12:37:41.504Z	System	2021-04-21T12:37:41.504Z	\N
259	1	6	c2eebd1c-a22e-4938-9027-9accd62c3dec	6	47	2	9	System	2021-04-21T12:37:41.508Z	System	2021-04-21T12:37:41.508Z	\N
260	1	6	b1389e91-7035-4f67-ba63-1fbceecf0db8	6	47	2	9	System	2021-04-21T12:37:41.510Z	System	2021-04-21T12:37:41.510Z	\N
261	1	6	03dacaad-5fb7-43f7-b7ad-47827fb1aa41	6	47	2	9	System	2021-04-21T12:37:41.513Z	System	2021-04-21T12:37:41.513Z	\N
262	1	6	865d94eb-ff14-4df1-ae6d-08a3e95c9741	6	47	2	9	System	2021-04-21T12:37:41.515Z	System	2021-04-21T12:37:41.515Z	\N
263	1	6	76264511-bb1c-4786-961d-48e89bbf8f79	6	47	2	9	System	2021-04-21T12:37:41.518Z	System	2021-04-21T12:37:41.518Z	\N
264	1	6	56339725-9610-423a-845e-24217971f530	6	47	2	9	System	2021-04-21T12:37:41.520Z	System	2021-04-21T12:37:41.520Z	\N
265	1	6	b27e5d8f-f981-455d-bf06-5e6bc128a63d	6	47	2	9	System	2021-04-21T12:37:41.523Z	System	2021-04-21T12:37:41.523Z	\N
266	1	6	9d4338b6-cf93-4c9e-9fd1-691d45ce89f5	6	47	2	9	System	2021-04-21T12:37:41.525Z	System	2021-04-21T12:37:41.525Z	\N
267	1	6	cfa28aa8-48ae-4390-a53d-3a408a30d90c	6	47	2	9	System	2021-04-21T12:37:41.527Z	System	2021-04-21T12:37:41.527Z	\N
268	1	6	3ec192af-df9d-459b-accc-070d2662f4be	6	47	2	9	System	2021-04-21T12:37:41.529Z	System	2021-04-21T12:37:41.529Z	\N
269	1	6	c13fc8de-2e0e-4778-8fd6-5d17ea15aaa3	6	47	2	9	System	2021-04-21T12:37:41.531Z	System	2021-04-21T12:37:41.531Z	\N
270	1	6	68306d9c-42bf-42cc-bc9f-cbabe330292a	6	47	2	9	System	2021-04-21T12:37:41.533Z	System	2021-04-21T12:37:41.533Z	\N
271	1	6	3d7cbf6f-bce1-4ce9-96f4-cb0d59601c2b	6	47	2	9	System	2021-04-21T12:37:41.535Z	System	2021-04-21T12:37:41.535Z	\N
272	1	6	fe311b5a-97d3-491c-9172-35b868060f46	6	47	2	9	System	2021-04-21T12:37:41.537Z	System	2021-04-21T12:37:41.537Z	\N
273	1	6	09085a60-388c-4d4a-8522-bd7490e2ebe0	6	47	2	9	System	2021-04-21T12:37:41.539Z	System	2021-04-21T12:37:41.539Z	\N
274	1	6	fd851a31-5b5b-43c3-9298-eb7d91f6fc64	6	47	2	9	System	2021-04-21T12:37:41.542Z	System	2021-04-21T12:37:41.542Z	\N
275	1	6	949e001f-6b39-4f9f-bcff-047acd10bf05	6	47	2	9	System	2021-04-21T12:37:41.544Z	System	2021-04-21T12:37:41.544Z	\N
276	1	6	b0516544-5c16-4281-87bf-460e3f4e3802	6	47	2	9	System	2021-04-21T12:37:41.546Z	System	2021-04-21T12:37:41.546Z	\N
277	1	6	3cfda347-801b-4e10-8975-71658aea1b5d	6	47	2	9	System	2021-04-21T12:37:41.549Z	System	2021-04-21T12:37:41.549Z	\N
278	1	6	dd88625f-583e-467f-915d-30351640f13c	6	47	2	9	System	2021-04-21T12:37:41.551Z	System	2021-04-21T12:37:41.551Z	\N
279	1	6	a2ee45b5-876a-4833-880e-8140da0c86ab	6	47	2	9	System	2021-04-21T12:37:41.553Z	System	2021-04-21T12:37:41.553Z	\N
280	1	6	264f8984-96a6-4c1b-b19a-c27fe7627059	6	47	2	9	System	2021-04-21T12:37:41.555Z	System	2021-04-21T12:37:41.555Z	\N
281	1	6	2607ce08-6ad5-4663-bde1-e12675cdd546	6	47	2	9	System	2021-04-21T12:37:41.557Z	System	2021-04-21T12:37:41.557Z	\N
282	1	6	e43e1632-898c-490c-8683-710428de2828	6	47	2	9	System	2021-04-21T12:37:41.560Z	System	2021-04-21T12:37:41.560Z	\N
283	1	6	2f8da867-b2e4-417b-8ce2-940702d46300	6	47	2	9	System	2021-04-21T12:37:41.562Z	System	2021-04-21T12:37:41.562Z	\N
732	1	2	3a2ab3db-81da-4966-9fae-4dca6680e109	13	3	2	4	\N	\N	\N	\N	\N
284	1	6	72834cd5-cac9-4fc5-97bb-31574e8f2aed	6	47	2	9	System	2021-04-21T12:37:41.564Z	System	2021-04-21T12:37:41.564Z	\N
285	1	6	0d033c11-f94f-4e3e-8a3e-82d93815cc22	6	47	2	9	System	2021-04-21T12:37:41.566Z	System	2021-04-21T12:37:41.566Z	\N
286	1	6	f764038a-0947-4b37-8171-722ad038adcc	6	47	2	9	System	2021-04-21T12:37:41.568Z	System	2021-04-21T12:37:41.568Z	\N
287	1	6	6f042e46-ae9d-4e52-ac3f-1fc465e5f9e6	6	47	2	9	System	2021-04-21T12:37:41.571Z	System	2021-04-21T12:37:41.571Z	\N
288	1	6	109b7309-31f4-448b-9c38-a50776e25baa	6	47	2	9	System	2021-04-21T12:37:41.573Z	System	2021-04-21T12:37:41.573Z	\N
289	1	6	20204b03-1118-410f-8e27-35b62ddaabb7	6	47	2	9	System	2021-04-21T12:37:41.575Z	System	2021-04-21T12:37:41.575Z	\N
290	1	6	f849a89d-59ff-406b-a093-6361554562c0	6	47	2	9	System	2021-04-21T12:37:41.577Z	System	2021-04-21T12:37:41.577Z	\N
291	1	6	1566ac1b-90e7-4844-a9c9-3cd649ea0e90	6	47	2	9	System	2021-04-21T12:37:41.580Z	System	2021-04-21T12:37:41.580Z	\N
292	1	6	8ad856da-d401-4808-8584-bfc833d53693	6	47	2	9	System	2021-04-21T12:37:41.582Z	System	2021-04-21T12:37:41.582Z	\N
293	1	6	c607bfb9-8c03-4a8c-8366-cab1f0c7c365	6	47	2	9	System	2021-04-21T12:37:41.584Z	System	2021-04-21T12:37:41.584Z	\N
294	1	6	09781f7d-468f-4eb0-954b-eaa20c485650	6	47	2	9	System	2021-04-21T12:37:41.587Z	System	2021-04-21T12:37:41.587Z	\N
295	1	6	5d3c15e7-1ee0-4fc9-9db1-242a7321c2d0	6	47	2	9	System	2021-04-21T12:37:41.589Z	System	2021-04-21T12:37:41.589Z	\N
296	1	6	5b7e99a4-d331-41b7-aca9-ff55f3113d09	6	47	2	9	System	2021-04-21T12:37:41.591Z	System	2021-04-21T12:37:41.591Z	\N
297	1	6	3bc91e24-7161-4d48-873f-28064281714c	6	47	2	9	System	2021-04-21T12:37:41.593Z	System	2021-04-21T12:37:41.593Z	\N
298	1	6	1565fba7-5e6e-4d81-8dac-ee786313f93d	6	47	2	9	System	2021-04-21T12:37:41.595Z	System	2021-04-21T12:37:41.595Z	\N
299	1	6	6a7dcd4a-c139-45a7-ac62-28a643a5f95e	6	47	2	9	System	2021-04-21T12:37:41.597Z	System	2021-04-21T12:37:41.597Z	\N
300	1	6	525e35cd-cc71-4251-a180-b72895cedfe0	6	47	2	9	System	2021-04-21T12:37:41.599Z	System	2021-04-21T12:37:41.599Z	\N
301	1	6	561d9b08-7b69-4ba6-9493-2c06542fd6ac	6	47	2	9	System	2021-04-21T12:37:41.601Z	System	2021-04-21T12:37:41.601Z	\N
302	1	6	008ba9ba-6499-4a7f-ae21-7a9734254cad	6	47	2	9	System	2021-04-21T12:37:41.603Z	System	2021-04-21T12:37:41.603Z	\N
303	1	6	46d621c5-1cc7-4fa8-80bf-0032aef4ddab	6	47	2	9	System	2021-04-21T12:37:41.606Z	System	2021-04-21T12:37:41.606Z	\N
304	1	6	fd29a223-25a6-4d9b-a30e-3b27cf9b7f9c	6	47	2	9	System	2021-04-21T12:37:41.608Z	System	2021-04-21T12:37:41.608Z	\N
305	1	6	ff9f2b79-ca42-473a-93d2-911e7250b6c8	6	47	2	9	System	2021-04-21T12:37:41.610Z	System	2021-04-21T12:37:41.610Z	\N
306	1	6	3532801f-d21a-46ad-b58a-d0d0b05a3f1c	6	47	2	9	System	2021-04-21T12:37:41.612Z	System	2021-04-21T12:37:41.612Z	\N
307	1	6	ec6404da-0a92-47ad-a8c0-bd682b941054	6	47	2	9	System	2021-04-21T12:37:41.614Z	System	2021-04-21T12:37:41.614Z	\N
308	1	6	acc32f2f-37d9-43df-a11f-dbc2c21fe580	6	47	2	9	System	2021-04-21T12:37:41.616Z	System	2021-04-21T12:37:41.616Z	\N
309	1	6	861bb928-83b9-4154-8518-c31d3ddaa83e	6	47	2	9	System	2021-04-21T12:37:41.619Z	System	2021-04-21T12:37:41.619Z	\N
310	1	6	373f5dc1-570d-4233-a44e-a1b42b7b2786	6	47	2	9	System	2021-04-21T12:37:41.621Z	System	2021-04-21T12:37:41.621Z	\N
311	1	6	784abb15-5def-482b-9e3a-66b458ce2c4c	6	47	2	9	System	2021-04-21T12:37:41.623Z	System	2021-04-21T12:37:41.623Z	\N
312	1	6	821878b5-5e04-4ca1-b68a-595fa6588506	6	47	2	9	System	2021-04-21T12:37:41.625Z	System	2021-04-21T12:37:41.625Z	\N
313	1	6	986c3df5-9900-4825-a94d-1a54213eb3c6	6	47	2	9	System	2021-04-21T12:37:41.628Z	System	2021-04-21T12:37:41.628Z	\N
314	1	6	e2534ad6-11a2-43c4-9cf3-134ae5d7e6b8	6	47	2	9	System	2021-04-21T12:37:41.630Z	System	2021-04-21T12:37:41.630Z	\N
315	1	6	4df94de1-be84-4e18-9d28-d66707fbaaa1	6	47	2	9	System	2021-04-21T12:37:41.632Z	System	2021-04-21T12:37:41.632Z	\N
316	1	6	a78c31ae-9867-4d95-8623-f7ba476c5ed3	6	47	2	9	System	2021-04-21T12:37:41.634Z	System	2021-04-21T12:37:41.634Z	\N
317	1	6	293201a9-8127-47af-aaca-c98121ce681b	6	47	2	9	System	2021-04-21T12:37:41.636Z	System	2021-04-21T12:37:41.636Z	\N
318	1	6	1e8365a4-4db7-4c2a-af94-601725cd4d99	6	47	2	9	System	2021-04-21T12:37:41.639Z	System	2021-04-21T12:37:41.639Z	\N
319	1	6	040968c2-e22b-4516-9706-9f4c68db80ad	6	47	2	9	System	2021-04-21T12:37:41.641Z	System	2021-04-21T12:37:41.641Z	\N
320	1	6	fa296f45-c65e-4de0-b8f2-97a6388770b5	6	47	2	9	System	2021-04-21T12:37:41.643Z	System	2021-04-21T12:37:41.643Z	\N
321	1	6	f6a05cc3-1767-4335-b3ac-8a8db2fc6475	6	47	2	9	System	2021-04-21T12:37:41.645Z	System	2021-04-21T12:37:41.645Z	\N
322	1	6	7ad02497-ba53-42c0-a94e-0a40eb1ab27b	6	47	2	9	System	2021-04-21T12:37:41.647Z	System	2021-04-21T12:37:41.647Z	\N
323	1	6	bcd42b8f-09be-4163-be0b-ba0e300aae62	6	47	2	9	System	2021-04-21T12:37:41.649Z	System	2021-04-21T12:37:41.649Z	\N
324	1	6	f4d831c8-b3bd-4467-bea9-8c1aeb388872	6	47	2	9	System	2021-04-21T12:37:41.651Z	System	2021-04-21T12:37:41.651Z	\N
733	1	2	d1ab6768-428e-419c-ab8f-6e14687e1eac	13	3	2	4	\N	\N	\N	\N	\N
325	1	6	e6e3af3f-15da-42d2-9b27-1db8d0f63bbf	6	47	2	9	System	2021-04-21T12:37:41.653Z	System	2021-04-21T12:37:41.653Z	\N
326	1	6	ca168984-9f39-4852-90e2-082ce9fafe45	6	47	2	9	System	2021-04-21T12:37:41.656Z	System	2021-04-21T12:37:41.656Z	\N
327	1	6	a05a260e-e937-4f93-9177-5730732c1b1a	6	47	2	9	System	2021-04-21T12:37:41.658Z	System	2021-04-21T12:37:41.658Z	\N
328	1	6	be09487e-2dd9-4b63-b7f2-cb5b362cdf14	6	47	2	9	System	2021-04-21T12:37:41.660Z	System	2021-04-21T12:37:41.660Z	\N
329	1	6	7bd23391-a211-4547-8bf4-fdb248e8c4e4	6	47	2	9	System	2021-04-21T12:37:41.662Z	System	2021-04-21T12:37:41.662Z	\N
330	1	6	47b4dd65-4eec-48ca-84ee-547016a5e990	6	47	2	9	System	2021-04-21T12:37:41.664Z	System	2021-04-21T12:37:41.664Z	\N
331	1	6	21a44186-fab8-4ab9-ad3c-d24e8cd49eb4	6	47	2	9	System	2021-04-21T12:37:41.666Z	System	2021-04-21T12:37:41.666Z	\N
332	1	6	eb430b9e-0649-4532-a129-9ab1233714f9	6	47	2	9	System	2021-04-21T12:37:41.668Z	System	2021-04-21T12:37:41.668Z	\N
333	1	6	ab197d94-e2bd-4149-96f1-b5a16052bf30	6	47	2	9	System	2021-04-21T12:37:41.670Z	System	2021-04-21T12:37:41.670Z	\N
334	1	6	0464b7d6-1ae5-42b4-a626-9048e80cc72f	6	47	2	9	System	2021-04-21T12:37:41.672Z	System	2021-04-21T12:37:41.672Z	\N
335	1	6	a47ea6e1-572b-4859-9b21-ae8d832e9351	6	47	2	9	System	2021-04-21T12:37:41.674Z	System	2021-04-21T12:37:41.674Z	\N
336	1	6	b8874e63-1e21-4e6d-850e-a1ceb4125a18	6	47	2	9	System	2021-04-21T12:37:41.676Z	System	2021-04-21T12:37:41.676Z	\N
337	1	6	f8a7d5ad-4f0e-4ae4-b270-49327aff8b07	6	47	2	9	System	2021-04-21T12:37:41.678Z	System	2021-04-21T12:37:41.678Z	\N
338	1	6	a500195d-5375-4f22-ada0-5dbbf7fa9acb	6	47	2	9	System	2021-04-21T12:37:41.680Z	System	2021-04-21T12:37:41.680Z	\N
339	1	6	72610c07-29b8-426e-b1d7-5e471646b9e5	6	47	2	9	System	2021-04-21T12:37:41.682Z	System	2021-04-21T12:37:41.682Z	\N
340	1	6	88dd914e-2444-425c-94eb-274994d24740	6	47	2	9	System	2021-04-21T12:37:41.684Z	System	2021-04-21T12:37:41.684Z	\N
341	1	6	cca4a30a-3a79-42ad-905b-e932bc43ae24	6	47	2	9	System	2021-04-21T12:37:41.685Z	System	2021-04-21T12:37:41.685Z	\N
342	1	6	8d9553f4-f9df-40f2-91d8-4521a197497c	6	47	2	9	System	2021-04-21T12:37:41.687Z	System	2021-04-21T12:37:41.687Z	\N
343	1	6	e6c918a3-a27e-401a-b68e-236ee61c9f5a	6	47	2	9	System	2021-04-21T12:37:41.689Z	System	2021-04-21T12:37:41.689Z	\N
344	1	6	926232b5-1ece-4244-a0e4-e0332d510bdb	6	47	2	9	System	2021-04-21T12:37:41.692Z	System	2021-04-21T12:37:41.692Z	\N
345	1	6	046274f8-e97c-4c12-8628-f453735777e5	6	47	2	9	System	2021-04-21T12:37:41.694Z	System	2021-04-21T12:37:41.694Z	\N
346	1	6	f152a1ea-f1f8-46ec-8f8e-2592c162dc5f	6	47	2	9	System	2021-04-21T12:37:41.696Z	System	2021-04-21T12:37:41.696Z	\N
347	1	6	ed0b2654-40fa-45f0-a58e-34e0cc09d824	6	47	2	9	System	2021-04-21T12:37:41.698Z	System	2021-04-21T12:37:41.698Z	\N
348	1	6	65613de1-9ee8-4b1e-bb77-5044b4a12292	6	47	2	9	System	2021-04-21T12:37:41.700Z	System	2021-04-21T12:37:41.700Z	\N
349	1	6	2709ac35-a169-46a7-bab2-6a6dc98833e2	6	47	2	9	System	2021-04-21T12:37:41.702Z	System	2021-04-21T12:37:41.702Z	\N
350	1	6	8102de48-8b66-48ee-966c-2a2a98299f83	6	47	2	9	System	2021-04-21T12:37:41.704Z	System	2021-04-21T12:37:41.704Z	\N
351	1	6	039f1582-62e4-4393-8494-ca62f7cdadc1	6	47	2	9	System	2021-04-21T12:37:41.707Z	System	2021-04-21T12:37:41.707Z	\N
352	1	6	0dbf6b19-2631-46e9-b55f-7783575aab3a	6	47	2	9	System	2021-04-21T12:37:41.709Z	System	2021-04-21T12:37:41.709Z	\N
353	1	6	dca799e6-a2be-4534-891e-ccae4e455b47	6	47	2	9	System	2021-04-21T12:37:41.711Z	System	2021-04-21T12:37:41.711Z	\N
354	1	6	98342784-04e2-49c5-bd6a-d751c11b3c26	6	47	2	9	System	2021-04-21T12:37:41.713Z	System	2021-04-21T12:37:41.713Z	\N
355	1	6	57f906e0-434f-40f6-81b5-f9e78f86ce16	6	47	2	9	System	2021-04-21T12:37:41.716Z	System	2021-04-21T12:37:41.716Z	\N
356	1	6	477bc3ac-195a-46a0-b5cb-cdbadf425d17	6	47	2	9	System	2021-04-21T12:37:41.719Z	System	2021-04-21T12:37:41.719Z	\N
357	1	6	dc779e7d-f6d8-4335-b519-ffe21503b41f	6	47	2	9	System	2021-04-21T12:37:41.721Z	System	2021-04-21T12:37:41.721Z	\N
358	1	6	8e67224c-a834-4f8c-9419-3445d9dcdef6	6	47	2	9	System	2021-04-21T12:37:41.723Z	System	2021-04-21T12:37:41.723Z	\N
359	1	6	35eea4d3-cd32-497d-a623-b3108b1941b4	6	47	2	9	System	2021-04-21T12:37:41.726Z	System	2021-04-21T12:37:41.726Z	\N
360	1	6	aa5ff6e7-cdbb-4eeb-89e3-9d0d6910f4f6	6	47	2	9	System	2021-04-21T12:37:41.728Z	System	2021-04-21T12:37:41.728Z	\N
361	1	6	fdd3ea4f-8a5d-4eea-958e-e3eca8e2a6ec	6	47	2	9	System	2021-04-21T12:37:41.730Z	System	2021-04-21T12:37:41.730Z	\N
362	1	6	e4f1cf4e-7f8d-4d1d-b603-cff95a6180cd	6	47	2	9	System	2021-04-21T12:37:41.732Z	System	2021-04-21T12:37:41.732Z	\N
363	1	6	1ac24413-9261-433f-bff2-deedbd83160c	6	47	2	9	System	2021-04-21T12:37:41.734Z	System	2021-04-21T12:37:41.734Z	\N
364	1	6	929a2416-31cf-4eaf-bb5f-38ae33871e3a	6	47	2	9	System	2021-04-21T12:37:41.736Z	System	2021-04-21T12:37:41.736Z	\N
365	1	6	05cc5597-576d-4084-b2b3-c419d3a1320c	6	47	2	9	System	2021-04-21T12:37:41.738Z	System	2021-04-21T12:37:41.738Z	\N
734	1	2	0171d3ce-2855-45b3-a493-d01debc9db88	13	3	2	4	\N	\N	\N	\N	\N
366	1	6	564f2988-00c9-41f1-9a56-40a103b176d9	6	47	2	9	System	2021-04-21T12:37:41.740Z	System	2021-04-21T12:37:41.740Z	\N
367	1	6	f1dba664-e069-4d20-8c95-35a92a83c023	6	47	2	9	System	2021-04-21T12:37:41.742Z	System	2021-04-21T12:37:41.742Z	\N
368	1	6	1e663181-0141-4b3e-b4bf-514ca0144911	6	47	2	9	System	2021-04-21T12:37:41.744Z	System	2021-04-21T12:37:41.744Z	\N
369	2	6	a510a28f-8056-40b3-92a5-a001444a24dd	6	47	2	32	System	2021-04-21T12:37:41.747Z	System	2021-04-21T12:37:41.747Z	\N
370	2	6	bc56f09a-9528-47ee-8189-f0179bd38fc9	6	50	2	34	\N	\N	\N	\N	\N
371	1	6	3de464a4-2923-49c8-9dc7-68f6cf58b53c	6	24	2	13	System	2021-04-21T12:37:41.775Z	System	2021-04-21T12:37:41.775Z	\N
372	1	6	58206df4-b324-4a2a-856b-80078ea0edeb	6	24	2	13	System	2021-04-21T12:37:41.779Z	System	2021-04-21T12:37:41.779Z	\N
373	1	6	0ff03fdf-5bea-49e6-b800-5d64bf328752	6	24	2	13	System	2021-04-21T12:37:41.782Z	System	2021-04-21T12:37:41.782Z	\N
374	1	6	013289a7-9fc5-4cb1-9f29-9f10ff71b3cf	6	24	2	13	System	2021-04-21T12:37:41.785Z	System	2021-04-21T12:37:41.785Z	\N
375	1	6	f42de2a0-f10b-4161-837c-61569642822e	6	24	2	13	System	2021-04-21T12:37:41.788Z	System	2021-04-21T12:37:41.788Z	\N
376	1	6	133059b7-649c-427d-bf36-65a8b46abcdd	6	24	2	13	System	2021-04-21T12:37:41.791Z	System	2021-04-21T12:37:41.791Z	\N
377	3	6	d7d724ac-ba12-4dc6-bcc3-bba5878e9fb2	6	51	2	13	System	2021-04-21T12:37:41.795Z	System	2021-04-21T12:37:41.795Z	\N
378	1	6	18d3f98d-7235-43db-9166-f373559eca24	6	24	2	13	System	2021-04-21T12:37:41.810Z	System	2021-04-21T12:37:41.810Z	\N
379	1	6	5e7c6333-940a-479c-80aa-85cef7fc19a9	6	24	2	13	System	2021-04-21T12:37:41.813Z	System	2021-04-21T12:37:41.813Z	\N
380	1	6	20e6b212-7e04-4e3a-9656-6c48aefbfe4f	6	24	2	13	System	2021-04-21T12:37:41.815Z	System	2021-04-21T12:37:41.815Z	\N
381	1	6	d642b077-d929-4cd4-8731-21386bdc4787	6	24	2	13	System	2021-04-21T12:37:41.817Z	System	2021-04-21T12:37:41.817Z	\N
881	1	6	f860de76-2839-4df3-ac77-caaae9c82b02	25	141	2	11	admin	2021-04-21T12:40:51.958Z	admin	2021-04-21T12:40:51.958Z	\N
382	2	6	355ee265-4e1b-4d7a-a6f7-487d26956837	6	51	2	13	System	2021-04-21T12:37:41.826Z	System	2021-04-21T12:37:41.826Z	\N
383	2	6	458dfa99-1d70-4443-ba6f-8b23b9c4ab5c	6	51	2	13	System	2021-04-21T12:37:41.839Z	System	2021-04-21T12:37:41.839Z	\N
877	6	6	5da80b21-4adf-4f92-ad14-1b9f8d3ed946	26	51	2	11	admin	2021-04-21T12:40:45.225Z	admin	2021-04-21T12:40:45.225Z	\N
384	2	6	d1ddf3fc-ffd5-4877-b181-8f681f1f1ee8	6	51	2	13	System	2021-04-21T12:37:41.849Z	System	2021-04-21T12:37:41.849Z	\N
385	2	6	7b60e395-225d-44fd-a292-4220f4c5cc29	6	51	2	13	System	2021-04-21T12:37:41.859Z	System	2021-04-21T12:37:41.859Z	\N
386	2	6	27c7fdee-b39e-4472-8b5c-86ddce11a261	6	51	2	13	System	2021-04-21T12:37:41.869Z	System	2021-04-21T12:37:41.869Z	\N
387	2	6	8d0b4227-2a45-46b7-9143-2c8af8d49050	6	51	2	13	System	2021-04-21T12:37:41.878Z	System	2021-04-21T12:37:41.878Z	\N
888	1	5	486f96e2-ef9f-45bb-86a0-3bc88b74f8e4	33	240	2	61	\N	\N	\N	\N	\N
388	2	6	51517179-db7b-48f2-9719-a9a106459759	6	51	2	13	System	2021-04-21T12:37:41.888Z	System	2021-04-21T12:37:41.888Z	\N
389	2	6	4528f727-48d1-4b08-b019-57161757132d	6	51	2	13	System	2021-04-21T12:37:41.897Z	System	2021-04-21T12:37:41.897Z	\N
887	5	5	2d1fae15-a7be-422b-a470-e4de52bc41b3	33	51	2	61	admin	2021-04-21T12:41:03.958Z	admin	2021-04-21T12:41:03.958Z	\N
390	2	6	d0ce5fca-c273-4424-a456-10ae0de693bf	6	51	2	13	System	2021-04-21T12:37:41.906Z	System	2021-04-21T12:37:41.906Z	\N
391	2	6	a9391e37-420d-4b3e-a40f-608b2e3b3677	6	51	2	13	System	2021-04-21T12:37:41.915Z	System	2021-04-21T12:37:41.915Z	\N
392	2	6	0d7b66a1-9b64-4120-a6f5-414eec81f2f7	6	51	2	13	System	2021-04-21T12:37:41.929Z	System	2021-04-21T12:37:41.929Z	\N
889	3	5	e0f6a3d8-d432-42ed-ac65-53b3492ff4df	33	141	2	61	admin	2021-04-21T12:41:05.370Z	admin	2021-04-21T12:41:05.370Z	\N
890	3	5	72d3e5d0-1dc9-482a-a843-7381c93a1941	33	141	2	61	admin	2021-04-21T12:41:07.706Z	admin	2021-04-21T12:41:07.706Z	\N
891	1	6	e0f6a3d8-d432-42ed-ac65-53b3492ff4df	33	148	1	\N	\N	\N	\N	\N	\N
393	1	6	6e10d009-9919-4fd0-a203-28ea7b2cf784	6	24	2	13	System	2021-04-21T12:37:41.967Z	System	2021-04-21T12:37:41.967Z	\N
394	2	6	52c5380e-3fd6-44ae-ada9-7627f6747062	6	51	2	13	System	2021-04-21T12:37:41.972Z	System	2021-04-21T12:37:41.972Z	\N
880	1	6	bf853486-0dac-4738-9f5a-ede33d42c6f6	23	148	1	\N	\N	\N	\N	\N	\N
396	2	6	193293ab-b554-44ef-8636-e4cedc7309b4	6	51	2	13	System	2021-04-21T12:37:41.984Z	System	2021-04-21T12:37:41.984Z	\N
397	1	6	b082579f-5197-470a-8ea5-cf08930f304e	6	24	2	13	System	2021-04-21T12:37:41.997Z	System	2021-04-21T12:37:41.997Z	\N
879	2	6	216a80b2-446a-4631-bb19-5947c2491239	23	141	2	11	admin	2021-04-21T12:40:47.587Z	admin	2021-04-21T12:40:47.587Z	\N
398	2	6	c5eeaab4-e082-4995-a4d9-82b7fd150d2a	6	51	2	13	System	2021-04-21T12:37:42.002Z	System	2021-04-21T12:37:42.002Z	\N
399	2	6	43a7e9da-7cbe-4bae-97ec-fce2ef675c2f	6	51	2	13	System	2021-04-21T12:37:42.011Z	System	2021-04-21T12:37:42.011Z	\N
400	2	6	2225f865-73d8-49d5-897f-6c6222e2f748	6	51	2	13	System	2021-04-21T12:37:42.020Z	System	2021-04-21T12:37:42.020Z	\N
401	2	6	d18349a2-9c54-48fd-bd86-107c1fdc3175	6	51	2	13	System	2021-04-21T12:37:42.028Z	System	2021-04-21T12:37:42.028Z	\N
402	2	6	29394403-43ee-4f7c-9511-91d4032888d1	6	51	2	13	System	2021-04-21T12:37:42.036Z	System	2021-04-21T12:37:42.036Z	\N
403	2	6	56d48ded-3b05-4c44-a18b-9a0257697dcd	6	51	2	13	System	2021-04-21T12:37:42.044Z	System	2021-04-21T12:37:42.044Z	\N
404	2	6	4e63d5ad-6a31-46ee-aa6e-97135030402a	6	51	2	13	System	2021-04-21T12:37:42.052Z	System	2021-04-21T12:37:42.052Z	\N
405	1	6	a102f0ac-9017-4a4c-a330-c00b8e745afd	6	24	2	13	System	2021-04-21T12:37:42.061Z	System	2021-04-21T12:37:42.061Z	\N
406	4	6	f65a1324-5c46-495d-9adb-7b02b01ff9fa	6	51	2	13	System	2021-04-21T12:37:42.064Z	System	2021-04-21T12:37:42.064Z	\N
1125	4	6	54ce1a72-12a0-4dec-adf3-86c22e500e22	432	51	2	11	User3	2020-01-07T13:37:13Z	admin	2021-04-21T12:42:29.942Z	2020-09-15T07:33:58Z
407	4	6	b42ec734-3f52-467e-bc73-d5f24c02a787	6	51	2	13	System	2021-04-21T12:37:42.072Z	System	2021-04-21T12:37:42.072Z	\N
1129	4	6	68a1618f-4ede-4fe4-a915-1edc152c1194	443	51	2	11	User9	2020-10-13T22:30:50Z	admin	2021-04-21T12:42:30.107Z	2020-01-26T11:39:46Z
408	4	6	08d3c0e1-395f-4fc6-b16e-c965db1c836c	6	51	2	13	System	2021-04-21T12:37:42.083Z	System	2021-04-21T12:37:42.083Z	\N
409	4	6	0d236afd-61ed-4319-ba85-bbd6c810f678	6	51	2	13	System	2021-04-21T12:37:42.091Z	System	2021-04-21T12:37:42.091Z	\N
410	4	6	11c65f6e-a0fc-4f94-bf7c-6f1fefed6ff6	6	51	2	13	System	2021-04-21T12:37:42.098Z	System	2021-04-21T12:37:42.098Z	\N
885	1	6	f5c0bd10-16a4-4dfb-9947-b47d2fe2f4e0	29	148	1	\N	\N	\N	\N	\N	\N
411	4	6	17370988-71f1-46e5-bdcd-cc309896ce82	6	51	2	13	System	2021-04-21T12:37:42.108Z	System	2021-04-21T12:37:42.108Z	\N
412	4	6	c1c59686-3912-4bb4-b15f-dd9e0b150a68	6	51	2	13	System	2021-04-21T12:37:42.117Z	System	2021-04-21T12:37:42.117Z	\N
413	1	6	315093d9-12ed-44f6-be78-86fcbaa02675	6	24	2	13	System	2021-04-21T12:37:42.125Z	System	2021-04-21T12:37:42.125Z	\N
904	2	6	4cd80404-5c4c-4fa4-ad63-38eb1d831cba	43	24	2	64	admin	2021-04-21T12:41:55.357Z	admin	2021-04-21T12:41:55.357Z	\N
414	4	6	wf-email-html-ftl	6	51	2	13	System	2021-04-21T12:37:42.129Z	System	2021-04-21T12:37:42.129Z	\N
905	1	1	3dd70af2-82d3-4c86-903d-9d7401a62c39	43	5	2	2	\N	\N	\N	\N	\N
906	2	6	b3342fcf-c288-450e-a5ae-3b5978a84293	45	106	2	66	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.572Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.572Z	\N
415	4	6	140df20b-e97c-48d7-a057-4ed30732cf90	6	51	2	13	System	2021-04-21T12:37:42.135Z	System	2021-04-21T12:37:42.135Z	\N
907	1	6	ddf3b3ad-e2b1-4d8f-b66b-c789c35de75c	45	89	2	43	\N	\N	\N	\N	\N
908	1	6	778ce411-143a-4013-bae7-bc7924c9acbe	45	89	2	43	\N	\N	\N	\N	\N
909	1	6	fc0546f4-c5ba-4673-ab08-c7eee0bcdf65	45	89	2	43	\N	\N	\N	\N	\N
416	4	6	32114724-d2ec-4681-905a-92dbf58fad85	6	51	2	13	System	2021-04-21T12:37:42.142Z	System	2021-04-21T12:37:42.142Z	\N
910	1	6	e98b4d96-d097-47b7-8c53-949da0d4c6bc	45	89	2	43	\N	\N	\N	\N	\N
911	1	6	f5e02751-e477-484b-8a42-41dc2da25a94	45	89	2	43	\N	\N	\N	\N	\N
903	3	6	190bd5e4-0ba2-4f79-aec4-88cbe7959fc3	45	35	2	62	\N	\N	\N	\N	\N
417	4	6	3e1a413f-f506-43ec-88ab-3f609ceb46bd	6	51	2	13	System	2021-04-21T12:37:42.153Z	System	2021-04-21T12:37:42.153Z	\N
418	4	6	183b7821-088e-4c61-9858-701f551129bf	6	51	2	13	System	2021-04-21T12:37:42.160Z	System	2021-04-21T12:37:42.160Z	\N
419	4	6	bbb60475-cb9c-403e-87eb-899d8d5ae75a	6	51	2	13	System	2021-04-21T12:37:42.169Z	System	2021-04-21T12:37:42.169Z	\N
912	7	6	277d580b-e727-494a-9d78-df08c5439082	45	24	2	67	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.714Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.714Z	\N
420	4	6	8d23080c-36a6-4c35-aebb-1b2ff83471cb	6	51	2	13	System	2021-04-21T12:37:42.177Z	System	2021-04-21T12:37:42.177Z	\N
421	2	6	6367fdb2-0233-4b7f-9d9b-e1b22d044706	6	51	2	15	System	2021-04-21T12:37:42.186Z	System	2021-04-21T12:37:42.186Z	\N
422	2	6	02b2d4ff-58b6-4605-b56b-28343495ccdc	6	51	2	13	System	2021-04-21T12:37:42.200Z	System	2021-04-21T12:37:42.200Z	\N
913	5	6	02b51d02-7911-41aa-8cd8-c1c7e1849bf6	45	24	2	67	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.730Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.730Z	\N
892	1	6	72d3e5d0-1dc9-482a-a843-7381c93a1941	33	148	1	\N	\N	\N	\N	\N	\N
423	2	6	a1d5bc61-bf76-4f78-b3ed-c386e9442fce	6	51	2	13	System	2021-04-21T12:37:42.208Z	System	2021-04-21T12:37:42.208Z	\N
893	1	6	2d1fae15-a7be-422b-a470-e4de52bc41b3	33	148	1	\N	\N	\N	\N	\N	\N
424	2	6	c390ab0a-1ede-4534-9faf-af99375f4c1f	6	51	2	13	System	2021-04-21T12:37:42.214Z	System	2021-04-21T12:37:42.214Z	\N
425	2	6	19ac0af3-cf7f-45ca-bda6-5195989c465c	6	51	2	13	System	2021-04-21T12:37:42.221Z	System	2021-04-21T12:37:42.221Z	\N
898	1	4	3c3d010b-4e45-49db-b473-4305b48f54e6	38	164	2	60	admin	2021-04-21T12:41:54.365Z	admin	2021-04-21T12:41:54.365Z	\N
426	2	6	e02ca3a5-285d-4d98-bba9-bd53b7558c8b	6	51	2	13	System	2021-04-21T12:37:42.227Z	System	2021-04-21T12:37:42.227Z	\N
899	1	4	ad8a7a35-9310-41e2-90b0-f18a521fab73	38	243	2	60	admin	2021-04-21T12:41:54.373Z	admin	2021-04-21T12:41:54.373Z	\N
427	2	6	ce61e2ee-b66e-4915-b888-059dc2e8f959	6	51	2	13	System	2021-04-21T12:37:42.233Z	System	2021-04-21T12:37:42.233Z	\N
429	2	6	1d3dbd00-cd15-4e78-a0b3-98b81aac89ba	6	51	2	13	System	2021-04-21T12:37:42.246Z	System	2021-04-21T12:37:42.246Z	\N
897	4	6	c38fe89b-3ed3-4d26-84ea-be53bbcd2408	39	243	2	13	admin	2021-04-21T12:41:54.338Z	admin	2021-04-21T12:41:54.600Z	\N
430	2	6	92e41bc4-79ed-44c4-9a6c-b230ca032984	6	51	2	13	System	2021-04-21T12:37:42.253Z	System	2021-04-21T12:37:42.253Z	\N
431	2	6	e8157e36-9d13-4bc0-a4d6-f944c2c2fd54	6	51	2	13	System	2021-04-21T12:37:42.262Z	System	2021-04-21T12:37:42.262Z	\N
432	2	6	03c36099-8e74-45c9-b6b3-e204e3160200	6	51	2	13	System	2021-04-21T12:37:42.268Z	System	2021-04-21T12:37:42.268Z	\N
433	1	6	81c248f9-5153-4171-bb68-c6ff38403eb3	6	24	2	13	System	2021-04-21T12:37:42.275Z	System	2021-04-21T12:37:42.275Z	\N
434	1	6	266859df-57fa-46b4-afdd-f58c41638ec5	6	24	2	13	System	2021-04-21T12:37:42.278Z	System	2021-04-21T12:37:42.278Z	\N
435	1	6	ece81e42-c322-4d3e-b540-20f5bfed5f58	6	24	2	13	System	2021-04-21T12:37:42.281Z	System	2021-04-21T12:37:42.281Z	\N
436	2	6	cb619524-bb96-4066-be37-6f6ddc075660	6	51	2	13	System	2021-04-21T12:37:42.284Z	System	2021-04-21T12:37:42.284Z	\N
1136	4	6	01247c4b-1dfd-4e22-be89-440c99e3c34f	457	51	2	11	User5	2020-09-23T17:12:35Z	admin	2021-04-21T12:42:30.413Z	2020-08-12T02:02:02Z
437	2	6	5e830a93-b3ba-461c-b671-37abfd7c9937	6	51	2	13	System	2021-04-21T12:37:42.291Z	System	2021-04-21T12:37:42.291Z	\N
438	2	6	aa13d4b3-a714-4ff9-ae71-4454b30908ab	6	51	2	13	System	2021-04-21T12:37:42.298Z	System	2021-04-21T12:37:42.298Z	\N
439	2	6	04bcfbc9-de89-445f-bfdc-cdc250a1cd4f	6	51	2	13	System	2021-04-21T12:37:42.304Z	System	2021-04-21T12:37:42.304Z	\N
440	2	6	61872d09-53ae-40bc-86a3-ffc773012117	6	51	2	13	System	2021-04-21T12:37:42.311Z	System	2021-04-21T12:37:42.311Z	\N
441	2	6	c25b91af-c01b-4b4c-91db-11495894ee72	6	51	2	13	System	2021-04-21T12:37:42.318Z	System	2021-04-21T12:37:42.318Z	\N
442	2	6	6c82944c-778b-43f0-b1a6-244fa8762c82	6	51	2	13	System	2021-04-21T12:37:42.325Z	System	2021-04-21T12:37:42.325Z	\N
443	2	6	04971be3-fc45-4f4b-9ef5-349db0295382	6	51	2	13	System	2021-04-21T12:37:42.333Z	System	2021-04-21T12:37:42.333Z	\N
444	2	6	e91ec48b-8904-460b-91ed-85de06ddda1b	6	51	2	13	System	2021-04-21T12:37:42.340Z	System	2021-04-21T12:37:42.340Z	\N
445	2	6	139ebed6-2262-492c-817c-cff71d3cfd46	6	51	2	13	System	2021-04-21T12:37:42.347Z	System	2021-04-21T12:37:42.347Z	\N
446	2	6	756a8988-1141-4cfa-81cf-60c98d241df4	6	51	2	13	System	2021-04-21T12:37:42.354Z	System	2021-04-21T12:37:42.354Z	\N
447	2	6	340917bc-5913-40ee-8f67-319c6a4a1b26	6	51	2	13	System	2021-04-21T12:37:42.361Z	System	2021-04-21T12:37:42.361Z	\N
448	2	6	6a836880-dc24-402c-998d-72f843c21078	6	51	2	13	System	2021-04-21T12:37:42.368Z	System	2021-04-21T12:37:42.368Z	\N
454	6	6	a9a9f477-5c57-11dc-ad6c-5136d620963c	41	24	2	13	System	2021-04-21T12:37:42.428Z	admin	2021-04-21T12:41:55.096Z	\N
449	2	6	f63728e6-5fcc-4341-a577-5497dd22db12	6	51	2	13	System	2021-04-21T12:37:42.379Z	System	2021-04-21T12:37:42.379Z	\N
900	4	6	a77289bc-0602-4a94-9bcf-ac2c2aa02c87	42	243	2	13	admin	2021-04-21T12:41:54.911Z	admin	2021-04-21T12:41:55.159Z	\N
450	2	6	7f80fef2-191a-4d3f-9b51-4f6e71922c16	6	51	2	13	System	2021-04-21T12:37:42.386Z	System	2021-04-21T12:37:42.386Z	\N
451	2	6	c832a646-8b9e-42c0-9ead-b7949b2c3c2c	6	51	2	13	System	2021-04-21T12:37:42.393Z	System	2021-04-21T12:37:42.393Z	\N
1135	4	6	694bed4c-9846-40f1-bc6b-01853b6e82df	456	51	2	11	User6	2020-03-17T03:35:26Z	admin	2021-04-21T12:42:30.413Z	2020-12-01T16:57:12Z
453	2	6	3b4fc54c-f889-4be3-b973-a9a012c2cb7c	6	51	2	13	System	2021-04-21T12:37:42.407Z	System	2021-04-21T12:37:42.407Z	\N
428	2	6	c6b8e11e-fb2a-48c5-931a-a2d4b14682dc	6	24	2	13	System	2021-04-21T12:37:42.243Z	System	2021-04-21T12:37:42.243Z	\N
452	2	6	54c66c12-0219-4a4b-b7e7-ac28a51fd7b4	6	24	2	13	System	2021-04-21T12:37:42.404Z	System	2021-04-21T12:37:42.404Z	\N
455	1	6	c317f789-5c57-11dc-ad6c-5136d620963c	6	59	2	13	System	2021-04-21T12:37:42.434Z	System	2021-04-21T12:37:42.434Z	\N
456	3	6	ceca5a11-5c57-11dc-ad6c-5136d620963c	6	61	2	13	System	2021-04-21T12:37:42.436Z	System	2021-04-21T12:37:42.436Z	\N
457	2	6	c074eb05-5c57-11dc-ad6c-5136d620963c	6	66	2	13	System	2021-04-21T12:37:42.443Z	System	2021-04-21T12:37:42.443Z	\N
458	2	6	c074eb06-5c57-11dc-ad6c-5136d620963c	6	72	2	13	System	2021-04-21T12:37:42.456Z	System	2021-04-21T12:37:42.456Z	\N
459	2	6	d11a167c-5c57-11dc-ad6c-5136d620963c	6	75	2	13	System	2021-04-21T12:37:42.460Z	System	2021-04-21T12:37:42.460Z	\N
460	2	6	c074eb07-5c57-11dc-ad6c-5136d620963c	6	79	2	13	System	2021-04-21T12:37:42.464Z	System	2021-04-21T12:37:42.464Z	\N
461	2	6	d34fbb36-5c57-11dc-ad6c-5136d620963c	6	75	2	13	System	2021-04-21T12:37:42.466Z	System	2021-04-21T12:37:42.466Z	\N
462	1	6	878bebfc-e383-49f6-a0a3-8cbf33b368e5	6	24	2	13	System	2021-04-21T12:37:42.470Z	System	2021-04-21T12:37:42.470Z	\N
463	1	6	5f016c87-2a66-42c5-a288-991150e747d4	6	24	2	13	System	2021-04-21T12:37:42.477Z	System	2021-04-21T12:37:42.477Z	\N
464	4	6	05590cd0-607e-11dc-af48-8b100325f217	6	24	2	13	System	2021-04-21T12:37:42.484Z	System	2021-04-21T12:37:42.484Z	\N
465	1	6	1e3a3916-607e-11dc-af48-8b100325f217	6	59	2	13	System	2021-04-21T12:37:42.489Z	System	2021-04-21T12:37:42.489Z	\N
466	3	6	1e40c8cc-607e-11dc-af48-8b100325f217	6	61	2	13	System	2021-04-21T12:37:42.491Z	System	2021-04-21T12:37:42.491Z	\N
914	6	6	d5895a80-669a-4015-bc14-286d9c254f13	45	51	2	67	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.739Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.739Z	\N
467	2	6	1e18a751-607e-11dc-af48-8b100325f217	6	66	2	13	System	2021-04-21T12:37:42.494Z	System	2021-04-21T12:37:42.494Z	\N
468	2	6	1e2ddd02-607e-11dc-af48-8b100325f217	6	72	2	13	System	2021-04-21T12:37:42.501Z	System	2021-04-21T12:37:42.501Z	\N
469	2	6	1e74d127-607e-11dc-af48-8b100325f217	6	75	2	13	System	2021-04-21T12:37:42.504Z	System	2021-04-21T12:37:42.504Z	\N
470	2	6	1e2ddd03-607e-11dc-af48-8b100325f217	6	79	2	13	System	2021-04-21T12:37:42.506Z	System	2021-04-21T12:37:42.506Z	\N
471	2	6	1e88cd61-607e-11dc-af48-8b100325f217	6	75	2	13	System	2021-04-21T12:37:42.509Z	System	2021-04-21T12:37:42.509Z	\N
472	2	6	tag:tag-root	6	47	2	36	System	2021-04-21T12:37:42.512Z	System	2021-04-21T12:37:42.512Z	\N
915	6	6	7e61bdd9-cf84-427e-874c-a1fd9cbcb141	45	51	2	67	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.752Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.752Z	\N
474	5	6	a9513758-12f6-4d48-a451-52029bd48706	6	24	2	11	System	2021-04-21T12:37:42.530Z	System	2021-04-21T12:37:42.530Z	\N
916	6	6	4be01f24-2353-4dd4-989d-8c3f447a28e2	45	51	2	67	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.765Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.765Z	\N
475	4	6	fc8d4036-88d7-4f80-bd6c-32dd69ecf9ba	6	24	2	38	System	2021-04-21T12:37:42.539Z	System	2021-04-21T12:37:42.539Z	\N
476	4	6	31b0773d-298d-427d-a6bd-85ff49334eb1	6	24	2	40	System	2021-04-21T12:37:42.553Z	System	2021-04-21T12:37:42.553Z	\N
477	2	6	781c661d-b880-45de-826a-a5391f287c4f	6	3	2	42	\N	\N	\N	\N	\N
478	1	6	GROUP_ALFRESCO_ADMINISTRATORS	6	89	2	43	\N	\N	\N	\N	\N
479	1	6	GROUP_EMAIL_CONTRIBUTORS	6	89	2	43	\N	\N	\N	\N	\N
480	1	6	GROUP_SITE_ADMINISTRATORS	6	89	2	43	\N	\N	\N	\N	\N
481	1	6	GROUP_ALFRESCO_SEARCH_ADMINISTRATORS	6	89	2	43	\N	\N	\N	\N	\N
482	1	6	GROUP_ALFRESCO_MODEL_ADMINISTRATORS	6	89	2	43	\N	\N	\N	\N	\N
483	2	6	c299fa8a-d65f-4a00-8c8b-28ff0ae5055b	6	3	2	44	\N	\N	\N	\N	\N
484	1	6	AUTH.ALF	6	91	2	45	System	2021-04-21T12:37:42.597Z	System	2021-04-21T12:37:42.597Z	\N
485	1	6	APP.DEFAULT	6	91	2	45	System	2021-04-21T12:37:42.616Z	System	2021-04-21T12:37:42.616Z	\N
486	2	6	remote_credentials_container	6	3	2	46	\N	\N	\N	\N	\N
487	2	6	syncset_definitions_container	6	3	2	48	\N	\N	\N	\N	\N
473	2	6	7c98f065-81a0-4ea5-a148-320c05c1f089	12	81	2	11	System	2021-04-21T12:37:42.527Z	System	2021-04-21T12:37:48.911Z	\N
488	1	6	01166c60-c2b2-41d1-bacc-32f52359ac68	6	24	2	13	System	2021-04-21T12:37:43.215Z	System	2021-04-21T12:37:43.215Z	\N
489	1	6	9ab5a400-4e64-44af-9f4a-60fddcea428b	6	24	2	13	System	2021-04-21T12:37:43.219Z	System	2021-04-21T12:37:43.219Z	\N
490	2	6	6b314348-2d53-448f-840f-c07cfefb44cf	6	51	2	13	System	2021-04-21T12:37:43.222Z	System	2021-04-21T12:37:43.222Z	\N
491	2	6	abcd97af-81e3-42da-9ac2-7d3a7d548b45	6	51	2	13	System	2021-04-21T12:37:43.229Z	System	2021-04-21T12:37:43.229Z	\N
492	1	6	dcf1ae15-a2d3-4259-b849-9ce5449a6b8a	6	24	2	13	System	2021-04-21T12:37:43.237Z	System	2021-04-21T12:37:43.237Z	\N
493	1	6	2a3001ff-8503-4e02-b132-ddf38da28507	6	24	2	13	System	2021-04-21T12:37:43.240Z	System	2021-04-21T12:37:43.240Z	\N
494	1	6	122eecb2-aa83-43dc-b1f3-5e7fc1958225	6	97	2	13	System	2021-04-21T12:37:43.244Z	System	2021-04-21T12:37:43.244Z	\N
495	1	6	e4e2a64f-95b6-473f-acda-ff2af67419ac	6	24	2	13	System	2021-04-21T12:37:43.249Z	System	2021-04-21T12:37:43.249Z	\N
496	1	6	224d08cc-a937-4a97-a990-98ee97a6de5b	6	24	2	13	System	2021-04-21T12:37:43.253Z	System	2021-04-21T12:37:43.253Z	\N
498	1	6	replication_actions_space	6	24	2	13	System	2021-04-21T12:37:43.262Z	System	2021-04-21T12:37:43.262Z	\N
499	1	6	8a890c54-6e79-476b-a402-a51aa42a36b8	6	59	2	13	System	2021-04-21T12:37:43.268Z	System	2021-04-21T12:37:43.268Z	\N
500	1	6	03835973-e040-4139-b971-e130e9559b9e	6	61	2	13	System	2021-04-21T12:37:43.275Z	System	2021-04-21T12:37:43.275Z	\N
917	6	6	82778fe9-5f68-4222-bf28-1110df2e5c52	45	51	2	67	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.777Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.777Z	\N
501	2	6	afc0bf11-9a76-4b29-bbf9-ecb51e7d1a07	6	66	2	13	System	2021-04-21T12:37:43.279Z	System	2021-04-21T12:37:43.279Z	\N
502	1	6	4642e40e-c64d-4cc3-8205-579ab517e29a	6	59	2	13	System	2021-04-21T12:37:43.289Z	System	2021-04-21T12:37:43.289Z	\N
503	1	6	fdfc838e-3ee6-4265-a6dc-14e426593f74	6	79	2	13	System	2021-04-21T12:37:43.291Z	System	2021-04-21T12:37:43.291Z	\N
504	1	6	ee3f914d-9c4a-4175-817e-f2ca18d3c89d	6	75	2	13	System	2021-04-21T12:37:43.295Z	System	2021-04-21T12:37:43.295Z	\N
505	1	6	e96dc80b-6e39-4bcf-9099-bc41382370bf	6	72	2	13	System	2021-04-21T12:37:43.296Z	System	2021-04-21T12:37:43.296Z	\N
506	1	6	b29db545-e1b3-4fa4-98b9-c2818e872ecb	6	75	2	13	System	2021-04-21T12:37:43.298Z	System	2021-04-21T12:37:43.298Z	\N
507	1	6	b3b4b3d1-0a60-4b98-a6c0-1fa5d09459c5	6	24	2	13	System	2021-04-21T12:37:43.302Z	System	2021-04-21T12:37:43.302Z	\N
508	4	6	0ce1a730-e43a-4f4e-8d66-1df36a78d5df	6	51	2	13	System	2021-04-21T12:37:43.307Z	System	2021-04-21T12:37:43.307Z	\N
918	6	6	65f93d73-ae65-4d70-b447-8849b8cf4acb	45	51	2	67	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.796Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.796Z	\N
509	4	6	95fc855a-73cb-4f81-9afa-bdb625122dac	6	51	2	13	System	2021-04-21T12:37:43.317Z	System	2021-04-21T12:37:43.317Z	\N
510	4	6	01e01863-aa9a-4621-8fa5-a5457dd6beb7	6	51	2	13	System	2021-04-21T12:37:43.325Z	System	2021-04-21T12:37:43.325Z	\N
511	4	6	5d94d750-6e7a-4edf-9d8d-bd634983ed05	6	51	2	13	System	2021-04-21T12:37:43.333Z	System	2021-04-21T12:37:43.333Z	\N
497	2	6	rendering_actions_space	16	24	2	13	System	2021-04-21T12:37:43.257Z	System	2021-04-21T12:37:50.103Z	\N
919	5	6	44049cfc-3d1e-4b47-93ba-913e57aa1bb8	45	24	2	67	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.808Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.808Z	\N
512	4	6	d88347cb-d025-4b3e-b347-fffb5f0a6092	6	51	2	13	System	2021-04-21T12:37:43.340Z	System	2021-04-21T12:37:43.340Z	\N
513	4	6	25f62591-b2dd-46db-81dc-140273fd971e	6	51	2	13	System	2021-04-21T12:37:43.347Z	System	2021-04-21T12:37:43.347Z	\N
920	5	6	569f1ab9-ddcc-4f0f-a1c3-efb771aad592	45	24	2	67	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.816Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.816Z	\N
514	4	6	fed14137-51c2-4011-ae65-cef93128ff93	6	51	2	13	System	2021-04-21T12:37:43.354Z	System	2021-04-21T12:37:43.354Z	\N
515	4	6	8c9f78ed-c489-4776-8031-ca1dfe85d8af	6	51	2	13	System	2021-04-21T12:37:43.363Z	System	2021-04-21T12:37:43.363Z	\N
921	5	6	1a704d26-c377-4903-905e-f9992b4be99a	45	24	2	67	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.830Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.830Z	\N
516	4	6	1d5466fe-fe87-4119-89b0-5528bf1eed3a	6	51	2	13	System	2021-04-21T12:37:43.370Z	System	2021-04-21T12:37:43.370Z	\N
517	4	6	86a8204e-d81d-4fd9-90f2-46738dfa7fac	6	51	2	13	System	2021-04-21T12:37:43.377Z	System	2021-04-21T12:37:43.377Z	\N
922	6	6	25ba0d7f-f3f0-48e9-9a09-34cef170002a	45	51	2	67	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.838Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.838Z	\N
923	2	6	69858ec8-5a33-4ace-9c9d-bf7c25d8540a	45	24	2	68	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.853Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:55.853Z	\N
518	4	6	18147c5e-d245-46f4-9bdc-4134e3a0cc71	6	51	2	13	System	2021-04-21T12:37:43.384Z	System	2021-04-21T12:37:43.384Z	\N
924	1	6	d96c1efe-678b-4d37-ae8e-1a7071bfe857	46	24	2	69	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:56.366Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:56.366Z	\N
926	1	4	ae620eab-1d2d-4e40-9ec5-04644d353975	47	164	2	60	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:56.759Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:56.759Z	\N
519	4	6	4a1da33e-34fc-4df8-a4ce-9984ed3526e8	6	51	2	13	System	2021-04-21T12:37:43.392Z	System	2021-04-21T12:37:43.392Z	\N
927	1	4	2bc93f5e-2f77-48d0-a66d-f489db9e1c2c	47	51	2	60	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:56.777Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:56.777Z	\N
520	4	6	0d42f772-43a8-413b-82d0-a9b58a635bb1	6	51	2	13	System	2021-04-21T12:37:43.400Z	System	2021-04-21T12:37:43.400Z	\N
925	4	6	a170644e-906e-427c-8909-99b62d445246	48	51	2	69	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:56.591Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:56.956Z	\N
521	4	6	d799662c-8ccf-40ad-9b31-73ee77a5c712	6	51	2	13	System	2021-04-21T12:37:43.407Z	System	2021-04-21T12:37:43.407Z	\N
522	4	6	58a51043-c499-4eb3-b908-4e30edada432	6	51	2	13	System	2021-04-21T12:37:43.416Z	System	2021-04-21T12:37:43.416Z	\N
929	1	4	177d879a-ec57-4c1c-84d8-687cffc17a15	49	164	2	60	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:57.029Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:57.029Z	\N
930	1	4	20c58c07-202e-4958-8c65-8c7adfdaee30	49	51	2	60	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:57.037Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:57.037Z	\N
523	4	6	37e30fba-3d9b-464e-9a1d-2ab681861a3e	6	51	2	13	System	2021-04-21T12:37:43.423Z	System	2021-04-21T12:37:43.423Z	\N
928	4	6	7b0ca868-9d44-4f54-be37-3565f3a0b0fb	50	51	2	69	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:56.977Z	UserSearch-zGiNtcNRnaJPlLO	2021-04-21T12:41:57.180Z	\N
524	4	6	a66923d0-4e11-4403-81a1-3389cf6ef312	6	51	2	13	System	2021-04-21T12:37:43.432Z	System	2021-04-21T12:37:43.432Z	\N
525	4	6	fe1da873-2086-4630-a89b-1a0344de1387	6	51	2	13	System	2021-04-21T12:37:43.439Z	System	2021-04-21T12:37:43.439Z	\N
526	4	6	9c08f1ff-cbb6-42d9-bf21-808210149d2e	6	51	2	13	System	2021-04-21T12:37:43.450Z	System	2021-04-21T12:37:43.450Z	\N
527	4	6	3789332f-b610-4c2d-bd59-54142763c70a	6	51	2	13	System	2021-04-21T12:37:43.457Z	System	2021-04-21T12:37:43.457Z	\N
528	4	6	4d8dcbbb-28e0-4f15-82b0-e5a94c22593d	6	51	2	13	System	2021-04-21T12:37:43.463Z	System	2021-04-21T12:37:43.463Z	\N
529	4	6	62926a42-7198-4324-a278-26a70e9a91ae	6	51	2	13	System	2021-04-21T12:37:43.472Z	System	2021-04-21T12:37:43.472Z	\N
530	2	6	ea37022d-568f-4ecc-ad34-804e4ac77ec7	6	51	2	13	System	2021-04-21T12:37:43.481Z	System	2021-04-21T12:37:43.481Z	\N
531	2	6	ac1f487c-28db-4d80-93b4-6ff421d51800	6	51	2	13	System	2021-04-21T12:37:43.487Z	System	2021-04-21T12:37:43.487Z	\N
532	2	6	769bc978-9854-4bff-bab7-557ff8d74a38	6	51	2	13	System	2021-04-21T12:37:43.493Z	System	2021-04-21T12:37:43.493Z	\N
533	2	6	14c1790e-23cc-45e3-a000-ba3358a9d4bf	6	51	2	13	System	2021-04-21T12:37:43.499Z	System	2021-04-21T12:37:43.499Z	\N
534	2	6	91a8f4f5-fbb0-40d7-a268-cd7ead6541d8	6	51	2	13	System	2021-04-21T12:37:43.505Z	System	2021-04-21T12:37:43.505Z	\N
535	2	6	4e33198c-f9b7-4b97-a8b3-28e7e4901556	6	51	2	13	System	2021-04-21T12:37:43.511Z	System	2021-04-21T12:37:43.511Z	\N
931	1	6	db222ab8-d7b8-473c-b680-ac9272bbb9c1	51	24	2	11	admin	2021-04-21T12:42:18.923Z	admin	2021-04-21T12:42:18.923Z	\N
536	2	6	d69e5cff-d84e-45ec-8b4e-9bae99d553b0	6	51	2	13	System	2021-04-21T12:37:43.518Z	System	2021-04-21T12:37:43.518Z	\N
932	1	6	f4cc5952-1efd-4537-81f1-a4b639696c72	53	24	2	11	admin	2021-04-21T12:42:19.039Z	admin	2021-04-21T12:42:19.039Z	\N
537	2	6	a3a0c39c-0263-4df1-a4b8-0b9201df258e	6	51	2	13	System	2021-04-21T12:37:43.527Z	System	2021-04-21T12:37:43.527Z	\N
538	2	6	3570396e-cca6-49fb-92ea-267ca4cb2823	6	51	2	13	System	2021-04-21T12:37:43.534Z	System	2021-04-21T12:37:43.534Z	\N
539	2	6	0ada7ece-e5c9-4278-a66f-688e29a7b938	6	51	2	13	System	2021-04-21T12:37:43.543Z	System	2021-04-21T12:37:43.543Z	\N
540	2	6	d8a0f57d-6c81-442f-a0ef-31e257e32912	6	51	2	13	System	2021-04-21T12:37:43.551Z	System	2021-04-21T12:37:43.551Z	\N
936	4	6	39d91419-de43-49d7-ba04-3dda305c21a2	61	51	2	11	User8	2020-05-21T00:07:23Z	admin	2021-04-21T12:42:20.106Z	2020-02-14T12:09:05Z
541	2	6	232ae68d-b05e-4a0a-acf3-a573edc9d2dc	6	51	2	13	System	2021-04-21T12:37:43.558Z	System	2021-04-21T12:37:43.558Z	\N
542	2	6	e0275be5-1790-4861-941c-cb12e6942553	6	51	2	13	System	2021-04-21T12:37:43.564Z	System	2021-04-21T12:37:43.564Z	\N
543	2	6	5776bf7f-9a0e-4480-8d76-0c4aec122af1	6	51	2	13	System	2021-04-21T12:37:43.573Z	System	2021-04-21T12:37:43.573Z	\N
544	2	6	d2fb5180-0ee2-4292-8743-2e23f0cf56ae	6	51	2	13	System	2021-04-21T12:37:43.582Z	System	2021-04-21T12:37:43.582Z	\N
940	4	6	8946269d-4b96-49cf-9947-fa31b21c7f72	68	51	2	11	User10	2020-03-19T20:10:06Z	admin	2021-04-21T12:42:20.473Z	2020-11-19T09:16:44Z
545	2	6	b7b39bc0-5df0-457b-802c-d4d237a6d30b	6	51	2	13	System	2021-04-21T12:37:43.589Z	System	2021-04-21T12:37:43.589Z	\N
546	2	6	8857d063-1269-46ab-80bb-ba590d67afa8	6	51	2	13	System	2021-04-21T12:37:43.596Z	System	2021-04-21T12:37:43.596Z	\N
547	2	6	fda24999-16ec-402a-a449-96d461e63001	6	51	2	13	System	2021-04-21T12:37:43.603Z	System	2021-04-21T12:37:43.603Z	\N
548	2	6	1f29b3e5-1c19-45ef-b044-6503745f83ce	6	51	2	13	System	2021-04-21T12:37:43.611Z	System	2021-04-21T12:37:43.611Z	\N
1018	4	6	5fad54d4-137f-4fe1-b881-1c9f03c1674a	221	51	2	11	User3	2020-10-02T22:04:32Z	admin	2021-04-21T12:42:23.799Z	2020-09-04T13:04:58Z
549	2	6	a28a4638-995f-4584-9082-3b29bc7989c6	6	51	2	13	System	2021-04-21T12:37:43.617Z	System	2021-04-21T12:37:43.617Z	\N
550	2	6	8a3bf730-f7e0-4c34-8937-6238bd6ac892	6	51	2	13	System	2021-04-21T12:37:43.627Z	System	2021-04-21T12:37:43.627Z	\N
551	2	6	edbd959b-54df-4800-8879-3d9724c1e9ef	6	51	2	13	System	2021-04-21T12:37:43.634Z	System	2021-04-21T12:37:43.634Z	\N
552	2	6	3a1ad24d-3ccb-400a-be38-d059c04083ad	6	51	2	13	System	2021-04-21T12:37:43.641Z	System	2021-04-21T12:37:43.641Z	\N
1022	4	6	d023adf5-e662-46a0-b756-60adefee8292	229	51	2	11	User5	2020-03-06T11:58:39Z	admin	2021-04-21T12:42:23.921Z	2020-07-17T02:18:13Z
553	2	6	2ebfe973-60ce-4a13-9987-2aa56ed05e9f	6	51	2	13	System	2021-04-21T12:37:43.648Z	System	2021-04-21T12:37:43.648Z	\N
554	2	6	7206c741-b89f-4c6d-a746-19b2828e663c	6	51	2	13	System	2021-04-21T12:37:43.654Z	System	2021-04-21T12:37:43.654Z	\N
555	2	6	ef824871-740d-43a6-8594-61515d7dcb73	6	51	2	13	System	2021-04-21T12:37:43.661Z	System	2021-04-21T12:37:43.661Z	\N
556	2	6	9cedf0ad-9f87-4662-8ad9-0f72645840a1	6	51	2	13	System	2021-04-21T12:37:43.667Z	System	2021-04-21T12:37:43.667Z	\N
933	1	6	e6787327-2ed6-4e28-a8de-2dd0e2ea03c5	54	24	2	11	admin	2021-04-21T12:42:19.129Z	admin	2021-04-21T12:42:19.129Z	\N
557	2	6	f4befa94-682f-4f93-a159-a3e2b0bedf77	6	51	2	13	System	2021-04-21T12:37:43.674Z	System	2021-04-21T12:37:43.674Z	\N
934	1	6	3955a461-ad47-4e24-9098-e9129f74027e	55	24	2	11	admin	2021-04-21T12:42:19.228Z	admin	2021-04-21T12:42:19.228Z	\N
558	2	6	d81ecf2f-4642-44c3-9af9-fcefe9dcb1a0	6	51	2	13	System	2021-04-21T12:37:43.681Z	System	2021-04-21T12:37:43.681Z	\N
935	1	6	4385ec15-5a8d-4afe-ad83-1c4d19940d65	56	24	2	11	admin	2021-04-21T12:42:19.347Z	admin	2021-04-21T12:42:19.347Z	\N
559	2	6	37cb3087-b337-4308-8dc9-7a260db59687	6	51	2	13	System	2021-04-21T12:37:43.687Z	System	2021-04-21T12:37:43.687Z	\N
560	2	6	dd0f7f4c-0244-4bd4-94cc-51fac6ba52e4	6	51	2	13	System	2021-04-21T12:37:43.694Z	System	2021-04-21T12:37:43.694Z	\N
561	2	6	a3867a07-3c0a-450e-b79d-4ad491c4117b	6	51	2	13	System	2021-04-21T12:37:43.700Z	System	2021-04-21T12:37:43.700Z	\N
562	2	6	346781dd-7c00-4529-ac55-41576b1b263f	6	51	2	13	System	2021-04-21T12:37:43.706Z	System	2021-04-21T12:37:43.706Z	\N
563	2	6	22fc09f7-8d96-4efd-94b7-8b7b9af93db4	6	51	2	13	System	2021-04-21T12:37:43.713Z	System	2021-04-21T12:37:43.713Z	\N
564	2	6	e11cd713-cbf2-48bc-b8ee-3833531a69a2	6	51	2	13	System	2021-04-21T12:37:43.720Z	System	2021-04-21T12:37:43.720Z	\N
565	2	6	651c6c4e-9453-4644-aa86-5f7e48d8a6c1	6	51	2	13	System	2021-04-21T12:37:43.726Z	System	2021-04-21T12:37:43.726Z	\N
566	2	6	03a56228-1f0a-4e4d-a8ee-6689564cb53d	6	51	2	13	System	2021-04-21T12:37:43.733Z	System	2021-04-21T12:37:43.733Z	\N
567	2	6	6e45de10-1591-4f6d-9739-415094714209	6	51	2	13	System	2021-04-21T12:37:43.740Z	System	2021-04-21T12:37:43.740Z	\N
568	2	6	d6f1fd31-6114-44c1-a83b-d3d9a7344695	6	51	2	13	System	2021-04-21T12:37:43.749Z	System	2021-04-21T12:37:43.749Z	\N
569	2	6	8d018a6f-2dd1-4058-8ef0-a72587f8706b	6	51	2	13	System	2021-04-21T12:37:43.755Z	System	2021-04-21T12:37:43.755Z	\N
570	2	6	59b2698b-6455-4cd1-b760-f91adbc1b108	6	51	2	13	System	2021-04-21T12:37:43.762Z	System	2021-04-21T12:37:43.762Z	\N
571	2	6	289234e2-a683-4d85-ac08-3a9b38d529d2	6	51	2	13	System	2021-04-21T12:37:43.772Z	System	2021-04-21T12:37:43.772Z	\N
572	2	6	66713e83-4d37-4442-914b-d8717277c642	6	51	2	13	System	2021-04-21T12:37:43.781Z	System	2021-04-21T12:37:43.781Z	\N
573	2	6	619055e2-1d37-4632-8e94-00f2d8fd949a	6	51	2	13	System	2021-04-21T12:37:43.787Z	System	2021-04-21T12:37:43.787Z	\N
574	2	6	55204976-1d42-44df-8e87-f05930236bf9	6	51	2	13	System	2021-04-21T12:37:43.795Z	System	2021-04-21T12:37:43.795Z	\N
575	2	6	43a03255-3935-468d-a9a0-5e79cc7c337a	6	51	2	13	System	2021-04-21T12:37:43.804Z	System	2021-04-21T12:37:43.804Z	\N
576	2	6	bae31260-16d6-4ce7-8433-d505ae345188	6	51	2	13	System	2021-04-21T12:37:43.812Z	System	2021-04-21T12:37:43.812Z	\N
577	2	6	d8ee9c3f-2e3e-48c4-b0a4-b49f81fb5faf	6	51	2	13	System	2021-04-21T12:37:43.818Z	System	2021-04-21T12:37:43.818Z	\N
578	2	6	924843e2-452d-4fa3-a93b-86f67d584648	6	51	2	13	System	2021-04-21T12:37:43.824Z	System	2021-04-21T12:37:43.824Z	\N
579	2	6	6574382c-c1bf-4dae-9f99-06b0922ecb25	6	51	2	13	System	2021-04-21T12:37:43.830Z	System	2021-04-21T12:37:43.830Z	\N
13	7	6	952b5f79-98b7-4ed7-aa61-81f6ddfb2151	52	24	2	10	System	2021-04-21T12:37:40.510Z	admin	2021-04-21T12:42:18.957Z	\N
580	2	6	5da82a08-1f12-40a4-a1a0-a6240179aa93	6	51	2	13	System	2021-04-21T12:37:43.837Z	System	2021-04-21T12:37:43.837Z	\N
581	2	6	downloads_container	6	3	2	50	\N	\N	\N	\N	\N
582	3	6	ebe122de-89f5-4e63-b9c9-4d1d13a9a253	6	103	2	19	System	2021-04-21T12:37:43.861Z	System	2021-04-21T12:37:43.861Z	\N
395	2	6	ee6d9992-0c99-4c4a-9a94-c8b0ea61762c	6	24	2	13	System	2021-04-21T12:37:41.980Z	System	2021-04-21T12:37:43.929Z	\N
15	2	6	68359e58-b388-4d9b-b9b1-5d0d408eced9	6	24	2	13	System	2021-04-21T12:37:40.606Z	System	2021-04-21T12:37:43.929Z	\N
16	2	6	8054a53a-a7da-41a9-b003-5779fcd1edd9	6	24	2	13	System	2021-04-21T12:37:40.616Z	System	2021-04-21T12:37:43.929Z	\N
17	2	6	f5af76ba-38ed-43b7-bddb-b4056aaf14a4	6	24	2	13	System	2021-04-21T12:37:40.625Z	System	2021-04-21T12:37:43.929Z	\N
19	3	6	899a92cb-7ff4-4081-8aa1-f70698bb485f	6	24	2	14	System	2021-04-21T12:37:40.642Z	System	2021-04-21T12:37:43.929Z	\N
954	4	6	a7a8835f-2d07-4833-87e2-7e2a42041418	92	51	2	11	User5	2020-02-05T11:41:58Z	admin	2021-04-21T12:42:21.406Z	2020-05-22T16:32:08Z
23	3	6	b96768de-e207-42dc-b7fb-0aac3a42cab3	6	24	2	18	System	2021-04-21T12:37:40.707Z	System	2021-04-21T12:37:43.929Z	\N
583	2	6	9abd62e0-918f-4a68-adb5-f7219ab1ef9d	9	51	2	13	System	2021-04-21T12:37:46.882Z	System	2021-04-21T12:37:46.882Z	\N
21	3	6	a2797841-f2cd-492d-8e82-07997a36b314	10	24	2	13	System	2021-04-21T12:37:40.688Z	System	2021-04-21T12:37:46.902Z	\N
585	1	6	ed24ff7e-4979-4da5-a9a0-1921fd678b03	11	89	2	43	\N	\N	\N	\N	\N
586	1	6	e281b35a-9bf5-47d7-8668-684962e9ae44	11	91	2	45	admin	2021-04-21T12:37:46.972Z	admin	2021-04-21T12:37:46.972Z	\N
587	1	6	61454db7-e5e3-4db7-a417-053b83f80794	11	89	2	43	\N	\N	\N	\N	\N
588	1	6	3f6bcf11-4dea-4323-be37-494c0536b483	11	89	2	43	\N	\N	\N	\N	\N
589	1	6	a8845547-238a-4fa6-8253-11565de52fad	11	89	2	43	\N	\N	\N	\N	\N
590	1	6	340fe6f4-30a1-448a-a9ba-4214e7075400	11	89	2	43	\N	\N	\N	\N	\N
591	1	1	eedbc083-548e-4a25-a54d-45671ebd54d0	11	5	2	2	\N	\N	\N	\N	\N
592	1	1	7b6c2624-aec7-479c-a754-d6047fb1a225	11	5	2	2	\N	\N	\N	\N	\N
957	4	6	712a0381-bb40-45ee-bf31-87af81af48f5	99	51	2	11	User6	2020-11-16T02:06:26Z	admin	2021-04-21T12:42:21.645Z	2020-05-26T02:40:22Z
593	2	6	dc103838-645f-43c1-8a2a-bc187e13c343	11	35	2	54	\N	\N	\N	\N	\N
964	4	6	cbfa04b1-ada2-49fe-90a8-8c920ba5f803	114	51	2	11	User9	2020-01-15T01:34:28Z	admin	2021-04-21T12:42:21.894Z	2020-09-24T20:08:32Z
594	5	6	d65a4795-578e-4780-9f27-96ce43bde700	11	51	2	55	abeecher	2015-09-29T10:45:15.729Z	abeecher	2015-09-29T10:45:15.729Z	\N
595	5	6	198500fc-1e99-4f5f-8926-248cea433366	11	141	2	55	abeecher	2015-09-29T10:45:16.111Z	abeecher	2015-09-29T10:45:16.111Z	\N
596	2	6	b6d80d49-21cc-4f04-9c92-e7063037543f	11	35	2	56	\N	\N	\N	\N	\N
14	4	6	9497be8f-e858-44ec-9b48-42622dda68c9	18	24	2	12	System	2021-04-21T12:37:40.574Z	System	2021-04-21T12:37:50.513Z	\N
597	5	6	42881f63-38cf-479d-a303-52e7ff99cb75	11	51	2	57	mjackson	2015-09-29T10:44:47.877Z	mjackson	2015-09-29T10:44:47.877Z	\N
942	4	6	be706839-61a8-467c-8e83-0abb829b0db7	67	51	2	11	User4	2020-05-09T05:32:09Z	admin	2021-04-21T12:42:20.472Z	2020-05-09T15:23:20Z
598	5	6	3fbde500-298b-4e80-ae50-e65a5cbc2c4d	11	141	2	57	mjackson	2015-09-29T10:44:48.322Z	mjackson	2015-09-29T10:44:48.322Z	\N
599	1	6	128babce-4b45-4eca-9d4b-8d6208aeddd9	11	148	1	\N	\N	\N	\N	\N	\N
944	4	6	39c6f263-8561-481f-acb5-75b75a11dc71	74	51	2	11	User1	2020-03-13T17:59:09Z	admin	2021-04-21T12:42:20.688Z	2020-02-11T01:54:27Z
600	5	6	b4cff62a-664d-4d45-9302-98723eac1319	11	106	4	58	mjackson	2011-02-15T20:16:27.080Z	mjackson	2011-02-15T20:16:27.080Z	\N
948	4	6	db07ceb5-a63d-4a68-b31a-0646ed29b051	82	51	2	11	User6	2020-12-31T05:40:56Z	admin	2021-04-21T12:42:20.802Z	2020-02-22T04:54:29Z
601	4	6	8f2105b4-daaf-4874-9e8a-2152569d109b	11	24	4	59	mjackson	2011-02-15T20:16:28.292Z	mjackson	2011-02-15T20:16:28.292Z	\N
952	4	6	6f45bc7e-3bb2-4543-86ab-91ec0064de09	90	51	2	11	User6	2020-08-11T18:49:28Z	admin	2021-04-21T12:42:21.288Z	2020-05-22T18:42:31Z
603	3	6	e0856836-ed5e-4eee-b8e5-bd7e8fb9384c	11	24	4	59	mjackson	2011-02-15T21:01:29.482Z	mjackson	2011-02-15T21:01:29.482Z	\N
956	4	6	fbbcaf38-4cad-4ca9-8ae1-dd0ffab32492	98	51	2	11	User5	2020-12-27T02:35:29Z	admin	2021-04-21T12:42:21.637Z	2020-08-09T14:49:04Z
960	4	6	ef444d3a-b39a-481a-9e10-c1d4b73fa6e6	107	51	2	11	User4	2020-10-14T20:12:39Z	admin	2021-04-21T12:42:21.766Z	2020-06-28T01:57:05Z
605	1	4	a977243d-4545-40bf-b01c-69c5bbf03b30	11	164	2	60	System	2021-04-21T12:37:47.596Z	System	2021-04-21T12:37:47.596Z	\N
606	2	4	cdb8ae73-fb43-439a-b561-9c5b405c803b	11	51	4	60	System	2021-04-21T12:37:47.604Z	System	2021-04-21T12:37:47.604Z	\N
604	9	6	1a0b110f-1e09-4ca2-b367-fe25e4964a4e	11	51	4	59	abeecher	2011-02-15T21:26:54.600Z	admin	2011-06-14T10:28:54.714Z	\N
977	4	6	2f1fd845-e6ef-41bc-86f5-c164fe7482cb	139	51	2	11	User10	2020-06-30T12:29:51Z	admin	2021-04-21T12:42:22.367Z	2020-10-14T23:38:29Z
607	5	6	d27ef7b9-7f2f-41c3-8069-cdd48c8714a1	11	141	4	59	abeecher	2011-02-15T21:26:55.401Z	abeecher	2011-02-16T10:34:06.014Z	\N
941	4	6	54e5b058-a241-493b-8fa8-4e23e741431d	66	51	2	11	User5	2020-12-28T18:17:26Z	admin	2021-04-21T12:42:20.472Z	2020-10-08T05:16:22Z
608	5	6	3f66a51e-580c-4c67-b183-a5d73cdbdd78	11	141	4	59	mjackson	2011-02-16T10:13:43.173Z	abeecher	2011-02-16T10:34:06.012Z	\N
609	3	6	880a0f47-31b1-4101-b20b-4d325e54e8b1	11	24	4	59	mjackson	2011-02-15T21:04:41.894Z	mjackson	2011-02-15T21:04:41.894Z	\N
950	4	6	237cae7a-f7d8-4ba4-adf6-42eb77e3974c	83	51	2	11	User1	2020-06-19T15:14:03Z	admin	2021-04-21T12:42:20.895Z	2020-06-19T07:11:24Z
610	6	6	7bb9c846-fcc5-43b5-a893-39e46ebe94d4	11	51	4	59	abeecher	2011-03-03T10:34:52.092Z	abeecher	2011-03-03T10:34:52.092Z	\N
953	4	6	b36fc57a-cd99-493d-bff9-89c57ec7bcd2	94	51	2	11	User10	2020-10-04T02:25:34Z	admin	2021-04-21T12:42:21.415Z	2020-07-01T04:24:53Z
611	5	6	7e72bcb1-23e3-4d38-a61b-02f8cbe7b8c9	11	141	4	59	abeecher	2011-03-03T10:34:58.796Z	abeecher	2011-03-03T10:34:58.796Z	\N
969	4	6	7d9d37c5-113e-452f-92f7-e22a137277e0	123	51	2	11	User1	2020-07-27T13:33:13Z	admin	2021-04-21T12:42:22.070Z	2020-03-01T00:32:18Z
973	4	6	58913208-65a8-42e3-b3a2-753ded070c18	131	51	2	11	User6	2020-10-05T12:16:16Z	admin	2021-04-21T12:42:22.182Z	2020-03-08T05:33:57Z
612	6	6	74cd8a96-8a21-47e5-9b3b-a1b3e296787d	11	51	4	59	abeecher	2011-03-03T10:34:52.102Z	abeecher	2011-03-03T10:34:52.102Z	\N
979	4	6	941c4468-8cc5-4475-950b-ccc5df673264	142	51	2	11	User3	2020-07-03T17:07:24Z	admin	2021-04-21T12:42:22.400Z	2020-03-09T22:20:59Z
613	5	6	ac5c46db-719c-40d3-aa0c-e51b23fe5c32	11	141	4	59	abeecher	2011-03-03T10:34:58.758Z	abeecher	2011-03-03T10:34:58.758Z	\N
1002	4	6	c94e502c-dc3c-4883-92b6-2c4bbc773212	188	51	2	11	User2	2020-06-11T19:20:54Z	admin	2021-04-21T12:42:23.265Z	2020-06-29T07:33:45Z
614	6	6	80a94ac8-3ece-47ad-864e-5d939424c47c	11	51	4	59	abeecher	2011-03-03T10:34:52.139Z	abeecher	2011-03-03T10:34:52.139Z	\N
615	5	6	242ddf76-d864-4db3-8705-b012c4a48e51	11	141	4	59	abeecher	2011-03-03T10:34:59.473Z	abeecher	2011-03-03T10:34:59.473Z	\N
943	4	6	06c3db55-41c8-4bc7-a587-de7139041807	69	51	2	11	User8	2020-06-19T08:30:24Z	admin	2021-04-21T12:42:20.493Z	2020-07-16T19:22:41Z
616	5	6	f2f3dc4b-7ced-49fc-8e85-ce53a459dd87	11	141	4	59	mjackson	2011-03-03T11:00:15.197Z	mjackson	2011-03-03T11:00:15.197Z	\N
949	4	6	a9b67a5d-07c9-427f-aa8b-1e358f745625	85	51	2	11	User4	2020-06-21T16:46:40Z	admin	2021-04-21T12:42:20.946Z	2020-09-16T07:45:03Z
617	6	6	267839b2-f466-42c5-9a35-cb3e41281bb9	11	51	4	59	abeecher	2011-03-03T10:34:52.620Z	abeecher	2011-03-03T10:34:52.620Z	\N
955	4	6	ebe142d4-bed7-4b84-ba5c-8680a5c82030	91	51	2	11	User5	2020-07-11T17:34:05Z	admin	2021-04-21T12:42:21.363Z	2020-11-11T09:29:02Z
618	5	6	9a7a891a-0db3-49dd-bd8d-8340f9c1cee8	11	141	4	59	abeecher	2011-03-03T10:35:00.221Z	abeecher	2011-03-03T10:35:00.221Z	\N
961	4	6	dbdf48fe-58f8-4e00-8804-575a82d6623a	106	51	2	11	User7	2020-08-26T12:57:26Z	admin	2021-04-21T12:42:21.766Z	2020-12-22T15:38:36Z
1045	4	6	ac9ce519-a593-4849-ab60-0318d7016039	275	51	2	11	User10	2020-06-19T00:43:07Z	admin	2021-04-21T12:42:26.120Z	2020-09-10T12:35:01Z
619	6	6	1f4ce811-1c61-4553-ac23-63b68bf1d121	11	51	4	59	abeecher	2011-03-03T10:34:52.623Z	abeecher	2011-03-03T10:34:52.623Z	\N
1050	4	6	6431ab7f-5150-4439-a8ed-b40ebe5969e3	283	51	2	11	User9	2020-12-17T07:15:34Z	admin	2021-04-21T12:42:26.359Z	2020-05-11T01:40:01Z
620	5	6	d20da3de-d4cd-4e28-892c-261e4c3b2669	11	141	4	59	abeecher	2011-03-03T10:35:00.614Z	abeecher	2011-03-03T10:35:00.614Z	\N
1053	4	6	003a7c5e-78ee-4b08-9ca7-f9e8859f9971	290	51	2	11	User7	2020-12-03T07:28:48Z	admin	2021-04-21T12:42:26.782Z	2020-06-18T19:02:42Z
621	6	6	0516a5cc-fc04-4512-a4ed-b595b7c3908b	11	51	4	59	abeecher	2011-03-03T10:34:52.784Z	abeecher	2011-03-03T10:34:52.784Z	\N
1055	4	6	60bcdb81-1a67-4812-802c-04c79efcc949	298	51	2	11	User10	2020-08-17T02:19:10Z	admin	2021-04-21T12:42:27.060Z	2020-11-07T03:43:47Z
1060	4	6	b7377961-ba74-49ae-b44f-c96463a1d488	304	51	2	11	User1	2020-10-04T11:15:36Z	admin	2021-04-21T12:42:27.188Z	2020-05-14T19:33:53Z
1063	4	6	94fcf400-fc10-407e-9745-184419b16f52	314	51	2	11	User8	2020-09-12T22:35:04Z	admin	2021-04-21T12:42:27.338Z	2020-05-27T00:56:50Z
1069	4	6	b8a20ef0-b0fb-44bf-b12a-07550610d518	322	51	2	11	User10	2020-07-11T15:51:03Z	admin	2021-04-21T12:42:27.705Z	2020-10-09T20:19:44Z
1072	4	6	287ee273-5922-41f9-9824-a457a56b4686	331	51	2	11	User3	2020-08-01T21:40:32Z	admin	2021-04-21T12:42:28.153Z	2020-08-02T20:11:45Z
1094	4	6	b5267a7d-084d-4deb-b9f1-e928160b4da5	370	51	2	11	User2	2020-12-13T11:44:36Z	admin	2021-04-21T12:42:28.873Z	2020-09-22T15:01:28Z
1112	4	6	5a30eea4-42c3-4cf2-bb7c-970dce3d4113	409	51	2	11	User1	2020-05-04T22:14:34Z	admin	2021-04-21T12:42:29.568Z	2020-01-05T09:45:22Z
1116	4	6	c3b48bc9-ec7c-469b-bc73-998ec3696c9d	417	51	2	11	User8	2020-12-21T05:06:02Z	admin	2021-04-21T12:42:29.690Z	2020-08-10T22:50:32Z
622	5	6	38db832f-8279-460f-99b8-fed560c8da8e	11	141	4	59	abeecher	2011-03-03T10:35:00.838Z	abeecher	2011-03-03T10:35:00.838Z	\N
945	4	6	51bfd7ec-9637-4c64-b380-0037e9848061	76	51	2	11	User6	2020-10-18T15:01:14Z	admin	2021-04-21T12:42:20.702Z	2020-08-23T23:02:33Z
623	6	6	0f672fb8-bbdb-41bb-84f3-7b9bb1c39b30	11	51	4	59	abeecher	2011-03-03T10:34:53.458Z	abeecher	2011-03-03T10:34:53.458Z	\N
959	4	6	671ed2ff-b1c0-4301-b1b2-39b6e3f767f0	103	51	2	11	User1	2020-02-04T18:10:04Z	admin	2021-04-21T12:42:21.687Z	2020-11-12T18:36:17Z
624	5	6	900ebb9a-2596-40e5-ab51-add0bfdf751f	11	141	4	59	abeecher	2011-03-03T10:35:01.920Z	abeecher	2011-03-03T10:35:01.920Z	\N
965	4	6	36dc947d-22e6-474b-9e83-7305f511788d	115	51	2	11	User6	2020-08-19T08:37:16Z	admin	2021-04-21T12:42:21.916Z	2020-06-18T23:53:07Z
625	6	6	14e2200e-9f1c-4274-8b6b-95dc9d59d204	11	51	4	59	abeecher	2011-03-03T10:34:53.482Z	abeecher	2011-03-03T10:34:53.482Z	\N
982	4	6	8e70e487-492c-45b2-ae8c-abc726eafa42	149	51	2	11	User3	2020-10-17T20:51:04Z	admin	2021-04-21T12:42:22.530Z	2020-02-04T18:35:42Z
626	5	6	a6bd3f86-99d3-4cde-b945-887fc3255859	11	141	4	59	abeecher	2011-03-03T10:35:01.509Z	abeecher	2011-03-03T10:35:01.509Z	\N
986	4	6	db232fec-05dd-4403-a7fd-1240f4434fa2	157	51	2	11	User3	2020-08-15T11:45:10Z	admin	2021-04-21T12:42:22.692Z	2020-06-17T02:36:14Z
627	6	6	052539b7-872d-46cc-a7f1-1e0757ed4a5b	11	51	4	59	abeecher	2011-03-03T10:34:53.551Z	abeecher	2011-03-03T10:34:53.551Z	\N
990	4	6	2e0bb37e-7239-4c6c-ab22-d62b786fbd02	165	51	2	11	User4	2020-08-15T08:24:40Z	admin	2021-04-21T12:42:22.855Z	2020-04-05T11:33Z
628	5	6	0cbca5ac-84f9-4d07-a2fe-c54c911eebb5	11	141	4	59	abeecher	2011-03-03T10:34:59.319Z	abeecher	2011-03-03T10:34:59.319Z	\N
994	4	6	8114128c-13cb-4174-ac59-12b7f98b41cc	172	51	2	11	User8	2020-04-03T03:16:20Z	admin	2021-04-21T12:42:22.972Z	2020-05-21T00:47:04Z
629	6	6	edcbdf18-36ac-4602-ac3d-79dd7aab7ae4	11	51	4	59	abeecher	2011-03-03T10:34:53.892Z	abeecher	2011-03-03T10:34:53.892Z	\N
946	4	6	60da7b8d-88cf-4fee-abe7-9183f9261aff	77	51	2	11	User2	2020-12-16T14:07:58Z	admin	2021-04-21T12:42:20.708Z	2020-06-02T16:23:18Z
630	5	6	bf581ca9-e270-413d-9796-635544674781	11	141	4	59	abeecher	2011-03-03T10:35:01.313Z	abeecher	2011-03-03T10:35:01.313Z	\N
951	4	6	b2ebedb0-9939-4ba7-863f-150481d2ac15	84	51	2	11	User7	2020-07-17T18:44:53Z	admin	2021-04-21T12:42:20.943Z	2020-12-16T03:52:37Z
631	6	6	72948f84-4bf1-4ec5-8378-1bed0951600a	11	51	4	59	abeecher	2011-03-03T10:34:53.982Z	abeecher	2011-03-03T10:34:53.982Z	\N
963	4	6	c6f31642-1fd0-41e6-a122-65d49506662f	111	51	2	11	User2	2020-03-25T01:32:24Z	admin	2021-04-21T12:42:21.809Z	2020-04-20T13:09:13Z
632	5	6	82ee74ea-a596-4d62-b689-ddfe0f7e8c16	11	141	4	59	abeecher	2011-03-03T10:34:59.646Z	abeecher	2011-03-03T10:34:59.646Z	\N
633	3	6	b1a98357-4f7a-470d-bf4c-327501158689	11	24	4	59	mjackson	2011-02-15T21:05:49.308Z	mjackson	2011-02-15T21:05:49.308Z	\N
967	4	6	544d5105-669e-41db-89ce-34131c9f39bc	119	51	2	11	User6	2020-12-23T07:51:05Z	admin	2021-04-21T12:42:21.958Z	2020-06-30T08:25:35Z
971	4	6	cd65e601-2b12-4b26-a0ab-ee4e296a25fe	127	51	2	11	User3	2020-04-18T11:00:21Z	admin	2021-04-21T12:42:22.107Z	2020-10-11T23:19:19Z
634	7	6	79a03a3e-a027-4b91-9f14-02b62723591e	11	51	4	59	abeecher	2011-03-03T10:36:45.396Z	abeecher	2011-03-03T10:36:45.396Z	\N
975	4	6	05f46bae-f9bb-4f58-9af3-f2ada7475bd6	135	51	2	11	User8	2020-05-02T23:38:33Z	admin	2021-04-21T12:42:22.227Z	2020-02-07T23:50:47Z
635	5	6	e6758089-d817-4bd8-b328-7c2c693c8cf1	11	141	4	59	abeecher	2011-03-03T10:36:48.359Z	abeecher	2011-03-03T10:36:48.359Z	\N
978	4	6	ab9dbbec-d19d-4a28-91f0-4f9839fdd383	141	51	2	11	User6	2020-12-22T23:14:25Z	admin	2021-04-21T12:42:22.401Z	2020-12-24T14:15:03Z
636	7	6	3deb5413-2c1d-4015-b9c9-2be9648446bc	11	51	4	59	abeecher	2011-03-03T10:36:45.431Z	abeecher	2011-03-03T10:36:45.431Z	\N
637	5	6	e751b9c9-de51-4eac-8b5c-905afbeb667a	11	141	4	59	abeecher	2011-03-03T10:36:48.376Z	abeecher	2011-03-03T10:36:48.376Z	\N
947	4	6	c5454162-91cc-4eb9-b618-975b63d8e651	75	51	2	11	User7	2020-01-12T04:36:05Z	admin	2021-04-21T12:42:20.699Z	2020-05-22T23:27Z
638	3	6	610771be-4d82-479a-a2d7-796adf498084	11	24	4	59	mjackson	2011-02-15T21:14:44.396Z	mjackson	2011-02-15T21:14:44.396Z	\N
958	4	6	49ba212f-f48d-40fa-86a9-f57f8ffa2930	100	51	2	11	User9	2020-02-27T17:57:33Z	admin	2021-04-21T12:42:21.673Z	2020-04-26T17:01:37Z
639	6	6	43485b48-2ca7-4077-a00c-9bfe810f9fa1	11	51	4	59	abeecher	2011-03-03T10:37:17.994Z	abeecher	2011-03-03T10:37:17.994Z	\N
962	4	6	aeb6f325-32ee-47fa-95d3-dea29973d6ef	108	51	2	11	User6	2020-11-25T21:56:16Z	admin	2021-04-21T12:42:21.802Z	2020-01-15T08:02:36Z
640	5	6	ad46dd6b-51b3-4bc5-bab8-4b9d4e64f07f	11	141	4	59	abeecher	2011-03-03T10:37:20.937Z	abeecher	2011-03-03T10:37:20.937Z	\N
966	4	6	4db923e4-aeac-4440-a0ad-eaa247fc7233	116	51	2	11	User9	2020-11-06T19:35:22Z	admin	2021-04-21T12:42:21.941Z	2020-10-28T21:29:12Z
641	6	6	4d4a272d-60d5-4810-8164-4a1e595d92f2	11	51	4	59	abeecher	2011-03-03T10:37:18.155Z	abeecher	2011-03-03T10:37:18.155Z	\N
968	4	6	86a578a2-9f88-4deb-bfde-9a86276644c6	122	51	2	11	User1	2020-03-29T08:36:19Z	admin	2021-04-21T12:42:22.018Z	2020-06-05T07:03:02Z
642	5	6	8aa76706-4a23-468a-b7b6-4ae8907d629d	11	141	4	59	abeecher	2011-03-03T10:37:20.795Z	abeecher	2011-03-03T10:37:20.795Z	\N
972	4	6	56fb577f-2cbf-47da-8938-d6610c669df5	130	51	2	11	User2	2020-05-25T23:31:59Z	admin	2021-04-21T12:42:22.145Z	2020-10-22T01:20:22Z
643	5	6	582260e9-672c-4aff-a970-8129650adb72	11	141	4	59	mjackson	2011-03-03T11:00:01.795Z	mjackson	2011-03-03T11:00:01.795Z	\N
976	4	6	422abd90-606d-44a4-8d84-245aca46c334	138	51	2	11	User10	2020-09-04T01:20:08Z	admin	2021-04-21T12:42:22.286Z	2020-07-29T06:52:18Z
644	7	6	7d90c94c-fcf7-4f79-9273-bd1352bbb612	11	51	4	59	abeecher	2011-03-03T10:37:18.155Z	abeecher	2011-03-03T10:37:18.155Z	\N
970	4	6	16948617-e970-4d7a-b439-1971de1ba568	124	51	2	11	User3	2020-02-18T14:29:19Z	admin	2021-04-21T12:42:22.087Z	2020-11-14T06:28:45Z
645	5	6	285b85d9-bcfe-4315-b92a-4cb5b8988871	11	141	4	59	abeecher	2011-03-03T10:37:21.297Z	abeecher	2011-03-03T10:37:21.297Z	\N
974	4	6	f4fe97e2-1e82-4815-a3c4-8113fc6dae69	133	51	2	11	User6	2020-04-24T19:39:43Z	admin	2021-04-21T12:42:22.212Z	2020-08-14T16:47:31Z
646	5	6	9c52032a-576f-4ab3-8620-7598f67592bd	11	141	4	59	mjackson	2011-03-03T10:57:02.996Z	mjackson	2011-03-03T10:57:02.996Z	\N
993	4	6	b5f7332b-001f-41bb-ade3-874a10aedfb8	173	51	2	11	User4	2020-06-01T03:46:29Z	admin	2021-04-21T12:42:22.973Z	2020-06-02T07:48:20Z
647	3	6	1d26e465-dea3-42f3-b415-faa8364b9692	11	24	4	59	abeecher	2011-03-08T10:34:50.822Z	abeecher	2011-03-08T10:34:50.822Z	\N
997	4	6	c6fd67db-a856-4758-9ed2-798673d8f9a2	180	51	2	11	User6	2020-06-07T12:13:37Z	admin	2021-04-21T12:42:23.119Z	2020-11-12T20:30:38Z
1010	4	6	92edbc1b-6bc2-4e4a-b49c-e55e4e5efb7a	204	51	2	11	User3	2020-06-08T08:43Z	admin	2021-04-21T12:42:23.505Z	2020-04-16T20:48:51Z
1013	4	6	c158682d-1fa8-4a03-a1ef-c700e9cd65b6	213	51	2	11	User10	2020-11-13T16:12:36Z	admin	2021-04-21T12:42:23.665Z	2020-09-23T14:58:04Z
650	3	6	d56afdc3-0174-4f8c-bce8-977cafd712ab	11	24	4	59	mjackson	2011-02-15T21:12:14.908Z	mjackson	2011-02-15T21:12:14.908Z	\N
1031	4	6	198804c0-a22e-4ca2-b08a-e0180014d10b	246	51	2	11	User6	2020-01-23T12:42:20Z	admin	2021-04-21T12:42:24.198Z	2020-11-18T17:14:02Z
651	6	6	723a0cff-3fce-495d-baa3-a3cd245ea5dc	11	51	4	59	mjackson	2011-03-03T10:33:35.274Z	mjackson	2011-03-03T10:33:35.274Z	\N
1043	4	6	93124e0d-94b9-442d-ad44-31df381c621f	272	51	2	11	User2	2020-05-11T04:19:03Z	admin	2021-04-21T12:42:26.089Z	2020-08-11T03:13:08Z
652	5	6	eb9d0356-9f9e-4503-a9f0-0ed4f0075ca7	11	141	4	59	mjackson	2011-03-03T10:33:39.687Z	mjackson	2011-03-03T10:33:39.687Z	\N
1047	4	6	7fb1a4b0-1f33-45e0-ade1-1155a3a3baec	280	51	2	11	User2	2020-03-20T16:29:12Z	admin	2021-04-21T12:42:26.229Z	2020-09-07T09:04:42Z
1051	4	6	3176856d-563e-4113-9ea3-1cab9a13c832	289	51	2	11	User5	2020-11-25T07:25:26Z	admin	2021-04-21T12:42:26.728Z	2020-09-08T09:23:43Z
1058	4	6	2f071d6e-9a3d-4321-9452-1076a2098b2b	297	51	2	11	User8	2020-01-28T05:37:18Z	admin	2021-04-21T12:42:27.059Z	2020-03-17T23:22:29Z
1061	4	6	bd206e58-19dc-4cac-a660-4b995b49720a	306	51	2	11	User6	2020-09-05T12:48:11Z	admin	2021-04-21T12:42:27.199Z	2020-05-29T15:23:32Z
1067	4	6	aeef5a5a-037d-4940-9391-9af40b8c90f3	321	51	2	11	User10	2020-09-26T03:10:11Z	admin	2021-04-21T12:42:27.709Z	2020-02-06T00:12:38Z
1082	4	6	91097085-0b2e-4bdf-8d58-ff8a8eca89d3	344	51	2	11	User1	2020-10-09T05:04:11Z	admin	2021-04-21T12:42:28.448Z	2020-05-03T04:36:06Z
1083	4	6	228d1f8e-e194-4f07-83eb-7c8ba84720cd	352	51	2	11	User9	2020-01-25T15:46:57Z	admin	2021-04-21T12:42:28.568Z	2020-03-24T08:43:46Z
1087	4	6	227ce347-3b29-4d6a-b122-b977932f2b36	362	51	2	11	User7	2020-03-28T00:24:37Z	admin	2021-04-21T12:42:28.731Z	2020-10-10T08:01:50Z
1092	4	6	86833269-d958-4664-b55d-4f4ff0356b28	371	51	2	11	User3	2020-01-31T09:43:54Z	admin	2021-04-21T12:42:28.873Z	2020-03-07T11:12:22Z
1096	4	6	cef422f2-d2c6-405d-a226-af224f73ff7f	378	51	2	11	User6	2020-05-16T05:33:17Z	admin	2021-04-21T12:42:29.010Z	2020-11-24T04:54:06Z
1102	4	6	50ac9a08-2e68-418d-b554-00ceb894aa85	385	51	2	11	User10	2020-09-25T02:56:55Z	admin	2021-04-21T12:42:29.152Z	2020-08-04T08:31:22Z
1103	4	6	454c5075-4eab-454c-a93b-c7bf8a2fbaf6	394	51	2	11	User2	2020-05-27T22:27:45Z	admin	2021-04-21T12:42:29.269Z	2020-07-10T12:13:55Z
1107	4	6	af45ce53-1fa9-43af-a5c8-f585c2648193	400	51	2	11	User4	2020-04-11T16:58:57Z	admin	2021-04-21T12:42:29.390Z	2020-08-21T11:36:38Z
1111	4	6	de3c7de3-089e-43ad-a4b7-1a455dd3af39	410	51	2	11	User3	2020-04-21T01:12:39Z	admin	2021-04-21T12:42:29.568Z	2020-03-18T18:59Z
1121	4	6	3a3c7e1e-374c-4933-a28d-5d73e31e4308	427	51	2	11	User4	2020-01-15T01:16:26Z	admin	2021-04-21T12:42:29.824Z	2020-11-06T14:22:18Z
653	6	6	7bb7bfa8-997e-4c55-8bd9-2e5029653bc8	11	51	4	59	mjackson	2011-03-03T10:33:35.301Z	mjackson	2011-03-03T10:33:35.301Z	\N
980	4	6	7d2f9da5-8a86-4249-8b8b-c4252220be92	146	51	2	11	User7	2020-09-29T15:52:31Z	admin	2021-04-21T12:42:22.467Z	2020-07-12T05:24:02Z
654	5	6	234e1322-d8c5-4d06-8690-7a96dbb9e914	11	141	4	59	mjackson	2011-03-03T10:33:39.695Z	mjackson	2011-03-03T10:33:39.695Z	\N
984	4	6	13c8db77-69c5-4441-8f24-fbb16ba41a4a	154	51	2	11	User1	2020-10-31T17:08:59Z	admin	2021-04-21T12:42:22.598Z	2020-02-07T18:11:13Z
988	4	6	ca8a5de9-9279-41ba-88bf-29e4dbafeb08	162	51	2	11	User3	2020-10-01T07:06:50Z	admin	2021-04-21T12:42:22.743Z	2020-07-06T03:00:49Z
655	6	6	5fa74ad3-9b5b-461b-9df5-de407f1f4fe7	11	51	4	59	mjackson	2011-02-15T21:35:26.467Z	mjackson	2011-02-15T21:35:26.467Z	\N
992	4	6	401f4764-84f3-423e-80e3-0ded7de9f5e8	169	51	2	11	User5	2020-10-24T13:17:34Z	admin	2021-04-21T12:42:22.900Z	2020-07-01T01:58:14Z
656	5	6	09d1a4bf-d8f2-4de6-913c-934205a3e910	11	141	4	59	mjackson	2011-02-16T10:17:05.469Z	mjackson	2011-02-16T10:17:05.469Z	\N
996	4	6	7898a1fc-a05a-4863-8ccf-b388b3a94dbe	174	51	2	11	User8	2020-03-20T01:08:48Z	admin	2021-04-21T12:42:23.032Z	2020-05-08T15:16:25Z
657	5	6	1d74ba8f-b858-4e72-83fd-c1433ef2b1b7	11	141	4	59	mjackson	2011-02-16T10:17:06.658Z	mjackson	2011-02-16T10:17:06.658Z	\N
658	3	6	18a9bfef-81ca-4cf1-9dae-07eef1c5b175	11	195	4	59	mjackson	2011-02-16T10:30:10.642Z	mjackson	2011-02-16T10:30:10.642Z	\N
999	4	6	997e5c16-8ebc-4545-9160-e55e5883ab28	182	51	2	11	User8	2020-07-01T02:23:18Z	admin	2021-04-21T12:42:23.163Z	2020-05-20T23:23:56Z
659	3	6	b7ba359a-b3df-4c0a-a763-df0b8b19a737	11	197	4	59	mjackson	2011-02-16T10:30:10.656Z	mjackson	2011-02-16T10:30:10.656Z	\N
660	4	6	86796712-4dc6-4b8d-973f-a943ef7f23ed	11	198	4	59	mjackson	2011-02-16T10:30:10.663Z	mjackson	2011-02-16T10:30:10.663Z	\N
1003	4	6	b0406411-1c32-4fec-ad0c-7b874b9b1f03	190	51	2	11	User6	2020-03-28T12:38:40Z	admin	2021-04-21T12:42:23.298Z	2020-01-23T09:07:56Z
661	3	6	a211774d-ba6d-4a35-b97f-dacfaac7bde3	11	24	4	59	mjackson	2011-02-15T21:16:26.500Z	mjackson	2011-02-15T21:16:26.500Z	\N
662	5	6	f3bb5d08-9fd1-46da-a94a-97f20f1ef208	11	51	4	59	mjackson	2011-02-24T16:16:37.286Z	mjackson	2011-02-24T16:16:37.286Z	\N
981	4	6	af11cf20-366d-4e15-bad3-22c58cffd7d9	147	51	2	11	User2	2020-08-06T14:40:42Z	admin	2021-04-21T12:42:22.510Z	2020-11-12T01:24:14Z
663	5	6	cbff1e97-dc79-41a9-adf0-09a2f697759e	11	141	4	59	mjackson	2011-02-24T16:16:44.342Z	mjackson	2011-02-24T16:16:44.342Z	\N
985	4	6	a6509917-80ef-4d47-8edc-4caddc10c883	156	51	2	11	User6	2020-07-02T05:26:42Z	admin	2021-04-21T12:42:22.644Z	2020-04-25T07:20:22Z
664	5	6	a2361514-eec8-4a7f-a16a-bed654b097b2	11	141	4	59	mjackson	2011-02-24T16:16:47.280Z	mjackson	2011-02-24T16:16:47.280Z	\N
989	4	6	091a78dc-b06d-4c73-acc9-d694d9f71edf	164	51	2	11	User8	2020-03-08T08:46:09Z	admin	2021-04-21T12:42:22.805Z	2020-02-24T21:17:10Z
665	5	6	150398b3-7f82-4cf6-af63-c450ef6c5eb8	11	51	4	59	mjackson	2011-02-24T16:16:37.300Z	mjackson	2011-02-24T16:16:37.300Z	\N
1001	4	6	35a9c8a8-10ab-4f97-a32e-403e1d623c61	189	51	2	11	User5	2020-03-16T16:48:40Z	admin	2021-04-21T12:42:23.264Z	2020-03-12T19:44:09Z
666	5	6	e54bccbb-6b04-449b-a9e6-830882d9b978	11	141	4	59	mjackson	2011-02-24T16:16:43.674Z	mjackson	2011-02-24T16:16:43.674Z	\N
1006	4	6	e08e0438-deaa-4a6c-989e-fe39ee3fc1e4	196	51	2	11	User2	2020-08-26T00:41:12Z	admin	2021-04-21T12:42:23.388Z	2020-12-08T13:10:50Z
667	5	6	59b60ea3-5fa8-440c-a846-8222d62e07a3	11	141	4	59	mjackson	2011-02-24T17:16:04.464Z	mjackson	2011-02-24T17:16:04.464Z	\N
1034	4	6	1c1da09e-b04e-44b2-9be6-8f96509c1ce3	253	51	2	11	User1	2020-10-15T14:18:02Z	admin	2021-04-21T12:42:24.315Z	2020-09-28T10:10:01Z
668	5	6	a8290263-4178-48f5-a0b0-be155a424828	11	51	4	59	mjackson	2011-02-24T16:16:37.332Z	mjackson	2011-02-24T16:16:37.332Z	\N
1073	4	6	b373f04f-4774-4964-bd8f-3c304b1a8b03	329	51	2	11	User1	2020-07-28T03:44:47Z	admin	2021-04-21T12:42:28.140Z	2020-10-24T20:01:58Z
669	5	6	9b8ce6d7-67eb-4918-ae0b-f5f9f0533c0b	11	141	4	59	mjackson	2011-02-24T16:16:45.489Z	mjackson	2011-02-24T16:16:45.489Z	\N
1088	4	6	22e77f0d-cac6-484b-b7a0-9e595ca017c9	360	51	2	11	User6	2020-04-26T16:51Z	admin	2021-04-21T12:42:28.729Z	2020-07-10T12:52:13Z
1091	4	6	daa30886-a8f5-4b1a-9062-dd8ae9003f87	368	51	2	11	User4	2020-06-09T18:11:06Z	admin	2021-04-21T12:42:28.869Z	2020-10-18T14:47:30Z
1097	4	6	baebcee9-d8ae-475f-9b38-f0e77bdb7ad8	379	51	2	11	User9	2020-02-22T03:38:38Z	admin	2021-04-21T12:42:29.011Z	2020-10-09T20:20:54Z
1100	4	6	c07952df-1113-4be5-a494-5293a9d9c18e	384	51	2	11	User9	2020-07-28T10:16:49Z	admin	2021-04-21T12:42:29.152Z	2020-07-21T07:01:16Z
1109	4	6	404b2495-213e-4967-a3d4-22bda735576b	403	51	2	11	User6	2020-04-06T09:29:30Z	admin	2021-04-21T12:42:29.424Z	2020-12-31T19:17:46Z
1114	4	6	f513732a-6354-485d-a34d-0025420971fd	408	51	2	11	User8	2020-12-14T19:34:52Z	admin	2021-04-21T12:42:29.562Z	2020-10-05T21:21:46Z
1117	4	6	361f2cf0-1202-49be-a573-7aaf7c290c53	416	51	2	11	User2	2020-10-25T17:58:41Z	admin	2021-04-21T12:42:29.687Z	2020-07-18T22:51:51Z
1137	4	6	dd6203fd-7bd1-4af9-8d93-f3d108509a7c	458	51	2	11	User2	2020-12-24T23:42:39Z	admin	2021-04-21T12:42:30.413Z	2020-01-02T03:16Z
670	5	6	928d2e14-8869-41f6-a2bc-489a37baa560	11	141	4	59	mjackson	2011-02-24T17:16:25.660Z	mjackson	2011-02-24T17:16:25.660Z	\N
983	4	6	10357afe-c720-4f02-83c9-77247faff2a9	150	51	2	11	User5	2020-03-12T16:41:13Z	admin	2021-04-21T12:42:22.546Z	2020-04-01T19:12:26Z
671	3	6	38745585-816a-403f-8005-0a55c0aec813	11	24	4	59	mjackson	2011-02-15T21:18:38.144Z	mjackson	2011-02-15T21:18:38.144Z	\N
987	4	6	3a929081-c14c-4cb2-95b9-3cf0d9b182fc	159	51	2	11	User3	2020-06-28T14:24:50Z	admin	2021-04-21T12:42:22.699Z	2020-10-27T16:06:41Z
672	5	6	5515d3e1-bb2a-42ed-833c-52802a367033	11	51	4	59	mjackson	2011-03-03T10:31:30.596Z	mjackson	2011-03-03T10:31:31.651Z	\N
991	4	6	24d889a7-fcf4-4f41-aaaa-d1dbb4b3b34f	166	51	2	11	User2	2020-08-07T08:58:41Z	admin	2021-04-21T12:42:22.867Z	2020-11-26T08:25:04Z
673	5	6	e725ee47-62c6-4ae9-a761-9b69ba2835c5	11	141	4	59	mjackson	2011-03-03T10:32:15.048Z	mjackson	2011-03-03T10:32:15.048Z	\N
995	4	6	6f0b0f9e-c007-4639-8689-853d37111d2e	177	51	2	11	User7	2020-09-16T19:23:38Z	admin	2021-04-21T12:42:23.047Z	2020-05-28T02:37:04Z
674	5	6	41c25437-ce2e-47e1-8e3d-a2f3008e7456	11	141	4	59	mjackson	2011-03-03T10:32:32.773Z	mjackson	2011-03-03T10:32:32.773Z	\N
1000	4	6	c8e58376-d428-49f2-8067-8751534b02cd	185	51	2	11	User6	2020-03-18T11:59:54Z	admin	2021-04-21T12:42:23.179Z	2020-01-18T01:14:22Z
675	5	6	99cb2789-f67e-41ff-bea9-505c138a6b23	11	51	4	59	mjackson	2011-03-03T10:31:30.596Z	mjackson	2011-03-03T10:31:31.651Z	\N
1004	4	6	158a167c-7f38-4d1a-94ad-712626644ea8	193	51	2	11	User8	2020-03-23T09:20:35Z	admin	2021-04-21T12:42:23.314Z	2020-08-06T15:15:55Z
676	5	6	8024c190-0aa9-4d69-b5a3-cd1cfe9c7ecc	11	141	4	59	mjackson	2011-03-03T10:32:23.462Z	mjackson	2011-03-03T10:32:23.462Z	\N
1008	4	6	ab7ed328-910c-4be1-ab99-3b2a63aa34fe	201	51	2	11	User6	2020-01-18T20:30:50Z	admin	2021-04-21T12:42:23.457Z	2020-07-18T23:09:42Z
677	5	6	1cffebce-c758-4071-a6ae-1e5730015e81	11	141	4	59	mjackson	2011-03-03T10:33:17.246Z	mjackson	2011-03-03T10:33:17.246Z	\N
678	4	6	0e24b99c-41f0-43e1-a55e-fb9f50d73820	11	24	4	59	mjackson	2011-02-15T20:18:59.808Z	mjackson	2011-02-15T20:18:59.808Z	\N
998	4	6	61ad4595-2717-4a48-befc-41c78602bbb2	181	51	2	11	User3	2020-01-13T16:00:40Z	admin	2021-04-21T12:42:23.119Z	2020-06-24T00:15:23Z
1014	4	6	56db4518-aff4-4009-b12c-5679c5ecc981	212	51	2	11	User3	2020-04-05T20:58:47Z	admin	2021-04-21T12:42:23.664Z	2020-11-18T09:45:47Z
1017	4	6	0a7d94da-ab8a-4a90-bb7d-f11812ac44c1	220	51	2	11	User9	2020-03-29T18:45:04Z	admin	2021-04-21T12:42:23.777Z	2020-03-16T05:02:54Z
681	4	6	64f69e69-f61e-42a3-8697-95eea1f2bda2	11	24	4	59	mjackson	2011-02-15T20:19:00.007Z	mjackson	2011-02-15T20:19:00.007Z	\N
682	3	6	a534356f-8dd6-4d9a-8ffb-dc1adb140c01	11	203	4	59	abeecher	2011-02-15T22:14:39.548Z	abeecher	2011-02-15T22:14:39.548Z	\N
1021	4	6	eafb12b3-c9bd-4bf0-bb5c-630357568b6d	228	51	2	11	User2	2020-05-20T02:26:28Z	admin	2021-04-21T12:42:23.904Z	2020-08-16T18:02:50Z
683	3	6	4b9ebe73-7b19-4aaf-b596-5e545544e2a6	11	205	4	59	abeecher	2011-02-15T22:15:49.142Z	abeecher	2011-02-15T22:15:49.142Z	\N
1025	4	6	735653ad-bbe4-4584-8a82-dde55f17802f	236	51	2	11	User5	2020-06-26T13:13:23Z	admin	2021-04-21T12:42:24.034Z	2020-01-28T02:49:15Z
685	3	6	a53c7a85-12d0-4eb1-8e03-f030e0778da3	11	205	4	59	abeecher	2011-02-15T22:19:20.437Z	abeecher	2011-02-15T22:19:20.437Z	\N
1029	4	6	3bb7cdf8-579e-436d-aa75-bd84bd2fd238	244	51	2	11	User3	2020-09-16T23:01:44Z	admin	2021-04-21T12:42:24.176Z	2020-04-03T06:10:16Z
686	3	6	e57195d3-aeda-432d-bfc4-0a556b2d8ab9	11	205	4	59	abeecher	2011-02-15T22:23:00.750Z	abeecher	2011-02-15T22:23:00.750Z	\N
687	3	6	fc50d8a0-1bac-430e-b13d-3ac271c6578e	11	203	4	59	abeecher	2011-02-15T22:27:06.046Z	abeecher	2011-02-24T17:41:49.912Z	\N
1033	4	6	3ca7bf84-c413-477e-9660-9c95dc04b6c0	252	51	2	11	User8	2020-11-05T17:30:43Z	admin	2021-04-21T12:42:24.299Z	2020-05-15T02:12:48Z
1037	4	6	bc482b04-00e6-4cfb-875e-d8c003129e45	258	51	2	11	User7	2020-06-04T15:32:51Z	admin	2021-04-21T12:42:24.605Z	2020-11-02T10:04:32Z
690	3	6	8d4429e7-804f-43cf-bd81-288e561db9a8	11	212	4	59	abeecher	2011-02-15T22:30:39.843Z	abeecher	2011-02-15T22:30:39.843Z	\N
1041	4	6	91c0aab5-77d6-490b-b812-916c63b57186	267	51	2	11	User10	2020-11-22T03:07:02Z	admin	2021-04-21T12:42:25.986Z	2020-08-13T10:51:22Z
1046	4	6	62d34f4e-2fb5-4fd3-b309-d692196587af	274	51	2	11	User2	2020-04-14T13:25:57Z	admin	2021-04-21T12:42:26.116Z	2020-05-12T15:55:19Z
1049	4	6	bcebdb5f-d30a-4bb5-b546-e8df3b9081d0	282	51	2	11	User9	2020-09-10T02:46:41Z	admin	2021-04-21T12:42:26.331Z	2020-02-23T16:41:05Z
1056	4	6	2c005eae-ce0e-47dc-bd37-a77505195449	296	51	2	11	User2	2020-10-25T14:13:10Z	admin	2021-04-21T12:42:27.060Z	2020-01-04T12:25:11Z
1059	4	6	9fe19cb2-8bc7-47cb-947a-1017ab2087de	307	51	2	11	User9	2020-03-25T01:35:33Z	admin	2021-04-21T12:42:27.195Z	2020-10-22T10:30:53Z
1065	4	6	1fbf16c4-ee6f-4d5b-8953-ada6070f83cb	312	51	2	11	User10	2020-04-10T15:44:58Z	admin	2021-04-21T12:42:27.337Z	2020-10-27T07:30:02Z
1086	4	6	ed14b80a-f34e-40f7-b396-9e4519c68c09	355	51	2	11	User5	2020-04-19T12:47:40Z	admin	2021-04-21T12:42:28.584Z	2020-07-31T06:57:04Z
1101	4	6	28edb273-f402-4e99-b5b8-1e512d3736c2	386	51	2	11	User5	2020-04-24T22:23:17Z	admin	2021-04-21T12:42:29.152Z	2020-03-13T04:17:06Z
1104	4	6	62602cbf-073b-4f9c-951d-a357dee5978a	392	51	2	11	User9	2020-05-23T08:25:32Z	admin	2021-04-21T12:42:29.265Z	2020-01-24T06:59:52Z
1113	4	6	3148267c-7541-497b-9fb4-63b39bc389d2	411	51	2	11	User9	2020-01-04T07:22:05Z	admin	2021-04-21T12:42:29.578Z	2020-02-02T12:41:21Z
1118	4	6	fef2e2fb-5043-4a6a-99e2-6ac4fc3c38f5	418	51	2	11	User5	2020-03-09T10:37:50Z	admin	2021-04-21T12:42:29.692Z	2020-10-28T19:31:12Z
1120	4	6	7bfa5df2-77da-4523-b349-80bf74529376	426	51	2	11	User5	2020-06-11T07:46:42Z	admin	2021-04-21T12:42:29.824Z	2020-09-03T04:31:01Z
1134	4	6	e993de69-39b9-4837-9906-ebbe474e5b98	451	51	2	11	User6	2020-05-30T21:53:48Z	admin	2021-04-21T12:42:30.261Z	2020-01-06T18:20:28Z
1005	4	6	5bcd5d39-ae77-4d7e-9b81-f35b1090235c	197	51	2	11	User8	2020-07-31T21:53:47Z	admin	2021-04-21T12:42:23.389Z	2020-11-17T20:25:24Z
1009	4	6	0b92658d-06ad-4f18-9954-2d55826c3116	205	51	2	11	User1	2020-09-10T16:06:27Z	admin	2021-04-21T12:42:23.507Z	2020-07-14T08:58:04Z
693	4	6	cdefb3a9-8f55-4771-a9e3-06fa370250f6	11	24	4	59	mjackson	2011-02-15T21:46:01.603Z	mjackson	2011-02-15T21:46:01.603Z	\N
1062	4	6	d3700f2a-6e46-4414-bbd0-7bc838cf5418	305	51	2	11	User2	2020-10-04T09:56:35Z	admin	2021-04-21T12:42:27.193Z	2020-08-12T20:56:20Z
695	1	4	f3bbc7a5-fcd7-41d2-8f56-e04913a75193	11	164	2	60	System	2021-04-21T12:37:48.469Z	System	2021-04-21T12:37:48.469Z	\N
696	2	4	6494ee99-3380-4e5b-bba9-c62a22ed9775	11	51	4	60	System	2021-04-21T12:37:48.473Z	System	2021-04-21T12:37:48.473Z	\N
1064	4	6	d84cf87b-2a7e-46f0-b072-68ebde484fed	313	51	2	11	User4	2020-03-02T02:36:28Z	admin	2021-04-21T12:42:27.338Z	2020-08-12T16:48:40Z
1068	4	6	bbb63551-9a16-47df-bcef-714730e9818a	320	51	2	11	User2	2020-02-01T15:21:49Z	admin	2021-04-21T12:42:27.705Z	2020-10-24T07:14:43Z
698	1	4	e9a25460-4f4c-423e-890d-4fb56e3f5a22	11	164	2	60	System	2021-04-21T12:37:48.502Z	System	2021-04-21T12:37:48.502Z	\N
1075	4	6	c85acb95-f510-4375-934c-ddc434a21027	336	51	2	11	User8	2020-08-30T11:07:31Z	admin	2021-04-21T12:42:28.296Z	2020-04-21T19:30:40Z
699	2	4	a0b9c07a-1c7d-444b-a681-98715e462ca0	11	51	4	60	System	2021-04-21T12:37:48.505Z	System	2021-04-21T12:37:48.505Z	\N
1079	4	6	8bccf72a-e770-43ce-bd3b-83a30ea3d249	346	51	2	11	User9	2020-06-15T16:16:47Z	admin	2021-04-21T12:42:28.461Z	2020-08-12T16:28:43Z
1085	4	6	99ece8e1-23f7-48ec-af75-8d1737d21712	354	51	2	11	User4	2020-06-11T08:31:13Z	admin	2021-04-21T12:42:28.582Z	2020-11-14T21:28:28Z
701	1	4	1f000777-84cf-4a22-90cf-e2c1d9685e5a	11	164	2	60	System	2021-04-21T12:37:48.536Z	System	2021-04-21T12:37:48.536Z	\N
702	2	4	79ed0c4f-3446-4f7b-a556-8555068353b5	11	51	4	60	System	2021-04-21T12:37:48.539Z	System	2021-04-21T12:37:48.539Z	\N
1090	4	6	0d4a1907-4044-4b40-847c-98335810d00b	363	51	2	11	User2	2020-04-11T17:22:52Z	admin	2021-04-21T12:42:28.736Z	2020-02-21T22:58:50Z
1105	4	6	3d69a67e-96a8-444d-aacd-425950ddd28b	393	51	2	11	User5	2020-04-29T07:37:43Z	admin	2021-04-21T12:42:29.265Z	2020-02-14T15:42:17Z
1108	4	6	6552b224-5509-41a2-b88d-89847ac7f8f0	401	51	2	11	User8	2020-01-16T22:21:18Z	admin	2021-04-21T12:42:29.395Z	2020-05-02T20:26:17Z
1132	4	6	25373c01-4d84-41bc-a3c9-64ddc045604e	450	51	2	11	User10	2020-02-15T20:12:37Z	admin	2021-04-21T12:42:30.257Z	2020-04-02T00:56:03Z
1138	4	6	8b584410-3d37-4b67-85b2-9a78c20f44de	459	51	2	11	User5	2020-02-12T15:06:30Z	admin	2021-04-21T12:42:30.421Z	2020-10-06T09:07:13Z
1007	4	6	911f658f-b814-4971-8a4c-d011f2a4fea6	198	51	2	11	User7	2020-12-09T01:59:02Z	admin	2021-04-21T12:42:23.441Z	2020-09-01T12:43:53Z
1011	4	6	7d82f7b9-2329-4392-b1f5-b9197ac967c5	208	51	2	11	User8	2020-03-29T02:21:51Z	admin	2021-04-21T12:42:23.552Z	2020-01-26T19:08:17Z
703	4	6	059c5bc7-2d38-4dc5-96b8-d09cd3c69b4c	11	24	4	59	mjackson	2011-02-15T22:04:54.290Z	mjackson	2011-02-15T22:04:54.290Z	\N
1015	4	6	3c21c3f2-7804-43e6-81dc-ee7a40ae9a86	214	51	2	11	User9	2020-10-07T07:52:05Z	admin	2021-04-21T12:42:23.701Z	2020-04-02T03:22:04Z
705	4	6	42226a03-34a8-43b0-bb37-d86cd09353f7	11	198	4	59	mjackson	2011-02-15T22:05:46.902Z	mjackson	2011-02-15T22:05:46.902Z	\N
1019	4	6	0da68d49-4332-4bcd-a29c-4d6b289d5c2f	223	51	2	11	User7	2020-03-30T12:47:09Z	admin	2021-04-21T12:42:23.826Z	2020-10-21T12:32:06Z
1023	4	6	128276a3-988b-4d4e-bdd0-34f042667907	230	51	2	11	User2	2020-04-18T04:27:09Z	admin	2021-04-21T12:42:23.954Z	2020-09-08T18:09:41Z
1027	4	6	f8d0898e-4f11-418f-8596-d39677feaefc	239	51	2	11	User2	2020-10-12T19:09:41Z	admin	2021-04-21T12:42:24.096Z	2020-06-26T17:29:10Z
1042	4	6	75ef71ab-4b11-4e95-b8c0-b5d2ceeedb0a	266	51	2	11	User6	2020-03-20T15:48:08Z	admin	2021-04-21T12:42:25.982Z	2020-07-12T17:39:39Z
1054	4	6	bf76dd53-1032-4472-b27a-b81698aface9	291	51	2	11	User7	2020-02-24T21:11:23Z	admin	2021-04-21T12:42:26.859Z	2020-02-27T19:31:13Z
1057	4	6	b389cfc5-7973-4fbb-9708-6911fb368dd7	299	51	2	11	User9	2020-07-10T16:05:48Z	admin	2021-04-21T12:42:27.062Z	2020-09-13T00:52:20Z
710	4	6	308ad851-b4ab-4f41-bbd0-c83398d2afe4	11	198	4	59	mjackson	2011-02-15T22:06:21.034Z	mjackson	2011-02-15T22:06:21.034Z	\N
1074	4	6	6b2b3ea9-f915-470d-8bc6-22e10d991069	328	51	2	11	User1	2020-11-01T05:00Z	admin	2021-04-21T12:42:28.133Z	2020-08-27T13:50:52Z
1076	4	6	832cc4c7-4de9-49c2-9b17-cd3141d95343	337	51	2	11	User3	2020-05-20T20:08:25Z	admin	2021-04-21T12:42:28.296Z	2020-11-30T14:23:37Z
1080	4	6	ef998e88-3f38-4bbb-9dbc-3d246a4a6a9e	345	51	2	11	User5	2020-09-08T05:13:38Z	admin	2021-04-21T12:42:28.461Z	2020-01-17T16:38:40Z
1084	4	6	e3a0a831-0a87-4d47-b4ef-06b3108560cb	353	51	2	11	User8	2020-10-10T19:52:12Z	admin	2021-04-21T12:42:28.580Z	2020-05-04T00:59:07Z
1098	4	6	1187f060-3024-4b7a-ac29-68817e41cea1	377	51	2	11	User5	2020-09-29T15:48:22Z	admin	2021-04-21T12:42:29.010Z	2020-12-02T05:12:26Z
1123	4	6	54233dfb-3102-44ca-a09e-32bf4b94fd5a	433	51	2	11	User9	2020-11-05T11:07:21Z	admin	2021-04-21T12:42:29.942Z	2020-11-11T06:27:14Z
1127	4	6	b9194683-07e4-4139-aea3-296e07a5e9a2	441	51	2	11	User8	2020-11-14T16:52:57Z	admin	2021-04-21T12:42:30.098Z	2020-10-11T11:00:02Z
1012	4	6	5fa76ce0-a9c0-4609-ab5a-45691986cbc3	209	51	2	11	User5	2020-01-19T05:10:50Z	admin	2021-04-21T12:42:23.565Z	2020-09-24T13:36:58Z
712	3	6	0db6f5ce-35b6-40df-9216-c9cf0aaf0961	11	24	4	59	admin	2011-06-14T14:11:32.858Z	admin	2011-06-14T14:11:32.858Z	\N
1016	4	6	d4b35de1-af10-44b2-afdc-fc554df47143	217	51	2	11	User8	2020-02-12T13:10:20Z	admin	2021-04-21T12:42:23.735Z	2020-01-01T11:54:31Z
713	3	6	fa555056-bd0c-4c59-ac9f-1ab5b0e18e27	11	24	4	59	admin	2011-06-14T14:15:58.746Z	admin	2011-06-14T14:15:58.746Z	\N
714	3	6	2edddd3a-2df8-4887-93ad-b33aa073c539	11	24	4	59	admin	2011-06-14T14:16:06.229Z	admin	2011-06-14T14:16:06.229Z	\N
1020	4	6	30b33816-a70c-4b28-8289-054bc9e5de1f	225	51	2	11	User5	2020-04-15T17:30:06Z	admin	2021-04-21T12:42:23.861Z	2020-06-27T01:34Z
715	3	6	0f746f38-4163-492e-92b2-73ecf7b31fa2	11	24	4	59	admin	2011-06-14T14:16:36.303Z	admin	2011-06-14T14:16:36.303Z	\N
1024	4	6	7f3c28e2-95a0-41d8-a3ff-e010a98d828c	233	51	2	11	User6	2020-11-30T11:11:30Z	admin	2021-04-21T12:42:23.984Z	2020-06-04T15:08:20Z
716	4	6	8915ce60-7845-40a2-b74e-4837afdb45a0	11	51	4	59	admin	2011-06-14T14:16:40.866Z	admin	2011-06-14T14:16:40.933Z	\N
717	3	6	ea915509-f71c-4b81-a600-cd96ddb9fce6	11	24	4	59	admin	2011-06-14T14:16:51.808Z	admin	2011-06-14T14:16:51.808Z	\N
1028	4	6	526f6ef0-0bb3-4e7d-b59f-d619c3962bc5	241	51	2	11	User5	2020-07-04T16:37:36Z	admin	2021-04-21T12:42:24.116Z	2020-10-04T09:15:35Z
718	4	6	0fb80b90-4b71-4c00-b9d2-279ce1beca5d	11	51	4	59	admin	2011-06-14T14:16:56.437Z	admin	2011-06-14T14:16:56.497Z	\N
1032	4	6	fd8f7c2e-c696-41e0-b7bb-3eaf5bf7511c	250	51	2	11	User10	2020-07-13T06:13:20Z	admin	2021-04-21T12:42:24.240Z	2020-10-06T14:44:41Z
719	4	6	6c38c7da-ac2c-40d0-a38d-f8517dba80b5	11	51	4	59	admin	2011-06-14T14:16:56.455Z	admin	2011-06-14T14:16:56.517Z	\N
720	4	6	37be157c-741c-4e51-b781-20d36e4e335a	11	51	4	59	admin	2011-06-14T14:16:56.597Z	admin	2011-06-14T14:16:56.658Z	\N
1036	4	6	145a438d-ba14-424c-af38-b9374725eb62	256	51	2	11	User6	2020-06-26T11:17:19Z	admin	2021-04-21T12:42:24.399Z	2020-05-01T00:50:03Z
721	4	6	81171dd1-865b-49d1-8c8a-27c6eb260774	11	51	4	59	admin	2011-06-14T14:16:56.600Z	admin	2011-06-14T14:16:56.658Z	\N
1040	4	6	38db162a-f9f1-4a39-8fcf-b49f2489d534	265	51	2	11	User9	2020-12-26T05:50:13Z	admin	2021-04-21T12:42:25.972Z	2020-11-20T18:15:10Z
722	4	6	8b44ac01-d864-4de8-a86c-8f7ec1cfe07d	11	51	4	59	admin	2011-06-14T14:16:56.735Z	admin	2011-06-14T14:16:56.786Z	\N
1066	4	6	33e121fc-8802-4776-9bd8-e0de6f7e5e80	315	51	2	11	User7	2020-01-20T02:57:51Z	admin	2021-04-21T12:42:27.347Z	2020-08-16T08:35:35Z
1070	4	6	20b1f989-32d5-4704-8caf-d067412c94d0	323	51	2	11	User9	2020-10-09T08:51:35Z	admin	2021-04-21T12:42:27.711Z	2020-08-04T10:47:17Z
1071	4	6	b2f140e1-8fa9-414d-826c-5f01e4af8f82	330	51	2	11	User5	2020-11-07T14:15:35Z	admin	2021-04-21T12:42:28.141Z	2020-05-25T05:13:05Z
1077	4	6	5720d60c-63f7-4751-9dd5-d6e874d3dcc4	338	51	2	11	User4	2020-01-09T22:05:07Z	admin	2021-04-21T12:42:28.296Z	2020-09-22T19:33:21Z
1081	4	6	9e88533c-2535-4cf1-89f4-3107b18f99c4	347	51	2	11	User8	2020-06-03T05:15:41Z	admin	2021-04-21T12:42:28.465Z	2020-07-26T17:39:05Z
1089	4	6	134cb028-dbf2-4982-b850-17bbe554d3ba	361	51	2	11	User5	2020-10-09T14:26:50Z	admin	2021-04-21T12:42:28.731Z	2020-07-03T02:17:58Z
1093	4	6	3407f8de-7082-45db-8ef7-96afc89df44e	369	51	2	11	User3	2020-10-10T08:04:13Z	admin	2021-04-21T12:42:28.869Z	2020-08-31T05:19:56Z
1122	4	6	68353615-ecf1-489c-bb76-a3c8be983558	425	51	2	11	User1	2020-09-13T07:40:26Z	admin	2021-04-21T12:42:29.823Z	2020-02-04T19:15:44Z
1124	4	6	ee5ee0b0-a27a-4b2b-b3a8-7d99d53222c0	434	51	2	11	User9	2020-11-18T13:19:33Z	admin	2021-04-21T12:42:29.943Z	2020-02-15T04:21:33Z
1128	4	6	78f87e19-aed2-403d-99a3-45dc530f77e4	440	51	2	11	User5	2020-01-11T03:52:27Z	admin	2021-04-21T12:42:30.097Z	2020-07-31T13:56:25Z
1133	4	6	72e48319-d664-4f20-ba7e-11edd90279ac	448	51	2	11	User1	2020-05-09T14:15:24Z	admin	2021-04-21T12:42:30.240Z	2020-08-10T15:13Z
1026	4	6	2868632f-025a-49ab-9662-0607c266af65	237	51	2	11	User3	2020-12-30T01:44:23Z	admin	2021-04-21T12:42:24.057Z	2020-01-12T19:39Z
723	4	6	a1f014db-b5a4-4ccd-8305-edc12e4a6f7b	11	51	4	59	admin	2011-06-14T14:16:56.919Z	admin	2011-06-14T14:16:56.971Z	\N
724	4	6	1d981316-baaf-4ef7-801b-c6aa5b1e102d	11	51	4	59	admin	2011-06-14T14:16:57.168Z	admin	2011-06-14T14:16:57.244Z	\N
1030	4	6	e94226eb-1657-43f5-b5a9-bf21cd2bf223	245	51	2	11	User8	2020-08-05T12:16:04Z	admin	2021-04-21T12:42:24.195Z	2020-05-31T03:57:10Z
725	4	6	fe0de3c8-5cc8-4852-bc36-bdcaef8c9989	11	51	4	59	admin	2011-06-14T14:16:57.453Z	admin	2011-06-14T14:16:57.530Z	\N
1035	4	6	56f3cc33-5487-4053-8d9b-4f1bee3fef60	254	51	2	11	User8	2020-12-10T07:35:06Z	admin	2021-04-21T12:42:24.317Z	2020-04-09T09:37:48Z
726	4	6	aa62394d-b2db-4489-8a1c-5120ab61a6a5	11	51	4	59	admin	2011-06-14T14:16:58.489Z	admin	2011-06-14T14:16:58.546Z	\N
708	6	6	45210491-2d7c-4a85-ab7c-e6997d32ff02	11	198	4	59	abeecher	2011-02-15T22:09:27.794Z	admin	2011-06-14T10:28:57.790Z	\N
707	6	6	9f37fbee-5a28-4732-8fce-95577e003ad5	11	198	4	59	mjackson	2011-02-15T22:08:49.798Z	admin	2011-06-14T10:28:57.681Z	\N
706	6	6	5eda88dd-74dc-4166-a3df-bb4500ed8877	11	198	4	59	abeecher	2011-02-15T22:06:59.801Z	admin	2011-06-14T10:28:57.647Z	\N
689	4	6	a10b3171-ea96-4d34-b090-f8db09f4efb1	11	212	4	59	abeecher	2011-02-15T22:29:40.511Z	admin	2011-06-14T10:28:57.702Z	\N
691	4	6	7778cf88-836f-4833-a0df-3056d2b20e7a	11	212	4	59	abeecher	2011-02-15T22:31:26.282Z	admin	2011-06-14T10:28:57.751Z	\N
684	4	6	db31dce5-2469-4c68-8641-9becad64a756	11	205	4	59	abeecher	2011-02-15T22:18:03.303Z	admin	2011-06-14T10:28:57.624Z	\N
688	4	6	05571c6d-bf6c-4509-8242-5d159726ec80	11	212	4	59	abeecher	2011-02-15T22:28:52.496Z	admin	2011-06-14T10:28:57.574Z	\N
692	4	6	06b07aa0-c8f5-494b-a2ff-ff134d7bcd9b	11	212	4	59	abeecher	2011-03-08T10:39:44.210Z	admin	2011-06-14T10:28:57.727Z	\N
711	6	6	f85d87f1-79d9-450f-b01c-5fed4b44f86b	11	198	4	59	abeecher	2011-02-15T22:07:30.638Z	admin	2011-06-14T10:28:57.600Z	\N
602	5	6	8bb36efb-c26d-4d2b-9199-ab6922f53c28	11	24	4	59	mjackson	2011-02-15T20:47:03.951Z	mjackson	2011-02-15T21:00:43.616Z	\N
648	6	6	05dedd34-9d9d-48d9-9af6-c81b555541c9	11	51	4	59	abeecher	2011-03-08T10:35:10.064Z	abeecher	2011-03-08T10:37:43.961Z	\N
649	5	6	8ab12916-4897-47fb-94eb-1ab699822ecb	11	24	4	59	mjackson	2011-02-15T20:50:25.839Z	mjackson	2011-02-15T21:08:20.590Z	\N
679	6	6	a38308f8-6f30-4d8a-8576-eaf6703fb9d3	11	199	4	59	mjackson	2011-02-15T21:43:14.377Z	mjackson	2011-02-15T21:43:14.377Z	\N
680	6	6	602b72e5-e365-4eee-b68d-b3dd26270ee3	11	199	4	59	mjackson	2011-02-15T21:44:04.010Z	mjackson	2011-02-15T21:44:04.010Z	\N
694	9	6	d6f3a279-ce86-4a12-8985-93b71afbb71d	11	51	4	59	mjackson	2011-02-15T21:46:47.847Z	admin	2011-06-14T10:28:57.221Z	\N
697	9	6	1373739a-2849-4647-9e97-7a4e05cc5841	11	51	4	59	mjackson	2011-02-15T21:50:49.999Z	admin	2011-06-14T10:28:57.304Z	\N
700	9	6	3c73aace-9f54-420d-a1c0-c54b6a116dcf	11	51	4	59	mjackson	2011-02-15T21:59:31.855Z	admin	2011-06-14T10:28:57.370Z	\N
704	5	6	53e8664e-e1fb-40d0-9104-019d57f06bee	11	197	4	59	mjackson	2011-02-15T22:05:46.889Z	mjackson	2011-02-15T22:05:46.889Z	\N
709	5	6	700ef542-9e3c-44dd-b7ea-027934010656	11	197	4	59	mjackson	2011-02-15T22:06:21.030Z	mjackson	2011-02-15T22:06:21.030Z	\N
1038	4	6	e987dc27-ce4c-46cb-b667-27a5dc7233d1	259	51	2	11	User3	2020-04-09T11:33:13Z	admin	2021-04-21T12:42:24.621Z	2020-06-02T07:02:11Z
1039	4	6	8d486c65-9083-424f-b181-8083a701f739	264	51	2	11	User5	2020-11-09T03:28:42Z	admin	2021-04-21T12:42:25.970Z	2020-06-15T12:49:37Z
1044	4	6	d3ce0465-3795-4c50-8d5e-5cb53c264856	273	51	2	11	User3	2020-10-17T18:52:40Z	admin	2021-04-21T12:42:26.096Z	2020-11-29T23:04:38Z
1048	4	6	cb6f5cda-f0d8-4923-bd94-e1117e6f2a4a	281	51	2	11	User4	2020-07-28T09:54:58Z	admin	2021-04-21T12:42:26.230Z	2020-03-20T16:04:35Z
1052	4	6	2d2c2cfd-69c4-402e-88c1-c4374fa326c1	288	51	2	11	User8	2020-04-05T15:02:59Z	admin	2021-04-21T12:42:26.727Z	2020-12-15T06:18:53Z
1078	4	6	be8d99fe-b5f3-47c3-88e6-6321281cb339	339	51	2	11	User1	2020-08-12T13:51:39Z	admin	2021-04-21T12:42:28.297Z	2020-08-20T22:21:39Z
1095	4	6	522d5aae-a78b-47a8-82e8-3ad81d950c22	376	51	2	11	User6	2020-02-09T07:50:40Z	admin	2021-04-21T12:42:29.009Z	2020-10-14T11:15:12Z
1099	4	6	57bc30a4-8411-499a-890a-a6b4d3600b42	387	51	2	11	User3	2020-09-20T02:02:13Z	admin	2021-04-21T12:42:29.152Z	2020-01-05T15:12:49Z
1106	4	6	52d18668-386c-4cdd-9a97-e4ef83059f7c	395	51	2	11	User10	2020-07-30T23:17:52Z	admin	2021-04-21T12:42:29.270Z	2020-01-10T12:15:20Z
1110	4	6	f204a2d1-8391-45fb-9c6e-b30eaead8da2	402	51	2	11	User10	2020-12-20T16:13:44Z	admin	2021-04-21T12:42:29.422Z	2020-03-20T12:18:27Z
1119	4	6	75497baf-a078-4a70-879c-29bdea4301ec	424	51	2	11	User6	2020-04-22T11:34:11Z	admin	2021-04-21T12:42:29.822Z	2020-07-13T00:13:58Z
735	1	6	33cada63-4314-409c-9b88-b988505313ed	15	79	2	13	System	2021-04-21T12:37:49.764Z	System	2021-04-21T12:37:49.764Z	\N
736	1	6	44a0b550-004a-4e06-9baa-43db1d11a5de	15	75	2	13	System	2021-04-21T12:37:49.777Z	System	2021-04-21T12:37:49.777Z	\N
737	1	6	ee778b36-8530-4699-a6d9-0f6fe6e88a6f	15	75	2	13	System	2021-04-21T12:37:49.780Z	System	2021-04-21T12:37:49.780Z	\N
738	1	6	bcf42f88-d5a7-4f9b-985f-b149473cab81	15	75	2	13	System	2021-04-21T12:37:49.782Z	System	2021-04-21T12:37:49.782Z	\N
739	1	6	037fd936-e55a-4ae0-9c2a-69c3e0404773	15	75	2	13	System	2021-04-21T12:37:49.784Z	System	2021-04-21T12:37:49.784Z	\N
740	1	6	cdc0be53-b93f-4338-9809-0713a61fa457	15	75	2	13	System	2021-04-21T12:37:49.786Z	System	2021-04-21T12:37:49.786Z	\N
741	1	6	c9922d0e-e22e-4bbf-bd02-ce3539617e8c	15	75	2	13	System	2021-04-21T12:37:49.789Z	System	2021-04-21T12:37:49.789Z	\N
742	1	6	0d623b3b-2e98-487c-8172-ae0be628c35a	15	75	2	13	System	2021-04-21T12:37:49.791Z	System	2021-04-21T12:37:49.791Z	\N
743	1	6	9ef82586-f353-416e-9176-dd1eeb886ba2	15	75	2	13	System	2021-04-21T12:37:49.794Z	System	2021-04-21T12:37:49.794Z	\N
744	1	6	1e6fd432-3015-46ac-b334-80bf5d54a12e	15	75	2	13	System	2021-04-21T12:37:49.796Z	System	2021-04-21T12:37:49.796Z	\N
745	1	6	8216e8e7-4e57-4126-ad3d-2ca70a6c5226	15	75	2	13	System	2021-04-21T12:37:49.799Z	System	2021-04-21T12:37:49.799Z	\N
746	1	6	e4a6f859-fa60-40c8-971f-26eaee602899	15	75	2	13	System	2021-04-21T12:37:49.801Z	System	2021-04-21T12:37:49.801Z	\N
747	1	6	30ea83ed-128f-42eb-b9bf-736b3eef6cf8	15	75	2	13	System	2021-04-21T12:37:49.803Z	System	2021-04-21T12:37:49.803Z	\N
748	1	6	bb74bc37-809e-4bbe-bc3d-35d820a01ced	15	75	2	13	System	2021-04-21T12:37:49.806Z	System	2021-04-21T12:37:49.806Z	\N
749	1	6	442a4d0e-0fef-40e5-a847-fa41bfd2d2cf	15	75	2	13	System	2021-04-21T12:37:49.808Z	System	2021-04-21T12:37:49.808Z	\N
750	1	6	1b8c6564-aeb9-451b-ae72-911561e5fe7f	15	75	2	13	System	2021-04-21T12:37:49.811Z	System	2021-04-21T12:37:49.811Z	\N
751	1	6	a90d7925-b0d3-4eca-9e0b-56e75b787d18	15	75	2	13	System	2021-04-21T12:37:49.814Z	System	2021-04-21T12:37:49.814Z	\N
752	1	6	b342000b-a759-4c51-9a08-88069b25f720	15	75	2	13	System	2021-04-21T12:37:49.816Z	System	2021-04-21T12:37:49.816Z	\N
753	1	6	d8a0bc90-f619-4af3-ad64-686404aa20db	15	75	2	13	System	2021-04-21T12:37:49.817Z	System	2021-04-21T12:37:49.817Z	\N
754	1	6	6fe4c32c-314f-498c-a166-5d1420e8c100	15	75	2	13	System	2021-04-21T12:37:49.819Z	System	2021-04-21T12:37:49.819Z	\N
755	1	6	30f0be68-fd80-4caf-9891-36a9da624c59	15	75	2	13	System	2021-04-21T12:37:49.821Z	System	2021-04-21T12:37:49.821Z	\N
756	1	6	e9adb4b2-a6ad-4753-bfc3-bc3ebdf7f110	15	79	2	13	System	2021-04-21T12:37:49.825Z	System	2021-04-21T12:37:49.825Z	\N
757	1	6	d585ec05-7884-4443-8f35-4c1734db2369	15	75	2	13	System	2021-04-21T12:37:49.829Z	System	2021-04-21T12:37:49.829Z	\N
758	1	6	5348b30f-e7bb-4354-bf52-0eaf27978c76	15	75	2	13	System	2021-04-21T12:37:49.831Z	System	2021-04-21T12:37:49.831Z	\N
759	1	6	d899d0c7-e8c8-459f-b4c7-bad2f4a73d9e	15	75	2	13	System	2021-04-21T12:37:49.833Z	System	2021-04-21T12:37:49.833Z	\N
760	1	6	06e45d4b-07da-40dc-b3f5-7d79e2c003b5	15	75	2	13	System	2021-04-21T12:37:49.835Z	System	2021-04-21T12:37:49.835Z	\N
761	1	6	f5c5ae6b-bd7d-445e-b51d-35ead3fe75d2	15	75	2	13	System	2021-04-21T12:37:49.837Z	System	2021-04-21T12:37:49.837Z	\N
762	1	6	80d6da54-5b9b-4e6b-a6e2-71fb37614e25	15	75	2	13	System	2021-04-21T12:37:49.840Z	System	2021-04-21T12:37:49.840Z	\N
763	1	6	2dab3755-953f-441a-9a04-a72300d2aa1d	15	75	2	13	System	2021-04-21T12:37:49.842Z	System	2021-04-21T12:37:49.842Z	\N
764	1	6	385fbb26-e59c-4867-9df3-28f66e7eae9e	15	75	2	13	System	2021-04-21T12:37:49.845Z	System	2021-04-21T12:37:49.845Z	\N
765	1	6	694d9f78-73c9-4e38-9e1c-eca901432439	15	75	2	13	System	2021-04-21T12:37:49.847Z	System	2021-04-21T12:37:49.847Z	\N
766	1	6	8b207a0b-13bf-4919-85f7-0c10672b2310	15	75	2	13	System	2021-04-21T12:37:49.849Z	System	2021-04-21T12:37:49.849Z	\N
767	1	6	bd7d82ab-2ef8-4937-8ef1-1746140133d4	15	75	2	13	System	2021-04-21T12:37:49.851Z	System	2021-04-21T12:37:49.851Z	\N
768	1	6	4532c124-2aef-4483-9ebc-79b80ee4efc0	15	75	2	13	System	2021-04-21T12:37:49.852Z	System	2021-04-21T12:37:49.852Z	\N
769	1	6	77dfbd03-c5f8-4e55-b739-0b72950ae0f0	15	75	2	13	System	2021-04-21T12:37:49.854Z	System	2021-04-21T12:37:49.854Z	\N
770	1	6	fff8798d-42ff-4c06-898b-15b7fec01580	15	75	2	13	System	2021-04-21T12:37:49.857Z	System	2021-04-21T12:37:49.857Z	\N
771	1	6	ef62462b-e0ab-41a8-b918-22ad202ee859	15	75	2	13	System	2021-04-21T12:37:49.859Z	System	2021-04-21T12:37:49.859Z	\N
772	1	6	21703a17-d4fd-4a4c-9f2d-20d5036a25e0	15	75	2	13	System	2021-04-21T12:37:49.861Z	System	2021-04-21T12:37:49.861Z	\N
773	1	6	8f31727b-4d49-4cc0-b4c3-31a3eb3277b9	15	75	2	13	System	2021-04-21T12:37:49.863Z	System	2021-04-21T12:37:49.863Z	\N
774	1	6	12b975f6-846a-4f6b-9508-a126b7c66150	15	75	2	13	System	2021-04-21T12:37:49.865Z	System	2021-04-21T12:37:49.865Z	\N
775	1	6	002aad37-bb6f-4208-bdc4-5be311b07dc9	15	75	2	13	System	2021-04-21T12:37:49.867Z	System	2021-04-21T12:37:49.867Z	\N
776	1	6	71c267ed-6634-48b3-9c0b-a91dba9408bd	15	75	2	13	System	2021-04-21T12:37:49.869Z	System	2021-04-21T12:37:49.869Z	\N
777	1	6	04c6126e-ebb5-4824-87d7-10c814cb69bf	15	79	2	13	System	2021-04-21T12:37:49.871Z	System	2021-04-21T12:37:49.871Z	\N
778	1	6	ed492aaf-59e4-4076-affa-f98c0ca6b3f6	15	75	2	13	System	2021-04-21T12:37:49.876Z	System	2021-04-21T12:37:49.876Z	\N
779	1	6	0112f07c-d551-438b-9d48-06d683942906	15	75	2	13	System	2021-04-21T12:37:49.878Z	System	2021-04-21T12:37:49.878Z	\N
780	1	6	d948802c-a360-42d8-afce-3ae103b75f25	15	75	2	13	System	2021-04-21T12:37:49.880Z	System	2021-04-21T12:37:49.880Z	\N
781	1	6	e6640fc0-4acc-4cc6-8f36-82a046c61cad	15	75	2	13	System	2021-04-21T12:37:49.882Z	System	2021-04-21T12:37:49.882Z	\N
782	1	6	cfeb7992-49f8-48ee-9603-5d05dc7edbcd	15	75	2	13	System	2021-04-21T12:37:49.884Z	System	2021-04-21T12:37:49.884Z	\N
783	1	6	4a8fae4c-3448-49cd-b3cc-036c5329c3b6	15	75	2	13	System	2021-04-21T12:37:49.886Z	System	2021-04-21T12:37:49.886Z	\N
784	1	6	3e38407c-181f-44ba-bf41-232fb9a3a238	15	75	2	13	System	2021-04-21T12:37:49.888Z	System	2021-04-21T12:37:49.888Z	\N
785	1	6	7610d391-ee0e-4e4a-aa6d-9e6d1f6b4b9a	15	75	2	13	System	2021-04-21T12:37:49.890Z	System	2021-04-21T12:37:49.890Z	\N
786	1	6	484b1de3-06ed-4d8b-aa59-07b2c741c67d	15	75	2	13	System	2021-04-21T12:37:49.892Z	System	2021-04-21T12:37:49.892Z	\N
787	1	6	0cfeb74d-c458-465a-90bf-307865d38da5	15	75	2	13	System	2021-04-21T12:37:49.895Z	System	2021-04-21T12:37:49.895Z	\N
788	1	6	20880c39-4624-483d-85ab-3122a244efe0	15	75	2	13	System	2021-04-21T12:37:49.897Z	System	2021-04-21T12:37:49.897Z	\N
789	1	6	b892235b-fe73-440d-9e91-3e25b228f1d8	15	79	2	13	System	2021-04-21T12:37:49.900Z	System	2021-04-21T12:37:49.900Z	\N
790	1	6	6cbdeb1d-9013-4009-a8ee-22ff57d8f239	15	75	2	13	System	2021-04-21T12:37:49.905Z	System	2021-04-21T12:37:49.905Z	\N
791	1	6	1892a0d0-ee97-4678-88a2-2638accc0469	15	75	2	13	System	2021-04-21T12:37:49.906Z	System	2021-04-21T12:37:49.906Z	\N
792	1	6	e3cda88b-d2be-4561-ae43-e0afafd8d158	15	75	2	13	System	2021-04-21T12:37:49.908Z	System	2021-04-21T12:37:49.908Z	\N
793	1	6	79bdc559-4f00-4638-a671-60cc9fe29e59	15	75	2	13	System	2021-04-21T12:37:49.910Z	System	2021-04-21T12:37:49.910Z	\N
794	1	6	cf4072ef-a752-4fd1-9df8-d8b2b56ef368	15	75	2	13	System	2021-04-21T12:37:49.912Z	System	2021-04-21T12:37:49.912Z	\N
795	1	6	435aaedb-f269-40fd-aeb3-a4016a5998f1	15	75	2	13	System	2021-04-21T12:37:49.914Z	System	2021-04-21T12:37:49.914Z	\N
796	1	6	403180d4-267c-4013-8772-3d8e9fdb843c	15	75	2	13	System	2021-04-21T12:37:49.916Z	System	2021-04-21T12:37:49.916Z	\N
797	1	6	0a5d9345-9d85-4eb6-9fc3-b3b6104e49d9	15	75	2	13	System	2021-04-21T12:37:49.919Z	System	2021-04-21T12:37:49.919Z	\N
798	1	6	c8742419-32e1-44ae-ad8d-f90d77adbba3	15	75	2	13	System	2021-04-21T12:37:49.922Z	System	2021-04-21T12:37:49.922Z	\N
799	1	6	b0fffbd4-9313-4fad-b887-44ff76cdf105	15	75	2	13	System	2021-04-21T12:37:49.924Z	System	2021-04-21T12:37:49.924Z	\N
800	1	6	8b9217ae-0960-4bd3-919c-1e0141cbf2cb	15	75	2	13	System	2021-04-21T12:37:49.926Z	System	2021-04-21T12:37:49.926Z	\N
801	1	6	c1de0e12-e0e7-4adc-9e39-39573d490225	15	75	2	13	System	2021-04-21T12:37:49.928Z	System	2021-04-21T12:37:49.928Z	\N
802	1	6	03515776-3be0-420f-9598-41eece9fdd03	15	75	2	13	System	2021-04-21T12:37:49.931Z	System	2021-04-21T12:37:49.931Z	\N
803	1	6	724d43ec-0e30-49ca-8e89-a277f5b0b2cf	15	75	2	13	System	2021-04-21T12:37:49.932Z	System	2021-04-21T12:37:49.932Z	\N
804	1	6	003d088c-343c-46f6-8b76-6145fb22a5fa	15	75	2	13	System	2021-04-21T12:37:49.934Z	System	2021-04-21T12:37:49.934Z	\N
805	1	6	e21a0d5b-e5bc-4cd5-9462-2ccb7e5c2a62	15	75	2	13	System	2021-04-21T12:37:49.936Z	System	2021-04-21T12:37:49.936Z	\N
806	1	6	9ed4d113-3878-408d-bebb-2fd5ead305ad	15	75	2	13	System	2021-04-21T12:37:49.938Z	System	2021-04-21T12:37:49.938Z	\N
807	1	6	e3578e0a-08b6-4df4-9ebe-8e2a76c2ec89	15	75	2	13	System	2021-04-21T12:37:49.940Z	System	2021-04-21T12:37:49.940Z	\N
808	1	6	6ebc78ab-ad48-49cb-ab29-2452de27733a	15	75	2	13	System	2021-04-21T12:37:49.942Z	System	2021-04-21T12:37:49.942Z	\N
809	1	6	efa8b402-ec9c-480e-b860-6d58c3aeef36	15	75	2	13	System	2021-04-21T12:37:49.944Z	System	2021-04-21T12:37:49.944Z	\N
810	1	6	4cd31a9d-108c-4501-8397-3a5ab3ab48dd	15	79	2	13	System	2021-04-21T12:37:49.947Z	System	2021-04-21T12:37:49.947Z	\N
811	1	6	6727ceb5-0912-43c7-9881-543355a7dff8	15	75	2	13	System	2021-04-21T12:37:49.951Z	System	2021-04-21T12:37:49.951Z	\N
812	1	6	45ed25f9-e704-4f66-a9c4-e8a547a45a99	15	75	2	13	System	2021-04-21T12:37:49.953Z	System	2021-04-21T12:37:49.953Z	\N
813	1	6	d0f5d516-ca42-4c51-8af0-816991e62fe4	15	75	2	13	System	2021-04-21T12:37:49.955Z	System	2021-04-21T12:37:49.955Z	\N
814	1	6	1b68e84a-fcac-46a6-a800-655e98daf083	15	75	2	13	System	2021-04-21T12:37:49.957Z	System	2021-04-21T12:37:49.957Z	\N
815	1	6	43fe4a61-1a5f-4c95-885c-c80849af77d1	15	75	2	13	System	2021-04-21T12:37:49.959Z	System	2021-04-21T12:37:49.959Z	\N
816	1	6	26285c1f-dd45-46e7-a631-e5dc80b307e3	15	75	2	13	System	2021-04-21T12:37:49.962Z	System	2021-04-21T12:37:49.962Z	\N
817	1	6	fd1cebc9-b061-48f0-8d23-37d376419972	15	75	2	13	System	2021-04-21T12:37:49.964Z	System	2021-04-21T12:37:49.964Z	\N
818	1	6	0b6788d9-198f-4984-8fcd-a170908143cb	15	75	2	13	System	2021-04-21T12:37:49.966Z	System	2021-04-21T12:37:49.966Z	\N
819	1	6	1d687586-fe70-4b90-acba-7842f82fb999	15	75	2	13	System	2021-04-21T12:37:49.968Z	System	2021-04-21T12:37:49.968Z	\N
820	1	6	d5a819a4-0c86-4528-a143-b2a5dec34885	15	75	2	13	System	2021-04-21T12:37:49.971Z	System	2021-04-21T12:37:49.971Z	\N
821	1	6	d9a43336-c721-480e-ac48-aca8209bc768	15	75	2	13	System	2021-04-21T12:37:49.973Z	System	2021-04-21T12:37:49.973Z	\N
822	1	6	865c5489-4088-415e-903d-480e46eee4f9	15	75	2	13	System	2021-04-21T12:37:49.974Z	System	2021-04-21T12:37:49.974Z	\N
823	1	6	950f1ea6-02db-4d4f-a862-0afd4c1c9874	15	75	2	13	System	2021-04-21T12:37:49.976Z	System	2021-04-21T12:37:49.976Z	\N
824	1	6	a12ee596-92f2-4d27-aea2-c5d2fde72655	15	75	2	13	System	2021-04-21T12:37:49.978Z	System	2021-04-21T12:37:49.978Z	\N
825	1	6	8c54eb84-0206-4083-9704-bc4ef5c5ba70	15	75	2	13	System	2021-04-21T12:37:49.980Z	System	2021-04-21T12:37:49.980Z	\N
826	1	6	57cb51e4-6e90-4a44-af53-ccfe5433f38b	15	75	2	13	System	2021-04-21T12:37:49.982Z	System	2021-04-21T12:37:49.982Z	\N
827	1	6	b796d0a8-4bab-4133-b3e0-23dd30aac365	15	75	2	13	System	2021-04-21T12:37:49.984Z	System	2021-04-21T12:37:49.984Z	\N
828	1	6	3976c3c1-e40f-4c98-9352-04007dcfd0c5	15	75	2	13	System	2021-04-21T12:37:49.986Z	System	2021-04-21T12:37:49.986Z	\N
829	1	6	ffcd8391-9381-45a8-87af-757158247c90	15	75	2	13	System	2021-04-21T12:37:49.988Z	System	2021-04-21T12:37:49.988Z	\N
830	1	6	4de43e5b-0519-4a8f-905b-bed96a0f89d0	15	75	2	13	System	2021-04-21T12:37:49.990Z	System	2021-04-21T12:37:49.990Z	\N
831	1	6	619024bf-ef7f-4705-84bf-9d113634e616	15	79	2	13	System	2021-04-21T12:37:49.993Z	System	2021-04-21T12:37:49.993Z	\N
832	1	6	cfd4f8a3-4b9b-4814-8d54-5e72dffa4a41	15	75	2	13	System	2021-04-21T12:37:49.996Z	System	2021-04-21T12:37:49.996Z	\N
833	1	6	30335743-f862-43e6-a06f-a65261ce2ce6	15	75	2	13	System	2021-04-21T12:37:49.998Z	System	2021-04-21T12:37:49.998Z	\N
834	1	6	a01e8570-8453-46d6-b81c-85ce6e2bd8b6	15	75	2	13	System	2021-04-21T12:37:50.001Z	System	2021-04-21T12:37:50.001Z	\N
835	1	6	7a2a1fb4-87bc-4c22-aa3a-95754dd9226e	15	75	2	13	System	2021-04-21T12:37:50.003Z	System	2021-04-21T12:37:50.003Z	\N
836	1	6	467592de-b25a-4cf5-80c8-005a878f0a6d	15	75	2	13	System	2021-04-21T12:37:50.006Z	System	2021-04-21T12:37:50.006Z	\N
837	1	6	0bafae3e-8ca3-405a-8a5b-580ebeb30130	15	75	2	13	System	2021-04-21T12:37:50.008Z	System	2021-04-21T12:37:50.008Z	\N
838	1	6	0047c43d-e599-4f74-b82a-994551f97551	15	75	2	13	System	2021-04-21T12:37:50.011Z	System	2021-04-21T12:37:50.011Z	\N
839	1	6	872394ab-5b8d-4d93-adc2-e0248df4003f	15	75	2	13	System	2021-04-21T12:37:50.013Z	System	2021-04-21T12:37:50.013Z	\N
840	1	6	3ae8b1a1-9e40-4a1e-b5f5-38568879bd47	15	75	2	13	System	2021-04-21T12:37:50.016Z	System	2021-04-21T12:37:50.016Z	\N
841	1	6	31a58140-b260-4994-85b4-31b8c854a623	15	75	2	13	System	2021-04-21T12:37:50.018Z	System	2021-04-21T12:37:50.018Z	\N
842	1	6	9ce94cbb-151f-4962-bae6-37af4f1544a6	15	75	2	13	System	2021-04-21T12:37:50.020Z	System	2021-04-21T12:37:50.020Z	\N
843	1	6	695114fb-6b6f-47b1-8ea2-f2b2e3cf13fe	15	75	2	13	System	2021-04-21T12:37:50.022Z	System	2021-04-21T12:37:50.022Z	\N
844	1	6	df207dde-1a2b-4658-9a32-54b03a5069ca	15	79	2	13	System	2021-04-21T12:37:50.025Z	System	2021-04-21T12:37:50.025Z	\N
845	1	6	06ba8798-b1d7-4eba-8b5e-15bc047876f8	15	75	2	13	System	2021-04-21T12:37:50.030Z	System	2021-04-21T12:37:50.030Z	\N
846	1	6	ff70eb33-e868-445e-b1a4-d9a03e2a64a0	15	75	2	13	System	2021-04-21T12:37:50.033Z	System	2021-04-21T12:37:50.033Z	\N
847	1	6	c71a0093-ad1c-4922-b11f-d60fbf7d6929	15	75	2	13	System	2021-04-21T12:37:50.035Z	System	2021-04-21T12:37:50.035Z	\N
848	1	6	40ed08a5-8285-4206-a092-4318563c5597	15	75	2	13	System	2021-04-21T12:37:50.038Z	System	2021-04-21T12:37:50.038Z	\N
849	1	6	0871d5e2-3d7d-4205-ba46-41976b59184e	15	75	2	13	System	2021-04-21T12:37:50.040Z	System	2021-04-21T12:37:50.040Z	\N
850	1	6	8a71f52b-618d-4480-b4e6-699cb51679ec	15	75	2	13	System	2021-04-21T12:37:50.042Z	System	2021-04-21T12:37:50.042Z	\N
851	1	6	2f8a6474-d223-4ca7-845e-ac4e80c7fad2	15	75	2	13	System	2021-04-21T12:37:50.045Z	System	2021-04-21T12:37:50.045Z	\N
852	1	6	367488af-bebe-41cd-80cc-acc29b26aef7	15	75	2	13	System	2021-04-21T12:37:50.047Z	System	2021-04-21T12:37:50.047Z	\N
853	1	6	9e1e6e39-f241-4dd2-97e2-cb3b61150b7b	15	75	2	13	System	2021-04-21T12:37:50.049Z	System	2021-04-21T12:37:50.049Z	\N
854	1	6	58a333a6-b643-42a3-8cd4-b3bcc9a45a65	15	75	2	13	System	2021-04-21T12:37:50.051Z	System	2021-04-21T12:37:50.051Z	\N
855	1	6	d9f9732f-697b-4668-afed-6504a88f5c32	15	75	2	13	System	2021-04-21T12:37:50.053Z	System	2021-04-21T12:37:50.053Z	\N
856	1	6	61d24257-7969-424d-9135-510697b82bd0	15	75	2	13	System	2021-04-21T12:37:50.054Z	System	2021-04-21T12:37:50.054Z	\N
857	1	6	4ea56c5e-3046-43bd-a673-1ba7296f0ac4	15	75	2	13	System	2021-04-21T12:37:50.056Z	System	2021-04-21T12:37:50.056Z	\N
858	1	6	428bec4a-d8ef-4276-aaa7-e5d888d0ff4e	15	75	2	13	System	2021-04-21T12:37:50.059Z	System	2021-04-21T12:37:50.059Z	\N
859	1	6	1fc77cdc-fd33-4ae2-a0f0-5e9fff91a684	15	75	2	13	System	2021-04-21T12:37:50.061Z	System	2021-04-21T12:37:50.061Z	\N
860	1	6	b530860e-29af-46ab-bd05-7ae75b81333e	15	75	2	13	System	2021-04-21T12:37:50.063Z	System	2021-04-21T12:37:50.063Z	\N
861	1	6	3c11ae01-4f86-4cd0-9193-653c2b71f3a0	15	75	2	13	System	2021-04-21T12:37:50.066Z	System	2021-04-21T12:37:50.066Z	\N
862	1	6	eb2fd115-13d4-4935-9a76-ff4f1e60e8d8	15	75	2	13	System	2021-04-21T12:37:50.067Z	System	2021-04-21T12:37:50.067Z	\N
863	1	6	726ae34a-4cd3-4f3e-bbca-c2bc99ab8f99	15	75	2	13	System	2021-04-21T12:37:50.070Z	System	2021-04-21T12:37:50.070Z	\N
864	1	6	ed85511b-4dc9-42e2-9773-b2842c6967f9	15	75	2	13	System	2021-04-21T12:37:50.071Z	System	2021-04-21T12:37:50.071Z	\N
865	1	6	solr_facets_root_space	17	230	2	13	System	2021-04-21T12:37:50.488Z	System	2021-04-21T12:37:50.488Z	\N
1115	4	6	299d640d-c74e-4037-90b9-c0401db34954	419	51	2	11	User10	2020-05-10T16:39:50Z	admin	2021-04-21T12:42:29.691Z	2020-08-12T04:01:44Z
1126	4	6	169e753c-f1df-4980-9418-c98860cc8ccb	435	51	2	11	User2	2020-01-06T06:48:44Z	admin	2021-04-21T12:42:29.950Z	2020-09-18T14:45:56Z
1130	4	6	cf2d66f9-9b26-404c-88c3-b3a6df53c48a	442	51	2	11	User10	2020-02-09T16:41:42Z	admin	2021-04-21T12:42:30.099Z	2020-09-16T04:02:59Z
1131	4	6	9045a9cf-f1f2-4d89-b1ea-6d50024ded59	449	51	2	11	User6	2020-01-03T05:51:17Z	admin	2021-04-21T12:42:30.243Z	2020-02-04T04:15:38Z
1139	1	6	e20d08e9-1a91-449e-91fb-3c6aa3043028	449	47	2	11	User6	2020-01-03T05:51:17Z	admin	2021-04-21T12:42:30.243Z	\N
1140	1	6	2e2996de-eea1-4d48-a256-ceb329de5760	449	47	2	11	User6	2020-01-03T05:51:17Z	admin	2021-04-21T12:42:30.243Z	\N
12147483647	1	6	9c724bbd-9249-4a96-9661-2855e623da80	449	47	2	11	User6	2020-01-03T05:51:17Z	admin	2021-04-21T12:42:30.243Z	\N
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
475	30
475	85
476	25
476	30
476	85
484	25
485	25
32	95
486	30
487	30
488	25
488	30
488	31
489	25
489	30
489	31
490	25
490	53
490	30
490	54
491	25
491	53
491	30
491	54
492	25
492	30
492	31
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
498	25
498	30
494	58
499	25
500	25
500	30
501	25
501	80
502	25
503	25
504	25
505	25
506	25
507	25
507	30
508	25
508	30
508	53
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
537	53
537	30
537	54
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
581	30
582	25
582	30
582	53
582	54
7	88
583	25
583	53
583	30
583	54
586	25
587	95
588	95
589	95
590	95
593	44
593	133
593	134
594	25
594	139
594	140
595	25
595	88
595	145
596	44
596	133
596	134
597	25
597	139
597	140
598	25
598	88
598	145
593	95
596	95
600	25
600	44
600	109
600	30
600	110
600	111
601	25
601	44
601	153
601	30
601	111
602	25
602	30
602	44
602	154
603	25
603	30
603	44
604	25
604	161
604	162
604	44
604	30
604	54
604	140
604	163
605	25
606	25
606	166
606	140
606	161
606	30
606	44
606	162
606	163
606	54
607	25
607	44
607	145
608	25
608	44
608	145
609	25
609	30
609	44
610	25
610	44
610	193
610	30
610	140
610	54
611	25
611	44
611	145
612	25
612	44
612	193
612	30
612	140
612	54
613	25
613	44
613	145
614	25
614	44
614	193
614	30
614	140
614	54
615	25
615	44
615	145
616	25
616	44
616	145
617	25
617	44
617	193
617	30
617	140
617	54
618	25
618	44
618	145
619	25
619	44
619	193
619	30
619	140
619	54
620	25
620	44
620	145
621	25
621	44
621	193
621	30
621	140
621	54
622	25
622	44
622	145
623	25
623	44
623	193
623	30
623	140
623	54
624	25
624	44
624	145
625	25
625	44
625	193
625	30
625	140
625	54
626	25
626	44
626	145
627	25
627	44
627	193
627	30
627	140
627	54
628	25
628	44
628	145
629	25
629	44
629	193
629	30
629	140
629	54
630	25
630	44
630	145
631	25
631	44
631	193
631	30
631	140
631	54
632	25
632	44
632	145
633	25
633	30
633	44
634	25
634	44
634	193
634	30
634	162
634	140
634	163
634	54
635	25
635	44
635	145
636	25
636	44
636	193
636	30
636	162
636	140
636	163
636	54
637	25
637	44
637	145
638	25
638	30
638	44
639	25
639	44
639	193
639	30
639	140
639	54
640	25
640	44
640	145
641	25
641	44
641	193
641	30
641	140
641	54
642	25
642	44
642	145
643	25
643	44
643	145
644	25
644	44
644	193
644	30
644	162
644	140
644	163
644	54
645	25
645	44
645	145
646	25
646	44
646	145
647	25
647	30
647	44
648	25
648	44
648	30
648	54
648	154
649	25
649	30
649	44
649	154
650	25
650	30
650	44
651	25
651	44
651	193
651	30
651	140
651	54
652	25
652	44
652	145
653	25
653	44
653	193
653	30
653	140
653	54
654	25
654	44
654	145
655	25
655	44
655	30
655	54
655	140
655	194
656	25
656	44
656	145
657	25
657	44
657	145
658	25
658	44
658	31
659	25
659	44
660	25
660	44
660	30
661	25
661	30
661	44
662	25
662	44
662	30
662	140
663	25
663	44
663	145
664	25
664	44
664	145
665	25
665	44
665	30
665	140
666	25
666	44
666	145
667	25
667	44
667	145
668	25
668	44
668	30
668	140
669	25
669	44
669	145
670	25
670	44
670	145
671	25
671	30
671	44
672	25
672	44
672	30
672	140
673	25
673	44
673	145
674	25
674	44
674	145
675	25
675	44
675	30
675	140
676	25
676	44
676	145
677	25
677	44
677	145
678	25
678	44
678	153
678	111
679	25
679	44
679	154
680	25
680	44
680	154
681	25
681	44
681	153
681	30
681	111
682	25
682	30
682	44
683	25
683	44
683	30
683	211
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
687	30
687	44
688	25
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
693	153
693	111
694	25
694	44
694	161
694	30
694	154
695	25
696	25
696	166
696	161
696	44
696	30
696	154
697	25
697	44
697	161
697	30
697	154
698	25
699	25
699	166
699	161
699	44
699	30
699	154
700	25
700	44
700	161
700	30
700	154
701	25
702	25
702	166
702	161
702	44
702	30
702	154
703	25
703	44
703	153
703	111
704	25
704	44
704	154
705	25
705	44
705	30
705	219
706	25
706	44
706	30
706	219
706	220
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
709	154
710	25
710	44
710	30
710	219
711	25
711	44
711	30
711	219
711	220
712	25
712	88
712	85
713	25
713	88
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
596	226
593	226
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
865	25
865	88
865	30
866	25
866	88
866	44
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
872	44
872	88
873	25
873	88
874	25
874	88
875	25
875	88
873	44
874	44
875	44
876	25
876	44
876	88
877	25
877	30
877	53
877	140
879	25
879	88
879	145
877	139
881	25
881	88
881	232
881	145
894	25
903	44
904	25
904	44
903	95
906	25
906	109
906	30
906	110
906	111
907	95
908	95
909	95
910	95
911	95
912	25
912	88
912	44
912	85
913	25
913	88
913	44
914	25
914	88
914	44
915	25
915	88
915	44
916	25
916	88
916	44
917	25
917	88
917	44
918	25
918	88
918	44
919	25
919	88
919	44
920	25
920	88
920	44
921	25
921	88
921	44
922	25
922	88
922	44
923	25
923	153
923	111
903	134
924	25
925	25
925	253
925	161
926	25
927	25
927	166
927	253
927	161
931	25
932	25
936	25
936	268
936	269
936	30
936	54
936	271
936	273
940	25
940	268
940	269
940	30
940	54
940	271
1018	25
1018	268
1018	269
1018	30
1018	54
1018	271
1018	273
1022	25
1022	268
1022	269
1022	30
1022	54
1022	271
1022	273
1026	25
1026	268
1026	269
1026	30
1026	54
1026	271
1026	273
1030	25
1030	268
1030	269
1030	30
1030	54
1030	271
1030	273
1035	25
1035	268
1035	269
1035	30
1035	54
1035	271
1035	273
1038	25
1038	268
1038	269
1038	30
1038	54
1038	271
1038	273
1039	25
1039	268
1039	269
1039	30
1039	54
1039	271
1039	273
1044	25
1044	268
1044	269
1044	30
1044	54
1044	271
1044	273
1048	25
1048	268
1048	269
1048	30
1048	54
1048	271
1047	273
1052	25
1052	268
1052	269
1052	30
1052	54
1052	271
1052	273
1057	273
1078	25
1078	268
1078	269
1078	30
1078	54
1078	271
1078	273
1091	273
1095	25
1095	268
1095	269
1095	30
1095	54
1095	271
1095	273
1099	25
1099	268
1099	269
1099	30
1099	54
1099	271
1100	273
1106	25
1106	268
1106	269
1106	30
1106	54
1106	271
1106	273
1110	25
1110	268
1110	269
1110	30
1110	54
894	54
894	30
897	54
897	30
900	54
900	30
945	25
945	268
945	269
945	30
945	54
945	271
959	25
959	268
959	269
959	30
959	54
959	271
959	273
965	25
965	268
965	269
965	30
965	54
965	271
965	273
979	273
982	25
982	268
982	269
982	30
982	54
982	271
982	273
986	25
986	268
986	269
986	30
986	54
986	271
986	273
990	25
990	268
990	269
990	30
990	54
990	271
990	273
994	25
994	268
994	269
994	30
994	54
994	271
994	273
998	25
998	268
998	269
998	30
998	54
998	271
1014	25
1014	268
1014	269
1014	30
1014	54
1014	271
1014	273
1017	25
1017	268
1017	269
1017	30
1017	54
1017	271
1017	273
1021	25
1021	268
1021	269
1021	30
1021	54
1021	271
1021	273
1025	25
1025	268
1025	269
1025	30
1025	54
1025	271
1025	273
1029	25
1029	268
1029	269
1029	30
1029	54
1029	271
1029	273
1033	25
1033	268
1033	269
1033	30
1033	54
1033	271
1033	273
1037	25
1037	268
1037	269
1037	30
1037	54
1037	271
1037	273
1041	25
1041	268
1041	269
1041	30
1041	54
1041	271
1041	273
1046	25
1046	268
1046	269
1046	30
1046	54
1046	271
1046	273
1049	25
1049	268
1049	269
1049	30
1049	54
1049	271
1051	273
1056	25
1056	268
1056	269
1056	30
1056	54
1056	271
1056	273
1059	25
1059	268
1059	269
1059	30
1059	54
1059	271
1062	273
1065	25
1065	268
1065	269
1065	30
1065	54
1065	271
1081	273
1086	25
1086	268
1086	269
1086	30
1086	54
1086	271
1097	273
1101	25
1101	268
1101	269
1101	30
1101	54
1101	271
1101	273
1104	25
1104	268
1104	269
1104	30
1104	54
1104	271
1109	273
1113	25
1113	268
1113	269
1113	30
1113	54
1113	271
1113	273
1118	25
1118	268
887	25
887	234
887	139
887	140
887	44
887	53
887	30
11	239
889	25
889	88
889	145
890	25
890	88
890	232
890	145
897	25
897	161
898	25
899	25
899	166
899	161
942	25
942	268
942	269
942	30
942	54
942	271
942	273
944	25
944	268
944	269
944	30
944	54
944	271
944	273
948	25
948	268
948	269
948	30
948	54
948	271
948	273
952	25
952	268
952	269
952	30
952	54
952	271
952	273
956	25
956	268
956	269
956	30
956	54
956	271
956	273
960	25
960	268
960	269
960	30
960	54
960	271
977	25
977	268
977	269
977	30
977	54
977	271
977	273
981	25
981	268
981	269
981	30
981	54
981	271
981	273
985	25
985	268
985	269
985	30
985	54
985	271
985	273
989	25
989	268
989	269
989	30
989	54
989	271
989	273
1001	25
1001	268
1001	269
1001	30
1001	54
1001	271
1006	25
1006	268
1006	269
1006	30
1006	54
1006	271
1031	273
1034	25
1034	268
1034	269
1034	30
1034	54
1034	271
1034	273
1073	25
1073	268
1073	269
1073	30
1073	54
1073	271
1084	273
1088	25
1088	268
1088	269
1088	30
1088	54
1088	271
1088	273
1091	25
1091	268
1091	269
1091	30
1091	54
1091	271
1093	273
1097	25
1097	268
1097	269
1097	30
1097	54
1097	271
1096	273
1100	25
1100	268
1100	269
1100	30
1100	54
1100	271
1103	273
1109	25
1109	268
1109	269
1109	30
1109	54
1109	271
1110	273
1114	25
1114	268
1114	269
1114	30
1114	54
1114	271
1114	273
1117	25
1117	268
1117	269
1117	30
1117	54
1117	271
1137	25
1137	268
1137	269
1137	30
1137	54
1137	271
1135	273
894	161
895	25
896	25
896	166
896	161
900	25
900	161
901	25
902	25
902	166
902	161
925	54
925	30
928	25
928	253
928	161
929	25
930	25
930	166
930	253
930	161
928	54
928	30
933	25
934	25
935	25
941	25
943	25
941	268
941	269
941	30
941	54
941	271
943	268
943	269
943	30
943	54
943	271
941	273
943	273
940	273
946	25
947	25
947	268
947	269
947	30
947	54
947	271
946	268
946	269
946	30
946	54
946	271
946	273
945	273
947	273
949	25
950	25
951	25
949	268
949	269
949	30
949	54
949	271
950	268
950	269
950	30
951	268
950	54
951	269
950	271
951	30
951	54
951	271
950	273
951	273
949	273
953	25
954	25
955	25
953	268
953	269
953	30
953	54
953	271
955	268
955	269
955	30
955	54
955	271
954	268
954	269
954	30
954	54
954	271
955	273
954	273
957	25
953	273
957	268
957	269
957	30
957	54
957	271
958	25
958	268
958	269
958	30
958	54
958	271
957	273
961	25
958	273
961	268
961	269
961	30
961	54
961	271
962	25
963	25
962	268
962	269
962	30
962	54
962	271
963	268
963	269
963	30
963	54
963	271
960	273
961	273
964	25
964	268
964	269
964	30
964	54
964	271
962	273
963	273
966	25
966	268
966	269
966	30
966	54
966	271
967	25
967	268
967	269
967	30
967	54
967	271
964	273
968	25
969	25
966	273
968	268
968	269
968	30
968	54
968	271
969	268
969	269
969	30
969	54
969	271
970	25
967	273
970	268
970	269
970	30
970	54
970	271
971	25
971	268
971	269
971	30
971	54
971	271
968	273
969	273
972	25
973	25
972	268
972	269
972	30
972	54
972	271
972	273
976	25
976	268
976	269
976	30
976	54
976	271
976	273
980	25
980	268
980	269
980	30
980	54
980	271
980	273
984	25
984	268
984	269
984	30
984	54
984	271
984	273
988	25
988	268
988	269
988	30
988	54
988	271
988	273
992	25
992	268
992	269
992	30
992	54
992	271
992	273
996	25
996	268
996	269
996	30
996	54
996	271
996	273
999	25
999	268
999	269
999	30
999	54
999	271
999	273
1003	25
1003	268
1003	269
1003	30
1003	54
1003	271
1003	273
1007	25
1007	268
1007	269
1007	30
1007	54
1007	271
1007	273
1011	25
1011	268
1011	269
1011	30
1011	54
1011	271
1011	273
1015	25
1015	268
1015	269
1015	30
1015	54
1015	271
1015	273
1019	25
1019	268
1019	269
1019	30
1019	54
1019	271
1019	273
1023	25
1023	268
1023	269
1023	30
1023	54
1023	271
1023	273
1027	25
1027	268
1027	269
1027	30
1027	54
1027	271
1042	25
1042	268
1042	269
1042	30
1042	54
1042	271
1050	273
1054	25
1054	268
1054	269
1054	30
1054	54
1054	271
1057	25
1057	268
1057	269
1057	30
1057	54
1057	271
1068	273
1074	25
1074	268
1074	269
1074	30
1074	54
1074	271
1074	273
1076	25
1076	268
1076	269
1076	30
1076	54
1076	271
1076	273
1080	25
1080	268
1080	269
1080	30
1080	54
1080	271
1080	273
1084	25
1084	268
1084	269
1084	30
1084	54
1084	271
1094	273
1098	25
1098	268
1098	269
1098	30
1098	54
1098	271
1119	273
1123	25
1123	268
1123	269
1123	30
1123	54
1123	271
1123	273
1127	25
1127	268
1127	269
1127	30
1127	54
1127	271
1129	273
973	268
973	269
973	30
973	54
973	271
973	273
979	25
979	268
979	269
979	30
979	54
979	271
998	273
1002	25
1002	268
1002	269
1002	30
1002	54
1002	271
1002	273
1005	25
1005	268
1005	269
1005	30
1005	54
1005	271
1005	273
1009	25
1009	268
1009	269
1009	30
1009	54
1009	271
1009	273
1062	25
1062	268
1062	269
1062	30
1062	54
1062	271
1059	273
1064	25
1064	268
1064	269
1064	30
1064	54
1064	271
1064	273
1068	25
1068	268
1068	269
1068	30
1068	54
1068	271
1067	273
1073	273
1075	25
1075	268
1075	269
1075	30
1075	54
1075	271
1075	273
1079	25
1079	268
1079	269
1079	30
1079	54
1079	271
1079	273
1085	25
1085	268
1085	269
1085	30
1085	54
1085	271
1086	273
1090	25
1090	268
1090	269
1090	30
1090	54
1090	271
1090	273
1102	273
1105	25
1105	268
1105	269
1105	30
1105	54
1105	271
1105	273
1108	25
1108	268
1108	269
1108	30
1108	54
1108	271
1127	273
1132	25
1132	268
1132	269
1132	30
1132	54
1132	271
1132	273
1138	25
1138	268
1138	269
1138	30
1138	54
1138	271
1138	273
970	273
974	25
974	268
974	269
974	30
974	54
974	271
974	273
993	25
993	268
993	269
993	30
993	54
993	271
993	273
997	25
997	268
997	269
997	30
997	54
997	271
997	273
1001	273
1006	273
1010	25
1010	268
1010	269
1010	30
1010	54
1010	271
1010	273
1013	25
1013	268
1013	269
1013	30
1013	54
1013	271
1013	273
1027	273
1031	25
1031	268
1031	269
1031	30
1031	54
1031	271
1040	273
1043	25
1043	268
1043	269
1043	30
1043	54
1043	271
1043	273
1047	25
1047	268
1047	269
1047	30
1047	54
1047	271
1048	273
1051	25
1051	268
1051	269
1051	30
1051	54
1051	271
1054	273
1058	25
1058	268
1058	269
1058	30
1058	54
1058	271
1058	273
1061	25
1061	268
1061	269
1061	30
1061	54
1061	271
1061	273
1065	273
1067	25
1067	268
1067	269
1067	30
1067	54
1067	271
1082	25
1082	268
1082	269
1082	30
1082	54
1082	271
1082	273
1083	25
1083	268
1083	269
1083	30
1083	54
1083	271
1083	273
1087	25
1087	268
1087	269
1087	30
1087	54
1087	271
1087	273
1092	25
1092	268
1092	269
1092	30
1092	54
1092	271
1092	273
1096	25
1096	268
1096	269
1096	30
1096	54
1096	271
1098	273
1102	25
1102	268
1102	269
1102	30
1102	54
1102	271
1099	273
1103	25
1103	268
1103	269
1103	30
1103	54
1103	271
1104	273
1107	25
1107	268
1107	269
1107	30
1107	54
1107	271
1107	273
1111	25
1111	268
1111	269
1111	30
1111	54
1111	271
1111	273
1118	273
1121	25
1121	268
1121	269
1121	30
1121	54
1121	271
971	273
975	25
975	268
975	269
975	30
975	54
975	271
975	273
978	25
978	268
978	269
978	30
978	54
978	271
978	273
983	25
983	268
983	269
983	30
983	54
983	271
983	273
987	25
987	268
987	269
987	30
987	54
987	271
987	273
991	25
991	268
991	269
991	30
991	54
991	271
991	273
995	25
995	268
995	269
995	30
995	54
995	271
995	273
1000	25
1000	268
1000	269
1000	30
1000	54
1000	271
1000	273
1004	25
1004	268
1004	269
1004	30
1004	54
1004	271
1004	273
1008	25
1008	268
1008	269
1008	30
1008	54
1008	271
1008	273
1012	25
1012	268
1012	269
1012	30
1012	54
1012	271
1012	273
1016	25
1016	268
1016	269
1016	30
1016	54
1016	271
1016	273
1020	25
1020	268
1020	269
1020	30
1020	54
1020	271
1020	273
1024	25
1024	268
1024	269
1024	30
1024	54
1024	271
1024	273
1028	25
1028	268
1028	269
1028	30
1028	54
1028	271
1028	273
1032	25
1032	268
1032	269
1032	30
1032	54
1032	271
1032	273
1036	25
1036	268
1036	269
1036	30
1036	54
1036	271
1036	273
1040	25
1040	268
1040	269
1040	30
1040	54
1040	271
1066	25
1066	268
1066	269
1066	30
1066	54
1066	271
1066	273
1070	25
1070	268
1070	269
1070	30
1070	54
1070	271
1070	273
1071	25
1071	268
1071	269
1071	30
1071	54
1071	271
1071	273
1077	25
1077	268
1077	269
1077	30
1077	54
1077	271
1077	273
1081	25
1081	268
1081	269
1081	30
1081	54
1081	271
1085	273
1089	25
1089	268
1089	269
1089	30
1089	54
1089	271
1089	273
1093	25
1093	268
1093	269
1093	30
1093	54
1093	271
1122	25
1122	268
1122	269
1122	30
1122	54
1122	271
1120	273
1124	25
1124	268
1124	269
1124	30
1124	54
1124	271
1124	273
1128	25
1128	268
1128	269
1128	30
1042	273
1045	25
1045	268
1045	269
1045	30
1045	54
1045	271
1045	273
1050	25
1050	268
1050	269
1050	30
1050	54
1050	271
1049	273
1053	25
1053	268
1053	269
1053	30
1053	54
1053	271
1053	273
1055	25
1055	268
1055	269
1055	30
1055	54
1055	271
1055	273
1060	25
1060	268
1060	269
1060	30
1060	54
1060	271
1060	273
1063	25
1063	268
1063	269
1063	30
1063	54
1063	271
1063	273
1069	25
1069	268
1069	269
1069	30
1069	54
1069	271
1069	273
1072	25
1072	268
1072	269
1072	30
1072	54
1072	271
1072	273
1094	25
1094	268
1094	269
1094	30
1094	54
1094	271
1108	273
1112	25
1112	268
1112	269
1112	30
1112	54
1112	271
1112	273
1116	25
1116	268
1116	269
1116	30
1116	54
1116	271
1116	273
1110	271
1117	273
1119	25
1119	268
1119	269
1119	30
1119	54
1119	271
1122	273
1125	25
1125	268
1125	269
1125	30
1125	54
1125	271
1125	273
1129	25
1129	268
1129	269
1129	30
1129	54
1129	271
1115	25
1115	268
1115	269
1115	30
1115	54
1115	271
1126	25
1126	268
1126	269
1126	30
1126	54
1126	271
1126	273
1130	25
1130	268
1130	269
1130	30
1130	54
1130	271
1130	273
1131	25
1131	268
1131	269
1131	30
1131	54
1131	271
1118	269
1118	30
1118	54
1118	271
1115	273
1120	25
1120	268
1120	269
1120	30
1120	54
1120	271
1121	273
1134	25
1134	268
1134	269
1134	30
1134	54
1134	271
1134	273
1128	54
1128	271
1128	273
1133	25
1133	268
1133	269
1133	30
1133	54
1133	271
1131	273
1133	273
1135	25
1136	25
1136	268
1135	268
1135	269
1135	30
1136	269
1135	54
1135	271
1136	30
1136	54
1136	271
1137	273
1136	273
1139	25
1140	25
12147483647	25
384	154
385	154
\.


--
-- Data for Name: alf_node_assoc; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_node_assoc (id, version, source_node_id, target_node_id, type_qname_id, assoc_index) FROM stdin;
1	1	593	594	146	1
2	1	596	597	146	1
3	1	605	606	177	1
4	1	695	696	177	1
5	1	698	699	177	1
6	1	701	702	177	1
7	1	708	707	223	1
8	1	707	706	223	1
9	1	706	705	223	1
10	1	689	655	224	1
11	1	691	604	224	1
12	1	684	655	225	1
13	1	688	672	224	1
14	1	692	648	224	1
15	1	711	710	223	1
16	1	895	896	177	1
17	1	898	899	177	1
18	1	901	902	177	1
19	1	926	927	177	1
20	1	929	930	177	1
\.


--
-- Data for Name: alf_node_properties; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_node_properties (node_id, actual_type_n, persisted_type_n, boolean_value, long_value, float_value, double_value, string_value, serializable_value, qname_id, list_index, locale_id) FROM stdin;
4	1	1	t	0	0	0	\N	\N	6	-1	1
4	1	1	f	0	0	0	\N	\N	7	-1	1
4	1	1	f	0	0	0	\N	\N	8	-1	1
4	6	6	f	0	0	0	admin	\N	9	-1	1
4	6	6	f	0	0	0	209c6174da490caeb422f3fa5a7ae634	\N	10	-1	1
4	1	1	f	0	0	0	\N	\N	11	-1	1
6	6	6	f	0	0	0		\N	17	-1	1
6	2	3	f	15000	0	0	\N	\N	18	-1	1
6	6	6	f	0	0	0	1	\N	19	-1	1
6	6	6	f	0	0	0	7	\N	13	-1	1
6	6	6	f	0	0	0	re0bf8708-blocal	\N	14	-1	1
6	6	6	f	0	0	0	0	\N	15	-1	1
6	6	6	f	0	0	0		\N	16	-1	1
7	6	6	f	0	0	0		\N	17	-1	1
7	2	3	f	15000	0	0	\N	\N	18	-1	1
7	6	6	f	0	0	0	1	\N	19	-1	1
7	6	6	f	0	0	0	7	\N	13	-1	1
7	6	6	f	0	0	0	re0bf8708-blocal	\N	14	-1	1
7	6	6	f	0	0	0	0	\N	15	-1	1
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
32	12	6	f	0	0	0	workspace://SpacesStore/952b5f79-98b7-4ed7-aa61-81f6ddfb2151	\N	45	-1	1
33	12	6	f	0	0	0	workspace://SpacesStore/ddf90839-2914-4b07-81c5-b66ced26d99f	\N	45	-1	1
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
384	12	6	f	0	0	0	workspace://SpacesStore/e20d08e9-1a91-449e-91fb-3c6aa3043028	\N	154	0	1
384	12	6	f	0	0	0	workspace://SpacesStore/2e2996de-eea1-4d48-a256-ceb329de5760	\N	154	1	1
385	1	1	t	0	0	0	\N	\N	52	-1	1
385	6	6	f	0	0	0	my_spaces.ftl	\N	26	-1	1
385	6	6	f	0	0	0	Displays a list of spaces in the current user Home Space	\N	27	-1	2
385	6	6	f	0	0	0	my_spaces.ftl	\N	28	-1	2
385	12	6	f	0	0	0	workspace://SpacesStore/e20d08e9-1a91-449e-91fb-3c6aa3043028	\N	154	0	1
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
428	12	6	f	0	0	0	workspace://SpacesStore/0d7b66a1-9b64-4120-a6f5-414eec81f2f7	\N	57	-1	1
452	12	6	f	0	0	0	workspace://SpacesStore/0d7b66a1-9b64-4120-a6f5-414eec81f2f7	\N	57	-1	1
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
475	6	6	f	0	0	0	extensions	\N	26	-1	1
475	6	6	f	0	0	0		\N	27	-1	2
475	2	3	f	0	0	0	\N	\N	82	-1	1
475	1	1	f	0	0	0	\N	\N	83	-1	1
475	1	1	f	0	0	0	\N	\N	84	-1	1
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
486	6	6	f	0	0	0	Remote Credentials	\N	26	-1	1
486	6	6	f	0	0	0	Root folder for Shared Remote Credentials	\N	27	-1	2
486	6	6	f	0	0	0	Remote Credentials	\N	28	-1	2
487	6	6	f	0	0	0	SyncSet Definitions	\N	26	-1	1
487	6	6	f	0	0	0	Root folder for SyncSet Definitions	\N	27	-1	2
487	6	6	f	0	0	0	SyncSet Definitions	\N	28	-1	2
488	6	6	f	0	0	0	Imap Configs	\N	26	-1	1
488	6	6	f	0	0	0	Imap Configs	\N	27	-1	2
488	6	6	f	0	0	0	Imap Configs	\N	28	-1	2
488	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
489	6	6	f	0	0	0	Templates	\N	26	-1	1
489	6	6	f	0	0	0	Templates for IMAP generated messages	\N	27	-1	2
489	6	6	f	0	0	0	Templates	\N	28	-1	2
489	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
490	1	1	t	0	0	0	\N	\N	52	-1	1
490	6	6	f	0	0	0		\N	54	-1	1
490	6	6	f	0	0	0	emailbody-textplain.ftl	\N	26	-1	1
490	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Default version	\N	27	-1	2
490	6	6	f	0	0	0	emailbody-textplain.ftl	\N	28	-1	2
490	21	3	f	68	0	0	\N	\N	51	-1	1
491	1	1	t	0	0	0	\N	\N	52	-1	1
491	6	6	f	0	0	0		\N	54	-1	1
491	6	6	f	0	0	0	emailbody-texthtml.ftl	\N	26	-1	1
491	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Default version	\N	27	-1	2
491	6	6	f	0	0	0	emailbody-texthtml.ftl	\N	28	-1	2
491	21	3	f	69	0	0	\N	\N	51	-1	1
492	6	6	f	0	0	0	Transfers	\N	26	-1	1
492	6	6	f	0	0	0	Folder used by the Transfer subsystem	\N	27	-1	2
492	6	6	f	0	0	0	Transfers	\N	28	-1	2
492	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
493	6	6	f	0	0	0	Transfer Target Groups	\N	26	-1	1
493	6	6	f	0	0	0	Folder containing groups of transfer targets	\N	27	-1	2
493	6	6	f	0	0	0	Transfer Target Groups	\N	28	-1	2
493	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
494	6	6	f	0	0	0	Default Group	\N	26	-1	1
494	6	6	f	0	0	0	Put your transfer targets in this folder	\N	27	-1	2
494	6	6	f	0	0	0	Default Group	\N	28	-1	2
494	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
495	6	6	f	0	0	0	Inbound Transfer Records	\N	26	-1	1
495	6	6	f	0	0	0	Folder containing records of inbound transfers	\N	27	-1	2
495	6	6	f	0	0	0	Inbound Transfer Records	\N	28	-1	2
495	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
496	6	6	f	0	0	0	Temp	\N	26	-1	1
496	6	6	f	0	0	0	Folder to store temporary nodes during transfer	\N	27	-1	2
496	6	6	f	0	0	0	Temp	\N	28	-1	2
496	6	6	f	0	0	0	space-icon-default	\N	29	-1	1
497	6	6	f	0	0	0	Rendering Actions Space	\N	26	-1	1
497	6	6	f	0	0	0	A space used by the system to persist rendering actions.	\N	27	-1	2
497	6	6	f	0	0	0	Rendering Actions Space	\N	28	-1	2
498	6	6	f	0	0	0	Replication Actions Space	\N	26	-1	1
498	6	6	f	0	0	0	A space used by the system to persist replication actions.	\N	27	-1	2
498	6	6	f	0	0	0	Replication Actions Space	\N	28	-1	2
500	6	6	f	0	0	0	inbound	\N	65	0	1
500	6	6	f	0	0	0		\N	27	-1	2
500	6	6	f	0	0	0	Specialise child folders into Transfer Targets	\N	28	-1	2
500	1	1	f	0	0	0	\N	\N	62	-1	1
500	1	1	f	0	0	0	\N	\N	63	-1	1
500	1	1	f	0	0	0	\N	\N	64	-1	1
501	0	0	f	0	0	0	\N	\N	98	-1	1
501	7	6	f	0	0	0	2010-08-11T12:06:18.419Z	\N	99	-1	1
501	1	1	f	0	0	0	\N	\N	68	-1	1
501	7	6	f	0	0	0	2010-08-11T12:06:18.408Z	\N	100	-1	1
501	0	0	f	0	0	0	\N	\N	69	-1	1
501	6	6	f	0	0	0	Completed	\N	101	-1	1
501	6	6	f	0	0	0	composite-action	\N	70	-1	1
501	0	0	f	0	0	0	\N	\N	71	-1	1
503	0	0	f	0	0	0	\N	\N	98	-1	1
503	0	0	f	0	0	0	\N	\N	99	-1	1
503	1	1	f	0	0	0	\N	\N	68	-1	1
503	0	0	f	0	0	0	\N	\N	100	-1	1
503	0	0	f	0	0	0	\N	\N	69	-1	1
503	6	6	f	0	0	0	New	\N	101	-1	1
503	6	6	f	0	0	0	specialise-type	\N	70	-1	1
503	0	0	f	0	0	0	\N	\N	71	-1	1
504	15	6	f	0	0	0	{http://www.alfresco.org/model/transfer/1.0}transferTarget	\N	77	-1	1
504	6	6	f	0	0	0	type-name	\N	78	-1	1
505	1	1	f	0	0	0	\N	\N	74	-1	1
505	6	6	f	0	0	0	is-subtype	\N	70	-1	1
506	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}folder	\N	77	-1	1
506	6	6	f	0	0	0	type	\N	78	-1	1
507	6	6	f	0	0	0	Scheduled Actions	\N	26	-1	1
507	6	6	f	0	0	0	Schedule of when persistent actions are executed	\N	27	-1	2
507	6	6	f	0	0	0	Scheduled Actions	\N	28	-1	2
508	1	1	t	0	0	0	\N	\N	52	-1	1
508	6	6	f	0	0	0	new-user-email.html.ftl	\N	26	-1	1
508	6	6	f	0	0	0	Email template used to inform new users of their accounts - Default version	\N	27	-1	2
508	6	6	f	0	0	0	new-user-email.html.ftl	\N	28	-1	2
508	21	3	f	70	0	0	\N	\N	51	-1	1
509	1	1	t	0	0	0	\N	\N	52	-1	1
509	6	6	f	0	0	0	new-user-email_fr.html.ftl	\N	26	-1	1
509	6	6	f	0	0	0	Email template used to inform new users of their accounts - French version	\N	27	-1	2
509	6	6	f	0	0	0	new-user-email_fr.html.ftl	\N	28	-1	2
509	21	3	f	71	0	0	\N	\N	51	-1	1
510	1	1	t	0	0	0	\N	\N	52	-1	1
510	6	6	f	0	0	0	new-user-email_es.html.ftl	\N	26	-1	1
510	6	6	f	0	0	0	Email template used to inform new users of their accounts - Spanish version	\N	27	-1	2
510	6	6	f	0	0	0	new-user-email_es.html.ftl	\N	28	-1	2
510	21	3	f	72	0	0	\N	\N	51	-1	1
511	1	1	t	0	0	0	\N	\N	52	-1	1
511	6	6	f	0	0	0	new-user-email_de.html.ftl	\N	26	-1	1
511	6	6	f	0	0	0	Email template used to inform new users of their accounts - German version	\N	27	-1	2
511	6	6	f	0	0	0	new-user-email_de.html.ftl	\N	28	-1	2
511	21	3	f	73	0	0	\N	\N	51	-1	1
512	1	1	t	0	0	0	\N	\N	52	-1	1
512	6	6	f	0	0	0	new-user-email_it.html.ftl	\N	26	-1	1
512	6	6	f	0	0	0	Email template used to inform new users of their accounts - Italian version	\N	27	-1	2
512	6	6	f	0	0	0	new-user-email_it.html.ftl	\N	28	-1	2
512	21	3	f	74	0	0	\N	\N	51	-1	1
513	1	1	t	0	0	0	\N	\N	52	-1	1
513	6	6	f	0	0	0	new-user-email_ja.html.ftl	\N	26	-1	1
513	6	6	f	0	0	0	Email template used to inform new users of their accounts - Japanese version	\N	27	-1	2
513	6	6	f	0	0	0	new-user-email_ja.html.ftl	\N	28	-1	2
513	21	3	f	75	0	0	\N	\N	51	-1	1
514	1	1	t	0	0	0	\N	\N	52	-1	1
514	6	6	f	0	0	0	new-user-email_nl.html.ftl	\N	26	-1	1
514	6	6	f	0	0	0	Email template used to inform new users of their accounts - Dutch version	\N	27	-1	2
514	6	6	f	0	0	0	new-user-email_nl.html.ftl	\N	28	-1	2
514	21	3	f	76	0	0	\N	\N	51	-1	1
515	1	1	t	0	0	0	\N	\N	52	-1	1
515	6	6	f	0	0	0	invite-email.html.ftl	\N	26	-1	1
515	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - Default version	\N	27	-1	2
515	6	6	f	0	0	0	invite-email.html.ftl	\N	28	-1	2
515	21	3	f	77	0	0	\N	\N	51	-1	1
516	1	1	t	0	0	0	\N	\N	52	-1	1
516	6	6	f	0	0	0	invite-email_fr.html.ftl	\N	26	-1	1
516	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - French version	\N	27	-1	2
516	6	6	f	0	0	0	invite-email_fr.html.ftl	\N	28	-1	2
516	21	3	f	78	0	0	\N	\N	51	-1	1
517	1	1	t	0	0	0	\N	\N	52	-1	1
517	6	6	f	0	0	0	invite-email_es.html.ftl	\N	26	-1	1
517	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - Spanish version	\N	27	-1	2
517	6	6	f	0	0	0	invite-email_es.html.ftl	\N	28	-1	2
517	21	3	f	79	0	0	\N	\N	51	-1	1
518	1	1	t	0	0	0	\N	\N	52	-1	1
518	6	6	f	0	0	0	invite-email_de.html.ftl	\N	26	-1	1
518	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - German version	\N	27	-1	2
518	6	6	f	0	0	0	invite-email_de.html.ftl	\N	28	-1	2
518	21	3	f	80	0	0	\N	\N	51	-1	1
519	1	1	t	0	0	0	\N	\N	52	-1	1
519	6	6	f	0	0	0	invite-email_it.html.ftl	\N	26	-1	1
519	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - Italian version	\N	27	-1	2
519	6	6	f	0	0	0	invite-email_it.html.ftl	\N	28	-1	2
519	21	3	f	81	0	0	\N	\N	51	-1	1
520	1	1	t	0	0	0	\N	\N	52	-1	1
520	6	6	f	0	0	0	invite-email_ja.html.ftl	\N	26	-1	1
531	21	3	f	93	0	0	\N	\N	51	-1	1
520	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - Japanese version	\N	27	-1	2
520	6	6	f	0	0	0	invite-email_ja.html.ftl	\N	28	-1	2
520	21	3	f	82	0	0	\N	\N	51	-1	1
521	1	1	t	0	0	0	\N	\N	52	-1	1
521	6	6	f	0	0	0	invite-email_nl.html.ftl	\N	26	-1	1
521	6	6	f	0	0	0	Email template used to generate the invite email for Alfresco Share - Dutch version	\N	27	-1	2
521	6	6	f	0	0	0	invite-email_nl.html.ftl	\N	28	-1	2
521	21	3	f	83	0	0	\N	\N	51	-1	1
522	1	1	t	0	0	0	\N	\N	52	-1	1
522	6	6	f	0	0	0	invite-email-add-direct.html.ftl	\N	26	-1	1
522	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - Default version	\N	27	-1	2
522	6	6	f	0	0	0	invite-email-add-direct.html.ftl	\N	28	-1	2
522	21	3	f	84	0	0	\N	\N	51	-1	1
523	1	1	t	0	0	0	\N	\N	52	-1	1
523	6	6	f	0	0	0	invite-email-add-direct.html_fr.ftl	\N	26	-1	1
523	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - French version	\N	27	-1	2
523	6	6	f	0	0	0	invite-email-add-direct.html_fr.ftl	\N	28	-1	2
523	21	3	f	85	0	0	\N	\N	51	-1	1
524	1	1	t	0	0	0	\N	\N	52	-1	1
524	6	6	f	0	0	0	invite-email-add-direct.html_es.ftl	\N	26	-1	1
524	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - Spanish version	\N	27	-1	2
524	6	6	f	0	0	0	invite-email-add-direct.html_es.ftl	\N	28	-1	2
524	21	3	f	86	0	0	\N	\N	51	-1	1
525	1	1	t	0	0	0	\N	\N	52	-1	1
525	6	6	f	0	0	0	invite-email-add-direct.html_de.ftl	\N	26	-1	1
525	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - German version	\N	27	-1	2
525	6	6	f	0	0	0	invite-email-add-direct.html_de.ftl	\N	28	-1	2
525	21	3	f	87	0	0	\N	\N	51	-1	1
526	1	1	t	0	0	0	\N	\N	52	-1	1
526	6	6	f	0	0	0	invite-email-add-direct.html_it.ftl	\N	26	-1	1
526	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - Italian version	\N	27	-1	2
526	6	6	f	0	0	0	invite-email-add-direct.html_it.ftl	\N	28	-1	2
526	21	3	f	88	0	0	\N	\N	51	-1	1
527	1	1	t	0	0	0	\N	\N	52	-1	1
527	6	6	f	0	0	0	invite-email-add-direct.html_ja.ftl	\N	26	-1	1
527	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - Japanese version	\N	27	-1	2
527	6	6	f	0	0	0	invite-email-add-direct.html_ja.ftl	\N	28	-1	2
527	21	3	f	89	0	0	\N	\N	51	-1	1
528	1	1	t	0	0	0	\N	\N	52	-1	1
528	6	6	f	0	0	0	invite-email-add-direct.html_nl.ftl	\N	26	-1	1
528	6	6	f	0	0	0	Email template used to generate the user added to site email for Alfresco Share - Dutch version	\N	27	-1	2
528	6	6	f	0	0	0	invite-email-add-direct.html_nl.ftl	\N	28	-1	2
528	21	3	f	90	0	0	\N	\N	51	-1	1
529	1	1	t	0	0	0	\N	\N	52	-1	1
529	6	6	f	0	0	0	invite-email-moderated.html.ftl	\N	26	-1	1
529	6	6	f	0	0	0	Email template used to generate the request to join site email for Alfresco Share - Default version	\N	27	-1	2
529	6	6	f	0	0	0	invite-email-moderated.html.ftl	\N	28	-1	2
529	21	3	f	91	0	0	\N	\N	51	-1	1
530	1	1	t	0	0	0	\N	\N	52	-1	1
530	6	6	f	0	0	0	notify_user_email.html.ftl	\N	26	-1	1
530	6	6	f	0	0	0	Email template for notifying users from a rule or action - Default version	\N	27	-1	2
530	6	6	f	0	0	0	notify_user_email.html.ftl	\N	28	-1	2
530	21	3	f	92	0	0	\N	\N	51	-1	1
531	1	1	t	0	0	0	\N	\N	52	-1	1
531	6	6	f	0	0	0	notify_user_email_de.html.ftl	\N	26	-1	1
531	6	6	f	0	0	0	Email template for notifying users from a rule or action - German version	\N	27	-1	2
531	6	6	f	0	0	0	notify_user_email_de.html.ftl	\N	28	-1	2
532	1	1	t	0	0	0	\N	\N	52	-1	1
532	6	6	f	0	0	0	notify_user_email_es.html.ftl	\N	26	-1	1
532	6	6	f	0	0	0	Email template for notifying users from a rule or action - Spanish version	\N	27	-1	2
532	6	6	f	0	0	0	notify_user_email_es.html.ftl	\N	28	-1	2
532	21	3	f	94	0	0	\N	\N	51	-1	1
533	1	1	t	0	0	0	\N	\N	52	-1	1
533	6	6	f	0	0	0	notify_user_email_fr.html.ftl	\N	26	-1	1
533	6	6	f	0	0	0	Email template for notifying users from a rule or action - French version	\N	27	-1	2
533	6	6	f	0	0	0	notify_user_email_fr.html.ftl	\N	28	-1	2
533	21	3	f	95	0	0	\N	\N	51	-1	1
534	1	1	t	0	0	0	\N	\N	52	-1	1
534	6	6	f	0	0	0	notify_user_email_it.html.ftl	\N	26	-1	1
534	6	6	f	0	0	0	Email template for notifying users from a rule or action - Italian version	\N	27	-1	2
534	6	6	f	0	0	0	notify_user_email_it.html.ftl	\N	28	-1	2
534	21	3	f	96	0	0	\N	\N	51	-1	1
535	1	1	t	0	0	0	\N	\N	52	-1	1
535	6	6	f	0	0	0	notify_user_email_ja.html.ftl	\N	26	-1	1
535	6	6	f	0	0	0	Email template for notifying users from a rule or action - Japanese version	\N	27	-1	2
535	6	6	f	0	0	0	notify_user_email_ja.html.ftl	\N	28	-1	2
535	21	3	f	97	0	0	\N	\N	51	-1	1
536	1	1	t	0	0	0	\N	\N	52	-1	1
536	6	6	f	0	0	0	notify_user_email_nl.html.ftl	\N	26	-1	1
536	6	6	f	0	0	0	Email template for notifying users from a rule or action - Dutch version	\N	27	-1	2
536	6	6	f	0	0	0	notify_user_email_nl.html.ftl	\N	28	-1	2
536	21	3	f	98	0	0	\N	\N	51	-1	1
537	1	1	t	0	0	0	\N	\N	52	-1	1
537	6	6	f	0	0	0		\N	54	-1	1
537	6	6	f	0	0	0	emailbody_textplain_share.ftl	\N	26	-1	1
537	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Default version	\N	27	-1	2
537	6	6	f	0	0	0	emailbody_textplain_share.ftl	\N	28	-1	2
537	21	3	f	99	0	0	\N	\N	51	-1	1
538	1	1	t	0	0	0	\N	\N	52	-1	1
538	6	6	f	0	0	0		\N	54	-1	1
538	6	6	f	0	0	0	emailbody_textplain_alfresco.ftl	\N	26	-1	1
538	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Default version	\N	27	-1	2
538	6	6	f	0	0	0	emailbody_textplain_alfresco.ftl	\N	28	-1	2
538	21	3	f	100	0	0	\N	\N	51	-1	1
539	1	1	t	0	0	0	\N	\N	52	-1	1
539	6	6	f	0	0	0		\N	54	-1	1
539	6	6	f	0	0	0	emailbody_texthtml_alfresco.ftl	\N	26	-1	1
539	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Default version	\N	27	-1	2
539	6	6	f	0	0	0	emailbody_texthtml_alfresco.ftl	\N	28	-1	2
539	21	3	f	101	0	0	\N	\N	51	-1	1
540	1	1	t	0	0	0	\N	\N	52	-1	1
540	6	6	f	0	0	0		\N	54	-1	1
540	6	6	f	0	0	0	emailbody_texthtml_share.ftl	\N	26	-1	1
540	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Default version	\N	27	-1	2
540	6	6	f	0	0	0	emailbody_texthtml_share.ftl	\N	28	-1	2
540	21	3	f	102	0	0	\N	\N	51	-1	1
541	1	1	t	0	0	0	\N	\N	52	-1	1
541	6	6	f	0	0	0		\N	54	-1	1
541	6	6	f	0	0	0	emailbody_textplain_share_de.ftl	\N	26	-1	1
541	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - German version	\N	27	-1	2
541	6	6	f	0	0	0	emailbody_textplain_share_de.ftl	\N	28	-1	2
541	21	3	f	103	0	0	\N	\N	51	-1	1
542	1	1	t	0	0	0	\N	\N	52	-1	1
542	6	6	f	0	0	0		\N	54	-1	1
542	6	6	f	0	0	0	emailbody_textplain_alfresco_de.ftl	\N	26	-1	1
542	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - German version	\N	27	-1	2
542	6	6	f	0	0	0	emailbody_textplain_alfresco_de.ftl	\N	28	-1	2
542	21	3	f	104	0	0	\N	\N	51	-1	1
543	1	1	t	0	0	0	\N	\N	52	-1	1
543	6	6	f	0	0	0		\N	54	-1	1
543	6	6	f	0	0	0	emailbody_texthtml_alfresco_de.ftl	\N	26	-1	1
543	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - German version	\N	27	-1	2
543	6	6	f	0	0	0	emailbody_texthtml_alfresco_de.ftl	\N	28	-1	2
543	21	3	f	105	0	0	\N	\N	51	-1	1
544	1	1	t	0	0	0	\N	\N	52	-1	1
544	6	6	f	0	0	0		\N	54	-1	1
544	6	6	f	0	0	0	emailbody_texthtml_share_de.ftl	\N	26	-1	1
544	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - German version	\N	27	-1	2
544	6	6	f	0	0	0	emailbody_texthtml_share_de.ftl	\N	28	-1	2
544	21	3	f	106	0	0	\N	\N	51	-1	1
545	1	1	t	0	0	0	\N	\N	52	-1	1
545	6	6	f	0	0	0		\N	54	-1	1
545	6	6	f	0	0	0	emailbody_textplain_share_es.ftl	\N	26	-1	1
545	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Spanish version	\N	27	-1	2
545	6	6	f	0	0	0	emailbody_textplain_share_es.ftl	\N	28	-1	2
545	21	3	f	107	0	0	\N	\N	51	-1	1
546	1	1	t	0	0	0	\N	\N	52	-1	1
546	6	6	f	0	0	0		\N	54	-1	1
546	6	6	f	0	0	0	emailbody_textplain_alfresco_es.ftl	\N	26	-1	1
546	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Spanish version	\N	27	-1	2
546	6	6	f	0	0	0	emailbody_textplain_alfresco_es.ftl	\N	28	-1	2
546	21	3	f	108	0	0	\N	\N	51	-1	1
547	1	1	t	0	0	0	\N	\N	52	-1	1
547	6	6	f	0	0	0		\N	54	-1	1
547	6	6	f	0	0	0	emailbody_texthtml_alfresco_es.ftl	\N	26	-1	1
547	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Spanish version	\N	27	-1	2
547	6	6	f	0	0	0	emailbody_texthtml_alfresco_es.ftl	\N	28	-1	2
547	21	3	f	109	0	0	\N	\N	51	-1	1
548	1	1	t	0	0	0	\N	\N	52	-1	1
548	6	6	f	0	0	0		\N	54	-1	1
548	6	6	f	0	0	0	emailbody_texthtml_share_es.ftl	\N	26	-1	1
548	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Spanish version	\N	27	-1	2
548	6	6	f	0	0	0	emailbody_texthtml_share_es.ftl	\N	28	-1	2
548	21	3	f	110	0	0	\N	\N	51	-1	1
549	1	1	t	0	0	0	\N	\N	52	-1	1
549	6	6	f	0	0	0		\N	54	-1	1
549	6	6	f	0	0	0	emailbody_textplain_share_fr.ftl	\N	26	-1	1
549	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - French version	\N	27	-1	2
549	6	6	f	0	0	0	emailbody_textplain_share_fr.ftl	\N	28	-1	2
549	21	3	f	111	0	0	\N	\N	51	-1	1
550	1	1	t	0	0	0	\N	\N	52	-1	1
550	6	6	f	0	0	0		\N	54	-1	1
550	6	6	f	0	0	0	emailbody_textplain_alfresco_fr.ftl	\N	26	-1	1
550	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - French version	\N	27	-1	2
550	6	6	f	0	0	0	emailbody_textplain_alfresco_fr.ftl	\N	28	-1	2
550	21	3	f	112	0	0	\N	\N	51	-1	1
551	1	1	t	0	0	0	\N	\N	52	-1	1
551	6	6	f	0	0	0		\N	54	-1	1
551	6	6	f	0	0	0	emailbody_texthtml_alfresco_fr.ftl	\N	26	-1	1
551	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - French version	\N	27	-1	2
551	6	6	f	0	0	0	emailbody_texthtml_alfresco_fr.ftl	\N	28	-1	2
551	21	3	f	113	0	0	\N	\N	51	-1	1
552	1	1	t	0	0	0	\N	\N	52	-1	1
552	6	6	f	0	0	0		\N	54	-1	1
552	6	6	f	0	0	0	emailbody_texthtml_share_fr.ftl	\N	26	-1	1
552	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - French version	\N	27	-1	2
552	6	6	f	0	0	0	emailbody_texthtml_share_fr.ftl	\N	28	-1	2
552	21	3	f	114	0	0	\N	\N	51	-1	1
553	1	1	t	0	0	0	\N	\N	52	-1	1
553	6	6	f	0	0	0		\N	54	-1	1
553	6	6	f	0	0	0	emailbody_textplain_share_it.ftl	\N	26	-1	1
553	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Italian version	\N	27	-1	2
553	6	6	f	0	0	0	emailbody_textplain_share_it.ftl	\N	28	-1	2
553	21	3	f	115	0	0	\N	\N	51	-1	1
554	1	1	t	0	0	0	\N	\N	52	-1	1
554	6	6	f	0	0	0		\N	54	-1	1
554	6	6	f	0	0	0	emailbody_textplain_alfresco_it.ftl	\N	26	-1	1
554	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Italian version	\N	27	-1	2
554	6	6	f	0	0	0	emailbody_textplain_alfresco_it.ftl	\N	28	-1	2
554	21	3	f	116	0	0	\N	\N	51	-1	1
555	1	1	t	0	0	0	\N	\N	52	-1	1
555	6	6	f	0	0	0		\N	54	-1	1
555	6	6	f	0	0	0	emailbody_texthtml_alfresco_it.ftl	\N	26	-1	1
555	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Italian version	\N	27	-1	2
555	6	6	f	0	0	0	emailbody_texthtml_alfresco_it.ftl	\N	28	-1	2
555	21	3	f	117	0	0	\N	\N	51	-1	1
556	1	1	t	0	0	0	\N	\N	52	-1	1
556	6	6	f	0	0	0		\N	54	-1	1
556	6	6	f	0	0	0	emailbody_texthtml_share_it.ftl	\N	26	-1	1
556	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Italian version	\N	27	-1	2
556	6	6	f	0	0	0	emailbody_texthtml_share_it.ftl	\N	28	-1	2
556	21	3	f	118	0	0	\N	\N	51	-1	1
557	1	1	t	0	0	0	\N	\N	52	-1	1
557	6	6	f	0	0	0		\N	54	-1	1
557	6	6	f	0	0	0	emailbody_textplain_share_ja.ftl	\N	26	-1	1
557	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Japanese version	\N	27	-1	2
557	6	6	f	0	0	0	emailbody_textplain_share_ja.ftl	\N	28	-1	2
557	21	3	f	119	0	0	\N	\N	51	-1	1
558	1	1	t	0	0	0	\N	\N	52	-1	1
558	6	6	f	0	0	0		\N	54	-1	1
558	6	6	f	0	0	0	emailbody_textplain_alfresco_ja.ftl	\N	26	-1	1
558	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Japanese version	\N	27	-1	2
558	6	6	f	0	0	0	emailbody_textplain_alfresco_ja.ftl	\N	28	-1	2
558	21	3	f	120	0	0	\N	\N	51	-1	1
559	1	1	t	0	0	0	\N	\N	52	-1	1
559	6	6	f	0	0	0		\N	54	-1	1
559	6	6	f	0	0	0	emailbody_texthtml_alfresco_ja.ftl	\N	26	-1	1
559	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Japanese version	\N	27	-1	2
559	6	6	f	0	0	0	emailbody_texthtml_alfresco_ja.ftl	\N	28	-1	2
559	21	3	f	121	0	0	\N	\N	51	-1	1
560	1	1	t	0	0	0	\N	\N	52	-1	1
560	6	6	f	0	0	0		\N	54	-1	1
560	6	6	f	0	0	0	emailbody_texthtml_share_ja.ftl	\N	26	-1	1
560	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Japanese version	\N	27	-1	2
560	6	6	f	0	0	0	emailbody_texthtml_share_ja.ftl	\N	28	-1	2
560	21	3	f	122	0	0	\N	\N	51	-1	1
561	1	1	t	0	0	0	\N	\N	52	-1	1
561	6	6	f	0	0	0		\N	54	-1	1
561	6	6	f	0	0	0	emailbody_textplain_share_nb.ftl	\N	26	-1	1
561	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Norwegian Bokmal version	\N	27	-1	2
561	6	6	f	0	0	0	emailbody_textplain_share_nb.ftl	\N	28	-1	2
561	21	3	f	123	0	0	\N	\N	51	-1	1
562	1	1	t	0	0	0	\N	\N	52	-1	1
562	6	6	f	0	0	0		\N	54	-1	1
562	6	6	f	0	0	0	emailbody_textplain_alfresco_nb.ftl	\N	26	-1	1
562	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Norwegian Bokmal version	\N	27	-1	2
562	6	6	f	0	0	0	emailbody_textplain_alfresco_nb.ftl	\N	28	-1	2
562	21	3	f	124	0	0	\N	\N	51	-1	1
563	1	1	t	0	0	0	\N	\N	52	-1	1
563	6	6	f	0	0	0		\N	54	-1	1
563	6	6	f	0	0	0	emailbody_texthtml_alfresco_nb.ftl	\N	26	-1	1
563	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Norwegian Bokmal version	\N	27	-1	2
563	6	6	f	0	0	0	emailbody_texthtml_alfresco_nb.ftl	\N	28	-1	2
563	21	3	f	125	0	0	\N	\N	51	-1	1
564	1	1	t	0	0	0	\N	\N	52	-1	1
564	6	6	f	0	0	0		\N	54	-1	1
564	6	6	f	0	0	0	emailbody_texthtml_share_nb.ftl	\N	26	-1	1
564	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Norwegian Bokmal version	\N	27	-1	2
564	6	6	f	0	0	0	emailbody_texthtml_share_nb.ftl	\N	28	-1	2
564	21	3	f	126	0	0	\N	\N	51	-1	1
565	1	1	t	0	0	0	\N	\N	52	-1	1
565	6	6	f	0	0	0		\N	54	-1	1
565	6	6	f	0	0	0	emailbody_textplain_share_nl.ftl	\N	26	-1	1
565	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Dutch version	\N	27	-1	2
565	6	6	f	0	0	0	emailbody_textplain_share_nl.ftl	\N	28	-1	2
565	21	3	f	127	0	0	\N	\N	51	-1	1
566	1	1	t	0	0	0	\N	\N	52	-1	1
566	6	6	f	0	0	0		\N	54	-1	1
566	6	6	f	0	0	0	emailbody_textplain_alfresco_nl.ftl	\N	26	-1	1
566	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Dutch version	\N	27	-1	2
566	6	6	f	0	0	0	emailbody_textplain_alfresco_nl.ftl	\N	28	-1	2
566	21	3	f	128	0	0	\N	\N	51	-1	1
567	1	1	t	0	0	0	\N	\N	52	-1	1
567	6	6	f	0	0	0		\N	54	-1	1
567	6	6	f	0	0	0	emailbody_texthtml_alfresco_nl.ftl	\N	26	-1	1
567	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Dutch version	\N	27	-1	2
567	6	6	f	0	0	0	emailbody_texthtml_alfresco_nl.ftl	\N	28	-1	2
567	21	3	f	129	0	0	\N	\N	51	-1	1
568	1	1	t	0	0	0	\N	\N	52	-1	1
568	6	6	f	0	0	0		\N	54	-1	1
568	6	6	f	0	0	0	emailbody_texthtml_share_nl.ftl	\N	26	-1	1
568	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Dutch version	\N	27	-1	2
568	6	6	f	0	0	0	emailbody_texthtml_share_nl.ftl	\N	28	-1	2
568	21	3	f	130	0	0	\N	\N	51	-1	1
569	1	1	t	0	0	0	\N	\N	52	-1	1
569	6	6	f	0	0	0		\N	54	-1	1
569	6	6	f	0	0	0	emailbody_textplain_share_pt_BR.ftl	\N	26	-1	1
569	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Brazilian Portuguese version	\N	27	-1	2
569	6	6	f	0	0	0	emailbody_textplain_share_pt_BR.ftl	\N	28	-1	2
569	21	3	f	131	0	0	\N	\N	51	-1	1
570	1	1	t	0	0	0	\N	\N	52	-1	1
570	6	6	f	0	0	0		\N	54	-1	1
570	6	6	f	0	0	0	emailbody_textplain_alfresco_pt_BR.ftl	\N	26	-1	1
579	6	6	f	0	0	0	emailbody_texthtml_alfresco_zh_CN.ftl	\N	28	-1	2
570	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Brazilian Portuguese version	\N	27	-1	2
570	6	6	f	0	0	0	emailbody_textplain_alfresco_pt_BR.ftl	\N	28	-1	2
570	21	3	f	132	0	0	\N	\N	51	-1	1
571	1	1	t	0	0	0	\N	\N	52	-1	1
571	6	6	f	0	0	0		\N	54	-1	1
571	6	6	f	0	0	0	emailbody_texthtml_alfresco_pt_BR.ftl	\N	26	-1	1
571	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Brazilian Portuguese version	\N	27	-1	2
571	6	6	f	0	0	0	emailbody_texthtml_alfresco_pt_BR.ftl	\N	28	-1	2
571	21	3	f	133	0	0	\N	\N	51	-1	1
572	1	1	t	0	0	0	\N	\N	52	-1	1
572	6	6	f	0	0	0		\N	54	-1	1
572	6	6	f	0	0	0	emailbody_texthtml_share_pt_BR.ftl	\N	26	-1	1
572	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Brazilian Portuguese version	\N	27	-1	2
572	6	6	f	0	0	0	emailbody_texthtml_share_pt_BR.ftl	\N	28	-1	2
572	21	3	f	134	0	0	\N	\N	51	-1	1
573	1	1	t	0	0	0	\N	\N	52	-1	1
573	6	6	f	0	0	0		\N	54	-1	1
573	6	6	f	0	0	0	emailbody_textplain_share_ru.ftl	\N	26	-1	1
573	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Russian version	\N	27	-1	2
573	6	6	f	0	0	0	emailbody_textplain_share_ru.ftl	\N	28	-1	2
573	21	3	f	135	0	0	\N	\N	51	-1	1
574	1	1	t	0	0	0	\N	\N	52	-1	1
574	6	6	f	0	0	0		\N	54	-1	1
574	6	6	f	0	0	0	emailbody_textplain_alfresco_ru.ftl	\N	26	-1	1
574	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Russian version	\N	27	-1	2
574	6	6	f	0	0	0	emailbody_textplain_alfresco_ru.ftl	\N	28	-1	2
574	21	3	f	136	0	0	\N	\N	51	-1	1
575	1	1	t	0	0	0	\N	\N	52	-1	1
575	6	6	f	0	0	0		\N	54	-1	1
575	6	6	f	0	0	0	emailbody_texthtml_alfresco_ru.ftl	\N	26	-1	1
575	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Russian version	\N	27	-1	2
575	6	6	f	0	0	0	emailbody_texthtml_alfresco_ru.ftl	\N	28	-1	2
575	21	3	f	137	0	0	\N	\N	51	-1	1
576	1	1	t	0	0	0	\N	\N	52	-1	1
576	6	6	f	0	0	0		\N	54	-1	1
576	6	6	f	0	0	0	emailbody_texthtml_share_ru.ftl	\N	26	-1	1
576	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Russian version	\N	27	-1	2
576	6	6	f	0	0	0	emailbody_texthtml_share_ru.ftl	\N	28	-1	2
576	21	3	f	138	0	0	\N	\N	51	-1	1
577	1	1	t	0	0	0	\N	\N	52	-1	1
577	6	6	f	0	0	0		\N	54	-1	1
577	6	6	f	0	0	0	emailbody_textplain_share_zh_CN.ftl	\N	26	-1	1
577	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) for Alfresco Share - Simplified Chinese version	\N	27	-1	2
577	6	6	f	0	0	0	emailbody_textplain_share_zh_CN.ftl	\N	28	-1	2
577	21	3	f	139	0	0	\N	\N	51	-1	1
578	1	1	t	0	0	0	\N	\N	52	-1	1
578	6	6	f	0	0	0		\N	54	-1	1
578	6	6	f	0	0	0	emailbody_textplain_alfresco_zh_CN.ftl	\N	26	-1	1
578	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/plain" part) - Simplified Chinese version	\N	27	-1	2
578	6	6	f	0	0	0	emailbody_textplain_alfresco_zh_CN.ftl	\N	28	-1	2
578	21	3	f	140	0	0	\N	\N	51	-1	1
579	1	1	t	0	0	0	\N	\N	52	-1	1
579	6	6	f	0	0	0		\N	54	-1	1
579	6	6	f	0	0	0	emailbody_texthtml_alfresco_zh_CN.ftl	\N	26	-1	1
579	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) - Simplified Chinese version	\N	27	-1	2
579	21	3	f	141	0	0	\N	\N	51	-1	1
580	1	1	t	0	0	0	\N	\N	52	-1	1
580	6	6	f	0	0	0		\N	54	-1	1
580	6	6	f	0	0	0	emailbody_texthtml_share_zh_CN.ftl	\N	26	-1	1
580	6	6	f	0	0	0	Email template used to generate the "multipart/alternative" IMAP message body ("text/html" part) for Alfresco Share - Simplified Chinese version	\N	27	-1	2
580	6	6	f	0	0	0	emailbody_texthtml_share_zh_CN.ftl	\N	28	-1	2
580	21	3	f	142	0	0	\N	\N	51	-1	1
581	6	6	f	0	0	0	Downloads	\N	26	-1	1
581	6	6	f	0	0	0	Root folder for downloads	\N	27	-1	2
581	6	6	f	0	0	0	Downloads	\N	28	-1	2
582	1	1	t	0	0	0	\N	\N	52	-1	1
582	6	6	f	0	0	0	smartFoldersExample.json	\N	26	-1	1
582	6	6	f	0	0	0	Smart Folder Template Sample	\N	27	-1	2
582	6	6	f	0	0	0	Smart Folder Template Sample	\N	28	-1	2
582	21	3	f	143	0	0	\N	\N	51	-1	1
7	21	3	f	144	0	0	\N	\N	104	-1	1
7	1	1	f	0	0	0	\N	\N	86	-1	1
7	1	1	f	0	0	0	\N	\N	87	-1	1
7	6	6	f	0	0	0	ENTERPRISE	\N	105	-1	1
7	6	6	f	0	0	0	Main Repository	\N	16	-1	1
583	1	1	t	0	0	0	\N	\N	52	-1	1
583	6	6	f	0	0	0	Alfresco	\N	54	-1	1
583	6	6	f	0	0	0	start-pooled-review-workflow.js	\N	26	-1	1
583	6	6	f	0	0	0	Starts the Pooled Review and Approve workflow for all members of the site the document belongs to	\N	27	-1	2
583	6	6	f	0	0	0	Start Pooled Review and Approve Workflow	\N	28	-1	2
583	21	3	f	145	0	0	\N	\N	51	-1	1
585	6	6	f	0	0	0	4f78dc6e000a27fc3bb7e9c70aa6b5d9	\N	26	-1	1
585	6	6	f	0	0	0	GROUP_site_swsdp	\N	90	-1	1
585	6	6	f	0	0	0	site_swsdp	\N	112	-1	1
586	6	6	f	0	0	0	APP.SHARE	\N	26	-1	1
587	6	6	f	0	0	0	9e5cb3fa1850083495559ca2a4ca2de9	\N	26	-1	1
587	6	6	f	0	0	0	GROUP_site_swsdp_SiteManager	\N	90	-1	1
587	6	6	f	0	0	0	site_swsdp_SiteManager	\N	112	-1	1
587	3	3	f	389377474	0	0	\N	\N	94	-1	1
587	3	3	f	11	0	0	\N	\N	96	-1	1
588	6	6	f	0	0	0	58d3dfc926fbcb0ce0a1213c37dc4711	\N	26	-1	1
588	6	6	f	0	0	0	GROUP_site_swsdp_SiteCollaborator	\N	90	-1	1
588	6	6	f	0	0	0	site_swsdp_SiteCollaborator	\N	112	-1	1
588	3	3	f	3554663376	0	0	\N	\N	94	-1	1
588	3	3	f	11	0	0	\N	\N	96	-1	1
589	6	6	f	0	0	0	5b487fd6a02f7430721726163ba0daa9	\N	26	-1	1
589	6	6	f	0	0	0	GROUP_site_swsdp_SiteContributor	\N	90	-1	1
589	6	6	f	0	0	0	site_swsdp_SiteContributor	\N	112	-1	1
589	3	3	f	210431739	0	0	\N	\N	94	-1	1
589	3	3	f	11	0	0	\N	\N	96	-1	1
590	6	6	f	0	0	0	73714588eb587e2a207a436130080c9e	\N	26	-1	1
590	6	6	f	0	0	0	GROUP_site_swsdp_SiteConsumer	\N	90	-1	1
590	6	6	f	0	0	0	site_swsdp_SiteConsumer	\N	112	-1	1
590	3	3	f	115054948	0	0	\N	\N	94	-1	1
590	3	3	f	11	0	0	\N	\N	96	-1	1
32	3	3	f	2525897058	0	0	\N	\N	94	-1	1
32	3	3	f	11	0	0	\N	\N	96	-1	1
591	0	0	f	0	0	0	\N	\N	113	-1	1
591	1	1	f	0	0	0	\N	\N	6	-1	1
591	1	1	f	0	0	0	\N	\N	7	-1	1
591	1	1	f	0	0	0	\N	\N	8	-1	1
591	6	6	f	0	0	0	abeecher	\N	9	-1	1
591	6	6	f	0	0	0	0eeb8b5ee6e96f1a8443edfb1dfc36ad	\N	10	-1	1
591	1	1	f	0	0	0	\N	\N	11	-1	1
592	0	0	f	0	0	0	\N	\N	113	-1	1
592	1	1	f	0	0	0	\N	\N	6	-1	1
592	1	1	f	0	0	0	\N	\N	7	-1	1
592	1	1	f	0	0	0	\N	\N	8	-1	1
592	6	6	f	0	0	0	mjackson	\N	9	-1	1
592	6	6	f	0	0	0	0eeb8b5ee6e96f1a8443edfb1dfc36ad	\N	10	-1	1
592	1	1	f	0	0	0	\N	\N	11	-1	1
593	6	6	f	0	0	0		\N	129	-1	1
593	6	6	f	0	0	0	Helping to design the look and feel of the new web site	\N	130	-1	1
593	3	3	f	440	0	0	\N	\N	131	-1	1
593	7	6	f	0	0	0	2011-02-15T20:20:13.432Z	\N	132	-1	1
593	3	3	f	8382006	0	0	\N	\N	36	-1	1
593	6	6	f	0	0	0	abeecher	\N	37	-1	1
593	6	6	f	0	0	0	abeecher@example.com	\N	38	-1	1
593	6	6	f	0	0	0	userHomesHomeFolderProvider	\N	39	-1	1
593	6	6	f	0	0	0	abeecher	\N	40	-1	1
593	6	6	f	0	0	0	Beecher	\N	41	-1	1
593	6	6	f	0	0	0	Alice	\N	43	-1	1
593	6	6	f	0	0	0	Graphic Designer	\N	114	-1	1
593	6	6	f	0	0	0	Tilbury, UK	\N	115	-1	1
593	6	6	f	0	0	0	0112211001100	\N	116	-1	1
593	6	6	f	0	0	0	abeecher	\N	117	-1	1
593	6	6	f	0	0	0	200 Butterwick Street	\N	118	-1	1
593	6	6	f	0	0	0	0112211001100	\N	119	-1	1
593	3	3	f	-1	0	0	\N	\N	120	-1	1
593	6	6	f	0	0	0		\N	121	-1	1
593	6	6	f	0	0	0	ALF1 SAM1	\N	122	-1	1
593	6	6	f	0	0	0		\N	123	-1	1
593	6	6	f	0	0	0	UK	\N	124	-1	1
593	6	6	f	0	0	0		\N	125	-1	1
593	6	6	f	0	0	0		\N	126	-1	1
593	6	6	f	0	0	0	Tilbury	\N	127	-1	1
593	6	6	f	0	0	0	Moresby, Garland and Wedge	\N	128	-1	1
593	21	3	f	146	0	0	\N	\N	135	-1	1
593	21	3	f	147	0	0	\N	\N	136	-1	1
594	6	6	f	0	0	0	abeecher-avatar.jpg	\N	26	-1	1
594	6	6	f	0	0	0	avatar:1443523516088	\N	138	0	1
594	21	3	f	148	0	0	\N	\N	51	-1	1
595	1	1	t	0	0	0	\N	\N	86	-1	1
595	1	1	f	0	0	0	\N	\N	87	-1	1
595	6	6	f	0	0	0	avatar	\N	26	-1	1
595	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
595	6	6	f	0	0	0	avatar	\N	144	-1	1
595	21	3	f	149	0	0	\N	\N	51	-1	1
596	6	6	f	0	0	0		\N	129	-1	1
596	6	6	f	0	0	0	Working on a new web design for the corporate site	\N	130	-1	1
596	3	3	f	442	0	0	\N	\N	131	-1	1
596	7	6	f	0	0	0	2011-02-15T20:13:09.649Z	\N	132	-1	1
596	3	3	f	8834773	0	0	\N	\N	36	-1	1
596	6	6	f	0	0	0	mjackson	\N	37	-1	1
596	6	6	f	0	0	0	mjackson@example.com	\N	38	-1	1
596	6	6	f	0	0	0	userHomesHomeFolderProvider	\N	39	-1	1
596	6	6	f	0	0	0	mjackson	\N	40	-1	1
596	6	6	f	0	0	0	Jackson	\N	41	-1	1
596	6	6	f	0	0	0	Mike	\N	43	-1	1
596	6	6	f	0	0	0	Web Site Manager	\N	114	-1	1
596	6	6	f	0	0	0	Threepwood, UK	\N	115	-1	1
596	6	6	f	0	0	0	012211331100	\N	116	-1	1
596	6	6	f	0	0	0	mjackson	\N	117	-1	1
596	6	6	f	0	0	0	100 Cavendish Street	\N	118	-1	1
596	6	6	f	0	0	0	012211331100	\N	119	-1	1
596	3	3	f	-1	0	0	\N	\N	120	-1	1
596	6	6	f	0	0	0		\N	121	-1	1
596	6	6	f	0	0	0	ALF1 SAM1	\N	122	-1	1
596	6	6	f	0	0	0		\N	123	-1	1
596	6	6	f	0	0	0	UK	\N	124	-1	1
596	6	6	f	0	0	0		\N	125	-1	1
596	6	6	f	0	0	0		\N	126	-1	1
596	6	6	f	0	0	0	Threepwood	\N	127	-1	1
596	6	6	f	0	0	0	Green Energy	\N	128	-1	1
596	21	3	f	150	0	0	\N	\N	135	-1	1
596	21	3	f	151	0	0	\N	\N	136	-1	1
597	6	6	f	0	0	0	mjackson-avatar.jpg	\N	26	-1	1
597	6	6	f	0	0	0	avatar:1443523488273	\N	138	0	1
597	21	3	f	152	0	0	\N	\N	51	-1	1
598	1	1	t	0	0	0	\N	\N	86	-1	1
598	1	1	f	0	0	0	\N	\N	87	-1	1
598	6	6	f	0	0	0	avatar	\N	26	-1	1
598	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
598	6	6	f	0	0	0	avatar	\N	144	-1	1
598	21	3	f	153	0	0	\N	\N	51	-1	1
593	0	0	f	0	0	0	\N	\N	45	-1	1
596	0	0	f	0	0	0	\N	\N	45	-1	1
593	3	3	f	3822287290	0	0	\N	\N	94	-1	1
593	3	3	f	11	0	0	\N	\N	96	-1	1
596	3	3	f	1579065264	0	0	\N	\N	94	-1	1
596	3	3	f	11	0	0	\N	\N	96	-1	1
599	3	3	f	584	0	0	\N	\N	149	-1	1
600	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	150	-1	1
600	6	6	f	0	0	0	admin	\N	40	-1	1
600	6	6	f	0	0	0	swsdp	\N	26	-1	1
600	6	6	f	0	0	0	PUBLIC	\N	107	-1	1
600	6	6	f	0	0	0	This is a Sample Alfresco Team site.	\N	27	-1	2
600	6	6	f	0	0	0	Sample: Web Site Design Project	\N	28	-1	2
600	6	6	f	0	0	0	site-dashboard	\N	108	-1	1
600	21	3	f	154	0	0	\N	\N	151	-1	1
601	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	150	-1	1
601	6	6	f	0	0	0	admin	\N	40	-1	1
601	6	6	f	0	0	0	documentLibrary	\N	152	-1	1
601	6	6	f	0	0	0	documentLibrary	\N	26	-1	1
601	6	6	f	0	0	0	Document Library	\N	27	-1	2
601	21	3	f	155	0	0	\N	\N	151	-1	1
602	6	6	f	0	0	0	admin	\N	40	-1	1
602	6	6	f	0	0	0	Agency Files	\N	26	-1	1
602	6	6	f	0	0	0	This folder holds the agency related files for the project	\N	27	-1	2
602	6	6	f	0	0	0	Agency related files	\N	28	-1	2
603	6	6	f	0	0	0	admin	\N	40	-1	1
603	6	6	f	0	0	0	Contracts	\N	26	-1	1
603	6	6	f	0	0	0	This folder holds the agency contracts	\N	27	-1	2
603	6	6	f	0	0	0	Project contracts	\N	28	-1	2
604	6	6	f	0	0	0	Alice Beecher	\N	54	-1	1
604	6	6	f	0	0	0	admin	\N	40	-1	1
604	6	6	f	0	0	0	Project Contract.pdf	\N	26	-1	1
604	1	1	f	0	0	0	\N	\N	155	-1	1
604	6	6	f	0	0	0	Contract for the Green Energy project	\N	27	-1	2
604	4	4	f	0	0	0	\N	\N	156	-1	1
604	6	6	f	0	0	0	Project Contract for Green Energy	\N	28	-1	2
604	1	1	t	0	0	0	\N	\N	158	-1	1
604	1	1	t	0	0	0	\N	\N	159	-1	1
604	2	3	f	0	0	0	\N	\N	160	-1	1
604	21	3	f	156	0	0	\N	\N	51	-1	1
605	6	6	f	0	0	0	1a0b110f-1e09-4ca2-b367-fe25e4964a4e	\N	26	-1	1
605	6	6	f	0	0	0	1a0b110f-1e09-4ca2-b367-fe25e4964a4e	\N	165	-1	1
606	6	6	f	0	0	0	Project Contract.pdf	\N	26	-1	1
606	21	3	f	157	0	0	\N	\N	51	-1	1
606	12	6	f	0	0	0	workspace://SpacesStore/1a0b110f-1e09-4ca2-b367-fe25e4964a4e	\N	167	-1	1
606	0	0	f	0	0	0	\N	\N	168	-1	1
606	3	3	f	604	0	0	\N	\N	170	-1	1
606	1	1	f	0	0	0	\N	\N	155	-1	1
606	0	0	f	0	0	0	\N	\N	171	-1	1
606	1	1	t	0	0	0	\N	\N	158	-1	1
606	1	1	t	0	0	0	\N	\N	159	-1	1
606	6	6	f	0	0	0	Contract for the Green Energy project	\N	27	-1	2
606	6	6	f	0	0	0	Project Contract for Green Energy	\N	28	-1	2
606	6	6	f	0	0	0	admin	\N	40	-1	1
606	6	6	f	0	0	0	abeecher	\N	172	-1	1
606	7	6	f	0	0	0	2011-02-15T21:26:54.600Z	\N	173	-1	1
606	6	6	f	0	0	0	admin	\N	174	-1	1
606	7	6	f	0	0	0	2011-06-14T10:28:54.714Z	\N	175	-1	1
606	0	0	f	0	0	0	\N	\N	176	-1	1
606	4	4	f	0	0	0	\N	\N	156	-1	1
606	2	3	f	0	0	0	\N	\N	160	-1	1
606	6	6	f	0	0	0	Alice Beecher	\N	54	-1	1
604	0	0	f	0	0	0	\N	\N	171	-1	1
604	6	6	f	0	0	0	1.1	\N	157	-1	1
606	0	0	f	0	0	0	\N	\N	157	-1	1
606	6	6	f	0	0	0	1.1	\N	169	-1	1
607	6	6	f	0	0	0	doclib	\N	26	-1	1
607	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
607	6	6	f	0	0	0	admin	\N	40	-1	1
607	21	3	f	158	0	0	\N	\N	51	-1	1
608	6	6	f	0	0	0	webpreview	\N	26	-1	1
608	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
608	6	6	f	0	0	0	admin	\N	40	-1	1
608	21	3	f	159	0	0	\N	\N	51	-1	1
609	6	6	f	0	0	0	admin	\N	40	-1	1
609	6	6	f	0	0	0	Images	\N	26	-1	1
609	6	6	f	0	0	0	This folder holds new web site images	\N	27	-1	2
609	6	6	f	0	0	0	Project images	\N	28	-1	2
610	6	6	f	0	0	0	admin	\N	40	-1	1
610	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
610	5	5	f	0	0	22	\N	\N	179	-1	1
610	5	5	f	0	0	72	\N	\N	180	-1	1
610	6	6	f	0	0	0	Inch	\N	181	-1	1
610	7	6	f	0	0	0	2003-09-23T14:55:24.000Z	\N	182	-1	1
610	2	3	f	1	0	0	\N	\N	183	-1	1
610	2	3	f	840	0	0	\N	\N	184	-1	1
610	6	6	f	0	0	0	E-10	\N	185	-1	1
610	6	6	f	0	0	0	coins.JPG	\N	26	-1	1
610	5	5	f	0	0	72	\N	\N	186	-1	1
610	6	6	f	0	0	0	OLYMPUS DIGITAL CAMERA	\N	27	-1	4
610	1	1	f	0	0	0	\N	\N	187	-1	1
610	6	6	f	0	0	0	coins.JPG	\N	28	-1	4
610	5	5	f	0	0	9	\N	\N	188	-1	1
610	6	6	f	0	0	0	80	\N	189	-1	1
610	6	6	f	0	0	0	OLYMPUS OPTICAL CO.,LTD	\N	190	-1	1
610	2	3	f	1120	0	0	\N	\N	191	-1	1
610	5	5	f	0	0	0.00312500000000000017	\N	\N	192	-1	1
610	21	3	f	160	0	0	\N	\N	51	-1	1
611	6	6	f	0	0	0	admin	\N	40	-1	1
611	6	6	f	0	0	0	doclib	\N	26	-1	1
611	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
611	6	6	f	0	0	0	doclib	\N	144	-1	1
611	21	3	f	161	0	0	\N	\N	51	-1	1
612	6	6	f	0	0	0	admin	\N	40	-1	1
612	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
612	5	5	f	0	0	230	\N	\N	179	-1	1
612	5	5	f	0	0	72	\N	\N	180	-1	1
612	6	6	f	0	0	0	Inch	\N	181	-1	1
612	7	6	f	0	0	0	2003-12-30T15:17:54.000Z	\N	182	-1	1
612	2	3	f	1	0	0	\N	\N	183	-1	1
612	2	3	f	664	0	0	\N	\N	184	-1	1
612	6	6	f	0	0	0	PENTAX K20D	\N	185	-1	1
612	6	6	f	0	0	0	graph.JPG	\N	26	-1	1
612	5	5	f	0	0	72	\N	\N	186	-1	1
612	1	1	f	0	0	0	\N	\N	187	-1	1
612	6	6	f	0	0	0	graph.JPG	\N	28	-1	4
612	5	5	f	0	0	6.29999999999999982	\N	\N	188	-1	1
612	6	6	f	0	0	0	100	\N	189	-1	1
612	6	6	f	0	0	0	PENTAX Corporation	\N	190	-1	1
612	2	3	f	1000	0	0	\N	\N	191	-1	1
612	5	5	f	0	0	0.0055555555555555601	\N	\N	192	-1	1
612	21	3	f	162	0	0	\N	\N	51	-1	1
613	6	6	f	0	0	0	admin	\N	40	-1	1
613	6	6	f	0	0	0	doclib	\N	26	-1	1
613	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
613	6	6	f	0	0	0	doclib	\N	144	-1	1
613	21	3	f	163	0	0	\N	\N	51	-1	1
614	6	6	f	0	0	0	admin	\N	40	-1	1
614	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
614	5	5	f	0	0	23.4200000000000017	\N	\N	179	-1	1
614	5	5	f	0	0	72	\N	\N	180	-1	1
614	6	6	f	0	0	0	Inch	\N	181	-1	1
614	7	6	f	0	0	0	2003-12-30T15:17:54.000Z	\N	182	-1	1
614	2	3	f	1	0	0	\N	\N	183	-1	1
614	2	3	f	754	0	0	\N	\N	184	-1	1
614	6	6	f	0	0	0	HP PhotoSmart C850 (V05.27)	\N	185	-1	1
614	6	6	f	0	0	0	grass.jpg	\N	26	-1	1
614	5	5	f	0	0	72	\N	\N	186	-1	1
614	1	1	t	0	0	0	\N	\N	187	-1	1
614	6	6	f	0	0	0	grass.jpg	\N	28	-1	4
614	5	5	f	0	0	3	\N	\N	188	-1	1
614	6	6	f	0	0	0	100	\N	189	-1	1
614	6	6	f	0	0	0	Hewlett-Packard	\N	190	-1	1
614	2	3	f	1000	0	0	\N	\N	191	-1	1
614	5	5	f	0	0	0.00800000000000000017	\N	\N	192	-1	1
614	21	3	f	164	0	0	\N	\N	51	-1	1
615	6	6	f	0	0	0	admin	\N	40	-1	1
615	6	6	f	0	0	0	doclib	\N	26	-1	1
615	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
615	6	6	f	0	0	0	doclib	\N	144	-1	1
615	21	3	f	165	0	0	\N	\N	51	-1	1
616	6	6	f	0	0	0	admin	\N	40	-1	1
616	6	6	f	0	0	0	imgpreview	\N	26	-1	1
616	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
616	6	6	f	0	0	0	imgpreview	\N	144	-1	1
616	21	3	f	166	0	0	\N	\N	51	-1	1
617	6	6	f	0	0	0	admin	\N	40	-1	1
617	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
617	5	5	f	0	0	8	\N	\N	179	-1	1
617	5	5	f	0	0	72	\N	\N	180	-1	1
617	6	6	f	0	0	0	Inch	\N	181	-1	1
617	7	6	f	0	0	0	2005-07-06T09:07:21.000Z	\N	182	-1	1
617	2	3	f	1	0	0	\N	\N	183	-1	1
617	2	3	f	1932	0	0	\N	\N	184	-1	1
617	6	6	f	0	0	0	KODAK DX4530 ZOOM DIGITAL CAMERA	\N	185	-1	1
617	6	6	f	0	0	0	money.JPG	\N	26	-1	1
617	5	5	f	0	0	72	\N	\N	186	-1	1
617	1	1	f	0	0	0	\N	\N	187	-1	1
617	6	6	f	0	0	0	money.JPG	\N	28	-1	4
617	5	5	f	0	0	2.79999999999999982	\N	\N	188	-1	1
617	6	6	f	0	0	0	200	\N	189	-1	1
617	6	6	f	0	0	0	EASTMAN KODAK COMPANY	\N	190	-1	1
617	2	3	f	2580	0	0	\N	\N	191	-1	1
617	5	5	f	0	0	0.0333333333333332982	\N	\N	192	-1	1
617	21	3	f	167	0	0	\N	\N	51	-1	1
618	6	6	f	0	0	0	admin	\N	40	-1	1
618	6	6	f	0	0	0	doclib	\N	26	-1	1
618	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
618	6	6	f	0	0	0	doclib	\N	144	-1	1
618	21	3	f	168	0	0	\N	\N	51	-1	1
619	6	6	f	0	0	0	admin	\N	40	-1	1
619	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
619	5	5	f	0	0	16	\N	\N	179	-1	1
619	5	5	f	0	0	72	\N	\N	180	-1	1
619	6	6	f	0	0	0	Inch	\N	181	-1	1
619	7	6	f	0	0	0	2008-12-13T17:05:16.000Z	\N	182	-1	1
619	2	3	f	1	0	0	\N	\N	183	-1	1
619	2	3	f	2448	0	0	\N	\N	184	-1	1
619	6	6	f	0	0	0	Canon PowerShot A590 IS	\N	185	-1	1
619	6	6	f	0	0	0	plugs.jpg	\N	26	-1	1
619	5	5	f	0	0	72	\N	\N	186	-1	1
619	1	1	f	0	0	0	\N	\N	187	-1	1
619	6	6	f	0	0	0	plugs.jpg	\N	28	-1	4
619	5	5	f	0	0	4.5	\N	\N	188	-1	1
619	6	6	f	0	0	0	200	\N	189	-1	1
619	6	6	f	0	0	0	Canon	\N	190	-1	1
619	2	3	f	3264	0	0	\N	\N	191	-1	1
619	5	5	f	0	0	0.0333333333333332982	\N	\N	192	-1	1
619	21	3	f	169	0	0	\N	\N	51	-1	1
620	6	6	f	0	0	0	admin	\N	40	-1	1
620	6	6	f	0	0	0	doclib	\N	26	-1	1
620	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
620	6	6	f	0	0	0	doclib	\N	144	-1	1
620	21	3	f	170	0	0	\N	\N	51	-1	1
621	6	6	f	0	0	0	admin	\N	40	-1	1
621	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
621	5	5	f	0	0	18	\N	\N	179	-1	1
621	5	5	f	0	0	72	\N	\N	180	-1	1
621	6	6	f	0	0	0	Inch	\N	181	-1	1
621	7	6	f	0	0	0	2005-01-09T16:00:55.000Z	\N	182	-1	1
621	2	3	f	1	0	0	\N	\N	183	-1	1
621	2	3	f	2048	0	0	\N	\N	184	-1	1
621	6	6	f	0	0	0	Canon EOS 300D DIGITAL	\N	185	-1	1
621	6	6	f	0	0	0	turbine.JPG	\N	26	-1	1
621	5	5	f	0	0	72	\N	\N	186	-1	1
621	1	1	t	0	0	0	\N	\N	187	-1	1
621	6	6	f	0	0	0	turbine.JPG	\N	28	-1	4
621	5	5	f	0	0	3.5	\N	\N	188	-1	1
621	6	6	f	0	0	0	400	\N	189	-1	1
621	6	6	f	0	0	0	Canon	\N	190	-1	1
621	2	3	f	3072	0	0	\N	\N	191	-1	1
621	5	5	f	0	0	0.400000000000000022	\N	\N	192	-1	1
621	21	3	f	171	0	0	\N	\N	51	-1	1
622	6	6	f	0	0	0	admin	\N	40	-1	1
622	6	6	f	0	0	0	doclib	\N	26	-1	1
622	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
622	6	6	f	0	0	0	doclib	\N	144	-1	1
622	21	3	f	172	0	0	\N	\N	51	-1	1
623	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
623	5	5	f	0	0	72	\N	\N	180	-1	1
623	6	6	f	0	0	0	Inch	\N	181	-1	1
623	2	3	f	1	0	0	\N	\N	183	-1	1
623	6	6	f	0	0	0	admin	\N	40	-1	1
623	2	3	f	912	0	0	\N	\N	184	-1	1
623	5	5	f	0	0	72	\N	\N	186	-1	1
623	6	6	f	0	0	0	wires.JPG	\N	26	-1	1
623	6	6	f	0	0	0	wires.JPG	\N	28	-1	4
623	2	3	f	1216	0	0	\N	\N	191	-1	1
623	21	3	f	173	0	0	\N	\N	51	-1	1
624	6	6	f	0	0	0	admin	\N	40	-1	1
624	6	6	f	0	0	0	doclib	\N	26	-1	1
624	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
624	6	6	f	0	0	0	doclib	\N	144	-1	1
624	21	3	f	174	0	0	\N	\N	51	-1	1
625	6	6	f	0	0	0	Adobe Photoshop CS5 Macintosh	\N	178	-1	1
625	5	5	f	0	0	72	\N	\N	180	-1	1
625	6	6	f	0	0	0	Inch	\N	181	-1	1
625	2	3	f	1	0	0	\N	\N	183	-1	1
625	6	6	f	0	0	0	admin	\N	40	-1	1
625	2	3	f	3008	0	0	\N	\N	184	-1	1
625	5	5	f	0	0	72	\N	\N	186	-1	1
625	6	6	f	0	0	0	wind turbine.JPG	\N	26	-1	1
625	6	6	f	0	0	0	wind turbine.JPG	\N	28	-1	4
625	2	3	f	2000	0	0	\N	\N	191	-1	1
625	21	3	f	175	0	0	\N	\N	51	-1	1
626	6	6	f	0	0	0	admin	\N	40	-1	1
626	6	6	f	0	0	0	doclib	\N	26	-1	1
626	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
626	6	6	f	0	0	0	doclib	\N	144	-1	1
626	21	3	f	176	0	0	\N	\N	51	-1	1
627	6	6	f	0	0	0	admin	\N	40	-1	1
627	2	3	f	217	0	0	\N	\N	184	-1	1
627	6	6	f	0	0	0	header.png	\N	26	-1	1
627	6	6	f	0	0	0	header.png	\N	28	-1	4
627	2	3	f	793	0	0	\N	\N	191	-1	1
627	21	3	f	177	0	0	\N	\N	51	-1	1
628	6	6	f	0	0	0	admin	\N	40	-1	1
628	6	6	f	0	0	0	doclib	\N	26	-1	1
628	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
628	6	6	f	0	0	0	doclib	\N	144	-1	1
628	21	3	f	178	0	0	\N	\N	51	-1	1
629	6	6	f	0	0	0	admin	\N	40	-1	1
629	2	3	f	1000	0	0	\N	\N	184	-1	1
629	6	6	f	0	0	0	windmill.png	\N	26	-1	1
629	6	6	f	0	0	0	windmill.png	\N	28	-1	4
629	2	3	f	591	0	0	\N	\N	191	-1	1
629	21	3	f	179	0	0	\N	\N	51	-1	1
630	6	6	f	0	0	0	admin	\N	40	-1	1
630	6	6	f	0	0	0	doclib	\N	26	-1	1
630	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
630	6	6	f	0	0	0	doclib	\N	144	-1	1
630	21	3	f	180	0	0	\N	\N	51	-1	1
631	6	6	f	0	0	0	admin	\N	40	-1	1
631	2	3	f	245	0	0	\N	\N	184	-1	1
631	6	6	f	0	0	0	low consumption bulb.png	\N	26	-1	1
631	6	6	f	0	0	0	low consumption bulb.png	\N	28	-1	4
631	2	3	f	625	0	0	\N	\N	191	-1	1
631	21	3	f	181	0	0	\N	\N	51	-1	1
632	6	6	f	0	0	0	admin	\N	40	-1	1
632	6	6	f	0	0	0	doclib	\N	26	-1	1
632	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
632	6	6	f	0	0	0	doclib	\N	144	-1	1
632	21	3	f	182	0	0	\N	\N	51	-1	1
633	6	6	f	0	0	0	admin	\N	40	-1	1
633	6	6	f	0	0	0	Logo Files	\N	26	-1	1
633	6	6	f	0	0	0	This folder holds new logo files for the web site	\N	27	-1	2
633	6	6	f	0	0	0	Project logo files	\N	28	-1	2
634	6	6	f	0	0	0	admin	\N	40	-1	1
634	2	3	f	192	0	0	\N	\N	184	-1	1
634	6	6	f	0	0	0	GE Logo.png	\N	26	-1	1
634	4	4	f	0	1	0	\N	\N	156	-1	1
634	6	6	f	0	0	0	GE Logo.png	\N	28	-1	4
634	2	3	f	400	0	0	\N	\N	191	-1	1
634	2	3	f	1	0	0	\N	\N	160	-1	1
634	21	3	f	183	0	0	\N	\N	51	-1	1
635	6	6	f	0	0	0	admin	\N	40	-1	1
635	6	6	f	0	0	0	doclib	\N	26	-1	1
635	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
635	6	6	f	0	0	0	doclib	\N	144	-1	1
635	21	3	f	184	0	0	\N	\N	51	-1	1
636	6	6	f	0	0	0	admin	\N	40	-1	1
636	2	3	f	398	0	0	\N	\N	184	-1	1
636	6	6	f	0	0	0	logo.png	\N	26	-1	1
636	4	4	f	0	1	0	\N	\N	156	-1	1
636	6	6	f	0	0	0	logo.png	\N	28	-1	4
636	2	3	f	414	0	0	\N	\N	191	-1	1
636	2	3	f	1	0	0	\N	\N	160	-1	1
636	21	3	f	185	0	0	\N	\N	51	-1	1
637	6	6	f	0	0	0	admin	\N	40	-1	1
637	6	6	f	0	0	0	doclib	\N	26	-1	1
637	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
637	6	6	f	0	0	0	doclib	\N	144	-1	1
637	21	3	f	186	0	0	\N	\N	51	-1	1
638	6	6	f	0	0	0	admin	\N	40	-1	1
638	6	6	f	0	0	0	Mock-Ups	\N	26	-1	1
638	6	6	f	0	0	0	This folder holds the web site mock-ups or wireframes	\N	27	-1	2
638	6	6	f	0	0	0	Web wireframes	\N	28	-1	2
639	6	6	f	0	0	0	admin	\N	40	-1	1
639	2	3	f	893	0	0	\N	\N	184	-1	1
639	6	6	f	0	0	0	sample 1.png	\N	26	-1	1
639	6	6	f	0	0	0	sample 1.png	\N	28	-1	4
639	2	3	f	1067	0	0	\N	\N	191	-1	1
639	21	3	f	187	0	0	\N	\N	51	-1	1
640	6	6	f	0	0	0	admin	\N	40	-1	1
640	6	6	f	0	0	0	doclib	\N	26	-1	1
640	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
640	6	6	f	0	0	0	doclib	\N	144	-1	1
640	21	3	f	188	0	0	\N	\N	51	-1	1
641	6	6	f	0	0	0	admin	\N	40	-1	1
641	2	3	f	921	0	0	\N	\N	184	-1	1
641	6	6	f	0	0	0	sample 2.png	\N	26	-1	1
641	6	6	f	0	0	0	sample 2.png	\N	28	-1	4
641	2	3	f	778	0	0	\N	\N	191	-1	1
641	21	3	f	189	0	0	\N	\N	51	-1	1
642	6	6	f	0	0	0	admin	\N	40	-1	1
642	6	6	f	0	0	0	doclib	\N	26	-1	1
642	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
642	6	6	f	0	0	0	doclib	\N	144	-1	1
642	21	3	f	190	0	0	\N	\N	51	-1	1
643	6	6	f	0	0	0	admin	\N	40	-1	1
643	6	6	f	0	0	0	imgpreview	\N	26	-1	1
643	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
643	6	6	f	0	0	0	imgpreview	\N	144	-1	1
643	21	3	f	191	0	0	\N	\N	51	-1	1
644	6	6	f	0	0	0	admin	\N	40	-1	1
644	2	3	f	769	0	0	\N	\N	184	-1	1
644	6	6	f	0	0	0	sample 3.png	\N	26	-1	1
644	4	4	f	0	2	0	\N	\N	156	-1	1
644	6	6	f	0	0	0	sample 3.png	\N	28	-1	4
644	2	3	f	782	0	0	\N	\N	191	-1	1
644	2	3	f	2	0	0	\N	\N	160	-1	1
644	21	3	f	192	0	0	\N	\N	51	-1	1
645	6	6	f	0	0	0	admin	\N	40	-1	1
645	6	6	f	0	0	0	doclib	\N	26	-1	1
645	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
645	6	6	f	0	0	0	doclib	\N	144	-1	1
645	21	3	f	193	0	0	\N	\N	51	-1	1
646	6	6	f	0	0	0	admin	\N	40	-1	1
646	6	6	f	0	0	0	imgpreview	\N	26	-1	1
646	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
646	6	6	f	0	0	0	imgpreview	\N	144	-1	1
646	21	3	f	194	0	0	\N	\N	51	-1	1
647	6	6	f	0	0	0	admin	\N	40	-1	1
647	6	6	f	0	0	0	Video Files	\N	26	-1	1
647	6	6	f	0	0	0	This folder holds any video files related to the project	\N	27	-1	4
647	6	6	f	0	0	0	Folder for video files	\N	28	-1	4
648	6	6	f	0	0	0	Created by John Cavendish	\N	54	-1	1
648	6	6	f	0	0	0	admin	\N	40	-1	1
648	6	6	f	0	0	0	WebSiteReview.mp4	\N	26	-1	1
648	6	6	f	0	0	0	This is a video of the mock up to show the planned structure for the new web site.	\N	27	-1	4
648	6	6	f	0	0	0	WebSiteReview.mp4	\N	28	-1	4
648	21	3	f	195	0	0	\N	\N	51	-1	1
649	6	6	f	0	0	0	admin	\N	40	-1	1
649	6	6	f	0	0	0	Budget Files	\N	26	-1	1
649	6	6	f	0	0	0	This folder holds the project budget and invoices	\N	27	-1	2
649	6	6	f	0	0	0	Project finance files	\N	28	-1	2
650	6	6	f	0	0	0	admin	\N	40	-1	1
650	6	6	f	0	0	0	Invoices	\N	26	-1	1
650	6	6	f	0	0	0	This folder holds invoices for the project	\N	27	-1	2
650	6	6	f	0	0	0	Project invoices	\N	28	-1	2
651	6	6	f	0	0	0	admin	\N	40	-1	1
651	2	3	f	974	0	0	\N	\N	184	-1	1
651	6	6	f	0	0	0	inv I200-109.png	\N	26	-1	1
651	6	6	f	0	0	0	inv I200-109.png	\N	28	-1	4
651	2	3	f	749	0	0	\N	\N	191	-1	1
651	21	3	f	196	0	0	\N	\N	51	-1	1
652	6	6	f	0	0	0	admin	\N	40	-1	1
652	6	6	f	0	0	0	doclib	\N	26	-1	1
652	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
652	6	6	f	0	0	0	doclib	\N	144	-1	1
652	21	3	f	197	0	0	\N	\N	51	-1	1
653	6	6	f	0	0	0	admin	\N	40	-1	1
653	2	3	f	970	0	0	\N	\N	184	-1	1
653	6	6	f	0	0	0	inv I200-189.png	\N	26	-1	1
653	6	6	f	0	0	0	inv I200-189.png	\N	28	-1	4
653	2	3	f	751	0	0	\N	\N	191	-1	1
653	21	3	f	198	0	0	\N	\N	51	-1	1
654	6	6	f	0	0	0	admin	\N	40	-1	1
654	6	6	f	0	0	0	doclib	\N	26	-1	1
654	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
654	6	6	f	0	0	0	doclib	\N	144	-1	1
654	21	3	f	199	0	0	\N	\N	51	-1	1
655	6	6	f	0	0	0	Mike Jackson	\N	54	-1	1
655	6	6	f	0	0	0	admin	\N	40	-1	1
655	6	6	f	0	0	0	budget.xls	\N	26	-1	1
655	6	6	f	0	0	0	Budget file for the web site redesign	\N	27	-1	2
655	6	6	f	0	0	0	Web Site Design - Budget	\N	28	-1	2
655	21	3	f	200	0	0	\N	\N	51	-1	1
656	6	6	f	0	0	0	admin	\N	40	-1	1
656	6	6	f	0	0	0	doclib	\N	26	-1	1
656	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
656	6	6	f	0	0	0	doclib	\N	144	-1	1
656	21	3	f	201	0	0	\N	\N	51	-1	1
657	6	6	f	0	0	0	admin	\N	40	-1	1
657	6	6	f	0	0	0	webpreview	\N	26	-1	1
657	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
657	6	6	f	0	0	0	webpreview	\N	144	-1	1
657	21	3	f	202	0	0	\N	\N	51	-1	1
658	6	6	f	0	0	0	budget.xls discussion	\N	26	-1	1
658	6	6	f	0	0	0	forum	\N	29	-1	1
658	6	6	f	0	0	0	admin	\N	40	-1	1
659	6	6	f	0	0	0	Comments	\N	26	-1	1
659	6	6	f	0	0	0	admin	\N	40	-1	1
660	6	6	f	0	0	0	comment-1297852210661_622	\N	26	-1	1
660	6	6	f	0	0	0		\N	28	-1	2
660	6	6	f	0	0	0	admin	\N	40	-1	1
660	21	3	f	203	0	0	\N	\N	51	-1	1
661	6	6	f	0	0	0	admin	\N	40	-1	1
661	6	6	f	0	0	0	Meeting Notes	\N	26	-1	1
661	6	6	f	0	0	0	This folder holds notes from the project review meetings	\N	27	-1	2
661	6	6	f	0	0	0	Project meeting notes	\N	28	-1	2
662	6	6	f	0	0	0	Meeting Notes 2011-01-27.doc	\N	26	-1	1
662	6	6	f	0	0	0	Meeting Notes 2011-01-27.doc	\N	28	-1	2
662	6	6	f	0	0	0	admin	\N	40	-1	1
662	21	3	f	204	0	0	\N	\N	51	-1	1
663	6	6	f	0	0	0	admin	\N	40	-1	1
663	6	6	f	0	0	0	doclib	\N	26	-1	1
663	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
663	6	6	f	0	0	0	doclib	\N	144	-1	1
663	21	3	f	205	0	0	\N	\N	51	-1	1
664	6	6	f	0	0	0	admin	\N	40	-1	1
664	6	6	f	0	0	0	webpreview	\N	26	-1	1
664	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
664	6	6	f	0	0	0	webpreview	\N	144	-1	1
664	21	3	f	206	0	0	\N	\N	51	-1	1
665	6	6	f	0	0	0	Meeting Notes 2011-02-03.doc	\N	26	-1	1
665	6	6	f	0	0	0	Meeting Notes 2011-02-03.doc	\N	28	-1	2
665	6	6	f	0	0	0	admin	\N	40	-1	1
665	21	3	f	207	0	0	\N	\N	51	-1	1
666	6	6	f	0	0	0	admin	\N	40	-1	1
666	6	6	f	0	0	0	doclib	\N	26	-1	1
666	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
666	6	6	f	0	0	0	doclib	\N	144	-1	1
666	21	3	f	208	0	0	\N	\N	51	-1	1
667	6	6	f	0	0	0	admin	\N	40	-1	1
667	6	6	f	0	0	0	webpreview	\N	26	-1	1
667	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
667	6	6	f	0	0	0	webpreview	\N	144	-1	1
667	21	3	f	209	0	0	\N	\N	51	-1	1
668	6	6	f	0	0	0	Meeting Notes 2011-02-10.doc	\N	26	-1	1
668	6	6	f	0	0	0	Meeting Notes 2011-02-10.doc	\N	28	-1	2
668	6	6	f	0	0	0	admin	\N	40	-1	1
668	21	3	f	210	0	0	\N	\N	51	-1	1
669	6	6	f	0	0	0	admin	\N	40	-1	1
669	6	6	f	0	0	0	doclib	\N	26	-1	1
669	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
669	6	6	f	0	0	0	doclib	\N	144	-1	1
669	21	3	f	211	0	0	\N	\N	51	-1	1
670	6	6	f	0	0	0	admin	\N	40	-1	1
670	6	6	f	0	0	0	webpreview	\N	26	-1	1
670	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
670	6	6	f	0	0	0	webpreview	\N	144	-1	1
670	21	3	f	212	0	0	\N	\N	51	-1	1
671	6	6	f	0	0	0	admin	\N	40	-1	1
671	6	6	f	0	0	0	Presentations	\N	26	-1	1
671	6	6	f	0	0	0	This folder holds presentations from the project	\N	27	-1	2
671	6	6	f	0	0	0	Project presentations	\N	28	-1	2
672	6	6	f	0	0	0	Project Objectives.ppt	\N	26	-1	1
672	6	6	f	0	0	0	Project Objectives.ppt	\N	28	-1	4
672	6	6	f	0	0	0	admin	\N	40	-1	1
672	21	3	f	213	0	0	\N	\N	51	-1	1
673	6	6	f	0	0	0	admin	\N	40	-1	1
673	6	6	f	0	0	0	doclib	\N	26	-1	1
673	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
673	6	6	f	0	0	0	doclib	\N	144	-1	1
673	21	3	f	214	0	0	\N	\N	51	-1	1
674	6	6	f	0	0	0	admin	\N	40	-1	1
674	6	6	f	0	0	0	webpreview	\N	26	-1	1
674	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
674	6	6	f	0	0	0	webpreview	\N	144	-1	1
674	21	3	f	215	0	0	\N	\N	51	-1	1
675	6	6	f	0	0	0	Project Overview.ppt	\N	26	-1	1
675	6	6	f	0	0	0	Project Overview.ppt	\N	28	-1	4
675	6	6	f	0	0	0	admin	\N	40	-1	1
675	21	3	f	216	0	0	\N	\N	51	-1	1
676	6	6	f	0	0	0	admin	\N	40	-1	1
676	6	6	f	0	0	0	doclib	\N	26	-1	1
676	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
676	6	6	f	0	0	0	doclib	\N	144	-1	1
676	21	3	f	217	0	0	\N	\N	51	-1	1
677	6	6	f	0	0	0	admin	\N	40	-1	1
677	6	6	f	0	0	0	webpreview	\N	26	-1	1
677	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
677	6	6	f	0	0	0	webpreview	\N	144	-1	1
677	21	3	f	218	0	0	\N	\N	51	-1	1
678	6	6	f	0	0	0	links	\N	26	-1	1
678	6	6	f	0	0	0	admin	\N	40	-1	1
678	6	6	f	0	0	0	links	\N	152	-1	1
679	6	6	f	0	0	0	The W3 Schools web site has some good guides (with interactive examples) on how to create websites	\N	200	-1	1
679	6	6	f	0	0	0	admin	\N	40	-1	1
679	6	6	f	0	0	0	W3 Schools	\N	201	-1	1
679	6	6	f	0	0	0	link-1297806194371_850	\N	26	-1	1
679	6	6	f	0	0	0	http://www.w3schools.com/	\N	202	-1	1
679	21	3	f	219	0	0	\N	\N	51	-1	1
680	6	6	f	0	0	0	W3C website. Includes some good guides to web design and application	\N	200	-1	1
680	6	6	f	0	0	0	admin	\N	40	-1	1
680	6	6	f	0	0	0	Web Design and Applications	\N	201	-1	1
680	6	6	f	0	0	0	link-1297806244007_178	\N	26	-1	1
680	6	6	f	0	0	0	http://www.w3.org/standards/webdesign/	\N	202	-1	1
680	21	3	f	220	0	0	\N	\N	51	-1	1
681	6	6	f	0	0	0	admin	\N	40	-1	1
681	6	6	f	0	0	0	dataLists	\N	152	-1	1
681	6	6	f	0	0	0	dataLists	\N	26	-1	1
681	6	6	f	0	0	0	Data Lists	\N	27	-1	2
682	6	6	f	0	0	0	admin	\N	40	-1	1
682	6	6	f	0	0	0	71824d77-9cd8-44c3-b3e4-dbca7e17dc49	\N	26	-1	1
682	6	6	f	0	0	0	Project issues	\N	27	-1	2
682	6	6	f	0	0	0	Issue Log	\N	28	-1	2
682	6	6	f	0	0	0	dl:issue	\N	204	-1	1
683	7	6	f	0	0	0	2011-03-09T00:00:00.000Z	\N	209	-1	1
683	6	6	f	0	0	0	Issue 1	\N	210	-1	1
683	6	6	f	0	0	0	admin	\N	40	-1	1
683	6	6	f	0	0	0	e6fc15e9-5caf-4f17-857e-7b0cfbc655a9	\N	26	-1	1
683	6	6	f	0	0	0	Support need to be able to access and update content of the corporate web site. Need to find a solution.	\N	27	-1	2
683	6	6	f	0	0	0	Support access	\N	28	-1	2
683	6	6	f	0	0	0	Not Started	\N	206	-1	1
683	6	6	f	0	0	0	Normal	\N	207	-1	1
683	6	6	f	0	0	0		\N	208	-1	1
684	7	6	f	0	0	0	2011-02-24T00:00:00.000Z	\N	209	-1	1
684	6	6	f	0	0	0	Issue 3	\N	210	-1	1
684	6	6	f	0	0	0	admin	\N	40	-1	1
684	6	6	f	0	0	0	42fcbae6-b1fe-4028-9f85-9ad7f81a8e3b	\N	26	-1	1
684	6	6	f	0	0	0	The budget has been cut. Need to address the cuts and work out how accomodate the project.	\N	27	-1	2
684	6	6	f	0	0	0	Budget cut	\N	28	-1	2
684	6	6	f	0	0	0	Not Started	\N	206	-1	1
684	6	6	f	0	0	0	High	\N	207	-1	1
684	6	6	f	0	0	0	Looking to use an Open Source solution	\N	208	-1	1
685	7	6	f	0	0	0	2011-03-02T00:00:00.000Z	\N	209	-1	1
685	6	6	f	0	0	0	Issue 2	\N	210	-1	1
685	6	6	f	0	0	0	admin	\N	40	-1	1
685	6	6	f	0	0	0	66028f46-c074-4cf5-9f37-8490e51ca540	\N	26	-1	1
685	6	6	f	0	0	0	There is an issue with the copyright of one of the images selected. Need to source a replacement.	\N	27	-1	2
685	6	6	f	0	0	0	Copyright issue	\N	28	-1	2
685	6	6	f	0	0	0	In Progress	\N	206	-1	1
685	6	6	f	0	0	0	High	\N	207	-1	1
685	6	6	f	0	0	0	Alice is actively trying to sort this	\N	208	-1	1
686	7	6	f	0	0	0	2011-02-17T00:00:00.000Z	\N	209	-1	1
686	6	6	f	0	0	0	Issue 4	\N	210	-1	1
686	6	6	f	0	0	0	admin	\N	40	-1	1
686	6	6	f	0	0	0	50046ccd-9034-420f-925b-0530836488c4	\N	26	-1	1
686	6	6	f	0	0	0	The Web Manager has resigned. Need to find a replacement.	\N	27	-1	2
686	6	6	f	0	0	0	Web Manager	\N	28	-1	2
686	6	6	f	0	0	0	Complete	\N	206	-1	1
686	6	6	f	0	0	0	High	\N	207	-1	1
686	6	6	f	0	0	0	This has been solved. Izzy Previn has joined the team.	\N	208	-1	1
687	6	6	f	0	0	0	admin	\N	40	-1	1
687	6	6	f	0	0	0	aea88103-517e-4aa0-a3be-de258d0e6465	\N	26	-1	1
687	6	6	f	0	0	0	Project to do list	\N	27	-1	4
687	6	6	f	0	0	0	Project to do list	\N	27	-1	2
687	6	6	f	0	0	0	To-Do	\N	28	-1	4
687	6	6	f	0	0	0	Task Log	\N	28	-1	2
687	6	6	f	0	0	0	dl:todoList	\N	204	-1	1
688	6	6	f	0	0	0	Not Started	\N	213	-1	1
688	2	3	f	3	0	0	\N	\N	214	-1	1
688	6	6	f	0	0	0		\N	215	-1	1
688	6	6	f	0	0	0	Revise Project Objectives	\N	216	-1	1
688	6	6	f	0	0	0	admin	\N	40	-1	1
688	7	6	f	0	0	0	2011-03-08T00:00:00.000Z	\N	217	-1	1
688	6	6	f	0	0	0	9198bd31-a664-4584-a271-b529daf4793b	\N	26	-1	1
689	6	6	f	0	0	0	In Progress	\N	213	-1	1
689	2	3	f	1	0	0	\N	\N	214	-1	1
689	6	6	f	0	0	0		\N	215	-1	1
689	6	6	f	0	0	0	Update budget	\N	216	-1	1
689	6	6	f	0	0	0	admin	\N	40	-1	1
689	7	6	f	0	0	0	2011-03-14T00:00:00.000Z	\N	217	-1	1
689	6	6	f	0	0	0	eb1c2fda-4868-4384-b29e-78c01b6601ec	\N	26	-1	1
690	6	6	f	0	0	0	On Hold	\N	213	-1	1
690	2	3	f	5	0	0	\N	\N	214	-1	1
690	6	6	f	0	0	0		\N	215	-1	1
690	6	6	f	0	0	0	Upload new images	\N	216	-1	1
690	6	6	f	0	0	0	admin	\N	40	-1	1
690	7	6	f	0	0	0	2011-03-16T00:00:00.000Z	\N	217	-1	1
690	6	6	f	0	0	0	35b8be80-170f-40af-a173-513758b83165	\N	26	-1	1
691	6	6	f	0	0	0	Complete	\N	213	-1	1
691	2	3	f	2	0	0	\N	\N	214	-1	1
691	6	6	f	0	0	0		\N	215	-1	1
691	6	6	f	0	0	0	Contract	\N	216	-1	1
691	6	6	f	0	0	0	admin	\N	40	-1	1
691	7	6	f	0	0	0	2011-02-01T00:00:00.000Z	\N	217	-1	1
691	6	6	f	0	0	0	567ee439-4ebc-40cf-a783-3e561ad5a605	\N	26	-1	1
692	6	6	f	0	0	0	Not Started	\N	213	-1	1
692	2	3	f	2	0	0	\N	\N	214	-1	1
692	6	6	f	0	0	0	Please take a look at the structure video and provide your feedback	\N	215	-1	1
692	6	6	f	0	0	0	Review the web structure video	\N	216	-1	1
692	6	6	f	0	0	0	admin	\N	40	-1	1
692	7	6	f	0	0	0	2011-03-30T23:00:00.000Z	\N	217	-1	1
692	6	6	f	0	0	0	7a0bb872-bf7c-457b-831e-95f94efb9816	\N	26	-1	1
693	6	6	f	0	0	0	wiki	\N	26	-1	1
693	6	6	f	0	0	0	admin	\N	40	-1	1
693	6	6	f	0	0	0	wiki	\N	152	-1	1
694	6	6	f	0	0	0	admin	\N	40	-1	1
694	6	6	f	0	0	0	Main_Page	\N	26	-1	1
694	1	1	t	0	0	0	\N	\N	155	-1	1
694	6	6	f	0	0	0	Main Page	\N	28	-1	4
694	6	6	f	0	0	0	Main Page	\N	28	-1	2
694	1	1	t	0	0	0	\N	\N	158	-1	1
694	1	1	t	0	0	0	\N	\N	159	-1	1
694	21	3	f	221	0	0	\N	\N	51	-1	1
695	6	6	f	0	0	0	d6f3a279-ce86-4a12-8985-93b71afbb71d	\N	26	-1	1
695	6	6	f	0	0	0	d6f3a279-ce86-4a12-8985-93b71afbb71d	\N	165	-1	1
696	6	6	f	0	0	0	Main_Page	\N	26	-1	1
696	21	3	f	222	0	0	\N	\N	51	-1	1
696	12	6	f	0	0	0	workspace://SpacesStore/d6f3a279-ce86-4a12-8985-93b71afbb71d	\N	167	-1	1
696	0	0	f	0	0	0	\N	\N	168	-1	1
696	3	3	f	694	0	0	\N	\N	170	-1	1
696	1	1	t	0	0	0	\N	\N	155	-1	1
696	0	0	f	0	0	0	\N	\N	171	-1	1
696	1	1	t	0	0	0	\N	\N	158	-1	1
696	1	1	t	0	0	0	\N	\N	159	-1	1
696	6	6	f	0	0	0	admin	\N	40	-1	1
696	0	0	f	0	0	0	\N	\N	27	-1	2
696	6	6	f	0	0	0	Main Page	\N	28	-1	4
696	6	6	f	0	0	0	Main Page	\N	28	-1	2
696	6	6	f	0	0	0	mjackson	\N	172	-1	1
696	7	6	f	0	0	0	2011-02-15T21:46:47.847Z	\N	173	-1	1
696	6	6	f	0	0	0	admin	\N	174	-1	1
696	7	6	f	0	0	0	2011-06-14T10:28:57.221Z	\N	175	-1	1
696	0	0	f	0	0	0	\N	\N	176	-1	1
696	0	0	f	0	0	0	\N	\N	154	-1	1
694	0	0	f	0	0	0	\N	\N	171	-1	1
694	6	6	f	0	0	0	1.15	\N	157	-1	1
696	0	0	f	0	0	0	\N	\N	157	-1	1
696	6	6	f	0	0	0	1.15	\N	169	-1	1
697	6	6	f	0	0	0	admin	\N	40	-1	1
697	6	6	f	0	0	0	Meetings	\N	26	-1	1
697	1	1	t	0	0	0	\N	\N	155	-1	1
697	6	6	f	0	0	0	Meetings	\N	28	-1	4
697	6	6	f	0	0	0	Meetings	\N	28	-1	2
697	1	1	t	0	0	0	\N	\N	158	-1	1
697	1	1	t	0	0	0	\N	\N	159	-1	1
697	21	3	f	223	0	0	\N	\N	51	-1	1
698	6	6	f	0	0	0	1373739a-2849-4647-9e97-7a4e05cc5841	\N	26	-1	1
698	6	6	f	0	0	0	1373739a-2849-4647-9e97-7a4e05cc5841	\N	165	-1	1
699	6	6	f	0	0	0	Meetings	\N	26	-1	1
699	21	3	f	224	0	0	\N	\N	51	-1	1
699	12	6	f	0	0	0	workspace://SpacesStore/1373739a-2849-4647-9e97-7a4e05cc5841	\N	167	-1	1
699	0	0	f	0	0	0	\N	\N	168	-1	1
699	3	3	f	697	0	0	\N	\N	170	-1	1
699	1	1	t	0	0	0	\N	\N	155	-1	1
699	0	0	f	0	0	0	\N	\N	171	-1	1
699	1	1	t	0	0	0	\N	\N	158	-1	1
699	1	1	t	0	0	0	\N	\N	159	-1	1
699	6	6	f	0	0	0	admin	\N	40	-1	1
699	0	0	f	0	0	0	\N	\N	27	-1	2
699	6	6	f	0	0	0	Meetings	\N	28	-1	4
699	6	6	f	0	0	0	Meetings	\N	28	-1	2
699	6	6	f	0	0	0	mjackson	\N	172	-1	1
699	7	6	f	0	0	0	2011-02-15T21:50:49.999Z	\N	173	-1	1
699	6	6	f	0	0	0	admin	\N	174	-1	1
699	7	6	f	0	0	0	2011-06-14T10:28:57.304Z	\N	175	-1	1
699	0	0	f	0	0	0	\N	\N	176	-1	1
699	0	0	f	0	0	0	\N	\N	154	-1	1
697	0	0	f	0	0	0	\N	\N	171	-1	1
697	6	6	f	0	0	0	1.2	\N	157	-1	1
699	0	0	f	0	0	0	\N	\N	157	-1	1
699	6	6	f	0	0	0	1.2	\N	169	-1	1
700	6	6	f	0	0	0	admin	\N	40	-1	1
700	6	6	f	0	0	0	Milestones	\N	26	-1	1
700	1	1	t	0	0	0	\N	\N	155	-1	1
700	6	6	f	0	0	0	Milestones	\N	28	-1	2
700	1	1	t	0	0	0	\N	\N	158	-1	1
700	1	1	t	0	0	0	\N	\N	159	-1	1
700	21	3	f	225	0	0	\N	\N	51	-1	1
701	6	6	f	0	0	0	3c73aace-9f54-420d-a1c0-c54b6a116dcf	\N	26	-1	1
701	6	6	f	0	0	0	3c73aace-9f54-420d-a1c0-c54b6a116dcf	\N	165	-1	1
702	6	6	f	0	0	0	Milestones	\N	26	-1	1
702	21	3	f	226	0	0	\N	\N	51	-1	1
702	12	6	f	0	0	0	workspace://SpacesStore/3c73aace-9f54-420d-a1c0-c54b6a116dcf	\N	167	-1	1
702	0	0	f	0	0	0	\N	\N	168	-1	1
702	3	3	f	700	0	0	\N	\N	170	-1	1
702	1	1	t	0	0	0	\N	\N	155	-1	1
702	0	0	f	0	0	0	\N	\N	171	-1	1
702	1	1	t	0	0	0	\N	\N	158	-1	1
702	1	1	t	0	0	0	\N	\N	159	-1	1
702	6	6	f	0	0	0	admin	\N	40	-1	1
702	0	0	f	0	0	0	\N	\N	27	-1	2
702	6	6	f	0	0	0	Milestones	\N	28	-1	2
702	6	6	f	0	0	0	mjackson	\N	172	-1	1
702	7	6	f	0	0	0	2011-02-15T21:59:31.855Z	\N	173	-1	1
702	6	6	f	0	0	0	admin	\N	174	-1	1
702	7	6	f	0	0	0	2011-06-14T10:28:57.370Z	\N	175	-1	1
702	0	0	f	0	0	0	\N	\N	176	-1	1
702	0	0	f	0	0	0	\N	\N	154	-1	1
700	0	0	f	0	0	0	\N	\N	171	-1	1
700	6	6	f	0	0	0	1.0	\N	157	-1	1
702	0	0	f	0	0	0	\N	\N	157	-1	1
702	6	6	f	0	0	0	1.0	\N	169	-1	1
703	6	6	f	0	0	0	discussions	\N	26	-1	1
703	6	6	f	0	0	0	admin	\N	40	-1	1
703	6	6	f	0	0	0	discussions	\N	152	-1	1
704	6	6	f	0	0	0	post-1297807546884_964	\N	26	-1	1
704	6	6	f	0	0	0	admin	\N	40	-1	1
705	6	6	f	0	0	0	admin	\N	40	-1	1
705	6	6	f	0	0	0	post-1297807546884_964	\N	26	-1	1
705	7	6	f	0	0	0	2011-02-15T22:05:46.921Z	\N	218	-1	1
705	6	6	f	0	0	0	Images for the web site	\N	28	-1	2
705	21	3	f	227	0	0	\N	\N	51	-1	1
706	6	6	f	0	0	0	admin	\N	40	-1	1
706	6	6	f	0	0	0	post-1297807619797_315	\N	26	-1	1
706	7	6	f	0	0	0	2011-02-15T22:06:59.836Z	\N	218	-1	1
706	6	6	f	0	0	0		\N	28	-1	2
706	21	3	f	228	0	0	\N	\N	51	-1	1
707	6	6	f	0	0	0	admin	\N	40	-1	1
707	6	6	f	0	0	0	post-1297807729794_112	\N	26	-1	1
707	7	6	f	0	0	0	2011-02-15T22:08:49.829Z	\N	218	-1	1
707	6	6	f	0	0	0		\N	28	-1	2
707	21	3	f	229	0	0	\N	\N	51	-1	1
708	6	6	f	0	0	0	admin	\N	40	-1	1
708	6	6	f	0	0	0	post-1297807767790_183	\N	26	-1	1
708	7	6	f	0	0	0	2011-02-15T22:09:27.840Z	\N	218	-1	1
708	6	6	f	0	0	0		\N	28	-1	2
708	21	3	f	230	0	0	\N	\N	51	-1	1
709	6	6	f	0	0	0	post-1297807581026_873	\N	26	-1	1
709	6	6	f	0	0	0	admin	\N	40	-1	1
710	6	6	f	0	0	0	admin	\N	40	-1	1
710	6	6	f	0	0	0	post-1297807581026_873	\N	26	-1	1
710	7	6	f	0	0	0	2011-02-15T22:06:21.056Z	\N	218	-1	1
710	6	6	f	0	0	0	Web Content Management Technology	\N	28	-1	2
710	21	3	f	231	0	0	\N	\N	51	-1	1
711	6	6	f	0	0	0	admin	\N	40	-1	1
711	6	6	f	0	0	0	post-1297807650635_649	\N	26	-1	1
711	7	6	f	0	0	0	2011-02-15T22:07:30.663Z	\N	218	-1	1
711	6	6	f	0	0	0		\N	28	-1	2
711	7	6	f	0	0	0	2011-02-15T22:08:02.670Z	\N	221	-1	1
711	21	3	f	232	0	0	\N	\N	51	-1	1
712	2	3	f	0	0	0	\N	\N	82	-1	1
712	1	1	f	0	0	0	\N	\N	83	-1	1
712	1	1	f	0	0	0	\N	\N	84	-1	1
712	1	1	t	0	0	0	\N	\N	86	-1	1
712	1	1	f	0	0	0	\N	\N	87	-1	1
712	6	6	f	0	0	0	surf-config	\N	26	-1	1
712	1	1	f	0	0	0	\N	\N	222	-1	1
713	6	6	f	0	0	0	pages	\N	26	-1	1
713	1	1	t	0	0	0	\N	\N	86	-1	1
713	1	1	f	0	0	0	\N	\N	87	-1	1
714	6	6	f	0	0	0	site	\N	26	-1	1
714	1	1	t	0	0	0	\N	\N	86	-1	1
714	1	1	f	0	0	0	\N	\N	87	-1	1
715	6	6	f	0	0	0	swsdp	\N	26	-1	1
715	1	1	t	0	0	0	\N	\N	86	-1	1
715	1	1	f	0	0	0	\N	\N	87	-1	1
716	6	6	f	0	0	0	dashboard.xml	\N	26	-1	1
716	1	1	t	0	0	0	\N	\N	86	-1	1
716	1	1	f	0	0	0	\N	\N	87	-1	1
716	21	3	f	233	0	0	\N	\N	51	-1	1
717	6	6	f	0	0	0	components	\N	26	-1	1
717	1	1	t	0	0	0	\N	\N	86	-1	1
717	1	1	f	0	0	0	\N	\N	87	-1	1
718	6	6	f	0	0	0	page.component-1-1.site~swsdp~dashboard.xml	\N	26	-1	1
718	1	1	t	0	0	0	\N	\N	86	-1	1
718	1	1	f	0	0	0	\N	\N	87	-1	1
718	21	3	f	234	0	0	\N	\N	51	-1	1
719	6	6	f	0	0	0	page.component-1-3.site~swsdp~dashboard.xml	\N	26	-1	1
719	1	1	t	0	0	0	\N	\N	86	-1	1
719	1	1	f	0	0	0	\N	\N	87	-1	1
719	21	3	f	235	0	0	\N	\N	51	-1	1
720	6	6	f	0	0	0	page.component-2-1.site~swsdp~dashboard.xml	\N	26	-1	1
720	1	1	t	0	0	0	\N	\N	86	-1	1
720	1	1	f	0	0	0	\N	\N	87	-1	1
720	21	3	f	236	0	0	\N	\N	51	-1	1
721	6	6	f	0	0	0	page.component-2-2.site~swsdp~dashboard.xml	\N	26	-1	1
721	1	1	t	0	0	0	\N	\N	86	-1	1
721	1	1	f	0	0	0	\N	\N	87	-1	1
721	21	3	f	237	0	0	\N	\N	51	-1	1
722	6	6	f	0	0	0	page.component-2-3.site~swsdp~dashboard.xml	\N	26	-1	1
722	1	1	t	0	0	0	\N	\N	86	-1	1
722	1	1	f	0	0	0	\N	\N	87	-1	1
722	21	3	f	238	0	0	\N	\N	51	-1	1
723	6	6	f	0	0	0	page.navigation.site~swsdp~dashboard.xml	\N	26	-1	1
723	1	1	t	0	0	0	\N	\N	86	-1	1
723	1	1	f	0	0	0	\N	\N	87	-1	1
723	21	3	f	239	0	0	\N	\N	51	-1	1
724	6	6	f	0	0	0	page.title.site~swsdp~dashboard.xml	\N	26	-1	1
724	1	1	t	0	0	0	\N	\N	86	-1	1
724	1	1	f	0	0	0	\N	\N	87	-1	1
724	21	3	f	240	0	0	\N	\N	51	-1	1
725	6	6	f	0	0	0	page.component-1-2.site~swsdp~dashboard.xml	\N	26	-1	1
725	1	1	t	0	0	0	\N	\N	86	-1	1
725	1	1	f	0	0	0	\N	\N	87	-1	1
725	21	3	f	241	0	0	\N	\N	51	-1	1
726	6	6	f	0	0	0	page.component-1-4.site~swsdp~dashboard.xml	\N	26	-1	1
726	1	1	t	0	0	0	\N	\N	86	-1	1
726	1	1	f	0	0	0	\N	\N	87	-1	1
726	21	3	f	242	0	0	\N	\N	51	-1	1
602	0	0	f	0	0	0	\N	\N	154	-1	1
648	0	0	f	0	0	0	\N	\N	154	-1	1
649	0	0	f	0	0	0	\N	\N	154	-1	1
679	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
680	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
694	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
697	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
700	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
704	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
709	9	9	f	0	0	0	\N	\\xaced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000077040000000078	154	-1	1
727	6	6	f	0	0	0	modules	\N	26	-1	1
728	6	6	f	0	0	0	bulkObjectMapper	\N	26	-1	1
728	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c000078707710000e312e302e372d534e415053484f5478	227	-1	1
728	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c000078707710000e312e302e372d534e415053484f5478	228	-1	1
729	6	6	f	0	0	0	alfresco-aos-module	\N	26	-1	1
729	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005312e342e3078	227	-1	1
729	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005312e342e3078	228	-1	1
730	6	6	f	0	0	0	org.alfresco.integrations.google.docs	\N	26	-1	1
730	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005332e322e3178	227	-1	1
730	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005332e322e3178	228	-1	1
731	6	6	f	0	0	0	org_alfresco_device_sync_repo	\N	26	-1	1
731	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005332e342e3078	227	-1	1
731	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005332e342e3078	228	-1	1
732	6	6	f	0	0	0	large_txn_generator	\N	26	-1	1
732	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c00007870770e000c302e312d534e415053484f5478	227	-1	1
732	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c00007870770e000c302e312d534e415053484f5478	228	-1	1
733	6	6	f	0	0	0	alfresco-share-services	\N	26	-1	1
733	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005372e302e3078	227	-1	1
733	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077070005372e302e3078	228	-1	1
734	6	6	f	0	0	0	alfresco-trashcan-cleaner	\N	26	-1	1
734	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077050003322e3378	227	-1	1
734	9	9	f	0	0	0	\N	\\xaced00057372002c6f72672e616c66726573636f2e7265706f2e6d6f64756c652e4d6f64756c6556657273696f6e4e756d62657277473de5d9c7c28c0c0000787077050003322e3378	228	-1	1
7	21	3	f	244	0	0	\N	\N	229	-1	1
735	6	6	f	0	0	0	imageRenderingEngine	\N	70	-1	1
735	0	0	f	0	0	0	\N	\N	98	-1	1
735	0	0	f	0	0	0	\N	\N	99	-1	1
735	1	1	f	0	0	0	\N	\N	68	-1	1
735	0	0	f	0	0	0	\N	\N	100	-1	1
735	0	0	f	0	0	0	\N	\N	69	-1	1
735	6	6	f	0	0	0	New	\N	101	-1	1
735	0	0	f	0	0	0	\N	\N	71	-1	1
736	3	3	f	-1	0	0	\N	\N	77	-1	1
736	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
737	6	6	f	0	0	0	System	\N	77	-1	1
737	6	6	f	0	0	0	runAs	\N	78	-1	1
738	1	1	t	0	0	0	\N	\N	77	-1	1
738	6	6	f	0	0	0	autoOrientation	\N	78	-1	1
739	6	6	f	0	0	0	image/jpeg	\N	77	-1	1
739	6	6	f	0	0	0	mime-type	\N	78	-1	1
740	2	3	f	960	0	0	\N	\N	77	-1	1
740	6	6	f	0	0	0	ysize	\N	78	-1	1
741	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
741	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
742	6	6	f	0	0	0	imgpreview	\N	77	-1	1
742	6	6	f	0	0	0	use	\N	78	-1	1
743	3	3	f	-1	0	0	\N	\N	77	-1	1
743	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
744	2	3	f	960	0	0	\N	\N	77	-1	1
744	6	6	f	0	0	0	xsize	\N	78	-1	1
745	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}imgpreview	\N	77	-1	1
745	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
746	6	6	f	0	0	0	alfresco/thumbnail/thumbnail_placeholder_256.png	\N	77	-1	1
746	6	6	f	0	0	0	placeHolderResourcePath	\N	78	-1	1
747	2	3	f	1	0	0	\N	\N	77	-1	1
747	6	6	f	0	0	0	pageLimit	\N	78	-1	1
748	3	3	f	-1	0	0	\N	\N	77	-1	1
748	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
749	2	3	f	-1	0	0	\N	\N	77	-1	1
749	6	6	f	0	0	0	maxPages	\N	78	-1	1
750	1	1	f	0	0	0	\N	\N	77	-1	1
750	6	6	f	0	0	0	isAbsolute	\N	78	-1	1
751	1	1	t	0	0	0	\N	\N	77	-1	1
751	6	6	f	0	0	0	resizeToThumbnail	\N	78	-1	1
752	6	6	f	0	0	0		\N	77	-1	1
752	6	6	f	0	0	0	commandOptions	\N	78	-1	1
753	1	1	f	0	0	0	\N	\N	77	-1	1
753	6	6	f	0	0	0	allowEnlargement	\N	78	-1	1
754	1	1	t	0	0	0	\N	\N	77	-1	1
754	6	6	f	0	0	0	maintainAspectRatio	\N	78	-1	1
755	3	3	f	-1	0	0	\N	\N	77	-1	1
755	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
756	6	6	f	0	0	0	imageRenderingEngine	\N	70	-1	1
756	0	0	f	0	0	0	\N	\N	98	-1	1
756	0	0	f	0	0	0	\N	\N	99	-1	1
756	1	1	f	0	0	0	\N	\N	68	-1	1
756	0	0	f	0	0	0	\N	\N	100	-1	1
756	0	0	f	0	0	0	\N	\N	69	-1	1
756	6	6	f	0	0	0	New	\N	101	-1	1
756	0	0	f	0	0	0	\N	\N	71	-1	1
757	3	3	f	-1	0	0	\N	\N	77	-1	1
757	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
758	6	6	f	0	0	0	System	\N	77	-1	1
758	6	6	f	0	0	0	runAs	\N	78	-1	1
759	1	1	t	0	0	0	\N	\N	77	-1	1
759	6	6	f	0	0	0	autoOrientation	\N	78	-1	1
760	6	6	f	0	0	0	image/png	\N	77	-1	1
760	6	6	f	0	0	0	mime-type	\N	78	-1	1
761	2	3	f	100	0	0	\N	\N	77	-1	1
761	6	6	f	0	0	0	ysize	\N	78	-1	1
762	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
762	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
763	6	6	f	0	0	0	doclib	\N	77	-1	1
763	6	6	f	0	0	0	use	\N	78	-1	1
764	3	3	f	-1	0	0	\N	\N	77	-1	1
764	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
765	2	3	f	100	0	0	\N	\N	77	-1	1
765	6	6	f	0	0	0	xsize	\N	78	-1	1
766	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}doclib	\N	77	-1	1
766	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
767	6	6	f	0	0	0	alfresco/thumbnail/thumbnail_placeholder_doclib.png	\N	77	-1	1
767	6	6	f	0	0	0	placeHolderResourcePath	\N	78	-1	1
768	2	3	f	1	0	0	\N	\N	77	-1	1
768	6	6	f	0	0	0	pageLimit	\N	78	-1	1
769	3	3	f	-1	0	0	\N	\N	77	-1	1
769	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
770	2	3	f	-1	0	0	\N	\N	77	-1	1
770	6	6	f	0	0	0	maxPages	\N	78	-1	1
771	1	1	f	0	0	0	\N	\N	77	-1	1
771	6	6	f	0	0	0	isAbsolute	\N	78	-1	1
772	1	1	t	0	0	0	\N	\N	77	-1	1
772	6	6	f	0	0	0	resizeToThumbnail	\N	78	-1	1
773	6	6	f	0	0	0		\N	77	-1	1
773	6	6	f	0	0	0	commandOptions	\N	78	-1	1
774	1	1	f	0	0	0	\N	\N	77	-1	1
774	6	6	f	0	0	0	allowEnlargement	\N	78	-1	1
775	1	1	t	0	0	0	\N	\N	77	-1	1
775	6	6	f	0	0	0	maintainAspectRatio	\N	78	-1	1
776	3	3	f	-1	0	0	\N	\N	77	-1	1
776	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
777	6	6	f	0	0	0	reformat	\N	70	-1	1
777	0	0	f	0	0	0	\N	\N	98	-1	1
777	0	0	f	0	0	0	\N	\N	99	-1	1
777	1	1	f	0	0	0	\N	\N	68	-1	1
777	0	0	f	0	0	0	\N	\N	100	-1	1
777	0	0	f	0	0	0	\N	\N	69	-1	1
777	6	6	f	0	0	0	New	\N	101	-1	1
777	0	0	f	0	0	0	\N	\N	71	-1	1
778	3	3	f	-1	0	0	\N	\N	77	-1	1
778	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
779	2	3	f	-1	0	0	\N	\N	77	-1	1
779	6	6	f	0	0	0	pageLimit	\N	78	-1	1
780	6	6	f	0	0	0	application/pdf	\N	77	-1	1
780	6	6	f	0	0	0	mime-type	\N	78	-1	1
781	6	6	f	0	0	0	9	\N	77	-1	1
781	6	6	f	0	0	0	flashVersion	\N	78	-1	1
782	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
782	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
783	3	3	f	-1	0	0	\N	\N	77	-1	1
783	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
784	6	6	f	0	0	0	pdf	\N	77	-1	1
784	6	6	f	0	0	0	use	\N	78	-1	1
785	2	3	f	-1	0	0	\N	\N	77	-1	1
785	6	6	f	0	0	0	maxPages	\N	78	-1	1
786	3	3	f	-1	0	0	\N	\N	77	-1	1
786	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
787	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}pdf	\N	77	-1	1
787	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
788	3	3	f	-1	0	0	\N	\N	77	-1	1
788	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
789	6	6	f	0	0	0	imageRenderingEngine	\N	70	-1	1
789	0	0	f	0	0	0	\N	\N	98	-1	1
789	0	0	f	0	0	0	\N	\N	99	-1	1
789	1	1	f	0	0	0	\N	\N	68	-1	1
789	0	0	f	0	0	0	\N	\N	100	-1	1
789	0	0	f	0	0	0	\N	\N	69	-1	1
789	6	6	f	0	0	0	New	\N	101	-1	1
789	0	0	f	0	0	0	\N	\N	71	-1	1
790	3	3	f	-1	0	0	\N	\N	77	-1	1
790	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
791	6	6	f	0	0	0	System	\N	77	-1	1
791	6	6	f	0	0	0	runAs	\N	78	-1	1
792	1	1	t	0	0	0	\N	\N	77	-1	1
792	6	6	f	0	0	0	autoOrientation	\N	78	-1	1
793	6	6	f	0	0	0	image/jpeg	\N	77	-1	1
793	6	6	f	0	0	0	mime-type	\N	78	-1	1
794	2	3	f	100	0	0	\N	\N	77	-1	1
794	6	6	f	0	0	0	ysize	\N	78	-1	1
795	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
795	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
796	6	6	f	0	0	0	medium	\N	77	-1	1
796	6	6	f	0	0	0	use	\N	78	-1	1
797	3	3	f	-1	0	0	\N	\N	77	-1	1
797	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
798	2	3	f	100	0	0	\N	\N	77	-1	1
798	6	6	f	0	0	0	xsize	\N	78	-1	1
799	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}medium	\N	77	-1	1
799	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
800	6	6	f	0	0	0	alfresco/thumbnail/thumbnail_placeholder_medium.jpg	\N	77	-1	1
800	6	6	f	0	0	0	placeHolderResourcePath	\N	78	-1	1
801	2	3	f	1	0	0	\N	\N	77	-1	1
801	6	6	f	0	0	0	pageLimit	\N	78	-1	1
802	3	3	f	-1	0	0	\N	\N	77	-1	1
802	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
803	2	3	f	-1	0	0	\N	\N	77	-1	1
803	6	6	f	0	0	0	maxPages	\N	78	-1	1
804	1	1	f	0	0	0	\N	\N	77	-1	1
804	6	6	f	0	0	0	isAbsolute	\N	78	-1	1
805	1	1	t	0	0	0	\N	\N	77	-1	1
805	6	6	f	0	0	0	resizeToThumbnail	\N	78	-1	1
806	6	6	f	0	0	0		\N	77	-1	1
806	6	6	f	0	0	0	commandOptions	\N	78	-1	1
807	1	1	t	0	0	0	\N	\N	77	-1	1
807	6	6	f	0	0	0	allowEnlargement	\N	78	-1	1
808	1	1	t	0	0	0	\N	\N	77	-1	1
808	6	6	f	0	0	0	maintainAspectRatio	\N	78	-1	1
809	3	3	f	-1	0	0	\N	\N	77	-1	1
809	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
810	6	6	f	0	0	0	imageRenderingEngine	\N	70	-1	1
810	0	0	f	0	0	0	\N	\N	98	-1	1
810	0	0	f	0	0	0	\N	\N	99	-1	1
810	1	1	f	0	0	0	\N	\N	68	-1	1
810	0	0	f	0	0	0	\N	\N	100	-1	1
810	0	0	f	0	0	0	\N	\N	69	-1	1
810	6	6	f	0	0	0	New	\N	101	-1	1
810	0	0	f	0	0	0	\N	\N	71	-1	1
811	3	3	f	-1	0	0	\N	\N	77	-1	1
811	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
812	6	6	f	0	0	0	System	\N	77	-1	1
812	6	6	f	0	0	0	runAs	\N	78	-1	1
813	1	1	t	0	0	0	\N	\N	77	-1	1
813	6	6	f	0	0	0	autoOrientation	\N	78	-1	1
814	6	6	f	0	0	0	image/png	\N	77	-1	1
814	6	6	f	0	0	0	mime-type	\N	78	-1	1
815	2	3	f	64	0	0	\N	\N	77	-1	1
815	6	6	f	0	0	0	ysize	\N	78	-1	1
816	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
816	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
817	6	6	f	0	0	0	avatar	\N	77	-1	1
817	6	6	f	0	0	0	use	\N	78	-1	1
818	3	3	f	-1	0	0	\N	\N	77	-1	1
818	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
819	2	3	f	64	0	0	\N	\N	77	-1	1
819	6	6	f	0	0	0	xsize	\N	78	-1	1
820	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}avatar	\N	77	-1	1
820	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
821	6	6	f	0	0	0	alfresco/thumbnail/thumbnail_placeholder_avatar.png	\N	77	-1	1
821	6	6	f	0	0	0	placeHolderResourcePath	\N	78	-1	1
822	2	3	f	1	0	0	\N	\N	77	-1	1
822	6	6	f	0	0	0	pageLimit	\N	78	-1	1
823	3	3	f	-1	0	0	\N	\N	77	-1	1
823	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
824	2	3	f	-1	0	0	\N	\N	77	-1	1
824	6	6	f	0	0	0	maxPages	\N	78	-1	1
825	1	1	f	0	0	0	\N	\N	77	-1	1
825	6	6	f	0	0	0	isAbsolute	\N	78	-1	1
826	1	1	t	0	0	0	\N	\N	77	-1	1
826	6	6	f	0	0	0	resizeToThumbnail	\N	78	-1	1
827	6	6	f	0	0	0		\N	77	-1	1
827	6	6	f	0	0	0	commandOptions	\N	78	-1	1
828	1	1	f	0	0	0	\N	\N	77	-1	1
828	6	6	f	0	0	0	allowEnlargement	\N	78	-1	1
829	1	1	t	0	0	0	\N	\N	77	-1	1
829	6	6	f	0	0	0	maintainAspectRatio	\N	78	-1	1
830	3	3	f	-1	0	0	\N	\N	77	-1	1
830	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
831	6	6	f	0	0	0	reformat	\N	70	-1	1
831	0	0	f	0	0	0	\N	\N	98	-1	1
831	0	0	f	0	0	0	\N	\N	99	-1	1
831	1	1	f	0	0	0	\N	\N	68	-1	1
831	0	0	f	0	0	0	\N	\N	100	-1	1
831	0	0	f	0	0	0	\N	\N	69	-1	1
831	6	6	f	0	0	0	New	\N	101	-1	1
831	0	0	f	0	0	0	\N	\N	71	-1	1
832	3	3	f	-1	0	0	\N	\N	77	-1	1
832	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
833	6	6	f	0	0	0	System	\N	77	-1	1
833	6	6	f	0	0	0	runAs	\N	78	-1	1
834	6	6	f	0	0	0	application/x-shockwave-flash	\N	77	-1	1
834	6	6	f	0	0	0	mime-type	\N	78	-1	1
835	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
835	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
836	6	6	f	0	0	0	webpreview	\N	77	-1	1
836	6	6	f	0	0	0	use	\N	78	-1	1
837	3	3	f	-1	0	0	\N	\N	77	-1	1
837	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
838	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}webpreview	\N	77	-1	1
838	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
839	2	3	f	-1	0	0	\N	\N	77	-1	1
839	6	6	f	0	0	0	pageLimit	\N	78	-1	1
840	6	6	f	0	0	0	9	\N	77	-1	1
840	6	6	f	0	0	0	flashVersion	\N	78	-1	1
841	3	3	f	-1	0	0	\N	\N	77	-1	1
841	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
842	2	3	f	-1	0	0	\N	\N	77	-1	1
842	6	6	f	0	0	0	maxPages	\N	78	-1	1
843	3	3	f	-1	0	0	\N	\N	77	-1	1
843	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
844	6	6	f	0	0	0	imageRenderingEngine	\N	70	-1	1
844	0	0	f	0	0	0	\N	\N	98	-1	1
844	0	0	f	0	0	0	\N	\N	99	-1	1
844	1	1	f	0	0	0	\N	\N	68	-1	1
844	0	0	f	0	0	0	\N	\N	100	-1	1
844	0	0	f	0	0	0	\N	\N	69	-1	1
844	6	6	f	0	0	0	New	\N	101	-1	1
844	0	0	f	0	0	0	\N	\N	71	-1	1
845	3	3	f	-1	0	0	\N	\N	77	-1	1
845	6	6	f	0	0	0	maxSourceSizeKBytes	\N	78	-1	1
846	6	6	f	0	0	0	System	\N	77	-1	1
846	6	6	f	0	0	0	runAs	\N	78	-1	1
847	1	1	t	0	0	0	\N	\N	77	-1	1
847	6	6	f	0	0	0	autoOrientation	\N	78	-1	1
848	6	6	f	0	0	0	image/png	\N	77	-1	1
848	6	6	f	0	0	0	mime-type	\N	78	-1	1
849	2	3	f	32	0	0	\N	\N	77	-1	1
849	6	6	f	0	0	0	ysize	\N	78	-1	1
850	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}thumbnail	\N	77	-1	1
850	6	6	f	0	0	0	rendition-nodetype	\N	78	-1	1
851	6	6	f	0	0	0	avatar32	\N	77	-1	1
851	6	6	f	0	0	0	use	\N	78	-1	1
852	3	3	f	-1	0	0	\N	\N	77	-1	1
852	6	6	f	0	0	0	readLimitKBytes	\N	78	-1	1
853	2	3	f	32	0	0	\N	\N	77	-1	1
853	6	6	f	0	0	0	xsize	\N	78	-1	1
854	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}avatar32	\N	77	-1	1
854	6	6	f	0	0	0	renderingActionName	\N	78	-1	1
855	6	6	f	0	0	0	alfresco/thumbnail/thumbnail_placeholder_avatar32.png	\N	77	-1	1
855	6	6	f	0	0	0	placeHolderResourcePath	\N	78	-1	1
856	2	3	f	1	0	0	\N	\N	77	-1	1
856	6	6	f	0	0	0	pageLimit	\N	78	-1	1
857	3	3	f	-1	0	0	\N	\N	77	-1	1
857	6	6	f	0	0	0	timeoutMs	\N	78	-1	1
858	2	3	f	-1	0	0	\N	\N	77	-1	1
858	6	6	f	0	0	0	maxPages	\N	78	-1	1
859	1	1	f	0	0	0	\N	\N	77	-1	1
859	6	6	f	0	0	0	isAbsolute	\N	78	-1	1
860	1	1	t	0	0	0	\N	\N	77	-1	1
860	6	6	f	0	0	0	resizeToThumbnail	\N	78	-1	1
861	6	6	f	0	0	0		\N	77	-1	1
861	6	6	f	0	0	0	commandOptions	\N	78	-1	1
862	1	1	f	0	0	0	\N	\N	77	-1	1
862	6	6	f	0	0	0	allowEnlargement	\N	78	-1	1
863	1	1	t	0	0	0	\N	\N	77	-1	1
863	6	6	f	0	0	0	maintainAspectRatio	\N	78	-1	1
864	3	3	f	-1	0	0	\N	\N	77	-1	1
864	6	6	f	0	0	0	readLimitTimeMs	\N	78	-1	1
865	1	1	t	0	0	0	\N	\N	86	-1	1
865	1	1	f	0	0	0	\N	\N	87	-1	1
865	6	6	f	0	0	0	Solr Facets Space	\N	26	-1	1
865	6	6	f	0	0	0	Root folder for the Solr Facet properties\n         	\N	27	-1	2
865	6	6	f	0	0	0	Solr Facets Space	\N	28	-1	2
865	6	6	f	0	0	0	filter_mimetype	\N	231	1	1
865	6	6	f	0	0	0	filter_created	\N	231	2	1
865	6	6	f	0	0	0	filter_content_size	\N	231	3	1
865	6	6	f	0	0	0	filter_modifier	\N	231	4	1
865	6	6	f	0	0	0	filter_modified	\N	231	5	1
865	6	6	f	0	0	0	filter_creator	\N	231	0	1
866	6	6	f	0	0	0	components	\N	26	-1	1
866	1	1	f	0	0	0	\N	\N	86	-1	1
866	1	1	f	0	0	0	\N	\N	87	-1	1
866	6	6	f	0	0	0	admin	\N	40	-1	1
867	6	6	f	0	0	0	page.title.user~admin~dashboard.xml	\N	26	-1	1
867	6	6	f	0	0	0	admin	\N	40	-1	1
867	1	1	t	0	0	0	\N	\N	86	-1	1
867	1	1	f	0	0	0	\N	\N	87	-1	1
868	6	6	f	0	0	0	page.full-width-dashlet.user~admin~dashboard.xml	\N	26	-1	1
868	6	6	f	0	0	0	admin	\N	40	-1	1
868	1	1	t	0	0	0	\N	\N	86	-1	1
868	1	1	f	0	0	0	\N	\N	87	-1	1
869	6	6	f	0	0	0	page.component-1-1.user~admin~dashboard.xml	\N	26	-1	1
869	6	6	f	0	0	0	admin	\N	40	-1	1
869	1	1	t	0	0	0	\N	\N	86	-1	1
869	1	1	f	0	0	0	\N	\N	87	-1	1
870	6	6	f	0	0	0	page.component-1-2.user~admin~dashboard.xml	\N	26	-1	1
870	6	6	f	0	0	0	admin	\N	40	-1	1
870	1	1	t	0	0	0	\N	\N	86	-1	1
870	1	1	f	0	0	0	\N	\N	87	-1	1
871	6	6	f	0	0	0	page.component-2-1.user~admin~dashboard.xml	\N	26	-1	1
871	6	6	f	0	0	0	admin	\N	40	-1	1
871	1	1	t	0	0	0	\N	\N	86	-1	1
871	1	1	f	0	0	0	\N	\N	87	-1	1
872	6	6	f	0	0	0	page.component-2-2.user~admin~dashboard.xml	\N	26	-1	1
872	6	6	f	0	0	0	admin	\N	40	-1	1
872	1	1	t	0	0	0	\N	\N	86	-1	1
872	1	1	f	0	0	0	\N	\N	87	-1	1
873	6	6	f	0	0	0	pages	\N	26	-1	1
873	1	1	f	0	0	0	\N	\N	86	-1	1
873	1	1	f	0	0	0	\N	\N	87	-1	1
874	6	6	f	0	0	0	user	\N	26	-1	1
874	1	1	f	0	0	0	\N	\N	86	-1	1
874	1	1	f	0	0	0	\N	\N	87	-1	1
875	6	6	f	0	0	0	admin	\N	26	-1	1
875	1	1	f	0	0	0	\N	\N	86	-1	1
875	1	1	f	0	0	0	\N	\N	87	-1	1
873	6	6	f	0	0	0	admin	\N	40	-1	1
874	6	6	f	0	0	0	admin	\N	40	-1	1
875	6	6	f	0	0	0	admin	\N	40	-1	1
876	6	6	f	0	0	0	dashboard.xml	\N	26	-1	1
876	6	6	f	0	0	0	admin	\N	40	-1	1
876	1	1	t	0	0	0	\N	\N	86	-1	1
876	1	1	f	0	0	0	\N	\N	87	-1	1
867	21	3	f	252	0	0	\N	\N	51	-1	1
868	21	3	f	253	0	0	\N	\N	51	-1	1
869	21	3	f	254	0	0	\N	\N	51	-1	1
870	21	3	f	255	0	0	\N	\N	51	-1	1
871	21	3	f	256	0	0	\N	\N	51	-1	1
872	21	3	f	257	0	0	\N	\N	51	-1	1
876	21	3	f	258	0	0	\N	\N	51	-1	1
877	6	6	f	0	0	0	uploaded	\N	26	-1	1
877	21	3	f	260	0	0	\N	\N	51	-1	1
877	1	1	t	0	0	0	\N	\N	52	-1	1
877	6	6	f	0	0	0		\N	27	-1	3
877	6	6	f	0	0	0	uploaded title	\N	28	-1	3
881	1	1	t	0	0	0	\N	\N	86	-1	1
881	1	1	f	0	0	0	\N	\N	87	-1	1
894	6	6	f	0	0	0	music-model.xml	\N	26	-1	1
879	1	1	t	0	0	0	\N	\N	86	-1	1
879	1	1	f	0	0	0	\N	\N	87	-1	1
879	6	6	f	0	0	0	pdf	\N	26	-1	1
879	21	3	f	262	0	0	\N	\N	51	-1	1
879	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
880	3	3	f	878	0	0	\N	\N	149	-1	1
879	6	6	f	0	0	0	pdf	\N	144	-1	1
877	6	6	f	0	0	0	pdf:1619008847587	\N	138	0	1
877	6	6	f	0	0	0	doclib:1619008851958	\N	138	1	1
881	6	6	f	0	0	0	doclib	\N	26	-1	1
881	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
881	6	6	f	0	0	0	doclib	\N	144	-1	1
881	2	3	f	365710032	0	0	\N	\N	233	-1	1
881	21	3	f	263	0	0	\N	\N	51	-1	1
885	3	3	f	883	0	0	\N	\N	149	-1	1
887	21	3	f	269	0	0	\N	\N	51	-1	1
887	1	1	t	0	0	0	\N	\N	52	-1	1
887	6	6	f	0	0	0	admin	\N	40	-1	1
887	6	6	f	0	0	0	deleted	\N	26	-1	1
887	7	6	f	0	0	0	2021-04-21T12:41:17.419Z	\N	235	-1	1
887	6	6	f	0	0	0	pdf:1619008865370	\N	138	0	1
887	6	6	f	0	0	0		\N	27	-1	3
887	6	6	f	0	0	0	deleted title	\N	28	-1	3
887	6	6	f	0	0	0	doclib:1619008867706	\N	138	1	1
887	13	6	f	0	0	0	workspace://SpacesStore/952b5f79-98b7-4ed7-aa61-81f6ddfb2151|workspace://SpacesStore/2d1fae15-a7be-422b-a470-e4de52bc41b3|{http://www.alfresco.org/model/content/1.0}contains|{http://www.alfresco.org/model/content/1.0}deleted|true|-1	\N	236	-1	1
887	6	6	f	0	0	0		\N	237	-1	1
887	6	6	f	0	0	0	admin	\N	238	-1	1
888	6	6	f	0	0	0	admin	\N	26	-1	1
889	21	3	f	270	0	0	\N	\N	51	-1	1
889	1	1	t	0	0	0	\N	\N	86	-1	1
889	1	1	f	0	0	0	\N	\N	87	-1	1
889	6	6	f	0	0	0	pdf	\N	26	-1	1
889	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
889	6	6	f	0	0	0	pdf	\N	144	-1	1
890	21	3	f	271	0	0	\N	\N	51	-1	1
890	1	1	t	0	0	0	\N	\N	86	-1	1
890	1	1	f	0	0	0	\N	\N	87	-1	1
890	2	3	f	-1219521921	0	0	\N	\N	233	-1	1
890	6	6	f	0	0	0	doclib	\N	26	-1	1
890	15	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content	\N	143	-1	1
890	6	6	f	0	0	0	doclib	\N	144	-1	1
891	3	3	f	884	0	0	\N	\N	149	-1	1
892	3	3	f	886	0	0	\N	\N	149	-1	1
893	3	3	f	882	0	0	\N	\N	149	-1	1
897	6	6	f	0	0	0	finance-model.xml	\N	26	-1	1
897	1	1	t	0	0	0	\N	\N	244	-1	1
897	21	3	f	274	0	0	\N	\N	51	-1	1
897	1	1	f	0	0	0	\N	\N	155	-1	1
897	1	1	t	0	0	0	\N	\N	158	-1	1
897	1	1	t	0	0	0	\N	\N	159	-1	1
898	6	6	f	0	0	0	c38fe89b-3ed3-4d26-84ea-be53bbcd2408	\N	26	-1	1
898	6	6	f	0	0	0	c38fe89b-3ed3-4d26-84ea-be53bbcd2408	\N	165	-1	1
899	21	3	f	275	0	0	\N	\N	51	-1	1
899	1	1	t	0	0	0	\N	\N	244	-1	1
899	0	0	f	0	0	0	\N	\N	245	-1	1
899	0	0	f	0	0	0	\N	\N	246	-1	1
899	0	0	f	0	0	0	\N	\N	247	-1	1
899	0	0	f	0	0	0	\N	\N	248	-1	1
899	0	0	f	0	0	0	\N	\N	249	-1	1
899	6	6	f	0	0	0	finance-model.xml	\N	26	-1	1
894	1	1	t	0	0	0	\N	\N	244	-1	1
894	21	3	f	272	0	0	\N	\N	51	-1	1
894	1	1	f	0	0	0	\N	\N	155	-1	1
894	1	1	t	0	0	0	\N	\N	158	-1	1
894	1	1	t	0	0	0	\N	\N	159	-1	1
895	6	6	f	0	0	0	c8378689-c06d-4b71-9d73-63ce4192344e	\N	26	-1	1
895	6	6	f	0	0	0	c8378689-c06d-4b71-9d73-63ce4192344e	\N	165	-1	1
896	21	3	f	273	0	0	\N	\N	51	-1	1
896	1	1	t	0	0	0	\N	\N	244	-1	1
896	0	0	f	0	0	0	\N	\N	245	-1	1
896	0	0	f	0	0	0	\N	\N	246	-1	1
896	0	0	f	0	0	0	\N	\N	247	-1	1
896	0	0	f	0	0	0	\N	\N	248	-1	1
896	0	0	f	0	0	0	\N	\N	249	-1	1
896	6	6	f	0	0	0	music-model.xml	\N	26	-1	1
896	12	6	f	0	0	0	workspace://SpacesStore/c8378689-c06d-4b71-9d73-63ce4192344e	\N	167	-1	1
896	6	6	f	0	0	0	Initial Version	\N	168	-1	1
896	6	6	f	0	0	0	1.0	\N	169	-1	1
896	3	3	f	894	0	0	\N	\N	170	-1	1
896	9	9	f	0	0	0	\N	\\xaced00057e72002c6f72672e616c66726573636f2e736572766963652e636d722e76657273696f6e2e56657273696f6e5479706500000000000000001200007872000e6a6176612e6c616e672e456e756d000000000000000012000078707400054d414a4f52	250	-1	1
896	1	1	f	0	0	0	\N	\N	155	-1	1
896	0	0	f	0	0	0	\N	\N	171	-1	1
896	0	0	f	0	0	0	\N	\N	157	-1	1
896	1	1	t	0	0	0	\N	\N	158	-1	1
896	1	1	t	0	0	0	\N	\N	159	-1	1
896	6	6	f	0	0	0	admin	\N	172	-1	1
896	7	6	f	0	0	0	2021-04-21T12:41:53.481Z	\N	173	-1	1
896	6	6	f	0	0	0	admin	\N	174	-1	1
896	7	6	f	0	0	0	2021-04-21T12:41:53.481Z	\N	175	-1	1
896	0	0	f	0	0	0	\N	\N	176	-1	1
894	6	6	f	0	0	0	1.0	\N	157	-1	1
894	6	6	f	0	0	0	MAJOR	\N	171	-1	1
894	15	6	f	0	0	0	{music}music	\N	245	-1	1
894	6	6	f	0	0	0	Administrator	\N	246	-1	1
894	0	0	f	0	0	0	\N	\N	247	-1	1
894	0	0	f	0	0	0	\N	\N	248	-1	1
894	0	0	f	0	0	0	\N	\N	249	-1	1
900	6	6	f	0	0	0	sharding-content-model.xml	\N	26	-1	1
900	1	1	t	0	0	0	\N	\N	244	-1	1
900	21	3	f	276	0	0	\N	\N	51	-1	1
900	1	1	f	0	0	0	\N	\N	155	-1	1
900	1	1	t	0	0	0	\N	\N	158	-1	1
900	1	1	t	0	0	0	\N	\N	159	-1	1
901	6	6	f	0	0	0	a77289bc-0602-4a94-9bcf-ac2c2aa02c87	\N	26	-1	1
901	6	6	f	0	0	0	a77289bc-0602-4a94-9bcf-ac2c2aa02c87	\N	165	-1	1
902	21	3	f	277	0	0	\N	\N	51	-1	1
902	1	1	t	0	0	0	\N	\N	244	-1	1
902	0	0	f	0	0	0	\N	\N	245	-1	1
902	0	0	f	0	0	0	\N	\N	246	-1	1
902	0	0	f	0	0	0	\N	\N	247	-1	1
902	0	0	f	0	0	0	\N	\N	248	-1	1
902	0	0	f	0	0	0	\N	\N	249	-1	1
902	6	6	f	0	0	0	sharding-content-model.xml	\N	26	-1	1
902	12	6	f	0	0	0	workspace://SpacesStore/a77289bc-0602-4a94-9bcf-ac2c2aa02c87	\N	167	-1	1
902	6	6	f	0	0	0	Initial Version	\N	168	-1	1
902	6	6	f	0	0	0	1.0	\N	169	-1	1
902	3	3	f	900	0	0	\N	\N	170	-1	1
902	9	9	f	0	0	0	\N	\\xaced00057e72002c6f72672e616c66726573636f2e736572766963652e636d722e76657273696f6e2e56657273696f6e5479706500000000000000001200007872000e6a6176612e6c616e672e456e756d000000000000000012000078707400054d414a4f52	250	-1	1
902	1	1	f	0	0	0	\N	\N	155	-1	1
902	0	0	f	0	0	0	\N	\N	171	-1	1
902	0	0	f	0	0	0	\N	\N	157	-1	1
902	1	1	t	0	0	0	\N	\N	158	-1	1
902	1	1	t	0	0	0	\N	\N	159	-1	1
902	6	6	f	0	0	0	admin	\N	172	-1	1
902	7	6	f	0	0	0	2021-04-21T12:41:54.911Z	\N	173	-1	1
902	6	6	f	0	0	0	admin	\N	174	-1	1
899	12	6	f	0	0	0	workspace://SpacesStore/c38fe89b-3ed3-4d26-84ea-be53bbcd2408	\N	167	-1	1
899	6	6	f	0	0	0	Initial Version	\N	168	-1	1
899	6	6	f	0	0	0	1.0	\N	169	-1	1
899	3	3	f	897	0	0	\N	\N	170	-1	1
899	9	9	f	0	0	0	\N	\\xaced00057e72002c6f72672e616c66726573636f2e736572766963652e636d722e76657273696f6e2e56657273696f6e5479706500000000000000001200007872000e6a6176612e6c616e672e456e756d000000000000000012000078707400054d414a4f52	250	-1	1
899	1	1	f	0	0	0	\N	\N	155	-1	1
899	0	0	f	0	0	0	\N	\N	171	-1	1
899	0	0	f	0	0	0	\N	\N	157	-1	1
899	1	1	t	0	0	0	\N	\N	158	-1	1
899	1	1	t	0	0	0	\N	\N	159	-1	1
899	6	6	f	0	0	0	admin	\N	172	-1	1
899	7	6	f	0	0	0	2021-04-21T12:41:54.338Z	\N	173	-1	1
899	6	6	f	0	0	0	admin	\N	174	-1	1
899	7	6	f	0	0	0	2021-04-21T12:41:54.338Z	\N	175	-1	1
899	0	0	f	0	0	0	\N	\N	176	-1	1
897	6	6	f	0	0	0	1.0	\N	157	-1	1
897	6	6	f	0	0	0	MAJOR	\N	171	-1	1
897	15	6	f	0	0	0	{Finance}Finance	\N	245	-1	1
897	6	6	f	0	0	0	Administrator	\N	246	-1	1
897	0	0	f	0	0	0	\N	\N	247	-1	1
897	0	0	f	0	0	0	\N	\N	248	-1	1
897	0	0	f	0	0	0	\N	\N	249	-1	1
902	7	6	f	0	0	0	2021-04-21T12:41:54.911Z	\N	175	-1	1
902	0	0	f	0	0	0	\N	\N	176	-1	1
900	6	6	f	0	0	0	1.0	\N	157	-1	1
900	6	6	f	0	0	0	MAJOR	\N	171	-1	1
900	15	6	f	0	0	0	{http://www.alfresco.org/model/sharding/1.0}contentModel	\N	245	-1	1
900	0	0	f	0	0	0	\N	\N	246	-1	1
900	0	0	f	0	0	0	\N	\N	247	-1	1
900	6	6	f	0	0	0	Explicit Routing for Sharding Sample Model	\N	248	-1	1
900	0	0	f	0	0	0	\N	\N	249	-1	1
903	3	3	f	0	0	0	\N	\N	36	-1	1
903	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	37	-1	1
903	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO@tas-automation.org	\N	38	-1	1
903	6	6	f	0	0	0	LN-UserSearch-zGiNtcNRnaJPlLO	\N	41	-1	1
903	6	6	f	0	0	0	FN-UserSearch-zGiNtcNRnaJPlLO	\N	43	-1	1
903	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
903	6	6	f	0	0	0	userHomesHomeFolderProvider	\N	39	-1	1
904	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	26	-1	1
903	12	6	f	0	0	0	workspace://SpacesStore/4cd80404-5c4c-4fa4-ad63-38eb1d831cba	\N	45	-1	1
904	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
905	6	6	f	0	0	0	bcbe379a-70af-4cf6-bd64-82164567db93	\N	113	-1	1
905	1	1	t	0	0	0	\N	\N	6	-1	1
905	1	1	f	0	0	0	\N	\N	7	-1	1
905	1	1	f	0	0	0	\N	\N	8	-1	1
905	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	9	-1	1
905	6	6	f	0	0	0	8846f7eaee8fb117ad06bdd830b7586c	\N	251	-1	1
905	1	1	f	0	0	0	\N	\N	11	-1	1
905	6	6	f	0	0	0	md4	\N	252	0	1
903	3	3	f	-1	0	0	\N	\N	120	-1	1
906	6	6	f	0	0	0	SiteSearch-cZXBaCaYHqqynWr	\N	26	-1	1
906	6	6	f	0	0	0	PRIVATE	\N	107	-1	1
906	6	6	f	0	0	0	SiteSearch-cZXBaCaYHqqynWrPUBLIC	\N	27	-1	3
906	6	6	f	0	0	0	SiteSearch-cZXBaCaYHqqynWr	\N	28	-1	3
906	6	6	f	0	0	0	site-dashboard	\N	108	-1	1
907	6	6	f	0	0	0	b6cda8e17d77a66de7bc87c9ab79ba8f	\N	26	-1	1
907	6	6	f	0	0	0	GROUP_site_SiteSearch-cZXBaCaYHqqynWr	\N	90	-1	1
907	6	6	f	0	0	0	site_SiteSearch-cZXBaCaYHqqynWr	\N	112	-1	1
907	3	3	f	2218622580	0	0	\N	\N	94	-1	1
907	3	3	f	45	0	0	\N	\N	96	-1	1
908	6	6	f	0	0	0	619626a105e3cee9e05d52e3c8871120	\N	26	-1	1
908	6	6	f	0	0	0	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteManager	\N	90	-1	1
908	6	6	f	0	0	0	site_SiteSearch-cZXBaCaYHqqynWr_SiteManager	\N	112	-1	1
908	3	3	f	45	0	0	\N	\N	96	-1	1
908	3	3	f	2315432156	0	0	\N	\N	94	-1	1
909	6	6	f	0	0	0	4a8da09d9b0e7322b5d2a00347931eed	\N	26	-1	1
909	6	6	f	0	0	0	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteCollaborator	\N	90	-1	1
909	6	6	f	0	0	0	site_SiteSearch-cZXBaCaYHqqynWr_SiteCollaborator	\N	112	-1	1
909	3	3	f	45	0	0	\N	\N	96	-1	1
909	3	3	f	928325457	0	0	\N	\N	94	-1	1
910	6	6	f	0	0	0	290f71d786839d0fa237fdbfdd7de322	\N	26	-1	1
910	6	6	f	0	0	0	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteContributor	\N	90	-1	1
910	6	6	f	0	0	0	site_SiteSearch-cZXBaCaYHqqynWr_SiteContributor	\N	112	-1	1
910	3	3	f	45	0	0	\N	\N	96	-1	1
910	3	3	f	994596683	0	0	\N	\N	94	-1	1
911	6	6	f	0	0	0	1f42a80fc5f1011c4e345e7574d25829	\N	26	-1	1
911	6	6	f	0	0	0	GROUP_site_SiteSearch-cZXBaCaYHqqynWr_SiteConsumer	\N	90	-1	1
911	6	6	f	0	0	0	site_SiteSearch-cZXBaCaYHqqynWr_SiteConsumer	\N	112	-1	1
911	3	3	f	45	0	0	\N	\N	96	-1	1
911	3	3	f	633632131	0	0	\N	\N	94	-1	1
903	3	3	f	2455197147	0	0	\N	\N	94	-1	1
903	3	3	f	45	0	0	\N	\N	96	-1	1
912	6	6	f	0	0	0	surf-config	\N	26	-1	1
912	1	1	t	0	0	0	\N	\N	86	-1	1
912	1	1	f	0	0	0	\N	\N	87	-1	1
912	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
912	2	3	f	0	0	0	\N	\N	82	-1	1
912	1	1	f	0	0	0	\N	\N	83	-1	1
912	1	1	f	0	0	0	\N	\N	84	-1	1
912	1	1	f	0	0	0	\N	\N	222	-1	1
913	6	6	f	0	0	0	components	\N	26	-1	1
913	1	1	t	0	0	0	\N	\N	86	-1	1
913	1	1	f	0	0	0	\N	\N	87	-1	1
913	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
914	6	6	f	0	0	0	page.title.site~SiteSearch-cZXBaCaYHqqynWr~dashboard.xml	\N	26	-1	1
914	1	1	t	0	0	0	\N	\N	86	-1	1
914	1	1	f	0	0	0	\N	\N	87	-1	1
914	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
914	21	3	f	278	0	0	\N	\N	51	-1	1
915	6	6	f	0	0	0	page.navigation.site~SiteSearch-cZXBaCaYHqqynWr~dashboard.xml	\N	26	-1	1
915	1	1	t	0	0	0	\N	\N	86	-1	1
915	1	1	f	0	0	0	\N	\N	87	-1	1
915	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
915	21	3	f	279	0	0	\N	\N	51	-1	1
916	6	6	f	0	0	0	page.component-1-1.site~SiteSearch-cZXBaCaYHqqynWr~dashboard.xml	\N	26	-1	1
916	1	1	t	0	0	0	\N	\N	86	-1	1
916	1	1	f	0	0	0	\N	\N	87	-1	1
916	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
916	21	3	f	280	0	0	\N	\N	51	-1	1
917	6	6	f	0	0	0	page.component-2-1.site~SiteSearch-cZXBaCaYHqqynWr~dashboard.xml	\N	26	-1	1
917	1	1	t	0	0	0	\N	\N	86	-1	1
917	1	1	f	0	0	0	\N	\N	87	-1	1
917	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
917	21	3	f	281	0	0	\N	\N	51	-1	1
918	6	6	f	0	0	0	page.component-2-2.site~SiteSearch-cZXBaCaYHqqynWr~dashboard.xml	\N	26	-1	1
918	1	1	t	0	0	0	\N	\N	86	-1	1
918	1	1	f	0	0	0	\N	\N	87	-1	1
918	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
918	21	3	f	282	0	0	\N	\N	51	-1	1
919	6	6	f	0	0	0	pages	\N	26	-1	1
919	1	1	t	0	0	0	\N	\N	86	-1	1
919	1	1	f	0	0	0	\N	\N	87	-1	1
919	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
920	6	6	f	0	0	0	site	\N	26	-1	1
920	1	1	t	0	0	0	\N	\N	86	-1	1
920	1	1	f	0	0	0	\N	\N	87	-1	1
920	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
921	6	6	f	0	0	0	SiteSearch-cZXBaCaYHqqynWr	\N	26	-1	1
921	1	1	t	0	0	0	\N	\N	86	-1	1
921	1	1	f	0	0	0	\N	\N	87	-1	1
921	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
922	6	6	f	0	0	0	dashboard.xml	\N	26	-1	1
922	1	1	t	0	0	0	\N	\N	86	-1	1
922	1	1	f	0	0	0	\N	\N	87	-1	1
922	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	40	-1	1
922	21	3	f	283	0	0	\N	\N	51	-1	1
923	6	6	f	0	0	0	documentLibrary	\N	26	-1	1
923	6	6	f	0	0	0	documentLibrary	\N	152	-1	1
903	21	3	f	284	0	0	\N	\N	135	-1	1
924	6	6	f	0	0	0	folder-TNiecQUWtKwqYRo	\N	26	-1	1
925	6	6	f	0	0	0	file-GZzBkqpJQZrNHpg.txt	\N	26	-1	1
925	6	6	f	0	0	0	t@t	\N	254	0	1
925	6	6	f	0	0	0	e@e	\N	254	1	1
925	21	3	f	285	0	0	\N	\N	51	-1	1
925	1	1	f	0	0	0	\N	\N	155	-1	1
925	1	1	t	0	0	0	\N	\N	158	-1	1
925	1	1	t	0	0	0	\N	\N	159	-1	1
926	6	6	f	0	0	0	a170644e-906e-427c-8909-99b62d445246	\N	26	-1	1
926	6	6	f	0	0	0	a170644e-906e-427c-8909-99b62d445246	\N	165	-1	1
927	6	6	f	0	0	0	file-GZzBkqpJQZrNHpg.txt	\N	26	-1	1
927	21	3	f	286	0	0	\N	\N	51	-1	1
927	12	6	f	0	0	0	workspace://SpacesStore/a170644e-906e-427c-8909-99b62d445246	\N	167	-1	1
927	6	6	f	0	0	0	Initial Version	\N	168	-1	1
927	6	6	f	0	0	0	1.0	\N	169	-1	1
927	3	3	f	925	0	0	\N	\N	170	-1	1
927	9	9	f	0	0	0	\N	\\xaced00057e72002c6f72672e616c66726573636f2e736572766963652e636d722e76657273696f6e2e56657273696f6e5479706500000000000000001200007872000e6a6176612e6c616e672e456e756d000000000000000012000078707400054d414a4f52	250	-1	1
927	0	0	f	0	0	0	\N	\N	257	-1	1
927	0	0	f	0	0	0	\N	\N	258	-1	1
927	0	0	f	0	0	0	\N	\N	255	-1	1
927	6	6	f	0	0	0	t@t	\N	254	0	1
927	0	0	f	0	0	0	\N	\N	256	-1	1
927	6	6	f	0	0	0	e@e	\N	254	1	1
927	1	1	f	0	0	0	\N	\N	155	-1	1
927	0	0	f	0	0	0	\N	\N	171	-1	1
927	0	0	f	0	0	0	\N	\N	157	-1	1
927	1	1	t	0	0	0	\N	\N	158	-1	1
927	1	1	t	0	0	0	\N	\N	159	-1	1
927	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	172	-1	1
927	7	6	f	0	0	0	2021-04-21T12:41:56.591Z	\N	173	-1	1
927	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	174	-1	1
927	7	6	f	0	0	0	2021-04-21T12:41:56.591Z	\N	175	-1	1
927	0	0	f	0	0	0	\N	\N	176	-1	1
925	6	6	f	0	0	0	1.0	\N	157	-1	1
925	6	6	f	0	0	0	MAJOR	\N	171	-1	1
928	6	6	f	0	0	0	file-ksihTJbQvNShWkz.txt	\N	26	-1	1
928	6	6	f	0	0	0	t@t	\N	254	0	1
928	6	6	f	0	0	0	e@e	\N	254	1	1
928	21	3	f	287	0	0	\N	\N	51	-1	1
928	1	1	f	0	0	0	\N	\N	155	-1	1
928	1	1	t	0	0	0	\N	\N	158	-1	1
928	1	1	t	0	0	0	\N	\N	159	-1	1
929	6	6	f	0	0	0	7b0ca868-9d44-4f54-be37-3565f3a0b0fb	\N	26	-1	1
929	6	6	f	0	0	0	7b0ca868-9d44-4f54-be37-3565f3a0b0fb	\N	165	-1	1
930	6	6	f	0	0	0	file-ksihTJbQvNShWkz.txt	\N	26	-1	1
930	21	3	f	288	0	0	\N	\N	51	-1	1
930	12	6	f	0	0	0	workspace://SpacesStore/7b0ca868-9d44-4f54-be37-3565f3a0b0fb	\N	167	-1	1
930	6	6	f	0	0	0	Initial Version	\N	168	-1	1
930	6	6	f	0	0	0	1.0	\N	169	-1	1
930	3	3	f	928	0	0	\N	\N	170	-1	1
930	9	9	f	0	0	0	\N	\\xaced00057e72002c6f72672e616c66726573636f2e736572766963652e636d722e76657273696f6e2e56657273696f6e5479706500000000000000001200007872000e6a6176612e6c616e672e456e756d000000000000000012000078707400054d414a4f52	250	-1	1
930	0	0	f	0	0	0	\N	\N	257	-1	1
930	0	0	f	0	0	0	\N	\N	258	-1	1
930	0	0	f	0	0	0	\N	\N	255	-1	1
930	6	6	f	0	0	0	t@t	\N	254	0	1
930	0	0	f	0	0	0	\N	\N	256	-1	1
930	6	6	f	0	0	0	e@e	\N	254	1	1
930	1	1	f	0	0	0	\N	\N	155	-1	1
930	0	0	f	0	0	0	\N	\N	171	-1	1
930	0	0	f	0	0	0	\N	\N	157	-1	1
930	1	1	t	0	0	0	\N	\N	158	-1	1
930	1	1	t	0	0	0	\N	\N	159	-1	1
930	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	172	-1	1
930	7	6	f	0	0	0	2021-04-21T12:41:56.977Z	\N	173	-1	1
930	6	6	f	0	0	0	UserSearch-zGiNtcNRnaJPlLO	\N	174	-1	1
930	7	6	f	0	0	0	2021-04-21T12:41:56.977Z	\N	175	-1	1
930	0	0	f	0	0	0	\N	\N	176	-1	1
928	6	6	f	0	0	0	1.0	\N	157	-1	1
928	6	6	f	0	0	0	MAJOR	\N	171	-1	1
931	6	6	f	0	0	0	acl-benchmark	\N	26	-1	1
932	6	6	f	0	0	0	folder-1	\N	26	-1	1
933	6	6	f	0	0	0	folder-0	\N	26	-1	1
934	6	6	f	0	0	0	e7003d05-3b90-4e1a-8790-b4751f42cee9-json	\N	26	-1	1
935	6	6	f	0	0	0	f2627085-5a7b-4abe-89f2-aa6b82b3f291-json	\N	26	-1	1
936	2	3	f	10	0	0	\N	\N	259	-1	1
936	6	6	f	0	0	0	0090cecf-0460-41e9-91fa-d81dc91e2cbb	\N	261	-1	1
936	7	6	f	0	0	0	2012-05-29T01:48:30.000Z	\N	262	-1	1
936	7	6	f	0	0	0	1927-12-22T23:30:36.000Z	\N	263	-1	1
936	6	6	f	0	0	0	Legend	\N	264	-1	1
936	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
936	6	6	f	0	0	0	Direct being edition am.pptx	\N	26	-1	1
936	6	6	f	0	0	0	Rosemaria Anais Allrud	\N	266	-1	1
936	6	6	f	0	0	0	An knee now.	\N	27	-1	3
936	6	6	f	0	0	0	Tools update reference michael.	\N	28	-1	3
936	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_8mb_10mb/prayer merger.pptx	\N	272	0	1
940	2	3	f	45	0	0	\N	\N	259	-1	1
940	6	6	f	0	0	0	27ea2e69-229d-4be1-a56f-9eb16e3cd8c6	\N	261	-1	1
940	7	6	f	0	0	0	1985-07-23T03:06:46.000Z	\N	262	-1	1
940	7	6	f	0	0	0	1926-07-02T20:17:41.000Z	\N	263	-1	1
940	6	6	f	0	0	0	Legend	\N	264	-1	1
940	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
940	6	6	f	0	0	0	Effects html.txt	\N	26	-1	1
940	6	6	f	0	0	0	Lorena Anais Moir	\N	266	-1	1
940	6	6	f	0	0	0	But.	\N	27	-1	3
940	6	6	f	0	0	0	But.	\N	28	-1	3
941	2	3	f	54	0	0	\N	\N	259	-1	1
941	6	6	f	0	0	0	f4cafb90-dc4a-4dff-87d7-a317251dece0	\N	261	-1	1
941	7	6	f	0	0	0	2005-04-08T00:49:44.000Z	\N	262	-1	1
941	7	6	f	0	0	0	1987-09-04T14:35:15.000Z	\N	263	-1	1
941	6	6	f	0	0	0	Magical Realism	\N	264	-1	1
941	6	6	f	0	0	0	Elsevier	\N	265	-1	1
942	2	3	f	67	0	0	\N	\N	259	-1	1
941	6	6	f	0	0	0	Months misc communications champions.docx	\N	26	-1	1
942	6	6	f	0	0	0	e4e8b410-6373-4b60-8ac7-04fb7d0b3e62	\N	261	-1	1
941	6	6	f	0	0	0	Kathi Xiomara Annabelle	\N	266	-1	1
941	6	6	f	0	0	0	Az rendered privacy.	\N	27	-1	3
941	6	6	f	0	0	0	Phpbb queens forced monday. Drop garden login pm.	\N	28	-1	3
942	7	6	f	0	0	0	1983-01-20T16:11:24.000Z	\N	262	-1	1
942	7	6	f	0	0	0	1971-10-13T13:40:48.000Z	\N	263	-1	1
942	6	6	f	0	0	0	Science Fiction	\N	264	-1	1
942	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
943	2	3	f	48	0	0	\N	\N	259	-1	1
943	6	6	f	0	0	0	c862c52e-a14e-4de4-80a1-c57a05f30f75	\N	261	-1	1
943	7	6	f	0	0	0	2102-04-02T13:18:21.000Z	\N	262	-1	1
943	7	6	f	0	0	0	2016-07-31T23:43:04.000Z	\N	263	-1	1
942	6	6	f	0	0	0	Non activities has.pptx	\N	26	-1	1
942	6	6	f	0	0	0	Joni Anais Derman	\N	266	-1	1
942	6	6	f	0	0	0	Past beta tickets.	\N	27	-1	3
942	6	6	f	0	0	0	India clark simple goals.	\N	28	-1	3
943	6	6	f	0	0	0	Crime and Detective	\N	264	-1	1
943	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
943	6	6	f	0	0	0	Nc september garden.docx	\N	26	-1	1
943	6	6	f	0	0	0	Linnea Antonella Beitch	\N	266	-1	1
943	6	6	f	0	0	0	Board. Ago below.	\N	27	-1	3
943	6	6	f	0	0	0	Reply usa.	\N	28	-1	3
941	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_2mb_4mb/tired.docx	\N	272	0	1
942	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_0kb_500kb/monitoring zip sudan stan.pptx	\N	272	0	1
940	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_8mb_10mb/accounts sudden.txt	\N	272	0	1
943	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_500kb_2mb/baths influenced.docx	\N	272	0	1
944	2	3	f	18	0	0	\N	\N	259	-1	1
944	6	6	f	0	0	0	aa45ea38-d732-415c-b67e-6798c28504ed	\N	261	-1	1
944	7	6	f	0	0	0	1990-01-26T12:03:56.000Z	\N	262	-1	1
944	7	6	f	0	0	0	1961-07-10T02:57:27.000Z	\N	263	-1	1
944	6	6	f	0	0	0	Mystery	\N	264	-1	1
945	2	3	f	35	0	0	\N	\N	259	-1	1
945	6	6	f	0	0	0	feb4067c-bb6c-400e-887b-71b354d9fdfa	\N	261	-1	1
944	6	6	f	0	0	0	Elsevier	\N	265	-1	1
944	6	6	f	0	0	0	Step counter already.txt	\N	26	-1	1
944	6	6	f	0	0	0	Kristin Anais Allison	\N	266	-1	1
944	6	6	f	0	0	0	What great sale assists.	\N	27	-1	3
944	6	6	f	0	0	0	Marketing performs. Feel project.	\N	28	-1	3
944	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_4mb_6mb/aqua discounts.txt	\N	272	0	1
948	2	3	f	34	0	0	\N	\N	259	-1	1
948	6	6	f	0	0	0	e5c2b7ef-91c6-4a59-a77d-441bc649bfd7	\N	261	-1	1
948	7	6	f	0	0	0	2036-02-24T22:00:03.000Z	\N	262	-1	1
948	7	6	f	0	0	0	1933-05-06T14:47:27.000Z	\N	263	-1	1
948	6	6	f	0	0	0	Horror	\N	264	-1	1
948	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
948	6	6	f	0	0	0	Please discounted base alaska.pptx	\N	26	-1	1
948	6	6	f	0	0	0	Agna Anais Ahmar	\N	266	-1	1
948	6	6	f	0	0	0	Again. Prevent national.	\N	27	-1	3
948	6	6	f	0	0	0	Cards ordinance md. Office similar.	\N	28	-1	3
948	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_6mb_8mb/unemployment.pptx	\N	272	0	1
952	2	3	f	57	0	0	\N	\N	259	-1	1
952	6	6	f	0	0	0	300962ae-3759-4176-a5c9-49dbdd59b0ac	\N	261	-1	1
952	7	6	f	0	0	0	1990-12-24T06:10:04.000Z	\N	262	-1	1
952	7	6	f	0	0	0	1987-07-15T14:53:44.000Z	\N	263	-1	1
952	6	6	f	0	0	0	Magical Realism	\N	264	-1	1
952	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
952	6	6	f	0	0	0	Least under.jpg	\N	26	-1	1
952	6	6	f	0	0	0	Lissa Kehlani Afton	\N	266	-1	1
952	6	6	f	0	0	0	Crucial. Company old features files.	\N	27	-1	3
952	6	6	f	0	0	0	Dr anna. Comments reduce thing issue.	\N	28	-1	3
952	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_8mb_10mb/go person ef.jpg	\N	272	0	1
956	2	3	f	59	0	0	\N	\N	259	-1	1
956	6	6	f	0	0	0	6bb10e45-ddb7-431d-98fd-9478bea44ce6	\N	261	-1	1
956	7	6	f	0	0	0	2007-04-20T05:56:35.000Z	\N	262	-1	1
956	7	6	f	0	0	0	2014-01-31T15:31:46.000Z	\N	263	-1	1
956	6	6	f	0	0	0	Horror	\N	264	-1	1
956	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
956	6	6	f	0	0	0	Others solutions daughter address.docx	\N	26	-1	1
956	6	6	f	0	0	0	Aloysia Kehlani Downes	\N	266	-1	1
956	6	6	f	0	0	0	Apr. F industry.	\N	27	-1	3
956	6	6	f	0	0	0	Hardware n.	\N	28	-1	3
956	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_500kb_2mb/baths influenced.docx	\N	272	0	1
960	2	3	f	98	0	0	\N	\N	259	-1	1
960	6	6	f	0	0	0	2e67b72a-6a88-4955-bb67-1007d0ca7653	\N	261	-1	1
960	7	6	f	0	0	0	1984-12-22T14:22:31.000Z	\N	262	-1	1
960	7	6	f	0	0	0	1945-07-17T03:58:28.000Z	\N	263	-1	1
960	6	6	f	0	0	0	Periodicals	\N	264	-1	1
960	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
960	6	6	f	0	0	0	Decent pr dollar.jpg	\N	26	-1	1
960	6	6	f	0	0	0	Lotty Anais Azal	\N	266	-1	1
960	6	6	f	0	0	0	No review. Risk daisy august.	\N	27	-1	3
960	6	6	f	0	0	0	Wales fishing adolescent only.	\N	28	-1	3
977	2	3	f	68	0	0	\N	\N	259	-1	1
977	6	6	f	0	0	0	99f99209-0be8-4b94-ab78-bbb11fde8ede	\N	261	-1	1
977	7	6	f	0	0	0	2094-10-25T01:58:50.000Z	\N	262	-1	1
977	7	6	f	0	0	0	1938-03-21T05:27:42.000Z	\N	263	-1	1
977	6	6	f	0	0	0	Memoir	\N	264	-1	1
977	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
977	6	6	f	0	0	0	O options her friend.jpg	\N	26	-1	1
977	6	6	f	0	0	0	Lorenza Zora Bethel	\N	266	-1	1
977	6	6	f	0	0	0	Furthermore.	\N	27	-1	3
977	6	6	f	0	0	0	Manager. Office.	\N	28	-1	3
977	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_500kb_2mb/sg cloth cv sanyo.jpg	\N	272	0	1
945	7	6	f	0	0	0	2055-03-12T11:58:45.000Z	\N	262	-1	1
945	7	6	f	0	0	0	1913-11-16T01:30:26.000Z	\N	263	-1	1
945	6	6	f	0	0	0	Biography/Autobiography	\N	264	-1	1
945	6	6	f	0	0	0	Random House	\N	265	-1	1
945	6	6	f	0	0	0	Wireless cover drives.pptx	\N	26	-1	1
945	6	6	f	0	0	0	Loella Kehlani Calderon	\N	266	-1	1
945	6	6	f	0	0	0	Space.	\N	27	-1	3
945	6	6	f	0	0	0	Reply ii.	\N	28	-1	3
959	2	3	f	62	0	0	\N	\N	259	-1	1
959	6	6	f	0	0	0	1fb72d41-bda8-4326-9a20-e43b576b85cd	\N	261	-1	1
959	7	6	f	0	0	0	2061-05-25T00:29:31.000Z	\N	262	-1	1
959	7	6	f	0	0	0	1963-04-23T04:41:21.000Z	\N	263	-1	1
959	6	6	f	0	0	0	Legend	\N	264	-1	1
959	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
959	6	6	f	0	0	0	Room m da.pptx	\N	26	-1	1
959	6	6	f	0	0	0	Kelsi Zora Candi	\N	266	-1	1
959	6	6	f	0	0	0	Support browser hobbies. Prices.	\N	27	-1	3
959	6	6	f	0	0	0	Party warriors.	\N	28	-1	3
959	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_8mb_10mb/prayer merger.pptx	\N	272	0	1
965	2	3	f	99	0	0	\N	\N	259	-1	1
965	6	6	f	0	0	0	ccebef2b-28b1-4e71-8264-0a4cd9911a81	\N	261	-1	1
965	7	6	f	0	0	0	1978-05-16T03:38:59.000Z	\N	262	-1	1
965	7	6	f	0	0	0	1991-01-09T04:06:11.000Z	\N	263	-1	1
965	6	6	f	0	0	0	Short Story	\N	264	-1	1
965	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
965	6	6	f	0	0	0	Civil moved.jpg	\N	26	-1	1
965	6	6	f	0	0	0	Kitti Anais Barren	\N	266	-1	1
965	6	6	f	0	0	0	Product be balance.	\N	27	-1	3
965	6	6	f	0	0	0	Do market media cd.	\N	28	-1	3
965	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_8mb_10mb/go person ef.jpg	\N	272	0	1
979	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_6mb_8mb/unemployment.pptx	\N	272	0	1
982	2	3	f	31	0	0	\N	\N	259	-1	1
982	6	6	f	0	0	0	ee4c13c0-e848-46c9-ad1c-83edaa971ba2	\N	261	-1	1
982	7	6	f	0	0	0	2027-01-24T15:52:25.000Z	\N	262	-1	1
982	7	6	f	0	0	0	1985-02-15T19:28:50.000Z	\N	263	-1	1
982	6	6	f	0	0	0	Narrative Nonfiction	\N	264	-1	1
982	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
982	6	6	f	0	0	0	Club gear including he.txt	\N	26	-1	1
982	6	6	f	0	0	0	Liora Anais Alabaster	\N	266	-1	1
982	6	6	f	0	0	0	Post nutrition jerusalem. Last final.	\N	27	-1	3
982	6	6	f	0	0	0	Fingers gifts sure second.	\N	28	-1	3
982	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_2mb_4mb/battle compliance monkey.txt	\N	272	0	1
986	2	3	f	66	0	0	\N	\N	259	-1	1
986	6	6	f	0	0	0	dd9e08de-3ffe-48eb-9587-c7cead905aee	\N	261	-1	1
986	7	6	f	0	0	0	2103-02-09T00:12:46.000Z	\N	262	-1	1
986	7	6	f	0	0	0	1913-03-11T10:42:42.000Z	\N	263	-1	1
986	6	6	f	0	0	0	Self-help Book	\N	264	-1	1
986	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
986	6	6	f	0	0	0	And p children.jpg	\N	26	-1	1
986	6	6	f	0	0	0	Kikelia Saoirse Bernardi	\N	266	-1	1
986	6	6	f	0	0	0	Professor fax philadelphia red.	\N	27	-1	3
986	6	6	f	0	0	0	Broadcast.	\N	28	-1	3
986	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_0kb_500kb/redeem.jpg	\N	272	0	1
990	2	3	f	33	0	0	\N	\N	259	-1	1
990	6	6	f	0	0	0	f67d00e0-b08e-409b-94f4-7ba99e81b61d	\N	261	-1	1
990	7	6	f	0	0	0	2043-12-25T10:57:25.000Z	\N	262	-1	1
990	7	6	f	0	0	0	1962-01-12T17:22:44.000Z	\N	263	-1	1
990	6	6	f	0	0	0	Biography/Autobiography	\N	264	-1	1
990	6	6	f	0	0	0	Random House	\N	265	-1	1
947	2	3	f	46	0	0	\N	\N	259	-1	1
947	6	6	f	0	0	0	eb9d6b53-1b45-4c9b-b64f-c7a4155ac0ab	\N	261	-1	1
947	7	6	f	0	0	0	2076-03-22T08:19:18.000Z	\N	262	-1	1
947	7	6	f	0	0	0	1961-07-23T15:11:23.000Z	\N	263	-1	1
947	6	6	f	0	0	0	Horror	\N	264	-1	1
947	6	6	f	0	0	0	Elsevier	\N	265	-1	1
947	6	6	f	0	0	0	Lead type.pptx	\N	26	-1	1
947	6	6	f	0	0	0	Karolina Anais Vasily	\N	266	-1	1
947	6	6	f	0	0	0	Macromedia. Que using credit.	\N	27	-1	3
947	6	6	f	0	0	0	Against world very. Till bank.	\N	28	-1	3
954	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
958	2	3	f	70	0	0	\N	\N	259	-1	1
958	6	6	f	0	0	0	6eb4ff94-ce71-40bd-baf0-b9c4d1be4a45	\N	261	-1	1
958	7	6	f	0	0	0	1975-06-20T02:02:36.000Z	\N	262	-1	1
958	7	6	f	0	0	0	1971-05-30T16:55:04.000Z	\N	263	-1	1
958	6	6	f	0	0	0	Anthology	\N	264	-1	1
958	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
958	6	6	f	0	0	0	Addition shows start.txt	\N	26	-1	1
958	6	6	f	0	0	0	Kirstyn Anais Kerri	\N	266	-1	1
958	6	6	f	0	0	0	Experience view content similar.	\N	27	-1	3
958	6	6	f	0	0	0	About estate made survivors.	\N	28	-1	3
958	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_2mb_4mb/battle compliance monkey.txt	\N	272	0	1
962	2	3	f	72	0	0	\N	\N	259	-1	1
962	6	6	f	0	0	0	2ed74f46-6b50-466d-a82b-27cedc3edb78	\N	261	-1	1
962	7	6	f	0	0	0	2066-02-14T23:45:49.000Z	\N	262	-1	1
962	7	6	f	0	0	0	1959-03-24T06:52:44.000Z	\N	263	-1	1
962	6	6	f	0	0	0	Romance	\N	264	-1	1
962	6	6	f	0	0	0	Elsevier	\N	265	-1	1
962	6	6	f	0	0	0	Logos results york.jpg	\N	26	-1	1
962	6	6	f	0	0	0	Tomasina Berkley Bahr	\N	266	-1	1
962	6	6	f	0	0	0	Penny bits powered.	\N	27	-1	3
962	6	6	f	0	0	0	Images votes auto. Somebody permissions words atom.	\N	28	-1	3
962	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_8mb_10mb/go person ef.jpg	\N	272	0	1
966	2	3	f	96	0	0	\N	\N	259	-1	1
966	6	6	f	0	0	0	82b7245a-2ef4-4805-a7d8-2a3685079528	\N	261	-1	1
966	7	6	f	0	0	0	2058-08-18T07:01:33.000Z	\N	262	-1	1
966	7	6	f	0	0	0	1981-02-08T08:45:40.000Z	\N	263	-1	1
966	6	6	f	0	0	0	Historical Fiction	\N	264	-1	1
966	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
966	6	6	f	0	0	0	Risk mt projects description.jpg	\N	26	-1	1
966	6	6	f	0	0	0	Gamaliel Anais Gothar	\N	266	-1	1
966	6	6	f	0	0	0	Georgia division.	\N	27	-1	3
966	6	6	f	0	0	0	Compliance long. Rentals learning.	\N	28	-1	3
968	2	3	f	57	0	0	\N	\N	259	-1	1
968	6	6	f	0	0	0	9f62c19a-5464-4026-83ee-b4fee1fa0db6	\N	261	-1	1
968	7	6	f	0	0	0	2040-11-14T05:26:41.000Z	\N	262	-1	1
968	7	6	f	0	0	0	1924-12-30T21:12:48.000Z	\N	263	-1	1
968	6	6	f	0	0	0	Fantasy	\N	264	-1	1
968	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
968	6	6	f	0	0	0	Breakfast represent.jpg	\N	26	-1	1
968	6	6	f	0	0	0	Joli Berkley Amorita	\N	266	-1	1
968	6	6	f	0	0	0	Right laws space fifty. Now board.	\N	27	-1	3
968	6	6	f	0	0	0	Attorney statement noon.	\N	28	-1	3
968	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_4mb_6mb/safe enhancement omaha.jpg	\N	272	0	1
972	2	3	f	36	0	0	\N	\N	259	-1	1
972	6	6	f	0	0	0	d4887790-106b-417b-91da-dc649d3e83bf	\N	261	-1	1
972	7	6	f	0	0	0	2095-01-20T04:44:19.000Z	\N	262	-1	1
972	7	6	f	0	0	0	1917-09-21T20:09:43.000Z	\N	263	-1	1
946	2	3	f	90	0	0	\N	\N	259	-1	1
946	6	6	f	0	0	0	fb6e1a83-5c26-40ab-9d57-1102c49ffc59	\N	261	-1	1
946	7	6	f	0	0	0	2008-08-28T13:08:26.000Z	\N	262	-1	1
946	7	6	f	0	0	0	1996-10-28T02:14:43.000Z	\N	263	-1	1
946	6	6	f	0	0	0	Speech	\N	264	-1	1
946	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
946	6	6	f	0	0	0	Screw testing relaxation.jpg	\N	26	-1	1
946	6	6	f	0	0	0	Kendra Melani Arbe	\N	266	-1	1
946	6	6	f	0	0	0	User tiger.	\N	27	-1	3
946	6	6	f	0	0	0	Section. Epson.	\N	28	-1	3
946	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_0kb_500kb/redeem.jpg	\N	272	0	1
951	2	3	f	38	0	0	\N	\N	259	-1	1
951	6	6	f	0	0	0	19c9afaa-b6b6-46df-879c-c37d99478b38	\N	261	-1	1
951	7	6	f	0	0	0	2077-08-27T23:28:01.000Z	\N	262	-1	1
951	7	6	f	0	0	0	2009-11-05T18:12:16.000Z	\N	263	-1	1
951	6	6	f	0	0	0	Comic and Graphic Novel	\N	264	-1	1
951	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
951	6	6	f	0	0	0	Reserved makes.pptx	\N	26	-1	1
951	6	6	f	0	0	0	Brandice Anais Albin	\N	266	-1	1
951	6	6	f	0	0	0	Body one then. Bus.	\N	27	-1	3
951	6	6	f	0	0	0	Four assets bids write. Distributed.	\N	28	-1	3
951	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_6mb_8mb/unemployment.pptx	\N	272	0	1
963	2	3	f	50	0	0	\N	\N	259	-1	1
963	6	6	f	0	0	0	8896241f-1c2b-4356-b5f2-f7651283aa8b	\N	261	-1	1
963	7	6	f	0	0	0	2057-03-20T00:57:46.000Z	\N	262	-1	1
963	7	6	f	0	0	0	1976-02-17T06:01:36.000Z	\N	263	-1	1
963	6	6	f	0	0	0	Textbook	\N	264	-1	1
963	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
963	6	6	f	0	0	0	Tools courts style obtained.jpg	\N	26	-1	1
963	6	6	f	0	0	0	Patrick Berkley Amabil	\N	266	-1	1
963	6	6	f	0	0	0	Hungarian. Co reserved.	\N	27	-1	3
963	6	6	f	0	0	0	Fort. Utility quick fork stories.	\N	28	-1	3
963	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_4mb_6mb/safe enhancement omaha.jpg	\N	272	0	1
967	2	3	f	43	0	0	\N	\N	259	-1	1
967	6	6	f	0	0	0	5d3eb12f-1d30-4dfe-be4f-8c8cb810d2dd	\N	261	-1	1
967	7	6	f	0	0	0	2066-02-26T09:25:28.000Z	\N	262	-1	1
967	7	6	f	0	0	0	1946-10-08T16:17:53.000Z	\N	263	-1	1
967	6	6	f	0	0	0	Reference Books	\N	264	-1	1
967	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
967	6	6	f	0	0	0	Art wedding.docx	\N	26	-1	1
967	6	6	f	0	0	0	Loren Berkley Sebastien	\N	266	-1	1
967	6	6	f	0	0	0	Photo move firm view. September.	\N	27	-1	3
967	6	6	f	0	0	0	Multi highs appeal thumbnail. Act.	\N	28	-1	3
967	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_500kb_2mb/baths influenced.docx	\N	272	0	1
971	2	3	f	28	0	0	\N	\N	259	-1	1
971	6	6	f	0	0	0	a43f8dbd-e679-4997-ba77-6752abef1239	\N	261	-1	1
971	7	6	f	0	0	0	1995-03-12T00:25:11.000Z	\N	262	-1	1
971	7	6	f	0	0	0	2003-02-23T08:53:13.000Z	\N	263	-1	1
971	6	6	f	0	0	0	Fantasy	\N	264	-1	1
971	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
971	6	6	f	0	0	0	Culture smoking.pdf	\N	26	-1	1
971	6	6	f	0	0	0	Kendre Anais Kooima	\N	266	-1	1
971	6	6	f	0	0	0	Gay warranty disney indeed.	\N	27	-1	3
971	6	6	f	0	0	0	Clusters percent city.	\N	28	-1	3
971	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
975	2	3	f	47	0	0	\N	\N	259	-1	1
975	6	6	f	0	0	0	e3953ea3-654d-4a32-a08e-98e9bb2cc5c8	\N	261	-1	1
975	7	6	f	0	0	0	2070-08-12T22:59:15.000Z	\N	262	-1	1
947	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_4mb_6mb/unified ftp.pptx	\N	272	0	1
945	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_8mb_10mb/prayer merger.pptx	\N	272	0	1
950	2	3	f	4	0	0	\N	\N	259	-1	1
950	6	6	f	0	0	0	c06b6715-58c4-456b-bffc-34cc34709f73	\N	261	-1	1
950	7	6	f	0	0	0	1991-05-22T16:11:28.000Z	\N	262	-1	1
950	7	6	f	0	0	0	1980-07-24T04:46:01.000Z	\N	263	-1	1
950	6	6	f	0	0	0	Classic	\N	264	-1	1
950	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
950	6	6	f	0	0	0	Home ready current.pptx	\N	26	-1	1
950	6	6	f	0	0	0	Aubree Zora Benzel	\N	266	-1	1
950	6	6	f	0	0	0	Down titles category. Electronics provided.	\N	27	-1	3
950	6	6	f	0	0	0	Con complete people call.	\N	28	-1	3
950	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_0kb_500kb/monitoring zip sudan stan.pptx	\N	272	0	1
953	2	3	f	6	0	0	\N	\N	259	-1	1
953	6	6	f	0	0	0	443ffdf5-b62a-40cb-910f-331ba8464634	\N	261	-1	1
953	7	6	f	0	0	0	2005-03-14T20:33:47.000Z	\N	262	-1	1
953	7	6	f	0	0	0	1952-10-17T20:42:39.000Z	\N	263	-1	1
953	6	6	f	0	0	0	Mystery	\N	264	-1	1
953	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
953	6	6	f	0	0	0	Type url.pptx	\N	26	-1	1
953	6	6	f	0	0	0	Mona Melani Sara	\N	266	-1	1
954	2	3	f	40	0	0	\N	\N	259	-1	1
954	6	6	f	0	0	0	ede2fbde-cdb6-434a-9169-c62394e96152	\N	261	-1	1
953	6	6	f	0	0	0	Think sure.	\N	27	-1	3
953	6	6	f	0	0	0	Listing illustration genes pics. Religious.	\N	28	-1	3
954	7	6	f	0	0	0	2066-07-09T20:16:53.000Z	\N	262	-1	1
954	7	6	f	0	0	0	1946-02-05T23:41:34.000Z	\N	263	-1	1
954	6	6	f	0	0	0	Fable	\N	264	-1	1
954	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
954	6	6	f	0	0	0	Diabetes issues supporters already.pdf	\N	26	-1	1
954	6	6	f	0	0	0	Lizabeth Anais Bartholomeo	\N	266	-1	1
954	6	6	f	0	0	0	Reserve.	\N	27	-1	3
954	6	6	f	0	0	0	Air.	\N	28	-1	3
955	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
953	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_500kb_2mb/saying carriers nascar feel.pptx	\N	272	0	1
957	2	3	f	79	0	0	\N	\N	259	-1	1
957	6	6	f	0	0	0	e8666c34-9ed4-43b8-9fc9-5aa658189daa	\N	261	-1	1
957	7	6	f	0	0	0	2025-12-20T03:48:53.000Z	\N	262	-1	1
957	7	6	f	0	0	0	1946-07-11T21:39:39.000Z	\N	263	-1	1
957	6	6	f	0	0	0	Reference Books	\N	264	-1	1
957	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
957	6	6	f	0	0	0	Eq present.pptx	\N	26	-1	1
957	6	6	f	0	0	0	Kimberley Zora Natalie	\N	266	-1	1
957	6	6	f	0	0	0	Happiness september romance. North study.	\N	27	-1	3
957	6	6	f	0	0	0	Sale newsletter.	\N	28	-1	3
957	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_2mb_4mb/nil climate enhancements.pptx	\N	272	0	1
960	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_500kb_2mb/sg cloth cv sanyo.jpg	\N	272	0	1
964	2	3	f	80	0	0	\N	\N	259	-1	1
964	6	6	f	0	0	0	424acfe5-c398-4613-9ccc-1e6d436ca45b	\N	261	-1	1
964	7	6	f	0	0	0	2096-01-16T03:34:23.000Z	\N	262	-1	1
964	7	6	f	0	0	0	1987-05-13T18:32:02.000Z	\N	263	-1	1
964	6	6	f	0	0	0	Biography/Autobiography	\N	264	-1	1
964	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
964	6	6	f	0	0	0	Qualifying am left exclusive.pdf	\N	26	-1	1
964	6	6	f	0	0	0	Leela Anais Behlau	\N	266	-1	1
964	6	6	f	0	0	0	Download. Medical centre nice note.	\N	27	-1	3
964	6	6	f	0	0	0	Loan africa minimum.	\N	28	-1	3
949	2	3	f	9	0	0	\N	\N	259	-1	1
949	6	6	f	0	0	0	3d991160-6b25-4ab4-88d2-2ee9029c7f59	\N	261	-1	1
949	7	6	f	0	0	0	2103-07-14T10:45:08.000Z	\N	262	-1	1
949	7	6	f	0	0	0	2005-08-10T17:29:55.000Z	\N	263	-1	1
949	6	6	f	0	0	0	Science Fiction	\N	264	-1	1
949	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
949	6	6	f	0	0	0	Reviews college.txt	\N	26	-1	1
949	6	6	f	0	0	0	Kittie Berkley Angus	\N	266	-1	1
949	6	6	f	0	0	0	Mar athletic ac us. Save pdf fact knowledge.	\N	27	-1	3
949	6	6	f	0	0	0	Cover.	\N	28	-1	3
949	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_0kb_500kb/basketball.txt	\N	272	0	1
955	2	3	f	62	0	0	\N	\N	259	-1	1
955	6	6	f	0	0	0	5ed92276-35c4-48d9-904d-5768c6914fad	\N	261	-1	1
955	7	6	f	0	0	0	2073-07-27T21:37:00.000Z	\N	262	-1	1
955	7	6	f	0	0	0	1975-08-31T17:46:16.000Z	\N	263	-1	1
955	6	6	f	0	0	0	Fable	\N	264	-1	1
955	6	6	f	0	0	0	Random House	\N	265	-1	1
955	6	6	f	0	0	0	Poland species too arise.pdf	\N	26	-1	1
955	6	6	f	0	0	0	Sianna Zora Barcroft	\N	266	-1	1
955	6	6	f	0	0	0	Open penalty select checked.	\N	27	-1	3
955	6	6	f	0	0	0	Rights.	\N	28	-1	3
961	2	3	f	74	0	0	\N	\N	259	-1	1
961	6	6	f	0	0	0	5c7a024b-1c25-438f-8df9-a0616b976293	\N	261	-1	1
961	7	6	f	0	0	0	2004-08-13T22:54:15.000Z	\N	262	-1	1
961	7	6	f	0	0	0	1986-08-13T11:41:34.000Z	\N	263	-1	1
961	6	6	f	0	0	0	Speech	\N	264	-1	1
961	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
961	6	6	f	0	0	0	Must autumn night find.pptx	\N	26	-1	1
961	6	6	f	0	0	0	Lauraine Zora Anjela	\N	266	-1	1
961	6	6	f	0	0	0	Always cents beaver.	\N	27	-1	3
961	6	6	f	0	0	0	Certification.	\N	28	-1	3
961	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_8mb_10mb/prayer merger.pptx	\N	272	0	1
1042	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_4mb_6mb/adopted ol intersection defence.docx	\N	272	0	1
1045	2	3	f	11	0	0	\N	\N	259	-1	1
1045	6	6	f	0	0	0	acefeda7-6ec5-492d-8634-b9845feecf11	\N	261	-1	1
1045	7	6	f	0	0	0	2047-04-09T12:05:15.000Z	\N	262	-1	1
1045	7	6	f	0	0	0	1949-04-19T09:59:12.000Z	\N	263	-1	1
1045	6	6	f	0	0	0	Science Fiction	\N	264	-1	1
1045	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1045	6	6	f	0	0	0	Saddam add edition.pptx	\N	26	-1	1
1045	6	6	f	0	0	0	Kathleen Anais Lynnelle	\N	266	-1	1
1045	6	6	f	0	0	0	Rock.	\N	27	-1	3
1045	6	6	f	0	0	0	Away.	\N	28	-1	3
1045	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_6mb_8mb/unemployment.pptx	\N	272	0	1
1050	2	3	f	66	0	0	\N	\N	259	-1	1
1050	6	6	f	0	0	0	2b26c35c-7318-48c5-85dd-b8626d22a565	\N	261	-1	1
1050	7	6	f	0	0	0	2057-04-10T01:33:00.000Z	\N	262	-1	1
1050	7	6	f	0	0	0	1923-06-06T07:48:14.000Z	\N	263	-1	1
1050	6	6	f	0	0	0	Fable	\N	264	-1	1
1050	6	6	f	0	0	0	Elsevier	\N	265	-1	1
1050	6	6	f	0	0	0	Teams total.jpg	\N	26	-1	1
1050	6	6	f	0	0	0	Loni Anais Broderic	\N	266	-1	1
1050	6	6	f	0	0	0	Order buy substantially.	\N	27	-1	3
1050	6	6	f	0	0	0	Sexually under w.	\N	28	-1	3
1049	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_500kb_2mb/decrease cafe madison rosa.pdf	\N	272	0	1
1053	2	3	f	3	0	0	\N	\N	259	-1	1
1053	6	6	f	0	0	0	b6f1c1e4-26b2-4c71-bded-7842d10e5cfc	\N	261	-1	1
1053	7	6	f	0	0	0	1977-11-26T16:24:50.000Z	\N	262	-1	1
1053	7	6	f	0	0	0	1980-07-19T09:05:54.000Z	\N	263	-1	1
964	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_2mb_4mb/mice hh lecture.pdf	\N	272	0	1
966	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_2mb_4mb/supported plymouth tomatoes central.jpg	\N	272	0	1
970	2	3	f	55	0	0	\N	\N	259	-1	1
970	6	6	f	0	0	0	4734c6d2-244b-4131-8785-92b1eeb644eb	\N	261	-1	1
970	7	6	f	0	0	0	2018-09-03T23:30:20.000Z	\N	262	-1	1
970	7	6	f	0	0	0	2011-09-27T16:06:51.000Z	\N	263	-1	1
970	6	6	f	0	0	0	Realistic Fiction	\N	264	-1	1
970	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
970	6	6	f	0	0	0	Julie birthday.jpg	\N	26	-1	1
970	6	6	f	0	0	0	Kimmie Anais Arianie	\N	266	-1	1
970	6	6	f	0	0	0	Everyone provide.	\N	27	-1	3
970	6	6	f	0	0	0	Reference once wagon. Beginning following write.	\N	28	-1	3
970	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_2mb_4mb/supported plymouth tomatoes central.jpg	\N	272	0	1
974	2	3	f	40	0	0	\N	\N	259	-1	1
974	6	6	f	0	0	0	37a54746-fc54-456c-bb78-754a526419ca	\N	261	-1	1
974	7	6	f	0	0	0	2025-06-23T05:16:34.000Z	\N	262	-1	1
974	7	6	f	0	0	0	1953-09-01T18:26:12.000Z	\N	263	-1	1
974	6	6	f	0	0	0	Action and Adventure	\N	264	-1	1
974	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
974	6	6	f	0	0	0	Low purchase call control.jpg	\N	26	-1	1
974	6	6	f	0	0	0	Mercy Anais Thorma	\N	266	-1	1
974	6	6	f	0	0	0	Eastern ws.	\N	27	-1	3
974	6	6	f	0	0	0	Tips vista forwarding contemporary.	\N	28	-1	3
974	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_4mb_6mb/safe enhancement omaha.jpg	\N	272	0	1
993	2	3	f	51	0	0	\N	\N	259	-1	1
993	6	6	f	0	0	0	2abad34f-4007-48d0-9c69-113593789f4d	\N	261	-1	1
993	7	6	f	0	0	0	2090-01-02T20:48:02.000Z	\N	262	-1	1
993	7	6	f	0	0	0	1922-10-29T17:23:24.000Z	\N	263	-1	1
993	6	6	f	0	0	0	Magical Realism	\N	264	-1	1
993	6	6	f	0	0	0	Elsevier	\N	265	-1	1
993	6	6	f	0	0	0	Largest glass europe.txt	\N	26	-1	1
993	6	6	f	0	0	0	Tabitha Anais Booze	\N	266	-1	1
993	6	6	f	0	0	0	Links win function president. Aug we johnny storage.	\N	27	-1	3
993	6	6	f	0	0	0	Of. Thread url terms.	\N	28	-1	3
993	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_4mb_6mb/aqua discounts.txt	\N	272	0	1
997	2	3	f	27	0	0	\N	\N	259	-1	1
997	6	6	f	0	0	0	cf6384bc-ada5-40eb-b47a-3e5c5a6e7035	\N	261	-1	1
997	7	6	f	0	0	0	2010-04-24T05:45:15.000Z	\N	262	-1	1
997	7	6	f	0	0	0	1964-03-14T06:05:59.000Z	\N	263	-1	1
997	6	6	f	0	0	0	Reference Books	\N	264	-1	1
997	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
997	6	6	f	0	0	0	Changed article ohio me.docx	\N	26	-1	1
997	6	6	f	0	0	0	Lavinia Berkley Angelika	\N	266	-1	1
997	6	6	f	0	0	0	Perhaps most xp history. Different direct more m.	\N	27	-1	3
997	6	6	f	0	0	0	Event agriculture.	\N	28	-1	3
997	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_0kb_500kb/circuits dash spaces workshops.docx	\N	272	0	1
1001	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_2mb_4mb/battle compliance monkey.txt	\N	272	0	1
1006	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_500kb_2mb/crew balloon s.txt	\N	272	0	1
1010	2	3	f	56	0	0	\N	\N	259	-1	1
1010	6	6	f	0	0	0	03cfb4b5-ff0b-437b-8627-969fccaaf10b	\N	261	-1	1
1010	7	6	f	0	0	0	1974-01-14T09:19:23.000Z	\N	262	-1	1
1010	7	6	f	0	0	0	2008-08-24T11:36:08.000Z	\N	263	-1	1
1010	6	6	f	0	0	0	Periodicals	\N	264	-1	1
1010	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
1010	6	6	f	0	0	0	Gone baseball this nov.pdf	\N	26	-1	1
969	2	3	f	10	0	0	\N	\N	259	-1	1
969	6	6	f	0	0	0	f5fe5fbf-c7c4-490a-abab-81738c6fe26c	\N	261	-1	1
969	7	6	f	0	0	0	2050-11-26T09:05:20.000Z	\N	262	-1	1
969	7	6	f	0	0	0	1976-05-25T02:40:19.000Z	\N	263	-1	1
969	6	6	f	0	0	0	Fairy Tale	\N	264	-1	1
969	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
969	6	6	f	0	0	0	Based students cancer california.txt	\N	26	-1	1
969	6	6	f	0	0	0	Lira Kehlani Angelina	\N	266	-1	1
969	6	6	f	0	0	0	Live data possibility.	\N	27	-1	3
969	6	6	f	0	0	0	Wed taxes map.	\N	28	-1	3
969	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_2mb_4mb/battle compliance monkey.txt	\N	272	0	1
973	2	3	f	91	0	0	\N	\N	259	-1	1
973	6	6	f	0	0	0	d5fd992a-8dc8-4a2f-9978-a6395954debc	\N	261	-1	1
973	7	6	f	0	0	0	2100-04-20T08:19:58.000Z	\N	262	-1	1
973	7	6	f	0	0	0	1937-01-12T23:53:23.000Z	\N	263	-1	1
973	6	6	f	0	0	0	Essay	\N	264	-1	1
973	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
973	6	6	f	0	0	0	Derived general stores.pdf	\N	26	-1	1
973	6	6	f	0	0	0	Clo Sunny Cheri	\N	266	-1	1
973	6	6	f	0	0	0	Whole j beauty. Full share field.	\N	27	-1	3
973	6	6	f	0	0	0	Stake.	\N	28	-1	3
973	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_500kb_2mb/decrease cafe madison rosa.pdf	\N	272	0	1
979	2	3	f	66	0	0	\N	\N	259	-1	1
979	6	6	f	0	0	0	886ab158-e0dc-44df-a61c-661fe800c770	\N	261	-1	1
979	7	6	f	0	0	0	2015-09-20T18:51:14.000Z	\N	262	-1	1
979	7	6	f	0	0	0	1973-10-30T04:21:35.000Z	\N	263	-1	1
979	6	6	f	0	0	0	Fan-Fiction	\N	264	-1	1
979	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
979	6	6	f	0	0	0	Also apr worth miss.pptx	\N	26	-1	1
979	6	6	f	0	0	0	Marthena Zora Pelagi	\N	266	-1	1
979	6	6	f	0	0	0	Involves.	\N	27	-1	3
979	6	6	f	0	0	0	Transportation observer brass.	\N	28	-1	3
998	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_2mb_4mb/supported plymouth tomatoes central.jpg	\N	272	0	1
1002	2	3	f	67	0	0	\N	\N	259	-1	1
1002	6	6	f	0	0	0	2513cf74-9773-4c1c-8b86-a22b28627c67	\N	261	-1	1
1002	7	6	f	0	0	0	2011-03-12T15:01:30.000Z	\N	262	-1	1
1002	7	6	f	0	0	0	1920-09-09T12:54:35.000Z	\N	263	-1	1
1002	6	6	f	0	0	0	Suspense/Thriller	\N	264	-1	1
1002	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1002	6	6	f	0	0	0	Team messages goal massachusetts.pdf	\N	26	-1	1
1002	6	6	f	0	0	0	Lucilia Xiomara Aloisia	\N	266	-1	1
1002	6	6	f	0	0	0	Areas user.	\N	27	-1	3
1002	6	6	f	0	0	0	Status test. Returned information engine.	\N	28	-1	3
1002	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_500kb_2mb/decrease cafe madison rosa.pdf	\N	272	0	1
1005	2	3	f	22	0	0	\N	\N	259	-1	1
1005	6	6	f	0	0	0	6b5bd402-b536-4d1e-8b24-701837779ef0	\N	261	-1	1
1005	7	6	f	0	0	0	2089-05-28T13:06:28.000Z	\N	262	-1	1
1005	7	6	f	0	0	0	1924-10-24T16:57:05.000Z	\N	263	-1	1
1005	6	6	f	0	0	0	Realistic Fiction	\N	264	-1	1
1005	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1005	6	6	f	0	0	0	Simply location try proceed.docx	\N	26	-1	1
1005	6	6	f	0	0	0	Peta Anais Arundell	\N	266	-1	1
1005	6	6	f	0	0	0	Mail scotia jun. K life.	\N	27	-1	3
1005	6	6	f	0	0	0	Each. Can min just.	\N	28	-1	3
1005	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_8mb_10mb/officers press ways bass.docx	\N	272	0	1
1009	2	3	f	37	0	0	\N	\N	259	-1	1
1009	6	6	f	0	0	0	35d03d7a-8862-4a8d-90d0-23f72e780648	\N	261	-1	1
1009	7	6	f	0	0	0	2034-11-23T17:51:55.000Z	\N	262	-1	1
972	6	6	f	0	0	0	Magical Realism	\N	264	-1	1
972	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
972	6	6	f	0	0	0	Projects how.pdf	\N	26	-1	1
972	6	6	f	0	0	0	Jonell Berkley Laden	\N	266	-1	1
972	6	6	f	0	0	0	Otherwise huntington. Online cooked strategies.	\N	27	-1	3
972	6	6	f	0	0	0	Movies lost soldier. Representative dial.	\N	28	-1	3
972	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_2mb_4mb/mice hh lecture.pdf	\N	272	0	1
976	2	3	f	92	0	0	\N	\N	259	-1	1
976	6	6	f	0	0	0	c6cf527d-ca29-481c-94b4-9a8acc3b3232	\N	261	-1	1
976	7	6	f	0	0	0	2063-11-04T07:57:10.000Z	\N	262	-1	1
976	7	6	f	0	0	0	1993-11-15T03:21:51.000Z	\N	263	-1	1
976	6	6	f	0	0	0	Mystery	\N	264	-1	1
976	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
976	6	6	f	0	0	0	Bill thinking.pptx	\N	26	-1	1
976	6	6	f	0	0	0	Madelina Anais Audrye	\N	266	-1	1
976	6	6	f	0	0	0	Complicated.	\N	27	-1	3
976	6	6	f	0	0	0	Wed based title. Have blonde.	\N	28	-1	3
976	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_2mb_4mb/nil climate enhancements.pptx	\N	272	0	1
980	2	3	f	24	0	0	\N	\N	259	-1	1
980	6	6	f	0	0	0	e29d8969-2dd2-4367-80d2-d154041cd8fc	\N	261	-1	1
980	7	6	f	0	0	0	2074-11-01T15:19:38.000Z	\N	262	-1	1
980	7	6	f	0	0	0	1973-11-27T07:54:11.000Z	\N	263	-1	1
980	6	6	f	0	0	0	Crime and Detective	\N	264	-1	1
980	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
980	6	6	f	0	0	0	Friend wc active dc.jpg	\N	26	-1	1
980	6	6	f	0	0	0	Sheena Anais Akel	\N	266	-1	1
980	6	6	f	0	0	0	Even learning home fiction.	\N	27	-1	3
980	6	6	f	0	0	0	Film groups send to. Powerpoint same.	\N	28	-1	3
980	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_500kb_2mb/sg cloth cv sanyo.jpg	\N	272	0	1
984	2	3	f	27	0	0	\N	\N	259	-1	1
984	6	6	f	0	0	0	cb7180c0-505d-400d-b89a-cd45fd0f3d6d	\N	261	-1	1
984	7	6	f	0	0	0	2050-06-24T08:34:34.000Z	\N	262	-1	1
984	7	6	f	0	0	0	1947-11-22T08:36:13.000Z	\N	263	-1	1
984	6	6	f	0	0	0	Horror	\N	264	-1	1
984	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
984	6	6	f	0	0	0	Child annual.txt	\N	26	-1	1
984	6	6	f	0	0	0	Lilli Berkley Nicole	\N	266	-1	1
984	6	6	f	0	0	0	Outcome living. Air while lesbian.	\N	27	-1	3
984	6	6	f	0	0	0	Care rate.	\N	28	-1	3
984	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_8mb_10mb/accounts sudden.txt	\N	272	0	1
988	2	3	f	80	0	0	\N	\N	259	-1	1
988	6	6	f	0	0	0	b606920b-3dfa-40de-8b6a-388891b745ba	\N	261	-1	1
988	7	6	f	0	0	0	2096-04-08T10:20:46.000Z	\N	262	-1	1
988	7	6	f	0	0	0	1973-03-18T14:16:21.000Z	\N	263	-1	1
988	6	6	f	0	0	0	Satire	\N	264	-1	1
988	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
988	6	6	f	0	0	0	Word learning.pdf	\N	26	-1	1
988	6	6	f	0	0	0	Mable Anais Bernice	\N	266	-1	1
988	6	6	f	0	0	0	Small few. Server performances nevada.	\N	27	-1	3
988	6	6	f	0	0	0	Table. Provide wales suggested postcards.	\N	28	-1	3
988	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_2mb_4mb/mice hh lecture.pdf	\N	272	0	1
992	2	3	f	94	0	0	\N	\N	259	-1	1
992	6	6	f	0	0	0	31b13300-48d0-48e1-b8c7-46dd7ab5b526	\N	261	-1	1
992	7	6	f	0	0	0	2049-10-24T05:11:45.000Z	\N	262	-1	1
992	7	6	f	0	0	0	1947-03-30T00:27:33.000Z	\N	263	-1	1
992	6	6	f	0	0	0	Science Fiction	\N	264	-1	1
992	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
992	6	6	f	0	0	0	Dvd description.jpg	\N	26	-1	1
975	7	6	f	0	0	0	1969-07-21T11:16:44.000Z	\N	263	-1	1
975	6	6	f	0	0	0	Crime and Detective	\N	264	-1	1
975	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
975	6	6	f	0	0	0	Attack end date id.pdf	\N	26	-1	1
975	6	6	f	0	0	0	Ondrea Anais Aramen	\N	266	-1	1
975	6	6	f	0	0	0	Sending.	\N	27	-1	3
975	6	6	f	0	0	0	Tax.	\N	28	-1	3
975	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_8mb_10mb/jewelry maritime.pdf	\N	272	0	1
978	2	3	f	16	0	0	\N	\N	259	-1	1
978	6	6	f	0	0	0	cb7be847-da3c-4f06-b334-19fbb06d91b4	\N	261	-1	1
978	7	6	f	0	0	0	2022-02-16T06:01:16.000Z	\N	262	-1	1
978	7	6	f	0	0	0	1948-12-30T13:44:26.000Z	\N	263	-1	1
978	6	6	f	0	0	0	Textbook	\N	264	-1	1
978	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
978	6	6	f	0	0	0	Creative sale.jpg	\N	26	-1	1
978	6	6	f	0	0	0	Cecilia Anais Beverley	\N	266	-1	1
978	6	6	f	0	0	0	Cd drink pose. Fish.	\N	27	-1	3
978	6	6	f	0	0	0	Wonderful. Fa.	\N	28	-1	3
978	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_0kb_500kb/redeem.jpg	\N	272	0	1
983	2	3	f	100	0	0	\N	\N	259	-1	1
983	6	6	f	0	0	0	13b8af35-e7d8-45d3-a6c3-df6f834a7aaf	\N	261	-1	1
983	7	6	f	0	0	0	2105-12-03T09:11:20.000Z	\N	262	-1	1
983	7	6	f	0	0	0	1914-09-26T00:57:33.000Z	\N	263	-1	1
983	6	6	f	0	0	0	Humor	\N	264	-1	1
983	6	6	f	0	0	0	Egmont	\N	265	-1	1
983	6	6	f	0	0	0	Forums orlando.pdf	\N	26	-1	1
983	6	6	f	0	0	0	Leela Anais Berhley	\N	266	-1	1
983	6	6	f	0	0	0	Church status than local.	\N	27	-1	3
983	6	6	f	0	0	0	Community.	\N	28	-1	3
983	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_8mb_10mb/jewelry maritime.pdf	\N	272	0	1
987	2	3	f	29	0	0	\N	\N	259	-1	1
987	6	6	f	0	0	0	f1516913-5125-4e70-92ef-014d8a06f0f6	\N	261	-1	1
987	7	6	f	0	0	0	2062-04-26T22:56:56.000Z	\N	262	-1	1
987	7	6	f	0	0	0	2004-02-26T20:19:09.000Z	\N	263	-1	1
987	6	6	f	0	0	0	Drama	\N	264	-1	1
987	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
987	6	6	f	0	0	0	Short economic asia being.jpg	\N	26	-1	1
987	6	6	f	0	0	0	Russel Anais Arvie	\N	266	-1	1
987	6	6	f	0	0	0	Customer jul had.	\N	27	-1	3
987	6	6	f	0	0	0	Proceeds way stick planet. Business never.	\N	28	-1	3
987	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_6mb_8mb/circular fa arthur.jpg	\N	272	0	1
991	2	3	f	16	0	0	\N	\N	259	-1	1
991	6	6	f	0	0	0	741e9b89-1f3d-466d-9e61-deba01599c48	\N	261	-1	1
991	7	6	f	0	0	0	2015-04-05T21:21:52.000Z	\N	262	-1	1
991	7	6	f	0	0	0	1917-07-27T11:03:00.000Z	\N	263	-1	1
991	6	6	f	0	0	0	Speech	\N	264	-1	1
991	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
991	6	6	f	0	0	0	Help hosted video.jpg	\N	26	-1	1
991	6	6	f	0	0	0	Lani Anais Freudberg	\N	266	-1	1
991	6	6	f	0	0	0	Garden women then.	\N	27	-1	3
991	6	6	f	0	0	0	Development.	\N	28	-1	3
991	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_0kb_500kb/redeem.jpg	\N	272	0	1
995	2	3	f	57	0	0	\N	\N	259	-1	1
995	6	6	f	0	0	0	de06a117-f7dd-4bfe-b7ba-517f4c433887	\N	261	-1	1
995	7	6	f	0	0	0	2079-04-24T00:54:35.000Z	\N	262	-1	1
995	7	6	f	0	0	0	1984-03-17T02:09:20.000Z	\N	263	-1	1
995	6	6	f	0	0	0	Fantasy	\N	264	-1	1
995	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
995	6	6	f	0	0	0	Dogs determined later rss.pdf	\N	26	-1	1
981	2	3	f	63	0	0	\N	\N	259	-1	1
981	6	6	f	0	0	0	58801c03-dad5-422a-8a22-8337e753ea1d	\N	261	-1	1
981	7	6	f	0	0	0	2014-05-20T09:10:18.000Z	\N	262	-1	1
981	7	6	f	0	0	0	1983-08-02T10:58:45.000Z	\N	263	-1	1
981	6	6	f	0	0	0	Legend	\N	264	-1	1
981	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
981	6	6	f	0	0	0	Mortgage mar.pdf	\N	26	-1	1
981	6	6	f	0	0	0	Kippy Anais Atalee	\N	266	-1	1
981	6	6	f	0	0	0	Village an.	\N	27	-1	3
981	6	6	f	0	0	0	Stories.	\N	28	-1	3
981	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
985	2	3	f	93	0	0	\N	\N	259	-1	1
985	6	6	f	0	0	0	512dc320-a263-4411-acae-8002ccf74ff6	\N	261	-1	1
985	7	6	f	0	0	0	2055-09-23T12:41:11.000Z	\N	262	-1	1
985	7	6	f	0	0	0	1910-11-29T04:18:33.000Z	\N	263	-1	1
985	6	6	f	0	0	0	Drama	\N	264	-1	1
985	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
985	6	6	f	0	0	0	Landing introduce finance.pptx	\N	26	-1	1
985	6	6	f	0	0	0	Joela Anais Lola	\N	266	-1	1
985	6	6	f	0	0	0	They investors. Had daily maker.	\N	27	-1	3
985	6	6	f	0	0	0	Wa cd points. Prices qualifying lake.	\N	28	-1	3
985	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_4mb_6mb/unified ftp.pptx	\N	272	0	1
989	2	3	f	43	0	0	\N	\N	259	-1	1
989	6	6	f	0	0	0	5c0c6f47-09a9-45f8-a4ae-0f9bb390e5a6	\N	261	-1	1
989	7	6	f	0	0	0	1992-05-23T00:26:33.000Z	\N	262	-1	1
989	7	6	f	0	0	0	1963-05-29T01:37:52.000Z	\N	263	-1	1
989	6	6	f	0	0	0	Legend	\N	264	-1	1
989	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
989	6	6	f	0	0	0	Shall improve platforms.txt	\N	26	-1	1
989	6	6	f	0	0	0	Laurice Xiomara Bultman	\N	266	-1	1
989	6	6	f	0	0	0	Submit leaders.	\N	27	-1	3
989	6	6	f	0	0	0	Properties imagine good.	\N	28	-1	3
989	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_4mb_6mb/aqua discounts.txt	\N	272	0	1
1001	2	3	f	31	0	0	\N	\N	259	-1	1
1001	6	6	f	0	0	0	002cc59e-7888-4491-8c78-089eaabd7b83	\N	261	-1	1
1001	7	6	f	0	0	0	2108-07-05T07:16:32.000Z	\N	262	-1	1
1001	7	6	f	0	0	0	1910-02-10T21:44:18.000Z	\N	263	-1	1
1001	6	6	f	0	0	0	Romance	\N	264	-1	1
1001	6	6	f	0	0	0	Random House	\N	265	-1	1
1001	6	6	f	0	0	0	Weather appear tours.txt	\N	26	-1	1
1001	6	6	f	0	0	0	Jimbo Anais Erma	\N	266	-1	1
1001	6	6	f	0	0	0	Mon log translator. Groups increase albany.	\N	27	-1	3
1001	6	6	f	0	0	0	Maker server.	\N	28	-1	3
1006	2	3	f	98	0	0	\N	\N	259	-1	1
1006	6	6	f	0	0	0	6a9144c9-ecb1-4664-acec-e434bda0f031	\N	261	-1	1
1006	7	6	f	0	0	0	2009-09-15T05:36:49.000Z	\N	262	-1	1
1006	7	6	f	0	0	0	1999-11-26T08:57:48.000Z	\N	263	-1	1
1006	6	6	f	0	0	0	Mythology	\N	264	-1	1
1006	6	6	f	0	0	0	Egmont	\N	265	-1	1
1006	6	6	f	0	0	0	Free struggle patrick.txt	\N	26	-1	1
1006	6	6	f	0	0	0	Orly Berkley Binetta	\N	266	-1	1
1006	6	6	f	0	0	0	Asin government.	\N	27	-1	3
1006	6	6	f	0	0	0	Google.	\N	28	-1	3
1031	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_6mb_8mb/crew antarctica.pdf	\N	272	0	1
1034	2	3	f	71	0	0	\N	\N	259	-1	1
1034	6	6	f	0	0	0	eea6e6f6-c4f4-4c08-a193-cf36d7a9bdd5	\N	261	-1	1
1034	7	6	f	0	0	0	2090-03-04T05:05:20.000Z	\N	262	-1	1
1034	7	6	f	0	0	0	1951-07-23T17:36:20.000Z	\N	263	-1	1
1034	6	6	f	0	0	0	Self-help Book	\N	264	-1	1
990	6	6	f	0	0	0	Neck j profile.txt	\N	26	-1	1
990	6	6	f	0	0	0	Leela Zora Elfont	\N	266	-1	1
990	6	6	f	0	0	0	Cost solutions blog than. Bingo which delivery.	\N	27	-1	3
990	6	6	f	0	0	0	Box employees. Orlando studies congress.	\N	28	-1	3
990	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_2mb_4mb/battle compliance monkey.txt	\N	272	0	1
994	2	3	f	19	0	0	\N	\N	259	-1	1
994	6	6	f	0	0	0	e499d832-f284-42de-8f6c-7b0534e7bad2	\N	261	-1	1
994	7	6	f	0	0	0	2013-08-27T13:08:47.000Z	\N	262	-1	1
994	7	6	f	0	0	0	1944-09-02T18:46:50.000Z	\N	263	-1	1
994	6	6	f	0	0	0	Suspense/Thriller	\N	264	-1	1
994	6	6	f	0	0	0	Egmont	\N	265	-1	1
994	6	6	f	0	0	0	Rights limited.docx	\N	26	-1	1
994	6	6	f	0	0	0	Noni Anais Barrada	\N	266	-1	1
994	6	6	f	0	0	0	Heard great solutions nodes. Telephone prices pages.	\N	27	-1	3
994	6	6	f	0	0	0	Uk analysts. Fellow big index premium.	\N	28	-1	3
994	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_0kb_500kb/circuits dash spaces workshops.docx	\N	272	0	1
998	2	3	f	39	0	0	\N	\N	259	-1	1
998	6	6	f	0	0	0	263da46b-021c-4065-bec8-88b70f218817	\N	261	-1	1
998	7	6	f	0	0	0	2035-07-12T20:46:01.000Z	\N	262	-1	1
998	7	6	f	0	0	0	1987-04-07T10:41:00.000Z	\N	263	-1	1
998	6	6	f	0	0	0	Poetry	\N	264	-1	1
998	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
998	6	6	f	0	0	0	Posts paper.jpg	\N	26	-1	1
998	6	6	f	0	0	0	Kathryn Anais Bolton	\N	266	-1	1
998	6	6	f	0	0	0	Especially mail brandon. Dual looking golden.	\N	27	-1	3
998	6	6	f	0	0	0	L. Level her.	\N	28	-1	3
1014	2	3	f	59	0	0	\N	\N	259	-1	1
1014	6	6	f	0	0	0	ac2f5393-a657-4c61-b5eb-1ef2b9a5f50e	\N	261	-1	1
1014	7	6	f	0	0	0	2086-11-05T22:11:29.000Z	\N	262	-1	1
1014	7	6	f	0	0	0	1913-04-18T15:35:02.000Z	\N	263	-1	1
1014	6	6	f	0	0	0	Memoir	\N	264	-1	1
1014	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1014	6	6	f	0	0	0	Newsletter aug up models.docx	\N	26	-1	1
1014	6	6	f	0	0	0	Karmen Anais Bellis	\N	266	-1	1
1014	6	6	f	0	0	0	Do.	\N	27	-1	3
1014	6	6	f	0	0	0	Stock.	\N	28	-1	3
1014	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_0kb_500kb/circuits dash spaces workshops.docx	\N	272	0	1
1017	2	3	f	59	0	0	\N	\N	259	-1	1
1017	6	6	f	0	0	0	3a0b5057-dd50-4575-a55c-03330cb4ba6a	\N	261	-1	1
1017	7	6	f	0	0	0	2048-12-03T16:30:43.000Z	\N	262	-1	1
1017	7	6	f	0	0	0	1980-06-27T08:21:40.000Z	\N	263	-1	1
1017	6	6	f	0	0	0	Narrative Nonfiction	\N	264	-1	1
1017	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1017	6	6	f	0	0	0	P welcome.pptx	\N	26	-1	1
1017	6	6	f	0	0	0	Latrina Berkley Anatole	\N	266	-1	1
1017	6	6	f	0	0	0	Pro. Subject dicke.	\N	27	-1	3
1017	6	6	f	0	0	0	Added requirements business. Pictures jeep pan.	\N	28	-1	3
1017	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_6mb_8mb/unemployment.pptx	\N	272	0	1
1021	2	3	f	17	0	0	\N	\N	259	-1	1
1021	6	6	f	0	0	0	fc222824-0e6e-4092-8dc4-c340baffed87	\N	261	-1	1
1021	7	6	f	0	0	0	2089-07-31T16:39:00.000Z	\N	262	-1	1
1021	7	6	f	0	0	0	1938-07-12T17:05:42.000Z	\N	263	-1	1
1021	6	6	f	0	0	0	Comic and Graphic Novel	\N	264	-1	1
1021	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1021	6	6	f	0	0	0	Caused philadelphia.jpg	\N	26	-1	1
1021	6	6	f	0	0	0	Justina Anais Alene	\N	266	-1	1
1021	6	6	f	0	0	0	Coalition terrace how response. Want design subject bookmarks.	\N	27	-1	3
1021	6	6	f	0	0	0	Color management too love. En specifics.	\N	28	-1	3
992	6	6	f	0	0	0	Corrie Anais Lazaruk	\N	266	-1	1
992	6	6	f	0	0	0	English red forces does. Learn.	\N	27	-1	3
992	6	6	f	0	0	0	Cool china.	\N	28	-1	3
992	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_0kb_500kb/redeem.jpg	\N	272	0	1
996	2	3	f	75	0	0	\N	\N	259	-1	1
996	6	6	f	0	0	0	28995ce3-bae2-4738-974e-074c30e86ccd	\N	261	-1	1
996	7	6	f	0	0	0	1975-03-05T13:49:56.000Z	\N	262	-1	1
996	7	6	f	0	0	0	1914-10-20T19:14:53.000Z	\N	263	-1	1
996	6	6	f	0	0	0	Periodicals	\N	264	-1	1
996	6	6	f	0	0	0	Egmont	\N	265	-1	1
996	6	6	f	0	0	0	Smooth october big.txt	\N	26	-1	1
996	6	6	f	0	0	0	Kora Anais Adi	\N	266	-1	1
996	6	6	f	0	0	0	Domains song.	\N	27	-1	3
996	6	6	f	0	0	0	Complete library. Say bill west resort.	\N	28	-1	3
996	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_6mb_8mb/terminal instructor podcasts button.txt	\N	272	0	1
999	2	3	f	20	0	0	\N	\N	259	-1	1
999	6	6	f	0	0	0	2f334bb6-9909-49a8-8b93-bd12948f7054	\N	261	-1	1
999	7	6	f	0	0	0	2036-01-20T10:32:04.000Z	\N	262	-1	1
999	7	6	f	0	0	0	1987-10-06T17:02:20.000Z	\N	263	-1	1
999	6	6	f	0	0	0	Science Fiction	\N	264	-1	1
999	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
999	6	6	f	0	0	0	Size entries high.jpg	\N	26	-1	1
999	6	6	f	0	0	0	Sallyanne Anais Basia	\N	266	-1	1
999	6	6	f	0	0	0	Thing arabia.	\N	27	-1	3
999	6	6	f	0	0	0	Complaints. Into camera.	\N	28	-1	3
999	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_4mb_6mb/safe enhancement omaha.jpg	\N	272	0	1
1003	2	3	f	6	0	0	\N	\N	259	-1	1
1003	6	6	f	0	0	0	59665e9f-c7de-4708-8d0b-d14ad2059b09	\N	261	-1	1
1003	7	6	f	0	0	0	2108-12-16T20:16:53.000Z	\N	262	-1	1
1003	7	6	f	0	0	0	1945-06-13T23:56:15.000Z	\N	263	-1	1
1003	6	6	f	0	0	0	Anthology	\N	264	-1	1
1003	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1003	6	6	f	0	0	0	Index d singapore in.jpg	\N	26	-1	1
1003	6	6	f	0	0	0	Joye Xiomara Alfi	\N	266	-1	1
1003	6	6	f	0	0	0	Copyright drives author party.	\N	27	-1	3
1003	6	6	f	0	0	0	Assistance examples sale. Sql box advertise must.	\N	28	-1	3
1003	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_500kb_2mb/sg cloth cv sanyo.jpg	\N	272	0	1
1007	2	3	f	49	0	0	\N	\N	259	-1	1
1007	6	6	f	0	0	0	1be7069b-2145-4b0e-b659-e2daff721477	\N	261	-1	1
1007	7	6	f	0	0	0	1976-11-26T11:20:00.000Z	\N	262	-1	1
1007	7	6	f	0	0	0	2012-08-28T03:34:59.000Z	\N	263	-1	1
1007	6	6	f	0	0	0	Drama	\N	264	-1	1
1007	6	6	f	0	0	0	Random House	\N	265	-1	1
1007	6	6	f	0	0	0	Recipes.txt	\N	26	-1	1
1007	6	6	f	0	0	0	Kalindi Anais Elo	\N	266	-1	1
1007	6	6	f	0	0	0	Federal forum holdings. Pet programming video.	\N	27	-1	3
1007	6	6	f	0	0	0	Goods month. Mh.	\N	28	-1	3
1007	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_8mb_10mb/accounts sudden.txt	\N	272	0	1
1011	2	3	f	46	0	0	\N	\N	259	-1	1
1011	6	6	f	0	0	0	ee70fd2b-3f5b-4c2f-82d8-bcecd24afe56	\N	261	-1	1
1011	7	6	f	0	0	0	1987-12-17T16:50:03.000Z	\N	262	-1	1
1011	7	6	f	0	0	0	1949-09-18T00:00:16.000Z	\N	263	-1	1
1011	6	6	f	0	0	0	Realistic Fiction	\N	264	-1	1
1011	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
1011	6	6	f	0	0	0	Jobs include order.jpg	\N	26	-1	1
1011	6	6	f	0	0	0	Griffith Anais Austine	\N	266	-1	1
1011	6	6	f	0	0	0	Pleasure.	\N	27	-1	3
1011	6	6	f	0	0	0	Via cheese minutes. Students population well.	\N	28	-1	3
995	6	6	f	0	0	0	Perle Anais Attalanta	\N	266	-1	1
995	6	6	f	0	0	0	Choose musicians.	\N	27	-1	3
995	6	6	f	0	0	0	Year.	\N	28	-1	3
995	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_8mb_10mb/jewelry maritime.pdf	\N	272	0	1
1000	2	3	f	59	0	0	\N	\N	259	-1	1
1000	6	6	f	0	0	0	038527b4-bdef-47fd-9134-7dcc6026c34a	\N	261	-1	1
1000	7	6	f	0	0	0	2101-02-05T20:29:42.000Z	\N	262	-1	1
1000	7	6	f	0	0	0	1929-10-29T14:49:37.000Z	\N	263	-1	1
1000	6	6	f	0	0	0	Poetry	\N	264	-1	1
1000	6	6	f	0	0	0	Elsevier	\N	265	-1	1
1000	6	6	f	0	0	0	Air included.pdf	\N	26	-1	1
1000	6	6	f	0	0	0	Joannes Anais Aara	\N	266	-1	1
1000	6	6	f	0	0	0	Planet this.	\N	27	-1	3
1000	6	6	f	0	0	0	Defeat hand.	\N	28	-1	3
1000	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
1004	2	3	f	82	0	0	\N	\N	259	-1	1
1004	6	6	f	0	0	0	9975dca3-596e-4ba6-bf29-2aea34959562	\N	261	-1	1
1004	7	6	f	0	0	0	2047-12-06T22:57:10.000Z	\N	262	-1	1
1004	7	6	f	0	0	0	1971-12-19T09:40:18.000Z	\N	263	-1	1
1004	6	6	f	0	0	0	Biography/Autobiography	\N	264	-1	1
1004	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1004	6	6	f	0	0	0	Re dvd yes.pdf	\N	26	-1	1
1004	6	6	f	0	0	0	Maggy Kehlani Abbie	\N	266	-1	1
1004	6	6	f	0	0	0	Treasure security legitimate projectors. Corporate babes.	\N	27	-1	3
1004	6	6	f	0	0	0	Big edit. Cheap invited thai.	\N	28	-1	3
1004	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_6mb_8mb/crew antarctica.pdf	\N	272	0	1
1008	2	3	f	71	0	0	\N	\N	259	-1	1
1008	6	6	f	0	0	0	1278be8e-1d4b-49c4-a6f3-41373bafa9c2	\N	261	-1	1
1008	7	6	f	0	0	0	2081-11-13T15:26:30.000Z	\N	262	-1	1
1008	7	6	f	0	0	0	1965-08-24T11:15:07.000Z	\N	263	-1	1
1008	6	6	f	0	0	0	Reference Books	\N	264	-1	1
1008	6	6	f	0	0	0	Elsevier	\N	265	-1	1
1008	6	6	f	0	0	0	Comparable how bonus.pptx	\N	26	-1	1
1008	6	6	f	0	0	0	Lynnell Promise Ailis	\N	266	-1	1
1008	6	6	f	0	0	0	Notice rural vacation zealand.	\N	27	-1	3
1008	6	6	f	0	0	0	Relationship. Computer preston.	\N	28	-1	3
1008	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_6mb_8mb/unemployment.pptx	\N	272	0	1
1012	2	3	f	1	0	0	\N	\N	259	-1	1
1012	6	6	f	0	0	0	a626a107-2d6c-43f9-8237-bfa1a6ed9d74	\N	261	-1	1
1012	7	6	f	0	0	0	2020-05-21T23:59:53.000Z	\N	262	-1	1
1012	7	6	f	0	0	0	1922-11-01T04:08:57.000Z	\N	263	-1	1
1012	6	6	f	0	0	0	Magical Realism	\N	264	-1	1
1012	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
1012	6	6	f	0	0	0	Addressing person.pptx	\N	26	-1	1
1012	6	6	f	0	0	0	Madonna Anais Audley	\N	266	-1	1
1012	6	6	f	0	0	0	Giving communist finished software.	\N	27	-1	3
1012	6	6	f	0	0	0	Try cycling archive greatly.	\N	28	-1	3
1012	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_0kb_500kb/monitoring zip sudan stan.pptx	\N	272	0	1
1016	2	3	f	61	0	0	\N	\N	259	-1	1
1016	6	6	f	0	0	0	85366da4-b414-479a-adb9-0bc8de1df6a7	\N	261	-1	1
1016	7	6	f	0	0	0	2018-09-23T17:35:48.000Z	\N	262	-1	1
1016	7	6	f	0	0	0	1931-07-24T11:08:04.000Z	\N	263	-1	1
1016	6	6	f	0	0	0	Essay	\N	264	-1	1
1016	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1016	6	6	f	0	0	0	Program co utility examination.pdf	\N	26	-1	1
1016	6	6	f	0	0	0	Joline Berkley Cavan	\N	266	-1	1
1016	6	6	f	0	0	0	Downloads.	\N	27	-1	3
1016	6	6	f	0	0	0	Sign technique car exists.	\N	28	-1	3
1010	6	6	f	0	0	0	Shaun Berkley Laughton	\N	266	-1	1
1010	6	6	f	0	0	0	Listing photo browse these. Offered air bikes variance.	\N	27	-1	3
1010	6	6	f	0	0	0	Small name penalties social.	\N	28	-1	3
1010	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_500kb_2mb/decrease cafe madison rosa.pdf	\N	272	0	1
1013	2	3	f	66	0	0	\N	\N	259	-1	1
1013	6	6	f	0	0	0	a9d2fdb1-81e7-4975-8b6b-98913500892f	\N	261	-1	1
1013	7	6	f	0	0	0	1974-08-10T19:15:18.000Z	\N	262	-1	1
1013	7	6	f	0	0	0	1983-07-28T09:44:09.000Z	\N	263	-1	1
1013	6	6	f	0	0	0	Horror	\N	264	-1	1
1013	6	6	f	0	0	0	Elsevier	\N	265	-1	1
1013	6	6	f	0	0	0	Discount dr gear.docx	\N	26	-1	1
1013	6	6	f	0	0	0	Junia Promise Bernelle	\N	266	-1	1
1013	6	6	f	0	0	0	Few points.	\N	27	-1	3
1013	6	6	f	0	0	0	Wisdom.	\N	28	-1	3
1013	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_templates/ripe restore.docx	\N	272	0	1
1027	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
1031	2	3	f	49	0	0	\N	\N	259	-1	1
1031	6	6	f	0	0	0	232ffef2-f81a-44c3-a30d-27093ca0d5e3	\N	261	-1	1
1031	7	6	f	0	0	0	2080-04-30T14:54:30.000Z	\N	262	-1	1
1031	7	6	f	0	0	0	1982-10-23T10:47:08.000Z	\N	263	-1	1
1031	6	6	f	0	0	0	Poetry	\N	264	-1	1
1031	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
1031	6	6	f	0	0	0	Direct reports keep.pdf	\N	26	-1	1
1031	6	6	f	0	0	0	Olympia Anais Afton	\N	266	-1	1
1031	6	6	f	0	0	0	Profiles pros judge avi.	\N	27	-1	3
1031	6	6	f	0	0	0	Really vision delivery.	\N	28	-1	3
1040	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_500kb_2mb/decrease cafe madison rosa.pdf	\N	272	0	1
1043	2	3	f	100	0	0	\N	\N	259	-1	1
1043	6	6	f	0	0	0	338f81c6-fd95-462d-8bca-b5455764c845	\N	261	-1	1
1043	7	6	f	0	0	0	2066-11-20T10:26:15.000Z	\N	262	-1	1
1043	7	6	f	0	0	0	1933-06-22T21:15:40.000Z	\N	263	-1	1
1043	6	6	f	0	0	0	Fable	\N	264	-1	1
1043	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
1043	6	6	f	0	0	0	Able management eyes audience.pdf	\N	26	-1	1
1043	6	6	f	0	0	0	Lydie Anais Blatt	\N	266	-1	1
1043	6	6	f	0	0	0	Security lookup american. Theory wish memorial motels.	\N	27	-1	3
1043	6	6	f	0	0	0	Outsourcing. Management low.	\N	28	-1	3
1043	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_0kb_500kb/specialty mandatory lists examined.pdf	\N	272	0	1
1047	2	3	f	37	0	0	\N	\N	259	-1	1
1047	6	6	f	0	0	0	32c1cd91-4ccf-4474-8e8a-0551473a9bb6	\N	261	-1	1
1047	7	6	f	0	0	0	2001-03-31T19:03:41.000Z	\N	262	-1	1
1047	7	6	f	0	0	0	2017-01-15T19:40:37.000Z	\N	263	-1	1
1047	6	6	f	0	0	0	Horror	\N	264	-1	1
1047	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
1047	6	6	f	0	0	0	Downloads version.pptx	\N	26	-1	1
1047	6	6	f	0	0	0	Romona Anais Alika	\N	266	-1	1
1047	6	6	f	0	0	0	Truth jan france.	\N	27	-1	3
1047	6	6	f	0	0	0	Unique underlying permalink babe.	\N	28	-1	3
1048	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_8mb_10mb/officers press ways bass.docx	\N	272	0	1
1051	2	3	f	56	0	0	\N	\N	259	-1	1
1051	6	6	f	0	0	0	03b594c8-9f99-49e5-a69c-5b6559b31c80	\N	261	-1	1
1051	7	6	f	0	0	0	2019-07-29T07:55:14.000Z	\N	262	-1	1
1051	7	6	f	0	0	0	1937-10-27T18:19:26.000Z	\N	263	-1	1
1051	6	6	f	0	0	0	Magical Realism	\N	264	-1	1
1051	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1051	6	6	f	0	0	0	Still previous springfield.txt	\N	26	-1	1
1051	6	6	f	0	0	0	Koren Anais Cuthbertson	\N	266	-1	1
1009	7	6	f	0	0	0	1919-11-23T04:06:04.000Z	\N	263	-1	1
1009	6	6	f	0	0	0	Speech	\N	264	-1	1
1009	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1009	6	6	f	0	0	0	Acceptance visit coral.pdf	\N	26	-1	1
1009	6	6	f	0	0	0	Lenee Anais Collis	\N	266	-1	1
1009	6	6	f	0	0	0	Park own.	\N	27	-1	3
1009	6	6	f	0	0	0	Travel search also. Update open radar cruz.	\N	28	-1	3
1009	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_500kb_2mb/decrease cafe madison rosa.pdf	\N	272	0	1
1062	2	3	f	17	0	0	\N	\N	259	-1	1
1062	6	6	f	0	0	0	f505757f-926b-485c-9638-78b342910f63	\N	261	-1	1
1062	7	6	f	0	0	0	2090-04-15T01:35:31.000Z	\N	262	-1	1
1062	7	6	f	0	0	0	1938-09-16T14:20:56.000Z	\N	263	-1	1
1062	6	6	f	0	0	0	Realistic Fiction	\N	264	-1	1
1062	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1062	6	6	f	0	0	0	Printers row chapel cbs.docx	\N	26	-1	1
1062	6	6	f	0	0	0	Jotham Anais Artamas	\N	266	-1	1
1062	6	6	f	0	0	0	England.	\N	27	-1	3
1062	6	6	f	0	0	0	Speech. Sell moscow cats states.	\N	28	-1	3
1059	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_0kb_500kb/basketball.txt	\N	272	0	1
1064	2	3	f	83	0	0	\N	\N	259	-1	1
1064	6	6	f	0	0	0	abcc4225-1029-4fee-a197-c104221b81b6	\N	261	-1	1
1064	7	6	f	0	0	0	2055-04-17T06:32:10.000Z	\N	262	-1	1
1064	7	6	f	0	0	0	2010-08-23T00:23:54.000Z	\N	263	-1	1
1064	6	6	f	0	0	0	Satire	\N	264	-1	1
1064	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1064	6	6	f	0	0	0	Wicked.jpg	\N	26	-1	1
1064	6	6	f	0	0	0	Bartholomeo Anais Andree	\N	266	-1	1
1064	6	6	f	0	0	0	Made. California disney windows.	\N	27	-1	3
1064	6	6	f	0	0	0	Violence a jones your. Computing paul error.	\N	28	-1	3
1064	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_0kb_500kb/redeem.jpg	\N	272	0	1
1068	2	3	f	30	0	0	\N	\N	259	-1	1
1068	6	6	f	0	0	0	70835d59-7097-463a-82ea-8f55488fcf4c	\N	261	-1	1
1068	7	6	f	0	0	0	2018-04-21T20:18:23.000Z	\N	262	-1	1
1068	7	6	f	0	0	0	2015-08-09T14:10:02.000Z	\N	263	-1	1
1068	6	6	f	0	0	0	Mythology	\N	264	-1	1
1068	6	6	f	0	0	0	Random House	\N	265	-1	1
1068	6	6	f	0	0	0	Real copyright.pdf	\N	26	-1	1
1068	6	6	f	0	0	0	Latia Anais Charmine	\N	266	-1	1
1068	6	6	f	0	0	0	Ladies made and. Driver.	\N	27	-1	3
1068	6	6	f	0	0	0	Sony.	\N	28	-1	3
1067	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_templates/ripe restore.docx	\N	272	0	1
1073	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_2mb_4mb/mice hh lecture.pdf	\N	272	0	1
1075	2	3	f	20	0	0	\N	\N	259	-1	1
1075	6	6	f	0	0	0	e0e137e6-fce0-4971-aa96-ed87e5795133	\N	261	-1	1
1075	7	6	f	0	0	0	2105-06-29T08:29:18.000Z	\N	262	-1	1
1075	7	6	f	0	0	0	1999-03-15T21:21:47.000Z	\N	263	-1	1
1075	6	6	f	0	0	0	Magical Realism	\N	264	-1	1
1075	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1075	6	6	f	0	0	0	Url singh.pptx	\N	26	-1	1
1075	6	6	f	0	0	0	Ronni Anais Marcie	\N	266	-1	1
1075	6	6	f	0	0	0	Head bin location.	\N	27	-1	3
1075	6	6	f	0	0	0	Title discharge mail. Consolidated.	\N	28	-1	3
1075	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_2mb_4mb/nil climate enhancements.pptx	\N	272	0	1
1079	2	3	f	79	0	0	\N	\N	259	-1	1
1079	6	6	f	0	0	0	8b06b510-e24e-409f-977b-48b06031b445	\N	261	-1	1
1079	7	6	f	0	0	0	2092-03-05T22:35:57.000Z	\N	262	-1	1
1079	7	6	f	0	0	0	1965-08-25T19:10:21.000Z	\N	263	-1	1
1079	6	6	f	0	0	0	Fairy Tale	\N	264	-1	1
1011	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_4mb_6mb/safe enhancement omaha.jpg	\N	272	0	1
1015	2	3	f	41	0	0	\N	\N	259	-1	1
1015	6	6	f	0	0	0	11fed7dd-ea3d-4339-a154-3e2dc0b4db5d	\N	261	-1	1
1015	7	6	f	0	0	0	1996-09-03T03:28:15.000Z	\N	262	-1	1
1015	7	6	f	0	0	0	2009-05-14T23:53:19.000Z	\N	263	-1	1
1015	6	6	f	0	0	0	Narrative Nonfiction	\N	264	-1	1
1015	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
1015	6	6	f	0	0	0	Mac floppy demonstration.jpg	\N	26	-1	1
1015	6	6	f	0	0	0	Margy Anais Ammamaria	\N	266	-1	1
1015	6	6	f	0	0	0	Systems search accept. Museums scenarios.	\N	27	-1	3
1015	6	6	f	0	0	0	Garden vegas.	\N	28	-1	3
1015	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_0kb_500kb/redeem.jpg	\N	272	0	1
1019	2	3	f	57	0	0	\N	\N	259	-1	1
1019	6	6	f	0	0	0	774b51ae-f169-4b62-bd93-4bdcaefda687	\N	261	-1	1
1019	7	6	f	0	0	0	2036-01-25T15:26:51.000Z	\N	262	-1	1
1019	7	6	f	0	0	0	1972-06-19T20:55:24.000Z	\N	263	-1	1
1019	6	6	f	0	0	0	Essay	\N	264	-1	1
1019	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
1019	6	6	f	0	0	0	There sort.docx	\N	26	-1	1
1019	6	6	f	0	0	0	Juliana Anais Atkins	\N	266	-1	1
1019	6	6	f	0	0	0	Today various image calls. Speak due.	\N	27	-1	3
1019	6	6	f	0	0	0	Mem ca tools.	\N	28	-1	3
1019	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_2mb_4mb/tired.docx	\N	272	0	1
1023	2	3	f	51	0	0	\N	\N	259	-1	1
1023	6	6	f	0	0	0	3837eb85-41b8-4eaf-b25f-955446d4f450	\N	261	-1	1
1023	7	6	f	0	0	0	2026-10-29T09:51:15.000Z	\N	262	-1	1
1023	7	6	f	0	0	0	1925-02-28T11:10:12.000Z	\N	263	-1	1
1023	6	6	f	0	0	0	Classic	\N	264	-1	1
1023	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1023	6	6	f	0	0	0	Line attendance.txt	\N	26	-1	1
1023	6	6	f	0	0	0	Lizette Anais Andrus	\N	266	-1	1
1023	6	6	f	0	0	0	Quick hours join. Happiness.	\N	27	-1	3
1023	6	6	f	0	0	0	Dress pass membrane topics.	\N	28	-1	3
1023	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_2mb_4mb/battle compliance monkey.txt	\N	272	0	1
1027	2	3	f	70	0	0	\N	\N	259	-1	1
1027	6	6	f	0	0	0	02a23c24-4158-4e0f-a0d3-27daabe17717	\N	261	-1	1
1027	7	6	f	0	0	0	2099-04-30T16:34:16.000Z	\N	262	-1	1
1027	7	6	f	0	0	0	1984-02-20T17:59:15.000Z	\N	263	-1	1
1027	6	6	f	0	0	0	Fairy Tale	\N	264	-1	1
1027	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
1027	6	6	f	0	0	0	Going fax.pdf	\N	26	-1	1
1027	6	6	f	0	0	0	Leena Anais Brest	\N	266	-1	1
1027	6	6	f	0	0	0	Software chain ordinary after. Window five estimates.	\N	27	-1	3
1027	6	6	f	0	0	0	Amsterdam value faculty whatever. Tickets education brain.	\N	28	-1	3
1042	2	3	f	14	0	0	\N	\N	259	-1	1
1042	6	6	f	0	0	0	fba13384-e487-49a0-a63e-3154735fb37e	\N	261	-1	1
1042	7	6	f	0	0	0	2092-05-26T03:12:29.000Z	\N	262	-1	1
1042	7	6	f	0	0	0	1981-10-01T13:18:37.000Z	\N	263	-1	1
1042	6	6	f	0	0	0	Speech	\N	264	-1	1
1042	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
1042	6	6	f	0	0	0	Mail keep product.docx	\N	26	-1	1
1042	6	6	f	0	0	0	Madelene Promise Adolf	\N	266	-1	1
1042	6	6	f	0	0	0	Business below membership.	\N	27	-1	3
1042	6	6	f	0	0	0	Big mid record inspired.	\N	28	-1	3
1050	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_500kb_2mb/sg cloth cv sanyo.jpg	\N	272	0	1
1054	2	3	f	65	0	0	\N	\N	259	-1	1
1054	6	6	f	0	0	0	35002d52-88ed-410c-8620-758b1feef6a6	\N	261	-1	1
1054	7	6	f	0	0	0	2096-05-19T10:51:18.000Z	\N	262	-1	1
1018	2	3	f	82	0	0	\N	\N	259	-1	1
1018	6	6	f	0	0	0	eaa3756f-336e-4a3c-aed8-674a0eca16ad	\N	261	-1	1
1018	7	6	f	0	0	0	2022-01-15T22:28:04.000Z	\N	262	-1	1
1018	7	6	f	0	0	0	1993-04-18T19:30:46.000Z	\N	263	-1	1
1018	6	6	f	0	0	0	Comic and Graphic Novel	\N	264	-1	1
1018	6	6	f	0	0	0	Elsevier	\N	265	-1	1
1018	6	6	f	0	0	0	Way since jobs.pptx	\N	26	-1	1
1018	6	6	f	0	0	0	Daisie Anais Ameline	\N	266	-1	1
1018	6	6	f	0	0	0	Leading sections.	\N	27	-1	3
1018	6	6	f	0	0	0	Movies.	\N	28	-1	3
1018	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_4mb_6mb/unified ftp.pptx	\N	272	0	1
1022	2	3	f	84	0	0	\N	\N	259	-1	1
1022	6	6	f	0	0	0	70e14a45-f9f6-4b62-acdd-e15f4a061aba	\N	261	-1	1
1022	7	6	f	0	0	0	2009-08-03T17:21:58.000Z	\N	262	-1	1
1022	7	6	f	0	0	0	2018-12-23T00:35:36.000Z	\N	263	-1	1
1022	6	6	f	0	0	0	Anthology	\N	264	-1	1
1022	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1022	6	6	f	0	0	0	Optimization file deal.txt	\N	26	-1	1
1022	6	6	f	0	0	0	Juana Anais Etom	\N	266	-1	1
1022	6	6	f	0	0	0	Them florida maryland. Paul food nathan falls.	\N	27	-1	3
1022	6	6	f	0	0	0	Book tagged.	\N	28	-1	3
1022	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_2mb_4mb/battle compliance monkey.txt	\N	272	0	1
1026	2	3	f	32	0	0	\N	\N	259	-1	1
1026	6	6	f	0	0	0	86c1ce64-b3b5-40ed-a742-e335f70fc42d	\N	261	-1	1
1026	7	6	f	0	0	0	2044-09-20T05:29:00.000Z	\N	262	-1	1
1026	7	6	f	0	0	0	1916-08-03T08:20:56.000Z	\N	263	-1	1
1026	6	6	f	0	0	0	Suspense/Thriller	\N	264	-1	1
1026	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1026	6	6	f	0	0	0	A lyrics were s.docx	\N	26	-1	1
1026	6	6	f	0	0	0	Lynea Anais Adlei	\N	266	-1	1
1026	6	6	f	0	0	0	Big event career policies. Notion kde radio.	\N	27	-1	3
1026	6	6	f	0	0	0	Availability list.	\N	28	-1	3
1026	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_0kb_500kb/circuits dash spaces workshops.docx	\N	272	0	1
1030	2	3	f	51	0	0	\N	\N	259	-1	1
1030	6	6	f	0	0	0	96c2983e-2759-49a3-ab54-e3829b6e710c	\N	261	-1	1
1030	7	6	f	0	0	0	2005-12-26T10:47:11.000Z	\N	262	-1	1
1030	7	6	f	0	0	0	1959-12-26T20:45:13.000Z	\N	263	-1	1
1030	6	6	f	0	0	0	Historical Fiction	\N	264	-1	1
1030	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1030	6	6	f	0	0	0	Be taken.docx	\N	26	-1	1
1030	6	6	f	0	0	0	Bunni Anais Aribold	\N	266	-1	1
1030	6	6	f	0	0	0	Questionnaire.	\N	27	-1	3
1030	6	6	f	0	0	0	Part direct. Of yahoo.	\N	28	-1	3
1030	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_6mb_8mb/gotten hebrew.docx	\N	272	0	1
1035	2	3	f	16	0	0	\N	\N	259	-1	1
1035	6	6	f	0	0	0	f2438083-dcf6-47ab-8cb8-19cb57e7a1c5	\N	261	-1	1
1035	7	6	f	0	0	0	2058-11-04T22:40:59.000Z	\N	262	-1	1
1035	7	6	f	0	0	0	1930-06-30T23:21:37.000Z	\N	263	-1	1
1035	6	6	f	0	0	0	Reference Books	\N	264	-1	1
1035	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1035	6	6	f	0	0	0	Call lines years states.pptx	\N	26	-1	1
1035	6	6	f	0	0	0	Jodee Anais Eberto	\N	266	-1	1
1035	6	6	f	0	0	0	Bald sell their. Ec re update rating.	\N	27	-1	3
1035	6	6	f	0	0	0	Permanent maternity holidays haven. Control.	\N	28	-1	3
1035	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_6mb_8mb/unemployment.pptx	\N	272	0	1
1038	2	3	f	31	0	0	\N	\N	259	-1	1
1038	6	6	f	0	0	0	b3d0a7eb-4f11-4f41-ad9d-6d0d89c48028	\N	261	-1	1
1038	7	6	f	0	0	0	2056-02-11T16:05:50.000Z	\N	262	-1	1
1016	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
1020	2	3	f	35	0	0	\N	\N	259	-1	1
1020	6	6	f	0	0	0	5344a609-80ce-4a7b-a98f-3af8ed3f173f	\N	261	-1	1
1020	7	6	f	0	0	0	2045-01-24T13:44:16.000Z	\N	262	-1	1
1020	7	6	f	0	0	0	1964-09-15T00:21:47.000Z	\N	263	-1	1
1020	6	6	f	0	0	0	Memoir	\N	264	-1	1
1020	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1020	6	6	f	0	0	0	Party ss world partner.docx	\N	26	-1	1
1020	6	6	f	0	0	0	Lanie Anais Sandie	\N	266	-1	1
1020	6	6	f	0	0	0	Eminem some agreement.	\N	27	-1	3
1020	6	6	f	0	0	0	Comment represent atlas. Vs making day.	\N	28	-1	3
1020	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_4mb_6mb/adopted ol intersection defence.docx	\N	272	0	1
1024	2	3	f	8	0	0	\N	\N	259	-1	1
1024	6	6	f	0	0	0	f1d020ed-3887-4e61-ac90-502ba0e4a724	\N	261	-1	1
1024	7	6	f	0	0	0	2079-05-27T21:29:51.000Z	\N	262	-1	1
1024	7	6	f	0	0	0	2000-05-20T12:44:14.000Z	\N	263	-1	1
1024	6	6	f	0	0	0	Speech	\N	264	-1	1
1024	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1024	6	6	f	0	0	0	Family stats.jpg	\N	26	-1	1
1024	6	6	f	0	0	0	Johnette Anais Jason	\N	266	-1	1
1024	6	6	f	0	0	0	Hart level.	\N	27	-1	3
1024	6	6	f	0	0	0	Their obtained.	\N	28	-1	3
1024	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_6mb_8mb/circular fa arthur.jpg	\N	272	0	1
1028	2	3	f	56	0	0	\N	\N	259	-1	1
1028	6	6	f	0	0	0	df99e99b-45ca-4acf-aa4c-bc0a5c662486	\N	261	-1	1
1028	7	6	f	0	0	0	1982-07-11T05:49:06.000Z	\N	262	-1	1
1028	7	6	f	0	0	0	1923-05-23T06:41:36.000Z	\N	263	-1	1
1028	6	6	f	0	0	0	Fairy Tale	\N	264	-1	1
1028	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1028	6	6	f	0	0	0	Med.pdf	\N	26	-1	1
1028	6	6	f	0	0	0	Michele Berkley Belle	\N	266	-1	1
1028	6	6	f	0	0	0	Long.	\N	27	-1	3
1028	6	6	f	0	0	0	Pi guitar friends. Higher cycles.	\N	28	-1	3
1028	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_2mb_4mb/mice hh lecture.pdf	\N	272	0	1
1032	2	3	f	41	0	0	\N	\N	259	-1	1
1032	6	6	f	0	0	0	25e2fad1-bcfe-4eeb-b70e-6ecfa09484e4	\N	261	-1	1
1032	7	6	f	0	0	0	2102-01-01T17:23:25.000Z	\N	262	-1	1
1032	7	6	f	0	0	0	1921-02-09T15:06:16.000Z	\N	263	-1	1
1032	6	6	f	0	0	0	Essay	\N	264	-1	1
1032	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1032	6	6	f	0	0	0	On just keywords.docx	\N	26	-1	1
1032	6	6	f	0	0	0	Stephani Zora Azarria	\N	266	-1	1
1032	6	6	f	0	0	0	Material introduced. Local.	\N	27	-1	3
1032	6	6	f	0	0	0	Using hosting. Invoice a chapter aside.	\N	28	-1	3
1032	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_4mb_6mb/adopted ol intersection defence.docx	\N	272	0	1
1036	2	3	f	4	0	0	\N	\N	259	-1	1
1036	6	6	f	0	0	0	ea7955d3-775d-4459-915f-dd96fd1aaa16	\N	261	-1	1
1036	7	6	f	0	0	0	2108-07-09T15:44:17.000Z	\N	262	-1	1
1036	7	6	f	0	0	0	1926-10-17T10:47:47.000Z	\N	263	-1	1
1036	6	6	f	0	0	0	Speech	\N	264	-1	1
1036	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1036	6	6	f	0	0	0	Between information kids.docx	\N	26	-1	1
1036	6	6	f	0	0	0	Ferdinand Anais Brinna	\N	266	-1	1
1036	6	6	f	0	0	0	Picture miles. Music sale day desk.	\N	27	-1	3
1036	6	6	f	0	0	0	Next three. Loan help loan.	\N	28	-1	3
1036	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_0kb_500kb/circuits dash spaces workshops.docx	\N	272	0	1
1040	2	3	f	82	0	0	\N	\N	259	-1	1
1040	6	6	f	0	0	0	89fe4c1b-85a0-4c1e-925d-7dcee1b37341	\N	261	-1	1
1021	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_8mb_10mb/go person ef.jpg	\N	272	0	1
1025	2	3	f	25	0	0	\N	\N	259	-1	1
1025	6	6	f	0	0	0	0ce648f4-9eb1-4c97-8040-a9ccb34aa328	\N	261	-1	1
1025	7	6	f	0	0	0	2018-03-18T15:53:19.000Z	\N	262	-1	1
1025	7	6	f	0	0	0	1963-03-05T05:10:08.000Z	\N	263	-1	1
1025	6	6	f	0	0	0	Humor	\N	264	-1	1
1025	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
1025	6	6	f	0	0	0	St saskatchewan shows.jpg	\N	26	-1	1
1025	6	6	f	0	0	0	Karolina Anais Cecilius	\N	266	-1	1
1025	6	6	f	0	0	0	J testing issues.	\N	27	-1	3
1025	6	6	f	0	0	0	Management currently. Street.	\N	28	-1	3
1025	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_2mb_4mb/supported plymouth tomatoes central.jpg	\N	272	0	1
1029	2	3	f	51	0	0	\N	\N	259	-1	1
1029	6	6	f	0	0	0	3f0cb0ab-6746-4609-abe8-78f2d65d8d90	\N	261	-1	1
1029	7	6	f	0	0	0	2058-11-25T17:36:01.000Z	\N	262	-1	1
1029	7	6	f	0	0	0	2015-01-31T12:36:54.000Z	\N	263	-1	1
1029	6	6	f	0	0	0	Anthology	\N	264	-1	1
1029	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1029	6	6	f	0	0	0	Account profile cars.pdf	\N	26	-1	1
1029	6	6	f	0	0	0	Lucienne Zora Allanson	\N	266	-1	1
1029	6	6	f	0	0	0	One might army forty.	\N	27	-1	3
1029	6	6	f	0	0	0	She czech.	\N	28	-1	3
1029	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
1033	2	3	f	50	0	0	\N	\N	259	-1	1
1033	6	6	f	0	0	0	f7184b06-3543-4781-9c5a-893f65de3677	\N	261	-1	1
1033	7	6	f	0	0	0	2017-07-06T15:56:01.000Z	\N	262	-1	1
1033	7	6	f	0	0	0	1975-03-24T15:03:16.000Z	\N	263	-1	1
1033	6	6	f	0	0	0	Essay	\N	264	-1	1
1033	6	6	f	0	0	0	Elsevier	\N	265	-1	1
1033	6	6	f	0	0	0	Unless source ok drive.pdf	\N	26	-1	1
1033	6	6	f	0	0	0	Quent Anais DeeAnn	\N	266	-1	1
1033	6	6	f	0	0	0	Warning photography home golden.	\N	27	-1	3
1033	6	6	f	0	0	0	Cell signed expect.	\N	28	-1	3
1033	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_500kb_2mb/decrease cafe madison rosa.pdf	\N	272	0	1
1037	2	3	f	9	0	0	\N	\N	259	-1	1
1037	6	6	f	0	0	0	58e7866e-8287-4d22-8b17-4167fa13fc4c	\N	261	-1	1
1037	7	6	f	0	0	0	1991-10-23T12:44:25.000Z	\N	262	-1	1
1037	7	6	f	0	0	0	2004-06-24T01:27:43.000Z	\N	263	-1	1
1037	6	6	f	0	0	0	Action and Adventure	\N	264	-1	1
1037	6	6	f	0	0	0	Elsevier	\N	265	-1	1
1037	6	6	f	0	0	0	Care might.txt	\N	26	-1	1
1037	6	6	f	0	0	0	Lizzy Zora Boak	\N	266	-1	1
1037	6	6	f	0	0	0	Size th topics. Stuff directory iceland hockey.	\N	27	-1	3
1037	6	6	f	0	0	0	Listing.	\N	28	-1	3
1037	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_6mb_8mb/terminal instructor podcasts button.txt	\N	272	0	1
1041	2	3	f	96	0	0	\N	\N	259	-1	1
1041	6	6	f	0	0	0	794b53a8-0fc5-4495-b41d-b1175b166e79	\N	261	-1	1
1041	7	6	f	0	0	0	1992-09-12T15:33:15.000Z	\N	262	-1	1
1041	7	6	f	0	0	0	1949-08-07T17:20:23.000Z	\N	263	-1	1
1041	6	6	f	0	0	0	Fan-Fiction	\N	264	-1	1
1041	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1041	6	6	f	0	0	0	General performance plan.pdf	\N	26	-1	1
1041	6	6	f	0	0	0	Abelard Anais Wolfy	\N	266	-1	1
1041	6	6	f	0	0	0	Holds argument purchase assured.	\N	27	-1	3
1041	6	6	f	0	0	0	Malta unique. Did.	\N	28	-1	3
1041	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
1046	2	3	f	31	0	0	\N	\N	259	-1	1
1034	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1034	6	6	f	0	0	0	Working house.jpg	\N	26	-1	1
1034	6	6	f	0	0	0	Vonny Anais Celestyna	\N	266	-1	1
1034	6	6	f	0	0	0	Bear baby job.	\N	27	-1	3
1034	6	6	f	0	0	0	Japanese contact. Pour travel amount signature.	\N	28	-1	3
1034	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_2mb_4mb/supported plymouth tomatoes central.jpg	\N	272	0	1
1073	2	3	f	53	0	0	\N	\N	259	-1	1
1073	6	6	f	0	0	0	095b676a-ab6f-4821-8512-a9030eddf5f8	\N	261	-1	1
1073	7	6	f	0	0	0	1984-02-02T22:18:18.000Z	\N	262	-1	1
1073	7	6	f	0	0	0	1940-07-01T01:10:38.000Z	\N	263	-1	1
1073	6	6	f	0	0	0	Mystery	\N	264	-1	1
1073	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1073	6	6	f	0	0	0	Being air payday.pdf	\N	26	-1	1
1073	6	6	f	0	0	0	Tobye Zora Arlon	\N	266	-1	1
1073	6	6	f	0	0	0	Frame india. Child en.	\N	27	-1	3
1073	6	6	f	0	0	0	Additional pal. Ruling.	\N	28	-1	3
1084	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_2mb_4mb/tired.docx	\N	272	0	1
1088	2	3	f	50	0	0	\N	\N	259	-1	1
1088	6	6	f	0	0	0	2223e408-7bea-4ab9-a2f7-ba9b45b72f89	\N	261	-1	1
1088	7	6	f	0	0	0	2000-07-13T10:00:31.000Z	\N	262	-1	1
1088	7	6	f	0	0	0	1969-10-28T17:22:39.000Z	\N	263	-1	1
1088	6	6	f	0	0	0	Humor	\N	264	-1	1
1088	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
1088	6	6	f	0	0	0	My carrying call.jpg	\N	26	-1	1
1088	6	6	f	0	0	0	Lorettalorna Anais Amena	\N	266	-1	1
1088	6	6	f	0	0	0	Zero. August.	\N	27	-1	3
1088	6	6	f	0	0	0	Games income. Consultants able cheap.	\N	28	-1	3
1088	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_500kb_2mb/sg cloth cv sanyo.jpg	\N	272	0	1
1091	2	3	f	38	0	0	\N	\N	259	-1	1
1091	6	6	f	0	0	0	8fda1773-28a2-47e7-b05d-4d20a73e1d0e	\N	261	-1	1
1091	7	6	f	0	0	0	2088-07-06T06:01:02.000Z	\N	262	-1	1
1091	7	6	f	0	0	0	2019-03-12T09:23:08.000Z	\N	263	-1	1
1091	6	6	f	0	0	0	Fantasy	\N	264	-1	1
1091	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1091	6	6	f	0	0	0	Living set financial examples.txt	\N	26	-1	1
1091	6	6	f	0	0	0	Avrit Promise Eldwon	\N	266	-1	1
1091	6	6	f	0	0	0	Vendors ca. Crops parks his.	\N	27	-1	3
1091	6	6	f	0	0	0	Fellow shall low.	\N	28	-1	3
1093	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_templates/ripe restore.docx	\N	272	0	1
1097	2	3	f	48	0	0	\N	\N	259	-1	1
1097	6	6	f	0	0	0	a84f7a4b-23ca-4bea-9a6d-cf474c70a2eb	\N	261	-1	1
1097	7	6	f	0	0	0	1971-12-13T07:15:22.000Z	\N	262	-1	1
1097	7	6	f	0	0	0	1988-12-05T13:30:28.000Z	\N	263	-1	1
1097	6	6	f	0	0	0	Science Fiction	\N	264	-1	1
1097	6	6	f	0	0	0	Elsevier	\N	265	-1	1
1097	6	6	f	0	0	0	Topics apnic model.pptx	\N	26	-1	1
1097	6	6	f	0	0	0	Kirby Anais Denys	\N	266	-1	1
1097	6	6	f	0	0	0	Opposition some says. Directory.	\N	27	-1	3
1097	6	6	f	0	0	0	He added enjoying. Are remember.	\N	28	-1	3
1096	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_500kb_2mb/saying carriers nascar feel.pptx	\N	272	0	1
1100	2	3	f	43	0	0	\N	\N	259	-1	1
1100	6	6	f	0	0	0	f4f92326-3327-4a66-9b73-943577701bed	\N	261	-1	1
1100	7	6	f	0	0	0	2099-05-11T06:51:53.000Z	\N	262	-1	1
1100	7	6	f	0	0	0	2016-10-27T19:24:50.000Z	\N	263	-1	1
1100	6	6	f	0	0	0	Periodicals	\N	264	-1	1
1100	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
1100	6	6	f	0	0	0	Within comments job.docx	\N	26	-1	1
1100	6	6	f	0	0	0	Lila Melani Adena	\N	266	-1	1
1038	7	6	f	0	0	0	2011-08-17T07:33:35.000Z	\N	263	-1	1
1038	6	6	f	0	0	0	Poetry	\N	264	-1	1
1038	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1038	6	6	f	0	0	0	Previous methods rating assistant.jpg	\N	26	-1	1
1038	6	6	f	0	0	0	Leonora Antonella Julie	\N	266	-1	1
1038	6	6	f	0	0	0	Kids.	\N	27	-1	3
1038	6	6	f	0	0	0	Defeat respected st. Power admissions regular working.	\N	28	-1	3
1038	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_6mb_8mb/circular fa arthur.jpg	\N	272	0	1
1039	2	3	f	21	0	0	\N	\N	259	-1	1
1039	6	6	f	0	0	0	41c17189-885e-43a3-a5d4-432c0ef7bf9b	\N	261	-1	1
1039	7	6	f	0	0	0	2021-01-26T17:42:47.000Z	\N	262	-1	1
1039	7	6	f	0	0	0	1968-02-05T19:19:20.000Z	\N	263	-1	1
1039	6	6	f	0	0	0	Biography/Autobiography	\N	264	-1	1
1039	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
1039	6	6	f	0	0	0	November their code property.pptx	\N	26	-1	1
1039	6	6	f	0	0	0	Love Anais Maxentia	\N	266	-1	1
1039	6	6	f	0	0	0	Easy lines.	\N	27	-1	3
1039	6	6	f	0	0	0	Bank strategic. Region element england.	\N	28	-1	3
1039	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_6mb_8mb/unemployment.pptx	\N	272	0	1
1044	2	3	f	27	0	0	\N	\N	259	-1	1
1044	6	6	f	0	0	0	a4f45823-986a-4a12-a27f-92e3c5b6e3f6	\N	261	-1	1
1044	7	6	f	0	0	0	2031-08-20T12:41:02.000Z	\N	262	-1	1
1044	7	6	f	0	0	0	1961-04-10T11:37:08.000Z	\N	263	-1	1
1044	6	6	f	0	0	0	Narrative Nonfiction	\N	264	-1	1
1044	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1044	6	6	f	0	0	0	Sir office widely education.txt	\N	26	-1	1
1044	6	6	f	0	0	0	Juli Promise Abagael	\N	266	-1	1
1044	6	6	f	0	0	0	Bird facilities. Res planet movement what.	\N	27	-1	3
1044	6	6	f	0	0	0	Figure september presidential scott. Total zoning.	\N	28	-1	3
1044	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_0kb_500kb/basketball.txt	\N	272	0	1
1048	2	3	f	37	0	0	\N	\N	259	-1	1
1048	6	6	f	0	0	0	30d621ae-f48a-408b-b609-a3dd4ab3b9fb	\N	261	-1	1
1048	7	6	f	0	0	0	2040-02-17T08:46:34.000Z	\N	262	-1	1
1048	7	6	f	0	0	0	1944-08-21T08:20:47.000Z	\N	263	-1	1
1048	6	6	f	0	0	0	Science Fiction	\N	264	-1	1
1048	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1048	6	6	f	0	0	0	Department neighborhood any.docx	\N	26	-1	1
1048	6	6	f	0	0	0	Laryssa Anais Berkly	\N	266	-1	1
1048	6	6	f	0	0	0	Holiday. Explore.	\N	27	-1	3
1048	6	6	f	0	0	0	Clearly.	\N	28	-1	3
1047	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_2mb_4mb/nil climate enhancements.pptx	\N	272	0	1
1052	2	3	f	7	0	0	\N	\N	259	-1	1
1052	6	6	f	0	0	0	72ffae85-5a4d-42e2-9fe5-add811ba248a	\N	261	-1	1
1052	7	6	f	0	0	0	2049-02-27T14:07:47.000Z	\N	262	-1	1
1052	7	6	f	0	0	0	1930-06-29T22:40:32.000Z	\N	263	-1	1
1052	6	6	f	0	0	0	Narrative Nonfiction	\N	264	-1	1
1052	6	6	f	0	0	0	Egmont	\N	265	-1	1
1052	6	6	f	0	0	0	Transfers students catholic channel.docx	\N	26	-1	1
1052	6	6	f	0	0	0	Claudia Anais Dustan	\N	266	-1	1
1052	6	6	f	0	0	0	Otherwise enough digital.	\N	27	-1	3
1052	6	6	f	0	0	0	Roughly wednesday.	\N	28	-1	3
1052	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_500kb_2mb/baths influenced.docx	\N	272	0	1
1057	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
1078	2	3	f	99	0	0	\N	\N	259	-1	1
1078	6	6	f	0	0	0	e3ebe889-faa5-4433-b08a-ece221e28194	\N	261	-1	1
1078	7	6	f	0	0	0	2107-09-29T19:25:26.000Z	\N	262	-1	1
1078	7	6	f	0	0	0	1943-05-18T18:22:09.000Z	\N	263	-1	1
1040	7	6	f	0	0	0	1989-06-19T03:46:05.000Z	\N	262	-1	1
1040	7	6	f	0	0	0	1936-09-28T13:59:48.000Z	\N	263	-1	1
1040	6	6	f	0	0	0	Horror	\N	264	-1	1
1040	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1040	6	6	f	0	0	0	Ring wind office.pdf	\N	26	-1	1
1040	6	6	f	0	0	0	Aldo Melani Arbuckle	\N	266	-1	1
1040	6	6	f	0	0	0	Gay. Needs google read industry.	\N	27	-1	3
1040	6	6	f	0	0	0	Men user cosmetic. Kde monetary level.	\N	28	-1	3
1066	2	3	f	54	0	0	\N	\N	259	-1	1
1066	6	6	f	0	0	0	64d817ca-b1a8-4c27-a49e-0ea8791f1a56	\N	261	-1	1
1066	7	6	f	0	0	0	2096-12-29T02:11:36.000Z	\N	262	-1	1
1066	7	6	f	0	0	0	2004-07-03T07:35:22.000Z	\N	263	-1	1
1066	6	6	f	0	0	0	Historical Fiction	\N	264	-1	1
1066	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1066	6	6	f	0	0	0	However every.pptx	\N	26	-1	1
1066	6	6	f	0	0	0	Karmen Antonella Haida	\N	266	-1	1
1066	6	6	f	0	0	0	Admin russian babes.	\N	27	-1	3
1066	6	6	f	0	0	0	Uw groups pairs dv. Project company call.	\N	28	-1	3
1066	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_4mb_6mb/unified ftp.pptx	\N	272	0	1
1070	2	3	f	87	0	0	\N	\N	259	-1	1
1070	6	6	f	0	0	0	2cb31abf-967c-42a9-afa0-b65582493952	\N	261	-1	1
1070	7	6	f	0	0	0	2021-09-23T08:40:22.000Z	\N	262	-1	1
1070	7	6	f	0	0	0	1944-06-15T04:22:59.000Z	\N	263	-1	1
1070	6	6	f	0	0	0	Romance	\N	264	-1	1
1070	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
1070	6	6	f	0	0	0	Center operates work years.txt	\N	26	-1	1
1070	6	6	f	0	0	0	Myrah Anais Allis	\N	266	-1	1
1070	6	6	f	0	0	0	Pages smoke.	\N	27	-1	3
1070	6	6	f	0	0	0	Financial.	\N	28	-1	3
1070	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_4mb_6mb/aqua discounts.txt	\N	272	0	1
1071	2	3	f	48	0	0	\N	\N	259	-1	1
1071	6	6	f	0	0	0	5b5024bd-7e59-4e83-a600-6b7306c75f08	\N	261	-1	1
1071	7	6	f	0	0	0	2001-10-09T16:19:17.000Z	\N	262	-1	1
1071	7	6	f	0	0	0	1953-01-29T07:34:06.000Z	\N	263	-1	1
1071	6	6	f	0	0	0	Fantasy	\N	264	-1	1
1071	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1071	6	6	f	0	0	0	Meetings using bench kingdom.pptx	\N	26	-1	1
1071	6	6	f	0	0	0	Lisha Berkley Lanta	\N	266	-1	1
1071	6	6	f	0	0	0	Public details.	\N	27	-1	3
1071	6	6	f	0	0	0	Sociology.	\N	28	-1	3
1071	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_6mb_8mb/unemployment.pptx	\N	272	0	1
1077	2	3	f	80	0	0	\N	\N	259	-1	1
1077	6	6	f	0	0	0	00ecadfc-53c6-43cd-841f-fc6a671bd1e7	\N	261	-1	1
1077	7	6	f	0	0	0	1979-12-08T03:58:11.000Z	\N	262	-1	1
1077	7	6	f	0	0	0	1954-11-05T12:42:15.000Z	\N	263	-1	1
1077	6	6	f	0	0	0	Textbook	\N	264	-1	1
1077	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1077	6	6	f	0	0	0	Wish devil price.pptx	\N	26	-1	1
1077	6	6	f	0	0	0	Karie Anais Greenberg	\N	266	-1	1
1077	6	6	f	0	0	0	Incoming.	\N	27	-1	3
1077	6	6	f	0	0	0	Physiology internet goal advanced. Think motors association little.	\N	28	-1	3
1077	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_4mb_6mb/unified ftp.pptx	\N	272	0	1
1081	2	3	f	85	0	0	\N	\N	259	-1	1
1081	6	6	f	0	0	0	021498db-06c4-480f-aea2-5d1348ddf04a	\N	261	-1	1
1081	7	6	f	0	0	0	2041-10-25T07:15:11.000Z	\N	262	-1	1
1081	7	6	f	0	0	0	1957-11-26T19:42:58.000Z	\N	263	-1	1
1081	6	6	f	0	0	0	Crime and Detective	\N	264	-1	1
1081	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1081	6	6	f	0	0	0	Turned wy.txt	\N	26	-1	1
1046	6	6	f	0	0	0	d446f812-07a9-4b13-aef8-d99187eebcd0	\N	261	-1	1
1046	7	6	f	0	0	0	2056-11-04T00:14:42.000Z	\N	262	-1	1
1046	7	6	f	0	0	0	1985-06-21T02:44:50.000Z	\N	263	-1	1
1046	6	6	f	0	0	0	Reference Books	\N	264	-1	1
1046	6	6	f	0	0	0	Elsevier	\N	265	-1	1
1046	6	6	f	0	0	0	Disease refers chem.jpg	\N	26	-1	1
1046	6	6	f	0	0	0	Lucilia Kehlani Romelda	\N	266	-1	1
1046	6	6	f	0	0	0	Key. Burton.	\N	27	-1	3
1046	6	6	f	0	0	0	Items las. Apr.	\N	28	-1	3
1046	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_2mb_4mb/supported plymouth tomatoes central.jpg	\N	272	0	1
1049	2	3	f	72	0	0	\N	\N	259	-1	1
1049	6	6	f	0	0	0	cd228ce0-21c9-46a1-835a-4db042df9d47	\N	261	-1	1
1049	7	6	f	0	0	0	2047-05-28T02:45:12.000Z	\N	262	-1	1
1049	7	6	f	0	0	0	1998-01-30T20:21:32.000Z	\N	263	-1	1
1049	6	6	f	0	0	0	Short Story	\N	264	-1	1
1049	6	6	f	0	0	0	Elsevier	\N	265	-1	1
1049	6	6	f	0	0	0	Wells maritime ringtones.pdf	\N	26	-1	1
1049	6	6	f	0	0	0	Rivalee Anais Allmon	\N	266	-1	1
1049	6	6	f	0	0	0	Series.	\N	27	-1	3
1049	6	6	f	0	0	0	Sample da ten julie. Social send putting maintained.	\N	28	-1	3
1051	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_2mb_4mb/battle compliance monkey.txt	\N	272	0	1
1056	2	3	f	89	0	0	\N	\N	259	-1	1
1056	6	6	f	0	0	0	818dfe2f-3ceb-47ee-a6cb-892895880034	\N	261	-1	1
1056	7	6	f	0	0	0	2105-06-25T08:14:43.000Z	\N	262	-1	1
1056	7	6	f	0	0	0	1964-09-13T05:34:39.000Z	\N	263	-1	1
1056	6	6	f	0	0	0	Romance	\N	264	-1	1
1056	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1056	6	6	f	0	0	0	We away la.pdf	\N	26	-1	1
1056	6	6	f	0	0	0	Kass Anais Beatrice	\N	266	-1	1
1056	6	6	f	0	0	0	Results be creating. Asked anderson.	\N	27	-1	3
1056	6	6	f	0	0	0	Additional vector big. Fat k respectively.	\N	28	-1	3
1056	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
1059	2	3	f	41	0	0	\N	\N	259	-1	1
1059	6	6	f	0	0	0	2f4bb32b-96a8-4ec2-a4ad-a9dc8ccf7dc6	\N	261	-1	1
1059	7	6	f	0	0	0	2058-06-01T17:46:37.000Z	\N	262	-1	1
1059	7	6	f	0	0	0	1910-12-06T05:13:29.000Z	\N	263	-1	1
1059	6	6	f	0	0	0	Narrative Nonfiction	\N	264	-1	1
1059	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
1059	6	6	f	0	0	0	Says beauty.txt	\N	26	-1	1
1059	6	6	f	0	0	0	Karon Zora Albertine	\N	266	-1	1
1059	6	6	f	0	0	0	Car. Natural mm.	\N	27	-1	3
1059	6	6	f	0	0	0	Things.	\N	28	-1	3
1062	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_4mb_6mb/adopted ol intersection defence.docx	\N	272	0	1
1065	2	3	f	58	0	0	\N	\N	259	-1	1
1065	6	6	f	0	0	0	61f69815-8ea5-4959-b8d0-7dd323a3bece	\N	261	-1	1
1065	7	6	f	0	0	0	2083-09-26T10:03:30.000Z	\N	262	-1	1
1065	7	6	f	0	0	0	1957-04-14T05:02:23.000Z	\N	263	-1	1
1065	6	6	f	0	0	0	Essay	\N	264	-1	1
1065	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1065	6	6	f	0	0	0	Committed about mills.docx	\N	26	-1	1
1065	6	6	f	0	0	0	Koressa Anais Behre	\N	266	-1	1
1065	6	6	f	0	0	0	Subject.	\N	27	-1	3
1065	6	6	f	0	0	0	Bookstore.	\N	28	-1	3
1081	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_8mb_10mb/accounts sudden.txt	\N	272	0	1
1086	2	3	f	95	0	0	\N	\N	259	-1	1
1086	6	6	f	0	0	0	4eab4039-931c-4a76-9927-393638679cac	\N	261	-1	1
1086	7	6	f	0	0	0	1982-08-21T04:48:21.000Z	\N	262	-1	1
1086	7	6	f	0	0	0	1955-09-16T08:21:00.000Z	\N	263	-1	1
1051	6	6	f	0	0	0	People between female programmer.	\N	27	-1	3
1051	6	6	f	0	0	0	Group titans events.	\N	28	-1	3
1054	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_8mb_10mb/prayer merger.pptx	\N	272	0	1
1058	2	3	f	40	0	0	\N	\N	259	-1	1
1058	6	6	f	0	0	0	abaa6e77-8999-40b3-bb13-a52a63a57a8d	\N	261	-1	1
1058	7	6	f	0	0	0	2021-07-17T03:27:32.000Z	\N	262	-1	1
1058	7	6	f	0	0	0	1954-01-27T04:22:40.000Z	\N	263	-1	1
1058	6	6	f	0	0	0	Realistic Fiction	\N	264	-1	1
1058	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1058	6	6	f	0	0	0	Stock unity reserved disaster.docx	\N	26	-1	1
1058	6	6	f	0	0	0	Lydie Anais Adlai	\N	266	-1	1
1058	6	6	f	0	0	0	Kinds. Like.	\N	27	-1	3
1058	6	6	f	0	0	0	Here. Jun.	\N	28	-1	3
1058	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_4mb_6mb/adopted ol intersection defence.docx	\N	272	0	1
1061	2	3	f	5	0	0	\N	\N	259	-1	1
1061	6	6	f	0	0	0	1c9359ae-3d95-4d53-a89a-84adf1572cea	\N	261	-1	1
1061	7	6	f	0	0	0	1979-07-24T23:07:02.000Z	\N	262	-1	1
1061	7	6	f	0	0	0	1928-11-11T11:51:36.000Z	\N	263	-1	1
1061	6	6	f	0	0	0	Action and Adventure	\N	264	-1	1
1061	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1061	6	6	f	0	0	0	Lab lines.pptx	\N	26	-1	1
1061	6	6	f	0	0	0	Lettie Anais Adaline	\N	266	-1	1
1061	6	6	f	0	0	0	Commercial. Surf beach taylor.	\N	27	-1	3
1061	6	6	f	0	0	0	Four knowledge. F j.	\N	28	-1	3
1061	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_8mb_10mb/prayer merger.pptx	\N	272	0	1
1065	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_6mb_8mb/gotten hebrew.docx	\N	272	0	1
1067	2	3	f	31	0	0	\N	\N	259	-1	1
1067	6	6	f	0	0	0	17608992-4025-4138-8263-4e598d610fc2	\N	261	-1	1
1067	7	6	f	0	0	0	1987-01-05T18:53:09.000Z	\N	262	-1	1
1067	7	6	f	0	0	0	1933-04-05T08:30:31.000Z	\N	263	-1	1
1067	6	6	f	0	0	0	Anthology	\N	264	-1	1
1067	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1067	6	6	f	0	0	0	Items not.docx	\N	26	-1	1
1067	6	6	f	0	0	0	Lainey Melani Chastain	\N	266	-1	1
1067	6	6	f	0	0	0	Must.	\N	27	-1	3
1067	6	6	f	0	0	0	Religion faq. Print yesterday thing making.	\N	28	-1	3
1082	2	3	f	8	0	0	\N	\N	259	-1	1
1082	6	6	f	0	0	0	edb10018-a578-4f47-adbf-8009a87a3a44	\N	261	-1	1
1082	7	6	f	0	0	0	2108-01-19T07:25:26.000Z	\N	262	-1	1
1082	7	6	f	0	0	0	1978-11-12T08:38:21.000Z	\N	263	-1	1
1082	6	6	f	0	0	0	Poetry	\N	264	-1	1
1082	6	6	f	0	0	0	Egmont	\N	265	-1	1
1082	6	6	f	0	0	0	People software.docx	\N	26	-1	1
1082	6	6	f	0	0	0	Nanni Berkley Alfonse	\N	266	-1	1
1082	6	6	f	0	0	0	International. Possibilities.	\N	27	-1	3
1082	6	6	f	0	0	0	Outdoor certainly delivery food.	\N	28	-1	3
1082	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_8mb_10mb/officers press ways bass.docx	\N	272	0	1
1083	2	3	f	33	0	0	\N	\N	259	-1	1
1083	6	6	f	0	0	0	63ab0542-644c-4043-8aa0-deea6dfbbbb3	\N	261	-1	1
1083	7	6	f	0	0	0	2065-05-14T21:10:54.000Z	\N	262	-1	1
1083	7	6	f	0	0	0	2009-08-13T03:51:07.000Z	\N	263	-1	1
1083	6	6	f	0	0	0	Textbook	\N	264	-1	1
1083	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1083	6	6	f	0	0	0	Composer rates into testing.txt	\N	26	-1	1
1083	6	6	f	0	0	0	Stefanie Anais Amandie	\N	266	-1	1
1083	6	6	f	0	0	0	Known yeah features. Error during records.	\N	27	-1	3
1083	6	6	f	0	0	0	Layers.	\N	28	-1	3
1100	6	6	f	0	0	0	Portal better hill. Hands.	\N	27	-1	3
1053	6	6	f	0	0	0	Legend	\N	264	-1	1
1053	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1053	6	6	f	0	0	0	Study ontario om.pdf	\N	26	-1	1
1053	6	6	f	0	0	0	Lela Zora Bailey	\N	266	-1	1
1053	6	6	f	0	0	0	Lauderdale rentals post. Venice february.	\N	27	-1	3
1053	6	6	f	0	0	0	Center digital life.	\N	28	-1	3
1053	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_8mb_10mb/jewelry maritime.pdf	\N	272	0	1
1055	2	3	f	30	0	0	\N	\N	259	-1	1
1055	6	6	f	0	0	0	e65f2adf-b131-404f-a740-0a1fc686f428	\N	261	-1	1
1055	7	6	f	0	0	0	2109-09-14T05:42:20.000Z	\N	262	-1	1
1055	7	6	f	0	0	0	1990-02-26T07:45:11.000Z	\N	263	-1	1
1055	6	6	f	0	0	0	Mystery	\N	264	-1	1
1055	6	6	f	0	0	0	Egmont	\N	265	-1	1
1055	6	6	f	0	0	0	Chip order units.jpg	\N	26	-1	1
1055	6	6	f	0	0	0	Kerrie Antonella Hannie	\N	266	-1	1
1055	6	6	f	0	0	0	Match law actual. January.	\N	27	-1	3
1055	6	6	f	0	0	0	Road emirates four. Practice inquire.	\N	28	-1	3
1055	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_8mb_10mb/go person ef.jpg	\N	272	0	1
1060	2	3	f	46	0	0	\N	\N	259	-1	1
1060	6	6	f	0	0	0	31b72d8c-7f06-4d9a-bc0e-90dc3e0cf5a9	\N	261	-1	1
1060	7	6	f	0	0	0	1982-11-29T05:45:31.000Z	\N	262	-1	1
1060	7	6	f	0	0	0	1913-12-09T08:01:58.000Z	\N	263	-1	1
1060	6	6	f	0	0	0	Crime and Detective	\N	264	-1	1
1060	6	6	f	0	0	0	Random House	\N	265	-1	1
1060	6	6	f	0	0	0	Fan working center.pptx	\N	26	-1	1
1060	6	6	f	0	0	0	Lucita Anais Dotti	\N	266	-1	1
1060	6	6	f	0	0	0	Franchise bolivia closed required.	\N	27	-1	3
1060	6	6	f	0	0	0	Compressed style problem. Report various his indirect.	\N	28	-1	3
1060	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_500kb_2mb/saying carriers nascar feel.pptx	\N	272	0	1
1063	2	3	f	68	0	0	\N	\N	259	-1	1
1063	6	6	f	0	0	0	cd4c50df-106a-4af9-a477-b88ff508c598	\N	261	-1	1
1063	7	6	f	0	0	0	2036-05-14T12:27:02.000Z	\N	262	-1	1
1063	7	6	f	0	0	0	2000-09-16T11:57:06.000Z	\N	263	-1	1
1063	6	6	f	0	0	0	Biography/Autobiography	\N	264	-1	1
1063	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
1063	6	6	f	0	0	0	Rs show sci radiation.txt	\N	26	-1	1
1063	6	6	f	0	0	0	Lilias Anais Chandless	\N	266	-1	1
1063	6	6	f	0	0	0	Add powered country.	\N	27	-1	3
1063	6	6	f	0	0	0	Chairman man whether healthy.	\N	28	-1	3
1063	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_4mb_6mb/aqua discounts.txt	\N	272	0	1
1069	2	3	f	69	0	0	\N	\N	259	-1	1
1069	6	6	f	0	0	0	ce076b72-7b9f-464f-8d23-c16553b3ed6b	\N	261	-1	1
1069	7	6	f	0	0	0	2079-01-28T07:58:29.000Z	\N	262	-1	1
1069	7	6	f	0	0	0	1959-03-01T03:07:54.000Z	\N	263	-1	1
1069	6	6	f	0	0	0	Reference Books	\N	264	-1	1
1069	6	6	f	0	0	0	Egmont	\N	265	-1	1
1069	6	6	f	0	0	0	Focused reading.pdf	\N	26	-1	1
1069	6	6	f	0	0	0	Alisa Anais Behl	\N	266	-1	1
1069	6	6	f	0	0	0	Books surveys novel saying.	\N	27	-1	3
1069	6	6	f	0	0	0	Energy.	\N	28	-1	3
1069	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
1072	2	3	f	51	0	0	\N	\N	259	-1	1
1072	6	6	f	0	0	0	1c352dee-fe36-4d60-8d23-9a6c6f0a3ad9	\N	261	-1	1
1072	7	6	f	0	0	0	2025-12-05T13:55:07.000Z	\N	262	-1	1
1072	7	6	f	0	0	0	1943-03-22T13:14:07.000Z	\N	263	-1	1
1072	6	6	f	0	0	0	Historical Fiction	\N	264	-1	1
1072	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1072	6	6	f	0	0	0	Nonprofit obviously me together.txt	\N	26	-1	1
1054	7	6	f	0	0	0	1963-12-26T17:32:37.000Z	\N	263	-1	1
1054	6	6	f	0	0	0	Fable	\N	264	-1	1
1054	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1054	6	6	f	0	0	0	Today wa interview analysis.pptx	\N	26	-1	1
1054	6	6	f	0	0	0	Eugene Anais Blanchette	\N	266	-1	1
1054	6	6	f	0	0	0	Accessed operation.	\N	27	-1	3
1054	6	6	f	0	0	0	Back. Chargers nhl thread.	\N	28	-1	3
1057	2	3	f	84	0	0	\N	\N	259	-1	1
1057	6	6	f	0	0	0	166b09ac-9ed1-41ca-9cd8-68dee65881b4	\N	261	-1	1
1057	7	6	f	0	0	0	2068-10-19T14:00:32.000Z	\N	262	-1	1
1057	7	6	f	0	0	0	1984-04-26T15:57:04.000Z	\N	263	-1	1
1057	6	6	f	0	0	0	Mystery	\N	264	-1	1
1057	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
1057	6	6	f	0	0	0	Person set.pdf	\N	26	-1	1
1057	6	6	f	0	0	0	Tonnie Berkley Gurtner	\N	266	-1	1
1057	6	6	f	0	0	0	Estate use. Thru ever search.	\N	27	-1	3
1057	6	6	f	0	0	0	Museum prediction rules partner.	\N	28	-1	3
1068	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_8mb_10mb/jewelry maritime.pdf	\N	272	0	1
1074	2	3	f	15	0	0	\N	\N	259	-1	1
1074	6	6	f	0	0	0	1f12ca12-21bc-4113-b1cc-1c90089a273e	\N	261	-1	1
1074	7	6	f	0	0	0	2000-03-05T16:02:38.000Z	\N	262	-1	1
1074	7	6	f	0	0	0	1952-03-24T08:29:57.000Z	\N	263	-1	1
1074	6	6	f	0	0	0	Mythology	\N	264	-1	1
1074	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
1074	6	6	f	0	0	0	Coast ago as write.docx	\N	26	-1	1
1074	6	6	f	0	0	0	Katey Anais Domph	\N	266	-1	1
1074	6	6	f	0	0	0	Variable president shall. Audience gave san pp.	\N	27	-1	3
1074	6	6	f	0	0	0	Brand june broadband.	\N	28	-1	3
1074	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_4mb_6mb/adopted ol intersection defence.docx	\N	272	0	1
1076	2	3	f	10	0	0	\N	\N	259	-1	1
1076	6	6	f	0	0	0	0fa7eadb-58b7-414e-86c1-0008998fd5a6	\N	261	-1	1
1076	7	6	f	0	0	0	2082-06-30T21:09:05.000Z	\N	262	-1	1
1076	7	6	f	0	0	0	1996-04-06T18:46:18.000Z	\N	263	-1	1
1076	6	6	f	0	0	0	Fantasy	\N	264	-1	1
1076	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1076	6	6	f	0	0	0	Holidays dealt.txt	\N	26	-1	1
1076	6	6	f	0	0	0	Mair Anais Cazzie	\N	266	-1	1
1076	6	6	f	0	0	0	Heart provides.	\N	27	-1	3
1076	6	6	f	0	0	0	Them.	\N	28	-1	3
1076	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_4mb_6mb/aqua discounts.txt	\N	272	0	1
1080	2	3	f	40	0	0	\N	\N	259	-1	1
1080	6	6	f	0	0	0	6dad935b-0071-44dd-a1fd-00e34a5e1857	\N	261	-1	1
1080	7	6	f	0	0	0	2006-12-20T08:45:39.000Z	\N	262	-1	1
1080	7	6	f	0	0	0	1946-01-12T03:15:02.000Z	\N	263	-1	1
1080	6	6	f	0	0	0	Reference Books	\N	264	-1	1
1080	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
1080	6	6	f	0	0	0	Display please hunt.jpg	\N	26	-1	1
1080	6	6	f	0	0	0	Kitti Berkley Armalla	\N	266	-1	1
1080	6	6	f	0	0	0	Did feedback.	\N	27	-1	3
1080	6	6	f	0	0	0	Fun.	\N	28	-1	3
1080	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_0kb_500kb/redeem.jpg	\N	272	0	1
1084	2	3	f	73	0	0	\N	\N	259	-1	1
1084	6	6	f	0	0	0	ddd885e8-0cb5-4b8c-a8ac-a5c727c0ee03	\N	261	-1	1
1084	7	6	f	0	0	0	2081-02-01T12:51:29.000Z	\N	262	-1	1
1084	7	6	f	0	0	0	1933-04-18T22:40:48.000Z	\N	263	-1	1
1084	6	6	f	0	0	0	Historical Fiction	\N	264	-1	1
1084	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1084	6	6	f	0	0	0	Martial between thumbs.docx	\N	26	-1	1
1084	6	6	f	0	0	0	Julee Anais Bautram	\N	266	-1	1
1072	6	6	f	0	0	0	Cathrin Anais Kenyon	\N	266	-1	1
1072	6	6	f	0	0	0	Good katrina.	\N	27	-1	3
1072	6	6	f	0	0	0	Gmc big corporate. Interested.	\N	28	-1	3
1072	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_0kb_500kb/basketball.txt	\N	272	0	1
1094	2	3	f	18	0	0	\N	\N	259	-1	1
1094	6	6	f	0	0	0	c86448c7-cf02-43af-937c-5d761a4dabc4	\N	261	-1	1
1094	7	6	f	0	0	0	2054-10-03T09:17:15.000Z	\N	262	-1	1
1094	7	6	f	0	0	0	1996-11-20T01:19:59.000Z	\N	263	-1	1
1094	6	6	f	0	0	0	Fantasy	\N	264	-1	1
1094	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1094	6	6	f	0	0	0	Advanced university build.txt	\N	26	-1	1
1094	6	6	f	0	0	0	Rora Anais Alessandra	\N	266	-1	1
1094	6	6	f	0	0	0	Staff pleasant greater. Subjects home.	\N	27	-1	3
1094	6	6	f	0	0	0	Girls. Application n children.	\N	28	-1	3
1108	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_8mb_10mb/go person ef.jpg	\N	272	0	1
1112	2	3	f	66	0	0	\N	\N	259	-1	1
1112	6	6	f	0	0	0	6424f508-472a-43e2-868a-83cca00ba105	\N	261	-1	1
1112	7	6	f	0	0	0	1987-10-24T06:58:14.000Z	\N	262	-1	1
1112	7	6	f	0	0	0	1920-11-19T18:53:41.000Z	\N	263	-1	1
1112	6	6	f	0	0	0	Self-help Book	\N	264	-1	1
1112	6	6	f	0	0	0	Random House	\N	265	-1	1
1112	6	6	f	0	0	0	Schools air our.pdf	\N	26	-1	1
1112	6	6	f	0	0	0	Philipa Anais Beilul	\N	266	-1	1
1112	6	6	f	0	0	0	Portable centers various recent.	\N	27	-1	3
1112	6	6	f	0	0	0	Photos minister. H shopping.	\N	28	-1	3
1112	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_2mb_4mb/mice hh lecture.pdf	\N	272	0	1
1116	2	3	f	29	0	0	\N	\N	259	-1	1
1116	6	6	f	0	0	0	d74f8e52-f6ef-484b-af2f-2e03fcc14a0b	\N	261	-1	1
1116	7	6	f	0	0	0	2039-02-19T22:29:22.000Z	\N	262	-1	1
1116	7	6	f	0	0	0	1978-05-24T08:53:08.000Z	\N	263	-1	1
1116	6	6	f	0	0	0	Science Fiction	\N	264	-1	1
1116	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
1116	6	6	f	0	0	0	Public boxes methods mark.pdf	\N	26	-1	1
1116	6	6	f	0	0	0	Roderich Anais Brennan	\N	266	-1	1
1116	6	6	f	0	0	0	Tuesday don types.	\N	27	-1	3
1116	6	6	f	0	0	0	Audience full gb including.	\N	28	-1	3
1116	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_2mb_4mb/mice hh lecture.pdf	\N	272	0	1
1078	6	6	f	0	0	0	Reference Books	\N	264	-1	1
1078	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
1078	6	6	f	0	0	0	Retro classes project.docx	\N	26	-1	1
1078	6	6	f	0	0	0	Lust Berkley Ashley	\N	266	-1	1
1078	6	6	f	0	0	0	Special august optional ftp.	\N	27	-1	3
1078	6	6	f	0	0	0	Through.	\N	28	-1	3
1078	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_4mb_6mb/adopted ol intersection defence.docx	\N	272	0	1
1091	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_8mb_10mb/accounts sudden.txt	\N	272	0	1
1095	2	3	f	82	0	0	\N	\N	259	-1	1
1095	6	6	f	0	0	0	7cdbf7c3-3ca1-487c-8ee3-16231577b8c1	\N	261	-1	1
1095	7	6	f	0	0	0	2062-05-05T20:47:50.000Z	\N	262	-1	1
1095	7	6	f	0	0	0	2008-04-27T18:38:35.000Z	\N	263	-1	1
1095	6	6	f	0	0	0	Magical Realism	\N	264	-1	1
1095	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1095	6	6	f	0	0	0	Limitation study.jpg	\N	26	-1	1
1095	6	6	f	0	0	0	Lorettalorna Kehlani Abdulla	\N	266	-1	1
1095	6	6	f	0	0	0	Allows processors.	\N	27	-1	3
1095	6	6	f	0	0	0	Film partnership.	\N	28	-1	3
1095	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_8mb_10mb/go person ef.jpg	\N	272	0	1
1099	2	3	f	33	0	0	\N	\N	259	-1	1
1099	6	6	f	0	0	0	19b87a3c-d8f3-4acb-8675-25e2309d6746	\N	261	-1	1
1099	7	6	f	0	0	0	1979-09-16T14:14:47.000Z	\N	262	-1	1
1099	7	6	f	0	0	0	1947-03-31T19:53:12.000Z	\N	263	-1	1
1099	6	6	f	0	0	0	Biography/Autobiography	\N	264	-1	1
1099	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1099	6	6	f	0	0	0	Where lyrics.txt	\N	26	-1	1
1099	6	6	f	0	0	0	Rora Anais Alfred	\N	266	-1	1
1099	6	6	f	0	0	0	Recycling.	\N	27	-1	3
1099	6	6	f	0	0	0	Course measures related. Between shipping.	\N	28	-1	3
1100	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_500kb_2mb/baths influenced.docx	\N	272	0	1
1106	2	3	f	72	0	0	\N	\N	259	-1	1
1106	6	6	f	0	0	0	dd02534f-bee4-4c12-abd7-ba364f5d8757	\N	261	-1	1
1106	7	6	f	0	0	0	2063-04-29T18:39:44.000Z	\N	262	-1	1
1106	7	6	f	0	0	0	2012-02-21T07:12:59.000Z	\N	263	-1	1
1106	6	6	f	0	0	0	Action and Adventure	\N	264	-1	1
1106	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
1106	6	6	f	0	0	0	Users metro there building.docx	\N	26	-1	1
1106	6	6	f	0	0	0	Theda Zora Dwayne	\N	266	-1	1
1106	6	6	f	0	0	0	Palm depth party. Expression.	\N	27	-1	3
1106	6	6	f	0	0	0	Corporate based watches hospital. Committed offer strategic may.	\N	28	-1	3
1106	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_2mb_4mb/tired.docx	\N	272	0	1
1110	2	3	f	96	0	0	\N	\N	259	-1	1
1110	6	6	f	0	0	0	90d0f94c-ea87-4485-ba16-29640f0444ef	\N	261	-1	1
1110	7	6	f	0	0	0	2068-11-04T20:00:39.000Z	\N	262	-1	1
1110	7	6	f	0	0	0	1953-09-27T11:21:02.000Z	\N	263	-1	1
1110	6	6	f	0	0	0	Mythology	\N	264	-1	1
1110	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1110	6	6	f	0	0	0	Data university verzeichnis.docx	\N	26	-1	1
1110	6	6	f	0	0	0	Rachel Zora Purington	\N	266	-1	1
1110	6	6	f	0	0	0	Make.	\N	27	-1	3
1110	6	6	f	0	0	0	Paintings. Leaving water.	\N	28	-1	3
1117	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_8mb_10mb/officers press ways bass.docx	\N	272	0	1
1119	2	3	f	45	0	0	\N	\N	259	-1	1
1119	6	6	f	0	0	0	bfcef5af-e386-4e48-855f-2e6d5d6e3972	\N	261	-1	1
1119	7	6	f	0	0	0	2017-12-14T22:50:37.000Z	\N	262	-1	1
1119	7	6	f	0	0	0	1967-06-23T08:48:36.000Z	\N	263	-1	1
1119	6	6	f	0	0	0	Fantasy	\N	264	-1	1
1079	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1079	6	6	f	0	0	0	Thought law.jpg	\N	26	-1	1
1079	6	6	f	0	0	0	Juliet Anais Alric	\N	266	-1	1
1079	6	6	f	0	0	0	Savings must.	\N	27	-1	3
1079	6	6	f	0	0	0	Data user request. Also advantage english ends.	\N	28	-1	3
1079	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_0kb_500kb/redeem.jpg	\N	272	0	1
1085	2	3	f	26	0	0	\N	\N	259	-1	1
1085	6	6	f	0	0	0	51998d92-9d8d-45e2-9af5-52171bf0841c	\N	261	-1	1
1085	7	6	f	0	0	0	1999-12-04T16:10:29.000Z	\N	262	-1	1
1085	7	6	f	0	0	0	1967-11-02T19:51:02.000Z	\N	263	-1	1
1085	6	6	f	0	0	0	Fantasy	\N	264	-1	1
1085	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1085	6	6	f	0	0	0	Automatically association.txt	\N	26	-1	1
1085	6	6	f	0	0	0	Letitia Anais Dever	\N	266	-1	1
1085	6	6	f	0	0	0	Black manager manage. Baby go while.	\N	27	-1	3
1085	6	6	f	0	0	0	Seller.	\N	28	-1	3
1086	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_2mb_4mb/nil climate enhancements.pptx	\N	272	0	1
1090	2	3	f	10	0	0	\N	\N	259	-1	1
1090	6	6	f	0	0	0	ad62c450-3de9-49c4-9fd4-a29228faed5b	\N	261	-1	1
1090	7	6	f	0	0	0	2030-10-15T17:28:05.000Z	\N	262	-1	1
1090	7	6	f	0	0	0	1974-01-23T05:20:46.000Z	\N	263	-1	1
1090	6	6	f	0	0	0	Periodicals	\N	264	-1	1
1090	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1090	6	6	f	0	0	0	July business.jpg	\N	26	-1	1
1090	6	6	f	0	0	0	Kerrin Anais Baker	\N	266	-1	1
1090	6	6	f	0	0	0	Software dragon. Along bigger.	\N	27	-1	3
1090	6	6	f	0	0	0	Went artist seems. View folks.	\N	28	-1	3
1090	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_0kb_500kb/redeem.jpg	\N	272	0	1
1102	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_500kb_2mb/sg cloth cv sanyo.jpg	\N	272	0	1
1105	2	3	f	73	0	0	\N	\N	259	-1	1
1105	6	6	f	0	0	0	5289dd1e-bc63-4243-ab15-2ca586811044	\N	261	-1	1
1105	7	6	f	0	0	0	2085-05-30T05:51:22.000Z	\N	262	-1	1
1105	7	6	f	0	0	0	1995-02-25T21:20:58.000Z	\N	263	-1	1
1105	6	6	f	0	0	0	Fantasy	\N	264	-1	1
1105	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1105	6	6	f	0	0	0	Xp organ paxil marketing.jpg	\N	26	-1	1
1105	6	6	f	0	0	0	Kordula Anais Adonis	\N	266	-1	1
1105	6	6	f	0	0	0	Lower format ev folders. Corn l james.	\N	27	-1	3
1105	6	6	f	0	0	0	Lang. Sep.	\N	28	-1	3
1105	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_8mb_10mb/go person ef.jpg	\N	272	0	1
1108	2	3	f	60	0	0	\N	\N	259	-1	1
1108	6	6	f	0	0	0	d600774a-2eac-41e4-8aeb-ebbc08fea1df	\N	261	-1	1
1108	7	6	f	0	0	0	2032-04-02T01:45:59.000Z	\N	262	-1	1
1108	7	6	f	0	0	0	1947-05-12T07:44:58.000Z	\N	263	-1	1
1108	6	6	f	0	0	0	Memoir	\N	264	-1	1
1108	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1108	6	6	f	0	0	0	International attempt.jpg	\N	26	-1	1
1108	6	6	f	0	0	0	Elenore Saoirse Senskell	\N	266	-1	1
1108	6	6	f	0	0	0	Security zoom official adult. High ways high stuff.	\N	27	-1	3
1108	6	6	f	0	0	0	Order.	\N	28	-1	3
1127	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_2mb_4mb/mice hh lecture.pdf	\N	272	0	1
1132	2	3	f	31	0	0	\N	\N	259	-1	1
1132	6	6	f	0	0	0	7317748e-7149-4408-89fb-8a672b92d9a6	\N	261	-1	1
1132	7	6	f	0	0	0	2097-06-10T02:18:41.000Z	\N	262	-1	1
1132	7	6	f	0	0	0	1990-09-29T19:49:10.000Z	\N	263	-1	1
1132	6	6	f	0	0	0	Poetry	\N	264	-1	1
1132	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1132	6	6	f	0	0	0	Disco library.txt	\N	26	-1	1
1081	6	6	f	0	0	0	Nonah Anais Lochner	\N	266	-1	1
1081	6	6	f	0	0	0	Calendar influence italian. Bill flu.	\N	27	-1	3
1081	6	6	f	0	0	0	Tales usd. Seller blue holmes.	\N	28	-1	3
1085	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_0kb_500kb/basketball.txt	\N	272	0	1
1089	2	3	f	16	0	0	\N	\N	259	-1	1
1089	6	6	f	0	0	0	6a5490ae-04bb-4d20-a007-cbfbe29f1187	\N	261	-1	1
1089	7	6	f	0	0	0	1977-10-22T16:37:58.000Z	\N	262	-1	1
1089	7	6	f	0	0	0	1920-05-23T04:35:11.000Z	\N	263	-1	1
1089	6	6	f	0	0	0	Classic	\N	264	-1	1
1089	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1089	6	6	f	0	0	0	Atmosphere fell half sign.txt	\N	26	-1	1
1089	6	6	f	0	0	0	Carter Anais Anestassia	\N	266	-1	1
1089	6	6	f	0	0	0	Karen small cache. Gold firefox academy.	\N	27	-1	3
1089	6	6	f	0	0	0	Industry.	\N	28	-1	3
1089	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_0kb_500kb/basketball.txt	\N	272	0	1
1093	2	3	f	54	0	0	\N	\N	259	-1	1
1093	6	6	f	0	0	0	33a15a1e-3cbf-467a-8ce0-22667a425607	\N	261	-1	1
1093	7	6	f	0	0	0	2107-09-27T05:28:28.000Z	\N	262	-1	1
1093	7	6	f	0	0	0	1918-06-19T05:06:44.000Z	\N	263	-1	1
1093	6	6	f	0	0	0	Suspense/Thriller	\N	264	-1	1
1093	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
1093	6	6	f	0	0	0	B failed.docx	\N	26	-1	1
1093	6	6	f	0	0	0	Shelby Anais Adham	\N	266	-1	1
1093	6	6	f	0	0	0	Known read night.	\N	27	-1	3
1093	6	6	f	0	0	0	Foto eclipse.	\N	28	-1	3
1122	2	3	f	47	0	0	\N	\N	259	-1	1
1122	6	6	f	0	0	0	abf0d5bf-0283-42be-98c4-b8ddb28167af	\N	261	-1	1
1122	7	6	f	0	0	0	2004-08-10T23:11:04.000Z	\N	262	-1	1
1122	7	6	f	0	0	0	1989-07-29T04:15:30.000Z	\N	263	-1	1
1122	6	6	f	0	0	0	Speech	\N	264	-1	1
1122	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1122	6	6	f	0	0	0	Regional seller.docx	\N	26	-1	1
1122	6	6	f	0	0	0	Lyndsay Anais Connel	\N	266	-1	1
1122	6	6	f	0	0	0	Affairs has guitars media. Occurred.	\N	27	-1	3
1122	6	6	f	0	0	0	Translate. Pattern video.	\N	28	-1	3
1120	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_8mb_10mb/accounts sudden.txt	\N	272	0	1
1124	2	3	f	25	0	0	\N	\N	259	-1	1
1124	6	6	f	0	0	0	d7bec726-2c87-4e1a-8399-d02a08a006f7	\N	261	-1	1
1124	7	6	f	0	0	0	1985-11-01T13:36:36.000Z	\N	262	-1	1
1124	7	6	f	0	0	0	1956-06-23T08:02:04.000Z	\N	263	-1	1
1124	6	6	f	0	0	0	Fairy Tale	\N	264	-1	1
1124	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1124	6	6	f	0	0	0	Interview together.txt	\N	26	-1	1
1124	6	6	f	0	0	0	Kaylyn Xiomara Dustan	\N	266	-1	1
1124	6	6	f	0	0	0	Login alone will century.	\N	27	-1	3
1124	6	6	f	0	0	0	Applicants sm.	\N	28	-1	3
1124	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_0kb_500kb/basketball.txt	\N	272	0	1
1128	2	3	f	71	0	0	\N	\N	259	-1	1
1128	6	6	f	0	0	0	bddfa2c8-8521-4200-bd07-c254bc53330f	\N	261	-1	1
1128	7	6	f	0	0	0	2054-04-18T01:41:10.000Z	\N	262	-1	1
1128	7	6	f	0	0	0	1985-06-12T19:34:37.000Z	\N	263	-1	1
1128	6	6	f	0	0	0	Fantasy	\N	264	-1	1
1128	6	6	f	0	0	0	Random House	\N	265	-1	1
1128	6	6	f	0	0	0	Annual discount.txt	\N	26	-1	1
1128	6	6	f	0	0	0	Judie Zora Berna	\N	266	-1	1
1128	6	6	f	0	0	0	Collective plan make. Until.	\N	27	-1	3
1128	6	6	f	0	0	0	Active.	\N	28	-1	3
1128	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_2mb_4mb/battle compliance monkey.txt	\N	272	0	1
1133	2	3	f	6	0	0	\N	\N	259	-1	1
1086	6	6	f	0	0	0	Fairy Tale	\N	264	-1	1
1086	6	6	f	0	0	0	Random House	\N	265	-1	1
1086	6	6	f	0	0	0	Wordpress.pptx	\N	26	-1	1
1086	6	6	f	0	0	0	Margaux Antonella Aphra	\N	266	-1	1
1086	6	6	f	0	0	0	Life faq college induced. Facing.	\N	27	-1	3
1086	6	6	f	0	0	0	L limits show calling. Cinema.	\N	28	-1	3
1097	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_8mb_10mb/prayer merger.pptx	\N	272	0	1
1101	2	3	f	52	0	0	\N	\N	259	-1	1
1101	6	6	f	0	0	0	c24070e1-d835-444d-9979-0c75233ff86c	\N	261	-1	1
1101	7	6	f	0	0	0	2002-04-11T04:39:17.000Z	\N	262	-1	1
1101	7	6	f	0	0	0	1938-11-06T23:19:22.000Z	\N	263	-1	1
1101	6	6	f	0	0	0	Reference Books	\N	264	-1	1
1101	6	6	f	0	0	0	Egmont	\N	265	-1	1
1101	6	6	f	0	0	0	Registered boy amount.txt	\N	26	-1	1
1101	6	6	f	0	0	0	Lottie Berkley Barbey	\N	266	-1	1
1101	6	6	f	0	0	0	At sit.	\N	27	-1	3
1101	6	6	f	0	0	0	Live some reserved. Manager travel trade role.	\N	28	-1	3
1101	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_6mb_8mb/terminal instructor podcasts button.txt	\N	272	0	1
1104	2	3	f	94	0	0	\N	\N	259	-1	1
1104	6	6	f	0	0	0	44c84672-5e74-4e2f-8227-9d55a9811463	\N	261	-1	1
1104	7	6	f	0	0	0	2101-02-18T12:55:36.000Z	\N	262	-1	1
1104	7	6	f	0	0	0	1937-02-11T09:25:24.000Z	\N	263	-1	1
1104	6	6	f	0	0	0	Essay	\N	264	-1	1
1104	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1104	6	6	f	0	0	0	Times recognition.txt	\N	26	-1	1
1104	6	6	f	0	0	0	Kalila Anais Berti	\N	266	-1	1
1104	6	6	f	0	0	0	Website payable.	\N	27	-1	3
1104	6	6	f	0	0	0	Average warranty library purchased.	\N	28	-1	3
1109	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_500kb_2mb/decrease cafe madison rosa.pdf	\N	272	0	1
1113	2	3	f	77	0	0	\N	\N	259	-1	1
1113	6	6	f	0	0	0	503e87d5-ab33-4d56-bca5-3bdc5d4716af	\N	261	-1	1
1113	7	6	f	0	0	0	2103-09-29T05:44:51.000Z	\N	262	-1	1
1113	7	6	f	0	0	0	1920-05-11T18:05:55.000Z	\N	263	-1	1
1113	6	6	f	0	0	0	Memoir	\N	264	-1	1
1113	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1113	6	6	f	0	0	0	Lynn golf.pdf	\N	26	-1	1
1113	6	6	f	0	0	0	Bryan Berkley Rebeka	\N	266	-1	1
1113	6	6	f	0	0	0	Theory hi says tsunami. His.	\N	27	-1	3
1113	6	6	f	0	0	0	Nest faith. Needs.	\N	28	-1	3
1113	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_500kb_2mb/decrease cafe madison rosa.pdf	\N	272	0	1
1118	2	3	f	36	0	0	\N	\N	259	-1	1
1118	6	6	f	0	0	0	8da47627-9e97-4cea-ab8e-a89cb79c85b2	\N	261	-1	1
1118	7	6	f	0	0	0	2066-04-04T07:26:14.000Z	\N	262	-1	1
1118	7	6	f	0	0	0	1953-06-14T04:02:48.000Z	\N	263	-1	1
1118	6	6	f	0	0	0	Drama	\N	264	-1	1
1118	6	6	f	0	0	0	John Wiley & Sons	\N	265	-1	1
1118	6	6	f	0	0	0	Based planning categories.pdf	\N	26	-1	1
1118	6	6	f	0	0	0	Morganne Anais Spence	\N	266	-1	1
1118	6	6	f	0	0	0	Testing cord twenty. Sony cover decisions.	\N	27	-1	3
1118	6	6	f	0	0	0	Honolulu there already. Provide forming process hot.	\N	28	-1	3
1115	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_500kb_2mb/saying carriers nascar feel.pptx	\N	272	0	1
1120	2	3	f	38	0	0	\N	\N	259	-1	1
1120	6	6	f	0	0	0	7f120436-010c-4ef2-ad90-5e556bb4b0d9	\N	261	-1	1
1120	7	6	f	0	0	0	2085-04-06T09:35:21.000Z	\N	262	-1	1
1120	7	6	f	0	0	0	2010-09-08T08:06:22.000Z	\N	263	-1	1
1120	6	6	f	0	0	0	Suspense/Thriller	\N	264	-1	1
1120	6	6	f	0	0	0	Egmont	\N	265	-1	1
1120	6	6	f	0	0	0	Move by position would.txt	\N	26	-1	1
1084	6	6	f	0	0	0	Cb. Such once founded update.	\N	27	-1	3
1084	6	6	f	0	0	0	Dying. Available.	\N	28	-1	3
1094	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_0kb_500kb/basketball.txt	\N	272	0	1
1098	2	3	f	29	0	0	\N	\N	259	-1	1
1098	6	6	f	0	0	0	a178b532-d29e-4dbb-8b3d-4d8aba8cc353	\N	261	-1	1
1098	7	6	f	0	0	0	2110-03-18T08:19:49.000Z	\N	262	-1	1
1098	7	6	f	0	0	0	2008-07-02T05:59:32.000Z	\N	263	-1	1
1098	6	6	f	0	0	0	Textbook	\N	264	-1	1
1098	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1098	6	6	f	0	0	0	Goes own hardware.txt	\N	26	-1	1
1098	6	6	f	0	0	0	Julienne Melani Critta	\N	266	-1	1
1098	6	6	f	0	0	0	Visit. Pro.	\N	27	-1	3
1098	6	6	f	0	0	0	Frontier arts.	\N	28	-1	3
1119	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_2mb_4mb/mice hh lecture.pdf	\N	272	0	1
1123	2	3	f	94	0	0	\N	\N	259	-1	1
1123	6	6	f	0	0	0	7e3f235c-d875-44e4-8674-9197a200a0dc	\N	261	-1	1
1123	7	6	f	0	0	0	2084-10-08T10:10:28.000Z	\N	262	-1	1
1123	7	6	f	0	0	0	1964-12-10T18:04:24.000Z	\N	263	-1	1
1123	6	6	f	0	0	0	Classic	\N	264	-1	1
1123	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1123	6	6	f	0	0	0	Adopted usa.txt	\N	26	-1	1
1123	6	6	f	0	0	0	Barnaby Xiomara Dot	\N	266	-1	1
1123	6	6	f	0	0	0	Look base theme board.	\N	27	-1	3
1123	6	6	f	0	0	0	Iowa program others watch.	\N	28	-1	3
1123	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_0kb_500kb/basketball.txt	\N	272	0	1
1127	2	3	f	80	0	0	\N	\N	259	-1	1
1127	6	6	f	0	0	0	ca53e7da-a5e7-4893-9ae2-abe4d89b8e2b	\N	261	-1	1
1127	7	6	f	0	0	0	2026-05-09T04:47:07.000Z	\N	262	-1	1
1127	7	6	f	0	0	0	1976-05-30T08:34:04.000Z	\N	263	-1	1
1127	6	6	f	0	0	0	Crime and Detective	\N	264	-1	1
1127	6	6	f	0	0	0	Random House	\N	265	-1	1
1127	6	6	f	0	0	0	Desired score oct june.pdf	\N	26	-1	1
1127	6	6	f	0	0	0	Petrina Antonella Altheta	\N	266	-1	1
1127	6	6	f	0	0	0	Phone tool hierarchy four. Jc her online.	\N	27	-1	3
1127	6	6	f	0	0	0	Signal worth. Real figure.	\N	28	-1	3
1129	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_templates/ripe restore.docx	\N	272	0	1
1083	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_6mb_8mb/terminal instructor podcasts button.txt	\N	272	0	1
1087	2	3	f	47	0	0	\N	\N	259	-1	1
1087	6	6	f	0	0	0	547b4689-052a-4a90-82fb-b36f7f425ecc	\N	261	-1	1
1087	7	6	f	0	0	0	2020-03-08T22:10:11.000Z	\N	262	-1	1
1087	7	6	f	0	0	0	1919-01-25T14:58:22.000Z	\N	263	-1	1
1087	6	6	f	0	0	0	Magical Realism	\N	264	-1	1
1087	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1087	6	6	f	0	0	0	International providers her.jpg	\N	26	-1	1
1087	6	6	f	0	0	0	Orella Anais Alexandria	\N	266	-1	1
1087	6	6	f	0	0	0	Content capability meant. Ear space last.	\N	27	-1	3
1087	6	6	f	0	0	0	Association link text web. Standing wire.	\N	28	-1	3
1087	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_6mb_8mb/circular fa arthur.jpg	\N	272	0	1
1092	2	3	f	98	0	0	\N	\N	259	-1	1
1092	6	6	f	0	0	0	0a125ec9-66a5-49b6-bdc3-6241df1fc51e	\N	261	-1	1
1092	7	6	f	0	0	0	2008-01-24T15:56:59.000Z	\N	262	-1	1
1092	7	6	f	0	0	0	2018-08-15T16:09:15.000Z	\N	263	-1	1
1092	6	6	f	0	0	0	Realistic Fiction	\N	264	-1	1
1092	6	6	f	0	0	0	Egmont	\N	265	-1	1
1092	6	6	f	0	0	0	Happen search.txt	\N	26	-1	1
1092	6	6	f	0	0	0	Caron Berkley Fidele	\N	266	-1	1
1092	6	6	f	0	0	0	Occur one authentication resource. Ranks.	\N	27	-1	3
1092	6	6	f	0	0	0	Overcome neighborhood integration.	\N	28	-1	3
1092	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_4mb_6mb/aqua discounts.txt	\N	272	0	1
1096	2	3	f	53	0	0	\N	\N	259	-1	1
1096	6	6	f	0	0	0	948ee878-e7f5-4158-94e8-81c9e824f1fe	\N	261	-1	1
1096	7	6	f	0	0	0	2007-03-26T19:52:54.000Z	\N	262	-1	1
1096	7	6	f	0	0	0	1966-09-06T03:37:44.000Z	\N	263	-1	1
1096	6	6	f	0	0	0	Fantasy	\N	264	-1	1
1096	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
1096	6	6	f	0	0	0	Forced sublime instead.pptx	\N	26	-1	1
1096	6	6	f	0	0	0	Colline Anais Emie	\N	266	-1	1
1096	6	6	f	0	0	0	See berkeley specials.	\N	27	-1	3
1096	6	6	f	0	0	0	Print label.	\N	28	-1	3
1098	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_8mb_10mb/accounts sudden.txt	\N	272	0	1
1102	2	3	f	98	0	0	\N	\N	259	-1	1
1102	6	6	f	0	0	0	8ea1dbe2-2a56-4d39-9896-f4ebbdaf5a03	\N	261	-1	1
1102	7	6	f	0	0	0	2029-12-15T21:58:37.000Z	\N	262	-1	1
1102	7	6	f	0	0	0	1932-12-22T06:58:49.000Z	\N	263	-1	1
1102	6	6	f	0	0	0	Biography/Autobiography	\N	264	-1	1
1102	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1102	6	6	f	0	0	0	Working specify dominican.jpg	\N	26	-1	1
1102	6	6	f	0	0	0	Kyla Anais Munro	\N	266	-1	1
1102	6	6	f	0	0	0	Man appearing america. Set accessories turkey.	\N	27	-1	3
1102	6	6	f	0	0	0	General n mt safely. Installed grow marble.	\N	28	-1	3
1099	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_0kb_500kb/basketball.txt	\N	272	0	1
1103	2	3	f	73	0	0	\N	\N	259	-1	1
1103	6	6	f	0	0	0	4508a834-28df-4c2d-a714-a44005a82380	\N	261	-1	1
1103	7	6	f	0	0	0	1971-02-01T10:15:08.000Z	\N	262	-1	1
1103	7	6	f	0	0	0	1992-03-15T22:06:27.000Z	\N	263	-1	1
1103	6	6	f	0	0	0	Fable	\N	264	-1	1
1103	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1103	6	6	f	0	0	0	Range home.pptx	\N	26	-1	1
1103	6	6	f	0	0	0	Katinka Promise Denver	\N	266	-1	1
1103	6	6	f	0	0	0	Article. Come information compressed.	\N	27	-1	3
1103	6	6	f	0	0	0	Ashley key.	\N	28	-1	3
1104	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_2mb_4mb/battle compliance monkey.txt	\N	272	0	1
1107	2	3	f	11	0	0	\N	\N	259	-1	1
1100	6	6	f	0	0	0	Yards.	\N	28	-1	3
1103	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_8mb_10mb/prayer merger.pptx	\N	272	0	1
1109	2	3	f	75	0	0	\N	\N	259	-1	1
1109	6	6	f	0	0	0	79668f5f-7950-4e73-a87b-47afe6cfc5ff	\N	261	-1	1
1109	7	6	f	0	0	0	1981-01-23T05:24:58.000Z	\N	262	-1	1
1109	7	6	f	0	0	0	2008-01-17T01:12:42.000Z	\N	263	-1	1
1109	6	6	f	0	0	0	Realistic Fiction	\N	264	-1	1
1109	6	6	f	0	0	0	Random House	\N	265	-1	1
1109	6	6	f	0	0	0	File offers execute.pdf	\N	26	-1	1
1109	6	6	f	0	0	0	Barby Anais Anastice	\N	266	-1	1
1109	6	6	f	0	0	0	Find another. Baby after bahamas isa.	\N	27	-1	3
1109	6	6	f	0	0	0	Award.	\N	28	-1	3
1110	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_500kb_2mb/baths influenced.docx	\N	272	0	1
1114	2	3	f	19	0	0	\N	\N	259	-1	1
1114	6	6	f	0	0	0	00b06a8a-98bb-4d10-b2dc-1ed3c472bc78	\N	261	-1	1
1114	7	6	f	0	0	0	2109-07-10T11:36:11.000Z	\N	262	-1	1
1114	7	6	f	0	0	0	2014-11-13T18:59:14.000Z	\N	263	-1	1
1114	6	6	f	0	0	0	Self-help Book	\N	264	-1	1
1114	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1114	6	6	f	0	0	0	Companies rating.pptx	\N	26	-1	1
1114	6	6	f	0	0	0	Kristien Kehlani Airliah	\N	266	-1	1
1114	6	6	f	0	0	0	Indeed along de.	\N	27	-1	3
1114	6	6	f	0	0	0	Standard ipod l.	\N	28	-1	3
1114	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_2mb_4mb/nil climate enhancements.pptx	\N	272	0	1
1117	2	3	f	4	0	0	\N	\N	259	-1	1
1117	6	6	f	0	0	0	e1e0a6e8-2bc9-42e4-9b37-d933abb24977	\N	261	-1	1
1117	7	6	f	0	0	0	2041-10-24T21:00:04.000Z	\N	262	-1	1
1117	7	6	f	0	0	0	1990-03-28T05:58:41.000Z	\N	263	-1	1
1117	6	6	f	0	0	0	Speech	\N	264	-1	1
1117	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
1117	6	6	f	0	0	0	Without due.docx	\N	26	-1	1
1117	6	6	f	0	0	0	Karim Anais Georgi	\N	266	-1	1
1117	6	6	f	0	0	0	Authors company local.	\N	27	-1	3
1117	6	6	f	0	0	0	Men style limited.	\N	28	-1	3
1137	2	3	f	80	0	0	\N	\N	259	-1	1
1137	6	6	f	0	0	0	6b3a0055-9bb3-44f3-91ea-c261bc566af3	\N	261	-1	1
1137	7	6	f	0	0	0	1974-07-04T21:58:35.000Z	\N	262	-1	1
1137	7	6	f	0	0	0	2005-10-29T06:41:40.000Z	\N	263	-1	1
1137	6	6	f	0	0	0	Reference Books	\N	264	-1	1
1137	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1137	6	6	f	0	0	0	Apr nt response.docx	\N	26	-1	1
1137	6	6	f	0	0	0	Willyt Anais Argyle	\N	266	-1	1
1137	6	6	f	0	0	0	Dvd. Fine html.	\N	27	-1	3
1137	6	6	f	0	0	0	Efforts. V.	\N	28	-1	3
1135	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_4mb_6mb/creations flu inappropriate actress.pdf	\N	272	0	1
1107	6	6	f	0	0	0	0ceeb55b-3420-4f55-b962-dc37197da6a9	\N	261	-1	1
1107	7	6	f	0	0	0	2076-03-02T21:01:53.000Z	\N	262	-1	1
1107	7	6	f	0	0	0	1916-10-20T05:07:08.000Z	\N	263	-1	1
1107	6	6	f	0	0	0	Speech	\N	264	-1	1
1107	6	6	f	0	0	0	Egmont	\N	265	-1	1
1107	6	6	f	0	0	0	Beliefs annex image.jpg	\N	26	-1	1
1107	6	6	f	0	0	0	Josy Anais Litt	\N	266	-1	1
1107	6	6	f	0	0	0	Con on first land. Commercial low writing.	\N	27	-1	3
1107	6	6	f	0	0	0	Countries large.	\N	28	-1	3
1107	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_4mb_6mb/safe enhancement omaha.jpg	\N	272	0	1
1111	2	3	f	8	0	0	\N	\N	259	-1	1
1111	6	6	f	0	0	0	8aa6977e-9909-4c87-a260-72a3b554b1d4	\N	261	-1	1
1111	7	6	f	0	0	0	2027-08-18T00:40:55.000Z	\N	262	-1	1
1111	7	6	f	0	0	0	2018-12-21T10:13:20.000Z	\N	263	-1	1
1111	6	6	f	0	0	0	Drama	\N	264	-1	1
1111	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1111	6	6	f	0	0	0	Gain app.pptx	\N	26	-1	1
1111	6	6	f	0	0	0	Roxy Promise Ammadas	\N	266	-1	1
1111	6	6	f	0	0	0	Become.	\N	27	-1	3
1111	6	6	f	0	0	0	Shape none metal was.	\N	28	-1	3
1111	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_0kb_500kb/monitoring zip sudan stan.pptx	\N	272	0	1
1115	2	3	f	55	0	0	\N	\N	259	-1	1
1115	6	6	f	0	0	0	83b73d5c-6988-4db3-9a96-dc8a6181fead	\N	261	-1	1
1115	7	6	f	0	0	0	2009-01-21T16:16:45.000Z	\N	262	-1	1
1115	7	6	f	0	0	0	1932-02-09T23:47:56.000Z	\N	263	-1	1
1115	6	6	f	0	0	0	Self-help Book	\N	264	-1	1
1115	6	6	f	0	0	0	Bloomsbury	\N	265	-1	1
1115	6	6	f	0	0	0	Resource california washington.pptx	\N	26	-1	1
1115	6	6	f	0	0	0	Kore Anais Allix	\N	266	-1	1
1115	6	6	f	0	0	0	Need only joint.	\N	27	-1	3
1115	6	6	f	0	0	0	Powered take days.	\N	28	-1	3
1118	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_6mb_8mb/crew antarctica.pdf	\N	272	0	1
1121	2	3	f	35	0	0	\N	\N	259	-1	1
1121	6	6	f	0	0	0	ccde8cd7-1b35-4564-908f-0cd1a54dae4d	\N	261	-1	1
1121	7	6	f	0	0	0	2048-05-12T18:39:44.000Z	\N	262	-1	1
1121	7	6	f	0	0	0	1991-03-29T04:54:41.000Z	\N	263	-1	1
1121	6	6	f	0	0	0	Mystery	\N	264	-1	1
1121	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1121	6	6	f	0	0	0	Arts double.pdf	\N	26	-1	1
1121	6	6	f	0	0	0	Morlee Anais Abramo	\N	266	-1	1
1121	6	6	f	0	0	0	Goal october gay.	\N	27	-1	3
1121	6	6	f	0	0	0	Request tuesday always whose.	\N	28	-1	3
1126	2	3	f	45	0	0	\N	\N	259	-1	1
1126	6	6	f	0	0	0	a0b2be3d-217c-4cc8-87b7-3aab7c68d363	\N	261	-1	1
1126	7	6	f	0	0	0	2047-03-17T09:01:22.000Z	\N	262	-1	1
1126	7	6	f	0	0	0	1946-11-06T02:15:45.000Z	\N	263	-1	1
1126	6	6	f	0	0	0	Classic	\N	264	-1	1
1126	6	6	f	0	0	0	Elsevier	\N	265	-1	1
1126	6	6	f	0	0	0	Systems sort guide.pdf	\N	26	-1	1
1126	6	6	f	0	0	0	Pamela Antonella Arjan	\N	266	-1	1
1126	6	6	f	0	0	0	Head. Monroe.	\N	27	-1	3
1126	6	6	f	0	0	0	Rules increase. Ontario uk saying touch.	\N	28	-1	3
1126	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_6mb_8mb/crew antarctica.pdf	\N	272	0	1
1130	2	3	f	31	0	0	\N	\N	259	-1	1
1130	6	6	f	0	0	0	59ca93ab-3152-4ee7-a885-29773b31befa	\N	261	-1	1
1130	7	6	f	0	0	0	1983-09-22T01:18:03.000Z	\N	262	-1	1
1130	7	6	f	0	0	0	1958-06-22T09:20:38.000Z	\N	263	-1	1
1130	6	6	f	0	0	0	Mystery	\N	264	-1	1
1130	6	6	f	0	0	0	Simon & Schuster	\N	265	-1	1
1119	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1119	6	6	f	0	0	0	From community point pill.pdf	\N	26	-1	1
1119	6	6	f	0	0	0	Simonette Zora Nicodemus	\N	266	-1	1
1119	6	6	f	0	0	0	Jan. Guitars.	\N	27	-1	3
1119	6	6	f	0	0	0	Mem pro win brazilian.	\N	28	-1	3
1122	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_templates/ripe restore.docx	\N	272	0	1
1125	2	3	f	82	0	0	\N	\N	259	-1	1
1125	6	6	f	0	0	0	07aa54d7-cd54-4d14-b456-142661c8a112	\N	261	-1	1
1125	7	6	f	0	0	0	2097-08-09T20:45:33.000Z	\N	262	-1	1
1125	7	6	f	0	0	0	1912-09-20T15:54:28.000Z	\N	263	-1	1
1125	6	6	f	0	0	0	Poetry	\N	264	-1	1
1125	6	6	f	0	0	0	Hachette Livre	\N	265	-1	1
1125	6	6	f	0	0	0	Value ebay.txt	\N	26	-1	1
1125	6	6	f	0	0	0	Sonnie Anais Anderer	\N	266	-1	1
1125	6	6	f	0	0	0	Tone final world. Movie.	\N	27	-1	3
1125	6	6	f	0	0	0	United special jan wireless. Now uses bowling src.	\N	28	-1	3
1125	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_4mb_6mb/aqua discounts.txt	\N	272	0	1
1129	2	3	f	75	0	0	\N	\N	259	-1	1
1129	6	6	f	0	0	0	2f799b38-c6d2-46ac-9c6d-4ed3db43661c	\N	261	-1	1
1129	7	6	f	0	0	0	2060-04-26T18:09:12.000Z	\N	262	-1	1
1129	7	6	f	0	0	0	1935-02-19T15:30:28.000Z	\N	263	-1	1
1129	6	6	f	0	0	0	Comic and Graphic Novel	\N	264	-1	1
1129	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1129	6	6	f	0	0	0	Competitions night.docx	\N	26	-1	1
1129	6	6	f	0	0	0	Maddy Antonella Vanden	\N	266	-1	1
1129	6	6	f	0	0	0	Environment dean. Handling.	\N	27	-1	3
1129	6	6	f	0	0	0	Exclusive art. Transition against.	\N	28	-1	3
1120	6	6	f	0	0	0	Loralee Antonella Amorete	\N	266	-1	1
1120	6	6	f	0	0	0	Teen globe lingerie sent. Collectibles policy following old.	\N	27	-1	3
1120	6	6	f	0	0	0	Pm slightly hampshire. Copyright humanities.	\N	28	-1	3
1121	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pdf_files_8mb_10mb/jewelry maritime.pdf	\N	272	0	1
1134	2	3	f	96	0	0	\N	\N	259	-1	1
1134	6	6	f	0	0	0	70b588a0-d8fb-4b4c-930e-5ed6d6304ae4	\N	261	-1	1
1134	7	6	f	0	0	0	2073-03-10T07:03:03.000Z	\N	262	-1	1
1134	7	6	f	0	0	0	1965-02-17T21:57:20.000Z	\N	263	-1	1
1134	6	6	f	0	0	0	Magical Realism	\N	264	-1	1
1134	6	6	f	0	0	0	Penguin Books	\N	265	-1	1
1134	6	6	f	0	0	0	Rights melissa.pptx	\N	26	-1	1
1134	6	6	f	0	0	0	Juli Anais Banebrudge	\N	266	-1	1
1134	6	6	f	0	0	0	Do credit response norm. Large version class.	\N	27	-1	3
1134	6	6	f	0	0	0	Browse event. Practices parts status.	\N	28	-1	3
1134	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_8mb_10mb/prayer merger.pptx	\N	272	0	1
1130	6	6	f	0	0	0	Vermont hotel smith before.txt	\N	26	-1	1
1130	6	6	f	0	0	0	Theresa-Marie Zora Borries	\N	266	-1	1
1130	6	6	f	0	0	0	T.	\N	27	-1	3
1130	6	6	f	0	0	0	November text labor hardware.	\N	28	-1	3
1130	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_4mb_6mb/aqua discounts.txt	\N	272	0	1
1131	2	3	f	88	0	0	\N	\N	259	-1	1
1131	6	6	f	0	0	0	1c45e1aa-9bdf-4f77-adef-fbd41c5021a3	\N	261	-1	1
1131	7	6	f	0	0	0	2063-03-10T10:01:42.000Z	\N	262	-1	1
1131	7	6	f	0	0	0	1964-10-11T18:12:26.000Z	\N	263	-1	1
1131	6	6	f	0	0	0	Drama	\N	264	-1	1
1131	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
1131	6	6	f	0	0	0	Genome production.jpg	\N	26	-1	1
1131	6	6	f	0	0	0	Krissie Anais Alger	\N	266	-1	1
1131	6	6	f	0	0	0	Line customers fluid. Poor.	\N	27	-1	3
1131	6	6	f	0	0	0	Mission group digital.	\N	28	-1	3
1133	6	6	f	0	0	0	7487da77-8080-445a-b9ae-d16d8d10d4d6	\N	261	-1	1
1133	7	6	f	0	0	0	2046-02-13T23:05:25.000Z	\N	262	-1	1
1133	7	6	f	0	0	0	1951-02-23T00:38:46.000Z	\N	263	-1	1
1133	6	6	f	0	0	0	Action and Adventure	\N	264	-1	1
1133	6	6	f	0	0	0	Pearson Education	\N	265	-1	1
1133	6	6	f	0	0	0	Forward jobs.pptx	\N	26	-1	1
1133	6	6	f	0	0	0	Vivian Anais Won	\N	266	-1	1
1133	6	6	f	0	0	0	They en jan commerce. Can allergy fields hiring.	\N	27	-1	3
1133	6	6	f	0	0	0	Movie. Have benjamin.	\N	28	-1	3
1131	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://jpg_files_4mb_6mb/safe enhancement omaha.jpg	\N	272	0	1
1135	2	3	f	50	0	0	\N	\N	259	-1	1
1135	6	6	f	0	0	0	b6dc483c-690e-4c2c-bb1c-96f27deca6c7	\N	261	-1	1
1135	7	6	f	0	0	0	1981-05-02T20:47:20.000Z	\N	262	-1	1
1135	7	6	f	0	0	0	1980-10-21T02:29:19.000Z	\N	263	-1	1
1135	6	6	f	0	0	0	Comic and Graphic Novel	\N	264	-1	1
1135	6	6	f	0	0	0	Pan Macmillan	\N	265	-1	1
1135	6	6	f	0	0	0	Her find intelligence posts.pdf	\N	26	-1	1
1135	6	6	f	0	0	0	Karry Anais Antoinetta	\N	266	-1	1
1135	6	6	f	0	0	0	Email three better washington.	\N	27	-1	3
1135	6	6	f	0	0	0	Share work after. Honest memory ideas.	\N	28	-1	3
1137	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://docx_files_2mb_4mb/tired.docx	\N	272	0	1
1132	6	6	f	0	0	0	Ketti Anais Austreng	\N	266	-1	1
1132	6	6	f	0	0	0	Handling summary excess paperback.	\N	27	-1	3
1132	6	6	f	0	0	0	Lyrics login here. Importance for.	\N	28	-1	3
1133	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_500kb_2mb/saying carriers nascar feel.pptx	\N	272	0	1
1132	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_500kb_2mb/crew balloon s.txt	\N	272	0	1
1136	2	3	f	57	0	0	\N	\N	259	-1	1
1136	6	6	f	0	0	0	53bac06b-9f37-4970-bec0-7189c1d0b807	\N	261	-1	1
1136	7	6	f	0	0	0	2035-09-08T05:17:34.000Z	\N	262	-1	1
1136	7	6	f	0	0	0	1947-12-13T04:38:48.000Z	\N	263	-1	1
1136	6	6	f	0	0	0	Horror	\N	264	-1	1
1136	6	6	f	0	0	0	HarperCollins	\N	265	-1	1
1136	6	6	f	0	0	0	Titled.txt	\N	26	-1	1
1136	6	6	f	0	0	0	Sebastien Berkley Coates	\N	266	-1	1
1136	6	6	f	0	0	0	Fun.	\N	27	-1	3
1136	6	6	f	0	0	0	Research te. Cams war love comparisons.	\N	28	-1	3
1138	2	3	f	6	0	0	\N	\N	259	-1	1
1138	6	6	f	0	0	0	4abeba4d-afac-4ddd-b06a-096778af84b9	\N	261	-1	1
1138	7	6	f	0	0	0	2104-07-04T12:31:31.000Z	\N	262	-1	1
1138	7	6	f	0	0	0	1923-07-26T01:01:00.000Z	\N	263	-1	1
1138	6	6	f	0	0	0	Horror	\N	264	-1	1
1138	6	6	f	0	0	0	Oxford University Press	\N	265	-1	1
1138	6	6	f	0	0	0	Instructions after so holiday.pptx	\N	26	-1	1
1138	6	6	f	0	0	0	Pembroke Anais Adah	\N	266	-1	1
1138	6	6	f	0	0	0	Toys far local brief. Speed z scheme tonight.	\N	27	-1	3
1138	6	6	f	0	0	0	Movies loss stress.	\N	28	-1	3
1136	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://txt_files_500kb_2mb/crew balloon s.txt	\N	272	0	1
1138	6	6	f	0	0	0	{http://www.alfresco.org/model/content/1.0}content|s3://pptx_files_4mb_6mb/unified ftp.pptx	\N	272	0	1
1139	6	6	f	0	0	0	tag1	\N	26	-1	1
1140	6	6	f	0	0	0	tag2	\N	26	-1	1
12147483647	6	6	f	0	0	0	tag3	\N	26	-1	1
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
25	0	0	52	52
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
\.


--
-- Data for Name: alf_prop_serializable_value; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_prop_serializable_value (id, serializable_value) FROM stdin;
1	\\xaced0005737200166f72672e616c66726573636f2e7574696c2e506169729937b84d08eece6c0200024c000566697273747400124c6a6176612f6c616e672f4f626a6563743b4c00067365636f6e6471007e0001787074002436356335333537642d316661652d343338612d613264382d656262316534353938393163737200196a617661782e63727970746f2e5365616c65644f626a6563743e363da6c3b754700200045b000d656e636f646564506172616d737400025b425b0010656e63727970746564436f6e74656e7471007e00054c0009706172616d73416c677400124c6a6176612f6c616e672f537472696e673b4c00077365616c416c6771007e00067870757200025b42acf317f8060854e002000078700000000a04089f92ba2f0a339ce27571007e0008000000305da7dbcff1104c581a774d7c0c72ec576ac9f551bbe694844a0284f4d8fdb0da3d50ff0a772831d95b7acc2c28d163b57400064445536564657400174445536564652f4342432f504b43533550616464696e67
2	\\xaced0005737200166f72672e616c66726573636f2e7574696c2e506169729937b84d08eece6c0200024c000566697273747400124c6a6176612f6c616e672f4f626a6563743b4c00067365636f6e6471007e0001787074002461633664353238652d386436362d343135392d396161362d626261303562316564636265737200196a617661782e63727970746f2e5365616c65644f626a6563743e363da6c3b754700200045b000d656e636f646564506172616d737400025b425b0010656e63727970746564436f6e74656e7471007e00054c0009706172616d73416c677400124c6a6176612f6c616e672f537472696e673b4c00077365616c416c6771007e00067870757200025b42acf317f8060854e002000078700000000a0408a141d9a5bc261f947571007e000800000030328478d2ad0fe1b9d3455de9c16a5bdb1bbd2ed08170b38c704cee068d1e6fe431e21139b12eb07942b0c7dd87844a767400064445536564657400174445536564652f4342432f504b43533550616464696e67
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
13	MainRepository-88588b6c-8d18-4587-9c52-b9b0980326ee	c52-b9b0980326ee	1251716270
14	.clusterMembers	.clustermembers	501968308
15	172.31.0.7:5701	172.31.0.7:5701	114495939
16	.host_name	.host_name	3564746463
17	11d23ecf25af	11d23ecf25af	790439321
18	.ip_address	.ip_address	2115545287
19	172.31.0.7	172.31.0.7	3205648161
20	.port	.port	3657884840
21	.clustering_enabled	ustering_enabled	2216157857
22	.last_registered	.last_registered	2925481710
23	.cluster_node_type	luster_node_type	3072798820
24	"Repository server"	pository server"	890365791
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
\.


--
-- Data for Name: alf_prop_unique_ctx; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_prop_unique_ctx (id, version, value1_prop_id, value2_prop_id, value3_prop_id, prop1_id) FROM stdin;
1	0	1	2	3	1
4	0	7	8	13	5
5	0	7	8	15	6
6	0	16	17	3	7
7	0	19	20	21	8
8	0	19	20	23	9
9	0	19	20	25	10
10	0	19	20	27	11
11	0	19	20	29	12
12	0	19	20	31	13
13	0	33	34	3	14
15	0	33	38	3	16
17	0	33	42	3	\N
18	0	33	43	3	18
16	1	33	40	3	19
14	1	33	36	3	20
19	0	1	47	3	21
2	2	7	8	9	24
3	2	7	8	11	25
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
10	5	1	1619008665107
11	1	3	8
12	5	1	0
13	1	3	9
14	5	1	1619008665113
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
30	8	1	1619008666304
31	1	3	23
32	1	3	24
33	1	3	25
34	1	3	26
35	5	1	1619008669428
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
46	5	1	1619008669449
47	1	3	36
48	3	4	2
49	5	1	1619008669739
50	5	1	1
51	5	1	1619008915543
52	5	1	2
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
232	0	14	rendition2
233	0	14	contentHashCode
234	0	1	archived
235	0	1	archivedDate
236	0	1	archivedOriginalParentAssoc
237	0	1	archivedOriginalOwner
238	0	1	archivedBy
239	0	1	archiveRoot
240	0	1	archiveUser
241	0	1	archiveUserLink
242	0	1	archivedLink
243	0	6	dictionaryModel
244	0	6	modelActive
245	0	6	modelName
246	0	6	modelAuthor
247	0	6	modelPublishedDate
248	0	6	modelDescription
249	0	6	modelVersion
250	0	5	metadata-versionType
251	0	2	passwordHash
252	0	2	hashIndicator
253	0	6	emailed
254	0	6	addressees
255	0	6	addressee
256	0	6	sentdate
257	0	6	originator
258	0	6	subjectline
259	0	6	hits
261	0	6	identifier
262	0	6	to
263	0	6	from
264	0	6	type
265	0	6	publisher
266	0	6	contributor
268	0	6	dublincore
269	0	6	countable
271	0	6	effectivity
272	0	21	missingContentInfo
273	0	21	missingContent
\.


--
-- Data for Name: alf_server; Type: TABLE DATA; Schema: public; Owner: alfresco
--

COPY public.alf_server (id, version, ip_address) FROM stdin;
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

COPY public.alf_transaction (id, version, server_id, change_txn_id, commit_time_ms) FROM stdin;
1	1	\N	d62b1170-f7fc-4afa-a8fa-ea0b72a3da7c	1619008660351
2	1	\N	c7b53ce1-f07c-4980-826b-b584f64e8e5d	1619008660429
3	1	\N	ebb4c1e6-d8f5-4f99-8e41-3fe5cb744f17	1619008660463
4	1	\N	23ba3fa1-c08e-4d6f-b9b6-5774b881a88d	1619008660485
5	1	\N	13b943bb-b5c1-4e25-a09e-0597c2fa031f	1619008660497
6	1	\N	d32d8b8d-bd19-4e75-b7b5-eb906bce0a68	1619008664031
7	1	\N	51dd3488-cb58-40e8-b6aa-3fbd68d39361	1619008665092
8	1	\N	6e821856-e383-4334-bd3e-e8784af1bd72	1619008665124
9	1	\N	d33eab93-54c4-4e8f-bad7-3805a8a069a8	1619008666898
10	1	\N	2c4a8dea-9caf-40f6-b3fc-0681912d9979	1619008666905
11	1	\N	2b5508cc-d561-4190-9dd7-992a6264ce0b	1619008668832
12	1	\N	308b2258-7d01-49f9-bf3a-533b33983ec4	1619008668916
13	1	\N	c2043139-bcfe-428c-a867-b377728aa7d4	1619008669007
14	1	\N	c72ea0bd-6dfc-41c3-bddf-b0008713d3d8	1619008669716
15	1	\N	d8d01cc7-03a6-43ea-950b-a9cdb6c7e5a8	1619008670101
16	1	\N	6d94ec3f-68e1-46ce-b2a6-b834b2831122	1619008670106
17	1	\N	fb057e36-4975-463e-be25-fb8f1808f9eb	1619008670502
18	1	\N	a659a181-0b97-4256-80c2-11e530557751	1619008670515
19	1	\N	b27e054f-84a6-43b5-964e-a6e87399fb34	1619008800577
20	1	\N	0f189b43-f5a8-44a6-afc8-ec77451cc3b1	1619008800935
21	1	\N	19cc997f-1e99-486a-86e2-823f09b0157a	1619008845267
22	1	\N	4e15ae26-4f15-4441-99c1-4710f3a9ffae	1619008845276
23	1	\N	2288887a-6192-43b6-928f-8d8d5936bff4	1619008847647
24	1	\N	3b41a86a-4501-410a-83b6-d81426cc6376	1619008847676
25	1	\N	ae36bfed-4af4-4d53-b508-786795658d73	1619008852004
26	1	\N	76352c24-1abe-484c-8de8-6eb30f14ee02	1619008852033
27	1	\N	0a3d9d6f-2a8b-4c6b-b31d-54955dd73610	1619008864053
28	1	\N	ff2c435d-5bd2-4a9b-9357-88b8df10ba08	1619008864061
29	1	\N	813ee9ce-44ff-4e20-819c-6f448a4a7ea2	1619008865417
30	1	\N	ab53dbe7-910d-47e1-98a5-93184f7d59b5	1619008865437
31	1	\N	5c56c7ba-60fe-4e99-97ff-85fffbe22951	1619008867744
32	1	\N	a743e6c8-c656-4f15-adf0-c69c0c0006f3	1619008867768
33	1	\N	2653c9d4-c302-4281-8728-603ed18a6cbe	1619008877486
34	1	\N	784f34e2-188a-4e39-bb9e-5ddeaf94b2ec	1619008877498
35	1	\N	c282ccb6-7bb5-44b2-8252-c720f946cfe3	1619008913671
36	1	\N	a8deb2d4-1757-416a-8725-3b91ecc3bcd2	1619008913894
37	1	\N	dbf4ce81-442f-44e6-bae0-a69b3dfc371e	1619008914035
38	1	\N	20bdada7-d3fe-4185-afa3-6aa908c79b13	1619008914412
39	1	\N	525470e3-908e-4345-a4d0-fae4063b606b	1619008914603
40	1	\N	383a98e3-ca16-41d4-a102-2cde79a76336	1619008914978
41	1	\N	005b7475-edf0-4980-92fd-c7995f5d6def	1619008915098
42	1	\N	0e565607-5222-443c-bfa1-f9a342c5f49f	1619008915164
43	1	\N	9837f72c-6b63-4b97-a8c4-fc7d70a776f5	1619008915438
44	1	\N	002cbcd1-09e1-403a-8e29-6ec27227f035	1619008915452
45	1	\N	4a3a8ebd-f385-4b68-ba0b-0c3f43cdda96	1619008915941
46	1	\N	c876269d-2eba-4dc5-bb8c-ff2f95d78bb3	1619008916430
47	1	\N	e6902955-67f1-41b9-818f-540591272393	1619008916844
48	1	\N	f1920e77-96b1-4297-b6f5-6d8113a85aad	1619008916960
49	1	\N	bf750b22-6ceb-4e09-867b-a87b598d745d	1619008917098
50	1	\N	4dd4f19f-01f1-4f5c-a4b3-f59868dd0c38	1619008917195
51	1	\N	93bcb7d4-713f-4f0d-a5be-458ca51bba67	1619008938950
52	1	\N	2fb99735-259d-4d33-881b-6d6823d97679	1619008938963
53	1	\N	745fc505-4646-467d-b62f-12b22b920fbe	1619008939057
54	1	\N	8a9fe493-b934-4708-ac97-b3cfdf773963	1619008939159
55	1	\N	a9a4da9f-1bcd-4d6f-a7a5-2907de571bc3	1619008939254
56	1	\N	1d8378ed-a519-45fc-b4f5-d2403c6d6562	1619008939375
57	1	\N	065ae775-600f-4863-8b4c-b21a3ecd53eb	1619008940079
61	1	\N	d2925c0e-a9bb-4992-8f82-ce66f693d749	1619008940167
62	1	\N	225a1d9e-ec59-4a76-89e2-785b3883de97	1619008940394
63	1	\N	1a9aa385-6d29-45c2-bfa5-efc36c3e96a1	1619008940401
64	1	\N	1ec5fdb8-cf06-4788-ae6d-c8c539f36683	1619008940402
65	1	\N	f7ba1b39-a059-49e9-a562-153000db28bf	1619008940433
67	1	\N	894c8869-4efb-4ab7-88fb-89ca81f4d5e6	1619008940573
69	1	\N	8b3285e1-1825-4448-8dce-1b55a0eee25c	1619008940577
68	1	\N	fdd184f2-1e68-4bc8-9d57-5ad4c89719c8	1619008940596
66	1	\N	afdb685e-aeb2-40fc-a016-38442237b437	1619008940596
70	1	\N	c662c8de-d5bf-441a-b195-fa6b9a710257	1619008940670
71	1	\N	d19d0fd3-76cb-421c-adc8-6e72134a2a45	1619008940678
73	1	\N	5c8b3152-662f-404d-9e12-faf64f7ace6d	1619008940679
72	1	\N	93e551e3-16c3-44ef-a998-193292309c93	1619008940688
412	1	\N	eafeaf26-3b07-49d6-9b70-d176055a42da	1619008949671
431	1	\N	951a2dd6-b422-4305-b485-7c8342048b1f	1619008949936
435	1	\N	7556f755-cda7-410e-9ac7-2ec68118b213	1619008949995
439	1	\N	eee89565-1f25-4035-9e38-b8fbc67d5e7f	1619008950080
442	1	\N	887911da-8438-4ae0-a147-d1af476a597f	1619008950140
444	1	\N	2d2ebaa3-7eef-42e5-85ef-6829cb1c701a	1619008950223
76	1	\N	c5ce71f8-a1d0-4c06-840e-b3c389c04607	1619008940740
88	1	\N	43a530fa-52b9-4db3-8441-8e915b631f17	1619008941318
91	1	\N	a32e3690-35ed-41e7-ba25-49772bc02991	1619008941451
95	1	\N	e871aec3-18ca-40a5-a8ba-98d3ef019ea4	1619008941630
99	1	\N	f4df3244-e080-4327-8345-0ae4283c231a	1619008941671
107	1	\N	17135de8-9b45-402b-bd53-d57b9e785bb6	1619008941792
109	1	\N	bac8bd4c-e3d6-47c9-b3fd-b5dd1902ff00	1619008941877
114	1	\N	f75145ca-c715-4a6f-b34a-f583b9da722a	1619008941936
116	1	\N	a1ff2448-3048-4309-bef7-1cd0f5d2effc	1619008941960
120	1	\N	61b8c3b4-9148-4b48-8195-3f00d72b7965	1619008942064
124	1	\N	debb4680-1ded-498e-a91c-cf56bafe18a3	1619008942119
128	1	\N	6601b1c5-e059-4ee1-9de7-a87279b842a5	1619008942194
133	1	\N	4c3733c0-a591-40b4-9e88-bf7bae228d20	1619008942271
167	1	\N	485ff15d-79ae-40ed-a2f5-7c82c9fb145d	1619008942951
173	1	\N	1c50dbba-81e0-446f-a0c4-f8f12093b183	1619008943026
175	1	\N	e2677dfe-d535-490b-83e4-c1f53b87c9f3	1619008943107
180	1	\N	cdde5cdb-7d9f-4aad-ab83-24a4cb2f1dbf	1619008943149
189	1	\N	c68cce60-92fd-4b9e-afc8-d6324403b102	1619008943296
196	1	\N	a99074bf-8f9e-443c-b1ad-cbd4540821e1	1619008943433
200	1	\N	6933575f-91bd-4c58-a66a-44fd56c516d8	1619008943489
204	1	\N	6619c6c9-8f46-4e8c-99d5-e557e9a7654d	1619008943539
206	1	\N	ed564c60-d04b-4d04-a850-03179a219365	1619008943650
213	1	\N	5ad3a152-057b-40a6-94d2-92a62898e613	1619008943696
239	1	\N	9eeded17-a36a-4452-b6a3-3fec56f7ddd7	1619008944118
242	1	\N	b4dbf11f-2df1-4b2f-b0c3-79158deb4f89	1619008944186
265	1	\N	751ebb0c-870f-435c-96b6-f86089968a79	1619008945993
268	1	\N	ac986bbf-9a89-457e-96ca-3abd38f2f25a	1619008946071
272	1	\N	cec8c62a-46a4-4f7a-b339-b9e20e16f9c6	1619008946120
276	1	\N	628a8d10-6945-46e2-8b75-ce2efc39240b	1619008946210
281	1	\N	a3e53548-523f-426f-886e-5fe2f5bb81b9	1619008946341
284	1	\N	39baf06b-48b7-4fe5-8efb-7e34642c6abf	1619008946663
291	1	\N	21035186-2bc5-4738-8103-f32ed963af3f	1619008946892
295	1	\N	bafc7304-f9bd-4ce6-9b4b-45ecaad6fd3b	1619008947039
297	1	\N	fc282748-1100-461a-97d4-404611a54047	1619008947111
302	1	\N	48b02250-336a-45c4-b717-0341576d1c33	1619008947171
306	1	\N	d29b7917-d0c4-4165-ae71-b109b478b9d5	1619008947261
312	1	\N	18c43bfe-669a-4e7d-8ba7-b8ca54e554ab	1619008947371
316	1	\N	34418a15-665e-4507-8569-ebb636275c96	1619008947630
343	1	\N	f5162d9c-a779-4fc3-ba77-b9d3d7708929	1619008948430
344	1	\N	af5af2a2-496a-441f-a4e8-b003928efce7	1619008948474
348	1	\N	5883d199-56da-42ce-bd00-49462a5a96ed	1619008948554
352	1	\N	424fc241-dff4-482c-adfc-b042321021b4	1619008948611
356	1	\N	7419c86c-54cc-454a-bb69-c669722048cd	1619008948711
362	1	\N	dba8738a-ab64-4b8b-a7ab-06cb89f47ac2	1619008948763
365	1	\N	25c80ea6-d0c9-49e0-83f2-33429b3af52b	1619008948861
371	1	\N	eebc0e79-ec73-44af-ad32-777c2a10362b	1619008948908
373	1	\N	14eac98e-17f9-4a9b-bdc5-4fc63f28bf6a	1619008948995
377	1	\N	7f310dfd-5818-4791-bef3-7f0c7182233b	1619008949075
383	1	\N	341244f7-a89e-4992-aa77-3c0347349564	1619008949138
387	1	\N	a1d19a0f-6def-4fb2-b0a8-b022216a17c7	1619008949184
388	1	\N	9dd8617c-4d24-4bb9-a843-b6414a0586dc	1619008949254
392	1	\N	5cfb78bb-ec17-44be-8fcb-1ba02e360eb8	1619008949304
398	1	\N	4f8aa86e-ca5e-4b47-bf0c-d021db34e2bb	1619008949371
400	1	\N	697db89c-d2a7-4cbf-9490-a4cbf3e29228	1619008949450
404	1	\N	eb52c650-c5bf-4676-b3af-243f0ceb3188	1619008949521
410	1	\N	659fb9b8-a814-4355-a71f-6f6edcf46e49	1619008949608
418	1	\N	922a9c91-7d6d-47ce-95e7-4f1973b92fc1	1619008949733
422	1	\N	476a8c77-4151-4166-8980-e583ba3d6680	1619008949810
77	1	\N	0bf22f3b-dbc5-48df-88a7-000dc93b84ff	1619008940733
81	1	\N	92917085-52cd-4fdb-99a3-e7ba3a4b5357	1619008940835
84	1	\N	98cf8cef-653b-4e30-8821-c31fe7e71ab3	1619008941085
105	1	\N	dc4389ee-079b-4ad2-8bf2-ebc5eb5e995d	1619008941791
111	1	\N	eff95f23-de19-4af7-ae39-fe7fc19e5acf	1619008941852
113	1	\N	09a4f65e-6390-4953-9832-ce43ba9c421a	1619008941941
119	1	\N	83a6df41-1967-4c7e-bb65-b2ef5aef730b	1619008941992
121	1	\N	59825cd0-92a6-4f8f-b5c5-86fb038d2fe2	1619008942093
127	1	\N	f12e4f09-1df6-4e0f-b574-bed1007d673d	1619008942130
129	1	\N	7cf98c04-bbff-4ca6-b76e-08bb64f4dfac	1619008942209
135	1	\N	15e7aae7-dba7-46f1-b154-add4e60008f7	1619008942271
136	1	\N	8f1e2b8f-1069-4eeb-8d66-74e83af4633f	1619008942386
141	1	\N	748d9886-c1c3-4e38-97f2-b9dc5a46257a	1619008942450
145	1	\N	7ffb5936-b05f-472c-94dc-4ad09553e3a8	1619008942531
150	1	\N	06626275-ee45-4aab-9f3e-1b8d9908b3d5	1619008942586
153	1	\N	ea755185-a730-4c1a-9e09-9a7e748c3902	1619008942671
159	1	\N	cb790170-61ed-4fb9-99ea-1776c75cee43	1619008942733
161	1	\N	4dc97e0a-8834-4fb8-8dbb-e37a63c46cbd	1619008942844
166	1	\N	74eb10a6-66dc-4124-9cd1-a3bfc5a355be	1619008942901
170	1	\N	89f5a869-76e0-42e8-b59c-a5952ba28183	1619008943030
177	1	\N	54c4c127-91b5-4621-9fea-82c924797d75	1619008943078
179	1	\N	9c00df61-3ccd-491c-b947-629a366cd1f5	1619008943160
185	1	\N	292479e7-32f4-49be-94b3-1a65b641c67f	1619008943224
187	1	\N	821f82b7-ac55-448a-b1e1-4c8f0e15eb99	1619008943296
193	1	\N	3e77aae5-8e39-4844-a4eb-a737920ab167	1619008943354
195	1	\N	1f0973c3-f517-45a2-8e57-dd5f44b12290	1619008943440
201	1	\N	81351fe1-4541-43e8-82a2-efc96c29b3c4	1619008943479
203	1	\N	aabe448f-f7eb-4250-9860-4b6181a94e3f	1619008943542
209	1	\N	ec35cd56-6168-4453-be64-f7999b4e634d	1619008943607
211	1	\N	21d32922-2154-4057-a165-2a90d6a71f17	1619008943719
217	1	\N	159e4943-6c3b-4ecb-973d-4cc826736c1f	1619008943765
219	1	\N	449b9cf7-0e0f-49fd-8640-1d4122aa0e2a	1619008943843
225	1	\N	d7216af1-ffb1-413f-8fd7-51c6c5705d0a	1619008943886
227	1	\N	59b1a852-f717-4c0a-9436-37715008b2aa	1619008943963
233	1	\N	322398fe-c1a7-41ce-83b1-2da7f87fdd28	1619008944011
235	1	\N	28fc13ee-345c-4dca-8849-f5d0a41e0356	1619008944099
241	1	\N	7d8df3b9-a458-400f-8d41-9373bb6c170f	1619008944143
243	1	\N	d3c7d6d2-0b82-4593-afb6-c2e922b4bade	1619008944214
250	1	\N	665112e1-3336-499f-91ff-288244d2ba1a	1619008944272
251	1	\N	0555a617-2906-49a8-a037-01bf90495388	1619008944354
256	1	\N	990f89c1-ca1c-4630-a09f-a03a00e3591f	1619008944510
262	1	\N	4e983788-1b82-4ef2-9837-a75e13cc7c3a	1619008945958
311	1	\N	6c86f5ae-2e1a-4dcf-9eb3-c5fa3d4d1294	1619008947333
315	1	\N	3460a333-e9b3-4249-bea1-006f6d7142bc	1619008947378
319	1	\N	88d201a0-1cda-4d82-b616-614bb854b47b	1619008947653
323	1	\N	016b018d-0eb0-409c-9874-a72f5e7d1ba5	1619008947846
324	1	\N	5ee0081d-b6ca-4195-86ec-a286edca5bf8	1619008948121
330	1	\N	23d2c130-d4ea-43c0-a3b9-023c7817ff2d	1619008948186
334	1	\N	90ddd4b1-d1cd-4263-bdec-5eb76bb8b7ae	1619008948285
338	1	\N	fe9f6dda-dfac-4b8a-ab32-2d2f4e4c1524	1619008948348
342	1	\N	c114a165-15f2-4199-9a70-92974c7be6fb	1619008948454
354	1	\N	63abafb4-5dd8-49cf-bf57-f8762a85ef01	1619008948616
358	1	\N	ce52a6e9-ba6f-46c5-aed4-bacfe064e7fc	1619008948713
361	1	\N	0de0e0ab-63d6-46ff-8818-529ab3f43add	1619008948764
366	1	\N	1d6d759c-3e43-448c-9018-20291e366e84	1619008948844
423	1	\N	2a4592e0-6901-4236-8a89-3319a4d4c4d3	1619008949809
426	1	\N	701727f3-55e3-424c-b111-6ae432b8253f	1619008949858
428	1	\N	e8a930e5-3517-4ad8-99a8-006674fd501e	1619008949927
434	1	\N	46efc3c7-efd6-405f-b7d0-899e3af563b7	1619008949982
437	1	\N	c493a180-b055-4e4c-9ef6-8e6967af5e5a	1619008950075
440	1	\N	87e9c4a9-28ba-40cc-8be2-9d39da7a12ba	1619008950145
446	1	\N	c4acdbe1-7310-415b-96ed-318e6f73a9ef	1619008950220
449	1	\N	3d99d4c2-b526-422c-bf8a-5313d7cdc835	1619008950284
452	1	\N	3bf93e8d-2064-4c7b-8dae-c441f895b131	1619008950402
458	1	\N	e6850dbc-453a-46f8-9ee2-a27b47ffbbb0	1619008950450
74	1	\N	69914104-a639-4271-b55f-76cd77c6869a	1619008940719
78	1	\N	38f0d8e2-2cb8-48e2-8cfb-a80e47ce5e8f	1619008940795
82	1	\N	526fd2fb-4627-42dd-bc0a-655daab4d3aa	1619008940921
86	1	\N	5887ce34-afd8-4519-8741-607b5b21c7ac	1619008941244
90	1	\N	0782b1d7-68ef-4c17-adbf-f3c303cde6f2	1619008941378
93	1	\N	67e85c5c-6bb3-4097-a0d3-054af02d97d3	1619008941619
98	1	\N	88f561c3-562a-494d-917e-fcd94455644c	1619008941666
101	1	\N	72251e7e-39ed-4eb8-93d9-0024dd009e74	1619008941754
134	1	\N	d342f81e-35b3-4b3a-8b63-1c511e41b705	1619008942342
139	1	\N	19601579-fc08-4b32-9101-973ef6d87d97	1619008942393
143	1	\N	608d7e42-c538-4df2-883b-a6e07f37a842	1619008942481
147	1	\N	8ab76d81-f672-4157-bf26-ce7adc20ee75	1619008942537
151	1	\N	8ffecdf7-3ac0-482c-8d8b-b5e0ee40437b	1619008942616
156	1	\N	7868db48-873e-402f-90da-a3f84d49b46c	1619008942674
158	1	\N	e5d5cf04-034c-495f-a0df-c76a0e40c6b6	1619008942786
164	1	\N	7144733f-4b6e-4ce7-8776-31db8ad6ec46	1619008942855
183	1	\N	a13c9e35-9b92-42c9-bede-9d63602b340d	1619008943251
192	1	\N	b7a48bd9-7ab4-40f3-92c4-c8554434ac0c	1619008943375
246	1	\N	12499872-d2a2-4849-a685-cd2956899d82	1619008944220
248	1	\N	5066b14a-3739-4f05-87b0-164d2b80b163	1619008944298
253	1	\N	64397f9c-e086-4be0-9fc3-9e04a845cff2	1619008944367
326	1	\N	c318e367-09e6-4096-9d3c-bea3108029bb	1619008948122
353	1	\N	d48f0487-6d24-4d96-9f03-1c3b705d7335	1619008948616
357	1	\N	4468798f-f0f6-4b8f-8a77-f9223fbe3a77	1619008948714
360	1	\N	c938df86-cb1e-4a65-a7af-825452d366fd	1619008948763
364	1	\N	f1446110-1a0d-4788-aa67-930f40a5e761	1619008948844
369	1	\N	6eaad8f5-d667-421f-97f2-b7e6b0750065	1619008948909
374	1	\N	a0e9f8e1-a160-4e9f-9588-c62ba8372b47	1619008948994
378	1	\N	492f5b70-8a28-41d2-817e-8594e7c97eee	1619008949073
381	1	\N	163cb9a2-6f69-4894-929c-1b695b45a0dc	1619008949137
394	1	\N	08e06958-a65e-44c4-a6c1-326ac9fc4924	1619008949304
397	1	\N	0f42810c-4bf1-4a98-ad27-c38d24112eed	1619008949387
402	1	\N	4a819ef2-e771-4599-8733-f8d9422de578	1619008949456
407	1	\N	ee90a992-6b82-47d4-942d-43c0ea727265	1619008949523
408	1	\N	a69cc735-a90c-4fd6-b1ad-7e4c73e6f4b6	1619008949597
414	1	\N	1ce0ad62-2059-420d-9c88-d1af8818ea4b	1619008949671
454	1	\N	9fa39bad-0c04-4dcd-9584-b63f353bfa05	1619008950403
456	1	\N	a92a6a08-ba07-4e90-9650-ae38bcd41a0d	1619008950455
75	1	\N	45b5a373-7b5c-46b6-ab83-79b92c272b81	1619008940733
80	1	\N	2a844b6d-9ea8-4044-af63-e54afa549fb5	1619008940801
83	1	\N	d2d01c67-231a-4ad1-b750-517691df292e	1619008941059
87	1	\N	e39d5785-ba77-429d-b6df-f2ec8854f66c	1619008941334
94	1	\N	b7d8813c-256f-4c08-a330-30bfa207897e	1619008941600
118	1	\N	853a50da-ab95-436e-8d79-9144609cb0ce	1619008942050
123	1	\N	9ac4bc2f-9c6a-4f91-bb10-b42a10d6af6c	1619008942091
126	1	\N	fad949dd-ffc1-4e6b-ab6b-f0654374521a	1619008942162
131	1	\N	ee716906-74d0-4b5f-8915-e0463ef420b7	1619008942213
137	1	\N	f2408579-c065-4dae-8089-177c03b2df0c	1619008942385
181	1	\N	94933bba-d6fe-483e-b9ff-93a724f99d8a	1619008943163
184	1	\N	d9689212-0f3c-482b-9ba9-cc244efb3ad7	1619008943253
188	1	\N	9946ef4a-369f-488b-88a6-35ab391eb8a3	1619008943297
191	1	\N	a57e1c92-9803-4f9f-96ae-c186fdb3c879	1619008943375
197	1	\N	aca7eed0-c283-441e-8ab4-5057f8984563	1619008943433
199	1	\N	b563e3dd-dd0f-4f1e-9743-6f827c9e09bd	1619008943492
205	1	\N	f64f6dc0-8a54-405a-af8f-b6168b310347	1619008943539
303	1	\N	9a84919f-8152-4871-b48f-a63f1f697c33	1619008947173
307	1	\N	f78e9d60-f2f4-481a-ab11-118fdfe246db	1619008947236
309	1	\N	1052db10-2794-4ee0-b952-229313c162fb	1619008947317
313	1	\N	e625db23-675c-472b-a1af-710d3978a0a3	1619008947369
317	1	\N	2b68cd8c-78ad-42e5-86d3-08726b76ba54	1619008947635
321	1	\N	1d4a0369-e7c0-414c-b566-3f0352ab67ec	1619008947854
329	1	\N	1b5df134-7ce5-456a-b7fa-d298d87efca5	1619008948185
332	1	\N	3913e94e-0e0c-446d-959a-1e05265564d6	1619008948282
336	1	\N	5db4cba7-c445-4972-b8fd-75a03bf67a76	1619008948346
340	1	\N	6021d4fb-0ad3-466b-a823-89e1ef5def93	1619008948450
346	1	\N	ce7266fa-f920-4b73-8e0c-d036fe61ed39	1619008948498
351	1	\N	2c1ac2c5-4af5-4050-af2f-578df4ccfa7c	1619008948564
355	1	\N	29c848eb-4cb4-4408-9a5e-927b939c6c45	1619008948621
359	1	\N	0074155f-a23d-4ac5-9520-5c67f74d9a12	1619008948721
363	1	\N	04b1313c-63ea-47c3-87ab-4a5d0bc4330e	1619008948779
385	1	\N	bea7c58e-d920-4e95-a03a-95326ed4a9dc	1619008949184
390	1	\N	b9a95687-3441-419f-8419-61f528e849e9	1619008949253
393	1	\N	93e05e1c-b4e9-417b-81bc-64f6f3745d11	1619008949305
396	1	\N	0962305c-26a7-4e17-a74e-afdb0ed52b4b	1619008949379
441	1	\N	71f59fa3-2a14-4587-a3f9-0fb0d0c892a2	1619008950138
445	1	\N	09ce1d4d-f227-4bf5-a3d7-fbfd8c88cf65	1619008950234
450	1	\N	87ad7c8b-9010-4de0-8a39-a6b4d9afbbaa	1619008950298
455	1	\N	4586e563-0f9b-4404-8357-15b6587daa9a	1619008950407
459	1	\N	fd13b874-62f1-4bf5-a57a-e72a9d5c81ab	1619008950468
79	1	\N	a2b2fc8d-63d0-462e-bd87-5bb2f06ebe63	1619008940872
85	1	\N	5d2f5777-61f8-4a66-bb40-d64da5d24124	1619008941093
89	1	\N	8621dc1c-9720-4f67-bd9f-dd733c4c748c	1619008941319
102	1	\N	014c3c56-4811-4ab2-9486-2d0eadcd5da2	1619008941754
106	1	\N	c16294ab-0240-40b8-a1ea-0bd9f2b69ed6	1619008941798
266	1	\N	5f19ba09-4d0c-4e09-bd6e-d0ba7f6eef65	1619008946011
270	1	\N	aaf595c8-3461-443b-8e0b-0e55dda2c583	1619008946102
275	1	\N	0eb748d0-e3f5-4385-ab10-0f2cfc5f3a6e	1619008946152
279	1	\N	334fb363-7652-4c7a-8b6c-8e933ce20a1b	1619008946244
282	1	\N	4a100cf9-40be-4c0f-ba9f-51dbb3e02092	1619008946457
286	1	\N	2a9f5a8b-f40a-4e30-91ed-62ff476e5550	1619008946710
290	1	\N	6db83049-0e62-42d6-bc15-4e14327c54af	1619008946846
292	1	\N	4a72aaef-e44c-42f8-9f05-a5f3800c27d3	1619008947035
298	1	\N	40c2f9e9-74b1-4793-858f-7b7f52640339	1619008947112
301	1	\N	2276e7e1-1f34-4ae5-b566-99518e888c90	1619008947173
304	1	\N	4e1f29d5-2e4e-4a00-8e7e-4f5e1d101ed7	1619008947235
308	1	\N	6d43090c-13ff-4fda-b942-82cb1899686f	1619008947321
314	1	\N	a9698130-cc30-47b8-8812-641464452fa8	1619008947372
318	1	\N	5d0cb6f7-2c37-4e0c-ab79-ab2273b90189	1619008947641
322	1	\N	eeff18ff-260e-46d7-924d-5dcdf811388d	1619008947841
325	1	\N	c3e2b8fd-2079-4305-a1ac-447175ff5454	1619008948124
331	1	\N	6c492ec8-14a4-44ab-b976-5090d2d96854	1619008948187
367	1	\N	c2a38e5d-6729-4bca-aec2-bdfa16b4a5f7	1619008948862
401	1	\N	9b4fcb0a-8983-49d4-96d7-380084151548	1619008949451
405	1	\N	99cd7b23-8a62-4999-b7b5-6c1ca7cdf7a2	1619008949523
409	1	\N	6c0c074a-9f78-4466-a1ce-ac78c7ea6dd7	1619008949599
413	1	\N	090805b2-2a4c-4c00-958d-730f833e2ff2	1619008949671
417	1	\N	4d3c95b1-9ad5-40cd-980c-36019ccdc787	1619008949734
92	1	\N	7e560803-898a-4507-b558-6b3fa1147a4d	1619008941530
96	1	\N	fe4a690b-cc0a-47b5-94d8-89408528b304	1619008941655
100	1	\N	cc24a90d-1349-4244-b0bd-dcbbb6e47779	1619008941705
104	1	\N	5fb06416-0dc8-42b1-ab05-16e1ce74d9b9	1619008941787
108	1	\N	3fa5d861-40c0-40e8-ab26-66cb43abb2b6	1619008941830
112	1	\N	485c354e-ebf5-441e-ab76-e44c062d6ee0	1619008941927
117	1	\N	8247da77-0041-4073-839c-8ef62873640b	1619008942004
122	1	\N	eb495d62-096c-4083-99b8-3a15f33fc090	1619008942079
125	1	\N	adbf062a-f0b7-46ce-a1df-80f49a9079a1	1619008942130
130	1	\N	e3a014e3-57c9-49f0-bd57-119ed60cda8c	1619008942177
132	1	\N	17371604-694b-4e15-85f0-cea619e2e347	1619008942270
138	1	\N	f2a344fb-fce4-4b70-8996-3bdb7ef46c83	1619008942361
140	1	\N	a5637e93-2edb-47a0-a4eb-f102301a2542	1619008942450
146	1	\N	265d7d74-4ef4-46ef-9f02-52bfba6fa05f	1619008942502
148	1	\N	b43e46a4-67a6-4e75-8ead-fcf6e1b264ad	1619008942585
154	1	\N	b9f66c9a-f154-47f6-a035-b309046cb328	1619008942630
155	1	\N	8b13b701-3144-4942-b9aa-d680364aa13c	1619008942719
162	1	\N	dba53b3a-e6bd-4366-9361-efbd75c7bfca	1619008942785
163	1	\N	8485fba8-6714-4e01-aac3-fd5b6f42ec11	1619008942879
169	1	\N	d166630f-d0f2-4eaa-bc0b-09aed214b7dc	1619008942924
171	1	\N	9229e7de-b2ed-4fd9-8e1d-fbea2c05226d	1619008943012
174	1	\N	c1a7c869-3980-4a5f-8d68-78627a1db4dd	1619008943073
178	1	\N	075ec9cc-5ae5-4d9f-985b-adcae259a095	1619008943142
182	1	\N	c2551abc-3006-4c8a-8da0-6237c19e3420	1619008943198
186	1	\N	ecfd4aee-dd14-431c-bf3d-2296fd2dfefd	1619008943282
190	1	\N	151dc527-5dd2-4755-97d9-8e143ae72874	1619008943331
194	1	\N	96bfcd88-0478-4fcd-b059-03f28cb4400d	1619008943421
198	1	\N	3342bdcb-555a-4896-9412-48374172b71b	1619008943463
202	1	\N	905a6b70-42fe-47aa-9ca5-8c634e550d36	1619008943537
208	1	\N	81dbb5e0-c50b-42dc-8242-e9259cb92c65	1619008943579
210	1	\N	c02e19b6-dbc2-4615-bb12-8ba618cc0b01	1619008943690
214	1	\N	26b66b8a-1166-4a60-8f92-c6f0b4a6bb64	1619008943734
218	1	\N	06f28773-ba50-4f33-bc7d-0d31133d7ecb	1619008943807
223	1	\N	50f40330-3120-4ff9-9593-094dea65cc91	1619008943861
226	1	\N	5d506252-f00e-4c83-a035-b53a5100dc8a	1619008943938
230	1	\N	e5e86d28-ee83-4b18-a21f-da8028aeea33	1619008943984
234	1	\N	e38a0764-d51a-45c9-b6f5-d7c12c9f965d	1619008944063
263	1	\N	920bcf80-2ff7-455c-a924-989583232fda	1619008945969
283	1	\N	5585d796-0932-4dab-a633-6a85f6791116	1619008946519
287	1	\N	6b10d2bb-d4aa-4f4b-914c-43457a21f81f	1619008946802
294	1	\N	07ceaa30-31c0-43ff-8600-b34dc58cbe00	1619008947035
320	1	\N	df1be469-47a5-4f92-afc7-50c79b7675da	1619008947865
327	1	\N	29e887e7-660d-464b-ae73-0d8886f2263d	1619008948120
328	1	\N	44bc6624-cb79-4b69-8ddd-b2672263368d	1619008948184
333	1	\N	eb542dbc-3b11-4878-8448-2dba47886559	1619008948281
337	1	\N	564fa6b1-2af5-4dd1-a7e5-a26ffdcb1d3a	1619008948348
341	1	\N	5a66ee25-70ec-4634-943f-1fcd21dae33e	1619008948447
345	1	\N	ac3356a0-24fc-479a-87e9-af6e9b85692a	1619008948495
349	1	\N	793ab28a-b9e4-4894-a3ca-41737c3a005c	1619008948563
370	1	\N	ea80c344-c11a-4c39-b717-53b8a799d88a	1619008948911
375	1	\N	edda408c-1170-47c0-be9a-d77d655cee30	1619008948995
424	1	\N	e423f5ce-1bc4-4abd-aac1-8e6e5a07ef63	1619008949858
429	1	\N	51990f41-1555-4fb8-baad-796c0d347f4c	1619008949926
433	1	\N	14a62eeb-4985-454d-b022-41ff5bc84367	1619008949990
436	1	\N	3b1b699f-4f03-48cc-8337-2bcbce007145	1619008950079
443	1	\N	3a47f4e9-45d0-4a1f-acd4-b0b412ea7ef2	1619008950151
97	1	\N	2f7a3f5e-5436-4635-93c6-4f3aa1bae32b	1619008941664
103	1	\N	b8be5143-48bd-4a74-b0be-4cb5cd2c3245	1619008941712
110	1	\N	9f2b3771-580e-4aa5-a5aa-e3f03c87c123	1619008941892
115	1	\N	825de5b0-df48-49f1-a3bc-3c36802e31c6	1619008941942
142	1	\N	49e59614-3a3f-40ac-a377-0c33c8ed3c47	1619008942429
144	1	\N	0c68ad78-7fa7-4a35-bd73-c55ec35524ca	1619008942514
149	1	\N	75b35ef7-da8a-4fde-8767-42b39330db8f	1619008942567
152	1	\N	361443e3-6e3d-4b70-8b19-87d6eb2e33db	1619008942664
157	1	\N	d0d8894f-8826-46a7-b343-aed0b091dd17	1619008942718
160	1	\N	62441a3a-2cbd-4b77-a7e4-1cfef4c7f690	1619008942814
165	1	\N	fe9c746c-bec4-4489-bf8c-55867b19453b	1619008942873
168	1	\N	a5a03b9d-915a-4a2e-bc51-be10f93664db	1619008942951
172	1	\N	e684abb0-cf1f-4f58-8e38-4bbbb959df54	1619008943028
176	1	\N	ff9a94bf-c5fa-4bb3-b32a-219c73dd0158	1619008943107
207	1	\N	310871f1-8b0c-43e0-8b7f-95fa24922214	1619008943649
212	1	\N	4797e9f2-236e-444f-959f-7d1f0a3d5a25	1619008943694
215	1	\N	2906fc6d-b3c6-4adf-b313-9211ea24651e	1619008943762
220	1	\N	b135a4e2-651e-4c4f-8a7c-2620a4c7bef5	1619008943809
222	1	\N	a54b4c8e-f8f2-4b05-b635-306f4db3ac17	1619008943882
228	1	\N	b2bb4512-bea6-4cf6-af11-eb8ab79973c2	1619008943945
231	1	\N	98675465-17b4-449b-ad63-389704acdf3b	1619008944019
236	1	\N	0ce6ee91-d0e4-4f85-b05d-0475c0537f3d	1619008944064
238	1	\N	f4799bd3-4055-4ada-9d69-c3528588ba9b	1619008944153
244	1	\N	80069076-0db3-41aa-bd8e-6beaa2e3ed57	1619008944198
247	1	\N	b9548876-a2de-43fa-bdb4-078a108f68c9	1619008944279
252	1	\N	9463973b-73a0-4e92-985f-2d49cd10953a	1619008944330
255	1	\N	7c49baee-f9eb-48b7-9e1a-990df1e2a791	1619008944568
258	1	\N	02c416f5-3e88-4622-a0c9-230e1e745ee8	1619008944699
261	1	\N	69733722-4166-45e2-963d-2383685ef10d	1619008945970
267	1	\N	cb72043a-f2c0-46d5-b06b-3711e642e23d	1619008946012
271	1	\N	a9ca5ece-787f-4cee-905e-198708fd3803	1619008946100
274	1	\N	84e01397-4593-4849-a90d-3e0cf1c6c373	1619008946152
278	1	\N	a29e2cf8-8edf-4e3b-a924-6a880c1a7bba	1619008946243
289	1	\N	455372bc-57e7-4408-939e-8a9477679a12	1619008946840
293	1	\N	98a69e47-b162-403f-9421-6ce3f1e991d0	1619008947035
296	1	\N	200ed7f7-16af-4cbd-a9d9-1f4b343210c1	1619008947111
300	1	\N	0e74356f-f948-4258-9775-5e52af57da1b	1619008947174
305	1	\N	0d8ff91c-40b8-4a98-b056-1f5c983482a5	1619008947237
310	1	\N	aa804e3f-cc32-47e2-9137-daccf89a0030	1619008947318
347	1	\N	09cb2138-a6bf-4955-a1ae-8439d01c551e	1619008948498
350	1	\N	fa025631-8077-4040-a80c-ae329dcdca91	1619008948565
379	1	\N	1c82f2d9-3d0a-45d5-a7d7-c26e6d4567a6	1619008949075
382	1	\N	8964e9fc-8af5-4455-b25d-78ad3b16b03b	1619008949138
386	1	\N	73d235ac-89bd-4911-b830-3d9ef8afcd59	1619008949184
389	1	\N	5678e648-96f6-4c23-bf9c-981878ae8853	1619008949251
403	1	\N	d6632786-1950-4cc3-bbc6-072d35b170f6	1619008949454
406	1	\N	fdc61652-a4e9-42fc-96cf-47ae52a889d2	1619008949550
411	1	\N	d0259d2a-6699-477d-874a-5ff93aa01ef2	1619008949610
415	1	\N	3ccbb855-2090-4f44-8d8d-d1b22cdccb53	1619008949670
419	1	\N	425e4c2c-b7c2-41a6-93d1-9fc771cea209	1619008949727
421	1	\N	c185b045-3395-443c-92ba-650500298953	1619008949809
427	1	\N	cebae2d2-b15f-4ca3-a7de-6848b340fd86	1619008949864
447	1	\N	c4420be1-8bc8-4f0a-8f3f-9d7aff8e6e46	1619008950238
451	1	\N	c514157d-8f15-4fd2-ba7e-da1812720cf0	1619008950304
216	1	\N	9e52fbd3-bf19-48fa-811a-fca6d3759ce5	1619008943774
221	1	\N	da5fcd8f-c9d2-4fbb-99fe-0bed7dfeb25f	1619008943823
224	1	\N	e2a61fab-7333-4e95-a69b-70300d40e975	1619008943898
229	1	\N	7a81392c-f8e6-4646-80bc-6f571abbd302	1619008943958
232	1	\N	c17faf11-8484-4ade-85a6-3eacfbdbc2b9	1619008944043
237	1	\N	3f1318c7-609d-4cbc-8636-d539bdd854a9	1619008944103
240	1	\N	d3c78665-10d3-42d6-b766-eca39a1b9424	1619008944182
245	1	\N	35bec6e4-23ee-44b6-b489-292cdf3c5d78	1619008944221
249	1	\N	1f4d2ba5-e64f-4c85-8a86-1ad89f969e6d	1619008944299
254	1	\N	6ec37795-f0f7-40b5-9df3-98b74fcef09f	1619008944359
257	1	\N	e175354b-71ea-4ab8-abd2-26b4368b262b	1619008944585
259	1	\N	e162fd06-0239-4106-a622-fae9ef8cc486	1619008944688
260	1	\N	25b95f1b-a109-4afd-81ff-9df961ff0925	1619008945952
264	1	\N	517f8910-152e-4f02-b46b-d6f3c9357e30	1619008946006
269	1	\N	416ceb63-a250-48ca-bfb6-037e0b8f2373	1619008946081
273	1	\N	60c6206c-3bf4-462a-b838-1cb77e9af196	1619008946131
277	1	\N	abd5e72f-bc69-4ed5-819a-7466fb7a03bf	1619008946202
280	1	\N	1dab92fc-8a44-4ff7-8138-4b0e53390294	1619008946353
285	1	\N	1a141e09-6988-4b1a-83a2-5f9f790e0d5b	1619008946643
288	1	\N	6f38ee76-e709-4e5c-90df-8e676f7c6fbd	1619008946829
299	1	\N	4ea7a0ec-1d50-438e-8a81-4766eaff3d5f	1619008947114
335	1	\N	bc643fad-9769-48a9-a48c-0c1872b0ba98	1619008948285
339	1	\N	ec8fb0bc-42cc-412f-9724-84dcef419f5f	1619008948350
368	1	\N	0390a6eb-31ea-4c25-a207-bb5d904cbc68	1619008948899
372	1	\N	2e0c9ef2-91ac-42b1-957e-e39310fb1564	1619008948992
376	1	\N	407551a6-5cb4-46c1-81a0-2e2ea59c07e0	1619008949071
380	1	\N	6e8eeb5c-fbfd-4dc3-99e7-e21803469678	1619008949136
384	1	\N	e9e43bd3-fe34-4579-a0a2-56862d388fc4	1619008949184
391	1	\N	d6acd685-f28c-4016-a472-cdb5037cedc3	1619008949254
395	1	\N	98b7359b-7023-40e9-a45b-1fa1ba281288	1619008949305
399	1	\N	fdaade0d-f01d-446b-a829-9d0b513a5cae	1619008949387
416	1	\N	72d54b7a-ad60-427e-b4b0-21af78150fb3	1619008949717
420	1	\N	f5a8dd03-e631-420c-9cf5-35bed49c9939	1619008949810
425	1	\N	86fb12c7-6ece-45f5-86fd-4817fd6d6676	1619008949860
430	1	\N	2913bc52-ccbf-4aea-be1f-43b33fb4394c	1619008949923
432	1	\N	985e721c-5fd6-4673-9a3e-2b03109c97e3	1619008949992
438	1	\N	443a3bf8-bf07-4348-ad2e-dda14b2ee215	1619008950086
448	1	\N	ee70ddb6-b9c7-49c8-9062-754e1831b9d8	1619008950284
453	1	\N	c9c75061-ac35-465d-99b7-8d50352dd53b	1619008950400
457	1	\N	0439f741-371b-4880-a8c6-5735fcf73631	1619008950449
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

SELECT pg_catalog.setval('public.alf_access_control_entry_seq', 25, true);


--
-- Name: alf_access_control_list_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_access_control_list_seq', 69, true);


--
-- Name: alf_ace_context_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_ace_context_seq', 1, false);


--
-- Name: alf_acl_change_set_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_acl_change_set_seq', 12, true);


--
-- Name: alf_acl_member_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_acl_member_seq', 193, true);


--
-- Name: alf_activity_feed_control_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_activity_feed_control_seq', 1, false);


--
-- Name: alf_activity_feed_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_activity_feed_seq', 3, true);


--
-- Name: alf_activity_post_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_activity_post_seq', 3, true);


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

SELECT pg_catalog.setval('public.alf_auth_status_seq', 2, true);


--
-- Name: alf_authority_alias_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_authority_alias_seq', 1, false);


--
-- Name: alf_authority_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_authority_seq', 16, true);


--
-- Name: alf_child_assoc_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_child_assoc_seq', 1180, true);


--
-- Name: alf_content_data_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_content_data_seq', 288, true);


--
-- Name: alf_content_url_enc_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_content_url_enc_seq', 1, false);


--
-- Name: alf_content_url_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_content_url_seq', 288, true);


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

SELECT pg_catalog.setval('public.alf_lock_resource_seq', 22, true);


--
-- Name: alf_lock_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_lock_seq', 30, true);


--
-- Name: alf_mimetype_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_mimetype_seq', 14, true);


--
-- Name: alf_namespace_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_namespace_seq', 21, true);


--
-- Name: alf_node_assoc_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_node_assoc_seq', 20, true);


--
-- Name: alf_node_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_node_seq', 1138, true);


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

SELECT pg_catalog.setval('public.alf_prop_root_seq', 25, true);


--
-- Name: alf_prop_serializable_value_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_prop_serializable_value_seq', 2, true);


--
-- Name: alf_prop_string_value_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_prop_string_value_seq', 36, true);


--
-- Name: alf_prop_unique_ctx_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_prop_unique_ctx_seq', 19, true);


--
-- Name: alf_prop_value_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_prop_value_seq', 52, true);


--
-- Name: alf_qname_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_qname_seq', 273, true);


--
-- Name: alf_server_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_server_seq', 1, false);


--
-- Name: alf_store_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_store_seq', 6, true);


--
-- Name: alf_transaction_seq; Type: SEQUENCE SET; Schema: public; Owner: alfresco
--

SELECT pg_catalog.setval('public.alf_transaction_seq', 459, true);


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
-- Name: alf_server alf_server_pkey; Type: CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_server
    ADD CONSTRAINT alf_server_pkey PRIMARY KEY (id);


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
-- Name: fk_alf_txn_svr; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE INDEX fk_alf_txn_svr ON public.alf_transaction USING btree (server_id);


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
-- Name: ip_address; Type: INDEX; Schema: public; Owner: alfresco
--

CREATE UNIQUE INDEX ip_address ON public.alf_server USING btree (ip_address);


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


/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


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
-- Name: alf_transaction fk_alf_txn_svr; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_transaction
    ADD CONSTRAINT fk_alf_txn_svr FOREIGN KEY (server_id) REFERENCES public.alf_server(id);


--
-- Name: alf_usage_delta fk_alf_usaged_n; Type: FK CONSTRAINT; Schema: public; Owner: alfresco
--

ALTER TABLE ONLY public.alf_usage_delta
    ADD CONSTRAINT fk_alf_usaged_n FOREIGN KEY (node_id) REFERENCES public.alf_node(id);


--
-- PostgreSQL database dump complete
--
