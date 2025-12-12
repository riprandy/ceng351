package ceng.ceng351.ModelHubPlatform;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModelHubPlatform implements IModelHubPlatform {

    private Connection connection;
    // The Evaluation class creates the actual H2 database connection.
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



    //6.2 Task 2: Insert Users
    @Override
    public int insertUsers(User[] users) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert Organizations
    @Override
    public int insertOrganizations(Organization[] organizations) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert Tasks
    @Override
    public int insertTasks(Task[] tasks) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert Datasets
    @Override
    public int insertDatasets(Dataset[] datasets) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert Publications
    @Override
    public int insertPublications(Publication[] publications) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert Profiles
    @Override
    public int insertProfiles(Profile[] profiles) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert follows
    @Override
    public int insertfollows(follow[] follows) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert Models
    @Override
    public int insertModels(Model[] models) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert uploads
    @Override
    public int insertuploads(upload[] uploads) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert ModelVersions
    @Override
    public int insertModelVersions(ModelVersion[] modelVersions) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert designed_for
    @Override
    public int insertdesigned_fors(designed_for[] designedFors) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert runs
    @Override
    public int insertruns(run[] runs) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert Results
    @Override
    public int insertResults(Result[] results) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }

    //6.2 Task 2: Insert includes
    @Override
    public int insertincludes(include[] includes) {
        int rowsInserted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsInserted;
    }


    //6.3 Task 3: Find Users Without Profiles
    @Override
    public User[] getUsersWithoutProfiles() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new User[0];
    }


    //6.4 Task 4: Decrease Reputation for Users Without Profiles
    @Override
    public int decreaseReputationForMissingProfiles() {
        int rowsUpdated = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsUpdated;
    }


    //6.5 Task 5: Find Users With Specific Bio Keywords
    @Override
    public QueryResult.UserPINNameReputationBio[] getUsersByBioKeywords() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new QueryResult.UserPINNameReputationBio[0];
    }


    //6.6 Task 6: Find Organizations With No Released Models and Low Rating
    @Override
    public Organization[] getOrganizationsWithNoReleasedModelsAndLowRating() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new Organization[0];
    }


    // 6.7 Task 7: Delete Organizations With No Released Models and Low Rating
    @Override
    public int deleteOrganizationsWithNoReleasedModelsAndLowRating() {
        int rowsDeleted = 0;

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return rowsDeleted;
    }


    // 6.8 Task 8: Retrieve Models and Their Primary Task Information
    @Override
    public QueryResult.ModelPrimaryTaskInfo[] getModelPrimaryTaskInfo() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new QueryResult.ModelPrimaryTaskInfo[0];
    }


    // 6.9 Task 9: Compute User Popularity Score
    @Override
    public QueryResult.UserPopularityInfo[] getUserPopularityScore() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new QueryResult.UserPopularityInfo[0];
    }


    // 6.10 Task 10: Comprehensive Model Information
    @Override
    public QueryResult.ComprehensiveModelInfo[] getComprehensiveModelInfo() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new QueryResult.ComprehensiveModelInfo[0];
    }


    // 6.11 Task 11: Dataset Statistics by Modality
    @Override
    public QueryResult.DatasetStatisticsByModality[] getDatasetStatisticsByModality() {


        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new QueryResult.DatasetStatisticsByModality[0];
    }


    // 6.12 Task 12: Retrieve Large-Parameter Model Versions Within a Date Range
    @Override
    public QueryResult.LargeModelVersionInfo[] getLargeModelVersionsByDateRange(String start_date, String end_date) {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new QueryResult.LargeModelVersionInfo[0];
    }


    // 6.13 Task 13: Find Dataset(s) With Maximum Upload Count
    @Override
    public QueryResult.DatasetMaxUploadInfo[] getDatasetsWithMaxUploadCount() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new QueryResult.DatasetMaxUploadInfo[0];
    }


    // 6.14 Task 14: Find Complete Dataset(s) with All Roles
    @Override
    public Dataset[] getCompleteDatasets() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new Dataset[0];
    }


    // 6.15 Task 15: Find Users Who Uploaded Datasets with Role 'creator' or 'contributor' but Never 'validator' and Have Reputation ≥ 60
    @Override
    public User[] getUsersCreatorOrContributorButNotValidator() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new User[0];
    }


    // 6.16 Task 16: Find Users Who Ran All Versions of at Least One Model
    @Override
    public QueryResult.UserModelVersionInfo[] getUsersWhoRanAllVersionsOfModels() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new QueryResult.UserModelVersionInfo[0];
    }


    // 6.17 Task 17: Run-Type Statistics
    @Override
    public QueryResult.RunTypeStats[] getRunTypeStatistics() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new QueryResult.RunTypeStats[0];
    }


    // 6.18 Task 18: Find Publications That Include Results From Runs of a Dataset
    @Override
    public Publication[] getPublicationsUsingDataset(String dataset_name) {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new Publication[0];
    }


    // 6.19 Task 19: Find Top 10 Highly-Reputed Users
    @Override
    public QueryResult.HighlyReputedUser[] getTopTenHighlyReputedUsers() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new QueryResult.HighlyReputedUser[0];
    }


    // 6.20 Task 20: Find Vulnerability Detection Publications
    @Override
    public QueryResult.TaskSpecificPublication[] getVulnerabilityDetectionPublications() {

        /*****************************************************/
        /*****************************************************/
        /*********************  TODO  ***********************/
        /*****************************************************/
        /*****************************************************/

        return new QueryResult.TaskSpecificPublication[0];
    }


}