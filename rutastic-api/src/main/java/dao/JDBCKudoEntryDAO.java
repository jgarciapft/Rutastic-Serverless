package dao;

import dao.implementations.DAOImplJDBC;
import helper.DateTimeUtils;
import helper.model.ModelMapper;
import helper.model.ModelMapperFactory;
import model.KudoEntry;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * JDBC Implementation for the {@code KudoEntry} DAO
 *
 * @see KudoEntry
 * @see KudoEntryDAO
 * @see DAOImplJDBC
 */
public class JDBCKudoEntryDAO implements KudoEntryDAO, DAOImplJDBC {

    private static final Logger logger = Logger.getLogger(JDBCKudoEntryDAO.class.getName());
    private boolean dependenciesConfigured;
    private Connection readOnlyConnection;
    private Connection writeConnection;

    public JDBCKudoEntryDAO() {
        dependenciesConfigured = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<KudoEntry> getAll() {
        if (!dependenciesConfigured()) return null;

        logger.info("FETCHING ALL KUDO ENTRIES");

        KudoEntry currentEntry;
        List<KudoEntry> allEntries = new ArrayList<>();
        ModelMapper<KudoEntry> kEntryModelMapper = ModelMapperFactory.get().forModel(KudoEntry.class);

        try {
            Statement st = readOnlyConnection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM routekudosregistry");

            while (rs.next()) {
                currentEntry = kEntryModelMapper.parseFromResultSet(rs);
                if (currentEntry != null) {
                    allEntries.add(currentEntry);
                    logger.info(String.format("[FETCHED Kudo Entry] user: %s | route: %s | modifier: %d | submission date: %s",
                            currentEntry.getUser(),
                            currentEntry.getRoute(),
                            currentEntry.getModifier(),
                            currentEntry.getSubmissionDate()));
                } else {
                    logger.warning("Attempted to read a NULL kudo entry");
                }
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return allEntries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<KudoEntry> getAllByUser(String username) {
        if (!dependenciesConfigured()) return null;

        logger.info("FETCHING ALL KUDO ENTRIES FOR USERNAME (" + username + ")");

        KudoEntry currentEntry;
        List<KudoEntry> allEntries = new ArrayList<>();
        ModelMapper<KudoEntry> kEntryModelMapper = ModelMapperFactory.get().forModel(KudoEntry.class);

        try {
            Statement st = readOnlyConnection.createStatement();
            ResultSet rs = st.executeQuery(String.format("SELECT * FROM routekudosregistry WHERE user = '%s'", username));

            while (rs.next()) {
                currentEntry = kEntryModelMapper.parseFromResultSet(rs);
                if (currentEntry != null) {
                    allEntries.add(currentEntry);
                    logger.info(String.format("[FETCHED Kudo Entry] user: %s | route: %d | modifier: %d | submission date: %s",
                            currentEntry.getUser(),
                            currentEntry.getRoute(),
                            currentEntry.getModifier(),
                            currentEntry.getSubmissionDate()));
                } else {
                    logger.warning("Attempted to read a NULL kudo entry");
                }
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return allEntries;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<KudoEntry> getAllByRoute(long routeId) {
        if (!dependenciesConfigured()) return null;

        logger.info("FETCHING ALL KUDO ENTRIES FOR ROUTEID (" + routeId + ")");

        KudoEntry currentEntry;
        List<KudoEntry> allEntries = new ArrayList<>();
        ModelMapper<KudoEntry> kEntryModelMapper = ModelMapperFactory.get().forModel(KudoEntry.class);

        try {
            Statement st = readOnlyConnection.createStatement();
            ResultSet rs = st.executeQuery(String.format("SELECT * FROM routekudosregistry WHERE route = %d", routeId));

            while (rs.next()) {
                currentEntry = kEntryModelMapper.parseFromResultSet(rs);
                if (currentEntry != null) {
                    allEntries.add(currentEntry);
                    logger.info(String.format("[FETCHED Kudo Entry] user: %s | route: %s | modifier: %d | submission date: %s",
                            currentEntry.getUser(),
                            currentEntry.getRoute(),
                            currentEntry.getModifier(),
                            currentEntry.getSubmissionDate()));
                } else {
                    logger.warning("Attempted to read a NULL kudo entry");
                }
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return allEntries;
    }

    /**
     * @throws UnsupportedOperationException Not supported. Use getByPKey()
     */
    @Override
    public KudoEntry getById(long... id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KudoEntry getByPKey(String username, long routeId) {
        if (!dependenciesConfigured()) return null;

        KudoEntry kudoEntry = null;
        ModelMapper<KudoEntry> kEntryModelMapper = ModelMapperFactory.get().forModel(KudoEntry.class);

        try {
            Statement st = readOnlyConnection.createStatement();
            ResultSet rs = st.executeQuery(
                    String.format("SELECT * FROM routekudosregistry WHERE user = '%s' AND route = %d", username, routeId));

            if (rs.next()) {
                kudoEntry = kEntryModelMapper.parseFromResultSet(rs);
                logger.info(String.format("[FETCHED Kudo Entry] user: %s | route: %s | modifier: %d | submission date: %s",
                        kudoEntry.getUser(),
                        kudoEntry.getRoute(),
                        kudoEntry.getModifier(),
                        kudoEntry.getSubmissionDate()));
            } else {
                logger.warning(String.format("There's no Kudo entry by the id (%s,%d)", username, routeId));
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return kudoEntry;
    }

    /**
     * @throws UnsupportedOperationException Not supported. Use add2()
     */
    @Override
    public long[] add(KudoEntry instance) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException Not supported. Use add2()
     */
    @Override
    public long[] add(KudoEntry instance, boolean isAtomic) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] add2(KudoEntry instance, boolean isAtomic) {
        long SQLERROR = -1L;

        if (!dependenciesConfigured()) return new Object[]{SQLERROR};

        try {
            Statement st = writeConnection.createStatement();
            st.executeUpdate(String.format("INSERT INTO routekudosregistry(user, route, modifier, submission_date) VALUES ('%s', %d, %d, %d)",
                    instance.getUser(),
                    instance.getRoute(),
                    instance.getModifier(),
                    DateTimeUtils.getEpochSeconds(instance.getSubmissionDate())));

            if (isAtomic) writeConnection.commit();
            st.close();

            logger.info(String.format("[NEW KUDO ENTRY CREATED] user: %s | route: %s | modifier: %d | submission date: %s",
                    instance.getUser(),
                    instance.getRoute(),
                    instance.getModifier(),
                    instance.getSubmissionDate()));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            if (isAtomic) {
                try {
                    writeConnection.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return new Object[]{SQLERROR};
        }

        // Kudo entry ID to return on success
        return new Object[]{instance.getUser(), instance.getRoute()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean save(KudoEntry instance) {
        return save(instance, true);
    }

    /**
     * {@inheritDoc}
     * <p></p>
     * The only modifiable field is the vote modifier, which indicates whether it's a downvote or upvote
     */
    @Override
    public boolean save(KudoEntry instance, boolean isAtomic) {
        if (!dependenciesConfigured()) return false;

        boolean updateSuccessful = false;

        try {
            Statement st = writeConnection.createStatement();
            st.executeUpdate(String.format("UPDATE routekudosregistry SET modifier = %d WHERE user = '%s' AND route = %d",
                    instance.getModifier(),
                    instance.getUser(),
                    instance.getRoute()));

            if (isAtomic) writeConnection.commit();
            updateSuccessful = true;
            st.close();

            logger.info(String.format("[KUDO ENTRY UPDATED] user: %s | route: %s | modifier: %d",
                    instance.getUser(),
                    instance.getRoute(),
                    instance.getModifier()));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            if (isAtomic) {
                try {
                    writeConnection.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return updateSuccessful;
    }

    /**
     * @throws UnsupportedOperationException Not supported. See deleteByPKey()
     */
    @Override
    public boolean deleteById(long... id) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws IllegalArgumentException On call with wrong number of identifiers
     */
    @Override
    public boolean deleteById(boolean isAtomic, long... id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteByPKey(boolean isAtomic, String username, long routeId) {
        if (!dependenciesConfigured()) return false;

        boolean deletionSuccessful = false;

        try {
            Statement st = writeConnection.createStatement();
            st.executeUpdate(String.format("DELETE FROM routekudosregistry WHERE user = '%s' AND route = %d", username, routeId));

            if (isAtomic) writeConnection.commit();
            deletionSuccessful = true;
            st.close();

            logger.info(String.format("[Kudo entry with the id (%s, %d) has been deleted]", username, routeId));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            if (isAtomic) {
                try {
                    writeConnection.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return deletionSuccessful;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Long, KudoEntry> getRouteIDMappedKudoEntriesForUser(String username) {
        List<KudoEntry> kudoEntries = getAllByUser(username);

        if (kudoEntries == null) return null; // Check any kudo entry could be retrieved

        // Return the requested map of route IDs to Kudo entries by user identified by 'userId'
        return kudoEntries
                .stream()
                .collect(Collectors.toMap(KudoEntry::getRoute, kudoEntry -> kudoEntry));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dependenciesConfigured() {
        return dependenciesConfigured;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDependenciesConfigured(boolean status) {
        dependenciesConfigured = status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReadOnlyConnection(Connection connection) {
        this.readOnlyConnection = connection;
    }

    @Override
    public void setWriteConnection(Connection connection) {
        this.writeConnection = connection;
    }
}
