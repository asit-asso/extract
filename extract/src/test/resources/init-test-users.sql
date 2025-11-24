-- Create test admin user with known password
-- The password hash is for 'motdepasse21' using Pbkdf2PasswordEncoder
-- This is used for functional tests
UPDATE users
SET pass = (SELECT pass FROM users WHERE login = 'marco.alves')
WHERE login = 'admin';