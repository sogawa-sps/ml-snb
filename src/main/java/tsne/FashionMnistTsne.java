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

package tsne;

import com.jujutsu.tsne.TSneConfiguration;
import com.jujutsu.tsne.barneshut.BarnesHutTSne;
import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.TSneUtils;
import com.opencsv.CSVWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static common.Common.*;

/**
 * Working "Fashion MNIST" t-SNE example using https://github.com/lejon/T-SNE-Java<br><br>
 *
 * Based on "fashion-mnist" dataset: https://www.kaggle.com/zalando-research/fashionmnist<br>
 * Please download "fashion-mnist_train.csv" file and put it into "[HOME]/fashion-mnist-example" folder<br><br>
 *
 * Inspired by: https://m.habr.com/ru/company/newprolab/blog/350584/
*/
public class FashionMnistTsne 
{
    final static String PATH = System.getProperty("user.home") + "\\fashion-mnist-example\\";
    final static int ROWS = 0;

    public static void main(String[] args) throws Exception
    {
        List<String> labels = new ArrayList<>();
        double[][] data = loadData(labels, ROWS, PATH + "\\fashion-mnist_train.csv");

        int initial_dims = 55;
        double perplexity = 5;
        BarnesHutTSne tsne = new ParallelBHTsne();
        TSneConfiguration config = TSneUtils.buildConfig(data, 2, initial_dims, perplexity, 500);
        
        long t = System.currentTimeMillis();
        double [][] emb = tsne.tsne(config); 
        System.out.println("Elapsed time: " + (System.currentTimeMillis() - t)/1000 + " s");

        saveAsFile(labels, emb, PATH + "\\fashion-mnist_train-emb.csv");
        saveScatter(emb, PATH + "\\fashion-mnist_train-emb.png", labels, LABELS_NAMES);
    }
    
    public static void saveAsFile(List<String> labels, double[][] emb, String outputFile) throws Exception
    {
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw)) {
            for (int i = 0; i < labels.size(); i++) {
                String[] line = {Double.toString(emb[i][0]), Double.toString(emb[i][1]), labels.get(i), LABELS_NAMES.get(labels.get(i))};
                writer.writeNext(line);
            }
        }
    }
    
    private static final Map<String, String> LABELS_NAMES = new HashMap()
    {{
        put("0", "T-shirt/top");
        put("1", "Trouser");
        put("2", "Pullover");
        put("3", "Dress");
        put("4", "Coat");
        put("5", "Sandal");
        put("6", "Shirt");
        put("7", "Sneaker");
        put("8", "Bag");
        put("9", "Ankle boot");
    }};
}
