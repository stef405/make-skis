CREATE TABLE Puzzles (
    puzzle_id SERIAL PRIMARY KEY,
    user_id VARCHAR(200) NOT NULL,
    puzzle_img TEXT NOT NULL
);

CREATE TABLE Pieces (
    piece_id SERIAL PRIMARY KEY,
    piece_img TEXT NOT NULL,
    solution_img TEXT NOT NULL,
    puzzle_id INTEGER NOT NULL,
    difficulty INTEGER,
    FOREIGN KEY(puzzle_id) REFERENCES Puzzles(puzzle_id) ON DELETE CASCADE
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO puzzler;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public to puzzler;