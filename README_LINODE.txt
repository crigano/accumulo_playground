
########
# Create the accumulo user
########

$ su - root
$ addgroup accumulo
$ adduser --ingroup accumulo accumulo
$ su - accumulo
$ ssh-keygen -t rsa -P ""
$ cat $HOME/.ssh/id_rsa.pub >> $HOME/.ssh/authorized_keys
$ chmod 600 ~/.ssh/authorized_keys
$ ssh localhost

##########
# Update BASH Configuration (.bashrc)
##########

export DEFAULT_PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games
export ACCUMULO_HOME=/usr/local/accumulo
export HADOOP_HOME=/usr/local/hadoop
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-i386
export ZOOKEEPER_HOME=/usr/local/zookeeper
export PATH=$ACCUMULO_HOME/bin:$HADOOP_HOME/bin:$JAVA_HOME/bin:$ZOOKEEPER_HOME/bin:$DEFAULT_PATH

$ exit

##########
# Update the installed software
##########
$ sudo apt-get update
$ sudo apt-get upgrade

##########
# For verification, you can display the OS release.
##########
$ cat /etc/lsb-release
$ sudo apt-get update
DISTRIB_ID=Ubuntu
DISTRIB_RELEASE=12.04
DISTRIB_CODENAME=precise
DISTRIB_DESCRIPTION="Ubuntu 12.04 LTS"

##########
# Download all of the packages you'll need. Hopefully,
# you have a fast download connection.
##########

$ sudo apt-get install curl git maven2 openssh-server openssh-client openjdk-7-jdk

##########
# Switch to the new Java. On my system, it was
# the third option (marked '2' naturally)
##########

$ sudo update-alternatives --config java

##########
# Set the JAVA_HOME variable. I took the
# time to update my .bashrc script.
##########

$ export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-i386

##########
# I stored the Accumulo source code into
# ~/workspace/accumulo. After compilation, you'll
# be working with a second Accumulo directory. By
# placing this 'original source' version into
# workspace it is nicely segregated.
##########

$ mkdir -p ~/workspace
$ cd ~/workspace
$ git clone https://github.com/apache/accumulo.git
$ cd accumulo

##########
# Now we can compile Accumulo which creates the
# accumulo-assemble-1.5.0-incubating-SNAPSHOT-dist.tar.gz
# file in the src/assemble/target directory.
##########

$ mvn -Dmaven.test.skip=true package -P assemble

##########
# Install Apache Hadoop 
#
# Check out the following for more details:
#  http://www.michael-noll.com/tutorials/running-hadoop-on-ubuntu-linux-single-node-cluster
##########
$ su - root
$ addgroup hadoop
$ adduser --ingroup hadoop hadoop
$ su - hadoop
$ ssh-keygen -t rsa -P ""
$ cat $HOME/.ssh/id_rsa.pub >> $HOME/.ssh/authorized_keys
$ ssh localhost
$ exit

###### 
#
# Back as root user
#  disable ipv6
#  

$ cd /usr/local 
$ wget http://apache.mirrors.tds.net//hadoop/common/hadoop-0.20.2/hadoop-0.20.2.tar.gz
$ tar xvfz hadoop-0.20.2.tar.gz
$ rm hadoop-0.20.2.tar.gz
$ chown -R hadoop:hadoop hadoop
$ ln -s hadoop-0.20.2 hadoop

##########
# Now we can configure Pseudo-Distributed hadoop
# These steps were borrowed from 
# http://hadoop.apache.org/common/docs/r0.20.2/quickstart.html
##########

# Set some environment variables. I added these to my 
# .bashrc file.

$ export DEFAULT_PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games
$ export HADOOP_HOME=/usr/local/hadoop
$ export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-i386
$ export PATH=$HADOOP_HOME/bin:$JAVA_HOME/bin:$DEFAULT_PATH

# Create the hadoop temp directory. It should not 
# be in the /tmp directory because that directory
# disappears after each system restart. Something
# that is done a lot with virtual machines.
sudo mkdir /hadoop_tmp_dir
sudo chmod 777 /hadoop_tmp_dir
sudo chown hadoop:hadoop /hadoop_tmp_dir

$ cd $HADOOP_HOME/conf

# Replace the existing file with the indented lines.
$ vi core-site.xml
    <?xml version="1.0"?>
    <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
    <configuration>
      <property>
        <name>hadoop.tmp.dir</name>
        <value>/hadoop_tmp_dir</value>
      </property>
      <property>
        <name>fs.default.name</name>
        <value>hdfs://localhost:9000</value>
      </property>
    </configuration>

##########
# Notice that the dfs secondary http address is not
# the default in the XML below. I don't know what
# process was using the default, but I needed to
# change it to avoid the 'port already in use' message.
##########

# Replace the existing file with the indented lines.
$ vi hdfs-site.xml
    <?xml version="1.0"?>
    <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
    <configuration>
        <property>
          <name>dfs.secondary.http.address</name>
          <value>0.0.0.0:8002</value>
        </property>
        <property>
          <name>dfs.replication</name>
          <value>1</value>
        </property>
    </configuration>

# Replace the existing file with the indented lines.
$ vi mapred-site.xml
    <?xml version="1.0"?>
    <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
    <configuration>
      <property>
        <name>mapred.job.tracker</name>
        <value>localhost:9001</value>
      </property>
    </configuration>

# Set the JAVA_HOME variable in hadoop-env.sh

# Format the hadoop filesystem
$ cd $HADOOP_HOME
$ bin/hadoop namenode -format

##########
# Time to setup password-less ssh to localhost
##########
$ cd ~
$ ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
$ cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys

# If you want to test that the ssh works, do this. Then exit.
$ ssh localhost

# Start hadoop. I remove the logs so that I can find errors
# faster when I iterate through configuration settings.

$ cd $HADOOP_HOME
$ rm -rf logs/*
$ bin/start-dfs.sh
$ bin/start-mapred.sh

# If desired, look at the hadoop jobs. Your output should look something
# like the intended lines.
$ jps
  4017 JobTracker
  4254 TaskTracker
  30279 Main
  9808 Jps
  3517 NameNode
  3737 DataNode

#########
# Create the /accumulo hdfs directory
#########

$ sudo su - hadoop
$ hadoop fs -mkdir /user/accumulo
$ hadoop fs -chown accumulo /user/accumulo

##############
# Get your IP address from the ifconfig command.

##########
# This is an optional step to prove that the NameNode is running.
# Use a web browser like Firefix if you can.
##########
$ wget http://[IP ADDRESS]:50070/
$ cat index.html
$ rm index.html

##########
# This is an optional step to prove that the JobTracker is running.
# Use a web browser like Firefix if you can.
##########
$ wget http://[IP ADDRESS]:50030/
$ cat index.html
$ rm index.html

##########
# Configure Zookeeper (see README_ZOOKEEPER.txt)
##########

$ sudu su -
$ cd /usr/local
$ export TAR_DIR=/home/medined/workspace/accumulo/assemble/target
$ rm -rf accumulo-1.5.0-SNAPSHOT
$ tar xvzf $TAR_DIR/accumulo-1.5.0-SNAPSHOT-dist.tar.gz
$ chown -R accumulo:accumulo /usr/local/accumulo-1.5.0-SNAPSHOT
# Make the lib/ext directory group writeable so that you can deply jar
# files there.
$ chmod g+w /usr/local/accumulo-1.5.0-SNAPSHOT/lib/ext
$ ln -s /usr/local/accumulo-1.5.0-SNAPSHOT accumulo

$ sudo su accumulo
$ cd $ACCUMULO_HOME
$ cp conf/examples/512MB/standalone/* conf

# Change (or add) the trace.password entry if the root password is
# not the default of "secret"
$ vi conf/accumulo-site.xml
  <property>
    <name>trace.password</name>
    <value>mypassword_for_root_user</value>
  </property>

# Also change the location where Accumulo stores its files int HDFS
# The default is /accumulo which makes it hard, at least for me,
# to recover. If something goes wrong, just delete the accumulo
# subdirectory and reinitialize.
   <property>
     <name>instance.dfs.dir</name>
     <value>/user/accumulo/accumulo</value>
   </property>


# create the write-ahead directory.
$ cd $ACCUMULO_HOME
$ mkdir walogs
$ bin/accumulo init
# Provide an instance (development) name and password (password) when asked.

###########
# And now, the payoff. Let's get Accumulo to run.
###########


# I remove the logs to make debugging easier.
rm -rf logs/*
bin/start-all.sh

##########
# This is an optional step to prove that Accumulo is running.
# Use a web browser like Firefox if you can.
##########
$ wget http://[IP ADDR]:50095/
$ cat index.html
$ rm index.html

# Check the logs directory.
$ cd logs

# Look for content in .err or .out files. The file sizes should all be zero.
$ ls -l *.err *.out

# Look for error messages. Ignore messages about the missing libNativeMap file.
$ grep ERROR *

# Start the Accumulo shell. If this works, see the README file for an example 
# how to use the shell.
$ bin/accumulo shell -u root -p password

###########
# Do a little victory dance. You're now an Accumulo user!
###########
