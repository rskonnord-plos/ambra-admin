package org.ambraproject.admin.action;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ambraproject.models.Article;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Generates Java source code for unit tests. It uses this running program as a reference implementation of an article
 * ingestion, and produces test cases that assert that some future version/program ingests an article the same way.
 * <p/>
 * This tool is meant only to be run locally on development machines. It's not meant ever to be merged back into
 * development code. If you see this class in a production version of the program, something has gone extremely wrong.
 * <p/>
 * To use this, define the input and output by editing the two String constants at the top, then rebuild and run Admin.
 * Kick the process off by opening the front page. Code has been shamelessly spliced into {@link AdminTopAction} to
 * ingest the zip file and then write JSON to the two specified paths.
 */
public class IngestTestCaseGenerator {

  // Edit these before building to specify input and output.
  // These are local paths to the server's (i.e., your workstations's) native file system.
  public static final String ingestCasePath = "/home/rskonnord/rhino/src/test/python/case_generation/output";
  public static final String jsonSavePath = ingestCasePath;


  private static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()

      .setExclusionStrategies(new ExclusionStrategy() {
        private final Set<String> transientFieldNames = new HashSet<String>(Arrays.asList(
            "ID", "created", "lastModified"));

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
          return transientFieldNames.contains(f.getName());
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
          return clazz.isAssignableFrom(Class.class);
        }
      })

      .create();

  public static void writeCase(Article article) throws IOException {
    String articleJson = GSON.toJson(article);

    Writer output = null;
    try {
      output = new FileWriter(new File(jsonSavePath));
      output = new BufferedWriter(output);
      output.write(articleJson);
    } finally {
      if (output != null) output.close();
    }
  }


}
