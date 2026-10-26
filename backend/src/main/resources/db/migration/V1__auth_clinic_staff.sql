CREATE TABLE clinic (
    id              uuid PRIMARY KEY,
    slug            varchar(160) NOT NULL UNIQUE,
    name            varchar(120) NOT NULL,
    address         varchar(300),
    city            varchar(80),
    phone           varchar(10),
    whatsapp        varchar(10),
    map_url         varchar(500),
    about           varchar(1000),
    timezone        varchar(40)  NOT NULL DEFAULT 'Asia/Kolkata',
    plan            varchar(20),
    plan_status     varchar(20)  NOT NULL DEFAULT 'TRIAL',
    trial_ends_at   timestamptz,
    message_credits integer      NOT NULL DEFAULT 0,
    created_at      timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE app_user (
    id         uuid PRIMARY KEY,
    clinic_id  uuid         NOT NULL REFERENCES clinic (id),
    mobile     varchar(16)  NOT NULL UNIQUE,
    name       varchar(120) NOT NULL,
    role       varchar(20)  NOT NULL CHECK (role IN ('OWNER', 'DOCTOR', 'RECEPTION', 'ADMIN')),
    active     boolean      NOT NULL DEFAULT true,
    created_at timestamptz  NOT NULL DEFAULT now()
);
CREATE INDEX app_user_clinic_id_idx ON app_user (clinic_id);

CREATE TABLE otp (
    id         uuid PRIMARY KEY,
    mobile     varchar(16) NOT NULL,
    code_hash  varchar(64) NOT NULL,
    purpose    varchar(20) NOT NULL,
    expires_at timestamptz NOT NULL,
    attempts   integer     NOT NULL DEFAULT 0,
    used_at    timestamptz,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX otp_mobile_created_at_idx ON otp (mobile, created_at);

CREATE TABLE refresh_token (
    id         uuid PRIMARY KEY,
    user_id    uuid        NOT NULL REFERENCES app_user (id),
    token_hash varchar(64) NOT NULL UNIQUE,
    family_id  uuid        NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX refresh_token_family_id_idx ON refresh_token (family_id);
CREATE INDEX refresh_token_user_id_idx ON refresh_token (user_id);
