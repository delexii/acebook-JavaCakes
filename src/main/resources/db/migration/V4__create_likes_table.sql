DROP TABLE IF EXISTS likes;

CREATE TABLE likes (
id bigserial PRIMARY KEY,
user_id bigint NOT NULL references users (id), 
post_id bigint NOT NULL references posts (id)

);
