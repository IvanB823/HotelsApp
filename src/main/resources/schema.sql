CREATE TABLE hotels (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    brand VARCHAR(100)
);

CREATE TABLE addresses (
    id SERIAL PRIMARY KEY,
    hotel_id INTEGER UNIQUE NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    house_number VARCHAR(20),
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    county VARCHAR(100),
    post_code VARCHAR(20)
);

CREATE TABLE contacts (
    id SERIAL PRIMARY KEY,
    hotel_id INTEGER NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    contact_type VARCHAR(50) NOT NULL,
    contact_value VARCHAR(255) NOT NULL
);

CREATE TABLE arrival_times (
    id SERIAL PRIMARY KEY,
    hotel_id INTEGER UNIQUE NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    check_in TIME NOT NULL,
    check_out TIME NOT NULL
);

CREATE TABLE hotel_amenities (
    id SERIAL PRIMARY KEY,
    hotel_id INTEGER NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    amenity_name VARCHAR(100) NOT NULL,
    UNIQUE(hotel_id, amenity_name)
);