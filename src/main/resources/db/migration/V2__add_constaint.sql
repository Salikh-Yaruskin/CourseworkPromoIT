ALTER TABLE users ADD CONSTRAINT unique_username UNIQUE (username),
				  ALTER COLUMN username TYPE VARCHAR(20);
ALTER TABLE users ADD CONSTRAINT unique_gmail UNIQUE (gmail);
ALTER TABLE users ADD CONSTRAINT gmail_format_check
					CHECK (gmail ~* '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$');

ALTER TABLE news ALTER COLUMN name TYPE VARCHAR(150);

