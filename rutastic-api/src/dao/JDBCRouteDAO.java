package dao;

import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import dao.implementations.RouteDAOImplJDBC;
import helper.DateTimeUtils;
import helper.model.ModelMapper;
import helper.model.ModelMapperFactory;
import model.Route;
import model.RouteCategory;
import model.RouteToCategoriesMapping;
import routefilter.SQLRouteFilter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * JDBC Implementation for the {@code Route} DAO. To retrieve all info about a route the view
 * route_expandedinfo is used, and the table routes for DDL operations
 * (ALTER TABLE, INSERT INTO, UPDATE, DELETE FROM)
 *
 * @see DAOImplJDBC
 * @see RouteDAO
 * @see Route
 */
public class JDBCRouteDAO implements RouteDAO, RouteDAOImplJDBC {

    private static final Logger logger = Logger.getLogger(JDBCRouteDAO.class.getName());
    private boolean dependenciesConfigured;
    private Connection connection;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Route> getAll() {
        if (!dependenciesConfigured()) return null;

        logger.info("FETCHING ALL ROUTES");

        Route currentRoute;
        List<Route> allRoutes = new ArrayList<>();
        ModelMapper<Route> routeModelMapper = ModelMapperFactory.get().forModel(Route.class);

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM routes_expandedinfo");

            while (rs.next()) {
                currentRoute = routeModelMapper.parseFromResultSet(rs);
                if (currentRoute != null) {
                    allRoutes.add(currentRoute);
                    logger.info(String.format("[Fetched route] id: %d | created by: %s | title: %s | creation date: %s | kudos: %d | categories: %s",
                            currentRoute.getId(),
                            currentRoute.getCreatedByUser(),
                            currentRoute.getTitle(),
                            currentRoute.getCreationDate(),
                            currentRoute.getKudos(),
                            currentRoute.getCategories()));
                } else {
                    logger.warning("Attempted to read a NULL route");
                }
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return allRoutes;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException On call with wrong number of identifiers
     */
    @Override
    public Route getById(long... id) {
        if (id.length != 1) throw new IllegalArgumentException("Wrong number of identifiers. Expected 1");

        if (!dependenciesConfigured()) return null;

        Route route = null;
        ModelMapper<Route> routeModelMapper = ModelMapperFactory.get().forModel(Route.class);

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM routes_expandedinfo WHERE id = " + id[0]);

            if (rs.next()) {
                route = routeModelMapper.parseFromResultSet(rs);
                logger.info(String.format("[Fetched route] id: %d | created by: %s | title: %s | creation date: %s | kudos: %d | categories: %s",
                        route.getId(),
                        route.getCreatedByUser(),
                        route.getTitle(),
                        route.getCreationDate(),
                        route.getKudos(),
                        route.getCategories()));
            } else {
                logger.warning("There's no route by the id (" + id[0] + ")");
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return route;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long[] add(Route instance) {
        return add(instance, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long[] add(Route instance, boolean isAtomic) {
        long lastId, SQLERROR = -1L;
        long[] newId = new long[1];

        if (!dependenciesConfigured()) return new long[]{SQLERROR};

        Function<Connection, Long> queryLatestId = connection -> {
            try {
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT id FROM routes ORDER BY id DESC LIMIT 1");

                if (rs.next()) {
                    long id = rs.getLong("id");
                    st.close();
                    return id;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            return SQLERROR;
        };

        lastId = queryLatestId.apply(connection);

        if (lastId == SQLERROR) return new long[]{SQLERROR};

        // Insert new route into the routes table

        try {
            Statement st = connection.createStatement();
            st.executeUpdate(String.format("INSERT INTO routes(created_by_user, title, description, distance, duration, elevation, skill_level)" +
                            " VALUES ('%s', '%s', '%s', %d, %d, %d, '%s')",
                    instance.getCreatedByUser(),
                    instance.getTitle(),
                    instance.getDescription(),
                    instance.getDistance(),
                    instance.getDuration(),
                    instance.getElevation(),
                    instance.getSkillLevel()));
            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        newId[0] = queryLatestId.apply(connection);

        if (newId[0] == SQLERROR || newId[0] <= lastId) {
            if (isAtomic) {
                try {
                    connection.rollback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            return new long[]{SQLERROR};
        }

        // Parse the category names into model and add them in bulk

        List<RouteCategory> routeCategoriesObjects = parseRouteCategoriesFromString(instance.getCategories());
        List<RouteToCategoriesMapping> routeCategoriesMappings =
                generateRouteCategoryMappings(newId[0], routeCategoriesObjects);
        RouteToCategoriesMappingDAO categoriesMappingDAO =
                DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(RouteToCategoriesMapping.class);

        // Add all new categories in bulk to the routecategoriesmapping table

        try {
            if (categoriesMappingDAO.addInBulk(routeCategoriesMappings, false))
                connection.commit();
            else
                connection.rollback();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        logger.info(String.format("[NEW ROUTE CREATED] id: %d | created by: %s | title: %s | creation date: %s | kudos: %d | categories: %s",
                newId[0],
                instance.getCreatedByUser(),
                instance.getTitle(),
                instance.getCreationDate() == null ? DateTimeUtils.formatDate(new Date()) : instance.getCreationDate(),
                instance.getKudos(),
                instance.getCategories()));

        return newId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean save(Route instance) {
        return save(instance, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean save(Route instance, boolean isAtomic) {
        if (!dependenciesConfigured()) return false;

        boolean updateSuccessful = false;

        // Update first the route info that is stored in the routes table

        try {
            Statement st = connection.createStatement();
            st.executeUpdate(String.format("UPDATE routes " +
                            "SET title = '%s', description = '%s', distance = %d, duration = %d, elevation = %d, skill_level = '%s', blocked = %d " +
                            "WHERE id = %d",
                    instance.getTitle(),
                    instance.getDescription(),
                    instance.getDistance(),
                    instance.getDuration(),
                    instance.getElevation(),
                    instance.getSkillLevel(),
                    instance.isBlocked() ? 1 : 0,
                    instance.getId()));

            // Update route categories by removing all of them and adding the newly specified ones

            List<RouteCategory> routeCategoriesObjects = parseRouteCategoriesFromString(instance.getCategories());
            List<RouteToCategoriesMapping> newMappings =
                    generateRouteCategoryMappings(instance.getId(), routeCategoriesObjects);
            RouteToCategoriesMappingDAO categoriesMappingDAO =
                    DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(RouteToCategoriesMapping.class);

            List<RouteToCategoriesMapping> oldMappings = categoriesMappingDAO.getAllByRoute(instance.getId());
            updateSuccessful = categoriesMappingDAO.deleteInBulk(oldMappings, false);
            updateSuccessful = updateSuccessful && categoriesMappingDAO.addInBulk(newMappings, false);

            try {
                if (updateSuccessful)
                    connection.commit();
                else
                    connection.rollback();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            st.close();

            logger.info(String.format("[ROUTE UPDATED] id: %d | created by: %s | title: %s | creation date: %s | kudos: %d | categories: %s",
                    instance.getId(),
                    instance.getCreatedByUser(),
                    instance.getTitle(),
                    instance.getCreationDate(),
                    instance.getKudos(),
                    instance.getCategories()));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return updateSuccessful;
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
            st.executeUpdate("DELETE FROM routes WHERE id = " + id[0]);

            if (isAtomic) connection.commit();
            deletionSuccessful = true;
            st.close();

            logger.info("[route with the id (" + id[0] + ") has been deleted]");
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
    public List<Route> getTopRoutesOfTheMonth() {
        if (!dependenciesConfigured()) return null;

        logger.info("FETCHING TOP MONTHLY ROUTES BY KUDOS");

        Route currentRoute;
        List<Route> topMonthlyRoutes = new ArrayList<>();
        ModelMapper<Route> routeModelMapper = ModelMapperFactory.get().forModel(Route.class);

        // Query the view of top monthly routes by kudos given this month

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM top_monthly_routes_by_kudos");

            while (rs.next()) {
                currentRoute = routeModelMapper.parseFromResultSet(rs);
                if (currentRoute != null) {
                    topMonthlyRoutes.add(currentRoute);
                    logger.info(String.format("[Fetched top monthly route] id: %d | created by: %s | title: %s | creation date: %s | kudos: %d | categories: %s",
                            currentRoute.getId(),
                            currentRoute.getCreatedByUser(),
                            currentRoute.getTitle(),
                            currentRoute.getCreationDate(),
                            currentRoute.getKudos(),
                            currentRoute.getCategories()));
                } else {
                    logger.warning("Attempted to read a NULL route");
                }
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return topMonthlyRoutes;
    }

    @Override
    public List<Route> getTopRoutesOfTheWeek() {
        if (!dependenciesConfigured()) return null;

        logger.info("FETCHING TOP WEEKLY ROUTES BY KUDOS");

        Route currentRoute;
        List<Route> topWeeklyRoutes = new ArrayList<>();
        ModelMapper<Route> routeModelMapper = ModelMapperFactory.get().forModel(Route.class);

        // Query the view of top weekly routes by kudos given this month

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM top_weekly_routes_by_kudos");

            while (rs.next()) {
                currentRoute = routeModelMapper.parseFromResultSet(rs);
                if (currentRoute != null) {
                    topWeeklyRoutes.add(currentRoute);
                    logger.info(String.format("[Fetched top weekly route] id: %d | created by: %s | title: %s | creation date: %s | kudos: %d | categories: %s",
                            currentRoute.getId(),
                            currentRoute.getCreatedByUser(),
                            currentRoute.getTitle(),
                            currentRoute.getCreationDate(),
                            currentRoute.getKudos(),
                            currentRoute.getCategories()));
                } else {
                    logger.warning("Attempted to read a NULL route");
                }
            }

            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return topWeeklyRoutes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Route> executeFilter(SQLRouteFilter sqlRouteFilter) {
        ModelMapper<Route> routeModelMapper = ModelMapperFactory.get().forModel(Route.class);
        List<Route> filteredRoutes = new ArrayList<>();
        Route currentRoute;

        if (sqlRouteFilter.isValid()) {
            try {
                logger.info("Executing route filter (" + sqlRouteFilter.consume() + ")");

                // Execute the query with the filter

                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sqlRouteFilter.consume());

                // Parse filtered routes from the executed query

                while (rs.next()) {
                    currentRoute = routeModelMapper.parseFromResultSet(rs);
                    if (currentRoute != null) {
                        filteredRoutes.add(currentRoute);
                        logger.info(String.format("[Filtered route] id: %d | created by: %s | title: %s | creation date: %s | kudos: %d | categories: %s",
                                currentRoute.getId(),
                                currentRoute.getCreatedByUser(),
                                currentRoute.getTitle(),
                                currentRoute.getCreationDate(),
                                currentRoute.getKudos(),
                                currentRoute.getCategories()));
                    } else {
                        logger.warning("Attempted to read a NULL route");
                    }
                }

                st.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return filteredRoutes;
    }

    /**
     * Parse multiple route categories from a categories string stored in a route model object. Categories should be
     * separated by the categories separator string specified in {@link Route#CATEGORY_SEPARATOR}
     *
     * @param string String containing all category names
     * @return A collection of parsed {@code RouteCategory} model instances
     */
    private List<RouteCategory> parseRouteCategoriesFromString(String string) {
        ArrayList<RouteCategory> categories = new ArrayList<>();
        String[] categoryNames = string.split(Route.CATEGORY_SEPARATOR); // Split string into individual category names
        RouteCategoryDAO routeCategoryDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(RouteCategory.class);

        // Fetch each route category by its name and add it to the return collection
        for (String categoryName : categoryNames) {
            RouteCategory parsedCategory = routeCategoryDAO.getByName(categoryName);
            if (parsedCategory != null)
                categories.add(parsedCategory);
        }

        return categories;
    }

    /**
     * Generate the necessary mappings to map a route to one or more categories
     *
     * @param routeId    Which route to attach the categories to
     * @param categories Collection of categories to be attach to a route. It should contain at least one element
     * @return Generated mappings
     */
    private List<RouteToCategoriesMapping> generateRouteCategoryMappings(long routeId, List<RouteCategory> categories) {
        if (categories.size() < 1) return null; // Check that at least 1 category is specified

        ArrayList<RouteToCategoriesMapping> mappings = new ArrayList<>();
        RouteToCategoriesMapping currentMapping;

        // Create each individual mapping
        for (RouteCategory routeCategory : categories) {
            currentMapping = new RouteToCategoriesMapping();
            currentMapping.setRoute(routeId);
            currentMapping.setCategory(routeCategory.getId());
            mappings.add(currentMapping);
        }

        return mappings;
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
