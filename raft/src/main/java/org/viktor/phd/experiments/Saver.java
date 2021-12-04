package org.viktor.phd.experiments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.viktor.phd.client.ApplicationDetails;
import org.viktor.phd.client.RecordedData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Saver {

    private final String basePath = "experiment_results/";
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public void save(ExperimentResults experimentResults) {
        try {
            String filePath = basePath + experimentResults.getExpId() +
                    "-" + experimentResults.getType().name() +
                    "-" + experimentResults.getApplicationId() + ".json";

            Path path = Path.of(filePath);
            Files.deleteIfExists(path);
            Files.createFile(path);
            String json = ow.writeValueAsString(experimentResults);
            BufferedWriter w = Files.newBufferedWriter(path);
            w.write(json);
            w.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
