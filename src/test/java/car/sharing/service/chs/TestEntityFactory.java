package car.sharing.service.chs;

import car.sharing.service.chs.model.Car;
import car.sharing.service.chs.model.CarType;
import car.sharing.service.chs.model.User;

import java.math.BigDecimal;

public class TestEntityFactory {

    public static User createUser() {
        User user = new User();
        user.setEmail("user@email.com");
        user.setPassword("123456");
        return user;
    }

    public static Car createCar() {
        Car car = new Car();
        car.setBrand("Tesla");
        car.setModel("Model Y");
        car.setType(CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(120));
        return car;
    }
}