package modelconnectors;

import database.DataBaseConnectionException;
import database.constructor.*;
import models.Credit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CreditDatabaseConnector extends BaseDatabaseConnector<Credit>{
    @Override
    protected String getTableName() {
        return "credits";
    }

    private static CreditDatabaseConnector instance;

    public static CreditDatabaseConnector getInstance() {
        if (instance == null) {
            instance = new CreditDatabaseConnector();
        }
        return instance;
    }

    @Override
    protected Credit constructObjectFromResultSet(ResultSet rs) {
        // Колонки, возвращаемые SQL запросом:
        // id, userId, totalSum, startPaymentDate, endPaymentDate, branchId
        Credit returnCredit = null;
        try {
            returnCredit = new Credit(
                    rs.getLong("id"),
                    rs.getLong("userId"),
                    rs.getLong("branchId"),
                    rs.getDouble("totalSum"),
                    rs.getDate("startPaymentDate").toLocalDate(),
                    rs.getDate("endPaymentDate").toLocalDate()
            );
        } catch (SQLException e) {
            // TODO: решить, как обрабатывать ошибку при невозможности создать sex из полученных данных.
            // Либо слать ошибку дальше по стеку вызовов, либо возвращать null.
        }
        return returnCredit;
    }

    @Override
    protected List<Parameter> getParametersForInsert(Credit credit) {
        List<Parameter> params = new ArrayList<>();

        params.add(new LongParameter("userId", credit.getUserId()));
        params.add(new DoubleParameter("totalSum", credit.getTotalSum()));
        params.add(new DateParameter("startPaymentDate", Date.valueOf(credit.getStartPaymentDate())));
        params.add(new DateParameter("endPaymentDate", Date.valueOf(credit.getEndPaymentDate())));
        params.add(new LongParameter("branchId", credit.getBranchId()));

        return params;
    }

    @Override
    protected ResultSet getResultSetOfRemovedObjectId(long id) throws SQLException, DataBaseConnectionException {
        // Удалять записи о кредитах нельзя.
        throw new UnsupportedOperationException();
    }
}
