package sample;

/**
 * Created by evger on 09-Nov-17.
 */
public class TempThread extends Thread{
    Controller controller;
    TempThread(Controller controller) {
        this.controller = controller;
        start();
    }

    @Override
    public void run() {
        try {
            System.out.println("1");
            Thread.sleep(4000);
            controller.addText();
            System.out.println("3");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
