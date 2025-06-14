-- Drop the role column constraint
ALTER TABLE users DROP CONSTRAINT users_role_check;

-- Drop the role column
ALTER TABLE users DROP COLUMN role;

-- Create user_roles table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT user_roles_role_check CHECK (role IN ('ADMIN', 'LIBRARIAN', 'MEMBER'))
); 