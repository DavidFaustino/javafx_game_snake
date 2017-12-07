package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

/**
 * @author David Faustino
 */
public class Main extends Application {

    public static final int BLOCK_SIZE = 20; //tamanho do bloc
    public static final int APP_WIDTH = 20 * BLOCK_SIZE; //largura
    public static final int APP_HEIGHT = 15 * BLOCK_SIZE; // altura
    public static Stage window;

    //Lista de Pontuação
    private ArrayList<Integer> allScores = new ArrayList<>();

    //Direção Default
    private Direction direction = Direction.RIGHT;

    //variavel de controle para não permitir mover-se em direções diferentes ao mesmo tempo
    private boolean moved = false;

    //variavel para verificar se a aplicação esta sendo executada
    private boolean running = false;

    //Objeto de animação
    private Timeline timeline = new Timeline();

    //Lista do Rabo da Snake
    private ObservableList<Node> snake;

    //variaveis para controle da pontuação
    private int scores = 0;
    private int lastScore = 0;
    private int bestScore = 0;

    //variavel para controle da velocidade
    private static double speed = 0.15;

    //varialve para controle do ambiente finito ou infinito
    public static boolean isEndless = true;



    private Parent createContent() throws Exception {

        URL leaderBoardUrl = getClass().getResource("LeaderBoard.txt");
        File leaderBoard = new File(leaderBoardUrl.getPath());
        if (leaderBoard.exists()) {
            System.out.println("File load");
        } else {
            System.out.println("File not load");
        }

        System.out.println(leaderBoard);

        //Carrego a tela do Jogo do arquivo snake.fxml
        Parent root1 = FXMLLoader.load(getClass().getResource("snake.fxml"));

        //vinculo os objetos do java fx aos ids do arquivo fxml
        Pane root2 = (Pane) root1.lookup("#paneOut");
        Pane root = (Pane) root2.lookup("#panePlay");

        //Adiciono o tamanho do painel
        root.setPrefSize(APP_WIDTH, APP_HEIGHT);
        double getRootTX = root.getTranslateX();
        double getRootTY = root.getTranslateY();

        //Adiciona os sub-elementos dos componentes para a observação do Objeto fx
        Group snakeBody = new Group();
        snake = snakeBody.getChildren();

        //Adiciona os sub-elementos dos componentes para a observação do Objeto fx
        Label scoresVal = (Label) root2.lookup("#scoresVal");
        scoresVal.setText("" + scores);
        Label lastScor = (Label) root1.lookup("#lastScoreVal");
        lastScor.setText("" + lastScore);
        Label bestScor = (Label) root1.lookup("#bestScoreVal");
        bestScor.setText("" + bestScore);
        Label speedVal = (Label) root1.lookup("#speedVal");
        if (speed == 0.2) {
            speedVal.setText("Slow");
        } else if (speed == 0.15) {
            speedVal.setText("Medium");
        } else if (speed == 0.09) {
            speedVal.setText("Expert");
        }

        //novo corpo da snake
        NewBody food = new NewBody(getRootTX, getRootTY);
        //carrega a imagem do corpo
        Image foodIm = new Image("sample/food.png");
        ImagePattern foodImp = new ImagePattern(foodIm);
        food.setrOut1(foodImp);
        foodRand(food);//?

        //Keyframe cria o quadro inicial da ação
        KeyFrame frame = new KeyFrame(Duration.seconds(speed), event -> {
            if (!running)
                return; //verifica a variavel de controle para ver se o jogo esta rodando

            //verifica de o tamanho da snake é maior que 1
            boolean toRemove = snake.size() > 1;
            //se sim ele remove
            Node tail = toRemove ? snake.remove(snake.size() - 1) : snake.get(0);

            double tailX = tail.getTranslateX();
            double tailY = tail.getTranslateY();


            switch (direction) {
                case UP:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() - BLOCK_SIZE);
                    break;
                case DOWN:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() + BLOCK_SIZE);
                    break;
                case LEFT:
                    tail.setTranslateX(snake.get(0).getTranslateX() - BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
                case RIGHT:
                    tail.setTranslateX(snake.get(0).getTranslateX() + BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
            }

            moved = true;

            if (toRemove)
                snake.add(0, tail);

            //detectar colisão, reinicia o jogo e executa as funções
            for (Node rect : snake) {
                if (rect != tail && tail.getTranslateX() == rect.getTranslateX() && tail.getTranslateY() == rect.getTranslateY()) {
                    allScores.add(scores);
                    lastScore = scores;
                    lastScor.setText("" + lastScore);
                    bestScore = Collections.max(allScores, null);
                    bestScor.setText("" + bestScore);
                    scores = 0;
                    scoresVal.setText("" + scores);
                    restartGame();
                    break;
                }
            }


            if (isEndless)
                fieldIsEndless((NewBody) tail);
            else
                fieldNOTEndless((NewBody) tail, scoresVal, food, lastScor, bestScor);


            if (tail.getTranslateX() == food.getTranslateX() && tail.getTranslateY() == food.getTranslateY()) {
                foodRand(food);
                scores += 20;//aumenta a pontuação
                scoresVal.setText("" + scores);//seta a pontuação na tela fxml
                NewBody rect = new NewBody(tailX, tailY);//cria um novo elemento
                snake.add(rect);//adiciona o rect na lista de elementos da snake
            }
        });

        timeline.getKeyFrames().addAll(frame); // adiciona o KeyFrame a TimeLine (sequência de animações)
        timeline.setCycleCount(Timeline.INDEFINITE); //sempre executará no mesmo quadro

        root.getChildren().addAll(food, snakeBody);

        return root2;
    }


    /**
     *Metodo para definir ações caso a tela seja finita
     * @param tail
     * @param scoresVal
     * @param food
     * @param lastScor
     * @param bestScor
     */
    private void fieldNOTEndless(NewBody tail, Label scoresVal, NewBody food, Label lastScor, Label bestScor) {
        if (tail.getTranslateX() < 0 || tail.getTranslateX() >= APP_WIDTH || tail.getTranslateY() < 0 || tail.getTranslateY() >= APP_HEIGHT) {
            allScores.add(scores);//lista de todas as pontuações
            lastScore = scores;//atualiza a variavel com a pontuação
            lastScor.setText("" + lastScore);//seta a variavel no Label do fxml
            bestScore = Collections.max(allScores, null); //analisa o arraylist trazendo o valor maximo e setando a variavel com o valor
            bestScor.setText("" + bestScore);//seta a variavel no Label do fxml

            scores = 0;//reinicio o valor do score
            scoresVal.setText("" + scores);//seta a variavel no Label do fxml
            restartGame();//reinicio o jogo
            foodRand(food);
        }
    }

    /**
     * Metodo para definir limite do rabo da snake caso a tela seja infinita
     * @param tail
     */
    private void fieldIsEndless(NewBody tail) {
        //esquerda da tela
        if (tail.getTranslateX() < 0)
            tail.setTranslateX(APP_WIDTH - BLOCK_SIZE);


        //direta da tela
        if (tail.getTranslateX() >= APP_WIDTH)
            tail.setTranslateX(0.0);


        //topo da tela
        if (tail.getTranslateY() < 0)
            tail.setTranslateY(APP_HEIGHT - BLOCK_SIZE);

        //base da tela
        if (tail.getTranslateY() >= APP_HEIGHT)
            tail.setTranslateY(0.0);
    }

    /**
     * Metodo para redefinir se quando o elemento estiver sobre o corpo da snake
     * @param snake
     * @param food
     */
    private void foodReset(ObservableList<Node> snake, NewBody food) {
        boolean flag = true;

        while (flag) {
            flag = false;
            ListIterator<Node> it = snake.listIterator();
            while (it.hasNext()) {
                Node x = it.next();
                //verificar se as coordenadas estão no mesmo ponto
                boolean match = x.getTranslateX() == food.getTranslateX() && x.getTranslateY() == food.getTranslateY();
                if (match) {
                    foodRand(food);
                    while (it.hasPrevious()) {
                        it.previous();
                    }
                }
            }
        }

    }

    private void foodRand(NewBody food) {
        food.setTranslateX((int) (Math.random() * (APP_WIDTH - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        food.setTranslateY((int) (Math.random() * (APP_HEIGHT - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        foodReset(snake, food);
    }

    /**
     * Metodo para reiniciar o jogo
     */
    private void restartGame() {
        scores = 0;//reinicia a pontuação
        stopGame();//metodo que para
        startGame();//metodo que inicia
    }

    /**
     * Metodo para parar o Jogo
     */
    private void stopGame() {
        running = false;//informa a variavel de controle que o jogo parou
        timeline.stop(); //para a sequência de animações
        snake.clear(); // Limpa os elementos dentro da lista da snake, ou seja, elimina todos os rabos
    }

    /**
     *Metodo para iniciar as funcionalidades para o jogo iniciar
     */
    private void startGame() {
        direction = Direction.RIGHT; //inicia a direção para a direita
        NewBody head = new NewBody(100, 100); //cria o panel da snake
        snake.add(head);//adioiona a lista de paineis que formará o rabo
        timeline.play();//inicia o conjunto de animações
        running = true;//atribui como verdade a variavel de controle que verifica se o jogo esta rodando
    }

    private void recursKey(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (!moved)
                return;

            switch (event.getCode()) {
                case W:
                    if (direction != Direction.DOWN)
                        direction = Direction.UP;
                    break;
                case UP:
                    if (direction != Direction.DOWN)
                        direction = Direction.UP;
                    break;
                case S:
                    if (direction != Direction.UP)
                        direction = Direction.DOWN;
                    break;
                case DOWN:
                    if (direction != Direction.UP)
                        direction = Direction.DOWN;
                    break;
                case A:
                    if (direction != Direction.RIGHT)
                        direction = Direction.LEFT;
                    break;
                case LEFT:
                    if (direction != Direction.RIGHT)
                        direction = Direction.LEFT;
                    break;
                case D:
                    if (direction != Direction.LEFT)
                        direction = Direction.RIGHT;
                    break;
                case RIGHT:
                    if (direction != Direction.LEFT)
                        direction = Direction.RIGHT;
                    break;
                case SPACE:
                    timeline.pause();
                    scene.setOnKeyPressed(event1 -> {
                        switch (event1.getCode()) {
                            case SPACE:
                                timeline.play();
                                recursKey(scene);
                                break;
                            case ESCAPE:
                                allScores.add(scores);
                                bestScore = Collections.max(allScores, null);
                                running = false;
                                timeline.stop();
                                try {
                                    Parent root2 = FXMLLoader.load(getClass().getResource("start.fxml"));
                                    window.setScene(new Scene(root2, 404, 400));
                                    prepareMainScreen(root2);
                                } catch (Exception e) {
                                }
                                break;
                        }
                    });
                    break;
                case ESCAPE:
                    allScores.add(scores);
                    bestScore = Collections.max(allScores, null);
                    running = false;
                    timeline.stop();
                    try {
                        Parent root2 = FXMLLoader.load(getClass().getResource("start.fxml"));
                        window.setScene(new Scene(root2, 404, 400));
                        prepareMainScreen(root2);
                    } catch (Exception e) {
                    }
                    break;
            }

            moved = false;
        });
    }

    public void prepareMainScreen(Parent root) {

        Button changeBtn = (Button) root.lookup("#speedBt");
        Label changeText = (Label) root.lookup("#labelSpeed");
        Button endlessBtn = (Button) root.lookup("#endlessBtn");
        if (isEndless) {
            endlessBtn.setText("Endless field");
        } else {
            endlessBtn.setText("NOT endless field");
        }

        if (speed == 0.2) {
            changeText.setText("Slow");
        } else if (speed == 0.15) {
            changeText.setText("Medium");
        } else if (speed == 0.09) {
            changeText.setText("Expert");
        }

        endlessBtn.setOnAction(event -> {
            if (isEndless) {
                isEndless = false;
                endlessBtn.setText("NOT endless field");
            } else {
                isEndless = true;
                endlessBtn.setText("Endless field");
            }
        });

        changeBtn.setOnAction(e -> {

            if (speed == 0.2) {
                Main.speed = 0.15;
                changeText.setText("Medium");
            } else if (speed == 0.15) {
                Main.speed = 0.09;
                changeText.setText("Expert");
            } else if (speed == 0.09) {
                Main.speed = 0.2;
                changeText.setText("Slow");
            }
        });
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("start.fxml"));
        primaryStage.setTitle("Snake-P4");

        window = primaryStage;
        window.setScene(new Scene(root, 404, 400));

        prepareMainScreen(root);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void startBtn() throws Exception {
        Scene scene = new Scene(createContent(), 404, 400);
        recursKey(scene);
        window.setScene(scene);
        window.show();
        startGame();
    }

    public void quit() {
        System.exit(0);
    }



}