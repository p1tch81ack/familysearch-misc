package org.familysearch.joe.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class SimpleClient {
    private Cluster cluster;
    private Session session;

    public SimpleClient(String contactPoint){
        cluster = Cluster.builder().addContactPoint(contactPoint).build();
        session = cluster.connect();
    }

    public void close(){
        session.close();
        cluster.close();
    }

    public void execute(String statement){
        session.execute(statement);
    }

    public PreparedStatement prepare(String statement){
        return session.prepare(statement);
    }

    public void execute(PreparedStatement statement, Object ... args){
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(args));
    }
}
