package helper.model;

import helper.DateTimeUtils;
import model.KudoEntry;
import org.apache.commons.lang3.NotImplementedException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

public class KudoEntryMapper implements ModelMapper<KudoEntry> {

    /**
     * Parse a model instance of type {@code KudoEntry} from query parameters (or any other strings collection)
     *
     * @param queryParams query parameters (or any other strings collection)
     * @return The parsed model instance or null if any error occurred
     */
    @Override
    public KudoEntry parseFromQueryParams(Map<String, String[]> queryParams) {
        throw new NotImplementedException();
    }

    /**
     * Parse a model instance of type {@code KudoEntry} from a result set from a database query
     *
     * @param rs Result set containing queried columns from a database
     * @return The parsed instance or null if it any error occurred
     */
    @Override
    public KudoEntry parseFromResultSet(ResultSet rs) {
        try {
            if (rs == null || rs.isClosed()) return null;
            if (rs.isBeforeFirst()) rs.next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        KudoEntry kudoEntry = new KudoEntry();

        try {
            kudoEntry.setUser(rs.getString("user"));
        } catch (SQLException ignored) {
        }
        try {
            kudoEntry.setRoute(rs.getLong("route"));
        } catch (SQLException ignored) {
        }
        try {
            kudoEntry.setModifier(rs.getInt("modifier"));
        } catch (SQLException ignored) {
        }
        try {
            kudoEntry.setSubmissionDate(DateTimeUtils.formatISO8601(Instant.ofEpochSecond(rs.getLong("submission_date"))));
        } catch (SQLException ignored) {
        }

        return kudoEntry;
    }
}
