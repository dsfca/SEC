import org.junit.jupiter.api.Test;
import shared.TrackerLocationSystem;

public class Test01 {

    @Test
    void simpleTest() {

        try {
            TrackerLocationSystem sys = new TrackerLocationSystem(10, 5, 5, 9100);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
