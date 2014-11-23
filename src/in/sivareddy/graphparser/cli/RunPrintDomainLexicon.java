package in.sivareddy.graphparser.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import in.sivareddy.graphparser.ccg.CcgAutoLexicon;
import in.sivareddy.graphparser.parsing.CreateGroundedLexicon;
import in.sivareddy.graphparser.util.KnowledgeBase;

public class RunPrintDomainLexicon extends AbstractCli {

  private OptionSpec<String> relationLexicalIdentifiers;
  private OptionSpec<String> argumentLexicalIdentifiers;
  private OptionSpec<String> relationTypingIdentifiers;
  private OptionSpec<String> candcIndexFile;
  private OptionSpec<String> unaryRulesFile;
  private OptionSpec<String> binaryRulesFile;
  private OptionSpec<String> specialCasesFile;

  // Relations that are potential types
  private OptionSpec<String> relationTypesFile;
  // Domain file
  private OptionSpec<String> kbZipFile;
  // output lexicon
  private OptionSpec<String> inputFile;

  // output lexicon
  private OptionSpec<String> outputLexiconFile;

  @Override
  public void initializeOptions(OptionParser parser) {
    parser.acceptsAll(Arrays.asList("help", "h"), "Print this help message.");

    relationLexicalIdentifiers = parser.accepts("relationLexicalIdentifiers",
        "lexicalalise a relation using the fields specified e.g. word or lemma:pos or any combination of word, lemma and pos").withRequiredArg().ofType(String.class).required();

    argumentLexicalIdentifiers = parser.accepts("argumentLexicalIdentifiers",
        "lexicalalise the argumunets using the fields specified e.g. word or lemma:pos or any combination of word, lemma, mid, neType and pos").withRequiredArg().ofType(String.class).required();

    relationTypingIdentifiers = parser.accepts("relationTypingIdentifiers",
        "type the relation using argument fields specified e.g. neType or empty string")
        .withRequiredArg().ofType(String.class).defaultsTo("");

    candcIndexFile = parser
        .accepts("candcIndexFile", "candc markedup file e.g. data/candc_markedup.modified")
        .withRequiredArg().ofType(String.class).required();

    unaryRulesFile = parser
        .accepts("unaryRulesFile", "candc parser unary rules file e.g. data/unary_rules.txt")
        .withRequiredArg().ofType(String.class).required();

    binaryRulesFile = parser
        .accepts("binaryRulesFile", "candc binary rules file e.g. data/binary_rules.txt")
        .withRequiredArg().ofType(String.class).required();

    specialCasesFile = parser.accepts("specialCasesFile",
        "file containing candc special rules e.g. data/lexicon_specialCases.txt").withRequiredArg()
        .ofType(String.class).required();

    relationTypesFile = parser.accepts("relationTypesFile",
        "File containing relations that may be potential types e.g. data/freebase/stats/business_relation_types.txt").withRequiredArg().ofType(String.class).required();

    kbZipFile = parser.accepts("kbZipFile",
        "knowledge base of the domain e.g. data/freebase/domain_facts/business_facts.txt.gz")
        .withRequiredArg().ofType(String.class).required();

    outputLexiconFile = parser
        .accepts("outputLexiconFile", "Output file where the lexicon will be written")
        .withRequiredArg().ofType(String.class).required();

    inputFile = parser
        .accepts("inputFile", "Input file which contains ccgParses and entity annotations")
        .withRequiredArg().ofType(String.class).defaultsTo("stdin");

  }

  @Override
  public void run(OptionSet options) {

    ArrayList<String> lexicalFieldsList = Lists.newArrayList(Splitter.on(":").trimResults()
        .omitEmptyStrings().split(options.valueOf(relationLexicalIdentifiers)));

    String[] lexicalFields = lexicalFieldsList.toArray(new String[lexicalFieldsList.size()]);

    ArrayList<String> argIdentifierFieldsList = Lists.newArrayList(Splitter.on(":").trimResults()
        .omitEmptyStrings().split(options.valueOf(argumentLexicalIdentifiers)));

    String[] argIdentifierFields =
        argIdentifierFieldsList.toArray(new String[argIdentifierFieldsList.size()]);

    ArrayList<String> relationTypingFeildsList = Lists.newArrayList(Splitter.on(":").trimResults()
        .omitEmptyStrings().split(options.valueOf(relationTypingIdentifiers)));

    String[] relationTypingFeilds =
        relationTypingFeildsList.toArray(new String[relationTypingFeildsList.size()]);


    try {
      String relationTypesFileName = options.valueOf(relationTypesFile);
      KnowledgeBase kb = new KnowledgeBase(options.valueOf(kbZipFile), relationTypesFileName);

      CcgAutoLexicon ccgAutoLexicon = new CcgAutoLexicon(options.valueOf(candcIndexFile),
          options.valueOf(unaryRulesFile), options.valueOf(binaryRulesFile),
          options.valueOf(specialCasesFile));

      boolean ignorePronouns = true;
      CreateGroundedLexicon creator = new CreateGroundedLexicon(kb,
          ccgAutoLexicon,
          lexicalFields,
          argIdentifierFields,
          relationTypingFeilds,
          ignorePronouns);

      String input = options.valueOf(inputFile);
      BufferedReader br;

      if (input.equals("stdin")) {
        br = new BufferedReader(new InputStreamReader(System.in));
      } else {
        br = new BufferedReader(new FileReader(input));
      }

      Gson gson = new Gson();
      JsonParser parser = new JsonParser();
      Set<Integer> sentenceCache = Sets.newHashSet();
      Integer sentCount = 0;

      try {
        sentCount += 1;
        if (sentenceCache.size() == 50000) {
          sentenceCache = Sets.newHashSet();
        }

        String line = br.readLine();
        while (line != null) {
          if (line.equals("") || line.charAt(0) == '#') {
            line = br.readLine();
            continue;
          }

          JsonObject jsonSentence = parser.parse(line).getAsJsonObject();
          List<Set<String>> semanticParses =
              creator.lexicaliseArgumentsToDomainEntities(jsonSentence, 1);
          if (semanticParses.size() == 0) {
            line = br.readLine();
            continue;
          }
          Integer sentenceHash = jsonSentence.get("sentence").getAsString().hashCode();
          if (sentenceCache.contains(sentenceHash)) {
            line = br.readLine();
            continue;
          }
          sentenceCache.add(sentenceHash);

          boolean isUsefulSentence = false;
          for (Set<String> semanticParse : semanticParses) {
            boolean isUsefulParse =
                creator.updateLexicon(semanticParse, jsonSentence, 1.0 / semanticParses.size());
            isUsefulSentence = isUsefulSentence || isUsefulParse;
          }
          if (isUsefulSentence) {
            System.out.println(gson.toJson(jsonSentence));
          }
          line = br.readLine();
        }
      } finally {
        br.close();
      }

      BufferedWriter bw = new BufferedWriter(new FileWriter(options.valueOf(outputLexiconFile)));
      creator.printLexicon(bw);
      bw.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    new RunPrintDomainLexicon().run(args);
  }

}
