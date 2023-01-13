package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";

        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeString(json, "data.json");

        List<Employee> list2 = parseXML("data.xml");
        String json2 = listToJson(list2);
        writeString(json2, "data2.json");


        String json3 = readString("new_data.json");


        List<Employee> list3 = jsonToList(json3);
        for (Employee employee: list3) {
            System.out.println(employee);
        }
    }

    private static List<Employee> jsonToList(String json) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        JsonArray jsonElements = new JsonArray();
        JsonParser parser = new JsonParser();
        List<Employee> employees = new ArrayList<>();
        Object obj = parser.parse(json);
        jsonElements = (JsonArray) obj;
        jsonElements.forEach(jsonElement -> employees.add(gson.fromJson(jsonElement, Employee.class)));


        return employees;
    }

    private static String readString(String s) {
        JsonParser parser = new JsonParser();
        JsonArray jsonArray;
        try {
            Object obj = parser.parse(new FileReader(s));
            jsonArray = (JsonArray) obj;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return jsonArray.toString();
    }

    private static List<Employee> parseXML(String s) {
        List<Employee> list = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(s));

            Node root = doc.getDocumentElement();
            NodeList employeesList = root.getChildNodes();
            for (int i = 0; i < employeesList.getLength(); i++) {
                Node node = employeesList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE){
                    NodeList employeeAttributes = node.getChildNodes();
                    Employee employee = new Employee();
                    for (int j = 0; j < employeeAttributes.getLength(); j++) {
                        Node elNode = employeeAttributes.item(j);
                        if(elNode.getNodeType() == Node.ELEMENT_NODE){
                            switch (elNode.getNodeName()){
                                case "id" : employee.id = Long.parseLong(getDataFromNode(elNode));break;
                                case "firstName" : employee.firstName = getDataFromNode(elNode);break;
                                case "lastName" : employee.lastName = getDataFromNode(elNode);break;
                                case "country" : employee.country = getDataFromNode(elNode); break;
                                case "age" : employee.age = Integer.parseInt(getDataFromNode(elNode)); break;
                            }
                        }
                    }
                    list.add(employee);
                }
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    private static String getDataFromNode(Node node){
        return node.getChildNodes().item(0).getNodeValue();
    }

    private static void writeString(String s, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)){

            fileWriter.write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Type listType = new TypeToken<List<Employee>>() {}.getType();
        return gson.toJson(list, listType);
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> staff;
        try(CSVReader reader = new CSVReader(new FileReader(fileName))){
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader).withMappingStrategy(strategy).build();

            staff = csv.parse();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return staff;
    }
}