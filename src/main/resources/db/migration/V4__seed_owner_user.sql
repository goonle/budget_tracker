-- Single-user mode: every record belongs to this user until login (E6) exists.
-- '!' is not a valid password hash, so nobody can log in with it.
INSERT INTO app_user (username, password_hash) VALUES ('owner', '!');
