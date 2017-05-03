VO-CLOUD Master server
======================

Requirements
------------

- JDK 7+
- Application server supporting Java EE 7 with EJB container support (Wildfly, Glassfish, ...)
- Database (PostgreSQL, MySQL, ...)
- Maven tool for project building

Install guide
-------------
For instance I will use Debian amd64 with Wildfly 8.2 application server, JDK 8 and PostgreSQL 8.4

1. Install JDK 8

   - Download JDK from
        `http://www.oracle.com/technetwork/java/javase/downloads/index.html`
        in zip file form, for example `jdk-8u45-linux-x64.tar.gz`
   - Extract archive to `/usr/lib/jvm`
   - Setup enviroment variables for Java -  
   add these lines to the end of `/etc/profile`
        ```
        export JAVA_HOME=/usr/lib/jvm/jdk1.8.45        
        export PATH=$JAVA_HOME/bin
        ```
        
2. Install WildFly 8.2.0

   - Download zip from `http://wildfly.org/downloads/`
   - Extract archive to the `/usr/local`
   - In the newly extracted WildFly directory execute `bin/add-user.sh` and setup a new WildFly administering user
   
3. Start Wildfly by executing `bin/standalone.sh`. Server should successfully start. If everything went OK:

   - Server is running on `http://localhost:8080/`
   - Admin console on `http://localhost:9990/`

4. Install and configure PostgreSQL database server

   - Install PostgreSQL using `apt-get install postgresql`
   - Log in as a postgres user `su - postgres` and start client command line `psql template1` 
   - Type in following commands to setup database for vocloud:
       ```
       CREATE USER vocloud WITH PASSWORD 'vocloud';
       CREATE DATABASE vocloud;
       GRANT ALL PRIVILEGES ON DATABASE vocloud TO vocloud;
       ```
   Note: You should really not use the same password as username. Do not forget to change it!
    
   It is also possible to use Docker to install PostgreSQL inside a Docker container. To do this, 
   execute the following command:
   
   ```
    docker run --name db -d -p 5432:5432
               -e POSTGRES_USER=<username> 
               -e POSTGRES_PASSWORD=<password>
               postgres
    ```

5. Configure database resource in WildFly:
	
   - Log into WildFly admin console at `http://localhost:9990/`
   - Type in credentials of administrating user   
   - Download JDBC for PostgreSQL at `https://jdbc.postgresql.org/`
   - In the admin console navigate to Deployments section
   - Click Add
   - Select downloaded JDBC `.jar` file and click OK
   - Enable newly uploaded JDBC driver
   - Navigate to a Configuration tab
   - Select Datasources
   - Click Add and insert following values:
        - `Name`: `VocloudDS`
        - `JNDI Name`: `java:jboss/datasources/vocloud`
   - Click Next
   - Select postgresql jdbc driver
   - Click Next
   - Insert following values:
        - `Connection URL`: `jdbc:postgresql://localhost:5432/vocloud`
        - `Username`: `vocloud`
        - `Password`: `vocloud`
   - Click Done
   - Enable newly created VocloudDS
   
   Datasource can be tested in section Connection > Test connection - ping should be successful.

6. Configure e-mail resource in WildFly
    
   It is necessary to have an email address that serves as the source of emails sent by vocloud. For instance, I will use address `vocloud@vocloud.org` where SMTP is running on port 465 and the host address of the SMTP server is `smtp.vocloud.org`.
    
   - Navigate to Configuration section
   - Select Socket Binding
   - Click View on standard-sockets
   - Select Outbound Remote section
   - Click Add and insert:
        - `Name`: `vocloud-smtp`
        - `Host`: `smtp.vocloud.org`
        - `Port`: `465`
   - Click Save
   - Navigate to Mail subsystem section
   - Click Add and insert:
        - `JNDI Name`: `java:jboss/mail/vocloud-mail`
   - Click View on the newly created mail session
   - Click Add and insert:
        - `Socket binding`: `vocloud-smtp`
        - `Type`: `smtp`
        - `Username`: `username to the email server`
        - `Password`: `password to the email server`
   - Check use SSL (if the port is 465)
   - Click Save

7. Configure security in WildFly

    - Navigate to Security Domains in Configuration section
    - Click Add and insert:
        - `Name`: `VocloudSecurityDomain`
    - Click Save
    - Click View on the newly created security domain
    - Click Add and insert:
        - `Code`: `Database`
        - `Flag`: `required`
    - Click Save
    - Now click on the newly created Login module
    - Click on Module Options
    - Add the following key=value pairs:
        - `dsJndiName` = `java:jboss/datasources/vocloud`
        - `principalsQuery` = `select pass from useraccount where username=?`
        - `rolesQuery` = `select groupName, 'Roles' from useraccount where username=?`
        - `hashAlgorithm` = `SHA-256`
        - `hashEncoding` = `hex`

8. Create master server's `vocloud.war` package
    - Navigate to the VO-CLOUD's master server application's directory
    - Execute `mvn package`
    - Package should be now created in `target/vocloud.war` 

9. Deploy `vocloud.war` package to the WildFly server

    - Log into the WildFly's admin console
    - Navigate to section Deployments
    - Click Add
    - Select `vocloud.war` file
    - Submit
    - Enable the newly deployed application
    
    VO-CLOUD master server should now be running on `http://localhost:8080/vocloud`
   
10. Create admin account

    - Open VO-CLOUD master server application in web browser 
    - Click Register
    - Register a new account with username `admin`
    
    This account now have administrator privileges.
