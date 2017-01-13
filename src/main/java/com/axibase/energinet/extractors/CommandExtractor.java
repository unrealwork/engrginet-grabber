package com.axibase.energinet.extractors;

import com.axibase.tsd.model.data.series.Sample;
import com.axibase.tsd.network.InsertCommand;
import com.axibase.tsd.network.PlainCommand;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

public class CommandExtractor implements Extractor<Collection<PlainCommand>, String[][]> {
    Logger log = org.slf4j.LoggerFactory.getLogger(CommandExtractor.class);
    private Map<String, Map<String, Map<String, String>>> metricDescription;

    public CommandExtractor(Map<String, Map<String, Map<String, String>>> metricDescription) {
        this.metricDescription = metricDescription;
    }

    public Collection<PlainCommand> extract(String[][] table) {
        List<PlainCommand> commands = new ArrayList<PlainCommand>();
        int height = table.length;
        int width = table[0].length;


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));


        for (int r = 2; r < height; r++) {
            long date = format.parse(table[r][0], new java.text.ParsePosition(0)).getTime() / 1000L;
            date += (Long.parseLong(table[r][1]) - 1L) * 3600L;
            date *= 1000L;

            String DEFAULT_ENTITY = "energinet.dk";

            for (int c = 2; c < width; c++) {
                PlainCommand plainCommand = null;
                if ((!org.apache.commons.lang3.StringUtils.isBlank(table[r][c])) && (!table[r][c].equals(" "))) {
                    String metric = table[0][c];
                    String name = table[1][c];
                    String value = table[r][c];
                    try {
                        Map<String, String> tags = this.metricDescription.get(metric).get(name);

                        metric = tags.get("metric").replace(' ', '_').trim();
                        Map<String, String> commandTags = new HashMap<String, String>();
                        for (String tagName : tags.keySet()) {
                            if ((!tagName.equals("metric")) && (!tagName.equals("unit"))) {
                                commandTags.put(tagName, tags.get(tagName));
                            }
                        }
                        plainCommand = new InsertCommand(DEFAULT_ENTITY, metric, new Sample(date, Double.parseDouble(value)), commandTags);
                    } catch (Exception e) {
                        this.log.warn("Fail to create network command!", e.getMessage());
                        plainCommand = null;
                    }
                }

                if (plainCommand != null) {
                    commands.add(plainCommand);
                }
            }
        }
        this.log.info("Exatracted commands: {}", commands.size());
        return commands;
    }
}