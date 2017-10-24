CREATE TABLE "user" (
  id SERIAL PRIMARY KEY,
  email VARCHAR,
  name VARCHAR
);

CREATE TABLE search_log (
  user_id SERIAL REFERENCES "user"(id),
  text VARCHAR,
  timestamp timestamp
)

