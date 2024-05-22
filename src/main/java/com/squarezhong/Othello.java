package com.squarezhong;

import java.util.HashMap;
import java.util.Random;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import static java.lang.Math.abs;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import frame.action.Action;
import frame.action.ActionPerformType;
import frame.board.BaseGrid;
import frame.event.BoardChangeEvent;
import frame.event.EventCenter;
import frame.player.AIPlayer;
import frame.player.PlayerManager;
import frame.Controller.Game;
import frame.view.View;
// import frame.view.board.BoardView;
import frame.view.board.GridPanelView;
import frame.view.stage.GameStage;

public class Othello {
    private static boolean cheating = false;
    private static Color cheatingColor = Color.BLACK;

    public static void main(String[] args) {
        // 1. paras of the game
        View.window.setSize(800, 400);
        Game.setMaximumPlayer(2);
        View.setName("Othello");
        Game.setBoardSize(8, 8);

        // 2. register board
        Game.registerBoard(Board.class);

        // 3. register event
        Game.registerGridAction((x, y) -> true, (x, y, mouseButton) -> {
            // no possible move, the round will end
            // if (Inspection.getPossibleMoves(color).size() == 0) {
            //     return null;
            // }

            if (mouseButton == 1) {
                return new Action(!cheating) {
                    // if cheating, the round will not end
                    @Override
                    public ActionPerformType perform() {
                        BaseGrid grid = Game.getBoard().getGrid(x, y);
                        Color color = Color.values()[Game.getCurrentPlayerIndex()];
                        if (grid.hasPiece()) {
                            return ActionPerformType.FAIL;
                        } else if (cheating) {
                            grid.setOwnedPiece(new Piece(x, y, cheatingColor));
                        } else {
                            if (Inspection.trySetPiece(x, y, color, true) == 0) {
                                return ActionPerformType.FAIL;
                            }
                            grid.setOwnedPiece(new Piece(x, y, color));
                            Inspection.turnPieces(x, y, color);
                        }
                        return ActionPerformType.SUCCESS;
                    }

                    @Override
                    public void undo() {
                        BaseGrid grid = Game.getBoard().getGrid(x, y);
                        grid.removeOwnedPiece();

                        // get the last flipped piece and flip it back
                        for (int[] last : Inspection.getLastFlipped()) {
                            BaseGrid lastGrid = Game.getBoard().getGrid(last[0], last[1]);
                            Color originalColor = Color.values()[Game.getCurrentPlayerIndex()];
                            lastGrid.removeOwnedPiece();
                            lastGrid.setOwnedPiece(new Piece(last[0], last[1], originalColor));
                        }

                    }

                };
            }
            return null;
        });

        // 4. judge the winner
        // when the board is full or no player can move, the game ends
        // traverse all the grids to check which player has more pieces
        Game.setPlayerWinningJudge((player -> {
            if (Inspection.getPossibleMoves(Color.WHITE).size() == 0 &&
                    Inspection.getPossibleMoves(Color.BLACK).size() == 0) {
                return Inspection.judgeWinner() == player.getId();
            }
            return false;
        }));

        // 5. judege the end of the game
        // draw
        Game.setGameEndingJudge(() -> {
            if (PlayerManager.isOnePlayerRemains()) {
                return true;
            }

            if (Inspection.getPossibleMoves(Color.WHITE).size() == 0 &&
                    Inspection.getPossibleMoves(Color.BLACK).size() == 0) {
                return Inspection.judgeWinner() == -1;
            }

            return false;
        });

        // 6.Set up AI
        AIPlayer.addAIType("Random", (id) -> {
            return new AIPlayer(id, "Random", 200) {
                @Override
                protected boolean calculateNextMove() {
                    Random random = new Random();
                    for (int i = 0; i < 200; i++) {
                        int x = abs(random.nextInt()) % Game.getWidth();
                        int y = abs(random.nextInt()) % Game.getHeight();
                        if (performGridAction(x, y, 1)) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        });

        AIPlayer.addAIType("France", (id) -> {
            return new AIPlayer(id, "France", 200) {
                @Override
                protected boolean calculateNextMove() {
                    surrender();
                    return true;
                }
            };
        });

        AIPlayer.addAIType("Greedy", (id) -> {
            return new AIPlayer(id, "Greedy", 600) {
                @Override
                protected boolean calculateNextMove() {
                    // get the move with the most pieces flipped
                    int max = 0;
                    int[] move = new int[2];
                    Color currentColor = Color.values()[Game.getCurrentPlayerIndex()];
                    for (HashMap.Entry<int[], Integer> entry : Inspection.getPossibleMoves(currentColor).entrySet()) {
                        int[] m = entry.getKey();
                        int count = entry.getValue();
                        if (count > max) {
                            max = count;
                            move = m;
                        }
                    }
                    performGridAction(move[0], move[1], 1);
                    return true;
                }
            };
        });

        // 7. register view
        // try {
        //     // set the background image
        //     URL bgUrl = Othello.class.getClassLoader().getResource("background/background.png");
        //     Image image = ImageIO.read(bgUrl);
        //     View.setBoardViewPattern(() -> new BoardView(image) {
        //     });
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }

        View.setGridViewPattern(() -> new GridPanelView() {
            boolean isHighLighted = false, hasMouseEntered = false;

            @Override
            public void init() {
                // highlight the grid when the mouse enters
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        super.mouseEntered(e);
                        setBackground(new java.awt.Color(255, 255, 150)); // highlight color
                        setOpaque(true); // set background to opaque
                        revalidate();
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        super.mouseExited(e);
                        hasMouseEntered = false;
                        if (!isHighLighted) {
                            setOpaque(false);
                        } else {
                            setBackground(java.awt.Color.YELLOW);
                        }
                        revalidate();
                        repaint();
                    }
                });
            }

            @Override
            public void redraw(BaseGrid grid) {
                boolean flag = true;
                // highlight the grids that are possible to move
                for (int[] move : Inspection.getPossibleMoves(Color.values()[Game.getCurrentPlayerIndex()]).keySet()) {
                    if (move[0] == grid.x && move[1] == grid.y) {
                        flag = false;
                        isHighLighted = true;
                        setBackground(java.awt.Color.YELLOW);
                        setOpaque(true);
                        break;
                    }
                }
                // grids that are not possible to move are not highlighted
                if (flag) {
                    isHighLighted = false;
                    if (!hasMouseEntered) {
                        setOpaque(false);
                    }
                }
                revalidate();
                repaint();
                if (grid.hasPiece()) {
                    Piece piece = (Piece) grid.getOwnedPiece();
                    if (piece.getColor() == Color.BLACK) {
                        URL blackUrl = Othello.class.getClassLoader().getResource("pieces/black_piece.png");
                        ImageIcon black = new ImageIcon(blackUrl);
                        this.label.setIcon(black);
                    } else {
                        URL whiteUrl = Othello.class.getClassLoader().getResource("pieces/white_piece.png");
                        ImageIcon white = new ImageIcon(whiteUrl);
                        this.label.setIcon(white);
                    }
                } else {
                    this.label.setIcon(null);
                }
            }
        });

        View.setPlayerWinView(
                (player -> JOptionPane.showMessageDialog(GameStage.instance(), player.getName() + " Win!")));
        View.setPlayerLoseView(
                (player -> JOptionPane.showMessageDialog(GameStage.instance(), player.getName() + " Lose!")));
        View.setGameEndView(withdraw -> {
            if (withdraw) {
                JOptionPane.showMessageDialog(GameStage.instance(), "Withdraw!");
            }
            View.changeStage("MenuStage");
        });

        // 8. DIY the menu

        // show the current player in the JPanel
        JLabel currentPlayerLabel = new JLabel();
        // listen to the board change event
        EventCenter.subscribe(BoardChangeEvent.class,
                e -> currentPlayerLabel.setText("Now: " + Color.values()[Game.getCurrentPlayerIndex()].name()));

        // reset
        JButton reset = new JButton("Reset");
        reset.addActionListener((e) -> {
            Game.init();
        });

        // cheat mode
        JLabel cheatText = new JLabel("Cheat mode");
        JCheckBox cheat = new JCheckBox();
        JComboBox<Color> cheatColor = new JComboBox<>();
        cheatColor.addItem(Color.BLACK);
        cheatColor.addItem(Color.WHITE);
        cheat.addActionListener((e) -> {
            cheating = cheat.isSelected();
            cheatColor.setVisible(cheat.isSelected());
        });
        cheatColor.setVisible(false);
        cheatColor.addActionListener((e) -> {
            cheatingColor = (Color) cheatColor.getSelectedItem();
        });

        GameStage.instance().setCustomDrawMethod(() -> {
            GameStage stage = GameStage.instance();
            stage.menuBar.add(reset);
            stage.menuBar.add(stage.menuButton);
            stage.menuBar.add(stage.saveButton);
            stage.menuBar.add(stage.undoButton);
            stage.menuBar.add(stage.surrenderButton);
            stage.menuBar.add(cheatText);
            stage.menuBar.add(cheat);
            stage.menuBar.add(cheatColor);
            stage.scoreBoard.add(currentPlayerLabel);
            stage.add("North", stage.menuBar);
            stage.add("South", stage.scoreBoard);
        });

        // 9. start the game
        View.start();
    }
}
