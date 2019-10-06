/*
 * Copyright (c) 2019 Financial Engines, Inc.  All Rights Reserved.
 * Sunnyvale, CA
 *
 * File: ParserUtils.java
 * Author: nishah
 */

package utilities;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;

public class ParserUtils {

  //private static final Logger logger = LoggerFactory.getLogger(ParserUtils.class);

  public static List<Map<String, String>> getJsonStringMap(S3Object s3Object) {
    List<Map<String, String>> jsonStringMaps = new ArrayList<>();
    Pattern pattern = Pattern.compile(",");
    JsonFactory fac = new JsonFactory();
    int totalCount = 0;
    int count = 0;
    int batchSize = 500;
    //int batchSize = 3;

    Map<String, String> jsonStringMap = new HashMap<>();

    try (BufferedReader in = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))) {
      String[] headers = pattern.split(in.readLine());

      CSVParser parser = new CSVParser(in,
          CSVFormat.DEFAULT.withRecordSeparator("\r\n").withQuote('"').withDelimiter(',')
              .withAllowMissingColumnNames(true));

      for (CSVRecord record : parser) {
        int recordLength = record.size();
        Writer json = new StringWriter();
        JsonGenerator gen = fac.createGenerator(json);
        gen.setPrettyPrinter(new MinimalPrettyPrinter("\n"));
        gen.writeStartObject();

        String id = record.get(0);
        for (int i = 0; i < headers.length; i++) {
          String value = i < recordLength ? record.get(i) : null;
          // TODO: check for int/float values and save it as it is instead of STRING
          if (value == null || value.equalsIgnoreCase("")) {
            gen.writeNullField(headers[i]);
          } else {
            gen.writeStringField(headers[i], value);
          }
        }
        gen.writeEndObject();
        gen.close();
        jsonStringMap.put(id, json.toString());
        totalCount++;
        count++;

        if (count == batchSize) {
          jsonStringMaps.add(jsonStringMap);
          jsonStringMap = new HashMap<>();
          count = 1;
        }
      }
      if (jsonStringMap.isEmpty() == false) {
        jsonStringMaps.add(jsonStringMap);
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      return null;
    }
    System.out.println("total records in the list of maps: " + totalCount);
    return jsonStringMaps;
  }


  public static String[] getHeaders(S3Object s3Object) throws Exception {
    Pattern pattern = Pattern.compile(",");
    BufferedReader in = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
    String[] headers = pattern.split(in.readLine());
    return headers;
  }

  public static CSVParser getCsvParser(S3Object response) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(response.getObjectContent()))) {
      return new CSVParser(in, CSVFormat.DEFAULT.withRecordSeparator("\r\n").withQuote('"').withDelimiter(',')
          .withAllowMissingColumnNames(true));
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      return null;
    }
  }
}
