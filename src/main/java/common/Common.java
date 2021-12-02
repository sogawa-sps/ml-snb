/*******************************************************************************
 * sogawa-sps
 *
 * This program is made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package common;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Common
{
    public static double[][] loadData(List<String> labels, int rows, String inputFile) throws IOException, InterruptedException
    {
        double[][] data = null;
        int size = getFileSize(rows, inputFile);
        try(CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"))
                .withSkipLines(1)
                .build();) {
            for(Iterator<String[]> iter = reader.iterator();iter.hasNext();)
            {
                String[] line = iter.next();
                labels.add(line[0]);
                int n = labels.size() - 1;
                if(data == null)
                {
                    data = new double[size][line.length - 1];
                }
                for(int i = 1; i < line.length; i++)
                {
                    data[n][i - 1] = Double.valueOf(line[i]);
                }
                if(n%10000 == 0 || n == size - 1|| !iter.hasNext())
                {
                    System.out.println("Processed row: " + n);
                }
                if(rows != 0 && n == size - 1)
                {
                    break;
                }
            }
        }
        return data;
    }

    private static int getFileSize(int rows, String inputFile) throws IOException 
    {
        int size;
        if(rows != 0)
        {
            size = rows;
        }
        else
        {
            size = 0;
            try(CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"))
                    .withSkipLines(1)
                    .build();) {
                for (Iterator<String[]> iter = reader.iterator(); iter.hasNext(); ) {
                    iter.next();
                    size++;
                }
            }
        }
        System.out.println("Data set size: " + size);
        return size;
    }

    public static void saveAsCsv(double[][] data, List<String> rowLabels, List<String> columnLabels, String outputFile) throws Exception 
    {
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw))
        {
            writer.append("labels,");
            columnLabels.forEach(l -> writer.append(l).append(","));
            writer.append("\n");
            for (int i = 0; i < rowLabels.size(); i++) {
                writer.append(rowLabels.get(i)).append(",");
                writer.append(DoubleStream.of(data[i])
                        .mapToObj(n -> Double.toString(n))
                        .collect(Collectors.joining( "," )));
                writer.append("\n");
            }
        }
    }
    
    public static void saveScatter(double[][] data, String outputFile) throws Exception
    {
        saveScatter(data, outputFile, null, null);
    }
    
    public static void saveScatter(List<String> labels, double[][] data, String outputFile) throws Exception
    {
        saveScatter(data, outputFile, labels, null);
    }
    
    public static void saveScatter(double[][] data, String outputFile, List<String> labels, Map<String, String> labelsOverride) throws Exception
    {
        Map<String, XYSeries> seriesMap = new HashMap<>();
        for(int i = 0; i < data.length; i++)
        {
            String label = labels == null ? "data" : labels.get(i);
            XYSeries series = seriesMap.get(label);
            if(series == null)
            {
                series = new XYSeries(labelsOverride == null? label : labelsOverride.get(label));
                seriesMap.put(label, series);
            }
            double[] item = data[i];
            series.add(item[0], item[1]);
        }
        XYSeriesCollection dataSet = new XYSeriesCollection();
        seriesMap.forEach((k,v) -> dataSet.addSeries(v));
        saveScatter(dataSet, outputFile);
    }

    public static void saveScatter(XYSeriesCollection dataSet, String outputFile) throws Exception
    {
        JFreeChart chart = ChartFactory.createScatterPlot("", "", "", dataSet,
                PlotOrientation.HORIZONTAL, true, true, false);
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(new Color(255,255,255));
        Shape shape = new Ellipse2D.Double(2, 2, 2, 2);
        XYItemRenderer renderer = plot.getRenderer();
        for(int i = 0; i < dataSet.getSeriesCount(); i++)
        {
            renderer.setSeriesShape(i, shape);
        }
        Shape legendShape = new Rectangle2D.Double(0, 0, 17, 13);
        LegendItemCollection newLIC = new LegendItemCollection();
        for(Iterator<LegendItem> iter = plot.getLegendItems().iterator(); iter.hasNext();)
        {
            LegendItem exLI = iter.next();
            LegendItem newLI = new LegendItem(exLI.getLabel(), exLI.getDescription(), exLI.getToolTipText(), exLI.getURLText(),
                    true, legendShape, true, exLI.getFillPaint(),
                    false, exLI.getOutlinePaint(), exLI.getOutlineStroke(),
                    false, exLI.getLine(), exLI.getLineStroke(), exLI.getLinePaint());
            newLIC.add(newLI);
        }
        plot.setFixedLegendItems(newLIC);
        try (OutputStream out = new FileOutputStream(outputFile);)
        {
            ChartUtilities.writeChartAsPNG(out, chart, 1024, 768);
        }
    }
}
