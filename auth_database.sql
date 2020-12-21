DROP DATABASE IF EXISTS auth;

CREATE DATABASE auth;
USE auth;

CREATE TABLE roles (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) UNIQUE NOT NULL,
    description VARCHAR(1024),
    create_at TIMESTAMP NOT NULL,
    update_at TIMESTAMP NOT NULL
);

CREATE TABLE devices (
    id VARCHAR(128) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    password VARCHAR(128) NOT NULL,
    description VARCHAR(1024),
    create_at TIMESTAMP NOT NULL,
    update_at TIMESTAMP NOT NULL
);

CREATE TABLE permissions (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    topic VARCHAR(1024) UNIQUE NOT NULL,
    publish_allowed BOOLEAN DEFAULT FALSE,
    subcribe_allowed BOOLEAN DEFAULT FALSE,
    qos_0_allowed BOOLEAN DEFAULT FALSE,
    qos_1_allowed BOOLEAN DEFAULT FALSE,
    qos_2_allowed BOOLEAN DEFAULT FALSE,
    create_at TIMESTAMP NOT NULL,
    update_at TIMESTAMP NOT NULL
);

CREATE TABLE device_roles (
    device_id VARCHAR(128)
    role_id INT UNSIGNED,
    create_at TIMESTAMP NOT NULL,
    update_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_user_roles PRIMARY KEY (device_id, role_id);
    CONSTRAINT FK_device_roles_devices FOREIGN KEY (device_id)
    REFERENCES devices(device_id),
    CONSTRAINT FK_device_roles_roles FOREIGN KEY (role_id)
    REFERENCES roles(role_id)
);

CREATE TABLE role_permissions (
    role_id INT UNSIGNED,
    permission_id INT UNSIGNED,
    create_at TIMESTAMP NOT NULL,
    update_at TIMESTAMP NOT NULL,
    CONSTRAINT PK_role_permissions PRIMARY KEY (role_id, permission_id);
    CONSTRAINT FK_role_permissions_roles FOREIGN KEY (role_id)
    REFERENCES roles(role_id),
    CONSTRAINT FK_role_permissions_permissions FOREIGN KEY (permission_id)
    REFERENCES permissions(permission_id)
);