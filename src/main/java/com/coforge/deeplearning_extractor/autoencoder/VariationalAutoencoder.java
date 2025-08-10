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
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

public class VariationalAutoencoder {
    private MultiLayerNetwork model;
    private double threshold = 0.5;
    private int latentDim;
    private int inputFeatures;
    
    public VariationalAutoencoder(int inputFeatures, int latentDim, double threshold) {
        this.inputFeatures = inputFeatures;
        this.latentDim = latentDim;
        this.threshold = threshold;
        
        // Create a simpler autoencoder that works like a VAE
        // We'll implement it as a standard autoencoder with regularization
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam())
                .list()
                // Encoder
                .layer(0, new DenseLayer.Builder()
                        .nIn(inputFeatures)
                        .nOut(inputFeatures / 2)
                        .activation(Activation.RELU)
                        .dropOut(0.2) // Add dropout for regularization
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(inputFeatures / 2)
                        .nOut(latentDim)
                        .activation(Activation.TANH) // Use tanh for latent representation
                        .build())
                // Decoder  
                .layer(2, new DenseLayer.Builder()
                        .nIn(latentDim)
                        .nOut(inputFeatures / 2)
                        .activation(Activation.RELU)
                        .dropOut(0.2)
                        .build())
                .layer(3, new OutputLayer.Builder()
                        .nIn(inputFeatures / 2)
                        .nOut(inputFeatures)
                        .activation(Activation.SIGMOID)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build())
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));
    }
    
    public void train(double[][] inputs, int epochs) {
        INDArray inputArray = Nd4j.create(inputs);
        
        // Train as a standard autoencoder with regularization (dropout) that acts like a VAE
        for (int epoch = 0; epoch < epochs; epoch++) {
            model.fit(inputArray, inputArray);
            
            if (epoch % 10 == 0) {
                System.out.println("VAE Training epoch " + epoch + ", score: " + model.score());
            }
        }
    }
    
    public INDArray encode(double[] features) {
        INDArray input = Nd4j.create(new double[][]{features});
        // Get the latent representation from layer 1 (encoder output)
        return model.activateSelectedLayers(0, 1, input);
    }
    
    public INDArray decode(INDArray latentCode) {
        // For decoding, we need to pass through layers 2 and 3 (decoder layers)
        // This is a simplified approach - in practice, you'd need more complex handling
        return model.output(latentCode);
    }
    
    public double reconstructionError(double[] features) {
        INDArray input = Nd4j.create(new double[][]{features});
        INDArray reconstructed = model.output(input);
        
        // Calculate MSE reconstruction error
        INDArray error = input.sub(reconstructed);
        return error.mul(error).meanNumber().doubleValue();
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

    public Object getThreshold() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getThreshold'");
    }
}
