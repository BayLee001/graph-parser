package in.sivareddy.graphparser.util;

import in.sivareddy.graphparser.util.knowledgebase.KnowledgeBaseOnline;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

public class KnowledgeBaseOnlineTest {

  @Test
  public void test() throws IOException {
    Logger
        .getLogger(org.apache.jena.riot.system.stream.JenaIOEnvironment.class)
        .setLevel(Level.OFF);
    KnowledgeBaseOnline.TYPE_KEY = "fb:type.object.type";
    
    Schema schema =
        new Schema("data/freebase/schema/all_domains_schema.txt");
    
    KnowledgeBaseOnline kb =
        new KnowledgeBaseOnline("rockall", "http://rockall:8890/sparql", "dba",
            "dba", 100000, schema);

    System.out.println(kb.getRelations("m.01ypc"));
    
    System.out.println(kb.getRelations("m.07484"));
    
    // true
    System.out.println(kb.hasRelation("m.06rn8p", "m.031q99"));

    // ["m.06rn8p","m.031q99"]
    // [["organization.organization_board_membership.member","organization.organization_board_membership.organization"],["organization.leadership.person","organization.leadership.organization"]]
    System.out.println(kb.getRelations("m.017nt", "m.04sv4"));
    System.out.println(kb.getRelations("m.04sv4", "type.datetime"));
    System.out.println(kb.getRelations("m.06rn8p", "m.031q99"));

    // []
    System.out.println(kb.getRelations("m.06rn8", "m.031q99"));

    System.out.println(kb.getRelations("m.017nt"));
    System.out.println(kb.getRelations("m.04sv4"));

    // "m.06rn8p" ["business.board_member"]
    System.out.println(kb.getTypes("m.06rn8p"));

    // m.017nt
    System.out.println(kb.getTypes("m.017nt"));

    System.out.println(kb.getRelations("m.06c0j"));
  }
}
