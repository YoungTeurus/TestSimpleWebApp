package modelconnectors;

import database.DataBaseConnectionException;
import database.constructor.*;
import models.Sex;
import models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDatabaseConnector extends BaseDatabaseConnector<User> {
    @Override
    protected String getTableName() {
        return "users";
    }

    private final SexDatabaseConnector sexDatabaseConnector = SexDatabaseConnector.getInstance();
    private static UserDatabaseConnector instance;

    public static UserDatabaseConnector getInstance() {
        if (instance == null) {
            instance = new UserDatabaseConnector();
        }
        return instance;
    }

    @Override
    protected List<Parameter> getParametersForInsert(User user) {
        List<Parameter> params = new ArrayList<>();

        params.add(new StringParameter("firstname", user.getFirstname()));
        params.add(new StringParameter("surname", user.getSurname()));
        params.add(new DateParameter("birth_date", Date.valueOf(user.getBirthDate())));
        params.add(new LongParameter("sex_id", user.getSex().getId()));
        params.add(new StringParameter("passport_number", user.getPassportNumber()));
        params.add(new StringParameter("tax_payer_id", user.getTaxPayerID()));
        params.add(new StringParameter("driver_licence_id", user.getDriverLicenceId()));

        return params;
    }

    @Override
    protected final ResultSet getResultSetOfRemovedObjectId(long id) throws SQLException, DataBaseConnectionException {
        Connection connection = db.getConnection();
        String sql = "DELETE FROM public.\"users\" WHERE id = ? RETURNING id;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, id);

        return db.executeStatement(preparedStatement);
    }
    @Override
    protected User constructObjectFromResultSet(ResultSet rs) {
        // Оторванность sql запроса и разбирания результа запроса для создания объекта напрягает.
        // Колонки, возвращаемые SQL запросом:
        // id, firstname, birth_date, passport_number, sex_id, surname, tax_payer_id, driver_licence_id
        try {
            int userSexForeignKey = rs.getInt("sex_id");
            Sex usersSex = getSexById(userSexForeignKey);

            User user = new User.Builder(rs.getString("firstname"))
                    .id(rs.getInt("id"))
                    .birthDate(rs.getDate("birth_date").toLocalDate())
                    .passportNumber(rs.getString("passport_number"))
                    .sex(usersSex)
                    .surname(rs.getString("surname"))
                    .taxPayerID(rs.getString("tax_payer_id"))
                    .driverLicenceId(rs.getString("driver_licence_id")).build();
            return user;
        } catch (SQLException e){
            // TODO: решить, как обрабатывать ошибку при невозможности создать user из полученных данных.
            // Либо слать ошибку дальше по стеку вызовов, либо возвращать null.
            return null;
        }
    }


    public final User getByPassportID(String passport) throws SQLException, DataBaseConnectionException  {
        List<Parameter> params = new ArrayList<>();
        params.add(new StringParameter("passport_number", passport));

        List<User> foundUsers = getByParameters(params);

        if (foundUsers.size() > 0) {
            return foundUsers.get(0);
        }
        return null;
    }

    private Sex getSexById(int id){
        Sex returnSex = null;
        try {
            returnSex = sexDatabaseConnector.getById(id);
        } catch (SQLException | DataBaseConnectionException e){
            // TODO: что делать, если не смогли получить пол?
        }
        return returnSex;
    }
}
