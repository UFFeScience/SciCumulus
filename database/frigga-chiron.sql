--
-- postgresQL database dump
--

-- Dumped from database version 9.1.3
-- Dumped by pg_dump version 9.1.3
-- Started on 2013-03-11 16:46:06 CET

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 182 (class 3079 OID 11907)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--
--CREATE LANGUAGE plpgsql;


--
-- TOC entry 2254 (class 0 OID 0)
-- Dependencies: 182
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- TOC entry 194 (class 1255 OID 79961)
-- Dependencies: 563 6
-- Name: f_emachine(integer, character varying, character varying, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_emachine(v_machineid integer, v_hostname character varying, v_address character varying, v_mflopspersecond double precision) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare d_machineid integer;
begin
    select v_machineid into d_machineid;
 if (coalesce(d_machineid, 0) = 0) then
select nextval('machineid_seq') into d_machineid;
        insert into emachine(machineid, hostname, address, mflopspersecond) values(d_machineid, v_hostname, v_address, v_mflopspersecond);
else
    update emachine set hostname = v_hostname, address = v_address, mflopspersecond = v_mflopspersecond where machineid = d_machineid;
end if;
 return d_machineid;
end;
$$;

ALTER FUNCTION public.f_emachine(v_machineid integer, v_hostname character varying, v_address character varying, v_mflopspersecond double precision) OWNER TO chiron;

--
-- TOC entry 194 (class 1255 OID 79961)
-- Dependencies: 563 6
-- Name: f_activation(integer, integer, integer, integer, character varying, character varying, text, text, timestamp with time zone, timestamp with time zone, character varying, text, character, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_activation(v_taskid integer, v_actid integer, v_machineid integer, v_exitstatus integer, v_commandline character varying, v_workspace character varying, v_failure_tries integer, v_terr text, v_tout text, v_starttime timestamp with time zone, v_endtime timestamp with time zone, v_status character varying, v_extractor text, v_constrained character, v_templatedir text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare d_taskid integer;
begin
    select v_taskid into d_taskid;
 if (coalesce(d_taskid, 0) = 0) then
select nextval('taskid_seq') into d_taskid;
        insert into eactivation(taskid, actid, machineid, exitstatus, commandline, workspace, failure_tries, terr, tout, starttime, endtime, status, extractor, constrained, templatedir) values(d_taskid, v_actid, v_machineid, v_exitstatus, v_commandline, v_workspace, v_failure_tries, v_terr, v_tout, v_starttime, v_endtime, v_status, v_extractor, v_constrained, v_templatedir);
else
    update eactivation set actid = v_actid, machineid = v_machineid, exitstatus = v_exitstatus, commandline = v_commandline, workspace = v_workspace, failure_tries = v_failure_tries, terr = v_terr, tout = v_tout, starttime = v_starttime, endtime = v_endtime, status = v_status, extractor = v_extractor, templatedir = v_templatedir where taskid = d_taskid;
end if;
 return d_taskid;
end;
$$;


ALTER FUNCTION public.f_activation(v_taskid integer, v_actid integer, v_processor integer, v_exitstatus integer, v_commandline character varying, v_workspace character varying, v_failure_tries integer, v_terr text, v_tout text, v_starttime timestamp with time zone, v_endtime timestamp with time zone, v_status character varying, v_extractor text, v_constrained character, v_templatedir text) OWNER TO chiron;

--
-- TOC entry 195 (class 1255 OID 79962)
-- Dependencies: 6 563
-- Name: f_activity(integer, integer, character varying, character varying, timestamp with time zone, timestamp with time zone, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_activity(v_actid integer, v_wkfid integer, v_tag character varying, v_status character varying, v_starttime timestamp with time zone, v_endtime timestamp with time zone, v_cactid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare d_actid integer;
begin
 select v_actid into d_actid;
 if (coalesce(d_actid, 0) = 0) then
   select nextval('actid_seq') into d_actid;
   insert into eactivity(actid, wkfid, tag, status, starttime, endtime, cactid) values(d_actid, v_wkfid, v_tag, v_status, v_starttime, v_endtime, v_cactid);
 else 
   update eactivity set status = v_status, starttime = v_starttime, endtime = v_endtime where actid = d_actid;
 end if;
 return d_actid;
end;
$$;


ALTER FUNCTION public.f_activity(v_actid integer, v_wkfid integer, v_tag character varying, v_status character varying, v_starttime timestamp with time zone, v_endtime timestamp with time zone, v_cactid integer) OWNER TO chiron;

--
-- TOC entry 196 (class 1255 OID 79963)
-- Dependencies: 6 563
-- Name: f_cactivity(integer, integer, character varying, character varying, character varying, character varying, character varying, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_cactivity(v_actid integer, v_wkfid integer, v_tag character varying, v_atype character varying, v_description character varying, v_activation character varying, v_extractor character varying, v_templatedir text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare d_actid integer;
begin
 select v_actid into d_actid;
 if (coalesce(d_actid, 0) = 0) then
   select nextval('actid_seq') into d_actid;
   insert into cactivity(actid, wkfid, tag, atype, description, activation, extractor, templatedir) values(d_actid, v_wkfid, v_tag, v_atype, v_description, v_activation, v_extractor, v_templatedir);
 else 
   update cactivity set atype = v_atype, description = v_description, templatedir = v_templatedir, activation = v_activation, extractor = v_extractor where actid = d_actid;
 end if;
 return d_actid;
end;
$$;


ALTER FUNCTION public.f_cactivity(v_actid integer, v_wkfid integer, v_tag character varying, v_atype character varying, v_description character varying, v_activation character varying, v_extractor character varying, v_templatedir text) OWNER TO chiron;

--
-- TOC entry 197 (class 1255 OID 79964)
-- Dependencies: 563 6
-- Name: f_crelation(integer, character varying, character varying, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_crelation(v_actid integer, v_rtype character varying, v_rname character varying, v_dep integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare d_relid integer;
begin
 select nextval('relid_seq') into d_relid;
 insert into crelation(relid, actid, rtype, rname, dependency) values(d_relid, v_actid, v_rtype, v_rname, v_dep);
 return d_relid;
end;
$$;


ALTER FUNCTION public.f_crelation(v_actid integer, v_rtype character varying, v_rname character varying, v_dep integer) OWNER TO chiron;

--
-- TOC entry 198 (class 1255 OID 79965)
-- Dependencies: 563 6
-- Name: f_cworkflow(integer, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_cworkflow(v_wkfid integer, v_tag character varying, v_description character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare d_wkfid integer;
begin
 select v_wkfid into d_wkfid;
 if (coalesce(d_wkfid, 0) = 0) then
   select nextval('wkfid_seq') into d_wkfid;
   insert into cworkflow(wkfid, tag, description) values(d_wkfid, v_tag, v_description);
 end if;
 return d_wkfid;
end;
$$;


ALTER FUNCTION public.f_cworkflow(v_wkfid integer, v_tag character varying, v_description character varying) OWNER TO chiron;

--
-- TOC entry 199 (class 1255 OID 79966)
-- Dependencies: 563 6
-- Name: f_del_workflow(character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_del_workflow(v_tagexec character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
begin
    update eworkflow set tag = 'lixo' where tagexec = v_tagexec;
 return 0;
end;
$$;


ALTER FUNCTION public.f_del_workflow(v_tagexec character varying) OWNER TO chiron;

--
-- TOC entry 200 (class 1255 OID 79967)
-- Dependencies: 6 563
-- Name: f_del_workflows(character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_del_workflows(v_tag character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
begin
	delete from efile where exists (
		select * from eactivity a, eworkflow w where a.actid = efile.actid	and a.wkfid = w.wkfid	and w.tag = v_tag	
	);

	delete from eactivation where actid in 
	(
		select a.actid	from eactivity a, eworkflow w where a.wkfid = w.wkfid and w.tag = v_tag
	);

	delete from efield where actid in (	
		select actid from eactivity a, eworkflow w where a.wkfid = w.wkfid and w.tag = v_tag
	);

	delete from erelation where actid in (
		select actid from eactivity a, eworkflow w where a.wkfid = w.wkfid and w.tag = v_tag );

	delete from eactivity where wkfid in (
		select w.wkfid	from eworkflow w where w.tag = v_tag);

	delete from eworkflow where tag = v_tag;
 return 0;
end;
$$;


ALTER FUNCTION public.f_del_workflows(v_tag character varying) OWNER TO chiron;

--
-- TOC entry 201 (class 1255 OID 79968)
-- Dependencies: 6 563
-- Name: f_ekeyspace(integer, integer, character varying, integer, integer, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_ekeyspace(v_taskid integer, v_actid integer, v_relationname character varying, v_iik integer, v_fik integer, v_relationtype character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare d_taskid integer;
begin
    select into d_taskid taskid from ekeyspace where taskid = v_taskid and actid = v_actid and relationname = v_relationname;
 if (coalesce(d_taskid, 0) = 0) then
        insert into ekeyspace(taskid, actid, relationname, iik, fik, relationtype) values(v_taskid, v_actid, v_relationname, v_iik, v_fik, v_relationtype);
else
    update ekeyspace set iik = v_iik, fik = v_fik where taskid = v_taskid and actid = v_actid and relationname = v_relationname;
    end if;
 return d_taskid;
end;
$$;


ALTER FUNCTION public.f_ekeyspace(v_taskid integer, v_actid integer, v_relationname character varying, v_iik integer, v_fik integer, v_relationtype character varying) OWNER TO chiron;

--
-- TOC entry 202 (class 1255 OID 79969)
-- Dependencies: 6 563
-- Name: f_file(integer, integer, integer, character varying, character varying, character varying, character varying, integer, timestamp with time zone, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_file(v_fileid integer, v_actid integer, v_taskid integer, v_ftemplate character varying, v_finstrumented character varying, v_fdir character varying, v_fname character varying, v_fsize integer, v_fdata timestamp with time zone, v_foper character varying, v_fieldname character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare d_fileid integer;
begin
    select v_fileid into d_fileid;
if (coalesce(d_fileid, 0) = 0) then
select nextval('fileid_seq') into d_fileid;
insert into efile(fileid, actid, taskid, fdir, fname, fsize, fdata, ftemplate, finstrumented, foper, fieldname) values(d_fileid, v_actid, v_taskid, v_fdir, v_fname, v_fsize, v_fdata, v_ftemplate, v_finstrumented, v_foper, v_fieldname);
else 
update efile set ftemplate = v_ftemplate, finstrumented = v_finstrumented, fdir = v_fdir, fname = v_fname, fsize = v_fsize, fdata = v_fdata where d_fileid = fileid;
end if;
return d_fileid;
end;
$$;


ALTER FUNCTION public.f_file(v_fileid integer, v_actid integer, v_taskid integer, v_ftemplate character varying, v_finstrumented character varying, v_fdir character varying, v_fname character varying, v_fsize integer, v_fdata timestamp with time zone, v_foper character varying, v_fieldname character varying) OWNER TO chiron;

--
-- TOC entry 203 (class 1255 OID 79970)
-- Dependencies: 6 563
-- Name: f_relation(integer, integer, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_relation(v_relid integer, v_actid integer, v_rtype character varying, v_rname character varying, v_filename character varying, v_dependency character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare d_relid integer;
begin
 select v_relid into d_relid;
 if (d_relid is null) then
   select nextval('relid_seq') into d_relid;
   insert into erelation(relid, actid, rtype, rname, filename, dependency) values(d_relid, v_actid, v_rtype, v_rname, v_filename, v_dependency);
 end if;
 return d_relid;
end;
$$;


ALTER FUNCTION public.f_relation(v_relid integer, v_actid integer, v_rtype character varying, v_rname character varying, v_filename character varying, v_dependency character varying) OWNER TO chiron;

--
-- TOC entry 204 (class 1255 OID 79971)
-- Dependencies: 563 6
-- Name: f_workflow(integer, character varying, character varying, character varying, character varying, integer, character varying, double precision, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION f_workflow(v_wkfid integer, v_tag character varying, v_tagexec character varying, v_expdir character varying, v_wfdir character varying, v_maximumfailures integer, v_userinteraction character varying, v_reliability double precision, v_redundancy character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare d_wkfid integer;
begin
 select v_wkfid into d_wkfid;
 if (coalesce(d_wkfid, 0) = 0) then
   select nextval('wkfid_seq') into d_wkfid;
   insert into eworkflow(ewkfid, tag, tagexec, expdir, wfdir, maximumfailures, userinteraction, reliability, redundancy) values(d_wkfid, v_tag, v_tagexec, v_expdir, v_wfdir, v_maximumfailures, v_userinteraction, v_reliability, v_redundancy);
 end if;
 return d_wkfid;
end;
$$;


ALTER FUNCTION public.f_workflow(v_wkfid integer, v_tag character varying, v_tagexec character varying, v_expdir character varying, v_wfdir character varying, v_maximumfailures integer, v_userinteraction character varying, v_reliability double precision, v_redundancy character varying) OWNER TO chiron;

--
-- TOC entry 161 (class 1259 OID 79972)
-- Dependencies: 6
-- Name: actid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE actid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.actid_seq OWNER TO chiron;

--
-- TOC entry 162 (class 1259 OID 79974)
-- Dependencies: 6
-- Name: cactid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE cactid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cactid_seq OWNER TO chiron;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 163 (class 1259 OID 79976)
-- Dependencies: 2189 6
-- Name: cactivity; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE cactivity (
    actid integer DEFAULT nextval(('cactid_seq'::text)::regclass) NOT NULL,
    wkfid integer NOT NULL,
    tag character varying(50) NOT NULL,
    atype character varying(25) NOT NULL,
    description character varying(250),
    activation text,
    extractor text,
    constrained character varying(1),
    templatedir text
);


ALTER TABLE public.cactivity OWNER TO chiron;

--
-- TOC entry 164 (class 1259 OID 79983)
-- Dependencies: 6
-- Name: cfield; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE cfield (
    fname character varying(20) NOT NULL,
    relid integer NOT NULL,
    ftype character varying(10) NOT NULL,
    decimalplaces integer,
    fileoperation character varying(20),
    instrumented character varying(5)
);


ALTER TABLE public.cfield OWNER TO chiron;

--
-- TOC entry 165 (class 1259 OID 79986)
-- Dependencies: 2190 6
-- Name: coperand; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE coperand (
    opid integer DEFAULT nextval(('copid_seq'::text)::regclass) NOT NULL,
    actid integer NOT NULL,
    oname character varying(100),
    numericvalue double precision,
    textvalue character varying(100)
);


ALTER TABLE public.coperand OWNER TO chiron;

--
-- TOC entry 166 (class 1259 OID 79990)
-- Dependencies: 6
-- Name: copid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE copid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.copid_seq OWNER TO chiron;

--
-- TOC entry 167 (class 1259 OID 79992)
-- Dependencies: 2191 6
-- Name: crelation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE crelation (
    relid integer DEFAULT nextval(('relid_seq'::text)::regclass) NOT NULL,
    actid integer NOT NULL,
    rtype character varying(10),
    rname character varying(100),
    dependency integer
);


ALTER TABLE public.crelation OWNER TO chiron;

--
-- TOC entry 168 (class 1259 OID 79996)
-- Dependencies: 6
-- Name: cwkfid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE cwkfid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cwkfid_seq OWNER TO chiron;

--
-- TOC entry 169 (class 1259 OID 79998)
-- Dependencies: 2192 6
-- Name: cworkflow; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE cworkflow (
    wkfid integer DEFAULT nextval(('cwkfid_seq'::text)::regclass) NOT NULL,
    tag character varying(200) NOT NULL,
    description character varying(100)
);


ALTER TABLE public.cworkflow OWNER TO chiron;

--
-- TOC entry 170 (class 1259 OID 80002)
-- Dependencies: 2193 6
-- Name: eactivation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE eactivation (
    taskid integer NOT NULL,
    actid integer NOT NULL,
    machineid integer,
    exitstatus integer,
    commandline text,
    workspace character varying(150),
    failure_tries integer,
    terr text,
    tout text,
    starttime timestamp with time zone,
    endtime timestamp with time zone,
    status character varying(25),
    extractor text,
    constrained character(1) DEFAULT 'F'::bpchar,
    templatedir text
);


ALTER TABLE public.eactivation OWNER TO chiron;

--
-- TOC entry 171 (class 1259 OID 80009)
-- Dependencies: 2194 6
-- Name: eactivity; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE eactivity (
    actid integer DEFAULT nextval(('cwkfid_seq'::text)::regclass) NOT NULL,
    wkfid integer NOT NULL,
    tag character varying(50) NOT NULL,
    status character varying(25),
    starttime timestamp with time zone,
    endtime timestamp with time zone,
    cactid integer,
    templatedir text
);


ALTER TABLE public.eactivity OWNER TO chiron;

--
-- TOC entry 172 (class 1259 OID 80016)
-- Dependencies: 2195 2196 2197 6
-- Name: efile; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE efile (
    fileid integer DEFAULT nextval(('fileid_seq'::text)::regclass) NOT NULL,
    actid integer NOT NULL,
    taskid integer,
    ftemplate character(1) DEFAULT 'F'::bpchar,
    finstrumented character(1) DEFAULT 'F'::bpchar,
    fdir character varying(500),
    fname character varying(500),
    fsize bigint,
    fdata timestamp with time zone,
    foper character varying(20),
    fieldname character varying(30)
);


ALTER TABLE public.efile OWNER TO chiron;

--
-- TOC entry 173 (class 1259 OID 80025)
-- Dependencies: 6
-- Name: ekeyspace; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE ekeyspace (
    taskid integer NOT NULL,
    actid integer NOT NULL,
    relationname character varying(30) NOT NULL,
    iik integer,
    fik integer,
    relationtype character varying(6)
);


ALTER TABLE public.ekeyspace OWNER TO chiron;

--
-- TOC entry 174 (class 1259 OID 80028)
-- Dependencies: 2198 6
-- Name: emachine; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE emachine (
    machineid integer DEFAULT nextval(('machineid_seq'::text)::regclass) NOT NULL,
    hostname character varying(250) NOT NULL,
    address character varying(250),
    mflopspersecond double precision
);


ALTER TABLE public.emachine OWNER TO chiron;

--
-- TOC entry 175 (class 1259 OID 80035)
-- Dependencies: 2199 6
-- Name: eworkflow; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE eworkflow (
    ewkfid integer DEFAULT nextval(('wkfid_seq'::text)::regclass) NOT NULL,
    tagexec character varying(200) NOT NULL,
    expdir character varying(150),
    wfdir character varying(200),
    tag character varying(200) NOT NULL,
    maximumfailures integer,
    userinteraction character(1) DEFAULT 'F'::bpchar,
    reliability double precision,
    redundancy character(1) DEFAULT 'F'::bpchar
);


ALTER TABLE public.eworkflow OWNER TO chiron;

--
-- TOC entry 176 (class 1259 OID 80042)
-- Dependencies: 6
-- Name: fieldid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE fieldid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.fieldid_seq OWNER TO chiron;

--
-- TOC entry 177 (class 1259 OID 80044)
-- Dependencies: 6
-- Name: fileid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE fileid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.fileid_seq OWNER TO chiron;

--
-- TOC entry 178 (class 1259 OID 80046)
-- Dependencies: 6
-- Name: machineid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE machineid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.machineid_seq OWNER TO chiron;

--
-- TOC entry 179 (class 1259 OID 80048)
-- Dependencies: 6
-- Name: relid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE relid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.relid_seq OWNER TO chiron;

--
-- TOC entry 180 (class 1259 OID 80050)
-- Dependencies: 6
-- Name: taskid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE taskid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.taskid_seq OWNER TO chiron;

--
-- TOC entry 181 (class 1259 OID 80052)
-- Dependencies: 6
-- Name: wkfid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE wkfid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.wkfid_seq OWNER TO chiron;

--
-- TOC entry 2203 (class 2606 OID 80055)
-- Dependencies: 163 163 163
-- Name: cactivity_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY cactivity
    ADD CONSTRAINT cactivity_pkey PRIMARY KEY (wkfid, actid);


--
-- TOC entry 2206 (class 2606 OID 80057)
-- Dependencies: 164 164 164
-- Name: cfield_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY cfield
    ADD CONSTRAINT cfield_pk PRIMARY KEY (relid, fname);

ALTER TABLE cfield CLUSTER ON cfield_pk;


--
-- TOC entry 2209 (class 2606 OID 80059)
-- Dependencies: 165 165 165
-- Name: coperand_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY coperand
    ADD CONSTRAINT coperand_pk PRIMARY KEY (actid, opid);


--
-- TOC entry 2211 (class 2606 OID 80061)
-- Dependencies: 167 167 167
-- Name: crelation_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY crelation
    ADD CONSTRAINT crelation_pk PRIMARY KEY (actid, relid);


--
-- TOC entry 2216 (class 2606 OID 80063)
-- Dependencies: 169 169
-- Name: cworkflow_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY cworkflow
    ADD CONSTRAINT cworkflow_pk PRIMARY KEY (wkfid);

ALTER TABLE cworkflow CLUSTER ON cworkflow_pk;


--
-- TOC entry 2224 (class 2606 OID 80065)
-- Dependencies: 171 171 171
-- Name: eactivity_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY eactivity
    ADD CONSTRAINT eactivity_pkey PRIMARY KEY (wkfid, actid);

ALTER TABLE eactivity CLUSTER ON eactivity_pkey;


--
-- TOC entry 2229 (class 2606 OID 80067)
-- Dependencies: 172 172 172
-- Name: efile_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY efile
    ADD CONSTRAINT efile_pk PRIMARY KEY (actid, fileid);

ALTER TABLE efile CLUSTER ON efile_pk;


--
-- TOC entry 2231 (class 2606 OID 80069)
-- Dependencies: 173 173 173 173
-- Name: ekeyspace_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ekeyspace
    ADD CONSTRAINT ekeyspace_pk PRIMARY KEY (taskid, actid, relationname);


--
-- TOC entry 2233 (class 2606 OID 80071)
-- Dependencies: 174 174
-- Name: emachineid_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY emachine
    ADD CONSTRAINT emachineid_pk PRIMARY KEY (machineid);


--
-- TOC entry 2220 (class 2606 OID 80073)
-- Dependencies: 170 170 170
-- Name: etask_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY eactivation
    ADD CONSTRAINT etask_pk PRIMARY KEY (actid, taskid);

ALTER TABLE eactivation CLUSTER ON etask_pk;


--
-- TOC entry 2235 (class 2606 OID 80075)
-- Dependencies: 175 175
-- Name: eworkflow_pk; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY eworkflow
    ADD CONSTRAINT eworkflow_pk PRIMARY KEY (ewkfid);

ALTER TABLE eworkflow CLUSTER ON eworkflow_pk;


--
-- TOC entry 2200 (class 1259 OID 80076)
-- Dependencies: 163
-- Name: c_activity_wkfid; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX c_activity_wkfid ON cactivity USING btree (wkfid);


--
-- TOC entry 2204 (class 1259 OID 80077)
-- Dependencies: 164 164
-- Name: c_field_key; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX c_field_key ON cfield USING btree (relid, fname);


--
-- TOC entry 2201 (class 1259 OID 80078)
-- Dependencies: 163
-- Name: cactivity_actid; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX cactivity_actid ON cactivity USING btree (actid);


--
-- TOC entry 2207 (class 1259 OID 80079)
-- Dependencies: 165
-- Name: coperand_opid; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX coperand_opid ON coperand USING btree (opid);


--
-- TOC entry 2212 (class 1259 OID 80080)
-- Dependencies: 167
-- Name: crelation_relid; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX crelation_relid ON crelation USING btree (relid);


--
-- TOC entry 2213 (class 1259 OID 80081)
-- Dependencies: 169
-- Name: cworkflow_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX cworkflow_index ON cworkflow USING btree (wkfid);


--
-- TOC entry 2214 (class 1259 OID 80082)
-- Dependencies: 169
-- Name: cworkflow_index_tag; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX cworkflow_index_tag ON cworkflow USING btree (tag);


--
-- TOC entry 2217 (class 1259 OID 80083)
-- Dependencies: 170
-- Name: e_activation_actid; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX e_activation_actid ON eactivation USING btree (actid);


--
-- TOC entry 2221 (class 1259 OID 80084)
-- Dependencies: 171
-- Name: e_activity_wkfid; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX e_activity_wkfid ON eactivity USING btree (wkfid);


--
-- TOC entry 2225 (class 1259 OID 80085)
-- Dependencies: 172
-- Name: e_file_actid; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX e_file_actid ON efile USING btree (actid);


--
-- TOC entry 2226 (class 1259 OID 80086)
-- Dependencies: 172
-- Name: e_file_taskid; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX e_file_taskid ON efile USING btree (taskid);


--
-- TOC entry 2218 (class 1259 OID 80087)
-- Dependencies: 170
-- Name: eactivation_taskid; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX eactivation_taskid ON eactivation USING btree (taskid);


--
-- TOC entry 2222 (class 1259 OID 80088)
-- Dependencies: 171
-- Name: eactivity_actid; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX eactivity_actid ON eactivity USING btree (actid);


--
-- TOC entry 2227 (class 1259 OID 80089)
-- Dependencies: 172
-- Name: efile_fileid; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX efile_fileid ON efile USING btree (fileid);


--
-- TOC entry 2236 (class 2606 OID 80090)
-- Dependencies: 2215 169 163
-- Name: cactivity_wkfid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY cactivity
    ADD CONSTRAINT cactivity_wkfid_fk FOREIGN KEY (wkfid) REFERENCES cworkflow(wkfid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2237 (class 2606 OID 80095)
-- Dependencies: 164 2212 167
-- Name: cfield_realid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY cfield
    ADD CONSTRAINT cfield_realid_fk FOREIGN KEY (relid) REFERENCES crelation(relid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2238 (class 2606 OID 80100)
-- Dependencies: 165 2201 163
-- Name: coperand_actid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY coperand
    ADD CONSTRAINT coperand_actid_fk FOREIGN KEY (actid) REFERENCES cactivity(actid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2239 (class 2606 OID 80105)
-- Dependencies: 2201 163 167
-- Name: crelation_actid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY crelation
    ADD CONSTRAINT crelation_actid_fk FOREIGN KEY (actid) REFERENCES cactivity(actid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2240 (class 2606 OID 80110)
-- Dependencies: 167 2201 163
-- Name: dependency_cactid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY crelation
    ADD CONSTRAINT dependency_cactid_fk FOREIGN KEY (dependency) REFERENCES cactivity(actid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2242 (class 2606 OID 80115)
-- Dependencies: 163 2201 171
-- Name: eactibity_cactid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY eactivity
    ADD CONSTRAINT eactibity_cactid_fk FOREIGN KEY (cactid) REFERENCES cactivity(actid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2243 (class 2606 OID 80120)
-- Dependencies: 175 2234 171
-- Name: eactivity_wkfid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY eactivity
    ADD CONSTRAINT eactivity_wkfid_fk FOREIGN KEY (wkfid) REFERENCES eworkflow(ewkfid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2244 (class 2606 OID 80125)
-- Dependencies: 2222 171 172
-- Name: efile_actid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY efile
    ADD CONSTRAINT efile_actid_fk FOREIGN KEY (actid) REFERENCES eactivity(actid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2245 (class 2606 OID 80130)
-- Dependencies: 2218 172 170
-- Name: efile_taskid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY efile
    ADD CONSTRAINT efile_taskid_fk FOREIGN KEY (taskid) REFERENCES eactivation(taskid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2246 (class 2606 OID 80135)
-- Dependencies: 2219 173 173 170 170
-- Name: ekeyspace_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ekeyspace
    ADD CONSTRAINT ekeyspace_fk FOREIGN KEY (actid, taskid) REFERENCES eactivation(actid, taskid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2241 (class 2606 OID 80140)
-- Dependencies: 170 171 2222
-- Name: etask_actid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY eactivation
    ADD CONSTRAINT etask_actid_fk FOREIGN KEY (actid) REFERENCES eactivity(actid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2247 (class 2606 OID 80145)
-- Dependencies: 2214 175 169
-- Name: eworkflow_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY eworkflow
    ADD CONSTRAINT eworkflow_fk FOREIGN KEY (tag) REFERENCES cworkflow(tag) ON UPDATE CASCADE ON DELETE CASCADE;
    
--
-- TOC entry 2236 (class 2606 OID 80090)
-- Dependencies: 2215 169 163
-- Name: cactivity_wkfid_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY eactivation
    ADD CONSTRAINT eactivation_machineid_fk FOREIGN KEY (machineid) REFERENCES emachine(machineid) ON UPDATE CASCADE ON DELETE CASCADE;    


--
-- TOC entry 2253 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM chiron;
GRANT ALL ON SCHEMA public TO chiron;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- TOC entry 2255 (class 0 OID 0)
-- Dependencies: 163
-- Name: cactivity; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE cactivity FROM PUBLIC;
REVOKE ALL ON TABLE cactivity FROM chiron;
GRANT ALL ON TABLE cactivity TO chiron;
GRANT ALL ON TABLE cactivity TO PUBLIC;


--
-- TOC entry 2256 (class 0 OID 0)
-- Dependencies: 164
-- Name: cfield; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE cfield FROM PUBLIC;
REVOKE ALL ON TABLE cfield FROM chiron;
GRANT ALL ON TABLE cfield TO chiron;
GRANT ALL ON TABLE cfield TO PUBLIC;


--
-- TOC entry 2257 (class 0 OID 0)
-- Dependencies: 165
-- Name: coperand; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE coperand FROM PUBLIC;
REVOKE ALL ON TABLE coperand FROM chiron;
GRANT ALL ON TABLE coperand TO chiron;
GRANT ALL ON TABLE coperand TO PUBLIC;


--
-- TOC entry 2258 (class 0 OID 0)
-- Dependencies: 167
-- Name: crelation; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE crelation FROM PUBLIC;
REVOKE ALL ON TABLE crelation FROM chiron;
GRANT ALL ON TABLE crelation TO chiron;
GRANT ALL ON TABLE crelation TO PUBLIC;


--
-- TOC entry 2259 (class 0 OID 0)
-- Dependencies: 169
-- Name: cworkflow; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE cworkflow FROM PUBLIC;
REVOKE ALL ON TABLE cworkflow FROM chiron;
GRANT ALL ON TABLE cworkflow TO chiron;
GRANT ALL ON TABLE cworkflow TO PUBLIC;


--
-- TOC entry 2260 (class 0 OID 0)
-- Dependencies: 170
-- Name: eactivation; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE eactivation FROM PUBLIC;
REVOKE ALL ON TABLE eactivation FROM chiron;
GRANT ALL ON TABLE eactivation TO chiron;
GRANT ALL ON TABLE eactivation TO PUBLIC;


--
-- TOC entry 2261 (class 0 OID 0)
-- Dependencies: 171
-- Name: eactivity; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE eactivity FROM PUBLIC;
REVOKE ALL ON TABLE eactivity FROM chiron;
GRANT ALL ON TABLE eactivity TO chiron;
GRANT ALL ON TABLE eactivity TO PUBLIC;


--
-- TOC entry 2262 (class 0 OID 0)
-- Dependencies: 172
-- Name: efile; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE efile FROM PUBLIC;
REVOKE ALL ON TABLE efile FROM chiron;
GRANT ALL ON TABLE efile TO chiron;
GRANT ALL ON TABLE efile TO PUBLIC;


--
-- TOC entry 2263 (class 0 OID 0)
-- Dependencies: 173
-- Name: ekeyspace; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE ekeyspace FROM PUBLIC;
REVOKE ALL ON TABLE ekeyspace FROM chiron;
GRANT ALL ON TABLE ekeyspace TO chiron;
GRANT ALL ON TABLE ekeyspace TO PUBLIC;


--
-- TOC entry 2264 (class 0 OID 0)
-- Dependencies: 174
-- Name: emachine; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE emachine FROM PUBLIC;
REVOKE ALL ON TABLE emachine FROM chiron;
GRANT ALL ON TABLE emachine TO chiron;
GRANT ALL ON TABLE emachine TO PUBLIC;


--
-- TOC entry 2265 (class 0 OID 0)
-- Dependencies: 175
-- Name: eworkflow; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE eworkflow FROM PUBLIC;
REVOKE ALL ON TABLE eworkflow FROM chiron;
GRANT ALL ON TABLE eworkflow TO chiron;
GRANT ALL ON TABLE eworkflow TO PUBLIC;

--
-- TOC entry 2265 (class 0 OID 0)
-- Dependencies: 175
-- Name: emachine; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE emachine FROM PUBLIC;
REVOKE ALL ON TABLE emachine FROM chiron;
GRANT ALL ON TABLE emachine TO chiron;
GRANT ALL ON TABLE emachine TO PUBLIC;

-- Completed on 2013-03-11 16:46:07 CET

--
-- postgresQL database dump complete
--

