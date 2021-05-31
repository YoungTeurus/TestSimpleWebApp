package servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import config.Config;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jetty.Jetty;
import models.Credit;
import models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class MainServletTest {

    @BeforeAll
    static void init()  {
        (new Thread(() -> {
            try {
                Jetty.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).start();
    }

    @Test
    public void getUserAndCreditsByFullNameAndPassport() {
        String requestedFirstname = "TESTUSER1";
        String requestedSurname = "TESTUSER1";
        String requestedPatronymic = "Отчество";
        String requestedPassport = "1234567890";

        System.out.println("Запрос по имени " + requestedFirstname + ", фамилии " + requestedSurname +
                ", отчеству " + requestedPatronymic + ", пасспорту " + requestedPassport);
        User user = findByFullNameAndPassport(requestedFirstname, requestedSurname, requestedPatronymic, requestedPassport);
        Assertions.assertNotNull(user);

        System.out.println("Полученный пользователь: ");
        System.out.println(user);

        Assertions.assertEquals(1, user.getId());

        // Аналог ...?firstname=<...>&surname=<...>&patronymic=<...>&passportNumber=<...>
        List<Credit> creditList = getCreditInfoByUser(user);
        for (Credit credit : creditList) {
            Assertions.assertEquals(1, credit.getUserId());
        }

        System.out.println("Полученные кредиты пользователя: ");
        System.out.println(creditList);
    }

    @Test
    public void getUserAndCreditsByFullNameAndDriverId() {
        User user = findByFullNameAndDriverId("TESTUSER1", "TESTUSER1", "Отчество", "QWERTY-12");
        Assertions.assertNotNull(user);
        Assertions.assertEquals(1, user.getId());
        List<Credit> creditList = getCreditInfoByUser(user);
        for (Credit credit : creditList) {
            Assertions.assertEquals(1, credit.getUserId());
        }
    }

    @Test
    public void getUserAndCreditsByFullNameAndTaxId() {
        User user = findByFullNameAndTaxId("TESTUSER1", "TESTUSER1", "Отчество", "123456789012");
        Assertions.assertNotNull(user);
        Assertions.assertEquals(1, user.getId());
        List<Credit> creditList = getCreditInfoByUser(user);
        for (Credit credit : creditList) {
            Assertions.assertEquals(1, credit.getUserId());
        }
    }

    private List<Credit> getCreditInfoByUser(User user) {
        Type listType = new TypeToken<ArrayList<Credit>>(){}.getType();
        return new Gson().fromJson(connectAndGet(Config.getCreditsURL() + "?userId=" + user.getCreditServiceId() + "&controlValue=1"), listType);
    }

    private User findByFullNameAndPassport(String firstname, String surname, String patronymic, String passport) {
        String json = connectAndGet(Config.getUsersURL() + "?firstname=" + firstname + "&surname=" + surname
                 + "&patronymic=" + patronymic + "&passportNumber=" + passport);
        return new Gson().fromJson(json, User.class);
    }

    private User findByFullNameAndDriverId(String firstname, String surname, String patronymic, String driverID) {
        String json = connectAndGet(Config.getUsersURL() + "?firstname=" + firstname + "&surname=" + surname
                + "&patronymic=" + patronymic + "&driverID=" + driverID);
        return new Gson().fromJson(json, User.class);
    }

    private User findByFullNameAndTaxId(String firstname, String surname, String patronymic, String taxID) {
        String json = connectAndGet(Config.getUsersURL() + "?firstname=" + firstname + "&surname=" + surname
                + "&patronymic=" + patronymic + "&taxID=" + taxID);
        return new Gson().fromJson(json, User.class);
    }

    private String connectAndGet(String path) {
        Client client = ClientBuilder.newClient();
        WebTarget resource = client.target(path);
        Invocation.Builder request = resource.request();
        request.accept(MediaType.APPLICATION_JSON);
        Response response = request.get();
        response.bufferEntity();
        return response.readEntity(String.class);
    }
}