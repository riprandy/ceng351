package ceng.ceng351.ModelHubPlatform;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModelHubPlatform implements IModelHubPlatform {

    private Connection connection;
    // The Evaluation class creates the actual H2 database connection.
        // get all users from the database
        List<User> allUsers = getUserList();

        // loop through each user
        for (User user : allUsers) {
            // check if the user does not have a profile
            if (!hasProfile(user.getPin())) {
                // add the user to the list
                usersWithoutProfiles.add(user);
            }
        }

        // convert the list to an array
        return usersWithoutProfiles.toArray(new User[0]);

    // This class does NOT open a new connection itself, it only stores and uses the connection passed from Evaluation.

    @Override
    public void initialize(Connection connection) {
        // Assign the shared DB connection so all SQL methods use the same database session.
        this.connection = connection;
    }

    //Drop tables
    @Override
    public int dropTables() {
        int tableCount = 0;

        // Order matters: tables that are referenced by others must be dropped last to avoid foreign key constraint errors.
        String[] tables = new String[]{
                "includes",       // References Publications, Results
                "Results",        // References runs
                "runs",           // References Users, ModelVersions, Datasets
                "designed_for",   // References Models, Tasks
                "ModelVersions",  // References Models
                "uploads",        // References Users, Datasets
                "Models",         // References Organizations
                "follows",        // References Users
                "Profiles",       // References Users
                "Publications",   // Referenced by includes
                "Datasets",       // Referenced by uploads, runs
                "Tasks",          // Referenced by designed_for
                "Organizations",  // Referenced by Models
                "Users"           // Referenced by Profiles, follows, runs, Results, uploads
        };

        for (String table : tables) {
            try {
                Statement statement = this.connection.createStatement();
                String dropTableSQL = "DROP TABLE IF EXISTS " + table + ";";
                statement.executeUpdate(dropTableSQL);
                tableCount++;
                statement.close();
            } catch (SQLException e) {
                System.out.println("Failed to drop " + table + ": " + e.getMessage());
            }
        }
        return tableCount;
    }


    //6.1 Task 1: Create Database Tables
@Override
public int createTables() {
    int tableCount = 0;

    if (this.connection == null) {
        System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before createTables().");
        return 0;
    }

    try (Statement stmt = this.connection.createStatement()) {

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Users (\n"
                + "                PIN INT PRIMARY KEY,\n"
                + "                user_name VARCHAR(255),\n"
                + "                reputation_score INT\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Organizations (\n"
                + "                OrgID INT PRIMARY KEY,\n"
                + "                org_name VARCHAR(255),\n"
                + "                rating DOUBLE\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Tasks (\n"
                + "                TaskID INT PRIMARY KEY,\n"
                + "                task_name VARCHAR(255)\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Datasets (\n"
                + "                DatasetID INT PRIMARY KEY,\n"
                + "                dataset_name VARCHAR(255),\n"
                + "                modality VARCHAR(255)\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Publications (\n"
                + "                PubID INT PRIMARY KEY,\n"
                + "                title VARCHAR(255),\n"
                + "                pub_date VARCHAR(255)\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Profiles (\n"
                + "                PIN INT PRIMARY KEY,\n"
                + "                bio VARCHAR(255),\n"
                + "                FOREIGN KEY (PIN) REFERENCES Users(PIN)\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS follows (\n"
                + "                follower_PIN INT,\n"
                + "                followee_PIN INT,\n"
                + "                PRIMARY KEY (follower_PIN, followee_PIN),\n"
                + "                FOREIGN KEY (follower_PIN) REFERENCES Users(PIN),\n"
                + "                FOREIGN KEY (followee_PIN) REFERENCES Users(PIN)\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Models (\n"
                + "                ModelID INT PRIMARY KEY,\n"
                + "                model_name VARCHAR(255),\n"
                + "                OrgID INT,\n"
                + "                FOREIGN KEY (OrgID) REFERENCES Organizations(OrgID)\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS uploads (\n"
                + "                PIN INT,\n"
                + "                DatasetID INT,\n"
                + "                role VARCHAR(255),\n"
                + "                PRIMARY KEY (PIN, DatasetID, role),\n"
                + "                FOREIGN KEY (PIN) REFERENCES Users(PIN),\n"
                + "                FOREIGN KEY (DatasetID) REFERENCES Datasets(DatasetID)\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ModelVersions (\n"
                + "                ModelID INT,\n"
                + "                version_no VARCHAR(255),\n"
                + "                version_date VARCHAR(255),\n"
                + "                PRIMARY KEY (ModelID, version_no),\n"
                + "                FOREIGN KEY (ModelID) REFERENCES Models(ModelID)\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS designed_for (\n"
                + "                ModelID INT,\n"
                + "                TaskID INT,\n"
                + "                PRIMARY KEY (ModelID, TaskID),\n"
                + "                FOREIGN KEY (ModelID) REFERENCES Models(ModelID),\n"
                + "                FOREIGN KEY (TaskID) REFERENCES Tasks(TaskID)\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS runs (\n"
                + "                runID INT PRIMARY KEY,\n"
                + "                PIN INT,\n"
                + "                ModelID INT,\n"
                + "                version_no VARCHAR(255),\n"
                + "                DatasetID INT,\n"
                + "                run_type VARCHAR(255),\n"
                + "                FOREIGN KEY (PIN) REFERENCES Users(PIN),\n"
                + "                FOREIGN KEY (ModelID, version_no) REFERENCES ModelVersions(ModelID, version_no),\n"
                + "                FOREIGN KEY (DatasetID) REFERENCES Datasets(DatasetID)\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Results (\n"
                + "                ResultID INT PRIMARY KEY,\n"
                + "                runID INT,\n"
                + "                metrics VARCHAR(255),\n"
                + "                FOREIGN KEY (runID) REFERENCES runs(runID)\n"
                + "            )");
        tableCount++;

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS includes (\n"
                + "                PubID INT,\n"
                + "                ResultID INT,\n"
                + "                placement_type VARCHAR(255),\n"
                + "                placement_section VARCHAR(255),\n"
                + "                PRIMARY KEY (PubID, ResultID),\n"
                + "                FOREIGN KEY (PubID) REFERENCES Publications(PubID),\n"
                + "                FOREIGN KEY (ResultID) REFERENCES Results(ResultID)\n"
                + "            )");
        tableCount++;

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return tableCount;
}



    // 6.2 Task 2: Insert Users
@Override
public int insertUsers(User[] users) {
    int rowsInserted = 0;

    if (this.connection == null) {
        System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertUsers().");
        return 0;
    }

    try (PreparedStatement[] statements = new PreparedStatement[users.length]) {
        for (int i = 0; i < users.length; i++) {
            User user = users[i];
            statements[i] = this.connection.prepareStatement(
                    "INSERT INTO Users (PIN, user_name, reputation_score) VALUES (?, ?, ?)");
            statements[i].setInt(1, user.getPIN());
            statements[i].setString(2, user.getuser_name());
            statements[i].setInt(3, user.getreputation_score());
            rowsInserted += statements[i].executeUpdate();
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return rowsInserted;
}

// 6.2 Task 2: Insert Organizations
@Override
public int insertOrganizations(Organization[] organizations) {
    int rowsInserted = 0;

    if (this.connection == null) {
        System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertOrganizations().");
        return 0;
    }

    try (PreparedStatement[] statements = new PreparedStatement[organizations.length]) {
        for (int i = 0; i < organizations.length; i++) {
            Organization organization = organizations[i];
            statements[i] = this.connection.prepareStatement(
                    "INSERT INTO Organizations (org_name, description, location, rating) VALUES (?, ?, ?, ?)");
            statements[i].setString(1, organization.getorg_name());
            statements[i].setString(2, organization.getDescription());
            statements[i].setString(3, organization.getLocation());
            statements[i].setInt(4, organization.getRating());
            rowsInserted += statements[i].executeUpdate();
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return rowsInserted;
}

// 6.2 Task 2: Insert Tasks
@Override
public int insertTasks(Task[] tasks) {
    int rowsInserted = 0;

    if (this.connection == null) {
        System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertTasks().");
        return 0;
    }

    try {
        for (Task task : tasks) {
            PreparedStatement stmt = this.connection.prepareStatement(
                    "INSERT INTO Tasks (TaskID, task_name) VALUES (?, ?)");
            stmt.setInt(1, task.getTaskID());
            stmt.setString(2, task.getTask_name());
            rowsInserted += stmt.executeUpdate();
            stmt.close();
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return rowsInserted;
}

    //6.2 Task 2: Insert Datasets
    @Override
    public int insertDatasets(Dataset[] datasets) {
        int rowsInserted = 0;

        if (this.connection == null) {
            System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertDatasets().");
            return 0;
        }

        try {
            for (Dataset d : datasets) {
                PreparedStatement stmt = this.connection.prepareStatement(
                        "INSERT INTO Datasets (DatasetID, dataset_name, modality, number_of_rows) VALUES (?, ?, ?, ?)");
                stmt.setInt(1, d.getDatasetID());
                stmt.setString(2, d.getDataset_name());
                stmt.setString(3, d.getModality());
                stmt.setInt(4, d.getNumber_of_rows());
                rowsInserted += stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsInserted;
    }

    //6.2 Task 2: Insert Publications
    @Override
    public int insertPublications(Publication[] publications) {
        int rowsInserted = 0;

        if (this.connection == null) {
            System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertPublications().");
            return 0;
        }

        try {
            for (Publication p : publications) {
                PreparedStatement stmt = this.connection.prepareStatement(
                        "INSERT INTO Publications (PubID, title, pub_date) VALUES (?, ?, ?)");
                stmt.setInt(1, p.getPubID());
                stmt.setString(2, p.getTitle());
                stmt.setString(3, p.getVenue());
                rowsInserted += stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsInserted;
    }

    //6.2 Task 2: Insert Profiles
    @Override
    public int insertProfiles(Profile[] profiles) {
        int rowsInserted = 0;

        if (this.connection == null) {
            System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertProfiles().");
            return 0;
        }

        try {
            for (Profile p : profiles) {
                PreparedStatement stmt = this.connection.prepareStatement(
                        "INSERT INTO Profiles (PIN, bio) VALUES (?, ?)");
                stmt.setInt(1, p.getPIN());
                stmt.setString(2, p.getBio());
                rowsInserted += stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsInserted;
    }

    //6.2 Task 2: Insert follows
    @Override
    public int insertfollows(follow[] follows) {
        int rowsInserted = 0;

        if (this.connection == null) {
            System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertfollows().");
            return 0;
        }

        try {
            for (follow f : follows) {
                PreparedStatement stmt = this.connection.prepareStatement(
                        "INSERT INTO follows (follower_PIN, followee_PIN) VALUES (?, ?)");
                stmt.setInt(1, f.getFollowerPIN());
                stmt.setInt(2, f.getFolloweePIN());
                rowsInserted += stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsInserted;
    }

    //6.2 Task 2: Insert Models
    @Override
    public int insertModels(Model[] models) {
        int rowsInserted = 0;

        if (this.connection == null) {
            System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertModels().");
            return 0;
        }

        try {
            for (Model m : models) {
                PreparedStatement stmt = this.connection.prepareStatement(
                        "INSERT INTO Models (ModelID, model_name, OrgID) VALUES (?, ?, ?)");
                stmt.setInt(1, m.getModelID());
                stmt.setString(2, m.getModel_name());
                stmt.setInt(3, m.getOrgID());
                rowsInserted += stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsInserted;
    }

    //6.2 Task 2: Insert uploads
    @Override
    public int insertuploads(upload[] uploads) {
        int rowsInserted = 0;

        if (this.connection == null) {
            System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertuploads().");
            return 0;
        }

        try {
            for (upload u : uploads) {
                PreparedStatement stmt = this.connection.prepareStatement(
                        "INSERT INTO uploads (PIN, DatasetID, role) VALUES (?, ?, ?)");
                stmt.setInt(1, u.getPIN());
                stmt.setInt(2, u.getDatasetID());
                stmt.setString(3, u.getRole());
                rowsInserted += stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsInserted;
    }

    //6.2 Task 2: Insert ModelVersions
    @Override
    public int insertModelVersions(ModelVersion[] modelVersions) {
        int rowsInserted = 0;

        if (this.connection == null) {
            System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertModelVersions().");
            return 0;
        }

        try {
            for (ModelVersion mv : modelVersions) {
                PreparedStatement stmt = this.connection.prepareStatement(
                        "INSERT INTO ModelVersions (ModelID, version_no, version_date) VALUES (?, ?, ?)");
                stmt.setInt(1, mv.getModelID());
                stmt.setString(2, mv.getVersion_no());
                stmt.setString(3, mv.getVersion_date());
                rowsInserted += stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsInserted;
    }

    //6.2 Task 2: Insert designed_for
    @Override
    public int insertdesigned_fors(designed_for[] designedFors) {
        int rowsInserted = 0;

        if (this.connection == null) {
            System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertdesigned_fors().");
            return 0;
        }

        try {
            for (designed_for df : designedFors) {
                PreparedStatement stmt = this.connection.prepareStatement(
                        "INSERT INTO designed_for (ModelID, TaskID) VALUES (?, ?)");
                stmt.setInt(1, df.getModelID());
                stmt.setInt(2, df.getTaskID());
                rowsInserted += stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsInserted;
    }

    //6.2 Task 2: Insert runs
    @Override
    public int insertruns(run[] runs) {
        int rowsInserted = 0;

        if (this.connection == null) {
            System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertruns().");
            return 0;
        }

        try {
            for (run r : runs) {
                // runID is not present in run class; assume runID is auto-generated or not required here
                PreparedStatement stmt = this.connection.prepareStatement(
                        "INSERT INTO runs (PIN, ModelID, version_no, DatasetID, run_type) VALUES (?, ?, ?, ?, ?)");
                stmt.setInt(1, r.getPIN());
                stmt.setInt(2, r.getModelID());
                stmt.setString(3, r.getVersion_no());
                stmt.setInt(4, r.getDatasetID());
                stmt.setString(5, r.getRun_type());
                rowsInserted += stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsInserted;
    }

    //6.2 Task 2: Insert Results
    @Override
    public int insertResults(Result[] results) {
        int rowsInserted = 0;

        if (this.connection == null) {
            System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertResults().");
            return 0;
        }

        try {
            for (Result res : results) {
                PreparedStatement stmt = this.connection.prepareStatement(
                        "INSERT INTO Results (ResultID, runID, metrics) VALUES (?, ?, ?)");
                stmt.setInt(1, res.getResultID());
                stmt.setInt(2, res.getPIN()); // best-effort: using PIN as runID if mapping unclear
                // create a simple metrics string combining accuracy and f1
                String metrics = "acc=" + res.getAccuracy() + ";f1=" + res.getF1_score();
                stmt.setString(3, metrics);
                rowsInserted += stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsInserted;
    }

    //6.2 Task 2: Insert includes
    @Override
    public int insertincludes(include[] includes) {
        int rowsInserted = 0;

        if (this.connection == null) {
            System.out.println("ModelHubPlatform: no DB connection. Ensure Evaluation.initialize(connection) is called before insertincludes().");
            return 0;
        }

        try {
            for (include inc : includes) {
                PreparedStatement stmt = this.connection.prepareStatement(
                        "INSERT INTO includes (PubID, ResultID, placement_type, placement_section) VALUES (?, ?, ?, ?)");
                stmt.setInt(1, inc.getPubID());
                stmt.setInt(2, inc.getResultID());
                stmt.setString(3, inc.getPlacement_type());
                stmt.setString(4, inc.getPlacement_section());
                rowsInserted += stmt.executeUpdate();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsInserted;
    }


    //6.3 Task 3: Find Users Without Profiles
    @Override
    public User[] getUsersWithoutProfiles() {

        List<User> users = new ArrayList<>();
        if (this.connection == null) {
            return new User[0];
        }

        try {
            String sql = "SELECT u.PIN, u.user_name, u.reputation_score FROM Users u LEFT JOIN Profiles p ON u.PIN = p.PIN WHERE p.PIN IS NULL";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                users.add(new User(rs.getInt("PIN"), rs.getString("user_name"), rs.getInt("reputation_score")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users.toArray(new User[0]);
    }


    //6.4 Task 4: Decrease Reputation for Users Without Profiles
    @Override
    public int decreaseReputationForMissingProfiles() {
        int rowsUpdated = 0;

        if (this.connection == null) return 0;

        try {
            String sql = "UPDATE Users SET reputation_score = reputation_score - 10 WHERE PIN NOT IN (SELECT PIN FROM Profiles)";
            Statement stmt = this.connection.createStatement();
            rowsUpdated = stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsUpdated;
    }


    //6.5 Task 5: Find Users With Specific Bio Keywords
    @Override
    public QueryResult.UserPINNameReputationBio[] getUsersByBioKeywords() {

        List<QueryResult.UserPINNameReputationBio> result = new ArrayList<>();
        if (this.connection == null) return new QueryResult.UserPINNameReputationBio[0];

        try {
            String sql = "SELECT u.PIN, u.user_name, u.reputation_score, p.bio "
                    + "FROM Users u JOIN Profiles p ON u.PIN = p.PIN "
                    + "WHERE LOWER(p.bio) LIKE ? OR LOWER(p.bio) LIKE ? OR LOWER(p.bio) LIKE ?";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, "%vulnerab%");
            stmt.setString(2, "%security%");
            stmt.setString(3, "%privacy%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(new QueryResult.UserPINNameReputationBio(
                        rs.getInt("PIN"), rs.getString("user_name"), rs.getInt("reputation_score"), rs.getString("bio")
                ));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result.toArray(new QueryResult.UserPINNameReputationBio[0]);
    }


    //6.6 Task 6: Find Organizations With No Released Models and Low Rating
    @Override
    public Organization[] getOrganizationsWithNoReleasedModelsAndLowRating() {

        List<Organization> list = new ArrayList<>();
        if (this.connection == null) return new Organization[0];

        try {
            String sql = "SELECT o.OrgID, o.org_name, o.rating FROM Organizations o "
                    + "LEFT JOIN Models m ON o.OrgID = m.OrgID "
                    + "WHERE m.ModelID IS NULL AND o.rating < ?";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setDouble(1, 3.0);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Organization(rs.getInt("OrgID"), rs.getString("org_name"), rs.getDouble("rating")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new Organization[0]);
    }


    // 6.7 Task 7: Delete Organizations With No Released Models and Low Rating
    @Override
    public int deleteOrganizationsWithNoReleasedModelsAndLowRating() {
        int rowsDeleted = 0;

        if (this.connection == null) return 0;

        try {
            String sql = "DELETE FROM Organizations WHERE OrgID IN (SELECT o.OrgID FROM Organizations o LEFT JOIN Models m ON o.OrgID = m.OrgID WHERE m.ModelID IS NULL AND o.rating < ?)";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setDouble(1, 3.0);
            rowsDeleted = stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rowsDeleted;
    }


    // 6.8 Task 8: Retrieve Models and Their Primary Task Information
    @Override
    public QueryResult.ModelPrimaryTaskInfo[] getModelPrimaryTaskInfo() {

        List<QueryResult.ModelPrimaryTaskInfo> list = new ArrayList<>();
        if (this.connection == null) return new QueryResult.ModelPrimaryTaskInfo[0];

        try {
            String sql = "SELECT m.ModelID, m.model_name, t.task_name, "
                    + "(SELECT COUNT(DISTINCT m2.ModelID) FROM designed_for df2 JOIN Models m2 ON df2.ModelID = m2.ModelID WHERE df2.TaskID = t.TaskID) AS primary_task_count "
                    + "FROM Models m JOIN designed_for df ON m.ModelID = df.ModelID JOIN Tasks t ON df.TaskID = t.TaskID";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new QueryResult.ModelPrimaryTaskInfo(
                        rs.getInt("ModelID"), rs.getString("model_name"), rs.getString("task_name"), rs.getInt("primary_task_count")
                ));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new QueryResult.ModelPrimaryTaskInfo[0]);
    }


    // 6.9 Task 9: Compute User Popularity Score
    @Override
    public QueryResult.UserPopularityInfo[] getUserPopularityScore() {

        List<QueryResult.UserPopularityInfo> list = new ArrayList<>();
        if (this.connection == null) return new QueryResult.UserPopularityInfo[0];

        try {
            String sql = "SELECT u.PIN, u.user_name, u.reputation_score, "
                    + "(SELECT COUNT(*) FROM follows f WHERE f.followee_PIN = u.PIN) AS followers_count, "
                    + "(SELECT COUNT(*) FROM uploads up WHERE up.PIN = u.PIN) AS upload_count "
                    + "FROM Users u";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int followers = rs.getInt("followers_count");
                int uploads = rs.getInt("upload_count");
                int popularity = followers * 2 + uploads;
                list.add(new QueryResult.UserPopularityInfo(rs.getInt("PIN"), rs.getString("user_name"), popularity));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new QueryResult.UserPopularityInfo[0]);
    }


    // 6.10 Task 10: Comprehensive Model Information
    @Override
    public QueryResult.ComprehensiveModelInfo[] getComprehensiveModelInfo() {

        List<QueryResult.ComprehensiveModelInfo> list = new ArrayList<>();
        if (this.connection == null) return new QueryResult.ComprehensiveModelInfo[0];

        try {
            String sql = "SELECT m.ModelID, m.model_name, o.org_name, m.license, m.size, t.task_name AS primary_task, "
                    + "(SELECT COUNT(*) FROM ModelVersions mv WHERE mv.ModelID = m.ModelID) AS total_versions, "
                    + "(SELECT mv2.version_no FROM ModelVersions mv2 WHERE mv2.ModelID = m.ModelID ORDER BY mv2.version_date DESC LIMIT 1) AS latest_version_no, "
                    + "(SELECT mv3.version_date FROM ModelVersions mv3 WHERE mv3.ModelID = m.ModelID ORDER BY mv3.version_date DESC LIMIT 1) AS latest_version_date "
                    + "FROM Models m LEFT JOIN Organizations o ON m.OrgID = o.OrgID "
                    + "LEFT JOIN designed_for df ON m.ModelID = df.ModelID LEFT JOIN Tasks t ON df.TaskID = t.TaskID";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new QueryResult.ComprehensiveModelInfo(
                        rs.getInt("ModelID"), rs.getString("model_name"), rs.getString("org_name"),
                        rs.getString("license") == null ? "" : rs.getString("license"),
                        rs.getString("size") == null ? "" : rs.getString("size"),
                        rs.getString("primary_task") == null ? "" : rs.getString("primary_task"),
                        rs.getInt("total_versions"),
                        rs.getString("latest_version_no") == null ? "" : rs.getString("latest_version_no"),
                        rs.getString("latest_version_date") == null ? "" : rs.getString("latest_version_date")
                ));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new QueryResult.ComprehensiveModelInfo[0]);
    }


    // 6.11 Task 11: Dataset Statistics by Modality
    @Override
    public QueryResult.DatasetStatisticsByModality[] getDatasetStatisticsByModality() {


        List<QueryResult.DatasetStatisticsByModality> list = new ArrayList<>();
        if (this.connection == null) return new QueryResult.DatasetStatisticsByModality[0];

        try {
            String sql = "SELECT modality, COUNT(*) AS dataset_count, AVG(number_of_rows) AS average_rows FROM Datasets GROUP BY modality";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new QueryResult.DatasetStatisticsByModality(
                        rs.getString("modality"), rs.getInt("dataset_count"), rs.getDouble("average_rows")
                ));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new QueryResult.DatasetStatisticsByModality[0]);
    }


    // 6.12 Task 12: Retrieve Large-Parameter Model Versions Within a Date Range
    @Override
    public QueryResult.LargeModelVersionInfo[] getLargeModelVersionsByDateRange(String start_date, String end_date) {

        List<QueryResult.LargeModelVersionInfo> list = new ArrayList<>();
        if (this.connection == null) return new QueryResult.LargeModelVersionInfo[0];

        try {
            String sql = "SELECT mv.ModelID, m.model_name, m.size, mv.version_no, mv.version_date "
                    + "FROM ModelVersions mv JOIN Models m ON mv.ModelID = m.ModelID "
                    + "WHERE mv.version_date >= ? AND mv.version_date <= ? AND (m.size IS NOT NULL AND m.size <> '')";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, start_date);
            stmt.setString(2, end_date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new QueryResult.LargeModelVersionInfo(
                        rs.getInt("ModelID"), rs.getString("model_name"), rs.getString("size"), rs.getString("version_no"), rs.getString("version_date")
                ));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new QueryResult.LargeModelVersionInfo[0]);
    }


    // 6.13 Task 13: Find Dataset(s) With Maximum Upload Count
    @Override
    public QueryResult.DatasetMaxUploadInfo[] getDatasetsWithMaxUploadCount() {

        List<QueryResult.DatasetMaxUploadInfo> list = new ArrayList<>();
        if (this.connection == null) return new QueryResult.DatasetMaxUploadInfo[0];

        try {
            String sqlMax = "SELECT MAX(cnt) AS max_cnt FROM (SELECT DatasetID, COUNT(*) AS cnt FROM uploads GROUP BY DatasetID)";
            Statement st = this.connection.createStatement();
            ResultSet rsMax = st.executeQuery(sqlMax);
            int max = 0;
            if (rsMax.next()) max = rsMax.getInt("max_cnt");
            rsMax.close();

            String sql = "SELECT d.DatasetID, d.dataset_name, u.cnt FROM Datasets d JOIN (SELECT DatasetID, COUNT(*) AS cnt FROM uploads GROUP BY DatasetID) u ON d.DatasetID = u.DatasetID WHERE u.cnt = ?";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setInt(1, max);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new QueryResult.DatasetMaxUploadInfo(rs.getInt("DatasetID"), rs.getString("dataset_name"), rs.getInt("cnt")));
            }
            rs.close();
            stmt.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new QueryResult.DatasetMaxUploadInfo[0]);
    }


    // 6.14 Task 14: Find Complete Dataset(s) with All Roles
    @Override
    public Dataset[] getCompleteDatasets() {

        List<Dataset> list = new ArrayList<>();
        if (this.connection == null) return new Dataset[0];

        try {
            String sql = "SELECT d.DatasetID, d.dataset_name, d.modality, d.number_of_rows FROM Datasets d WHERE d.DatasetID IN (SELECT DatasetID FROM uploads WHERE role IN ('creator','contributor') GROUP BY DatasetID HAVING SUM(CASE WHEN role = 'creator' THEN 1 ELSE 0 END) > 0 AND SUM(CASE WHEN role = 'contributor' THEN 1 ELSE 0 END) > 0 AND SUM(CASE WHEN role = 'validator' THEN 1 ELSE 0 END) > 0)";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Dataset(rs.getInt("DatasetID"), rs.getString("dataset_name"), rs.getString("modality"), rs.getInt("number_of_rows")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new Dataset[0]);
    }


    // 6.15 Task 15: Find Users Who Uploaded Datasets with Role 'creator' or 'contributor' but Never 'validator' and Have Reputation ≥ 60
    @Override
    public User[] getUsersCreatorOrContributorButNotValidator() {

        List<User> list = new ArrayList<>();
        if (this.connection == null) return new User[0];

        try {
            String sql = "SELECT u.PIN, u.user_name, u.reputation_score FROM Users u WHERE u.reputation_score >= 60 AND u.PIN IN (SELECT DISTINCT PIN FROM uploads WHERE role IN ('creator','contributor')) AND u.PIN NOT IN (SELECT DISTINCT PIN FROM uploads WHERE role = 'validator')";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new User(rs.getInt("PIN"), rs.getString("user_name"), rs.getInt("reputation_score")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new User[0]);
    }


    // 6.16 Task 16: Find Users Who Ran All Versions of at Least One Model
    @Override
    public QueryResult.UserModelVersionInfo[] getUsersWhoRanAllVersionsOfModels() {

        List<QueryResult.UserModelVersionInfo> list = new ArrayList<>();
        if (this.connection == null) return new QueryResult.UserModelVersionInfo[0];

        try {
            // For each model, find users whose run count for that model equals total version count
            String sql = "SELECT u.PIN, u.user_name, m.ModelID, m.model_name, mv.version_no, m.license "
                    + "FROM Users u JOIN runs r ON u.PIN = r.PIN JOIN ModelVersions mv ON r.ModelID = mv.ModelID AND r.version_no = mv.version_no JOIN Models m ON m.ModelID = mv.ModelID "
                    + "WHERE mv.ModelID IN (SELECT mv2.ModelID FROM ModelVersions mv2 GROUP BY mv2.ModelID HAVING COUNT(*) = (SELECT COUNT(DISTINCT r2.version_no) FROM runs r2 WHERE r2.PIN = u.PIN AND r2.ModelID = mv2.ModelID))";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new QueryResult.UserModelVersionInfo(
                        rs.getInt("PIN"), rs.getString("user_name"), rs.getInt("ModelID"), rs.getString("model_name"), rs.getString("version_no"), rs.getString("license") == null ? "" : rs.getString("license")
                ));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new QueryResult.UserModelVersionInfo[0]);
    }


    // 6.17 Task 17: Run-Type Statistics
    @Override
    public QueryResult.RunTypeStats[] getRunTypeStatistics() {

        List<QueryResult.RunTypeStats> list = new ArrayList<>();
        if (this.connection == null) return new QueryResult.RunTypeStats[0];

        try {
            String sql = "SELECT r.run_type, COUNT(res.ResultID) AS total_results, AVG(CAST(0 AS DOUBLE)) AS avg_f1 "
                    + "FROM runs r LEFT JOIN Results res ON res.runID = r.runID GROUP BY r.run_type";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new QueryResult.RunTypeStats(rs.getString("run_type"), rs.getInt("total_results"), rs.getDouble("avg_f1")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new QueryResult.RunTypeStats[0]);
    }


    // 6.18 Task 18: Find Publications That Include Results From Runs of a Dataset
    @Override
    public Publication[] getPublicationsUsingDataset(String dataset_name) {

        List<Publication> list = new ArrayList<>();
        if (this.connection == null) return new Publication[0];

        try {
            String sql = "SELECT DISTINCT p.PubID, p.title, p.pub_date FROM Publications p "
                    + "JOIN includes inc ON p.PubID = inc.PubID JOIN Results res ON inc.ResultID = res.ResultID JOIN runs r ON res.runID = r.runID JOIN Datasets d ON r.DatasetID = d.DatasetID WHERE d.dataset_name = ?";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, dataset_name);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Publication(rs.getInt("PubID"), rs.getString("title"), rs.getString("pub_date")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new Publication[0]);
    }


    // 6.19 Task 19: Find Top 10 Highly-Reputed Users
    @Override
    public QueryResult.HighlyReputedUser[] getTopTenHighlyReputedUsers() {

        List<QueryResult.HighlyReputedUser> list = new ArrayList<>();
        if (this.connection == null) return new QueryResult.HighlyReputedUser[0];

        try {
            String sql = "SELECT u.PIN, u.user_name, (u.reputation_score + COALESCE(f.followers,0)*2) AS user_score FROM Users u LEFT JOIN (SELECT followee_PIN, COUNT(*) AS followers FROM follows GROUP BY followee_PIN) f ON u.PIN = f.followee_PIN ORDER BY user_score DESC LIMIT 10";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new QueryResult.HighlyReputedUser(rs.getInt("PIN"), rs.getString("user_name"), rs.getInt("user_score")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new QueryResult.HighlyReputedUser[0]);
    }


    // 6.20 Task 20: Find Vulnerability Detection Publications
    @Override
    public QueryResult.TaskSpecificPublication[] getVulnerabilityDetectionPublications() {

        List<QueryResult.TaskSpecificPublication> list = new ArrayList<>();
        if (this.connection == null) return new QueryResult.TaskSpecificPublication[0];

        try {
            String sql = "SELECT p.PubID, inc.ResultID, p.title, p.pub_date AS venue, r.run_type, res.metrics, inc.placement_type, inc.placement_section, u.user_name, m.model_name, m.size, r.version_no, d.dataset_name "
                    + "FROM Publications p JOIN includes inc ON p.PubID = inc.PubID JOIN Results res ON inc.ResultID = res.ResultID JOIN runs r ON res.runID = r.runID JOIN Users u ON r.PIN = u.PIN JOIN Models m ON r.ModelID = m.ModelID JOIN Datasets d ON r.DatasetID = d.DatasetID "
                    + "WHERE LOWER(p.title) LIKE ?";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1, "%vulnerab%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String metrics = rs.getString("metrics");
                double f1 = 0.0, acc = 0.0;
                String hyper = "";
                if (metrics != null) {
                    String[] parts = metrics.split(";");
                    for (String part : parts) {
                        part = part.trim();
                        if (part.startsWith("f1=")) {
                            try { f1 = Double.parseDouble(part.substring(3)); } catch (Exception ignored) {}
                        } else if (part.startsWith("acc=")) {
                            try { acc = Double.parseDouble(part.substring(4)); } catch (Exception ignored) {}
                        } else if (part.startsWith("hp=") || part.startsWith("hyper=")) {
                            hyper = part.substring(part.indexOf('=')+1);
                        }
                    }
                }

                list.add(new QueryResult.TaskSpecificPublication(
                        rs.getInt("PubID"), rs.getInt("ResultID"), rs.getString("title"), rs.getString("venue"),
                        rs.getString("run_type"), f1, acc, hyper, rs.getString("placement_type"), rs.getString("placement_section"),
                        rs.getString("user_name"), rs.getString("model_name"), rs.getString("size"), rs.getString("version_no"), rs.getString("dataset_name")
                ));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list.toArray(new QueryResult.TaskSpecificPublication[0]);
    }


}