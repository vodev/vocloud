Spark worker
============

Requirements
------------

- JDK 7+
- Java application server supporting Java servlet technology (tomcat, wildfly, ...)
- Maven tool (if building is necessary)
- Spark deployable application for each Spark worker type

Install guide
-------------
For instance I will use Debian amd64 with Wildfly 8.2 application server, JDK 8 and Maven 3.1


1. Install JDK 8

   - Download JDK from
        http://www.oracle.com/technetwork/java/javase/downloads/index.html
        in zip file form, for example jdk-8u45-linux-x64.tar.gz
   - Extract archive to /usr/lib/jvm
   - Setup enviroment variables for java 
   - add these lines to the end of /etc/profile
        ```
        export JAVA_HOME=/usr/lib/jvm/jdk1.8.45  
        export PATH=$JAVA_HOME/bin
        ```

2. Install Wildfly 8.2.0

    - Download zip from http://wildfly.org/downloads/
    - Extract archive to the /usr/local
    - In the newly extracted wildfly directory execute bin/add-user.sh and setup new wildfly administering user.
   
3. Start Wildfly by executing bin/standalone.sh

    - Server should successfully start.
    - If everything went OK:
    - Server is running on http://localhost:8080/
    - Admin console on http://localhost:9990/

4. Configure spark-worker configuration file (optional step if you want another configuration that it is in prebuilt archive) 

    - Download sources for spark-worker
    - Go to src/main/resources/
    - Adjust uws-config.xml file
    - Go back to sources root
    - Execute command "mvn package"
    - Worker is compiled and the deployable archive is created in target/spark-worker.war
    
5. Deploy spark worker to Wildfly

    - Open Wildfly admin console on http://localhost:9990/
    - Login with the credentials of administrating user
    - Navigate to Deployments section
    - Click Add
    - Select deployable spark-worker.war archive
    - Click OK
    - Enable the newly deployed application

    UWS service should now run on
    http://localhost:8080/spark-worker/uws
    

Note: This is only description of spark-worker application which serves as the mediator between the master server and spark submit script. 
In order to make a worker fully functional you have to set proper configuration values into the
uws configuration file matching your running Spark instance. 

Configuration file description
------------------------------

Configuration of the Spark worker is define by the xml file containing all necessary information
for the Spark worker deployment. The schema of the xml configuration file is specified by `xsd` file and is 
located in `src/main/resources/configSchema.xsd`.

Let us explain the configuration file format on the example:

```xml
<?xml version="1.0" encoding="utf-8"?>
<ns:uws-settings
        xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
        xmlns:ns='http://vocloud.ivoa.cz/spark/schema'
        xsi:schemaLocation='http://vocloud.ivoa.cz/spark/schema configSchema.xsd'>
    <ns:vocloud-server-address>http://localhost:8080/vocloud-betelgeuse</ns:vocloud-server-address>
    <ns:local-address>http://localhost:8080</ns:local-address>
    <ns:spark-executable>/opt/spark/bin/spark-submit</ns:spark-executable>
    <ns:hadoop-default-fs>hdfs://betelgeuse:9000</ns:haddop-default-fs>
    <ns:max-jobs>4</ns:max-jobs>
    <ns:description>Spark UWS worker</ns:description>
    <ns:submit-params>
        <conf name="spark.driver.maxResultSize">12g</conf>
        <conf name="spark.yarn.executor.memoryOverhead">4096</conf>
        <master>yarn</master>
        <driver-memory>4g</driver-memory>
        <deploy-mode>client</deploy-mode>
        <num-executors>5</num-executors>
        <executor-cores>3</executor-cores>
        <executor-memory>4g</executor-memory>
    </ns:submit-params>
    <ns:workers>
        <ns:worker>
            <ns:identifier>spark-preprocessing</ns:identifier>
            <ns:description>Spark preprocessing</ns:description>
            <ns:submit-params>
                <packages>com.databricks:spark-avro_2.10:2.0.1</packages>
                <py-files>
                    /home/hadoop/workflow-test/preprocessing/vocloud_spark_import/dist/vocloud_spark_preprocess-0.1.0-py2.7.egg
                </py-files>
            </ns:submit-params>
            <ns:submit-target>
                /home/hadoop/workflow-test/preprocessing/vocloud_spark_import/bin/vocloud_preprocess.py
            </ns:submit-target>
        </ns:worker>
    </ns:workers>
</ns:uws-settings>
```

- `vocloud-server-address` [optional] - Specifies URL address to the deployed vocloud server. 
  This URL is necessary when the worker needs to download some data from the vocloud server. Note that in order to do so
  you will have to arrange the network visibility from the worker to master server and vice versa.
- `local-address` - Hostname URL to the worker server from the master server point of network view.
- `max-jobs` - Maximum count of jobs that this worker allows to be run concurrently. Note that Spark execution manager
(e.g. YARN) can have additional restrictions to the count of jobs/resources requirement.
- `description` - Description of this UWS worker.
- `spark-executable` - Path to the `spark-submit` script on the filesystem.
- `hadoop-default-fs` - URL locator of the HDFS filesystem.
- `submit-params` [optional] - This complex tag can be either in the root `uws-settings` tag or in the `worker` tag (see later).
It specifies implicit parameters to be passed to the `spark-submit`. Parameters from the root tag can be overriden
by the parameters specified in the `worker` tag and both parameter specification can be overridden by the parameters
specified in the job's configuration file. Parameters are specified in the following format:
`<param-name>param-value</param-name>`. This statement is translated to `--param-name param-value` in the `spark-submit`
script. Note: `<conf>` tag have special form: `<conf name="conf-name">conf-value</conf>` that is translated to
`--conf conf-name=conf-value`. There can be multiple `<conf>` tags.
- `workers` - Contains sequence of `<worker>` tags.
- `worker` - Contains configuration for the single worker type instance. It contains following tags:

    - `identifier` - Identification if the worker. Must not contain space character.
    - `description` - Description of the worker.
    - `submit-params` - Same as in the root tag.
    - `submit-target` - Path to the file that should be passed to the `spark-submit` script. 
    
Job configuration
-----------------

The following JSON is an example of the spark job configuration.

```json
{
    "download_files": [
        {
            "urls": [
                "vocloud://DATA/allspec-ond700-prep/prep.csv",
                "vocloud://DATA/allspec-ond700-prep/prep2.csv"
            ],
            "folder": "/user/test/input1/"        
        },{
            "urls": ["vocloud://DATA/folder/st.csv"],
            "folder": "/user/test/input2/"
        }
    ],
    "spark_params": {
        "num-executors": "2",
        "executor-cores": "4",
        "conf": {
            "spark.driver.maxResultSize": "12g",
            "spark.yarn.executor.memoryOverhead": "4096"
        }
    },
    "job_config": {
        "dataset": "hdfs:///user/workflow-test/lof-input/preprocessed.csv",
        "min_pts": 15,
        "output": "hdfs:///user/workflow-test/output/lof_kepler-out.csv"
    },
    "copy_output": [
        {
            "path": "/user/workflow-test/output/lof_kepler-out.csv",
            "merge_parts": true
        }
    ]
}
```

Most of the configuration JSON file is optional. The only mandatory part is `job_config` object
part that specifies the configuration file of the Spark application. The content of this object
will be written in the temporary file and the path will be passed to the `spark-submit` script 
as the last parameter. If the configuration does not contain the `copy_output` item, the whole
configuration file is considered as the config for the `spark-submit` script - in this case it would be:

```json
{
    "dataset": "hdfs:///user/workflow-test/lof-input/preprocessed.csv",
    "min_pts": 15,
    "output": "hdfs:///user/workflow-test/output/lof_kepler-out.csv"
}
```

-  `download_files` - Specifies files that should be downloaded from the vocloud filesystem (or some other URL) and saved to the
hdfs to the specified path before the spark job itself is executed. It must contain array where each item is object containing two
mandatory items:
    - `urls` - Array of string containing the remote file path. It supports `http`/`https` protocol and 
    if the path has scheme `vocloud` the files are downloaded from the vocloud's filesystem. Note: in order
    to do so it is necessary that worker has properly set the path to the vocloud server and the server
    is directly visible on the network.
    - `folder` - Target path on hdfs where the files specified in the `urls` part should be saved.
    Save fails if the path already exists.
- `spark_params` - Allows user to override parameters passed to the `spark-submit` script. It contains
JSON object where each item `"name": "value"` is translated to the parameter `--name value`. The only exception
is an item named `conf` that if present must contain additional JSON object where each item `"name": "value"` is
translated to `--conf name=value`. Parameters here can override the default one specified in the xml
 configuration file.
- `job_config` - Specifies the configuration for the Spark job itself. See above.
- `copy_output` - Allows user to obtain files from the hdfs back to the vocloud. It must contain
JSON array containing JSON objects containing following items:
    - `path` - Path to the file or folder on the HDFS.
    - `merge_parts` [optional] - Spark jobs usually produce results as folder containing `part_xxx` files.
     If this item is set to `true` the worker merges these parts together to produce a single file.
      This item is optional, default value is set to `false`.
