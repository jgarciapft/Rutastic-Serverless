CREATE TABLE users
(
    username VARCHAR(30) PRIMARY KEY
);

CREATE TABLE routes
(
    id              INTEGER PRIMARY KEY AUTO_INCREMENT,
    created_by_user VARCHAR(30),
    title           VARCHAR(100) NOT NULL,
    description     TEXT,
    distance        INTEGER      NOT NULL,
    duration        INTEGER      NOT NULL,
    elevation       INTEGER      NOT NULL,
    creation_date   INTEGER      NOT NULL,
    skill_level     VARCHAR(15)  NOT NULL,
    blocked         NUMERIC(1),

    FOREIGN KEY (created_by_user) REFERENCES users (username) ON DELETE CASCADE
);

-- Table to store each possible category for a route
CREATE TABLE routecategories
(
    id          INTEGER PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(30) UNIQUE NOT NULL,
    description VARCHAR(250)
);

-- Insert into route categories' table default categories
INSERT INTO routecategories (name, description)
VALUES ('senderismo', 'La ruta puede ser completada a pie');
INSERT INTO routecategories (name, description)
VALUES ('carrera', 'La ruta puede ser completada corriendo');
INSERT INTO routecategories (name, description)
VALUES ('ciclismo', 'La ruta puede ser completada con cualquier tipo de bicicleta');

-- Maps route categories to routes. Each route can fit into one or multiple categories
CREATE TABLE routetocategoriesmapping
(
    route    INTEGER,
    category INTEGER,

    FOREIGN KEY (route) REFERENCES routes (id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (category) REFERENCES routecategories (id) ON UPDATE CASCADE ON DELETE CASCADE,

    PRIMARY KEY (route, category)
);

-- Maps kudos given by an user to a route. Each user can either add or remove 1 kudo of each route, and
-- each route can have multiple kudos
CREATE TABLE routekudosregistry
(
    user            VARCHAR(30),
    route           INTEGER,
    modifier        NUMERIC(1) NOT NULL,
    submission_date INTEGER    NOT NULL,

    FOREIGN KEY (user) REFERENCES users (username) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (route) REFERENCES routes (id) ON UPDATE CASCADE ON DELETE CASCADE,

    PRIMARY KEY (user, route)
);

-- Create an expanded view about routes with info about its categories and kudos balance
CREATE VIEW routes_expandedinfo
            (id, created_by_user, title, description, distance, duration, elevation, creation_date, skill_level,
             blocked, kudos, categories)
AS
SELECT r.id,
       r.created_by_user,
       r.title,
       r.description,
       r.distance,
       r.duration,
       r.elevation,
       r.creation_date,
       r.skill_level,
       r.blocked,
       (SELECT coalesce(sum(modifier), 0) FROM routekudosregistry WHERE route = r.id),
       group_concat(DISTINCT rc.name)
FROM routes r
         INNER JOIN routetocategoriesmapping rcm ON r.id = rcm.route
         INNER JOIN routecategories rc ON rcm.category = rc.id
GROUP BY r.id
ORDER BY r.id;

-- EXTRA REQUIREMENTS

-- Top routes of the month by kudo ratings submitted within the current month. This view doesn't take into account
-- routes with negative or 0 kudo balance

CREATE VIEW top_monthly_routes_by_kudos
            (id, created_by_user, title, description, distance, duration, elevation, creation_date, skill_level,
             blocked, kudos, categories)
AS
SELECT r.id,
       r.created_by_user,
       r.title,
       r.description,
       r.distance,
       r.duration,
       r.elevation,
       r.creation_date,
       r.skill_level,
       r.blocked,
       (SELECT coalesce(sum(modifier), 0)
        FROM routekudosregistry
        WHERE route = r.id
          AND from_unixtime(submission_date) BETWEEN date_sub(current_timestamp, interval
                                                              dayofmonth(current_timestamp) - 1
                                                              day) AND current_timestamp) as kudos,
       group_concat(DISTINCT rc.name)
FROM routes r
         INNER JOIN routetocategoriesmapping rcm ON r.id = rcm.route
         INNER JOIN routecategories rc ON rcm.category = rc.id
         INNER JOIN routekudosregistry rkr on r.id = rkr.route
WHERE from_unixtime(rkr.submission_date) BETWEEN date_sub(current_timestamp, interval dayofmonth(current_timestamp) - 1
                                                          day)
          AND current_timestamp
GROUP BY r.id
HAVING kudos > 0
ORDER BY kudos DESC;

-- Top routes of the week by kudo ratings submitted within the current week (weeks start on Monday). This view doesn't
-- take into account routes with negative or 0 kudo balance

CREATE VIEW top_weekly_routes_by_kudos
            (id, created_by_user, title, description, distance, duration, elevation, creation_date, skill_level,
             blocked, kudos, categories)
AS
SELECT r.id,
       r.created_by_user,
       r.title,
       r.description,
       r.distance,
       r.duration,
       r.elevation,
       r.creation_date,
       r.skill_level,
       r.blocked,
       (SELECT coalesce(sum(modifier), 0)
        FROM routekudosregistry
        WHERE route = r.id
          AND from_unixtime(submission_date) BETWEEN date_sub(current_timestamp, interval weekday(current_timestamp) day) AND current_timestamp) AS kudos,
       group_concat(DISTINCT rc.name)
FROM routes r
         INNER JOIN routetocategoriesmapping rcm ON r.id = rcm.route
         INNER JOIN routecategories rc ON rcm.category = rc.id
         INNER JOIN routekudosregistry rkr on r.id = rkr.route
WHERE from_unixtime(rkr.submission_date) BETWEEN date_sub(current_timestamp, interval weekday(current_timestamp) day) AND current_timestamp
GROUP BY r.id
HAVING kudos > 0
ORDER BY kudos DESC;

-- Top users authors of top monthly routes ordered by descending number of top routes

CREATE VIEW top_users_by_top_monthly_routes(username, top_routes) AS
SELECT username,
       (SELECT count(DISTINCT tpr.id)
        FROM top_monthly_routes_by_kudos tpr
        WHERE u.username = tpr.created_by_user) AS top_routes
FROM users u
WHERE username IN (SELECT created_by_user FROM top_monthly_routes_by_kudos)
ORDER BY top_routes DESC;

-- Top users by average kudo ratings on their routes

CREATE VIEW top_users_by_top_avg_kudos(username, avg_kudos) AS
SELECT u.username, avg(rei.kudos) as avg_kudos
FROM routes_expandedinfo rei
         INNER JOIN users u ON rei.created_by_user = u.username
GROUP BY rei.created_by_user
HAVING avg_kudos > 0
ORDER BY avg_kudos DESC;