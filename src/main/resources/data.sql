-- Create tables first (if using create-drop mode)
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     first_name VARCHAR(255) NOT NULL,
                                     last_name VARCHAR(255) NOT NULL,
                                     location VARCHAR(255),
                                     average_rating DECIMAL(3,2),
                                     total_ratings INTEGER
);

CREATE TABLE IF NOT EXISTS users_roles (
                                           user_id BIGINT,
                                           roles VARCHAR(255),
                                           FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS ingredients (
                                           id BIGSERIAL PRIMARY KEY,
                                           name VARCHAR(255) NOT NULL,
                                           category VARCHAR(255) NOT NULL,
                                           quantity DECIMAL(10,2) NOT NULL,
                                           unit VARCHAR(255) NOT NULL,
                                           price DECIMAL(10,2) NOT NULL,
                                           expiry_date DATE NOT NULL,
                                           low_stock_threshold DECIMAL(10,2) NOT NULL,
                                           active BOOLEAN NOT NULL,
                                           retailer_id BIGINT NOT NULL,
                                           FOREIGN KEY (retailer_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS questions (
                                         id BIGSERIAL PRIMARY KEY,
                                         title VARCHAR(255) NOT NULL,
                                         content TEXT NOT NULL,
                                         created_at TIMESTAMP NOT NULL,
                                         updated_at TIMESTAMP,
                                         is_active BOOLEAN NOT NULL DEFAULT true,
                                         is_pinned BOOLEAN NOT NULL DEFAULT false,
                                         author_id BIGINT NOT NULL,
                                         FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS answers (
                                       id BIGSERIAL PRIMARY KEY,
                                       content TEXT NOT NULL,
                                       created_at TIMESTAMP NOT NULL,
                                       updated_at TIMESTAMP,
                                       active BOOLEAN NOT NULL DEFAULT true,
                                       accepted BOOLEAN NOT NULL DEFAULT false,
                                       verified_answer BOOLEAN NOT NULL DEFAULT false,
                                       author_id BIGINT NOT NULL,
                                       question_id BIGINT NOT NULL,
                                       FOREIGN KEY (author_id) REFERENCES users(id),
                                       FOREIGN KEY (question_id) REFERENCES questions(id)
);

CREATE TABLE IF NOT EXISTS votes (
                                     id BIGSERIAL PRIMARY KEY,
                                     type VARCHAR(50) NOT NULL,
                                     created_at TIMESTAMP NOT NULL,
                                     user_id BIGINT NOT NULL,
                                     question_id BIGINT,
                                     answer_id BIGINT,
                                     FOREIGN KEY (user_id) REFERENCES users(id),
                                     FOREIGN KEY (question_id) REFERENCES questions(id),
                                     FOREIGN KEY (answer_id) REFERENCES answers(id)
);

CREATE TABLE IF NOT EXISTS reports (
                                       id BIGSERIAL PRIMARY KEY,
                                       reason TEXT NOT NULL,
                                       description VARCHAR(1000),
                                       status VARCHAR(50) NOT NULL,
                                       created_at TIMESTAMP NOT NULL,
                                       resolved_at TIMESTAMP,
                                       reporter_id BIGINT NOT NULL,
                                       question_id BIGINT,
                                       answer_id BIGINT,
                                       FOREIGN KEY (reporter_id) REFERENCES users(id),
                                       FOREIGN KEY (question_id) REFERENCES questions(id),
                                       FOREIGN KEY (answer_id) REFERENCES answers(id)
);

CREATE TABLE IF NOT EXISTS orders (
                                      id BIGSERIAL PRIMARY KEY,
                                      status VARCHAR(50) NOT NULL,
                                      total_price DECIMAL(10,2) NOT NULL,
                                      order_date TIMESTAMP NOT NULL,
                                      notes VARCHAR(255),
                                      customer_id BIGINT NOT NULL,
                                      retailer_id BIGINT NOT NULL,
                                      FOREIGN KEY (customer_id) REFERENCES users(id),
                                      FOREIGN KEY (retailer_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
                                           id BIGSERIAL PRIMARY KEY,
                                           order_id BIGINT NOT NULL,
                                           ingredient_id BIGINT NOT NULL,
                                           quantity DECIMAL(10,2) NOT NULL,
                                           price_per_unit DECIMAL(10,2) NOT NULL,
                                           total_price DECIMAL(10,2) NOT NULL,
                                           FOREIGN KEY (order_id) REFERENCES orders(id),
                                           FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
);

CREATE TABLE IF NOT EXISTS ratings (
                                       id BIGSERIAL PRIMARY KEY,
                                       score INTEGER NOT NULL,
                                       comment TEXT,
                                       created_at TIMESTAMP NOT NULL,
                                       customer_id BIGINT NOT NULL,
                                       retailer_id BIGINT NOT NULL,
                                       order_id BIGINT,
                                       FOREIGN KEY (customer_id) REFERENCES users(id),
                                       FOREIGN KEY (retailer_id) REFERENCES users(id),
                                       FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Delete existing data
TRUNCATE TABLE users_roles, votes, reports, answers, questions, ratings, order_items, orders, ingredients, users CASCADE;

-- Insert initial users without explicit IDs
INSERT INTO users (email, password, first_name, last_name, location, average_rating, total_ratings)
VALUES
    ('beeruser@example.com', '$2a$12$XfJB9oY8E.RK2zPL0PQ57ej48qogZba0qd4o/n/.WaGxejm15zkLG', 'Bob', 'Beer', 'Amsterdam', 0.0, 0),
    ('brewery@example.com', '$2a$12$XfJB9oY8E.RK2zPL0PQ57ej48qogZba0qd4o/n/.WaGxejm15zkLG', 'Brewer', 'Smith', 'Rotterdam', 4.5, 10),
    ('beermod@example.com', '$2a$12$XfJB9oY8E.RK2zPL0PQ57ej48qogZba0qd4o/n/.WaGxejm15zkLG', 'Mike', 'Moderator', 'Utrecht', 0.0, 0),
    ('craftbeer@example.com', '$2a$12$XfJB9oY8E.RK2zPL0PQ57ej48qogZba0qd4o/n/.WaGxejm15zkLG', 'Craft', 'Enthusiast', 'The Hague', 0.0, 0);

-- Assign user roles using subqueries
INSERT INTO users_roles (user_id, roles)
SELECT id, 'USER' FROM users WHERE email = 'beeruser@example.com';

INSERT INTO users_roles (user_id, roles)
SELECT id, 'RETAILER' FROM users WHERE email = 'brewery@example.com';

INSERT INTO users_roles (user_id, roles)
SELECT id, 'MODERATOR' FROM users WHERE email = 'beermod@example.com';

INSERT INTO users_roles (user_id, roles)
SELECT id, 'USER' FROM users WHERE email = 'craftbeer@example.com';

-- Insert ingredients using array unnest for bulk insert
INSERT INTO ingredients (name, category, quantity, price, low_stock_threshold, active, retailer_id, expiry_date, unit)
SELECT unnest(ARRAY['Pilsner Malt', 'Cascade Hops', 'Belgian Yeast', 'Citra Hops', 'Munich Malt', 'Wheat Malt', 'Saaz Hops', 'Lager Yeast']),
       unnest(ARRAY['GRAINS', 'HOPS', 'YEAST', 'HOPS', 'GRAINS', 'GRAINS', 'HOPS', 'YEAST']),
       unnest(ARRAY[100, 80, 50, 5, 40, 75, 30, 25]::decimal[]),
       unnest(ARRAY[2.99, 4.99, 3.99, 5.99, 3.49, 2.99, 4.49, 3.99]::decimal[]),
       unnest(ARRAY[20, 15, 10, 10, 8, 15, 8, 5]::decimal[]),
       true,
       (SELECT id FROM users WHERE email = 'brewery@example.com'),
       CURRENT_DATE + INTERVAL '6 months',
       unnest(ARRAY['KG', 'G', 'G', 'G', 'KG', 'KG', 'G', 'G']);

-- Insert community questions
INSERT INTO questions (title, content, created_at, updated_at, is_active, is_pinned, author_id)
VALUES
    ('Best hops for IPA?',
     'I''m brewing my first IPA and wondering which hop combinations work best. Any suggestions?',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false,
     (SELECT id FROM users WHERE email = 'beeruser@example.com')),
    ('Proper fermentation temperature?',
     'What''s the ideal temperature range for fermenting a Belgian-style ale?',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false,
     (SELECT id FROM users WHERE email = 'craftbeer@example.com')),
    ('Cloudy beer problem',
     'My latest batch came out really cloudy. Using wheat malt and Belgian yeast. Normal?',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false,
     (SELECT id FROM users WHERE email = 'beeruser@example.com'));

-- Insert answers to community questions
INSERT INTO answers (content, created_at, updated_at, active, accepted, verified_answer, author_id, question_id)
VALUES
    ('For an IPA, I highly recommend a combination of Cascade for bittering and Citra for aroma. They create that perfect citrusy profile that''s popular in modern IPAs.',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, true, true,
     (SELECT id FROM users WHERE email = 'brewery@example.com'),
     (SELECT id FROM questions WHERE title = 'Best hops for IPA?')),
    ('18°C is actually perfect for Belgian ales! They typically ferment best between 17-21°C (63-70°F).',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, true,
     (SELECT id FROM users WHERE email = 'beermod@example.com'),
     (SELECT id FROM questions WHERE title = 'Proper fermentation temperature?')),
    ('With wheat malt and Belgian yeast, cloudiness is completely normal! Belgian witbiers are traditionally cloudy.',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, false,
     (SELECT id FROM users WHERE email = 'brewery@example.com'),
     (SELECT id FROM questions WHERE title = 'Cloudy beer problem'));

-- Insert votes on questions and answers
INSERT INTO votes (type, created_at, user_id, question_id)
SELECT 'UPVOTE', CURRENT_TIMESTAMP, id,
       (SELECT id FROM questions WHERE title = 'Best hops for IPA?')
FROM users WHERE email IN ('beeruser@example.com', 'craftbeer@example.com');

INSERT INTO votes (type, created_at, user_id, answer_id)
VALUES
    ('UPVOTE', CURRENT_TIMESTAMP,
     (SELECT id FROM users WHERE email = 'beeruser@example.com'),
     (SELECT id FROM answers WHERE content LIKE '%Cascade for bittering%')),
    ('UPVOTE', CURRENT_TIMESTAMP,
     (SELECT id FROM users WHERE email = 'craftbeer@example.com'),
     (SELECT id FROM answers WHERE content LIKE '%18°C is actually perfect%'));

-- Insert content reports
INSERT INTO reports (reason, status, created_at, reporter_id, answer_id)
VALUES ('Possible spam - promoting specific brand', 'PENDING', CURRENT_TIMESTAMP,
        (SELECT id FROM users WHERE email = 'craftbeer@example.com'),
        (SELECT id FROM answers WHERE content LIKE '%Cascade for bittering%'));

-- Insert orders
INSERT INTO orders (status, total_price, order_date, notes, customer_id, retailer_id)
VALUES
    ('DELIVERED', 45.97, CURRENT_TIMESTAMP, 'First order',
     (SELECT id FROM users WHERE email = 'beeruser@example.com'),
     (SELECT id FROM users WHERE email = 'brewery@example.com')),
    ('PENDING', 23.98, CURRENT_TIMESTAMP, NULL,
     (SELECT id FROM users WHERE email = 'craftbeer@example.com'),
     (SELECT id FROM users WHERE email = 'brewery@example.com'));

-- Insert order items
INSERT INTO order_items (order_id, ingredient_id, quantity, price_per_unit, total_price)
VALUES
    ((SELECT id FROM orders WHERE notes = 'First order'),
     (SELECT id FROM ingredients WHERE name = 'Pilsner Malt'),
     10.0, 2.99, 29.90),
    ((SELECT id FROM orders WHERE notes = 'First order'),
     (SELECT id FROM ingredients WHERE name = 'Cascade Hops'),
     5.0, 4.99, 24.95),
    ((SELECT id FROM orders WHERE notes IS NULL),
     (SELECT id FROM ingredients WHERE name = 'Belgian Yeast'),
     6.0, 3.99, 23.94);

-- Insert ratings for orders and retailers
INSERT INTO ratings (score, comment, created_at, customer_id, retailer_id, order_id)
VALUES
    (5, 'Great quality malt and super fresh hops!', CURRENT_TIMESTAMP,
     (SELECT id FROM users WHERE email = 'beeruser@example.com'),
     (SELECT id FROM users WHERE email = 'brewery@example.com'),
     (SELECT id FROM orders WHERE notes = 'First order')),
    (5, 'The Belgian yeast worked perfectly for my witbier!', CURRENT_TIMESTAMP,
     (SELECT id FROM users WHERE email = 'craftbeer@example.com'),
     (SELECT id FROM users WHERE email = 'brewery@example.com'),
     (SELECT id FROM orders WHERE notes IS NULL));