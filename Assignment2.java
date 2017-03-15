import java.sql.*;

// Remember that part of your mark is for doing as much in SQL (not Java) 
// as you can. At most you can justify using an array, or the more flexible
// ArrayList. Don't go crazy with it, though. You need it rarely if at all.
import java.util.ArrayList;

public class Assignment2 {

    // A connection to the database
    Connection connection;
    
    // string for the query
    String queryString;
    
    // prepared statement for the query
    PreparedStatement ps;
    
    // result set for the query
    ResultSet rs;
    
    
    Assignment2() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connects to the database and sets the search path.
     * 
     * Establishes a connection to be used for this session, assigning it to the
     * instance variable 'connection'. In addition, sets the search path to
     * markus.
     * 
     * @param url
     *            the url for the database
     * @param username
     *            the username to be used to connect to the database
     * @param password
     *            the password to be used to connect to the database
     * @return true if connecting is successful, false otherwise
     */
    public boolean connectDB(String URL, String username, String password) {
        
        try {
        	// set up connection
        	connection = DriverManager.getConnection(URL, username, password);

        	//set the search path to markus
        	ps = connection.prepareStatement("SET SEARCH_PATH TO markus");
        	
        	ps.executeUpdate();

        	return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
    }

    /**
     * Closes the database connection.
     * 
     * @return true if the closing was successful, false otherwise
     */
    public boolean disconnectDB() {
        
        try {
        	connection.close();

        	return true;

        } catch (SQLException e) {
            e.printStackTrace();
	    return false;
        }
        
    }

    /**
     * Assigns a grader for a group for an assignment.
     * 
     * Returns false if the groupID does not exist in the AssignmentGroup table,
     * if some grader has already been assigned to the group, or if grader is
     * not either a TA or instructor.
     * 
     * @param groupID
     *            id of the group
     * @param grader
     *            username of the grader
     * @return true if the operation was successful, false otherwise
     */
    public boolean assignGrader(int groupID, String grader) {
        
        try {
        	// Prepare the appropriate database query
		    queryString = "DROP VIEW IF EXISTS ta_or_inst";
		    ps = connection.prepareStatement(queryString); 
		    ps.executeUpdate();
        	
        	// Step 1 - check if the groupID is in AssignmentGroup table
		    
		    // Prepare the appropriate database query
		    queryString = "SELECT group_id " + 
				  "FROM AssignmentGroup " + 
				  "WHERE group_id = ?";
		    ps = connection.prepareStatement(queryString); 
		    // Insert that string into the PreparedStatement and execute it.
		    ps.setInt(1, groupID);
		    rs = ps.executeQuery();
		    
		    //check if resultset is an empty table
		    if(!rs.next()){
		    	System.out.println("Invalid group ID!");
		    	return false;
		    }
		    
		    // Step 2 - check if grader is either a TA or instructor
		    
		    // Prepare the appropriate database query
		    queryString = "CREATE VIEW ta_or_inst AS " + 
		    		"(SELECT username " + 
				  	"FROM MarkusUser " + 
				  	"WHERE type = 'TA' OR type = 'instructor')";
		    ps = connection.prepareStatement(queryString); 
		    ps.executeUpdate();
		    
		    queryString = "SELECT username " + 
				  	"FROM ta_or_inst " + 
				  	"WHERE username = ?";
		    ps = connection.prepareStatement(queryString); 
		    // Insert that string into the PreparedStatement and execute it.
		    ps.setString(1, grader);
		    rs = ps.executeQuery();
		    
		    //check if resultset is an empty table
		    if(!rs.next()){
		    	System.out.println("Grader is neither TA nor instructor!");
		    	return false;
		    }
        	
		    // Step 3 - check if some grader has already been assigned to the group
		    
		    // Prepare the appropriate database query
		    queryString = "SELECT * " + 
				  "FROM Grader " + 
				  "WHERE group_id = ?";
		    ps = connection.prepareStatement(queryString); 
		    // Insert that string into the PreparedStatement and execute it.
		    ps.setInt(1, groupID);
		    rs = ps.executeQuery();
		    
		    //check if resultset is an empty table
		    if(rs.next()){
		    	System.out.println("Another grader has been assigned to the group!");
		    	return false;
		    }
		    
		    // Step 4 - assigns the grader for the group
		    
        	// Prepare the appropriate database query
        	queryString = "INSERT INTO Grader VALUES (?, ?)";
        	ps = connection.prepareStatement(queryString); 
        	// Insert that string into the PreparedStatement and execute it.
        	ps.setInt(1, groupID);
        	ps.setString(2, grader);
        	
        	ps.executeUpdate();	    
        	return true;
        }
        catch (SQLException e) {
        	System.out.println("Failed to assign grader.");
        	e.printStackTrace();
        	return false;
        }
        
        
    }

    /**
     * Adds a member to a group for an assignment.
     * 
     * Records the fact that a new member is part of a group for an assignment.
     * Does nothing (but returns true) if the member is already declared to be
     * in the group.
     * 
     * Does nothing and returns false if any of these conditions hold: - the
     * group is already at capacity, - newMember is not a valid username or is
     * not a student, - there is no assignment with this assignment ID, or - the
     * group ID has not been declared for the assignment.
     * 
     * @param assignmentID
     *            id of the assignment
     * @param groupID
     *            id of the group to receive a new member
     * @param newMember
     *            username of the new member to be added to the group
     * @return true if the operation was successful, false otherwise
     */
    public boolean recordMember(int assignmentID, int groupID, String newMember) {
        
        try {
        
		    // Step 1 - check if the newMember is a valid username and it is a student
		    
		    // Prepare the appropriate database query
		    queryString = "SELECT username " + 
				  "FROM MarkusUser " + 
				  "WHERE username = ? and type = 'student'";
		    ps = connection.prepareStatement(queryString); 
		    // Insert that string into the PreparedStatement and execute it.
		    ps.setString(1, newMember);
		    rs = ps.executeQuery();
		    
		    //check if resultset is an empty table
		    if(!rs.next()){
		    	System.out.println("Invalid username!");
		    	return false;
		    }
		    
		    // get username from query result
		    String username = rs.getString("username");
		    
		    //if (username == null) {
			//System.out.println("Invalid username.");
			//return false;
		    //}
		    	   
		   
		    // Step 2 - get group_max for this assignment 
		    // (also check if assignment ID is valid ) 
		    
		    // Prepare the appropriate database query
		    queryString = "SELECT group_max " + 
				  "FROM Assignment " + 
				  "WHERE assignment_id = ?";
		    ps = connection.prepareStatement(queryString); 
		    // Insert that string into the PreparedStatement and execute it.
		    ps.setInt(1, assignmentID);
		    rs = ps.executeQuery();
		    
		    //check if resultset is an empty table
		    if(!rs.next()){
		    	System.out.println("Invalid assignment ID!");
		    	return false;
		    }
		    
		    // get group_max from query result
		    int group_max = rs.getInt("group_max");
		    
		    
		    // Step 3 - check if the group ID has been declared for the assignment
		    	    
		    // Prepare the appropriate database query
		    queryString = "SELECT group_id " + 
				  "FROM AssignmentGroup " + 
				  "WHERE group_id = ? and assignment_id = ?";
		    ps = connection.prepareStatement(queryString); 
		    // Insert that string into the PreparedStatement and execute it.
		    ps.setInt(1, groupID);
		    ps.setInt(2, assignmentID);
		    rs = ps.executeQuery();
		    
		    //check if resultset is an empty table
		    if(!rs.next()){
		    	System.out.println("Invalid group ID!");
		    	return false;
		    }
		    
		    // get group_id from query result
		    int group_id = rs.getInt("group_id");
		    
		    //if (group_id == null) {
			//System.out.println("Invalid group ID.");
			//return false;
		    //}
		    	   	   
		    
		    // Step 4 - check if the member is already declared to be in the group
		    
		    // Prepare the appropriate database query
		    queryString = "SELECT username " + 
				  "FROM Membership " + 
				  "WHERE username = ? and group_id = ?";
		    ps = connection.prepareStatement(queryString); 
		    // Insert that string into the PreparedStatement and execute it.
		    ps.setString(1, newMember);
		    ps.setInt(2, groupID);
		    rs = ps.executeQuery();
		    
		    if(rs.next()){
		    	System.out.println("Already in this group.");
				return true;
		    }
		    // get username from query result
		    //username = rs.getString("username");
		    
		    //if (username != null) {
			//	System.out.println("Already in this group.");
			//	return true;
		    //}
		    
		    
		    // Step 5 - get number of students already in this group
		    
		    // Prepare the appropriate database query
		    queryString = "SELECT count(*) as group_size " + 
				  "FROM Membership " + 
				  "WHERE group_id = ?";
		    ps = connection.prepareStatement(queryString); 
		    // Insert that string into the PreparedStatement and execute it.
		    ps.setInt(1, groupID);
		    rs = ps.executeQuery();
		    // get group_size from query result
		    int group_size;
		    if(rs.next()){
		    	group_size = rs.getInt("group_size");
		    }else{
		    	group_size = 0;
		    }
		    
		     
		    
		    // Step 6 - check if the group is already at capacity
		    
		    if (group_size >= group_max) {
				System.out.println("This group is full!");
				return false;
		    }
		        
		    
		    // Step 7 - add the new member to the group  
		    
		    // Prepare the appropriate database query
		    queryString = "INSERT INTO Membership VALUES (?, ?)";
		    ps = connection.prepareStatement(queryString); 
		    // Insert that string into the PreparedStatement and execute it.
		    ps.setString(1, newMember);
		    ps.setInt(2, groupID);
		    
		    ps.executeUpdate();	    //check!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		    return true;
	    
        }
        catch (SQLException e) {
		    System.out.println("Failed to record member.");
		    e.printStackTrace();
		    return false;
        }
        
        
    }

    /**
     * Creates student groups for an assignment.
     * 
     * Finds all students who are defined in the Users table and puts each of
     * them into a group for the assignment. Suppose there are n. Each group
     * will be of the maximum size allowed for the assignment (call that k),
     * except for possibly one group of smaller size if n is not divisible by k.
     * Note that k may be as low as 1.
     * 
     * The choice of which students to put together is based on their grades on
     * another assignment, as recorded in table Results. Starting from the
     * highest grade on that other assignment, the top k students go into one
     * group, then the next k students go into the next, and so on. The last n %
     * k students form a smaller group.
     * 
     * In the extreme case that there are no students, does nothing and returns
     * true.
     * 
     * Students with no grade recorded for the other assignment come at the
     * bottom of the list, after students who received zero. When there is a tie
     * for grade (or non-grade) on the other assignment, takes students in order
     * by username, using alphabetical order from A to Z.
     * 
     * When a group is created, its group ID is generated automatically because
     * the group_id attribute of table AssignmentGroup is of type SERIAL. The
     * value of attribute repo is repoPrefix + "/group_" + group_id
     * 
     * Does nothing and returns false if there is no assignment with ID
     * assignmentToGroup or no assignment with ID otherAssignment, or if any
     * group has already been defined for this assignment.
     * 
     * @param assignmentToGroup
     *            the assignment ID of the assignment for which groups are to be
     *            created
     * @param otherAssignment
     *            the assignment ID of the other assignment on which the
     *            grouping is to be based
     * @param repoPrefix
     *            the prefix of the URL for the group's repository
     * @return true if successful and false otherwise
     */
    public boolean createGroups(int assignmentToGroup, int otherAssignment,
            String repoPrefix) {
        // Replace this return statement with an implementation of this method!
        return false;
    }

    public static void main(String[] args) {
        // You can put testing code in here. It will not affect our autotester.
    	
		try {
			Assignment2 a2;
			a2 = new Assignment2();
			
			boolean conn;
			conn = a2.connectDB("jdbc:postgresql://localhost:5432/csc343h-chenliy5", "chenliy5", "");
			System.out.println("connection test: " + conn);
			
			// string for the query
		    String queryString_main;
		    // prepared statement for the query
		    PreparedStatement ps_main;
		    // result set for the query
		    ResultSet rs_main;
		    
//		    // Prepare the appropriate database query
//		    queryString_main = "'\'i schema.ddl";
//		    ps_main = a2.connection.prepareStatement(queryString_main); 
//		    // Insert that string into the PreparedStatement and execute it.
//		    ps_main.executeUpdate();	
//		    
//		    // Prepare the appropriate database query
//		    queryString_main = "'\'i date.sql";
//		    ps_main = a2.connection.prepareStatement(queryString_main); 
//		    // Insert that string into the PreparedStatement and execute it.
//		    ps_main.executeUpdate();
			
		    boolean result;
		    int group_id_result;
		    String username_result;
		    //assignGrader function
		    //test 1: insert successful //remember to add a new group 2009 to the dataset
		    result = a2.assignGrader(2009, "t3");
		    System.out.println("f1: test 1: insert successful -" + result);
		    //check the table
		    queryString_main = "SELECT * " + 
					  "FROM Grader";
		    ps_main = a2.connection.prepareStatement(queryString_main); 
		    rs_main = ps_main.executeQuery();
		    System.out.println("Grader table--------------------------");
		    System.out.println("group_id" + "\t" + "username");
		    while(rs_main.next()){
		    	group_id_result = rs_main.getInt("group_id");
		    	username_result = rs_main.getString("username");
		    	System.out.println(group_id_result + "\t" + username_result);
		    }
		    
		    //test 2: Invalid group ID!
		    result = a2.assignGrader(2010, "t3");
		    System.out.println("f1: test 2: Invalid group ID! " + result);
		    //check the table
		    queryString_main = "SELECT * " + 
					  "FROM Grader";
		    ps_main = a2.connection.prepareStatement(queryString_main); 
		    rs_main = ps_main.executeQuery();
		    System.out.println("Grader table--------------------------");
		    System.out.println("group_id" + "\t" + "username");
		    while(rs_main.next()){
		    	group_id_result = rs_main.getInt("group_id");
		    	username_result = rs_main.getString("username");
		    	System.out.println(group_id_result + "\t" + username_result);
		    }
		    
		    //test 3: Grader is neither TA nor instructor!
		    result = a2.assignGrader(2001, "s1");
		    System.out.println("f1: test 3: Grader is neither TA nor instructor! -" + result);
		    //check the table
		    queryString_main = "SELECT * " + 
					  "FROM Grader";
		    ps_main = a2.connection.prepareStatement(queryString_main); 
		    rs_main = ps_main.executeQuery();
		    System.out.println("Grader table--------------------------");
		    System.out.println("group_id" + "\t" + "username");
		    while(rs_main.next()){
		    	group_id_result = rs_main.getInt("group_id");
		    	username_result = rs_main.getString("username");
		    	System.out.println(group_id_result + "\t" + username_result);
		    }
		    		    
		    //test 4: Another grader has been assigned to the group!
		    result = a2.assignGrader(2001, "t3");
		    System.out.println("f1: test 4: Another grader has been assigned to the group! -" + result);
		    //check the table
		    queryString_main = "SELECT * " + 
					  "FROM Grader";
		    ps_main = a2.connection.prepareStatement(queryString_main); 
		    rs_main = ps_main.executeQuery();
		    System.out.println("Grader table--------------------------");
		    System.out.println("group_id" + "\t" + "username");
		    while(rs_main.next()){
		    	group_id_result = rs_main.getInt("group_id");
		    	username_result = rs_main.getString("username");
		    	System.out.println(group_id_result + "\t" + username_result);
		    }
		    		    
		    
		    //recordMember function
		    //test 1: Invalid username! //remember to add a new group 2009 to the dataset
		    result = a2.recordMember(1000, 2001, "s10");
		    System.out.println("f2: test 1_1: Invalid username! -not in database -" + result);
		    //check the table
		    queryString_main = "SELECT * " + 
					  "FROM Membership";
		    ps_main = a2.connection.prepareStatement(queryString_main); 
		    rs_main = ps_main.executeQuery();
		    System.out.println("Membership table--------------------------");
		    System.out.println("group_id" + "\t" + "username");
		    while(rs_main.next()){
		    	group_id_result = rs_main.getInt("group_id");
		    	username_result = rs_main.getString("username");
		    	System.out.println(group_id_result + "\t" + username_result);
		    }
		    		    
		    result = a2.recordMember(1000, 2001, "t2");
		    System.out.println("f2: test 1_2: Invalid username! -not student -"  + result);
		    //check the table
		    queryString_main = "SELECT * " + 
					  "FROM Membership";
		    ps_main = a2.connection.prepareStatement(queryString_main); 
		    rs_main = ps_main.executeQuery();
		    System.out.println("Membership table--------------------------");
		    System.out.println("group_id" + "\t" + "username");
		    while(rs_main.next()){
		    	group_id_result = rs_main.getInt("group_id");
		    	username_result = rs_main.getString("username");
		    	System.out.println(group_id_result + "\t" + username_result);
		    }
		    		    
		    //test 2: Invalid assignment ID!
		    result = a2.recordMember(1003, 2001, "s3");
		    System.out.println("f2: test 2: Invalid assignment ID! -" + result);
		    //check the table
		    queryString_main = "SELECT * " + 
					  "FROM Membership";
		    ps_main = a2.connection.prepareStatement(queryString_main); 
		    rs_main = ps_main.executeQuery();
		    System.out.println("Membership table--------------------------");
		    System.out.println("group_id" + "\t" + "username");
		    while(rs_main.next()){
		    	group_id_result = rs_main.getInt("group_id");
		    	username_result = rs_main.getString("username");
		    	System.out.println(group_id_result + "\t" + username_result);
		    }
		    		    
		    //test 3: Invalid group ID!
		    result = a2.recordMember(1000, 2006, "s1");
		    System.out.println("f2: test 3: Grader is neither TA nor instructor! -" + result);
		    //check the table
		    queryString_main = "SELECT * " + 
					  "FROM Membership";
		    ps_main = a2.connection.prepareStatement(queryString_main); 
		    rs_main = ps_main.executeQuery();
		    System.out.println("Membership table--------------------------");
		    System.out.println("group_id" + "\t" + "username");
		    while(rs_main.next()){
		    	group_id_result = rs_main.getInt("group_id");
		    	username_result = rs_main.getString("username");
		    	System.out.println(group_id_result + "\t" + username_result);
		    }
		    		    
		    //test 4: Already in this group.
		    result = a2.recordMember(1000, 2001, "s1");
		    System.out.println("f2: test 4: Already in this group. -" + result);
		    //check the table
		    queryString_main = "SELECT * " + 
					  "FROM Membership";
		    ps_main = a2.connection.prepareStatement(queryString_main); 
		    rs_main = ps_main.executeQuery();
		    System.out.println("Membership table--------------------------");
		    System.out.println("group_id" + "\t" + "username");
		    while(rs_main.next()){
		    	group_id_result = rs_main.getInt("group_id");
		    	username_result = rs_main.getString("username");
		    	System.out.println(group_id_result + "\t" + username_result);
		    }
		    		    
		    //test 5: This group is full!
		    result = a2.recordMember(1000, 2001, "s8");
		    System.out.println("f2: test 5: This group is full! -" + result);
		    //check the table
		    queryString_main = "SELECT * " + 
					  "FROM Membership";
		    ps_main = a2.connection.prepareStatement(queryString_main); 
		    rs_main = ps_main.executeQuery();
		    System.out.println("Membership table--------------------------");
		    System.out.println("group_id" + "\t" + "username");
		    while(rs_main.next()){
		    	group_id_result = rs_main.getInt("group_id");
		    	username_result = rs_main.getString("username");
		    	System.out.println(group_id_result + "\t" + username_result);
		    }
		    		    
		    //test 6: insert successful
		    result = a2.recordMember(1001, 2005, "s7");
		    System.out.println("f2: test 6: insert successful -" + result);
		    //check the table
		    queryString_main = "SELECT * " + 
					  "FROM Membership";
		    ps_main = a2.connection.prepareStatement(queryString_main); 
		    rs_main = ps_main.executeQuery();
		    System.out.println("Membership table--------------------------");
		    System.out.println("group_id" + "\t" + "username");
		    while(rs_main.next()){
		    	group_id_result = rs_main.getInt("group_id");
		    	username_result = rs_main.getString("username");
		    	System.out.println(group_id_result + "\t" + username_result);
		    }
		    		    
		    
		    
		    
		    
			conn = a2.disconnectDB();
			System.out.println("disconnection test: " + conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
        System.out.println("Boo!");
    }
}
