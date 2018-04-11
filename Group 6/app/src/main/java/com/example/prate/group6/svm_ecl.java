package com.example.prate.group6;
import libsvm.*;


public class svm_ecl {
    public static double[] features;
    public static svm_model model;
    public static void svmTrain(double[][] train, double[] train_label) {
        svm_problem prob = new svm_problem();
        int dataCount = train.length;
        prob.y = new double[dataCount];
        prob.l = dataCount;
        prob.x = new svm_node[dataCount][];

        for (int i = 0; i < dataCount; i++) {
            features = train[i];
            prob.x[i] = new svm_node[features.length - 1];
            for (int j = 0; j < features.length - 1; j++) {
                svm_node node = new svm_node();
                node.index = j;
                node.value = features[j];
                prob.x[i][j] = node;
            }
            prob.y[i] = train_label[i];
        }
        svm_parameter param = new svm_parameter();
        param.probability = 1;
        param.gamma = 0.5;
        param.nu = 0.5;
        param.C = 1;
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 20000;
        param.eps = 0.001;

        model = svm.svm_train(prob, param);
        double[] target = new double[train_label.length];
        svm.svm_cross_validation(prob, param, 4, target);
        double correctCounter = 0;
        for (int i = 0; i < target.length; i++) {
            if (target[i] == train_label[i]) {
                correctCounter++;
            }
        }
        double cross_val=(correctCounter*100)/train_label.length;
        System.out.println("Cross_val"+cross_val);

        //return model;
    }

    public static double svmTest(double[][] test, double[] test_label) {
        svm_node[][] nodes = new svm_node[12][features.length - 1];
        double res;
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < features.length - 1; j++) {
                svm_node node = new svm_node();
                node.index = j;
                node.value = test[i][j];
                nodes[i][j] = node;

            }
        }
        int totalClasses = 3;
        float count = 0;
        int[] labels = new int[totalClasses];
        svm.svm_get_labels(model, labels);

        double[] prob_estimates = new double[totalClasses];
        for (int i = 0; i < 12; i++) {
            res = svm.svm_predict(model, nodes[i]);
            System.out.println("actual=" + test_label[i] + "res=" + res);
            if (test_label[i] == res)
                count += 1;
        }
        float accuracy = (count / 12) * 100;
        return accuracy;

    }
}