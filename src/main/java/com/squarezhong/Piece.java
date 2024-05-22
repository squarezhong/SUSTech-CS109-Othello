package com.squarezhong;

import frame.board.BasePiece;

public class Piece extends BasePiece {
    private final Color color;
    public Piece(int x, int y, Color color) {
        super(x, y);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
