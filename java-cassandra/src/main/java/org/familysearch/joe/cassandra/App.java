package org.familysearch.joe.cassandra;

import com.datastax.driver.core.PreparedStatement;

public class App {
  public static void main( String[] args ) {
    SimpleClient simpleClient = new SimpleClient("127.0.0.1");
//    simpleClient.execute("CREATE KEYSPACE addressbook1 WITH replication = {'class':'SimpleStrategy','replication_factor':1};");
//    simpleClient.execute("CREATE TABLE addressbook1.user1 (name text PRIMARY KEY, age int);");
//      simpleClient.execute("INSERT INTO addressbook1.user1 (name, age) VALUES ('Bob Black', 42);");
    PreparedStatement insertIntoAddressBook = simpleClient.prepare("INSERT INTO addressbook1.user1 (name, age) VALUES (?, ?);");
    simpleClient.execute(insertIntoAddressBook, "Oona Orange", 26);
    simpleClient.close();
  }
}
