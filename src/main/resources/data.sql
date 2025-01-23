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

-- Insert users
INSERT INTO users (id, email, password, first_name, last_name, location, average_rating, total_ratings)
VALUES
    (1, 'beeruser@example.com', '$2a$12$XfJB9oY8E.RK2zPL0PQ57ej48qogZba0qd4o/n/.WaGxejm15zkLG', 'Bob', 'Beer', 'Amsterdam', 0.0, 0),
    (2, 'brewery@example.com', '$2a$12$XfJB9oY8E.RK2zPL0PQ57ej48qogZba0qd4o/n/.WaGxejm15zkLG', 'Brewer', 'Smith', 'Rotterdam', 4.5, 10),
    (3, 'beermod@example.com', '$2a$12$XfJB9oY8E.RK2zPL0PQ57ej48qogZba0qd4o/n/.WaGxejm15zkLG', 'Mike', 'Moderator', 'Utrecht', 0.0, 0),
    (4, 'craftbeer@example.com', '$2a$12$XfJB9oY8E.RK2zPL0PQ57ej48qogZba0qd4o/n/.WaGxejm15zkLG', 'Craft', 'Enthusiast', 'The Hague', 0.0, 0);

-- Insert user roles
INSERT INTO users_roles (user_id, roles)
VALUES
    (1, 'USER'),
    (2, 'RETAILER'),
    (3, 'MODERATOR'),
    (4, 'USER');

-- Insert ingredients
INSERT INTO ingredients (id, name, category, quantity, price, low_stock_threshold, active, retailer_id, expiry_date, unit)
VALUES
    (1, 'Pilsner Malt', 'GRAINS', 100, 2.99, 20, true, 2, CURRENT_DATE + INTERVAL '6 months', 'KG'),
    (2, 'Cascade Hops', 'HOPS', 80, 4.99, 15, true, 2, CURRENT_DATE + INTERVAL '6 months', 'G'),
    (3, 'Belgian Yeast', 'YEAST', 50, 3.99, 10, true, 2, CURRENT_DATE + INTERVAL '3 months', 'G'),
    (4, 'Citra Hops', 'HOPS', 5, 5.99, 10, true, 2, CURRENT_DATE + INTERVAL '6 months', 'G'),
    (5, 'Munich Malt', 'GRAINS', 40, 3.49, 8, true, 2, CURRENT_DATE + INTERVAL '6 months', 'KG'),
    (6, 'Wheat Malt', 'GRAINS', 75, 2.99, 15, true, 2, CURRENT_DATE + INTERVAL '6 months', 'KG'),
    (7, 'Saaz Hops', 'HOPS', 30, 4.49, 8, true, 2, CURRENT_DATE + INTERVAL '6 months', 'G'),
    (8, 'Lager Yeast', 'YEAST', 25, 3.99, 5, true, 2, CURRENT_DATE + INTERVAL '3 months', 'G');

-- Insert questions
INSERT INTO questions (id, title, content, created_at, updated_at, is_active, is_pinned, author_id)
VALUES
    (1, 'Best hops for IPA?', 'I''m brewing my first IPA and wondering which hop combinations work best. Any suggestions?', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, 1),
    (2, 'Proper fermentation temperature?', 'What''s the ideal temperature range for fermenting a Belgian-style ale?', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, 4),
    (3, 'Cloudy beer problem', 'My latest batch came out really cloudy. Using wheat malt and Belgian yeast. Normal?', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, 1);

-- Insert answers
INSERT INTO answers (id, content, created_at, updated_at, active, accepted, verified_answer, author_id, question_id)
VALUES
    (1, 'For an IPA, I highly recommend a combination of Cascade for bittering and Citra for aroma. They create that perfect citrusy profile that''s popular in modern IPAs.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, true, true, 2, 1),
    (2, '18°C is actually perfect for Belgian ales! They typically ferment best between 17-21°C (63-70°F).', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, true, 3, 2),
    (3, 'With wheat malt and Belgian yeast, cloudiness is completely normal! Belgian witbiers are traditionally cloudy.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, false, 2, 3);

-- Insert votes
INSERT INTO votes (id, type, created_at, user_id, question_id, answer_id)
VALUES
    (1, 'UPVOTE', CURRENT_TIMESTAMP, 1, 1, NULL),
    (2, 'UPVOTE', CURRENT_TIMESTAMP, 4, 1, NULL),
    (3, 'UPVOTE', CURRENT_TIMESTAMP, 1, NULL, 1),
    (4, 'UPVOTE', CURRENT_TIMESTAMP, 4, NULL, 2);

-- Insert reports
INSERT INTO reports (id, reason, status, created_at, reporter_id, question_id, answer_id)
VALUES
    (1, 'Possible spam - promoting specific brand', 'PENDING', CURRENT_TIMESTAMP, 4, NULL, 1);

-- Insert orders
INSERT INTO orders (id, status, total_price, order_date, notes, customer_id, retailer_id)
VALUES
    (1, 'DELIVERED', 45.97, CURRENT_TIMESTAMP, 'First order', 1, 2),
    (2, 'PENDING', 23.98, CURRENT_TIMESTAMP, NULL, 4, 2);

-- Create orders
INSERT INTO order_items (id, order_id, ingredient_id, quantity, price_per_unit, total_price)
VALUES
    (1, 1, 1, 10.0, 2.99, 29.90),
    (2, 1, 2, 5.0, 4.99, 24.95),
    (3, 2, 3, 6.0, 3.99, 23.94);

-- Insert ratings
INSERT INTO ratings (id, score, comment, created_at, customer_id, retailer_id, order_id)
VALUES
    (1, 5, 'Great quality malt and super fresh hops!', CURRENT_TIMESTAMP, 1, 2, 1),
    (2, 5, 'The Belgian yeast worked perfectly for my witbier!', CURRENT_TIMESTAMP, 4, 2, 2);