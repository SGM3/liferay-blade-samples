package search.facet.displayer.portlet.portlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class D3JsonTracker {

    public D3JsonTracker(String xf, String yf, String yl, String d3f){
        axisProperties = new D3AxisProperties();
        axisProperties.setAxisxfield(xf);
        axisProperties.setAxisyfield(yf);
        axisProperties.setAxisylabel(yl);
        axisProperties.setAxisyd3format(d3f);
        d3Columns = new D3Columns(axisProperties);
    }

    public static class D3AxisProperties {

        public String getAxisxfield() {
            return axisxfield;
        }

        public void setAxisxfield(String axisxfield) {
            this.axisxfield = axisxfield;
        }

        public String getAxisyfield() {
            return axisyfield;
        }

        public void setAxisyfield(String axisyfield) {
            this.axisyfield = axisyfield;
        }

        public String getAxisylabel() {
            return axisylabel;
        }

        public void setAxisylabel(String axisylabel) {
            this.axisylabel = axisylabel;
        }

        public String getAxisyd3format() {
            return axisyd3format;
        }

        public void setAxisyd3format(String axisyd3format) {
            this.axisyd3format = axisyd3format;
        }

        public String getAxisyticks() {
            return axisyticks;
        }

        public void setAxisyticks(String axisyticks) {
            this.axisyticks = axisyticks;
        }

        private String axisxfield;
        private String axisyfield;
        private String axisylabel;
        private String axisyd3format;
        private String axisyticks;
    }

    public static class  D3Columns {
        public D3Columns(D3AxisProperties d3ap){
            xField = d3ap.getAxisxfield();
            yField = d3ap.getAxisyfield();
            entries = new ArrayList<>();
        }

        public void addEntry(String x, String y){
            Map<String, String> m = new HashMap<>();
            m.put(xField,x);
            m.put(yField,y);
            entries.add(m);
        }

        public List<Map<String, String>> getEntries() {
            return entries;
        }

        private List<Map<String, String>> entries;
        private String xField;
        private String yField;
    }

    public void addEntry(String x, String y){
        d3Columns.addEntry(x, y);
    }

    public void clearAllEntries(){
        d3Columns = new D3Columns(axisProperties);
    }

    public String getJsonAxisProperties() {
        if (axisProperties.getAxisyd3format().equals("d")){
            String yf = axisProperties.getAxisyfield();
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (Map<String, String> entry: d3Columns.getEntries()){
                int val = Integer.parseInt(entry.get(yf));
                min = val < min? val : min;
                max = val > max? val : max;
            }
            if (max > min){
                int diff = max - min + 1;
                axisProperties.setAxisyticks(
                    "" + (diff > MAX_TICKS ? MAX_TICKS : diff));
            }
        }
        try {
            return "'" + _mapper.writeValueAsString(axisProperties) + "'";
        } catch (JsonProcessingException e) {
            return "'{}'";
        }
    }

    public String getJsonList() {
        try {
            return  "'" + _mapper.writeValueAsString(d3Columns) + "'";
        } catch (JsonProcessingException e) {
            return "'{}'";
        }
    }

    private D3Columns d3Columns;
    private D3AxisProperties axisProperties;

    private static final int MAX_TICKS = 10;

    private static ObjectMapper _mapper = new ObjectMapper();
}
