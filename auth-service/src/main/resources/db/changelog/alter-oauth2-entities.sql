-- liquibase formatted sql

-- changeset nguyenkhoi:alter-oauth2-client-text-columns

ALTER TABLE oauth2_client
    ALTER COLUMN authentication_methods TYPE TEXT;

ALTER TABLE oauth2_client
    ALTER COLUMN authorization_grant_types TYPE TEXT;

ALTER TABLE oauth2_client
    ALTER COLUMN redirect_uris TYPE TEXT;

ALTER TABLE oauth2_client
    ALTER COLUMN scopes TYPE TEXT;