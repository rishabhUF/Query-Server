package rishabh;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class App {
    static final int ALPHABET_SIZE = 26;

    public static final Logger logger = Logger.getLogger(App.class.getName());

    static class TrieNode {
        TrieNode[] children = new TrieNode[ALPHABET_SIZE];
        // isEndOfWord is true if the node represent
        // keeping the top 10 words.
        TreeMap<Integer, String> top10;

        TrieNode() {
            top10 = new TreeMap<>();
            for (int i = 0; i < ALPHABET_SIZE; i++)
                children[i] = null;
        }
    }
    static TrieNode root;

    public  void insert(String str, int value) {
        int level;
        int length = str.length();
        int index;
        TrieNode crawlNode = root;
        for (level = 0; level < length; level++) {
            if (str.charAt(level) == '_') {
                crawlNode = root;
            } else {
                index = str.charAt(level) - 'a';
                if (crawlNode.children[index] == null) {
                    crawlNode.children[index] = new TrieNode();
                }
                // index should be added to the trie irrespective of it should be added to the top10 or not.
                crawlNode = crawlNode.children[index];
                if (crawlNode.top10.size() < 10) {
                    crawlNode.top10.put(value, str);
                } else {
                    if (value > crawlNode.top10.firstKey()) {
                        if (crawlNode.top10.size() == 10) {
                            crawlNode.top10.remove(crawlNode.top10.firstKey());
                        }
                        crawlNode.top10.put(value, str);
                    }
                }
            }
        }
        logger.log(Level.INFO, str+" is added to the trie");
    }

    public  List<String> search(String str) {
        int level;
        int index;
        int length = str.length();
        TrieNode crawlNode = root;
        for (level = 0; level < length; level++) {
            index = str.charAt(level) - 'a';
            if (crawlNode.children[index] == null)
                return new LinkedList<>();
            crawlNode = crawlNode.children[index];
        }
        List<String> result = new LinkedList<>(crawlNode.top10.values());

        return result;
    }

    // Serialize CSV file into JSON file.
    public  void makeJson() {
        Pattern pattern = Pattern.compile(",");
        try (BufferedReader in = new BufferedReader(new FileReader("test.txt"));) {
            List<Pair> pairs = in.lines().map((String line) -> {
                String[] x = pattern.split(line);
                return new Pair(x[0], Integer.parseInt(x[1]));
            }).collect(Collectors.toList());
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File("test.json"), pairs);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "FILE NOT FOUND TO ADD TO JSON.");
            e.printStackTrace();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "ERROR while making json file");
            e.printStackTrace();
        }
    }

    // Deserialize Json File and prepossessing for sub linear searching.
    public  void readJson(){
        JSONParser parser = new JSONParser();
        try {
            Object object = parser.parse(new FileReader("test.json"));
            //convert Object to JSONObject
            JSONArray jsonObject = (JSONArray) object;
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<List<JSONObject>> mapType = new TypeReference<List<JSONObject>>() {
            };
            List<JSONObject> jsonToPersonList = objectMapper.readValue(jsonObject.toString(), mapType);
            for (int i = 0; i < jsonToPersonList.size(); i++)
            {
                insert(jsonToPersonList.get(i).get("name").toString(), Integer.parseInt(jsonToPersonList.get(i).get("score").toString()));
            }
            logger.log(Level.INFO,"JSON file read completely");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "JSON FILE NOT READ COMPLETELY");
            ex.printStackTrace();
        }
    }
    public static void main (String[]args) throws IOException
    {
        FileHandler fp;
        fp = new FileHandler("test.log");
        logger.addHandler(fp);
        SimpleFormatter formatter = new SimpleFormatter();
        fp.setFormatter(formatter);
        App application = new App();
        root = new TrieNode();
        application.makeJson();
        application.readJson();
        List<String> result = new LinkedList<>();
        result = application.search("a");
        System.out.println(result);
        }
    }
