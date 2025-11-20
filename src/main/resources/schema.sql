-- Mission Attempts Table
CREATE TABLE IF NOT EXISTS mission_attempts (
    id BIGSERIAL PRIMARY KEY,
    attempt_id VARCHAR(50) UNIQUE NOT NULL,
    session_id VARCHAR(36) NOT NULL,
    mission_type VARCHAR(20) NOT NULL,
    mission_name VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    total_duration NUMERIC(10, 3),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Mission Events Table
CREATE TABLE IF NOT EXISTS mission_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(50) UNIQUE NOT NULL,
    attempt_id VARCHAR(50) NOT NULL,
    session_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    data JSONB NOT NULL,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processing_time BIGINT,
    FOREIGN KEY (attempt_id) REFERENCES mission_attempts(attempt_id)
);

-- Reviews Table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    review_id VARCHAR(50) UNIQUE NOT NULL,
    attempt_id VARCHAR(50) NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    rating_text VARCHAR(20) NOT NULL,
    feedback TEXT,
    has_feedback BOOLEAN NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attempt_id) REFERENCES mission_attempts(attempt_id)
);

-- Indexes for mission_attempts
CREATE INDEX IF NOT EXISTS idx_attempt_mission_type ON mission_attempts(mission_type);
CREATE INDEX IF NOT EXISTS idx_attempt_status ON mission_attempts(status);
CREATE INDEX IF NOT EXISTS idx_attempt_start_time ON mission_attempts(start_time DESC);
CREATE INDEX IF NOT EXISTS idx_attempt_session_id ON mission_attempts(session_id);

-- Indexes for mission_events
CREATE INDEX IF NOT EXISTS idx_event_attempt_id ON mission_events(attempt_id);
CREATE INDEX IF NOT EXISTS idx_event_type ON mission_events(event_type);
CREATE INDEX IF NOT EXISTS idx_event_timestamp ON mission_events(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_event_data ON mission_events USING GIN (data);

-- Indexes for reviews
CREATE INDEX IF NOT EXISTS idx_review_attempt_id ON reviews(attempt_id);
CREATE INDEX IF NOT EXISTS idx_review_rating ON reviews(rating);
