package sample;


import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import static sample.Main.BLOCK_SIZE;

/**
 * Created by User on 26/11/2016.
 */
public class NewBody extends Pane {

    private Rectangle rOut1;


    public NewBody(double x, double y){
        this.setTranslateX(x);
        this.setTranslateY(y);
        rOut1 = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
        Image body = new Image("sample/body.png");
        ImagePattern ip = new ImagePattern(body);
        rOut1.setFill(ip);
        super.getChildren().addAll(rOut1);
    }

    public void setrOut1(ImagePattern imp) {
        this.rOut1.setFill(imp);
    }
}