SET FOREIGN_KEY_CHECKS = 0;

create table if not exists user_info
(
    uid      varchar(20) not null comment '唯一主键',
    time     bigint  default 0 comment '创建时间',
    phone    varchar(11) comment '电话号码',
    email    varchar(50) comment '电子邮箱',
    wxOid    varchar(32) comment '微信28位公众号OpenId数据',
    wxAid    varchar(32) comment '微信28位小程序OpenId数据',
    username varchar(100) comment '用户名称',
    password varchar(100) comment '6-10位AES登陆密码',
    photo    text comment '用户头像',
    gender   integer default 0 comment '用户性别，0,1,2',
    regionId varchar(20) comment '用户地区ID，来自地区表',
    depId    varchar(20) comment '用户部门ID，来自部门表',
    index (username),
    index (phone),
    index (email),
    index (wxOid),
    primary key (uid)
) comment '用户信息表';

create table if not exists user_role
(
    uid    varchar(20) not null comment '唯一主键',
    time   bigint default 0 comment '创建时间',
    userId varchar(20) comment '用户ID，来自用户表',
    roleId varchar(20) comment '角色ID，来自角色表',
    index (userId),
    index (roleId),
    primary key (uid)
) comment '用户角色表';

create table if not exists region_info
(
    uid         varchar(20) not null comment '唯一主键',
    time        bigint  default 0 comment '创建时间',
    pid         varchar(20) comment '父节点ID',
    name        varchar(40) comment '地区标准名称',
    shortName   varchar(40) comment '地区简名',
    fullName    varchar(40) comment '地区全名',
    englishName varchar(40) comment '英文名',
    levelType   integer default 0 comment '地区等级，0,1,2,3',
    cityCode    varchar(40) comment '城市代码',
    zipCode     varchar(40) comment '邮政编码',
    longitude   float   default 0 comment '东经',
    latitude    float   default 0 comment '北纬',
    index (name),
    index (pid),
    primary key (uid)
) comment '国家地区信息表';

create table if not exists department_info
(
    uid       varchar(20) not null comment '唯一主键',
    time      bigint default 0 comment '创建时间',
    pid       varchar(20) comment '父节点ID',
    name      varchar(50) comment '部门名称',
    code      varchar(50) comment '部门代码，按商定的规则限制',
    adminUser varchar(20) comment '部门管理员用户，来自用户信息表',
    regionId  varchar(20) comment '部门地区ID，来自地区表',
    index (pid),
    primary key (uid)
) comment '部门信息表';

create table if not exists department_assistant
(
    uid     varchar(20) not null comment '唯一主键',
    time    bigint  default 0 comment '创建时间',
    depId   varchar(20) comment '部门Id，来自部门表',
    userId  varchar(20) comment '用户Id，用户必须在部门中',
    manUser integer default 0 comment '管理部门用户，0,1',
    manDep  integer default 0 comment '管理子部门，0,1，前提要有manUser权限',
    index (depId),
    index (userId),
    primary key (uid)
) comment '部门助理表';

create table if not exists permission_info
(
    uid    varchar(20) not null comment '唯一主键',
    time   bigint default 0 comment '创建时间',
    name   varchar(50) comment '权限名称',
    depict text comment '权限描述',
    primary key (uid)
) comment '权限信息表';

create table if not exists permission_api
(
    uid    varchar(20) not null comment '唯一主键',
    time   bigint default 0 comment '创建时间',
    perId  varchar(20) comment '权限ID，来自权限表',
    name   varchar(50) comment '接口名称',
    depict text comment '接口说明',
    api    varchar(50) comment '接口路径',
    index (perId),
    primary key (uid)
) comment '权限接口表';

create table if not exists role_info
(
    uid    varchar(20) not null comment '唯一主键',
    time   bigint default 0 comment '创建时间',
    name   varchar(50) comment '角色名称',
    depict text comment '角色描述',
    primary key (uid)
) comment '角色（职务）信息表';

create table if not exists role_permission
(
    uid    varchar(20) not null comment '唯一主键',
    time   bigint default 0 comment '创建时间',
    roleId varchar(20) comment '角色ID，来自角色表',
    perId  text comment '权限ID，来自权限信息表',
    index (roleId),
    primary key (uid)
) comment '角色（职务）权限表';

create table if not exists sms_info
(
    uid   varchar(20) not null comment '唯一主键',
    time  bigint default 0 comment '创建时间',
    phone varchar(11) comment '目标电话号码',
    value varchar(100) comment '短信内容',
    cIp   varchar(100) comment '客户端网络IP',
    cId   varchar(100) comment '客户端设备ID',
    index (phone),
    primary key (uid)
) comment '短信发送记录';
