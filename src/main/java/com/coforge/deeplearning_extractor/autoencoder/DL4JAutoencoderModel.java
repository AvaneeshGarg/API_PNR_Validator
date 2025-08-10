package com.coforge.deeplearning_extractor.autoencoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bson.Document;
import org.bson.types.Binary;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.layers.AutoEncoder;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class DL4JAutoencoderModel {
    private MultiLayerNetwork model;
    private double threshold;

    public DL4JAutoencoderModel(int inputSize, int encodingSize, double threshold) {
        this.threshold = threshold;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam())
                .list()
                // Encoder
                .layer(0, new AutoEncoder.Builder()
                        .nIn(inputSize)
                        .nOut(encodingSize)
                        .activation(Activation.RELU)
                        .build())
                // Decoder
                .layer(1, new OutputLayer.Builder()
                        .nIn(encodingSize)
                        .nOut(inputSize) 
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build())
                .setInputType(InputType.feedForward(inputSize))
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));
    }

    public void train(double[][] inputs, int epochs) {
        INDArray input = Nd4j.create(inputs);
        for (int i = 0; i < epochs; i++) {
            model.fit(input, input);
        }
    }

    public double reconstructionError(double[] features) {
        INDArray input = Nd4j.create(new double[][]{features});
        INDArray output = model.output(input, false);
        return input.distance2(output);
    }

    public boolean isAnomaly(double[] features) {
        return reconstructionError(features) > threshold;
    }
    
    public void saveModel(String path) throws IOException {
        ModelSerializer.writeModel(model, new File(path), true);
    }

    public void loadModel(String path) throws IOException {
        this.model = ModelSerializer.restoreMultiLayerNetwork(new File(path));
    }
    
    public void saveModelToMongo(String mongoUri, String dbName, String collectionName, String modelId) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ModelSerializer.writeModel(model, baos, true);
        byte[] modelBytes = baos.toByteArray();
        baos.close();

        try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
            MongoDatabase database = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            Document doc = new Document("_id", modelId)
                    .append("model", new Binary(modelBytes));
            collection.replaceOne(new Document("_id", modelId), doc, new ReplaceOptions().upsert(true));
        }
    
    }
    
 // Load the model from MongoDB
    public void loadModelFromMongo(String mongoUri, String dbName, String collectionName, String modelId) throws IOException {
        try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
            MongoDatabase database = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            Document doc = collection.find(new Document("_id", modelId)).first();
            if (doc == null) throw new FileNotFoundException("Model not found in MongoDB with id: " + modelId);

            byte[] modelBytes = doc.get("model", Binary.class).getData();
            ByteArrayInputStream bais = new ByteArrayInputStream(modelBytes);
            this.model = ModelSerializer.restoreMultiLayerNetwork(bais);
            bais.close();
        }
    }
}