CREATE TABLE Puzzles (
    puzzle_id INTEGER PRIMARY KEY AUTO INCREMENT,
    user_id INTEGER NOT NULL,
    puzzle_img TEXT NOT NULL,
    piece_ct INTEGER,
    width INTEGER,
    height INTEGER
);

CREATE TABLE Pieces (
    piece_id INTEGER PRIMARY KEY AUTO INCREMENT,
    piece_image TEXT NOT NULL,
    solution_image TEXT NOT NULL,
    puzzle_id INTEGER NOT NULL,
    difficulty INTEGER,
    FOREIGN KEY(puzzle_id) REFERENCES Puzzles(puzzle_id)
);