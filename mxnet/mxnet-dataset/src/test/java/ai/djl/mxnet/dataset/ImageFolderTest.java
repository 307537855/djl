/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package ai.djl.mxnet.dataset;

import ai.djl.Model;
import ai.djl.modality.cv.util.BufferedImageUtils;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.training.Activation;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.dataset.Batch;
import ai.djl.training.initializer.Initializer;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ImageFolderTest {

    @Test
    public void testImageFolder() throws IOException {
        TrainingConfig config = new DefaultTrainingConfig(Initializer.ONES);

        try (Model model = Model.newInstance()) {
            model.setBlock(Activation.IDENTITY_BLOCK);

            ImageFolder dataset =
                    new ImageFolder.Builder()
                            .setRoot("src/test/resources/imagefolder")
                            .setResize(100, 100)
                            .setSequenceSampling(1, false)
                            .build();
            dataset.prepare();

            try (Trainer trainer = model.newTrainer(config)) {
                NDManager manager = trainer.getManager();
                NDArray cat =
                        BufferedImageUtils.readFileToArray(
                                manager, Paths.get("src/test/resources/imagefolder/cat/cat2.jpeg"));
                NDArray dog =
                        BufferedImageUtils.readFileToArray(
                                manager,
                                Paths.get("src/test/resources/imagefolder/dog/puppy1.jpg"));

                Iterator<Batch> ds = trainer.iterateDataset(dataset).iterator();

                Batch catBatch = ds.next();
                Assertions.assertAlmostEquals(
                        NDImageUtils.resize(cat, 100, 100).expandDims(0),
                        catBatch.getData().head());
                Assert.assertEquals(manager.create(new int[] {0}), catBatch.getLabels().head());
                catBatch.close();

                Batch dogBatch = ds.next();
                Assertions.assertAlmostEquals(
                        NDImageUtils.resize(dog, 100, 100).expandDims(0),
                        dogBatch.getData().head());
                Assert.assertEquals(manager.create(new int[] {1}), dogBatch.getLabels().head());
                dogBatch.close();
            }
        }
    }
}
