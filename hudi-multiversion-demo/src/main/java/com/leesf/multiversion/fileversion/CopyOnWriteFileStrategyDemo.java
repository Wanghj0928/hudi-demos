package com.leesf.multiversion.fileversion;

import com.leesf.data.CustomDataGenerator;
import com.leesf.data.OpType;
import com.leesf.multiversion.MultiVersionDemo;
import com.leesf.multiversion.commits.CommitStrategyMultiVersion;
import org.apache.hudi.common.model.HoodieCleaningPolicy;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import java.util.HashMap;
import java.util.Map;

/**
 * KEEP_LATEST_FILE_VERSIONS Strategy, will retain one version of files.
 */
public class CopyOnWriteFileStrategyDemo extends CommitStrategyMultiVersion {
    private static String basePath = "/tmp/multiversion/file/copyonwrite/";

    public CopyOnWriteFileStrategyDemo(Map<String, String> properties) {
        super(properties, basePath);
    }

    public static void main(String[] args) {
        Map<String, String> config = new HashMap<>();
        // use HoodieCleaningPolicy.KEEP_LATEST_FILE_VERSIONS and retains max 1 version of data files.
        config.put("hoodie.cleaner.fileversions.retained", "1");
        config.put("hoodie.cleaner.policy", HoodieCleaningPolicy.KEEP_LATEST_FILE_VERSIONS.name());

        MultiVersionDemo cowMultiVersionDemo = new CopyOnWriteFileStrategyDemo(config);

        Dataset<Row> dataset = CustomDataGenerator.getCustomDataset(10, OpType.INSERT, spark);

        cowMultiVersionDemo.writeHudi(dataset, SaveMode.Overwrite);

        // update 10 times to shanghai partition.
        for (int i = 0; i < 10; i++) {
            dataset = CustomDataGenerator.getCustomDataset(10, OpType.UPDATE, i, "shanghai", spark);
            cowMultiVersionDemo.writeHudi(dataset, SaveMode.Append);
        }
    }

    @Override
    public String tableType() {
        return "COPY_ON_WRITE";
    }
}
