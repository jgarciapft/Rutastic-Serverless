package dao;

import dao.implementations.DAOImplJDBC;
import helper.model.ModelMapper;
import helper.model.ModelMapperFactory;
import model.User;
import model.statistic.UserStatistic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * JDBC Implementation for the {@code User} DAO. The password of an user is never logged through this class
 *
 * @see DAOImplJDBC
 * @see UserDAO
 * @see User
 */
public class JDBCUserDAO implements UserDAO, DAOImplJDBC {

    private static final Logger logger = Logger.getLogger(JDBCUserDAO.class.getName());
    private boolean dependenciesConfigured;
    private Connection connection;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getAll() {
        if (!dependenciesConfigured()) return null;

        logger.info("FETCHING ALL USERS");

        User currentUser;
        List<User> allUsers = new ArrayList<>();
        ModelMapper<User> userModelMapper = ModelMapperFactory.get().forModel(User.class);

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM users");

            while (rs.next()) {
                currentUser = userModelMapper.parseFromResultSet(rs);
                if (currentUser != null) {
                    allUsers.add(currentUser);
                    logger.info(String.format("[FETCHED USER] username: %s",
                            currentUser.getUsername()));
                } else {
                    logger.warning("Attempted to read a NULL user");
                }
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return allUsers;
    }

    /**
     * @throws UnsupportedOperationException Not supported. See getByUsername()
     */
    @Override
    public User getById(long... id) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getByUsername(String username) {
        if (!dependenciesConfigured()) return null;

        User user = null;
        ModelMapper<User> userModelMapper = ModelMapperFactory.get().forModel(User.class);

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(String.format("SELECT * FROM users WHERE username = '%s'", username));

            if (rs.next()) {
                user = userModelMapper.parseFromResultSet(rs);
                logger.info(String.format("[FETCHED USER] username: %s",
                        user.getUsername()));
            } else {
                logger.warning("There's no user by the username (" + username + ")");
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long[] add(User instance) {
        return add(instance, true);
    }

    /**
     * {@inheritDoc}
     * <p></p>
     * This method can only add regular users, not admins or any other type of user / role
     */
    @Override
    public long[] add(User instance, boolean isAtomic) {
        long SQLERROR = -1L;

        if (!dependenciesConfigured()) return new long[]{SQLERROR};

        try {
            Statement st = connection.createStatement();
            // It can only register regular users, the default value for the role column
            st.executeUpdate(String.format("INSERT INTO users(username) VALUES ('%s')",
                    instance.getUsername()));
            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            if (isAtomic) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return new long[]{SQLERROR};
        }

        if (isAtomic) {
            try {
                connection.commit();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        logger.info(String.format("[NEW USER CREATED] username: %s",
                instance.getUsername()));

        return new long[]{0};
    }

    /**
     * @throws UnsupportedOperationException Not supported
     */
    @Override
    public boolean save(User instance) {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException Not supported
     */
    @Override
    public boolean save(User instance, boolean isAtomic) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException On call with wrong number of identifiers
     */
    @Override
    public boolean deleteById(long... id) {
        return deleteById(true, id);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException On call with wrong number of identifiers
     */
    @Override
    public boolean deleteById(boolean isAtomic, long... id) {
        if (id.length != 1) throw new IllegalArgumentException("Wrong number of identifiers. Expected 1");

        if (!dependenciesConfigured()) return false;

        boolean deletionSuccessful = false;

        try {
            Statement st = connection.createStatement();
            st.executeUpdate("DELETE FROM users WHERE id = " + id[0]);

            if (isAtomic) connection.commit();
            deletionSuccessful = true;
            st.close();

            logger.info("[user with the id (" + id[0] + ") has been deleted]");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            if (isAtomic) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return deletionSuccessful;
    }

    @Override
    public boolean deleteByUsername(boolean isAtomic, String username) {
        if (!dependenciesConfigured()) return false;

        boolean deletionSuccessful = false;

        try {
            Statement st = connection.createStatement();
            st.executeUpdate("DELETE FROM users WHERE username = '" + username + "'");

            if (isAtomic) connection.commit();
            deletionSuccessful = true;
            st.close();

            logger.info("[user with the username (" + username + ") has been deleted]");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            if (isAtomic) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return deletionSuccessful;
    }

    @Override
    public List<UserStatistic> getTopUsersByTopMonthlyRoutes() {
        if (!dependenciesConfigured()) return null;

        logger.info("FETCHING TOP USERS BY TOP MONTHLY ROUTES");

        UserStatistic currentUserStat;
        List<UserStatistic> topUsers = new ArrayList<>();

        // Query the view of top users by routes at the top monthly chart

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM top_users_by_top_monthly_routes");

            // Parse each row into a list of user stats which links an username to the number of top monthly routes

            while (rs.next()) {
                currentUserStat = new UserStatistic();

                currentUserStat.setUsername(rs.getString("username"));
                currentUserStat.setStat((int) rs.getFloat("top_routes"));

                // Check some stat could be parsed

                if (currentUserStat.getUsername() != null) {
                    topUsers.add(currentUserStat);
                    logger.info(String.format("[Top user stat] username: %s | top routes: %d",
                            currentUserStat.getUsername(),
                            (int) currentUserStat.getStat()));
                }
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return topUsers;
    }

    @Override
    public List<UserStatistic> getTopUsersByAvgKudos() {
        if (!dependenciesConfigured()) return null;

        logger.info("FETCHING TOP USERS BY TOP AVEGARE KUDOS");

        UserStatistic currentUserStat;
        List<UserStatistic> topUsers = new ArrayList<>();

        // Query the view of top users by average kudo ratings of their routes

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM top_users_by_top_avg_kudos");

            // Parse each row into a list of user stats which links an username to the average kudo rating

            while (rs.next()) {
                currentUserStat = new UserStatistic();

                currentUserStat.setUsername(rs.getString("username"));
                currentUserStat.setStat(rs.getFloat("avg_kudos"));

                // Check some stat could be parsed

                if (currentUserStat.getUsername() != null) {
                    topUsers.add(currentUserStat);
                    logger.info(String.format("[Top user stat] username: %s | avg. kudos: %f",
                            currentUserStat.getUsername(),
                            currentUserStat.getStat()));
                }
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return topUsers;
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
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
